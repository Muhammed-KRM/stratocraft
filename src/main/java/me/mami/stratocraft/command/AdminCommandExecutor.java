package me.mami.stratocraft.command;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MobManager;
import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.util.LangManager;
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
    private final LangManager langManager;
    
    public AdminCommandExecutor(Main plugin) {
        this.plugin = plugin;
        this.langManager = plugin.getLangManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("stratocraft.admin")) {
            sender.sendMessage(langManager.getMessage("admin.no-permission"));
            return true;
        }
        
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return true;
        }
        
        // List komutu oyuncu olmayanlar için de çalışabilir
        if (args[0].equalsIgnoreCase("list")) {
            if (sender instanceof Player) {
                return handleList((Player) sender, args);
            } else {
                sender.sendMessage(langManager.getMessage("admin.player-only"));
                return true;
            }
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(langManager.getMessage("admin.player-only"));
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
            default:
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleGive(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(langManager.getMessage("admin.give-usage"));
            p.sendMessage(langManager.getMessage("admin.give-example"));
            return true;
        }
        
        String itemName = args[1].toLowerCase();
        int amount = args.length > 2 ? parseInt(args[2], 1) : 1;
        
        // Miktar kontrolü
        if (amount < 1) {
            amount = 1;
        }
        if (amount > 2304) { // Max stack size * 36 slots
            p.sendMessage("§cMiktar çok yüksek! Maksimum 2304.");
            return true;
        }
        
        ItemStack item = getItemByName(itemName);
        if (item == null) {
            p.sendMessage(langManager.getMessage("admin.give-invalid-item"));
            return true;
        }
        
        item.setAmount(amount);
        
        // Envanter doluysa yere düşür
        java.util.HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
            p.sendMessage(langManager.getMessage("admin.give-success-overflow", 
                "amount", String.valueOf(amount),
                "item", getItemDisplayName(itemName)));
        } else {
            p.sendMessage(langManager.getMessage("admin.give-success",
                "amount", String.valueOf(amount),
                "item", getItemDisplayName(itemName)));
        }
        return true;
    }
    
    private boolean handleSpawn(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(langManager.getMessage("admin.spawn-usage"));
            p.sendMessage(langManager.getMessage("admin.spawn-example"));
            return true;
        }
        
        String mobName = args[1].toLowerCase();
        MobManager mobManager = plugin.getMobManager();
        String mobDisplayName = "";
        
        switch (mobName) {
            // Eski moblar
            case "hell_dragon":
            case "ejder":
            case "cehennem_ejderi":
                mobManager.spawnHellDragon(p.getLocation(), p);
                mobDisplayName = "Cehennem Ejderi";
                break;
            case "terror_worm":
            case "solucan":
            case "toprak_solucani":
                mobManager.spawnTerrorWorm(p.getLocation(), p);
                mobDisplayName = "Toprak Solucanı";
                break;
            case "war_bear":
            case "ayi":
            case "savas_ayisi":
                mobManager.spawnWarBear(p.getLocation(), p);
                mobDisplayName = "Savaş Ayısı";
                break;
            case "shadow_panther":
            case "panter":
            case "golge_panteri":
                mobManager.spawnShadowPanther(p.getLocation(), p);
                mobDisplayName = "Gölge Panteri";
                break;
            case "wyvern":
                mobManager.spawnWyvern(p.getLocation(), p);
                mobDisplayName = "Wyvern";
                break;
            // Sık gelen canavarlar
            case "goblin":
                mobManager.spawnGoblin(p.getLocation());
                mobDisplayName = "Goblin";
                break;
            case "ork":
                mobManager.spawnOrk(p.getLocation());
                mobDisplayName = "Ork";
                break;
            case "troll":
                mobManager.spawnTroll(p.getLocation());
                mobDisplayName = "Troll";
                break;
            case "skeleton_knight":
            case "iskelet_sovalyesi":
                mobManager.spawnSkeletonKnight(p.getLocation());
                mobDisplayName = "İskelet Şövalye";
                break;
            case "dark_mage":
            case "karanlik_buyucu":
                mobManager.spawnDarkMage(p.getLocation());
                mobDisplayName = "Karanlık Büyücü";
                break;
            case "werewolf":
            case "kurt_adam":
                mobManager.spawnWerewolf(p.getLocation());
                mobDisplayName = "Kurt Adam";
                break;
            case "giant_spider":
            case "dev_orumcek":
                mobManager.spawnGiantSpider(p.getLocation());
                mobDisplayName = "Dev Örümcek";
                break;
            case "minotaur":
                mobManager.spawnMinotaur(p.getLocation());
                mobDisplayName = "Minotaur";
                break;
            case "harpy":
                mobManager.spawnHarpy(p.getLocation());
                mobDisplayName = "Harpy";
                break;
            case "basilisk":
                mobManager.spawnBasilisk(p.getLocation());
                mobDisplayName = "Basilisk";
                break;
            // Nadir canavarlar
            case "dragon":
            case "ejderha":
                mobManager.spawnDragon(p.getLocation());
                mobDisplayName = "Ejderha";
                break;
            case "trex":
            case "t_rex":
            case "tiranosaur":
                mobManager.spawnTRex(p.getLocation());
                mobDisplayName = "T-Rex";
                break;
            case "cyclops":
            case "tek_gozlu_dev":
                mobManager.spawnCyclops(p.getLocation());
                mobDisplayName = "Tek Gözlü Dev";
                break;
            case "griffin":
                mobManager.spawnGriffin(p.getLocation());
                mobDisplayName = "Griffin";
                break;
            case "wraith":
            case "hayalet":
                mobManager.spawnWraith(p.getLocation());
                mobDisplayName = "Hayalet";
                break;
            case "lich":
                mobManager.spawnLich(p.getLocation());
                mobDisplayName = "Lich";
                break;
            case "kraken":
                mobManager.spawnKraken(p.getLocation());
                mobDisplayName = "Kraken";
                break;
            case "phoenix":
            case "anka":
                mobManager.spawnPhoenix(p.getLocation());
                mobDisplayName = "Phoenix";
                break;
            case "hydra":
                mobManager.spawnHydra(p.getLocation());
                mobDisplayName = "Hydra";
                break;
            case "behemoth":
                mobManager.spawnBehemoth(p.getLocation());
                mobDisplayName = "Behemoth";
                break;
            default:
                p.sendMessage(langManager.getMessage("admin.spawn-invalid-mob"));
                return true;
        }
        
        p.sendMessage(langManager.getMessage("admin.spawn-success", "name", mobDisplayName));
        return true;
    }
    
    private boolean handleDisaster(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(langManager.getMessage("admin.disaster-usage"));
            p.sendMessage(langManager.getMessage("admin.disaster-example"));
            return true;
        }
        
        String disasterName = args[1].toUpperCase();
        DisasterManager disasterManager = plugin.getDisasterManager();
        
        try {
            Disaster.Type type = Disaster.Type.valueOf(disasterName);
            disasterManager.triggerDisaster(type);
            p.sendMessage(langManager.getMessage("admin.disaster-success", 
                "name", getDisasterDisplayName(disasterName)));
            return true;
        } catch (IllegalArgumentException e) {
            p.sendMessage(langManager.getMessage("admin.disaster-invalid"));
            return true;
        }
    }
    
    private boolean handleList(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(langManager.getMessage("admin.list-usage"));
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
                p.sendMessage(langManager.getMessage("admin.list-invalid-type"));
                return true;
        }
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(langManager.getMessage("admin.help-title"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "help", "description", "Bu yardım menüsünü gösterir"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "give <item> [miktar]", "description", "Özel item verir"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "spawn <mob>", "description", "Özel canlı çağırır"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "disaster <type>", "description", "Felaket tetikler"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "list <items|mobs|disasters|all>", "description", "Listeleri gösterir"));
        sender.sendMessage("");
        sender.sendMessage(langManager.getMessage("admin.help-examples"));
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
        p.sendMessage("§7--- Eski Moblar ---");
        p.sendMessage("§e1. §7hell_dragon §7- Cehennem Ejderi");
        p.sendMessage("§e2. §7terror_worm §7- Toprak Solucanı");
        p.sendMessage("§e3. §7war_bear §7- Savaş Ayısı");
        p.sendMessage("§e4. §7shadow_panther §7- Gölge Panteri");
        p.sendMessage("§e5. §7wyvern §7- Wyvern");
        p.sendMessage("§7--- Sık Gelen Canavarlar (10 tane) ---");
        p.sendMessage("§e6. §7goblin §7- Goblin");
        p.sendMessage("§e7. §7ork §7- Ork");
        p.sendMessage("§e8. §7troll §7- Troll");
        p.sendMessage("§e9. §7skeleton_knight §7- İskelet Şövalye");
        p.sendMessage("§e10. §7dark_mage §7- Karanlık Büyücü");
        p.sendMessage("§e11. §7werewolf §7- Kurt Adam");
        p.sendMessage("§e12. §7giant_spider §7- Dev Örümcek");
        p.sendMessage("§e13. §7minotaur §7- Minotaur");
        p.sendMessage("§e14. §7harpy §7- Harpy");
        p.sendMessage("§e15. §7basilisk §7- Basilisk");
        p.sendMessage("§7--- Nadir Canavarlar (10 tane) ---");
        p.sendMessage("§c16. §7dragon §7- Ejderha (ÇOK NADİR)");
        p.sendMessage("§c17. §7trex §7- T-Rex (NADİR)");
        p.sendMessage("§c18. §7cyclops §7- Tek Gözlü Dev (NADİR)");
        p.sendMessage("§c19. §7griffin §7- Griffin (NADİR)");
        p.sendMessage("§c20. §7wraith §7- Hayalet (NADİR)");
        p.sendMessage("§c21. §7lich §7- Lich (NADİR)");
        p.sendMessage("§c22. §7kraken §7- Kraken (NADİR)");
        p.sendMessage("§c23. §7phoenix §7- Phoenix (NADİR)");
        p.sendMessage("§c24. §7hydra §7- Hydra (ÇOK NADİR)");
        p.sendMessage("§c25. §7behemoth §7- Behemoth (NADİR)");
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
    
    /**
     * Tab Completion - Komut tamamlama desteği
     * /stratocraft [TAB] -> give, spawn, disaster, list, help
     * /stratocraft give [TAB] -> blueprint, lightning_core, ...
     * /stratocraft spawn [TAB] -> hell_dragon, terror_worm, ...
     * /stratocraft disaster [TAB] -> titan_golem, abyssal_worm, ...
     * /stratocraft list [TAB] -> items, mobs, disasters, all
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Permission kontrolü
        if (!sender.hasPermission("stratocraft.admin")) {
            return new ArrayList<>();
        }
        
        // İlk argüman (komut seçimi)
        if (args.length == 1) {
            List<String> commands = Arrays.asList("give", "spawn", "disaster", "list", "help");
            String input = args[0].toLowerCase();
            
            // Eğer boşsa veya başlangıç eşleşiyorsa filtrele
            if (input.isEmpty()) {
                return commands;
            }
            
            return commands.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        
        // İkinci argüman (komut parametreleri)
        if (args.length == 2) {
            String commandName = args[0].toLowerCase();
            String input = args[1].toLowerCase();
            
            switch (commandName) {
                case "give":
                    // Tüm item isimlerini listele
                    List<String> items = Arrays.asList(
                        "blueprint", "lightning_core", "titanium_ore", "titanium_ingot", 
                        "dark_matter", "red_diamond", "ruby", "adamantite", "star_core", 
                        "flame_amplifier", "devil_horn", "devil_snake_eye", "recipe_tectonic", 
                        "war_fan", "tower_shield", "hell_fruit", "clan_crystal", "clan_fence"
                    );
                    if (input.isEmpty()) {
                        return items;
                    }
                    return items.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                    
                case "spawn":
                    // Tüm mob isimlerini listele (25 mob)
                    List<String> mobs = Arrays.asList(
                        // Eski moblar
                        "hell_dragon", "terror_worm", "war_bear", "shadow_panther", "wyvern",
                        // Sık gelen canavarlar
                        "goblin", "ork", "troll", "skeleton_knight", "dark_mage",
                        "werewolf", "giant_spider", "minotaur", "harpy", "basilisk",
                        // Nadir canavarlar
                        "dragon", "trex", "cyclops", "griffin", "wraith",
                        "lich", "kraken", "phoenix", "hydra", "behemoth"
                    );
                    if (input.isEmpty()) {
                        return mobs;
                    }
                    return mobs.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                    
                case "disaster":
                    // Tüm felaket tiplerini listele
                    List<String> disasters = Arrays.asList(
                        "titan_golem", "abyssal_worm", "solar_flare"
                    );
                    if (input.isEmpty()) {
                        return disasters;
                    }
                    return disasters.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                    
                case "list":
                    // Liste tiplerini listele
                    List<String> listTypes = Arrays.asList("items", "mobs", "disasters", "all");
                    if (input.isEmpty()) {
                        return listTypes;
                    }
                    return listTypes.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
            }
        }
        
        // Üçüncü argüman (sadece give komutu için miktar)
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Miktar için tab completion gerekmez, boş liste döndür
            return new ArrayList<>();
        }
        
        // Diğer durumlar için boş liste
        return new ArrayList<>();
    }
}

