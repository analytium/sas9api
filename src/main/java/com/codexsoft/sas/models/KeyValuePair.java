package com.codexsoft.sas.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class KeyValuePair<K, V> {
    private K key;
    private V value;

    public static <K, V> Map<K, V> fromList(List<KeyValuePair<K, V>> list) {
        return new HashMap<K, V>() {{
            for(KeyValuePair<K, V> item : list) {
                this.put(item.getKey(), item.getValue());
            }
        }};
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
