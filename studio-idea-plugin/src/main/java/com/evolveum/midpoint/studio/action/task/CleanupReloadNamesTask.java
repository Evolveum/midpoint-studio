package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.impl.binding.AbstractReferencable;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.client.SearchResult;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.configuration.Referencable;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefNode;
import com.evolveum.midpoint.studio.ui.cleanup.MissingRefObjectsTable;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CleanupReloadNamesTask extends SimpleBackgroundableTask {

    public static String TITLE = "Reload names";

    public static String NOTIFICATION_KEY = TITLE;

    private static final int CHUNK_SIZE = 25;

    private final MissingRefObjectsTable table;

    public CleanupReloadNamesTask(@NotNull Project project, MissingRefObjectsTable table) {
        super(project, TITLE, NOTIFICATION_KEY);

        this.table = table;
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        indicator.setIndeterminate(true);

        if (client == null) {
            MidPointUtils.publishNotification(
                    getProject(), NOTIFICATION_KEY, TITLE,
                    "Environment not selected, client not available", NotificationType.WARNING);
            return;
        }

        DefaultMutableTreeTableNode node = (DefaultMutableTreeTableNode) table.getTableModel().getRoot();
        Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> toResolve = listNodesToResolve(node, new HashMap<>());
        if (toResolve.isEmpty()) {
            return;
        }

        List<Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>>> chunks = chunkMap(toResolve);

        // todo implement only max 4 threads executing at once
        for (Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> chunk : chunks) {
            ApplicationManager.getApplication().executeOnPooledThread(
                    new ResolveChunkRunnable(client, table, chunk));
        }
    }

    private List<Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>>> chunkMap(
            Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> map) {

        List<Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>>> chunks = new ArrayList<>();

        List<ObjectReferenceType> keys = new ArrayList<>(map.keySet());
        for (int i = 0; i < keys.size(); i += CHUNK_SIZE) {
            List<ObjectReferenceType> chunkKeys = keys.subList(i, Math.min(keys.size(), i + CHUNK_SIZE));

            Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> chunk = new HashMap<>();
            for (ObjectReferenceType key : chunkKeys) {
                chunk.put(key, map.get(key));
            }
            chunks.add(chunk);
        }

        return chunks;
    }

    private Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> listNodesToResolve(
            DefaultMutableTreeTableNode node, Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> toResolve) {

        if (node == null) {
            return toResolve;
        }

        Referencable ref = getReferencable(node);

        if (ref != null) {
            ObjectReferenceType or = ref.toObjectReferenceType();
            List<DefaultMutableTreeTableNode> nodes = toResolve.getOrDefault(or, new ArrayList<>());
            nodes.add(node);

            toResolve.putIfAbsent(or, nodes);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeTableNode child = (DefaultMutableTreeTableNode) node.getChildAt(i);
            listNodesToResolve(child, toResolve);
        }

        return toResolve;
    }

    private Referencable getReferencable(DefaultMutableTreeTableNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof MissingRefNode<?> refNode) {
            userObject = refNode.getValue();
        }

        if (userObject instanceof Referencable ref) {
            return ref;
        }

        return null;
    }

    private record ResolveChunkRunnable(MidPointClient client,
                                        MissingRefObjectsTable table,
                                        Map<ObjectReferenceType, List<DefaultMutableTreeTableNode>> chunk)
            implements Runnable {

        private static final Logger LOG = Logger.getInstance(ResolveChunkRunnable.class);

        @Override
        public void run() {
            Class<? extends ObjectType> type = ObjectType.class;

            List<String> oids = chunk.keySet().stream().map(AbstractReferencable::getOid).toList();
            ObjectQuery query = MidPointUtils.DEFAULT_PRISM_CONTEXT.queryFor(type)
                    .id(oids.toArray(new String[0]))
                    .build();

            try {
                SearchResult result = client.search(type, query, true);
                for (MidPointObject object : result.getObjects()) {
                    ObjectReferenceType key = new ObjectReferenceType()
                            .oid(object.getOid())
                            .type(object.getType().getTypeQName());

                    List<DefaultMutableTreeTableNode> nodes = chunk.getOrDefault(key, List.of());
                    for (DefaultMutableTreeTableNode node : nodes) {
                        updateNodeName(node, object.getName());
                    }
                }
            } catch (Exception ex) {
                LOG.error("Couldn't resolve names", ex);
            }
        }

        private void updateNodeName(DefaultMutableTreeTableNode node, String name) {
            Object userObject = node.getUserObject();
            if (userObject instanceof MissingRefNode<?> refNode) {
                userObject = refNode.getValue();
            }

            if (userObject instanceof Referencable ref) {
                ref.setName(name);
            }

            ApplicationManager.getApplication().invokeLater(() -> table.getTableModel().nodeChanged(node));
        }
    }
}
