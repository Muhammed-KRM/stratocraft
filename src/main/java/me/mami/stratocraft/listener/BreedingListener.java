package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BreedingManager;
import me.mami.stratocraft.manager.TamingManager;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Çiftleştirme Sistemi Dinleyicisi
 * - Yemek verme ile çiftleştirme
 * - Çiftleştirme tesisi etkileşimi
 * - Yumurta çatlama kontrolü
 */
public class BreedingListener implements Listener {
    private final BreedingManager breedingManager;
    private final TamingManager tamingManager;
    private final me.mami.stratocraft.Main plugin;

    public BreedingListener(BreedingManager breedingManager, TamingManager tamingManager, me.mami.stratocraft.Main plugin) {
        this.breedingManager = breedingManager;
        this.tamingManager = tamingManager;
        this.plugin = plugin;
    }

    /**
     * Yemek verme ile çiftleştirme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFeedCreature(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Yemek item'ı mı?
        if (item == null || !isFood(item.getType())) {
            return;
        }

        // Eğitilmiş mi?
        if (!tamingManager.isTamed(entity)) {
            return;
        }

        // Sahip mi?
        if (!tamingManager.canUseCreature(entity, player.getUniqueId())) {
            return;
        }

        // Yakındaki eş canlıyı bul
        LivingEntity mate = findMate(entity, player.getLocation(), 5.0);
        if (mate == null) {
            // Eş yok, sadece besle
            player.sendMessage("§aCanlıyı besledin!");
            return;
        }

        // Çiftleştirme
        if (breedingManager.breedCreatures(entity, mate, player)) {
            // Item tüket
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            event.setCancelled(true);
        }
    }

    /**
     * Üreme Çekirdeği yerleştirme (item ile bloğa sağ tık)
     * Yüksek priority ile önce çalışır
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreedingCorePlace(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Üreme Çekirdeği kontrolü
        if (!ItemManager.isCustomItem(item, "BREEDING_CORE")) {
            return;
        }
        
        // Bloğun üstüne yerleştir
        Block targetBlock = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP);
        if (targetBlock.getType() != Material.AIR) {
            player.sendMessage("§cBuraya yerleştirilemez! Blok boş olmalı.");
            event.setCancelled(true);
            return;
        }
        
        // Üreme Çekirdeği olarak işaretle
        targetBlock.setType(Material.BEACON);
        targetBlock.setMetadata("BreedingCore", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§a§lÜreme Çekirdeği yerleştirildi!");
        player.sendMessage("§7Etrafına seviyeye göre yapıları yap ve aktifleştirme için sağ tıkla.");
        
        // Efekt
        org.bukkit.Location loc = targetBlock.getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 20, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        
        event.setCancelled(true);
    }
    
    /**
     * Cinsiyet kontrolü (item ile canlıya sağ tık)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onGenderCheck(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Cinsiyet Ayırıcı kontrolü
        if (!ItemManager.isCustomItem(item, "GENDER_SCANNER")) {
            return;
        }
        
        // Eğitilmiş mi?
        if (!tamingManager.isTamed(entity)) {
            player.sendMessage("§cBu canlı eğitilmemiş!");
            return;
        }
        
        // Cinsiyet kontrolü
        TamingManager.Gender gender = tamingManager.getGender(entity);
        if (gender == null) {
            player.sendMessage("§cCanlının cinsiyeti belirlenemiyor!");
            return;
        }
        
        String genderText = gender == TamingManager.Gender.MALE ? "§b♂ Erkek" : "§d♀ Dişi";
        String creatureName = entity.getCustomName() != null ? entity.getCustomName() : "Bilinmeyen Canlı";
        
        player.sendMessage("§6=== CİNSİYET BİLGİSİ ===");
        player.sendMessage("§7Canlı: §e" + creatureName);
        player.sendMessage("§7Cinsiyet: " + genderText);
        
        event.setCancelled(true);
    }
    
    /**
     * Çiftleştirme tesisi etkileşimi (Üreme Çekirdeği aktifleştirme)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBreedingFacilityInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Üreme Çekirdeği mi?
        if (block.hasMetadata("BreedingCore")) {
            Player player = event.getPlayer();
            // Üreme çekirdeğini aktifleştir
            if (breedingManager.activateBreedingCore(block, player)) {
                event.setCancelled(true);
            }
            return;
        }

        // Eski sistem (Beacon bloğu) - geriye dönük uyumluluk
        if (block.getType() != Material.BEACON) {
            return;
        }

        Location loc = block.getLocation();
        BreedingManager.BreedingFacility facility = breedingManager.getFacility(loc);

        if (facility == null) {
            return;
        }

        Player player = event.getPlayer();

        // Yakındaki eğitilmiş canlıyı bul
        LivingEntity creature = findNearbyTamedCreature(loc, 3.0);
        if (creature != null) {
            // Canlıyı tesise ekle
            if (breedingManager.addCreatureToFacility(loc, creature, player)) {
                event.setCancelled(true);
            }
        } else {
            // Tesis bilgisi göster
            showFacilityInfo(player, facility);
            event.setCancelled(true);
        }
    }

    /**
     * Yumurta çatlama kontrolü (EntityAgeEvent ile - opsiyonel)
     * Ana kontrol Main.java'daki task'ta yapılıyor
     */
    /*
     * @EventHandler(priority = EventPriority.MONITOR)
     * public void onEggHatch(org.bukkit.event.entity.EntityAgeEvent event) {
     * if (!(event.getEntity() instanceof Turtle)) {
     * return;
     * }
     * 
     * Turtle turtle = (Turtle) event.getEntity();
     * 
     * // Yumurta mı?
     * if (!turtle.hasMetadata("EggOwner")) {
     * return;
     * }
     * 
     * // Yaş 0 veya üzeri ise çatlamış demektir
     * if (turtle.getAge() >= 0) {
     * // Yumurta çatladı mı kontrol et
     * breedingManager.checkEggHatching(turtle);
     * }
     * }
     */

    /**
     * Eş canlıyı bul
     */
    private LivingEntity findMate(LivingEntity entity, org.bukkit.Location loc, double radius) {
        TamingManager.Gender entityGender = tamingManager.getGender(entity);
        if (entityGender == null) {
            return null;
        }

        TamingManager.Gender targetGender = entityGender == TamingManager.Gender.MALE ? TamingManager.Gender.FEMALE
                : TamingManager.Gender.MALE;

        for (org.bukkit.entity.Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (!(nearby instanceof LivingEntity) || nearby.equals(entity)) {
                continue;
            }

            LivingEntity nearbyEntity = (LivingEntity) nearby;

            // Eğitilmiş mi?
            if (!tamingManager.isTamed(nearbyEntity)) {
                continue;
            }

            // Aynı sahip mi?
            java.util.UUID owner1 = tamingManager.getOwner(entity);
            java.util.UUID owner2 = tamingManager.getOwner(nearbyEntity);
            if (owner1 == null || owner2 == null || !owner1.equals(owner2)) {
                continue;
            }

            // Karşı cins mi?
            TamingManager.Gender nearbyGender = tamingManager.getGender(nearbyEntity);
            if (nearbyGender == targetGender) {
                return nearbyEntity;
            }
        }

        return null;
    }

    /**
     * Yakındaki eğitilmiş canlıyı bul
     */
    private LivingEntity findNearbyTamedCreature(org.bukkit.Location loc, double radius) {
        for (org.bukkit.entity.Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (nearby instanceof LivingEntity && !(nearby instanceof Player)) {
                LivingEntity entity = (LivingEntity) nearby;
                if (tamingManager.isTamed(entity)) {
                    return entity;
                }
            }
        }
        return null;
    }

    /**
     * Yemek item'ı mı?
     */
    private boolean isFood(Material material) {
        return material == Material.WHEAT || material == Material.CARROT ||
                material == Material.POTATO || material == Material.BEETROOT ||
                material == Material.APPLE || material == Material.BREAD ||
                material == Material.COOKED_BEEF || material == Material.COOKED_PORKCHOP ||
                material == Material.COOKED_CHICKEN || material == Material.COOKED_MUTTON ||
                material == Material.GOLDEN_APPLE || material == Material.GOLDEN_CARROT;
    }

    /**
     * Tesis bilgisi göster
     */
    private void showFacilityInfo(Player player, BreedingManager.BreedingFacility facility) {
        player.sendMessage("§6=== ÇİFTLEŞTİRME TESİSİ ===");
        player.sendMessage("§7Seviye: §e" + facility.getLevel());

        if (facility.getFemale() != null) {
            player.sendMessage("§7Dişi: §a" + facility.getFemale().getCustomName());
        } else {
            player.sendMessage("§7Dişi: §cYok");
        }

        if (facility.getMale() != null) {
            player.sendMessage("§7Erkek: §a" + facility.getMale().getCustomName());
        } else {
            player.sendMessage("§7Erkek: §cYok");
        }

        if (facility.isBreeding()) {
            long remaining = facility.getRemainingTime();
            player.sendMessage("§7Durum: §aÇiftleştirme aktif");
            player.sendMessage("§7Kalan Süre: §e" + formatTime(remaining));
        } else {
            player.sendMessage("§7Durum: §eBeklemede");
            player.sendMessage("§7Not: 1 dişi + 1 erkek + yiyecek gerekli");
        }
    }

    /**
     * Zaman formatla
     */
    private String formatTime(long ms) {
        long days = ms / 86400000L;
        long hours = (ms % 86400000L) / 3600000L;
        long minutes = (ms % 3600000L) / 60000L;

        if (days > 0) {
            return days + " gün " + hours + " saat";
        } else if (hours > 0) {
            return hours + " saat " + minutes + " dakika";
        } else {
            return minutes + " dakika";
        }
    }
}
