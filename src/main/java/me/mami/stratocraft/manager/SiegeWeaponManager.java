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
 * KATEGORİ 2: Klan özel yapılar (Klan alanı dışına kurulabilir, sadece klan
 * üyelerine etki eder)
 * - Can Tapınağı (Healing Shrine): Sadece kurucu klanın üyelerine iyileştirme
 * verir
 * - Güç Totemi (Power Totem): Klan üyelerine güç buff verir
 * - Hız Çemberi (Speed Circle): Klan üyelerine hız buff verir
 * - Savunma Duvarı (Defense Wall): Klan üyelerine direnç buff verir
 */
public class SiegeWeaponManager {
    private final Main plugin;
    private final ClanManager clanManager;

    // KATEGORİ 1: Herkesin kullanabildiği yapılar
    private final Map<Location, List<Block>> activeShields = new HashMap<>(); // Jeneratör -> Kalkan blokları
    private final Map<UUID, Long> catapultCooldowns = new HashMap<>(); // Player UUID -> Cooldown
    private final Map<Location, Long> ballistaCooldowns = new HashMap<>();
    private final Map<Location, Long> lavaFountainCooldowns = new HashMap<>();
    private final Map<Location, Long> poisonDispenserCooldowns = new HashMap<>();

    // Mancınık binekleri: Player UUID -> ArmorStand (Binek)
    private final Map<UUID, org.bukkit.entity.ArmorStand> catapultMounts = new HashMap<>();
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

    /**
     * Ozon Kalkanı oluştur (Hollow shell)
     */
    public void createShield(Location center, int radius) {
        List<Block> shieldBlocks = new ArrayList<>();

        // Hollow sphere - only shell
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);

                    // Check if point is on the shell (within radius but not filled)
                    if (distance >= radius - 0.5 && distance <= radius) {
                        Block b = center.getWorld().getBlockAt(
                                center.getBlockX() + x,
                                center.getBlockY() + y,
                                center.getBlockZ() + z);

                        // Only replace air blocks
                        if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) {
                            b.setType(Material.GLASS);
                            shieldBlocks.add(b);

                            // Metadata ekle ki normal bloklardan ayırt edelim
                            b.setMetadata("ForceFieldBlock", new FixedMetadataValue(plugin, true));

                            // Efekt
                            center.getWorld().spawnParticle(org.bukkit.Particle.END_ROD,
                                    b.getLocation().add(0.5, 0.5, 0.5), 1);
                        }
                    }
                }
            }
        }

        // Blokları kaydet (Jeneratör kırılınca silmek için)
        activeShields.put(center, shieldBlocks);
    }

    /**
     * Kalkanı aktif et (Varsayılan yarıçap ile)
     */
    public void activateShield(Location center) {
        createShield(center, 5);
    }

    public boolean removeShield(Location center) {
        if (!activeShields.containsKey(center))
            return false;

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
                if (x == 0 && z == 0)
                    continue; // Merkez bloğu (Beacon) atla

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
        if (playerClan == null)
            return false;

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
                if (x == 0 && z == 0)
                    continue; // Merkez bloğu atla

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
        if (playerClan == null)
            return false;

        // Zaten aktif mi?
        if (powerTotems.containsKey(totemLoc))
            return false;

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
        if (playerClan == null)
            return false;

        // Zaten aktif mi?
        if (speedCircles.containsKey(circleLoc))
            return false;

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
        if (playerClan == null)
            return false;

        // Zaten aktif mi?
        if (defenseWalls.containsKey(wallLoc))
            return false;

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

    // ========== CATAPULT & SIEGE WEAPONS ==========

    public boolean isCatapultStructure(Block block) {
        // Check for 3x3 base of stone brick stairs
        if (!(block.getBlockData() instanceof org.bukkit.block.data.type.Stairs)) {
            return false;
        }

        Location base = block.getLocation();
        int stairCount = 0;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block check = base.clone().add(x, -1, z).getBlock();
                if (check.getType() == Material.STONE_BRICK_STAIRS) {
                    stairCount++;
                }
            }
        }

        return stairCount >= 9; // All 9 blocks must be stairs
    }

    // Nişan alma taskı
    private int aimingTaskId = -1;

    public void mountCatapult(Player player, Block catapult) {
        if (catapultMounts.containsKey(player.getUniqueId())) {
            // Zaten biniliyse dismount yap
            dismountCatapult(player);
            return;
        }

        // DÜZGÜN OTURMA POZİSYONU (0.5 blok yukarıda, 1 değil!)
        Location mountLoc = catapult.getLocation().add(0.5, 0.5, 0.5);
        org.bukkit.entity.ArmorStand mount = catapult.getWorld().spawn(mountLoc, org.bukkit.entity.ArmorStand.class);
        mount.setVisible(false);
        mount.setGravity(false);
        mount.setInvulnerable(true);
        mount.setSmall(true); // Küçük ArmorStand - daha doğal oturma
        mount.addPassenger(player);

        catapultMounts.put(player.getUniqueId(), mount);
        player.sendMessage("§a§lMANCINIĞA BİNDİN!");
        player.sendMessage("§7Sol Tık = Ateş Et  |  Shift + Sağ Tık = İn");

        startAimingTask();
    }

    public void dismountCatapult(Player player) {
        org.bukkit.entity.ArmorStand mount = catapultMounts.remove(player.getUniqueId());
        if (mount != null) {
            mount.remove();
            player.sendMessage("§7Mancınıktan indin.");
        }

        if (catapultMounts.isEmpty()) {
            stopAimingTask();
        }
    }

    private void startAimingTask() {
        if (aimingTaskId != -1)
            return;

        aimingTaskId = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (UUID playerId : catapultMounts.keySet()) {
                Player player = org.bukkit.Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    // Nişan çizgisi (Particle)
                    Location start = player.getEyeLocation();
                    Vector dir = start.getDirection().normalize();

                    for (double i = 0.5; i <= 20; i += 0.5) {
                        Location point = start.clone().add(dir.clone().multiply(i));
                        // Parabolik kavis efekti (basitçe aşağı eğim ekle)
                        point.add(0, -0.02 * i * i, 0);

                        player.spawnParticle(org.bukkit.Particle.REDSTONE, point, 1,
                                new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 0.5f));

                        if (point.getBlock().getType().isSolid())
                            break;
                    }
                }
            }
        }, 0L, 2L); // Her 2 tickte bir (0.1 saniye)
    }

    private void stopAimingTask() {
        if (aimingTaskId != -1) {
            org.bukkit.Bukkit.getScheduler().cancelTask(aimingTaskId);
            aimingTaskId = -1;
        }
    }

    public boolean isMounted(Player player) {
        return catapultMounts.containsKey(player.getUniqueId());
    }

    public org.bukkit.entity.ArmorStand getCatapultMount(Player player) {
        return catapultMounts.get(player.getUniqueId());
    }

    public boolean canFireCatapult(Player player) {
        if (!catapultCooldowns.containsKey(player.getUniqueId())) {
            return true;
        }
        return System.currentTimeMillis() - catapultCooldowns.get(player.getUniqueId()) >= CATAPULT_COOLDOWN;
    }

    public long getCatapultCooldownRemaining(Player player) {
        if (!catapultCooldowns.containsKey(player.getUniqueId())) {
            return 0;
        }
        long remaining = CATAPULT_COOLDOWN - (System.currentTimeMillis() - catapultCooldowns.get(player.getUniqueId()));
        return Math.max(0, remaining / 1000); // Convert to seconds
    }

    public void fireCatapult(Player player, Block catapult) {
        // 1. Yönü belirle
        Vector directionVector;

        if (isMounted(player)) {
            // Biniliyse oyuncunun baktığı yöne
            directionVector = player.getLocation().getDirection().normalize().multiply(2.5);
        } else {
            // Dışarıdansa mancınığın baktığı yöne
            if (!(catapult.getBlockData() instanceof org.bukkit.block.data.Directional)) {
                player.sendMessage("§cMancınık yönü belirlenemiyor!");
                return;
            }
            org.bukkit.block.BlockFace face = ((org.bukkit.block.data.Directional) catapult.getBlockData()).getFacing();
            directionVector = new Vector(face.getModX(), 0.8, face.getModZ()).normalize().multiply(2.5);
        }

        // 2. Mermiyi oluştur (MAGMA_BLOCK)
        Location spawnLoc = catapult.getLocation().add(0.5, 1.5, 0.5);
        org.bukkit.entity.FallingBlock ammo = player.getWorld().spawnFallingBlock(
                spawnLoc,
                Material.MAGMA_BLOCK.createBlockData());

        // 3. Fizik uygula
        ammo.setVelocity(directionVector);
        ammo.setDropItem(false); // Yere düşünce eşya olmasın
        ammo.setHurtEntities(true); // Varlıklara zarar verebilir

        // 4. Mermiyi işaretle (Patlama için - EntityChangeBlockEvent'te yakalanacak)
        ammo.setMetadata("SiegeAmmo", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

        // Efekt ve ses
        player.getWorld().playSound(spawnLoc, org.bukkit.Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, spawnLoc, 1);
        player.sendMessage("§c§lMANCINIK ATEŞ ETTİ!");

        // Cooldown kaydet (UUID kullan)
        catapultCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isShieldActive(Location location) {
        return activeShields.containsKey(location);
    }

    public boolean canFireBallista(Location location) {
        if (!ballistaCooldowns.containsKey(location)) {
            return true;
        }
        return System.currentTimeMillis() - ballistaCooldowns.get(location) >= BALLISTA_COOLDOWN;
    }

    public long getBallistaCooldownRemaining(Location location) {
        if (!ballistaCooldowns.containsKey(location)) {
            return 0;
        }
        long remaining = BALLISTA_COOLDOWN - (System.currentTimeMillis() - ballistaCooldowns.get(location));
        return Math.max(0, remaining / 1000);
    }

    public void fireBallista(Player player, Block ballista) {
        Location launchLoc = ballista.getLocation().add(0.5, 1.2, 0.5); // Biraz daha yukarıdan
        Vector direction = player.getLocation().getDirection().normalize();

        // Okun blok içinde doğmasını engellemek için ileri taşı
        launchLoc.add(direction.clone().multiply(1.5));

        Arrow arrow = launchLoc.getWorld().spawnArrow(launchLoc, direction.multiply(3), 3.0f, 0);
        arrow.setDamage(10.0);
        arrow.setCritical(true);
        arrow.setShooter(player); // Atanı ayarla

        // Metadata ekle ki ProjectileHitEvent handler'da tespit edilsin
        arrow.setMetadata("BallistaArrow", new FixedMetadataValue(plugin, true));

        launchLoc.getWorld().playSound(launchLoc, Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.5f);
        ballistaCooldowns.put(ballista.getLocation(), System.currentTimeMillis());
    }

    public boolean canActivateLavaFountain(Location location) {
        if (!lavaFountainCooldowns.containsKey(location)) {
            return true;
        }
        return System.currentTimeMillis() - lavaFountainCooldowns.get(location) >= LAVA_FOUNTAIN_COOLDOWN;
    }

    public long getLavaFountainCooldownRemaining(Location location) {
        if (!lavaFountainCooldowns.containsKey(location)) {
            return 0;
        }
        long remaining = LAVA_FOUNTAIN_COOLDOWN - (System.currentTimeMillis() - lavaFountainCooldowns.get(location));
        return Math.max(0, remaining / 1000);
    }

    public void activateLavaFountain(Location location) {
        // Spray lava in a cone
        for (int i = 0; i < 10; i++) {
            Vector direction = new Vector(
                    Math.random() - 0.5,
                    Math.random() + 0.5,
                    Math.random() - 0.5).normalize().multiply(0.5);

            org.bukkit.entity.SmallFireball fireball = location.getWorld().spawn(
                    location.clone().add(0, 1, 0),
                    org.bukkit.entity.SmallFireball.class);
            fireball.setVelocity(direction);
        }

        location.getWorld().playSound(location, Sound.BLOCK_LAVA_POP, 1.0f, 0.5f);
        lavaFountainCooldowns.put(location, System.currentTimeMillis());
    }

    public boolean canActivatePoisonDispenser(Location location) {
        if (!poisonDispenserCooldowns.containsKey(location)) {
            return true;
        }
        return System.currentTimeMillis() - poisonDispenserCooldowns.get(location) >= POISON_DISPENSER_COOLDOWN;
    }

    public long getPoisonDispenserCooldownRemaining(Location location) {
        if (!poisonDispenserCooldowns.containsKey(location)) {
            return 0;
        }
        long remaining = POISON_DISPENSER_COOLDOWN
                - (System.currentTimeMillis() - poisonDispenserCooldowns.get(location));
        return Math.max(0, remaining / 1000);
    }

    public void activatePoisonDispenser(Location location) {
        // Spawn poison cloud
        location.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, location.clone().add(0.5, 1, 0.5), 100, 3, 3,
                3, 0);

        // Apply poison to nearby enemies
        for (Entity entity : location.getWorld().getNearbyEntities(location, 5, 5, 5)) {
            if (entity instanceof Player) {
                ((Player) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.POISON, 100, 1, false, false));
            }
        }

        location.getWorld().playSound(location, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 1.0f, 0.5f);
        poisonDispenserCooldowns.put(location, System.currentTimeMillis());
    }

    /**
     * Oyuncu çıkışında cooldown'ları temizle (Memory leak önleme)
     */
    public void clearPlayerCooldowns(java.util.UUID uuid) {
        // Lokasyon bazlı cooldown olduğu için oyuncu çıkınca silmeye gerek yok
        // Ancak binekleri temizle
        Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null) {
            dismountCatapult(player);
        }
    }
}
