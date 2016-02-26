package com.cesarparent.utils;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by cesar on 25/02/2016.
 *
 * Similar to WeakHashMap in that it doesn't retain keys, except value objects are not retained
 * either, making it suitable for a in-memory model object cache.
 *
 * TODO: Implement the full java.util.Map interface
 */
public class WeakMapTable<K, V> {

    private WeakHashMap<K, WeakReference<V>> _map;

    public WeakMapTable() {
        _map = new WeakHashMap<>();
    }

    public V get(K key) {
        WeakReference<V> ref = _map.get(key);
        if(ref == null) {
            return null;
        }
        V obj = ref.get();
        if(obj == null) {
            _map.remove(key);
        }
        return obj;
    }

    public void put(K key, V value) {
        _map.put(key, new WeakReference<>(value));
    }

    public boolean containsKey(K key) {
        return _map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return _map.containsValue(new WeakReference<>(value));
    }

    public V remove(K key) {
        WeakReference<V> ref = _map.remove(key);
        if(ref == null) {
            return null;
        }
        return ref.get();
    }

    public void clear() {
        _map.clear();
    }

    public int size() {
        return _map.size();
    }
}
