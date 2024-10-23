package com.evolveum.midpoint.studio.impl;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetConfiguration implements FacetConfiguration {

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new MidPointFacetEditorTab()};
    }
}
