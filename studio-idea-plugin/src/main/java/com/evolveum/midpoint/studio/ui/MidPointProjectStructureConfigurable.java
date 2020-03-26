package com.evolveum.midpoint.studio.ui;

import com.evolveum.midpoint.studio.impl.MidPointManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.ui.navigation.Place;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointProjectStructureConfigurable implements SearchableConfigurable, Place.Navigator,
        Configurable.NoMargin, Configurable.NoScroll {

    private Project project;

    private StructureConfigurableContext context;

    private MidPointSettingsPanel settings;

    public MidPointProjectStructureConfigurable(@NotNull Project project, @NotNull StructureConfigurableContext context) {
        this.project = project;
        this.context = context;
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
        root.setBorder(JBUI.Borders.empty(5,10,0,0));

        JLabel label = new JLabel("<html><body><b>MidPoint settings</b></body></html>", SwingConstants.LEFT);
        root.add(label, BorderLayout.NORTH);

        MidPointManager mm = MidPointManager.getInstance(project);

        settings = new MidPointSettingsPanel(mm.getSettings());
        settings.setBorder(JBUI.Borders.emptyLeft(10));

        Wrapper wrapper = new Wrapper();
        wrapper.setContent(settings);
        root.add(wrapper, BorderLayout.CENTER);

        return root;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public ActionCallback navigateTo(@Nullable Place place, boolean requestFocus) {
        return new ActionCallback();
    }
}
