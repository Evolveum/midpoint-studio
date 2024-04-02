package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTableModel;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.util.ui.ColumnInfo;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissingRefObjectsTableModel extends DefaultTreeTableModel<List<MissingRefObject>> {

    public static final Object NODE_ROOT = new Object();

    public static final Object NODE_ALL = new Object();

    private static final List<ColumnInfo> COLUMNS = List.of(
            new MissingRefColumn(),
            new MissingRefActionColumn()
    );

    public MissingRefObjectsTableModel() {
        super(COLUMNS);
    }

    @Override
    public void setData(List<MissingRefObject> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode(new MissingRefNode<>(NODE_ROOT, null));

        DefaultMutableTreeTableNode allNode = new DefaultMutableTreeTableNode(new MissingRefNode<>(NODE_ALL, null));
        rootNode.add(allNode);

        Map<QName, List<MissingRefObject>> map = data.stream()
                .collect(Collectors.groupingBy(MissingRefObject::getType));

        List<ObjectTypes> types = Arrays.asList(ObjectTypes.values());
        types.sort(MidPointUtils.OBJECT_TYPES_COMPARATOR);

        for (ObjectTypes type : types) {
            List<MissingRefObject> list = map.get(type.getTypeQName());
            if (list == null) {
                continue;
            }

            DefaultMutableTreeTableNode typeNode =
                    new DefaultMutableTreeTableNode(
                            new MissingRefNode<>(type, null));
            allNode.add(typeNode);

            for (MissingRefObject object : list) {
                DefaultMutableTreeTableNode objectNode =
                        new DefaultMutableTreeTableNode(
                                new MissingRefNode<>(object, null));
                typeNode.add(objectNode);

                for (MissingRef ref : object.getReferences()) {
                    DefaultMutableTreeTableNode refNode = new DefaultMutableTreeTableNode(ref);
                    objectNode.add(refNode);
                }
            }
        }

        setRoot(rootNode);

        super.setData(data);
    }
}
