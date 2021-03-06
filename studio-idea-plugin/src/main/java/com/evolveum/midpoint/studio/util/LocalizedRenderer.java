package com.evolveum.midpoint.studio.util;

import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LocalizedRenderer<T extends Localized> extends SimpleListCellRenderer<T> {

    private String nullText;

    public LocalizedRenderer() {
        this(null);
    }

    public LocalizedRenderer(String nullText) {
        this.nullText = nullText;
    }

    public void setNullText(String nullText) {
        this.nullText = nullText;
    }

    @Override
    public void customize(@NotNull JList<? extends T> list, T value, int index, boolean selected, boolean hasFocus) {
        String text = value != null ? value.getKey() : nullText;
        setText(text);
    }
}
