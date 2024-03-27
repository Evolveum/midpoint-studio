package com.evolveum.midpoint.studio.ui;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record NamedItem<T>(T value, @NotNull Supplier<String> name) {

}
