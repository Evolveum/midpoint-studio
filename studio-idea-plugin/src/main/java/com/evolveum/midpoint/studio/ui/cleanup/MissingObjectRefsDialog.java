package com.evolveum.midpoint.studio.ui.cleanup;

import com.evolveum.midpoint.studio.impl.configuration.MissingRefObject;
import com.evolveum.midpoint.studio.ui.MissingRefObjectsEditor;
import com.evolveum.midpoint.studio.util.ActionUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MissingObjectRefsDialog extends DialogWrapper {

    private final List<MissingRefObject> objects;

    private final MissingRefObjectsEditor editor;

    private final DialogPanel panel;

    private Action saveAndDownloadAction;

    public MissingObjectRefsDialog(@Nullable Project project, @NotNull List<MissingRefObject> objects) {
        super(project);

        this.objects = objects;

        this.editor = new MissingRefObjectsEditor(project);
        editor.setObjects(objects);

        this.panel = editor.createComponent();

        setTitle("Missing references configuration");
        setOKButtonText("Save");
        setSize(800, 600);

        init();
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();

        saveAndDownloadAction = new DialogWrapperAction("Save and Download") {

            @Override
            protected void doAction(ActionEvent e) {
                saveAndDownloadPerformed(e);
            }
        };
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public @NotNull List<MissingRefObject> getData() {
        return editor.getObjects();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction(), saveAndDownloadAction, getCancelAction()};
    }

    private void saveAndDownloadPerformed(ActionEvent evt) {
        Project project = editor.getProject();

        // update settings
        List<MissingRefObject> result = getData();
        List<MissingRefKey> removed = MissingRefUtils.computeRemovedRefKeys(this.objects, result);

        MissingRefUtils.updateMissingRefSettings(project, result, removed);

        // download missing refs
        List<ObjectReferenceType> references = MissingRefUtils.computeDownloadOnly(project, result);
        ActionUtils.runDownloadTask(project, references, false);

        close(OK_EXIT_CODE);
    }
}
