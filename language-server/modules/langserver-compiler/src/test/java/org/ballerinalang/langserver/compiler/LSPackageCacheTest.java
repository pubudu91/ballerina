package org.ballerinalang.langserver.compiler;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.repository.WorkspacePackageRepository;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Test Case for Lang Server Package Caching.
 */
public class LSPackageCacheTest {

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @Test
    public void testInvalidate() throws Exception {
        Path filePath = RES_DIR.resolve("source").resolve("singlepackage").resolve("io-sample.bal");
        compileFileAndCheckCache(filePath);
        filePath = RES_DIR.resolve("source").resolve("multipackages").resolve("sample").resolve("main.bal");
        compileFileAndCheckCache(filePath);
    }

    private void compileFileAndCheckCache(Path filePath)
            throws IOException, CompilationFailedException, WorkspaceDocumentException {
        // Read test bal file
        String content = new String(Files.readAllBytes(filePath));
        // Prepare compiler resources
        String sourceRoot = LSCompilerUtil.getProjectRoot(filePath);
        LSDocument lsDocument = new LSDocument(sourceRoot);
        String pkgName = lsDocument.getOwnerModule();
        LSDocument sourceDocument = new LSDocument(filePath, sourceRoot);
        WorkspaceDocumentManagerImpl documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        PackageRepository packageRepository = new WorkspacePackageRepository(sourceRoot, documentManager);
        PackageID packageID = new PackageID(Names.ANON_ORG, new Name(pkgName), Names.DEFAULT_VERSION);
        CompilerContext context = LSCompilerUtil.prepareCompilerContext(packageID, packageRepository, sourceDocument,
                                                                        documentManager, false);
        documentManager.updateFile(filePath, content);
        // Compile test bal file
//        LSModuleCompiler lsCompiler = new LSModuleCompiler(documentManager);
        ExtendedLSCompiler.compileFile(filePath, CompilerPhase.TAINT_ANALYZE);

        // Check cache whether it still holds the current package
        Set<String> packageMayKeySet = LSPackageCache.getInstance(context).getPackageMap().keySet();
        PackageID nPackageIDDemo = new PackageID(new Name("demo"), new Name(pkgName), new Name("0.0.1"));
        PackageID nPackageIDAnon = new PackageID(Names.ANON_ORG, new Name(pkgName), Names.DEFAULT_VERSION);
        Assert.assertFalse(packageMayKeySet.contains(nPackageIDDemo.toString()));
        Assert.assertFalse(packageMayKeySet.contains(nPackageIDAnon.toString()));
    }
}
