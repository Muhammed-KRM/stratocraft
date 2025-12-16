package me.mami.stratocraft.task;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.StructureEffectManager;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * StructureEffectTask - Yapı efektlerini periyodik olarak uygular
 * 
 * Her 1 saniyede (20 tick) çalışır ve tüm yapıların etkilerini uygular:
 * - Simya Kulesi: Batarya güçlendirme
 * - Zehir Reaktörü: Düşmanlara zehir
 * - Gözetleme Kulesi: Düşman tespiti
 * - Şifa Kulesi: Üyelere şifa
 * - vb.
 * 
 * YENİ: StructureEffectManager kullanıyor
 */
public class StructureEffectTask extends BukkitRunnable {

    private final StructureEffectManager effectManager;

    public StructureEffectTask(StructureEffectManager effectManager) {
        this.effectManager = effectManager;
    }
    
    /**
     * GERİYE UYUMLULUK: Eski constructor
     * @deprecated StructureEffectManager kullanın
     */
    @Deprecated
    public StructureEffectTask(ClanManager cm) {
        // Eski sistem için null (StructureEffectManager olmadan çalışmaz)
        this.effectManager = null;
    }

    @Override
    public void run() {
        if (effectManager == null) {
            // Eski sistem devre dışı
            return;
        }
        
        // StructureEffectManager'a güncelleme yaptır
        effectManager.updateEffects();
    }
}
