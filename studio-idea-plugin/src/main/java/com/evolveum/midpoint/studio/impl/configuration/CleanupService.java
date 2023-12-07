package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.schema.util.cleanup.CleanupActionProcessor;
import com.evolveum.midpoint.schema.util.cleanup.CleanupEvent;
import com.evolveum.midpoint.schema.util.cleanup.CleanupPath;
import com.evolveum.midpoint.schema.util.cleanup.CleanupPathAction;
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

    public static CleanupService getInstance(@NotNull Project project) {
        return project.getService(CleanupService.class);
    }

    @Override
    protected CleanupConfiguration createDefaultSettings() {
        return new CleanupConfiguration();
    }

    private List<CleanupPath> getCleanupPaths() {
        return getSettings().getCleanupPaths().stream()
                .map(cp -> cp.toCleanupPath())
                .collect(Collectors.toList());
    }

    private CleanupPathAction getAskActionOverride() {
        CleanupPathActionConfiguration action = getSettings().getAskActionOverride();
        if (action == null) {
            return CleanupPathAction.ASK;
        }

        return action.value();
    }

    public CleanupActionProcessor createCleanupProcessor() {
        CleanupActionProcessor processor = new CleanupActionProcessor();
        processor.setPaths(getCleanupPaths());
        processor.setListener(evt -> handleCleanupEvent(evt));

        processor.setRemoveAskActionItemsByDefault(getAskActionOverride() == CleanupPathAction.REMOVE);

        return processor;
    }

    private boolean handleCleanupEvent(CleanupEvent event) {
        // todo implement
        return false;
    }
}
