package me.mami.stratocraft.manager.clan;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.clan.config.ClanBankConfig;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klan Bankası Sistemi (Item-Based)
 * 
 * Özellikler:
 * - Fiziksel sandık entegrasyonu (Ender Chest)
 * - Rütbe bazlı erişim kontrolü
 * - Item yatırma/çekme
 * - Otomatik maaş sistemi
 * - Otomatik transfer kontratları
 * - Sandık durumu takibi
 */
public class ClanBankSystem {
    private final Main plugin;
    private final ClanManager clanManager;
    private final ClanRankSystem rankSystem;
    private ClanBankConfig config;
    
    // Klan -> Banka sandığı konumu
    private final Map<UUID, Location> bankChestLocations = new ConcurrentHashMap<>();
    
    // Otomatik maaş sistemi: Üye -> Son maaş zamanı
    private final Map<UUID, Long> lastSalaryTime = new ConcurrentHashMap<>();
    
    // Otomatik transfer kontratları: Klan -> Kontrat listesi (thread-safe)
    private final Map<UUID, List<TransferContract>> transferContracts = new ConcurrentHashMap<>();
    
    public ClanBankSystem(Main plugin, ClanManager clanManager, ClanRankSystem rankSystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.rankSystem = rankSystem;
        this.config = new ClanBankConfig();
    }
    
    /**
     * Config yükle
     */
    public void loadConfig(org.bukkit.configuration.file.FileConfiguration fileConfig) {
        config.loadFromConfig(fileConfig);
    }
    
    /**
     * Klan bankası oluştur (fiziksel)
     * 
     * Gereksinimler:
     * - Ender Chest
     * - Item Frame + Name Tag ("KLAN_BANKASI")
     */
    public boolean createBankChest(Player player, Location chestLoc) {
        if (player == null || chestLoc == null) return false;
        
        Block block = chestLoc.getBlock();
        if (block.getType() != Material.ENDER_CHEST) {
            player.sendMessage("§cKlan bankası için Ender Chest gerekli!");
            return false;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return false;
        }
        
        // Yetki kontrolü (Lider veya General)
        if (!rankSystem.hasPermission(clan, player.getUniqueId(), 
                ClanRankSystem.Permission.MANAGE_BANK)) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        // Name Tag kontrolü (Item Frame'de)
        boolean hasBankTag = checkForBankTag(chestLoc);
        if (!hasBankTag) {
            player.sendMessage("§cKlan bankası için Item Frame'e 'KLAN_BANKASI' yazılı Name Tag gerekli!");
            return false;
        }
        
        // Metadata ekle
        block.setMetadata("ClanBank", new FixedMetadataValue(plugin, clan.getId().toString()));
        
        // Konumu kaydet
        bankChestLocations.put(clan.getId(), chestLoc);
        
        player.sendMessage("§aKlan bankası oluşturuldu!");
        return true;
    }
    
    /**
     * Name Tag kontrolü (Item Frame'de)
     */
    private boolean checkForBankTag(Location chestLoc) {
        if (chestLoc == null || chestLoc.getWorld() == null) return false;
        
        try {
            for (org.bukkit.entity.Entity entity : chestLoc.getWorld()
                    .getNearbyEntities(chestLoc, 2, 2, 2)) {
                if (entity instanceof ItemFrame) {
                    ItemFrame frame = (ItemFrame) entity;
                    ItemStack item = frame.getItem();
                    if (item != null && item.getType() == Material.NAME_TAG) {
                        if (item.hasItemMeta()) {
                            String displayName = item.getItemMeta().getDisplayName();
                            if (displayName != null && displayName.contains("KLAN_BANKASI")) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Banka tag kontrolü hatası: " + e.getMessage());
            return false;
        }
        return false;
    }
    
    /**
     * Klan bankası sandığını al (cache ile optimize edilmiş)
     */
    // Cache: Klan -> Inventory (5 saniye cache)
    private final Map<UUID, Inventory> bankChestCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> bankChestCacheTime = new ConcurrentHashMap<>();
    private static final long BANK_CHEST_CACHE_DURATION = 5000L; // 5 saniye
    
    public Inventory getBankChest(Clan clan) {
        if (clan == null) return null;
        
        UUID clanId = clan.getId();
        long now = System.currentTimeMillis();
        
        // Cache kontrolü
        Inventory cached = bankChestCache.get(clanId);
        Long cacheTime = bankChestCacheTime.get(clanId);
        
        if (cached != null && cacheTime != null && now - cacheTime < BANK_CHEST_CACHE_DURATION) {
            return cached;
        }
        
        Location chestLoc = bankChestLocations.get(clanId);
        if (chestLoc == null) {
            // Cache'i temizle
            bankChestCache.remove(clanId);
            bankChestCacheTime.remove(clanId);
            return null;
        }
        
        // Null check
        if (chestLoc.getWorld() == null) {
            bankChestLocations.remove(clanId);
            return null;
        }
        
        Block block = chestLoc.getBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST) {
            // Sandık yok, konumu temizle
            bankChestLocations.remove(clanId);
            bankChestCache.remove(clanId);
            bankChestCacheTime.remove(clanId);
            return null;
        }
        
        // Metadata kontrolü
        try {
            List<MetadataValue> metadata = block.getMetadata("ClanBank");
            if (metadata.isEmpty()) {
                return null;
            }
            
            // EnderChest bir BlockState ama InventoryHolder değil
            // Klan bankası için sanal bir inventory oluştur
            Inventory inventory = bankChestCache.get(clanId);
            if (inventory == null) {
                // Yeni bir inventory oluştur (27 slot - ender chest boyutu)
                inventory = org.bukkit.Bukkit.createInventory(null, 27, "§5Klan Bankası");
                bankChestCache.put(clanId, inventory);
            }
            bankChestCacheTime.put(clanId, now);
            
            return inventory;
        } catch (Exception e) {
            plugin.getLogger().warning("Banka sandığı alınırken hata: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Item yatırma
     */
    public boolean depositItem(Player player, ItemStack item, int amount) {
        if (player == null || item == null || amount <= 0) return false;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return false;
        }
        
        // Envanter kontrolü
        if (!player.getInventory().containsAtLeast(item, amount)) {
            player.sendMessage("§cYeterli item yok!");
            return false;
        }
        
        // Banka sandığı kontrolü
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) {
            player.sendMessage("§cKlan bankası bulunamadı!");
            return false;
        }
        
        // ⚠️ TRANSACTION MANTIĞI: Önce envanterden al, sonra bankaya ekle (dupe önleme)
        // 1. ÖNCE ENVANTERDEN AL (transaction başlat)
        ItemStack toRemove = item.clone();
        toRemove.setAmount(amount);
        HashMap<Integer, ItemStack> removeResult = player.getInventory().removeItem(toRemove);
        
        if (!removeResult.isEmpty()) {
            // Envanterden alınamadı (yeterli item yok), işlem iptal
            player.sendMessage("§cYeterli item yok!");
            return false;
        }
        
        // 2. SONRA BANKAYA EKLE
        ItemStack depositItem = item.clone();
        depositItem.setAmount(amount);
        HashMap<Integer, ItemStack> overflow = bankChest.addItem(depositItem);
        
        if (!overflow.isEmpty()) {
            // Sandık dolu, item'i geri ver (rollback)
            player.sendMessage("§cBanka sandığı dolu! Itemler envanterine geri verildi.");
            HashMap<Integer, ItemStack> refundResult = player.getInventory().addItem(toRemove);
            if (!refundResult.isEmpty()) {
                // Envanter dolu, yere düşür
                for (ItemStack remaining : refundResult.values()) {
                    if (remaining != null) {
                        player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                    }
                }
            }
            return false;
        }
        
        // 3. İŞLEM BAŞARILI (item zaten envanterden alındı, bankaya eklendi)
        
        player.sendMessage("§a" + amount + "x " + getItemDisplayName(item) + " bankaya yatırıldı!");
        return true;
    }
    
    /**
     * Item çekme
     */
    public boolean withdrawItem(Player player, Material material, int amount) {
        if (player == null || material == null || amount <= 0) return false;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return false;
        }
        
        // Yetki kontrolü
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (!hasWithdrawPermission(rank)) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        // Banka sandığı kontrolü
        Inventory bankChest = getBankChest(clan);
        if (bankChest == null) {
            player.sendMessage("§cKlan bankası bulunamadı!");
            return false;
        }
        
        // ⚠️ TRANSACTION MANTIĞI: Önce bankadan al, sonra envantere ekle (dupe önleme)
        // 1. ÖNCE BANKADAN AL (transaction başlat)
        ItemStack toRemove = new ItemStack(material, amount);
        HashMap<Integer, ItemStack> removeResult = bankChest.removeItem(toRemove);
        
        // removeItem() başarılı olursa boş HashMap döner, başarısız olursa kalan itemleri döner
        if (removeResult == null || !removeResult.isEmpty()) {
            // Bankadan alınamadı (yeterli item yok), işlem iptal
            player.sendMessage("§cBanka'da yeterli " + getMaterialDisplayName(material) + " yok!");
            return false;
        }
        
        // 2. SONRA ENVANTERE EKLE
        ItemStack withdrawItem = new ItemStack(material, amount);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(withdrawItem);
        
        if (!overflow.isEmpty()) {
            // Envanter dolu, item'i geri bankaya koy (rollback)
            player.sendMessage("§cEnvanterin dolu! Itemler bankaya geri konuldu.");
            for (ItemStack remaining : overflow.values()) {
                if (remaining != null) {
                    HashMap<Integer, ItemStack> refundResult = bankChest.addItem(remaining);
                    if (!refundResult.isEmpty()) {
                        // Banka da dolu (çok nadir), yere düşür
                        player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                    }
                }
            }
            return false;
        }
        
        // 3. İŞLEM BAŞARILI (item zaten bankadan alındı, envantere eklendi)
        player.sendMessage("§a" + amount + "x " + getMaterialDisplayName(material) + " bankadan çekildi!");
        return true;
    }
    
    /**
     * Çekme yetkisi kontrolü
     */
    private boolean hasWithdrawPermission(Clan.Rank rank) {
        if (rank == null) return false;
        
        // Config'den limit al
        if (config == null) return false;
        
        switch (rank) {
            case LEADER:
                return true; // Sınırsız
            case GENERAL:
                return config.canGeneralWithdraw();
            case ELITE:
                return config.canEliteWithdraw();
            case MEMBER:
                return config.canMemberWithdraw();
            case RECRUIT:
                return false; // Çekme yetkisi yok
            default:
                return false;
        }
    }
    
    /**
     * Otomatik maaş dağıtımı (scheduled task - optimize edilmiş)
     */
    public void distributeSalaries() {
        if (config == null || clanManager == null) return;
        
        long currentTime = System.currentTimeMillis();
        long salaryInterval = config.getSalaryInterval();
        
        // Null check
        java.util.Collection<Clan> allClans = clanManager.getAllClans();
        if (allClans == null || allClans.isEmpty()) return;
        
        // Rate limiting: Her tick'te maksimum 5 klan işle (lag önleme)
        int processedClans = 0;
        int maxClansPerTick = 5;
        
        for (Clan clan : allClans) {
            if (processedClans >= maxClansPerTick) break; // Rate limiting
            
            if (clan == null) continue;
            
            Inventory bankChest = getBankChest(clan);
            if (bankChest == null) continue;
            
            // Thread-safe: Copy of keySet kullan
            java.util.Set<UUID> memberIds = new java.util.HashSet<>(clan.getMembers().keySet());
            
            // Rate limiting: Her klan için maksimum 10 üye (lag önleme)
            int processedMembers = 0;
            int maxMembersPerClan = 10;
            
            // Klan üyelerine maaş dağıt
            for (UUID memberId : memberIds) {
                if (processedMembers >= maxMembersPerClan) break; // Rate limiting
                
                long lastSalary = lastSalaryTime.getOrDefault(memberId, 0L);
                
                // Geçersiz zaman kontrolü
                if (lastSalary < 0) {
                    lastSalary = 0L;
                }
                
                if (currentTime - lastSalary >= salaryInterval) {
                    distributeSalaryToMember(clan, memberId, bankChest);
                    lastSalaryTime.put(memberId, currentTime);
                    processedMembers++;
                }
            }
            
            processedClans++;
        }
    }
    
    /**
     * Üyeye maaş dağıt (null check ve exception handling ile)
     */
    private void distributeSalaryToMember(Clan clan, UUID memberId, Inventory bankChest) {
        if (clan == null || memberId == null || bankChest == null) return;
        
        // Config null check
        if (config == null) return;
        
        Clan.Rank rank = clan.getRank(memberId);
        if (rank == null) return;
        
        // Maaş itemleri al (config'den)
        Map<Material, Integer> salaryItems = config.getSalaryItems(rank);
        if (salaryItems == null || salaryItems.isEmpty()) return;
        
        Player member = Bukkit.getPlayer(memberId);
        if (member == null || !member.isOnline()) {
            // Offline, maaş bekleme listesine eklenebilir (gelecekte)
            return;
        }
        
        // Maaş itemlerini bankadan çek ve oyuncuya ver
        boolean anySalaryGiven = false;
        for (Map.Entry<Material, Integer> entry : salaryItems.entrySet()) {
            Material material = entry.getKey();
            int amount = entry.getValue();
            
            // Geçersiz değer kontrolü
            if (material == null || amount <= 0) continue;
            
            try {
                // Bankada yeterli var mı?
                ItemStack checkItem = new ItemStack(material, amount);
                if (bankChest.containsAtLeast(checkItem, amount)) {
                    ItemStack salaryItem = new ItemStack(material, amount);
                    
                    // Envanter dolu mu kontrol et
                    HashMap<Integer, ItemStack> overflow = member.getInventory().addItem(salaryItem);
                    
                    if (!overflow.isEmpty()) {
                        // Envanter dolu, geri bankaya koy
                        for (ItemStack remaining : overflow.values()) {
                            if (remaining != null) {
                                bankChest.addItem(remaining);
                            }
                        }
                    } else {
                        // Maaş verildi, bankadan çıkar
                        bankChest.removeItem(salaryItem);
                        anySalaryGiven = true;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Maaş dağıtımı hatası (Material: " + 
                    material + ", Amount: " + amount + "): " + e.getMessage());
            }
        }
        
        if (anySalaryGiven) {
            member.sendMessage("§aKlan maaşınız yatırıldı!");
        }
    }
    
    /**
     * Otomatik transfer kontratı oluştur
     */
    public boolean createTransferContract(Player player, UUID targetPlayerId, 
                                         Material material, int amount, long interval) {
        if (player == null || targetPlayerId == null || material == null || 
            amount <= 0 || interval <= 0) return false;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return false;
        }
        
        // Yetki kontrolü (Lider veya General)
        if (!rankSystem.hasPermission(clan, player.getUniqueId(), 
                ClanRankSystem.Permission.MANAGE_BANK)) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        // Kontrat oluştur
        TransferContract contract = new TransferContract();
        contract.setClanId(clan.getId());
        contract.setCreatorId(player.getUniqueId());
        contract.setTargetPlayerId(targetPlayerId);
        contract.setMaterial(material);
        contract.setAmount(amount);
        contract.setInterval(interval);
        contract.setLastTransferTime(0L);
        contract.setActive(true);
        
        // Kontrat listesine ekle (thread-safe)
        List<TransferContract> contracts = transferContracts.computeIfAbsent(
            clan.getId(), k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (contracts) {
            contracts.add(contract);
        }
        
        player.sendMessage("§aOtomatik transfer kontratı oluşturuldu!");
        return true;
    }
    
    /**
     * Otomatik transfer kontratlarını işle (scheduled task - optimize edilmiş)
     */
    public void processTransferContracts() {
        if (clanManager == null) return;
        
        long currentTime = System.currentTimeMillis();
        
        // Rate limiting: Her tick'te maksimum 10 kontrat işle (lag önleme)
        int processedContracts = 0;
        int maxContractsPerTick = 10;
        
        // Thread-safe: Copy of entrySet kullan
        java.util.Set<Map.Entry<UUID, List<TransferContract>>> entries = 
            new java.util.HashSet<>(transferContracts.entrySet());
        
        for (Map.Entry<UUID, List<TransferContract>> entry : entries) {
            if (processedContracts >= maxContractsPerTick) break; // Rate limiting
            
            UUID clanId = entry.getKey();
            List<TransferContract> contracts = entry.getValue();
            
            if (contracts == null || contracts.isEmpty()) continue;
            
            Clan clan = clanManager.getClanById(clanId);
            if (clan == null) {
                // Klan yok, kontratları temizle
                transferContracts.remove(clanId);
                continue;
            }
            
            Inventory bankChest = getBankChest(clan);
            if (bankChest == null) continue;
            
            // Thread-safe: Synchronized list işlemi
            synchronized (contracts) {
                // Aktif kontratları işle
                Iterator<TransferContract> iterator = contracts.iterator();
                while (iterator.hasNext() && processedContracts < maxContractsPerTick) {
                    TransferContract contract = iterator.next();
                    
                    if (contract == null) {
                        iterator.remove();
                        continue;
                    }
                    
                    if (!contract.isActive()) {
                        iterator.remove();
                        continue;
                    }
                    
                    // Geçersiz zaman kontrolü
                    if (contract.getLastTransferTime() < 0) {
                        contract.setLastTransferTime(0L);
                    }
                    
                    // Transfer zamanı geldi mi?
                    if (currentTime - contract.getLastTransferTime() >= contract.getInterval()) {
                        processTransferContract(contract, bankChest);
                        contract.setLastTransferTime(currentTime);
                        processedContracts++;
                    }
                }
            }
        }
    }
    
    /**
     * Transfer kontratını işle
     */
    private void processTransferContract(TransferContract contract, Inventory bankChest) {
        if (contract == null || bankChest == null) return;
        
        // Bankada yeterli item var mı?
        ItemStack checkItem = new ItemStack(contract.getMaterial(), contract.getAmount());
        if (!bankChest.containsAtLeast(checkItem, contract.getAmount())) {
            return; // Yeterli item yok, atla
        }
        
        // Hedef oyuncu online mı?
        Player targetPlayer = Bukkit.getPlayer(contract.getTargetPlayerId());
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            return; // Offline, atla
        }
        
        // Item oluştur
        ItemStack transferItem = new ItemStack(contract.getMaterial(), contract.getAmount());
        
        // Envanter dolu mu kontrol et
        HashMap<Integer, ItemStack> overflow = targetPlayer.getInventory().addItem(transferItem);
        
        if (!overflow.isEmpty()) {
            // Envanter dolu, geri bankaya koy
            for (ItemStack remaining : overflow.values()) {
                bankChest.addItem(remaining);
            }
            return;
        }
        
        // Transfer başarılı, bankadan çıkar
        bankChest.removeItem(transferItem);
        targetPlayer.sendMessage("§aOtomatik transfer: " + contract.getAmount() + "x " + 
            getMaterialDisplayName(contract.getMaterial()) + " alındı!");
    }
    
    /**
     * Item display name (Türkçe)
     */
    private String getItemDisplayName(ItemStack item) {
        if (item == null) return "Bilinmeyen";
        
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        
        return getMaterialDisplayName(item.getType());
    }
    
    /**
     * Material display name (Türkçe)
     */
    private String getMaterialDisplayName(Material material) {
        if (material == null) return "Bilinmeyen";
        
        // Basit Türkçe çeviri
        switch (material) {
            case DIAMOND: return "Elmas";
            case GOLD_INGOT: return "Altın";
            case IRON_INGOT: return "Demir";
            case EMERALD: return "Zümrüt";
            case NETHERITE_INGOT: return "Netherite";
            case OBSIDIAN: return "Obsidyen";
            default: return material.name();
        }
    }
    
    /**
     * Config getter
     */
    public ClanBankConfig getConfig() {
        return config;
    }
    
    /**
     * Son maaş zamanı
     */
    public long getLastSalaryTime(UUID playerId) {
        return lastSalaryTime.getOrDefault(playerId, 0L);
    }
    
    /**
     * Transfer kontratları
     */
    public List<TransferContract> getTransferContracts(UUID clanId) {
        return transferContracts.getOrDefault(clanId, new ArrayList<>());
    }
    
    /**
     * Transfer Kontratı Model
     */
    public static class TransferContract {
        private UUID clanId;
        private UUID creatorId;
        private UUID targetPlayerId;
        private Material material;
        private int amount;
        private long interval; // ms
        private long lastTransferTime;
        private boolean active;
        
        // Getters & Setters
        public UUID getClanId() { return clanId; }
        public void setClanId(UUID clanId) { this.clanId = clanId; }
        
        public UUID getCreatorId() { return creatorId; }
        public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
        
        public UUID getTargetPlayerId() { return targetPlayerId; }
        public void setTargetPlayerId(UUID targetPlayerId) { this.targetPlayerId = targetPlayerId; }
        
        public Material getMaterial() { return material; }
        public void setMaterial(Material material) { this.material = material; }
        
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        
        public long getInterval() { return interval; }
        public void setInterval(long interval) { this.interval = interval; }
        
        public long getLastTransferTime() { return lastTransferTime; }
        public void setLastTransferTime(long lastTransferTime) { this.lastTransferTime = lastTransferTime; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}

