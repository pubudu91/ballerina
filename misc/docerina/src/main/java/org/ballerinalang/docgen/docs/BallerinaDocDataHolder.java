/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.docgen.model.ModuleDoc;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds ballerina doc generation data.
 */
public class BallerinaDocDataHolder {

    private static final BallerinaDocDataHolder instance = new BallerinaDocDataHolder();
    /**
     * Key - package name.
     * Value - {@link ModuleDoc}.
     */
    private Map<String, ModuleDoc> packageMap;
    private String orgName;
    private String version;

    protected BallerinaDocDataHolder() {
        packageMap = new HashMap<String, ModuleDoc>();
    }

    public static BallerinaDocDataHolder getInstance() {
        return instance;
    }

    public Map<String, ModuleDoc> getPackageMap() {
        return packageMap;
    }

    public void setPackageMap(Map<String, ModuleDoc> packageMap) {
        this.packageMap = packageMap;
    }

    public String getOrgName() {
        if (orgName != null) {
            return orgName;
        }
        orgName = ConfigRegistry.getInstance().getAsString(BallerinaDocConstants.ORG_NAME);
        if (orgName == null) {
            orgName = System.getProperty(BallerinaDocConstants.ORG_NAME);
        }
        return orgName;
    }

    public String getVersion() {
        if (version != null) {
            return version;
        }
        version = ConfigRegistry.getInstance().getAsString(BallerinaDocConstants.VERSION);
        if (version == null) {
            version = System.getProperty(BallerinaDocConstants.VERSION);
        }
        return version;
    }
}
