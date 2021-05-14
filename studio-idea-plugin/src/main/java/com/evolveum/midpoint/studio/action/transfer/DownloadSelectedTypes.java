package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrderDirection;
import com.evolveum.midpoint.prism.query.QueryFactory;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.action.browse.DownloadAction;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.impl.MidPointSettings;
import com.evolveum.midpoint.studio.impl.client.ServiceFactory;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadSelectedTypes extends BackgroundAction {

    public static final String NOTIFICATION_KEY = "Download Selected Types Action";

    private static final String TASK_TITLE = "Download selected types";

    public DownloadSelectedTypes() {
        super(TASK_TITLE);
    }

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        MidPointUtils.isVisibleWithMidPointFacet(evt);
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        Project project = evt.getProject();
        if (project == null) {
            return;
        }

        MidPointService mm = MidPointService.getInstance(project);
        MidPointSettings settings = mm.getSettings();

        if (settings.getTypesToDownloadLimit() == 0 && settings.getDownloadTypesInclude().isEmpty() && settings.getDownloadTypesExclude().isEmpty()) {
            // probably
            MidPointSettings defaults = MidPointSettings.createDefaultSettings();
            settings.setDownloadTypesInclude(defaults.getDownloadTypesInclude());
            settings.setDownloadTypesExclude(defaults.getDownloadTypesExclude());
            settings.setTypesToDownloadLimit(defaults.getTypesToDownloadLimit());
            mm.settingsUpdated();
        }

        if (settings.getTypesToDownloadLimit() <= 0) {
            MidPointUtils.publishNotification(NOTIFICATION_KEY, "Download Selected Types", "Download limit set to zero. Done.", NotificationType.WARNING);
            return;
        }

        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment environment = em.getSelected();

        PrismContext ctx = ServiceFactory.DEFAULT_PRISM_CONTEXT;
        QueryFactory qf = ctx.queryFactory();

        ObjectPaging paging = qf.createPaging(0, settings.getTypesToDownloadLimit(),
                ctx.path(ObjectType.F_NAME), OrderDirection.ASCENDING);
        ObjectQuery query = qf.createQuery(null, paging);

        List<ObjectTypes> typesToDownload = determineTypesToDownload(settings);
        for (ObjectTypes type : typesToDownload) {
            try {
                DownloadAction da = new DownloadAction(environment, type, query, false, false, true);
                da.setOpenAfterDownload(false);
                da.download(evt, indicator);
            } catch (Exception ex) {
                mm.printToConsole(environment, getClass(), "Couldn't download objects of type '" + type.getValue() + "'. Reason: " + ex.getMessage());
            }
        }
    }

    private List<ObjectTypes> determineTypesToDownload(MidPointSettings settings) {
        List<ObjectTypes> rv = new ArrayList<>();
        List<ObjectTypes> include = settings.getDownloadTypesInclude();
        List<ObjectTypes> exclude = settings.getDownloadTypesExclude();

        if (include.isEmpty()) {
            rv.addAll(MidPointUtils.getConcreteObjectTypes());
        } else {
            rv.addAll(include);
        }

        if (exclude != null) {
            rv.removeAll(exclude);
        }

        return rv;
    }
}
