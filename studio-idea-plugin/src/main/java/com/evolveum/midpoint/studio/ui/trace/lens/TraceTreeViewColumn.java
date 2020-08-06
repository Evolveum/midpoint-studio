package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.trace.ZeroSensitiveTableCellRenderer;

import javax.swing.table.TableCellRenderer;
import java.util.function.Function;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatPercent;
import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 *
 */
public enum TraceTreeViewColumn implements ColumnDefinition<OpNode> {

    OPERATION_NAME("Operation", 500, OpNode::getOperationNameFormatted, null),
    CLOCKWORK_STATE("State", 60, OpNode::getClockworkState, null),
    EXECUTION_WAVE("EW", 35, OpNode::getExecutionWave, null),
    STATUS("Status", 100, o -> o.getResult().getStatus().toString(), null),
    IMPORTANCE("W", 20, OpNode::getImportanceSymbol, null),
    START("Start", 60, o -> Long.toString(o.getStart(0)), null),
    TIME("Time", 80, o -> formatTime(o.getResult().getMicroseconds()), null),
    TYPE("Type", 100, o -> o.getType().toString(), null),
    OVERHEAD("OH", 50, o -> formatPercent(o.getOverhead()), null),
    OVERHEAD2("OH2", 50, o -> formatPercent(o.getOverhead2()), null),
    REPO_COUNT("Repo #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_TIME("Repo time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_R_COUNT("Repo:R #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_READ).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_R_TIME("Repo:R time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_READ).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_W_COUNT("Repo:W #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_WRITE).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_W_TIME("Repo:W time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_WRITE).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_CACHE_COUNT("RCache #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    REPO_CACHE_TIME("RCache time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    MAP_COUNT("Map #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.MAPPING_EVALUATION).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    MAP_TIME("Map time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.MAPPING_EVALUATION).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    ICF_COUNT("ICF #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    ICF_TIME("ICF time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    ICF_R_COUNT("ICF:R #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.ICF_READ).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    ICF_R_TIME("ICF:R time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF_READ).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    ICF_W_COUNT("ICF:W #", 70, o -> String.valueOf(o.getPerformanceByCategory().get(PerformanceCategory.ICF_WRITE).getTotalCount()), ZeroSensitiveTableCellRenderer.INSTANCE),
    ICF_W_TIME("ICF:W time", 80, o -> formatTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF_WRITE).getTotalTime()), ZeroSensitiveTableCellRenderer.INSTANCE),
    LOG_ENTRIES("Log", 50, o -> Integer.toString(o.getLogEntriesCount()), ZeroSensitiveTableCellRenderer.INSTANCE);

    private final String name;
    private final int size;
    private final Function<OpNode, String> formatter;
    private final TableCellRenderer tableCellRenderer;

    TraceTreeViewColumn(String name, int size, Function<OpNode, String> formatter, TableCellRenderer tableCellRenderer) {
        this.name = name;
        this.size = size;
        this.formatter = formatter;
        this.tableCellRenderer = tableCellRenderer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Function<OpNode, String> getFormatter() {
        return formatter;
    }

    @Override
    public TableCellRenderer getTableCellRenderer() {
        return tableCellRenderer;
    }
}
