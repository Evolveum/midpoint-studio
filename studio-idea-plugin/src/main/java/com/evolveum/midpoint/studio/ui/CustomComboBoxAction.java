package com.evolveum.midpoint.studio.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CustomComboBoxAction<T> extends ComboBoxAction {

    private T selected;

    public CustomComboBoxAction() {
        selected = getDefaultItem();
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();

        List<T> items = getItems();
        if (items == null) {
            return group;
        }

        for (T item : items) {
            AnAction action = createItemAction(item);
            group.add(action);
        }

        return group;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        T item = getSelected();
        String label = createItemLabel(item);
        e.getPresentation().setText(label);
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    public T getDefaultItem() {
        return null;
    }

    public List<T> getItems() {
        return Collections.emptyList();
    }

    protected String createItemLabel(T item) {
        return item != null ? item.toString() : "";
    }

    protected AnAction createItemAction(T item) {
        return new ComboItemAction<>(this, item) {

            @Override
            protected String createLabel(T item) {
                return createItemLabel(item);
            }
        };
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    public static class ComboItemAction<T> extends AnAction {

        private CustomComboBoxAction combo;

        private T item;

        public ComboItemAction(CustomComboBoxAction combo, T item) {
            this.combo = combo;
            this.item = item;
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);

            String label = createLabel(item);
            e.getPresentation().setText(label);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            combo.setSelected(item);
            combo.update(e);
        }

        protected String createLabel(T item) {
            return item != null ? item.toString() : "";
        }
    }
}
