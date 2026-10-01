/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard;

import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step.PermissionStep;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step.SmartSuggestionStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GenerateSuggestionWizard extends AbstractWizard<Step> {

    private final GenerateSuggestionDataModel dataModel;
    private final Runnable onFinish;

    public GenerateSuggestionWizard(
            Project project,
            String title,
            GenerateSuggestionDataModel dataModel,
            Runnable onFinish
    ) {
        super(title, project);

        this.dataModel = dataModel;
        this.onFinish = onFinish;

        addStep(new SmartSuggestionStep(this, dataModel));
        addStep(new PermissionStep(dataModel));

        getHelpButton().setVisible(false);
        setSize(800, 600);
        init();
    }

    @Override
    protected void doOKAction() {
        onFinish.run();
        super.doOKAction();
    }

    @Override
    protected @Nullable @NonNls String getHelpID() {
        return "";
    }

    @Override
    protected void updateStep() {
        super.updateStep();

        if (mySteps.get(getCurrentStep()) instanceof PermissionStep) {
            getNextButton().setText("Allow and continue");
        } else {
            getNextButton().setText("Continue");
        }
    }

    public void setEnabledNextButton(boolean enabled) {
        getNextButton().setEnabled(enabled);
    }

    @Override
    protected boolean canGoNext() {
        return dataModel.getResourceOid() != null && dataModel.getObjectClass() != null;
    }


    public static final class Icons {

        public static final Icon Chip =
                IconLoader.getIcon("/icons/chip.svg", Icons.class);
    }
}
