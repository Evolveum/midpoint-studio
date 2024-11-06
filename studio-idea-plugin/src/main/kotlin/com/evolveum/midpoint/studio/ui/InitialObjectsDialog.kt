package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.studio.util.StudioLocalization.message
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

class InitialObjectsDialog(val project: Project) {

    private fun createPanel(): DialogPanel {
        return panel {
            row(message("InitialObjectsDialog.currentVanillaObjects")) {
//                textFieldWithBrowseButton(
//                    message("InitialObjectsDialog.selectDirectory"),
//                    project,
//                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
//                )
//                    .columns(COLUMNS_LARGE)

            }
                .comment(message("InitialObjectsDialog.currentVanillaObjects.comment"))
            row(message("InitialObjectsDialog.newVanillaObjects")) {
//                textFieldWithBrowseButton(
//                    message("InitialObjectsDialog.selectDirectory"),
//                    project,
//                    FileChooserDescriptorFactory.createSingleFolderDescriptor()
//                )
//                    .columns(COLUMNS_LARGE)

            }
                .comment(message("InitialObjectsDialog.newVanillaObjects.comment"))
        }
    }
}