package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.handler.DisasterHandler;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterEntityAI;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * Grup Felaket Handler (30 adet orta güçte)
 * ZombieHorde, SkeletonLegion, SpiderSwarm için
 */
public class GroupDisasterHandler implements DisasterHandler {
    protected final TerritoryManager territoryManager;
    
    public GroupDisasterHandler(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Grup felaketler için tek entity handle edilmez
        // handleGroup kullanılır
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        if (entities == null || entities.isEmpty()) return;
        
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Grup AI ile hedefe git
        DisasterEntityAI.updateGroupAI(entities, target, config);
        
        // Her entity için chunk yükleme ve temel işlemler
        for (Entity entity : entities) {
            if (entity == null || entity.isDead() || !entity.isValid()) continue;
            
            Location current = entity.getLocation();
            DisasterUtils.loadChunk(current, true);
        }
    }
}
