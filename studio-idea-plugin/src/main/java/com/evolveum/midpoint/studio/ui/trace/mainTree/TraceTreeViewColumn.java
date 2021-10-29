package com.evolveum.midpoint.studio.ui.trace.mainTree;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.ui.common.ColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.DisplayUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MonitoredOperationStatisticsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MonitoredOperationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MonitoredOperationsStatisticsType;

import javax.swing.table.TableCellRenderer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import static com.evolveum.midpoint.studio.util.MidPointUtils.*;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.MonitoredOperationType.*;

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
    TIME("Time", 80, o -> formatTimePrecise(o.getResult().getMicroseconds())),
    TYPE("Type", 100, o -> o.getType().toString()),
    CLONE_COUNT("Clone#", 70, o -> formatMonitoredOpCount(o, CLONE)),
    CLONE_TIME("Clone time", 80, o -> formatMonitoredOpTime(o, CLONE)),
    PARSING_COUNT("Parsing#", 70, o -> formatMonitoredOpCount(o, OBJECT_PARSING)),
    PARSING_TIME("Parsing time", 80, o -> formatMonitoredOpTime(o, OBJECT_PARSING)),
    SERIALIZATION_COUNT("Ser#", 70, o -> formatMonitoredOpCount(o, OBJECT_SERIALIZATION)),
    SERIALIZATION_TIME("Ser time", 80, o -> formatMonitoredOpTime(o, OBJECT_SERIALIZATION)),
    CURRENT_DELTA_COUNT("CurΔ#", 70, o -> formatMonitoredOpCount(o, CURRENT_DELTA_COMPUTATION)),
    CURRENT_DELTA_TIME("CurΔ time", 80, o -> formatMonitoredOpTime(o, CURRENT_DELTA_COMPUTATION)),
    SUMMARY_DELTA_COUNT("SumΔ#", 70, o -> formatMonitoredOpCount(o, SUMMARY_DELTA_COMPUTATION)),
    SUMMARY_DELTA_TIME("SumΔ time", 80, o -> formatMonitoredOpTime(o, SUMMARY_DELTA_COMPUTATION)),
    NEW_OBJECT_COUNT("NewObj#", 70, o -> formatMonitoredOpCount(o, NEW_OBJECT_COMPUTATION)),
    NEW_OBJECT_TIME("NewObj time", 80, o -> formatMonitoredOpTime(o, NEW_OBJECT_COMPUTATION)),
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

    private final String label;
    private final String description = null; // TODO
    private final int size;
    private final Function<OpNode, String> formatter;
    private final TableCellRenderer tableCellRenderer;

    TraceTreeViewColumn(String label, int size, Function<OpNode, String> formatter) {
        this.label = label;
        this.size = size;
        this.formatter = formatter;
        this.tableCellRenderer = null;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
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

    private static String coloredTimePrecise(long value) {
        if (value == 0) {
            return DisplayUtil.makeDisabled(formatTimePrecise(value));
        } else {
            return formatTime(value);
        }
    }

    private static String formatMonitoredOpCount(OpNode o, MonitoredOperationType operation) {
        MonitoredOperationStatisticsType op = findMonitoredOperation(o, operation);
        if (op == null || op.getCount() == null) {
            return coloredInt(0);
        } else {
            return coloredInt(op.getCount());
        }
    }

    private static String formatMonitoredOpTime(OpNode o, MonitoredOperationType operation) {
        MonitoredOperationStatisticsType op = findMonitoredOperation(o, operation);
        if (op == null || op.getNanos() == null) {
            return coloredTimePrecise(0);
        } else {
            return coloredTimePrecise(op.getNanos() / 1000);
        }
    }

    private static MonitoredOperationStatisticsType findMonitoredOperation(OpNode opNode, MonitoredOperationType operation) {
        MonitoredOperationsStatisticsType ops = opNode.getResult().getMonitoredOperations();
        if (ops == null) {
            return null;
        }
        for (MonitoredOperationStatisticsType op : ops.getOperation()) {
            if (op.getOperation() == operation) {
                return op;
            }
        }
        return null;
    }

}
