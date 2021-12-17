package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Viliam Repan (lazyman).
 */
@Deprecated
public abstract class BaseObjectsAction extends BackgroundAction {

    private static final Logger LOG = Logger.getInstance(BaseObjectsAction.class);

    private final String notificationKey;

    private final String operation;

    private List<Pair<String, ObjectTypes>> oids;

    private Environment environment;

    public BaseObjectsAction(String taskTitle, String notificationKey, String operation) {
        super(taskTitle);

        this.notificationKey = notificationKey;
        this.operation = operation;
    }

    public BaseObjectsAction(String taskTitle, String notificationKey, String operation, List<Pair<String, ObjectTypes>> oids) {
        super(taskTitle);

        this.notificationKey = notificationKey;
        this.operation = operation;

        this.oids = oids;
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        boolean enabled = isActionEnabled(evt);
        evt.getPresentation().setEnabled(enabled);
    }

    protected boolean isActionEnabled(AnActionEvent evt) {
        if (!MidPointUtils.isVisibleWithMidPointFacet(evt)) {
            return false;
        }

        EnvironmentService em = EnvironmentService.getInstance(evt.getProject());
        if (em.getSelected() == null) {
            return false;
        }

        return MidPointUtils.isMidpointObjectFileSelected(evt) || (oids != null && !oids.isEmpty());
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        MidPointService mm = MidPointService.getInstance(e.getProject());

        LOG.debug("Setting up MidPoint client");

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        environment = em.getSelected();

        mm.printToConsole(environment, getClass(), "Initializing " + operation + " action");

        MidPointClient client = new MidPointClient(e.getProject(), environment);

        LOG.debug("MidPoint client setup done");

        processObjects(e, indicator, client);
    }

    protected void processObjects(AnActionEvent e, ProgressIndicator indicator, MidPointClient client) {
        indicator.setIndeterminate(false);

        if (oids != null && !oids.isEmpty()) {
            processObjectsByOids(e, indicator, client);
            return;
        }

        processObjectsBySelection(e, indicator, client);
    }

    private void processObjectsByOids(AnActionEvent evt, ProgressIndicator indicator, MidPointClient client) {
        MidPointService mm = MidPointService.getInstance(evt.getProject());

        ProcessState state = new ProcessState();

        try {
            int i = 0;
            for (Pair<String, ObjectTypes> pair : oids) {
                ProgressManager.checkCanceled();

                i++;
                indicator.setFraction((double) i / oids.size());

                processObject(mm, state, new ExtendedCallable<>() {

                    @Override
                    public String describe() {
                        return pair.getFirst() + "(" + pair.getSecond() + ")";
                    }

                    @Override
                    public ProcessObjectResult call() throws Exception {
                        return processObjectOid(evt, client, pair.getSecond(), pair.getFirst());
                    }
                });
            }
        } catch (Exception ex) {
            state.fail++;

            publishException(mm, "Exception occurred during " + operation, ex);
        }

        showNotificationAfterFinish(state, evt.getProject());
    }

    private void processObjectsBySelection(AnActionEvent e, ProgressIndicator indicator, MidPointClient client) {
        MidPointService mm = MidPointService.getInstance(e.getProject());

        Editor editor = UIUtil.invokeAndWaitIfNeeded(() -> e.getData(PlatformDataKeys.EDITOR));

        if (editor != null) {
            String text = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {

                String txt = editor.getSelectionModel().getSelectedText();
                if (StringUtils.isNotEmpty(txt)) {
                    return txt;
                }

                return editor.getDocument().getText();
            });

            if (!StringUtils.isEmpty(text)) {
                VirtualFile file = UIUtil.invokeAndWaitIfNeeded(() -> e.getData(PlatformDataKeys.VIRTUAL_FILE));
                BaseObjectsAction.ProcessState state = processText(e, mm, indicator, client, text, file);

                showNotificationAfterFinish(state, e.getProject());
            } else {
                MidPointUtils.publishNotification(e.getProject(), notificationKey, "Error", "Text is empty", NotificationType.ERROR);
            }

            return;
        }

        VirtualFile[] selectedFiles = UIUtil.invokeAndWaitIfNeeded(() -> e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.publishNotification(e.getProject(), notificationKey, getTaskTitle(),
                    "No files selected for " + operation, NotificationType.WARNING);
            return;
        }

        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(e.getProject(), notificationKey, getTaskTitle(),
                    "No files matched for " + operation + " (xml)", NotificationType.WARNING);
            return;
        }

        processFiles(e, mm, indicator, client, toProcess);
    }

    private void showNotificationAfterFinish(ProcessState state, Project project) {
        NotificationType type;
        String title;
        StringBuilder sb = new StringBuilder();

        if (state.failedFiles == 0 && state.fail == 0 && state.success > 0) {
            type = NotificationType.INFORMATION;
            title = "Success";

            sb.append(getTaskTitle() + " finished.");
        } else {
            type = NotificationType.WARNING;
            title = "Warning";

            sb.append("There were problems during '" + getTaskTitle() + "'");
        }

        sb.append("<br/>");
        sb.append("Processed: ").append(state.success).append(" objects<br/>");
        sb.append("Failed to process: ").append(state.fail).append(" objects<br/>");

        if (state.files > 0 || state.failedFiles > 0) {
            sb.append("Files processed: ").append(state.files).append("<br/>");
            sb.append("Failed to process: ").append(state.failedFiles).append("<br/>");
        }

        MidPointUtils.publishNotification(project, notificationKey, title, sb.toString(), type);
    }

    private void publishException(MidPointService mm, String msg, Exception ex) {
        mm.printToConsole(environment, getClass(), msg + ". Reason: " + ex.getMessage());

        MidPointUtils.publishExceptionNotification(mm.getProject(), environment, getClass(), notificationKey, msg, ex);
    }

    private void processFiles(AnActionEvent evt, MidPointService mm, ProgressIndicator indicator, MidPointClient client, List<VirtualFile> files) {
        ProcessState fullState = new ProcessState();

        int i = 0;
        for (VirtualFile file : files) {
            ProgressManager.checkCanceled();

            fullState.incrementFiles();

            i++;
            indicator.setFraction((double) i / files.size());

            RunnableUtils.runWriteActionAndWait(() -> {
                MidPointUtils.forceSaveAndRefresh(evt.getProject(), file);

                try (Reader in = new BufferedReader(new InputStreamReader(file.getInputStream(), file.getCharset()))) {
                    String xml = IOUtils.toString(in);

                    ProcessState state = processText(evt, mm, null, client, xml, file);
                    fullState.incrementAll(state);
                } catch (IOException ex) {
                    fullState.incrementFailedFiles();
                    publishException(mm, "Exception occurred when loading file '" + file.getName() + "'", ex);
                }
            });
        }

        showNotificationAfterFinish(fullState, evt.getProject());
    }

    private ProcessState processText(AnActionEvent evt, MidPointService mm, ProgressIndicator indicator, MidPointClient client, String text, VirtualFile file) {
        ProcessState state = new ProcessState();

        try {
            List<MidPointObject> objects = MidPointUtils.parseText(evt.getProject(), text, notificationKey);
            objects = ClientUtils.filterObjectTypeOnly(objects, false);

            int i = 0;
            for (MidPointObject obj : objects) {
                ProgressManager.checkCanceled();

                obj.setFile(VfsUtil.virtualToIoFile(file));

                i++;
                if (indicator != null) {
                    indicator.setFraction((double) i / objects.size());
                }

                processObject(mm, state, new ExtendedCallable<>() {

                    @Override
                    public String describe() {
                        return obj.getName() + "(" + obj.getOid() + ")";
                    }

                    @Override
                    public ProcessObjectResult call() throws Exception {
                        return processObject(evt, client, obj);
                    }
                });
            }
        } catch (Exception ex) {
            state.fail++;

            publishException(mm, "Exception occurred during " + operation, ex);
        }

        return state;
    }

    private boolean processObject(MidPointService mm, ProcessState state, ExtendedCallable<ProcessObjectResult> callable) {
        try {
            ProcessObjectResult processResult = callable.call();
            if (processResult.problem()) {
                state.fail++;
            } else {
                state.success++;
            }

            if (!processResult.shouldContinue()) {
                return false;
            }
        } catch (Exception ex) {
            state.fail++;

            publishException(mm, "Exception occurred during " + operation + " of '" + callable.describe() + "'", ex);
        }

        return true;
    }

    public <O extends ObjectType> ProcessObjectResult processObject(AnActionEvent evt, MidPointClient client, MidPointObject obj) throws Exception {
        return processObjectOid(evt, client, obj.getType(), obj.getOid());
    }

    public <O extends ObjectType> ProcessObjectResult processObjectOid(AnActionEvent evt, MidPointClient client, ObjectTypes type, String oid) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

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

        mm.printToConsole(environment, getClass(), message);
    }

    protected void printAndNotifyProblem(Project project, String operation, String objectName, OperationResult result, Exception ex) {
        MidPointService mm = MidPointService.getInstance(project);

        String msg = StringUtils.capitalize(operation) + " status of " + objectName + " was " + result.getStatus();
        mm.printToConsole(environment, getClass(), msg);

        MidPointUtils.publishNotification(project, notificationKey, "Warning", msg,
                NotificationType.WARNING, new ShowResultNotificationAction(result));

        if (ex != null) {
            publishException(mm, "Exception occurred during " + operation + " of " + objectName, ex);
        }
    }

    protected void printSuccess(Project project, String operation, String objectName) {
        MidPointService mm = MidPointService.getInstance(project);

        mm.printToConsole(environment, getClass(), StringUtils.capitalize(operation) + " '" + objectName + "' finished");
    }

    protected String getOperation() {
        return operation;
    }

    private static class ProcessState {

        private int success;

        private int fail;

        private int files;

        private int failedFiles;

        public void incrementSuccess() {
            success++;
        }

        public void incrementFail() {
            fail++;
        }

        public void incrementFiles() {
            files++;
        }

        public void incrementFailedFiles() {
            failedFiles++;
        }

        public void incrementAll(ProcessState state) {
            if (state == null) {
                return;
            }

            success += state.success;
            fail += state.fail;
            files += state.files;
            failedFiles += state.failedFiles;
        }
    }

    private interface ExtendedCallable<T> extends Callable<T> {

        String describe();
    }
}
