package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrderDirection;
import com.evolveum.midpoint.prism.query.QueryFactory;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.configuration.MidPointConfiguration;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DownloadSelectedTypesTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(DownloadSelectedTypesTask.class);

    public static String TITLE = "Download selected types task";

    public static String NOTIFICATION_KEY = TITLE;

    public DownloadSelectedTypesTask(@NotNull Project project, Supplier<DataContext> dataContextSupplier) {
        super(project, dataContextSupplier, TITLE, NOTIFICATION_KEY);
    }

    @Override
    protected void doRun(ProgressIndicator indicator) {
        super.doRun(indicator);

        MidPointConfiguration settings = midPointService.getSettings();

        if (settings.getTypesToDownloadLimit() == 0 && settings.getDownloadTypesInclude().isEmpty() && settings.getDownloadTypesExclude().isEmpty()) {
            // probably
            MidPointConfiguration defaults = MidPointConfiguration.createDefaultSettings();
            settings.setDownloadTypesInclude(defaults.getDownloadTypesInclude());
            settings.setDownloadTypesExclude(defaults.getDownloadTypesExclude());
            settings.setTypesToDownloadLimit(defaults.getTypesToDownloadLimit());
            midPointService.settingsUpdated();
        }

        if (settings.getTypesToDownloadLimit() <= 0) {
            MidPointUtils.publishNotification(getProject(), NOTIFICATION_KEY, "Download Selected Types", "Download limit set to zero. Done.", NotificationType.WARNING);
            return;
        }

        Environment environment = getEnvironment();

        Project project = getProject();

        StudioPrismContextService.runWithProject(project, () -> {
            PrismContext ctx = StudioPrismContextService.getPrismContext(project);
            QueryFactory qf = ctx.queryFactory();

            ObjectPaging paging = qf.createPaging(0, settings.getTypesToDownloadLimit(),
                    ctx.path(ObjectType.F_NAME), OrderDirection.ASCENDING);
            ObjectQuery query = qf.createQuery(null, paging);

            List<ObjectTypes> typesToDownload = determineTypesToDownload(settings);
            for (ObjectTypes type : typesToDownload) {
                DownloadTask dt = new DownloadTask(project, null, type, query, false, false, true);
                dt.setEnvironment(environment);
                dt.setOpenAfterDownload(false);

                ProgressManager.getInstance().run(dt);
            }
        });
    }

    private List<ObjectTypes> determineTypesToDownload(MidPointConfiguration settings) {
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
