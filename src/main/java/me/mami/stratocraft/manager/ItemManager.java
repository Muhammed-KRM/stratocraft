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
import java.util.Arrays;
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
    // Güçlü Yiyecekler
    public static ItemStack LIFE_ELIXIR; // Canı fulleyen
    public static ItemStack POWER_FRUIT; // Hasarı 5 kat arttıran
    public static ItemStack SPEED_ELIXIR; // Hızı arttıran
    public static ItemStack REGENERATION_ELIXIR; // Hızlı can yenileme
    public static ItemStack STRENGTH_ELIXIR; // Güç artışı

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
    public static ItemStack RECIPE_BLUEPRINT_PAPER;
    public static ItemStack RECIPE_LIGHTNING_CORE;
    public static ItemStack RECIPE_TITANIUM_ORE;
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
    public static ItemStack RECIPE_LIFE_ELIXIR;
    public static ItemStack RECIPE_POWER_FRUIT;
    public static ItemStack RECIPE_SPEED_ELIXIR;
    public static ItemStack RECIPE_REGENERATION_ELIXIR;
    public static ItemStack RECIPE_STRENGTH_ELIXIR;
    public static ItemStack RECIPE_SULFUR_ORE;
    public static ItemStack RECIPE_SULFUR;
    public static ItemStack RECIPE_BAUXITE_ORE;
    public static ItemStack RECIPE_BAUXITE_INGOT;
    public static ItemStack RECIPE_ROCK_SALT_ORE;
    public static ItemStack RECIPE_ROCK_SALT;
    public static ItemStack RECIPE_MITHRIL_ORE;
    public static ItemStack RECIPE_MITHRIL_INGOT;
    public static ItemStack RECIPE_MITHRIL_STRING;
    public static ItemStack RECIPE_ASTRAL_ORE;
    public static ItemStack RECIPE_ASTRAL_CRYSTAL;
    public static ItemStack RECIPE_RUSTY_HOOK;
    public static ItemStack RECIPE_GOLDEN_HOOK;
    public static ItemStack RECIPE_TITAN_GRAPPLE;
    public static ItemStack RECIPE_TRAP_CORE;
    public static ItemStack RECIPE_TAMING_CORE;
    public static ItemStack RECIPE_SUMMON_CORE;
    public static ItemStack RECIPE_BREEDING_CORE;
    public static ItemStack RECIPE_GENDER_SCANNER;

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
    public static ItemStack TAMING_CORE; // Eğitim Çekirdeği
    public static ItemStack SUMMON_CORE; // Çağırma Çekirdeği (Boss çağırma için)
    public static ItemStack BREEDING_CORE; // Üreme Çekirdeği
    public static ItemStack GENDER_SCANNER; // Cinsiyet Ayırıcı

    // ========== ÖZEL ZIRHLAR (5 Seviye x 5 Zırh = 25 Zırh) ==========
    // Seviye 1 Zırhlar
    public static ItemStack ARMOR_L1_1; // Demir Savaşçı Zırhı
    public static ItemStack ARMOR_L1_2; // Demir Koruyucu Zırhı
    public static ItemStack ARMOR_L1_3; // Demir Avcı Zırhı
    public static ItemStack ARMOR_L1_4; // Demir Kaşif Zırhı
    public static ItemStack ARMOR_L1_5; // Demir Şövalye Zırhı
    
    // Seviye 2 Zırhlar (Diken Etkisi)
    public static ItemStack ARMOR_L2_1; // Elmas Diken Zırhı
    public static ItemStack ARMOR_L2_2; // Elmas Zehir Diken Zırhı
    public static ItemStack ARMOR_L2_3; // Elmas Ateş Diken Zırhı
    public static ItemStack ARMOR_L2_4; // Elmas Buz Diken Zırhı
    public static ItemStack ARMOR_L2_5; // Elmas Yıldırım Diken Zırhı
    
    // Seviye 3 Zırhlar (2x Hız, Yüksek Zıplama, Aşırı Koruma)
    public static ItemStack ARMOR_L3_1; // Netherite Hız Zırhı
    public static ItemStack ARMOR_L3_2; // Netherite Zıplama Zırhı
    public static ItemStack ARMOR_L3_3; // Netherite Savunma Zırhı
    public static ItemStack ARMOR_L3_4; // Netherite Savaşçı Zırhı
    public static ItemStack ARMOR_L3_5; // Netherite Efsane Zırhı
    
    // Seviye 4 Zırhlar (Sürekli Can Yenileme)
    public static ItemStack ARMOR_L4_1; // Titanyum Yaşam Zırhı
    public static ItemStack ARMOR_L4_2; // Titanyum Ölümsüzlük Zırhı
    public static ItemStack ARMOR_L4_3; // Titanyum Yenilenme Zırhı
    public static ItemStack ARMOR_L4_4; // Titanyum Kutsal Zırhı
    public static ItemStack ARMOR_L4_5; // Titanyum Ebedi Zırhı
    
    // Seviye 5 Zırhlar (Uçma Gücü)
    public static ItemStack ARMOR_L5_1; // Efsanevi Uçan Zırhı
    public static ItemStack ARMOR_L5_2; // Efsanevi Gökyüzü Zırhı
    public static ItemStack ARMOR_L5_3; // Efsanevi Bulut Zırhı
    public static ItemStack ARMOR_L5_4; // Efsanevi Yıldız Zırhı
    public static ItemStack ARMOR_L5_5; // Efsanevi Tanrı Zırhı

    // ========== ÖZEL SİLAHLAR (5 Seviye x 5 Silah = 25 Silah) ==========
    // Seviye 1 Silahlar
    public static ItemStack WEAPON_L1_1; // Demir Kılıç
    public static ItemStack WEAPON_L1_2; // Demir Balta
    public static ItemStack WEAPON_L1_3; // Demir Mızrak
    public static ItemStack WEAPON_L1_4; // Demir Yay
    public static ItemStack WEAPON_L1_5; // Demir Çekiç
    
    // Seviye 2 Silahlar
    public static ItemStack WEAPON_L2_1; // Elmas Kılıç
    public static ItemStack WEAPON_L2_2; // Elmas Balta
    public static ItemStack WEAPON_L2_3; // Elmas Mızrak
    public static ItemStack WEAPON_L2_4; // Elmas Yay
    public static ItemStack WEAPON_L2_5; // Elmas Çekiç
    
    // Seviye 3 Silahlar (Patlama Atabilme - 20 blok menzil)
    public static ItemStack WEAPON_L3_1; // Netherite Patlama Kılıcı
    public static ItemStack WEAPON_L3_2; // Netherite Patlama Baltası
    public static ItemStack WEAPON_L3_3; // Netherite Patlama Mızrağı
    public static ItemStack WEAPON_L3_4; // Netherite Patlama Yayı
    public static ItemStack WEAPON_L3_5; // Netherite Patlama Çekici
    
    // Seviye 4 Silahlar (Devamlı Lazer - Yüksek Hasar)
    public static ItemStack WEAPON_L4_1; // Titanyum Lazer Kılıcı
    public static ItemStack WEAPON_L4_2; // Titanyum Lazer Baltası
    public static ItemStack WEAPON_L4_3; // Titanyum Lazer Mızrağı
    public static ItemStack WEAPON_L4_4; // Titanyum Lazer Yayı
    public static ItemStack WEAPON_L4_5; // Titanyum Lazer Çekici
    
    // Seviye 5 Silahlar (Çok Modlu: Blok Fırlatma, Duvar Yapma, Atılma/Patlama)
    public static ItemStack WEAPON_L5_1; // Efsanevi Çok Modlu Kılıç
    public static ItemStack WEAPON_L5_2; // Efsanevi Çok Modlu Balta
    public static ItemStack WEAPON_L5_3; // Efsanevi Çok Modlu Mızrak
    public static ItemStack WEAPON_L5_4; // Efsanevi Çok Modlu Yay
    public static ItemStack WEAPON_L5_5; // Efsanevi Çok Modlu Çekiç

    // ========== BOSS İTEMLERİ ==========
    public static ItemStack GOBLIN_CROWN;
    public static ItemStack ORC_AMULET;
    public static ItemStack TROLL_HEART;
    public static ItemStack DRAGON_SCALE;
    public static ItemStack TREX_TOOTH;
    public static ItemStack CYCLOPS_EYE;
    public static ItemStack TITAN_CORE;
    public static ItemStack PHOENIX_FEATHER;
    public static ItemStack KRAKEN_TENTACLE;
    public static ItemStack DEMON_LORD_HORN;
    public static ItemStack VOID_DRAGON_HEART;

    // ========== ÖZEL ZIRH VE SİLAH TARİF KİTAPLARI ==========
    // Seviye 1 Zırh Tarifleri
    public static ItemStack RECIPE_ARMOR_L1_1;
    public static ItemStack RECIPE_ARMOR_L1_2;
    public static ItemStack RECIPE_ARMOR_L1_3;
    public static ItemStack RECIPE_ARMOR_L1_4;
    public static ItemStack RECIPE_ARMOR_L1_5;
    
    // Seviye 2 Zırh Tarifleri
    public static ItemStack RECIPE_ARMOR_L2_1;
    public static ItemStack RECIPE_ARMOR_L2_2;
    public static ItemStack RECIPE_ARMOR_L2_3;
    public static ItemStack RECIPE_ARMOR_L2_4;
    public static ItemStack RECIPE_ARMOR_L2_5;
    
    // Seviye 3 Zırh Tarifleri
    public static ItemStack RECIPE_ARMOR_L3_1;
    public static ItemStack RECIPE_ARMOR_L3_2;
    public static ItemStack RECIPE_ARMOR_L3_3;
    public static ItemStack RECIPE_ARMOR_L3_4;
    public static ItemStack RECIPE_ARMOR_L3_5;
    
    // Seviye 4 Zırh Tarifleri
    public static ItemStack RECIPE_ARMOR_L4_1;
    public static ItemStack RECIPE_ARMOR_L4_2;
    public static ItemStack RECIPE_ARMOR_L4_3;
    public static ItemStack RECIPE_ARMOR_L4_4;
    public static ItemStack RECIPE_ARMOR_L4_5;
    
    // Seviye 5 Zırh Tarifleri
    public static ItemStack RECIPE_ARMOR_L5_1;
    public static ItemStack RECIPE_ARMOR_L5_2;
    public static ItemStack RECIPE_ARMOR_L5_3;
    public static ItemStack RECIPE_ARMOR_L5_4;
    public static ItemStack RECIPE_ARMOR_L5_5;
    
    // Seviye 1 Silah Tarifleri
    public static ItemStack RECIPE_WEAPON_L1_1;
    public static ItemStack RECIPE_WEAPON_L1_2;
    public static ItemStack RECIPE_WEAPON_L1_3;
    public static ItemStack RECIPE_WEAPON_L1_4;
    public static ItemStack RECIPE_WEAPON_L1_5;
    
    // Seviye 2 Silah Tarifleri
    public static ItemStack RECIPE_WEAPON_L2_1;
    public static ItemStack RECIPE_WEAPON_L2_2;
    public static ItemStack RECIPE_WEAPON_L2_3;
    public static ItemStack RECIPE_WEAPON_L2_4;
    public static ItemStack RECIPE_WEAPON_L2_5;
    
    // Seviye 3 Silah Tarifleri
    public static ItemStack RECIPE_WEAPON_L3_1;
    public static ItemStack RECIPE_WEAPON_L3_2;
    public static ItemStack RECIPE_WEAPON_L3_3;
    public static ItemStack RECIPE_WEAPON_L3_4;
    public static ItemStack RECIPE_WEAPON_L3_5;
    
    // Seviye 4 Silah Tarifleri
    public static ItemStack RECIPE_WEAPON_L4_1;
    public static ItemStack RECIPE_WEAPON_L4_2;
    public static ItemStack RECIPE_WEAPON_L4_3;
    public static ItemStack RECIPE_WEAPON_L4_4;
    public static ItemStack RECIPE_WEAPON_L4_5;
    
    // Seviye 5 Silah Tarifleri
    public static ItemStack RECIPE_WEAPON_L5_1;
    public static ItemStack RECIPE_WEAPON_L5_2;
    public static ItemStack RECIPE_WEAPON_L5_3;
    public static ItemStack RECIPE_WEAPON_L5_4;
    public static ItemStack RECIPE_WEAPON_L5_5;

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
        // Güçlü Yiyecekler
        LIFE_ELIXIR = create(Material.GOLDEN_APPLE, "LIFE_ELIXIR", "§a§lYaşam İksiri");
        POWER_FRUIT = create(Material.ENCHANTED_GOLDEN_APPLE, "POWER_FRUIT", "§c§lGüç Meyvesi");
        SPEED_ELIXIR = create(Material.SUGAR, "SPEED_ELIXIR", "§b§lHız İksiri");
        REGENERATION_ELIXIR = create(Material.GLISTERING_MELON_SLICE, "REGENERATION_ELIXIR", "§d§lYenilenme İksiri");
        STRENGTH_ELIXIR = create(Material.BLAZE_POWDER, "STRENGTH_ELIXIR", "§6§lGüç İksiri");
        // Güçlü Yiyecekler
        LIFE_ELIXIR = create(Material.GOLDEN_APPLE, "LIFE_ELIXIR", "§a§lYaşam İksiri");
        POWER_FRUIT = create(Material.ENCHANTED_GOLDEN_APPLE, "POWER_FRUIT", "§c§lGüç Meyvesi");
        SPEED_ELIXIR = create(Material.SUGAR, "SPEED_ELIXIR", "§b§lHız İksiri");
        REGENERATION_ELIXIR = create(Material.GLISTERING_MELON_SLICE, "REGENERATION_ELIXIR", "§d§lYenilenme İksiri");
        STRENGTH_ELIXIR = create(Material.BLAZE_POWDER, "STRENGTH_ELIXIR", "§6§lGüç İksiri");

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
        RECIPE_BLUEPRINT_PAPER = createRecipeBook("RECIPE_BLUEPRINT_PAPER", "§bTarif: Mühendis Şeması");
        RECIPE_LIGHTNING_CORE = createRecipeBook("RECIPE_LIGHTNING_CORE", "§eTarif: Yıldırım Çekirdeği");
        RECIPE_TITANIUM_ORE = createRecipeBook("RECIPE_TITANIUM_ORE", "§fTarif: Titanyum Parçası");
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
        // Yiyecek tarif kitapları
        RECIPE_LIFE_ELIXIR = createRecipeBook("RECIPE_LIFE_ELIXIR", "§a§lTarif: Yaşam İksiri");
        RECIPE_POWER_FRUIT = createRecipeBook("RECIPE_POWER_FRUIT", "§c§lTarif: Güç Meyvesi");
        RECIPE_SPEED_ELIXIR = createRecipeBook("RECIPE_SPEED_ELIXIR", "§b§lTarif: Hız İksiri");
        RECIPE_REGENERATION_ELIXIR = createRecipeBook("RECIPE_REGENERATION_ELIXIR", "§d§lTarif: Yenilenme İksiri");
        RECIPE_STRENGTH_ELIXIR = createRecipeBook("RECIPE_STRENGTH_ELIXIR", "§6§lTarif: Güç İksiri");
        // Maden tarif kitapları
        RECIPE_SULFUR_ORE = createRecipeBook("RECIPE_SULFUR_ORE", "§eTarif: Kükürt Cevheri");
        RECIPE_BAUXITE_ORE = createRecipeBook("RECIPE_BAUXITE_ORE", "§6Tarif: Boksit Cevheri");
        RECIPE_ROCK_SALT_ORE = createRecipeBook("RECIPE_ROCK_SALT_ORE", "§fTarif: Tuz Kayası");
        RECIPE_MITHRIL_ORE = createRecipeBook("RECIPE_MITHRIL_ORE", "§bTarif: Mithril Cevheri");
        RECIPE_ASTRAL_ORE = createRecipeBook("RECIPE_ASTRAL_ORE", "§5Tarif: Astral Cevheri");
        // Çekirdek tarif kitapları
        RECIPE_TAMING_CORE = createRecipeBook("RECIPE_TAMING_CORE", "§a§lTarif: Eğitim Çekirdeği");
        RECIPE_SUMMON_CORE = createRecipeBook("RECIPE_SUMMON_CORE", "§5§lTarif: Çağırma Çekirdeği");
        RECIPE_BREEDING_CORE = createRecipeBook("RECIPE_BREEDING_CORE", "§d§lTarif: Üreme Çekirdeği");
        RECIPE_GENDER_SCANNER = createRecipeBook("RECIPE_GENDER_SCANNER", "§bTarif: Cinsiyet Ayırıcı");

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
        TAMING_CORE = create(Material.HEART_OF_THE_SEA, "TAMING_CORE", "§a§lEğitim Çekirdeği");
        SUMMON_CORE = create(Material.END_CRYSTAL, "SUMMON_CORE", "§5§lÇağırma Çekirdeği");
        BREEDING_CORE = create(Material.BEACON, "BREEDING_CORE", "§d§lÜreme Çekirdeği");
        GENDER_SCANNER = create(Material.SPYGLASS, "GENDER_SCANNER", "§bCinsiyet Ayırıcı");

        // ========== ÖZEL ZIRHLAR ==========
        initSpecialArmors();
        
        // ========== BOSS ÖZEL İTEMLERİ ==========
        initBossItems();
        
        // ========== ÖZEL SİLAHLAR ==========
        initSpecialWeapons(); // SpecialItemManager'dan silahları çek
        
        // ========== TARİF KİTAPLARI - ÖZEL ZIRH VE SİLAHLAR ==========
        initSpecialItemRecipeBooks();
        
        // ========== ÖZEL SİLAH TARİFLERİ (BOSS EŞYALARI İLE) ==========
        registerSpecialWeaponRecipes();

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

        // ========== BOSS ÖZEL İTEMLERİ ==========
        // Her boss için özel itemler (tarif zorlaştırma için)
        initBossItems();

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
        
        // Özel zırh ve silah tarifleri (boss itemleri ile)
        registerSpecialWeaponAndArmorRecipes();
        
        // Çekirdekler ve özel eşyalar için tarifler
        registerCoreRecipes();
        
        // Güçlü yiyecekler için tarifler
        registerConsumableRecipes();
        
        // Özel silah ve kalkanlar için tarifler
        registerSpecialWeaponAndShieldRecipes();
        
        // Kanca tarifleri (Golden Hook eksikti)
        registerGoldenHookRecipe();
        
        // Özel silah tarifleri (25 silah - Boss itemleri ile)
        registerSpecialWeaponRecipes();
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
        // BUG FIX: Türkçe karakter sorunu - Locale.ENGLISH kullan
        String keyName = "level" + level + "_" + type.name().toLowerCase(java.util.Locale.ENGLISH);
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), keyName),
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
        // BUG FIX: Türkçe karakter sorunu - Locale.ENGLISH kullan
        String keyName = "level" + level + "_" + type.name().toLowerCase(java.util.Locale.ENGLISH) + "_upgrade";
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), keyName),
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
            if (info.getCraftingRecipe() != null && !info.getCraftingRecipe().isEmpty()) {
                lore.add("");
                lore.add("§e§lCrafting Tarifi:");
                for (String line : info.getCraftingRecipe()) {
                    lore.add("§7" + line);
                }
            } else {
                lore.add("§7Tarif detayları için kitaba");
                lore.add("§7Shift+Sağ tıklayın.");
            }
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
            // Yeni eklenen tarif kitapları
            case "BLUEPRINT_PAPER":
                return new RecipeInfo("Mühendis Şeması", "§7Crafting masasında", "Yapı tasarımı için şema.", true);
            case "TITANIUM_ORE":
                return new RecipeInfo("Titanyum Parçası", "§7Crafting masasında", "Titanyum külçesi için ham madde.", true);
            case "LIFE_ELIXIR":
                return new RecipeInfo("Yaşam İksiri", "§7Crafting masasında", "Canı tamamen doldurur.", true);
            case "POWER_FRUIT":
                return new RecipeInfo("Güç Meyvesi", "§7Crafting masasında", "Hasarı 5 kat arttırır.", true);
            case "SPEED_ELIXIR":
                return new RecipeInfo("Hız İksiri", "§7Crafting masasında", "Hareket hızını arttırır.", true);
            case "REGENERATION_ELIXIR":
                return new RecipeInfo("Yenilenme İksiri", "§7Crafting masasında", "Hızlı can yenileme sağlar.", true);
            case "STRENGTH_ELIXIR":
                return new RecipeInfo("Güç İksiri", "§7Crafting masasında", "Saldırı gücünü arttırır.", true);
            case "SULFUR_ORE":
                return new RecipeInfo("Kükürt Cevheri", "§7Crafting masasında", "Kükürt için ham madde.", true);
            case "BAUXITE_ORE":
                return new RecipeInfo("Boksit Cevheri", "§7Crafting masasında", "Boksit külçesi için ham madde.", true);
            case "ROCK_SALT_ORE":
                return new RecipeInfo("Tuz Kayası", "§7Crafting masasında", "Tuz için ham madde.", true);
            case "MITHRIL_ORE":
                return new RecipeInfo("Mithril Cevheri", "§7Crafting masasında", "Mithril külçesi için ham madde.", true);
            case "ASTRAL_ORE":
                return new RecipeInfo("Astral Cevheri", "§7Crafting masasında", "Astral kristali için ham madde.", true);
            case "TAMING_CORE":
                return new RecipeInfo("Eğitim Çekirdeği", "§7Crafting masasında", "Mobları eğitmek için çekirdek.", true);
            case "SUMMON_CORE":
                return new RecipeInfo("Çağırma Çekirdeği", "§7Crafting masasında", "Boss çağırmak için çekirdek.", true);
            case "BREEDING_CORE":
                return new RecipeInfo("Üreme Çekirdeği", "§7Crafting masasında", "Mob üretimi için çekirdek.", true);
            case "GENDER_SCANNER":
                return new RecipeInfo("Cinsiyet Ayırıcı", "§7Crafting masasında", "Mob cinsiyetini tespit eder.", true);
            // Silah tarif kitapları - Her silah için özel tarif
            case "WEAPON_L1_1":
                return new RecipeInfo("Hız Hançeri Tarifi", "§7Crafting masasında", 
                    "Hızlı ve ölümcül hançer. Elinde tutarken hız verir.", true,
                    Arrays.asList("Satır 1: [I] [F] [I]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "I = Demir Külçe", "F = Tüy", "B = Goblin Kralı Taçı", "S = Çubuk"));
            case "WEAPON_L1_2":
                return new RecipeInfo("Çiftçi Tırpanı Tarifi", "§7Crafting masasında",
                    "Alan hasarı veren tırpan. Kalabalık gruplara etkili.", true,
                    Arrays.asList("Satır 1: [W] [W] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "W = Buğday", "B = Goblin Kralı Taçı", "S = Çubuk"));
            case "WEAPON_L1_3":
                return new RecipeInfo("Yerçekimi Gürzü Tarifi", "§7Crafting masasında",
                    "Sağ tıkla havaya fırlatır. Kaçış için ideal.", true,
                    Arrays.asList("Satır 1: [ ] [P] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "P = Barut", "B = Goblin Kralı Taçı", "S = Çubuk"));
            case "WEAPON_L1_4":
                return new RecipeInfo("Patlayıcı Yay Tarifi", "§7Crafting masasında",
                    "Okları patlar. Blok kırmayan patlama yaratır.", true,
                    Arrays.asList("Satır 1: [ ] [T] [ ]", "Satır 2: [A] [B] [A]", "Satır 3: [ ] [T] [ ]",
                        "T = TNT", "A = İp", "B = Goblin Kralı Taçı"));
            case "WEAPON_L1_5":
                return new RecipeInfo("Vampir Dişi Tarifi", "§7Crafting masasında",
                    "Vurduğun hasarın %20'si kadar can verir.", true,
                    Arrays.asList("Satır 1: [ ] [R] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [G] [ ]",
                        "R = Redstone", "B = Goblin Kralı Taçı", "G = Altın Külçe"));
            case "WEAPON_L2_1":
                return new RecipeInfo("Alev Kılıcı Tarifi", "§7Crafting masasında",
                    "Sağ tıkla alev dalgası atar. Önündeki alanı yakar.", true,
                    Arrays.asList("Satır 1: [ ] [F] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [G] [ ]",
                        "F = Alev Tozu", "B = Troll Kralı Kalbi", "G = Altın Külçe"));
            case "WEAPON_L2_2":
                return new RecipeInfo("Buz Asası Tarifi", "§7Crafting masasında",
                    "Sağ tıkla hedefi 3 saniye dondurur.", true,
                    Arrays.asList("Satır 1: [ ] [I] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "I = Buz", "B = Troll Kralı Kalbi", "S = Çubuk"));
            case "WEAPON_L2_3":
                return new RecipeInfo("Zehirli Mızrak Tarifi", "§7Crafting masasında",
                    "Fırlatıldığında zehir bulutu oluşturur.", true,
                    Arrays.asList("Satır 1: [ ] [E] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "E = Örümcek Gözü", "B = Troll Kralı Kalbi", "S = Çubuk"));
            case "WEAPON_L2_4":
                return new RecipeInfo("Golem Kalkanı Tarifi", "§7Crafting masasında",
                    "Eğilince dostları iyileştirir ve korur.", true,
                    Arrays.asList("Satır 1: [ ] [I] [ ]", "Satır 2: [I] [B] [I]", "Satır 3: [I] [I] [I]",
                        "I = Demir Blok", "B = Troll Kralı Kalbi"));
            case "WEAPON_L2_5":
                return new RecipeInfo("Şok Baltası Tarifi", "§7Crafting masasında",
                    "Kritik vuruşta yıldırım düşürür.", true,
                    Arrays.asList("Satır 1: [L] [L] [ ]", "Satır 2: [L] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "L = Paratoner", "B = Troll Kralı Kalbi", "S = Çubuk"));
            case "WEAPON_L3_1":
                return new RecipeInfo("Gölge Katanası Tarifi", "§7Crafting masasında",
                    "Sağ tıkla 6 blok dash atar ve yoluna çıkanlara hasar verir.", true,
                    Arrays.asList("Satır 1: [ ] [C] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "C = Kömür Bloğu", "B = T-Rex Dişi", "S = Çubuk"));
            case "WEAPON_L3_2":
                return new RecipeInfo("Deprem Çekici Tarifi", "§7Crafting masasında",
                    "Sağ tıkla yeri sarstırır ve herkesi havaya fırlatır.", true,
                    Arrays.asList("Satır 1: [O] [O] [O]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "O = Obsidyen", "B = T-Rex Dişi", "S = Çubuk"));
            case "WEAPON_L3_3":
                return new RecipeInfo("Taramalı Yay Tarifi", "§7Crafting masasında",
                    "Sağ tık basılı tutulduğunda saniyede 5 ok atar.", true,
                    Arrays.asList("Satır 1: [ ] [R] [ ]", "Satır 2: [A] [B] [A]", "Satır 3: [ ] [R] [ ]",
                        "R = Redstone Bloğu", "A = İp", "B = T-Rex Dişi"));
            case "WEAPON_L3_4":
                return new RecipeInfo("Büyücü Küresi Tarifi", "§7Crafting masasında",
                    "Sağ tıkla en yakın 3 düşmana güdümlü mermi atar.", true,
                    Arrays.asList("Satır 1: [ ] [G] [ ]", "Satır 2: [G] [B] [G]", "Satır 3: [ ] [G] [ ]",
                        "G = Magma Kremi", "B = T-Rex Dişi"));
            case "WEAPON_L3_5":
                return new RecipeInfo("Hayalet Hançeri Tarifi", "§7Crafting masasında",
                    "Sağ tıkla 5 saniye görünmez ol. İlk vuruş 3x hasar.", true,
                    Arrays.asList("Satır 1: [ ] [P] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [F] [ ]",
                        "P = Hayalet Zarı", "B = T-Rex Dişi", "F = Tüy"));
            case "WEAPON_L4_1":
                return new RecipeInfo("Element Kılıcı Tarifi", "§7Crafting masasında",
                    "Mod 1: Her vuruşta alev saçar | Mod 2: Her vuruşta yavaşlatır.", true,
                    Arrays.asList("Satır 1: [ ] [F] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [D] [ ]",
                        "F = Alev Amplifikatörü", "B = Titan Golem Çekirdeği", "D = Elmas Kılıç"));
            case "WEAPON_L4_2":
                return new RecipeInfo("Yaşam ve Ölüm Tarifi", "§7Crafting masasında",
                    "Mod 1: Wither kafası fırlatır | Mod 2: Can basar.", true,
                    Arrays.asList("Satır 1: [ ] [W] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [K] [ ]",
                        "W = Wither Kafası", "B = Titan Golem Çekirdeği", "K = Kemik"));
            case "WEAPON_L4_3":
                return new RecipeInfo("Mjölnir V2 Tarifi", "§7Crafting masasında",
                    "Mod 1: Zincirleme yıldırım | Mod 2: Fırlat ve geri dön.", true,
                    Arrays.asList("Satır 1: [ ] [L] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [A] [ ]",
                        "L = Yıldırım Çekirdeği", "B = Titan Golem Çekirdeği", "A = Demir Balta"));
            case "WEAPON_L4_4":
                return new RecipeInfo("Avcı Yayı Tarifi", "§7Crafting masasında",
                    "Mod 1: Sniper (50 blok x2 hasar) | Mod 2: Shotgun (5 ok).", true,
                    Arrays.asList("Satır 1: [ ] [S] [ ]", "Satır 2: [A] [B] [A]", "Satır 3: [ ] [ ] [ ]",
                        "S = Dürbün", "A = İp", "B = Titan Golem Çekirdeği"));
            case "WEAPON_L4_5":
                return new RecipeInfo("Manyetik Eldiven Tarifi", "§7Crafting masasında",
                    "Mod 1: Hedefi çek | Mod 2: Hedefi fırlat.", true,
                    Arrays.asList("Satır 1: [ ] [ ] [I]", "Satır 2: [ ] [I] [B]", "Satır 3: [I] [A] [ ]",
                        "I = Demir Külçe", "B = Titan Golem Çekirdeği", "A = Olta"));
            case "WEAPON_L5_1":
                return new RecipeInfo("Hiperiyon Kılıcı Tarifi", "§7Crafting masasında",
                    "Mod 1: Işınlan ve patlat | Mod 2: Kara delik kalkanı.", true,
                    Arrays.asList("Satır 1: [ ] [E] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [N] [ ]",
                        "E = Ender Gözü", "B = Void Dragon Heart", "N = Netherite Kılıç"));
            case "WEAPON_L5_2":
                return new RecipeInfo("Meteor Çağıran Tarifi", "§7Crafting masasında",
                    "Mod 1: Meteor yağdır | Mod 2: Yer yarıp lav çıkart.", true,
                    Arrays.asList("Satır 1: [ ] [F] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [G] [ ]",
                        "F = Ateş Topu", "B = Void Dragon Heart", "G = Altın Balta"));
            case "WEAPON_L5_3":
                return new RecipeInfo("Titan Katili Tarifi", "§7Crafting masasında",
                    "Mod 1: %5 hasar | Mod 2: Mızrak yağmuru (10 mızrak).", true,
                    Arrays.asList("Satır 1: [ ] [D] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "D = Elmas Blok", "B = Void Dragon Heart", "S = Çubuk"));
            case "WEAPON_L5_4":
                return new RecipeInfo("Ruh Biçen Tarifi", "§7Crafting masasında",
                    "Mod 1: Öldürdüğün mobları çağır | Mod 2: Hortlakları patlat.", true,
                    Arrays.asList("Satır 1: [ ] [W] [ ]", "Satır 2: [ ] [B] [ ]", "Satır 3: [ ] [S] [ ]",
                        "W = Wither Gülü", "B = Void Dragon Heart", "S = Ruh Kumu"));
            case "WEAPON_L5_5":
                return new RecipeInfo("Zamanı Büken Tarifi", "§7Crafting masasında",
                    "Mod 1: Zamanı durdur (10 sn) | Mod 2: Geri sar (5 sn önce).", true,
                    Arrays.asList("Satır 1: [ ] [S] [ ]", "Satır 2: [C] [B] [C]", "Satır 3: [ ] [S] [ ]",
                        "S = Saat", "B = Void Dragon Heart", "C = Yıldız Çekirdeği"));
            // Zırh tarif kitapları (RECIPE_ARMOR_L1_1 -> ARMOR_L1_1)
            case "ARMOR_L1_1":
            case "ARMOR_L1_2":
            case "ARMOR_L1_3":
            case "ARMOR_L1_4":
            case "ARMOR_L1_5":
                return new RecipeInfo("Seviye 1 Zırh Tarifi", "§7Crafting masasında", "Goblin Kralı Taçı gerektirir.", true);
            case "ARMOR_L2_1":
            case "ARMOR_L2_2":
            case "ARMOR_L2_3":
            case "ARMOR_L2_4":
            case "ARMOR_L2_5":
                return new RecipeInfo("Seviye 2 Zırh Tarifi", "§7Crafting masasında", "Troll Kralı Kalbi gerektirir.", true);
            case "ARMOR_L3_1":
            case "ARMOR_L3_2":
            case "ARMOR_L3_3":
            case "ARMOR_L3_4":
            case "ARMOR_L3_5":
                return new RecipeInfo("Seviye 3 Zırh Tarifi", "§7Crafting masasında", "T-Rex Dişi gerektirir.", true);
            case "ARMOR_L4_1":
            case "ARMOR_L4_2":
            case "ARMOR_L4_3":
            case "ARMOR_L4_4":
            case "ARMOR_L4_5":
                return new RecipeInfo("Seviye 4 Zırh Tarifi", "§7Crafting masasında", "Titan Golem Çekirdeği gerektirir.", true);
            case "ARMOR_L5_1":
            case "ARMOR_L5_2":
            case "ARMOR_L5_3":
            case "ARMOR_L5_4":
            case "ARMOR_L5_5":
                return new RecipeInfo("Seviye 5 Zırh Tarifi", "§7Crafting masasında", "Void Dragon Heart gerektirir.", true);
            
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
        private final List<String> craftingRecipe;
        
        public RecipeInfo(String displayName, String locationInfo, String functionInfo) {
            this(displayName, locationInfo, functionInfo, false, null);
        }
        
        public RecipeInfo(String displayName, String locationInfo, String functionInfo, boolean isItemRecipe) {
            this(displayName, locationInfo, functionInfo, isItemRecipe, null);
        }
        
        public RecipeInfo(String displayName, String locationInfo, String functionInfo, boolean isItemRecipe, List<String> craftingRecipe) {
            this.displayName = displayName;
            this.locationInfo = locationInfo;
            this.functionInfo = functionInfo;
            this.isItemRecipe = isItemRecipe;
            this.craftingRecipe = craftingRecipe != null ? craftingRecipe : new ArrayList<>();
        }
        
        public String getDisplayName() { return displayName; }
        public String getLocationInfo() { return locationInfo; }
        public String getFunctionInfo() { return functionInfo; }
        public boolean isItemRecipe() { return isItemRecipe; }
        public List<String> getCraftingRecipe() { return craftingRecipe; }
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
    
    /**
     * Özel zırhları başlat
     */
    private void initSpecialArmors() {
        // Seviye 1 Zırhlar (Sadece koruma)
        ARMOR_L1_1 = createSpecialArmor(1, 1, Material.IRON_HELMET, "Demir Savaşçı Zırhı", "§f");
        ARMOR_L1_2 = createSpecialArmor(1, 2, Material.IRON_CHESTPLATE, "Demir Koruyucu Zırhı", "§f");
        ARMOR_L1_3 = createSpecialArmor(1, 3, Material.IRON_LEGGINGS, "Demir Avcı Zırhı", "§f");
        ARMOR_L1_4 = createSpecialArmor(1, 4, Material.IRON_BOOTS, "Demir Kaşif Zırhı", "§f");
        ARMOR_L1_5 = createSpecialArmor(1, 5, Material.IRON_CHESTPLATE, "Demir Şövalye Zırhı", "§f");
        
        // Seviye 2 Zırhlar (Diken Etkisi)
        ARMOR_L2_1 = createSpecialArmor(2, 1, Material.DIAMOND_HELMET, "Elmas Diken Zırhı", "§b");
        ARMOR_L2_2 = createSpecialArmor(2, 2, Material.DIAMOND_CHESTPLATE, "Elmas Zehir Diken Zırhı", "§b");
        ARMOR_L2_3 = createSpecialArmor(2, 3, Material.DIAMOND_LEGGINGS, "Elmas Ateş Diken Zırhı", "§b");
        ARMOR_L2_4 = createSpecialArmor(2, 4, Material.DIAMOND_BOOTS, "Elmas Buz Diken Zırhı", "§b");
        ARMOR_L2_5 = createSpecialArmor(2, 5, Material.DIAMOND_CHESTPLATE, "Elmas Yıldırım Diken Zırhı", "§b");
        
        // Seviye 3 Zırhlar (2x Hız, Yüksek Zıplama, Aşırı Koruma)
        ARMOR_L3_1 = createSpecialArmor(3, 1, Material.NETHERITE_HELMET, "Netherite Hız Zırhı", "§5");
        ARMOR_L3_2 = createSpecialArmor(3, 2, Material.NETHERITE_CHESTPLATE, "Netherite Zıplama Zırhı", "§5");
        ARMOR_L3_3 = createSpecialArmor(3, 3, Material.NETHERITE_LEGGINGS, "Netherite Savunma Zırhı", "§5");
        ARMOR_L3_4 = createSpecialArmor(3, 4, Material.NETHERITE_BOOTS, "Netherite Savaşçı Zırhı", "§5");
        ARMOR_L3_5 = createSpecialArmor(3, 5, Material.NETHERITE_CHESTPLATE, "Netherite Efsane Zırhı", "§5");
        
        // Seviye 4 Zırhlar (Sürekli Can Yenileme)
        ARMOR_L4_1 = createSpecialArmor(4, 1, Material.NETHERITE_HELMET, "Titanyum Yaşam Zırhı", "§6");
        ARMOR_L4_2 = createSpecialArmor(4, 2, Material.NETHERITE_CHESTPLATE, "Titanyum Ölümsüzlük Zırhı", "§6");
        ARMOR_L4_3 = createSpecialArmor(4, 3, Material.NETHERITE_LEGGINGS, "Titanyum Yenilenme Zırhı", "§6");
        ARMOR_L4_4 = createSpecialArmor(4, 4, Material.NETHERITE_BOOTS, "Titanyum Kutsal Zırhı", "§6");
        ARMOR_L4_5 = createSpecialArmor(4, 5, Material.NETHERITE_CHESTPLATE, "Titanyum Ebedi Zırhı", "§6");
        
        // Seviye 5 Zırhlar (Uçma Gücü)
        ARMOR_L5_1 = createSpecialArmor(5, 1, Material.NETHERITE_HELMET, "Efsanevi Uçan Zırhı", "§d§l");
        ARMOR_L5_2 = createSpecialArmor(5, 2, Material.NETHERITE_CHESTPLATE, "Efsanevi Gökyüzü Zırhı", "§d§l");
        ARMOR_L5_3 = createSpecialArmor(5, 3, Material.NETHERITE_LEGGINGS, "Efsanevi Bulut Zırhı", "§d§l");
        ARMOR_L5_4 = createSpecialArmor(5, 4, Material.NETHERITE_BOOTS, "Efsanevi Yıldız Zırhı", "§d§l");
        ARMOR_L5_5 = createSpecialArmor(5, 5, Material.NETHERITE_CHESTPLATE, "Efsanevi Tanrı Zırhı", "§d§l");
    }
    
    /**
     * Özel Silahları Başlat (SpecialItemManager üzerinden)
     */
    private void initSpecialWeapons() {
        me.mami.stratocraft.manager.SpecialItemManager sim = Main.getInstance().getSpecialItemManager();
        if (sim == null) {
            Main.getInstance().getLogger().severe("HATA: SpecialItemManager başlatılamadı! Silahlar yüklenemiyor.");
            return;
        }

        // --- SEVİYE 1 (Çaylak) ---
        WEAPON_L1_1 = sim.getTier1Weapon("l1_1"); // Hız Hançeri
        WEAPON_L1_2 = sim.getTier1Weapon("l1_2"); // Çiftçi Tırpanı
        WEAPON_L1_3 = sim.getTier1Weapon("l1_3"); // Yerçekimi Gürzü
        WEAPON_L1_4 = sim.getTier1Weapon("l1_4"); // Patlayıcı Yay
        WEAPON_L1_5 = sim.getTier1Weapon("l1_5"); // Vampir Dişi

        // --- SEVİYE 2 (Asker) ---
        WEAPON_L2_1 = sim.getTier2Weapon("l2_1"); // Alev Kılıcı
        WEAPON_L2_2 = sim.getTier2Weapon("l2_2"); // Buz Asası
        WEAPON_L2_3 = sim.getTier2Weapon("l2_3"); // Zehirli Mızrak
        WEAPON_L2_4 = sim.getTier2Weapon("l2_4"); // Golem Kalkanı
        WEAPON_L2_5 = sim.getTier2Weapon("l2_5"); // Şok Baltası

        // --- SEVİYE 3 (Elit) ---
        WEAPON_L3_1 = sim.getTier3Weapon("l3_1"); // Gölge Katanası
        WEAPON_L3_2 = sim.getTier3Weapon("l3_2"); // Deprem Çekici
        WEAPON_L3_3 = sim.getTier3Weapon("l3_3"); // Taramalı Yay
        WEAPON_L3_4 = sim.getTier3Weapon("l3_4"); // Büyücü Küresi
        WEAPON_L3_5 = sim.getTier3Weapon("l3_5"); // Hayalet Hançeri

        // --- SEVİYE 4 (Efsanevi) ---
        WEAPON_L4_1 = sim.getTier4Weapon("l4_1"); // Element Kılıcı
        WEAPON_L4_2 = sim.getTier4Weapon("l4_2"); // Yaşam ve Ölüm
        WEAPON_L4_3 = sim.getTier4Weapon("l4_3"); // Mjölnir V2
        WEAPON_L4_4 = sim.getTier4Weapon("l4_4"); // Avcı Yayı
        WEAPON_L4_5 = sim.getTier4Weapon("l4_5"); // Manyetik Eldiven

        // --- SEVİYE 5 (Tanrısal) ---
        WEAPON_L5_1 = sim.getTier5Weapon("l5_1"); // Hiperiyon Kılıcı
        WEAPON_L5_2 = sim.getTier5Weapon("l5_2"); // Meteor Çağıran
        WEAPON_L5_3 = sim.getTier5Weapon("l5_3"); // Titan Katili
        WEAPON_L5_4 = sim.getTier5Weapon("l5_4"); // Ruh Biçen
        WEAPON_L5_5 = sim.getTier5Weapon("l5_5"); // Zamanı Büken
    }
    
    /**
     * Özel zırh oluştur
     */
    private ItemStack createSpecialArmor(int level, int variant, Material material, String name, String color) {
        ItemStack armor = new ItemStack(material);
        ItemMeta meta = armor.getItemMeta();
        meta.setDisplayName(color + "§l" + name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Seviye: §e" + level);
        double armorPoints = 6.0 + (level - 1) * 4.0; // Seviye 1: 6, Seviye 2: 10, Seviye 3: 14, Seviye 4: 18, Seviye 5: 22
        lore.add("§7Koruma: §b" + String.format("%.1f", armorPoints));
        lore.add("");
        
        // Özel güçler
        if (level >= 2) {
            lore.add("§6§lÖzel Güçler:");
            if (level == 2) {
                lore.add("§e• Güçlü Diken Etkisi");
                lore.add("§7  Saldırıya uğradığında saldırana hasar verir");
            } else if (level == 3) {
                lore.add("§e• 2x Hız Artışı");
                lore.add("§e• Yüksek Zıplama Gücü");
                lore.add("§e• Aşırı Güçlü Koruma");
            } else if (level == 4) {
                lore.add("§e• Sürekli Can Yenileme");
                lore.add("§7  Her saniye can yenilenir");
            } else if (level == 5) {
                lore.add("§e• Uçma Gücü");
                lore.add("§7  Çift zıplama ile uçabilirsin");
            }
        }
        
        lore.add("");
        lore.add("§7Stratocraft Özel Zırhı");
        meta.setLore(lore);
        
        // Zırh modifier ekle
        EquipmentSlot slot = material.name().contains("HELMET") ? EquipmentSlot.HEAD :
                            material.name().contains("CHESTPLATE") ? EquipmentSlot.CHEST :
                            material.name().contains("LEGGINGS") ? EquipmentSlot.LEGS :
                            EquipmentSlot.FEET;
        
        AttributeModifier armorMod = new AttributeModifier(
            UUID.randomUUID(),
            "special_armor_" + level + "_" + variant,
            armorPoints,
            AttributeModifier.Operation.ADD_NUMBER,
            slot
        );
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, armorMod);
        
        // Özel ID kaydet
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "special_armor_id"),
            PersistentDataType.STRING,
            "ARMOR_L" + level + "_" + variant
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "armor_level"),
            PersistentDataType.INTEGER,
            level
        );
        
        armor.setItemMeta(meta);
        return armor;
    }
    
    /**
     * Özel silah oluştur (varsayılan - özel yetenek yok)
     */
    private ItemStack createSpecialWeapon(int level, int variant, Material material, String name, String color) {
        return createSpecialWeapon(level, variant, material, name, color, (String[]) null);
    }
    
    /**
     * Özel silah oluştur (özel yetenekler ile)
     */
    private ItemStack createSpecialWeapon(int level, int variant, Material material, String name, String color, String... abilities) {
        ItemStack weapon = new ItemStack(material);
        ItemMeta meta = weapon.getItemMeta();
        meta.setDisplayName(color + "§l" + name);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Seviye: §e" + level);
        double baseDamage = 5.0 + (level - 1) * 3.0; // Seviye 1: 5, Seviye 2: 8, Seviye 3: 11, Seviye 4: 14, Seviye 5: 17
        lore.add("§7Hasar: §c" + String.format("%.1f", baseDamage));
        lore.add("");
        
        // Özel yetenekler
        if (abilities != null && abilities.length > 0) {
            lore.add("§6§lÖzel Yetenekler:");
            for (String ability : abilities) {
                if (ability != null && !ability.isEmpty()) {
                    lore.add(ability);
                }
            }
        } else if (level >= 3) {
            // Varsayılan yetenekler (geriye dönük uyumluluk)
            lore.add("§6§lÖzel Yetenekler:");
            if (level == 3) {
                lore.add("§e• Patlama Atabilme");
                lore.add("§7  Sağ tık ile 20 blok menzile patlama at");
            } else if (level == 4) {
                lore.add("§e• Devamlı Lazer");
                lore.add("§7  Sağ tık ile yüksek hasarlı lazer at");
            } else if (level == 5) {
                lore.add("§e• Çok Modlu Silah");
                lore.add("§7  Shift+Sağ Tık: Mod Seçimi");
                lore.add("§7  Q: Blok Fırlatma");
                lore.add("§7  Sağ Tık: Duvar Yapma");
                lore.add("§7  Sağ Tık: Atılma/Patlama");
            }
        }
        
        lore.add("");
        lore.add("§7Stratocraft Özel Silahı");
        meta.setLore(lore);
        
        // Hasar modifier ekle (sadece kılıç ve balta için)
        if (material.name().contains("SWORD") || material.name().contains("AXE")) {
            AttributeModifier damageMod = new AttributeModifier(
                UUID.randomUUID(),
                "special_weapon_damage_" + level + "_" + variant,
                baseDamage - 1.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageMod);
        }
        
        // Özel ID kaydet
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "special_weapon_id"),
            PersistentDataType.STRING,
            "WEAPON_L" + level + "_" + variant
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Main.getInstance(), "weapon_level"),
            PersistentDataType.INTEGER,
            level
        );
        
        weapon.setItemMeta(meta);
        return weapon;
    }
    
    /**
     * Boss özel itemlerini başlat
     */
    private void initBossItems() {
        // Seviye 1 Bosslar
        GOBLIN_CROWN = create(Material.GOLDEN_HELMET, "GOBLIN_CROWN", "§6Goblin Kralı Taçı");
        ORC_AMULET = create(Material.GOLDEN_APPLE, "ORC_AMULET", "§6Ork Şefi Amuleti");
        
        // Seviye 2 Bosslar
        TROLL_HEART = create(Material.HEART_OF_THE_SEA, "TROLL_HEART", "§5Troll Kralı Kalbi");
        
        // Seviye 3 Bosslar
        DRAGON_SCALE = create(Material.SCUTE, "DRAGON_SCALE", "§cEjderha Ölçeği");
        TREX_TOOTH = create(Material.BONE, "TREX_TOOTH", "§7T-Rex Dişi");
        CYCLOPS_EYE = create(Material.ENDER_EYE, "CYCLOPS_EYE", "§5Cyclops Gözü");
        
        // Seviye 4 Bosslar
        TITAN_CORE = create(Material.NETHER_STAR, "TITAN_CORE", "§6Titan Golem Çekirdeği");
        PHOENIX_FEATHER = create(Material.FEATHER, "PHOENIX_FEATHER", "§cPhoenix Tüyü");
        KRAKEN_TENTACLE = create(Material.KELP, "KRAKEN_TENTACLE", "§9Kraken Dokunaçı");
        
        // Seviye 5 Bosslar
        DEMON_LORD_HORN = create(Material.GOAT_HORN, "DEMON_LORD_HORN", "§4§lŞeytan Lordu Boynuzu");
        VOID_DRAGON_HEART = create(Material.ECHO_SHARD, "VOID_DRAGON_HEART", "§5§lHiçlik Ejderi Kalbi");
    }
    
    /**
     * Özel zırh ve silah tarif kitaplarını başlat
     */
    private void initSpecialItemRecipeBooks() {
        // Seviye 1 Zırh Tarifleri
        RECIPE_ARMOR_L1_1 = createRecipeBook("RECIPE_ARMOR_L1_1", "§fTarif: Demir Savaşçı Zırhı");
        RECIPE_ARMOR_L1_2 = createRecipeBook("RECIPE_ARMOR_L1_2", "§fTarif: Demir Koruyucu Zırhı");
        RECIPE_ARMOR_L1_3 = createRecipeBook("RECIPE_ARMOR_L1_3", "§fTarif: Demir Avcı Zırhı");
        RECIPE_ARMOR_L1_4 = createRecipeBook("RECIPE_ARMOR_L1_4", "§fTarif: Demir Kaşif Zırhı");
        RECIPE_ARMOR_L1_5 = createRecipeBook("RECIPE_ARMOR_L1_5", "§fTarif: Demir Şövalye Zırhı");
        
        // Seviye 2 Zırh Tarifleri
        RECIPE_ARMOR_L2_1 = createRecipeBook("RECIPE_ARMOR_L2_1", "§bTarif: Elmas Diken Zırhı");
        RECIPE_ARMOR_L2_2 = createRecipeBook("RECIPE_ARMOR_L2_2", "§bTarif: Elmas Zehir Diken Zırhı");
        RECIPE_ARMOR_L2_3 = createRecipeBook("RECIPE_ARMOR_L2_3", "§bTarif: Elmas Ateş Diken Zırhı");
        RECIPE_ARMOR_L2_4 = createRecipeBook("RECIPE_ARMOR_L2_4", "§bTarif: Elmas Buz Diken Zırhı");
        RECIPE_ARMOR_L2_5 = createRecipeBook("RECIPE_ARMOR_L2_5", "§bTarif: Elmas Yıldırım Diken Zırhı");
        
        // Seviye 3 Zırh Tarifleri
        RECIPE_ARMOR_L3_1 = createRecipeBook("RECIPE_ARMOR_L3_1", "§5Tarif: Netherite Hız Zırhı");
        RECIPE_ARMOR_L3_2 = createRecipeBook("RECIPE_ARMOR_L3_2", "§5Tarif: Netherite Zıplama Zırhı");
        RECIPE_ARMOR_L3_3 = createRecipeBook("RECIPE_ARMOR_L3_3", "§5Tarif: Netherite Savunma Zırhı");
        RECIPE_ARMOR_L3_4 = createRecipeBook("RECIPE_ARMOR_L3_4", "§5Tarif: Netherite Savaşçı Zırhı");
        RECIPE_ARMOR_L3_5 = createRecipeBook("RECIPE_ARMOR_L3_5", "§5Tarif: Netherite Efsane Zırhı");
        
        // Seviye 4 Zırh Tarifleri
        RECIPE_ARMOR_L4_1 = createRecipeBook("RECIPE_ARMOR_L4_1", "§6Tarif: Titanyum Yaşam Zırhı");
        RECIPE_ARMOR_L4_2 = createRecipeBook("RECIPE_ARMOR_L4_2", "§6Tarif: Titanyum Ölümsüzlük Zırhı");
        RECIPE_ARMOR_L4_3 = createRecipeBook("RECIPE_ARMOR_L4_3", "§6Tarif: Titanyum Yenilenme Zırhı");
        RECIPE_ARMOR_L4_4 = createRecipeBook("RECIPE_ARMOR_L4_4", "§6Tarif: Titanyum Kutsal Zırhı");
        RECIPE_ARMOR_L4_5 = createRecipeBook("RECIPE_ARMOR_L4_5", "§6Tarif: Titanyum Ebedi Zırhı");
        
        // Seviye 5 Zırh Tarifleri
        RECIPE_ARMOR_L5_1 = createRecipeBook("RECIPE_ARMOR_L5_1", "§d§lTarif: Efsanevi Uçan Zırhı");
        RECIPE_ARMOR_L5_2 = createRecipeBook("RECIPE_ARMOR_L5_2", "§d§lTarif: Efsanevi Gökyüzü Zırhı");
        RECIPE_ARMOR_L5_3 = createRecipeBook("RECIPE_ARMOR_L5_3", "§d§lTarif: Efsanevi Bulut Zırhı");
        RECIPE_ARMOR_L5_4 = createRecipeBook("RECIPE_ARMOR_L5_4", "§d§lTarif: Efsanevi Yıldız Zırhı");
        RECIPE_ARMOR_L5_5 = createRecipeBook("RECIPE_ARMOR_L5_5", "§d§lTarif: Efsanevi Tanrı Zırhı");
        
        // Seviye 1 Silah Tarifleri
        RECIPE_WEAPON_L1_1 = createRecipeBook("RECIPE_WEAPON_L1_1", "§fTarif: Demir Kılıç");
        RECIPE_WEAPON_L1_2 = createRecipeBook("RECIPE_WEAPON_L1_2", "§fTarif: Demir Balta");
        RECIPE_WEAPON_L1_3 = createRecipeBook("RECIPE_WEAPON_L1_3", "§fTarif: Demir Mızrak");
        RECIPE_WEAPON_L1_4 = createRecipeBook("RECIPE_WEAPON_L1_4", "§fTarif: Demir Yay");
        RECIPE_WEAPON_L1_5 = createRecipeBook("RECIPE_WEAPON_L1_5", "§fTarif: Demir Çekiç");
        
        // Seviye 2 Silah Tarifleri
        RECIPE_WEAPON_L2_1 = createRecipeBook("RECIPE_WEAPON_L2_1", "§bTarif: Elmas Kılıç");
        RECIPE_WEAPON_L2_2 = createRecipeBook("RECIPE_WEAPON_L2_2", "§bTarif: Elmas Balta");
        RECIPE_WEAPON_L2_3 = createRecipeBook("RECIPE_WEAPON_L2_3", "§bTarif: Elmas Mızrak");
        RECIPE_WEAPON_L2_4 = createRecipeBook("RECIPE_WEAPON_L2_4", "§bTarif: Elmas Yay");
        RECIPE_WEAPON_L2_5 = createRecipeBook("RECIPE_WEAPON_L2_5", "§bTarif: Elmas Çekiç");
        
        // Seviye 3 Silah Tarifleri
        RECIPE_WEAPON_L3_1 = createRecipeBook("RECIPE_WEAPON_L3_1", "§5Tarif: Netherite Patlama Kılıcı");
        RECIPE_WEAPON_L3_2 = createRecipeBook("RECIPE_WEAPON_L3_2", "§5Tarif: Netherite Patlama Baltası");
        RECIPE_WEAPON_L3_3 = createRecipeBook("RECIPE_WEAPON_L3_3", "§5Tarif: Netherite Patlama Mızrağı");
        RECIPE_WEAPON_L3_4 = createRecipeBook("RECIPE_WEAPON_L3_4", "§5Tarif: Netherite Patlama Yayı");
        RECIPE_WEAPON_L3_5 = createRecipeBook("RECIPE_WEAPON_L3_5", "§5Tarif: Netherite Patlama Çekici");
        
        // Seviye 4 Silah Tarifleri
        RECIPE_WEAPON_L4_1 = createRecipeBook("RECIPE_WEAPON_L4_1", "§6Tarif: Titanyum Lazer Kılıcı");
        RECIPE_WEAPON_L4_2 = createRecipeBook("RECIPE_WEAPON_L4_2", "§6Tarif: Titanyum Lazer Baltası");
        RECIPE_WEAPON_L4_3 = createRecipeBook("RECIPE_WEAPON_L4_3", "§6Tarif: Titanyum Lazer Mızrağı");
        RECIPE_WEAPON_L4_4 = createRecipeBook("RECIPE_WEAPON_L4_4", "§6Tarif: Titanyum Lazer Yayı");
        RECIPE_WEAPON_L4_5 = createRecipeBook("RECIPE_WEAPON_L4_5", "§6Tarif: Titanyum Lazer Çekici");
        
        // Seviye 5 Silah Tarifleri
        RECIPE_WEAPON_L5_1 = createRecipeBook("RECIPE_WEAPON_L5_1", "§d§lTarif: Efsanevi Çok Modlu Kılıç");
        RECIPE_WEAPON_L5_2 = createRecipeBook("RECIPE_WEAPON_L5_2", "§d§lTarif: Efsanevi Çok Modlu Balta");
        RECIPE_WEAPON_L5_3 = createRecipeBook("RECIPE_WEAPON_L5_3", "§d§lTarif: Efsanevi Çok Modlu Mızrak");
        RECIPE_WEAPON_L5_4 = createRecipeBook("RECIPE_WEAPON_L5_4", "§d§lTarif: Efsanevi Çok Modlu Yay");
        RECIPE_WEAPON_L5_5 = createRecipeBook("RECIPE_WEAPON_L5_5", "§d§lTarif: Efsanevi Çok Modlu Çekiç");
    }
    
    /**
     * Özel zırh ve silah tariflerini kaydet (boss itemleri ile zorlaştırılmış)
     */
    private void registerSpecialWeaponAndArmorRecipes() {
        // Seviye 1 Zırh Tarifleri (Kolay - Sadece demir)
        registerSpecialArmorRecipe(1, 1, Material.IRON_HELMET, Material.IRON_INGOT, null);
        registerSpecialArmorRecipe(1, 2, Material.IRON_CHESTPLATE, Material.IRON_INGOT, null);
        registerSpecialArmorRecipe(1, 3, Material.IRON_LEGGINGS, Material.IRON_INGOT, null);
        registerSpecialArmorRecipe(1, 4, Material.IRON_BOOTS, Material.IRON_INGOT, null);
        registerSpecialArmorRecipe(1, 5, Material.IRON_CHESTPLATE, Material.IRON_INGOT, null);
        
        // Seviye 2 Zırh Tarifleri (Orta - Elmas + Goblin/Ork itemi)
        registerSpecialArmorRecipe(2, 1, Material.DIAMOND_HELMET, Material.DIAMOND, GOBLIN_CROWN);
        registerSpecialArmorRecipe(2, 2, Material.DIAMOND_CHESTPLATE, Material.DIAMOND, ORC_AMULET);
        registerSpecialArmorRecipe(2, 3, Material.DIAMOND_LEGGINGS, Material.DIAMOND, GOBLIN_CROWN);
        registerSpecialArmorRecipe(2, 4, Material.DIAMOND_BOOTS, Material.DIAMOND, ORC_AMULET);
        registerSpecialArmorRecipe(2, 5, Material.DIAMOND_CHESTPLATE, Material.DIAMOND, TROLL_HEART);
        
        // Seviye 3 Zırh Tarifleri (Zor - Netherite + Seviye 3 boss itemi)
        registerSpecialArmorRecipe(3, 1, Material.NETHERITE_HELMET, Material.NETHERITE_INGOT, DRAGON_SCALE);
        registerSpecialArmorRecipe(3, 2, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_INGOT, TREX_TOOTH);
        registerSpecialArmorRecipe(3, 3, Material.NETHERITE_LEGGINGS, Material.NETHERITE_INGOT, CYCLOPS_EYE);
        registerSpecialArmorRecipe(3, 4, Material.NETHERITE_BOOTS, Material.NETHERITE_INGOT, DRAGON_SCALE);
        registerSpecialArmorRecipe(3, 5, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_INGOT, TREX_TOOTH);
        
        // Seviye 4 Zırh Tarifleri (Çok Zor - Titanyum + Seviye 4 boss itemi)
        // Not: TITANIUM_INGOT bir ItemStack, ama metod Material bekliyor. TITANIUM_INGOT Material.IRON_INGOT kullanıyor.
        registerSpecialArmorRecipe(4, 1, Material.NETHERITE_HELMET, Material.IRON_INGOT, TITAN_CORE);
        registerSpecialArmorRecipe(4, 2, Material.NETHERITE_CHESTPLATE, Material.IRON_INGOT, PHOENIX_FEATHER);
        registerSpecialArmorRecipe(4, 3, Material.NETHERITE_LEGGINGS, Material.IRON_INGOT, KRAKEN_TENTACLE);
        registerSpecialArmorRecipe(4, 4, Material.NETHERITE_BOOTS, Material.IRON_INGOT, TITAN_CORE);
        registerSpecialArmorRecipe(4, 5, Material.NETHERITE_CHESTPLATE, Material.IRON_INGOT, PHOENIX_FEATHER);
        
        // Seviye 5 Zırh Tarifleri (Efsanevi - Netherite + Seviye 5 boss itemi)
        registerSpecialArmorRecipe(5, 1, Material.NETHERITE_HELMET, Material.NETHERITE_INGOT, DEMON_LORD_HORN);
        registerSpecialArmorRecipe(5, 2, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_INGOT, VOID_DRAGON_HEART);
        registerSpecialArmorRecipe(5, 3, Material.NETHERITE_LEGGINGS, Material.NETHERITE_INGOT, DEMON_LORD_HORN);
        registerSpecialArmorRecipe(5, 4, Material.NETHERITE_BOOTS, Material.NETHERITE_INGOT, VOID_DRAGON_HEART);
        registerSpecialArmorRecipe(5, 5, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_INGOT, DEMON_LORD_HORN);
        
        // Seviye 1 Silah Tarifleri (Kolay - Sadece demir)
        registerSpecialWeaponRecipe(1, 1, Material.IRON_SWORD, Material.IRON_INGOT, null);
        registerSpecialWeaponRecipe(1, 2, Material.IRON_AXE, Material.IRON_INGOT, null);
        registerSpecialWeaponRecipe(1, 3, Material.TRIDENT, Material.IRON_INGOT, null);
        registerSpecialWeaponRecipe(1, 4, Material.BOW, Material.IRON_INGOT, null);
        registerSpecialWeaponRecipe(1, 5, Material.IRON_PICKAXE, Material.IRON_INGOT, null);
        
        // Seviye 2 Silah Tarifleri (Orta - Elmas + Goblin/Ork itemi)
        registerSpecialWeaponRecipe(2, 1, Material.DIAMOND_SWORD, Material.DIAMOND, GOBLIN_CROWN);
        registerSpecialWeaponRecipe(2, 2, Material.DIAMOND_AXE, Material.DIAMOND, ORC_AMULET);
        registerSpecialWeaponRecipe(2, 3, Material.TRIDENT, Material.DIAMOND, GOBLIN_CROWN);
        registerSpecialWeaponRecipe(2, 4, Material.BOW, Material.DIAMOND, ORC_AMULET);
        registerSpecialWeaponRecipe(2, 5, Material.DIAMOND_PICKAXE, Material.DIAMOND, TROLL_HEART);
        
        // Seviye 3 Silah Tarifleri (Zor - Netherite + Seviye 3 boss itemi)
        registerSpecialWeaponRecipe(3, 1, Material.NETHERITE_SWORD, Material.NETHERITE_INGOT, DRAGON_SCALE);
        registerSpecialWeaponRecipe(3, 2, Material.NETHERITE_AXE, Material.NETHERITE_INGOT, TREX_TOOTH);
        registerSpecialWeaponRecipe(3, 3, Material.TRIDENT, Material.NETHERITE_INGOT, CYCLOPS_EYE);
        registerSpecialWeaponRecipe(3, 4, Material.BOW, Material.NETHERITE_INGOT, DRAGON_SCALE);
        registerSpecialWeaponRecipe(3, 5, Material.NETHERITE_PICKAXE, Material.NETHERITE_INGOT, TREX_TOOTH);
        
        // Seviye 4 Silah Tarifleri (Çok Zor - Titanyum + Seviye 4 boss itemi)
        // Not: TITANIUM_INGOT bir ItemStack, ama metod Material bekliyor. TITANIUM_INGOT Material.IRON_INGOT kullanıyor.
        registerSpecialWeaponRecipe(4, 1, Material.NETHERITE_SWORD, Material.IRON_INGOT, TITAN_CORE);
        registerSpecialWeaponRecipe(4, 2, Material.NETHERITE_AXE, Material.IRON_INGOT, PHOENIX_FEATHER);
        registerSpecialWeaponRecipe(4, 3, Material.TRIDENT, Material.IRON_INGOT, KRAKEN_TENTACLE);
        registerSpecialWeaponRecipe(4, 4, Material.BOW, Material.IRON_INGOT, TITAN_CORE);
        registerSpecialWeaponRecipe(4, 5, Material.NETHERITE_PICKAXE, Material.IRON_INGOT, PHOENIX_FEATHER);
        
        // Seviye 5 Silah Tarifleri (Efsanevi - Netherite + Seviye 5 boss itemi)
        registerSpecialWeaponRecipe(5, 1, Material.NETHERITE_SWORD, Material.NETHERITE_INGOT, DEMON_LORD_HORN);
        registerSpecialWeaponRecipe(5, 2, Material.NETHERITE_AXE, Material.NETHERITE_INGOT, VOID_DRAGON_HEART);
        registerSpecialWeaponRecipe(5, 3, Material.TRIDENT, Material.NETHERITE_INGOT, DEMON_LORD_HORN);
        registerSpecialWeaponRecipe(5, 4, Material.BOW, Material.NETHERITE_INGOT, VOID_DRAGON_HEART);
        registerSpecialWeaponRecipe(5, 5, Material.NETHERITE_PICKAXE, Material.NETHERITE_INGOT, DEMON_LORD_HORN);
    }
    
    /**
     * Tüm Özel Silah Tariflerini Kaydet (Boss İtemleri ile)
     */
    private void registerSpecialWeaponRecipes() {
        // --- SEVİYE 1: GOBLIN CROWN GEREKTİRİR ---
        // L1_1: Hız Hançeri (Demir Kılıç + Tüy + Goblin Tacı)
        registerRecipe("craft_l1_1", WEAPON_L1_1, "IFI", " B ", " S ", 
            'I', Material.IRON_INGOT, 'F', Material.FEATHER, 'B', GOBLIN_CROWN, 'S', Material.STICK);

        // L1_2: Çiftçi Tırpanı (Demir Çapa + Buğday + Goblin Tacı)
        registerRecipe("craft_l1_2", WEAPON_L1_2, "WW ", " B ", " S ", 
            'W', Material.WHEAT, 'B', GOBLIN_CROWN, 'S', Material.STICK);

        // L1_3: Yerçekimi Gürzü (Demir Kürek + Barut + Goblin Tacı)
        registerRecipe("craft_l1_3", WEAPON_L1_3, " P ", " B ", " S ", 
            'P', Material.GUNPOWDER, 'B', GOBLIN_CROWN, 'S', Material.STICK);

        // L1_4: Patlayıcı Yay (Yay + TNT + Goblin Tacı)
        registerRecipe("craft_l1_4", WEAPON_L1_4, " T ", "ABA", " T ", 
            'A', Material.STRING, 'B', GOBLIN_CROWN, 'T', Material.TNT);

        // L1_5: Vampir Dişi (Altın Kılıç + Redstone + Goblin Tacı)
        registerRecipe("craft_l1_5", WEAPON_L1_5, " R ", " B ", " G ", 
            'R', Material.REDSTONE, 'B', GOBLIN_CROWN, 'G', Material.GOLD_INGOT);

        // --- SEVİYE 2: TROLL HEART GEREKTİRİR ---
        // L2_1: Alev Kılıcı (Altın Kılıç + Blaze Powder + Troll Kalbi)
        registerRecipe("craft_l2_1", WEAPON_L2_1, " F ", " B ", " G ", 
            'F', Material.BLAZE_POWDER, 'B', TROLL_HEART, 'G', Material.GOLD_INGOT);

        // L2_2: Buz Asası (Çubuk + Buz + Troll Kalbi)
        registerRecipe("craft_l2_2", WEAPON_L2_2, " I ", " B ", " S ", 
            'I', Material.PACKED_ICE, 'B', TROLL_HEART, 'S', Material.STICK);

        // L2_3: Zehirli Mızrak (Trident yoksa Demir Mızrak + Örümcek Gözü + Troll Kalbi)
        registerRecipe("craft_l2_3", WEAPON_L2_3, " E ", " B ", " S ", 
            'E', Material.SPIDER_EYE, 'B', TROLL_HEART, 'S', Material.STICK);

        // L2_4: Golem Kalkanı (Kalkan + Demir Blok + Troll Kalbi)
        registerRecipe("craft_l2_4", WEAPON_L2_4, " I ", "IBI", " I ", 
            'I', Material.IRON_BLOCK, 'B', TROLL_HEART);

        // L2_5: Şok Baltası (Demir Balta + Paratoner + Troll Kalbi)
        registerRecipe("craft_l2_5", WEAPON_L2_5, "LL ", "LB ", " S ", 
            'L', Material.LIGHTNING_ROD, 'B', TROLL_HEART, 'S', Material.STICK);

        // --- SEVİYE 3: TREX TOOTH GEREKTİRİR ---
        // L3_1: Gölge Katanası (Demir Kılıç + Kömür Bloğu + T-Rex Dişi)
        registerRecipe("craft_l3_1", WEAPON_L3_1, " C ", " B ", " S ", 
            'C', Material.COAL_BLOCK, 'B', TREX_TOOTH, 'S', Material.STICK);

        // L3_2: Deprem Çekici (Netherite Kürek/Elmas Kürek + Obsidyen + T-Rex Dişi)
        registerRecipe("craft_l3_2", WEAPON_L3_2, "OOO", " B ", " S ", 
            'O', Material.OBSIDIAN, 'B', TREX_TOOTH, 'S', Material.STICK);

        // L3_3: Taramalı Yay (Arbalet + Redstone Blok + T-Rex Dişi)
        registerRecipe("craft_l3_3", WEAPON_L3_3, " R ", "ABA", " R ", 
            'R', Material.REDSTONE_BLOCK, 'A', Material.STRING, 'B', TREX_TOOTH);

        // L3_4: Büyücü Küresi (Magma Kremi + Işıktaşı + T-Rex Dişi)
        registerRecipe("craft_l3_4", WEAPON_L3_4, " G ", "GBG", " G ", 
            'G', Material.MAGMA_CREAM, 'B', TREX_TOOTH);

        // L3_5: Hayalet Hançeri (Tüy + Hayalet Zarı + T-Rex Dişi)
        registerRecipe("craft_l3_5", WEAPON_L3_5, " P ", " B ", " F ", 
            'P', Material.PHANTOM_MEMBRANE, 'B', TREX_TOOTH, 'F', Material.FEATHER);

        // --- SEVİYE 4: TITAN CORE GEREKTİRİR ---
        // L4_1: Element Kılıcı (Elmas Kılıç + Alev Amplifikatörü + Titan Çekirdeği)
        registerRecipe("craft_l4_1", WEAPON_L4_1, " F ", " B ", " D ", 
            'F', FLAME_AMPLIFIER, 'B', TITAN_CORE, 'D', Material.DIAMOND_SWORD);

        // L4_2: Yaşam ve Ölüm (Kemik + Wither Kafası + Titan Çekirdeği)
        registerRecipe("craft_l4_2", WEAPON_L4_2, " W ", " B ", " K ", 
            'W', Material.WITHER_SKELETON_SKULL, 'B', TITAN_CORE, 'K', Material.BONE);

        // L4_3: Mjölnir V2 (Demir Balta + Yıldırım Çekirdeği + Titan Çekirdeği)
        registerRecipe("craft_l4_3", WEAPON_L4_3, " L ", " B ", " A ", 
            'L', LIGHTNING_CORE, 'B', TITAN_CORE, 'A', Material.IRON_AXE);

        // L4_4: Avcı Yayı (Yay + Dürbün + Titan Çekirdeği)
        registerRecipe("craft_l4_4", WEAPON_L4_4, " S ", "ABA", "   ", 
            'S', Material.SPYGLASS, 'A', Material.STRING, 'B', TITAN_CORE);

        // L4_5: Manyetik Eldiven (Olta + Demir Külçe + Titan Çekirdeği)
        registerRecipe("craft_l4_5", WEAPON_L4_5, "  I", " IB", "I A", 
            'I', Material.IRON_INGOT, 'B', TITAN_CORE, 'A', Material.FISHING_ROD);

        // --- SEVİYE 5: VOID DRAGON HEART GEREKTİRİR ---
        // L5_1: Hiperiyon Kılıcı (Netherite Kılıç + Ender Gözü + Void Dragon Heart)
        registerRecipe("craft_l5_1", WEAPON_L5_1, " E ", " B ", " N ", 
            'E', Material.ENDER_EYE, 'B', VOID_DRAGON_HEART, 'N', Material.NETHERITE_SWORD);

        // L5_2: Meteor Çağıran (Altın Balta + Ateş Topu + Void Dragon Heart)
        registerRecipe("craft_l5_2", WEAPON_L5_2, " F ", " B ", " G ", 
            'F', Material.FIRE_CHARGE, 'B', VOID_DRAGON_HEART, 'G', Material.GOLDEN_AXE);

        // L5_3: Titan Katili (Mızrak + Elmas Blok + Void Dragon Heart)
        registerRecipe("craft_l5_3", WEAPON_L5_3, " D ", " B ", " S ", 
            'D', Material.DIAMOND_BLOCK, 'B', VOID_DRAGON_HEART, 'S', Material.STICK);

        // L5_4: Ruh Biçen (Wither Gülü + Ruh Kumu + Void Dragon Heart)
        registerRecipe("craft_l5_4", WEAPON_L5_4, " W ", " B ", " S ", 
            'W', Material.WITHER_ROSE, 'B', VOID_DRAGON_HEART, 'S', Material.SOUL_SAND);

        // L5_5: Zamanı Büken (Saat + Yıldız Çekirdeği + Void Dragon Heart)
        registerRecipe("craft_l5_5", WEAPON_L5_5, " S ", "CBC", " S ", 
            'S', Material.CLOCK, 'B', VOID_DRAGON_HEART, 'C', STAR_CORE);
    }
    
    /**
     * Yardımcı Metod: Özel Eşyalı Tarif Kaydetme
     */
    private void registerRecipe(String key, ItemStack result, String line1, String line2, String line3, Object... ingredients) {
        if (result == null) return;
        
        org.bukkit.inventory.ShapedRecipe recipe = new org.bukkit.inventory.ShapedRecipe(
            new NamespacedKey(Main.getInstance(), key), result);
            
        recipe.shape(line1, line2, line3);
        
        for (int i = 0; i < ingredients.length; i += 2) {
            char keyChar = (char) ingredients[i];
            Object ingredient = ingredients[i + 1];
            
            if (ingredient instanceof Material) {
                recipe.setIngredient(keyChar, (Material) ingredient);
            } else if (ingredient instanceof ItemStack) {
                // Boss itemleri gibi özel itemler için ExactChoice kullanıyoruz
                recipe.setIngredient(keyChar, new org.bukkit.inventory.RecipeChoice.ExactChoice((ItemStack) ingredient));
            }
        }
        
        Bukkit.addRecipe(recipe);
    }
    
    /**
     * Özel zırh tarifi kaydet
     */
    private void registerSpecialArmorRecipe(int level, int variant, Material armorMaterial, Material baseMaterial, ItemStack bossItem) {
        ItemStack armor = null;
        switch (level) {
            case 1:
                switch (variant) {
                    case 1: armor = ARMOR_L1_1; break;
                    case 2: armor = ARMOR_L1_2; break;
                    case 3: armor = ARMOR_L1_3; break;
                    case 4: armor = ARMOR_L1_4; break;
                    case 5: armor = ARMOR_L1_5; break;
                }
                break;
            case 2:
                switch (variant) {
                    case 1: armor = ARMOR_L2_1; break;
                    case 2: armor = ARMOR_L2_2; break;
                    case 3: armor = ARMOR_L2_3; break;
                    case 4: armor = ARMOR_L2_4; break;
                    case 5: armor = ARMOR_L2_5; break;
                }
                break;
            case 3:
                switch (variant) {
                    case 1: armor = ARMOR_L3_1; break;
                    case 2: armor = ARMOR_L3_2; break;
                    case 3: armor = ARMOR_L3_3; break;
                    case 4: armor = ARMOR_L3_4; break;
                    case 5: armor = ARMOR_L3_5; break;
                }
                break;
            case 4:
                switch (variant) {
                    case 1: armor = ARMOR_L4_1; break;
                    case 2: armor = ARMOR_L4_2; break;
                    case 3: armor = ARMOR_L4_3; break;
                    case 4: armor = ARMOR_L4_4; break;
                    case 5: armor = ARMOR_L4_5; break;
                }
                break;
            case 5:
                switch (variant) {
                    case 1: armor = ARMOR_L5_1; break;
                    case 2: armor = ARMOR_L5_2; break;
                    case 3: armor = ARMOR_L5_3; break;
                    case 4: armor = ARMOR_L5_4; break;
                    case 5: armor = ARMOR_L5_5; break;
                }
                break;
        }
        
        if (armor == null) return;
        
        // Zırh tipine göre şekil belirle
        String shape1, shape2, shape3;
        if (armorMaterial.name().contains("HELMET")) {
            shape1 = "MMM";
            shape2 = "M M";
            shape3 = "   ";
        } else if (armorMaterial.name().contains("CHESTPLATE")) {
            shape1 = "M M";
            shape2 = "MMM";
            shape3 = "MMM";
        } else if (armorMaterial.name().contains("LEGGINGS")) {
            shape1 = "MMM";
            shape2 = "M M";
            shape3 = "M M";
        } else { // BOOTS
            shape1 = "   ";
            shape2 = "M M";
            shape3 = "M M";
        }
        
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "special_armor_l" + level + "_" + variant),
            armor.clone()
        );
        recipe.shape(shape1, shape2, shape3);
        recipe.setIngredient('M', baseMaterial);
        
        // Boss itemi varsa ortaya ekle (custom item kontrolü ResearchListener'da yapılacak)
        // Şimdilik sadece base material ile tarif oluşturuluyor
        
        Bukkit.addRecipe(recipe);
    }
    
    /**
     * Özel silah tarifi kaydet
     */
    private void registerSpecialWeaponRecipe(int level, int variant, Material weaponMaterial, Material baseMaterial, ItemStack bossItem) {
        ItemStack weapon = null;
        switch (level) {
            case 1:
                switch (variant) {
                    case 1: weapon = WEAPON_L1_1; break;
                    case 2: weapon = WEAPON_L1_2; break;
                    case 3: weapon = WEAPON_L1_3; break;
                    case 4: weapon = WEAPON_L1_4; break;
                    case 5: weapon = WEAPON_L1_5; break;
                }
                break;
            case 2:
                switch (variant) {
                    case 1: weapon = WEAPON_L2_1; break;
                    case 2: weapon = WEAPON_L2_2; break;
                    case 3: weapon = WEAPON_L2_3; break;
                    case 4: weapon = WEAPON_L2_4; break;
                    case 5: weapon = WEAPON_L2_5; break;
                }
                break;
            case 3:
                switch (variant) {
                    case 1: weapon = WEAPON_L3_1; break;
                    case 2: weapon = WEAPON_L3_2; break;
                    case 3: weapon = WEAPON_L3_3; break;
                    case 4: weapon = WEAPON_L3_4; break;
                    case 5: weapon = WEAPON_L3_5; break;
                }
                break;
            case 4:
                switch (variant) {
                    case 1: weapon = WEAPON_L4_1; break;
                    case 2: weapon = WEAPON_L4_2; break;
                    case 3: weapon = WEAPON_L4_3; break;
                    case 4: weapon = WEAPON_L4_4; break;
                    case 5: weapon = WEAPON_L4_5; break;
                }
                break;
            case 5:
                switch (variant) {
                    case 1: weapon = WEAPON_L5_1; break;
                    case 2: weapon = WEAPON_L5_2; break;
                    case 3: weapon = WEAPON_L5_3; break;
                    case 4: weapon = WEAPON_L5_4; break;
                    case 5: weapon = WEAPON_L5_5; break;
                }
                break;
        }
        
        if (weapon == null) return;
        
        // Silah tipine göre şekil belirle
        String shape1, shape2, shape3;
        if (weaponMaterial.name().contains("SWORD") || weaponMaterial.name().contains("AXE") || weaponMaterial.name().contains("PICKAXE")) {
            shape1 = " M ";
            shape2 = " M ";
            shape3 = " S ";
        } else if (weaponMaterial == Material.BOW) {
            shape1 = " MS";
            shape2 = "M S";
            shape3 = " MS";
        } else { // TRIDENT
            shape1 = " M ";
            shape2 = " M ";
            shape3 = " S ";
        }
        
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "special_weapon_l" + level + "_" + variant),
            weapon.clone()
        );
        recipe.shape(shape1, shape2, shape3);
        recipe.setIngredient('M', baseMaterial);
        recipe.setIngredient('S', Material.STICK);
        
        // Boss itemi varsa ortaya ekle (custom item kontrolü ResearchListener'da yapılacak)
        // Şimdilik sadece base material ile tarif oluşturuluyor
        
        Bukkit.addRecipe(recipe);
    }
    
    /**
     * Çekirdekler ve özel eşyalar için tarifler
     */
    private void registerCoreRecipes() {
        // Eğitim Çekirdeği: 4 Demir + 1 Altın Elma + 4 Yem
        ShapedRecipe tamingCore = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_taming_core"),
            TAMING_CORE.clone()
        );
        tamingCore.shape("III", "IGI", "III");
        tamingCore.setIngredient('I', Material.IRON_INGOT);
        tamingCore.setIngredient('G', Material.GOLDEN_APPLE);
        Bukkit.addRecipe(tamingCore);
        
        // Çağırma Çekirdeği: 4 Obsidyen + 1 Ender İncisi + 4 Netherite
        ShapedRecipe summonCore = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_summon_core"),
            SUMMON_CORE.clone()
        );
        summonCore.shape("ONO", "NEN", "ONO");
        summonCore.setIngredient('O', Material.OBSIDIAN);
        summonCore.setIngredient('N', Material.NETHERITE_INGOT);
        summonCore.setIngredient('E', Material.ENDER_PEARL);
        Bukkit.addRecipe(summonCore);
        
        // Üreme Çekirdeği: 4 Elmas + 1 Altın Elma + 4 Altın
        ShapedRecipe breedingCore = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_breeding_core"),
            BREEDING_CORE.clone()
        );
        breedingCore.shape("DGD", "GAG", "DGD");
        breedingCore.setIngredient('D', Material.DIAMOND);
        breedingCore.setIngredient('G', Material.GOLD_INGOT);
        breedingCore.setIngredient('A', Material.GOLDEN_APPLE);
        Bukkit.addRecipe(breedingCore);
        
        // Cinsiyet Ayırıcı: 3 Cam + 1 Kırmızı Taş + 3 Altın
        ShapedRecipe genderScanner = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_gender_scanner"),
            GENDER_SCANNER.clone()
        );
        genderScanner.shape("GGG", "GRG", "GGG");
        genderScanner.setIngredient('G', Material.GOLD_INGOT);
        genderScanner.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(genderScanner);
    }
    
    /**
     * Güçlü yiyecekler için tarifler
     */
    private void registerConsumableRecipes() {
        // Yaşam İksiri: 3 Altın Elma + 1 Altın + 5 Cam Şişe
        ShapelessRecipe lifeElixir = new ShapelessRecipe(
            new NamespacedKey(Main.getInstance(), "craft_life_elixir"),
            LIFE_ELIXIR.clone()
        );
        lifeElixir.addIngredient(Material.GOLDEN_APPLE);
        lifeElixir.addIngredient(Material.GOLDEN_APPLE);
        lifeElixir.addIngredient(Material.GOLDEN_APPLE);
        lifeElixir.addIngredient(Material.GOLD_INGOT);
        lifeElixir.addIngredient(Material.GLASS_BOTTLE);
        lifeElixir.addIngredient(Material.GLASS_BOTTLE);
        lifeElixir.addIngredient(Material.GLASS_BOTTLE);
        lifeElixir.addIngredient(Material.GLASS_BOTTLE);
        lifeElixir.addIngredient(Material.GLASS_BOTTLE);
        Bukkit.addRecipe(lifeElixir);
        
        // Güç Meyvesi: 1 Altın Elma + 8 Netherite
        ShapedRecipe powerFruit = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_power_fruit"),
            POWER_FRUIT.clone()
        );
        powerFruit.shape("NNN", "NGN", "NNN");
        powerFruit.setIngredient('N', Material.NETHERITE_INGOT);
        powerFruit.setIngredient('G', Material.GOLDEN_APPLE);
        Bukkit.addRecipe(powerFruit);
        
        // Hız İksiri: 2 Şeker + 1 Kırmızı Taş + 1 Cam Şişe
        ShapelessRecipe speedElixir = new ShapelessRecipe(
            new NamespacedKey(Main.getInstance(), "craft_speed_elixir"),
            SPEED_ELIXIR.clone()
        );
        speedElixir.addIngredient(Material.SUGAR);
        speedElixir.addIngredient(Material.SUGAR);
        speedElixir.addIngredient(Material.REDSTONE);
        speedElixir.addIngredient(Material.GLASS_BOTTLE);
        Bukkit.addRecipe(speedElixir);
        
        // Yenilenme İksiri: 1 Altın Havuç + 1 Kırmızı Taş + 1 Cam Şişe
        ShapelessRecipe regenElixir = new ShapelessRecipe(
            new NamespacedKey(Main.getInstance(), "craft_regeneration_elixir"),
            REGENERATION_ELIXIR.clone()
        );
        regenElixir.addIngredient(Material.GOLDEN_CARROT);
        regenElixir.addIngredient(Material.REDSTONE);
        regenElixir.addIngredient(Material.GLASS_BOTTLE);
        Bukkit.addRecipe(regenElixir);
        
        // Güç İksiri: 1 Blaze Tozu + 1 Kırmızı Taş + 1 Cam Şişe
        ShapelessRecipe strengthElixir = new ShapelessRecipe(
            new NamespacedKey(Main.getInstance(), "craft_strength_elixir"),
            STRENGTH_ELIXIR.clone()
        );
        strengthElixir.addIngredient(Material.BLAZE_POWDER);
        strengthElixir.addIngredient(Material.REDSTONE);
        strengthElixir.addIngredient(Material.GLASS_BOTTLE);
        Bukkit.addRecipe(strengthElixir);
    }
    
    /**
     * Özel silah ve kalkanlar için tarifler
     */
    private void registerSpecialWeaponAndShieldRecipes() {
        // Savaş Yelpazesi: 3 Demir + 2 İp + 4 Altın
        ShapedRecipe warFan = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_war_fan"),
            WAR_FAN.clone()
        );
        warFan.shape("IGI", "S S", "IGI");
        warFan.setIngredient('I', Material.IRON_INGOT);
        warFan.setIngredient('G', Material.GOLD_INGOT);
        warFan.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(warFan);
        
        // Kule Kalkanı: 6 Demir + 1 Elmas + 2 Tahta
        ShapedRecipe towerShield = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_tower_shield"),
            TOWER_SHIELD.clone()
        );
        towerShield.shape("IDI", "IWI", "IWI");
        towerShield.setIngredient('I', Material.IRON_INGOT);
        towerShield.setIngredient('D', Material.DIAMOND);
        towerShield.setIngredient('W', Material.OAK_PLANKS);
        Bukkit.addRecipe(towerShield);
        
        // Cehennem Meyvesi: 1 Elma + 4 Netherite + 4 Alev Tozu
        ShapedRecipe hellFruit = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_hell_fruit"),
            HELL_FRUIT.clone()
        );
        hellFruit.shape("NFN", "FAF", "NFN");
        hellFruit.setIngredient('N', Material.NETHERITE_INGOT);
        hellFruit.setIngredient('F', Material.BLAZE_POWDER);
        hellFruit.setIngredient('A', Material.APPLE);
        Bukkit.addRecipe(hellFruit);
    }
    
    /**
     * Altın Kanca tarifi
     */
    private void registerGoldenHookRecipe() {
        // Altın Kanca: 2 Altın + 1 İp + 1 Demir
        ShapedRecipe goldenHook = new ShapedRecipe(
            new NamespacedKey(Main.getInstance(), "craft_golden_hook"),
            GOLDEN_HOOK.clone()
        );
        goldenHook.shape(" G ", " G ", "IS ");
        goldenHook.setIngredient('G', Material.GOLD_INGOT);
        goldenHook.setIngredient('I', Material.IRON_INGOT);
        goldenHook.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(goldenHook);
    }
}
