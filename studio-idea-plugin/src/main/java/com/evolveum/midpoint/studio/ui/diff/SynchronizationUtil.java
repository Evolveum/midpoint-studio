package com.evolveum.midpoint.studio.ui.diff;

import com.evolveum.midpoint.prism.ModificationType;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;

import java.awt.*;

public class SynchronizationUtil {

    public static final ColorKey ADDED = ColorKey.createColorKey("FILESTATUS_ADDED");

    public static final ColorKey DELETED = ColorKey.createColorKey("FILESTATUS_DELETED");

    public static final ColorKey MODIFIED = ColorKey.createColorKey("FILESTATUS_MODIFIED");

    public static Color getColorForModificationType(ModificationType modificationType) {
        if (modificationType == null) {
            return null;
        }

        EditorColorsScheme scheme = EditorColorsManager.getInstance().getSchemeForCurrentUITheme();
        return switch (modificationType) {
            case ADD -> scheme.getColor(ADDED);
            case DELETE -> scheme.getColor(DELETED);
            case REPLACE -> scheme.getColor(MODIFIED);
        };
    }
}
