package com.evolveum.midpoint.studio.impl.cache;

public record CacheEntry<T>(T data, long timestamp) {
}