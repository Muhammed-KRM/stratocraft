package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Territory;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.Color;

import java.util.*;

public class TerritoryListener implements Listener {
    private final TerritoryManager territoryManager;
    private final SiegeManager siegeManager;
    private me.mami.stratocraft.manager.TerritoryBoundaryManager boundaryManager;
    private me.mami.stratocraft.manager.config.TerritoryConfig territoryConfig;
    
    // Klan kurma için chat input sistemi
    private final Map<UUID, PendingClanCreation> waitingForClanName = new HashMap<>();
    
    // Klan kristali taşıma için aktif task takibi (lag önleme)
    private final Map<UUID, org.bukkit.scheduler.BukkitTask> activeCrystalMoveTasks = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Bekleyen klan oluşturma verisi
    private static class PendingClanCreation {
        final Location crystalLoc;
        final EnderCrystal crystalEntity;
        final Block placeLocation;
        
        PendingClanCreation(Location crystalLoc, EnderCrystal crystalEntity, Block placeLocation) {
            this.crystalLoc = crystalLoc;
            this.crystalEntity = crystalEntity;
            this.placeLocation = placeLocation;
        }
    }

    public TerritoryListener(TerritoryManager tm, SiegeManager sm) {
        this.territoryManager = tm;
        this.siegeManager = sm;
    }
    
    /**
     * TerritoryBoundaryManager setter
     */
    public void setBoundaryManager(me.mami.stratocraft.manager.TerritoryBoundaryManager boundaryManager) {
        this.boundaryManager = boundaryManager;
    }
    
    /**
     * TerritoryConfig setter
     */
    public void setTerritoryConfig(me.mami.stratocraft.manager.config.TerritoryConfig territoryConfig) {
        this.territoryConfig = territoryConfig;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(event.getPlayer())) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }
        
        Clan owner = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
        
        // Sahipsiz yerse kırılabilir
        if (owner == null) return;
        
        // Ölümsüz klan önleme: Kristal yoksa bölge koruması yok
        if (!owner.hasCrystal()) {
            return; // Kristal yoksa koruma yok
        }
        
        // YENİ: Klan yapıları kırılmamalı (korunmalı)
        Block block = event.getBlock();
        if (plugin != null && plugin.getStructureCoreManager() != null) {
            if (plugin.getStructureCoreManager().isStructureCore(block)) {
                // Bu bir yapı çekirdeği, kırılamaz
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cKlan yapıları kırılamaz! Yapıyı kaldırmak için klan menüsünü kullanın.");
                return;
            }
        }
        
        // Kendi yerinse kırılabilir (Rütbe kontrolü dahil)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        
        if (playerClan != null && playerClan.equals(owner)) {
            // Rütbe Kontrolü: Recruit (Acemi) yapı kıramaz
            if (playerClan.getRank(event.getPlayer().getUniqueId()) == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cAcemilerin yapı yıkma yetkisi yok!");
                return;
            }
            return; // Yetkisi varsa kırabilir
        }
        
        // Misafir İzni (Guest)
        if (owner.isGuest(event.getPlayer().getUniqueId())) {
             return; 
        }

        // --- ENERJİ KALKANI OFFLINE KORUMA ---
        // Eğer klan üyelerinden hiçbiri online değilse VE kalkan yakıtı > 0 ise hasarı iptal et
        Structure core = owner.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .findFirst().orElse(null);
        
        if (core != null && core.isShieldActive()) {
            boolean anyOnline = owner.getMembers().keySet().stream()
                    .anyMatch(uuid -> org.bukkit.Bukkit.getPlayer(uuid) != null);
            
            if (!anyOnline) {
                // Offline koruma aktif
                event.setCancelled(true);
                event.getPlayer().sendMessage("§bEnerji Kalkanı aktif! Offline klan korunuyor. Kalkan Gücü: " + core.getShieldFuel());
                core.consumeFuel(); // Yakıt tüket
                return;
            }
        }

        // --- TAMAMLANMIŞ KUŞATMA KONTROLLERİ ---
        
        // ✅ YENİ: Düşman bölgesi ise: SADECE BU OYUNCUNUN KLANIYLA SAVAŞTAYSA KIRILABİLİR
        if (playerClan != null && owner.isAtWarWith(playerClan.getId())) {
            // Eğer Ana Kristali (EnderCrystal entity) kırarsa oyunu bitir
            // Not: BEACON değil, EnderCrystal entity kontrolü yapılmalı (onCrystalBreak'te)
            // Burada sadece blok kırma izni ver
            
            // Stratejik yıkım: Diğer blokları kırmaya izin ver
            return; // Kuşatma altındayken diğer blokları kırmaya izin ver (Stratejik yıkım)
        }

        // Koruma Aktif (Savaş yoksa dokunamazsın)
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cBu bölge " + owner.getName() + " klanına ait! Önce kuşatma başlatmalısın.");
    }
    
    // ========== SANDIK AÇMA KORUMASI ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Admin bypass kontrolü
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(player)) {
                return; // Admin bypass yetkisi varsa korumaları atla
            }
        }
        
        // Sadece blok envanterleri (sandık, fırın vb.)
        if (event.getInventory().getType() != InventoryType.CHEST &&
            event.getInventory().getType() != InventoryType.ENDER_CHEST &&
            event.getInventory().getType() != InventoryType.BARREL &&
            event.getInventory().getType() != InventoryType.SHULKER_BOX) {
            return;
        }
        
        // Envanterin konumunu bul
        Location invLocationTemp = null;
        if (event.getInventory().getHolder() instanceof org.bukkit.block.BlockState) {
            org.bukkit.block.BlockState state = (org.bukkit.block.BlockState) event.getInventory().getHolder();
            invLocationTemp = state.getLocation();
        } else if (event.getView().getTopInventory().getLocation() != null) {
            invLocationTemp = event.getView().getTopInventory().getLocation();
        }
        
        if (invLocationTemp == null) return;
        final Location invLocation = invLocationTemp;
        
        // Bölge sahibi kontrolü
        Clan owner = territoryManager.getTerritoryOwner(invLocation);
        if (owner == null) return; // Sahipsiz yerse açılabilir
        
        // Ölümsüz klan önleme: Kristal yoksa bölge koruması yok
        if (!owner.hasCrystal()) {
            return; // Kristal yoksa koruma yok
        }
        
        // Oyuncu kontrolü
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        
        // Kendi yerinse açılabilir (Rütbe kontrolü dahil)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan != null && playerClan.equals(owner)) {
            // YENİ: Recruit chest açamaz
            if (playerClan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                player.sendMessage("§cAcemilerin chest açma yetkisi yok!");
                return;
            }
            return; // Yetkisi varsa açabilir
        }
        
        // Misafir izni
        if (owner.isGuest(player.getUniqueId())) {
            return; // Misafir açabilir
        }
        
        // ✅ YENİ: Savaş kontrolü - Sadece bu oyuncunun klanıyla savaşta ise açılabilir
        if (playerClan != null && owner.isAtWarWith(playerClan.getId())) {
            return; // Bu klanla savaşta ise açılabilir
        }
        
        // Klan bankası kontrolü (RitualInteractionListener'daki özel kontrol)
        Block block = invLocation.getBlock();
        if (block.hasMetadata("ClanBank")) {
            // Klan bankası özel kontrolü RitualInteractionListener'da yapılıyor
            return;
        }
        
        // Sanal Bağlantı kontrolü (VirtualStorageListener'daki özel kontrol)
        if (block.getType() == Material.ENDER_CHEST && playerClan != null) {
            final Location finalInvLocation = invLocation;
            Structure virtualLink = playerClan.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.TELEPORTER && 
                            s.getLocation().distance(finalInvLocation) <= 10)
                .findFirst().orElse(null);
            if (virtualLink != null) {
                // Sanal Bağlantı kontrolü VirtualStorageListener'da yapılıyor
                return;
            }
        }
        
        // Engelle
        event.setCancelled(true);
        player.sendMessage("§cBu sandık " + owner.getName() + " klanına ait! Önce kuşatma başlatmalısın.");
    }
    
    // ========== KLAN ALANI OTOMATIK GENİŞLETME ==========
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFencePlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(player)) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }
        
        // Material kontrolü - Sadece OAK_FENCE kontrol et (klan çiti OAK_FENCE)
        if (block.getType() != Material.OAK_FENCE) {
            return;
        }
        
        // KRİTİK: Normal çitlerin yerleştirilmesi engellenmeli
        // YENİ: Klan çiti item kontrolü (config'den)
        ItemStack item = event.getItemInHand();
        boolean isClanFence = false;
        
        if (territoryConfig != null && territoryConfig.isRequireClanFenceItem()) {
            if (item != null && ItemManager.isClanItem(item, "FENCE")) {
                isClanFence = true;
            }
        } else {
            // Config kapalıysa eski kontrol
            if (item != null && ItemManager.isClanItem(item, "FENCE")) {
                isClanFence = true;
            }
        }
        
        // Normal çit yerleştirme engelle
        if (!isClanFence) {
            event.setCancelled(true);
            player.sendMessage("§cKlan alanında sadece §6Klan Çiti §cyerleştirilebilir!");
            player.sendMessage("§7Normal çitler kabul edilmez. Klan Çiti craft edin.");
            return;
        }
        
        // YENİ: Metadata ekle (klan çiti işaretleme)
        if (territoryConfig != null) {
            String metadataKey = territoryConfig.getFenceMetadataKey();
            block.setMetadata(metadataKey, new org.bukkit.metadata.FixedMetadataValue(
                me.mami.stratocraft.Main.getInstance(), true));
        }
        
        // Oyuncunun klanı var mı?
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) {
            // Klan üyesi değil ama çit yerleştirilebilir (sonra klan kurulabilir)
            return;
        }
        
        // YENİ: TerritoryData'ya çit lokasyonu ekle
        if (boundaryManager != null) {
            boundaryManager.addFenceLocation(playerClan, block.getLocation());
        }
        
        // Kristal var mı?
        if (playerClan.getCrystalLocation() == null || !playerClan.hasCrystal()) {
            return; // Kristal yok, genişletme yok
        }
        
        Territory territory = playerClan.getTerritory();
        if (territory == null) return;
        
        Location crystalLoc = playerClan.getCrystalLocation();
        Location fenceLoc = block.getLocation();
        
        // Aynı dünya kontrolü
        if (!crystalLoc.getWorld().equals(fenceLoc.getWorld())) {
            return;
        }
        
        // Çit kristalden ne kadar uzakta?
        double distanceToCrystal = crystalLoc.distance(fenceLoc);
        int currentRadius = territory.getRadius();
        
        // Çit mevcut sınırın dışındaysa ve kristalden 50 bloktan fazla uzaktaysa genişlet
        if (distanceToCrystal > currentRadius && distanceToCrystal > 50) {
            // Yeni radius: Çitin kristale olan mesafesi + 5 blok buffer
            int newRadius = (int) Math.ceil(distanceToCrystal) + 5;
            int expandAmount = newRadius - currentRadius;
            
            // YENİ: Config'den maksimum genişletme limiti
            int maxExpansion = territoryConfig != null ? 
                territoryConfig.getMaxExpansionPerAction() : 20;
            
            // Maksimum genişletme limiti (anti-abuse)
            if (expandAmount > 0 && expandAmount <= maxExpansion) {
                territory.expand(expandAmount);
                territoryManager.setCacheDirty();
                
                // Oyuncuya bilgi ver (spam önleme için cooldown)
                long now = System.currentTimeMillis();
                Long lastExpandTime = lastTerritoryExpandTime.get(player.getUniqueId());
                if (lastExpandTime == null || (now - lastExpandTime) > 5000L) { // 5 saniye cooldown
                    player.sendMessage("§aKlan alanı genişletildi! Yeni radius: §e" + territory.getRadius() + " blok");
                    lastTerritoryExpandTime.put(player.getUniqueId(), now);
                }
            }
        }
    }
    
    /**
     * YENİ: Çit kırma event'i - TerritoryData güncelle
     * OPTİMİZE: Sadece o blokta TerritoryData'sı olan klanları kontrol et
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFenceBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        // Material kontrolü
        if (block.getType() != Material.OAK_FENCE) {
            return;
        }
        
        if (boundaryManager == null) return;
        
        // OPTİMİZE: Önce metadata kontrolü (hızlı filtreleme)
        boolean hasMetadata = false;
        if (territoryConfig != null) {
            String metadataKey = territoryConfig.getFenceMetadataKey();
            hasMetadata = block.hasMetadata(metadataKey);
            // Metadata yoksa ama TerritoryData'da varsa da kontrol et (server restart sonrası)
        }
        
        // OPTİMİZE: Sadece bu blokta TerritoryData'sı olan klanları kontrol et
        // Blok konumuna yakın klanları bul
        org.bukkit.Location blockLoc = block.getLocation();
        Clan nearbyClan = territoryManager.getTerritoryOwner(blockLoc);
        
        if (nearbyClan != null) {
            // Bu blok bir klan alanında, o klanın TerritoryData'sını kontrol et
            me.mami.stratocraft.model.territory.TerritoryData data = boundaryManager.getTerritoryData(nearbyClan);
            if (data != null) {
                List<org.bukkit.Location> fenceLocs = data.getFenceLocations();
                for (org.bukkit.Location fenceLoc : fenceLocs) {
                    if (fenceLoc.getWorld().equals(blockLoc.getWorld()) &&
                        fenceLoc.getBlockX() == blockLoc.getBlockX() &&
                        fenceLoc.getBlockY() == blockLoc.getBlockY() &&
                        fenceLoc.getBlockZ() == blockLoc.getBlockZ()) {
                        // Bu klanın çiti
                        boundaryManager.removeFenceLocation(nearbyClan, blockLoc);
                        return; // Bulundu, çık
                    }
                }
            }
        }
        
        // Metadata varsa ama TerritoryData'da bulunamadıysa, tüm klanları tara (fallback)
        // Bu nadiren olur ama server restart sonrası metadata kaybolabilir
        if (hasMetadata || !territoryConfig.isRequireClanFenceItem()) {
            // Fallback: Tüm klanları tara (yavaş ama nadiren çalışır)
            for (Clan clan : territoryManager.getClanManager().getAllClans()) {
                me.mami.stratocraft.model.territory.TerritoryData data = boundaryManager.getTerritoryData(clan);
                if (data != null) {
                    List<org.bukkit.Location> fenceLocs = data.getFenceLocations();
                    for (org.bukkit.Location fenceLoc : fenceLocs) {
                        if (fenceLoc.getWorld().equals(blockLoc.getWorld()) &&
                            fenceLoc.getBlockX() == blockLoc.getBlockX() &&
                            fenceLoc.getBlockY() == blockLoc.getBlockY() &&
                            fenceLoc.getBlockZ() == blockLoc.getBlockZ()) {
                            // Bu klanın çiti
                            boundaryManager.removeFenceLocation(clan, blockLoc);
                            return; // Bulundu, çık
                        }
                    }
                }
            }
        }
    }
    
    // Genişletme mesajı cooldown
    private final Map<UUID, Long> lastTerritoryExpandTime = new HashMap<>();
    
    // ========== KLAN ALANINDA BLOK YERLEŞTİRME KORUMASI ==========
    
    /**
     * Klan alanında blok yerleştirme kontrolü
     * Sadece klan üyeleri, misafirler ve kuşatma durumunda saldıran klan yerleştirebilir
     * NOT: Çit ve End Crystal için özel metodlar var, bu metod onlardan sonra çalışır
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlaceInTerritory(BlockPlaceEvent event) {
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(event.getPlayer())) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }
        
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Location blockLoc = block.getLocation();
        
        // Çit ve End Crystal için özel kontroller var, burada kontrol etme
        if (block.getType() == Material.OAK_FENCE || block.getType() == Material.END_CRYSTAL) {
            return; // Özel metodlar zaten kontrol ediyor
        }
        
        // Bölge sahibi kontrolü
        Clan owner = territoryManager.getTerritoryOwner(blockLoc);
        if (owner == null) return; // Sahipsiz yerse yerleştirilebilir
        
        // Ölümsüz klan önleme: Kristal yoksa bölge koruması yok
        if (!owner.hasCrystal()) {
            return; // Kristal yoksa koruma yok
        }
        
        // Kendi yerinse yerleştirilebilir (Rütbe kontrolü dahil)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan != null && playerClan.equals(owner)) {
            // YENİ: Recruit blok yerleştiremez
            if (playerClan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                player.sendMessage("§cAcemilerin blok yerleştirme yetkisi yok!");
                return;
            }
            return; // Yetkisi varsa yerleştirebilir
        }
        
        // Misafir İzni (Guest)
        if (owner.isGuest(player.getUniqueId())) {
            return; // Misafir yerleştirebilir
        }
        
        // ✅ YENİ: Savaş kontrolü - Sadece bu oyuncunun klanıyla savaşta ise yerleştirebilir
        if (playerClan != null && owner.isAtWarWith(playerClan.getId())) {
            return; // Bu klanla savaşta ise yerleştirebilir
        }
        
        // Engelle - Düşman klan alanında blok yerleştirme yasak
        event.setCancelled(true);
        player.sendMessage("§cBu bölge " + owner.getName() + " klanına ait! Önce kuşatma başlatmalısın.");
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onFuelAdd(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.BEACON) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.COAL && event.getItem().getType() != Material.CHARCOAL) return;

        Clan owner = territoryManager.getTerritoryOwner(event.getClickedBlock().getLocation());
        if (owner == null) return;

        Structure core = owner.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .findFirst().orElse(null);

        if (core == null) return;

        core.addFuel(10);
        event.getPlayer().sendMessage("§aKalkan Yakıtı Eklendi. Seviye: " + core.getShieldFuel());
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
    
    // ========== KLAN KRISTALİ KURMA ==========
    
    /**
     * Normal End Crystal yerleştirme engelleme
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onNormalEndCrystalPlace(BlockPlaceEvent event) {
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(event.getPlayer())) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }
        
        // Sadece End Crystal kontrol et
        if (event.getBlockPlaced().getType() != Material.END_CRYSTAL) {
            return;
        }
        
        // Klan kristali item kontrolü
        ItemStack item = event.getItemInHand();
        boolean isClanCrystal = false;
        
        if (territoryConfig != null && territoryConfig.isRequireClanCrystalItem()) {
            if (item != null && ItemManager.isClanItem(item, "CRYSTAL")) {
                isClanCrystal = true;
            }
        } else {
            // Config kapalıysa eski kontrol
            if (item != null && ItemManager.isClanItem(item, "CRYSTAL")) {
                isClanCrystal = true;
            }
        }
        
        // Normal End Crystal yerleştirme engelle
        if (!isClanCrystal) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cKlan alanında sadece §bKlan Kristali §cyerleştirilebilir!");
            event.getPlayer().sendMessage("§7Normal End Crystal kabul edilmez. Klan Kristali craft edin.");
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalPlace(PlayerInteractEvent event) {
        // Sadece sağ tıklama ve blok yüzeyine koyma işlemi
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        // YENİ: Klan kristali item kontrolü (config'den)
        if (territoryConfig != null && territoryConfig.isRequireClanCrystalItem()) {
            if (!ItemManager.isClanItem(event.getItem(), "CRYSTAL")) {
                return; // Normal End Crystal, klan kristali değil
            }
        } else {
            // Config kapalıysa eski kontrol
            if (!ItemManager.isClanItem(event.getItem(), "CRYSTAL")) return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        // Kristal havaya değil yere konur, üstü boş olmalı
        Block placeLocation = clickedBlock.getRelative(BlockFace.UP);
        if (placeLocation.getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        
        Player player = event.getPlayer();
        
        // Oyuncunun zaten klanı var mı?
        if (territoryManager.getClanManager().getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cZaten bir klanın var! Yeni kurmak için eskisini terk etmelisin.");
            event.setCancelled(true);
            return;
        }
        
        // --- ALAN KONTROLÜ (FENCE CHECK) - ASYNC ---
        event.setCancelled(true); // Önce iptal et, async kontrol sonrası devam edeceğiz
        
        // Async flood-fill kontrolü (büyük alanlar için main thread'i kilitlememek için)
        Player finalPlayer = player;
        Block finalPlaceLocation = placeLocation;
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
            me.mami.stratocraft.Main.getInstance(),
            () -> {
                boolean isValid = isSurroundedByClanFences(finalPlaceLocation);
                
                // Main thread'e geri dön
                org.bukkit.Bukkit.getScheduler().runTask(
                    me.mami.stratocraft.Main.getInstance(),
                    () -> {
                        if (!isValid) {
                            finalPlayer.sendMessage("§cKlan Kristali sadece §6Klan Çitleri §cile tamamen çevrelenmiş güvenli bir alana kurulabilir!");
                            return;
                        }
                        
                        // --- KLAN KURULUMU ---
                        continueCrystalPlacement(finalPlayer, finalPlaceLocation);
                    }
                );
            }
        );
        
        return; // Async işlem başladı, buradan çık
    }
    
    /**
     * Admin komutu için klan kurulumu başlat (public metod)
     * ✅ Çitler zaten createClanAdmin'de oluşturuldu, burada sadece chat input bekliyoruz
     */
    public void startAdminClanCreation(Player player, Location crystalLoc, org.bukkit.entity.EnderCrystal crystalEntity, Block placeLocation) {
        // Chat input için beklet
        waitingForClanName.put(player.getUniqueId(), new PendingClanCreation(crystalLoc, crystalEntity, placeLocation));
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§lKLAN KURULUYOR!");
        player.sendMessage("§7Çitler ve kristal oluşturuldu!");
        player.sendMessage("§7Lütfen chat'e klan ismini yazın:");
        player.sendMessage("§7(İptal için 'iptal' yazın)");
        player.sendMessage("§6§l════════════════════════════");
        // ✅ Çitler zaten createClanAdmin'de oluşturuldu, burada sadece chat input bekliyoruz
        // Çitler korunacak çünkü zaten dünyada var
    }
    
    /**
     * Kristal yerleştirme işlemini tamamla (main thread'de)
     */
    private void continueCrystalPlacement(Player player, Block placeLocation) {
        // Eşyayı tüket
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Kristal Entity'sini oluştur
        Location crystalLoc = placeLocation.getLocation().add(0.5, 0, 0.5);
        EnderCrystal crystalEntity = (EnderCrystal) crystalLoc.getWorld().spawnEntity(crystalLoc, EntityType.ENDER_CRYSTAL);
        crystalEntity.setShowingBottom(true); // Tabanı görünsün
        crystalEntity.setBeamTarget(null);
        
        // YENİ: Metadata ekle (klan kristali işaretleme)
        if (territoryConfig != null) {
            String metadataKey = territoryConfig.getCrystalMetadataKey();
            crystalEntity.setMetadata(metadataKey, new org.bukkit.metadata.FixedMetadataValue(
                me.mami.stratocraft.Main.getInstance(), true));
        }
        
        // Chat input için beklet
        waitingForClanName.put(player.getUniqueId(), new PendingClanCreation(crystalLoc, crystalEntity, placeLocation));
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§lKLAN KURULUYOR!");
        player.sendMessage("§7Lütfen chat'e klan ismini yazın:");
        player.sendMessage("§7(İptal için 'iptal' yazın)");
        player.sendMessage("§6§l════════════════════════════");
    }
    
    /**
     * Chat input ile klan ismi al
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChatInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingClanCreation pending = waitingForClanName.get(player.getUniqueId());
        
        if (pending == null) return; // Bu oyuncu klan kurmuyor
        
        event.setCancelled(true); // Chat mesajını iptal et
        
        String message = event.getMessage().trim();
        
        // İptal kontrolü
        if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
            waitingForClanName.remove(player.getUniqueId());
            pending.crystalEntity.remove(); // Kristali kaldır
            player.sendMessage("§cKlan kurma iptal edildi.");
            return;
        }
        
        // İsim validasyonu
        if (message.length() < 3 || message.length() > 16) {
            player.sendMessage("§cKlan ismi 3-16 karakter arasında olmalı!");
            return;
        }
        
        // Özel karakter kontrolü
        if (!message.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage("§cKlan ismi sadece harf, rakam ve alt çizgi içerebilir!");
            return;
        }
        
        // Aynı isimde klan var mı?
        boolean nameExists = territoryManager.getClanManager().getAllClans().stream()
            .anyMatch(c -> c.getName().equalsIgnoreCase(message));
        
        if (nameExists) {
            player.sendMessage("§cBu isimde bir klan zaten var!");
            return;
        }
        
        // ⚠️ YENİ: Kristal kontrolü (kırılmış mı?)
        if (pending.crystalEntity == null || pending.crystalEntity.isDead() || 
            !pending.crystalEntity.isValid()) {
            waitingForClanName.remove(player.getUniqueId());
            player.sendMessage("§cKlan Kristali sağlam değil! Klan oluşturma iptal edildi.");
            return;
        }
        
        // Kristal konumu kontrolü
        if (pending.crystalLoc == null || pending.crystalLoc.getWorld() == null) {
            waitingForClanName.remove(player.getUniqueId());
            player.sendMessage("§cKlan Kristali konumu geçersiz! Klan oluşturma iptal edildi.");
            return;
        }
        
        // Main thread'de klanı oluştur
        org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            // ⚠️ YENİ: Tekrar kristal kontrolü (main thread'de)
            if (pending.crystalEntity == null || pending.crystalEntity.isDead() || 
                !pending.crystalEntity.isValid()) {
                waitingForClanName.remove(player.getUniqueId());
                player.sendMessage("§cKlan Kristali sağlam değil! Klan oluşturma iptal edildi.");
                return;
            }
            
            Clan newClan = territoryManager.getClanManager().createClan(message, player.getUniqueId());
            if (newClan != null) {
                newClan.setCrystalLocation(pending.crystalLoc);
                newClan.setCrystalEntity(pending.crystalEntity);
                
                // YENİ: Metadata ekle (klan kristali işaretleme)
                if (territoryConfig != null && pending.crystalEntity != null) {
                    String metadataKey = territoryConfig.getCrystalMetadataKey();
                    pending.crystalEntity.setMetadata(metadataKey, new org.bukkit.metadata.FixedMetadataValue(
                        me.mami.stratocraft.Main.getInstance(), true));
                }
                
                // Minimum sınır ile Territory oluştur (50 blok radius)
                Territory territory = new Territory(newClan.getId(), pending.crystalLoc);
                // Territory constructor'ında zaten radius = 50, ama emin olmak için:
                if (territory.getRadius() < 50) {
                    territory.expand(50 - territory.getRadius());
                }
                newClan.setTerritory(territory);
                newClan.setHasCrystal(true); // Kristal var
                
                // YENİ: TerritoryData oluştur ve çit lokasyonlarını ekle
                if (boundaryManager != null && territoryConfig != null) {
                    me.mami.stratocraft.model.territory.TerritoryData territoryData = 
                        new me.mami.stratocraft.model.territory.TerritoryData(newClan.getId(), pending.crystalLoc);
                    territoryData.setRadius(territory.getRadius());
                    territoryData.setSkyHeight(territoryConfig.getSkyHeight());
                    territoryData.setGroundDepth(territoryConfig.getGroundDepth());
                    
                    // Çit lokasyonlarını bul ve ekle (ASYNC - büyük alanlar için)
                    // Async olarak çit lokasyonlarını bul
                    final me.mami.stratocraft.model.Clan finalNewClan = newClan;
                    org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                        me.mami.stratocraft.Main.getInstance(),
                        () -> {
                            findAndAddFenceLocations(pending.placeLocation.getLocation(), territoryData);
                            
                            // Main thread'e geri dön ve TerritoryData'yı kaydet
                            org.bukkit.Bukkit.getScheduler().runTask(
                                me.mami.stratocraft.Main.getInstance(),
                                () -> {
                                    if (boundaryManager != null) {
                                        boundaryManager.setTerritoryData(finalNewClan, territoryData);
                                    }
                                }
                            );
                        }
                    );
                }
                
                territoryManager.setCacheDirty(); // Cache'i güncelle
                
                player.sendMessage("§a§lTEBRİKLER! §eKlan Kristali aktifleşti ve bölgeni mühürledi.");
                player.getWorld().strikeLightningEffect(pending.crystalLoc); // Görsel şimşek
                player.getWorld().spawnParticle(Particle.TOTEM, pending.crystalLoc, 100, 1, 1, 1, 0.5);
                player.playSound(pending.crystalLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                player.sendTitle("§6§lKLAN KURULDU", "§e" + message, 10, 70, 20);
            } else {
                player.sendMessage("§cKlan oluşturulamadı! Zaten bir klanın olabilir.");
                pending.crystalEntity.remove(); // Kristali kaldır
            }
            
            waitingForClanName.remove(player.getUniqueId());
        });
    }
    
    // Partikül cooldown (performans için)
    private final Map<UUID, Long> lastBoundaryParticleTime = new HashMap<>();
    private static final long BOUNDARY_PARTICLE_COOLDOWN = 1000L; // 1 saniye
    
    /**
     * Klan sınırlarını partikül ile göster (klan üyelerine)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // PERFORMANS: Sadece blok değiştiyse çalış
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;
        
        // Cooldown kontrolü
        long now = System.currentTimeMillis();
        Long lastTime = lastBoundaryParticleTime.get(player.getUniqueId());
        if (lastTime != null && (now - lastTime) < BOUNDARY_PARTICLE_COOLDOWN) {
            return; // Cooldown'da
        }
        
        // Oyuncunun klanını kontrol et
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return; // Klan üyesi değil
        
        Territory territory = playerClan.getTerritory();
        if (territory == null || territory.getCenter() == null) return;
        
        // Oyuncu kendi klanının sınırına yakın mı? (10 blok mesafe)
        Location center = territory.getCenter();
        double distanceToCenter = center.distance(to);
        double radius = territory.getRadius();
        double distanceToBoundary = Math.abs(distanceToCenter - radius);
        
        // Sınırın 10 blok yakınındaysa partikül göster
        if (distanceToBoundary <= 10) {
            // Sınır çizgisini göster (her 2 blokta bir partikül)
            showTerritoryBoundary(player, territory, to);
            lastBoundaryParticleTime.put(player.getUniqueId(), now);
        }
    }
    
    /**
     * Klan sınırını partikül ile göster
     */
    private void showTerritoryBoundary(Player player, Territory territory, Location playerLoc) {
        Location center = territory.getCenter();
        if (center == null || center.getWorld() == null) return;
        if (!center.getWorld().equals(playerLoc.getWorld())) return;
        
        double radius = territory.getRadius();
        double angle = Math.atan2(playerLoc.getZ() - center.getZ(), playerLoc.getX() - center.getX());
        
        // Sınır çizgisinde birkaç noktada partikül göster
        for (int i = -2; i <= 2; i++) {
            double offsetAngle = angle + (i * 0.3); // Her 0.3 radyan (yaklaşık 17 derece)
            double x = center.getX() + (radius * Math.cos(offsetAngle));
            double z = center.getZ() + (radius * Math.sin(offsetAngle));
            
            // Yükseklik: Oyuncunun göz seviyesi ± 2 blok
            double y = playerLoc.getY() + (i * 0.5);
            
            Location particleLoc = new Location(center.getWorld(), x, y, z);
            
            // Mesafe kontrolü (performans)
            if (playerLoc.distance(particleLoc) > 20) continue;
            
            // Klan rengine göre partikül (yeşil - kendi klanı)
            player.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0f)); // Yeşil
        }
    }
    
    // Flood Fill Algoritması ile Çit Kontrolü
    private boolean isSurroundedByClanFences(Block center) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(center);
        visited.add(center);
        
        int minArea = 9; // Minimum alan büyüklüğü (3x3 = 9 blok)
        int iterations = 0;
        int maxIterations = 5000; // Maksimum iterasyon limiti (lag önleme)
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            // Maksimum limit kontrolü (lag önleme)
            if (iterations > maxIterations) {
                // Çok büyük alan, güvenlik için false döndür
                return false;
            }
            
            // 4 Yöne bak (Kuzey, Güney, Doğu, Batı)
            Block[] neighbors = {
                current.getRelative(BlockFace.NORTH),
                current.getRelative(BlockFace.SOUTH),
                current.getRelative(BlockFace.EAST),
                current.getRelative(BlockFace.WEST)
            };
            
            for (Block neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;
                
                // Eğer blok bizim Klan Çiti ise, burası sınırdır
                // Metadata kontrolü yap (normal çitler kabul edilmez)
                if (neighbor.getType() == Material.OAK_FENCE) {
                    // Metadata kontrolü - sadece klan çitleri kabul et
                    boolean isClanFence = false;
                    if (territoryConfig != null) {
                        String metadataKey = territoryConfig.getFenceMetadataKey();
                        isClanFence = neighbor.hasMetadata(metadataKey);
                    }
                    
                    // Metadata yoksa TerritoryData'dan kontrol et (server restart sonrası)
                    if (!isClanFence && boundaryManager != null) {
                        // Bu blok konumuna yakın klanları kontrol et
                        Clan nearbyClan = territoryManager.getTerritoryOwner(neighbor.getLocation());
                        if (nearbyClan != null) {
                            me.mami.stratocraft.model.territory.TerritoryData data = boundaryManager.getTerritoryData(nearbyClan);
                            if (data != null) {
                                for (org.bukkit.Location fenceLoc : data.getFenceLocations()) {
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
                    }
                    
                    if (isClanFence) {
                        // Sınır bulundu (klan çiti), bu yöne devam etme
                        continue;
                    } else {
                        // Normal çit, sınır sayılmaz - engel olarak kabul et
                        continue;
                    }
                }
                
                // Eğer hava değilse ve çit de değilse, engel say
                if (neighbor.getType() != Material.AIR) {
                    // Engel var, sınır kabul et
                    continue;
                }
                
                // Eğer boşluksa ve sınır değilse, kuyruğa ekle
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
        
        // Minimum alan kontrolü: En az 3x3 alan olmalı
        if (visited.size() < minArea) {
            return false;
        }
        
        // Eğer döngü limit aşılmadan bittiyse ve minimum şartları sağlıyorsa, kapalı bir alandır
        return true;
    }
    
    // ========== KLAN KRISTALİ KIRILMA ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalBreak(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        
        // ⚠️ YENİ: Pending klan oluşturma var mı? (Kristal kırılma kontrolü)
        for (Map.Entry<UUID, PendingClanCreation> entry : waitingForClanName.entrySet()) {
            if (entry.getValue().crystalEntity != null && 
                entry.getValue().crystalEntity.equals(crystal)) {
                // Kristal kırıldı, pending'i temizle
                UUID playerId = entry.getKey();
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage("§cKlan Kristali kırıldı! Klan oluşturma iptal edildi.");
                }
                waitingForClanName.remove(playerId);
                break;
            }
        }
        
        // Bu kristal bir klan kristali mi?
        Clan owner = findClanByCrystal(crystal);
        if (owner == null) return; // Normal end crystal
        
        // Kristal kırılıyor mu? (EnderCrystal'ın sağlığı 1.0, yeterli hasar aldığında kırılır)
        // EnderCrystal'da getHealth() yok ama hasar >= 1.0 olduğunda kırılır
        if (event.getFinalDamage() >= 1.0 && !event.isCancelled()) {
            // Kırılma nedenini kontrol et
            Player breaker = null;
            Entity damager = null;
            if (event instanceof EntityDamageByEntityEvent) {
                damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    breaker = (Player) damager;
                }
            }
            
            // Felaket entity'si kristali kırıyor mu? (DisasterTask'tan geliyor)
            // Felaket entity'leri için özel durum - klanı dağıt
            if (damager != null && !(damager instanceof Player)) {
                // Bu bir felaket entity'si olabilir - DisasterTask zaten klanı dağıtacak
                // Burada sadece event'i işle, klan dağıtma DisasterTask'ta yapılıyor
                return; // DisasterTask zaten işleyecek
            }
            
            // Lider kendi kristalini kırıyor mu?
            if (breaker != null && owner.getRank(breaker.getUniqueId()) == Clan.Rank.LEADER) {
                // YENİ: Klan alanı korumasını kaldır ve sınırları temizle
                owner.setCrystalLocation(null);
                if (boundaryManager != null) {
                    boundaryManager.removeTerritoryData(owner);
                }
                
                // Lider klanı bozdu
                territoryManager.getClanManager().disbandClan(owner);
                territoryManager.setCacheDirty(); // Cache'i güncelle
                breaker.sendMessage("§cKlanınız dağıtıldı!");
                crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                return;
            }
            
            // ✅ YENİ: Kuşatma var mı? - Sadece bu oyuncunun klanıyla savaşta ise
            if (breaker != null) {
                Clan attacker = territoryManager.getClanManager().getClanByPlayer(breaker.getUniqueId());
                if (attacker != null && !attacker.equals(owner) && owner.isAtWarWith(attacker.getId())) {
                    // Savaşta kristal kırıldı - klan bozuldu
                    siegeManager.endSiege(attacker, owner);
                    breaker.sendMessage("§6§lZAFER! Düşman kristalini parçaladın.");
                // YENİ: Klan alanı korumasını kaldır ve sınırları temizle
                owner.setCrystalLocation(null);
                if (boundaryManager != null) {
                    boundaryManager.removeTerritoryData(owner);
                }
                
                territoryManager.getClanManager().disbandClan(owner);
                territoryManager.setCacheDirty(); // Cache'i güncelle
                crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                return;
            }
            
            // Normal durumda sadece lider kırabilir
            if (breaker != null) {
                Clan playerClan = territoryManager.getClanManager().getClanByPlayer(breaker.getUniqueId());
                if (playerClan == null || !playerClan.equals(owner) || owner.getRank(breaker.getUniqueId()) != Clan.Rank.LEADER) {
                    event.setCancelled(true);
                    breaker.sendMessage("§cKlan Kristalini sadece klan lideri kırabilir!");
                    return;
                }
            } else {
                // Doğal hasar (lava, patlama vb.) - engelle
                event.setCancelled(true);
            }
        }
    }
    
    // Kristal entity'sine göre klanı bul
    private Clan findClanByCrystal(EnderCrystal crystal) {
        // YENİ: Metadata kontrolü (klan kristali mi?)
        if (territoryConfig != null) {
            String metadataKey = territoryConfig.getCrystalMetadataKey();
            if (!crystal.hasMetadata(metadataKey)) {
                return null; // Normal End Crystal, klan kristali değil
            }
        }
        
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (clan.getCrystalEntity() != null && clan.getCrystalEntity().equals(crystal)) {
                return clan;
            }
        }
        return null;
    }
    
    // ========== KLAN KRISTALİ HAREKET ETTİRME ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalMove(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // ÖNCE: Oyuncunun klanı var mı ve kristal var mı kontrol et
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(playerId);
        if (playerClan == null) return; // Oyuncunun klanı yok
        
        // Kristal var mı?
        if (playerClan.getCrystalLocation() == null || playerClan.getCrystalEntity() == null) {
            return; // Kristal yok, devam etme
        }
        
        EnderCrystal crystal = playerClan.getCrystalEntity();
        Location crystalLoc = crystal.getLocation();
        
        // ÖNCE: Oyuncu kristale yakın mı? (5 blok mesafe) - Bu kontrol önce yapılmalı
        double distance = player.getLocation().distance(crystalLoc);
        if (distance > 5) {
            // Kristale yakın değilse hiçbir şey yapma
            return;
        }
        
        // Şimdi shift+sağ tık ve elinde boş mu kontrolü (sadece kristale yakınsa)
        if (!player.isSneaking()) return;
        
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem != null && handItem.getType() != Material.AIR) return;
        
        // Lider kontrolü
        if (playerClan.getRank(playerId) != Clan.Rank.LEADER) {
            return; // Lider değil, devam etme
        }
        
        // Zaten bir taşıma işlemi devam ediyor mu? (spam önleme)
        if (activeCrystalMoveTasks.containsKey(playerId)) {
            org.bukkit.scheduler.BukkitTask existingTask = activeCrystalMoveTasks.get(playerId);
            if (existingTask != null && !existingTask.isCancelled()) {
                player.sendMessage("§cLütfen bekleyin, önceki taşıma işlemi tamamlanıyor...");
                event.setCancelled(true);
                return;
            } else {
                // Eski task iptal edilmiş, temizle
                activeCrystalMoveTasks.remove(playerId);
            }
        }
        
        Block clicked = event.getClickedBlock();
        Block newLocation = clicked.getRelative(BlockFace.UP);
        
        // Yeni konum boş mu?
        if (newLocation.getType() != Material.AIR) {
            player.sendMessage("§cYeni konum boş olmalı!");
            event.setCancelled(true);
            return;
        }
        
        // Aynı konuma taşıma kontrolü
        Location newLoc = newLocation.getLocation().add(0.5, 0, 0.5);
        if (crystalLoc.getBlockX() == newLoc.getBlockX() && 
            crystalLoc.getBlockY() == newLoc.getBlockY() && 
            crystalLoc.getBlockZ() == newLoc.getBlockZ()) {
            player.sendMessage("§cKristal zaten bu konumda!");
            event.setCancelled(true);
            return;
        }
        
        // Async çit kontrolü (büyük alanlar için lag önleme)
        event.setCancelled(true);
        
        // Task'ı kaydet ve iptal edilebilir hale getir
        org.bukkit.scheduler.BukkitTask asyncTask = org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
            me.mami.stratocraft.Main.getInstance(),
            () -> {
                // Plugin kapanıyor mu kontrol et
                if (!me.mami.stratocraft.Main.getInstance().isEnabled()) {
                    return;
                }
                
                boolean isValid = isSurroundedByClanFences(newLocation);
                
                // Main thread'e geri dön
                org.bukkit.scheduler.BukkitTask syncTask = org.bukkit.Bukkit.getScheduler().runTask(
                    me.mami.stratocraft.Main.getInstance(),
                    () -> {
                        // Task'ı temizle
                        activeCrystalMoveTasks.remove(playerId);
                        
                        // Plugin kapanıyor mu kontrol et
                        if (!me.mami.stratocraft.Main.getInstance().isEnabled()) {
                            return;
                        }
                        
                        // Oyuncu hala online mı?
                        Player finalPlayer = org.bukkit.Bukkit.getPlayer(playerId);
                        if (finalPlayer == null || !finalPlayer.isOnline()) {
                            return;
                        }
                        
                        // Kristal hala var mı?
                        if (playerClan.getCrystalEntity() == null || !playerClan.getCrystalEntity().equals(crystal)) {
                            return;
                        }
                        
                        if (!isValid) {
                            finalPlayer.sendMessage("§cYeni konum Klan Çitleri ile çevrili olmalı!");
                            return;
                        }
                        
                        // Kristali taşı
                        crystal.teleport(newLoc);
                        playerClan.setCrystalLocation(newLoc);
                        playerClan.setCrystalEntity(crystal); // Entity referansını güncelle
                        
                        // Minimum sınır ile Territory oluştur (50 blok radius)
                        Territory territory = new Territory(playerClan.getId(), newLoc);
                        if (territory.getRadius() < 50) {
                            territory.expand(50 - territory.getRadius());
                        }
                        playerClan.setTerritory(territory);
                        territoryManager.setCacheDirty(); // Cache'i güncelle
                        
                        // Efektler
                        finalPlayer.getWorld().spawnParticle(Particle.TOTEM, newLoc, 50, 0.5, 0.5, 0.5, 0.1);
                        finalPlayer.playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
                        
                        finalPlayer.sendMessage("§a§lKlan Kristali taşındı!");
                    }
                );
                
                // Sync task'ı da kaydet (eğer iptal edilmesi gerekirse)
                if (syncTask != null) {
                    activeCrystalMoveTasks.put(playerId, syncTask);
                }
            }
        );
        
        // Async task'ı kaydet
        if (asyncTask != null) {
            activeCrystalMoveTasks.put(playerId, asyncTask);
        }
    }
    
    /**
     * Tüm aktif kristal taşıma task'larını iptal et (plugin kapanırken)
     */
    public void cancelAllCrystalMoveTasks() {
        for (org.bukkit.scheduler.BukkitTask task : activeCrystalMoveTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeCrystalMoveTasks.clear();
    }
    
    /**
     * Çit lokasyonlarını bul ve TerritoryData'ya ekle
     * Flood fill algoritması ile çitleri tespit eder
     */
    private void findAndAddFenceLocations(Location centerLocation, me.mami.stratocraft.model.territory.TerritoryData territoryData) {
        if (centerLocation == null || centerLocation.getWorld() == null || territoryData == null) {
            return;
        }
        
        org.bukkit.World world = centerLocation.getWorld();
        int centerX = centerLocation.getBlockX();
        int centerY = centerLocation.getBlockY();
        int centerZ = centerLocation.getBlockZ();
        
        // Flood fill ile çitleri bul
        java.util.Set<org.bukkit.Location> visited = new java.util.HashSet<>();
        java.util.Queue<org.bukkit.block.Block> queue = new java.util.LinkedList<>();
        
        // Başlangıç noktası
        org.bukkit.block.Block startBlock = world.getBlockAt(centerX, centerY, centerZ);
        queue.add(startBlock);
        visited.add(startBlock.getLocation());
        
        int maxIterations = 50000; // Büyük alanlar için limit
        int iterations = 0;
        
        while (!queue.isEmpty() && iterations < maxIterations) {
            org.bukkit.block.Block current = queue.poll();
            iterations++;
            
            // 4 yöne bak
            org.bukkit.block.Block[] neighbors = {
                current.getRelative(org.bukkit.block.BlockFace.NORTH),
                current.getRelative(org.bukkit.block.BlockFace.SOUTH),
                current.getRelative(org.bukkit.block.BlockFace.EAST),
                current.getRelative(org.bukkit.block.BlockFace.WEST)
            };
            
            for (org.bukkit.block.Block neighbor : neighbors) {
                if (neighbor == null || neighbor.getWorld() == null) continue;
                
                org.bukkit.Location neighborLoc = neighbor.getLocation();
                if (visited.contains(neighborLoc)) continue;
                
                // Çit kontrolü - Metadata ile klan çiti kontrolü
                if (neighbor.getType() == Material.OAK_FENCE) {
                    boolean isClanFence = false;
                    if (territoryConfig != null) {
                        String metadataKey = territoryConfig.getFenceMetadataKey();
                        isClanFence = neighbor.hasMetadata(metadataKey);
                    }
                    
                    // Metadata yoksa TerritoryData'dan kontrol et
                    if (!isClanFence) {
                        for (org.bukkit.Location fenceLoc : territoryData.getFenceLocations()) {
                            if (fenceLoc.getWorld().equals(neighbor.getWorld()) &&
                                fenceLoc.getBlockX() == neighbor.getX() &&
                                fenceLoc.getBlockY() == neighbor.getY() &&
                                fenceLoc.getBlockZ() == neighbor.getZ()) {
                                isClanFence = true;
                                break;
                            }
                        }
                    }
                    
                    if (isClanFence) {
                        // Klan çiti bulundu, TerritoryData'ya ekle (eğer yoksa)
                        territoryData.addFenceLocation(neighborLoc);
                        visited.add(neighborLoc);
                        continue; // Sınır, devam etme
                    } else {
                        // Normal çit, sınır sayılmaz - engel olarak kabul et
                        continue;
                    }
                }
                
                // Hava veya geçilebilir blok
                if (neighbor.getType().isAir() || !neighbor.getType().isSolid()) {
                    visited.add(neighborLoc);
                    queue.add(neighbor);
                }
            }
        }
    }
}

