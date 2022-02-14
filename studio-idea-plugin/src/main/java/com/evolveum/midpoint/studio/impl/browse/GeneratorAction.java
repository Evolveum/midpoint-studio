package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.studio.action.AsyncAction;
import com.evolveum.midpoint.studio.action.task.GeneratorTask;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorAction extends AsyncAction<GeneratorTask> {

    public static final String ACTION_NAME = "Processing objects";

    private Generator generator;

    private GeneratorOptions options;

    private List<ObjectType> objects;

    private boolean execute;

    public GeneratorAction(Generator generator, GeneratorOptions options, List<ObjectType> objects, boolean execute) {
        super(ACTION_NAME);

        this.generator = generator;
        this.options = options;
        this.objects = objects;
        this.execute = execute;
    }

    @Override
    protected GeneratorTask createTask(AnActionEvent e, Environment env) {
        GeneratorTask task = new GeneratorTask(e, generator, options, objects, execute);
        task.setEnvironment(env);

        return task;
    }
}
