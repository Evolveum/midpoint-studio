package com.evolveum.midpoint.sdk.api;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.sdk.api.client.MidpointClient;
import com.evolveum.midpoint.sdk.api.lang.AuthorizationActionProvider;
import com.evolveum.midpoint.sdk.api.lang.ExpressionVariablesProvider;
import com.evolveum.midpoint.sdk.api.lang.TaskHandlerProvider;

public class SdkContext {

    private MidpointClient client;

    private PrismContext prismContext;

    private TaskHandlerProvider taskHandlerProvider;

    private AuthorizationActionProvider authorizationActionProvider;

    private ExpressionVariablesProvider expressionVariablesProvider;

    public MidpointClient client() {
        return client;
    }

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

        private MidpointClient client;

        private PrismContext prismContext;

        private TaskHandlerProvider taskHandlerProvider;

        private AuthorizationActionProvider authorizationActionProvider;

        private ExpressionVariablesProvider expressionVariablesProvider;

        public Builder client(MidpointClient client) {
            this.client = client;
            return this;
        }

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
            sdkContext.client = client;
            sdkContext.prismContext = prismContext;
            sdkContext.taskHandlerProvider = taskHandlerProvider;
            sdkContext.authorizationActionProvider = authorizationActionProvider;
            sdkContext.expressionVariablesProvider = expressionVariablesProvider;
            return sdkContext;
        }
    }
}
