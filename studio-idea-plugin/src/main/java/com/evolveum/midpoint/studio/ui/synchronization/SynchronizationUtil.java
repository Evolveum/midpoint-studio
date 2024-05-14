package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.ModificationType;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;

import java.awt.*;
import java.util.Set;

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

    public static ModificationType getModificationType(Set<ModificationType> set) {
        if (set == null
                || set.isEmpty()
                || set.stream().allMatch(m -> m == null)) {
            return null;
        }

        if (set.size() > 1) {
            return ModificationType.REPLACE;
        } else if (set.size() == 1) {
            return set.iterator().next();
        }

        return null;
    }
}
