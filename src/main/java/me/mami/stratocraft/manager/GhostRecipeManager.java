package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * GhostRecipeManager - Hayalet tarif sistemi
 * Oyuncular tarif kitaplarına baktığında hayalet yapılar gösterir
 */
public class GhostRecipeManager {
    
    /**
     * GhostRecipeData - Tarif verisi (blokların konumları ve tipleri)
     */
    public static class GhostRecipeData {
        private final Map<Vector, Material> blocks = new HashMap<>();
        
        public void addBlock(Vector pos, Material material) {
            blocks.put(pos, material);
        }
        
        public Map<Vector, Material> getBlocks() {
            return blocks;
        }
    }
    
    /**
     * GhostRecipe - Aktif hayalet tarif (oyuncuya gösterilen)
     */
    public static class GhostRecipe {
        private final String recipeId;
        private final Location baseLocation;
        private final Map<Location, ArmorStand> ghostBlocks = new HashMap<>();
        private final GhostRecipeData data;
        
        public GhostRecipe(String recipeId, Location baseLocation, GhostRecipeData data) {
            this.recipeId = recipeId;
            this.baseLocation = baseLocation;
            this.data = data;
        }
        
        public String getRecipeId() { return recipeId; }
        public Location getBaseLocation() { return baseLocation; }
        public Map<Location, ArmorStand> getGhostBlocks() { return ghostBlocks; }
        public GhostRecipeData getData() { return data; }
    }
    // Oyuncu UUID -> Aktif hayalet tarif verisi
    private final Map<UUID, GhostRecipe> activeGhostRecipes = new HashMap<>();
    
    // Sabit tarifler (yer tıklayınca sabit kalır)
    private final Map<Location, GhostRecipe> fixedGhostRecipes = new HashMap<>();
    
    // Tarif ID -> Tarif verisi (blokların konumları ve tipleri)
    private final Map<String, GhostRecipeData> recipeData = new HashMap<>();
    
    public GhostRecipeManager() {
        initializeRecipeData();
    }
    
    /**
     * Tarif verilerini başlat (yapılar, bataryalar, ritüeller)
     */
    private void initializeRecipeData() {
        // ========== YAPILAR ==========
        
        // Simya Kulesi (ALCHEMY_TOWER)
        GhostRecipeData alchemyTower = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                alchemyTower.addBlock(new Vector(x, -1, z), Material.COBBLESTONE);
            }
        }
        alchemyTower.addBlock(new Vector(0, 0, 0), Material.ENCHANTING_TABLE);
        recipeData.put("ALCHEMY", alchemyTower);
        recipeData.put("ALCHEMY_TOWER", alchemyTower);
        
        // Tektonik Sabitleyici (TECTONIC_STABILIZER)
        GhostRecipeData tectonic = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                tectonic.addBlock(new Vector(x, 0, z), Material.OBSIDIAN);
            }
        }
        tectonic.addBlock(new Vector(0, 0, 0), Material.PISTON);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                tectonic.addBlock(new Vector(x, -1, z), Material.STONE);
            }
        }
        recipeData.put("TECTONIC", tectonic);
        recipeData.put("TECTONIC_STABILIZER", tectonic);
        
        // Zehir Reaktörü (POISON_REACTOR)
        GhostRecipeData poisonReactor = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                poisonReactor.addBlock(new Vector(x, -1, z), Material.GREEN_CONCRETE);
            }
        }
        poisonReactor.addBlock(new Vector(0, 0, 0), Material.BEACON);
        recipeData.put("POISON_REACTOR", poisonReactor);
        
        // Şifa Kulesi (HEALING_BEACON)
        GhostRecipeData healingBeacon = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                healingBeacon.addBlock(new Vector(x, -1, z), Material.QUARTZ_BLOCK);
            }
        }
        healingBeacon.addBlock(new Vector(0, 0, 0), Material.LANTERN);
        recipeData.put("HEALING_BEACON", healingBeacon);
        
        // Global Pazar Kapısı (GLOBAL_MARKET_GATE)
        GhostRecipeData marketGate = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                marketGate.addBlock(new Vector(x, -1, z), Material.GOLD_BLOCK);
            }
        }
        marketGate.addBlock(new Vector(0, 0, 0), Material.ENDER_CHEST);
        recipeData.put("GLOBAL_MARKET_GATE", marketGate);
        
        // Otomatik Taret (AUTO_TURRET)
        GhostRecipeData autoTurret = new GhostRecipeData();
        autoTurret.addBlock(new Vector(0, -1, 0), Material.IRON_BLOCK);
        autoTurret.addBlock(new Vector(0, 0, 0), Material.DISPENSER);
        recipeData.put("AUTO_TURRET", autoTurret);
        
        // Diğer yapılar için basit desenler (ileride detaylandırılabilir)
        addSimpleStructureRecipe("SIEGE_FACTORY", Material.DISPENSER, Material.IRON_BLOCK);
        addSimpleStructureRecipe("WALL_GENERATOR", Material.PISTON, Material.STONE);
        addSimpleStructureRecipe("GRAVITY_WELL", Material.BEACON, Material.IRON_BLOCK);
        addSimpleStructureRecipe("LAVA_TRENCHER", Material.DISPENSER, Material.LAVA_BUCKET);
        addSimpleStructureRecipe("WATCHTOWER", Material.BEACON, Material.IRON_BLOCK);
        addSimpleStructureRecipe("DRONE_STATION", Material.DISPENSER, Material.IRON_BLOCK);
        addSimpleStructureRecipe("AUTO_DRILL", Material.DISPENSER, Material.IRON_BLOCK);
        addSimpleStructureRecipe("XP_BANK", Material.ENCHANTING_TABLE, Material.EXPERIENCE_BOTTLE);
        addSimpleStructureRecipe("MAG_RAIL", Material.POWERED_RAIL, Material.IRON_BLOCK);
        addSimpleStructureRecipe("TELEPORTER", Material.END_PORTAL_FRAME, Material.ENDER_PEARL);
        addSimpleStructureRecipe("FOOD_SILO", Material.CHEST, Material.ICE);
        addSimpleStructureRecipe("OIL_REFINERY", Material.BLAST_FURNACE, Material.COAL_BLOCK);
        addSimpleStructureRecipe("WEATHER_MACHINE", Material.BEACON, Material.LIGHTNING_ROD);
        addSimpleStructureRecipe("CROP_ACCELERATOR", Material.BEACON, Material.HAY_BLOCK);
        addSimpleStructureRecipe("MOB_GRINDER", Material.DISPENSER, Material.IRON_BLOCK);
        addSimpleStructureRecipe("INVISIBILITY_CLOAK", Material.BEACON, Material.GLASS);
        addSimpleStructureRecipe("ARMORY", Material.CHEST, Material.IRON_BLOCK);
        addSimpleStructureRecipe("LIBRARY", Material.LECTERN, Material.BOOKSHELF);
        addSimpleStructureRecipe("WARNING_SIGN", Material.OAK_SIGN, Material.REDSTONE_BLOCK);
        
        // ========== ÖZEL EŞYALAR ==========
        // Eşyalar için tarif kitapları sadece bilgilendirme amaçlı, hayalet blok göstermez
        // Ama yine de basit desenler ekleyebiliriz
        
        // ========== BATARYALAR (75 Batarya) ==========
        // Eski batarya (geriye dönük uyumluluk)
        GhostRecipeData magmaBattery = new GhostRecipeData();
        magmaBattery.addBlock(new Vector(0, 0, 0), Material.MAGMA_BLOCK);
        magmaBattery.addBlock(new Vector(0, -1, 0), Material.MAGMA_BLOCK);
        recipeData.put("MAGMA_BATTERY", magmaBattery);
        
        // Yeni 75 batarya için hayalet tarifleri NewBatteryManager'dan alınacak
        // initializeBatteryRecipes() metodu Main.java'da çağrılacak
        
        // ========== RİTÜELLER ==========
        GhostRecipeData clanCreate = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                clanCreate.addBlock(new Vector(x, -1, z), Material.GOLD_BLOCK);
            }
        }
        clanCreate.addBlock(new Vector(0, 0, 0), Material.BEACON);
        recipeData.put("CLAN_CREATE", clanCreate);
        
        // Diğer ritüeller için basit desenler
        addSimpleStructureRecipe("CLAN_UPGRADE", Material.BEACON, Material.DIAMOND_BLOCK);
        addSimpleStructureRecipe("CLAN_DISBAND", Material.BEACON, Material.TNT);
    }
    
    /**
     * Basit yapı tarifi ekle (merkez blok + alt blok)
     */
    private void addSimpleStructureRecipe(String id, Material center, Material base) {
        GhostRecipeData data = new GhostRecipeData();
        data.addBlock(new Vector(0, 0, 0), center);
        data.addBlock(new Vector(0, -1, 0), base);
        recipeData.put(id, data);
    }
    
    /**
     * Tarif verisini al
     */
    public GhostRecipeData getRecipeData(String recipeId) {
        return recipeData.get(recipeId);
    }
    
    /**
     * Aktif hayalet tarif ekle
     */
    public void addActiveGhostRecipe(UUID playerId, GhostRecipe recipe) {
        activeGhostRecipes.put(playerId, recipe);
    }
    
    /**
     * Aktif hayalet tarifi kaldır
     */
    public void removeActiveGhostRecipe(UUID playerId) {
        activeGhostRecipes.remove(playerId);
    }
    
    /**
     * Aktif hayalet tarifi al
     */
    public GhostRecipe getActiveGhostRecipe(UUID playerId) {
        return activeGhostRecipes.get(playerId);
    }
    
    /**
     * Sabit hayalet tarif ekle
     */
    public void addFixedGhostRecipe(Location loc, GhostRecipe recipe) {
        fixedGhostRecipes.put(loc, recipe);
    }
    
    /**
     * Sabit hayalet tarifi kaldır
     */
    public void removeFixedGhostRecipe(Location loc) {
        fixedGhostRecipes.remove(loc);
    }
    
    /**
     * Sabit hayalet tarifi al
     */
    public GhostRecipe getFixedGhostRecipe(Location loc) {
        return fixedGhostRecipes.get(loc);
    }
    
    /**
     * NewBatteryManager'dan 75 batarya için hayalet tarifleri ekle
     * Bu metod Main.java'da NewBatteryManager oluşturulduktan sonra çağrılmalı
     */
    public void initializeBatteryRecipes(NewBatteryManager batteryManager) {
        if (batteryManager == null) return;
        
        // Tüm RecipeChecker'ları al
        Map<String, NewBatteryManager.RecipeChecker> recipeCheckers = batteryManager.getAllRecipeCheckers();
        
        // Kategoriye göre sayaçlar
        Map<String, Integer> categoryCounters = new HashMap<>();
        categoryCounters.put("ATTACK", 1);
        categoryCounters.put("CONSTRUCTION", 1);
        categoryCounters.put("SUPPORT", 1);
        
        for (Map.Entry<String, NewBatteryManager.RecipeChecker> entry : recipeCheckers.entrySet()) {
            String batteryName = entry.getKey();
            NewBatteryManager.RecipeChecker checker = entry.getValue();
            
            // BlockPattern'i al
            NewBatteryManager.BlockPattern pattern = checker.getPattern();
            if (pattern == null) continue;
            
            // GhostRecipeData oluştur
            GhostRecipeData data = new GhostRecipeData();
            
            // Merkez blok
            data.addBlock(new Vector(0, 0, 0), pattern.getCenterBlock());
            
            // Diğer bloklar
            for (Map.Entry<NewBatteryManager.BlockPosition, Material> blockEntry : pattern.getRequiredBlocks().entrySet()) {
                NewBatteryManager.BlockPosition pos = blockEntry.getKey();
                Material mat = blockEntry.getValue();
                data.addBlock(new Vector(pos.getX(), pos.getY(), pos.getZ()), mat);
            }
            
            // BatteryManager'dan kategoriyi al
            BatteryManager.BatteryCategory category = getBatteryCategory(batteryName);
            String categoryStr = category.name();
            int batteryNum = categoryCounters.get(categoryStr);
            categoryCounters.put(categoryStr, batteryNum + 1);
            
            // Recipe ID oluştur
            String recipeId = "BATTERY_" + categoryStr + "_L" + checker.getLevel() + "_" + batteryNum;
            
            // Tarif verisini kaydet
            recipeData.put(recipeId, data);
            
            // Ayrıca batarya ismi ile de kaydet (alternatif erişim)
            recipeData.put(batteryName.toUpperCase().replace(" ", "_"), data);
        }
    }
    
    /**
     * NewMineManager'dan 25 mayın için hayalet tarifleri ekle
     * Bu metod Main.java'da NewMineManager oluşturulduktan sonra çağrılmalı
     */
    public void initializeMineRecipes(NewMineManager mineManager) {
        if (mineManager == null) return;
        
        // Tüm mayın tiplerini al
        for (NewMineManager.MineType mineType : NewMineManager.MineType.values()) {
            String mineName = mineType.name();
            int level = mineType.getLevel();
            
            // Mayınlar için basit hayalet tarif (basınç plakası + seviyeye göre blok)
            GhostRecipeData data = new GhostRecipeData();
            
            // Basınç plakası (merkez)
            Material pressurePlate = getPressurePlateForLevel(level);
            data.addBlock(new Vector(0, 0, 0), pressurePlate);
            
            // Seviyeye göre alt blok
            Material baseBlock = getBaseBlockForLevel(level);
            data.addBlock(new Vector(0, -1, 0), baseBlock);
            
            // Recipe ID oluştur
            String recipeId = "RECIPE_MINE_" + mineName;
            
            // Tarif verisini kaydet
            recipeData.put(recipeId, data);
        }
        
        // Gizleme aleti için basit tarif
        GhostRecipeData concealerData = new GhostRecipeData();
        concealerData.addBlock(new Vector(0, 0, 0), Material.SPYGLASS);
        recipeData.put("RECIPE_MINE_CONCEALER", concealerData);
    }
    
    /**
     * Seviyeye göre basınç plakası tipi
     */
    private Material getPressurePlateForLevel(int level) {
        switch (level) {
            case 1: return Material.STONE_PRESSURE_PLATE;
            case 2: return Material.OAK_PRESSURE_PLATE;
            case 3: return Material.POLISHED_BLACKSTONE_PRESSURE_PLATE;
            case 4: return Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
            case 5: return Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
            default: return Material.STONE_PRESSURE_PLATE;
        }
    }
    
    /**
     * Seviyeye göre alt blok tipi
     */
    private Material getBaseBlockForLevel(int level) {
        switch (level) {
            case 1: return Material.COBBLESTONE;
            case 2: return Material.STONE;
            case 3: return Material.IRON_BLOCK;
            case 4: return Material.DIAMOND_BLOCK;
            case 5: return Material.NETHERITE_BLOCK;
            default: return Material.COBBLESTONE;
        }
    }
    
    /**
     * Batarya isminden kategoriyi belirle (BatteryManager'dan)
     */
    private BatteryManager.BatteryCategory getBatteryCategory(String batteryName) {
        // BatteryManager'daki BatteryType enum'undan kategoriyi bul
        for (BatteryManager.BatteryType type : BatteryManager.BatteryType.values()) {
            if (type.getDisplayName().equals(batteryName)) {
                return type.getCategory();
            }
        }
        
        // Fallback: İsimden kategori tahmin et
        if (batteryName.contains("Yıldırım") || batteryName.contains("Cehennem") || 
            batteryName.contains("Buz") || batteryName.contains("Zehir") || 
            batteryName.contains("Şok") || batteryName.contains("Çift") ||
            batteryName.contains("Zincir") || batteryName.contains("Asit") ||
            batteryName.contains("Elektrik") || batteryName.contains("Meteor") ||
            batteryName.contains("Tesla") || batteryName.contains("Ölüm") ||
            batteryName.contains("Kıyamet") || batteryName.contains("Lava") ||
            batteryName.contains("Boss") || batteryName.contains("Alan") ||
            batteryName.contains("Dağ")) {
            return BatteryManager.BatteryCategory.ATTACK;
        } else if (batteryName.contains("Köprü") || batteryName.contains("Duvar") || 
                   batteryName.contains("Kafes") || batteryName.contains("Kale") ||
                   batteryName.contains("Hapishane") || batteryName.contains("Kule") ||
                   batteryName.contains("Şato") || batteryName.contains("Barikat") ||
                   batteryName.contains("Tünel")) {
            return BatteryManager.BatteryCategory.CONSTRUCTION;
        } else {
            return BatteryManager.BatteryCategory.SUPPORT;
        }
    }
    
    /**
     * Aktif tarif var mı?
     */
    public boolean hasActiveRecipe(UUID playerId) {
        return activeGhostRecipes.containsKey(playerId);
    }
    
    /**
     * Oyuncunun aktif tarifini kaldır
     */
    public void removeGhostRecipe(Player player) {
        removeActiveGhostRecipe(player.getUniqueId());
    }
    
    /**
     * Hayalet tarifi göster
     */
    public void showGhostRecipe(Player player, String recipeId, Location baseLocation) {
        GhostRecipeData data = getRecipeData(recipeId);
        if (data == null) {
            player.sendMessage("§cTarif bulunamadı: " + recipeId);
            return;
        }
        
        // Eski tarifi kaldır
        if (hasActiveRecipe(player.getUniqueId())) {
            removeActiveGhostRecipe(player.getUniqueId());
        }
        
        // Yeni tarif oluştur
        GhostRecipe recipe = new GhostRecipe(recipeId, baseLocation, data);
        
        // Hayalet blokları oluştur
        for (Map.Entry<Vector, Material> entry : data.getBlocks().entrySet()) {
            Vector offset = entry.getKey();
            Material material = entry.getValue();
            Location blockLoc = baseLocation.clone().add(offset);
            
            // ArmorStand ile hayalet blok göster
            ArmorStand stand = (ArmorStand) baseLocation.getWorld().spawnEntity(
                blockLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setCustomNameVisible(false);
            stand.setMarker(true);
            
            // Blok görünümü için ArmorStand'a blok itemı ver
            org.bukkit.inventory.ItemStack blockItem = new org.bukkit.inventory.ItemStack(material);
            stand.getEquipment().setHelmet(blockItem);
            
            recipe.getGhostBlocks().put(blockLoc, stand);
        }
        
        // Aktif tarif olarak kaydet
        addActiveGhostRecipe(player.getUniqueId(), recipe);
    }
    
    /**
     * Mesafe kontrolü (oyuncu çok uzaklaşırsa tarifi kaldır)
     */
    public void checkDistance(Player player) {
        GhostRecipe recipe = getActiveGhostRecipe(player.getUniqueId());
        if (recipe == null) return;
        
        Location baseLoc = recipe.getBaseLocation();
        double distance = player.getLocation().distance(baseLoc);
        
        if (distance > 50) { // 50 bloktan uzaksa kaldır
            removeActiveGhostRecipe(player.getUniqueId());
            player.sendMessage("§7Hayalet tarif kaldırıldı (çok uzaklaştınız).");
        }
    }
    
    /**
     * Blok koyulduğunda kontrol et ve doğru blok ise hayalet görüntüsünü kaldır
     */
    public void checkAndRemoveBlock(Player player, Location blockLocation, Material placedMaterial) {
        // Aktif tarif kontrolü
        GhostRecipe activeRecipe = getActiveGhostRecipe(player.getUniqueId());
        if (activeRecipe != null) {
            checkAndRemoveBlockFromRecipe(player, activeRecipe, blockLocation, placedMaterial, true);
        }
        
        // Sabit tarif kontrolü
        GhostRecipe fixedRecipe = getFixedGhostRecipe(blockLocation);
        if (fixedRecipe != null) {
            checkAndRemoveBlockFromRecipe(player, fixedRecipe, blockLocation, placedMaterial, false);
        }
    }
    
    /**
     * Tariften blok kontrolü
     */
    private void checkAndRemoveBlockFromRecipe(Player player, GhostRecipe recipe, 
            Location blockLocation, Material placedMaterial, boolean isActive) {
        GhostRecipeData data = recipe.getData();
        Location baseLoc = recipe.getBaseLocation();
        
        // Blok konumunu baseLocation'a göre offset'e çevir
        Vector offset = blockLocation.toVector().subtract(baseLoc.toVector());
        
        // Bu konumda blok var mı?
        Material requiredMaterial = data.getBlocks().get(offset);
        if (requiredMaterial == null) return;
        
        // Doğru blok mu?
        if (requiredMaterial != placedMaterial) return;
        
        // Hayalet bloku kaldır
        ArmorStand stand = recipe.getGhostBlocks().get(blockLocation);
        if (stand != null) {
            stand.remove();
            recipe.getGhostBlocks().remove(blockLocation);
        }
        
        // Tüm bloklar tamamlandı mı?
        if (recipe.getGhostBlocks().isEmpty()) {
            if (isActive) {
                removeActiveGhostRecipe(player.getUniqueId());
                player.sendMessage("§aTarif tamamlandı!");
            } else {
                removeFixedGhostRecipe(blockLocation);
            }
        }
    }
    
    /**
     * Tarifi sabitle (yer tıklayınca sabit kalır)
     */
    public void fixGhostRecipe(Player player, Location baseLocation) {
        GhostRecipe activeRecipe = getActiveGhostRecipe(player.getUniqueId());
        if (activeRecipe == null) return;
        
        // Sabit tarif olarak ekle
        addFixedGhostRecipe(baseLocation, activeRecipe);
        
        // Aktif tariften kaldır (ama hayalet bloklar kalır)
        removeActiveGhostRecipe(player.getUniqueId());
        
        player.sendMessage("§aTarif sabitlendi! Shift+Sol tık ile kaldırabilirsiniz.");
    }
    
    /**
     * Bu konumda sabit tarif var mı?
     */
    public boolean hasFixedRecipeAt(Location loc) {
        return fixedGhostRecipes.containsKey(loc);
    }
    
    /**
     * Bu konumdaki sabit tarifi kaldır
     */
    public void removeFixedRecipeAt(Location loc) {
        GhostRecipe recipe = getFixedGhostRecipe(loc);
        if (recipe != null) {
            // Tüm hayalet blokları kaldır
            for (ArmorStand stand : recipe.getGhostBlocks().values()) {
                if (stand.isValid()) {
                    stand.remove();
                }
            }
            removeFixedGhostRecipe(loc);
        }
    }
    
    /**
     * Tüm sabit tarifleri kaldır
     */
    public int clearAllFixedRecipes() {
        int count = 0;
        for (Location loc : new ArrayList<>(fixedGhostRecipes.keySet())) {
            removeFixedRecipeAt(loc);
            count++;
        }
        return count;
    }
}
