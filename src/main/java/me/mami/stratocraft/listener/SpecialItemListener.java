package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.SpecialItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Özel Eşyalar Dinleyicisi
 * - Kanca sistemi (Paslı Kanca, Titan Kancası)
 * - Casusluk Dürbünü
 */
public class SpecialItemListener implements Listener {
    private final SpecialItemManager specialItemManager;

    public SpecialItemListener(SpecialItemManager specialItemManager) {
        this.specialItemManager = specialItemManager;
    }

    // ========== KANCA SİSTEMİ ==========

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        ItemStack rod = event.getPlayer().getInventory().getItemInMainHand();

        // Özel kanca kontrolü
        if (ItemManager.isCustomItem(rod, "RUSTY_HOOK") ||
                ItemManager.isCustomItem(rod, "TITAN_GRAPPLE")) {
            specialItemManager.handleGrapple(event, rod);
        }
    }

    // ========== CASUSLUK DÜRBÜNÜ ==========

    @EventHandler
    public void onSpyglassUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Dürbün kontrolü
        if (item == null || item.getType() != org.bukkit.Material.SPYGLASS) {
            return;
        }

        // Ray trace ile hedef oyuncuyu bul
        org.bukkit.util.RayTraceResult result = player.rayTraceEntities(50);
        specialItemManager.handleSpyglass(player, result);
    }

    // NOT: Casusluk Dürbünü için PlayerMoveEvent kullanılmıyor
    // Bunun yerine Main.java'da başlatılan bir Scheduler task kullanılıyor
    // Bu sayede oyuncu durup kafasını çevirdiğinde de dürbün çalışır

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Oyuncu çıkışında casusluk verilerini temizle
        specialItemManager.clearSpyData(event.getPlayer());
    }

    // ========== SAVAŞ YELPAZESİ ==========
    private final Map<UUID, Long> fanCooldowns = new HashMap<>();
    private static final long FAN_COOLDOWN = 3000; // 3 saniye

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR)
            return;

        // Savaş Yelpazesi (War Fan)
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                ItemManager.isCustomItem(item, "WAR_FAN")) {

            event.setCancelled(true);

            // Cooldown kontrolü
            if (fanCooldowns.containsKey(player.getUniqueId())) {
                long timeLeft = (fanCooldowns.get(player.getUniqueId()) + FAN_COOLDOWN) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    return;
                }
            }

            // Yelpaze efekti - GÜÇLENDİRİLMİŞ
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.5f);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 1, 0), 50, 2.0, 1.0, 2.0, 0.2);
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 10, 1.5, 0.5, 1.5,
                    0.1);

            // Önündeki entityleri GÜÇLÜ şekilde it
            Vector direction = player.getLocation().getDirection().normalize();
            for (Entity entity : player.getNearbyEntities(8, 8, 8)) { // 4->8 blok (2x daha geniş)
                if (entity instanceof LivingEntity && entity != player) {
                    // Oyuncunun baktığı yönde mi?
                    Vector toEntity = entity.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (toEntity.normalize().dot(direction) > 0.5) {
                        // GÜÇLÜ itme (1.5->2.5) ve yukarı fırlatma (0.5->1.0)
                        entity.setVelocity(direction.multiply(2.5).setY(1.0));
                        // GÜÇLÜ hasar (2.0->5.0)
                        ((LivingEntity) entity).damage(5.0, player);

                        // Vurma efekti
                        entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation(), 10);
                    }
                }
            }

            fanCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
}
