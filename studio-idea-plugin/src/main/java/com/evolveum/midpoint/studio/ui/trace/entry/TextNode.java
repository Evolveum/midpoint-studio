package com.evolveum.midpoint.studio.ui.trace.entry;

public class TextNode extends Node<String> {

    public TextNode(String label, String value) {
        super(value);

        setLabel(label);
        setValue(value);
    }

    public static TextNode create(String label, Object value, Node parent) {
        TextNode node = new TextNode(label, value != null ? value.toString() : "");
        if (parent != null) {
            parent.add(node);
        }

        return node;
    }
}
