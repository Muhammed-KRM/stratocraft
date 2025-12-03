package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
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
    private final Main plugin;
    
    public ConsumableListener(Main plugin) {
        this.plugin = plugin;
    }

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
            return; // Event iptal edildi, devam etme
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
            return; // Event iptal edildi, devam etme
        }
        
        // Yaşam İksiri - Canı fulleyen
        if (ItemManager.isCustomItem(item, "LIFE_ELIXIR")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Canı fulle
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setSaturation(20.0f);
            p.sendMessage("§a§lYaşam İksiri tüketildi! Can ve açlık ful doldu!");
            
            // Efekt
            p.getWorld().spawnParticle(org.bukkit.Particle.HEART, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            return;
        }
        
        // Güç Meyvesi - Hasarı 5 kat arttıran (30 saniye)
        if (ItemManager.isCustomItem(item, "POWER_FRUIT")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Hasar artışı (5 kat = 400% artış)
            AttributeModifier damageMod = new AttributeModifier(
                UUID.randomUUID(),
                "power_fruit_damage",
                4.0, // 5 kat = 1 + 4 = 5x
                AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                EquipmentSlot.HAND
            );
            
            p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(damageMod);
            p.sendMessage("§c§lGüç Meyvesi tüketildi! 30 saniye boyunca hasarın 5 katına çıktı!");
            
            // 30 saniye sonra kaldır
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(damageMod);
                p.sendMessage("§7Güç Meyvesi etkisi bitti.");
            }, 600L); // 30 saniye = 600 tick
            
            // Efekt
            p.getWorld().spawnParticle(org.bukkit.Particle.CRIT_MAGIC, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
            return;
        }
        
        // Hız İksiri - Hızı arttıran (2 dakika)
        if (ItemManager.isCustomItem(item, "SPEED_ELIXIR")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Hız artışı (Level 2 Speed = %40 hız)
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED,
                4800, // 2 dakika = 2400 tick
                1 // Level 2
            ));
            p.sendMessage("§b§lHız İksiri tüketildi! 2 dakika boyunca hızın arttı!");
            
            // Efekt
            p.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_HORSE_GALLOP, 1.0f, 1.5f);
            return;
        }
        
        // Yenilenme İksiri - Hızlı can yenileme (1 dakika)
        if (ItemManager.isCustomItem(item, "REGENERATION_ELIXIR")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Hızlı can yenileme (Level 2 Regeneration)
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.REGENERATION,
                2400, // 1 dakika = 1200 tick
                1 // Level 2
            ));
            p.sendMessage("§d§lYenilenme İksiri tüketildi! 1 dakika boyunca hızlı can yenileme aktif!");
            
            // Efekt
            p.getWorld().spawnParticle(org.bukkit.Particle.HEART, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            return;
        }
        
        // Güç İksiri - Güç artışı (2 dakika)
        if (ItemManager.isCustomItem(item, "STRENGTH_ELIXIR")) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            
            // Güç artışı (Level 2 Strength = +6 hasar)
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE,
                4800, // 2 dakika = 2400 tick
                1 // Level 2
            ));
            p.sendMessage("§6§lGüç İksiri tüketildi! 2 dakika boyunca gücün arttı!");
            
            // Efekt
            p.getWorld().spawnParticle(org.bukkit.Particle.CRIT, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 0.8f);
            return;
        }
    }
}

