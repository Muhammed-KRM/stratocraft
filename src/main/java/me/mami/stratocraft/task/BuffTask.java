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

                // ZEHİR REAKTÖRÜ - Düşmanlara zehir ver
                if (s.getType() == Structure.Type.POISON_REACTOR && !isFriendly && p.getGameMode() != GameMode.CREATIVE) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    if (s.getLevel() >= 3) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    }
                }

                // ERKEN UYARI RADARI - Düşman yaklaşımı uyarısı
                if (s.getType() == Structure.Type.WATCHTOWER && isFriendly) {
                    // 200 blok yarıçapında düşman kontrolü
                    for (Player nearby : Bukkit.getOnlinePlayers()) {
                        if (nearby.getLocation().distance(p.getLocation()) <= 200 && 
                            !territoryClan.getMembers().containsKey(nearby.getUniqueId())) {
                            p.sendMessage("§c§lDİKKAT! §7" + nearby.getName() + " bölgenize yaklaşıyor! (" + 
                                (int)nearby.getLocation().distance(p.getLocation()) + " blok)");
                        }
                    }
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

