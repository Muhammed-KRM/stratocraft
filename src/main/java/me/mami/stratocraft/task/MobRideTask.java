package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MobRideTask extends BukkitRunnable {
    private final MobManager mobManager;

    public MobRideTask(MobManager mm) { this.mobManager = mm; }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isInsideVehicle()) {
                mobManager.handleRiding(p);
            }
        }
    }
}

