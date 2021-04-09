package com.evolveum.midpoint.studio.action.browse;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointClient;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.impl.client.DeleteOptions;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.studio.util.Pair;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DeleteAction extends BackgroundAction {

    public static final String NOTIFICATION_KEY = "Delete Action";

    private static final String TASK_TITLE = "Deleting objects";

    private Environment environment;

    private List<Pair<String, ObjectTypes>> oids;

    private boolean raw;

    public DeleteAction(@NotNull Environment environment, @NotNull List<Pair<String, ObjectTypes>> oids, boolean raw) {
        super(TASK_TITLE);

        this.environment = environment;

        this.oids = oids;
        this.raw = raw;
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        indicator.setIndeterminate(true);

        MidPointService mm = MidPointService.getInstance(evt.getProject());
        MidPointClient client = new MidPointClient(evt.getProject(), environment);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        try {
            indicator.setFraction(0d);

            int i = 0;
            for (Pair<String, ObjectTypes> pair : oids) {
                i++;
                indicator.setFraction(i / oids.size());

                try {
                    client.delete(pair.getSecond().getClassDefinition(), pair.getFirst(), new DeleteOptions().raw(raw));
                    success.incrementAndGet();
                } catch (Exception ex) {
                    fail.incrementAndGet();

                    publishException(mm, "Exception occurred when deleting '" + pair.getFirst()
                            + "' (" + pair.getSecond().getTypeQName().getLocalPart() + ")", ex);
                }
            }
        } catch (Exception ex) {
            MidPointUtils.publishExceptionNotification(environment, DeleteAction.class, NOTIFICATION_KEY,
                    "Exception occurred when deleting objects ", ex);
        }

        showNotificationAfterFinish(success.get(), fail.get());
    }

    private void publishException(MidPointService mm, String msg, Exception ex) {
        mm.printToConsole(environment, getClass(), msg + ". Reason: " + ex.getMessage());

        MidPointUtils.publishExceptionNotification(environment, getClass(), NOTIFICATION_KEY, msg, ex);
    }

    private void showNotificationAfterFinish(int successObjects, int failedObjects) {
        NotificationType type;
        String title;
        StringBuilder sb = new StringBuilder();

        if (failedObjects == 0 && successObjects > 0) {
            type = NotificationType.INFORMATION;
            title = "Success";

            sb.append(getTaskTitle() + " finished.");
        } else {
            type = NotificationType.WARNING;
            title = "Warning";

            sb.append("There were problems during '" + getTaskTitle() + "'");
        }

        sb.append("<br/>");
        sb.append("Deleted: ").append(successObjects).append(" objects<br/>");
        sb.append("Failed to delete: ").append(failedObjects).append(" objects");

        MidPointUtils.publishNotification(NOTIFICATION_KEY, title, sb.toString(), type);
    }
}
