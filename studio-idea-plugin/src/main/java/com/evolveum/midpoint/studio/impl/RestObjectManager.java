package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.evolveum.midpoint.studio.action.browse.DownloadOptions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface RestObjectManager extends Listener {

    static RestObjectManager getInstance(@NotNull Project project) {
        return project.getComponent(RestObjectManager.class);
    }

    Environment getEnvironment();

    PrismContext getPrismContext();

    <O extends ObjectType> SearchResultList search(Class<O> type, ObjectQuery query, boolean raw);

    <O extends ObjectType> void download(Class<O> type, ObjectQuery query, DownloadOptions options);

    void upload(String text, UploadOptions options);

    void upload(List<VirtualFile> files, UploadOptions options);

    OperationResult testResource(String oid);
}
