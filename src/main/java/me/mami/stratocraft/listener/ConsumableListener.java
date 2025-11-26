package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ConsumableListener implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getItemMeta() == null) return;
        
        // Cehennem Meyvesi kontrolü
        if (ItemManager.isCustomItem(item, "HELL_FRUIT")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Kalıcı can artışı (+2 maksimum can)
            AttributeModifier healthMod = new AttributeModifier(
                UUID.randomUUID(),
                "hell_fruit_health",
                2.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthMod);
            p.setHealth(Math.min(p.getHealth() + 2, p.getMaxHealth()));
            p.sendMessage("§c§lCehennem Meyvesi tüketildi! +2 kalıcı can kazandın!");
            
            // Ateş direnci ver (5 dakika)
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE,
                6000, // 5 dakika
                0
            ));
        }
        
        // Kızıl Elmas Otu kontrolü (eğer ItemManager'da tanımlıysa)
        if (ItemManager.isCustomItem(item, "RED_DIAMOND_HERB")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Kalıcı hasar artışı (+%10 saldırı hasarı)
            AttributeModifier damageMod = new AttributeModifier(
                UUID.randomUUID(),
                "red_diamond_herb_damage",
                0.1,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                EquipmentSlot.HAND
            );
            
            p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(damageMod);
            p.sendMessage("§c§lKızıl Elmas Otu tüketildi! +%10 kalıcı hasar kazandın!");
        }
    }
}

