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
 * Kristal Zırh Yapısı Handler
 * Kristale gelen hasarı azaltır
 */
public class CrystalArmorHandler implements Listener {
    private final Main plugin;
    private final Map<UUID, Integer> structureFuel = new HashMap<>(); // Yapı UUID -> Yakıt miktarı
    
    // Seviye -> Hasar azaltma çarpanı
    private final Map<Integer, Double> levelDamageReduction = new HashMap<>();
    
    public CrystalArmorHandler(Main plugin) {
        this.plugin = plugin;
        initializeDamageReduction();
        
        // Her saniye zırh kontrolü yap
        startArmorUpdateTask();
    }
    
    private void initializeDamageReduction() {
        levelDamageReduction.put(1, 0.10); // %10
        levelDamageReduction.put(2, 0.25); // %25
        levelDamageReduction.put(3, 0.50); // %50
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_ARMOR_STRUCTURE) {
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
        if (item != null && isArmorFuel(item)) {
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
    
    private boolean isArmorFuel(ItemStack item) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals("ARMOR_FUEL");
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
        int maxFuel = getMaxFuel(structure.getLevel());
        
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
    
    private int getMaxFuel(int level) {
        switch (level) {
            case 1: return 500;
            case 2: return 1000;
            case 3: return 2000;
            default: return 500;
        }
    }
    
    /**
     * Zırh güncelleme task'ı - Her 2 saniyede bir çalışır (performans için)
     */
    private void startArmorUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllArmorStructures();
            }
        }.runTaskTimer(plugin, 0L, 40L); // Her 2 saniye (performans optimizasyonu)
    }
    
    // ✅ PERFORMANS: Cache mekanizması
    private final Map<UUID, Long> clanUpdateCache = new HashMap<>(); // Klan UUID -> Son güncelleme zamanı
    private final Map<UUID, Double> clanArmorCache = new HashMap<>(); // Klan UUID -> Son zırh değeri
    private static final long CACHE_DURATION = 1000L; // 1 saniye cache
    
    private void updateAllArmorStructures() {
        if (plugin.getTerritoryManager() == null) return;
        
        long now = System.currentTimeMillis();
        int processed = 0;
        int maxPerTick = 10; // Tick başına maksimum işlem (performans için)
        
        for (Clan clan : plugin.getTerritoryManager().getClanManager().getAllClans()) {
            if (processed >= maxPerTick) break; // Performans limiti
            if (clan == null || !clan.hasCrystal()) continue;
            
            UUID clanId = clan.getId();
            
            // Cache kontrolü
            Long lastUpdate = clanUpdateCache.get(clanId);
            if (lastUpdate != null && (now - lastUpdate) < CACHE_DURATION) {
                // Cache'den al
                Double cachedArmor = clanArmorCache.get(clanId);
                if (cachedArmor != null) {
                    clan.setCrystalDamageReduction(cachedArmor);
                    continue;
                }
            }
            
            // En yüksek seviyeli zırh yapısını bul
            Structure bestArmorStructure = null;
            int maxLevel = 0;
            
            for (Structure structure : clan.getStructures()) {
                if (structure.getType() == Structure.Type.CRYSTAL_ARMOR_STRUCTURE) {
                    if (structure.getLevel() > maxLevel) {
                        UUID structureId = UUID.nameUUIDFromBytes(
                            (structure.getLocation().getBlockX() + ";" + 
                             structure.getLocation().getBlockY() + ";" + 
                             structure.getLocation().getBlockZ()).getBytes()
                        );
                        int fuel = structureFuel.getOrDefault(structureId, 0);
                        
                        // Yakıt varsa aktif
                        if (fuel > 0) {
                            bestArmorStructure = structure;
                            maxLevel = structure.getLevel();
                        }
                    }
                }
            }
            
            // Zırh değerini ayarla
            double damageReduction = 0.0;
            if (bestArmorStructure != null) {
                damageReduction = levelDamageReduction.get(maxLevel);
            }
            
            clan.setCrystalDamageReduction(damageReduction);
            
            // Cache'e kaydet
            clanUpdateCache.put(clanId, now);
            clanArmorCache.put(clanId, damageReduction);
            
            processed++;
        }
    }
    
    /**
     * Hasar alındığında yakıt tüket (ChaosDragonHandler'dan çağrılacak)
     */
    public void consumeFuelOnDamage(Clan clan, double damage) {
        // En yüksek seviyeli zırh yapısını bul
        Structure bestArmorStructure = null;
        int maxLevel = 0;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getType() == Structure.Type.CRYSTAL_ARMOR_STRUCTURE) {
                if (structure.getLevel() > maxLevel) {
                    UUID structureId = UUID.nameUUIDFromBytes(
                        (structure.getLocation().getBlockX() + ";" + 
                         structure.getLocation().getBlockY() + ";" + 
                         structure.getLocation().getBlockZ()).getBytes()
                    );
                    int fuel = structureFuel.getOrDefault(structureId, 0);
                    
                    if (fuel > 0) {
                        bestArmorStructure = structure;
                        maxLevel = structure.getLevel();
                    }
                }
            }
        }
        
        if (bestArmorStructure != null) {
            UUID structureId = UUID.nameUUIDFromBytes(
                (bestArmorStructure.getLocation().getBlockX() + ";" + 
                 bestArmorStructure.getLocation().getBlockY() + ";" + 
                 bestArmorStructure.getLocation().getBlockZ()).getBytes()
            );
            int currentFuel = structureFuel.getOrDefault(structureId, 0);
            
            // Hasar başına yakıt tüket (hasar miktarına göre)
            int fuelConsumption = (int) Math.ceil(damage / 10.0); // Her 10 hasar için 1 yakıt
            int newFuel = Math.max(0, currentFuel - fuelConsumption);
            
            structureFuel.put(structureId, newFuel);
            
            // Yakıt bitti mi?
            if (newFuel == 0) {
                // Klan üyelerine bildir
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = org.bukkit.Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§c⚠ Kristal Zırh Yapısı yakıtı bitti! Zırh pasif oldu.");
                    }
                }
            }
        }
    }
}

