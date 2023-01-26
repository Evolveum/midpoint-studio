package com.evolveum.midpoint.studio.lang.axiom;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomFileType extends LanguageFileType {

    public static final AxiomFileType INSTANCE = new AxiomFileType();

    private AxiomFileType() {
        super(AxiomLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Axiom File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Axiom language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "axiom";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AxiomLanguage.ICON;
    }
}
