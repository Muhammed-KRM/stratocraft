package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ShopManager;
import me.mami.stratocraft.model.Shop;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {
    private final ShopManager shopManager;

    public ShopListener(ShopManager sm) { this.shopManager = sm; }

    @EventHandler
    public void onShopInteract(PlayerInteractEvent event) {
        // MEVCUT MARKETLE ETKİLEŞİM (SATIN ALMA)
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {
            Shop shop = shopManager.getShop(event.getClickedBlock().getLocation());
            if (shop != null) {
                event.setCancelled(true); // Sandığı açma, alışveriş yap
                shopManager.handlePurchase(event.getPlayer(), shop);
                return;
            }
        }
    }

    // YENİ: MARKET KURULUMU (TABELA İLE)
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // Tabela formatı:
        // Satır 1: [MARKET]
        // (Diğer satırlar otomatik dolacak veya dekoratif kalacak)
        
        if (event.getLine(0).equalsIgnoreCase("[MARKET]")) {
            org.bukkit.block.Block block = event.getBlock();
            // Tabelanın altındaki blok Sandık mı?
            org.bukkit.block.Block attached = block.getRelative(org.bukkit.block.BlockFace.DOWN); // Basitçe altı kontrol edelim
            
            if (attached.getType() == Material.CHEST) {
                Chest chest = (Chest) attached.getState();
                
                // Market kurmak için sandığın içinde:
                // 1. Slot (0): Satılacak Eşya
                // 2. Slot (1): İstenen Ücret Eşyası (Örn: Altın)
                ItemStack sellItem = chest.getInventory().getItem(0);
                ItemStack priceItem = chest.getInventory().getItem(1);

                if (sellItem == null || priceItem == null) {
                    event.getPlayer().sendMessage("§cMarket kurmak için sandığın ilk slotuna SATILACAK, ikinci slotuna ÜCRET eşyasını koymalısın.");
                    event.setLine(0, "§cHATA");
                    return;
                }

                // Market Oluştur
                // protectedZone parametresi basitlik için false, klan bölgesi kontrolü eklenebilir.
                shopManager.createShop(event.getPlayer(), attached.getLocation(), sellItem, priceItem, true);
                
                // Tabelayı Güncelle
                event.setLine(0, "§a[MARKET]");
                event.setLine(1, sellItem.getAmount() + "x " + sellItem.getType().toString());
                event.setLine(2, "Fiyat:");
                event.setLine(3, priceItem.getAmount() + "x " + priceItem.getType().toString());
                
                event.getPlayer().sendMessage("§aMarket başarıyla kuruldu!");
            }
        }
    }
}

