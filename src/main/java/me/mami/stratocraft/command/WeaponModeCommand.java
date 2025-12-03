package me.mami.stratocraft.command;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.listener.SpecialWeaponListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Silah modu değiştirme komutu
 */
public class WeaponModeCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    
    public WeaponModeCommand(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular için!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§cKullanım: /weaponmode <1|2|3>");
            player.sendMessage("§71. Blok Fırlatma");
            player.sendMessage("§72. Duvar Yapma");
            player.sendMessage("§73. Atılma/Patlama");
            return true;
        }
        
        try {
            int modeNumber = Integer.parseInt(args[0]);
            
            // SpecialWeaponListener'ı bul
            SpecialWeaponListener listener = getWeaponListener();
            if (listener == null) {
                player.sendMessage("§cSistem hatası! Lütfen yöneticiye bildirin.");
                return true;
            }
            
            listener.changeWeaponMode(player, modeNumber);
        } catch (NumberFormatException e) {
            player.sendMessage("§cGeçersiz mod numarası! 1, 2 veya 3 seçin.");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("1", "2", "3");
        }
        return new ArrayList<>();
    }
    
    /**
     * SpecialWeaponListener'ı bul
     */
    private SpecialWeaponListener getWeaponListener() {
        // Main.java'dan listener'ı al
        if (plugin.getSpecialWeaponListener() != null) {
            return plugin.getSpecialWeaponListener();
        }
        return null;
    }
}

