package me.mami.stratocraft.enums;

/**
 * Ceza Tipleri Enum
 * 
 * Kontrat ihlalinde uygulanacak ceza tiplerini içerir.
 * Merkezi enum yönetimi için oluşturulmuştur.
 */
public enum PenaltyType {
    HEALTH_PENALTY,    // Can cezası (şu kadar kalıcı canı gidecek)
    BANK_PENALTY,      // Banka cezası (bankadan şu kadar şu kaynak bana gelecek, yoksa borç olacak, bankaya koyduğu anda geçecek)
    MORTGAGE           // Hipotek (şu item silinecek/bana geçecek)
}

