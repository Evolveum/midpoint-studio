package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.MidPointSettings;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.DataManager;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.util.DisposeAwareRunnable;
import org.apache.commons.lang3.StringUtils;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointUtils {

    private static final Random RANDOM = new Random();

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile("\\$(t|T|s|e|n|o)");

    public static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    @Deprecated
    public static Project getCurrentProject() {
        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        return DataKeys.PROJECT.getData(dataContext);
    }

    public static void createAndPushNotification(String group, String title, String content, NotificationType type,
                                                 NotificationAction... actions) {

        Notification notification = new Notification(group, title, content, type);
        Arrays.stream(actions).forEach(a -> notification.addAction(a));

        Notifications.Bus.notify(notification);
    }

    public static Color generateAwtColor() {
        float hue = RANDOM.nextFloat();
        // Saturation between 0.1 and 0.3
        float saturation = (RANDOM.nextInt(2000) + 1000) / 10000f;

        return Color.getHSBColor(hue, saturation, 0.9f);
    }

    public static LookupElement buildLookupElement(String name, String oid, String source, int priority) {
        LookupElementBuilder builder = buildLookupElement(name, oid, source);
        LookupElement element = builder.withAutoCompletionPolicy(AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE);

        return PrioritizedLookupElement.withPriority(element, priority);
    }

    public static LookupElementBuilder buildLookupElement(String name, String oid, String source) {
        return LookupElementBuilder.create(oid)
                .withTailText("(" + name + ")")
                .withTypeText(source)
                .withBoldness(true)
                .withCaseSensitivity(false);
    }

    public static String escapeObjectName(String name) {
        if (name == null) {
            return null;
        }

        String escaped = StringUtils.stripAccents(name);
        escaped = escaped.replaceAll("[\\s]+", "-");
        return escaped.replaceAll("[^A-Za-z0-9]", "");
    }

    public static void runWhenInitialized(final Project project, final Runnable r) {
        if (project.isDisposed()) {
            return;
        }

        if (isNoBackgroundMode()) {
            r.run();
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(DisposeAwareRunnable.create(r, project));
            return;
        }

        runDumbAware(project, r);
    }

    public static boolean isNoBackgroundMode() {
        return ApplicationManager.getApplication().isUnitTestMode()
                || ApplicationManager.getApplication().isHeadlessEnvironment();
    }

    public static void runDumbAware(Project project, Runnable runnable) {
        if (DumbService.isDumbAware(runnable)) {
            runnable.run();
        } else {
            DumbService.getInstance(project).runWhenSmart(DisposeAwareRunnable.create(runnable, project));
        }
    }

    public static String replaceFilePath(String text, Map<String, String> properties) {
        Matcher matcher = FILE_PATH_PATTERN.matcher(text);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement = properties.get(matcher.group(1));
            if (replacement == null) {
                replacement = "null";
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static Credentials getCredentials(String key) {
        CredentialAttributes attributes = createCredentialAttributes(key);

        PasswordSafe safe = PasswordSafe.getInstance();

        return safe.get(attributes);
    }

    public static String getPassword(String key) {
        CredentialAttributes attributes = createCredentialAttributes(key);

        PasswordSafe safe = PasswordSafe.getInstance();
        return safe.getPassword(attributes);
    }

    public static void setCredentials(String key, String username, String password) {
        CredentialAttributes attributes = createCredentialAttributes(key);
        Credentials credentials = username == null && password == null ? null : new Credentials(username, password);
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    public static void setPassword(String key, String password) {
        CredentialAttributes attributes = createCredentialAttributes(key);
        PasswordSafe.getInstance().setPassword(attributes, password);
    }

    private static CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt
                .generateServiceName(MidPointSettings.class.getSimpleName(), key));
    }

    public static void publishNotification(String key, String title, String content, NotificationType type) {
        publishNotification(key, title, content, type, null);
    }

    public static void publishNotification(String key, String title, String content, NotificationType type,
                                           NotificationAction... actions) {
        Notification notification = new Notification(key, title, content, type);
        if (actions != null) {
            Arrays.asList(actions).forEach(a -> {

                if (a != null) {
                    notification.addAction(a);
                }
            });
        }
        Notifications.Bus.notify(notification);
    }

    public static Map<String, Object> mapOf(Entry<String, Object>... entries) {
        Map<String, Object> map = new HashMap<>();

        if (entries == null) {
            return map;
        }

        for (Entry<String, Object> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }
}
