package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.traces.TraceParser;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.impl.performance.PerformanceTree;
import com.evolveum.midpoint.studio.impl.performance.output.AsciiWriter;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.util.Holder;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TracingOutputType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class SummarizeOperations extends BackgroundAction {

    private static final String NOTIFICATION_KEY = "Summarize operations";

    public SummarizeOperations() {
        super("Summarizing operations");
    }

    public SummarizeOperations(String taskTitle) {
        super(taskTitle);
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {

        List<VirtualFile> files = getFilesToProcess(evt);
        if (files.isEmpty()) {
            return;
        }

        MidPointService mm = getMidpointService(evt);
        EnvironmentService es = EnvironmentService.getInstance(Objects.requireNonNull(evt.getProject()));
        summarizeFiles(files, mm, es, evt.getProject());
    }

    private MidPointService getMidpointService(AnActionEvent evt) {
        Project project = evt.getProject();
        assert project != null;
        return MidPointService.getInstance(project);
    }

    private void summarizeFiles(List<VirtualFile> files, MidPointService mm,
            EnvironmentService es, Project project) {
        PrismContext prismContext = MidPointUtils.DEFAULT_PRISM_CONTEXT;
        TraceParser traceParser = new TraceParser(prismContext);

        PerformanceTree performanceTree = new PerformanceTree();

        for (VirtualFile file : files) {
            RunnableUtils.runWriteActionAndWait(() -> {
                Environment env = es.getSelected();
                try {
                    TracingOutputType tracingOutput = traceParser.parse(file.getInputStream(), true, true, file.getCanonicalPath());
                    OperationResultType operationResult = tracingOutput.getResult();
                    if (operationResult != null) {
                        performanceTree.addSample(operationResult);
                        mm.printToConsole(env, getClass(), "Processed " + file.getName());
                    } else {
                        mm.printToConsole(env, getClass(), "No operation result in " + file.getName());
                    }
                } catch (Exception ex) {
                    String msg = "Exception occurred when loading file '" + file.getName() + "'";
                    processException(msg, ex, mm, es);
                }
            });
        }

        Environment env = es.getSelected();
        mm.printToConsole(env, getClass(), "Parsed " + performanceTree.getSamples() + " samples");
        if (performanceTree.getSamples() > 0) {
            performanceTree.computeStatistics();
            System.out.println(performanceTree.dump());

            System.out.println();
            System.out.println(new AsciiWriter(performanceTree).write());

            Holder<VirtualFile> summaryFileHolder = new Holder<>();
            RunnableUtils.runWriteActionAndWait(() -> {
                try {
                    VirtualFile directory = files.get(0).getParent();
                    int index = getFreeIndex(directory);

                    VirtualFile newSummaryFile = directory.createChildData(this, getSummaryFileName(index));

                    ObjectOutputStream oos = new ObjectOutputStream(newSummaryFile.getOutputStream(this));
                    oos.writeObject(performanceTree);
                    oos.close();
                    MidPointUtils.publishNotification(NOTIFICATION_KEY, getTaskTitle(),
                            "Summary written to " + newSummaryFile.getCanonicalPath(), NotificationType.INFORMATION);
                    summaryFileHolder.setValue(newSummaryFile);
                } catch (IOException e) {
                    processException("Couldn't write summary file", e, mm, es);
                }
            });

            if (project != null && summaryFileHolder.getValue() != null) {
                ApplicationManager.getApplication().invokeAndWait(() -> MidPointUtils.openFile(project, summaryFileHolder.getValue()));
            }
        }
    }

    private int getFreeIndex(VirtualFile directory) {
        int index = 0;
        VirtualFile existingSummaryFile;
        do {
            existingSummaryFile = directory.findChild(getSummaryFileName(++index));
        } while (existingSummaryFile != null);
        return index;
    }

    private String getSummaryFileName(int index) {
        return String.format("_summary%d.perf-sum", index);
    }

    private void processException(String msg, Exception ex, MidPointService mm, EnvironmentService es) {
        Environment env = es.getSelected();
        mm.printToConsole(env, getClass(), msg + ". Reason: " + ex.getMessage());
        MidPointUtils.publishExceptionNotification(env, getClass(), NOTIFICATION_KEY, msg, ex);
    }

    private List<VirtualFile> getFilesToProcess(AnActionEvent evt) {
        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));
        if (selectedFiles == null || selectedFiles.length == 0) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, getTaskTitle(),
                    "No files selected for summarization", NotificationType.WARNING);
            return emptyList();
        }

        List<VirtualFile> toProcess = MidPointUtils.filterZipFiles(selectedFiles);
        if (toProcess.isEmpty()) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, getTaskTitle(),
                    "No files matched for summarization (zip)", NotificationType.WARNING);
        }
        return toProcess;
    }
}
