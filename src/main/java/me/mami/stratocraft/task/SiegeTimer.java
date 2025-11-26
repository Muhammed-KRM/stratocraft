package me.mami.stratocraft.task;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SiegeTimer extends BukkitRunnable {
    private final Clan defender;
    private int countdown;
    private final me.mami.stratocraft.Main plugin;

    public SiegeTimer(Clan defender, me.mami.stratocraft.Main plugin) {
        this.defender = defender;
        this.plugin = plugin;
        // Config'den warmup süresini al
        this.countdown = plugin.getConfigManager() != null ? 
            plugin.getConfigManager().getSiegeWarmupTime() : 300; // Varsayılan 5 dakika
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            Bukkit.broadcastMessage("§4§lSAVAŞ BAŞLADI! §c" + defender.getName() + " klanının korumaları kalktı! Artık saldırı serbest.");
            this.cancel();
            return;
        }

        if (countdown % 60 == 0 || countdown == 30 || countdown == 10) {
            Bukkit.broadcastMessage("§cKuşatma Başlangıcına Kalan Süre: " + countdown / 60 + " dakika.");
        }

        countdown--;
    }
}

