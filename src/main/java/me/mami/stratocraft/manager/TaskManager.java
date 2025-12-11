package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task Yönetim Sistemi
 * 
 * ⚠️ MEMORY LEAK ÖNLEME: Oyuncu çıkışında tüm task'ları otomatik iptal eder
 * 
 * Özellikler:
 * - Oyuncu bazlı task takibi
 * - Otomatik temizlik (PlayerQuitEvent)
 * - Thread-safe (ConcurrentHashMap)
 * - Periyodik audit (kontrol)
 */
public class TaskManager implements Listener {
    
    private final Main plugin;
    
    // ✅ THREAD SAFETY: ConcurrentHashMap kullan (multi-threaded environment)
    private final Map<UUID, Set<BukkitTask>> playerTasks = new ConcurrentHashMap<>();
    
    // Periyodik audit task'ı
    private BukkitTask auditTask;
    
    // Audit interval (varsayılan: 5 dakika)
    private static final long AUDIT_INTERVAL = 6000L; // 5 dakika = 6000 tick
    
    public TaskManager(Main plugin) {
        this.plugin = plugin;
        
        // Listener'ı kaydet
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Periyodik audit başlat
        startAuditTask();
    }
    
    /**
     * Oyuncu için task kaydet
     * 
     * @param playerId Oyuncu UUID
     * @param task Kaydedilecek task
     */
    public void registerPlayerTask(UUID playerId, BukkitTask task) {
        if (playerId == null || task == null) return;
        
        playerTasks.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                   .add(task);
    }
    
    /**
     * Oyuncu için task kaydet (Player objesi ile)
     */
    public void registerPlayerTask(Player player, BukkitTask task) {
        if (player == null) return;
        registerPlayerTask(player.getUniqueId(), task);
    }
    
    /**
     * Oyuncu için tüm task'ları iptal et
     * 
     * @param playerId Oyuncu UUID
     */
    public void cancelPlayerTasks(UUID playerId) {
        if (playerId == null) return;
        
        Set<BukkitTask> tasks = playerTasks.remove(playerId);
        if (tasks != null) {
            for (BukkitTask task : tasks) {
                if (task != null && !task.isCancelled()) {
                    task.cancel();
                }
            }
        }
    }
    
    /**
     * Oyuncu için tüm task'ları iptal et (Player objesi ile)
     */
    public void cancelPlayerTasks(Player player) {
        if (player == null) return;
        cancelPlayerTasks(player.getUniqueId());
    }
    
    /**
     * Belirli bir task'ı kaldır (iptal etmeden)
     * 
     * @param playerId Oyuncu UUID
     * @param task Kaldırılacak task
     */
    public void unregisterPlayerTask(UUID playerId, BukkitTask task) {
        if (playerId == null || task == null) return;
        
        Set<BukkitTask> tasks = playerTasks.get(playerId);
        if (tasks != null) {
            tasks.remove(task);
            
            // Eğer başka task kalmadıysa map'i temizle
            if (tasks.isEmpty()) {
                playerTasks.remove(playerId);
            }
        }
    }
    
    /**
     * Oyuncu çıkışında tüm task'ları iptal et
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        cancelPlayerTasks(playerId);
    }
    
    /**
     * Periyodik audit task'ı başlat
     * (İptal edilmiş veya geçersiz task'ları temizle)
     */
    private void startAuditTask() {
        auditTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            int cleanedCount = 0;
            
            // Tüm oyuncu task'larını kontrol et
            for (Map.Entry<UUID, Set<BukkitTask>> entry : playerTasks.entrySet()) {
                UUID playerId = entry.getKey();
                Set<BukkitTask> tasks = entry.getValue();
                
                // Oyuncu online mı kontrol et
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    // Oyuncu offline, tüm task'ları iptal et
                    for (BukkitTask task : tasks) {
                        if (task != null && !task.isCancelled()) {
                            task.cancel();
                            cleanedCount++;
                        }
                    }
                    // Map'ten kaldır
                    playerTasks.remove(playerId);
                    continue;
                }
                
                // İptal edilmiş task'ları temizle
                tasks.removeIf(task -> {
                    if (task == null || task.isCancelled()) {
                        return true;
                    }
                    return false;
                });
                
                // Eğer başka task kalmadıysa map'i temizle
                if (tasks.isEmpty()) {
                    playerTasks.remove(playerId);
                }
            }
            
            // Log (sadece temizlik yapıldıysa)
            if (cleanedCount > 0) {
                plugin.getLogger().info("TaskManager: " + cleanedCount + " task temizlendi.");
            }
        }, AUDIT_INTERVAL, AUDIT_INTERVAL);
    }
    
    /**
     * Tüm task'ları iptal et (sunucu kapanırken)
     */
    public void shutdown() {
        // Audit task'ı durdur
        if (auditTask != null && !auditTask.isCancelled()) {
            auditTask.cancel();
        }
        
        // Tüm oyuncu task'larını iptal et
        for (Set<BukkitTask> tasks : playerTasks.values()) {
            for (BukkitTask task : tasks) {
                if (task != null && !task.isCancelled()) {
                    task.cancel();
                }
            }
        }
        
        playerTasks.clear();
    }
    
    /**
     * Aktif task sayısını al (debug için)
     */
    public int getActiveTaskCount() {
        return playerTasks.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
    
    /**
     * Oyuncu için aktif task sayısını al
     */
    public int getPlayerTaskCount(UUID playerId) {
        Set<BukkitTask> tasks = playerTasks.get(playerId);
        return tasks != null ? tasks.size() : 0;
    }
}

