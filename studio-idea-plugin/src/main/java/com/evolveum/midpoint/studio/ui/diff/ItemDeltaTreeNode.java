package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.binding.TypeSafeEnum;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDeltaTreeNode extends ObjectDeltaTreeNode<ItemDelta<?, ?>> {

    private final PrismObject<?> target;

    public ItemDeltaTreeNode(ItemDelta<?, ?> value, PrismObject<?> target) {
        super(value);

        this.target = target;
    }

    public Item<?, ?> getTargetItem() {
        if (getValue() == null) {
            return null;
        }

        return target.findItem(getValue().getPath());
    }

    @Override
    String getText() {
        return createReadablePath();
    }

    @Override
    ModificationType getModificationType() {
        ItemDelta<?, ?> delta = getValue();

        return getModificationType(delta);
    }

    @Override
    ApplicableDelta<?> getApplicableDelta() {
        return new ApplicableItemDelta<>(getValue());
    }

    private String createReadablePath() {
        ItemPath path = getValue().getPath();

        List<String> segments = new ArrayList<>();

        ItemPath partial = ItemPath.EMPTY_PATH;
        for (Object segment : path.getSegments()) {
            if (segment instanceof Long id) {
                String suffix = createReadableDescription(target, partial, id);
                if (suffix != null) {
                    segments.add(id + " (" + suffix + ")");
                } else {
                    segments.add(id.toString());
                }
            } else {
                segments.add(ItemPathSegment.toString(segment));
            }

            partial = partial.append(segment);
        }

        return StringUtils.join(segments, "/");
    }

    private String createReadableDescription(PrismObject<?> object, ItemPath path, Long id) {
        String description = getDisplayName(target, path, id);
        if (description != null) {
            return description;
        }

        return getNaturalKey(object, path, id);
    }

    private String getDisplayName(PrismObject<?> object, ItemPath path, Long id) {
        if (object == null) {
            return null;
        }

        PrismContainer<?> item = object.findContainer(path);
        if (item == null) {
            return null;
        }

        PrismContainerValue<?> value = null;
        if (id != null) {
            value = item.findValue(id);
        } else if (item.isSingleValue()) {
            value = item.getValue();
        }

        if (value == null) {
            return null;
        }

        Object displayName = value.getPropertyRealValue(ResourceObjectTypeDefinitionType.F_DISPLAY_NAME, Object.class);
        if (displayName instanceof String s) {
            return s;
        } else if (displayName instanceof PolyString poly) {
            return poly.getOrig();
        } else if (displayName instanceof PolyStringType poly) {
            return poly.getOrig();
        }

        return null;
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
                .map(i -> i.getElementName().getLocalPart() + ": " + StringUtils.join(getPresentableRealValues(i.getRealValues()), ", "))
                .collect(Collectors.joining("; "));

        return StringUtils.abbreviate(key, 200);
    }

    private Collection<?> getPresentableRealValues(Collection<?> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .map(v -> {
                    if (v instanceof QName qname) {
                        return qname.getLocalPart();
                    }

                    if (v instanceof TypeSafeEnum tse) {
                        return tse.value();
                    }

                    return v;
                })
                .toList();
    }
}
