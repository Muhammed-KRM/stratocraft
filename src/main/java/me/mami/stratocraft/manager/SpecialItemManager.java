package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Özel Eşyalar Yöneticisi
 * - Kanca Sistemi (Paslı Kanca, Titan Kancası)
 * - Casusluk Dürbünü
 * - Yapım tarifleri
 */
public class SpecialItemManager {
    // 3 KADEMELİ KANCA SİSTEMİ
    private static final double RUSTY_HOOK_RANGE = Double.MAX_VALUE; // Sınırsız menzil
    private static final double GOLDEN_HOOK_RANGE = 20.0; // Orta menzil
    private static final double TITAN_GRAPPLE_RANGE = 40.0; // Uzun menzil
    private static final long SPY_DURATION = 3000; // 3 saniye (milisaniye)

    // Casusluk Dürbünü için takip
    private final Map<Player, Long> spyStartTimes = new HashMap<>();
    private final Map<Player, Player> spyTargets = new HashMap<>();

    // Kanca Cooldown sistemi (Fly hack önleme)
    private final Map<java.util.UUID, Long> hookCooldowns = new HashMap<>();
    private static final long HOOK_COOLDOWN = 1000; // 1 saniye

    /**
     * Kanca kullanımını işle - 3 KADEMELİ SİSTEM
     */
    public void handleGrapple(PlayerFishEvent event, ItemStack rod) {
        // Sadece kanca bir bloğa takıldığında çek (Fly hack önleme)
        if (event.getState() != PlayerFishEvent.State.IN_GROUND) {
            return;
        }

        Player player = event.getPlayer();

        // COOLDOWN KONTROLÜ
        if (hookCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (hookCooldowns.get(player.getUniqueId()) + HOOK_COOLDOWN) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage("§cKanca soğumadı! Bekle: §e" + String.format("%.1f", timeLeft / 1000.0) + "§c sn");
                event.setCancelled(true);
                return;
            }
        }

        // Cooldown kaydet
        hookCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        FishHook hook = event.getHook();

        // 1. PASLI KANCA - Sınırsız menzil ama MAX 3 BLOK YUKARI
        if (ItemManager.isCustomItem(rod, "RUSTY_HOOK")) {
            double verticalDistance = hook.getLocation().getY() - player.getLocation().getY();

            // Max 3 blok yukarı çek
            if (verticalDistance > 3.0) {
                // 3 bloktan fazlaysa, gücü azalt
                double reducedStrength = 0.4 * (3.0 / verticalDistance);
                pullPlayer(player, hook.getLocation(), reducedStrength);
            } else {
                // 3 blok veya altıysa normal çek
                pullPlayer(player, hook.getLocation(), 0.4);
            }

            player.sendMessage("§7Paslı Kanca kullanıldı! (Max 3 blok yukarı)");
        }
        // 2. ALTIN KANCA - 20 blok menzil, bloğun üstüne çıkar
        else if (ItemManager.isCustomItem(rod, "GOLDEN_HOOK")) {
            double distance = hook.getLocation().distance(player.getLocation());

            if (distance > GOLDEN_HOOK_RANGE) {
                player.sendMessage("§cAltın Kanca menzil dışında! Maksimum " + GOLDEN_HOOK_RANGE + " blok.");
                hook.remove();
                event.setCancelled(true);
                return;
            }

            // Bloğun üstüne çıkmak için kısa slow falling
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOW_FALLING, 20, 0, false, false));

            // Orta güçte çekme (bloğun üstüne yetecek kadar)
            pullPlayer(player, hook.getLocation(), 1.2);
            player.sendMessage("§6Altın Kanca kullanıldı!");

            // Partikül efekti
            player.getWorld().spawnParticle(org.bukkit.Particle.CRIT_MAGIC,
                    player.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
        }
        // 3. TİTAN KANCASI - 40 blok, çok güçlü
        else if (ItemManager.isCustomItem(rod, "TITAN_GRAPPLE")) {
            double distance = hook.getLocation().distance(player.getLocation());

            if (distance > TITAN_GRAPPLE_RANGE) {
                player.sendMessage("§cTitan Kancası menzil dışında! Maksimum " + TITAN_GRAPPLE_RANGE + " blok.");
                hook.remove();
                event.setCancelled(true);
                return;
            }

            // Kısa slow falling (hızlı iniş)
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOW_FALLING, 10, 0, false, false));

            // Çok güçlü çekme
            pullPlayer(player, hook.getLocation(), 2.5);

            // Ses ve partikül
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.2f);
            player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH,
                    player.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);

            player.sendMessage("§6§lTitan Kancası kullanıldı!");

            // Neredeyse kırılmaz (sadece düşük durability'de damage al)
            org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) rod.getItemMeta();
            if (damageable != null) {
                int currentDamage = damageable.getDamage();
                int maxDurability = rod.getType().getMaxDurability();
                int remaining = maxDurability - currentDamage;

                // %20'den az kaldıysa damage al
                if (remaining < maxDurability * 0.2) {
                    damageItem(rod, 1);
                }
            }
        }
    }

    /**
     * Casusluk Dürbünü kullanımını işle
     */
    public void handleSpyglass(Player player, org.bukkit.util.RayTraceResult result) {
        if (result == null || !(result.getHitEntity() instanceof Player)) {
            spyStartTimes.remove(player);
            spyTargets.remove(player);
            return;
        }

        Player target = (Player) result.getHitEntity();

        // Kendine bakamaz
        if (target.equals(player)) {
            spyStartTimes.remove(player);
            spyTargets.remove(player);
            return;
        }

        // Hedef değişti mi kontrol et
        Player previousTarget = spyTargets.get(player);
        if (previousTarget == null || !previousTarget.equals(target)) {
            // Yeni hedef, zamanı sıfırla
            spyStartTimes.put(player, System.currentTimeMillis());
            spyTargets.put(player, target);
            return;
        }

        // Aynı hedefe bakıyor, zamanı kontrol et
        Long startTime = spyStartTimes.get(player);
        if (startTime == null) {
            spyStartTimes.put(player, System.currentTimeMillis());
            return;
        }

        // 3 saniye geçti mi kontrol et
        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed >= SPY_DURATION) {
            // Bilgileri göster
            showPlayerInfo(player, target);
            spyStartTimes.remove(player);
            spyTargets.remove(player);
        }
    }

    /**
     * Oyuncu bilgilerini göster (Casusluk Dürbünü)
     */
    public void showPlayerInfo(Player spy, Player target) {
        spy.sendMessage("§6§l═══════════════════════════");
        spy.sendMessage("§e§lCASUSLUK RAPORU: §f" + target.getName());
        spy.sendMessage("§7Can: §c" + String.format("%.1f", target.getHealth()) + "§7/§c" +
                String.format("%.1f",
                        target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));

        // Zırh durumu
        org.bukkit.inventory.PlayerInventory inv = target.getInventory();
        int armorPoints = 0;
        if (inv.getHelmet() != null)
            armorPoints += getArmorPoints(inv.getHelmet().getType());
        if (inv.getChestplate() != null)
            armorPoints += getArmorPoints(inv.getChestplate().getType());
        if (inv.getLeggings() != null)
            armorPoints += getArmorPoints(inv.getLeggings().getType());
        if (inv.getBoots() != null)
            armorPoints += getArmorPoints(inv.getBoots().getType());

        spy.sendMessage("§7Zırh: §b" + armorPoints + "§7/20");

        // Envanter doluluğu
        int filledSlots = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item != null && item.getType() != Material.AIR) {
                filledSlots++;
            }
        }
        int totalSlots = 36;
        double fillPercent = (filledSlots * 100.0) / totalSlots;

        spy.sendMessage("§7Envanter: §e" + filledSlots + "§7/§e" + totalSlots +
                " §7(§e" + String.format("%.0f", fillPercent) + "%§7)");
        spy.sendMessage("§6§l═══════════════════════════");
    }

    /**
     * Oyuncuyu hedefe çek (Kanca) - Geliştirilmiş yukarı fırlatma
     */
    private void pullPlayer(Player player, org.bukkit.Location target, double strength) {
        Vector direction = target.toVector().subtract(player.getLocation().toVector()).normalize();
        direction.multiply(strength);

        // Y eksenini güçlendir (yukarı fırlatma için)
        double yBoost = Math.max(0.4, direction.getY() * 1.5);
        direction.setY(yBoost);

        player.setVelocity(direction);
    }

    /**
     * Eşyanın dayanıklılığını azalt
     */
    private void damageItem(ItemStack item, int damage) {
        if (item.getType().getMaxDurability() > 0) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof org.bukkit.inventory.meta.Damageable) {
                org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                damageable.setDamage(damageable.getDamage() + damage);

                if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                    item.setType(Material.AIR);
                } else {
                    item.setItemMeta(meta);
                }
            }
        }
    }

    /**
     * Zırh puanını hesapla
     */
    private int getArmorPoints(Material armorType) {
        switch (armorType) {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                return 1;
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
                return 2;
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
                return 3;
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                return 4;
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_LEGGINGS:
            case NETHERITE_BOOTS:
                return 5;
            default:
                return 0;
        }
    }

    /**
     * Özel eşya yapım tariflerini kaydet
     */
    public void registerRecipes() {
        // Paslı Kanca: 2 Demir + 1 İp
        org.bukkit.NamespacedKey rustyHookKey = new org.bukkit.NamespacedKey(Main.getInstance(), "rusty_hook");
        org.bukkit.inventory.ShapedRecipe rustyHookRecipe = new org.bukkit.inventory.ShapedRecipe(rustyHookKey,
                ItemManager.RUSTY_HOOK);
        rustyHookRecipe.shape(" I ", " I ", " S ");
        rustyHookRecipe.setIngredient('I', Material.IRON_INGOT);
        rustyHookRecipe.setIngredient('S', Material.STRING);
        org.bukkit.Bukkit.addRecipe(rustyHookRecipe);

        // Titan Kancası: 2 Titanyum + 1 Mithril İpi + 1 Nether Star
        // Not: Custom item'lar için tarif kontrolü ResearchListener'da yapılacak
        // Burada sadece placeholder tarif ekleniyor
        if (ItemManager.TITANIUM_INGOT != null && ItemManager.MITHRIL_STRING != null) {
            org.bukkit.NamespacedKey titanGrappleKey = new org.bukkit.NamespacedKey(Main.getInstance(),
                    "titan_grapple");
            org.bukkit.inventory.ShapedRecipe titanGrappleRecipe = new org.bukkit.inventory.ShapedRecipe(
                    titanGrappleKey, ItemManager.TITAN_GRAPPLE);
            titanGrappleRecipe.shape(" T ", " T ", "MS ");
            // Placeholder: Custom item kontrolü ResearchListener'da yapılacak
            titanGrappleRecipe.setIngredient('T', Material.IRON_INGOT); // Titanyum placeholder (2 adet)
            titanGrappleRecipe.setIngredient('M', Material.STRING); // Mithril İpi placeholder
            titanGrappleRecipe.setIngredient('S', Material.NETHER_STAR); // Nether Star
            org.bukkit.Bukkit.addRecipe(titanGrappleRecipe);
        }
    }

    /**
     * Casusluk durumunu temizle (oyuncu çıkışında)
     */
    public void clearSpyData(Player player) {
        spyStartTimes.remove(player);
        spyTargets.remove(player);
        hookCooldowns.remove(player.getUniqueId()); // Cooldown'u da temizle
    }
}
