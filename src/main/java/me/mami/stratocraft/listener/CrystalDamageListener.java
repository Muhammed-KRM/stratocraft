package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.UUID;

/**
 * ✅ YENİ: Klan Kristali Hasar Sistemi
 * 
 * Görevler:
 * 1. EnderCrystal'ın normal patlamasını engelle
 * 2. Hasarı can sistemine yönlendir
 * 3. Klan üyesi kontrolü yap
 * 4. Entity hasar hesaplama
 */
public class CrystalDamageListener implements Listener {
    private final Main plugin;
    
    public CrystalDamageListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCrystalDamage(EntityDamageEvent event) {
        // Sadece EnderCrystal kontrolü
        if (!(event.getEntity() instanceof EnderCrystal)) {
            return;
        }
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        
        // Klan kristali mi kontrol et
        Clan ownerClan = findClanByCrystal(crystal);
        if (ownerClan == null) {
            return; // Klan kristali değil, normal işlem devam etsin
        }
        
        // ✅ ÖNEMLİ: Patlamayı engelle (event'i iptal et)
        event.setCancelled(true);
        
        // Hasar kaynağını bul
        Entity damager = null;
        DamageCause cause = event.getCause();
        
        // Direkt hasar veren entity (örn: oyuncu vuruşu)
        if (event instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent byEntityEvent = 
                (org.bukkit.event.entity.EntityDamageByEntityEvent) event;
            damager = byEntityEvent.getDamager();
        }
        
        // Hasar miktarını hesapla
        double baseDamage = event.getFinalDamage();
        double finalDamage = calculateDamage(baseDamage, damager, ownerClan);
        
        if (finalDamage <= 0) {
            return; // Hasar yok
        }
        
        // ✅ Can sistemine hasar ver
        ownerClan.damageCrystal(finalDamage);
        
        // Hasar kaynağına göre mesaj ve efekt
        handleDamageEffects(crystal, damager, ownerClan, finalDamage);
        
        // Kristal yok edildi mi kontrol et
        if (ownerClan.getCrystalCurrentHealth() <= 0) {
            handleCrystalDestroyed(ownerClan, damager);
        }
    }
    
    /**
     * Kristal entity'sinden klanı bul
     * ✅ DÜZELTME: TerritoryListener'daki mantık kullanıldı - PDC, metadata, location kontrolü eklendi
     */
    private Clan findClanByCrystal(EnderCrystal crystal) {
        if (plugin.getTerritoryManager() == null || plugin.getClanManager() == null) {
            return null;
        }
        
        Location crystalLoc = crystal.getLocation();
        if (crystalLoc == null) return null;
        
        // ✅ YENİ: 1. PDC kontrolü (CustomBlockData - kristal altındaki blok)
        org.bukkit.block.Block blockBelow = crystalLoc.clone().add(0, -1, 0).getBlock();
        UUID clanIdFromPDC = me.mami.stratocraft.util.CustomBlockData.getClanCrystalData(blockBelow);
        if (clanIdFromPDC != null) {
            Clan clan = plugin.getClanManager().getClan(clanIdFromPDC);
            if (clan != null) {
                plugin.getLogger().info("[CrystalDamageListener] PDC'den klan bulundu: " + clan.getName());
                // ✅ ÖNEMLİ: Entity referansını güncelle (sunucu restart sonrası)
                if (clan.getCrystalEntity() == null || !clan.getCrystalEntity().equals(crystal)) {
                    clan.setCrystalEntity(crystal);
                }
                return clan;
            }
        }
        
        // ✅ YENİ: 2. Metadata kontrolü
        me.mami.stratocraft.config.TerritoryConfig territoryConfig = plugin.getTerritoryConfig();
        if (territoryConfig != null) {
            String metadataKey = territoryConfig.getCrystalMetadataKey();
            if (crystal.hasMetadata(metadataKey)) {
                // Metadata var, tüm klanları kontrol et
                for (Clan clan : plugin.getClanManager().getAllClans()) {
                    if (clan == null || !clan.hasCrystal()) continue;
                    
                    Location clanCrystalLoc = clan.getCrystalLocation();
                    if (clanCrystalLoc != null &&
                        clanCrystalLoc.getBlockX() == crystalLoc.getBlockX() &&
                        clanCrystalLoc.getBlockY() == crystalLoc.getBlockY() &&
                        clanCrystalLoc.getBlockZ() == crystalLoc.getBlockZ()) {
                        plugin.getLogger().info("[CrystalDamageListener] Metadata + Location ile klan bulundu: " + clan.getName());
                        // ✅ ÖNEMLİ: Entity referansını güncelle
                        if (clan.getCrystalEntity() == null || !clan.getCrystalEntity().equals(crystal)) {
                            clan.setCrystalEntity(crystal);
                        }
                        return clan;
                    }
                }
            }
        }
        
        // ✅ YENİ: 3. Entity UUID kontrolü (tüm klanlar)
        for (Clan clan : plugin.getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            // UUID eşleşmesi
            if (clan.getCrystalEntity() != null && 
                clan.getCrystalEntity().getUniqueId().equals(crystal.getUniqueId())) {
                plugin.getLogger().info("[CrystalDamageListener] UUID eşleşmesi ile klan bulundu: " + clan.getName());
                return clan;
            }
            
            // ✅ YENİ: 4. Location eşleşmesi (entity referansı null olabilir)
            Location clanCrystalLoc = clan.getCrystalLocation();
            if (clanCrystalLoc != null &&
                clanCrystalLoc.getBlockX() == crystalLoc.getBlockX() &&
                clanCrystalLoc.getBlockY() == crystalLoc.getBlockY() &&
                clanCrystalLoc.getBlockZ() == crystalLoc.getBlockZ() &&
                clanCrystalLoc.getWorld().equals(crystalLoc.getWorld())) {
                plugin.getLogger().info("[CrystalDamageListener] Location eşleşmesi ile klan bulundu: " + clan.getName());
                // ✅ ÖNEMLİ: Entity referansını güncelle (sunucu restart sonrası)
                clan.setCrystalEntity(crystal);
                return clan;
            }
        }
        
        plugin.getLogger().info("[CrystalDamageListener] Klan bulunamadı - Normal end crystal olabilir");
        return null;
    }
    
    /**
     * Hasar miktarını hesapla (kaynağa göre)
     */
    private double calculateDamage(double baseDamage, Entity damager, Clan ownerClan) {
        if (plugin.getConfigManager() == null) {
            return baseDamage; // Config yoksa base damage kullan
        }
        
        double multiplier = 1.0;
        
        // Oyuncu hasarı
        if (damager instanceof Player) {
            Player player = (Player) damager;
            UUID playerId = player.getUniqueId();
            
            // Klan üyesi mi kontrol et
            if (ownerClan.getMembers().containsKey(playerId)) {
                // Klan üyesi hasarı (config'den)
                multiplier = plugin.getConfigManager().getConfig().getDouble(
                    "crystal-damage-system.clan-member-damage-multiplier", 0.5);
            } else {
                // Normal oyuncu hasarı
                multiplier = plugin.getConfigManager().getConfig().getDouble(
                    "crystal-damage-system.player-damage-multiplier", 1.0);
            }
        }
        // LivingEntity hasarı (boss, mob, vb.)
        else if (damager instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) damager;
            
            // Boss kontrolü (metadata veya özel etiket)
            if (isBoss(living)) {
                multiplier = plugin.getConfigManager().getConfig().getDouble(
                    "crystal-damage-system.boss-damage-multiplier", 2.0);
            }
            // Mini felaket mob kontrolü
            else if (isMiniDisasterMob(living)) {
                multiplier = plugin.getConfigManager().getConfig().getDouble(
                    "crystal-damage-system.mini-disaster-damage-multiplier", 1.5);
            }
            // Normal mob hasarı
            else {
                multiplier = plugin.getConfigManager().getConfig().getDouble(
                    "crystal-damage-system.mob-damage-multiplier", 0.5);
            }
        }
        // Diğer hasar kaynakları (patlama, yangın, vb.)
        else {
            // Config'den genel hasar çarpanı
            multiplier = plugin.getConfigManager().getConfig().getDouble(
                "crystal-damage-system.other-damage-multiplier", 0.3);
        }
        
        return baseDamage * multiplier;
    }
    
    /**
     * Boss kontrolü
     */
    private boolean isBoss(LivingEntity entity) {
        if (entity == null) return false;
        
        // Metadata kontrolü (BossManager'dan)
        if (entity.hasMetadata("boss_type") || entity.hasMetadata("disaster_entity")) {
            return true;
        }
        
        // Özel isim kontrolü
        String customName = entity.getCustomName();
        if (customName != null && (
            customName.contains("BOSS") || 
            customName.contains("FELAKET") ||
            customName.contains("TITAN") ||
            customName.contains("EJDER"))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Mini felaket mob kontrolü
     */
    private boolean isMiniDisasterMob(LivingEntity entity) {
        if (entity == null) return false;
        
        // Metadata kontrolü
        return entity.hasMetadata("mini_disaster_mob") || 
               entity.hasMetadata("crystal_hunter");
    }
    
    /**
     * Hasar efektleri (partikül, ses, mesaj)
     */
    private void handleDamageEffects(EnderCrystal crystal, Entity damager, 
                                     Clan ownerClan, double damage) {
        org.bukkit.Location crystalLoc = crystal.getLocation();
        double currentHealth = ownerClan.getCrystalCurrentHealth();
        double maxHealth = ownerClan.getCrystalMaxHealth();
        double healthPercent = (currentHealth / maxHealth) * 100.0;
        
        // Partikül efekti (can yüzdesine göre)
        if (healthPercent > 75) {
            crystalLoc.getWorld().spawnParticle(
                org.bukkit.Particle.VILLAGER_HAPPY, crystalLoc, 5, 0.5, 0.5, 0.5, 0.1);
        } else if (healthPercent > 50) {
            crystalLoc.getWorld().spawnParticle(
                org.bukkit.Particle.CRIT, crystalLoc, 10, 0.5, 0.5, 0.5, 0.1);
        } else if (healthPercent > 25) {
            crystalLoc.getWorld().spawnParticle(
                org.bukkit.Particle.DAMAGE_INDICATOR, crystalLoc, 15, 0.5, 0.5, 0.5, 0.1);
        } else {
            crystalLoc.getWorld().spawnParticle(
                org.bukkit.Particle.LAVA, crystalLoc, 20, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Ses efekti
        crystalLoc.getWorld().playSound(crystalLoc, 
            org.bukkit.Sound.ENTITY_ENDERMAN_HURT, 0.5f, 1.5f);
        
        // Klan üyelerine mesaj (sadece önemli hasarlarda)
        if (damage >= 10.0 || healthPercent < 50) {
            for (UUID memberId : ownerClan.getMembers().keySet()) {
                Player member = org.bukkit.Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    String damagerName = damager instanceof Player ? 
                        ((Player) damager).getName() : 
                        (damager != null ? damager.getType().name() : "Bilinmeyen");
                    
                    member.sendMessage("§c⚠ Kristal hasar aldı! " +
                        "Hasar: §f" + String.format("%.1f", damage) + " §c| " +
                        "Can: §f" + String.format("%.1f", currentHealth) + "/" + 
                        String.format("%.1f", maxHealth) + " §7(" + 
                        String.format("%.0f", healthPercent) + "%)");
                    
                    if (damager instanceof Player && !ownerClan.getMembers().containsKey(damager.getUniqueId())) {
                        member.sendMessage("§cSaldıran: §f" + damagerName);
                    }
                }
            }
        }
    }
    
    /**
     * Kristal yok edildiğinde
     */
    private void handleCrystalDestroyed(Clan ownerClan, Entity damager) {
        // Kristal entity'sini kaldır
        if (ownerClan.getCrystalEntity() != null) {
            ownerClan.getCrystalEntity().remove();
        }
        
        // Broadcast mesajı
        String damagerName = damager instanceof Player ? 
            ((Player) damager).getName() : 
            (damager != null ? damager.getType().name() : "Bilinmeyen");
        
        org.bukkit.Bukkit.broadcastMessage(
            org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
            ownerClan.getName() + " klanının kristali yok edildi!" +
            (damager != null ? " (Saldıran: " + damagerName + ")" : ""));
        
        // Klan üyelerine özel mesaj
        for (UUID memberId : ownerClan.getMembers().keySet()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendTitle(
                    "§c§lKRISTAL YOK EDİLDİ!",
                    "§7Klanınızın kristali yok edildi!",
                    10, 70, 20);
            }
        }
    }
}

