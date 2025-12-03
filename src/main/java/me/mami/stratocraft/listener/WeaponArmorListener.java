package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Seviyeli silah ve zırh sistemi için listener
 * Hasar artırımı ve zırh koruması sağlar
 */
public class WeaponArmorListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        
        // Seviyeli silah kontrolü
        if (ItemManager.isLeveledWeapon(weapon)) {
            int level = ItemManager.getWeaponLevel(weapon);
            double bonusDamage = getWeaponBonusDamage(level);
            
            // Hasar artır
            double currentDamage = event.getDamage();
            event.setDamage(currentDamage + bonusDamage);
            
            // Kritik vuruş efekti (seviye 3+)
            if (level >= 3 && Math.random() < 0.15) {
                event.setDamage(event.getDamage() * 1.5); // %50 ekstra hasar
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    target.getWorld().spawnParticle(
                        org.bukkit.Particle.CRIT_MAGIC,
                        target.getLocation().add(0, 1, 0),
                        20,
                        0.5, 1, 0.5,
                        0.1
                    );
                    target.getWorld().playSound(
                        target.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT,
                        1.0f,
                        1.5f
                    );
                }
            }
        }
        
        // Hedef oyuncu ise zırh koruması kontrol et
        if (event.getEntity() instanceof Player) {
            Player defender = (Player) event.getEntity();
            PlayerInventory inv = defender.getInventory();
            
            double totalArmorBonus = 0;
            
            // Her zırh parçasını kontrol et
            ItemStack helmet = inv.getHelmet();
            if (helmet != null && ItemManager.isLeveledArmor(helmet)) {
                totalArmorBonus += getArmorBonus(ItemManager.getArmorLevel(helmet));
            }
            
            ItemStack chestplate = inv.getChestplate();
            if (chestplate != null && ItemManager.isLeveledArmor(chestplate)) {
                totalArmorBonus += getArmorBonus(ItemManager.getArmorLevel(chestplate));
            }
            
            ItemStack leggings = inv.getLeggings();
            if (leggings != null && ItemManager.isLeveledArmor(leggings)) {
                totalArmorBonus += getArmorBonus(ItemManager.getArmorLevel(leggings));
            }
            
            ItemStack boots = inv.getBoots();
            if (boots != null && ItemManager.isLeveledArmor(boots)) {
                totalArmorBonus += getArmorBonus(ItemManager.getArmorLevel(boots));
            }
            
            // Zırh bonusu uygula (hasarı azalt)
            if (totalArmorBonus > 0) {
                double currentDamage = event.getDamage();
                // Her 1 zırh puanı %4 hasar azaltır (Minecraft'ın standart formülü)
                double reduction = currentDamage * (totalArmorBonus * 0.04);
                event.setDamage(Math.max(0, currentDamage - reduction));
            }
        }
    }
    
    /**
     * Silah seviyesine göre bonus hasar hesapla
     */
    private double getWeaponBonusDamage(int level) {
        switch (level) {
            case 1: return 2.0;  // Demir: +2 hasar
            case 2: return 5.0;  // Elmas: +5 hasar
            case 3: return 10.0; // Netherite: +10 hasar
            case 4: return 20.0; // Titanyum: +20 hasar
            case 5: return 35.0; // Efsanevi: +35 hasar
            default: return 0.0;
        }
    }
    
    /**
     * Zırh seviyesine göre bonus zırh puanı hesapla
     */
    private double getArmorBonus(int level) {
        switch (level) {
            case 1: return 1.0;  // Demir: +1 zırh
            case 2: return 2.0;  // Elmas: +2 zırh
            case 3: return 3.0;  // Netherite: +3 zırh
            case 4: return 5.0;  // Titanyum: +5 zırh
            case 5: return 8.0;  // Efsanevi: +8 zırh
            default: return 0.0;
        }
    }
}


