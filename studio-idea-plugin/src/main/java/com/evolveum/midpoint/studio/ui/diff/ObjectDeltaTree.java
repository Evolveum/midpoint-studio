package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectDeltaTree<O extends ObjectType> extends Tree {

    private static final ColorKey ADDED = ColorKey.createColorKey("FILESTATUS_ADDED");
    private static final ColorKey DELETED = ColorKey.createColorKey("FILESTATUS_DELETED");
    private static final ColorKey MODIFIED = ColorKey.createColorKey("FILESTATUS_MODIFIED");

    public ObjectDeltaTree(@NotNull ObjectDeltaTreeModel<O> model) {
        super(model);

        setup();
    }

    private void setup() {
        setRootVisible(false);

        setCellRenderer(new LabelBasedRenderer.Tree() {

            @Override
            public @NotNull Component getTreeCellRendererComponent(@NotNull JTree tree, @Nullable Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Color color = computeColor(node.getUserObject());
                if (color != null) {
                    c.setForeground(color);
                }

                return c;
            }
        });
    }

    private Color computeColor(Object userObject) {
        ModificationType type = null;
        if (userObject instanceof DeltaItem di) {
            type = di.modificationType();
        } else if (userObject instanceof ItemDelta<?, ?> id) {
            type = getModificationType(id);
        } else if (userObject instanceof ObjectDelta<?> od) {
            type = getModificationType(od);
        }

        return getColorForModificationType(type);
    }

    private ModificationType getModificationType(ObjectDelta<?> delta) {
        if (delta.getModifications() == null) {
            return null;
        }

        Set<ModificationType> set = delta.getModifications().stream()
                .map(this::getModificationType)
                .collect(Collectors.toSet());

        return getModificationType(set);
    }

    private Color getColorForModificationType(ModificationType modificationType) {
        if (modificationType == null) {
            return null;
        }

        EditorColorsScheme scheme = EditorColorsManager.getInstance().getSchemeForCurrentUITheme();
        return switch (modificationType) {
            case ADD -> scheme.getColor(ADDED);
            case DELETE -> scheme.getColor(DELETED);
            case REPLACE -> scheme.getColor(MODIFIED);
        };
    }

    private ModificationType getModificationType(ItemDelta<?, ?> delta) {
        Set<ModificationType> modifications = new HashSet<>();
        if (delta.getValuesToAdd() != null && !delta.getValuesToAdd().isEmpty()) {
            modifications.add(ModificationType.ADD);
        }
        if (delta.getValuesToDelete() != null && !delta.getValuesToDelete().isEmpty()) {
            modifications.add(ModificationType.DELETE);
        }
        if (delta.getValuesToReplace() != null && !delta.getValuesToReplace().isEmpty()) {
            modifications.add(ModificationType.REPLACE);
        }

        return getModificationType(modifications);
    }

    private ModificationType getModificationType(Set<ModificationType> modificationTypes) {
        if (modificationTypes.size() > 1) {
            return ModificationType.REPLACE;
        } else if (modificationTypes.size() == 1) {
            return modificationTypes.iterator().next();
        }

        return null;
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ObjectDelta<?> od) {
            return "All";
        } else if (node.getUserObject() instanceof DeltaItem di) {
            value = di.value();
        } else if (node.getUserObject() instanceof ItemDelta<?, ?> id) {
            value = id.getPath().toString();
        }

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}
