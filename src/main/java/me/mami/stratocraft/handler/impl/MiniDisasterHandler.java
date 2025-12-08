package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.handler.DisasterHandler;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Mini Felaket Handler Base
 * BossBuffWave, MobInvasion, PlayerBuffWave için
 */
public class MiniDisasterHandler implements DisasterHandler {
    protected final TerritoryManager territoryManager;
    
    public MiniDisasterHandler(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Mini felaketler entity kullanmaz
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        // Mini felaketler entity kullanmaz
    }
}
