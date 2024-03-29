package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.ui.NamedItem;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

public class MissingRefColumn extends DefaultColumnInfo {

    public MissingRefColumn() {
        super(
                "Reference",
                o -> {
                    if (o == MissingRefObjectsTableModel.NODE_ROOT) {
                        // shouldn't be visible at all
                        return null;
                    }

                    if (o == MissingRefObjectsTableModel.NODE_ALL) {
                        return StudioLocalization.message("MissingObjectTableModel.all");
                    }

                    if (o instanceof ObjectTypes type) {
                        return StudioLocalization.get().translateEnum(type);
                    }

                    if (o instanceof NamedItem ref) {
                        return ref.name().get();
                    }

                    return o.toString();
                });

        setMinWidth(50);
        setPreferredWidth(200);
    }

    @Override
    public Class<?> getColumnClass() {
        return TreeTableModel.class;
    }
}
