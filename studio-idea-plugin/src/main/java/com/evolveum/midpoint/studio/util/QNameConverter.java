package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.util.QNameUtil;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;

public class QNameConverter extends Converter<QName> {

    @Override
    public @Nullable QName fromString(@NotNull String value) {
        return QNameUtil.uriToQName(value);
    }

    @Override
    public @Nullable String toString(@NotNull QName value) {
        return QNameUtil.qNameToUri(value);
    }
}
