package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.impl.binding.AbstractPlainStructured;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDeltaValueTreeNode extends ObjectDeltaTreeNode<PrismValue> {

    private static final Logger LOG = Logger.getInstance(ItemDeltaValueTreeNode.class);

    private ItemDelta<?, ?> itemDelta;

    private ModificationType modificationType;

    public ItemDeltaValueTreeNode(ItemDelta<?, ?> itemDelta, ModificationType modificationType, PrismValue value) {
        super(value);

        this.itemDelta = itemDelta;
        this.modificationType = modificationType;
    }

    public ItemDelta<?, ?> getItemDelta() {
        return itemDelta;
    }

    @Override
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    ApplicableDelta<?> getApplicableDelta() {
        return new ApplicableItemValueDelta<>(itemDelta, modificationType, getValue());
    }

    @Override
    String getText() {
        String value = "";

        ModificationType modificationType = getModificationType();
        String prefix = modificationType == null ?
                "" :
                switch (modificationType) {
                    case ADD -> "Add: ";
                    case DELETE -> "Remove: ";
                    case REPLACE -> "Replace existing with: ";
                };

        PrismValue prismValue = getValue();
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
            String relationStr = relation != null ? PrettyPrinter.prettyPrint(relation) : "";

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

        return prefix + value;
    }
}
