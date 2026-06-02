package com.evolveum.midpoint.studio.ui.trace.singleOp;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.trace.Format;
import com.evolveum.midpoint.studio.impl.trace.FormattingContext;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.MiscUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Display selected of given OpNode - as a tree.
 */
public abstract class AbstractOpTreePanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(AbstractOpTreePanel.class);

    private DefaultTreeTable<Node<?>, OpNodeTableModel> variables;
    private OpNodeTableModel variablesModel;

    private FormatComboboxAction variablesDisplayAs;
    private CheckboxAction variablesWrapText;
    private JBTextArea variablesValue;

    private OpNode currentOpNode;

    public AbstractOpTreePanel(@NotNull Project project) {
        initLayout();
        updateModel(null);
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {

            @Override
            public void selectedTraceNodeChange(OpNode node) {
                nodeChange(node);
            }
        });
    }

    void updateModel(OpNode node) {
        this.currentOpNode = node;
    }

    private void nodeChange(OpNode node) {
        updateModel(node);
    }

    void applySelection(Node<?> obj) {
        if (obj == null) {
            variablesValue.setText(null);
        } else {
            Format format = variablesDisplayAs.getFormat();
            String text = format.format(obj.getObject(), new FormattingContext(currentOpNode));
            variablesValue.setText(text);
            variablesValue.setCaretPosition(0);
        }
    }

    void updateTreeModel(DefaultMutableTreeNode newRoot) {
        variablesModel.setRoot(newRoot);
        variables.invalidate();
    }

    private void variableDisplayAsChanged(Format format) {
        int[] indices = variables.getSelectedRows();
        if (indices == null || indices.length == 0) {
            applySelection(null);
            return;
        }

        TreePath path = variables.getTree().getPathForRow(indices[0]);
        if (path != null && path.getLastPathComponent() instanceof Node<?> node) {
            applySelection(node);
        } else {
            applySelection(null);
        }
    }

    private void variablesSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        Node<?> node = path != null && path.getLastPathComponent() instanceof Node<?> n ? n : null;
        applySelection(node);
    }

    private void initLayout() {
        JBSplitter splitter = new OnePixelSplitter(false);
        add(splitter, BorderLayout.CENTER);

        variablesModel = new OpNodeTableModel();
        variables = new DefaultTreeTable<>(variablesModel);
        variables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variables.getTree().setRootVisible(false);
        variables.getTree().addTreeSelectionListener(this::variablesSelectionChanged);

        // Apply expansion-sensitive renderer to the value column
        TableColumn valueColumn = variables.getColumnModel().getColumn(1);
        valueColumn.setCellRenderer(new ExpansionSensitiveTableCellRenderer());

        JComponent mainToolbar = initMainToolbar();
        splitter.setFirstComponent(MidPointUtils.createBorderLayoutPanel(
                mainToolbar,
                new JBScrollPane(variables, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                null));

        JPanel left = new BorderLayoutPanel();
        splitter.setSecondComponent(left);

        DefaultActionGroup group = new DefaultActionGroup();
        variablesDisplayAs = new FormatComboboxAction() {

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

            @Override
            public void setFormat(Format format) {
                super.setFormat(format);
                variableDisplayAsChanged(format);
            }
        };
        group.add(variablesDisplayAs);
        variablesWrapText = new SimpleCheckboxAction("Wrap text") {

            @Override
            public void onStateChange() {
                variablesValue.setLineWrap(isSelected());
                variablesValue.invalidate();
            }
        };
        group.add(variablesWrapText);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewVariablesToolbar", group, true);
        toolbar.setTargetComponent(this);
        left.add(toolbar.getComponent(), BorderLayout.NORTH);

        variablesValue = new JBTextArea();
        left.add(MidPointUtils.borderlessScrollPane(variablesValue), BorderLayout.CENTER);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> expandAll()));
        group.add(MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> collapseAll()));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceTreeToolbar", group, true);
        toolbar.setTargetComponent(this);
        return toolbar.getComponent();
    }

    private void expandAll() {
        for (int i = 0; i < variables.getRowCount(); i++) {
            variables.getTree().expandRow(i);
        }
    }

    private void collapseAll() {
        for (int i = variables.getRowCount() - 1; i >= 0; i--) {
            variables.getTree().collapseRow(i);
        }
    }

    void setViewingState(ViewingState viewingState) {
        SwingUtilities.invokeLater(() -> {
            collapseAll();
            Integer selectedIndex = viewingState.getSelectedIndex();
            if (selectedIndex != null) {
                variables.setRowSelectionInterval(selectedIndex, selectedIndex);
            }
            for (TreePath treePath : MiscUtil.emptyIfNull(viewingState.getExpandedPaths())) {
                variables.getTree().expandPath(treePath);
            }
        });
    }

    private class FormatComboboxAction extends ComboBoxAction {

        private Format format = Format.XML_SIMPLIFIED;

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();
            for (Format f : Format.values()) {
                group.add(new FormatAction(f) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        setFormat(this.getFormat());
                        variablesValue.setCaretPosition(0);
                    }
                });
            }
            return group;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setText(getFormat().getDisplayName());
        }

        public void setFormat(Format format) {
            this.format = format;
        }

        public Format getFormat() {
            return format != null ? format : Format.XML_SIMPLIFIED;
        }
    }

    private static abstract class FormatAction extends AnAction implements DumbAware {

        private final Format format;

        public FormatAction(Format format) {
            super(format.getDisplayName());
            this.format = format;
        }

        public Format getFormat() {
            return format;
        }
    }
}
