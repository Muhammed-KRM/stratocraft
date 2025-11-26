package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.GhostRecipeManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

/**
 * GhostRecipeListener - Hayalet tarif sistemi listener'ı
 * Oyuncu tarif kitabına sağ tıkladığında hayalet yapı gösterir
 */
public class GhostRecipeListener implements Listener {
    private final GhostRecipeManager ghostRecipeManager;
    private final ResearchManager researchManager;
    
    public GhostRecipeListener(GhostRecipeManager grm, ResearchManager rm) {
        this.ghostRecipeManager = grm;
        this.researchManager = rm;
    }
    
    /**
     * Oyuncu tarif kitabına sağ tıkladığında hayalet yapı göster
     */
    @EventHandler
    public void onRecipeBookInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null) return;
        
        // Tarif kitabı kontrolü
        String recipeId = getRecipeIdFromItem(item);
        if (recipeId == null) return;
        
        // Oyuncunun bu tarife sahip olduğunu kontrol et
        if (!researchManager.hasRecipeBook(player, recipeId)) {
            player.sendMessage("§cBu tarif kitabına sahip değilsin!");
            return;
        }
        
        // Ray trace ile oyuncunun baktığı yönü bul
        RayTraceResult rayTrace = player.rayTraceBlocks(50);
        Location targetLocation;
        
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            // Blok üzerine tıkladıysa, o blokun konumunu kullan
            targetLocation = rayTrace.getHitBlock().getLocation();
        } else {
            // Havaya tıkladıysa, oyuncunun önüne 5 blok mesafede
            Location playerLoc = player.getLocation();
            targetLocation = playerLoc.clone().add(playerLoc.getDirection().multiply(5));
        }
        
        // Hayalet tarifi göster
        ghostRecipeManager.showGhostRecipe(player, recipeId, targetLocation);
        
        event.setCancelled(true);
    }
    
    /**
     * Oyuncu hareket ettiğinde mesafe kontrolü yap
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Sadece blok değişikliğinde kontrol et (performans için)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        if (ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) {
            ghostRecipeManager.checkDistance(player);
        }
    }
    
    /**
     * Oyuncu blok koyduğunda tarif tamamlanma kontrolü
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (!ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) return;
        
        // Blok koyulduğunda tarif tamamlanma kontrolü yap
        // Not: checkLocation parametresi kullanılmıyor, tüm tarif kontrol ediliyor (doğru)
        ghostRecipeManager.checkRecipeComplete(player, event.getBlockPlaced().getLocation());
    }
    
    /**
     * Oyuncu el değiştirdiğinde hayalet tarifi kaldır
     */
    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        if (ghostRecipeManager.hasActiveRecipe(player.getUniqueId())) {
            // Eğer yeni elinde tarif kitabı yoksa hayalet tarifi kaldır
            ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
            String recipeId = getRecipeIdFromItem(newItem);
            
            if (recipeId == null) {
                ghostRecipeManager.removeGhostRecipe(player);
                player.sendMessage("§cHayalet tarif kaldırıldı.");
            }
        }
    }
    
    /**
     * Oyuncu oyundan çıktığında hayalet tarifi temizle
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ghostRecipeManager.removeGhostRecipe(event.getPlayer());
    }
    
    /**
     * ItemStack'ten tarif ID'sini çıkar
     */
    private String getRecipeIdFromItem(ItemStack item) {
        if (item == null) return null;
        
        // RECIPE_ prefix'li özel eşyaları kontrol et
        if (ItemManager.isCustomItem(item, "RECIPE_ALCHEMY")) return "ALCHEMY";
        if (ItemManager.isCustomItem(item, "RECIPE_TECTONIC")) return "TECTONIC";
        if (ItemManager.isCustomItem(item, "RECIPE_MAGMA_BATTERY")) return "MAGMA_BATTERY";
        if (ItemManager.isCustomItem(item, "RECIPE_CLAN_CREATE")) return "CLAN_CREATE";
        
        return null;
    }
}

