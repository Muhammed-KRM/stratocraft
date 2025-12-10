package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Disaster;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Felaket Arena Transformasyon Yönetim Sistemi
 * Felaket entity'lerinin etrafında sürekli değişen, dinamik arena
 * Boss sistemindeki NewBossArenaManager'dan adapte edilmiştir
 */
public class DisasterArenaManager {
    private final Main plugin;
    private final Map<UUID, DisasterArenaData> activeArenas;
    
    // Config ayarları
    private int blocksPerCycle = 5;
    private long taskInterval = 40L; // 2 saniye
    private double arenaExpansionLimit = 100.0; // Oyuncu 100 blok içindeyse arena genişler
    private double currentFarDistance = 150.0; // Oyuncu 150 blok içindeyse arena aktif
    
    private BukkitTask arenaTask;
    
    public DisasterArenaManager(Main plugin) {
        this.plugin = plugin;
        this.activeArenas = new ConcurrentHashMap<>();
        loadConfig();
        startArenaTask();
    }
    
    /**
     * Config'den ayarları yükle
     */
    private void loadConfig() {
        // Config'den okunabilir, şimdilik varsayılan değerler
        blocksPerCycle = 5;
        taskInterval = 40L;
        arenaExpansionLimit = 100.0;
        currentFarDistance = 150.0;
    }
    
    /**
     * Arena transformasyonunu başlat
     */
    public void startArenaTransformation(Location center, Disaster.Type disasterType, int level, UUID disasterId) {
        // Arena verisi oluştur
        DisasterArenaData arena = new DisasterArenaData(center, disasterType, level, disasterId);
        activeArenas.put(disasterId, arena);
        
        // İlk kuleleri hemen oluştur
        int maxRadius = getArenaRadius(level);
        createDisasterTowers(arena, maxRadius);
        
        plugin.getLogger().info("Felaket arena transformasyonu başlatıldı: " + disasterId);
    }
    
    /**
     * Arena transformasyonunu durdur
     */
    public void stopArenaTransformation(UUID disasterId) {
        DisasterArenaData arena = activeArenas.remove(disasterId);
        if (arena != null) {
            // Arena bloklarını temizle (opsiyonel)
            plugin.getLogger().info("Felaket arena transformasyonu durduruldu: " + disasterId);
        }
    }
    
    /**
     * Arena yarıçapını seviyeye göre hesapla
     */
    private int getArenaRadius(int level) {
        switch (level) {
            case 1: return 20;
            case 2: return 30;
            case 3: return 40;
            default: return 25;
        }
    }
    
    /**
     * Merkezi arena task'ı başlat
     */
    private void startArenaTask() {
        arenaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeArenas.isEmpty()) return;
                
                for (Map.Entry<UUID, DisasterArenaData> entry : activeArenas.entrySet()) {
                    UUID disasterId = entry.getKey();
                    DisasterArenaData arena = entry.getValue();
                    
                    // Felaket kontrolü
                    DisasterManager disasterManager = plugin.getDisasterManager();
                    if (disasterManager == null) continue;
                    
                    Disaster disaster = disasterManager.getActiveDisaster();
                    if (disaster == null || disaster.isDead() || 
                        !disaster.getEntity().getUniqueId().equals(disasterId)) {
                        stopArenaTransformation(disasterId);
                        continue;
                    }
                    
                    // Entity'nin güncel konumunu al
                    Entity entity = disaster.getEntity();
                    if (entity == null || entity.isDead()) {
                        stopArenaTransformation(disasterId);
                        continue;
                    }
                    
                    Location entityLoc = entity.getLocation();
                    arena.setCenter(entityLoc);
                    
                    // Arena transformasyonunu gerçekleştir
                    transformArenaBlocks(arena);
                }
            }
        }.runTaskTimer(plugin, 0L, taskInterval);
    }
    
    /**
     * Arena bloklarını dönüştür
     */
    private void transformArenaBlocks(DisasterArenaData arena) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        // En yakın oyuncu mesafesini al
        double nearestPlayerDistance = getNearestPlayerDistance(center);
        boolean isNearArena = nearestPlayerDistance <= currentFarDistance;
        
        if (!isNearArena) {
            return; // Uzak arenalar için transformasyon yapılmaz
        }
        
        int maxRadius = getArenaRadius(arena.getLevel());
        
        // Kuleler sürekli oluşturulur
        arena.incrementCycleCount();
        boolean isWithinExpansionLimit = nearestPlayerDistance <= arenaExpansionLimit;
        
        if (isWithinExpansionLimit) {
            // Her 60 saniyede bir (30 döngü) yeni kuleler
            if (arena.getCycleCount() % 30 == 0) {
                createDisasterTowers(arena, maxRadius);
            }
            
            // Rastgele tehlikeler oluştur
            if (arena.getCycleCount() % 20 == 0) {
                createRandomHazards(arena, maxRadius);
            }
        }
        
        // Mevcut genişleme yarıçapını artır
        double currentRadius = arena.getCurrentRadius();
        if (isWithinExpansionLimit && currentRadius < maxRadius) {
            arena.setCurrentRadius(currentRadius + 1.2); // Her 2 saniyede 1.2 blok genişle
        }
        
        // Şu anki radius'ta blokları dönüştür
        if (!isWithinExpansionLimit) {
            return;
        }
        
        int transformed = 0;
        int maxAttempts = blocksPerCycle * 3;
        
        for (int attempt = 0; attempt < maxAttempts && transformed < blocksPerCycle; attempt++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = currentRadius + (Math.random() * 3 - 1.5);
            if (distance < 3) distance = 3;
            if (distance > maxRadius) distance = maxRadius;
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) {
                continue;
            }
            
            int y = findGroundLevel(world, x, center.getBlockY(), z);
            if (y == -1) continue;
            
            Location blockLoc = new Location(world, x, y, z);
            if (blockLoc.distance(center) < 3) continue;
            
            transformSingleBlock(blockLoc, arena);
            transformed++;
        }
    }
    
    /**
     * Tek bir bloğu dönüştür
     */
    private void transformSingleBlock(Location loc, DisasterArenaData arena) {
        Block block = loc.getBlock();
        Material currentType = block.getType();
        
        // Hava veya yapılamaz blokları atla
        if (currentType == Material.AIR || 
            currentType == Material.BEDROCK ||
            currentType == Material.BARRIER) {
            return;
        }
        
        // Felaket tipine göre blok dönüşümü
        Material targetMaterial = getTransformationMaterial(arena.getDisasterType());
        
        if (targetMaterial != null && currentType != targetMaterial) {
            block.setType(targetMaterial);
            
            // Partikül efekti
            loc.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                loc.add(0.5, 0.5, 0.5),
                10,
                0.3, 0.3, 0.3,
                0.1,
                targetMaterial.createBlockData()
            );
        }
    }
    
    /**
     * Felaket tipine göre dönüşüm materyali
     */
    private Material getTransformationMaterial(Disaster.Type disasterType) {
        switch (disasterType) {
            case CATASTROPHIC_TITAN:
                return Material.OBSIDIAN;
            case CATASTROPHIC_CHAOS_DRAGON:
                return Material.NETHERRACK;
            case CATASTROPHIC_ICE_LEVIATHAN:
                return Material.PACKED_ICE;
            case CATASTROPHIC_ABYSSAL_WORM:
                return Material.DEEPSLATE;
            case CATASTROPHIC_VOID_TITAN:
                return Material.END_STONE;
            default:
                return Material.OBSIDIAN;
        }
    }
    
    /**
     * Felaket kuleleri oluştur
     */
    private void createDisasterTowers(DisasterArenaData arena, int maxRadius) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        int towerCount = 5 + new Random().nextInt(5); // 5-9 kule
        
        for (int i = 0; i < towerCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = 5 + Math.random() * (maxRadius - 5);
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) continue;
            
            int y = findGroundLevel(world, x, center.getBlockY(), z);
            if (y == -1) continue;
            
            Location towerLoc = new Location(world, x, y, z);
            createTower(towerLoc, arena);
        }
    }
    
    /**
     * Kule oluştur
     */
    private void createTower(Location loc, DisasterArenaData arena) {
        World world = loc.getWorld();
        if (world == null) return;
        
        Material towerMaterial = getTransformationMaterial(arena.getDisasterType());
        int height = 2 + new Random().nextInt(14); // 2-15 blok yükseklik
        int width = 1 + new Random().nextInt(6); // 1-6 blok genişlik
        
        for (int y = 0; y < height; y++) {
            for (int x = -width/2; x <= width/2; x++) {
                for (int z = -width/2; z <= width/2; z++) {
                    Block block = world.getBlockAt(
                        loc.getBlockX() + x,
                        loc.getBlockY() + y,
                        loc.getBlockZ() + z
                    );
                    
                    if (block.getType() == Material.AIR || 
                        block.getType() == Material.GRASS_BLOCK ||
                        block.getType() == Material.DIRT) {
                        block.setType(towerMaterial);
                    }
                }
            }
        }
    }
    
    /**
     * Rastgele tehlikeler oluştur
     */
    private void createRandomHazards(DisasterArenaData arena, int maxRadius) {
        Location center = arena.getCenter();
        World world = center.getWorld();
        if (world == null) return;
        
        int hazardCount = 3 + new Random().nextInt(5); // 3-7 tehlike
        
        for (int i = 0; i < hazardCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = 5 + Math.random() * (maxRadius - 5);
            
            int x = (int) (center.getX() + distance * Math.cos(angle));
            int z = (int) (center.getZ() + distance * Math.sin(angle));
            
            Chunk chunk = world.getChunkAt(x >> 4, z >> 4);
            if (!chunk.isLoaded()) continue;
            
            int y = findGroundLevel(world, x, center.getBlockY(), z);
            if (y == -1) continue;
            
            Location hazardLoc = new Location(world, x, y, z);
            createHazard(hazardLoc, arena);
        }
    }
    
    /**
     * Tehlike oluştur (lav, su, örümcek ağı)
     */
    private void createHazard(Location loc, DisasterArenaData arena) {
        World world = loc.getWorld();
        if (world == null) return;
        
        Material hazardMaterial;
        switch (arena.getDisasterType()) {
            case CATASTROPHIC_CHAOS_DRAGON:
                hazardMaterial = Material.LAVA;
                break;
            case CATASTROPHIC_ICE_LEVIATHAN:
                hazardMaterial = Material.WATER;
                break;
            default:
                hazardMaterial = Material.COBWEB;
        }
        
        Block block = world.getBlockAt(loc);
        if (block.getType() == Material.AIR || 
            block.getType() == Material.GRASS_BLOCK ||
            block.getType() == Material.DIRT) {
            block.setType(hazardMaterial);
        }
    }
    
    /**
     * Zemin seviyesini bul
     */
    private int findGroundLevel(World world, int x, int startY, int z) {
        int maxY = Math.min(startY + 20, world.getMaxHeight());
        int minY = Math.max(startY - 20, world.getMinHeight());
        
        for (int y = maxY; y >= minY; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getType() != Material.AIR && 
                block.getType() != Material.CAVE_AIR &&
                block.getType() != Material.VOID_AIR) {
                return y;
            }
        }
        return -1;
    }
    
    /**
     * En yakın oyuncu mesafesini al
     */
    private double getNearestPlayerDistance(Location center) {
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : center.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(center);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        
        return minDistance == Double.MAX_VALUE ? Double.MAX_VALUE : minDistance;
    }
    
    /**
     * Task'ı durdur
     */
    public void stop() {
        if (arenaTask != null) {
            arenaTask.cancel();
        }
        activeArenas.clear();
    }
    
    /**
     * Felaket Arena Verisi
     */
    private static class DisasterArenaData {
        private Location center;
        private final Disaster.Type disasterType;
        private final int level;
        private final UUID disasterId;
        private double currentRadius = 3.0;
        private int cycleCount = 0;
        
        public DisasterArenaData(Location center, Disaster.Type disasterType, int level, UUID disasterId) {
            this.center = center;
            this.disasterType = disasterType;
            this.level = level;
            this.disasterId = disasterId;
        }
        
        public Location getCenter() { return center; }
        public void setCenter(Location center) { this.center = center; }
        public Disaster.Type getDisasterType() { return disasterType; }
        public int getLevel() { return level; }
        public UUID getDisasterId() { return disasterId; }
        public double getCurrentRadius() { return currentRadius; }
        public void setCurrentRadius(double radius) { this.currentRadius = radius; }
        public int getCycleCount() { return cycleCount; }
        public void incrementCycleCount() { this.cycleCount++; }
    }
}

