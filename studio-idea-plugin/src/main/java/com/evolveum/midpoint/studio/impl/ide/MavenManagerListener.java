package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.studio.impl.MidPointFacet;
import com.evolveum.midpoint.studio.impl.MidPointFacetConfiguration;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MavenManagerListener implements MavenProjectsManager.Listener {

    private static final Logger LOG = Logger.getInstance(MavenManagerListener.class);

    private final Project project;

    private final Module module;

    public MavenManagerListener(@NotNull Project project) {
        this(project, null);
    }

    public MavenManagerListener(@NotNull Project project, Module module) {
        this.project = project;
        this.module = module;
    }

    @Override
    public void projectImportCompleted() {
        LOG.info("Maven project import completed");

        if (project == null) {
            return;
        }

        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
            try {
                if (MidPointUtils.hasMidPointFacet(project)) {
                    return;
                }

                Module module = this.module;
                if (module == null) {
                    ModuleManager mm = ModuleManager.getInstance(project);
                    Module[] modules = mm.getModules();
                    if (modules.length == 0) {
                        return;
                    }

                    module = modules[0];
                }

                project.save();

                FacetType<MidPointFacet, MidPointFacetConfiguration> facetType =
                        FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);

                FacetUtil.addFacet(module, facetType, facetType.getPresentableName());
            } catch (Exception ex) {
                LOG.error("Couldn't add MidPoint facet", ex);
            }
        });
    }
}
