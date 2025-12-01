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
        
        // ========== BATARYALAR ==========
        GhostRecipeData magmaBattery = new GhostRecipeData();
        magmaBattery.addBlock(new Vector(0, 0, 0), Material.MAGMA_BLOCK);
        magmaBattery.addBlock(new Vector(0, -1, 0), Material.MAGMA_BLOCK);
        recipeData.put("MAGMA_BATTERY", magmaBattery);
        
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
        }
        
        // Aktif tarifi kaydet
        GhostRecipe recipe = new GhostRecipe(recipeId, targetLocation, ghostBlocks, System.currentTimeMillis());
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
        }
        
        // Sabit tarifi kaydet
        GhostRecipe fixedRecipe = new GhostRecipe(recipe.getRecipeId(), targetLocation, ghostBlocks, System.currentTimeMillis());
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
     * Tarif tamamlandı mı kontrol et (bloklar doğru yere konuldu mu?)
     */
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
        private final long startTime;
        
        public GhostRecipe(String recipeId, Location location, List<ArmorStand> ghostBlocks, long startTime) {
            this.recipeId = recipeId;
            this.location = location;
            this.ghostBlocks = ghostBlocks;
            this.startTime = startTime;
        }
        
        public String getRecipeId() { return recipeId; }
        public Location getLocation() { return location; }
        public List<ArmorStand> getGhostBlocks() { return ghostBlocks; }
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
}

