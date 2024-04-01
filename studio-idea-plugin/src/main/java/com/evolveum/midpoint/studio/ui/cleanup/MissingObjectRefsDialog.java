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
import java.util.List;

public class MissingObjectRefsDialog extends DialogWrapper {

    private final MissingRefObjectsEditor editor;

    private final DialogPanel panel;

    public MissingObjectRefsDialog(@Nullable Project project, @NotNull List<MissingRefObject> objects) {
        super(project);

        editor = new MissingRefObjectsEditor(project, objects);
        panel = editor.createComponent();

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    public @NotNull List<MissingRefObject> getData() {
        return List.of(); // todo implement
    }



    // todo save (ok action) and create (save & download) action

//    List<ObjectReferenceType> references = createRefsForDownload(dialog.getData());
//        if (references.isEmpty()) {
//        return;
//    }
//
//        ActionUtils.runDownloadTask(project, references, false);
}
