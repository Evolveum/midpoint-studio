package com.evolveum.midpoint.studio.action.environment;

import com.evolveum.midpoint.studio.MidPointConstants;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.EnvironmentService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by lazyman on 10/02/2017.
 */
public class SelectEnvironment extends AnAction {

    public static final String ACTION_ID = MidPointConstants.ACTION_ID_PREFIX + SelectEnvironment.class.getSimpleName();

    private Environment environment;

    public SelectEnvironment(Environment environment) {
        super(environment != null ? environment.getName() : "No Environment");

        this.environment = environment;

        if (environment != null) {
            getTemplatePresentation().setIcon(MidPointUtils.createEnvironmentIcon(environment.getAwtColor()));
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        EnvironmentService manager = EnvironmentService.getInstance(e.getProject());
        manager.select(environment != null ? environment.getId() : null);
    }
}
