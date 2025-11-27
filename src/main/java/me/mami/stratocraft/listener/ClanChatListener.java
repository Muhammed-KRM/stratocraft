package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.util.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * ClanChatListener - Klan sohbeti sistemi
 * Oyuncular mesajlarının başına "!" koyarak sadece klan üyelerine mesaj gönderebilir
 * Diğer sohbet pluginleriyle uyumlu çalışır (e.getFormat() kullanmaz, sadece mesajı iptal eder)
 */
public class ClanChatListener implements Listener {
    private final ClanManager clanManager;
    private final LangManager langManager;

    public ClanChatListener(ClanManager cm, LangManager lm) {
        this.clanManager = cm;
        this.langManager = lm;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        
        // Eğer mesaj "!" ile başlıyorsa klan sohbeti
        if (!message.startsWith("!")) return;
        
        Player player = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        
        if (clan == null) {
            player.sendMessage(langManager.getMessage("clan-chat.not-in-clan"));
            event.setCancelled(true);
            return;
        }
        
        // Normal sohbeti iptal et (diğer pluginlerin formatını bozmamak için)
        event.setCancelled(true);
        
        // Mesajı düzenle (! işaretini kaldır)
        String clanMessage = message.substring(1).trim();
        if (clanMessage.isEmpty()) {
            player.sendMessage(langManager.getMessage("clan-chat.empty-message"));
            return;
        }
        
        // LangManager'dan format al
        String formattedMessage = langManager.getMessage("clan-chat.format", 
            "player", player.getName(), 
            "message", clanMessage);
        String consoleMessage = langManager.getMessage("clan-chat.console-format",
            "clan", clan.getName(),
            "player", player.getName(),
            "message", clanMessage);
        
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
                Bukkit.getConsoleSender().sendMessage(consoleMessage);
            });
        }
    }
}

