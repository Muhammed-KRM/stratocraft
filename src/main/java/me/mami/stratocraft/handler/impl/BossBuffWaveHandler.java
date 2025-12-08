package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Boss Buff Dalgası Handler
 * Tüm bosslara güçlendirme efekti verir
 */
public class BossBuffWaveHandler extends MiniDisasterHandler {
    
    public BossBuffWaveHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Tüm bosslara buff ver
        me.mami.stratocraft.manager.BossManager bossManager = Main.getInstance().getBossManager();
        if (bossManager != null) {
            // BossManager'dan tüm bossları al ve buff ver
            // (BossManager API'sine göre implement edilebilir)
        }
        
        // Dünyadaki tüm canlı entity'lere buff ver (boss olabilir)
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof LivingEntity && !(e instanceof org.bukkit.entity.Player)) {
                    LivingEntity living = (LivingEntity) e;
                    // Güçlendirme efektleri
                    living.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 1, false, false));
                    living.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 1, false, false));
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1, false, false));
                }
            }
        }
    }
}
