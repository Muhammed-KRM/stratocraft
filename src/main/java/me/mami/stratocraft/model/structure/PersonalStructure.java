package me.mami.stratocraft.model.structure;

import me.mami.stratocraft.enums.StructureType;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Kişisel Yapı Modeli
 * 
 * Kişisel yapılar için özel sınıf.
 * BaseStructure'den türer, sahip bilgisi ekler.
 */
public class PersonalStructure extends BaseStructure {
    // ownerId zaten BaseStructure'da var, burada sadece özel metodlar olabilir
    
    public PersonalStructure(StructureType type, Location location, int level, UUID ownerId) {
        super(type, location, level);
        setOwnerId(ownerId);
    }
    
    public PersonalStructure(UUID id, StructureType type, Location location, int level, UUID ownerId) {
        super(id, type, location, level);
        setOwnerId(ownerId);
    }
}

