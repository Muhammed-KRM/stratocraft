package me.mami.stratocraft.enums;

/**
 * Eşya Kategorileri Enum
 * 
 * Tüm özel eşyaların (silahlar, zırhlar, yardımcı eşyalar, vb.) 
 * fonksiyonel kategorilerini içerir.
 * 
 * Bu enum, eşyaların ne işe yaradığını belirtir:
 * - ATTACK: Saldırı eşyaları (silahlar, savaş eşyaları)
 * - DEFENSE: Savunma eşyaları (zırhlar, kalkanlar)
 * - SUPPORT: Destek eşyaları (şifa, hız, efekt veren)
 * - CONSTRUCTION: Oluşturma eşyaları (blok oluşturma, yapı)
 * - UTILITY: Yardımcı eşyalar (COMPASS, CLOCK, RECIPE, PERSONAL_TERMINAL, vb.)
 */
public enum ItemCategory {
    ATTACK,         // Saldırı eşyaları (WEAPON, WAR_FAN, vb.)
    DEFENSE,        // Savunma eşyaları (ARMOR, TOWER_SHIELD, vb.)
    SUPPORT,        // Destek eşyaları (şifa, hız, efekt veren)
    CONSTRUCTION,   // Oluşturma eşyaları (blok oluşturma, yapı)
    UTILITY         // Yardımcı eşyalar (PERSONAL_TERMINAL, COMPASS, CLOCK, RECIPE, vb.)
}

