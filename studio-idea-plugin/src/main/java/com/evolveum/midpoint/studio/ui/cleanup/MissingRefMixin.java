package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.*;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.openapi.project.Project;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

public interface MissingRefMixin {

    record Key(String oid, QName type) {
    }

    default List<ObjectReferenceType> computeDownloadOnly(Project project, List<MissingRefObject> cleanupTaskOutput) {
        MissingRefObjects config = CleanupService.get(project).getSettings().getMissingReferences();
        MissingRefAction defaultAction = config.getDefaultAction();
        if (defaultAction == null) {
            defaultAction = MissingRefAction.DOWNLOAD;
        }

        Map<Key, MissingRefObject> map = config.getObjects().stream()
                .collect(Collectors.toMap(o -> new Key(o.getOid(), o.getType()), o -> o));

        List<ObjectReferenceType> download = new ArrayList<>();

        for (MissingRefObject mro : cleanupTaskOutput) {
            MissingRefObject mroConfig = map.get(new Key(mro.getOid(), mro.getType()));

            Map<Key, MissingRefAction> refConfigMap = new HashMap<>();
            if (mroConfig != null) {
                refConfigMap = mroConfig.getReferences().stream()
                        .collect(Collectors.toMap(o -> new Key(o.getOid(), o.getType()), o -> o.getAction()));
            }

            for (MissingRef ref : mro.getReferences()) {
                MissingRefAction action = refConfigMap.get(new Key(ref.getOid(), ref.getType()));
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
