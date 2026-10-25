package com.evolveum.midpoint.studio.util;

import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.junit.Test;

import java.util.concurrent.CancellationException;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

/**
 * Covers rethrowing of cancellation (control flow) exceptions from generic catch blocks.
 * Logging those via Logger.error makes the platform report "Control-flow exceptions should never
 * be logged" and floods the log, see InitialObjectsCache.createReloadQuery and
 * StudioPrismContextService.reloadPrismContext.
 */
public class RunnableUtilsTest {

    @Test
    public void testRethrowsProcessCanceledException() {
        ProcessCanceledException ex = new ProcessCanceledException();

        ProcessCanceledException thrown = assertThrows(ProcessCanceledException.class,
                () -> RunnableUtils.rethrowIfCancellation(ex));

        assertSame(ex, thrown);
    }

    @Test
    public void testRethrowsAnyControlFlowException() {
        class TestControlFlowException extends RuntimeException implements ControlFlowException {
        }

        TestControlFlowException ex = new TestControlFlowException();

        TestControlFlowException thrown = assertThrows(TestControlFlowException.class,
                () -> RunnableUtils.rethrowIfCancellation(ex));

        assertSame(ex, thrown);
    }

    @Test
    public void testRethrowsCancellationException() {
        CancellationException ex = new CancellationException();

        CancellationException thrown = assertThrows(CancellationException.class,
                () -> RunnableUtils.rethrowIfCancellation(ex));

        assertSame(ex, thrown);
    }

    @Test
    public void testIgnoresOtherExceptions() {
        RunnableUtils.rethrowIfCancellation(new IllegalStateException("not a cancellation"));
        RunnableUtils.rethrowIfCancellation(new Exception("checked, not a cancellation"));
    }
}
