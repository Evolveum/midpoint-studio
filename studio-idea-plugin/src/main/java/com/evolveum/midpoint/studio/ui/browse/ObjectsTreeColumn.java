package com.evolveum.midpoint.studio.ui.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractRoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

public class ObjectsTreeColumn extends DefaultColumnInfo<Object, String> {

    public ObjectsTreeColumn() {
        super("Name");

        setMinWidth(50);
        setPreferredWidth(350);
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
        }

        if (!(userObject instanceof ObjectType ot)) {
            return "<<Unknown>>";
        }

        String name = MidPointUtils.getOrigFromPolyString(ot.getName());

        String displayName = null;
        if (ot instanceof AbstractRoleType role) {
            displayName = MidPointUtils.getOrigFromPolyString(role.getDisplayName());
        }

        if (displayName != null) {
            displayName = " (" + displayName + ")";
        } else {
            displayName = "";
        }

        return StringUtils.joinWith(" ", name, displayName);
    }
}
