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
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
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
}

