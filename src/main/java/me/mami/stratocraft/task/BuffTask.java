package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BuffTask extends BukkitRunnable {
    private final TerritoryManager territoryManager;
    private final ClanManager clanManager;

    public BuffTask(TerritoryManager tm) {
        this.territoryManager = tm;
        this.clanManager = tm.getClanManager();
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Clan territoryClan = territoryManager.getTerritoryOwner(p.getLocation());
            if (territoryClan == null) continue;

            boolean isFriendly = territoryClan.getMembers().containsKey(p.getUniqueId());

            for (Structure s : territoryClan.getStructures()) {
                if (s.getType() == Structure.Type.HEALING_BEACON && isFriendly) {
                    if (p.getHealth() < p.getMaxHealth()) {
                        p.setHealth(Math.min(p.getHealth() + 0.5, p.getMaxHealth()));
                    }
                }

                if (s.getType() == Structure.Type.GRAVITY_WELL && !isFriendly && p.getGameMode() != GameMode.CREATIVE) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 200));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                }

                if (s.getType() == Structure.Type.CORE) {
                    boolean anyOnline = clanManager.getAllClans().stream()
                            .filter(clan -> clan.equals(territoryClan))
                            .flatMap(clan -> clan.getMembers().keySet().stream())
                            .anyMatch(uuid -> Bukkit.getPlayer(uuid) != null);

                    if (!anyOnline) {
                        s.consumeFuel();
                    }
                }
            }
        }
    }
}

