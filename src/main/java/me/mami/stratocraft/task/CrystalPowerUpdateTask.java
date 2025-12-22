package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Klan gücüne göre kristal canını günceller
 * Her 5 dakikada bir çalışır
 */
public class CrystalPowerUpdateTask extends BukkitRunnable {
    private final Main plugin;
    private final double HP_PER_POWER = 2.5; // Her güç birimi için 2.5 HP
    
    public CrystalPowerUpdateTask(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        if (plugin.getTerritoryManager() == null) return;
        
        for (Clan clan : plugin.getTerritoryManager().getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            // Toplam güç hesapla
            int totalPower = 0;
            for (Structure structure : clan.getStructures()) {
                totalPower += structure.getLevel();
            }
            
            // Can artışı hesapla
            double healthFromPower = totalPower * HP_PER_POWER;
            
            // Mevcut maksimum canı kontrol et
            double currentMaxHealth = clan.getCrystalMaxHealth();
            double baseHealth = 100.0; // Temel can
            
            // Güçten gelen can artışını hesapla
            double powerHealthIncrease = healthFromPower - (currentMaxHealth - baseHealth);
            
            // Eğer güç artışı varsa, canı artır
            if (powerHealthIncrease > 0) {
                clan.increaseCrystalMaxHealth(powerHealthIncrease);
                
                // Klan üyelerine bildir (sadece önemli artışlarda)
                if (powerHealthIncrease >= 50) {
                    for (UUID memberId : clan.getMembers().keySet()) {
                        Player member = org.bukkit.Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.sendMessage("§aKlan gücü arttı! Kristal canı +" + 
                                String.format("%.1f", powerHealthIncrease) + " HP");
                        }
                    }
                }
            }
        }
    }
}

