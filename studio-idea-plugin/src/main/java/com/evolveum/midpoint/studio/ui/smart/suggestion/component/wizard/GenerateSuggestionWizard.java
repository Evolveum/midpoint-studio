/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard;

import com.evolveum.midpoint.studio.impl.LocalizationService;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step.PermissionStep;
import com.evolveum.midpoint.studio.ui.smart.suggestion.component.wizard.step.SmartSuggestionStep;
import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class GenerateSuggestionWizard extends AbstractWizard<Step> {

    private final GenerateSuggestionDataModel dataModel;
    private final Runnable onFinish;

    private final LocalizationService localizationService;

    public GenerateSuggestionWizard(
            Project project,
            String title,
            LocalizationService localizationService,
            GenerateSuggestionDataModel dataModel,
            Runnable onFinish
    ) {
        super(title, project);

        this.localizationService = localizationService;
        this.dataModel = dataModel;
        this.onFinish = onFinish;

        addStep(new SmartSuggestionStep(this, dataModel, localizationService));
        addStep(new PermissionStep(this, dataModel, localizationService));

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
            getNextButton().setText(localizationService.translate("SmartSuggestConfirmationPanel.allowAndContinue"));
        } else {
            getNextButton().setText(localizationService.translate("ResourceGeneratingSuggestionObjectClassWizardPanel.continue"));
        }
    }

    public void setEnabledNextButton(boolean enabled) {
        getNextButton().setEnabled(enabled);
    }

    @Override
    protected boolean canGoNext() {
        return dataModel.getResourceOid() != null && dataModel.getObjectClass() != null;
    }
}
