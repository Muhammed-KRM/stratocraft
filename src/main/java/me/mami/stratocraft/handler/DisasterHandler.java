package me.mami.stratocraft.handler;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * Felaket Handler Interface
 * Her felaket tipi için özel handler sınıfları bu interface'i implement eder
 */
public interface DisasterHandler {
    /**
     * Tek entity felaketini işle
     */
    void handle(Disaster disaster, Entity entity, DisasterConfig config);
    
    /**
     * Grup felaketini işle (30 adet veya 100-500 adet)
     */
    void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config);
}
