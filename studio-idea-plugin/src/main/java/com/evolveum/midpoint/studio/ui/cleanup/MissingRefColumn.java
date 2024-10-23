package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

public class MissingRefColumn extends DefaultColumnInfo {

    public MissingRefColumn() {
        super("Reference");

        setMinWidth(50);
        setPreferredWidth(200);
    }

    @Nullable
    @Override
    public Object valueOf(DefaultMutableTreeTableNode node) {
        Object object = node.getUserObject();

        if (object instanceof MissingRefNode refNode) {
            Object value = refNode.getValue();
            if (value == MissingRefObjectsTableModel.NODE_ALL) {
                return StudioLocalization.get().translate("MissingRefColumn.all");
            } else if (value == MissingRefObjectsTableModel.NODE_ROOT) {
                return StudioLocalization.get().translate("MissingRefColumn.root");
            }

            if (value instanceof ObjectTypes type) {
                return StudioLocalization.get().translateEnum(type);
            }

            if (value instanceof MissingRefObject refObject) {
                String type = getTypeSuffix(refObject.getType());
                if (refObject.getName() != null) {
                    return refObject.getName() + type;
                }

                return refObject.getOid() + getTypeSuffix(refObject.getType());
            }

            return null;
        }

        if (object instanceof MissingRef ref) {
            String type = getTypeSuffix(ref.getType());
            if (ref.getName() != null) {
                return ref.getName() + type;
            }

            return ref.getOid() + getTypeSuffix(ref.getType());
        }

        return null;
    }

    private String getTypeSuffix(QName type) {
        ObjectTypes t = ObjectTypes.getObjectTypeFromTypeQName(type);

        return " (" + StudioLocalization.get().translateEnum(t) + ")";
    }

    @Override
    public Class<?> getColumnClass() {
        return TreeTableModel.class;
    }
}
