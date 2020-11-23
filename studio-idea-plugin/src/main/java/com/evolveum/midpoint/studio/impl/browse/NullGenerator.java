package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

import java.util.List;

public class NullGenerator extends Generator {

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public String generate(List<ObjectType> objects, GeneratorOptions options) {
        return "";
    }

}
