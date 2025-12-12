package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.util.BlockRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Yapı Tarif Yönetim Sistemi
 * 
 * Sorumluluklar:
 * - Kod içi tarifleri yönetir
 * - Şema tariflerini yönetir
 * - Tarif doğrulama (sync ve async)
 * 
 * Factory Pattern: Tarif oluşturma için yardımcı metodlar
 * Thread-Safe: ConcurrentHashMap kullanır
 */
public class StructureRecipeManager {
    
    private final Main plugin;
    private final StructureValidator structureValidator;
    
    // Kod içi tarifler: Structure.Type -> BlockRecipe
    private final Map<Structure.Type, BlockRecipe> codeRecipes = new ConcurrentHashMap<>();
    
    // Şema tarifleri: Structure.Type -> Schematic Name
    private final Map<Structure.Type, String> schematicRecipes = new ConcurrentHashMap<>();
    
    public StructureRecipeManager(Main plugin) {
        this.plugin = plugin;
        this.structureValidator = new StructureValidator();
        
        // Tarifleri kaydet
        registerAllRecipes();
    }
    
    /**
     * Kod içi tarif kaydet
     */
    public void registerCodeRecipe(Structure.Type type, BlockRecipe recipe) {
        if (type == null || recipe == null) return;
        codeRecipes.put(type, recipe);
    }
    
    /**
     * Şema tarifi kaydet
     */
    public void registerSchematicRecipe(Structure.Type type, String schematicName) {
        if (type == null || schematicName == null) return;
        schematicRecipes.put(type, schematicName);
    }
    
    /**
     * Tarif var mı kontrol et
     */
    public boolean hasRecipe(Structure.Type type) {
        if (type == null) return false;
        return codeRecipes.containsKey(type) || schematicRecipes.containsKey(type);
    }
    
    /**
     * Kod içi tarif mi kontrol et
     */
    public boolean isCodeRecipe(Structure.Type type) {
        if (type == null) return false;
        return codeRecipes.containsKey(type);
    }
    
    /**
     * Şema tarifi mi kontrol et
     */
    public boolean isSchematicRecipe(Structure.Type type) {
        if (type == null) return false;
        return schematicRecipes.containsKey(type);
    }
    
    /**
     * Tarif doğrulama (sync - main thread'de çalışmalı)
     */
    public boolean validateStructure(Location coreLocation, Structure.Type type) {
        if (coreLocation == null || type == null) return false;
        
        // Önce kod içi tarif kontrolü
        BlockRecipe codeRecipe = codeRecipes.get(type);
        if (codeRecipe != null) {
            return codeRecipe.validate(coreLocation);
        }
        
        // Şema tarif kontrolü (sync - deprecated, async kullanılmalı)
        String schematicName = schematicRecipes.get(type);
        if (schematicName != null) {
            plugin.getLogger().warning("Sync şema doğrulama kullanılıyor, async kullanılmalı: " + type);
            return structureValidator.validate(coreLocation, schematicName);
        }
        
        return false;
    }
    
    /**
     * Tarif doğrulama (async - performanslı)
     */
    public void validateStructureAsync(Location coreLocation, Structure.Type type, Consumer<Boolean> callback) {
        if (coreLocation == null || type == null) {
            if (callback != null) callback.accept(false);
            return;
        }
        
        // Kod içi tarif kontrolü (hızlı, sync yapılabilir)
        BlockRecipe codeRecipe = codeRecipes.get(type);
        if (codeRecipe != null) {
            // Main thread'de yap (World API thread-safe değil)
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean result = codeRecipe.validate(coreLocation);
                if (callback != null) callback.accept(result);
            });
            return;
        }
        
        // Şema tarif kontrolü (async)
        String schematicName = schematicRecipes.get(type);
        if (schematicName != null) {
            structureValidator.validateAsync(coreLocation, schematicName, callback);
            return;
        }
        
        // Tarif bulunamadı
        if (callback != null) callback.accept(false);
    }
    
    /**
     * Tüm tarifleri kaydet (Factory Pattern)
     */
    private void registerAllRecipes() {
        // Basit yapılar - Kod içi tarifler
        
        // 1. Görev Loncası (PERSONAL_MISSION_GUILD)
        BlockRecipe missionGuildRecipe = BlockRecipe.builder("Görev Loncası")
            .setCore(Material.END_CRYSTAL)
            .addBlockBelow(Material.COBBLESTONE) // Altında kırıktaş
            .addBlockAbove(Material.LECTERN); // Üstünde kürsü
        registerCodeRecipe(Structure.Type.PERSONAL_MISSION_GUILD, missionGuildRecipe.build());
        
        // 2. Klan Bankası (CLAN_BANK)
        BlockRecipe bankRecipe = BlockRecipe.builder("Klan Bankası")
            .setCore(Material.END_CRYSTAL)
            .addBlockBelow(Material.GOLD_BLOCK) // Altında altın blok
            .addBlockAbove(Material.CHEST); // Üstünde sandık
        registerCodeRecipe(Structure.Type.CLAN_BANK, bankRecipe.build());
        
        // 3. Kontrat Bürosu (CONTRACT_OFFICE)
        BlockRecipe contractOfficeRecipe = BlockRecipe.builder("Kontrat Bürosu")
            .setCore(Material.END_CRYSTAL)
            .addBlockBelow(Material.STONE) // Altında taş
            .addBlockAbove(Material.CRAFTING_TABLE); // Üstünde masa
        registerCodeRecipe(Structure.Type.CONTRACT_OFFICE, contractOfficeRecipe.build());
        
        // 4. Klan Görev Loncası (CLAN_MISSION_GUILD)
        BlockRecipe clanMissionGuildRecipe = BlockRecipe.builder("Klan Görev Loncası")
            .setCore(Material.END_CRYSTAL)
            .addBlockBelow(Material.EMERALD_BLOCK) // Altında zümrüt blok
            .addBlockAbove(Material.LECTERN); // Üstünde kürsü
        registerCodeRecipe(Structure.Type.CLAN_MISSION_GUILD, clanMissionGuildRecipe.build());
        
        // 5. Market (MARKET_PLACE)
        BlockRecipe marketRecipe = BlockRecipe.builder("Market")
            .setCore(Material.END_CRYSTAL)
            .addBlockBelow(Material.COAL_BLOCK) // Altında kömür blok
            .addBlockAbove(Material.CHEST); // Üstünde sandık
        registerCodeRecipe(Structure.Type.MARKET_PLACE, marketRecipe.build());
        
        // 6. Tarif Kütüphanesi (RECIPE_LIBRARY)
        BlockRecipe recipeLibraryRecipe = BlockRecipe.builder("Tarif Kütüphanesi")
            .setCore(Material.END_CRYSTAL)
            .addBlockBelow(Material.BOOKSHELF) // Altında kitaplık
            .addBlockAbove(Material.LECTERN); // Üstünde kürsü
        registerCodeRecipe(Structure.Type.RECIPE_LIBRARY, recipeLibraryRecipe.build());
        
        // Karmaşık yapılar - Şema tarifleri (mevcut sistem)
        registerSchematicRecipe(Structure.Type.ALCHEMY_TOWER, "alchemy_tower");
        registerSchematicRecipe(Structure.Type.TECTONIC_STABILIZER, "tectonic_stabilizer");
        registerSchematicRecipe(Structure.Type.POISON_REACTOR, "poison_reactor");
        registerSchematicRecipe(Structure.Type.AUTO_TURRET, "auto_turret");
        registerSchematicRecipe(Structure.Type.GLOBAL_MARKET_GATE, "market_gate");
        
        plugin.getLogger().info("§aYapı tarifleri yüklendi: " + 
            (codeRecipes.size() + schematicRecipes.size()) + " tarif");
    }
    
    /**
     * Tarif bilgisi al (hata mesajları için)
     */
    public String getRecipeInfo(Structure.Type type) {
        if (type == null) return "Bilinmeyen yapı";
        
        BlockRecipe codeRecipe = codeRecipes.get(type);
        if (codeRecipe != null) {
            return codeRecipe.getRecipeName() + " (Kod içi tarif, " + 
                   codeRecipe.getRequirementCount() + " gereksinim)";
        }
        
        String schematicName = schematicRecipes.get(type);
        if (schematicName != null) {
            return type.name() + " (Şema tarifi: " + schematicName + ".schem)";
        }
        
        return "Tarif bulunamadı";
    }
}

