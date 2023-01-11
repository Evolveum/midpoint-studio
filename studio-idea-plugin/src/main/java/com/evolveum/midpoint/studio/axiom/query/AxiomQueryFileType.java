package com.evolveum.midpoint.studio.axiom.query;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class AxiomQueryFileType extends LanguageFileType {

    public static final AxiomQueryFileType INSTANCE = new AxiomQueryFileType();

    private AxiomQueryFileType() {
        super(AxiomQueryLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Axiom Query File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Axiom Query file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "axq";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AxiomQueryLanguage.ICON;
    }
}
