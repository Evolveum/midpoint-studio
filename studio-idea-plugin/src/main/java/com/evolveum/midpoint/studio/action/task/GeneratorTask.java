package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.ShowResultNotificationAction;
import com.evolveum.midpoint.studio.impl.browse.Generator;
import com.evolveum.midpoint.studio.impl.browse.GeneratorOptions;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(GeneratorTask.class);

    public static String TITLE = "Generator task";

    public static String NOTIFICATION_KEY = TITLE;

    private Generator generator;

    private GeneratorOptions options;

    private List<ObjectType> objects;

    private boolean execute;

    public GeneratorTask(
            @NotNull Project project, @Nullable Supplier<DataContext> dataContextSupplier, Generator generator,
            GeneratorOptions options, List<ObjectType> objects, boolean execute) {

        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);

        this.generator = generator;
        this.options = options;
        this.objects = objects;
        this.execute = execute;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        updateIndicator(indicator, "Starting generator");

        Project project = getProject();
        Environment env = getEnvironment();

        String content = generator.generate(project, objects, options);

        if (options.isUseActivities()) {
            try {
                content = MidPointUtils.upgradeTaskToUseActivities(content);
            } catch (Exception ex) {
                midPointService.printToConsole(env, getClass(), "Couldn't update generate task to use activities. Reason: " + ex.getMessage());

                MidPointUtils.publishExceptionNotification(project, env, GeneratorTask.class, NOTIFICATION_KEY,
                        "Couldn't update generate task to use activities", ex);
            }
        }

        if (!execute) {
            writeContent(indicator, content);
        } else {
            uploadContent(indicator, content);
        }
    }

    private void uploadContent(ProgressIndicator indicator, String content) {
        Project project = getProject();
        Environment env = getEnvironment();

        updateIndicator(indicator, "Content created, uploading to " + env.getName());

        MidPointService mm = MidPointService.get(project);

        MidPointClient client = new MidPointClient(project, env);

        List<MidPointObject> objects = MidPointUtils.parseText(project, content, null, NOTIFICATION_KEY);

        int fail = 0;
        int success = 0;
        for (MidPointObject object : objects) {
            try {
                OperationResult result = UploadTaskMixin.uploadExecute(client, object);
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

                MidPointUtils.publishExceptionNotification(project, env, GeneratorTask.class, NOTIFICATION_KEY,
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

    private void writeContent(ProgressIndicator indicator, String content) {
        updateIndicator(indicator, "Content created, writing to file");

        Project project = getProject();
        Environment env = getEnvironment();

        RunnableUtils.runWriteActionAndWait(() -> {

            VirtualFile file = null;
            Writer out = null;
            try {
                file = FileUtils.createScratchFile(project, env);

                out = new BufferedWriter(
                        new OutputStreamWriter(file.getOutputStream(GeneratorTask.this), file.getCharset()));

                IOUtils.write(content, out);
            } catch (IOException ex) {
                MidPointUtils.publishExceptionNotification(project, env, GeneratorTask.class, NOTIFICATION_KEY,
                        "Couldn't store generated content to file " + (file != null ? file.getName() : "[null]"), ex);
            } finally {
                IOUtils.closeQuietly(out);
            }

            if (file != null) {
                formatAndOpenFile(project, file);
            }
        });

        updateIndicator(indicator, "File saved");
    }

    private void formatAndOpenFile(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        WriteCommandAction.writeCommandAction(getProject()).run(() ->
                CodeStyleManager.getInstance(getProject()).reformatText(
                        psiFile, List.of(psiFile.getTextRange())
                )
        );

        FileEditorManager fem = FileEditorManager.getInstance(project);
        fem.openFile(file, true, true);
    }

    private void updateIndicator(ProgressIndicator indicator, String message) {
        LOG.debug(message);

        indicator.setText(message);
    }
}
