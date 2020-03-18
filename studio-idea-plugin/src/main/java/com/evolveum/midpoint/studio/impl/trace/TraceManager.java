package com.evolveum.midpoint.studio.impl.trace;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TraceManager {

    private OpViewType opViewType;

    private Options options;

    public TraceManager() {
        opViewType = OpViewType.ALL;

        options = createOptions(opViewType);
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
