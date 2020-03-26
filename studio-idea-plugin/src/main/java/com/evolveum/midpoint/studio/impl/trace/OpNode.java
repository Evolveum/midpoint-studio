package com.evolveum.midpoint.studio.impl.trace;

import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpNode {

    private final OperationResultType result;
    private final List<OpNode> children = new ArrayList<>();
    private final OpNode parent;
    private final OpResultInfo info;
    private final TraceInfo traceInfo;

    private boolean visible = true;

    public OpNode(OperationResultType result, OpResultInfo info, OpNode parent, TraceInfo traceInfo) {
        assert result != null;
        this.result = result;
        this.info = info;
        this.parent = parent;
        this.traceInfo = traceInfo;
    }

    public OperationResultType getResult() {
        return result;
    }

    public OperationsPerformanceInformationType getPerformance() {
        return info.getPerformance();
    }

    public Map<PerformanceCategory, PerformanceCategoryInfo> getPerformanceByCategory() {
        return info.getPerformanceByCategory();
    }

    public OpType getType() {
        return info.getType();
    }

    public List<OpNode> getChildren() {
        return children;
    }

    public OpNode getParent() {
        return parent;
    }

    public long getStart(long base) {
        return XmlTypeConverter.toMillis(result.getStart()) - base;
    }

    public String dump() {
        try {
            return OperationResult.createOperationResult(result).debugDump();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public TraceType getFirstTrace() {
        return result.getTrace().isEmpty() ? null : result.getTrace().get(0);
    }

    public String getOperationQualified() {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getOperation());
        if (!result.getQualifier().isEmpty()) {
            sb.append(" (");
            sb.append(String.join("; ", result.getQualifier()));
            sb.append(")");
        }
        return sb.toString();
    }

    public String getOperationNameFormatted() {
        return getType().getFormattedName(this) + (visible ? "" : "!");
    }


    public String getClockworkState() {
        ClockworkTraceType click = getTrace(ClockworkTraceType.class);
        if (click instanceof ClockworkClickTraceType && ((ClockworkClickTraceType) click).getState() != null) {
            return String.valueOf(((ClockworkClickTraceType) click).getState());
        } else if (click != null && click.getInputLensContext() != null && click.getInputLensContext().getState() != null) {
            return String.valueOf(click.getInputLensContext().getState());
        } else if (parent != null) {
            return parent.getClockworkState();
        } else {
            return "";
        }
    }

    public String getExecutionWave() {
        ClockworkTraceType click = getTrace(ClockworkTraceType.class);
        if (click instanceof ClockworkClickTraceType && ((ClockworkClickTraceType) click).getExecutionWave() != null) {
            return String.valueOf(((ClockworkClickTraceType) click).getExecutionWave());
        } else if (click != null && click.getInputLensContext() != null && click.getInputLensContext().getExecutionWave() != null) {
            return String.valueOf(click.getInputLensContext().getExecutionWave());
        } else if (parent != null) {
            return parent.getExecutionWave();
        } else {
            return "";
        }
    }

    public String getProjectionWave() {
        ClockworkTraceType click = getTrace(ClockworkTraceType.class);
        if (click instanceof ClockworkClickTraceType && ((ClockworkClickTraceType) click).getProjectionWave() != null) {
            return String.valueOf(((ClockworkClickTraceType) click).getProjectionWave());
        } else if (click != null && click.getInputLensContext() != null && click.getInputLensContext().getProjectionWave() != null) {
            return String.valueOf(click.getInputLensContext().getProjectionWave());
        } else if (parent != null) {
            return parent.getProjectionWave();
        } else {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getTrace(Class<T> aClass) {
        return TraceUtil.getTrace(result, aClass);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void applyOptions(Options options) {
        setVisible(isVisible(options));
        for (OpNode child : children) {
            child.applyOptions(options);
        }
    }

    private boolean isVisible(Options options) {
        if (options.getTypesToShow().contains(getType())) {
            return true;
        }
        for (PerformanceCategory cat : options.getCategoriesToShow()) {
            PerformanceCategoryInfo perfInfo = getPerformanceByCategory().get(cat);
            if (options.isShowAlsoParents()) {
                if (perfInfo.getTotalCount() > 0) {
                    return true;
                }
            } else {
                if (perfInfo.getOwnCount() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getImportanceSymbol() {
        if (result.getImportance() != null) {
            switch (result.getImportance()) {
                case MAJOR:
                    return "O";
                case NORMAL:
                    return "o";
                case MINOR:
                    return ".";
                default:
                    return "?";
            }
        } else return Boolean.TRUE.equals(result.isMinor()) ? "." : "o";
    }

    public LensContextType getContextToView() {
        List<TraceType> traces = result.getTrace();
        for (TraceType trace : traces) {
            if (trace instanceof ClockworkTraceType) {
                return ((ClockworkTraceType) trace).getOutputLensContext();
            }
        }
        return null;
    }

//    public List<ViewedObject> getObjectsToView() {
//        List<TraceType> traces = result.getTrace();
//        for (TraceType trace : traces) {
//            if (trace instanceof ClockworkTraceType) {
//                return processContext(((ClockworkTraceType) trace).getOutputLensContext());
//            }
//        }
//        return null;
//    }
//
//    public List<ViewedObject> processContext(LensContextType ctx) {
//        List<ViewedObject> rv = new ArrayList<ViewedObject>();
//        if (ctx != null && ctx.getFocusContext() != null) {
//            LensFocusContextType fctx = ctx.getFocusContext();
//            ObjectType objectOld = fctx.getObjectOld();
//            ObjectType objectCurrent = fctx.getObjectCurrent();
//            ObjectType objectNew = fctx.getObjectNew();
//            if (objectOld != null) {
//                rv.add(new ViewedObject("old", objectOld.asPrismObject()));
//            }
//            if (objectCurrent != null) {
//                rv.add(new ViewedObject("current", objectCurrent.asPrismObject()));
//            }
//            if (objectNew != null) {
//                rv.add(new ViewedObject("new", objectNew.asPrismObject()));
//            }
//        }
//        return rv.isEmpty() ? null : rv;
//    }

    public List<String> getTraceNames() {
        return result.getTrace().stream().map(trace -> trace.getClass().getSimpleName()).collect(Collectors.toList());
    }

    public String getResultComment() {
        return getResultComment(result);
    }

    public static String getResultComment(OperationResultType result) {
        ParamsType returns = result.getReturns();
        for (EntryType entry : returns.getEntry()) {
            if (OperationResult.RETURN_COMMENT.equals(entry.getKey())) {
                JAXBElement<?> value = entry.getEntryValue();
                if (value == null) {
                    return null;
                } else if (value.getValue() instanceof RawType) {
                    return ((RawType) value.getValue()).extractString();
                } else {
                    return String.valueOf(value.getValue());
                }
            }
        }
        return null;
    }

    public int getLogEntriesCount() {
        int rv = 0;
        for (LogSegmentType segment : result.getLog()) {
            rv += segment.getEntry().size();
        }
        return rv;
    }

    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public Double getOverhead() {
        long repository = getPerformanceByCategory().get(PerformanceCategory.REPOSITORY).getTotalTime();
        long icf = getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalTime();
        Long total = getResult().getMicroseconds();
        if (total != null && total.doubleValue() != 0.0) {
            return (total.doubleValue() - repository - icf) / total.doubleValue();
        } else {
            return null;
        }
    }

    public Double getOverhead2() {
        long repository = getPerformanceByCategory().get(PerformanceCategory.REPOSITORY_CACHE).getTotalTime();
        long icf = getPerformanceByCategory().get(PerformanceCategory.ICF).getTotalTime();
        Long total = getResult().getMicroseconds();
        if (total != null && total.doubleValue() != 0.0) {
            return (total.doubleValue() - repository - icf) / total.doubleValue();
        } else {
            return null;
        }
    }
}
