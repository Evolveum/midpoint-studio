package com.evolveum.midpoint.studio.ui.configuration;


import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Created by Viliam Repan (lazyman).
 */
@Service(Service.Level.PROJECT)
@State(name = "MidPointSettings", storages = @Storage(value = "midpoint-studio.xml"), useLoadedStateAsExisting = false, category = SettingsCategory.PLUGINS)
public final class MidPointSettings implements PersistentStateComponentWithModificationTracker<MidPointSettingsState> {

    private MidPointSettingsState state = new MidPointSettingsState();

    private long modificationCount;

    public static MidPointSettings getInstance(Project project) {
        return project.getService(MidPointSettings.class);
    }

    @Override
    public long getStateModificationCount() {
        return modificationCount;
    }

    @Override
    public @Nullable MidPointSettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull MidPointSettingsState state) {
        setState(state);
    }

    public void setState(@NotNull MidPointSettingsState state) {
        this.state = state;

        modificationCount++;
    }
}
