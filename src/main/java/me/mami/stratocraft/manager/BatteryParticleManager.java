package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Batarya Partikül Yönetim Sistemi
 * 
 * Özellikler:
 * - Bakış açısına göre farklı partiküller (FPS vs 3. kişi)
 * - FPS modunda: Ufak partiküller (bel hizasında, görüşü kapatmayacak)
 * - 3. kişi modunda: Normal partiküller
 * - Diğer oyuncular: Normal partiküller
 * - Config'den ayarlanabilir
 * - Modüler ve optimize
 */
public class BatteryParticleManager {
    
    private final Main plugin;
    // ✅ THREAD SAFETY: ConcurrentHashMap kullan (multi-threaded environment)
    private final Map<UUID, Map<Integer, BukkitTask>> activeParticleTasks = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Config değerleri (default)
    private double normalRadius = 1.5;
    private double fpsRadius = 0.3;
    private double normalHeight = 1.0;
    private double fpsHeight = 0.5; // Bel hizası
    private int normalParticleCount = 8;
    private int fpsParticleCount = 2;
    private double normalRotationSpeed = 2.0;
    private double fpsRotationSpeed = 1.0;
    private long updateInterval = 2L; // Tick
    private double fpsDetectionThreshold = 0.3; // Göz-vücut mesafe eşiği
    
    public BatteryParticleManager(Main plugin) {
        this.plugin = plugin;
        // Config yükleme Main.java'da onEnable'da yapılacak (configManager hazır olduktan sonra)
        // Constructor'da yükleme yapılmıyor çünkü configManager henüz hazır olmayabilir
    }
    
    /**
     * Config'den ayarları yükle
     */
    public void loadConfig(FileConfiguration config) {
        if (config == null) return;
        
        // Config path
        String path = "battery.particles.";
        
        normalRadius = config.getDouble(path + "normal-radius", 1.5);
        fpsRadius = config.getDouble(path + "fps-radius", 0.3);
        normalHeight = config.getDouble(path + "normal-height", 1.0);
        fpsHeight = config.getDouble(path + "fps-height", 0.5);
        normalParticleCount = config.getInt(path + "normal-count", 8);
        fpsParticleCount = config.getInt(path + "fps-count", 2);
        normalRotationSpeed = config.getDouble(path + "normal-rotation-speed", 2.0);
        fpsRotationSpeed = config.getDouble(path + "fps-rotation-speed", 1.0);
        updateInterval = config.getLong(path + "update-interval", 2L);
        fpsDetectionThreshold = config.getDouble(path + "fps-detection-threshold", 0.3);
    }
    
    /**
     * Batarya partikül efektini başlat
     */
    public void startBatteryParticles(Player player, int slot, Particle particleType) {
        if (player == null || particleType == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Eski task'ı durdur (eğer varsa)
        stopBatteryParticles(player, slot);
        
        // Task map'i oluştur
        activeParticleTasks.putIfAbsent(playerId, new HashMap<>());
        
        // Yeni task başlat
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                // Batarya hala aktif mi?
                if (plugin.getNewBatteryManager() != null && 
                    !plugin.getNewBatteryManager().hasLoadedBattery(player, slot)) {
                    cancel();
                    stopBatteryParticles(player, slot);
                    return;
                }
                
                // Partikülleri göster
                spawnBatteryParticles(player, slot, particleType);
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
        
        activeParticleTasks.get(playerId).put(slot, task);
        
        // ✅ TASK MANAGER: Task'ı kaydet (memory leak önleme)
        if (plugin.getTaskManager() != null) {
            plugin.getTaskManager().registerPlayerTask(player, task);
        }
    }
    
    /**
     * Batarya partikül efektini durdur
     */
    public void stopBatteryParticles(Player player, int slot) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        Map<Integer, BukkitTask> playerTasks = activeParticleTasks.get(playerId);
        
        if (playerTasks != null) {
            BukkitTask task = playerTasks.remove(slot);
            if (task != null) {
                task.cancel();
            }
            
            // Eğer başka task kalmadıysa map'i temizle
            if (playerTasks.isEmpty()) {
                activeParticleTasks.remove(playerId);
            }
        }
    }
    
    /**
     * Oyuncu için tüm partikül efektlerini durdur
     */
    public void stopAllBatteryParticles(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        Map<Integer, BukkitTask> playerTasks = activeParticleTasks.remove(playerId);
        
        if (playerTasks != null) {
            for (BukkitTask task : playerTasks.values()) {
                if (task != null) {
                    task.cancel();
                }
            }
        }
    }
    
    /**
     * Partikülleri spawn et (bakış açısına göre)
     */
    private void spawnBatteryParticles(Player player, int slot, Particle particleType) {
        if (player == null || !player.isOnline()) return;
        
        Location playerLoc = player.getLocation();
        if (playerLoc == null || playerLoc.getWorld() == null) return;
        
        // Bakış açısını kontrol et
        boolean isFirstPerson = isFirstPersonView(player);
        
        // Partikül parametrelerini belirle
        double radius = isFirstPerson ? fpsRadius : normalRadius;
        double height = isFirstPerson ? fpsHeight : normalHeight;
        int particleCount = isFirstPerson ? fpsParticleCount : normalParticleCount;
        double rotationSpeed = isFirstPerson ? fpsRotationSpeed : normalRotationSpeed;
        
        // Zaman bazlı açı
        double time = System.currentTimeMillis() / 1000.0;
        
        // Partikül pozisyonlarını hesapla
        for (int i = 0; i < particleCount; i++) {
            double angle = (time * rotationSpeed) + (i * (2 * Math.PI / particleCount));
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = height + (isFirstPerson ? 0 : Math.sin(time * 3.0) * 0.3);
            
            Location particleLoc = playerLoc.clone().add(x, y, z);
            
            // Tüm oyunculara göster
            // ✅ OPTİMİZE: getNearbyPlayers() kullan ve distanceSquared() kullan
            double maxViewDistance = 32.0;
            double maxViewDistanceSquared = maxViewDistance * maxViewDistance;
            
            for (Player viewer : playerLoc.getWorld().getNearbyPlayers(playerLoc, maxViewDistance)) {
                if (viewer == null || !viewer.isOnline()) continue;
                if (viewer.getWorld() != playerLoc.getWorld()) continue;
                
                Location viewerLoc = viewer.getLocation();
                if (viewerLoc == null) continue;
                
                // ✅ OPTİMİZE: Mesafe kontrolü (distanceSquared - performans)
                if (viewerLoc.distanceSquared(playerLoc) > maxViewDistanceSquared) continue;
                
                // Kendi partiküllerini göster (bakış açısına göre)
                if (viewer.equals(player)) {
                    if (isFirstPerson) {
                        // FPS modunda: Ufak partiküller (bel hizasında)
                        spawnSmallParticle(viewer, particleLoc, particleType);
                    } else {
                        // 3. kişi modunda: Normal partiküller
                        viewer.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0);
                    }
                } else {
                    // Diğer oyuncular: Normal partiküller
                    viewer.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }
    }
    
    /**
     * Ufak partikül spawn et (FPS modu için - görüşü kapatmayacak)
     */
    private void spawnSmallParticle(Player viewer, Location loc, Particle particleType) {
        if (viewer == null || loc == null) return;
        
        // Ufak partikül için özel ayarlar (görüşü kapatmayacak şekilde)
        switch (particleType) {
            case FLAME:
                // Ateş için ufak alev partikülü (FLAME yerine daha ufak)
                viewer.spawnParticle(Particle.FLAME, loc, 1, 0.05, 0.05, 0.05, 0.01);
                break;
            case ELECTRIC_SPARK:
                // Elektrik için ufak spark
                viewer.spawnParticle(Particle.ELECTRIC_SPARK, loc, 1, 0.05, 0.05, 0.05, 0.01);
                break;
            case SNOWBALL:
                // Buz için ufak kar tanesi
                viewer.spawnParticle(Particle.SNOWFLAKE, loc, 1, 0.05, 0.05, 0.05, 0.01);
                break;
            default:
                // Diğer partiküller için ufak versiyon (offset ve speed çok küçük)
                viewer.spawnParticle(particleType, loc, 1, 0.05, 0.05, 0.05, 0.01);
                break;
        }
    }
    
    /**
     * Oyuncu FPS modunda mı? (1. bakış açısı)
     * 
     * Yöntem: Oyuncunun göz pozisyonu ile vücut pozisyonu arasındaki mesafeyi kontrol et
     * FPS modunda göz pozisyonu vücut pozisyonuna çok yakındır
     */
    private boolean isFirstPersonView(Player player) {
        if (player == null) return false;
        
        Location eyeLoc = player.getEyeLocation();
        Location bodyLoc = player.getLocation();
        
        if (eyeLoc == null || bodyLoc == null) return false;
        
        // Göz ve vücut arasındaki mesafe
        double distance = eyeLoc.distance(bodyLoc);
        
        // Eşik değerinden küçükse FPS modunda
        return distance < fpsDetectionThreshold;
    }
    
    /**
     * Batarya isminden partikül tipini belirle
     */
    public static Particle getParticleType(String batteryName) {
        if (batteryName == null) return Particle.ENCHANTMENT_TABLE;
        
        String name = batteryName.toLowerCase();
        
        if (name.contains("ateş") || name.contains("cehennem") || name.contains("lava")) {
            return Particle.FLAME;
        } else if (name.contains("yıldırım") || name.contains("elektrik") || name.contains("şok")) {
            return Particle.ELECTRIC_SPARK;
        } else if (name.contains("buz") || name.contains("kale")) {
            return Particle.SNOWBALL;
        } else if (name.contains("zehir") || name.contains("asit")) {
            return Particle.DRIP_LAVA;
        } else if (name.contains("meteor") || name.contains("kıyamet")) {
            return Particle.EXPLOSION_LARGE;
        } else if (name.contains("köprü") || name.contains("duvar") || name.contains("kale")) {
            return Particle.VILLAGER_HAPPY;
        } else if (name.contains("can") || name.contains("yenilenme")) {
            return Particle.HEART;
        } else if (name.contains("hız")) {
            return Particle.CLOUD;
        } else if (name.contains("hasar")) {
            return Particle.CRIT;
        } else if (name.contains("zırh")) {
            return Particle.TOTEM;
        }
        
        return Particle.ENCHANTMENT_TABLE;
    }
}

