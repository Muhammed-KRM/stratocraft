package me.mami.stratocraft.manager;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.mami.stratocraft.Main;

/**
 * Hava Drop Sistemi
 * Her 3 saatte bir rastgele noktaya sandık düşer
 */
public class SupplyDropManager {
    private final Main plugin;
    private long dropInterval = 3 * 60 * 60 * 20L; // 3 saat (tick cinsinden) - config'den
    private final Random random = new Random();
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;

    public SupplyDropManager(Main plugin) {
        this.plugin = plugin;
        startDropTask();
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
        if (config != null) {
            this.dropInterval = config.getSupplyDropInterval();
        }
    }

    /**
     * Hava drop görevini başlat
     */
    private void startDropTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnSupplyDrop();
            }
        }.runTaskTimer(plugin, dropInterval, dropInterval);
    }

    /**
     * Hava drop oluştur (rastgele konum)
     */
    public void spawnSupplyDrop() {
        // Rastgele dünya seç (normal dünya)
        World world = plugin.getServer().getWorlds().get(0);
        if (world == null)
            return;

        // Rastgele konum (spawn'dan uzak, PvP açık alan)
        Location spawnLoc = world.getSpawnLocation();
        int spawnRange = balanceConfig != null ? balanceConfig.getSupplyDropSpawnRange() : 2000;
        int x = spawnLoc.getBlockX() + random.nextInt(spawnRange) - (spawnRange / 2);
        int z = spawnLoc.getBlockZ() + random.nextInt(spawnRange) - (spawnRange / 2);
        
        // Güvenli Y koordinatı bul (sıvı ve geçersiz blok kontrolü)
        int groundY = findSafeGroundY(world, x, z);
        if (groundY <= 0) {
            // Geçersiz konum, tekrar dene
            plugin.getLogger().warning("Supply Drop için geçersiz konum bulundu, atlanıyor.");
            return;
        }
        
        int dropHeight = balanceConfig != null ? balanceConfig.getSupplyDropHeight() : 50;
        int dropY = groundY + dropHeight; // Config'den yükseklik

        Location dropLoc = new Location(world, x + 0.5, dropY, z + 0.5);
        Location groundLoc = new Location(world, x, groundY + 1, z);

        spawnSupplyDropAtLocation(dropLoc, groundLoc);
    }

    /**
     * Hava drop oluştur (belirli konum - admin komutu için)
     */
    public void spawnSupplyDropAtLocation(Location targetLocation, Location groundLocation) {
        if (targetLocation == null || targetLocation.getWorld() == null)
            return;

        World world = targetLocation.getWorld();

        // Eğer groundLocation verilmemişse, targetLocation'dan hesapla
        Location groundLoc;
        if (groundLocation == null) {
            int groundY = findSafeGroundY(world, targetLocation.getBlockX(), targetLocation.getBlockZ());
            if (groundY <= 0) {
                plugin.getLogger().warning("Supply Drop için geçersiz konum bulundu, atlanıyor.");
                return;
            }
            groundLoc = new Location(world, targetLocation.getBlockX(), groundY + 1, targetLocation.getBlockZ());
        } else {
            groundLoc = groundLocation;
        }

        // Drop konumu (targetLocation'dan config'den yükseklik kadar yukarı)
        int dropHeight = balanceConfig != null ? balanceConfig.getSupplyDropHeight() : 50;
        int dropY = targetLocation.getBlockY() + dropHeight;
        Location dropLoc = new Location(world, targetLocation.getBlockX() + 0.5, dropY,
                targetLocation.getBlockZ() + 0.5);

        // Düşen sandık oluştur (FallingBlock - CHEST block data)
        FallingBlock fallingChest = world.spawnFallingBlock(dropLoc, Material.CHEST.createBlockData());
        fallingChest.setDropItem(false);
        fallingChest.setHurtEntities(false);
        fallingChest.setMetadata("SupplyDrop", new org.bukkit.metadata.FixedMetadataValue(plugin, groundLoc));

        // YAVAŞ DÜŞME - Velocity azaltma
        fallingChest.setVelocity(new Vector(0, -0.3, 0)); // Yavaş düşüş

        // Düşerken YOGUN partikül efekti (yavaş düşüş için artırılmış partikül)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!fallingChest.isValid() || fallingChest.isOnGround()) {
                    cancel();
                    return;
                }

                // Velocity'yi her tick azalt (yavaş düşme efekti)
                Vector currentVel = fallingChest.getVelocity();
                if (currentVel.getY() < -0.3) {
                    fallingChest.setVelocity(new Vector(0, -0.3, 0));
                }

                // YOGUN duman + ateş efekti
                try {
                    Location loc = fallingChest.getLocation();
                    world.spawnParticle(org.bukkit.Particle.SMOKE_LARGE, loc, 20, 0.5, 0.5, 0.5, 0.05);
                    world.spawnParticle(org.bukkit.Particle.FLAME, loc, 15, 0.3, 0.3, 0.3, 0.02);
                    world.spawnParticle(org.bukkit.Particle.CLOUD, loc, 10, 0.4, 0.4, 0.4, 0.03);
                    // Her 5 tick'te bir ses efekti
                    if (fallingChest.getTicksLived() % 5 == 0) {
                        world.playSound(loc, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.8f);
                    }
                } catch (Exception e) {
                    // Partikül hatası - sessizce atla
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Global mesaj (düşmeden önce)
        plugin.getServer().broadcastMessage("§6§l════════════════════════════");
        plugin.getServer().broadcastMessage("§e§lHAVA DROP DÜŞÜYOR!");
        plugin.getServer().broadcastMessage("§7Konum: §e" + groundLoc.getBlockX() +
                "§7, §e" + groundLoc.getBlockY() + "§7, §e" + groundLoc.getBlockZ());
        plugin.getServer().broadcastMessage("§6§l════════════════════════════");
    }

    /**
     * Hava drop yere düştüğünde sandığı oluştur (EntityChangeBlockEvent'te
     * çağrılır)
     */
    public void onSupplyDropLand(org.bukkit.event.entity.EntityChangeBlockEvent event) {
        if (event == null)
            return;
        if (!(event.getEntity() instanceof FallingBlock))
            return;
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();

        if (fallingBlock == null || !fallingBlock.hasMetadata("SupplyDrop"))
            return;

        // Metadata kontrolü
        if (fallingBlock.getMetadata("SupplyDrop").isEmpty())
            return;
        Location groundLoc = (Location) fallingBlock.getMetadata("SupplyDrop").get(0).value();
        if (groundLoc == null)
            return;

        Location finalLoc = event.getBlock().getLocation();
        if (finalLoc == null || finalLoc.getWorld() == null)
            return;

        // Sandığı yere koy (blok kontrolü ile)
        // NOT: Listener'da event iptal edilmediği için blok doğal olarak oluşacak
        // if (finalLoc.getBlock().getType() != Material.CHEST) {
        // finalLoc.getBlock().setType(Material.CHEST);
        // }

        // Biraz bekle (blok state'inin yüklenmesi için)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Null ve dünya kontrolü
                if (finalLoc == null || finalLoc.getWorld() == null)
                    return;
                if (finalLoc.getBlock() == null)
                    return;

                if (finalLoc.getBlock().getState() instanceof Chest) {
                    Chest chest = (Chest) finalLoc.getBlock().getState();

                    // İçine değerli eşyalar koy (Titanyum, Batarya, Para) - Config'den
                    int minDiamond = balanceConfig != null ? balanceConfig.getSupplyDropMinDiamond() : 3;
                    int maxDiamond = balanceConfig != null ? balanceConfig.getSupplyDropMaxDiamond() : 8;
                    chest.getInventory().addItem(new ItemStack(Material.DIAMOND, random.nextInt(maxDiamond - minDiamond + 1) + minDiamond));
                    if (ItemManager.TITANIUM_INGOT != null) {
                        chest.getInventory().addItem(ItemManager.TITANIUM_INGOT.clone());
                    }
                    int minGold = balanceConfig != null ? balanceConfig.getSupplyDropMinGold() : 10;
                    int maxGold = balanceConfig != null ? balanceConfig.getSupplyDropMaxGold() : 30;
                    chest.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, random.nextInt(maxGold - minGold + 1) + minGold));
                    int minEmerald = balanceConfig != null ? balanceConfig.getSupplyDropMinEmerald() : 5;
                    int maxEmerald = balanceConfig != null ? balanceConfig.getSupplyDropMaxEmerald() : 15;
                    chest.getInventory().addItem(new ItemStack(Material.EMERALD, random.nextInt(maxEmerald - minEmerald + 1) + minEmerald));

                    // Batarya eşyası ekle (Lightning Core veya başka bir batarya eşyası) - Config'den
                    double lightningCoreChance = balanceConfig != null ? balanceConfig.getSupplyDropLightningCoreChance() : 0.3;
                    if (ItemManager.LIGHTNING_CORE != null && random.nextDouble() < lightningCoreChance) {
                        chest.getInventory().addItem(ItemManager.LIGHTNING_CORE.clone());
                    }

                    chest.update();
                }

                // Duman efekti - Config'den süre
                int smokeDuration = balanceConfig != null ? balanceConfig.getSupplyDropSmokeEffectDuration() : 100;
                new BukkitRunnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        if (ticks > smokeDuration || finalLoc.getWorld() == null) {
                            cancel();
                            return;
                        }

                        // Duman efekti
                        try {
                            finalLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE,
                                    finalLoc.clone().add(0.5, 1, 0.5), 10, 0.5, 0.5, 0.5, 0.1);
                        } catch (Exception e) {
                            // Partikül hatası - sessizce atla
                            cancel();
                            return;
                        }

                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);

                // Global mesaj
                plugin.getServer().broadcastMessage("§6§l════════════════════════════");
                plugin.getServer().broadcastMessage("§e§lHAVA DROP DÜŞTÜ!");
                plugin.getServer().broadcastMessage("§7Konum: §e" + finalLoc.getBlockX() +
                        "§7, §e" + finalLoc.getBlockY() + "§7, §e" + finalLoc.getBlockZ());
                plugin.getServer().broadcastMessage("§6§l════════════════════════════");

                // HAVAI FİŞEK İŞARETİ (Görsel işaret - haritanın öbür ucundan görülebilir)
                // Null kontrolü ile
                if (finalLoc.getWorld() != null) {
                    try {
                        org.bukkit.entity.Entity fireworkEntity = finalLoc.getWorld().spawnEntity(
                                finalLoc.clone().add(0, 5, 0),
                                org.bukkit.entity.EntityType.FIREWORK);
                        if (fireworkEntity instanceof Firework) {
                            Firework firework = (Firework) fireworkEntity;
                            FireworkMeta fireworkMeta = firework.getFireworkMeta();
                            fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
                                    .with(org.bukkit.FireworkEffect.Type.BURST)
                                    .withColor(org.bukkit.Color.ORANGE, org.bukkit.Color.YELLOW, org.bukkit.Color.RED)
                                    .withFade(org.bukkit.Color.WHITE)
                                    .flicker(true)
                                    .trail(true)
                                    .build());
                            fireworkMeta.setPower(2);
                            firework.setFireworkMeta(fireworkMeta);
                        }

                        // Yıldırım efekti (hasarsız - sadece görsel)
                        finalLoc.getWorld().strikeLightningEffect(finalLoc.clone().add(0, 10, 0));
                    } catch (Exception e) {
                        // Firework spawn hatası - sessizce atla
                        plugin.getLogger().warning("Supply Drop firework spawn hatası: " + e.getMessage());
                    }
                }
            }
        }.runTaskLater(plugin, 1L); // 1 tick sonra çalıştır
    }
    
    /**
     * Güvenli Y koordinatı bul (sıvı ve geçersiz blok kontrolü)
     */
    private int findSafeGroundY(World world, int x, int z) {
        int groundY = world.getHighestBlockYAt(x, z);
        
        // Geçersiz Y kontrolü
        if (groundY <= 0 || groundY >= world.getMaxHeight()) {
            return -1;
        }
        
        // Sıvı kontrolü: Eğer en üst blok su veya lav ise, bir altına bak
        org.bukkit.block.Block topBlock = world.getBlockAt(x, groundY, z);
        if (topBlock.getType() == Material.WATER || 
            topBlock.getType() == Material.LAVA ||
            topBlock.getType() == Material.KELP ||
            topBlock.getType() == Material.SEAGRASS) {
            // Sıvı üzerinde, bir altına bak
            if (groundY > 0) {
                groundY--;
            } else {
                return -1; // Geçersiz
            }
        }
        
        // Blok geçerliliği kontrolü: Hava veya geçici bloklar (yaprak, çimen vb.) olabilir
        org.bukkit.block.Block finalBlock = world.getBlockAt(x, groundY, z);
        if (finalBlock.getType() == Material.AIR || 
            finalBlock.getType() == Material.CAVE_AIR ||
            finalBlock.getType() == Material.VOID_AIR) {
            // Hava bloğu, bir altına bak
            if (groundY > 0) {
                groundY--;
            } else {
                return -1; // Geçersiz
            }
        }
        
        return groundY;
    }
}
