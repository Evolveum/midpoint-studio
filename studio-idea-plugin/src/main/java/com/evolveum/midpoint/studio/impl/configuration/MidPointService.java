package com.evolveum.midpoint.studio.impl.configuration;

import com.evolveum.midpoint.studio.impl.ConsoleService;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "MidPointManager", storages = @Storage(value = "midpoint.xml")
)
public class MidPointService extends ServiceBase<MidPointConfiguration> {

    public MidPointService(@NotNull Project project) {
        super(project, MidPointConfiguration.class);
    }

    @Override
    protected MidPointConfiguration createDefaultSettings() {
        return MidPointConfiguration.createDefaultSettings();
    }

    public static MidPointService get(@NotNull Project project) {
        return project.getService(MidPointService.class);
    }

    @Deprecated
    public void focusConsole() {
        ConsoleService.get(getProject()).focusConsole();
    }

    @Deprecated
    public void printToConsole(Environment env, Class clazz, String message) {
        ConsoleService.get(getProject()).printToConsole(env, clazz, message);
    }

    @Deprecated
    public void printToConsole(Environment env, Class clazz, String message, Exception ex) {
        ConsoleService.get(getProject()).printToConsole(env, clazz, message, ex);
    }

    @Deprecated
    public void printToConsole(Environment env, @NotNull Class clazz, String message, Exception ex, @NotNull ConsoleViewContentType type) {
        ConsoleService.get(getProject()).printToConsole(env, clazz, message, ex, type);
    }
}
