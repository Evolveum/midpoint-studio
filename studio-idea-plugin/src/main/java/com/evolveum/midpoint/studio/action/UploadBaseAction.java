package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentManager;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.UploadOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
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
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Upload",
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
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Upload",
                    "No files matched for upload (xml)", NotificationType.WARNING);
            return;
        }

        execute(e, indicator, toUpload);
    }

    protected UploadOptions buildAddOptions() {
        return new UploadOptions().overwrite(true);
    }

    private void execute(AnActionEvent evt, ProgressIndicator indicator, List<VirtualFile> files) {
        EnvironmentManager em = EnvironmentManager.getInstance(evt.getProject());
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(evt.getProject(), env);

        for (VirtualFile file : files) {
            ApplicationManager.getApplication().invokeAndWait(() ->
                    ApplicationManager.getApplication().runWriteAction(() -> {
                        try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                            String xml = IOUtils.toString(in);

                            execute(indicator, client, xml);
                        } catch (IOException ex) {
                            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Error",
                                    "Couldn't read file " + file.getName() + ", reason: " + ex.getMessage(), NotificationType.ERROR);

                            LOG.debug("Exception occurred when loading file {}, reason: {}", file.getName(), ex.getMessage());
                        }
                    }));
        }
    }

    private void execute(AnActionEvent evt, ProgressIndicator indicator, String text) {
        EnvironmentManager em = EnvironmentManager.getInstance(evt.getProject());
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(evt.getProject(), env);

        execute(indicator, client, text);
    }

    private void execute(ProgressIndicator indicator, MidPointClient client, String text) {

    }
}
