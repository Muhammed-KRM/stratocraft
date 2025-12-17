package me.mami.stratocraft.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryBoundaryManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.manager.config.TerritoryConfig;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.territory.TerritoryData;

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
        String title = event.getView().getTitle();
        
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
     * Sınırları yeniden hesapla (YENİ: Çit kontrolü ile)
     */
    private void recalculateBoundaries(Player player, Clan clan) {
        TerritoryData territoryData = boundaryManager != null ? 
            boundaryManager.getTerritoryData(clan) : null;
        
        if (territoryData == null) {
            player.sendMessage("§cAlan bilgisi bulunamadı!");
            return;
        }
        
        Location crystalLoc = clan.getCrystalLocation();
        if (crystalLoc == null) {
            player.sendMessage("§cKlan kristali bulunamadı!");
            return;
        }
        
        player.sendMessage("§7Çit kontrolü yapılıyor...");
        
        // Async çit kontrolü ve sınır hesaplama
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // YENİ: Klan kristalini çevreleyen çitler tam şekilde kapanıyor mu kontrol et
            Block crystalBlock = crystalLoc.getBlock();
            boolean isSurrounded = isSurroundedByClanFences(crystalBlock, clan);
            
            if (!isSurrounded) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cKlan kristalini çevreleyen çitler tam şekilde kapanmamış!");
                    player.sendMessage("§7Boşluk var. Lütfen tüm çitleri kontrol edin.");
                });
                return;
            }
            
            // Çitler tam şekilde kapanmış, sınırları yeniden hesapla
            // Eski sınırları temizle
            territoryData.clearBoundaries();
            
            // Yeni çit lokasyonlarını topla (dünyadan)
            List<Location> newFenceLocations = collectFenceLocations(crystalLoc, clan);
            
            // TerritoryData'yı güncelle
            territoryData.clearFenceLocations();
            for (Location fenceLoc : newFenceLocations) {
                territoryData.addFenceLocation(fenceLoc);
            }
            
            // Sınırları hesapla
            territoryData.calculateBoundaries();
            
            // Main thread'e geri dön
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§a§l✓ Klan alanı güncellendi!");
                player.sendMessage("§7Çit Sayısı: §e" + territoryData.getFenceCount());
                player.sendMessage("§7Sınır Koordinat Sayısı: §e" + territoryData.getBoundaryCoordinates().size());
                
                // Cache'i güncelle
                if (territoryManager != null) {
                    territoryManager.setCacheDirty();
                }
            });
        });
    }
    
    /**
     * Klan kristalini çevreleyen çitler tam şekilde kapanıyor mu kontrol et
     */
    private boolean isSurroundedByClanFences(Block center, Clan clan) {
        // TerritoryListener'daki isSurroundedByClanFences metoduna benzer
        // Ama burada sadece klan çitlerini kontrol et
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(center);
        visited.add(center);
        
        int iterations = 0;
        int maxIterations = 5000;
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            if (iterations > maxIterations) {
                return false; // Çok büyük alan
            }
            
            Block[] neighbors = {
                current.getRelative(BlockFace.NORTH),
                current.getRelative(BlockFace.SOUTH),
                current.getRelative(BlockFace.EAST),
                current.getRelative(BlockFace.WEST)
            };
            
            for (Block neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;
                
                if (neighbor.getType() == Material.OAK_FENCE) {
                    // Klan çiti mi kontrol et
                    boolean isClanFence = false;
                    if (config != null) {
                        String metadataKey = config.getFenceMetadataKey();
                        isClanFence = neighbor.hasMetadata(metadataKey);
                    }
                    
                    // TerritoryData'dan kontrol et
                    if (!isClanFence && boundaryManager != null) {
                        TerritoryData data = boundaryManager.getTerritoryData(clan);
                        if (data != null) {
                            for (Location fenceLoc : data.getFenceLocations()) {
                                if (fenceLoc.getWorld().equals(neighbor.getWorld()) &&
                                    fenceLoc.getBlockX() == neighbor.getX() &&
                                    fenceLoc.getBlockY() == neighbor.getY() &&
                                    fenceLoc.getBlockZ() == neighbor.getZ()) {
                                    isClanFence = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (isClanFence) {
                        continue; // Sınır bulundu
                    }
                }
                
                if (neighbor.getType() != Material.AIR) {
                    continue; // Engel
                }
                
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
        
        // Eğer döngü limit aşılmadan bittiyse, kapalı bir alandır
        return true;
    }
    
    /**
     * Klan kristali etrafındaki çit lokasyonlarını topla
     */
    private List<Location> collectFenceLocations(Location crystalLoc, Clan clan) {
        List<Location> fenceLocations = new ArrayList<>();
        
        // Kristal etrafında 100 blok yarıçapta çitleri ara
        int searchRadius = 100;
        World world = crystalLoc.getWorld();
        if (world == null) return fenceLocations;
        
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                Block block = world.getBlockAt(
                    crystalLoc.getBlockX() + x,
                    crystalLoc.getBlockY(),
                    crystalLoc.getBlockZ() + z
                );
                
                if (block.getType() == Material.OAK_FENCE) {
                    // Klan çiti mi kontrol et
                    boolean isClanFence = false;
                    if (config != null) {
                        String metadataKey = config.getFenceMetadataKey();
                        isClanFence = block.hasMetadata(metadataKey);
                    }
                    
                    if (isClanFence) {
                        fenceLocations.add(block.getLocation());
                    }
                }
            }
        }
        
        return fenceLocations;
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

