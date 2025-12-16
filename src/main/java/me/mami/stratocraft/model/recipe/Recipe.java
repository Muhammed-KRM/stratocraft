package me.mami.stratocraft.model.recipe;

import me.mami.stratocraft.enums.RecipeCategory;
import me.mami.stratocraft.enums.RecipeType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Tarif Veri Modeli
 * 
 * Tariflerin tüm verilerini tutar.
 */
public class Recipe extends BaseModel {
    private String recipeId; // Tarif ID'si
    private RecipeType type;
    private RecipeCategory category;
    private String displayName; // Tarif görünen adı
    private List<ItemStack> ingredients; // Malzemeler
    private ItemStack result; // Sonuç
    private int level; // Gerekli seviye
    private UUID unlockerId; // Tarifi açan oyuncu (null ise herkes kullanabilir)
    private boolean isUnlocked; // Açıldı mı?
    private long unlockTime; // Açılma zamanı
    
    public Recipe(String recipeId, RecipeType type, RecipeCategory category, 
                 String displayName, List<ItemStack> ingredients, ItemStack result, int level) {
        super();
        this.recipeId = recipeId;
        this.type = type;
        this.category = category;
        this.displayName = displayName;
        this.ingredients = ingredients;
        this.result = result;
        this.level = level;
        this.unlockerId = null;
        this.isUnlocked = false;
    }
    
    public Recipe(UUID id, String recipeId, RecipeType type, RecipeCategory category,
                 String displayName, List<ItemStack> ingredients, ItemStack result, int level) {
        super(id);
        this.recipeId = recipeId;
        this.type = type;
        this.category = category;
        this.displayName = displayName;
        this.ingredients = ingredients;
        this.result = result;
        this.level = level;
        this.unlockerId = null;
        this.isUnlocked = false;
    }
    
    // Getters
    public String getRecipeId() { return recipeId; }
    public RecipeType getType() { return type; }
    public RecipeCategory getCategory() { return category; }
    public String getDisplayName() { return displayName; }
    public List<ItemStack> getIngredients() { return ingredients; }
    public ItemStack getResult() { return result; }
    public int getLevel() { return level; }
    public UUID getUnlockerId() { return unlockerId; }
    public boolean isUnlocked() { return isUnlocked; }
    public long getUnlockTime() { return unlockTime; }
    
    // Setters
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; updateTimestamp(); }
    public void setType(RecipeType type) { this.type = type; updateTimestamp(); }
    public void setCategory(RecipeCategory category) { this.category = category; updateTimestamp(); }
    public void setDisplayName(String displayName) { this.displayName = displayName; updateTimestamp(); }
    public void setIngredients(List<ItemStack> ingredients) { this.ingredients = ingredients; updateTimestamp(); }
    public void setResult(ItemStack result) { this.result = result; updateTimestamp(); }
    public void setLevel(int level) { this.level = level; updateTimestamp(); }
    public void setUnlockerId(UUID unlockerId) { this.unlockerId = unlockerId; updateTimestamp(); }
    public void setUnlocked(boolean unlocked) { 
        this.isUnlocked = unlocked; 
        if (unlocked) {
            this.unlockTime = System.currentTimeMillis();
        }
        updateTimestamp(); 
    }
}

