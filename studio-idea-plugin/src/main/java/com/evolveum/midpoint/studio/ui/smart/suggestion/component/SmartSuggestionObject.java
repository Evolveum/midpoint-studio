package com.evolveum.midpoint.studio.ui.smart.suggestion.component;

import com.evolveum.midpoint.studio.ui.smart.suggestion.component.dialog.GenerateSuggestionDialogContext;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import org.jetbrains.annotations.NotNull;

public class SmartSuggestionObject<T> {

    T object;
    Object parent;
    @NotNull ResourceType resource;
    ResourceObjectTypeDefinitionType objectType;

    public SmartSuggestionObject(T object, @NotNull ResourceType resource) {
        this.object = object;
        this.resource = resource;
    }

    public SmartSuggestionObject(
            T object,
            Object parent,
            @NotNull ResourceType resource,
            ResourceObjectTypeDefinitionType objectType
    ) {
        this.object = object;
        this.parent = parent;
        this.resource = resource;
        this.objectType = objectType;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public ResourceObjectTypeDefinitionType getObjectType() {
        return objectType;
    }

    public void setObjectType(ResourceObjectTypeDefinitionType objectType) {
        this.objectType = objectType;
    }

    public @NotNull ResourceType getResource() {
        return resource;
    }

    public void setResource(@NotNull ResourceType resource) {
        this.resource = resource;
    }
}
