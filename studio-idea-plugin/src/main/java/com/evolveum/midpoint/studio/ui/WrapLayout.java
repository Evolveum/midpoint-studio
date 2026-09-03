/*
 *
 *  * Copyright (C) 2010-2025 Evolveum and contributors
 *  *
 *  * Licensed under the EUPL-1.2 or later.
 *
 */

package com.evolveum.midpoint.studio.ui;

import java.awt.*;

public class WrapLayout extends FlowLayout {
    public WrapLayout() { super(LEFT); }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int maxWidth = target.getParent().getWidth();

            if (maxWidth == 0) {
                maxWidth = Integer.MAX_VALUE;
            }

            int width = 0, height = getVgap();
            int rowHeight = 0;

            for (Component m : target.getComponents()) {
                if (!m.isVisible()) continue;

                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                if (width + d.width > maxWidth) {
                    width = 0;
                    height += rowHeight + getVgap();
                    rowHeight = 0;
                }

                width += d.width + getHgap();
                rowHeight = Math.max(rowHeight, d.height);
            }

            height += rowHeight;
            return new Dimension(maxWidth, height);
        }
    }
}
