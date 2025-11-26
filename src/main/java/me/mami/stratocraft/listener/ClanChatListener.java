package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * ClanChatListener - Klan sohbeti sistemi
 * Oyuncular mesajlarının başına "!" koyarak sadece klan üyelerine mesaj gönderebilir
 */
public class ClanChatListener implements Listener {
    private final ClanManager clanManager;

    public ClanChatListener(ClanManager cm) {
        this.clanManager = cm;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        
        // Eğer mesaj "!" ile başlıyorsa klan sohbeti
        if (!message.startsWith("!")) return;
        
        Player player = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin! Klan sohbeti kullanamazsın.");
            event.setCancelled(true);
            return;
        }
        
        // Normal sohbeti iptal et
        event.setCancelled(true);
        
        // Mesajı düzenle (! işaretini kaldır)
        String clanMessage = message.substring(1).trim();
        if (clanMessage.isEmpty()) {
            player.sendMessage("§cBoş mesaj gönderemezsin!");
            return;
        }
        
        // Klan üyelerine gönder (Async event olduğu için sync'e al)
        String formattedMessage = "§b[Klan] §e" + player.getName() + "§7: §f" + clanMessage;
        String clanName = clan.getName();
        String playerName = player.getName();
        Clan finalClan = clan; // Final reference for lambda
        
        // Sync thread'e al (Bukkit API thread-safe değil)
        Main plugin = Main.getInstance();
        if (plugin != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (UUID memberId : finalClan.getMembers().keySet()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(formattedMessage);
                    }
                }
                
                // Konsola da yazdır (opsiyonel, adminler için)
                Bukkit.getConsoleSender().sendMessage("§b[Klan:" + clanName + "] §e" + playerName + "§7: §f" + clanMessage);
            });
        }
    }
}

