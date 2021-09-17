package com.evolveum.midpoint.studio.ui.trace.lens;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.trace.DisplayUtil;

import javax.swing.table.TableCellRenderer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import static com.evolveum.midpoint.studio.util.MidPointUtils.formatPercent;
import static com.evolveum.midpoint.studio.util.MidPointUtils.formatTime;

/**
 *
 */
public enum TraceTreeViewColumn implements ColumnDefinition<OpNode> {

    OPERATION_NAME("Operation", 500, OpNode::getLabel),
    CLOCKWORK_STATE("State", 60, OpNode::getClockworkState),
    EXECUTION_WAVE("EW", 35, OpNode::getExecutionWave),
    STATUS("Status", 100, o -> o.getResult().getStatus().toString()),
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
    REPO_COUNT("Repo #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalCount())),
    REPO_TIME("Repo time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalTime())),
    REPO_R_COUNT("Repo:R #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_READ).getTotalCount())),
    REPO_R_TIME("Repo:R time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_READ).getTotalTime())),
    REPO_W_COUNT("Repo:W #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_WRITE).getTotalCount())),
    REPO_W_TIME("Repo:W time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_WRITE).getTotalTime())),
    REPO_CACHE_COUNT("RCache #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalCount())),
    REPO_CACHE_TIME("RCache time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalTime())),
    MAP_COUNT("Map #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.MAPPING_EVALUATION).getTotalCount())),
    MAP_TIME("Map time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.MAPPING_EVALUATION).getTotalTime())),
    ICF_COUNT("ICF #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalCount())),
    ICF_TIME("ICF time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalTime())),
    ICF_R_COUNT("ICF:R #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.ICF_READ).getTotalCount())),
    ICF_R_TIME("ICF:R time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF_READ).getTotalTime())),
    ICF_W_COUNT("ICF:W #", 70, o -> coloredInt(o.getPerformanceByCategory().get(PerformanceCategory.ICF_WRITE).getTotalCount())),
    ICF_W_TIME("ICF:W time", 80, o -> coloredTime(o.getPerformanceByCategory().get(PerformanceCategory.ICF_WRITE).getTotalTime())),
    LOG_ENTRIES("Log", 50, o -> coloredInt(o.getLogEntriesCount()));

    private final String name;
    private final int size;
    private final Function<OpNode, String> formatter;
    private final TableCellRenderer tableCellRenderer;

    TraceTreeViewColumn(String name, int size, Function<OpNode, String> formatter) {
        this.name = name;
        this.size = size;
        this.formatter = formatter;
        this.tableCellRenderer = null; //tableCellRenderer;
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
    
    private static String coloredInt(int value) {
        return DisplayUtil.disableIfZero(value);
    }

    private static String coloredTime(long value) {
        if (value == 0) {
            return DisplayUtil.makeDisabled(formatTime(value));
        } else {
            return formatTime(value);
        }
    }
}
