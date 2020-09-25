package com.evolveum.midpoint.studio.ui.trace.singleOp;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifierAdapter;
import com.evolveum.midpoint.studio.impl.trace.Format;
import com.evolveum.midpoint.studio.impl.trace.FormattingContext;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.studio.ui.TreeTableColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.ViewingState;
import com.evolveum.midpoint.studio.ui.trace.entry.Node;
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
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Display selected of given OpNode - as a tree.
 *
 * Created by Viliam Repan (lazyman).
 */
public abstract class AbstractOpTreePanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(AbstractOpTreePanel.class);

    private JXTreeTable variables;

    private FormatComboboxAction variablesDisplayAs;

    private CheckboxAction variablesWrapText;

    private JBTextArea variablesValue;

    private OpNode currentOpNode;

    public AbstractOpTreePanel(@NotNull Project project) {
        initLayout();
        updateModel(null);
        MessageBus bus = project.getMessageBus();
        bus.connect().subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifierAdapter() {

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

    void updateTreeModel(DefaultMutableTreeTableNode newRoot) {
        DefaultTreeTableModel model = (DefaultTreeTableModel) variables.getTreeTableModel();
        model.setRoot(newRoot);

        this.variables.invalidate();
    }

    private void variableDisplayAsChanged(Format format) {
        ListSelectionModel ext = variables.getSelectionModel();
        int[] indices = ext.getSelectedIndices();
        if (indices == null || indices.length == 0) {
            applySelection(null);
        }

        TreeTableNode node = (TreeTableNode) variables.getPathForRow(indices[0]).getLastPathComponent();
        if (node instanceof Node) {
            applySelection((Node) node);
        } else {
            applySelection(null);
        }
    }

    private void variablesSelectionChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        TreeTableNode node = path != null ? (TreeTableNode) path.getLastPathComponent() : null;

        Node obj = null;
        if (node instanceof Node) {
            obj = (Node) node;
        }

        applySelection(obj);
    }

    private void initLayout() {
        JBSplitter splitter = new OnePixelSplitter(false);
        add(splitter, BorderLayout.CENTER);

        List<TreeTableColumnDefinition<String, ?>> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<>("Item", 150, o -> null));
        columns.add(new TreeTableColumnDefinition<>("Variable", 400, o -> null, new ExpansionSensitiveTableCellRenderer()));

        this.variables = MidPointUtils.createTable2(
                new DefaultTreeTableModel(new DefaultMutableTreeTableNode(), Arrays.asList("Item", "Variable")),
                MidPointUtils.createTableColumnModel(columns), false);
        this.variables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.variables.addTreeSelectionListener(this::variablesSelectionChanged);

        this.variables.addHighlighter(new AbstractHighlighter() {
            @Override
            protected Component doHighlight(Component component, ComponentAdapter adapter) {
                int row = adapter.convertRowIndexToModel(adapter.row);
                TreePath pathForRow = variables.getPathForRow(row);
                Node node = (Node) pathForRow.getLastPathComponent();
                if (adapter.isSelected()) {
                    component.setBackground(variables.getSelectionBackground());
                } else if (node.getBackgroundColor() == null) {
                    component.setBackground(variables.getBackground());
                } else {
                    component.setBackground(node.getBackgroundColor());
                }
                return component;
            }
        });

        TableColumn column = this.variables.getColumnModel().getColumn(1);
        column.setCellRenderer(new ExpansionSensitiveTableCellRenderer());

        JComponent mainToolbar = initMainToolbar();

        splitter.setFirstComponent(MidPointUtils.createBorderLayoutPanel(mainToolbar, new JBScrollPane(this.variables), null));

        JPanel left = new BorderLayoutPanel();
        splitter.setSecondComponent(left);

        DefaultActionGroup group = new DefaultActionGroup();
        variablesDisplayAs = new FormatComboboxAction() {

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
        left.add(toolbar.getComponent(), BorderLayout.NORTH);

        variablesValue = new JBTextArea();
        left.add(new JBScrollPane(variablesValue), BorderLayout.CENTER);
    }

    private JComponent initMainToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        AnAction expandAll = MidPointUtils.createAnAction("Expand All", AllIcons.Actions.Expandall, e -> variables.expandAll());
        group.add(expandAll);

        AnAction collapseAll = MidPointUtils.createAnAction("Collapse All", AllIcons.Actions.Collapseall, e -> variables.collapseAll());
        group.add(collapseAll);

        ActionToolbar resultsActionsToolbar = ActionManager.getInstance().createActionToolbar("TraceTreeToolbar", group, true);
        return resultsActionsToolbar.getComponent();
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
                        AbstractOpTreePanel.FormatComboboxAction.this.setFormat(this.getFormat());
                    }
                });
            }

            return group;
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);

            String text = getFormat().getDisplayName();
            getTemplatePresentation().setText(text);
            e.getPresentation().setText(text);
            variablesValue.setCaretPosition(0);
        }

        public void setFormat(Format format) {
            this.format = format;
        }

        public Format getFormat() {
            return format != null ? format : Format.XML_SIMPLIFIED;
        }
    }

    private static abstract class FormatAction extends AnAction implements DumbAware {

        private Format format;

        public FormatAction(Format format) {
            super(format.getDisplayName());
            this.format = format;
        }

        public Format getFormat() {
            return format;
        }
    }

    void setViewingState(ViewingState viewingState) {

        SwingUtilities.invokeLater(() -> {
            variables.collapseAll();
            Integer selectedIndex = viewingState.getSelectedIndex();
            if (selectedIndex != null) {
                variables.setRowSelectionInterval(selectedIndex, selectedIndex);
            }
            for (TreePath treePath : MiscUtil.emptyIfNull(viewingState.getExpandedPaths())) {
                variables.expandPath(treePath);
            }
        });
    }

}
