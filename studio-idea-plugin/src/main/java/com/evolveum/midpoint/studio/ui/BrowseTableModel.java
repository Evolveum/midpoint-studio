package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AbstractRoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BrowseTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Name", "Display Name", "Subtype", "Oid"};

    private List<ObjectType> data = new ArrayList<>();

    public List<ObjectType> getData() {
        return data;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ObjectType obj = data.get(rowIndex);

        if (obj == null) {
            return null;
        }

        PrismObject object = obj.asPrismObject();

        switch (columnIndex) {
            case 0:
                return getOrigFromPolyString(object.getName());
            case 1:
                if (obj instanceof AbstractRoleType) {
                    PolyString name = (PolyString) object.getPropertyRealValue(AbstractRoleType.F_DISPLAY_NAME, PolyString.class);
                    return getOrigFromPolyString(name);
                }
                break;
            case 2:
                return StringUtils.join(obj.getSubtype(), ", ");
            case 3:
                return obj.getOid();
            default:
        }

        return null;
    }

    private String getOrigFromPolyString(PolyString poly) {
        return poly != null ? poly.getOrig() : null;
    }
}
