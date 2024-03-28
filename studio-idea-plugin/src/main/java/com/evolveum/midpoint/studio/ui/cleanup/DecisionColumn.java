package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.ReferenceDecisionConfiguration;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;

public class DecisionColumn extends DefaultColumnInfo<Object, Boolean> {

    private final ReferenceDecisionConfiguration decision;

    public DecisionColumn(String name, ReferenceDecisionConfiguration decision) {
        super(name, o -> {
            if (o == null) {
                // todo root
            }

            return true; // todo fix
        });

        this.decision = decision;

        setPreferredWidth(80);
        setMinWidth(80);
        setMaxWidth(80);
    }

    @Override
    public Class<?> getColumnClass() {
        return Boolean.class;
    }
}
