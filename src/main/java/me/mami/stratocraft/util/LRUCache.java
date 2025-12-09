package me.mami.stratocraft.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache (Least Recently Used)
 * En son kullanılmayan entry'ler otomatik silinir
 * Memory leak önleme için kullanılır
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;
    
    /**
     * LRU Cache oluştur
     * 
     * @param maxSize Maksimum entry sayısı
     */
    public LRUCache(int maxSize) {
        super(16, 0.75f, true); // accessOrder = true (LRU için)
        this.maxSize = maxSize;
    }
    
    /**
     * En eski entry silinmeli mi?
     * Max size aşılırsa en eski entry otomatik silinir
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}

