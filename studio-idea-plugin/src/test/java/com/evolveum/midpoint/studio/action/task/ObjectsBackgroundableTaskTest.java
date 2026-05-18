package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Covers MID-11199: Upload action must process XML files when a directory (or
 * individual files) is passed via VIRTUAL_FILE_ARRAY in the data context.
 *
 * <p>Root cause: {@code processFiles} called {@code getData(VIRTUAL_FILE_ARRAY)} on the EDT
 * without a read action. When a <em>directory</em> is selected, IntelliJ's
 * {@code VirtualFileArrayRule} enumerates PSI children — which requires a read action.
 * Without one the call returns {@code null}, {@code filterXmlFiles(null)} returns an empty
 * list, and the "No files matched" warning is shown. Individual file selections were
 * unaffected because their array is provided directly by the selection without PSI lookup.
 *
 * <p>Fix: wrap the {@code getData} call in {@code ApplicationManager.getApplication()
 * .runReadAction()} inside {@code processFiles}.
 */
public class ObjectsBackgroundableTaskTest extends BasePlatformTestCase {

    private static final String MIDPOINT_NS = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";

    private static String userXml(String oid, String name) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<user xmlns=\"" + MIDPOINT_NS + "\" oid=\"" + oid + "\">\n"
                + "    <name>" + name + "</name>\n"
                + "</user>\n";
    }

    /**
     * Minimal task subclass that records which files reach {@code loadObjectsFromFile}
     * without performing any network or XML-parsing operations.
     */
    private abstract static class FileTrackingTask extends ObjectsBackgroundableTask<TaskState> {

        final List<String> loadedFilePaths = Collections.synchronizedList(new ArrayList<>());

        FileTrackingTask(com.intellij.openapi.project.Project project, DataContext dataContext) {
            super(project, () -> dataContext, "Upload (Full Processing)", "Upload (Full Processing)");
        }

        @Override
        protected List<MidPointObject> loadObjectsFromFile(VirtualFile file) {
            loadedFilePaths.add(file.getPath());
            return List.of();
        }

        @Override
        protected ProcessObjectResult processObject(MidPointObject object) {
            return new ProcessObjectResult(null);
        }

        /** Calls {@code doRun} directly, bypassing server-init scaffolding in {@code run}. */
        void runForTest() {
            doRun(new EmptyProgressIndicator());
        }
    }

    private DataContext dataContextWith(VirtualFile... files) {
        return dataId -> PlatformDataKeys.VIRTUAL_FILE_ARRAY.getName().equals(dataId) ? files : null;
    }

    // ── tests ─────────────────────────────────────────────────────────────────────

    /**
     * Two individual XML files selected → both must reach {@code loadObjectsFromFile}.
     */
    public void testTwoSelectedXmlFilesArePassedToUploadProcessing() {
        VirtualFile file1 = myFixture.addFileToProject(
                "objects/user1.xml",
                userXml("00000000-0000-0000-0000-000000000001", "user1")).getVirtualFile();
        VirtualFile file2 = myFixture.addFileToProject(
                "objects/user2.xml",
                userXml("00000000-0000-0000-0000-000000000002", "user2")).getVirtualFile();

        FileTrackingTask task = new FileTrackingTask(getProject(), dataContextWith(file1, file2)) {};

        task.runForTest();

        assertEquals("Both selected XML files should be loaded for upload processing",
                2, task.loadedFilePaths.size());
        assertTrue("user1.xml must be processed",
                task.loadedFilePaths.stream().anyMatch(p -> p.endsWith("user1.xml")));
        assertTrue("user2.xml must be processed",
                task.loadedFilePaths.stream().anyMatch(p -> p.endsWith("user2.xml")));
    }

    /**
     * A directory selected → XML files inside must reach {@code loadObjectsFromFile}.
     * This is the scenario that triggers MID-11199.
     */
    public void testXmlFilesInSelectedDirectoryArePassedToUploadProcessing() {
        myFixture.addFileToProject("dir/user1.xml",
                userXml("00000000-0000-0000-0000-000000000001", "user1"));
        myFixture.addFileToProject("dir/user2.xml",
                userXml("00000000-0000-0000-0000-000000000002", "user2"));
        VirtualFile dir = myFixture.findFileInTempDir("dir");

        assertNotNull("Test directory must exist in the temp project", dir);
        assertTrue("dir must be a directory", dir.isDirectory());

        FileTrackingTask task = new FileTrackingTask(getProject(), dataContextWith(dir)) {};

        task.runForTest();

        assertEquals("Both XML files inside the selected directory should be loaded for upload processing",
                2, task.loadedFilePaths.size());
        assertTrue("user1.xml must be processed",
                task.loadedFilePaths.stream().anyMatch(p -> p.endsWith("user1.xml")));
        assertTrue("user2.xml must be processed",
                task.loadedFilePaths.stream().anyMatch(p -> p.endsWith("user2.xml")));
    }
}
