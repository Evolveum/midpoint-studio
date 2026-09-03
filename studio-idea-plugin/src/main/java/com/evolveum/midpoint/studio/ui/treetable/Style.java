package com.evolveum.midpoint.studio.ui.treetable;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Immutable cell styling descriptor: optional foreground color, background color, and font.
 * Null fields mean "use default" (from selection state or table defaults).
 */
public final class Style {

    @Nullable private final Color foreground;
    @Nullable private final Color background;
    @Nullable private final Font font;

    private Style(@Nullable Color foreground, @Nullable Color background, @Nullable Font font) {
        this.foreground = foreground;
        this.background = background;
        this.font = font;
    }

    public static Style of(@Nullable Color foreground, @Nullable Color background, @Nullable Font font) {
        return new Style(foreground, background, font);
    }

    /** Returns a style with the IDE's disabled label foreground color (theme-aware). */
    public static Style disabled() {
        return new Style(UIUtil.getLabelDisabledForeground(), null, null);
    }

    public static Style success() {
        return new Style(UIUtil.getLabelSuccessForeground(), null, null);
    }

    public static Style error() {
        return new Style(UIUtil.getErrorForeground(), null, null);
    }

    public static Style background(Color background) {
        return new Style(null, background, null);
    }

    public static Style foreground(Color foreground) {
        return new Style(foreground, null, null);
    }

    /**
     * Merges this style with a row-level style. Cell-level values (this) take priority.
     * Returns a new CellStyle combining both, preferring non-null values from this.
     */
    public Style mergeOver(Style rowStyle) {
        if (rowStyle == null) {
            return this;
        }
        return new Style(
                foreground != null ? foreground : rowStyle.foreground,
                background != null ? background : rowStyle.background,
                font != null ? font : rowStyle.font
        );
    }

    @Nullable
    public Color getForeground() {
        return foreground;
    }

    @Nullable
    public Color getBackground() {
        return background;
    }

    @Nullable
    public Font getFont() {
        return font;
    }
}
