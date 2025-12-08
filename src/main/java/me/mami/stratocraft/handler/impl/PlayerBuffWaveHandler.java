package me.mami.stratocraft.handler.impl;

import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.model.DisasterConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Oyuncu Buff Dalgası Handler
 * Tüm oyunculara geçici buff verir
 */
public class PlayerBuffWaveHandler extends MiniDisasterHandler {
    
    public PlayerBuffWaveHandler(me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        super(territoryManager);
    }
    
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Tüm oyunculara buff ver
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Güçlendirme efektleri
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6000, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 6000, 1, false, false));
            
            player.sendMessage("§a§lMini Felaket: Güçlendirme dalgası aldınız!");
        }
    }
}
