package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Kristal Saldırı Yardımcı Sınıfı
 * Tüm saldırı tipleri için ortak hasar hesaplama ve uygulama
 */
public class CrystalAttackHelper {
    
    /**
     * Saldırı tipi enum'u
     */
    public enum AttackType {
        DISASTER_BOSS,      // Felaket bossları (Kaos Ejderi, Titan, vb.)
        NORMAL_BOSS,        // Normal bosslar (Ork Şefi, Troll Kralı, vb.)
        SPECIAL_MOB,        // Özel moblar (Ork, İskelet Şövalye, vb.)
        WILD_CREEPER,       // Vahşi Creeper
        PLAYER              // Oyuncu saldırısı
    }
    
    /**
     * Saldırı sonucu
     */
    public static class AttackResult {
        private final boolean success;          // Saldırı başarılı mı?
        private final boolean blocked;          // Kalkan tarafından engellendi mi?
        private final double damageDealt;       // Verilen hasar
        private final double currentHealth;     // Kalan can
        private final double maxHealth;         // Maksimum can
        private final boolean destroyed;        // Kristal yok edildi mi?
        
        public AttackResult(boolean success, boolean blocked, double damageDealt, 
                          double currentHealth, double maxHealth, boolean destroyed) {
            this.success = success;
            this.blocked = blocked;
            this.damageDealt = damageDealt;
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
            this.destroyed = destroyed;
        }
        
        public boolean isSuccess() { return success; }
        public boolean isBlocked() { return blocked; }
        public double getDamageDealt() { return damageDealt; }
        public double getCurrentHealth() { return currentHealth; }
        public double getMaxHealth() { return maxHealth; }
        public boolean isDestroyed() { return destroyed; }
    }
    
    /**
     * Felaket boss saldırısı
     * @param disasterDamageMultiplier Felaket hasar çarpanı
     * @return Saldırı sonucu
     */
    public static AttackResult attackCrystalByDisaster(Clan targetClan, Location crystalLoc, 
                                                       double disasterDamageMultiplier, Main plugin) {
        if (targetClan == null || crystalLoc == null || plugin == null) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        // ✅ FELAKET BOSS HASARI: Base 10.0 * damageMultiplier
        // Örnek: damageMultiplier = 2.0 → 20.0 hasar
        double baseDamage = disasterDamageMultiplier * 10.0;
        
        return applyDamage(targetClan, crystalLoc, baseDamage, AttackType.DISASTER_BOSS, plugin);
    }
    
    /**
     * Normal boss saldırısı
     * @param bossLevel Boss seviyesi (1-5)
     * @return Saldırı sonucu
     */
    public static AttackResult attackCrystalByBoss(Clan targetClan, Location crystalLoc, 
                                                   int bossLevel, Main plugin) {
        if (targetClan == null || crystalLoc == null || plugin == null) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        // ✅ NORMAL BOSS HASARI: Seviye bazlı
        // Seviye 1: 5.0, Seviye 2: 8.0, Seviye 3: 12.0, Seviye 4: 18.0, Seviye 5: 25.0
        double baseDamage = calculateBossDamage(bossLevel);
        
        return applyDamage(targetClan, crystalLoc, baseDamage, AttackType.NORMAL_BOSS, plugin);
    }
    
    /**
     * Özel mob saldırısı
     * @param mobType Mob tipi (ork, skeleton_knight, vb.)
     * @return Saldırı sonucu
     */
    public static AttackResult attackCrystalBySpecialMob(Clan targetClan, Location crystalLoc, 
                                                         String mobType, Main plugin) {
        if (targetClan == null || crystalLoc == null || plugin == null) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        // ✅ ÖZEL MOB HASARI: Mob tipine göre
        // Ork: 3.0, İskelet Şövalye: 2.5, Troll: 4.0, Goblin: 1.5, vb.
        double baseDamage = calculateSpecialMobDamage(mobType);
        
        return applyDamage(targetClan, crystalLoc, baseDamage, AttackType.SPECIAL_MOB, plugin);
    }
    
    /**
     * Vahşi Creeper saldırısı (patlama)
     * @return Saldırı sonucu
     */
    public static AttackResult attackCrystalByWildCreeper(Clan targetClan, Location crystalLoc, 
                                                         Main plugin) {
        if (targetClan == null || crystalLoc == null || plugin == null) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        EnderCrystal crystal = targetClan.getCrystalEntity();
        if (crystal == null || crystal.isDead()) {
            return new AttackResult(false, false, 0, 0, 0, false);
        }
        
        // ✅ VAHŞİ CREEPER HASARI: Normal creeper patlaması 3 katı
        // Normal creeper: ~3.0 hasar → Vahşi: 9.0 hasar
        double baseDamage = 9.0;
        
        return applyDamage(targetClan, crystalLoc, baseDamage, AttackType.WILD_CREEPER, plugin);
    }
    
    /**
     * Hasar uygula (ortak metod)
     */
    private static AttackResult applyDamage(Clan targetClan, Location crystalLoc, double baseDamage, 
                                           AttackType attackType, Main plugin) {
        // Kalkan kontrolü
        me.mami.stratocraft.handler.structure.CrystalShieldHandler shieldHandler = 
            plugin.getCrystalShieldHandler();
        if (shieldHandler != null) {
            boolean blocked = shieldHandler.consumeShieldBlockOnDamage(targetClan);
            if (blocked) {
                // Kalkan efekti
                crystalLoc.getWorld().spawnParticle(
                    org.bukkit.Particle.BLOCK_CRACK,
                    crystalLoc,
                    20,
                    0.5, 0.5, 0.5, 0.1,
                    Material.BARRIER.createBlockData()
                );
                return new AttackResult(true, true, 0, 
                    targetClan.getCrystalCurrentHealth(), 
                    targetClan.getCrystalMaxHealth(), 
                    false);
            }
        }
        
        // Zırh kontrolü
        me.mami.stratocraft.handler.structure.CrystalArmorHandler armorHandler = 
            plugin.getCrystalArmorHandler();
        if (armorHandler != null) {
            armorHandler.consumeFuelOnDamage(targetClan, baseDamage);
        }
        
        // ✅ DÜZELTME: Kalkan kontrolünden sonra healthBefore al (kalkan varsa hasar yok)
        double healthBefore = targetClan.getCrystalCurrentHealth();
        
        // Hasar azaltma çarpanı
        double damageReduction = targetClan.getCrystalDamageReduction();
        double finalDamage = baseDamage * (1.0 - damageReduction);
        
        // Hasar uygula
        targetClan.damageCrystal(finalDamage);
        double healthAfter = targetClan.getCrystalCurrentHealth();
        double actualDamage = healthBefore - healthAfter;
        
        double currentHealth = targetClan.getCrystalCurrentHealth();
        double maxHealth = targetClan.getCrystalMaxHealth();
        double healthPercent = (currentHealth / maxHealth) * 100.0;
        
        // Partikül efekti (can yüzdesine göre)
        if (healthPercent > 50) {
            crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, crystalLoc, 10);
        } else if (healthPercent > 25) {
            crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, crystalLoc, 15);
        } else {
            crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, crystalLoc, 20);
        }
        
        // Klan üyelerine uyarı
        String attackerName = getAttackerName(attackType);
        for (UUID memberId : targetClan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c⚠ Kristal hasar aldı! [" + attackerName + "] " + 
                    String.format("%.1f", actualDamage) + " hasar - Can: " + 
                    String.format("%.1f", currentHealth) + "/" + 
                    String.format("%.1f", maxHealth) + " (" + 
                    String.format("%.1f", healthPercent) + "%)");
            }
        }
        
        // Kristal yok edildi mi?
        boolean destroyed = currentHealth <= 0;
        if (destroyed) {
            EnderCrystal crystal = targetClan.getCrystalEntity();
            if (crystal != null) {
                crystal.remove();
            }
            Bukkit.broadcastMessage(
                org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
                targetClan.getName() + " klanının kristali yok edildi! [" + attackerName + "]"
            );
        }
        
        return new AttackResult(true, false, actualDamage, currentHealth, maxHealth, destroyed);
    }
    
    /**
     * Boss hasarı hesapla
     */
    private static double calculateBossDamage(int bossLevel) {
        switch (bossLevel) {
            case 1: return 5.0;
            case 2: return 8.0;
            case 3: return 12.0;
            case 4: return 18.0;
            case 5: return 25.0;
            default: return 5.0;
        }
    }
    
    /**
     * Özel mob hasarı hesapla
     */
    private static double calculateSpecialMobDamage(String mobType) {
        if (mobType == null) return 2.0;
        
        switch (mobType.toLowerCase()) {
            case "ork":
            case "orc":
                return 3.0;
            case "skeleton_knight":
            case "knight":
                return 2.5;
            case "troll":
                return 4.0;
            case "goblin":
                return 1.5;
            case "werewolf":
            case "kurt_adam":
                return 2.0;
            case "dark_mage":
            case "mage":
                return 1.8;
            case "giant_spider":
            case "spider":
                return 2.2;
            default:
                return 2.0; // Varsayılan
        }
    }
    
    /**
     * Saldırgan ismi al
     */
    private static String getAttackerName(AttackType attackType) {
        switch (attackType) {
            case DISASTER_BOSS:
                return "Felaket Boss";
            case NORMAL_BOSS:
                return "Boss";
            case SPECIAL_MOB:
                return "Özel Mob";
            case WILD_CREEPER:
                return "Vahşi Creeper";
            case PLAYER:
                return "Oyuncu";
            default:
                return "Bilinmeyen";
        }
    }
}

