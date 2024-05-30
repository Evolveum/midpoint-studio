package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.impl.binding.AbstractPlainStructured;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.ui.synchronization.SynchronizationUtil;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ObjectDeltaTree<O extends ObjectType> extends Tree implements Disposable {

    private static final Logger LOG = Logger.getInstance(ObjectDeltaTree.class);

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
        } else if (userObject instanceof ObjectDeltaTreeNode itemDeltaNode) {
            type = getModificationType(itemDeltaNode.delta());
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
                Object realValue = property.getRealValue();
                if (realValue instanceof ExpressionType expression) {
                    return "Expression: " + expression.getExpressionEvaluator().stream()
                            .map(e -> e.getValue().getClass().getSimpleName())
                            .collect(Collectors.joining(", "));
                } else if (realValue instanceof AbstractPlainStructured) {
                    try {
                        String xml = PrismContext.get().xmlSerializer().serialize(property);
                        value = StringUtils.abbreviate(xml, 200);
                    } catch (Exception ex) {
                        LOG.error("Couldn't serialize property value", ex);
                        value = property.debugDump();
                    }
                } else {
                    value = PrettyPrinter.prettyPrint(property.getRealValue());
                }
            } else if (prismValue instanceof PrismReferenceValue ref) {
                QName relation = ref.getRelation();
                String relationStr = relation != null ? QNameUtil.prettyPrint(relation) : "";

                ObjectTypes type = ObjectTypes.getObjectTypeFromTypeQNameIfKnown(ref.getTargetType());
                String typeStr = type != null ? StudioLocalization.get().translateEnum(type) : "";

                String oid = ref.getOid();
                if (oid == null) {
                    oid = ref.getFilter() != null ? "Filter defined" : "Undefined oid";
                }

                String details = List.of(typeStr, relationStr).stream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(", "));

                value = oid + " (" + details + ")";
            } else if (prismValue instanceof PrismContainerValue<?> container) {
                try {
                    String xml = PrismContext.get().xmlSerializer().serialize(container);
                    value = StringUtils.abbreviate(xml, 200);
                } catch (Exception ex) {
                    LOG.error("Couldn't serialize container value", ex);
                    value = container.debugDump();
                }
            }

            value = prefix + value;
        } else if (node.getUserObject() instanceof ObjectDeltaTreeNode itemDeltaNode) {
            value = createReadablePath(itemDeltaNode);
        }

        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }

    private String createReadablePath(ObjectDeltaTreeNode itemDeltaNode) {
        Item<?, ?> targetItem = itemDeltaNode.targetItem();
        PrismObject<?> object = getObject(targetItem);

        ItemPath path = itemDeltaNode.delta().getPath();

        List<String> segments = new ArrayList<>();

        ItemPath partial = ItemPath.EMPTY_PATH;
        for (Object segment : path.getSegments()) {
            if (segment instanceof Long id) {
                String suffix = createNaturalKeySuffix(object, partial, id);
                if (suffix != null) {
                    segments.add(id + suffix);
                }
                segments.add(id.toString());
            } else {
                segments.add(ItemPathSegment.toString(segment));
            }

            partial = partial.append(segment);
        }

        return StringUtils.join(segments, "/");
    }

    private String createNaturalKeySuffix(PrismObject<?> object, ItemPath path, Long id) {
        String naturalKey = getNaturalKey(object, path, id);

        return naturalKey == null ? null : " (" + naturalKey + ")";
    }

    private String getNaturalKey(PrismObject<?> object, ItemPath path, Long id) {
        if (object == null) {
            return null;
        }
        PrismContainer<?> item = object.findContainer(path);
        if (item == null) {
            return null;
        }

        ItemDefinition<?> itemDefinition = item.getDefinition();
        NaturalKeyDefinition def = itemDefinition.getNaturalKeyInstance();
        if (def == null) {
            ItemMerger merger = itemDefinition.getMergerInstance(MergeStrategy.FULL, null);
            if (merger != null) {
                def = merger.getNaturalKey();
            }
        }

        if (def == null) {
            return null;
        }

        PrismContainerValue<?> value = item.findValue(id);
        Collection<Item<?, ?>> items = def.getConstituents(value);
        if (items == null) {
            return null;
        }

        String key = items.stream()
                .map(i -> i.getElementName().getLocalPart() + ": " + StringUtils.join(i.getRealValues(), ", "))
                .collect(Collectors.joining("; "));

        return StringUtils.abbreviate(key, 200);
    }

    private PrismObject<?> getObject(Item<?, ?> item) {
        if (item == null) {
            return null;
        }

        if (item instanceof PrismObject<?> o) {
            return o;
        }

        PrismContainerValue parentValue = item.getParent();
        if (parentValue != null) {
            return getObject((Item) parentValue.getParent());
        }

        return null;
    }
}
