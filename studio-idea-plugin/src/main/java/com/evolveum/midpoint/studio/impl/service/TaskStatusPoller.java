package com.evolveum.midpoint.studio.impl.service;

import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.util.concurrency.AppExecutorUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service(Service.Level.APP)
public final class TaskStatusPoller implements Disposable {

    private volatile ScheduledFuture<?> pollingFuture;
    private volatile OperationResultStatusType status;
    private volatile Instant startedAt;

    public synchronized void startPolling(
            Supplier<OperationResultStatusType> statusFetcher) {

        if (pollingFuture != null && !pollingFuture.isDone()) {
            return;
        }

        status = null;
        startedAt = Instant.now();
        pollingFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
                () -> status = statusFetcher.get(), 3, 1, TimeUnit.SECONDS);
    }

    public synchronized void stopPolling() {
        ScheduledFuture<?> future = pollingFuture;
        pollingFuture = null;

        if (future != null) {
            future.cancel(true);
        }

        status = null;
        startedAt = null;
    }

    public OperationResultStatusType getStatus() {
        return status;
    }

    public Duration getElapsedTime() {
        Instant start = startedAt;
        return start == null
                ? Duration.ZERO
                : Duration.between(start, Instant.now());
    }

    public boolean isPolling() {
        ScheduledFuture<?> future = pollingFuture;
        return future != null && !future.isDone();
    }

    @Override
    public void dispose() {
        stopPolling();
    }
}
