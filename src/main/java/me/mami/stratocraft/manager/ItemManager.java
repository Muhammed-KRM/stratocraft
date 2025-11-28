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
    public static ItemStack TITAN_GRAPPLE;
    public static ItemStack TRAP_CORE;

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
        
        // Yeni Eşyalar
        RUSTY_HOOK = create(Material.FISHING_ROD, "RUSTY_HOOK", "§7Paslı Kanca");
        TITAN_GRAPPLE = create(Material.FISHING_ROD, "TITAN_GRAPPLE", "§6§lTitan Kancası");
        TRAP_CORE = create(Material.LODESTONE, "TRAP_CORE", "§cTuzak Çekirdeği");

        registerRecipes();
    }

    private void registerRecipes() {
        ShapelessRecipe blueprint = new ShapelessRecipe(new NamespacedKey(Main.getInstance(), "craft_blueprint"), BLUEPRINT_PAPER);
        blueprint.addIngredient(Material.PAPER);
        blueprint.addIngredient(Material.LAPIS_LAZULI);
        Bukkit.addRecipe(blueprint);

        ShapedRecipe lightning = new ShapedRecipe(new NamespacedKey(Main.getInstance(), "craft_lightning_core"), LIGHTNING_CORE);
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
            TRAP_CORE
        );
        trapCoreRecipe.shape("OEO", "IDI", "OEO");
        trapCoreRecipe.setIngredient('O', Material.OBSIDIAN);      // Obsidyen
        trapCoreRecipe.setIngredient('E', Material.ENDER_PEARL);   // Ender İncisi
        trapCoreRecipe.setIngredient('I', Material.IRON_INGOT);     // Demir
        trapCoreRecipe.setIngredient('D', Material.DIAMOND);        // Elmas (ortada)
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
        //        Elmas Blok - Ender İncisi - Elmas Blok
        //        Boş - Obsidyen - Boş
        recipe.shape(" B ", "BEB", " O ");
        recipe.setIngredient('B', Material.DIAMOND_BLOCK); // Elmas Blok
        recipe.setIngredient('E', Material.ENDER_PEARL);  // Ender İncisi
        recipe.setIngredient('O', Material.OBSIDIAN);    // Obsidyen
        
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
        if (item == null || item.getItemMeta() == null) return false;
        String data = item.getItemMeta().getPersistentDataContainer()
                .get(new NamespacedKey(Main.getInstance(), "custom_id"), PersistentDataType.STRING);
        return id != null && id.equals(data);
    }
    
    /**
     * Bir eşyanın Klan Kristali veya Klan Çiti olup olmadığını kontrol eder
     */
    public static boolean isClanItem(ItemStack item, String type) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "clan_item");
        String data = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return data != null && data.equals(type);
    }
}

