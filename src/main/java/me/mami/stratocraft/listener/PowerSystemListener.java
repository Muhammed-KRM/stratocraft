package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.PlayerPowerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectAddEvent;
import org.bukkit.potion.PotionEffectRemoveEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Power System Event Listener
 * 
 * Güç sistemi için event'leri dinler:
 * - PlayerQuitEvent: Offline cache'e kaydetme
 * - PotionEffectAddEvent/RemoveEvent: Buff cache güncelleme
 * - BlockPlaceEvent/BlockBreakEvent: Delta sistemi (ritüel blok tracking)
 */
public class PowerSystemListener implements Listener {
    private final StratocraftPowerSystem powerSystem;
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    private final me.mami.stratocraft.Main plugin;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    
    public PowerSystemListener(StratocraftPowerSystem powerSystem, 
                              ClanManager clanManager,
                              TerritoryManager territoryManager) {
        this.powerSystem = powerSystem;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
        this.plugin = me.mami.stratocraft.Main.getInstance();
        // ✅ CONFIG: GameBalanceConfig'i al
        if (plugin != null && plugin.getConfigManager() != null) {
            this.balanceConfig = plugin.getConfigManager().getGameBalanceConfig();
        }
    }
    
    /**
     * Oyuncu girişinde adını seviyeye göre güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            // Oyuncu adını güncelle (seviye ve renk)
            updatePlayerName(player);
        }
    }
    
    /**
     * Oyuncu çıkışında offline cache'e kaydet
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            powerSystem.onPlayerQuit(player);
            
            // ✅ GÜÇ GEÇMİŞİ: Oyuncu çıkışında temizle
            if (plugin != null && plugin.getSimplePowerHistory() != null) {
                plugin.getSimplePowerHistory().onPlayerQuit(player.getUniqueId());
            }
            
            // Team'i temizle (performans için)
            clearPlayerTeam(player);
        }
    }
    
    /**
     * PotionEffect eklendiğinde buff cache'i güncelle ve adı güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectAdd(PotionEffectAddEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            powerSystem.updateBuffPowerCache(player);
            // ✅ Oyuncu adını güncelle (güç değişti)
            updatePlayerName(player);
        }
    }
    
    /**
     * PotionEffect kaldırıldığında buff cache'i güncelle ve adı güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectRemove(PotionEffectRemoveEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            powerSystem.updateBuffPowerCache(player);
            // ✅ Oyuncu adını güncelle (güç değişti)
            updatePlayerName(player);
        }
    }
    
    // ✅ PERFORMANS: Slot değişikliği cache (çok sık çağrılıyor)
    private final java.util.Map<java.util.UUID, Integer> lastSlotCache = 
        new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<java.util.UUID, Long> lastSlotUpdateTime = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * Slot update cooldown'u al (config'den)
     */
    private long getSlotUpdateCooldown() {
        return balanceConfig != null ? balanceConfig.getPowerSystemSlotUpdateCooldown() : 500L;
    }
    
    /**
     * Silah değiştiğinde (slot değiştiğinde) oyuncu adını güncelle
     * ✅ PERFORMANS: Cooldown ile optimize edildi
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        int newSlot = event.getNewSlot();
        int oldSlot = lastSlotCache.getOrDefault(player.getUniqueId(), -1);
        long now = System.currentTimeMillis();
        Long lastUpdate = lastSlotUpdateTime.get(player.getUniqueId());
        
        // Aynı slot'a geçiş veya çok sık güncelleme varsa atla
        if (newSlot == oldSlot) return;
        long cooldown = getSlotUpdateCooldown();
        if (lastUpdate != null && now - lastUpdate < cooldown) return;
        
        // Cache güncelle
        lastSlotCache.put(player.getUniqueId(), newSlot);
        lastSlotUpdateTime.put(player.getUniqueId(), now);
        
        // ✅ Oyuncu adını güncelle (silah değişti, güç değişebilir)
        updatePlayerName(player);
    }
    
    /**
     * Envanter değiştiğinde (zırh takıldı/çıkarıldığında) oyuncu adını güncelle
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            // Sadece zırh slotları değiştiğinde güncelle (performans için)
            int slot = event.getSlot();
            if (slot >= 36 && slot <= 39) { // Zırh slotları (36-39)
                // ✅ Oyuncu adını güncelle (zırh değişti, güç değişebilir)
                // Gecikme ile güncelle (envanter kapanmadan önce)
                if (plugin != null) {
                    Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> updatePlayerName(player),
                        1L
                    );
                }
            }
        }
    }
    
    /**
     * Blok koyulduğunda ritüel blok tracking (Delta sistemi)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material material = block.getType();
        
        // Ritüel blok mu?
        if (!powerSystem.isRitualBlock(material)) return;
        
        // Klan arazisi içinde mi?
        Clan clan = territoryManager != null ? 
            territoryManager.getTerritoryOwner(loc) : null;
        
        if (clan == null) {
            // Oyuncunun klanı var mı? (kendi arazisinde değilse)
            clan = clanManager != null ? 
                clanManager.getClanByPlayer(player.getUniqueId()) : null;
        }
        
        if (clan != null) {
            // Delta sistemi: Blok koyuldu
            powerSystem.onRitualBlockPlace(clan, loc, material);
        }
    }
    
    /**
     * Blok kırıldığında ritüel blok tracking (Delta sistemi)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material material = block.getType();
        
        // Ritüel blok mu?
        if (!powerSystem.isRitualBlock(material)) return;
        
        // Klan arazisi içinde mi?
        Clan clan = territoryManager != null ? 
            territoryManager.getTerritoryOwner(loc) : null;
        
        if (clan == null) {
            // Oyuncunun klanı var mı?
            clan = clanManager != null ? 
                clanManager.getClanByPlayer(player.getUniqueId()) : null;
        }
        
        if (clan != null) {
            // Delta sistemi: Blok kırıldı
            powerSystem.onRitualBlockBreak(clan, loc, material);
        }
    }
    
    // ========== OYUNCU ADI GÜNCELLEME SİSTEMİ ==========
    
    /**
     * Oyuncu adını seviyeye göre güncelle (renk + seviye gösterimi)
     */
    public void updatePlayerName(Player player) {
        if (player == null) return;
        
        // Güç profili al
        PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(player);
        int level = profile.getPlayerLevel();
        
        // Renk belirle (seviyeye göre)
        String color = getLevelColor(level);
        
        // Format: "[Seviye] Renkliİsim"
        String playerName = player.getName();
        String formattedName = color + "[" + level + "] " + playerName;
        
        // Tab list'te görünen isim
        player.setPlayerListName(formattedName);
        
        // Scoreboard Team sistemi (daha esnek)
        updatePlayerTeam(player, level, color, playerName);
    }
    
    /**
     * Seviyeye göre renk belirle
     */
    private String getLevelColor(int level) {
        if (level >= 19) {
            return "§5"; // Efsanevi (Koyu Mor)
        } else if (level >= 16) {
            return "§c"; // Kırmızı
        } else if (level >= 13) {
            return "§6"; // Altın
        } else if (level >= 10) {
            return "§d"; // Mor
        } else if (level >= 7) {
            return "§b"; // Mavi
        } else if (level >= 4) {
            return "§a"; // Yeşil
        } else {
            return "§7"; // Gri
        }
    }
    
    /**
     * Scoreboard Team ile oyuncu adını güncelle
     */
    private void updatePlayerTeam(Player player, int level, String color, String playerName) {
        // Ana scoreboard'u al
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        
        // Team adı (oyuncu UUID'sine göre benzersiz)
        String teamName = "lvl_" + player.getUniqueId().toString().substring(0, 13);
        
        // Mevcut team'i al veya oluştur
        Team team = mainScoreboard.getTeam(teamName);
        if (team == null) {
            team = mainScoreboard.registerNewTeam(teamName);
        }
        
        // Prefix: "[Seviye] "
        team.setPrefix(color + "[" + level + "] ");
        
        // Suffix: boş (isteğe bağlı eklenebilir)
        team.setSuffix("");
        
        // Oyuncuyu team'e ekle (zaten ekliyse hata vermez)
        if (!team.hasEntry(playerName)) {
            team.addEntry(playerName);
        }
    }
    
    /**
     * Oyuncu team'ini temizle
     */
    private void clearPlayerTeam(Player player) {
        if (player == null) return;
        
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "lvl_" + player.getUniqueId().toString().substring(0, 13);
        
        Team team = mainScoreboard.getTeam(teamName);
        if (team != null) {
            team.removeEntry(player.getName());
            if (team.getEntries().isEmpty()) {
                team.unregister();
            }
        }
    }
    
    /**
     * Tüm online oyuncuların adlarını güncelle (güç değiştiğinde çağrılabilir)
     */
    public void updateAllPlayerNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerName(player);
        }
    }
}

