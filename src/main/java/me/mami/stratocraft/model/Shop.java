package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Shop {
    private final UUID ownerId;
    private final Location location;
    private final ItemStack sellingItem;
    private final ItemStack priceItem;
    private final boolean protectedZone;

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
}

