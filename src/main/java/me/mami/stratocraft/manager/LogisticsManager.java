package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;

public class LogisticsManager {
    @SuppressWarnings("unused")
    private final TerritoryManager territoryManager;
    private final Map<Location, Location> railConnections = new HashMap<>(); // Start -> End

    public LogisticsManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    public void createRailConnection(Location start, Location end) {
        railConnections.put(start, end);
    }

    public void transportItems(Player p, Location railStation) {
        Location destination = railConnections.get(railStation);
        if (destination == null) {
            p.sendMessage("§cBu istasyon bağlı değil!");
            return;
        }

        Block startBlock = railStation.getBlock();
        Block destBlock = destination.getBlock();
        
        if (startBlock.getType() != Material.CHEST || destBlock.getType() != Material.CHEST) {
            p.sendMessage("§cHer iki tarafta da sandık olmalı!");
            return;
        }

        Chest startChest = (Chest) startBlock.getState();
        Inventory startInv = startChest.getInventory();
        
        // Sandıktaki eşyaları kaydet
        ItemStack[] items = startInv.getContents().clone();
        
        // Sandığı temizle
        startInv.clear();
        
        p.sendMessage("§aEşyalar gönderildi! 5 dakika içinde hedefe ulaşacak.");
        
        // 5 dakika (6000 tick) sonra hedef sandığa ekle
        new BukkitRunnable() {
            @Override
            public void run() {
                if (destBlock.getType() == Material.CHEST) {
                    Chest destChest = (Chest) destBlock.getState();
                    Inventory destInv = destChest.getInventory();
                    
                    for (ItemStack item : items) {
                        if (item != null && item.getType() != Material.AIR) {
                            destInv.addItem(item);
                        }
                    }
                    
                    p.sendMessage("§aEşyalar hedefe ulaştı!");
                }
            }
        }.runTaskLater(Main.getInstance(), 6000L); // 5 dakika = 6000 tick
    }
}

