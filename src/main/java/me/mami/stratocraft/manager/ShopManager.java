package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Shop;

public class ShopManager {
    private final Map<Location, Shop> shops = new HashMap<>();
    private final Main plugin;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;

    public ShopManager() {
        this.plugin = Main.getInstance();
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    private double getTaxPercentage() {
        return balanceConfig != null ? balanceConfig.getShopTaxPercentage() : 0.05;
    }

    public void createShop(Player owner, Location chestLoc, ItemStack sell, ItemStack price, boolean protectedZone) {
        Shop shop = new Shop(owner.getUniqueId(), chestLoc, sell, price, protectedZone);
        shops.put(chestLoc, shop);
    }

    public Shop getShop(Location loc) { return shops.get(loc); }

    public void handlePurchase(Player buyer, Shop shop) {
        // KRİTİK: Kendinle ticaret engelleme
        if (shop.getOwnerId().equals(buyer.getUniqueId())) {
            buyer.sendMessage("§cKendi marketinden alışveriş yapamazsın!");
            return;
        }
        
        Block b = shop.getLocation().getBlock();
        if (b.getType() != Material.CHEST) {
            buyer.sendMessage("§cMarket sandığı bulunamadı!");
            return;
        }
        
        // KRİTİK: Fiziksel sandığı tekrar kontrol et (dupe önleme)
        Chest chest = (Chest) b.getState();
        if (chest == null) {
            buyer.sendMessage("§cMarket sandığı erişilemez!");
            return;
        }
        
        // KRİTİK: Null check'ler
        ItemStack priceItem = shop.getPriceItem();
        ItemStack sellingItem = shop.getSellingItem();
        
        if (priceItem == null || sellingItem == null) {
            buyer.sendMessage("§cMarket bilgileri hatalı!");
            return;
        }
        
        // KRİTİK: Stok kontrolü - GUI snapshot yerine anlık kontrol
        if (!chest.getInventory().containsAtLeast(sellingItem, sellingItem.getAmount())) {
            buyer.sendMessage("§cMarket stoğu tükenmiş!");
            return;
        }

        if (!buyer.getInventory().containsAtLeast(priceItem, priceItem.getAmount())) {
            buyer.sendMessage("§cYeterli ödemeye sahip değilsin!");
            return;
        }

        // KRİTİK: Anlık bölge kontrolü (vergi kaçırma önleme)
        boolean isProtectedZone = false;
        if (plugin != null && plugin.getTerritoryManager() != null) {
            me.mami.stratocraft.model.Clan territoryOwner = plugin.getTerritoryManager().getTerritoryOwner(shop.getLocation());
            isProtectedZone = (territoryOwner != null);
        }

        // KRİTİK: Ödemeyi al (clone kullan - orijinal item'ı koru)
        ItemStack paymentClone = priceItem.clone();
        HashMap<Integer, ItemStack> removeResult = buyer.getInventory().removeItem(paymentClone);
        
        // Ödeme alınamadı mı? (race condition önleme)
        if (!removeResult.isEmpty()) {
            buyer.sendMessage("§cÖdeme alınamadı! Lütfen tekrar deneyin.");
            return;
        }
        
        // KRİTİK: Stok tekrar kontrolü (race condition önleme - ödeme alındıktan sonra)
        if (!chest.getInventory().containsAtLeast(sellingItem, sellingItem.getAmount())) {
            // Stok tükendi, ödemeyi geri ver
            HashMap<Integer, ItemStack> refundResult = buyer.getInventory().addItem(paymentClone);
            if (!refundResult.isEmpty()) {
                // Envanter dolu, yere düşür
                for (ItemStack remaining : refundResult.values()) {
                    if (remaining != null) {
                        buyer.getWorld().dropItemNaturally(buyer.getLocation(), remaining);
                    }
                }
            }
            buyer.sendMessage("§cMarket stoğu tükenmiş! Ödemeniz iade edildi.");
            return;
        }
        
        // Stoktan item'i al
        HashMap<Integer, ItemStack> removeStockResult = chest.getInventory().removeItem(sellingItem);
        if (!removeStockResult.isEmpty()) {
            // Stok alınamadı (çok nadir durum), ödemeyi geri ver
            HashMap<Integer, ItemStack> refundResult = buyer.getInventory().addItem(paymentClone);
            if (!refundResult.isEmpty()) {
                for (ItemStack remaining : refundResult.values()) {
                    if (remaining != null) {
                        buyer.getWorld().dropItemNaturally(buyer.getLocation(), remaining);
                    }
                }
            }
            buyer.sendMessage("§cStok alınamadı! Ödemeniz iade edildi.");
            return;
        }
        
        // Vergi hesaplama (config'den) - Anlık bölge kontrolüne göre
        if (isProtectedZone) {
            double taxPercentage = getTaxPercentage();
            ItemStack taxItem = paymentClone.clone();
            taxItem.setAmount((int) Math.ceil(taxItem.getAmount() * taxPercentage));
            
            // Vergiyi sandığa ekle
            HashMap<Integer, ItemStack> taxOverflow = chest.getInventory().addItem(taxItem);
            if (!taxOverflow.isEmpty()) {
                // Sandık dolu, vergi yere düşer (nadir durum)
                for (ItemStack remaining : taxOverflow.values()) {
                    if (remaining != null) {
                        buyer.getWorld().dropItemNaturally(buyer.getLocation(), remaining);
                    }
                }
            }
            
            // Sahibin ödemesini hesapla
            ItemStack ownerPayment = paymentClone.clone();
            ownerPayment.setAmount(ownerPayment.getAmount() - taxItem.getAmount());
            if (ownerPayment.getAmount() > 0) {
                HashMap<Integer, ItemStack> ownerOverflow = chest.getInventory().addItem(ownerPayment);
                if (!ownerOverflow.isEmpty()) {
                    // Sandık dolu, sahibin ödemesi yere düşer (nadir durum)
                    for (ItemStack remaining : ownerOverflow.values()) {
                        if (remaining != null) {
                            buyer.getWorld().dropItemNaturally(buyer.getLocation(), remaining);
                        }
                    }
                }
            }
        } else {
            // Vergi yok, tüm ödemeyi sandığa ekle
            HashMap<Integer, ItemStack> paymentOverflow = chest.getInventory().addItem(paymentClone);
            if (!paymentOverflow.isEmpty()) {
                // Sandık dolu, ödeme yere düşer (nadir durum)
                for (ItemStack remaining : paymentOverflow.values()) {
                    if (remaining != null) {
                        buyer.getWorld().dropItemNaturally(buyer.getLocation(), remaining);
                    }
                }
            }
        }
        
        // KRİTİK: Envanter kontrolü - Ödül yere düşebilir
        HashMap<Integer, ItemStack> rewardOverflow = buyer.getInventory().addItem(sellingItem);
        if (!rewardOverflow.isEmpty()) {
            // Envanter dolu, yere düşür
            for (ItemStack remaining : rewardOverflow.values()) {
                if (remaining != null) {
                    buyer.getWorld().dropItemNaturally(buyer.getLocation(), remaining);
                }
            }
            buyer.sendMessage("§eEnvanterin dolu! Ödül yere düştü.");
        }
        
        buyer.sendMessage("§aSatın alma başarılı!" + (isProtectedZone ? " §7(%5 vergi alındı)" : ""));
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
        
        // Takas yap (null check ve overflow kontrolü ile)
        ItemStack offerItemClone = offer.getOfferItem().clone();
        offerItemClone.setAmount(offer.getOfferAmount());
        ItemStack sellingItemClone = shop.getSellingItem().clone();
        
        // 1. Teklif verenden teklif item'ını al
        HashMap<Integer, ItemStack> offerRemoveResult = offerer.getInventory().removeItem(offerItemClone);
        if (!offerRemoveResult.isEmpty()) {
            owner.sendMessage("§cTeklif veren oyuncunun envanterinde yeterli item yok!");
            return;
        }
        
        // 2. Mağaza sahibinden satılan item'ı al
        HashMap<Integer, ItemStack> ownerRemoveResult = owner.getInventory().removeItem(sellingItemClone);
        if (!ownerRemoveResult.isEmpty()) {
            // Satılan item alınamadı, teklif item'ını geri ver
            HashMap<Integer, ItemStack> refundResult = offerer.getInventory().addItem(offerItemClone);
            if (!refundResult.isEmpty()) {
                for (ItemStack remaining : refundResult.values()) {
                    if (remaining != null) {
                        offerer.getWorld().dropItemNaturally(offerer.getLocation(), remaining);
                    }
                }
            }
            owner.sendMessage("§cEnvanterinde satılan item yok!");
            return;
        }
        
        // 3. Teklif verene satılan item'ı ver (overflow kontrolü)
        HashMap<Integer, ItemStack> offerAddResult = offerer.getInventory().addItem(sellingItemClone);
        if (!offerAddResult.isEmpty()) {
            // Envanter dolu, yere düşür
            for (ItemStack remaining : offerAddResult.values()) {
                if (remaining != null) {
                    offerer.getWorld().dropItemNaturally(offerer.getLocation(), remaining);
                }
            }
            offerer.sendMessage("§eEnvanterin dolu! Ödül yere düştü.");
        }
        
        // 4. Mağaza sahibine teklif item'ını ver (overflow kontrolü)
        HashMap<Integer, ItemStack> ownerAddResult = owner.getInventory().addItem(offerItemClone);
        if (!ownerAddResult.isEmpty()) {
            // Envanter dolu, yere düşür
            for (ItemStack remaining : ownerAddResult.values()) {
                if (remaining != null) {
                    owner.getWorld().dropItemNaturally(owner.getLocation(), remaining);
                }
            }
            owner.sendMessage("§eEnvanterin dolu! Ödül yere düştü.");
        }
        
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

