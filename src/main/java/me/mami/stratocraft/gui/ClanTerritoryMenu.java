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
        
        // ✅ YENİ: BoundaryLine boşsa ama radius varsa, dinamik olarak hesapla
        List<Location> finalBoundaryLine;
        if (boundaryLine.isEmpty()) {
            int radius = territoryData.getRadius();
            Location center = territoryData.getCenter();
            if (radius > 0 && center != null && center.getWorld() != null) {
                // Radius varsa, dinamik olarak sınır çizgisi oluştur
                int particleCount = (int) (radius * 2 * Math.PI / 2.0); // Her 2 blokta bir partikül
                if (particleCount < 8) {
                    particleCount = 8; // En az 8 nokta
                }
                finalBoundaryLine = new ArrayList<>();
                for (int i = 0; i < particleCount; i++) {
                    double angle = (2 * Math.PI * i) / particleCount;
                    double x = center.getX() + radius * Math.cos(angle);
                    double z = center.getZ() + radius * Math.sin(angle);
                    Location boundaryLoc = new Location(center.getWorld(), x, center.getY(), z);
                    finalBoundaryLine.add(boundaryLoc);
                }
            } else {
                player.sendMessage("§cSınır koordinatları hesaplanmamış!");
                player.sendMessage("§7Önce 'Alan Güncelle' butonuna basın.");
                return;
            }
        } else {
            // ✅ DÜZELTME: Lambda içinde kullanmak için final kopya oluştur
            finalBoundaryLine = new ArrayList<>(boundaryLine);
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
            
            // ✅ OPTİMİZE: Maksimum partikül sayısı sınırı (performans için)
            int maxParticles = 50; // Her seferinde maksimum 50 partikül
            int count = 0;
            int particlesSpawned = 0;
            
            for (Location boundaryLoc : finalBoundaryLine) {
                if (particlesSpawned >= maxParticles) break; // ✅ Maksimum partikül sayısına ulaşıldı
                
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
                
                particlesSpawned++;
                count++;
            }
        }, 0L, 20L); // ✅ OPTİMİZE: Her 1 saniyede bir (20 tick) - performans için
        
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
        
        player.sendMessage("§7Klan kristali etrafındaki çitler taranıyor...");
        
        // ✅ YENİ: Async çit kontrolü ve sınır hesaplama
        // Önce chunk'ları yükle (main thread'de)
        org.bukkit.Chunk crystalChunk = crystalLoc.getChunk();
        if (!crystalChunk.isLoaded()) {
            try {
                crystalChunk.load(false);
            } catch (Exception e) {
                player.sendMessage("§cKlan kristali chunk'ı yüklenemedi! Lütfen tekrar deneyin.");
                return;
            }
        }
        
        // Async çit kontrolü ve sınır hesaplama
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // ✅ YENİ: Klan kristalini çevreleyen çitler tam şekilde kapanıyor mu kontrol et
                Block crystalBlock = crystalLoc.getBlock();
                boolean isSurrounded = isSurroundedByClanFences(crystalBlock, clan);
                
                if (!isSurrounded) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage("§cKlan kristalini çevreleyen çitler tam şekilde kapanmamış!");
                        player.sendMessage("§7Boşluk var veya normal çit bulundu. Lütfen tüm çitleri kontrol edin.");
                        player.sendMessage("§7Not: Sadece Klan Çiti (CLAN_FENCE item'ı ile yerleştirilen) kullanılmalıdır.");
                    });
                    return;
                }
                
                // ✅ YENİ: Çitler tam şekilde kapanmış, yeni çit lokasyonlarını topla
                player.sendMessage("§7Çitler tespit edildi, lokasyonlar toplanıyor...");
                List<Location> newFenceLocations = collectFenceLocations(crystalLoc, clan);
                
                if (newFenceLocations.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage("§cHiç klan çiti bulunamadı!");
                        player.sendMessage("§7Klan kristali etrafında klan çiti olmalıdır.");
                        player.sendMessage("§7Not: Normal çit değil, Klan Çiti (CLAN_FENCE item'ı) kullanın.");
                    });
                    return;
                }
                
                // ✅ YENİ: Eski sınırları temizle
                territoryData.clearBoundaries();
                territoryData.clearFenceLocations();
                
                // ✅ YENİ: Yeni çit lokasyonlarını ekle
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
                
                // ✅ YENİ: Sınırları hesapla
                territoryData.calculateBoundaries();
                
                // Main thread'e geri dön
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§a§l✓ Klan alanı başarıyla güncellendi!");
                    player.sendMessage("§7Çit Sayısı: §e" + territoryData.getFenceCount());
                    player.sendMessage("§7Sınır Koordinat Sayısı: §e" + territoryData.getBoundaryCoordinates().size());
                    player.sendMessage("§7Y Yüksekliği: §e" + territoryData.getMinY() + " - " + territoryData.getMaxY());
                    
                    // Cache'i güncelle
                    if (territoryManager != null) {
                        territoryManager.setCacheDirty();
                    }
                });
            } catch (Exception e) {
                // Hata durumunda oyuncuya bilgi ver
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cAlan güncelleme hatası: " + e.getMessage());
                    plugin.getLogger().warning("Klan alanı güncelleme hatası: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
    }
    
    /**
     * ✅ YENİ: Klan kristalini çevreleyen çitler tam şekilde kapanıyor mu kontrol et
     * TerritoryListener'daki isSurroundedByClanFences3D() metoduna benzer 2.5D algoritması
     */
    private boolean isSurroundedByClanFences(Block center, Clan clan) {
        if (center == null || center.getWorld() == null) {
            return false;
        }
        
        // ✅ YENİ: İç alan taramasını mümkünse havada başlat (zemin üstü 1 blok)
        Block start = center;
        if (start.getType().isSolid()) {
            Block up = start.getRelative(BlockFace.UP);
            if (up != null && !up.getType().isSolid()) {
                start = up;
            }
        }
        
        final int scanY = start.getY();
        final int centerX = center.getX();
        final int centerZ = center.getZ();
        
        // ✅ YENİ: Makul sınır (lag önlemek için). 64 blok yarıçap = 4 chunk.
        final int maxRadius = 64;
        final int minArea = 9; // 3x3
        final int maxIterations = (2 * maxRadius + 1) * (2 * maxRadius + 1); // 2D alan üst limiti
        
        // ✅ YENİ: Packed koordinat kullan (daha hızlı)
        Set<Long> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        boolean foundClanFence = false;
        
        queue.add(start);
        visited.add(packCoords(start));
        
        int iterations = 0;
        
        // ✅ YENİ: Sadece yatay yönler (2.5D algoritması)
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            if (iterations > maxIterations) {
                return false; // Çok büyük alan veya açık alan
            }
            
            for (BlockFace face : faces) {
                int nx = current.getX() + face.getModX();
                int nz = current.getZ() + face.getModZ();
                
                // ✅ YENİ: Yarıçap sınırı (çitle çevrili değilse dışarı sızmayı hızlı yakalar)
                if (Math.abs(nx - centerX) > maxRadius || Math.abs(nz - centerZ) > maxRadius) {
                    return false; // Açık alan
                }
                
                // ✅ YENİ: Chunk yükleme kontrolü
                org.bukkit.Chunk chunk = center.getWorld().getChunkAt(nx >> 4, nz >> 4);
                if (!chunk.isLoaded()) {
                    try {
                        chunk.load(false);
                    } catch (Exception e) {
                        // Chunk yüklenemiyorsa atla
                        continue;
                    }
                }
                
                Block neighbor = center.getWorld().getBlockAt(nx, scanY, nz);
                long neighborKey = packCoords(neighbor);
                if (visited.contains(neighborKey)) continue;
                
                // ✅ YENİ: Çit bariyeri kontrolü: scanY ve scanY-1 seviyesinde bak
                Block fenceAtY = neighbor.getType() == Material.OAK_FENCE ? neighbor : null;
                Block fenceBelow = center.getWorld().getBlockAt(nx, scanY - 1, nz);
                if (fenceBelow.getType() == Material.OAK_FENCE) {
                    if (fenceAtY == null) fenceAtY = fenceBelow;
                }
                
                if (fenceAtY != null && fenceAtY.getType() == Material.OAK_FENCE) {
                    // ✅ YENİ: CustomBlockData.isClanFence() kullan
                    boolean isClanFence = me.mami.stratocraft.util.CustomBlockData.isClanFence(fenceAtY);
                    
                    if (!isClanFence) {
                        // Normal çit varsa alan geçersiz
                        return false;
                    }
                    
                    foundClanFence = true;
                    visited.add(neighborKey); // Bariyer olarak işaretle
                    continue;
                }
                
                Material type = neighbor.getType();
                
                // Solid blok bariyer (duvar vs.)
                if (type.isSolid()) {
                    visited.add(neighborKey);
                    continue;
                }
                
                // Geçilebilir alan
                visited.add(neighborKey);
                queue.add(neighbor);
            }
        }
        
        // ✅ YENİ: Minimum alan kontrolü ve klan çiti bulundu mu kontrolü
        return visited.size() >= minArea && foundClanFence;
    }
    
    /**
     * ✅ YENİ: Koordinatları paketle (performans için)
     */
    private long packCoords(Block block) {
        return ((long)block.getX() & 0x3FFFFFF) | (((long)block.getZ() & 0x3FFFFFF) << 26) | (((long)block.getY() & 0xFFF) << 52);
    }
    
    /**
     * ✅ YENİ: Klan kristali etrafındaki çit lokasyonlarını topla
     * TerritoryListener'daki findAndAddFenceLocations() metoduna benzer
     * Klan kristalinden etrafını tarayıp klan çitlerini tespit eder
     */
    private List<Location> collectFenceLocations(Location crystalLoc, Clan clan) {
        List<Location> fenceLocations = new ArrayList<>();
        
        if (crystalLoc == null || crystalLoc.getWorld() == null || clan == null) {
            return fenceLocations;
        }
        
        org.bukkit.World world = crystalLoc.getWorld();
        int centerX = crystalLoc.getBlockX();
        int centerY = crystalLoc.getBlockY();
        int centerZ = crystalLoc.getBlockZ();
        
        // ✅ YENİ: 2D flood-fill ile çitleri bul (TerritoryListener'daki gibi)
        Set<Location> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        
        // ✅ YENİ: Başlangıç noktası - kristal bloğu
        Block startBlock = world.getBlockAt(centerX, centerY, centerZ);
        
        // ✅ YENİ: Chunk yükleme kontrolü
        org.bukkit.Chunk chunk = startBlock.getChunk();
        if (!chunk.isLoaded()) {
            try {
                chunk.load(false);
            } catch (Exception e) {
                // Chunk yüklenemiyorsa boş liste döner
                return fenceLocations;
            }
        }
        
        queue.add(startBlock);
        visited.add(startBlock.getLocation());
        
        int maxIterations = 50000; // Büyük alanlar için limit
        int iterations = 0;
        
        while (!queue.isEmpty() && iterations < maxIterations) {
            Block current = queue.poll();
            iterations++;
            
            // ✅ YENİ: 4 yöne bak (2D flood-fill - TerritoryListener'daki gibi)
            Block[] neighbors = {
                current.getRelative(BlockFace.NORTH),
                current.getRelative(BlockFace.SOUTH),
                current.getRelative(BlockFace.EAST),
                current.getRelative(BlockFace.WEST)
            };
            
            for (Block neighbor : neighbors) {
                if (neighbor == null || neighbor.getWorld() == null) continue;
                
                Location neighborLoc = neighbor.getLocation();
                if (visited.contains(neighborLoc)) continue;
                
                // ✅ YENİ: Chunk yükleme kontrolü
                org.bukkit.Chunk neighborChunk = neighbor.getChunk();
                if (!neighborChunk.isLoaded()) {
                    try {
                        neighborChunk.load(false);
                    } catch (Exception e) {
                        // Chunk yüklenemiyorsa atla
                        continue;
                    }
                }
                
                // ✅ YENİ: Çit kontrolü - CustomBlockData.isClanFence() kullan
                if (neighbor.getType() == Material.OAK_FENCE) {
                    boolean isClanFence = me.mami.stratocraft.util.CustomBlockData.isClanFence(neighbor);
                    
                    if (isClanFence) {
                        // Klan çiti bulundu, listeye ekle
                        fenceLocations.add(neighborLoc);
                        visited.add(neighborLoc);
                        continue; // Sınır, devam etme
                    } else {
                        // Normal çit, sınır sayılmaz - engel olarak kabul et
                        visited.add(neighborLoc);
                        continue;
                    }
                }
                
                // ✅ YENİ: Hava veya geçilebilir blok
                if (neighbor.getType().isAir() || !neighbor.getType().isSolid()) {
                    visited.add(neighborLoc);
                    queue.add(neighbor);
                } else {
                    // Solid blok - engel
                    visited.add(neighborLoc);
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

