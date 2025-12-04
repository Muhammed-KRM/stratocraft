package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Shop {
    private final UUID ownerId;
    private final Location location;
    private final ItemStack sellingItem;
    private final ItemStack priceItem;
    private final boolean protectedZone;
    
    // Teklif Sistemi
    private final List<Offer> offers = new ArrayList<>(); // Gelen teklifler
    private boolean acceptOffers = true; // Teklif kabul ediyor mu?
    private int maxOffers = 10; // Maksimum teklif sayısı
    
    public static class Offer {
        private final UUID offerer; // Teklif veren
        private final ItemStack offerItem; // Teklif edilen item
        private final int offerAmount; // Teklif miktarı
        private final long offerTime; // Teklif zamanı
        private boolean accepted = false; // Kabul edildi mi?
        private boolean rejected = false; // Reddedildi mi?
        
        public Offer(UUID offerer, ItemStack offerItem, int offerAmount) {
            this.offerer = offerer;
            this.offerItem = offerItem;
            this.offerAmount = offerAmount;
            this.offerTime = System.currentTimeMillis();
        }
        
        public UUID getOfferer() { return offerer; }
        public ItemStack getOfferItem() { return offerItem; }
        public int getOfferAmount() { return offerAmount; }
        public long getOfferTime() { return offerTime; }
        public boolean isAccepted() { return accepted; }
        public void setAccepted(boolean accepted) { this.accepted = accepted; }
        public boolean isRejected() { return rejected; }
        public void setRejected(boolean rejected) { this.rejected = rejected; }
    }

    public Shop(UUID ownerId, Location location, ItemStack sellingItem, ItemStack priceItem, boolean protectedZone) {
        this.ownerId = ownerId;
        this.location = location;
        this.sellingItem = sellingItem;
        this.priceItem = priceItem;
        this.protectedZone = protectedZone;
    }

    public UUID getOwnerId() { return ownerId; }
    public Location getLocation() { return location; }
    public ItemStack getSellingItem() { return sellingItem; }
    public ItemStack getPriceItem() { return priceItem; }
    public boolean isProtectedZone() { return protectedZone; }
    
    // Teklif Sistemi Getter/Setter
    public List<Offer> getOffers() { return offers; }
    public void addOffer(Offer offer) { 
        if (offers.size() < maxOffers) {
            offers.add(offer);
        }
    }
    public void removeOffer(Offer offer) { offers.remove(offer); }
    public boolean isAcceptOffers() { return acceptOffers; }
    public void setAcceptOffers(boolean accept) { this.acceptOffers = accept; }
    public int getMaxOffers() { return maxOffers; }
    public void setMaxOffers(int max) { this.maxOffers = max; }
}

