package com.evolveum.midpoint.studio.ui.trace.mainTree;

import com.evolveum.midpoint.studio.ui.trace.TraceViewEditor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.components.panels.Wrapper;

/**
 * Guards the expand all / collapse all behaviour of the trace tree: expand all makes every
 * operation node a visible row (the implementation expands in bulk, so the number of rows is
 * the only observable), collapse all keeps the root operation open one level, as it always did.
 */
public class OpTreePanelTest extends BasePlatformTestCase {

    private static final String NESTED_TRACE = """
            <tracingOutput xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <result>
                    <operation>root</operation>
                    <status>success</status>
                    <microseconds>1000</microseconds>
                    <partialResults>
                        <operation>a</operation>
                        <status>success</status>
                        <microseconds>100</microseconds>
                        <partialResults>
                            <operation>a1</operation>
                            <status>success</status>
                            <microseconds>10</microseconds>
                        </partialResults>
                    </partialResults>
                    <partialResults>
                        <operation>b</operation>
                        <status>success</status>
                        <microseconds>100</microseconds>
                    </partialResults>
                </result>
            </tracingOutput>
            """;

    public void testExpandAllShowsEveryOperationAsRow() {
        VirtualFile file = myFixture.addFileToProject("traces/nested.mptrace", NESTED_TRACE).getVirtualFile();

        TraceViewEditor editor = new TraceViewEditor(getProject(), file);
        try {
            OpTreePanel panel = (OpTreePanel) ((Wrapper) editor.getComponent()).getTargetComponent();

            panel.expandAll();
            assertEquals("root, a, a1, b", 4, panel.getRowCount());

            panel.collapseAll();
            assertEquals("root with its direct children a and b", 3, panel.getRowCount());

            panel.expandAll();
            assertEquals("expanding again after collapse", 4, panel.getRowCount());
        } finally {
            Disposer.dispose(editor);
        }
    }
}
