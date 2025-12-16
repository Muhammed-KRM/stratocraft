package me.mami.stratocraft.enums;

/**
 * Felaket Tipleri Enum
 * 
 * Tüm felaket tiplerini içerir.
 * Merkezi enum yönetimi için oluşturulmuştur.
 */
public enum DisasterType {
    // Canlı Felaketler - Tek Boss (Felaket Bossları - Normal bosslardan ayrı)
    CATASTROPHIC_TITAN,        // Seviye 3 - Felaket Titanı (30 blok boyunda dev golem)
    CATASTROPHIC_ABYSSAL_WORM, // Seviye 2 - Felaket Hiçlik Solucanı
    CATASTROPHIC_CHAOS_DRAGON, // Seviye 3 - Felaket Khaos Ejderi
    CATASTROPHIC_VOID_TITAN,   // Seviye 3 - Felaket Boşluk Titanı
    CATASTROPHIC_ICE_LEVIATHAN,// Seviye 2 - Felaket Buzul Leviathan
    
    // Canlı Felaketler - Grup (30 adet)
    ZOMBIE_HORDE,       // Seviye 2 - 30 Orta Güçte Zombi
    SKELETON_LEGION,    // Seviye 2 - 30 Orta Güçte İskelet
    SPIDER_SWARM,       // Seviye 2 - 30 Orta Güçte Örümcek
    
    // Canlı Felaketler - Mini Dalga (100-500 adet)
    CREEPER_SWARM,      // Seviye 1 - 100-500 Mini Creeper
    ZOMBIE_WAVE,        // Seviye 1 - 100-500 Mini Zombi
    
    // Doğa Olayları
    SOLAR_FLARE,        // Seviye 1 - Güneş Patlaması
    EARTHQUAKE,         // Seviye 2 - Deprem
    STORM,              // Seviye 2 - Fırtına
    METEOR_SHOWER,      // Seviye 2 - Meteor Yağmuru
    VOLCANIC_ERUPTION,  // Seviye 3 - Volkanik Patlama
    
    // Mini Felaketler
    BOSS_BUFF_WAVE,     // Mini - Boss güçlenme dalgası
    MOB_INVASION,       // Mini - Mob istilası
    PLAYER_BUFF_WAVE    // Mini - Oyuncu buff dalgası
}

