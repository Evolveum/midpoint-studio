package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.client.TestConnectionResult;
import com.evolveum.midpoint.studio.impl.Environment;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Covers the order of work after a connection test result is known: the result notification
 * must be published before the (slow) environment cache reload, and the reload must run only
 * for a successful test.
 */
public class TestConnectionActionTest extends BasePlatformTestCase {

    private final List<String> events = new ArrayList<>();

    private final List<Notification> notifications = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getProject().getMessageBus().connect(getTestRootDisposable()).subscribe(Notifications.TOPIC, new Notifications() {

            @Override
            public void notify(@NotNull Notification notification) {
                events.add("notification");
                notifications.add(notification);
            }
        });
    }

    public void testNotificationPublishedBeforeCacheReloadOnSuccess() {
        TestConnectionResult result = new TestConnectionResult(true, "4.11.0", "abc123");

        TestConnectionAction.handleResult(getProject(), environment(), result, () -> events.add("reload"));

        assertEquals(List.of("notification", "reload"), events);

        Notification notification = notifications.getFirst();
        assertEquals(NotificationType.INFORMATION, notification.getType());
        assertTrue(notification.getContent(), notification.getContent().contains("4.11.0"));
        assertTrue(notification.getContent(), notification.getContent().contains("abc123"));
    }

    public void testCachesNotReloadedOnFailure() {
        TestConnectionResult result = new TestConnectionResult(false, new IllegalStateException("connection refused"));

        TestConnectionAction.handleResult(getProject(), environment(), result, () -> events.add("reload"));

        assertEquals(List.of("notification"), events);

        Notification notification = notifications.getFirst();
        assertEquals(NotificationType.ERROR, notification.getType());
        // exception is available through the "Show exception" action
        assertEquals(1, notification.getActions().size());
    }

    public void testFailureWithoutExceptionHasNoAction() {
        TestConnectionResult result = new TestConnectionResult(false, (Exception) null);

        TestConnectionAction.handleResult(getProject(), environment(), result, () -> events.add("reload"));

        assertEquals(List.of("notification"), events);
        assertEquals(0, notifications.getFirst().getActions().size());
    }

    private static Environment environment() {
        Environment env = new Environment("env-id");
        env.setName("demo");
        return env;
    }
}
