package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.block.BlockFromToEvent;

/**
 * Grief Protection Listener
 * Piston, Hopper, Su/Lav akışı ve TNT koruması
 */
public class GriefProtectionListener implements Listener {
    private final TerritoryManager territoryManager;

    public GriefProtectionListener(TerritoryManager tm) {
        this.territoryManager = tm;
    }

    // ========== PISTON KORUMASI ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // Pistonun ittiği bloklar başka bir klanın bölgesine giriyor mu?
        for (Block block : event.getBlocks()) {
            Block targetBlock = block.getRelative(event.getDirection());
            Clan targetOwner = territoryManager.getTerritoryOwner(targetBlock.getLocation());
            Clan pistonOwner = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
            
            // Farklı bölgeler arası piston hareketi engelle
            if (targetOwner != null && pistonOwner != null && !targetOwner.equals(pistonOwner)) {
                event.setCancelled(true);
                return;
            }
            
            // Pistonun kendisi başka bir bölgedeyse ve ittiği blok da başka bölgeye giriyorsa engelle
            if (targetOwner != null && (pistonOwner == null || !targetOwner.equals(pistonOwner))) {
                // Pistonun ittiği blok korumalı bölgeye giriyor
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // Pistonun çektiği blok başka bir klanın bölgesinden mi geliyor?
        if (event.getBlocks().isEmpty()) return;
        
        Block pulledBlock = event.getBlocks().get(0);
        Clan pulledOwner = territoryManager.getTerritoryOwner(pulledBlock.getLocation());
        Clan pistonOwner = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
        
        // Farklı bölgeler arası piston hareketi engelle
        if (pulledOwner != null && pistonOwner != null && !pulledOwner.equals(pistonOwner)) {
            event.setCancelled(true);
        }
    }

    // ========== HUNI (HOPPER) HIRSIZLIGI ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onHopperSteal(InventoryMoveItemEvent event) {
        // Eğer eşya bir sandıktan çıkıp huniye gidiyorsa ve o sandık kilitli bir bölgedeyse iptal et
        org.bukkit.Location sourceLoc = event.getSource().getLocation();
        org.bukkit.Location destinationLoc = event.getDestination().getLocation();
        
        if (sourceLoc == null || destinationLoc == null) return;
        
        Clan sourceOwner = territoryManager.getTerritoryOwner(sourceLoc);
        Clan destOwner = territoryManager.getTerritoryOwner(destinationLoc);
        
        // Kaynak bölge korumalıysa ve hedef farklı bir bölgeyse engelle
        if (sourceOwner != null) {
            // Kaynak bölge korumalı - huni hırsızlığını engelle
            if (destOwner == null || !destOwner.equals(sourceOwner)) {
                event.setCancelled(true);
            }
        }
    }

    // ========== SU VE LAV AKIŞI ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onLiquidFlow(BlockFromToEvent event) {
        // Akışkanın gideceği blok bir klan bölgesindeyse iptal et
        Block toBlock = event.getToBlock();
        Block fromBlock = event.getBlock();
        
        Clan toOwner = territoryManager.getTerritoryOwner(toBlock.getLocation());
        Clan fromOwner = territoryManager.getTerritoryOwner(fromBlock.getLocation());
        
        // Farklı bölgeler arası akışkan hareketi engelle
        if (toOwner != null) {
            // Hedef bölge korumalı
            if (fromOwner == null || !fromOwner.equals(toOwner)) {
                // Kaynak bölge korumasız veya farklı bölge - akışı engelle
                event.setCancelled(true);
            }
        }
    }

    // ========== TNT VE PATLAMA KORUMASI ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosion(EntityExplodeEvent event) {
        // Patlamadan etkilenen bloklar listesini kontrol et
        java.util.List<Block> blocksToRemove = new java.util.ArrayList<>();
        
        for (Block block : event.blockList()) {
            Clan owner = territoryManager.getTerritoryOwner(block.getLocation());
            
            // Eğer blok korumalı bir bölgedeyse patlamadan etkilenmesin
            if (owner != null) {
                blocksToRemove.add(block);
            }
        }
        
        // Korumalı blokları listeden çıkar
        event.blockList().removeAll(blocksToRemove);
    }
}

