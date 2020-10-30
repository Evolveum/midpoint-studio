package com.evolveum.midpoint.studio.ui.performance.mainTree;

import com.evolveum.midpoint.schema.traces.PerformanceCategory;
import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.evolveum.midpoint.studio.ui.common.ColumnDefinition;
import com.evolveum.midpoint.studio.ui.trace.DisplayUtil;
import com.evolveum.midpoint.studio.ui.common.RightAlignTableCellRenderer;

import javax.swing.table.TableCellRenderer;
import java.util.Locale;
import java.util.function.Function;

/**
 *
 */
public enum PerformanceTreeViewColumn implements ColumnDefinition<OperationPerformance> {

    OPERATION_NAME("Operation", null, 500, PerformanceTreeViewColumn::getFormattedName),
    INVOCATIONS("Invocations", null, 35, PerformanceTreeViewColumn::getInvocations, RightAlignTableCellRenderer.INSTANCE),
    INVOCATIONS_PER_SAMPLE("Inv/sample", "Invocations per sample", 35, PerformanceTreeViewColumn::getInvocationsPerSample, RightAlignTableCellRenderer.INSTANCE),
    TIME_PER_SAMPLE("Time/sample", "Average time per sample", 35, PerformanceTreeViewColumn::getTimePerSample, RightAlignTableCellRenderer.INSTANCE),
    PERCENT_OF_PARENT("% of parent", "Percent of the parent operation", 35, PerformanceTreeViewColumn::getPercentOfParent, RightAlignTableCellRenderer.INSTANCE),
    PERCENT_OF_ROOT("% of root", "Percent of the root operation", 35, PerformanceTreeViewColumn::getPercentOfRoot, RightAlignTableCellRenderer.INSTANCE),
    OWN_TIME_PER_SAMPLE("Own time", "Average own time per sample", 35, PerformanceTreeViewColumn::getOwnTimePerSample, RightAlignTableCellRenderer.INSTANCE),
    TIME_PER_INVOCATION("Time/inv", "Average time per invocation", 35, PerformanceTreeViewColumn::getTimePerInvocation, RightAlignTableCellRenderer.INSTANCE),
    CPU_TIME_PER_SAMPLE("CTime/sample", "Average CPU time per sample", 35, PerformanceTreeViewColumn::getCpuTimePerSample, RightAlignTableCellRenderer.INSTANCE),
    OWN_CPU_TIME_PER_SAMPLE("Own ctime", "Average own CPU time per sample", 35, PerformanceTreeViewColumn::getOwnCpuTimePerSample, RightAlignTableCellRenderer.INSTANCE),
    CPU_TIME_PER_INVOCATION("CTime/inv", "Average CPU time per invocation", 35, PerformanceTreeViewColumn::getCpuTimePerInvocation, RightAlignTableCellRenderer.INSTANCE),
    PRESENCE("Presence", "Number of samples containing the operation", 35, PerformanceTreeViewColumn::getPresence, RightAlignTableCellRenderer.INSTANCE),
    PRESENCE_PERCENT("Presence %", "Percentage of samples containing the operation", 35, PerformanceTreeViewColumn::getPresencePercent, RightAlignTableCellRenderer.INSTANCE),
    TIME_PER_PRESENCE("Time/presence", "Average time when present", 35, PerformanceTreeViewColumn::getTimePerPresence, RightAlignTableCellRenderer.INSTANCE),
    OWN_TIME_PER_PRESENCE("Own/presence", "Average own time when present", 35, PerformanceTreeViewColumn::getOwnTimePerPresence, RightAlignTableCellRenderer.INSTANCE),

    REPO_COUNT("Repo #", null, 70, o -> getCountFor(o, PerformanceCategory.REPOSITORY), RightAlignTableCellRenderer.INSTANCE),
    REPO_TIME("Repo", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.REPOSITORY), RightAlignTableCellRenderer.INSTANCE),
    REPO_R_COUNT("Repo:R #", null, 70, o -> getCountFor(o, PerformanceCategory.REPOSITORY_READ), RightAlignTableCellRenderer.INSTANCE),
    REPO_R_TIME("Repo:R", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.REPOSITORY_READ), RightAlignTableCellRenderer.INSTANCE),
    REPO_W_COUNT("Repo:W #", null, 70, o -> getCountFor(o, PerformanceCategory.REPOSITORY_WRITE), RightAlignTableCellRenderer.INSTANCE),
    REPO_W_TIME("Repo:W", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.REPOSITORY_WRITE), RightAlignTableCellRenderer.INSTANCE),
    REPO_CACHE_COUNT("RCache #", null, 70, o -> getCountFor(o, PerformanceCategory.REPOSITORY_CACHE), RightAlignTableCellRenderer.INSTANCE),
    REPO_CACHE_TIME("RCache", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.REPOSITORY_CACHE), RightAlignTableCellRenderer.INSTANCE),
    MAP_COUNT("Map #", null, 70, o -> getCountFor(o, PerformanceCategory.MAPPING_EVALUATION), RightAlignTableCellRenderer.INSTANCE),
    MAP_TIME("Map", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.MAPPING_EVALUATION), RightAlignTableCellRenderer.INSTANCE),
    NOTIFICATIONS_COUNT("Not #", null, 80, o -> getCountFor(o, PerformanceCategory.NOTIFICATIONS), RightAlignTableCellRenderer.INSTANCE),
    NOTIFICATIONS_TIME("Not", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.NOTIFICATIONS), RightAlignTableCellRenderer.INSTANCE),
    NOTIFICATION_TRANSPORTS_COUNT("NTrans #", null, 80, o -> getCountFor(o, PerformanceCategory.NOTIFICATION_TRANSPORTS), RightAlignTableCellRenderer.INSTANCE),
    NOTIFICATION_TRANSPORTS_TIME("NTrans", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.NOTIFICATION_TRANSPORTS), RightAlignTableCellRenderer.INSTANCE),
    AUDIT_COUNT("Audit #", null, 80, o -> getCountFor(o, PerformanceCategory.AUDIT), RightAlignTableCellRenderer.INSTANCE),
    AUDIT_TIME("Audit", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.AUDIT), RightAlignTableCellRenderer.INSTANCE),
    ICF_COUNT("ICF #", null, 70, o -> getCountFor(o, PerformanceCategory.ICF), RightAlignTableCellRenderer.INSTANCE),
    ICF_TIME("ICF", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.ICF), RightAlignTableCellRenderer.INSTANCE),
    ICF_R_COUNT("ICF:R #", null, 70, o -> getCountFor(o, PerformanceCategory.ICF_READ), RightAlignTableCellRenderer.INSTANCE),
    ICF_R_TIME("ICF:R", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.ICF_READ), RightAlignTableCellRenderer.INSTANCE),
    ICF_W_COUNT("ICF:W #", null, 70, o -> getCountFor(o, PerformanceCategory.ICF_WRITE), RightAlignTableCellRenderer.INSTANCE),
    ICF_W_TIME("ICF:W", null, 80, o -> getFormattedTimeFor(o, PerformanceCategory.ICF_WRITE), RightAlignTableCellRenderer.INSTANCE),

    EXTERNAL_TIME("Ext", "External processing time", 80, o -> getFormattedTimeFor(o, PerformanceCategory.EXTERNAL), RightAlignTableCellRenderer.INSTANCE),
    EXTERNAL_PLUS_REPO_CACHE_TIME("Ext+RC", "External processing time (incl. repo cache)", 80, o -> getFormattedTimeFor(o, PerformanceCategory.EXTERNAL_PLUS_REPO_CACHE), RightAlignTableCellRenderer.INSTANCE),
    INTERNAL_TIME("Int", "Internal processing time", 80, o -> getFormattedTime(o, PerformanceTreeViewColumn::getInternalTime), RightAlignTableCellRenderer.INSTANCE),
    INTERNAL_TIME_PERCENT("Int%", "Internal processing time (%)", 80, o -> getFormattedPercent(getInternalTimeRatio(o)), RightAlignTableCellRenderer.INSTANCE),
    INTERNAL_MINUS_REPO_CACHE_TIME("Int-RC", "Internal processing time (excl. repo cache)", 80, o -> getFormattedTime(o, PerformanceTreeViewColumn::getInternalTimeMinusCache), RightAlignTableCellRenderer.INSTANCE),
    INTERNAL_MINUS_REPO_CACHE_TIME_PERCENT("Int-RC%", "Internal processing time (excl. repo cache) (%)", 80, o -> getFormattedPercent(getInternalTimeMinusCacheRatio(o)), RightAlignTableCellRenderer.INSTANCE),
    REPO_CACHE_OVERHEAD("RC OH", "Repository cache overhead", 80, o -> getFormattedTime(o, PerformanceTreeViewColumn::getRCacheOverhead), RightAlignTableCellRenderer.INSTANCE);

    private static long getInternalTime(OperationPerformance o1) {
        return o1.getOperationStatistics().getTotalTime() - getTimeFor(o1, PerformanceCategory.EXTERNAL);
    }

    private static long getInternalTimeMinusCache(OperationPerformance o1) {
        return o1.getOperationStatistics().getTotalTime() - getTimeFor(o1, PerformanceCategory.EXTERNAL_PLUS_REPO_CACHE);
    }

    private static String getFormattedTimeFor(OperationPerformance o, PerformanceCategory category) {
        return getFormattedTime(o, timeFor(category));
    }

    private static Function<OperationPerformance, Long> timeFor(PerformanceCategory category) {
        return o -> o.getPerformanceByCategory().get(category).getTotalTime();
    }

    private static long getTimeFor(OperationPerformance o, PerformanceCategory category) {
        return o.getPerformanceByCategory().get(category).getTotalTime();
    }

    private static String getFormattedTime(OperationPerformance o, Function<OperationPerformance, Long> timeFunction) {
        return coloredTimePerSample(timeFunction.apply(o), getSamples(o));
    }

    private static String getCountFor(OperationPerformance o, PerformanceCategory repository) {
        return coloredIntPerSample(o.getPerformanceByCategory().get(repository).getTotalCount(), getSamples(o));
    }

    private final String label;
    private final String description;
    private final int size;
    private final Function<OperationPerformance, String> formatter;
    private final TableCellRenderer tableCellRenderer;

    PerformanceTreeViewColumn(String label, String description, int size, Function<OperationPerformance, String> formatter, TableCellRenderer renderer) {
        this.label = label;
        this.description = description;
        this.size = size;
        this.formatter = formatter;
        this.tableCellRenderer = renderer;
    }

    PerformanceTreeViewColumn(String label, String description, int size, Function<OperationPerformance, String> formatter) {
        this(label, description, size, formatter, null);
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
    public Function<OperationPerformance, String> getFormatter() {
        return formatter;
    }

    @Override
    public TableCellRenderer getTableCellRenderer() {
        return tableCellRenderer;
    }
    
    private static String coloredIntPerSample(int value, int samples) {
        String text = String.format("%,.2f", (double) value / samples);
        if (value != 0) {
            return text;
        } else {
            return DisplayUtil.makeDisabled(text);
        }
    }

    private static String coloredTimePerSample(long value, int samples) {
        String text = String.format("%,.2f", (double) value / samples / 1000.0);
        if (value != 0) {
            return text;
        } else {
            return DisplayUtil.makeDisabled(text);
        }
    }

    private static String getFormattedName(OperationPerformance op) {
        return op.getKey().getFormattedName();
    }

    private static String getInvocations(OperationPerformance op) {
        return String.format(Locale.US, "%,d", op.getOperationStatistics().getInvocations());
    }

    private static String getInvocationsPerSample(OperationPerformance op) {
        return String.format(Locale.US, "%,.1f", (double) op.getOperationStatistics().getInvocations() / getSamples(op));
    }

    private static String getTimePerSample(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", op.getOperationStatistics().getAvgTimeOverall() / 1000.0);
    }

    private static String getCpuTimePerSample(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", op.getOperationStatistics().getTotalCpuTime() / getSamples(op) / 1000.0);
    }

    private static String getPercentOfParent(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", 100.0 * op.getOperationStatistics().getRatioOfParent());
    }

    private static String getPercentOfRoot(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", 100.0 * op.getOperationStatistics().getRatioOfRoot());
    }

    private static String getTimePerInvocation(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", (double) op.getOperationStatistics().getTotalTime() / op.getOperationStatistics().getInvocations() / 1000.0);
    }

    private static String getCpuTimePerInvocation(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", (double) op.getOperationStatistics().getTotalCpuTime() / op.getOperationStatistics().getInvocations() / 1000.0);
    }

    private static String getPresence(OperationPerformance op) {
        return String.format(Locale.US, "%,d", op.getOperationStatistics().getPresence());
    }

    private static String getPresencePercent(OperationPerformance op) {
        return String.format(Locale.US, "%5.1f", 100.0 * op.getOperationStatistics().getPresence() / getSamples(op));
    }

    private static String getOwnTimePerSample(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", op.getOperationStatistics().getAvgOwnTimeOverall() / 1000.0);
    }

    private static String getOwnCpuTimePerSample(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", op.getOperationStatistics().getOwnCpuTime() / getSamples(op) / 1000.0);
    }

    private static String getTimePerPresence(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", op.getOperationStatistics().getAvgTimeWhenPresent() / 1000.0);
    }

    private static String getOwnTimePerPresence(OperationPerformance op) {
        return String.format(Locale.US, "%,.2f", op.getOperationStatistics().getAvgOwnTimeWhenPresent() / 1000.0);
    }

    private String getMinTime(OperationPerformance op) {
        Long minTime = op.getOperationStatistics().getMinTime();
        if (minTime != null) {
            return String.format(Locale.US, "%,.2f", minTime / 1000.0);
        } else {
            return "";
        }
    }

    private String getMaxTime(OperationPerformance op) {
        Long maxTime = op.getOperationStatistics().getMaxTime();
        if (maxTime != null) {
            return String.format(Locale.US, "%,.2f", maxTime / 1000.0);
        } else {
            return "";
        }
    }

    private static int getSamples(OperationPerformance op) {
        return op != null ? op.getTree().getSamples() : 0;
    }

    private static String getFormattedPercent(double value) {
        return String.format(Locale.US, "%,.2f", 100.0 * value);
    }

    private static double getInternalTimeRatio(OperationPerformance o) {
        return (double) getInternalTime(o) / o.getOperationStatistics().getTotalTime();
    }

    private static double getInternalTimeMinusCacheRatio(OperationPerformance o) {
        return (double) getInternalTimeMinusCache(o) / o.getOperationStatistics().getTotalTime();
    }

    private static long getRCacheOverhead(OperationPerformance o) {
        return o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalTime() -
                o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalTime();
    }

//    private static String getRCacheOverheadPercent(OperationPerformance o) {
//        long base = o.getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalTime();
//        if (base > 0) {
//            return String.format(Locale.US, "%,.2f", 100.0 * getRCacheOverheadValue(o) / base);
//        } else {
//            return "0.00";
//        }
//    }
}
