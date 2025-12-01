package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

/**
 * Zindan yönetim sistemi
 * Difficulty seviyesine göre zindanlar spawn eder
 */
public class DungeonManager {
    private final Main plugin;
    private final DifficultyManager difficultyManager;
    private final MobManager mobManager;
    
    // Seviye -> Zindan tipi listesi
    private final Map<Integer, List<String>> dungeonTypes = new HashMap<>();
    // Seviye -> Spawn şansı
    private final Map<Integer, Double> spawnChances = new HashMap<>();
    // Spawn edilmiş zindanlar (chunk bazlı, tekrar spawn'ı önlemek için)
    private final Set<String> spawnedDungeons = new HashSet<>();
    
    private final Random random = new Random();
    
    public DungeonManager(Main plugin) {
        this.plugin = plugin;
        this.difficultyManager = plugin.getDifficultyManager();
        this.mobManager = plugin.getMobManager();
        
        // Null kontrolü
        if (difficultyManager == null) {
            plugin.getLogger().severe("DifficultyManager bulunamadı! Zindan sistemi çalışmayabilir.");
        }
        if (mobManager == null) {
            plugin.getLogger().severe("MobManager bulunamadı! Zindan mob spawn çalışmayabilir.");
        }
        
        loadConfig();
        createDungeonDirectories();
    }
    
    /**
     * Config'den zindan ayarlarını yükle
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Enabled kontrolü
        boolean enabled = config.getBoolean("dungeons.enabled", true);
        if (!enabled) {
            plugin.getLogger().info("Zindan sistemi devre dışı (config'de enabled: false)");
            return;
        }
        
        // Spawn şansları
        for (int level = 1; level <= 5; level++) {
            double chance = config.getDouble("dungeons.spawn-chance.level" + level, 0.05);
            spawnChances.put(level, chance);
            
            // Zindan tipleri
            List<String> types = config.getStringList("dungeons.types.level" + level);
            if (types.isEmpty()) {
                // Varsayılan zindanlar
                types = getDefaultDungeonsForLevel(level);
                plugin.getLogger().info("Seviye " + level + " için varsayılan zindanlar kullanılıyor");
            }
            dungeonTypes.put(level, types);
        }
        
        plugin.getLogger().info("Zindan ayarları yüklendi: " + dungeonTypes.size() + " seviye");
    }
    
    /**
     * Varsayılan zindan listesi (config yoksa)
     */
    private List<String> getDefaultDungeonsForLevel(int level) {
        switch (level) {
            case 1:
                return Arrays.asList("goblin_cave", "spider_nest", "bandit_hideout");
            case 2:
                return Arrays.asList("orc_fortress", "skeleton_crypt", "dark_temple");
            case 3:
                return Arrays.asList("dragon_lair", "ancient_ruins", "demon_castle");
            case 4:
                return Arrays.asList("titan_tomb", "void_prison", "hell_fortress");
            case 5:
                return Arrays.asList("cosmic_temple", "god_realm", "chaos_dimension");
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Zindan klasörlerini oluştur
     */
    private void createDungeonDirectories() {
        File schematicsDir = new File(plugin.getDataFolder(), "schematics");
        File dungeonsDir = new File(schematicsDir, "dungeons");
        
        for (int level = 1; level <= 5; level++) {
            File levelDir = new File(dungeonsDir, "level" + level);
            if (!levelDir.exists()) {
                levelDir.mkdirs();
                plugin.getLogger().info("Zindan klasörü oluşturuldu: level" + level);
            }
        }
    }
    
    /**
     * Bu konumda zindan spawn edilmeli mi?
     */
    public boolean shouldSpawnDungeon(Location loc, int difficultyLevel) {
        // Config kontrolü
        FileConfiguration config = plugin.getConfig();
        boolean enabled = config.getBoolean("dungeons.enabled", true);
        if (!enabled) {
            return false;
        }
        
        if (difficultyLevel < 1 || difficultyLevel > 5) {
            return false; // Seviye 0'da zindan yok
        }
        
        // Chunk bazlı kontrol (tekrar spawn'ı önle)
        String chunkKey = getChunkKey(loc);
        if (spawnedDungeons.contains(chunkKey)) {
            return false; // Bu chunk'ta zaten zindan var
        }
        
        // Spawn şansı kontrolü
        double chance = spawnChances.getOrDefault(difficultyLevel, 0.05);
        return random.nextDouble() < chance;
    }
    
    /**
     * Chunk key oluştur (tekrar spawn kontrolü için)
     */
    private String getChunkKey(Location loc) {
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        return loc.getWorld().getName() + "_" + chunkX + "_" + chunkZ;
    }
    
    /**
     * Zindan spawn et
     */
    public void spawnDungeon(Location loc, int difficultyLevel) {
        if (loc == null || loc.getWorld() == null) {
            plugin.getLogger().warning("Geçersiz konum: zindan spawn edilemedi");
            return;
        }
        
        String chunkKey = getChunkKey(loc);
        
        // Zindan tipi seç
        String dungeonType = selectDungeonType(difficultyLevel);
        if (dungeonType == null) {
            plugin.getLogger().warning("Seviye " + difficultyLevel + " için zindan tipi bulunamadı!");
            return;
        }
        
        // Şema dosyası yolu
        String schematicPath = "dungeons/level" + difficultyLevel + "/" + dungeonType;
        
        // Şema dosyası var mı kontrol et
        if (!StructureBuilder.schematicExists(schematicPath)) {
            plugin.getLogger().fine("Şema dosyası bulunamadı (normal): " + schematicPath);
            return; // Şema yoksa sessizce çık (her chunk'ta şema olmayabilir)
        }
        
        // Yer altında spawn et (y=30-50 arası)
        Location spawnLoc = findSuitableLocation(loc);
        if (spawnLoc == null) {
            plugin.getLogger().warning("Zindan için uygun konum bulunamadı: " + loc);
            return;
        }
        
        // Şema yükle
        boolean success = StructureBuilder.pasteSchematic(spawnLoc, schematicPath);
        if (success) {
            spawnedDungeons.add(chunkKey);
            
            // Zindan içi mob spawn (mobManager null değilse)
            if (mobManager != null) {
                spawnDungeonMobs(spawnLoc, difficultyLevel);
            }
            
            // Loot yerleştir
            placeDungeonLoot(spawnLoc, difficultyLevel);
            
            plugin.getLogger().info("Zindan spawn edildi: " + dungeonType + " (Seviye " + difficultyLevel + ")");
        } else {
            plugin.getLogger().warning("Zindan şeması yüklenemedi: " + schematicPath);
        }
    }
    
    /**
     * Zindan tipi seç (rastgele)
     */
    private String selectDungeonType(int difficultyLevel) {
        List<String> types = dungeonTypes.get(difficultyLevel);
        if (types == null || types.isEmpty()) {
            return null;
        }
        return types.get(random.nextInt(types.size()));
    }
    
    /**
     * Uygun spawn konumu bul (yer altı)
     */
    private Location findSuitableLocation(Location center) {
        World world = center.getWorld();
        if (world == null) return null;
        
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();
        
        // Y=30-50 arası uygun bir yükseklik bul
        for (int y = 50; y >= 30; y--) {
            Location testLoc = new Location(world, centerX, y, centerZ);
            
            // Yeterli boş alan var mı kontrol et (10x10x10)
            if (hasEnoughSpace(testLoc, 10, 10, 10)) {
                return testLoc;
            }
        }
        
        // Uygun yer bulunamazsa merkez konumunu kullan
        return new Location(world, centerX, 40, centerZ);
    }
    
    /**
     * Yeterli boş alan var mı?
     */
    private boolean hasEnoughSpace(Location center, int radiusX, int radiusY, int radiusZ) {
        World world = center.getWorld();
        if (world == null) return false;
        
        int airCount = 0;
        int totalCount = 0;
        
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = 0; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (loc.getBlock().getType().isAir()) {
                        airCount++;
                    }
                    totalCount++;
                }
            }
        }
        
        // %60'tan fazla hava varsa uygun
        return (double) airCount / totalCount > 0.6;
    }
    
    /**
     * Zindan içi mob spawn
     */
    private void spawnDungeonMobs(Location dungeonCenter, int difficultyLevel) {
        if (mobManager == null || dungeonCenter == null || dungeonCenter.getWorld() == null) {
            return;
        }
        
        // Zindan merkezinden 10-20 blok mesafede mob spawn
        int mobCount = 3 + difficultyLevel; // Seviye 1: 4 mob, Seviye 5: 8 mob
        
        for (int i = 0; i < mobCount; i++) {
            // Rastgele konum (dungeon merkezinden 5-15 blok)
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 5 + random.nextDouble() * 10;
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            
            Location mobLoc = dungeonCenter.clone().add(offsetX, 0, offsetZ);
            
            // Yüzey bul
            mobLoc.setY(findSurface(mobLoc));
            
            // Difficulty seviyesine göre mob spawn
            spawnMobForDifficulty(mobLoc, difficultyLevel);
        }
    }
    
    /**
     * Yüzey yüksekliğini bul
     */
    private double findSurface(Location loc) {
        World world = loc.getWorld();
        if (world == null) return loc.getY();
        
        for (int y = (int) loc.getY(); y >= 0; y--) {
            Location testLoc = new Location(world, loc.getX(), y, loc.getZ());
            if (!testLoc.getBlock().getType().isAir()) {
                return y + 1;
            }
        }
        return loc.getY();
    }
    
    /**
     * Difficulty seviyesine göre mob spawn
     */
    private void spawnMobForDifficulty(Location loc, int difficultyLevel) {
        if (mobManager == null || loc == null || loc.getWorld() == null) {
            return;
        }
        
        // DifficultyManager'dan mob spawn şanslarını al
        // Burada sadece örnek, gerçek implementasyon DifficultyManager'a göre olacak
        
        try {
            switch (difficultyLevel) {
                case 1:
                    if (random.nextDouble() < 0.5) {
                        mobManager.spawnGoblin(loc);
                    } else {
                        mobManager.spawnOrk(loc);
                    }
                    break;
                case 2:
                    if (random.nextDouble() < 0.3) {
                        mobManager.spawnTroll(loc);
                    } else {
                        mobManager.spawnSkeletonKnight(loc);
                    }
                    break;
                case 3:
                    if (random.nextDouble() < 0.2) {
                        mobManager.spawnDragon(loc);
                    } else {
                        mobManager.spawnTRex(loc);
                    }
                    break;
                case 4:
                    if (random.nextDouble() < 0.1) {
                        mobManager.spawnTitanGolem(loc, null);
                    } else {
                        mobManager.spawnHydra(loc);
                    }
                    break;
                case 5:
                    // En güçlü moblar
                    mobManager.spawnTitanGolem(loc, null);
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Zindan mob spawn hatası: " + e.getMessage());
        }
    }
    
    /**
     * Zindan loot yerleştir
     */
    private void placeDungeonLoot(Location loc, int difficultyLevel) {
        // Loot chest'leri zindan içine yerleştir
        // Şimdilik basit implementasyon, sonra genişletilebilir
        
        int chestCount = 1 + difficultyLevel / 2; // Seviye 1: 1, Seviye 5: 3
        
        for (int i = 0; i < chestCount; i++) {
            // Rastgele konum
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 3 + random.nextDouble() * 7;
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            
            Location chestLoc = loc.clone().add(offsetX, 0, offsetZ);
            chestLoc.setY(findSurface(chestLoc));
            
            // Chest yerleştir (şimdilik sadece log, gerçek implementasyon için ChestManager gerekebilir)
            // chestLoc.getBlock().setType(Material.CHEST);
        }
    }
    
    /**
     * Manuel zindan spawn (admin komutu için)
     */
    public boolean spawnDungeonManually(Location loc, int difficultyLevel, String dungeonType) {
        if (loc == null || loc.getWorld() == null || dungeonType == null || dungeonType.isEmpty()) {
            return false;
        }
        
        String schematicPath = "dungeons/level" + difficultyLevel + "/" + dungeonType;
        
        // Şema dosyası var mı kontrol et
        if (!StructureBuilder.schematicExists(schematicPath)) {
            plugin.getLogger().warning("Şema dosyası bulunamadı: " + schematicPath);
            return false;
        }
        
        Location spawnLoc = findSuitableLocation(loc);
        if (spawnLoc == null) {
            return false;
        }
        
        boolean success = StructureBuilder.pasteSchematic(spawnLoc, schematicPath);
        if (success) {
            if (mobManager != null) {
                spawnDungeonMobs(spawnLoc, difficultyLevel);
            }
            placeDungeonLoot(spawnLoc, difficultyLevel);
        }
        
        return success;
    }
    
    /**
     * Mevcut zindan tiplerini listele
     */
    public List<String> getDungeonTypes(int difficultyLevel) {
        return dungeonTypes.getOrDefault(difficultyLevel, new ArrayList<>());
    }
    
    /**
     * Spawn edilmiş zindanları temizle (test için)
     */
    public void clearSpawnedDungeons() {
        spawnedDungeons.clear();
    }
}

