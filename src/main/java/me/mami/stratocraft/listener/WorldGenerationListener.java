package me.mami.stratocraft.listener;

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
 * - Özel madenlerin chunk yüklendiğinde oluşturulması
 * - Doğal yapıların (ruins, outposts) spawn edilmesi
 * - Doğal mobların (Wyvern, Taş Golem) spawn edilmesi
 */
public class WorldGenerationListener implements Listener {
    private final TerritoryManager territoryManager;
    private final MobManager mobManager;
    private final Random random = new Random();
    
    public WorldGenerationListener(TerritoryManager tm, MobManager mm) {
        this.territoryManager = tm;
        this.mobManager = mm;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // Sadece yeni chunk'lar için (ilk yükleme)
        if (!event.isNewChunk()) return;
        
        Chunk chunk = event.getChunk();
        if (chunk == null) return;
        
        World world = chunk.getWorld();
        if (world == null) return;
        
        // Spawn'dan çok yakınsa atla (spawn koruması)
        Location spawnLoc = world.getSpawnLocation();
        if (spawnLoc == null) return;
        
        if (Math.abs(chunk.getX() * 16 - spawnLoc.getBlockX()) < 200 && 
            Math.abs(chunk.getZ() * 16 - spawnLoc.getBlockZ()) < 200) {
            return;
        }
        
        // Chunk yükleme işlemlerini async yapma, sync yap (blok değişiklikleri için)
        // Özel madenler oluştur (%30 şans)
        if (random.nextDouble() < 0.3) {
            generateOres(chunk);
        }
        
        // Doğal yapılar oluştur (%0.5 şans - çok nadir)
        if (random.nextDouble() < 0.005) {
            generateNaturalStructure(chunk);
        }
    }
    
    /**
     * Chunk içinde özel madenler oluşturur
     */
    private void generateOres(Chunk chunk) {
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
            
            // Titanyum Cevheri (%70) - ANCIENT_DEBRIS olarak işaretle
            if (random.nextDouble() < 0.7) {
                block.setType(Material.ANCIENT_DEBRIS);
                // NBT ile işaretle (SurvivalListener'da kontrol edilecek)
            } else if (y <= -60) {
                // Kızıl Elmas (%30, sadece -60 altında)
                block.setType(Material.DEEPSLATE_DIAMOND_ORE);
                // NBT ile işaretle (SurvivalListener'da kontrol edilecek)
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
     * Doğal mob spawn'larını değiştirir (20 yeni canavar + Wyvern, Taş Golem)
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
        
        // ========== NADİR CANAVARLAR (Önce kontrol et, çünkü daha özel) ==========
        
        // EJDERHA - Her yerde, çok nadir (%0.5)
        if (entityType == EntityType.PHANTOM && rand < 0.005) {
            event.setCancelled(true);
            mobManager.spawnDragon(loc);
            return;
        }
        
        // T-REX - Ovalarda, nadir (%1) - Behemoth'tan önce kontrol et
        if ((biome == Biome.PLAINS || biome == Biome.SAVANNA) && 
            entityType == EntityType.RAVAGER && rand < 0.005) {
            event.setCancelled(true);
            mobManager.spawnTRex(loc);
            return;
        }
        
        // CYCLOPS - Dağlarda, nadir (%1.5)
        if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS) && 
            entityType == EntityType.GIANT && rand < 0.015) {
            event.setCancelled(true);
            mobManager.spawnCyclops(loc);
            return;
        }
        
        // GRIFFIN - Yüksek yerlerde, nadir (%1)
        if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS) && 
            entityType == EntityType.PHANTOM && rand < 0.01 && rand >= 0.005) {
            event.setCancelled(true);
            mobManager.spawnGriffin(loc);
            return;
        }
        
        // WRAITH - Gece, her yerde, nadir (%1.5)
        if (loc.getWorld().getTime() > 13000 && entityType == EntityType.VEX && rand < 0.015) {
            event.setCancelled(true);
            mobManager.spawnWraith(loc);
            return;
        }
        
        // LICH - Mağaralarda, nadir (%2) - SkeletonKnight'tan önce kontrol et
        if (loc.getY() < 0 && entityType == EntityType.SKELETON && rand < 0.02) {
            event.setCancelled(true);
            mobManager.spawnLich(loc);
            return;
        }
        
        // KRAKEN - Su biyomlarında, nadir (%1.5)
        if ((biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.RIVER) && 
            entityType == EntityType.SQUID && rand < 0.015) {
            event.setCancelled(true);
            mobManager.spawnKraken(loc);
            return;
        }
        
        // PHOENIX - Çöl veya kuru biyomlarda, nadir (%1)
        if ((biome == Biome.DESERT || biome == Biome.BADLANDS || biome == Biome.ERODED_BADLANDS) && 
            entityType == EntityType.BLAZE && rand < 0.01) {
            event.setCancelled(true);
            mobManager.spawnPhoenix(loc);
            return;
        }
        
        // HYDRA - Çok nadir, her yerde (EnderDragon yerine başka entity kullan)
        // Not: EnderDragon normal dünyada spawn edilemez, bu yüzden bu spawn'ı kaldırıyoruz
        // Hydra'yı sadece admin komutuyla çağırabilirsiniz
        
        // BEHEMOTH - Her yerde, nadir (%1) - T-Rex'ten sonra
        if (entityType == EntityType.RAVAGER && rand >= 0.005 && rand < 0.015) {
            event.setCancelled(true);
            mobManager.spawnBehemoth(loc);
            return;
        }
        
        // ========== SIK GELEN CANAVARLAR ==========
        
        // ZOMBIE tabanlı canavarlar (Goblin, Ork, Troll)
        if (entityType == EntityType.ZOMBIE) {
            // TROLL - Ormanlarda öncelikli (%15)
            if ((biome == Biome.FOREST || biome == Biome.DARK_FOREST || biome == Biome.TAIGA) && rand < 0.15) {
                event.setCancelled(true);
                mobManager.spawnTroll(loc);
                return;
            }
            // GOBLIN - Her yerde, sık (%15)
            else if (rand < 0.15) {
                event.setCancelled(true);
                mobManager.spawnGoblin(loc);
                return;
            }
            // ORK - Her yerde, sık (%12) - Goblin'den sonra
            else if (rand < 0.27) {
                event.setCancelled(true);
                mobManager.spawnOrk(loc);
                return;
            }
        }
        
        // SKELETON tabanlı canavarlar (SkeletonKnight, Lich)
        // Not: Lich zaten yukarıda kontrol edildi (mağaralarda %2), buraya gelmez
        if (entityType == EntityType.SKELETON) {
            // İSKELET ŞÖVALYE - Her yerde, sık (%10)
            if (rand < 0.10) {
                event.setCancelled(true);
                mobManager.spawnSkeletonKnight(loc);
                return;
            }
        }
        
        // KARANLIK BÜYÜCÜ - Her yerde, sık (%12)
        if (entityType == EntityType.WITCH && rand < 0.12) {
            event.setCancelled(true);
            mobManager.spawnDarkMage(loc);
            return;
        }
        
        // KURT ADAM - Ormanlarda, sık (%15)
        if ((biome == Biome.FOREST || biome == Biome.DARK_FOREST) && 
            entityType == EntityType.WOLF && rand < 0.15) {
            event.setCancelled(true);
            mobManager.spawnWerewolf(loc);
            return;
        }
        
        // DEV ÖRÜMCEK - Mağaralarda, sık (%18)
        if (loc.getY() < 50 && entityType == EntityType.SPIDER && rand < 0.18) {
            event.setCancelled(true);
            mobManager.spawnGiantSpider(loc);
            return;
        }
        
        // MİNOTAUR - Ovalarda, sık (%12)
        if ((biome == Biome.PLAINS || biome == Biome.SAVANNA) && 
            entityType == EntityType.COW && rand < 0.12) {
            event.setCancelled(true);
            mobManager.spawnMinotaur(loc);
            return;
        }
        
        // HARPY - Yüksek yerlerde, sık (%15)
        if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS) && 
            entityType == EntityType.PARROT && rand < 0.15) {
            event.setCancelled(true);
            mobManager.spawnHarpy(loc);
            return;
        }
        
        // BASILISK - Mağaralarda, sık (%15)
        if (loc.getY() < 0 && entityType == EntityType.SILVERFISH && rand < 0.15) {
            event.setCancelled(true);
            mobManager.spawnBasilisk(loc);
            return;
        }
        
        // ========== ESKİ SİSTEM (Wyvern ve Taş Golem) ==========
        
        // Dağ biyomlarında Phantom yerine Wyvern (%5)
        if ((biome == Biome.JAGGED_PEAKS || biome == Biome.FROZEN_PEAKS || 
             biome == Biome.STONY_PEAKS) && entityType == EntityType.PHANTOM) {
            if (rand < 0.05) {
                event.setCancelled(true);
                mobManager.spawnWyvern(loc, null);
            }
        }
        
        // Taşlık biyomlarda Zombie yerine Taş Golem (%3)
        // Not: Bu kontrol Goblin/Ork/Troll kontrollerinden sonra gelir
        // Çünkü biome kontrolü var, çakışma riski düşük
        if ((biome == Biome.STONY_SHORE || biome == Biome.STONY_PEAKS) && 
            entityType == EntityType.ZOMBIE) {
            // Eğer yukarıdaki kontrollerden geçtiyse (Goblin/Ork/Troll spawn olmadıysa)
            // Taş Golem kontrolü yap
            if (rand < 0.03) {
                event.setCancelled(true);
                org.bukkit.entity.IronGolem golem = (org.bukkit.entity.IronGolem) 
                    loc.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
                golem.setCustomName("§7Taş Golem");
                if (golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)
                        .setBaseValue(100.0);
                }
                golem.setHealth(100.0);
            }
        }
    }
}

