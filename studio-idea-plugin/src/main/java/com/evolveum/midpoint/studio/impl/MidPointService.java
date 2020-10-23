package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.ui.MidPointConsolePanel;
import com.evolveum.midpoint.studio.ui.MidPointConsoleView;
import com.evolveum.midpoint.studio.ui.MidPointToolWindowFactory;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
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
public class MidPointService extends ServiceBase<MidPointSettings> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private MidPointConsoleView console;

    public MidPointService(@NotNull Project project) {
        super(project, MidPointSettings.class);
    }

    @Override
    protected MidPointSettings createDefaultSettings() {
        return MidPointSettings.createDefaultSettings();
    }

    public static MidPointService getInstance(@NotNull Project project) {
        return project.getService(MidPointService.class);
    }

    public void focusConsole() {
        ToolWindow tw = ToolWindowManager.getInstance(getProject()).getToolWindow(MidPointToolWindowFactory.WINDOW_ID);
        tw.show(null);

        ContentManager cm = tw.getContentManager();
        Content content = cm.getContent(1);
        cm.setSelectedContent(content);

        MidPointConsoleView console = getConsole();

        console.requestFocus();
        console.requestScrollingToEnd();
    }

    private MidPointConsoleView getConsole() {
        if (console != null) {
            return console;
        }

        ApplicationManager.getApplication().invokeAndWait(() -> {
            ToolWindow tw = ToolWindowManager.getInstance(getProject()).getToolWindow(MidPointToolWindowFactory.WINDOW_ID);

            ContentManager cm = tw.getContentManager();
            Content content = cm.getContent(1);
            MidPointConsolePanel panel = (MidPointConsolePanel) content.getComponent();
            this.console = panel.getConsole();
        });

        return console;
    }

    public void printToConsole(Class clazz, String message) {
        printToConsole(clazz, message, null, ConsoleViewContentType.LOG_INFO_OUTPUT);
    }

    public void printToConsole(Class clazz, String message, Exception ex) {
        printToConsole(clazz, message, ex, ConsoleViewContentType.LOG_ERROR_OUTPUT);
    }

    public void printToConsole(Class clazz, String message, Exception ex, ConsoleViewContentType type) {
        MidPointConsoleView console = getConsole();

        Validate.notNull(clazz, "Class must not be null");
        Validate.notNull(type, "Console view content type must not be null");

        StringBuilder sb = new StringBuilder();
        sb.append(DATE_FORMAT.format(new Date()));
        sb.append(' ');
        sb.append(clazz.getSimpleName());
        sb.append(": ");
        if (message != null) {
            sb.append(message);
        }
        if (message != null && ex != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(os));
            sb.append('\n').append(os.toString());
        }
        sb.append('\n');

        console.print(sb.toString(), type);
    }
}
