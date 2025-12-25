package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.TamingManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.MobPowerCalculator;
import me.mami.stratocraft.util.TrainingSuccessCalculator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

/**
 * ✅ YENİ: Eğitme Çekirdeği Aktivasyon Listener
 * 
 * Training Arena yapısı içine canavar çekilince eğitme çekirdeği aktif edilir.
 * Eğitme ihtimali canavarın gücüne göre hesaplanır.
 */
public class TrainingCoreListener implements Listener {
    
    private final Main plugin;
    private final TamingManager tamingManager;
    
    public TrainingCoreListener(Main plugin, TamingManager tamingManager) {
        this.plugin = plugin;
        this.tamingManager = tamingManager;
    }
    
    /**
     * Eğitme çekirdeği aktivasyonu
     * Training Arena yapısı içine canavar çekilince eğitme çekirdeğine sağ tık yapılır
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onTrainingCoreActivate(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        
        // Eğitme çekirdeği kontrolü (BEACON bloğu + TamingCore metadata)
        if (clickedBlock.getType() != Material.BEACON) {
            return;
        }
        
        if (!clickedBlock.hasMetadata("TamingCore")) {
            return;
        }
        
        Player player = event.getPlayer();
        Location coreLoc = clickedBlock.getLocation();
        
        // Training Arena yapısı kontrolü
        Structure trainingArena = findTrainingArenaAt(coreLoc);
        if (trainingArena == null) {
            player.sendMessage("§cBu eğitme çekirdeği bir Eğitim Alanı yapısına ait değil!");
            return;
        }
        
        // ✅ DÜZELTME: Null kontrolleri
        if (plugin.getClanManager() == null || plugin.getTerritoryManager() == null) {
            player.sendMessage("§cSistem hatası! Lütfen yöneticiye bildirin.");
            return;
        }
        
        // Klan kontrolü
        Clan playerClan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) {
            player.sendMessage("§cEğitim Alanı kullanmak için bir klana üye olmalısın!");
            return;
        }
        
        // Yapı sahibi kontrolü (yapı çekirdeği konumuna göre)
        Location structureCoreLoc = coreLoc.clone().subtract(0, 2, 0);
        Clan ownerClan = plugin.getTerritoryManager().getTerritoryOwner(structureCoreLoc);
        if (ownerClan == null || !ownerClan.equals(playerClan)) {
            player.sendMessage("§cBu Eğitim Alanı senin klanına ait değil!");
            return;
        }
        
        // Yapı içindeki canavarı bul (5 blok yarıçap)
        LivingEntity targetEntity = findCreatureInArena(coreLoc, 5.0);
        if (targetEntity == null) {
            player.sendMessage("§cEğitim Alanı içinde eğitilebilir bir canavar bulunamadı!");
            player.sendMessage("§7Canavarı yapı içine getir ve eğitme çekirdeğine sağ tık yap.");
            return;
        }
        
        // Canlı eğitilebilir mi?
        if (!tamingManager.canBeTamed(targetEntity)) {
            player.sendMessage("§cBu canlı eğitilemez!");
            if (tamingManager.isTamed(targetEntity)) {
                player.sendMessage("§7Bu canlı zaten eğitilmiş.");
            } else {
                player.sendMessage("§7Bu canlı özel isimli değil.");
            }
            return;
        }
        
        // ✅ YENİ: Yapı seviyesini canavarın seviyesine göre güncelle
        int mobLevel = MobPowerCalculator.getMobLevel(targetEntity, coreLoc);
        int mobPower = MobPowerCalculator.calculateMobPower(targetEntity, coreLoc);
        boolean isBoss = MobPowerCalculator.isBoss(targetEntity);
        
        // Yapı seviyesi canavarın seviyesine göre belirlenir (maksimum 5)
        int effectiveArenaLevel = Math.min(5, Math.max(1, mobLevel));
        
        // ✅ DÜZELTME: Yapı seviyesini güncelle (eğer değiştiyse)
        // Not: Bu değişiklik sadece bellekte kalır, veritabanına kaydedilmez
        // Çünkü yapı seviyesi dinamik olarak canavar seviyesine göre belirlenir
        int arenaLevel = effectiveArenaLevel;
        
        // Yapı seviyesi bilgisini göster (güncelleme mesajı sadece bilgilendirme amaçlı)
        if (trainingArena.getLevel() != effectiveArenaLevel) {
            player.sendMessage("§eEğitim Alanı seviyesi canavarın seviyesine göre ayarlandı: §6" + effectiveArenaLevel);
        }
        double successChance = TrainingSuccessCalculator.calculateSuccessChance(targetEntity, coreLoc, arenaLevel);
        String chanceStr = String.format("%.1f", successChance * 100) + "%";
        
        // Bilgi mesajı
        String mobName = targetEntity.getCustomName();
        if (mobName == null) {
            mobName = targetEntity.getType().name();
        }
        
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§e§lEĞİTME DENEMESİ");
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§7Canavar: §e" + mobName);
        player.sendMessage("§7Canavar Seviyesi: §e" + mobLevel);
        player.sendMessage("§7Canavar Gücü: §e" + mobPower);
        player.sendMessage("§7Yapı Seviyesi: §e" + arenaLevel);
        player.sendMessage("§7Tip: §e" + (isBoss ? "Boss" : "Normal Mob"));
        if (arenaLevel < mobLevel) {
            player.sendMessage("§c⚠ Yapı seviyesi canavar seviyesinden düşük! İhtimal azalır.");
        } else if (arenaLevel > mobLevel) {
            player.sendMessage("§a✓ Yapı seviyesi canavar seviyesinden yüksek! İhtimal artar.");
        }
        player.sendMessage("§7Başarı İhtimali: §a" + chanceStr);
        player.sendMessage("§6═══════════════════════════════════");
        
        // Eğitme denemesi (yapı seviyesi ile)
        boolean success = Math.random() < successChance;
        
        if (success) {
            // Başarılı eğitme
            // ✅ DÜZELTME: Null kontrolü
            if (plugin.getDifficultyManager() == null) {
                player.sendMessage("§cSistem hatası! DifficultyManager bulunamadı.");
                return;
            }
            
            int difficultyLevel = plugin.getDifficultyManager().getDifficultyLevel(coreLoc);
            if (tamingManager.tameCreature(targetEntity, player.getUniqueId(), difficultyLevel)) {
                // Başarı mesajı ve efektler
                player.sendMessage("§a§l✓ EĞİTME BAŞARILI!");
                player.sendMessage("§7" + mobName + " artık senin!");
                
                Location entityLoc = targetEntity.getLocation();
                // ✅ DÜZELTME: Null kontrolü
                if (entityLoc != null && entityLoc.getWorld() != null) {
                    entityLoc.getWorld().spawnParticle(Particle.HEART, entityLoc, 50, 1.5, 1.5, 1.5, 0.2);
                    entityLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, entityLoc, 30, 1, 1, 1, 0.1);
                    entityLoc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, entityLoc, 20, 1, 1, 1, 0.3);
                    entityLoc.getWorld().playSound(entityLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
                    entityLoc.getWorld().playSound(entityLoc, Sound.ENTITY_WOLF_WHINE, 1.0f, 1.0f);
                }
                
                // Eğitme çekirdeği efektleri
                if (coreLoc.getWorld() != null) {
                    coreLoc.getWorld().spawnParticle(Particle.TOTEM, coreLoc.clone().add(0.5, 1, 0.5), 30, 0.5, 0.5, 0.5, 0.1);
                    coreLoc.getWorld().playSound(coreLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                }
            } else {
                player.sendMessage("§cEğitme başarısız oldu! (Sistem hatası)");
            }
        } else {
            // Başarısız eğitme
            player.sendMessage("§c§l✗ EĞİTME BAŞARISIZ!");
            player.sendMessage("§7Canavar eğitilemedi. Tekrar deneyebilirsin.");
            
            Location entityLoc = targetEntity.getLocation();
            // ✅ DÜZELTME: Null kontrolü
            if (entityLoc != null && entityLoc.getWorld() != null) {
                entityLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, entityLoc, 20, 1, 1, 1, 0.1);
                entityLoc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, entityLoc, 10, 0.5, 0.5, 0.5, 0.1);
                entityLoc.getWorld().playSound(entityLoc, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            
            // Eğitme çekirdeği efektleri
            if (coreLoc.getWorld() != null) {
                coreLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, coreLoc.clone().add(0.5, 1, 0.5), 20, 0.3, 0.3, 0.3, 0.05);
                coreLoc.getWorld().playSound(coreLoc, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
            }
        }
        
        event.setCancelled(true);
    }
    
    /**
     * Belirli bir konumdaki Training Arena yapısını bul
     * ✅ DÜZELTME: Eğitme çekirdeği yapı çekirdeğinden 2 blok yukarıda olduğu için
     * yapı çekirdeği konumunu hesaplayarak kontrol ediyoruz
     */
    private Structure findTrainingArenaAt(Location coreLoc) {
        if (plugin == null || plugin.getClanManager() == null || coreLoc == null) {
            return null;
        }
        
        // Eğitme çekirdeği Enchanting Table'ın üstünde, yapı çekirdeği ise Enchanting Table'ın altında
        // Yapı çekirdeği konumunu hesapla (eğitme çekirdeğinden 2 blok aşağı)
        Location structureCoreLoc = coreLoc.clone().subtract(0, 2, 0);
        
        // ✅ DÜZELTME: Thread-safe liste kopyası
        java.util.List<Clan> allClans;
        try {
            allClans = new java.util.ArrayList<>(plugin.getClanManager().getAllClans());
        } catch (Exception e) {
            plugin.getLogger().warning("TrainingCoreListener: getAllClans() hatası: " + e.getMessage());
            return null;
        }
        
        // Tüm klanların yapılarını kontrol et
        for (Clan clan : allClans) {
            if (clan == null) continue;
            
            // ✅ DÜZELTME: Thread-safe yapı listesi kopyası
            java.util.List<Structure> structures;
            try {
                structures = new java.util.ArrayList<>(clan.getStructures());
            } catch (Exception e) {
                continue; // Bu klanı atla
            }
            
            for (Structure structure : structures) {
                if (structure == null) continue;
                
                if (structure.getType() == Structure.Type.TRAINING_ARENA) {
                    Location structLoc = structure.getLocation();
                    if (structLoc != null && structLoc.getWorld().equals(structureCoreLoc.getWorld())) {
                        // Yapı çekirdeği konumuna yakın mı? (1 blok tolerans - tam konum kontrolü)
                        double distance = structLoc.distance(structureCoreLoc);
                        if (distance <= 1.0) {
                            return structure;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Eğitim Alanı içindeki eğitilebilir canavarı bul
     */
    private LivingEntity findCreatureInArena(Location center, double radius) {
        if (center == null || center.getWorld() == null) {
            return null;
        }
        
        for (org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            
            if (entity instanceof Player) {
                continue; // Oyuncuları atla
            }
            
            LivingEntity living = (LivingEntity) entity;
            
            // Eğitilebilir mi?
            if (tamingManager.canBeTamed(living)) {
                return living;
            }
        }
        
        return null;
    }
}

