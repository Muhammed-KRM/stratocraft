package me.mami.stratocraft.command;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MobManager;
import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.model.Disaster;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommandExecutor implements CommandExecutor, TabCompleter {
    
    private final Main plugin;
    
    public AdminCommandExecutor(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("stratocraft.admin")) {
            sender.sendMessage("§cBu komutu kullanmak için yetkiniz yok!");
            return true;
        }
        
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        Player p = (Player) sender;
        
        switch (args[0].toLowerCase()) {
            case "give":
                return handleGive(p, args);
            case "spawn":
                return handleSpawn(p, args);
            case "disaster":
                return handleDisaster(p, args);
            case "list":
                return handleList(p, args);
            default:
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleGive(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft give <item> [miktar]");
            p.sendMessage("§7Örnek: /stratocraft give blueprint 64");
            return true;
        }
        
        String itemName = args[1].toLowerCase();
        int amount = args.length > 2 ? parseInt(args[2], 1) : 1;
        
        ItemStack item = getItemByName(itemName);
        if (item == null) {
            p.sendMessage("§cGeçersiz item! /stratocraft list items komutu ile listeyi görebilirsiniz.");
            return true;
        }
        
        item.setAmount(amount);
        
        // Envanter doluysa yere düşür
        java.util.HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
            p.sendMessage("§a" + amount + "x " + getItemDisplayName(itemName) + " envanterinize eklendi! (Envanter dolu, bazıları yere düştü)");
        } else {
            p.sendMessage("§a" + amount + "x " + getItemDisplayName(itemName) + " envanterinize eklendi!");
        }
        return true;
    }
    
    private boolean handleSpawn(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft spawn <mob>");
            p.sendMessage("§7Örnek: /stratocraft spawn hell_dragon");
            return true;
        }
        
        String mobName = args[1].toLowerCase();
        MobManager mobManager = plugin.getMobManager();
        
        switch (mobName) {
            case "hell_dragon":
            case "ejder":
            case "cehennem_ejderi":
                mobManager.spawnHellDragon(p.getLocation(), p);
                p.sendMessage("§aCehennem Ejderi çağrıldı!");
                break;
            case "terror_worm":
            case "solucan":
            case "toprak_solucani":
                mobManager.spawnTerrorWorm(p.getLocation(), p);
                p.sendMessage("§aToprak Solucanı çağrıldı!");
                break;
            case "war_bear":
            case "ayi":
            case "savas_ayisi":
                mobManager.spawnWarBear(p.getLocation(), p);
                p.sendMessage("§aSavaş Ayısı çağrıldı!");
                break;
            case "shadow_panther":
            case "panter":
            case "golge_panteri":
                mobManager.spawnShadowPanther(p.getLocation(), p);
                p.sendMessage("§aGölge Panteri çağrıldı!");
                break;
            case "wyvern":
            case "wyvern":
                mobManager.spawnWyvern(p.getLocation(), p);
                p.sendMessage("§aWyvern çağrıldı!");
                break;
            default:
                p.sendMessage("§cGeçersiz mob! /stratocraft list mobs komutu ile listeyi görebilirsiniz.");
                return true;
        }
        return true;
    }
    
    private boolean handleDisaster(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft disaster <type>");
            p.sendMessage("§7Örnek: /stratocraft disaster titan_golem");
            return true;
        }
        
        String disasterName = args[1].toUpperCase();
        DisasterManager disasterManager = plugin.getDisasterManager();
        
        try {
            Disaster.Type type = Disaster.Type.valueOf(disasterName);
            disasterManager.triggerDisaster(type);
            p.sendMessage("§aFelaket tetiklendi: " + getDisasterDisplayName(disasterName));
            return true;
        } catch (IllegalArgumentException e) {
            p.sendMessage("§cGeçersiz felaket tipi! /stratocraft list disasters komutu ile listeyi görebilirsiniz.");
            return true;
        }
    }
    
    private boolean handleList(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft list <items|mobs|disasters|all>");
            return true;
        }
        
        String listType = args[1].toLowerCase();
        
        switch (listType) {
            case "items":
                showItemsList(p);
                break;
            case "mobs":
                showMobsList(p);
                break;
            case "disasters":
                showDisastersList(p);
                break;
            case "all":
                showItemsList(p);
                showMobsList(p);
                showDisastersList(p);
                break;
            default:
                p.sendMessage("§cGeçersiz liste tipi! <items|mobs|disasters|all>");
                return true;
        }
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== STRATOCRAFT ADMIN KOMUTLARI ===");
        sender.sendMessage("§e/stratocraft help §7- Bu yardım menüsünü gösterir");
        sender.sendMessage("§e/stratocraft give <item> [miktar] §7- Özel item verir");
        sender.sendMessage("§e/stratocraft spawn <mob> §7- Özel canlı çağırır");
        sender.sendMessage("§e/stratocraft disaster <type> §7- Felaket tetikler");
        sender.sendMessage("§e/stratocraft list <items|mobs|disasters|all> §7- Listeleri gösterir");
        sender.sendMessage("");
        sender.sendMessage("§7Örnekler:");
        sender.sendMessage("§7  /stratocraft give blueprint 64");
        sender.sendMessage("§7  /stratocraft spawn hell_dragon");
        sender.sendMessage("§7  /stratocraft disaster titan_golem");
        sender.sendMessage("§7  /stratocraft list all");
    }
    
    private void showItemsList(Player p) {
        p.sendMessage("§6§l=== ÖZEL İTEMLAR ===");
        p.sendMessage("§e1. §7blueprint §7- Mühendis Şeması");
        p.sendMessage("§e2. §7lightning_core §7- Yıldırım Çekirdeği");
        p.sendMessage("§e3. §7titanium_ore §7- Titanyum Parçası");
        p.sendMessage("§e4. §7titanium_ingot §7- Titanyum Külçesi");
        p.sendMessage("§e5. §7dark_matter §7- Karanlık Madde");
        p.sendMessage("§e6. §7red_diamond §7- Kızıl Elmas");
        p.sendMessage("§e7. §7ruby §7- Yakut");
        p.sendMessage("§e8. §7adamantite §7- Adamantite");
        p.sendMessage("§e9. §7star_core §7- Yıldız Çekirdeği");
        p.sendMessage("§e10. §7flame_amplifier §7- Alev Amplifikatörü");
        p.sendMessage("§e11. §7devil_horn §7- Şeytan Boynuzu");
        p.sendMessage("§e12. §7devil_snake_eye §7- İblis Yılanın Gözü");
        p.sendMessage("§e13. §7recipe_tectonic §7- Tarif: Tektonik Sabitleyici");
        p.sendMessage("§e14. §7war_fan §7- Savaş Yelpazesi");
        p.sendMessage("§e15. §7tower_shield §7- Kule Kalkanı");
        p.sendMessage("§e16. §7hell_fruit §7- Cehennem Meyvesi");
        p.sendMessage("§e17. §7clan_crystal §7- Klan Kristali (YENİ)");
        p.sendMessage("§e18. §7clan_fence §7- Klan Çiti (YENİ)");
    }
    
    private void showMobsList(Player p) {
        p.sendMessage("§6§l=== ÖZEL CANLILAR ===");
        p.sendMessage("§e1. §7hell_dragon §7- Cehennem Ejderi");
        p.sendMessage("§e2. §7terror_worm §7- Toprak Solucanı");
        p.sendMessage("§e3. §7war_bear §7- Savaş Ayısı");
        p.sendMessage("§e4. §7shadow_panther §7- Gölge Panteri");
        p.sendMessage("§e5. §7wyvern §7- Wyvern");
    }
    
    private void showDisastersList(Player p) {
        p.sendMessage("§6§l=== FELAKETLER ===");
        p.sendMessage("§e1. §7titan_golem §7- Titan Golem");
        p.sendMessage("§e2. §7abyssal_worm §7- Hiçlik Solucanı");
        p.sendMessage("§e3. §7solar_flare §7- Güneş Fırtınası");
    }
    
    private ItemStack getItemByName(String name) {
        switch (name.toLowerCase()) {
            case "blueprint":
            case "blueprint_paper":
            case "mühendis_şeması":
                return ItemManager.BLUEPRINT_PAPER != null ? ItemManager.BLUEPRINT_PAPER.clone() : null;
            case "lightning_core":
            case "yıldırım_çekirdeği":
                return ItemManager.LIGHTNING_CORE != null ? ItemManager.LIGHTNING_CORE.clone() : null;
            case "titanium_ore":
            case "titanium":
            case "titanyum_parçası":
                return ItemManager.TITANIUM_ORE != null ? ItemManager.TITANIUM_ORE.clone() : null;
            case "titanium_ingot":
            case "titanyum_külçesi":
                return ItemManager.TITANIUM_INGOT != null ? ItemManager.TITANIUM_INGOT.clone() : null;
            case "dark_matter":
            case "karanlık_madde":
                return ItemManager.DARK_MATTER != null ? ItemManager.DARK_MATTER.clone() : null;
            case "red_diamond":
            case "kızıl_elmas":
                return ItemManager.RED_DIAMOND != null ? ItemManager.RED_DIAMOND.clone() : null;
            case "ruby":
            case "yakut":
                return ItemManager.RUBY != null ? ItemManager.RUBY.clone() : null;
            case "adamantite":
                return ItemManager.ADAMANTITE != null ? ItemManager.ADAMANTITE.clone() : null;
            case "star_core":
            case "yıldız_çekirdeği":
                return ItemManager.STAR_CORE != null ? ItemManager.STAR_CORE.clone() : null;
            case "flame_amplifier":
            case "alev_amplifikatörü":
                return ItemManager.FLAME_AMPLIFIER != null ? ItemManager.FLAME_AMPLIFIER.clone() : null;
            case "devil_horn":
            case "şeytan_boynuzu":
                return ItemManager.DEVIL_HORN != null ? ItemManager.DEVIL_HORN.clone() : null;
            case "devil_snake_eye":
            case "iblis_yılanın_gözü":
                return ItemManager.DEVIL_SNAKE_EYE != null ? ItemManager.DEVIL_SNAKE_EYE.clone() : null;
            case "recipe_tectonic":
            case "recipe_book_tectonic":
            case "tarif_tektonik":
                return ItemManager.RECIPE_BOOK_TECTONIC != null ? ItemManager.RECIPE_BOOK_TECTONIC.clone() : null;
            case "war_fan":
            case "savaş_yelpazesi":
                return ItemManager.WAR_FAN != null ? ItemManager.WAR_FAN.clone() : null;
            case "tower_shield":
            case "kule_kalkanı":
                return ItemManager.TOWER_SHIELD != null ? ItemManager.TOWER_SHIELD.clone() : null;
            case "hell_fruit":
            case "cehennem_meyvesi":
                return ItemManager.HELL_FRUIT != null ? ItemManager.HELL_FRUIT.clone() : null;
            case "clan_crystal":
            case "klan_kristali":
                return createClanCrystal();
            case "clan_fence":
            case "klan_çiti":
            case "klan_citi":
                return createClanFence();
            default:
                return null;
        }
    }
    
    private ItemStack createClanCrystal() {
        ItemStack crystal = new ItemStack(Material.END_CRYSTAL);
        org.bukkit.inventory.meta.ItemMeta meta = crystal.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lKlan Kristali");
            meta.setLore(Arrays.asList("§7Klan kurmak için kullanılır.", "§7Etrafı Klan Çiti ile çevrili", "§7bir alana koyulmalıdır."));
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "clan_item");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, "CRYSTAL");
            crystal.setItemMeta(meta);
        }
        return crystal;
    }
    
    private ItemStack createClanFence() {
        ItemStack fence = new ItemStack(Material.OAK_FENCE);
        org.bukkit.inventory.meta.ItemMeta meta = fence.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lKlan Çiti");
            meta.setLore(Arrays.asList("§7Klan bölgesi sınırlarını belirler."));
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "clan_item");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, "FENCE");
            fence.setItemMeta(meta);
        }
        return fence;
    }
    
    private String getItemDisplayName(String name) {
        String lowerName = name.toLowerCase();
        // Alternatif isimleri de kontrol et
        if (lowerName.contains("blueprint") || lowerName.contains("mühendis") || lowerName.contains("şema")) {
            return "Mühendis Şeması";
        }
        if (lowerName.contains("lightning") || lowerName.contains("yıldırım") || lowerName.contains("çekirdek")) {
            return "Yıldırım Çekirdeği";
        }
        if (lowerName.contains("titanium_ore") || lowerName.contains("titanyum_parça")) {
            return "Titanyum Parçası";
        }
        if (lowerName.contains("titanium_ingot") || lowerName.contains("titanyum_külçe")) {
            return "Titanyum Külçesi";
        }
        if (lowerName.contains("dark_matter") || lowerName.contains("karanlık_madde")) {
            return "Karanlık Madde";
        }
        if (lowerName.contains("red_diamond") || lowerName.contains("kızıl_elmas")) {
            return "Kızıl Elmas";
        }
        if (lowerName.contains("ruby") || lowerName.contains("yakut")) {
            return "Yakut";
        }
        if (lowerName.contains("adamantite")) {
            return "Adamantite";
        }
        if (lowerName.contains("star_core") || lowerName.contains("yıldız_çekirdeği")) {
            return "Yıldız Çekirdeği";
        }
        if (lowerName.contains("flame_amplifier") || lowerName.contains("alev_amplifikatör")) {
            return "Alev Amplifikatörü";
        }
        if (lowerName.contains("devil_horn") || lowerName.contains("şeytan_boynuzu")) {
            return "Şeytan Boynuzu";
        }
        if (lowerName.contains("devil_snake_eye") || lowerName.contains("iblis_yılanın_gözü")) {
            return "İblis Yılanın Gözü";
        }
        if (lowerName.contains("recipe_tectonic") || lowerName.contains("tarif_tektonik")) {
            return "Tarif: Tektonik Sabitleyici";
        }
        if (lowerName.contains("war_fan") || lowerName.contains("savaş_yelpazesi")) {
            return "Savaş Yelpazesi";
        }
        if (lowerName.contains("tower_shield") || lowerName.contains("kule_kalkanı")) {
            return "Kule Kalkanı";
        }
        if (lowerName.contains("hell_fruit") || lowerName.contains("cehennem_meyvesi")) {
            return "Cehennem Meyvesi";
        }
        if (lowerName.contains("clan_crystal") || lowerName.contains("klan_kristali")) {
            return "Klan Kristali";
        }
        if (lowerName.contains("clan_fence") || lowerName.contains("klan_çiti") || lowerName.contains("klan_citi")) {
            return "Klan Çiti";
        }
        return name;
    }
    
    private String getDisasterDisplayName(String name) {
        switch (name.toUpperCase()) {
            case "TITAN_GOLEM": return "Titan Golem";
            case "ABYSSAL_WORM": return "Hiçlik Solucanı";
            case "SOLAR_FLARE": return "Güneş Fırtınası";
            default: return name;
        }
    }
    
    private int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("stratocraft.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("give", "spawn", "disaster", "list", "help").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give":
                    return Arrays.asList("blueprint", "lightning_core", "titanium_ore", "titanium_ingot", 
                            "dark_matter", "red_diamond", "ruby", "adamantite", "star_core", 
                            "flame_amplifier", "devil_horn", "devil_snake_eye", "recipe_tectonic", 
                            "war_fan", "tower_shield", "hell_fruit", "clan_crystal", "clan_fence").stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "spawn":
                    return Arrays.asList("hell_dragon", "terror_worm", "war_bear", "shadow_panther", "wyvern").stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "disaster":
                    return Arrays.asList("titan_golem", "abyssal_worm", "solar_flare").stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "list":
                    return Arrays.asList("items", "mobs", "disasters", "all").stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
}

