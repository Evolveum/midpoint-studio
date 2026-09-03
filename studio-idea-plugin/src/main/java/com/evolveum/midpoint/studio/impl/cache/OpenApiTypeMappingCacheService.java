package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.studio.lang.groovy.OpenApiTypeConstant;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dominik.
 */
public class OpenApiTypeMappingCacheService {

    private final Map<String, List<OpenApiTypeConstant>> openApiTypeCache = new ConcurrentHashMap<>();

    public void put(String fqClassName, List<OpenApiTypeConstant> data) {
        openApiTypeCache.put(fqClassName, data);
    }

    public List<OpenApiTypeConstant> get(String fqClassName) {
        return openApiTypeCache.get(fqClassName);
    }

    public boolean contains(String fqClassName) {
        return openApiTypeCache.containsKey(fqClassName);
    }

    public void clear() {
        openApiTypeCache.clear();
    }

    // TODO refresh value after changes of file
//    public static List<OpenApiTypeConstant> getCachedConstants(Project project, PsiClass psiClass) {
//        return CachedValuesManager.getManager(project).getCachedValue(
//                psiClass, () -> {
//                    List<OpenApiTypeConstant> data = extractEnumConstants(psiClass);
//                    return CachedValueProvider.Result.create(data, psiClass);
//                }
//        );
//    }
}
