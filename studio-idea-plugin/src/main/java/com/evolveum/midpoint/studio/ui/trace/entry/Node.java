package com.evolveum.midpoint.studio.ui.trace.entry;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import java.awt.*;
import java.util.Objects;

public abstract class Node<T> extends AbstractMutableTreeTableNode {

    private String label;

    private String value;

    private Color backgroundColor;

    public Node() {
    }

    public Node(Object userObject) {
        super(userObject);
    }

    @Override
    public T getUserObject() {
        return (T) super.getUserObject();
    }

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

    public String getLabel() {
        return label;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor, boolean deep) {
        setBackgroundColor(backgroundColor);
        if (deep) {
            for (MutableTreeTableNode child : children) {
                if (child instanceof Node) {
                    ((Node<?>) child).setBackgroundColor(backgroundColor, true);
                }
            }
        }
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Object getObject() {
        return getUserObject();
    }

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
