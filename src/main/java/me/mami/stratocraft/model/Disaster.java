package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Gelişmiş Felaket Sistemi
 * 
 * Kategoriler:
 * - CREATURE: Canlı felaketler (felaket bossları, normal bosslardan ayrı, çok daha güçlü)
 * - NATURAL: Doğa olayları (güneş patlaması, deprem)
 * - MINI: Mini felaketler
 * 
 * Seviye Sistemi (İki Katmanlı):
 * 
 * 1. KATEGORİ SEVİYELERİ (Otomatik spawn sıklığı):
 *    - Seviye 1: Her gün gelen felaketler (SOLAR_FLARE, CREEPER_SWARM, vb.)
 *    - Seviye 2: 3 günde bir gelen felaketler (CATASTROPHIC_ABYSSAL_WORM, EARTHQUAKE, vb.)
 *    - Seviye 3: 7 günde bir gelen felaketler (CATASTROPHIC_TITAN, CATASTROPHIC_CHAOS_DRAGON, vb.)
 *    - Özel Event: Admin tarafından manuel başlatılan özel felaketler
 * 
 * 2. İÇ SEVİYELER (Admin komutunda belirtilen, felaketin gücünü belirler):
 *    - Seviye 1: Zayıf form (düşük can/hasar)
 *    - Seviye 2: Orta form (orta can/hasar)
 *    - Seviye 3: Güçlü form (yüksek can/hasar)
 * 
 * Örnek: CATASTROPHIC_TITAN kategori seviyesi 3 (7 günde bir), ama admin komutunda
 *        iç seviye 1-3 arası belirtilebilir (güçlü/güçsüz form).
 */
public class Disaster {
    public enum Type { 
        // Canlı Felaketler - Tek Boss (Felaket Bossları - Normal bosslardan ayrı)
        CATASTROPHIC_TITAN,        // Seviye 3 - Felaket Titanı (30 blok boyunda dev golem)
        CATASTROPHIC_ABYSSAL_WORM, // Seviye 2 - Felaket Hiçlik Solucanı
        CATASTROPHIC_CHAOS_DRAGON, // Seviye 3 - Felaket Khaos Ejderi
        CATASTROPHIC_VOID_TITAN,   // Seviye 3 - Felaket Boşluk Titanı
        CATASTROPHIC_ICE_LEVIATHAN,// Seviye 2 - Felaket Buzul Leviathan
        
        // Canlı Felaketler - Grup (30 adet)
        ZOMBIE_HORDE,       // Seviye 2 - 30 Orta Güçte Zombi
        SKELETON_LEGION,    // Seviye 2 - 30 Orta Güçte İskelet
        SPIDER_SWARM,       // Seviye 2 - 30 Orta Güçte Örümcek
        
        // Canlı Felaketler - Mini Dalga (100-500 adet)
        CREEPER_SWARM,      // Seviye 1 - 100-500 Mini Creeper
        ZOMBIE_WAVE,        // Seviye 1 - 100-500 Mini Zombi
        
        // Doğa Olayları
        SOLAR_FLARE,        // Seviye 1 - Güneş Patlaması
        EARTHQUAKE,         // Seviye 2 - Deprem
        STORM,              // Seviye 2 - Fırtına
        METEOR_SHOWER,      // Seviye 2 - Meteor Yağmuru
        VOLCANIC_ERUPTION,  // Seviye 3 - Volkanik Patlama
        
        // Mini Felaketler
        BOSS_BUFF_WAVE,     // Mini - Boss güçlenme dalgası
        MOB_INVASION,       // Mini - Mob istilası
        PLAYER_BUFF_WAVE    // Mini - Oyuncu buff dalgası
    }
    
    public enum Category {
        CREATURE,   // Canlı felaketler
        NATURAL,    // Doğa olayları
        MINI        // Mini felaketler
    }
    
    /**
     * Canavar felaket tipi (tek boss, grup, mini dalga)
     */
    public enum CreatureDisasterType {
        SINGLE_BOSS,    // Tek boss
        MEDIUM_GROUP,   // 30 orta güçte
        MINI_SWARM      // 100-500 mini
    }
    
    private final Type type;
    private final Category category;
    private final int level;        // İç seviye (1-3): Admin komutunda belirtilen, felaketin gücünü belirler
    private final Entity entity;    // Bossun Minecraft Entity'si (canlı felaketler için)
    private Location target;        // Merkez noktası (canlı felaketler için)
    private Location targetCrystalLocation; // Klan kristali hedefi (canlı felaketler için)
    private CreatureDisasterType creatureDisasterType; // Canavar felaket tipi
    private java.util.List<Entity> groupEntities; // Grup felaketler için entity listesi
    private boolean isDead = false;
    
    // Dinamik güç
    private double maxHealth;       // Hesaplanmış maksimum can
    private double currentHealth;   // Mevcut can
    private double damageMultiplier; // Hasar çarpanı
    
    // Zamanlama
    private long startTime;         // Başlangıç zamanı
    private long duration;           // Süre (ms)
    
    // Faz sistemi
    private DisasterPhase currentPhase; // Mevcut faz
    private long lastPhaseTransitionTime; // Son faz geçiş zamanı
    
    public Disaster(Type type, Category category, int level, Entity entity, Location target, 
                   double maxHealth, double damageMultiplier, long duration) {
        this.type = type;
        this.category = category;
        this.level = level;
        this.entity = entity;
        this.target = target;
        this.targetCrystalLocation = null;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.damageMultiplier = damageMultiplier;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.groupEntities = new java.util.ArrayList<>();
        
        // Faz sistemi: Başlangıçta EXPLORATION fazında
        this.currentPhase = DisasterPhase.EXPLORATION;
        this.lastPhaseTransitionTime = System.currentTimeMillis();
        
        // Canavar felaket tipini belirle
        if (category == Category.CREATURE) {
            this.creatureDisasterType = getCreatureDisasterType(type);
        } else {
            this.creatureDisasterType = null;
        }
        
        // Entity'ye can ayarla (Minecraft limiti: 2048)
        if (entity != null && entity instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
            if (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                // Minecraft'ın maksimum can değeri 2048
                if (maxHealth > 2048.0) {
                    // Base değeri 2048'e ayarla
                    living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048.0);
                    
                    // Kalan canı attribute modifier ile ekle
                    double extraHealth = maxHealth - 2048.0;
                    org.bukkit.attribute.AttributeModifier healthMod = new org.bukkit.attribute.AttributeModifier(
                        java.util.UUID.randomUUID(),
                        "disaster_extra_health",
                        extraHealth,
                        org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER
                    );
                    
                    // Eski modifier'ı kaldır (varsa)
                    living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)
                        .getModifiers().stream()
                        .filter(m -> m.getName().equals("disaster_extra_health"))
                        .forEach(m -> living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).removeModifier(m));
                    
                    // Yeni modifier'ı ekle
                    living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).addModifier(healthMod);
                    
                    // Attribute modifier eklendikten sonra gerçek maksimum canı al
                    org.bukkit.attribute.AttributeInstance healthAttr = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                    double actualMaxHealth = healthAttr != null ? healthAttr.getValue() : maxHealth;
                    
                    // Canı ayarla (gerçek maksimum can değerini kullan)
                    living.setHealth(actualMaxHealth);
                } else {
                    // 2048'in altındaysa normal şekilde ayarla
                    living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                    living.setHealth(maxHealth);
                }
            }
        }
    }
    
    public Type getType() { return type; }
    public Category getCategory() { return category; }
    public int getLevel() { return level; }
    public Entity getEntity() { return entity; }
    public Location getTarget() { return target; }
    public void setTarget(Location target) { this.target = target; }
    public Location getTargetCrystal() { return targetCrystalLocation; }
    public void setTargetCrystal(Location crystalLoc) { this.targetCrystalLocation = crystalLoc; }
    public CreatureDisasterType getCreatureDisasterType() { return creatureDisasterType; }
    public java.util.List<Entity> getGroupEntities() { return groupEntities; }
    public void addGroupEntity(Entity entity) { this.groupEntities.add(entity); }
    
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
    
    // Faz sistemi getters/setters
    public DisasterPhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(DisasterPhase phase) { 
        this.currentPhase = phase;
        this.lastPhaseTransitionTime = System.currentTimeMillis();
    }
    public long getLastPhaseTransitionTime() { return lastPhaseTransitionTime; }
    
    /**
     * Can yüzdesine göre mevcut fazı hesapla ve güncelle
     * 
     * @return Eski faz (faz değiştiyse), null (faz değişmediyse)
     */
    public DisasterPhase updatePhase() {
        // maxHealth kontrolü (sıfıra bölme hatası önleme)
        if (maxHealth <= 0) {
            return null;
        }
        
        double currentHealth = getCurrentHealth();
        double healthPercent = currentHealth / maxHealth;
        
        // Can yüzdesi kontrolü
        if (healthPercent < 0.0) healthPercent = 0.0;
        if (healthPercent > 1.0) healthPercent = 1.0;
        
        DisasterPhase newPhase = DisasterPhase.getCurrentPhase(healthPercent);
        
        if (newPhase != currentPhase) {
            DisasterPhase oldPhase = currentPhase;
            setCurrentPhase(newPhase);
            return oldPhase; // Eski fazı döndür (geçiş bildirimi için)
        }
        
        return null; // Faz değişmedi
    }
    
    /**
     * Can yüzdesini al (0.0 - 1.0 arası)
     * 
     * @return Can yüzdesi (0.0 - 1.0)
     */
    public double getHealthPercent() {
        if (maxHealth <= 0) return 0.0;
        double percent = getCurrentHealth() / maxHealth;
        if (percent < 0.0) return 0.0;
        if (percent > 1.0) return 1.0;
        return percent;
    }
    
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
        // Grup entity'lerini de temizle
        for (Entity e : groupEntities) {
            if (e != null && !e.isDead()) {
                e.remove();
            }
        }
        groupEntities.clear();
    }
    
    /**
     * Tip'e göre kategori
     */
    public static Category getCategory(Type type) {
        switch (type) {
            case CATASTROPHIC_TITAN:
            case CATASTROPHIC_ABYSSAL_WORM:
            case CATASTROPHIC_CHAOS_DRAGON:
            case CATASTROPHIC_VOID_TITAN:
            case CATASTROPHIC_ICE_LEVIATHAN:
            case ZOMBIE_HORDE:
            case SKELETON_LEGION:
            case SPIDER_SWARM:
            case CREEPER_SWARM:
            case ZOMBIE_WAVE:
                return Category.CREATURE;
            case SOLAR_FLARE:
            case EARTHQUAKE:
            case STORM:
            case METEOR_SHOWER:
            case VOLCANIC_ERUPTION:
                return Category.NATURAL;
            case BOSS_BUFF_WAVE:
            case MOB_INVASION:
            case PLAYER_BUFF_WAVE:
                return Category.MINI;
            default:
                return Category.CREATURE;
        }
    }
    
    /**
     * Canavar felaket tipini belirle
     */
    public static CreatureDisasterType getCreatureDisasterType(Type type) {
        switch (type) {
            case CATASTROPHIC_TITAN:
            case CATASTROPHIC_ABYSSAL_WORM:
            case CATASTROPHIC_CHAOS_DRAGON:
            case CATASTROPHIC_VOID_TITAN:
            case CATASTROPHIC_ICE_LEVIATHAN:
                return CreatureDisasterType.SINGLE_BOSS;
            case ZOMBIE_HORDE:
            case SKELETON_LEGION:
            case SPIDER_SWARM:
                return CreatureDisasterType.MEDIUM_GROUP;
            case CREEPER_SWARM:
            case ZOMBIE_WAVE:
                return CreatureDisasterType.MINI_SWARM;
            default:
                return null;
        }
    }
    
    /**
     * Tip'e göre varsayılan kategori seviyesi (otomatik spawn sıklığı)
     * Not: Bu kategori seviyesidir. Admin komutunda iç seviye (1-3) belirtilebilir.
     */
    public static int getDefaultLevel(Type type) {
        switch (type) {
            case SOLAR_FLARE:
            case CREEPER_SWARM:
            case ZOMBIE_WAVE:
            case BOSS_BUFF_WAVE:
            case MOB_INVASION:
            case PLAYER_BUFF_WAVE:
                return 1;
            case CATASTROPHIC_ABYSSAL_WORM:
            case EARTHQUAKE:
            case STORM:
            case METEOR_SHOWER:
            case CATASTROPHIC_ICE_LEVIATHAN:
            case ZOMBIE_HORDE:
            case SKELETON_LEGION:
            case SPIDER_SWARM:
                return 2;
            case CATASTROPHIC_TITAN:
            case CATASTROPHIC_CHAOS_DRAGON:
            case CATASTROPHIC_VOID_TITAN:
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
            case STORM:
                return baseDuration; // 20 dakika
            case METEOR_SHOWER:
                return baseDuration; // 20 dakika
            case VOLCANIC_ERUPTION:
                return baseDuration * 2; // 60 dakika
            case BOSS_BUFF_WAVE:
            case MOB_INVASION:
            case PLAYER_BUFF_WAVE:
                // Mini felaketler: 5-15 dakika arası rastgele
                long minDuration = 300000L;  // 5 dakika
                long maxDuration = 900000L;  // 15 dakika
                return minDuration + (long)(Math.random() * (maxDuration - minDuration));
            default:
                return baseDuration;
        }
    }
}
