package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemManager {
    public static ItemStack BLUEPRINT_PAPER;
    public static ItemStack LIGHTNING_CORE;
    public static ItemStack TITANIUM_ORE;
    public static ItemStack TITANIUM_INGOT;
    public static ItemStack DARK_MATTER;
    public static ItemStack RED_DIAMOND;
    public static ItemStack RUBY;
    public static ItemStack ADAMANTITE;
    public static ItemStack STAR_CORE;
    public static ItemStack FLAME_AMPLIFIER;
    public static ItemStack DEVIL_HORN;
    public static ItemStack DEVIL_SNAKE_EYE;
    public static ItemStack RECIPE_BOOK_TECTONIC;
    public static ItemStack RECIPE_TECTONIC_STABILIZER; // Alias for RECIPE_BOOK_TECTONIC
    public static ItemStack WAR_FAN;
    public static ItemStack TOWER_SHIELD;
    public static ItemStack HELL_FRUIT;

    // ========== TARİF KİTAPLARI - YAPILAR ==========
    // Sadece bazı yapılar tarif gerektirir (aktifleştirme için)
    // Tüm yapıların tarifi var ama sadece bazıları çalışması için tarif gerektirir
    public static ItemStack RECIPE_CORE;
    public static ItemStack RECIPE_ALCHEMY_TOWER;
    public static ItemStack RECIPE_POISON_REACTOR;
    public static ItemStack RECIPE_SIEGE_FACTORY;
    public static ItemStack RECIPE_WALL_GENERATOR;
    public static ItemStack RECIPE_GRAVITY_WELL;
    public static ItemStack RECIPE_LAVA_TRENCHER;
    public static ItemStack RECIPE_WATCHTOWER;
    public static ItemStack RECIPE_DRONE_STATION;
    public static ItemStack RECIPE_AUTO_TURRET;
    public static ItemStack RECIPE_GLOBAL_MARKET_GATE;
    public static ItemStack RECIPE_AUTO_DRILL;
    public static ItemStack RECIPE_XP_BANK;
    public static ItemStack RECIPE_MAG_RAIL;
    public static ItemStack RECIPE_TELEPORTER;
    public static ItemStack RECIPE_FOOD_SILO;
    public static ItemStack RECIPE_OIL_REFINERY;
    public static ItemStack RECIPE_HEALING_BEACON;
    public static ItemStack RECIPE_WEATHER_MACHINE;
    public static ItemStack RECIPE_CROP_ACCELERATOR;
    public static ItemStack RECIPE_MOB_GRINDER;
    public static ItemStack RECIPE_INVISIBILITY_CLOAK;
    public static ItemStack RECIPE_ARMORY;
    public static ItemStack RECIPE_LIBRARY;
    public static ItemStack RECIPE_WARNING_SIGN;

    // ========== TARİF KİTAPLARI - ÖZEL EŞYALAR ==========
    public static ItemStack RECIPE_LIGHTNING_CORE;
    public static ItemStack RECIPE_TITANIUM_INGOT;
    public static ItemStack RECIPE_DARK_MATTER;
    public static ItemStack RECIPE_RED_DIAMOND;
    public static ItemStack RECIPE_RUBY;
    public static ItemStack RECIPE_ADAMANTITE;
    public static ItemStack RECIPE_STAR_CORE;
    public static ItemStack RECIPE_FLAME_AMPLIFIER;
    public static ItemStack RECIPE_DEVIL_HORN;
    public static ItemStack RECIPE_DEVIL_SNAKE_EYE;
    public static ItemStack RECIPE_WAR_FAN;
    public static ItemStack RECIPE_TOWER_SHIELD;
    public static ItemStack RECIPE_HELL_FRUIT;
    public static ItemStack RECIPE_SULFUR;
    public static ItemStack RECIPE_BAUXITE_INGOT;
    public static ItemStack RECIPE_ROCK_SALT;
    public static ItemStack RECIPE_MITHRIL_INGOT;
    public static ItemStack RECIPE_MITHRIL_STRING;
    public static ItemStack RECIPE_ASTRAL_CRYSTAL;
    public static ItemStack RECIPE_RUSTY_HOOK;
    public static ItemStack RECIPE_GOLDEN_HOOK;
    public static ItemStack RECIPE_TITAN_GRAPPLE;
    public static ItemStack RECIPE_TRAP_CORE;

    // Yeni Madenler
    public static ItemStack SULFUR_ORE;
    public static ItemStack SULFUR;
    public static ItemStack BAUXITE_ORE;
    public static ItemStack BAUXITE_INGOT;
    public static ItemStack ROCK_SALT_ORE;
    public static ItemStack ROCK_SALT;
    public static ItemStack MITHRIL_ORE;
    public static ItemStack MITHRIL_INGOT;
    public static ItemStack MITHRIL_STRING;
    public static ItemStack ASTRAL_ORE;
    public static ItemStack ASTRAL_CRYSTAL;

    // Yeni Eşyalar
    public static ItemStack RUSTY_HOOK;
    public static ItemStack GOLDEN_HOOK; // YENİ: Orta kademe kanca
    public static ItemStack TITAN_GRAPPLE;
    public static ItemStack TRAP_CORE;

    // ========== SEVİYE 1 MOB DROP İTEMLERİ ==========
    public static ItemStack WILD_BOAR_HIDE;
    public static ItemStack WILD_BOAR_MEAT;
    public static ItemStack WOLF_FANG;
    public static ItemStack WOLF_PELT;
    public static ItemStack SNAKE_VENOM;
    public static ItemStack SNAKE_SKIN;
    public static ItemStack EAGLE_FEATHER;
    public static ItemStack EAGLE_CLAW;
    public static ItemStack BEAR_CLAW;
    public static ItemStack BEAR_PELT;

    // ========== SEVİYE 2 MOB DROP İTEMLERİ ==========
    public static ItemStack IRON_CORE;
    public static ItemStack IRON_DUST;
    public static ItemStack ICE_HEART;
    public static ItemStack ICE_CRYSTAL;
    public static ItemStack FIRE_CORE;
    public static ItemStack FIRE_SCALE;
    public static ItemStack EARTH_STONE;
    public static ItemStack EARTH_DUST;
    public static ItemStack SOUL_FRAGMENT;
    public static ItemStack GHOST_DUST;

    // ========== SEVİYE 3 MOB DROP İTEMLERİ ==========
    public static ItemStack SHADOW_HEART;
    public static ItemStack SHADOW_SCALE;
    public static ItemStack LIGHT_HEART;
    public static ItemStack LIGHT_FEATHER;
    public static ItemStack STORM_CORE;
    public static ItemStack STORM_DUST;
    public static ItemStack LAVA_HEART;
    public static ItemStack LAVA_SCALE;
    public static ItemStack ICE_CORE;
    public static ItemStack ICE_SHARD;

    // ========== SEVİYE 4 MOB DROP İTEMLERİ ==========
    public static ItemStack DEVIL_BLOOD; // Şeytan Kanı (her zaman düşer)
    public static ItemStack BLACK_DRAGON_HEART;
    public static ItemStack BLACK_DRAGON_SCALE;
    public static ItemStack DEATH_SWORD_FRAGMENT;
    public static ItemStack DEATH_DUST;
    public static ItemStack CHAOS_CORE;
    public static ItemStack CHAOS_SCALE;
    public static ItemStack HELL_STONE;
    public static ItemStack HELL_FIRE;

    // ========== SEVİYE 5 MOB DROP İTEMLERİ ==========
    public static ItemStack LEGENDARY_DRAGON_HEART;
    public static ItemStack LEGENDARY_DRAGON_SCALE;
    public static ItemStack GOD_BLOOD;
    public static ItemStack GOD_FRAGMENT;
    public static ItemStack VOID_CORE;
    public static ItemStack VOID_DUST;
    public static ItemStack TIME_CORE;
    public static ItemStack TIME_SCALE;
    public static ItemStack FATE_STONE;
    public static ItemStack FATE_FRAGMENT;

    public void init() {
        BLUEPRINT_PAPER = create(Material.PAPER, "BLUEPRINT", "§bMühendis Şeması");
        LIGHTNING_CORE = create(Material.END_ROD, "LIGHTNING_CORE", "§eYıldırım Çekirdeği");
        TITANIUM_ORE = create(Material.FLINT, "TITANIUM", "§7Titanyum Parçası");
        TITANIUM_INGOT = create(Material.IRON_INGOT, "TITANIUM_INGOT", "§fTitanyum Külçesi");
        DARK_MATTER = create(Material.COAL, "DARK_MATTER", "§0Karanlık Madde");
        RED_DIAMOND = create(Material.DIAMOND, "RED_DIAMOND", "§cKızıl Elmas");
        RUBY = create(Material.REDSTONE, "RUBY", "§cYakut");
        ADAMANTITE = create(Material.NETHERITE_INGOT, "ADAMANTITE", "§5Adamantite");
        STAR_CORE = create(Material.NETHER_STAR, "STAR_CORE", "§bYıldız Çekirdeği");
        FLAME_AMPLIFIER = create(Material.BLAZE_ROD, "FLAME_AMPLIFIER", "§6Alev Amplifikatörü");
        DEVIL_HORN = create(Material.GOAT_HORN, "DEVIL_HORN", "§4Şeytan Boynuzu");
        DEVIL_SNAKE_EYE = create(Material.ENDER_EYE, "DEVIL_SNAKE_EYE", "§5İblis Yılanın Gözü");
        RECIPE_BOOK_TECTONIC = createRecipeBook("RECIPE_TECTONIC", "§dTarif: Tektonik Sabitleyici");
        RECIPE_TECTONIC_STABILIZER = RECIPE_BOOK_TECTONIC; // Alias
        WAR_FAN = create(Material.FEATHER, "WAR_FAN", "§eSavaş Yelpazesi");
        TOWER_SHIELD = create(Material.SHIELD, "TOWER_SHIELD", "§7Kule Kalkanı");
        HELL_FRUIT = create(Material.APPLE, "HELL_FRUIT", "§cCehennem Meyvesi");

        // ========== TARİF KİTAPLARI - YAPILAR ==========
        RECIPE_CORE = createRecipeBook("RECIPE_CORE", "§bTarif: Ana Kristal");
        RECIPE_ALCHEMY_TOWER = createRecipeBook("RECIPE_ALCHEMY_TOWER", "§dTarif: Simya Kulesi");
        RECIPE_POISON_REACTOR = createRecipeBook("RECIPE_POISON_REACTOR", "§2Tarif: Zehir Reaktörü");
        RECIPE_SIEGE_FACTORY = createRecipeBook("RECIPE_SIEGE_FACTORY", "§cTarif: Kuşatma Fabrikası");
        RECIPE_WALL_GENERATOR = createRecipeBook("RECIPE_WALL_GENERATOR", "§7Tarif: Sur Jeneratörü");
        RECIPE_GRAVITY_WELL = createRecipeBook("RECIPE_GRAVITY_WELL", "§5Tarif: Yerçekimi Kuyusu");
        RECIPE_LAVA_TRENCHER = createRecipeBook("RECIPE_LAVA_TRENCHER", "§cTarif: Lav Hendekçisi");
        RECIPE_WATCHTOWER = createRecipeBook("RECIPE_WATCHTOWER", "§eTarif: Gözetleme Kulesi");
        RECIPE_DRONE_STATION = createRecipeBook("RECIPE_DRONE_STATION", "§bTarif: Drone İstasyonu");
        RECIPE_AUTO_TURRET = createRecipeBook("RECIPE_AUTO_TURRET", "§6Tarif: Otomatik Taret");
        RECIPE_GLOBAL_MARKET_GATE = createRecipeBook("RECIPE_GLOBAL_MARKET_GATE", "§aTarif: Global Pazar Kapısı");
        RECIPE_AUTO_DRILL = createRecipeBook("RECIPE_AUTO_DRILL", "§7Tarif: Otomatik Madenci");
        RECIPE_XP_BANK = createRecipeBook("RECIPE_XP_BANK", "§eTarif: Tecrübe Bankası");
        RECIPE_MAG_RAIL = createRecipeBook("RECIPE_MAG_RAIL", "§bTarif: Manyetik Ray");
        RECIPE_TELEPORTER = createRecipeBook("RECIPE_TELEPORTER", "§dTarif: Işınlanma Platformu");
        RECIPE_FOOD_SILO = createRecipeBook("RECIPE_FOOD_SILO", "§6Tarif: Buzdolabı");
        RECIPE_OIL_REFINERY = createRecipeBook("RECIPE_OIL_REFINERY", "§8Tarif: Petrol Rafinerisi");
        RECIPE_HEALING_BEACON = createRecipeBook("RECIPE_HEALING_BEACON", "§aTarif: Şifa Kulesi");
        RECIPE_WEATHER_MACHINE = createRecipeBook("RECIPE_WEATHER_MACHINE", "§bTarif: Hava Kontrolcüsü");
        RECIPE_CROP_ACCELERATOR = createRecipeBook("RECIPE_CROP_ACCELERATOR", "§2Tarif: Tarım Hızlandırıcı");
        RECIPE_MOB_GRINDER = createRecipeBook("RECIPE_MOB_GRINDER", "§cTarif: Mob Öğütücü");
        RECIPE_INVISIBILITY_CLOAK = createRecipeBook("RECIPE_INVISIBILITY_CLOAK", "§7Tarif: Görünmezlik Perdesi");
        RECIPE_ARMORY = createRecipeBook("RECIPE_ARMORY", "§6Tarif: Cephanelik");
        RECIPE_LIBRARY = createRecipeBook("RECIPE_LIBRARY", "§eTarif: Kütüphane");
        RECIPE_WARNING_SIGN = createRecipeBook("RECIPE_WARNING_SIGN", "§cTarif: Yasaklı Bölge Tabelası");

        // ========== TARİF KİTAPLARI - ÖZEL EŞYALAR ==========
        RECIPE_LIGHTNING_CORE = createRecipeBook("RECIPE_LIGHTNING_CORE", "§eTarif: Yıldırım Çekirdeği");
        RECIPE_TITANIUM_INGOT = createRecipeBook("RECIPE_TITANIUM_INGOT", "§fTarif: Titanyum Külçesi");
        RECIPE_DARK_MATTER = createRecipeBook("RECIPE_DARK_MATTER", "§0Tarif: Karanlık Madde");
        RECIPE_RED_DIAMOND = createRecipeBook("RECIPE_RED_DIAMOND", "§cTarif: Kızıl Elmas");
        RECIPE_RUBY = createRecipeBook("RECIPE_RUBY", "§cTarif: Yakut");
        RECIPE_ADAMANTITE = createRecipeBook("RECIPE_ADAMANTITE", "§5Tarif: Adamantite");
        RECIPE_STAR_CORE = createRecipeBook("RECIPE_STAR_CORE", "§bTarif: Yıldız Çekirdeği");
        RECIPE_FLAME_AMPLIFIER = createRecipeBook("RECIPE_FLAME_AMPLIFIER", "§6Tarif: Alev Amplifikatörü");
        RECIPE_DEVIL_HORN = createRecipeBook("RECIPE_DEVIL_HORN", "§4Tarif: Şeytan Boynuzu");
        RECIPE_DEVIL_SNAKE_EYE = createRecipeBook("RECIPE_DEVIL_SNAKE_EYE", "§5Tarif: İblis Yılanın Gözü");
        RECIPE_WAR_FAN = createRecipeBook("RECIPE_WAR_FAN", "§eTarif: Savaş Yelpazesi");
        RECIPE_TOWER_SHIELD = createRecipeBook("RECIPE_TOWER_SHIELD", "§7Tarif: Kule Kalkanı");
        RECIPE_HELL_FRUIT = createRecipeBook("RECIPE_HELL_FRUIT", "§cTarif: Cehennem Meyvesi");
        RECIPE_SULFUR = createRecipeBook("RECIPE_SULFUR", "§eTarif: Kükürt");
        RECIPE_BAUXITE_INGOT = createRecipeBook("RECIPE_BAUXITE_INGOT", "§6Tarif: Boksit Külçesi");
        RECIPE_ROCK_SALT = createRecipeBook("RECIPE_ROCK_SALT", "§fTarif: Tuz");
        RECIPE_MITHRIL_INGOT = createRecipeBook("RECIPE_MITHRIL_INGOT", "§bTarif: Mithril Külçesi");
        RECIPE_MITHRIL_STRING = createRecipeBook("RECIPE_MITHRIL_STRING", "§bTarif: Mithril İpi");
        RECIPE_ASTRAL_CRYSTAL = createRecipeBook("RECIPE_ASTRAL_CRYSTAL", "§5Tarif: Astral Kristali");
        RECIPE_RUSTY_HOOK = createRecipeBook("RECIPE_RUSTY_HOOK", "§7Tarif: Paslı Kanca");
        RECIPE_GOLDEN_HOOK = createRecipeBook("RECIPE_GOLDEN_HOOK", "§6Tarif: Altın Kanca");
        RECIPE_TITAN_GRAPPLE = createRecipeBook("RECIPE_TITAN_GRAPPLE", "§6§lTarif: Titan Kancası");
        RECIPE_TRAP_CORE = createRecipeBook("RECIPE_TRAP_CORE", "§cTarif: Tuzak Çekirdeği");

        // Yeni Madenler
        SULFUR_ORE = create(Material.YELLOW_CONCRETE_POWDER, "SULFUR_ORE", "§eKükürt Cevheri");
        SULFUR = create(Material.GUNPOWDER, "SULFUR", "§eKükürt");
        BAUXITE_ORE = create(Material.ORANGE_CONCRETE_POWDER, "BAUXITE_ORE", "§6Boksit Cevheri");
        BAUXITE_INGOT = create(Material.COPPER_INGOT, "BAUXITE_INGOT", "§6Boksit Külçesi");
        ROCK_SALT_ORE = create(Material.QUARTZ_BLOCK, "ROCK_SALT_ORE", "§fTuz Kayası");
        ROCK_SALT = create(Material.SUGAR, "ROCK_SALT", "§fTuz");
        MITHRIL_ORE = create(Material.LIGHT_BLUE_CONCRETE_POWDER, "MITHRIL_ORE", "§bMithril Cevheri");
        MITHRIL_INGOT = create(Material.IRON_INGOT, "MITHRIL_INGOT", "§bMithril Külçesi");
        MITHRIL_STRING = create(Material.STRING, "MITHRIL_STRING", "§bMithril İpi");
        ASTRAL_ORE = create(Material.AMETHYST_BLOCK, "ASTRAL_ORE", "§5Astral Cevheri");
        ASTRAL_CRYSTAL = create(Material.ECHO_SHARD, "ASTRAL_CRYSTAL", "§5Astral Kristali");

        // Yeni Eşyalar - 3 Kademeli Kanca Sistemi
        RUSTY_HOOK = create(Material.FISHING_ROD, "RUSTY_HOOK", "§7Paslı Kanca");
        GOLDEN_HOOK = create(Material.FISHING_ROD, "GOLDEN_HOOK", "§6Altın Kanca");
        TITAN_GRAPPLE = create(Material.FISHING_ROD, "TITAN_GRAPPLE", "§6§lTitan Kancası");
        TRAP_CORE = create(Material.LODESTONE, "TRAP_CORE", "§cTuzak Çekirdeği");

        // ========== SEVİYE 1 MOB DROP İTEMLERİ ==========
        WILD_BOAR_HIDE = create(Material.LEATHER, "WILD_BOAR_HIDE", "§6Yaban Domuzu Postu");
        WILD_BOAR_MEAT = create(Material.PORKCHOP, "WILD_BOAR_MEAT", "§6Yaban Domuzu Eti");
        WOLF_FANG = create(Material.BONE, "WOLF_FANG", "§7Kurt Dişi");
        WOLF_PELT = create(Material.LEATHER, "WOLF_PELT", "§7Kurt Postu");
        SNAKE_VENOM = create(Material.POISONOUS_POTATO, "SNAKE_VENOM", "§2Yılan Zehri");
        SNAKE_SKIN = create(Material.LEATHER, "SNAKE_SKIN", "§2Yılan Derisi");
        EAGLE_FEATHER = create(Material.FEATHER, "EAGLE_FEATHER", "§eKartal Tüyü");
        EAGLE_CLAW = create(Material.FLINT, "EAGLE_CLAW", "§eKartal Pençesi");
        BEAR_CLAW = create(Material.FLINT, "BEAR_CLAW", "§7Ayı Pençesi");
        BEAR_PELT = create(Material.LEATHER, "BEAR_PELT", "§7Ayı Postu");

        // ========== SEVİYE 2 MOB DROP İTEMLERİ ==========
        IRON_CORE = create(Material.IRON_INGOT, "IRON_CORE", "§fDemir Çekirdek");
        IRON_DUST = create(Material.GUNPOWDER, "IRON_DUST", "§fDemir Tozu");
        ICE_HEART = create(Material.BLUE_ICE, "ICE_HEART", "§bBuz Kalbi");
        ICE_CRYSTAL = create(Material.PACKED_ICE, "ICE_CRYSTAL", "§bBuz Kristali");
        FIRE_CORE = create(Material.BLAZE_ROD, "FIRE_CORE", "§cAteş Çekirdeği");
        FIRE_SCALE = create(Material.MAGMA_CREAM, "FIRE_SCALE", "§cAteş Ölçeği");
        EARTH_STONE = create(Material.COBBLESTONE, "EARTH_STONE", "§6Toprak Taşı");
        EARTH_DUST = create(Material.DIRT, "EARTH_DUST", "§6Toprak Tozu");
        SOUL_FRAGMENT = create(Material.ECHO_SHARD, "SOUL_FRAGMENT", "§5Ruh Parçası");
        GHOST_DUST = create(Material.GUNPOWDER, "GHOST_DUST", "§7Hayalet Tozu");

        // ========== SEVİYE 3 MOB DROP İTEMLERİ ==========
        SHADOW_HEART = create(Material.COAL, "SHADOW_HEART", "§8Gölge Kalbi");
        SHADOW_SCALE = create(Material.BLACK_DYE, "SHADOW_SCALE", "§8Gölge Ölçeği");
        LIGHT_HEART = create(Material.GLOWSTONE_DUST, "LIGHT_HEART", "§eIşık Kalbi");
        LIGHT_FEATHER = create(Material.FEATHER, "LIGHT_FEATHER", "§eIşık Tüyü");
        STORM_CORE = create(Material.LIGHTNING_ROD, "STORM_CORE", "§bFırtına Çekirdeği");
        STORM_DUST = create(Material.GUNPOWDER, "STORM_DUST", "§bFırtına Tozu");
        LAVA_HEART = create(Material.MAGMA_CREAM, "LAVA_HEART", "§cLav Kalbi");
        LAVA_SCALE = create(Material.MAGMA_CREAM, "LAVA_SCALE", "§cLav Ölçeği");
        ICE_CORE = create(Material.BLUE_ICE, "ICE_CORE", "§bBuz Çekirdeği");
        ICE_SHARD = create(Material.PACKED_ICE, "ICE_SHARD", "§bBuz Parçası");

        // ========== SEVİYE 4 MOB DROP İTEMLERİ ==========
        DEVIL_BLOOD = create(Material.REDSTONE, "DEVIL_BLOOD", "§4Şeytan Kanı");
        BLACK_DRAGON_HEART = create(Material.NETHER_STAR, "BLACK_DRAGON_HEART", "§0Kara Ejder Kalbi");
        BLACK_DRAGON_SCALE = create(Material.BLACK_DYE, "BLACK_DRAGON_SCALE", "§0Kara Ejder Ölçeği");
        DEATH_SWORD_FRAGMENT = create(Material.IRON_SWORD, "DEATH_SWORD_FRAGMENT", "§8Ölüm Kılıcı Parçası");
        DEATH_DUST = create(Material.GUNPOWDER, "DEATH_DUST", "§8Ölüm Tozu");
        CHAOS_CORE = create(Material.ENDER_PEARL, "CHAOS_CORE", "§5Kaos Çekirdeği");
        CHAOS_SCALE = create(Material.PURPLE_DYE, "CHAOS_SCALE", "§5Kaos Ölçeği");
        HELL_STONE = create(Material.NETHERRACK, "HELL_STONE", "§4Cehennem Taşı");
        HELL_FIRE = create(Material.BLAZE_POWDER, "HELL_FIRE", "§4Cehennem Ateşi");

        // ========== SEVİYE 5 MOB DROP İTEMLERİ ==========
        LEGENDARY_DRAGON_HEART = create(Material.NETHER_STAR, "LEGENDARY_DRAGON_HEART", "§6§lEfsanevi Ejder Kalbi");
        LEGENDARY_DRAGON_SCALE = create(Material.DRAGON_EGG, "LEGENDARY_DRAGON_SCALE", "§6§lEfsanevi Ejder Ölçeği");
        GOD_BLOOD = create(Material.ECHO_SHARD, "GOD_BLOOD", "§d§lTanrı Kanı");
        GOD_FRAGMENT = create(Material.NETHER_STAR, "GOD_FRAGMENT", "§d§lTanrı Parçası");
        VOID_CORE = create(Material.ENDER_EYE, "VOID_CORE", "§5§lHiçlik Çekirdeği");
        VOID_DUST = create(Material.GUNPOWDER, "VOID_DUST", "§5§lHiçlik Tozu");
        TIME_CORE = create(Material.CLOCK, "TIME_CORE", "§b§lZaman Çekirdeği");
        TIME_SCALE = create(Material.ECHO_SHARD, "TIME_SCALE", "§b§lZaman Ölçeği");
        FATE_STONE = create(Material.AMETHYST_SHARD, "FATE_STONE", "§d§lKader Taşı");
        FATE_FRAGMENT = create(Material.ECHO_SHARD, "FATE_FRAGMENT", "§d§lKader Parçası");

        registerRecipes();
    }

    private void registerRecipes() {
        ShapelessRecipe blueprint = new ShapelessRecipe(new NamespacedKey(Main.getInstance(), "craft_blueprint"),
                BLUEPRINT_PAPER);
        blueprint.addIngredient(Material.PAPER);
        blueprint.addIngredient(Material.LAPIS_LAZULI);
        Bukkit.addRecipe(blueprint);

        ShapedRecipe lightning = new ShapedRecipe(new NamespacedKey(Main.getInstance(), "craft_lightning_core"),
                LIGHTNING_CORE);
        lightning.shape("GEG", "EDE", "GEG");
        lightning.setIngredient('G', Material.GOLD_INGOT);
        lightning.setIngredient('E', Material.ENDER_PEARL);
        lightning.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(lightning);

        // Klan Kristali ve Klan Çiti tarifleri
        registerClanCrystalRecipe();
        registerClanFenceRecipe();

        // Tuzak Çekirdeği (TRAP_CORE) tarifi
        registerTrapCoreRecipe();
        
        // Seviyeli silah ve zırh tarifleri
        registerLeveledWeaponsAndArmor();
    }
    
    /**
     * Seviyeli silah ve zırh tariflerini kaydet
     */
    private void registerLeveledWeaponsAndArmor() {
        // Seviye 1: Demir seviyesi
        registerLevel1Recipes();
        // Seviye 2: Elmas seviyesi
        registerLevel2Recipes();
        // Seviye 3: Netherite seviyesi
        registerLevel3Recipes();
        // Seviye 4: Titanyum seviyesi
        registerLevel4Recipes();
        // Seviye 5: Efsanevi seviye
        registerLevel5Recipes();
    }
    
    private void registerLevel1Recipes() {
        // Demir Kılıç: 3 Demir + 2 Çubuk
        ShapedRecipe l1Sword = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level1_sword"),
            createLeveledWeapon(1, WeaponType.SWORD)
        );
        l1Sword.shape(" I ", " I ", " S ");
        l1Sword.setIngredient('I', Material.IRON_INGOT);
        l1Sword.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(l1Sword);
        
        // Demir Zırh seti: Standart demir zırh tarifleri ama özel item olarak
        registerArmorRecipe(1, ArmorType.HELMET, Material.IRON_HELMET);
        registerArmorRecipe(1, ArmorType.CHESTPLATE, Material.IRON_CHESTPLATE);
        registerArmorRecipe(1, ArmorType.LEGGINGS, Material.IRON_LEGGINGS);
        registerArmorRecipe(1, ArmorType.BOOTS, Material.IRON_BOOTS);
    }
    
    private void registerLevel2Recipes() {
        // Elmas Kılıç: 3 Elmas + 2 Çubuk
        ShapedRecipe l2Sword = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level2_sword"),
            createLeveledWeapon(2, WeaponType.SWORD)
        );
        l2Sword.shape(" D ", " D ", " S ");
        l2Sword.setIngredient('D', Material.DIAMOND);
        l2Sword.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(l2Sword);
        
        // Elmas Zırh seti
        registerArmorRecipe(2, ArmorType.HELMET, Material.DIAMOND_HELMET);
        registerArmorRecipe(2, ArmorType.CHESTPLATE, Material.DIAMOND_CHESTPLATE);
        registerArmorRecipe(2, ArmorType.LEGGINGS, Material.DIAMOND_LEGGINGS);
        registerArmorRecipe(2, ArmorType.BOOTS, Material.DIAMOND_BOOTS);
    }
    
    private void registerLevel3Recipes() {
        // Netherite Kılıç: 1 Netherite Külçe + 1 Elmas Kılıç
        ShapedRecipe l3Sword = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level3_sword"),
            createLeveledWeapon(3, WeaponType.SWORD)
        );
        l3Sword.shape("N", "D");
        l3Sword.setIngredient('N', Material.NETHERITE_INGOT);
        l3Sword.setIngredient('D', Material.DIAMOND_SWORD);
        Bukkit.addRecipe(l3Sword);
        
        // Netherite Zırh seti
        registerArmorRecipe(3, ArmorType.HELMET, Material.NETHERITE_HELMET);
        registerArmorRecipe(3, ArmorType.CHESTPLATE, Material.NETHERITE_CHESTPLATE);
        registerArmorRecipe(3, ArmorType.LEGGINGS, Material.NETHERITE_LEGGINGS);
        registerArmorRecipe(3, ArmorType.BOOTS, Material.NETHERITE_BOOTS);
    }
    
    private void registerLevel4Recipes() {
        // Titanyum Kılıç: 3 Titanyum Külçe + 2 Çubuk
        ShapedRecipe l4Sword = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level4_sword"),
            createLeveledWeapon(4, WeaponType.SWORD)
        );
        l4Sword.shape(" T ", " T ", " S ");
        l4Sword.setIngredient('T', TITANIUM_INGOT);
        l4Sword.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(l4Sword);
        
        // Titanyum Zırh seti: Netherite zırh + Titanyum Külçe
        registerArmorUpgradeRecipe(4, ArmorType.HELMET, Material.NETHERITE_HELMET, TITANIUM_INGOT);
        registerArmorUpgradeRecipe(4, ArmorType.CHESTPLATE, Material.NETHERITE_CHESTPLATE, TITANIUM_INGOT);
        registerArmorUpgradeRecipe(4, ArmorType.LEGGINGS, Material.NETHERITE_LEGGINGS, TITANIUM_INGOT);
        registerArmorUpgradeRecipe(4, ArmorType.BOOTS, Material.NETHERITE_BOOTS, TITANIUM_INGOT);
    }
    
    private void registerLevel5Recipes() {
        // Efsanevi Kılıç: 3 Kızıl Elmas + 2 Çubuk
        ShapedRecipe l5Sword = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level5_sword"),
            createLeveledWeapon(5, WeaponType.SWORD)
        );
        l5Sword.shape(" R ", " R ", " S ");
        l5Sword.setIngredient('R', RED_DIAMOND);
        l5Sword.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(l5Sword);
        
        // Efsanevi Zırh seti: Titanyum zırh + Kızıl Elmas
        registerArmorUpgradeRecipe(5, ArmorType.HELMET, Material.NETHERITE_HELMET, RED_DIAMOND);
        registerArmorUpgradeRecipe(5, ArmorType.CHESTPLATE, Material.NETHERITE_CHESTPLATE, RED_DIAMOND);
        registerArmorUpgradeRecipe(5, ArmorType.LEGGINGS, Material.NETHERITE_LEGGINGS, RED_DIAMOND);
        registerArmorUpgradeRecipe(5, ArmorType.BOOTS, Material.NETHERITE_BOOTS, RED_DIAMOND);
    }
    
    private void registerArmorRecipe(int level, ArmorType type, Material baseMaterial) {
        ItemStack armor = createLeveledArmor(level, type);
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level" + level + "_" + type.name().toLowerCase()),
            armor
        );
        
        // Standart zırh tarifleri
        switch (type) {
            case HELMET:
                recipe.shape("MMM", "M M", "   ");
                recipe.setIngredient('M', baseMaterial == Material.IRON_HELMET ? Material.IRON_INGOT :
                                     baseMaterial == Material.DIAMOND_HELMET ? Material.DIAMOND :
                                     Material.NETHERITE_INGOT);
                break;
            case CHESTPLATE:
                recipe.shape("M M", "MMM", "MMM");
                recipe.setIngredient('M', baseMaterial == Material.IRON_CHESTPLATE ? Material.IRON_INGOT :
                                     baseMaterial == Material.DIAMOND_CHESTPLATE ? Material.DIAMOND :
                                     Material.NETHERITE_INGOT);
                break;
            case LEGGINGS:
                recipe.shape("MMM", "M M", "M M");
                recipe.setIngredient('M', baseMaterial == Material.IRON_LEGGINGS ? Material.IRON_INGOT :
                                     baseMaterial == Material.DIAMOND_LEGGINGS ? Material.DIAMOND :
                                     Material.NETHERITE_INGOT);
                break;
            case BOOTS:
                recipe.shape("   ", "M M", "M M");
                recipe.setIngredient('M', baseMaterial == Material.IRON_BOOTS ? Material.IRON_INGOT :
                                     baseMaterial == Material.DIAMOND_BOOTS ? Material.DIAMOND :
                                     Material.NETHERITE_INGOT);
                break;
        }
        
        Bukkit.addRecipe(recipe);
    }
    
    private void registerArmorUpgradeRecipe(int level, ArmorType type, Material baseArmor, ItemStack upgradeMaterial) {
        ItemStack armor = createLeveledArmor(level, type);
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "level" + level + "_" + type.name().toLowerCase() + "_upgrade"),
            armor
        );
        
        recipe.shape("U", "A");
        recipe.setIngredient('U', upgradeMaterial);
        recipe.setIngredient('A', baseArmor);
        
        Bukkit.addRecipe(recipe);
    }

    private void registerTrapCoreRecipe() {
        // Tuzak Çekirdeği: 4 Obsidyen + 1 Ender İncisi + 4 Demir
        ShapedRecipe trapCoreRecipe = new ShapedRecipe(
                new NamespacedKey(Main.getInstance(), "trap_core"),
                TRAP_CORE);
        trapCoreRecipe.shape("OEO", "IDI", "OEO");
        trapCoreRecipe.setIngredient('O', Material.OBSIDIAN); // Obsidyen
        trapCoreRecipe.setIngredient('E', Material.ENDER_PEARL); // Ender İncisi
        trapCoreRecipe.setIngredient('I', Material.IRON_INGOT); // Demir
        trapCoreRecipe.setIngredient('D', Material.DIAMOND); // Elmas (ortada)
        Bukkit.addRecipe(trapCoreRecipe);
    }

    private void registerClanCrystalRecipe() {
        // Klan Kristali (End Crystal görünümünde)
        ItemStack crystal = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = crystal.getItemMeta();
        meta.setDisplayName("§b§lKlan Kristali");
        List<String> lore = new ArrayList<>();
        lore.add("§7Klan kurmak için kullanılır.");
        lore.add("§7Etrafı Klan Çiti ile çevrili");
        lore.add("§7bir alana koyulmalıdır.");
        meta.setLore(lore);

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "clan_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "CRYSTAL");
        crystal.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Main.getInstance(), "clan_crystal"), crystal);
        // Tarif: Boş - Elmas Blok - Boş
        // Elmas Blok - Ender İncisi - Elmas Blok
        // Boş - Obsidyen - Boş
        recipe.shape(" B ", "BEB", " O ");
        recipe.setIngredient('B', Material.DIAMOND_BLOCK); // Elmas Blok
        recipe.setIngredient('E', Material.ENDER_PEARL); // Ender İncisi
        recipe.setIngredient('O', Material.OBSIDIAN); // Obsidyen

        Bukkit.addRecipe(recipe);
    }

    private void registerClanFenceRecipe() {
        // Klan Çiti (Normal çit ama ortası demir)
        ItemStack fence = new ItemStack(Material.OAK_FENCE);
        ItemMeta meta = fence.getItemMeta();
        meta.setDisplayName("§6§lKlan Çiti");
        List<String> lore = new ArrayList<>();
        lore.add("§7Klan bölgesi sınırlarını belirler.");
        meta.setLore(lore);

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "clan_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "FENCE");
        fence.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(Main.getInstance(), "clan_fence"), fence);
        // Tarif: Tahta - Demir - Tahta (2 satır)
        recipe.shape("WIW", "WIW");
        recipe.setIngredient('W', Material.OAK_PLANKS);
        recipe.setIngredient('I', Material.IRON_INGOT);

        Bukkit.addRecipe(recipe);
    }

    private ItemStack create(Material mat, String id, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add("§7Stratocraft Özel Eşyası");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "custom_id"),
                PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Tarif kitabı oluştur (geliştirilmiş açıklamalarla)
     */
    private ItemStack createRecipeBook(String id, String name) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        
        // Tarif türüne göre açıklama ekle
        String recipeId = id.replace("RECIPE_", "").toUpperCase();
        RecipeInfo info = getRecipeInfo(recipeId);
        
        lore.add("§6═══════════════════════");
        lore.add("§e§l" + info.getDisplayName());
        lore.add("§6═══════════════════════");
        lore.add("");
        lore.add("§7§l📍 Yerleşim:");
        lore.add("§7" + info.getLocationInfo());
        lore.add("");
        lore.add("§7§l⚙️ İşlev:");
        lore.add("§7" + info.getFunctionInfo());
        lore.add("");
        
        // Eğer item tarifi ise crafting bilgisi ekle
        if (info.isItemRecipe()) {
            lore.add("§7§l🔨 Yapılış:");
            lore.add("§7Crafting masasında yapılır.");
            lore.add("§7Tarif detayları için kitaba");
            lore.add("§7Shift+Sağ tıklayın.");
        } else {
            lore.add("§7§l📖 Kullanım:");
            lore.add("§7Sağ tık: Hayalet yapı göster");
            lore.add("§7Shift+Sağ tık: Tarifi sabitle");
            lore.add("§7Shift+Sol tık: Tarifi kaldır");
        }
        
        lore.add("");
        lore.add("§8Tarif Kitabı");
        
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(Main.getInstance(), "custom_id"),
                PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Tarif bilgilerini döndür
     */
    private RecipeInfo getRecipeInfo(String recipeId) {
        // Yapılar
        switch (recipeId) {
            case "CORE":
                return new RecipeInfo("Ana Kristal", "§cSadece klan bölgesi içinde", "Klan merkezi ve offline koruma sağlar. Kırılırsa klan dağılır!");
            case "ALCHEMY_TOWER":
            case "ALCHEMY":
                return new RecipeInfo("Simya Kulesi", "§cSadece klan bölgesi içinde", "Bataryaların gücünü %10-75 arası artırır (seviyeye göre).");
            case "POISON_REACTOR":
                return new RecipeInfo("Zehir Reaktörü", "§cSadece klan bölgesi içinde", "Bölgeye giren düşmanlara sürekli zehir verir (30 blok menzil).");
            case "TECTONIC":
            case "TECTONIC_STABILIZER":
                return new RecipeInfo("Tektonik Sabitleyici", "§cSadece klan bölgesi içinde", "Felaket hasarını %50-99 arası azaltır (seviyeye göre).");
            case "SIEGE_FACTORY":
                return new RecipeInfo("Kuşatma Fabrikası", "§cSadece klan bölgesi içinde", "Mancınık ve Balista üretir (seviyeye göre hız artar).");
            case "WALL_GENERATOR":
                return new RecipeInfo("Sur Jeneratörü", "§cSadece klan bölgesi içinde", "Otomatik sur blokları oluşturur.");
            case "GRAVITY_WELL":
                return new RecipeInfo("Yerçekimi Kuyusu", "§cSadece klan bölgesi içinde", "Düşmanları yavaşlatır ve çeker.");
            case "LAVA_TRENCHER":
                return new RecipeInfo("Lav Hendekçisi", "§cSadece klan bölgesi içinde", "Lav hendekleri oluşturur.");
            case "WATCHTOWER":
                return new RecipeInfo("Gözetleme Kulesi", "§cSadece klan bölgesi içinde", "Alarm sistemi - düşmanları tespit eder ve uyarır.");
            case "DRONE_STATION":
                return new RecipeInfo("Drone İstasyonu", "§cSadece klan bölgesi içinde", "Otomatik drone üretir.");
            case "AUTO_TURRET":
                return new RecipeInfo("Otomatik Taret", "§cSadece klan bölgesi içinde", "Otonom ok savunması (20 blok menzil).");
            case "GLOBAL_MARKET_GATE":
                return new RecipeInfo("Global Pazar Kapısı", "§cSadece klan bölgesi içinde", "Klanlar arası ticaret platformu.");
            case "AUTO_DRILL":
                return new RecipeInfo("Otomatik Madenci", "§cSadece klan bölgesi içinde", "Otomatik maden çıkarır.");
            case "XP_BANK":
                return new RecipeInfo("Tecrübe Bankası", "§cSadece klan bölgesi içinde", "XP depolama ve paylaşım.");
            case "MAG_RAIL":
                return new RecipeInfo("Manyetik Ray", "§cSadece klan bölgesi içinde", "Hızlı ulaşım rayı.");
            case "TELEPORTER":
                return new RecipeInfo("Işınlanma Platformu", "§cSadece klan bölgesi içinde", "Klan içi ışınlanma.");
            case "FOOD_SILO":
                return new RecipeInfo("Buzdolabı", "§cSadece klan bölgesi içinde", "Yiyecek depolama.");
            case "OIL_REFINERY":
                return new RecipeInfo("Petrol Rafinerisi", "§cSadece klan bölgesi içinde", "Yakıt üretimi.");
            case "HEALING_BEACON":
                return new RecipeInfo("Şifa Kulesi", "§aKlan bölgesi veya dışarıda", "Sürekli regen efekti verir.");
            case "WEATHER_MACHINE":
                return new RecipeInfo("Hava Kontrolcüsü", "§cSadece klan bölgesi içinde", "Hava durumunu kontrol eder.");
            case "CROP_ACCELERATOR":
                return new RecipeInfo("Tarım Hızlandırıcı", "§cSadece klan bölgesi içinde", "Ekinleri hızlandırır.");
            case "MOB_GRINDER":
                return new RecipeInfo("Mob Öğütücü", "§cSadece klan bölgesi içinde", "Mobları otomatik öğütür.");
            case "INVISIBILITY_CLOAK":
                return new RecipeInfo("Görünmezlik Perdesi", "§cSadece klan bölgesi içinde", "Klan üyelerini görünmez yapar.");
            case "ARMORY":
                return new RecipeInfo("Cephanelik", "§cSadece klan bölgesi içinde", "Ekipman depolama.");
            case "LIBRARY":
                return new RecipeInfo("Kütüphane", "§cSadece klan bölgesi içinde", "Tarif kitabı depolama.");
            case "WARNING_SIGN":
                return new RecipeInfo("Yasaklı Bölge Tabelası", "§aKlan bölgesi veya dışarıda", "Yasaklı bölge işareti.");
            
            // Özel Eşyalar
            case "LIGHTNING_CORE":
                return new RecipeInfo("Yıldırım Çekirdeği", "§7Crafting masasında", "Batarya yakıtı - güçlü yıldırım efekti.", true);
            case "TITANIUM_INGOT":
                return new RecipeInfo("Titanyum Külçesi", "§7Crafting masasında", "Güçlü zırh ve silah malzemesi.", true);
            case "DARK_MATTER":
                return new RecipeInfo("Karanlık Madde", "§7Crafting masasında", "Efsanevi eşya malzemesi.", true);
            case "RED_DIAMOND":
                return new RecipeInfo("Kızıl Elmas", "§7Crafting masasında", "En güçlü silahlar için malzeme.", true);
            case "RUBY":
                return new RecipeInfo("Yakut", "§7Crafting masasında", "Değerli mücevher.", true);
            case "ADAMANTITE":
                return new RecipeInfo("Adamantite", "§7Crafting masasında", "Efsanevi zırh malzemesi.", true);
            case "STAR_CORE":
                return new RecipeInfo("Yıldız Çekirdeği", "§7Crafting masasında", "Güçlü eşya malzemesi.", true);
            case "FLAME_AMPLIFIER":
                return new RecipeInfo("Alev Amplifikatörü", "§7Crafting masasında", "Ateş bataryası güçlendirici.", true);
            case "DEVIL_HORN":
                return new RecipeInfo("Şeytan Boynuzu", "§7Crafting masasında", "Özel eşya malzemesi.", true);
            case "DEVIL_SNAKE_EYE":
                return new RecipeInfo("İblis Yılanın Gözü", "§7Crafting masasında", "Özel eşya malzemesi.", true);
            case "WAR_FAN":
                return new RecipeInfo("Savaş Yelpazesi", "§7Crafting masasında", "Özel silah.", true);
            case "TOWER_SHIELD":
                return new RecipeInfo("Kule Kalkanı", "§7Crafting masasında", "Güçlü kalkan.", true);
            case "HELL_FRUIT":
                return new RecipeInfo("Cehennem Meyvesi", "§7Crafting masasında", "Özel tüketilebilir.", true);
            case "SULFUR":
                return new RecipeInfo("Kükürt", "§7Crafting masasında", "Yakıt ve patlayıcı malzemesi.", true);
            case "BAUXITE_INGOT":
                return new RecipeInfo("Boksit Külçesi", "§7Crafting masasında", "Orta seviye malzeme.", true);
            case "ROCK_SALT":
                return new RecipeInfo("Tuz", "§7Crafting masasında", "Temel malzeme.", true);
            case "MITHRIL_INGOT":
                return new RecipeInfo("Mithril Külçesi", "§7Crafting masasında", "Güçlü zırh malzemesi.", true);
            case "MITHRIL_STRING":
                return new RecipeInfo("Mithril İpi", "§7Crafting masasında", "Güçlü ip malzemesi.", true);
            case "ASTRAL_CRYSTAL":
                return new RecipeInfo("Astral Kristali", "§7Crafting masasında", "İleri seviye malzeme.", true);
            case "RUSTY_HOOK":
                return new RecipeInfo("Paslı Kanca", "§7Crafting masasında", "7 blok menzilli kanca.", true);
            case "GOLDEN_HOOK":
                return new RecipeInfo("Altın Kanca", "§7Crafting masasında", "15 blok menzilli kanca.", true);
            case "TITAN_GRAPPLE":
                return new RecipeInfo("Titan Kancası", "§7Crafting masasında", "40 blok menzilli kanca + Slow Falling.", true);
            case "TRAP_CORE":
                return new RecipeInfo("Tuzak Çekirdeği", "§7Crafting masasında", "Tuzak kurmak için çekirdek.", true);
            
            default:
                return new RecipeInfo("Bilinmeyen Tarif", "§7Bilinmeyen", "Açıklama yok.");
        }
    }
    
    /**
     * Tarif bilgisi sınıfı
     */
    private static class RecipeInfo {
        private final String displayName;
        private final String locationInfo;
        private final String functionInfo;
        private final boolean isItemRecipe;
        
        public RecipeInfo(String displayName, String locationInfo, String functionInfo) {
            this(displayName, locationInfo, functionInfo, false);
        }
        
        public RecipeInfo(String displayName, String locationInfo, String functionInfo, boolean isItemRecipe) {
            this.displayName = displayName;
            this.locationInfo = locationInfo;
            this.functionInfo = functionInfo;
            this.isItemRecipe = isItemRecipe;
        }
        
        public String getDisplayName() { return displayName; }
        public String getLocationInfo() { return locationInfo; }
        public String getFunctionInfo() { return functionInfo; }
        public boolean isItemRecipe() { return isItemRecipe; }
    }

    public static boolean isCustomItem(ItemStack item, String id) {
        if (item == null || item.getItemMeta() == null)
            return false;
        String data = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(Main.getInstance(), "custom_id"), PersistentDataType.STRING);
        return id != null && id.equals(data);
    }

    /**
     * Bir eşyanın Klan Kristali veya Klan Çiti olup olmadığını kontrol eder
     */
    public static boolean isClanItem(ItemStack item, String type) {
        if (item == null || !item.hasItemMeta())
            return false;
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "clan_item");
        String data = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return data != null && data.equals(type);
    }

    // ========== SEVİYELİ SİLAH VE ZIRH SİSTEMİ ==========
    
    /**
     * Seviyeye göre silah oluştur
     * @param level Seviye (1-5)
     * @param weaponType Silah tipi (SWORD, AXE, BOW)
     * @return Oluşturulan silah
     */
    public static ItemStack createLeveledWeapon(int level, WeaponType weaponType) {
        Material baseMaterial;
        String name;
        double baseDamage;
        String color;
        
        switch (level) {
            case 1:
                baseMaterial = Material.IRON_SWORD;
                color = "§f";
                baseDamage = 12.0; // Demir seviyesi
                name = "Demir Kılıç";
                break;
            case 2:
                baseMaterial = Material.DIAMOND_SWORD;
                color = "§b";
                baseDamage = 20.0; // Elmas seviyesi
                name = "Elmas Kılıç";
                break;
            case 3:
                baseMaterial = Material.NETHERITE_SWORD;
                color = "§5";
                baseDamage = 32.0; // Netherite seviyesi
                name = "Netherite Kılıç";
                break;
            case 4:
                baseMaterial = Material.NETHERITE_SWORD;
                color = "§6";
                baseDamage = 50.0; // Özel seviye
                name = "Titanyum Kılıç";
                break;
            case 5:
                baseMaterial = Material.NETHERITE_SWORD;
                color = "§d§l";
                baseDamage = 80.0; // Efsanevi seviye
                name = "Efsanevi Kılıç";
                break;
            default:
                baseMaterial = Material.IRON_SWORD;
                color = "§7";
                baseDamage = 8.0;
                name = "Temel Kılıç";
        }
        
        // WeaponType'a göre material değiştir
        if (weaponType == WeaponType.AXE) {
            switch (level) {
                case 1: baseMaterial = Material.IRON_AXE; name = "Demir Balta"; break;
                case 2: baseMaterial = Material.DIAMOND_AXE; name = "Elmas Balta"; break;
                case 3: baseMaterial = Material.NETHERITE_AXE; name = "Netherite Balta"; break;
                case 4: baseMaterial = Material.NETHERITE_AXE; name = "Titanyum Balta"; break;
                case 5: baseMaterial = Material.NETHERITE_AXE; name = "Efsanevi Balta"; break;
            }
        } else if (weaponType == WeaponType.BOW) {
            baseMaterial = Material.BOW;
            switch (level) {
                case 1: name = "Demir Yay"; break;
                case 2: name = "Elmas Yay"; break;
                case 3: name = "Netherite Yay"; break;
                case 4: name = "Titanyum Yay"; break;
                case 5: name = "Efsanevi Yay"; break;
            }
        }
        
        ItemStack weapon = new ItemStack(baseMaterial);
        ItemMeta meta = weapon.getItemMeta();
        meta.setDisplayName(color + "§l" + name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Seviye: §e" + level);
        lore.add("§7Hasar: §c" + String.format("%.1f", baseDamage));
        lore.add("");
        lore.add("§7Stratocraft Özel Silahı");
        meta.setLore(lore);
        
        // Hasar modifier ekle
        if (weaponType != WeaponType.BOW) {
            AttributeModifier damageMod = new AttributeModifier(
                UUID.randomUUID(),
                "stratocraft_weapon_damage",
                baseDamage - 1.0, // Minecraft'ın base hasarı 1.0
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageMod);
        }
        
        // Seviye bilgisini kaydet
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "weapon_level"),
            PersistentDataType.INTEGER,
            level
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "custom_id"),
            PersistentDataType.STRING,
            "LEVELED_WEAPON_" + level
        );
        
        weapon.setItemMeta(meta);
        return weapon;
    }
    
    /**
     * Seviyeye göre zırh oluştur
     * @param level Seviye (1-5)
     * @param armorType Zırh tipi (HELMET, CHESTPLATE, LEGGINGS, BOOTS)
     * @return Oluşturulan zırh
     */
    public static ItemStack createLeveledArmor(int level, ArmorType armorType) {
        Material baseMaterial;
        String name;
        double armorPoints;
        String color;
        
        switch (level) {
            case 1:
                color = "§f";
                armorPoints = 6.0; // Demir seviyesi
                name = "Demir";
                break;
            case 2:
                color = "§b";
                armorPoints = 10.0; // Elmas seviyesi
                name = "Elmas";
                break;
            case 3:
                color = "§5";
                armorPoints = 15.0; // Netherite seviyesi
                name = "Netherite";
                break;
            case 4:
                color = "§6";
                armorPoints = 22.0; // Özel seviye
                name = "Titanyum";
                break;
            case 5:
                color = "§d§l";
                armorPoints = 30.0; // Efsanevi seviye
                name = "Efsanevi";
                break;
            default:
                color = "§7";
                armorPoints = 3.0;
                name = "Temel";
        }
        
        // ArmorType'a göre material ve isim belirle
        switch (armorType) {
            case HELMET:
                switch (level) {
                    case 1: baseMaterial = Material.IRON_HELMET; name += " Miğfer"; break;
                    case 2: baseMaterial = Material.DIAMOND_HELMET; name += " Miğfer"; break;
                    case 3: baseMaterial = Material.NETHERITE_HELMET; name += " Miğfer"; break;
                    case 4: baseMaterial = Material.NETHERITE_HELMET; name += " Miğfer"; break;
                    case 5: baseMaterial = Material.NETHERITE_HELMET; name += " Miğfer"; break;
                    default: baseMaterial = Material.IRON_HELMET; name += " Miğfer"; break;
                }
                armorPoints *= 0.25; // Miğfer = %25
                break;
            case CHESTPLATE:
                switch (level) {
                    case 1: baseMaterial = Material.IRON_CHESTPLATE; name += " Göğüslük"; break;
                    case 2: baseMaterial = Material.DIAMOND_CHESTPLATE; name += " Göğüslük"; break;
                    case 3: baseMaterial = Material.NETHERITE_CHESTPLATE; name += " Göğüslük"; break;
                    case 4: baseMaterial = Material.NETHERITE_CHESTPLATE; name += " Göğüslük"; break;
                    case 5: baseMaterial = Material.NETHERITE_CHESTPLATE; name += " Göğüslük"; break;
                    default: baseMaterial = Material.IRON_CHESTPLATE; name += " Göğüslük"; break;
                }
                armorPoints *= 0.4; // Göğüslük = %40
                break;
            case LEGGINGS:
                switch (level) {
                    case 1: baseMaterial = Material.IRON_LEGGINGS; name += " Pantolon"; break;
                    case 2: baseMaterial = Material.DIAMOND_LEGGINGS; name += " Pantolon"; break;
                    case 3: baseMaterial = Material.NETHERITE_LEGGINGS; name += " Pantolon"; break;
                    case 4: baseMaterial = Material.NETHERITE_LEGGINGS; name += " Pantolon"; break;
                    case 5: baseMaterial = Material.NETHERITE_LEGGINGS; name += " Pantolon"; break;
                    default: baseMaterial = Material.IRON_LEGGINGS; name += " Pantolon"; break;
                }
                armorPoints *= 0.3; // Pantolon = %30
                break;
            case BOOTS:
                switch (level) {
                    case 1: baseMaterial = Material.IRON_BOOTS; name += " Bot"; break;
                    case 2: baseMaterial = Material.DIAMOND_BOOTS; name += " Bot"; break;
                    case 3: baseMaterial = Material.NETHERITE_BOOTS; name += " Bot"; break;
                    case 4: baseMaterial = Material.NETHERITE_BOOTS; name += " Bot"; break;
                    case 5: baseMaterial = Material.NETHERITE_BOOTS; name += " Bot"; break;
                    default: baseMaterial = Material.IRON_BOOTS; name += " Bot"; break;
                }
                armorPoints *= 0.15; // Bot = %15
                break;
            default:
                baseMaterial = Material.IRON_HELMET;
                name += " Miğfer";
        }
        
        ItemStack armor = new ItemStack(baseMaterial);
        ItemMeta meta = armor.getItemMeta();
        meta.setDisplayName(color + "§l" + name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Seviye: §e" + level);
        lore.add("§7Zırh: §a" + String.format("%.1f", armorPoints));
        lore.add("");
        lore.add("§7Stratocraft Özel Zırhı");
        meta.setLore(lore);
        
        // Zırh modifier ekle
        EquipmentSlot slot = armorType == ArmorType.HELMET ? EquipmentSlot.HEAD :
                            armorType == ArmorType.CHESTPLATE ? EquipmentSlot.CHEST :
                            armorType == ArmorType.LEGGINGS ? EquipmentSlot.LEGS :
                            EquipmentSlot.FEET;
        
        AttributeModifier armorMod = new AttributeModifier(
            UUID.randomUUID(),
            "stratocraft_armor",
            armorPoints,
            AttributeModifier.Operation.ADD_NUMBER,
            slot
        );
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, armorMod);
        
        // Seviye bilgisini kaydet
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "armor_level"),
            PersistentDataType.INTEGER,
            level
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "custom_id"),
            PersistentDataType.STRING,
            "LEVELED_ARMOR_" + level
        );
        
        armor.setItemMeta(meta);
        return armor;
    }
    
    /**
     * Bir eşyanın seviyeli silah olup olmadığını kontrol et
     */
    public static boolean isLeveledWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(new NamespacedKey(Main.getInstance(), "weapon_level"), PersistentDataType.INTEGER);
    }
    
    /**
     * Bir eşyanın seviyeli zırh olup olmadığını kontrol et
     */
    public static boolean isLeveledArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(new NamespacedKey(Main.getInstance(), "armor_level"), PersistentDataType.INTEGER);
    }
    
    /**
     * Silah seviyesini al
     */
    public static int getWeaponLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer level = item.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey(Main.getInstance(), "weapon_level"), PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }
    
    /**
     * Zırh seviyesini al
     */
    public static int getArmorLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Integer level = item.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey(Main.getInstance(), "armor_level"), PersistentDataType.INTEGER);
        return level != null ? level : 0;
    }
    
    /**
     * Silah tipi enum
     */
    public enum WeaponType {
        SWORD, AXE, BOW
    }
    
    /**
     * Zırh tipi enum
     */
    public enum ArmorType {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS
    }
}
