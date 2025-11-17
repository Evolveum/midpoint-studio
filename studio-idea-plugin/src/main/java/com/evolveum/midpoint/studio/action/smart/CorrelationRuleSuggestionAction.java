/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.action.smart;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.QueryFactory;
import com.evolveum.midpoint.schema.SearchResultList;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CorrelationRuleSuggestionAction extends AnAction {

    private String resourceOid;
    private SearchResultList<ResourceType> resources = new SearchResultList<>();

    @Override
    public void update(@NotNull AnActionEvent e) {
        var presentation = e.getPresentation();
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null) {
            EnvironmentService em = EnvironmentService.getInstance(Objects.requireNonNull(e.getProject()));
            Environment env = em.getSelected();
            MidPointClient client = new MidPointClient(e.getProject(), env);
            PrismContext prismContext = StudioPrismContextService.getPrismContext(e.getProject());
            QueryFactory qf = prismContext.queryFactory();

            resources = client.list(ObjectTypes.RESOURCE.getClassDefinition(), qf.createQuery(), true);
            resourceOid = MidPointUtils.findResourceOidByPsi(psiFile);

            var resource = resources.stream()
                    .filter(r -> {
                        assert resourceOid != null;
                        return resourceOid.equals(r.getOid());
                    })
                    .findFirst()
                    .orElse(null);

            if (isCorrelationRule(resource)) {
                presentation.setEnabled(false);
            }
        } else {
            resourceOid = null;
        }
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean isCorrelationRule(ResourceType resource) {
        if (resource == null) {
            return false;
        }

        var schemaHandling = resource.getSchemaHandling();

        if (schemaHandling == null) {
            return false;
        }

        return !schemaHandling.getObjectType().isEmpty();
    }
}
