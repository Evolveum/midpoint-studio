package com.evolveum.midpoint.studio.action.transfer;

import com.evolveum.midpoint.schema.result.OperationResult;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProcessObjectResult {

    private OperationResult result;

    private boolean problem;

    private boolean shouldContinue = true;

    public ProcessObjectResult(OperationResult result) {
        this.result = result;
    }

    public OperationResult result() {
        return this.result;
    }

    public boolean problem() {
        return this.problem;
    }

    public boolean shouldContinue() {
        return this.shouldContinue;
    }

    public ProcessObjectResult result(final OperationResult result) {
        this.result = result;
        return this;
    }

    public ProcessObjectResult problem(final boolean problem) {
        this.problem = problem;
        return this;
    }

    public ProcessObjectResult shouldContinue(final boolean shouldContinue) {
        this.shouldContinue = shouldContinue;
        return this;
    }
}
