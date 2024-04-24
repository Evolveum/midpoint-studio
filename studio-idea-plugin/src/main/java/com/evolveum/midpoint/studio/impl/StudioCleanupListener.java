package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.common.cleanup.CleanupEvent;
import com.evolveum.midpoint.common.cleanup.DefaultCleanupListener;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.util.MavenUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StudioCleanupListener extends DefaultCleanupListener {

    private final Project project;

    private final MidPointClient client;

    public StudioCleanupListener(
            @NotNull Project project, @Nullable MidPointClient client, @NotNull PrismContext prismContext) {

        super(prismContext);

        this.project = project;
        this.client = client;
    }

    @Override
    public boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                project, null, "Do you really want to remove item " + event.path() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }

    @Override
    protected String getMidpointVersion() {
        String current = MavenUtils.getMidpointVersion(project);
        return current != null ? current : MidPointConstants.DEFAULT_MIDPOINT_VERSION;
    }

    @Override
    protected <O extends ObjectType> boolean canResolveLocalObject(Class<O> type, String oid) {
        if (oid == null) {
            return false;
        }

        List<VirtualFile> files = ApplicationManager.getApplication().runReadAction(
                (Computable<List<VirtualFile>>) () ->
                        ObjectFileBasedIndexImpl.getVirtualFiles(oid, project, true));

        return !files.isEmpty();
    }

    @Override
    protected PrismObject<ConnectorType> resolveConnector(String oid) {
        if (client == null) {
            return null;
        }

        try {
            MidPointObject object = client.get(ConnectorType.class, oid, new SearchOptions().raw(true));
            if (object == null) {
                return null;
            }

            return (PrismObject<ConnectorType>) client.parseObject(object.getContent());
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }
}
