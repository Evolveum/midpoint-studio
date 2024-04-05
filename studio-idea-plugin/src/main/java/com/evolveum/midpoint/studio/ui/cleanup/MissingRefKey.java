package com.evolveum.midpoint.studio.ui.cleanup;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;

public record MissingRefKey(@NotNull String oid, QName type) {
}
