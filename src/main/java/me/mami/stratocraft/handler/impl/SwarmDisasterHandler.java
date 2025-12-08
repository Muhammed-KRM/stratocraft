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
 * Mini Dalga Felaket Handler (100-500 adet)
 * CreeperSwarm, ZombieWave için
 */
public class SwarmDisasterHandler implements DisasterHandler {
    protected final TerritoryManager territoryManager;
    
    public SwarmDisasterHandler(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Mini dalga felaketler için tek entity handle edilmez
        // handleGroup kullanılır
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        if (entities == null || entities.isEmpty()) return;
        
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // Performans için sadece bir kısmını işle (her tick farklı entity'ler)
        int processCount = Math.min(entities.size(), 50); // Her tick maksimum 50 entity işle
        
        for (int i = 0; i < processCount; i++) {
            Entity entity = entities.get((int)(System.currentTimeMillis() % entities.size()));
            if (entity == null || entity.isDead() || !entity.isValid()) continue;
            
            Location current = entity.getLocation();
            
            // AI ile hedefe git
            DisasterEntityAI.navigateToTarget(entity, target, config);
            
            // Chunk yükle
            DisasterUtils.loadChunk(current, true);
        }
    }
}
