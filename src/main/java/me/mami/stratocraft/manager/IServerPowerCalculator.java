package me.mami.stratocraft.manager;

/**
 * Sunucu Güç Hesaplama Interface
 * 
 * Gelecekte farklı sunucu güç hesaplama sistemleri eklenebilir
 * Strategy Pattern kullanılarak kolayca değiştirilebilir
 */
public interface IServerPowerCalculator {
    /**
     * Sunucu toplam güç puanını hesapla
     * 
     * @return Sunucu güç puanı
     */
    double calculateServerPower();
    
    /**
     * Cache'i temizle (oyuncu giriş/çıkışında çağrılabilir)
     */
    void clearCache();
}

