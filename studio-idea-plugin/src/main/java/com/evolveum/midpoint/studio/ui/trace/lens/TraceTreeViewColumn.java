package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.treetable.CellStyle;
import com.evolveum.midpoint.studio.ui.treetable.DefaultColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatPercent;
import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 * Column definitions for the main trace operation tree.
 */
public enum TraceTreeViewColumn implements ColumnDefinition<OpNode> {

    OPERATION_NAME("Operation", TreeTableModel.class, 500, OpNode::getLabel),
    CLOCKWORK_STATE("State", 60, OpNode::getClockworkState),
    EXECUTION_WAVE("EW", 35, OpNode::getExecutionWave),
    STATUS("Status", 100, o -> o.getResult().getStatus()),
    IMPORTANCE("W", 20, OpNode::getImportanceSymbol),
    START("Start", 60, o -> {
        long start = o.getStart(0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
        return df.format(new Date(start));
    }),
    TIME("Time", 80, o -> formatTime(o.getResult().getMicroseconds())),
    TYPE("Type", 100, o -> o.getType().toString()),
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

    private final Function<OpNode, Object> value;

    /**
     * True for columns that display counts or times — their value is styled
     * as disabled (greyed out) when the rendered string represents zero.
     */
    private final boolean disableIfZero;

    TraceTreeViewColumn(String name, int size, Function<OpNode, Object> value) {
        this(name, String.class, size, value);
    }

    TraceTreeViewColumn(String name, Class<?> type, int size, Function<OpNode, Object> value) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.value = value;
        this.disableIfZero = name.endsWith("#") || name.endsWith("time") || name.equals("Log");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Function<OpNode, Object> getValue() {
        return value;
    }

    @Override
    public javax.swing.table.TableCellRenderer getTableCellRenderer() {
        return null;
    }

    /**
     * Builds a {@link DefaultColumnInfo} for use with {@link com.evolveum.midpoint.studio.ui.treetable.DefaultTreeTable}.
     * Columns that display counts or times are styled with the disabled foreground color when their
     * value represents zero, replacing the old HTML color-string approach.
     */
    public DefaultColumnInfo<OpNode, Object> toColumnInfo() {
        DefaultColumnInfo<OpNode, Object> ci = new DefaultColumnInfo<>(name, type, value);
        ci.preferredWidth(size);

        if (disableIfZero) {
            ci.style(node -> isZeroValue(value.apply(node)) ? CellStyle.disabled() : null);
        }

        return ci;
    }

    private static boolean isZeroValue(Object val) {
        return "0".equals(val) || "0ms".equals(val) || "0.0%".equals(val);
    }

    private static String intValue(int value) {
        return String.valueOf(value);
    }
}
