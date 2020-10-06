package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class BaseObjectsAction extends BackgroundAction {

    private static final Logger LOG = Logger.getInstance(BaseObjectsAction.class);

    private final String notificationKey;

    private final String operation;

    public BaseObjectsAction(String taskTitle, String notificationKey, String operation) {
        super(taskTitle);

        this.notificationKey = notificationKey;
        this.operation = operation;
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        MidPointService mm = MidPointService.getInstance(e.getProject());
        mm.printToConsole(getClass(), "Initializing " + operation + " action");

        LOG.debug("Setting up MidPoint client");

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
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
                ProcessState state = processText(e, mm, indicator, client, text, e.getDataContext().getData(PlatformDataKeys.VIRTUAL_FILE));

                showNotificationAfterFinish(0, 0, state.success, state.fail);
            } else {
                MidPointUtils.publishNotification(notificationKey, "Error", "Text is empty", NotificationType.ERROR);
            }

            return;
        }

        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.publishNotification(notificationKey, getTaskTitle(),
                    "No files selected for " + operation, NotificationType.WARNING);
            return;
        }

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(notificationKey, getTaskTitle(),
                    "No files matched for " + operation + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(e, mm, indicator, client, toProcess);
    }

    private void showNotificationAfterFinish(int filesCount, int failedFilesCount, int successObjects, int failedObjects) {
        NotificationType type;
        String title;
        StringBuilder sb = new StringBuilder();

        if (failedFilesCount == 0 && failedObjects == 0 && successObjects > 0) {
            type = NotificationType.INFORMATION;
            title = "Success";

            sb.append(getTaskTitle() + " finished.");
        } else {
            type = NotificationType.WARNING;
            title = "Warning";

            sb.append("There were problems during '" + getTaskTitle() + "'");
        }

        sb.append("<br/>");
        sb.append("Processed: ").append(successObjects).append(" objects<br/>");
        sb.append("Failed to process: ").append(failedObjects).append(" objects <br/>");
        sb.append("Files processed: ").append(filesCount).append("<br/>");
        sb.append("Failed to process: ").append(failedFilesCount).append(" files<br/>");

        MidPointUtils.publishNotification(notificationKey, title, sb.toString(), type);
    }

    private void publishException(MidPointService mm, String msg, Exception ex) {
        mm.printToConsole(getClass(), msg + ". Reason: " + ex.getMessage());

        MidPointUtils.publishExceptionNotification(notificationKey, msg, ex);
    }

    private void processFiles(AnActionEvent evt, MidPointService mm, ProgressIndicator indicator, MidPointClient client, List<VirtualFile> files) {
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        int filesCount = 0;
        AtomicInteger failedFilesCount = new AtomicInteger(0);

        for (VirtualFile file : files) {
            if (isCanceled()) {
                break;
            }

            filesCount++;

            RunnableUtils.runWriteActionAndWait(() -> {
                try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                    String xml = IOUtils.toString(in);

                    ProcessState state = processText(evt, mm, indicator, client, xml, file);
                    success.addAndGet(state.success);
                    fail.addAndGet(state.fail);
                } catch (IOException ex) {
                    failedFilesCount.incrementAndGet();
                    publishException(mm, "Exception occurred when loading file '" + file.getName() + "'", ex);
                }
            });
        }

        showNotificationAfterFinish(filesCount, failedFilesCount.get(), success.get(), fail.get());
    }

    private ProcessState processText(AnActionEvent evt, MidPointService mm, ProgressIndicator indicator, MidPointClient client, String text, VirtualFile file) {
        indicator.setIndeterminate(false);

        ProcessState state = new ProcessState();

        try {
            List<MidPointObject> objects = MidPointObjectUtils.parseText(text, notificationKey);

            int i = 0;
            for (MidPointObject obj : objects) {
                obj.setFile(file);

                i++;

                indicator.setFraction(i / objects.size());

                try {
                    ProcessObjectResult processResult = processObject(evt, client, obj);
                    if (processResult.problem()) {
                        state.fail++;
                    } else {
                        state.success++;
                    }

                    if (!processResult.shouldContinue()) {
                        break;
                    }
                } catch (Exception ex) {
                    state.fail++;

                    publishException(mm, "Exception occurred during " + operation + " of '" + obj.getName() + "(" + obj.getOid() + ")'", ex);
                }
            }
        } catch (Exception ex) {
            state.fail++;

            publishException(mm, "Exception occurred during " + operation, ex);
        }

        return state;
    }

    public abstract <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception;

    protected ProcessObjectResult validateOperationResult(AnActionEvent evt, OperationResult result, String operation, String objectName) {
        boolean problem = result != null && !result.isSuccess();
        if (problem) {
            printAndNotifyProblem(evt.getProject(), operation, objectName, result, null);
        } else {
            printSuccess(evt.getProject(), operation, objectName);
        }

        return new ProcessObjectResult(result).problem(problem);
    }

    protected void printProblem(Project project, String message) {
        MidPointService mm = MidPointService.getInstance(project);

        mm.printToConsole(getClass(), message);
    }

    protected void printAndNotifyProblem(Project project, String operation, String objectName, OperationResult result, Exception ex) {
        MidPointService mm = MidPointService.getInstance(project);

        String msg = StringUtils.capitalize(operation) + " status of " + objectName + " was " + result.getStatus();
        mm.printToConsole(getClass(), msg);

        MidPointUtils.publishNotification(notificationKey, "Warning", msg,
                NotificationType.WARNING, new ShowResultNotificationAction(result));

        if (ex != null) {
            publishException(mm, "Exception occurred during " + operation + " of " + objectName, ex);
        }
    }

    protected void printSuccess(Project project, String operation, String objectName) {
        MidPointService mm = MidPointService.getInstance(project);

        mm.printToConsole(getClass(), StringUtils.capitalize(operation) + " '" + objectName + "' finished");
    }

    protected String getOperation() {
        return operation;
    }

    private static class ProcessState {

        private int success;

        private int fail;
    }
}
