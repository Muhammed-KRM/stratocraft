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
    
    /**
     * Özel yetenekleri kullan (faz bazlı)
     * @param disaster Felaket
     * @param entity Entity
     * @param config Config
     * @param phase Mevcut faz
     */
    default void useSpecialAbilities(Disaster disaster, Entity entity, DisasterConfig config, me.mami.stratocraft.model.DisasterPhase phase) {
        // Varsayılan implementasyon boş, handler'lar override edebilir
    }
    
    /**
     * Çevre değişimi yeteneği (bazı felaketler için)
     * @param disaster Felaket
     * @param entity Entity
     * @param config Config
     */
    default void changeEnvironment(Disaster disaster, Entity entity, DisasterConfig config) {
        // Varsayılan implementasyon boş, handler'lar override edebilir
    }
}
