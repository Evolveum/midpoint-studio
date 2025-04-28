package com.evolveum.midpoint.studio.impl.assistant;

import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.Gray;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Created by Dominik.
 */
public class SimpleInlayRenderer implements EditorCustomElementRenderer {
    private final String text;

    public SimpleInlayRenderer(String text) {
        this.text = text;
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        Font font = inlay.getEditor().getColorsScheme().getFont(EditorFontType.PLAIN);
        FontMetrics metrics = inlay.getEditor().getContentComponent().getFontMetrics(font);
        return metrics.stringWidth(text);
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle targetRegion, @NotNull TextAttributes textAttributes) {
        g.setColor(Gray._150);
        g.setFont(new Font("Monospaced", Font.PLAIN, g.getFont().getSize()));
        g.drawString(text, targetRegion.x, targetRegion.y + g.getFontMetrics().getAscent());
    }
}
