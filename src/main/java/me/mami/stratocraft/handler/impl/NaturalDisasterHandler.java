package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.handler.DisasterHandler;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import me.mami.stratocraft.util.DisasterUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

/**
 * Doğa Olayı Handler Base
 * SolarFlare, Earthquake, Storm için temel sınıf
 */
public class NaturalDisasterHandler implements DisasterHandler {
    protected final TerritoryManager territoryManager;
    protected final Random random = new Random();
    
    public NaturalDisasterHandler(TerritoryManager territoryManager) {
        this.territoryManager = territoryManager;
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Doğa olayları entity kullanmaz
    }
    
    @Override
    public void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config) {
        // Doğa olayları entity kullanmaz
    }
}
