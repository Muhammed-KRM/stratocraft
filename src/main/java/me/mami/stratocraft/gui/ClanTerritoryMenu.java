package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryBoundaryManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.manager.config.TerritoryConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.territory.TerritoryData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klan Alanı Yönetim Menüsü
 * 
 * Özellikler:
 * - Klan alanı genişletme/küçültme
 * - Alan bilgisi
 * - Sınır görselleştirme
 * - Sınır yeniden hesaplama
 */
public class ClanTerritoryMenu implements Listener {
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    private final TerritoryBoundaryManager boundaryManager;
    private final TerritoryConfig config;
    private final Main plugin;
    
    // Pending işlemler (onay için)
    private final Map<UUID, PendingExpansion> pendingExpansions = new ConcurrentHashMap<>();
    private final Map<UUID, PendingShrinkage> pendingShrinkages = new ConcurrentHashMap<>();
    
    // Açık menüler
    private final Map<UUID, Inventory> openMenus = new ConcurrentHashMap<>();
    
    public ClanTerritoryMenu(Main plugin, ClanManager clanManager, TerritoryManager territoryManager,
                             TerritoryBoundaryManager boundaryManager, TerritoryConfig config) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
        this.boundaryManager = boundaryManager;
        this.config = config;
    }
    
    /**
     * Menüyü aç
     */
    public void openMenu(Player player) {
        if (player == null) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        // Yetki kontrolü (Lider veya General)
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
            player.sendMessage("§cBu menüye erişim yetkiniz yok! (Lider/General)");
            return;
        }
        
        // Klan bölgesinde mi kontrol
        Clan owner = territoryManager.getTerritoryOwner(player.getLocation());
        if (owner == null || !owner.equals(clan)) {
            player.sendMessage("§cBu menüyü sadece kendi klan bölgenizde açabilirsiniz!");
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Klan Alanı Yönetimi");
        
        // Genişlet butonu (Slot 10)
        menu.setItem(10, createButton(Material.GREEN_CONCRETE, "§a§lGenişlet",
            Arrays.asList("§7Klan alanını genişlet", "§7Çitlerle çevrelenmiş alan gerekli")));
        
        // Küçült butonu (Slot 12)
        menu.setItem(12, createButton(Material.RED_CONCRETE, "§c§lKüçült",
            Arrays.asList("§7Klan alanını küçült", "§7Çitlerle çevrelenmiş alan gerekli")));
        
        // Bilgi butonu (Slot 14)
        TerritoryData territoryData = boundaryManager != null ? 
            boundaryManager.getTerritoryData(clan) : null;
        List<String> infoLore = new ArrayList<>();
        if (territoryData != null) {
            infoLore.add("§7Radius: §e" + territoryData.getRadius() + " blok");
            infoLore.add("§7Alan: §e" + territoryData.calculateArea() + " blok²");
            infoLore.add("§7Çit Sayısı: §e" + territoryData.getFenceCount());
            infoLore.add("§7Y Yüksekliği: §e" + territoryData.getMinY() + " - " + territoryData.getMaxY());
            infoLore.add("§7Gökyüzüne: §e+" + territoryData.getSkyHeight() + " blok");
            infoLore.add("§7Yer Altına: §e-" + territoryData.getGroundDepth() + " blok");
        } else {
            infoLore.add("§7Bilgi yükleniyor...");
        }
        menu.setItem(14, createButton(Material.BOOK, "§e§lBilgi", infoLore));
        
        // Sınırlar butonu (Slot 16)
        menu.setItem(16, createButton(Material.ENDER_EYE, "§b§lSınırlar",
            Arrays.asList("§7Sınır koordinatlarını partikül ile göster", "§7Süre: 10 saniye")));
        
        // Yeniden Hesapla butonu (Slot 22)
        menu.setItem(22, createButton(Material.COMPASS, "§6§lYeniden Hesapla",
            Arrays.asList("§7Sınır koordinatlarını yeniden hesapla", "§7Çit lokasyonlarını kontrol et")));
        
        // Çıkış butonu (Slot 26)
        menu.setItem(26, createButton(Material.BARRIER, "§c§lÇıkış",
            Arrays.asList("§7Menüyü kapat")));
        
        player.openInventory(menu);
        openMenus.put(player.getUniqueId(), menu);
    }
    
    /**
     * Buton oluştur
     */
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        Inventory inventory = event.getInventory();
        String title = inventory.getTitle();
        
        if (!title.equals("§6Klan Alanı Yönetimi")) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return;
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 10: // Genişlet
                handleExpand(player, clan);
                break;
            case 12: // Küçült
                handleShrink(player, clan);
                break;
            case 14: // Bilgi
                showInfo(player, clan);
                break;
            case 16: // Sınırlar
                showBoundaries(player, clan);
                break;
            case 22: // Yeniden Hesapla
                recalculateBoundaries(player, clan);
                break;
            case 26: // Çıkış
                player.closeInventory();
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            openMenus.remove(event.getPlayer().getUniqueId());
        }
    }
    
    /**
     * Genişletme işlemi
     */
    private void handleExpand(Player player, Clan clan) {
        player.sendMessage("§7Çit kontrolü yapılıyor...");
        
        // Async çit kontrolü
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Çit kontrolü yapılacak (isSurroundedByClanFences benzeri)
            // Şimdilik basit kontrol
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§cGenişletme özelliği henüz tamamlanmadı!");
                player.sendMessage("§7Çitlerle yeni alan çevreleyip menüden tekrar deneyin.");
            });
        });
    }
    
    /**
     * Küçültme işlemi
     */
    private void handleShrink(Player player, Clan clan) {
        player.sendMessage("§7Çit kontrolü yapılıyor...");
        
        // Async çit kontrolü
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Çit kontrolü yapılacak
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§cKüçültme özelliği henüz tamamlanmadı!");
                player.sendMessage("§7Çitlerle yeni alan çevreleyip menüden tekrar deneyin.");
            });
        });
    }
    
    /**
     * Bilgi göster
     */
    private void showInfo(Player player, Clan clan) {
        TerritoryData territoryData = boundaryManager != null ? 
            boundaryManager.getTerritoryData(clan) : null;
        
        if (territoryData == null) {
            player.sendMessage("§cAlan bilgisi bulunamadı!");
            return;
        }
        
        player.sendMessage("§6§l=== KLAN ALANI BİLGİLERİ ===");
        player.sendMessage("§7Radius: §e" + territoryData.getRadius() + " blok");
        player.sendMessage("§7Alan: §e" + territoryData.calculateArea() + " blok²");
        player.sendMessage("§7Çit Sayısı: §e" + territoryData.getFenceCount());
        player.sendMessage("§7Y Yüksekliği: §e" + territoryData.getMinY() + " - " + territoryData.getMaxY());
        player.sendMessage("§7Gökyüzüne: §e+" + territoryData.getSkyHeight() + " blok");
        player.sendMessage("§7Yer Altına: §e-" + territoryData.getGroundDepth() + " blok");
        player.sendMessage("§7Sınır Koordinat Sayısı: §e" + territoryData.getBoundaryCoordinates().size());
    }
    
    /**
     * Sınır partikülleri göster
     */
    private void showBoundaries(Player player, Clan clan) {
        TerritoryData territoryData = boundaryManager != null ? 
            boundaryManager.getTerritoryData(clan) : null;
        
        if (territoryData == null) {
            player.sendMessage("§cAlan bilgisi bulunamadı!");
            return;
        }
        
        List<Location> boundaryLine = territoryData.getBoundaryLine();
        if (boundaryLine.isEmpty()) {
            player.sendMessage("§cSınır koordinatları hesaplanmamış!");
            player.sendMessage("§7Önce 'Yeniden Hesapla' butonuna basın.");
            return;
        }
        
        player.sendMessage("§aSınır partikülleri gösteriliyor... (10 saniye)");
        
        // 10 saniye boyunca partikül göster
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!player.isOnline()) return;
            
            Particle particleType = config.getBoundaryParticleType();
            org.bukkit.Color particleColor = config.getBoundaryParticleColor();
            double spacing = config.getBoundaryParticleSpacing();
            
            int count = 0;
            for (Location boundaryLoc : boundaryLine) {
                if (count % (int) spacing != 0) {
                    count++;
                    continue;
                }
                
                double y = player.getLocation().getY() + (Math.random() * 4 - 2);
                Location particleLoc = boundaryLoc.clone();
                particleLoc.setY(y);
                
                if (particleType == Particle.REDSTONE) {
                    player.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(particleColor, 1.0f));
                } else {
                    player.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0);
                }
                
                count++;
            }
        }, 0L, 10L); // Her 0.5 saniyede bir
        
        // 10 saniye sonra durdur
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskId);
            player.sendMessage("§7Sınır partikülleri durduruldu.");
        }, 200L); // 10 saniye = 200 tick
    }
    
    /**
     * Sınırları yeniden hesapla
     */
    private void recalculateBoundaries(Player player, Clan clan) {
        TerritoryData territoryData = boundaryManager != null ? 
            boundaryManager.getTerritoryData(clan) : null;
        
        if (territoryData == null) {
            player.sendMessage("§cAlan bilgisi bulunamadı!");
            return;
        }
        
        player.sendMessage("§7Sınır koordinatları yeniden hesaplanıyor...");
        
        if (config.isAsyncBoundaryCalculation()) {
            boundaryManager.calculateBoundariesAsync(clan, territoryData, (data) -> {
                player.sendMessage("§a§l✓ Sınır koordinatları yeniden hesaplandı!");
                player.sendMessage("§7Çit Sayısı: §e" + data.getFenceCount());
                player.sendMessage("§7Sınır Koordinat Sayısı: §e" + data.getBoundaryCoordinates().size());
            });
        } else {
            boundaryManager.calculateBoundaries(clan, territoryData);
            player.sendMessage("§a§l✓ Sınır koordinatları yeniden hesaplandı!");
            player.sendMessage("§7Çit Sayısı: §e" + territoryData.getFenceCount());
            player.sendMessage("§7Sınır Koordinat Sayısı: §e" + territoryData.getBoundaryCoordinates().size());
        }
    }
    
    /**
     * Pending genişletme verisi
     */
    private static class PendingExpansion {
        final int oldArea;
        final int newArea;
        final int expandAmount;
        
        PendingExpansion(int oldArea, int newArea, int expandAmount) {
            this.oldArea = oldArea;
            this.newArea = newArea;
            this.expandAmount = expandAmount;
        }
    }
    
    /**
     * Pending küçültme verisi
     */
    private static class PendingShrinkage {
        final int oldArea;
        final int newArea;
        final int shrinkAmount;
        
        PendingShrinkage(int oldArea, int newArea, int shrinkAmount) {
            this.oldArea = oldArea;
            this.newArea = newArea;
            this.shrinkAmount = shrinkAmount;
        }
    }
}

