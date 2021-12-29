package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.SearchOptions;
import com.evolveum.midpoint.studio.impl.ShowResultNotificationAction;
import com.evolveum.midpoint.studio.impl.UploadResponse;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SetLoggerTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(SetLoggerTask.class);

    public static String TITLE = "Set logger task";

    public static final String NOTIFICATION_KEY = "Set logger task";

    public SetLoggerTask(@NotNull AnActionEvent event) {
        this(event, TITLE, NOTIFICATION_KEY);
    }

    protected SetLoggerTask(@NotNull AnActionEvent event, @NotNull String title, @NotNull String notificationKey) {
        super(event.getProject(), title, notificationKey);

        this.event = event;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        indicator.setIndeterminate(false);

        List<ClassLoggerConfigurationType> newLoggers = buildClassLoggers();
        if (newLoggers == null || newLoggers.isEmpty()) {
            noChangeNeeded();
            return;
        }

        Environment env = getEnvironment();

        LOG.debug("Downloading system configuration");
        PrismObject<SystemConfigurationType> configPrism;
        try {
            MidPointObject object = client.get(SystemConfigurationType.class,
                    SystemObjectsType.SYSTEM_CONFIGURATION.value(), new SearchOptions());
            if (object == null) {
                return;
            }

            configPrism = (PrismObject) client.parseObject(object.getContent());

            indicator.setFraction(0.5);
        } catch (Exception ex) {
            MidPointUtils.publishException(getProject(), env, getClass(), NOTIFICATION_KEY,
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

            indicator.setFraction(1);
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
                midPointService.printToConsole(env, getClass(), msg);

                MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Warning", msg,
                        NotificationType.WARNING, new ShowResultNotificationAction(result));
            } else {
                midPointService.printToConsole(env, getClass(), "System configuration uploaded");
            }

            indicator.setFraction(1);
        } catch (Exception ex) {
            MidPointUtils.publishException(getProject(), env, getClass(), NOTIFICATION_KEY,
                    "Exception occurred during system configuration upload", ex);
        }
    }

    private void noChangeNeeded() {
        LOG.debug("No changes to logging configuration");
        MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Warning",
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
