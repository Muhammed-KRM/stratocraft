package me.mami.stratocraft.model.market;

import me.mami.stratocraft.enums.MarketType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Market Veri Modeli
 * 
 * Market'lerin tüm verilerini tutar.
 */
public class Market extends BaseModel {
    private MarketType type;
    private UUID ownerId; // Market sahibi (oyuncu veya klan)
    private UUID clanId; // Klan marketi ise
    private Location location; // Market konumu
    private List<MarketItem> items; // Market'teki eşyalar
    private boolean isActive;
    private double taxRate; // Vergi oranı
    
    /**
     * Market eşyası
     */
    public static class MarketItem {
        private ItemStack item;
        private double price;
        private int stock; // Stok miktarı (-1 ise sınırsız)
        private UUID sellerId; // Satıcı ID'si
        
        public MarketItem(ItemStack item, double price, int stock, UUID sellerId) {
            this.item = item;
            this.price = price;
            this.stock = stock;
            this.sellerId = sellerId;
        }
        
        // Getters
        public ItemStack getItem() { return item; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
        public UUID getSellerId() { return sellerId; }
        
        // Setters
        public void setItem(ItemStack item) { this.item = item; }
        public void setPrice(double price) { this.price = price; }
        public void setStock(int stock) { this.stock = stock; }
        public void setSellerId(UUID sellerId) { this.sellerId = sellerId; }
    }
    
    public Market(MarketType type, UUID ownerId, Location location) {
        super();
        this.type = type;
        this.ownerId = ownerId;
        this.clanId = null;
        this.location = location;
        this.items = new ArrayList<>();
        this.isActive = true;
        this.taxRate = 0.1; // Varsayılan %10 vergi
    }
    
    public Market(UUID id, MarketType type, UUID ownerId, Location location) {
        super(id);
        this.type = type;
        this.ownerId = ownerId;
        this.clanId = null;
        this.location = location;
        this.items = new ArrayList<>();
        this.isActive = true;
        this.taxRate = 0.1;
    }
    
    // Getters
    public MarketType getType() { return type; }
    public UUID getOwnerId() { return ownerId; }
    public UUID getClanId() { return clanId; }
    public Location getLocation() { return location; }
    public List<MarketItem> getItems() { return items; }
    public boolean isActive() { return isActive; }
    public double getTaxRate() { return taxRate; }
    
    // Setters
    public void setType(MarketType type) { this.type = type; updateTimestamp(); }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; updateTimestamp(); }
    public void setClanId(UUID clanId) { this.clanId = clanId; updateTimestamp(); }
    public void setLocation(Location location) { this.location = location; updateTimestamp(); }
    public void setItems(List<MarketItem> items) { this.items = items; updateTimestamp(); }
    public void setActive(boolean active) { this.isActive = active; updateTimestamp(); }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; updateTimestamp(); }
    
    // Helper methods
    public void addItem(MarketItem item) {
        if (item != null) {
            this.items.add(item);
            updateTimestamp();
        }
    }
    
    public void removeItem(MarketItem item) {
        this.items.remove(item);
        updateTimestamp();
    }
}

