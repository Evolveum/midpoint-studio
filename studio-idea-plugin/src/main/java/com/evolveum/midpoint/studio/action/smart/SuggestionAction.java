package com.evolveum.midpoint.studio.action.smart;

import com.evolveum.midpoint.studio.ui.smart.suggestion.DialogWindowActionHandler;
import com.evolveum.midpoint.studio.ui.smart.suggestion.DialogWindowWrapper;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Dominik.
 */
public class SuggestionAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        var presentation = e.getPresentation();
        presentation.setEnabled(isResourceObject(e.getData(CommonDataKeys.PSI_FILE)));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        new DialogWindowWrapper(
                anActionEvent.getProject(),
                "Allow AI to analyze your resource data?",
                createResourceFormPanel(),
                new DialogWindowActionHandler() {
                    @Override
                    public String getOkButtonTitle() {
                        return "Allow and continue";
                    }

                    @Override
                    public void onOk() {
                        System.out.println("OK clicked: ");
                    }

                    @Override
                    public void onCancel() {
                        System.out.println("Dialog canceled");
                    }
                }
        ).show();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    // method checks it if the content of a file is a resource object (for XML, JSON, YAML file types)
    private boolean isResourceObject(PsiFile psiFile) {
        if (psiFile instanceof XmlFile xmlFile) {
            XmlTag rootTag = xmlFile.getRootTag();
            return rootTag != null && "resource".equals(rootTag.getName());
        } else if (psiFile instanceof JsonFile jsonFile) {
            JsonObject jsonObject = (JsonObject) jsonFile.getTopLevelValue();
            if (jsonObject == null || jsonObject.getPropertyList().isEmpty()) return false;
            JsonProperty first = jsonObject.getPropertyList().get(0);
            return "resource".equals(first.getName());
        }

        return false;
    }

    private JPanel createResourceFormPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel("Resource:"));
        panel.add(new JTextField(50));
        panel.add(new JLabel("Object class:"));
        panel.add(new JTextField(50));
        return panel;
    }

}
