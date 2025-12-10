package me.mami.stratocraft.manager.clan;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.clan.config.ClanMissionConfig;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klan Görevleri Sistemi
 * 
 * Özellikler:
 * - Klan görevleri oluşturma (Lider/General)
 * - Fiziksel görev tahtası (Lectern)
 * - Üye bazlı ilerleme takibi
 * - Otomatik görev tamamlama
 * - Ödül dağıtımı (Item-Based)
 */
public class ClanMissionSystem {
    private final Main plugin;
    private final ClanManager clanManager;
    private final ClanRankSystem rankSystem;
    private ClanMissionConfig config;
    
    // Klan -> Aktif görev
    private final Map<UUID, ClanMission> activeMissions = new ConcurrentHashMap<>();
    
    // Klan -> Görev tahtası konumu
    private final Map<UUID, Location> missionBoardLocations = new ConcurrentHashMap<>();
    
    public ClanMissionSystem(Main plugin, ClanManager clanManager, ClanRankSystem rankSystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.rankSystem = rankSystem;
        this.config = new ClanMissionConfig();
    }
    
    /**
     * Config yükle
     */
    public void loadConfig(org.bukkit.configuration.file.FileConfiguration fileConfig) {
        config.loadFromConfig(fileConfig);
    }
    
    /**
     * Görev tahtası oluştur (fiziksel: Lectern)
     */
    public boolean createMissionBoard(Player player, Location lecternLoc) {
        if (player == null || lecternLoc == null) return false;
        
        Block block = lecternLoc.getBlock();
        if (block.getType() != Material.LECTERN) {
            player.sendMessage("§cGörev tahtası için Lectern gerekli!");
            return false;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return false;
        }
        
        // Yetki kontrolü (Lider veya General)
        if (!rankSystem.hasPermission(clan, player.getUniqueId(), 
                ClanRankSystem.Permission.START_MISSION)) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        // Klan işareti kontrolü (Item Frame'de)
        boolean hasClanTag = checkForClanTag(lecternLoc);
        if (!hasClanTag) {
            player.sendMessage("§cGörev tahtası için Item Frame'e klan işareti gerekli!");
            return false;
        }
        
        // Metadata ekle
        block.setMetadata("ClanMissionBoard", new FixedMetadataValue(plugin, clan.getId().toString()));
        
        // Konumu kaydet
        missionBoardLocations.put(clan.getId(), lecternLoc);
        
        player.sendMessage("§aKlan görev tahtası oluşturuldu!");
        return true;
    }
    
    /**
     * Klan işareti kontrolü (Item Frame'de)
     */
    private boolean checkForClanTag(Location lecternLoc) {
        if (lecternLoc == null || lecternLoc.getWorld() == null) return false;
        
        try {
            for (org.bukkit.entity.Entity entity : lecternLoc.getWorld()
                    .getNearbyEntities(lecternLoc, 2, 2, 2)) {
                if (entity instanceof ItemFrame) {
                    ItemFrame frame = (ItemFrame) entity;
                    ItemStack item = frame.getItem();
                    if (item != null && item.getType() == Material.NAME_TAG) {
                        if (item.hasItemMeta()) {
                            String displayName = item.getItemMeta().getDisplayName();
                            if (displayName != null && displayName.contains("KLAN_GOREV")) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Klan işareti kontrolü hatası: " + e.getMessage());
            return false;
        }
        return false;
    }
    
    /**
     * Klan görevi oluştur
     */
    public boolean createMission(Player creator, MissionType type, int target, 
                                Material targetMaterial, String description) {
        if (creator == null || type == null || target <= 0) return false;
        
        Clan clan = clanManager.getClanByPlayer(creator.getUniqueId());
        if (clan == null) {
            creator.sendMessage("§cBir klana üye değilsin!");
            return false;
        }
        
        // Yetki kontrolü (Lider veya General)
        if (!rankSystem.hasPermission(clan, creator.getUniqueId(), 
                ClanRankSystem.Permission.START_MISSION)) {
            creator.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        // Config null check
        if (config == null) {
            creator.sendMessage("§cGörev sistemi yapılandırılmamış!");
            plugin.getLogger().warning("ClanMissionConfig null! Görev oluşturulamıyor.");
            return false;
        }
        
        // Zaten aktif görev var mı?
        if (activeMissions.containsKey(clan.getId())) {
            creator.sendMessage("§cKlanınızın zaten aktif bir görevi var!");
            return false;
        }
        
        // Görev oluştur
        ClanMission mission = new ClanMission();
        mission.setMissionId(UUID.randomUUID());
        mission.setClanId(clan.getId());
        mission.setCreatorId(creator.getUniqueId());
        mission.setType(type);
        mission.setTarget(target);
        mission.setTargetMaterial(targetMaterial);
        mission.setDescription(description != null ? description : getDefaultDescription(type, target, targetMaterial));
        mission.setProgress(0);
        mission.setMemberProgress(new ConcurrentHashMap<>());
        mission.setCreatedTime(System.currentTimeMillis());
        mission.setExpiryTime(System.currentTimeMillis() + config.getMissionDuration());
        mission.setActive(true);
        
        // Ödüller oluştur (config'den)
        Map<String, Object> rewards = createRewards(type, target);
        if (rewards == null) {
            creator.sendMessage("§cÖdüller oluşturulamadı!");
            return false;
        }
        mission.setRewards(rewards);
        
        // Görev tahtasına kitap koy
        Location boardLoc = missionBoardLocations.get(clan.getId());
        if (boardLoc != null) {
            placeMissionBook(boardLoc, mission);
        }
        
        // Aktif görevlere ekle
        activeMissions.put(clan.getId(), mission);
        
        // Tüm üyelere bildir
        broadcastToClan(clan, "§a§lYeni klan görevi oluşturuldu!");
        broadcastToClan(clan, "§e" + mission.getDescription());
        
        creator.sendMessage("§aKlan görevi oluşturuldu!");
        return true;
    }
    
    /**
     * Görev tahtasına kitap koy
     */
    private void placeMissionBook(Location boardLoc, ClanMission mission) {
        if (boardLoc == null || mission == null) return;
        
        // World null check
        if (boardLoc.getWorld() == null) {
            plugin.getLogger().warning("Görev tahtası dünyası null!");
            return;
        }
        
        Block block = boardLoc.getBlock();
        if (block == null || block.getType() != Material.LECTERN) return;
        
        try {
            if (block.getState() instanceof Lectern) {
                Lectern lectern = (Lectern) block.getState();
                
                // Kitap oluştur
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) book.getItemMeta();
                
                if (bookMeta != null) {
                    bookMeta.setTitle("§6Klan Görevi");
                    bookMeta.setAuthor("Klan Sistemi");
                    
                    // İçerik
                    List<String> pages = new ArrayList<>();
                    pages.add("§lKlan Görevi\n\n" + 
                        "§7Tip: §e" + getMissionTypeName(mission.getType()) + "\n" +
                        "§7Hedef: §e" + mission.getTarget() + "\n" +
                        "§7İlerleme: §e" + mission.getProgress() + "/" + mission.getTarget() + "\n\n" +
                        "§7Açıklama:\n" + (mission.getDescription() != null ? mission.getDescription() : ""));
                    
                    bookMeta.setPages(pages);
                    book.setItemMeta(bookMeta);
                    
                    lectern.getInventory().setItem(0, book);
                    lectern.update();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Görev kitabı yerleştirme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Görev ilerlemesi güncelle (optimize edilmiş)
     */
    public void updateMissionProgress(Clan clan, UUID memberId, MissionType type, int amount) {
        if (clan == null || memberId == null || type == null || amount <= 0) return;
        
        ClanMission mission = activeMissions.get(clan.getId());
        if (mission == null || !mission.isActive()) return;
        
        // Görev tipi eşleşiyor mu?
        if (mission.getType() != type) return;
        
        // Thread-safe: Synchronized
        synchronized (mission.getMemberProgress()) {
            // Üye ilerlemesi güncelle
            int currentProgress = mission.getMemberProgress().getOrDefault(memberId, 0);
            mission.getMemberProgress().put(memberId, currentProgress + amount);
            
            // Toplam ilerleme hesapla (optimize: sadece değişen değeri ekle)
            int oldTotal = mission.getProgress();
            int newTotal = oldTotal + amount;
            mission.setProgress(newTotal);
        }
        
        // Görev tamamlandı mı? (synchronized dışında kontrol et)
        if (mission.getProgress() >= mission.getTarget()) {
            completeMission(clan, mission);
        }
    }
    
    /**
     * Görev tamamla (optimize edilmiş - batch processing)
     */
    private void completeMission(Clan clan, ClanMission mission) {
        if (clan == null || mission == null) return;
        
        mission.setActive(false);
        
        // Thread-safe: Copy of keySet kullan
        java.util.Set<UUID> memberIds = new java.util.HashSet<>(clan.getMembers().keySet());
        
        // Batch processing: Tüm online oyuncuları bir kez al
        java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        
        // Rate limiting: Her tick'te maksimum 20 üye (lag önleme)
        int rewardedCount = 0;
        int processed = 0;
        int maxProcessPerTick = 20;
        
        for (Player member : onlinePlayers) {
            if (processed >= maxProcessPerTick) break; // Rate limiting
            
            if (member != null && member.isOnline() && memberIds.contains(member.getUniqueId())) {
                try {
                    if (distributeRewards(member, mission)) {
                        rewardedCount++;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Ödül dağıtımı hatası (Üye: " + 
                        member.getName() + "): " + e.getMessage());
                }
                processed++;
            }
        }
        
        // Broadcast
        broadcastToClan(clan, "§a§lKlan görevi tamamlandı!");
        broadcastToClan(clan, "§e" + rewardedCount + " üye ödül aldı!");
        
        // Görev tahtasından kitabı kaldır
        Location boardLoc = missionBoardLocations.get(clan.getId());
        if (boardLoc != null) {
            removeMissionBook(boardLoc);
        }
        
        // Aktif görevlerden çıkar
        activeMissions.remove(clan.getId());
    }
    
    /**
     * Ödül dağıt (null check ve exception handling ile)
     */
    private boolean distributeRewards(Player member, ClanMission mission) {
        if (member == null || mission == null) return false;
        
        // Rewards null check
        Map<String, Object> rewards = mission.getRewards();
        if (rewards == null) {
            plugin.getLogger().warning("Görev ödülleri null! Üye: " + member.getName());
            return false;
        }
        
        boolean anyRewardGiven = false;
        
        // Item ödülleri
        Object itemsObj = rewards.get("items");
        if (itemsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<ItemStack> rewardItems = (List<ItemStack>) itemsObj;
            
            if (rewardItems != null && !rewardItems.isEmpty()) {
                for (ItemStack reward : rewardItems) {
                    if (reward == null) continue;
                    
                    try {
                        // World null check
                        if (member.getWorld() == null) {
                            plugin.getLogger().warning("Oyuncu dünyası null! Üye: " + member.getName());
                            continue;
                        }
                        
                        HashMap<Integer, ItemStack> overflow = member.getInventory().addItem(reward);
                        if (!overflow.isEmpty()) {
                            // Envanter dolu, yere düşür
                            for (ItemStack remaining : overflow.values()) {
                                if (remaining != null) {
                                    member.getWorld().dropItemNaturally(member.getLocation(), remaining);
                                }
                            }
                        }
                        anyRewardGiven = true;
                    } catch (Exception e) {
                        plugin.getLogger().warning("Ödül dağıtımı hatası: " + e.getMessage());
                    }
                }
            }
        }
        
        // XP ödülü
        Object xpObj = rewards.get("xp");
        if (xpObj instanceof Integer) {
            Integer xpReward = (Integer) xpObj;
            if (xpReward != null && xpReward > 0) {
                try {
                    member.giveExp(xpReward);
                    anyRewardGiven = true;
                } catch (Exception e) {
                    plugin.getLogger().warning("XP ödülü hatası: " + e.getMessage());
                }
            }
        }
        
        if (anyRewardGiven) {
            member.sendMessage("§aKlan görevi ödülü alındı!");
        }
        
        return anyRewardGiven;
    }
    
    /**
     * Ödüller oluştur (config'den)
     */
    private Map<String, Object> createRewards(MissionType type, int target) {
        if (config == null) {
            plugin.getLogger().warning("ClanMissionConfig null! Varsayılan ödüller kullanılıyor.");
            // Varsayılan ödüller
            Map<String, Object> rewards = new HashMap<>();
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(Material.DIAMOND, 5));
            items.add(new ItemStack(Material.GOLD_INGOT, 10));
            rewards.put("items", items);
            rewards.put("xp", 100);
            return rewards;
        }
        
        Map<String, Object> rewards = new HashMap<>();
        
        // Item ödülleri (config'den)
        List<ItemStack> items = new ArrayList<>();
        int diamondReward = config.getRewardDiamond();
        int goldReward = config.getRewardGold();
        
        if (diamondReward > 0) {
            items.add(new ItemStack(Material.DIAMOND, diamondReward));
        }
        if (goldReward > 0) {
            items.add(new ItemStack(Material.GOLD_INGOT, goldReward));
        }
        
        rewards.put("items", items);
        
        // XP ödülü (config'den)
        rewards.put("xp", config.getRewardXP());
        
        return rewards;
    }
    
    /**
     * Varsayılan açıklama
     */
    private String getDefaultDescription(MissionType type, int target, Material targetMaterial) {
        switch (type) {
            case DEPOSIT_ITEM:
                return "Klan bankasına " + target + "x " + 
                    (targetMaterial != null ? getMaterialDisplayName(targetMaterial) : "item") + " yatır";
            case BUILD_STRUCTURE:
                return target + " yapı inşa et";
            case USE_RITUAL:
                return target + " ritüel yap";
            case WIN_WAR:
                return target + " klan savaşı kazan";
            default:
                return "Görev tamamla";
        }
    }
    
    /**
     * Görev tipi ismi (Türkçe)
     */
    private String getMissionTypeName(MissionType type) {
        if (type == null) return "Bilinmeyen";
        
        switch (type) {
            case DEPOSIT_ITEM: return "Kaynak Yatırma";
            case BUILD_STRUCTURE: return "Yapı İnşası";
            case USE_RITUAL: return "Ritüel Yapma";
            case WIN_WAR: return "Savaş Kazanma";
            default: return "Bilinmeyen";
        }
    }
    
    /**
     * Material display name (Türkçe)
     */
    private String getMaterialDisplayName(Material material) {
        if (material == null) return "Bilinmeyen";
        
        switch (material) {
            case DIAMOND: return "Elmas";
            case GOLD_INGOT: return "Altın";
            case IRON_INGOT: return "Demir";
            case EMERALD: return "Zümrüt";
            default: return material.name();
        }
    }
    
    /**
     * Görev tahtasından kitabı kaldır
     */
    private void removeMissionBook(Location boardLoc) {
        if (boardLoc == null) return;
        
        // World null check
        if (boardLoc.getWorld() == null) {
            plugin.getLogger().warning("Görev tahtası dünyası null!");
            return;
        }
        
        Block block = boardLoc.getBlock();
        if (block == null || block.getType() != Material.LECTERN) return;
        
        try {
            if (block.getState() instanceof Lectern) {
                Lectern lectern = (Lectern) block.getState();
                lectern.getInventory().clear();
                lectern.update();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Görev kitabı kaldırma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Klan üyelerine broadcast (optimize edilmiş)
     */
    private void broadcastToClan(Clan clan, String message) {
        if (clan == null || message == null) return;
        
        // Thread-safe: Copy of keySet kullan
        java.util.Set<UUID> memberIds = new java.util.HashSet<>(clan.getMembers().keySet());
        
        // Batch processing: Tüm online oyuncuları bir kez al
        java.util.Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        
        // Rate limiting: Her tick'te maksimum 50 üye (lag önleme)
        int processed = 0;
        int maxProcessPerTick = 50;
        
        for (Player member : onlinePlayers) {
            if (processed >= maxProcessPerTick) break; // Rate limiting
            
            if (member != null && member.isOnline() && memberIds.contains(member.getUniqueId())) {
                try {
                    member.sendMessage(message);
                } catch (Exception e) {
                    plugin.getLogger().warning("Broadcast hatası (Üye: " + 
                        member.getName() + "): " + e.getMessage());
                }
                processed++;
            }
        }
    }
    
    /**
     * Süresi dolmuş görevleri temizle (scheduled task - optimize edilmiş)
     */
    public void cleanupExpiredMissions() {
        if (clanManager == null) return;
        
        long currentTime = System.currentTimeMillis();
        
        // Thread-safe: Copy of entrySet kullan
        java.util.Set<Map.Entry<UUID, ClanMission>> entries = 
            new java.util.HashSet<>(activeMissions.entrySet());
        
        // Rate limiting: Her tick'te maksimum 10 görev (lag önleme)
        int processed = 0;
        int maxProcessPerTick = 10;
        
        Iterator<Map.Entry<UUID, ClanMission>> iterator = entries.iterator();
        while (iterator.hasNext() && processed < maxProcessPerTick) {
            Map.Entry<UUID, ClanMission> entry = iterator.next();
            ClanMission mission = entry.getValue();
            
            if (mission == null || !mission.isActive()) {
                activeMissions.remove(entry.getKey());
                processed++;
                continue;
            }
            
            // Geçersiz zaman kontrolü
            if (mission.getExpiryTime() < 0) {
                activeMissions.remove(entry.getKey());
                processed++;
                continue;
            }
            
            // Süresi dolmuş mu?
            if (currentTime > mission.getExpiryTime()) {
                try {
                    Clan clan = clanManager.getClanById(mission.getClanId());
                    if (clan != null) {
                        broadcastToClan(clan, "§cKlan görevi süresi doldu!");
                    }
                    
                    // Görev tahtasından kitabı kaldır
                    Location boardLoc = missionBoardLocations.get(mission.getClanId());
                    if (boardLoc != null) {
                        removeMissionBook(boardLoc);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Görev temizleme hatası: " + e.getMessage());
                }
                
                activeMissions.remove(entry.getKey());
                processed++;
            }
        }
    }
    
    /**
     * Config getter
     */
    public ClanMissionConfig getConfig() {
        return config;
    }
    
    /**
     * Aktif görev al
     */
    public ClanMission getActiveMission(Clan clan) {
        if (clan == null) return null;
        return activeMissions.get(clan.getId());
    }
    
    /**
     * Görev iptal et (Lider/General)
     */
    public boolean cancelMission(Clan clan, Player player) {
        if (clan == null || player == null) return false;
        
        // Yetki kontrolü
        if (!rankSystem.hasPermission(clan, player.getUniqueId(), 
                ClanRankSystem.Permission.START_MISSION)) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return false;
        }
        
        ClanMission mission = activeMissions.get(clan.getId());
        if (mission == null) {
            player.sendMessage("§cAktif görev bulunamadı!");
            return false;
        }
        
        // Görevi iptal et
        mission.setActive(false);
        activeMissions.remove(clan.getId());
        
        // Görev tahtasından kitabı kaldır
        Location boardLoc = missionBoardLocations.get(clan.getId());
        if (boardLoc != null) {
            removeMissionBook(boardLoc);
        }
        
        // Tüm üyelere bildir
        broadcastToClan(clan, "§c§lKlan görevi iptal edildi!");
        
        player.sendMessage("§aGörev iptal edildi!");
        return true;
    }
    
    /**
     * Görev Tipi Enum
     */
    public enum MissionType {
        DEPOSIT_ITEM,      // Kaynak yatırma (Item-Based)
        BUILD_STRUCTURE,   // Yapı inşası
        USE_RITUAL,        // Ritüel yapma
        WIN_WAR            // Savaş kazanma
    }
    
    /**
     * Klan Görevi Model
     */
    public static class ClanMission {
        private UUID missionId;
        private UUID clanId;
        private UUID creatorId;
        private MissionType type;
        private int target;
        private Material targetMaterial;
        private String description;
        private int progress;
        private Map<UUID, Integer> memberProgress; // Üye -> İlerleme
        private Map<String, Object> rewards;
        private long createdTime;
        private long expiryTime;
        private boolean active;
        
        // Getters & Setters
        public UUID getMissionId() { return missionId; }
        public void setMissionId(UUID missionId) { this.missionId = missionId; }
        
        public UUID getClanId() { return clanId; }
        public void setClanId(UUID clanId) { this.clanId = clanId; }
        
        public UUID getCreatorId() { return creatorId; }
        public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
        
        public MissionType getType() { return type; }
        public void setType(MissionType type) { this.type = type; }
        
        public int getTarget() { return target; }
        public void setTarget(int target) { this.target = target; }
        
        public Material getTargetMaterial() { return targetMaterial; }
        public void setTargetMaterial(Material targetMaterial) { this.targetMaterial = targetMaterial; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        
        public Map<UUID, Integer> getMemberProgress() { return memberProgress; }
        public void setMemberProgress(Map<UUID, Integer> memberProgress) { this.memberProgress = memberProgress; }
        
        public Map<String, Object> getRewards() { return rewards; }
        public void setRewards(Map<String, Object> rewards) { this.rewards = rewards; }
        
        public long getCreatedTime() { return createdTime; }
        public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
        
        public long getExpiryTime() { return expiryTime; }
        public void setExpiryTime(long expiryTime) { this.expiryTime = expiryTime; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}

