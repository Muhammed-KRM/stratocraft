package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Eski BossManager için sadeleştirilmiş adaptor.
 *
 * - Dış sınıfların (listener, komutlar, eğitme sistemi, bataryalar vb.)
 *   ihtiyaç duyduğu API'yi sağlar.
 * - Gerçek savaş yapay zekâsı ve arena sistemi {@link NewBossArenaManager}
 *   üzerinden yürütülür.
 * - Blok kırma / delik açma gibi eski, sorunlu özellikler BİLİNÇLİ OLARAK
 *   UYGULANMAMIŞTIR.
 */
public class BossManager {
    private final Main plugin;

    // Eski sistemle uyum için, sadece temel bilgiler tutuluyor
    private final Map<UUID, BossData> activeBosses = new HashMap<>();

    // BossBar sistemi
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private int maxBossBarDistance = 100;
    private int maxBossBarsPerPlayer = 3;

    // Ritüel cooldown’ları (merkez blok konumu → son kullanım zamanı)
    private final Map<Location, Long> ritualCooldowns = new HashMap<>();
    private static final long RITUAL_COOLDOWN = 60_000L; // 60 sn

    // Zayıf nokta & kalkan süreleri (sadece durum bilgisini sağlamak için)
    private final Map<UUID, Long> weakPointCooldowns = new HashMap<>();
    private static final long WEAK_POINT_DURATION = 5_000L;

    private final Map<UUID, Long> shieldCooldowns = new HashMap<>();
    private static final long SHIELD_DURATION = 3_000L;

    public BossManager(Main plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;

        // Yetenek task'ını başlat
        startAbilityTask();
        // BossBar task'ını başlat
        startBossBarTask();
    }

    /**
     * Eski boss tipleri – bu enum diğer sınıflar tarafından yoğun şekilde kullanılıyor,
     * bu yüzden isimler DEĞİŞTİRİLMEMELİ.
     */
    public enum BossType {
        GOBLIN_KING,   // Seviye 1
        ORC_CHIEF,     // Seviye 1-2
        TROLL_KING,    // Seviye 2
        DRAGON,        // Seviye 3
        TREX,          // Seviye 3
        CYCLOPS,       // Seviye 3-4
        TITAN_GOLEM,   // Seviye 4
        HELL_DRAGON,   // Seviye 4
        HYDRA,         // Seviye 4-5
        PHOENIX,       // Seviye 4
        VOID_DRAGON,   // Seviye 5
        CHAOS_TITAN,   // Seviye 5
        CHAOS_GOD      // Seviye 5
    }

    public enum BossWeakness {
        FIRE,
        WATER,
        POISON,
        LIGHTNING
    }

    /**
     * Boss özel saldırıları (eski sistemden sadeleştirilmiş hâli)
     */
    public enum BossAbility {
        FIRE_BREATH,      // Ateş püskürtme
        EXPLOSION,        // Küçük patlama (blok kırmaz)
        LIGHTNING_STRIKE, // Yıldırım
        BLOCK_THROW,      // Üstten düşen taş/kum blokları
        POISON_CLOUD,     // Zehir alanı
        TELEPORT,         // Hedefe yakın ışınlanma
        CHARGE,           // Koşup çarpma
        SUMMON_MINIONS,   // Minyon çağırma
        HEAL,             // Kendini iyileştirme
        SHOCKWAVE         // Şok dalgası (geri savurma)
    }

    /**
     * Eski BossData'nın sadeleştirilmiş hâli.
     * Faz ve yetenek bilgileri bu sınıf dışında (örneğin arena ve diğer sistemlerde)
     * sadece diğer sınıfların ihtiyaç duyduğu alanlar bulunur.
     */
    public static class BossData {
        private final BossType type;
        private final LivingEntity entity;
        private final UUID ownerId;
        private final int maxPhase;
        private int phase;
        private final List<BossWeakness> weaknesses;

        // Yetenek sistemi için
        private long lastAbilityTime;
        private long abilityCooldownMs;

        public BossData(BossType type,
                        LivingEntity entity,
                        UUID ownerId,
                        int maxPhase,
                        List<BossWeakness> weaknesses) {
            this.type = type;
            this.entity = entity;
            this.ownerId = ownerId;
            this.maxPhase = maxPhase;
            this.phase = 1;
            this.weaknesses = weaknesses != null ? weaknesses : Collections.emptyList();
            this.lastAbilityTime = 0L;
            this.abilityCooldownMs = 6_000L; // varsayılan 6 sn
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

        public void nextPhase() {
            if (phase < maxPhase) {
                phase++;
            }
        }

        public long getLastAbilityTime() {
            return lastAbilityTime;
        }

        public void setLastAbilityTime(long lastAbilityTime) {
            this.lastAbilityTime = lastAbilityTime;
        }

        public long getAbilityCooldownMs() {
            return abilityCooldownMs;
        }

        public void setAbilityCooldownMs(long abilityCooldownMs) {
            this.abilityCooldownMs = abilityCooldownMs;
        }

        public boolean canUseAbility() {
            return System.currentTimeMillis() - lastAbilityTime >= abilityCooldownMs;
        }
    }

    // =====================================================================
    //  D I Ş   A P I
    // =====================================================================

    /**
     * Dış sınıfların boss bilgisine erişmesi için.
     */
    public BossData getBossData(UUID entityId) {
        return activeBosses.get(entityId);
    }

    /**
     * Boss öldüğünde veya manuel olarak kaldırıldığında çağrılır.
     * - BossData kaydı silinir
     * - Zayıf nokta / kalkan durumları temizlenir
     * - Arena transformasyonu durdurulur (NewBossArenaManager üzerinden)
     */
    public void removeBoss(UUID entityId) {
        if (entityId == null) return;

        activeBosses.remove(entityId);
        weakPointCooldowns.remove(entityId);
        shieldCooldowns.remove(entityId);

         // BossBar'ı kaldır
        BossBar bar = bossBars.remove(entityId);
        if (bar != null) {
            bar.removeAll();
        }

        // Arena görevini durdur
        try {
            me.mami.stratocraft.manager.NewBossArenaManager arenaMgr =
                    me.mami.stratocraft.Main.getInstance().getNewBossArenaManager();
            if (arenaMgr != null) {
                arenaMgr.stopArenaTransformation(entityId);
            }
        } catch (Exception ignored) {
            // Arena sistemi yoksa sessizce geç
        }
    }

    // =====================================================================
    //  Y E T E N E K   S İ S T E M İ
    // =====================================================================

    private void startAbilityTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBosses.isEmpty()) {
                    return;
                }

                for (BossData data : new ArrayList<>(activeBosses.values())) {
                    LivingEntity boss = data.getEntity();
                    if (boss == null || !boss.isValid() || boss.isDead()) {
                        continue;
                    }

                    // Yakında oyuncu yoksa yetenek kullanma
                    Player target = BossManager.this.findNearestPlayer(boss.getLocation(), 30);
                    if (target == null) continue;

                    if (!data.canUseAbility()) continue;

                    BossAbility ability = chooseAbility(data, target);
                    if (ability != null) {
                        long delay = 0L;
                        if (shouldShowThreatWarning(ability)) {
                            showThreatWarning(data, ability, 3);
                            delay = 60L; // 3 saniye sonra saldır
                        }

                        final BossAbility finalAbility = ability;
                        final Player finalTarget = target;
                        if (delay > 0) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    executeAbility(data, finalTarget, finalAbility);
                                }
                            }.runTaskLater(plugin, delay);
                        } else {
                            executeAbility(data, target, ability);
                        }

                        data.setLastAbilityTime(System.currentTimeMillis());
                        data.setAbilityCooldownMs(getAbilityCooldown(ability));
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 40L); // 5 sn sonra başla, her 2 sn'de kontrol et
    }

    /**
     * Boss'un kullanabileceği yetenekleri seç (mesafe ve boss tipine göre)
     */
    private BossAbility chooseAbility(BossData data, Player target) {
        LivingEntity boss = data.getEntity();
        if (boss == null || target == null) return null;

        double distance = boss.getLocation().distance(target.getLocation());
        List<BossAbility> abilities = new ArrayList<>();

        // Mesafeye göre temel havuz
        if (distance > 15) {
            abilities.add(BossAbility.FIRE_BREATH);
            abilities.add(BossAbility.LIGHTNING_STRIKE);
            abilities.add(BossAbility.SUMMON_MINIONS);
        } else if (distance > 6) {
            abilities.add(BossAbility.FIRE_BREATH);
            abilities.add(BossAbility.CHARGE);
            abilities.add(BossAbility.SHOCKWAVE);
            abilities.add(BossAbility.EXPLOSION);
        } else { // çok yakın
            abilities.add(BossAbility.SHOCKWAVE);
            abilities.add(BossAbility.CHARGE);
            abilities.add(BossAbility.POISON_CLOUD);
        }

        // Boss tipine özel eklemeler
        switch (data.getType()) {
            case DRAGON:
            case HELL_DRAGON:
            case VOID_DRAGON:
                abilities.add(BossAbility.FIRE_BREATH);
                abilities.add(BossAbility.LIGHTNING_STRIKE);
                abilities.add(BossAbility.TELEPORT);
                break;
            case TITAN_GOLEM:
            case CHAOS_TITAN:
                abilities.add(BossAbility.SHOCKWAVE);
                abilities.add(BossAbility.BLOCK_THROW);
                break;
            case HYDRA:
                abilities.add(BossAbility.POISON_CLOUD);
                abilities.add(BossAbility.SUMMON_MINIONS);
                break;
            case PHOENIX:
                abilities.add(BossAbility.FIRE_BREATH);
                abilities.add(BossAbility.HEAL);
                break;
            case CHAOS_GOD:
                abilities.add(BossAbility.EXPLOSION);
                abilities.add(BossAbility.LIGHTNING_STRIKE);
                abilities.add(BossAbility.TELEPORT);
                abilities.add(BossAbility.SUMMON_MINIONS);
                break;
            default:
                // düşük seviye bosslar için ekstra minyon / charge
                abilities.add(BossAbility.CHARGE);
                abilities.add(BossAbility.SUMMON_MINIONS);
                break;
        }

        if (abilities.isEmpty()) return null;
        return abilities.get(new Random().nextInt(abilities.size()));
    }

    /**
     * Seçilen yeteneği çalıştır
     */
    private void executeAbility(BossData data, Player target, BossAbility ability) {
        LivingEntity boss = data.getEntity();
        if (boss == null || boss.isDead()) return;

        switch (ability) {
            case FIRE_BREATH:
                abilityFireBreath(boss, target);
                break;
            case EXPLOSION:
                abilityExplosion(boss.getLocation());
                break;
            case LIGHTNING_STRIKE:
                abilityLightning(target != null ? target.getLocation() : boss.getLocation());
                break;
            case BLOCK_THROW:
                abilityBlockThrow(boss, target);
                break;
            case POISON_CLOUD:
                abilityPoisonCloud(target != null ? target.getLocation() : boss.getLocation());
                break;
            case TELEPORT:
                abilityTeleport(boss, target);
                break;
            case CHARGE:
                abilityCharge(boss, target);
                break;
            case SUMMON_MINIONS:
                abilitySummonMinions(boss.getLocation(), data.getType());
                break;
            case HEAL:
                abilityHeal(boss);
                break;
            case SHOCKWAVE:
                abilityShockwave(boss.getLocation(), boss);
                break;
        }
    }

    // ---- Yetenek implementasyonları (blok kırmadan, güvenli) ----

    private void abilityFireBreath(LivingEntity boss, Player target) {
        Location start = boss.getEyeLocation();
        Vector direction = target != null
                ? target.getEyeLocation().toVector().subtract(start.toVector()).normalize()
                : boss.getLocation().getDirection().normalize();

        for (int i = 1; i <= 10; i++) {
            final int step = i;
            Location fireLoc = start.clone().add(direction.clone().multiply(step * 0.8));
            final Location finalLoc = fireLoc;

            new BukkitRunnable() {
                @Override
                public void run() {
                    finalLoc.getWorld().spawnParticle(Particle.FLAME, finalLoc, 8, 0.3, 0.3, 0.3, 0.02);
                    finalLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, finalLoc, 2, 0.2, 0.2, 0.2, 0.01);

                    for (Entity e : finalLoc.getWorld().getNearbyEntities(finalLoc, 1.0, 1.0, 1.0)) {
                        if (e instanceof Player) {
                            Player p = (Player) e;
                            p.setFireTicks(60);
                            p.damage(3.0, boss);
                        }
                    }
                }
            }.runTaskLater(plugin, step * 2L);
        }

        boss.getWorld().playSound(start, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
    }

    private void abilityExplosion(Location center) {
        center.getWorld().createExplosion(center, 3.0f, false, false);
        center.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 1);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
    }

    private void abilityLightning(Location targetLoc) {
        Location safe = targetLoc.clone().add(0, 1, 0);
        safe.getWorld().strikeLightningEffect(safe);
        safe.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, safe, 30, 0.5, 1.0, 0.5, 0.1);

        for (Entity e : safe.getWorld().getNearbyEntities(targetLoc, 2, 3, 2)) {
            if (e instanceof Player) {
                ((Player) e).damage(5.0);
            }
        }
    }

    private void abilityBlockThrow(LivingEntity boss, Player target) {
        if (target == null) return;

        Location origin = boss.getLocation().add(0, 2, 0);
        for (int i = 0; i < 3; i++) {
            FallingBlock fb = origin.getWorld().spawnFallingBlock(
                    origin,
                    Material.COBBLESTONE.createBlockData()
            );
            Vector dir = target.getLocation().toVector().subtract(origin.toVector()).normalize().multiply(1.0);
            dir.setY(0.4);
            fb.setVelocity(dir);
            fb.setHurtEntities(true);
            fb.setDropItem(false);
        }
        boss.getWorld().playSound(origin, Sound.BLOCK_STONE_BREAK, 1.0f, 0.9f);
    }

    private void abilityPoisonCloud(Location center) {
        center.getWorld().spawnParticle(Particle.SPELL_MOB, center, 60, 2.5, 1.0, 2.5, 0.1);
        center.getWorld().playSound(center, Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.8f);

        for (Entity e : center.getWorld().getNearbyEntities(center, 4, 2, 4)) {
            if (e instanceof Player) {
                Player p = (Player) e;
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            }
        }
    }

    private void abilityTeleport(LivingEntity boss, Player target) {
        if (target == null) return;

        Location base = target.getLocation();
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(45 * i);
            double dist = 3.0;
            Location candidate = base.clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
            candidate.setY(candidate.getWorld().getHighestBlockYAt(candidate) + 1);

            if (candidate.getBlock().getType().isAir()) {
                boss.teleport(candidate);
                boss.getWorld().playSound(candidate, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return;
            }
        }
    }

    private void abilityCharge(LivingEntity boss, Player target) {
        if (target == null) return;

        Vector dir = target.getLocation().toVector().subtract(boss.getLocation().toVector()).normalize();
        dir.setY(0.2);
        dir.multiply(1.5);
        boss.setVelocity(dir);

        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.0f);
    }

    private void abilitySummonMinions(Location center, BossType type) {
        EntityType minionType;
        switch (type) {
            case DRAGON:
            case HELL_DRAGON:
            case VOID_DRAGON:
                minionType = EntityType.BLAZE;
                break;
            case TITAN_GOLEM:
            case CHAOS_TITAN:
                minionType = EntityType.IRON_GOLEM;
                break;
            default:
                minionType = EntityType.ZOMBIE;
                break;
        }

        int count = 2;
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            Location spawnLoc = center.clone().add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);
            spawnLoc.setY(spawnLoc.getWorld().getHighestBlockYAt(spawnLoc) + 1);
            center.getWorld().spawnEntity(spawnLoc, minionType);
        }
        center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.8f);
    }

    private void abilityHeal(LivingEntity boss) {
        double max = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double heal = max * 0.15;
        boss.setHealth(Math.min(max, boss.getHealth() + heal));
        boss.getWorld().spawnParticle(Particle.HEART, boss.getLocation().add(0, 1.5, 0), 10, 0.5, 0.5, 0.5, 0.05);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
    }

    private void abilityShockwave(Location center, LivingEntity boss) {
        double radius = 6.0;
        center.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, center, 20, radius, 0.2, radius, 0.1);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);

        for (Entity e : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (e instanceof Player) {
                Player p = (Player) e;
                p.damage(4.0, boss);
                Vector kb = p.getLocation().toVector().subtract(center.toVector()).normalize();
                kb.setY(0.4);
                p.setVelocity(kb);
            }
        }
    }

    // =====================================================================
    //  TEHDİT UYARISI ve COOLDOWN
    // =====================================================================

    private boolean shouldShowThreatWarning(BossAbility ability) {
        switch (ability) {
            case EXPLOSION:
            case SHOCKWAVE:
            case LIGHTNING_STRIKE:
            case FIRE_BREATH:
            case CHARGE:
                return true;
            default:
                return false;
        }
    }

    private void showThreatWarning(BossData data, BossAbility ability, int seconds) {
        LivingEntity entity = data.getEntity();
        if (entity == null || entity.isDead()) return;

        Location loc = entity.getLocation();
        String abilityName = ability.name().replace('_', ' ');

        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining <= 0 || entity.isDead()) {
                    cancel();
                    return;
                }

                String msg = "§c§l⚠ " + abilityName + " " + remaining + " SANİYE!";
                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distance(loc) <= 30) {
                        p.sendTitle("", msg, 0, 20, 0);
                        p.playSound(p.getLocation(),
                                Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + remaining * 0.1f);
                    }
                }

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private long getAbilityCooldown(BossAbility ability) {
        switch (ability) {
            case HEAL:
            case TELEPORT:
            case SUMMON_MINIONS:
                return 12_000L;
            case EXPLOSION:
            case SHOCKWAVE:
                return 8_000L;
            default:
                return 5_000L;
        }
    }

    // =====================================================================
    //  B O S S B A R   S İ S T E M İ
    // =====================================================================

    private void createBossBar(LivingEntity entity, BossType type) {
        String name = getBossDisplayName(type);
        BossBar bar = Bukkit.createBossBar("§c§l" + name, BarColor.RED, BarStyle.SOLID);
        bar.setVisible(true);
        bossBars.put(entity.getUniqueId(), bar);
    }

    private void startBossBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBosses.isEmpty()) {
                    // Herhangi bir boss yoksa tüm BossBar'lardan oyuncuları kaldır
                    for (BossBar bar : bossBars.values()) {
                        bar.removeAll();
                    }
                    return;
                }

                // BossBar progress ve başlık güncelle
                for (Map.Entry<UUID, BossData> entry : activeBosses.entrySet()) {
                    BossData data = entry.getValue();
                    LivingEntity boss = data.getEntity();
                    
                    // Boss geçersizse temizle
                    if (boss == null || !boss.isValid() || boss.isDead()) {
                        BossBar bar = bossBars.remove(entry.getKey());
                        if (bar != null) bar.removeAll();
                        continue;
                    }
                    
                    // BossBar yoksa oluştur (güvenlik kontrolü)
                    BossBar bar = bossBars.get(entry.getKey());
                    if (bar == null) {
                        createBossBar(boss, data.getType());
                        bar = bossBars.get(entry.getKey());
                        if (bar == null) continue; // Hala null ise atla
                    }

                    // BossBar'ı güncelle
                    double max = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double hp = boss.getHealth();
                    double progress = Math.max(0.0, Math.min(1.0, hp / max));
                    String title = "§c§l" + getBossDisplayName(data.getType()) +
                            " §7[" + (int) hp + "/" + (int) max + "]";
                    bar.setTitle(title);
                    bar.setProgress(progress);
                    bar.setVisible(true); // Her zaman görünür yap
                }

                // Her oyuncu için en yakın bossBar'ları göster
                for (Player player : Bukkit.getOnlinePlayers()) {
                    List<Map.Entry<UUID, BossData>> nearby = new ArrayList<>();
                    Location pl = player.getLocation();

                    for (Map.Entry<UUID, BossData> entry : activeBosses.entrySet()) {
                        LivingEntity boss = entry.getValue().getEntity();
                        if (boss == null || !boss.isValid() || boss.isDead()) continue;
                        if (!boss.getWorld().equals(pl.getWorld())) continue;
                        double dist = pl.distance(boss.getLocation());
                        if (dist <= maxBossBarDistance) {
                            nearby.add(entry);
                        }
                    }

                    // mesafeye göre sırala
                    nearby.sort(Comparator.comparingDouble(
                            e -> e.getValue().getEntity().getLocation().distance(pl)
                    ));

                    // sadece ilk maxBossBarsPerPlayer kadarını göster
                    Set<UUID> visibleIds = new HashSet<>();
                    for (int i = 0; i < Math.min(maxBossBarsPerPlayer, nearby.size()); i++) {
                        visibleIds.add(nearby.get(i).getKey());
                    }

                    // BossBar'lara ekle/çıkar
                    for (Map.Entry<UUID, BossBar> entry : bossBars.entrySet()) {
                        BossBar bar = entry.getValue();
                        if (bar == null) continue; // Güvenlik kontrolü
                        
                        if (visibleIds.contains(entry.getKey())) {
                            // Yakındaki boss - oyuncuya ekle
                            if (!bar.getPlayers().contains(player)) {
                                bar.addPlayer(player);
                            }
                        } else {
                            // Uzaktaki boss - oyuncudan çıkar
                            if (bar.getPlayers().contains(player)) {
                                bar.removePlayer(player);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // her saniye
    }

    /**
     * En yakın oyuncuyu bul (sadece aynı dünyadakiler arasında, max mesafe sınırıyla).
     */
    private Player findNearestPlayer(Location loc, double maxDistance) {
        if (loc == null || loc.getWorld() == null) return null;

        Player nearest = null;
        double nearestDist = maxDistance;

        for (Player player : loc.getWorld().getPlayers()) {
            if (player == null || !player.isOnline()) continue;

            double d = player.getLocation().distance(loc);
            if (d <= nearestDist) {
                nearest = player;
                nearestDist = d;
            }
        }

        return nearest;
    }

     /**
      * Oyuncu giriş yaptığında – eski sistem BossBar güncellemesini buradan başlatıyordu.
      * Yeni birleşik sistem BossBar’ları kendi içinde güncelleyebilecek; şimdilik
      * burada ekstra işlem yok.
      */
    public void onPlayerJoin(Player player) {
        // Bilinçli olarak boş bırakıldı.
    }

    /**
     * Boss görünür adı.
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
     * Ritüel deseni kontrolü.
     * Merkez blokta "SummonCore" metadata’sı ve altında doğru blok paterni olup
     * olmadığını kontrol eder.
     */
    public boolean checkRitualPattern(Block centerBlock, BossType type) {
        if (centerBlock == null) return false;

        // Merkezde çağırma çekirdeği olmalı
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
     * Ritüel desenini döndür (Admin komutları ve görsel yardım için).
     */
    public Material[][] getRitualPattern(BossType type) {
        switch (type) {
            case GOBLIN_KING:
                // 3x3 Cobblestone + Merkez Gold Block
                return new Material[][]{
                        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE},
                        {Material.COBBLESTONE, Material.GOLD_BLOCK, Material.COBBLESTONE},
                        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE}
                };

            case ORC_CHIEF:
                // 3x3 Stone + Merkez Iron Block
                return new Material[][]{
                        {Material.STONE, Material.STONE, Material.STONE},
                        {Material.STONE, Material.IRON_BLOCK, Material.STONE},
                        {Material.STONE, Material.STONE, Material.STONE}
                };

            case TROLL_KING:
                // 3x3 Stone Bricks + Merkez Diamond Block
                return new Material[][]{
                        {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS},
                        {Material.STONE_BRICKS, Material.DIAMOND_BLOCK, Material.STONE_BRICKS},
                        {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS}
                };

            case DRAGON:
                // 5x5 Obsidian + Merkez Emerald Block
                return new Material[][]{
                        {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, Material.EMERALD_BLOCK, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
                };

            case TREX:
                // 5x5 Stone + Merkez Gold Block + Köşeler Diamond
                return new Material[][]{
                        {Material.DIAMOND_BLOCK, Material.STONE, Material.STONE, Material.STONE, Material.DIAMOND_BLOCK},
                        {Material.STONE, null, null, null, Material.STONE},
                        {Material.STONE, null, Material.GOLD_BLOCK, null, Material.STONE},
                        {Material.STONE, null, null, null, Material.STONE},
                        {Material.DIAMOND_BLOCK, Material.STONE, Material.STONE, Material.STONE, Material.DIAMOND_BLOCK}
                };

            case CYCLOPS:
                // 5x5 Stone Bricks + Merkez Emerald Block + Köşeler Gold
                return new Material[][]{
                        {Material.GOLD_BLOCK, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.GOLD_BLOCK},
                        {Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS},
                        {Material.STONE_BRICKS, null, Material.EMERALD_BLOCK, null, Material.STONE_BRICKS},
                        {Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS},
                        {Material.GOLD_BLOCK, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.GOLD_BLOCK}
                };

            case TITAN_GOLEM:
                // 7x7 Obsidian + Merkez Netherite Block + Köşeler Diamond
                return new Material[][]{
                        {Material.DIAMOND_BLOCK, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.DIAMOND_BLOCK},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, Material.NETHERITE_BLOCK, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.DIAMOND_BLOCK, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.DIAMOND_BLOCK}
                };

            case HELL_DRAGON:
                // 7x7 Netherrack + Merkez Beacon (Nether Star temsili)
                return new Material[][]{
                        {Material.OBSIDIAN, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.OBSIDIAN},
                        {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                        {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                        {Material.NETHERRACK, null, null, Material.BEACON, null, null, Material.NETHERRACK},
                        {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                        {Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK},
                        {Material.OBSIDIAN, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.OBSIDIAN}
                };

            case HYDRA:
                // 7x7 Prismarine + Merkez Conduit (Heart of the Sea temsili)
                return new Material[][]{
                        {Material.EMERALD_BLOCK, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.EMERALD_BLOCK},
                        {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                        {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                        {Material.PRISMARINE, null, null, Material.CONDUIT, null, null, Material.PRISMARINE},
                        {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                        {Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE},
                        {Material.EMERALD_BLOCK, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE, Material.EMERALD_BLOCK}
                };

            case CHAOS_GOD:
                // 9x9 Bedrock + Merkez End Stone Bricks + Köşeler Netherite + Kenarlar Obsidian
                return new Material[][]{
                        {Material.NETHERITE_BLOCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.NETHERITE_BLOCK},
                        {Material.BEDROCK, Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN, Material.BEDROCK},
                        {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                        {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                        {Material.BEDROCK, null, null, null, Material.END_STONE_BRICKS, null, null, null, Material.BEDROCK},
                        {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                        {Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK},
                        {Material.BEDROCK, Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN, Material.BEDROCK},
                        {Material.NETHERITE_BLOCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.NETHERITE_BLOCK}
                };

            case PHOENIX:
                // 5x5 Netherrack + Merkez Beacon
                return new Material[][]{
                        {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK},
                        {Material.NETHERRACK, null, null, null, Material.NETHERRACK},
                        {Material.NETHERRACK, null, Material.BEACON, null, Material.NETHERRACK},
                        {Material.NETHERRACK, null, null, null, Material.NETHERRACK},
                        {Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK}
                };

            case VOID_DRAGON:
                // 7x7 Obsidian + Merkez End Portal Frame
                return new Material[][]{
                        {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, Material.END_PORTAL_FRAME, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN},
                        {Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN}
                };

            case CHAOS_TITAN:
                // 7x7 Netherite + Merkez Beacon + Diamond Blocks
                return new Material[][]{
                        {Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK},
                        {Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, null, null, null, Material.DIAMOND_BLOCK, Material.NETHERITE_BLOCK},
                        {Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK},
                        {Material.NETHERITE_BLOCK, null, null, Material.BEACON, null, null, Material.NETHERITE_BLOCK},
                        {Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK},
                        {Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, null, null, null, Material.DIAMOND_BLOCK, Material.NETHERITE_BLOCK},
                        {Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK}
                };

            default:
                return null;
        }
    }

    /**
     * Ritüel aktifleştirme item'i.
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
     * Zayıflık çarpanı (BossListener bu metodu kullanıyor).
     */
    public double getWeaknessMultiplier(BossData boss, EntityDamageEvent.DamageCause cause) {
        if (boss == null || cause == null) return 1.0;

        double multiplier = 1.0;

        for (BossWeakness weakness : boss.getWeaknesses()) {
            switch (weakness) {
                case FIRE:
                    if (cause == EntityDamageEvent.DamageCause.FIRE ||
                            cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                            cause == EntityDamageEvent.DamageCause.LAVA) {
                        multiplier = 2.0;
                    }
                    break;
                case WATER:
                    if (cause == EntityDamageEvent.DamageCause.DROWNING) {
                        multiplier = 2.0;
                    }
                    break;
                case POISON:
                    // Potion effect kontrolü BossListener içinde ayrıca yapılıyor
                    break;
                case LIGHTNING:
                    if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
                        multiplier = 2.0;
                    }
                    break;
            }
        }

        return multiplier;
    }

    /**
     * Zayıf nokta aktif mi? (kritik vuruş sistemi için)
     */
    public boolean isWeakPointActive(UUID bossId) {
        Long until = weakPointCooldowns.get(bossId);
        return until != null && System.currentTimeMillis() < until;
    }

    /**
     * Kalkan aktif mi? (hasar azaltma için)
     */
    public boolean isShieldActive(UUID bossId) {
        Long until = shieldCooldowns.get(bossId);
        return until != null && System.currentTimeMillis() < until;
    }

    // =====================================================================
    //  B O S S   S P A W N   L O J I Ğ I
    // =====================================================================

    /**
     * Ritüel ile boss spawn et.
     * Eski sistemdeki gibi cooldown ve duyuru yapar; savaş davranışı ve arena
     * etkileri yeni, sadeleştirilmiş sistem üzerinden yönetilir.
     */
    public boolean spawnBossFromRitual(Location loc, BossType type, UUID ownerId) {
        if (loc == null || loc.getWorld() == null || type == null) {
            return false;
        }

        // Cooldown kontrolü (yükseklik sabitlenmiş lokasyon üzerinden)
        Location ritualLoc = loc.clone();
        ritualLoc.setY(ritualLoc.getBlockY());
        Long lastUse = ritualCooldowns.get(ritualLoc);
        if (lastUse != null && System.currentTimeMillis() - lastUse < RITUAL_COOLDOWN) {
            return false;
        }

        LivingEntity bossEntity = spawnUsingNewSystem(loc, type);
        if (bossEntity == null) {
            bossEntity = spawnFallbackVanillaBoss(loc, type);
        }
        if (bossEntity == null) {
            return false;
        }

        // BossData oluştur (faz ve zayıflık bilgileri ile)
        int maxPhase = getBossMaxPhase(type);
        List<BossWeakness> weaknesses = getBossWeaknesses(type);
        BossData bossData = new BossData(type, bossEntity, ownerId, maxPhase, weaknesses);
        activeBosses.put(bossEntity.getUniqueId(), bossData);

        // BossBar oluştur (boss canlarının gösterilmesi için)
        createBossBar(bossEntity, type);

        // Arena dönüşümünü başlat (güçlü boss'lar için yayılmalı alan)
        try {
            me.mami.stratocraft.manager.NewBossArenaManager arenaMgr =
                    me.mami.stratocraft.Main.getInstance().getNewBossArenaManager();
            if (arenaMgr != null) {
                int level = getDefaultLevelForType(type);
                arenaMgr.startArenaTransformation(loc, type, level, bossEntity.getUniqueId());
            }
        } catch (Exception ignored) {
            // Arena sistemi yoksa sessizce geç
        }

        // Cooldown güncelle
        ritualCooldowns.put(ritualLoc, System.currentTimeMillis());

        // Oyunculara duyuru
        String bossName = getBossDisplayName(type);
        loc.getWorld().getPlayers().forEach(p -> {
            if (p.getLocation().distance(loc) <= 50) {
                p.sendTitle("§c§l" + bossName, "§eÇağrıldı!", 20, 60, 20);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            }
        });

        return true;
    }

    /**
     * Zorluk seviyesine göre doğada boss spawn etmeyi dener.
     * WorldGenerationListener bu metodu kullanıyor.
     */
    public void trySpawnBossInNature(Location loc, int difficultyLevel) {
        if (loc == null || loc.getWorld() == null) return;

        double chance = getBossSpawnChance(difficultyLevel);
        if (chance <= 0 || new Random().nextDouble() > chance) {
            return;
        }

        BossType type = getRandomBossForLevel(difficultyLevel);
        if (type == null) return;

        spawnBossFromRitual(loc, type, null);

        plugin.getLogger().info("[BossManager] Doğada boss spawn edildi: " +
                getBossDisplayName(type) + " (Seviye " + difficultyLevel + ")");
    }

    // ---- Yardımcılar ----------------------------------------------------

    private int getBossMaxPhase(BossType type) {
        switch (type) {
            case GOBLIN_KING:
            case ORC_CHIEF:
            case TROLL_KING:
            case TREX:
                return 1;
            case DRAGON:
            case CYCLOPS:
            case HELL_DRAGON:
                return 2;
            case TITAN_GOLEM:
            case HYDRA:
            case CHAOS_GOD:
                return 3;
            default:
                return 1;
        }
    }

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
            default:
                // diğer bossların belirgin zayıflığı yok
                break;
        }
        return weaknesses;
    }

    private double getBossSpawnChance(int difficultyLevel) {
        switch (difficultyLevel) {
            case 1:
                return 0.01;   // %1
            case 2:
                return 0.015;  // %1.5
            case 3:
                return 0.02;   // %2
            case 4:
                return 0.025;  // %2.5
            case 5:
                return 0.03;   // %3
            default:
                return 0.0;
        }
    }

    private BossType getRandomBossForLevel(int difficultyLevel) {
        List<BossType> available = new ArrayList<>();
        switch (difficultyLevel) {
            case 1:
                available.add(BossType.GOBLIN_KING);
                available.add(BossType.ORC_CHIEF);
                break;
            case 2:
                available.add(BossType.ORC_CHIEF);
                available.add(BossType.TROLL_KING);
                break;
            case 3:
                available.add(BossType.DRAGON);
                available.add(BossType.TREX);
                available.add(BossType.CYCLOPS);
                break;
            case 4:
                available.add(BossType.CYCLOPS);
                available.add(BossType.TITAN_GOLEM);
                available.add(BossType.HELL_DRAGON);
                available.add(BossType.HYDRA);
                break;
            case 5:
                available.add(BossType.HYDRA);
                available.add(BossType.CHAOS_GOD);
                break;
            default:
                break;
        }
        if (available.isEmpty()) {
            return null;
        }
        return available.get(new Random().nextInt(available.size()));
    }

    /**
     * Mümkünse yeni boss sistemini kullanarak boss spawn eder.
     */
    private LivingEntity spawnUsingNewSystem(Location loc, BossType type) {
        // Yeni, birleşik sistemde doğrudan bu sınıf boss’ları yönetiyor; arena
        // dönüşümü ise NewBossArenaManager tarafından yapılacak.
        // Şimdilik burada ekstra bir yönlendirme yapılmıyor.
        return null;
    }

    private int getDefaultLevelForType(BossType type) {
        switch (type) {
            case GOBLIN_KING:
            case ORC_CHIEF:
                return 1;
            case TROLL_KING:
                return 2;
            case DRAGON:
            case TREX:
            case CYCLOPS:
                return 3;
            case TITAN_GOLEM:
            case HELL_DRAGON:
            case HYDRA:
            case PHOENIX:
                return 4;
            case VOID_DRAGON:
            case CHAOS_TITAN:
            case CHAOS_GOD:
                return 5;
            default:
                return 1;
        }
    }

    /**
     * Yeni sistem başarısız olursa basit vanilla entity spawn fallback'i.
     * Burada HİÇBİR blok kırma / kazma mekaniği yoktur.
     */
    private LivingEntity spawnFallbackVanillaBoss(Location loc, BossType type) {
        EntityType entityType;
        String name;
        double maxHealth;

        switch (type) {
            case GOBLIN_KING:
                entityType = EntityType.ZOMBIE;
                name = "§2§lGOBLIN KRALI";
                maxHealth = 150.0;
                break;
            case ORC_CHIEF:
                entityType = EntityType.ZOMBIE;
                name = "§c§lORK ŞEFİ";
                maxHealth = 200.0;
                break;
            case TROLL_KING:
                entityType = EntityType.ZOMBIE;
                name = "§5§lTROLL KRALI";
                maxHealth = 300.0;
                break;
            case DRAGON:
                entityType = EntityType.PHANTOM;
                name = "§4§lEJDERHA";
                maxHealth = 500.0;
                break;
            case TREX:
                entityType = EntityType.RAVAGER;
                name = "§c§lT-REX";
                maxHealth = 600.0;
                break;
            case CYCLOPS:
                entityType = EntityType.GIANT;
                name = "§6§lTEK GÖZLÜ DEV";
                maxHealth = 700.0;
                break;
            case TITAN_GOLEM:
                entityType = EntityType.IRON_GOLEM;
                name = "§7§lTITAN GOLEM";
                maxHealth = 800.0;
                break;
            case HELL_DRAGON:
                entityType = EntityType.PHANTOM;
                name = "§4§lCEHENNEM EJDERİ";
                maxHealth = 900.0;
                break;
            case HYDRA:
                entityType = EntityType.GUARDIAN;
                name = "§3§lHYDRA";
                maxHealth = 850.0;
                break;
            case PHOENIX:
                entityType = EntityType.BLAZE;
                name = "§6§lPHOENIX";
                maxHealth = 600.0;
                break;
            case VOID_DRAGON:
                entityType = EntityType.ENDER_DRAGON;
                name = "§5§lHİÇLİK EJDERİ";
                maxHealth = 1200.0;
                break;
            case CHAOS_TITAN:
                entityType = EntityType.WITHER;
                name = "§5§lKAOS TİTANI";
                maxHealth = 1100.0;
                break;
            case CHAOS_GOD:
                entityType = EntityType.WITHER;
                name = "§5§lKHAOS TANRISI";
                maxHealth = 1300.0;
                break;
            default:
                return null;
        }

        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, entityType);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        }
        entity.setHealth(maxHealth);
        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);
        return entity;
    }
}

// ==== Eski, TAM BossManager implementasyonu aşağıda yorum olarak tutuluyor ====
//package me.mami.stratocraft.manager;
//
//import me.mami.stratocraft.Main;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.entity.*;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.util.Vector;
//
//import java.util.*;
//
///**
// * Boss Sistemi
// *
// * 10 farklı boss
// * Her boss için ritüel çağırma
// * Özel hareketler (ateş, yıldırım, patlama, blok fırlatma, zehir vb.)
// * Zayıf noktalar (güçlü bosslar için)
// * Faz sistemi (güçlü bosslar için 2-3 faz)
// */
//public class BossManager {
//    private final Main plugin;
//    private final MobManager mobManager;
//
//    // Aktif bosslar (Entity -> BossData)
//    private final Map<UUID, BossData> activeBosses = new HashMap<>();
//
//    // BossBar'lar (Entity UUID -> BossBar)
//    private final Map<UUID, org.bukkit.boss.BossBar> bossBars = new HashMap<>();
//    private org.bukkit.scheduler.BukkitTask bossBarUpdateTask = null;
//    private org.bukkit.scheduler.BukkitTask locationUpdateTask = null;
//
//    // Config ayarları
//    private int maxBossBarDistance = 100;  // BossBar gösterim mesafesi (blok)
//    private int maxBossBars = 3;            // Maksimum gösterilecek BossBar sayısı
//    private boolean showLocation = true;    // ActionBar'da konum bilgisi göster
//
//    // AI ayarları
//    private long abilityCooldownMin = 4000;  // Minimum yetenek cooldown (ms)
//    private long abilityCooldownMax = 8000;  // Maksimum yetenek cooldown (ms)
//    private int closeRange = 5;              // Yakın mesafe (blok)
//    private int mediumRange = 15;            // Orta mesafe (blok)
//    private int farRange = 30;               // Uzak mesafe (blok)
//    private boolean useSmartTargeting = true;  // Akıllı hedef seçimi
//    private boolean avoidSelfDamage = true;    // Kendine zarar vermeme
//    private boolean usePathfinding = true;    // Pathfinding kullan
//    private double minHealthForHeal = 0.3;     // İyileştirme için min can %
//    private double minHealthForTeleport = 0.2; // Işınlanma için min can %
//
//    // Yetenek öncelikleri
//    private Map<BossAbility, Integer> abilityPriorities = new HashMap<>();
//
//    // Zayıf nokta sistemi
//    private Map<UUID, Long> weakPointCooldowns = new HashMap<>();
//    private static final long WEAK_POINT_DURATION = 5000L; // 5 saniye
//    private static final long WEAK_POINT_COOLDOWN = 15000L; // 15 saniye cooldown
//
//    // Savunma mekanizmaları
//    private Map<UUID, Long> shieldCooldowns = new HashMap<>();
//    private static final long SHIELD_DURATION = 3000L; // 3 saniye
//    private static final long SHIELD_COOLDOWN = 20000L; // 20 saniye cooldown
//
//    // Kombo sistemi
//    private Map<UUID, List<BossAbility>> lastAbilities = new HashMap<>(); // Son kullanılan yetenekler
//    private static final int COMBO_CHANCE = 30; // %30 şans kombo için
//
//    // Ritüel cooldown (Location -> Long)
//    private final Map<Location, Long> ritualCooldowns = new HashMap<>();
//    private static final long RITUAL_COOLDOWN = 60000L; // 1 dakika
//
//    public enum BossType {
//        GOBLIN_KING, // Seviye 1 - Goblin Kralı
//        ORC_CHIEF, // Seviye 1-2 - Ork Şefi
//        TROLL_KING, // Seviye 2 - Troll Kralı
//        DRAGON, // Seviye 3 - Ejderha (2 faz)
//        TREX, // Seviye 3 - T-Rex
//        CYCLOPS, // Seviye 3-4 - Cyclops (2 faz)
//        TITAN_GOLEM, // Seviye 4 - Titan Golem (3 faz, zayıf: alev)
//        HELL_DRAGON, // Seviye 4 - Cehennem Ejderi (2 faz, zayıf: su)
//        HYDRA, // Seviye 4-5 - Hydra (3 faz, zayıf: zehir)
//        PHOENIX, // Seviye 4 - Phoenix (2 faz, zayıf: su)
//        VOID_DRAGON, // Seviye 5 - Void Dragon (3 faz)
//        CHAOS_TITAN, // Seviye 5 - Chaos Titan (3 faz)
//        CHAOS_GOD // Seviye 5 - Khaos Tanrısı (3 faz, zayıf: alev, zehir)
//    }
//
//    public enum BossWeakness {
//        FIRE, // Alev zayıflığı
//        WATER, // Su zayıflığı
//        POISON, // Zehir zayıflığı
//        LIGHTNING // Yıldırım zayıflığı
//    }
//
//    public enum BossAbility {
//        FIRE_BREATH, // Ateş püskürtme
//        EXPLOSION, // Patlama yapma
//        LIGHTNING_STRIKE, // Yıldırım atma
//        BLOCK_THROW, // Blok fırlatma
//        POISON_CLOUD, // Zehir bulutu
//        TELEPORT, // Işınlanma
//        CHARGE, // Koşu saldırısı
//        SUMMON_MINIONS, // Minyon çağırma
//        HEAL, // Kendini iyileştirme
//        SHOCKWAVE // Şok dalgası
//    }
//
//    public static class BossData {
//        private final BossType type;
//        private final LivingEntity entity;
//        private final UUID ownerId; // Ritüel yapan oyuncu
//        private int phase;
//        private final int maxPhase;
//        private final List<BossWeakness> weaknesses;
//        private final Map<Integer, List<BossAbility>> phaseAbilities; // Faz -> Hareketler
//        private long lastAbilityTime;
//        private long currentCooldown = 4000L; // Dinamik cooldown
//
//        public BossData(BossType type, LivingEntity entity, UUID ownerId, int maxPhase,
//                List<BossWeakness> weaknesses, Map<Integer, List<BossAbility>> phaseAbilities) {
//            this.type = type;
//            this.entity = entity;
//            this.ownerId = ownerId;
//            this.phase = 1;
//            this.maxPhase = maxPhase;
//            this.weaknesses = weaknesses;
//            this.phaseAbilities = phaseAbilities;
//            this.lastAbilityTime = System.currentTimeMillis();
//        }
//
//        public BossType getType() {
//            return type;
//        }
//
//        public LivingEntity getEntity() {
//            return entity;
//        }
//
//        public UUID getOwnerId() {
//            return ownerId;
//        }
//
//        public int getPhase() {
//            return phase;
//        }
//
//        public int getMaxPhase() {
//            return maxPhase;
//        }
//
//        public List<BossWeakness> getWeaknesses() {
//            return weaknesses;
//        }
//
//        public List<BossAbility> getCurrentAbilities() {
//            return phaseAbilities.getOrDefault(phase, new ArrayList<>());
//        }
//
//        public long getLastAbilityTime() {
//            return lastAbilityTime;
//        }
//
//        public void setLastAbilityTime(long time) {
//            this.lastAbilityTime = time;
//        }
//
//        public void nextPhase() {
//            if (phase < maxPhase) {
//                phase++;
//            }
//        }
//
//        public boolean canUseAbility() {
//            return System.currentTimeMillis() - lastAbilityTime >= currentCooldown;
//        }
//
//        public void setCurrentCooldown(long cooldown) {
//            this.currentCooldown = cooldown;
//        }
//
//        public long getCurrentCooldown() {
//            return currentCooldown;
//        }
//    }
//
//    private BossArenaManager bossArenaManager;
//
//    public BossManager(Main plugin) {
//        this.plugin = plugin;
//        if (plugin == null) {
//            throw new IllegalArgumentException("Plugin cannot be null!");
//        }
//        this.mobManager = plugin.getMobManager();
//        if (mobManager == null) {
//            throw new IllegalStateException("MobManager must be initialized before BossManager!");
//        }
//        this.bossArenaManager = plugin.getBossArenaManager();
//        loadConfig();
//        loadBosses();
//        startBossAbilityTask();
//        startBossMusicTask();
//        startWeakPointTask();
//    }
//
//    /**
//     * Config ayarlarını yükle
//     */
//    private void loadConfig() {
//        // Config anahtarlarını kontrol et - hem eski hem yeni formatı destekle
//        maxBossBarDistance = plugin.getConfig().getInt("boss.bossbar.bossbar-display-range",
//            plugin.getConfig().getInt("boss.bossbar.max-distance", 100));
//        maxBossBars = plugin.getConfig().getInt("boss.bossbar.max-bossbars-per-player",
//            plugin.getConfig().getInt("boss.bossbar.max-bars", 3));
//        showLocation = plugin.getConfig().getBoolean("boss.bossbar.show-location", true);
//
//        // AI ayarları
//        abilityCooldownMin = plugin.getConfig().getLong("boss.ai.ability-cooldown-min", 4000);
//        abilityCooldownMax = plugin.getConfig().getLong("boss.ai.ability-cooldown-max", 8000);
//        closeRange = plugin.getConfig().getInt("boss.ai.close-range", 5);
//        mediumRange = plugin.getConfig().getInt("boss.ai.medium-range", 15);
//        farRange = plugin.getConfig().getInt("boss.ai.far-range", 30);
//        useSmartTargeting = plugin.getConfig().getBoolean("boss.ai.use-smart-targeting", true);
//        avoidSelfDamage = plugin.getConfig().getBoolean("boss.ai.avoid-self-damage", true);
//        usePathfinding = plugin.getConfig().getBoolean("boss.ai.use-pathfinding", true);
//        minHealthForHeal = plugin.getConfig().getDouble("boss.ai.min-health-for-heal", 0.3);
//        minHealthForTeleport = plugin.getConfig().getDouble("boss.ai.min-health-for-teleport", 0.2);
//
//        // Yetenek önceliklerini yükle
//        abilityPriorities.put(BossAbility.HEAL, plugin.getConfig().getInt("boss.ai.priority.heal", 10));
//        abilityPriorities.put(BossAbility.TELEPORT, plugin.getConfig().getInt("boss.ai.priority.teleport", 8));
//        abilityPriorities.put(BossAbility.SUMMON_MINIONS, plugin.getConfig().getInt("boss.ai.priority.summon-minions", 6));
//        abilityPriorities.put(BossAbility.CHARGE, plugin.getConfig().getInt("boss.ai.priority.charge", 5));
//        abilityPriorities.put(BossAbility.FIRE_BREATH, plugin.getConfig().getInt("boss.ai.priority.fire-breath", 4));
//        abilityPriorities.put(BossAbility.LIGHTNING_STRIKE, plugin.getConfig().getInt("boss.ai.priority.lightning-strike", 4));
//        abilityPriorities.put(BossAbility.BLOCK_THROW, plugin.getConfig().getInt("boss.ai.priority.block-throw", 3));
//        abilityPriorities.put(BossAbility.SHOCKWAVE, plugin.getConfig().getInt("boss.ai.priority.shockwave", 3));
//        abilityPriorities.put(BossAbility.POISON_CLOUD, plugin.getConfig().getInt("boss.ai.priority.poison-cloud", 2));
//        abilityPriorities.put(BossAbility.EXPLOSION, plugin.getConfig().getInt("boss.ai.priority.explosion", 1));
//    }
//
//    /**
//     * Boss yeteneklerini sürekli kontrol et
//     */
//    private void startBossAbilityTask() {
//        int checkInterval = plugin.getConfig().getInt("boss.ai.ability-check-interval", 20);
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                for (BossData boss : new ArrayList<>(activeBosses.values())) {
//                    LivingEntity entity = boss.getEntity();
//                    if (entity == null || entity.isDead()) {
//                        if (entity != null) {
//                            activeBosses.remove(entity.getUniqueId());
//                        }
//                        continue;
//                    }
//
//                    // Pathfinding ile hedefe yaklaş
//                    if (usePathfinding) {
//                        updateBossPathfinding(boss);
//                    }
//
//                    // Yetenek kullan (akıllı seçim)
//                    if (boss.canUseAbility()) {
//                        useSmartAbility(boss);
//                    }
//
//                    // Faz kontrolü (can %'sine göre)
//                    checkPhaseTransition(boss);
//                }
//            }
//        }.runTaskTimer(plugin, 20L, checkInterval);
//    }
//
//    /**
//     * Boss müzik task'ını başlat
//     */
//    private void startBossMusicTask() {
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                // Optimizasyon: Sadece aktif boss'ları kontrol et
//                if (activeBosses.isEmpty()) return;
//
//                for (BossData boss : new ArrayList<>(activeBosses.values())) {
//                    LivingEntity entity = boss.getEntity();
//                    if (entity == null || !entity.isValid() || entity.isDead()) continue;
//
//                    try {
//                        Location bossLoc = entity.getLocation();
//                        if (bossLoc == null || bossLoc.getWorld() == null) continue;
//
//                        org.bukkit.attribute.AttributeInstance maxHealthAttr =
//                            entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
//                        if (maxHealthAttr == null || maxHealthAttr.getValue() <= 0) continue;
//
//                        double healthPercent = entity.getHealth() / maxHealthAttr.getValue();
//
//                        // Optimizasyon: Sadece yakındaki oyunculara müzik çal
//                        List<Player> nearbyPlayers = bossLoc.getWorld().getPlayers();
//                        for (Player player : nearbyPlayers) {
//                            if (player == null || !player.isOnline()) continue;
//
//                            try {
//                                double distance = player.getLocation().distance(bossLoc);
//                                if (distance <= 50) {
//                                    playBossMusic(player, boss, distance, healthPercent);
//                                }
//                            } catch (Exception e) {
//                                // Oyuncu konumu alınamadı, atla
//                            }
//                        }
//                    } catch (Exception e) {
//                        // Boss entity geçersiz, atla
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 100L, 200L); // Her 10 saniye (müzik uzun sürer)
//    }
//
//    /**
//     * Boss müziği çal (faz ve can durumuna göre)
//     */
//    private void playBossMusic(Player player, BossData boss, double distance, double healthPercent) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null) return;
//
//        Location bossLoc = entity.getLocation();
//
//        // Mesafeye göre ses seviyesi
//        float volume = (float) Math.max(0.1, 1.0 - (distance / 50.0));
//
//        // Faz ve can durumuna göre müzik
//        org.bukkit.Sound musicSound = null;
//        float pitch = 1.0f;
//
//        if (healthPercent < 0.3) {
//            // Kritik durum - yoğun müzik
//            musicSound = org.bukkit.Sound.MUSIC_DISC_WARD;
//            pitch = 0.8f;
//        } else if (boss.getPhase() >= 2) {
//            // Faz 2+ - orta yoğunluk
//            musicSound = org.bukkit.Sound.MUSIC_DISC_PIGSTEP;
//            pitch = 1.0f;
//        } else {
//            // Normal - hafif müzik
//            musicSound = org.bukkit.Sound.MUSIC_DISC_OTHERSIDE;
//            pitch = 1.2f;
//        }
//
//        // Müzik çal (sadece yakınsa)
//        if (distance <= 30 && musicSound != null) {
//            player.playSound(bossLoc, musicSound, volume, pitch);
//        }
//    }
//
//    /**
//     * Zayıf nokta task'ını başlat
//     */
//    private void startWeakPointTask() {
//        new BukkitRunnable() {
//            private final Random random = new Random(); // Optimizasyon: Tek Random instance
//
//            @Override
//            public void run() {
//                // Optimizasyon: Sadece aktif boss'ları kontrol et
//                if (activeBosses.isEmpty()) return;
//
//                long currentTime = System.currentTimeMillis();
//
//                for (BossData boss : new ArrayList<>(activeBosses.values())) {
//                    LivingEntity entity = boss.getEntity();
//                    if (entity == null || !entity.isValid() || entity.isDead()) continue;
//
//                    try {
//                        UUID bossId = entity.getUniqueId();
//
//                        // Zayıf nokta kontrolü (her 15 saniyede bir aktif olabilir)
//                        Long weakPointTime = weakPointCooldowns.get(bossId);
//                        if (weakPointTime == null || currentTime > weakPointTime + WEAK_POINT_COOLDOWN) {
//                            // Rastgele zayıf nokta aktif et (%30 şans)
//                            if (random.nextDouble() < 0.3) {
//                                activateWeakPoint(boss);
//                            }
//                        }
//
//                        // Kalkan kontrolü (can düşükse)
//                        org.bukkit.attribute.AttributeInstance maxHealthAttr =
//                            entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
//                        if (maxHealthAttr != null && maxHealthAttr.getValue() > 0) {
//                            double healthPercent = entity.getHealth() / maxHealthAttr.getValue();
//                            if (healthPercent < 0.5 && !shieldCooldowns.containsKey(bossId)) {
//                                // Rastgele kalkan aktif et (%20 şans)
//                                if (random.nextDouble() < 0.2) {
//                                    activateShield(boss);
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        // Boss entity geçersiz, atla
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 100L, 300L); // Her 15 saniye
//    }
//
//    /**
//     * Zayıf nokta aktif et
//     */
//    private void activateWeakPoint(BossData boss) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) return;
//
//        UUID bossId = entity.getUniqueId();
//        weakPointCooldowns.put(bossId, System.currentTimeMillis() + WEAK_POINT_DURATION);
//
//        // Görsel gösterge - başın etrafında parlak partiküller
//        new BukkitRunnable() {
//            int ticks = 0;
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 100 || entity.isDead() || // 5 saniye
//                    !weakPointCooldowns.containsKey(bossId) ||
//                    System.currentTimeMillis() > weakPointCooldowns.get(bossId)) {
//                    weakPointCooldowns.remove(bossId);
//                    cancel();
//                    return;
//                }
//
//                Location headLoc = entity.getLocation().add(0, 2, 0);
//                entity.getWorld().spawnParticle(
//                    org.bukkit.Particle.END_ROD,
//                    headLoc, 20, 0.3, 0.3, 0.3, 0.1
//                );
//
//                // Oyunculara uyarı
//                for (Player player : entity.getWorld().getPlayers()) {
//                    if (player.getLocation().distance(entity.getLocation()) <= 30) {
//                        player.sendActionBar(
//                            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
//                                .deserialize("§e§l⚡ ZAYIF NOKTA AÇIK! BAŞA SALDIR!")
//                        );
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 5L);
//
//        // Ses efekti
//        entity.getWorld().playSound(entity.getLocation(),
//            org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
//    }
//
//    /**
//     * Zayıf nokta aktif mi?
//     */
//    public boolean isWeakPointActive(UUID bossId) {
//        return weakPointCooldowns.containsKey(bossId) &&
//               System.currentTimeMillis() < weakPointCooldowns.get(bossId);
//    }
//
//    /**
//     * Kalkan aktif et
//     */
//    private void activateShield(BossData boss) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) return;
//
//        UUID bossId = entity.getUniqueId();
//        shieldCooldowns.put(bossId, System.currentTimeMillis() + SHIELD_DURATION);
//
//        // Görsel gösterge - kalkan partikülleri
//        new BukkitRunnable() {
//            int ticks = 0;
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 60 || entity.isDead() || // 3 saniye
//                    !shieldCooldowns.containsKey(bossId) ||
//                    System.currentTimeMillis() > shieldCooldowns.get(bossId)) {
//                    shieldCooldowns.remove(bossId);
//                    cancel();
//                    return;
//                }
//
//                // Kalkan partikülleri (dairesel)
//                for (int i = 0; i < 360; i += 10) {
//                    double angle = Math.toRadians(i + ticks * 5);
//                    Location particleLoc = entity.getLocation().add(
//                        Math.cos(angle) * 2,
//                        1 + Math.sin(angle * 2) * 0.5,
//                        Math.sin(angle) * 2
//                    );
//                    entity.getWorld().spawnParticle(
//                        org.bukkit.Particle.END_ROD,
//                        particleLoc, 1, 0, 0, 0, 0
//                    );
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
//
//        // Oyunculara bildir
//        for (Player player : entity.getWorld().getPlayers()) {
//            if (player.getLocation().distance(entity.getLocation()) <= 30) {
//                player.sendMessage("§b§l🛡️ BOSS KALKAN OLUŞTURDU!");
//            }
//        }
//
//        // Kalkan cooldown'u
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                shieldCooldowns.put(bossId, System.currentTimeMillis() + SHIELD_COOLDOWN);
//            }
//        }.runTaskLater(plugin, 60L); // 3 saniye sonra cooldown başlar
//    }
//
//    /**
//     * Kalkan aktif mi?
//     */
//    public boolean isShieldActive(UUID bossId) {
//        return shieldCooldowns.containsKey(bossId) &&
//               System.currentTimeMillis() < shieldCooldowns.get(bossId);
//    }
//
//    /**
//     * Pathfinding ile boss'u hedefe yaklaştır
//     */
//    private void updateBossPathfinding(BossData boss) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null) return;
//
//        Player target = findNearestPlayer(entity.getLocation(), farRange);
//        if (target == null) return;
//
//        double distance = entity.getLocation().distance(target.getLocation());
//
//        // Eğer çok uzaktaysa yaklaş
//        if (distance > mediumRange) {
//            // Pathfinding: Hedefe doğru hareket et
//            Location targetLoc = target.getLocation();
//            Location bossLoc = entity.getLocation();
//
//            Vector direction = targetLoc.toVector().subtract(bossLoc.toVector()).normalize();
//            direction.multiply(0.3); // Hareket hızı
//            direction.setY(0); // Y eksenini sıfırla (uçmayı engelle)
//
//            Location newLoc = bossLoc.clone().add(direction);
//
//            // Güvenli konum kontrolü (havada kalmayı engelle)
//            if (newLoc.getBlock().getType().isAir() &&
//                newLoc.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
//                // Güvenli konum - hareket et
//                entity.setVelocity(direction);
//            } else {
//                // Güvenli değil - zıpla
//                if (entity.isOnGround()) {
//                    entity.setVelocity(new Vector(direction.getX(), 0.4, direction.getZ()));
//                }
//            }
//        }
//    }
//
//    /**
//     * Faz geçişi kontrolü
//     */
//    private void checkPhaseTransition(BossData boss) {
//        if (boss.getPhase() >= boss.getMaxPhase()) {
//            return; // Son fazda
//        }
//
//        double healthPercent = boss.getEntity().getHealth() / boss.getEntity().getAttribute(
//                org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
//
//        // Faz geçişi eşikleri
//        if (boss.getMaxPhase() == 2) {
//            // 2 fazlı boss: %50'de faz değişir
//            if (healthPercent <= 0.5 && boss.getPhase() == 1) {
//                boss.nextPhase();
//                announcePhaseChange(boss);
//            }
//        } else if (boss.getMaxPhase() == 3) {
//            // 3 fazlı boss: %66 ve %33'te faz değişir
//            if (healthPercent <= 0.66 && boss.getPhase() == 1) {
//                boss.nextPhase();
//                announcePhaseChange(boss);
//            } else if (healthPercent <= 0.33 && boss.getPhase() == 2) {
//                boss.nextPhase();
//                announcePhaseChange(boss);
//            }
//        }
//    }
//
//    /**
//     * Faz değişimi duyurusu (Epik animasyonlar ile)
//     */
//    private void announcePhaseChange(BossData boss) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) return;
//
//        Location loc = entity.getLocation();
//        String bossName = getBossDisplayName(boss.getType());
//
//        // 1. Büyük patlama efekti
//        epicPhaseTransitionExplosion(loc);
//
//        // 2. Ekran titremesi ve duyuru
//        for (Player player : loc.getWorld().getPlayers()) {
//            double distance = player.getLocation().distance(loc);
//            if (distance <= 50) {
//                // Title
//                player.sendTitle("§c§l" + bossName, "§e§lFAZ " + boss.getPhase() + "!", 10, 60, 20);
//
//                // Ses
//                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
//                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
//
//                // Ekran titremesi (velocity ile)
//                if (distance <= 30) {
//                    player.setVelocity(new Vector(
//                        (Math.random() - 0.5) * 0.3,
//                        0.2,
//                        (Math.random() - 0.5) * 0.3
//                    ));
//                }
//            }
//        }
//
//        // 3. Boss büyümesi/küçülmesi (faz 2'de büyür) - Paper API ile
//        // Not: setScale sadece bazı entity'lerde var, kontrol et
//        try {
//            if (entity instanceof org.bukkit.entity.Mob) {
//                // Mob entity'ler için scale API kullanılabilir (Paper)
//                if (boss.getPhase() == 2) {
//                    // Faz 2 için görsel efekt (boyut değişimi yerine partikül)
//                    for (int i = 0; i < 20; i++) {
//                        entity.getWorld().spawnParticle(
//                            org.bukkit.Particle.CLOUD,
//                            entity.getLocation().add(0, i * 0.1, 0),
//                            1, 0.2, 0.2, 0.2, 0.05
//                        );
//                    }
//
//                    // Faz 2'de çevresel tehlikeler başlar
//                    createPhase2EnvironmentalHazards(loc, boss.getType());
//                } else if (boss.getPhase() == 3) {
//                    // Faz 3 için daha büyük efekt
//                    for (int i = 0; i < 30; i++) {
//                        entity.getWorld().spawnParticle(
//                            org.bukkit.Particle.CLOUD,
//                            entity.getLocation().add(0, i * 0.1, 0),
//                            1, 0.3, 0.3, 0.3, 0.05
//                        );
//                    }
//
//                    // Faz 3'te yoğun çevresel tehlikeler
//                    createPhase3EnvironmentalHazards(loc, boss.getType());
//                }
//            }
//        } catch (Exception e) {
//            // Scale API yoksa görsel efektler yeterli
//        }
//    }
//
//    /**
//     * Faz 2 çevresel tehlikeler
//     */
//    private void createPhase2EnvironmentalHazards(Location center, BossType bossType) {
//        if (center == null || center.getWorld() == null) return;
//
//        // Faz 2'de lav akıntıları ve küçük tehlikeler
//        new BukkitRunnable() {
//            int ticks = 0;
//            private final Random random = new Random();
//            private final org.bukkit.World world = center.getWorld();
//
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 600 || world == null) { // 30 saniye
//                    cancel();
//                    return;
//                }
//
//                // Her 3 saniyede bir lav akıntısı
//                if (ticks % 60 == 0) {
//                    for (int i = 0; i < 2; i++) {
//                        try {
//                            double angle = random.nextDouble() * 2 * Math.PI;
//                            double distance = 5 + random.nextDouble() * 5;
//                            Location lavaLoc = center.clone().add(
//                                Math.cos(angle) * distance,
//                                -1,
//                                Math.sin(angle) * distance
//                            );
//
//                            Block block = lavaLoc.getBlock();
//                            if (block.getType().isSolid() &&
//                                block.getType() != Material.BEDROCK) {
//                                block.setType(Material.LAVA);
//                            }
//                        } catch (Exception e) {
//                            // Blok değiştirilemedi, atla
//                        }
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
//    }
//
//    /**
//     * Faz 3 çevresel tehlikeler
//     */
//    private void createPhase3EnvironmentalHazards(Location center, BossType bossType) {
//        if (center == null || center.getWorld() == null) return;
//
//        // Faz 3'te yoğun tehlikeler
//        new BukkitRunnable() {
//            int ticks = 0;
//            private final Random random = new Random();
//            private final org.bukkit.World world = center.getWorld();
//
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 1200 || world == null) { // 60 saniye
//                    cancel();
//                    return;
//                }
//
//                // Her 2 saniyede bir lav akıntısı
//                if (ticks % 40 == 0) {
//                    for (int i = 0; i < 3; i++) {
//                        try {
//                            double angle = random.nextDouble() * 2 * Math.PI;
//                            double distance = 3 + random.nextDouble() * 7;
//                            Location lavaLoc = center.clone().add(
//                                Math.cos(angle) * distance,
//                                -1,
//                                Math.sin(angle) * distance
//                            );
//
//                            Block block = lavaLoc.getBlock();
//                            if (block.getType().isSolid() &&
//                                block.getType() != Material.BEDROCK) {
//                                block.setType(Material.LAVA);
//                            }
//                        } catch (Exception e) {
//                            // Blok değiştirilemedi, atla
//                        }
//                    }
//                }
//
//                // Her 5 saniyede bir örümcek ağı
//                if (ticks % 100 == 0) {
//                    for (int i = 0; i < 5; i++) {
//                        try {
//                            double angle = random.nextDouble() * 2 * Math.PI;
//                            double distance = random.nextDouble() * 8;
//                            Location webLoc = center.clone().add(
//                                Math.cos(angle) * distance,
//                                1,
//                                Math.sin(angle) * distance
//                            );
//
//                            Block block = webLoc.getBlock();
//                            if (block.getType().isAir()) {
//                                block.setType(Material.COBWEB);
//                            }
//                        } catch (Exception e) {
//                            // Blok değiştirilemedi, atla
//                        }
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
//    }
//
//    /**
//     * Faz geçişi patlama efekti
//     */
//    private void epicPhaseTransitionExplosion(Location loc) {
//        // Büyük patlama partikülleri
//        for (int i = 0; i < 50; i++) {
//            double angle = Math.toRadians(i * 7.2); // 360 derece
//            double radius = 3 + Math.random() * 2;
//            Location particleLoc = loc.clone().add(
//                Math.cos(angle) * radius,
//                Math.random() * 3,
//                Math.sin(angle) * radius
//            );
//
//            loc.getWorld().spawnParticle(
//                org.bukkit.Particle.EXPLOSION_LARGE,
//                particleLoc, 1, 0, 0, 0, 0
//            );
//
//            // Yan partiküller
//            loc.getWorld().spawnParticle(
//                org.bukkit.Particle.FLAME,
//                particleLoc, 3, 0.2, 0.2, 0.2, 0.05
//            );
//        }
//
//        // Merkez patlama
//        loc.getWorld().spawnParticle(
//            org.bukkit.Particle.EXPLOSION_HUGE,
//            loc.clone().add(0, 1, 0), 1
//        );
//    }
//
//    /**
//     * Akıllı yetenek seçimi ve kullanımı
//     */
//    private void useSmartAbility(BossData boss) {
//        List<BossAbility> abilities = boss.getCurrentAbilities();
//        if (abilities.isEmpty()) {
//            return;
//        }
//
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) return;
//
//        Player target = findNearestPlayer(entity.getLocation(), farRange);
//        double distance = target != null ? entity.getLocation().distance(target.getLocation()) : Double.MAX_VALUE;
//        double healthPercent = entity.getHealth() / entity.getAttribute(
//            org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
//
//        // Yetenek skorlama sistemi
//        Map<BossAbility, Double> abilityScores = new HashMap<>();
//
//        for (BossAbility ability : abilities) {
//            double score = 0.0;
//
//            // Temel öncelik
//            score += abilityPriorities.getOrDefault(ability, 1);
//
//            // Mesafe bazlı skorlama
//            switch (ability) {
//                case CHARGE:
//                case SHOCKWAVE:
//                    // Yakın mesafe yetenekleri
//                    if (distance <= closeRange) {
//                        score += 5.0;
//                    } else if (distance <= mediumRange) {
//                        score += 2.0;
//                    } else {
//                        score -= 3.0; // Çok uzak
//                    }
//                    break;
//
//                case FIRE_BREATH:
//                case BLOCK_THROW:
//                    // Orta mesafe yetenekleri
//                    if (distance > closeRange && distance <= mediumRange) {
//                        score += 4.0;
//                    } else if (distance <= closeRange) {
//                        score += 1.0;
//                    } else {
//                        score -= 2.0;
//                    }
//                    break;
//
//                case LIGHTNING_STRIKE:
//                    // Uzak mesafe yetenekleri
//                    if (distance > mediumRange && distance <= farRange) {
//                        score += 4.0;
//                    } else if (distance > farRange) {
//                        score += 2.0;
//                    } else {
//                        score -= 1.0;
//                    }
//                    break;
//
//                case TELEPORT:
//                    // Işınlanma - can düşükse veya çok uzaksa
//                    if (healthPercent <= minHealthForTeleport && distance > closeRange) {
//                        score += 8.0; // Can çok düşükse öncelikli
//                    } else if (distance > farRange) {
//                        score += 4.0; // Çok uzaksa ışınlan
//                    } else if (distance > mediumRange && distance <= farRange) {
//                        score += 3.0; // Uzaktaysa ışınlan
//                    } else {
//                        score -= 2.0; // Yakınsa ışınlanma
//                    }
//                    break;
//
//                case HEAL:
//                    // Can düşükse iyileştirme öncelikli
//                    if (healthPercent <= minHealthForHeal) {
//                        score += 10.0;
//                    } else {
//                        score -= 5.0; // Can yüksekse kullanma
//                    }
//                    break;
//
//                case EXPLOSION:
//                    // Patlama - sadece hedef yakınsa ve kendine zarar vermeyecekse
//                    if (distance > closeRange && distance <= mediumRange && avoidSelfDamage) {
//                        score += 2.0;
//                    } else {
//                        score -= 5.0; // Kendine zarar verebilir
//                    }
//                    break;
//
//                case SUMMON_MINIONS:
//                    // Minyon çağırma - orta mesafede ve can düşükse
//                    if (distance > closeRange && healthPercent < 0.7) {
//                        score += 3.0;
//                    }
//                    break;
//
//                case POISON_CLOUD:
//                    // Zehir bulutu - orta mesafede
//                    if (distance > closeRange && distance <= mediumRange) {
//                        score += 3.0;
//                    }
//                    break;
//            }
//
//            // Görüş hattı kontrolü (uzaktan saldırılar için)
//            if (useSmartTargeting && target != null) {
//                if (ability == BossAbility.LIGHTNING_STRIKE ||
//                    ability == BossAbility.FIRE_BREATH ||
//                    ability == BossAbility.BLOCK_THROW) {
//                    if (hasLineOfSight(entity, target)) {
//                        score += 2.0;
//                    } else {
//                        score -= 3.0; // Görüş hattı yok
//                    }
//                }
//            }
//
//            abilityScores.put(ability, score);
//        }
//
//        // En yüksek skorlu yeteneği seç
//        BossAbility bestAbility = abilities.get(0);
//        double bestScore = abilityScores.getOrDefault(bestAbility, 0.0);
//
//        for (BossAbility ability : abilities) {
//            double score = abilityScores.getOrDefault(ability, 0.0);
//            if (score > bestScore) {
//                bestScore = score;
//                bestAbility = ability;
//            }
//        }
//
//        // Eğer tüm yetenekler negatif skorluysa, en az negatif olanı seç
//        if (bestScore < 0) {
//            for (BossAbility ability : abilities) {
//                double score = abilityScores.getOrDefault(ability, 0.0);
//                if (score > bestScore) {
//                    bestScore = score;
//                    bestAbility = ability;
//                }
//            }
//        }
//
//        // Kombo kontrolü (%30 şans)
//        UUID bossId = entity.getUniqueId();
//        List<BossAbility> lastBossAbilities = lastAbilities.getOrDefault(bossId, new ArrayList<>());
//
//        if (new Random().nextInt(100) < COMBO_CHANCE && !lastBossAbilities.isEmpty()) {
//            // Kombo yapılabilir - son yeteneğe göre kombo seç
//            BossAbility comboStarter = lastBossAbilities.get(lastBossAbilities.size() - 1);
//            List<BossAbility> comboChain = getComboChain(comboStarter, abilities);
//
//            if (!comboChain.isEmpty()) {
//                // Kombo başlat
//                executeCombo(boss, comboChain);
//                lastBossAbilities.addAll(comboChain);
//                // Son 3 yeteneği tut
//                if (lastBossAbilities.size() > 3) {
//                    lastBossAbilities = lastBossAbilities.subList(lastBossAbilities.size() - 3, lastBossAbilities.size());
//                }
//                lastAbilities.put(bossId, lastBossAbilities);
//                return; // Kombo kullanıldı, normal yetenek kullanma
//            }
//        }
//
//        // Normal yetenek kullanımı
//        lastBossAbilities.add(bestAbility);
//        if (lastBossAbilities.size() > 3) {
//            lastBossAbilities = lastBossAbilities.subList(lastBossAbilities.size() - 3, lastBossAbilities.size());
//        }
//        lastAbilities.put(bossId, lastBossAbilities);
//
//        // Tehdit uyarısı göster (büyük saldırılar için)
//        if (shouldShowThreatWarning(bestAbility)) {
//            showThreatWarning(boss, bestAbility, 3); // 3 saniye önceden uyar
//        }
//
//        // Yeteneği kullan (uyarıdan sonra)
//        final BossAbility finalAbility = bestAbility;
//        final long delay = shouldShowThreatWarning(bestAbility) ? 60L : 0L; // 3 saniye gecikme
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                executeAbility(boss, finalAbility);
//            }
//        }.runTaskLater(plugin, delay);
//
//        boss.setLastAbilityTime(System.currentTimeMillis());
//
//        // Dinamik cooldown ayarla (yetenek tipine göre)
//        long cooldown = getAbilityCooldown(bestAbility);
//        boss.setCurrentCooldown(cooldown);
//    }
//
//    /**
//     * Kombo zinciri al
//     */
//    private List<BossAbility> getComboChain(BossAbility starter, List<BossAbility> availableAbilities) {
//        List<BossAbility> combo = new ArrayList<>();
//
//        switch (starter) {
//            case FIRE_BREATH:
//                // Ateş kombo: FIRE_BREATH -> EXPLOSION -> TELEPORT
//                if (availableAbilities.contains(BossAbility.EXPLOSION)) {
//                    combo.add(BossAbility.EXPLOSION);
//                    if (availableAbilities.contains(BossAbility.TELEPORT)) {
//                        combo.add(BossAbility.TELEPORT);
//                    }
//                }
//                break;
//
//            case CHARGE:
//                // Koşu kombo: CHARGE -> SHOCKWAVE -> BLOCK_THROW
//                if (availableAbilities.contains(BossAbility.SHOCKWAVE)) {
//                    combo.add(BossAbility.SHOCKWAVE);
//                    if (availableAbilities.contains(BossAbility.BLOCK_THROW)) {
//                        combo.add(BossAbility.BLOCK_THROW);
//                    }
//                }
//                break;
//
//            case LIGHTNING_STRIKE:
//                // Yıldırım kombo: LIGHTNING_STRIKE -> FIRE_BREATH
//                if (availableAbilities.contains(BossAbility.FIRE_BREATH)) {
//                    combo.add(BossAbility.FIRE_BREATH);
//                }
//                break;
//
//            case EXPLOSION:
//            case SHOCKWAVE:
//            case TELEPORT:
//            case BLOCK_THROW:
//            case POISON_CLOUD:
//            case HEAL:
//            case SUMMON_MINIONS:
//                // Bu yetenekler kombo başlatıcı değil
//                break;
//        }
//
//        return combo;
//    }
//
//    /**
//     * Kombo uygula
//     */
//    private void executeCombo(BossData boss, List<BossAbility> comboChain) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) return;
//
//        // Kombo başladı uyarısı
//        for (Player player : entity.getWorld().getPlayers()) {
//            if (player.getLocation().distance(entity.getLocation()) <= 30) {
//                player.sendTitle("§c§l⚠ KOMBO SALDIRISI!", "", 10, 30, 10);
//                player.playSound(entity.getLocation(),
//                    org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
//            }
//        }
//
//        // Kombo yeteneklerini sırayla kullan
//        int delay = 0;
//        for (BossAbility ability : comboChain) {
//            final BossAbility finalAbility = ability;
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    executeAbility(boss, finalAbility);
//                }
//            }.runTaskLater(plugin, delay);
//            delay += 40; // 2 saniye arayla
//        }
//
//        // Cooldown ayarla (kombo için daha uzun)
//        boss.setLastAbilityTime(System.currentTimeMillis());
//        boss.setCurrentCooldown(abilityCooldownMax + 2000);
//    }
//
//    /**
//     * Tehdit uyarısı gösterilmeli mi?
//     */
//    private boolean shouldShowThreatWarning(BossAbility ability) {
//        switch (ability) {
//            case EXPLOSION:
//            case SHOCKWAVE:
//            case LIGHTNING_STRIKE:
//            case FIRE_BREATH:
//            case CHARGE:
//                return true; // Büyük saldırılar
//            default:
//                return false;
//        }
//    }
//
//    /**
//     * Tehdit uyarısı göster
//     */
//    private void showThreatWarning(BossData boss, BossAbility ability, int seconds) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) return;
//
//        Location bossLoc = entity.getLocation();
//        String abilityName = getAbilityDisplayName(ability);
//
//        // Geri sayım
//        new BukkitRunnable() {
//            int countdown = seconds;
//            @Override
//            public void run() {
//                if (countdown <= 0 || entity.isDead()) {
//                    cancel();
//                    return;
//                }
//
//                String message = "§c§l⚠ " + abilityName + " " + countdown + " SANİYE!";
//
//                for (Player player : bossLoc.getWorld().getPlayers()) {
//                    double distance = player.getLocation().distance(bossLoc);
//                    if (distance <= 30) {
//                        // Title uyarısı
//                        player.sendTitle("", message, 0, 20, 0);
//
//                        // Ses uyarısı
//                        player.playSound(player.getLocation(),
//                            org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (countdown * 0.1f));
//
//                        // Partikül uyarısı
//                        player.getWorld().spawnParticle(
//                            org.bukkit.Particle.REDSTONE,
//                            player.getLocation().add(0, 2, 0),
//                            10, 0.3, 0.3, 0.3, 0,
//                            new org.bukkit.Particle.DustOptions(
//                                org.bukkit.Color.RED, 2.0f
//                            )
//                        );
//                    }
//                }
//
//                countdown--;
//            }
//        }.runTaskTimer(plugin, 0L, 20L); // Her saniye
//    }
//
//    /**
//     * Yetenek görünen adı
//     */
//    private String getAbilityDisplayName(BossAbility ability) {
//        switch (ability) {
//            case FIRE_BREATH: return "ATEŞ PÜSKÜRTME";
//            case EXPLOSION: return "PATLAMA";
//            case LIGHTNING_STRIKE: return "YILDIRIM";
//            case SHOCKWAVE: return "ŞOK DALGASI";
//            case CHARGE: return "KOŞU SALDIRISI";
//            default: return "SALDIRI";
//        }
//    }
//
//    /**
//     * Yetenek tipine göre cooldown belirle
//     */
//    private long getAbilityCooldown(BossAbility ability) {
//        switch (ability) {
//            case HEAL:
//            case TELEPORT:
//                return abilityCooldownMax; // Uzun cooldown
//            case EXPLOSION:
//            case SHOCKWAVE:
//                return (abilityCooldownMin + abilityCooldownMax) / 2; // Orta cooldown
//            case SUMMON_MINIONS:
//                return abilityCooldownMax + 2000; // Çok uzun cooldown
//            default:
//                return abilityCooldownMin + (long)(Math.random() * (abilityCooldownMax - abilityCooldownMin));
//        }
//    }
//
//    /**
//     * Görüş hattı kontrolü
//     */
//    private boolean hasLineOfSight(LivingEntity from, Player to) {
//        Location fromLoc = from.getEyeLocation();
//        Location toLoc = to.getEyeLocation();
//
//        // Ray trace ile görüş hattı kontrolü
//        org.bukkit.util.RayTraceResult result = from.getWorld().rayTraceBlocks(
//            fromLoc,
//            toLoc.toVector().subtract(fromLoc.toVector()).normalize(),
//            fromLoc.distance(toLoc)
//        );
//
//        return result == null || result.getHitBlock() == null;
//    }
//
//    /**
//     * Yetenek uygula
//     */
//    private void executeAbility(BossData boss, BossAbility ability) {
//        LivingEntity entity = boss.getEntity();
//        if (entity == null || entity.isDead()) {
//            return;
//        }
//
//        Location loc = entity.getLocation();
//        Player target = findNearestPlayer(loc, 30);
//
//        switch (ability) {
//            case FIRE_BREATH:
//                fireBreath(entity, target != null ? target : null);
//                break;
//            case EXPLOSION:
//                createExplosion(loc);
//                break;
//            case LIGHTNING_STRIKE:
//                strikeLightning(target != null ? target.getLocation() : loc);
//                break;
//            case BLOCK_THROW:
//                throwBlocks(entity, target);
//                break;
//            case POISON_CLOUD:
//                createPoisonCloud(loc);
//                break;
//            case TELEPORT:
//                teleportBoss(entity, target);
//                break;
//            case CHARGE:
//                chargeAttack(entity, target);
//                break;
//            case SUMMON_MINIONS:
//                summonMinions(loc, boss.getType());
//                break;
//            case HEAL:
//                healBoss(entity);
//                break;
//            case SHOCKWAVE:
//                createShockwave(loc);
//                break;
//        }
//    }
//
//    /**
//     * Ateş püskürtme
//     */
//    private void fireBreath(LivingEntity boss, Player target) {
//        Location start = boss.getLocation().add(0, 1, 0);
//        Vector direction;
//
//        if (target != null) {
//            direction = target.getLocation().subtract(start).toVector().normalize();
//        } else {
//            direction = boss.getLocation().getDirection();
//        }
//
//        for (int i = 1; i <= 10; i++) {
//            final int step = i; // Final yap
//            Location fireLoc = start.clone().add(direction.clone().multiply(step * 0.5));
//            final Location finalLoc = fireLoc;
//
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    finalLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, finalLoc, 5, 0.2, 0.2, 0.2, 0.05);
//                    finalLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, finalLoc, 2, 0.1, 0.1, 0.1,
//                            0.01);
//
//                    // Oyunculara hasar ver
//                    for (Entity nearby : finalLoc.getWorld().getNearbyEntities(finalLoc, 1, 1, 1)) {
//                        if (nearby instanceof Player) {
//                            ((Player) nearby).setFireTicks(60);
//                            ((Player) nearby).damage(2.0, boss);
//                        }
//                    }
//
//                    // Çevresel tehlike: Ateş alanları oluştur (zemin)
//                    if (step % 3 == 0) { // Her 3. adımda bir
//                        Location groundLoc = finalLoc.clone();
//                        groundLoc.setY(groundLoc.getWorld().getHighestBlockYAt(groundLoc));
//                        Block groundBlock = groundLoc.getBlock();
//
//                        // Zemin yanıcı değilse ateş bloğu oluştur
//                        if (groundBlock.getType().isSolid() &&
//                            groundBlock.getType() != Material.BEDROCK &&
//                            groundBlock.getType() != Material.LAVA &&
//                            groundBlock.getType() != Material.WATER) {
//                            groundBlock.setType(Material.FIRE);
//                        }
//                    }
//                }
//            }.runTaskLater(plugin, step * 2L);
//        }
//
//        boss.getWorld().playSound(start, org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
//    }
//
//    /**
//     * Patlama yapma (kendine zarar vermeme kontrolü ile + çevresel tehlikeler)
//     */
//    private void createExplosion(Location loc) {
//        // Kendine zarar vermeme kontrolü
//        if (avoidSelfDamage) {
//            // Patlamayı hedefe doğru yap, boss'tan uzakta
//            Player target = findNearestPlayer(loc, mediumRange);
//            if (target != null) {
//                Location targetLoc = target.getLocation();
//                Vector direction = targetLoc.toVector().subtract(loc.toVector()).normalize();
//                Location explosionLoc = loc.clone().add(direction.multiply(3)); // Boss'tan 3 blok uzakta
//
//                // Güvenli konum kontrolü
//                if (explosionLoc.getBlock().getType().isAir() ||
//                    explosionLoc.getBlock().getType() == Material.WATER) {
//                    explosionLoc.getWorld().createExplosion(explosionLoc, 3.0f, false, false);
//                    explosionLoc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, explosionLoc, 1);
//
//                    // Çevresel tehlike: Lav oluştur
//                    createLavaHazard(explosionLoc);
//                    return;
//                }
//            }
//        }
//
//        // Normal patlama (kendine zarar verme kontrolü kapalıysa veya hedef yoksa)
//        loc.getWorld().createExplosion(loc, 3.0f, false, false);
//        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, loc, 1);
//
//        // Çevresel tehlike: Lav oluştur
//        createLavaHazard(loc);
//    }
//
//    /**
//     * Lav tehlikesi oluştur
//     */
//    private void createLavaHazard(Location center) {
//        if (center == null || center.getWorld() == null) return;
//
//        // Patlama sonrası lav havuzları oluştur
//        new BukkitRunnable() {
//            int ticks = 0;
//            private final Random random = new Random();
//            private final org.bukkit.World world = center.getWorld();
//
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 100 || world == null) { // 5 saniye
//                    cancel();
//                    return;
//                }
//
//                // Her saniye birkaç lav bloğu oluştur
//                if (ticks % 20 == 0) {
//                    for (int i = 0; i < 3; i++) {
//                        try {
//                            double angle = random.nextDouble() * 2 * Math.PI;
//                            double distance = 2 + random.nextDouble() * 3;
//                            Location lavaLoc = center.clone().add(
//                                Math.cos(angle) * distance,
//                                -1,
//                                Math.sin(angle) * distance
//                            );
//
//                            Block block = lavaLoc.getBlock();
//                            if (block.getType().isSolid() &&
//                                block.getType() != Material.BEDROCK &&
//                                block.getType() != Material.LAVA) {
//                                block.setType(Material.LAVA);
//
//                                // Optimizasyon: Sadece yakındaki oyunculara uyarı
//                                for (Player player : world.getPlayers()) {
//                                    if (player != null && player.isOnline() &&
//                                        player.getLocation().distance(lavaLoc) <= 5) {
//                                        player.sendMessage("§c§l⚠ LAV OLUŞTU!");
//                                        player.playSound(lavaLoc, org.bukkit.Sound.BLOCK_LAVA_POP, 1.0f, 1.0f);
//                                    }
//                                }
//                            }
//                        } catch (Exception e) {
//                            // Blok değiştirilemedi, atla
//                        }
//                    }
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
//    }
//
//    /**
//     * Yıldırım atma (güvenli versiyon - boss'a zarar vermez)
//     */
//    private void strikeLightning(Location target) {
//        // Yıldırımı biraz yukarıdan düşür (boss'a zarar vermesin)
//        Location safeTarget = target.clone().add(0, 2, 0);
//
//        // Fake lightning (hasar vermeyen)
//        target.getWorld().strikeLightningEffect(safeTarget);
//        target.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, safeTarget, 20, 0.5, 1, 0.5, 0.1);
//
//        // Sadece oyunculara hasar ver
//        for (Entity nearby : target.getWorld().getNearbyEntities(target, 2, 2, 2)) {
//            if (nearby instanceof Player) {
//                ((Player) nearby).damage(4.0); // Yıldırım hasarı
//            }
//        }
//    }
//
//    /**
//     * Blok fırlatma
//     */
//    private void throwBlocks(LivingEntity boss, Player target) {
//        if (target == null)
//            return;
//
//        Location bossLoc = boss.getLocation();
//        Location targetLoc = target.getLocation();
//
//        // 3x3 alanından blokları al ve fırlat (merkez hariç)
//        int blockCount = 0;
//        int maxBlocks = 3; // Maksimum 3 blok fırlat
//
//        for (int x = -1; x <= 1 && blockCount < maxBlocks; x++) {
//            for (int z = -1; z <= 1 && blockCount < maxBlocks; z++) {
//                // Merkez bloğu atla
//                if (x == 0 && z == 0)
//                    continue;
//
//                Block block = bossLoc.clone().add(x, -1, z).getBlock();
//                if (block.getType().isSolid() && block.getType() != Material.BEDROCK) {
//                    FallingBlock fallingBlock = bossLoc.getWorld().spawnFallingBlock(
//                            bossLoc.clone().add(0, 2, 0), block.getBlockData());
//
//                    Vector direction = targetLoc.subtract(bossLoc).toVector().normalize();
//                    fallingBlock.setVelocity(direction.multiply(0.8));
//                    fallingBlock.setHurtEntities(true);
//
//                    block.setType(Material.AIR);
//                    blockCount++;
//                }
//            }
//        }
//    }
//
//    /**
//     * Zehir bulutu (hedefe doğru)
//     */
//    private void createPoisonCloud(Location loc) {
//        Player target = findNearestPlayer(loc, mediumRange);
//        Location cloudCenter = target != null ? target.getLocation() : loc;
//
//        // Zehir bulutunu hedefin etrafında oluştur
//        for (int i = 0; i < 15; i++) {
//            Location cloudLoc = cloudCenter.clone().add(
//                    (Math.random() - 0.5) * 4,
//                    Math.random() * 1.5,
//                    (Math.random() - 0.5) * 4);
//
//            loc.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, cloudLoc, 8, 0.4, 0.4, 0.4, 0);
//
//            // Yakındaki oyunculara zehir ver
//            for (Entity nearby : loc.getWorld().getNearbyEntities(cloudLoc, 2, 2, 2)) {
//                if (nearby instanceof Player) {
//                    // Boss entity'si zaten Player değil, bu yüzden sadece Player kontrolü yeterli
//                    ((Player) nearby).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false, false));
//                }
//            }
//        }
//
//        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_WITCH_AMBIENT, 0.8f, 0.6f);
//    }
//
//    /**
//     * Işınlanma (güvenli konum bulma ile)
//     */
//    private void teleportBoss(LivingEntity boss, Player target) {
//        if (target == null)
//            return;
//
//        Location targetLoc = target.getLocation();
//        Location safeLoc = null;
//
//        // Güvenli konum bul (10 deneme)
//        for (int i = 0; i < 10; i++) {
//            Location testLoc = targetLoc.clone().add(
//                    (Math.random() - 0.5) * 6, // 3 blok yarıçap
//                    0,
//                    (Math.random() - 0.5) * 6);
//
//            // Yüksekliği ayarla
//            int highestY = testLoc.getWorld().getHighestBlockYAt(testLoc);
//            testLoc.setY(highestY + 1);
//
//            // Güvenli konum kontrolü (havada değil, suda değil, lavda değil)
//            Block ground = testLoc.clone().add(0, -1, 0).getBlock();
//            Block feet = testLoc.getBlock();
//            Block head = testLoc.clone().add(0, 1, 0).getBlock();
//
//            if (ground.getType().isSolid() &&
//                !ground.getType().equals(Material.LAVA) &&
//                feet.getType().isAir() &&
//                head.getType().isAir() &&
//                !feet.getType().equals(Material.WATER) &&
//                !head.getType().equals(Material.WATER)) {
//                safeLoc = testLoc;
//                break;
//            }
//        }
//
//        // Güvenli konum bulunamadıysa, hedefin arkasına ışınlan
//        if (safeLoc == null) {
//            Vector direction = targetLoc.toVector().subtract(boss.getLocation().toVector()).normalize();
//            safeLoc = targetLoc.clone().subtract(direction.multiply(4));
//            safeLoc.setY(targetLoc.getWorld().getHighestBlockYAt(safeLoc) + 1);
//        }
//
//        // Önce partikül efekti (gidiş)
//        boss.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, boss.getLocation(), 30, 0.5, 1, 0.5, 0.3);
//        boss.getWorld().playSound(boss.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
//
//        // Işınlan
//        boss.teleport(safeLoc);
//
//        // Sonra partikül efekti (varış)
//        boss.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, safeLoc, 50, 1, 1, 1, 0.5);
//        boss.getWorld().playSound(safeLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
//    }
//
//    /**
//     * Koşu saldırısı (daha kontrollü)
//     */
//    private void chargeAttack(LivingEntity boss, Player target) {
//        if (target == null)
//            return;
//
//        Location bossLoc = boss.getLocation();
//        Location targetLoc = target.getLocation();
//        double distance = bossLoc.distance(targetLoc);
//
//        // Çok yakınsa charge yapma
//        if (distance < 3) {
//            return;
//        }
//
//        Vector direction = targetLoc.toVector().subtract(bossLoc.toVector()).normalize();
//        direction.multiply(Math.min(1.2, distance / 5)); // Mesafeye göre hız ayarla
//        direction.setY(0.2); // Daha kontrollü yükseklik
//
//        boss.setVelocity(direction);
//        boss.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, boss.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
//        boss.getWorld().playSound(bossLoc, org.bukkit.Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.2f);
//
//        // Charge sırasında hasar verme kontrolü
//        new BukkitRunnable() {
//            int ticks = 0;
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 20 || boss.isDead()) { // 1 saniye sonra dur
//                    cancel();
//                    return;
//                }
//
//                // Hedefe çarptıysa hasar ver
//                if (boss.getLocation().distance(targetLoc) < 2) {
//                    target.damage(6.0, boss);
//                    cancel();
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
//    }
//
//    /**
//     * Minyon çağırma
//     */
//    private void summonMinions(Location loc, BossType bossType) {
//        // Boss tipine göre minyonlar
//        switch (bossType) {
//            case GOBLIN_KING:
//                mobManager.spawnGoblin(loc.clone().add(2, 0, 0));
//                mobManager.spawnGoblin(loc.clone().add(-2, 0, 0));
//                break;
//            case ORC_CHIEF:
//                mobManager.spawnOrk(loc.clone().add(2, 0, 0));
//                mobManager.spawnOrk(loc.clone().add(-2, 0, 0));
//                break;
//            case TROLL_KING:
//                mobManager.spawnTroll(loc.clone().add(2, 0, 0));
//                break;
//            case DRAGON:
//            case TREX:
//            case CYCLOPS:
//            case TITAN_GOLEM:
//            case HELL_DRAGON:
//            case HYDRA:
//            case PHOENIX:
//            case VOID_DRAGON:
//            case CHAOS_TITAN:
//            case CHAOS_GOD:
//                // Güçlü bosslar için özel minyonlar (opsiyonel)
//                break;
//        }
//
//        loc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, loc, 30, 1, 1, 1, 0.2);
//        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
//    }
//
//    /**
//     * Kendini iyileştirme
//     */
//    private void healBoss(LivingEntity boss) {
//        if (boss == null || boss.isDead()) return;
//
//        double maxHealth = boss.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
//        double healAmount = maxHealth * 0.2; // %20 iyileşme
//
//        // Animasyonlu iyileştirme
//        new BukkitRunnable() {
//            int ticks = 0;
//            @Override
//            public void run() {
//                ticks++;
//                if (ticks > 20 || boss.isDead()) { // 1 saniye animasyon
//                    cancel();
//                    return;
//                }
//
//                // Her tick'te küçük miktar iyileştir
//                double tickHeal = healAmount / 20.0;
//                boss.setHealth(Math.min(maxHealth, boss.getHealth() + tickHeal));
//
//                // Partikül efekti
//                boss.getWorld().spawnParticle(org.bukkit.Particle.HEART,
//                    boss.getLocation().add(0, 1 + (ticks * 0.1), 0),
//                    2, 0.3, 0.3, 0.3, 0.05);
//            }
//        }.runTaskTimer(plugin, 0L, 1L);
//
//        boss.getWorld().playSound(boss.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
//
//        // Yakındaki oyunculara bildir
//        BossType bossType = getBossType(boss);
//        if (bossType != null) {
//            String bossName = getBossDisplayName(bossType);
//            for (Player player : boss.getWorld().getPlayers()) {
//                if (player.getLocation().distance(boss.getLocation()) <= 30) {
//                    player.sendMessage("§c" + bossName + " §e§lkendini iyileştiriyor!");
//                }
//            }
//        }
//    }
//
//    /**
//     * Boss entity'sinden boss tipini bul
//     */
//    private BossType getBossType(LivingEntity entity) {
//        for (BossData bossData : activeBosses.values()) {
//            if (bossData.getEntity() != null && bossData.getEntity().equals(entity)) {
//                return bossData.getType();
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Şok dalgası (boss'a zarar vermez)
//     */
//    private void createShockwave(Location loc) {
//        LivingEntity boss = null;
//        // Boss'u bul
//        for (BossData bossData : activeBosses.values()) {
//            if (bossData.getEntity() != null &&
//                bossData.getEntity().getLocation().distance(loc) < 2) {
//                boss = bossData.getEntity();
//                break;
//            }
//        }
//
//        for (int radius = 1; radius <= 5; radius++) {
//            final int finalRadius = radius;
//            final LivingEntity finalBoss = boss;
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < 360; i += 10) {
//                        double angle = Math.toRadians(i);
//                        Location waveLoc = loc.clone().add(
//                                Math.cos(angle) * finalRadius,
//                                0,
//                                Math.sin(angle) * finalRadius);
//
//                        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, waveLoc, 1);
//
//                        // Oyunculara hasar ve itme (boss hariç)
//                        for (Entity nearby : loc.getWorld().getNearbyEntities(waveLoc, 1, 1, 1)) {
//                            if (nearby instanceof Player && !nearby.equals(finalBoss)) {
//                                Vector push = nearby.getLocation().subtract(loc).toVector().normalize();
//                                push.multiply(1.5);
//                                push.setY(0.5);
//                                nearby.setVelocity(push);
//                                ((Player) nearby).damage(3.0);
//                            }
//                        }
//                    }
//                }
//            }.runTaskLater(plugin, radius * 5L);
//        }
//
//        loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_RAVAGER_ROAR, 1.0f, 0.8f);
//
//        // Çevresel tehlike: Zemin çatlakları (şok dalgası sonrası)
//        createGroundCracks(loc);
//    }
//
//    /**
//     * Zemin çatlakları oluştur
//     */
//    private void createGroundCracks(Location center) {
//        new BukkitRunnable() {
//            int radius = 1;
//            @Override
//            public void run() {
//                if (radius > 5) {
//                    cancel();
//                    return;
//                }
//
//                // Çatlaklar oluştur
//                for (int i = 0; i < 360; i += 15) {
//                    double angle = Math.toRadians(i);
//                    Location crackLoc = center.clone().add(
//                        Math.cos(angle) * radius,
//                        0,
//                        Math.sin(angle) * radius
//                    );
//                    crackLoc.setY(center.getWorld().getHighestBlockYAt(crackLoc));
//
//                    Block block = crackLoc.getBlock();
//                    // Zemin bloklarını çatlat (hava yap)
//                    if (block.getType().isSolid() &&
//                        block.getType() != Material.BEDROCK) {
//                        block.setType(Material.AIR);
//
//                        // Altındaki blok da çatlat
//                        Block below = crackLoc.clone().add(0, -1, 0).getBlock();
//                        if (below.getType().isSolid() &&
//                            below.getType() != Material.BEDROCK) {
//                            below.setType(Material.AIR);
//                        }
//                    }
//                }
//
//                radius++;
//            }
//        }.runTaskTimer(plugin, 40L, 10L); // Şok dalgasından sonra çatlaklar
//    }
//
//    /**
//     * En yakın oyuncuyu bul
//     */
//    private Player findNearestPlayer(Location loc, double maxDistance) {
//        Player nearest = null;
//        double nearestDistance = maxDistance;
//
//        for (Player player : loc.getWorld().getPlayers()) {
//            double distance = player.getLocation().distance(loc);
//            if (distance < nearestDistance) {
//                nearest = player;
//                nearestDistance = distance;
//            }
//        }
//
//        return nearest;
//    }
//
//    /**
//     * Boss spawn et (ritüel ile)
//     */
//    public boolean spawnBossFromRitual(Location loc, BossType type, UUID ownerId) {
//        if (loc == null || loc.getWorld() == null) {
//            return false;
//        }
//
//        // Cooldown kontrolü
//        Location ritualLoc = loc.clone();
//        ritualLoc.setY(ritualLoc.getBlockY());
//        if (ritualCooldowns.containsKey(ritualLoc)) {
//            long timeLeft = (ritualCooldowns.get(ritualLoc) + RITUAL_COOLDOWN) - System.currentTimeMillis();
//            if (timeLeft > 0) {
//                return false;
//            }
//        }
//
//        LivingEntity bossEntity = spawnBossEntity(loc, type);
//        if (bossEntity == null) {
//            return false;
//        }
//
//        // BossData oluştur
//        BossData bossData = createBossData(type, bossEntity, ownerId);
//        activeBosses.put(bossEntity.getUniqueId(), bossData);
//
//        // BossBar oluştur
//        createBossBar(bossEntity, type);
//
//        // Cooldown kaydet
//        ritualCooldowns.put(ritualLoc, System.currentTimeMillis());
//
//        // Duyuru
//        String bossName = getBossDisplayName(type);
//        loc.getWorld().getPlayers().forEach(p -> {
//            if (p.getLocation().distance(loc) <= 50) {
//                p.sendTitle("§c§l" + bossName, "§eÇağrıldı!", 20, 60, 20);
//                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
//            }
//        });
//
//        // Doğal arena dönüşümü başlat (güçlü boss'lar için)
//        if (bossArenaManager != null && bossArenaManager.isPowerfulBoss(type)) {
//            bossArenaManager.startRitualAreaTransformation(loc, type);
//        }
//
//        // BossBar güncelleme task'ını başlat (eğer başlatılmamışsa)
//        if (bossBarUpdateTask == null) {
//            startBossBarUpdateTask();
//        }
//
//        saveBosses();
//        return true;
//    }
//
//    /**
//     * Boss entity spawn et
//     */
//    private LivingEntity spawnBossEntity(Location loc, BossType type) {
//        switch (type) {
//            case GOBLIN_KING:
//                Zombie goblin = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
//                goblin.setCustomName("§2§lGOBLIN KRALI");
//                goblin.setBaby(false);
//                if (goblin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    goblin.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(150.0);
//                }
//                goblin.setHealth(150.0);
//                return goblin;
//
//            case ORC_CHIEF:
//                Zombie orc = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
//                orc.setCustomName("§c§lORK ŞEFİ");
//                if (orc.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    orc.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
//                }
//                orc.setHealth(200.0);
//                return orc;
//
//            case TROLL_KING:
//                Zombie troll = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
//                troll.setCustomName("§5§lTROLL KRALI");
//                if (troll.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    troll.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(300.0);
//                }
//                troll.setHealth(300.0);
//                return troll;
//
//            case DRAGON:
//                Phantom dragon = (Phantom) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
//                dragon.setCustomName("§4§lEJDERHA");
//                dragon.setSize(25);
//                if (dragon.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    dragon.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
//                }
//                dragon.setHealth(500.0);
//                return dragon;
//
//            case TREX:
//                Ravager trex = (Ravager) loc.getWorld().spawnEntity(loc, EntityType.RAVAGER);
//                trex.setCustomName("§c§lT-REX");
//                if (trex.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    trex.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(600.0);
//                }
//                trex.setHealth(600.0);
//                return trex;
//
//            case CYCLOPS:
//                Giant cyclops = (Giant) loc.getWorld().spawnEntity(loc, EntityType.GIANT);
//                cyclops.setCustomName("§6§lTEK GÖZLÜ DEV");
//                if (cyclops.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    cyclops.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(700.0);
//                }
//                cyclops.setHealth(700.0);
//                return cyclops;
//
//            case TITAN_GOLEM:
//                return mobManager.spawnTitanGolem(loc, null);
//
//            case HELL_DRAGON:
//                return mobManager.spawnHellDragon(loc, null);
//
//            case HYDRA:
//                return mobManager.spawnHydra(loc);
//
//            case CHAOS_GOD:
//                // En güçlü boss - özel entity
//                Wither wither = (Wither) loc.getWorld().spawnEntity(loc, EntityType.WITHER);
//                wither.setCustomName("§5§lKHAOS TANRISI");
//                if (wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
//                    wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(1000.0);
//                }
//                wither.setHealth(1000.0);
//                return wither;
//
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * BossData oluştur
//     */
//    private BossData createBossData(BossType type, LivingEntity entity, UUID ownerId) {
//        int maxPhase = getBossMaxPhase(type);
//        List<BossWeakness> weaknesses = getBossWeaknesses(type);
//        Map<Integer, List<BossAbility>> phaseAbilities = getBossAbilities(type);
//
//        return new BossData(type, entity, ownerId, maxPhase, weaknesses, phaseAbilities);
//    }
//
//    /**
//     * Boss faz sayısı
//     */
//    private int getBossMaxPhase(BossType type) {
//        switch (type) {
//            case GOBLIN_KING:
//            case ORC_CHIEF:
//            case TROLL_KING:
//            case TREX:
//                return 1; // Tek faz
//            case DRAGON:
//            case CYCLOPS:
//            case HELL_DRAGON:
//                return 2; // 2 faz
//            case TITAN_GOLEM:
//            case HYDRA:
//            case CHAOS_GOD:
//                return 3; // 3 faz
//            default:
//                return 1;
//        }
//    }
//
//    /**
//     * Boss zayıf noktaları
//     */
//    private List<BossWeakness> getBossWeaknesses(BossType type) {
//        List<BossWeakness> weaknesses = new ArrayList<>();
//
//        switch (type) {
//            case TITAN_GOLEM:
//                weaknesses.add(BossWeakness.FIRE);
//                break;
//            case HELL_DRAGON:
//                weaknesses.add(BossWeakness.WATER);
//                break;
//            case HYDRA:
//                weaknesses.add(BossWeakness.POISON);
//                break;
//            case CHAOS_GOD:
//                weaknesses.add(BossWeakness.FIRE);
//                weaknesses.add(BossWeakness.POISON);
//                break;
//            case GOBLIN_KING:
//            case ORC_CHIEF:
//            case TROLL_KING:
//            case DRAGON:
//            case TREX:
//            case CYCLOPS:
//            case PHOENIX:
//            case VOID_DRAGON:
//            case CHAOS_TITAN:
//                // Bu bossların zayıflığı yok
//                break;
//        }
//
//        return weaknesses;
//    }
//
//    /**
//     * Boss yetenekleri (faza göre)
//     */
//    private Map<Integer, List<BossAbility>> getBossAbilities(BossType type) {
//        Map<Integer, List<BossAbility>> abilities = new HashMap<>();
//
//        switch (type) {
//            case GOBLIN_KING:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.CHARGE,
//                        BossAbility.SUMMON_MINIONS,
//                        BossAbility.EXPLOSION));
//                break;
//
//            case ORC_CHIEF:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.CHARGE,
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SUMMON_MINIONS));
//                break;
//
//            case TROLL_KING:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.HEAL));
//                break;
//
//            case DRAGON:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.TELEPORT,
//                        BossAbility.EXPLOSION));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.TELEPORT,
//                        BossAbility.SUMMON_MINIONS));
//                break;
//
//            case TREX:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.CHARGE,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.EXPLOSION));
//                break;
//
//            case CYCLOPS:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.CHARGE));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.EXPLOSION,
//                        BossAbility.HEAL));
//                break;
//
//            case TITAN_GOLEM:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.EXPLOSION));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.HEAL));
//                abilities.put(3, Arrays.asList(
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.EXPLOSION,
//                        BossAbility.SUMMON_MINIONS));
//                break;
//
//            case HELL_DRAGON:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.TELEPORT,
//                        BossAbility.EXPLOSION));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.POISON_CLOUD,
//                        BossAbility.TELEPORT));
//                break;
//
//            case HYDRA:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.POISON_CLOUD,
//                        BossAbility.TELEPORT,
//                        BossAbility.SUMMON_MINIONS));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.POISON_CLOUD,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.HEAL,
//                        BossAbility.SUMMON_MINIONS));
//                abilities.put(3, Arrays.asList(
//                        BossAbility.POISON_CLOUD,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.EXPLOSION,
//                        BossAbility.HEAL,
//                        BossAbility.SUMMON_MINIONS));
//                break;
//
//            case CHAOS_GOD:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.TELEPORT));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.POISON_CLOUD,
//                        BossAbility.EXPLOSION,
//                        BossAbility.HEAL));
//                abilities.put(3, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.POISON_CLOUD,
//                        BossAbility.EXPLOSION,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.HEAL,
//                        BossAbility.SUMMON_MINIONS));
//                break;
//
//            case PHOENIX:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.TELEPORT,
//                        BossAbility.EXPLOSION));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.TELEPORT,
//                        BossAbility.HEAL));
//                break;
//
//            case VOID_DRAGON:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.TELEPORT,
//                        BossAbility.EXPLOSION));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.TELEPORT,
//                        BossAbility.SUMMON_MINIONS));
//                abilities.put(3, Arrays.asList(
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.FIRE_BREATH,
//                        BossAbility.EXPLOSION,
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.HEAL));
//                break;
//
//            case CHAOS_TITAN:
//                abilities.put(1, Arrays.asList(
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.EXPLOSION));
//                abilities.put(2, Arrays.asList(
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.HEAL));
//                abilities.put(3, Arrays.asList(
//                        BossAbility.SHOCKWAVE,
//                        BossAbility.BLOCK_THROW,
//                        BossAbility.LIGHTNING_STRIKE,
//                        BossAbility.EXPLOSION,
//                        BossAbility.SUMMON_MINIONS));
//                break;
//        }
//
//        return abilities;
//    }
//
//    /**
//     * Boss display name
//     */
//    /**
//     * Boss için BossBar oluştur
//     */
//    private void createBossBar(LivingEntity bossEntity, BossType type) {
//        String bossName = getBossDisplayName(type);
//        org.bukkit.boss.BossBar bossBar = org.bukkit.Bukkit.createBossBar(
//            "§c§l" + bossName,
//            org.bukkit.boss.BarColor.RED,
//            org.bukkit.boss.BarStyle.SOLID
//        );
//
//        // BossBar'ı görünür yap
//        bossBar.setVisible(true);
//
//        // Başlangıçta hiçbir oyuncuya ekleme - mesafe kontrolü yapılacak
//        bossBars.put(bossEntity.getUniqueId(), bossBar);
//    }
//
//    /**
//     * BossBar güncelleme task'ını başlat
//     */
//    private void startBossBarUpdateTask() {
//        if (bossBarUpdateTask != null) {
//            return; // Zaten çalışıyor
//        }
//
//        bossBarUpdateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
//            // Ölü bossları temizle
//            java.util.Iterator<java.util.Map.Entry<UUID, BossData>> iterator = activeBosses.entrySet().iterator();
//            while (iterator.hasNext()) {
//                java.util.Map.Entry<UUID, BossData> entry = iterator.next();
//                BossData bossData = entry.getValue();
//
//                // Entity null, ölü veya geçersizse temizle
//                if (bossData.getEntity() == null || !bossData.getEntity().isValid() || bossData.getEntity().isDead()) {
//                    // Boss öldü, BossBar'ı KESINLIKLE temizle
//                    UUID bossId = entry.getKey();
//                    org.bukkit.boss.BossBar bossBar = bossBars.remove(bossId);
//                    if (bossBar != null) {
//                        bossBar.removeAll();
//                        bossBar.setVisible(false);
//                    }
//                    // Ekstra güvenlik: bossBars map'inde hala varsa temizle
//                    bossBar = bossBars.get(bossId);
//                    if (bossBar != null) {
//                        bossBar.removeAll();
//                        bossBar.setVisible(false);
//                        bossBars.remove(bossId);
//                    }
//                    iterator.remove();
//                    continue;
//                }
//
//                // BossBar güncelle
//                org.bukkit.boss.BossBar bossBar = bossBars.get(entry.getKey());
//                if (bossBar != null) {
//                    LivingEntity entity = bossData.getEntity();
//                    // Entity null, ölü veya geçersizse temizle ve atla
//                    if (entity == null || !entity.isValid() || entity.isDead()) {
//                        // Hemen temizle
//                        UUID bossId = entry.getKey();
//                        bossBar.removeAll();
//                        bossBar.setVisible(false);
//                        bossBars.remove(bossId);
//                        iterator.remove();
//                        continue;
//                    }
//
//                    // Null check için attribute kontrolü
//                    org.bukkit.attribute.AttributeInstance maxHealthAttr = entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
//                    if (maxHealthAttr == null) {
//                        continue; // Attribute yok, atla
//                    }
//
//                    double health = entity.getHealth();
//                    double maxHealth = maxHealthAttr.getValue();
//                    if (maxHealth <= 0) {
//                        continue; // Geçersiz max health
//                    }
//
//                    double healthPercent = Math.max(0.0, Math.min(1.0, health / maxHealth));
//
//                    String bossName = getBossDisplayName(bossData.getType());
//                    String phaseText = bossData.getMaxPhase() > 1 ? " §7(Faz " + bossData.getPhase() + "/" + bossData.getMaxPhase() + ")" : "";
//                    String bossBarTitle = "§c§l" + bossName + phaseText + " §7| §c" +
//                        String.format("%.0f/%.0f", health, maxHealth);
//
//                    bossBar.setTitle(bossBarTitle);
//                    bossBar.setProgress(healthPercent);
//
//                    // Can durumuna göre renk değiştir
//                    if (healthPercent > 0.6) {
//                        bossBar.setColor(org.bukkit.boss.BarColor.RED);
//                    } else if (healthPercent > 0.3) {
//                        bossBar.setColor(org.bukkit.boss.BarColor.YELLOW);
//                    } else {
//                        bossBar.setColor(org.bukkit.boss.BarColor.GREEN);
//                    }
//                }
//            }
//
//            // Her oyuncu için yakındaki boss'ları bul ve sadece onların BossBar'ını göster
//            for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
//                List<BossLocationInfo> nearbyBosses = getNearbyBosses(player, maxBossBarDistance);
//                Set<UUID> nearbyBossIds = new HashSet<>();
//                for (BossLocationInfo info : nearbyBosses) {
//                    nearbyBossIds.add(info.bossId);
//                }
//
//                // Tüm BossBar'lardan oyuncuyu çıkar, sonra sadece yakındakileri ekle
//                for (java.util.Map.Entry<UUID, org.bukkit.boss.BossBar> barEntry : bossBars.entrySet()) {
//                    org.bukkit.boss.BossBar bossBar = barEntry.getValue();
//                    if (nearbyBossIds.contains(barEntry.getKey())) {
//                        // Yakındaki boss - ekle
//                        if (!bossBar.getPlayers().contains(player)) {
//                            bossBar.addPlayer(player);
//                        }
//                    } else {
//                        // Uzaktaki boss - çıkar
//                        if (bossBar.getPlayers().contains(player)) {
//                            bossBar.removePlayer(player);
//                        }
//                    }
//                }
//            }
//
//            // Eğer aktif boss yoksa task'ı durdur
//            if (activeBosses.isEmpty()) {
//                if (bossBarUpdateTask != null) {
//                    bossBarUpdateTask.cancel();
//                    bossBarUpdateTask = null;
//                }
//                if (locationUpdateTask != null) {
//                    locationUpdateTask.cancel();
//                    locationUpdateTask = null;
//                }
//            }
//        }, 0L, 20L); // Her saniye
//
//        // Konum gösterimi task'ını başlat
//        if (showLocation && locationUpdateTask == null) {
//            startLocationUpdateTask();
//        }
//    }
//
//
//    /**
//     * Konum gösterimi task'ını başlat (ActionBar)
//     */
//    private void startLocationUpdateTask() {
//        if (locationUpdateTask != null) return;
//
//        int updateInterval = plugin.getConfig().getInt("boss.bossbar.location-update-interval", 20);
//
//        locationUpdateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
//            if (activeBosses.isEmpty()) {
//                if (locationUpdateTask != null) {
//                    locationUpdateTask.cancel();
//                    locationUpdateTask = null;
//                }
//                return;
//            }
//
//            // Her oyuncu için yakındaki boss'ları bul ve göster
//            for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
//                List<BossLocationInfo> nearbyBosses = getNearbyBosses(player, maxBossBarDistance);
//
//                if (!nearbyBosses.isEmpty()) {
//                    // En yakın boss'u ActionBar'da göster
//                    BossLocationInfo nearest = nearbyBosses.get(0);
//                    String direction = getDirection(player.getLocation(), nearest.location);
//                    int distance = (int) nearest.distance;
//                    String bossName = getBossDisplayName(nearest.type);
//
//                    String actionBarText = "§c§l" + bossName + " §7| §e" + direction + " §7(" + distance + "m)";
//                    if (nearbyBosses.size() > 1) {
//                        actionBarText += " §7+ " + (nearbyBosses.size() - 1) + " daha";
//                    }
//
//                    // ActionBar göster (Paper API - Adventure)
//                    player.sendActionBar(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(actionBarText));
//                }
//            }
//        }, 0L, updateInterval);
//    }
//
//    /**
//     * Yakındaki boss'ları bul (mesafeye göre sıralı)
//     */
//    private List<BossLocationInfo> getNearbyBosses(org.bukkit.entity.Player player, double maxDistance) {
//        List<BossLocationInfo> nearbyBosses = new ArrayList<>();
//        Location playerLoc = player.getLocation();
//
//        for (java.util.Map.Entry<UUID, BossData> entry : activeBosses.entrySet()) {
//            LivingEntity bossEntity = entry.getValue().getEntity();
//            if (bossEntity == null || !bossEntity.isValid() || bossEntity.isDead()) continue;
//
//            Location bossLoc = bossEntity.getLocation();
//            double distance = playerLoc.distance(bossLoc);
//
//            if (distance <= maxDistance) {
//                nearbyBosses.add(new BossLocationInfo(
//                    entry.getKey(),
//                    entry.getValue().getType(),
//                    bossLoc,
//                    distance
//                ));
//            }
//        }
//
//        // Mesafeye göre sırala (en yakın önce)
//        nearbyBosses.sort(Comparator.comparingDouble(b -> b.distance));
//
//        // Maksimum sayıya göre sınırla
//        if (nearbyBosses.size() > maxBossBars) {
//            nearbyBosses = nearbyBosses.subList(0, maxBossBars);
//        }
//
//        return nearbyBosses;
//    }
//
//    /**
//     * Yön bilgisi hesapla (Kuzey, Güney, Doğu, Batı vb.)
//     */
//    private String getDirection(Location from, Location to) {
//        double dx = to.getX() - from.getX();
//        double dz = to.getZ() - from.getZ();
//
//        double angle = Math.toDegrees(Math.atan2(dx, dz));
//        if (angle < 0) angle += 360;
//
//        if (angle >= 337.5 || angle < 22.5) return "↑ Kuzey";
//        if (angle >= 22.5 && angle < 67.5) return "↗ Kuzey-Doğu";
//        if (angle >= 67.5 && angle < 112.5) return "→ Doğu";
//        if (angle >= 112.5 && angle < 157.5) return "↘ Güney-Doğu";
//        if (angle >= 157.5 && angle < 202.5) return "↓ Güney";
//        if (angle >= 202.5 && angle < 247.5) return "↙ Güney-Batı";
//        if (angle >= 247.5 && angle < 292.5) return "← Batı";
//        if (angle >= 292.5 && angle < 337.5) return "↖ Kuzey-Batı";
//
//        return "?";
//    }
//
//    /**
//     * Boss konum bilgisi sınıfı
//     */
//    private static class BossLocationInfo {
//        final UUID bossId;
//        final BossType type;
//        final Location location;
//        final double distance;
//
//        BossLocationInfo(UUID bossId, BossType type, Location location, double distance) {
//            this.bossId = bossId;
//            this.type = type;
//            this.location = location;
//            this.distance = distance;
//        }
//    }
//
//    /**
//     * Oyuncu giriş yaptığında yakındaki BossBar'lara ekle
//     */
//    public void onPlayerJoin(org.bukkit.entity.Player player) {
//        // Mesafe kontrolü yapılacak, burada ekleme yapmıyoruz
//        // startBossBarUpdateTask içinde otomatik eklenir
//    }
//
//    public String getBossDisplayName(BossType type) {
//        switch (type) {
//            case GOBLIN_KING:
//                return "Goblin Kralı";
//            case ORC_CHIEF:
//                return "Ork Şefi";
//            case TROLL_KING:
//                return "Troll Kralı";
//            case DRAGON:
//                return "Ejderha";
//            case TREX:
//                return "T-Rex";
//            case CYCLOPS:
//                return "Tek Gözlü Dev";
//            case TITAN_GOLEM:
//                return "Titan Golem";
//            case HELL_DRAGON:
//                return "Cehennem Ejderi";
//            case HYDRA:
//                return "Hydra";
//            case PHOENIX:
//                return "Phoenix";
//            case VOID_DRAGON:
//                return "Hiçlik Ejderi";
//            case CHAOS_TITAN:
//                return "Kaos Titani";
//            case CHAOS_GOD:
//                return "Khaos Tanrısı";
//            default:
//                return "Bilinmeyen Boss";
//        }
//    }
//
//    /**
//     * Ritüel deseni kontrol et
//     */
//    public boolean checkRitualPattern(Block centerBlock, BossType type) {
//        // Merkez bloğun Çağırma Çekirdeği olup olmadığını kontrol et
//        if (!centerBlock.hasMetadata("SummonCore")) {
//            return false;
//        }
//
//        Material[][] pattern = getRitualPattern(type);
//        if (pattern == null) {
//            return false;
//        }
//
//        int size = pattern.length;
//        int offset = size / 2;
//
//        for (int x = 0; x < size; x++) {
//            for (int z = 0; z < size; z++) {
//                Block checkBlock = centerBlock.getRelative(x - offset, -1, z - offset);
//                Material required = pattern[x][z];
//
//                if (required != null && checkBlock.getType() != required) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }
//
//    /**
//     * Ritüel deseni al (public - listener için)
//     */
//    public Material[][] getRitualPattern(BossType type) {
//        switch (type) {
//            case GOBLIN_KING:
//                // 3x3 Cobblestone + Merkez Gold Block
//                return new Material[][] {
//                        { Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE },
//                        { Material.COBBLESTONE, Material.GOLD_BLOCK, Material.COBBLESTONE },
//                        { Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE }
//                };
//
//            case ORC_CHIEF:
//                // 3x3 Stone + Merkez Iron Block
//                return new Material[][] {
//                        { Material.STONE, Material.STONE, Material.STONE },
//                        { Material.STONE, Material.IRON_BLOCK, Material.STONE },
//                        { Material.STONE, Material.STONE, Material.STONE }
//                };
//
//            case TROLL_KING:
//                // 3x3 Stone Bricks + Merkez Diamond Block
//                return new Material[][] {
//                        { Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS },
//                        { Material.STONE_BRICKS, Material.DIAMOND_BLOCK, Material.STONE_BRICKS },
//                        { Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS }
//                };
//
//            case DRAGON:
//                // 5x5 Obsidian + Merkez Emerald Block
//                return new Material[][] {
//                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
//                                Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, Material.EMERALD_BLOCK, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
//                                Material.OBSIDIAN }
//                };
//
//            case TREX:
//                // 5x5 Stone + Merkez Gold Block + Köşeler Diamond
//                return new Material[][] {
//                        { Material.DIAMOND_BLOCK, Material.STONE, Material.STONE, Material.STONE,
//                                Material.DIAMOND_BLOCK },
//                        { Material.STONE, null, null, null, Material.STONE },
//                        { Material.STONE, null, Material.GOLD_BLOCK, null, Material.STONE },
//                        { Material.STONE, null, null, null, Material.STONE },
//                        { Material.DIAMOND_BLOCK, Material.STONE, Material.STONE, Material.STONE,
//                                Material.DIAMOND_BLOCK }
//                };
//
//            case CYCLOPS:
//                // 5x5 Stone Bricks + Merkez Emerald Block + Köşeler Gold
//                return new Material[][] {
//                        { Material.GOLD_BLOCK, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS,
//                                Material.GOLD_BLOCK },
//                        { Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS },
//                        { Material.STONE_BRICKS, null, Material.EMERALD_BLOCK, null, Material.STONE_BRICKS },
//                        { Material.STONE_BRICKS, null, null, null, Material.STONE_BRICKS },
//                        { Material.GOLD_BLOCK, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS,
//                                Material.GOLD_BLOCK }
//                };
//
//            case TITAN_GOLEM:
//                // 7x7 Obsidian + Merkez Netherite Block + Köşeler Diamond
//                return new Material[][] {
//                        { Material.DIAMOND_BLOCK, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
//                                Material.OBSIDIAN, Material.OBSIDIAN, Material.DIAMOND_BLOCK },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, Material.NETHERITE_BLOCK, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.DIAMOND_BLOCK, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
//                                Material.OBSIDIAN, Material.OBSIDIAN, Material.DIAMOND_BLOCK }
//                };
//
//            case HELL_DRAGON:
//                // 7x7 Netherrack + Merkez Nether Star (Beacon) + Köşeler Obsidian
//                return new Material[][] {
//                        { Material.OBSIDIAN, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
//                                Material.NETHERRACK, Material.NETHERRACK, Material.OBSIDIAN },
//                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, null, null, Material.BEACON, null, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, null, null, null, null, null, Material.NETHERRACK },
//                        { Material.OBSIDIAN, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
//                                Material.NETHERRACK, Material.NETHERRACK, Material.OBSIDIAN }
//                };
//
//            case HYDRA:
//                // 7x7 Prismarine + Merkez Heart of the Sea + Köşeler Emerald
//                return new Material[][] {
//                        { Material.EMERALD_BLOCK, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE,
//                                Material.PRISMARINE, Material.PRISMARINE, Material.EMERALD_BLOCK },
//                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
//                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
//                        { Material.PRISMARINE, null, null, Material.CONDUIT, null, null, Material.PRISMARINE },
//                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
//                        { Material.PRISMARINE, null, null, null, null, null, Material.PRISMARINE },
//                        { Material.EMERALD_BLOCK, Material.PRISMARINE, Material.PRISMARINE, Material.PRISMARINE,
//                                Material.PRISMARINE, Material.PRISMARINE, Material.EMERALD_BLOCK }
//                };
//
//            case CHAOS_GOD:
//                // 9x9 Bedrock + Merkez End Stone Bricks + Köşeler Netherite + Kenarlar Obsidian
//                return new Material[][] {
//                        { Material.NETHERITE_BLOCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
//                                Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
//                                Material.NETHERITE_BLOCK },
//                        { Material.BEDROCK, Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN,
//                                Material.BEDROCK },
//                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
//                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
//                        { Material.BEDROCK, null, null, null, Material.END_STONE_BRICKS, null, null, null,
//                                Material.BEDROCK },
//                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
//                        { Material.BEDROCK, null, null, null, null, null, null, null, Material.BEDROCK },
//                        { Material.BEDROCK, Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN,
//                                Material.BEDROCK },
//                        { Material.NETHERITE_BLOCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
//                                Material.BEDROCK, Material.BEDROCK, Material.BEDROCK, Material.BEDROCK,
//                                Material.NETHERITE_BLOCK }
//                };
//
//            case PHOENIX:
//                // 5x5 Netherrack + Merkez Beacon + Köşeler Blaze Rod pattern
//                return new Material[][] {
//                        { Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
//                                Material.NETHERRACK },
//                        { Material.NETHERRACK, null, null, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, null, Material.BEACON, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, null, null, null, Material.NETHERRACK },
//                        { Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK, Material.NETHERRACK,
//                                Material.NETHERRACK }
//                };
//
//            case VOID_DRAGON:
//                // 7x7 Obsidian + Merkez End Portal Frame + Köşeler Ender Eye
//                return new Material[][] {
//                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
//                                Material.OBSIDIAN, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, Material.END_PORTAL_FRAME, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, null, null, null, null, null, Material.OBSIDIAN },
//                        { Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN, Material.OBSIDIAN,
//                                Material.OBSIDIAN, Material.OBSIDIAN }
//                };
//
//            case CHAOS_TITAN:
//                // 7x7 Netherite + Merkez Beacon + Diamond Blocks
//                return new Material[][] {
//                        { Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
//                                Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
//                                Material.NETHERITE_BLOCK },
//                        { Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, null, null, null, Material.DIAMOND_BLOCK,
//                                Material.NETHERITE_BLOCK },
//                        { Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK },
//                        { Material.NETHERITE_BLOCK, null, null, Material.BEACON, null, null, Material.NETHERITE_BLOCK },
//                        { Material.NETHERITE_BLOCK, null, null, null, null, null, Material.NETHERITE_BLOCK },
//                        { Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, null, null, null, Material.DIAMOND_BLOCK,
//                                Material.NETHERITE_BLOCK },
//                        { Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
//                                Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK, Material.NETHERITE_BLOCK,
//                                Material.NETHERITE_BLOCK }
//                };
//
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * Ritüel aktifleştirme itemi
//     */
//    public Material getRitualActivationItem(BossType type) {
//        switch (type) {
//            case GOBLIN_KING:
//                return Material.ROTTEN_FLESH;
//            case ORC_CHIEF:
//                return Material.IRON_SWORD;
//            case TROLL_KING:
//                return Material.STONE_AXE;
//            case DRAGON:
//                return Material.DRAGON_EGG;
//            case TREX:
//                return Material.BONE;
//            case CYCLOPS:
//                return Material.ENDER_EYE;
//            case TITAN_GOLEM:
//                return Material.NETHER_STAR;
//            case HELL_DRAGON:
//                return Material.BLAZE_ROD;
//            case HYDRA:
//                return Material.HEART_OF_THE_SEA;
//            case PHOENIX:
//                return Material.BLAZE_POWDER;
//            case VOID_DRAGON:
//                return Material.DRAGON_EGG;
//            case CHAOS_TITAN:
//                return Material.NETHER_STAR;
//            case CHAOS_GOD:
//                return Material.NETHER_STAR;
//            default:
//                return null;
//        }
//    }
//
//    /**
//     * Boss zayıflığı kontrolü (hasar çarpanı)
//     */
//    public double getWeaknessMultiplier(BossData boss, org.bukkit.event.entity.EntityDamageEvent.DamageCause cause) {
//        double multiplier = 1.0;
//
//        for (BossWeakness weakness : boss.getWeaknesses()) {
//            switch (weakness) {
//                case FIRE:
//                    if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE ||
//                            cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK ||
//                            cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA) {
//                        multiplier = 2.0; // 2x hasar
//                    }
//                    break;
//                case WATER:
//                    if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.DROWNING) {
//                        multiplier = 2.0;
//                    }
//                    break;
//                case POISON:
//                    // Potion effect kontrolü ayrı yapılacak
//                    break;
//                case LIGHTNING:
//                    if (cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.LIGHTNING) {
//                        multiplier = 2.0;
//                    }
//                    break;
//            }
//        }
//
//        return multiplier;
//    }
//
//    /**
//     * Doğada boss spawn (zorluk seviyesine göre)
//     */
//    public void trySpawnBossInNature(Location loc, int difficultyLevel) {
//        if (loc == null || loc.getWorld() == null) {
//            return;
//        }
//
//        // Spawn şansı
//        double spawnChance = getBossSpawnChance(difficultyLevel);
//        if (new Random().nextDouble() > spawnChance) {
//            return;
//        }
//
//        // Seviyeye göre boss seç
//        BossType bossType = getRandomBossForLevel(difficultyLevel);
//        if (bossType == null) {
//            return;
//        }
//
//        // Spawn et
//        LivingEntity bossEntity = spawnBossEntity(loc, bossType);
//        if (bossEntity != null) {
//            BossData bossData = createBossData(bossType, bossEntity, null);
//            activeBosses.put(bossEntity.getUniqueId(), bossData);
//
//            // BossBar oluştur
//            createBossBar(bossEntity, bossType);
//
//            // Doğal biyom oluştur (güçlü boss'lar için)
//            if (bossArenaManager != null && bossArenaManager.isPowerfulBoss(bossType)) {
//                bossArenaManager.createNaturalBossBiome(loc, bossType);
//            }
//
//            // BossBar güncelleme task'ını başlat (eğer başlatılmamışsa)
//            if (bossBarUpdateTask == null) {
//                startBossBarUpdateTask();
//            }
//
//            plugin.getLogger().info(
//                    "Doğada boss spawn edildi: " + getBossDisplayName(bossType) + " (Seviye " + difficultyLevel + ")");
//        }
//    }
//
//    /**
//     * Boss spawn şansı
//     */
//    private double getBossSpawnChance(int difficultyLevel) {
//        switch (difficultyLevel) {
//            case 1:
//                return 0.01; // %1
//            case 2:
//                return 0.015; // %1.5
//            case 3:
//                return 0.02; // %2
//            case 4:
//                return 0.025; // %2.5
//            case 5:
//                return 0.03; // %3
//            default:
//                return 0.0;
//        }
//    }
//
//    /**
//     * Seviyeye göre rastgele boss
//     */
//    private BossType getRandomBossForLevel(int difficultyLevel) {
//        List<BossType> availableBosses = new ArrayList<>();
//
//        switch (difficultyLevel) {
//            case 1:
//                availableBosses.add(BossType.GOBLIN_KING);
//                availableBosses.add(BossType.ORC_CHIEF);
//                break;
//            case 2:
//                availableBosses.add(BossType.ORC_CHIEF);
//                availableBosses.add(BossType.TROLL_KING);
//                break;
//            case 3:
//                availableBosses.add(BossType.DRAGON);
//                availableBosses.add(BossType.TREX);
//                availableBosses.add(BossType.CYCLOPS);
//                break;
//            case 4:
//                availableBosses.add(BossType.CYCLOPS);
//                availableBosses.add(BossType.TITAN_GOLEM);
//                availableBosses.add(BossType.HELL_DRAGON);
//                availableBosses.add(BossType.HYDRA);
//                break;
//            case 5:
//                availableBosses.add(BossType.HYDRA);
//                availableBosses.add(BossType.CHAOS_GOD);
//                break;
//        }
//
//        if (availableBosses.isEmpty()) {
//            return null;
//        }
//
//        return availableBosses.get(new Random().nextInt(availableBosses.size()));
//    }
//
//    /**
//     * Boss kaydet
//     */
//    private void saveBosses() {
//        // Aktif bosslar runtime'da tutulur, sadece ritüel cooldown'ları kaydedilir
//        // Boss öldüğünde otomatik temizlenir
//    }
//
//    /**
//     * Boss yükle
//     */
//    private void loadBosses() {
//        // Boss'lar runtime'da tutulur, restart sonrası spawn olmaz
//        // Ritüel cooldown'ları yüklenebilir (opsiyonel)
//    }
//
//    /**
//     * Boss bilgisi al
//     */
//    public BossData getBossData(UUID entityId) {
//        return activeBosses.get(entityId);
//    }
//
//    /**
//     * Boss kaldır (öldüğünde)
//     * BossBar'ı kesinlikle temizler
//     */
//    public void removeBoss(UUID entityId) {
//        activeBosses.remove(entityId);
//
//        // BossBar'ı da kaldır - KESINLIKLE temizle
//        org.bukkit.boss.BossBar bossBar = bossBars.remove(entityId);
//        if (bossBar != null) {
//            // Tüm oyuncuları kaldır
//            bossBar.removeAll();
//            // BossBar'ı null yap (güvenlik için)
//            bossBar.setVisible(false);
//        }
//
//        // Ekstra güvenlik: Eğer bossBars map'inde hala varsa, onu da temizle
//        bossBar = bossBars.get(entityId);
//        if (bossBar != null) {
//            bossBar.removeAll();
//            bossBar.setVisible(false);
//            bossBars.remove(entityId);
//        }
//    }
//}
