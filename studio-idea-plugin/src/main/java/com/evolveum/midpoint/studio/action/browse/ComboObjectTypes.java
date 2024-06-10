package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ComboObjectTypes extends ComboBoxAction implements DumbAware {

    private List<SelectionListener<ObjectTypes>> selectionListeners = new java.util.ArrayList<>();

    private ObjectTypes selected = ObjectTypes.OBJECT;

    public void addSelectionListener(SelectionListener<ObjectTypes> listener) {
        selectionListeners.add(listener);
    }

    public void removeSelectionListener(SelectionListener<ObjectTypes> listener) {
        selectionListeners.remove(listener);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        String value = StudioLocalization.get().translateEnum(selected);
        getTemplatePresentation().setText(value);
        e.getPresentation().setText(value);
    }

    public void setSelected(ObjectTypes selected) {
        if (selected == null) {
            selected = ObjectTypes.OBJECT;
        }

        this.selected = selected;

        for (SelectionListener<ObjectTypes> listener : selectionListeners) {
            listener.onSelectionChanged(selected);
        }
    }

    public ObjectTypes getSelected() {
        return selected;
    }

    @NotNull
    @Deprecated
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();

        List<ObjectTypes> types = Arrays.asList(ObjectTypes.values());
        types.sort(MidPointUtils.OBJECT_TYPES_COMPARATOR);

        for (ObjectTypes type : types) {
            group.add(new TypeAction(type, this));
        }

        return group;
    }

    private static class TypeAction extends AnAction implements DumbAware {

        private final ObjectTypes type;

        private final ComboObjectTypes combo;

        public TypeAction(ObjectTypes type, ComboObjectTypes combo) {
            super(type.getTypeQName().getLocalPart());

            this.type = type;
            this.combo = combo;
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            combo.setSelected(type);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);

            String value = StudioLocalization.get().translateEnum(type);
            getTemplatePresentation().setText(value);
            e.getPresentation().setText(value);
        }
    }
}
