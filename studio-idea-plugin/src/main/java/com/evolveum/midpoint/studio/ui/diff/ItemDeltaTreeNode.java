package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ItemMerger;
import com.evolveum.midpoint.prism.key.NaturalKeyDefinition;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ItemDeltaTreeNode extends ObjectDeltaTreeNode<ItemDelta<?, ?>> {

    private Item<?, ?> targetItem;

    public ItemDeltaTreeNode(ItemDelta<?, ?> value, Item<?, ?> targetItem) {
        super(value);

        this.targetItem = targetItem;
    }

    public Item<?, ?> getTargetItem() {
        return targetItem;
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

    private String createReadablePath() {
        PrismObject<?> object = getObject(targetItem);

        ItemPath path = getValue().getPath();

        List<String> segments = new ArrayList<>();

        ItemPath partial = ItemPath.EMPTY_PATH;
        for (Object segment : path.getSegments()) {
            if (segment instanceof Long id) {
                String suffix = createNaturalKeySuffix(object, partial, id);
                if (suffix != null) {
                    segments.add(id + suffix);
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
