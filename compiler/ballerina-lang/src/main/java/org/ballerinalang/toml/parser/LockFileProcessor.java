/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.parser;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.toml.model.LockFile;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * LockFile Processor which processes the toml file parsed and populate a {@link LockFile}.
 *
 * @since 0.973.1
 */
public class LockFileProcessor {
    private static final PrintStream err = System.err;
    private static final CompilerContext.Key<LockFileProcessor> LOCK_FILE_PROC_KEY = new CompilerContext.Key<>();
    private final LockFile lockFile;

    private LockFileProcessor(LockFile lockFile) {
        this.lockFile = lockFile;
    }

    /**
     * Get an instance of the LockFileProcessor.
     *
     * @param context compiler context
     * @param lockEnabled if lock is enabled or not
     * @return instance of LockFileProcessor
     */
    public static LockFileProcessor getInstance(CompilerContext context, boolean lockEnabled) {
        if (!lockEnabled) {
            return new LockFileProcessor(new LockFile());
        }
        LockFileProcessor lockFileProcessor = context.get(LOCK_FILE_PROC_KEY);
        if (lockFileProcessor == null) {
            SourceDirectory sourceDirectory = context.get(SourceDirectory.class);
            LockFile lockFile = LockFileProcessor.parseTomlContentAsStream(sourceDirectory.getLockFileContent());
            LockFileProcessor instance = new LockFileProcessor(lockFile);
            context.put(LOCK_FILE_PROC_KEY, instance);
            return instance;
        }
        return lockFileProcessor;
    }

    /**
     * Get the char stream from inputstream.
     *
     * @param inputStream inputstream of the toml file content
     * @return lockFile object
     */
    public static LockFile parseTomlContentAsStream(InputStream inputStream) {
        try {
            Toml lockToml = new Toml().read(inputStream);
            return lockToml.to(LockFile.class);
        } catch (Exception e) {
            err.println("Ballerina.lock file is corrupted. this build will ignore using the lock file in resolving " +
                        "dependencies. a valid lock file will be generated if '--skip-lock' is set to false when " +
                        "using build command. '--skip-lock' flag is false by default.");
        }
        
        return null;
    }

    public LockFile getLockFile() {
        return this.lockFile;
    }
}
