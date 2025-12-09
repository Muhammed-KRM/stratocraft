package me.mami.stratocraft.manager;

import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ResearchManager {

    // Bir oyuncunun belirli bir tarifi öğrenip öğrenmediğini kontrol eder.
    public boolean hasRecipeBook(Player player, String recipeId) {
        String fullId = "RECIPE_" + recipeId.toUpperCase();
        
        // 1. Envanterde var mı?
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemManager.isCustomItem(item, fullId)) return true;
        }

        // 2. Yakındaki Araştırma Masasında (Kürsü) var mı? - Config'den mesafe
        me.mami.stratocraft.manager.GameBalanceConfig balanceConfig = me.mami.stratocraft.Main.getInstance().getConfigManager() != null ? 
            me.mami.stratocraft.Main.getInstance().getConfigManager().getGameBalanceConfig() : null;
        int researchDistance = balanceConfig != null ? balanceConfig.getResearchTableDistance() : 10;
        // Bu, WorldEdit kontrolü kadar önemlidir.
        for (org.bukkit.block.BlockState state : player.getLocation().getChunk().getTileEntities()) {
            if (state instanceof Lectern) {
                Lectern lectern = (Lectern) state;
                if (lectern.getLocation().distance(player.getLocation()) <= researchDistance) {
                    ItemStack book = lectern.getInventory().getItem(0);
                    if (book != null && ItemManager.isCustomItem(book, fullId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

