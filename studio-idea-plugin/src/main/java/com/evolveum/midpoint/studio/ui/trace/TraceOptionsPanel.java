package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpType;
import com.evolveum.midpoint.studio.impl.trace.OpViewType;
import com.evolveum.midpoint.studio.impl.trace.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.HeaderDecorator;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceOptionsPanel extends BorderLayoutPanel {

    private ViewTypeComboboxAction viewType;
    private CheckboxAction todo;

    public TraceOptionsPanel() {
        initLayout();
    }

    private void initLayout() {
        DefaultActionGroup group = new DefaultActionGroup();
        viewType = new ViewTypeComboboxAction();
        group.add(viewType);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("TraceOptionsToolbar", group, true);
        add(toolbar.getComponent(), BorderLayout.NORTH);

        JPanel root = new BorderLayoutPanel();

        JPanel events = new JPanel();
        events.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        events.setLayout(new BoxLayout(events, BoxLayout.Y_AXIS));

        for (OpType type : OpType.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(type.getLabel());
            events.add(check);
        }

        root.add(new HeaderDecorator("Events to show", events), BorderLayout.NORTH);

        JPanel categories = new JPanel();
        categories.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        categories.setLayout(new BoxLayout(categories, BoxLayout.Y_AXIS));

        for (PerformanceCategory type : PerformanceCategory.values()) {
            JCheckBox check = new JCheckBox();
            check.setText(type.getLabel());
            categories.add(check);
        }

        root.add(new HeaderDecorator("Categories to show", categories), BorderLayout.SOUTH);

        add(new JBScrollPane(root));
    }

    private static class ViewTypeComboboxAction extends ComboBoxAction {

        private OpViewType opView = OpViewType.ALL;

        @NotNull
        @Override
        protected DefaultActionGroup createPopupActionGroup(JComponent button) {
            DefaultActionGroup group = new DefaultActionGroup();
            group.add(new ViewTypeAction(null) {

                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    ViewTypeComboboxAction.this.setOpView(this.getOpView());
                }
            });

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
