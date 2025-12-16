package me.mami.stratocraft.model.bank;

import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Banka İşlem Veri Modeli
 * 
 * Banka işlemlerinin tüm verilerini tutar.
 */
public class BankTransaction extends BaseModel {
    private UUID bankId; // İşlem yapılan banka ID'si
    private UUID playerId; // İşlem yapan oyuncu
    private TransactionType type;
    private double amount; // Para miktarı (item işlemi ise 0)
    private ItemStack item; // Eşya (para işlemi ise null)
    private long transactionTime;
    private String description; // İşlem açıklaması
    
    /**
     * İşlem tipleri
     */
    public enum TransactionType {
        DEPOSIT_MONEY,      // Para yatırma
        WITHDRAW_MONEY,     // Para çekme
        DEPOSIT_ITEM,       // Eşya yatırma
        WITHDRAW_ITEM,      // Eşya çekme
        TRANSFER_MONEY,      // Para transferi
        TRANSFER_ITEM,       // Eşya transferi
        SALARY,              // Maaş
        TAX,                 // Vergi
        FEE                  // Ücret
    }
    
    public BankTransaction(UUID bankId, UUID playerId, TransactionType type, 
                         double amount, ItemStack item, String description) {
        super();
        this.bankId = bankId;
        this.playerId = playerId;
        this.type = type;
        this.amount = amount;
        this.item = item;
        this.transactionTime = System.currentTimeMillis();
        this.description = description;
    }
    
    public BankTransaction(UUID id, UUID bankId, UUID playerId, TransactionType type,
                         double amount, ItemStack item, String description) {
        super(id);
        this.bankId = bankId;
        this.playerId = playerId;
        this.type = type;
        this.amount = amount;
        this.item = item;
        this.transactionTime = System.currentTimeMillis();
        this.description = description;
    }
    
    // Getters
    public UUID getBankId() { return bankId; }
    public UUID getPlayerId() { return playerId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public ItemStack getItem() { return item; }
    public long getTransactionTime() { return transactionTime; }
    public String getDescription() { return description; }
    
    // Setters
    public void setBankId(UUID bankId) { this.bankId = bankId; updateTimestamp(); }
    public void setPlayerId(UUID playerId) { this.playerId = playerId; updateTimestamp(); }
    public void setType(TransactionType type) { this.type = type; updateTimestamp(); }
    public void setAmount(double amount) { this.amount = amount; updateTimestamp(); }
    public void setItem(ItemStack item) { this.item = item; updateTimestamp(); }
    public void setTransactionTime(long transactionTime) { this.transactionTime = transactionTime; updateTimestamp(); }
    public void setDescription(String description) { this.description = description; updateTimestamp(); }
}

