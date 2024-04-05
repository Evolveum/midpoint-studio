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
                                                        r -> r.getAction()
                                                )
                                        )
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
                .collect(Collectors.toMap(o -> new MissingRefKey(o.getOid(), o.getType()), o -> o));

        List<ObjectReferenceType> download = new ArrayList<>();

        for (MissingRefObject mro : cleanupTaskOutput) {
            MissingRefObject mroConfig = map.get(new MissingRefKey(mro.getOid(), mro.getType()));

            Map<MissingRefKey, MissingRefAction> refConfigMap = new HashMap<>();
            if (mroConfig != null) {
                refConfigMap = mroConfig.getReferences().stream()
                        .collect(Collectors.toMap(o -> new MissingRefKey(o.getOid(), o.getType()), o -> o.getAction()));
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
}
