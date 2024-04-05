package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.JBColor;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MissingRefActionColumn extends DefaultColumnInfo<Object, MissingRefAction> {

    public MissingRefActionColumn() {
        super("Action");

        setPreferredWidth(150);
        setMinWidth(150);
        setMaxWidth(150);
    }

    @Override
    public @Nullable TableCellRenderer getRenderer(DefaultMutableTreeTableNode node) {
        return new Editor();
    }

    @Override
    public @Nullable TableCellEditor getEditor(DefaultMutableTreeTableNode node) {
        return new Editor()
                .withClickCount(1);
    }

    @Override
    public Class<?> getColumnClass() {
        return MissingRefAction.class;
    }

    @Override
    public boolean isCellEditable(DefaultMutableTreeTableNode node) {
        return true;
    }

    @Override
    public @Nullable MissingRefAction valueOf(DefaultMutableTreeTableNode node) {
        Object object = node.getUserObject();

        if (object instanceof MissingRefNode refNode) {
            return refNode.getAction() != null ? refNode.getAction() : MissingRefAction.UNDEFINED;
        }

        if (object instanceof MissingRef ref) {
            return ref.getAction() != null ? ref.getAction() : MissingRefAction.UNDEFINED;
        }

        return MissingRefAction.UNDEFINED;
    }

    @Override
    public void setValue(DefaultMutableTreeTableNode node, MissingRefAction value) {
        Object object = node.getUserObject();

        if (object instanceof MissingRefNode refNode) {
            refNode.setAction(value);

            for (int i = 0; i < node.getChildCount(); i++) {
                MutableTreeTableNode child = (MutableTreeTableNode) node.getChildAt(i);
                setValue((DefaultMutableTreeTableNode) child, value);
            }
        } else if (object instanceof MissingRef ref) {
            ref.setAction(value);
        }
    }

    private static class Editor extends ComboBoxTableRenderer<MissingRefAction> {

        public Editor() {
            super(MissingRefAction.values());
        }

        @Override
        protected @NlsContexts.Label String getTextFor(@NotNull MissingRefAction value) {
            return StudioLocalization.get().translateEnum(value);
        }

        @Override
        protected void customizeComponent(MissingRefAction value, JTable table, boolean isSelected) {
            super.customizeComponent(value, table, isSelected);

            Color def = getForeground();

            if (value == MissingRefAction.IGNORE) {
                setForeground(JBColor.RED.darker());
            } else if (value == MissingRefAction.DOWNLOAD) {
                setForeground(JBColor.GREEN.darker());
            } else {
                setForeground(def);
            }
        }
    }
}
