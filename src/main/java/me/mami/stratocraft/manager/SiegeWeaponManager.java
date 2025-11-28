package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Savaş Alanı Yapıları Yöneticisi
 * 
 * KATEGORİ 1: Herkesin kullanabildiği yapılar (Klan alanı dışına kurulabilir)
 * - Mancınık (Catapult): Patlayıcı mermi fırlatır
 * - Enerji Kalkanı (Force Field): Geçici koruma küresi oluşturur
 * - Balista (Ballista): Hızlı ok fırlatır (daha az hasar, daha hızlı)
 * - Lav Fıskiyesi (Lava Fountain): Etrafına lav fırlatır
 * - Zehir Gazı Yayıcı (Poison Gas Dispenser): Etrafına zehir verir
 * 
 * KATEGORİ 2: Klan özel yapılar (Klan alanı dışına kurulabilir, sadece klan üyelerine etki eder)
 * - Can Tapınağı (Healing Shrine): Sadece kurucu klanın üyelerine iyileştirme verir
 * - Güç Totemi (Power Totem): Klan üyelerine güç buff verir
 * - Hız Çemberi (Speed Circle): Klan üyelerine hız buff verir
 * - Savunma Duvarı (Defense Wall): Klan üyelerine direnç buff verir
 */
public class SiegeWeaponManager {
    private final Main plugin;
    private final ClanManager clanManager;
    
    // KATEGORİ 1: Herkesin kullanabildiği yapılar
    private final Map<Location, List<Block>> activeShields = new HashMap<>(); // Jeneratör -> Kalkan blokları
    private final Map<UUID, Long> catapultCooldowns = new HashMap<>(); // Oyuncu UUID -> Son ateşleme zamanı (Memory leak önleme)
    private final Map<UUID, Long> ballistaCooldowns = new HashMap<>(); // Balista cooldown (Memory leak önleme)
    private final Map<Location, Long> lavaFountains = new HashMap<>(); // Lav fıskiyesi lokasyonları -> Son aktivasyon
    private final Map<Location, Long> poisonDispensers = new HashMap<>(); // Zehir yayıcı lokasyonları -> Son aktivasyon
    private static final long CATAPULT_COOLDOWN = 10000; // 10 saniye (milisaniye)
    private static final long BALLISTA_COOLDOWN = 3000; // 3 saniye (milisaniye)
    private static final long LAVA_FOUNTAIN_COOLDOWN = 5000; // 5 saniye
    private static final long POISON_DISPENSER_COOLDOWN = 8000; // 8 saniye
    
    // KATEGORİ 2: Klan özel yapılar
    private final Map<Location, UUID> healingShrines = new HashMap<>(); // Tapınak lokasyonu -> Kurucu klan ID
    private final Map<Location, UUID> powerTotems = new HashMap<>(); // Güç totemi -> Kurucu klan ID
    private final Map<Location, UUID> speedCircles = new HashMap<>(); // Hız çemberi -> Kurucu klan ID
    private final Map<Location, UUID> defenseWalls = new HashMap<>(); // Savunma duvarı -> Kurucu klan ID
    
    public SiegeWeaponManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }
    
    // ========== MANCINIK (CATAPULT) ==========
    
    /**
     * Mancınık cooldown kontrolü
     */
    public boolean canFireCatapult(Player player) {
        Long lastFire = catapultCooldowns.get(player.getUniqueId());
        return lastFire == null || (System.currentTimeMillis() - lastFire) >= CATAPULT_COOLDOWN;
    }
    
    /**
     * Mancınık kalan cooldown süresi (saniye)
     */
    public long getCatapultCooldownRemaining(Player player) {
        Long lastFire = catapultCooldowns.get(player.getUniqueId());
        if (lastFire == null) return 0;
        long remaining = CATAPULT_COOLDOWN - (System.currentTimeMillis() - lastFire);
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * Mancınık mermi fırlatma
     */
    public void fireCatapult(Player user, Block catapultBlock) {
        // 1. Yapının yönünü tespit et
        if (!(catapultBlock.getBlockData() instanceof Directional)) {
            user.sendMessage("§cMancınık yönü belirlenemiyor!");
            return;
        }
        
        BlockFace direction = ((Directional) catapultBlock.getBlockData()).getFacing();
        
        // 2. Mermiyi oluştur (Yanan Magma Bloğu)
        Location spawnLoc = catapultBlock.getLocation().add(0.5, 1.5, 0.5);
        FallingBlock ammo = user.getWorld().spawnFallingBlock(
            spawnLoc, 
            Material.MAGMA_BLOCK.createBlockData()
        );
        
        // 3. Fizik uygula (Fırlatma gücü)
        Vector directionVector = new Vector(
            direction.getModX(), 
            0.8, // Biraz yukarı kavis
            direction.getModZ()
        ).normalize().multiply(2.5);
        
        ammo.setVelocity(directionVector);
        ammo.setDropItem(false); // Yere düşünce eşya olmasın
        
        // 4. Mermiyi işaretle (Listener'da yakalamak için)
        ammo.setMetadata("SiegeAmmo", new FixedMetadataValue(plugin, true));
        
        // Efekt
        user.getWorld().playSound(spawnLoc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.5f);
        user.sendMessage("§6§lMANCINIK ATEŞLENDİ!");
        
        // Cooldown kaydet (UUID kullan - memory leak önleme)
        catapultCooldowns.put(user.getUniqueId(), System.currentTimeMillis());
    }
    
    // ========== BALİSTA (BALLISTA) ==========
    
    public boolean canFireBallista(Player player) {
        Long lastFire = ballistaCooldowns.get(player);
        return lastFire == null || (System.currentTimeMillis() - lastFire) >= BALLISTA_COOLDOWN;
    }
    
    public long getBallistaCooldownRemaining(Player player) {
        Long lastFire = ballistaCooldowns.get(player);
        if (lastFire == null) return 0;
        long remaining = BALLISTA_COOLDOWN - (System.currentTimeMillis() - lastFire);
        return Math.max(0, remaining / 1000);
    }
    
    public void fireBallista(Player user, Block ballistaBlock) {
        if (!(ballistaBlock.getBlockData() instanceof Directional)) {
            user.sendMessage("§cBalista yönü belirlenemiyor!");
            return;
        }
        
        BlockFace direction = ((Directional) ballistaBlock.getBlockData()).getFacing();
        
        // Ok fırlat
        Location spawnLoc = ballistaBlock.getLocation().add(0.5, 1, 0.5);
        Arrow arrow = user.getWorld().spawn(spawnLoc, Arrow.class);
        
        Vector directionVector = new Vector(
            direction.getModX(),
            0.1,
            direction.getModZ()
        ).normalize().multiply(3.0); // Hızlı ama daha az güçlü
        
        arrow.setVelocity(directionVector);
        arrow.setDamage(8.0); // Mancınıktan daha az hasar
        arrow.setMetadata("BallistaArrow", new FixedMetadataValue(plugin, true));
        
        user.getWorld().playSound(spawnLoc, Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.5f);
        user.sendMessage("§e§lBALİSTA ATEŞLENDİ!");
        
        // Cooldown kaydet (UUID kullan - memory leak önleme)
        ballistaCooldowns.put(user.getUniqueId(), System.currentTimeMillis());
    }
    
    // ========== LAV FISKIYESI (LAVA FOUNTAIN) ==========
    
    public boolean canActivateLavaFountain(Location loc) {
        Long lastActivation = lavaFountains.get(loc);
        return lastActivation == null || (System.currentTimeMillis() - lastActivation) >= LAVA_FOUNTAIN_COOLDOWN;
    }
    
    public long getLavaFountainCooldownRemaining(Location loc) {
        Long lastActivation = lavaFountains.get(loc);
        if (lastActivation == null) return 0;
        long remaining = LAVA_FOUNTAIN_COOLDOWN - (System.currentTimeMillis() - lastActivation);
        return Math.max(0, remaining / 1000);
    }
    
    public void activateLavaFountain(Location center) {
        // 8 yöne lav fırlat
        for (BlockFace face : new BlockFace[]{
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
        }) {
            Location target = center.clone().add(face.getModX() * 5, 0, face.getModZ() * 5);
            target.getBlock().setType(Material.LAVA);
            
            // 3 saniye sonra lavı temizle
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (target.getBlock().getType() == Material.LAVA) {
                    target.getBlock().setType(Material.AIR);
                }
            }, 60L); // 3 saniye = 60 tick
        }
        
        center.getWorld().playSound(center, Sound.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
        center.getWorld().spawnParticle(org.bukkit.Particle.LAVA, center.add(0, 1, 0), 20);
        
        // Cooldown kaydet
        lavaFountains.put(center, System.currentTimeMillis());
    }
    
    // ========== ZEHİR GAZI YAYICI (POISON GAS DISPENSER) ==========
    
    public boolean canActivatePoisonDispenser(Location loc) {
        Long lastActivation = poisonDispensers.get(loc);
        return lastActivation == null || (System.currentTimeMillis() - lastActivation) >= POISON_DISPENSER_COOLDOWN;
    }
    
    public long getPoisonDispenserCooldownRemaining(Location loc) {
        Long lastActivation = poisonDispensers.get(loc);
        if (lastActivation == null) return 0;
        long remaining = POISON_DISPENSER_COOLDOWN - (System.currentTimeMillis() - lastActivation);
        return Math.max(0, remaining / 1000);
    }
    
    public void activatePoisonDispenser(Location center) {
        // 10 blok yarıçapındaki tüm oyunculara zehir ver
        for (Entity entity : center.getWorld().getNearbyEntities(center, 10, 10, 10)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.POISON, 
                    100, // 5 saniye
                    1 // Seviye 2 zehir
                ));
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS,
                    60, // 3 saniye
                    0
                ));
                target.sendMessage("§c§lZEHİR GAZINA YAKALANDIN!");
            }
        }
        
        center.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, center.add(0, 1, 0), 50);
        center.getWorld().playSound(center, Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.5f);
        
        // Cooldown kaydet
        poisonDispensers.put(center, System.currentTimeMillis());
    }
    
    // ========== ENERJİ KALKANI (FORCE FIELD) ==========
    
    public boolean isShieldActive(Location loc) {
        return activeShields.containsKey(loc);
    }
    
    public void activateShield(Location center) {
        int radius = 5;
        List<Block> shieldBlocks = new ArrayList<>();
        
        // 5 Blok yarıçapında küre oluştur
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Küre kontrolü
                    if (Math.sqrt(x*x + y*y + z*z) <= radius) {
                        Block b = center.clone().add(x, y, z).getBlock();
                        
                        // Sadece hava olan yerlere cam koy (Mevcut blokları bozma)
                        if (b.getType() == Material.AIR) {
                            b.setType(Material.TINTED_GLASS); // Işık geçirmeyen cam
                            shieldBlocks.add(b);
                            
                            // Metadata ekle ki normal bloklardan ayırt edelim
                            b.setMetadata("ForceFieldBlock", new FixedMetadataValue(plugin, true));
                            
                            // Efekt
                            center.getWorld().spawnParticle(
                                org.bukkit.Particle.END_ROD, 
                                b.getLocation().add(0.5, 0.5, 0.5), 
                                1
                            );
                        }
                    }
                }
            }
        }
        
        // Blokları kaydet (Jeneratör kırılınca silmek için)
        activeShields.put(center, shieldBlocks);
    }
    
    public boolean removeShield(Location center) {
        if (!activeShields.containsKey(center)) return false;
        
        List<Block> blocks = activeShields.get(center);
        
        // Tüm kalkan bloklarını kaldır
        for (Block shieldBlock : blocks) {
            if (shieldBlock.hasMetadata("ForceFieldBlock")) {
                shieldBlock.setType(Material.AIR);
            }
        }
        
        activeShields.remove(center);
        return true;
    }
    
    public boolean isInsideShield(Location loc) {
        for (Map.Entry<Location, List<Block>> entry : activeShields.entrySet()) {
            Location center = entry.getKey();
            if (loc.distance(center) <= 5) {
                return true;
            }
        }
        return false;
    }
    
    // ========== KATEGORİ 2: KLAN ÖZEL YAPILAR ==========
    
    /**
     * Can Tapınağı yapısı kontrolü (3x3 Altın Bloğu + Ortada Beacon)
     */
    public boolean isShrineStructure(Block center) {
        // Etrafında 3x3 Altın Bloğu kontrolü
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Merkez bloğu (Beacon) atla
                
                Block checkBlock = center.getRelative(x, -1, z); // Beacon'ın altındaki bloklar
                if (checkBlock.getType() != Material.GOLD_BLOCK) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean createHealingShrine(Location shrineLoc, Player player) {
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return false;
        
        Block block = shrineLoc.getBlock();
        healingShrines.put(shrineLoc, playerClan.getId());
        
        // Metadata ile işaretle
        block.setMetadata("HealingShrine", new FixedMetadataValue(plugin, true));
        block.setMetadata("ShrineOwner", new FixedMetadataValue(plugin, playerClan.getId().toString()));
        
        return true;
    }
    
    public void removeHealingShrine(Location shrineLoc) {
        healingShrines.remove(shrineLoc);
    }
    
    /**
     * Totem yapısı kontrolü (2x2 blok + ortada özel item)
     */
    public boolean isTotemStructure(Block center, Material baseMaterial) {
        // 2x2 blok kontrolü (merkez hariç)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // Merkez bloğu atla
                
                Block checkBlock = center.getRelative(x, -1, z);
                if (checkBlock.getType() != baseMaterial) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean createPowerTotem(Location totemLoc, Player player) {
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return false;
        
        // Zaten aktif mi?
        if (powerTotems.containsKey(totemLoc)) return false;
        
        Block block = totemLoc.getBlock();
        powerTotems.put(totemLoc, playerClan.getId());
        
        block.setMetadata("PowerTotem", new FixedMetadataValue(plugin, true));
        block.setMetadata("TotemOwner", new FixedMetadataValue(plugin, playerClan.getId().toString()));
        
        return true;
    }
    
    public void removePowerTotem(Location totemLoc) {
        powerTotems.remove(totemLoc);
    }
    
    public boolean createSpeedCircle(Location circleLoc, Player player) {
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return false;
        
        // Zaten aktif mi?
        if (speedCircles.containsKey(circleLoc)) return false;
        
        Block block = circleLoc.getBlock();
        speedCircles.put(circleLoc, playerClan.getId());
        
        block.setMetadata("SpeedCircle", new FixedMetadataValue(plugin, true));
        block.setMetadata("CircleOwner", new FixedMetadataValue(plugin, playerClan.getId().toString()));
        
        return true;
    }
    
    public void removeSpeedCircle(Location circleLoc) {
        speedCircles.remove(circleLoc);
    }
    
    public boolean createDefenseWall(Location wallLoc, Player player) {
        Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return false;
        
        // Zaten aktif mi?
        if (defenseWalls.containsKey(wallLoc)) return false;
        
        Block block = wallLoc.getBlock();
        defenseWalls.put(wallLoc, playerClan.getId());
        
        block.setMetadata("DefenseWall", new FixedMetadataValue(plugin, true));
        block.setMetadata("WallOwner", new FixedMetadataValue(plugin, playerClan.getId().toString()));
        
        return true;
    }
    
    public void removeDefenseWall(Location wallLoc) {
        defenseWalls.remove(wallLoc);
    }
    
    // ========== GETTER METODLARI (BuffTask için) ==========
    
    public Map<Location, UUID> getAllHealingShrines() {
        return new HashMap<>(healingShrines);
    }
    
    public Map<Location, UUID> getAllPowerTotems() {
        return new HashMap<>(powerTotems);
    }
    
    public Map<Location, UUID> getAllSpeedCircles() {
        return new HashMap<>(speedCircles);
    }
    
    public Map<Location, UUID> getAllDefenseWalls() {
        return new HashMap<>(defenseWalls);
    }
    
    public boolean isHealingShrine(Location loc) {
        Block block = loc.getBlock();
        return block.hasMetadata("HealingShrine");
    }
    
    /**
     * Oyuncu çıkışında cooldown'ları temizle (Memory leak önleme)
     */
    public void clearPlayerCooldowns(UUID playerId) {
        catapultCooldowns.remove(playerId);
        ballistaCooldowns.remove(playerId);
    }
}
