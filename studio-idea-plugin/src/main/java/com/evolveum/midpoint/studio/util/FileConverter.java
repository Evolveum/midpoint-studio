package com.evolveum.midpoint.studio.util;

import com.intellij.util.xmlb.Converter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FileConverter extends Converter<File> {

    @Nullable
    @Override
    public File fromString(@NotNull String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        return new File(value);
    }

    @Nullable
    @Override
    public String toString(@NotNull File value) {
        return value.getPath();
    }
}
