package me.mami.stratocraft.handler.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import me.mami.stratocraft.handler.DisasterHandler;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterEntityAI;

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
    
    // ✅ PERFORMANS: Tick sayacı (her 5 tick'te bir çalıştır)
    private static final Map<UUID, Integer> tickCounters = new java.util.concurrent.ConcurrentHashMap<>();
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        if (entities == null || entities.isEmpty()) return;
        
        Location target = disaster.getTargetCrystal() != null ? disaster.getTargetCrystal() : disaster.getTarget();
        if (target == null) return;
        
        // ✅ PERFORMANS: Felaket UUID'si al
        UUID disasterId = disaster.getEntity() != null ? disaster.getEntity().getUniqueId() : UUID.randomUUID();
        
        // ✅ PERFORMANS: Her 5 tick'te bir çalıştır (saniyede 4 kez)
        int tickCount = tickCounters.getOrDefault(disasterId, 0);
        tickCounters.put(disasterId, tickCount + 1);
        if (tickCount % 5 != 0) {
            return; // Bu tick'te çalıştırma
        }
        
        // ✅ PERFORMANS: Daha az entity işle (50 -> 20)
        int processCount = Math.min(entities.size(), 20); // Her tick maksimum 20 entity işle
        
        for (int i = 0; i < processCount; i++) {
            Entity entity = entities.get((int)(System.currentTimeMillis() % entities.size()));
            if (entity == null || entity.isDead() || !entity.isValid()) continue;
            
            // ✅ PERFORMANS: AI ile hedefe git (daha az sıklıkta)
            DisasterEntityAI.navigateToTarget(entity, target, config);
            
            // ✅ PERFORMANS: Chunk yükleme işlemini kaldır (çok ağır, force load yapıyor)
            // DisasterUtils.loadChunk(current, true); // KALDIRILDI
        }
    }
}
