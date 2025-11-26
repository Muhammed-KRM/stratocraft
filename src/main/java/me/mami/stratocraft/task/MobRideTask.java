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
    private final Map<UUID, Long> wyvernLastCheck = new HashMap<>(); // Son kontrol zamanı (optimizasyon)
    private static final long FEED_INTERVAL = 60000L; // 60 saniye (milisaniye)
    private static final long CHECK_INTERVAL = 1000L; // 1 saniyede bir kontrol et (optimizasyon)

    public MobRideTask(MobManager mm) { this.mobManager = mm; }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isInsideVehicle()) {
                mobManager.handleRiding(p);
                
                // WYVERN BESLEME KONTROLÜ (Optimize edilmiş: Her saniye kontrol)
                Entity vehicle = p.getVehicle();
                if (vehicle != null && vehicle instanceof Phantom) {
                    Phantom phantom = (Phantom) vehicle;
                    if (phantom.getCustomName() != null && phantom.getCustomName().contains("Wyvern")) {
                        UUID playerId = p.getUniqueId();
                        
                        // Son kontrol zamanını kontrol et (optimizasyon)
                        Long lastCheck = wyvernLastCheck.get(playerId);
                        if (lastCheck == null || (currentTime - lastCheck) >= CHECK_INTERVAL) {
                            wyvernLastCheck.put(playerId, currentTime);
                            
                            // İlk kontrol veya 60 saniye geçti mi?
                            if (!wyvernFeedTimes.containsKey(playerId) || 
                                (currentTime - wyvernFeedTimes.get(playerId)) >= FEED_INTERVAL) {
                                
                                // Oyuncunun envanterinde Kızıl Elmas var mı?
                                boolean hasRedDiamond = false;
                                for (ItemStack item : p.getInventory().getContents()) {
                                    if (item != null && ItemManager.isCustomItem(item, "RED_DIAMOND")) {
                                        hasRedDiamond = true;
                                        // Eşyayı sil
                                        if (item.getAmount() > 1) {
                                            item.setAmount(item.getAmount() - 1);
                                        } else {
                                            p.getInventory().removeItem(item);
                                        }
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
                                    wyvernLastCheck.remove(playerId);
                                } else {
                                    // Beslendi - 60 saniye daha uçabilir
                                    wyvernFeedTimes.put(playerId, currentTime);
                                    p.sendMessage("§aWyvern besledin! 60 saniye daha uçabilirsin.");
                                }
                            }
                        }
                    }
                }
            } else {
                // Araçtan indi, beslenme zamanını temizle
                wyvernFeedTimes.remove(p.getUniqueId());
                wyvernLastCheck.remove(p.getUniqueId());
            }
        }
    }
}

