package me.mami.stratocraft.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.ClanPowerProfile;
import me.mami.stratocraft.model.PlayerPowerProfile;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.LRUCache;

/**
 * Stratocraft Güç Sistemi (SGP - Stratocraft Global Power)
 * 
 * Bu sistem, rapor tasarımına göre oluşturulmuş merkezi güç hesaplama sistemidir.
 * 
 * Özellikler:
 * - Modüler yapı: Her özellik için ayrı fonksiyon
 * - Config tabanlı: Tüm değerler config'den yönetilir
 * - Cache sistemi: Performans optimizasyonu
 * - Profile sistemi: PlayerPowerProfile ve ClanPowerProfile
 * - Hibrit seviye sistemi: Karekök + Logaritmik
 * - Gelişmiş koruma: Acemi koruması, klan savaşı istisnası
 */
public class StratocraftPowerSystem {
    private final Main plugin;
    private final ClanManager clanManager;
    private final TrainingManager trainingManager;
    private final SpecialItemManager specialItemManager;
    private final BuffManager buffManager;
    private final TerritoryManager territoryManager;
    private final SiegeManager siegeManager;
    
    // Config
    private ClanPowerConfig powerConfig;
    
    // Cache (performans için) - Thread-safe + LRU
    private final Map<UUID, PlayerPowerProfile> playerProfileCache = 
        Collections.synchronizedMap(new LRUCache<>(500)); // Max 500 oyuncu
    private final Map<UUID, Long> playerProfileCacheTime = new ConcurrentHashMap<>();
    private final Map<UUID, ClanPowerProfile> clanProfileCache = new ConcurrentHashMap<>();
    
    // Offline player cache (24 saat geçerli)
    private final Map<UUID, PlayerPowerProfile> offlinePlayerCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> offlineCacheTime = new ConcurrentHashMap<>();
    private static final long OFFLINE_CACHE_DURATION = 86400000L; // 24 saat
    
    // Training data cache (performans için)
    private final Map<UUID, Map<String, Integer>> trainingDataCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> trainingDataCacheTime = new ConcurrentHashMap<>();
    private static final long TRAINING_CACHE_DURATION = 30000L; // 30 saniye
    
    // Buff power cache (event-based)
    private final Map<UUID, Double> buffPowerCache = new ConcurrentHashMap<>();
    
    // Player lookup cache (network overhead önleme)
    private final Map<UUID, Player> playerLookupCache = new ConcurrentHashMap<>();
    
    // Clan locks (race condition önleme)
    private final Map<UUID, Object> clanLocks = new ConcurrentHashMap<>();
    
    // Player locks (race condition önleme)
    private final Map<UUID, Object> playerLocks = new ConcurrentHashMap<>();
    
    // Delta sistemi: Ritüel blok tracking (event-based)
    private final Map<UUID, me.mami.stratocraft.model.ClanRitualBlockSnapshot> ritualBlockSnapshots = new ConcurrentHashMap<>();
    
    // Ritüel kaynak istatistikleri (sadece başarılı ritüeller için)
    private final Map<UUID, me.mami.stratocraft.model.ClanRitualResourceStats> ritualResourceStats = new ConcurrentHashMap<>();
    
    // Cache süreleri
    private static final long PLAYER_CACHE_DURATION = 5000L; // 5 saniye
    private static final long CLAN_CACHE_DURATION = 300000L; // 5 dakika
    
    public StratocraftPowerSystem(Main plugin, ClanManager clanManager,
                                  TrainingManager trainingManager,
                                  SpecialItemManager specialItemManager,
                                  BuffManager buffManager,
                                  TerritoryManager territoryManager,
                                  SiegeManager siegeManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.trainingManager = trainingManager;
        this.specialItemManager = specialItemManager;
        this.buffManager = buffManager;
        this.territoryManager = territoryManager;
        this.siegeManager = siegeManager;
        this.powerConfig = new ClanPowerConfig();
    }
    
    /**
     * Config'den ayarları yükle
     */
    public void loadConfig(FileConfiguration config) {
        powerConfig.loadFromConfig(config);
        
        // Periyodik cache temizleme başlat
        startCacheCleanupTask();
        
        // Player lookup cache güncelleme başlat
        startPlayerLookupCacheTask();
        
        // Periyodik persistence (güç profillerini kaydet)
        startPersistenceTask();
    }
    
    /**
     * Periyodik persistence task'ı başlat (her 5 dakikada bir)
     */
    private void startPersistenceTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            saveAllPlayerProfiles();
        }, 6000L, 60000L); // Her 1 dakikada bir (async)
    }
    
    /**
     * Tüm oyuncu profillerini kaydet (async)
     */
    private void saveAllPlayerProfiles() {
        for (Map.Entry<UUID, PlayerPowerProfile> entry : playerProfileCache.entrySet()) {
            savePlayerProfile(entry.getKey(), entry.getValue());
        }
        
        // Offline cache'deki profilleri de kaydet
        for (Map.Entry<UUID, PlayerPowerProfile> entry : offlinePlayerCache.entrySet()) {
            savePlayerProfile(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Tüm oyuncu profillerini kaydet (sync - onDisable için)
     */
    public void saveAllPlayerProfilesSync() {
        try {
            File powerProfilesDir = new File(plugin.getDataFolder(), "data/power_profiles");
            powerProfilesDir.mkdirs();
            
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .create();
            
            int saved = 0;
            
            // Online cache'deki profilleri kaydet
            for (Map.Entry<UUID, PlayerPowerProfile> entry : playerProfileCache.entrySet()) {
                File file = new File(powerProfilesDir, entry.getKey().toString() + ".json");
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    gson.toJson(entry.getValue(), writer);
                    saved++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Güç profili kaydetme hatası (" + entry.getKey() + "): " + e.getMessage());
                }
            }
            
            // Offline cache'deki profilleri kaydet
            for (Map.Entry<UUID, PlayerPowerProfile> entry : offlinePlayerCache.entrySet()) {
                File file = new File(powerProfilesDir, entry.getKey().toString() + ".json");
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    gson.toJson(entry.getValue(), writer);
                    saved++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Güç profili kaydetme hatası (" + entry.getKey() + "): " + e.getMessage());
                }
            }
            
            plugin.getLogger().info("§a" + saved + " oyuncu güç profili kaydedildi!");
        } catch (Exception e) {
            plugin.getLogger().severe("§cGüç profilleri kaydetme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Oyuncu profilini kaydet (async)
     */
    public void savePlayerProfile(UUID playerId, PlayerPowerProfile profile) {
        if (playerId == null || profile == null) return;
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File powerProfilesDir = new File(plugin.getDataFolder(), "data/power_profiles");
                powerProfilesDir.mkdirs();
                
                File file = new File(powerProfilesDir, playerId.toString() + ".json");
                
                com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .setPrettyPrinting()
                    .create();
                
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    gson.toJson(profile, writer);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Güç profili kaydetme hatası (" + playerId + "): " + e.getMessage());
            }
        });
    }
    
    /**
     * Oyuncu profilini yükle (sync - Main thread'de çağrılmalı)
     */
    public PlayerPowerProfile loadPlayerProfile(UUID playerId) {
        if (playerId == null) return null;
        
        File file = new File(plugin.getDataFolder(), 
            "data/power_profiles/" + playerId.toString() + ".json");
        
        if (!file.exists()) return null;
        
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            try (java.io.FileReader reader = new java.io.FileReader(file)) {
                PlayerPowerProfile profile = gson.fromJson(reader, PlayerPowerProfile.class);
                if (profile != null) {
                    // Offline cache'e ekle
                    offlinePlayerCache.put(playerId, profile);
                    offlineCacheTime.put(playerId, System.currentTimeMillis());
                }
                return profile;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Güç profili yükleme hatası (" + playerId + "): " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Tüm oyuncu profillerini yükle (sunucu başlangıcında)
     */
    public void loadAllPlayerProfiles() {
        File powerProfilesDir = new File(plugin.getDataFolder(), "data/power_profiles");
        if (!powerProfilesDir.exists() || !powerProfilesDir.isDirectory()) {
            return;
        }
        
        File[] files = powerProfilesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;
        
        int loaded = 0;
        for (File file : files) {
            try {
                String fileName = file.getName();
                String uuidString = fileName.substring(0, fileName.length() - 5); // .json'u çıkar
                UUID playerId = UUID.fromString(uuidString);
                
                PlayerPowerProfile profile = loadPlayerProfile(playerId);
                if (profile != null) {
                    loaded++;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Güç profili yükleme hatası: " + file.getName() + " - " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("§a" + loaded + " oyuncu güç profili yüklendi!");
    }
    
    /**
     * Periyodik cache temizleme task'ı başlat
     */
    private void startCacheCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            cleanupOldCache();
        }, 6000L, 300000L); // 5 dakikada bir (async)
    }
    
    /**
     * Player lookup cache güncelleme task'ı başlat
     */
    private void startPlayerLookupCacheTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updatePlayerLookupCache();
        }, 20L, 100L); // Her 5 saniyede bir (sync - Bukkit API)
    }
    
    /**
     * Eski cache'leri temizle
     */
    private void cleanupOldCache() {
        long now = System.currentTimeMillis();
        long playerExpireTime = now - (PLAYER_CACHE_DURATION * 2); // 10 saniye
        long offlineExpireTime = now - OFFLINE_CACHE_DURATION; // 24 saat
        long trainingExpireTime = now - TRAINING_CACHE_DURATION; // 30 saniye
        
        // Player cache temizle
        playerProfileCacheTime.entrySet().removeIf(entry -> {
            if (entry.getValue() < playerExpireTime) {
                playerProfileCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
        
        // Offline cache temizle
        offlineCacheTime.entrySet().removeIf(entry -> {
            if (entry.getValue() < offlineExpireTime) {
                offlinePlayerCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
        
        // Training cache temizle
        trainingDataCacheTime.entrySet().removeIf(entry -> {
            if (entry.getValue() < trainingExpireTime) {
                trainingDataCache.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Player lookup cache güncelle
     */
    private void updatePlayerLookupCache() {
        playerLookupCache.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerLookupCache.put(player.getUniqueId(), player);
        }
    }
    
    // ========== OYUNCU GÜÇ HESAPLAMA ==========
    
    /**
     * Oyuncu güç profili hesapla (tüm bileşenleri topla)
     * Thread-safe: Double-check locking ile race condition önleme
     */
    public PlayerPowerProfile calculatePlayerProfile(Player player) {
        if (player == null || !player.isOnline()) {
            return new PlayerPowerProfile();
        }
        
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Atomic get (thread-safe)
        PlayerPowerProfile cached = playerProfileCache.get(playerId);
        if (cached != null) {
            Long cacheTime = playerProfileCacheTime.get(playerId);
            if (cacheTime != null && now - cacheTime < PLAYER_CACHE_DURATION) {
                return cached; // Cache'den dön
            }
        }
        
        // Double-check locking (race condition önleme)
        Object lock = playerLocks.computeIfAbsent(playerId, k -> new Object());
        synchronized (lock) {
            // Tekrar kontrol et (başka thread hesaplamış olabilir)
            cached = playerProfileCache.get(playerId);
            if (cached != null) {
                Long cacheTime = playerProfileCacheTime.get(playerId);
                if (cacheTime != null && now - cacheTime < PLAYER_CACHE_DURATION) {
                    return cached;
                }
            }
            
            // Hesaplama (sadece bir thread)
            PlayerPowerProfile profile = calculatePlayerProfileInternal(player, now);
            
            // Cache'e kaydet (atomic)
            playerProfileCache.put(playerId, profile);
            playerProfileCacheTime.put(playerId, now);
            
            return profile;
        }
    }
    
    /**
     * Internal hesaplama (synchronized dışında)
     */
    private PlayerPowerProfile calculatePlayerProfileInternal(Player player, long now) {
        PlayerPowerProfile profile = new PlayerPowerProfile();
        
        // 1. Eşya gücü (histerezis ile)
        double gearPower = calculateGearPower(player);
        profile.setGearPower(gearPower);
        
        // 2. Ustalık gücü
        profile.setTrainingPower(calculatePlayerTrainingMasteryPower(player));
        
        // 3. Buff gücü (cache'den)
        profile.setBuffPower(getCachedBuffPower(player));
        
        // 4. Ritüel gücü (oyuncu bazlı, gelecekte eklenebilir)
        profile.setRitualPower(0.0);
        
        // Toplamlar (histerezis ile etkili güç kullan)
        double effectiveGearPower = profile.getEffectiveGearPower(powerConfig.getGearDecreaseDelay());
        double combatPower = effectiveGearPower + profile.getBuffPower();
        double progressionPower = profile.getTrainingPower() + profile.getRitualPower();
        
        // Ağırlıklı toplam (config'den)
        double combatWeight = powerConfig.getCombatPowerWeight();
        double progressionWeight = powerConfig.getProgressionPowerWeight();
        
        double totalSGP = (combatPower * combatWeight) + (progressionPower * progressionWeight);
        
        profile.setTotalCombatPower(combatPower);
        profile.setTotalProgressionPower(progressionPower);
        profile.setTotalSGP(totalSGP);
        
        // Seviye hesapla (hibrit sistem)
        profile.setPlayerLevel(powerConfig.calculatePlayerLevel(totalSGP));
        profile.setLastUpdate(now);
        
        // ✅ GÜÇ GEÇMİŞİ: Güç değişimini logla (sadece önemli değişimlerde)
        // Cache'den önceki değeri al ve karşılaştır
        UUID playerId = player.getUniqueId();
        PlayerPowerProfile cachedProfile = playerProfileCache.get(playerId);
        if (cachedProfile != null) {
            double oldPower = cachedProfile.getTotalSGP();
            double change = Math.abs(totalSGP - oldPower);
            
            // Sadece önemli değişimlerde logla (100'den fazla veya %10'dan fazla)
            if (change > 100 || (oldPower > 0 && change / oldPower > 0.1)) {
                logPowerChange(player, totalSGP);
            }
        } else {
            // İlk hesaplama, logla
            logPowerChange(player, totalSGP);
        }
        
        return profile;
    }
    
    /**
     * Güç değişimini logla (basit versiyon)
     * ✅ PERFORMANS: Sadece önemli değişimlerde çağrılır
     */
    private void logPowerChange(Player player, double newPower) {
        // SimplePowerHistory varsa kullan
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getSimplePowerHistory() != null) {
            plugin.getSimplePowerHistory().logPowerChange(player, newPower);
        }
    }
    
    /**
     * Eşya gücü hesapla (silah + zırh + özel itemler + materyaller)
     */
    public double calculateGearPower(Player player) {
        return calculateWeaponPower(player) + 
               calculateArmorPower(player) + 
               calculateSpecialItemPower(player) +
               calculateMaterialPower(player);
    }
    
    /**
     * Silah gücü hesapla (envanterdeki tüm seviyeli silahların toplamı)
     * Aynı item'dan birden fazla varsa, hepsinin gücü toplanır (stack boyutuna göre)
     */
    private double calculateWeaponPower(Player player) {
        double totalPower = 0.0;
        
        // Envanterdeki tüm itemleri kontrol et (tüm seviyeli silahların gücünü topla)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
            
            // Seviyeli silah mı?
            if (ItemManager.isLeveledWeapon(item)) {
                int level = ItemManager.getWeaponLevel(item);
                if (level > 0) {
                    double power = powerConfig.getWeaponPower(level);
                    // Stack boyutuna göre çarp (eğer birden fazla varsa)
                    totalPower += power * item.getAmount();
                }
            }
        }
        
        return totalPower;
    }
    
    /**
     * Zırh gücü hesapla (takılı zırh + envanterdeki tüm seviyeli zırhlar)
     * Aynı zırh parçasından birden fazla varsa, hepsinin gücü toplanır (stack boyutuna göre)
     */
    private double calculateArmorPower(Player player) {
        double totalPower = 0.0;
        double equippedPower = 0.0;
        int equippedPieces = 0;
        
        // Önce takılı zırhları kontrol et
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece != null) {
                int level = ItemManager.getArmorLevel(piece);
                if (level > 0) {
                    double power = powerConfig.getArmorPower(level);
                    // Takılı zırh için stack boyutuna göre çarp
                    totalPower += power * piece.getAmount();
                    equippedPower += power * piece.getAmount();
                    equippedPieces++;
                }
            }
        }
        
        // Envanterdeki tüm seviyeli zırhları kontrol et (takılı olmayanlar)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
            
            // Seviyeli zırh mı?
            if (ItemManager.isLeveledArmor(item)) {
                int level = ItemManager.getArmorLevel(item);
                if (level > 0) {
                    double power = powerConfig.getArmorPower(level);
                    // Stack boyutuna göre çarp (eğer birden fazla varsa)
                    totalPower += power * item.getAmount();
                }
            }
        }
        
        // Tam set bonusu (4 parça takılıysa)
        if (equippedPieces == 4 && equippedPower > 0) {
            // Sadece takılı zırhların gücüne bonus uygula
            double bonus = equippedPower * (powerConfig.getArmorSetBonus() - 1.0); // Sadece bonus kısmı
            totalPower += bonus;
        }
        
        return totalPower;
    }
    
    /**
     * Özel item gücü (envanterdeki tüm özel itemler)
     * Aynı özel item'dan birden fazla varsa, hepsinin gücü toplanır (stack boyutuna göre)
     */
    private double calculateSpecialItemPower(Player player) {
        if (specialItemManager == null) return 0.0;
        
        double totalPower = 0.0;
        
        // Envanterdeki tüm itemleri kontrol et
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
            
            // Özel item kontrolü (SpecialItemManager'dan)
            if (item.hasItemMeta()) {
                org.bukkit.NamespacedKey specialItemKey = new org.bukkit.NamespacedKey(
                    plugin, "special_item_id");
                String specialItemId = item.getItemMeta().getPersistentDataContainer()
                    .get(specialItemKey, org.bukkit.persistence.PersistentDataType.STRING);
                
                if (specialItemId != null) {
                    // Özel item gücü (config'den tier'a göre)
                    // Özel itemlerin tier'ını al (weapon_level veya armor_level key'inden)
                    org.bukkit.NamespacedKey tierKey = new org.bukkit.NamespacedKey(
                        plugin, "weapon_level");
                    Integer tier = item.getItemMeta().getPersistentDataContainer()
                        .get(tierKey, org.bukkit.persistence.PersistentDataType.INTEGER);
                    
                    if (tier == null) {
                        // armor_level'dan dene
                        tierKey = new org.bukkit.NamespacedKey(plugin, "armor_level");
                        tier = item.getItemMeta().getPersistentDataContainer()
                            .get(tierKey, org.bukkit.persistence.PersistentDataType.INTEGER);
                    }
                    
                    if (tier != null && tier > 0) {
                        // Config'den özel item gücü al (eğer varsa)
                        // Şimdilik basit: Her tier için 50 puan
                        // Stack boyutuna göre çarp (eğer birden fazla varsa)
                        totalPower += (tier * 50.0) * item.getAmount();
                    }
                }
            }
        }
        
        return totalPower;
    }
    
    /**
     * Materyal gücü hesapla (envanterdeki değerli materyaller)
     * Elmas, Karanlık Madde, Obsidyen, Kızıl Elmas, Titanyum vb.
     */
    private double calculateMaterialPower(Player player) {
        double totalPower = 0.0;
        
        // Envanterdeki tüm itemleri kontrol et
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
            
            // Seviyeli silah/zırh/özel item değilse materyal kontrolü yap
            if (!ItemManager.isLeveledWeapon(item) && 
                !ItemManager.isLeveledArmor(item) && 
                !isSpecialItem(item)) {
                
                // Önce özel item kontrolü yap (Karanlık Madde, Kızıl Elmas, Titanyum)
                // ItemManager.create() metodu "custom_id" key'ini kullanıyor
                if (item.hasItemMeta()) {
                    org.bukkit.NamespacedKey customItemKey = new org.bukkit.NamespacedKey(
                        plugin, "custom_id");
                    String customItemId = item.getItemMeta().getPersistentDataContainer()
                        .get(customItemKey, org.bukkit.persistence.PersistentDataType.STRING);
                    
                    if (customItemId != null) {
                        // Özel item gücü (config'den)
                        double specialMaterialPower = powerConfig.getSpecialMaterialPower(customItemId);
                        if (specialMaterialPower > 0) {
                            // Stack boyutuna göre çarp
                            totalPower += specialMaterialPower * item.getAmount();
                            continue; // Özel item bulundu, normal materyal kontrolüne geçme
                        }
                    }
                }
                
                // Normal materyal gücü (config'den)
                double materialPower = powerConfig.getMaterialPower(item.getType());
                if (materialPower > 0) {
                    // Stack boyutuna göre çarp
                    totalPower += materialPower * item.getAmount();
                }
            }
        }
        
        return totalPower;
    }
    
    /**
     * Özel item kontrolü (SpecialItemManager'dan)
     */
    private boolean isSpecialItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        // SpecialItemManager'dan kontrol et
        if (specialItemManager != null) {
            org.bukkit.NamespacedKey specialItemKey = new org.bukkit.NamespacedKey(
                plugin, "special_item_id");
            return item.getItemMeta().getPersistentDataContainer()
                .has(specialItemKey, org.bukkit.persistence.PersistentDataType.STRING);
        }
        return false;
    }
    
    /**
     * Oyuncu ustalık gücü hesapla (cache ile)
     */
    public double calculatePlayerTrainingMasteryPower(Player player) {
        if (trainingManager == null) return 0.0;
        
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        Map<String, Integer> playerTraining = trainingDataCache.get(playerId);
        if (playerTraining == null) {
            Long cacheTime = trainingDataCacheTime.get(playerId);
            if (cacheTime == null || now - cacheTime >= TRAINING_CACHE_DURATION) {
                // Cache'den al (thread-safe)
                Map<UUID, Map<String, Integer>> allData = trainingManager.getAllTrainingData();
                if (allData != null) {
                    Map<String, Integer> data = allData.get(playerId);
                    if (data != null) {
                        // Defensive copy (thread-safe)
                        playerTraining = new ConcurrentHashMap<>(data);
                        trainingDataCache.put(playerId, playerTraining);
                        trainingDataCacheTime.put(playerId, now);
                    } else {
                        playerTraining = new ConcurrentHashMap<>();
                    }
                } else {
                    playerTraining = new ConcurrentHashMap<>();
                }
            } else {
                playerTraining = trainingDataCache.get(playerId);
            }
        }
        
        if (playerTraining == null || playerTraining.isEmpty()) return 0.0;
        
        double totalPower = 0.0;
        
        for (String ritualId : playerTraining.keySet()) {
            int totalUses = playerTraining.getOrDefault(ritualId, 0);
            
            // Ustalık yüzdesi hesapla
            double masteryPercent = calculateMasteryPercent(totalUses, ritualId);
            
            if (masteryPercent > 100) {
                totalPower += powerConfig.getMasteryPower(masteryPercent);
            }
        }
        
        return totalPower;
    }
    
    /**
     * Ustalık yüzdesi hesapla
     * TrainingManager'dan gerçek ustalık seviyesi al ve yüzdeye çevir
     * Formül: Her seviye = %100 ustalık (seviye 1 = %100, seviye 2 = %200, vb.)
     */
    private double calculateMasteryPercent(int totalUses, String ritualId) {
        if (trainingManager == null) {
            // Fallback: Basit hesaplama (100 kullanım = %100)
            return (double) totalUses;
        }
        
        UUID playerId = null;
        // Player'dan UUID almak için cache'den bak
        // Şimdilik basit: totalUses'ı seviyeye çevir
        // TrainingManager'ın getMasteryLevel() metodu var ama playerId gerekiyor
        // Bu yüzden basit formül kullanıyoruz: Her 100 kullanım = 1 seviye = %100
        
        // Basit formül: 100 kullanım = %100, 200 kullanım = %200
        // Gelecekte TrainingManager'dan gerçek seviye alınabilir
        return (double) totalUses; // 200 kullanım = %200 ustalık
    }
    
    /**
     * Buff gücü hesapla (cache'den)
     */
    private double calculateBuffPower(Player player) {
        return getCachedBuffPower(player);
    }
    
    /**
     * Cache'den buff gücü al (event-based güncelleme)
     */
    private double getCachedBuffPower(Player player) {
        UUID playerId = player.getUniqueId();
        Double cached = buffPowerCache.get(playerId);
        if (cached != null) {
            return cached;
        }
        
        // Cache yoksa hesapla
        double totalPower = calculateBuffPowerInternal(player);
        buffPowerCache.put(playerId, totalPower);
        return totalPower;
    }
    
    /**
     * Buff gücü hesapla (internal)
     */
    private double calculateBuffPowerInternal(Player player) {
        double totalPower = 0.0;
        
        // PotionEffect buffları
        for (PotionEffect effect : player.getActivePotionEffects()) {
            int amplifier = effect.getAmplifier() + 1;
            // Basit hesaplama: Her amplifier seviyesi için 10 puan
            totalPower += amplifier * 10.0;
        }
        
        // BuffManager buffları (Klan bazlı)
        Clan clan = clanManager != null ? clanManager.getClanByPlayer(player.getUniqueId()) : null;
        if (buffManager != null && clan != null) {
            // ✅ BUFF ENTEGRASYONU: Klan bufflarından güç al
            if (buffManager.hasConquerorBuff(clan)) {
                // Fatih Buff'ı: %20 daha fazla hasar → 200 güç puanı
                totalPower += 200.0;
            }
            if (buffManager.hasHeroBuff(clan)) {
                // Kahraman Buff'ı: %15 daha fazla can + %25 savunma → 150 güç puanı
                totalPower += 150.0;
            }
        }
        
        return totalPower;
    }
    
    /**
     * Buff gücü cache'ini güncelle (event-based)
     */
    public void updateBuffPowerCache(Player player) {
        if (player == null || !player.isOnline()) return;
        double totalPower = calculateBuffPowerInternal(player);
        buffPowerCache.put(player.getUniqueId(), totalPower);
    }
    
    // ========== KLAN GÜÇ HESAPLAMA ==========
    
    /**
     * Klan güç profili hesapla (thread-safe + batch processing)
     */
    public ClanPowerProfile calculateClanProfile(Clan clan) {
        if (clan == null) return new ClanPowerProfile();
        
        UUID clanId = clan.getId();
        long now = System.currentTimeMillis();
        
        // Atomic get (thread-safe)
        ClanPowerProfile cached = clanProfileCache.get(clanId);
        if (cached != null && now - cached.getLastUpdate() < CLAN_CACHE_DURATION) {
            return cached;
        }
        
        // Klan bazlı lock (race condition önleme)
        Object lock = clanLocks.computeIfAbsent(clanId, k -> new Object());
        synchronized (lock) {
            // Double-check
            cached = clanProfileCache.get(clanId);
            if (cached != null && now - cached.getLastUpdate() < CLAN_CACHE_DURATION) {
                return cached;
            }
            
            // Hesaplama (sadece bir thread)
            ClanPowerProfile profile = calculateClanProfileInternal(clan, now);
            
            // Cache'e kaydet
            clanProfileCache.put(clanId, profile);
            
            return profile;
        }
    }
    
    /**
     * Internal klan güç hesaplama (batch processing ile)
     */
    private ClanPowerProfile calculateClanProfileInternal(Clan clan, long now) {
        ClanPowerProfile profile = new ClanPowerProfile();
        
        // 1. Üye güçleri toplamı (batch processing)
        double memberPowerSum = 0.0;
        
        // Online üyeleri topla (optimizasyon: keySet'i bir kez al)
        Set<UUID> memberIds = clan.getMembers().keySet();
        List<Player> onlineMembers = new ArrayList<>();
        for (UUID memberId : memberIds) {
            Player member = getCachedPlayer(memberId);
            if (member != null && member.isOnline()) {
                onlineMembers.add(member);
            }
        }
        
        // Batch hesaplama (paralel stream - performans)
        if (!onlineMembers.isEmpty()) {
            memberPowerSum = onlineMembers.parallelStream()
                .mapToDouble(member -> {
                    PlayerPowerProfile memberProfile = calculatePlayerProfile(member);
                    return memberProfile.getTotalSGP();
                })
                .sum();
        }
        
        // Offline üyeler (cache'den) - aynı keySet'i kullan
        for (UUID memberId : memberIds) {
            if (getCachedPlayer(memberId) == null) {
                PlayerPowerProfile cachedProfile = offlinePlayerCache.get(memberId);
                if (cachedProfile != null) {
                    Long cacheTime = offlineCacheTime.get(memberId);
                    if (cacheTime != null && 
                        System.currentTimeMillis() - cacheTime < OFFLINE_CACHE_DURATION) {
                        memberPowerSum += cachedProfile.getTotalSGP();
                    }
                }
            }
        }
        
        profile.setMemberPowerSum(memberPowerSum);
        
        // 2. Yapı gücü
        profile.setStructurePower(calculateClanStructurePower(clan));
        
        // 3. Ritüel blok gücü (Delta sistemi - event-based)
        profile.setRitualBlockPower(calculateRitualBlockPower(clan));
        
        // 4. Ritüel kaynak gücü (sadece başarılı ritüeller için)
        profile.setRitualResourcePower(calculateRitualResourcePower(clan));
        
        // Toplam klan gücü
        double totalClanPower = memberPowerSum + 
                               profile.getStructurePower() + 
                               profile.getRitualBlockPower() + 
                               profile.getRitualResourcePower();
        
        profile.setTotalClanPower(totalClanPower);
        
        // Klan seviyesi hesapla
        profile.setClanLevel(powerConfig.calculateClanLevel(totalClanPower));
        profile.setLastUpdate(now);
        
        return profile;
    }
    
    /**
     * Cache'den player al (network overhead önleme)
     */
    private Player getCachedPlayer(UUID playerId) {
        return playerLookupCache.get(playerId);
    }
    
    /**
     * Klan yapı gücü hesapla
     */
    public double calculateClanStructurePower(Clan clan) {
        if (clan == null) return 0.0;
        
        double totalPower = 0.0;
        
        // Klan Kristali (sabit bonus) - Sadece kristal varsa
        if (clan.getCrystalEntity() != null && !clan.getCrystalEntity().isDead()) {
            totalPower += powerConfig.getCrystalBasePower();
        }
        
        // Yapılar
        for (Structure structure : clan.getStructures()) {
            int level = structure.getLevel();
            totalPower += powerConfig.getStructurePower(level);
        }
        
        return totalPower;
    }
    
    /**
     * Ritüel blok gücü hesapla (Delta sistemi - event-based)
     */
    public double calculateRitualBlockPower(Clan clan) {
        if (clan == null) return 0.0;
        
        UUID clanId = clan.getId();
        me.mami.stratocraft.model.ClanRitualBlockSnapshot snapshot = 
            ritualBlockSnapshots.get(clanId);
        
        if (snapshot == null) {
            // İlk kez oluştur
            snapshot = new me.mami.stratocraft.model.ClanRitualBlockSnapshot(clanId);
            ritualBlockSnapshots.put(clanId, snapshot);
            return 0.0; // Henüz blok yok
        }
        
        // Delta sisteminden güç hesapla
        return snapshot.calculateTotalPower(powerConfig);
    }
    
    /**
     * Ritüel blok koyulduğunda çağrılır (Delta sistemi)
     */
    public void onRitualBlockPlace(Clan clan, org.bukkit.Location loc, org.bukkit.Material material) {
        if (clan == null || loc == null || material == null) return;
        
        UUID clanId = clan.getId();
        me.mami.stratocraft.model.ClanRitualBlockSnapshot snapshot = 
            ritualBlockSnapshots.computeIfAbsent(clanId, 
                k -> new me.mami.stratocraft.model.ClanRitualBlockSnapshot(clanId));
        
        snapshot.onBlockPlace(loc, material);
        
        // Klan cache'ini temizle (güç değişti)
        clearClanCache(clanId);
    }
    
    /**
     * Ritüel blok kırıldığında çağrılır (Delta sistemi)
     */
    public void onRitualBlockBreak(Clan clan, org.bukkit.Location loc, org.bukkit.Material material) {
        if (clan == null || loc == null || material == null) return;
        
        UUID clanId = clan.getId();
        me.mami.stratocraft.model.ClanRitualBlockSnapshot snapshot = 
            ritualBlockSnapshots.get(clanId);
        
        if (snapshot == null) return; // Henüz snapshot yok
        
        snapshot.onBlockBreak(loc, material);
        
        // Klan cache'ini temizle (güç değişti)
        clearClanCache(clanId);
    }
    
    /**
     * Ritüel kaynak gücü hesapla (sadece başarılı ritüeller için)
     */
    public double calculateRitualResourcePower(Clan clan) {
        if (clan == null) return 0.0;
        
        UUID clanId = clan.getId();
        me.mami.stratocraft.model.ClanRitualResourceStats stats = 
            ritualResourceStats.get(clanId);
        
        if (stats == null) {
            // İlk kez oluştur
            stats = new me.mami.stratocraft.model.ClanRitualResourceStats(clanId);
            ritualResourceStats.put(clanId, stats);
            return 0.0; // Henüz ritüel yok
        }
        
        // İstatistiklerden güç hesapla
        return stats.calculateTotalPower(powerConfig);
    }
    
    /**
     * Ritüel başarıyla tamamlandığında çağrılır (ritüel kaynak gücü için)
     */
    public void onRitualSuccess(Clan clan, String ritualType, Map<String, Integer> usedResources) {
        if (clan == null || ritualType == null || usedResources == null || usedResources.isEmpty()) {
            return;
        }
        
        UUID clanId = clan.getId();
        me.mami.stratocraft.model.ClanRitualResourceStats stats = 
            ritualResourceStats.computeIfAbsent(clanId, 
                k -> new me.mami.stratocraft.model.ClanRitualResourceStats(clanId));
        
        // Sadece başarılı ritüeller için puan ver
        stats.onRitualSuccess(ritualType, usedResources);
        
        // Klan cache'ini temizle (güç değişti)
        clearClanCache(clanId);
    }
    
    /**
     * Ritüel başarısız olduğunda çağrılır (puan verme)
     */
    public void onRitualFailure(Clan clan, String ritualType) {
        if (clan == null || ritualType == null) return;
        
        UUID clanId = clan.getId();
        me.mami.stratocraft.model.ClanRitualResourceStats stats = 
            ritualResourceStats.get(clanId);
        
        if (stats != null) {
            // Başarısız ritüeller puan vermez
            stats.onRitualFailure(ritualType);
        }
    }
    
    /**
     * Ritüel blok mu? (config'den kontrol edilebilir)
     */
    public boolean isRitualBlock(org.bukkit.Material material) {
        if (material == null) return false;
        
        // Config'den kontrol (şimdilik hardcoded)
        return material == org.bukkit.Material.IRON_BLOCK ||
               material == org.bukkit.Material.OBSIDIAN ||
               material == org.bukkit.Material.DIAMOND_BLOCK ||
               material == org.bukkit.Material.GOLD_BLOCK ||
               material == org.bukkit.Material.EMERALD_BLOCK ||
               material == org.bukkit.Material.NETHERITE_BLOCK;
    }
    
    // ========== SEVİYE HESAPLAMA ==========
    
    /**
     * Oyuncu seviyesi hesapla (hibrit sistem)
     */
    public int calculatePlayerLevel(Player player) {
        if (player == null) return 1;
        
        PlayerPowerProfile profile = calculatePlayerProfile(player);
        return profile.getPlayerLevel();
    }
    
    /**
     * Klan seviyesi hesapla
     */
    public int calculateClanLevel(Clan clan) {
        if (clan == null) return 1;
        
        ClanPowerProfile profile = calculateClanProfile(clan);
        return profile.getClanLevel();
    }
    
    // ========== KORUMA SİSTEMİ ==========
    
    /**
     * Oyuncu saldırı yapabilir mi? (Tüm koruma kuralları)
     */
    public boolean canAttackPlayer(Player attacker, Player target) {
        if (attacker == null || target == null || attacker.equals(target)) {
            return false;
        }
        
        // Güçleri al (histerezis ile - exploit önleme)
        PlayerPowerProfile attackerProfile = calculatePlayerProfile(attacker);
        PlayerPowerProfile targetProfile = calculatePlayerProfile(target);
        
        // Histerezis: Etkili güç kullan (zırh çıkarma exploit önleme)
        double attackerEffectiveGear = attackerProfile.getEffectiveGearPower(
            powerConfig.getGearDecreaseDelay());
        double targetEffectiveGear = targetProfile.getEffectiveGearPower(
            powerConfig.getGearDecreaseDelay());
        
        // Etkili toplam güç (histerezis ile)
        double attackerCombatPower = attackerEffectiveGear + attackerProfile.getBuffPower();
        double targetCombatPower = targetEffectiveGear + targetProfile.getBuffPower();
        
        double attackerProgressionPower = attackerProfile.getTrainingPower() + 
                                         attackerProfile.getRitualPower();
        double targetProgressionPower = targetProfile.getTrainingPower() + 
                                       targetProfile.getRitualPower();
        
        double combatWeight = powerConfig.getCombatPowerWeight();
        double progressionWeight = powerConfig.getProgressionPowerWeight();
        
        double attackerPower = (attackerCombatPower * combatWeight) + 
                              (attackerProgressionPower * progressionWeight);
        double targetPower = (targetCombatPower * combatWeight) + 
                            (targetProgressionPower * progressionWeight);
        
        // 1. Klan savaşı kontrolü (en yüksek öncelik)
        Clan attackerClan = clanManager != null ? 
            clanManager.getClanByPlayer(attacker.getUniqueId()) : null;
        Clan targetClan = clanManager != null ? 
            clanManager.getClanByPlayer(target.getUniqueId()) : null;
        
        // Klan savaşı kontrolü (SiegeManager üzerinden)
        if (attackerClan != null && targetClan != null && siegeManager != null) {
            // SiegeManager'da aktif kuşatma var mı kontrol et
            if (isClanAtWar(attackerClan, targetClan)) {
                return true; // Savaşta herkes herkese saldırabilir
            }
        }
        
        // 2. Klan içi koruma
        if (attackerClan != null && attackerClan.equals(targetClan)) {
            double clanThreshold = attackerPower * powerConfig.getClanProtectionThreshold();
            if (targetPower < clanThreshold) {
                attacker.sendMessage("§cKlan içinde güçsüz üyelere saldıramazsın!");
                return false;
            }
        }
        
        // 3. Acemi koruması
        double rookieThreshold = powerConfig.getRookieThreshold();
        double strongPlayerThreshold = powerConfig.getStrongPlayerThreshold();
        
        if (targetPower < rookieThreshold && attackerPower > strongPlayerThreshold) {
            attacker.sendMessage("§cBu oyuncu çok güçsüz! Onurlu bir savaş değil.");
            return false;
        }
        
        // 4. Normal koruma (Onurlu Savaş Aralığı)
        double protectionThreshold = attackerPower * powerConfig.getProtectionThreshold();
        if (targetPower < protectionThreshold) {
            attacker.sendMessage("§cBu oyuncu senin dengin değil! Saldırı yapılamaz.");
            return false;
        }
        
        return true; // Tüm kontroller geçti
    }
    
    // ========== CACHE YÖNETİMİ ==========
    
    /**
     * Oyuncu cache'ini temizle
     */
    public void clearPlayerCache(UUID playerId) {
        playerProfileCache.remove(playerId);
        playerProfileCacheTime.remove(playerId);
        trainingDataCache.remove(playerId);
        trainingDataCacheTime.remove(playerId);
        buffPowerCache.remove(playerId);
        playerLocks.remove(playerId);
    }
    
    /**
     * Klan cache'ini temizle
     */
    public void clearClanCache(UUID clanId) {
        clanProfileCache.remove(clanId);
        clanLocks.remove(clanId);
    }
    
    /**
     * Klan dağıldığında tüm verileri temizle
     */
    public void onClanDisband(UUID clanId) {
        clearClanCache(clanId);
        ritualBlockSnapshots.remove(clanId);
        
        me.mami.stratocraft.model.ClanRitualResourceStats stats = ritualResourceStats.remove(clanId);
        if (stats != null) {
            stats.clear();
        }
    }
    
    /**
     * Tüm cache'i temizle
     */
    public void clearAllCache() {
        playerProfileCache.clear();
        playerProfileCacheTime.clear();
        clanProfileCache.clear();
        offlinePlayerCache.clear();
        offlineCacheTime.clear();
        trainingDataCache.clear();
        trainingDataCacheTime.clear();
        buffPowerCache.clear();
        playerLocks.clear();
        clanLocks.clear();
    }
    
    /**
     * Oyuncu çıkışında offline cache'e kaydet
     */
    public void onPlayerQuit(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Son güç profilini offline cache'e kaydet
        PlayerPowerProfile profile = playerProfileCache.get(playerId);
        if (profile != null) {
            offlinePlayerCache.put(playerId, profile);
            offlineCacheTime.put(playerId, System.currentTimeMillis());
        }
        
        // Online cache'i temizle
        clearPlayerCache(playerId);
        playerLookupCache.remove(playerId);
    }
    
    /**
     * Tüm cache'leri temizle (reload için)
     */
    public void clearAllCaches() {
        playerProfileCache.clear();
        playerProfileCacheTime.clear();
        clanProfileCache.clear();
        trainingDataCache.clear();
        offlinePlayerCache.clear();
        offlineCacheTime.clear();
        playerLookupCache.clear();
        ritualBlockSnapshots.clear();
        ritualResourceStats.clear();
        playerLocks.clear();
        clanLocks.clear();
        plugin.getLogger().info("StratocraftPowerSystem: Tüm cache'ler temizlendi.");
    }
    
    // ========== GETTERS ==========
    
    /**
     * Config getter
     */
    public ClanPowerConfig getConfig() {
        return powerConfig;
    }
    
    /**
     * Cache'den oyuncu gücü al (hızlı erişim)
     */
    public double getCachedPlayerPower(Player player) {
        if (player == null) return 0.0;
        
        PlayerPowerProfile profile = playerProfileCache.get(player.getUniqueId());
        return profile != null ? profile.getTotalSGP() : 0.0;
    }
    
    /**
     * Cache'den klan gücü al (hızlı erişim)
     */
    public double getCachedClanPower(Clan clan) {
        if (clan == null) return 0.0;
        
        ClanPowerProfile profile = clanProfileCache.get(clan.getId());
        return profile != null ? profile.getTotalClanPower() : 0.0;
    }
    
    /**
     * İki klan savaşta mı? (SiegeManager kontrolü)
     */
    private boolean isClanAtWar(Clan clan1, Clan clan2) {
        if (siegeManager == null) return false;
        
        // SiegeManager'da aktif kuşatma var mı kontrol et
        // clan1, clan2'ye saldırıyor mu veya tam tersi?
        try {
            // SiegeManager'ın isUnderSiege metodunu kullan
            // Şimdilik basit kontrol: Her iki klan da aktif kuşatmalarda mı?
            // Gelecekte SiegeManager'a özel metod eklenebilir
            return siegeManager.isUnderSiege(clan1) || siegeManager.isUnderSiege(clan2);
        } catch (Exception e) {
            plugin.getLogger().warning("Klan savaşı kontrolü hatası: " + e.getMessage());
            return false;
        }
    }
}

