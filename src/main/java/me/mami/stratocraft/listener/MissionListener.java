package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.MissionManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MissionListener implements Listener {
    private final MissionManager missionManager;

    public MissionListener(MissionManager mm) { this.missionManager = mm; }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            missionManager.handleKill(event.getEntity().getKiller(), event.getEntityType());
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOW)
    public void onTotemInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.LODESTONE) {
            Block base = event.getClickedBlock().getRelative(0, -1, 0);
            if (base.getType() == Material.COBBLESTONE || base.getType() == Material.IRON_BLOCK || base.getType() == Material.DIAMOND_BLOCK) {
                missionManager.interactWithTotem(event.getPlayer(), base.getType());
            }
        }
    }
}

