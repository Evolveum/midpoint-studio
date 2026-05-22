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

@Service(Service.Level.PROJECT)
public final class TaskStatusPoller implements Disposable {

    private ScheduledFuture<?> pollingFuture;

    private volatile OperationResultStatusType status;

    private volatile Instant startedAt;

    public void startPolling(Supplier<OperationResultStatusType> statusFetcher) {

        if (pollingFuture != null && !pollingFuture.isCancelled()) {
            return;
        }

        startedAt = Instant.now();

        pollingFuture = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            try {
                setStatus(statusFetcher.get());
            } catch (Exception e) {
                stopPolling();
                throw new RuntimeException(e);
            }
        }, 3, 1, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (pollingFuture != null) {
            pollingFuture.cancel(true);
        }
    }

    public OperationResultStatusType getStatus() {
        return status;
    }

    public void setStatus(OperationResultStatusType status) {
        this.status = status;
    }

    public Duration getElapsedTime() {
        return startedAt == null ? Duration.ZERO : Duration.between(startedAt, Instant.now());
    }

    @Override
    public void dispose() {
        stopPolling();
    }
}
