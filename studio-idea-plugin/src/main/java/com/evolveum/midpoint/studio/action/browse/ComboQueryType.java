package com.evolveum.midpoint.studio.action.browse;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ComboQueryType extends ComboBoxAction implements DumbAware {

    public enum Type {

        NAME_OR_OID("Name or Oid"),

        NAME("Name"),

        OID("Oid"),

        AXIOM("Axiom"),

        QUERY_XML("Query XML");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private Type selected = Type.NAME_OR_OID;

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        String text = selected.getLabel();
        getTemplatePresentation().setText(text);
        e.getPresentation().setText(text);
    }

    public void setSelected(Type selected) {
        if (selected == null) {
            selected = Type.NAME_OR_OID;
        }

        this.selected = selected;
    }

    public Type getSelected() {
        return selected;
    }

    @NotNull
    @Override
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();

        for (Type type : Type.values()) {
            group.add(new TypeAction(type, this));
        }

        return group;
    }

    private static class TypeAction extends AnAction implements DumbAware {

        private Type type;

        private ComboQueryType combo;

        public TypeAction(Type type, ComboQueryType combo) {
            super(type.getLabel());

            this.type = type;
            this.combo = combo;
        }

        public Type getType() {
            return type;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            combo.setSelected(type);
            combo.update(e);
        }
    }
}
