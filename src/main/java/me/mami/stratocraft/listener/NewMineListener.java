package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.NewMineManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
        
        // Basınç plakası aktif mi kontrol et
        org.bukkit.block.data.Powerable powerable = (org.bukkit.block.data.Powerable) standingBlock.getBlockData();
        if (powerable.isPowered()) {
            // Mayın tetikle
            mineManager.triggerMine(standingBlock, player);
        }
    }
    
    /**
     * Oyuncu etkileşimi (mayın oluşturma ve gizleme)
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
                    if (mineManager.toggleMineVisibility(block.getLocation())) {
                        NewMineManager.MineData mine = mineManager.getMine(block.getLocation());
                        if (mine != null && mine.isHidden()) {
                            player.sendMessage("§aMayın gizlendi!");
                        } else {
                            player.sendMessage("§aMayın görünür yapıldı!");
                        }
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
        
        // Mayın oluşturma (item ile basınç plakasına sağ tık)
        if (!player.isSneaking() && NewMineManager.isPressurePlate(block.getType())) {
            // Zaten mayın var mı?
            if (block.hasMetadata("NewMine")) {
                event.setCancelled(true);
                return;
            }
            
            // Mayın itemı mı?
            NewMineManager.MineType mineType = getMineTypeFromItem(item);
            if (mineType != null) {
                // Mayın oluştur
                if (mineManager.createMine(player, block, mineType)) {
                    // Item'ı tüket (1 adet)
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                    event.setCancelled(true);
                }
            }
        }
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

