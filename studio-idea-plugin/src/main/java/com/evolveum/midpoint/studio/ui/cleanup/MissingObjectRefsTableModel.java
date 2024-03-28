package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.ObjectReferencesConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.ReferenceDecisionConfiguration;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissingObjectRefsTableModel extends DefaultTreeTableModel<List<ObjectReferencesConfiguration>> {

    public static final List<ColumnInfo> COLUMNS = List.of(
            new ReferenceColumn(),
            new DecisionColumn("Download", ReferenceDecisionConfiguration.DOWNLOAD),
            new DecisionColumn("Ignore", ReferenceDecisionConfiguration.IGNORE)
    );

    public MissingObjectRefsTableModel() {
        super(COLUMNS);
    }

    @Override
    public void setData(List<ObjectReferencesConfiguration> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode(null);

        Map<QName, List<ObjectReferencesConfiguration>> map = data.stream()
                .collect(Collectors.groupingBy(ObjectReferencesConfiguration::getType));

        List<ObjectTypes> types = Arrays.asList(ObjectTypes.values());
        types.sort(MidPointUtils.OBJECT_TYPES_COMPARATOR);


        // todo how to get unknown type list from map?
        for (ObjectTypes type : types) {
            List<ObjectReferencesConfiguration> list = map.get(type.getTypeQName());
            if (list == null) {
                continue;
            }

            DefaultMutableTreeTableNode typeNode = new DefaultMutableTreeTableNode(type);
            root.add(typeNode);

            for (ObjectReferencesConfiguration ref : list) {
                DefaultMutableTreeTableNode refNode = new DefaultMutableTreeTableNode(ref);
                typeNode.add(refNode);
            }
        }

        setRoot(root);

        TreeTableTree tree = getTree();
        if (tree != null) {
            boolean rootVisible = root.getChildCount() > 1;

            tree.setRootVisible(rootVisible);
        }

        super.setData(data);
    }
}
