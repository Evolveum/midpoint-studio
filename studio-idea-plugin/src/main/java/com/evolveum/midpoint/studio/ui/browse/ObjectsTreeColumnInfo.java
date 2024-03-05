package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

public class ObjectsTreeColumnInfo extends ObjectColumnInfo<Object, String> {

    public ObjectsTreeColumnInfo() {
        super("Name");
    }

    @Override
    public Class<?> getColumnClass() {
        return TreeTableModel.class;
    }

    @Override
    public @Nullable String valueOf(DefaultMutableTreeTableNode node) {
        Object userObject = node.getUserObject();

        if (userObject instanceof ObjectTypes) {
            ObjectTypes type = (ObjectTypes) userObject;

            return StudioLocalization.get().translateEnum(type);
        } else if (userObject instanceof ObjectType) {
            ObjectType ot = (ObjectType) userObject;
            return MidPointUtils.getOrigFromPolyString(ot.getName());
        }

        return "<<Unknown>>";
    }
}
