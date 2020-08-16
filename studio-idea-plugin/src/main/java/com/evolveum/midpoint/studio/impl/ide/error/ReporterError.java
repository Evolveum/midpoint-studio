package com.evolveum.midpoint.studio.impl.ide.error;

import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.util.ExceptionUtil;

import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ReporterError {

    private static final String KEY_STACK_TRACE = "error.stacktrace";

    private static final String KEY_LAST_ACTION = "Last Action";

    private static final String KEY_MESSAGE = "error.message";

    private static final String KEY_DESCRIPTION = "error.description";

    private static final String KEY_PLUGIN_NAME = "Plugin Name";

    private static final String KEY_PLUGIN_VERSION = "Plugin Version";

    private static final String KEY_EXCEPTION_HASH = "exception.hash";

    private static final String KEY_ATTACHMENTS = "attachments";

    private static final String KEY_OS_NAME = "OS Name";

    private static final String KEY_JAVA_VERSION = "Java Version";

    private static final String KEY_JAVA_VM_VENDOR = "Java VM Vendor";

    private static final String KEY_APP_NAME = "App Name";

    private static final String KEY_APP_FULL_NAME = "App Full Name";

    private static final String KEY_APP_VERSION_NAME = "App Version Name";

    private static final String KEY_IS_EAP = "Is EAP";

    private static final String KEY_APP_BUILD = "App Build";

    private static final String KEY_APP_VERSION = "App Version";

    private Map<String, Object> values = new HashMap<>();

    public ReporterError(Throwable throwable, String lastAction) {
        if (throwable != null) {
            setStackTrace(ExceptionUtil.getThrowableText(throwable));
            setMessage(throwable.getMessage());

            int hashCode = Arrays.hashCode(throwable.getStackTrace());
            setExceptionHash(String.valueOf(hashCode));
        }

        setLastAction(lastAction);
    }

    public String getStackTrace() {
        return (String) values.get(KEY_STACK_TRACE);
    }

    public String getLastAction() {
        return (String) values.get(KEY_LAST_ACTION);
    }

    public String getMessage() {
        return (String) values.get(KEY_MESSAGE);
    }

    public String getDescription() {
        return (String) values.get(KEY_DESCRIPTION);
    }

    public String getPluginName() {
        return (String) values.get(KEY_PLUGIN_NAME);
    }

    public String getPluginVersion() {
        return (String) values.get(KEY_PLUGIN_VERSION);
    }

    public List<Attachment> getAttachments() {
        List<Attachment> attachments = (List) values.get(KEY_ATTACHMENTS);
        if (attachments == null) {
            attachments = new ArrayList<>();
            values.put(KEY_ATTACHMENTS, attachments);
        }

        return attachments;
    }

    public String getExceptionHash() {
        return (String) values.get(KEY_EXCEPTION_HASH);
    }

    public String getOsName() {
        return (String) values.get(KEY_OS_NAME);
    }

    public String getJavaVersion() {
        return (String) values.get(KEY_JAVA_VERSION);
    }

    public String getJavaVmVendor() {
        return (String) values.get(KEY_JAVA_VM_VENDOR);
    }

    public String getAppName() {
        return (String) values.get(KEY_APP_NAME);
    }

    public String getAppFullName() {
        return (String) values.get(KEY_APP_FULL_NAME);
    }

    public String getAppVersionName() {
        return (String) values.get(KEY_APP_VERSION_NAME);
    }

    public Boolean isEap() {
        return (Boolean) values.get(KEY_IS_EAP);
    }

    public String getAppBuild() {
        return (String) values.get(KEY_APP_BUILD);
    }

    public String getAppVersion() {
        return (String) values.get(KEY_APP_VERSION);
    }

    public void setMessage(String message) {
        values.put(KEY_MESSAGE, message);
    }

    public void setDescription(String description) {
        values.put(KEY_DESCRIPTION, description);
    }

    public void setPluginName(String pluginName) {
        values.put(KEY_PLUGIN_NAME, pluginName);
    }

    public void setPluginVersion(String pluginVersion) {
        values.put(KEY_PLUGIN_VERSION, pluginVersion);
    }

    public void setStackTrace(String stackTrace) {
        values.put(KEY_STACK_TRACE, stackTrace);
    }

    public void setLastAction(String lastAction) {
        values.put(KEY_LAST_ACTION, lastAction);
    }

    private void setExceptionHash(String exceptionHash) {
        values.put(KEY_EXCEPTION_HASH, exceptionHash);
    }

    public void setAttachments(List<Attachment> attachments) {
        values.put(KEY_ATTACHMENTS, attachments);
    }

    public void setOsName(String osName) {
        values.put(KEY_OS_NAME, osName);
    }

    public void setJavaVersion(String javaVersion) {
        values.put(KEY_JAVA_VERSION, javaVersion);
    }

    public void setJavaVmVendor(String javaVmVendor) {
        values.put(KEY_JAVA_VM_VENDOR, javaVmVendor);
    }

    public void setAppName(String appName) {
        values.put(KEY_APP_NAME, appName);
    }

    public void setAppFullName(String name) {
        values.put(KEY_APP_FULL_NAME, name);
    }

    public void setAppVersionName(String appVersionName) {
        values.put(KEY_APP_VERSION_NAME, appVersionName);
    }

    public void setEap(Boolean isEap) {
        values.put(KEY_IS_EAP, isEap);
    }

    public void setAppBuild(String appBuild) {
        values.put(KEY_APP_BUILD, appBuild);
    }

    public void setAppVersion(String appVersion) {
        values.put(KEY_APP_VERSION, appVersion);
    }
}
