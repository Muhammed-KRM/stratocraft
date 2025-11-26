package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Phantom;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobRideTask extends BukkitRunnable {
    private final MobManager mobManager;
    private final Map<UUID, Long> wyvernFeedTimes = new HashMap<>(); // Wyvern beslenme zamanları
    private static final long FEED_INTERVAL = 1200L; // 60 saniye = 1200 tick

    public MobRideTask(MobManager mm) { this.mobManager = mm; }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isInsideVehicle()) {
                mobManager.handleRiding(p);
                
                // WYVERN BESLEME KONTROLÜ
                Entity vehicle = p.getVehicle();
                if (vehicle != null && vehicle instanceof Phantom) {
                    Phantom phantom = (Phantom) vehicle;
                    if (phantom.getCustomName() != null && phantom.getCustomName().contains("Wyvern")) {
                        UUID playerId = p.getUniqueId();
                        long currentTime = System.currentTimeMillis();
                        
                        // İlk kontrol veya 60 saniye geçti mi?
                        if (!wyvernFeedTimes.containsKey(playerId) || 
                            (currentTime - wyvernFeedTimes.get(playerId)) >= 60000) {
                            
                            // Oyuncunun envanterinde Kızıl Elmas var mı?
                            boolean hasRedDiamond = false;
                            for (ItemStack item : p.getInventory().getContents()) {
                                if (item != null && ItemManager.isCustomItem(item, "RED_DIAMOND")) {
                                    hasRedDiamond = true;
                                    item.setAmount(item.getAmount() - 1);
                                    break;
                                }
                            }
                            
                            if (!hasRedDiamond) {
                                // Kızıl Elmas yok - Wyvern saldırır
                                p.sendMessage("§c§lWyvern aç! Kızıl Elmas gerekli!");
                                vehicle.removePassenger(p);
                                if (vehicle instanceof LivingEntity) {
                                    ((LivingEntity) vehicle).setTarget(p);
                                }
                                wyvernFeedTimes.remove(playerId);
                            } else {
                                // Beslendi
                                wyvernFeedTimes.put(playerId, currentTime);
                                p.sendMessage("§aWyvern besledin! 60 saniye daha uçabilirsin.");
                            }
                        }
                    }
                }
            } else {
                // Araçtan indi, beslenme zamanını temizle
                wyvernFeedTimes.remove(p.getUniqueId());
            }
        }
    }
}

