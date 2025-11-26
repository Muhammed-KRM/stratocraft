package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;

public class SiegeManager {
    // Savunan Klan ID -> Saldıran Klan ID
    private final Map<Clan, Clan> activeSieges = new HashMap<>();
    private BuffManager buffManager;

    public void setBuffManager(BuffManager bm) {
        this.buffManager = bm;
    }

    public void startSiege(Clan attacker, Clan defender) {
        activeSieges.put(defender, attacker);
        Bukkit.broadcastMessage("§4§lSAVAŞ İLANI! §e" + attacker.getName() + " klani, " + defender.getName() + " klanına savaş açtı!");
        // SiegeTimer burada başlatılmalı
    }

    public void endSiege(Clan winner, Clan loser) {
        activeSieges.remove(loser);
        Bukkit.broadcastMessage("§2§lZAFER! §a" + winner.getName() + " klanı, " + loser.getName() + " klanını fethetti!");
        
        // Ödül mantığı
        double loot = loser.getBalance() * 0.5;
        winner.deposit(loot);
        loser.withdraw(loot);
        
        // FATİH BUFF'I UYGULA
        if (buffManager != null) {
            buffManager.applyConquerorBuff(winner);
        }
    }

    public boolean isUnderSiege(Clan clan) {
        return activeSieges.containsKey(clan);
    }
}

