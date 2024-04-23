package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.namespace.QName;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectDeltaTree<O extends ObjectType> extends Tree implements Disposable {

    public ObjectDeltaTree(@NotNull ObjectDeltaTreeModel<O> model) {
        super(model);

        setup();
    }

    @Override
    public void dispose() {
        UIUtil.dispose(this);
    }

    private void setup() {
        setRootVisible(false);

        setCellRenderer(new LabelBasedRenderer.Tree() {

            @Override
            public @NotNull Component getTreeCellRendererComponent(
                    @NotNull JTree tree, @Nullable Object value, boolean selected, boolean expanded, boolean leaf,
                    int row, boolean focused) {

                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);

                if (!(value instanceof DefaultMutableTreeNode node)) {
                    return c;
                }

                Color color = computeColor(node.getUserObject());
                if (color != null) {
                    c.setForeground(color);
                }

                return c;
            }
        });
    }

    private Color computeColor(Object userObject) {
        ModificationType type = getModificationType(userObject);

        return SynchronizationUtil.getColorForModificationType(type);
    }

    private ModificationType getModificationType(Object userObject) {
        ModificationType type = null;
        if (userObject instanceof DeltaItem di) {
            type = di.modificationType();
        } else if (userObject instanceof ItemDelta<?, ?> id) {
            type = getModificationType(id);
        } else if (userObject instanceof ObjectDelta<?> od) {
            type = getModificationType(od);
        }

        return type;
    }

    private ModificationType getModificationType(ObjectDelta<?> delta) {
        Set<ModificationType> set = delta.getModifications().stream()
                .map(this::getModificationType)
                .collect(Collectors.toSet());

        return SynchronizationUtil.getModificationType(set);
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

        return SynchronizationUtil.getModificationType(modifications);
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        if (node.getUserObject() instanceof ObjectDelta<?> od) {
            return "All";
        } else if (node.getUserObject() instanceof DeltaItem di) {
            ModificationType modificationType = getModificationType(node.getUserObject());
            String prefix = modificationType == null ?
                    "" :
                    switch (modificationType) {
                        case ADD -> "Add: ";
                        case DELETE -> "Remove: ";
                        case REPLACE -> "Replace existing with: ";
                    };

            PrismValue prismValue = di.value();
            if (prismValue instanceof PrismPropertyValue<?> property) {
                value = property.getRealValue();
            } else if (prismValue instanceof PrismReferenceValue ref) {
                QName relation = ref.getRelation();
                String relationStr = relation != null ? QNameUtil.prettyPrint(relation) : "";

                ObjectTypes type = ObjectTypes.getObjectTypeFromTypeQNameIfKnown(ref.getTargetType());
                String typeStr = type != null ? StudioLocalization.get().translateEnum(type) : "";

                value = ref.getOid() + " (" + StringUtils.joinWith(", ", typeStr, relationStr) + ")";
            } else if (prismValue instanceof PrismContainerValue<?> container) {
                return container.debugDump();
            }

            value = prefix + value;
        } else if (node.getUserObject() instanceof ItemDelta<?, ?> id) {
            value = id.getPath().toString();
        }

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}
