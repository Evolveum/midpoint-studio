package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

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
        MidPointManager mm = MidPointManager.getInstance(e.getProject());
        mm.printToConsole(getClass(), "Initializing upload action");

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
                int problemCount = execute(mm, indicator, client, text);
                if (problemCount != 0) {
                    showNotificationAfterFinish(problemCount);
                }
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

        execute(mm, indicator, client, toUpload);
    }

    protected UploadOptions buildAddOptions() {
        return new UploadOptions().overwrite(true);
    }

    private void execute(MidPointManager mm, ProgressIndicator indicator, MidPointClient client, List<VirtualFile> files) {
        AtomicInteger count = new AtomicInteger(0);

        for (VirtualFile file : files) {
            if (isCanceled()) {
                break;
            }

            ApplicationManager.getApplication().invokeAndWait(() ->
                    RunnableUtils.runWriteAction(() -> {
                        try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                            String xml = IOUtils.toString(in);

                            int problems = execute(mm, indicator, client, xml);
                            count.addAndGet(problems);
                        } catch (IOException ex) {
                            publishException(mm, "Exception occurred when loading file " + file.getName(), ex);
                        }
                    }));
        }

        if (count.get() > 0) {
            showNotificationAfterFinish(count.get());
        }
    }

    private int execute(MidPointManager mm, ProgressIndicator indicator, MidPointClient client, String text) {
        int problemCount = 0;

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
                        problemCount++;


                        String msg = "Upload status of " + obj.getName() + "(" + obj.getOid() + ") was " + result.getStatus();
                        mm.printToConsole(getClass(), msg);

                        MidPointUtils.publishNotification(NOTIFICATION_KEY, "Warning", msg,
                                NotificationType.WARNING, new ShowResultNotificationAction(result));
                    } else {
                        mm.printToConsole(getClass(), "Uploaded object " + obj.getName());
                    }
                } catch (Exception ex) {
                    problemCount++;

                    publishException(mm, "Exception occurred during upload of " + obj.getName() + "(" + obj.getOid() + ")", ex);
                }
            }
        } catch (Exception ex) {
            problemCount++;

            publishException(mm, "Exception occurred during upload", ex);
        }

        return problemCount;
    }

    private void publishException(MidPointManager mm, String msg, Exception ex) {
        mm.printToConsole(getClass(), msg + ". Reason: " + ex.getMessage());

        MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, msg, ex);
    }

    private void showNotificationAfterFinish(int problemCount) {
        if (problemCount == 0) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Success", "Upload finished", NotificationType.INFORMATION);
        } else {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Warning",
                    "There were " + problemCount + " problems during upload", NotificationType.WARNING);
        }
    }
}
