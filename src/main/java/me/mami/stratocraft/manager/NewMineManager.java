package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Yeni Mayın Sistemi Yöneticisi
 * 
 * 25 farklı mayın tipi (5 seviye x 5 mayın)
 * Basınç plakası tabanlı, görünür mayınlar
 * Gizleme sistemi ile görünmez yapılabilir
 */
public class NewMineManager {
    private final Main plugin;
    private final ClanManager clanManager;
    
    // Aktif mayınlar (Location -> MineData)
    private final Map<Location, MineData> activeMines = new HashMap<>();
    // Mayın isim standları (Location -> ArmorStand)
    private final Map<Location, ArmorStand> mineNameStands = new HashMap<>();
    // Gizli mayınlar (Location -> boolean)
    private final Set<Location> hiddenMines = new HashSet<>();
    
    /**
     * Mayın Tipi Enum (25 benzersiz mayın)
     */
    public enum MineType {
        // Seviye 1 (5 Mayın)
        EXPLOSIVE("Patlama Mayını", 1),
        POISON("Zehir Mayını", 1),
        SLOWNESS("Yavaşlık Mayını", 1),
        LIGHTNING("Yıldırım Mayını", 1),
        FIRE("Yakma Mayını", 1),
        
        // Seviye 2 (5 Mayın)
        CAGE("Kafes Hapsetme Mayını", 2),
        LAUNCH("Fırlatma Mayını", 2),
        MOB_SPAWN("Canavar Spawn Mayını", 2),
        BLINDNESS("Körlük Mayını", 2),
        WEAKNESS("Zayıflık Mayını", 2),
        
        // Seviye 3 (5 Mayın)
        FREEZE("Dondurma Mayını", 3),
        CONFUSION("Karışıklık Mayını", 3),
        FATIGUE("Yorgunluk Mayını", 3),
        POISON_CLOUD("Zehir Bulutu Mayını", 3),
        LIGHTNING_STORM("Yıldırım Fırtınası Mayını", 3),
        
        // Seviye 4 (5 Mayın)
        MEGA_EXPLOSIVE("Büyük Patlama Mayını", 4),
        LARGE_CAGE("Büyük Kafes Mayını", 4),
        SUPER_LAUNCH("Güçlü Fırlatma Mayını", 4),
        ELITE_MOB_SPAWN("Güçlü Canavar Spawn Mayını", 4),
        MULTI_EFFECT("Çoklu Efekt Mayını", 4),
        
        // Seviye 5 (5 Mayın)
        NUCLEAR_EXPLOSIVE("Nükleer Patlama Mayını", 5),
        DEATH_CLOUD("Ölüm Bulutu Mayını", 5),
        THUNDERSTORM("Gök Gürültüsü Mayını", 5),
        BOSS_SPAWN("Boss Spawn Mayını", 5),
        CHAOS("Kaos Mayını", 5);
        
        private final String displayName;
        private final int level;
        
        MineType(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public static MineType fromString(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    /**
     * Mayın Veri Sınıfı
     */
    public static class MineData {
        private final UUID ownerId;
        private final UUID ownerClanId;
        private final MineType type;
        private final Location location;
        private boolean hidden;
        
        public MineData(UUID ownerId, UUID ownerClanId, MineType type, Location location) {
            this.ownerId = ownerId;
            this.ownerClanId = ownerClanId;
            this.type = type;
            this.location = location;
            this.hidden = false;
        }
        
        public UUID getOwnerId() { return ownerId; }
        public UUID getOwnerClanId() { return ownerClanId; }
        public MineType getType() { return type; }
        public Location getLocation() { return location; }
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
    }
    
    public NewMineManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }
    
    /**
     * Mayın oluştur
     */
    public boolean createMine(Player player, Block pressurePlate, MineType type) {
        if (pressurePlate == null || player == null) {
            return false;
        }
        
        Location loc = pressurePlate.getLocation();
        
        // Zaten mayın var mı?
        if (activeMines.containsKey(loc)) {
            player.sendMessage("§cBu konumda zaten bir mayın var!");
            return false;
        }
        
        // Basınç plakası kontrolü
        if (!isPressurePlate(pressurePlate.getType())) {
            player.sendMessage("§cBu bir basınç plakası değil!");
            return false;
        }
        
        // Klan ID'sini al
        UUID clanId = null;
        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            clanId = clanManager.getClanByPlayer(player.getUniqueId()).getId();
        }
        
        // MineData oluştur
        MineData mine = new MineData(player.getUniqueId(), clanId, type, loc);
        
        // Aktif mayınlar listesine ekle
        activeMines.put(loc, mine);
        
        // Metadata ekle
        pressurePlate.setMetadata("NewMine", new FixedMetadataValue(plugin, true));
        pressurePlate.setMetadata("MineOwner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        pressurePlate.setMetadata("MineType", new FixedMetadataValue(plugin, type.name()));
        
        // İsim standı oluştur (görünür mayınlar için)
        createNameStand(loc, type);
        
        player.sendMessage("§a§lMayın yerleştirildi: §e" + type.getDisplayName());
        return true;
    }
    
    /**
     * İsim standı oluştur
     */
    private void createNameStand(Location loc, MineType type) {
        Location standLoc = loc.clone().add(0.5, 0.1, 0.5);
        ArmorStand nameStand = (ArmorStand) loc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
        nameStand.setVisible(false);
        nameStand.setGravity(false);
        nameStand.setInvulnerable(true);
        nameStand.setCustomNameVisible(true);
        nameStand.setCustomName("§c§l" + type.getDisplayName());
        nameStand.setMarker(true);
        nameStand.setSmall(true);
        nameStand.setHeadPose(new EulerAngle(0, 0, 0));
        nameStand.setMetadata("MineNameStand", new FixedMetadataValue(plugin, true));
        
        mineNameStands.put(loc, nameStand);
    }
    
    /**
     * Mayın tetikle
     */
    public void triggerMine(Block pressurePlate, Player victim) {
        if (pressurePlate == null || victim == null) {
            return;
        }
        
        Location loc = pressurePlate.getLocation();
        MineData mine = activeMines.get(loc);
        
        if (mine == null) {
            return;
        }
        
        // Klan kontrolü - Dostlar korunur
        if (mine.getOwnerClanId() != null && victim != null) {
            me.mami.stratocraft.model.Clan victimClan = clanManager.getClanByPlayer(victim.getUniqueId());
            if (victimClan != null && victimClan.getId().equals(mine.getOwnerClanId())) {
                return; // Aynı klan, mayın tetiklenmez
            }
        }
        
        // Mayın etkisini uygula
        applyMineEffect(mine, victim, pressurePlate.getLocation());
        
        // Mayını kaldır (basınca yok olur)
        removeMine(loc);
        
        // Basınç plakasını kaldır
        pressurePlate.setType(Material.AIR);
    }
    
    /**
     * Mayın etkisini uygula
     */
    private void applyMineEffect(MineData mine, Player victim, Location mineLoc) {
        MineType type = mine.getType();
        int level = type.getLevel();
        
        switch (type) {
            // ========== SEVİYE 1 MAYINLAR ==========
            case EXPLOSIVE:
                mineLoc.getWorld().createExplosion(mineLoc, 2.0f, false, false);
                victim.sendMessage("§c§lMAYIN PATLADI!");
                break;
                
            case POISON:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, false, false));
                victim.sendMessage("§2§lZEHİRLENDİN!");
                break;
                
            case SLOWNESS:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, false, false));
                victim.sendMessage("§b§lYAVAŞLADIN!");
                break;
                
            case LIGHTNING:
                mineLoc.getWorld().strikeLightning(mineLoc);
                victim.damage(3.0);
                victim.sendMessage("§e§lYILDIRIM ÇARPTI!");
                break;
                
            case FIRE:
                victim.setFireTicks(100);
                victim.damage(2.0);
                victim.sendMessage("§c§lYANDIN!");
                mineLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, 
                    mineLoc.clone().add(0.5, 0.1, 0.5), 30, 0.3, 0.1, 0.3, 0.1);
                break;
            
            // ========== SEVİYE 2 MAYINLAR ==========
            case CAGE:
                createCage(mineLoc, 3, 200); // 3x3x3, 10 saniye
                victim.sendMessage("§8§lKAFESE HAPSEDİLDİN!");
                break;
                
            case LAUNCH:
                victim.setVelocity(new Vector(0, 1.5, 0));
                victim.sendMessage("§e§lYUKARI FIRLATILDIN!");
                break;
                
            case MOB_SPAWN:
                spawnMobs(mineLoc, 3, EntityType.ZOMBIE);
                victim.sendMessage("§c§lCANAVARLAR SPAWN OLDU!");
                break;
                
            case BLINDNESS:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 160, 0, false, false));
                victim.sendMessage("§8§lKÖR OLDUN!");
                break;
                
            case WEAKNESS:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 160, 0, false, false));
                victim.sendMessage("§7§lZAYIFLADIN!");
                break;
            
            // ========== SEVİYE 3 MAYINLAR ==========
            case FREEZE:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2, false, false)); // Çok yavaş
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 1, false, false));
                mineLoc.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, 
                    mineLoc.clone().add(0.5, 0.1, 0.5), 30, 0.3, 0.1, 0.3, 0.1);
                victim.sendMessage("§b§lDONDUN!");
                break;
                
            case CONFUSION:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1, false, false));
                victim.sendMessage("§d§lKARIŞIKLIK HİSSEDİYORSUN!");
                break;
                
            case FATIGUE:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 300, 2, false, false));
                victim.sendMessage("§7§lYORGUNLUK HİSSEDİYORSUN!");
                break;
                
            case POISON_CLOUD:
                // Çevredeki tüm oyunculara zehir
                for (Entity entity : mineLoc.getWorld().getNearbyEntities(mineLoc, 5, 5, 5)) {
                    if (entity instanceof Player) {
                        Player nearby = (Player) entity;
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 0, false, false));
                    }
                }
                mineLoc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, 
                    mineLoc.clone().add(0.5, 0.1, 0.5), 50, 2.5, 1, 2.5, 0.1);
                victim.sendMessage("§2§lZEHİR BULUTU!");
                break;
                
            case LIGHTNING_STORM:
                for (int i = 0; i < 3; i++) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        mineLoc.getWorld().strikeLightning(mineLoc);
                    }, i * 10L);
                }
                victim.damage(5.0);
                victim.sendMessage("§e§lYILDIRIM FIRTINASI!");
                break;
            
            // ========== SEVİYE 4 MAYINLAR ==========
            case MEGA_EXPLOSIVE:
                mineLoc.getWorld().createExplosion(mineLoc, 6.0f, false, false);
                victim.sendMessage("§c§lBÜYÜK PATLAMA!");
                break;
                
            case LARGE_CAGE:
                createCage(mineLoc, 5, 400); // 5x5x5, 20 saniye
                victim.sendMessage("§8§lBÜYÜK KAFESE HAPSEDİLDİN!");
                break;
                
            case SUPER_LAUNCH:
                victim.setVelocity(new Vector(0, 2.5, 0));
                victim.sendMessage("§e§lÇOK YUKARI FIRLATILDIN!");
                break;
                
            case ELITE_MOB_SPAWN:
                spawnMobs(mineLoc, 5, EntityType.ZOMBIE);
                spawnMobs(mineLoc, 2, EntityType.SKELETON);
                victim.sendMessage("§c§lGÜÇLÜ CANAVARLAR!");
                break;
                
            case MULTI_EFFECT:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1, false, false));
                victim.sendMessage("§d§lÇOKLU EFEKT!");
                break;
            
            // ========== SEVİYE 5 MAYINLAR ==========
            case NUCLEAR_EXPLOSIVE:
                mineLoc.getWorld().createExplosion(mineLoc, 10.0f, false, false);
                victim.sendMessage("§4§lNÜKLEER PATLAMA!");
                break;
                
            case DEATH_CLOUD:
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 400, 3, false, false));
                // Sürekli hasar
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks++ >= 100 || victim == null || victim.isDead()) {
                            cancel();
                            return;
                        }
                        victim.damage(0.5);
                    }
                }.runTaskTimer(plugin, 0L, 10L);
                mineLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, 
                    mineLoc.clone().add(0.5, 0.1, 0.5), 100, 5, 2, 5, 0.1);
                victim.sendMessage("§4§lÖLÜM BULUTU!");
                break;
                
            case THUNDERSTORM:
                for (int i = 0; i < 10; i++) {
                    Location strikeLoc = mineLoc.clone().add(
                        (Math.random() - 0.5) * 10,
                        0,
                        (Math.random() - 0.5) * 10
                    );
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        strikeLoc.getWorld().strikeLightning(strikeLoc);
                    }, i * 5L);
                }
                victim.damage(10.0);
                victim.sendMessage("§e§lGÖK GÜRÜLTÜSÜ!");
                break;
                
            case BOSS_SPAWN:
                if (Math.random() < 0.5) {
                    mineLoc.getWorld().spawnEntity(mineLoc, EntityType.WITHER_SKELETON);
                } else {
                    mineLoc.getWorld().spawnEntity(mineLoc, EntityType.ENDERMAN);
                }
                victim.sendMessage("§4§lBOSS SPAWN OLDU!");
                break;
                
            case CHAOS:
                // Tüm efektler
                mineLoc.getWorld().createExplosion(mineLoc, 5.0f, false, false);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 2, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, false, false));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2, false, false));
                victim.setFireTicks(200);
                mineLoc.getWorld().strikeLightning(mineLoc);
                victim.sendMessage("§4§lKAOS!");
                break;
        }
        
        // Ses efekti
        mineLoc.getWorld().playSound(mineLoc, org.bukkit.Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
        
        // Partikül efekti
        mineLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE,
                mineLoc.clone().add(0.5, 0.1, 0.5), 20, 0.3, 0.1, 0.3, 0.1);
    }
    
    /**
     * Kafes oluştur
     */
    private void createCage(Location center, int size, long duration) {
        int halfSize = size / 2;
        List<Location> cageBlocks = new ArrayList<>();
        
        // Kafes bloklarını oluştur
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int y = 0; y <= size; y++) {
                for (int z = -halfSize; z <= halfSize; z++) {
                    // Sadece kenarları oluştur
                    if (x == -halfSize || x == halfSize || 
                        y == 0 || y == size ||
                        z == -halfSize || z == halfSize) {
                        Location blockLoc = center.clone().add(x, y, z);
                        if (blockLoc.getBlock().getType() == Material.AIR) {
                            blockLoc.getBlock().setType(Material.OBSIDIAN);
                            cageBlocks.add(blockLoc);
                        }
                    }
                }
            }
        }
        
        // Belirli süre sonra kaldır
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : cageBlocks) {
                    if (loc.getBlock().getType() == Material.OBSIDIAN) {
                        loc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(plugin, duration / 50L);
    }
    
    /**
     * Canavarlar spawnla
     */
    private void spawnMobs(Location loc, int count, EntityType type) {
        for (int i = 0; i < count; i++) {
            Location spawnLoc = loc.clone().add(
                (Math.random() - 0.5) * 3,
                1,
                (Math.random() - 0.5) * 3
            );
            loc.getWorld().spawnEntity(spawnLoc, type);
        }
    }
    
    /**
     * Mayını kaldır
     */
    public void removeMine(Location loc) {
        MineData mine = activeMines.remove(loc);
        if (mine != null) {
            Block block = loc.getBlock();
            block.removeMetadata("NewMine", plugin);
            block.removeMetadata("MineOwner", plugin);
            block.removeMetadata("MineType", plugin);
            
            // İsim standını kaldır
            ArmorStand nameStand = mineNameStands.remove(loc);
            if (nameStand != null && !nameStand.isDead()) {
                nameStand.remove();
            }
            
            hiddenMines.remove(loc);
        }
    }
    
    /**
     * Mayını gizle/göster
     */
    public boolean toggleMineVisibility(Location loc) {
        MineData mine = activeMines.get(loc);
        if (mine == null) {
            return false;
        }
        
        boolean isHidden = hiddenMines.contains(loc);
        
        if (isHidden) {
            // Görünür yap
            hiddenMines.remove(loc);
            mine.setHidden(false);
            
            // Basınç plakasını görünür yap
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(getPressurePlateType(mine.getType()));
            }
            
            // İsim standını görünür yap
            ArmorStand nameStand = mineNameStands.get(loc);
            if (nameStand != null && !nameStand.isDead()) {
                nameStand.setCustomNameVisible(true);
            }
            
            return true;
        } else {
            // Gizle
            hiddenMines.add(loc);
            mine.setHidden(true);
            
            // Basınç plakasını gizle
            Block block = loc.getBlock();
            block.setType(Material.AIR);
            
            // İsim standını gizle
            ArmorStand nameStand = mineNameStands.get(loc);
            if (nameStand != null && !nameStand.isDead()) {
                nameStand.setCustomNameVisible(false);
            }
            
            return true;
        }
    }
    
    /**
     * Basınç plakası tipi al
     */
    private Material getPressurePlateType(MineType type) {
        int level = type.getLevel();
        switch (level) {
            case 1:
            case 2:
                return Material.OAK_PRESSURE_PLATE;
            case 3:
                return Material.STONE_PRESSURE_PLATE;
            case 4:
                return Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
            case 5:
                return Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
            default:
                return Material.OAK_PRESSURE_PLATE;
        }
    }
    
    /**
     * Basınç plakası mı?
     */
    public static boolean isPressurePlate(Material material) {
        return material == Material.OAK_PRESSURE_PLATE ||
               material == Material.STONE_PRESSURE_PLATE ||
               material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
               material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }
    
    /**
     * Mayın var mı?
     */
    public boolean hasMine(Location loc) {
        return activeMines.containsKey(loc);
    }
    
    /**
     * Mayın al
     */
    public MineData getMine(Location loc) {
        return activeMines.get(loc);
    }
    
    /**
     * Tüm mayınları al
     */
    public Map<Location, MineData> getAllMines() {
        return new HashMap<>(activeMines);
    }
    
    /**
     * Seviyeye göre mayınları al
     */
    public List<MineType> getMinesByLevel(int level) {
        List<MineType> mines = new ArrayList<>();
        for (MineType type : MineType.values()) {
            if (type.getLevel() == level) {
                mines.add(type);
            }
        }
        return mines;
    }
}

