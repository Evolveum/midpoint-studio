package com.evolveum.midpoint.studio.impl.browse;

import java.util.List;

public class NullGenerator extends Generator {

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public String generate(List<MidPointObject> objects, GeneratorOptions options) {
        return "";
    }

}
