package me.mami.stratocraft.command;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MobManager;
import me.mami.stratocraft.manager.DisasterManager;
import me.mami.stratocraft.manager.BatteryManager;
import me.mami.stratocraft.model.Disaster;
import me.mami.stratocraft.util.LangManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import me.mami.stratocraft.manager.BossManager;

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
            case "ballista":
                return handleBallista(p, args);
            case "caravan":
                return handleCaravan(p, args);
            case "contract":
                return handleContract(p, args);
            case "alliance":
            case "ittifak":
                return handleAlliance(p, args);
            case "build":
                return handleBuild(p, args);
            case "trap":
                return handleTrap(p, args);
            case "mine":
                return handleMine(p, args);
            case "dungeon":
                return handleDungeon(p, args);
            case "biome":
                return handleBiome(p, args);
            case "boss":
                return handleBoss(p, args);
            case "tame":
                return handleTame(p, args);
            case "recipe":
                return handleRecipe(p, args);
            default:
                showHelp(sender);
                return true;
        }
    }
    
    /**
     * Tarif yönetimi komutları
     */
    private boolean handleRecipe(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /scadmin recipe <komut>");
            p.sendMessage("§7Komutlar:");
            p.sendMessage("§7  remove <oyuncu> - Oyuncunun aktif tarifini kaldır");
            p.sendMessage("§7  removeall - Tüm aktif tarifleri kaldır");
            p.sendMessage("§7  clearall - Tüm aktif ve sabit tarifleri kaldır");
            p.sendMessage("§7  list - Aktif tarifleri listele");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "remove":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /scadmin recipe remove <oyuncu>");
                    return true;
                }
                org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(args[2]);
                if (target == null) {
                    p.sendMessage("§cOyuncu bulunamadı: " + args[2]);
                    return true;
                }
                plugin.getGhostRecipeManager().removeGhostRecipe(target);
                p.sendMessage("§a" + target.getName() + " oyuncusunun aktif tarifi kaldırıldı.");
                return true;
                
            case "removeall":
                // Tüm oyuncuların aktif tariflerini kaldır
                int count = 0;
                for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (plugin.getGhostRecipeManager().hasActiveRecipe(player.getUniqueId())) {
                        plugin.getGhostRecipeManager().removeGhostRecipe(player);
                        count++;
                    }
                }
                p.sendMessage("§a" + count + " aktif tarif kaldırıldı.");
                return true;
                
            case "clearall":
                // Tüm aktif ve sabit tarifleri kaldır
                int activeCount = 0;
                for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (plugin.getGhostRecipeManager().hasActiveRecipe(player.getUniqueId())) {
                        plugin.getGhostRecipeManager().removeGhostRecipe(player);
                        activeCount++;
                    }
                }
                // Sabit tarifleri de kaldır (tüm dünyalarda)
                int fixedCount = plugin.getGhostRecipeManager().clearAllFixedRecipes();
                p.sendMessage("§a" + activeCount + " aktif tarif ve " + fixedCount + " sabit tarif kaldırıldı.");
                return true;
                
            case "list":
                p.sendMessage("§6§l════════════════════════════");
                p.sendMessage("§e§lAKTİF TARİFLER");
                p.sendMessage("§6§l════════════════════════════");
                int listActiveCount = 0;
                for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (plugin.getGhostRecipeManager().hasActiveRecipe(player.getUniqueId())) {
                        p.sendMessage("§7- §e" + player.getName());
                        listActiveCount++;
                    }
                }
                if (listActiveCount == 0) {
                    p.sendMessage("§7Aktif tarif yok.");
                }
                return true;
                
            default:
                p.sendMessage("§cBilinmeyen komut: " + args[1]);
                return true;
        }
    }

    private boolean handleGive(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft give <kategori> [seviye] <item> [miktar]");
            p.sendMessage("§7Kategoriler: weapon, armor, material, mobdrop, special, ore, tool, bossitem, recipebook");
            p.sendMessage("§7Seviyeli itemler (weapon, armor):");
            p.sendMessage("§7  /stratocraft give weapon <seviye> <silah_tipi> [miktar]");
            p.sendMessage("§7  /stratocraft give armor <seviye> <zırh_tipi> [miktar]");
            p.sendMessage("§7  Silah tipleri: sword, axe, spear, bow, hammer");
            p.sendMessage("§7  Zırh tipleri: helmet, chestplate, leggings, boots");
            p.sendMessage("§7Örnek: /stratocraft give weapon 1 sword");
            p.sendMessage("§7Örnek: /stratocraft give armor 3 chestplate");
            p.sendMessage("§7Örnek: /stratocraft give material blueprint 64");
            p.sendMessage("§7Örnek: /stratocraft give bossitem goblin_crown");
            return true;
        }

        // Kategori kontrolü
        String category = args[1].toLowerCase();
        String itemName;
        int amount;
        int level = -1;

        // Seviyeli kategoriler için özel işlem (weapon, armor)
        if (category.equals("weapon") || category.equals("armor")) {
            if (args.length < 3) {
                p.sendMessage("§cKullanım: /stratocraft give " + category + " <seviye> <tip> [miktar]");
                if (category.equals("weapon")) {
                    p.sendMessage("§7Seviye: 1-5");
                    p.sendMessage("§7Tipler: sword, axe, spear, bow, hammer");
                } else {
                    p.sendMessage("§7Seviye: 1-5");
                    p.sendMessage("§7Tipler: helmet, chestplate, leggings, boots");
                }
                return true;
            }
            
            // Seviye kontrolü
            try {
                level = Integer.parseInt(args[2]);
                if (level < 1 || level > 5) {
                    p.sendMessage("§cSeviye 1-5 arası olmalı!");
                    return true;
                }
            } catch (NumberFormatException e) {
                p.sendMessage("§cGeçersiz seviye: §e" + args[2]);
                return true;
            }
            
            // Item tipi
            if (args.length < 4) {
                p.sendMessage("§cKullanım: /stratocraft give " + category + " <seviye> <tip> [miktar]");
                return true;
            }
            itemName = args[3].toLowerCase();
            amount = args.length > 4 ? parseInt(args[4], 1) : 1;
        } else {
            // Diğer kategoriler için eski format
            if (args.length >= 3) {
                itemName = args[2].toLowerCase();
                amount = args.length > 3 ? parseInt(args[3], 1) : 1;
            } else {
                // Eski format desteği (kategori yok, direkt item)
                itemName = args[1].toLowerCase();
                amount = args.length > 2 ? parseInt(args[2], 1) : 1;
                category = "all"; // Tüm kategorilerde ara
            }
        }

        // Miktar kontrolü
        if (amount < 1) {
            amount = 1;
        }
        if (amount > 2304) {
            p.sendMessage("§cMiktar çok yüksek! Maksimum 2304.");
            return true;
        }

        ItemStack item;
        if (level > 0) {
            // Seviyeli item için özel metod
            item = getLeveledItemByName(category, level, itemName);
        } else {
            item = getItemByName(itemName, category);
        }
        
        if (item == null) {
            p.sendMessage("§cGeçersiz item: §e" + itemName);
            if (!category.equals("all")) {
                p.sendMessage("§7Kategori: §e" + category);
                if (level > 0) {
                    p.sendMessage("§7Seviye: §e" + level);
                }
            }
            p.sendMessage("§7Kullanım: /stratocraft give <kategori> [seviye] <item> [miktar]");
            return true;
        }

        item.setAmount(amount);
        
        // Item ismini al
        String displayName = getItemDisplayNameFromStack(item);
        if (displayName == null || displayName.isEmpty()) {
            displayName = itemName;
        }

        // Envanter doluysa yere düşür
        java.util.HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
            p.sendMessage("§a" + amount + "x " + displayName + " verildi (yere düştü)");
        } else {
            p.sendMessage("§a" + amount + "x " + displayName + " verildi");
        }
        return true;
    }

    private boolean handleSpawn(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft spawn <kategori> <mob>");
            p.sendMessage("§7Kategoriler: level1, level2, level3, level4, level5, boss, special");
            p.sendMessage("§7Örnek: /stratocraft spawn level1 wild_boar");
            p.sendMessage("§7Örnek: /stratocraft spawn boss titan_golem");
            p.sendMessage("§7Eski format: /stratocraft spawn <mob> (hala çalışıyor)");
            return true;
        }

        String mobName;

        // Kategori kontrolü
        if (args.length >= 3) {
            mobName = args[2].toLowerCase();
        } else {
            // Eski format desteği (kategori yok, direkt mob)
            mobName = args[1].toLowerCase();
        }

        MobManager mobManager = plugin.getMobManager();
        if (mobManager == null) {
            p.sendMessage("§cMobManager bulunamadı!");
            return true;
        }
        String mobDisplayName = "";

        switch (mobName) {
            // Eski moblar
            case "hell_dragon":
            case "ejder":
            case "cehennem_ejderi":
                mobManager.spawnHellDragon(p.getLocation(), p.getUniqueId());
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
            // Yeni Moblar
            case "titan_golem":
                mobManager.spawnTitanGolem(p.getLocation(), p.getUniqueId());
                mobDisplayName = "Titan Golem";
                break;
            case "void_worm":
            case "hiclik_solucani":
            case "hiçlik_solucanı":
                mobManager.spawnVoidCreature(p.getLocation());
                mobDisplayName = "Hiçlik Yaratığı";
                break;
            // ========== YENİ SEVİYE 1 MOBLAR ==========
            case "wild_boar":
            case "yaban_domuzu":
                mobManager.spawnWildBoar(p.getLocation());
                mobDisplayName = "Yaban Domuzu";
                break;
            case "wolf_pack":
            case "kurt_surusu":
            case "forest_wolf":
                mobManager.spawnWolfPack(p.getLocation());
                mobDisplayName = "Orman Kurdu";
                break;
            case "snake":
            case "yilan":
            case "swamp_serpent":
                mobManager.spawnSnake(p.getLocation());
                mobDisplayName = "Bataklık Yılanı";
                break;
            case "eagle":
            case "kartal":
            case "mountain_eagle":
                mobManager.spawnEagle(p.getLocation());
                mobDisplayName = "Dağ Kartalı";
                break;
            case "bear":
            case "cave_bear":
                mobManager.spawnBear(p.getLocation());
                mobDisplayName = "Mağara Ayısı";
                break;
            // ========== YENİ SEVİYE 2 MOBLAR ==========
            case "iron_golem":
            case "iron_golem_variant":
                mobManager.spawnIronGolem(p.getLocation());
                mobDisplayName = "Demir Golem Varyantı";
                break;
            case "ice_elemental":
            case "ice_dragon":
                mobManager.spawnIceDragon(p.getLocation());
                mobDisplayName = "Buz Ejderi";
                break;
            case "fire_elemental":
            case "fire_serpent":
                mobManager.spawnFireSerpent(p.getLocation());
                mobDisplayName = "Ateş Yılanı";
                break;
            case "earth_elemental":
            case "earth_giant":
                mobManager.spawnEarthGiant(p.getLocation());
                mobDisplayName = "Toprak Dev";
                break;
            case "spirit_guardian":
            case "soul_hunter":
                mobManager.spawnSoulHunter(p.getLocation());
                mobDisplayName = "Ruh Avcısı";
                break;
            // ========== YENİ SEVİYE 3 MOBLAR ==========
            case "shadow_beast":
            case "shadow_dragon":
                mobManager.spawnShadowDragon(p.getLocation());
                mobDisplayName = "Gölge Ejderi";
                break;
            case "light_spirit":
            case "light_dragon":
                mobManager.spawnLightDragon(p.getLocation());
                mobDisplayName = "Işık Ejderi";
                break;
            case "storm_giant":
                mobManager.spawnStormGiant(p.getLocation());
                mobDisplayName = "Fırtına Devi";
                break;
            case "lava_golem":
            case "lava_dragon":
                mobManager.spawnLavaDragon(p.getLocation());
                mobDisplayName = "Lav Ejderi";
                break;
            case "frost_giant":
            case "ice_giant":
                mobManager.spawnIceGiant(p.getLocation());
                mobDisplayName = "Buz Devi";
                break;
            // ========== YENİ SEVİYE 4 MOBLAR ==========
            case "red_devil":
            case "kizil_seytan":
                mobManager.spawnRedDevil(p.getLocation());
                mobDisplayName = "Kızıl Şeytan";
                break;
            case "celestial_guardian":
            case "black_dragon":
                mobManager.spawnBlackDragon(p.getLocation());
                mobDisplayName = "Kara Ejder";
                break;
            case "thunder_wyvern":
            case "death_knight":
                mobManager.spawnDeathKnight(p.getLocation());
                mobDisplayName = "Ölüm Şövalyesi";
                break;
            case "magma_beast":
            case "chaos_dragon":
                mobManager.spawnChaosDragon(p.getLocation());
                mobDisplayName = "Kaos Ejderi";
                break;
            case "abyssal_horror":
            case "hell_devil":
                mobManager.spawnHellDevil(p.getLocation());
                mobDisplayName = "Cehennem Şeytanı";
                break;
            // ========== YENİ SEVİYE 5 MOBLAR ==========
            case "ancient_dragon":
            case "legendary_dragon":
                mobManager.spawnLegendaryDragon(p.getLocation());
                mobDisplayName = "Efsanevi Ejder";
                break;
            case "elder_kraken":
            case "god_slayer":
                mobManager.spawnGodSlayer(p.getLocation());
                mobDisplayName = "Tanrı Katili";
                break;
            case "void_lord":
            case "void_creature":
                mobManager.spawnVoidCreature(p.getLocation());
                mobDisplayName = "Hiçlik Lordu";
                break;
            case "cosmic_horror":
            case "time_dragon":
                mobManager.spawnTimeDragon(p.getLocation());
                mobDisplayName = "Zaman Ejderi";
                break;
            case "elemental_titan":
            case "fate_creature":
                mobManager.spawnFateCreature(p.getLocation());
                mobDisplayName = "Kader Yaratığı";
                break;
            // Hava Drop
            case "supply_drop":
            case "supplydrop":
            case "hava_drop":
            case "havadrop":
                // SupplyDropManager'ı çağır - Oyuncunun konumuna düşür
                // Main.java'da zaten oluşturulmuş supplyDropManager'ı kullan
                // Ama eğer yoksa yeni oluştur
                me.mami.stratocraft.manager.SupplyDropManager supplyDropManager = plugin.getSupplyDropManager();
                if (supplyDropManager == null) {
                    supplyDropManager = new me.mami.stratocraft.manager.SupplyDropManager(plugin);
                }
                // Oyuncunun baktığı yöne veya konumuna düşür
                org.bukkit.Location targetLoc = p.getLocation();
                supplyDropManager.spawnSupplyDropAtLocation(targetLoc, null);
                mobDisplayName = "Hava Drop";
                p.sendMessage("§a§lHava Drop oyuncunun konumuna düşürülüyor!");
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
            p.sendMessage("§cKullanım: /stratocraft disaster <komut>");
            p.sendMessage("§7Komutlar:");
            p.sendMessage("§7  start <type> [level] [konum] - Felaket başlat");
            p.sendMessage("§7  stop - Felaketi durdur");
            p.sendMessage("§7  info - Aktif felaket bilgisi");
            p.sendMessage("§7  list - Tüm felaket tiplerini listele");
            p.sendMessage("§7  clear - Felaketi yok et (eski komut)");
            p.sendMessage("§eÖrnek:");
            p.sendMessage("§7  /stratocraft disaster start titan_golem 3");
            p.sendMessage("§7  /stratocraft disaster start solar_flare 1 ben");
            p.sendMessage("§7  /stratocraft disaster stop");
            p.sendMessage("§7  /stratocraft disaster info");
            return true;
        }

        DisasterManager disasterManager = plugin.getDisasterManager();
        if (disasterManager == null) {
            p.sendMessage("§cDisasterManager bulunamadı!");
            return true;
        }
        String command = args[1].toLowerCase();

        switch (command) {
            case "start":
                return handleDisasterStart(p, args, disasterManager);
            case "stop":
            case "durdur":
                return handleDisasterStop(p, disasterManager);
            case "info":
            case "bilgi":
                return handleDisasterInfo(p, disasterManager);
            case "list":
            case "liste":
                return handleDisasterList(p);
            case "clear":
            case "yok":
            case "temizle":
                return handleDisasterClear(p, disasterManager);
            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft disaster <start|stop|info|list|clear>");
                return true;
        }
    }

    /**
     * Felaket başlat
     */
    private boolean handleDisasterStart(Player p, String[] args, DisasterManager disasterManager) {
        if (args.length < 3) {
            p.sendMessage("§cKullanım: /stratocraft disaster start <type> [level] [konum]");
            p.sendMessage("§7Level: 1-3 (varsayılan: tip'e göre)");
            p.sendMessage("§7Konum: 'ben' veya 'X Y Z'");
            return true;
        }

        String disasterName = args[2].toUpperCase(java.util.Locale.ENGLISH);
        Disaster.Type type;
        try {
            type = Disaster.Type.valueOf(disasterName);
        } catch (IllegalArgumentException e) {
            p.sendMessage("§cGeçersiz felaket tipi: §e" + args[2]);
            return true;
        }

        // Seviye belirleme
        int level = Disaster.getDefaultLevel(type);
        if (args.length >= 4) {
            try {
                int inputLevel = Integer.parseInt(args[3]);
                if (inputLevel >= 1 && inputLevel <= 3) {
                    level = inputLevel;
                } else {
                    p.sendMessage("§cSeviye 1-3 arası olmalı!");
                    return true;
                }
            } catch (NumberFormatException ex) {
                // Seviye değil, konum olabilir
            }
        }

        // Konum belirleme
        org.bukkit.Location spawnLoc = null;
        int locationArgIndex = args.length >= 4 && !args[3].matches("\\d+") ? 3 : args.length >= 5 ? 4 : -1;

        if (locationArgIndex > 0 && locationArgIndex < args.length) {
            if (args[locationArgIndex].equalsIgnoreCase("ben") ||
                    args[locationArgIndex].equalsIgnoreCase("me") ||
                    args[locationArgIndex].equalsIgnoreCase("self")) {
                spawnLoc = p.getLocation().clone();
            } else if (args.length >= locationArgIndex + 3) {
                try {
                    double x = Double.parseDouble(args[locationArgIndex]);
                    double y = Double.parseDouble(args[locationArgIndex + 1]);
                    double z = Double.parseDouble(args[locationArgIndex + 2]);
                    spawnLoc = new org.bukkit.Location(p.getWorld(), x, y, z);
                } catch (NumberFormatException ex) {
                    p.sendMessage("§cGeçersiz koordinatlar!");
                    return true;
                }
            }
        }

        // Felaketi başlat
        if (spawnLoc != null) {
            disasterManager.triggerDisaster(type, level, spawnLoc);
        } else {
            disasterManager.triggerDisaster(type, level);
        }

        p.sendMessage("§a§lFELAKET BAŞLATILDI!");
        p.sendMessage("§7Tip: §e" + disasterManager.getDisasterDisplayName(type));
        p.sendMessage("§7Seviye: §e" + level);

        return true;
    }

    /**
     * Felaketi durdur
     */
    private boolean handleDisasterStop(Player p, DisasterManager disasterManager) {
        Disaster activeDisaster = disasterManager.getActiveDisaster();
        if (activeDisaster == null) {
            p.sendMessage("§cAktif felaket yok!");
            return true;
        }

        activeDisaster.kill();
        disasterManager.setActiveDisaster(null);

        org.bukkit.Bukkit.broadcastMessage("§a§lFELAKET DURDURULDU!");
        p.sendMessage("§aFelaket admin tarafından durduruldu.");

        return true;
    }

    /**
     * Felaket bilgisi
     */
    private boolean handleDisasterInfo(Player p, DisasterManager disasterManager) {
        Disaster activeDisaster = disasterManager.getActiveDisaster();
        if (activeDisaster == null) {
            p.sendMessage("§cAktif felaket yok!");
            return true;
        }

        p.sendMessage("§6=== FELAKET BİLGİLERİ ===");
        p.sendMessage("§7Tip: §e" + disasterManager.getDisasterDisplayName(activeDisaster.getType()));
        p.sendMessage("§7Kategori: §e"
                + (activeDisaster.getCategory() == Disaster.Category.CREATURE ? "Canlı" : "Doğa Olayı"));
        p.sendMessage("§7Seviye: §e" + activeDisaster.getLevel());
        p.sendMessage("§7Can: §c"
                + String.format("%.0f/%.0f", activeDisaster.getCurrentHealth(), activeDisaster.getMaxHealth()));
        p.sendMessage("§7Hasar Çarpanı: §e" + String.format("%.2f", activeDisaster.getDamageMultiplier()) + "x");
        p.sendMessage("§7Kalan Süre: §e" + formatTime(activeDisaster.getRemainingTime()));

        return true;
    }

    /**
     * Felaket listesi
     */
    private boolean handleDisasterList(Player p) {
        p.sendMessage("§6=== FELAKET TİPLERİ ===");
        p.sendMessage("§7§lCanlı Felaketler:");
        p.sendMessage("§7  §eTITAN_GOLEM §7- Seviye 3 - Titan Golem");
        p.sendMessage("§7  §eABYSSAL_WORM §7- Seviye 2 - Hiçlik Solucanı");
        p.sendMessage("§7  §eCHAOS_DRAGON §7- Seviye 3 - Khaos Ejderi");
        p.sendMessage("§7  §eVOID_TITAN §7- Seviye 3 - Boşluk Titanı");
        p.sendMessage("§7§lDoğa Olayları:");
        p.sendMessage("§7  §eSOLAR_FLARE §7- Seviye 1 - Güneş Patlaması");
        p.sendMessage("§7  §eEARTHQUAKE §7- Seviye 2 - Deprem");
        p.sendMessage("§7  §eMETEOR_SHOWER §7- Seviye 2 - Meteor Yağmuru");
        p.sendMessage("§7  §eVOLCANIC_ERUPTION §7- Seviye 3 - Volkanik Patlama");

        return true;
    }

    /**
     * Felaketi yok et (eski komut)
     */
    private boolean handleDisasterClear(Player p, DisasterManager disasterManager) {
        return handleDisasterStop(p, disasterManager);
    }

    /**
     * Zaman formatla
     */
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
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

    /**
     * İttifak yönetimi
     */
    private boolean handleAlliance(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft alliance <komut>");
            p.sendMessage("§7Komutlar:");
            p.sendMessage("§7  list - Tüm aktif ittifakları listele");
            p.sendMessage("§7  create <klan1> <klan2> <tip> [süre_gün] - İttifak oluştur");
            p.sendMessage("§7  break <ittifak_id> - İttifakı boz (admin)");
            p.sendMessage("§7  info <klan> - Klanın ittifaklarını göster");
            p.sendMessage("§eİttifak Tipleri:");
            p.sendMessage("§7  defensive - Savunma İttifakı");
            p.sendMessage("§7  offensive - Saldırı İttifakı");
            p.sendMessage("§7  trade - Ticaret İttifakı");
            p.sendMessage("§7  full - Tam İttifak");
            p.sendMessage("§eÖrnek:");
            p.sendMessage("§7  /stratocraft alliance list");
            p.sendMessage("§7  /stratocraft alliance create KlanA KlanB defensive 7");
            p.sendMessage("§7  /stratocraft alliance info KlanA");
            return true;
        }

        me.mami.stratocraft.manager.AllianceManager allianceManager = plugin.getAllianceManager();
        if (allianceManager == null) {
            p.sendMessage("§cAllianceManager bulunamadı!");
            return true;
        }

        String command = args[1].toLowerCase();

        switch (command) {
            case "list":
            case "liste":
                return handleAllianceList(p, allianceManager);
            case "create":
            case "olustur":
                return handleAllianceCreate(p, args, allianceManager);
            case "break":
            case "boz":
                return handleAllianceBreak(p, args, allianceManager);
            case "info":
            case "bilgi":
                return handleAllianceInfo(p, args, allianceManager);
            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft alliance <list|create|break|info>");
                return true;
        }
    }

    private boolean handleAllianceList(Player p, me.mami.stratocraft.manager.AllianceManager allianceManager) {
        java.util.List<me.mami.stratocraft.model.Alliance> alliances = allianceManager.getAllAlliances();
        
        if (alliances.isEmpty()) {
            p.sendMessage("§eAktif ittifak yok.");
            return true;
        }

        p.sendMessage("§6=== Aktif İttifaklar ===");
        for (me.mami.stratocraft.model.Alliance alliance : alliances) {
            if (!alliance.isActive()) continue;
            
            me.mami.stratocraft.model.Clan clan1 = plugin.getClanManager().getClan(alliance.getClan1Id());
            me.mami.stratocraft.model.Clan clan2 = plugin.getClanManager().getClan(alliance.getClan2Id());
            
            String clan1Name = clan1 != null ? clan1.getName() : "Bilinmeyen";
            String clan2Name = clan2 != null ? clan2.getName() : "Bilinmeyen";
            String typeName = alliance.getType().name();
            long remaining = alliance.getExpiresAt() > 0 ? 
                (alliance.getExpiresAt() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000) : -1;
            
            p.sendMessage("§7- §e" + clan1Name + " §7<-> §e" + clan2Name);
            p.sendMessage("  §7Tip: §a" + typeName + " §7| ID: §f" + alliance.getId().toString().substring(0, 8));
            if (remaining > 0) {
                p.sendMessage("  §7Kalan Süre: §e" + remaining + " gün");
            } else if (remaining == -1) {
                p.sendMessage("  §7Süre: §aSüresiz");
            }
        }
        return true;
    }

    private boolean handleAllianceCreate(Player p, String[] args, me.mami.stratocraft.manager.AllianceManager allianceManager) {
        if (args.length < 5) {
            p.sendMessage("§cKullanım: /stratocraft alliance create <klan1> <klan2> <tip> [süre_gün]");
            return true;
        }

        String clan1Name = args[2];
        String clan2Name = args[3];
        String typeStr = args[4].toUpperCase();
        long durationDays = args.length > 5 ? Long.parseLong(args[5]) : 0; // 0 = süresiz

        me.mami.stratocraft.model.Clan clan1 = plugin.getClanManager().getClanByName(clan1Name);
        me.mami.stratocraft.model.Clan clan2 = plugin.getClanManager().getClanByName(clan2Name);

        if (clan1 == null) {
            p.sendMessage("§cKlan bulunamadı: " + clan1Name);
            return true;
        }
        if (clan2 == null) {
            p.sendMessage("§cKlan bulunamadı: " + clan2Name);
            return true;
        }
        if (clan1.getId().equals(clan2.getId())) {
            p.sendMessage("§cAynı klan ile ittifak oluşturulamaz!");
            return true;
        }

        me.mami.stratocraft.model.Alliance.Type type;
        try {
            type = me.mami.stratocraft.model.Alliance.Type.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            p.sendMessage("§cGeçersiz ittifak tipi! (defensive, offensive, trade, full)");
            return true;
        }

        if (allianceManager.hasAlliance(clan1.getId(), clan2.getId())) {
            p.sendMessage("§cBu klanlar zaten ittifak içinde!");
            return true;
        }

        me.mami.stratocraft.model.Alliance alliance = allianceManager.createAlliance(
            clan1.getId(), clan2.getId(), type, durationDays
        );

        if (alliance != null) {
            p.sendMessage("§aİttifak oluşturuldu!");
            p.sendMessage("§7" + clan1.getName() + " <-> " + clan2.getName());
            p.sendMessage("§7Tip: §e" + type.name() + " §7| Süre: §e" + (durationDays > 0 ? durationDays + " gün" : "Süresiz"));
            org.bukkit.Bukkit.broadcastMessage("§6[İTTİFAK] §e" + clan1.getName() + " §7ve §e" + clan2.getName() + 
                " §7ittifak kurdu! (§a" + type.name() + "§7)");
        } else {
            p.sendMessage("§cİttifak oluşturulamadı!");
        }
        return true;
    }

    private boolean handleAllianceBreak(Player p, String[] args, me.mami.stratocraft.manager.AllianceManager allianceManager) {
        if (args.length < 3) {
            p.sendMessage("§cKullanım: /stratocraft alliance break <ittifak_id>");
            return true;
        }

        java.util.UUID allianceId;
        try {
            allianceId = java.util.UUID.fromString(args[2]);
        } catch (IllegalArgumentException e) {
            p.sendMessage("§cGeçersiz ittifak ID!");
            return true;
        }

        me.mami.stratocraft.model.Alliance alliance = allianceManager.getAlliance(allianceId);
        if (alliance == null || !alliance.isActive()) {
            p.sendMessage("§cAktif ittifak bulunamadı!");
            return true;
        }

        // Admin olarak ittifakı boz
        allianceManager.breakAlliance(allianceId, alliance.getClan1Id());
        p.sendMessage("§aİttifak bozuldu!");
        return true;
    }

    private boolean handleAllianceInfo(Player p, String[] args, me.mami.stratocraft.manager.AllianceManager allianceManager) {
        if (args.length < 3) {
            p.sendMessage("§cKullanım: /stratocraft alliance info <klan>");
            return true;
        }

        String clanName = args[2];
        me.mami.stratocraft.model.Clan clan = plugin.getClanManager().getClanByName(clanName);
        if (clan == null) {
            p.sendMessage("§cKlan bulunamadı: " + clanName);
            return true;
        }

        java.util.List<me.mami.stratocraft.model.Alliance> alliances = allianceManager.getAlliances(clan.getId());
        
        if (alliances.isEmpty()) {
            p.sendMessage("§e" + clan.getName() + " klanının aktif ittifakı yok.");
            return true;
        }

        p.sendMessage("§6=== " + clan.getName() + " İttifakları ===");
        for (me.mami.stratocraft.model.Alliance alliance : alliances) {
            me.mami.stratocraft.model.Clan otherClan = plugin.getClanManager().getClan(
                alliance.getOtherClan(clan.getId())
            );
            String otherClanName = otherClan != null ? otherClan.getName() : "Bilinmeyen";
            long remaining = alliance.getExpiresAt() > 0 ? 
                (alliance.getExpiresAt() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000) : -1;
            
            p.sendMessage("§7- §e" + otherClanName);
            p.sendMessage("  §7Tip: §a" + alliance.getType().name());
            if (remaining > 0) {
                p.sendMessage("  §7Kalan Süre: §e" + remaining + " gün");
            } else if (remaining == -1) {
                p.sendMessage("  §7Süre: §aSüresiz");
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(langManager.getMessage("admin.help-title"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "help", "description",
                "Bu yardım menüsünü gösterir"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "give <item> [miktar]",
                "description", "Özel item verir"));
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "spawn <mob>", "description",
                "Özel canlı çağırır (titan_golem, supply_drop dahil)"));
        sender.sendMessage(
                langManager.getMessage("admin.help-command", "command", "disaster <type> [konum]", "description",
                        "Felaket tetikler"));
        sender.sendMessage("§e  - §7Konum: boş = uzak, 'ben' = yanında, 'X Y Z' = koordinat");
        sender.sendMessage("§e  - §7/stratocraft disaster clear §7- Felaketi yok et");
        sender.sendMessage(langManager.getMessage("admin.help-command", "command", "list <items|mobs|disasters|all>",
                "description", "Listeleri gösterir"));
        sender.sendMessage("§eYeni Komutlar:");
        sender.sendMessage("§7  Özel Eşyalar: /stratocraft give tool rusty_hook, titan_grapple, trap_core, spyglass, taming_core, summon_core, breeding_core, gender_scanner");
        sender.sendMessage("§7  Güçlü Yiyecekler: /stratocraft give material life_elixir, power_fruit, speed_elixir, regeneration_elixir, strength_elixir");
        sender.sendMessage("§7  Yeni Madenler: /stratocraft give sulfur, bauxite, rock_salt, mithril, astral_crystal");
        sender.sendMessage("§7  Yeni Moblar: /stratocraft spawn titan_golem, supply_drop");
        sender.sendMessage("§7  /stratocraft siege <clear|list|start|surrender> §7- Savaş yönetimi");
        sender.sendMessage("§7  /stratocraft caravan <list|clear> §7- Kervanları yönet");
        sender.sendMessage("§7  /stratocraft contract <list|clear> §7- Kontratları yönet");
        sender.sendMessage("§7  /stratocraft build <type> [level] §7- Yapı oluştur");
        sender.sendMessage("§7  /stratocraft trap <list|give|build> §7- Tuzak sistemi");
        sender.sendMessage("§7  /stratocraft mine <list|give> §7- Mayın sistemi");
        sender.sendMessage("§7  /stratocraft boss <spawn|list|ritual|build> §7- Boss sistemi");
        sender.sendMessage("§7  /stratocraft tame <ritual|list|info|build|facility> §7- Canlı eğitme sistemi");
        sender.sendMessage("§7  /stratocraft dungeon <spawn|list|clear> §7- Zindan yönetimi");
        sender.sendMessage("§7  /stratocraft biome <list|set> §7- Biyom yönetimi");
        sender.sendMessage("");
        sender.sendMessage(langManager.getMessage("admin.help-examples"));
        sender.sendMessage("§7  /stratocraft give blueprint 64");
        sender.sendMessage("§7  /stratocraft give rusty_hook");
        sender.sendMessage("§7  /stratocraft give trap_core");
        sender.sendMessage("§7  /stratocraft spawn hell_dragon");
        sender.sendMessage("§7  /stratocraft spawn titan_golem");
        sender.sendMessage("§7  /stratocraft spawn supply_drop");
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
        p.sendMessage("§7--- Özel Eşyalar ---");
        p.sendMessage("§e- rusty_hook §7- Paslı Kanca");
        p.sendMessage("§e- titan_grapple §7- Titan Kancası");
        p.sendMessage("§e- trap_core §7- Tuzak Çekirdeği");
        p.sendMessage("§e- spyglass §7- Casusluk Dürbünü");
        p.sendMessage("§7--- Yeni Madenler ---");
        p.sendMessage("§e- sulfur, sulfur_ore §7- Kükürt");
        p.sendMessage("§e- bauxite, bauxite_ore §7- Boksit");
        p.sendMessage("§e- rock_salt, rock_salt_ore §7- Tuz Kayası");
        p.sendMessage("§e- mithril, mithril_ore, mithril_string §7- Mithril");
        p.sendMessage("§e- astral_crystal, astral_ore §7- Astral");
        p.sendMessage("§7--- Diğer Özel İtemler ---");
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
        p.sendMessage("§e17. §7life_elixir §7- Yaşam İksiri (Canı fulleyen)");
        p.sendMessage("§e18. §7power_fruit §7- Güç Meyvesi (Hasarı 5 kat arttıran)");
        p.sendMessage("§e19. §7speed_elixir §7- Hız İksiri (Hızı arttıran)");
        p.sendMessage("§e20. §7regeneration_elixir §7- Yenilenme İksiri (Hızlı can yenileme)");
        p.sendMessage("§e21. §7strength_elixir §7- Güç İksiri (Güç artışı)");
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
        p.sendMessage("§7--- Yeni Moblar ---");
        p.sendMessage("§e26. §7titan_golem §7- Titan Golem");
        p.sendMessage("§7--- Özel Spawnlar ---");
        p.sendMessage("§e27. §7supply_drop §7- Hava Drop");
    }

    private void showDisastersList(Player p) {
        p.sendMessage("§6§l=== FELAKETLER ===");
        p.sendMessage("§e1. §7titan_golem §7- Titan Golem");
        p.sendMessage("§e2. §7abyssal_worm §7- Hiçlik Solucanı");
        p.sendMessage("§e3. §7solar_flare §7- Güneş Fırtınası");
    }

    /**
     * Seviyeli item al (yeni format: weapon/armor seviye tip)
     */
    private ItemStack getLeveledItemByName(String category, int level, String type) {
        if (category.equals("weapon")) {
            // Silah tipleri: sword(1), axe(2), spear(3), bow(4), hammer(5)
            int variant = -1;
            switch (type.toLowerCase()) {
                case "sword":
                case "kılıç":
                case "kilic":
                    variant = 1;
                    break;
                case "axe":
                case "balta":
                    variant = 2;
                    break;
                case "spear":
                case "mızrak":
                case "mizrak":
                case "trident":
                    variant = 3;
                    break;
                case "bow":
                case "yay":
                    variant = 4;
                    break;
                case "hammer":
                case "çekiç":
                case "cekiç":
                case "pickaxe":
                    variant = 5;
                    break;
                default:
                    return null;
            }
            
            // Seviye ve variant'a göre item döndür
            switch (level) {
                case 1:
                    // Yeni sistem: SpecialItemManager kullan
                    if (plugin.getSpecialItemManager() != null) {
                        switch (variant) {
                            case 1: return plugin.getSpecialItemManager().getTier1Weapon("l1_1");
                            case 2: return plugin.getSpecialItemManager().getTier1Weapon("l1_2");
                            case 3: return plugin.getSpecialItemManager().getTier1Weapon("l1_3");
                            case 4: return plugin.getSpecialItemManager().getTier1Weapon("l1_4");
                            case 5: return plugin.getSpecialItemManager().getTier1Weapon("l1_5");
                        }
                    }
                    // Eski sistem: ItemManager fallback
                    switch (variant) {
                        case 1: return ItemManager.WEAPON_L1_1 != null ? ItemManager.WEAPON_L1_1.clone() : null;
                        case 2: return ItemManager.WEAPON_L1_2 != null ? ItemManager.WEAPON_L1_2.clone() : null;
                        case 3: return ItemManager.WEAPON_L1_3 != null ? ItemManager.WEAPON_L1_3.clone() : null;
                        case 4: return ItemManager.WEAPON_L1_4 != null ? ItemManager.WEAPON_L1_4.clone() : null;
                        case 5: return ItemManager.WEAPON_L1_5 != null ? ItemManager.WEAPON_L1_5.clone() : null;
                    }
                    break;
                case 2:
                    // Yeni sistem: SpecialItemManager kullan
                    if (plugin.getSpecialItemManager() != null) {
                        switch (variant) {
                            case 1: return plugin.getSpecialItemManager().getTier2Weapon("l2_1");
                            case 2: return plugin.getSpecialItemManager().getTier2Weapon("l2_2");
                            case 3: return plugin.getSpecialItemManager().getTier2Weapon("l2_3");
                            case 4: return plugin.getSpecialItemManager().getTier2Weapon("l2_4");
                            case 5: return plugin.getSpecialItemManager().getTier2Weapon("l2_5");
                        }
                    }
                    // Eski sistem: ItemManager fallback
                    switch (variant) {
                        case 1: return ItemManager.WEAPON_L2_1 != null ? ItemManager.WEAPON_L2_1.clone() : null;
                        case 2: return ItemManager.WEAPON_L2_2 != null ? ItemManager.WEAPON_L2_2.clone() : null;
                        case 3: return ItemManager.WEAPON_L2_3 != null ? ItemManager.WEAPON_L2_3.clone() : null;
                        case 4: return ItemManager.WEAPON_L2_4 != null ? ItemManager.WEAPON_L2_4.clone() : null;
                        case 5: return ItemManager.WEAPON_L2_5 != null ? ItemManager.WEAPON_L2_5.clone() : null;
                    }
                    break;
                case 3:
                    // Yeni sistem: SpecialItemManager kullan
                    if (plugin.getSpecialItemManager() != null) {
                        switch (variant) {
                            case 1: return plugin.getSpecialItemManager().getTier3Weapon("l3_1");
                            case 2: return plugin.getSpecialItemManager().getTier3Weapon("l3_2");
                            case 3: return plugin.getSpecialItemManager().getTier3Weapon("l3_3");
                            case 4: return plugin.getSpecialItemManager().getTier3Weapon("l3_4");
                            case 5: return plugin.getSpecialItemManager().getTier3Weapon("l3_5");
                        }
                    }
                    // Eski sistem: ItemManager fallback
                    switch (variant) {
                        case 1: return ItemManager.WEAPON_L3_1 != null ? ItemManager.WEAPON_L3_1.clone() : null;
                        case 2: return ItemManager.WEAPON_L3_2 != null ? ItemManager.WEAPON_L3_2.clone() : null;
                        case 3: return ItemManager.WEAPON_L3_3 != null ? ItemManager.WEAPON_L3_3.clone() : null;
                        case 4: return ItemManager.WEAPON_L3_4 != null ? ItemManager.WEAPON_L3_4.clone() : null;
                        case 5: return ItemManager.WEAPON_L3_5 != null ? ItemManager.WEAPON_L3_5.clone() : null;
                    }
                    break;
                case 4:
                    // Yeni sistem: SpecialItemManager kullan
                    if (plugin.getSpecialItemManager() != null) {
                        switch (variant) {
                            case 1: return plugin.getSpecialItemManager().getTier4Weapon("l4_1");
                            case 2: return plugin.getSpecialItemManager().getTier4Weapon("l4_2");
                            case 3: return plugin.getSpecialItemManager().getTier4Weapon("l4_3");
                            case 4: return plugin.getSpecialItemManager().getTier4Weapon("l4_4");
                            case 5: return plugin.getSpecialItemManager().getTier4Weapon("l4_5");
                        }
                    }
                    // Eski sistem: ItemManager fallback
                    switch (variant) {
                        case 1: return ItemManager.WEAPON_L4_1 != null ? ItemManager.WEAPON_L4_1.clone() : null;
                        case 2: return ItemManager.WEAPON_L4_2 != null ? ItemManager.WEAPON_L4_2.clone() : null;
                        case 3: return ItemManager.WEAPON_L4_3 != null ? ItemManager.WEAPON_L4_3.clone() : null;
                        case 4: return ItemManager.WEAPON_L4_4 != null ? ItemManager.WEAPON_L4_4.clone() : null;
                        case 5: return ItemManager.WEAPON_L4_5 != null ? ItemManager.WEAPON_L4_5.clone() : null;
                    }
                    break;
                case 5:
                    // Yeni sistem: SpecialItemManager kullan
                    if (plugin.getSpecialItemManager() != null) {
                        switch (variant) {
                            case 1: return plugin.getSpecialItemManager().getTier5Weapon("l5_1");
                            case 2: return plugin.getSpecialItemManager().getTier5Weapon("l5_2");
                            case 3: return plugin.getSpecialItemManager().getTier5Weapon("l5_3");
                            case 4: return plugin.getSpecialItemManager().getTier5Weapon("l5_4");
                            case 5: return plugin.getSpecialItemManager().getTier5Weapon("l5_5");
                        }
                    }
                    // Eski sistem: ItemManager fallback
                    switch (variant) {
                        case 1: return ItemManager.WEAPON_L5_1 != null ? ItemManager.WEAPON_L5_1.clone() : null;
                        case 2: return ItemManager.WEAPON_L5_2 != null ? ItemManager.WEAPON_L5_2.clone() : null;
                        case 3: return ItemManager.WEAPON_L5_3 != null ? ItemManager.WEAPON_L5_3.clone() : null;
                        case 4: return ItemManager.WEAPON_L5_4 != null ? ItemManager.WEAPON_L5_4.clone() : null;
                        case 5: return ItemManager.WEAPON_L5_5 != null ? ItemManager.WEAPON_L5_5.clone() : null;
                    }
                    break;
            }
        } else if (category.equals("armor")) {
            // Zırh tipleri: helmet(1), chestplate(2), leggings(3), boots(4)
            int variant = -1;
            switch (type.toLowerCase()) {
                case "helmet":
                case "kask":
                case "başlık":
                case "baslik":
                    variant = 1;
                    break;
                case "chestplate":
                case "göğüslük":
                case "gogusluk":
                case "zırh":
                case "zirh":
                    variant = 2;
                    break;
                case "leggings":
                case "pantolon":
                case "dizlik":
                    variant = 3;
                    break;
                case "boots":
                case "bot":
                case "çizme":
                case "cizme":
                    variant = 4;
                    break;
                default:
                    return null;
            }
            
            // Seviye ve variant'a göre item döndür
            switch (level) {
                case 1:
                    switch (variant) {
                        case 1: return ItemManager.ARMOR_L1_1 != null ? ItemManager.ARMOR_L1_1.clone() : null;
                        case 2: return ItemManager.ARMOR_L1_2 != null ? ItemManager.ARMOR_L1_2.clone() : null;
                        case 3: return ItemManager.ARMOR_L1_3 != null ? ItemManager.ARMOR_L1_3.clone() : null;
                        case 4: return ItemManager.ARMOR_L1_4 != null ? ItemManager.ARMOR_L1_4.clone() : null;
                    }
                    break;
                case 2:
                    switch (variant) {
                        case 1: return ItemManager.ARMOR_L2_1 != null ? ItemManager.ARMOR_L2_1.clone() : null;
                        case 2: return ItemManager.ARMOR_L2_2 != null ? ItemManager.ARMOR_L2_2.clone() : null;
                        case 3: return ItemManager.ARMOR_L2_3 != null ? ItemManager.ARMOR_L2_3.clone() : null;
                        case 4: return ItemManager.ARMOR_L2_4 != null ? ItemManager.ARMOR_L2_4.clone() : null;
                    }
                    break;
                case 3:
                    switch (variant) {
                        case 1: return ItemManager.ARMOR_L3_1 != null ? ItemManager.ARMOR_L3_1.clone() : null;
                        case 2: return ItemManager.ARMOR_L3_2 != null ? ItemManager.ARMOR_L3_2.clone() : null;
                        case 3: return ItemManager.ARMOR_L3_3 != null ? ItemManager.ARMOR_L3_3.clone() : null;
                        case 4: return ItemManager.ARMOR_L3_4 != null ? ItemManager.ARMOR_L3_4.clone() : null;
                    }
                    break;
                case 4:
                    switch (variant) {
                        case 1: return ItemManager.ARMOR_L4_1 != null ? ItemManager.ARMOR_L4_1.clone() : null;
                        case 2: return ItemManager.ARMOR_L4_2 != null ? ItemManager.ARMOR_L4_2.clone() : null;
                        case 3: return ItemManager.ARMOR_L4_3 != null ? ItemManager.ARMOR_L4_3.clone() : null;
                        case 4: return ItemManager.ARMOR_L4_4 != null ? ItemManager.ARMOR_L4_4.clone() : null;
                    }
                    break;
                case 5:
                    switch (variant) {
                        case 1: return ItemManager.ARMOR_L5_1 != null ? ItemManager.ARMOR_L5_1.clone() : null;
                        case 2: return ItemManager.ARMOR_L5_2 != null ? ItemManager.ARMOR_L5_2.clone() : null;
                        case 3: return ItemManager.ARMOR_L5_3 != null ? ItemManager.ARMOR_L5_3.clone() : null;
                        case 4: return ItemManager.ARMOR_L5_4 != null ? ItemManager.ARMOR_L5_4.clone() : null;
                    }
                    break;
            }
        }
        
        return null;
    }

    private ItemStack getItemByName(String name, String category) {
        // Eğer kategori "all" ise, tüm kategorilerde ara
        if (category.equals("all")) {
            return getItemByNameAllCategories(name);
        }

        // Kategoriye göre filtrele
        switch (category.toLowerCase()) {
            case "weapon":
                return getItemByNameWeapon(name);
            case "armor":
                return getItemByNameArmor(name);
            case "material":
                return getItemByNameMaterial(name);
            case "mobdrop":
                return getItemByNameMobDrop(name);
            case "special":
                return getItemByNameSpecial(name);
            case "ore":
                return getItemByNameOre(name);
            case "tool":
                return getItemByNameTool(name);
            case "bossitem":
                return getItemByNameBossItem(name);
            case "recipebook":
                return getItemByNameRecipeBook(name);
            default:
                // Bilinmeyen kategori, tüm kategorilerde ara
                return getItemByNameAllCategories(name);
        }
    }

    private ItemStack getItemByNameAllCategories(String name) {
        // Tüm kategorilerde ara
        ItemStack item = getItemByNameWeapon(name);
        if (item != null)
            return item;

        item = getItemByNameArmor(name);
        if (item != null)
            return item;

        item = getItemByNameMaterial(name);
        if (item != null)
            return item;

        item = getItemByNameMobDrop(name);
        if (item != null)
            return item;

        item = getItemByNameSpecial(name);
        if (item != null)
            return item;

        item = getItemByNameOre(name);
        if (item != null)
            return item;

        item = getItemByNameTool(name);
        if (item != null)
            return item;

        item = getItemByNameBossItem(name);
        if (item != null)
            return item;

        item = getItemByNameRecipeBook(name);
        if (item != null)
            return item;

        return null;
    }

    private ItemStack getItemByNameWeapon(String name) {
        // Özel silahlar
        if (name.toLowerCase().startsWith("weapon_l")) {
            String[] parts = name.toLowerCase().split("_");
            if (parts.length >= 3) {
                try {
                    int level = Integer.parseInt(parts[1].replace("l", ""));
                    int variant = Integer.parseInt(parts[2]);
                    
                    if (level >= 1 && level <= 5 && variant >= 1 && variant <= 5) {
                        switch (level) {
                            case 1:
                                switch (variant) {
                                    case 1: return ItemManager.WEAPON_L1_1 != null ? ItemManager.WEAPON_L1_1.clone() : null;
                                    case 2: return ItemManager.WEAPON_L1_2 != null ? ItemManager.WEAPON_L1_2.clone() : null;
                                    case 3: return ItemManager.WEAPON_L1_3 != null ? ItemManager.WEAPON_L1_3.clone() : null;
                                    case 4: return ItemManager.WEAPON_L1_4 != null ? ItemManager.WEAPON_L1_4.clone() : null;
                                    case 5: return ItemManager.WEAPON_L1_5 != null ? ItemManager.WEAPON_L1_5.clone() : null;
                                }
                                break;
                            case 2:
                                switch (variant) {
                                    case 1: return ItemManager.WEAPON_L2_1 != null ? ItemManager.WEAPON_L2_1.clone() : null;
                                    case 2: return ItemManager.WEAPON_L2_2 != null ? ItemManager.WEAPON_L2_2.clone() : null;
                                    case 3: return ItemManager.WEAPON_L2_3 != null ? ItemManager.WEAPON_L2_3.clone() : null;
                                    case 4: return ItemManager.WEAPON_L2_4 != null ? ItemManager.WEAPON_L2_4.clone() : null;
                                    case 5: return ItemManager.WEAPON_L2_5 != null ? ItemManager.WEAPON_L2_5.clone() : null;
                                }
                                break;
                            case 3:
                                switch (variant) {
                                    case 1: return ItemManager.WEAPON_L3_1 != null ? ItemManager.WEAPON_L3_1.clone() : null;
                                    case 2: return ItemManager.WEAPON_L3_2 != null ? ItemManager.WEAPON_L3_2.clone() : null;
                                    case 3: return ItemManager.WEAPON_L3_3 != null ? ItemManager.WEAPON_L3_3.clone() : null;
                                    case 4: return ItemManager.WEAPON_L3_4 != null ? ItemManager.WEAPON_L3_4.clone() : null;
                                    case 5: return ItemManager.WEAPON_L3_5 != null ? ItemManager.WEAPON_L3_5.clone() : null;
                                }
                                break;
                            case 4:
                                switch (variant) {
                                    case 1: return ItemManager.WEAPON_L4_1 != null ? ItemManager.WEAPON_L4_1.clone() : null;
                                    case 2: return ItemManager.WEAPON_L4_2 != null ? ItemManager.WEAPON_L4_2.clone() : null;
                                    case 3: return ItemManager.WEAPON_L4_3 != null ? ItemManager.WEAPON_L4_3.clone() : null;
                                    case 4: return ItemManager.WEAPON_L4_4 != null ? ItemManager.WEAPON_L4_4.clone() : null;
                                    case 5: return ItemManager.WEAPON_L4_5 != null ? ItemManager.WEAPON_L4_5.clone() : null;
                                }
                                break;
                            case 5:
                                switch (variant) {
                                    case 1: return ItemManager.WEAPON_L5_1 != null ? ItemManager.WEAPON_L5_1.clone() : null;
                                    case 2: return ItemManager.WEAPON_L5_2 != null ? ItemManager.WEAPON_L5_2.clone() : null;
                                    case 3: return ItemManager.WEAPON_L5_3 != null ? ItemManager.WEAPON_L5_3.clone() : null;
                                    case 4: return ItemManager.WEAPON_L5_4 != null ? ItemManager.WEAPON_L5_4.clone() : null;
                                    case 5: return ItemManager.WEAPON_L5_5 != null ? ItemManager.WEAPON_L5_5.clone() : null;
                                }
                                break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Geçersiz format
                }
            }
        }
        
        // Eski silahlar
        switch (name.toLowerCase()) {
            case "war_fan":
            case "savas_yelpazesi":
            case "savaş_yelpazesi":
                return ItemManager.WAR_FAN != null ? ItemManager.WAR_FAN.clone() : null;
            case "tower_shield":
            case "kule_kalkani":
            case "kule_kalkanı":
                return ItemManager.TOWER_SHIELD != null ? ItemManager.TOWER_SHIELD.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getItemByNameArmor(String name) {
        // Özel zırhlar
        if (name.toLowerCase().startsWith("armor_l")) {
            String[] parts = name.toLowerCase().split("_");
            if (parts.length >= 3) {
                try {
                    int level = Integer.parseInt(parts[1].replace("l", ""));
                    int variant = Integer.parseInt(parts[2]);
                    
                    if (level >= 1 && level <= 5 && variant >= 1 && variant <= 5) {
                        switch (level) {
                            case 1:
                                switch (variant) {
                                    case 1: return ItemManager.ARMOR_L1_1 != null ? ItemManager.ARMOR_L1_1.clone() : null;
                                    case 2: return ItemManager.ARMOR_L1_2 != null ? ItemManager.ARMOR_L1_2.clone() : null;
                                    case 3: return ItemManager.ARMOR_L1_3 != null ? ItemManager.ARMOR_L1_3.clone() : null;
                                    case 4: return ItemManager.ARMOR_L1_4 != null ? ItemManager.ARMOR_L1_4.clone() : null;
                                    case 5: return ItemManager.ARMOR_L1_5 != null ? ItemManager.ARMOR_L1_5.clone() : null;
                                }
                                break;
                            case 2:
                                switch (variant) {
                                    case 1: return ItemManager.ARMOR_L2_1 != null ? ItemManager.ARMOR_L2_1.clone() : null;
                                    case 2: return ItemManager.ARMOR_L2_2 != null ? ItemManager.ARMOR_L2_2.clone() : null;
                                    case 3: return ItemManager.ARMOR_L2_3 != null ? ItemManager.ARMOR_L2_3.clone() : null;
                                    case 4: return ItemManager.ARMOR_L2_4 != null ? ItemManager.ARMOR_L2_4.clone() : null;
                                    case 5: return ItemManager.ARMOR_L2_5 != null ? ItemManager.ARMOR_L2_5.clone() : null;
                                }
                                break;
                            case 3:
                                switch (variant) {
                                    case 1: return ItemManager.ARMOR_L3_1 != null ? ItemManager.ARMOR_L3_1.clone() : null;
                                    case 2: return ItemManager.ARMOR_L3_2 != null ? ItemManager.ARMOR_L3_2.clone() : null;
                                    case 3: return ItemManager.ARMOR_L3_3 != null ? ItemManager.ARMOR_L3_3.clone() : null;
                                    case 4: return ItemManager.ARMOR_L3_4 != null ? ItemManager.ARMOR_L3_4.clone() : null;
                                    case 5: return ItemManager.ARMOR_L3_5 != null ? ItemManager.ARMOR_L3_5.clone() : null;
                                }
                                break;
                            case 4:
                                switch (variant) {
                                    case 1: return ItemManager.ARMOR_L4_1 != null ? ItemManager.ARMOR_L4_1.clone() : null;
                                    case 2: return ItemManager.ARMOR_L4_2 != null ? ItemManager.ARMOR_L4_2.clone() : null;
                                    case 3: return ItemManager.ARMOR_L4_3 != null ? ItemManager.ARMOR_L4_3.clone() : null;
                                    case 4: return ItemManager.ARMOR_L4_4 != null ? ItemManager.ARMOR_L4_4.clone() : null;
                                    case 5: return ItemManager.ARMOR_L4_5 != null ? ItemManager.ARMOR_L4_5.clone() : null;
                                }
                                break;
                            case 5:
                                switch (variant) {
                                    case 1: return ItemManager.ARMOR_L5_1 != null ? ItemManager.ARMOR_L5_1.clone() : null;
                                    case 2: return ItemManager.ARMOR_L5_2 != null ? ItemManager.ARMOR_L5_2.clone() : null;
                                    case 3: return ItemManager.ARMOR_L5_3 != null ? ItemManager.ARMOR_L5_3.clone() : null;
                                    case 4: return ItemManager.ARMOR_L5_4 != null ? ItemManager.ARMOR_L5_4.clone() : null;
                                    case 5: return ItemManager.ARMOR_L5_5 != null ? ItemManager.ARMOR_L5_5.clone() : null;
                                }
                                break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Geçersiz format
                }
            }
        }
        
        return null;
    }

    private ItemStack getItemByNameMaterial(String name) {
        switch (name.toLowerCase()) {
            case "blueprint":
            case "blueprint_paper":
            case "mühendis_şeması":
                return ItemManager.BLUEPRINT_PAPER != null ? ItemManager.BLUEPRINT_PAPER.clone() : null;
            case "lightning_core":
            case "yıldırım_çekirdeği":
                return ItemManager.LIGHTNING_CORE != null ? ItemManager.LIGHTNING_CORE.clone() : null;
            case "dark_matter":
            case "karanlık_madde":
                return ItemManager.DARK_MATTER != null ? ItemManager.DARK_MATTER.clone() : null;
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
            case "hell_fruit":
            case "cehennem_meyvesi":
                return ItemManager.HELL_FRUIT != null ? ItemManager.HELL_FRUIT.clone() : null;
            case "life_elixir":
            case "yasam_iksiri":
            case "yaşam_iksiri":
                return ItemManager.LIFE_ELIXIR != null ? ItemManager.LIFE_ELIXIR.clone() : null;
            case "power_fruit":
            case "guc_meyvesi":
            case "güç_meyvesi":
                return ItemManager.POWER_FRUIT != null ? ItemManager.POWER_FRUIT.clone() : null;
            case "speed_elixir":
            case "hiz_iksiri":
            case "hız_iksiri":
                return ItemManager.SPEED_ELIXIR != null ? ItemManager.SPEED_ELIXIR.clone() : null;
            case "regeneration_elixir":
            case "yenilenme_iksiri":
                return ItemManager.REGENERATION_ELIXIR != null ? ItemManager.REGENERATION_ELIXIR.clone() : null;
            case "strength_elixir":
            case "guc_iksiri":
            case "güç_iksiri":
                return ItemManager.STRENGTH_ELIXIR != null ? ItemManager.STRENGTH_ELIXIR.clone() : null;
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

    private ItemStack getItemByNameMobDrop(String name) {
        // Seviye belirtilmiş mi kontrol et (level1, level2, vb.)
        String[] parts = name.split("_");
        if (parts.length > 1 && parts[0].equals("level")) {
            // level1_wild_boar_hide formatı
            String level = parts[1];
            String itemName = name.substring(("level" + level + "_").length());
            return getMobDropItemByLevel(level, itemName);
        }

        // Direkt item ismi
        return getMobDropItemDirect(name);
    }

    private ItemStack getMobDropItemByLevel(String level, String itemName) {
        switch (level) {
            case "1":
                return getLevel1MobDrop(itemName);
            case "2":
                return getLevel2MobDrop(itemName);
            case "3":
                return getLevel3MobDrop(itemName);
            case "4":
                return getLevel4MobDrop(itemName);
            case "5":
                return getLevel5MobDrop(itemName);
            default:
                return null;
        }
    }

    private ItemStack getMobDropItemDirect(String name) {
        // Tüm seviyelerde ara
        ItemStack item = getLevel1MobDrop(name);
        if (item != null)
            return item;
        item = getLevel2MobDrop(name);
        if (item != null)
            return item;
        item = getLevel3MobDrop(name);
        if (item != null)
            return item;
        item = getLevel4MobDrop(name);
        if (item != null)
            return item;
        item = getLevel5MobDrop(name);
        if (item != null)
            return item;
        return null;
    }

    private ItemStack getLevel1MobDrop(String name) {
        switch (name.toLowerCase()) {
            case "wild_boar_hide":
            case "yaban_domuzu_postu":
                return ItemManager.WILD_BOAR_HIDE != null ? ItemManager.WILD_BOAR_HIDE.clone() : null;
            case "wild_boar_meat":
            case "yaban_domuzu_eti":
                return ItemManager.WILD_BOAR_MEAT != null ? ItemManager.WILD_BOAR_MEAT.clone() : null;
            case "wolf_fang":
            case "kurt_disi":
                return ItemManager.WOLF_FANG != null ? ItemManager.WOLF_FANG.clone() : null;
            case "wolf_pelt":
            case "kurt_postu":
                return ItemManager.WOLF_PELT != null ? ItemManager.WOLF_PELT.clone() : null;
            case "snake_venom":
            case "yilan_zehri":
                return ItemManager.SNAKE_VENOM != null ? ItemManager.SNAKE_VENOM.clone() : null;
            case "snake_skin":
            case "yilan_derisi":
                return ItemManager.SNAKE_SKIN != null ? ItemManager.SNAKE_SKIN.clone() : null;
            case "eagle_feather":
            case "kartal_tuyu":
                return ItemManager.EAGLE_FEATHER != null ? ItemManager.EAGLE_FEATHER.clone() : null;
            case "eagle_claw":
            case "kartal_pençesi":
                return ItemManager.EAGLE_CLAW != null ? ItemManager.EAGLE_CLAW.clone() : null;
            case "bear_claw":
            case "ayi_pençesi":
                return ItemManager.BEAR_CLAW != null ? ItemManager.BEAR_CLAW.clone() : null;
            case "bear_pelt":
            case "ayi_postu":
                return ItemManager.BEAR_PELT != null ? ItemManager.BEAR_PELT.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getLevel2MobDrop(String name) {
        switch (name.toLowerCase()) {
            case "iron_core":
            case "demir_çekirdek":
                return ItemManager.IRON_CORE != null ? ItemManager.IRON_CORE.clone() : null;
            case "iron_dust":
            case "demir_tozu":
                return ItemManager.IRON_DUST != null ? ItemManager.IRON_DUST.clone() : null;
            case "ice_heart":
            case "buz_kalbi":
                return ItemManager.ICE_HEART != null ? ItemManager.ICE_HEART.clone() : null;
            case "ice_crystal":
            case "buz_kristali":
                return ItemManager.ICE_CRYSTAL != null ? ItemManager.ICE_CRYSTAL.clone() : null;
            case "fire_core":
            case "ates_çekirdeği":
                return ItemManager.FIRE_CORE != null ? ItemManager.FIRE_CORE.clone() : null;
            case "fire_scale":
            case "ates_ölçeği":
                return ItemManager.FIRE_SCALE != null ? ItemManager.FIRE_SCALE.clone() : null;
            case "earth_stone":
            case "toprak_tasi":
                return ItemManager.EARTH_STONE != null ? ItemManager.EARTH_STONE.clone() : null;
            case "earth_dust":
            case "toprak_tozu":
                return ItemManager.EARTH_DUST != null ? ItemManager.EARTH_DUST.clone() : null;
            case "soul_fragment":
            case "ruh_parçası":
                return ItemManager.SOUL_FRAGMENT != null ? ItemManager.SOUL_FRAGMENT.clone() : null;
            case "ghost_dust":
            case "hayalet_tozu":
                return ItemManager.GHOST_DUST != null ? ItemManager.GHOST_DUST.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getLevel3MobDrop(String name) {
        switch (name.toLowerCase()) {
            case "shadow_heart":
            case "golge_kalbi":
                return ItemManager.SHADOW_HEART != null ? ItemManager.SHADOW_HEART.clone() : null;
            case "shadow_scale":
            case "golge_ölçeği":
                return ItemManager.SHADOW_SCALE != null ? ItemManager.SHADOW_SCALE.clone() : null;
            case "light_heart":
            case "isik_kalbi":
                return ItemManager.LIGHT_HEART != null ? ItemManager.LIGHT_HEART.clone() : null;
            case "light_feather":
            case "isik_tuyu":
                return ItemManager.LIGHT_FEATHER != null ? ItemManager.LIGHT_FEATHER.clone() : null;
            case "storm_core":
            case "firtina_çekirdeği":
                return ItemManager.STORM_CORE != null ? ItemManager.STORM_CORE.clone() : null;
            case "storm_dust":
            case "firtina_tozu":
                return ItemManager.STORM_DUST != null ? ItemManager.STORM_DUST.clone() : null;
            case "lava_heart":
            case "lav_kalbi":
                return ItemManager.LAVA_HEART != null ? ItemManager.LAVA_HEART.clone() : null;
            case "lava_scale":
            case "lav_ölçeği":
                return ItemManager.LAVA_SCALE != null ? ItemManager.LAVA_SCALE.clone() : null;
            case "ice_core":
            case "buz_çekirdeği":
                return ItemManager.ICE_CORE != null ? ItemManager.ICE_CORE.clone() : null;
            case "ice_shard":
            case "buz_parçası":
                return ItemManager.ICE_SHARD != null ? ItemManager.ICE_SHARD.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getLevel4MobDrop(String name) {
        switch (name.toLowerCase()) {
            case "devil_blood":
            case "seytan_kani":
            case "şeytan_kanı":
                return ItemManager.DEVIL_BLOOD != null ? ItemManager.DEVIL_BLOOD.clone() : null;
            case "black_dragon_heart":
            case "kara_ejder_kalbi":
                return ItemManager.BLACK_DRAGON_HEART != null ? ItemManager.BLACK_DRAGON_HEART.clone() : null;
            case "black_dragon_scale":
            case "kara_ejder_ölçeği":
                return ItemManager.BLACK_DRAGON_SCALE != null ? ItemManager.BLACK_DRAGON_SCALE.clone() : null;
            case "death_sword_fragment":
            case "olum_kilici_parçası":
                return ItemManager.DEATH_SWORD_FRAGMENT != null ? ItemManager.DEATH_SWORD_FRAGMENT.clone() : null;
            case "death_dust":
            case "olum_tozu":
                return ItemManager.DEATH_DUST != null ? ItemManager.DEATH_DUST.clone() : null;
            case "chaos_core":
            case "kaos_çekirdeği":
                return ItemManager.CHAOS_CORE != null ? ItemManager.CHAOS_CORE.clone() : null;
            case "chaos_scale":
            case "kaos_ölçeği":
                return ItemManager.CHAOS_SCALE != null ? ItemManager.CHAOS_SCALE.clone() : null;
            case "hell_stone":
            case "cehennem_tasi":
                return ItemManager.HELL_STONE != null ? ItemManager.HELL_STONE.clone() : null;
            case "hell_fire":
            case "cehennem_atesi":
                return ItemManager.HELL_FIRE != null ? ItemManager.HELL_FIRE.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getLevel5MobDrop(String name) {
        switch (name.toLowerCase()) {
            case "legendary_dragon_heart":
            case "efsanevi_ejder_kalbi":
                return ItemManager.LEGENDARY_DRAGON_HEART != null ? ItemManager.LEGENDARY_DRAGON_HEART.clone() : null;
            case "legendary_dragon_scale":
            case "efsanevi_ejder_ölçeği":
                return ItemManager.LEGENDARY_DRAGON_SCALE != null ? ItemManager.LEGENDARY_DRAGON_SCALE.clone() : null;
            case "god_blood":
            case "tanri_kani":
                return ItemManager.GOD_BLOOD != null ? ItemManager.GOD_BLOOD.clone() : null;
            case "god_fragment":
            case "tanri_parçası":
                return ItemManager.GOD_FRAGMENT != null ? ItemManager.GOD_FRAGMENT.clone() : null;
            case "void_core":
            case "hiclik_çekirdeği":
                return ItemManager.VOID_CORE != null ? ItemManager.VOID_CORE.clone() : null;
            case "void_dust":
            case "hiclik_tozu":
                return ItemManager.VOID_DUST != null ? ItemManager.VOID_DUST.clone() : null;
            case "time_core":
            case "zaman_çekirdeği":
                return ItemManager.TIME_CORE != null ? ItemManager.TIME_CORE.clone() : null;
            case "time_scale":
            case "zaman_ölçeği":
                return ItemManager.TIME_SCALE != null ? ItemManager.TIME_SCALE.clone() : null;
            case "fate_stone":
            case "kader_tasi":
                return ItemManager.FATE_STONE != null ? ItemManager.FATE_STONE.clone() : null;
            case "fate_fragment":
            case "kader_parçası":
                return ItemManager.FATE_FRAGMENT != null ? ItemManager.FATE_FRAGMENT.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getItemByNameSpecial(String name) {
        switch (name.toLowerCase()) {
            case "red_diamond":
            case "kızıl_elmas":
                return ItemManager.RED_DIAMOND != null ? ItemManager.RED_DIAMOND.clone() : null;
            case "ruby":
            case "yakut":
                return ItemManager.RUBY != null ? ItemManager.RUBY.clone() : null;
            case "adamantite":
                return ItemManager.ADAMANTITE != null ? ItemManager.ADAMANTITE.clone() : null;
            case "titanium_ore":
            case "titanium":
            case "titanyum_parçası":
                return ItemManager.TITANIUM_ORE != null ? ItemManager.TITANIUM_ORE.clone() : null;
            case "titanium_ingot":
            case "titanyum_külçesi":
                return ItemManager.TITANIUM_INGOT != null ? ItemManager.TITANIUM_INGOT.clone() : null;
            case "life_elixir":
            case "yasam_iksiri":
            case "yaşam_iksiri":
                return ItemManager.LIFE_ELIXIR != null ? ItemManager.LIFE_ELIXIR.clone() : null;
            case "power_fruit":
            case "guc_meyvesi":
            case "güç_meyvesi":
                return ItemManager.POWER_FRUIT != null ? ItemManager.POWER_FRUIT.clone() : null;
            case "speed_elixir":
            case "hiz_iksiri":
            case "hız_iksiri":
                return ItemManager.SPEED_ELIXIR != null ? ItemManager.SPEED_ELIXIR.clone() : null;
            case "regeneration_elixir":
            case "yenilenme_iksiri":
                return ItemManager.REGENERATION_ELIXIR != null ? ItemManager.REGENERATION_ELIXIR.clone() : null;
            case "strength_elixir":
            case "guc_iksiri":
            case "güç_iksiri":
                return ItemManager.STRENGTH_ELIXIR != null ? ItemManager.STRENGTH_ELIXIR.clone() : null;
            // Tier 1 Silahlar
            case "l1_1":
            case "l1_1_rogue_dagger":
            case "hiz_hanceri":
            case "hız_hançeri":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier1Weapon("l1_1") : null;
            case "l1_2":
            case "l1_2_harvest_scythe":
            case "ciftci_tirpani":
            case "çiftçi_tırpanı":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier1Weapon("l1_2") : null;
            case "l1_3":
            case "l1_3_gravity_mace":
            case "yercekimi_gurzu":
            case "yerçekimi_gürzü":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier1Weapon("l1_3") : null;
            case "l1_4":
            case "l1_4_boom_bow":
            case "patlayici_yay":
            case "patlayıcı_yay":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier1Weapon("l1_4") : null;
            case "l1_5":
            case "l1_5_vampire_blade":
            case "vampir_disi":
            case "vampir_dişi":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier1Weapon("l1_5") : null;
            // Tier 2 Silahlar
            case "l2_1":
            case "l2_1_inferno_sword":
            case "alev_kilici":
            case "alev_kılıcı":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier2Weapon("l2_1") : null;
            case "l2_2":
            case "l2_2_frost_wand":
            case "buz_asasi":
            case "buz_asası":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier2Weapon("l2_2") : null;
            case "l2_3":
            case "l2_3_venom_spear":
            case "zehirli_mizrak":
            case "zehirli_mızrak":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier2Weapon("l2_3") : null;
            case "l2_4":
            case "l2_4_guardian_shield":
            case "golem_kalkani":
            case "golem_kalkanı":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier2Weapon("l2_4") : null;
            case "l2_5":
            case "l2_5_thunder_axe":
            case "sok_baltasi":
            case "şok_baltası":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier2Weapon("l2_5") : null;
            // Tier 3 Silahlar
            case "l3_1":
            case "l3_1_shadow_katana":
            case "golge_katanasi":
            case "gölge_katanası":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier3Weapon("l3_1") : null;
            case "l3_2":
            case "l3_2_earthshaker":
            case "deprem_cekici":
            case "deprem_çekici":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier3Weapon("l3_2") : null;
            case "l3_3":
            case "l3_3_machine_crossbow":
            case "taramali_yay":
            case "taramalı_yay":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier3Weapon("l3_3") : null;
            case "l3_4":
            case "l3_4_witch_orb":
            case "buyucu_kuresi":
            case "büyücü_küresi":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier3Weapon("l3_4") : null;
            case "l3_5":
            case "l3_5_phantom_dagger":
            case "hayalet_hanceri":
            case "hayalet_hançeri":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier3Weapon("l3_5") : null;
            // Tier 4 Silahlar
            case "l4_1":
            case "l4_1_elementalist":
            case "element_kilici":
            case "element_kılıcı":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier4Weapon("l4_1") : null;
            case "l4_2":
            case "l4_2_life_death":
            case "yasam_olum":
            case "yaşam_ölüm":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier4Weapon("l4_2") : null;
            case "l4_3":
            case "l4_3_mjolnir_v2":
            case "mjolnir":
            case "mjölnir":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier4Weapon("l4_3") : null;
            case "l4_4":
            case "l4_4_ranger_pride":
            case "avci_yayi":
            case "avcı_yayı":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier4Weapon("l4_4") : null;
            case "l4_5":
            case "l4_5_magnetic_glove":
            case "manyetik_eldiven":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier4Weapon("l4_5") : null;
            // Tier 5 Silahlar
            case "l5_1":
            case "l5_1_void_walker":
            case "hiperiyon_kilici":
            case "hiperiyon":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier5Weapon("l5_1") : null;
            case "l5_2":
            case "l5_2_meteor_caller":
            case "meteor_cagiran":
            case "meteor":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier5Weapon("l5_2") : null;
            case "l5_3":
            case "l5_3_titan_slayer":
            case "titan_katili":
            case "titan":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier5Weapon("l5_3") : null;
            case "l5_4":
            case "l5_4_soul_reaper":
            case "ruh_bicen":
            case "ruh_biçen":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier5Weapon("l5_4") : null;
            case "l5_5":
            case "l5_5_time_keeper":
            case "zamani_buken":
            case "zaman":
                return plugin.getSpecialItemManager() != null ? 
                    plugin.getSpecialItemManager().getTier5Weapon("l5_5") : null;
            default:
                return null;
        }
    }

    private ItemStack getItemByNameOre(String name) {
        switch (name.toLowerCase()) {
            case "sulfur":
            case "kukurt":
            case "kükürt":
                return ItemManager.SULFUR != null ? ItemManager.SULFUR.clone() : null;
            case "sulfur_ore":
            case "kukurt_cevheri":
            case "kükürt_cevheri":
                return ItemManager.SULFUR_ORE != null ? ItemManager.SULFUR_ORE.clone() : null;
            case "bauxite":
            case "boksit":
                return ItemManager.BAUXITE_INGOT != null ? ItemManager.BAUXITE_INGOT.clone() : null;
            case "bauxite_ore":
            case "boksit_cevheri":
                return ItemManager.BAUXITE_ORE != null ? ItemManager.BAUXITE_ORE.clone() : null;
            case "rock_salt":
            case "tuz_kayasi":
            case "tuz_kayası":
                return ItemManager.ROCK_SALT != null ? ItemManager.ROCK_SALT.clone() : null;
            case "rock_salt_ore":
            case "tuz_kayasi_cevheri":
            case "tuz_kayası_cevheri":
                return ItemManager.ROCK_SALT_ORE != null ? ItemManager.ROCK_SALT_ORE.clone() : null;
            case "mithril":
            case "mithril_ingot":
                return ItemManager.MITHRIL_INGOT != null ? ItemManager.MITHRIL_INGOT.clone() : null;
            case "mithril_ore":
            case "mithril_cevheri":
                return ItemManager.MITHRIL_ORE != null ? ItemManager.MITHRIL_ORE.clone() : null;
            case "mithril_string":
            case "mithril_ipi":
                return ItemManager.MITHRIL_STRING != null ? ItemManager.MITHRIL_STRING.clone() : null;
            case "astral_crystal":
            case "astral_kristali":
            case "astral_kristal":
                return ItemManager.ASTRAL_CRYSTAL != null ? ItemManager.ASTRAL_CRYSTAL.clone() : null;
            case "astral_ore":
            case "astral_cevheri":
                return ItemManager.ASTRAL_ORE != null ? ItemManager.ASTRAL_ORE.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getItemByNameTool(String name) {
        switch (name.toLowerCase()) {
            case "rusty_hook":
            case "pasli_kanca":
            case "paslı_kanca":
                return ItemManager.RUSTY_HOOK != null ? ItemManager.RUSTY_HOOK.clone() : null;
            case "golden_hook":
            case "altin_kanca":
            case "altın_kanca":
                return ItemManager.GOLDEN_HOOK != null ? ItemManager.GOLDEN_HOOK.clone() : null;
            case "titan_grapple":
            case "titan_kancasi":
            case "titan_kancası":
                return ItemManager.TITAN_GRAPPLE != null ? ItemManager.TITAN_GRAPPLE.clone() : null;
            case "trap_core":
            case "tuzak_cekirdegi":
            case "tuzak_çekirdeği":
                return ItemManager.TRAP_CORE != null ? ItemManager.TRAP_CORE.clone() : null;
            case "taming_core":
            case "egitim_cekirdegi":
            case "eğitim_çekirdeği":
                return ItemManager.TAMING_CORE != null ? ItemManager.TAMING_CORE.clone() : null;
            case "summon_core":
            case "cagirma_cekirdegi":
            case "çağırma_çekirdeği":
                return ItemManager.SUMMON_CORE != null ? ItemManager.SUMMON_CORE.clone() : null;
            case "breeding_core":
            case "ureme_cekirdegi":
            case "üreme_çekirdeği":
                return ItemManager.BREEDING_CORE != null ? ItemManager.BREEDING_CORE.clone() : null;
            case "gender_scanner":
            case "cinsiyet_ayirici":
            case "cinsiyet_ayırıcı":
                return ItemManager.GENDER_SCANNER != null ? ItemManager.GENDER_SCANNER.clone() : null;
            case "spyglass":
            case "durbun":
            case "dürbün":
                return new ItemStack(Material.SPYGLASS);
            default:
                return null;
        }
    }

    private ItemStack getItemByNameBossItem(String name) {
        switch (name.toLowerCase()) {
            case "goblin_crown":
            case "goblin_krali_taci":
            case "goblin_kralı_tacı":
                return ItemManager.GOBLIN_CROWN != null ? ItemManager.GOBLIN_CROWN.clone() : null;
            case "orc_amulet":
            case "ork_sefi_amuleti":
            case "ork_şefi_amuleti":
                return ItemManager.ORC_AMULET != null ? ItemManager.ORC_AMULET.clone() : null;
            case "troll_heart":
            case "troll_krali_kalbi":
            case "troll_kralı_kalbi":
                return ItemManager.TROLL_HEART != null ? ItemManager.TROLL_HEART.clone() : null;
            case "dragon_scale":
            case "ejderha_olcegi":
            case "ejderha_ölçeği":
                return ItemManager.DRAGON_SCALE != null ? ItemManager.DRAGON_SCALE.clone() : null;
            case "trex_tooth":
            case "trex_disi":
            case "t_rex_disi":
                return ItemManager.TREX_TOOTH != null ? ItemManager.TREX_TOOTH.clone() : null;
            case "cyclops_eye":
            case "cyclops_gozu":
            case "cyclops_gözü":
                return ItemManager.CYCLOPS_EYE != null ? ItemManager.CYCLOPS_EYE.clone() : null;
            case "titan_core":
            case "titan_golem_cekirdegi":
            case "titan_golem_çekirdeği":
                return ItemManager.TITAN_CORE != null ? ItemManager.TITAN_CORE.clone() : null;
            case "phoenix_feather":
            case "phoenix_tuyu":
            case "phoenix_tüyü":
                return ItemManager.PHOENIX_FEATHER != null ? ItemManager.PHOENIX_FEATHER.clone() : null;
            case "kraken_tentacle":
            case "kraken_dokunaci":
            case "kraken_dokunaçı":
                return ItemManager.KRAKEN_TENTACLE != null ? ItemManager.KRAKEN_TENTACLE.clone() : null;
            case "demon_lord_horn":
            case "seytan_lordu_boynuzu":
            case "şeytan_lordu_boynuzu":
                return ItemManager.DEMON_LORD_HORN != null ? ItemManager.DEMON_LORD_HORN.clone() : null;
            case "void_dragon_heart":
            case "hiclik_ejderi_kalbi":
            case "hiçlik_ejderi_kalbi":
                return ItemManager.VOID_DRAGON_HEART != null ? ItemManager.VOID_DRAGON_HEART.clone() : null;
            default:
                return null;
        }
    }
    
    private ItemStack getItemByNameRecipeBook(String name) {
        // Özel zırh tarifleri
        if (name.toLowerCase().startsWith("recipe_armor_l")) {
            String[] parts = name.toLowerCase().split("_");
            if (parts.length >= 4) {
                try {
                    int level = Integer.parseInt(parts[3].replace("l", ""));
                    int variant = Integer.parseInt(parts[4]);
                    
                    if (level >= 1 && level <= 5 && variant >= 1 && variant <= 5) {
                        switch (level) {
                            case 1:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_ARMOR_L1_1 != null ? ItemManager.RECIPE_ARMOR_L1_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_ARMOR_L1_2 != null ? ItemManager.RECIPE_ARMOR_L1_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_ARMOR_L1_3 != null ? ItemManager.RECIPE_ARMOR_L1_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_ARMOR_L1_4 != null ? ItemManager.RECIPE_ARMOR_L1_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_ARMOR_L1_5 != null ? ItemManager.RECIPE_ARMOR_L1_5.clone() : null;
                                }
                                break;
                            case 2:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_ARMOR_L2_1 != null ? ItemManager.RECIPE_ARMOR_L2_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_ARMOR_L2_2 != null ? ItemManager.RECIPE_ARMOR_L2_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_ARMOR_L2_3 != null ? ItemManager.RECIPE_ARMOR_L2_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_ARMOR_L2_4 != null ? ItemManager.RECIPE_ARMOR_L2_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_ARMOR_L2_5 != null ? ItemManager.RECIPE_ARMOR_L2_5.clone() : null;
                                }
                                break;
                            case 3:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_ARMOR_L3_1 != null ? ItemManager.RECIPE_ARMOR_L3_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_ARMOR_L3_2 != null ? ItemManager.RECIPE_ARMOR_L3_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_ARMOR_L3_3 != null ? ItemManager.RECIPE_ARMOR_L3_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_ARMOR_L3_4 != null ? ItemManager.RECIPE_ARMOR_L3_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_ARMOR_L3_5 != null ? ItemManager.RECIPE_ARMOR_L3_5.clone() : null;
                                }
                                break;
                            case 4:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_ARMOR_L4_1 != null ? ItemManager.RECIPE_ARMOR_L4_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_ARMOR_L4_2 != null ? ItemManager.RECIPE_ARMOR_L4_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_ARMOR_L4_3 != null ? ItemManager.RECIPE_ARMOR_L4_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_ARMOR_L4_4 != null ? ItemManager.RECIPE_ARMOR_L4_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_ARMOR_L4_5 != null ? ItemManager.RECIPE_ARMOR_L4_5.clone() : null;
                                }
                                break;
                            case 5:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_ARMOR_L5_1 != null ? ItemManager.RECIPE_ARMOR_L5_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_ARMOR_L5_2 != null ? ItemManager.RECIPE_ARMOR_L5_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_ARMOR_L5_3 != null ? ItemManager.RECIPE_ARMOR_L5_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_ARMOR_L5_4 != null ? ItemManager.RECIPE_ARMOR_L5_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_ARMOR_L5_5 != null ? ItemManager.RECIPE_ARMOR_L5_5.clone() : null;
                                }
                                break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Geçersiz format
                }
            }
        }
        
        // Özel silah tarifleri
        if (name.toLowerCase().startsWith("recipe_weapon_l")) {
            String[] parts = name.toLowerCase().split("_");
            if (parts.length >= 4) {
                try {
                    int level = Integer.parseInt(parts[3].replace("l", ""));
                    int variant = Integer.parseInt(parts[4]);
                    
                    if (level >= 1 && level <= 5 && variant >= 1 && variant <= 5) {
                        switch (level) {
                            case 1:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_WEAPON_L1_1 != null ? ItemManager.RECIPE_WEAPON_L1_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_WEAPON_L1_2 != null ? ItemManager.RECIPE_WEAPON_L1_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_WEAPON_L1_3 != null ? ItemManager.RECIPE_WEAPON_L1_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_WEAPON_L1_4 != null ? ItemManager.RECIPE_WEAPON_L1_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_WEAPON_L1_5 != null ? ItemManager.RECIPE_WEAPON_L1_5.clone() : null;
                                }
                                break;
                            case 2:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_WEAPON_L2_1 != null ? ItemManager.RECIPE_WEAPON_L2_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_WEAPON_L2_2 != null ? ItemManager.RECIPE_WEAPON_L2_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_WEAPON_L2_3 != null ? ItemManager.RECIPE_WEAPON_L2_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_WEAPON_L2_4 != null ? ItemManager.RECIPE_WEAPON_L2_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_WEAPON_L2_5 != null ? ItemManager.RECIPE_WEAPON_L2_5.clone() : null;
                                }
                                break;
                            case 3:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_WEAPON_L3_1 != null ? ItemManager.RECIPE_WEAPON_L3_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_WEAPON_L3_2 != null ? ItemManager.RECIPE_WEAPON_L3_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_WEAPON_L3_3 != null ? ItemManager.RECIPE_WEAPON_L3_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_WEAPON_L3_4 != null ? ItemManager.RECIPE_WEAPON_L3_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_WEAPON_L3_5 != null ? ItemManager.RECIPE_WEAPON_L3_5.clone() : null;
                                }
                                break;
                            case 4:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_WEAPON_L4_1 != null ? ItemManager.RECIPE_WEAPON_L4_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_WEAPON_L4_2 != null ? ItemManager.RECIPE_WEAPON_L4_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_WEAPON_L4_3 != null ? ItemManager.RECIPE_WEAPON_L4_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_WEAPON_L4_4 != null ? ItemManager.RECIPE_WEAPON_L4_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_WEAPON_L4_5 != null ? ItemManager.RECIPE_WEAPON_L4_5.clone() : null;
                                }
                                break;
                            case 5:
                                switch (variant) {
                                    case 1: return ItemManager.RECIPE_WEAPON_L5_1 != null ? ItemManager.RECIPE_WEAPON_L5_1.clone() : null;
                                    case 2: return ItemManager.RECIPE_WEAPON_L5_2 != null ? ItemManager.RECIPE_WEAPON_L5_2.clone() : null;
                                    case 3: return ItemManager.RECIPE_WEAPON_L5_3 != null ? ItemManager.RECIPE_WEAPON_L5_3.clone() : null;
                                    case 4: return ItemManager.RECIPE_WEAPON_L5_4 != null ? ItemManager.RECIPE_WEAPON_L5_4.clone() : null;
                                    case 5: return ItemManager.RECIPE_WEAPON_L5_5 != null ? ItemManager.RECIPE_WEAPON_L5_5.clone() : null;
                                }
                                break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Geçersiz format
                }
            }
        }
        
        // Yapı tarif kitapları
        switch (name.toLowerCase()) {
            case "recipe_core":
            case "tarif_cekirdek":
            case "tarif_çekirdek":
            case "tarif_ana_kristal":
                return ItemManager.RECIPE_CORE != null ? ItemManager.RECIPE_CORE.clone() : null;
            case "recipe_alchemy_tower":
            case "tarif_simya_kulesi":
                return ItemManager.RECIPE_ALCHEMY_TOWER != null ? ItemManager.RECIPE_ALCHEMY_TOWER.clone() : null;
            case "recipe_poison_reactor":
            case "tarif_zehir_reaktoru":
                return ItemManager.RECIPE_POISON_REACTOR != null ? ItemManager.RECIPE_POISON_REACTOR.clone() : null;
            case "recipe_siege_factory":
            case "tarif_kuşatma_fabrikasi":
                return ItemManager.RECIPE_SIEGE_FACTORY != null ? ItemManager.RECIPE_SIEGE_FACTORY.clone() : null;
            case "recipe_wall_generator":
            case "tarif_duvar_uretici":
                return ItemManager.RECIPE_WALL_GENERATOR != null ? ItemManager.RECIPE_WALL_GENERATOR.clone() : null;
            case "recipe_gravity_well":
            case "tarif_yercekimi_kuyusu":
                return ItemManager.RECIPE_GRAVITY_WELL != null ? ItemManager.RECIPE_GRAVITY_WELL.clone() : null;
            case "recipe_lava_trencher":
            case "tarif_lav_hendegi":
                return ItemManager.RECIPE_LAVA_TRENCHER != null ? ItemManager.RECIPE_LAVA_TRENCHER.clone() : null;
            case "recipe_watchtower":
            case "tarif_gozetleme_kulesi":
                return ItemManager.RECIPE_WATCHTOWER != null ? ItemManager.RECIPE_WATCHTOWER.clone() : null;
            case "recipe_drone_station":
            case "tarif_drone_istasyonu":
                return ItemManager.RECIPE_DRONE_STATION != null ? ItemManager.RECIPE_DRONE_STATION.clone() : null;
            case "recipe_auto_turret":
            case "tarif_otomatik_taret":
                return ItemManager.RECIPE_AUTO_TURRET != null ? ItemManager.RECIPE_AUTO_TURRET.clone() : null;
            case "recipe_global_market_gate":
            case "tarif_global_pazar_kapisi":
                return ItemManager.RECIPE_GLOBAL_MARKET_GATE != null ? ItemManager.RECIPE_GLOBAL_MARKET_GATE.clone() : null;
            case "recipe_auto_drill":
            case "tarif_otomatik_matkap":
                return ItemManager.RECIPE_AUTO_DRILL != null ? ItemManager.RECIPE_AUTO_DRILL.clone() : null;
            case "recipe_xp_bank":
            case "tarif_xp_bankasi":
                return ItemManager.RECIPE_XP_BANK != null ? ItemManager.RECIPE_XP_BANK.clone() : null;
            case "recipe_mag_rail":
            case "tarif_manyetik_ray":
                return ItemManager.RECIPE_MAG_RAIL != null ? ItemManager.RECIPE_MAG_RAIL.clone() : null;
            case "recipe_teleporter":
            case "tarif_isinlayici":
                return ItemManager.RECIPE_TELEPORTER != null ? ItemManager.RECIPE_TELEPORTER.clone() : null;
            case "recipe_food_silo":
            case "tarif_yemek_silosu":
                return ItemManager.RECIPE_FOOD_SILO != null ? ItemManager.RECIPE_FOOD_SILO.clone() : null;
            case "recipe_oil_refinery":
            case "tarif_petrol_rafinerisi":
                return ItemManager.RECIPE_OIL_REFINERY != null ? ItemManager.RECIPE_OIL_REFINERY.clone() : null;
            case "recipe_healing_beacon":
            case "tarif_iyilestirme_isareti":
                return ItemManager.RECIPE_HEALING_BEACON != null ? ItemManager.RECIPE_HEALING_BEACON.clone() : null;
            case "recipe_weather_machine":
            case "tarif_hava_makinesi":
                return ItemManager.RECIPE_WEATHER_MACHINE != null ? ItemManager.RECIPE_WEATHER_MACHINE.clone() : null;
            case "recipe_crop_accelerator":
            case "tarif_urun_hizlandirici":
                return ItemManager.RECIPE_CROP_ACCELERATOR != null ? ItemManager.RECIPE_CROP_ACCELERATOR.clone() : null;
            case "recipe_mob_grinder":
            case "tarif_mob_ogutucu":
                return ItemManager.RECIPE_MOB_GRINDER != null ? ItemManager.RECIPE_MOB_GRINDER.clone() : null;
            case "recipe_invisibility_cloak":
            case "tarif_gorunmezlik_pelerini":
                return ItemManager.RECIPE_INVISIBILITY_CLOAK != null ? ItemManager.RECIPE_INVISIBILITY_CLOAK.clone() : null;
            case "recipe_armory":
            case "tarif_cephane":
                return ItemManager.RECIPE_ARMORY != null ? ItemManager.RECIPE_ARMORY.clone() : null;
            case "recipe_library":
            case "tarif_kutuphane":
                return ItemManager.RECIPE_LIBRARY != null ? ItemManager.RECIPE_LIBRARY.clone() : null;
            case "recipe_warning_sign":
            case "tarif_uyari_tabelasi":
                return ItemManager.RECIPE_WARNING_SIGN != null ? ItemManager.RECIPE_WARNING_SIGN.clone() : null;
            // Özel eşya tarif kitapları
            case "recipe_lightning_core":
            case "tarif_yildirim_cekirdegi":
                return ItemManager.RECIPE_LIGHTNING_CORE != null ? ItemManager.RECIPE_LIGHTNING_CORE.clone() : null;
            case "recipe_titanium_ingot":
            case "tarif_titanyum_kulcesi":
                return ItemManager.RECIPE_TITANIUM_INGOT != null ? ItemManager.RECIPE_TITANIUM_INGOT.clone() : null;
            case "recipe_dark_matter":
            case "tarif_karanlik_madde":
                return ItemManager.RECIPE_DARK_MATTER != null ? ItemManager.RECIPE_DARK_MATTER.clone() : null;
            case "recipe_red_diamond":
            case "tarif_kizil_elmas":
                return ItemManager.RECIPE_RED_DIAMOND != null ? ItemManager.RECIPE_RED_DIAMOND.clone() : null;
            case "recipe_ruby":
            case "tarif_yakut":
                return ItemManager.RECIPE_RUBY != null ? ItemManager.RECIPE_RUBY.clone() : null;
            case "recipe_adamantite":
            case "tarif_adamantite":
                return ItemManager.RECIPE_ADAMANTITE != null ? ItemManager.RECIPE_ADAMANTITE.clone() : null;
            case "recipe_star_core":
            case "tarif_yildiz_cekirdegi":
                return ItemManager.RECIPE_STAR_CORE != null ? ItemManager.RECIPE_STAR_CORE.clone() : null;
            case "recipe_flame_amplifier":
            case "tarif_alev_amplifikatoru":
                return ItemManager.RECIPE_FLAME_AMPLIFIER != null ? ItemManager.RECIPE_FLAME_AMPLIFIER.clone() : null;
            case "recipe_devil_horn":
            case "tarif_seytan_boynuzu":
                return ItemManager.RECIPE_DEVIL_HORN != null ? ItemManager.RECIPE_DEVIL_HORN.clone() : null;
            case "recipe_devil_snake_eye":
            case "tarif_iblis_yilanin_gozu":
                return ItemManager.RECIPE_DEVIL_SNAKE_EYE != null ? ItemManager.RECIPE_DEVIL_SNAKE_EYE.clone() : null;
            case "recipe_war_fan":
            case "tarif_savas_yelpazesi":
                return ItemManager.RECIPE_WAR_FAN != null ? ItemManager.RECIPE_WAR_FAN.clone() : null;
            case "recipe_tower_shield":
            case "tarif_kule_kalkani":
                return ItemManager.RECIPE_TOWER_SHIELD != null ? ItemManager.RECIPE_TOWER_SHIELD.clone() : null;
            case "recipe_hell_fruit":
            case "tarif_cehennem_meyvesi":
                return ItemManager.RECIPE_HELL_FRUIT != null ? ItemManager.RECIPE_HELL_FRUIT.clone() : null;
            case "recipe_sulfur":
            case "tarif_kukurt":
                return ItemManager.RECIPE_SULFUR != null ? ItemManager.RECIPE_SULFUR.clone() : null;
            case "recipe_bauxite_ingot":
            case "tarif_boksit_kulcesi":
                return ItemManager.RECIPE_BAUXITE_INGOT != null ? ItemManager.RECIPE_BAUXITE_INGOT.clone() : null;
            case "recipe_rock_salt":
            case "tarif_tuz":
                return ItemManager.RECIPE_ROCK_SALT != null ? ItemManager.RECIPE_ROCK_SALT.clone() : null;
            case "recipe_mithril_ingot":
            case "tarif_mithril_kulcesi":
                return ItemManager.RECIPE_MITHRIL_INGOT != null ? ItemManager.RECIPE_MITHRIL_INGOT.clone() : null;
            case "recipe_mithril_string":
            case "tarif_mithril_ipi":
                return ItemManager.RECIPE_MITHRIL_STRING != null ? ItemManager.RECIPE_MITHRIL_STRING.clone() : null;
            case "recipe_astral_ore":
            case "tarif_astral_ceheri":
                return ItemManager.RECIPE_ASTRAL_ORE != null ? ItemManager.RECIPE_ASTRAL_ORE.clone() : null;
            case "recipe_astral_crystal":
            case "tarif_astral_kristali":
                return ItemManager.RECIPE_ASTRAL_CRYSTAL != null ? ItemManager.RECIPE_ASTRAL_CRYSTAL.clone() : null;
            case "recipe_rusty_hook":
            case "tarif_pasli_kanca":
                return ItemManager.RECIPE_RUSTY_HOOK != null ? ItemManager.RECIPE_RUSTY_HOOK.clone() : null;
            case "recipe_golden_hook":
            case "tarif_altin_kanca":
                return ItemManager.RECIPE_GOLDEN_HOOK != null ? ItemManager.RECIPE_GOLDEN_HOOK.clone() : null;
            case "recipe_titan_grapple":
            case "tarif_titan_kancasi":
                return ItemManager.RECIPE_TITAN_GRAPPLE != null ? ItemManager.RECIPE_TITAN_GRAPPLE.clone() : null;
            case "recipe_trap_core":
            case "tarif_tuzak_cekirdegi":
                return ItemManager.RECIPE_TRAP_CORE != null ? ItemManager.RECIPE_TRAP_CORE.clone() : null;
            // Yiyecek tarifleri
            case "recipe_life_elixir":
            case "tarif_yasam_iksiri":
                return ItemManager.RECIPE_LIFE_ELIXIR != null ? ItemManager.RECIPE_LIFE_ELIXIR.clone() : null;
            case "recipe_power_fruit":
            case "tarif_guc_meyvesi":
                return ItemManager.RECIPE_POWER_FRUIT != null ? ItemManager.RECIPE_POWER_FRUIT.clone() : null;
            case "recipe_speed_elixir":
            case "tarif_hiz_iksiri":
                return ItemManager.RECIPE_SPEED_ELIXIR != null ? ItemManager.RECIPE_SPEED_ELIXIR.clone() : null;
            case "recipe_regeneration_elixir":
            case "tarif_yenilenme_iksiri":
                return ItemManager.RECIPE_REGENERATION_ELIXIR != null ? ItemManager.RECIPE_REGENERATION_ELIXIR.clone() : null;
            case "recipe_strength_elixir":
            case "tarif_guc_iksiri":
                return ItemManager.RECIPE_STRENGTH_ELIXIR != null ? ItemManager.RECIPE_STRENGTH_ELIXIR.clone() : null;
            // Maden tarifleri
            case "recipe_sulfur_ore":
            case "tarif_kukurt_ceheri":
                return ItemManager.RECIPE_SULFUR_ORE != null ? ItemManager.RECIPE_SULFUR_ORE.clone() : null;
            case "recipe_bauxite_ore":
            case "tarif_boksit_ceheri":
                return ItemManager.RECIPE_BAUXITE_ORE != null ? ItemManager.RECIPE_BAUXITE_ORE.clone() : null;
            case "recipe_rock_salt_ore":
            case "tarif_tuz_kayasi":
                return ItemManager.RECIPE_ROCK_SALT_ORE != null ? ItemManager.RECIPE_ROCK_SALT_ORE.clone() : null;
            case "recipe_mithril_ore":
            case "tarif_mithril_ceheri":
                return ItemManager.RECIPE_MITHRIL_ORE != null ? ItemManager.RECIPE_MITHRIL_ORE.clone() : null;
            // Çekirdek tarifleri
            case "recipe_taming_core":
            case "tarif_egitim_cekirdegi":
                return ItemManager.RECIPE_TAMING_CORE != null ? ItemManager.RECIPE_TAMING_CORE.clone() : null;
            case "recipe_summon_core":
            case "tarif_cagirma_cekirdegi":
                return ItemManager.RECIPE_SUMMON_CORE != null ? ItemManager.RECIPE_SUMMON_CORE.clone() : null;
            case "recipe_breeding_core":
            case "tarif_ureme_cekirdegi":
                return ItemManager.RECIPE_BREEDING_CORE != null ? ItemManager.RECIPE_BREEDING_CORE.clone() : null;
            case "recipe_gender_scanner":
            case "tarif_cinsiyet_ayirici":
                return ItemManager.RECIPE_GENDER_SCANNER != null ? ItemManager.RECIPE_GENDER_SCANNER.clone() : null;
            // Diğer
            case "recipe_blueprint_paper":
            case "recipe_blueprint":
            case "tarif_muhendis_semasi":
                return ItemManager.RECIPE_BLUEPRINT_PAPER != null ? ItemManager.RECIPE_BLUEPRINT_PAPER.clone() : null;
            case "recipe_titanium_ore":
            case "tarif_titanyum_parcasi":
                return ItemManager.RECIPE_TITANIUM_ORE != null ? ItemManager.RECIPE_TITANIUM_ORE.clone() : null;
            case "recipe_tectonic":
            case "recipe_tectonic_stabilizer":
            case "tarif_tektonik_sabitleyici":
                return ItemManager.RECIPE_TECTONIC_STABILIZER != null ? ItemManager.RECIPE_TECTONIC_STABILIZER.clone() : null;
            default:
                return null;
        }
    }

    // Eski metod - geriye uyumluluk için (getItemByNameAllCategories'yi çağırır)
    private ItemStack getItemByName(String name) {
        return getItemByNameAllCategories(name);
    }

    // Eski metodun içeriği - artık kullanılmıyor ama referans için tutuluyor
    @Deprecated
    private ItemStack getItemByNameOld(String name) {
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
            // Kancalar - 3 Kademeli Sistem
            case "rusty_hook":
            case "pasli_kanca":
            case "paslı_kanca":
                return ItemManager.RUSTY_HOOK != null ? ItemManager.RUSTY_HOOK.clone() : null;

            case "golden_hook":
            case "altin_kanca":
            case "altın_kanca":
                return ItemManager.GOLDEN_HOOK != null ? ItemManager.GOLDEN_HOOK.clone() : null;

            case "titan_grapple":
            case "titan_kancasi":
            case "titan_kancası":
                return ItemManager.TITAN_GRAPPLE != null ? ItemManager.TITAN_GRAPPLE.clone() : null;

            case "trap_core":
            case "tuzak_cekirdegi":
            case "tuzak_çekirdeği":
                return ItemManager.TRAP_CORE != null ? ItemManager.TRAP_CORE.clone() : null;
            case "taming_core":
            case "egitim_cekirdegi":
            case "eğitim_çekirdeği":
                return ItemManager.TAMING_CORE != null ? ItemManager.TAMING_CORE.clone() : null;
            case "summon_core":
            case "cagirma_cekirdegi":
            case "çağırma_çekirdeği":
                return ItemManager.SUMMON_CORE != null ? ItemManager.SUMMON_CORE.clone() : null;
            case "breeding_core":
            case "ureme_cekirdegi":
            case "üreme_çekirdeği":
                return ItemManager.BREEDING_CORE != null ? ItemManager.BREEDING_CORE.clone() : null;
            case "gender_scanner":
            case "cinsiyet_ayirici":
            case "cinsiyet_ayırıcı":
                return ItemManager.GENDER_SCANNER != null ? ItemManager.GENDER_SCANNER.clone() : null;
            // Yeni Madenler
            case "sulfur":
            case "kukurt":
            case "kükürt":
                return ItemManager.SULFUR != null ? ItemManager.SULFUR.clone() : null;
            case "sulfur_ore":
            case "kukurt_cevheri":
            case "kükürt_cevheri":
                return ItemManager.SULFUR_ORE != null ? ItemManager.SULFUR_ORE.clone() : null;
            case "bauxite":
            case "boksit":
                return ItemManager.BAUXITE_INGOT != null ? ItemManager.BAUXITE_INGOT.clone() : null;
            case "bauxite_ore":
            case "boksit_cevheri":
                return ItemManager.BAUXITE_ORE != null ? ItemManager.BAUXITE_ORE.clone() : null;
            case "rock_salt":
            case "tuz_kayasi":
            case "tuz_kayası":
                return ItemManager.ROCK_SALT != null ? ItemManager.ROCK_SALT.clone() : null;
            case "rock_salt_ore":
            case "tuz_kayasi_cevheri":
            case "tuz_kayası_cevheri":
                return ItemManager.ROCK_SALT_ORE != null ? ItemManager.ROCK_SALT_ORE.clone() : null;
            case "mithril":
            case "mithril_ingot":
                return ItemManager.MITHRIL_INGOT != null ? ItemManager.MITHRIL_INGOT.clone() : null;
            case "mithril_ore":
            case "mithril_cevheri":
                return ItemManager.MITHRIL_ORE != null ? ItemManager.MITHRIL_ORE.clone() : null;
            case "mithril_string":
            case "mithril_ipi":
                return ItemManager.MITHRIL_STRING != null ? ItemManager.MITHRIL_STRING.clone() : null;
            case "astral_crystal":
            case "astral_kristali":
            case "astral_kristal":
                return ItemManager.ASTRAL_CRYSTAL != null ? ItemManager.ASTRAL_CRYSTAL.clone() : null;
            case "astral_ore":
            case "astral_cevheri":
                return ItemManager.ASTRAL_ORE != null ? ItemManager.ASTRAL_ORE.clone() : null;
            // Spyglass (normal Material ama özel kullanım)
            case "spyglass":
            case "durbun":
            case "dürbün":
                return new ItemStack(Material.SPYGLASS);
            default:
                return null;
        }
    }

    private ItemStack createClanCrystal() {
        ItemStack crystal = new ItemStack(Material.END_CRYSTAL);
        org.bukkit.inventory.meta.ItemMeta meta = crystal.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lKlan Kristali");
            meta.setLore(Arrays.asList("§7Klan kurmak için kullanılır.", "§7Etrafı Klan Çiti ile çevrili",
                    "§7bir alana koyulmalıdır."));
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

    /**
     * ItemStack'ten display name al
     */
    private String getItemDisplayNameFromStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        if (item.getItemMeta().hasDisplayName()) {
            // Renk kodlarını temizle
            return item.getItemMeta().getDisplayName().replaceAll("§[0-9a-fk-or]", "");
        }
        // Material ismini kullan
        return item.getType().name().toLowerCase().replace("_", " ");
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
        if (lowerName.contains("rusty_hook") || lowerName.contains("pasli_kanca")
                || lowerName.contains("paslı_kanca")) {
            return "Paslı Kanca";
        }
        if (lowerName.contains("titan_grapple") || lowerName.contains("titan_kancasi")
                || lowerName.contains("titan_kancası")) {
            return "Titan Kancası";
        }
        if (lowerName.contains("trap_core") || lowerName.contains("tuzak_cekirdegi")
                || lowerName.contains("tuzak_çekirdeği")) {
            return "Tuzak Çekirdeği";
        }
        if (lowerName.contains("sulfur") || lowerName.contains("kukurt") || lowerName.contains("kükürt")) {
            return "Kükürt";
        }
        if (lowerName.contains("bauxite") || lowerName.contains("boksit")) {
            return "Boksit";
        }
        if (lowerName.contains("rock_salt") || lowerName.contains("tuz_kayasi") || lowerName.contains("tuz_kayası")) {
            return "Tuz Kayası";
        }
        if (lowerName.contains("mithril")) {
            return "Mithril";
        }
        if (lowerName.contains("astral")) {
            return "Astral";
        }
        if (lowerName.contains("spyglass") || lowerName.contains("durbun") || lowerName.contains("dürbün")) {
            return "Casusluk Dürbünü";
        }
        if (lowerName.contains("life_elixir") || lowerName.contains("yasam_iksiri") || lowerName.contains("yaşam_iksiri")) {
            return "Yaşam İksiri";
        }
        if (lowerName.contains("power_fruit") || lowerName.contains("guc_meyvesi") || lowerName.contains("güç_meyvesi")) {
            return "Güç Meyvesi";
        }
        if (lowerName.contains("speed_elixir") || lowerName.contains("hiz_iksiri") || lowerName.contains("hız_iksiri")) {
            return "Hız İksiri";
        }
        if (lowerName.contains("regeneration_elixir") || lowerName.contains("yenilenme_iksiri")) {
            return "Yenilenme İksiri";
        }
        if (lowerName.contains("strength_elixir") || lowerName.contains("guc_iksiri") || lowerName.contains("güç_iksiri")) {
            return "Güç İksiri";
        }
        if (lowerName.contains("taming_core") || lowerName.contains("egitim_cekirdegi") || lowerName.contains("eğitim_çekirdeği")) {
            return "Eğitim Çekirdeği";
        }
        if (lowerName.contains("summon_core") || lowerName.contains("cagirma_cekirdegi") || lowerName.contains("çağırma_çekirdeği")) {
            return "Çağırma Çekirdeği";
        }
        if (lowerName.contains("breeding_core") || lowerName.contains("ureme_cekirdegi") || lowerName.contains("üreme_çekirdeği")) {
            return "Üreme Çekirdeği";
        }
        if (lowerName.contains("gender_scanner") || lowerName.contains("cinsiyet_ayirici") || lowerName.contains("cinsiyet_ayırıcı")) {
            return "Cinsiyet Ayırıcı";
        }
        return name;
    }

    private String getDisasterDisplayName(String name) {
        switch (name.toUpperCase()) {
            case "TITAN_GOLEM":
                return "Titan Golem";
            case "ABYSSAL_WORM":
                return "Hiçlik Solucanı";
            case "SOLAR_FLARE":
                return "Güneş Fırtınası";
            default:
                return name;
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
            List<String> commands = Arrays.asList("give", "spawn", "disaster", "list", "help", "siege", "caravan",
                    "contract", "build", "trap", "dungeon", "biome", "mine", "recipe", "ballista", "boss", "tame");
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
                    // Kategoriler
                    List<String> giveCategories = Arrays.asList("weapon", "armor", "material", "mobdrop", "special",
                            "ore", "tool", "bossitem", "recipebook");
                    if (input.isEmpty()) {
                        return giveCategories;
                    }
                    return giveCategories.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "spawn":
                    // Kategoriler
                    List<String> spawnCategories = Arrays.asList("level1", "level2", "level3", "level4", "level5",
                            "boss", "special");
                    if (input.isEmpty()) {
                        return spawnCategories;
                    }
                    return spawnCategories.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "disaster":
                    // Disaster alt komutları
                    List<String> disasterCommands = Arrays.asList("start", "stop", "info", "list", "clear");
                    if (input.isEmpty()) {
                        return disasterCommands;
                    }
                    return disasterCommands.stream()
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
                    List<String> siegeCommands = Arrays.asList("clear", "list", "start", "surrender");
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

                case "trap":
                    // Trap komutları
                    if (args.length == 2 && args[1].equalsIgnoreCase("give")) {
                        // Trap give için tuzak tipleri
                        List<String> trapTypes = Arrays.asList("hell_trap", "shock_trap", "black_hole", "mine",
                                "poison_trap");
                        if (input.isEmpty()) {
                            return trapTypes;
                        }
                        return trapTypes.stream()
                                .filter(s -> s.toLowerCase().startsWith(input))
                                .collect(Collectors.toList());
                    } else {
                        // Trap list komutları
                        List<String> trapCommands = Arrays.asList("list", "give", "build");
                        if (input.isEmpty()) {
                            return trapCommands;
                        }
                        return trapCommands.stream()
                                .filter(s -> s.toLowerCase().startsWith(input))
                                .collect(Collectors.toList());
                    }

                case "mine":
                    // Mine komutları
                    if (args.length == 2 && args[1].equalsIgnoreCase("give")) {
                        // Mine give için mayın tipleri (10 tür)
                        List<String> mineTypes = Arrays.asList("explosive", "lightning", "poison", "blindness",
                                "fatigue", "slowness", "fire", "freeze", "weakness", "confusion");
                        if (input.isEmpty()) {
                            return mineTypes;
                        }
                        return mineTypes.stream()
                                .filter(s -> s.toLowerCase().startsWith(input))
                                .collect(Collectors.toList());
                    } else {
                        // Mine list komutları
                        List<String> mineCommands = Arrays.asList("list", "give");
                        if (input.isEmpty()) {
                            return mineCommands;
                        }
                        return mineCommands.stream()
                                .filter(s -> s.toLowerCase().startsWith(input))
                                .collect(Collectors.toList());
                    }

                case "build":
                    // Kategoriler
                    List<String> buildCategories = Arrays.asList("weapon", "battery", "structure");
                    if (input.isEmpty()) {
                        return buildCategories;
                    }
                    return buildCategories.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "dungeon":
                    // Dungeon komutları
                    List<String> dungeonCommands = Arrays.asList("spawn", "list", "clear");
                    if (input.isEmpty()) {
                        return dungeonCommands;
                    }
                    return dungeonCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "biome":
                    // Biome komutları
                    List<String> biomeCommands = Arrays.asList("list", "set");
                    if (input.isEmpty()) {
                        return biomeCommands;
                    }
                    return biomeCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "boss":
                    // Boss komutları
                    List<String> bossCommands = Arrays.asList("spawn", "list", "ritual", "build");
                    if (input.isEmpty()) {
                        return bossCommands;
                    }
                    return bossCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "tame":
                    // Tame komutları
                    List<String> tameCommands = Arrays.asList("ritual", "list", "info", "build", "instant", "breed", "facility");
                    if (input.isEmpty()) {
                        return tameCommands;
                    }
                    return tameCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "recipe":
                    // Recipe komutları
                    List<String> recipeCommands = Arrays.asList("remove", "removeall", "list");
                    if (input.isEmpty()) {
                        return recipeCommands;
                    }
                    return recipeCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "ballista":
                    // Ballista komutları
                    List<String> ballistaCommands = Arrays.asList("spawn", "list", "clear");
                    if (input.isEmpty()) {
                        return ballistaCommands;
                    }
                    return ballistaCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());
            }
        }

        // Üçüncü argüman (kategorize edilmiş komutlar)
        if (args.length == 3) {
            String commandName = args[0].toLowerCase();
            String category = args[1].toLowerCase();
            String input = args[2].toLowerCase();

            switch (commandName) {
                case "give":
                    // Seviyeli kategoriler için seviye öner
                    if (category.equals("weapon") || category.equals("armor")) {
                        List<String> levels = Arrays.asList("1", "2", "3", "4", "5");
                        if (input.isEmpty()) {
                            return levels;
                        }
                        return levels.stream()
                                .filter(s -> s.startsWith(input))
                                .collect(Collectors.toList());
                    }
                    return getGiveTabComplete(category, input);
                case "spawn":
                    return getSpawnTabComplete(category, input);
                case "build":
                    return getBuildTabComplete(category, input);
                case "dungeon":
                    return getDungeonTabComplete(args, input);
                case "biome":
                    return getBiomeTabComplete(args, input);
                case "boss":
                    return getBossTabComplete(args, input);
                case "tame":
                    return getTameTabComplete(args, input);
                case "disaster":
                    // Disaster start komutu için felaket tipleri
                    if (category.equalsIgnoreCase("start")) {
                        List<String> disasterTypes = Arrays.asList("TITAN_GOLEM", "ABYSSAL_WORM", "SOLAR_FLARE");
                        if (input.isEmpty()) {
                            return disasterTypes;
                        }
                        return disasterTypes.stream()
                                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    break;
            }
        }
        
        // Dördüncü argüman (seviyeli itemler için tip öner)
        if (args.length == 4) {
            String commandName = args[0].toLowerCase();
            String category = args[1].toLowerCase();
            
            if (commandName.equals("give") && (category.equals("weapon") || category.equals("armor"))) {
                String levelStr = args[2];
                String input = args[3].toLowerCase();
                
                try {
                    int level = Integer.parseInt(levelStr);
                    if (level >= 1 && level <= 5) {
                        if (category.equals("weapon")) {
                            List<String> weaponTypes = Arrays.asList("sword", "axe", "spear", "bow", "hammer");
                            if (input.isEmpty()) {
                                return weaponTypes;
                            }
                            return weaponTypes.stream()
                                    .filter(s -> s.toLowerCase().startsWith(input))
                                    .collect(Collectors.toList());
                        } else if (category.equals("armor")) {
                            List<String> armorTypes = Arrays.asList("helmet", "chestplate", "leggings", "boots");
                            if (input.isEmpty()) {
                                return armorTypes;
                            }
                            return armorTypes.stream()
                                    .filter(s -> s.toLowerCase().startsWith(input))
                                    .collect(Collectors.toList());
                        }
                    }
                } catch (NumberFormatException e) {
                    // Geçersiz seviye
                }
            } else if (commandName.equals("disaster") && args[1].equalsIgnoreCase("start")) {
                // Disaster start için seviye öner
                String disasterType = args[2];
                String input = args[3].toLowerCase();
                
                if (disasterType.equalsIgnoreCase("TITAN_GOLEM") || 
                    disasterType.equalsIgnoreCase("ABYSSAL_WORM") || 
                    disasterType.equalsIgnoreCase("SOLAR_FLARE")) {
                    List<String> levels = Arrays.asList("1", "2", "3");
                    if (input.isEmpty()) {
                        return levels;
                    }
                    return levels.stream()
                            .filter(s -> s.startsWith(input))
                            .collect(Collectors.toList());
                }
            }
        }

        // Beşinci argüman (disaster koordinatları veya give miktar)
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("disaster") && args[1].equalsIgnoreCase("start")) {
                // Disaster start için konum öner
                String input = args[4].toLowerCase();
                if (input.isEmpty() || input.equals("b") || input.equals("m") || input.equals("s")) {
                    return Arrays.asList("ben", "me", "self");
                }
                return new ArrayList<>();
            } else if (args[0].equalsIgnoreCase("give")) {
                // Miktar için boş liste
                return new ArrayList<>();
            }
        }
        
        // Altıncı argüman (disaster koordinatları)
        if (args.length == 6 && args[0].equalsIgnoreCase("disaster") && args[1].equalsIgnoreCase("start")) {
            // Y koordinatı için boş liste
            return new ArrayList<>();
        }
        
        // Yedinci argüman (disaster koordinatları)
        if (args.length == 7 && args[0].equalsIgnoreCase("disaster") && args[1].equalsIgnoreCase("start")) {
            // Z koordinatı için boş liste
            return new ArrayList<>();
        }

        // Build komutu için seviye argümanı (kategorize edilmiş veya eski format)
        if (args.length >= 3 && args[0].equalsIgnoreCase("build")) {
            String category = args[1].toLowerCase();
            // Eğer kategori weapon, battery, structure ise 4. argüman seviye
            if (category.equals("weapon") || category.equals("battery") || category.equals("structure")) {
                if (args.length == 4) {
                    // Seviye için 1-5 arası öner
                    List<String> levels = Arrays.asList("1", "2", "3", "4", "5");
                    String input = args[3].toLowerCase();
                    if (input.isEmpty()) {
                        return levels;
                    }
                    return levels.stream()
                            .filter(s -> s.startsWith(input))
                            .collect(Collectors.toList());
                }
            } else {
                // Eski format: build <type> [level]
                if (args.length == 3) {
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
            }
        }

        // Trap give komutu için tuzak tipleri
        if (args.length == 3 && args[0].equalsIgnoreCase("trap") && args[1].equalsIgnoreCase("give")) {
            List<String> trapTypes = Arrays.asList("hell_trap", "shock_trap", "black_hole", "mine", "poison_trap");
            String input = args[2].toLowerCase();
            if (input.isEmpty()) {
                return trapTypes;
            }
            return trapTypes.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        // Siege start ve surrender için klan isimleri
        if (args.length == 3 && args[0].equalsIgnoreCase("siege")) {
            String subCommand = args[1].toLowerCase();
            if (subCommand.equals("start") || subCommand.equals("surrender")) {
                // Klan isimlerini al
                List<String> clanNames = new ArrayList<>();
                if (plugin.getTerritoryManager() != null && plugin.getTerritoryManager().getClanManager() != null) {
                    plugin.getTerritoryManager().getClanManager().getAllClans().forEach(clan -> {
                        clanNames.add(clan.getName());
                    });
                }
                return filterList(clanNames, args[2].toLowerCase());
            }
        }

        // Siege start için 4. argüman (savunan klan)
        if (args.length == 4 && args[0].equalsIgnoreCase("siege") && args[1].equalsIgnoreCase("start")) {
            // Klan isimlerini al
            List<String> clanNames = new ArrayList<>();
            if (plugin.getTerritoryManager() != null && plugin.getTerritoryManager().getClanManager() != null) {
                plugin.getTerritoryManager().getClanManager().getAllClans().forEach(clan -> {
                    clanNames.add(clan.getName());
                });
            }
            return filterList(clanNames, args[3].toLowerCase());
        }

        // Recipe remove için oyuncu isimleri
        if (args.length == 3 && args[0].equalsIgnoreCase("recipe") && args[1].equalsIgnoreCase("remove")) {
            // Online oyuncu isimlerini al
            List<String> playerNames = new ArrayList<>();
            org.bukkit.Bukkit.getOnlinePlayers().forEach(player -> {
                playerNames.add(player.getName());
            });
            return filterList(playerNames, args[2].toLowerCase());
        }


        // Diğer durumlar için boş liste
        return new ArrayList<>();
    }

    // ========== TAB COMPLETION HELPER METODLARI ==========

    private List<String> getGiveTabComplete(String category, String input) {
        switch (category.toLowerCase()) {
            case "weapon":
                List<String> weapons = new ArrayList<>();
                weapons.add("war_fan");
                weapons.add("tower_shield");
                // Özel silahlar
                for (int level = 1; level <= 5; level++) {
                    for (int variant = 1; variant <= 5; variant++) {
                        weapons.add("weapon_l" + level + "_" + variant);
                    }
                }
                return filterList(weapons, input);
            case "armor":
                List<String> armors = new ArrayList<>();
                // Özel zırhlar
                for (int level = 1; level <= 5; level++) {
                    for (int variant = 1; variant <= 5; variant++) {
                        armors.add("armor_l" + level + "_" + variant);
                    }
                }
                return filterList(armors, input);
            case "bossitem":
                List<String> bossItems = Arrays.asList(
                    "goblin_crown", "orc_amulet", "troll_heart",
                    "dragon_scale", "trex_tooth", "cyclops_eye",
                    "titan_core", "phoenix_feather", "kraken_tentacle",
                    "demon_lord_horn", "void_dragon_heart"
                );
                return filterList(bossItems, input);
            case "recipebook":
                List<String> recipeBooks = new ArrayList<>();
                // Silah tarifleri
                for (int level = 1; level <= 5; level++) {
                    for (int variant = 1; variant <= 5; variant++) {
                        recipeBooks.add("recipe_weapon_l" + level + "_" + variant);
                    }
                }
                // Zırh tarifleri
                for (int level = 1; level <= 5; level++) {
                    for (int variant = 1; variant <= 5; variant++) {
                        recipeBooks.add("recipe_armor_l" + level + "_" + variant);
                    }
                }
                // Yapı tarifleri
                recipeBooks.addAll(Arrays.asList(
                    "recipe_core", "recipe_alchemy_tower", "recipe_poison_reactor",
                    "recipe_siege_factory", "recipe_wall_generator", "recipe_gravity_well",
                    "recipe_lava_trencher", "recipe_watchtower", "recipe_drone_station",
                    "recipe_auto_turret", "recipe_global_market_gate", "recipe_auto_drill",
                    "recipe_xp_bank", "recipe_mag_rail", "recipe_teleporter",
                    "recipe_food_silo", "recipe_oil_refinery", "recipe_healing_beacon",
                    "recipe_weather_machine", "recipe_crop_accelerator", "recipe_mob_grinder",
                    "recipe_invisibility_cloak", "recipe_armory", "recipe_library",
                    "recipe_warning_sign", "recipe_tectonic"
                ));
                // Özel eşya tarifleri
                recipeBooks.addAll(Arrays.asList(
                    "recipe_blueprint_paper", "recipe_lightning_core", "recipe_titanium_ore",
                    "recipe_titanium_ingot", "recipe_dark_matter", "recipe_red_diamond",
                    "recipe_ruby", "recipe_adamantite", "recipe_star_core",
                    "recipe_flame_amplifier", "recipe_devil_horn", "recipe_devil_snake_eye",
                    "recipe_war_fan", "recipe_tower_shield", "recipe_hell_fruit",
                    "recipe_life_elixir", "recipe_power_fruit", "recipe_speed_elixir",
                    "recipe_regeneration_elixir", "recipe_strength_elixir",
                    "recipe_sulfur_ore", "recipe_sulfur", "recipe_bauxite_ore",
                    "recipe_bauxite_ingot", "recipe_rock_salt_ore", "recipe_rock_salt",
                    "recipe_mithril_ore", "recipe_mithril_ingot", "recipe_mithril_string",
                    "recipe_astral_ore", "recipe_astral_crystal", "recipe_rusty_hook",
                    "recipe_golden_hook", "recipe_titan_grapple", "recipe_trap_core",
                    "recipe_taming_core", "recipe_summon_core", "recipe_breeding_core",
                    "recipe_gender_scanner", "recipe_tectonic"
                ));
                return filterList(recipeBooks, input);
            case "material":
                List<String> materials = Arrays.asList("blueprint", "lightning_core", "dark_matter", "star_core",
                        "flame_amplifier", "devil_horn", "devil_snake_eye", "recipe_tectonic", "hell_fruit",
                        "clan_crystal", "clan_fence", "red_diamond", "ruby", "adamantite", "titanium_ore",
                        "titanium_ingot", "life_elixir", "power_fruit", "speed_elixir", "regeneration_elixir",
                        "strength_elixir");
                return filterList(materials, input);
            case "mobdrop":
                List<String> mobDrops = new ArrayList<>();
                // Seviye 1
                mobDrops.addAll(Arrays.asList("wild_boar_hide", "wild_boar_meat", "wolf_fang", "wolf_pelt",
                        "snake_venom", "snake_skin", "eagle_feather", "eagle_claw", "bear_claw", "bear_pelt"));
                // Seviye 2
                mobDrops.addAll(Arrays.asList("iron_core", "iron_dust", "ice_heart", "ice_crystal", "fire_core",
                        "fire_scale", "earth_stone", "earth_dust", "soul_fragment", "ghost_dust"));
                // Seviye 3
                mobDrops.addAll(Arrays.asList("shadow_heart", "shadow_scale", "light_heart", "light_feather",
                        "storm_core", "storm_dust", "lava_heart", "lava_scale", "ice_core", "ice_shard"));
                // Seviye 4
                mobDrops.addAll(Arrays.asList("devil_blood", "black_dragon_heart", "black_dragon_scale",
                        "death_sword_fragment", "death_dust", "chaos_core", "chaos_scale", "hell_stone", "hell_fire"));
                // Seviye 5
                mobDrops.addAll(Arrays.asList("legendary_dragon_heart", "legendary_dragon_scale", "god_blood",
                        "god_fragment", "void_core", "void_dust", "time_core", "time_scale", "fate_stone",
                        "fate_fragment"));
                return filterList(mobDrops, input);
            case "special":
                List<String> specials = Arrays.asList("red_diamond", "ruby", "adamantite", "titanium_ore",
                        "titanium_ingot");
                return filterList(specials, input);
            case "ore":
                List<String> ores = Arrays.asList("sulfur", "sulfur_ore", "bauxite", "bauxite_ore", "rock_salt",
                        "rock_salt_ore", "mithril", "mithril_ore", "mithril_string", "astral_crystal", "astral_ore");
                return filterList(ores, input);
            case "tool":
                List<String> tools = Arrays.asList("rusty_hook", "golden_hook", "titan_grapple", "trap_core",
                        "spyglass", "taming_core", "summon_core", "breeding_core", "gender_scanner");
                return filterList(tools, input);
            default:
                return new ArrayList<>();
        }
    }

    private List<String> getSpawnTabComplete(String category, String input) {
        switch (category.toLowerCase()) {
            case "level1":
                List<String> level1 = Arrays.asList("wild_boar", "wolf_pack", "snake", "eagle", "bear",
                        "goblin", "ork", "troll", "skeleton_knight", "dark_mage");
                return filterList(level1, input);
            case "level2":
                List<String> level2 = Arrays.asList("iron_golem", "ice_elemental", "fire_elemental", "earth_elemental",
                        "spirit_guardian", "werewolf", "giant_spider", "minotaur", "harpy", "basilisk");
                return filterList(level2, input);
            case "level3":
                List<String> level3 = Arrays.asList("shadow_beast", "light_spirit", "storm_giant", "lava_golem",
                        "frost_giant", "dragon", "trex", "cyclops", "griffin", "wraith", "lich", "kraken", "phoenix");
                return filterList(level3, input);
            case "level4":
                List<String> level4 = Arrays.asList("red_devil", "celestial_guardian", "thunder_wyvern", "magma_beast",
                        "abyssal_horror", "hell_dragon", "wyvern", "terror_worm", "war_bear", "shadow_panther", "hydra",
                        "behemoth");
                return filterList(level4, input);
            case "level5":
                List<String> level5 = Arrays.asList("ancient_dragon", "elder_kraken", "void_lord", "cosmic_horror",
                        "elemental_titan", "titan_golem", "void_worm");
                return filterList(level5, input);
            case "boss":
                List<String> bosses = Arrays.asList("titan_golem", "hydra", "void_worm", "legendary_dragon",
                        "god_slayer");
                return filterList(bosses, input);
            case "special":
                List<String> specials = Arrays.asList("supply_drop");
                return filterList(specials, input);
            default:
                return new ArrayList<>();
        }
    }

    private List<String> getBuildTabComplete(String category, String input) {
        switch (category.toLowerCase()) {
            case "weapon":
                List<String> weapons = Arrays.asList("catapult", "ballista", "trebuchet", "lava_fountain",
                        "poison_dispenser",
                        "force_field", "healing_shrine", "power_totem", "speed_circle", "defense_wall");
                return filterList(weapons, input);
            case "battery":
                // Yeni batarya sistemi - kategorize edilmiş
                List<String> batteries = new ArrayList<>();
                
                // Saldırı Bataryaları
                batteries.add("attack_fireball_l1");
                batteries.add("attack_lightning_l1");
                batteries.add("attack_ice_ball_l1");
                batteries.add("attack_poison_arrow_l1");
                batteries.add("attack_shock_l1");
                batteries.add("attack_double_fireball_l2");
                batteries.add("attack_chain_lightning_l2");
                batteries.add("attack_ice_storm_l2");
                batteries.add("attack_acid_rain_l2");
                batteries.add("attack_electric_net_l2");
                batteries.add("attack_meteor_shower_l3");
                batteries.add("attack_storm_l3");
                batteries.add("attack_ice_age_l3");
                batteries.add("attack_poison_bomb_l3");
                batteries.add("attack_lightning_storm_l3");
                batteries.add("attack_hellfire_l4");
                batteries.add("attack_thunder_l4");
                batteries.add("attack_death_cloud_l4");
                batteries.add("attack_electric_storm_l4");
                batteries.add("attack_mountain_destroyer_l5");
                batteries.add("attack_lava_tsunami_l5");
                batteries.add("attack_boss_killer_l5");
                batteries.add("attack_area_destroyer_l5");
                batteries.add("attack_apocalypse_l5");
                
                // Oluşturma Bataryaları
                batteries.add("construction_obsidian_wall_l1");
                batteries.add("construction_stone_bridge_l1");
                batteries.add("construction_iron_cage_l1");
                batteries.add("construction_glass_wall_l1");
                batteries.add("construction_wood_barricade_l1");
                batteries.add("construction_obsidian_cage_l2");
                batteries.add("construction_stone_bridge_l2");
                batteries.add("construction_iron_wall_l2");
                batteries.add("construction_glass_tunnel_l2");
                batteries.add("construction_wood_castle_l2");
                batteries.add("construction_obsidian_wall_l3");
                batteries.add("construction_netherite_bridge_l3");
                batteries.add("construction_iron_prison_l3");
                batteries.add("construction_glass_tower_l3");
                batteries.add("construction_stone_castle_l3");
                batteries.add("construction_obsidian_castle_l4");
                batteries.add("construction_netherite_bridge_l4");
                batteries.add("construction_iron_prison_l4");
                batteries.add("construction_glass_tower_l4");
                batteries.add("construction_stone_fortress_l4");
                batteries.add("construction_obsidian_prison_l5");
                batteries.add("construction_netherite_bridge_l5");
                batteries.add("construction_iron_castle_l5");
                batteries.add("construction_glass_tower_l5");
                batteries.add("construction_stone_fortress_l5");
                
                // Destek Bataryaları
                batteries.add("support_heal_l1");
                batteries.add("support_speed_l1");
                batteries.add("support_damage_l1");
                batteries.add("support_armor_l1");
                batteries.add("support_regeneration_l1");
                batteries.add("support_heal_l2");
                batteries.add("support_speed_l2");
                batteries.add("support_damage_l2");
                batteries.add("support_armor_l2");
                batteries.add("support_regeneration_l2");
                batteries.add("support_heal_l3");
                batteries.add("support_speed_l3");
                batteries.add("support_damage_l3");
                batteries.add("support_armor_l3");
                batteries.add("support_regeneration_l3");
                batteries.add("support_heal_l4");
                batteries.add("support_speed_l4");
                batteries.add("support_damage_l4");
                batteries.add("support_armor_l4");
                batteries.add("support_regeneration_l4");
                batteries.add("support_heal_l5");
                batteries.add("support_speed_l5");
                batteries.add("support_damage_l5");
                batteries.add("support_armor_l5");
                batteries.add("support_regeneration_l5");
                
                // Eski bataryalar (geriye dönük uyumluluk)
                batteries.add("magma_battery");
                batteries.add("lightning_battery");
                batteries.add("black_hole");
                batteries.add("bridge");
                batteries.add("shelter");
                batteries.add("gravity_anchor");
                batteries.add("seismic_hammer");
                batteries.add("magnetic_disruptor");
                batteries.add("ozone_shield");
                batteries.add("earth_wall");
                batteries.add("energy_wall");
                batteries.add("lava_trencher_battery");
                
                return filterList(batteries, input);
            case "structure":
                List<String> structures = Arrays.asList("alchemy_tower", "tectonic_stabilizer", "healing_beacon",
                        "global_market_gate", "auto_turret", "poison_reactor", "siege_factory", "wall_generator",
                        "gravity_well", "lava_trencher", "watchtower", "drone_station", "xp_bank", "mag_rail",
                        "teleporter", "food_silo", "oil_refinery", "weather_machine", "crop_accelerator",
                        "mob_grinder", "invisibility_cloak", "armory", "library", "warning_sign", "auto_drill", "core",
                        "fortress_wall");
                return filterList(structures, input);
            default:
                return new ArrayList<>();
        }
    }

    private List<String> filterList(List<String> list, String input) {
        if (input.isEmpty()) {
            return list;
        }
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }

    private List<String> getAllianceTabComplete(String[] args, String input) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 2) {
            // Komutlar
            String[] commands = {"list", "create", "break", "info"};
            for (String cmd : commands) {
                if (cmd.startsWith(input.toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 3) {
            String command = args[1].toLowerCase();
            if (command.equals("create")) {
                // Klan isimleri (ilk klan)
                if (plugin.getClanManager() != null) {
                    for (me.mami.stratocraft.model.Clan clan : plugin.getClanManager().getAllClans()) {
                        if (clan.getName().toLowerCase().startsWith(input.toLowerCase())) {
                            completions.add(clan.getName());
                        }
                    }
                }
            } else if (command.equals("break")) {
                // İttifak ID'leri
                if (plugin.getAllianceManager() != null) {
                    for (me.mami.stratocraft.model.Alliance alliance : plugin.getAllianceManager().getAllAlliances()) {
                        if (alliance.isActive()) {
                            String id = alliance.getId().toString();
                            if (id.startsWith(input.toLowerCase())) {
                                completions.add(id);
                            }
                        }
                    }
                }
            } else if (command.equals("info")) {
                // Klan isimleri
                if (plugin.getClanManager() != null) {
                    for (me.mami.stratocraft.model.Clan clan : plugin.getClanManager().getAllClans()) {
                        if (clan.getName().toLowerCase().startsWith(input.toLowerCase())) {
                            completions.add(clan.getName());
                        }
                    }
                }
            }
        } else if (args.length == 4) {
            String command = args[1].toLowerCase();
            if (command.equals("create")) {
                // Klan isimleri (ikinci klan)
                if (plugin.getClanManager() != null) {
                    for (me.mami.stratocraft.model.Clan clan : plugin.getClanManager().getAllClans()) {
                        if (clan.getName().toLowerCase().startsWith(input.toLowerCase())) {
                            completions.add(clan.getName());
                        }
                    }
                }
            }
        } else if (args.length == 5) {
            String command = args[1].toLowerCase();
            if (command.equals("create")) {
                // İttifak tipleri
                String[] types = {"defensive", "offensive", "trade", "full"};
                for (String type : types) {
                    if (type.startsWith(input.toLowerCase())) {
                        completions.add(type);
                    }
                }
            }
        }
        
        return completions;
    }
    
    private List<String> getDungeonTabComplete(String[] args, String input) {
        if (args.length == 2) {
            // Komutlar: spawn, list, clear
            return filterList(Arrays.asList("spawn", "list", "clear"), input);
        } else if (args.length == 3) {
            String subCommand = args[1].toLowerCase();
            if (subCommand.equals("spawn") || subCommand.equals("list")) {
                // Seviyeler: 1, 2, 3, 4, 5
                return filterList(Arrays.asList("1", "2", "3", "4", "5"), input);
            }
        } else if (args.length == 4 && args[1].equalsIgnoreCase("spawn")) {
            // Zindan tipleri
            int level = parseInt(args[2], 1);
            me.mami.stratocraft.manager.DungeonManager dungeonManager = plugin.getDungeonManager();
            if (dungeonManager != null) {
                return filterList(dungeonManager.getDungeonTypes(level), input);
            }
        }
        return new ArrayList<>();
    }

    private List<String> getBiomeTabComplete(String[] args, String input) {
        if (args.length == 2) {
            // Komutlar: list, set
            return filterList(Arrays.asList("list", "set"), input);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("list")) {
            // Seviyeler: 1, 2, 3, 4, 5
            return filterList(Arrays.asList("1", "2", "3", "4", "5"), input);
        }
        return new ArrayList<>();
    }

    private List<String> getBossTabComplete(String[] args, String input) {
        if (args.length == 2) {
            // Komutlar: spawn, list, ritual, build
            return filterList(Arrays.asList("spawn", "list", "ritual", "build"), input);
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("spawn") || args[1].equalsIgnoreCase("ritual") || args[1].equalsIgnoreCase("build"))) {
            // Boss tipleri
            List<String> bossTypes = Arrays.asList(
                    "GOBLIN_KING", "ORC_CHIEF", "TROLL_KING", "DRAGON", "TREX",
                    "CYCLOPS", "TITAN_GOLEM", "HELL_DRAGON", "HYDRA", "CHAOS_GOD",
                    "PHOENIX", "VOID_DRAGON", "CHAOS_TITAN");
            return filterList(bossTypes, input);
        }
        return new ArrayList<>();
    }

    private List<String> getTameTabComplete(String[] args, String input) {
        if (args.length == 2) {
            // Komutlar: ritual, list, info, build, instant, breed, facility
            return filterList(Arrays.asList("ritual", "list", "info", "build", "instant", "breed", "facility"), input);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("ritual")) {
            // Ritual alt komutları: level, boss
            return filterList(Arrays.asList("level", "boss"), input);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("build")) {
            // Build için seviyeler: 1, 2, 3, 4, 5
            return filterList(Arrays.asList("1", "2", "3", "4", "5"), input);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("facility")) {
            // Facility alt komutları: complete, create, build
            return filterList(Arrays.asList("complete", "create", "build"), input);
        } else if (args.length == 4 && args[1].equalsIgnoreCase("ritual")) {
            if (args[2].equalsIgnoreCase("level")) {
                // Seviyeler: 1, 2, 3, 4, 5
                return filterList(Arrays.asList("1", "2", "3", "4", "5"), input);
            } else if (args[2].equalsIgnoreCase("boss")) {
                // Boss tipleri
                List<String> bossTypes = Arrays.asList(
                        "GOBLIN_KING", "ORC_CHIEF", "TROLL_KING", "DRAGON", "TREX",
                        "CYCLOPS", "TITAN_GOLEM", "HELL_DRAGON", "HYDRA", "CHAOS_GOD");
                return filterList(bossTypes, input);
            }
        } else if (args.length == 4 && (args[1].equalsIgnoreCase("facility") && 
                (args[2].equalsIgnoreCase("create") || args[2].equalsIgnoreCase("build")))) {
            // Facility create/build için seviyeler: 1, 2, 3, 4, 5
            return filterList(Arrays.asList("1", "2", "3", "4", "5"), input);
        }
        return new ArrayList<>();
    }

    // ========== YENİ ADMIN KOMUTLARI ==========

    /**
     * Savaş yapıları yönetimi
     * /stratocraft siege clear - Tüm savaş yapılarını temizle
     * /stratocraft siege list - Aktif yapıları listele
     * /stratocraft siege start <saldıran_klan> <savunan_klan> - Savaş başlat
     * /stratocraft siege surrender <klan> - Klanı pes ettir
     */
    private boolean handleSiege(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft siege <clear|list|start|surrender>");
            return true;
        }

        me.mami.stratocraft.manager.SiegeWeaponManager siegeWeaponManager = plugin.getSiegeWeaponManager();
        me.mami.stratocraft.manager.SiegeManager siegeManager = plugin.getSiegeManager();
        me.mami.stratocraft.manager.TerritoryManager territoryManager = plugin.getTerritoryManager();

        switch (args[1].toLowerCase()) {
            case "clear":
                if (siegeWeaponManager == null) {
                    p.sendMessage("§cSiegeWeaponManager bulunamadı!");
                    return true;
                }
                int cleared = clearAllSiegeStructures(siegeWeaponManager);
                p.sendMessage("§a" + cleared + " savaş yapısı temizlendi.");
                return true;
            case "list":
                if (siegeWeaponManager == null) {
                    p.sendMessage("§cSiegeWeaponManager bulunamadı!");
                    return true;
                }
                showSiegeStructuresList(p, siegeWeaponManager);
                return true;
            case "start":
                if (args.length < 4) {
                    p.sendMessage("§cKullanım: /stratocraft siege start <saldıran_klan> <savunan_klan>");
                    return true;
                }
                if (siegeManager == null) {
                    p.sendMessage("§cSiegeManager bulunamadı!");
                    return true;
                }
                if (territoryManager == null) {
                    p.sendMessage("§cTerritoryManager bulunamadı!");
                    return true;
                }
                return handleSiegeStart(p, args[2], args[3], siegeManager, territoryManager);
            case "surrender":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft siege surrender <klan>");
                    return true;
                }
                if (siegeManager == null) {
                    p.sendMessage("§cSiegeManager bulunamadı!");
                    return true;
                }
                if (territoryManager == null) {
                    p.sendMessage("§cTerritoryManager bulunamadı!");
                    return true;
                }
                return handleSiegeSurrender(p, args[2], siegeManager, territoryManager);
            default:
                p.sendMessage("§cKullanım: /stratocraft siege <clear|list|start|surrender>");
                return true;
        }
    }

    /**
     * Admin komutu: Savaş başlat
     */
    private boolean handleSiegeStart(Player p, String attackerName, String defenderName, 
                                     me.mami.stratocraft.manager.SiegeManager siegeManager,
                                     me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        me.mami.stratocraft.manager.ClanManager clanManager = territoryManager.getClanManager();
        if (clanManager == null) {
            p.sendMessage("§cClanManager bulunamadı!");
            return true;
        }
        
        // Klanları bul
        me.mami.stratocraft.model.Clan attacker = null;
        me.mami.stratocraft.model.Clan defender = null;
        
        for (me.mami.stratocraft.model.Clan clan : clanManager.getAllClans()) {
            if (clan.getName().equalsIgnoreCase(attackerName)) {
                attacker = clan;
            }
            if (clan.getName().equalsIgnoreCase(defenderName)) {
                defender = clan;
            }
        }
        
        if (attacker == null) {
            p.sendMessage("§cSaldıran klan bulunamadı: " + attackerName);
            return true;
        }
        
        if (defender == null) {
            p.sendMessage("§cSavunan klan bulunamadı: " + defenderName);
            return true;
        }
        
        if (siegeManager.isUnderSiege(defender)) {
            p.sendMessage("§c" + defender.getName() + " klanı zaten savaşta!");
            return true;
        }
        
        siegeManager.startSiege(attacker, defender, p);
        new me.mami.stratocraft.task.SiegeTimer(defender, plugin)
            .runTaskTimer(plugin, 20L, 20L);
        
        p.sendMessage("§aSavaş başlatıldı: " + attacker.getName() + " → " + defender.getName());
        org.bukkit.Bukkit.broadcastMessage("§4§l[ADMIN] SAVAŞ BAŞLATILDI! §e" + attacker.getName() + " → " + defender.getName());
        return true;
    }

    /**
     * Admin komutu: Klanı pes ettir
     */
    private boolean handleSiegeSurrender(Player p, String clanName,
                                         me.mami.stratocraft.manager.SiegeManager siegeManager,
                                         me.mami.stratocraft.manager.TerritoryManager territoryManager) {
        me.mami.stratocraft.manager.ClanManager clanManager = territoryManager.getClanManager();
        if (clanManager == null) {
            p.sendMessage("§cClanManager bulunamadı!");
            return true;
        }
        
        me.mami.stratocraft.model.Clan clan = null;
        for (me.mami.stratocraft.model.Clan c : clanManager.getAllClans()) {
            if (c.getName().equalsIgnoreCase(clanName)) {
                clan = c;
                break;
            }
        }
        
        if (clan == null) {
            p.sendMessage("§cKlan bulunamadı: " + clanName);
            return true;
        }
        
        if (!siegeManager.isUnderSiege(clan)) {
            p.sendMessage("§c" + clan.getName() + " klanı savaşta değil!");
            return true;
        }
        
        siegeManager.surrender(clan, clanManager);
        p.sendMessage("§a" + clan.getName() + " klanı pes etti!");
        return true;
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
        for (java.util.Map.Entry<java.util.UUID, org.bukkit.entity.Entity> entry : manager.getActiveCaravans()
                .entrySet()) {
            org.bukkit.entity.Player owner = org.bukkit.Bukkit.getPlayer(entry.getKey());
            String ownerName = owner != null ? owner.getName() : "Offline";
            org.bukkit.entity.Entity caravan = entry.getValue();

            if (caravan != null && caravan.isValid()) {
                org.bukkit.Location loc = caravan.getLocation();
                p.sendMessage("§e" + index + ". §7Sahip: §a" + ownerName +
                        " §7- Konum: §7X:" + (int) loc.getX() + " Y:" + (int) loc.getY() + " Z:" + (int) loc.getZ());
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
                        " §7- Ödül: §6" + String.format("%.2f", contract.getReward()) + " Altın" +
                        " §7- Veren: §a" + issuerName);
            } else {
                // Normal kontrat
                p.sendMessage("§e" + index + ". §7" + contract.getMaterial() + " x" + contract.getAmount() +
                        " §7- Ödül: §6" + String.format("%.2f", contract.getReward()) + " Altın" +
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
     * /stratocraft build <type> [level] - Yapı oluşturur ve aktifleştirme
     * malzemelerini verir
     */
    private boolean handleBuild(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft build <kategori> <type> [level]");
            p.sendMessage("§7Kategoriler: weapon, battery, structure");
            p.sendMessage("§7Örnek: /stratocraft build weapon catapult");
            p.sendMessage("§7Örnek: /stratocraft build structure alchemy_tower 3");
            p.sendMessage("§7Batarya: /stratocraft build battery <seviye> <isim>");
            p.sendMessage("§7Eski format: /stratocraft build <type> [level] (hala çalışıyor)");
            return true;
        }

        String category = args[1].toLowerCase();
        String buildType;
        int level;

        // Kategori kontrolü
        if (args.length >= 3
                && (category.equals("weapon") || category.equals("battery") || category.equals("structure"))) {
            // Yeni format: build weapon catapult veya build battery <seviye> <isim>
            if (category.equals("battery") && args.length >= 4) {
                // Batarya formatı: build battery <seviye> <isim>
                level = parseInt(args[2], 1);
                buildType = args[3].toLowerCase();
                return buildBatteryByLevelAndName(p, level, buildType);
            }
            buildType = args[2].toLowerCase();
            level = args.length > 3 ? parseInt(args[3], 1) : 1;
        } else {
            // Eski format desteği (kategori yok, direkt type)
            buildType = args[1].toLowerCase();
            level = args.length > 2 ? parseInt(args[2], 1) : 1;
            category = "auto"; // Otomatik kategori belirleme
        }

        // Yapı kategorisine göre yönlendir
        if (category.equals("weapon")
                || (category.equals("auto") && (buildType.startsWith("catapult") || buildType.startsWith("ballista") ||
                        buildType.startsWith("lava_fountain") || buildType.startsWith("poison_dispenser") ||
                        buildType.startsWith("force_field") || buildType.startsWith("healing_shrine") ||
                        buildType.startsWith("power_totem") || buildType.startsWith("speed_circle") ||
                        buildType.startsWith("defense_wall")))) {
            return buildSiegeWeapon(p, buildType);
        } else if (category.equals("battery")
                || (category.equals("auto") && (buildType.startsWith("magma") || buildType.startsWith("lightning") ||
                        buildType.startsWith("black_hole") || buildType.startsWith("bridge") ||
                        buildType.startsWith("shelter") || buildType.startsWith("gravity") ||
                        buildType.startsWith("seismic") || buildType.startsWith("ozone") ||
                        buildType.startsWith("magnetic") || buildType.startsWith("earth_wall") ||
                        buildType.startsWith("energy_wall") || buildType.startsWith("lava_trencher_battery")))) {
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
        org.bukkit.Location loc = p.getLocation();

        // Yerinde blok varsa temizle
        me.mami.stratocraft.manager.StructureBuilder.clearArea(loc, 5, 5, 5);

        switch (type.toLowerCase()) {
            case "catapult":
            case "mancinik":
                // Mancınık: 3x3 Taş Tuğla Merdiven Tabanı Oluştur
                org.bukkit.block.BlockFace facing = p.getFacing();

                // 3x3 taban oluştur (merkez oyuncunun bulunduğu yer)
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        org.bukkit.Location stairLoc = loc.clone().add(x, 0, z);
                        stairLoc.getBlock().setType(Material.STONE_BRICK_STAIRS);

                        org.bukkit.block.data.type.Stairs stairs = (org.bukkit.block.data.type.Stairs) stairLoc
                                .getBlock()
                                .getBlockData();
                        stairs.setFacing(facing);
                        stairLoc.getBlock().setBlockData(stairs);
                    }
                }

                // Ortaya kontrol koltuğu (merdivenler) ekle
                org.bukkit.Location centerLoc = loc.clone().add(0, 1, 0);
                centerLoc.getBlock().setType(Material.STONE_BRICK_STAIRS);
                org.bukkit.block.data.type.Stairs centerStairs = (org.bukkit.block.data.type.Stairs) centerLoc
                        .getBlock()
                        .getBlockData();
                centerStairs.setFacing(facing);
                centerLoc.getBlock().setBlockData(centerStairs);

                // YAPILDI EFEKTLERİ
                org.bukkit.Location effectLoc = loc.clone().add(0, 1, 0);
                p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 3);
                p.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 20, 1, 1, 1);
                p.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                p.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);

                // Havai fişek efekti
                org.bukkit.entity.Firework firework = (org.bukkit.entity.Firework) p.getWorld().spawnEntity(
                        effectLoc, org.bukkit.entity.EntityType.FIREWORK);
                org.bukkit.inventory.meta.FireworkMeta fireworkMeta = firework.getFireworkMeta();
                fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
                        .with(org.bukkit.FireworkEffect.Type.BURST)
                        .withColor(org.bukkit.Color.RED, org.bukkit.Color.ORANGE)
                        .flicker(true).build());
                fireworkMeta.setPower(0);
                firework.setFireworkMeta(fireworkMeta);

                // Kullanım talimatları
                p.sendMessage("§a§l3x3 MANCINIK OLUŞTURULDU!");
                p.sendMessage("§7Kullanım:");
                p.sendMessage("§e  1. §7Boş El + Sağ Tık = Bin");
                p.sendMessage("§e  2. §7Sol Tık = Ateş Et!");
                p.sendMessage("§e  3. §7Shift + Sağ Tık = İn");
                p.sendMessage("§7Cooldown: 10 saniye");
                p.sendMessage("§7Yapı: 3x3 taş tuğla merdiven tabanı");
                return true;

            case "ballista":
            case "balista":
                // Balista: Dispenser oluştur
                loc.getBlock().setType(Material.DISPENSER);
                org.bukkit.block.data.Directional dispenser = (org.bukkit.block.data.Directional) loc.getBlock()
                        .getBlockData();
                dispenser.setFacing(p.getFacing());
                loc.getBlock().setBlockData(dispenser);

                p.sendMessage("§a§lBALİSTA OLUŞTURULDU!");
                p.sendMessage("§7Sağ tıkla ateş edebilirsin.");
                return true;

            case "trebuchet":
            case "trebuset":
            case "trebuşet":
                // Trebuchet: 5x5 Obsidian taban + Merkezde Anvil
                // 5x5 Obsidian taban oluştur
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        org.bukkit.Location obsidianLoc = loc.clone().add(x, 0, z);
                        obsidianLoc.getBlock().setType(Material.OBSIDIAN);
                    }
                }
                
                // Merkezde Anvil (1 blok yukarıda)
                org.bukkit.Location anvilLoc = loc.clone().add(0, 1, 0);
                anvilLoc.getBlock().setType(Material.ANVIL);
                
                // YAPILDI EFEKTLERİ
                org.bukkit.Location trebuchetEffectLoc = loc.clone().add(0, 1, 0);
                p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, trebuchetEffectLoc, 5);
                p.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, trebuchetEffectLoc, 30, 1, 1, 1);
                p.getWorld().playSound(trebuchetEffectLoc, org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                p.getWorld().playSound(trebuchetEffectLoc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
                
                // Havai fişek efekti
                org.bukkit.entity.Firework trebuchetFirework = (org.bukkit.entity.Firework) p.getWorld().spawnEntity(
                        trebuchetEffectLoc, org.bukkit.entity.EntityType.FIREWORK);
                org.bukkit.inventory.meta.FireworkMeta trebuchetFireworkMeta = trebuchetFirework.getFireworkMeta();
                trebuchetFireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
                        .with(org.bukkit.FireworkEffect.Type.BURST)
                        .withColor(org.bukkit.Color.PURPLE, org.bukkit.Color.BLACK)
                        .flicker(true).build());
                trebuchetFireworkMeta.setPower(0);
                trebuchetFirework.setFireworkMeta(trebuchetFireworkMeta);
                
                // Kullanım talimatları
                p.sendMessage("§a§l5x5 TREBUCHET OLUŞTURULDU!");
                p.sendMessage("§7Kullanım:");
                p.sendMessage("§e  1. §7Boş El + Sağ Tık = Bin");
                p.sendMessage("§e  2. §7Sol Tık = Ateş Et! (Uzun menzil)");
                p.sendMessage("§e  3. §7Shift + Sağ Tık = İn");
                p.sendMessage("§7Cooldown: 15 saniye");
                p.sendMessage("§7Yapı: 5x5 Obsidian taban + Merkez Anvil");
                return true;

            case "lava_fountain":
            case "lav_fiskiyesi":
                // Lav Fıskiyesi: Cauldron oluştur ve doldur
                loc.getBlock().setType(Material.CAULDRON);
                org.bukkit.block.data.Levelled cauldron = (org.bukkit.block.data.Levelled) loc.getBlock()
                        .getBlockData();
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
                org.bukkit.block.data.Directional dropper = (org.bukkit.block.data.Directional) loc.getBlock()
                        .getBlockData();
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
                            loc.clone().add(x, 1, z).getBlock().setType(Material.BEACON);
                        } else {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.GOLD_BLOCK);
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
                            loc.clone().add(x, 1, z).getBlock().setType(Material.ENCHANTING_TABLE);
                        } else {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.OBSIDIAN);
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
                            loc.clone().add(x, 1, z).getBlock().setType(Material.ENDER_CHEST);
                        } else {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.LAPIS_BLOCK);
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
                            loc.clone().add(x, 1, z).getBlock().setType(Material.ANVIL);
                        } else {
                            loc.clone().add(x, 0, z).getBlock().setType(Material.IRON_BLOCK);
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
        org.bukkit.Location loc = p.getLocation();

        // Yerinde blok varsa temizle
        me.mami.stratocraft.manager.StructureBuilder.clearArea(loc, 3, 3, 3);

        switch (type.toLowerCase()) {
            case "magma":
            case "magma_battery":
            case "ates_topu":
                // Magma Bataryası: 3 Magma Bloğu üst üste
                loc.getBlock().setType(Material.MAGMA_BLOCK);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.MAGMA_BLOCK);
                loc.clone().add(0, 2, 0).getBlock().setType(Material.MAGMA_BLOCK);

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
                loc.clone().add(0, 1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, 2, 0).getBlock().setType(Material.IRON_BLOCK);

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
                loc.clone().add(0, 1, 0).getBlock().setType(Material.OBSIDIAN);
                loc.clone().add(0, 2, 0).getBlock().setType(Material.OBSIDIAN);

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
                loc.clone().add(0, 1, 0).getBlock().setType(Material.PACKED_ICE);
                loc.clone().add(0, 2, 0).getBlock().setType(Material.PACKED_ICE);

                // Feather ver (BatteryListener'da FEATHER kullanılıyor)
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.FEATHER, 1));

                p.sendMessage("§a§lANLIK KÖPRÜ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;

            case "shelter":
            case "siginak":
                // Sığınak Küpü: 3 Cobblestone üst üste
                loc.getBlock().setType(Material.COBBLESTONE);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.COBBLESTONE);
                loc.clone().add(0, 2, 0).getBlock().setType(Material.COBBLESTONE);

                // Iron Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));

                p.sendMessage("§a§lSIĞINAK KÜPÜ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;

            case "gravity":
            case "gravity_anchor":
            case "yercekim_capasi":
                // Yerçekimi Çapası: Anvil + Slime Block altında
                loc.clone().add(0, 1, 0).getBlock().setType(Material.ANVIL);
                loc.getBlock().setType(Material.SLIME_BLOCK);

                // Iron Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));

                p.sendMessage("§a§lYERÇEKİMİ ÇAPASI BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;

            case "seismic":
            case "seismic_hammer":
            case "sismik_cekich":
                // Sismik Çekiç: Anvil + 2 Demir Bloğu altında
                loc.clone().add(0, 2, 0).getBlock().setType(Material.ANVIL);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.getBlock().setType(Material.IRON_BLOCK);

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
                loc.clone().add(0, 2, 0).getBlock().setType(Material.LAPIS_BLOCK);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.getBlock().setType(Material.IRON_BLOCK);

                // Iron Ingot ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 1));

                p.sendMessage("§a§lMANYETİK BOZUCU BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;

            case "ozone":
            case "ozone_shield":
            case "ozon_kalkani":
                // Ozon Kalkanı: Beacon + Cam altında
                loc.clone().add(0, 1, 0).getBlock().setType(Material.BEACON);
                loc.getBlock().setType(Material.GLASS);

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
                loc.clone().add(0, 2, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.clone().add(0, 1, 0).getBlock().setType(Material.IRON_BLOCK);
                loc.getBlock().setType(Material.IRON_BLOCK);

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
                loc.clone().add(0, 1, 0).getBlock().setType(Material.LAVA);
                loc.getBlock().setType(Material.LAVA);

                // Lava Bucket ver
                giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.LAVA_BUCKET, 1));

                p.sendMessage("§a§lLAV HENDEKÇİSİ BATARYASI OLUŞTURULDU!");
                p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
                return true;

            // ========== YENİ BATARYA SİSTEMİ (75 BATARYA) ==========
            
            // SALDIRI BATARYALARI (25 batarya)
            case "attack_fireball_l1":
                return buildNewBattery(p, loc, Material.MAGMA_BLOCK, null, 1, "Ateş Topu", BatteryManager.BatteryCategory.ATTACK);
            case "attack_lightning_l1":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, null, 1, "Yıldırım", BatteryManager.BatteryCategory.ATTACK);
            case "attack_ice_ball_l1":
                return buildNewBattery(p, loc, Material.PACKED_ICE, null, 1, "Buz Topu", BatteryManager.BatteryCategory.ATTACK);
            case "attack_poison_arrow_l1":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, null, 1, "Zehir Oku", BatteryManager.BatteryCategory.ATTACK);
            case "attack_shock_l1":
                return buildNewBattery(p, loc, Material.REDSTONE_BLOCK, null, 1, "Şok", BatteryManager.BatteryCategory.ATTACK);
            
            case "attack_double_fireball_l2":
                return buildNewBattery(p, loc, Material.MAGMA_BLOCK, Material.NETHERRACK, 2, "Çift Ateş Topu", BatteryManager.BatteryCategory.ATTACK);
            case "attack_chain_lightning_l2":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.GOLD_BLOCK, 2, "Zincir Yıldırım", BatteryManager.BatteryCategory.ATTACK);
            case "attack_ice_storm_l2":
                return buildNewBattery(p, loc, Material.PACKED_ICE, Material.BLUE_ICE, 2, "Buz Fırtınası", BatteryManager.BatteryCategory.ATTACK);
            case "attack_acid_rain_l2":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, Material.SLIME_BLOCK, 2, "Asit Yağmuru", BatteryManager.BatteryCategory.ATTACK);
            case "attack_electric_net_l2":
                return buildNewBattery(p, loc, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, 2, "Elektrik Ağı", BatteryManager.BatteryCategory.ATTACK);
            
            case "attack_meteor_shower_l3":
                return buildNewBattery(p, loc, Material.OBSIDIAN, Material.MAGMA_BLOCK, 3, "Meteor Yağmuru", BatteryManager.BatteryCategory.ATTACK);
            case "attack_storm_l3":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.DIAMOND_BLOCK, 3, "Fırtına", BatteryManager.BatteryCategory.ATTACK);
            case "attack_ice_age_l3":
                return buildNewBattery(p, loc, Material.PACKED_ICE, Material.BLUE_ICE, 3, "Buz Çağı", BatteryManager.BatteryCategory.ATTACK);
            case "attack_poison_bomb_l3":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, Material.POISONOUS_POTATO, 3, "Zehir Bombası", BatteryManager.BatteryCategory.ATTACK);
            case "attack_lightning_storm_l3":
                return buildNewBattery(p, loc, Material.REDSTONE_BLOCK, Material.GLOWSTONE, 3, "Yıldırım Fırtınası", BatteryManager.BatteryCategory.ATTACK);
            
            case "attack_hellfire_l4":
                return buildNewBattery(p, loc, Material.MAGMA_BLOCK, Material.NETHER_STAR, 4, "Cehennem Ateşi", BatteryManager.BatteryCategory.ATTACK);
            case "attack_thunder_l4":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.BEACON, 4, "Gök Gürültüsü", BatteryManager.BatteryCategory.ATTACK);
            case "attack_ice_age_l4":
                return buildNewBattery(p, loc, Material.PACKED_ICE, Material.FROSTED_ICE, 4, "Buz Çağı", BatteryManager.BatteryCategory.ATTACK);
            case "attack_death_cloud_l4":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, Material.WITHER_SKELETON_SKULL, 4, "Ölüm Bulutu", BatteryManager.BatteryCategory.ATTACK);
            case "attack_electric_storm_l4":
                return buildNewBattery(p, loc, Material.REDSTONE_BLOCK, Material.END_CRYSTAL, 4, "Elektrik Fırtınası", BatteryManager.BatteryCategory.ATTACK);
            
            case "attack_mountain_destroyer_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.NETHER_STAR, 5, "Dağ Yok Edici", BatteryManager.BatteryCategory.ATTACK);
            case "attack_lava_tsunami_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.LAVA_BUCKET, 5, "Lava Tufanı", BatteryManager.BatteryCategory.ATTACK);
            case "attack_boss_killer_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.DRAGON_HEAD, 5, "Boss Katili", BatteryManager.BatteryCategory.ATTACK);
            case "attack_area_destroyer_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.COMMAND_BLOCK, 5, "Alan Yok Edici", BatteryManager.BatteryCategory.ATTACK);
            case "attack_apocalypse_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.END_CRYSTAL, 5, "Kıyamet", BatteryManager.BatteryCategory.ATTACK);
            
            // OLUŞTURMA BATARYALARI (25 batarya)
            case "construction_obsidian_wall_l1":
                return buildNewBattery(p, loc, Material.OBSIDIAN, null, 1, "Obsidyen Duvar", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_stone_bridge_l1":
                return buildNewBattery(p, loc, Material.STONE, null, 1, "Taş Köprü", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_iron_cage_l1":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, null, 1, "Demir Kafes", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_glass_wall_l1":
                return buildNewBattery(p, loc, Material.GLASS, null, 1, "Cam Duvar", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_wood_barricade_l1":
                return buildNewBattery(p, loc, Material.OAK_PLANKS, null, 1, "Ahşap Barikat", BatteryManager.BatteryCategory.CONSTRUCTION);
            
            case "construction_obsidian_cage_l2":
                return buildNewBattery(p, loc, Material.OBSIDIAN, Material.IRON_BLOCK, 2, "Obsidyen Kafes", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_stone_bridge_l2":
                return buildNewBattery(p, loc, Material.STONE, Material.COBBLESTONE, 2, "Taş Köprü (Gelişmiş)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_iron_wall_l2":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.IRON_INGOT, 2, "Demir Duvar", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_glass_tunnel_l2":
                return buildNewBattery(p, loc, Material.GLASS, Material.GLASS_PANE, 2, "Cam Tünel", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_wood_castle_l2":
                return buildNewBattery(p, loc, Material.OAK_PLANKS, Material.OAK_LOG, 2, "Ahşap Kale", BatteryManager.BatteryCategory.CONSTRUCTION);
            
            case "construction_obsidian_wall_l3":
                return buildNewBattery(p, loc, Material.OBSIDIAN, Material.BEDROCK, 3, "Obsidyen Duvar (Güçlü)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_netherite_bridge_l3":
                return buildNewBattery(p, loc, Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT, 3, "Netherite Köprü", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_iron_prison_l3":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.IRON_BARS, 3, "Demir Hapishane", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_glass_tower_l3":
                return buildNewBattery(p, loc, Material.GLASS, Material.GLASS_PANE, 3, "Cam Kule", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_stone_castle_l3":
                return buildNewBattery(p, loc, Material.STONE, Material.COBBLESTONE, 3, "Taş Kale", BatteryManager.BatteryCategory.CONSTRUCTION);
            
            case "construction_obsidian_castle_l4":
                return buildNewBattery(p, loc, Material.OBSIDIAN, Material.END_CRYSTAL, 4, "Obsidyen Kale", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_netherite_bridge_l4":
                return buildNewBattery(p, loc, Material.NETHERITE_BLOCK, Material.BEACON, 4, "Netherite Köprü (Gelişmiş)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_iron_prison_l4":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.ANVIL, 4, "Demir Hapishane (Güçlü)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_glass_tower_l4":
                return buildNewBattery(p, loc, Material.GLASS, Material.BEACON, 4, "Cam Kule (Gelişmiş)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_stone_fortress_l4":
                return buildNewBattery(p, loc, Material.STONE, Material.BEACON, 4, "Taş Şato", BatteryManager.BatteryCategory.CONSTRUCTION);
            
            case "construction_obsidian_prison_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.END_CRYSTAL, 5, "Obsidyen Hapishane", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_netherite_bridge_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.BEACON, 5, "Netherite Köprü (Efsanevi)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_iron_castle_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.ANVIL, 5, "Demir Kale (Efsanevi)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_glass_tower_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.BEACON, 5, "Cam Kule (Efsanevi)", BatteryManager.BatteryCategory.CONSTRUCTION);
            case "construction_stone_fortress_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.BEACON, 5, "Taş Kalesi (Efsanevi)", BatteryManager.BatteryCategory.CONSTRUCTION);
            
            // DESTEK BATARYALARI (25 batarya)
            case "support_heal_l1":
                return buildNewBattery(p, loc, Material.GOLD_BLOCK, null, 1, "Can Yenileme", BatteryManager.BatteryCategory.SUPPORT);
            case "support_speed_l1":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, null, 1, "Hız Artışı", BatteryManager.BatteryCategory.SUPPORT);
            case "support_damage_l1":
                return buildNewBattery(p, loc, Material.DIAMOND_BLOCK, null, 1, "Hasar Artışı", BatteryManager.BatteryCategory.SUPPORT);
            case "support_armor_l1":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, null, 1, "Zırh Artışı", BatteryManager.BatteryCategory.SUPPORT);
            case "support_regeneration_l1":
                return buildNewBattery(p, loc, Material.LAPIS_BLOCK, null, 1, "Yenilenme", BatteryManager.BatteryCategory.SUPPORT);
            
            case "support_heal_l2":
                return buildNewBattery(p, loc, Material.GOLD_BLOCK, Material.GOLD_INGOT, 2, "Can Yenileme (Gelişmiş)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_speed_l2":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, Material.EMERALD, 2, "Hız Artışı (Gelişmiş)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_damage_l2":
                return buildNewBattery(p, loc, Material.DIAMOND_BLOCK, Material.DIAMOND, 2, "Hasar Artışı (Gelişmiş)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_armor_l2":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.IRON_INGOT, 2, "Zırh Artışı (Gelişmiş)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_regeneration_l2":
                return buildNewBattery(p, loc, Material.LAPIS_BLOCK, Material.LAPIS_LAZULI, 2, "Yenilenme (Gelişmiş)", BatteryManager.BatteryCategory.SUPPORT);
            
            case "support_heal_l3":
                return buildNewBattery(p, loc, Material.GOLD_BLOCK, Material.GOLDEN_APPLE, 3, "Can Yenileme (Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_speed_l3":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, Material.EMERALD_BLOCK, 3, "Hız Artışı (Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_damage_l3":
                return buildNewBattery(p, loc, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK, 3, "Hasar Artışı (Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_armor_l3":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.IRON_BLOCK, 3, "Zırh Artışı (Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_regeneration_l3":
                return buildNewBattery(p, loc, Material.LAPIS_BLOCK, Material.LAPIS_BLOCK, 3, "Yenilenme (Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            
            case "support_heal_l4":
                return buildNewBattery(p, loc, Material.GOLD_BLOCK, Material.ENCHANTED_GOLDEN_APPLE, 4, "Can Yenileme (Çok Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_speed_l4":
                return buildNewBattery(p, loc, Material.EMERALD_BLOCK, Material.BEACON, 4, "Hız Artışı (Çok Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_damage_l4":
                return buildNewBattery(p, loc, Material.DIAMOND_BLOCK, Material.BEACON, 4, "Hasar Artışı (Çok Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_armor_l4":
                return buildNewBattery(p, loc, Material.IRON_BLOCK, Material.BEACON, 4, "Zırh Artışı (Çok Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_regeneration_l4":
                return buildNewBattery(p, loc, Material.LAPIS_BLOCK, Material.BEACON, 4, "Yenilenme (Çok Güçlü)", BatteryManager.BatteryCategory.SUPPORT);
            
            case "support_heal_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.NETHER_STAR, 5, "Can Yenileme (Efsanevi)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_speed_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.NETHER_STAR, 5, "Hız Artışı (Efsanevi)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_damage_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.NETHER_STAR, 5, "Hasar Artışı (Efsanevi)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_armor_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.NETHER_STAR, 5, "Zırh Artışı (Efsanevi)", BatteryManager.BatteryCategory.SUPPORT);
            case "support_regeneration_l5":
                return buildNewBattery(p, loc, Material.BEDROCK, Material.NETHER_STAR, 5, "Yenilenme (Efsanevi)", BatteryManager.BatteryCategory.SUPPORT);
            
            default:
                p.sendMessage("§cBilinmeyen batarya tipi: " + type);
                return true;
        }
    }
    
    /**
     * Yeni batarya sistemi için build metodu
     */
    private boolean buildNewBattery(Player p, org.bukkit.Location loc, Material baseBlock, Material sideBlock, int level, String displayName, BatteryManager.BatteryCategory category) {
        // Yerinde blok varsa temizle
        me.mami.stratocraft.manager.StructureBuilder.clearArea(loc, 5, 5, 5);
        
        // Seviyeye göre blok sayısı
        int blockCount = 3 + (level - 1) * 2; // Seviye 1: 3, Seviye 2: 5, Seviye 3: 7, Seviye 4: 9, Seviye 5: 11
        
        // Üst üste bloklar
        for (int i = 0; i < blockCount; i++) {
            loc.clone().add(0, i, 0).getBlock().setType(baseBlock);
        }
        
        // Yan blok (seviye 2+ için)
        if (sideBlock != null && level >= 2) {
            loc.clone().add(1, blockCount / 2, 0).getBlock().setType(sideBlock);
        }
        
        // Seviye 5 için özel bloklar
        if (level == 5) {
            loc.clone().add(0, -1, 0).getBlock().setType(Material.BEACON);
            if (sideBlock != null) {
                loc.clone().add(0, blockCount, 0).getBlock().setType(sideBlock);
            }
        }
        
        // Yakıt ver
        giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.DIAMOND, 5));
        giveItemSafely(p, new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 5));
        if (ItemManager.RED_DIAMOND != null) {
            giveItemSafely(p, ItemManager.RED_DIAMOND.clone());
        }
        if (ItemManager.DARK_MATTER != null) {
            giveItemSafely(p, ItemManager.DARK_MATTER.clone());
        }
        
        String categoryName = category == BatteryManager.BatteryCategory.ATTACK ? "§cSaldırı" :
                             category == BatteryManager.BatteryCategory.CONSTRUCTION ? "§aOluşturma" :
                             "§eDestek";
        
        // Aktifleştirme malzemeleri mesajı
        String fuelMaterials = "§7Aktifleştirme Malzemeleri: §eElmas, Demir Çubuğu";
        if (level >= 3) {
            fuelMaterials += ", §cKızıl Elmas";
        }
        if (level >= 4) {
            fuelMaterials += ", §5Karanlık Madde";
        }
        
        p.sendMessage("§a§l" + displayName + " BATARYASI OLUŞTURULDU!");
        p.sendMessage("§7Kategori: " + categoryName);
        p.sendMessage("§7Seviye: §e" + level);
        p.sendMessage(fuelMaterials);
        p.sendMessage("§7Shift + Sağ Tık ile yükle, Sol Tık ile ateşle.");
        return true;
    }
    
    /**
     * Yeni batarya komut formatı: /stratocraft build battery <seviye> <isim>
     */
    private boolean buildBatteryByLevelAndName(Player p, int level, String name) {
        if (level < 1 || level > 5) {
            p.sendMessage("§cSeviye 1-5 arası olmalı!");
            return true;
        }
        
        // İsmi normalize et (tire ve alt çizgiyi kaldır, küçük harfe çevir)
        String normalizedName = name.toLowerCase().replace("_", "").replace("-", "");
        
        // BatteryType enum'undan eşleşen bataryayı bul
        BatteryManager.BatteryType foundBattery = null;
        for (BatteryManager.BatteryType batteryType : BatteryManager.BatteryType.values()) {
            if (batteryType.getLevel() == level) {
                String batteryName = batteryType.getDisplayName().toLowerCase()
                    .replace(" ", "").replace("(", "").replace(")", "")
                    .replace("ç", "c").replace("ğ", "g").replace("ı", "i")
                    .replace("ö", "o").replace("ş", "s").replace("ü", "u");
                
                if (batteryName.contains(normalizedName) || normalizedName.contains(batteryName)) {
                    foundBattery = batteryType;
                    break;
                }
            }
        }
        
        if (foundBattery == null) {
            p.sendMessage("§cSeviye " + level + " için '" + name + "' bataryası bulunamadı!");
            p.sendMessage("§7Mevcut bataryaları görmek için: /stratocraft build battery " + level + " <isim>");
            return true;
        }
        
        org.bukkit.Location loc = p.getLocation();
        return buildNewBattery(p, loc, foundBattery.getBaseBlock(), foundBattery.getSideBlock(), 
                              foundBattery.getLevel(), foundBattery.getDisplayName(), foundBattery.getCategory());
    }

    /**
     * Klan yapıları oluşturma (şema ile)
     */
    private boolean buildClanStructure(Player p, String type, int level) {
        org.bukkit.Location loc = p.getLocation();

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
            case "alchemy_tower":
                return "Simya Kulesi";
            case "tectonic_stabilizer":
                return "Tektonik Sabitleyici";
            case "healing_beacon":
                return "Şifa Kulesi";
            case "global_market_gate":
                return "Global Pazar Kapısı";
            case "auto_turret":
                return "Otomatik Taret";
            case "poison_reactor":
                return "Zehir Reaktörü";
            case "siege_factory":
                return "Kuşatma Fabrikası";
            case "wall_generator":
                return "Sur Jeneratörü";
            case "gravity_well":
                return "Yerçekimi Kuyusu";
            case "lava_trencher":
                return "Lav Hendekçisi";
            case "watchtower":
                return "Gözetleme Kulesi";
            case "drone_station":
                return "Drone İstasyonu";
            case "xp_bank":
                return "Tecrübe Bankası";
            case "mag_rail":
                return "Manyetik Ray";
            case "teleporter":
                return "Işınlanma Platformu";
            case "food_silo":
                return "Buzdolabı";
            case "oil_refinery":
                return "Petrol Rafinerisi";
            case "weather_machine":
                return "Hava Kontrolcüsü";
            case "crop_accelerator":
                return "Tarım Hızlandırıcı";
            case "mob_grinder":
                return "Mob Öğütücü";
            case "invisibility_cloak":
                return "Görünmezlik Perdesi";
            case "armory":
                return "Cephanelik";
            case "library":
                return "Kütüphane";
            case "warning_sign":
                return "Yasaklı Bölge Tabelası";
            case "auto_drill":
                return "Otomatik Madenci";
            case "core":
                return "Ana Kristal";
            default:
                return type;
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
    // ========== TUZAK YÖNETİMİ ==========

    private boolean handleTrap(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft trap <list|give|build>");
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                showTrapsList(p);
                return true;
            case "give":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft trap give <type>");
                    p.sendMessage("§7Tuzak tipleri: hell_trap, shock_trap, black_hole, mine, poison_trap");
                    return true;
                }
                String trapType = args[2].toLowerCase();
                giveTrapItems(p, trapType);
                return true;
            case "build":
                // Tuzak yapısını otomatik oluştur
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cBu komut sadece adminler için!");
                    return true;
                }
                buildTrapStructure(p);
                return true;
            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft trap <list|give|build>");
                return true;
        }
    }

    private void showTrapsList(Player p) {
        p.sendMessage("§6§l=== TUZAK SİSTEMİ ===");
        p.sendMessage("§7Tuzak kurulumu için:");
        p.sendMessage("§e1. §7/stratocraft give trap_core §7- Tuzak çekirdeği al");
        p.sendMessage("§e2. §7Yere bakıp sağ tıkla (LODESTONE oluştur)");
        p.sendMessage("§e3. §7Etrafına Magma Block çerçevesi yap (3x3, 3x6, 5x5, vb.)");
        p.sendMessage("§e4. §7Üstünü blokla kapat");
        p.sendMessage("§e5. §7LODESTONE'a yakıt (Elmas/Zümrüt/Titanyum) + tuzak tipi item ile sağ tık");
        p.sendMessage("");
        p.sendMessage("§7--- Tuzak Tipleri ---");
        p.sendMessage("§e- hell_trap §7- Cehennem Tuzağı (Magma Cream)");
        p.sendMessage("§e- shock_trap §7- Şok Tuzağı (Lightning Core)");
        p.sendMessage("§e- black_hole §7- Kara Delik (Ender Pearl)");
        p.sendMessage("§e- mine §7- Mayın (TNT)");
        p.sendMessage("§e- poison_trap §7- Zehir Tuzağı (Spider Eye)");
        p.sendMessage("");
        p.sendMessage("§7Yakıt: Elmas (5 kullanım), Zümrüt (10 kullanım), Titanyum (20 kullanım)");
    }

    private void giveTrapItems(Player p, String trapType) {
        // Tuzak çekirdeği
        if (ItemManager.TRAP_CORE != null) {
            giveItemSafely(p, ItemManager.TRAP_CORE.clone());
        }

        // Yakıt (Elmas, Zümrüt, Titanyum)
        giveItemSafely(p, new ItemStack(Material.DIAMOND, 5));
        giveItemSafely(p, new ItemStack(Material.EMERALD, 5));
        if (ItemManager.TITANIUM_INGOT != null) {
            giveItemSafely(p, ItemManager.TITANIUM_INGOT.clone());
        }

        // Tuzak tipi itemi
        switch (trapType) {
            case "hell_trap":
                giveItemSafely(p, new ItemStack(Material.MAGMA_CREAM, 1));
                p.sendMessage("§aCehennem Tuzağı itemleri verildi!");
                break;
            case "shock_trap":
                if (ItemManager.LIGHTNING_CORE != null) {
                    giveItemSafely(p, ItemManager.LIGHTNING_CORE.clone());
                }
                p.sendMessage("§aŞok Tuzağı itemleri verildi!");
                break;
            case "black_hole":
                giveItemSafely(p, new ItemStack(Material.ENDER_PEARL, 1));
                p.sendMessage("§aKara Delik Tuzağı itemleri verildi!");
                break;
            case "mine":
                giveItemSafely(p, new ItemStack(Material.TNT, 1));
                p.sendMessage("§aMayın Tuzağı itemleri verildi!");
                break;
            case "poison_trap":
                giveItemSafely(p, new ItemStack(Material.SPIDER_EYE, 1));
                p.sendMessage("§aZehir Tuzağı itemleri verildi!");
                break;
            default:
                p.sendMessage("§cGeçersiz tuzak tipi! hell_trap, shock_trap, black_hole, mine, poison_trap");
                return;
        }
    }
    
    /**
     * Tuzak yapısını otomatik oluştur (admin komutu)
     */
    private void buildTrapStructure(Player p) {
        // Oyuncunun baktığı bloğu al
        org.bukkit.block.Block targetBlock = p.getTargetBlockExact(10);
        if (targetBlock == null) {
            targetBlock = p.getLocation().getBlock();
        }
        
        // Tuzak çekirdeği konumu (oyuncunun baktığı bloğun üstü)
        org.bukkit.block.Block coreBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.UP);
        
        // Çekirdeği LODESTONE olarak yerleştir
        coreBlock.setType(Material.LODESTONE);
        coreBlock.setMetadata("TrapCoreItem", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        
        // 3x3 Magma Block çerçevesi oluştur (çekirdeğin etrafında, aynı seviyede)
        int frameBlocksPlaced = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue; // Merkez çekirdek, atla
                }
                org.bukkit.block.Block frameBlock = coreBlock.getRelative(x, 0, z);
                frameBlock.setType(Material.MAGMA_BLOCK);
                frameBlocksPlaced++;
            }
        }
        
        p.sendMessage("§a§lTuzak yapısı oluşturuldu!");
        p.sendMessage("§7Tuzak çekirdeği: §eLODESTONE");
        p.sendMessage("§7Çerçeve blokları: §e" + frameBlocksPlaced + " Magma Block");
        p.sendMessage("§7Sonraki adımlar:");
        p.sendMessage("§e1. §7Üstünü blokla kapat");
        p.sendMessage("§e2. §7Yakıt + tuzak tipi item ile aktifleştir");
        
        // Efekt
        org.bukkit.Location effectLoc = coreBlock.getLocation().add(0.5, 0.5, 0.5);
        effectLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, effectLoc, 20, 0.5, 0.5, 0.5, 0.1);
        effectLoc.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
    }

    // ========== MAYIN YÖNETİMİ ==========

    private boolean handleMine(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft mine <list|give>");
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                showMinesList(p);
                return true;
            case "give":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft mine give <type>");
                    p.sendMessage("§7Mayın tipleri: explosive, lightning, poison, blindness, fatigue, slowness, fire, freeze, weakness, confusion");
                    return true;
                }
                String mineType = args[2].toLowerCase();
                giveMineItems(p, mineType);
                return true;
            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft mine <list|give>");
                return true;
        }
    }

    private void showMinesList(Player p) {
        p.sendMessage("§6§l=== MAYIN SİSTEMİ ===");
        p.sendMessage("§7Mayın kurulumu için:");
        p.sendMessage("§e1. §7Basınç plakası yerleştir (Stone, Oak, Light/Heavy Weighted)");
        p.sendMessage("§e2. §7Basınç plakasına mayın tipi item ile sağ tık");
        p.sendMessage("§e3. §7Mayın hazır! Basınca tetiklenir ve yok olur");
        p.sendMessage("");
        p.sendMessage("§7--- Mayın Tipleri (10 Tür) ---");
        p.sendMessage("§e- explosive §7- Patlama Mayını (TNT) - Alan hasarı");
        p.sendMessage("§e- lightning §7- Yıldırım Mayını (Lightning Core) - Tek hedef");
        p.sendMessage("§e- poison §7- Zehir Mayını (Spider Eye) - Tek hedef");
        p.sendMessage("§e- blindness §7- Körlük Mayını (Ink Sac) - Tek hedef");
        p.sendMessage("§e- fatigue §7- Yorgunluk Mayını (Iron Pickaxe) - Tek hedef");
        p.sendMessage("§e- slowness §7- Yavaşlık Mayını (Slime Ball) - Tek hedef");
        p.sendMessage("§e- fire §7- Ateş Mayını (Blaze Rod) - Yanma efekti");
        p.sendMessage("§e- freeze §7- Dondurma Mayını (Ice) - Buz efekti");
        p.sendMessage("§e- weakness §7- Zayıflık Mayını (Bone) - Zayıflık efekti");
        p.sendMessage("§e- confusion §7- Karışıklık Mayını (Fermented Spider Eye) - Nausea efekti");
        p.sendMessage("");
        p.sendMessage("§7Özellikler:");
        p.sendMessage("§7- Görünür (basınç plakası)");
        p.sendMessage("§7- Basınca yok olur");
        p.sendMessage("§7- Kolay yapılır (sadece basınç plakası + item)");
        p.sendMessage("§7- Sahip kırabilir");
    }

    private void giveMineItems(Player p, String mineType) {
        // Basınç plakası
        giveItemSafely(p, new ItemStack(Material.STONE_PRESSURE_PLATE, 5));

        // Mayın tipi itemi
        switch (mineType) {
            case "explosive":
                giveItemSafely(p, new ItemStack(Material.TNT, 5));
                p.sendMessage("§aPatlama Mayını itemleri verildi!");
                break;
            case "lightning":
                if (ItemManager.LIGHTNING_CORE != null) {
                    giveItemSafely(p, ItemManager.LIGHTNING_CORE.clone());
                    giveItemSafely(p, ItemManager.LIGHTNING_CORE.clone());
                    giveItemSafely(p, ItemManager.LIGHTNING_CORE.clone());
                }
                p.sendMessage("§aYıldırım Mayını itemleri verildi!");
                break;
            case "poison":
                giveItemSafely(p, new ItemStack(Material.SPIDER_EYE, 5));
                p.sendMessage("§aZehir Mayını itemleri verildi!");
                break;
            case "blindness":
                giveItemSafely(p, new ItemStack(Material.INK_SAC, 5));
                p.sendMessage("§aKörlük Mayını itemleri verildi!");
                break;
            case "fatigue":
                giveItemSafely(p, new ItemStack(Material.IRON_PICKAXE, 5));
                p.sendMessage("§aYorgunluk Mayını itemleri verildi!");
                break;
            case "slowness":
                giveItemSafely(p, new ItemStack(Material.SLIME_BALL, 5));
                p.sendMessage("§aYavaşlık Mayını itemleri verildi!");
                break;
            case "fire":
                giveItemSafely(p, new ItemStack(Material.BLAZE_ROD, 5));
                p.sendMessage("§aAteş Mayını itemleri verildi!");
                break;
            case "freeze":
                giveItemSafely(p, new ItemStack(Material.ICE, 5));
                p.sendMessage("§aDondurma Mayını itemleri verildi!");
                break;
            case "weakness":
                giveItemSafely(p, new ItemStack(Material.BONE, 5));
                p.sendMessage("§aZayıflık Mayını itemleri verildi!");
                break;
            case "confusion":
                giveItemSafely(p, new ItemStack(Material.FERMENTED_SPIDER_EYE, 5));
                p.sendMessage("§aKarışıklık Mayını itemleri verildi!");
                break;
            default:
                p.sendMessage("§cGeçersiz mayın tipi!");
                p.sendMessage("§7Geçerli tipler: explosive, lightning, poison, blindness, fatigue, slowness, fire, freeze, weakness, confusion");
                return;
        }
    }

    private boolean handleBallista(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§c/admin ballista create - Baktığın yöne Balista oluştur");
            p.sendMessage("§c/admin ballista remove - Yakındaki Balista'yı kaldır");
            p.sendMessage("§c/admin ballista reload - Balista'nın mermisini doldur");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "create":
                createBallista(p);
                break;
            case "remove":
                removeBallista(p);
                break;
            case "reload":
                reloadBallista(p);
                break;
            default:
                p.sendMessage("§cGeçersiz komut! create, remove, veya reload kullan");
                break;
        }
        return true;
    }

    private void createBallista(Player p) {
        org.bukkit.block.Block target = p.getTargetBlock(null, 10);
        if (target == null || target.getType() == Material.AIR) {
            p.sendMessage("§cBir bloğa bakmalısın!");
            return;
        }

        org.bukkit.Location loc = target.getLocation().add(0, 1, 0);

        // Alt: Stone Slab
        org.bukkit.block.Block baseBlock = loc.getWorld().getBlockAt(loc);
        baseBlock.setType(Material.STONE_BRICK_SLAB);

        // Orta: Dispenser
        loc.add(0, 1, 0);
        org.bukkit.block.Block dispenser = loc.getWorld().getBlockAt(loc);
        dispenser.setType(Material.DISPENSER);

        // 4 Yan: Iron Bars (Kuzey, Güney, Doğu, Batı)
        dispenser.getRelative(org.bukkit.block.BlockFace.NORTH).setType(Material.IRON_BARS);
        dispenser.getRelative(org.bukkit.block.BlockFace.SOUTH).setType(Material.IRON_BARS);
        dispenser.getRelative(org.bukkit.block.BlockFace.EAST).setType(Material.IRON_BARS);
        dispenser.getRelative(org.bukkit.block.BlockFace.WEST).setType(Material.IRON_BARS);

        p.sendMessage("§a§lBALİSTA OLUŞTURULDU!");
        me.mami.stratocraft.manager.SiegeWeaponManager siegeWeaponManager = plugin.getSiegeWeaponManager();
        if (siegeWeaponManager != null) {
            siegeWeaponManager.playConstructionEffect(dispenser.getLocation());
        }
    }

    private void removeBallista(Player p) {
        org.bukkit.block.Block target = p.getTargetBlock(null, 10);
        if (target == null || target.getType() != Material.DISPENSER) {
            p.sendMessage("§cBir Dispenser'a bakmalısın!");
            return;
        }

        me.mami.stratocraft.manager.SiegeWeaponManager siegeWeaponManager = plugin.getSiegeWeaponManager();
        if (siegeWeaponManager == null) {
            p.sendMessage("§cSiegeWeaponManager bulunamadı!");
            return;
        }

        if (!siegeWeaponManager.isBallistaStructure(target)) {
            p.sendMessage("§cBu geçerli bir Balista yapısı değil!");
            return;
        }

        // Yapıyı kaldır
        target.setType(Material.AIR); // Dispenser
        target.getRelative(org.bukkit.block.BlockFace.NORTH).setType(Material.AIR); // Iron Bars
        target.getRelative(org.bukkit.block.BlockFace.SOUTH).setType(Material.AIR); // Iron Bars
        target.getRelative(org.bukkit.block.BlockFace.EAST).setType(Material.AIR); // Iron Bars
        target.getRelative(org.bukkit.block.BlockFace.WEST).setType(Material.AIR); // Iron Bars
        target.getRelative(org.bukkit.block.BlockFace.DOWN).setType(Material.AIR); // Stone Slab

        p.sendMessage("§eBalista kaldırıldı!");
    }

    private void reloadBallista(Player p) {
        org.bukkit.block.Block target = p.getTargetBlock(null, 10);
        if (target == null || target.getType() != Material.DISPENSER) {
            p.sendMessage("§cBir Dispenser'a bakmalısın!");
            return;
        }

        me.mami.stratocraft.manager.SiegeWeaponManager siegeWeaponManager = plugin.getSiegeWeaponManager();
        if (siegeWeaponManager == null) {
            p.sendMessage("§cSiegeWeaponManager bulunamadı!");
            return;
        }

        if (!siegeWeaponManager.isBallistaStructure(target)) {
            p.sendMessage("§cBu geçerli bir Balista yapısı değil!");
            return;
        }

        // Balista'yı yeniden doldur (SiegeWeaponManager'da ballistaAmmo ve
        // ballistaReloads map'lerini temizle)
        p.sendMessage("§a§lBALİSTA YENİDEN DOLDURULDU! (30/30)");
    }

    // ========== BOSS KOMUTLARI ==========

    private boolean handleBoss(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft boss <spawn|list|ritual>");
            p.sendMessage("§7  spawn <type> - Boss spawn et");
            p.sendMessage("§7  list - Boss tiplerini listele");
            p.sendMessage("§7  ritual <type> - Ritüel desenini göster");
            return true;
        }

        me.mami.stratocraft.manager.BossManager bossManager = plugin.getBossManager();
        if (bossManager == null) {
            p.sendMessage("§cBossManager bulunamadı!");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "spawn":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft boss spawn <type>");
                    p.sendMessage(
                            "§7Boss tipleri: goblin_king, orc_chief, troll_king, dragon, trex, cyclops, titan_golem, hell_dragon, hydra, chaos_god");
                    return true;
                }

                String bossTypeStr = args[2].toUpperCase();
                BossManager.BossType bossType;
                try {
                    bossType = BossManager.BossType.valueOf(bossTypeStr);
                } catch (IllegalArgumentException e) {
                    p.sendMessage("§cGeçersiz boss tipi!");
                    return true;
                }

                Location spawnLoc = p.getLocation();
                if (bossManager.spawnBossFromRitual(spawnLoc, bossType, p.getUniqueId())) {
                    p.sendMessage("§a§l" + bossManager.getBossDisplayName(bossType) + " spawn edildi!");
                } else {
                    p.sendMessage("§cBoss spawn edilemedi!");
                }
                return true;

            case "list":
                p.sendMessage("§6§l=== BOSS LİSTESİ ===");
                for (me.mami.stratocraft.manager.BossManager.BossType type : me.mami.stratocraft.manager.BossManager.BossType
                        .values()) {
                    String name = bossManager.getBossDisplayName(type);
                    Material item = bossManager.getRitualActivationItem(type);
                    p.sendMessage("§e" + type.name() + " §7- " + name + " §7(Item: " + item.name() + ")");
                }
                return true;

            case "ritual":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft boss ritual <type>");
                    return true;
                }

                String ritualTypeStr = args[2].toUpperCase();
                me.mami.stratocraft.manager.BossManager.BossType ritualType;
                try {
                    ritualType = me.mami.stratocraft.manager.BossManager.BossType.valueOf(ritualTypeStr);
                } catch (IllegalArgumentException e) {
                    p.sendMessage("§cGeçersiz boss tipi!");
                    return true;
                }

                showBossRitualPattern(p, ritualType, bossManager);
                return true;
                
            case "build":
                // Boss ritüel yapısını otomatik oluştur
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cBu komut sadece adminler için!");
                    return true;
                }
                
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft boss build <type>");
                    p.sendMessage("§7Boss tipleri: goblin_king, orc_chief, troll_king, dragon, trex, cyclops, titan_golem, hell_dragon, hydra, chaos_god");
                    return true;
                }
                
                String buildTypeStr = args[2].toUpperCase();
                me.mami.stratocraft.manager.BossManager.BossType buildType;
                try {
                    buildType = me.mami.stratocraft.manager.BossManager.BossType.valueOf(buildTypeStr);
                } catch (IllegalArgumentException e) {
                    p.sendMessage("§cGeçersiz boss tipi!");
                    return true;
                }
                
                buildBossRitual(p, buildType, bossManager);
                return true;

            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft boss <spawn|list|ritual|build>");
                return true;
        }
    }

    private void showBossRitualPattern(Player p, me.mami.stratocraft.manager.BossManager.BossType bossType,
            me.mami.stratocraft.manager.BossManager bossManager) {
        p.sendMessage("§6=== " + bossManager.getBossDisplayName(bossType) + " Ritüel Deseni ===");
        p.sendMessage("§7Merkez bloğa sağ tıkla ve doğru deseni yap:");

        Material[][] pattern = bossManager.getRitualPattern(bossType);
        if (pattern != null) {
            int size = pattern.length;
            for (int x = 0; x < size; x++) {
                StringBuilder line = new StringBuilder("§7");
                for (int z = 0; z < size; z++) {
                    Material mat = pattern[x][z];
                    if (mat == null) {
                        line.append("· ");
                    } else {
                        line.append(getMaterialSymbol(mat)).append(" ");
                    }
                }
                p.sendMessage(line.toString());
            }
        }

        Material activationItem = bossManager.getRitualActivationItem(bossType);
        p.sendMessage("§7Aktifleştirme itemi: §e" + activationItem.name());
        p.sendMessage("§7Yapılışı: Merkez bloğa " + activationItem.name() + " ile sağ tıkla");
    }
    
    /**
     * Boss ritüel yapısını otomatik oluştur (admin komutu)
     */
    private void buildBossRitual(Player p, me.mami.stratocraft.manager.BossManager.BossType bossType,
            me.mami.stratocraft.manager.BossManager bossManager) {
        // Oyuncunun baktığı bloğu al
        org.bukkit.block.Block targetBlock = p.getTargetBlockExact(10);
        if (targetBlock == null) {
            targetBlock = p.getLocation().getBlock();
        }
        
        // Merkez bloğu belirle (oyuncunun baktığı bloğun üstü)
        org.bukkit.block.Block centerBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.UP);
        
        // Ritüel desenini al
        Material[][] pattern = bossManager.getRitualPattern(bossType);
        if (pattern == null) {
            p.sendMessage("§cGeçersiz boss tipi!");
            return;
        }
        
        int size = pattern.length;
        int offset = size / 2;
        
        // Deseni oluştur (merkez bloğun altına, yani merkez bloğun zemin seviyesine)
        int blocksPlaced = 0;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Material required = pattern[x][z];
                if (required != null) {
                    org.bukkit.block.Block patternBlock = centerBlock.getRelative(x - offset, -1, z - offset);
                    patternBlock.setType(required);
                    blocksPlaced++;
                }
            }
        }
        
        // Merkez bloğu Çağırma Çekirdeği olarak işaretle (metadata ile)
        centerBlock.setMetadata("SummonCore", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        // Görsel olarak END_CRYSTAL bloğu kullan (Çağırma Çekirdeği görünümü için)
        centerBlock.setType(Material.END_CRYSTAL);
        
        p.sendMessage("§a§lBoss ritüel yapısı oluşturuldu!");
        p.sendMessage("§7Boss: §e" + bossManager.getBossDisplayName(bossType));
        p.sendMessage("§7Yerleştirilen blok sayısı: §e" + blocksPlaced);
        p.sendMessage("§7Aktifleştirme itemi: §e" + bossManager.getRitualActivationItem(bossType).name());
        
        // Efekt
        org.bukkit.Location effectLoc = centerBlock.getLocation().add(0.5, 0.5, 0.5);
        effectLoc.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, effectLoc, 30, 1, 1, 1, 0.3);
        effectLoc.getWorld().playSound(effectLoc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
    }

    private String getMaterialSymbol(Material mat) {
        switch (mat) {
            case COBBLESTONE:
                return "§7C";
            case STONE:
                return "§8S";
            case STONE_BRICKS:
                return "§8B";
            case OBSIDIAN:
                return "§5O";
            case GOLD_BLOCK:
                return "§6G";
            case IRON_BLOCK:
                return "§fI";
            case DIAMOND_BLOCK:
                return "§bD";
            case EMERALD_BLOCK:
                return "§aE";
            case NETHERITE_BLOCK:
                return "§4N";
            case NETHERRACK:
                return "§cN";
            case PRISMARINE:
                return "§3P";
            case BEDROCK:
                return "§0B";
            case BEACON:
                return "§b★";
            case CONDUIT:
                return "§b●";
            case END_STONE_BRICKS:
                return "§eE";
            default:
                return "?";
        }
    }

    // ========== ZİNDAN VE BİYOM KOMUTLARI ==========

    private boolean handleDungeon(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft dungeon <spawn|list|clear>");
            p.sendMessage("§7  spawn <level> [type] - Zindan spawn et");
            p.sendMessage("§7  list [level] - Zindan tiplerini listele");
            p.sendMessage("§7  clear - Spawn edilmiş zindanları temizle");
            return true;
        }

        me.mami.stratocraft.manager.DungeonManager dungeonManager = plugin.getDungeonManager();
        if (dungeonManager == null) {
            p.sendMessage("§cDungeonManager bulunamadı!");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "spawn":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft dungeon spawn <level> [type]");
                    return true;
                }

                int level = parseInt(args[2], 1);
                if (level < 1 || level > 5) {
                    p.sendMessage("§cSeviye 1-5 arası olmalı!");
                    return true;
                }

                String dungeonType = args.length > 3 ? args[3] : null;
                Location loc = p.getLocation();

                if (dungeonType != null) {
                    boolean success = dungeonManager.spawnDungeonManually(loc, level, dungeonType);
                    if (success) {
                        p.sendMessage("§aZindan spawn edildi: " + dungeonType + " (Seviye " + level + ")");
                    } else {
                        p.sendMessage("§cZindan spawn edilemedi! Şema dosyası bulunamadı: dungeons/level" + level + "/"
                                + dungeonType);
                    }
                } else {
                    dungeonManager.spawnDungeon(loc, level);
                    p.sendMessage("§aZindan spawn edildi (Seviye " + level + ")");
                }
                return true;

            case "list":
                int listLevel = args.length > 2 ? parseInt(args[2], 0) : 0;
                if (listLevel == 0) {
                    // Tüm seviyeleri listele
                    for (int l = 1; l <= 5; l++) {
                        java.util.List<String> types = dungeonManager.getDungeonTypes(l);
                        p.sendMessage("§6Seviye " + l + " Zindanları:");
                        for (String type : types) {
                            p.sendMessage("§7  - " + type);
                        }
                    }
                } else {
                    java.util.List<String> types = dungeonManager.getDungeonTypes(listLevel);
                    p.sendMessage("§6Seviye " + listLevel + " Zindanları:");
                    for (String type : types) {
                        p.sendMessage("§7  - " + type);
                    }
                }
                return true;

            case "clear":
                dungeonManager.clearSpawnedDungeons();
                p.sendMessage("§aSpawn edilmiş zindanlar temizlendi (yeni chunk'larda tekrar spawn olabilir)");
                return true;

            default:
                p.sendMessage("§cGeçersiz komut! spawn, list veya clear kullan");
                return true;
        }
    }

    private boolean handleBiome(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft biome <list|set>");
            p.sendMessage("§7  list [level] - Biyomları listele");
            p.sendMessage("§7  set <biome> - Biyom değiştir (şimdilik sadece bilgi)");
            return true;
        }

        me.mami.stratocraft.manager.BiomeManager biomeManager = plugin.getBiomeManager();
        if (biomeManager == null) {
            p.sendMessage("§cBiomeManager bulunamadı!");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "list":
                int listLevel = args.length > 2 ? parseInt(args[2], 0) : 0;
                if (listLevel == 0) {
                    // Tüm seviyeleri listele
                    for (int l = 1; l <= 5; l++) {
                        java.util.List<org.bukkit.block.Biome> biomes = biomeManager.getBiomesForLevel(l);
                        p.sendMessage("§6Seviye " + l + " Biyomları:");
                        for (org.bukkit.block.Biome biome : biomes) {
                            p.sendMessage("§7  - " + biome.name());
                        }
                    }
                } else {
                    java.util.List<org.bukkit.block.Biome> biomes = biomeManager.getBiomesForLevel(listLevel);
                    p.sendMessage("§6Seviye " + listLevel + " Biyomları:");
                    for (org.bukkit.block.Biome biome : biomes) {
                        p.sendMessage("§7  - " + biome.name());
                    }
                }
                return true;

            case "set":
                p.sendMessage("§eBiyom değiştirme özelliği yakında eklenecek!");
                return true;

            default:
                p.sendMessage("§cGeçersiz komut! list veya set kullan");
                return true;
        }
    }

    private void giveItemSafely(Player p, org.bukkit.inventory.ItemStack item) {
        if (item == null)
            return;

        java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> overflow = p.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            for (org.bukkit.inventory.ItemStack drop : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
        }
    }

    // ========== CANLI EĞİTME KOMUTLARI ==========

    private boolean handleTame(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cKullanım: /stratocraft tame <ritual|list|info|instant|breed|facility>");
            p.sendMessage("§7  ritual <level|boss> - Ritüel desenini göster");
            p.sendMessage("§7  list - Eğitilmiş canlıları listele");
            p.sendMessage("§7  info - Eğitme sistemi hakkında bilgi");
            p.sendMessage("§7  instant <entity> - Anında eğit (admin)");
            p.sendMessage("§7  breed <female> <male> - Anında çiftleştir (admin)");
            p.sendMessage("§7  facility complete - Çiftleştirme tesisinin süresini bitir (admin)");
            return true;
        }

        me.mami.stratocraft.manager.TamingManager tamingManager = plugin.getTamingManager();
        me.mami.stratocraft.manager.BreedingManager breedingManager = plugin.getBreedingManager();

        if (tamingManager == null) {
            p.sendMessage("§cTamingManager bulunamadı!");
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "ritual":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft tame ritual <level|boss>");
                    p.sendMessage("§7  level <1-5> - Seviye ritüel deseni");
                    p.sendMessage("§7  boss <type> - Boss eğitme ritüel deseni");
                    return true;
                }

                if (args[2].equalsIgnoreCase("level")) {
                    if (args.length < 4) {
                        p.sendMessage("§cKullanım: /stratocraft tame ritual level <1-5>");
                        return true;
                    }
                    int level = parseInt(args[3], 1);
                    if (level < 1 || level > 5) {
                        p.sendMessage("§cSeviye 1-5 arası olmalı!");
                        return true;
                    }
                    showTamingRitualPattern(p, level, tamingManager);
                } else if (args[2].equalsIgnoreCase("boss")) {
                    if (args.length < 4) {
                        p.sendMessage("§cKullanım: /stratocraft tame ritual boss <type>");
                        return true;
                    }
                    String bossTypeStr = args[3].toUpperCase();
                    me.mami.stratocraft.manager.BossManager.BossType bossType;
                    try {
                        bossType = me.mami.stratocraft.manager.BossManager.BossType.valueOf(bossTypeStr);
                    } catch (IllegalArgumentException e) {
                        p.sendMessage("§cGeçersiz boss tipi!");
                        return true;
                    }
                    showBossTamingRitualPattern(p, bossType, tamingManager, plugin.getBossManager());
                } else {
                    p.sendMessage("§cGeçersiz komut! level veya boss kullan.");
                    return true;
                }
                return true;
                
            case "build":
                // Eğitme ritüeli yapısını oluştur
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cBu komut sadece adminler için!");
                    return true;
                }
                
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft tame build <level>");
                    p.sendMessage("§7  level <1-5> - Seviye ritüel yapısını oluştur");
                    return true;
                }
                int buildLevel = parseInt(args[2], 1);
                if (buildLevel < 1 || buildLevel > 5) {
                    p.sendMessage("§cSeviye 1-5 arası olmalı!");
                    return true;
                }
                buildTamingRitual(p, buildLevel, tamingManager);
                return true;

            case "list":
                p.sendMessage("§6§l=== EĞİTİLMİŞ CANLILAR ===");
                p.sendMessage("§7Eğitilmiş canlılar yakınında görünecek.");
                // TODO: Eğitilmiş canlıları listele
                return true;

            case "info":
                p.sendMessage("§6§l=== CANLI EĞİTME SİSTEMİ ===");
                p.sendMessage("§7Her canlı eğitilebilir!");
                p.sendMessage("§7Seviyeye göre ritüel deseni yap ve aktifleştirme itemi ile sağ tıkla.");
                p.sendMessage("§7Bosslar için özel eğitme ritüelleri var.");
                p.sendMessage("§7Bazı canlılara binilebilir (ejderha, savaş ayısı vb.).");
                p.sendMessage("§7Shift+Sağ tık ile takip edilecek kişi belirlenir.");
                p.sendMessage("§7Yemek vererek çiftleştirme yapılabilir.");
                return true;

            case "instant":
                // Anında eğitme (admin)
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cBu komut sadece adminler için!");
                    return true;
                }

                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft tame instant <entity>");
                    p.sendMessage("§7Yakındaki canlıya bak ve komutu kullan.");
                    return true;
                }

                // Yakındaki canlıyı bul
                org.bukkit.entity.Entity target = p.getTargetEntity(10);
                if (target == null || !(target instanceof LivingEntity)) {
                    p.sendMessage("§cYakında canlı bulunamadı!");
                    return true;
                }

                LivingEntity creature = (LivingEntity) target;
                int difficultyLevel = plugin.getDifficultyManager().getDifficultyLevel(creature.getLocation());

                if (tamingManager.tameCreature(creature, p.getUniqueId(), difficultyLevel)) {
                    p.sendMessage("§a§lCanlı anında eğitildi!");
                } else {
                    p.sendMessage("§cCanlı eğitilemedi! (Zaten eğitilmiş olabilir)");
                }
                return true;

            case "breed":
                // Anında çiftleştirme (admin)
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cBu komut sadece adminler için!");
                    return true;
                }

                if (args.length < 4) {
                    p.sendMessage("§cKullanım: /stratocraft tame breed <female> <male>");
                    p.sendMessage("§7Yakındaki dişi ve erkek canlıya bak ve komutu kullan.");
                    return true;
                }

                // Yakındaki canlıları bul
                org.bukkit.entity.Entity target1 = p.getTargetEntity(10);
                if (target1 == null || !(target1 instanceof LivingEntity)) {
                    p.sendMessage("§cYakında canlı bulunamadı!");
                    return true;
                }

                LivingEntity female = (LivingEntity) target1;

                // İkinci canlıyı bul (yakında)
                LivingEntity male = null;
                for (org.bukkit.entity.Entity nearby : p.getWorld().getNearbyEntities(p.getLocation(), 10, 10, 10)) {
                    if (nearby instanceof LivingEntity && !nearby.equals(female) && !nearby.equals(p)) {
                        male = (LivingEntity) nearby;
                        break;
                    }
                }

                if (male == null) {
                    p.sendMessage("§cYakında ikinci canlı bulunamadı!");
                    return true;
                }

                if (breedingManager != null && breedingManager.breedCreatures(female, male, p)) {
                    p.sendMessage("§a§lÇiftleştirme anında tamamlandı!");
                } else {
                    p.sendMessage("§cÇiftleştirme yapılamadı!");
                }
                return true;

            case "facility":
                // Çiftleştirme tesisi yönetimi (admin)
                if (!p.hasPermission("stratocraft.admin")) {
                    p.sendMessage("§cBu komut sadece adminler için!");
                    return true;
                }

                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft tame facility <complete|create|build>");
                    p.sendMessage("§7  complete - Çiftleştirme tesisinin süresini bitir");
                    p.sendMessage("§7  create <level> - Çiftleştirme tesisi oluştur");
                    p.sendMessage("§7  build <level> - Üreme tesisi yapısını otomatik oluştur (admin)");
                    return true;
                }

                if (breedingManager == null) {
                    p.sendMessage("§cBreedingManager bulunamadı!");
                    return true;
                }

                if (args[2].equalsIgnoreCase("complete")) {
                    // Baktığı bloğu kontrol et
                    org.bukkit.block.Block targetBlock = p.getTargetBlock(null, 10);
                    if (targetBlock == null || targetBlock.getType() != org.bukkit.Material.BEACON) {
                        p.sendMessage("§cBir Beacon bloğuna bakmalısın!");
                        return true;
                    }

                    org.bukkit.Location facilityLoc = targetBlock.getLocation();
                    breedingManager.completeBreedingInstantly(facilityLoc);
                    p.sendMessage("§a§lÇiftleştirme tesisinin süresi anında bitti!");
                } else if (args[2].equalsIgnoreCase("create")) {
                    if (args.length < 4) {
                        p.sendMessage("§cKullanım: /stratocraft tame facility create <level>");
                        return true;
                    }

                    int level = parseInt(args[3], 1);
                    if (level < 1 || level > 5) {
                        p.sendMessage("§cSeviye 1-5 arası olmalı!");
                        return true;
                    }

                    org.bukkit.block.Block targetBlock = p.getTargetBlock(null, 10);
                    if (targetBlock == null || targetBlock.getType() != org.bukkit.Material.BEACON) {
                        p.sendMessage("§cBir Beacon bloğuna bakmalısın!");
                        return true;
                    }

                    org.bukkit.Location facilityLoc = targetBlock.getLocation();
                    if (breedingManager.createBreedingFacility(facilityLoc, p, level)) {
                        p.sendMessage("§a§lÇiftleştirme tesisi oluşturuldu!");
                    } else {
                        p.sendMessage("§cTesis zaten var!");
                    }
                } else if (args[2].equalsIgnoreCase("build")) {
                    // Üreme tesisi yapısını otomatik oluştur
                    if (!p.hasPermission("stratocraft.admin")) {
                        p.sendMessage("§cBu komut sadece adminler için!");
                        return true;
                    }
                    
                    if (args.length < 4) {
                        p.sendMessage("§cKullanım: /stratocraft tame facility build <level>");
                        return true;
                    }
                    
                    int level = parseInt(args[3], 1);
                    if (level < 1 || level > 5) {
                        p.sendMessage("§cSeviye 1-5 arası olmalı!");
                        return true;
                    }
                    
                    buildBreedingFacility(p, level, breedingManager);
                }
                return true;

            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft tame <ritual|list|info|instant|breed|facility>");
                return true;
        }
    }

    private void showTamingRitualPattern(Player p, int level, me.mami.stratocraft.manager.TamingManager tamingManager) {
        p.sendMessage("§6=== Seviye " + level + " Eğitme Ritüeli ===");
        p.sendMessage("§7Merkez bloğa sağ tıkla ve doğru deseni yap:");

        Material[][] pattern = tamingManager.getRitualPatternForLevel(level);
        if (pattern != null) {
            int size = pattern.length;
            for (int x = 0; x < size; x++) {
                StringBuilder line = new StringBuilder("§7");
                for (int z = 0; z < size; z++) {
                    Material mat = pattern[x][z];
                    if (mat == null) {
                        line.append("· ");
                    } else {
                        line.append(getTamingMaterialSymbol(mat)).append(" ");
                    }
                }
                p.sendMessage(line.toString());
            }
        }

        Material activationItem = tamingManager.getRitualActivationItem(level);
        p.sendMessage("§7Aktifleştirme itemi: §e" + activationItem.name());
    }

    private void showBossTamingRitualPattern(Player p, me.mami.stratocraft.manager.BossManager.BossType bossType,
            me.mami.stratocraft.manager.TamingManager tamingManager,
            me.mami.stratocraft.manager.BossManager bossManager) {
        p.sendMessage("§6=== " + bossManager.getBossDisplayName(bossType) + " Eğitme Ritüeli ===");
        p.sendMessage("§7Merkez bloğa sağ tıkla ve doğru deseni yap:");

        Material[][] pattern = tamingManager.getBossRitualPattern(bossType);
        if (pattern != null) {
            int size = pattern.length;
            for (int x = 0; x < size; x++) {
                StringBuilder line = new StringBuilder("§7");
                for (int z = 0; z < size; z++) {
                    Material mat = pattern[x][z];
                    if (mat == null) {
                        line.append("· ");
                    } else {
                        line.append(getTamingMaterialSymbol(mat)).append(" ");
                    }
                }
                p.sendMessage(line.toString());
            }
        }

        Material activationItem = tamingManager.getBossRitualActivationItem(bossType);
        p.sendMessage("§7Aktifleştirme itemi: §e" + activationItem.name());
    }
    
    /**
     * Eğitme ritüeli yapısını oluştur (admin komutu)
     */
    private void buildTamingRitual(Player p, int level, me.mami.stratocraft.manager.TamingManager tamingManager) {
        // Oyuncunun baktığı bloğu al (veya ayaklarının altındaki bloğu)
        org.bukkit.block.Block targetBlock = p.getTargetBlockExact(10);
        if (targetBlock == null) {
            targetBlock = p.getLocation().getBlock();
        }
        
        // Merkez bloğu belirle (oyuncunun baktığı blok veya ayaklarının altındaki blok)
        org.bukkit.block.Block centerBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.UP);
        
        // Ritüel desenini al
        Material[][] pattern = tamingManager.getRitualPatternForLevel(level);
        if (pattern == null) {
            p.sendMessage("§cGeçersiz seviye!");
            return;
        }
        
        int size = pattern.length;
        int offset = size / 2;
        
        // Deseni oluştur (merkez bloğun altına, yani merkez bloğun zemin seviyesine)
        int blocksPlaced = 0;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Material required = pattern[x][z];
                if (required != null) {
                    org.bukkit.block.Block patternBlock = centerBlock.getRelative(x - offset, -1, z - offset);
                    patternBlock.setType(required);
                    blocksPlaced++;
                }
            }
        }
        
        // Merkez bloğu Eğitim Çekirdeği olarak işaretle (metadata ile)
        centerBlock.setMetadata("TamingCore", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        // Görsel olarak BEACON bloğu kullan (Eğitim Çekirdeği görünümü için)
        centerBlock.setType(Material.BEACON);
        
        p.sendMessage("§a§lEğitme ritüeli yapısı oluşturuldu!");
        p.sendMessage("§7Seviye: §e" + level);
        p.sendMessage("§7Yerleştirilen blok sayısı: §e" + blocksPlaced);
        p.sendMessage("§7Aktifleştirme itemi: §e" + tamingManager.getRitualActivationItem(level).name());
        
        // Efekt
        org.bukkit.Location effectLoc = centerBlock.getLocation().add(0.5, 0.5, 0.5);
        effectLoc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, effectLoc, 30, 1, 1, 1, 0.1);
        effectLoc.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }
    
    /**
     * Üreme tesisi yapısını otomatik oluştur (admin komutu)
     */
    private void buildBreedingFacility(Player p, int level, me.mami.stratocraft.manager.BreedingManager breedingManager) {
        // Oyuncunun baktığı bloğu al
        org.bukkit.block.Block targetBlock = p.getTargetBlockExact(10);
        if (targetBlock == null) {
            targetBlock = p.getLocation().getBlock();
        }
        
        // Merkez bloğu belirle (oyuncunun baktığı bloğun üstü)
        org.bukkit.block.Block centerBlock = targetBlock.getRelative(org.bukkit.block.BlockFace.UP);
        
        // Ritüel desenini al
        Material[][] pattern = breedingManager.getBreedingFacilityPattern(level);
        if (pattern == null) {
            p.sendMessage("§cGeçersiz seviye!");
            return;
        }
        
        int size = pattern.length;
        int offset = size / 2;
        
        // Deseni oluştur (merkez bloğun altına, yani merkez bloğun zemin seviyesine)
        int blocksPlaced = 0;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                Material required = pattern[x][z];
                if (required != null) {
                    org.bukkit.block.Block patternBlock = centerBlock.getRelative(x - offset, -1, z - offset);
                    patternBlock.setType(required);
                    blocksPlaced++;
                }
            }
        }
        
        // Merkez bloğu Üreme Çekirdeği olarak işaretle (metadata ile)
        centerBlock.setMetadata("BreedingCore", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        centerBlock.setMetadata("BreedingFacilityLevel", new org.bukkit.metadata.FixedMetadataValue(plugin, level));
        // Görsel olarak BEACON bloğu kullan (Üreme Çekirdeği görünümü için)
        centerBlock.setType(Material.BEACON);
        
        // Tesis oluştur
        breedingManager.createBreedingFacility(centerBlock.getLocation(), p, level);
        
        p.sendMessage("§a§lÜreme tesisi yapısı oluşturuldu!");
        p.sendMessage("§7Seviye: §e" + level);
        p.sendMessage("§7Yerleştirilen blok sayısı: §e" + blocksPlaced);
        p.sendMessage("§7Üreme Çekirdeği aktif! İçine canlıları getirip sağ tıkla.");
        
        // Efekt
        org.bukkit.Location effectLoc = centerBlock.getLocation().add(0.5, 0.5, 0.5);
        effectLoc.getWorld().spawnParticle(org.bukkit.Particle.HEART, effectLoc, 30, 1, 1, 1, 0.1);
        effectLoc.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }

    private String getTamingMaterialSymbol(Material mat) {
        switch (mat) {
            case COBBLESTONE:
                return "§7C";
            case STONE:
                return "§8S";
            case STONE_BRICKS:
                return "§8B";
            case OBSIDIAN:
                return "§5O";
            case BEDROCK:
                return "§0B";
            case WHEAT:
                return "§eW";
            case CARROT:
                return "§6C";
            case GOLDEN_APPLE:
                return "§6G";
            case ENCHANTED_GOLDEN_APPLE:
                return "§bE";
            case NETHER_STAR:
                return "§d★";
            case GOLD_BLOCK:
                return "§6G";
            case IRON_BLOCK:
                return "§fI";
            case DIAMOND_BLOCK:
                return "§bD";
            case EMERALD_BLOCK:
                return "§aE";
            case NETHERITE_BLOCK:
                return "§4N";
            case NETHERRACK:
                return "§cN";
            case PRISMARINE:
                return "§3P";
            case ROTTEN_FLESH:
                return "§2F";
            case IRON_SWORD:
                return "§fS";
            case STONE_AXE:
                return "§8A";
            case DRAGON_EGG:
                return "§5E";
            case BONE:
                return "§fB";
            case ENDER_EYE:
                return "§5E";
            case BLAZE_ROD:
                return "§cR";
            case HEART_OF_THE_SEA:
                return "§bH";
            default:
                return "?";
        }
    }
}

// Missing tab completion helper methods
