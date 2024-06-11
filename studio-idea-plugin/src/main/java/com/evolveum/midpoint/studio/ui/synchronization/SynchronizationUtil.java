package com.evolveum.midpoint.studio.ui.synchronization;

import com.evolveum.midpoint.prism.ModificationType;
import com.evolveum.midpoint.studio.ui.diff.DiffEditor;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SynchronizationUtil {

    public static final ColorKey ADDED = ColorKey.createColorKey("FILESTATUS_ADDED");

    public static final ColorKey DELETED = ColorKey.createColorKey("FILESTATUS_DELETED");

    public static final ColorKey MODIFIED = ColorKey.createColorKey("FILESTATUS_MODIFIED");

    public static final ColorKey IGNORED = ColorKey.createColorKey("FILESTATUS_IDEA_FILESTATUS_IGNORED");

    public static Color getColor(ColorKey key) {
        if (key == null) {
            return null;
        }

        EditorColorsScheme scheme = EditorColorsManager.getInstance().getSchemeForCurrentUITheme();
        return scheme.getColor(key);
    }

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

    public static void closeDiffEditors(Project project, List<String> ids) {
        RunnableUtils.invokeLaterIfNeeded(() -> {
            FileEditorManager fem = FileEditorManager.getInstance(project);
            List<DiffEditor> editors = Arrays.stream(fem.getAllEditors())
                    .filter(e -> e instanceof DiffEditor)
                    .map(e -> (DiffEditor) e)
                    .filter(e -> ids.contains(e.getFile().getProcessor().getId()))
                    .toList();

            editors.forEach(e -> fem.closeFile(e.getFile()));
        });
    }
}
