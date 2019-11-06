package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.trace.OpViewType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
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
            super(opView.getLabel());
            this.opView = opView;
        }

        public OpViewType getOpView() {
            return opView;
        }
    }
}
