package com.evolveum.midpoint.studio.impl.ide.error;

import com.intellij.diagnostic.IdeaReportingEvent;
import com.intellij.diagnostic.LogMessage;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class JiraErrorReporter extends ErrorReportSubmitter {

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events,
                          String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<SubmittedReportInfo> consumer) {

        DataContext dataContext = DataManager.getInstance().getDataContext(parentComponent);
        IdeaLoggingEvent event = events[0];

        ReporterError error = new ReporterError(events[0].getThrowable(), IdeaLogger.ourLastActionId);

        error.setDescription(additionalInfo);
        error.setMessage(event.getMessage());

        if (event instanceof IdeaReportingEvent) {
            final IdeaPluginDescriptor plugin = ((IdeaReportingEvent) event).getPlugin();
            if (plugin != null) {
                error.setPluginName(plugin.getName());
                error.setPluginVersion(plugin.getVersion());
            }
        }

        Object data = event.getData();
        if (data instanceof LogMessage) {
            error.setAttachments(ContainerUtil.filter(((LogMessage) data).getAllAttachments(), Attachment::isIncluded));
        }

        error.setOsName(SystemInfo.OS_NAME);
        error.setJavaVersion(SystemInfo.JAVA_VERSION);
        error.setJavaVmVendor(SystemInfo.JAVA_VENDOR);

        ApplicationNamesInfo namesInfo = ApplicationNamesInfo.getInstance();

        error.setAppName(namesInfo.getProductName());
        error.setAppFullName(namesInfo.getFullProductName());

        ApplicationInfoEx appInfo = (ApplicationInfoEx) ApplicationInfo.getInstance();
        error.setAppVersionName(appInfo.getVersionName());
        error.setEap(appInfo.isEAP());
        error.setAppBuild(appInfo.getBuild().asString());
        error.setAppVersion(appInfo.getFullVersion());

        final Project project = CommonDataKeys.PROJECT.getData(dataContext);

        JiraFeedbackTask task = new JiraFeedbackTask(project, "Submitting error report...",
                true, error, new CallbackWithNotification(consumer, project));

        if (project == null) {
            task.run(new EmptyProgressIndicator());
        } else {
            ProgressManager.getInstance().run(task);
        }

        return true;
    }

    @NotNull
    @Override
    public String getReportActionText() {
        return "Create Jira Issue";
    }

//    @Override
//    public @Nullable String getReporterAccount() {
//        return "";
//    }
//
//    @Override
//    public void changeReporterAccount(@NotNull Component parentComponent) {
//        JBPopupFactory factory = JBPopupFactory.getInstance();
//
//    }
}