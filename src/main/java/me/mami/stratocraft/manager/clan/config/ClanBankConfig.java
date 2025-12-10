package me.mami.stratocraft.manager.clan.config;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Klan Bankası Config
 * Tüm banka ayarları config'den okunur
 */
public class ClanBankConfig {
    // Çekme yetkileri
    private boolean canGeneralWithdraw = true;
    private boolean canEliteWithdraw = true;
    private boolean canMemberWithdraw = false;
    
    // Otomatik maaş sistemi
    private long salaryInterval = 86400000L; // 24 saat (ms)
    
    // Rütbe bazlı maaş itemleri
    private Map<Clan.Rank, Map<Material, Integer>> salaryItems = new HashMap<>();
    
    /**
     * Config'den yükle
     */
    public void loadFromConfig(FileConfiguration config) {
        if (config == null) {
            loadDefaults();
            return;
        }
        
        String path = "clan.bank-system.";
        
        // Çekme yetkileri
        canGeneralWithdraw = config.getBoolean(path + "withdraw.general", true);
        canEliteWithdraw = config.getBoolean(path + "withdraw.elite", true);
        canMemberWithdraw = config.getBoolean(path + "withdraw.member", false);
        
        // Maaş aralığı
        salaryInterval = config.getLong(path + "salary.interval", 86400000L);
        
        // Rütbe bazlı maaş itemleri
        loadSalaryItems(config, path + "salary.items.");
        
        // Geçersiz değer kontrolleri
        if (salaryInterval < 0) {
            salaryInterval = 86400000L; // Varsayılan
        }
    }
    
    /**
     * Varsayılan değerleri yükle
     */
    private void loadDefaults() {
        canGeneralWithdraw = true;
        canEliteWithdraw = true;
        canMemberWithdraw = false;
        salaryInterval = 86400000L;
        
        // Varsayılan maaş itemleri
        Map<Material, Integer> leaderSalary = new HashMap<>();
        leaderSalary.put(Material.DIAMOND, 10);
        leaderSalary.put(Material.GOLD_INGOT, 50);
        salaryItems.put(Clan.Rank.LEADER, leaderSalary);
        
        Map<Material, Integer> generalSalary = new HashMap<>();
        generalSalary.put(Material.DIAMOND, 5);
        generalSalary.put(Material.GOLD_INGOT, 25);
        salaryItems.put(Clan.Rank.GENERAL, generalSalary);
        
        Map<Material, Integer> eliteSalary = new HashMap<>();
        eliteSalary.put(Material.GOLD_INGOT, 15);
        salaryItems.put(Clan.Rank.ELITE, eliteSalary);
        
        Map<Material, Integer> memberSalary = new HashMap<>();
        memberSalary.put(Material.IRON_INGOT, 10);
        salaryItems.put(Clan.Rank.MEMBER, memberSalary);
        
        // Recruit maaş almaz
    }
    
    /**
     * Maaş itemlerini config'den yükle
     */
    private void loadSalaryItems(FileConfiguration config, String path) {
        // Leader maaşı
        Map<Material, Integer> leaderSalary = new HashMap<>();
        if (config.contains(path + "leader")) {
            for (String key : config.getConfigurationSection(path + "leader").getKeys(false)) {
                Material material = Material.getMaterial(key.toUpperCase());
                if (material != null) {
                    int amount = config.getInt(path + "leader." + key, 0);
                    if (amount > 0) {
                        leaderSalary.put(material, amount);
                    }
                }
            }
        }
        if (!leaderSalary.isEmpty()) {
            salaryItems.put(Clan.Rank.LEADER, leaderSalary);
        }
        
        // General maaşı
        Map<Material, Integer> generalSalary = new HashMap<>();
        if (config.contains(path + "general")) {
            for (String key : config.getConfigurationSection(path + "general").getKeys(false)) {
                Material material = Material.getMaterial(key.toUpperCase());
                if (material != null) {
                    int amount = config.getInt(path + "general." + key, 0);
                    if (amount > 0) {
                        generalSalary.put(material, amount);
                    }
                }
            }
        }
        if (!generalSalary.isEmpty()) {
            salaryItems.put(Clan.Rank.GENERAL, generalSalary);
        }
        
        // Elite maaşı
        Map<Material, Integer> eliteSalary = new HashMap<>();
        if (config.contains(path + "elite")) {
            for (String key : config.getConfigurationSection(path + "elite").getKeys(false)) {
                Material material = Material.getMaterial(key.toUpperCase());
                if (material != null) {
                    int amount = config.getInt(path + "elite." + key, 0);
                    if (amount > 0) {
                        eliteSalary.put(material, amount);
                    }
                }
            }
        }
        if (!eliteSalary.isEmpty()) {
            salaryItems.put(Clan.Rank.ELITE, eliteSalary);
        }
        
        // Member maaşı
        Map<Material, Integer> memberSalary = new HashMap<>();
        if (config.contains(path + "member")) {
            for (String key : config.getConfigurationSection(path + "member").getKeys(false)) {
                Material material = Material.getMaterial(key.toUpperCase());
                if (material != null) {
                    int amount = config.getInt(path + "member." + key, 0);
                    if (amount > 0) {
                        memberSalary.put(material, amount);
                    }
                }
            }
        }
        if (!memberSalary.isEmpty()) {
            salaryItems.put(Clan.Rank.MEMBER, memberSalary);
        }
    }
    
    // Getters
    public boolean canGeneralWithdraw() { return canGeneralWithdraw; }
    public boolean canEliteWithdraw() { return canEliteWithdraw; }
    public boolean canMemberWithdraw() { return canMemberWithdraw; }
    public long getSalaryInterval() { return salaryInterval; }
    
    /**
     * Rütbe bazlı maaş itemleri
     */
    public Map<Material, Integer> getSalaryItems(Clan.Rank rank) {
        if (rank == null) return null;
        return salaryItems.get(rank);
    }
}

