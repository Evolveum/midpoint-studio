package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.ReferenceDecisionConfiguration;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;

public class DecisionColumn extends DefaultColumnInfo<Object, ReferenceDecisionConfiguration> {

    public DecisionColumn() {
        super("Decision", o -> {
            if (o == null) {
                // todo root
            }

            return ReferenceDecisionConfiguration.ALWAYS; // todo fix
        });

        setPreferredWidth(150);
        setMinWidth(150);
        setMaxWidth(150);
    }

    @Override
    public Class<?> getColumnClass() {
        return ReferenceDecisionConfiguration.class;
    }
}
