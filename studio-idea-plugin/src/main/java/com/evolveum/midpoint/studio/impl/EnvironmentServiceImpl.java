package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "EnvironmentManager", storages = @Storage(value = "midpoint.xml")
)
public class EnvironmentServiceImpl extends ServiceBase<EnvironmentSettings> implements EnvironmentService {

    private static final String KEY_PROXY_SUFFIX = "proxy";

    private static final String DESCRIPTION_PROXY_SUFFIX = " (proxy)";

    private static final Logger LOG = Logger.getInstance(EnvironmentServiceImpl.class);

    private MessageBus messageBus;

    public EnvironmentServiceImpl(@NotNull Project project) {
        super(project, EnvironmentSettings.class);

        this.messageBus = project.getMessageBus();
    }

    private EncryptionService getEncryptionService() {
        return getProject().getService(EncryptionService.class);
    }

    @Override
    protected EnvironmentSettings createDefaultSettings() {
        return EnvironmentSettings.createDefaultSettings();
    }

    @Override
    public void setSettings(EnvironmentSettings settings) {
        LOG.debug("Setting new settings " + settings);

        Set<String> newIds = new HashSet<>();
        for (Environment env : settings.getEnvironments()) {
            add(env);
            newIds.add(env.getId());
        }

        Iterator<Environment> iterator = getEnvironments().iterator();
        while (iterator.hasNext()) {
            Environment env = iterator.next();
            if (!newIds.contains(env.getId())) {
                iterator.remove();
            }
        }

        select(settings.getSelectedId());

        super.setSettings(settings);
    }

    @Override
    public EnvironmentSettings getFullSettings() {
        EnvironmentSettings settings = new EnvironmentSettings();
        settings.setEnvironments(getEnvironments());
        settings.setSelectedId(getSettings().getSelectedId());

        return settings;
    }

    @Override
    public List<Environment> getEnvironments() {
        List<Environment> result = new ArrayList<>();

        List<Environment> environments = getSettings().getEnvironments();
        for (Environment env : environments) {
            Environment copy = buildFullEnvironment(env);
            result.add(copy);
        }

        return result;
    }

    private Environment buildFullEnvironment(Environment env) {
        Environment copy = new Environment(env);

        EncryptionService service = getEncryptionService();

        EncryptedCredentials credentials = service.get(copy.getId(), EncryptedCredentials.class);
        if (credentials != null) {
            copy.setUsername(credentials.getUsername());
            copy.setPassword(credentials.getPassword());
        }

        credentials = service.get(copy.getId() + KEY_PROXY_SUFFIX, EncryptedCredentials.class);
        if (credentials != null) {
            copy.setProxyUsername(credentials.getUsername());
            copy.setProxyPassword(credentials.getPassword());
        }

        return copy;
    }

    @Override
    public boolean isEnvironmentSelected() {
        return getSettings().getSelected() != null;
    }

    @Override
    public Environment getSelected() {
        Environment env = getSettings().getSelected();
        return env != null ? buildFullEnvironment(env) : null;
    }

    @Override
    public void select(String id) {
        LOG.info("Selecting new environment");

        Environment selected = getSelected();
        Environment newSelected = get(id);

        LOG.debug("New environment " + newSelected + ", old one " + selected);

        if (Objects.equals(selected, newSelected)) {
            return;
        }

        getSettings().setSelectedId(id);
        settingsUpdated();

        LOG.info("New environment selected, publishing notification on message bus");

        messageBus.syncPublisher(MidPointProjectNotifier.MIDPOINT_NOTIFIER_TOPIC).environmentChanged(selected, newSelected);

        LOG.info("New environment selection finished");
    }

    @Override
    public String add(Environment env) {
        LOG.debug("Adding environment " + env);

        if (StringUtils.isNotEmpty(env.getUsername()) || StringUtils.isNotEmpty(env.getPassword())) {
            EncryptedCredentials credentials = new EncryptedCredentials(
                    env.getId(), env.getId(), env.getUsername(), env.getPassword(), env.getName());
            getEncryptionService().add(credentials);
        }

        if (StringUtils.isNotEmpty(env.getProxyUsername()) || StringUtils.isNotEmpty(env.getProxyPassword())) {
            EncryptedCredentials credentials = new EncryptedCredentials(KEY_PROXY_SUFFIX, env.getId(), env.getProxyUsername(),
                    env.getProxyPassword(), env.getName() + DESCRIPTION_PROXY_SUFFIX);
            getEncryptionService().add(credentials);
        }

        getSettings().getEnvironments().add(env);
        settingsUpdated();

        return env.getId();
    }

    @Override
    public boolean delete(String id) {
        LOG.debug("Deleting environment " + id);
        Environment env = get(id);
        if (env == null) {
            return false;
        }

        if (StringUtils.isNotEmpty(env.getUsername()) || StringUtils.isNotEmpty(env.getPassword())) {
            getEncryptionService().delete(env.getId());
        }

        if (StringUtils.isNotEmpty(env.getProxyUsername()) || StringUtils.isNotEmpty(env.getProxyPassword())) {
            getEncryptionService().delete(env.getId() + KEY_PROXY_SUFFIX);
        }

        getSettings().getEnvironments().remove(env);
        settingsUpdated();

        return true;
    }

    @Override
    public Environment get(String id) {
        if (id == null) {
            return null;
        }

        List<Environment> envs = getSettings().getEnvironments();
        for (Environment env : envs) {
            if (Objects.equals(id, env.getId())) {
                return buildFullEnvironment(env);
            }
        }

        return null;
    }

    @Override
    public EnvironmentProperties getSelectedEnvironmentProperties() {
        Environment env = getSelected();

        return new EnvironmentProperties(env);
    }
}
