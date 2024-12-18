package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.openapi.project.Project;

import java.util.*;
import java.util.stream.Collectors;

public class MissingRefUtils {

    private static MissingRefObject removeNonActionableRefs(MissingRefObject object) {
        object.getReferences().removeIf(ref -> ref.getAction() == null || ref.getAction() == MissingRefAction.UNDEFINED);
        return object.getReferences().isEmpty() ? null : object;
    }

    /**
     * Clones the list of objects and removes those that have no actionable references (i.e. UNDEFINED or null action).
     */
    public static List<MissingRefObject> cloneAndRemoveNonActionable(List<MissingRefObject> objects) {
        return objects.stream()
                .map(MissingRefObject::copy)
                .map(o -> removeNonActionableRefs(o))
                .filter(o -> o != null)
                .collect(Collectors.toList());
    }

    public static Map<MissingRefKey, Map<MissingRefKey, MissingRefAction>> buildActionMapFromConfiguration(Project project) {
        MissingRefObjects config = CleanupService.get(project).getSettings().getMissingReferences();

        return config.getObjects().stream()
                .collect(
                        Collectors.toMap(
                                o -> new MissingRefKey(o.getOid(), o.getType()),
                                o -> o.getReferences().stream()
                                        .collect(
                                                Collectors.toMap(
                                                        r -> new MissingRefKey(r.getOid(), r.getType()),
                                                        r -> r.getAction(),
                                                        (m1, m2) -> m1
                                                )
                                        ),
                                (m1, m2) -> m1
                        )
                );
    }

    public static List<ObjectReferenceType> computeDownloadOnly(Project project, List<MissingRefObject> cleanupTaskOutput) {
        MissingRefObjects config = CleanupService.get(project).getSettings().getMissingReferences();
        MissingRefAction defaultAction = config.getDefaultAction();
        if (defaultAction == null) {
            defaultAction = MissingRefAction.DOWNLOAD;
        }

        Map<MissingRefKey, MissingRefObject> map = config.getObjects().stream()
                .collect(Collectors.toMap(
                        o -> new MissingRefKey(o.getOid(), o.getType()),
                        o -> o,
                        (m1, m2) -> m1));

        List<ObjectReferenceType> download = new ArrayList<>();

        for (MissingRefObject mro : cleanupTaskOutput) {
            MissingRefObject mroConfig = map.get(new MissingRefKey(mro.getOid(), mro.getType()));

            Map<MissingRefKey, MissingRefAction> refConfigMap = new HashMap<>();
            if (mroConfig != null) {
                refConfigMap = mroConfig.getReferences().stream()
                        .collect(Collectors.toMap(
                                o -> new MissingRefKey(o.getOid(), o.getType()),
                                o -> o.getAction(),
                                (m1, m2) -> m1));
            }

            for (MissingRef ref : mro.getReferences()) {
                MissingRefAction action = refConfigMap.get(new MissingRefKey(ref.getOid(), ref.getType()));
                if (action == null) {
                    action = defaultAction;
                }

                if (Objects.equals(action, MissingRefAction.DOWNLOAD)) {
                    download.add(new ObjectReferenceType()
                            .oid(ref.getOid())
                            .type(ref.getType()));
                }
            }
        }

        return download;
    }

    /**
     * Update settings (in midpoint.xml) for missing ref objects defined in missingRefObjects parameter.
     * <p>
     * Replaces existing {@link MissingRefObject} object with the ones from missingRefObjects list.
     * Removes {@link MissingRefObject} objects that were removed in the editor dialog by matching against input.
     */
    public static void updateMissingRefSettings(
            Project project, List<MissingRefObject> missingRefObjects, List<MissingRefKey> removed) {

        List<MissingRefObject> cloned = MissingRefUtils.cloneAndRemoveNonActionable(missingRefObjects);

        CleanupService cs = CleanupService.get(project);

        CleanupConfiguration config = cs.getSettings();
        MissingRefObjects objects = config.getMissingReferences();

        Map<MissingRefKey, MissingRefObject> existing = objects.getObjects().stream()
                .collect(Collectors.toMap(
                        o -> new MissingRefKey(o.getOid(), o.getType()),
                        o -> o,
                        (m1, m2) -> m1));

        // replace objects in settings with the ones from the dialog
        for (MissingRefObject object : cloned) {
            MissingRefKey key = new MissingRefKey(object.getOid(), object.getType());

            MissingRefObject existingObject = existing.get(key);
            if (existingObject != null) {
                objects.getObjects().remove(existingObject);
            }

            objects.getObjects().add(object);
        }

        removed.forEach(key -> {
            MissingRefObject existingObject = existing.get(key);
            if (existingObject != null) {
                objects.getObjects().remove(existingObject);
            }
        });

        cs.settingsUpdated();
    }

    /**
     * Finds action in cleanup settings for given missing references - if there's any
     * and populates the action in the missing reference object.
     *
     * @param project
     * @param missing
     * @return cloned list of missing references with actions populated from cleanup settings
     */
    public static List<MissingRefObject> populateCurrentActionsForMissingRefs(
            Project project, List<MissingRefObject> missing) {

        List<MissingRefObject> clonedMissing = missing.stream()
                .map(MissingRefObject::copy)
                .collect(Collectors.toList());

        CleanupService cs = CleanupService.get(project);
        MissingRefObjects settings = cs.getSettings().getMissingReferences();

        Map<MissingRefKey, MissingRefObject> existing = settings.getObjects().stream()
                .collect(Collectors.toMap(
                        o -> new MissingRefKey(o.getOid(), o.getType()),
                        o -> o,
                        (m1, m2) -> m1));

        for (MissingRefObject clonedObject : clonedMissing) {
            MissingRefKey key = new MissingRefKey(clonedObject.getOid(), clonedObject.getType());
            MissingRefObject existingObject = existing.get(key);

            Map<MissingRefKey, MissingRefAction> existingRefs = existingObject != null ?
                    existingObject.getReferences().stream()
                            .collect(Collectors.toMap(
                                    o -> new MissingRefKey(o.getOid(), o.getType()),
                                    o -> o.getAction(),
                                    (m1, m2) -> m1))
                    : new HashMap<>();

            clonedObject.getReferences().forEach(mr -> {
                MissingRefKey refKey = new MissingRefKey(mr.getOid(), mr.getType());
                MissingRefAction action = existingRefs.get(refKey);
                if (action != null) {
                    mr.setAction(action);
                }
            });
        }

        return clonedMissing;
    }

    /**
     * Computes list of {@link MissingRefKey} removed objects from output list compare to input list
     *
     * @param input  list of {@link MissingRefObject}
     * @param output list of {@link MissingRefObject} that was modified (may not contain all objects from input list)
     * @return list of {@link MissingRefKey keys representing objects removed from input list
     */
    public static List<MissingRefKey> computeRemovedRefKeys(
            List<MissingRefObject> input, List<MissingRefObject> output) {

        Set<MissingRefKey> resultRefKeys = output.stream()
                .map(o -> new MissingRefKey(o.getOid(), o.getType()))
                .collect(Collectors.toSet());

        return input.stream()
                .map(o -> new MissingRefKey(o.getOid(), o.getType()))
                .filter(k -> !resultRefKeys.contains(k))
                .toList();
    }
}
