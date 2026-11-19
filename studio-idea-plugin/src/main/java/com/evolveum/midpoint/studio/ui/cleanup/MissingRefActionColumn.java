package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.ui.treetable.Style;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;

public class MissingRefActionColumn extends DefaultColumnInfo<Object, MissingRefAction> {

    private final MissingRefObjectsTableModel model;

    public MissingRefActionColumn(@NotNull MissingRefObjectsTableModel model) {
        super("Action");

        this.model = model;

        setPreferredWidth(150);
        setMinWidth(150);
        setMaxWidth(150);

        style(userObject -> styleFor(actionOf(userObject)));
    }

    private static Color colorFor(MissingRefAction action) {
        if (action == MissingRefAction.IGNORE) {
            return JBColor.RED.darker();
        }

        if (action == MissingRefAction.DOWNLOAD) {
            return JBColor.GREEN.darker();
        }

        return null;
    }

    private static Style styleFor(MissingRefAction action) {
        Color color = colorFor(action);
        return color != null ? Style.foreground(color) : null;
    }

    private static MissingRefAction actionOf(Object userObject) {
        if (userObject instanceof MissingRefNode refNode) {
            return refNode.getAction() != null ? refNode.getAction() : MissingRefAction.UNDEFINED;
        }

        if (userObject instanceof MissingRef ref) {
            return ref.getAction() != null ? ref.getAction() : MissingRefAction.UNDEFINED;
        }

        return MissingRefAction.UNDEFINED;
    }

    @Override
    public @Nullable TableCellRenderer getRenderer(MutableTreeNode node) {
        return new Editor();
    }

    @Override
    public @Nullable TableCellEditor getEditor(MutableTreeNode node) {
        return new Editor()
                .withClickCount(1);
    }

    @Override
    public Class<?> getColumnClass() {
        return MissingRefAction.class;
    }

    @Override
    public boolean isCellEditable(MutableTreeNode node) {
        return true;
    }

    @Override
    public @Nullable MissingRefAction valueOf(MutableTreeNode node) {
        return actionOf(getUserObject(node));
    }

    @Override
    public void setValue(MutableTreeNode node, MissingRefAction value) {
        Object object = getUserObject(node);

        if (object instanceof MissingRefNode refNode) {
            refNode.setAction(value);

            for (int i = 0; i < node.getChildCount(); i++) {
                MutableTreeNode child = (MutableTreeNode) node.getChildAt(i);
                setValue(child, value);
            }
        } else if (object instanceof MissingRef ref) {
            ref.setAction(value);
        }

        model.nodeChanged(node);
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

            // keeps the editor component colored while the combo box is open,
            // rendering goes through the style provider in DefaultTreeTable.prepareRenderer
            Color color = colorFor(value);
            if (color != null) {
                setForeground(color);
            }
        }
    }
}
