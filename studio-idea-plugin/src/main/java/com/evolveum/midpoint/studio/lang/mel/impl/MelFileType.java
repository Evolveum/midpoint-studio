package com.evolveum.midpoint.studio.lang.mel.impl;

import com.evolveum.midpoint.studio.lang.mel.MelIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MelFileType extends LanguageFileType {

    public static final MelFileType INSTANCE = new MelFileType();

    private MelFileType() {
        super(MelLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return MelConstants.FILE_TYPE;
    }

    @NotNull
    @Override
    public String getDescription() {
        return MelConstants.FILE_TYPE_DESCRIPTION;
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return MelConstants.FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return MelIcons.ICON_FILE_TYPE;
    }
}
