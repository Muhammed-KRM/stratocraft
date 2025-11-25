package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MissionManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SurvivalListener implements Listener {
    private final MissionManager missionManager;

    public SurvivalListener(MissionManager mm) {
        this.missionManager = mm;
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player p = (Player) event.getPlayer();
        // Özel madenler düşürme mantığı
        // Derinlerde Titanyum madeni düşürme
        if (event.getBlock().getType() == Material.DEEPSLATE || 
            event.getBlock().getType() == Material.DEEPSLATE_COAL_ORE ||
            event.getBlock().getType() == Material.DEEPSLATE_IRON_ORE ||
            event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            
            // Y koordinatı -50'den aşağıdaysa %15 şansla Titanyum düşür
            if (event.getBlock().getY() < -50 && Math.random() < 0.15) {
                if (ItemManager.TITANIUM_ORE != null) {
                    event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(),
                        ItemManager.TITANIUM_ORE.clone()
                    );
                }
            }
        }
        
        // Kızıl Elmas düşürme (nadir)
        if (event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            if (event.getBlock().getY() < -60 && Math.random() < 0.05) {
                if (ItemManager.RED_DIAMOND != null) {
                    event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(),
                        ItemManager.RED_DIAMOND.clone()
                    );
                }
            }
        }
        
        // Görev takibi: Maden toplama
        missionManager.handleGather(p, event.getBlock().getType());
    }
}

