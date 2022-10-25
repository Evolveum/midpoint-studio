package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.intellij.openapi.diagnostic.Logger;
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

    public MidPointSettingsConfigurable(@NotNull Project project) {
        super("MidPoint 2", "","midpoint.project.structure");
        this.project = project;
    }

    @NotNull
    @Override
    public DialogPanel createPanel() {
        MidPointService mm = MidPointService.getInstance(project);
        EnvironmentService em = EnvironmentService.getInstance(project);

        GeneralConfigurationPanel panel = new GeneralConfigurationPanel(project, GeneralConfigurationKt.asGeneralConfiguration(mm.getSettings()), mm.getSettings(), em.getFullSettings()) {

            @Override
            public void onImportFromEclipseClicked() {
                LOG.info("AAAAAAAAAAAAAAA");
            }
        };

        return panel.createPanel();
    }
}
