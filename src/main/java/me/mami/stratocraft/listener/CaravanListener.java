package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.CaravanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

/**
 * Kervan hasar ve ölüm yönetimi
 * - Kervanlar PvP ve PvE'ye açıktır
 * - Kervan sahibi kendi kervanına zarar veremez (Friendly Fire koruması)
 */
public class CaravanListener implements Listener {
    private final CaravanManager caravanManager;
    
    public CaravanListener(CaravanManager caravanManager) {
        this.caravanManager = caravanManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCaravanDamage(EntityDamageByEntityEvent event) {
        // Sadece Mule'leri kontrol et (kervan hayvanı)
        if (!(event.getEntity() instanceof Mule)) return;
        
        Mule caravan = (Mule) event.getEntity();
        
        // Bu bir kervan mı?
        if (!caravanManager.isCaravan(caravan)) return;
        
        // Kervan sahibi kendi malına zarar veremez (Friendly Fire Koruması)
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            UUID ownerId = caravanManager.getOwner(caravan);
            
            if (ownerId != null && attacker.getUniqueId().equals(ownerId)) {
                event.setCancelled(true);
                attacker.sendMessage("§cKendi kervanına saldıramazsın!");
                return;
            }
        }
        
        // DİĞER HERKES (Haydutlar, Rakip Oyuncular) hasar verebilir.
        // Event iptal EDİLMEZ. Kervan ölebilir.
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCaravanDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mule)) return;
        
        Mule caravan = (Mule) event.getEntity();
        
        // Bu bir kervan mı?
        if (!caravanManager.isCaravan(caravan)) return;
        
        // Kervan öldü, görev başarısız mesajı yolla
        UUID ownerId = caravanManager.getOwner(caravan);
        if (ownerId != null) {
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage("§c§l════════════════════════════");
                owner.sendMessage("§c§lKERVANINIZ YOK EDİLDİ!");
                owner.sendMessage("§7Mallarınız yağmalandı ve kayboldu.");
                owner.sendMessage("§c§l════════════════════════════");
            }
        }
        
        // Kervanı sistemden temizle
        caravanManager.removeCaravan(caravan);
        
        // Eşyalar vanilla mantığıyla yere düşer (Drop), ekstra koda gerek yok.
    }
}

