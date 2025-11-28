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
            case "siege":
                return handleSiege(p, args);
            case "caravan":
                return handleCaravan(p, args);
            case "contract":
                return handleContract(p, args);
            case "build":
                return handleBuild(p, args);
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
        sender.sendMessage("§eYeni Komutlar:");
        sender.sendMessage("§7  /stratocraft siege <clear|list> §7- Savaş yapılarını yönet");
        sender.sendMessage("§7  /stratocraft caravan <list|clear> §7- Kervanları yönet");
        sender.sendMessage("§7  /stratocraft contract <list|clear> §7- Kontratları yönet");
        sender.sendMessage("§7  /stratocraft build <type> [level] §7- Yapı oluştur");
        sender.sendMessage("");
        sender.sendMessage(langManager.getMessage("admin.help-examples"));
        sender.sendMessage("§7  /stratocraft give blueprint 64");
        sender.sendMessage("§7  /stratocraft spawn hell_dragon");
        sender.sendMessage("§7  /stratocraft disaster titan_golem");
        sender.sendMessage("§7  /stratocraft list all");
        sender.sendMessage("§7  /stratocraft siege list");
        sender.sendMessage("§7  /stratocraft caravan clear");
        sender.sendMessage("§7  /stratocraft build catapult");
        sender.sendMessage("§7  /stratocraft build alchemy_tower 3");
        sender.sendMessage("§7  /stratocraft build magma_battery");
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
            List<String> commands = Arrays.asList("give", "spawn", "disaster", "list", "help", "siege", "caravan", "contract", "build");
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
                            
                case "siege":
                    // Siege komutları
                    List<String> siegeCommands = Arrays.asList("clear", "list");
                    if (input.isEmpty()) {
                        return siegeCommands;
                    }
                    return siegeCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                            
                case "caravan":
                    // Caravan komutları
                    List<String> caravanCommands = Arrays.asList("clear", "list");
                    if (input.isEmpty()) {
                        return caravanCommands;
                    }
                    return caravanCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                            
                case "contract":
                    // Contract komutları
                    List<String> contractCommands = Arrays.asList("clear", "list");
                    if (input.isEmpty()) {
                        return contractCommands;
                    }
                    return contractCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
                            
                case "build":
                    // Build komutları - Tüm yapı tipleri
                    List<String> buildTypes = Arrays.asList(
                        // Savaş yapıları
                        "catapult", "ballista", "lava_fountain", "poison_dispenser",
                        "force_field", "healing_shrine", "power_totem", "speed_circle", "defense_wall",
                        // Bataryalar
                        "magma_battery", "lightning_battery", "black_hole", "bridge",
                        "shelter", "gravity_anchor", "seismic_hammer", "magnetic_disruptor", "ozone_shield",
                        "earth_wall", "energy_wall", "lava_trencher_battery",
                        // Klan yapıları
                        "alchemy_tower", "tectonic_stabilizer", "healing_beacon", "global_market_gate",
                        "auto_turret", "poison_reactor", "siege_factory", "wall_generator",
                        "gravity_well", "lava_trencher", "watchtower", "drone_station",
                        "xp_bank", "mag_rail", "teleporter", "food_silo", "oil_refinery",
                        "weather_machine", "crop_accelerator", "mob_grinder", "invisibility_cloak",
                        "armory", "library", "warning_sign", "auto_drill", "core"
                    );
                    if (input.isEmpty()) {
                        return buildTypes;
                    }
                    return buildTypes.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
            }
        }
        
        // Build komutu için seviye argümanı
        if (args.length == 3 && args[0].equalsIgnoreCase("build")) {
            // Seviye için 1-5 arası öner
            List<String> levels = Arrays.asList("1", "2", "3", "4", "5");
            String input = args[2].toLowerCase();
            if (input.isEmpty()) {
                return levels;
            }
            return levels.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }
        
        // Üçüncü argüman (sadece give komutu için miktar)
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Miktar için tab completion gerekmez, boş liste döndür
            return new ArrayList<>();
        }
        
        // Diğer durumlar için boş liste
        return new ArrayList<>();
    }
    
    // ========== YENİ ADMIN KOMUTLARI ==========
    
    /**
     * Savaş yapıları yönetimi
     * /stratocraft siege clear - Tüm savaş yapılarını temizle
     * /stratocraft siege list - Aktif yapıları listele
     */
    private boolean handleSiege(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft siege <clear|list>");
            return true;
        }
        
        me.mami.stratocraft.manager.SiegeWeaponManager siegeManager = plugin.getSiegeWeaponManager();
        if (siegeManager == null) {
            p.sendMessage("§cSiegeWeaponManager bulunamadı!");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "clear":
                // Tüm aktif yapıları temizle
                int cleared = clearAllSiegeStructures(siegeManager);
                p.sendMessage("§a" + cleared + " savaş yapısı temizlendi.");
                return true;
            case "list":
                showSiegeStructuresList(p, siegeManager);
                return true;
            default:
                p.sendMessage("§cKullanım: /stratocraft siege <clear|list>");
                return true;
        }
    }
    
    private int clearAllSiegeStructures(me.mami.stratocraft.manager.SiegeWeaponManager manager) {
        int count = 0;
        
        // Can Tapınaklarını temizle
        for (org.bukkit.Location loc : new java.util.ArrayList<>(manager.getAllHealingShrines().keySet())) {
            manager.removeHealingShrine(loc);
            count++;
        }
        
        // Güç Totemlerini temizle
        for (org.bukkit.Location loc : new java.util.ArrayList<>(manager.getAllPowerTotems().keySet())) {
            manager.removePowerTotem(loc);
            count++;
        }
        
        // Hız Çemberlerini temizle
        for (org.bukkit.Location loc : new java.util.ArrayList<>(manager.getAllSpeedCircles().keySet())) {
            manager.removeSpeedCircle(loc);
            count++;
        }
        
        // Savunma Duvarlarını temizle
        for (org.bukkit.Location loc : new java.util.ArrayList<>(manager.getAllDefenseWalls().keySet())) {
            manager.removeDefenseWall(loc);
            count++;
        }
        
        return count;
    }
    
    private void showSiegeStructuresList(Player p, me.mami.stratocraft.manager.SiegeWeaponManager manager) {
        p.sendMessage("§6§l=== AKTİF SAVAŞ YAPILARI ===");
        
        int shrineCount = manager.getAllHealingShrines().size();
        int totemCount = manager.getAllPowerTotems().size();
        int circleCount = manager.getAllSpeedCircles().size();
        int wallCount = manager.getAllDefenseWalls().size();
        
        p.sendMessage("§eCan Tapınağı: §7" + shrineCount);
        p.sendMessage("§eGüç Totemi: §7" + totemCount);
        p.sendMessage("§eHız Çemberi: §7" + circleCount);
        p.sendMessage("§eSavunma Duvarı: §7" + wallCount);
        p.sendMessage("§7Toplam: §a" + (shrineCount + totemCount + circleCount + wallCount));
    }
    
    /**
     * Kervan yönetimi
     * /stratocraft caravan list - Aktif kervanları listele
     * /stratocraft caravan clear - Tüm kervanları temizle
     */
    private boolean handleCaravan(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft caravan <list|clear>");
            return true;
        }
        
        me.mami.stratocraft.manager.CaravanManager caravanManager = plugin.getCaravanManager();
        if (caravanManager == null) {
            p.sendMessage("§cCaravanManager bulunamadı!");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "list":
                showCaravansList(p, caravanManager);
                return true;
            case "clear":
                int cleared = caravanManager.clearAllCaravans();
                p.sendMessage("§a" + cleared + " kervan temizlendi.");
                return true;
            default:
                p.sendMessage("§cKullanım: /stratocraft caravan <list|clear>");
                return true;
        }
    }
    
    private void showCaravansList(Player p, me.mami.stratocraft.manager.CaravanManager manager) {
        int count = manager.getActiveCaravanCount();
        p.sendMessage("§6§l=== AKTİF KERVANLAR ===");
        p.sendMessage("§7Toplam: §a" + count);
        
        if (count == 0) {
            p.sendMessage("§7Aktif kervan yok.");
            return;
        }
        
        int index = 1;
        for (java.util.Map.Entry<java.util.UUID, org.bukkit.entity.Entity> entry : manager.getActiveCaravans().entrySet()) {
            org.bukkit.entity.Player owner = org.bukkit.Bukkit.getPlayer(entry.getKey());
            String ownerName = owner != null ? owner.getName() : "Offline";
            org.bukkit.entity.Entity caravan = entry.getValue();
            
            if (caravan != null && caravan.isValid()) {
                org.bukkit.Location loc = caravan.getLocation();
                p.sendMessage("§e" + index + ". §7Sahip: §a" + ownerName + 
                    " §7- Konum: §7X:" + (int)loc.getX() + " Y:" + (int)loc.getY() + " Z:" + (int)loc.getZ());
            } else {
                p.sendMessage("§e" + index + ". §7Sahip: §a" + ownerName + " §c(Geçersiz)");
            }
            index++;
        }
    }
    
    /**
     * Kontrat yönetimi
     * /stratocraft contract list - Aktif kontratları listele
     * /stratocraft contract clear - Tüm kontratları temizle
     */
    private boolean handleContract(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft contract <list|clear>");
            return true;
        }
        
        me.mami.stratocraft.manager.ContractManager contractManager = plugin.getContractManager();
        if (contractManager == null) {
            p.sendMessage("§cContractManager bulunamadı!");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "list":
                showContractsList(p, contractManager);
                return true;
            case "clear":
                int cleared = clearAllContracts(contractManager);
                p.sendMessage("§a" + cleared + " kontrat temizlendi.");
                return true;
            default:
                p.sendMessage("§cKullanım: /stratocraft contract <list|clear>");
                return true;
        }
    }
    
    private void showContractsList(Player p, me.mami.stratocraft.manager.ContractManager manager) {
        java.util.List<me.mami.stratocraft.model.Contract> contracts = manager.getContracts();
        p.sendMessage("§6§l=== AKTİF KONTRATLAR ===");
        p.sendMessage("§7Toplam: §a" + contracts.size());
        
        if (contracts.isEmpty()) {
            p.sendMessage("§7Aktif kontrat yok.");
            return;
        }
        
        int index = 1;
        for (me.mami.stratocraft.model.Contract contract : contracts) {
            org.bukkit.entity.Player issuer = org.bukkit.Bukkit.getPlayer(contract.getIssuer());
            String issuerName = issuer != null ? issuer.getName() : "Offline";
            
            if (contract.getTargetPlayer() != null) {
                // Bounty kontratı
                org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(contract.getTargetPlayer());
                String targetName = target != null ? target.getName() : "Offline";
                p.sendMessage("§e" + index + ". §7Bounty: §c" + targetName + 
                    " §7- Ödül: §6" + String.format("%.2f", contract.getRewardAmount()) + " Altın" +
                    " §7- Veren: §a" + issuerName);
            } else {
                // Normal kontrat
                p.sendMessage("§e" + index + ". §7" + contract.getMaterial() + " x" + contract.getAmount() + 
                    " §7- Ödül: §6" + String.format("%.2f", contract.getRewardAmount()) + " Altın" +
                    " §7- Veren: §a" + issuerName);
            }
            index++;
        }
    }
    
    private int clearAllContracts(me.mami.stratocraft.manager.ContractManager manager) {
        int count = manager.getContracts().size();
        manager.getContracts().clear();
        return count;
    }
    
    // ========== YAPI OLUŞTURMA KOMUTLARI ==========
    
    /**
     * Yapı oluşturma komutu
     * /stratocraft build <type> [level] - Yapı oluşturur ve aktifleştirme malzemelerini verir
     */
    private boolean handleBuild(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft build <type> [level]");
            p.sendMessage("§7Örnek: /stratocraft build catapult");
            p.sendMessage("§7Örnek: /stratocraft build alchemy_tower 3");
            p.sendMessage("§7Yapı tipleri: siege, structure, battery");
            return true;
        }
        
        String buildType = args[1].toLowerCase();
        int level = args.length > 2 ? parseInt(args[2], 1) : 1;
        
        // Yapı kategorisine göre yönlendir
        if (buildType.startsWith("catapult") || buildType.startsWith("ballista") || 
            buildType.startsWith("lava_fountain") || buildType.startsWith("poison_dispenser") ||
            buildType.startsWith("force_field") || buildType.startsWith("healing_shrine") ||
            buildType.startsWith("power_totem") || buildType.startsWith("speed_circle") ||
            buildType.startsWith("defense_wall")) {
            return buildSiegeWeapon(p, buildType);
        } else if (buildType.startsWith("magma") || buildType.startsWith("lightning") ||
                   buildType.startsWith("black_hole") || buildType.startsWith("bridge") ||
                   buildType.startsWith("shelter") || buildType.startsWith("gravity") ||
                   buildType.startsWith("seismic") || buildType.startsWith("ozone") ||
                   buildType.startsWith("magnetic") || buildType.startsWith("earth_wall") ||
                   buildType.startsWith("energy_wall") || buildType.startsWith("lava_trencher_battery")) {
            return buildBattery(p, buildType);
        } else {
            // Klan yapıları (Structure.Type)
            return buildClanStructure(p, buildType, level);
        }
    }
    
    /**
     * Savaş yapıları oluşturma
     */
    private boolean buildSiegeWeapon(Player p, String type) {
        Location loc = p.getLocation();
        
        // Yerinde blok varsa temizle
        me.mami.stratocraft.manager.StructureBuilder.clearArea(loc, 5, 5, 5);
        
        switch (type.toLowerCase()) {
            case "catapult":
            case "mancinik":
                // Mancınık: Basamak bloğu oluştur
                loc.getBlock().setType(Material.STONE_BRICK_STAIRS);
                org.bukkit.block.data.type.Stairs stairs = (org.bukkit.block.data.type.Stairs) 
                    loc.getBlock().getBlockData();
                stairs.setFacing(p.getFacing());
                loc.getBlock().setBlockData(stairs);
                
                // Aktifleştirme malzemesi yok (sadece sağ tık)
                p.sendMessage("§a§lMANCINIK OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla ateş edebilirsin.");
                return true;
                
            case "ballista":
            case "balista":
                // Balista: Dispenser oluştur
                loc.getBlock().setType(Material.DISPENSER);
                org.bukkit.block.data.Directional dispenser = (org.bukkit.block.data.Directional)
                    loc.getBlock().getBlockData();
                dispenser.setFacing(p.getFacing());
                loc.getBlock().setBlockData(dispenser);
                
                p.sendMessage("§a§lBALİSTA OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla ateş edebilirsin.");
                return true;
                
            case "lava_fountain":
            case "lav_fiskiyesi":
                // Lav Fıskiyesi: Cauldron oluştur ve doldur
                loc.getBlock().setType(Material.CAULDRON);
                org.bukkit.block.data.Levelled cauldron = (org.bukkit.block.data.Levelled)
                    loc.getBlock().getBlockData();
                cauldron.setLevel(3); // Tam dolu
                loc.getBlock().setBlockData(cauldron);
                
                // Lava bucket ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.LAVA_BUCKET, 1));
                
                p.sendMessage("§a§lLAV FISKIYESI OLUŞTURULDU!");
                p.sendMessage("§7Lava ile doldur ve sağ tıkla.");
                return true;
                
            case "poison_dispenser":
            case "zehir_yayici":
                // Zehir Yayıcı: Dropper oluştur
                loc.getBlock().setType(Material.DROPPER);
                org.bukkit.block.data.Directional dropper = (org.bukkit.block.data.Directional)
                    loc.getBlock().getBlockData();
                dropper.setFacing(p.getFacing());
                loc.getBlock().setBlockData(dropper);
                
                // Spider Eye ver (aktifleştirme için)
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.SPIDER_EYE, 1));
                
                p.sendMessage("§a§lZEHİR GAZI YAYICI OLUŞTURULDU!");
                p.sendMessage("§7Spider Eye ile sağ tıkla.");
                return true;
                
            case "force_field":
            case "enerji_kalkani":
                // Enerji Kalkanı: Beacon oluştur
                loc.getBlock().setType(Material.BEACON);
                
                p.sendMessage("§a§lENERJİ KALKANI OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla aktifleştir.");
                return true;
                
            case "healing_shrine":
            case "can_tapinagi":
                // Can Tapınağı: 3x3 Altın Bloğu + Ortada Beacon
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.BEACON);
                        } else {
                            loc.clone().add(x, -1, z).getBlock().setType(Material.GOLD_BLOCK);
                        }
                    }
                }
                
                p.sendMessage("§a§lCAN TAPINAĞI OLUŞTURULDU!");
                p.sendMessage("§7Klan üyeleriniz buradan faydalanabilir.");
                return true;
                
            case "power_totem":
            case "guc_totemi":
                // Güç Totemi: 2x2 Obsidyen + Ortada Enchanting Table
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.ENCHANTING_TABLE);
                        } else {
                            loc.clone().add(x, -1, z).getBlock().setType(Material.OBSIDIAN);
                        }
                    }
                }
                
                p.sendMessage("§a§lGÜÇ TOTEMİ OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla aktifleştir.");
                return true;
                
            case "speed_circle":
            case "hiz_cemberi":
                // Hız Çemberi: 2x2 Lapis Bloğu + Ortada Ender Chest
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.ENDER_CHEST);
                        } else {
                            loc.clone().add(x, -1, z).getBlock().setType(Material.LAPIS_BLOCK);
                        }
                    }
                }
                
                p.sendMessage("§a§lHIZ ÇEMBERİ OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla aktifleştir.");
                return true;
                
            case "defense_wall":
            case "savunma_duvari":
                // Savunma Duvarı: 2x2 Demir Bloğu + Ortada Anvil
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.ANVIL);
                        } else {
                            loc.clone().add(x, -1, z).getBlock().setType(Material.IRON_BLOCK);
                        }
                    }
                }
                
                p.sendMessage("§a§lSAVUNMA DUVARI OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla aktifleştir.");
                return true;
                
            default:
                p.sendMessage("§cBilinmeyen savaş yapısı: " + type);
                return true;
        }
    }
    
    /**
     * Batarya oluşturma
     */
    private boolean buildBattery(Player p, String type) {
        Location loc = p.getLocation();
        
        // Yerinde blok varsa temizle
        me.mami.stratocraft.manager.StructureBuilder.clearArea(loc, 3, 3, 3);
        
        switch (type.toLowerCase()) {
            case "magma":
            case "magma_battery":
            case "ates_topu":
                // Magma Bataryası: 3 Magma Bloğu üst üste
                loc.getBlock().setType(Material.MAGMA_BLOCK);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.MAGMA_BLOCK);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.MAGMA_BLOCK);
                
                // Aktifleştirme malzemeleri ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.DIAMOND, 1));
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));
                if (ItemManager.RED_DIAMOND != null) {
                    giveItemSafely(p, ItemManager.RED_DIAMOND.clone());
                }
                if (ItemManager.DARK_MATTER != null) {
                    giveItemSafely(p, ItemManager.DARK_MATTER.clone());
                }
                
                p.sendMessage("§a§lMAGMA BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "lightning":
            case "lightning_battery":
            case "yildirim":
                // Yıldırım Bataryası: 3 Demir Bloğu üst üste
                loc.getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.IRON_BLOCK);
                
                // Lightning Core ver
                if (ItemManager.LIGHTNING_CORE != null) {
                    giveItemSafely(p, ItemManager.LIGHTNING_CORE.clone());
                }
                
                p.sendMessage("§a§lYILDIRIM BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "black_hole":
            case "kara_delik":
                // Kara Delik: 3 Obsidyen üst üste
                loc.getBlock().setType(Material.OBSIDIAN);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.OBSIDIAN);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.OBSIDIAN);
                
                // Dark Matter ver
                if (ItemManager.DARK_MATTER != null) {
                    giveItemSafely(p, ItemManager.DARK_MATTER.clone());
                }
                
                p.sendMessage("§a§lKARA DELİK BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "bridge":
            case "anlik_kopru":
                // Anlık Köprü: 3 Buz üst üste
                loc.getBlock().setType(Material.PACKED_ICE);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.PACKED_ICE);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.PACKED_ICE);
                
                // Feather ver (BatteryListener'da FEATHER kullanılıyor)
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.FEATHER, 1));
                
                p.sendMessage("§a§lANLIK KÖPRÜ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "shelter":
            case "siginak":
                // Sığınak Küpü: 3 Cobblestone üst üste
                loc.getBlock().setType(Material.COBBLESTONE);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.COBBLESTONE);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.COBBLESTONE);
                
                // Iron Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));
                
                p.sendMessage("§a§lSIĞINAK KÜPÜ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "gravity":
            case "gravity_anchor":
            case "yercekim_capasi":
                // Yerçekimi Çapası: Anvil + Slime Block altında
                loc.getBlock().setType(Material.ANVIL);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.SLIME_BLOCK);
                
                // Iron Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));
                
                p.sendMessage("§a§lYERÇEKİMİ ÇAPASI BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "seismic":
            case "seismic_hammer":
            case "sismik_cekich":
                // Sismik Çekiç: Anvil + 2 Demir Bloğu altında
                loc.getBlock().setType(Material.ANVIL);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, -2, 0).getBlock().setType(Material.IRON_BLOCK);
                
                // Star Core ver (BatteryListener'da STAR_CORE kullanılıyor)
                if (ItemManager.STAR_CORE != null) {
                    giveItemSafely(p, ItemManager.STAR_CORE.clone());
                }
                
                p.sendMessage("§a§lSİSMİK ÇEKİÇ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "magnetic":
            case "magnetic_disruptor":
            case "manyetik_bozucu":
                // Manyetik Bozucu: Lapis Bloğu + 2 Demir Bloğu altında
                loc.getBlock().setType(Material.LAPIS_BLOCK);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, -2, 0).getBlock().setType(Material.IRON_BLOCK);
                
                // Iron Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));
                
                p.sendMessage("§a§lMANYETİK BOZUCU BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "ozone":
            case "ozone_shield":
            case "ozon_kalkani":
                // Ozon Kalkanı: Beacon + Cam altında
                loc.getBlock().setType(Material.BEACON);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.GLASS);
                
                // Ruby ver (BatteryListener'da RUBY kullanılıyor)
                if (ItemManager.RUBY != null) {
                    giveItemSafely(p, ItemManager.RUBY.clone());
                }
                
                p.sendMessage("§a§lOZON KALKANI BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "earth_wall":
            case "toprak_suru":
                // Toprak Suru: 3 Toprak Bloğu yanyana
                loc.getBlock().setType(Material.DIRT);
                loc.clone().add(1, 0, 0).getBlock().setType(Material.DIRT);
                loc.clone().add(-1, 0, 0).getBlock().setType(Material.DIRT);
                
                // Cobblestone veya Titanium Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.COBBLESTONE, 1));
                if (ItemManager.TITANIUM_INGOT != null) {
                    giveItemSafely(p, ItemManager.TITANIUM_INGOT.clone());
                }
                
                p.sendMessage("§a§lTOPRAK SURU BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "energy_wall":
            case "enerji_duvari":
                // Enerji Duvarı: 3 Demir Bloğu üst üste
                loc.getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, -2, 0).getBlock().setType(Material.IRON_BLOCK);
                
                // Adamantite ver (BatteryListener'da ADAMANTITE kullanılıyor)
                if (ItemManager.ADAMANTITE != null) {
                    giveItemSafely(p, ItemManager.ADAMANTITE.clone());
                }
                
                p.sendMessage("§a§lENERJİ DUVARI BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            case "lava_trencher_battery":
            case "lav_hendekcisi_battery":
                // Lav Hendekçisi: 2 Lav üst üste
                loc.getBlock().setType(Material.LAVA);
                loc.clone().add(0, -1, 0).getBlock().setType(Material.LAVA);
                
                // Lava Bucket ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.LAVA_BUCKET, 1));
                
                p.sendMessage("§a§lLAV HENDEKÇİSİ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;
                
            default:
                p.sendMessage("§cBilinmeyen batarya tipi: " + type);
                return true;
        }
    }
    
    /**
     * Klan yapıları oluşturma (şema ile)
     */
    private boolean buildClanStructure(Player p, String type, int level) {
        Location loc = p.getLocation();
        
        // Yerinde blok varsa temizle
        me.mami.stratocraft.manager.StructureBuilder.clearArea(loc, 10, 10, 10);
        
        // Şema dosya adını belirle
        String schematicName = getSchematicName(type, level);
        boolean schematicExists = me.mami.stratocraft.manager.StructureBuilder.schematicExists(schematicName);
        
        if (schematicExists) {
            // Şema varsa yükle
            boolean success = me.mami.stratocraft.manager.StructureBuilder.pasteSchematic(loc, schematicName);
            if (success) {
                p.sendMessage("§a§l" + getStructureDisplayName(type) + " OLUŞTURULDU!");
                p.sendMessage("§7Seviye: §e" + level);
            } else {
                p.sendMessage("§cŞema yüklenirken hata oluştu!");
                // Yer tutucu oluştur
                me.mami.stratocraft.manager.StructureBuilder.createPlaceholderStructure(loc);
                p.sendMessage("§eYer tutucu yapı oluşturuldu (3 kırmızı yün).");
            }
        } else {
            // Şema yoksa yer tutucu oluştur
            p.sendMessage("§c§lUYARI: Şema dosyası bulunamadı!");
            p.sendMessage("§7Şema: §e" + schematicName + ".schem");
            p.sendMessage("§7Yer tutucu yapı oluşturuldu (3 kırmızı yün).");
            me.mami.stratocraft.manager.StructureBuilder.createPlaceholderStructure(loc);
        }
        
        // Aktifleştirme malzemelerini ver
        giveStructureActivationMaterials(p, type, level);
        
        return true;
    }
    
    /**
     * Şema dosya adını belirle
     */
    private String getSchematicName(String type, int level) {
        String baseName = type.toLowerCase();
        
        // Özel durumlar: StructureListener'da kullanılan şema adları
        switch (baseName) {
            case "healing_beacon":
                baseName = "healing_tower"; // StructureListener'da "healing_tower" kullanılıyor
                break;
            case "global_market_gate":
                baseName = "market_gate"; // StructureListener'da "market_gate" kullanılıyor
                break;
            // Diğerleri aynı kalıyor: alchemy_tower, tectonic_stabilizer, vb.
        }
        
        // Seviyeli yapılar için seviye ekle
        if (level > 1) {
            return baseName + "_level" + level;
        }
        
        return baseName;
    }
    
    /**
     * Yapı görünen adını al
     */
    private String getStructureDisplayName(String type) {
        switch (type.toLowerCase()) {
            case "alchemy_tower": return "Simya Kulesi";
            case "tectonic_stabilizer": return "Tektonik Sabitleyici";
            case "healing_beacon": return "Şifa Kulesi";
            case "global_market_gate": return "Global Pazar Kapısı";
            case "auto_turret": return "Otomatik Taret";
            case "poison_reactor": return "Zehir Reaktörü";
            case "siege_factory": return "Kuşatma Fabrikası";
            case "wall_generator": return "Sur Jeneratörü";
            case "gravity_well": return "Yerçekimi Kuyusu";
            case "lava_trencher": return "Lav Hendekçisi";
            case "watchtower": return "Gözetleme Kulesi";
            case "drone_station": return "Drone İstasyonu";
            case "xp_bank": return "Tecrübe Bankası";
            case "mag_rail": return "Manyetik Ray";
            case "teleporter": return "Işınlanma Platformu";
            case "food_silo": return "Buzdolabı";
            case "oil_refinery": return "Petrol Rafinerisi";
            case "weather_machine": return "Hava Kontrolcüsü";
            case "crop_accelerator": return "Tarım Hızlandırıcı";
            case "mob_grinder": return "Mob Öğütücü";
            case "invisibility_cloak": return "Görünmezlik Perdesi";
            case "armory": return "Cephanelik";
            case "library": return "Kütüphane";
            case "warning_sign": return "Yasaklı Bölge Tabelası";
            case "auto_drill": return "Otomatik Madenci";
            case "core": return "Ana Kristal";
            default: return type;
        }
    }
    
    /**
     * Yapı aktifleştirme malzemelerini ver
     */
    private void giveStructureActivationMaterials(Player p, String type, int level) {
        switch (type.toLowerCase()) {
            case "alchemy_tower":
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 32));
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.DIAMOND, 16));
                p.sendMessage("§7Aktifleştirme: §e32 Altın + 16 Elmas");
                break;
                
            case "tectonic_stabilizer":
                if (ItemManager.TITANIUM_INGOT != null) {
                    org.bukkit.inventory.ItemStack titanium = ItemManager.TITANIUM_INGOT.clone();
                    titanium.setAmount(16);
                    giveItemSafely(p, titanium);
                }
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.PISTON, 8));
                p.sendMessage("§7Aktifleştirme: §e16 Titanyum + 8 Piston");
                break;
                
            case "healing_beacon":
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 16));
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.LAPIS_LAZULI, 8));
                p.sendMessage("§7Aktifleştirme: §e16 Demir + 8 Lapis");
                break;
                
            case "global_market_gate":
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 32));
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.ENDER_PEARL, 16));
                p.sendMessage("§7Aktifleştirme: §e32 Altın + 16 Ender Pearl");
                break;
                
            case "auto_turret":
                // Antik Dişli (Iron Nugget) + Piston
                org.bukkit.inventory.ItemStack gear = new org.bukkit.inventory.ItemStack(Material.IRON_NUGGET, 1);
                org.bukkit.inventory.meta.ItemMeta gearMeta = gear.getItemMeta();
                if (gearMeta != null) {
                    gearMeta.setDisplayName("§6Antik Dişli");
                    gear.setItemMeta(gearMeta);
                }
                giveItemSafely(p, gear);
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.PISTON, 1));
                p.sendMessage("§7Aktifleştirme: §eAntik Dişli + Piston");
                break;
                
            default:
                // Diğer yapılar için Blueprint ver
                if (ItemManager.BLUEPRINT_PAPER != null) {
                    giveItemSafely(p, ItemManager.BLUEPRINT_PAPER.clone());
                    p.sendMessage("§7Blueprint verildi. Yapıyı aktifleştirmek için kullan.");
                }
                break;
        }
    }
    
    /**
     * Oyuncuya eşya ver (envanter doluysa yere düşür)
     */
    private void giveItemSafely(Player p, org.bukkit.inventory.ItemStack item) {
        if (item == null) return;
        
        java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> overflow = p.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            for (org.bukkit.inventory.ItemStack drop : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
        }
    }
}

