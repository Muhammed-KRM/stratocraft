package me.mami.stratocraft.enums;

/**
 * Felaket Durum Enum'u
 * Felaketlerin davranış durumlarını belirler
 */
public enum DisasterState {
    /**
     * Merkeze gitme durumu
     * Felaket merkeze ulaşmaya çalışıyor
     */
    GO_CENTER,
    
    /**
     * Klan saldırısı durumu
     * Felaket klan kristallerini yok etmeye çalışıyor
     */
    ATTACK_CLAN,
    
    /**
     * Oyuncu saldırısı durumu
     * Felaket oyuncuları kovalıyor
     */
    ATTACK_PLAYER
}

