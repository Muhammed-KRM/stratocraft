package me.mami.stratocraft.enums;

/**
 * Yapı Sahiplik Tipi Enum
 * 
 * Yapıların sahiplik ve kullanım haklarını belirler.
 */
public enum StructureOwnershipType {
    /**
     * CLAN_ONLY - Sadece klan alanına yapılabilen yapılar
     * - Sadece klan alanına yapılabilir
     * - Sadece klan üyeleri kullanabilir
     * - Sahiplik kontrolü: Klan üyeliği gerekli
     */
    CLAN_ONLY,
    
    /**
     * CLAN_OWNED - Klan dışına yapılabilen ama sadece yapan oyuncu ve klanının kullanabildiği yapılar
     * - Klan dışına yapılabilir
     * - Sadece yapan oyuncu ve klanı kullanabilir
     * - Sahiplik kontrolü: Yapan oyuncu veya klan üyeliği gerekli
     */
    CLAN_OWNED,
    
    /**
     * PUBLIC - Her yere yapılabilen ve herkesin kullanabildiği yapılar
     * - Her yere yapılabilir
     * - Herkes kullanabilir
     * - Sahiplik kontrolü: YOK (herkese açık)
     */
    PUBLIC
}

