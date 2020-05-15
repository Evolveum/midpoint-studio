package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismObject;
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
public abstract class BaseObjectsAction extends BackgroundAction {

    private static final Logger LOG = Logger.getInstance(BaseObjectsAction.class);

    private static final String XML_EXTENSION = "xml";

    private final String notificationKey;

    private final String operation;

    public BaseObjectsAction(String taskTitle, String notificationKey, String operation) {
        super(taskTitle);

        this.notificationKey = notificationKey;
        this.operation = operation;
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        MidPointManager mm = MidPointManager.getInstance(e.getProject());
        mm.printToConsole(getClass(), "Initializing " + operation + " action");

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
                int problemCount = processText(e, mm, indicator, client, text);
                if (problemCount != 0) {
                    showNotificationAfterFinish(problemCount);
                }
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

        List<VirtualFile> toProcess = new ArrayList<>();
        for (VirtualFile selected : selectedFiles) {
            if (isCanceled()) {
                return;
            }

            if (selected.isDirectory()) {
                VfsUtilCore.iterateChildrenRecursively(
                        selected,
                        file -> XML_EXTENSION.equalsIgnoreCase(file.getExtension()),
                        file -> {
                            toProcess.add(file);
                            return true;
                        });
            } else if (XML_EXTENSION.equalsIgnoreCase(selected.getExtension())) {
                toProcess.add(selected);
            }
        }

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(notificationKey, getTaskTitle(),
                    "No files matched for " + operation + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(e, mm, indicator, client, toProcess);
    }

    private void showNotificationAfterFinish(int problemCount) {
        if (problemCount == 0) {
            MidPointUtils.publishNotification(notificationKey, "Success", getTaskTitle() + " finished", NotificationType.INFORMATION);
        } else {
            MidPointUtils.publishNotification(notificationKey, "Warning",
                    "There were " + problemCount + " problems during '" + getTaskTitle() + "'", NotificationType.WARNING);
        }
    }

    private void publishException(MidPointManager mm, String msg, Exception ex) {
        mm.printToConsole(getClass(), msg + ". Reason: " + ex.getMessage());

        MidPointUtils.publishExceptionNotification(notificationKey, msg, ex);
    }

    private void processFiles(AnActionEvent evt, MidPointManager mm, ProgressIndicator indicator, MidPointClient client, List<VirtualFile> files) {
        AtomicInteger count = new AtomicInteger(0);

        for (VirtualFile file : files) {
            if (isCanceled()) {
                break;
            }

            ApplicationManager.getApplication().invokeAndWait(() ->
                    RunnableUtils.runWriteAction(() -> {
                        try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                            String xml = IOUtils.toString(in);

                            int problems = processText(evt, mm, indicator, client, xml);
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

    private int processText(AnActionEvent evt, MidPointManager mm, ProgressIndicator indicator, MidPointClient client, String text) {
        indicator.setIndeterminate(false);

        int problemCount = 0;

        try {
            List<PrismObject<?>> objects = client.parseObjects(text);

            int i = 0;
            for (PrismObject obj : objects) {
                i++;

                indicator.setFraction(i / objects.size());

                try {
                    ProcessObjectResult processResult = processObject(evt, client, obj);
                    if (processResult.problem()) {
                        problemCount++;
                    }

                    if (!processResult.shouldContinue()) {
                        break;
                    }
                } catch (Exception ex) {
                    problemCount++;

                    publishException(mm, "Exception occurred during " + operation + " of " + obj.getName() + "(" + obj.getOid() + ")", ex);
                }
            }
        } catch (Exception ex) {
            problemCount++;

            publishException(mm, "Exception occurred during " + operation, ex);
        }

        return problemCount;
    }

    public abstract <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, PrismObject<O> obj) throws Exception;

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
        MidPointManager mm = MidPointManager.getInstance(project);

        mm.printToConsole(getClass(), message);
    }

    protected void printAndNotifyProblem(Project project, String operation, String objectName, OperationResult result, Exception ex) {
        MidPointManager mm = MidPointManager.getInstance(project);

        String msg = StringUtils.capitalize(operation) + " status of " + objectName + " was " + result.getStatus();
        mm.printToConsole(getClass(), msg);

        MidPointUtils.publishNotification(notificationKey, "Warning", msg,
                NotificationType.WARNING, new ShowResultNotificationAction(result));

        if (ex != null) {
            publishException(mm, "Exception occurred during " + operation + " of " + objectName, ex);
        }
    }

    protected void printSuccess(Project project, String operation, String objectName) {
        MidPointManager mm = MidPointManager.getInstance(project);

        mm.printToConsole(getClass(), StringUtils.capitalize(operation) + " " + objectName + " finished");
    }

    protected String getOperation() {
        return operation;
    }
}
