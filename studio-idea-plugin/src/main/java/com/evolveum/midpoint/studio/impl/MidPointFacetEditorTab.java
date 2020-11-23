package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.ui.MidPointFacetEditorPanel;
import com.intellij.facet.ui.FacetEditorTab;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetEditorTab extends FacetEditorTab {

    @NotNull
    @Override
    public JComponent createComponent() {
        return new MidPointFacetEditorPanel();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return MidPointFacetType.FACET_NAME;
    }

    @Override
    public boolean isModified() {
        return false;
    }
}
