package com.evolveum.midpoint.sdk.api;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.sdk.api.lang.AuthorizationActionProvider;
import com.evolveum.midpoint.sdk.api.lang.ExpressionVariablesProvider;
import com.evolveum.midpoint.sdk.api.lang.TaskHandlerProvider;

public class SdkContext {

    private PrismContext prismContext;

    private TaskHandlerProvider taskHandlerProvider;

    private AuthorizationActionProvider authorizationActionProvider;

    private ExpressionVariablesProvider expressionVariablesProvider;

    public PrismContext prismContext() {
        return prismContext;
    }

    public TaskHandlerProvider taskHandlerProvider() {
        return taskHandlerProvider;
    }

    public AuthorizationActionProvider authorizationActionProvider() {
        return authorizationActionProvider;
    }

    public ExpressionVariablesProvider expressionVariablesProvider() {
        return expressionVariablesProvider;
    }

    public static class Builder {

        private PrismContext prismContext;

        private TaskHandlerProvider taskHandlerProvider;

        private AuthorizationActionProvider authorizationActionProvider;

        private ExpressionVariablesProvider expressionVariablesProvider;

        public Builder prismContext(PrismContext prismContext) {
            this.prismContext = prismContext;
            return this;
        }

        public Builder taskHandlerProvider(TaskHandlerProvider taskHandlerProvider) {
            this.taskHandlerProvider = taskHandlerProvider;
            return this;
        }

        public Builder authorizationActionProvider(AuthorizationActionProvider authorizationActionProvider) {
            this.authorizationActionProvider = authorizationActionProvider;
            return this;
        }

        public Builder expressionVariablesProvider(ExpressionVariablesProvider expressionVariablesProvider) {
            this.expressionVariablesProvider = expressionVariablesProvider;
            return this;
        }

        public SdkContext build() {
            SdkContext sdkContext = new SdkContext();
            sdkContext.prismContext = prismContext;
            sdkContext.taskHandlerProvider = taskHandlerProvider;
            sdkContext.authorizationActionProvider = authorizationActionProvider;
            sdkContext.expressionVariablesProvider = expressionVariablesProvider;
            return sdkContext;
        }
    }
}
