package com.evolveum.midpoint.studio.ui.trace.entry;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Collections;
import java.util.Objects;

public abstract class Node<T> extends DefaultMutableTreeNode {

    private String label;

    private String value;

    private Color backgroundColor;

    public Node() {
    }

    public Node(Object userObject) {
        super(userObject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getUserObject() {
        return (T) super.getUserObject();
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
            Collections.list(children()).forEach(child -> {
                if (child instanceof Node<?> n) {
                    n.setBackgroundColor(backgroundColor, true);
                }
            });
        }
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Object getObject() {
        return getUserObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Node)) {
            return false;
        }
        Node<?> node = (Node<?>) o;
        return Objects.equals(label, node.label) &&
                Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, value);
    }
}
