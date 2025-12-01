package me.mami.stratocraft.model;

import org.bukkit.entity.Entity;
import org.bukkit.Location;

/**
 * Gelişmiş Felaket Sistemi
 * 
 * Kategoriler:
 * - CREATURE: Canlı felaketler (boss gibi, merkeze ilerler)
 * - NATURAL: Doğa olayları (güneş patlaması, deprem)
 * 
 * Seviyeler:
 * - 1: Güçsüz, kısa sürer, her gün
 * - 2: Orta, 3 günde bir
 * - 3: Güçlü, 7 günde bir
 */
public class Disaster {
    public enum Type { 
        // Canlı Felaketler
        TITAN_GOLEM,        // Seviye 3 - Titan Golem
        ABYSSAL_WORM,       // Seviye 2 - Hiçlik Solucanı
        CHAOS_DRAGON,       // Seviye 3 - Khaos Ejderi
        VOID_TITAN,         // Seviye 3 - Boşluk Titanı
        
        // Doğa Olayları
        SOLAR_FLARE,        // Seviye 1 - Güneş Patlaması
        EARTHQUAKE,         // Seviye 2 - Deprem
        METEOR_SHOWER,      // Seviye 2 - Meteor Yağmuru
        VOLCANIC_ERUPTION   // Seviye 3 - Volkanik Patlama
    }
    
    public enum Category {
        CREATURE,   // Canlı felaketler
        NATURAL     // Doğa olayları
    }
    
    private final Type type;
    private final Category category;
    private final int level;        // 1-3 seviye
    private final Entity entity;    // Bossun Minecraft Entity'si (canlı felaketler için)
    private Location target;        // Merkez noktası (canlı felaketler için)
    private boolean isDead = false;
    
    // Dinamik güç
    private double maxHealth;       // Hesaplanmış maksimum can
    private double currentHealth;   // Mevcut can
    private double damageMultiplier; // Hasar çarpanı
    
    // Zamanlama
    private long startTime;         // Başlangıç zamanı
    private long duration;           // Süre (ms)
    
    public Disaster(Type type, Category category, int level, Entity entity, Location target, 
                   double maxHealth, double damageMultiplier, long duration) {
        this.type = type;
        this.category = category;
        this.level = level;
        this.entity = entity;
        this.target = target;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.damageMultiplier = damageMultiplier;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        
        // Entity'ye can ayarla
        if (entity != null && entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
            if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            }
            living.setHealth(maxHealth);
        }
    }
    
    public Type getType() { return type; }
    public Category getCategory() { return category; }
    public int getLevel() { return level; }
    public Entity getEntity() { return entity; }
    public Location getTarget() { return target; }
    public void setTarget(Location target) { this.target = target; }
    
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { 
        if (entity != null && entity instanceof org.bukkit.entity.LivingEntity) {
            return ((org.bukkit.entity.LivingEntity) entity).getHealth();
        }
        return currentHealth; 
    }
    public double getDamageMultiplier() { return damageMultiplier; }
    
    public long getStartTime() { return startTime; }
    public long getDuration() { return duration; }
    public long getRemainingTime() { 
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, duration - elapsed);
    }
    public boolean isExpired() { return getRemainingTime() <= 0; }
    
    public boolean isDead() { 
        if (isDead) return true;
        if (category == Category.CREATURE && entity != null) {
            return entity.isDead();
        }
        if (category == Category.NATURAL) {
            return isExpired();
        }
        return false;
    }
    
    public void kill() { 
        this.isDead = true; 
        if (entity != null) {
            entity.remove();
        }
    }
    
    /**
     * Tip'e göre kategori
     */
    public static Category getCategory(Type type) {
        switch (type) {
            case TITAN_GOLEM:
            case ABYSSAL_WORM:
            case CHAOS_DRAGON:
            case VOID_TITAN:
                return Category.CREATURE;
            case SOLAR_FLARE:
            case EARTHQUAKE:
            case METEOR_SHOWER:
            case VOLCANIC_ERUPTION:
                return Category.NATURAL;
            default:
                return Category.CREATURE;
        }
    }
    
    /**
     * Tip'e göre seviye
     */
    public static int getDefaultLevel(Type type) {
        switch (type) {
            case SOLAR_FLARE:
                return 1;
            case ABYSSAL_WORM:
            case EARTHQUAKE:
            case METEOR_SHOWER:
                return 2;
            case TITAN_GOLEM:
            case CHAOS_DRAGON:
            case VOID_TITAN:
            case VOLCANIC_ERUPTION:
                return 3;
            default:
                return 1;
        }
    }
    
    /**
     * Tip'e göre varsayılan süre (ms)
     */
    public static long getDefaultDuration(Type type, int level) {
        // Seviyeye göre süre
        long baseDuration;
        switch (level) {
            case 1: baseDuration = 600000L; break;  // 10 dakika
            case 2: baseDuration = 1200000L; break; // 20 dakika
            case 3: baseDuration = 1800000L; break; // 30 dakika
            default: baseDuration = 600000L;
        }
        
        // Tip'e göre ayarlama
        switch (type) {
            case SOLAR_FLARE:
                return baseDuration; // 10 dakika
            case EARTHQUAKE:
                return baseDuration / 2; // 5 dakika
            case METEOR_SHOWER:
                return baseDuration; // 20 dakika
            case VOLCANIC_ERUPTION:
                return baseDuration * 2; // 60 dakika
            default:
                return baseDuration;
        }
    }
}
