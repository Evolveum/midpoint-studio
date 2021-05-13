package com.evolveum.midpoint.studio;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointIcons {

    public static final Icon ACTION_MIDPOINT = IconLoader.findIcon("/icons/midpoint.png");

    public static final Icon ACTION_RANDOM_OID = IconLoader.findIcon("/icons/random_oid.png");

    public static final @NotNull Icon RemoveRaw = load("icons/removeRaw.svg", 1640828675481524950L, 2);

    public static final @NotNull Icon ServerLog = load("icons/serverLog.svg", 1650828675481524950L, 2);

    public static final @NotNull Icon TestConnection = load("icons/testConnection.svg", 1660828675481524950L, 2);

    public static final @NotNull Icon UploadExecute = load("icons/uploadExecute.svg", 1670828675481524950L, 2);

    public static final @NotNull Icon UploadExecuteStop = load("icons/uploadExecuteStop.svg", 1680828675481524950L, 2);

    public static final @NotNull Icon UploadTest = load("icons/uploadTest.svg", 1690828675481524949L, 2);

    public static final @NotNull Icon UploadTestValidate = load("icons/uploadTestValidate.svg", 1730828675481524950L, 2);

    private static @NotNull Icon load(@NotNull String path, long cacheKey, int flags) {
        return IconManager.getInstance().loadRasterizedIcon(path, MidPointIcons.class.getClassLoader(), cacheKey, flags);
    }
}
