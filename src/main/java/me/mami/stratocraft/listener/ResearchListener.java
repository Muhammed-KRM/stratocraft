package me.mami.stratocraft.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class ResearchListener implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        // Araştırma kontrolü - eğer tarif kitabı yoksa özel eşyalar yapılamaz
        // Bu mantık ItemManager ve ResearchManager ile entegre edilebilir
        // Şimdilik boş bırakıyoruz, gerekirse genişletilebilir
    }
}

