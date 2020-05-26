package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.service.MidPointLocalizationService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ComboObjectTypes extends ComboBoxAction implements DumbAware {

    private ObjectTypes selected = ObjectTypes.OBJECT;

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

        String text = selected.getTypeQName().getLocalPart();

        String value = ServiceManager.getService(MidPointLocalizationService.class).translate("ObjectType." + text, text);
        getTemplatePresentation().setText(value);
        e.getPresentation().setText(value);
    }

    public void setSelected(ObjectTypes selected) {
        if (selected == null) {
            selected = ObjectTypes.OBJECT;
        }

        this.selected = selected;
    }

    public ObjectTypes getSelected() {
        return selected;
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();

        List<ObjectTypes> types = new ArrayList<>();
        types.addAll(Arrays.asList(ObjectTypes.values()));
        Collections.sort(types, MidPointUtils.OBJECT_TYPES_COMPARATOR);

        for (ObjectTypes type : types) {
            group.add(new TypeAction(type, this));
        }

        return group;
    }

    private static class TypeAction extends AnAction implements DumbAware {

        private ObjectTypes type;

        private ComboObjectTypes combo;

        public TypeAction(ObjectTypes type, ComboObjectTypes combo) {
            super(type.getTypeQName().getLocalPart());

            this.type = type;
            this.combo = combo;
        }

        public ObjectTypes getType() {
            return type;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            combo.setSelected(type);
            combo.update(e);
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);

            String text = type.getTypeQName().getLocalPart();

            String value = ServiceManager.getService(MidPointLocalizationService.class).translate("ObjectType." + text, text);
            getTemplatePresentation().setText(value);
            e.getPresentation().setText(value);
        }
    }
}
