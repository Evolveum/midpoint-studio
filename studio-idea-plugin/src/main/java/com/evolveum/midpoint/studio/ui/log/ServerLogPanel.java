package com.evolveum.midpoint.studio.ui.log;

import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.MidPointProjectNotifier;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.impl.log.*;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.StudioLocalization;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SearchTextField;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tool window tab that tails the selected environment's server log over REST.
 * All polling happens off the EDT through {@link ServerLogTailer}; this class is
 * only wiring: console, toolbar, filter, capture file and scheduling.
 */
public class ServerLogPanel extends BorderLayoutPanel implements Disposable {

    private final Project project;

    private final ConsoleViewImpl console;

    private final SearchTextField filterField;

    private final JLabel status = new JLabel();

    private final Alarm filterAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);

    private static final DateTimeFormatter SESSION_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final Object lock = new Object();

    // guarded by lock
    private ServerLogTailer tailer;
    private ScheduledFuture<?> scheduled;
    private boolean running;
    private LogEntryParser parser = new LogEntryParser();

    /**
     * All capture file I/O runs here, so neither the EDT nor the poll thread ever touches
     * disk. Single-threaded, so open/write/close stay ordered; {@link #captureWriter} is
     * only ever touched from this executor.
     */
    private final ExecutorService captureExecutor =
            AppExecutorUtil.createBoundedApplicationPoolExecutor("MidPoint Server Log Capture", 1);

    private LogCaptureWriter captureWriter;

    private volatile LogEntryBuffer buffer;

    private volatile String filter = "";

    private volatile long filterGeneration;

    /** Toggle state: the user wants captures. Survives the tail being stopped. */
    private volatile boolean recording;

    /** Whether a capture session file is currently open (or about to be). */
    private volatile boolean sessionOpen;

    public ServerLogPanel(@NotNull Project project) {
        this.project = project;

        MidPointConfiguration settings = MidPointService.get(project).getSettings();
        this.buffer = new LogEntryBuffer(settings.getLogBufferEntries());

        this.console = new ConsoleViewImpl(project, true);
        Disposer.register(this, console);

        this.filterField = new SearchTextField(false);
        filterField.getTextEditor().getEmptyText()
                .setText(StudioLocalization.message("ServerLogPanel.filter.placeholder"));
        filterField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                filterAlarm.cancelAllRequests();
                filterAlarm.addRequest(() -> applyFilter(filterField.getText()), 200);
            }
        });

        JPanel north = new JPanel(new BorderLayout());
        north.add(filterField, BorderLayout.CENTER);
        north.add(status, BorderLayout.EAST);

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("ServerLogPanel", createActions(), false);
        toolbar.setTargetComponent(this);

        addToLeft(toolbar.getComponent());
        addToTop(north);
        addToCenter(console.getComponent());

        project.getMessageBus().connect(this)
                .subscribe(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC, new MidPointProjectNotifier() {
                    @Override
                    public void environmentChanged(Environment oldEnv, Environment newEnv) {
                        onEnvironmentChanged();
                    }
                });
    }

    private DefaultActionGroup createActions() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new ToggleAction(StudioLocalization.message("ServerLogPanel.start"),
                null, AllIcons.Actions.Execute) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                synchronized (lock) {
                    return running;
                }
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                if (state) {
                    start();
                } else {
                    stop(null);
                }
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                super.update(e);

                Environment env = EnvironmentService.getInstance(project).getSelected();
                e.getPresentation().setEnabled(env != null);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });

        group.add(new AnAction(StudioLocalization.message("ServerLogPanel.clear"),
                null, AllIcons.Actions.GC) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                synchronized (lock) {
                    parser.reset();
                }
                buffer.clear();
                console.clear();
            }
        });

        group.add(new ToggleAction(StudioLocalization.message("ServerLogPanel.record"),
                null, AllIcons.Actions.MenuSaveall) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return recording;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                if (state) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        return group;
    }

    private void start() {
        Environment env = EnvironmentService.getInstance(project).getSelected();
        if (env == null) {
            setStatus(StudioLocalization.message("ServerLogPanel.noEnvironment"));
            return;
        }

        MidPointConfiguration settings = MidPointService.get(project).getSettings();

        // suppressNotifications + suppressConsole: no body-logging interceptor,
        // no per-poll notifications - errors surface only in this panel's status
        MidPointClient client = new MidPointClient(project, env, true, true);

        ServerLogTailer.Config config = new ServerLogTailer.Config(
                settings.getLogPollInterval() * 1000L,
                settings.getLogInitialTailSize() * 1024L,
                settings.getLogMaxChunkSize() * 1024L,
                60_000L);

        String envId = env.getId();

        synchronized (lock) {
            parser = createParser(settings.getLogEntryStartPattern());
            tailer = new ServerLogTailer(
                    (from, max) -> client.getLog(from, max),
                    config,
                    () -> isStillSelected(envId),
                    new TailListener());
            running = true;
            schedule(0);
        }
        setStatus("");
    }

    private LogEntryParser createParser(String pattern) {
        try {
            return new LogEntryParser(pattern);
        } catch (java.util.regex.PatternSyntaxException ex) {
            // settings UI validates on input, but the persisted value may still be bad
            setStatus("Invalid entry start pattern, using default: " + ex.getDescription());
            return new LogEntryParser();
        }
    }

    private boolean isStillSelected(String envId) {
        Environment current = EnvironmentService.getInstance(project).getSelected();
        return current != null && current.getId().equals(envId);
    }

    private void schedule(long delayMillis) {
        // caller holds lock
        scheduled = AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
            ServerLogTailer current;
            synchronized (lock) {
                if (!running || tailer == null) {
                    return;
                }
                current = tailer;
            }

            long next = current.pollOnce();

            synchronized (lock) {
                if (!running || tailer != current) {
                    return;
                }
                if (next == ServerLogTailer.STOP) {
                    running = false;
                    return;
                }
                schedule(next);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void stop(String message) {
        synchronized (lock) {
            running = false;
            if (scheduled != null) {
                scheduled.cancel(false);
                scheduled = null;
            }
            if (tailer != null) {
                tailer.flush();
                tailer = null;
            }
        }
        // the capture stays armed; only the session file is finished. Restarting the tail
        // opens a new one.
        closeCaptureSession();
        if (message != null) {
            setStatus(message);
        }
    }

    private void onEnvironmentChanged() {
        stop(null);
        buffer.clear();
        ApplicationManager.getApplication().invokeLater(console::clear);
        setStatus("");
    }

    private void startRecording() {
        recording = true;
        openCaptureSession();
    }

    private void stopRecording() {
        recording = false;
        closeCaptureSession();
    }

    /**
     * Opens a fresh, uniquely named session file seeded with the current buffer. Every
     * arming of the capture - and every restart of the tail while armed - produces its own
     * file, so a session file is self-contained and nothing is ever appended twice.
     */
    private void openCaptureSession() {
        Environment env = EnvironmentService.getInstance(project).getSelected();
        if (env == null || project.getBasePath() == null) {
            return;
        }

        MidPointConfiguration settings = MidPointService.get(project).getSettings();
        long maxFileSize = settings.getLogCaptureMaxFileSize() * 1024L * 1024L;
        int maxFiles = settings.getLogCaptureMaxFiles();

        // snapshot on the calling thread so the seed can't pick up entries parsed later
        List<LogEntry> seed = buffer.snapshot();
        String envName = env.getName();

        sessionOpen = true;
        captureExecutor.execute(() -> doOpenSession(envName, seed, maxFileSize, maxFiles));
    }

    private void closeCaptureSession() {
        sessionOpen = false;
        captureExecutor.execute(this::doCloseSession);
    }

    private void doOpenSession(String envName, List<LogEntry> seed, long maxFileSize, int maxFiles) {
        doCloseSession();

        try {
            File dir = new File(project.getBasePath(), "logs");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();

            String name = MidPointUtils.escapeObjectName(envName) + "_" + SESSION_STAMP.format(LocalDateTime.now());
            File file = new File(dir, name + ".log");

            LogCaptureWriter writer = new LogCaptureWriter(file, maxFileSize, maxFiles);
            for (LogEntry entry : seed) {
                writer.write(entry.getText());
            }

            captureWriter = writer;

            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        } catch (IOException ex) {
            captureWriter = null;
            sessionOpen = false;
            recording = false;
            setStatus("Couldn't start recording: " + ex.getMessage());
        }
    }

    private void doWriteToSession(String chunk) {
        if (captureWriter == null) {
            return;
        }

        try {
            captureWriter.write(chunk);
        } catch (IOException ex) {
            doCloseSession();
            sessionOpen = false;
            recording = false;
            setStatus("Recording failed: " + ex.getMessage());
        }
    }

    private void doCloseSession() {
        if (captureWriter == null) {
            return;
        }

        try {
            captureWriter.close();
        } catch (IOException ignored) {
        }
        captureWriter = null;

        if (project.getBasePath() != null) {
            LocalFileSystem.getInstance().refreshIoFiles(
                    List.of(new File(project.getBasePath(), "logs")), true, true, null);
        }
    }

    private class TailListener implements ServerLogTailer.Listener {

        @Override
        public void onText(String chunk) {
            if (recording) {
                // the tail was restarted while still armed - start a new session file,
                // seeded before this chunk is parsed so the seed and the chunk can't overlap
                if (!sessionOpen) {
                    openCaptureSession();
                }
                captureExecutor.execute(() -> doWriteToSession(chunk));
            }

            String currentFilter = filter;
            long generation = filterGeneration;

            String[] lines = chunk.split("\n", -1);
            // a chunk ending with '\n' yields one trailing empty element - drop it
            int count = lines.length > 0 && lines[lines.length - 1].isEmpty()
                    ? lines.length - 1 : lines.length;

            for (int i = 0; i < count; i++) {
                LogEntryParser.ParsedLine parsed;
                synchronized (lock) {
                    parsed = parser.feedLine(lines[i]);
                }
                if (parsed.newEntry()) {
                    buffer.add(parsed.entry());
                }
                printLive(parsed, currentFilter, generation);
            }
        }

        @Override
        public void onRotation() {
            LogEntry marker = new LogEntry(LogLevel.UNKNOWN);
            marker.appendLine(StudioLocalization.message("ServerLogPanel.rotated"));

            synchronized (lock) {
                // post-rotation content must start a fresh entry, not continue the
                // last pre-rotation one
                parser.reset();
            }

            buffer.add(marker);
            console.print(marker.getText(), ConsoleViewContentType.SYSTEM_OUTPUT);

            // the discontinuity matters most in the recorded file
            if (recording && sessionOpen) {
                captureExecutor.execute(() -> doWriteToSession(marker.getText()));
            }
        }

        @Override
        public void onFatalError(String message, Exception ex) {
            stop(StudioLocalization.message("ServerLogPanel.stopped.fatal", message));
        }

        @Override
        public void onTransientError(Exception ex, long retryInMillis) {
            setStatus(StudioLocalization.message("ServerLogPanel.reconnecting",
                    retryInMillis / 1000, ex.getMessage()));
        }
    }

    private void printLive(LogEntryParser.ParsedLine parsed, String filter, long generation) {
        LogEntry entry = parsed.entry();
        if (!entry.matches(filter)) {
            return;
        }

        if (entry.getPrintedGeneration() != generation) {
            // entry was hidden (or is new): print everything accumulated so far
            console.print(entry.getText(), contentType(entry.getLevel()));
            entry.setPrintedGeneration(generation);
        } else {
            console.print(parsed.line() + "\n", contentType(entry.getLevel()));
        }
    }

    private void applyFilter(String newFilter) {
        this.filter = newFilter == null ? "" : newFilter.toLowerCase();
        this.filterGeneration++;

        console.clear();
        long generation = filterGeneration;
        String currentFilter = filter;

        for (LogEntry entry : buffer.snapshot()) {
            if (entry.matches(currentFilter)) {
                console.print(entry.getText(), contentType(entry.getLevel()));
                entry.setPrintedGeneration(generation);
            }
        }
    }

    private ConsoleViewContentType contentType(LogLevel level) {
        switch (level) {
            case ERROR:
                return ConsoleViewContentType.LOG_ERROR_OUTPUT;
            case WARN:
                return ConsoleViewContentType.LOG_WARNING_OUTPUT;
            case INFO:
                return ConsoleViewContentType.LOG_INFO_OUTPUT;
            case DEBUG:
                return ConsoleViewContentType.LOG_DEBUG_OUTPUT;
            case TRACE:
                return ConsoleViewContentType.LOG_VERBOSE_OUTPUT;
            default:
                return ConsoleViewContentType.NORMAL_OUTPUT;
        }
    }

    private void setStatus(String text) {
        ApplicationManager.getApplication().invokeLater(() -> status.setText(text));
    }

    @Override
    public void dispose() {
        recording = false;
        stop(null);
        captureExecutor.shutdown();
    }
}
