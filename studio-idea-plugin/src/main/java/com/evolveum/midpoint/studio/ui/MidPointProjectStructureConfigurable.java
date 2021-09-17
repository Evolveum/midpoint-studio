package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.ide.MidPointModuleBuilder;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.navigation.Place;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointProjectStructureConfigurable implements SearchableConfigurable, Place.Navigator,
        Configurable.NoMargin, Configurable.NoScroll {

    private Project project;

    private ProjectConfigurationPanel settings;

    public MidPointProjectStructureConfigurable(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String getId() {
        return "midpoint.project.structure";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "MidPoint";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel root = new BorderLayoutPanel();
        root.setBorder(JBUI.Borders.empty(5, 10, 0, 0));

        JLabel label = new JLabel("<html><body><b>MidPoint settings</b></body></html>", SwingConstants.LEFT);
        root.add(label, BorderLayout.NORTH);

        ProjectSettings pSettings = new ProjectSettings();

        MidPointService mm = MidPointService.getInstance(project);
        pSettings.setMidPointSettings(mm.getSettings());

        EnvironmentService em = EnvironmentService.getInstance(project);
        pSettings.setEnvironmentSettings(em.getFullSettings());

        settings = new ProjectConfigurationPanel(pSettings, true) {

            @Override
            public void importFromEclipsePerformed(ActionEvent evt) {
                MidPointProjectStructureConfigurable.this.importFromEclipsePerformed(evt);
            }

            @Override
            protected boolean isImportFromEclipseVisible() {
                return true;
            }
        };
        settings.setBorder(JBUI.Borders.emptyLeft(10));

        Wrapper wrapper = new Wrapper();
        wrapper.setContent(settings);
        root.add(wrapper, BorderLayout.CENTER);

        return root;
    }

    @Override
    public boolean isModified() {
        return settings != null && settings.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        if (settings == null) {
            return;
        }

        settings.validateData();
        settings.updateSettings();

        ProjectSettings pSettings = settings.getSettings();

        if (StringUtils.isNotEmpty(pSettings.getMasterPassword())) {
            try {
                EncryptionService.getInstance(project).changeMasterPassword(pSettings.getOldMasterPassword(), pSettings.getMasterPassword());
            } catch (Exception ex) {
                throw new ConfigurationException(ex.getMessage());
            }
        }

        MidPointService.getInstance(project).setSettings(pSettings.getMidPointSettings());
        EnvironmentService.getInstance(project).setSettings(pSettings.getEnvironmentSettings());

        settings.clearPasswords();
    }

    private void importFromEclipsePerformed(ActionEvent evt) {
        // check whether there's a module created and midpoint facet enabled
        ApplicationManager.getApplication().runWriteAction(() -> {
            validateModule();
            validateFacet();
        });
    }

    private void validateModule() {
        new MidPointModuleBuilder().createProjectFiles(project, project.getBaseDir());

        ModuleManager mm = ModuleManager.getInstance(project);
        Module[] modules = mm.getModules();

        if (modules == null || modules.length == 0) {
            return;
        }

        Module module = modules[0];
        if (MidPointModuleBuilder.MODULE_NAME.equals(module.getModuleTypeName())) {
            return;
        }

//        modules[0].setModuleType(MidPointModuleBuilder.MODULE_NAME);

//        MavenProjectsManager.getInstance(project).addManagedFiles(Arrays.asList(project.getBaseDir().findChild("pom.xml")));
    }

    private void validateFacet() {
        Module module = MidPointUtils.guessMidpointModule(project);
        if (module == null) {
            return;
        }

        FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        FacetManager fm = FacetManager.getInstance(module);

        if (fm.getFacetByType(MidPointFacetType.FACET_TYPE_ID) == null) {
            fm.addFacet(facetType, facetType.getDefaultFacetName(), null);
        }
    }

    @Override
    public ActionCallback navigateTo(@Nullable Place place, boolean requestFocus) {
        return new ActionCallback();
    }
}
