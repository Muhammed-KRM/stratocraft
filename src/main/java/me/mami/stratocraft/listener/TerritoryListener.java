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
    
    /**
     * ✅ YAPı ÇEKİRDEĞİ GİBİ: Klan çiti yerleştirme
     * ItemStack'ten kontrol et ve PDC'ye kaydet
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFencePlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        
        // ✅ YAPı ÇEKİRDEĞİ GİBİ: Önce item kontrolü yap (blok yerleştirilmeden önce)
        boolean isClanFenceItem = ItemManager.isCustomItem(item, "CLAN_FENCE");
        
        if (!isClanFenceItem) {
            // Normal çit kontrolü - bypass kontrolü
            boolean hasBypass = me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(event.getPlayer());
            if (!hasBypass) {
                Block block = event.getBlock();
        // Material kontrolü - Sadece OAK_FENCE kontrol et (klan çiti OAK_FENCE)
                if (block.getType() == Material.OAK_FENCE) {
                    me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE] Normal çit engellendi");
            event.setCancelled(true);
                    event.getPlayer().sendMessage("§cKlan alanında sadece §6Klan Çiti §cyerleştirilebilir!");
                    event.getPlayer().sendMessage("§7Normal çitler kabul edilmez. Klan Çiti craft edin.");
                }
            }
            return;
        }
        
        
        // ✅ KRİTİK: Klan çiti item'ı ile yerleştirme - blok yerleştirildikten SONRA işaretle
        // YAPı ÇEKİRDEĞİ GİBİ: getBlockPlaced() kullan (blok artık dünyada)
        Block placed = event.getBlockPlaced();
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE] getBlockPlaced(): " + (placed != null ? placed.getType().name() + " @ " + placed.getLocation() : "NULL"));
        
        if (placed == null || placed.getType() != Material.OAK_FENCE) {
            me.mami.stratocraft.Main.getInstance().getLogger().warning("[CLAN_FENCE_PLACE] Blok null veya OAK_FENCE değil, return");
            return;
        }
        
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE] setClanFenceData() çağrılıyor: " + placed.getLocation());
        // ✅ KRİTİK: Admin bypass olsa bile özel blok işaretlemesi yapılmalı
        // Böylece /stratocraft give ile alınan veya craftlanan klan çiti yere konup kırılınca
        // tekrar klan çiti olarak düşer.
        boolean setResult = me.mami.stratocraft.util.CustomBlockData.setClanFenceData(placed);
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE] setClanFenceData() sonucu: " + setResult);
        
        Player player = event.getPlayer();
        
        // ✅ Klan kontrolü - genişletme için (klan yoksa genişletme yapılmaz)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) {
            return; // Klan yoksa genişletme işlemi yapma
        }
        
        // Kristal var mı?
        if (playerClan.getCrystalLocation() == null || !playerClan.hasCrystal()) {
            return; // Kristal yok, genişletme yok
        }
        
        Territory territory = playerClan.getTerritory();
        if (territory == null) return;
        
        Location crystalLoc = playerClan.getCrystalLocation();
        Location fenceLoc = placed.getLocation();
        
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
    /**
     * ✅ YAPı ÇEKİRDEĞİ GİBİ: Klan çiti kırma event'i
     * PersistentDataContainer'dan veri oku ve drop edilen item'a ekle
     * ✅ KRİTİK: HIGHEST priority kullan (diğer listener'lar override etmesin)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFenceBreak(BlockBreakEvent event) {
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] Event tetiklendi - Priority: HIGHEST");
        
        Block block = event.getBlock();
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] Block: " + block.getType().name() + " @ " + block.getLocation());
        
        // Material kontrolü
        if (block.getType() != Material.OAK_FENCE) {
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] OAK_FENCE değil, return");
            return;
        }
        
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] OAK_FENCE tespit edildi, chunk kontrolü başlıyor");
        
        // ✅ YAPı ÇEKİRDEĞİ GİBİ: Chunk yükleme kontrolü (PDC okumak için chunk yüklü olmalı)
        org.bukkit.Chunk chunk = block.getChunk();
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] Chunk yüklü mü: " + chunk.isLoaded());
        
        if (!chunk.isLoaded()) {
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] Chunk yüklü değil, yüklenmeye çalışılıyor");
            try {
                chunk.load(false);
                me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] Chunk yükleme sonucu: " + chunk.isLoaded());
            } catch (Exception e) {
                // Chunk yüklenemiyorsa atla
                me.mami.stratocraft.Main.getInstance().getLogger().warning("[CLAN_FENCE_BREAK] Chunk yüklenemedi: " + e.getMessage());
                return;
            }
        }
        
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] isClanFence() kontrolü başlıyor");
        // ✅ DÜZELTME: Sadece klan çiti bayrağını kontrol et (clanId yok)
        // ✅ DEBUG: Runtime + PDC kontrolü
        boolean isClanFence = me.mami.stratocraft.util.CustomBlockData.isClanFence(block);
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] isClanFence() sonucu: " + isClanFence);
        
        if (!isClanFence) {
            // Normal çit, işlem yok
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] Normal çit, return (klan çiti değil)");
            return;
        }
        
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] KLAN ÇİTİ TESPİT EDİLDİ! Drop işlemi başlıyor");
        
        // ✅ Normal drop'ları iptal et (HIGHEST priority'de önce biz çalışalım)
        event.setDropItems(false);
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] setDropItems(false) çağrıldı");
        
        // ✅ YAPı ÇEKİRDEĞİ GİBİ: ItemManager'dan static field'ı kullan
        // ✅ DÜZELTME: ClanId verisi ekleme - stacklenme için clanId verisi eklenmeyecek
        // ClanId verisi sadece yerleştirme sırasında oyuncunun klanından alınır, item'da tutulmaz
        // Bu sayede tüm klan çitleri stacklenebilir
        ItemStack clanFenceItem = ItemManager.CLAN_FENCE != null ? ItemManager.CLAN_FENCE.clone() : null;
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] ItemManager.CLAN_FENCE: " + (ItemManager.CLAN_FENCE != null ? "VAR" : "NULL"));
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] clanFenceItem clone: " + (clanFenceItem != null ? "VAR" : "NULL"));
        
        // ✅ YAPı ÇEKİRDEĞİ GİBİ: Eğer item null ise fallback (olmamalı ama güvenlik için)
        if (clanFenceItem == null) {
            me.mami.stratocraft.Main.getInstance().getLogger().warning("[CLAN_FENCE_BREAK] clanFenceItem null, fallback oluşturuluyor");
            clanFenceItem = new ItemStack(Material.OAK_FENCE);
            org.bukkit.inventory.meta.ItemMeta meta = clanFenceItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6§lKlan Çiti");
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add("§7Klan bölgesi sınırlarını belirler.");
                meta.setLore(lore);
                
                org.bukkit.persistence.PersistentDataContainer container = meta.getPersistentDataContainer();
                org.bukkit.NamespacedKey customIdKey = new org.bukkit.NamespacedKey(me.mami.stratocraft.Main.getInstance(), "custom_id");
                container.set(customIdKey, org.bukkit.persistence.PersistentDataType.STRING, "CLAN_FENCE");
                
                clanFenceItem.setItemMeta(meta);
            }
        } else {
            // ✅ DÜZELTME: ClanId verisi EKLENMEYECEK - stacklenme için
            // ClanId verisi sadece CustomBlockData'da (PDC) tutulur
            // Item'da clanId verisi yok, bu yüzden tüm çitler stacklenebilir
            // Yerleştirme sırasında oyuncunun klanı kullanılır (onFencePlace'te)
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] clanFenceItem hazır: " + clanFenceItem.getType().name());
        }
        
        if (clanFenceItem != null) {
            // ✅ Özel item'ı drop et
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] dropItemNaturally çağrılıyor: " + block.getLocation());
            block.getWorld().dropItemNaturally(block.getLocation(), clanFenceItem);
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] dropItemNaturally tamamlandı");
        } else {
            me.mami.stratocraft.Main.getInstance().getLogger().severe("[CLAN_FENCE_BREAK] clanFenceItem hala null, drop edilemedi!");
        }
        
        // ✅ YAPı ÇEKİRDEĞİ GİBİ: CustomBlockData'dan temizle
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] removeClanFenceData() çağrılıyor");
        me.mami.stratocraft.util.CustomBlockData.removeClanFenceData(block);
        me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_BREAK] removeClanFenceData() tamamlandı");
    }
    
    /**
     * ✅ YENİ: BlockPlaceEvent'te ItemStack'ten veri geri yükleme
     * YAPı ÇEKİRDEĞİ GİBİ: Tüm özel bloklar için ItemStack'ten veri oku ve bloka yaz
     * ✅ KRİTİK: MONITOR priority kullan (blok artık dünyada olduğu için PDC'ye yazabiliriz)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
        
        // ✅ YAPı ÇEKİRDEĞİ GİBİ: Klan çiti kontrolü
        // ✅ DÜZELTME: onFencePlace zaten HIGH priority'de setClanFenceData() çağırıyor
        // Bu MONITOR event'i sadece ek güvence (double-check) için
        // onFencePlace'te runtime set edildiği için burada tekrar set etmeye gerek yok
        // Ama PDC yazımını garantilemek için burada da deneyebiliriz (idempotent)
        if (type == Material.OAK_FENCE) {
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE_RESTORE] OAK_FENCE tespit edildi (MONITOR priority)");
            // ✅ ÖNCE: isCustomItem kontrolü (yapı çekirdeği gibi)
            boolean isClanFenceItem = ItemManager.isCustomItem(item, "CLAN_FENCE");
            me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE_RESTORE] isClanFenceItem: " + isClanFenceItem);
            if (isClanFenceItem) {
                // ✅ MONITOR priority'de blok kesinlikle dünyada, PDC yazımını garantile
                // (onFencePlace HIGH priority'de PDC yazımı başarısız olmuşsa burada deneriz)
                me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE_RESTORE] setClanFenceData() çağrılıyor (MONITOR): " + block.getLocation());
                boolean setResult = me.mami.stratocraft.util.CustomBlockData.setClanFenceData(block);
                me.mami.stratocraft.Main.getInstance().getLogger().info("[CLAN_FENCE_PLACE_RESTORE] setClanFenceData() sonucu: " + setResult);
            }
        }
        
        // ✅ Yapı çekirdeği kontrolü
        // ✅ DÜZELTME: Item'dan veri okunmayacak (stacklenme için item'da veri yok)
        // Yapı çekirdeği yerleştirildiğinde oyuncunun UUID'si kullanılır (StructureCoreListener'da zaten yapılıyor)
        // Bu event sadece item kontrolü için, veri geri yükleme yapılmaz
        // Çünkü StructureCoreListener'da zaten CustomBlockData.setStructureCoreData() çağrılıyor
        if (type == Material.OAK_LOG) {
            // ✅ ÖNCE: isCustomItem kontrolü (yapı çekirdeği gibi)
            if (ItemManager.isCustomItem(item, "STRUCTURE_CORE")) {
                // ✅ DÜZELTME: Item'da owner verisi yok (stacklenme için)
                // Yerleştirme sırasında oyuncunun UUID'si kullanılır (StructureCoreListener'da zaten yapılıyor)
                // Bu event sadece item kontrolü için, veri geri yükleme yapılmaz
                // Çünkü StructureCoreListener'da zaten CustomBlockData.setStructureCoreData() çağrılıyor
            }
        }
        
        // ✅ Tuzak çekirdeği kontrolü
        // ✅ DÜZELTME: Item'dan veri okunmayacak (stacklenme için item'da veri yok)
        // Tuzak çekirdeği yerleştirildiğinde oyuncunun UUID'si kullanılır (TrapListener'da zaten yapılıyor)
        // Bu event sadece item kontrolü için, veri geri yükleme yapılmaz
        // Çünkü TrapListener'da zaten CustomBlockData.setTrapCoreData() çağrılıyor
        if (type == Material.LODESTONE) {
            // ✅ ÖNCE: isCustomItem kontrolü (tuzak çekirdeği gibi)
            if (ItemManager.isCustomItem(item, "TRAP_CORE")) {
                // ✅ DÜZELTME: Item'da owner verisi yok (stacklenme için)
                // Yerleştirme sırasında oyuncunun UUID'si kullanılır (TrapListener'da zaten yapılıyor)
                // Bu event sadece item kontrolü için, veri geri yükleme yapılmaz
                // Çünkü TrapListener'da zaten CustomBlockData.setTrapCoreData() çağrılıyor
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
        // ✅ DÜZELTME: Sadece boolean bayrak tutuluyor (clanId yok)
        if (type == Material.OAK_FENCE) {
            if (me.mami.stratocraft.util.CustomBlockData.isClanFence(block)) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("stratocraft", "clan_fence");
                container.set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                hasCustomData = true;
            }
        }
        
        // ✅ Yapı çekirdeği kontrolü - KALDIRILDI (kullanıcı isteği)
        // Yapı çekirdeğine sağ tıklayınca item verilmesi özelliği kaldırıldı
        
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
                            // ✅ DÜZELTME: Klan çiti artık sadece bayrak tutuyor (clanId yok)
                            // Chunk yüklendiğinde özel bir işlem yapmaya gerek yok
                            // Çünkü çitler sadece yerleştirildiğinde set ediliyor
                            
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
        // ✅ DÜZELTME: Debug log ekle (çit algılama sorununu tespit etmek için)
        me.mami.stratocraft.Main mainPlugin = me.mami.stratocraft.Main.getInstance();
        if (mainPlugin != null) {
            mainPlugin.getLogger().info("[CRYSTAL_PLACE] Çit kontrolü başlatılıyor... Center: " + finalPlaceLocation.getLocation());
        }
        
        boolean isValid = isSurroundedByClanFences3D(finalPlaceLocation);
        
        if (mainPlugin != null) {
            mainPlugin.getLogger().info("[CRYSTAL_PLACE] Çit kontrolü sonucu: " + isValid + " (Center: " + finalPlaceLocation.getLocation() + ")");
        }
        
        if (!isValid) {
            finalPlayer.sendMessage("§cKlan Kristali sadece §6Klan Çitleri §cile tamamen çevrelenmiş güvenli bir alana kurulabilir!");
            finalPlayer.sendMessage("§7Lütfen çitlerin doğru şekilde yerleştirildiğinden ve klan çiti olduğundan emin olun.");
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
    
    // ✅ DÜZELTME: Klan Çiti “çevreleme” kontrolü (2.5D Flood Fill)
    //
    // Problem: Tam 3D flood-fill (UP/DOWN ile) çitlerin ÜSTÜNDEN kaçabiliyor.
    // Çünkü çitler yalnızca kendi blok koordinatında (ör. y=86) var; y=88 gibi seviyelerde
    // “çit üstü hava”dan dışarı sızma olabiliyor ve bu da maxIterations ile false’a düşürüyor.
    //
    // Çözüm: Sadece yatay (N/S/E/W) arama yap.
    // Ayrıca “çitler genelde zeminde, iç alan ise 1 blok üstte hava” olduğu için,
    // yatay komşuyu kontrol ederken hem aranan Y seviyesinde (scanY) hem de 1 blok altında (scanY-1)
    // çit var mı diye bak.
    private boolean isSurroundedByClanFences3D(Block center) {
        me.mami.stratocraft.Main.getInstance().getLogger()
            .info("[isSurroundedByClanFences3D] Başlangıç (2.5D) - Center: " + center.getLocation());

        if (center == null || center.getWorld() == null) {
            me.mami.stratocraft.Main.getInstance().getLogger()
                .warning("[isSurroundedByClanFences3D] Center null/world null, return false");
            return false;
        }

        // İç alan taramasını mümkünse havada başlat (zemin üstü 1 blok)
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

        // Bu kontrolün “makul” sınırı (lag önlemek için). 64 blok yarıçap = 4 chunk.
        final int maxRadius = 64;
        final int minArea = 9; // 3x3
        final int maxIterations = (2 * maxRadius + 1) * (2 * maxRadius + 1); // 2D alan üst limiti

        me.mami.stratocraft.Main.getInstance().getLogger().info(
            "[isSurroundedByClanFences3D] scanY=" + scanY + ", maxRadius=" + maxRadius + ", maxIterations=" + maxIterations);

        java.util.Set<Long> visited = new java.util.HashSet<>();
        java.util.Queue<Block> queue = new java.util.LinkedList<>();
        boolean foundClanFence = false;
        
        queue.add(start);
        visited.add(packCoords(start));
        
        int iterations = 0;
        
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            if (iterations > maxIterations) {
                me.mami.stratocraft.Main.getInstance().getLogger().info(
                    "[isSurroundedByClanFences3D] maxIterations aşıldı (alan açık ya da çok büyük). iterations=" + iterations);
                return false;
            }
            
            for (BlockFace face : faces) {
                int nx = current.getX() + face.getModX();
                int nz = current.getZ() + face.getModZ();
                
                // Yarıçap sınırı (çitle çevrili değilse dışarı sızmayı hızlı yakalar)
                if (Math.abs(nx - centerX) > maxRadius || Math.abs(nz - centerZ) > maxRadius) {
                    me.mami.stratocraft.Main.getInstance().getLogger().info(
                        "[isSurroundedByClanFences3D] maxRadius dışına kaçış tespit edildi -> açık alan. nx=" + nx + " nz=" + nz);
                    return false;
                }

                Block neighbor = center.getWorld().getBlockAt(nx, scanY, nz);
                long neighborKey = packCoords(neighbor);
                if (visited.contains(neighborKey)) continue;

                // ✅ Çit bariyeri kontrolü: scanY ve scanY-1 seviyesinde bak
                Block fenceAtY = neighbor.getType() == Material.OAK_FENCE ? neighbor : null;
                Block fenceBelow = center.getWorld().getBlockAt(nx, scanY - 1, nz);
                if (fenceBelow.getType() == Material.OAK_FENCE) {
                    // Eğer scanY’de de çit varsa, aşağıya bakmaya gerek yok; ama ikisini de destekleyelim
                    if (fenceAtY == null) fenceAtY = fenceBelow;
                }

                if (fenceAtY != null && fenceAtY.getType() == Material.OAK_FENCE) {
                    me.mami.stratocraft.Main.getInstance().getLogger().info(
                        "[isSurroundedByClanFences3D] OAK_FENCE bulundu (barrier) @ " + fenceAtY.getLocation());

                    boolean isClanFence = isClanFenceFast(fenceAtY);
                    me.mami.stratocraft.Main.getInstance().getLogger().info(
                        "[isSurroundedByClanFences3D] isClanFenceFast sonucu: " + isClanFence + " @ " + fenceAtY.getLocation());

                    if (!isClanFence) {
                        // Normal çit varsa alan geçersiz
                        me.mami.stratocraft.Main.getInstance().getLogger().info(
                            "[isSurroundedByClanFences3D] Normal çit bulundu -> geçersiz alan: " + fenceAtY.getLocation());
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
        
        boolean result = visited.size() >= minArea && foundClanFence;
        me.mami.stratocraft.Main.getInstance().getLogger().info(
            "[isSurroundedByClanFences3D] Bitiş (2.5D) - visited.size=" + visited.size() +
            ", minArea=" + minArea + ", foundClanFence=" + foundClanFence + ", result=" + result);
        return result;
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
        me.mami.stratocraft.Main.getInstance().getLogger().info("[isClanFenceFast] Başlangıç - Block: " + (block != null ? block.getType().name() + " @ " + block.getLocation() : "NULL"));
        
        if (block.getType() != Material.OAK_FENCE) {
            me.mami.stratocraft.Main.getInstance().getLogger().info("[isClanFenceFast] OAK_FENCE değil, return false");
            return false;
        }
        
        // ✅ DÜZELTME: Sadece boolean bayrak kontrol ediliyor (clanId yok)
        boolean result = me.mami.stratocraft.util.CustomBlockData.isClanFence(block);
        me.mami.stratocraft.Main.getInstance().getLogger().info("[isClanFenceFast] isClanFence() sonucu: " + result + " @ " + block.getLocation());
        return result;
    }
    
    // ========== KLAN KRISTALİ KIRILMA ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalBreak(EntityDamageEvent event) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        Location crystalLoc = crystal.getLocation();
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL KIRMA] Event tetiklendi - Kristal: " + crystal.getUniqueId() + 
                " @ " + crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ() +
                ", Final Damage: " + event.getFinalDamage() + ", Cancelled: " + event.isCancelled());
        }
        
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
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL KIRMA] findClanByCrystal sonucu: " + 
                (owner != null ? owner.getName() + " (ID: " + owner.getId() + ")" : "null"));
        }
        
        if (owner == null) {
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL KIRMA] Normal end crystal, işlem yapılmıyor");
            }
            return; // Normal end crystal
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL KIRMA] Klan bulundu: " + owner.getName() + 
                ", hasCrystal: " + owner.hasCrystal() + 
                ", crystalEntity: " + (owner.getCrystalEntity() != null ? owner.getCrystalEntity().getUniqueId() : "null") +
                ", crystalLocation: " + (owner.getCrystalLocation() != null ? owner.getCrystalLocation().toString() : "null"));
        }
        
        // Kristal kırılıyor mu? (EnderCrystal'ın sağlığı 1.0, yeterli hasar aldığında kırılır)
        // EnderCrystal'da getHealth() yok ama hasar >= 1.0 olduğunda kırılır
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL KIRMA] Hasar kontrolü - Final Damage: " + event.getFinalDamage() + 
                ", Cancelled: " + event.isCancelled() + ", Kırılma koşulu: " + (event.getFinalDamage() >= 1.0 && !event.isCancelled()));
        }
        
        if (event.getFinalDamage() >= 1.0 && !event.isCancelled()) {
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL KIRMA] Kristal kırılıyor! İşlem başlatılıyor...");
            }
            // Kırılma nedenini kontrol et
            Player breaker = null;
            Entity damager = null;
            if (event instanceof EntityDamageByEntityEvent) {
                damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    breaker = (Player) damager;
                } else if (damager instanceof org.bukkit.entity.Projectile) {
                    // ✅ YENİ: Ender pearl, kartopu vb. projectile'lar için shooter'ı bul
                    org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) damager;
                    if (projectile.getShooter() instanceof Player) {
                        breaker = (Player) projectile.getShooter();
                    }
                }
            }
            
            // Felaket entity'si kristali kırıyor mu? (DisasterTask'tan geliyor)
            // Felaket entity'leri için özel durum - klanı dağıt
            if (damager != null && !(damager instanceof Player) && !(damager instanceof org.bukkit.entity.Projectile)) {
                // Bu bir felaket entity'si olabilir - DisasterTask zaten klanı dağıtacak
                // Burada sadece event'i işle, klan dağıtma DisasterTask'ta yapılıyor
                return; // DisasterTask zaten işleyecek
            }
            
            // ✅ YENİ: Lider kendi kristalini kırıyor mu?
            if (breaker != null && owner.getRank(breaker.getUniqueId()) == Clan.Rank.LEADER) {
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Lider kendi kristalini kırıyor - Klan dağıtılıyor: " + owner.getName());
                }
                
                // YENİ: Klan alanı korumasını kaldır ve sınırları temizle
                owner.setCrystalLocation(null);
                owner.setCrystalEntity(null);
                owner.setHasCrystal(false);
                if (boundaryManager != null) {
                    boundaryManager.removeTerritoryData(owner);
                }
                
                // Lider klanı bozdu
                territoryManager.getClanManager().disbandClan(owner);
                territoryManager.setCacheDirty(); // Cache'i güncelle
                breaker.sendMessage("§cKlanınız dağıtıldı!");
                crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Lider tarafından klan dağıtıldı: " + owner.getName());
                }
                return;
            }
            
            // ✅ YENİ: Kuşatma var mı? - Sadece bu oyuncunun klanıyla savaşta ise
            if (breaker != null) {
                Clan attacker = territoryManager.getClanManager().getClanByPlayer(breaker.getUniqueId());
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Breaker klanı: " + 
                        (attacker != null ? attacker.getName() : "null") + 
                        ", Owner: " + owner.getName() + 
                        ", Savaş durumu: " + (attacker != null && owner.isAtWarWith(attacker.getId())));
                }
                
                if (attacker != null && !attacker.equals(owner) && owner.isAtWarWith(attacker.getId())) {
                    if (plugin != null) {
                        plugin.getLogger().info("[KRISTAL KIRMA] Savaşta kristal kırıldı - Klan dağıtılıyor: " + owner.getName());
                    }
                    
                    // Savaşta kristal kırıldı - klan bozuldu
                    siegeManager.endSiege(attacker, owner);
                    breaker.sendMessage("§6§lZAFER! Düşman kristalini parçaladın.");
                    // YENİ: Klan alanı korumasını kaldır ve sınırları temizle
                    owner.setCrystalLocation(null);
                    owner.setCrystalEntity(null);
                    owner.setHasCrystal(false);
                    if (boundaryManager != null) {
                        boundaryManager.removeTerritoryData(owner);
                    }
                    
                    territoryManager.getClanManager().disbandClan(owner);
                    territoryManager.setCacheDirty(); // Cache'i güncelle
                    crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                    
                    if (plugin != null) {
                        plugin.getLogger().info("[KRISTAL KIRMA] Savaş sonucu klan dağıtıldı: " + owner.getName());
                    }
                    return;
                }
            }
            
            // ✅ YENİ: Normal durumda (ender pearl, kartopu vb. ile kırılıyorsa) klanı dağıt
            // Klan kristali yok olunca klan da yok olmalı
            if (breaker != null) {
                Clan playerClan = territoryManager.getClanManager().getClanByPlayer(breaker.getUniqueId());
                
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Breaker kontrolü - PlayerClan: " + 
                        (playerClan != null ? playerClan.getName() : "null") + 
                        ", Owner: " + owner.getName() + 
                        ", Aynı klan: " + (playerClan != null && playerClan.equals(owner)) +
                        ", Breaker rütbesi: " + (playerClan != null ? owner.getRank(breaker.getUniqueId()) : "null"));
                }
                
                // Eğer atan oyuncu lider değilse veya farklı bir klan üyesiyse, klanı dağıt
                if (playerClan == null || !playerClan.equals(owner) || owner.getRank(breaker.getUniqueId()) != Clan.Rank.LEADER) {
                    if (plugin != null) {
                        plugin.getLogger().info("[KRISTAL KIRMA] Normal durum - Klan dağıtılıyor: " + owner.getName() + 
                            " (Breaker: " + breaker.getName() + ")");
                    }
                    
                    // Klan kristali yok oldu - klan da yok olmalı
                    owner.setCrystalLocation(null);
                    owner.setCrystalEntity(null);
                    owner.setHasCrystal(false);
                    if (boundaryManager != null) {
                        boundaryManager.removeTerritoryData(owner);
                    }
                    
                    territoryManager.getClanManager().disbandClan(owner);
                    territoryManager.setCacheDirty(); // Cache'i güncelle
                    
                    // Tüm klan üyelerine mesaj gönder
                    for (UUID memberId : owner.getMembers().keySet()) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.sendMessage("§c§lKLAN KRISTALİ YOK OLDU!");
                            member.sendMessage("§7Klanınız dağıtıldı.");
                        }
                    }
                    
                    if (breaker != null) {
                        breaker.sendMessage("§6§lZAFER! Düşman klan kristalini yok ettin!");
                    }
                    
                    crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                    
                    if (plugin != null) {
                        plugin.getLogger().info("[KRISTAL KIRMA] Normal durum - Klan dağıtıldı: " + owner.getName());
                    }
                    return;
                }
                
                // ✅ Lider kristali kırıyor - özel item drop EntityDeathEvent'te yapılacak
                // Not: EnderCrystal entity olduğu için BlockBreakEvent değil, EntityDeathEvent kullanılacak
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Lider kendi kristalini kırıyor - Item drop EntityDeathEvent'te yapılacak");
                }
            } else {
                // ✅ YENİ: Doğal hasar veya bilinmeyen neden (ender pearl/kartopu shooter yok)
                // Klan kristali yok oldu - klan da yok olmalı
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Breaker null - Doğal hasar veya bilinmeyen neden, klan dağıtılıyor: " + owner.getName());
                }
                
                owner.setCrystalLocation(null);
                owner.setCrystalEntity(null);
                owner.setHasCrystal(false);
                if (boundaryManager != null) {
                    boundaryManager.removeTerritoryData(owner);
                }
                
                territoryManager.getClanManager().disbandClan(owner);
                territoryManager.setCacheDirty(); // Cache'i güncelle
                
                // Tüm klan üyelerine mesaj gönder
                for (UUID memberId : owner.getMembers().keySet()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§c§lKLAN KRISTALİ YOK OLDU!");
                        member.sendMessage("§7Klanınız dağıtıldı.");
                    }
                }
                
                crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL KIRMA] Doğal hasar - Klan dağıtıldı: " + owner.getName());
                }
                // Event'i cancel etme - kristal yok olmalı
            }
        } else {
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL KIRMA] Kristal kırılmıyor - Final Damage: " + event.getFinalDamage() + 
                    " < 1.0 veya Event cancelled: " + event.isCancelled() + 
                    ", Owner: " + (owner != null ? owner.getName() : "null"));
            }
        }
    }
    
    /**
     * ✅ YENİ: EnderCrystal öldüğünde özel item drop et (CLAN_CRYSTAL)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCrystalDeath(EntityDeathEvent event) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        Location crystalLoc = crystal.getLocation();
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL ÖLÜM] Event tetiklendi - Kristal: " + crystal.getUniqueId() + 
                " @ " + crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
        }
        
        // Bu kristal bir klan kristali mi?
        Clan owner = findClanByCrystal(crystal);
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL ÖLÜM] findClanByCrystal sonucu: " + 
                (owner != null ? owner.getName() + " (ID: " + owner.getId() + ")" : "null"));
        }
        
        if (owner == null) {
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL ÖLÜM] Normal end crystal, item drop yapılmıyor");
            }
            return; // Normal end crystal
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL ÖLÜM] Klan bulundu: " + owner.getName() + 
                ", hasCrystal: " + owner.hasCrystal() + 
                ", crystalEntity: " + (owner.getCrystalEntity() != null ? owner.getCrystalEntity().getUniqueId() : "null") +
                ", crystalEntity equals: " + (owner.getCrystalEntity() != null && owner.getCrystalEntity().equals(crystal)));
        }
        
        // ✅ YENİ: Eğer klan zaten dağıtıldıysa (crystal entity null ise), item drop etme
        // onCrystalBreak'te klan dağıtıldıysa, burada item drop etmemeliyiz
        // Not: onCrystalBreak HIGH priority'de çalışıyor, bu HIGH priority'de çalışıyor
        // Ama onCrystalBreak'te event cancel edilmediği için onCrystalDeath de tetiklenir
        // Bu durumda klan zaten dağıtılmış olabilir, kontrol et
        if (owner.getCrystalEntity() == null || !owner.hasCrystal() || 
            owner.getCrystalEntity() != crystal) {
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL ÖLÜM] Klan zaten dağıtıldı veya farklı kristal - Item drop yapılmıyor");
            }
            // Klan zaten dağıtıldı veya farklı bir kristal, item drop etme
            event.getDrops().clear();
            return;
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL ÖLÜM] Item drop yapılıyor - Klan: " + owner.getName());
        }
        
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
        // crystalLoc zaten yukarıda tanımlanmış (satır 1883)
        if (crystalLoc != null) {
            org.bukkit.block.Block block = crystalLoc.getBlock();
            if (block != null && block.getType() == Material.END_CRYSTAL) {
                me.mami.stratocraft.util.CustomBlockData.removeClanCrystalData(block);
            }
        }
    }
    
    // Kristal entity'sine göre klanı bul
    private Clan findClanByCrystal(EnderCrystal crystal) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        Location crystalLoc = crystal.getLocation();
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL BULMA] findClanByCrystal başlatıldı - Kristal: " + crystal.getUniqueId() + 
                " @ " + crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ());
        }
        
        // ✅ DÜZELTME: Metadata kontrolü opsiyonel - metadata yoksa location kontrolü yeterli
        boolean hasMetadata = false;
        if (territoryConfig != null) {
            String metadataKey = territoryConfig.getCrystalMetadataKey();
            hasMetadata = crystal.hasMetadata(metadataKey);
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL BULMA] Metadata kontrolü - Key: " + metadataKey + ", Has Metadata: " + hasMetadata);
            }
        }
        
        // ✅ DÜZELTME: Sunucu restart sonrası crystalEntity null olabilir, location kontrolü de yap
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (plugin != null) {
                plugin.getLogger().info("[KRISTAL BULMA] Klan kontrol ediliyor: " + clan.getName() + 
                    ", hasCrystal: " + clan.hasCrystal() + 
                    ", crystalLocation: " + (clan.getCrystalLocation() != null ? clan.getCrystalLocation().toString() : "null") +
                    ", crystalEntity: " + (clan.getCrystalEntity() != null ? clan.getCrystalEntity().getUniqueId() : "null"));
            }
            
            // Önce entity referansına bak
            if (clan.getCrystalEntity() != null && clan.getCrystalEntity().equals(crystal)) {
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL BULMA] Entity referansı ile bulundu: " + clan.getName());
                }
                return clan;
            }
            
            // Entity referansı null ise location kontrolü yap
            // ✅ DÜZELTME: hasCrystal() kontrolü gereksiz - crystalLocation varsa yeterli
            // (hasCrystal() metodu zaten crystalLocation kontrolü yapıyor)
            Location clanCrystalLoc = clan.getCrystalLocation();
            if (clanCrystalLoc != null) {
                // Location'ları karşılaştır (blok seviyesinde)
                boolean locationMatch = clanCrystalLoc.getBlockX() == crystalLoc.getBlockX() &&
                    clanCrystalLoc.getBlockY() == crystalLoc.getBlockY() &&
                    clanCrystalLoc.getBlockZ() == crystalLoc.getBlockZ() &&
                    clanCrystalLoc.getWorld().equals(crystalLoc.getWorld());
                
                if (plugin != null) {
                    plugin.getLogger().info("[KRISTAL BULMA] Location karşılaştırması: " + 
                        "Clan: " + clanCrystalLoc.getBlockX() + "," + clanCrystalLoc.getBlockY() + "," + clanCrystalLoc.getBlockZ() +
                        " vs Crystal: " + crystalLoc.getBlockX() + "," + crystalLoc.getBlockY() + "," + crystalLoc.getBlockZ() +
                        ", Match: " + locationMatch);
                }
                
                if (locationMatch) {
                    if (plugin != null) {
                        plugin.getLogger().info("[KRISTAL BULMA] Location kontrolü ile bulundu: " + clan.getName() + 
                            " (Entity referansı null, location eşleşti)");
                    }
                    
                    // ✅ ÖNEMLİ: Entity referansını güncelle (sunucu restart sonrası)
                    clan.setCrystalEntity(crystal);
                    
                    // ✅ ÖNEMLİ: Metadata ekle (eğer yoksa)
                    if (territoryConfig != null && !hasMetadata) {
                        String metadataKey = territoryConfig.getCrystalMetadataKey();
                        crystal.setMetadata(metadataKey, new org.bukkit.metadata.FixedMetadataValue(
                            me.mami.stratocraft.Main.getInstance(), true));
                        if (plugin != null) {
                            plugin.getLogger().info("[KRISTAL BULMA] Metadata eklendi: " + clan.getName());
                        }
                    }
                    
                    return clan;
                }
            }
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[KRISTAL BULMA] Klan bulunamadı - Normal end crystal olabilir veya location eşleşmedi");
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

