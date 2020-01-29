package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.RestObjectManager;
import com.evolveum.midpoint.studio.impl.UploadOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class UploadBaseAction extends BackgroundAction {

    public UploadBaseAction() {
        super("Uploading objects");
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            String selected = editor.getSelectionModel().getSelectedText();
            if (StringUtils.isNotEmpty(selected)) {
                execute(e, indicator, selected);
                return;
            } else {
                execute(e, indicator, editor.getDocument().getText());
            }
        }

        VirtualFile[] selectedFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.createAndPushNotification("Upload", "Upload",
                    "No files selected for upload", NotificationType.WARNING);
            return;
        }

        List<VirtualFile> toUpload = new ArrayList<>();
        for (VirtualFile selected : selectedFiles) {
            if (selected.isDirectory()) {
                VfsUtilCore.iterateChildrenRecursively(
                        selected,
                        file -> "xml".equalsIgnoreCase(file.getExtension()),
                        file -> {
                            toUpload.add(file);
                            return true;
                        });
            } else if ("xml".equalsIgnoreCase(selected.getExtension())) {
                toUpload.add(selected);
            }
        }

        if (toUpload.isEmpty()) {
            MidPointUtils.createAndPushNotification("Upload", "Upload",
                    "No files matched for upload (xml)", NotificationType.WARNING);
            return;
        }

        execute(e, indicator, toUpload);
    }

    protected UploadOptions buildAddOptions() {
        return new UploadOptions().overwrite(true);
    }

    private void execute(AnActionEvent evt, ProgressIndicator indicator, List<VirtualFile> files) {


        for (VirtualFile file : files) {

        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                RestObjectManager rest = RestObjectManager.getInstance(evt.getProject());
                rest.upload(files, buildAddOptions());


// todo       String status = result.isSuccess() ? "SUCCESS" : result.dump(true);
            } catch (Exception ex) {
                ex.printStackTrace(); // todo implement
            }
        });
    }

    private void execute(AnActionEvent evt, ProgressIndicator indicator, String text) {
        Environment env = null;
        if (indicator != null) {
            indicator.setText("Uploading text to environment " + env);
        }


        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                RestObjectManager rest = RestObjectManager.getInstance(evt.getProject());
                rest.upload(text, buildAddOptions());


//  todo      String status = result.isSuccess() ? "SUCCESS" : result.dump(true);
            } catch (Exception ex) {
                ex.printStackTrace(); // todo implement
            }
        });
    }

    protected void execute(AnActionEvent evt, String text) {

    }
}
