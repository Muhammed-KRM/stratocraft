package me.mami.stratocraft.listener;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.enums.TrapType;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.TrapManager;

/**
 * Tuzak sistemi dinleyicisi:
 * - Tuzak oluşturma (LODESTONE'a sağ tık + yakıt)
 * - Tuzak tetikleme (PlayerMoveEvent)
 * - Tuzak kapatma kontrolü
 */
public class TrapListener implements Listener {
    private final TrapManager trapManager;

    public TrapListener(TrapManager trapManager) {
        this.trapManager = trapManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTrapInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;

        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        boolean isTrapCoreItem = ItemManager.isCustomItem(handItem, "TRAP_CORE");

        // 1. TRAP_CORE ile yere bakıp sağ tık (LODESTONE oluştur)
        if (isTrapCoreItem
                && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            // RayTrace ile baktığı yöndeki bloğu bul
            Block placeBlock = player.getTargetBlockExact(5);
            if (placeBlock == null)
                return;

            // Alttan koymayı engelle
            if (event.getBlockFace() == org.bukkit.block.BlockFace.DOWN) {
                return;
            }

            placeBlock = placeBlock.getRelative(event.getBlockFace());

            // Altında solid blok olmalı
            if (!placeBlock.getRelative(org.bukkit.block.BlockFace.DOWN).getType().isSolid()) {
                player.sendMessage("§cTuzak çekirdeği havaya koyulamaz! Altında blok olmalı.");
                return;
            }

            // Üstünde blok olmamalı (veya koyulan yer dolu olmamalı)
            if (placeBlock.getType().isSolid()) {
                return;
            }
            if (placeBlock.getRelative(org.bukkit.block.BlockFace.UP).getType().isSolid()) {
                player.sendMessage("§cTuzak çekirdeği sıkışık alana koyulamaz! Üstü açık olmalı.");
                return;
            }

            placeBlock.setType(Material.LODESTONE);
            
            // ✅ DÜZELTME: CustomBlockData kütüphanesi ile PDC kullan (LODESTONE TileState değil ama artık çalışıyor)
            me.mami.stratocraft.util.CustomBlockData.setTrapCoreData(placeBlock, player.getUniqueId());
            
            // ❌ ESKİ: Metadata kaldırıldı
            // placeBlock.setMetadata("TrapCoreItem", new org.bukkit.metadata.FixedMetadataValue(
            //         me.mami.stratocraft.Main.getInstance(), true));

            // ✅ Metadata kalıcı olmadığı için dosyaya kaydet (sunucu restart sonrası için)
            // ✅ Memory'de tutulacak (registerInactiveTrapCore) + PDC'ye de kaydediliyor
            trapManager.registerInactiveTrapCore(placeBlock.getLocation(), player.getUniqueId());

            player.sendMessage("§a§lTuzak çekirdeği yerleştirildi!");
            player.sendMessage("§7Şimdi etrafına Magma Block çerçevesi yap (3x3, 3x6, 5x5, vb.).");

            // Item tüket (null kontrolü ile)
            if (handItem != null && handItem.getAmount() > 0) {
                handItem.setAmount(handItem.getAmount() - 1);
            }
            event.setCancelled(true);
            return;
        }

        // 2. Tuzak aktifleştirme: Üstteki bloklardan birine shift+sağ tık + yakıt
        // VEYA LODESTONE'a yakıt ile sağ tık (eski yöntem - geriye dönük uyumluluk)
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;
        
        // Shift+sağ tık: Üstteki bloklardan birine tıklanınca, altındaki tuzak çekirdeğini bul
        Block trapCoreBlock = null;
        if (player.isSneaking()) {
            // Tıklanan bloğun altında LODESTONE var mı? (1 veya 2 blok altında)
            Block below1 = block.getRelative(0, -1, 0);
            if (below1.getType() == Material.LODESTONE &&
                    (below1.hasMetadata("TrapCoreItem") || trapManager.isInactiveTrapCore(below1.getLocation()))) {
                trapCoreBlock = below1;
            } else {
                Block below2 = below1.getRelative(0, -1, 0);
                if (below2.getType() == Material.LODESTONE &&
                        (below2.hasMetadata("TrapCoreItem") || trapManager.isInactiveTrapCore(below2.getLocation()))) {
                    trapCoreBlock = below2;
                }
            }
            
            if (trapCoreBlock == null) {
                return; // Altında tuzak çekirdeği yok
            }
        } else {
            // Shift yok: Direkt LODESTONE'a tıklama (eski yöntem - geriye dönük uyumluluk)
            if (block.getType() == Material.LODESTONE &&
                    (block.hasMetadata("TrapCoreItem") || trapManager.isInactiveTrapCore(block.getLocation()))) {
                trapCoreBlock = block;
            } else {
                return; // Shift yok ve LODESTONE değil, işlem yapma
            }
        }

        // TrapCoreItem metadata'sı veya dosyadan yüklenen veri kontrolü
        if (trapCoreBlock != null &&
                (trapCoreBlock.hasMetadata("TrapCoreItem") || trapManager.isInactiveTrapCore(trapCoreBlock.getLocation()))) {
            // Tuzak yapısı kontrolü (çerçeve tamamlanmış mı?)
            if (!trapManager.isTrapStructure(trapCoreBlock)) {
                player.sendMessage("§cTuzak çerçevesi tamamlanmamış! Magma Block çerçevesi yap (3x3, 3x6, 5x5, vb.).");
                event.setCancelled(true);
                return;
            }

            // Yakıt kontrolü (null kontrolü)
            if (handItem == null || handItem.getType() == Material.AIR) {
                player.sendMessage("§cElinde yakıt olmalı!");
                event.setCancelled(true);
                return;
            }

            Material fuelMaterial = handItem.getType();
            boolean isValidFuel = fuelMaterial == Material.DIAMOND ||
                    fuelMaterial == Material.EMERALD ||
                    ItemManager.isCustomItem(handItem, "TITANIUM_INGOT");

            if (!isValidFuel) {
                player.sendMessage("§cGeçersiz yakıt! Elmas, Zümrüt veya Titanyum kullan.");
                event.setCancelled(true);
                return;
            }

            // Tuzak tipini belirle (ikincil eşyaya göre)
            TrapType trapType = determineTrapType(player);
            if (trapType == null) {
                player.sendMessage("§cTuzak tipi belirlenemedi! Elinde şunlardan biri olmalı:");
                player.sendMessage("§7- Magma Cream (Cehennem Tuzağı)");
                player.sendMessage("§7- Lightning Core (Şok Tuzağı)");
                player.sendMessage("§7- Ender Pearl (Kara Delik)");
                player.sendMessage("§7- TNT (Mayın)");
                player.sendMessage("§7- Spider Eye (Zehir Tuzağı)");
                event.setCancelled(true);
                return;
            }

            // Tuzak oluştur
            if (trapManager.createTrap(player, trapCoreBlock, trapType, fuelMaterial)) {
                // Yakıtı tüket (null kontrolü ile)
                if (handItem != null && handItem.getAmount() > 0) {
                    handItem.setAmount(handItem.getAmount() - 1);
                }
                event.setCancelled(true);
            }
        }
    }

    private TrapType determineTrapType(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        // Off-hand kontrolü
        if (offHand != null) {
            if (offHand.getType() == Material.MAGMA_CREAM) {
                return TrapType.HELL_TRAP;
            } else if (ItemManager.isCustomItem(offHand, "LIGHTNING_CORE")) {
                return TrapType.SHOCK_TRAP;
            } else if (offHand.getType() == Material.ENDER_PEARL) {
                return TrapType.BLACK_HOLE;
            } else if (offHand.getType() == Material.TNT) {
                return TrapType.MINE;
            } else if (offHand.getType() == Material.SPIDER_EYE) {
                return TrapType.POISON_TRAP;
            }
        }

        // Envanter kontrolü (off-hand'da yoksa)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null)
                continue;

            if (item.getType() == Material.MAGMA_CREAM && item != mainHand) {
                return TrapType.HELL_TRAP;
            } else if (ItemManager.isCustomItem(item, "LIGHTNING_CORE") && item != mainHand) {
                return TrapType.SHOCK_TRAP;
            } else if (item.getType() == Material.ENDER_PEARL && item != mainHand) {
                return TrapType.BLACK_HOLE;
            } else if (item.getType() == Material.TNT && item != mainHand) {
                return TrapType.MINE;
            } else if (item.getType() == Material.SPIDER_EYE && item != mainHand) {
                return TrapType.POISON_TRAP;
            }
        }

        return null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış (X, Y, Z kontrolü)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Oyuncu sadece kafasını çevirmiş, işlem yapma
        }

        Player player = event.getPlayer();
        Block standingBlock = event.getTo().getBlock();

        // Tuzak tetikleme kontrolü - oyuncunun üzerinde durduğu blok (üstteki bloklara basıldığında)
        // Bu blok tuzağın üstündeki kapak bloğu olabilir
        trapManager.triggerTrap(standingBlock.getLocation(), player);
        
        // Ayrıca oyuncunun altındaki blokları da kontrol et (çerçeve bloklarına basıldığında)
        Block below = standingBlock.getRelative(0, -1, 0);
        if (below.getType() == Material.MAGMA_BLOCK || below.getType() == Material.LODESTONE) {
            trapManager.triggerTrap(below.getLocation(), player);
        }
        
        // Çerçeve bloklarının üstündeki bloklara basıldığında da kontrol et
        Block below2 = below.getRelative(0, -1, 0);
        if (below2.getType() == Material.MAGMA_BLOCK) {
            trapManager.triggerTrap(below2.getLocation(), player);
        }

        // ✅ YENİ: Tuzak kapatma kontrolü (PersistentDataContainer)
        if (below.getType() == Material.LODESTONE && me.mami.stratocraft.util.CustomBlockData.isTrapCore(below)) {
            trapManager.checkTrapCoverage(below.getLocation());
        }
        
        // ❌ ESKİ: Metadata kontrolü kaldırıldı
        // if (below.getType() == Material.LODESTONE && below.hasMetadata("TrapCore")) {
        //     trapManager.checkTrapCoverage(below.getLocation());
        // }
    }
    
    /**
     * ✅ YENİ: Tuzak çekirdeği kırıldığında özel item drop et
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTrapCoreBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        if (block.getType() != Material.LODESTONE) {
            return;
        }
        
        Location trapLoc = block.getLocation();
        
        // ✅ ÖNCE MEMORY'DEN KONTROL ET (TileState gerektirmez)
        UUID ownerId = trapManager.getInactiveTrapCoreOwner(trapLoc);
        
        // ✅ Eğer memory'de yoksa, aktif tuzak kontrolü yap
        if (ownerId == null) {
            // Aktif tuzak var mı kontrol et (activeTraps Map'inden)
            java.util.Map<org.bukkit.Location, me.mami.stratocraft.manager.TrapManager.TrapData> activeTraps = trapManager.getActiveTraps();
            me.mami.stratocraft.manager.TrapManager.TrapData trapData = activeTraps.get(trapLoc);
            if (trapData != null) {
                ownerId = trapData.getOwnerId();
            }
        }
        
        // ✅ Eğer hala memory'de yoksa, PDC'den oku (chunk yüklüyse)
        if (ownerId == null) {
            try {
                org.bukkit.Chunk chunk = trapLoc.getChunk();
                if (chunk.isLoaded()) {
                    ownerId = me.mami.stratocraft.util.CustomBlockData.getTrapCoreOwner(block);
                }
            } catch (Exception e) {
                // Chunk yüklenemiyorsa atla
            }
        }
        
        if (ownerId == null) {
            return; // Normal LODESTONE
        }
        
        // ✅ Normal drop'ları iptal et
        event.setDropItems(false);
        
        // ✅ Özel item oluştur (TRAP_CORE item'ı)
        // ✅ DÜZELTME: Owner verisi ekleme - stacklenme için owner verisi eklenmeyecek
        // Owner verisi sadece yerleştirme sırasında memory'den alınır, item'da tutulmaz
        // Bu sayede tüm tuzak çekirdekleri stacklenebilir
        ItemStack trapCoreItem = ItemManager.TRAP_CORE != null ? ItemManager.TRAP_CORE.clone() : null;
        if (trapCoreItem != null) {
            // ✅ Owner verisi EKLENMEYECEK - stacklenme için
            // Owner verisi sadece TrapManager'da memory'de tutulur
            // Item'da owner verisi yok, bu yüzden tüm çekirdekler stacklenebilir
            
            // ✅ Özel item'ı drop et
            block.getWorld().dropItemNaturally(block.getLocation(), trapCoreItem);
        }
        
        // ✅ Tuzak çekirdeğini temizle (TrapManager'dan)
        trapManager.removeTrap(block.getLocation());
        
        // ✅ Inactive trap core kayıtlarını da temizle (eğer varsa)
        // Not: removeTrap() zaten active trap'leri temizliyor, ama inactive trap core'ları da kontrol etmeliyiz
        // TrapManager'da inactiveTrapCores Map'i var, ama public metod yok, bu yüzden removeTrap() yeterli
        
        // ✅ PERFORMANS: Cache temizleme ile birlikte veri silme
        me.mami.stratocraft.util.CustomBlockData.removeTrapCoreData(block);
    }
    
    /**
     * ✅ YENİ: BlockPlaceEvent'te ItemStack'ten veri geri yükleme
     * Yapı çekirdeği gibi: Tuzak çekirdeği yerleştirildiğinde memory'den owner verisi alınır
     * Item'da owner verisi yok (stacklenme için), bu yüzden sadece memory'den kontrol edilir
     * ✅ KRİTİK: MONITOR priority kullan (blok artık dünyada olduğu için PDC'ye yazabiliriz)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrapCorePlaceRestore(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        
        if (block.getType() != Material.LODESTONE || item == null) {
            return;
        }
        
        // ✅ ItemStack'ten kontrol et (TRAP_CORE item'ı mı?)
        if (!ItemManager.isCustomItem(item, "TRAP_CORE")) {
            return;
        }
        
        // ✅ DÜZELTME: Item'da owner verisi yok (stacklenme için)
        // Owner verisi sadece TrapManager'da memory'de tutulur
        // Yerleştirme sırasında oyuncunun UUID'si kullanılır (onTrapInteract'te zaten yapılıyor)
        // Bu event sadece item kontrolü için, veri geri yükleme yapılmaz
        // Çünkü onTrapInteract'te zaten CustomBlockData.setTrapCoreData() çağrılıyor
    }
}
