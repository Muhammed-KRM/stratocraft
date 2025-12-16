package me.mami.stratocraft.model.structure;

import me.mami.stratocraft.enums.StructureType;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Klan Yapısı Modeli
 * 
 * Klan yapıları için özel sınıf.
 * BaseStructure'den türer, klan bilgisi ekler.
 */
public class ClanStructure extends BaseStructure {
    private UUID clanId; // Hangi klana ait
    
    public ClanStructure(StructureType type, Location location, int level, UUID clanId) {
        super(type, location, level);
        this.clanId = clanId;
    }
    
    public ClanStructure(UUID id, StructureType type, Location location, int level, UUID clanId) {
        super(id, type, location, level);
        this.clanId = clanId;
    }
    
    public UUID getClanId() { 
        return clanId; 
    }
    
    public void setClanId(UUID clanId) { 
        this.clanId = clanId; 
        updateTimestamp(); 
    }
}

