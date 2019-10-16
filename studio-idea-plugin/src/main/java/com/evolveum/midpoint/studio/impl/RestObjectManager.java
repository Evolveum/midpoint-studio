package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.DownloadOptions;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * TODO cleanup this interface !!!
 *
 * Created by Viliam Repan (lazyman).
 */
public interface RestObjectManager extends Listener {

    static RestObjectManager getInstance(@NotNull Project project) {
        return project.getComponent(RestObjectManager.class);
    }

    Environment getEnvironment();

    PrismContext getPrismContext();

    <O extends ObjectType> PrismObject<O> get(Class<O> type, String oid, SearchOptions opts);

    <O extends ObjectType> SearchResultList search(Class<O> type, ObjectQuery query, boolean raw);

    <O extends ObjectType> VirtualFile[] download(Class<O> type, ObjectQuery query, DownloadOptions options);

    void upload(String text, UploadOptions options);

    void upload(List<VirtualFile> files, UploadOptions options);

    <O extends ObjectType> PrismObject<O> parse(VirtualFile file);

    List<PrismObject<?>> parseObjects(VirtualFile file) throws SchemaException, IOException;

    OperationResult testResource(String oid);
}
