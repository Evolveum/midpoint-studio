package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.action.task.UploadExecuteTask;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorAction extends BackgroundAction {

    public static final String NOTIFICATION_KEY = "Generate Action";

    private static final Logger LOG = Logger.getInstance(GeneratorAction.class);

    private Generator generator;

    private GeneratorOptions options;

    private List<ObjectType> objects;

    private boolean execute;

    public GeneratorAction(Generator generator, GeneratorOptions options, List<ObjectType> objects, boolean execute) {
        super("Processing objects");

        this.generator = generator;
        this.options = options;
        this.objects = objects;
        this.execute = execute;
    }

    private void updateIndicator(ProgressIndicator indicator, String message) {
        LOG.debug(message);

        indicator.setText(message);
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        Project project = evt.getProject();

        EnvironmentService em = EnvironmentService.getInstance(project);
        if (!em.isEnvironmentSelected()) {
            MidPointUtils.publishNotification(project, NOTIFICATION_KEY, "Error", "Environment not selected", NotificationType.ERROR);
            return;
        }

        Environment env = em.getSelected();

        updateIndicator(indicator, "Starting generator");

        String content = generator.generate(project, objects, options);

        if (options.isUseActivities()) {
            try {
                content = MidPointUtils.upgradeTaskToUseActivities(content);
            } catch (Exception ex) {
                MidPointService mm = MidPointService.getInstance(project);

                mm.printToConsole(env, getClass(), "Couldn't update generate task to use activities. Reason: " + ex.getMessage());

                MidPointUtils.publishExceptionNotification(project, env, GeneratorAction.class, NOTIFICATION_KEY,
                        "Couldn't update generate task to use activities", ex);
            }
        }

        if (!execute) {
            writeContent(evt, indicator, env, content);
        } else {
            uploadContent(evt, indicator, env, content);
        }
    }

    private void uploadContent(AnActionEvent evt, ProgressIndicator indicator, Environment env, String content) {
        updateIndicator(indicator, "Content created, uploading to " + env.getName());

        Project project = evt.getProject();

        MidPointService mm = MidPointService.getInstance(project);

        MidPointClient client = new MidPointClient(project, env);

        List<MidPointObject> objects = MidPointUtils.parseText(project, content, NOTIFICATION_KEY);

        int fail = 0;
        int success = 0;
        for (MidPointObject object : objects) {
            try {
                OperationResult result = UploadExecuteTask.uploadExecute(client, object);
                boolean problem = result != null && !result.isSuccess();

                if (problem) {
                    fail++;

                    String msg = "Upload status of " + object.getName() + " was " + result.getStatus();
                    mm.printToConsole(env, getClass(), msg);

                    MidPointUtils.publishNotification(project, NOTIFICATION_KEY, "Warning", msg,
                            NotificationType.WARNING, new ShowResultNotificationAction(result));
                } else {
                    success++;

                    mm.printToConsole(env, getClass(), "Content uploaded successfuly");
                }
            } catch (Exception ex) {
                fail++;

                mm.printToConsole(env, getClass(), "Couldn't upload generated content. Reason: " + ex.getMessage());

                MidPointUtils.publishExceptionNotification(project, env, GeneratorAction.class, NOTIFICATION_KEY,
                        "Couldn't upload generated content", ex);
            }
        }

        showNotificationAfterFinish(project, success, fail);

        updateIndicator(indicator, "Content uploaded");
    }

    private void showNotificationAfterFinish(Project project, int successObjects, int failedObjects) {
        NotificationType type;
        String title;
        StringBuilder sb = new StringBuilder();

        if (failedObjects == 0 && successObjects > 0) {
            type = NotificationType.INFORMATION;
            title = "Success";

            sb.append("Upload finished.");
        } else {
            type = NotificationType.WARNING;
            title = "Warning";

            sb.append("There were problems during upload");
        }

        sb.append("<br/>");
        sb.append("Processed: ").append(successObjects).append(" objects<br/>");
        sb.append("Failed to process: ").append(failedObjects).append(" objects");

        MidPointUtils.publishNotification(project, NOTIFICATION_KEY, title, sb.toString(), type);
    }

    private void writeContent(AnActionEvent evt, ProgressIndicator indicator, Environment env, String content) {
        updateIndicator(indicator, "Content created, writing to file");

        RunnableUtils.runWriteActionAndWait(() -> {

            VirtualFile file = null;
            Writer out = null;
            try {
                file = FileUtils.createScratchFile(evt.getProject(), env);

                out = new BufferedWriter(
                        new OutputStreamWriter(file.getOutputStream(GeneratorAction.this), file.getCharset()));

                IOUtils.write(content, out);

                FileEditorManager fem = FileEditorManager.getInstance(evt.getProject());
                fem.openFile(file, true, true);
            } catch (IOException ex) {
                MidPointUtils.publishExceptionNotification(evt.getProject(), env, GeneratorAction.class, NOTIFICATION_KEY,
                        "Couldn't store generated content to file " + (file != null ? file.getName() : "[null]"), ex);
            } finally {
                IOUtils.closeQuietly(out);
            }
        });

        updateIndicator(indicator, "File saved");
    }
}
