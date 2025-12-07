package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.NewMineManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Yeni Mayın Sistemi Dinleyicisi
 * - Basınç plakası tetikleme
 * - Mayın oluşturma (item ile sağ tık)
 * - Gizleme itemi ile görünürlük değiştirme
 */
public class NewMineListener implements Listener {
    private final NewMineManager mineManager;
    
    public NewMineListener(NewMineManager mineManager) {
        this.mineManager = mineManager;
    }
    
    /**
     * Oyuncu hareket ettiğinde mayın kontrolü
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Block standingBlock = event.getTo().getBlock();
        
        // Basınç plakası mı?
        if (!NewMineManager.isPressurePlate(standingBlock.getType())) {
            return;
        }
        
        // Mayın var mı?
        if (!standingBlock.hasMetadata("NewMine")) {
            return;
        }
        
        // Mayın tetikle
        mineManager.triggerMine(standingBlock, player);
    }
    
    /**
     * Basınç plakası yerleştirme (yeni mayın sistemi)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlaceMine(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        
        // Basınç plakası mı?
        if (!NewMineManager.isPressurePlate(block.getType())) {
            return;
        }
        
        // Mayın itemı mı?
        NewMineManager.MineType mineType = getMineTypeFromItem(item);
        if (mineType == null) {
            return; // Normal basınç plakası
        }
        
        // MAYIN OLUŞTUR!
        if (mineManager.createMine(player, block, mineType)) {
            player.sendMessage("§a✓ §e" + mineType.getDisplayName() + " §ayerleştirildi!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON, 1.0f, 0.8f);
        } else {
            player.sendMessage("§cMayın oluşturulamadı!");
            event.setCancelled(true);
        }
    }
    
    /**
     * Oyuncu etkileşimi (gizleme)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Gizleme itemi kontrolü (Shift + Sağ Tık)
        if (player.isSneaking() && isMineConcealer(item)) {
            // Basınç plakası mı?
            if (NewMineManager.isPressurePlate(block.getType())) {
                // Mayın var mı?
                if (block.hasMetadata("NewMine")) {
                    // Görünürlüğü değiştir
                    Location mineLoc = block.getLocation();
                    if (mineManager.toggleMineVisibility(mineLoc)) {
                        NewMineManager.MineData mine = mineManager.getMine(mineLoc);
                        if (mine != null && mine.isHidden()) {
                            player.sendMessage("§a§l✓ Mayın gizlendi!");
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                        } else {
                            player.sendMessage("§a§l✓ Mayın görünür yapıldı!");
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                        }
                        event.setCancelled(true);
                    } else {
                        player.sendMessage("§cMayın bulunamadı!");
                    }
                    return;
                } else {
                    player.sendMessage("§cBu bir mayın değil!");
                }
            }
        }
        
        // ESKİ KOD KALDIRILDI - Artık BlockPlaceEvent kullanılıyor
    }
    
    /**
     * Item'dan mayın tipini al
     */
    private NewMineManager.MineType getMineTypeFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        // Custom NBT tag kontrolü
        org.bukkit.persistence.PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(me.mami.stratocraft.Main.getInstance(), "MineType");
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            String mineId = container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
            // ID'den MineType'a çevir
            return convertIdToMineType(mineId);
        }
        
        return null;
    }
    
    /**
     * Item ID'den MineType'a çevir
     */
    private NewMineManager.MineType convertIdToMineType(String id) {
        // MINE_EXPLOSIVE -> EXPLOSIVE
        if (id.startsWith("MINE_")) {
            String typeStr = id.substring(5); // "MINE_" kısmını kaldır
            return NewMineManager.MineType.fromString(typeStr);
        }
        return null;
    }
    
    /**
     * Gizleme itemi mi?
     */
    private boolean isMineConcealer(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        org.bukkit.persistence.PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(me.mami.stratocraft.Main.getInstance(), "MineConcealer");
        
        return container.has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }
}

