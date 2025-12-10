package me.mami.stratocraft.handler;

import me.mami.stratocraft.handler.impl.*;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Disaster;

import java.util.HashMap;
import java.util.Map;

/**
 * Felaket Handler Kayıt Defteri
 * Her felaket tipi için doğru handler'ı döndürür
 */
public class DisasterHandlerRegistry {
    private final Map<Disaster.Type, DisasterHandler> handlers = new HashMap<>();
    private final TerritoryManager territoryManager;
    
    public DisasterHandlerRegistry(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
        registerHandlers();
    }
    
    /**
     * Tüm handler'ları kaydet
     */
    private void registerHandlers() {
        // Tek Boss Felaketler (Felaket Bossları)
        handlers.put(Disaster.Type.CATASTROPHIC_TITAN, new TitanGolemHandler(territoryManager));
        handlers.put(Disaster.Type.CATASTROPHIC_ABYSSAL_WORM, new AbyssalWormHandler(territoryManager));
        handlers.put(Disaster.Type.CATASTROPHIC_CHAOS_DRAGON, new ChaosDragonHandler(territoryManager));
        handlers.put(Disaster.Type.CATASTROPHIC_VOID_TITAN, new VoidTitanHandler(territoryManager));
        handlers.put(Disaster.Type.CATASTROPHIC_ICE_LEVIATHAN, new IceLeviathanHandler(territoryManager));
        
        // Grup Felaketler (30 adet)
        handlers.put(Disaster.Type.ZOMBIE_HORDE, new GroupDisasterHandler(territoryManager));
        handlers.put(Disaster.Type.SKELETON_LEGION, new GroupDisasterHandler(territoryManager));
        handlers.put(Disaster.Type.SPIDER_SWARM, new GroupDisasterHandler(territoryManager));
        
        // Mini Dalga Felaketler (100-500 adet)
        handlers.put(Disaster.Type.CREEPER_SWARM, new SwarmDisasterHandler(territoryManager));
        handlers.put(Disaster.Type.ZOMBIE_WAVE, new SwarmDisasterHandler(territoryManager));
        
        // Doğa Olayları
        handlers.put(Disaster.Type.SOLAR_FLARE, new SolarFlareHandler(territoryManager));
        handlers.put(Disaster.Type.EARTHQUAKE, new EarthquakeHandler(territoryManager));
        handlers.put(Disaster.Type.STORM, new StormHandler(territoryManager));
        handlers.put(Disaster.Type.METEOR_SHOWER, new MeteorShowerHandler(territoryManager));
        handlers.put(Disaster.Type.VOLCANIC_ERUPTION, new VolcanicEruptionHandler(territoryManager));
        
        // Mini Felaketler
        handlers.put(Disaster.Type.BOSS_BUFF_WAVE, new BossBuffWaveHandler(territoryManager));
        handlers.put(Disaster.Type.MOB_INVASION, new MobInvasionHandler(territoryManager));
        handlers.put(Disaster.Type.PLAYER_BUFF_WAVE, new PlayerBuffWaveHandler(territoryManager));
    }
    
    /**
     * Felaket tipi için handler al
     */
    public DisasterHandler getHandler(Disaster.Type type) {
        DisasterHandler handler = handlers.get(type);
        if (handler == null) {
            // Varsayılan handler (BaseCreatureHandler)
            return new BaseCreatureHandler(territoryManager);
        }
        return handler;
    }
}
