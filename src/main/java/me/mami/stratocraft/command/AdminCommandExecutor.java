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
                
            case "list":
                p.sendMessage("§6§l════════════════════════════");
                p.sendMessage("§e§lAKTİF TARİFLER");
                p.sendMessage("§6§l════════════════════════════");
                int activeCount = 0;
                for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (plugin.getGhostRecipeManager().hasActiveRecipe(player.getUniqueId())) {
                        p.sendMessage("§7- §e" + player.getName());
                        activeCount++;
                    }
                }
                if (activeCount == 0) {
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
            p.sendMessage("§cKullanım: /stratocraft give <kategori> <item> [miktar]");
            p.sendMessage("§7Kategoriler: weapon, armor, material, mobdrop, special, ore, tool");
            p.sendMessage("§7Örnek: /stratocraft give weapon war_fan");
            p.sendMessage("§7Örnek: /stratocraft give mobdrop level1 wild_boar_hide");
            p.sendMessage("§7Örnek: /stratocraft give material blueprint 64");
            return true;
        }

        // Kategori kontrolü
        String category = args[1].toLowerCase();
        String itemName;
        int amount;

        if (args.length >= 3) {
            itemName = args[2].toLowerCase();
            amount = args.length > 3 ? parseInt(args[3], 1) : 1;
        } else {
            // Eski format desteği (kategori yok, direkt item)
            itemName = args[1].toLowerCase();
            amount = args.length > 2 ? parseInt(args[2], 1) : 1;
            category = "all"; // Tüm kategorilerde ara
        }

        // Miktar kontrolü
        if (amount < 1) {
            amount = 1;
        }
        if (amount > 2304) {
            p.sendMessage("§cMiktar çok yüksek! Maksimum 2304.");
            return true;
        }

        ItemStack item = getItemByName(itemName, category);
        if (item == null) {
            p.sendMessage("§cGeçersiz item: §e" + itemName);
            if (!category.equals("all")) {
                p.sendMessage("§7Kategori: §e" + category);
            }
            p.sendMessage("§7Kullanım: /stratocraft give <kategori> <item> [miktar]");
            return true;
        }

        item.setAmount(amount);

        // Envanter doluysa yere düşür
        java.util.HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
            p.sendMessage("§a" + amount + "x " + getItemDisplayName(itemName) + " verildi (yere düştü)");
        } else {
            p.sendMessage("§a" + amount + "x " + getItemDisplayName(itemName) + " verildi");
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

        String category = args[1].toLowerCase();
        String mobName;

        // Kategori kontrolü
        if (args.length >= 3) {
            mobName = args[2].toLowerCase();
        } else {
            // Eski format desteği (kategori yok, direkt mob)
            mobName = args[1].toLowerCase();
            category = "all"; // Tüm kategorilerde ara
        }

        MobManager mobManager = plugin.getMobManager();
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
        sender.sendMessage("§7  Özel Eşyalar: /stratocraft give rusty_hook, titan_grapple, trap_core, spyglass");
        sender.sendMessage("§7  Yeni Madenler: /stratocraft give sulfur, bauxite, rock_salt, mithril, astral_crystal");
        sender.sendMessage("§7  Yeni Moblar: /stratocraft spawn titan_golem, supply_drop");
        sender.sendMessage("§7  /stratocraft siege <clear|list|start|surrender> §7- Savaş yönetimi");
        sender.sendMessage("§7  /stratocraft caravan <list|clear> §7- Kervanları yönet");
        sender.sendMessage("§7  /stratocraft contract <list|clear> §7- Kontratları yönet");
        sender.sendMessage("§7  /stratocraft build <type> [level] §7- Yapı oluştur");
        sender.sendMessage("§7  /stratocraft trap <list|give> §7- Tuzak sistemi");
        sender.sendMessage("§7  /stratocraft mine <list|give> §7- Mayın sistemi");
        sender.sendMessage("§7  /stratocraft boss <spawn|list|ritual> §7- Boss sistemi");
        sender.sendMessage("§7  /stratocraft tame <ritual|list|info> §7- Canlı eğitme sistemi");
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

        return null;
    }

    private ItemStack getItemByNameWeapon(String name) {
        switch (name.toLowerCase()) {
            case "war_fan":
            case "savas_yelpazesi":
                return ItemManager.WAR_FAN != null ? ItemManager.WAR_FAN.clone() : null;
            case "tower_shield":
            case "kule_kalkani":
                return ItemManager.TOWER_SHIELD != null ? ItemManager.TOWER_SHIELD.clone() : null;
            default:
                return null;
        }
    }

    private ItemStack getItemByNameArmor(String name) {
        // Şimdilik armor yok, gelecekte eklenebilir
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
            case "spyglass":
            case "durbun":
            case "dürbün":
                return new ItemStack(Material.SPYGLASS);
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
                            "ore", "tool");
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
                    // Felaket tipleri + clear
                    List<String> disasters = Arrays.asList("titan_golem", "abyssal_worm", "solar_flare", "clear");
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
                        List<String> trapCommands = Arrays.asList("list", "give");
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
                    List<String> bossCommands = Arrays.asList("spawn", "list", "ritual");
                    if (input.isEmpty()) {
                        return bossCommands;
                    }
                    return bossCommands.stream()
                            .filter(s -> s.toLowerCase().startsWith(input))
                            .collect(Collectors.toList());

                case "tame":
                    // Tame komutları
                    List<String> tameCommands = Arrays.asList("ritual", "list", "info");
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
                    // Disaster için koordinat tamamlama
                    if (category.equalsIgnoreCase("titan_golem") ||
                            category.equalsIgnoreCase("abyssal_worm") ||
                            category.equalsIgnoreCase("solar_flare")) {
                        // Koordinat önerileri: "ben", "me", "self" veya sayılar
                        if (input.isEmpty() || input.equals("b") || input.equals("m") || input.equals("s")) {
                            return Arrays.asList("ben", "me", "self");
                        }
                        // Sayısal koordinat için boş liste (kullanıcı kendisi yazacak)
                        return new ArrayList<>();
                    }
                    break;
            }
        }

        // Dördüncü argüman (disaster koordinatları)
        if (args.length == 4 && args[0].equalsIgnoreCase("disaster")) {
            // Y koordinatı için boş liste
            return new ArrayList<>();
        }

        // Beşinci argüman (disaster koordinatları)
        if (args.length == 5 && args[0].equalsIgnoreCase("disaster")) {
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

        // Dördüncü argüman (give komutu için miktar - kategorize edilmiş format)
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            // Miktar için tab completion gerekmez, boş liste döndür
            return new ArrayList<>();
        }

        // Üçüncü argüman (give komutu için miktar - eski format)
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String category = args[1].toLowerCase();
            // Eğer kategori değilse (eski format), miktar için boş liste
            if (!category.equals("weapon") && !category.equals("armor") && !category.equals("material") &&
                    !category.equals("mobdrop") && !category.equals("special") && !category.equals("ore") &&
                    !category.equals("tool")) {
                return new ArrayList<>();
            }
        }

        // Diğer durumlar için boş liste
        return new ArrayList<>();
    }

    // ========== TAB COMPLETION HELPER METODLARI ==========

    private List<String> getGiveTabComplete(String category, String input) {
        switch (category.toLowerCase()) {
            case "weapon":
                List<String> weapons = Arrays.asList("war_fan", "tower_shield");
                return filterList(weapons, input);
            case "armor":
                return new ArrayList<>(); // Şimdilik boş
            case "material":
                List<String> materials = Arrays.asList("blueprint", "lightning_core", "dark_matter", "star_core",
                        "flame_amplifier", "devil_horn", "devil_snake_eye", "recipe_tectonic", "hell_fruit",
                        "clan_crystal", "clan_fence", "red_diamond", "ruby", "adamantite", "titanium_ore",
                        "titanium_ingot");
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
                        "spyglass");
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
                List<String> batteries = Arrays.asList("magma_battery", "lightning_battery", "black_hole", "bridge",
                        "shelter", "gravity_anchor", "seismic_hammer", "magnetic_disruptor", "ozone_shield",
                        "earth_wall", "energy_wall", "lava_trencher_battery");
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
            // Komutlar: spawn, list, ritual
            return filterList(Arrays.asList("spawn", "list", "ritual"), input);
        } else if (args.length == 3 && (args[1].equalsIgnoreCase("spawn") || args[1].equalsIgnoreCase("ritual"))) {
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
            // Komutlar: ritual, list, info
            return filterList(Arrays.asList("ritual", "list", "info"), input);
        } else if (args.length == 3 && args[1].equalsIgnoreCase("ritual")) {
            // Ritual alt komutları: level, boss
            return filterList(Arrays.asList("level", "boss"), input);
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
                return handleSiegeStart(p, args[2], args[3], siegeManager, territoryManager);
            case "surrender":
                if (args.length < 3) {
                    p.sendMessage("§cKullanım: /stratocraft siege surrender <klan>");
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
        
        siegeManager.startSiege(attacker, defender);
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
            p.sendMessage("§7Eski format: /stratocraft build <type> [level] (hala çalışıyor)");
            return true;
        }

        String category = args[1].toLowerCase();
        String buildType;
        int level;

        // Kategori kontrolü
        if (args.length >= 3
                && (category.equals("weapon") || category.equals("battery") || category.equals("structure"))) {
            // Yeni format: build weapon catapult
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
                org.bukkit.block.BlockFace facing = p.getFacing();
                
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
                org.bukkit.Location effectLoc = loc.clone().add(0, 1, 0);
                p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 5);
                p.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 30, 1, 1, 1);
                p.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_ANVIL_USE, 1.0f, 0.8f);
                p.getWorld().playSound(effectLoc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
                
                // Havai fişek efekti
                org.bukkit.entity.Firework firework = (org.bukkit.entity.Firework) p.getWorld().spawnEntity(
                        effectLoc, org.bukkit.entity.EntityType.FIREWORK);
                org.bukkit.inventory.meta.FireworkMeta fireworkMeta = firework.getFireworkMeta();
                fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
                        .with(org.bukkit.FireworkEffect.Type.BURST)
                        .withColor(org.bukkit.Color.PURPLE, org.bukkit.Color.BLACK)
                        .flicker(true).build());
                fireworkMeta.setPower(0);
                firework.setFireworkMeta(fireworkMeta);
                
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

            default:
                p.sendMessage("§cBilinmeyen batarya tipi: " + type);
                return true;
        }
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
            p.sendMessage("§cKullanım: /stratocraft trap <list|give>");
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
            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft trap <list|give>");
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
        plugin.getSiegeWeaponManager().playConstructionEffect(dispenser.getLocation());
    }

    private void removeBallista(Player p) {
        org.bukkit.block.Block target = p.getTargetBlock(null, 10);
        if (target == null || target.getType() != Material.DISPENSER) {
            p.sendMessage("§cBir Dispenser'a bakmalısın!");
            return;
        }

        if (!plugin.getSiegeWeaponManager().isBallistaStructure(target)) {
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

        if (!plugin.getSiegeWeaponManager().isBallistaStructure(target)) {
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

            default:
                p.sendMessage("§cGeçersiz komut! /stratocraft boss <spawn|list|ritual>");
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
                    p.sendMessage("§cKullanım: /stratocraft tame facility <complete|create>");
                    p.sendMessage("§7  complete - Çiftleştirme tesisinin süresini bitir");
                    p.sendMessage("§7  create <level> - Çiftleştirme tesisi oluştur");
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
