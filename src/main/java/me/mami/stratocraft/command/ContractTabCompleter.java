package me.mami.stratocraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tab Completer for /kontrat command
 */
public class ContractTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList(
                "list", "olustur", "teslim", "kabul"
            ));
            
            String input = args[0].toLowerCase();
            options.removeIf(option -> !option.toLowerCase().startsWith(input));
            
            return options;
        }
        
        return new ArrayList<>();
    }
}

