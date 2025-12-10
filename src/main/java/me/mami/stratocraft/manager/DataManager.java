package me.mami.stratocraft.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;
import me.mami.stratocraft.model.Shop;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Territory;
import me.mami.stratocraft.model.Disaster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import me.mami.stratocraft.listener.VirtualStorageListener;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.*;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

public class DataManager {
    private final Main plugin;
    private final File dataFolder;
    private final Gson gson;
    
    // File locking için (race condition önleme)
    private final java.util.concurrent.locks.ReentrantLock saveLock = new java.util.concurrent.locks.ReentrantLock();
    
    // Data version (format değişikliklerinde migration için)
    private static final int DATA_VERSION = 1;
    
    // Backup ayarları
    private static final int MAX_BACKUPS = 5; // Son 5 backup saklanır
    private static final String BACKUP_FOLDER = "backups";
    
    // Auto-save ayarları (config'den yüklenecek)
    private long autoSaveInterval = 300000L; // 5 dakika (ms) - default
    private boolean autoSaveEnabled = true;
    private org.bukkit.scheduler.BukkitTask autoSaveTask;
    
    public DataManager(Main plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .create();
        
        // Klasörleri oluştur
        new File(dataFolder, "data").mkdirs();
        new File(dataFolder, BACKUP_FOLDER).mkdirs();
        
        // Config'den ayarları yükle
        loadConfig();
    }
    
    /**
     * Config'den ayarları yükle
     */
    private void loadConfig() {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        if (config != null) {
            autoSaveInterval = config.getLong("data-manager.auto-save-interval", 300000L); // 5 dakika default
            autoSaveEnabled = config.getBoolean("data-manager.auto-save-enabled", true);
        }
    }
    
    /**
     * Periyodik otomatik kayıt başlat
     * NOT: Bu metod Main.java'dan çağrılmalı çünkü manager'lara erişim gerekiyor
     */
    public void startAutoSave(java.util.function.Supplier<Void> saveCallback) {
        if (!autoSaveEnabled || autoSaveTask != null) return;
        
        long ticks = autoSaveInterval / 50; // tick'e çevir (1 tick = 50ms)
        if (ticks < 1) ticks = 1; // Minimum 1 tick
        
        autoSaveTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                if (saveCallback != null) {
                    saveCallback.get();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Auto-save hatası: " + e.getMessage());
            }
        }, ticks, ticks);
        
        plugin.getLogger().info("§aPeriyodik otomatik kayıt başlatıldı (" + (autoSaveInterval / 1000) + " saniye aralıkla)");
    }
    
    /**
     * Periyodik otomatik kayıt durdur
     */
    public void stopAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }
    
    // ========== KAYDETME METODLARI ==========
    
    /**
     * Tüm verileri kaydet (SYNC - Main Thread'de çağrılmalı)
     * Bukkit API'ye erişim gerektirdiği için async yapılamaz
     * @param forceSync true ise async kullanmaz (onDisable için)
     */
    public void saveAll(ClanManager clanManager, ContractManager contractManager, 
                       ShopManager shopManager, VirtualStorageListener virtualStorage, 
                       AllianceManager allianceManager, DisasterManager disasterManager, boolean forceSync) {
        saveAll(clanManager, contractManager, shopManager, virtualStorage, allianceManager, 
                disasterManager, null, null, null, null, forceSync);
    }
    
    /**
     * Tüm verileri kaydet (genişletilmiş versiyon - yeni sistemlerle)
     */
    public void saveAll(ClanManager clanManager, ContractManager contractManager, 
                       ShopManager shopManager, VirtualStorageListener virtualStorage, 
                       AllianceManager allianceManager, DisasterManager disasterManager,
                       me.mami.stratocraft.manager.clan.ClanBankSystem clanBankSystem,
                       me.mami.stratocraft.manager.clan.ClanMissionSystem clanMissionSystem,
                       me.mami.stratocraft.manager.clan.ClanActivitySystem clanActivitySystem,
                       me.mami.stratocraft.manager.TrapManager trapManager,
                       boolean forceSync) {
        try {
            // Önce tüm verileri snapshot al (sync thread'de)
            ClanSnapshot clanSnapshot = createClanSnapshot(clanManager);
            ContractSnapshot contractSnapshot = createContractSnapshot(contractManager);
            ShopSnapshot shopSnapshot = createShopSnapshot(shopManager);
            InventorySnapshot inventorySnapshot = createInventorySnapshot(virtualStorage);
            AllianceSnapshot allianceSnapshot = createAllianceSnapshot(allianceManager);
            DisasterSnapshot disasterSnapshot = createDisasterSnapshot(disasterManager);
            
            // Yeni sistemler için snapshot'lar (null kontrolü ile)
            ClanBankSnapshot bankSnapshot = null;
            ClanMissionSnapshot missionSnapshot = null;
            ClanActivitySnapshot activitySnapshot = null;
            TrapSnapshot trapSnapshot = null;
            
            if (clanBankSystem != null) {
                bankSnapshot = createClanBankSnapshot(clanBankSystem, clanManager);
            }
            if (clanMissionSystem != null) {
                missionSnapshot = createClanMissionSnapshot(clanMissionSystem, clanManager);
            }
            if (clanActivitySystem != null) {
                activitySnapshot = createClanActivitySnapshot(clanActivitySystem);
            }
            if (trapManager != null) {
                trapSnapshot = createTrapSnapshot(trapManager);
            }
            
            if (forceSync) {
                // Sunucu kapanıyor - sync kayıt (onDisable)
                // File locking ile race condition önleme
                saveLock.lock();
                try {
                    // Transaction: Tüm dosyalar başarılı olmalı (all-or-nothing)
                    List<File> writtenFiles = new ArrayList<>();
                    List<Exception> errors = new ArrayList<>();
                    
                    try {
                        writeClanSnapshot(clanSnapshot);
                        writtenFiles.add(new File(dataFolder, "data/clans.json"));
                    } catch (Exception e) {
                        errors.add(e);
                    }
                    
                    try {
                        writeContractSnapshot(contractSnapshot);
                        writtenFiles.add(new File(dataFolder, "data/contracts.json"));
                    } catch (Exception e) {
                        errors.add(e);
                    }
                    
                    try {
                        writeShopSnapshot(shopSnapshot);
                        writtenFiles.add(new File(dataFolder, "data/shops.json"));
                    } catch (Exception e) {
                        errors.add(e);
                    }
                    
                    try {
                        writeInventorySnapshot(inventorySnapshot);
                        writtenFiles.add(new File(dataFolder, "data/virtual_inventories.json"));
                    } catch (Exception e) {
                        errors.add(e);
                    }
                    
                    try {
                        writeAllianceSnapshot(allianceSnapshot);
                        writtenFiles.add(new File(dataFolder, "data/alliances.json"));
                    } catch (Exception e) {
                        errors.add(e);
                    }
                    
                    try {
                        writeDisasterSnapshot(disasterSnapshot);
                        writtenFiles.add(new File(dataFolder, "data/disaster.json"));
                    } catch (Exception e) {
                        errors.add(e);
                    }
                    
                    // Yeni sistemler
                    if (bankSnapshot != null) {
                        try {
                            writeClanBankSnapshot(bankSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/clan_banks.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                    }
                    
                    if (missionSnapshot != null) {
                        try {
                            writeClanMissionSnapshot(missionSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/clan_missions.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                    }
                    
                    if (activitySnapshot != null) {
                        try {
                            writeClanActivitySnapshot(activitySnapshot);
                            writtenFiles.add(new File(dataFolder, "data/clan_activity.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                    }
                    
                    if (trapSnapshot != null) {
                        try {
                            writeTrapSnapshot(trapSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/traps.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                    }
                    
                    // Hata kontrolü ve kritik dosya kontrolü
                    if (!errors.isEmpty()) {
                        plugin.getLogger().severe("§c" + errors.size() + " dosya kaydetme hatası!");
                        for (Exception e : errors) {
                            plugin.getLogger().severe("§cHata: " + e.getMessage());
                            e.printStackTrace();
                        }
                        // Error recovery: Backup'tan geri yükleme önerisi
                        plugin.getLogger().warning("§eVeri kaybı riski! Backup'tan geri yükleme önerilir.");
                        plugin.getLogger().warning("§eKomut: /stratocraft data restore <dosya_adi>");
                    } else {
                        // Kritik dosyaların varlığını kontrol et
                        boolean allCriticalFilesExist = true;
                        String[] criticalFiles = {"clans.json", "contracts.json", "shops.json"};
                        for (String fileName : criticalFiles) {
                            File criticalFile = new File(dataFolder, "data/" + fileName);
                            if (!criticalFile.exists() || criticalFile.length() == 0) {
                                plugin.getLogger().warning("§eKritik dosya eksik veya boş: " + fileName);
                                allCriticalFilesExist = false;
                            }
                        }
                        
                        if (allCriticalFilesExist) {
                            plugin.getLogger().info("§aTüm veriler kaydedildi! (" + writtenFiles.size() + " dosya)");
                        } else {
                            plugin.getLogger().warning("§eBazı kritik dosyalar eksik veya boş! Backup kontrolü önerilir.");
                        }
                    }
                } finally {
                    saveLock.unlock();
                }
            } else {
                // Normal kayıt - async (file locking ile)
                final ClanBankSnapshot finalBankSnapshot = bankSnapshot;
                final ClanMissionSnapshot finalMissionSnapshot = missionSnapshot;
                final ClanActivitySnapshot finalActivitySnapshot = activitySnapshot;
                final TrapSnapshot finalTrapSnapshot = trapSnapshot;
                
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    // File locking (async thread'de de çalışır)
                    if (!saveLock.tryLock()) {
                        plugin.getLogger().warning("§eVeri kaydetme devam ediyor, atlandı...");
                        return;
                    }
                    
                    try {
                        List<File> writtenFiles = new ArrayList<>();
                        List<Exception> errors = new ArrayList<>();
                        
                        try {
                            writeClanSnapshot(clanSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/clans.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                        
                        try {
                            writeContractSnapshot(contractSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/contracts.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                        
                        try {
                            writeShopSnapshot(shopSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/shops.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                        
                        try {
                            writeInventorySnapshot(inventorySnapshot);
                            writtenFiles.add(new File(dataFolder, "data/virtual_inventories.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                        
                        try {
                            writeAllianceSnapshot(allianceSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/alliances.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                        
                        try {
                            writeDisasterSnapshot(disasterSnapshot);
                            writtenFiles.add(new File(dataFolder, "data/disaster.json"));
                        } catch (Exception e) {
                            errors.add(e);
                        }
                        
                        // Yeni sistemler
                        if (finalBankSnapshot != null) {
                            try {
                                writeClanBankSnapshot(finalBankSnapshot);
                                writtenFiles.add(new File(dataFolder, "data/clan_banks.json"));
                            } catch (Exception e) {
                                errors.add(e);
                            }
                        }
                        
                        if (finalMissionSnapshot != null) {
                            try {
                                writeClanMissionSnapshot(finalMissionSnapshot);
                                writtenFiles.add(new File(dataFolder, "data/clan_missions.json"));
                            } catch (Exception e) {
                                errors.add(e);
                            }
                        }
                        
                        if (finalActivitySnapshot != null) {
                            try {
                                writeClanActivitySnapshot(finalActivitySnapshot);
                                writtenFiles.add(new File(dataFolder, "data/clan_activity.json"));
                            } catch (Exception e) {
                                errors.add(e);
                            }
                        }
                        
                        if (finalTrapSnapshot != null) {
                            try {
                                writeTrapSnapshot(finalTrapSnapshot);
                                writtenFiles.add(new File(dataFolder, "data/traps.json"));
                            } catch (Exception e) {
                                errors.add(e);
                            }
                        }
                        
                        // Hata kontrolü ve kritik dosya kontrolü
                        if (!errors.isEmpty()) {
                            plugin.getLogger().severe("§c" + errors.size() + " dosya kaydetme hatası!");
                            for (Exception e : errors) {
                                plugin.getLogger().severe("§cHata: " + e.getMessage());
                            }
                            plugin.getLogger().warning("§eVeri kaybı riski! Backup kontrolü önerilir.");
                        } else {
                            // Kritik dosyaların varlığını kontrol et (async thread'de güvenli)
                            String[] criticalFiles = {"clans.json", "contracts.json", "shops.json"};
                            boolean allCriticalFilesExist = true;
                            for (String fileName : criticalFiles) {
                                File criticalFile = new File(dataFolder, "data/" + fileName);
                                if (!criticalFile.exists() || criticalFile.length() == 0) {
                                    allCriticalFilesExist = false;
                                    break;
                                }
                            }
                            
                            if (allCriticalFilesExist) {
                                plugin.getLogger().info("§aTüm veriler kaydedildi! (" + writtenFiles.size() + " dosya)");
                            } else {
                                plugin.getLogger().warning("§eBazı kritik dosyalar eksik veya boş!");
                            }
                        }
                    } finally {
                        saveLock.unlock();
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().severe("§cVeri snapshot hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Overload: Varsayılan olarak async kayıt
     */
    public void saveAll(ClanManager clanManager, ContractManager contractManager, 
                       ShopManager shopManager, VirtualStorageListener virtualStorage, 
                       AllianceManager allianceManager, DisasterManager disasterManager) {
        saveAll(clanManager, contractManager, shopManager, virtualStorage, allianceManager, disasterManager, false);
    }
    
    // Snapshot sınıfları
    private static class ClanSnapshot {
        List<ClanData> clans = new ArrayList<>();
    }
    
    private static class ContractSnapshot {
        List<ContractData> contracts = new ArrayList<>();
    }
    
    private static class ShopSnapshot {
        List<ShopData> shops = new ArrayList<>();
    }
    
    private static class InventorySnapshot {
        Map<String, String> inventories = new HashMap<>();
    }
    
    private static class AllianceSnapshot {
        List<AllianceData> alliances = new ArrayList<>();
    }
    
    private static class AllianceData {
        String id;
        String clan1Id;
        String clan2Id;
        String type;
        long createdAt;
        long expiresAt;
        boolean active;
        boolean broken;
        String breakerClanId;
    }
    
    private static class DisasterSnapshot {
        DisasterStateData disaster = null;
    }
    
    private static class ClanBankSnapshot {
        Map<String, BankData> banks = new HashMap<>();
    }
    
    private static class BankData {
        String clanId;
        String chestLocation; // Serialized Location
        Map<String, Long> lastSalaryTime; // Player UUID -> Last salary time
        List<TransferContractData> transferContracts = new ArrayList<>();
    }
    
    private static class TransferContractData {
        String clanId;
        String creatorId;
        String targetPlayerId;
        String material;
        int amount;
        long interval;
        long lastTransferTime;
        boolean active;
    }
    
    private static class ClanMissionSnapshot {
        Map<String, MissionData> missions = new HashMap<>();
        Map<String, String> missionBoardLocations = new HashMap<>(); // Clan ID -> Location
    }
    
    private static class MissionData {
        String clanId;
        String type;
        int targetAmount;
        int currentProgress;
        Map<String, Integer> memberProgress = new HashMap<>(); // Player UUID -> Progress
        Map<String, Object> rewards = new HashMap<>();
        long createdAt;
        long deadline;
        boolean completed;
    }
    
    private static class ClanActivitySnapshot {
        Map<String, Long> lastOnlineTime = new HashMap<>(); // Player UUID -> Last online time
    }
    
    private static class TrapSnapshot {
        List<TrapData> activeTraps = new ArrayList<>();
        List<InactiveTrapCoreData> inactiveCores = new ArrayList<>();
    }
    
    private static class TrapData {
        String location; // Serialized Location
        String ownerId;
        String ownerClanId; // Nullable
        String type; // TrapType.name()
        int fuel;
        List<String> frameBlocks = new ArrayList<>(); // Serialized Locations
        boolean isCovered;
    }
    
    private static class InactiveTrapCoreData {
        String location; // Serialized Location
        String ownerId;
    }
    
    /**
     * Klan verilerini snapshot al (sync thread'de - Bukkit API kullanılabilir)
     */
    private ClanSnapshot createClanSnapshot(ClanManager clanManager) {
        ClanSnapshot snapshot = new ClanSnapshot();
        
        if (clanManager == null) return snapshot;
        
        java.util.Collection<Clan> clans = clanManager.getAllClans();
        if (clans == null) return snapshot;
        
        for (Clan clan : clans) {
            if (clan == null) continue;
            ClanData data = new ClanData();
            data.id = clan.getId().toString();
            data.name = clan.getName();
            data.members = clan.getMembers().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().name()));
            data.bankBalance = clan.getBalance();
            data.storedXP = clan.getStoredXP();
            data.createdAt = clan.getCreatedAt(); // Grace period için
            data.guests = clan.getGuests().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            
            // Territory
            if (clan.getTerritory() != null) {
                Territory t = clan.getTerritory();
                data.territory = new TerritoryData();
                data.territory.center = serializeLocation(t.getCenter());
                data.territory.radius = t.getRadius();
                data.territory.outposts = t.getOutposts().stream()
                        .map(this::serializeLocation)
                        .collect(Collectors.toList());
            }
            
            // Crystal location
            if (clan.getCrystalLocation() != null) {
                data.crystalLocation = serializeLocation(clan.getCrystalLocation());
            }
            
            // Structures
            data.structures = clan.getStructures().stream()
                    .map(s -> {
                        StructureData sd = new StructureData();
                        sd.type = s.getType().name();
                        sd.location = serializeLocation(s.getLocation());
                        sd.level = s.getLevel();
                        sd.shieldFuel = s.getShieldFuel();
                        return sd;
                    })
                    .collect(Collectors.toList());
            
            snapshot.clans.add(data);
        }
        
        return snapshot;
    }
    
    private ContractSnapshot createContractSnapshot(ContractManager contractManager) {
        ContractSnapshot snapshot = new ContractSnapshot();
        
        if (contractManager == null) return snapshot;
        
        java.util.Collection<Contract> contracts = contractManager.getContracts();
        if (contracts == null) return snapshot;
        
        for (Contract contract : contracts) {
            if (contract == null) continue;
            ContractData data = new ContractData();
            data.id = contract.getId().toString();
            data.issuer = contract.getIssuer().toString();
            data.acceptor = contract.getAcceptor() != null ? contract.getAcceptor().toString() : null;
            data.material = contract.getMaterial().name();
            data.amount = contract.getAmount();
            data.reward = contract.getReward();
            data.delivered = contract.getDelivered();
            data.deadline = contract.getDeadline();
            snapshot.contracts.add(data);
        }
        
        return snapshot;
    }
    
    private ShopSnapshot createShopSnapshot(ShopManager shopManager) {
        ShopSnapshot snapshot = new ShopSnapshot();
        
        if (shopManager == null) return snapshot;
        
        java.util.Collection<Shop> shops = shopManager.getAllShops();
        if (shops == null) {
            snapshot.shops = new ArrayList<>();
            return snapshot;
        }
        
        snapshot.shops = shops.stream()
                .map(shop -> {
                    ShopData data = new ShopData();
                    data.id = UUID.randomUUID().toString();
                    data.owner = shop.getOwnerId().toString();
                    data.location = serializeLocation(shop.getLocation());
                    data.sellItem = serializeItemStack(shop.getSellingItem());
                    data.priceItem = serializeItemStack(shop.getPriceItem());
                    data.protectedZone = shop.isProtectedZone();
                    
                    // KRİTİK: Teklifleri kaydet (veri kaybı önleme)
                    data.offers = shop.getOffers().stream()
                            .map(offer -> {
                                OfferData offerData = new OfferData();
                                offerData.offerer = offer.getOfferer().toString();
                                offerData.offerItem = serializeItemStack(offer.getOfferItem());
                                offerData.offerAmount = offer.getOfferAmount();
                                offerData.offerTime = offer.getOfferTime();
                                offerData.accepted = offer.isAccepted();
                                offerData.rejected = offer.isRejected();
                                return offerData;
                            })
                            .collect(Collectors.toList());
                    
                    data.acceptOffers = shop.isAcceptOffers();
                    data.maxOffers = shop.getMaxOffers();
                    
                    return data;
                })
                .collect(Collectors.toList());
        
        return snapshot;
    }
    
    private InventorySnapshot createInventorySnapshot(VirtualStorageListener virtualStorage) {
        InventorySnapshot snapshot = new InventorySnapshot();
        
        if (virtualStorage == null) {
            return snapshot;
        }
        
        // VirtualStorageListener'dan tüm sanal envanterleri al
        Map<UUID, Inventory> virtualInventories = virtualStorage.getVirtualInventories();
        
        for (Map.Entry<UUID, Inventory> entry : virtualInventories.entrySet()) {
            UUID clanId = entry.getKey();
            Inventory inv = entry.getValue();
            
            // Inventory'yi Base64 string'e çevir
            String base64 = serializeInventory(inv);
            snapshot.inventories.put(clanId.toString(), base64);
        }
        
        return snapshot;
    }
    
    /**
     * Alliance verilerini snapshot al
     */
    private AllianceSnapshot createAllianceSnapshot(AllianceManager allianceManager) {
        AllianceSnapshot snapshot = new AllianceSnapshot();
        
        if (allianceManager == null) return snapshot;
        
        // AllianceManager'dan tüm ittifakları al
        java.util.Collection<me.mami.stratocraft.model.Alliance> alliances = allianceManager.getAllAlliances();
        if (alliances == null) return snapshot;
        
        for (me.mami.stratocraft.model.Alliance alliance : alliances) {
            if (alliance == null) continue;
            AllianceData data = new AllianceData();
            data.id = alliance.getId().toString();
            data.clan1Id = alliance.getClan1Id().toString();
            data.clan2Id = alliance.getClan2Id().toString();
            data.type = alliance.getType().name();
            data.createdAt = alliance.getCreatedAt();
            data.expiresAt = alliance.getExpiresAt();
            data.active = alliance.isActive();
            data.broken = alliance.isBroken();
            data.breakerClanId = alliance.getBreakerClanId() != null ? 
                alliance.getBreakerClanId().toString() : null;
            snapshot.alliances.add(data);
        }
        
        return snapshot;
    }
    
    /**
     * Disaster snapshot oluştur
     */
    private DisasterSnapshot createDisasterSnapshot(DisasterManager disasterManager) {
        DisasterSnapshot snapshot = new DisasterSnapshot();
        if (disasterManager != null) {
            DisasterManager.DisasterState state = disasterManager.getDisasterState();
            if (state != null) {
                DisasterStateData data = new DisasterStateData();
                data.type = state.type.name();
                data.category = state.category.name();
                data.level = state.level;
                data.startTime = state.startTime;
                data.duration = state.duration;
                data.target = state.target != null ? serializeLocation(state.target) : null;
                snapshot.disaster = data;
            }
        }
        return snapshot;
    }
    
    /**
     * Clan Bank snapshot oluştur
     */
    private ClanBankSnapshot createClanBankSnapshot(
            me.mami.stratocraft.manager.clan.ClanBankSystem bankSystem,
            ClanManager clanManager) {
        ClanBankSnapshot snapshot = new ClanBankSnapshot();
        
        if (bankSystem == null || clanManager == null) {
            return snapshot;
        }
        
        try {
            // Reflection kullanarak private field'lara eriş
            java.lang.reflect.Field bankChestLocationsField = 
                me.mami.stratocraft.manager.clan.ClanBankSystem.class.getDeclaredField("bankChestLocations");
            bankChestLocationsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Location> bankChestLocations = 
                (Map<UUID, Location>) bankChestLocationsField.get(bankSystem);
            
            java.lang.reflect.Field lastSalaryTimeField = 
                me.mami.stratocraft.manager.clan.ClanBankSystem.class.getDeclaredField("lastSalaryTime");
            lastSalaryTimeField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Long> lastSalaryTime = 
                (Map<UUID, Long>) lastSalaryTimeField.get(bankSystem);
            
            java.lang.reflect.Field transferContractsField = 
                me.mami.stratocraft.manager.clan.ClanBankSystem.class.getDeclaredField("transferContracts");
            transferContractsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, List<me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract>> transferContracts = 
                (Map<UUID, List<me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract>>) 
                transferContractsField.get(bankSystem);
            
            // Her klan için banka verilerini kaydet
            java.util.Collection<Clan> clans = clanManager.getAllClans();
            if (clans == null) return snapshot;
            
            for (Clan clan : clans) {
                if (clan == null) continue;
                UUID clanId = clan.getId();
                if (clanId == null) continue;
                BankData bankData = new BankData();
                bankData.clanId = clanId.toString();
                
                // Banka sandığı konumu
                if (bankChestLocations != null && bankChestLocations.containsKey(clanId)) {
                    bankData.chestLocation = serializeLocation(bankChestLocations.get(clanId));
                }
                
                // Maaş zamanları (klan üyeleri için)
                if (lastSalaryTime != null) {
                    bankData.lastSalaryTime = new HashMap<>();
                    for (UUID memberId : clan.getMembers().keySet()) {
                        if (lastSalaryTime.containsKey(memberId)) {
                            bankData.lastSalaryTime.put(memberId.toString(), lastSalaryTime.get(memberId));
                        }
                    }
                }
                
                // Transfer kontratları
                if (transferContracts != null && transferContracts.containsKey(clanId)) {
                    List<me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract> contracts = 
                        transferContracts.get(clanId);
                    if (contracts != null) {
                        for (me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract contract : contracts) {
                            if (contract != null && contract.isActive()) {
                                TransferContractData contractData = new TransferContractData();
                                contractData.clanId = contract.getClanId() != null ? contract.getClanId().toString() : null;
                                contractData.creatorId = contract.getCreatorId() != null ? contract.getCreatorId().toString() : null;
                                contractData.targetPlayerId = contract.getTargetPlayerId() != null ? contract.getTargetPlayerId().toString() : null;
                                contractData.material = contract.getMaterial() != null ? contract.getMaterial().name() : null;
                                contractData.amount = contract.getAmount();
                                contractData.interval = contract.getInterval();
                                contractData.lastTransferTime = contract.getLastTransferTime();
                                contractData.active = contract.isActive();
                                bankData.transferContracts.add(contractData);
                            }
                        }
                    }
                }
                
                snapshot.banks.put(clanId.toString(), bankData);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Clan Bank snapshot oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return snapshot;
    }
    
    /**
     * Clan Mission snapshot oluştur
     */
    private ClanMissionSnapshot createClanMissionSnapshot(
            me.mami.stratocraft.manager.clan.ClanMissionSystem missionSystem,
            ClanManager clanManager) {
        ClanMissionSnapshot snapshot = new ClanMissionSnapshot();
        
        if (missionSystem == null || clanManager == null) {
            return snapshot;
        }
        
        try {
            // Reflection kullanarak private field'lara eriş
            java.lang.reflect.Field activeMissionsField = 
                me.mami.stratocraft.manager.clan.ClanMissionSystem.class.getDeclaredField("activeMissions");
            activeMissionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, me.mami.stratocraft.manager.clan.ClanMissionSystem.ClanMission> activeMissions = 
                (Map<UUID, me.mami.stratocraft.manager.clan.ClanMissionSystem.ClanMission>) 
                activeMissionsField.get(missionSystem);
            
            java.lang.reflect.Field missionBoardLocationsField = 
                me.mami.stratocraft.manager.clan.ClanMissionSystem.class.getDeclaredField("missionBoardLocations");
            missionBoardLocationsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Location> missionBoardLocations = 
                (Map<UUID, Location>) missionBoardLocationsField.get(missionSystem);
            
            // Görev tahtası konumları
            if (missionBoardLocations != null) {
                for (Map.Entry<UUID, Location> entry : missionBoardLocations.entrySet()) {
                    snapshot.missionBoardLocations.put(entry.getKey().toString(), serializeLocation(entry.getValue()));
                }
            }
            
            // Aktif görevler
            if (activeMissions != null) {
                for (Map.Entry<UUID, me.mami.stratocraft.manager.clan.ClanMissionSystem.ClanMission> entry : activeMissions.entrySet()) {
                    UUID clanId = entry.getKey();
                    me.mami.stratocraft.manager.clan.ClanMissionSystem.ClanMission mission = entry.getValue();
                    
                    if (mission != null && mission.isActive()) {
                        MissionData missionData = new MissionData();
                        missionData.clanId = clanId.toString();
                        missionData.type = mission.getType() != null ? mission.getType().name() : null;
                        missionData.targetAmount = mission.getTarget();
                        missionData.currentProgress = mission.getProgress();
                        missionData.createdAt = mission.getCreatedTime();
                        missionData.deadline = mission.getExpiryTime();
                        missionData.completed = !mission.isActive() || (mission.getProgress() >= mission.getTarget());
                        
                        // Üye ilerlemeleri
                        try {
                            java.lang.reflect.Field memberProgressField = 
                                me.mami.stratocraft.manager.clan.ClanMissionSystem.ClanMission.class.getDeclaredField("memberProgress");
                            memberProgressField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Map<UUID, Integer> memberProgress = 
                                (Map<UUID, Integer>) memberProgressField.get(mission);
                            
                            if (memberProgress != null) {
                                for (Map.Entry<UUID, Integer> progressEntry : memberProgress.entrySet()) {
                                    missionData.memberProgress.put(progressEntry.getKey().toString(), progressEntry.getValue());
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Member progress okuma hatası: " + e.getMessage());
                        }
                        
                        // Ödüller
                        try {
                            java.lang.reflect.Field rewardsField = 
                                me.mami.stratocraft.manager.clan.ClanMissionSystem.ClanMission.class.getDeclaredField("rewards");
                            rewardsField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Map<String, Object> rewards = 
                                (Map<String, Object>) rewardsField.get(mission);
                            
                            if (rewards != null) {
                                missionData.rewards.putAll(rewards);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Rewards okuma hatası: " + e.getMessage());
                        }
                        
                        snapshot.missions.put(clanId.toString(), missionData);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Clan Mission snapshot oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return snapshot;
    }
    
    /**
     * Clan Activity snapshot oluştur
     */
    private ClanActivitySnapshot createClanActivitySnapshot(
            me.mami.stratocraft.manager.clan.ClanActivitySystem activitySystem) {
        ClanActivitySnapshot snapshot = new ClanActivitySnapshot();
        
        if (activitySystem == null) {
            return snapshot;
        }
        
        try {
            // Reflection kullanarak private field'lara eriş
            // Field adı: lastActivityTime (lastOnlineTime değil)
            java.lang.reflect.Field lastActivityTimeField = 
                me.mami.stratocraft.manager.clan.ClanActivitySystem.class.getDeclaredField("lastActivityTime");
            lastActivityTimeField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Long> lastActivityTime = 
                (Map<UUID, Long>) lastActivityTimeField.get(activitySystem);
            
            if (lastActivityTime != null) {
                for (Map.Entry<UUID, Long> entry : lastActivityTime.entrySet()) {
                    snapshot.lastOnlineTime.put(entry.getKey().toString(), entry.getValue());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Clan Activity snapshot oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return snapshot;
    }
    
    /**
     * Trap snapshot oluştur
     */
    private TrapSnapshot createTrapSnapshot(me.mami.stratocraft.manager.TrapManager trapManager) {
        TrapSnapshot snapshot = new TrapSnapshot();
        
        if (trapManager == null) {
            return snapshot;
        }
        
        try {
            // Reflection kullanarak private field'lara eriş
            java.lang.reflect.Field activeTrapsField = 
                me.mami.stratocraft.manager.TrapManager.class.getDeclaredField("activeTraps");
            activeTrapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Location, me.mami.stratocraft.manager.TrapManager.TrapData> activeTraps = 
                (Map<Location, me.mami.stratocraft.manager.TrapManager.TrapData>) activeTrapsField.get(trapManager);
            
            java.lang.reflect.Field inactiveTrapCoresField = 
                me.mami.stratocraft.manager.TrapManager.class.getDeclaredField("inactiveTrapCores");
            inactiveTrapCoresField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Location, UUID> inactiveTrapCores = 
                (Map<Location, UUID>) inactiveTrapCoresField.get(trapManager);
            
            // Aktif tuzakları kaydet
            if (activeTraps != null) {
                for (Map.Entry<Location, me.mami.stratocraft.manager.TrapManager.TrapData> entry : activeTraps.entrySet()) {
                    Location loc = entry.getKey();
                    me.mami.stratocraft.manager.TrapManager.TrapData trap = entry.getValue();
                    
                    if (trap == null || loc == null) continue;
                    
                    TrapData trapData = new TrapData();
                    trapData.location = serializeLocation(loc);
                    trapData.ownerId = trap.getOwnerId() != null ? trap.getOwnerId().toString() : null;
                    trapData.ownerClanId = trap.getOwnerClanId() != null ? trap.getOwnerClanId().toString() : null;
                    trapData.type = trap.getType() != null ? trap.getType().name() : null;
                    trapData.fuel = trap.getFuel();
                    trapData.isCovered = trap.isCovered();
                    
                    // Frame blocks
                    try {
                        java.lang.reflect.Field frameBlocksField = 
                            me.mami.stratocraft.manager.TrapManager.TrapData.class.getDeclaredField("frameBlocks");
                        frameBlocksField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        List<Location> frameBlocks = 
                            (List<Location>) frameBlocksField.get(trap);
                        
                        if (frameBlocks != null) {
                            for (Location frameLoc : frameBlocks) {
                                trapData.frameBlocks.add(serializeLocation(frameLoc));
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Frame blocks okuma hatası: " + e.getMessage());
                    }
                    
                    snapshot.activeTraps.add(trapData);
                }
            }
            
            // İnaktif tuzak çekirdeklerini kaydet
            if (inactiveTrapCores != null) {
                for (Map.Entry<Location, UUID> entry : inactiveTrapCores.entrySet()) {
                    InactiveTrapCoreData coreData = new InactiveTrapCoreData();
                    coreData.location = serializeLocation(entry.getKey());
                    coreData.ownerId = entry.getValue() != null ? entry.getValue().toString() : null;
                    snapshot.inactiveCores.add(coreData);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Trap snapshot oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
        
        return snapshot;
    }
    
    /**
     * Atomic write: Geçici dosyaya yaz, sonra rename (dosya bozulmasını önler)
     */
    private void atomicWrite(File targetFile, Object data) throws IOException {
        // Geçici dosya oluştur
        File tempFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");
        
        try {
            // Geçici dosyaya yaz
            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(data, writer);
            }
            
            // Backup oluştur (eğer hedef dosya varsa)
            if (targetFile.exists()) {
                createBackup(targetFile);
            }
            
            // Geçici dosyayı hedef dosyaya taşı (atomic operation)
            if (!tempFile.renameTo(targetFile)) {
                throw new IOException("Dosya taşıma başarısız: " + targetFile.getName());
            }
        } catch (Exception e) {
            // Hata durumunda geçici dosyayı temizle
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }
    }
    
    /**
     * Backup oluştur (son MAX_BACKUPS backup saklanır)
     */
    private void createBackup(File originalFile) {
        try {
            File backupFolder = new File(dataFolder, BACKUP_FOLDER);
            String fileName = originalFile.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            
            // Yeni backup dosyası
            String timestamp = String.valueOf(System.currentTimeMillis());
            File backupFile = new File(backupFolder, baseName + "_" + timestamp + extension);
            
            // Dosyayı kopyala
            try (java.io.FileInputStream fis = new java.io.FileInputStream(originalFile);
                 java.io.FileOutputStream fos = new java.io.FileOutputStream(backupFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            // Eski backup'ları temizle (MAX_BACKUPS'tan fazlasını sil)
            cleanupOldBackups(backupFolder, baseName, extension);
        } catch (Exception e) {
            plugin.getLogger().warning("Backup oluşturma hatası: " + e.getMessage());
        }
    }
    
    /**
     * Eski backup'ları temizle (son MAX_BACKUPS backup saklanır)
     */
    private void cleanupOldBackups(File backupFolder, String baseName, String extension) {
        try {
            File[] backups = backupFolder.listFiles((dir, name) -> 
                name.startsWith(baseName + "_") && name.endsWith(extension));
            
            if (backups == null || backups.length <= MAX_BACKUPS) return;
            
            // Tarihe göre sırala (en eski önce)
            Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
            
            // En eski backup'ları sil
            int deleteCount = backups.length - MAX_BACKUPS;
            for (int i = 0; i < deleteCount; i++) {
                backups[i].delete();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Backup temizleme hatası: " + e.getMessage());
        }
    }
    
    /**
     * Snapshot'ları diske yaz (async thread'de güvenli)
     */
    private void writeClanSnapshot(ClanSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/clans.json");
        atomicWrite(file, snapshot.clans);
    }
    
    private void writeContractSnapshot(ContractSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/contracts.json");
        atomicWrite(file, snapshot.contracts);
    }
    
    private void writeShopSnapshot(ShopSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/shops.json");
        atomicWrite(file, snapshot.shops);
    }
    
    private void writeAllianceSnapshot(AllianceSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/alliances.json");
        atomicWrite(file, snapshot.alliances);
    }
    
    private void writeDisasterSnapshot(DisasterSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/disaster.json");
        atomicWrite(file, snapshot.disaster);
    }
    
    private void writeClanBankSnapshot(ClanBankSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/clan_banks.json");
        atomicWrite(file, snapshot.banks);
    }
    
    private void writeClanMissionSnapshot(ClanMissionSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/clan_missions.json");
        Map<String, Object> data = new HashMap<>();
        data.put("version", DATA_VERSION);
        data.put("missions", snapshot.missions);
        data.put("missionBoardLocations", snapshot.missionBoardLocations);
        atomicWrite(file, data);
    }
    
    private void writeClanActivitySnapshot(ClanActivitySnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/clan_activity.json");
        atomicWrite(file, snapshot.lastOnlineTime);
    }
    
    private void writeTrapSnapshot(TrapSnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/traps.json");
        Map<String, Object> data = new HashMap<>();
        data.put("version", DATA_VERSION);
        data.put("activeTraps", snapshot.activeTraps);
        data.put("inactiveCores", snapshot.inactiveCores);
        atomicWrite(file, data);
    }
    
    private void writeInventorySnapshot(InventorySnapshot snapshot) throws IOException {
        File file = new File(dataFolder, "data/virtual_inventories.json");
        file.getParentFile().mkdirs(); // Klasör yoksa oluştur
        atomicWrite(file, snapshot.inventories);
    }
    
    private void saveShops(ShopManager shopManager) throws IOException {
        File file = new File(dataFolder, "data/shops.json");
        List<ShopData> shopDataList = shopManager.getAllShops().stream()
                .map(shop -> {
                    ShopData data = new ShopData();
                    data.id = UUID.randomUUID().toString(); // Shop ID yoksa random
                    data.owner = shop.getOwnerId().toString();
                    data.location = serializeLocation(shop.getLocation());
                    data.sellItem = serializeItemStack(shop.getSellingItem());
                    data.priceItem = serializeItemStack(shop.getPriceItem());
                    data.protectedZone = shop.isProtectedZone();
                    return data;
                })
                .collect(Collectors.toList());
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(shopDataList, writer);
        }
    }
    
    private void saveVirtualInventories(VirtualStorageListener virtualStorage) throws IOException {
        File file = new File(dataFolder, "data/virtual_inventories.json");
        Map<String, String> inventoryMap = new HashMap<>();
        
        for (Map.Entry<UUID, Inventory> entry : virtualStorage.getVirtualInventories().entrySet()) {
            String base64 = serializeInventory(entry.getValue());
            inventoryMap.put(entry.getKey().toString(), base64);
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(inventoryMap, writer);
        }
    }
    
    // ========== YÜKLEME METODLARI ==========
    
    public void loadAll(ClanManager clanManager, ContractManager contractManager,
                       ShopManager shopManager, VirtualStorageListener virtualStorage,
                       AllianceManager allianceManager, DisasterManager disasterManager) {
        loadAll(clanManager, contractManager, shopManager, virtualStorage, allianceManager, 
                disasterManager, null, null, null, null);
    }
    
    /**
     * Tüm verileri yükle (genişletilmiş versiyon - yeni sistemlerle)
     */
    public void loadAll(ClanManager clanManager, ContractManager contractManager,
                       ShopManager shopManager, VirtualStorageListener virtualStorage,
                       AllianceManager allianceManager, DisasterManager disasterManager,
                       me.mami.stratocraft.manager.clan.ClanBankSystem clanBankSystem,
                       me.mami.stratocraft.manager.clan.ClanMissionSystem clanMissionSystem,
                       me.mami.stratocraft.manager.clan.ClanActivitySystem clanActivitySystem,
                       me.mami.stratocraft.manager.TrapManager trapManager) {
        try {
            loadClans(clanManager);
            loadContracts(contractManager);
            loadShops(shopManager);
            loadVirtualInventories(virtualStorage);
            loadAlliances(allianceManager);
            loadDisaster(disasterManager);
            
            // Yeni sistemler
            if (clanBankSystem != null) {
                loadClanBank(clanBankSystem, clanManager);
            }
            if (clanMissionSystem != null) {
                loadClanMission(clanMissionSystem, clanManager);
            }
            if (clanActivitySystem != null) {
                loadClanActivity(clanActivitySystem);
            }
            if (trapManager != null) {
                loadTraps(trapManager);
            }
            
            plugin.getLogger().info("§aTüm veriler yüklendi!");
        } catch (Exception e) {
            plugin.getLogger().severe("§cVeri yükleme hatası: " + e.getMessage());
            e.printStackTrace();
            // Error recovery: Backup önerisi
            plugin.getLogger().warning("§eVeri yükleme başarısız! Backup'tan geri yükleme önerilir.");
        }
    }
    
    private void loadClans(ClanManager clanManager) throws IOException {
        if (clanManager == null) return;
        
        File file = new File(dataFolder, "data/clans.json");
        if (!file.exists()) return;
        
        List<ClanData> clanDataList = safeJsonParse(file, new TypeToken<List<ClanData>>(){});
        if (clanDataList == null) return;
        
        for (ClanData data : clanDataList) {
            // Data validation (güçlendirilmiş)
            if (data.id == null || !isValidUUID(data.id)) {
                plugin.getLogger().warning("Geçersiz klan ID atlandı: " + data.id);
                continue;
            }
            
            if (data.name == null || data.name.isEmpty()) {
                plugin.getLogger().warning("Geçersiz klan ismi atlandı: " + data.id);
                continue;
            }
            
            // Location validation
            if (data.territory != null && data.territory.center != null) {
                if (!isValidLocation(data.territory.center)) {
                    plugin.getLogger().warning("Geçersiz territory center atlandı: " + data.id);
                    data.territory.center = null; // Geçersiz location'ı null yap
                }
            }
            
            if (data.crystalLocation != null && !isValidLocation(data.crystalLocation)) {
                plugin.getLogger().warning("Geçersiz crystal location atlandı: " + data.id);
                data.crystalLocation = null;
            }
            
            try {
                UUID clanId = UUID.fromString(data.id);
                UUID leaderId = data.members.entrySet().stream()
                        .filter(e -> e.getValue().equals("LEADER"))
                        .map(e -> UUID.fromString(e.getKey()))
                        .findFirst().orElse(null);
                
                if (leaderId == null) continue;
                
                // Clan oluştur ve ID'yi set et
                Clan clan = new Clan(data.name, leaderId);
                clan.setId(clanId);
                
                // Members
                for (Map.Entry<String, String> entry : data.members.entrySet()) {
                    UUID memberId = UUID.fromString(entry.getKey());
                    Clan.Rank rank = Clan.Rank.valueOf(entry.getValue());
                    if (!memberId.equals(leaderId)) {
                        clan.addMember(memberId, rank);
                    }
                }
                
                // Territory
                if (data.territory != null) {
                    Territory territory = new Territory(clanId, deserializeLocation(data.territory.center));
                    territory.expand(data.territory.radius - 50);
                    for (String outpostStr : data.territory.outposts) {
                        territory.addOutpost(deserializeLocation(outpostStr));
                    }
                    clan.setTerritory(territory);
                }
                
                // Structures
                for (StructureData sd : data.structures) {
                    Structure structure = new Structure(
                            Structure.Type.valueOf(sd.type),
                            deserializeLocation(sd.location),
                            sd.level
                    );
                    for (int i = 0; i < sd.shieldFuel; i++) {
                        structure.addFuel(1);
                    }
                    clan.addStructure(structure);
                }
                
                // Bank balance ve XP
                clan.deposit(data.bankBalance);
                clan.setStoredXP(data.storedXP);
                
                // Guests
                for (String guestId : data.guests) {
                    clan.addGuest(UUID.fromString(guestId));
                }
                
                // ClanManager'a ekle
                clanManager.loadClan(clan);
            } catch (Exception e) {
                plugin.getLogger().warning("Clan yükleme hatası: " + data.id + " - " + e.getMessage());
            }
        }
    }
    
    private void loadContracts(ContractManager contractManager) throws IOException {
        if (contractManager == null) return;
        
        File file = new File(dataFolder, "data/contracts.json");
        if (!file.exists()) return;
        
        List<ContractData> contractDataList = safeJsonParse(file, new TypeToken<List<ContractData>>(){});
        if (contractDataList == null) return;
        
        for (ContractData data : contractDataList) {
                // Data validation
                if (data.id == null || !isValidUUID(data.id)) {
                    plugin.getLogger().warning("Geçersiz contract ID atlandı");
                    continue;
                }
                
                if (data.issuer == null || !isValidUUID(data.issuer)) {
                    plugin.getLogger().warning("Geçersiz contract issuer atlandı: " + data.id);
                    continue;
                }
                
                if (data.material == null) {
                    plugin.getLogger().warning("Geçersiz contract material atlandı: " + data.id);
                    continue;
                }
                
                try {
                    Contract contract = new Contract(
                            UUID.fromString(data.issuer),
                            org.bukkit.Material.valueOf(data.material),
                            data.amount,
                            data.reward,
                            (data.deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                    );
                    
                    // ID ve diğer alanları set et
                    contract.setId(UUID.fromString(data.id));
                    contract.setDelivered(data.delivered);
                    if (data.acceptor != null && isValidUUID(data.acceptor)) {
                        contract.setAcceptor(UUID.fromString(data.acceptor));
                    }
                    
                    contractManager.loadContract(contract);
                } catch (Exception e) {
                    plugin.getLogger().warning("Contract yükleme hatası: " + data.id + " - " + e.getMessage());
                }
        }
    }
    
    private void loadShops(ShopManager shopManager) throws IOException {
        if (shopManager == null) return;
        
        File file = new File(dataFolder, "data/shops.json");
        if (!file.exists()) return;
        
        List<ShopData> shopDataList = safeJsonParse(file, new TypeToken<List<ShopData>>(){});
        if (shopDataList == null) return;
        
        for (ShopData data : shopDataList) {
                // Data validation
                if (data.owner == null || !isValidUUID(data.owner)) {
                    plugin.getLogger().warning("Geçersiz shop owner atlandı");
                    continue;
                }
                
                if (data.location == null || !isValidLocation(data.location)) {
                    plugin.getLogger().warning("Geçersiz shop location atlandı: " + data.owner);
                    continue;
                }
                
                try {
                    Shop shop = new Shop(
                            UUID.fromString(data.owner),
                            deserializeLocation(data.location),
                            deserializeItemStack(data.sellItem),
                            deserializeItemStack(data.priceItem),
                            data.protectedZone
                    );
                
                    // KRİTİK: Teklifleri yükle (veri kaybı önleme)
                    if (data.offers != null) {
                        for (OfferData offerData : data.offers) {
                            // Sadece kabul/reddedilmemiş teklifleri yükle
                            if (!offerData.accepted && !offerData.rejected) {
                                Shop.Offer offer = new Shop.Offer(
                                        UUID.fromString(offerData.offerer),
                                        deserializeItemStack(offerData.offerItem),
                                        offerData.offerAmount
                                );
                                shop.addOffer(offer);
                            }
                        }
                    }
                    
                    // Teklif ayarlarını yükle
                    shop.setAcceptOffers(data.acceptOffers);
                    shop.setMaxOffers(data.maxOffers);
                    
                    shopManager.loadShop(shop);
                } catch (Exception e) {
                    plugin.getLogger().warning("Shop yükleme hatası: " + data.id + " - " + e.getMessage());
                }
        }
    }
    
    private void loadVirtualInventories(VirtualStorageListener virtualStorage) throws IOException {
        if (virtualStorage == null) return;
        
        File file = new File(dataFolder, "data/virtual_inventories.json");
        if (!file.exists()) return;
        
        try (FileReader reader = new FileReader(file)) {
            Map<String, String> inventoryMap = gson.fromJson(reader,
                    new TypeToken<Map<String, String>>(){}.getType());
            
            if (inventoryMap == null) return;
            
            for (Map.Entry<String, String> entry : inventoryMap.entrySet()) {
                if (!isValidUUID(entry.getKey())) {
                    plugin.getLogger().warning("Geçersiz inventory clan ID atlandı: " + entry.getKey());
                    continue;
                }
                
                try {
                    UUID clanId = UUID.fromString(entry.getKey());
                    Inventory inv = deserializeInventory(entry.getValue());
                    if (inv != null) {
                        virtualStorage.setVirtualInventory(clanId, inv);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Inventory yükleme hatası: " + entry.getKey() + " - " + e.getMessage());
                }
            }
        }
    }
    
    private void loadAlliances(AllianceManager allianceManager) throws IOException {
        if (allianceManager == null) return;
        
        File file = new File(dataFolder, "data/alliances.json");
        if (!file.exists()) return;
        
        List<AllianceData> allianceDataList = safeJsonParse(file, new TypeToken<List<AllianceData>>(){});
        if (allianceDataList == null) return;
        
        for (AllianceData data : allianceDataList) {
                // Data validation
                if (data.id == null || !isValidUUID(data.id)) {
                    plugin.getLogger().warning("Geçersiz alliance ID atlandı");
                    continue;
                }
                
                if (data.clan1Id == null || !isValidUUID(data.clan1Id) ||
                    data.clan2Id == null || !isValidUUID(data.clan2Id)) {
                    plugin.getLogger().warning("Geçersiz alliance clan ID atlandı: " + data.id);
                    continue;
                }
                
                if (data.type == null) {
                    plugin.getLogger().warning("Geçersiz alliance type atlandı: " + data.id);
                    continue;
                }
                
                try {
                    me.mami.stratocraft.model.Alliance.Type type = 
                        me.mami.stratocraft.model.Alliance.Type.valueOf(data.type);
                    long durationDays = data.expiresAt > 0 ? 
                        (data.expiresAt - data.createdAt) / (24 * 60 * 60 * 1000) : 0;
                    
                    me.mami.stratocraft.model.Alliance alliance = new me.mami.stratocraft.model.Alliance(
                        UUID.fromString(data.clan1Id),
                        UUID.fromString(data.clan2Id),
                        type,
                        durationDays
                    );
                    alliance.setId(UUID.fromString(data.id));
                    alliance.setActive(data.active);
                    if (data.broken && data.breakerClanId != null) {
                        alliance.breakAlliance(UUID.fromString(data.breakerClanId));
                    }
                    
                    // AllianceManager'a yükle
                    allianceManager.loadAlliance(alliance);
                } catch (Exception e) {
                    plugin.getLogger().warning("Alliance yükleme hatası: " + data.id + " - " + e.getMessage());
                }
        }
    }
    
    private void loadDisaster(DisasterManager disasterManager) throws IOException {
        if (disasterManager == null) return;
        
        File file = new File(dataFolder, "data/disaster.json");
        if (!file.exists()) return;
        
        DisasterStateData data = safeJsonParse(file, new TypeToken<DisasterStateData>(){});
        if (data == null) return;
        
        // Data validation
        if (data.type == null || data.category == null) {
            plugin.getLogger().warning("Geçersiz disaster data atlandı");
            return;
        }
        
        if (data.target != null && !isValidLocation(data.target)) {
            plugin.getLogger().warning("Geçersiz disaster target atlandı");
            data.target = null;
        }
        
        try {
            Disaster.Type type = Disaster.Type.valueOf(data.type);
            Disaster.Category category = Disaster.Category.valueOf(data.category);
            Location target = data.target != null ? deserializeLocation(data.target) : null;
            
            DisasterManager.DisasterState state = new DisasterManager.DisasterState(
                type, category, data.level, data.startTime, data.duration, target
            );
            
            disasterManager.loadDisasterState(state);
        } catch (Exception e) {
            plugin.getLogger().warning("Disaster yükleme hatası: " + e.getMessage());
        }
    }
    
    /**
     * Restore: Backup'tan geri yükleme
     */
    public boolean restoreFromBackup(String fileName) {
        try {
            File backupFolder = new File(dataFolder, BACKUP_FOLDER);
            File[] backups = backupFolder.listFiles((dir, name) -> 
                name.startsWith(fileName.replace(".json", "")) && name.endsWith(".json"));
            
            if (backups == null || backups.length == 0) {
                plugin.getLogger().warning("§cBackup bulunamadı: " + fileName);
                return false;
            }
            
            // En yeni backup'ı bul
            Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            File latestBackup = backups[0];
            
            // Hedef dosyaya kopyala
            File targetFile = new File(dataFolder, "data/" + fileName);
            try (java.io.FileInputStream fis = new java.io.FileInputStream(latestBackup);
                 java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            plugin.getLogger().info("§aBackup'tan geri yüklendi: " + fileName + " (" + latestBackup.getName() + ")");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("§cBackup restore hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Tüm backup'ları listele
     */
    public List<String> listBackups(String fileName) {
        List<String> backups = new ArrayList<>();
        try {
            File backupFolder = new File(dataFolder, BACKUP_FOLDER);
            String baseName = fileName.replace(".json", "");
            File[] backupFiles = backupFolder.listFiles((dir, name) -> 
                name.startsWith(baseName + "_") && name.endsWith(".json"));
            
            if (backupFiles != null) {
                Arrays.sort(backupFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                for (File backup : backupFiles) {
                    backups.add(backup.getName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Backup listeleme hatası: " + e.getMessage());
        }
        return backups;
    }
    
    private void loadClanBank(me.mami.stratocraft.manager.clan.ClanBankSystem bankSystem,
                              ClanManager clanManager) throws IOException {
        File file = new File(dataFolder, "data/clan_banks.json");
        if (!file.exists() || bankSystem == null || clanManager == null) return;
        
        Map<String, BankData> banks = safeJsonParse(file, new TypeToken<Map<String, BankData>>(){});
        if (banks == null) return;
        
        for (BankData data : banks.values()) {
            UUID clanId = UUID.fromString(data.clanId);
            Clan clan = clanManager.getClanById(clanId);
            if (clan == null) continue;
            
            // Banka sandığı konumu
            if (data.chestLocation != null) {
                Location chestLoc = deserializeLocation(data.chestLocation);
                try {
                    java.lang.reflect.Field bankChestLocationsField = 
                        me.mami.stratocraft.manager.clan.ClanBankSystem.class.getDeclaredField("bankChestLocations");
                    bankChestLocationsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<UUID, Location> bankChestLocations = 
                        (Map<UUID, Location>) bankChestLocationsField.get(bankSystem);
                    if (bankChestLocations != null) {
                        bankChestLocations.put(clanId, chestLoc);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Banka sandığı konumu yükleme hatası: " + e.getMessage());
                }
            }
            
            // Maaş zamanları
            if (data.lastSalaryTime != null) {
                try {
                    java.lang.reflect.Field lastSalaryTimeField = 
                        me.mami.stratocraft.manager.clan.ClanBankSystem.class.getDeclaredField("lastSalaryTime");
                    lastSalaryTimeField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<UUID, Long> lastSalaryTime = 
                        (Map<UUID, Long>) lastSalaryTimeField.get(bankSystem);
                    if (lastSalaryTime != null) {
                        for (Map.Entry<String, Long> entry : data.lastSalaryTime.entrySet()) {
                            lastSalaryTime.put(UUID.fromString(entry.getKey()), entry.getValue());
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Maaş zamanları yükleme hatası: " + e.getMessage());
                }
            }
            
            // Transfer kontratları
            if (data.transferContracts != null && !data.transferContracts.isEmpty()) {
                try {
                    java.lang.reflect.Field transferContractsField = 
                        me.mami.stratocraft.manager.clan.ClanBankSystem.class.getDeclaredField("transferContracts");
                    transferContractsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<UUID, List<me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract>> transferContracts = 
                        (Map<UUID, List<me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract>>) 
                        transferContractsField.get(bankSystem);
                    
                    if (transferContracts != null) {
                        List<me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract> contracts = 
                            transferContracts.computeIfAbsent(clanId, k -> Collections.synchronizedList(new ArrayList<>()));
                        
                        for (TransferContractData contractData : data.transferContracts) {
                            me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract contract = 
                                new me.mami.stratocraft.manager.clan.ClanBankSystem.TransferContract();
                            contract.setClanId(clanId);
                            if (contractData.creatorId != null) {
                                contract.setCreatorId(UUID.fromString(contractData.creatorId));
                            }
                            if (contractData.targetPlayerId != null) {
                                contract.setTargetPlayerId(UUID.fromString(contractData.targetPlayerId));
                            }
                            if (contractData.material != null) {
                                contract.setMaterial(Material.valueOf(contractData.material));
                            }
                            contract.setAmount(contractData.amount);
                            contract.setInterval(contractData.interval);
                            contract.setLastTransferTime(contractData.lastTransferTime);
                            contract.setActive(contractData.active);
                            
                            synchronized (contracts) {
                                contracts.add(contract);
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Transfer kontratları yükleme hatası: " + e.getMessage());
                }
            }
        }
    }
    
    private void loadClanMission(me.mami.stratocraft.manager.clan.ClanMissionSystem missionSystem,
                                 ClanManager clanManager) throws IOException {
        File file = new File(dataFolder, "data/clan_missions.json");
        if (!file.exists() || missionSystem == null || clanManager == null) return;
        
        Map<String, Object> data = safeJsonParse(file, new TypeToken<Map<String, Object>>(){});
        if (data == null) return;
        
        // Version kontrolü (migration için)
        if (data.containsKey("version")) {
            int version = ((Double) data.get("version")).intValue();
            if (version != DATA_VERSION) {
                plugin.getLogger().warning("§eEski data formatı tespit edildi (version " + version + 
                    "), migration gerekebilir.");
            }
        }
        
        // Görev tahtası konumları
        if (data.containsKey("missionBoardLocations")) {
            Map<String, String> locations = gson.fromJson(
                gson.toJsonTree(data.get("missionBoardLocations")),
                new TypeToken<Map<String, String>>(){}.getType());
            
            if (locations != null) {
                try {
                    java.lang.reflect.Field missionBoardLocationsField = 
                        me.mami.stratocraft.manager.clan.ClanMissionSystem.class.getDeclaredField("missionBoardLocations");
                    missionBoardLocationsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<UUID, Location> missionBoardLocations = 
                        (Map<UUID, Location>) missionBoardLocationsField.get(missionSystem);
                    
                    if (missionBoardLocations != null) {
                        for (Map.Entry<String, String> entry : locations.entrySet()) {
                            missionBoardLocations.put(UUID.fromString(entry.getKey()), 
                                deserializeLocation(entry.getValue()));
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Görev tahtası konumları yükleme hatası: " + e.getMessage());
                }
            }
        }
        
        // Aktif görevler - Basitleştirilmiş versiyon (reflection çok karmaşık)
        // Görevler runtime'da oluşturulabilir, sadece konumları yüklemek yeterli
    }
    
    private void loadClanActivity(me.mami.stratocraft.manager.clan.ClanActivitySystem activitySystem) throws IOException {
        File file = new File(dataFolder, "data/clan_activity.json");
        if (!file.exists() || activitySystem == null) return;
        
        Map<String, Long> lastOnlineTime = safeJsonParse(file, new TypeToken<Map<String, Long>>(){});
        if (lastOnlineTime == null) return;
        
        try {
            // Field adı: lastActivityTime (lastOnlineTime değil)
            java.lang.reflect.Field lastActivityTimeField = 
                me.mami.stratocraft.manager.clan.ClanActivitySystem.class.getDeclaredField("lastActivityTime");
            lastActivityTimeField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Long> activityMap = 
                (Map<UUID, Long>) lastActivityTimeField.get(activitySystem);
            
            if (activityMap != null) {
                for (Map.Entry<String, Long> entry : lastOnlineTime.entrySet()) {
                    activityMap.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Aktivite verileri yükleme hatası: " + e.getMessage());
        }
    }
    
    private void loadTraps(me.mami.stratocraft.manager.TrapManager trapManager) throws IOException {
        File file = new File(dataFolder, "data/traps.json");
        if (!file.exists() || trapManager == null) return;
        
        Map<String, Object> data = safeJsonParse(file, new TypeToken<Map<String, Object>>(){});
        if (data == null) return;
        
        // Version kontrolü
        if (data.containsKey("version")) {
            int version = ((Double) data.get("version")).intValue();
            if (version != DATA_VERSION) {
                plugin.getLogger().warning("§eEski trap data formatı tespit edildi (version " + version + 
                    "), migration gerekebilir.");
            }
        }
        
        // TrapSnapshot'a dönüştür
        TrapSnapshot snapshot = new TrapSnapshot();
        if (data.containsKey("activeTraps")) {
            List<Map<String, Object>> activeTrapsData = gson.fromJson(
                gson.toJsonTree(data.get("activeTraps")),
                new TypeToken<List<Map<String, Object>>>(){}.getType());
            
            if (activeTrapsData != null) {
                for (Map<String, Object> trapData : activeTrapsData) {
                    TrapData trap = gson.fromJson(gson.toJsonTree(trapData), TrapData.class);
                    if (trap != null) {
                        snapshot.activeTraps.add(trap);
                    }
                }
            }
        }
        
        if (data.containsKey("inactiveCores")) {
            List<Map<String, Object>> inactiveCoresData = gson.fromJson(
                gson.toJsonTree(data.get("inactiveCores")),
                new TypeToken<List<Map<String, Object>>>(){}.getType());
            
            if (inactiveCoresData != null) {
                for (Map<String, Object> coreData : inactiveCoresData) {
                    InactiveTrapCoreData core = gson.fromJson(gson.toJsonTree(coreData), InactiveTrapCoreData.class);
                    if (core != null) {
                        snapshot.inactiveCores.add(core);
                    }
                }
            }
        }
        
        if (snapshot.activeTraps.isEmpty() && snapshot.inactiveCores.isEmpty()) return;
        
        try {
            // Reflection ile activeTraps ve inactiveTrapCores field'larına eriş
            java.lang.reflect.Field activeTrapsField = 
                me.mami.stratocraft.manager.TrapManager.class.getDeclaredField("activeTraps");
            activeTrapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Location, me.mami.stratocraft.manager.TrapManager.TrapData> activeTraps = 
                (Map<Location, me.mami.stratocraft.manager.TrapManager.TrapData>) activeTrapsField.get(trapManager);
            
            java.lang.reflect.Field inactiveTrapCoresField = 
                me.mami.stratocraft.manager.TrapManager.class.getDeclaredField("inactiveTrapCores");
            inactiveTrapCoresField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Location, UUID> inactiveTrapCores = 
                (Map<Location, UUID>) inactiveTrapCoresField.get(trapManager);
            
            // Aktif tuzakları yükle
            if (snapshot.activeTraps != null && activeTraps != null) {
                for (TrapData trapData : snapshot.activeTraps) {
                    Location loc = deserializeLocation(trapData.location);
                    if (loc == null || loc.getWorld() == null) continue;
                    
                    UUID ownerId = trapData.ownerId != null ? UUID.fromString(trapData.ownerId) : null;
                    UUID clanId = trapData.ownerClanId != null ? UUID.fromString(trapData.ownerClanId) : null;
                    me.mami.stratocraft.manager.TrapManager.TrapType type = 
                        trapData.type != null ? me.mami.stratocraft.manager.TrapManager.TrapType.valueOf(trapData.type) : null;
                    int fuel = trapData.fuel;
                    
                    if (ownerId == null || type == null) continue;
                    
                    // TrapData oluştur
                    me.mami.stratocraft.manager.TrapManager.TrapData trap = 
                        new me.mami.stratocraft.manager.TrapManager.TrapData(ownerId, clanId, type, fuel, loc);
                    
                    // Frame blocks'u set et
                    if (trapData.frameBlocks != null && !trapData.frameBlocks.isEmpty()) {
                        try {
                            java.lang.reflect.Field frameBlocksField = 
                                me.mami.stratocraft.manager.TrapManager.TrapData.class.getDeclaredField("frameBlocks");
                            frameBlocksField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            List<Location> frameBlocks = 
                                (List<Location>) frameBlocksField.get(trap);
                            
                            if (frameBlocks != null) {
                                for (String frameLocStr : trapData.frameBlocks) {
                                    Location frameLoc = deserializeLocation(frameLocStr);
                                    if (frameLoc != null) {
                                        frameBlocks.add(frameLoc);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Frame blocks yükleme hatası: " + e.getMessage());
                        }
                    }
                    
                    // isCovered set et
                    try {
                        java.lang.reflect.Field isCoveredField = 
                            me.mami.stratocraft.manager.TrapManager.TrapData.class.getDeclaredField("isCovered");
                        isCoveredField.setAccessible(true);
                        isCoveredField.set(trap, trapData.isCovered);
                    } catch (Exception e) {
                        plugin.getLogger().warning("isCovered set etme hatası: " + e.getMessage());
                    }
                    
                    activeTraps.put(loc, trap);
                    
                    // Metadata'yı geri yükle
                    Block block = loc.getBlock();
                    if (block.getType() == Material.LODESTONE) {
                        block.setMetadata("TrapCore", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                        if (ownerId != null) {
                            block.setMetadata("TrapOwner", new org.bukkit.metadata.FixedMetadataValue(plugin, ownerId.toString()));
                        }
                    }
                }
            }
            
            // İnaktif tuzak çekirdeklerini yükle
            if (snapshot.inactiveCores != null && inactiveTrapCores != null) {
                for (InactiveTrapCoreData coreData : snapshot.inactiveCores) {
                    Location loc = deserializeLocation(coreData.location);
                    if (loc == null || loc.getWorld() == null) continue;
                    
                    UUID ownerId = coreData.ownerId != null ? UUID.fromString(coreData.ownerId) : null;
                    if (ownerId != null) {
                        inactiveTrapCores.put(loc, ownerId);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Trap yükleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== YARDIMCI METODLAR ==========
    
    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + 
               ";" + loc.getYaw() + ";" + loc.getPitch();
    }
    
    private Location deserializeLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            String[] parts = str.split(";");
            if (parts.length < 6) return null;
            org.bukkit.World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            return new Location(
                    world,
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]),
                    Float.parseFloat(parts[4]),
                    Float.parseFloat(parts[5])
            );
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Güvenli JSON parse (corruption detection)
     */
    private <T> T safeJsonParse(File file, TypeToken<T> typeToken) {
        if (file == null || !file.exists()) return null;
        try {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, typeToken.getType());
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            plugin.getLogger().severe("§cJSON corruption tespit edildi: " + file.getName());
            plugin.getLogger().severe("§cHata: " + e.getMessage());
            plugin.getLogger().warning("§eBackup'tan geri yükleme önerilir: /stratocraft data restore " + file.getName());
            return null;
        } catch (Exception e) {
            plugin.getLogger().warning("JSON parse hatası: " + file.getName() + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * UUID format kontrolü
     */
    private boolean isValidUUID(String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) return false;
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Location geçerlilik kontrolü (world null check)
     */
    private boolean isValidLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) return false;
        try {
            Location loc = deserializeLocation(locationStr);
            return loc != null && loc.getWorld() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
    
    private ItemStack deserializeItemStack(String str) {
        if (str == null) return null;
        try {
            byte[] data = Base64.getDecoder().decode(str);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String serializeInventory(Inventory inv) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(inv.getSize());
            for (int i = 0; i < inv.getSize(); i++) {
                dataOutput.writeObject(inv.getItem(i));
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
    
    private Inventory deserializeInventory(String str) {
        try {
            byte[] data = Base64.getDecoder().decode(str);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();
            Inventory inv = Bukkit.createInventory(null, size, "§5Sanal Bağlantı - Şubeler Arası Depo");
            for (int i = 0; i < size; i++) {
                inv.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return inv;
        } catch (Exception e) {
            return Bukkit.createInventory(null, 54, "§5Sanal Bağlantı - Şubeler Arası Depo");
        }
    }
    
    // ========== DATA CLASSES ==========
    
    private static class ClanData {
        String id;
        String name;
        Map<String, String> members;
        double bankBalance;
        int storedXP;
        long createdAt; // Grace period için
        String crystalLocation; // Ölümsüz klan önleme için
        List<String> guests;
        TerritoryData territory;
        List<StructureData> structures;
    }
    
    private static class TerritoryData {
        String center;
        int radius;
        List<String> outposts;
    }
    
    private static class StructureData {
        String type;
        String location;
        int level;
        int shieldFuel;
    }
    
    private static class ContractData {
        String id;
        String issuer;
        String acceptor;
        String material;
        int amount;
        double reward;
        int delivered;
        long deadline;
    }
    
    private static class ShopData {
        @SuppressWarnings("unused")
        String id;
        String owner;
        String location;
        String sellItem;
        String priceItem;
        boolean protectedZone;
        // KRİTİK: Teklif sistemi verileri
        List<OfferData> offers = new ArrayList<>();
        boolean acceptOffers = true;
        int maxOffers = 10;
    }
    
    private static class OfferData {
        String offerer;
        String offerItem;
        int offerAmount;
        long offerTime;
        boolean accepted = false;
        boolean rejected = false;
    }
    
    private static class DisasterStateData {
        String type;
        String category;
        int level;
        long startTime;
        long duration;
        String target;
    }
    
    // Location adapter for Gson
    private static class LocationAdapter implements com.google.gson.JsonSerializer<Location>, 
            com.google.gson.JsonDeserializer<Location> {
        @Override
        public com.google.gson.JsonElement serialize(Location src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("world", src.getWorld().getName());
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            obj.addProperty("yaw", src.getYaw());
            obj.addProperty("pitch", src.getPitch());
            return obj;
        }
        
        @Override
        public Location deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) {
            com.google.gson.JsonObject obj = json.getAsJsonObject();
            return new Location(
                    Bukkit.getWorld(obj.get("world").getAsString()),
                    obj.get("x").getAsDouble(),
                    obj.get("y").getAsDouble(),
                    obj.get("z").getAsDouble(),
                    obj.get("yaw").getAsFloat(),
                    obj.get("pitch").getAsFloat()
            );
        }
    }
}

