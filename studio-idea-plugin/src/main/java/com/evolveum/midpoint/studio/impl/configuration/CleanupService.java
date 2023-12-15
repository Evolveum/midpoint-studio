package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.common.cleanup.*;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.messages.MessageDialog;
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

    public CleanupActionProcessor createCleanupProcessor() {
        CleanupActionProcessor processor = new CleanupActionProcessor();
        processor.setIgnoreNamespaces(true);
        processor.setPaths(getCleanupPaths());
        processor.setListener(new CleanupListener() {

            @Override
            public boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
                return CleanupService.this.onConfirmOptionalCleanup(event);
            }
        });

        processor.setRemoveAskActionItemsByDefault(getAskActionOverride() == CleanupPathAction.REMOVE);

        return processor;
    }

    private boolean onConfirmOptionalCleanup(CleanupEvent<Item<?, ?>> event) {
        int result = MidPointUtils.showConfirmationDialog(
                getProject(), null, "Do you really want to remove item " + event.getPath() + "?",
                "Confirm remove", "Remove", "Skip");

        return result == MessageDialog.OK_EXIT_CODE;
    }
}