package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Klan Güç Hesaplama Sistemi
 * 
 * Bu sistem oyuncu ve klan güç puanlarını hesaplar:
 * - Oyuncu gücü: Itemler, ritüel blokları/kaynakları, antrenman
 * - Klan gücü: Klan yapıları
 * - Güç seviyeleri: Algoritma ile otomatik belirlenir
 * - Koruma sistemi: Güçsüz oyunculara saldırı engelleme
 */
public class ClanPowerSystem {
    private final Main plugin;
    private final ClanManager clanManager;
    private final TrainingManager trainingManager;
    private final SpecialItemManager specialItemManager;
    
    // Config değerleri
    private ClanPowerConfig powerConfig;
    
    // Cache (performans için)
    private final Map<UUID, Double> playerPowerCache = new HashMap<>();
    private final Map<UUID, Long> playerPowerCacheTime = new HashMap<>();
    private final Map<UUID, Integer> playerLevelCache = new HashMap<>();
    private final Map<UUID, Double> clanPowerCache = new HashMap<>();
    private final Map<UUID, Integer> clanLevelCache = new HashMap<>();
    private static final long CACHE_DURATION = 5000L; // 5 saniye
    
    public ClanPowerSystem(Main plugin, ClanManager clanManager, 
                          TrainingManager trainingManager, 
                          SpecialItemManager specialItemManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.trainingManager = trainingManager;
        this.specialItemManager = specialItemManager;
        this.powerConfig = new ClanPowerConfig();
    }
    
    /**
     * Config'den ayarları yükle
     */
    public void loadConfig(FileConfiguration config) {
        powerConfig.loadFromConfig(config);
    }
    
    /**
     * Oyuncunun toplam güç puanını hesapla (cache ile)
     */
    public double calculatePlayerPower(Player player) {
        if (player == null || !player.isOnline()) return 0.0;
        
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        if (playerPowerCache.containsKey(playerId)) {
            Long cacheTime = playerPowerCacheTime.get(playerId);
            if (cacheTime != null && now - cacheTime < CACHE_DURATION) {
                return playerPowerCache.get(playerId);
            }
        }
        
        // Güç hesapla
        double totalPower = 0.0;
        
        // 1. Item gücü (silah + zırh)
        totalPower += calculateItemPower(player);
        
        // 2. Ritüel blokları gücü (ritüellerde kullanılan bloklar)
        totalPower += calculateRitualBlockPower(player);
        
        // 3. Ritüel kaynakları gücü (ritüelleri aktif eden kaynaklar)
        totalPower += calculateRitualResourcePower(player);
        
        // 4. Antrenman/Ustalık gücü
        totalPower += calculateTrainingMasteryPower(player);
        
        // Cache'e kaydet
        playerPowerCache.put(playerId, totalPower);
        playerPowerCacheTime.put(playerId, now);
        
        return totalPower;
    }
    
    /**
     * Item gücü (silah + zırh seviyeleri)
     */
    private double calculateItemPower(Player player) {
        double totalPower = 0.0;
        
        // Silah gücü
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon != null) {
            int level = ItemManager.getWeaponLevel(weapon);
            if (level > 0) {
                totalPower += powerConfig.getWeaponPower(level);
            }
        }
        
        // Zırh gücü
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece != null) {
                int level = ItemManager.getArmorLevel(piece);
                if (level > 0) {
                    totalPower += powerConfig.getArmorPower(level);
                }
            }
        }
        
        return totalPower;
    }
    
    /**
     * Ritüel blokları gücü (ritüellerde kullanılan bloklar)
     * NOT: Bu sistem henüz tam implement edilmemiş, şimdilik placeholder
     */
    private double calculateRitualBlockPower(Player player) {
        // TODO: Ritüel bloklarını takip eden bir sistem gerekli
        // Şimdilik 0 döndürüyoruz, gelecekte implement edilecek
        return 0.0;
    }
    
    /**
     * Ritüel kaynakları gücü (ritüelleri aktif eden kaynaklar)
     * NOT: Bu sistem henüz tam implement edilmemiş, şimdilik placeholder
     */
    private double calculateRitualResourcePower(Player player) {
        // TODO: Ritüel kaynaklarını takip eden bir sistem gerekli
        // Şimdilik 0 döndürüyoruz, gelecekte implement edilecek
        return 0.0;
    }
    
    /**
     * Antrenman/Ustalık gücü
     * Her %100 üzerine çıkış için puan
     */
    private double calculateTrainingMasteryPower(Player player) {
        if (trainingManager == null) return 0.0;
        
        UUID playerId = player.getUniqueId();
        Map<String, Integer> playerTraining = trainingManager.getAllTrainingData()
            .getOrDefault(playerId, new HashMap<>());
        
        if (playerTraining.isEmpty()) return 0.0;
        
        double totalPower = 0.0;
        
        for (String ritualId : playerTraining.keySet()) {
            int totalUses = trainingManager.getTotalUses(playerId, ritualId);
            
            // %100 üzerine çıkışları hesapla
            // Örnek: 150 kullanım = %150 = 1.5x = 300 puan, 200 kullanım = %200 = 2.0x = 750 puan
            if (totalUses > 100) {
                // %100'den fazla kullanım için bonus puan
                double masteryPercent = (totalUses / 100.0) * 100; // Yüzde
                totalPower += powerConfig.getMasteryPower(masteryPercent);
            }
        }
        
        return totalPower;
    }
    
    /**
     * Klanın toplam güç puanını hesapla (cache ile)
     */
    public double calculateClanPower(Clan clan) {
        if (clan == null) return 0.0;
        
        UUID clanId = clan.getId();
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        if (clanPowerCache.containsKey(clanId)) {
            // Cache süresi kontrolü (clan için daha uzun süre)
            // Şimdilik basit cache, gelecekte zaman kontrolü eklenebilir
        }
        
        double totalPower = 0.0;
        
        // Klan yapıları gücü
        for (Structure structure : clan.getStructures()) {
            int level = structure.getLevel();
            totalPower += powerConfig.getStructurePower(level);
        }
        
        // Cache'e kaydet
        clanPowerCache.put(clanId, totalPower);
        
        return totalPower;
    }
    
    /**
     * Oyuncu güç seviyesini hesapla (algoritma ile)
     */
    public int calculatePlayerLevel(Player player) {
        if (player == null) return 0;
        
        UUID playerId = player.getUniqueId();
        
        // Cache kontrolü
        if (playerLevelCache.containsKey(playerId)) {
            return playerLevelCache.get(playerId);
        }
        
        double power = calculatePlayerPower(player);
        int level = powerConfig.calculateLevel(power);
        
        // Cache'e kaydet
        playerLevelCache.put(playerId, level);
        
        return level;
    }
    
    /**
     * Klan güç seviyesini hesapla (algoritma ile)
     */
    public int calculateClanLevel(Clan clan) {
        if (clan == null) return 0;
        
        UUID clanId = clan.getId();
        
        // Cache kontrolü
        if (clanLevelCache.containsKey(clanId)) {
            return clanLevelCache.get(clanId);
        }
        
        double power = calculateClanPower(clan);
        int level = powerConfig.calculateClanLevel(power);
        
        // Cache'e kaydet
        clanLevelCache.put(clanId, level);
        
        return level;
    }
    
    /**
     * Oyuncu saldırı yapabilir mi? (koruma sistemi)
     */
    public boolean canAttackPlayer(Player attacker, Player target) {
        if (attacker == null || target == null) return false;
        
        double attackerPower = calculatePlayerPower(attacker);
        double targetPower = calculatePlayerPower(target);
        
        // Koruma eşiği: Hedef, saldıranın gücünün belirli bir yüzdesinden düşükse saldırı yapılamaz
        double protectionThreshold = attackerPower * powerConfig.getProtectionThreshold();
        
        if (targetPower < protectionThreshold) {
            return false; // Hedef çok güçsüz, saldırı yapılamaz
        }
        
        return true;
    }
    
    /**
     * Klan içi saldırı yapabilir mi? (koruma sistemi)
     */
    public boolean canAttackClanMember(Player attacker, Player target) {
        if (attacker == null || target == null) return false;
        
        Clan attackerClan = clanManager.getClanByPlayer(attacker.getUniqueId());
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        
        // Aynı klan içinde mi?
        if (attackerClan != null && attackerClan.equals(targetClan)) {
            double attackerPower = calculatePlayerPower(attacker);
            double targetPower = calculatePlayerPower(target);
            
            double protectionThreshold = attackerPower * powerConfig.getClanProtectionThreshold();
            
            if (targetPower < protectionThreshold) {
                return false; // Klan içinde güçsüz üyeye saldırı yapılamaz
            }
        }
        
        return true;
    }
    
    /**
     * Cache'i temizle (oyuncu giriş/çıkışında)
     */
    public void clearPlayerCache(UUID playerId) {
        playerPowerCache.remove(playerId);
        playerPowerCacheTime.remove(playerId);
        playerLevelCache.remove(playerId);
    }
    
    /**
     * Tüm cache'i temizle
     */
    public void clearAllCache() {
        playerPowerCache.clear();
        playerPowerCacheTime.clear();
        playerLevelCache.clear();
        clanPowerCache.clear();
        clanLevelCache.clear();
    }
    
    /**
     * Config getter
     */
    public ClanPowerConfig getConfig() {
        return powerConfig;
    }
}

