package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ResearchListener implements Listener {
    private final ResearchManager researchManager;

    public ResearchListener(ResearchManager rm) {
        this.researchManager = rm;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player p = (Player) event.getWhoClicked();
        
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(p)) {
            return; // Admin bypass yetkisi varsa tarif kısıtlamalarını atla
        }
        
        ItemStack result = event.getRecipe().getResult();
        
        // Özel eşyalar için tarif kontrolü
        if (ItemManager.isCustomItem(result, "RECIPE_TECTONIC") || 
            ItemManager.isCustomItem(result, "TITANIUM_INGOT") ||
            ItemManager.isCustomItem(result, "DARK_MATTER")) {
            
            // Eğer tarif kitabı yoksa crafting'i iptal et
            String recipeId = "";
            if (ItemManager.isCustomItem(result, "RECIPE_TECTONIC")) {
                recipeId = "TECTONIC";
            } else if (ItemManager.isCustomItem(result, "TITANIUM_INGOT")) {
                recipeId = "TITANIUM";
            }
            
            if (!recipeId.isEmpty() && !researchManager.hasRecipeBook(p, recipeId)) {
                event.setCancelled(true);
                p.sendMessage("§cBu eşyayı yapmak için gerekli tarif kitabına sahip değilsin!");
                return; // Event iptal edildi, devam etme
            }
        }
    }
}

