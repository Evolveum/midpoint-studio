package com.evolveum.midpoint.studio.action.logging;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetLoggerAction extends BackgroundAction {

    private static final Logger LOG = Logger.getInstance(SetLoggerAction.class);

    private static final String NOTIFICATION_KEY = "Update logging configuration";

    public SetLoggerAction() {
        super("Updating logging configuration");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        if (e.getProject() == null) {
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());

        boolean enabled = em.getSelected() != null;
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    protected void executeOnBackground(AnActionEvent e, ProgressIndicator indicator) {
        List<ClassLoggerConfigurationType> newLoggers = buildClassLoggers();
        if (newLoggers == null || newLoggers.isEmpty()) {
            noChangeNeeded();
            return;
        }

        MidPointService mm = MidPointService.getInstance(e.getProject());

        LOG.debug("Setting up MidPoint client");

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        Environment env = em.getSelected();

        mm.printToConsole(env, getClass(), "Initializing action");

        MidPointClient client = new MidPointClient(e.getProject(), env);

        LOG.debug("MidPoint client setup done");

        LOG.debug("Downloading system configuration");
        PrismObject<SystemConfigurationType> configPrism;
        try {
            MidPointObject object = client.get(SystemConfigurationType.class,
                    SystemObjectsType.SYSTEM_CONFIGURATION.value(), new SearchOptions());

            configPrism = (PrismObject) client.parseObject(object.getContent());
        } catch (ObjectNotFoundException | SchemaException | IOException ex) {
            MidPointUtils.publishException(e.getProject(), env, getClass(), NOTIFICATION_KEY,
                    "Couldn't download and parse system configuration", ex);

            return;
        }

        LOG.debug("Updating logging configuration");

        SystemConfigurationType config = configPrism.asObjectable();
        LoggingConfigurationType logging = config.getLogging();

        if (logging == null) {
            logging = new LoggingConfigurationType();
            config.setLogging(logging);
        }

        List<ClassLoggerConfigurationType> existing = logging.getClassLogger();
        Map<String, ClassLoggerConfigurationType> existingMap = existing.stream().collect(Collectors.toMap(
                o -> o.getPackage(), o -> o, (a, b) -> a));

        boolean changed = false;
        for (ClassLoggerConfigurationType cl : newLoggers) {
            ClassLoggerConfigurationType existingLogger = existingMap.get(cl.getPackage());
            if (existingLogger == null) {
                LOG.debug("Adding new class logger ", cl.getPackage(), " with level ", cl.getLevel());
                existing.add(cl);
                changed = true;
            } else {
                if (existingLogger.getLevel() != cl.getLevel()) {
                    LOG.debug("Updating class logger ", existingLogger.getPackage(), " with level ", cl.getLevel());
                    existingLogger.setLevel(cl.getLevel());
                    changed = true;
                }
            }
        }

        if (!changed) {
            noChangeNeeded();
            return;
        }

        LOG.debug("Uploading system configuration");

        try {
            UploadResponse resp = client.upload(configPrism, Arrays.asList(
                    ModelExecuteOptionsType.F_OVERWRITE.getLocalPart()
            ));
            OperationResult result = resp.getResult();
            if (result != null && !result.isSuccess()) {
                String msg = "Upload status of system configuration was " + result.getStatus();
                mm.printToConsole(env, getClass(), msg);

                MidPointUtils.publishNotification(NOTIFICATION_KEY, "Warning", msg,
                        NotificationType.WARNING, new ShowResultNotificationAction(result));
            } else {
                mm.printToConsole(env, getClass(), "System configuration uploaded");
            }
        } catch (Exception ex) {
            MidPointUtils.publishException(e.getProject(), env, getClass(), NOTIFICATION_KEY,
                    "Exception occurred during system configuration upload", ex);
        }
    }

    private void noChangeNeeded() {
        LOG.debug("No changes to logging configuration");
        MidPointUtils.publishNotification(NOTIFICATION_KEY, "Warning",
                "No changes of logging configuration created, skipping system configuration upload", NotificationType.WARNING);
    }

    public List<ClassLoggerConfigurationType> buildClassLoggers() {
        return Collections.emptyList();
    }

    protected ClassLoggerConfigurationType createClassLogger(String logger, LoggingLevelType level) {
        ClassLoggerConfigurationType cl = new ClassLoggerConfigurationType();
        cl.setPackage(logger);
        cl.setLevel(level);

        return cl;
    }
}
