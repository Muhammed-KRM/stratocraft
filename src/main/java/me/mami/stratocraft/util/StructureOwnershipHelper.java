package me.mami.stratocraft.util;

import me.mami.stratocraft.enums.StructureOwnershipType;
import me.mami.stratocraft.enums.StructureType;

/**
 * Yapı Sahiplik Yardımcı Sınıfı
 * 
 * Yapı tiplerine göre sahiplik tipini belirler.
 */
public class StructureOwnershipHelper {
    
    /**
     * Yapı tipine göre sahiplik tipini döndürür
     * 
     * @param type Yapı tipi
     * @return Sahiplik tipi
     */
    public static StructureOwnershipType getOwnershipType(StructureType type) {
        if (type == null) {
            return StructureOwnershipType.CLAN_ONLY; // Varsayılan
        }
        
        switch (type) {
            // CLAN_ONLY - Sadece klan alanına yapılabilen yapılar
            case CORE:
            case CLAN_MANAGEMENT_CENTER:
            case CLAN_BANK:
            case CLAN_MISSION_GUILD:
            case ALCHEMY_TOWER:
            case POISON_REACTOR:
            case TECTONIC_STABILIZER:
            case SIEGE_FACTORY:
            case WALL_GENERATOR:
            case GRAVITY_WELL:
            case LAVA_TRENCHER:
            case WATCHTOWER:
            case DRONE_STATION:
            case AUTO_TURRET:
            case CATAPULT:
            case GLOBAL_MARKET_GATE:
            case AUTO_DRILL:
            case XP_BANK:
            case MAG_RAIL:
            case TELEPORTER:
            case FOOD_SILO:
            case OIL_REFINERY:
            case HEALING_BEACON:
            case WEATHER_MACHINE:
            case CROP_ACCELERATOR:
            case MOB_GRINDER:
            case INVISIBILITY_CLOAK:
            case ARMORY:
            case LIBRARY:
            case WARNING_SIGN:
            case TRAINING_ARENA:
            case CARAVAN_STATION:
                return StructureOwnershipType.CLAN_ONLY;
            
            // CLAN_OWNED - Klan dışına yapılabilen ama sadece yapan oyuncu ve klanının kullanabildiği
            // (Şu an için özel bir yapı yok, ileride eklenebilir)
            // Örnek: Özel bir yapı tipi eklenirse buraya eklenir
            
            // PUBLIC - Her yere yapılabilen ve herkesin kullanabildiği yapılar
            case PERSONAL_MISSION_GUILD:
            case CONTRACT_OFFICE:
            case MARKET_PLACE:
            case RECIPE_LIBRARY:
                return StructureOwnershipType.PUBLIC;
            
            default:
                return StructureOwnershipType.CLAN_ONLY; // Varsayılan
        }
    }
    
    /**
     * Yapı tipine göre sahiplik kontrolü gerekip gerekmediğini döndürür
     * 
     * @param type Yapı tipi
     * @return Sahiplik kontrolü gerekli mi?
     */
    public static boolean requiresOwnershipCheck(StructureType type) {
        StructureOwnershipType ownershipType = getOwnershipType(type);
        return ownershipType != StructureOwnershipType.PUBLIC;
    }
}

