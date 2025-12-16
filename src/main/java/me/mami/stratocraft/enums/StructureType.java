package me.mami.stratocraft.enums;

/**
 * Yapı Tipleri Enum
 * 
 * Tüm yapı tiplerini içerir.
 * Merkezi enum yönetimi için oluşturulmuştur.
 */
public enum StructureType {
    // --- TEMEL ---
    CORE,                 // Ana Kristal
    
    // --- SAVUNMA & SALDIRI ---
    ALCHEMY_TOWER,        // Simya Kulesi (Batarya Buff)
    POISON_REACTOR,       // Zehir Reaktörü
    TECTONIC_STABILIZER,  // Tektonik Sabitleyici (Felaket Kalkanı)
    SIEGE_FACTORY,        // Kuşatma Fabrikası
    WALL_GENERATOR,       // Sur Jeneratörü
    GRAVITY_WELL,         // Yerçekimi Kuyusu
    LAVA_TRENCHER,        // Lav Hendekçisi
    WATCHTOWER,           // Gözetleme Kulesi
    DRONE_STATION,        // Drone İstasyonu
    AUTO_TURRET,          // Otomatik Taret (Hurda teknolojisi ile yapılır)
    CATAPULT,             // Mancınık (SiegeWeaponManager ile entegre)
    
    // --- EKONOMİ & LOJİSTİK ---
    GLOBAL_MARKET_GATE,   // Global Pazar Kapısı
    AUTO_DRILL,           // Otomatik Madenci
    XP_BANK,              // Tecrübe Bankası
    MAG_RAIL,             // Manyetik Ray
    TELEPORTER,           // Işınlanma Platformu
    FOOD_SILO,            // Buzdolabı
    OIL_REFINERY,         // Petrol Rafinerisi
    
    // --- DESTEK & UTIL ---
    HEALING_BEACON,       // Şifa Kulesi
    WEATHER_MACHINE,      // Hava Kontrolcüsü
    CROP_ACCELERATOR,     // Tarım Hızlandırıcı
    MOB_GRINDER,          // Mob Öğütücü
    INVISIBILITY_CLOAK,   // Görünmezlik Perdesi
    ARMORY,               // Cephanelik
    LIBRARY,              // Kütüphane
    WARNING_SIGN,         // Yasaklı Bölge Tabelası
    
    // --- YÖNETİM & MENÜ YAPILARI ---
    PERSONAL_MISSION_GUILD,  // Kişisel Görev Loncası (her yere yapılabilir)
    CLAN_MANAGEMENT_CENTER, // Klan Yönetim Merkezi (Klan menüleri)
    CLAN_BANK,              // Klan Bankası
    CLAN_MISSION_GUILD,     // Klan Görev Loncası (sadece klan içine)
    TRAINING_ARENA,         // Eğitim Alanı (Eğitilmiş Canlılar, Üreme)
    CARAVAN_STATION,        // Kervan İstasyonu
    CONTRACT_OFFICE,        // Kontrat Bürosu (genel)
    MARKET_PLACE,           // Market
    RECIPE_LIBRARY          // Tarif Kütüphanesi
}

