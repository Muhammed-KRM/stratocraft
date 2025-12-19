package me.mami.stratocraft.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import me.mami.stratocraft.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Özel Eşyalar Yöneticisi
 * - Kanca Sistemi (Paslı Kanca, Titan Kancası)
 * - Casusluk Dürbünü
 * - Yapım tarifleri
 */
public class SpecialItemManager {
    // 3 KADEMELİ KANCA SİSTEMİ - Config'den değerler
    private static final double RUSTY_HOOK_RANGE = Double.MAX_VALUE; // Sınırsız menzil
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;

    // Casusluk Dürbünü için takip
    private final Map<Player, Long> spyStartTimes = new HashMap<>();
    private final Map<Player, Player> spyTargets = new HashMap<>();

    // Kanca Cooldown sistemi (Fly hack önleme)
    private final Map<java.util.UUID, Long> hookCooldowns = new HashMap<>();
    
    // Düşme hasarı koruması (kanca kullanıldıktan sonra)
    private final Map<java.util.UUID, Long> fallDamageProtection = new HashMap<>();
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }

    /**
     * Kanca kullanımını işle - 3 KADEMELİ SİSTEM
     */
    public void handleGrapple(PlayerFishEvent event, ItemStack rod) {
        // Sadece kanca bir bloğa takıldığında çek (Fly hack önleme)
        if (event.getState() != PlayerFishEvent.State.IN_GROUND) {
            return;
        }

        Player player = event.getPlayer();

        // COOLDOWN KONTROLÜ - Config'den
        long hookCooldown = balanceConfig != null ? balanceConfig.getHookCooldown() : 1000L;
        if (hookCooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = (hookCooldowns.get(player.getUniqueId()) + hookCooldown) - System.currentTimeMillis();
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

            // Düşme hasarı koruması ekle (slow falling olmadan)
            fallDamageProtection.put(player.getUniqueId(), System.currentTimeMillis());

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
        // 2. ALTIN KANCA - Config'den menzil ve güç
        else if (ItemManager.isCustomItem(rod, "GOLDEN_HOOK")) {
            double goldenRange = balanceConfig != null ? balanceConfig.getHookGoldenRange() : 20.0;
            double distance = hook.getLocation().distance(player.getLocation());

            if (distance > goldenRange) {
                player.sendMessage("§cAltın Kanca menzil dışında! Maksimum " + goldenRange + " blok.");
                hook.remove();
                event.setCancelled(true);
                return;
            }

            // Düşme hasarı koruması ekle (slow falling olmadan)
            fallDamageProtection.put(player.getUniqueId(), System.currentTimeMillis());

            // Orta güçte çekme (bloğun üstüne yetecek kadar)
            pullPlayer(player, hook.getLocation(), 1.2);
            player.sendMessage("§6Altın Kanca kullanıldı!");

            // Partikül efekti
            player.getWorld().spawnParticle(org.bukkit.Particle.CRIT_MAGIC,
                    player.getLocation(), 15, 0.5, 0.5, 0.5, 0.1);
        }
        // 3. TİTAN KANCASI - Config'den menzil ve güç
        else if (ItemManager.isCustomItem(rod, "TITAN_GRAPPLE")) {
            double titanRange = balanceConfig != null ? balanceConfig.getHookTitanRange() : 40.0;
            double pullStrength = balanceConfig != null ? balanceConfig.getHookTitanPullStrength() : 2.5;
            double distance = hook.getLocation().distance(player.getLocation());

            if (distance > titanRange) {
                player.sendMessage("§cTitan Kancası menzil dışında! Maksimum " + titanRange + " blok.");
                hook.remove();
                event.setCancelled(true);
                return;
            }

            // Düşme hasarı koruması ekle (slow falling olmadan) - Config'den
            long fallProtectionDuration = balanceConfig != null ? balanceConfig.getHookFallDamageProtectionDuration() : 3000L;
            fallDamageProtection.put(player.getUniqueId(), System.currentTimeMillis());

            // Çok güçlü çekme - Config'den
            pullPlayer(player, hook.getLocation(), pullStrength);

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

        // Config'den süre kontrolü
        long spyDuration = balanceConfig != null ? balanceConfig.getSpyDuration() : 3000L;
        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed >= spyDuration) {
            // ✅ DÜZELTME: GUI menüsü aç (sadece bir kez açılmalı)
            // Eğer menü zaten açıksa tekrar açma
            try {
                String currentTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(player.getOpenInventory().title());
                if (currentTitle.contains("Casusluk:")) {
                    return; // Menü zaten açık
                }
            } catch (Exception e) {
                // Hata olursa devam et
            }
            openSpyMenu(player, target);
            spyStartTimes.remove(player);
            spyTargets.remove(player);
        }
        // Progress gösterimi kaldırıldı (performans için)
    }
    
    /**
     * Casusluk Dürbünü GUI Menüsünü Aç
     */
    public void openSpyMenu(Player player, Player target) {
        if (player == null || target == null || !target.isOnline()) {
            player.sendMessage("§cHedef oyuncu artık online değil!");
            return;
        }
        
        // 27 slot GUI oluştur
        Inventory gui = Bukkit.createInventory(null, 27, 
            net.kyori.adventure.text.Component.text("§e§lCasusluk: §f" + target.getName()));
        
        // Butonlar
        // Can (Kırmızı Kırmızı Taş)
        ItemStack healthBtn = new ItemStack(Material.REDSTONE);
        org.bukkit.inventory.meta.ItemMeta healthMeta = healthBtn.getItemMeta();
        healthMeta.displayName(net.kyori.adventure.text.Component.text("§c§lCAN DURUMU"));
        java.util.List<net.kyori.adventure.text.Component> healthLore = new java.util.ArrayList<>();
        healthLore.add(net.kyori.adventure.text.Component.text("§7Tıklayarak detaylı can bilgisi görüntüle"));
        healthMeta.lore(healthLore);
        healthBtn.setItemMeta(healthMeta);
        gui.setItem(10, healthBtn);
        
        // Açlık (Ekmek)
        ItemStack hungerBtn = new ItemStack(Material.BREAD);
        org.bukkit.inventory.meta.ItemMeta hungerMeta = hungerBtn.getItemMeta();
        hungerMeta.displayName(net.kyori.adventure.text.Component.text("§e§lAÇLIK DURUMU"));
        java.util.List<net.kyori.adventure.text.Component> hungerLore = new java.util.ArrayList<>();
        hungerLore.add(net.kyori.adventure.text.Component.text("§7Tıklayarak detaylı açlık bilgisi görüntüle"));
        hungerMeta.lore(hungerLore);
        hungerBtn.setItemMeta(hungerMeta);
        gui.setItem(12, hungerBtn);
        
        // Zırh (Demir Göğüslük)
        ItemStack armorBtn = new ItemStack(Material.IRON_CHESTPLATE);
        org.bukkit.inventory.meta.ItemMeta armorMeta = armorBtn.getItemMeta();
        armorMeta.displayName(net.kyori.adventure.text.Component.text("§b§lZIRH DURUMU"));
        java.util.List<net.kyori.adventure.text.Component> armorLore = new java.util.ArrayList<>();
        armorLore.add(net.kyori.adventure.text.Component.text("§7Tıklayarak detaylı zırh bilgisi görüntüle"));
        armorMeta.lore(armorLore);
        armorBtn.setItemMeta(armorMeta);
        gui.setItem(14, armorBtn);
        
        // Envanter (Sandık)
        ItemStack invBtn = new ItemStack(Material.CHEST);
        org.bukkit.inventory.meta.ItemMeta invMeta = invBtn.getItemMeta();
        invMeta.displayName(net.kyori.adventure.text.Component.text("§e§lENVANTER DURUMU"));
        java.util.List<net.kyori.adventure.text.Component> invLore = new java.util.ArrayList<>();
        invLore.add(net.kyori.adventure.text.Component.text("§7Tıklayarak envanter doluluk bilgisi görüntüle"));
        invMeta.lore(invLore);
        invBtn.setItemMeta(invMeta);
        gui.setItem(16, invBtn);
        
        // Efektler (İksir)
        ItemStack effectBtn = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.ItemMeta effectMeta = effectBtn.getItemMeta();
        effectMeta.displayName(net.kyori.adventure.text.Component.text("§d§lAKTİF EFEKTLER"));
        java.util.List<net.kyori.adventure.text.Component> effectLore = new java.util.ArrayList<>();
        effectLore.add(net.kyori.adventure.text.Component.text("§7Tıklayarak aktif efektleri görüntüle"));
        effectMeta.lore(effectLore);
        effectBtn.setItemMeta(effectMeta);
        gui.setItem(22, effectBtn);
        
        // Kapat (Bariyer)
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        org.bukkit.inventory.meta.ItemMeta closeMeta = closeBtn.getItemMeta();
        closeMeta.displayName(net.kyori.adventure.text.Component.text("§c§lKAPAT"));
        java.util.List<net.kyori.adventure.text.Component> closeLore = new java.util.ArrayList<>();
        closeLore.add(net.kyori.adventure.text.Component.text("§7Menüyü kapat"));
        closeMeta.lore(closeLore);
        closeBtn.setItemMeta(closeMeta);
        gui.setItem(26, closeBtn);
        
        // GUI'yi aç
        player.openInventory(gui);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_IN, 1.0f, 1.0f);
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

        // Açlık durumu
        int foodLevel = target.getFoodLevel();
        double saturation = target.getSaturation();
        String hungerColor = foodLevel >= 18 ? "§a" : foodLevel >= 12 ? "§e" : foodLevel >= 6 ? "§6" : "§c";
        spy.sendMessage("§7Açlık: " + hungerColor + foodLevel + "§7/20 §7(Doygunluk: §e" + String.format("%.1f", saturation) + "§7)");

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
        
        // Aktif Potion Efektleri
        java.util.Collection<org.bukkit.potion.PotionEffect> effects = target.getActivePotionEffects();
        if (effects.isEmpty()) {
            spy.sendMessage("§7Efektler: §8Yok");
        } else {
            spy.sendMessage("§7Efektler:");
            for (org.bukkit.potion.PotionEffect effect : effects) {
                String effectName = getEffectDisplayName(effect.getType());
                int level = effect.getAmplifier() + 1; // Amplifier 0-based, level 1-based
                int duration = effect.getDuration() / 20; // Tick'ten saniyeye
                String color = getEffectColor(effect.getType());
                spy.sendMessage("  " + color + effectName + " §7" + level + " §8(" + duration + "s)");
            }
        }
        
        spy.sendMessage("§6§l═══════════════════════════");
    }
    
    /**
     * Potion efekt ismini Türkçe'ye çevir
     */
    public String getEffectDisplayName(org.bukkit.potion.PotionEffectType type) {
        if (type == null) return "Bilinmeyen";
        
        switch (type.getName().toLowerCase()) {
            case "speed": return "Hız";
            case "slowness": return "Yavaşlık";
            case "haste": return "Çabukluk";
            case "mining_fatigue": return "Yorgunluk";
            case "strength": return "Güç";
            case "instant_health": return "Anında Can";
            case "instant_damage": return "Anında Hasar";
            case "jump_boost": return "Zıplama";
            case "nausea": return "Mide Bulantısı";
            case "regeneration": return "Yenilenme";
            case "resistance": return "Direnç";
            case "fire_resistance": return "Ateş Direnci";
            case "water_breathing": return "Su Nefesi";
            case "invisibility": return "Görünmezlik";
            case "blindness": return "Körlük";
            case "night_vision": return "Gece Görüşü";
            case "hunger": return "Açlık";
            case "weakness": return "Zayıflık";
            case "poison": return "Zehir";
            case "wither": return "Solma";
            case "health_boost": return "Can Artışı";
            case "absorption": return "Emilim";
            case "saturation": return "Doygunluk";
            case "glowing": return "Parlama";
            case "levitation": return "Yükselme";
            case "luck": return "Şans";
            case "unluck": return "Şanssızlık";
            case "slow_falling": return "Yavaş Düşüş";
            case "conduit_power": return "Deniz Feneri Gücü";
            case "dolphins_grace": return "Yunus Zarafeti";
            case "bad_omen": return "Kötü Alâmet";
            case "hero_of_the_village": return "Köy Kahramanı";
            default: return type.getName();
        }
    }
    
    /**
     * Potion efekt rengini belirle
     */
    public String getEffectColor(org.bukkit.potion.PotionEffectType type) {
        if (type == null) return "§7";
        
        switch (type.getName().toLowerCase()) {
            case "speed": return "§b";
            case "slowness": return "§8";
            case "haste": return "§e";
            case "mining_fatigue": return "§8";
            case "strength": return "§c";
            case "instant_health": return "§a";
            case "instant_damage": return "§4";
            case "jump_boost": return "§a";
            case "nausea": return "§5";
            case "regeneration": return "§d";
            case "resistance": return "§9";
            case "fire_resistance": return "§6";
            case "water_breathing": return "§b";
            case "invisibility": return "§7";
            case "blindness": return "§8";
            case "night_vision": return "§e";
            case "hunger": return "§6";
            case "weakness": return "§7";
            case "poison": return "§5";
            case "wither": return "§8";
            case "health_boost": return "§c";
            case "absorption": return "§e";
            case "saturation": return "§a";
            case "glowing": return "§b";
            case "levitation": return "§d";
            case "luck": return "§a";
            case "unluck": return "§c";
            case "slow_falling": return "§b";
            case "conduit_power": return "§3";
            case "dolphins_grace": return "§b";
            case "bad_omen": return "§4";
            case "hero_of_the_village": return "§e";
            default: return "§7";
        }
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
    public int getArmorPoints(Material armorType) {
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
        fallDamageProtection.remove(player.getUniqueId()); // Düşme hasarı korumasını da temizle
    }
    
    /**
     * ✅ OPTİMİZE: Dürbün kullanan oyuncuları döndür (Main.java task optimizasyonu için)
     */
    public java.util.Set<Player> getSpyglassUsers() {
        // spyStartTimes Map'indeki tüm oyuncuları döndür (dürbün kullananlar)
        return new java.util.HashSet<>(spyStartTimes.keySet());
    }
    
    /**
     * Düşme hasarı koruması kontrolü
     * @return true eğer oyuncu korumalıysa
     */
    public boolean hasFallDamageProtection(Player player) {
        if (!fallDamageProtection.containsKey(player.getUniqueId())) {
            return false;
        }
        
        long protectionTime = fallDamageProtection.get(player.getUniqueId());
        long elapsed = System.currentTimeMillis() - protectionTime;
        
        // Süre dolduysa kaldır
        long fallProtectionDuration = balanceConfig != null ? balanceConfig.getHookFallDamageProtectionDuration() : 3000L;
        if (elapsed >= fallProtectionDuration) {
            fallDamageProtection.remove(player.getUniqueId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Özel silah oluştur (Tier 4 ve Tier 5 silahlar için)
     */
    public ItemStack createSpecialItem(String id, String name, Material material, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // ID'den tier'ı belirle
        int tier = 4; // Varsayılan
        if (id.startsWith("l5_")) {
            tier = 5;
        } else if (id.startsWith("l4_")) {
            tier = 4;
        }
        
        // Tier'a göre renk ve isim
        NamedTextColor nameColor = tier == 4 ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.RED;
        String tierName = tier == 4 ? "4 (Efsanevi)" : "5 (Tanrısal)";
        
        meta.displayName(Component.text(name).color(nameColor));
        
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text(description).color(NamedTextColor.GRAY));
        lore.add(Component.text("Tier: " + tierName).color(NamedTextColor.GOLD));
        lore.add(Component.text("Mod Değiştirmek için Shift+Sağ Tık").color(NamedTextColor.YELLOW));
        meta.lore(lore);

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "special_item_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        
        // Tier bilgisini kaydet (weapon_level)
        NamespacedKey tierKey = new NamespacedKey(Main.getInstance(), "weapon_level");
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
        
        // Varsayılan Mod 1
        NamespacedKey modeKey = new NamespacedKey(Main.getInstance(), "weapon_mode");
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.INTEGER, 1);
        
        // Kırılmazlık
        meta.setUnbreakable(true);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * God Tier silahı al (Eski metod - getTier5Weapon kullanılmalı)
     * @deprecated Use getTier5Weapon instead
     */
    @Deprecated
    public ItemStack getGodTierWeapon(String weaponCode) {
        return getTier5Weapon(weaponCode);
    }
    
    /**
     * Tier 1 silahı oluştur (Çaylak)
     */
    public ItemStack getTier1Weapon(String id) {
        switch (id) {
            case "l1_1": // Hız Hançeri
                ItemStack dagger = createSpecialItemWithTier("l1_1_rogue_dagger", "Hız Hançeri", Material.IRON_SWORD, "Hafif ve ölümcül.", 1);
                ItemMeta meta = dagger.getItemMeta();
                // Elinde tutarken Speed II verir (Attribute)
                AttributeModifier speedMod = new AttributeModifier(
                    UUID.randomUUID(), 
                    "generic.movementSpeed", 
                    0.05, 
                    AttributeModifier.Operation.ADD_NUMBER, 
                    EquipmentSlot.HAND
                );
                meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, speedMod);
                // Attack Speed yüksek (hızlı vuruş)
                AttributeModifier attackSpeedMod = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.attackSpeed",
                    2.0, // +2.0 attack speed (daha hızlı vuruş)
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
                );
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedMod);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                dagger.setItemMeta(meta);
                return dagger;
            case "l1_2": 
                return createSpecialItemWithTier("l1_2_harvest_scythe", "Çiftçi Tırpanı", Material.IRON_HOE, "Alan hasarı vurur.", 1);
            case "l1_3": 
                return createSpecialItemWithTier("l1_3_gravity_mace", "Yerçekimi Gürzü", Material.IRON_SHOVEL, "Sağ tıkla havaya fırla!", 1);
            case "l1_4": 
                return createSpecialItemWithTier("l1_4_boom_bow", "Patlayıcı Yay", Material.BOW, "Okları patlar.", 1);
            case "l1_5": 
                return createSpecialItemWithTier("l1_5_vampire_blade", "Vampir Dişi", Material.GOLDEN_SWORD, "Can çalar.", 1);
            default: 
                return null;
        }
    }

    /**
     * Tier 2 silahı oluştur (Asker)
     */
    public ItemStack getTier2Weapon(String id) {
        switch (id) {
            case "l2_1": 
                return createSpecialItemWithTier("l2_1_inferno_sword", "Alev Kılıcı", Material.GOLDEN_SWORD, "Alev dalgası atar.", 2);
            case "l2_2": 
                return createSpecialItemWithTier("l2_2_frost_wand", "Buz Asası", Material.STICK, "Düşmanı dondurur.", 2);
            case "l2_3": 
                return createSpecialItemWithTier("l2_3_venom_spear", "Zehirli Mızrak", Material.TRIDENT, "Zehir bulutu oluşturur.", 2);
            case "l2_4": 
                return createSpecialItemWithTier("l2_4_guardian_shield", "Golem Kalkanı", Material.SHIELD, "Eğilince dostları iyileştirir.", 2);
            case "l2_5": 
                return createSpecialItemWithTier("l2_5_thunder_axe", "Şok Baltası", Material.IRON_AXE, "Kritik vuruşta çarpar.", 2);
            default: 
                return null;
        }
    }

    /**
     * Tier 3 silahı oluştur (Elit)
     */
    public ItemStack getTier3Weapon(String id) {
        switch (id) {
            case "l3_1": 
                return createSpecialItemWithTier("l3_1_shadow_katana", "Gölge Katanası", Material.IRON_SWORD, "İleri atıl (Dash).", 3);
            case "l3_2": 
                return createSpecialItemWithTier("l3_2_earthshaker", "Deprem Çekici", Material.NETHERITE_SHOVEL, "Yeri sars ve herkesi fırlat.", 3);
            case "l3_3": 
                return createSpecialItemWithTier("l3_3_machine_crossbow", "Taramalı Yay", Material.CROSSBOW, "Seri atış yapar.", 3);
            case "l3_4": 
                return createSpecialItemWithTier("l3_4_witch_orb", "Büyücü Küresi", Material.MAGMA_CREAM, "Güdümlü mermiler atar.", 3);
            case "l3_5": 
                return createSpecialItemWithTier("l3_5_phantom_dagger", "Hayalet Hançeri", Material.FEATHER, "Görünmez ol ve suikast yap.", 3);
            default: 
                return null;
        }
    }
    
    /**
     * Tier 4 silahı oluştur (Efsanevi - Modlu)
     */
    public ItemStack getTier4Weapon(String id) {
        switch (id) {
            case "l4_1": 
                return createSpecialItem("l4_1_elementalist", "Element Kılıcı", Material.DIAMOND_SWORD, "Mod 1: Ateş | Mod 2: Buz");
            case "l4_2": 
                return createSpecialItem("l4_2_life_death", "Yaşam ve Ölüm", Material.BONE, "Mod 1: Wither | Mod 2: Can Bas");
            case "l4_3": 
                return createSpecialItem("l4_3_mjolnir_v2", "Mjölnir V2", Material.IRON_AXE, "Mod 1: Zincirleme Şimşek | Mod 2: Fırlat");
            case "l4_4": 
                return createSpecialItem("l4_4_ranger_pride", "Avcı Yayı", Material.BOW, "Mod 1: Sniper | Mod 2: Pompalı");
            case "l4_5": 
                return createSpecialItem("l4_5_magnetic_glove", "Manyetik Eldiven", Material.FISHING_ROD, "Mod 1: Çek | Mod 2: İt");
            default: 
                return null;
        }
    }

    /**
     * Tier 5 silahı oluştur (Tanrısal - Modlu)
     */
    public ItemStack getTier5Weapon(String id) {
        switch (id) {
            case "l5_1": 
                return createSpecialItem("l5_1_void_walker", "Hiperiyon Kılıcı", Material.NETHERITE_SWORD, "Mod 1: Işınlan Patlat | Mod 2: Kara Kalkan");
            case "l5_2": 
                return createSpecialItem("l5_2_meteor_caller", "Meteor Çağıran", Material.GOLDEN_AXE, "Mod 1: Kıyamet | Mod 2: Yer Yaran");
            case "l5_3": 
                return createSpecialItem("l5_3_titan_slayer", "Titan Katili", Material.TRIDENT, "Mod 1: %5 Gerçek Hasar | Mod 2: Mızrak Yağmuru");
            case "l5_4": 
                return createSpecialItem("l5_4_soul_reaper", "Ruh Biçen", Material.WITHER_ROSE, "Mod 1: Hortlak Çağır | Mod 2: Ruh Patlaması");
            case "l5_5": 
                return createSpecialItem("l5_5_time_keeper", "Zamanı Büken", Material.CLOCK, "Mod 1: Zamanı Durdur | Mod 2: Geri Sar");
            default: 
                return null;
        }
    }
    
    /**
     * createSpecialItem metodunu Tier 1-3 için güncelle (Tier bilgisi ekle)
     */
    private ItemStack createSpecialItemWithTier(String id, String name, Material material, String description, int tier) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Tier'a göre renk
        NamedTextColor nameColor = tier == 1 ? NamedTextColor.GRAY : 
                                   tier == 2 ? NamedTextColor.BLUE : 
                                   tier == 3 ? NamedTextColor.DARK_PURPLE : NamedTextColor.RED;
        
        meta.displayName(Component.text(name).color(nameColor));
        
        java.util.List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.text(description).color(NamedTextColor.GRAY));
        lore.add(Component.text("Tier: " + tier).color(NamedTextColor.GOLD));
        meta.lore(lore);

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "special_item_id");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        
        // Tier bilgisini de kaydet
        NamespacedKey tierKey = new NamespacedKey(Main.getInstance(), "weapon_level");
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);

        item.setItemMeta(meta);
        return item;
    }
}
