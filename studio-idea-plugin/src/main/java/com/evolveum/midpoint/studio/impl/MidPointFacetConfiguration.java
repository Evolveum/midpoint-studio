package com.evolveum.midpoint.studio.impl;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetConfiguration implements FacetConfiguration {//}, PersistentStateComponent<MidPointSettings>, ModificationTracker {

//    private MidPointSettings settings;
//
//    private long modificationCount;
//
//    @Override
//    public long getModificationCount() {
//        return modificationCount;
//    }
//
//    @Nullable
//    @Override
//    public MidPointSettings getState() {
//        return settings;
//    }
//
//    @Override
//    public void loadState(MidPointSettings state) {
//        MidPointSettings settings = MidPointSettings.createDefaultSettings();
//        XmlSerializerUtil.copyBean(state, settings);
//
//        setSettings(settings);
//    }
//
//    public void setSettings(MidPointSettings settings) {
//        this.settings = settings;
//
//        modificationCount++;
//    }
//
//    @Override
//    public void noStateLoaded() {
//        MidPointSettings settings = MidPointSettings.createDefaultSettings();
//        setSettings(settings);
//    }

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new MidPointFacetEditorTab()};
    }
}
