package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.SpecialItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
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
                ItemManager.isCustomItem(rod, "GOLDEN_HOOK") ||
                ItemManager.isCustomItem(rod, "TITAN_GRAPPLE")) {
            specialItemManager.handleGrapple(event, rod);
        }
        
        // Olta hızını artır (tüm kancalar için)
        if (ItemManager.isCustomItem(rod, "RUSTY_HOOK") ||
                ItemManager.isCustomItem(rod, "GOLDEN_HOOK") ||
                ItemManager.isCustomItem(rod, "TITAN_GRAPPLE")) {
            
            org.bukkit.entity.FishHook hook = event.getHook();
            if (hook != null) {
                // Hook hızını artır - her tick'te güncelle
                if (event.getState() == PlayerFishEvent.State.FISHING) {
                    // Hook'un hızını artırmak için task başlat
                    accelerateHook(hook);
                }
            }
        }
    }
    
    // Aktif hook'ları takip et (performans için)
    private final Map<UUID, Integer> activeHooks = new HashMap<>();
    
    /**
     * Hook hızını artır (task ile sürekli güncelle)
     */
    private void accelerateHook(org.bukkit.entity.FishHook hook) {
        if (hook == null || !hook.isValid() || hook.isOnGround()) {
            activeHooks.remove(hook.getUniqueId());
            return;
        }
        
        // Hook'un mevcut hızını al
        Vector velocity = hook.getVelocity();
        
        // Eğer çok yavaşsa (düşüyorsa), hızını artır
        if (velocity.length() < 0.8) {
            // Yönü koruyarak hızı artır
            if (velocity.length() > 0.01) {
                velocity.normalize().multiply(1.8); // 1.8x hız
            } else {
                // Hız yoksa, aşağı doğru hızlandır
                velocity = new Vector(0, -0.5, 0);
            }
            hook.setVelocity(velocity);
        }
        
        // Maksimum 100 tick (5 saniye) sonra durdur
        int tickCount = activeHooks.getOrDefault(hook.getUniqueId(), 0);
        if (tickCount > 100) {
            activeHooks.remove(hook.getUniqueId());
            return;
        }
        
        activeHooks.put(hook.getUniqueId(), tickCount + 1);
        
        // Her tick'te güncelle
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            me.mami.stratocraft.Main.getInstance(),
            () -> accelerateHook(hook),
            1L // 1 tick sonra tekrar kontrol et
        );
    }
    
    /**
     * Düşme hasarını engelle (kanca kullanıldıktan sonra)
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Sadece düşme hasarını engelle
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            // Kanca koruması var mı kontrol et
            if (specialItemManager.hasFallDamageProtection(player)) {
                event.setCancelled(true);
                player.sendMessage("§aKanca koruması aktif! Düşme hasarı engellendi.");
            }
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

        // Dürbün kontrolü - Özel Casusluk Dürbünü olmalı
        if (item == null || item.getType() != org.bukkit.Material.SPYGLASS) {
            return;
        }
        
        // Özel Casusluk Dürbünü kontrolü
        if (!ItemManager.isCustomItem(item, "CASUSLUK_DURBUN")) {
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
    
    // ========== CASUSLUK DÜRBÜNÜ GUI MENÜSÜ ==========
    
    @EventHandler
    public void onSpyMenuClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getView().title() == null) return;
        
        String title = ((net.kyori.adventure.text.TextComponent)event.getView().title()).content();
        if (!title.contains("Casusluk:")) return;
        
        event.setCancelled(true); // Eşya çıkarılmasını engelle
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Tıklama sesi
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        
        // Hedef oyuncuyu bul (menü başlığından)
        String targetName = title.replace("§e§lCasusluk: §f", "").trim();
        Player target = org.bukkit.Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cHedef oyuncu artık online değil!");
            player.closeInventory();
            return;
        }
        
        // Buton tıklamaları
        switch (clicked.getType()) {
            case REDSTONE:
                // Can detayları
                player.sendMessage("§6§l═══════════════════════════");
                player.sendMessage("§c§lCAN DURUMU: §f" + target.getName());
                player.sendMessage("§7Mevcut: §c" + String.format("%.1f", target.getHealth()) + "❤");
                player.sendMessage("§7Maksimum: §c" + String.format("%.1f", 
                    target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) + "❤");
                double healthPercent = (target.getHealth() / target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) * 100;
                player.sendMessage("§7Yüzde: §c" + String.format("%.0f", healthPercent) + "%");
                player.sendMessage("§6§l═══════════════════════════");
                break;
                
            case BREAD:
                // Açlık detayları
                player.sendMessage("§6§l═══════════════════════════");
                player.sendMessage("§e§lAÇLIK DURUMU: §f" + target.getName());
                int foodLevel = target.getFoodLevel();
                double saturation = target.getSaturation();
                String hungerColor = foodLevel >= 18 ? "§a" : foodLevel >= 12 ? "§e" : foodLevel >= 6 ? "§6" : "§c";
                player.sendMessage("§7Seviye: " + hungerColor + foodLevel + "§7/20");
                player.sendMessage("§7Doygunluk: §e" + String.format("%.1f", saturation));
                player.sendMessage("§6§l═══════════════════════════");
                break;
                
            case IRON_CHESTPLATE:
                // Zırh detayları
                player.sendMessage("§6§l═══════════════════════════");
                player.sendMessage("§b§lZIRH DURUMU: §f" + target.getName());
                org.bukkit.inventory.PlayerInventory inv = target.getInventory();
                int armorPoints = 0;
                if (inv.getHelmet() != null) {
                    armorPoints += specialItemManager.getArmorPoints(inv.getHelmet().getType());
                    player.sendMessage("§7Miğfer: §b" + inv.getHelmet().getType().name());
                }
                if (inv.getChestplate() != null) {
                    armorPoints += specialItemManager.getArmorPoints(inv.getChestplate().getType());
                    player.sendMessage("§7Göğüslük: §b" + inv.getChestplate().getType().name());
                }
                if (inv.getLeggings() != null) {
                    armorPoints += specialItemManager.getArmorPoints(inv.getLeggings().getType());
                    player.sendMessage("§7Pantolon: §b" + inv.getLeggings().getType().name());
                }
                if (inv.getBoots() != null) {
                    armorPoints += specialItemManager.getArmorPoints(inv.getBoots().getType());
                    player.sendMessage("§7Bot: §b" + inv.getBoots().getType().name());
                }
                player.sendMessage("§7Toplam: §b" + armorPoints + "§7/20 puan");
                player.sendMessage("§6§l═══════════════════════════");
                break;
                
            case CHEST:
                // Envanter detayları
                player.sendMessage("§6§l═══════════════════════════");
                player.sendMessage("§e§lENVANTER DURUMU: §f" + target.getName());
                org.bukkit.inventory.PlayerInventory targetInv = target.getInventory();
                int filledSlots = 0;
                for (ItemStack item : targetInv.getStorageContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        filledSlots++;
                    }
                }
                int totalSlots = 36;
                double fillPercent = (filledSlots * 100.0) / totalSlots;
                player.sendMessage("§7Dolu Slot: §e" + filledSlots + "§7/§e" + totalSlots);
                player.sendMessage("§7Doluluk: §e" + String.format("%.0f", fillPercent) + "%");
                player.sendMessage("§6§l═══════════════════════════");
                break;
                
            case POTION:
                // Efekt detayları
                player.sendMessage("§6§l═══════════════════════════");
                player.sendMessage("§d§lAKTİF EFEKTLER: §f" + target.getName());
                java.util.Collection<org.bukkit.potion.PotionEffect> effects = target.getActivePotionEffects();
                if (effects.isEmpty()) {
                    player.sendMessage("§8Aktif efekt yok");
                } else {
                    for (org.bukkit.potion.PotionEffect effect : effects) {
                        String effectName = specialItemManager.getEffectDisplayName(effect.getType());
                        int level = effect.getAmplifier() + 1;
                        int duration = effect.getDuration() / 20;
                        String color = specialItemManager.getEffectColor(effect.getType());
                        player.sendMessage("  " + color + effectName + " §7" + level + " §8(" + duration + "s)");
                    }
                }
                player.sendMessage("§6§l═══════════════════════════");
                break;
                
            case BARRIER:
                // Kapat
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.5f);
                break;
        }
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
