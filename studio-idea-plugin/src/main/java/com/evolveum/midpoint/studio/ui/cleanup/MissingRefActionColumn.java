package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.configuration.MissingRef;
import com.evolveum.midpoint.studio.impl.configuration.MissingRefAction;
import com.evolveum.midpoint.studio.ui.NamedItem;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;

public class MissingRefActionColumn extends DefaultColumnInfo<Object, String> {

    public MissingRefActionColumn() {
        super("Action", o -> {
            if (o == MissingRefObjectsTableModel.NODE_ROOT
                    || o == MissingRefObjectsTableModel.NODE_ALL
                    || o instanceof ObjectTypes) {
                return null;
            }

            if (o instanceof NamedItem item) {
                if (item.value() instanceof MissingRef ref) {
                    MissingRefAction action = ref.getAction();

                    return action != null ? StudioLocalization.get().translateEnum(action) : null;
                }
            }

            return null;
        });

        setPreferredWidth(150);
        setMinWidth(150);
        setMaxWidth(150);
    }

    @Override
    public @Nullable TableCellEditor getEditor(DefaultMutableTreeTableNode node) {
        return new ComboBoxTableRenderer<>(MissingRefAction.values());
    }

    @Override
    public Class<?> getColumnClass() {
        return MissingRefAction.class;
    }
}
