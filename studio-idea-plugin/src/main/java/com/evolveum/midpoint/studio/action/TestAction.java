package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.search.JavaFilesSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TestAction extends AnAction {

    private boolean isPluginVersionRelease = true;

    public TestAction() {
        super("MidPoint Test Action");

        IdeaPluginDescriptor descriptor = PluginManager.getInstance()
                .findEnabledPlugin(PluginId.getId(MidPointConstants.PLUGIN_ID));
        if (descriptor == null) {
            return;
        }

        String version = descriptor.getVersion();

        // e.g. it's release like "4.4.0" and not snapshot "4.4.0-snapshot-250" or other non released version
        isPluginVersionRelease = !version.contains("-");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        if (!MidPointUtils.isVisibleWithMidPointFacet(e)) {
            SwingUtilities.invokeLater(() -> e.getPresentation().setVisible(false));
            return;
        }

        if (isPluginVersionRelease) {
            SwingUtilities.invokeLater(() -> e.getPresentation().setVisible(false));
            return;
        }

        boolean internal = ApplicationManager.getApplication().isInternal();
        SwingUtilities.invokeLater(() -> e.getPresentation().setVisible(internal));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();
        if (project == null) {
            return;
        }

        SearchScope scope = new JavaFilesSearchScope(project);

        AllClassesSearch.SearchParameters params = new AllClassesSearch.SearchParameters(scope, project);
        PsiClass[] classes = AllClassesSearch.INSTANCE
                .createQuery(params)
                .filtering(c -> c.getAnnotation("com.evolveum.midpoint.web.application.PanelType") != null)
                .toArray(new PsiClass[0]);

        List<String> panelNamesQuoted = Arrays.stream(classes)
                .map(p -> p.getAnnotation("com.evolveum.midpoint.web.application.PanelType"))
                .map(a -> a.findAttributeValue("name").getText())
                .sorted()
                .collect(Collectors.toList());

        System.out.println("CLASSES>>> " + classes.length);
    }
}
