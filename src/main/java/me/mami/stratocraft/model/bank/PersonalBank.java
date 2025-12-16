package me.mami.stratocraft.model.bank;

import me.mami.stratocraft.enums.BankAccountType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kişisel Banka Veri Modeli
 * 
 * Kişisel banka hesaplarının tüm verilerini tutar.
 */
public class PersonalBank extends BaseModel {
    private UUID playerId;
    private BankAccountType accountType;
    private Location bankLocation; // Banka sandığı konumu
    private List<ItemStack> items; // Bankadaki eşyalar
    private double balance; // Para bakiyesi
    private int maxSlots; // Maksimum slot sayısı
    private boolean isActive;
    
    public PersonalBank(UUID playerId, BankAccountType accountType, Location bankLocation) {
        super();
        this.playerId = playerId;
        this.accountType = accountType;
        this.bankLocation = bankLocation;
        this.items = new ArrayList<>();
        this.balance = 0.0;
        this.maxSlots = 54; // Varsayılan 54 slot (Ender Chest)
        this.isActive = true;
    }
    
    public PersonalBank(UUID id, UUID playerId, BankAccountType accountType, Location bankLocation) {
        super(id);
        this.playerId = playerId;
        this.accountType = accountType;
        this.bankLocation = bankLocation;
        this.items = new ArrayList<>();
        this.balance = 0.0;
        this.maxSlots = 54;
        this.isActive = true;
    }
    
    // Getters
    public UUID getPlayerId() { return playerId; }
    public BankAccountType getAccountType() { return accountType; }
    public Location getBankLocation() { return bankLocation; }
    public List<ItemStack> getItems() { return items; }
    public double getBalance() { return balance; }
    public int getMaxSlots() { return maxSlots; }
    public boolean isActive() { return isActive; }
    
    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; updateTimestamp(); }
    public void setAccountType(BankAccountType accountType) { this.accountType = accountType; updateTimestamp(); }
    public void setBankLocation(Location bankLocation) { this.bankLocation = bankLocation; updateTimestamp(); }
    public void setItems(List<ItemStack> items) { this.items = items; updateTimestamp(); }
    public void setBalance(double balance) { this.balance = balance; updateTimestamp(); }
    public void setMaxSlots(int maxSlots) { this.maxSlots = maxSlots; updateTimestamp(); }
    public void setActive(boolean active) { this.isActive = active; updateTimestamp(); }
    
    // Helper methods
    public void addItem(ItemStack item) {
        if (item != null) {
            this.items.add(item);
            updateTimestamp();
        }
    }
    
    public void removeItem(ItemStack item) {
        this.items.remove(item);
        updateTimestamp();
    }
    
    public void addBalance(double amount) {
        this.balance += amount;
        updateTimestamp();
    }
    
    public void removeBalance(double amount) {
        this.balance = Math.max(0, this.balance - amount);
        updateTimestamp();
    }
}

