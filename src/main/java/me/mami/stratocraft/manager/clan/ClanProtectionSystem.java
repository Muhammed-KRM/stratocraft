package me.mami.stratocraft.manager.clan;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.manager.clan.config.ClanProtectionConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.PlayerPowerProfile;
import org.bukkit.entity.Player;

/**
 * Gelişmiş Klan Koruma Sistemi
 * 
 * Hibrit koruma sistemi: Güç + Seviye + Aktivite
 * Tüm koruma kuralları config'den kontrol edilir
 * 
 * Özellikler:
 * - Klan savaşı istisnası (en yüksek öncelik)
 * - Güç bazlı koruma (%40 eşik)
 * - Seviye bazlı koruma (5 seviye farkı)
 * - Acemi koruması (3,000 güç + Seviye 5 altı)
 * - Aktivite bazlı koruma (7 gün offline)
 * - Klan içi koruma (%50 eşik)
 */
public class ClanProtectionSystem {
    private final Main plugin;
    private final ClanManager clanManager;
    private final StratocraftPowerSystem powerSystem;
    private final SiegeManager siegeManager;
    private final ClanActivitySystem activitySystem;
    private ClanProtectionConfig config;
    
    public ClanProtectionSystem(Main plugin, ClanManager clanManager, 
                               StratocraftPowerSystem powerSystem,
                               SiegeManager siegeManager,
                               ClanActivitySystem activitySystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.powerSystem = powerSystem;
        this.siegeManager = siegeManager;
        this.activitySystem = activitySystem;
        this.config = new ClanProtectionConfig();
    }
    
    /**
     * Config yükle
     */
    public void loadConfig(org.bukkit.configuration.file.FileConfiguration fileConfig) {
        config.loadFromConfig(fileConfig);
    }
    
    /**
     * Oyuncu saldırı yapabilir mi? (Tüm koruma kuralları)
     * 
     * Öncelik Sırası:
     * 1. Klan savaşı istisnası (en yüksek öncelik)
     * 2. Güç bazlı koruma (%40 eşik)
     * 3. Seviye bazlı koruma (5 seviye farkı)
     * 4. Acemi koruması (3,000 güç + Seviye 5 altı)
     * 5. Aktivite bazlı koruma (7 gün offline)
     * 6. Klan içi koruma (%50 eşik)
     */
    public boolean canAttackPlayer(Player attacker, Player target) {
        if (attacker == null || target == null || attacker.equals(target)) {
            return false;
        }
        
        // 1. Klan savaşı kontrolü (en yüksek öncelik)
        if (isClanAtWar(attacker, target)) {
            return true; // Savaşta herkes herkese saldırabilir
        }
        
        // Güç ve seviye hesapla (null check + cache kullanımı)
        if (powerSystem == null) {
            plugin.getLogger().warning("PowerSystem null! Koruma kontrolü yapılamıyor.");
            return false;
        }
        
        PlayerPowerProfile attackerProfile = powerSystem.calculatePlayerProfile(attacker);
        PlayerPowerProfile targetProfile = powerSystem.calculatePlayerProfile(target);
        
        // Null check
        if (attackerProfile == null || targetProfile == null) {
            plugin.getLogger().warning("Güç profili null! Oyuncu: " + 
                (attacker != null ? attacker.getName() : "null") + 
                " / " + (target != null ? target.getName() : "null"));
            return false;
        }
        
        double attackerPower = attackerProfile.getTotalSGP();
        double targetPower = targetProfile.getTotalSGP();
        int attackerLevel = attackerProfile.getPlayerLevel();
        int targetLevel = targetProfile.getPlayerLevel();
        
        // Güç değerleri geçerli mi kontrol et
        if (attackerPower < 0 || targetPower < 0 || attackerLevel < 0 || targetLevel < 0) {
            plugin.getLogger().warning("Geçersiz güç/seviye değerleri! Saldıran: " + attackerPower + 
                "/" + attackerLevel + ", Hedef: " + targetPower + "/" + targetLevel);
            return false;
        }
        
        // 2. Güç bazlı koruma (%40 eşik)
        if (!checkPowerProtection(attacker, attackerPower, targetPower)) {
            return false;
        }
        
        // 3. Seviye bazlı koruma (5 seviye farkı)
        if (!checkLevelProtection(attacker, attackerLevel, targetLevel)) {
            return false;
        }
        
        // 4. Acemi koruması (3,000 güç + Seviye 5 altı)
        if (!checkRookieProtection(attacker, attackerPower, targetPower, targetLevel)) {
            return false;
        }
        
        // 5. Aktivite bazlı koruma (7 gün offline)
        if (!checkActivityProtection(attacker, target)) {
            return false;
        }
        
        // 6. Klan içi koruma (%50 eşik)
        if (!checkClanInternalProtection(attacker, target, attackerPower, targetPower)) {
            return false;
        }
        
        return true; // Tüm kontroller geçti
    }
    
    /**
     * Güç bazlı koruma kontrolü
     */
    private boolean checkPowerProtection(Player attacker, double attackerPower, double targetPower) {
        // Geçersiz değer kontrolü
        if (attackerPower < 0 || targetPower < 0) {
            return false;
        }
        
        // Config null check
        if (config == null) {
            plugin.getLogger().warning("ClanProtectionConfig null! Koruma kontrolü yapılamıyor.");
            return true; // Güvenli tarafta kal
        }
        
        double powerThreshold = attackerPower * config.getPowerThreshold();
        if (targetPower < powerThreshold) {
            if (attacker != null) {
                attacker.sendMessage("§cBu oyuncu senin dengin değil! (Güç: " + 
                    String.format("%.0f", targetPower) + " < " + 
                    String.format("%.0f", powerThreshold) + ")");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Seviye bazlı koruma kontrolü
     */
    private boolean checkLevelProtection(Player attacker, int attackerLevel, int targetLevel) {
        // Geçersiz değer kontrolü
        if (attackerLevel < 0 || targetLevel < 0) {
            return false;
        }
        
        // Config null check
        if (config == null) {
            return true; // Güvenli tarafta kal
        }
        
        int levelDiff = attackerLevel - targetLevel;
        int maxLevelDiff = config.getMaxLevelDiff();
        if (levelDiff > maxLevelDiff) {
            if (attacker != null) {
                attacker.sendMessage("§cSeviye farkı çok büyük! (Sen: " + attackerLevel + 
                    ", Hedef: " + targetLevel + ", Fark: " + levelDiff + ")");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Acemi koruması kontrolü
     */
    private boolean checkRookieProtection(Player attacker, double attackerPower, 
                                         double targetPower, int targetLevel) {
        // Geçersiz değer kontrolü
        if (attackerPower < 0 || targetPower < 0 || targetLevel < 0) {
            return false;
        }
        
        // Config null check
        if (config == null) {
            return true; // Güvenli tarafta kal
        }
        
        double rookiePowerThreshold = config.getRookiePowerThreshold();
        int rookieLevelThreshold = config.getRookieLevelThreshold();
        double strongPlayerThreshold = config.getStrongPlayerThreshold();
        
        if (targetPower < rookiePowerThreshold && 
            targetLevel < rookieLevelThreshold &&
            attackerPower > strongPlayerThreshold) {
            if (attacker != null) {
                attacker.sendMessage("§cBu oyuncu çok yeni! Onurlu bir savaş değil.");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Aktivite bazlı koruma kontrolü
     */
    private boolean checkActivityProtection(Player attacker, Player target) {
        if (activitySystem == null) return true;
        if (target == null) return true;
        
        // Config null check
        if (config == null) {
            return true; // Güvenli tarafta kal
        }
        
        long lastActivity = activitySystem.getLastActivity(target.getUniqueId());
        long inactiveThreshold = config.getInactiveThreshold();
        
        // Geçersiz zaman kontrolü
        if (lastActivity < 0 || inactiveThreshold < 0) {
            return true; // Güvenli tarafta kal
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActivity > inactiveThreshold) {
            if (attacker != null) {
                attacker.sendMessage("§cBu oyuncu uzun süredir offline! Saldırı yapılamaz.");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Klan içi koruma kontrolü (eski metod - kullanılmıyor, silinebilir)
     * @deprecated Bu metod kullanılmıyor, target parametreli versiyonu kullanılmalı
     */
    @Deprecated
    private boolean checkClanInternalProtection(Player attacker, double attackerPower, double targetPower) {
        // Bu metod kullanılmıyor, target parametreli versiyonu kullanılmalı
        return true;
    }
    
    /**
     * Klan içi koruma kontrolü (target parametreli)
     */
    private boolean checkClanInternalProtection(Player attacker, Player target, 
                                                double attackerPower, double targetPower) {
        // Null check
        if (clanManager == null || attacker == null || target == null) {
            return true; // Güvenli tarafta kal
        }
        
        // Geçersiz değer kontrolü
        if (attackerPower < 0 || targetPower < 0) {
            return false;
        }
        
        // Config null check
        if (config == null) {
            return true; // Güvenli tarafta kal
        }
        
        Clan attackerClan = clanManager.getClanByPlayer(attacker.getUniqueId());
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        
        if (attackerClan != null && attackerClan.equals(targetClan)) {
            double clanThreshold = attackerPower * config.getClanThreshold();
            if (targetPower < clanThreshold) {
                if (attacker != null) {
                    attacker.sendMessage("§cKlan içinde güçsüz üyelere saldıramazsın!");
                }
                return false;
            }
        }
        return true;
    }
    
    /**
     * İki klan savaşta mı?
     */
    private boolean isClanAtWar(Player attacker, Player target) {
        if (siegeManager == null) return false;
        
        Clan attackerClan = clanManager.getClanByPlayer(attacker.getUniqueId());
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        
        if (attackerClan == null || targetClan == null) {
            return false;
        }
        
        // İki klan birbirine savaş açmış mı? (null check)
        try {
            Clan attackerOfAttacker = siegeManager.isUnderSiege(attackerClan) ? 
                siegeManager.getAttacker(attackerClan) : null;
            Clan attackerOfTarget = siegeManager.isUnderSiege(targetClan) ? 
                siegeManager.getAttacker(targetClan) : null;
            
            return (attackerOfAttacker != null && attackerOfAttacker.equals(targetClan)) ||
                   (attackerOfTarget != null && attackerOfTarget.equals(attackerClan));
        } catch (Exception e) {
            plugin.getLogger().warning("Klan savaşı kontrolü hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hasar azaltma hesapla (koruma aktifse)
     * 
     * Not: Bu metod sadece saldırı yapılabilir durumda çağrılmalı.
     * Eğer canAttackPlayer false dönerse, event zaten cancel edilmiş olmalı.
     * Bu metod sadece güç farkına göre hasar azaltma için kullanılır.
     */
    public double calculateDamageReduction(Player attacker, Player target) {
        // Null check
        if (attacker == null || target == null || attacker.equals(target)) {
            return 1.0; // Normal hasar
        }
        
        // Config null check
        if (config == null || powerSystem == null) {
            return 1.0; // Normal hasar
        }
        
        // Güç profillerini al
        PlayerPowerProfile attackerProfile = powerSystem.calculatePlayerProfile(attacker);
        PlayerPowerProfile targetProfile = powerSystem.calculatePlayerProfile(target);
        
        if (attackerProfile == null || targetProfile == null) {
            return 1.0; // Normal hasar
        }
        
        double attackerPower = attackerProfile.getTotalSGP();
        double targetPower = targetProfile.getTotalSGP();
        
        // Division by zero önleme
        if (attackerPower <= 0) {
            return 1.0; // Normal hasar
        }
        
        // Geçersiz değer kontrolü
        if (attackerPower < 0 || targetPower < 0) {
            return 1.0; // Normal hasar
        }
        
        double powerRatio = targetPower / attackerPower;
        double powerThreshold = config.getPowerThreshold();
        double clanThreshold = config.getClanThreshold();
        
        // Güç oranına göre hasar azaltma
        double minReduction = config.getDamageReductionMin();
        double maxReduction = config.getDamageReductionMax();
        
        // Geçersiz config değerleri kontrolü
        if (minReduction < 0 || maxReduction < 0 || minReduction > 1 || maxReduction > 1) {
            return 1.0; // Normal hasar
        }
        
        // Güç eşiğinin altındaysa minimum hasar
        if (powerRatio < powerThreshold) {
            return minReduction;
        }
        
        // Klan eşiğinin altındaysa kademeli azaltma
        if (powerRatio < clanThreshold) {
            // Division by zero önleme
            double thresholdDiff = clanThreshold - powerThreshold;
            if (thresholdDiff <= 0) {
                return minReduction;
            }
            
            double reduction = minReduction + 
                ((powerRatio - powerThreshold) / thresholdDiff) * 
                (maxReduction - minReduction);
            
            // Sınırları kontrol et
            return Math.max(minReduction, Math.min(maxReduction, reduction));
        }
        
        return 1.0; // Normal hasar
    }
    
    /**
     * Config getter
     */
    public ClanProtectionConfig getConfig() {
        return config;
    }
}

