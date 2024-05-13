package com.evolveum.midpoint.studio.ui.diff;

import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.NoDiffRequest;
import com.intellij.diff.util.DiffUserDataKeysEx;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.ThreadingAssertions;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleDiffRequestProcessor extends DiffRequestProcessor {

    private @NotNull DiffRequest request;

    SimpleDiffRequestProcessor(@Nullable Project project) {
        super(project);

        this.request = NoDiffRequest.INSTANCE;
    }

    public synchronized void setRequest(@Nullable DiffRequest request) {
        this.request = request != null ? request : NoDiffRequest.INSTANCE;

        UIUtil.invokeLaterIfNeeded(() -> {
            this.updateRequest();
            System.out.println();
        });
    }

    @RequiresEdt
    public synchronized void updateRequest(
            boolean force, @Nullable DiffUserDataKeysEx.@Nullable ScrollToPolicy scrollToChangePolicy) {

        ThreadingAssertions.assertEventDispatchThread();
        this.applyRequest(this.request, force, scrollToChangePolicy);
    }
}
