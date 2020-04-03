package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
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
        EnvironmentManager em = EnvironmentManager.getInstance(evt.getProject());
        if (!em.isEnvironmentSelected()) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Error", "Environment not selected", NotificationType.ERROR);
            return;
        }

        Environment env = em.getSelected();

        updateIndicator(indicator, "Starting generator");

        String content = generator.generate(objects, options);

        if (!execute) {
            writeContent(evt, indicator, env, content);
        } else {
            uploadContent(evt, indicator, env, content);
        }
    }

    private void uploadContent(AnActionEvent evt, ProgressIndicator indicator, Environment env, String content) {
        updateIndicator(indicator, "Content created, uploading to " + env.getName());

        MidPointClient client = new MidPointClient(evt.getProject(), env);

        List<PrismObject<?>> objects = null;
        try {
            objects = client.parseObjects(content);
        } catch (IOException | SchemaException ex) {
            MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Couldn't parse generated content", ex);
        }

        for (PrismObject object : objects) {
            try {
                UploadResponse resp = client.upload(object, new UploadOptions());
                // todo check oid/result
            } catch (Exception ex) {
                // todo proper error handling (sum all errors and show notification if necessary)
            }
        }

        updateIndicator(indicator, "Content uploaded");
    }

    private void writeContent(AnActionEvent evt, ProgressIndicator indicator, Environment env, String content) {
        updateIndicator(indicator, "Content created, writing to file");

        ApplicationManager.getApplication().invokeAndWait(() ->
                RunnableUtils.runWriteAction(() -> {

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
                        MidPointUtils.publishExceptionNotification(NOTIFICATION_KEY, "Couldn't store generated content to file " + (file != null ? file.getName() : "[null]"), ex);
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                }));

        updateIndicator(indicator, "File saved");
    }
}
