package me.mami.stratocraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab Completer for /klan command
 */
public class ClanTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // İlk argüman: komut seçenekleri
            List<String> options = new ArrayList<>(Arrays.asList(
                "kur", "davet", "ayril", "bilgi", "market", "menü", "üyeler", "bakiye"
            ));
            
            // Kullanıcının yazdığına göre filtrele
            String input = args[0].toLowerCase();
            options.removeIf(option -> !option.toLowerCase().startsWith(input));
            
            return options;
        } else if (args.length == 2) {
            // İkinci argüman: komut tipine göre
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("davet") || subCommand.equals("üyeler")) {
                // Oyuncu isimleri öner (basit versiyon)
                return null; // Bukkit otomatik oyuncu isimlerini önerir
            }
        }
        
        return new ArrayList<>(); // Boş liste döndür
    }
}

