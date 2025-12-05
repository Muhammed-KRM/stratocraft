package me.mami.stratocraft.manager;

import org.bukkit.Location;
import org.bukkit.Material;
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
        addSimpleStructureRecipe("CORE", Material.END_CRYSTAL, Material.DIAMOND_BLOCK);
        addSimpleStructureRecipe("SIEGE_FACTORY", Material.CRAFTING_TABLE, Material.IRON_BLOCK);
        addSimpleStructureRecipe("WALL_GENERATOR", Material.BEACON, Material.STONE_BRICKS);
        addSimpleStructureRecipe("GRAVITY_WELL", Material.BEACON, Material.OBSIDIAN);
        addSimpleStructureRecipe("LAVA_TRENCHER", Material.DISPENSER, Material.MAGMA_BLOCK);
        addSimpleStructureRecipe("WATCHTOWER", Material.BEACON, Material.COBBLESTONE);
        addSimpleStructureRecipe("DRONE_STATION", Material.CRAFTING_TABLE, Material.IRON_BLOCK);
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
                clanCreate.addBlock(new Vector(x, -1, z), Material.COBBLESTONE);
            }
        }
        clanCreate.addBlock(new Vector(0, 0, 0), Material.CRAFTING_TABLE);
        recipeData.put("CLAN_CREATE", clanCreate);
    }
    
    /**
     * Basit yapı tarifi ekle (merkez blok + alt platform)
     */
    private void addSimpleStructureRecipe(String recipeId, Material centerBlock, Material platformBlock) {
        GhostRecipeData data = new GhostRecipeData();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                data.addBlock(new Vector(x, -1, z), platformBlock);
            }
        }
        data.addBlock(new Vector(0, 0, 0), centerBlock);
        recipeData.put(recipeId, data);
    }
    
    /**
     * Oyuncu tarif kitabına baktığında hayalet yapı göster
     */
    public void showGhostRecipe(Player player, String recipeId, Location targetLocation) {
        // Eğer zaten aktif bir hayalet tarif varsa iptal et
        if (activeGhostRecipes.containsKey(player.getUniqueId())) {
            removeGhostRecipe(player);
        }
        
        GhostRecipeData data = recipeData.get(recipeId);
        if (data == null) {
            player.sendMessage("§cBu tarif için hayalet görüntü mevcut değil.");
            return;
        }
        
        // Hayalet yapıyı oluştur
        List<ArmorStand> ghostBlocks = new ArrayList<>();
        Map<Vector, ArmorStand> offsetToGhostMap = new HashMap<>(); // Offset -> ArmorStand mapping
        
        for (Map.Entry<Vector, Material> entry : data.getBlocks().entrySet()) {
            Vector offset = entry.getKey();
            Material material = entry.getValue();
            
            Location blockLoc = targetLocation.clone().add(offset);
            
            // ArmorStand ile hayalet blok göster
            Location spawnLoc = blockLoc.clone().add(0.5, 0, 0.5);
            ArmorStand ghost = (ArmorStand) player.getWorld().spawnEntity(
                spawnLoc, EntityType.ARMOR_STAND
            );
            ghost.setVisible(false);
            ghost.setGravity(false);
            ghost.setInvulnerable(true);
            ghost.setMarker(true);
            ghost.setSmall(true);
            ghost.setCustomNameVisible(false);
            
            // Blok görünümü için kafasına blok koy
            ghost.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(material));
            
            // Glow efekti (1.20+ için)
            try {
                ghost.setGlowing(true);
            } catch (Exception e) {
                // Eski versiyonlarda çalışmayabilir
            }
            
            ghostBlocks.add(ghost);
            offsetToGhostMap.put(offset, ghost); // Offset -> ArmorStand mapping
        }
        
        // Aktif tarifi kaydet
        GhostRecipe recipe = new GhostRecipe(recipeId, targetLocation, ghostBlocks, offsetToGhostMap, System.currentTimeMillis());
        activeGhostRecipes.put(player.getUniqueId(), recipe);
        
        player.sendMessage("§aHayalet tarif gösteriliyor! Blokları doğru yere koyun.");
        player.sendMessage("§7Yere Shift+Sağ tıklayarak tarifi sabitleyebilirsiniz. (50 blok uzaklaşınca kaybolur)");
    }
    
    /**
     * Yere tıklayınca tarifi sabitle
     */
    public void fixGhostRecipe(Player player, Location targetLocation) {
        GhostRecipe recipe = activeGhostRecipes.get(player.getUniqueId());
        if (recipe == null) {
            player.sendMessage("§cAktif bir hayalet tarif yok!");
            return;
        }
        
        // Eski tarifi kaldır
        removeGhostRecipe(player);
        
        // Yeni konumda sabit tarif oluştur
        GhostRecipeData data = recipeData.get(recipe.getRecipeId());
        if (data == null) {
            player.sendMessage("§cTarif verisi bulunamadı!");
            return;
        }
        
        // Hayalet yapıyı oluştur
        List<ArmorStand> ghostBlocks = new ArrayList<>();
        Map<Vector, ArmorStand> offsetToGhostMap = new HashMap<>(); // Offset -> ArmorStand mapping
        
        for (Map.Entry<Vector, Material> entry : data.getBlocks().entrySet()) {
            Vector offset = entry.getKey();
            Material material = entry.getValue();
            
            Location blockLoc = targetLocation.clone().add(offset);
            
            // ArmorStand ile hayalet blok göster
            Location spawnLoc = blockLoc.clone().add(0.5, 0, 0.5);
            ArmorStand ghost = (ArmorStand) player.getWorld().spawnEntity(
                spawnLoc, EntityType.ARMOR_STAND
            );
            ghost.setVisible(false);
            ghost.setGravity(false);
            ghost.setInvulnerable(true);
            ghost.setMarker(true);
            ghost.setSmall(true);
            ghost.setCustomNameVisible(false);
            
            // Blok görünümü için kafasına blok koy
            ghost.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(material));
            
            // Glow efekti
            try {
                ghost.setGlowing(true);
            } catch (Exception e) {
                // Eski versiyonlarda çalışmayabilir
            }
            
            ghostBlocks.add(ghost);
            offsetToGhostMap.put(offset, ghost); // Offset -> ArmorStand mapping
        }
        
        // Sabit tarifi kaydet
        GhostRecipe fixedRecipe = new GhostRecipe(recipe.getRecipeId(), targetLocation, ghostBlocks, offsetToGhostMap, System.currentTimeMillis());
        fixedGhostRecipes.put(targetLocation, fixedRecipe);
        
        player.sendMessage("§a§lTarif sabitlendi! Bu konumda kalacak.");
    }
    
    /**
     * Sabit tarifi kaldır
     */
    public void removeFixedRecipe(Location location) {
        GhostRecipe recipe = fixedGhostRecipes.remove(location);
        if (recipe != null) {
            for (ArmorStand ghost : recipe.getGhostBlocks()) {
                ghost.remove();
            }
        }
    }
    
    /**
     * Belirli bir konumda sabit tarif var mı kontrol et
     */
    public boolean hasFixedRecipeAt(Location location) {
        // 5 blok yarıçap içinde sabit tarif var mı?
        for (Location fixedLoc : fixedGhostRecipes.keySet()) {
            if (fixedLoc.getWorld().equals(location.getWorld()) &&
                fixedLoc.distance(location) <= 5) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Belirli bir konumdaki sabit tarifi kaldır
     */
    public boolean removeFixedRecipeAt(Location location) {
        // 5 blok yarıçap içinde sabit tarif bul ve kaldır
        Location foundLoc = null;
        for (Location fixedLoc : fixedGhostRecipes.keySet()) {
            if (fixedLoc.getWorld().equals(location.getWorld()) &&
                fixedLoc.distance(location) <= 5) {
                foundLoc = fixedLoc;
                break;
            }
        }
        
        if (foundLoc != null) {
            removeFixedRecipe(foundLoc);
            return true;
        }
        return false;
    }
    
    /**
     * Hayalet tarifi kaldır
     */
    public void removeGhostRecipe(Player player) {
        GhostRecipe recipe = activeGhostRecipes.remove(player.getUniqueId());
        if (recipe != null) {
            for (ArmorStand ghost : recipe.getGhostBlocks()) {
                ghost.remove();
            }
        }
    }
    
    /**
     * Tüm sabit tarifleri kaldır
     */
    public int clearAllFixedRecipes() {
        int count = 0;
        for (GhostRecipe recipe : fixedGhostRecipes.values()) {
            for (ArmorStand ghost : recipe.getGhostBlocks()) {
                ghost.remove();
            }
            count++;
        }
        fixedGhostRecipes.clear();
        return count;
    }
    
    /**
     * Oyuncu 50 bloktan uzaklaştı mı kontrol et
     */
    public void checkDistance(Player player) {
        GhostRecipe recipe = activeGhostRecipes.get(player.getUniqueId());
        if (recipe == null) return;
        
        // Oyuncu offline veya null kontrolü
        if (player == null || !player.isOnline()) {
            removeGhostRecipe(player);
            return;
        }
        
        double distance = player.getLocation().distance(recipe.getLocation());
        if (distance > 50) {
            removeGhostRecipe(player);
            player.sendMessage("§cHayalet tarif 50 bloktan uzaklaştığınız için kaldırıldı.");
        }
    }
    
    /**
     * Blok koyulduğunda kontrol et ve doğru blok ise hayalet görüntüsünü kaldır
     * @return true eğer blok doğru yerde ve hayalet görüntüsü kaldırıldıysa
     */
    public boolean checkAndRemoveBlock(Player player, Location blockLocation, Material placedMaterial) {
        // Önce aktif tarifleri kontrol et
        GhostRecipe activeRecipe = activeGhostRecipes.get(player.getUniqueId());
        if (activeRecipe != null) {
            if (checkAndRemoveBlockFromRecipe(player, activeRecipe, blockLocation, placedMaterial, true, null)) {
                return true;
            }
        }
        
        // Sonra sabit tarifleri kontrol et (tarif boyutuna göre dinamik yarıçap)
        for (Map.Entry<Location, GhostRecipe> entry : new ArrayList<>(fixedGhostRecipes.entrySet())) {
            Location fixedLoc = entry.getKey();
            GhostRecipe fixedRecipe = entry.getValue();
            
            if (!fixedLoc.getWorld().equals(blockLocation.getWorld())) continue;
            
            // Tarif boyutuna göre maksimum mesafe hesapla (en büyük offset + 2 blok güvenlik)
            GhostRecipeData recipeData = this.recipeData.get(fixedRecipe.getRecipeId());
            if (recipeData != null) {
                double maxDistance = 0;
                for (Vector offset : recipeData.getBlocks().keySet()) {
                    double dist = offset.length();
                    if (dist > maxDistance) maxDistance = dist;
                }
                maxDistance = Math.max(maxDistance + 2, 5); // En az 5 blok
                
                if (fixedLoc.distance(blockLocation) <= maxDistance) {
                    // Sabit tarif için doğru key'i (fixedLoc) geçir
                    if (checkAndRemoveBlockFromRecipe(player, fixedRecipe, blockLocation, placedMaterial, false, fixedLoc)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Belirli bir tarif için blok kontrolü ve hayalet görüntüsü kaldırma
     * @param fixedRecipeKey Sabit tarifler için map key'i (Location), aktif tarifler için null
     */
    private boolean checkAndRemoveBlockFromRecipe(Player player, GhostRecipe recipe, 
                                                   Location blockLocation, Material placedMaterial, 
                                                   boolean isActive, Location fixedRecipeKey) {
        GhostRecipeData data = recipeData.get(recipe.getRecipeId());
        if (data == null) return false;
        
        // Koyulan bloğun tarifteki konumunu bul
        Location recipeCenter = recipe.getLocation();
        Vector offset = blockLocation.toVector().subtract(recipeCenter.toVector());
        
        // En yakın offset'i bul (blok koordinatları için)
        Vector blockOffset = new Vector(
            Math.round(offset.getX()),
            Math.round(offset.getY()),
            Math.round(offset.getZ())
        );
        
        // Bu offset'te bir blok var mı?
        Material expectedMaterial = data.getBlocks().get(blockOffset);
        if (expectedMaterial == null) return false; // Bu blok tarifte yok
        
        // Malzeme doğru mu?
        if (placedMaterial != expectedMaterial) return false; // Yanlış malzeme
        
        // Bu offset'teki hayalet görüntüsünü kaldır
        ArmorStand ghost = recipe.getOffsetToGhostMap().get(blockOffset);
        if (ghost != null && ghost.isValid()) {
            ghost.remove();
            recipe.getOffsetToGhostMap().remove(blockOffset);
            recipe.getGhostBlocks().remove(ghost);
            
            // Kalan blok sayısını kontrol et
            int remainingBlocks = recipe.getOffsetToGhostMap().size();
            if (remainingBlocks == 0) {
                // Tüm bloklar tamamlandı!
                if (isActive) {
                    activeGhostRecipes.remove(player.getUniqueId());
                } else {
                    // Sabit tarif tamamlandı, kaldır (doğru key'i kullan)
                    if (fixedRecipeKey != null) {
                        fixedGhostRecipes.remove(fixedRecipeKey);
                    } else {
                        // Fallback: recipe.getLocation() kullan (ama bu genelde çalışmaz)
                        fixedGhostRecipes.remove(recipe.getLocation());
                    }
                }
                player.sendMessage("§a§l════════════════════════════");
                player.sendMessage("§a§l✓ TARİF TAMAMLANDI!");
                player.sendMessage("§7Yapının tarifi başarıyla tamamlandı.");
                player.sendMessage("§a§l════════════════════════════");
                return true;
            } else {
                player.sendMessage("§e✓ Blok doğru yere konuldu! (§7" + remainingBlocks + " blok kaldı§e)");
            }
        }
        
        return true;
    }
    
    /**
     * Tarif tamamlandı mı kontrol et (bloklar doğru yere konuldu mu?)
     * @deprecated checkAndRemoveBlock kullanın
     */
    @Deprecated
    public boolean checkRecipeComplete(Player player, Location checkLocation) {
        GhostRecipe recipe = activeGhostRecipes.get(player.getUniqueId());
        if (recipe == null) return false;
        
        GhostRecipeData data = recipeData.get(recipe.getRecipeId());
        if (data == null) return false;
        
        // Tüm blokların doğru yerde olup olmadığını kontrol et
        for (Map.Entry<Vector, Material> entry : data.getBlocks().entrySet()) {
            Vector offset = entry.getKey();
            Material expectedMaterial = entry.getValue();
            
            Location blockLoc = recipe.getLocation().clone().add(offset);
            if (blockLoc.getBlock().getType() != expectedMaterial) {
                return false; // Bir blok yanlış
            }
        }
        
        // Tüm bloklar doğru - tarif tamamlandı!
        removeGhostRecipe(player);
        player.sendMessage("§a§lTarif tamamlandı! Hayalet yapı kaldırıldı.");
        return true;
    }
    
    public boolean hasActiveRecipe(UUID playerId) {
        return activeGhostRecipes.containsKey(playerId);
    }
    
    public GhostRecipe getActiveRecipe(UUID playerId) {
        return activeGhostRecipes.get(playerId);
    }
    
    // ========== DATA CLASSES ==========
    
    private static class GhostRecipe {
        private final String recipeId;
        private final Location location;
        private final List<ArmorStand> ghostBlocks;
        private final Map<Vector, ArmorStand> offsetToGhostMap; // Offset -> ArmorStand mapping
        private final long startTime;
        
        public GhostRecipe(String recipeId, Location location, List<ArmorStand> ghostBlocks, 
                          Map<Vector, ArmorStand> offsetToGhostMap, long startTime) {
            this.recipeId = recipeId;
            this.location = location;
            this.ghostBlocks = ghostBlocks;
            this.offsetToGhostMap = offsetToGhostMap;
            this.startTime = startTime;
        }
        
        public String getRecipeId() { return recipeId; }
        public Location getLocation() { return location; }
        public List<ArmorStand> getGhostBlocks() { return ghostBlocks; }
        public Map<Vector, ArmorStand> getOffsetToGhostMap() { return offsetToGhostMap; }
        @SuppressWarnings("unused")
        public long getStartTime() { return startTime; }
    }
    
    private static class GhostRecipeData {
        private final Map<Vector, Material> blocks = new HashMap<>();
        
        public void addBlock(Vector offset, Material material) {
            blocks.put(offset, material);
        }
        
        public Map<Vector, Material> getBlocks() {
            return blocks;
        }
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
}

