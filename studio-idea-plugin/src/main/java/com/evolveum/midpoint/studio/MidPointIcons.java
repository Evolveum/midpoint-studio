package com.evolveum.midpoint.studio;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointIcons {

    public static final Icon ACTION_RANDOM_OID = IconLoader.findIcon("/icons/random_oid.png");

    public static final @NotNull Icon Midpoint = load("icons/midpoint.svg");

    public static final @NotNull Icon RemoveRaw = load("icons/removeRaw.svg");

    public static final @NotNull Icon ServerLog = load("icons/serverLog.svg");

    public static final @NotNull Icon TestConnection = load("icons/testConnection.svg");

    public static final @NotNull Icon UploadExecute = load("icons/uploadExecute.svg");

    public static final @NotNull Icon UploadExecuteStop = load("icons/uploadExecuteStop.svg");

    public static final @NotNull Icon UploadTest = load("icons/uploadTest.svg");

    public static final @NotNull Icon UploadTestValidate = load("icons/uploadTestValidate.svg");

    private static @NotNull Icon load(@NotNull String path) {
        return IconManager.getInstance().getIcon(path, MidPointIcons.class);
    }
}
