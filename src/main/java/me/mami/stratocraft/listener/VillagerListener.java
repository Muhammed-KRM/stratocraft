package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
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
        AbstractVillager abstractVillager = event.getEntity();

        // Sadece gerçek köylülerde (Wandering Trader hariç) tarif kitapları satılır
        if (abstractVillager instanceof Villager villager &&
                (villager.getProfession() == Villager.Profession.CLERIC ||
                 villager.getProfession() == Villager.Profession.LIBRARIAN)) {

            // %30 şansla tarif kitabı ekle
            if (random.nextDouble() < 0.3) {
                List<MerchantRecipe> newRecipes = new ArrayList<>(abstractVillager.getRecipes());

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

                abstractVillager.setRecipes(newRecipes);
            }
        }
    }
}

