package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface FileObjectManager extends Stateful<FileObjectSettings> {

    static FileObjectManager getInstance(@NotNull Project project) {
        return project.getComponent(FileObjectManager.class);
    }

    <O extends ObjectType> VirtualFile saveObject(PrismObject<O> object, boolean asScratch);

    <O extends ObjectType> VirtualFile[] saveObjects(List<PrismObject<O>> objects, boolean asScratch);

    <O extends ObjectType> PrismObject<O> loadObject(String path, boolean expand);
}
