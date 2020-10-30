package com.evolveum.midpoint.studio.impl.performance.output;

import com.evolveum.midpoint.studio.impl.performance.OperationPerformance;
import com.evolveum.midpoint.studio.impl.performance.OperationStatistics;
import com.evolveum.midpoint.studio.impl.performance.PerformanceTree;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.evolveum.midpoint.studio.impl.performance.output.Formatting.Alignment.LEFT;
import static com.evolveum.midpoint.studio.impl.performance.output.Formatting.Alignment.RIGHT;

/**
 * TODO unify with midPoint code
 */
public class AsciiWriter {

    private final PerformanceTree tree;

    public AsciiWriter(PerformanceTree tree) {
        this.tree = tree;
    }

    public String write() {
        Formatting formatting = createFormatting();
        Data data = createData();
        return formatting.apply(data);
    }

    private Formatting createFormatting() {
        AsciiTableFormatting f = new AsciiTableFormatting();
        f.addColumn("Operation", LEFT, "%s");
        f.addColumn("Presence", RIGHT, "%,d");
        f.addColumn("%", RIGHT, "%,.2f");
        f.addColumn("Invocations", RIGHT, "%,d");
        f.addColumn("Inv/sample", RIGHT, "%,.1f");
        f.addColumn("Time/sample (ms)", RIGHT, "%,.2f");
        f.addColumn("Own time/sample (ms)", RIGHT, "%,.2f");
        f.addColumn("Min", RIGHT, "%,.2f");
        f.addColumn("Max", RIGHT, "%,.2f");
        f.addColumn("Time/presence (ms)", RIGHT, "%,.2f");
        f.addColumn("Own time/presence (ms)", RIGHT, "%,.2f");
        return f;
    }

    private Data createData() {
        Data data = new Data();
        addToData(data, tree.getRoot(), 0);
        return data;
    }

    private void addToData(Data data, OperationPerformance operation, int indent) {
        int samples = tree.getSamples();
        Data.Record record = data.createRecord();
        record.add(StringUtils.repeat(' ', indent) + operation.getKey().getFormattedName());
        OperationStatistics stat = operation.getOperationStatistics();
        record.add(stat.getPresence());
        record.add((double) stat.getPresence() / samples);
        record.add(stat.getInvocations());
        record.add((double) stat.getInvocations() / samples);
        record.add(stat.getAvgTimeOverall() / 1000.0);
        record.add(stat.getAvgOwnTimeOverall() / 1000.0);
        record.add(stat.getMinTime() != null ? stat.getMinTime() / 1000.0 : null);
        record.add(stat.getMaxTime() != null ? stat.getMaxTime() / 1000.0 : null);
        record.add(stat.getAvgTimeWhenPresent() / 1000.0);
        record.add(stat.getAvgOwnTimeWhenPresent() / 1000.0);
        record.add(stat.hasUnknownDurations() ? "Unknown durations" : "");
        operation.getChildren().forEach(child -> addToData(data, child, indent + 1));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File summary = new File("C:\\midpoint\\home\\scale1\\trace\\run-3-optimal\\summary.perf-sum");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(summary));
        PerformanceTree tree = (PerformanceTree) ois.readObject();
        ois.close();

        AsciiWriter writer = new AsciiWriter(tree);
        System.out.println(writer.write());
    }
}
