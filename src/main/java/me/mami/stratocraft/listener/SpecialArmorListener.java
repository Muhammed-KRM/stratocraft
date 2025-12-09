package me.mami.stratocraft.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.mami.stratocraft.Main;

/**
 * Özel zırh güçlerini yöneten listener
 */
public class SpecialArmorListener implements Listener {
    private final Main plugin;
    private final Map<UUID, Long> lastRegenTime = new HashMap<>();
    private final Map<UUID, Integer> doubleJumpCount = new HashMap<>();
    private final Map<UUID, Long> lastJumpTime = new HashMap<>();
    
    public SpecialArmorListener(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Zırh seviyesini kontrol et
     */
    private int getArmorLevel(Player player) {
        PlayerInventory inv = player.getInventory();
        int maxLevel = 0;
        
        ItemStack[] armor = {inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()};
        for (ItemStack item : armor) {
            if (item != null && item.hasItemMeta()) {
                Integer level = item.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(Main.getInstance(), "armor_level"), PersistentDataType.INTEGER);
                if (level != null && level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        
        return maxLevel;
    }
    
    /**
     * Özel zırh ID'sini kontrol et
     */
    private String getSpecialArmorId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey(Main.getInstance(), "special_armor_id"), PersistentDataType.STRING);
    }
    
    /**
     * Seviye 2: Diken Etkisi - Saldırıya uğradığında saldırana hasar ver
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        int armorLevel = getArmorLevel(player);
        
        if (armorLevel >= 2) {
            // Diken etkisi - saldırana hasar ver - Config'den
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = plugin.getConfigManager() != null ? 
                    plugin.getConfigManager().getGameBalanceConfig() : null;
                double thornMultiplier = balanceConfig != null ? balanceConfig.getArmorLevel2ThornDamageMultiplier() : 0.3;
                double thornDamage = event.getFinalDamage() * thornMultiplier;
                attacker.damage(thornDamage);
                attacker.sendMessage("§cDiken etkisi! " + String.format("%.1f", thornDamage) + " hasar aldın!");
            }
        }
    }
    
    /**
     * Seviye 3: 2x Hız, Yüksek Zıplama, Aşırı Koruma
     * Seviye 4: Sürekli Can Yenileme
     * Seviye 5: Uçma Gücü
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        int armorLevel = getArmorLevel(player);
        
        if (armorLevel >= 3) {
            // 2x Hız - Config'den
            me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = plugin.getConfigManager() != null ? 
                plugin.getConfigManager().getGameBalanceConfig() : null;
            int speedDuration = balanceConfig != null ? balanceConfig.getArmorLevel3SpeedDuration() : 100;
            int speedLevel = balanceConfig != null ? balanceConfig.getArmorLevel3SpeedLevel() : 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration, speedLevel, false, false));
            
            // Yüksek Zıplama - Config'den
            int jumpDuration = balanceConfig != null ? balanceConfig.getArmorLevel3JumpDuration() : 100;
            int jumpLevel = balanceConfig != null ? balanceConfig.getArmorLevel3JumpLevel() : 2;
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, jumpDuration, jumpLevel, false, false));
        }
        
        if (armorLevel >= 4) {
            // Sürekli Can Yenileme - Config'den
            me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = plugin.getConfigManager() != null ? 
                plugin.getConfigManager().getGameBalanceConfig() : null;
            long regenInterval = balanceConfig != null ? balanceConfig.getArmorLevel4RegenInterval() : 1000L;
            double regenAmount = balanceConfig != null ? balanceConfig.getArmorLevel4RegenAmount() : 1.0;
            long currentTime = System.currentTimeMillis();
            Long lastRegen = lastRegenTime.get(player.getUniqueId());
            
            if (lastRegen == null || currentTime - lastRegen >= regenInterval) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.setHealth(Math.min(player.getHealth() + regenAmount, player.getMaxHealth()));
                    lastRegenTime.put(player.getUniqueId(), currentTime);
                }
            }
        }
        
        // Seviye 5 uçma kontrolü aşağıda
        if (armorLevel >= 5) {
            // Çift zıplama kontrolü - yerde değilse ve yukarı hareket ediyorsa
            if (!player.isOnGround() && event.getTo().getY() > event.getFrom().getY()) {
                long currentTime = System.currentTimeMillis();
                Long lastJump = lastJumpTime.get(player.getUniqueId());
                Integer jumpCount = doubleJumpCount.get(player.getUniqueId());
                
                // İlk zıplama tespiti (yerden ayrılma) - Config'den
                me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = plugin.getConfigManager() != null ? 
                    plugin.getConfigManager().getGameBalanceConfig() : null;
                long cooldown1 = balanceConfig != null ? balanceConfig.getArmorLevel5DoubleJumpCooldown1() : 1000L;
                if (lastJump == null || (currentTime - lastJump > cooldown1 && player.getVelocity().getY() > 0.3)) {
                    doubleJumpCount.put(player.getUniqueId(), 1);
                    lastJumpTime.put(player.getUniqueId(), currentTime);
                } 
                // İkinci zıplama (havadayken tekrar zıplama) - Config'den
                else if (jumpCount != null && jumpCount == 1) {
                    long cooldown2 = balanceConfig != null ? balanceConfig.getArmorLevel5DoubleJumpCooldown2() : 500L;
                    if (currentTime - lastJump < cooldown2) {
                        // Çift zıplama tespit edildi - uçma aktif
                        doubleJumpCount.put(player.getUniqueId(), 2);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        player.sendMessage("§dUçma gücü aktif!");
                        
                        // Config'den süre sonra uçmayı kapat
                        long flightDuration = balanceConfig != null ? balanceConfig.getArmorLevel5FlightDuration() : 100L;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    player.setFlying(false);
                                    player.setAllowFlight(false);
                                    doubleJumpCount.remove(player.getUniqueId());
                                    lastJumpTime.remove(player.getUniqueId());
                                }
                            }
                        }.runTaskLater(plugin, flightDuration);
                    }
                }
            }
            
            // Yere indiğinde sıfırla
            if (player.isOnGround()) {
                doubleJumpCount.remove(player.getUniqueId());
                lastJumpTime.remove(player.getUniqueId());
            }
        }
    }
}

