package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.ui.NamedItem;
import com.evolveum.midpoint.studio.ui.DefaultColumnInfo;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

import javax.xml.namespace.QName;

public class ReferenceColumn extends DefaultColumnInfo {

    public ReferenceColumn() {
        super(
                "Reference",
                o -> {
                    if (o == null) {
                        return StudioLocalization.message("MissingObjectTableModel.all");
                    }

                    if (o instanceof QName qname) {
                        ObjectTypes type = ObjectTypes.getObjectTypeFromTypeQNameIfKnown(qname);
                        return StudioLocalization.get().translateEnum(type, "MissingObjectTableModel.unknown");
                    }

                    if (o instanceof NamedItem ref) {
                        return ref.name().get();
                    }

                    return o.toString();
                });
    }

    @Override
    public Class<?> getColumnClass() {
        return TreeTableModel.class;
    }
}
