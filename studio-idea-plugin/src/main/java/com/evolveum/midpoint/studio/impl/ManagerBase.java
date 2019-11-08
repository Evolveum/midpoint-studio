package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class ManagerBase<T extends Serializable>
        implements PersistentStateComponent<T>, ModificationTracker, Stateful<T> {

    private static final Logger LOG = Logger.getInstance(ManagerBase.class);

    private Project project;

    private Class<T> settingsClass;
    private T settings;

    private long modificationCount;

    public ManagerBase(Project project, Class<T> settingsClass) {
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
        return settings;
    }

    @Override
    public void loadState(T state) {
        T settings = createEmptySettings();
        XmlSerializerUtil.copyBean(state, settings);

        setSettings(settings);
    }

    @Override
    @Transient
    public T getSettings() {
        return getState();
    }

    @Override
    public void setSettings(T settings) {
        this.settings = settings;

        modificationCount++;
    }

    public void settingsUpdated() {
        modificationCount++;
    }

    @Override
    public void noStateLoaded() {
        T settings = createDefaultSettings();
        setSettings(settings);
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
