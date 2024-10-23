package com.evolveum.midpoint.studio.lang.axiom;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class AxiomFileRoot extends PsiFileBase {

    public AxiomFileRoot(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, AxiomLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return AxiomFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Axiom file";
    }
}
