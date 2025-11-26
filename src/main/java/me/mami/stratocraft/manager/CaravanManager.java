package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaravanManager {
    private final Map<UUID, Entity> activeCaravans = new HashMap<>();
    private final Map<UUID, Location> caravanTargets = new HashMap<>(); // Kervan hedefleri
    private final Map<UUID, Clan> caravanClans = new HashMap<>(); // Kervan sahibi klanlar

    public void createCaravan(Player owner, Location start, Location end) {
        Llama llama = start.getWorld().spawn(start, Llama.class);
        llama.setTamed(true);
        llama.setOwner(owner);
        activeCaravans.put(owner.getUniqueId(), llama);
        caravanTargets.put(owner.getUniqueId(), end);
        
        Clan ownerClan = Main.getInstance().getClanManager().getClanByPlayer(owner.getUniqueId());
        if (ownerClan != null) {
            caravanClans.put(owner.getUniqueId(), ownerClan);
        }
        
        // Kervan hedefe ulaşma kontrolü
        checkCaravanArrival(owner.getUniqueId());
    }

    public Entity getCaravan(UUID playerId) {
        return activeCaravans.get(playerId);
    }
    
    private void checkCaravanArrival(UUID playerId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Entity caravan = activeCaravans.get(playerId);
                Location target = caravanTargets.get(playerId);
                
                if (caravan == null || !caravan.isValid() || target == null) {
                    cancel();
                    return;
                }
                
                // Kervan hedefe 5 blok yaklaştı mı?
                if (caravan.getLocation().distance(target) <= 5) {
                    // Ödül: Mallar x1.5 değer kazanır
                    Clan clan = caravanClans.get(playerId);
                    if (clan != null && caravan instanceof Llama) {
                        Llama llama = (Llama) caravan;
                        double totalValue = 0;
                        
                        // Llama'nın envanterindeki eşyaları değerlendir
                        for (ItemStack item : llama.getInventory().getContents()) {
                            if (item != null) {
                                // Basit değer hesaplama (örnek)
                                totalValue += item.getAmount() * 10; // Her eşya 10 altın değerinde
                            }
                        }
                        
                        // x1.5 bonus
                        double reward = totalValue * 1.5;
                        clan.deposit(reward);
                        
                        // Kervanı temizle
                        llama.getInventory().clear();
                        activeCaravans.remove(playerId);
                        caravanTargets.remove(playerId);
                        caravanClans.remove(playerId);
                        
                        Player owner = Main.getInstance().getServer().getPlayer(playerId);
                        if (owner != null) {
                            owner.sendMessage("§a§lKervan hedefe ulaştı! " + reward + " altın kazandınız (x1.5 bonus)!");
                        }
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L); // Her saniye kontrol et
    }
}

