package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

/**
 * ESKİ BossArenaManager (DEVRE DIŞI SÜRÜM)
 *
 * Bu sınıf artık DÜNYADA HİÇBİR BLOĞU DEĞİŞTİRMEZ.
 * Sadece eski kodun referanslarını korumak için basit ve güvenli bir iskelet olarak duruyor.
 *
 * Gerçek arena ve çevre dönüşümü artık {@link NewBossArenaManager} tarafından yönetiliyor.
 */
public class BossArenaManager {

    private final Main plugin;

    // Güçlü boss listesi (yalnızca bilgi amaçlı, eski API için)
    private final Set<BossManager.BossType> powerfulBosses = new HashSet<>();

    public BossArenaManager(Main plugin) {
        this.plugin = plugin;
        initializePowerfulBosses();
        if (this.plugin != null) {
            this.plugin.getLogger().info("[BossArenaManager] Eski arena sistemi DEVRE DIŞI. Hiçbir blok değiştirilmeyecek.");
        }
    }

    /**
     * Güçlü boss'ları belirle (eski API'yi bozmamak için tutuluyor)
     */
    private void initializePowerfulBosses() {
        // Seviye 2
        powerfulBosses.add(BossManager.BossType.TROLL_KING);

        // Seviye 3
        powerfulBosses.add(BossManager.BossType.DRAGON);
        powerfulBosses.add(BossManager.BossType.CYCLOPS);

        // Seviye 4
        powerfulBosses.add(BossManager.BossType.TITAN_GOLEM);
        powerfulBosses.add(BossManager.BossType.HELL_DRAGON);
        powerfulBosses.add(BossManager.BossType.HYDRA);
        powerfulBosses.add(BossManager.BossType.PHOENIX);

        // Seviye 5
        powerfulBosses.add(BossManager.BossType.VOID_DRAGON);
        powerfulBosses.add(BossManager.BossType.CHAOS_TITAN);
        powerfulBosses.add(BossManager.BossType.CHAOS_GOD);
    }

    /**
     * Boss güçlü mü kontrol et (eski API uyumluluğu)
     */
    public boolean isPowerfulBoss(BossManager.BossType type) {
        return powerfulBosses.contains(type);
    }

    /**
     * Boss'un seviyesini belirle (eski API uyumluluğu)
     */
    public int getBossLevel(BossManager.BossType type) {
        if (type == BossManager.BossType.TROLL_KING) return 2;
        if (type == BossManager.BossType.DRAGON || type == BossManager.BossType.CYCLOPS) return 3;
        if (type == BossManager.BossType.TITAN_GOLEM
                || type == BossManager.BossType.HELL_DRAGON
                || type == BossManager.BossType.HYDRA
                || type == BossManager.BossType.PHOENIX) return 4;
        if (type == BossManager.BossType.VOID_DRAGON
                || type == BossManager.BossType.CHAOS_TITAN
                || type == BossManager.BossType.CHAOS_GOD) return 5;
        return 1;
    }

    /**
     * ESKİ: Doğal boss biyomu oluşturma.
     * YENİ: Hiçbir şey yapmaz, sadece debug log yazar.
     */
    public void createNaturalBossBiome(Location center, BossManager.BossType bossType) {
        if (plugin != null) {
            plugin.getLogger().fine("[BossArenaManager] createNaturalBossBiome DEVRE DIŞI. BossType="
                    + bossType + " Loc=" + formatLoc(center));
        }
        // NO-OP: Blok değişimi yok.
    }

    /**
     * ESKİ: Ritüel ile alan dönüşümü.
     * YENİ: Hiçbir şey yapmaz, sadece debug log yazar.
     */
    public void startRitualAreaTransformation(Location center, BossManager.BossType bossType) {
        if (plugin != null) {
            plugin.getLogger().fine("[BossArenaManager] startRitualAreaTransformation DEVRE DIŞI. BossType="
                    + bossType + " Loc=" + formatLoc(center));
        }
        // NO-OP
    }

    /**
     * ESKİ: Arena temizleme.
     * YENİ: Sadece log, hiçbir blok geri alınmaz (zaten değiştirilmedi).
     */
    public void cleanupArena(Location center) {
        if (plugin != null) {
            plugin.getLogger().fine("[BossArenaManager] cleanupArena DEVRE DIŞI. Loc=" + formatLoc(center));
        }
        // NO-OP
    }

    // Küçük yardımcı: Lokasyonu okunabilir string'e çevir
    private String formatLoc(Location loc) {
        if (loc == null) return "null";
        return loc.getWorld().getName() + " "
                + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}


