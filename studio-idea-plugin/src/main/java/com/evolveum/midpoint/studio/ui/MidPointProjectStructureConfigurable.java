package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.impl.ide.MidPointModuleBuilder;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.ModuleManagerEx;
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
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
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

        MidPointManager mm = MidPointManager.getInstance(project);
        pSettings.setMidPointSettings(mm.getSettings());

        EnvironmentManager em = EnvironmentManager.getInstance(project);
        pSettings.setEnvironmentSettings(em.getFullSettings());

        settings = new ProjectConfigurationPanel(pSettings, true);
        settings.setBorder(JBUI.Borders.emptyLeft(10));

        Wrapper wrapper = new Wrapper();
        wrapper.setContent(settings);
        root.add(wrapper, BorderLayout.CENTER);

        return root;
    }

    @Override
    public boolean isModified() {
        if (settings == null || settings.getSettings() == null) {
            return false;
        }

        ProjectSettings pSettings = new ProjectSettings();

        MidPointManager mm = MidPointManager.getInstance(project);
        pSettings.setMidPointSettings(mm.getSettings());

        EnvironmentManager em = EnvironmentManager.getInstance(project);
        pSettings.setEnvironmentSettings(em.getFullSettings());

        return !Objects.equals(pSettings, settings.getSettings());
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
                CredentialsManager.getInstance(project).changeMasterPassword(pSettings.getOldMasterPassword(), pSettings.getMasterPassword());
            } catch (Exception ex) {
                throw new ConfigurationException(ex.getMessage());
            }
        }

        MidPointManager.getInstance(project).setSettings(pSettings.getMidPointSettings());
        EnvironmentManager.getInstance(project).setSettings(pSettings.getEnvironmentSettings());

        settings.clearPasswords();

        // check whether there's a module created and midpoint facet enabled
//        ApplicationManager.getApplication().runWriteAction(() -> {
//            validateModule();
//            validateFacet();
//        });
    }

    private void validateModule() {
        ModuleManagerEx mm = ModuleManagerEx.getInstanceEx(project);
        Module[] modules = mm.getModules();

        if (modules == null || modules.length == 0) {
            return;
        }

        Module module = modules[0];
        if (MidPointModuleBuilder.MODULE_NAME.equals(module.getModuleTypeName())) {
            return;
        }

        new MidPointModuleBuilder().createProjectFiles(project, project.getBaseDir());
        
//        modules[0].setModuleType(MidPointModuleBuilder.MODULE_NAME);

//        MavenProjectsManager.getInstance(project).addManagedFiles(Arrays.asList(project.getBaseDir().findChild("pom.xml")));
    }

    private void validateFacet() {
        FacetType facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        ModuleManagerEx mm = ModuleManagerEx.getInstanceEx(project);
        Module[] modules = mm.getModules();
        if (modules == null || modules.length == 0) {
            return;
        }
        FacetManager fm = FacetManager.getInstance(modules[0]);
        if (fm.getFacetByType(MidPointFacetType.FACET_TYPE_ID) == null) {
            fm.addFacet(facetType, facetType.getDefaultFacetName(), null);
        }
    }

    @Override
    public ActionCallback navigateTo(@Nullable Place place, boolean requestFocus) {
        return new ActionCallback();
    }
}
