package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Disaster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Gelişmiş Felaket Yönetim Sistemi
 * 
 * Özellikler:
 * - Dinamik güç hesaplama (oyuncu sayısı + klan seviyesi)
 * - Kategoriler: Canlı felaketler, Doğa olayları
 * - Seviyeler: 1-3 (güç ve spawn sıklığı)
 * - Otomatik spawn sistemi
 * - Ekranda sayaç (BossBar)
 */
public class DisasterManager {
    private final Main plugin;
    private final ClanManager clanManager;
    private final DifficultyManager difficultyManager;
    
    private Disaster activeDisaster = null;
    private long lastDisasterTime = System.currentTimeMillis();
    
    /**
     * Aktif felaket durumunu kaydet (DataManager için)
     * Not: Entity'ler kaydedilemez, sadece felaket durumu kaydedilir
     */
    public DisasterState getDisasterState() {
        if (activeDisaster == null || activeDisaster.isDead()) {
            return null;
        }
        return new DisasterState(
            activeDisaster.getType(),
            activeDisaster.getCategory(),
            activeDisaster.getLevel(),
            activeDisaster.getStartTime(),
            activeDisaster.getDuration(),
            activeDisaster.getTarget() != null ? activeDisaster.getTarget() : null
        );
    }
    
    /**
     * Felaket durumunu yükle (DataManager'dan çağrılır)
     * Not: Entity'ler kaydedilemediği için, sadece süre kontrolü yapılır
     * Eğer süre dolmamışsa felaket iptal edilir (entity olmadan devam edemez)
     */
    public void loadDisasterState(DisasterState state) {
        if (state == null) return;
        
        // Süre kontrolü
        long elapsed = System.currentTimeMillis() - state.startTime;
        long remaining = state.duration - elapsed;
        
        if (remaining <= 0) {
            // Süre dolmuş, felaket bitti
            plugin.getLogger().info("Kaydedilmiş felaket süresi dolmuş, iptal edildi.");
            return;
        }
        
        // Entity'ler kaydedilemediği için felaketi iptal et
        // (Entity olmadan felaket devam edemez)
        plugin.getLogger().warning("Aktif felaket tespit edildi ancak entity'ler kaydedilemediği için iptal edildi: " + 
            state.type.name() + " (Kalan süre: " + (remaining / 1000) + " saniye)");
        
        // İsteğe bağlı: Felaketi yeniden başlat (ancak bu karmaşık olabilir)
        // Şimdilik sadece iptal ediyoruz
    }
    
    /**
     * Felaket durumu (kayıt için)
     */
    public static class DisasterState {
        public final Disaster.Type type;
        public final Disaster.Category category;
        public final int level;
        public final long startTime;
        public final long duration;
        public final Location target;
        
        public DisasterState(Disaster.Type type, Disaster.Category category, int level,
                           long startTime, long duration, Location target) {
            this.type = type;
            this.category = category;
            this.level = level;
            this.startTime = startTime;
            this.duration = duration;
            this.target = target;
        }
    }
    
    // Spawn zamanları (ms)
    private static final long LEVEL_1_INTERVAL = 86400000L;  // 1 gün
    private static final long LEVEL_2_INTERVAL = 259200000L; // 3 gün
    private static final long LEVEL_3_INTERVAL = 604800000L; // 7 gün
    
    // BossBar (ekranda sayaç)
    private BossBar disasterBossBar = null;
    private BukkitTask bossBarUpdateTask = null;
    
    // Countdown BossBar (spawn olacağı zamanı gösterir)
    private BossBar countdownBossBar = null;
    private BukkitTask countdownUpdateTask = null;
    
    public DisasterManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.difficultyManager = plugin.getDifficultyManager();
    }
    
    /**
     * Güç hesaplama formülü
     * 
     * Formül: basePower * (1 + playerCount * 0.1 + avgClanLevel * 0.15)
     * 
     * @param basePower Temel güç (seviyeye göre)
     * @return Hesaplanmış güç
     */
    public DisasterPower calculateDisasterPower(int level) {
        // Oyuncu sayısı
        int playerCount = Bukkit.getOnlinePlayers().size();
        
        // Ortalama klan seviyesi
        Collection<Clan> clans = clanManager.getAllClans();
        double avgClanLevel = 0;
        if (!clans.isEmpty()) {
            int totalLevel = 0;
            for (Clan clan : clans) {
                totalLevel += clan.getTechLevel();
            }
            avgClanLevel = (double) totalLevel / clans.size();
        }
        
        // Temel güç (seviyeye göre)
        double baseHealth;
        double baseDamage;
        switch (level) {
            case 1:
                baseHealth = 500.0;
                baseDamage = 1.0;
                break;
            case 2:
                baseHealth = 1500.0;
                baseDamage = 2.0;
                break;
            case 3:
                baseHealth = 5000.0;
                baseDamage = 5.0;
                break;
            default:
                baseHealth = 500.0;
                baseDamage = 1.0;
        }
        
        // Güç çarpanı
        double powerMultiplier = 1.0 + (playerCount * 0.1) + (avgClanLevel * 0.15);
        
        // Hesaplanmış güç
        double calculatedHealth = baseHealth * powerMultiplier;
        double calculatedDamage = baseDamage * powerMultiplier;
        
        return new DisasterPower(calculatedHealth, calculatedDamage, powerMultiplier);
    }
    
    /**
     * Güç verisi sınıfı
     */
    public static class DisasterPower {
        public final double health;
        public final double damage;
        public final double multiplier;
        
        public DisasterPower(double health, double damage, double multiplier) {
            this.health = health;
            this.damage = damage;
            this.multiplier = multiplier;
        }
    }
    
    /**
     * Felaket başlat
     */
    public void triggerDisaster(Disaster.Type type, int level) {
        World world = Bukkit.getWorlds().get(0);
        Location centerLoc = difficultyManager.getCenterLocation();
        if (centerLoc == null) {
            centerLoc = world.getSpawnLocation();
        }
        
        // Merkezden en uzak noktayı bul (5000 blok)
        int x = centerLoc.getBlockX() + (new Random().nextBoolean() ? 5000 : -5000);
        int z = centerLoc.getBlockZ() + (new Random().nextBoolean() ? 5000 : -5000);
        
        // Chunk'ı force load et (felaket hareket edebilsin diye)
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        world.getChunkAt(chunkX, chunkZ).load(true); // Force load
        
        // Chunk yüklendikten sonra spawn yap
        int y = world.getHighestBlockYAt(x, z);
        Location spawnLoc = new Location(world, x, y + 1, z);
        
        triggerDisaster(type, level, spawnLoc);
    }
    
    /**
     * Felaket başlat (konum belirtilmiş)
     */
    public void triggerDisaster(Disaster.Type type, int level, Location spawnLoc) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        // Spawn lokasyonunun chunk'ını force load et
        World world = spawnLoc.getWorld();
        int chunkX = spawnLoc.getBlockX() >> 4;
        int chunkZ = spawnLoc.getBlockZ() >> 4;
        world.getChunkAt(chunkX, chunkZ).load(true); // Force load
        
        // Canlı felaketler için merkeze giden yol boyunca chunk'ları da yükle (opsiyonel, performans için)
        // NOT: DisasterTask içinde chunk yönetimi yapılıyor, burada sadece spawn chunk'ını yükle
        
        Disaster.Category category = Disaster.getCategory(type);
        DisasterPower power = calculateDisasterPower(level);
        long duration = Disaster.getDefaultDuration(type, level);
        
        Entity entity = null;
        
        // Canlı felaketler için entity oluştur
        if (category == Disaster.Category.CREATURE) {
            entity = spawnCreatureDisaster(type, spawnLoc, power);
        }
        
        // Felaket oluştur
        activeDisaster = new Disaster(type, category, level, entity, 
                                     difficultyManager.getCenterLocation(), 
                                     power.health, power.damage, duration);
        
        // Countdown BossBar'ı kaldır
        if (countdownBossBar != null) {
            countdownBossBar.removeAll();
            countdownBossBar = null;
        }
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
            countdownUpdateTask = null;
        }
        
        // BossBar oluştur
        createBossBar(activeDisaster);
        
        // Broadcast
        String disasterName = getDisasterDisplayName(type);
        Bukkit.broadcastMessage("§c§l⚠ FELAKET BAŞLADI! ⚠");
        Bukkit.broadcastMessage("§4§l" + disasterName + " §7(Seviye " + level + ")");
        Bukkit.broadcastMessage("§7Güç Çarpanı: §e" + String.format("%.2f", power.multiplier) + "x");
        
        lastDisasterTime = System.currentTimeMillis();
    }
    
    /**
     * Canlı felaket spawn et
     */
    private Entity spawnCreatureDisaster(Disaster.Type type, Location loc, DisasterPower power) {
        World world = loc.getWorld();
        
        switch (type) {
            case TITAN_GOLEM:
                Giant golem = (Giant) world.spawnEntity(loc, EntityType.GIANT);
                golem.setCustomName("§4§lTITAN GOLEM");
                if (golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    golem.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(power.health);
                }
                golem.setHealth(power.health);
                return golem;
                
            case ABYSSAL_WORM:
                Silverfish worm = (Silverfish) world.spawnEntity(loc, EntityType.SILVERFISH);
                worm.setCustomName("§5§lHİÇLİK SOLUCANI");
                if (worm.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    worm.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(power.health);
                }
                worm.setHealth(power.health);
                worm.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.INVISIBILITY, 999999, 0, false, false));
                return worm;
                
            case CHAOS_DRAGON:
                EnderDragon dragon = (EnderDragon) world.spawnEntity(loc, EntityType.ENDER_DRAGON);
                dragon.setCustomName("§5§lKHAOS EJDERİ");
                if (dragon.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    dragon.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(power.health);
                }
                dragon.setHealth(power.health);
                return dragon;
                
            case VOID_TITAN:
                Wither wither = (Wither) world.spawnEntity(loc, EntityType.WITHER);
                wither.setCustomName("§8§lBOŞLUK TİTANI");
                if (wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null) {
                    wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(power.health);
                }
                wither.setHealth(power.health);
                return wither;
                
            default:
                return null;
        }
    }
    
    /**
     * BossBar oluştur ve güncelle
     */
    private void createBossBar(Disaster disaster) {
        if (disasterBossBar != null) {
            disasterBossBar.removeAll();
        }
        
        String title = "§c§l" + getDisasterDisplayName(disaster.getType()) + 
                      " §7(Seviye " + disaster.getLevel() + ")";
        disasterBossBar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SOLID);
        
        // Tüm oyunculara göster
        for (Player player : Bukkit.getOnlinePlayers()) {
            disasterBossBar.addPlayer(player);
        }
        
        // Güncelleme task'ı
        if (bossBarUpdateTask != null) {
            bossBarUpdateTask.cancel();
        }
        
        bossBarUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeDisaster == null || activeDisaster.isDead()) {
                if (disasterBossBar != null) {
                    disasterBossBar.removeAll();
                    disasterBossBar = null;
                }
                if (bossBarUpdateTask != null) {
                    bossBarUpdateTask.cancel();
                    bossBarUpdateTask = null;
                }
                // Felaket bittiğinde countdown'u tekrar göster
                updateCountdownBossBar();
                return;
            }
            
            // Can güncelle
            double health = activeDisaster.getCurrentHealth();
            double maxHealth = activeDisaster.getMaxHealth();
            double progress = Math.max(0, Math.min(1, health / maxHealth));
            disasterBossBar.setProgress(progress);
            
            // Başlık güncelle
            String timeLeft = formatTime(activeDisaster.getRemainingTime());
            String healthText = String.format("%.0f/%.0f", health, maxHealth);
            disasterBossBar.setTitle("§c§l" + getDisasterDisplayName(activeDisaster.getType()) + 
                                    " §7| §c" + healthText + " §7| §e" + timeLeft);
            
            // ActionBar ile sağ üstte göster (her oyuncuya)
            // PERFORMANS OPTİMİZASYONU: getOnlinePlayers()'ı bir kez çağır
            java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
            String actionBarText = "§c§l" + getDisasterDisplayName(activeDisaster.getType()) + 
                                 " §7| §c" + healthText + " §7| §e⏰ " + timeLeft;
            for (Player player : onlinePlayers) {
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                
                // Yeni oyuncuları ekle (aynı döngüde)
                if (!disasterBossBar.getPlayers().contains(player)) {
                    disasterBossBar.addPlayer(player);
                }
            }
        }, 0L, 20L); // Her saniye
    }
    
    /**
     * Zaman formatla (ms -> dakika:saniye)
     */
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    /**
     * Felaket ismi
     */
    public String getDisasterDisplayName(Disaster.Type type) {
        switch (type) {
            case TITAN_GOLEM: return "Titan Golem";
            case ABYSSAL_WORM: return "Hiçlik Solucanı";
            case CHAOS_DRAGON: return "Khaos Ejderi";
            case VOID_TITAN: return "Boşluk Titanı";
            case SOLAR_FLARE: return "Güneş Patlaması";
            case EARTHQUAKE: return "Deprem";
            case METEOR_SHOWER: return "Meteor Yağmuru";
            case VOLCANIC_ERUPTION: return "Volkanik Patlama";
            default: return "Bilinmeyen Felaket";
        }
    }
    
    /**
     * Seviyeye göre spawn zamanı kontrolü
     */
    public boolean shouldSpawnDisaster(int level) {
        long elapsed = System.currentTimeMillis() - lastDisasterTime;
        long interval;
        
        switch (level) {
            case 1: interval = LEVEL_1_INTERVAL; break;
            case 2: interval = LEVEL_2_INTERVAL; break;
            case 3: interval = LEVEL_3_INTERVAL; break;
            default: return false;
        }
        
        return elapsed >= interval;
    }
    
    /**
     * Otomatik felaket spawn kontrolü
     */
    public void checkAutoSpawn() {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            // Aktif felaket varsa countdown'u kaldır
            if (countdownBossBar != null) {
                countdownBossBar.removeAll();
                countdownBossBar = null;
            }
            if (countdownUpdateTask != null) {
                countdownUpdateTask.cancel();
                countdownUpdateTask = null;
            }
            return; // Zaten aktif felaket var
        }
        
        // Countdown BossBar'ı güncelle
        updateCountdownBossBar();
        
        // Seviye 1 kontrolü (her gün)
        if (shouldSpawnDisaster(1)) {
            Disaster.Type[] level1Types = {Disaster.Type.SOLAR_FLARE};
            Disaster.Type randomType = level1Types[new Random().nextInt(level1Types.length)];
            triggerDisaster(randomType, 1);
            return;
        }
        
        // Seviye 2 kontrolü (3 günde bir)
        if (shouldSpawnDisaster(2)) {
            Disaster.Type[] level2Types = {Disaster.Type.ABYSSAL_WORM, Disaster.Type.EARTHQUAKE, Disaster.Type.METEOR_SHOWER};
            Disaster.Type randomType = level2Types[new Random().nextInt(level2Types.length)];
            triggerDisaster(randomType, 2);
            return;
        }
        
        // Seviye 3 kontrolü (7 günde bir)
        if (shouldSpawnDisaster(3)) {
            Disaster.Type[] level3Types = {Disaster.Type.TITAN_GOLEM, Disaster.Type.CHAOS_DRAGON, 
                                          Disaster.Type.VOID_TITAN, Disaster.Type.VOLCANIC_ERUPTION};
            Disaster.Type randomType = level3Types[new Random().nextInt(level3Types.length)];
            triggerDisaster(randomType, 3);
            return;
        }
    }
    
    /**
     * Countdown BossBar'ı güncelle (spawn olacağı zamanı gösterir)
     */
    private void updateCountdownBossBar() {
        long elapsed = System.currentTimeMillis() - lastDisasterTime;
        
        // En yakın spawn zamanını bul
        long nextSpawnTime = Long.MAX_VALUE;
        int nextLevel = 0;
        
        for (int level = 1; level <= 3; level++) {
            long interval;
            switch (level) {
                case 1: interval = LEVEL_1_INTERVAL; break;
                case 2: interval = LEVEL_2_INTERVAL; break;
                case 3: interval = LEVEL_3_INTERVAL; break;
                default: continue;
            }
            
            long remaining = interval - elapsed;
            if (remaining > 0 && remaining < nextSpawnTime) {
                nextSpawnTime = remaining;
                nextLevel = level;
            }
        }
        
        // Eğer hiç spawn zamanı yoksa (hepsi geçmişse), en kısa interval'i kullan
        if (nextSpawnTime == Long.MAX_VALUE || nextSpawnTime <= 0) {
            // En kısa interval'i bul
            long minInterval = Math.min(LEVEL_1_INTERVAL, Math.min(LEVEL_2_INTERVAL, LEVEL_3_INTERVAL));
            long timeSinceLast = elapsed % minInterval;
            nextSpawnTime = minInterval - timeSinceLast;
            nextLevel = 1; // En kısa interval seviye 1
        }
        
        // Countdown BossBar oluştur veya güncelle
        if (countdownBossBar == null) {
            countdownBossBar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
            for (Player player : Bukkit.getOnlinePlayers()) {
                countdownBossBar.addPlayer(player);
            }
            
            // Güncelleme task'ı
            if (countdownUpdateTask != null) {
                countdownUpdateTask.cancel();
            }
            
            countdownUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (activeDisaster != null && !activeDisaster.isDead()) {
                    // Aktif felaket varsa countdown'u kaldır
                    if (countdownBossBar != null) {
                        countdownBossBar.removeAll();
                        countdownBossBar = null;
                    }
                    if (countdownUpdateTask != null) {
                        countdownUpdateTask.cancel();
                        countdownUpdateTask = null;
                    }
                    return;
                }
                
                long currentElapsed = System.currentTimeMillis() - lastDisasterTime;
                long currentNextSpawnTime = Long.MAX_VALUE;
                int currentNextLevel = 0;
                
                for (int level = 1; level <= 3; level++) {
                    long interval;
                    switch (level) {
                        case 1: interval = LEVEL_1_INTERVAL; break;
                        case 2: interval = LEVEL_2_INTERVAL; break;
                        case 3: interval = LEVEL_3_INTERVAL; break;
                        default: continue;
                    }
                    
                    long remaining = interval - currentElapsed;
                    if (remaining > 0 && remaining < currentNextSpawnTime) {
                        currentNextSpawnTime = remaining;
                        currentNextLevel = level;
                    }
                }
                
                if (currentNextSpawnTime == Long.MAX_VALUE || currentNextSpawnTime <= 0) {
                    // En kısa interval'i bul
                    long minInterval = Math.min(LEVEL_1_INTERVAL, Math.min(LEVEL_2_INTERVAL, LEVEL_3_INTERVAL));
                    long timeSinceLast = currentElapsed % minInterval;
                    currentNextSpawnTime = minInterval - timeSinceLast;
                    currentNextLevel = 1; // En kısa interval seviye 1
                }
                
                if (countdownBossBar != null) {
                    // Progress hesapla (0'dan 1'e)
                    long maxInterval = currentNextLevel == 1 ? LEVEL_1_INTERVAL : 
                                      currentNextLevel == 2 ? LEVEL_2_INTERVAL : LEVEL_3_INTERVAL;
                    double progress = Math.max(0, Math.min(1, (double)currentNextSpawnTime / maxInterval));
                    countdownBossBar.setProgress(progress);
                    
                    // Başlık güncelle
                    String timeText = formatTime(currentNextSpawnTime);
                    String levelText = "Seviye " + currentNextLevel;
                    countdownBossBar.setTitle("§e§l⏰ Sonraki Felaket: §6" + levelText + " §7| §e" + timeText);
                    
                    // ActionBar ile sağ üstte göster (her oyuncuya)
                    // PERFORMANS OPTİMİZASYONU: getOnlinePlayers()'ı bir kez çağır
                    String actionBarText = "§e§l⏰ Sonraki Felaket: §6" + levelText + " §7| §e" + timeText;
                    java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                    for (Player player : onlinePlayers) {
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                        
                        // Yeni oyuncuları ekle (aynı döngüde)
                        if (!countdownBossBar.getPlayers().contains(player)) {
                            countdownBossBar.addPlayer(player);
                        }
                    }
                }
            }, 0L, 20L); // Her saniye
        }
        
        // İlk güncelleme
        long maxInterval = nextLevel == 1 ? LEVEL_1_INTERVAL : 
                          nextLevel == 2 ? LEVEL_2_INTERVAL : LEVEL_3_INTERVAL;
        double progress = Math.max(0, Math.min(1, (double)nextSpawnTime / maxInterval));
        countdownBossBar.setProgress(progress);
        
        String timeText = formatTime(nextSpawnTime);
        String levelText = "Seviye " + nextLevel;
        countdownBossBar.setTitle("§e§l⏰ Sonraki Felaket: §6" + levelText + " §7| §e" + timeText);
    }
    
    // Getter/Setter
    public Disaster getActiveDisaster() { return activeDisaster; }
    public void setActiveDisaster(Disaster d) { 
        this.activeDisaster = d;
        if (d == null) {
            if (disasterBossBar != null) {
                disasterBossBar.removeAll();
                disasterBossBar = null;
            }
            if (bossBarUpdateTask != null) {
                bossBarUpdateTask.cancel();
                bossBarUpdateTask = null;
            }
            // Felaket bittiğinde countdown'u tekrar göster
            updateCountdownBossBar();
        }
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
        }
        
        if (disasterBossBar != null) {
            disasterBossBar.removeAll();
            disasterBossBar = null;
        }
        
        if (bossBarUpdateTask != null) {
            bossBarUpdateTask.cancel();
            bossBarUpdateTask = null;
        }
        
        if (countdownBossBar != null) {
            countdownBossBar.removeAll();
            countdownBossBar = null;
        }
        
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
            countdownUpdateTask = null;
        }
        
        Bukkit.broadcastMessage("§a§lTüm felaketler temizlendi!");
    }
    
    // Eski metodlar (geriye dönük uyumluluk)
    public void triggerDisaster(Disaster.Type type) {
        int level = Disaster.getDefaultLevel(type);
        triggerDisaster(type, level);
    }
    
    public void triggerDisaster(Disaster.Type type, Location spawnLoc) {
        int level = Disaster.getDefaultLevel(type);
        triggerDisaster(type, level, spawnLoc);
    }
    
    /**
     * Yeni oyuncu giriş yaptığında BossBar'a ekle
     */
    public void onPlayerJoin(Player player) {
        if (disasterBossBar != null) {
            disasterBossBar.addPlayer(player);
        }
        if (countdownBossBar != null) {
            countdownBossBar.addPlayer(player);
        }
    }
    
    // Eski metodlar
    private me.mami.stratocraft.manager.BuffManager buffManager;
    private me.mami.stratocraft.manager.TerritoryManager territoryManager;
    
    public void setBuffManager(me.mami.stratocraft.manager.BuffManager bm) {
        this.buffManager = bm;
    }
    
    public void setTerritoryManager(me.mami.stratocraft.manager.TerritoryManager tm) {
        this.territoryManager = tm;
    }
    
    public void dropRewards(Disaster disaster) {
        if (disaster == null || disaster.getEntity() == null) return;
        Location loc = disaster.getEntity().getLocation();
        
        // Enkaz yığını oluştur
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
        
        // Kahraman Buff'ı
        if (territoryManager != null && buffManager != null) {
            Clan affectedClan = territoryManager.getTerritoryOwner(loc);
            if (affectedClan != null) {
                buffManager.applyHeroBuff(affectedClan);
            }
        }
        
        Bukkit.broadcastMessage("§a§lFelaket yok edildi! Ödüller düştü!");
    }
    
    private void createWreckageStructure(Location center) {
        org.bukkit.Material wreckageMat = org.bukkit.Material.ANCIENT_DEBRIS;
        int surfaceY = center.getWorld().getHighestBlockYAt(center);
        Location surfaceLoc = center.clone();
        surfaceLoc.setY(surfaceY);
        
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
        
        if (me.mami.stratocraft.Main.getInstance() != null) {
            me.mami.stratocraft.manager.ScavengerManager sm = 
                ((me.mami.stratocraft.Main) me.mami.stratocraft.Main.getInstance()).getScavengerManager();
            if (sm != null) {
                sm.markWreckage(surfaceLoc, java.util.UUID.randomUUID());
            }
        }
    }
    
    public void forceWormSurface(Location seismicLocation) {
        Disaster disaster = getActiveDisaster();
        if (disaster == null || disaster.getType() != Disaster.Type.ABYSSAL_WORM) return;
        
        Entity worm = disaster.getEntity();
        if (worm == null) return;
        
        Location surfaceLoc = seismicLocation.clone();
        surfaceLoc.setY(seismicLocation.getWorld().getHighestBlockYAt(seismicLocation) + 1);
        worm.teleport(surfaceLoc);
        Bukkit.broadcastMessage("§6§lSİSMİK ÇEKİÇ! Hiçlik Solucanı yüzeye çıkmaya zorlandı!");
    }
}
