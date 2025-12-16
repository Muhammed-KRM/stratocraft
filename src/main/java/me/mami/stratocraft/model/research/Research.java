package me.mami.stratocraft.model.research;

import me.mami.stratocraft.enums.ResearchType;
import me.mami.stratocraft.model.base.BaseModel;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Araştırma Veri Modeli
 * 
 * Araştırmaların tüm verilerini tutar.
 */
public class Research extends BaseModel {
    private UUID playerId; // Araştırmayı yapan oyuncu
    private UUID clanId; // Klan araştırması ise
    private ResearchType type;
    private String researchId; // Araştırma ID'si (recipe ID, structure ID, vb.)
    private Location researchLocation; // Araştırma masası konumu
    private ItemStack researchBook; // Araştırma kitabı
    private boolean isCompleted;
    private long completedTime;
    private int level; // Araştırma seviyesi
    
    public Research(UUID playerId, ResearchType type, String researchId, 
                   Location researchLocation, ItemStack researchBook, int level) {
        super();
        this.playerId = playerId;
        this.clanId = null;
        this.type = type;
        this.researchId = researchId;
        this.researchLocation = researchLocation;
        this.researchBook = researchBook;
        this.isCompleted = false;
        this.level = level;
    }
    
    public Research(UUID id, UUID playerId, ResearchType type, String researchId,
                   Location researchLocation, ItemStack researchBook, int level) {
        super(id);
        this.playerId = playerId;
        this.clanId = null;
        this.type = type;
        this.researchId = researchId;
        this.researchLocation = researchLocation;
        this.researchBook = researchBook;
        this.isCompleted = false;
        this.level = level;
    }
    
    // Getters
    public UUID getPlayerId() { return playerId; }
    public UUID getClanId() { return clanId; }
    public ResearchType getType() { return type; }
    public String getResearchId() { return researchId; }
    public Location getResearchLocation() { return researchLocation; }
    public ItemStack getResearchBook() { return researchBook; }
    public boolean isCompleted() { return isCompleted; }
    public long getCompletedTime() { return completedTime; }
    public int getLevel() { return level; }
    
    // Setters
    public void setPlayerId(UUID playerId) { this.playerId = playerId; updateTimestamp(); }
    public void setClanId(UUID clanId) { this.clanId = clanId; updateTimestamp(); }
    public void setType(ResearchType type) { this.type = type; updateTimestamp(); }
    public void setResearchId(String researchId) { this.researchId = researchId; updateTimestamp(); }
    public void setResearchLocation(Location researchLocation) { this.researchLocation = researchLocation; updateTimestamp(); }
    public void setResearchBook(ItemStack researchBook) { this.researchBook = researchBook; updateTimestamp(); }
    public void setCompleted(boolean completed) { 
        this.isCompleted = completed; 
        if (completed) {
            this.completedTime = System.currentTimeMillis();
        }
        updateTimestamp(); 
    }
    public void setLevel(int level) { this.level = level; updateTimestamp(); }
}

