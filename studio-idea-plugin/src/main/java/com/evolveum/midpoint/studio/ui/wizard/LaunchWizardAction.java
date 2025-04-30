package com.evolveum.midpoint.studio.ui.wizard;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Dominik.
 */
public class LaunchWizardAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        WizardDialog wizardDialog = new WizardDialog();
        wizardDialog.show();
    }
//    @Override
//    public void actionPerformed(@NotNull AnActionEvent e) {
//        ExampleOneWizardComponent oneWizardComponent = new ExampleOneWizardComponent();
//        if (oneWizardComponent.showAndGet()) {
//            ExampleTwoWizardComponent twoWizardComponent = new ExampleTwoWizardComponent();
//            if (twoWizardComponent.showAndGet()) {
//                ExampleThreeWizardComponent threeWizardComponent = new ExampleThreeWizardComponent();
//                threeWizardComponent.showAndGet();
//            }
//        }
//    }
}
