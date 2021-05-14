package com.evolveum.midpoint.studio.ui.trace.mainTree.model;

import com.evolveum.midpoint.schema.traces.OpNode;
import com.evolveum.midpoint.schema.traces.RepositoryCacheOpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CacheUseCategoryTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CacheUseTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RepositoryOperationTraceType;

import java.util.*;

import static com.evolveum.midpoint.util.MiscUtil.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class CacheAnalyzer {

    public static void analyze(List<OpNode> opNodes) {
        Analysis analysis = new Analysis();
        opNodes.forEach(analysis::processTree);
        System.out.println(analysis.dump());
    }

    private static class Analysis {

        Set<OpNode> nodesSeen = new HashSet<>();

        private final Map<String, Integer> globalPassReasons = new HashMap<>();
        private final Map<String, Integer> localPassReasons = new HashMap<>();
        private int noTrace;

        public void processTree(OpNode opNode) {
            processNode(opNode);
            opNode.getChildrenStream(Integer.MAX_VALUE)
                    .forEach(this::processNode);
        }

        private void processNode(OpNode opNode) {
            if (!nodesSeen.add(opNode)) {
                return;
            }

            if (!(opNode instanceof RepositoryCacheOpNode)) {
                return;
            }

            System.out.println("Processing " + opNode);

            RepositoryCacheOpNode cacheOpNode = (RepositoryCacheOpNode) opNode;
            RepositoryOperationTraceType trace = cacheOpNode.getTrace();
            if (trace == null) {
                noTrace++;
                System.out.println("No trace");
                return;
            }

            processCacheUse("Global", globalPassReasons, trace.getGlobalCacheUse());
            processCacheUse("Local", localPassReasons, trace.getLocalCacheUse());
        }

        private void processCacheUse(String label, Map<String, Integer> reasons, CacheUseTraceType cacheUse) {
            if (cacheUse == null) {
                System.out.println(label + ": No cache use");
                return;
            }
            CacheUseCategoryTraceType category = cacheUse.getCategory();
            System.out.println(label + ": " + category + " " + emptyIfNull(cacheUse.getComment()));
            if (category == CacheUseCategoryTraceType.PASS) {
                reasons.compute(cacheUse.getComment(), (comment, count) -> defaultIfNull(count, 0) + 1);
            }
        }

        String dump() {
            StringBuilder sb = new StringBuilder();
            sb.append("No trace: ").append(noTrace).append("\n");
            sb.append("Global cache:\n");
            dump(sb, globalPassReasons);
            sb.append("Local cache:\n");
            dump(sb, localPassReasons);
            return sb.toString();
        }

        private void dump(StringBuilder sb, Map<String, Integer> reasons) {
            reasons.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(e -> "  " + e.getKey() + ":" + e.getValue() + "\n")
                    .forEach(sb::append);
        }
    }
}
