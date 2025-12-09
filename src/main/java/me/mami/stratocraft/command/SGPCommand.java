package me.mami.stratocraft.command;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.ClanPowerProfile;
import me.mami.stratocraft.model.PlayerPowerProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Güç Sistemi Komutları
 * 
 * Komutlar:
 * /sgp - Kendi gücünü göster
 * /sgp player <oyuncu> - Oyuncu gücü göster
 * /sgp clan - Klan gücü göster
 * /sgp top [limit] - Top oyuncular
 * /sgp components - Güç bileşenleri
 * /sgp help - Yardım
 */
public class SGPCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular için!");
            return true;
        }
        
        Player player = (Player) sender;
        Main plugin = Main.getInstance();
        
        if (plugin == null) {
            player.sendMessage("§cPlugin yüklenemedi!");
            return true;
        }
        
        StratocraftPowerSystem powerSystem = plugin.getStratocraftPowerSystem();
        if (powerSystem == null) {
            player.sendMessage("§cGüç sistemi yüklenemedi!");
            return true;
        }
        
        if (args.length == 0) {
            // Kendi gücünü göster
            showPlayerPower(player, player, powerSystem);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "me":
            case "self":
                showPlayerPower(player, player, powerSystem);
                break;
                
            case "player":
            case "p":
                if (args.length < 2) {
                    player.sendMessage("§cKullanım: /sgp player <oyuncu>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cOyuncu bulunamadı: " + args[1]);
                    return true;
                }
                showPlayerPower(player, target, powerSystem);
                break;
                
            case "clan":
            case "c":
                Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana ait değilsiniz!");
                    return true;
                }
                showClanPower(player, clan, powerSystem);
                break;
                
            case "top":
                int limit = 10;
                if (args.length > 1) {
                    try {
                        limit = Integer.parseInt(args[1]);
                        if (limit < 1 || limit > 100) {
                            limit = 10;
                        }
                    } catch (NumberFormatException e) {
                        limit = 10;
                    }
                }
                // ✅ PERFORMANS: SimpleRankingSystem kullan (cache'li)
                showTopPlayers(player, limit, plugin);
                break;
                
            case "components":
            case "comp":
                showPowerComponents(player, player, powerSystem);
                break;
                
            case "help":
            case "?":
                showHelp(player);
                break;
                
            default:
                player.sendMessage("§cBilinmeyen komut! /sgp help");
                break;
        }
        
        return true;
    }
    
    /**
     * Oyuncu gücü göster
     */
    private void showPlayerPower(Player viewer, Player target, StratocraftPowerSystem powerSystem) {
        PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(target);
        
        viewer.sendMessage("§6╔════════════════════════════════╗");
        viewer.sendMessage("§6║  " + target.getName() + " Güç Bilgileri");
        viewer.sendMessage("§6╠════════════════════════════════╣");
        viewer.sendMessage("§eToplam SGP: §f" + String.format("%.2f", profile.getTotalSGP()));
        viewer.sendMessage("§eCombat Power: §f" + String.format("%.2f", profile.getTotalCombatPower()));
        viewer.sendMessage("§eProgression Power: §f" + String.format("%.2f", profile.getTotalProgressionPower()));
        viewer.sendMessage("§eSeviye: §f" + profile.getPlayerLevel());
        viewer.sendMessage("§6╚════════════════════════════════╝");
    }
    
    /**
     * Klan gücü göster
     */
    private void showClanPower(Player viewer, Clan clan, StratocraftPowerSystem powerSystem) {
        ClanPowerProfile profile = powerSystem.calculateClanProfile(clan);
        
        viewer.sendMessage("§6╔════════════════════════════════╗");
        viewer.sendMessage("§6║  " + clan.getName() + " Klan Güç Bilgileri");
        viewer.sendMessage("§6╠════════════════════════════════╣");
        viewer.sendMessage("§eToplam Klan Gücü: §f" + String.format("%.2f", profile.getTotalClanPower()));
        viewer.sendMessage("§eKlan Seviyesi: §f" + profile.getClanLevel());
        viewer.sendMessage("§6╠════════════════════════════════╣");
        viewer.sendMessage("§7Üye Gücü: §f" + String.format("%.2f", profile.getMemberPowerSum()));
        viewer.sendMessage("§7Yapı Gücü: §f" + String.format("%.2f", profile.getStructurePower()));
        viewer.sendMessage("§7Ritüel Blok Gücü: §f" + String.format("%.2f", profile.getRitualBlockPower()));
        viewer.sendMessage("§7Ritüel Kaynak Gücü: §f" + String.format("%.2f", profile.getRitualResourcePower()));
        viewer.sendMessage("§6╚════════════════════════════════╝");
    }
    
    /**
     * Top oyuncuları göster
     * ✅ PERFORMANS: SimpleRankingSystem kullan (cache'li)
     */
    private void showTopPlayers(Player viewer, int limit, Main plugin) {
        me.mami.stratocraft.manager.SimpleRankingSystem rankingSystem = plugin.getSimpleRankingSystem();
        if (rankingSystem == null) {
            viewer.sendMessage("§cSıralama sistemi yüklenemedi!");
            return;
        }
        
        List<me.mami.stratocraft.manager.SimpleRankingSystem.PlayerRanking> rankings = 
            rankingSystem.getTopPlayers(limit);
        
        viewer.sendMessage("§6╔════════════════════════════════╗");
        viewer.sendMessage("§6║  Top " + limit + " Oyuncu");
        viewer.sendMessage("§6╠════════════════════════════════╣");
        
        for (int i = 0; i < rankings.size(); i++) {
            me.mami.stratocraft.manager.SimpleRankingSystem.PlayerRanking ranking = rankings.get(i);
            String medal = getMedal(i + 1);
            viewer.sendMessage(medal + " §e" + (i + 1) + ". §f" + ranking.getPlayerName() + 
                " §7- §f" + String.format("%.2f", ranking.getPower()) + " SGP §7(Seviye " + ranking.getLevel() + ")");
        }
        
        viewer.sendMessage("§6╚════════════════════════════════╝");
    }
    
    /**
     * Güç bileşenleri göster
     */
    private void showPowerComponents(Player viewer, Player target, StratocraftPowerSystem powerSystem) {
        PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(target);
        
        viewer.sendMessage("§6╔════════════════════════════════╗");
        viewer.sendMessage("§6║  " + target.getName() + " Güç Bileşenleri");
        viewer.sendMessage("§6╠════════════════════════════════╣");
        viewer.sendMessage("§7Eşya Gücü: §f" + String.format("%.2f", profile.getGearPower()));
        viewer.sendMessage("§7Ustalık Gücü: §f" + String.format("%.2f", profile.getTrainingPower()));
        viewer.sendMessage("§7Buff Gücü: §f" + String.format("%.2f", profile.getBuffPower()));
        viewer.sendMessage("§7Ritüel Gücü: §f" + String.format("%.2f", profile.getRitualPower()));
        viewer.sendMessage("§6╠════════════════════════════════╣");
        viewer.sendMessage("§eCombat Power: §f" + String.format("%.2f", profile.getTotalCombatPower()));
        viewer.sendMessage("§eProgression Power: §f" + String.format("%.2f", profile.getTotalProgressionPower()));
        viewer.sendMessage("§eToplam SGP: §f" + String.format("%.2f", profile.getTotalSGP()));
        viewer.sendMessage("§6╚════════════════════════════════╝");
    }
    
    /**
     * Yardım mesajı
     */
    private void showHelp(Player player) {
        player.sendMessage("§6╔════════════════════════════════╗");
        player.sendMessage("§6║  Güç Sistemi Komutları");
        player.sendMessage("§6╠════════════════════════════════╣");
        player.sendMessage("§e/sgp §7- Kendi gücünü göster");
        player.sendMessage("§e/sgp player <oyuncu> §7- Oyuncu gücü göster");
        player.sendMessage("§e/sgp clan §7- Klan gücü göster");
        player.sendMessage("§e/sgp top [limit] §7- Top oyuncular (varsayılan: 10)");
        player.sendMessage("§e/sgp components §7- Güç bileşenleri");
        player.sendMessage("§e/sgp help §7- Bu yardım mesajı");
        player.sendMessage("§6╚════════════════════════════════╝");
    }
    
    /**
     * Sıralama rozeti
     */
    private String getMedal(int rank) {
        switch (rank) {
            case 1: return "§6§l🥇";
            case 2: return "§7§l🥈";
            case 3: return "§c§l🥉";
            default: return "§7";
        }
    }
    
    // ✅ KALDIRILDI: SimpleRankingSystem.PlayerRanking kullanılıyor
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("me", "player", "clan", "top", "components", "help")
                .stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p"))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            return Arrays.asList("5", "10", "20", "50", "100");
        }
        
        return new ArrayList<>();
    }
}

