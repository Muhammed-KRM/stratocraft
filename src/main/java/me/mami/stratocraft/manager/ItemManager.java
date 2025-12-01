package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

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
    public static ItemStack WAR_FAN;
    public static ItemStack TOWER_SHIELD;
    public static ItemStack HELL_FRUIT;

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
        RECIPE_BOOK_TECTONIC = create(Material.BOOK, "RECIPE_TECTONIC", "§dTarif: Tektonik Sabitleyici");
        WAR_FAN = create(Material.FEATHER, "WAR_FAN", "§eSavaş Yelpazesi");
        TOWER_SHIELD = create(Material.SHIELD, "TOWER_SHIELD", "§7Kule Kalkanı");
        HELL_FRUIT = create(Material.APPLE, "HELL_FRUIT", "§cCehennem Meyvesi");

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
}
