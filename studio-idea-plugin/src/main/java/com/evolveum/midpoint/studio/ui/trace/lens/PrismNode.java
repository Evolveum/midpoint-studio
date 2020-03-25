package com.evolveum.midpoint.studio.ui.trace.lens;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public abstract class PrismNode extends AbstractMutableTreeTableNode {

    protected PrismNode parent;        // due to hack in PrismItemNode

    public PrismNode(PrismNode parent) {
        this.parent = parent;

        if (parent != null) {
            parent.add(this);
        }
    }

    public List<PrismNode> getChildren() {
        Enumeration e = children();
        if (e == null) {
            return new ArrayList<>();
        }

        return Collections.list(e);
    }

    public abstract String getLabel();

    public abstract String getValue(int i);

    @Override
    public Object getValueAt(int i) {
        if (i == 0) {
            return getLabel();
        }

        return getValue(i - 1);
    }

    @Override
    public int getColumnCount() {
        return 4;
    }
}
