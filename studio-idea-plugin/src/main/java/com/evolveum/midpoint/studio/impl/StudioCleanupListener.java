package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.common.cleanup.CleanupEvent;
import com.evolveum.midpoint.common.cleanup.DefaultCleanupListener;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.impl.cache.EnvironmentCacheManager;
import com.evolveum.midpoint.studio.impl.cache.ObjectCache;
import com.evolveum.midpoint.studio.impl.configuration.CleanupConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.CleanupService;
import com.evolveum.midpoint.studio.impl.psi.search.ObjectFileBasedIndexImpl;
import com.evolveum.midpoint.studio.util.MavenUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StudioCleanupListener extends DefaultCleanupListener {

    private final Project project;

    public StudioCleanupListener(@NotNull Project project, @NotNull PrismContext prismContext) {

        super(prismContext);

        this.project = project;

        CleanupService cs = CleanupService.get(project);
        CleanupConfiguration configuration = cs.getSettings();
        setWarnAboutMissingReferences(configuration.isWarnAboutMissingReferences());
    }

    @Override
    public boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                project, "Do you really want to remove item " + event.path() + "?",
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
        EnvironmentCacheManager ecm = EnvironmentCacheManager.get(project);
        ObjectCache<ConnectorType> cache = ecm.getCache(EnvironmentCacheManager.KEY_CONNECTOR);

        ConnectorType connector = cache.get(oid);
        if (connector == null) {
            return null;
        }

        return connector.asPrismObject();
    }
}
