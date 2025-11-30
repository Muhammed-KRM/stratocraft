package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Disaster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Entity;

public class DisasterManager {
    private Disaster activeDisaster = null;
    @SuppressWarnings("unused")
    private long lastDisasterTime = System.currentTimeMillis();

    public void triggerDisaster(Disaster.Type type) {
        World world = Bukkit.getWorlds().get(0); // Ana dünya
        int x = world.getSpawnLocation().getBlockX() + 5000;
        int z = world.getSpawnLocation().getBlockZ() + 5000;
        int y = world.getHighestBlockYAt(x, z);
        Location spawnLoc = new Location(world, x, y + 1, z);
        triggerDisaster(type, spawnLoc);
    }

    public void triggerDisaster(Disaster.Type type, Location spawnLoc) {
        World world = spawnLoc.getWorld();

        if (type == Disaster.Type.TITAN_GOLEM) {
            Giant golem = (Giant) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.GIANT);
            golem.setCustomName("§4§lTITAN GOLEM");
            golem.setHealth(500.0);

            // Maksimum sağlık artır
            if (golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
            }
            golem.setHealth(500.0);

            activeDisaster = new Disaster(type, golem, world.getSpawnLocation());
            Bukkit.broadcastMessage("§c§lUYARI! §4Titan Golem haritanın ucunda doğdu ve merkeze yürüyor!");
        } else if (type == Disaster.Type.ABYSSAL_WORM) {
            // Yüzeyin biraz altında ama boşlukta doğmaya çalış
            // Şimdilik yüzeyde doğsun, AI onu aşağı çeker veya burrow yapar
            Location wormLoc = spawnLoc.clone();
            // Yüzeyin 5 blok altına koy ama solid blok kontrolü yapma (Silverfish zaten
            // blok içinde gezebilir)
            // Ancak görünür olması için yüzeye yakın olsun

            org.bukkit.entity.Silverfish worm = (org.bukkit.entity.Silverfish) wormLoc.getWorld()
                    .spawnEntity(wormLoc, EntityType.SILVERFISH);
            worm.setCustomName("§5§lHİÇLİK SOLUCANI");

            if (worm.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                worm.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
            }
            worm.setHealth(300.0);

            // Görünmezlik veya efekt ekle
            worm.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY,
                    999999, 0, false, false));

            activeDisaster = new Disaster(type, worm, world.getSpawnLocation());
            Bukkit.broadcastMessage("§5§lUYARI! §dHiçlik Solucanı yer altından merkeze doğru ilerliyor!");
        } else if (type == Disaster.Type.SOLAR_FLARE) {
            activeDisaster = new Disaster(type, null, world.getSpawnLocation());
            Bukkit.broadcastMessage("§c§lUYARI! §6Güneş Fırtınası başladı! 10 dakika boyunca yüzeyde yanacaksınız!");
            // Güneş Fırtınası için özel task başlatılacak
        }
    }

    public void forceWormSurface(Location seismicLocation) {
        Disaster disaster = getActiveDisaster();
        if (disaster == null || disaster.getType() != Disaster.Type.ABYSSAL_WORM)
            return;

        Entity worm = disaster.getEntity();
        if (worm == null)
            return;

        // Solucanı yüzeye çıkar
        Location surfaceLoc = seismicLocation.clone();
        surfaceLoc.setY(seismicLocation.getWorld().getHighestBlockYAt(seismicLocation) + 1);
        worm.teleport(surfaceLoc);
        Bukkit.broadcastMessage("§6§lSİSMİK ÇEKİÇ! Hiçlik Solucanı yüzeye çıkmaya zorlandı!");
    }

    private me.mami.stratocraft.manager.BuffManager buffManager;
    private me.mami.stratocraft.manager.TerritoryManager territoryManager;

    public void setBuffManager(me.mami.stratocraft.manager.BuffManager bm) {
        this.buffManager = bm;
    }

    public void setTerritoryManager(me.mami.stratocraft.manager.TerritoryManager tm) {
        this.territoryManager = tm;
    }

    public void dropRewards(Disaster disaster) {
        if (disaster == null || disaster.getEntity() == null)
            return;
        Location loc = disaster.getEntity().getLocation();

        // ENKAZ YIĞINI OLUŞTUR: Boss öldüğünde havada asılı bir yapı
        createWreckageStructure(loc);

        // Ödüller düşür
        if (Math.random() < 0.5) {
            if (me.mami.stratocraft.manager.ItemManager.DARK_MATTER != null) {
                loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
            }
        } else {
            if (me.mami.stratocraft.manager.ItemManager.STAR_CORE != null) {
                loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
            }
        }

        // KAHraman Buff'ı: Felaket tarafından yıkılan klanlara ver
        if (territoryManager != null && buffManager != null) {
            me.mami.stratocraft.model.Clan affectedClan = territoryManager.getTerritoryOwner(loc);
            if (affectedClan != null) {
                buffManager.applyHeroBuff(affectedClan);
            }
        }

        Bukkit.broadcastMessage("§a§lFelaket yok edildi! Ödüller düştü!");
    }

    private void createWreckageStructure(Location center) {
        // Enkaz yığını: Yere düşür (yüzeye)
        org.bukkit.Material wreckageMat = org.bukkit.Material.ANCIENT_DEBRIS;

        // Yüzey Y koordinatını bul
        int surfaceY = center.getWorld().getHighestBlockYAt(center);
        Location surfaceLoc = center.clone();
        surfaceLoc.setY(surfaceY);

        // 5x5x3 boyutunda enkaz yığını (yüzeyde)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y < 3; y++) {
                    Location blockLoc = surfaceLoc.clone().add(x, y, z);
                    if (blockLoc.getBlock().getType() == org.bukkit.Material.AIR ||
                            blockLoc.getBlock().getType() == org.bukkit.Material.GRASS_BLOCK ||
                            blockLoc.getBlock().getType() == org.bukkit.Material.TALL_GRASS) {
                        blockLoc.getBlock().setType(wreckageMat);
                    }
                }
            }
        }

        // Enkazı ScavengerManager'a kaydet (yüzey konumuna göre)
        if (me.mami.stratocraft.Main.getInstance() != null) {
            me.mami.stratocraft.manager.ScavengerManager sm = ((me.mami.stratocraft.Main) me.mami.stratocraft.Main
                    .getInstance()).getScavengerManager();
            if (sm != null) {
                sm.markWreckage(surfaceLoc, java.util.UUID.randomUUID());
            }
        }
    }

    public Disaster getActiveDisaster() {
        return activeDisaster;
    }

    public void setActiveDisaster(Disaster d) {
        this.activeDisaster = d;
    }

    /**
     * Tüm aktif felaketleri temizle
     */
    public void clearAllDisasters() {
        if (activeDisaster != null) {
            if (activeDisaster.getEntity() != null) {
                activeDisaster.kill();
            }
            activeDisaster = null;
            Bukkit.broadcastMessage("§a§lTüm felaketler temizlendi!");
        }
    }
}
