package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.impl.ide.MidPointModuleBuilder;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.BoundSearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointSettingsConfigurable extends BoundSearchableConfigurable {

    private static final Logger LOG = Logger.getInstance(MidPointSettingsConfigurable.class);

    private Project project;

    private FullConfigurationPanel panel;

    public MidPointSettingsConfigurable(@NotNull Project project) {
        super("MidPoint 2", "", "midpoint.project.structure");
        this.project = project;
    }

    @NotNull
    @Override
    public DialogPanel createPanel() {
        MidPointService mm = MidPointService.getInstance(project);
        EnvironmentService em = EnvironmentService.getInstance(project);

        GeneralConfiguration config = FullConfigurationKt.asGeneralConfiguration(mm.getSettings());

        panel = new FullConfigurationPanel(project, config, mm.getSettings(), em.getFullSettings()) {

            @Override
            public void onImportFromEclipseClicked() {
                MidPointSettingsConfigurable.this.onImportFromEclipseClicked();
            }
        };

        return panel.createPanel();
    }

    @Override
    public void apply() {
        super.apply();

        Module module = panel.getModel().getMidpointModule();
        if (module == null) {
            return;
        }

        validateFacet(module);
    }

    private void onImportFromEclipseClicked() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            validateModule();

            Module module = MidPointUtils.guessMidpointModule(project);
            validateFacet(module);
        });
    }

    private void validateModule() {
        new MidPointModuleBuilder().createProjectFiles(project, project.getBaseDir());
    }

    private void validateFacet(Module module) {
        if (module == null) {
            return;
        }

        FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        FacetManager fm = FacetManager.getInstance(module);

        if (fm.getFacetByType(MidPointFacetType.FACET_TYPE_ID) == null) {
            fm.addFacet(facetType, facetType.getDefaultFacetName(), null);
        }
    }
}
