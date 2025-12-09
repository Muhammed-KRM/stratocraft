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
    private DifficultyManager difficultyManager; // Final değil, sonradan set edilecek
    private DisasterConfigManager configManager; // Config yöneticisi
    
    // Yeni dinamik zorluk sistemi (interface kullanarak gelecekte değiştirilebilir)
    private DisasterPowerConfig powerConfig;
    private IPowerCalculator playerPowerCalculator;
    private IServerPowerCalculator serverPowerCalculator;
    
    // Yeni Stratocraft Güç Sistemi (köprü fonksiyon için)
    private me.mami.stratocraft.manager.StratocraftPowerSystem stratocraftPowerSystem;
    
    private Disaster activeDisaster = null;
    private long lastDisasterTime = System.currentTimeMillis();
    
    // Mini felaket sistemi
    private long lastMiniDisasterTime = System.currentTimeMillis();
    private int miniDisasterCountToday = 0; // Bugün spawn olan mini felaket sayısı
    private long lastDayReset = System.currentTimeMillis();
    
    // Plan'a göre: 2 dakika önce uyarı sistemi
    private long lastWarningTime = 0;
    private static final long WARNING_INTERVAL = 120000L; // 2 dakika = 120000 ms
    
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
    
    // Countdown Scoreboard (spawn olacağı zamanı gösterir - sağ üst köşe)
    private org.bukkit.scoreboard.Scoreboard countdownScoreboard = null;
    private org.bukkit.scoreboard.Objective countdownObjective = null;
    private BukkitTask countdownUpdateTask = null;
    
    public DisasterManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        // difficultyManager henüz oluşturulmamış olabilir, sonradan set edilecek
        this.difficultyManager = plugin.getDifficultyManager();
        // ConfigManager'dan DisasterConfigManager al
        if (plugin.getConfigManager() != null) {
            this.configManager = plugin.getConfigManager().getDisasterConfigManager();
        }
        
        // Dinamik zorluk sistemini başlat (config yüklendikten sonra Main.java'da çağrılacak)
        // initializeDynamicDifficulty() Main.java'da çağrılmalı
    }
    
    /**
     * DifficultyManager'ı set et (Main.java'da sonradan çağrılır)
     */
    public void setDifficultyManager(me.mami.stratocraft.manager.DifficultyManager dm) {
        this.difficultyManager = dm;
    }
    
    /**
     * ConfigManager'ı set et
     */
    public void setConfigManager(DisasterConfigManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * Güç hesaplama formülü (Config'den okur)
     * 
     * İki sistem desteklenir:
     * 1. Yeni Dinamik Zorluk Sistemi (önerilen)
     * 2. Eski Sistem (geriye dönük uyumluluk)
     * 
     * @param level Felaket seviyesi
     * @return Hesaplanmış güç
     */
    public DisasterPower calculateDisasterPower(int level) {
        // Config'den seviye config'i al
        me.mami.stratocraft.model.DisasterConfig levelConfig;
        if (configManager != null) {
            levelConfig = configManager.getConfigForLevel(level);
        } else {
            levelConfig = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        // Config'den temel güç
        double baseHealth = levelConfig.getBaseHealth() * levelConfig.getHealthMultiplier();
        double baseDamage = levelConfig.getBaseDamage() * levelConfig.getDamageMultiplier();
        
        // Yeni dinamik zorluk sistemi aktif mi?
        if (powerConfig != null && powerConfig.isDynamicDifficultyEnabled() && 
            serverPowerCalculator != null) {
            return calculateDisasterPowerDynamic(levelConfig, baseHealth, baseDamage);
        }
        
        // Eski sistem (geriye dönük uyumluluk)
        return calculateDisasterPowerLegacy(levelConfig, baseHealth, baseDamage);
    }
    
    /**
     * Yeni dinamik zorluk sistemi ile güç hesaplama
     */
    private DisasterPower calculateDisasterPowerDynamic(
            me.mami.stratocraft.model.DisasterConfig levelConfig,
            double baseHealth, double baseDamage) {
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Yeni sistem varsa onu kullan, yoksa eski sistemi kullan
        double serverPower;
        
        if (stratocraftPowerSystem != null) {
            // Yeni Stratocraft Güç Sistemi kullan
            serverPower = calculateServerPowerWithNewSystem();
        } else if (serverPowerCalculator != null) {
            // Eski sistem (geriye dönük uyumluluk)
            serverPower = serverPowerCalculator.calculateServerPower();
        } else {
            // Hiçbir sistem yok, varsayılan değer
            serverPower = 0.0;
        }
        
        // Güç çarpanı hesaplama
        double powerScalingFactor = powerConfig.getPowerScalingFactor();
        double powerMultiplier = 1.0 + (serverPower / 100.0) * powerScalingFactor;
        
        // Maksimum ve minimum sınırlar
        double minMultiplier = powerConfig.getMinPowerMultiplier();
        double maxMultiplier = powerConfig.getMaxPowerMultiplier();
        powerMultiplier = Math.max(minMultiplier, Math.min(maxMultiplier, powerMultiplier));
        
        // Hesaplanmış güç
        double calculatedHealth = baseHealth * powerMultiplier;
        double calculatedDamage = baseDamage * powerMultiplier;
        
        return new DisasterPower(calculatedHealth, calculatedDamage, powerMultiplier);
    }
    
    // ✅ PERFORMANS: Sunucu güç cache (felaket spawn'larında gereksiz hesaplama önleme)
    private double cachedServerPowerNewSystem = 0.0;
    private long lastServerPowerUpdate = 0;
    private static final long SERVER_POWER_CACHE_DURATION = 10000L; // 10 saniye
    
    /**
     * Yeni Stratocraft Güç Sistemi ile sunucu gücü hesapla (köprü fonksiyon)
     * ✅ PERFORMANS: Cache kullanarak gereksiz hesaplamaları önler
     */
    private double calculateServerPowerWithNewSystem() {
        if (stratocraftPowerSystem == null) return 0.0;
        
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        if (now - lastServerPowerUpdate < SERVER_POWER_CACHE_DURATION) {
            return cachedServerPowerNewSystem;
        }
        
        java.util.Collection<? extends org.bukkit.entity.Player> players = org.bukkit.Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            cachedServerPowerNewSystem = 0.0;
            lastServerPowerUpdate = now;
            return 0.0;
        }
        
        double totalPower = 0.0;
        int activePlayerCount = 0;
        
        // Tüm oyuncuların güç puanlarını topla (yeni sistemden - cache kullanır)
        for (org.bukkit.entity.Player player : players) {
            if (player.isOnline() && !player.isDead()) {
                me.mami.stratocraft.model.PlayerPowerProfile profile = 
                    stratocraftPowerSystem.calculatePlayerProfile(player); // Cache kullanır
                // Felaket için combat power önemli (config'den ayarlanabilir)
                double playerPower = profile.getTotalCombatPower();
                totalPower += playerPower;
                activePlayerCount++;
            }
        }
        
        if (activePlayerCount == 0) {
            cachedServerPowerNewSystem = 0.0;
            lastServerPowerUpdate = now;
            return 0.0;
        }
        
        // Ortalama güç
        double averagePower = totalPower / activePlayerCount;
        
        // Oyuncu sayısı çarpanı (config'den)
        // ✅ NULL KONTROLÜ: powerConfig null olabilir
        double playerCountMultiplier = 1.0;
        if (powerConfig != null) {
            playerCountMultiplier = powerConfig.getPlayerCountMultiplier(activePlayerCount);
        }
        
        // Sunucu güç puanı = Ortalama × Oyuncu Sayısı Çarpanı
        cachedServerPowerNewSystem = averagePower * playerCountMultiplier;
        lastServerPowerUpdate = now;
        
        return cachedServerPowerNewSystem;
    }
    
    /**
     * Sunucu güç cache'ini temizle (oyuncu giriş/çıkışında çağrılabilir)
     */
    public void clearServerPowerCache() {
        cachedServerPowerNewSystem = 0.0;
        lastServerPowerUpdate = 0;
    }
    
    /**
     * Eski sistem ile güç hesaplama (geriye dönük uyumluluk)
     */
    private DisasterPower calculateDisasterPowerLegacy(
            me.mami.stratocraft.model.DisasterConfig levelConfig,
            double baseHealth, double baseDamage) {
        
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
        
        // Eski çarpanlar
        double playerMultiplier = powerConfig != null ? 
            powerConfig.getLegacyPlayerMultiplier() : levelConfig.getPlayerMultiplier();
        double clanMultiplier = powerConfig != null ? 
            powerConfig.getLegacyClanMultiplier() : levelConfig.getClanMultiplier();
        
        // Güç çarpanı
        double powerMultiplier = 1.0 + (playerCount * playerMultiplier) + (avgClanLevel * clanMultiplier);
        
        // Hesaplanmış güç
        double calculatedHealth = baseHealth * powerMultiplier;
        double calculatedDamage = baseDamage * powerMultiplier;
        
        return new DisasterPower(calculatedHealth, calculatedDamage, powerMultiplier);
    }
    
    /**
     * Dinamik zorluk sistemini başlat
     * Interface kullanarak gelecekte farklı implementasyonlar eklenebilir
     */
    public void initializeDynamicDifficulty(DisasterPowerConfig powerConfig,
                                           IPowerCalculator playerPowerCalculator,
                                           IServerPowerCalculator serverPowerCalculator) {
        this.powerConfig = powerConfig;
        this.playerPowerCalculator = playerPowerCalculator;
        this.serverPowerCalculator = serverPowerCalculator;
    }
    
    /**
     * Yeni Stratocraft Güç Sistemini set et (köprü fonksiyon için)
     */
    public void setStratocraftPowerSystem(me.mami.stratocraft.manager.StratocraftPowerSystem powerSystem) {
        this.stratocraftPowerSystem = powerSystem;
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
        Location centerLoc = null;
        if (difficultyManager != null) {
            centerLoc = difficultyManager.getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = world.getSpawnLocation();
        }
        
        // Config'den spawn mesafesini al
        double spawnDistance = 5000.0; // Varsayılan
        if (configManager != null) {
            me.mami.stratocraft.model.DisasterConfig config = configManager.getConfig(type, level);
            spawnDistance = config.getSpawnDistance();
        }
        
        // Merkezden en uzak noktayı bul (config'den okunan mesafe)
        int distance = (int) spawnDistance;
        int x = centerLoc.getBlockX() + (new Random().nextBoolean() ? distance : -distance);
        int z = centerLoc.getBlockZ() + (new Random().nextBoolean() ? distance : -distance);
        
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
        Location targetLoc = null;
        if (difficultyManager != null) {
            targetLoc = difficultyManager.getCenterLocation();
        }
        if (targetLoc == null) {
            targetLoc = spawnLoc.getWorld().getSpawnLocation();
        }
        activeDisaster = new Disaster(type, category, level, entity, 
                                     targetLoc, 
                                     power.health, power.damage, duration);
        
        // Hedef kristali belirle
        setDisasterTarget(activeDisaster);
        
        // Countdown Scoreboard'ı kaldır
        if (countdownObjective != null) {
            countdownObjective.unregister();
            countdownObjective = null;
        }
        if (countdownScoreboard != null) {
            // Tüm oyuncuların scoreboard'unu temizle
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboard().equals(countdownScoreboard)) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                }
            }
            countdownScoreboard = null;
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
     * Canlı felaket spawn et (Config kullanır)
     */
    private Entity spawnCreatureDisaster(Disaster.Type type, Location loc, DisasterPower power) {
        World world = loc.getWorld();
        
        // Config'den ayarları al
        me.mami.stratocraft.model.DisasterConfig config = null;
        if (configManager != null) {
            int level = Disaster.getDefaultLevel(type);
            config = configManager.getConfig(type, level);
        }
        if (config == null) {
            config = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        Entity entity = null;
        
        switch (type) {
            case TITAN_GOLEM:
                entity = world.spawnEntity(loc, EntityType.GIANT);
                entity.setCustomName("§4§lTITAN GOLEM");
                break;
                
            case ABYSSAL_WORM:
                entity = world.spawnEntity(loc, EntityType.SILVERFISH);
                entity.setCustomName("§5§lHİÇLİK SOLUCANI");
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.INVISIBILITY, 999999, 0, false, false));
                }
                break;
                
            case CHAOS_DRAGON:
                entity = world.spawnEntity(loc, EntityType.ENDER_DRAGON);
                entity.setCustomName("§5§lKHAOS EJDERİ");
                break;
                
            case VOID_TITAN:
                entity = world.spawnEntity(loc, EntityType.WITHER);
                entity.setCustomName("§8§lBOŞLUK TİTANI");
                break;
                
            case ICE_LEVIATHAN:
                entity = world.spawnEntity(loc, EntityType.ELDER_GUARDIAN);
                entity.setCustomName("§b§lBUZUL LEVİATHAN");
                if (entity instanceof org.bukkit.entity.LivingEntity) {
                    ((org.bukkit.entity.LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, 999999, 0, false, false));
                }
                break;
                
            default:
                return null;
        }
        
        // DisasterUtils ile güçlendirme (tüm entity'ler için ortak)
        if (entity != null) {
            me.mami.stratocraft.util.DisasterUtils.strengthenEntity(entity, config, power.multiplier);
        }
        
        return entity;
    }
    
    /**
     * BossBar oluştur ve güncelle
     */
    private void createBossBar(Disaster disaster) {
        // Eski BossBar'ı temizle
        if (disasterBossBar != null) {
            disasterBossBar.removeAll();
            disasterBossBar = null;
        }
        
        // Canlı felaketler için BossBar oluştur
        if (disaster.getCategory() == Disaster.Category.CREATURE && disaster.getEntity() != null) {
            String disasterName = getDisasterDisplayName(disaster.getType());
            disasterBossBar = Bukkit.createBossBar(
                "§c§l" + disasterName,
                BarColor.RED,
                BarStyle.SOLID
            );
            
            // Tüm oyunculara ekle
            for (Player player : Bukkit.getOnlinePlayers()) {
                disasterBossBar.addPlayer(player);
            }
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
            
            // Can ve zaman bilgisi
            double health = activeDisaster.getCurrentHealth();
            double maxHealth = activeDisaster.getMaxHealth();
            double healthPercent = Math.max(0.0, Math.min(1.0, health / maxHealth));
            String timeLeft = formatTime(activeDisaster.getRemainingTime());
            String disasterName = getDisasterDisplayName(activeDisaster.getType());
            
            // Canlı felaketler için BossBar güncelle
            if (activeDisaster.getCategory() == Disaster.Category.CREATURE && disasterBossBar != null) {
                String bossBarTitle = "§c§l" + disasterName + " §7| §c" + 
                    String.format("%.0f/%.0f", health, maxHealth) + " §7| §e⏰ " + timeLeft;
                disasterBossBar.setTitle(bossBarTitle);
                disasterBossBar.setProgress(healthPercent);
                
                // Can durumuna göre renk değiştir
                if (healthPercent > 0.6) {
                    disasterBossBar.setColor(BarColor.RED);
                } else if (healthPercent > 0.3) {
                    disasterBossBar.setColor(BarColor.YELLOW);
                } else {
                    disasterBossBar.setColor(BarColor.GREEN);
                }
                
                // Yeni oyuncuları ekle (optimizasyon: sadece yeni oyuncular varsa)
                java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                for (Player player : onlinePlayers) {
                    if (!disasterBossBar.getPlayers().contains(player)) {
                        disasterBossBar.addPlayer(player);
                    }
                }
            } else {
                // Doğa olayları için ActionBar kullan
                java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                String actionBarText = "§c§l" + disasterName + " §7| §e⏰ " + timeLeft;
                for (Player player : onlinePlayers) {
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionBarText));
                }
            }
        }, 0L, 20L); // Her saniye
    }
    
    /**
     * HUD için countdown bilgisini al
     */
    public String[] getCountdownInfo() {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            return null; // Aktif felaket varsa countdown gösterme
        }
        
        long elapsed = System.currentTimeMillis() - lastDisasterTime;
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
        
        if (nextSpawnTime == Long.MAX_VALUE || nextSpawnTime <= 0) {
            long minInterval = Math.min(LEVEL_1_INTERVAL, Math.min(LEVEL_2_INTERVAL, LEVEL_3_INTERVAL));
            long timeSinceLast = elapsed % minInterval;
            nextSpawnTime = minInterval - timeSinceLast;
            nextLevel = 1;
        }
        
        String timeText = formatTime(nextSpawnTime);
        return new String[]{"Seviye " + nextLevel, timeText};
    }
    
    /**
     * Zaman formatla (ms -> dd/hh/mm/ss)
     */
    public String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (days > 0) {
            return String.format("%02d/%02d/%02d/%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("00/%02d/%02d/%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("00/00/%02d/%02d", minutes, seconds);
        } else {
            return String.format("00/00/00/%02d", seconds);
        }
    }
    
    /**
     * Seviyeye göre felaket tipi ismi (uyarı için)
     */
    private String getDisasterTypeNameForLevel(int level) {
        switch (level) {
            case 1: return "Günlük";
            case 2: return "Orta";
            case 3: return "Büyük";
            default: return "Bilinmeyen";
        }
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
            case ICE_LEVIATHAN: return "Buzul Leviathan";
            case ZOMBIE_HORDE: return "Zombi Ordusu";
            case SKELETON_LEGION: return "İskelet Lejyonu";
            case SPIDER_SWARM: return "Örümcek Sürüsü";
            case CREEPER_SWARM: return "Creeper Dalgası";
            case ZOMBIE_WAVE: return "Zombi Dalgası";
            case SOLAR_FLARE: return "Güneş Patlaması";
            case EARTHQUAKE: return "Deprem";
            case STORM: return "Fırtına";
            case METEOR_SHOWER: return "Meteor Yağmuru";
            case VOLCANIC_ERUPTION: return "Volkanik Patlama";
            case BOSS_BUFF_WAVE: return "Boss Güçlenme Dalgası";
            case MOB_INVASION: return "Mob İstilası";
            case PLAYER_BUFF_WAVE: return "Oyuncu Buff Dalgası";
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
            if (countdownObjective != null) {
                countdownObjective.unregister();
                countdownObjective = null;
            }
            if (countdownScoreboard != null) {
                // Tüm oyuncuların scoreboard'unu temizle
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getScoreboard().equals(countdownScoreboard)) {
                        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                    }
                }
                countdownScoreboard = null;
            }
            if (countdownUpdateTask != null) {
                countdownUpdateTask.cancel();
                countdownUpdateTask = null;
            }
            return; // Zaten aktif felaket var
        }
        
        // Countdown Scoreboard'ı güncelle
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
        
        // Mini felaket kontrolü (günde 2-5 kez, rastgele zamanda)
        checkMiniDisasterSpawn();
    }
    
    /**
     * Mini felaket otomatik spawn kontrolü
     * Günde 2-5 kez rastgele zamanda spawn olur
     */
    private void checkMiniDisasterSpawn() {
        long now = System.currentTimeMillis();
        
        // Gün sıfırlama kontrolü (24 saat = 86400000 ms)
        if (now - lastDayReset >= 86400000L) {
            miniDisasterCountToday = 0;
            lastDayReset = now;
        }
        
        // Günde maksimum 5 kez
        if (miniDisasterCountToday >= 5) {
            return;
        }
        
        // Minimum 2 kez spawn olmalı (eğer gün bitiyorsa)
        long timeSinceLastMini = now - lastMiniDisasterTime;
        long timeUntilDayEnd = 86400000L - (now - lastDayReset);
        
        // Eğer gün bitiyorsa ve henüz 2 kez spawn olmadıysa zorla spawn et
        if (timeUntilDayEnd < 3600000L && miniDisasterCountToday < 2) { // Son 1 saat
            spawnRandomMiniDisaster();
            return;
        }
        
        // Rastgele spawn kontrolü (2-6 saat arası rastgele aralık)
        long minInterval = 7200000L;  // 2 saat
        long maxInterval = 21600000L; // 6 saat
        long randomInterval = minInterval + (long)(random.nextDouble() * (maxInterval - minInterval));
        
        if (timeSinceLastMini >= randomInterval) {
            spawnRandomMiniDisaster();
        }
    }
    
    /**
     * Rastgele mini felaket spawn et
     */
    private void spawnRandomMiniDisaster() {
        Disaster.Type[] miniTypes = {
            Disaster.Type.BOSS_BUFF_WAVE,
            Disaster.Type.MOB_INVASION,
            Disaster.Type.PLAYER_BUFF_WAVE
        };
        
        Disaster.Type randomType = miniTypes[random.nextInt(miniTypes.length)];
        int level = Disaster.getDefaultLevel(randomType);
        
        // Mini felaketler için özel spawn (entity yok, sadece efektler)
        triggerDisaster(randomType, level);
        
        lastMiniDisasterTime = System.currentTimeMillis();
        miniDisasterCountToday++;
        
        Bukkit.broadcastMessage("§6§l⚡ MİNİ FELAKET: " + getDisasterDisplayName(randomType) + " ⚡");
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
        
        // Scoreboard oluştur (sağ üst köşe için)
        if (countdownScoreboard == null) {
            countdownScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }
        
        // Objective oluştur veya güncelle
        if (countdownObjective == null) {
            countdownObjective = countdownScoreboard.registerNewObjective("disaster_countdown", "dummy", "§e§l⏰ FELAKET SAYACI");
            countdownObjective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        }
        
        // Güncelleme task'ı
        if (countdownUpdateTask != null) {
            countdownUpdateTask.cancel();
        }
        
        countdownUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeDisaster != null && !activeDisaster.isDead()) {
                // Aktif felaket varsa countdown'u kaldır
                if (countdownObjective != null) {
                    countdownObjective.unregister();
                    countdownObjective = null;
                }
                if (countdownScoreboard != null) {
                    // Tüm oyuncuların scoreboard'unu temizle
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getScoreboard().equals(countdownScoreboard)) {
                            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                        }
                    }
                    countdownScoreboard = null;
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
            
            // Plan'a göre: Felaket spawn olmadan 2 dakika önce uyarı
            if (currentNextSpawnTime <= WARNING_INTERVAL && currentNextSpawnTime > (WARNING_INTERVAL - 2000)) {
                // 2 dakika kala uyarı (2 saniye tolerans)
                long now = System.currentTimeMillis();
                if (now - lastWarningTime >= WARNING_INTERVAL) {
                    String disasterTypeName = getDisasterTypeNameForLevel(currentNextLevel);
                    Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
                        "⚠ UYARI: " + disasterTypeName + " felaketi 2 dakika içinde başlayacak! ⚠");
                    lastWarningTime = now;
                }
            }
            
            // Scoreboard'u güncelle (sağ üst köşe)
            if (countdownScoreboard == null) {
                countdownScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            }
            if (countdownObjective == null) {
                countdownObjective = countdownScoreboard.registerNewObjective("disaster_countdown", "dummy", "§e§l⏰ FELAKET SAYACI");
                countdownObjective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            }
            
            // Tüm entry'leri temizle
            for (String entry : countdownScoreboard.getEntries()) {
                countdownScoreboard.resetScores(entry);
            }
            
            // Yeni bilgileri ekle
            String timeText = formatTime(currentNextSpawnTime);
            String levelText = "Seviye " + currentNextLevel;
            
            // Scoreboard entry'leri (yukarıdan aşağıya)
            org.bukkit.scoreboard.Team team1 = countdownScoreboard.getTeam("team1");
            if (team1 == null) {
                team1 = countdownScoreboard.registerNewTeam("team1");
            }
            team1.addEntry("§7");
            team1.setPrefix("§7");
            countdownObjective.getScore("§7").setScore(3);
            
            org.bukkit.scoreboard.Team team2 = countdownScoreboard.getTeam("team2");
            if (team2 == null) {
                team2 = countdownScoreboard.registerNewTeam("team2");
            }
            team2.addEntry("§6");
            team2.setPrefix("§e⏰ Sonraki: §6" + levelText);
            countdownObjective.getScore("§6").setScore(2);
            
            org.bukkit.scoreboard.Team team3 = countdownScoreboard.getTeam("team3");
            if (team3 == null) {
                team3 = countdownScoreboard.registerNewTeam("team3");
            }
            team3.addEntry("§5");
            team3.setPrefix("§7Kalan: §e" + timeText);
            countdownObjective.getScore("§5").setScore(1);
            
            // Tüm oyunculara scoreboard'u ata
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(countdownScoreboard);
            }
        }, 0L, 20L); // Her saniye
        
        // İlk güncelleme
        String timeText = formatTime(nextSpawnTime);
        String levelText = "Seviye " + nextLevel;
        
        // Scoreboard entry'leri (yukarıdan aşağıya)
        org.bukkit.scoreboard.Team team1 = countdownScoreboard.getTeam("team1");
        if (team1 == null) {
            team1 = countdownScoreboard.registerNewTeam("team1");
        }
        team1.addEntry("§7");
        team1.setPrefix("§7");
        countdownObjective.getScore("§7").setScore(3);
        
        org.bukkit.scoreboard.Team team2 = countdownScoreboard.getTeam("team2");
        if (team2 == null) {
            team2 = countdownScoreboard.registerNewTeam("team2");
        }
        team2.addEntry("§6");
        team2.setPrefix("§e⏰ Sonraki: §6" + levelText);
        countdownObjective.getScore("§6").setScore(2);
        
        org.bukkit.scoreboard.Team team3 = countdownScoreboard.getTeam("team3");
        if (team3 == null) {
            team3 = countdownScoreboard.registerNewTeam("team3");
        }
        team3.addEntry("§5");
        team3.setPrefix("§7Kalan: §e" + timeText);
        countdownObjective.getScore("§5").setScore(1);
        
        // Tüm oyunculara scoreboard'u ata
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(countdownScoreboard);
        }
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
        
        if (countdownObjective != null) {
            countdownObjective.unregister();
            countdownObjective = null;
        }
        if (countdownScoreboard != null) {
            // Tüm oyuncuların scoreboard'unu temizle
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getScoreboard().equals(countdownScoreboard)) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                }
            }
            countdownScoreboard = null;
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
     * Yeni oyuncu giriş yaptığında BossBar'a ekle ve cache'i temizle (güç hesaplama için)
     */
    public void onPlayerJoin(Player player) {
        // Cache'i temizle (yeni oyuncu gücü hesaplanacak)
        if (serverPowerCalculator != null) {
            serverPowerCalculator.clearCache();
        }
        if (disasterBossBar != null) {
            disasterBossBar.addPlayer(player);
        }
        if (countdownScoreboard != null) {
            player.setScoreboard(countdownScoreboard);
        }
    }
    
    /**
     * Oyuncu çıkış yaptığında cache'i temizle
     */
    public void onPlayerQuit(Player player) {
        // Cache'i temizle (oyuncu çıktı, güç hesaplaması değişecek)
        if (serverPowerCalculator != null) {
            serverPowerCalculator.clearCache();
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
        
        // Plan'a göre: Felaket yok edilince ödül
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
        
        // Plan'a göre: Klan kristali korunursa bonus ödül
        if (territoryManager != null) {
            Clan affectedClan = territoryManager.getTerritoryOwner(loc);
            if (affectedClan != null && affectedClan.getCrystalEntity() != null && !affectedClan.getCrystalEntity().isDead()) {
                // Kristal korundu - bonus ödül
                if (me.mami.stratocraft.manager.ItemManager.DARK_MATTER != null) {
                    loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.DARK_MATTER.clone());
                }
                if (me.mami.stratocraft.manager.ItemManager.STAR_CORE != null) {
                    loc.getWorld().dropItemNaturally(loc, me.mami.stratocraft.manager.ItemManager.STAR_CORE.clone());
                }
                Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.GOLD + "" + org.bukkit.ChatColor.BOLD + 
                    "⭐ BONUS ÖDÜL: " + affectedClan.getName() + " klanının kristali korundu! ⭐");
            }
        }
        
        // Kahraman Buff'ı
        if (territoryManager != null && buffManager != null) {
            Clan affectedClan = territoryManager.getTerritoryOwner(loc);
            if (affectedClan != null) {
                buffManager.applyHeroBuff(affectedClan);
            }
        }
        
        Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.GREEN + "" + org.bukkit.ChatColor.BOLD + 
            "Felaket yok edildi! Ödüller düştü!");
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
    
    /**
     * En yakın klan kristalini bul
     */
    public Location findNearestCrystal(Location from) {
        if (from == null || clanManager == null) return null;
        
        Location nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Clan clan : clanManager.getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            Location crystalLoc = clan.getCrystalLocation();
            if (crystalLoc == null) continue;
            
            // Aynı dünyada mı kontrol et
            if (!crystalLoc.getWorld().equals(from.getWorld())) continue;
            
            double distance = from.distance(crystalLoc);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = crystalLoc;
            }
        }
        
        return nearest;
    }
    
    /**
     * Felaket hedefini belirle (kristal veya merkez)
     */
    public void setDisasterTarget(Disaster disaster) {
        if (disaster == null || disaster.getCategory() != Disaster.Category.CREATURE) return;
        
        Entity entity = disaster.getEntity();
        if (entity == null && disaster.getGroupEntities().isEmpty()) return;
        
        Location currentLoc = null;
        if (entity != null) {
            currentLoc = entity.getLocation();
        } else if (!disaster.getGroupEntities().isEmpty()) {
            currentLoc = disaster.getGroupEntities().get(0).getLocation();
        }
        
        if (currentLoc == null) return;
        
        // Önce en yakın kristali bul
        Location nearestCrystal = findNearestCrystal(currentLoc);
        if (nearestCrystal != null) {
            disaster.setTargetCrystal(nearestCrystal);
            disaster.setTarget(nearestCrystal);
            return;
        }
        
        // Kristal yoksa merkeze git
        Location centerLoc = null;
        if (difficultyManager != null) {
            centerLoc = difficultyManager.getCenterLocation();
        }
        if (centerLoc == null) {
            centerLoc = currentLoc.getWorld().getSpawnLocation();
        }
        disaster.setTarget(centerLoc);
    }
    
    private java.util.Random random = new java.util.Random();
    
    /**
     * Grup felaket spawn (30 adet orta güçte) - Config kullanır
     */
    public void spawnGroupDisaster(org.bukkit.entity.EntityType entityType, int count, Location spawnLoc) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        World world = spawnLoc.getWorld();
        java.util.List<Entity> entities = new java.util.ArrayList<>();
        
        // EntityType'a göre felaket tipi belirle
        Disaster.Type disasterType = getDisasterTypeFromEntityType(entityType, true);
        int level = Disaster.getDefaultLevel(disasterType);
        DisasterPower power = calculateDisasterPower(level);
        
        // Config'den ayarları al
        me.mami.stratocraft.model.DisasterConfig config = null;
        if (configManager != null) {
            config = configManager.getConfig(disasterType, level);
        }
        if (config == null) {
            config = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        double spawnRadius = config.getSpawnRadius();
        
        // Spawn
        for (int i = 0; i < count; i++) {
            // Config'den spawn radius ile rastgele konum
            Location entityLoc = spawnLoc.clone().add(
                (random.nextDouble() - 0.5) * spawnRadius * 2,
                0,
                (random.nextDouble() - 0.5) * spawnRadius * 2
            );
            entityLoc.setY(world.getHighestBlockYAt(entityLoc) + 1);
            
            Entity entity = world.spawnEntity(entityLoc, entityType);
            
            // DisasterUtils ile güçlendirme
            me.mami.stratocraft.util.DisasterUtils.strengthenEntity(entity, config, power.multiplier);
            
            entities.add(entity);
        }
        
        // Disaster oluştur
        Location targetLoc = findNearestCrystal(spawnLoc);
        if (targetLoc == null) {
            if (difficultyManager != null) {
                targetLoc = difficultyManager.getCenterLocation();
            }
            if (targetLoc == null) {
                targetLoc = world.getSpawnLocation();
            }
        }
        
        Disaster disaster = new Disaster(
            disasterType,
            Disaster.Category.CREATURE,
            level,
            entities.isEmpty() ? null : entities.get(0),
            targetLoc,
            power.health,
            power.damage,
            Disaster.getDefaultDuration(disasterType, level)
        );
        
        // Grup entity'lerini ekle
        for (Entity e : entities) {
            disaster.addGroupEntity(e);
        }
        
        activeDisaster = disaster;
        setDisasterTarget(disaster);
        
        Bukkit.broadcastMessage("§c§l⚠ GRUP FELAKET BAŞLADI! ⚠");
        Bukkit.broadcastMessage("§4§l" + count + " adet güçlendirilmiş canavar spawn oldu!");
    }
    
    /**
     * EntityType'a göre felaket tipi belirle
     */
    private Disaster.Type getDisasterTypeFromEntityType(org.bukkit.entity.EntityType entityType, boolean isGroup) {
        if (isGroup) {
            // Grup felaketler
            switch (entityType) {
                case ZOMBIE: return Disaster.Type.ZOMBIE_HORDE;
                case SKELETON: return Disaster.Type.SKELETON_LEGION;
                case SPIDER: return Disaster.Type.SPIDER_SWARM;
                default: return Disaster.Type.ZOMBIE_HORDE;
            }
        } else {
            // Mini dalga felaketler
            switch (entityType) {
                case CREEPER: return Disaster.Type.CREEPER_SWARM;
                case ZOMBIE: return Disaster.Type.ZOMBIE_WAVE;
                default: return Disaster.Type.CREEPER_SWARM;
            }
        }
    }
    
    /**
     * Mini felaket dalgası spawn (100-500 adet) - Config kullanır
     */
    public void spawnSwarmDisaster(org.bukkit.entity.EntityType entityType, int count, Location spawnLoc) {
        if (activeDisaster != null && !activeDisaster.isDead()) {
            Bukkit.broadcastMessage("§cZaten aktif bir felaket var!");
            return;
        }
        
        World world = spawnLoc.getWorld();
        java.util.List<Entity> entities = new java.util.ArrayList<>();
        
        // EntityType'a göre felaket tipi belirle
        Disaster.Type disasterType = getDisasterTypeFromEntityType(entityType, false);
        int level = Disaster.getDefaultLevel(disasterType);
        DisasterPower power = calculateDisasterPower(level);
        
        // Config'den ayarları al
        me.mami.stratocraft.model.DisasterConfig config = null;
        if (configManager != null) {
            config = configManager.getConfig(disasterType, level);
        }
        if (config == null) {
            config = new me.mami.stratocraft.model.DisasterConfig();
        }
        
        double spawnRadius = config.getSpawnRadius();
        double healthPercentage = config.getHealthPercentage(); // Mini dalga için %20 = 0.2
        
        // Performans kontrolü - max 500
        int actualCount = Math.min(count, 500);
        
        // Spawn (config'den spawn radius ile)
        for (int i = 0; i < actualCount; i++) {
            // Config'den spawn radius ile rastgele konum
            Location entityLoc = spawnLoc.clone().add(
                (random.nextDouble() - 0.5) * spawnRadius * 2,
                0,
                (random.nextDouble() - 0.5) * spawnRadius * 2
            );
            entityLoc.setY(world.getHighestBlockYAt(entityLoc) + 1);
            
            Entity entity = world.spawnEntity(entityLoc, entityType);
            
            // DisasterUtils ile güçlendirme (healthPercentage ile)
            me.mami.stratocraft.util.DisasterUtils.strengthenEntity(entity, config, power.multiplier * healthPercentage);
            
            entities.add(entity);
        }
        
        // Disaster oluştur
        Location targetLoc = findNearestCrystal(spawnLoc);
        if (targetLoc == null) {
            if (difficultyManager != null) {
                targetLoc = difficultyManager.getCenterLocation();
            }
            if (targetLoc == null) {
                targetLoc = world.getSpawnLocation();
            }
        }
        
        Disaster disaster = new Disaster(
            disasterType,
            Disaster.Category.CREATURE,
            level,
            entities.isEmpty() ? null : entities.get(0),
            targetLoc,
            power.health * healthPercentage,
            power.damage * healthPercentage,
            Disaster.getDefaultDuration(disasterType, level)
        );
        
        // Grup entity'lerini ekle
        for (Entity e : entities) {
            disaster.addGroupEntity(e);
        }
        
        activeDisaster = disaster;
        setDisasterTarget(disaster);
        
        Bukkit.broadcastMessage("§c§l⚠ MİNİ FELAKET DALGASI BAŞLADI! ⚠");
        Bukkit.broadcastMessage("§4§l" + actualCount + " adet mini canavar spawn oldu!");
    }
}
