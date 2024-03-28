package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.ReferenceDecisionConfiguration;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;

public class DecisionColumn extends DefaultColumnInfo<Object, Boolean> {

    private final ReferenceDecisionConfiguration decision;

    public DecisionColumn(String name, ReferenceDecisionConfiguration decision) {
        super(name, o -> {
            return true; // todo fix
        });

        this.decision = decision;

        setPreferredWidth(40);
        setMinWidth(40);
        setMaxWidth(40);
    }

    @Override
    public Class<?> getColumnClass() {
        return Boolean.class;
    }
}
