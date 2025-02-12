package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.common.cleanup.CleanupEvent;
import com.evolveum.midpoint.common.cleanup.CleanupPath;
import com.evolveum.midpoint.common.cleanup.CleanupPathAction;
import com.evolveum.midpoint.common.cleanup.ObjectCleaner;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.schema.validator.ObjectValidator;
import com.evolveum.midpoint.schema.validator.ValidationItemType;
import com.evolveum.midpoint.studio.impl.StudioCleanupListener;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Override
    public void settingsUpdated() {
        // check for repeating objects or missing refs
        CleanupConfiguration config = getSettings();
        MissingRefObjects objects = config.getMissingReferences();

        List<MissingRefObject> missingRefObjects = objects.getObjects();
        removeDuplicateObjects(missingRefObjects);

        for (MissingRefObject obj : missingRefObjects) {
            List<MissingRef> refs = obj.getReferences();

            removeDuplicateRefs(refs);
        }

        super.settingsUpdated();
    }

    private void removeDuplicateRefs(List<MissingRef> refs) {
        Set<String> existingOids = new HashSet<>();
        refs.removeIf(ref -> !existingOids.add(ref.getOid()));
    }

    private void removeDuplicateObjects(List<MissingRefObject> objects) {
        Set<String> existingOids = new HashSet<>();
        objects.removeIf(obj -> !existingOids.add(obj.getOid()));
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

    private boolean isRemoveMetadata() {
        return getSettings().isRemoveMetadata();
    }

    public ObjectCleaner createCleanupProcessor() {
        ObjectCleaner processor = new ObjectCleaner();
        processor.setRemoveObjectVersion(true);
        processor.setIgnoreNamespaces(true);
        processor.setPaths(getCleanupPaths());
        processor.setRemoveAskActionItemsByDefault(getAskActionOverride() == CleanupPathAction.REMOVE);
        processor.setRemoveContainerIds(isRemoveContainerIds());
        processor.setRemoveMetadata(isRemoveMetadata());

        Project project = getProject();
        processor.setListener(
                new StudioCleanupListener(getProject(), StudioPrismContextService.getPrismContext(project)));

        return processor;
    }

    public ObjectValidator createObjectValidator() {
        ObjectValidator validator = new ObjectValidator();
        validator.setAllWarnings();
        validator.setTypeToCheck(ValidationItemType.MISSING_NATURAL_KEY, getSettings().isMissingNaturalKeys());

        return validator;
    }
}