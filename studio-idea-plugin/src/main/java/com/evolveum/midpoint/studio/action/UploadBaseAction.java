package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class UploadBaseAction extends BackgroundAction {

    public static final String NOTIFICATION_KEY = "Upload Action";

    private static final Logger LOG = Logger.getInstance(UploadBaseAction.class);

    public UploadBaseAction() {
        super("Uploading objects");
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        LOG.debug("Setting up MidPoint client");

        EnvironmentManager em = EnvironmentManager.getInstance(e.getProject());
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(e.getProject(), env);

        LOG.debug("MidPoint client setup done");

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            String text = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {

                String txt = editor.getSelectionModel().getSelectedText();
                if (StringUtils.isNotEmpty(txt)) {
                    return txt;
                }

                return editor.getDocument().getText();
            });

            if (!StringUtils.isEmpty(text)) {
                execute(indicator, client, text);
            } else {
                MidPointUtils.publishNotification(NOTIFICATION_KEY, "Error", "Text is empty", NotificationType.ERROR);
            }

            return;
        }

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction((Computable<VirtualFile[]>) () -> e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Upload",
                    "No files selected for upload", NotificationType.WARNING);
            return;
        }

        List<VirtualFile> toUpload = new ArrayList<>();
        for (VirtualFile selected : selectedFiles) {
            if (isCanceled()) {
                return;
            }

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
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Upload",
                    "No files matched for upload (xml)", NotificationType.WARNING);
            return;
        }

        execute(indicator, client, toUpload);
    }

    protected UploadOptions buildAddOptions() {
        return new UploadOptions().overwrite(true);
    }

    private void execute(ProgressIndicator indicator, MidPointClient client, List<VirtualFile> files) {
        for (VirtualFile file : files) {
            if (isCanceled()) {
                break;
            }

            ApplicationManager.getApplication().invokeAndWait(() ->
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                            String xml = IOUtils.toString(in);

                            execute(indicator, client, xml);
                        } catch (IOException ex) {
                            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Exception occurred when loading file " + file.getName(), ex);
                        }
                    }));
        }
    }

    private void execute(ProgressIndicator indicator, MidPointClient client, String text) {
        try {
            List<PrismObject<?>> objects = client.parseObjects(text);

            int i = 0;
            for (PrismObject obj : objects) {
                i++;

                indicator.setFraction(i / objects.size());
                try {
                    UploadResponse resp = client.upload(obj, buildAddOptions());
                    OperationResult result = resp.getResult();
                    if (result != null && !result.isSuccess()) {
                        MidPointUtils.publishNotification(NOTIFICATION_KEY, "Warning",
                                "Upload status of " + obj.getName() + "(" + obj.getOid() + ") was " + result.getStatus(),
                                NotificationType.WARNING, new ShowResultNotificationAction(result));
                    }
                } catch (Exception ex) {
                    MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY,
                            "Exception occurred during upload of " + obj.getName() + "(" + obj.getOid() + ")", ex);
                }
            }
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Exception occurred during upload", ex);
        }
    }
}
