package me.mami.stratocraft.enums;

/**
 * Görev Tipleri Enum
 * 
 * Tüm görev tiplerini içerir.
 * Merkezi enum yönetimi için oluşturulmuştur.
 * 
 * Not: Görevler MissionScope ile kişisel veya klan görevi olarak işaretlenir.
 * Bazı görev tipleri sadece kişisel (PERSONAL), bazıları sadece klan (CLAN),
 * bazıları ise her ikisi de olabilir.
 */
public enum MissionType {
    // Kişisel Görevler
    KILL_MOBS,          // Canlı öldürme görevleri (PERSONAL)
    COLLECT_ITEMS,      // Eşya toplama görevleri (PERSONAL)
    EXPLORE_AREA,       // Bölge keşif görevleri (PERSONAL)
    TRADE_ITEMS,        // Ticaret görevleri (PERSONAL)
    CRAFT_ITEMS,        // Üretim görevleri (PERSONAL)
    DEFEAT_BOSS,        // Boss yenme görevleri (PERSONAL)
    SURVIVE_DISASTER,   // Felaket hayatta kalma görevleri (PERSONAL)
    
    // Klan Görevleri
    BUILD_STRUCTURE,    // Yapı inşa görevleri (CLAN)
    DEFEND_CLAN,        // Klan savunma görevleri (CLAN)
    COMPLETE_RITUAL,    // Ritüel tamamlama görevleri (CLAN)
    CLAN_TERRITORY,     // Klan bölgesi genişletme görevleri (CLAN)
    CLAN_WAR,           // Klan savaşı görevleri (CLAN)
    CLAN_RESOURCE       // Klan kaynak toplama görevleri (CLAN)
}

