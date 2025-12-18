package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Phantom;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobRideTask extends BukkitRunnable {
    private final MobManager mobManager;
    private final Map<UUID, Long> wyvernFeedTimes = new HashMap<>(); // Wyvern beslenme zamanları
    private final Map<UUID, Long> wyvernLastCheck = new HashMap<>(); // Son kontrol zamanı (optimizasyon)
    private final Map<UUID, Long> wyvernLastMessage = new HashMap<>(); // Son mesaj zamanı (chat spam önleme)
    private static final long FEED_INTERVAL = 60000L; // 60 saniye (milisaniye)
    private static final long CHECK_INTERVAL = 1000L; // 1 saniyede bir kontrol et (optimizasyon)
    private static final long MESSAGE_COOLDOWN = 3000L; // 3 saniyede bir mesaj gönder (chat spam önleme)

    public MobRideTask(MobManager mm) { this.mobManager = mm; }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        
        // ✅ OPTİMİZE: Oyuncu yoksa erken çıkış
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            return;
        }
        
        int playerCount = 0;
        for (Player p : onlinePlayers) {
            playerCount++;
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
                                    // Chat spam önleme: Son mesajdan 3 saniye geçtiyse gönder
                                    Long lastMsg = wyvernLastMessage.get(playerId);
                                    if (lastMsg == null || (currentTime - lastMsg) >= MESSAGE_COOLDOWN) {
                                        // ActionBar kullan (chat spam önleme) - try-catch ile güvenli
                                        try {
                                            p.spigot().sendMessage(
                                                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                                new net.md_5.bungee.api.chat.TextComponent("§c§lWyvern aç! Kızıl Elmas gerekli!")
                                            );
                                        } catch (Exception e) {
                                            // Spigot API yoksa normal mesaj gönder
                                            p.sendMessage("§c§lWyvern aç! Kızıl Elmas gerekli!");
                                        }
                                        wyvernLastMessage.put(playerId, currentTime);
                                    }
                                    
                                    vehicle.removePassenger(p);
                                    if (vehicle instanceof Mob mob) {
                                        mob.setTarget(p);
                                    }
                                    wyvernFeedTimes.remove(playerId);
                                    wyvernLastCheck.remove(playerId);
                                } else {
                                    // Beslendi - 60 saniye daha uçabilir
                                    wyvernFeedTimes.put(playerId, currentTime);
                                    // ActionBar kullan (chat spam önleme) - try-catch ile güvenli
                                    try {
                                        p.spigot().sendMessage(
                                            net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                            new net.md_5.bungee.api.chat.TextComponent("§aWyvern besledin! 60 saniye daha uçabilirsin.")
                                        );
                                    } catch (Exception e) {
                                        // Spigot API yoksa normal mesaj gönder
                                        p.sendMessage("§aWyvern besledin! 60 saniye daha uçabilirsin.");
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Araçtan indi, beslenme zamanını temizle
                wyvernFeedTimes.remove(p.getUniqueId());
                wyvernLastCheck.remove(p.getUniqueId());
                wyvernLastMessage.remove(p.getUniqueId());
            }
        }
        
    }
}

