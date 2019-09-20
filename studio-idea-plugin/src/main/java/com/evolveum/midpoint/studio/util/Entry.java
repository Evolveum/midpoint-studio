package com.evolveum.midpoint.studio.util;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Entry<K, V> {

    private K key;

    private V value;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
