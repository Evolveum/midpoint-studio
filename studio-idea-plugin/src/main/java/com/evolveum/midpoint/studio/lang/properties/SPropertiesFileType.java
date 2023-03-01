package com.evolveum.midpoint.studio.lang.properties;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class SPropertiesFileType extends LanguageFileType {

    public static final SPropertiesFileType INSTANCE = new SPropertiesFileType();

    private SPropertiesFileType() {
        super(SPropertiesLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Studio Properties File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Studio Properties file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "sproperties";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return SPropertiesLanguage.ICON;
    }
}
