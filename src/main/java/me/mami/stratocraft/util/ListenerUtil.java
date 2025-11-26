package me.mami.stratocraft.util;

import org.bukkit.entity.Player;

/**
 * ListenerUtil - Listener'lar için ortak yardımcı metodlar
 */
public class ListenerUtil {
    
    /**
     * Admin bypass kontrolü - Tüm listener'larda kullanılabilir
     */
    public static boolean hasAdminBypass(Player player) {
        return player.hasPermission("stratocraft.bypass");
    }
    
    /**
     * Event'in iptal edilip edilmeyeceğini kontrol et (admin bypass ile)
     */
    public static boolean shouldCancelEvent(Player player) {
        return !hasAdminBypass(player);
    }
}

