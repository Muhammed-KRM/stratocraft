package me.mami.stratocraft.util;

import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.mami.stratocraft.Main;

/**
 * Özel Blok Veri Yönetimi
 * 
 * ✅ YENİ: CustomBlockData kütüphanesi kullanılıyor (TileState olmayan bloklar için)
 * PersistentDataContainer kullanarak özel blok verilerini kalıcı şekilde tutar.
 * Server restart sonrası veriler korunur.
 */
public class CustomBlockData {
    private static Main plugin;
    
    // NamespacedKey'ler
    private static final NamespacedKey CLAN_FENCE_KEY = new NamespacedKey("stratocraft", "clan_fence");
    private static final NamespacedKey CLAN_CRYSTAL_KEY = new NamespacedKey("stratocraft", "clan_crystal");
    private static final NamespacedKey STRUCTURE_CORE_KEY = new NamespacedKey("stratocraft", "structure_core");
    private static final NamespacedKey STRUCTURE_CORE_OWNER_KEY = new NamespacedKey("stratocraft", "structure_core_owner");
    private static final NamespacedKey TRAP_CORE_KEY = new NamespacedKey("stratocraft", "trap_core");
    private static final NamespacedKey TRAP_CORE_OWNER_KEY = new NamespacedKey("stratocraft", "trap_core_owner");
    private static final NamespacedKey CLAN_BANK_KEY = new NamespacedKey("stratocraft", "clan_bank");
    
    // ✅ PERFORMANS: Reflection cache (reflection çağrıları pahalı)
    private static Class<?> cachedCustomBlockDataClass = null;
    private static java.lang.reflect.Constructor<?> cachedConstructor = null;
    private static java.lang.reflect.Method cachedRegisterMethod = null;
    private static final Object reflectionLock = new Object();
    
    // ✅ PERFORMANS: PDC container cache (Location bazlı, 5 saniye cache)
    private static final java.util.Map<String, PersistentDataContainer> pdcCache = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Long> pdcCacheTime = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final long PDC_CACHE_DURATION = 5000L; // 5 saniye
    
    /**
     * Plugin instance'ı set et (Main.java'da çağrılmalı)
     * ✅ YENİ: CustomBlockData kütüphanesi listener'ını kaydet
     * ✅ PERFORMANS: Reflection cache kullanılıyor
     */
    public static void initialize(Main mainPlugin) {
        plugin = mainPlugin;
        // ✅ CustomBlockData kütüphanesi listener'ını kaydet (blok kırılınca otomatik temizlik)
        synchronized (reflectionLock) {
            try {
                // ✅ PERFORMANS: Reflection cache kullan (ilk çağrıda cache'le)
                if (cachedCustomBlockDataClass == null) {
                    cachedCustomBlockDataClass = Class.forName("me.mami.stratocraft.lib.customblockdata.CustomBlockData");
                    cachedRegisterMethod = cachedCustomBlockDataClass.getMethod("registerListener", org.bukkit.plugin.Plugin.class);
                }
                // Cache'den al ve çağır
                if (cachedRegisterMethod != null) {
                    cachedRegisterMethod.invoke(null, mainPlugin);
                }
            } catch (Exception e) {
                // Kütüphane henüz yüklenmemiş olabilir, bu normal (build sonrası çalışacak)
                mainPlugin.getLogger().info("CustomBlockData listener kaydedilemedi (build sonrası aktif olacak): " + e.getMessage());
            }
        }
    }
    
    /**
     * ✅ YENİ: CustomBlockData kütüphanesi ile PDC al (TileState olmayan bloklar için)
     * ✅ PERFORMANS: Reflection cache + PDC cache kullanılıyor
     */
    private static PersistentDataContainer getCustomBlockDataContainer(Block block) {
        if (block == null || plugin == null) return null;
        
        // ✅ DÜZELTME: Chunk yükleme kontrolü (PDC okumak için chunk yüklü olmalı)
        org.bukkit.Chunk chunk = block.getChunk();
        if (!chunk.isLoaded()) {
            // Chunk yüklenmemiş, yükle
            chunk.load(false);
        }
        
        // ✅ PERFORMANS: Location bazlı cache key oluştur
        String cacheKey = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        long now = System.currentTimeMillis();
        
        // ✅ Cache kontrolü
        PersistentDataContainer cached = pdcCache.get(cacheKey);
        Long cacheTime = pdcCacheTime.get(cacheKey);
        if (cached != null && cacheTime != null && now - cacheTime < PDC_CACHE_DURATION) {
            return cached; // Cache'den dön
        }
        
        try {
            synchronized (reflectionLock) {
                // ✅ PERFORMANS: Reflection cache kullan (ilk çağrıda cache'le)
                if (cachedCustomBlockDataClass == null) {
                    cachedCustomBlockDataClass = Class.forName("me.mami.stratocraft.lib.customblockdata.CustomBlockData");
                }
                if (cachedConstructor == null) {
                    cachedConstructor = cachedCustomBlockDataClass.getConstructor(
                        org.bukkit.block.Block.class, 
                        org.bukkit.plugin.Plugin.class
                    );
                }
                
                // ✅ DÜZELTME: Chunk yüklü mü tekrar kontrol et (constructor çağrısından önce)
                if (!chunk.isLoaded()) {
                    // Chunk hala yüklenmemiş, container alınamaz
                    return null;
                }
                
                // Cache'den constructor'ı kullan
                Object instance = cachedConstructor.newInstance(block, plugin);
                PersistentDataContainer container = (PersistentDataContainer) instance;
                
                // ✅ Cache'e kaydet
                pdcCache.put(cacheKey, container);
                pdcCacheTime.put(cacheKey, now);
                
                return container;
            }
        } catch (ClassNotFoundException e) {
            // Kütüphane henüz build edilmemiş, bu normal (maven build sonrası çalışacak)
            return null;
        } catch (Exception e) {
            // Diğer hatalar
            if (plugin != null) {
                plugin.getLogger().fine("CustomBlockData container alınamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * ✅ PERFORMANS: PDC cache'i temizle (blok kırıldığında veya değiştiğinde)
     */
    public static void clearPDCCache(Block block) {
        if (block == null) return;
        String cacheKey = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        pdcCache.remove(cacheKey);
        pdcCacheTime.remove(cacheKey);
    }
    
    /**
     * ✅ PERFORMANS: Periyodik cache temizleme (eski cache'leri sil)
     */
    public static void cleanupPDCCache() {
        long now = System.currentTimeMillis();
        java.util.Iterator<java.util.Map.Entry<String, Long>> iterator = pdcCacheTime.entrySet().iterator();
        while (iterator.hasNext()) {
            java.util.Map.Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > PDC_CACHE_DURATION) {
                // Cache süresi dolmuş, temizle
                pdcCache.remove(entry.getKey());
                iterator.remove();
            }
        }
    }
    
    // ========== KLAN ÇİTİ METODLARI ==========
    
    /**
     * Klan çiti verisini kaydet
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_FENCE TileState değil)
     * 
     * @param block Çit bloğu
     * @param clanId Klan ID'si (null olabilir)
     * @return Başarılıysa true
     */
    public static boolean setClanFenceData(Block block, UUID clanId) {
        if (block == null) return false;
        
        try {
            // ✅ DÜZELTME: Chunk yükleme kontrolü (PDC yazmak için chunk yüklü olmalı)
            org.bukkit.Chunk chunk = block.getChunk();
            if (!chunk.isLoaded()) {
                // Chunk yüklenmemiş, yükle
                chunk.load(false);
                // Chunk yüklenemediyse hata dön
                if (!chunk.isLoaded()) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Klan çiti verisi kaydedilemedi: Chunk yüklenemedi");
                    }
                    return false;
                }
            }
            
            // ✅ ÖNCE TileState kontrolü (TileState ise normal PDC kullan)
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Klan çiti verisi kaydedilemedi: CustomBlockData container alınamadı");
                    }
                    return false;
                }
            }
            
            if (container == null) {
                if (plugin != null) {
                    plugin.getLogger().warning("Klan çiti verisi kaydedilemedi: Container null");
                }
                return false;
            }
            
            if (clanId != null) {
                container.set(CLAN_FENCE_KEY, PersistentDataType.STRING, clanId.toString());
                // ✅ Cache'i temizle (veri değişti)
                if (!isTileState) {
                    clearPDCCache(block);
                }
            } else {
                container.remove(CLAN_FENCE_KEY);
                // ✅ Cache'i temizle (veri silindi)
                if (!isTileState) {
                    clearPDCCache(block);
                }
            }
            
            // ✅ TileState ise update() çağır
            if (isTileState) {
                ((TileState) state).update();
            }
            
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan çiti verisi kaydedilemedi: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * Klan çiti verisini oku
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_FENCE TileState değil)
     * 
     * @param block Çit bloğu
     * @return Klan ID'si (yoksa null)
     */
    public static UUID getClanFenceData(Block block) {
        if (block == null) return null;
        
        try {
            // ✅ DÜZELTME: Chunk yükleme kontrolü (PDC okumak için chunk yüklü olmalı)
            org.bukkit.Chunk chunk = block.getChunk();
            if (!chunk.isLoaded()) {
                // Chunk yüklenmemiş, yükle
                boolean loaded = chunk.load(false);
                // Chunk yüklenemediyse null dön
                if (!loaded || !chunk.isLoaded()) {
                    return null; // Chunk yüklenemedi
                }
            }
            
            // ✅ DÜZELTME: State'i chunk yüklendikten sonra al (daha güvenilir)
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    // ✅ DÜZELTME: Container null ise, chunk yüklü mü tekrar kontrol et
                    if (!chunk.isLoaded()) {
                        return null; // Chunk yüklenemedi
                    }
                    // ✅ DÜZELTME: Container null ise, cache'i temizle ve tekrar dene
                    clearPDCCache(block);
                    container = getCustomBlockDataContainer(block);
                    if (container == null) {
                        return null; // Container alınamadı
                    }
                }
            }
            
            if (container == null) {
                return null; // Container null
            }
            
            if (container.has(CLAN_FENCE_KEY, PersistentDataType.STRING)) {
                String clanIdStr = container.get(CLAN_FENCE_KEY, PersistentDataType.STRING);
                if (clanIdStr == null || clanIdStr.isEmpty()) {
                    return null;
                }
                try {
                    return UUID.fromString(clanIdStr);
                } catch (IllegalArgumentException e) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Klan çiti UUID formatı geçersiz: " + clanIdStr);
                    }
                    return null;
                }
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan çiti verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Klan çiti mi kontrol et
     * 
     * @param block Çit bloğu
     * @return Klan çiti ise true
     */
    public static boolean isClanFence(Block block) {
        if (block == null || block.getType() != org.bukkit.Material.OAK_FENCE) {
            return false;
        }
        
        return getClanFenceData(block) != null;
    }
    
    /**
     * Klan çiti verisini temizle
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_FENCE TileState değil)
     * ✅ PERFORMANS: Cache temizleme
     * 
     * @param block Çit bloğu
     */
    public static void removeClanFenceData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return;
                }
            }
            
            if (container != null) {
                container.remove(CLAN_FENCE_KEY);
                
                // ✅ TileState ise update() çağır
                if (isTileState) {
                    ((TileState) state).update();
                } else {
                    // ✅ Cache'i temizle (veri silindi)
                    clearPDCCache(block);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan çiti verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== KLAN KRİSTALİ METODLARI ==========
    
    /**
     * Klan kristali verisini kaydet
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (END_CRYSTAL TileState değil)
     * 
     * @param block Kristal bloğu
     * @param clanId Klan ID'si (null olabilir)
     * @return Başarılıysa true
     */
    public static boolean setClanCrystalData(Block block, UUID clanId) {
        if (block == null) return false;
        
        try {
            // ✅ ÖNCE TileState kontrolü (TileState ise normal PDC kullan)
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Klan kristali verisi kaydedilemedi: CustomBlockData container alınamadı");
                    }
                    return false;
                }
            }
            
            if (clanId != null) {
                container.set(CLAN_CRYSTAL_KEY, PersistentDataType.STRING, clanId.toString());
            } else {
                container.remove(CLAN_CRYSTAL_KEY);
                // ✅ Cache'i temizle (veri silindi)
                if (!isTileState) {
                    clearPDCCache(block);
                }
            }
            
            // ✅ TileState ise update() çağır
            if (isTileState) {
                ((TileState) state).update();
            }
            
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan kristali verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Klan kristali verisini oku
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (END_CRYSTAL TileState değil)
     * 
     * @param block Kristal bloğu
     * @return Klan ID'si (yoksa null)
     */
    public static UUID getClanCrystalData(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return null;
                }
            }
            
            if (container != null && container.has(CLAN_CRYSTAL_KEY, PersistentDataType.STRING)) {
                String clanIdStr = container.get(CLAN_CRYSTAL_KEY, PersistentDataType.STRING);
                return UUID.fromString(clanIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan kristali verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Klan kristali mi kontrol et
     * 
     * @param block Kristal bloğu
     * @return Klan kristali ise true
     */
    public static boolean isClanCrystal(Block block) {
        if (block == null) return false;
        
        return getClanCrystalData(block) != null;
    }
    
    /**
     * Klan kristali verisini temizle
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (END_CRYSTAL TileState değil)
     * ✅ PERFORMANS: Cache temizleme
     * 
     * @param block Kristal bloğu
     */
    public static void removeClanCrystalData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return;
                }
            }
            
            if (container != null) {
                container.remove(CLAN_CRYSTAL_KEY);
                
                // ✅ TileState ise update() çağır
                if (isTileState) {
                    ((TileState) state).update();
                } else {
                    // ✅ Cache'i temizle (veri silindi)
                    clearPDCCache(block);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan kristali verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== YAPI ÇEKİRDEĞİ METODLARI ==========
    
    /**
     * Yapı çekirdeği verisini kaydet
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_LOG TileState değil)
     * ✅ PERFORMANS: Cache kullanılıyor
     */
    public static boolean setStructureCoreData(Block block, UUID ownerId) {
        if (block == null) return false;
        
        try {
            // ✅ ÖNCE TileState kontrolü (TileState ise normal PDC kullan)
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Yapı çekirdeği verisi kaydedilemedi: CustomBlockData container alınamadı");
                    }
                    return false;
                }
            }
            
            if (ownerId != null) {
                container.set(STRUCTURE_CORE_KEY, PersistentDataType.BYTE, (byte) 1);
                container.set(STRUCTURE_CORE_OWNER_KEY, PersistentDataType.STRING, ownerId.toString());
            } else {
                container.remove(STRUCTURE_CORE_KEY);
                container.remove(STRUCTURE_CORE_OWNER_KEY);
                // ✅ Cache'i temizle (veri silindi)
                if (!isTileState) {
                    clearPDCCache(block);
                }
            }
            
            // ✅ TileState ise update() çağır
            if (isTileState) {
                ((TileState) state).update();
            }
            
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Yapı çekirdeği verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Yapı çekirdeği verisini oku
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_LOG TileState değil)
     */
    public static UUID getStructureCoreOwner(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return null;
                }
            }
            
            if (container != null && container.has(STRUCTURE_CORE_OWNER_KEY, PersistentDataType.STRING)) {
                String ownerIdStr = container.get(STRUCTURE_CORE_OWNER_KEY, PersistentDataType.STRING);
                return UUID.fromString(ownerIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Yapı çekirdeği verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Yapı çekirdeği mi kontrol et
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_LOG TileState değil)
     */
    public static boolean isStructureCore(Block block) {
        if (block == null || block.getType() != org.bukkit.Material.OAK_LOG) {
            return false;
        }
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return false;
                }
            }
            
            return container != null && container.has(STRUCTURE_CORE_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Yapı çekirdeği verisini temizle
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (OAK_LOG TileState değil)
     * ✅ PERFORMANS: Cache temizleme
     */
    public static void removeStructureCoreData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return;
                }
            }
            
            if (container != null) {
                container.remove(STRUCTURE_CORE_KEY);
                container.remove(STRUCTURE_CORE_OWNER_KEY);
                
                // ✅ TileState ise update() çağır
                if (isTileState) {
                    ((TileState) state).update();
                } else {
                    // ✅ Cache'i temizle (veri silindi)
                    clearPDCCache(block);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Yapı çekirdeği verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== TUZAK ÇEKİRDEĞİ METODLARI ==========
    
    /**
     * Tuzak çekirdeği verisini kaydet
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (LODESTONE TileState değil)
     * ✅ PERFORMANS: Cache kullanılıyor
     */
    public static boolean setTrapCoreData(Block block, UUID ownerId) {
        if (block == null) return false;
        
        try {
            // ✅ ÖNCE TileState kontrolü (TileState ise normal PDC kullan)
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Tuzak çekirdeği verisi kaydedilemedi: CustomBlockData container alınamadı");
                    }
                    return false;
                }
            }
            
            if (ownerId != null) {
                container.set(TRAP_CORE_KEY, PersistentDataType.BYTE, (byte) 1);
                container.set(TRAP_CORE_OWNER_KEY, PersistentDataType.STRING, ownerId.toString());
            } else {
                container.remove(TRAP_CORE_KEY);
                container.remove(TRAP_CORE_OWNER_KEY);
                // ✅ Cache'i temizle (veri silindi)
                if (!isTileState) {
                    clearPDCCache(block);
                }
            }
            
            // ✅ TileState ise update() çağır
            if (isTileState) {
                ((TileState) state).update();
            }
            
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Tuzak çekirdeği verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Tuzak çekirdeği verisini oku
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (LODESTONE TileState değil)
     */
    public static UUID getTrapCoreOwner(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return null;
                }
            }
            
            if (container != null && container.has(TRAP_CORE_OWNER_KEY, PersistentDataType.STRING)) {
                String ownerIdStr = container.get(TRAP_CORE_OWNER_KEY, PersistentDataType.STRING);
                return UUID.fromString(ownerIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Tuzak çekirdeği verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Tuzak çekirdeği mi kontrol et
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (LODESTONE TileState değil)
     */
    public static boolean isTrapCore(Block block) {
        if (block == null || block.getType() != org.bukkit.Material.LODESTONE) {
            return false;
        }
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return false;
                }
            }
            
            return container != null && container.has(TRAP_CORE_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Tuzak çekirdeği verisini temizle
     * ✅ DÜZELTME: CustomBlockData kütüphanesi kullanılıyor (LODESTONE TileState değil)
     * ✅ PERFORMANS: Cache temizleme
     */
    public static void removeTrapCoreData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return;
                }
            }
            
            if (container != null) {
                container.remove(TRAP_CORE_KEY);
                container.remove(TRAP_CORE_OWNER_KEY);
                
                // ✅ TileState ise update() çağır
                if (isTileState) {
                    ((TileState) state).update();
                } else {
                    // ✅ Cache'i temizle (veri silindi)
                    clearPDCCache(block);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Tuzak çekirdeği verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== KLAN BANKASI METODLARI ==========
    
    /**
     * Klan bankası verisini kaydet
     */
    public static boolean setClanBankData(Block block, UUID clanId) {
        if (block == null) return false;
        
        try {
            // ✅ ÖNCE TileState kontrolü (TileState ise normal PDC kullan)
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    if (plugin != null) {
                        plugin.getLogger().warning("Klan bankası verisi kaydedilemedi: CustomBlockData container alınamadı");
                    }
                    return false;
                }
            }
            
            if (clanId != null) {
                container.set(CLAN_BANK_KEY, PersistentDataType.STRING, clanId.toString());
            } else {
                container.remove(CLAN_BANK_KEY);
                // ✅ Cache'i temizle (veri silindi)
                if (!isTileState) {
                    clearPDCCache(block);
                }
            }
            
            // ✅ TileState ise update() çağır
            if (isTileState) {
                ((TileState) state).update();
            }
            
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan bankası verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Klan bankası verisini oku
     */
    public static UUID getClanBankData(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            
            if (state instanceof TileState) {
                // ✅ TileState ise normal PDC kullan
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return null;
                }
            }
            
            if (container != null && container.has(CLAN_BANK_KEY, PersistentDataType.STRING)) {
                String clanIdStr = container.get(CLAN_BANK_KEY, PersistentDataType.STRING);
                return UUID.fromString(clanIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan bankası verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Klan bankası mı kontrol et
     */
    public static boolean isClanBank(Block block) {
        if (block == null) return false;
        
        return getClanBankData(block) != null;
    }
    
    /**
     * Klan bankası verisini temizle
     */
    public static void removeClanBankData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = null;
            boolean isTileState = false;
            
            if (state instanceof TileState) {
                TileState tileState = (TileState) state;
                container = tileState.getPersistentDataContainer();
                isTileState = true;
            } else {
                // ✅ TileState değilse CustomBlockData kütüphanesi kullan (cache ile)
                container = getCustomBlockDataContainer(block);
                if (container == null) {
                    return;
                }
            }
            
            if (container != null) {
                container.remove(CLAN_BANK_KEY);
                
                // ✅ TileState ise update() çağır
                if (isTileState) {
                    ((TileState) state).update();
                } else {
                    // ✅ Cache'i temizle (veri silindi)
                    clearPDCCache(block);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan bankası verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
}

