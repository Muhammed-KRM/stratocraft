package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.RecipeType;
import me.mami.stratocraft.enums.RecipeCategory;
import me.mami.stratocraft.model.recipe.Recipe;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Merkezi Tarif Yönetim Sistemi
 * 
 * Sorumluluklar:
 * - Tüm tarifleri yönetir (Structure, Battery, Ritual, Crafting, vb.)
 * - Tarifleri JSON/YAML formatında saklar
 * - Tarif yükleme/kaydetme
 * - Tarif arama ve filtreleme
 * 
 * Thread-Safe: ConcurrentHashMap kullanır
 */
public class RecipeManager {
    
    private final Main plugin;
    
    // Tarifler: Recipe ID -> Recipe
    private final Map<String, Recipe> recipes = new ConcurrentHashMap<>();
    
    // Kategoriye göre tarifler: RecipeCategory -> List<Recipe>
    private final Map<RecipeCategory, List<Recipe>> recipesByCategory = new ConcurrentHashMap<>();
    
    // Tipe göre tarifler: RecipeType -> List<Recipe>
    private final Map<RecipeType, List<Recipe>> recipesByType = new ConcurrentHashMap<>();
    
    // Tarif dosyaları dizini
    private final File recipesDirectory;
    
    public RecipeManager(Main plugin) {
        this.plugin = plugin;
        this.recipesDirectory = new File(plugin.getDataFolder(), "recipes");
        
        // Dizinleri oluştur
        createDirectories();
        
        // Tarifleri yükle
        loadAllRecipes();
    }
    
    /**
     * Tarif dizinlerini oluştur
     */
    private void createDirectories() {
        if (!recipesDirectory.exists()) {
            recipesDirectory.mkdirs();
        }
        
        // Alt dizinler
        new File(recipesDirectory, "structures").mkdirs();
        new File(recipesDirectory, "batteries").mkdirs();
        new File(recipesDirectory, "rituals").mkdirs();
        new File(recipesDirectory, "crafting").mkdirs();
        new File(recipesDirectory, "other").mkdirs();
    }
    
    /**
     * Tüm tarifleri yükle
     */
    private void loadAllRecipes() {
        // Structure tarifleri
        loadRecipesFromDirectory(new File(recipesDirectory, "structures"), RecipeType.STRUCTURE);
        
        // Battery tarifleri
        loadRecipesFromDirectory(new File(recipesDirectory, "batteries"), RecipeType.BATTERY);
        
        // Ritual tarifleri
        loadRecipesFromDirectory(new File(recipesDirectory, "rituals"), RecipeType.RITUAL);
        
        // Crafting tarifleri
        loadRecipesFromDirectory(new File(recipesDirectory, "crafting"), RecipeType.CRAFTING);
        
        // Diğer tarifler
        loadRecipesFromDirectory(new File(recipesDirectory, "other"), RecipeType.CUSTOM);
        
        plugin.getLogger().info("Yüklenen toplam tarif sayısı: " + recipes.size());
    }
    
    /**
     * Dizinden tarifleri yükle
     */
    private void loadRecipesFromDirectory(File directory, RecipeType defaultType) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) return;
        
        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadRecipesFromConfig(config, defaultType);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Tarif dosyası yüklenirken hata: " + file.getName(), e);
            }
        }
    }
    
    /**
     * Config'den tarifleri yükle
     */
    private void loadRecipesFromConfig(FileConfiguration config, RecipeType defaultType) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;
            
            try {
                Recipe recipe = loadRecipeFromSection(key, section, defaultType);
                if (recipe != null) {
                    registerRecipe(recipe);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Tarif yüklenirken hata: " + key, e);
            }
        }
    }
    
    /**
     * Config section'dan tarif oluştur
     */
    private Recipe loadRecipeFromSection(String recipeId, ConfigurationSection section, RecipeType defaultType) {
        // Temel bilgiler
        RecipeType type = section.contains("type") ? 
            RecipeType.valueOf(section.getString("type").toUpperCase()) : defaultType;
        RecipeCategory category = section.contains("category") ? 
            RecipeCategory.valueOf(section.getString("category").toUpperCase()) : RecipeCategory.SPECIAL;
        String displayName = section.getString("displayName", recipeId);
        int level = section.getInt("level", 1);
        
        // Malzemeler
        List<ItemStack> ingredients = new ArrayList<>();
        if (section.contains("ingredients")) {
            List<?> ingredientsList = section.getList("ingredients");
            if (ingredientsList != null) {
                for (Object item : ingredientsList) {
                    if (item instanceof ItemStack) {
                        ingredients.add((ItemStack) item);
                    } else if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        Material material = Material.valueOf(itemMap.get("material").toString().toUpperCase());
                        int amount = itemMap.containsKey("amount") ? (Integer) itemMap.get("amount") : 1;
                        ingredients.add(new ItemStack(material, amount));
                    }
                }
            }
        }
        
        // Sonuç
        ItemStack result = null;
        if (section.contains("result")) {
            Object resultObj = section.get("result");
            if (resultObj instanceof ItemStack) {
                result = (ItemStack) resultObj;
            } else if (resultObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultMap = (Map<String, Object>) resultObj;
                Material material = Material.valueOf(resultMap.get("material").toString().toUpperCase());
                int amount = resultMap.containsKey("amount") ? (Integer) resultMap.get("amount") : 1;
                result = new ItemStack(material, amount);
            }
        }
        
        if (result == null) {
            plugin.getLogger().warning("Tarif sonucu bulunamadı: " + recipeId);
            return null;
        }
        
        return new Recipe(recipeId, type, category, displayName, ingredients, result, level);
    }
    
    /**
     * Tarif kaydet
     */
    public void registerRecipe(Recipe recipe) {
        if (recipe == null || recipe.getRecipeId() == null) return;
        
        recipes.put(recipe.getRecipeId(), recipe);
        
        // Kategoriye göre ekle
        recipesByCategory.computeIfAbsent(recipe.getCategory(), k -> new ArrayList<>()).add(recipe);
        
        // Tipe göre ekle
        recipesByType.computeIfAbsent(recipe.getType(), k -> new ArrayList<>()).add(recipe);
    }
    
    /**
     * Tarifi dosyaya kaydet
     */
    public void saveRecipe(Recipe recipe) {
        if (recipe == null) return;
        
        // Dosya dizinini belirle
        File directory = getDirectoryForType(recipe.getType());
        File file = new File(directory, recipe.getRecipeId() + ".yml");
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            
            // Tarif bilgilerini kaydet
            config.set(recipe.getRecipeId() + ".type", recipe.getType().name());
            config.set(recipe.getRecipeId() + ".category", recipe.getCategory().name());
            config.set(recipe.getRecipeId() + ".displayName", recipe.getDisplayName());
            config.set(recipe.getRecipeId() + ".level", recipe.getLevel());
            
            // Malzemeleri kaydet
            List<Map<String, Object>> ingredientsList = new ArrayList<>();
            for (ItemStack ingredient : recipe.getIngredients()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("material", ingredient.getType().name());
                itemMap.put("amount", ingredient.getAmount());
                ingredientsList.add(itemMap);
            }
            config.set(recipe.getRecipeId() + ".ingredients", ingredientsList);
            
            // Sonucu kaydet
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("material", recipe.getResult().getType().name());
            resultMap.put("amount", recipe.getResult().getAmount());
            config.set(recipe.getRecipeId() + ".result", resultMap);
            
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Tarif kaydedilirken hata: " + recipe.getRecipeId(), e);
        }
    }
    
    /**
     * Tip'e göre dizin döndür
     */
    private File getDirectoryForType(RecipeType type) {
        switch (type) {
            case STRUCTURE:
                return new File(recipesDirectory, "structures");
            case BATTERY:
                return new File(recipesDirectory, "batteries");
            case RITUAL:
                return new File(recipesDirectory, "rituals");
            case CRAFTING:
            case FURNACE:
            case SMITHING:
            case BREWING:
            case ENCHANTING:
                return new File(recipesDirectory, "crafting");
            default:
                return new File(recipesDirectory, "other");
        }
    }
    
    /**
     * Tarif al (ID ile)
     */
    public Recipe getRecipe(String recipeId) {
        return recipes.get(recipeId);
    }
    
    /**
     * Kategoriye göre tarifler
     */
    public List<Recipe> getRecipesByCategory(RecipeCategory category) {
        return new ArrayList<>(recipesByCategory.getOrDefault(category, Collections.emptyList()));
    }
    
    /**
     * Tipe göre tarifler
     */
    public List<Recipe> getRecipesByType(RecipeType type) {
        return new ArrayList<>(recipesByType.getOrDefault(type, Collections.emptyList()));
    }
    
    /**
     * Tüm tarifler
     */
    public Collection<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes.values());
    }
    
    /**
     * Tarif var mı?
     */
    public boolean hasRecipe(String recipeId) {
        return recipes.containsKey(recipeId);
    }
    
    /**
     * Tarif sil
     */
    public void removeRecipe(String recipeId) {
        Recipe recipe = recipes.remove(recipeId);
        if (recipe != null) {
            recipesByCategory.getOrDefault(recipe.getCategory(), Collections.emptyList()).remove(recipe);
            recipesByType.getOrDefault(recipe.getType(), Collections.emptyList()).remove(recipe);
            
            // Dosyayı sil
            File directory = getDirectoryForType(recipe.getType());
            File file = new File(directory, recipeId + ".yml");
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    /**
     * Tüm tarifleri yeniden yükle
     */
    public void reload() {
        recipes.clear();
        recipesByCategory.clear();
        recipesByType.clear();
        loadAllRecipes();
    }
}

