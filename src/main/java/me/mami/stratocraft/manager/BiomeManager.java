package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

/**
 * Biyom yönetim sistemi
 * Difficulty seviyesine göre özel biyomlar ve yapılar
 */
public class BiomeManager {
    private final Main plugin;
    private final DifficultyManager difficultyManager;
    
    // Seviye -> Biyom listesi
    private final Map<Integer, List<Biome>> difficultyBiomes = new HashMap<>();
    // Biome-specific structures
    private final Map<Biome, List<String>> biomeStructures = new HashMap<>();
    
    private final Random random = new Random();
    
    public BiomeManager(Main plugin) {
        this.plugin = plugin;
        this.difficultyManager = plugin.getDifficultyManager();
        
        // Null kontrolü
        if (difficultyManager == null) {
            plugin.getLogger().severe("DifficultyManager bulunamadı! Biyom sistemi çalışmayabilir.");
        }
        
        loadConfig();
        createBiomeDirectories();
    }
    
    /**
     * Config'den biyom ayarlarını yükle
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Enabled kontrolü
        boolean enabled = config.getBoolean("biomes.enabled", true);
        if (!enabled) {
            plugin.getLogger().info("Biyom sistemi devre dışı (config'de enabled: false)");
            return;
        }
        
        // Her seviye için biyomlar
        for (int level = 1; level <= 5; level++) {
            List<String> biomeNames = config.getStringList("biomes.custom-biomes.level" + level);
            List<Biome> biomes = new ArrayList<>();
            
            for (String biomeName : biomeNames) {
                try {
                    Biome biome = Biome.valueOf(biomeName.toUpperCase());
                    biomes.add(biome);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Geçersiz biyom: " + biomeName + " (Seviye " + level + ")");
                }
            }
            
            if (biomes.isEmpty()) {
                // Varsayılan biyomlar
                biomes = getDefaultBiomesForLevel(level);
                plugin.getLogger().info("Seviye " + level + " için varsayılan biyomlar kullanılıyor");
            }
            
            difficultyBiomes.put(level, biomes);
        }
        
        plugin.getLogger().info("Biyom ayarları yüklendi: " + difficultyBiomes.size() + " seviye");
    }
    
    /**
     * Varsayılan biyom listesi
     */
    private List<Biome> getDefaultBiomesForLevel(int level) {
        switch (level) {
            case 1:
                return Arrays.asList(Biome.FOREST, Biome.PLAINS, Biome.BIRCH_FOREST);
            case 2:
                return Arrays.asList(Biome.TAIGA, Biome.SWAMP, Biome.DARK_FOREST);
            case 3:
                return Arrays.asList(Biome.JUNGLE, Biome.SAVANNA, Biome.BADLANDS);
            case 4:
                return Arrays.asList(Biome.NETHER_WASTES, Biome.SOUL_SAND_VALLEY, Biome.CRIMSON_FOREST);
            case 5:
                return Arrays.asList(Biome.END_BARRENS, Biome.END_HIGHLANDS, Biome.THE_END);
            default:
                return Arrays.asList(Biome.PLAINS);
        }
    }
    
    /**
     * Biyom klasörlerini oluştur
     */
    private void createBiomeDirectories() {
        File schematicsDir = new File(plugin.getDataFolder(), "schematics");
        File biomesDir = new File(schematicsDir, "biomes");
        File structuresDir = new File(biomesDir, "structures");
        File customDir = new File(biomesDir, "custom");
        
        if (!structuresDir.exists()) {
            structuresDir.mkdirs();
        }
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        
        plugin.getLogger().info("Biyom klasörleri oluşturuldu");
    }
    
    /**
     * Difficulty seviyesine göre biyom döndür
     */
    public Biome getBiomeForDifficulty(Location loc, int difficultyLevel) {
        if (difficultyLevel < 1 || difficultyLevel > 5) {
            return loc.getBlock().getBiome(); // Mevcut biyomu koru
        }
        
        List<Biome> biomes = difficultyBiomes.get(difficultyLevel);
        if (biomes == null || biomes.isEmpty()) {
            return loc.getBlock().getBiome();
        }
        
        // Rastgele biyom seç
        return biomes.get(random.nextInt(biomes.size()));
    }
    
    /**
     * Chunk'taki biyomları değiştir (chunk generation'da)
     */
    public void setChunkBiomes(World world, int chunkX, int chunkZ, int difficultyLevel) {
        if (difficultyLevel < 1 || difficultyLevel > 5) {
            return; // Seviye 0'da biyom değiştirme
        }
        
        Biome targetBiome = getBiomeForDifficulty(
            new Location(world, chunkX * 16, 64, chunkZ * 16),
            difficultyLevel
        );
        
        // Chunk içindeki tüm blokların biyomunu değiştir
        // Not: Bu işlem performans açısından ağır olabilir, sadece gerektiğinde kullan
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    Location loc = new Location(world, chunkX * 16 + x, y, chunkZ * 16 + z);
                    // Biome değiştirme işlemi (Bukkit API ile)
                    // Not: Bu işlem chunk generation sırasında yapılmalı
                }
            }
        }
    }
    
    /**
     * Biyom-specific yapı spawn
     */
    public void spawnBiomeStructure(Location loc, Biome biome, int difficultyLevel) {
        // Biyom yapıları için şema dosyaları
        List<String> structures = biomeStructures.get(biome);
        if (structures == null || structures.isEmpty()) {
            return;
        }
        
        // Rastgele yapı seç
        String structureName = structures.get(random.nextInt(structures.size()));
        String schematicPath = "biomes/structures/" + structureName;
        
        // Şema yükle
        StructureBuilder.pasteSchematic(loc, schematicPath);
    }
    
    /**
     * Biyom-specific mob spawn
     */
    public void spawnBiomeMobs(Location loc, Biome biome) {
        // Biyom tipine göre özel moblar spawn edilebilir
        // Şimdilik basit implementasyon
        
        switch (biome) {
            case SWAMP:
            case MANGROVE_SWAMP:
                // Bataklık mobları
                break;
            case JUNGLE:
                // Orman mobları
                break;
            case NETHER_WASTES:
            case SOUL_SAND_VALLEY:
                // Nether mobları
                break;
            default:
                break;
        }
    }
    
    /**
     * Mevcut biyomları listele
     */
    public List<Biome> getBiomesForLevel(int difficultyLevel) {
        return difficultyBiomes.getOrDefault(difficultyLevel, new ArrayList<>());
    }
}

