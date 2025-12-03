package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            // Diken etkisi - saldırana hasar ver
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                double thornDamage = event.getFinalDamage() * 0.3; // %30 geri hasar
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
            // 2x Hız
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
            
            // Yüksek Zıplama
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 2, false, false));
        }
        
        if (armorLevel >= 4) {
            // Sürekli Can Yenileme (her saniye)
            long currentTime = System.currentTimeMillis();
            Long lastRegen = lastRegenTime.get(player.getUniqueId());
            
            if (lastRegen == null || currentTime - lastRegen >= 1000) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.setHealth(Math.min(player.getHealth() + 1.0, player.getMaxHealth()));
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
                
                // İlk zıplama tespiti (yerden ayrılma)
                if (lastJump == null || (currentTime - lastJump > 1000 && player.getVelocity().getY() > 0.3)) {
                    doubleJumpCount.put(player.getUniqueId(), 1);
                    lastJumpTime.put(player.getUniqueId(), currentTime);
                } 
                // İkinci zıplama (havadayken tekrar zıplama)
                else if (jumpCount != null && jumpCount == 1 && currentTime - lastJump < 500) {
                    // Çift zıplama tespit edildi - uçma aktif
                    doubleJumpCount.put(player.getUniqueId(), 2);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage("§dUçma gücü aktif!");
                    
                    // 5 saniye sonra uçmayı kapat
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
                    }.runTaskLater(plugin, 100L); // 5 saniye
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

