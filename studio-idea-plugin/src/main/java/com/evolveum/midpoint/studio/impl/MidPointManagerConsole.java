package com.evolveum.midpoint.studio.impl;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointManagerConsole implements Console {

    private Project project;

    private MidPointService midPointService;

    public MidPointManagerConsole(Project project) {
        this.project = project;
    }

    private MidPointService getMidPointService() {
        if (midPointService != null) {
            return midPointService;
        }

        midPointService = project.getService(MidPointService.class);
        return midPointService;
    }

    @Override
    public void printToConsole(Environment env, Class clazz, String message) {
        getMidPointService().printToConsole(env, clazz, message);
    }

    @Override
    public void printToConsole(Environment env, Class clazz, String message, Exception ex) {
        getMidPointService().printToConsole(env, clazz, message, ex);
    }

    @Override
    public void printToConsole(Environment env, Class clazz, String message, Exception ex, @NotNull Console.ContentType type) {
        getMidPointService().printToConsole(env, clazz, message, ex, type.getType());
    }
}
