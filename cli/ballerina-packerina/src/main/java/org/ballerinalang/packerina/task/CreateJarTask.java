/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.util.BootstrapRunner;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Task for creating jar file.
 */
public class CreateJarTask implements Task {

    private boolean dumpBir;

    public CreateJarTask(boolean dumpBir) {
        this.dumpBir = dumpBir;
    }
    
    @Override
    public void execute(BuildContext buildContext) {
        
        // This will avoid initializing Config registry during jar creation.
        ConfigRegistry.getInstance().setInitialized(true);
        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
        Path projectBIRCache = buildContext.get(BuildContextField.BIR_CACHE_DIR);
        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        Path tmpDir = targetDir.resolve(ProjectDirConstants.TARGET_TMP_DIRECTORY);
        Path homeBIRCache = buildContext.getBirCacheFromHome();
        Path systemBIRCache = buildContext.getSystemRepoBirCache();
        
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        for (BLangPackage module : moduleBirMap) {
            writeImportJar(tmpDir, module.symbol.imports, sourceRoot, buildContext,
                    projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
            
            // get the bir path of the module
            Path entryBir = buildContext.getBirPathFromTargetCache(module.packageID);
            
            // get the jar path of the module.
            Path jarOutput = buildContext.getJarPathFromTargetCache(module.packageID);

            BootstrapRunner.loadTargetAndGenerateJarBinary(tmpDir,
                    entryBir.toString(), jarOutput.toString(), this.dumpBir,
                    projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());

            // If there is a testable package we will create testable jar.
            if (module.hasTestablePackage()) {
                // get the bir path of the module
                Path testBir = buildContext.getTestBirPathFromTargetCache(module.packageID);

                // get the jar path of the module.
                Path testJarOutput = buildContext.getTestJarPathFromTargetCache(module.packageID);

                BootstrapRunner.loadTargetAndGenerateJarBinary(tmpDir,
                        testBir.toString(), testJarOutput.toString(), this.dumpBir,
                        projectBIRCache.toString(), homeBIRCache.toString(), systemBIRCache.toString());
            }

        }
        ConfigRegistry.getInstance().setInitialized(false);
    }
    
    private void writeImportJar(Path tmpDir, List<BPackageSymbol> imports, Path sourceRoot, BuildContext buildContext,
                                String... reps) {
        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
                continue;
            }
            Path jarFilePath;
            Path birFilePath;
            // If the module is part of the project write it to project jar cache check if file exist
            // If not write it to home jar cache
            // skip ballerina and ballerinax
            if (ProjectDirs.isModuleExist(sourceRoot, id.name.value)  ||
                                                                buildContext.getImportPathDependency(id).isPresent()) {
                jarFilePath = buildContext.getJarPathFromTargetCache(id);
                birFilePath = buildContext.getBirPathFromTargetCache(id);
            } else {
                jarFilePath = buildContext.getJarPathFromHomeCache(id);
                birFilePath = buildContext.getBirPathFromHomeCache(id);
            }
            if (!Files.exists(jarFilePath)) {
                BootstrapRunner.loadTargetAndGenerateJarBinary(tmpDir,
                        birFilePath.toString(), jarFilePath.toString(), this.dumpBir, reps);
            }
            writeImportJar(tmpDir, bimport.imports, sourceRoot, buildContext, reps);
        }
    }
}
