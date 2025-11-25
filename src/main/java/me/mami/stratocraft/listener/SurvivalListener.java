package me.mami.stratocraft.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class SurvivalListener implements Listener {

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        // Özel madenler düşürme mantığı
        // Örnek: Titanyum madeni düşürme
        if (event.getBlock().getType() == Material.DEEPSLATE) {
            // %10 şansla Titanyum düşür
            if (Math.random() < 0.1) {
                event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    new ItemStack(Material.IRON_INGOT) // ItemManager.TITANIUM_ORE kullanılabilir
                );
            }
        }
    }
}

