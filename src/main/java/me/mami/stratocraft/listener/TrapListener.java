package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.TrapManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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
            placeBlock.setMetadata("TrapCoreItem", new org.bukkit.metadata.FixedMetadataValue(
                    me.mami.stratocraft.Main.getInstance(), true));

            // Metadata kalıcı olmadığı için dosyaya kaydet (sunucu restart sonrası için)
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
            TrapManager.TrapType trapType = determineTrapType(player);
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

    private TrapManager.TrapType determineTrapType(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        // Off-hand kontrolü
        if (offHand != null) {
            if (offHand.getType() == Material.MAGMA_CREAM) {
                return TrapManager.TrapType.HELL_TRAP;
            } else if (ItemManager.isCustomItem(offHand, "LIGHTNING_CORE")) {
                return TrapManager.TrapType.SHOCK_TRAP;
            } else if (offHand.getType() == Material.ENDER_PEARL) {
                return TrapManager.TrapType.BLACK_HOLE;
            } else if (offHand.getType() == Material.TNT) {
                return TrapManager.TrapType.MINE;
            } else if (offHand.getType() == Material.SPIDER_EYE) {
                return TrapManager.TrapType.POISON_TRAP;
            }
        }

        // Envanter kontrolü (off-hand'da yoksa)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null)
                continue;

            if (item.getType() == Material.MAGMA_CREAM && item != mainHand) {
                return TrapManager.TrapType.HELL_TRAP;
            } else if (ItemManager.isCustomItem(item, "LIGHTNING_CORE") && item != mainHand) {
                return TrapManager.TrapType.SHOCK_TRAP;
            } else if (item.getType() == Material.ENDER_PEARL && item != mainHand) {
                return TrapManager.TrapType.BLACK_HOLE;
            } else if (item.getType() == Material.TNT && item != mainHand) {
                return TrapManager.TrapType.MINE;
            } else if (item.getType() == Material.SPIDER_EYE && item != mainHand) {
                return TrapManager.TrapType.POISON_TRAP;
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

        // Tuzak kapatma kontrolü (oyuncu üzerinde yürüyorsa, altındaki tuzakları kontrol et)
        if (below.getType() == Material.LODESTONE && below.hasMetadata("TrapCore")) {
            trapManager.checkTrapCoverage(below.getLocation());
        }
    }
}
