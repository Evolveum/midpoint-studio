package com.evolveum.midpoint.studio.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnumComboBoxModel<T extends Enum<T>> extends AbstractListModel<T> implements ComboBoxModel<T> {

    private List<T> items = new ArrayList<>();

    private T selected;

    public EnumComboBoxModel(Class<T> type) {
        this(type, false);
    }

    public EnumComboBoxModel(Class<T> type, boolean sort) {
        items.addAll(EnumSet.allOf(type));

        if (sort) {
            items.sort((e1, e2) -> {
                String s1 = (e1 instanceof Localized) ? ((Localized) e1).getKey() : e1.name();
                String s2 = (e2 instanceof Localized) ? ((Localized) e2).getKey() : e2.name();

                return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            });
        }
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int index) {
        return items.get(index);
    }

    @Override
    public void setSelectedItem(Object item) {
        selected = (T) item;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }
}
