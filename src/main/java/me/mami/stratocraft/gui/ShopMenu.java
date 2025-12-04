package me.mami.stratocraft.gui;

import me.mami.stratocraft.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ShopMenu {
    
    /**
     * Mağaza Menüsü
     */
    public static Inventory createShopMenu(Shop shop) {
        Inventory menu = Bukkit.createInventory(null, 27, "§aMarket");
        
        // Satılan item (Slot 11)
        ItemStack sellingItem = shop.getSellingItem().clone();
        ItemMeta sellingMeta = sellingItem.getItemMeta();
        List<String> sellingLore = new ArrayList<>();
        sellingLore.add("§7Satılan Item");
        sellingLore.add("§7Miktar: §e" + sellingItem.getAmount());
        sellingMeta.setLore(sellingLore);
        sellingItem.setItemMeta(sellingMeta);
        menu.setItem(11, sellingItem);
        
        // İstenen item (Slot 13)
        ItemStack priceItem = shop.getPriceItem().clone();
        ItemMeta priceMeta = priceItem.getItemMeta();
        List<String> priceLore = new ArrayList<>();
        priceLore.add("§7İstenen Ödeme");
        priceLore.add("§7Miktar: §e" + priceItem.getAmount());
        priceMeta.setLore(priceLore);
        priceItem.setItemMeta(priceMeta);
        menu.setItem(13, priceItem);
        
        // Satın Al butonu (Slot 15)
        menu.setItem(15, createButton(Material.EMERALD_BLOCK, "§a[Satın Al]", 
            "§7Klasik satın alma"));
        
        // Teklif Ver butonu (Slot 17)
        if (shop.isAcceptOffers()) {
            menu.setItem(17, createButton(Material.GOLD_BLOCK, "§e[Teklif Ver]", 
                "§7Alternatif ödeme teklif et"));
        }
        
        // Teklifler butonu (Slot 22) - Sadece mağaza sahibi için
        if (shop.getOffers().size() > 0) {
            menu.setItem(22, createButton(Material.PAPER, "§eTeklifler (" + 
                shop.getOffers().size() + ")", "§7Gelen teklifleri gör"));
        }
        
        // Kapat butonu (Slot 26)
        menu.setItem(26, createButton(Material.BARRIER, "§cKapat", null));
        
        return menu;
    }
    
    /**
     * Teklif Verme Menüsü
     */
    public static Inventory createOfferMenu(Shop shop) {
        Inventory menu = Bukkit.createInventory(null, 27, "§eTeklif Ver");
        
        // İstenen item bilgisi (Slot 4)
        ItemStack wantedItem = shop.getPriceItem().clone();
        ItemMeta wantedMeta = wantedItem.getItemMeta();
        List<String> wantedLore = new ArrayList<>();
        wantedLore.add("§7Mağaza sahibi bunu istiyor:");
        wantedLore.add("§7" + wantedItem.getType().name() + " x" + wantedItem.getAmount());
        wantedMeta.setLore(wantedLore);
        wantedItem.setItemMeta(wantedMeta);
        menu.setItem(4, wantedItem);
        
        // Teklif item'ı seç (Slot 13) - Oyuncu envanterinden seçecek
        menu.setItem(13, createButton(Material.CHEST, "§eTeklif Item'ı Seç", 
            "§7Envanterinden item seç"));
        
        // Miktar ayarla (Slot 11, 15)
        menu.setItem(11, createButton(Material.REDSTONE, "§c-1", "§7Miktar azalt"));
        menu.setItem(15, createButton(Material.EMERALD, "§a+1", "§7Miktar artır"));
        
        // Teklif Gönder butonu (Slot 22)
        menu.setItem(22, createButton(Material.EMERALD_BLOCK, "§a[Teklif Gönder]", null));
        
        // Geri butonu (Slot 18)
        menu.setItem(18, createButton(Material.ARROW, "§eGeri", null));
        
        return menu;
    }
    
    /**
     * Teklifler Listesi Menüsü (Mağaza sahibi için)
     */
    public static Inventory createOffersMenu(Shop shop) {
        Inventory menu = Bukkit.createInventory(null, 54, "§eGelen Teklifler");
        
        int slot = 0;
        for (Shop.Offer offer : shop.getOffers()) {
            if (slot >= 45) break; // 45 slot yeterli
            
            ItemStack offerItem = new ItemStack(offer.getOfferItem().getType(), offer.getOfferAmount());
            ItemMeta meta = offerItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Teklif Veren: §e" + Bukkit.getOfflinePlayer(offer.getOfferer()).getName());
            lore.add("§7Teklif: §a" + offer.getOfferItem().getType().name() + " x" + offer.getOfferAmount());
            lore.add("§7Zaman: §e" + formatTime(offer.getOfferTime()));
            lore.add("");
            lore.add("§a[Kabul Et]");
            lore.add("§c[Reddet]");
            meta.setLore(lore);
            offerItem.setItemMeta(meta);
            menu.setItem(slot, offerItem);
            slot++;
        }
        
        // Kapat butonu (Slot 49)
        menu.setItem(49, createButton(Material.BARRIER, "§cKapat", null));
        
        return menu;
    }
    
    /**
     * Yardımcı metod: Buton oluştur
     */
    private static ItemStack createButton(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (description != null) {
            List<String> lore = new ArrayList<>();
            lore.add(description);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Zaman formatla
     */
    private static String formatTime(long timeMillis) {
        long seconds = (System.currentTimeMillis() - timeMillis) / 1000;
        if (seconds < 60) {
            return seconds + " saniye önce";
        } else if (seconds < 3600) {
            return (seconds / 60) + " dakika önce";
        } else {
            return (seconds / 3600) + " saat önce";
        }
    }
}

