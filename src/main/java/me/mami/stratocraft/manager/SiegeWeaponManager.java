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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Savaş Alanı Yapıları Yöneticisi
 */
public class SiegeWeaponManager {
    private final Main plugin;
    private final ClanManager clanManager;

    // KATEGORİ 1: Herkesin kullanabildiği yapılar
    private final Map<Location, List<Block>> activeShields = new HashMap<>();
    private final Map<UUID, Long> catapultCooldowns = new HashMap<>();
    private final Map<Location, Long> lavaFountainCooldowns = new HashMap<>();
    private final Map<Location, Long> poisonDispenserCooldowns = new HashMap<>();

    // Mancınık ve Balista binekleri
    private final Map<UUID, org.bukkit.entity.ArmorStand> catapultMounts = new HashMap<>();
    private final Map<UUID, org.bukkit.entity.ArmorStand> ballistaMounts = new HashMap<>();

    // Balista Şarjör Sistemi
    private final Map<Location, Integer> ballistaAmmo = new HashMap<>();
    private final Map<Location, Long> ballistaReloads = new HashMap<>();

    private static final long CATAPULT_COOLDOWN = 10000;
    private static final long LAVA_FOUNTAIN_COOLDOWN = 5000;
    private static final long POISON_DISPENSER_COOLDOWN = 8000;

    // KATEGORİ 2: Klan özel yapılar
    private final Map<Location, UUID> healingShrines = new HashMap<>();
    private final Map<Location, UUID> powerTotems = new HashMap<>();
    private final Map<Location, UUID> speedCircles = new HashMap<>();
    private final Map<Location, UUID> defenseWalls = new HashMap<>();

    // Nişangah task ID
    private int aimingTaskId = -1;

    // Son binme zamanı (anında ateş etmeyi önlemek için)
    private final Map<UUID, Long> lastMountTime = new HashMap<>();

    public SiegeWeaponManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    // ========== SHIELD SYSTEM ==========

    public void createShield(Location center, int radius) {
        List<Block> shieldBlocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance >= radius - 0.5 && distance <= radius) {
                        Block b = center.getWorld().getBlockAt(center.getBlockX() + x, center.getBlockY() + y,
                                center.getBlockZ() + z);
                        if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) {
                            b.setType(Material.GLASS);
                            shieldBlocks.add(b);
                            b.setMetadata("ForceFieldBlock", new FixedMetadataValue(plugin, true));
                            center.getWorld().spawnParticle(org.bukkit.Particle.END_ROD,
                                    b.getLocation().add(0.5, 0.5, 0.5), 1);
                        }
                    }
                }
            }
        }
        activeShields.put(center, shieldBlocks);
    }

    public void activateShield(Location center) {
        createShield(center, 5);
    }

    public boolean removeShield(Location center) {
        if (!activeShields.containsKey(center))
            return false;
        List<Block> blocks = activeShields.remove(center);
        for (Block b : blocks) {
            if (b.getType() == Material.GLASS) {
                b.setType(Material.AIR);
                b.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, b.getLocation().add(0.5, 0.5, 0.5), 5,
                        Material.GLASS.createBlockData());
            }
        }
        return true;
    }

    public boolean isShieldActive(Location loc) {
        return activeShields.containsKey(loc);
    }

    public boolean isInsideShield(Location loc) {
        Block b = loc.getBlock();
        return b.hasMetadata("ForceFieldBlock");
    }

    // ========== CLAN STRUCTURES ==========

    public boolean isShrineStructure(Block center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;
                if (center.getRelative(x, -1, z).getType() != Material.GOLD_BLOCK)
                    return false;
            }
        }
        return true;
    }

    public boolean createHealingShrine(Location loc, Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null)
            return false;
        healingShrines.put(loc, clan.getId());
        loc.getBlock().setMetadata("HealingShrine", new FixedMetadataValue(plugin, true));
        return true;
    }

    public void removeHealingShrine(Location loc) {
        healingShrines.remove(loc);
    }

    public boolean isTotemStructure(Block center, Material mat) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue;
                if (center.getRelative(x, -1, z).getType() != mat)
                    return false;
            }
        }
        return true;
    }

    public boolean createPowerTotem(Location loc, Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || powerTotems.containsKey(loc))
            return false;
        powerTotems.put(loc, clan.getId());
        loc.getBlock().setMetadata("PowerTotem", new FixedMetadataValue(plugin, true));
        return true;
    }

    public void removePowerTotem(Location loc) {
        powerTotems.remove(loc);
    }

    public boolean createSpeedCircle(Location loc, Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || speedCircles.containsKey(loc))
            return false;
        speedCircles.put(loc, clan.getId());
        loc.getBlock().setMetadata("SpeedCircle", new FixedMetadataValue(plugin, true));
        return true;
    }

    public void removeSpeedCircle(Location loc) {
        speedCircles.remove(loc);
    }

    public boolean createDefenseWall(Location loc, Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null || defenseWalls.containsKey(loc))
            return false;
        defenseWalls.put(loc, clan.getId());
        loc.getBlock().setMetadata("DefenseWall", new FixedMetadataValue(plugin, true));
        return true;
    }

    public void removeDefenseWall(Location loc) {
        defenseWalls.remove(loc);
    }

    // Getters for BuffTask
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

    // ========== CATAPULT & BALLISTA ==========

    public boolean isCatapultStructure(Block base) {
        if (base.getType() != Material.STONE_BRICK_STAIRS && base.getType() != Material.COBBLESTONE_STAIRS)
            return false;
        int count = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (base.getRelative(x, -1, z).getType() == Material.STONE_BRICK_STAIRS)
                    count++;
            }
        }
        return count >= 9;
    }

    public boolean isBallistaStructure(Block dispenser) {
        // Dispenser kontrolü
        if (dispenser.getType() != Material.DISPENSER)
            return false;

        // Alt blok: Stone Brick Slab veya Stone Slab
        Block below = dispenser.getRelative(BlockFace.DOWN);
        if (below.getType() != Material.STONE_BRICK_SLAB && below.getType() != Material.STONE_SLAB)
            return false;

        // 4 yan: Iron Bars (Kuzey, Güney, Doğu, Batı)
        int ironBarsCount = 0;
        if (dispenser.getRelative(BlockFace.NORTH).getType() == Material.IRON_BARS)
            ironBarsCount++;
        if (dispenser.getRelative(BlockFace.SOUTH).getType() == Material.IRON_BARS)
            ironBarsCount++;
        if (dispenser.getRelative(BlockFace.EAST).getType() == Material.IRON_BARS)
            ironBarsCount++;
        if (dispenser.getRelative(BlockFace.WEST).getType() == Material.IRON_BARS)
            ironBarsCount++;

        // En az 4 tarafta Iron Bars olmalı
        return ironBarsCount >= 4;
    }

    public void mountCatapult(Player player, Block catapult) {
        if (isMounted(player)) {
            dismountCatapult(player);
            dismountBallista(player);
            return;
        }
        Location loc = catapult.getLocation().add(0.5, 0.5, 0.5);
        org.bukkit.entity.ArmorStand mount = catapult.getWorld().spawn(loc, org.bukkit.entity.ArmorStand.class);
        mount.setVisible(false);
        mount.setGravity(false);
        mount.setInvulnerable(true);
        mount.setSmall(true);
        mount.addPassenger(player);
        catapultMounts.put(player.getUniqueId(), mount);
        lastMountTime.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§a§lMANCINIĞA BİNDİN!");
        startAimingTask();
    }

    public void dismountCatapult(Player player) {
        org.bukkit.entity.ArmorStand mount = catapultMounts.remove(player.getUniqueId());
        if (mount != null)
            mount.remove();
        stopAimingTask();
    }

    public void mountBallista(Player player, Block ballista) {
        // Yapı kontrolü
        if (!isBallistaStructure(ballista)) {
            player.sendMessage(
                    "§cGeçerli bir Balista yapısı değil! (Alt: Taş Levha, Orta: Fırlatıcı, 4 Yan: Demir Parmaklık)");
            return;
        }

        if (isMounted(player)) {
            dismountCatapult(player);
            dismountBallista(player);
            return;
        }
        // Normal pozisyon - Iron Bars yanlarda olduğu için görüşü engellemez
        Location loc = ballista.getLocation().add(0.5, 0.0, 0.5);
        org.bukkit.entity.ArmorStand mount = ballista.getWorld().spawn(loc, org.bukkit.entity.ArmorStand.class);
        mount.setVisible(false);
        mount.setGravity(false);
        mount.setInvulnerable(true);
        mount.setSmall(true);
        mount.addPassenger(player);
        ballistaMounts.put(player.getUniqueId(), mount);
        lastMountTime.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§a§lBALİSTAYA BİNDİN!");
        int ammo = ballistaAmmo.getOrDefault(ballista.getLocation(), 30);
        player.sendMessage("§eMermi: " + ammo + "/30");
        startAimingTask();
    }

    public void dismountBallista(Player player) {
        org.bukkit.entity.ArmorStand mount = ballistaMounts.remove(player.getUniqueId());
        if (mount != null)
            mount.remove();
        stopAimingTask();
    }

    public boolean isMounted(Player player) {
        return catapultMounts.containsKey(player.getUniqueId()) || ballistaMounts.containsKey(player.getUniqueId());
    }

    public org.bukkit.entity.ArmorStand getMount(Player player) {
        if (catapultMounts.containsKey(player.getUniqueId()))
            return catapultMounts.get(player.getUniqueId());
        if (ballistaMounts.containsKey(player.getUniqueId()))
            return ballistaMounts.get(player.getUniqueId());
        return null;
    }

    private void startAimingTask() {
        if (aimingTaskId != -1)
            return;
        aimingTaskId = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (UUID uuid : catapultMounts.keySet()) {
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null)
                    showAimingLine(p, 5.0, true);
            }
            for (UUID uuid : ballistaMounts.keySet()) {
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null)
                    showAimingLine(p, 5.0, false);
            }
        }, 0L, 2L);
    }

    private void showAimingLine(Player player, double speed, boolean isCurved) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        Location current = start.clone().add(dir.clone().multiply(1.0));
        Vector velocity = dir.clone().multiply(speed);

        for (int i = 0; i < 100; i++) {
            current.add(velocity);
            velocity.multiply(0.99);
            if (isCurved) {
                velocity.multiply(0.99);
                velocity.setY(velocity.getY() - 0.04);
            } else {
                velocity.setY(velocity.getY() - 0.05);
            }

            boolean isTooClose = i < 5;
            org.bukkit.Particle.DustOptions opts = isTooClose
                    ? new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 0.5f)
                    : new org.bukkit.Particle.DustOptions(org.bukkit.Color.WHITE, 0.5f);
            player.spawnParticle(org.bukkit.Particle.REDSTONE, current, 1, opts);
            if (current.getBlock().getType().isSolid())
                break;
        }
    }

    private void stopAimingTask() {
        if (aimingTaskId != -1 && catapultMounts.isEmpty() && ballistaMounts.isEmpty()) {
            org.bukkit.Bukkit.getScheduler().cancelTask(aimingTaskId);
            aimingTaskId = -1;
        }
    }

    public boolean canFireCatapult(Player player) {
        if (!catapultCooldowns.containsKey(player.getUniqueId()))
            return true;
        return System.currentTimeMillis() - catapultCooldowns.get(player.getUniqueId()) >= CATAPULT_COOLDOWN;
    }

    public long getCatapultCooldownRemaining(Player player) {
        if (!catapultCooldowns.containsKey(player.getUniqueId()))
            return 0;
        return Math.max(0,
                (CATAPULT_COOLDOWN - (System.currentTimeMillis() - catapultCooldowns.get(player.getUniqueId())))
                        / 1000);
    }

    public void fireCatapult(Player player, Block catapult) {
        // Binme sonrası 500ms cooldown (anında ateş etmeyi önle)
        if (lastMountTime.containsKey(player.getUniqueId())) {
            long timeSinceMount = System.currentTimeMillis() - lastMountTime.get(player.getUniqueId());
            if (timeSinceMount < 500) {
                return; // Sessizce engelle
            }
        }

        if (!canFireCatapult(player)) {
            player.sendMessage("§cMancınık soğumadı! " + getCatapultCooldownRemaining(player) + "s");
            return;
        }

        Vector direction;
        Location spawnLoc;

        if (isMounted(player)) {
            direction = player.getLocation().getDirection().normalize().multiply(5.0); // 2x Range
            if (player.getLocation().getPitch() > 60) {
                player.sendMessage("§cÇok yakına ateş edemezsin!");
                return;
            }
            spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection());
        } else {
            if (!(catapult.getBlockData() instanceof Directional))
                return;
            BlockFace face = ((Directional) catapult.getBlockData()).getFacing();
            direction = new Vector(face.getModX(), 0.8, face.getModZ()).normalize().multiply(5.0);
            spawnLoc = catapult.getLocation().add(0.5, 1.5, 0.5);
        }

        FallingBlock ammo = player.getWorld().spawnFallingBlock(spawnLoc, Material.MAGMA_BLOCK.createBlockData());
        ammo.setVelocity(direction);
        ammo.setDropItem(false);
        ammo.setHurtEntities(true);
        ammo.setMetadata("SiegeAmmo", new FixedMetadataValue(plugin, true));

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!ammo.isValid() || ammo.isDead() || ticks > 200) {
                    cancel();
                    return;
                }

                // Anlık çarpışma kontrolü
                Location current = ammo.getLocation();
                Vector velocity = ammo.getVelocity();
                Location next = current.clone().add(velocity);

                if (current.getBlock().getType().isSolid() ||
                        next.getBlock().getType().isSolid() ||
                        ammo.isOnGround() ||
                        velocity.length() < 0.1) {

                    triggerExplosion(current);
                    ammo.remove();
                    cancel();
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);

        player.getWorld().playSound(spawnLoc, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 0.5f);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, spawnLoc, 1);
        catapultCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean canFireBallista(Location loc) {
        return !ballistaReloads.containsKey(loc) || System.currentTimeMillis() >= ballistaReloads.get(loc);
    }

    public long getBallistaCooldownRemaining(Location loc) {
        if (!ballistaReloads.containsKey(loc))
            return 0;
        return Math.max(0, (ballistaReloads.get(loc) - System.currentTimeMillis()) / 1000);
    }

    public void fireBallista(Player player, Block ballista) {
        // Binme sonrası 500ms cooldown (anında ateş etmeyi önle)
        if (lastMountTime.containsKey(player.getUniqueId())) {
            long timeSinceMount = System.currentTimeMillis() - lastMountTime.get(player.getUniqueId());
            if (timeSinceMount < 500) {
                return; // Sessizce engelle
            }
        }

        Location loc = ballista.getLocation();
        if (ballistaReloads.containsKey(loc)) {
            if (System.currentTimeMillis() < ballistaReloads.get(loc)) {
                player.sendMessage("§eBalista yeniden dolduruluyor... " + getBallistaCooldownRemaining(loc) + "s");
                return;
            } else {
                ballistaReloads.remove(loc);
                ballistaAmmo.put(loc, 30);
                player.sendMessage("§a§lBALİSTA HAZIR!");
            }
        }

        int ammo = ballistaAmmo.getOrDefault(loc, 30);
        if (ammo <= 0) {
            ballistaReloads.put(loc, System.currentTimeMillis() + 15000);
            player.sendMessage("§c§lŞARJÖR BİTTİ! YENİDEN DOLDURULUYOR (15s)...");
            player.getWorld().playSound(loc, Sound.BLOCK_PISTON_EXTEND, 1.0f, 0.5f);
            return;
        }

        ballistaAmmo.put(loc, ammo - 1);

        Vector direction = player.getLocation().getDirection().normalize().multiply(5.0); // 2x Range
        Location spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection());

        Arrow arrow = player.getWorld().spawn(spawnLoc, Arrow.class);
        arrow.setVelocity(direction);
        arrow.setShooter(player);
        arrow.setDamage(2.5); // 1/4 Damage
        arrow.setMetadata("BallistaArrow", new FixedMetadataValue(plugin, true));

        player.getWorld().playSound(loc, Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.5f);
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent("§eMermi: " + (ammo - 1) + "/30"));
    }

    public void triggerExplosion(Location loc) {
        loc.getWorld().createExplosion(loc, 4.0f, true);
        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, loc, 5);
        for (Entity nearby : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
            if (nearby instanceof Player) {
                ((Player) nearby).sendMessage("§c§lMANCINIK MERMİSİNE YAKALANDIN!");
            }
        }
    }

    public void playConstructionEffect(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 20) {
                    cancel();
                    return;
                }
                loc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, loc.clone().add(0.5, 0.5, 0.5), 10, 0.5, 0.5,
                        0.5, 0.05);
                count += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    // ========== OTHER WEAPONS ==========

    public boolean canActivateLavaFountain(Location loc) {
        if (!lavaFountainCooldowns.containsKey(loc))
            return true;
        return System.currentTimeMillis() - lavaFountainCooldowns.get(loc) >= LAVA_FOUNTAIN_COOLDOWN;
    }

    public long getLavaFountainCooldownRemaining(Location loc) {
        if (!lavaFountainCooldowns.containsKey(loc))
            return 0;
        return Math.max(0,
                (LAVA_FOUNTAIN_COOLDOWN - (System.currentTimeMillis() - lavaFountainCooldowns.get(loc))) / 1000);
    }

    public void activateLavaFountain(Location loc) {
        for (int i = 0; i < 10; i++) {
            Vector dir = new Vector(Math.random() - 0.5, Math.random() + 0.5, Math.random() - 0.5).normalize()
                    .multiply(0.5);
            org.bukkit.entity.SmallFireball fb = loc.getWorld().spawn(loc.clone().add(0, 1, 0),
                    org.bukkit.entity.SmallFireball.class);
            fb.setVelocity(dir);
        }
        loc.getWorld().playSound(loc, Sound.BLOCK_LAVA_POP, 1.0f, 0.5f);
        lavaFountainCooldowns.put(loc, System.currentTimeMillis());
    }

    public boolean canActivatePoisonDispenser(Location loc) {
        if (!poisonDispenserCooldowns.containsKey(loc))
            return true;
        return System.currentTimeMillis() - poisonDispenserCooldowns.get(loc) >= POISON_DISPENSER_COOLDOWN;
    }

    public long getPoisonDispenserCooldownRemaining(Location loc) {
        if (!poisonDispenserCooldowns.containsKey(loc))
            return 0;
        return Math.max(0,
                (POISON_DISPENSER_COOLDOWN - (System.currentTimeMillis() - poisonDispenserCooldowns.get(loc))) / 1000);
    }

    public void activatePoisonDispenser(Location loc) {
        loc.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, loc.clone().add(0.5, 1, 0.5), 100, 3, 3, 3, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5, 5, 5)) {
            if (entity instanceof Player) {
                ((Player) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.POISON, 100, 1, false, false));
            }
        }
        loc.getWorld().playSound(loc, Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 1.0f, 0.5f);
        poisonDispenserCooldowns.put(loc, System.currentTimeMillis());
    }

    public void clearPlayerCooldowns(UUID uuid) {
        Player player = org.bukkit.Bukkit.getPlayer(uuid);
        if (player != null) {
            dismountCatapult(player);
            dismountBallista(player);
        }
    }
}
