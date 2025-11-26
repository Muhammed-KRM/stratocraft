package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillagerListener implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onVillagerTrade(VillagerAcquireTradeEvent event) {
        Villager villager = event.getEntity();
        
        // Sadece belirli mesleklerde tarif kitapları satılır
        if (villager.getProfession() == Villager.Profession.CLERIC || 
            villager.getProfession() == Villager.Profession.LIBRARIAN) {
            
            MerchantRecipe recipe = event.getRecipe();
            
            // %30 şansla tarif kitabı ekle
            if (random.nextDouble() < 0.3) {
                List<MerchantRecipe> newRecipes = new ArrayList<>(villager.getRecipes());
                
                // Tektonik Sabitleyici tarifi
                if (ItemManager.RECIPE_BOOK_TECTONIC != null) {
                    MerchantRecipe tectonicRecipe = new MerchantRecipe(
                        ItemManager.RECIPE_BOOK_TECTONIC.clone(),
                        3 // Maksimum kullanım
                    );
                    tectonicRecipe.addIngredient(new org.bukkit.inventory.ItemStack(Material.EMERALD, 32));
                    tectonicRecipe.addIngredient(new org.bukkit.inventory.ItemStack(Material.BOOK, 1));
                    newRecipes.add(tectonicRecipe);
                }
                
                villager.setRecipes(newRecipes);
            }
        }
    }
}

