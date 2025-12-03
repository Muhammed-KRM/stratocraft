package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Boss Sistemi
 * 
 * 10 farklı boss
 * Her boss için ritüel çağırma
 * Özel hareketler (ateş, yıldırım, patlama, blok fırlatma, zehir vb.)
 * Zayıf noktalar (güçlü bosslar için)
 * Faz sistemi (güçlü bosslar için 2-3 faz)
 */
public class BossManager {
    private final Main plugin;
    private final MobManager mobManager;
    private final DifficultyManager difficultyManager;

    // Aktif bosslar (Entity -> BossData)
    private final Map<UUID, BossData> activeBosses = new HashMap<>();

    // Ritüel cooldown (Location -> Long)
    private final Map<Location, Long> ritualCooldowns = new HashMap<>();
    private static final long RITUAL_COOLDOWN = 60000L; // 1 dakika

    private File bossesFile;
    private FileConfiguration bossesConfig;

    public enum BossType {
        GOBLIN_KING, // Seviye 1 - Goblin Kralı
        ORC_CHIEF, // Seviye 1-2 - Ork Şefi
        TROLL_KING, // Seviye 2 - Troll Kralı
        DRAGON, // Seviye 3 - Ejderha (2 faz)
        TREX, // Seviye 3 - T-Rex
        CYCLOPS, // Seviye 3-4 - Cyclops (2 faz)
        TITAN_GOLEM, // Seviye 4 - Titan Golem (3 faz, zayıf: alev)
        HELL_DRAGON, // Seviye 4 - Cehennem Ejderi (2 faz, zayıf: su)
        HYDRA, // Seviye 4-5 - Hydra (3 faz, zayıf: zehir)
        PHOENIX, // Seviye 4 - Phoenix (2 faz, zayıf: su)
        VOID_DRAGON, // Seviye 5 - Void Dragon (3 faz)
        CHAOS_TITAN, // Seviye 5 - Chaos Titan (3 faz)
        CHAOS_GOD // Seviye 5 - Khaos Tanrısı (3 faz, zayıf: alev, zehir)
    }

    public enum BossWeakness {
        FIRE, // Alev zayıflığı
        WATER, // Su zayıflığı
        POISON, // Zehir zayıflığı
        LIGHTNING // Yıldırım zayıflığı
    }

    public enum BossAbility {
        FIRE_BREATH, // Ateş püskürtme
        EXPLOSION, // Patlama yapma
        LIGHTNING_STRIKE, // Yıldırım atma
        BLOCK_THROW, // Blok fırlatma
        POISON_CLOUD, // Zehir bulutu
        TELEPORT, // Işınlanma
        CHARGE, // Koşu saldırısı
        SUMMON_MINIONS, // Minyon çağırma
        HEAL, // Kendini iyileştirme
        SHOCKWAVE // Şok dalgası
    }

    public static class BossData {
        private final BossType type;
        private final LivingEntity entity;
        private final UUID ownerId; // Ritüel yapan oyuncu
        private int phase;
        private final int maxPhase;
        private final List<BossWeakness> weaknesses;
        private final Map<Integer, List<BossAbility>> phaseAbilities; // Faz -> Hareketler
        private long lastAbilityTime;
        private static final long ABILITY_COOLDOWN = 3000L; // 3 saniye

        public BossData(BossType type, LivingEntity entity, UUID ownerId, int maxPhase,
                List<BossWeakness> weaknesses, Map<Integer, List<BossAbility>> phaseAbilities) {
            this.type = type;
            this.entity = entity;
            this.ownerId = ownerId;
            this.phase = 1;
            this.maxPhase = maxPhase;
            this.weaknesses = weaknesses;
            this.phaseAbilities = phaseAbilities;
            this.lastAbilityTime = System.currentTimeMillis();
        }

        public BossType getType() {
            return type;
        }

        public LivingEntity getEntity() {
            return entity;
        }

        public UUID getOwnerId() {
            return ownerId;
        }

        public int getPhase() {
            return phase;
        }

        public int getMaxPhase() {
            return maxPhase;
        }

        public List<BossWeakness> getWeaknesses() {
            return weaknesses;
        }

        public List<BossAbility> getCurrentAbilities() {
            return phaseAbilities.getOrDefault(phase, new ArrayList<>());
        }

        public long getLastAbilityTime() {
            return lastAbilityTime;
        }

        public void setLastAbilityTime(long time) {
            this.lastAbilityTime = time;
        }

        public void nextPhase() {
            if (phase < maxPhase) {
                phase++;
            }
        }

        public boolean canUseAbility() {
            return System.currentTimeMillis() - lastAbilityTime >= ABILITY_COOLDOWN;
        }
    }

    public BossManager(Main plugin) {
        this.plugin = plugin;
        this.mobManager = plugin.getMobManager();
        this.difficultyManager = plugin.getDifficultyManager();
        loadBosses();
        startBossAbilityTask();
    }

    /**
     * Boss yeteneklerini sürekli kontrol et
     */
    private void startBossAbilityTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (BossData boss : new ArrayList<>(activeBosses.values())) {
                    if (boss.getEntity() == null || boss.getEntity().isDead()) {
                        activeBosses.remove(boss.getEntity().getUniqueId());
                        continue;
                    }

                    // Yetenek kullan
                    if (boss.canUseAbility()) {
                        useRandomAbility(boss);
                    }

                    // Faz kontrolü (can %'sine göre)
                    checkPhaseTransition(boss);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Her saniye
    }

    /**
     * Faz geçişi kontrolü
     */
    private void checkPhaseTransition(BossData boss) {
        if (boss.getPhase() >= boss.getMaxPhase()) {
            return; // Son fazda
        }

        double healthPercent = boss.getEntity().getHealth() / boss.getEntity().getAttribute(
                org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

        // Faz geçişi eşikleri
        if (boss.getMaxPhase() == 2) {
            // 2 fazlı boss: %50'de faz değişir
            if (healthPercent <= 0.5 && boss.getPhase() == 1) {
                boss.nextPhase();
                announcePhaseChange(boss);
            }
        } else if (boss.getMaxPhase() == 3) {
            // 3 fazlı boss: %66 ve %33'te faz değişir
            if (healthPercent <= 0.66 && boss.getPhase() == 1) {
                boss.nextPhase();
                announcePhaseChange(boss);
            } else if (healthPercent <= 0.33 && boss.getPhase() == 2) {
                boss.nextPhase();
                announcePhaseChange(boss);
            }
        }
    }

    /**
     * Faz değişimi duyurusu
     */
    private void announcePhaseChange(BossData boss) {
        String bossName = getBossDisplayName(boss.getType());
        boss.getEntity().getWorld().getPlayers().forEach(p -> {
            if (p.getLocation().distance(boss.getEntity().getLocation()) <= 50) {
                p.sendTitle("§c§l" + bossName, "§eFaz " + boss.getPhase() + "!", 20, 60, 20);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
            }
        });
    }

    /**
     * Rastgele yetenek kullan
     */
    private void useRandomAbility(BossData boss) {
        List<BossAbility> abilities = boss.getCurrentAbilities();
        if (abilities.isEmpty()) {
            return;
        }

        BossAbility ability = abilities.get(new Random().nextInt(abilities.size()));
        executeAbility(boss, ability);
        boss.setLastAbilityTime(System.currentTimeMillis());
    }

    /**
     * Yetenek uygula
     */
    private void executeAbility(BossData boss, BossAbility ability) {
        LivingEntity entity = boss.getEntity();
        if (entity == null || entity.isDead()) {
            return;
        }

        Location loc = entity.getLocation();
        Player target = findNearestPlayer(loc, 30);

        switch (ability) {
            case FIRE_BREATH:
                fireBreath(entity, target != null ? target : null);
                break;
            case EXPLOSION:
                createExplosion(loc);
                break;
            case LIGHTNING_STRIKE:
                strikeLightning(target != null ? target.getLocation() : loc);
                break;
            case BLOCK_THROW:
                throwBlocks(entity, target);
                break;
            case POISON_CLOUD:
                createPoisonCloud(loc);
                break;
            case TELEPORT:
                teleportBoss(entity, target);
                break;
            case CHARGE:
                chargeAttack(entity, target);
                break;
            case SUMMON_MINIONS:
                summonMinions(loc, boss.getType());
                break;
            case HEAL:
                healBoss(entity);
                break;
            case SHOCKWAVE:
                createShockwave(loc);
                break;
        }
    }

    /**
     * Ateş püskürtme
     */
    private void fireBreath(LivingEntity boss, Player target) {
        Location start = boss.getLocation().add(0, 1, 0);
        Vector direction;

        if (target != null) {
            direction = target.getLocation().subtract(start).toVector().normalize();
        } else {
            direction = boss.getLocation().getDirection();
        }

        for (int i = 1; i <= 10; i++) {
            Location fireLoc = start.clone().add(direction.clone().multiply(i * 0.5));
            final Location finalLoc = fireLoc;

            new BukkitRunnable() {
                @Override
                public void run() {
                    finalLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, finalLoc, 5, 0.2, 0.2, 0.2, 0.05);
                    finalLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, finalLoc, 2, 0.1, 0.1, 0.1,
                            0.01);

                    // Oyunculara hasar ver
                    for (Entity nearby : finalLoc.getWorld().getNearbyEntities(finalLoc, 1, 1, 1)) {
                        if (nearby instanceof Player) {
                            ((Player) nearby).setFireTicks(60);
                            ((Player) nearby).damage(2.0, boss);
                        }
                    }
                }
            }.runTaskLater(plugin, i * 2L);
        }

        boss.getWorld().playSound(start, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
    }

    /**
     * Patlama yapma
     */
    private void createExplosion(Location loc) {
        loc.getWorld().createExplosion(loc, 3.0f, false, false);
        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, loc, 1);
    }

    /**
     * Yıldırım atma
     */
    private void strikeLightning(Location target) {
        target.getWorld().strikeLightning(target);
        target.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, target, 20, 0.5, 1, 0.5, 0.1);
    }

    /**
     * Blok fırlatma
     */
    private void throwBlocks(LivingEntity boss, Player target) {
        if (target == null)
            return;

        Location bossLoc = boss.getLocation();
        Location targetLoc = target.getLocation();

        // 3x3 alanından blokları al ve fırlat (merkez hariç)
        int blockCount = 0;
        int maxBlocks = 3; // Maksimum 3 blok fırlat

        for (int x = -1; x <= 1 && blockCount < maxBlocks; x++) {
            for (int z = -1; z <= 1 && blockCount < maxBlocks; z++) {
                // Merkez bloğu atla
                if (x == 0 && z == 0)
                    continue;

                Block block = bossLoc.clone().add(x, -1, z).getBlock();
                if (block.getType().isSolid() && block.getType() != Material.BEDROCK) {
                    FallingBlock fallingBlock = bossLoc.getWorld().spawnFallingBlock(
                            bossLoc.clone().add(0, 2, 0), block.getBlockData());

                    Vector direction = targetLoc.subtract(bossLoc).toVector().normalize();
                    fallingBlock.setVelocity(direction.multiply(0.8));
                    fallingBlock.setHurtEntities(true);

                    block.setType(Material.AIR);
                    blockCount++;
                }
            }
        }
    }

    /**
     * Zehir bulutu
     */
    private void createPoisonCloud(Location loc) {
        for (int i = 0; i < 20; i++) {
            Location cloudLoc = loc.clone().add(
                    (Math.random() - 0.5) * 5,
                    Math.random() * 2,
                    (Math.random() - 0.5) * 5);

            loc.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, cloudLoc, 10, 0.5, 0.5, 0.5, 0);

            // Yakındaki oyunculara zehir ver
            for (Entity nearby : loc.getWorld().getNearbyEntities(cloudLoc, 2, 2, 2)) {
                if (nearby instanceof Player) {
                    ((Player) nearby).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false, false));
                }
            }
        }
    }

    /**
     * Işınlanma
     */
    private void teleportBoss(LivingEntity boss, Player target) {
        if (target == null)
            return;

        Location teleportLoc = target.getLocation().add(
                (Math.random() - 0.5) * 3,
                0,
                (Math.random() - 0.5) * 3);

        // Güvenli konum bul
        teleportLoc.setY(teleportLoc.getWorld().getHighestBlockYAt(teleportLoc) + 1);

        boss.teleport(teleportLoc);
        boss.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, teleportLoc, 50, 1, 1, 1, 0.5);
        boss.getWorld().playSound(teleportLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    /**
     * Koşu saldırısı
     */
    private void chargeAttack(LivingEntity boss, Player target) {
        if (target == null)
            return;

        Vector direction = target.getLocation().subtract(boss.getLocation()).toVector().normalize();
        direction.multiply(1.5);
        direction.setY(0.3);

        boss.setVelocity(direction);
        boss.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, boss.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    /**
     * Minyon çağırma
     */
    private void summonMinions(Location loc, BossType bossType) {
        // Boss tipine göre minyonlar
        switch (bossType) {
            case GOBLIN_KING:
                mobManager.spawnGoblin(loc.clone().add(2, 0, 0));
                mobManager.spawnGoblin(loc.clone().add(-2, 0, 0));
                break;
            case ORC_CHIEF:
                mobManager.spawnOrk(loc.clone().add(2, 0, 0));
                mobManager.spawnOrk(loc.clone().add(-2, 0, 0));
                break;
            case TROLL_KING:
                mobManager.spawnTroll(loc.clone().add(2, 0, 0));
                break;
            // Diğer bosslar için de minyonlar
        }

        loc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, loc, 30, 1, 1, 1, 0.2);
        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
    }

    /**
     * Kendini iyileştirme
     */
    private void healBoss(LivingEntity boss) {
        double maxHealth = boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = boss.getHealth();
        double healAmount = maxHealth * 0.2; // %20 iyileşme

        boss.setHealth(Math.min(maxHealth, currentHealth + healAmount));
        boss.getWorld().spawnParticle(org.bukkit.Particle.HEART, boss.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5,
                0.1);
        boss.getWorld().playSound(boss.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
    }

    /**
     * Şok dalgası
     */
    private void createShockwave(Location loc) {
        for (int radius = 1; radius <= 5; radius++) {
            final int finalRadius = radius;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 360; i += 10) {
                        double angle = Math.toRadians(i);
                        Location waveLoc = loc.clone().add(
                                Math.cos(angle) * finalRadius,
                                0,
                                Math.sin(angle) * finalRadius);

                        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, waveLoc, 1);

                        // Oyunculara hasar ve itme
                        for (Entity nearby : loc.getWorld().getNearbyEntities(waveLoc, 1, 1, 1)) {
                            if (nearby instanceof Player) {
                                Vector push = nearby.getLocation().subtract(loc).toVector().normalize();
                                push.multiply(1.5);
                                push.setY(0.5);
                                nearby.setVelocity(push);
                                ((Player) nearby).damage(3.0);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, radius * 5L);
        }
    }

    /**
     * En yakın oyuncuyu bul
     */
    private Player findNearestPlayer(Location loc, double maxDistance) {
        Player nearest = null;
        double nearestDistance = maxDistance;

        for (Player player : loc.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(loc);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * Boss spawn et (ritüel ile)
     */
    public boolean spawnBossFromRitual(Location loc, BossType type, UUID ownerId) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }

        // Cooldown kontrolü
        Location ritualLoc = loc.clone();
        ritualLoc.setY(ritualLoc.getBlockY());
        if (ritualCooldowns.containsKey(ritualLoc)) {
            long timeLeft = (ritualCooldowns.get(ritualLoc) + RITUAL_COOLDOWN) - System.currentTimeMillis();
            if (timeLeft > 0) {
                return false;
            }
        }

        LivingEntity bossEntity = spawnBossEntity(loc, type);
        if (bossEntity == null) {
            return false;
        }

        // BossData oluştur
        BossData bossData = createBossData(type, bossEntity, ownerId);
        activeBosses.put(bossEntity.getUniqueId(), bossData);

        // Cooldown kaydet
        ritualCooldowns.put(ritualLoc, System.currentTimeMillis());

        // Duyuru
        String bossName = getBossDisplayName(type);
        loc.getWorld().getPlayers().forEach(p -> {
            if (p.getLocation().distance(loc) <= 50) {
                p.sendTitle("§c§l" + bossName, "§eÇağrıldı!", 20, 60, 20);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            }
        });

        saveBosses();
        return true;
    }

    /**
     * Boss entity spawn et
     */
    private LivingEntity spawnBossEntity(Location loc, BossType type) {
        switch (type) {
            case GOBLIN_KING:
                Zombie goblin = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                goblin.setCustomName("§2§lGOBLIN KRALI");
                goblin.setBaby(false);
                if (goblin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    goblin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(150.0);
                }
                goblin.setHealth(150.0);
                return goblin;

            case ORC_CHIEF:
                Zombie orc = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                orc.setCustomName("§c§lORK ŞEFİ");
                if (orc.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    orc.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
                }
                orc.setHealth(200.0);
                return orc;

            case TROLL_KING:
                Zombie troll = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                troll.setCustomName("§5§lTROLL KRALI");
                if (troll.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    troll.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
                }
                troll.setHealth(300.0);
                return troll;

            case DRAGON:
                Phantom dragon = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
                dragon.setCustomName("§4§lEJDERHA");
                dragon.setSize(25);
                if (dragon.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    dragon.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
                }
                dragon.setHealth(500.0);
                return dragon;

            case TREX:
                Ravager trex = (Ravager) loc.getWorld().spawnEntity(loc, EntityType.RAVAGER);
                trex.setCustomName("§c§lT-REX");
                if (trex.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    trex.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(600.0);
                }
                trex.setHealth(600.0);
                return trex;

            case CYCLOPS:
                Giant cyclops = (Giant) loc.getWorld().spawnEntity(loc, EntityType.GIANT);
                cyclops.setCustomName("§6§lTEK GÖZLÜ DEV");
                if (cyclops.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    cyclops.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(700.0);
                }
                cyclops.setHealth(700.0);
                return cyclops;

            case TITAN_GOLEM:
                return mobManager.spawnTitanGolem(loc, null);

            case HELL_DRAGON:
                return mobManager.spawnHellDragon(loc, null);

            case HYDRA:
                return mobManager.spawnHydra(loc);

            case CHAOS_GOD:
                // En güçlü boss - özel entity
                Wither wither = (Wither) loc.getWorld().spawnEntity(loc, EntityType.WITHER);
                wither.setCustomName("§5§lKHAOS TANRISI");
                if (wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(1000.0);
                }
                wither.setHealth(1000.0);
                return wither;

            default:
                return null;
        }
    }

    /**
     * BossData oluştur
     */
    private BossData createBossData(BossType type, LivingEntity entity, UUID ownerId) {
        int maxPhase = getBossMaxPhase(type);
        List<BossWeakness> weaknesses = getBossWeaknesses(type);
        Map<Integer, List<BossAbility>> phaseAbilities = getBossAbilities(type);

        return new BossData(type, entity, ownerId, maxPhase, weaknesses, phaseAbilities);
    }

    /**
     * Boss faz sayısı
     */
    private int getBossMaxPhase(BossType type) {
        switch (type) {
            case GOBLIN_KING:
            case ORC_CHIEF:
            case TROLL_KING:
            case TREX:
                return 1; // Tek faz
            case DRAGON:
            case CYCLOPS:
            case HELL_DRAGON:
                return 2; // 2 faz
            case TITAN_GOLEM:
            case HYDRA:
            case CHAOS_GOD:
                return 3; // 3 faz
            default:
                return 1;
        }
    }

    /**
     * Boss zayıf noktaları
     */
    private List<BossWeakness> getBossWeaknesses(BossType type) {
        List<BossWeakness> weaknesses = new ArrayList<>();

        switch (type) {
            case TITAN_GOLEM:
                weaknesses.add(BossWeakness.FIRE);
                break;
            case HELL_DRAGON:
                weaknesses.add(BossWeakness.WATER);
                break;
            case HYDRA:
                weaknesses.add(BossWeakness.POISON);
                break;
            case CHAOS_GOD:
                weaknesses.add(BossWeakness.FIRE);
                weaknesses.add(BossWeakness.POISON);
                break;
        }

        return weaknesses;
    }

    /**
     * Boss yetenekleri (faza göre)
     */
    private Map<Integer, List<BossAbility>> getBossAbilities(BossType type) {
        Map<Integer, List<BossAbility>> abilities = new HashMap<>();

        switch (type) {
            case GOBLIN_KING:
                abilities.put(1, Arrays.asList(
                        BossAbility.CHARGE,
                        BossAbility.SUMMON_MINIONS,
                        BossAbility.EXPLOSION));
                break;

            case ORC_CHIEF:
                abilities.put(1, Arrays.asList(
                        BossAbility.CHARGE,
                        BossAbility.BLOCK_THROW,
                        BossAbility.SUMMON_MINIONS));
                break;

            case TROLL_KING:
                abilities.put(1, Arrays.asList(
                        BossAbility.BLOCK_THROW,
                        BossAbility.SHOCKWAVE,
                        BossAbility.HEAL));
                break;

            case DRAGON:
                abilities.put(1, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.TELEPORT,
                        BossAbility.EXPLOSION));
                abilities.put(2, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.TELEPORT,
                        BossAbility.SUMMON_MINIONS));
                break;

            case TREX:
                abilities.put(1, Arrays.asList(
                        BossAbility.CHARGE,
                        BossAbility.SHOCKWAVE,
                        BossAbility.EXPLOSION));
                break;

            case CYCLOPS:
                abilities.put(1, Arrays.asList(
                        BossAbility.BLOCK_THROW,
                        BossAbility.SHOCKWAVE,
                        BossAbility.CHARGE));
                abilities.put(2, Arrays.asList(
                        BossAbility.BLOCK_THROW,
                        BossAbility.SHOCKWAVE,
                        BossAbility.EXPLOSION,
                        BossAbility.HEAL));
                break;

            case TITAN_GOLEM:
                abilities.put(1, Arrays.asList(
                        BossAbility.BLOCK_THROW,
                        BossAbility.SHOCKWAVE,
                        BossAbility.EXPLOSION));
                abilities.put(2, Arrays.asList(
                        BossAbility.BLOCK_THROW,
                        BossAbility.SHOCKWAVE,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.HEAL));
                abilities.put(3, Arrays.asList(
                        BossAbility.BLOCK_THROW,
                        BossAbility.SHOCKWAVE,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.EXPLOSION,
                        BossAbility.SUMMON_MINIONS));
                break;

            case HELL_DRAGON:
                abilities.put(1, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.TELEPORT,
                        BossAbility.EXPLOSION));
                abilities.put(2, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.POISON_CLOUD,
                        BossAbility.TELEPORT));
                break;

            case HYDRA:
                abilities.put(1, Arrays.asList(
                        BossAbility.POISON_CLOUD,
                        BossAbility.TELEPORT,
                        BossAbility.SUMMON_MINIONS));
                abilities.put(2, Arrays.asList(
                        BossAbility.POISON_CLOUD,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.HEAL,
                        BossAbility.SUMMON_MINIONS));
                abilities.put(3, Arrays.asList(
                        BossAbility.POISON_CLOUD,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.EXPLOSION,
                        BossAbility.HEAL,
                        BossAbility.SUMMON_MINIONS));
                break;

            case CHAOS_GOD:
                abilities.put(1, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.TELEPORT));
                abilities.put(2, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.POISON_CLOUD,
                        BossAbility.EXPLOSION,
                        BossAbility.HEAL));
                abilities.put(3, Arrays.asList(
                        BossAbility.FIRE_BREATH,
                        BossAbility.LIGHTNING_STRIKE,
                        BossAbility.POISON_CLOUD,
                        BossAbility.EXPLOSION,
                        BossAbility.SHOCKWAVE,
                        BossAbility.HEAL,
                        BossAbility.SUMMON_MINIONS));
                break;
        }

        return abilities;
    }

    /**
     * Boss display name
     */
    public String getBossDisplayName(BossType type) {
        switch (type) {
            case GOBLIN_KING:
                return "Goblin Kralı";
            case ORC_CHIEF:
                return "Ork Şefi";
            case TROLL_KING:
                return "Troll Kralı";
            case DRAGON:
                return "Ejderha";
            case TREX:
                return "T-Rex";
            case CYCLOPS:
                return "Tek Gözlü Dev";
            case TITAN_GOLEM:
                return "Titan Golem";
            case HELL_DRAGON:
                return "Cehennem Ejderi";
            case HYDRA:
                return "Hydra";
            case PHOENIX:
                return "Phoenix";
            case VOID_DRAGON:
                return "Hiçlik Ejderi";
            case CHAOS_TITAN:
                return "Kaos Titani";
            case CHAOS_GOD:
                return "Khaos Tanrısı";
            default:
                return "Bilinmeyen Boss";
        }
    }

    /**
     * Ritüel deseni kontrol et
     */
    public boolean checkRitualPattern(Block centerBlock, BossType type) {
        // Merkez bloğun Çağırma Çekirdeği olup olmadığını kontrol et
        if (!centerBlock.hasMetadata("SummonCore")) {
            return false;
        }
        
        Material[][] pattern = getRitualPattern(type);
        if (pattern == null) {
            return false;
        }

        int size = pattern.length;
        int offset = size / 2;

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Block checkBlock = centerBlock.getRelative(x - offset, -1, z - offset);
                Material required = pattern[x][z];

                if (required != null && checkBlock.getType() != required) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Ritüel deseni al (public - listener için)
     */
    public Material[][] getRitualPattern(BossType type) {
        switch (type) {
            case GOBLIN_KING:
                // 3x3 Cobblestone + Merkez Gold Block
                return new Material[][] {
                        { Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE },
                        { Material.COBBLESTONE, Material.GOLD_BLOCK, Material.COBBLESTONE },
                        { Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE }
                };

            case ORC_CHIEF:
                // 3x3 Stone + Merkez Iron Block
                return new Material[][] {
                        { Material.STONE, Material.STONE, Material.STONE },
                        { Material.STONE, Material.IRON_BLOCK, Material.STONE },
                        { Material.STONE, Material.STONE, Material.STONE }
                };

            case TROLL_KING:
                // 3x3 Stone Bricks + Merkez Diamond Block
                return new Material[][] {
                        { Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS },
                        { Material.STONE_BRICKS, Material.DIAMOND_BLOCK, Material.STONE_BRICKS },
                        { Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS }
                };

            case DRAGON:
                // 5x5 Obsidian + Merkez Emerald Block
                return new Material[][] {
                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
                                Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, Material.EMERALD_BLOCK, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
                                Material.OBSIDIAN }
                };

            case TREX:
                // 5x5 Stone + Merkez Gold Block + Köşeler Diamond
                return new Material[][] {
                        { Material.DIAMOND_BLOCK, Material.STONE, Material.STONE, Material.STONE,
                                Material.DIAMOND_BLOCK },
                        { Material.STONE, null, null, null, Material.STONE },
                        { Material.STONE, null, Material.GOLD_BLOCK, null, Material.STONE },
                        { Material.STONE, null, null, null, Material.STONE },
                        { Material.DIAMOND_BLOCK, Material.STONE, Material.STONE, Material.STONE,
                                Material.DIAMOND_BLOCK }
                };

            case CYCLOPS:
                // 5x5 Stone Bricks + Merkez Emerald Block + Köşeler Gold
                return new Material[][] {
                        { Material.GOLD_BLOCK, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS,
                                Material.GOLD_BLOCK },
                        { Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS },
                        { Material.STONE_BRICKS, null, Material.EMERALD_BLOCK, null, Material.STONE_BRICKS },
                        { Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS },
                        { Material.GOLD_BLOCK, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS,
                                Material.GOLD_BLOCK }
                };

            case TITAN_GOLEM:
                // 7x7 Obsidian + Merkez Netherite Block + Köşeler Diamond
                return new Material[][] {
                        { Material.DIAMOND_BLOCK, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
                                Material.OBSIDIAN, Material.OBSIDIAN, Material.DIAMOND_BLOCK },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, Material.NETHERITE_BLOCK, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.DIAMOND_BLOCK, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
                                Material.OBSIDIAN, Material.OBSIDIAN, Material.DIAMOND_BLOCK }
                };

            case HELL_DRAGON:
                // 7x7 Netherrack + Merkez Nether Star (Beacon) + Köşeler Obsidian
                return new Material[][] {
                        { Material.OBSIDIAN, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
                                Material.NETHERRACK, Material.NETHERRACK, Material.OBSIDIAN },
                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
                        { Material.NETHERRACK, null, null, Material.BEACON, null, null, Material.NETHERRACK },
                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
                        { Material.OBSIDIAN, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
                                Material.NETHERRACK, Material.NETHERRACK, Material.OBSIDIAN }
                };

            case HYDRA:
                // 7x7 Prismarine + Merkez Heart of the Sea + Köşeler Emerald
                return new Material[][] {
                        { Material.EMERALD_BLOCK, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE,
                                Material.PRISMARINE, Material.PRISMARINE, Material.EMERALD_BLOCK },
                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
                        { Material.PRISMARINE, null, null, Material.CONDUIT, null, null, Material.PRISMARINE },
                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
                        { Material.EMERALD_BLOCK, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE,
                                Material.PRISMARINE, Material.PRISMARINE, Material.EMERALD_BLOCK }
                };

            case CHAOS_GOD:
                // 9x9 Bedrock + Merkez End Stone Bricks + Köşeler Netherite + Kenarlar Obsidian
                return new Material[][] {
                        { Material.NETHERITE_BLOCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
                                Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
                                Material.NETHERITE_BLOCK },
                        { Material.BEDROCK, Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN,
                                Material.BEDROCK },
                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
                        { Material.BEDROCK, null, null, null, Material.END_STONE_BRICKS, null, null, null,
                                Material.BEDROCK },
                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
                        { Material.BEDROCK, Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN,
                                Material.BEDROCK },
                        { Material.NETHERITE_BLOCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
                                Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
                                Material.NETHERITE_BLOCK }
                };

            case PHOENIX:
                // 5x5 Netherrack + Merkez Beacon + Köşeler Blaze Rod pattern
                return new Material[][] {
                        { Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
                                Material.NETHERRACK },
                        { Material.NETHERRACK, null, null, null, Material.NETHERRACK },
                        { Material.NETHERRACK, null, Material.BEACON, null, Material.NETHERRACK },
                        { Material.NETHERRACK, null, null, null, Material.NETHERRACK },
                        { Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
                                Material.NETHERRACK }
                };

            case VOID_DRAGON:
                // 7x7 Obsidian + Merkez End Portal Frame + Köşeler Ender Eye
                return new Material[][] {
                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
                                Material.OBSIDIAN, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, Material.END_PORTAL_FRAME, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
                                Material.OBSIDIAN, Material.OBSIDIAN }
                };

            case CHAOS_TITAN:
                // 7x7 Netherite + Merkez Beacon + Diamond Blocks
                return new Material[][] {
                        { Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
                                Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
                                Material.NETHERITE_BLOCK },
                        { Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, null, null, null, Material.DIAMOND_BLOCK,
                                Material.NETHERITE_BLOCK },
                        { Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK },
                        { Material.NETHERITE_BLOCK, null, null, Material.BEACON, null, null, Material.NETHERITE_BLOCK },
                        { Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK },
                        { Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, null, null, null, Material.DIAMOND_BLOCK,
                                Material.NETHERITE_BLOCK },
                        { Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
                                Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
                                Material.NETHERITE_BLOCK }
                };

            default:
                return null;
        }
    }

    /**
     * Ritüel aktifleştirme itemi
     */
    public Material getRitualActivationItem(BossType type) {
        switch (type) {
            case GOBLIN_KING:
                return Material.ROTTEN_FLESH;
            case ORC_CHIEF:
                return Material.IRON_SWORD;
            case TROLL_KING:
                return Material.STONE_AXE;
            case DRAGON:
                return Material.DRAGON_EGG;
            case TREX:
                return Material.BONE;
            case CYCLOPS:
                return Material.ENDER_EYE;
            case TITAN_GOLEM:
                return Material.NETHER_STAR;
            case HELL_DRAGON:
                return Material.BLAZE_ROD;
            case HYDRA:
                return Material.HEART_OF_THE_SEA;
            case PHOENIX:
                return Material.BLAZE_POWDER;
            case VOID_DRAGON:
                return Material.DRAGON_EGG;
            case CHAOS_TITAN:
                return Material.NETHER_STAR;
            case CHAOS_GOD:
                return Material.NETHER_STAR;
            default:
                return null;
        }
    }

    /**
     * Boss zayıflığı kontrolü (hasar çarpanı)
     */
    public double getWeaknessMultiplier(BossData boss, org.bukkit.event.entity.EntityDamageEvent.DamageCause cause) {
        double multiplier = 1.0;

        for (BossWeakness weakness : boss.getWeaknesses()) {
            switch (weakness) {
                case FIRE:
                    if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE ||
                            cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK ||
                            cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA) {
                        multiplier = 2.0; // 2x hasar
                    }
                    break;
                case WATER:
                    if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.DROWNING) {
                        multiplier = 2.0;
                    }
                    break;
                case POISON:
                    // Potion effect kontrolü ayrı yapılacak
                    break;
                case LIGHTNING:
                    if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LIGHTNING) {
                        multiplier = 2.0;
                    }
                    break;
            }
        }

        return multiplier;
    }

    /**
     * Doğada boss spawn (zorluk seviyesine göre)
     */
    public void trySpawnBossInNature(Location loc, int difficultyLevel) {
        if (loc == null || loc.getWorld() == null) {
            return;
        }

        // Spawn şansı
        double spawnChance = getBossSpawnChance(difficultyLevel);
        if (new Random().nextDouble() > spawnChance) {
            return;
        }

        // Seviyeye göre boss seç
        BossType bossType = getRandomBossForLevel(difficultyLevel);
        if (bossType == null) {
            return;
        }

        // Spawn et
        LivingEntity bossEntity = spawnBossEntity(loc, bossType);
        if (bossEntity != null) {
            BossData bossData = createBossData(bossType, bossEntity, null);
            activeBosses.put(bossEntity.getUniqueId(), bossData);

            plugin.getLogger().info(
                    "Doğada boss spawn edildi: " + getBossDisplayName(bossType) + " (Seviye " + difficultyLevel + ")");
        }
    }

    /**
     * Boss spawn şansı
     */
    private double getBossSpawnChance(int difficultyLevel) {
        switch (difficultyLevel) {
            case 1:
                return 0.01; // %1
            case 2:
                return 0.015; // %1.5
            case 3:
                return 0.02; // %2
            case 4:
                return 0.025; // %2.5
            case 5:
                return 0.03; // %3
            default:
                return 0.0;
        }
    }

    /**
     * Seviyeye göre rastgele boss
     */
    private BossType getRandomBossForLevel(int difficultyLevel) {
        List<BossType> availableBosses = new ArrayList<>();

        switch (difficultyLevel) {
            case 1:
                availableBosses.add(BossType.GOBLIN_KING);
                availableBosses.add(BossType.ORC_CHIEF);
                break;
            case 2:
                availableBosses.add(BossType.ORC_CHIEF);
                availableBosses.add(BossType.TROLL_KING);
                break;
            case 3:
                availableBosses.add(BossType.DRAGON);
                availableBosses.add(BossType.TREX);
                availableBosses.add(BossType.CYCLOPS);
                break;
            case 4:
                availableBosses.add(BossType.CYCLOPS);
                availableBosses.add(BossType.TITAN_GOLEM);
                availableBosses.add(BossType.HELL_DRAGON);
                availableBosses.add(BossType.HYDRA);
                break;
            case 5:
                availableBosses.add(BossType.HYDRA);
                availableBosses.add(BossType.CHAOS_GOD);
                break;
        }

        if (availableBosses.isEmpty()) {
            return null;
        }

        return availableBosses.get(new Random().nextInt(availableBosses.size()));
    }

    /**
     * Boss kaydet
     */
    private void saveBosses() {
        // Aktif bosslar runtime'da tutulur, sadece ritüel cooldown'ları kaydedilir
        // Boss öldüğünde otomatik temizlenir
    }

    /**
     * Boss yükle
     */
    private void loadBosses() {
        // Boss'lar runtime'da tutulur, restart sonrası spawn olmaz
        // Ritüel cooldown'ları yüklenebilir (opsiyonel)
    }

    /**
     * Boss bilgisi al
     */
    public BossData getBossData(UUID entityId) {
        return activeBosses.get(entityId);
    }

    /**
     * Boss kaldır (öldüğünde)
     */
    public void removeBoss(UUID entityId) {
        activeBosses.remove(entityId);
    }
}
