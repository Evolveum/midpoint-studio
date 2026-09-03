package com.evolveum.midpoint.studio.lang.groovy;

import com.evolveum.midpoint.studio.impl.cache.OpenApiTypeMappingCacheService;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dominik.
 */
public class GroovySchemaStartupActivity implements ProjectActivity {

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        // Project indexing complete, can use PSI
        DumbService.getInstance(project).runWhenSmart(() -> {
            storeOpenApiTypeMappingEnum(project);
        });

        return null;
    }

    private void storeOpenApiTypeMappingEnum(Project project) {
        List<OpenApiTypeConstant> openApiTypeConstants = new ArrayList<>();

        PsiClass enumClass = JavaPsiFacade.getInstance(project)
                .findClass(OpenApiTypeConstant.OPEN_API_TYPE_MAPPING_ENUM, GlobalSearchScope.projectScope(project));

        if (enumClass != null && enumClass.isEnum()) {
            for (PsiField field : enumClass.getFields()) {
                if (field instanceof PsiEnumConstant enumConst) {

                    // TODO append to OpenApiTypeConstant record other arguments (availableWireTypes, connidClass)
//                    PsiExpressionList argList = enumConst.getArgumentList();
//                    if (argList != null) {
//                        PsiExpression[] args = argList.getExpressions();
//                        for (int i = 0; i < args.length; i++) {
//                            String argText = args[i].getText();
//                            System.out.println("  Arg " + i + ": " + argText);
//                        }
//                    }

                    openApiTypeConstants.add(new OpenApiTypeConstant(enumConst.getName(), enumClass.getText(), null, null));

                    // Cache OpenApiType constants for completions/validation
                    OpenApiTypeMappingCacheService cacheService = project.getService(OpenApiTypeMappingCacheService.class);
                    cacheService.put(OpenApiTypeConstant.OPEN_API_TYPE_MAPPING_ENUM, openApiTypeConstants);
                }
            }
        }
    }
}
