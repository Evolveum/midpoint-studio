package com.evolveum.midpoint.studio.lang.mel.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class MelFileRoot extends PsiFileBase {

    public MelFileRoot(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, MelLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return MelFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return MelConstants.FILE_TYPE;
    }
}
