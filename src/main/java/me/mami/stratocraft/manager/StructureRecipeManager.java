package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.StructureType;
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
    
    // Kod içi tarifler: StructureType -> BlockRecipe (YENİ)
    private final Map<StructureType, BlockRecipe> codeRecipes = new ConcurrentHashMap<>();
    
    // Şema tarifleri: StructureType -> Schematic Name (YENİ)
    private final Map<StructureType, String> schematicRecipes = new ConcurrentHashMap<>();
    
    // GERİYE UYUMLULUK: Eski Structure.Type desteği
    @Deprecated
    private final Map<Structure.Type, BlockRecipe> legacyCodeRecipes = new ConcurrentHashMap<>();
    @Deprecated
    private final Map<Structure.Type, String> legacySchematicRecipes = new ConcurrentHashMap<>();
    
    public StructureRecipeManager(Main plugin) {
        this.plugin = plugin;
        this.structureValidator = new StructureValidator();
        
        // Tarifleri kaydet
        registerAllRecipes();
    }
    
    /**
     * Kod içi tarif kaydet (YENİ: StructureType)
     */
    public void registerCodeRecipe(StructureType type, BlockRecipe recipe) {
        if (type == null || recipe == null) return;
        codeRecipes.put(type, recipe);
    }
    
    /**
     * Şema tarifi kaydet (YENİ: StructureType)
     */
    public void registerSchematicRecipe(StructureType type, String schematicName) {
        if (type == null || schematicName == null) return;
        schematicRecipes.put(type, schematicName);
    }
    
    /**
     * Kod içi tarif kaydet (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public void registerCodeRecipe(Structure.Type type, BlockRecipe recipe) {
        if (type == null || recipe == null) return;
        legacyCodeRecipes.put(type, recipe);
        // Yeni enum'a da kaydet
        try {
            StructureType newType = StructureType.valueOf(type.name());
            codeRecipes.put(newType, recipe);
        } catch (IllegalArgumentException e) {
            // Eski enum'da yeni enum'da olmayan bir tip varsa
        }
    }
    
    /**
     * Şema tarifi kaydet (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public void registerSchematicRecipe(Structure.Type type, String schematicName) {
        if (type == null || schematicName == null) return;
        legacySchematicRecipes.put(type, schematicName);
        // Yeni enum'a da kaydet
        try {
            StructureType newType = StructureType.valueOf(type.name());
            schematicRecipes.put(newType, schematicName);
        } catch (IllegalArgumentException e) {
            // Eski enum'da yeni enum'da olmayan bir tip varsa
        }
    }
    
    /**
     * Tarif var mı kontrol et (YENİ: StructureType)
     */
    public boolean hasRecipe(StructureType type) {
        if (type == null) return false;
        return codeRecipes.containsKey(type) || schematicRecipes.containsKey(type);
    }
    
    /**
     * Kod içi tarif mi kontrol et (YENİ: StructureType)
     */
    public boolean isCodeRecipe(StructureType type) {
        if (type == null) return false;
        return codeRecipes.containsKey(type);
    }
    
    /**
     * Şema tarifi mi kontrol et (YENİ: StructureType)
     */
    public boolean isSchematicRecipe(StructureType type) {
        if (type == null) return false;
        return schematicRecipes.containsKey(type);
    }
    
    /**
     * Tarif var mı kontrol et (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public boolean hasRecipe(Structure.Type type) {
        if (type == null) return false;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return hasRecipe(newType);
        } catch (IllegalArgumentException e) {
            return legacyCodeRecipes.containsKey(type) || legacySchematicRecipes.containsKey(type);
        }
    }
    
    /**
     * Kod içi tarif mi kontrol et (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public boolean isCodeRecipe(Structure.Type type) {
        if (type == null) return false;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return isCodeRecipe(newType);
        } catch (IllegalArgumentException e) {
            return legacyCodeRecipes.containsKey(type);
        }
    }
    
    /**
     * Şema tarifi mi kontrol et (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public boolean isSchematicRecipe(Structure.Type type) {
        if (type == null) return false;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return isSchematicRecipe(newType);
        } catch (IllegalArgumentException e) {
            return legacySchematicRecipes.containsKey(type);
        }
    }
    
    /**
     * Tarif doğrulama (sync - main thread'de çalışmalı) (YENİ: StructureType)
     */
    public boolean validateStructure(Location coreLocation, StructureType type) {
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
     * Kod içi tarif al (YENİ: StructureType)
     */
    public BlockRecipe getCodeRecipe(StructureType type) {
        if (type == null) return null;
        return codeRecipes.get(type);
    }
    
    /**
     * Tariften yapıyı oluştur (blokları yerleştir) (YENİ: StructureType)
     * 
     * @param coreLocation Çekirdek bloğunun konumu
     * @param type Yapı tipi
     * @return Başarılıysa true
     */
    public boolean buildFromRecipe(Location coreLocation, StructureType type) {
        if (coreLocation == null || type == null) return false;
        
        BlockRecipe codeRecipe = codeRecipes.get(type);
        if (codeRecipe != null) {
            codeRecipe.build(coreLocation);
            return true;
        }
        
        return false;
    }
    
    /**
     * Tarif doğrulama (async - performanslı) (YENİ: StructureType)
     */
    public void validateStructureAsync(Location coreLocation, StructureType type, Consumer<Boolean> callback) {
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
     * Tarif doğrulama (sync - main thread'de çalışmalı) (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public boolean validateStructure(Location coreLocation, Structure.Type type) {
        if (coreLocation == null || type == null) return false;
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return validateStructure(coreLocation, newType);
        } catch (IllegalArgumentException e) {
            // Eski sistem
            BlockRecipe codeRecipe = legacyCodeRecipes.get(type);
            if (codeRecipe != null) {
                return codeRecipe.validate(coreLocation);
            }
            String schematicName = legacySchematicRecipes.get(type);
            if (schematicName != null) {
                plugin.getLogger().warning("Sync şema doğrulama kullanılıyor, async kullanılmalı: " + type);
                return structureValidator.validate(coreLocation, schematicName);
            }
            return false;
        }
    }
    
    /**
     * Tarif doğrulama (async - performanslı) (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public void validateStructureAsync(Location coreLocation, Structure.Type type, Consumer<Boolean> callback) {
        if (coreLocation == null || type == null) {
            if (callback != null) callback.accept(false);
            return;
        }
        try {
            StructureType newType = StructureType.valueOf(type.name());
            validateStructureAsync(coreLocation, newType, callback);
        } catch (IllegalArgumentException e) {
            // Eski sistem
            BlockRecipe codeRecipe = legacyCodeRecipes.get(type);
            if (codeRecipe != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    boolean result = codeRecipe.validate(coreLocation);
                    if (callback != null) callback.accept(result);
                });
                return;
            }
            String schematicName = legacySchematicRecipes.get(type);
            if (schematicName != null) {
                structureValidator.validateAsync(coreLocation, schematicName, callback);
                return;
            }
            if (callback != null) callback.accept(false);
        }
    }
    
    /**
     * Tüm tarifleri kaydet (Factory Pattern)
     */
    private void registerAllRecipes() {
        // Basit yapılar - Kod içi tarifler (YENİ: StructureType)
        
        // 1. Görev Loncası (PERSONAL_MISSION_GUILD)
        BlockRecipe missionGuildRecipe = BlockRecipe.builder("Görev Loncası")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor
            .addBlockBelow(Material.COBBLESTONE)
            .addBlockAbove(Material.LECTERN)
            .build();
        registerCodeRecipe(StructureType.PERSONAL_MISSION_GUILD, missionGuildRecipe);
        
        // 2. Klan Bankası (CLAN_BANK)
        BlockRecipe bankRecipe = BlockRecipe.builder("Klan Bankası")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor
            .addBlockBelow(Material.GOLD_BLOCK)
            .addBlockAbove(Material.CHEST)
            .build();
        registerCodeRecipe(StructureType.CLAN_BANK, bankRecipe);
        
        // 3. Kontrat Bürosu (CONTRACT_OFFICE)
        BlockRecipe contractOfficeRecipe = BlockRecipe.builder("Kontrat Bürosu")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor
            .addBlockBelow(Material.STONE)
            .addBlockAbove(Material.CRAFTING_TABLE)
            .build();
        registerCodeRecipe(StructureType.CONTRACT_OFFICE, contractOfficeRecipe);
        
        // 4. Klan Görev Loncası (CLAN_MISSION_GUILD)
        BlockRecipe clanMissionGuildRecipe = BlockRecipe.builder("Klan Görev Loncası")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor
            .addBlockBelow(Material.EMERALD_BLOCK)
            .addBlockAbove(Material.LECTERN)
            .build();
        registerCodeRecipe(StructureType.CLAN_MISSION_GUILD, clanMissionGuildRecipe);
        
        // 5. Market (MARKET_PLACE)
        BlockRecipe marketRecipe = BlockRecipe.builder("Market")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor
            .addBlockBelow(Material.COAL_BLOCK)
            .addBlockAbove(Material.CHEST)
            .build();
        registerCodeRecipe(StructureType.MARKET_PLACE, marketRecipe);
        
        // 6. Tarif Kütüphanesi (RECIPE_LIBRARY)
        BlockRecipe recipeLibraryRecipe = BlockRecipe.builder("Tarif Kütüphanesi")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor
            .addBlockBelow(Material.BOOKSHELF)
            .addBlockAbove(Material.LECTERN)
            .build();
        registerCodeRecipe(StructureType.RECIPE_LIBRARY, recipeLibraryRecipe);
        
        // 7. Klan Yönetim Merkezi (CLAN_MANAGEMENT_CENTER) - YENİ: Oak Log core
        BlockRecipe clanManagementCenterRecipe = BlockRecipe.builder("Klan Yönetim Merkezi")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor // YENİ: Yapı çekirdeği
            .addBlockAbove(Material.BEACON) // Üstünde Beacon
            .addBlock(-1, -1, -1, Material.IRON_BLOCK)
            .addBlock(-1, -1, 0, Material.IRON_BLOCK)
            .addBlock(-1, -1, 1, Material.IRON_BLOCK)
            .addBlock(0, -1, -1, Material.IRON_BLOCK)
            .addBlock(0, -1, 1, Material.IRON_BLOCK)
            .addBlock(1, -1, -1, Material.IRON_BLOCK)
            .addBlock(1, -1, 0, Material.IRON_BLOCK)
            .addBlock(1, -1, 1, Material.IRON_BLOCK)
            .build();
        registerCodeRecipe(StructureType.CLAN_MANAGEMENT_CENTER, clanManagementCenterRecipe);
        
        // 8. Eğitim Alanı (TRAINING_ARENA) - YENİ: Oak Log core
        BlockRecipe trainingArenaRecipe = BlockRecipe.builder("Eğitim Alanı")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor // YENİ: Yapı çekirdeği
            .addBlockAbove(Material.ENCHANTING_TABLE) // Üstünde Enchanting Table
            .addBlock(0, -1, 0, Material.IRON_BLOCK)
            .addBlock(0, -1, 1, Material.IRON_BLOCK)
            .addBlock(1, -1, 0, Material.IRON_BLOCK)
            .addBlock(1, -1, 1, Material.IRON_BLOCK)
            .build();
        registerCodeRecipe(StructureType.TRAINING_ARENA, trainingArenaRecipe);
        
        // 9. Kervan İstasyonu (CARAVAN_STATION) - YENİ: Oak Log core
        BlockRecipe caravanStationRecipe = BlockRecipe.builder("Kervan İstasyonu")
            .setCore(Material.OAK_LOG) // YENİ: Yapı çekirdeği LOG kullanıyor // YENİ: Yapı çekirdeği
            .addBlockAbove(Material.CHEST) // Üstünde Chest
            .addBlock(0, -1, 0, Material.IRON_BLOCK)
            .addBlock(0, -1, 1, Material.IRON_BLOCK)
            .addBlock(1, -1, 0, Material.IRON_BLOCK)
            .addBlock(1, -1, 1, Material.IRON_BLOCK)
            .build();
        registerCodeRecipe(StructureType.CARAVAN_STATION, caravanStationRecipe);
        
        // Karmaşık yapılar - Şema tarifleri (YENİ: StructureType)
        registerSchematicRecipe(StructureType.ALCHEMY_TOWER, "alchemy_tower");
        registerSchematicRecipe(StructureType.TECTONIC_STABILIZER, "tectonic_stabilizer");
        registerSchematicRecipe(StructureType.POISON_REACTOR, "poison_reactor");
        registerSchematicRecipe(StructureType.AUTO_TURRET, "auto_turret");
        registerSchematicRecipe(StructureType.GLOBAL_MARKET_GATE, "market_gate");
        
        plugin.getLogger().info("§aYapı tarifleri yüklendi: " + 
            (codeRecipes.size() + schematicRecipes.size()) + " tarif");
    }
    
    /**
     * Tarif bilgisi al (hata mesajları için) (YENİ: StructureType)
     */
    public String getRecipeInfo(StructureType type) {
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
    
    /**
     * Tarif bilgisi al (GERİYE UYUMLULUK: Structure.Type)
     * @deprecated StructureType kullanın
     */
    @Deprecated
    public String getRecipeInfo(Structure.Type type) {
        if (type == null) return "Bilinmeyen yapı";
        try {
            StructureType newType = StructureType.valueOf(type.name());
            return getRecipeInfo(newType);
        } catch (IllegalArgumentException e) {
            BlockRecipe codeRecipe = legacyCodeRecipes.get(type);
            if (codeRecipe != null) {
                return codeRecipe.getRecipeName() + " (Kod içi tarif, " + 
                       codeRecipe.getRequirementCount() + " gereksinim)";
            }
            String schematicName = legacySchematicRecipes.get(type);
            if (schematicName != null) {
                return type.name() + " (Şema tarifi: " + schematicName + ".schem)";
            }
            return "Tarif bulunamadı";
        }
    }
}

