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
        
        org.bukkit.Location blockLoc = event.getBlock().getLocation();
        org.bukkit.Location spawnLoc = p.getWorld().getSpawnLocation();
        double distanceFromSpawn = blockLoc.distance(spawnLoc);
        
        // UZAK DİYARLAR MANTIĞI: Merkeze uzaklaştıkça madenler zenginleşir
        boolean isFarLands = distanceFromSpawn > 5000;
        double distanceMultiplier = isFarLands ? 1.5 : 1.0; // 5000 bloktan sonra %50 daha fazla şans
        
        // Özel madenler düşürme mantığı
        // ANCIENT_DEBRIS = Titanyum Cevheri (WorldGenerationListener tarafından oluşturulmuş)
        if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
            // ANCIENT_DEBRIS kırıldığında Titanyum düşür
            if (ItemManager.TITANIUM_ORE != null) {
                event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    ItemManager.TITANIUM_ORE.clone()
                );
                p.sendMessage("§6Titanyum Cevheri buldun!");
            }
            return; // Normal drop mantığına devam etme
        }
        
        // Derinlerde Titanyum madeni düşürme (eski sistem - geriye dönük uyumluluk)
        if (event.getBlock().getType() == Material.DEEPSLATE || 
            event.getBlock().getType() == Material.DEEPSLATE_COAL_ORE ||
            event.getBlock().getType() == Material.DEEPSLATE_IRON_ORE ||
            event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            
            // Y koordinatı -50'den aşağıdaysa %15 şansla Titanyum düşür (uzak diyarlarda %22.5)
            double titaniumChance = 0.15 * distanceMultiplier;
            if (event.getBlock().getY() < -50 && Math.random() < titaniumChance) {
                if (ItemManager.TITANIUM_ORE != null) {
                    event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(),
                        ItemManager.TITANIUM_ORE.clone()
                    );
                    if (isFarLands) {
                        p.sendMessage("§6Uzak Diyarların zenginliği! Titanyum buldun!");
                    }
                }
            }
        }
        
        // Kızıl Elmas düşürme (nadir) - Uzak diyarlarda daha sık
        // DEEPSLATE_DIAMOND_ORE kırıldığında özel kontrol
        if (event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            double redDiamondChance = (isFarLands ? 0.075 : 0.05) * distanceMultiplier;
            if (event.getBlock().getY() < -60 && Math.random() < redDiamondChance) {
                if (ItemManager.RED_DIAMOND != null) {
                    event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(),
                        ItemManager.RED_DIAMOND.clone()
                    );
                    p.sendMessage("§c§lUZAK DİYARLARIN HAZİNESİ! Kızıl Elmas buldun!");
                }
            }
        }
        
        // Görev takibi: Maden toplama
        missionManager.handleGather(p, event.getBlock().getType());
    }
}

