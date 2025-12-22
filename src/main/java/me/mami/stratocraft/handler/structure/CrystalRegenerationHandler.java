package me.mami.stratocraft.handler.structure;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Can Yenileme Yapısı Handler
 * Özel yakıt ile kristal canını yeniler
 */
public class CrystalRegenerationHandler implements Listener {
    private final Main plugin;
    private final Map<UUID, Integer> structureFuel = new HashMap<>(); // Yapı UUID -> Yakıt miktarı
    
    // Seviye -> HP/dakika yenileme hızı
    private final Map<Integer, Double> levelRegenRate = new HashMap<>();
    
    // Seviye -> Maksimum yakıt
    private final Map<Integer, Integer> levelMaxFuel = new HashMap<>();
    
    public CrystalRegenerationHandler(Main plugin) {
        this.plugin = plugin;
        initializeRegenValues();
        
        // Her dakika can yenileme
        startRegenerationTask();
    }
    
    private void initializeRegenValues() {
        // Seviye -> HP/dakika
        levelRegenRate.put(1, 1.0);
        levelRegenRate.put(2, 2.0);
        levelRegenRate.put(3, 5.0);
        
        // Seviye -> Maksimum yakıt
        levelMaxFuel.put(1, 500);
        levelMaxFuel.put(2, 1000);
        levelMaxFuel.put(3, 2000);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_REGENERATION_STRUCTURE) {
            return;
        }
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(blockLoc);
        if (clan == null) return;
        
        if (clan.getRank(player.getUniqueId()) == null) {
            player.sendMessage("§cBu yapıyı kullanmak için klan üyesi olmalısınız!");
            return;
        }
        
        // Yakıt ekleme
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && isRegenerationFuel(item)) {
            addFuel(structure, clan, item, player);
            event.setCancelled(true);
        }
    }
    
    private Structure findStructureAt(Location loc) {
        if (plugin.getTerritoryManager() == null) return null;
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(loc);
        if (clan == null) return null;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(loc) < 2.0) {
                return structure;
            }
        }
        
        return null;
    }
    
    private boolean isRegenerationFuel(ItemStack item) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals("REGENERATION_FUEL");
    }
    
    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_id");
        org.bukkit.persistence.PersistentDataContainer container = 
            item.getItemMeta().getPersistentDataContainer();
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        
        return null;
    }
    
    private void addFuel(Structure structure, Clan clan, ItemStack item, Player player) {
        UUID structureId = UUID.nameUUIDFromBytes(
            (structure.getLocation().getBlockX() + ";" + 
             structure.getLocation().getBlockY() + ";" + 
             structure.getLocation().getBlockZ()).getBytes()
        );
        int fuelPerItem = 100; // Her item 100 yakıt
        
        int currentFuel = structureFuel.getOrDefault(structureId, 0);
        int maxFuel = levelMaxFuel.get(structure.getLevel());
        
        if (currentFuel >= maxFuel) {
            player.sendMessage("§cYapının yakıt deposu dolu! (Max: " + maxFuel + ")");
            return;
        }
        
        // Yakıt ekle
        int newFuel = Math.min(maxFuel, currentFuel + fuelPerItem);
        structureFuel.put(structureId, newFuel);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§aYakıt eklendi! (" + newFuel + "/" + maxFuel + ")");
    }
    
    // ✅ PERFORMANS: Cache mekanizması
    private final Map<UUID, Long> regenCache = new HashMap<>(); // Klan UUID -> Son yenileme zamanı
    private static final long REGEN_CACHE_DURATION = 60000L; // 1 dakika cache
    
    /**
     * Can yenileme task'ı - Her dakika çalışır
     */
    private void startRegenerationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                regenerateAllCrystals();
            }
        }.runTaskTimer(plugin, 0L, 1200L); // 1200 tick = 1 dakika
    }
    
    private void regenerateAllCrystals() {
        if (plugin.getTerritoryManager() == null) return;
        
        long now = System.currentTimeMillis();
        int processed = 0;
        int maxPerTick = 20; // Tick başına maksimum işlem (performans için)
        
        for (Clan clan : plugin.getTerritoryManager().getClanManager().getAllClans()) {
            if (processed >= maxPerTick) break; // Performans limiti
            if (clan == null || !clan.hasCrystal()) continue;
            
            UUID clanId = clan.getId();
            
            // Cache kontrolü
            Long lastRegen = regenCache.get(clanId);
            if (lastRegen != null && (now - lastRegen) < REGEN_CACHE_DURATION) {
                continue; // Henüz yenileme zamanı gelmedi
            }
            
            // Can yenileme yapısını bul
            Structure regenStructure = null;
            int maxLevel = 0;
            
            for (Structure structure : clan.getStructures()) {
                if (structure.getType() == Structure.Type.CRYSTAL_REGENERATION_STRUCTURE) {
                    if (structure.getLevel() > maxLevel) {
                        UUID structureId = UUID.nameUUIDFromBytes(
                            (structure.getLocation().getBlockX() + ";" + 
                             structure.getLocation().getBlockY() + ";" + 
                             structure.getLocation().getBlockZ()).getBytes()
                        );
                        int fuel = structureFuel.getOrDefault(structureId, 0);
                        
                        // Yakıt varsa aktif
                        if (fuel > 0) {
                            regenStructure = structure;
                            maxLevel = structure.getLevel();
                        }
                    }
                }
            }
            
            // Can yenile
            if (regenStructure != null) {
                double regenRate = levelRegenRate.get(maxLevel);
                UUID structureId = UUID.nameUUIDFromBytes(
                    (regenStructure.getLocation().getBlockX() + ";" + 
                     regenStructure.getLocation().getBlockY() + ";" + 
                     regenStructure.getLocation().getBlockZ()).getBytes()
                );
                
                // Can yenile
                clan.regenerateCrystalHealth(regenRate);
                
                // Yakıt tüket (1 dakika için 1 yakıt)
                int currentFuel = structureFuel.getOrDefault(structureId, 0);
                if (currentFuel > 0) {
                    structureFuel.put(structureId, currentFuel - 1);
                }
                
                // Efekt (can yenilendiğinde) - performans için azaltıldı
                Location crystalLoc = clan.getCrystalLocation();
                if (crystalLoc != null && crystalLoc.getWorld() != null) {
                    crystalLoc.getWorld().spawnParticle(
                        org.bukkit.Particle.HEART,
                        crystalLoc,
                        3, // 5 -> 3 (performans optimizasyonu)
                        0.3, 0.3, 0.3, 0.1
                    );
                }
                
                // Cache'e kaydet
                regenCache.put(clanId, now);
            }
            
            processed++;
        }
    }
}

