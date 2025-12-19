package me.mami.stratocraft.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Territory;

public class TerritoryListener implements Listener {
    private final TerritoryManager territoryManager;
    private final SiegeManager siegeManager;
    private me.mami.stratocraft.manager.TerritoryBoundaryManager boundaryManager;
    private me.mami.stratocraft.manager.config.TerritoryConfig territoryConfig;
    
    // Klan kurma için chat input sistemi
    private final Map<UUID, PendingClanCreation> waitingForClanName = new HashMap<>();
    
    // Klan kristali taşıma sistemi (Enderman tarzı)
    // Oyuncu UUID -> Taşıdığı kristal bilgisi
    private final Map<UUID, CarryingCrystalData> carryingCrystalPlayers = new java.util.concurrent.ConcurrentHashMap<>();
    
    // ✅ PERFORMANS: onPlayerMove için cache (5 saniye cache süresi)
    private final Map<UUID, CachedPlayerClanData> playerMoveCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long PLAYER_MOVE_CACHE_DURATION = 5000L; // 5 saniye
    
    /**
     * Player move cache data class
     */
    private static class CachedPlayerClanData {
        final UUID clanId;
        final long lastCheck;
        
        CachedPlayerClanData(UUID clanId, long lastCheck) {
            this.clanId = clanId;
            this.lastCheck = lastCheck;
        }
    }
    
    // Kristal taşıma verisi
    public static class CarryingCrystalData {
        public final Clan clan;
        public final Location originalLocation;
        public final EnderCrystal originalEntity; // null olabilir - kristal kaldırıldı
        
        public CarryingCrystalData(Clan clan, Location originalLocation, EnderCrystal originalEntity) {
            this.clan = clan;
            this.originalLocation = originalLocation;
            this.originalEntity = originalEntity;
        }
    }
    
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

    @EventHandler(priority = EventPriority.NORMAL) // ✅ DÜZELTME: Priority belirtildi (özel blok handler'ları HIGH priority'de)
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
        
        // ✅ DÜZELTME: Yapı çekirdeği korumasını kaldırdık
        // Özel blok handler'ları (StructureCoreListener, TrapListener, vb.) zaten var
        // ve doğru çalışıyor. Burada koruma yapmaya gerek yok.
        
        // Kendi yerinse kırılabilir (Rütbe kontrolü dahil)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        
        if (playerClan != null && playerClan.equals(owner)) {
            // ✅ YENİ: Rütbe Kontrolü - RECRUIT ve MEMBER blok kıramaz
            Clan.Rank rank = playerClan.getRank(event.getPlayer().getUniqueId());
            if (rank == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cAcemilerin blok kırma yetkisi yok!");
                return;
            }
            if (rank == Clan.Rank.MEMBER) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cÜyelerin blok kırma yetkisi yok!");
                return;
            }
            // ELITE, GENERAL, LEADER blok kırabilir
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
        
        // ✅ YENİ: Klan bankası kontrolü (PersistentDataContainer)
        Block block = invLocation.getBlock();
        if (me.mami.stratocraft.util.CustomBlockData.isClanBank(block)) {
            // Klan bankası özel kontrolü RitualInteractionListener'da yapılıyor
            return;
        }
        
        // ❌ ESKİ: Metadata kontrolü kaldırıldı
        // if (block.hasMetadata("ClanBank")) {
        //     return;
        // }
        
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
        
        // Oyuncunun klanı var mı?
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        
        // ✅ YENİ: PersistentDataContainer kullan (metadata yerine)
        if (playerClan != null) {
            // PersistentDataContainer'a kaydet
            me.mami.stratocraft.util.CustomBlockData.setClanFenceData(block, playerClan.getId());
        } else {
            // Klan yok ama çit yerleştirilebilir (sonra klan kurulabilir)
            // Geçici olarak null kaydet (sonra güncellenebilir)
            me.mami.stratocraft.util.CustomBlockData.setClanFenceData(block, null);
        }
        
        // ❌ ESKİ: Metadata kaldır
        // if (territoryConfig != null) {
        //     String metadataKey = territoryConfig.getFenceMetadataKey();
        //     block.setMetadata(metadataKey, new org.bukkit.metadata.FixedMetadataValue(
        //         me.mami.stratocraft.Main.getInstance(), true));
        // }
        
        if (playerClan == null) {
            return;
        }
        
        // ✅ İYİ: TerritoryData'ya ekle (backup)
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
     * ✅ DÜZELTME: Çit kırma event'i - Özel item drop et
     * PersistentDataContainer'dan veri oku ve drop edilen item'a ekle
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFenceBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        // Material kontrolü
        if (block.getType() != Material.OAK_FENCE) {
            return;
        }
        
        // ✅ PersistentDataContainer'dan veri oku
        UUID clanId = me.mami.stratocraft.util.CustomBlockData.getClanFenceData(block);
        if (clanId == null) {
            // Normal çit, işlem yok
            return;
        }
        
        // ✅ Normal drop'ları iptal et
        event.setDropItems(false);
        
        // ✅ Özel item oluştur (OAK_FENCE + PDC verisi)
        ItemStack clanFenceItem = new ItemStack(Material.OAK_FENCE);
        org.bukkit.inventory.meta.ItemMeta meta = clanFenceItem.getItemMeta();
        if (meta != null) {
            // Display name ve lore ekle (ItemManager.registerClanFenceRecipe() ile uyumlu)
            meta.setDisplayName("§6§lKlan Çiti");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7Klan bölgesi sınırlarını belirler.");
            meta.setLore(lore);
            
            // ✅ PDC verisini ekle
            org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_fence");
            container.set(key, org.bukkit.persistence.PersistentDataType.STRING, clanId.toString());
            
            // ✅ ItemManager.isClanItem() için custom_id ekle (ItemManager.registerClanFenceRecipe() ile uyumlu)
            org.bukkit.NamespacedKey customIdKey = new org.bukkit.NamespacedKey(me.mami.stratocraft.Main.getInstance(), "custom_id");
            container.set(customIdKey, org.bukkit.persistence.PersistentDataType.STRING, "CLAN_FENCE");
            
            clanFenceItem.setItemMeta(meta);
        }
        
        // ✅ Özel item'ı drop et
        block.getWorld().dropItemNaturally(block.getLocation(), clanFenceItem);
        
        // ✅ PERFORMANS: Cache temizleme ile birlikte veri silme
        me.mami.stratocraft.util.CustomBlockData.removeClanFenceData(block);
        
        // ✅ TerritoryData'dan kaldır (backup)
        if (boundaryManager != null) {
            Clan clan = territoryManager.getClanManager().getClan(clanId);
            if (clan != null) {
                boundaryManager.removeFenceLocation(clan, block.getLocation());
            }
        }
        
        // ✅ CustomBlockData'dan da temizle
        me.mami.stratocraft.util.CustomBlockData.removeClanFenceData(block);
    }
    
    /**
     * ✅ YENİ: BlockPlaceEvent'te ItemStack'ten veri geri yükleme
     * Tüm özel bloklar için ItemStack'ten veri oku ve bloka yaz
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCustomBlockPlaceRestore(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        Material type = block.getType();
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // ✅ Klan çiti kontrolü
        if (type == Material.OAK_FENCE) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_fence");
            if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                String clanIdStr = container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
                UUID clanId = UUID.fromString(clanIdStr);
                
                // ✅ Bloka veri yaz
                me.mami.stratocraft.util.CustomBlockData.setClanFenceData(block, clanId);
                
                // ✅ TerritoryData'ya ekle (backup)
                if (boundaryManager != null) {
                    Clan clan = territoryManager.getClanManager().getClan(clanId);
                    if (clan != null) {
                        boundaryManager.addFenceLocation(clan, block.getLocation());
                    }
                }
            }
        }
        
        // ✅ Yapı çekirdeği kontrolü
        if (type == Material.OAK_LOG) {
            org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey("stratocraft", "structure_core_owner");
            if (container.has(ownerKey, org.bukkit.persistence.PersistentDataType.STRING)) {
                String ownerIdStr = container.get(ownerKey, org.bukkit.persistence.PersistentDataType.STRING);
                UUID ownerId = UUID.fromString(ownerIdStr);
                
                // ✅ DÜZELTME: CustomBlockData kütüphanesi ile PDC kullan (OAK_LOG TileState değil ama artık çalışıyor)
                me.mami.stratocraft.util.CustomBlockData.setStructureCoreData(block, ownerId);
                
                // StructureCoreManager'a kaydet (memory'de tutulacak + PDC'ye de kaydediliyor)
                me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                if (mainPlugin != null && mainPlugin.getStructureCoreManager() != null) {
                    mainPlugin.getStructureCoreManager().addInactiveCore(block.getLocation(), ownerId);
                }
            }
        }
        
        // ✅ Tuzak çekirdeği kontrolü
        if (type == Material.LODESTONE) {
            org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey("stratocraft", "trap_core_owner");
            if (container.has(ownerKey, org.bukkit.persistence.PersistentDataType.STRING)) {
                String ownerIdStr = container.get(ownerKey, org.bukkit.persistence.PersistentDataType.STRING);
                UUID ownerId = UUID.fromString(ownerIdStr);
                
                // ✅ DÜZELTME: CustomBlockData kütüphanesi ile PDC kullan (LODESTONE TileState değil ama artık çalışıyor)
                me.mami.stratocraft.util.CustomBlockData.setTrapCoreData(block, ownerId);
                
                // TrapManager'a kaydet (memory'de tutulacak + PDC'ye de kaydediliyor)
                me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                if (mainPlugin != null && mainPlugin.getTrapManager() != null) {
                    mainPlugin.getTrapManager().registerInactiveTrapCore(block.getLocation(), ownerId);
                }
            }
        }
        
        // ✅ Klan bankası kontrolü
        if (type == Material.ENDER_CHEST) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_bank");
            if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                String clanIdStr = container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
                UUID clanId = UUID.fromString(clanIdStr);
                
                // ✅ Bloka veri yaz
                me.mami.stratocraft.util.CustomBlockData.setClanBankData(block, clanId);
                
                // ClanBankSystem'e kaydet
                me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                if (mainPlugin != null && mainPlugin.getClanBankSystem() != null) {
                    try {
                        java.lang.reflect.Field field = mainPlugin.getClanBankSystem().getClass()
                            .getDeclaredField("bankChestLocations");
                        field.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        java.util.Map<UUID, org.bukkit.Location> locations = 
                            (java.util.Map<UUID, org.bukkit.Location>) field.get(mainPlugin.getClanBankSystem());
                        if (locations != null) {
                            locations.put(clanId, block.getLocation());
                        }
                    } catch (Exception e) {
                        // Reflection hatası - önemli değil
                    }
                }
            }
        }
    }
    
    /**
     * ✅ YENİ: Yaratıcı modda orta tık ile kopyalama (pick block)
     * Tüm özel bloklar için PersistentDataContainer verisini ItemStack'e kopyala
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreativeCopy(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        Material type = block.getType();
        ItemStack item = new ItemStack(type);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean hasCustomData = false;
        
        // ✅ Klan çiti kontrolü
        if (type == Material.OAK_FENCE) {
            UUID clanId = me.mami.stratocraft.util.CustomBlockData.getClanFenceData(block);
            if (clanId != null) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_fence");
                container.set(key, org.bukkit.persistence.PersistentDataType.STRING, clanId.toString());
                hasCustomData = true;
            }
        }
        
        // ✅ Yapı çekirdeği kontrolü
        if (type == Material.OAK_LOG) {
            UUID ownerId = me.mami.stratocraft.util.CustomBlockData.getStructureCoreOwner(block);
            if (ownerId != null) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "structure_core");
                container.set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey("stratocraft", "structure_core_owner");
                container.set(ownerKey, org.bukkit.persistence.PersistentDataType.STRING, ownerId.toString());
                hasCustomData = true;
            }
        }
        
        // ✅ Tuzak çekirdeği kontrolü
        if (type == Material.LODESTONE) {
            UUID ownerId = me.mami.stratocraft.util.CustomBlockData.getTrapCoreOwner(block);
            if (ownerId != null) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "trap_core");
                container.set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey("stratocraft", "trap_core_owner");
                container.set(ownerKey, org.bukkit.persistence.PersistentDataType.STRING, ownerId.toString());
                hasCustomData = true;
            }
        }
        
        // ✅ Klan bankası kontrolü
        if (type == Material.ENDER_CHEST) {
            UUID clanId = me.mami.stratocraft.util.CustomBlockData.getClanBankData(block);
            if (clanId != null) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_bank");
                container.set(key, org.bukkit.persistence.PersistentDataType.STRING, clanId.toString());
                hasCustomData = true;
            }
        }
        
        // ✅ Özel blok verisi varsa ItemStack'e ekle
        if (hasCustomData) {
            item.setItemMeta(meta);
            event.getPlayer().getInventory().setItemInMainHand(item);
            event.setCancelled(true);
        }
    }
    
    /**
     * ✅ YENİ: Chunk yüklendiğinde özel blokları kontrol et
     * PersistentDataContainer otomatik yüklenir ama backup sistemler için kontrol edilmeli
     * ✅ DÜZELTME: Main thread'de çalışmalı (async thread'de chunk/blok işlemleri yapılamaz - sonsuz döngüye neden olur)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        // ✅ DÜZELTME: Main thread'de çalıştır (async thread'de chunk yükleme sonsuz döngüye neden olur)
        org.bukkit.Bukkit.getScheduler().runTask(
            me.mami.stratocraft.Main.getInstance(),
            () -> {
                org.bukkit.Chunk chunk = event.getChunk();
                if (chunk == null || !chunk.isLoaded()) {
                    return; // Chunk yüklü değilse atla
                }
                
                // ✅ Sadece özel blok tiplerini kontrol et (performans için)
                // OAK_FENCE, OAK_LOG, LODESTONE, ENDER_CHEST
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        // ✅ DÜZELTME: getHighestBlockYAt chunk yükleme tetikleyebilir, chunk yüklü mü kontrol et
                        if (!chunk.isLoaded()) {
                            continue; // Chunk yüklü değilse atla
                        }
                        
                        // Y eksenini optimize et: Sadece yüzeyden ±50 blok kontrol et
                        int centerY = chunk.getWorld().getHighestBlockYAt(
                            chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
                        int minY = Math.max(chunk.getWorld().getMinHeight(), centerY - 50);
                        int maxY = Math.min(chunk.getWorld().getMaxHeight(), centerY + 50);
                        
                        for (int y = minY; y <= maxY; y++) {
                            // ✅ DÜZELTME: Chunk yüklü mü kontrol et (sonsuz döngü önleme)
                            if (!chunk.isLoaded()) {
                                break; // Chunk yüklü değilse döngüden çık
                            }
                            
                            Block block = chunk.getBlock(x, y, z);
                            Material type = block.getType();
                            
                            // ✅ Klan çiti kontrolü
                            if (type == Material.OAK_FENCE) {
                                UUID clanId = me.mami.stratocraft.util.CustomBlockData.getClanFenceData(block);
                                if (clanId != null && boundaryManager != null) {
                                    // ✅ DÜZELTME: Artık main thread'deyiz, direkt çağır
                                    Clan clan = territoryManager.getClanManager().getClan(clanId);
                                    if (clan != null) {
                                        boundaryManager.addFenceLocation(clan, block.getLocation());
                                    }
                                }
                            }
                            
                            // ✅ Yapı çekirdeği kontrolü
                            if (type == Material.OAK_LOG) {
                                UUID ownerId = me.mami.stratocraft.util.CustomBlockData.getStructureCoreOwner(block);
                                if (ownerId != null) {
                                    // ✅ DÜZELTME: Artık main thread'deyiz, direkt çağır
                                    me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                                    if (mainPlugin != null && mainPlugin.getStructureCoreManager() != null) {
                                        if (!mainPlugin.getStructureCoreManager().isInactiveCore(block.getLocation())) {
                                            mainPlugin.getStructureCoreManager().addInactiveCore(block.getLocation(), ownerId);
                                        }
                                    }
                                }
                            }
                            
                            // ✅ Tuzak çekirdeği kontrolü
                            if (type == Material.LODESTONE) {
                                UUID ownerId = me.mami.stratocraft.util.CustomBlockData.getTrapCoreOwner(block);
                                if (ownerId != null) {
                                    // ✅ DÜZELTME: Artık main thread'deyiz, direkt çağır
                                    me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                                    if (mainPlugin != null && mainPlugin.getTrapManager() != null) {
                                        mainPlugin.getTrapManager().registerInactiveTrapCore(block.getLocation(), ownerId);
                                    }
                                }
                            }
                            
                            // ✅ Klan bankası kontrolü
                            if (type == Material.ENDER_CHEST) {
                                UUID clanId = me.mami.stratocraft.util.CustomBlockData.getClanBankData(block);
                                if (clanId != null) {
                                    // ✅ DÜZELTME: Artık main thread'deyiz, direkt çağır
                                    me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                                    if (mainPlugin != null && mainPlugin.getClanBankSystem() != null) {
                                        // bankChestLocations'a ekle (reflection ile)
                                        try {
                                            java.lang.reflect.Field field = mainPlugin.getClanBankSystem().getClass()
                                                .getDeclaredField("bankChestLocations");
                                            field.setAccessible(true);
                                            @SuppressWarnings("unchecked")
                                            java.util.Map<UUID, org.bukkit.Location> locations = 
                                                (java.util.Map<UUID, org.bukkit.Location>) field.get(mainPlugin.getClanBankSystem());
                                            if (locations != null && !locations.containsKey(clanId)) {
                                                locations.put(clanId, block.getLocation());
                                            }
                                        } catch (Exception e) {
                                            // Reflection hatası - önemli değil
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
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
        
        // ✅ YENİ: TNT yerleştirme kontrolü (grief protection)
        if (block.getType() == Material.TNT) {
            Clan owner = territoryManager.getTerritoryOwner(blockLoc);
            if (owner != null && owner.hasCrystal()) {
                Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
                // Kendi klanında TNT yerleştirebilir (savaş durumunda)
                if (playerClan != null && playerClan.equals(owner)) {
                    return; // Kendi klanında TNT yerleştirebilir
                }
                // Misafir TNT yerleştiremez
                if (owner.isGuest(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage("§cTNT yerleştirmek için misafir izni yeterli değil!");
                    return;
                }
                // Savaş durumunda düşman klanında TNT yerleştirebilir
                if (playerClan != null && owner.isAtWarWith(playerClan.getId())) {
                    return; // Savaş durumunda TNT yerleştirebilir
                }
                // Engelle - Düşman klan alanında TNT yerleştirme yasak
                event.setCancelled(true);
                player.sendMessage("§cTNT yerleştirmek için önce kuşatma başlatmalısın!");
                return;
            }
        }
        
        // Bölge sahibi kontrolü
        Clan owner = territoryManager.getTerritoryOwner(blockLoc);
        if (owner == null) return; // Sahipsiz yerse yerleştirilebilir
        
        // Ölümsüz klan önleme: Kristal yoksa bölge koruması yok
        if (!owner.hasCrystal()) {
            return; // Kristal yoksa koruma yok
        }
        
        // Kendi yerinse yerleştirilebilir (Rütbe kontrolü dahil)
        UUID playerId = player.getUniqueId();
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(playerId);
        if (playerClan != null && playerClan.equals(owner)) {
            // ✅ PERFORMANS: Rütbe cache kullan (her event'te getRank() çağrısını önle)
            // Not: Rütbe nadiren değişir, mevcut implementasyon yeterli
            Clan.Rank rank = playerClan.getRank(playerId);
            if (rank == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                player.sendMessage("§cAcemilerin blok yerleştirme yetkisi yok!");
                return;
            }
            if (rank == Clan.Rank.MEMBER) {
                event.setCancelled(true);
                player.sendMessage("§cÜyelerin blok yerleştirme yetkisi yok!");
                return;
            }
            // ELITE, GENERAL, LEADER blok yerleştirebilir
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
        
        // --- ALAN KONTROLÜ (FENCE CHECK) - MAIN THREAD ---
        event.setCancelled(true); // Önce iptal et, kontrol sonrası devam edeceğiz
        
        // ✅ DÜZELTME: Main thread'de çalıştır (chunk yükleme ve PDC okuma için gerekli)
        // Async context'te chunk yükleme ve blok state'i alma sorunlu olabilir
        Player finalPlayer = player;
        Block finalPlaceLocation = placeLocation;
        
        // ✅ DÜZELTME: Önce tüm chunk'ları yükle (çit kontrolü için)
        org.bukkit.Location crystalLoc = finalPlaceLocation.getLocation();
        int radius = 20; // Çit kontrolü için yarıçap
        java.util.Set<org.bukkit.Chunk> loadedChunks = new java.util.HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int chunkX = (crystalLoc.getBlockX() + x) >> 4;
                int chunkZ = (crystalLoc.getBlockZ() + z) >> 4;
                try {
                    org.bukkit.Chunk chunk = crystalLoc.getWorld().getChunkAt(chunkX, chunkZ);
                    if (!chunk.isLoaded()) {
                        boolean loaded = chunk.load(false);
                        if (loaded) {
                            loadedChunks.add(chunk);
                        }
                    }
                } catch (Exception e) {
                    // Chunk yüklenemedi, devam et
                    me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
                    if (mainPlugin != null) {
                        mainPlugin.getLogger().fine("Chunk yüklenemedi: " + chunkX + "," + chunkZ + " - " + e.getMessage());
                    }
                }
            }
        }
        
        // Main thread'de çit kontrolü yap
        boolean isValid = isSurroundedByClanFences3D(finalPlaceLocation);
        
        if (!isValid) {
            finalPlayer.sendMessage("§cKlan Kristali sadece §6Klan Çitleri §cile tamamen çevrelenmiş güvenli bir alana kurulabilir!");
            return;
        }
        
        // --- KLAN KURULUMU ---
        continueCrystalPlacement(finalPlayer, finalPlaceLocation);
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
                            
                            // ✅ YENİ: Y ekseni sınırlarını güncelle
                            territoryData.updateYBounds();
                            
                            // Main thread'e geri dön ve TerritoryData'yı kaydet
                            org.bukkit.Bukkit.getScheduler().runTask(
                                me.mami.stratocraft.Main.getInstance(),
                                () -> {
                                    if (boundaryManager != null) {
                                        boundaryManager.setTerritoryData(finalNewClan, territoryData);
                                        // ✅ YENİ: Sınır koordinatlarını hesapla
                                        territoryData.calculateBoundaries();
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
    @EventHandler(priority = EventPriority.LOW) // ✅ OPTİMİZE: MONITOR → LOW (diğer listener'lar önce çalışsın)
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
        
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // ✅ OPTİMİZE: Cooldown kontrolü (2 saniye → 5 saniye)
        Long lastTime = lastBoundaryParticleTime.get(playerId);
        if (lastTime != null && (now - lastTime) < 5000L) { // 5 saniye cooldown
            return; // Cooldown'da
        }
        
        // ✅ PERFORMANS: Cache'den klan ID'sini al
        CachedPlayerClanData cached = playerMoveCache.get(playerId);
        Clan playerClan = null;
        
        if (cached != null && now - cached.lastCheck < PLAYER_MOVE_CACHE_DURATION) {
            // Cache'den al
            if (cached.clanId != null) {
                playerClan = territoryManager.getClanManager().getClanById(cached.clanId);
            }
        } else {
            // Cache'de yoksa veya süresi dolmuşsa hesapla
            playerClan = territoryManager.getClanManager().getClanByPlayer(playerId);
            UUID clanId = playerClan != null ? playerClan.getId() : null;
            playerMoveCache.put(playerId, new CachedPlayerClanData(clanId, now));
        }
        
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
     * ✅ YENİ: Y ekseni sınırlarını dikkate alır
     */
    private void showTerritoryBoundary(Player player, Territory territory, Location playerLoc) {
        Location center = territory.getCenter();
        if (center == null || center.getWorld() == null) return;
        if (!center.getWorld().equals(playerLoc.getWorld())) return;
        
        // ✅ YENİ: TerritoryData'dan Y ekseni sınırlarını al
        int minY = Integer.MIN_VALUE;
        int maxY = Integer.MAX_VALUE;
        if (boundaryManager != null) {
            Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
            if (playerClan != null) {
                me.mami.stratocraft.model.territory.TerritoryData data = boundaryManager.getTerritoryData(playerClan);
                if (data != null) {
                    minY = data.getMinY() - data.getGroundDepth();
                    maxY = data.getMaxY() + data.getSkyHeight();
                }
            }
        }
        
        double radius = territory.getRadius();
        double angle = Math.atan2(playerLoc.getZ() - center.getZ(), playerLoc.getX() - center.getX());
        
        // Sınır çizgisinde birkaç noktada partikül göster
        for (int i = -2; i <= 2; i++) {
            double offsetAngle = angle + (i * 0.3); // Her 0.3 radyan (yaklaşık 17 derece)
            double x = center.getX() + (radius * Math.cos(offsetAngle));
            double z = center.getZ() + (radius * Math.sin(offsetAngle));
            
            // ✅ YENİ: Yükseklik: Oyuncunun Y seviyesi, ama sınırlar içinde
            int playerY = playerLoc.getBlockY();
            int effectiveY = (minY != Integer.MIN_VALUE && maxY != Integer.MAX_VALUE) 
                ? Math.max(minY, Math.min(maxY, playerY)) 
                : playerY;
            double y = effectiveY + (i * 0.5);
            
            // Y sınırlarını kontrol et
            if (minY != Integer.MIN_VALUE && maxY != Integer.MAX_VALUE) {
                y = Math.max(minY, Math.min(maxY, y));
            }
            
            Location particleLoc = new Location(center.getWorld(), x, y, z);
            
            // Mesafe kontrolü (performans)
            if (playerLoc.distance(particleLoc) > 20) continue;
            
            // Klan rengine göre partikül (yeşil - kendi klanı)
            player.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0,
                new Particle.DustOptions(org.bukkit.Color.fromRGB(0, 255, 0), 1.0f)); // Yeşil
        }
    }
    
    // ✅ YENİ: 3D Flood Fill Algoritması ile Klan Çiti Kontrolü
    // Yükseklik farkı ve havada çitler desteklenir
    private boolean isSurroundedByClanFences3D(Block center) {
        int heightTolerance = territoryConfig != null ? 
            territoryConfig.getFenceHeightTolerance() : 5; // Varsayılan: 5 blok
        
        Set<Location> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        boolean foundClanFence = false;
        
        queue.add(center);
        visited.add(center.getLocation());
        
        int minArea = 9; // Minimum alan (3x3)
        int maxIterations = 1000; // 3D için daha fazla iteration gerekebilir
        int iterations = 0;
        
        int centerY = center.getY();
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            if (iterations > maxIterations) {
                return false; // Çok büyük alan
            }
            
            // ✅ YENİ: 6 yöne bak (3D)
            BlockFace[] faces = {
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST,
                BlockFace.UP, BlockFace.DOWN  // ✅ Y ekseni eklendi
            };
            
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                Location neighborLoc = neighbor.getLocation();
                
                if (visited.contains(neighborLoc)) continue;
                
                // ✅ DÜZELTME: Chunk yükleme kontrolü (blok okumak için chunk yüklü olmalı)
                // Not: Zaten önceden chunk'lar yükleniyor ama yine de kontrol edelim (güvenlik için)
                org.bukkit.Chunk neighborChunk = neighbor.getChunk();
                if (!neighborChunk.isLoaded()) {
                    // Chunk yüklenmemiş, yükle (ama bu nadiren olmalı çünkü önceden yükleniyor)
                    neighborChunk.load(false);
                    // Chunk yüklenemediyse bu bloğu atla
                    if (!neighborChunk.isLoaded()) {
                        visited.add(neighborLoc);
                        continue;
                    }
                }
                
                // ✅ YENİ: Yükseklik toleransı kontrolü
                int heightDiff = Math.abs(neighbor.getY() - centerY);
                if (heightDiff > heightTolerance) {
                    visited.add(neighborLoc); // Ziyaret edildi olarak işaretle
                    continue; // Tolerans dışında, atla
                }
                
                Material type = neighbor.getType();
                
                // Çit kontrolü
                if (type == Material.OAK_FENCE) {
                    if (isClanFenceFast(neighbor)) {
                        foundClanFence = true;
                        visited.add(neighborLoc);
                        
                        // ✅ Çit bağlantı kontrolü (opsiyonel)
                        if (territoryConfig != null && territoryConfig.isFenceConnectionRequired()) {
                            if (current.getType() == Material.OAK_FENCE && 
                                isClanFenceFast(current)) {
                                if (!isFenceConnected(current, neighbor)) {
                                    // Bağlantısız çit - alan açık
                                    return false;
                                }
                            }
                        }
                        
                        continue; // Klan çiti, devam et
                    } else {
                        return false; // Normal çit - alan açık
                    }
                }
                
                // Solid blok - engel (yükseklik farkı olabilir)
                if (type != Material.AIR && 
                    type != Material.CAVE_AIR && 
                    type != Material.VOID_AIR) {
                    visited.add(neighborLoc);
                    continue;
                }
                
                // Hava - aramaya devam (3D)
                visited.add(neighborLoc);
                queue.add(neighbor);
            }
        }
        
        return visited.size() >= minArea && foundClanFence;
    }
    
    /**
     * ✅ YENİ: İki çitin birbirine bağlı olup olmadığını kontrol et
     */
    private boolean isFenceConnected(Block fence1, Block fence2) {
        // Material kontrolü
        if (fence1.getType() != Material.OAK_FENCE || 
            fence2.getType() != Material.OAK_FENCE) {
            return false;
        }
        
        // ✅ Fence BlockData kontrolü
        org.bukkit.block.data.BlockData data1 = fence1.getBlockData();
        org.bukkit.block.data.BlockData data2 = fence2.getBlockData();
        
        if (data1 instanceof org.bukkit.block.data.type.Fence && 
            data2 instanceof org.bukkit.block.data.type.Fence) {
            org.bukkit.block.data.type.Fence fenceData1 = (org.bukkit.block.data.type.Fence) data1;
            org.bukkit.block.data.type.Fence fenceData2 = (org.bukkit.block.data.type.Fence) data2;
            
            // ✅ Yön hesaplama
            BlockFace direction = getDirection(fence1, fence2);
            if (direction == null) {
                return false; // Geçersiz yön
            }
            
            // ✅ Çitlerin birbirine bağlı olup olmadığını kontrol et
            return fenceData1.hasFace(direction) && 
                   fenceData2.hasFace(direction.getOppositeFace());
        }
        
        return false;
    }
    
    /**
     * ✅ YENİ: İki blok arasındaki yönü hesapla
     */
    private BlockFace getDirection(Block from, Block to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        
        // ✅ Sadece 1 blok mesafede olan bloklar için yön hesapla
        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
            return null; // 1 bloktan fazla mesafe
        }
        
        // ✅ Yön hesaplama
        if (dx == 1 && dy == 0 && dz == 0) return BlockFace.EAST;
        if (dx == -1 && dy == 0 && dz == 0) return BlockFace.WEST;
        if (dx == 0 && dy == 0 && dz == 1) return BlockFace.SOUTH;
        if (dx == 0 && dy == 0 && dz == -1) return BlockFace.NORTH;
        if (dx == 0 && dy == 1 && dz == 0) return BlockFace.UP;
        if (dx == 0 && dy == -1 && dz == 0) return BlockFace.DOWN;
        
        return null;
    }
    
    // Flood Fill Algoritması ile Klan Çiti Kontrolü (ESKİ - 2D)
    // ❌ DEPRECATED: isSurroundedByClanFences3D() kullanılmalı
    @Deprecated
    private boolean isSurroundedByClanFences(Block center) {
        Set<Long> visited = new HashSet<>(); // Packed koordinat (daha hızlı)
        Queue<Block> queue = new LinkedList<>();
        boolean foundClanFence = false;
        
        queue.add(center);
        visited.add(packCoords(center));
        
        int minArea = 9; // Minimum alan büyüklüğü (3x3 = 9 blok)
        int iterations = 0;
        int maxIterations = 500; // OPTİMİZE: 5000'den 500'e düşürüldü (daha küçük alan)
        
        // Klan çitleri için cache (aynı çiti tekrar kontrol etme)
        Set<Long> checkedFences = new HashSet<>();
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            // Maksimum limit kontrolü (lag önleme)
            if (iterations > maxIterations) {
                return false;
            }
            
            // 4 Yöne bak (Kuzey, Güney, Doğu, Batı)
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                long neighborKey = packCoords(neighbor);
                if (visited.contains(neighborKey)) continue;
                
                Material type = neighbor.getType();
                
                // Çit kontrolü (hızlı)
                if (type == Material.OAK_FENCE) {
                    // Önce cache'e bak
                    if (!checkedFences.contains(neighborKey)) {
                        checkedFences.add(neighborKey);
                        if (isClanFenceFast(neighbor)) {
                            foundClanFence = true;
                            visited.add(neighborKey);
                            continue;
                        } else {
                            // Normal çit - alan açık
                            return false;
                        }
                    }
                    visited.add(neighborKey);
                    continue;
                }
                
                // Solid blok - engel
                if (type != Material.AIR && type != Material.CAVE_AIR && type != Material.VOID_AIR) {
                    visited.add(neighborKey);
                    continue;
                }
                
                // Hava - aramaya devam
                visited.add(neighborKey);
                queue.add(neighbor);
            }
        }
        
        // Minimum alan ve en az bir klan çiti kontrolü
        return visited.size() >= minArea && foundClanFence;
    }
    
    /**
     * Koordinatları long'a pack et (daha hızlı HashSet işlemleri)
     */
    private long packCoords(Block block) {
        return ((long)block.getX() & 0x3FFFFFF) | (((long)block.getZ() & 0x3FFFFFF) << 26) | (((long)block.getY() & 0xFFF) << 52);
    }
    
    /**
     * Hızlı klan çiti kontrolü (PersistentDataContainer)
     * ✅ YENİ: PersistentDataContainer kontrolü
     * ✅ FALLBACK: TerritoryData kontrolü (backup)
     */
    private boolean isClanFenceFast(Block block) {
        if (block.getType() != Material.OAK_FENCE) {
            return false;
        }
        
        // ✅ DÜZELTME: Chunk yükleme kontrolü (PDC okumak için chunk yüklü olmalı)
        // Not: getClanFenceData() zaten chunk yükleme kontrolü yapıyor, burada tekrar yapmaya gerek yok
        // Ancak async context'te çalışıyorsak chunk yükleme gerekebilir
        
        // ✅ YENİ: PersistentDataContainer kontrolü
        UUID clanId = me.mami.stratocraft.util.CustomBlockData.getClanFenceData(block);
        if (clanId != null) {
            return true; // Klan çiti
        }
        
        // ✅ FALLBACK: TerritoryData kontrolü (backup - chunk yükleme gerektirmez)
        if (boundaryManager != null) {
            // TerritoryData'da bu konum var mı?
            org.bukkit.Location blockLoc = block.getLocation();
            Clan nearbyClan = territoryManager.getTerritoryOwner(blockLoc);
            if (nearbyClan != null) {
                me.mami.stratocraft.model.territory.TerritoryData data = boundaryManager.getTerritoryData(nearbyClan);
                if (data != null) {
                    for (org.bukkit.Location fenceLoc : data.getFenceLocations()) {
                        if (fenceLoc.getWorld().equals(blockLoc.getWorld()) &&
                            fenceLoc.getBlockX() == blockLoc.getBlockX() &&
                            fenceLoc.getBlockY() == blockLoc.getBlockY() &&
                            fenceLoc.getBlockZ() == blockLoc.getBlockZ()) {
                            return true; // TerritoryData'da bulundu
                        }
                    }
                }
            }
        }
        
        // ❌ ESKİ: Metadata kontrolü kaldırıldı
        // if (territoryConfig != null) {
        //     String metadataKey = territoryConfig.getFenceMetadataKey();
        //     if (block.hasMetadata(metadataKey)) {
        //         return true;
        //     }
        // }
        
        return false;
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
            }
            
            // Normal durumda sadece lider kırabilir
            if (breaker != null) {
                Clan playerClan = territoryManager.getClanManager().getClanByPlayer(breaker.getUniqueId());
                if (playerClan == null || !playerClan.equals(owner) || owner.getRank(breaker.getUniqueId()) != Clan.Rank.LEADER) {
                    event.setCancelled(true);
                    breaker.sendMessage("§cKlan Kristalini sadece klan lideri kırabilir!");
                    return;
                }
                
                // ✅ Lider kristali kırıyor - özel item drop EntityDeathEvent'te yapılacak
                // Not: EnderCrystal entity olduğu için BlockBreakEvent değil, EntityDeathEvent kullanılacak
            } else {
                // Doğal hasar (lava, patlama vb.) - engelle
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * ✅ YENİ: EnderCrystal öldüğünde özel item drop et (CLAN_CRYSTAL)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCrystalDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        
        // Bu kristal bir klan kristali mi?
        Clan owner = findClanByCrystal(crystal);
        if (owner == null) return; // Normal end crystal
        
        // ✅ Normal drop'ları iptal et
        event.getDrops().clear();
        
        // ✅ Özel item oluştur (END_CRYSTAL + PDC verisi)
        ItemStack crystalItem = new ItemStack(Material.END_CRYSTAL);
        org.bukkit.inventory.meta.ItemMeta meta = crystalItem.getItemMeta();
        if (meta != null) {
            // Display name ve lore ekle (ItemManager.registerClanCrystalRecipe() ile uyumlu)
            meta.setDisplayName("§5§lKlan Kristali");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7Klan bölgesinin merkezi.");
            meta.setLore(lore);
            
            // ✅ PDC verisini ekle
            org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_crystal");
            container.set(key, org.bukkit.persistence.PersistentDataType.STRING, owner.getId().toString());
            
            // ✅ ItemManager.isClanItem() için custom_id ekle
            org.bukkit.NamespacedKey customIdKey = new org.bukkit.NamespacedKey(me.mami.stratocraft.Main.getInstance(), "custom_id");
            container.set(customIdKey, org.bukkit.persistence.PersistentDataType.STRING, "CLAN_CRYSTAL");
            
            crystalItem.setItemMeta(meta);
        }
        
        // ✅ Özel item'ı drop et
        crystal.getWorld().dropItemNaturally(crystal.getLocation(), crystalItem);
        
        // ✅ CustomBlockData'dan temizle (eğer blok olarak kaydedilmişse)
        org.bukkit.Location crystalLoc = crystal.getLocation();
        if (crystalLoc != null) {
            org.bukkit.block.Block block = crystalLoc.getBlock();
            if (block != null && block.getType() == Material.END_CRYSTAL) {
                me.mami.stratocraft.util.CustomBlockData.removeClanCrystalData(block);
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
    
    // ========== KLAN KRISTALİ TAŞIMA SİSTEMİ (ENDERMAN TARZI) ==========
    
    /**
     * Kristali eline alma - Shift+Sağ tık ile kristale yaklaşıp alır
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalPickup(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof EnderCrystal)) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Shift basılı olmalı
        if (!player.isSneaking()) return;
        
        // Zaten kristal taşıyor mu?
        if (carryingCrystalPlayers.containsKey(playerId)) {
            player.sendMessage("§cZaten bir kristal taşıyorsunuz!");
            event.setCancelled(true);
            return;
        }
        
        EnderCrystal crystal = (EnderCrystal) event.getRightClicked();
        
        // Bu kristal bir klanın kristali mi?
        Clan ownerClan = null;
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (clan.getCrystalEntity() != null && clan.getCrystalEntity().equals(crystal)) {
                ownerClan = clan;
                break;
            }
        }
        
        if (ownerClan == null) {
            return; // Normal kristal, devam etme
        }
        
        // Oyuncu bu klanın lideri mi?
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(playerId);
        if (playerClan == null || !playerClan.equals(ownerClan)) {
            player.sendMessage("§cBu kristal sizin klanınıza ait değil!");
            event.setCancelled(true);
            return;
        }
        
        if (playerClan.getRank(playerId) != Clan.Rank.LEADER) {
            player.sendMessage("§cSadece klan lideri kristali taşıyabilir!");
            event.setCancelled(true);
            return;
        }
        
        // Eller boş mu kontrol et
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if ((mainHand != null && mainHand.getType() != Material.AIR) || 
            (offHand != null && offHand.getType() != Material.AIR)) {
            player.sendMessage("§cKristali taşımak için her iki eliniz de boş olmalı!");
            event.setCancelled(true);
            return;
        }
        
        event.setCancelled(true);
        
        // Kristali kaldır ve taşımaya başla
        Location crystalLoc = crystal.getLocation().clone();
        crystal.remove(); // Kristali dünyadan kaldır
        
        // Taşıma verisini kaydet
        carryingCrystalPlayers.put(playerId, new CarryingCrystalData(ownerClan, crystalLoc, null));
        
        // Klandaki kristal referansını null yap
        ownerClan.setCrystalEntity(null);
        
        // Elle End Crystal görseli ver (görsel için)
        ItemStack crystalItem = new ItemStack(Material.END_CRYSTAL);
        org.bukkit.inventory.meta.ItemMeta meta = crystalItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5§l" + ownerClan.getName() + " Kristali");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7Klan Kristali taşınıyor...");
            lore.add("§c§lUyarı: Ölürsen kristal patlar!");
            lore.add("");
            lore.add("§eSağ tık ile yere bırak");
            meta.setLore(lore);
            crystalItem.setItemMeta(meta);
        }
        player.getInventory().setItemInMainHand(crystalItem);
        
        // Efektler
        player.getWorld().spawnParticle(Particle.PORTAL, crystalLoc, 100, 0.5, 0.5, 0.5, 0.5);
        player.playSound(crystalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        
        player.sendMessage("§d§lKlan Kristali'ni aldınız!");
        player.sendMessage("§7Yere bırakmak için §eSağ Tık §7yapın.");
        player.sendMessage("§c§lUyarı: Ölürseniz kristal patlar!");
        
        // Title göster
        player.sendTitle("§5§lKRİSTAL TAŞINIYOR", "§7Dikkatli olun!", 10, 70, 20);
    }
    
    /**
     * Kristali yere bırakma - Sağ tık ile yere bırakır
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalDrop(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Kristal taşıyor mu?
        CarryingCrystalData carryData = carryingCrystalPlayers.get(playerId);
        if (carryData == null) return;
        
        // Elindeki item kristal mi?
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.END_CRYSTAL) return;
        
        event.setCancelled(true);
        
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        
        Block newLocation = clicked.getRelative(BlockFace.UP);
        
        // Yeni konum boş mu?
        if (newLocation.getType() != Material.AIR) {
            player.sendMessage("§cYeni konum boş olmalı!");
            return;
        }
        
        Location newLoc = newLocation.getLocation().add(0.5, 0, 0.5);
        
        // Async çit kontrolü
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
            me.mami.stratocraft.Main.getInstance(),
            () -> {
                boolean isValid = isSurroundedByClanFences3D(newLocation);
                
                org.bukkit.Bukkit.getScheduler().runTask(
                    me.mami.stratocraft.Main.getInstance(),
                    () -> {
                        // Oyuncu hala online mi ve hala taşıyor mu?
                        Player p = org.bukkit.Bukkit.getPlayer(playerId);
                        if (p == null || !p.isOnline()) return;
                        
                        CarryingCrystalData data = carryingCrystalPlayers.get(playerId);
                        if (data == null) return;
                        
                        if (!isValid) {
                            p.sendMessage("§cYeni konum Klan Çitleri ile çevrili olmalı!");
                            return;
                        }
                        
                        // Kristali dünyaya geri koy
                        EnderCrystal newCrystal = (EnderCrystal) newLoc.getWorld().spawnEntity(newLoc, EntityType.ENDER_CRYSTAL);
                        newCrystal.setShowingBottom(true);
                        newCrystal.setInvulnerable(true);
                        
                        // Klan verilerini güncelle
                        data.clan.setCrystalLocation(newLoc);
                        data.clan.setCrystalEntity(newCrystal);
                        
                        // Territory oluştur
                        Territory territory = new Territory(data.clan.getId(), newLoc);
                        if (territory.getRadius() < 50) {
                            territory.expand(50 - territory.getRadius());
                        }
                        data.clan.setTerritory(territory);
                        territoryManager.setCacheDirty();
                        
                        // Elinden kristali al
                        p.getInventory().setItemInMainHand(null);
                        
                        // Taşıma verisini kaldır
                        carryingCrystalPlayers.remove(playerId);
                        
                        // Efektler
                        p.getWorld().spawnParticle(Particle.TOTEM, newLoc, 100, 0.5, 0.5, 0.5, 0.1);
                        p.getWorld().spawnParticle(Particle.END_ROD, newLoc, 50, 1, 1, 1, 0.1);
                        p.playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
                        p.playSound(newLoc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                        
                        p.sendMessage("§a§lKlan Kristali yerleştirildi!");
                        p.sendTitle("§a§lKRİSTAL YERLEŞTİRİLDİ", "§7Yeni konum aktif!", 10, 40, 10);
                    }
                );
            }
        );
    }
    
    /**
     * Kristal taşırken ölürse kristal patlar
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCarryingPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        CarryingCrystalData carryData = carryingCrystalPlayers.remove(playerId);
        if (carryData == null) return;
        
        // Kristali elinden düşür (drop olmasın)
        player.getInventory().setItemInMainHand(null);
        
        // Kristal patlaması efekti
        Location deathLoc = player.getLocation();
        deathLoc.getWorld().createExplosion(deathLoc, 4f, false, false);
        deathLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, deathLoc, 5, 1, 1, 1, 0);
        deathLoc.getWorld().spawnParticle(Particle.DRAGON_BREATH, deathLoc, 100, 2, 2, 2, 0.1);
        
        // Klan kristalini tamamen yok et
        carryData.clan.setCrystalLocation(null);
        carryData.clan.setCrystalEntity(null);
        carryData.clan.setTerritory(null);
        territoryManager.setCacheDirty();
        
        // Klan üyelerine bildir
        for (UUID memberId : carryData.clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§c§l⚠ KLAN KRİSTALİ YOK EDİLDİ!");
                member.sendMessage("§7Lider kristali taşırken öldü!");
                member.sendTitle("§c§lKRİSTAL YOK EDİLDİ!", "§7Lider öldü!", 10, 60, 20);
                member.playSound(member.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 0.5f);
            }
        }
        
        // Global duyuru
        Bukkit.broadcastMessage("§c§l" + carryData.clan.getName() + " §cklanının kristali yok edildi!");
    }
    
    /**
     * Kristal taşırken item değiştirme engelleme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCarryingPlayerItemHeld(org.bukkit.event.player.PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!carryingCrystalPlayers.containsKey(playerId)) return;
        
        // Item değiştirmeyi engelle
        event.setCancelled(true);
        player.sendMessage("§cKristal taşırken item değiştiremezsiniz!");
    }
    
    /**
     * Kristal taşırken item alma engelleme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCarryingPlayerPickup(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();
        
        if (!carryingCrystalPlayers.containsKey(playerId)) return;
        
        // Item almayı engelle
        event.setCancelled(true);
    }
    
    /**
     * Kristal taşırken item atma engelleme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCarryingPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!carryingCrystalPlayers.containsKey(playerId)) return;
        
        // Kristali atmayı engelle
        event.setCancelled(true);
        player.sendMessage("§cKristal taşırken item atamazsınız!");
    }
    
    /**
     * Kristal taşırken envanter açma engelleme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCarryingPlayerInventory(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!carryingCrystalPlayers.containsKey(playerId)) return;
        
        // Sadece kendi envanterini açabilsin
        if (event.getInventory().getType() != InventoryType.PLAYER && 
            event.getInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
            player.sendMessage("§cKristal taşırken envanter açamazsınız!");
        }
    }
    
    /**
     * Kristal taşırken saldırı engelleme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCarryingPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        
        if (!carryingCrystalPlayers.containsKey(playerId)) return;
        
        // Saldırıyı engelle
        event.setCancelled(true);
        player.sendMessage("§cKristal taşırken saldıramazsınız!");
    }
    
    /**
     * Oyuncu kristal taşıyor mu kontrol et
     */
    public boolean isCarryingCrystal(UUID playerId) {
        return carryingCrystalPlayers.containsKey(playerId);
    }
    
    /**
     * Taşınan kristal verisini al
     */
    public CarryingCrystalData getCarryingCrystalData(UUID playerId) {
        return carryingCrystalPlayers.get(playerId);
    }
    
    /**
     * Plugin kapanırken tüm taşınan kristalleri geri koy
     */
    public void cancelAllCrystalMoveTasks() {
        for (Map.Entry<UUID, CarryingCrystalData> entry : carryingCrystalPlayers.entrySet()) {
            UUID playerId = entry.getKey();
            CarryingCrystalData data = entry.getValue();
            
            // Orijinal konuma kristali geri koy
            if (data.originalLocation != null && data.originalLocation.getWorld() != null) {
                EnderCrystal crystal = (EnderCrystal) data.originalLocation.getWorld().spawnEntity(
                    data.originalLocation, EntityType.ENDER_CRYSTAL);
                crystal.setShowingBottom(true);
                crystal.setInvulnerable(true);
                
                data.clan.setCrystalLocation(data.originalLocation);
                data.clan.setCrystalEntity(crystal);
            }
            
            // Oyuncunun elinden kristali al
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage("§eKristal orijinal konumuna geri döndürüldü.");
            }
        }
        carryingCrystalPlayers.clear();
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
        
        // ✅ YENİ: Y ekseni sınırlarını güncelle
        territoryData.updateYBounds();
    }
}

