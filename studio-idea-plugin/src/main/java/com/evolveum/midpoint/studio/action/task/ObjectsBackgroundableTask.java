package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.transfer.ProcessObjectResult;
import com.evolveum.midpoint.studio.action.transfer.RefreshAction;
import com.evolveum.midpoint.studio.client.ClientUtils;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.ConsoleService;
import com.evolveum.midpoint.studio.impl.ExpanderException;
import com.evolveum.midpoint.studio.impl.ShowResultNotificationAction;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.LineSeparator;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectsBackgroundableTask<S extends TaskState> extends BackgroundableTask {

    public interface TaskListener {

        void objectProcessed(MidPointObject object, ProcessObjectResult result);
    }

    enum ConfirmationUnit {

        OBJECT("object", "objects"),

        FILES("file", "files");

        private final String singular;

        private final String plural;

        ConfirmationUnit(String singular, String plural) {
            this.singular = singular;
            this.plural = plural;
        }

        public String getSingular() {
            return singular;
        }

        public String getPlural() {
            return plural;
        }

        public String getString(int count) {
            return count > 1 ? plural : singular;
        }
    }

    private interface ExtendedCallable<T> extends Callable<T> {

        String describe();
    }

    private static final Logger LOG = Logger.getInstance(ObjectsBackgroundableTask.class);

    private List<TaskListener> taskListeners = new ArrayList<>();

    protected List<Pair<String, ObjectTypes>> oids;

    protected List<MidPointObject> objects;

    protected S state = createNewState();

    public ObjectsBackgroundableTask(
            @NotNull Project project, @Nullable Supplier<DataContext> dataContextSupplier, @NotNull String title,
            @NotNull String notificationKey) {

        super(project, dataContextSupplier, title, notificationKey);
    }

    public void addTaskListener(@NotNull TaskListener listener) {
        taskListeners.add(listener);
    }

    public void removeTaskListener(@NotNull TaskListener listener) {
        taskListeners.remove(listener);
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public List<Pair<String, ObjectTypes>> getOids() {
        return oids;
    }

    public void setOids(List<Pair<String, ObjectTypes>> oids) {
        this.oids = oids;
    }

    public List<MidPointObject> getObjects() {
        return objects;
    }

    public void setObjects(List<MidPointObject> objects) {
        this.objects = objects;
    }

    protected S createNewState() {
        return (S) new TaskState();
    }

    public boolean isShowConfirmationDialog() {
        return false;
    }

    protected String getConfirmationMessage(int count, ConfirmationUnit unit) {
        return "Do you want to process " + count + " " + unit.getString(count) + "?";
    }

    protected String getConfirmationYesActionText() {
        return "Confirm";
    }

    private int showConfirmationDialog(int filesCount, ConfirmationUnit unit) {
        return MidPointUtils.showConfirmationDialog(
                getProject(), getConfirmationMessage(filesCount, unit), "Confirm action",
                getConfirmationYesActionText(), "Cancel");
    }

    private boolean confirmedOperation(int count, ConfirmationUnit unit) {
        if (isShowConfirmationDialog()) {
            int result = showConfirmationDialog(count, unit);

            return result != Messages.NO;
        }

        return true;
    }

    protected boolean isProcessingOnlyObjectTypes() {
        return false;
    }

    protected boolean isUpdateObjectAfterProcessing() {
        return false;
    }

    protected boolean isShouldStopOnError() {
        return false;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        LOG.info("Starting action " + getTitle());

        if (getProject() == null) {
            return;
        }

        indicator.setIndeterminate(false);

        boolean executed;
        if (oids != null && !oids.isEmpty()) {
            executed = processOidsList(indicator);
        } else if (objects != null && !objects.isEmpty()) {
            executed = processObjects(indicator, objects);
        } else {
            Editor editor = UIUtil.invokeAndWaitIfNeeded(() -> getData(PlatformDataKeys.EDITOR));
            if (editor != null) {
                executed = processEditor(indicator, editor);
            } else {
                executed = processFiles(indicator);
            }
        }

        if (executed) {
            showNotificationAfterFinish(false);
        }

        LOG.info("Finishing action " + getTitle());
    }

    private void publishException(MidPointService mm, String msg, Exception ex) {
        mm.printToConsole(getEnvironment(), getClass(), msg + ". Reason: " + ex.getMessage());

        MidPointUtils.publishExceptionNotification(mm.getProject(), getEnvironment(), getClass(), notificationKey, msg, ex);
    }

    private boolean processObjects(ProgressIndicator indicator, List<MidPointObject> objects) {
        if (!confirmedOperation(objects.size(), ConfirmationUnit.OBJECT)) {
            return false;
        }

        processObjects(indicator, objects, null);

        return true;
    }

    private boolean processOidsList(ProgressIndicator indicator) {
        if (!confirmedOperation(oids.size(), ConfirmationUnit.OBJECT)) {
            return false;
        }

        int i = 0;
        for (Pair<String, ObjectTypes> pair : oids) {
            ProgressManager.checkCanceled();

            i++;
            indicator.setFraction((double) i / oids.size());

            ProcessObjectResult result = processObject(null, new ExtendedCallable<>() {

                @Override
                public String describe() {
                    return pair.getFirst() + "(" + pair.getSecond() + ")";
                }

                @Override
                public ProcessObjectResult call() throws Exception {
                    return processObjectOid(pair.getSecond(), pair.getFirst());
                }
            });

            if (!result.shouldContinue()) {
                state.setStopOnError();
                break;
            }
        }

        return true;
    }

    private boolean processEditor(ProgressIndicator indicator, Editor editor) {
        String text = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {

            String txt = editor.getSelectionModel().getSelectedText();
            if (StringUtils.isNotEmpty(txt)) {
                return txt;
            }

            return editor.getDocument().getText();
        });

        VirtualFile file = UIUtil.invokeAndWaitIfNeeded(() -> getData(PlatformDataKeys.VIRTUAL_FILE));

        if (!StringUtils.isEmpty(text)) {
            return processEditorText(indicator, editor, text, file);
        }

        MidPointUtils.publishNotification(getProject(), notificationKey, "Error", "Text is empty", NotificationType.ERROR);

        return false;
    }

    protected boolean processEditorText(ProgressIndicator indicator, Editor editor, String text, VirtualFile sourceFile) {
        try {
            List<MidPointObject> objects = MidPointUtils.parseText(getProject(), text, sourceFile, getNotificationKey());
            objects = ClientUtils.filterObjectTypeOnly(objects, isProcessingOnlyObjectTypes());

            if (objects.isEmpty()) {
                MidPointUtils.publishNotification(getProject(), notificationKey, getTitle(),
                        "No objects found in text", NotificationType.WARNING);
                return false;
            }

            if (!confirmedOperation(objects.size(), ConfirmationUnit.OBJECT)) {
                return false;
            }

            ProcessFileObjectsResult result = processObjects(indicator, objects, sourceFile);

            if (isUpdateObjectAfterProcessing()) {
                updateEditor(editor, result.getNewObjects());
            }

            state.incrementProcessedFile();
        } catch (Exception ex) {
            midPointService.printToConsole(null, ObjectsBackgroundableTask.class, "Error occurred when processing text in editor", ex);
        }

        return true;
    }

    private boolean processFiles(ProgressIndicator indicator) {
        VirtualFile[] selectedFiles = UIUtil.invokeAndWaitIfNeeded(() -> getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        List<VirtualFile> toProcess = MidPointUtils.filterXmlFiles(selectedFiles);

        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(getProject(), notificationKey, getTitle(),
                    "No files matched for '" + getTitle() + "' (xml)", NotificationType.WARNING);
            return false;
        }

        if (!confirmedOperation(toProcess.size(), ConfirmationUnit.FILES)) {
            return false;
        }

        int current = 0;
        for (VirtualFile file : toProcess) {
            ProgressManager.checkCanceled();

            current++;
            indicator.setFraction((double) current / toProcess.size());

            boolean shouldContinue = processFile(file);
            if (!shouldContinue) {
                break;
            }
        }

        return true;
    }

    private boolean hasFailures() {
        return state.getFailed() > 0 || state.getSkipped() > 0 || state.getSkippedFile() > 0;
    }

    @Override
    protected List<MidPointObject> loadObjectsFromFile(VirtualFile file) throws Exception {
        return super.loadObjectsFromFile(file, isProcessingOnlyObjectTypes());
    }

    protected boolean processFile(VirtualFile file) {
        try {
            List<MidPointObject> objects = loadObjectsFromFile(file);

            ProcessFileObjectsResult result = processObjects(null, objects, file);
            List<String> newObjects = result.getNewObjects();

            boolean checkChanges = false;
            if (objects.size() == newObjects.size()) {
                for (int i = 0; i < objects.size(); i++) {
                    MidPointObject object = objects.get(i);
                    if (!Objects.equals(object.getContent(), newObjects.get(i))) {
                        checkChanges = true;
                        break;
                    }
                }
            }

            if (checkChanges && isUpdateObjectAfterProcessing()) {
                writeObjectsToFile(file, newObjects);
            }

            if (result.isStop()) {
                state.incrementProcessedFile();
                return false;
            }
        } catch (Exception ex) {
            state.incrementSkippedFile();

            midPointService.printToConsole(null, getClass(),
                    "Couldn't process file " + (file != null ? file.getPath() : "<unknown>") + ", reason: " + ex.getMessage(), ex);
        }

        state.incrementProcessedFile();
        return true;
    }

    private boolean isUpdateSelectionInEditor(Editor editor) {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {

            String txt = editor.getSelectionModel().getSelectedText();
            return StringUtils.isNotEmpty(txt);
        });
    }

    protected void updateEditor(Editor editor, List<String> newObjects) {
        boolean isUpdateSelection = isUpdateSelectionInEditor(editor);

        StringBuilder text = new StringBuilder();
        if (!isUpdateSelection && newObjects.size() > 1) {
            text.append(ClientUtils.OBJECTS_XML_PREFIX);
        }

        newObjects.forEach(o -> text.append(o));

        if (!isUpdateSelection && newObjects.size() > 1) {
            text.append(ClientUtils.OBJECTS_XML_SUFFIX);
        }

        com.intellij.openapi.editor.Document document = editor.getDocument();

        String normalized = normalizeText(text.toString(), document);

        int startIndex;
        int endIndex;
        if (isUpdateSelection) {
            AtomicInteger start = new AtomicInteger(0);
            AtomicInteger end = new AtomicInteger(0);

            ApplicationManager.getApplication().runReadAction(() -> {
                SelectionModel model = editor.getSelectionModel();
                start.set(model.getSelectionStart());
                end.set(model.getSelectionEnd());
            });

            startIndex = start.get();
            endIndex = end.get();
        } else {
            startIndex = 0;
            endIndex = document.getTextLength();
        }

        Project project = getProject();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.replaceString(startIndex, endIndex, normalized);

            VirtualFile file = editor.getVirtualFile();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            CodeStyleManager.getInstance(project).reformatText(psiFile, startIndex, normalized.length());
        });
    }

    private String normalizeText(String text, Document document) {
        if (text == null) {
            return StringUtil.notNullize(text);
        }

        boolean doNotNormalizeDetect = document instanceof DocumentImpl && ((DocumentImpl) document).acceptsSlashR();
        if (doNotNormalizeDetect) {
            return StringUtil.notNullize(text);
        }

        LineSeparator separator = StringUtil.detectSeparators(text);
        if (separator == null) {
            return StringUtil.notNullize(text);
        }

        return StringUtil.convertLineSeparators(text);
    }

    protected void writeObjectsToFile(VirtualFile file, List<String> objects) {
        RunnableUtils.runWriteActionAndWait(() -> {
            try (Writer writer = new OutputStreamWriter(file.getOutputStream(this), file.getCharset())) {
                if (objects.size() > 1) {
                    writer.write(ClientUtils.OBJECTS_XML_PREFIX);
                    writer.write(System.lineSeparator());
                }

                for (String obj : objects) {
                    writer.write(obj);
                }

                if (objects.size() > 1) {
                    writer.write(ClientUtils.OBJECTS_XML_SUFFIX);
                    writer.write(System.lineSeparator());
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
        msg.append("Skipped: ").append(state.getSkipped()).append(" objects<br/>");
        msg.append("Failed to process: ").append(state.getFailed()).append(" objects<br/>");
        msg.append("Processed files: ").append(state.getProcessedFile()).append("<br/>");
        msg.append("Skipped files: ").append(state.getSkippedFile()).append("<br/>");

        LOG.info("Task " + getTitle() + " status: " + type.name() + "\n" + msg);

        MidPointUtils.publishNotification(getProject(), notificationKey, title, msg.toString(), type, getNotificationActionsAfterFinish());
    }

    protected NotificationAction[] getNotificationActionsAfterFinish() {
        return new NotificationAction[0];
    }

    protected boolean shouldSkipObjectProcessing(MidPointObject object) {
        return false;
    }

    protected ProcessFileObjectsResult processObjects(ProgressIndicator indicator, List<MidPointObject> objects, VirtualFile file) {
        if (objects.isEmpty()) {
            state.incrementSkippedFile();
            midPointService.printToConsole(null, RefreshAction.class,
                    "Skipped file " + (file != null ? file.getPath() : "<unknown>") + " no objects found (parsed).");

            return new ProcessFileObjectsResult();
        }

        List<String> newObjects = new ArrayList<>();
        boolean stop = false;

        int current = 0;
        for (MidPointObject object : objects) {
            ProgressManager.checkCanceled();

            current++;
            if (indicator != null) {
                indicator.setFraction((double) current / objects.size());
            }

            if (shouldSkipObjectProcessing(object)) {
                newObjects.add(object.getContent());
                state.incrementSkipped();
                continue;
            }

            ProcessObjectResult result = processObject(object, new ExtendedCallable<>() {

                @Override
                public String describe() {
                    return object.getName() + "(" + object.getOid() + ")";
                }

                @Override
                public ProcessObjectResult call() throws Exception {
                    return processObject(object);
                }
            });

            taskListeners.forEach(l -> l.objectProcessed(object, result));

            if (!result.shouldContinue()) {
                state.setStopOnError();
                stop = true;
                break;
            }

            MidPointObject newObject = result.object();
            if (newObject != null) {
                newObjects.add(newObject.getContent());
            }
        }

        return new ProcessFileObjectsResult(newObjects, stop);
    }

    private ProcessObjectResult processObject(MidPointObject object, ExtendedCallable<ProcessObjectResult> callable) {
        try {
            ProcessObjectResult processResult = callable.call();
            if (processResult.problem()) {
                state.incrementFailed();
            } else {
                state.incrementProcessed();
            }

            return processResult;
        } catch (Exception ex) {
            state.incrementFailed();

            String msg = "Exception occurred during '" + getTitle() + "' of '" + callable.describe() + "', reason: " + ex.getMessage();
            if (ex instanceof ExpanderException) {
                ConsoleService.get(getProject()).printToConsole(getEnvironment(), getClass(), msg);

                MidPointUtils.publishNotification(
                        getProject(), notificationKey, "Error", msg, NotificationType.WARNING);
            } else {
                publishException(midPointService, msg, ex);
            }
        }

        ProcessObjectResult result = new ProcessObjectResult(null);
        result.object(object);
        result.shouldContinue(!isShouldStopOnError());

        return result;
    }

    protected ProcessObjectResult processObject(MidPointObject object) throws Exception {
        return processObjectOid(object.getType(), object.getOid());
    }

    protected ProcessObjectResult processObjectOid(ObjectTypes type, String oid) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected ProcessObjectResult validateOperationResult(String operation, OperationResult result, String objectName) {
        boolean problem = result != null && !result.isSuccess();
        if (problem) {
            printAndNotifyProblem(operation, objectName, result, null);
        } else {
            printSuccess(operation, objectName);
        }

        return new ProcessObjectResult(result).problem(problem);
    }

    protected void printAndNotifyProblem(String operation, String objectName, OperationResult result, Exception ex) {
        String msg = StringUtils.capitalize(operation) + " status of " + objectName + " was " + result.getStatus();

        midPointService.printToConsole(getEnvironment(), getClass(), msg);

        MidPointUtils.publishNotification(getProject(), notificationKey, "Warning", msg,
                NotificationType.WARNING, new ShowResultNotificationAction(result));

        if (ex != null) {
            publishException(midPointService, "Exception occurred during '" + operation + "' of " + objectName, ex);
        }
    }

    protected void printSuccess(String operation, String objectName) {
        midPointService.printToConsole(getEnvironment(), getClass(), StringUtils.capitalize(operation) + " '" + objectName + "' finished");
    }

    protected void printProblem(String message) {
        midPointService.printToConsole(getEnvironment(), getClass(), message);
    }
}
