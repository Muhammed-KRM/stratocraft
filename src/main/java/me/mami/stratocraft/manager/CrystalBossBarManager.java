package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * ✅ YENİ: Klan Kristali Can Göstergesi (BossBar)
 * 
 * Oyuncu klan kristaline yakınsa (50 blok içinde) BossBar gösterir
 * - Can yüzdesine göre renk değişir
 * - Zırh ve kalkan bilgisi gösterilir
 */
public class CrystalBossBarManager {
    private final Main plugin;
    private final Map<UUID, BossBar> playerBossBars = new HashMap<>(); // Player UUID -> BossBar
    private final Map<UUID, UUID> playerCrystalClanIds = new HashMap<>(); // Player UUID -> Clan ID (hangi klanın kristali gösteriliyor)
    private BukkitTask updateTask;
    private static final double BOSSBAR_DISTANCE = 50.0; // 50 blok yakınlık
    
    public CrystalBossBarManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * BossBar güncelleme task'ını başlat
     */
    public void start() {
        if (updateTask != null) return; // Zaten çalışıyor
        
        // Her 2 saniyede bir güncelle (performans için)
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllBossBars();
            }
        }.runTaskTimer(plugin, 0L, 40L); // 40 tick = 2 saniye
    }
    
    /**
     * BossBar güncelleme task'ını durdur
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // Tüm BossBar'ları kaldır
        for (BossBar bar : playerBossBars.values()) {
            if (bar != null) {
                bar.removeAll();
            }
        }
        playerBossBars.clear();
        playerCrystalClanIds.clear();
    }
    
    /**
     * Tüm oyuncular için BossBar'ları güncelle
     */
    private void updateAllBossBars() {
        if (plugin.getClanManager() == null || plugin.getTerritoryManager() == null) return;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            
            updateBossBar(player);
        }
    }
    
    /**
     * Oyuncu için BossBar'ı güncelle
     */
    private void updateBossBar(Player player) {
        if (plugin.getClanManager() == null || plugin.getTerritoryManager() == null) return;
        
        Location playerLoc = player.getLocation();
        
        // En yakın klan kristalini bul (50 blok içinde)
        Clan nearestCrystalClan = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Clan clan : plugin.getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            Location crystalLoc = clan.getCrystalLocation();
            if (crystalLoc == null || !crystalLoc.getWorld().equals(playerLoc.getWorld())) continue;
            
            double distance = playerLoc.distance(crystalLoc);
            if (distance <= BOSSBAR_DISTANCE && distance < nearestDistance) {
                nearestDistance = distance;
                nearestCrystalClan = clan;
            }
        }
        
        // Eğer yakında kristal yoksa, mevcut BossBar'ı kaldır
        if (nearestCrystalClan == null) {
            removeBossBar(player);
            return;
        }
        
        // Oyuncu kendi klanının kristaline yakınsa veya başka bir klanın kristaline yakınsa göster
        // (Savaş durumunda düşman kristali de gösterilebilir)
        UUID clanId = nearestCrystalClan.getId();
        UUID currentClanId = playerCrystalClanIds.get(player.getUniqueId());
        
        // Eğer farklı bir kristale yaklaştıysa, eski BossBar'ı kaldır
        if (currentClanId != null && !currentClanId.equals(clanId)) {
            removeBossBar(player);
        }
        
        // BossBar'ı oluştur veya güncelle
        BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar == null) {
            bar = createBossBar(player, nearestCrystalClan);
            if (bar != null) {
                playerBossBars.put(player.getUniqueId(), bar);
                playerCrystalClanIds.put(player.getUniqueId(), clanId);
            }
        } else {
            // Mevcut BossBar'ı güncelle
            updateBossBarContent(bar, nearestCrystalClan, player);
        }
    }
    
    /**
     * Yeni BossBar oluştur
     */
    private BossBar createBossBar(Player player, Clan clan) {
        if (clan == null || !clan.hasCrystal()) return null;
        
        double currentHealth = clan.getCrystalCurrentHealth();
        double maxHealth = clan.getCrystalMaxHealth();
        double healthPercent = Math.max(0.0, Math.min(1.0, currentHealth / maxHealth));
        
        // Renk belirleme
        BarColor color = getBarColor(healthPercent);
        
        // Başlık oluştur
        String title = buildBossBarTitle(clan, currentHealth, maxHealth);
        
        // BossBar oluştur
        BossBar bar = Bukkit.createBossBar(title, color, BarStyle.SEGMENTED_10);
        bar.setProgress(healthPercent);
        bar.addPlayer(player);
        bar.setVisible(true);
        
        return bar;
    }
    
    /**
     * BossBar içeriğini güncelle
     */
    private void updateBossBarContent(BossBar bar, Clan clan, Player player) {
        if (bar == null || clan == null || !clan.hasCrystal()) return;
        
        double currentHealth = clan.getCrystalCurrentHealth();
        double maxHealth = clan.getCrystalMaxHealth();
        double healthPercent = Math.max(0.0, Math.min(1.0, currentHealth / maxHealth));
        
        // Renk güncelle
        BarColor color = getBarColor(healthPercent);
        bar.setColor(color);
        
        // Başlık güncelle
        String title = buildBossBarTitle(clan, currentHealth, maxHealth);
        bar.setTitle(title);
        
        // Progress güncelle
        bar.setProgress(healthPercent);
        
        // Oyuncu eklenmiş mi kontrol et
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
    }
    
    /**
     * BossBar başlığı oluştur
     */
    private String buildBossBarTitle(Clan clan, double currentHealth, double maxHealth) {
        String clanName = clan.getName();
        double healthPercent = (currentHealth / maxHealth) * 100.0;
        
        // Renk belirleme (başlık için)
        String healthColor;
        if (healthPercent >= 75) {
            healthColor = "§a";
        } else if (healthPercent >= 50) {
            healthColor = "§e";
        } else if (healthPercent >= 25) {
            healthColor = "§6";
        } else {
            healthColor = "§c";
        }
        
        StringBuilder title = new StringBuilder();
        title.append("§6💎 ").append(clanName).append(" Kristali: ");
        title.append(healthColor).append(String.format("%.1f", currentHealth));
        title.append("§7/§f").append(String.format("%.1f", maxHealth));
        title.append(" §7(").append(String.format("%.0f", healthPercent)).append("%)");
        
        // Savunma sistemleri bilgisi
        if (clan.getCrystalDamageReduction() > 0 || clan.getCrystalShieldBlocks() > 0) {
            title.append(" §7|");
            if (clan.getCrystalDamageReduction() > 0) {
                double armorPercent = clan.getCrystalDamageReduction() * 100.0;
                title.append(" §bZırh:").append(String.format("%.0f", armorPercent)).append("%");
            }
            if (clan.getCrystalShieldBlocks() > 0) {
                title.append(" §dKalkan:").append(clan.getCrystalShieldBlocks());
            }
        }
        
        return title.toString();
    }
    
    /**
     * Can yüzdesine göre BossBar rengi
     */
    private BarColor getBarColor(double healthPercent) {
        if (healthPercent >= 0.75) {
            return BarColor.GREEN; // Yeşil (sağlıklı)
        } else if (healthPercent >= 0.50) {
            return BarColor.YELLOW; // Sarı (orta)
        } else if (healthPercent >= 0.25) {
            return BarColor.RED; // Kırmızı (düşük)
        } else {
            return BarColor.RED; // Kırmızı (kritik)
        }
    }
    
    /**
     * Oyuncu için BossBar'ı kaldır
     */
    private void removeBossBar(Player player) {
        if (player == null) return;
        
        BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
            bar.removeAll();
        }
        playerCrystalClanIds.remove(player.getUniqueId());
    }
    
    /**
     * Oyuncu çıkış yaptığında temizle
     */
    public void onPlayerQuit(Player player) {
        removeBossBar(player);
    }
}

