package com.evolveum.midpoint.studio.ui.trace.entry;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public class TextNode extends Node<String> {

    public TextNode(String label, String value) {
        super(value);

        setLabel(label);
        setValue(value);
    }

    public static TextNode create(String label, Object value, AbstractMutableTreeTableNode parent) {
        TextNode node = new TextNode(label, value != null ? value.toString() : "");
        if (parent != null) {
            parent.add(node);
        }

        return node;
    }
}
