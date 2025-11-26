package me.mami.stratocraft.model;

import org.bukkit.Location;

public class Structure {
    public enum Type {
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
        WARNING_SIGN          // Yasaklı Bölge Tabelası
    }

    private final Type type;
    private final Location location;
    private int level;
    private int shieldFuel = 0;
    
    // MAX_FUEL artık ConfigManager'dan gelecek, burada sadece varsayılan değer
    public static int getDefaultMaxFuel() {
        return 12 * 60; // 12 Saat (saniye cinsinden)
    }

    public Structure(Type type, Location location, int level) {
        this.type = type;
        this.location = location;
        this.level = level;
    }

    public Type getType() { return type; }
    public Location getLocation() { return location; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public void addFuel(int amount, int maxFuel) { 
        this.shieldFuel = Math.min(this.shieldFuel + amount, maxFuel); 
    }
    
    // Geriye uyumluluk için eski metod (varsayılan değer kullanır)
    public void addFuel(int amount) { 
        this.shieldFuel = Math.min(this.shieldFuel + amount, getDefaultMaxFuel()); 
    }
    public void consumeFuel() { if (this.shieldFuel > 0) this.shieldFuel--; }
    public boolean isShieldActive() { return this.shieldFuel > 0; }
    public int getShieldFuel() { return shieldFuel; }
}

