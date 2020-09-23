package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.studio.ui.trace.TraceViewEditor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceService {

    private Project project;

    private OpViewType opViewType;

    private Options options;

    public TraceService(@NotNull Project project) {
        this.project = project;

        this.opViewType = OpViewType.ALL;

        this.options = createOptions(opViewType);
    }

    public static TraceService getInstance(@NotNull Project project) {
        return project.getService(TraceService.class);
    }

    public OpViewType getOpViewType() {
        return opViewType;
    }

    public void setOpViewType(OpViewType opViewType) {
        this.opViewType = opViewType;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;

        FileEditorManager fem = FileEditorManager.getInstance(project);

        for (FileEditor editor : fem.getAllEditors()) {
            if (!(editor instanceof TraceViewEditor)) {
                continue;
            }

            TraceViewEditor traceViewEditor = (TraceViewEditor) editor;
            traceViewEditor.applyOptions(options);
        }
    }

    private Options createOptions(OpViewType opViewType) {
        Options options = new Options();

        for (OpType op : OpType.values()) {
            if (opViewType.getTypes() == null || opViewType.getTypes().contains(op)) {
                options.getTypesToShow().add(op);
            }
        }

        for (PerformanceCategory pc : PerformanceCategory.values()) {
            if (opViewType.getCategories() == null || opViewType.getCategories().contains(pc)) {
                options.getCategoriesToShow().add(pc);
            }
        }

        options.setShowAlsoParents(opViewType.isShowAlsoParents());
        options.setShowPerformanceColumns(opViewType.isShowPerformanceColumns());
        options.setShowReadWriteColumns(opViewType.isShowReadWriteColumns());

        return options;
    }
}
