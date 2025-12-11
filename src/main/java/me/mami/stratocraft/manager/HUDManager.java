package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.Mission;
import me.mami.stratocraft.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Sağ Üst Köşe Bilgi Barı (HUD) Yöneticisi
 * 
 * Gösterilen Bilgiler:
 * - Felaket Sayacı
 * - Aktif Batarya Bilgisi
 * - Alışveriş Teklif Bildirimleri
 * - Aktif Görev İlerlemesi
 * - Aktif Kontratlar
 * - Aktif Buff'lar
 */
public class HUDManager {
    private final Main plugin;
    
    // Manager referansları
    private DisasterManager disasterManager;
    private NewBatteryManager batteryManager;
    private ShopManager shopManager;
    private MissionManager missionManager;
    private ContractManager contractManager;
    private BuffManager buffManager;
    private ClanManager clanManager;
    private TerritoryManager territoryManager;
    
    // Scoreboard sistemi
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();
    private BukkitTask updateTask;
    
    // Teklif bildirimi takibi (son 30 saniye içinde yeni teklif var mı?)
    private final Map<UUID, Long> lastShopOfferTime = new HashMap<>();
    
    public HUDManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Manager referanslarını ayarla
     */
    public void setManagers(DisasterManager dm, NewBatteryManager bm, ShopManager sm, 
                           MissionManager mm, ContractManager cm, BuffManager bfm,
                           ClanManager cm2, TerritoryManager tm) {
        this.disasterManager = dm;
        this.batteryManager = bm;
        this.shopManager = sm;
        this.missionManager = mm;
        this.contractManager = cm;
        this.buffManager = bfm;
        this.clanManager = cm2;
        this.territoryManager = tm;
    }
    
    /**
     * HUD sistemini başlat
     */
    public void start() {
        // Her saniye güncelle
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateHUD(player);
            }
        }, 0L, 20L); // Her saniye (20 tick)
    }
    
    /**
     * HUD sistemini durdur
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // Tüm scoreboard'ları temizle
        for (Player player : Bukkit.getOnlinePlayers()) {
            clearHUD(player);
        }
        playerScoreboards.clear();
        playerObjectives.clear();
    }
    
    /**
     * Oyuncu için HUD'u güncelle
     */
    private void updateHUD(Player player) {
        List<HUDLine> lines = collectHUDInfo(player);
        
        if (lines.isEmpty()) {
            clearHUD(player);
            return;
        }
        
        // Scoreboard oluştur veya al
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            playerScoreboards.put(player.getUniqueId(), scoreboard);
        }
        
        // Objective oluştur veya al
        Objective objective = playerObjectives.get(player.getUniqueId());
        if (objective == null) {
            objective = scoreboard.registerNewObjective("hud_info", "dummy", "§e§l📊 BİLGİ PANELİ");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            playerObjectives.put(player.getUniqueId(), objective);
        }
        
        // Tüm entry'leri temizle
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        
        // Yeni bilgileri ekle (yukarıdan aşağıya)
        int score = lines.size();
        for (HUDLine line : lines) {
            String entryKey = getUniqueEntry(score);
            Team team = scoreboard.getTeam("team" + score);
            if (team == null) {
                team = scoreboard.registerNewTeam("team" + score);
            }
            team.setPrefix(line.getText());
            if (!team.hasEntry(entryKey)) {
                team.addEntry(entryKey);
            }
            objective.getScore(entryKey).setScore(score);
            score--;
        }
        
        player.setScoreboard(scoreboard);
    }
    
    /**
     * Oyuncu için HUD bilgilerini topla
     */
    private List<HUDLine> collectHUDInfo(Player player) {
        List<HUDLine> lines = new ArrayList<>();
        
        // 1. Aktif Felaket Bilgisi (varsa öncelikli)
        List<HUDLine> activeDisasterInfo = getActiveDisasterInfo(player);
        if (activeDisasterInfo != null && !activeDisasterInfo.isEmpty()) {
            lines.addAll(activeDisasterInfo);
            lines.add(new HUDLine("§7")); // Boş satır
        } else {
            // Aktif felaket yoksa countdown göster
            HUDLine disaster = getDisasterCountdown();
            if (disaster != null) {
                lines.add(disaster);
                String[] countdownInfo = disasterManager.getCountdownInfo();
                if (countdownInfo != null && countdownInfo.length > 1) {
                    lines.add(new HUDLine("§7Kalan: §e" + countdownInfo[1]));
                }
                lines.add(new HUDLine("§7")); // Boş satır
            }
        }
        
        // 2. Aktif Batarya (varsa)
        HUDLine battery = getBatteryInfo(player);
        if (battery != null) {
            lines.add(battery);
            // Batarya adını ikinci satırda göster
            int currentSlot = player.getInventory().getHeldItemSlot();
            if (batteryManager != null && batteryManager.hasLoadedBattery(player, currentSlot)) {
                NewBatteryManager.NewBatteryData data = batteryManager.getLoadedBattery(player, currentSlot);
                if (data != null) {
                    String batteryName = data.getBatteryName();
                    // İsim çok uzunsa kısalt
                    if (batteryName.length() > 25) {
                        batteryName = batteryName.substring(0, 22) + "...";
                    }
                    lines.add(new HUDLine("§7" + batteryName));
                }
            }
            lines.add(new HUDLine("§7")); // Boş satır
        }
        
        // 3. Alışveriş Teklifleri (varsa)
        HUDLine shop = getShopOfferInfo(player);
        if (shop != null) {
            lines.add(shop);
            lines.add(new HUDLine("§7")); // Boş satır
        }
        
        // 4. Görev (varsa)
        HUDLine mission = getMissionInfo(player);
        if (mission != null) {
            lines.add(mission);
            lines.add(new HUDLine("§7")); // Boş satır
        }
        
        // 5. Kontratlar (varsa)
        HUDLine contract = getContractInfo(player);
        if (contract != null) {
            lines.add(contract);
            lines.add(new HUDLine("§7")); // Boş satır
        }
        
        // 6. Buff'lar (varsa)
        HUDLine buff = getBuffInfo(player);
        if (buff != null) {
            lines.add(buff);
        }
        
        // 7. ✅ GÜÇ BİLGİSİ (her zaman göster)
        HUDLine power = getPowerInfo(player);
        if (power != null) {
            lines.add(new HUDLine("§7")); // Boş satır
            lines.add(power);
        }
        
        // 8. ✅ KİŞİSEL TERMİNAL KONTROLÜ (item yoksa bilgilendirme)
        HUDLine terminalHint = getPersonalTerminalHint(player);
        if (terminalHint != null) {
            lines.add(new HUDLine("§7")); // Boş satır
            lines.add(terminalHint);
            HUDLine recipeHint = getPersonalTerminalRecipeHint(player);
            if (recipeHint != null) {
                lines.add(recipeHint);
            }
        }
        
        return lines;
    }
    
    // ✅ PERFORMANS: Güç bilgisi cache (her saniye hesaplama yerine)
    // ✅ THREAD-SAFETY: ConcurrentHashMap kullan (main thread'de çalışsa da güvenli)
    private final java.util.Map<java.util.UUID, me.mami.stratocraft.model.PlayerPowerProfile> powerCache = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<java.util.UUID, Long> powerCacheTime = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final long POWER_CACHE_DURATION = 5000L; // 5 saniye cache
    
    /**
     * Oyuncu güç bilgisi (HUD için)
     * ✅ PERFORMANS: Cache kullanarak her saniye hesaplama yapmıyor
     */
    private HUDLine getPowerInfo(Player player) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin == null) return null;
        
        me.mami.stratocraft.manager.StratocraftPowerSystem powerSystem = plugin.getStratocraftPowerSystem();
        if (powerSystem == null) return null;
        
        // Cache kontrolü
        java.util.UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        me.mami.stratocraft.model.PlayerPowerProfile profile = powerCache.get(playerId);
        Long cacheTime = powerCacheTime.get(playerId);
        
        if (profile == null || cacheTime == null || now - cacheTime > POWER_CACHE_DURATION) {
            // Cache'den al veya hesapla
            profile = powerSystem.calculatePlayerProfile(player);
            powerCache.put(playerId, profile);
            powerCacheTime.put(playerId, now);
        }
        
        // Güç bilgisi: SGP ve Seviye
        double totalSGP = profile.getTotalSGP();
        int level = profile.getPlayerLevel();
        
        // Format: "💪 Güç: 1234.56 SGP (Seviye 5)"
        String powerText = "§e💪 Güç: §f" + String.format("%.0f", totalSGP) + " SGP §7(Seviye " + level + ")";
        
        return new HUDLine(powerText);
    }
    
    /**
     * Aktif felaket bilgisi (HUD için)
     */
    private List<HUDLine> getActiveDisasterInfo(Player player) {
        if (disasterManager == null) return null;
        
        Disaster activeDisaster = disasterManager.getActiveDisaster();
        if (activeDisaster == null || activeDisaster.isDead()) {
            return null;
        }
        
        List<HUDLine> lines = new ArrayList<>();
        String disasterName = disasterManager.getDisasterDisplayName(activeDisaster.getType());
        String category = activeDisaster.getCategory() == Disaster.Category.CREATURE ? "Canlı" : 
                         activeDisaster.getCategory() == Disaster.Category.NATURAL ? "Doğa" : "Mini";
        
        // Başlık
        lines.add(new HUDLine("§c§l⚠ AKTİF FELAKET"));
        
        // İsim ve kategori
        lines.add(new HUDLine("§4" + disasterName + " §7(" + category + ")"));
        
        // Canlı felaketler için can bilgisi
        if (activeDisaster.getCategory() == Disaster.Category.CREATURE) {
            double health = activeDisaster.getCurrentHealth();
            double maxHealth = activeDisaster.getMaxHealth();
            double healthPercent = maxHealth > 0 ? (health / maxHealth) * 100 : 0;
            String healthColor = healthPercent > 60 ? "§c" : healthPercent > 30 ? "§e" : "§a";
            lines.add(new HUDLine("§7Can: " + healthColor + String.format("%.0f/%.0f", health, maxHealth) + 
                " §7(" + String.format("%.0f%%", healthPercent) + ")"));
            
            // Felaket konum bilgisi (oyuncuya göre)
            String locationInfo = getDisasterLocationInfo(activeDisaster, player);
            if (locationInfo != null) {
                lines.add(new HUDLine(locationInfo));
            }
        }
        
        // Kalan süre
        long remainingTime = activeDisaster.getRemainingTime();
        String timeText = formatTime(remainingTime);
        lines.add(new HUDLine("§7Kalan Süre: §e" + timeText));
        
        return lines;
    }
    
    /**
     * Felaket konum bilgisi (oyuncuya göre mesafe ve yön)
     */
    private String getDisasterLocationInfo(Disaster disaster, Player player) {
        if (disaster == null || player == null) return null;
        
        // Canlı felaketler için entity konumu
        org.bukkit.Location disasterLoc = null;
        if (disaster.getCategory() == Disaster.Category.CREATURE) {
            if (disaster.getEntity() != null && !disaster.getEntity().isDead()) {
                disasterLoc = disaster.getEntity().getLocation();
            } else if (disaster.getGroupEntities() != null && !disaster.getGroupEntities().isEmpty()) {
                // Grup felaketler için ilk entity'nin konumu
                for (org.bukkit.entity.Entity entity : disaster.getGroupEntities()) {
                    if (entity != null && !entity.isDead() && entity.isValid()) {
                        disasterLoc = entity.getLocation();
                        break;
                    }
                }
            }
        }
        
        if (disasterLoc == null) return null;
        
        org.bukkit.Location playerLoc = player.getLocation();
        if (!disasterLoc.getWorld().equals(playerLoc.getWorld())) {
            return "§7Konum: §cFarklı Dünya";
        }
        
        // Mesafe hesapla
        double distance = playerLoc.distance(disasterLoc);
        String distanceText;
        if (distance >= 1000) {
            distanceText = String.format("%.1f km", distance / 1000.0);
        } else {
            distanceText = String.format("%.0f m", distance);
        }
        
        // Yön hesapla (8 yön: N, NE, E, SE, S, SW, W, NW)
        String direction = calculateDirection(playerLoc, disasterLoc);
        
        return "§7Konum: §e" + direction + " §7(" + distanceText + ")";
    }
    
    /**
     * İki konum arasındaki yönü hesapla (8 yön)
     */
    private String calculateDirection(org.bukkit.Location from, org.bukkit.Location to) {
        if (from == null || to == null) return "?";
        if (!from.getWorld().equals(to.getWorld())) return "?";
        
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        // Yaw açısını hesapla (0-360 derece)
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        if (angle < 0) {
            angle += 360;
        }
        
        // 8 yön: N, NE, E, SE, S, SW, W, NW
        // Her yön 45 derece aralığında
        if (angle >= 337.5 || angle < 22.5) {
            return "§bKuzey";
        } else if (angle >= 22.5 && angle < 67.5) {
            return "§bKuzey-Doğu";
        } else if (angle >= 67.5 && angle < 112.5) {
            return "§eDoğu";
        } else if (angle >= 112.5 && angle < 157.5) {
            return "§aGüney-Doğu";
        } else if (angle >= 157.5 && angle < 202.5) {
            return "§aGüney";
        } else if (angle >= 202.5 && angle < 247.5) {
            return "§6Güney-Batı";
        } else if (angle >= 247.5 && angle < 292.5) {
            return "§6Batı";
        } else {
            return "§bKuzey-Batı";
        }
    }
    
    /**
     * Felaket sayacı bilgisi
     */
    private HUDLine getDisasterCountdown() {
        if (disasterManager == null) return null;
        
        String[] countdownInfo = disasterManager.getCountdownInfo();
        if (countdownInfo == null) return null;
        
        return new HUDLine("§e⏰ Sonraki: §6" + countdownInfo[0]);
    }
    
    /**
     * Aktif batarya bilgisi
     */
    private HUDLine getBatteryInfo(Player player) {
        if (batteryManager == null) return null;
        
        int currentSlot = player.getInventory().getHeldItemSlot();
        if (batteryManager.hasLoadedBattery(player, currentSlot)) {
            NewBatteryManager.NewBatteryData data = batteryManager.getLoadedBattery(player, currentSlot);
            if (data != null) {
                String batteryName = data.getBatteryName();
                // İsim çok uzunsa kısalt
                if (batteryName.length() > 20) {
                    batteryName = batteryName.substring(0, 17) + "...";
                }
                return new HUDLine("§e⚡ Batarya: §6Slot " + (currentSlot + 1));
            }
        }
        return null;
    }
    
    /**
     * Alışveriş teklif bilgisi
     */
    private HUDLine getShopOfferInfo(Player player) {
        if (shopManager == null) return null;
        
        int newOfferCount = 0;
        long currentTime = System.currentTimeMillis();
        long thirtySecondsAgo = currentTime - 30000; // 30 saniye
        
        // Oyuncunun sahip olduğu mağazaları kontrol et
        for (Shop shop : shopManager.getAllShops()) {
            if (shop.getOwnerId().equals(player.getUniqueId())) {
                for (Shop.Offer offer : shop.getOffers()) {
                    if (!offer.isAccepted() && !offer.isRejected()) {
                        // Son 30 saniye içinde gelen teklifler
                        if (offer.getOfferTime() > thirtySecondsAgo) {
                            newOfferCount++;
                        }
                    }
                }
            }
        }
        
        if (newOfferCount > 0) {
            return new HUDLine("§e💰 Teklif: §6" + newOfferCount + " yeni");
        }
        
        return null;
    }
    
    /**
     * Görev bilgisi
     */
    private HUDLine getMissionInfo(Player player) {
        if (missionManager == null) return null;
        
        Mission mission = missionManager.getActiveMission(player.getUniqueId());
        if (mission == null || mission.isCompleted() || mission.isExpired()) {
            return null;
        }
        
        String progressText;
        if (mission.getType() == Mission.Type.TRAVEL_DISTANCE) {
            int progress = (int) mission.getTravelProgress();
            int target = mission.getTargetDistance();
            progressText = "§a" + progress + "§7/§a" + target;
        } else {
            progressText = "§a" + mission.getProgress() + "§7/§a" + mission.getTargetAmount();
        }
        
        String missionType = getMissionTypeName(mission.getType());
        return new HUDLine("§e📋 Görev: " + progressText + " §7" + missionType);
    }
    
    /**
     * Kontrat bilgisi
     */
    private HUDLine getContractInfo(Player player) {
        if (contractManager == null) return null;
        
        List<Contract> contracts = contractManager.getPlayerContracts(player.getUniqueId());
        if (contracts.isEmpty()) {
            // Bounty kontratı var mı? (başında ödül)
            Contract bounty = contractManager.getBountyContract(player.getUniqueId());
            if (bounty != null) {
                return new HUDLine("§c⚠ Bounty: §6" + (int)bounty.getReward() + " altın");
            }
            return null;
        }
        
        // Bounty kontratı var mı?
        Contract bounty = contractManager.getBountyContract(player.getUniqueId());
        if (bounty != null) {
            return new HUDLine("§e📜 Kontrat: §6" + contracts.size() + " §7| §cBounty: §6" + (int)bounty.getReward());
        }
        
        return new HUDLine("§e📜 Kontrat: §6" + contracts.size() + " aktif");
    }
    
    /**
     * Buff bilgisi
     */
    private HUDLine getBuffInfo(Player player) {
        if (buffManager == null || clanManager == null) return null;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return null;
        
        // Fatih Buff'ı kontrol et
        Long conquerorEnd = buffManager.getConquerorBuffEnd(clan.getId());
        if (conquerorEnd != null && conquerorEnd > System.currentTimeMillis()) {
            long remaining = conquerorEnd - System.currentTimeMillis();
            String timeText = formatTime(remaining);
            return new HUDLine("§6⚡ Buff: §eFatih §7(" + timeText + ")");
        }
        
        // Kahraman Buff'ı kontrol et
        Long heroEnd = buffManager.getHeroBuffEnd(clan.getId());
        if (heroEnd != null && heroEnd > System.currentTimeMillis()) {
            long remaining = heroEnd - System.currentTimeMillis();
            String timeText = formatTime(remaining);
            return new HUDLine("§b⚡ Buff: §eKahraman §7(" + timeText + ")");
        }
        
        return null;
    }
    
    /**
     * Kişisel Terminal bilgilendirmesi (item yoksa göster)
     */
    private HUDLine getPersonalTerminalHint(Player player) {
        // Envanterde Personal Terminal var mı kontrol et
        if (hasPersonalTerminal(player)) {
            return null; // Item varsa bilgilendirme gösterme
        }
        
        // Item yoksa bilgilendirme göster
        return new HUDLine("§e⚠ Kişisel Yönetim Terminali yapmanız gerekiyor!");
    }
    
    /**
     * Oyuncunun envanterinde Personal Terminal var mı?
     */
    private boolean hasPersonalTerminal(Player player) {
        if (player == null) return false;
        
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && me.mami.stratocraft.manager.ItemManager.isCustomItem(item, "PERSONAL_TERMINAL")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Kişisel Terminal tarif bilgisi (ikinci satır)
     */
    private HUDLine getPersonalTerminalRecipeHint(Player player) {
        if (hasPersonalTerminal(player)) {
            return null;
        }
        return new HUDLine("§7Tarif: §e8x Kağıt + 1x Kırmızı Taş");
    }
    
    /**
     * Görev tipi ismini al
     */
    private String getMissionTypeName(Mission.Type type) {
        switch (type) {
            case KILL_MOB: return "Mob Öldür";
            case GATHER_ITEM: return "Malzeme Topla";
            case VISIT_LOCATION: return "Lokasyon Ziyaret";
            case BUILD_STRUCTURE: return "Yapı İnşa";
            case KILL_PLAYER: return "Oyuncu Öldür";
            case CRAFT_ITEM: return "Item Craft";
            case MINE_BLOCK: return "Blok Kaz";
            case TRAVEL_DISTANCE: return "Mesafe Kat Et";
            default: return "Bilinmeyen";
        }
    }
    
    /**
     * Zaman formatla (ms -> hh:mm:ss veya dd:hh:mm:ss)
     */
    private String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (days > 0) {
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Benzersiz entry anahtarı oluştur
     */
    private String getUniqueEntry(int score) {
        // Scoreboard entry'leri için benzersiz anahtar
        // Minecraft'ta entry'ler 40 karakter sınırı var
        return "§" + score + "§r";
    }
    
    /**
     * Oyuncu için HUD'u temizle
     */
    private void clearHUD(Player player) {
        Scoreboard scoreboard = playerScoreboards.get(player.getUniqueId());
        if (scoreboard != null) {
            Objective objective = playerObjectives.get(player.getUniqueId());
            if (objective != null) {
                objective.unregister();
            }
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            playerScoreboards.remove(player.getUniqueId());
            playerObjectives.remove(player.getUniqueId());
        }
    }
    
    /**
     * Oyuncu giriş yaptığında HUD'u başlat
     */
    public void onPlayerJoin(Player player) {
        // HUD otomatik güncellenecek
    }
    
    /**
     * Oyuncu çıkış yaptığında HUD'u temizle
     */
    public void onPlayerQuit(Player player) {
        clearHUD(player);
        lastShopOfferTime.remove(player.getUniqueId());
        
        // ✅ PERFORMANS: Cache'leri temizle
        if (player != null) {
            java.util.UUID playerId = player.getUniqueId();
            powerCache.remove(playerId);
            powerCacheTime.remove(playerId);
        }
    }
    
    /**
     * HUD satırı sınıfı
     */
    private static class HUDLine {
        private final String text;
        
        public HUDLine(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
    }
}

