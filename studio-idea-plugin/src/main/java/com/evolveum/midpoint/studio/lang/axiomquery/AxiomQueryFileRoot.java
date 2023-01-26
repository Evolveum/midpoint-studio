package com.evolveum.midpoint.studio.lang.axiomquery;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class AxiomQueryFileRoot extends PsiFileBase {

    public AxiomQueryFileRoot(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, AxiomQueryLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return AxiomQueryFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Axiom query file";
    }
}
