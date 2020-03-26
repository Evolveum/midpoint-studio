package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.action.browse.BackgroundAction;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.evolveum.midscribe.generator.GenerateOptions;
import com.evolveum.midscribe.generator.Generator;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;

import java.util.Properties;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GenerateDocumentationAction extends BackgroundAction {

    private static Logger LOG = Logger.getInstance(GenerateDocumentationAction.class);

    private static final String NOTIFICATION_KEY = "Documentation Generator";

    private GenerateOptions options;

    public GenerateDocumentationAction(GenerateOptions options) {
        super("Generating documentation");

        this.options = options;
    }

    @Override
    protected void executeOnBackground(AnActionEvent evt, ProgressIndicator indicator) {
        Properties properties = new Properties();
        properties.put(RuntimeConstants.RESOURCE_MANAGER_INSTANCE, new ResourceManagerImpl());

        Generator generator = new Generator(options);
        try {
            generator.generate(properties);
        } catch (Exception ex) {
            LOG.error("Couldn't generate documentation", ex);

            MidPointUtils.handleGenericException(evt.getProject(), GenerateDocumentationAction.class,
                    NOTIFICATION_KEY, "Couldn't generate documentation", ex);
        }
    }
}
