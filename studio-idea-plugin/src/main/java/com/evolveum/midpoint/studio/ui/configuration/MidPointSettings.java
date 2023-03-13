package com.evolveum.midpoint.studio.ui.configuration;


import com.intellij.diagnostic.LoadingState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Created by Viliam Repan (lazyman).
 */
@State(name = "MidPointSettings", storages = @Storage(value = "midpoint-studio.xml"), useLoadedStateAsExisting = false, category = SettingsCategory.PLUGINS)
public class MidPointSettings implements PersistentStateComponentWithModificationTracker<MidPointSettingsState> {

    private static volatile MidPointSettings INSTANCE;

    private MidPointSettingsState state = new MidPointSettingsState();

    public static MidPointSettings getInstance() {
        MidPointSettings instance = INSTANCE;
        if (instance == null) {
            LoadingState.CONFIGURATION_STORE_INITIALIZED.checkOccurred();

            instance = ApplicationManager.getApplication().getService(MidPointSettings.class);
            INSTANCE = instance;
        }

        return instance;
    }

    @Override
    public long getStateModificationCount() {
        return state.getModificationCount();
    }

    @Override
    public @Nullable MidPointSettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull MidPointSettingsState state) {
        this.state = state;
    }
}
