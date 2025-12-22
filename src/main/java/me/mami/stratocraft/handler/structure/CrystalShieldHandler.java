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
 * Kristal Kalkan Yapısı Handler
 * Belirli sayıda saldırıyı tamamen engeller
 */
public class CrystalShieldHandler implements Listener {
    private final Main plugin;
    
    // Seviye -> Maksimum kalkan bloğu
    private final Map<Integer, Integer> levelMaxShieldBlocks = new HashMap<>();
    
    // Seviye -> Yakıt başına kalkan bloğu
    private final Map<Integer, Integer> levelShieldBlocksPerFuel = new HashMap<>();
    
    public CrystalShieldHandler(Main plugin) {
        this.plugin = plugin;
        initializeShieldValues();
        
        // Her saniye kalkan güncelleme
        startShieldUpdateTask();
    }
    
    private void initializeShieldValues() {
        // Seviye -> Maksimum kalkan bloğu
        levelMaxShieldBlocks.put(1, 5);
        levelMaxShieldBlocks.put(2, 15);
        levelMaxShieldBlocks.put(3, 30);
        
        // Seviye -> Yakıt başına kalkan bloğu
        levelShieldBlocksPerFuel.put(1, 1);
        levelShieldBlocksPerFuel.put(2, 3);
        levelShieldBlocksPerFuel.put(3, 5);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_SHIELD_STRUCTURE) {
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
        if (item != null && isShieldFuel(item)) {
            addShieldBlocks(structure, clan, item, player);
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
    
    private boolean isShieldFuel(ItemStack item) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals("SHIELD_FUEL");
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
    
    private void addShieldBlocks(Structure structure, Clan clan, ItemStack item, Player player) {
        int currentBlocks = clan.getCrystalShieldBlocks();
        int maxBlocks = levelMaxShieldBlocks.get(structure.getLevel());
        int blocksPerFuel = levelShieldBlocksPerFuel.get(structure.getLevel());
        
        if (currentBlocks >= maxBlocks) {
            player.sendMessage("§cKalkan deposu dolu! (Max: " + maxBlocks + ")");
            return;
        }
        
        // Kalkan bloğu ekle
        int newBlocks = Math.min(maxBlocks, currentBlocks + blocksPerFuel);
        clan.setCrystalShieldBlocks(newBlocks);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§aKalkan bloğu eklendi! (" + newBlocks + "/" + maxBlocks + ")");
        
        // Efekt
        Location structureLoc = structure.getLocation();
        structureLoc.getWorld().spawnParticle(
            org.bukkit.Particle.BLOCK_CRACK,
            structureLoc.add(0.5, 1, 0.5),
            20,
            0.3, 0.3, 0.3, 0.1,
            org.bukkit.Material.BARRIER.createBlockData()
        );
    }
    
    /**
     * Kalkan güncelleme task'ı
     */
    private void startShieldUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Kalkan değerleri zaten Clan modelinde tutuluyor
                // Burada sadece görsel güncellemeler yapılabilir
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Hasar alındığında kalkan bloğu tüket (ChaosDragonHandler'dan çağrılacak)
     */
    public boolean consumeShieldBlockOnDamage(Clan clan) {
        int currentBlocks = clan.getCrystalShieldBlocks();
        
        if (currentBlocks > 0) {
            // Kalkan bloğu var, saldırıyı engelle
            clan.setCrystalShieldBlocks(currentBlocks - 1);
            
            // Klan üyelerine bildir (kritik seviyede)
            if (currentBlocks <= 3) {
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = org.bukkit.Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§c⚠ Kalkan bloğu azaldı! (" + (currentBlocks - 1) + " kaldı)");
                    }
                }
            }
            
            return true; // Saldırı engellendi
        }
        
        return false; // Kalkan bloğu yok, normal hasar alınacak
    }
}

