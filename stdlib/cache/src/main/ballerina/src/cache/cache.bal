// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/system;
import ballerina/task;
import ballerina/time;

# Cache cleanup task starting delay in ms.
const int CACHE_CLEANUP_START_DELAY = 0;

# Cache cleanup task invoking interval in ms.
const int CACHE_CLEANUP_INTERVAL = 5000;

# Map which stores all of the caches.
map<Cache> cacheMap = {};

task:TimerConfiguration cacheCleanupTimerConfiguration = {
    intervalInMillis: CACHE_CLEANUP_INTERVAL,
    initialDelayInMillis: CACHE_CLEANUP_START_DELAY
};

task:Scheduler cacheCleanupTimer = new(cacheCleanupTimerConfiguration);

boolean timerStarted = false;

# Represents a cache entry.
#
# + value - cache value
# + lastAccessedTime - last accessed time in ms of this value which is used to remove LRU cached values
type CacheEntry record {|
    any value;
    int lastAccessedTime;
|};

# Represents a Ballerina `Cache` which can hold multiple entries and remove entries based on time and size.
public type Cache object {

    private int capacity;
    map<CacheEntry> entries = {};
    int expiryTimeInMillis;
    private float evictionFactor;
    private string uuid;

    # Creates a new `Cache`.
    #
    # + expiryTimeInMillis - Time since its last access in which the cache will be expired.
    # + capacity - Maximum number of entries allowed.
    # + evictionFactor - The factor which the entries will be evicted once the cache full.
    public function __init(public int expiryTimeInMillis = 900000, public int capacity = 100, public float evictionFactor = 0.25) {

        // Cache expiry time must be a positive value.
        if (expiryTimeInMillis <= 0) {
            Error e = error(CACHE_ERROR, message = "Expiry time must be greater than 0.");
            panic e;
        }
        // Cache capacity must be a positive value.
        if (capacity <= 0) {
            Error e = error(CACHE_ERROR, message = "Capacity must be greater than 0.");
            panic e;
        }
        // Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).
        if (evictionFactor <= 0 || evictionFactor > 1) {
            Error e = error(CACHE_ERROR, message = "Cache eviction factor must be between 0.0 (exclusive) and 1.0 (inclusive).");
            panic e;
        }
        // We remove empty caches to prevent OOM issues. So in such scenarios, the cache will not be in the `cacheMap`
        // when we are trying to add a new cache entry to that cache. So we need to create a new cache. For that, keep
        // track of the UUID.
        self.uuid = system:uuid();
        cacheMap[self.uuid] = self;
        self.expiryTimeInMillis = expiryTimeInMillis;
        self.capacity = capacity;
        self.evictionFactor = evictionFactor;

        var attachCacheCleanerResult = cacheCleanupTimer.attach(cacheCleanupService);
        if (attachCacheCleanerResult is error) {
            record {| string message?; anydata|error...; |} detail = attachCacheCleanerResult.detail();
            Error e = error(CACHE_ERROR, message = "Failed to create the cache cleanup task: " +  <string> detail["message"]);
            panic e;
        }

        if (!timerStarted && !cacheCleanupTimer.isStarted()) {
            lock {
                if(!cacheCleanupTimer.isStarted()) {
                    var timerStartResult = cacheCleanupTimer.start();
                    if (timerStartResult is error) {
                        record {| string message?; anydata|error...; |} detail = timerStartResult.detail();
                        Error e = error(CACHE_ERROR, message = "Failed to start the cache cleanup task: " +  <string> detail["message"]);
                        panic e;
                    }
                    timerStarted = true;
                }
            }
        }
    }

    # Checks whether the given key has an associated cache value.
    #
    # + key - The key to be checked.
    # + return - `true` if the given key has an associated value, `false` otherwise.
    public function hasKey(string key) returns boolean {
        return self.entries.hasKey(key);
    }

    # Returns the size of the cache.
    #
    # + return - The size of the cache.
    public function size() returns int {
        return self.entries.length();
    }

    # Adds the given key, value pair to the provided cache.
    #
    # + key - Value which should be used as the key.
    # + value - Value to be cached.
    public function put(string key, any value) {
        // We need to synchronize this process otherwise concurrency might cause issues.
         lock {
            int cacheCapacity = self.capacity;
            int cacheSize = self.entries.length();

            // If the current cache is full, evict cache.
            if (cacheCapacity <= cacheSize) {
                self.evict();
            }
            // Add the new cache entry.
            int time = time:currentTime().time;
            CacheEntry entry = { value: value, lastAccessedTime: time };
            self.entries[key] = entry;

            // If the UUID is not found, that means that cache was removed after being empty. So we need to create a
            // new cache with the current cache object.
            if (!cacheMap.hasKey(self.uuid)) {
                cacheMap[self.uuid] = self;
            }
         }
    }

    # Evicts the cache when the cache is full.
    function evict() {
        int maxCapacity = self.capacity;
        float ef = self.evictionFactor;
        int numberOfKeysToEvict = <int>(maxCapacity * ef);
        // Get the above number of least recently used cache entry keys from the cache
        string[] cacheKeys = self.getLRUCacheKeys(numberOfKeysToEvict);
        // Iterate through the map and remove entries.
        foreach var c in cacheKeys {
            // These cache values are ignored. So it is not needed to check the return value for the remove function.
            var tempVar = self.entries.remove(c);
        }
    }

    # Returns the cached value associated with the given key. If the provided cache key is not found,
    # () will be returned.
    #
    # + key - Key which is used to retrieve the cached value.
    # + return - The cached value associated with the given key.
    public function get(string key) returns any? {
        // Check whether the requested cache is available.
        if (!self.hasKey(key)) {
            return ();
        }
        // Get the requested cache entry from the map.
        CacheEntry? cacheEntry = self.entries[key];

        if (cacheEntry is CacheEntry) {
            // Check whether the cache entry is already expired. Since the cache cleaning task runs in predefined intervals,
            // sometimes the cache entry might not have been removed at this point even though it is expired. So this check
            // guarantees that the expired cache entries will not be returned.
            int currentSystemTime = time:currentTime().time;
            if (currentSystemTime >= cacheEntry.lastAccessedTime + self.expiryTimeInMillis) {
                // If it is expired, remove the cache and return nil.
                self.remove(key);
                return ();
            }
            // Modify the last accessed time and return the cache if it is not expired.
            cacheEntry.lastAccessedTime = time:currentTime().time;
            return cacheEntry.value;
        } else {
            return ();
        }
    }

    # Removes a cached value from a cache.
    #
    # + key - Key of the cache entry which needs to be removed.
    public function remove(string key) {
        // Cache might already be removed by the cache clearing task. So no need to check the return value.
        if (self.entries.hasKey(key)) {
            var tempVar = self.entries.remove(key);
        }
    }

    # Returns all keys from current cache.
    #
    # + return - Array of all keys from the current cache.
    public function keys() returns string[] {
        return self.entries.keys();
    }

    # Returns the key of the least recently used cache entry.
    # This is used to remove entries if the cache is full.
    #
    # + numberOfKeysToEvict - The number of keys which should be evicted.
    # + return - Number of keys to be evicted.
    function getLRUCacheKeys(int numberOfKeysToEvict) returns string[] {
        // Create new arrays to hold keys to be removed and hold the corresponding timestamps.
        string[] cacheKeysToBeRemoved = [];
        int[] timestamps = [];
        string[] keys = self.entries.keys();
        // Iterate through the keys.
        foreach var key in keys {
            CacheEntry? cacheEntry = self.entries[key];
            if (cacheEntry is CacheEntry) {
                // Check and add the key to the cacheKeysToBeRemoved if it matches the conditions.
                checkAndAdd(numberOfKeysToEvict, cacheKeysToBeRemoved, timestamps, key, cacheEntry.lastAccessedTime);
            }
            // If the key is not found in the map, that means that the corresponding cache is already removed
            // (possibly by a another worker).
        }
        // Return the array.
        return cacheKeysToBeRemoved;
    }
};

# Removes expired cache entries from all caches.
function runCacheExpiry() {

    // We need to keep track of empty caches. We remove these to prevent OOM issues.
    int emptyCacheCount = 0;
    string[] emptyCacheKeys = [];

    // Iterate through all caches.
    int keyIndex = 0;
    string[] currentCacheKeys = cacheMap.keys();
    int cacheKeysLength = currentCacheKeys.length();
    while (keyIndex < cacheKeysLength) {

        string currentCacheKey = currentCacheKeys[keyIndex];
        keyIndex += 1;
        Cache? currentCache = cacheMap[currentCacheKey];
        if (currentCache is ()) {
            continue;
        } else {
            // Get the expiry time of the current cache
            int currentCacheExpiryTime = currentCache.expiryTimeInMillis;

            // Create a new array to store keys of cache entries which needs to be removed.
            string[] cachesToBeRemoved = [];

            int cachesToBeRemovedIndex = 0;
            // Iterate through all keys.
            int entrykeyIndex = 0;
            string[] entryKeys = currentCache.entries.keys();
            int entryKeysLength = entryKeys.length();
            while (entrykeyIndex < entryKeysLength) {

                var key = entryKeys[entrykeyIndex];
                entrykeyIndex += 1;
                CacheEntry? entry = currentCache.entries[key];
                if (entry is ()) {
                    continue;
                } else {
                    // Get the current system time.
                    int currentSystemTime = time:currentTime().time;

                    // Check whether the cache entry needs to be removed.
                    if (currentSystemTime >= entry.lastAccessedTime + currentCacheExpiryTime) {
                        cachesToBeRemoved[cachesToBeRemovedIndex] = key;
                        cachesToBeRemovedIndex += 1;
                    }
                }
            }

            // Iterate through the key list which needs to be removed.
            int currentKeyIndex = 0;
            while(currentKeyIndex < cachesToBeRemovedIndex) {
                string key = cachesToBeRemoved[currentKeyIndex];
                // Remove the cache entry.
                var tempVar = currentCache.entries.remove(key);
                currentKeyIndex += 1;
            }

            // If there are no entries, we add that cache key to the `emptyCacheKeys`.
            int size = currentCache.entries.length();
            if (size == 0) {
                emptyCacheKeys[emptyCacheCount] = currentCacheKey;
                emptyCacheCount += 1;
            }
        }
    }

    // We iterate though all empty cache keys and remove them from the `cacheMap`.
    foreach var emptyCacheKey in emptyCacheKeys {
        if (cacheMap.hasKey(emptyCacheKey)) {
            var tempVar = cacheMap.remove(emptyCacheKey);
        }
    }
    return ();
}

# Utility function to identify which cache entries should be evicted.
function checkAndAdd(int numberOfKeysToEvict, string[] cacheKeys, int[] timestamps, string key, int lastAccessTime) {
    string myKey = key;
    int myLastAccessTime = lastAccessTime;

    // Iterate while we count all values from 0 to numberOfKeysToEvict exclusive of numberOfKeysToEvict since the
    // array size should be numberOfKeysToEvict.
    foreach var index in 0..<numberOfKeysToEvict {
        // If we have encountered the end of the array, that means we can add the new values to the end of the
        // array since we haven’t reached the numberOfKeysToEvict limit.
        if (cacheKeys.length() == index) {
            cacheKeys[index] = myKey;
            timestamps[index] = myLastAccessTime;
            // Break the loop since we don't have any more elements to compare since we are at the end
            break;
        } else {
            // If the timestamps[index] > lastAccessTime, that means the cache which corresponds to the 'key' is
            // older than the current entry at the array which we are checking.
            if (timestamps[index] > myLastAccessTime) {
                // Swap the values. We use the swapped value to continue to check whether we can find any place to
                // add it in the array.
                string tempKey = cacheKeys[index];
                int tempTimeStamp = timestamps[index];
                cacheKeys[index] = myKey;
                timestamps[index] = myLastAccessTime;
                myKey = tempKey;
                myLastAccessTime = tempTimeStamp;
            }
        }
    }
}

# Cleanup service which cleans the cache periodically.
service cacheCleanupService = service {
    resource function onTrigger() {
        runCacheExpiry();
    }
};
