/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.studio.cmd.action;

import com.evolveum.midpoint.studio.client.Service;
import com.evolveum.midpoint.studio.client.ServiceFactory;
import com.evolveum.midpoint.studio.cmd.StudioContext;
import com.evolveum.midpoint.studio.cmd.opts.EnvironmentOptions;
import com.evolveum.midpoint.studio.cmd.util.Log;
import com.evolveum.midpoint.studio.cmd.util.LogTarget;
import com.evolveum.midpoint.studio.cmd.util.StudioUtil;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class Action<T> {

    protected Log log;

    protected StudioContext context;

    protected T options;

    public void init(StudioContext context, T options) {
        this.context = context;
        this.options = options;

        LogTarget target = getInfoLogTarget();
        log = new Log(target, this.context);

        this.context.setLog(log);

        EnvironmentOptions connection = StudioUtil.getOptions(this.context.getJc(), EnvironmentOptions.class);
//        this.context.init(connection);
    }

    public LogTarget getInfoLogTarget() {
        return LogTarget.SYSTEM_OUT;
    }

//    protected void handleResultOnFinish(OperationStatus operation, String finishMessage) {
//        OperationResult result = operation.getResult();
//        result.recomputeStatus();
//
//        if (result.isAcceptable()) {
//            log.info("{} in {}s. {}", finishMessage, StudioUtil.DECIMAL_FORMAT.format(operation.getTotalTime()),
//                    operation.print());
//        } else {
//            log.error("{} in {}s with some problems, reason: {}. {}", finishMessage,
//                    StudioUtil.DECIMAL_FORMAT.format(operation.getTotalTime()), result.getMessage(), operation.print());
//
//            if (context.isVerbose()) {
//                log.error("Full result\n{}", result.debugDumpLazily());
//            }
//        }
//    }

    protected Service buildClient() throws Exception {
        EnvironmentOptions env = context.getEnvironmentOptions();

        ServiceFactory factory = new ServiceFactory();
        factory
                .url(env.getUrl())
                .username(env.getUsername())
                .password(env.getOrAskPassword())
                .proxyServer(env.getProxyHost())
                .proxyServerPort(env.getProxyPort())
                .proxyServerType(env.getProxyType())
                .proxyUsername(env.getProxyUsername())
                .proxyPassword(env.getOrAskProxyPassword())
                .ignoreSSLErrors(env.isIgnoreSSLErrors())
                .responseTimeout(env.getResponseTimeout());

        // todo fix logging
        factory.messageListener(message -> System.out.println(message));

        return factory.create();
    }

    public abstract void execute() throws Exception;
}
