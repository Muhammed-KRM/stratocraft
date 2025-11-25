package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Shop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class ShopManager {
    private final Map<Location, Shop> shops = new HashMap<>();

    public void createShop(Player owner, Location chestLoc, ItemStack sell, ItemStack price, boolean protectedZone) {
        Shop shop = new Shop(owner.getUniqueId(), chestLoc, sell, price, protectedZone);
        shops.put(chestLoc, shop);
    }

    public Shop getShop(Location loc) { return shops.get(loc); }

    public void handlePurchase(Player buyer, Shop shop) {
        Block b = shop.getLocation().getBlock();
        if (b.getType() != Material.CHEST) return;
        Chest chest = (Chest) b.getState();
        
        if (!chest.getInventory().containsAtLeast(shop.getSellingItem(), shop.getSellingItem().getAmount())) {
            buyer.sendMessage("§cMarket stoğu tükenmiş!");
            return;
        }

        if (!buyer.getInventory().containsAtLeast(shop.getPriceItem(), shop.getPriceItem().getAmount())) {
            buyer.sendMessage("§cYeterli ödemeye sahip değilsin!");
            return;
        }

        buyer.getInventory().removeItem(shop.getPriceItem()); 
        chest.getInventory().addItem(shop.getPriceItem()); 
        
        chest.getInventory().removeItem(shop.getSellingItem());
        buyer.getInventory().addItem(shop.getSellingItem());    
        
        buyer.sendMessage("§aSatın alma başarılı!");
    }
}

