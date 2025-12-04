package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ShopManager {
    private final Map<Location, Shop> shops = new HashMap<>();
    private final Main plugin;

    public ShopManager() {
        this.plugin = Main.getInstance();
    }

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
        
        // Vergi hesaplama (%5)
        if (shop.isProtectedZone()) {
            ItemStack taxItem = shop.getPriceItem().clone();
            taxItem.setAmount((int) Math.ceil(taxItem.getAmount() * 0.05));
            chest.getInventory().addItem(taxItem);
            
            ItemStack ownerPayment = shop.getPriceItem().clone();
            ownerPayment.setAmount(ownerPayment.getAmount() - taxItem.getAmount());
            chest.getInventory().addItem(ownerPayment);
        } else {
            chest.getInventory().addItem(shop.getPriceItem());
        }
        
        chest.getInventory().removeItem(shop.getSellingItem());
        buyer.getInventory().addItem(shop.getSellingItem());    
        
        buyer.sendMessage("§aSatın alma başarılı!" + (shop.isProtectedZone() ? " §7(%5 vergi alındı)" : ""));
    }
    
    // DataManager için
    public List<Shop> getAllShops() {
        return new ArrayList<>(shops.values());
    }
    
    public void loadShop(Shop shop) {
        shops.put(shop.getLocation(), shop);
    }
    
    // ========== TEKLİF SİSTEMİ ==========
    
    /**
     * Teklif Gönder
     */
    public void sendOffer(Player offerer, Shop shop, ItemStack offerItem, int offerAmount) {
        if (!shop.isAcceptOffers()) {
            offerer.sendMessage("§cBu mağaza teklif kabul etmiyor!");
            return;
        }
        
        if (shop.getOffers().size() >= shop.getMaxOffers()) {
            offerer.sendMessage("§cBu mağazaya maksimum teklif sayısına ulaşıldı!");
            return;
        }
        
        // Teklif oluştur
        Shop.Offer offer = new Shop.Offer(offerer.getUniqueId(), offerItem, offerAmount);
        shop.addOffer(offer);
        
        // Mağaza sahibine bildirim gönder
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§e════════════════════════════");
            owner.sendMessage("§eYENİ TEKLİF ALDIN!");
            owner.sendMessage("§7Teklif Veren: §e" + offerer.getName());
            owner.sendMessage("§7İstediğin: §e" + shop.getPriceItem().getType().name() + 
                            " x" + shop.getPriceItem().getAmount());
            owner.sendMessage("§7Teklif: §a" + offerItem.getType().name() + " x" + offerAmount);
            owner.sendMessage("§e/shop offers komutunu kullan");
            owner.sendMessage("§e════════════════════════════");
            
            // ActionBar bildirimi
            owner.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                    "§eYeni teklif! /shop offers komutunu kullan"));
        }
        
        // Teklif verene onay
        offerer.sendMessage("§aTeklifin gönderildi! Mağaza sahibi bildirildi.");
    }
    
    /**
     * Teklif Kabul Et
     */
    public void acceptOffer(Player owner, Shop shop, Shop.Offer offer) {
        if (!shop.getOwnerId().equals(owner.getUniqueId())) {
            owner.sendMessage("§cBu mağaza sana ait değil!");
            return;
        }
        
        Player offerer = Bukkit.getPlayer(offer.getOfferer());
        if (offerer == null || !offerer.isOnline()) {
            owner.sendMessage("§cTeklif veren oyuncu offline!");
            return;
        }
        
        // Teklif verenin envanterinde teklif item'ı var mı?
        if (!offerer.getInventory().containsAtLeast(offer.getOfferItem(), offer.getOfferAmount())) {
            owner.sendMessage("§cTeklif veren oyuncunun envanterinde yeterli item yok!");
            offerer.sendMessage("§cTeklifin kabul edildi ama envanterinde yeterli item yok!");
            return;
        }
        
        // Mağaza sahibinin envanterinde satılan item var mı?
        if (!owner.getInventory().containsAtLeast(shop.getSellingItem(), shop.getSellingItem().getAmount())) {
            owner.sendMessage("§cEnvanterinde satılan item yok!");
            return;
        }
        
        // Takas yap
        // 1. Teklif verenden teklif item'ını al
        offerer.getInventory().removeItem(new ItemStack(offer.getOfferItem().getType(), offer.getOfferAmount()));
        
        // 2. Mağaza sahibinden satılan item'ı al
        owner.getInventory().removeItem(shop.getSellingItem());
        
        // 3. Teklif verene satılan item'ı ver
        offerer.getInventory().addItem(shop.getSellingItem());
        
        // 4. Mağaza sahibine teklif item'ını ver
        owner.getInventory().addItem(new ItemStack(offer.getOfferItem().getType(), offer.getOfferAmount()));
        
        // Mesajlar
        owner.sendMessage("§aTeklif kabul edildi! Takas tamamlandı.");
        offerer.sendMessage("§aTeklifin kabul edildi! Takas tamamlandı.");
        
        // Teklifi listeden kaldır
        offer.setAccepted(true);
        shop.removeOffer(offer);
    }
    
    /**
     * Teklif Reddet
     */
    public void rejectOffer(Player owner, Shop shop, Shop.Offer offer) {
        if (!shop.getOwnerId().equals(owner.getUniqueId())) {
            owner.sendMessage("§cBu mağaza sana ait değil!");
            return;
        }
        
        Player offerer = Bukkit.getPlayer(offer.getOfferer());
        if (offerer != null && offerer.isOnline()) {
            offerer.sendMessage("§cTeklifin reddedildi.");
        }
        
        offer.setRejected(true);
        shop.removeOffer(offer);
    }
}

