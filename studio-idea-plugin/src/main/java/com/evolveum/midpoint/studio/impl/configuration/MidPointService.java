package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.ui.MidPointConsolePanel;
import com.evolveum.midpoint.studio.ui.MidPointConsoleView;
import com.evolveum.midpoint.studio.ui.MidPointToolWindowFactory;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "MidPointManager", storages = @Storage(value = "midpoint.xml")
)
public class MidPointService extends ServiceBase<MidPointConfiguration> {

    private static final Logger LOG = Logger.getInstance(MidPointService.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private MidPointConsoleView console;

    public MidPointService(@NotNull Project project) {
        super(project, MidPointConfiguration.class);
    }

    @Override
    protected MidPointConfiguration createDefaultSettings() {
        return MidPointConfiguration.createDefaultSettings();
    }

    public static MidPointService getInstance(@NotNull Project project) {
        return project.getService(MidPointService.class);
    }

    public void focusConsole() {
        ToolWindow tw = ToolWindowManager.getInstance(getProject()).getToolWindow(MidPointToolWindowFactory.WINDOW_ID);
        if (tw == null) {
            LOG.debug("Midpoint tool windows was not found");
            return;
        }

        tw.show(null);

        ContentManager cm = tw.getContentManager();
        Content content = cm.getContent(1);
        cm.setSelectedContent(content);

        MidPointConsoleView console = getConsole();
        if (console == null) {
            return;
        }

        console.requestFocus();
        console.requestScrollingToEnd();
    }

    private MidPointConsoleView getConsole() {
        if (console != null) {
            return console;
        }

        ApplicationManager.getApplication().invokeAndWait(() -> {
            ToolWindow tw = ToolWindowManager.getInstance(getProject()).getToolWindow(MidPointToolWindowFactory.WINDOW_ID);

            if (tw == null) {
                return;
            }

            ContentManager cm = tw.getContentManager();
            Content content = cm.getContent(1);
            MidPointConsolePanel panel = (MidPointConsolePanel) content.getComponent();
            this.console = panel.getConsole();
        });

        return console;
    }

    public void printToConsole(Environment env, Class clazz, String message) {
        printToConsole(env, clazz, message, null, ConsoleViewContentType.LOG_INFO_OUTPUT);
    }

    public void printToConsole(Environment env, Class clazz, String message, Exception ex) {
        printToConsole(env, clazz, message, ex, ConsoleViewContentType.LOG_ERROR_OUTPUT);
    }

    public void printToConsole(Environment env, @NotNull Class clazz, String message, Exception ex, @NotNull ConsoleViewContentType type) {
        Validate.notNull(clazz, "Class must not be null");
        Validate.notNull(type, "Console view content type must not be null");

        StringBuilder sb = new StringBuilder();
        sb.append(DATE_FORMAT.format(new Date()));
        sb.append(' ');

        if (env != null) {
            sb.append('[').append(env.getName()).append("] ");
        }

        sb.append(clazz != null ? clazz.getSimpleName() : "");
        sb.append(": ");
        if (message != null) {
            sb.append(message);
        }
        if (message != null && ex != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(os));
            sb.append('\n').append(os);
        }
        sb.append('\n');

        MidPointConsoleView console = getConsole();
        if (console != null) {
            RunnableUtils.invokeLaterIfNeeded(() -> console.print(sb.toString(), type), ModalityState.defaultModalityState());
        }
    }
}
