package com.evolveum.midpoint.studio.impl.browse;

import com.evolveum.midpoint.studio.ui.Execution;
import com.evolveum.midpoint.studio.ui.ProcessResultsDialog;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProcessResultsOptions {

    private GeneratorOptions options;

    private Execution execution;

    private Generator generator;

    public GeneratorOptions getOptions() {
        if (options == null) {
            options = new GeneratorOptions();
        }
        return options;
    }

    public void setOptions(GeneratorOptions options) {
        this.options = options;
    }

    public Execution getExecution() {
        if (execution == null) {
            execution = Execution.OID_ONE_BATCH;
        }
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public Generator getGenerator() {
        if (generator == null) {
            generator = ProcessResultsDialog.GENERATORS.get(0);
        }
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }
}
