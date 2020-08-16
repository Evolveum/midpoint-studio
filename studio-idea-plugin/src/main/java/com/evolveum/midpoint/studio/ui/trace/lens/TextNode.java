package com.evolveum.midpoint.studio.ui.trace.lens;

import java.util.List;

public class TextNode extends PrismNode {

    private final String label;

    private final List<String> values;

    public TextNode(String label, List<String> values, PrismNode parent) {
        super(parent);
        this.label = label;
        this.values = values;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getValue(int i) {
        return values.get(i);
    }

}
