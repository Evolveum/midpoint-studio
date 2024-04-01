package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.NamedItem;
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

    private final boolean summary;

    public MissingRefObjectsTableModel(boolean summary) {
        super(COLUMNS);

        this.summary = summary;
    }

    @Override
    public void setData(List<MissingRefObject> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        DefaultMutableTreeTableNode rootNode = new DefaultMutableTreeTableNode(NODE_ROOT);

        DefaultMutableTreeTableNode allNode = new DefaultMutableTreeTableNode(NODE_ALL);
        rootNode.add(allNode);

        Map<QName, List<MissingRefObject>> map = data.stream()
                .collect(Collectors.groupingBy(MissingRefObject::getType));

        List<ObjectTypes> types = Arrays.asList(ObjectTypes.values());
        types.sort(MidPointUtils.OBJECT_TYPES_COMPARATOR);

        // todo how to get unknown type list from map?
        for (ObjectTypes type : types) {
            List<MissingRefObject> list = map.get(type.getTypeQName());
            if (list == null) {
                continue;
            }

            DefaultMutableTreeTableNode typeNode = new DefaultMutableTreeTableNode(type);
            allNode.add(typeNode);

            for (MissingRefObject object : list) {
                DefaultMutableTreeTableNode objectNode = new DefaultMutableTreeTableNode(
                        new NamedItem<>(object, () -> object.getOid())  // todo improve loading of names
                );
                typeNode.add(objectNode);

                for (MissingRef ref : object.getReferences()) {
                    DefaultMutableTreeTableNode refNode = new DefaultMutableTreeTableNode(
                            new NamedItem<>(ref, () -> ref.getOid() + "(" + ref.getType() + ")")
                    );
                    objectNode.add(refNode);
                }
            }
        }

        setRoot(rootNode);

        super.setData(data);
    }
}
