package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.studio.impl.browse.Generator;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GeneratorRenderer extends SimpleListCellRenderer<Generator> {

    @Override
    public void customize(@NotNull JList<? extends Generator> list, Generator value, int index,
                          boolean selected, boolean hasFocus) {
        if (value == null) {
            return;
        }

        setText(value.getLabel());
        setToolTipText(value.getActionDescription());
    }
}
