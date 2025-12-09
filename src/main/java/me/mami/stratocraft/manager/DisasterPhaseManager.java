package me.mami.stratocraft.manager;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterPhase;

/**
 * Felaket Faz Yönetim Sistemi
 * 
 * Faz geçişlerini yönetir, bildirimler gönderir ve efektler uygular
 */
public class DisasterPhaseManager {
    private final DisasterPhaseConfig phaseConfig;
    private final Main plugin;
    
    public DisasterPhaseManager(DisasterPhaseConfig phaseConfig, Main plugin) {
        this.phaseConfig = phaseConfig;
        this.plugin = plugin;
    }
    
    /**
     * Felaketin fazını kontrol et ve gerekirse geçiş yap
     * 
     * @param disaster Kontrol edilecek felaket
     * @return Faz değişti mi?
     */
    public boolean checkAndUpdatePhase(Disaster disaster) {
        if (disaster == null || disaster.isDead()) {
            return false;
        }
        
        // Faz güncelle
        DisasterPhase oldPhase = disaster.updatePhase();
        
        // Faz değişti mi?
        if (oldPhase != null) {
            DisasterPhase newPhase = disaster.getCurrentPhase();
            handlePhaseTransition(disaster, oldPhase, newPhase);
            return true;
        }
        
        return false;
    }
    
    /**
     * Faz geçişini işle (bildirimler, efektler, vb.)
     */
    private void handlePhaseTransition(Disaster disaster, DisasterPhase oldPhase, DisasterPhase newPhase) {
        if (phaseConfig == null) return;
        
        // Bildirim gönder
        if (phaseConfig.isPhaseTransitionMessages()) {
            sendPhaseTransitionMessage(disaster, oldPhase, newPhase);
        }
        
        // Efektler uygula
        if (phaseConfig.isPhaseTransitionEffects()) {
            applyPhaseTransitionEffects(disaster, oldPhase, newPhase);
        }
        
        // Faz özelliklerini uygula
        applyPhaseProperties(disaster, newPhase);
        
        plugin.getLogger().info("Felaket faz geçişi: " + oldPhase.getDisplayName() + 
                                " -> " + newPhase.getDisplayName());
    }
    
    /**
     * Faz geçiş bildirimi gönder
     */
    private void sendPhaseTransitionMessage(Disaster disaster, DisasterPhase oldPhase, DisasterPhase newPhase) {
        if (disaster == null || oldPhase == null || newPhase == null) {
            return;
        }
        
        String message = "§c[FELAKET] §e" + disaster.getType().name() + 
                        " §7faz değişti: §6" + oldPhase.getDisplayName() + 
                        " §7-> §c" + newPhase.getDisplayName();
        
        Bukkit.getServer().broadcastMessage(message);
        
        // Ses efekti (tüm oyunculara)
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null && player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Faz geçiş ses efekti uygulanırken hata: " + e.getMessage());
        }
    }
    
    /**
     * Faz geçiş efektleri uygula
     */
    private void applyPhaseTransitionEffects(Disaster disaster, DisasterPhase oldPhase, DisasterPhase newPhase) {
        if (disaster == null || newPhase == null) {
            return;
        }
        
        // Entity'ye efekt uygula (canlı felaketler için)
        if (disaster.getCategory() == Disaster.Category.CREATURE && disaster.getEntity() != null) {
            if (disaster.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity living = 
                    (org.bukkit.entity.LivingEntity) disaster.getEntity();
                
                // Faz geçişi partikül efekti (gelecekte eklenebilir)
                // Şimdilik sadece log
            }
        }
        
        // Oyunculara efekt uygula (RAGE ve DESPERATION fazlarında)
        if (newPhase == DisasterPhase.RAGE || newPhase == DisasterPhase.DESPERATION) {
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player == null || !player.isOnline()) continue;
                    
                    // Yakındaki oyunculara korku efekti
                    if (disaster.getEntity() != null) {
                        try {
                            double distance = player.getLocation().distance(disaster.getEntity().getLocation());
                            if (distance < 50) {
                                player.addPotionEffect(new PotionEffect(
                                    PotionEffectType.SLOW, 60, 0, false, false
                                ));
                            }
                        } catch (Exception e) {
                            // Mesafe hesaplama hatası (farklı dünyalar vb.)
                            plugin.getLogger().fine("Faz geçiş efekti uygulanırken mesafe hatası: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Faz geçiş efekti uygulanırken hata: " + e.getMessage());
            }
        }
    }
    
    /**
     * Faz özelliklerini uygula (hareket hızı, saldırı aralığı, vb.)
     */
    private void applyPhaseProperties(Disaster disaster, DisasterPhase phase) {
        if (disaster == null || phaseConfig == null) {
            return;
        }
        
        if (disaster.getCategory() != Disaster.Category.CREATURE) {
            return; // Sadece canlı felaketler için
        }
        
        if (disaster.getEntity() != null && 
            disaster.getEntity() instanceof org.bukkit.entity.LivingEntity) {
            
            org.bukkit.entity.LivingEntity living = 
                (org.bukkit.entity.LivingEntity) disaster.getEntity();
            
            // Hareket hızı çarpanı
            double speedMultiplier = phaseConfig.getSpeedMultiplier(phase);
            if (speedMultiplier > 0 && 
                living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                double baseSpeed = 0.25; // Varsayılan hareket hızı (config'den alınabilir)
                double newSpeed = baseSpeed * speedMultiplier;
                // Maksimum hız sınırı (çok hızlı olmasını önle)
                if (newSpeed > 1.0) newSpeed = 1.0;
                living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED)
                      .setBaseValue(newSpeed);
            }
        }
    }
    
    /**
     * Faz için saldırı aralığını al
     */
    public long getAttackInterval(Disaster disaster) {
        if (disaster == null) return 120000L; // Varsayılan
        
        DisasterPhase phase = disaster.getCurrentPhase();
        return phaseConfig.getAttackInterval(phase);
    }
    
    /**
     * Faz için aktif yetenek sayısını al
     */
    public int getActiveAbilityCount(Disaster disaster) {
        if (disaster == null) return 0;
        
        DisasterPhase phase = disaster.getCurrentPhase();
        return phaseConfig.getAbilityCount(phase);
    }
    
    /**
     * Faz için oyuncu saldırısı yapıyor mu?
     */
    public boolean shouldAttackPlayers(Disaster disaster) {
        if (disaster == null) return false;
        
        DisasterPhase phase = disaster.getCurrentPhase();
        return phaseConfig.shouldAttackPlayers(phase);
    }
}

