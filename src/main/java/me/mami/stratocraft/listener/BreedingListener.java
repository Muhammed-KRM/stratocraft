package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BreedingManager;
import me.mami.stratocraft.manager.TamingManager;
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
    
    public BreedingListener(BreedingManager breedingManager, TamingManager tamingManager) {
        this.breedingManager = breedingManager;
        this.tamingManager = tamingManager;
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
     * Çiftleştirme tesisi etkileşimi
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
        
        // Çiftleştirme tesisi mi? (Beacon bloğu)
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
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEggHatch(org.bukkit.event.entity.EntityAgeEvent event) {
        if (!(event.getEntity() instanceof Turtle)) {
            return;
        }
        
        Turtle turtle = (Turtle) event.getEntity();
        
        // Yumurta mı?
        if (!turtle.hasMetadata("EggOwner")) {
            return;
        }
        
        // Yaş 0 veya üzeri ise çatlamış demektir
        if (turtle.getAge() >= 0) {
            // Yumurta çatladı mı kontrol et
            breedingManager.checkEggHatching(turtle);
        }
    }
    
    /**
     * Eş canlıyı bul
     */
    private LivingEntity findMate(LivingEntity entity, org.bukkit.Location loc, double radius) {
        TamingManager.Gender entityGender = tamingManager.getGender(entity);
        if (entityGender == null) {
            return null;
        }
        
        TamingManager.Gender targetGender = entityGender == TamingManager.Gender.MALE ? 
            TamingManager.Gender.FEMALE : TamingManager.Gender.MALE;
        
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

