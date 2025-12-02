package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class SiegeManager {
    // Savunan Klan ID -> Saldıran Klan ID
    private final Map<Clan, Clan> activeSieges = new HashMap<>();
    private BuffManager buffManager;

    public void setBuffManager(BuffManager bm) {
        this.buffManager = bm;
    }

    public void startSiege(Clan attacker, Clan defender) {
        activeSieges.put(defender, attacker);
        Bukkit.broadcastMessage("§4§lSAVAŞ İLANI! §e" + attacker.getName() + " klani, " + defender.getName() + " klanına savaş açtı!");
        // SiegeTimer burada başlatılmalı
    }

    public void endSiege(Clan winner, Clan loser) {
        activeSieges.remove(loser);
        Bukkit.broadcastMessage("§2§lZAFER! §a" + winner.getName() + " klanı, " + loser.getName() + " klanını fethetti!");
        
        // Ödül mantığı
        double loot = loser.getBalance() * 0.5;
        winner.deposit(loot);
        loser.withdraw(loot);
        
        // FATİH BUFF'I UYGULA
        if (buffManager != null) {
            buffManager.applyConquerorBuff(winner);
        }
    }

    /**
     * Beyaz Bayrak - Pes Etme Sistemi
     * Klan savaşta pes eder, klan yok olmaz ama sandıkların yarısı gider
     */
    public void surrender(Clan surrenderingClan, ClanManager clanManager) {
        if (!isUnderSiege(surrenderingClan)) {
            return; // Savaşta değil
        }
        
        Clan attacker = activeSieges.get(surrenderingClan);
        if (attacker == null) return;
        
        // Savaşı bitir
        activeSieges.remove(surrenderingClan);
        
        // Mesaj
        Bukkit.broadcastMessage("§f§lBEYAZ BAYRAK! §e" + surrenderingClan.getName() + " klanı pes etti!");
        
        // Klan yok olmaz, sadece sandıkların yarısı gider
        // Sandık itemlerinin yarısını al
        takeHalfChestItems(surrenderingClan, attacker);
        
        // Kazanan klan ödülü (para)
        double loot = surrenderingClan.getBalance() * 0.5;
        attacker.deposit(loot);
        surrenderingClan.withdraw(loot);
        
        // Fatih buff'ı uygula
        if (buffManager != null) {
            buffManager.applyConquerorBuff(attacker);
        }
    }

    /**
     * Klan bölgesindeki tüm sandıkların itemlerinin yarısını al
     */
    private void takeHalfChestItems(Clan surrenderingClan, Clan attacker) {
        if (surrenderingClan.getTerritory() == null) return;
        
        Location center = surrenderingClan.getTerritory().getCenter();
        if (center == null) return;
        
        // Performans için: Sadece yüzey seviyesinde ve yakın alanlarda ara
        // Veya tüm chunk'ları tara (daha verimli)
        int searchRadius = Math.min(surrenderingClan.getTerritory().getRadius(), 100); // Max 100 blok yarıçap
        
        // Bölgedeki tüm sandıkları bul (sadece yüzey seviyesinde)
        for (int x = -searchRadius; x <= searchRadius; x += 2) { // Her 2 blokta bir kontrol (performans)
            for (int z = -searchRadius; z <= searchRadius; z += 2) {
                // Y seviyesi: 0-256 arası (yüzey seviyesi odaklı)
                for (int y = 0; y <= 256; y += 16) { // Her 16 blokta bir kontrol
                    Location loc = center.clone().add(x, y, z);
                    
                    // Sadece klan bölgesi içindeyse kontrol et
                    if (!me.mami.stratocraft.util.GeometryUtil.isInsideRadius(center, loc, searchRadius)) {
                        continue;
                    }
                    
                    Block block = loc.getBlock();
                    
                    if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                        if (block.getState() instanceof Chest) {
                            Chest chest = (Chest) block.getState();
                            org.bukkit.inventory.Inventory inv = chest.getInventory();
                            
                            // Her item'ın yarısını al
                            for (int i = 0; i < inv.getSize(); i++) {
                                ItemStack item = inv.getItem(i);
                                if (item != null && item.getType() != Material.AIR) {
                                    int halfAmount = item.getAmount() / 2;
                                    if (halfAmount > 0) {
                                        ItemStack halfItem = item.clone();
                                        halfItem.setAmount(halfAmount);
                                        
                                        // Yere düşür (kazanan klan toplayabilir)
                                        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 1, 0.5), halfItem);
                                        
                                        // Sandıktan çıkar
                                        item.setAmount(item.getAmount() - halfAmount);
                                        inv.setItem(i, item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isUnderSiege(Clan clan) {
        return activeSieges.containsKey(clan);
    }
    
    public Clan getAttacker(Clan defender) {
        return activeSieges.get(defender);
    }
}

