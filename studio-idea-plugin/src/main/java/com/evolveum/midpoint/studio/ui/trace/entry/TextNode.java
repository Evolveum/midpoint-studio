package com.evolveum.midpoint.studio.ui.trace.entry;

public class TextNode extends Node {

    private String label;

    private String value;

    public TextNode(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Object getObject() {
        return value;
    }

    public static TextNode create(String label, Object value, Node parent) {
        TextNode node = new TextNode(label, value != null ? value.toString() : "");
        if (parent != null) {
            parent.add(node);
        }

        return node;
    }
}
