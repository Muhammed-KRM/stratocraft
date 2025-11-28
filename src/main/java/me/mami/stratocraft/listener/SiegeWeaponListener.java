package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.SiegeWeaponManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Savaş Alanı Yapıları Event Handler'ları
 * 
 * Tüm event handler'lar burada, mantık SiegeWeaponManager'da
 */
public class SiegeWeaponListener implements Listener {
    private final SiegeWeaponManager manager;
    
    public SiegeWeaponListener(SiegeWeaponManager manager) {
        this.manager = manager;
    }
    
    // ========== MANCINIK (CATAPULT) ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCatapultInteract(PlayerInteractEvent event) {
        // Çift el kontrolü (BatteryListener mantığı)
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Mancınık kontrolü: Basamak (STONE_BRICK_STAIRS) veya Merdiven
        if (block.getType() != Material.STONE_BRICK_STAIRS && 
            block.getType() != Material.COBBLESTONE_STAIRS) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Cooldown kontrolü
        if (!manager.canFireCatapult(player)) {
            long remaining = manager.getCatapultCooldownRemaining(player);
            player.sendMessage("§cMancınık hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }
        
        manager.fireCatapult(player, block);
        event.setCancelled(true); // Bloğu yanlışlıkla koymayı engelle
    }
    
    @EventHandler
    public void onProjectileHit(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.FallingBlock)) return;
        
        org.bukkit.entity.FallingBlock fallingBlock = (org.bukkit.entity.FallingBlock) event.getEntity();
        
        // Mancınık mermisi mi?
        if (!fallingBlock.hasMetadata("SiegeAmmo")) return;
        
        event.setCancelled(true); // Bloğa dönüşmesini engelle
        
        org.bukkit.Location hitLoc = fallingBlock.getLocation();
        
        // Patlama Yarat (Güç: 4.0 = TNT)
        fallingBlock.getWorld().createExplosion(hitLoc, 4.0f, true);
        
        // Partikül efekti
        fallingBlock.getWorld().spawnParticle(
            org.bukkit.Particle.EXPLOSION_LARGE, 
            hitLoc, 
            5
        );
        
        fallingBlock.remove(); // Entity'i sil
        
        // Yakındaki oyunculara mesaj
        for (org.bukkit.entity.Entity nearby : hitLoc.getWorld().getNearbyEntities(hitLoc, 5, 5, 5)) {
            if (nearby instanceof Player) {
                Player target = (Player) nearby;
                target.sendMessage("§c§lMANCINIK MERMİSİNE YAKALANDIN!");
            }
        }
    }
    
    @EventHandler
    public void onBallistaArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Arrow)) return;
        
        org.bukkit.entity.Arrow arrow = (org.bukkit.entity.Arrow) event.getEntity();
        
        // Balista oku mu?
        if (!arrow.hasMetadata("BallistaArrow")) return;
        
        org.bukkit.Location hitLoc = arrow.getLocation();
        
        // Küçük patlama efekti (Mancınıktan daha az güçlü)
        arrow.getWorld().createExplosion(hitLoc, 1.5f, false); // Blokları kırmaz
        
        // Partikül efekti
        arrow.getWorld().spawnParticle(
            org.bukkit.Particle.EXPLOSION_LARGE,
            hitLoc,
            3
        );
        
        arrow.remove(); // Oku sil
    }
    
    // ========== ENERJİ KALKANI (FORCE FIELD) ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onShieldGeneratorInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Jeneratör kontrolü: Beacon bloğu
        if (block.getType() != Material.BEACON) return;
        
        // Can Tapınağı kontrolü - Eğer Can Tapınağı ise bu handler'ı atla
        if (block.hasMetadata("HealingShrine")) return;
        
        // Zaten aktif bir kalkan var mı?
        if (manager.isShieldActive(block.getLocation())) {
            event.getPlayer().sendMessage("§cBu jeneratör zaten aktif!");
            event.setCancelled(true);
            return;
        }
        
        manager.activateShield(block.getLocation());
        event.getPlayer().sendMessage("§b§lENERJİ KALKANI AKTİF!");
        event.setCancelled(true); // Beacon menüsünü açmayı engelle
    }
    
    @EventHandler
    public void onGeneratorBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Jeneratör mü?
        if (block.getType() != Material.BEACON) return;
        
        if (manager.removeShield(block.getLocation())) {
            Player breaker = event.getPlayer();
            if (breaker != null) {
                breaker.sendMessage("§eEnerji kalkanı devre dışı bırakıldı!");
            }
        }
    }
    
    @EventHandler
    public void onPlayerMoveIntoShield(PlayerMoveEvent event) {
        // PERFORMANS FİLTRESİ: Sadece blok değiştiyse çalış (X, Y, Z kontrolü)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Oyuncu sadece kafasını çevirmiş, işlem yapma
        }
        
        org.bukkit.Location to = event.getTo();
        if (to == null) return;
        
        // Kalkan içine girmeye çalışıyor mu?
        if (manager.isInsideShield(to)) {
            org.bukkit.Location from = event.getFrom();
            // Eğer dışarıdan içeriye giriyorsa engelle
            if (!manager.isInsideShield(from)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cEnerji kalkanı içine giremezsin! Önce kalkanı kır veya kapıyı aç.");
            }
        }
    }
    
    @EventHandler
    public void onProjectileHitShield(ProjectileHitEvent event) {
        org.bukkit.Location hitLoc = event.getEntity().getLocation();
        
        if (manager.isInsideShield(hitLoc)) {
            // Mermiyi yok et
            event.getEntity().remove();
            // Efekt
            hitLoc.getWorld().spawnParticle(
                org.bukkit.Particle.ELECTRIC_SPARK,
                hitLoc,
                10
            );
        }
    }
    
    // ========== KATEGORİ 2: KLAN ÖZEL YAPILAR ==========
    
    @EventHandler
    public void onHealingShrinePlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        
        // Beacon yerleştirildi mi?
        if (block.getType() != Material.BEACON) return;
        
        // Etrafında 3x3 Altın Bloğu var mı kontrol et
        if (manager.isShrineStructure(block)) {
            Player player = event.getPlayer();
            
            if (manager.createHealingShrine(block.getLocation(), player)) {
                player.sendMessage("§a§lCAN TAPINAĞI KURULDU!");
                player.sendMessage("§7Sadece klan üyeleriniz buradan faydalanabilir.");
            } else {
                player.sendMessage("§cCan Tapınağı kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onHealingShrineBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        if (block.hasMetadata("HealingShrine")) {
            manager.removeHealingShrine(block.getLocation());
            event.getPlayer().sendMessage("§eCan Tapınağı kırıldı!");
        }
    }
    
    // ========== KATEGORİ 1: YENİ YAPILAR ==========
    
    // 3. BALİSTA (Ballista)
    @EventHandler(priority = EventPriority.HIGH)
    public void onBallistaInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Balista kontrolü: Dispenser bloğu
        if (block.getType() != Material.DISPENSER) return;
        
        Player player = event.getPlayer();
        
        // Cooldown kontrolü
        if (!manager.canFireBallista(player)) {
            long remaining = manager.getBallistaCooldownRemaining(player);
            player.sendMessage("§cBalista hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }
        
        manager.fireBallista(player, block);
        event.setCancelled(true); // Dispenser menüsünü açmayı engelle
    }
    
    // 4. LAV FISKIYESI (Lava Fountain)
    @EventHandler(priority = EventPriority.HIGH)
    public void onLavaFountainInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Lav Fıskiyesi kontrolü: Cauldron (Kazan) + Lava içinde
        if (block.getType() != Material.CAULDRON) return;
        
        org.bukkit.block.data.Levelled cauldron = (org.bukkit.block.data.Levelled) block.getBlockData();
        if (cauldron.getLevel() < 3) return; // Tam dolu olmalı
        
        org.bukkit.Location loc = block.getLocation();
        
        if (!manager.canActivateLavaFountain(loc)) {
            long remaining = manager.getLavaFountainCooldownRemaining(loc);
            event.getPlayer().sendMessage("§cLav Fıskiyesi hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }
        
        manager.activateLavaFountain(loc);
        event.setCancelled(true); // Cauldron etkileşimini engelle
    }
    
    // 5. ZEHİR GAZI YAYICI (Poison Gas Dispenser)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPoisonDispenserInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Zehir Yayıcı kontrolü: Dropper bloğu
        if (block.getType() != Material.DROPPER) return;
        
        org.bukkit.Location loc = block.getLocation();
        
        if (!manager.canActivatePoisonDispenser(loc)) {
            long remaining = manager.getPoisonDispenserCooldownRemaining(loc);
            event.getPlayer().sendMessage("§cZehir Yayıcı hazır değil! " + remaining + " saniye kaldı.");
            event.setCancelled(true);
            return;
        }
        
        manager.activatePoisonDispenser(loc);
        event.setCancelled(true); // Dropper menüsünü açmayı engelle
    }
    
    // ========== KATEGORİ 2: YENİ YAPILAR ==========
    
    // 2. GÜÇ TOTEMİ (Power Totem)
    @EventHandler(priority = EventPriority.HIGH)
    public void onPowerTotemInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Güç Totemi kontrolü: Enchanting Table + Altında 2x2 Obsidyen
        if (block.getType() != Material.ENCHANTING_TABLE) return;
        
        if (manager.isTotemStructure(block, Material.OBSIDIAN)) {
            Player player = event.getPlayer();
            
            if (manager.createPowerTotem(block.getLocation(), player)) {
                player.sendMessage("§6§lGÜÇ TOTEMİ AKTİF!");
                player.sendMessage("§7Klan üyeleriniz buradan güç alacak.");
                event.setCancelled(true); // Enchanting Table menüsünü açmayı engelle
            } else {
                player.sendMessage("§cGüç Totemi kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }
    
    // 3. HIZ ÇEMBERİ (Speed Circle)
    @EventHandler(priority = EventPriority.HIGH)
    public void onSpeedCircleInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Hız Çemberi kontrolü: Ender Chest + Altında 2x2 Lapis Bloğu
        if (block.getType() != Material.ENDER_CHEST) return;
        
        if (manager.isTotemStructure(block, Material.LAPIS_BLOCK)) {
            Player player = event.getPlayer();
            
            if (manager.createSpeedCircle(block.getLocation(), player)) {
                player.sendMessage("§b§lHIZ ÇEMBERİ AKTİF!");
                player.sendMessage("§7Klan üyeleriniz buradan hız alacak.");
                event.setCancelled(true); // Ender Chest menüsünü açmayı engelle
            } else {
                player.sendMessage("§cHız Çemberi kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }
    
    // 4. SAVUNMA DUVARI (Defense Wall)
    @EventHandler(priority = EventPriority.HIGH)
    public void onDefenseWallInteract(PlayerInteractEvent event) {
        // Çift el kontrolü
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Savunma Duvarı kontrolü: Anvil + Altında 2x2 Demir Bloğu
        if (block.getType() != Material.ANVIL) return;
        
        if (manager.isTotemStructure(block, Material.IRON_BLOCK)) {
            Player player = event.getPlayer();
            
            if (manager.createDefenseWall(block.getLocation(), player)) {
                player.sendMessage("§7§lSAVUNMA DUVARI AKTİF!");
                player.sendMessage("§7Klan üyeleriniz buradan direnç alacak.");
                event.setCancelled(true); // Anvil menüsünü açmayı engelle
            } else {
                player.sendMessage("§cSavunma Duvarı kurmak için bir klana üye olmalısınız!");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onClanStructureBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        org.bukkit.Location loc = block.getLocation();
        
        if (block.hasMetadata("PowerTotem")) {
            manager.removePowerTotem(loc);
            event.getPlayer().sendMessage("§eGüç Totemi kırıldı!");
        } else if (block.hasMetadata("SpeedCircle")) {
            manager.removeSpeedCircle(loc);
            event.getPlayer().sendMessage("§eHız Çemberi kırıldı!");
        } else if (block.hasMetadata("DefenseWall")) {
            manager.removeDefenseWall(loc);
            event.getPlayer().sendMessage("§eSavunma Duvarı kırıldı!");
        }
    }
    
    /**
     * Oyuncu çıkışında cooldown'ları temizle (Memory leak önleme)
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.clearPlayerCooldowns(event.getPlayer().getUniqueId());
    }
}

