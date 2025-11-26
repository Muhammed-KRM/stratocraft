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
    
    // Tarif ID -> Tarif verisi (blokların konumları ve tipleri)
    private final Map<String, GhostRecipeData> recipeData = new HashMap<>();
    
    public GhostRecipeManager() {
        initializeRecipeData();
    }
    
    /**
     * Tarif verilerini başlat (yapılar, bataryalar, ritüeller)
     */
    private void initializeRecipeData() {
        // Örnek: Simya Kulesi tarifi (3x3 platform + Enchanting Table)
        GhostRecipeData alchemyTower = new GhostRecipeData();
        // Enchanting Table'ın altında 3x3 Cobblestone
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                alchemyTower.addBlock(new Vector(x, -1, z), Material.COBBLESTONE);
            }
        }
        alchemyTower.addBlock(new Vector(0, 0, 0), Material.ENCHANTING_TABLE);
        recipeData.put("ALCHEMY", alchemyTower);
        
        // Örnek: Magma Bataryası tarifi (2x2 Magma Block)
        GhostRecipeData magmaBattery = new GhostRecipeData();
        magmaBattery.addBlock(new Vector(0, 0, 0), Material.MAGMA_BLOCK);
        magmaBattery.addBlock(new Vector(0, -1, 0), Material.MAGMA_BLOCK);
        recipeData.put("MAGMA_BATTERY", magmaBattery);
        
        // Örnek: Klan Kurma Ritüeli (3x3 Cobblestone + Crafting Table)
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
        
        player.sendMessage("§aHayalet tarif gösteriliyor! Blokları doğru yere koyun. (50 blok uzaklaşınca kaybolur)");
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

