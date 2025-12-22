package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;

/**
 * ✅ YENİ: Mini Felaket Yöneticisi
 * 
 * 3 farklı mini felaket:
 * 1. Crystal Hunter Wave (Orta güç)
 * 2. Crystal Destroyer Army (Yüksek güç)
 * 3. Crystal Catastrophe (Çok yüksek güç)
 */
public class MiniDisasterManager {
    private final Main plugin;
    private final Random random = new Random();
    
    // Spawn task'ları
    private BukkitTask hunterWaveTask;
    private BukkitTask destroyerArmyTask;
    private BukkitTask catastropheTask;
    
    // Config değerleri
    private MiniDisasterConfig hunterWaveConfig;
    private MiniDisasterConfig destroyerArmyConfig;
    private MiniDisasterConfig catastropheConfig;
    
    /**
     * Mini felaket config sınıfı
     */
    public static class MiniDisasterConfig {
        public boolean enabled;
        public long spawnInterval; // Saniye
        public int mobCount;
        public double healthBonus; // Çarpan (örn: 0.5 = %50 artış)
        public double damageBonus;
        public double crystalDamageBonus;
        public int speedLevel;
        public int resistanceLevel;
        public int regenerationLevel;
        
        public MiniDisasterConfig(boolean enabled, long interval, int count,
                                 double healthBonus, double damageBonus, double crystalDamageBonus,
                                 int speedLevel, int resistanceLevel, int regenerationLevel) {
            this.enabled = enabled;
            this.spawnInterval = interval;
            this.mobCount = count;
            this.healthBonus = healthBonus;
            this.damageBonus = damageBonus;
            this.crystalDamageBonus = crystalDamageBonus;
            this.speedLevel = speedLevel;
            this.resistanceLevel = resistanceLevel;
            this.regenerationLevel = regenerationLevel;
        }
    }
    
    public MiniDisasterManager(Main plugin) {
        this.plugin = plugin;
        loadConfigs();
    }
    
    /**
     * Config'den ayarları yükle
     */
    private void loadConfigs() {
        if (plugin.getConfigManager() == null || plugin.getConfigManager().getConfig() == null) {
            // Config yoksa default değerler kullan
            loadDefaultConfigs();
            return;
        }
        
        // Crystal Hunter Wave
        hunterWaveConfig = new MiniDisasterConfig(
            plugin.getConfigManager().getConfig().getBoolean(
                "mini-disasters.crystal-hunter-wave.enabled", true),
            plugin.getConfigManager().getConfig().getLong(
                "mini-disasters.crystal-hunter-wave.spawn-interval", 1800L), // 30 dakika
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-hunter-wave.mob-count", 25),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-hunter-wave.health-bonus", 0.5),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-hunter-wave.damage-bonus", 0.3),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-hunter-wave.crystal-damage-bonus", 2.0),
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-hunter-wave.speed-level", 1),
            0, // Resistance yok
            0  // Regeneration yok
        );
        
        // Crystal Destroyer Army
        destroyerArmyConfig = new MiniDisasterConfig(
            plugin.getConfigManager().getConfig().getBoolean(
                "mini-disasters.crystal-destroyer-army.enabled", true),
            plugin.getConfigManager().getConfig().getLong(
                "mini-disasters.crystal-destroyer-army.spawn-interval", 2400L), // 40 dakika
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-destroyer-army.mob-count", 18),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-destroyer-army.health-bonus", 1.0),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-destroyer-army.damage-bonus", 0.5),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-destroyer-army.crystal-damage-bonus", 3.0),
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-destroyer-army.speed-level", 2),
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-destroyer-army.resistance-level", 1),
            0  // Regeneration yok
        );
        
        // Crystal Catastrophe
        catastropheConfig = new MiniDisasterConfig(
            plugin.getConfigManager().getConfig().getBoolean(
                "mini-disasters.crystal-catastrophe.enabled", true),
            plugin.getConfigManager().getConfig().getLong(
                "mini-disasters.crystal-catastrophe.spawn-interval", 3600L), // 60 dakika
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-catastrophe.mob-count", 12),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-catastrophe.health-bonus", 1.5),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-catastrophe.damage-bonus", 0.75),
            plugin.getConfigManager().getConfig().getDouble(
                "mini-disasters.crystal-catastrophe.crystal-damage-bonus", 4.0),
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-catastrophe.speed-level", 3),
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-catastrophe.resistance-level", 2),
            plugin.getConfigManager().getConfig().getInt(
                "mini-disasters.crystal-catastrophe.regeneration-level", 1)
        );
    }
    
    /**
     * Default config değerlerini yükle
     */
    private void loadDefaultConfigs() {
        // Crystal Hunter Wave (default)
        hunterWaveConfig = new MiniDisasterConfig(
            true, 1800L, 25, 0.5, 0.3, 2.0, 1, 0, 0);
        
        // Crystal Destroyer Army (default)
        destroyerArmyConfig = new MiniDisasterConfig(
            true, 2400L, 18, 1.0, 0.5, 3.0, 2, 1, 0);
        
        // Crystal Catastrophe (default)
        catastropheConfig = new MiniDisasterConfig(
            true, 3600L, 12, 1.5, 0.75, 4.0, 3, 2, 1);
    }
    
    /**
     * Mini felaket sistemini başlat
     */
    public void start() {
        // Config'ler yüklenmemişse default değerleri yükle
        if (hunterWaveConfig == null || destroyerArmyConfig == null || catastropheConfig == null) {
            loadConfigs();
        }
        
        // Crystal Hunter Wave
        if (hunterWaveConfig != null && hunterWaveConfig.enabled) {
            startHunterWaveTask();
        }
        
        // Crystal Destroyer Army
        if (destroyerArmyConfig != null && destroyerArmyConfig.enabled) {
            startDestroyerArmyTask();
        }
        
        // Crystal Catastrophe
        if (catastropheConfig != null && catastropheConfig.enabled) {
            startCatastropheTask();
        }
    }
    
    /**
     * Mini felaket sistemini durdur
     */
    public void stop() {
        if (hunterWaveTask != null) {
            hunterWaveTask.cancel();
            hunterWaveTask = null;
        }
        if (destroyerArmyTask != null) {
            destroyerArmyTask.cancel();
            destroyerArmyTask = null;
        }
        if (catastropheTask != null) {
            catastropheTask.cancel();
            catastropheTask = null;
        }
    }
    
    /**
     * Crystal Hunter Wave task'ını başlat
     */
    private void startHunterWaveTask() {
        long intervalTicks = hunterWaveConfig.spawnInterval * 20L; // Saniye -> Tick
        
        hunterWaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnMiniDisaster("Crystal Hunter Wave", hunterWaveConfig,
                    Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER));
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks); // İlk spawn interval sonra
    }
    
    /**
     * Crystal Destroyer Army task'ını başlat
     */
    private void startDestroyerArmyTask() {
        long intervalTicks = destroyerArmyConfig.spawnInterval * 20L;
        
        destroyerArmyTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnMiniDisaster("Crystal Destroyer Army", destroyerArmyConfig,
                    Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, 
                                 EntityType.SPIDER, EntityType.CREEPER));
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }
    
    /**
     * Crystal Catastrophe task'ını başlat
     */
    private void startCatastropheTask() {
        long intervalTicks = catastropheConfig.spawnInterval * 20L;
        
        catastropheTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnMiniDisaster("Crystal Catastrophe", catastropheConfig,
                    Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER));
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }
    
    /**
     * Mini felaket spawn et
     */
    private void spawnMiniDisaster(String name, MiniDisasterConfig config, 
                                  List<EntityType> mobTypes) {
        if (plugin.getClanManager() == null) return;
        
        // Rastgele bir klan kristali seç
        List<Clan> clansWithCrystal = new ArrayList<>();
        for (Clan clan : plugin.getClanManager().getAllClans()) {
            if (clan != null && clan.hasCrystal() && clan.getCrystalLocation() != null) {
                clansWithCrystal.add(clan);
            }
        }
        
        if (clansWithCrystal.isEmpty()) {
            return; // Kristal yok, spawn yapma
        }
        
        // Rastgele klan seç
        Clan targetClan = clansWithCrystal.get(random.nextInt(clansWithCrystal.size()));
        Location crystalLoc = targetClan.getCrystalLocation();
        
        // Spawn noktaları oluştur (kristal etrafında 20-50 blok)
        List<Location> spawnPoints = generateSpawnPoints(crystalLoc, config.mobCount);
        
        int spawnedCount = 0;
        for (Location spawnPoint : spawnPoints) {
            if (spawnedCount >= config.mobCount) break;
            
            // Rastgele mob tipi seç
            EntityType mobType = mobTypes.get(random.nextInt(mobTypes.size()));
            
            // Mob spawn et
            Entity entity = spawnPoint.getWorld().spawnEntity(spawnPoint, mobType);
            
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                
                // Mob'u güçlendir
                enhanceMob(living, config, name);
                
                spawnedCount++;
            }
        }
        
        // Broadcast mesajı
        org.bukkit.Bukkit.broadcastMessage(
            org.bukkit.ChatColor.RED + "§l⚠ " + name + " başladı! " +
            org.bukkit.ChatColor.YELLOW + targetClan.getName() + 
            " klanının kristaline saldırıyor!");
    }
    
    /**
     * Spawn noktaları oluştur
     */
    private List<Location> generateSpawnPoints(Location center, int count) {
        List<Location> points = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // Rastgele açı ve mesafe
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 20.0 + random.nextDouble() * 30.0; // 20-50 blok
            
            double x = center.getX() + Math.cos(angle) * distance;
            double z = center.getZ() + Math.sin(angle) * distance;
            double y = center.getY();
            
            // Y eksenini düzelt (yüksek blok bul)
            Location spawnLoc = new Location(center.getWorld(), x, y, z);
            spawnLoc.setY(findSafeY(spawnLoc));
            
            points.add(spawnLoc);
        }
        
        return points;
    }
    
    /**
     * Güvenli Y koordinatı bul
     */
    private double findSafeY(Location loc) {
        org.bukkit.World world = loc.getWorld();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        
        // Y'den yukarı doğru bak
        for (int y = loc.getBlockY(); y < world.getMaxHeight(); y++) {
            org.bukkit.block.Block block = world.getBlockAt(x, y, z);
            org.bukkit.block.Block above = world.getBlockAt(x, y + 1, z);
            
            if (block.getType() != Material.AIR && above.getType() == Material.AIR) {
                return y + 1.0;
            }
        }
        
        return loc.getY();
    }
    
    /**
     * Mob'u güçlendir
     */
    private void enhanceMob(LivingEntity mob, MiniDisasterConfig config, String disasterName) {
        if (mob == null) return;
        
        // 1. Can artışı
        if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double baseHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            double newHealth = baseHealth * (1.0 + config.healthBonus);
            
            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
            mob.setHealth(newHealth);
        }
        
        // 2. Hasar artışı
        if (mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            double baseDamage = mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
            double newDamage = baseDamage * (1.0 + config.damageBonus);
            
            mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(newDamage);
        }
        
        // 3. Potion efektleri
        if (config.speedLevel > 0) {
            mob.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, Integer.MAX_VALUE, config.speedLevel - 1, false, false));
        }
        
        if (config.resistanceLevel > 0) {
            mob.addPotionEffect(new PotionEffect(
                PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 
                config.resistanceLevel - 1, false, false));
        }
        
        if (config.regenerationLevel > 0) {
            mob.addPotionEffect(new PotionEffect(
                PotionEffectType.REGENERATION, Integer.MAX_VALUE, 
                config.regenerationLevel - 1, false, false));
        }
        
        // 4. Özel isim
        String color = getDisasterColor(disasterName);
        mob.setCustomName(color + "§l" + disasterName + " §7" + 
                         mob.getType().name().replace("_", " "));
        mob.setCustomNameVisible(true);
        
        // 5. Metadata (kristal avcısı işareti)
        mob.setMetadata("mini_disaster_mob", 
            new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        mob.setMetadata("crystal_hunter", 
            new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        mob.setMetadata("crystal_damage_bonus", 
            new org.bukkit.metadata.FixedMetadataValue(plugin, config.crystalDamageBonus));
    }
    
    /**
     * Felaket rengi
     */
    private String getDisasterColor(String name) {
        if (name.contains("Hunter")) return "§e"; // Sarı
        if (name.contains("Destroyer")) return "§c"; // Kırmızı
        if (name.contains("Catastrophe")) return "§4"; // Koyu kırmızı
        return "§7"; // Gri
    }
}

