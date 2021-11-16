package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class BackgroundableTask<S extends TaskState> extends Task.Backgroundable {

    private static final Logger LOG = Logger.getInstance(BackgroundableTask.class);

    private String notificationKey;

    protected AnActionEvent event;

    protected S state = createNewState();

    protected MidPointService midPointService;

    public BackgroundableTask(@NotNull Project project, @NotNull String title, @NotNull String notificationKey) {
        super(project, title, true);

        this.midPointService = MidPointService.getInstance(project);

        this.notificationKey = notificationKey;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        LOG.info("Starting task: " + getClass().getName());

        new RunnableUtils.PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                doRun(indicator);
            }
        }.run();
    }

    public void setEvent(AnActionEvent event) {
        this.event = event;
    }

    public boolean showConfirmationDialog() {
        return true;
    }

    protected String getConfirmationMessage(int filesCount) {
        String files = filesCount > 1 ? "files" : "file";
        return "Do you want to process " + filesCount + " " + files + "?";
    }

    protected String getConfirmationYesActionText() {
        return "Confirm";
    }

    private int showConfirmationDialog(int filesCount) {
        AtomicInteger result = new AtomicInteger(0);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            Component comp = null;
            if (event != null) {
                InputEvent inputEvent = event.getInputEvent();
                comp = inputEvent != null ? inputEvent.getComponent() : null;
            }

            JComponent source;
            if (comp instanceof JComponent) {
                source = (JComponent) comp;
            } else {
                if (comp instanceof JWindow) {
                    JWindow w = (JWindow) comp;
                    source = w.getRootPane();
                } else {
                    JFrame f = (JFrame) WindowManager.getInstance().suggestParentWindow(getProject());
                    source = f.getRootPane();
                }
            }

            int r = Messages.showConfirmationDialog(source, getConfirmationMessage(filesCount), "Confirm action",
                    getConfirmationYesActionText(), "Cancel");

            result.set(r);
        });

        return result.get();
    }

    protected S createNewState() {
        return (S) new TaskState();
    }

    private void doRun(ProgressIndicator indicator) {
        if (getProject() == null) {
            return;
        }

        Editor editor = event != null ? event.getData(PlatformDataKeys.EDITOR) : null;
        if (editor != null) {
            processEditor(indicator, editor);

            showNotificationAfterFinish(false);
            return;
        }

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (showConfirmationDialog()) {
            int result = showConfirmationDialog(toProcess.size());

            if (result == Messages.NO) {
                return;
            }
        }

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(getProject(), notificationKey, getTitle(),
                    "No files matched for " + getTitle() + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(indicator, toProcess);

        showNotificationAfterFinish(false);
    }

    private void processEditor(ProgressIndicator indicator, Editor editor) {
        String text = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {

            String txt = editor.getSelectionModel().getSelectedText();
            if (StringUtils.isNotEmpty(txt)) {
                return txt;
            }

            return editor.getDocument().getText();
        });

        if (!StringUtils.isEmpty(text)) {
            processEditorText(indicator, editor, text, event.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE));
        } else {
            MidPointUtils.publishNotification(getProject(), notificationKey, "Error", "Text is empty", NotificationType.ERROR);
        }
    }

    private void processFiles(ProgressIndicator indicator, List<VirtualFile> files) {
        indicator.setIndeterminate(false);

        int current = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / files.size());

            processFile(file);
        }
    }

    private boolean hasFailures() {
        return state.getFailed() > 0 || state.getSkipped() > 0;
    }

    protected List<MidPointObject> loadObjectsFromFile(VirtualFile file) {
        List<MidPointObject> objects = new ArrayList<>();

        RunnableUtils.runWriteActionAndWait(() -> {
            MidPointUtils.forceSaveAndRefresh(getProject(), file);

            List<MidPointObject> obj = MidPointUtils.parseProjectFile(getProject(), file, notificationKey);
            obj = ClientUtils.filterObjectTypeOnly(obj);

            objects.addAll(obj);
        });

        return objects;
    }

    protected void processEditorText(ProgressIndicator indicator, Editor editor, String text, VirtualFile sourceFile) {

    }

    protected void processFile(VirtualFile file) {

    }

    protected void updateEditor(Editor editor, String text) {
        boolean updateSelection = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {

            String txt = editor.getSelectionModel().getSelectedText();
            return StringUtils.isNotEmpty(txt);
        });

        com.intellij.openapi.editor.Document document = editor.getDocument();
        if (updateSelection) {
            SelectionModel model = editor.getSelectionModel();
            int start = model.getSelectionStart();
            int end = model.getSelectionEnd();

            WriteCommandAction.runWriteCommandAction(getProject(), () ->
                    document.replaceString(start, end, text)
            );
        } else {
            WriteCommandAction.runWriteCommandAction(getProject(), () ->
                    document.replaceString(0, document.getTextLength(), text)
            );
        }
    }

    protected void writeObjectsToFile(VirtualFile file, List<String> objects) {
        RunnableUtils.runWriteActionAndWait(() -> {
            try (Writer writer = new OutputStreamWriter(file.getOutputStream(this), file.getCharset())) {
                if (objects.size() > 1) {
                    writer.write(ClientUtils.OBJECTS_XML_PREFIX);
                    writer.write('\n');
                }

                for (String obj : objects) {
                    writer.write(obj);
                }

                if (objects.size() > 1) {
                    writer.write(ClientUtils.OBJECTS_XML_SUFFIX);
                    writer.write('\n');
                }
            } catch (IOException ex) {
                state.incrementFailed();

                midPointService.printToConsole(null, getClass(), "Failed to write objects to file " + file.getPath(), ex);
            }
        });
    }

    @Override
    public void onCancel() {
        LOG.info(getTitle() + " was cancelled");

        showNotificationAfterFinish(true);
    }

    protected void showNotificationAfterFinish(boolean canceled) {
        NotificationType type;
        String title;
        StringBuilder msg = new StringBuilder();

        if (canceled) {
            type = NotificationType.WARNING;
            title = getTitle() + "canceled";

            msg.append("Processed before cancel requested.");
        } else if (hasFailures()) {
            type = NotificationType.WARNING;
            title = getTitle();

            msg.append("There were problems during '" + getTitle() + "'.");
        } else {
            type = NotificationType.INFORMATION;
            title = getTitle();

            msg.append(getTitle() + " finished.");
        }

        msg.append("<br/>");
        msg.append("Processed: ").append(state.getProcessed()).append(" objects<br/>");
        msg.append("Failed to process: ").append(state.getFailed()).append(" objects<br/>");

        MidPointUtils.publishNotification(getProject(), notificationKey, title, msg.toString(), type);
    }
}
