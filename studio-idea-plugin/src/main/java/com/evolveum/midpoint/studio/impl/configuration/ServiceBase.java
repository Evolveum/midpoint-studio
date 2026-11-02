package com.evolveum.midpoint.studio.impl.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Base for project services persisted in midpoint.xml. Settings are always available through
 * {@link #getSettings()}, but {@link #getState()} returns null until state was loaded from disk
 * or a caller changed the settings, so the platform writes nothing for a project that only
 * instantiated the service.
 *
 * The default project never reports state, its entries would be copied into every new project as
 * a template.
 *
 * Created by Viliam Repan (lazyman).
 */
public abstract class ServiceBase<T extends Serializable>
        implements PersistentStateComponent<T>, ModificationTracker, Stateful<T> {

    private static final Logger LOG = Logger.getInstance(ServiceBase.class);

    private Project project;

    private Class<T> settingsClass;
    private T settings;

    private long modificationCount;

    private boolean persisted;

    public ServiceBase(Project project, Class<T> settingsClass) {
        LOG.info("Initializing " + getClass().getSimpleName());
        this.project = project;
        this.settingsClass = settingsClass;
        this.settings = createDefaultSettings();
    }

    @Override
    public long getModificationCount() {
        return modificationCount;
    }

    @Nullable
    @Override
    public T getState() {
        return persisted && !project.isDefault() ? settings : null;
    }

    @Override
    public void loadState(@NotNull T state) {
        T settings = createEmptySettings();
        XmlSerializerUtil.copyBean(state, settings);

        loadSettings(settings);

        persisted = true;
    }

    /**
     * Called from {@link #loadState}, runs during component initialization inside read action.
     */
    protected void loadSettings(T settings) {
        replaceSettings(settings);
    }

    @Override
    @Transient
    public T getSettings() {
        return settings;
    }

    @Override
    public void setSettings(T settings) {
        replaceSettings(settings);

        settingsUpdated();
    }

    /**
     * Replaces the settings object without marking the service as modified or persisted.
     */
    protected void replaceSettings(T settings) {
        this.settings = settings;
    }

    public void settingsUpdated() {
        persisted = true;
        modificationCount++;
    }

    @Override
    public void noStateLoaded() {
        replaceSettings(createDefaultSettings());
    }

    public Project getProject() {
        return project;
    }

    protected T createEmptySettings() {
        try {
            return settingsClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected abstract T createDefaultSettings();
}
