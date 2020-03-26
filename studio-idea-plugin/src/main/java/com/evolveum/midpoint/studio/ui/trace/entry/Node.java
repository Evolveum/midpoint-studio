package com.evolveum.midpoint.studio.ui.trace.entry;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import java.util.Objects;

public abstract class Node extends AbstractMutableTreeTableNode {

    @Override
    public Object getValueAt(int i) {
        switch (i) {
            case 0:
                return getLabel();
            case 1:
                return getValue();
        }

        return null;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    public abstract String getLabel();

    public abstract String getValue();

    public abstract Object getObject();

    @Override
    public int hashCode() {
        return Objects.hash(getLabel());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Node)) {
            return false;
        } else {
            Node node = (Node) obj;
            return Objects.equals(getLabel(), node.getLabel());
        }
    }
}
