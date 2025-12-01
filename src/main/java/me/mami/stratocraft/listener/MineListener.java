package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.MineManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Mayın Sistemi Dinleyicisi
 * - Mayın oluşturma (Basınç plakası + item)
 * - Mayın tetikleme (Basınç plakasına basma)
 * - Mayın kırma
 */
public class MineListener implements Listener {
    private final MineManager mineManager;
    
    public MineListener(MineManager mineManager) {
        this.mineManager = mineManager;
    }
    
    /**
     * Basınç plakasına basma (mayın tetikleme)
     * PlayerMoveEvent ile kontrol et
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Oyuncu sadece kafasını çevirmiş
        }
        
        Player player = event.getPlayer();
        Block standingBlock = event.getTo().getBlock();
        
        // Basınç plakası mı?
        if (standingBlock.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
            standingBlock.getType() != Material.HEAVY_WEIGHTED_PRESSURE_PLATE &&
            standingBlock.getType() != Material.STONE_PRESSURE_PLATE &&
            standingBlock.getType() != Material.OAK_PRESSURE_PLATE) {
            return;
        }
        
        // Mayın var mı?
        if (!standingBlock.hasMetadata("Mine")) {
            return;
        }
        
        // Mayın tetikle
        mineManager.triggerMine(standingBlock, player);
    }
    
    /**
     * Entity basınç plakasına basma (moblar için)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof Player) {
            return; // Oyuncular için PlayerMoveEvent kullanılıyor
        }
        
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        
        // Basınç plakası mı?
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
            block.getType() != Material.HEAVY_WEIGHTED_PRESSURE_PLATE &&
            block.getType() != Material.STONE_PRESSURE_PLATE &&
            block.getType() != Material.OAK_PRESSURE_PLATE) {
            return;
        }
        
        // Mayın var mı? (moblar için de tetiklenebilir)
        if (!block.hasMetadata("Mine")) {
            return;
        }
        
        // Moblar için mayın tetiklenmez (sadece oyuncular için)
        // İsterseniz buraya mob tetikleme kodu ekleyebilirsiniz
    }
    
    /**
     * Mayın oluşturma (Basınç plakası + item ile sağ tık)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMineCreate(PlayerInteractEvent event) {
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
        
        // Basınç plakası mı?
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
            block.getType() != Material.HEAVY_WEIGHTED_PRESSURE_PLATE &&
            block.getType() != Material.STONE_PRESSURE_PLATE &&
            block.getType() != Material.OAK_PRESSURE_PLATE) {
            return;
        }
        
        // Zaten mayın var mı?
        if (block.hasMetadata("Mine")) {
            event.setCancelled(true);
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Mayın tipini belirle
        MineManager.MineType mineType = determineMineType(item);
        if (mineType == null) {
            return;
        }
        
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
    
    /**
     * Mayın tipini belirle (item'a göre)
     */
    private MineManager.MineType determineMineType(ItemStack item) {
        if (item == null) {
            return null;
        }
        
        Material material = item.getType();
        
        // TNT - Patlama mayını
        if (material == Material.TNT) {
            return MineManager.MineType.EXPLOSIVE;
        }
        
        // Lightning Core - Yıldırım mayını
        if (ItemManager.isCustomItem(item, "LIGHTNING_CORE")) {
            return MineManager.MineType.LIGHTNING;
        }
        
        // Spider Eye - Zehir mayını
        if (material == Material.SPIDER_EYE) {
            return MineManager.MineType.POISON;
        }
        
        // Ink Sac - Körlük mayını
        if (material == Material.INK_SAC) {
            return MineManager.MineType.BLINDNESS;
        }
        
        // Iron Pickaxe - Yorgunluk mayını
        if (material == Material.IRON_PICKAXE) {
            return MineManager.MineType.FATIGUE;
        }
        
        // Slime Ball - Yavaşlık mayını
        if (material == Material.SLIME_BALL) {
            return MineManager.MineType.SLOWNESS;
        }
        
        return null;
    }
    
    /**
     * Mayın kırma (sahip kırabilir)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMineBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Basınç plakası mı?
        if (block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
            block.getType() != Material.HEAVY_WEIGHTED_PRESSURE_PLATE &&
            block.getType() != Material.STONE_PRESSURE_PLATE &&
            block.getType() != Material.OAK_PRESSURE_PLATE) {
            return;
        }
        
        // Mayın var mı?
        if (!block.hasMetadata("Mine")) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Sahip kontrolü
        if (block.hasMetadata("MineOwner")) {
            String ownerStr = block.getMetadata("MineOwner").get(0).asString();
            if (player.getUniqueId().toString().equals(ownerStr)) {
                // Sahip, mayını kaldır
                mineManager.removeMine(block.getLocation());
                player.sendMessage("§aMayın kaldırıldı!");
            } else {
                // Sahip değil, kırma
                event.setCancelled(true);
                player.sendMessage("§cBu mayın sana ait değil!");
            }
        }
    }
}

