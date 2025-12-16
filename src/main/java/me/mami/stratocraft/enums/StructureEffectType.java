package me.mami.stratocraft.enums;

/**
 * Yapı Efekt Tipleri Enum
 * 
 * Yapıların verdiği efekt tiplerini tanımlar.
 */
public enum StructureEffectType {
    BUFF,           // Buff (pozitif efekt)
    DEBUFF,         // Debuff (negatif efekt - düşmanlara)
    UTILITY,        // Utility (menü, teleport, vb.)
    PASSIVE,        // Pasif (güç, kaynak üretimi, vb.)
    NONE            // Efekt yok
}

