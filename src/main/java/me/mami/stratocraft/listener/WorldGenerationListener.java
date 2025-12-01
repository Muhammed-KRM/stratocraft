package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BossManager;
import me.mami.stratocraft.manager.DifficultyManager;
import me.mami.stratocraft.manager.DungeonManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MobManager;
import me.mami.stratocraft.manager.TerritoryManager;
import org.bukkit.block.Biome;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Random;

/**
 * Dünya oluşturma ve doğal spawn olaylarını yönetir:
 * - Özel madenlerin chunk yüklendiğinde oluşturulması (uzaklığa göre)
 * - Doğal yapıların (ruins, outposts) spawn edilmesi
 * - Doğal mobların spawn edilmesi (uzaklığa göre zorluk sistemi)
 */
public class WorldGenerationListener implements Listener {
    private final TerritoryManager territoryManager;
    private final MobManager mobManager;
    private final DifficultyManager difficultyManager;
    private final DungeonManager dungeonManager;
    private final BossManager bossManager;
    private final Random random = new Random();
    
    public WorldGenerationListener(TerritoryManager tm, MobManager mm, DifficultyManager dm, 
                                   DungeonManager dungeonMgr, BossManager bossMgr) {
        this.territoryManager = tm;
        this.mobManager = mm;
        this.difficultyManager = dm;
        this.dungeonManager = dungeonMgr;
        this.bossManager = bossMgr;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // Sadece yeni chunk'lar için (ilk yükleme)
        if (!event.isNewChunk()) return;
        
        Chunk chunk = event.getChunk();
        if (chunk == null) return;
        
        World world = chunk.getWorld();
        if (world == null) return;
        
        // Chunk merkezini hesapla
        Location chunkCenter = new Location(world, chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
        
        // Merkezden uzaklığı kontrol et
        double distance = difficultyManager.getDistanceFromCenter(chunkCenter);
        int difficultyLevel = difficultyManager.getDifficultyLevel(chunkCenter);
        
        // Merkezden çok yakınsa (200 blok içinde) normal spawn koruması - maden yok
        if (distance < 200) {
            return; // Başlangıç alanı - özel maden yok
        }
        
        // Chunk yükleme işlemlerini async yapma, sync yap (blok değişiklikleri için)
        // Özel madenler oluştur (%30 şans) - uzaklığa göre
        if (random.nextDouble() < 0.3) {
            generateOres(chunk, difficultyLevel);
        }
        
        // Doğal yapılar oluştur (%0.5 şans - çok nadir)
        if (random.nextDouble() < 0.005) {
            generateNaturalStructure(chunk);
        }
        
        // Boss spawn kontrolü (difficulty seviyesi 1-5 arası, çok nadir)
        if (difficultyLevel >= 1 && difficultyLevel <= 5 && bossManager != null) {
            try {
                Location bossSpawnLoc = chunkCenter.clone();
                bossSpawnLoc.setY(world.getHighestBlockYAt(bossSpawnLoc) + 1);
                bossManager.trySpawnBossInNature(bossSpawnLoc, difficultyLevel);
            } catch (Exception e) {
                // Boss spawn hatası - logla ama devam et
                me.mami.stratocraft.Main.getInstance().getLogger().fine("Boss spawn hatası: " + e.getMessage());
            }
        }
        
        // Zindan spawn kontrolü (difficulty seviyesi 1-5 arası)
        if (difficultyLevel >= 1 && difficultyLevel <= 5 && dungeonManager != null) {
            try {
                if (dungeonManager.shouldSpawnDungeon(chunkCenter, difficultyLevel)) {
                    dungeonManager.spawnDungeon(chunkCenter, difficultyLevel);
                }
            } catch (Exception e) {
                // Zindan spawn hatası - logla ama devam et
                me.mami.stratocraft.Main.getInstance().getLogger().warning("Zindan spawn hatası: " + e.getMessage());
            }
        }
    }
    
    /**
     * Chunk içinde özel madenler oluşturur (uzaklığa göre zorluk seviyesine bağlı)
     */
    private void generateOres(Chunk chunk, int difficultyLevel) {
        int oreCount = random.nextInt(3) + 1; // 1-3 maden
        
        for (int i = 0; i < oreCount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = random.nextInt(44) - 64; // -64 ile -20 arası
            
            Block block = chunk.getBlock(x, y, z);
            
            // Sadece taş veya deepslate üzerine
            if (block.getType() != Material.STONE && 
                block.getType() != Material.DEEPSLATE &&
                block.getType() != Material.DEEPSLATE_COAL_ORE &&
                block.getType() != Material.DEEPSLATE_IRON_ORE) {
                continue;
            }
            
            // Yeni maden sistemi - Uzaklığa göre zorluk seviyesi
            double rand = random.nextDouble();
            Biome biome = block.getBiome();
            
            // Seviye 1 madenler (Kükürt, Boksit, Tuz Kayası)
            if (difficultyLevel >= 1) {
                if (rand < 0.25 && difficultyManager.canSpawnOreAtLevel(difficultyLevel, "SULFUR")) {
                    block.setType(Material.YELLOW_CONCRETE_POWDER);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "SULFUR"));
                    continue;
                } else if (rand < 0.45 && difficultyManager.canSpawnOreAtLevel(difficultyLevel, "BAUXITE")) {
                    block.setType(Material.ORANGE_CONCRETE_POWDER);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "BAUXITE"));
                    continue;
                } else if (rand < 0.60 && difficultyManager.canSpawnOreAtLevel(difficultyLevel, "ROCK_SALT")) {
                    block.setType(Material.QUARTZ_BLOCK);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "ROCK_SALT"));
                    continue;
                }
            }
            
            // Seviye 2 madenler (Titanyum)
            if (difficultyLevel >= 2 && rand < 0.70 && y <= -40) {
                if (difficultyManager.canSpawnOreAtLevel(difficultyLevel, "TITANIUM")) {
                    block.setType(Material.ANCIENT_DEBRIS);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "TITANIUM"));
                    continue;
                }
            }
            
            // Seviye 3 madenler (Mithril)
            if (difficultyLevel >= 3 && rand < 0.85) {
                if ((biome == org.bukkit.block.Biome.JAGGED_PEAKS || 
                     biome == org.bukkit.block.Biome.FROZEN_PEAKS ||
                     biome == org.bukkit.block.Biome.STONY_PEAKS) &&
                    difficultyManager.canSpawnOreAtLevel(difficultyLevel, "MITHRIL")) {
                    block.setType(Material.LIGHT_BLUE_CONCRETE_POWDER);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "MITHRIL"));
                    continue;
                }
            }
            
            // Seviye 4 madenler (Astral)
            if (difficultyLevel >= 4 && rand < 0.95 && y <= -60) {
                if (difficultyManager.canSpawnOreAtLevel(difficultyLevel, "ASTRAL")) {
                    block.setType(Material.AMETHYST_BLOCK);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "ASTRAL"));
                    continue;
                }
            }
            
            // Seviye 5 madenler (Kızıl Elmas - en nadir)
            if (difficultyLevel >= 5 && rand >= 0.95 && y <= -60) {
                if (difficultyManager.canSpawnOreAtLevel(difficultyLevel, "RED_DIAMOND")) {
                    block.setType(Material.DEEPSLATE_DIAMOND_ORE);
                    block.setMetadata("OreType", new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), "RED_DIAMOND"));
                }
            }
        }
    }
    
    /**
     * Doğal yapılar (ruins, outposts) oluşturur
     */
    private void generateNaturalStructure(Chunk chunk) {
        World world = chunk.getWorld();
        int centerX = chunk.getX() * 16 + random.nextInt(16);
        int centerZ = chunk.getZ() * 16 + random.nextInt(16);
        int surfaceY = world.getHighestBlockYAt(centerX, centerZ);
        
        Location structureLoc = new Location(world, centerX, surfaceY, centerZ);
        
        // Klan bölgesinde değilse yapı oluştur
        if (territoryManager.getTerritoryOwner(structureLoc) != null) {
            return;
        }
        
        // Basit bir "Terk Edilmiş Karakol" yapısı oluştur
        // 5x5x3 boyutunda taş yapı
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y < 3; y++) {
                    Block block = structureLoc.clone().add(x, y, z).getBlock();
                    
                    // Sadece hava veya çimen üzerine
                    if (block.getType() == Material.AIR || 
                        block.getType() == Material.GRASS_BLOCK ||
                        block.getType() == Material.TALL_GRASS) {
                        
                        // Duvarlar ve köşeler
                        if (x == -2 || x == 2 || z == -2 || z == 2 || y == 2) {
                            block.setType(Material.COBBLESTONE);
                        } else if (y == 0) {
                            block.setType(Material.STONE_BRICKS);
                        }
                    }
                }
            }
        }
        
        // İçine sandık koy (%50 şans)
        if (random.nextDouble() < 0.5) {
            Block chestBlock = structureLoc.clone().add(0, 1, 0).getBlock();
            if (chestBlock.getType() == Material.AIR) {
                chestBlock.setType(Material.CHEST);
            }
        }
    }
    
    /**
     * Doğal mob spawn'larını değiştirir (uzaklığa göre zorluk sistemi)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Sadece doğal spawn'ları kontrol et
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        
        Location loc = event.getLocation();
        Biome biome = loc.getBlock().getBiome();
        EntityType entityType = event.getEntityType();
        double rand = random.nextDouble();
        
        // Zorluk seviyesini hesapla
        int difficultyLevel = difficultyManager.getDifficultyLevel(loc);
        
        // Seviye 0 (0-200 blok): Başlangıç alanı - normal moblar
        if (difficultyLevel == 0) {
            return; // Başlangıç alanı - sadece normal moblar
        }
        
        // ========== SEVİYE 1 YENİ MOBLAR (200-1000 blok) ==========
        // Not: Seviye 1 artık özel moblar içeriyor (200-1000 blok arası)
        if (difficultyLevel == 1) {
            // PIG tabanlı - Yaban Domuzu
            if (entityType == EntityType.PIG && rand < 0.25) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "wild_boar")) {
                    event.setCancelled(true);
                    mobManager.spawnWildBoar(loc);
                    return;
                }
            }
            
            // WOLF tabanlı - Kurt Sürüsü
            if (entityType == EntityType.WOLF && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "wolf_pack")) {
                    event.setCancelled(true);
                    mobManager.spawnWolfPack(loc);
                    return;
                }
            }
            
            // SILVERFISH tabanlı - Yılan
            if (entityType == EntityType.SILVERFISH && rand < 0.18) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "snake")) {
                    event.setCancelled(true);
                    mobManager.spawnSnake(loc);
                    return;
                }
            }
            
            // PARROT tabanlı - Kartal
            if (entityType == EntityType.PARROT && rand < 0.15) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "eagle")) {
                    event.setCancelled(true);
                    mobManager.spawnEagle(loc);
                    return;
                }
            }
            
            // POLAR_BEAR tabanlı - Ayı
            if (entityType == EntityType.POLAR_BEAR && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "bear")) {
                    event.setCancelled(true);
                    mobManager.spawnBear(loc);
                    return;
                }
            }
            
            // Diğer seviye 1 mobları (Goblin vb.) için normal akış devam eder
            return;
        }
        
        // ========== SEVİYE 2 CANAVARLAR (1000-3000 blok): Ork, Troll, SkeletonKnight, DarkMage, Werewolf, GiantSpider, Minotaur, Harpy, Basilisk + Yeni Moblar ==========
        
        if (difficultyLevel == 2) {
            // YENİ SEVİYE 2 MOBLAR - Öncelikli spawn (daha sık)
            
            // IRON_GOLEM tabanlı - Demir Golem
            if (entityType == EntityType.IRON_GOLEM && rand < 0.30) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "iron_golem")) {
                    event.setCancelled(true);
                    mobManager.spawnIronGolem(loc);
                    return;
                }
            }
            
            // PHANTOM tabanlı - Buz Ejderi
            if (entityType == EntityType.PHANTOM && rand < 0.25) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "ice_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnIceDragon(loc);
                    return;
                }
            }
            
            // BLAZE tabanlı - Ateş Yılanı
            if (entityType == EntityType.BLAZE && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "fire_serpent")) {
                    event.setCancelled(true);
                    mobManager.spawnFireSerpent(loc);
                    return;
                }
            }
            
            // GIANT tabanlı - Toprak Dev
            if (entityType == EntityType.GIANT && rand < 0.30) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "earth_giant")) {
                    event.setCancelled(true);
                    mobManager.spawnEarthGiant(loc);
                    return;
                }
            }
            
            // VEX tabanlı - Ruh Avcısı
            if (entityType == EntityType.VEX && rand < 0.25) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "soul_hunter")) {
                    event.setCancelled(true);
                    mobManager.spawnSoulHunter(loc);
                    return;
                }
            }
            
            // ESKİ SEVİYE 2 MOBLAR
            // ZOMBIE tabanlı canavarlar
            if (entityType == EntityType.ZOMBIE) {
                // TROLL - Ormanlarda öncelikli (%20)
                if ((biome == Biome.FOREST || biome == Biome.DARK_FOREST || biome == Biome.TAIGA) && rand < 0.20) {
                    if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "troll")) {
                        event.setCancelled(true);
                        mobManager.spawnTroll(loc);
                        return;
                    }
                }
                // ORK - Her yerde, sık (%25)
                else if (rand < 0.45) {
                    if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "ork")) {
                        event.setCancelled(true);
                        mobManager.spawnOrk(loc);
                        return;
                    }
                }
            }
            
            // SKELETON tabanlı canavarlar
            if (entityType == EntityType.SKELETON && rand < 0.15) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "skeleton_knight")) {
                    event.setCancelled(true);
                    mobManager.spawnSkeletonKnight(loc);
                    return;
                }
            }
            
            // KARANLIK BÜYÜCÜ
            if (entityType == EntityType.WITCH && rand < 0.15) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "dark_mage")) {
                    event.setCancelled(true);
                    mobManager.spawnDarkMage(loc);
                    return;
                }
            }
            
            // KURT ADAM
            if ((biome == Biome.FOREST || biome == Biome.DARK_FOREST) && 
                entityType == EntityType.WOLF && rand < 0.18) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "werewolf")) {
                    event.setCancelled(true);
                    mobManager.spawnWerewolf(loc);
                    return;
                }
            }
            
            // DEV ÖRÜMCEK
            if (loc.getY() < 50 && entityType == EntityType.SPIDER && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "giant_spider")) {
                    event.setCancelled(true);
                    mobManager.spawnGiantSpider(loc);
                    return;
                }
            }
            
            // MİNOTAUR
            if ((biome == Biome.PLAINS || biome == Biome.SAVANNA) && 
                entityType == EntityType.COW && rand < 0.15) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "minotaur")) {
                    event.setCancelled(true);
                    mobManager.spawnMinotaur(loc);
                    return;
                }
            }
            
            // HARPY
            if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS) && 
                entityType == EntityType.PARROT && rand < 0.18) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "harpy")) {
                    event.setCancelled(true);
                    mobManager.spawnHarpy(loc);
                    return;
                }
            }
            
            // BASILISK
            if (loc.getY() < 0 && entityType == EntityType.SILVERFISH && rand < 0.18) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "basilisk")) {
                    event.setCancelled(true);
                    mobManager.spawnBasilisk(loc);
                    return;
                }
            }
        }
        
        // ========== SEVİYE 3 CANAVARLAR (3000-5000 blok): T-Rex, Cyclops, Griffin, Wraith, Lich, Kraken, Phoenix, Behemoth + Yeni Moblar ==========
        
        if (difficultyLevel == 3) {
            // YENİ SEVİYE 3 MOBLAR - Öncelikli spawn
            
            // PHANTOM tabanlı - Gölge Ejderi
            if (entityType == EntityType.PHANTOM && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "shadow_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnShadowDragon(loc);
                    return;
                }
            }
            
            // PHANTOM tabanlı - Işık Ejderi (Gölge'den sonra)
            if (entityType == EntityType.PHANTOM && rand >= 0.20 && rand < 0.40) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "light_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnLightDragon(loc);
                    return;
                }
            }
            
            // GIANT tabanlı - Fırtına Dev
            if (entityType == EntityType.GIANT && rand < 0.25) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "storm_giant")) {
                    event.setCancelled(true);
                    mobManager.spawnStormGiant(loc);
                    return;
                }
            }
            
            // PHANTOM tabanlı - Lav Ejderi
            if (entityType == EntityType.PHANTOM && rand >= 0.40 && rand < 0.60) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "lava_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnLavaDragon(loc);
                    return;
                }
            }
            
            // GIANT tabanlı - Buz Dev
            if (entityType == EntityType.GIANT && rand >= 0.25 && rand < 0.50) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "ice_giant")) {
                    event.setCancelled(true);
                    mobManager.spawnIceGiant(loc);
                    return;
                }
            }
            
            // ESKİ SEVİYE 3 MOBLAR
            // T-REX - Ovalarda
            if ((biome == Biome.PLAINS || biome == Biome.SAVANNA) && 
                entityType == EntityType.RAVAGER && rand < 0.008) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "trex")) {
                    event.setCancelled(true);
                    mobManager.spawnTRex(loc);
                    return;
                }
            }
            
            // CYCLOPS - Dağlarda
            if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS) && 
                entityType == EntityType.GIANT && rand < 0.02) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "cyclops")) {
                    event.setCancelled(true);
                    mobManager.spawnCyclops(loc);
                    return;
                }
            }
            
            // GRIFFIN - Yüksek yerlerde
            if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS) && 
                entityType == EntityType.PHANTOM && rand >= 0.008 && rand < 0.015) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "griffin")) {
                    event.setCancelled(true);
                    mobManager.spawnGriffin(loc);
                    return;
                }
            }
            
            // WRAITH - Gece
            if (loc.getWorld().getTime() > 13000 && entityType == EntityType.VEX && rand < 0.02) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "wraith")) {
                    event.setCancelled(true);
                    mobManager.spawnWraith(loc);
                    return;
                }
            }
            
            // LICH - Mağaralarda
            if (loc.getY() < 0 && entityType == EntityType.SKELETON && rand < 0.025) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "lich")) {
                    event.setCancelled(true);
                    mobManager.spawnLich(loc);
                    return;
                }
            }
            
            // KRAKEN - Su biyomlarında
            if ((biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.RIVER) && 
                entityType == EntityType.SQUID && rand < 0.02) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "kraken")) {
                    event.setCancelled(true);
                    mobManager.spawnKraken(loc);
                    return;
                }
            }
            
            // PHOENIX - Çöl veya kuru biyomlarda
            if ((biome == Biome.DESERT || biome == Biome.BADLANDS || biome == Biome.ERODED_BADLANDS) && 
                entityType == EntityType.BLAZE && rand < 0.015) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "phoenix")) {
                    event.setCancelled(true);
                    mobManager.spawnPhoenix(loc);
                    return;
                }
            }
            
            // BEHEMOTH
            if (entityType == EntityType.RAVAGER && rand >= 0.008 && rand < 0.018) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "behemoth")) {
                    event.setCancelled(true);
                    mobManager.spawnBehemoth(loc);
                    return;
                }
            }
        }
        
        // ========== SEVİYE 4 CANAVARLAR (5000+ blok): Dragon, Wyvern, HellDragon, TerrorWorm, WarBear, ShadowPanther + Yeni Moblar ==========
        
        if (difficultyLevel == 4) {
            // YENİ SEVİYE 4 MOBLAR - Öncelikli spawn (çok sık)
            
            // BLAZE tabanlı - Kızıl Şeytan (en sık)
            if (entityType == EntityType.BLAZE && rand < 0.35) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "red_devil")) {
                    event.setCancelled(true);
                    mobManager.spawnRedDevil(loc);
                    return;
                }
            }
            
            // PHANTOM tabanlı - Kara Ejder
            if (entityType == EntityType.PHANTOM && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "black_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnBlackDragon(loc);
                    return;
                }
            }
            
            // SKELETON tabanlı - Ölüm Şövalyesi
            if (entityType == EntityType.SKELETON && rand < 0.25) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "death_knight")) {
                    event.setCancelled(true);
                    mobManager.spawnDeathKnight(loc);
                    return;
                }
            }
            
            // PHANTOM tabanlı - Kaos Ejderi
            if (entityType == EntityType.PHANTOM && rand >= 0.20 && rand < 0.40) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "chaos_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnChaosDragon(loc);
                    return;
                }
            }
            
            // BLAZE tabanlı - Cehennem Şeytanı
            if (entityType == EntityType.BLAZE && rand >= 0.35 && rand < 0.70) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "hell_devil")) {
                    event.setCancelled(true);
                    mobManager.spawnHellDevil(loc);
                    return;
                }
            }
            
            // ESKİ SEVİYE 4 MOBLAR
            // EJDERHA - Her yerde, nadir
            if (entityType == EntityType.PHANTOM && rand < 0.01) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnDragon(loc);
                    return;
                }
            }
            
            // WYVERN - Dağ biyomlarında
            if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS || 
                 biome == Biome.STONY_PEAKS) && entityType == EntityType.PHANTOM && rand >= 0.01 && rand < 0.08) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "wyvern")) {
                    event.setCancelled(true);
                    mobManager.spawnWyvern(loc, null);
                    return;
                }
            }
            
            // HELL DRAGON - Çok nadir
            if (entityType == EntityType.PHANTOM && rand >= 0.08 && rand < 0.012) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "hell_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnHellDragon(loc, null);
                    return;
                }
            }
            
            // TERROR WORM - Yer altında
            if (loc.getY() < 0 && entityType == EntityType.SILVERFISH && rand < 0.015) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "terror_worm")) {
                    event.setCancelled(true);
                    mobManager.spawnTerrorWorm(loc, null);
                    return;
                }
            }
            
            // WAR BEAR - Ormanlarda
            if ((biome == Biome.FOREST || biome == Biome.DARK_FOREST || biome == Biome.TAIGA) && 
                entityType == EntityType.POLAR_BEAR && rand < 0.02) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "war_bear")) {
                    event.setCancelled(true);
                    mobManager.spawnWarBear(loc, null);
                    return;
                }
            }
            
            // SHADOW PANTHER - Gece, ormanlarda
            if (loc.getWorld().getTime() > 13000 && 
                (biome == Biome.FOREST || biome == Biome.DARK_FOREST) && 
                entityType == EntityType.CAT && rand < 0.02) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "shadow_panther")) {
                    event.setCancelled(true);
                    mobManager.spawnShadowPanther(loc, null);
                    return;
                }
            }
        }
        
        // ========== SEVİYE 5 CANAVARLAR (Efsanevi): TitanGolem, Hydra, VoidWorm + Yeni Moblar ==========
        
        if (difficultyLevel >= 5) {
            // YENİ SEVİYE 5 MOBLAR - Çok nadir ama güçlü
            
            // PHANTOM tabanlı - Efsanevi Ejder
            if (entityType == EntityType.PHANTOM && rand < 0.15) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "legendary_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnLegendaryDragon(loc);
                    return;
                }
            }
            
            // GIANT tabanlı - Tanrı Katili
            if (entityType == EntityType.GIANT && rand < 0.12) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "god_slayer")) {
                    event.setCancelled(true);
                    mobManager.spawnGodSlayer(loc);
                    return;
                }
            }
            
            // VEX tabanlı - Hiçlik Yaratığı
            if (entityType == EntityType.VEX && rand < 0.10) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "void_creature")) {
                    event.setCancelled(true);
                    mobManager.spawnVoidCreature(loc);
                    return;
                }
            }
            
            // PHANTOM tabanlı - Zaman Ejderi
            if (entityType == EntityType.PHANTOM && rand >= 0.15 && rand < 0.30) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "time_dragon")) {
                    event.setCancelled(true);
                    mobManager.spawnTimeDragon(loc);
                    return;
                }
            }
            
            // VEX tabanlı - Kader Yaratığı
            if (entityType == EntityType.VEX && rand >= 0.10 && rand < 0.20) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "fate_creature")) {
                    event.setCancelled(true);
                    mobManager.spawnFateCreature(loc);
                    return;
                }
            }
            
            // ESKİ SEVİYE 5 MOBLAR
            // TITAN GOLEM - Çok nadir
            if (entityType == EntityType.IRON_GOLEM && rand < 0.015) {
                if (difficultyManager.canSpawnMobAtLevel(difficultyLevel, "titan_golem")) {
                    event.setCancelled(true);
                    mobManager.spawnTitanGolem(loc, null);
                    return;
                }
            }
            
            // HYDRA - Çok nadir (sadece admin komutuyla spawn edilebilir, doğal spawn yok)
            // Not: Hydra normal dünyada spawn edilemez, bu yüzden bu spawn'ı kaldırıyoruz
        }
    }
}

