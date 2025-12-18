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
        
        // ✅ YENİ: Tek "Alan Güncelle" butonu (Slot 10)
        menu.setItem(10, createButton(Material.EMERALD_BLOCK, "§a§lAlan Güncelle",
            Arrays.asList("§7Klan çitlerini kontrol et", "§7Yeni çevrelenen alanı hesapla", 
                          "§7Eski alan verilerini sil", "§7Yeni alan verilerini oluştur")));
        
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
            case 10: // ✅ YENİ: Alan Güncelle (tek tuş)
                recalculateBoundaries(player, clan);
                break;
            case 14: // Bilgi
                showInfo(player, clan);
                break;
            case 16: // Sınırlar
                showBoundaries(player, clan);
                break;
            case 22: // Yeniden Hesapla (eski - geriye uyumluluk için)
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
    
    // ✅ YENİ: handleExpand() ve handleShrink() kaldırıldı
    // Artık tek bir "Alan Güncelle" butonu var (recalculateBoundaries() kullanılıyor)
    
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
            
            // ✅ YENİ: Y ekseni sınırlarını al
            TerritoryData currentTerritoryData = boundaryManager != null ? 
                boundaryManager.getTerritoryData(clan) : null;
            int minY = Integer.MIN_VALUE;
            int maxY = Integer.MAX_VALUE;
            if (currentTerritoryData != null) {
                minY = currentTerritoryData.getMinY() - currentTerritoryData.getGroundDepth();
                maxY = currentTerritoryData.getMaxY() + currentTerritoryData.getSkyHeight();
            }
            int playerY = player.getLocation().getBlockY();
            int effectiveY = (minY != Integer.MIN_VALUE && maxY != Integer.MAX_VALUE) 
                ? Math.max(minY, Math.min(maxY, playerY)) 
                : playerY;
            
            int count = 0;
            for (Location boundaryLoc : boundaryLine) {
                if (count % (int) spacing != 0) {
                    count++;
                    continue;
                }
                
                // ✅ YENİ: Y koordinatını sınırlar içinde ayarla
                double y = Math.max(minY, Math.min(maxY, effectiveY + (Math.random() * 4 - 2)));
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
            
            // ✅ YENİ: Y ekseni sınırlarını güncelle
            territoryData.updateYBounds();
            
            // ✅ YENİ: Config'den skyHeight ve groundDepth set et
            if (config != null) {
                territoryData.setSkyHeight(config.getSkyHeight());
                territoryData.setGroundDepth(config.getGroundDepth());
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
            
            // ✅ YENİ: 3D flood-fill (6 yöne bak)
            Block[] neighbors = {
                current.getRelative(BlockFace.NORTH),
                current.getRelative(BlockFace.SOUTH),
                current.getRelative(BlockFace.EAST),
                current.getRelative(BlockFace.WEST),
                current.getRelative(BlockFace.UP),    // ✅ Y ekseni eklendi
                current.getRelative(BlockFace.DOWN)   // ✅ Y ekseni eklendi
            };
            
            for (Block neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;
                
                if (neighbor.getType() == Material.OAK_FENCE) {
                    // ✅ YENİ: CustomBlockData.isClanFence() kullan
                    boolean isClanFence = me.mami.stratocraft.util.CustomBlockData.isClanFence(neighbor);
                    
                    // ✅ FALLBACK: TerritoryData kontrolü (backup)
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
    /**
     * ✅ YENİ: 3D flood-fill ile çit lokasyonlarını topla
     * Klan kristalini çevreleyen tüm klan çitlerini bulur
     */
    private List<Location> collectFenceLocations(Location crystalLoc, Clan clan) {
        List<Location> fenceLocations = new ArrayList<>();
        
        if (crystalLoc == null || crystalLoc.getWorld() == null || clan == null) {
            return fenceLocations;
        }
        
        // ✅ YENİ: 3D flood-fill ile çitleri bul
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        
        Block centerBlock = crystalLoc.getBlock();
        queue.add(centerBlock);
        visited.add(centerBlock);
        
        int maxIterations = 50000; // Büyük alanlar için limit
        int iterations = 0;
        
        while (!queue.isEmpty() && iterations < maxIterations) {
            Block current = queue.poll();
            iterations++;
            
            // ✅ YENİ: 3D flood-fill (6 yöne bak)
            BlockFace[] faces = {
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN
            };
            
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (visited.contains(neighbor)) continue;
                
                Material type = neighbor.getType();
                
                if (type == Material.OAK_FENCE) {
                    // ✅ YENİ: CustomBlockData.isClanFence() kullan
                    boolean isClanFence = me.mami.stratocraft.util.CustomBlockData.isClanFence(neighbor);
                    
                    if (isClanFence) {
                        UUID fenceClanId = me.mami.stratocraft.util.CustomBlockData.getClanFenceData(neighbor);
                        if (fenceClanId != null && fenceClanId.equals(clan.getId())) {
                            fenceLocations.add(neighbor.getLocation());
                            visited.add(neighbor);
                            continue; // Sınır, devam etme
                        }
                    }
                }
                
                // Hava veya geçilebilir blok
                if (type == Material.AIR || 
                    type == Material.CAVE_AIR || 
                    type == Material.VOID_AIR ||
                    !type.isSolid()) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                } else {
                    // Solid blok - engel
                    visited.add(neighbor);
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

