package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.ui.trace.mainTree.OpTreePanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.panels.Wrapper;

import javax.swing.*;

/**
 * Covers the result of loading a trace file into the editor: a valid trace ends with the op
 * node tree in the editor component, an unreadable file ends with an error message in the
 * editor component instead of an empty tree.
 */
public class TraceViewEditorTest extends BasePlatformTestCase {

    private static final String OPERATION = "com.example.TestOperation.run";

    private static final String MINIMAL_TRACE = """
            <tracingOutput xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <result>
                    <operation>%s</operation>
                    <status>success</status>
                    <microseconds>1000</microseconds>
                </result>
            </tracingOutput>
            """.formatted(OPERATION);

    public void testValidTraceShowsOpNodeTree() {
        VirtualFile file = myFixture.addFileToProject("traces/test.mptrace", MINIMAL_TRACE).getVirtualFile();

        TraceViewEditor editor = new TraceViewEditor(getProject(), file);
        try {
            OpNode root = editor.getRootOpNode();
            assertNotNull("root op node", root);
            assertEquals(OPERATION, root.getResult().getOperation());

            assertTrue("editor shows the op node tree", content(editor) instanceof OpTreePanel);
        } finally {
            Disposer.dispose(editor);
        }
    }

    public void testUnreadableTraceShowsErrorInsteadOfTree() {
        VirtualFile file = myFixture.addFileToProject("traces/broken.mptrace", "this is not a trace").getVirtualFile();

        TraceViewEditor editor = new TraceViewEditor(getProject(), file);
        try {
            assertNull("no root op node", editor.getRootOpNode());

            JComponent content = content(editor);
            assertTrue("editor shows an error panel, got " + content.getClass().getName(),
                    content instanceof JBPanelWithEmptyText);
            String text = ((JBPanelWithEmptyText) content).getEmptyText().getText();
            assertTrue(text, text.contains("Couldn't load"));
        } finally {
            Disposer.dispose(editor);
        }
    }

    private static JComponent content(TraceViewEditor editor) {
        return ((Wrapper) editor.getComponent()).getTargetComponent();
    }
}
