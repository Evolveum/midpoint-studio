package com.evolveum.midpoint.studio.lang.properties;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class StudioPropertiesFileRoot extends PsiFileBase {

    public StudioPropertiesFileRoot(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, StudioPropertiesLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return StudioPropertiesFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Studio properties file";
    }
}
