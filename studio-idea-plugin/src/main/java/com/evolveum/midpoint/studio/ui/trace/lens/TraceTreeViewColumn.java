package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.OpType;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.LocalizationService;
import com.evolveum.midpoint.studio.ui.treetable.Style;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatPercent;
import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 * Column definitions for the main trace operation tree.
 */
public enum TraceTreeViewColumn {

    OPERATION_NAME(
            "Operation",
            TreeTableModel.class,
            600,
            OpNode::getLabel),
    CLOCKWORK_STATE("State", 100, OpNode::getClockworkState),
    EXECUTION_WAVE("EW", 50, OpNode::getExecutionWave),
    STATUS(
            "Status",
            150,
            o -> LocalizationService.get().translate(o.getResult().getStatus()),
            TraceTreeViewColumn::getOperationResultStatusCellStyle
    ),
    IMPORTANCE("W", 20, OpNode::getImportanceSymbol),
    START("Start", 250, o -> {
        long start = o.getStart(0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        return df.format(new Date(start));
    }),
    TIME("Time", 80, o -> formatTime(o.getResult().getMicroseconds())),
    TYPE("Type", 150, o -> LocalizationService.get().translate(o.getType())),
    OVERHEAD("OH", 50, o -> formatPercent(o.getOverhead())),
    OVERHEAD2("OH2", 50, o -> formatPercent(o.getOverhead2())),
    REPO_COUNT("Repo #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalCount())),
    REPO_TIME("Repo time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalTime())),
    REPO_R_COUNT("Repo:R #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_READ).getTotalCount())),
    REPO_R_TIME("Repo:R time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_READ).getTotalTime())),
    REPO_W_COUNT("Repo:W #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_WRITE).getTotalCount())),
    REPO_W_TIME("Repo:W time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_WRITE).getTotalTime())),
    REPO_CACHE_COUNT("RCache #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalCount())),
    REPO_CACHE_TIME("RCache time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalTime())),
    MAP_COUNT("Map #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.MAPPING_EVALUATION).getTotalCount())),
    MAP_TIME("Map time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.MAPPING_EVALUATION).getTotalTime())),
    ICF_COUNT("ICF #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalCount())),
    ICF_TIME("ICF time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalTime())),
    ICF_R_COUNT("ICF:R #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.ICF_READ).getTotalCount())),
    ICF_R_TIME("ICF:R time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF_READ).getTotalTime())),
    ICF_W_COUNT("ICF:W #", 70, o -> intValue(o.getPerformanceByCategory().get(PerformanceCategory.ICF_WRITE).getTotalCount())),
    ICF_W_TIME("ICF:W time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF_WRITE).getTotalTime())),
    LOG_ENTRIES("Log", 50, o -> intValue(o.getLogEntriesCount()));

    private final String name;

    private final Class<?> type;

    private final int size;

    private final Function<OpNode, Object> valueProvider;

    private final Function<OpNode, Style> cellStyleProvider;

    /**
     * True for columns that display counts or times — their value is styled
     * as disabled (greyed out) when the rendered string represents zero.
     */
    private final boolean disableIfZero;

    TraceTreeViewColumn(String name, int size, Function<OpNode, Object> valueProvider) {
        this(name, String.class, size, valueProvider);
    }

    TraceTreeViewColumn(String name, Class<?> type, int size, Function<OpNode, Object> valueProvider) {
        this(name, type, size, valueProvider, null);
    }

    TraceTreeViewColumn(String name, int size, Function<OpNode, Object> valueProvider,
                        Function<OpNode, Style> cellStyleProvider) {
        this(name, String.class, size, valueProvider, cellStyleProvider);
    }

    TraceTreeViewColumn(String name, Class<?> type, int size, Function<OpNode, Object> valueProvider,
                        Function<OpNode, Style> cellStyleProvider) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.valueProvider = valueProvider;
        this.cellStyleProvider = cellStyleProvider;
        this.disableIfZero = name.endsWith("#") || name.endsWith("time") || name.equals("Log");
    }

    public String getName() {
        return name;
    }

    /**
     * Builds a {@link DefaultColumnInfo} for use with {@link com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable}.
     * Columns that display counts or times are styled with the disabled foreground color when their
     * value represents zero, replacing the old HTML color-string approach.
     */
    public DefaultColumnInfo<OpNode, Object> toColumnInfo() {
        DefaultColumnInfo<OpNode, Object> ci = new DefaultColumnInfo<>(name, type, valueProvider);
        ci.preferredWidth(size);

        ci.style(cellStyleProvider);

        // todo get rid of this
        if (disableIfZero) {
            ci.style(node -> isZeroValue(valueProvider.apply(node)) ? Style.disabled() : null);
        }

        return ci;
    }

    private static boolean isZeroValue(Object val) {
        return "0".equals(val) || "0ms".equals(val) || "0.0%".equals(val);
    }

    private static String intValue(int value) {
        return String.valueOf(value);
    }

    private static Style getOperationResultStatusCellStyle(OpNode node) {
        if (node == null || node.getResult() == null) {
            return null;
        }

        OperationResultStatusType status = node.getResult().getStatus();
        if (status == null) {
            return null;
        }

        return switch (status) {
            case SUCCESS -> Style.success();
            case FATAL_ERROR, PARTIAL_ERROR, HANDLED_ERROR -> Style.error();
            default -> null;
        };
    }
}
