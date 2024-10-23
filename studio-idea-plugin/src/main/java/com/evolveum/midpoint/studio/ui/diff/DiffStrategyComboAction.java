package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.studio.ui.CustomComboBoxAction;

import java.util.Arrays;
import java.util.List;

public class DiffStrategyComboAction extends CustomComboBoxAction<DiffStrategy> {

    public DiffStrategyComboAction(DiffStrategy defaultStrategy) {
        super(defaultStrategy);
    }

    @Override
    public List<DiffStrategy> getItems() {
        return Arrays.asList(DiffStrategy.values());
    }

    @Override
    protected String createItemLabel(DiffStrategy item) {
        return item != null ? item.getLabel() : "";
    }
}
