package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.common.cleanup.CleanupPath;
import com.evolveum.midpoint.common.cleanup.CleanupPathAction;
import com.evolveum.midpoint.common.cleanup.ObjectCleaner;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@State(
        name = "CleanupService", storages = @Storage(value = "midpoint.xml")
)
public class CleanupService extends ServiceBase<CleanupConfiguration> {

    public CleanupService(@NotNull Project project) {
        super(project, CleanupConfiguration.class);
    }

    public static CleanupService get(@NotNull Project project) {
        return project.getService(CleanupService.class);
    }

    @Override
    protected CleanupConfiguration createDefaultSettings() {
        return new CleanupConfiguration();
    }

    private List<CleanupPath> getCleanupPaths() {
        return getSettings().getCleanupPaths().stream()
                .map(CleanupPathConfiguration::toCleanupPath)
                .collect(Collectors.toList());
    }

    private CleanupPathAction getAskActionOverride() {
        CleanupPathActionConfiguration action = getSettings().getAskActionOverride();
        if (action == null) {
            return CleanupPathAction.ASK;
        }

        return action.value();
    }

    private boolean isRemoveContainerIds() {
        // todo enabled also option to remove container IDs in configurable
        return MidPointUtils.isDevelopmentMode(true) && getSettings().isRemoveContainerIds();
    }

    public ObjectCleaner createCleanupProcessor() {
        ObjectCleaner processor = new ObjectCleaner();
        processor.setIgnoreNamespaces(true);
        processor.setPaths(getCleanupPaths());
        processor.setRemoveAskActionItemsByDefault(getAskActionOverride() == CleanupPathAction.REMOVE);
        processor.setRemoveContainerIds(isRemoveContainerIds());

        return processor;
    }
}