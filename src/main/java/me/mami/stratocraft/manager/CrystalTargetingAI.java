package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * ✅ YENİ: Klan Kristali Hedef Seçim AI Sistemi
 * 
 * Görevler:
 * 1. Entity'lerin hedef seçimini yönet
 * 2. Öncelik sistemi: Kristal > Oyuncu > Normal
 * 3. Periyodik güncelleme
 */
public class CrystalTargetingAI {
    private final Main plugin;
    private BukkitTask updateTask;
    
    // Entity hedef takibi
    private final Map<UUID, TargetInfo> entityTargets = new HashMap<>();
    
    // Config değerleri
    private long updateInterval = 40L; // 2 saniye (varsayılan)
    private double crystalPriorityDistance = 50.0; // 50 blok
    private double playerPriorityDistance = 30.0; // 30 blok
    
    /**
     * Hedef bilgisi
     */
    public static class TargetInfo {
        public enum TargetType {
            CRYSTAL,    // Klan kristali
            PLAYER,     // Oyuncu
            NONE        // Hedef yok
        }
        
        public TargetType type;
        public Location targetLocation;
        public UUID targetId; // Clan ID veya Player UUID
        
        public TargetInfo(TargetType type, Location location, UUID id) {
            this.type = type;
            this.targetLocation = location;
            this.targetId = id;
        }
    }
    
    public CrystalTargetingAI(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Config'den ayarları yükle
     */
    private void loadConfig() {
        if (plugin.getConfigManager() == null) return;
        
        updateInterval = plugin.getConfigManager().getConfig().getLong(
            "crystal-damage-system.ai-update-interval", 40L);
        crystalPriorityDistance = plugin.getConfigManager().getConfig().getDouble(
            "crystal-damage-system.crystal-priority-distance", 50.0);
        playerPriorityDistance = plugin.getConfigManager().getConfig().getDouble(
            "crystal-damage-system.player-priority-distance", 30.0);
    }
    
    /**
     * AI sistemini başlat
     */
    public void start() {
        if (updateTask != null) return; // Zaten çalışıyor
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllEntityTargets();
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
    }
    
    /**
     * AI sistemini durdur
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        entityTargets.clear();
    }
    
    /**
     * Tüm entity'lerin hedeflerini güncelle
     */
    private void updateAllEntityTargets() {
        if (plugin.getClanManager() == null || plugin.getTerritoryManager() == null) {
            return;
        }
        
        // Tüm canlı entity'leri bul (bosslar, moblar, vb.)
        Collection<Entity> entities = getTargetableEntities();
        
        for (Entity entity : entities) {
            if (entity == null || !entity.isValid() || entity.isDead()) {
                entityTargets.remove(entity.getUniqueId());
                continue;
            }
            
            // Hedef seç
            TargetInfo target = selectTarget(entity);
            
            if (target != null && target.type != TargetInfo.TargetType.NONE) {
                entityTargets.put(entity.getUniqueId(), target);
                applyTarget(entity, target);
            } else {
                entityTargets.remove(entity.getUniqueId());
            }
        }
    }
    
    /**
     * Hedeflenebilir entity'leri bul
     */
    private Collection<Entity> getTargetableEntities() {
        Set<Entity> entities = new HashSet<>();
        
        // Tüm dünyalardaki canlı entity'leri bul
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity)) continue;
                if (entity instanceof Player) continue; // Oyuncuları atla
                
                // Boss veya özel entity kontrolü
                if (shouldTargetCrystal(entity)) {
                    entities.add(entity);
                }
            }
        }
        
        return entities;
    }
    
    /**
     * Entity kristale saldırmalı mı?
     */
    private boolean shouldTargetCrystal(Entity entity) {
        if (entity == null) return false;
        
        // Boss kontrolü
        if (isBoss(entity)) return true;
        
        // Felaket entity kontrolü
        if (entity.hasMetadata("disaster_entity")) return true;
        
        // Mini felaket mob kontrolü
        if (entity.hasMetadata("mini_disaster_mob") || 
            entity.hasMetadata("crystal_hunter")) return true;
        
        // Özel isim kontrolü
        if (entity instanceof LivingEntity) {
            String customName = ((LivingEntity) entity).getCustomName();
            if (customName != null && (
                customName.contains("BOSS") || 
                customName.contains("FELAKET") ||
                customName.contains("AVCI") ||
                customName.contains("YOK EDİCİ"))) {
                return true;
            }
        }
        
        // Normal moblar da kristale saldırabilir (config'den ayarlanabilir)
        // Şimdilik sadece özel entity'ler
        
        return false;
    }
    
    /**
     * Boss kontrolü
     */
    private boolean isBoss(Entity entity) {
        if (entity == null) return false;
        
        // Metadata kontrolü
        if (entity.hasMetadata("boss_type") || 
            entity.hasMetadata("disaster_entity")) {
            return true;
        }
        
        // ✅ YENİ: BossManager'dan kontrol et
        if (plugin != null && plugin.getBossManager() != null) {
            me.mami.stratocraft.manager.BossManager.BossData bossData = 
                plugin.getBossManager().getBossData(entity.getUniqueId());
            if (bossData != null) {
                return true; // BossManager'da kayıtlı bir boss
            }
        }
        
        // Özel isim kontrolü (güvenlik için)
        if (entity instanceof LivingEntity) {
            String customName = ((LivingEntity) entity).getCustomName();
            if (customName != null && (
                customName.contains("KRAL") || 
                customName.contains("KING") ||
                customName.contains("BOSS") || 
                customName.contains("FELAKET"))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Entity için hedef seç
     */
    private TargetInfo selectTarget(Entity entity) {
        if (entity == null) return null;
        
        Location entityLoc = entity.getLocation();
        
        // 1. ÖNCELİK: Klan Kristali (50 blok içinde)
        TargetInfo crystalTarget = findNearestCrystal(entityLoc);
        if (crystalTarget != null) {
            return crystalTarget;
        }
        
        // 2. İKİNCİ ÖNCELİK: Oyuncu (30 blok içinde)
        TargetInfo playerTarget = findNearestPlayer(entityLoc);
        if (playerTarget != null) {
            return playerTarget;
        }
        
        // 3. HEDEF YOK
        return new TargetInfo(TargetInfo.TargetType.NONE, null, null);
    }
    
    /**
     * En yakın klan kristalini bul
     */
    private TargetInfo findNearestCrystal(Location entityLoc) {
        if (plugin.getClanManager() == null) return null;
        
        Clan nearestClan = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Clan clan : plugin.getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            Location crystalLoc = clan.getCrystalLocation();
            if (crystalLoc == null || !crystalLoc.getWorld().equals(entityLoc.getWorld())) {
                continue;
            }
            
            double distance = entityLoc.distance(crystalLoc);
            if (distance <= crystalPriorityDistance && distance < nearestDistance) {
                nearestDistance = distance;
                nearestClan = clan;
            }
        }
        
        if (nearestClan != null) {
            return new TargetInfo(
                TargetInfo.TargetType.CRYSTAL,
                nearestClan.getCrystalLocation(),
                nearestClan.getId()
            );
        }
        
        return null;
    }
    
    /**
     * En yakın oyuncuyu bul
     */
    private TargetInfo findNearestPlayer(Location entityLoc) {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            if (!player.getWorld().equals(entityLoc.getWorld())) continue;
            
            double distance = entityLoc.distance(player.getLocation());
            if (distance <= playerPriorityDistance && distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }
        
        if (nearestPlayer != null) {
            return new TargetInfo(
                TargetInfo.TargetType.PLAYER,
                nearestPlayer.getLocation(),
                nearestPlayer.getUniqueId()
            );
        }
        
        return null;
    }
    
    /**
     * Entity'ye hedefi uygula
     */
    private void applyTarget(Entity entity, TargetInfo target) {
        if (entity == null || target == null || !(entity instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity living = (LivingEntity) entity;
        
        // LivingEntity'nin hedefini ayarla
        if (target.type == TargetInfo.TargetType.CRYSTAL) {
            // Kristal hedefi: Entity'yi kristale yönlendir
            // Not: EnderCrystal LivingEntity değil, bu yüzden manuel yönlendirme yapılmalı
            // Bu işlem handler'larda yapılacak (DisasterTask, vb.)
            
            // Metadata ile hedefi işaretle
            entity.setMetadata("crystal_target", new org.bukkit.metadata.FixedMetadataValue(
                plugin, target.targetId.toString()));
        } else if (target.type == TargetInfo.TargetType.PLAYER) {
            // Oyuncu hedefi: Normal AI kullan
            Player targetPlayer = org.bukkit.Bukkit.getPlayer(target.targetId);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                // LivingEntity'nin hedefini ayarla (eğer destekleniyorsa)
                try {
                    // NMS kullanmadan, sadece metadata ile işaretle
                    entity.setMetadata("player_target", new org.bukkit.metadata.FixedMetadataValue(
                        plugin, target.targetId.toString()));
                } catch (Exception e) {
                    // Hata durumunda devam et
                }
            }
        }
    }
    
    /**
     * Entity'nin hedefini al
     */
    public TargetInfo getEntityTarget(UUID entityId) {
        return entityTargets.get(entityId);
    }
    
    /**
     * Entity'yi sistemden kaldır (ölüm, vb.)
     */
    public void removeEntity(UUID entityId) {
        entityTargets.remove(entityId);
    }
}

