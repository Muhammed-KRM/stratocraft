package me.mami.stratocraft.enums;

/**
 * Kontrat Tipleri Enum
 * 
 * Tüm kontrat tiplerini içerir.
 * Merkezi enum yönetimi için oluşturulmuştur.
 * 
 * Kontratlar iki tarafta bağımsız şekilde şartlar ve süreler verebilir.
 * Her kontrat tipinin olumlu ve olumsuz versiyonları vardır.
 */
public enum ContractType {
    RESOURCE_COLLECTION,   // Kaynak toplama kontratları (şu kadar kaynak ver/verme)
    CONSTRUCTION,          // İnşaat kontratları (şu yapıyı yapma/yap)
    COMBAT,               // Saldırı kontratları (şu oyuncuyu öldürme/öldür, şu oyuncuya vurma/vur)
    TERRITORY             // Bölge kontratları (şu verilen 4 köşenin kordinatları arasındaki bölgeye gitme/git)
}

