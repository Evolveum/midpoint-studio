package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.Format;
import com.evolveum.midpoint.studio.ui.SimpleCheckboxAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceVariablesPanel extends BorderLayoutPanel {

    private JXTreeTable variables;
    private FormatComboboxAction variablesDisplayAs;
    private CheckboxAction variablesWrapText;
    private JBTextArea variablesValue;

    public TraceVariablesPanel() {
        initLayout();
    }

    private void initLayout() {
        JBSplitter variables = new OnePixelSplitter(false);
        add(variables, BorderLayout.CENTER);

        List<TreeTableColumnDefinition> columns = new ArrayList<>();

        columns.add(new TreeTableColumnDefinition<String, String>("Item", 500, o -> null));
        columns.add(new TreeTableColumnDefinition<String, String>("Variable", 500, o -> null));

        this.variables = MidPointUtils.createTable(new DefaultTreeTableModel(new DefaultMutableTreeTableNode("")), null);
        variables.setFirstComponent(new JBScrollPane(this.variables));

        JPanel left = new BorderLayoutPanel();
        variables.setSecondComponent(left);

        DefaultActionGroup group = new DefaultActionGroup();
        variablesDisplayAs = new FormatComboboxAction();
        group.add(variablesDisplayAs);
        variablesWrapText = new SimpleCheckboxAction("Wrap text");
        group.add(variablesWrapText);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceViewVariablesToolbar", group, true);
        left.add(toolbar.getComponent(), BorderLayout.NORTH);

        variablesValue = new JBTextArea();
        left.add(new JBScrollPane(variablesValue), BorderLayout.CENTER);
    }

    private static class FormatComboboxAction extends ComboBoxAction {

        private Format format = Format.AUTO;

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();

            for (Format f : Format.values()) {
                group.add(new FormatAction(f) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        FormatComboboxAction.this.setFormat(this.getFormat());
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
        }

        public void setFormat(Format format) {
            this.format = format;
        }

        public Format getFormat() {
            return format != null ? format : Format.AUTO;
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
}
