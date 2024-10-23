package com.evolveum.midpoint.studio.lang.properties;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class SPropertiesFileRoot extends PsiFileBase {

    public SPropertiesFileRoot(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, SPropertiesLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return SPropertiesFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Studio properties file";
    }
}
