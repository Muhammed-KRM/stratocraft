package me.mami.stratocraft.manager;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * EconomyManager - Vault entegrasyonu için ekonomi yöneticisi
 * Vault yoksa kendi iç ekonomi sistemini kullanır
 */
public class EconomyManager {
    private Economy economy = null;
    private boolean vaultEnabled = false;
    
    public EconomyManager() {
        setupEconomy();
    }
    
    /**
     * Vault ekonomi sistemini başlat
     */
    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().warning("[Stratocraft] Vault bulunamadı! Kendi ekonomi sistemi kullanılacak.");
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().warning("[Stratocraft] Vault ekonomi servisi bulunamadı! Kendi ekonomi sistemi kullanılacak.");
            return false;
        }
        
        economy = rsp.getProvider();
        vaultEnabled = true;
        Bukkit.getLogger().info("[Stratocraft] Vault entegrasyonu başarıyla yüklendi!");
        return true;
    }
    
    /**
     * Vault aktif mi?
     */
    public boolean isVaultEnabled() {
        return vaultEnabled && economy != null;
    }
    
    /**
     * Oyuncunun bakiyesini al (Vault varsa Vault'tan, yoksa kendi sisteminden)
     */
    public double getBalance(org.bukkit.entity.Player player) {
        if (isVaultEnabled()) {
            return economy.getBalance(player);
        }
        // Kendi ekonomi sistemini kullan (Clan.getBalance() gibi)
        return 0.0; // Varsayılan - kendi sisteminizde implement edin
    }
    
    /**
     * Oyuncuya para ekle
     */
    public void depositPlayer(org.bukkit.entity.Player player, double amount) {
        if (isVaultEnabled()) {
            economy.depositPlayer(player, amount);
        } else {
            // Kendi ekonomi sisteminize ekleyin
            // Örnek: clanManager.getClanByPlayer(player.getUniqueId()).addBalance(amount);
        }
    }
    
    /**
     * Oyuncudan para çek
     */
    public void withdrawPlayer(org.bukkit.entity.Player player, double amount) {
        if (isVaultEnabled()) {
            economy.withdrawPlayer(player, amount);
        } else {
            // Kendi ekonomi sisteminizden çekin
            // Örnek: clanManager.getClanByPlayer(player.getUniqueId()).removeBalance(amount);
        }
    }
    
    /**
     * Oyuncunun yeterli parası var mı?
     */
    public boolean has(org.bukkit.entity.Player player, double amount) {
        if (isVaultEnabled()) {
            return economy.has(player, amount);
        }
        // Kendi ekonomi sisteminizi kontrol edin
        return getBalance(player) >= amount;
    }
    
    /**
     * Para formatını al (örn: "$1,000.00")
     */
    public String format(double amount) {
        if (isVaultEnabled()) {
            return economy.format(amount);
        }
        return String.format("%.2f", amount);
    }
    
    /**
     * Para birimi adını al (örn: "Dolar", "Altın")
     */
    public String currencyNamePlural() {
        if (isVaultEnabled()) {
            return economy.currencyNamePlural();
        }
        return "Altın";
    }
    
    /**
     * Vault Economy instance'ını al (gelişmiş kullanım için)
     */
    public Economy getEconomy() {
        return economy;
    }
}

