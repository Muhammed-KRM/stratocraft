package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Merkezden uzaklaştıkça zorlaşan dünya sistemi yöneticisi
 * - Merkez noktası (spawn) yönetimi
 * - Uzaklık hesaplama
 * - Zorluk seviyesi belirleme
 * - Uzaklığa göre mob ve maden spawn kontrolü
 */
public class DifficultyManager {
    private final Main plugin;
    private final ConfigManager configManager;
    private Location centerLocation;
    private World world;

    // Zorluk seviyeleri (blok cinsinden)
    private int level1Distance = 1000; // Seviye 1: Yeni başlangıç mobları (200-1000 blok)
    private int level2Distance = 3000; // Seviye 2: Ork seviyesi (1000-3000 blok)
    private int level3Distance = 5000; // Seviye 3: Güçlü canavarlar (3000-5000 blok)
    private int level4Distance = 10000; // Seviye 4: Ejder seviyesi (5000-10000 blok)
    private int level5Distance = 20000; // Seviye 5: En zor seviye (10000+ blok)

    public DifficultyManager(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        loadCenterLocation();
    }

    /**
     * Merkez noktasını config'den yükle veya spawn noktasını kullan
     */
    private void loadCenterLocation() {
        // Config'den merkez noktasını al
        if (configManager.getConfig().contains("world.center.x")) {
            double x = configManager.getConfig().getDouble("world.center.x");
            double y = configManager.getConfig().getDouble("world.center.y", 64);
            double z = configManager.getConfig().getDouble("world.center.z");
            String worldName = configManager.getConfig().getString("world.center.world", "world");

            World centerWorld = plugin.getServer().getWorld(worldName);
            if (centerWorld != null) {
                this.centerLocation = new Location(centerWorld, x, y, z);
                this.world = centerWorld;
            } else {
                // Dünya bulunamadı, spawn noktasını kullan
                useSpawnAsCenter();
            }
        } else {
            // Config'de yok, spawn noktasını kullan
            useSpawnAsCenter();
        }

        // Zorluk seviyelerini config'den yükle
        level1Distance = configManager.getConfig().getInt("world.difficulty.level1-distance", 200);
        level2Distance = configManager.getConfig().getInt("world.difficulty.level2-distance", 1000);
        level3Distance = configManager.getConfig().getInt("world.difficulty.level3-distance", 3000);
        level4Distance = configManager.getConfig().getInt("world.difficulty.level4-distance", 5000);
        level5Distance = configManager.getConfig().getInt("world.difficulty.level5-distance", 10000);
    }

    /**
     * Spawn noktasını merkez olarak kullan
     */
    private void useSpawnAsCenter() {
        World defaultWorld = plugin.getServer().getWorlds().get(0);
        if (defaultWorld != null) {
            this.centerLocation = defaultWorld.getSpawnLocation();
            this.world = defaultWorld;
            // Config'e kaydet
            saveCenterToConfig();
        }
    }

    /**
     * Merkez noktasını config'e kaydet
     */
    private void saveCenterToConfig() {
        if (centerLocation != null) {
            configManager.getConfig().set("world.center.x", centerLocation.getX());
            configManager.getConfig().set("world.center.y", centerLocation.getY());
            configManager.getConfig().set("world.center.z", centerLocation.getZ());
            configManager.getConfig().set("world.center.world", centerLocation.getWorld().getName());
            plugin.saveConfig();
        }
    }

    /**
     * Belirli bir konumun merkezden uzaklığını hesapla
     */
    public double getDistanceFromCenter(Location loc) {
        if (centerLocation == null || loc == null) {
            return 0;
        }

        // Farklı dünyalarda ise 0 döndür
        if (!centerLocation.getWorld().equals(loc.getWorld())) {
            return 0;
        }

        // 2D uzaklık (X ve Z eksenleri)
        double dx = loc.getX() - centerLocation.getX();
        double dz = loc.getZ() - centerLocation.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Belirli bir konumun zorluk seviyesini döndür (1-5)
     */
    public int getDifficultyLevel(Location loc) {
        double distance = getDistanceFromCenter(loc);

        // 200 blok içinde başlangıç alanı (normal moblar)
        if (distance < 200) {
            return 0; // Başlangıç alanı - normal moblar
        } else if (distance < level1Distance) {
            return 1; // Seviye 1: Yeni başlangıç mobları
        } else if (distance < level2Distance) {
            return 2; // Seviye 2: Ork seviyesi
        } else if (distance < level3Distance) {
            return 3; // Seviye 3: Güçlü canavarlar
        } else if (distance < level4Distance) {
            return 4; // Seviye 4: Ejder seviyesi
        } else {
            return 5; // Seviye 5: En zor seviye
        }
    }

    /**
     * Zorluk seviyesine göre isim döndür
     */
    public String getDifficultyName(int level) {
        switch (level) {
            case 0:
                return "Başlangıç Alanı";
            case 1:
                return "Başlangıç";
            case 2:
                return "Orta";
            case 3:
                return "Zor";
            case 4:
                return "Çok Zor";
            case 5:
                return "Efsanevi";
            default:
                return "Bilinmeyen";
        }
    }

    /**
     * Belirli bir zorluk seviyesinde hangi mobların spawn olabileceğini kontrol et
     * 
     * @param level   Zorluk seviyesi (1-5)
     * @param mobName Mob ismi
     * @return Bu mob bu seviyede spawn olabilir mi?
     */
    public boolean canSpawnMobAtLevel(int level, String mobName) {
        switch (mobName.toLowerCase()) {
            // Seviye 1 (0-200 blok): Goblin
            case "goblin":
                return level == 1;

            // Seviye 2 (200-1000 blok): Ork, Troll, SkeletonKnight, DarkMage, Werewolf,
            // GiantSpider, Minotaur, Harpy, Basilisk
            case "ork":
            case "troll":
            case "skeleton_knight":
            case "dark_mage":
            case "werewolf":
            case "giant_spider":
            case "minotaur":
            case "harpy":
            case "basilisk":
                return level == 2;

            // Seviye 3 (1000-3000 blok): T-Rex, Cyclops, Griffin, Wraith, Lich, Kraken,
            // Phoenix, Behemoth
            case "trex":
            case "t_rex":
            case "cyclops":
            case "griffin":
            case "wraith":
            case "lich":
            case "kraken":
            case "phoenix":
            case "behemoth":
                return level == 3;

            // Seviye 4 (3000-5000 blok): Dragon, Wyvern, HellDragon, TerrorWorm, WarBear,
            // ShadowPanther
            case "dragon":
            case "ejderha":
            case "wyvern":
            case "hell_dragon":
            case "ejder":
            case "terror_worm":
            case "solucan":
            case "war_bear":
            case "savas_ayisi":
            case "shadow_panther":
            case "panter":
                return level == 4;

            // Seviye 5 (5000+ blok): TitanGolem, Hydra, VoidWorm (en güçlüler)
            case "titan_golem":
            case "hydra":
            case "void_worm":
            case "hiclik_solucani":
                return level >= 5;

            // ========== YENİ SEVİYE 1 MOBLAR ==========
            case "wild_boar":
            case "yaban_domuzu":
            case "wolf_pack":
            case "kurt_surusu":
            case "snake":
            case "yilan":
            case "eagle":
            case "kartal":
            case "bear":
            case "ayi":
                return level == 1;

            // ========== YENİ SEVİYE 2 MOBLAR ==========
            case "iron_golem":
            case "demir_golem":
            case "ice_dragon":
            case "buz_ejderi":
            case "fire_serpent":
            case "ates_yilani":
            case "earth_giant":
            case "toprak_dev":
            case "soul_hunter":
            case "ruh_avcisi":
                return level == 2;

            // ========== YENİ SEVİYE 3 MOBLAR ==========
            case "shadow_dragon":
            case "golge_ejderi":
            case "light_dragon":
            case "isik_ejderi":
            case "storm_giant":
            case "firtina_dev":
            case "lava_dragon":
            case "lav_ejderi":
            case "ice_giant":
            case "buz_dev":
                return level == 3;

            // ========== YENİ SEVİYE 4 MOBLAR ==========
            case "red_devil":
            case "kizil_seytan":
            case "black_dragon":
            case "kara_ejder":
            case "death_knight":
            case "olum_sovalyesi":
            case "chaos_dragon":
            case "kaos_ejderi":
            case "hell_devil":
            case "cehennem_seytani":
                return level == 4;

            // ========== YENİ SEVİYE 5 MOBLAR ==========
            case "legendary_dragon":
            case "efsanevi_ejder":
            case "god_slayer":
            case "tanri_katili":
            case "void_creature":
            case "hiclik_yaratigi":
            case "time_dragon":
            case "zaman_ejderi":
            case "fate_creature":
            case "kader_yaratigi":
                return level >= 5;

            default:
                return false;
        }
    }

    /**
     * Belirli bir zorluk seviyesinde hangi madenlerin spawn olabileceğini kontrol
     * et
     * 
     * @param level   Zorluk seviyesi (1-5)
     * @param oreName Maden ismi
     * @return Bu maden bu seviyede spawn olabilir mi?
     */
    public boolean canSpawnOreAtLevel(int level, String oreName) {
        switch (oreName.toUpperCase()) {
            // Seviye 1: Sadece normal madenler (Kükürt, Boksit, Tuz Kayası)
            case "SULFUR":
            case "BAUXITE":
            case "ROCK_SALT":
                return level >= 1;

            // Seviye 2: Titanyum başlar
            case "TITANIUM":
                return level >= 2;

            // Seviye 3: Mithril başlar
            case "MITHRIL":
                return level >= 3;

            // Seviye 4: Astral başlar
            case "ASTRAL":
            case "ASTRAL_ORE":
                return level >= 4;

            // Seviye 5: Kızıl Elmas (en nadir)
            case "RED_DIAMOND":
                return level >= 5;

            default:
                return false;
        }
    }

    /**
     * Merkez noktasını manuel olarak ayarla (admin komutu için)
     */
    public void setCenterLocation(Location loc) {
        this.centerLocation = loc.clone();
        this.world = loc.getWorld();
        saveCenterToConfig();
    }

    /**
     * Merkez noktasını al
     */
    public Location getCenterLocation() {
        return centerLocation != null ? centerLocation.clone() : null;
    }

    /**
     * Merkez noktasını yeniden yükle (config değiştiğinde)
     */
    public void reload() {
        loadCenterLocation();
    }

    // Getter'lar
    public int getLevel1Distance() {
        return level1Distance;
    }

    public int getLevel2Distance() {
        return level2Distance;
    }

    public int getLevel3Distance() {
        return level3Distance;
    }

    public int getLevel4Distance() {
        return level4Distance;
    }

    public int getLevel5Distance() {
        return level5Distance;
    }
}
