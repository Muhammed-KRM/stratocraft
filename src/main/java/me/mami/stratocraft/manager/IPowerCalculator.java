package me.mami.stratocraft.manager;

import org.bukkit.entity.Player;

/**
 * Güç Hesaplama Interface
 * 
 * Gelecekte farklı güç hesaplama sistemleri eklenebilir
 * Strategy Pattern kullanılarak kolayca değiştirilebilir
 */
public interface IPowerCalculator {
    /**
     * Oyuncunun toplam güç puanını hesapla
     * 
     * @param player Oyuncu
     * @return Toplam güç puanı
     */
    double calculatePlayerPower(Player player);
}

