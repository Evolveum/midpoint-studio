package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.*;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceOptionsPanel extends BorderLayoutPanel {

    private static final Logger LOG = Logger.getInstance(TraceOptionsPanel.class);

    private TraceManager traceManager;

    private Map<OpType, JCheckBox> eventChecks = new HashMap<>();

    private Map<PerformanceCategory, JCheckBox> categoriesChecks = new HashMap<>();

    private ViewTypeComboboxAction viewType;

    private AnAction apply;

    private JCheckBox alsoParents;

    private JCheckBox perfColumns;

    private JCheckBox readWriteColumns;

    public TraceOptionsPanel(Project project) {
        this.traceManager = TraceManager.getInstance(project);

        initLayout();
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();
        viewType = new ViewTypeComboboxAction(traceManager.getOpViewType()) {

            @Override
            public void setOpView(OpViewType opView) {
                super.setOpView(opView);

                viewTypeChanged(opView);
            }
        };
        group.add(viewType);

        apply = new AnAction("Apply", "Apply options changes", AllIcons.Actions.Commit) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                applyPerformed(e);
            }
        };
        group.add(apply);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceOptionsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        JPanel root = new BorderLayoutPanel();

        JPanel events = createBoxLayoutPanel();
        events.setBorder(JBUI.Borders.empty(5));

        for (OpType type : OpType.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(type.getLabel());
            events.add(check);

            eventChecks.put(type, check);
        }

        root.add(new HeaderDecorator("Events to show", events), BorderLayout.NORTH);

        JPanel categories = createBoxLayoutPanel();
        categories.setBorder(JBUI.Borders.empty(5));

        for (PerformanceCategory type : PerformanceCategory.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(type.getLabel());
            categories.add(check);

            categoriesChecks.put(type, check);
        }

        root.add(new HeaderDecorator("Categories to show", categories), BorderLayout.CENTER);

        JPanel other = createBoxLayoutPanel();
        other.setBorder(JBUI.Borders.empty(5));

        alsoParents = new JCheckBox();
        alsoParents.setText("Show also parents");
        other.add(alsoParents);

        perfColumns = new JCheckBox();
        perfColumns.setText("Show performance columns");
        other.add(perfColumns);

        readWriteColumns = new JCheckBox();
        readWriteColumns.setText("Show read/write ops columns");
        other.add(readWriteColumns);

        root.add(new HeaderDecorator("Other to show", other), BorderLayout.SOUTH);

        add(new JBScrollPane(root));

        viewTypeChanged(viewType.getOpView());
    }

    private JPanel createBoxLayoutPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(JBUI.Borders.emptyLeft(5));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        return panel;
    }

    private void viewTypeChanged(OpViewType opViewType) {
        LOG.debug("View type changed to: ", opViewType);

        traceManager.setOpViewType(opViewType);

        eventChecks.forEach((type, button) ->
                button.setSelected(opViewType.getTypes() == null || opViewType.getTypes().contains(type)));
        categoriesChecks.forEach((category, button) ->
                button.setSelected(opViewType.getCategories() == null || opViewType.getCategories().contains(category)));
        alsoParents.setSelected(opViewType.isShowAlsoParents());
        perfColumns.setSelected(opViewType.isShowPerformanceColumns());
        readWriteColumns.setSelected(opViewType.isShowReadWriteColumns());

        invalidate();
    }

    private void applyPerformed(AnActionEvent evt) {
        Options options = createOptions();

        traceManager.setOptions(options);
    }

    private Options createOptions() {
        Options rv = new Options();
        for (Map.Entry<OpType, JCheckBox> e : eventChecks.entrySet()) {
            if (e.getValue().isSelected()) {
                rv.getTypesToShow().add(e.getKey());
            }
        }
        for (Map.Entry<PerformanceCategory, JCheckBox> e : categoriesChecks.entrySet()) {
            if (e.getValue().isSelected()) {
                rv.getCategoriesToShow().add(e.getKey());
            }
        }
        rv.setShowAlsoParents(alsoParents.isSelected());
        rv.setShowPerformanceColumns(perfColumns.isSelected());
        rv.setShowReadWriteColumns(readWriteColumns.isSelected());

        return rv;
    }

    private static class ViewTypeComboboxAction extends ComboBoxAction {

        private OpViewType opView;

        public ViewTypeComboboxAction(OpViewType opView) {
            this.opView = opView;
        }

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();
            for (OpViewType o : OpViewType.values()) {
                group.add(new ViewTypeAction(o) {

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        ViewTypeComboboxAction.this.setOpView(this.getOpView());
                    }
                });
            }

            return group;
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);

            OpViewType o = getOpView();
            if (o == null) {
                return;
            }

            String text = o.getLabel();
            getTemplatePresentation().setText(text);
            e.getPresentation().setText(text);
        }

        public void setOpView(OpViewType opView) {
            this.opView = opView;
        }

        public OpViewType getOpView() {
            return opView;
        }
    }

    private static abstract class ViewTypeAction extends AnAction implements DumbAware {

        private OpViewType opView;

        public ViewTypeAction(OpViewType opView) {
            super(opView != null ? opView.getLabel() : "");
            this.opView = opView;
        }

        public OpViewType getOpView() {
            return opView;
        }
    }
}
