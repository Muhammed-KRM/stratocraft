# 🎯 3 SİSTEM DETAYLI ÇÖZÜM RAPORU

## 📋 İÇİNDEKİLER

1. [Sözleşme Sistemi - Detaylı Çözüm](#1-sözleşme-sistemi)
2. [Görev Sistemi - Detaylı Çözüm](#2-görev-sistemi)
3. [Alışveriş Sistemi - Detaylı Çözüm](#3-alışveriş-sistemi)
4. [Genel Öneriler ve Kod Yapıları](#genel-öneriler)

---

## 1. SÖZLEŞME SİSTEMİ

### 📊 Mevcut Durum Analizi

**Mevcut Kod:**
- `Contract.java`: Sadece `Material` ve `amount` var (basit malzeme kontratları)
- `ContractManager.java`: Sadece malzeme temini kontratları
- `punishBreach()`: Hain damgası var ama kalıcı can kaybı yok

**Eksikler:**
- ❌ Oyuncu öldürme kontratları (bounty hunting)
- ❌ Bölge yasakları (territory restrictions)
- ❌ Saldırmama anlaşmaları (non-aggression)
- ❌ Klanlar arası sözleşmeler
- ❌ Kalıcı can kaybı sistemi
- ❌ Kapsamlı ihlal takibi

---

### 🎯 Önerilen Çözüm

#### **1.1 Veri Yapısı (Contract.java Güncellemesi)**

```java
package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.List;
import java.util.UUID;

public class Contract {
    // Temel Bilgiler
    private UUID id = UUID.randomUUID();
    private final UUID issuer; // Sözleşmeyi veren
    private UUID acceptor = null; // Sözleşmeyi kabul eden
    private final ContractType type; // Sözleşme tipi
    private final ContractScope scope; // Oyuncu mu, klan mı?
    
    // Sözleşme Tipi Enum
    public enum ContractType {
        MATERIAL_DELIVERY,    // Malzeme temini
        PLAYER_KILL,          // Oyuncu öldürme (bounty)
        TERRITORY_RESTRICT,   // Bölge yasağı
        NON_AGGRESSION,       // Saldırmama
        BASE_PROTECTION,      // Base koruma
        STRUCTURE_BUILD       // Yapı inşa
    }
    
    // Kapsam Enum
    public enum ContractScope {
        PLAYER_TO_PLAYER,     // Oyuncu → Oyuncu
        CLAN_TO_CLAN,         // Klan → Klan
        PLAYER_TO_CLAN,       // Oyuncu → Klan
        CLAN_TO_PLAYER        // Klan → Oyuncu
    }
    
    // Malzeme Temini (Mevcut)
    private Material material;
    private int amount;
    private int delivered = 0;
    
    // Oyuncu Öldürme (Bounty)
    private UUID targetPlayer = null;
    
    // Bölge Yasağı
    private List<Location> restrictedAreas = null; // Yasak bölgeler (merkez + radius)
    private int restrictionRadius = 0;
    
    // Saldırmama Anlaşması
    private UUID nonAggressionTarget = null; // Saldırmama hedefi (oyuncu veya klan)
    private boolean isClanNonAggression = false; // Klan anlaşması mı?
    
    // Base Koruma
    private Location protectedBase = null;
    private long protectionDuration = 0; // Süre (milisaniye)
    
    // Yapı İnşa
    private String structureType = null; // Yapı tipi
    private Location buildLocation = null;
    
    // Ortak Alanlar
    private final double reward; // Ödül
    private final double penalty; // İhlal cezası
    private final long deadline; // Süre (milisaniye)
    private boolean completed = false;
    private boolean breached = false; // İhlal edildi mi?
    
    // Kan İmzası
    private boolean bloodSigned = false; // Kan ile imzalandı mı?
    private long signedTime = 0; // İmzalanma zamanı
    
    // Constructor'lar (her tip için)
    public Contract(UUID issuer, ContractType type, ContractScope scope, 
                   double reward, double penalty, long deadlineDays) {
        this.issuer = issuer;
        this.type = type;
        this.scope = scope;
        this.reward = reward;
        this.penalty = penalty;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
    }
    
    // Getter/Setter metodları...
    public ContractType getType() { return type; }
    public ContractScope getScope() { return scope; }
    public UUID getTargetPlayer() { return targetPlayer; }
    public void setTargetPlayer(UUID target) { this.targetPlayer = target; }
    public List<Location> getRestrictedAreas() { return restrictedAreas; }
    public void setRestrictedAreas(List<Location> areas) { this.restrictedAreas = areas; }
    public UUID getNonAggressionTarget() { return nonAggressionTarget; }
    public void setNonAggressionTarget(UUID target) { this.nonAggressionTarget = target; }
    public boolean isBloodSigned() { return bloodSigned; }
    public void setBloodSigned(boolean signed) { 
        this.bloodSigned = signed;
        this.signedTime = System.currentTimeMillis();
    }
    public boolean isBreached() { return breached; }
    public void setBreached(boolean breached) { this.breached = breached; }
}
```

---

#### **1.2 İhlal Takip Sistemi (ContractListener.java Güncellemesi)**

```java
package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ContractManager;
import me.mami.stratocraft.model.Contract;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ContractListener implements Listener {
    private final ContractManager contractManager;
    
    public ContractListener(ContractManager cm) {
        this.contractManager = cm;
    }
    
    /**
     * Oyuncu Hareket Takibi - Bölge Yasağı Kontrolü
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return; // Aynı blokta
        
        // Oyuncunun aktif sözleşmelerini kontrol et
        List<Contract> contracts = contractManager.getPlayerContracts(player.getUniqueId());
        
        for (Contract contract : contracts) {
            if (contract.getType() == Contract.ContractType.TERRITORY_RESTRICT) {
                // Yasak bölge kontrolü
                List<Location> restrictedAreas = contract.getRestrictedAreas();
                if (restrictedAreas != null) {
                    for (Location restrictedCenter : restrictedAreas) {
                        double distance = event.getTo().distance(restrictedCenter);
                        if (distance <= contract.getRestrictionRadius()) {
                            // İHLAL! Yasak bölgeye girdi
                            contractManager.breachContract(contract, player.getUniqueId(), 
                                "Yasak bölgeye girdi: " + restrictedCenter.getBlockX() + ", " + 
                                restrictedCenter.getBlockZ());
                            return;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Oyuncu Öldürme Takibi - Bounty Kontratları
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) return;
        
        // Bounty kontratı var mı?
        Contract bounty = contractManager.getBountyContract(victim.getUniqueId());
        if (bounty != null && bounty.getAcceptor() != null) {
            // Kontratı kabul eden öldürdü mü?
            if (bounty.getAcceptor().equals(killer.getUniqueId())) {
                contractManager.completeBountyContract(bounty, killer.getUniqueId());
            }
        }
    }
    
    /**
     * Saldırma Takibi - Non-Aggression Anlaşmaları
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // Saldırmama anlaşması var mı?
        Contract nonAggression = contractManager.getNonAggressionContract(
            attacker.getUniqueId(), victim.getUniqueId());
        
        if (nonAggression != null) {
            // İHLAL! Saldırmama anlaşması var ama saldırdı
            contractManager.breachContract(nonAggression, attacker.getUniqueId(),
                "Saldırmama anlaşmasını ihlal etti: " + victim.getName());
        }
    }
}
```

---

#### **1.3 Ceza Sistemi (ContractManager.java Güncellemesi)**

```java
package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Contract;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ContractManager {
    // Kalıcı can kaybı takibi (UUID -> Kayıp can sayısı)
    private final Map<UUID, Integer> permanentHealthLoss = new ConcurrentHashMap<>();
    
    /**
     * Sözleşme İhlali - Ciddi Ceza
     */
    public void breachContract(Contract contract, UUID violator, String reason) {
        if (contract.isBreached()) return; // Zaten ihlal edilmiş
        
        contract.setBreached(true);
        
        Player violatorPlayer = Bukkit.getPlayer(violator);
        if (violatorPlayer != null && violatorPlayer.isOnline()) {
            // 1. Kalıcı 2 Can Kaybı
            applyPermanentHealthLoss(violatorPlayer, 2);
            
            // 2. Hain Damgası
            applyTraitorTag(violatorPlayer);
            
            // 3. Para Cezası
            applyPenalty(violator, contract.getPenalty());
            
            // 4. Mesaj
            violatorPlayer.sendMessage("§4§l════════════════════════════");
            violatorPlayer.sendMessage("§4§lSÖZLEŞME İHLAL EDİLDİ!");
            violatorPlayer.sendMessage("§cSebep: §7" + reason);
            violatorPlayer.sendMessage("§cCeza: §7-2 Kalıcı Can, Hain Damgası");
            violatorPlayer.sendMessage("§4§l════════════════════════════");
        } else {
            // Oyuncu offline - veriyi kaydet
            permanentHealthLoss.put(violator, permanentHealthLoss.getOrDefault(violator, 0) + 2);
            // Giriş yaptığında cezayı uygula (PlayerJoinEvent'te)
        }
        
        // İssuer'a bildir
        Player issuer = Bukkit.getPlayer(contract.getIssuer());
        if (issuer != null && issuer.isOnline()) {
            issuer.sendMessage("§cSözleşmeniz ihlal edildi! " + 
                (violatorPlayer != null ? violatorPlayer.getName() : "Bilinmeyen"));
        }
    }
    
    /**
     * Kalıcı Can Kaybı Uygula
     */
    private void applyPermanentHealthLoss(Player player, int hearts) {
        // Mevcut kayıp can sayısını al
        int currentLoss = permanentHealthLoss.getOrDefault(player.getUniqueId(), 0);
        int newLoss = currentLoss + hearts;
        permanentHealthLoss.put(player.getUniqueId(), newLoss);
        
        // Maksimum canı düşür
        Attribute maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            double currentMax = maxHealthAttr.getBaseValue();
            double newMax = Math.max(1.0, currentMax - (hearts * 2.0)); // Her kalp = 2 can
            maxHealthAttr.setBaseValue(newMax);
            
            // Eğer mevcut can yeni maksimumdan fazlaysa, düşür
            if (player.getHealth() > newMax) {
                player.setHealth(newMax);
            }
        }
        
        // DataManager'a kaydet (kalıcı)
        // DataManager.savePlayerHealthLoss(player.getUniqueId(), newLoss);
    }
    
    /**
     * Hain Damgası Uygula
     */
    private void applyTraitorTag(Player player) {
        // Scoreboard Team'e ekle
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team traitorTeam = mainBoard.getTeam("TRAITOR");
        if (traitorTeam == null) {
            traitorTeam = mainBoard.registerNewTeam("TRAITOR");
            traitorTeam.setColor(ChatColor.RED);
            traitorTeam.setPrefix("§4[HAİN] ");
        }
        traitorTeam.addEntry(player.getName());
        
        // Display name'i değiştir
        player.setDisplayName("§4[HAİN] " + player.getName());
        
        // DataManager'a kaydet (kalıcı)
        // DataManager.saveTraitorTag(player.getUniqueId(), true);
    }
    
    /**
     * Para Cezası Uygula
     */
    private void applyPenalty(UUID playerId, double amount) {
        // EconomyManager kullan
        if (plugin.getEconomyManager() != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                plugin.getEconomyManager().withdrawPlayer(player, amount);
            }
        }
    }
    
    /**
     * Oyuncu giriş yaptığında kalıcı cezaları uygula
     */
    public void onPlayerJoin(Player player) {
        // Kalıcı can kaybı
        int healthLoss = permanentHealthLoss.getOrDefault(player.getUniqueId(), 0);
        if (healthLoss > 0) {
            applyPermanentHealthLoss(player, 0); // Mevcut kaybı uygula
        }
        
        // Hain damgası
        if (isTraitor(player.getUniqueId())) {
            applyTraitorTag(player);
        }
    }
}
```

---

#### **1.4 GUI Menü Tasarımı**

```java
package me.mami.stratocraft.gui;

import me.mami.stratocraft.model.Contract;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ContractMenu {
    
    /**
     * Ana Sözleşme Menüsü (54 slot - sayfalama)
     */
    public static Inventory createMainMenu(List<Contract> contracts, int page) {
        Inventory menu = Bukkit.createInventory(null, 54, "§6Aktif Sözleşmeler - Sayfa " + page);
        
        int startIndex = (page - 1) * 45; // Her sayfada 45 sözleşme
        int endIndex = Math.min(startIndex + 45, contracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = contracts.get(i);
            menu.setItem(slot, createContractItem(contract));
            slot++;
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", "§7Sayfa " + (page - 1)));
        }
        if (endIndex < contracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", "§7Sayfa " + (page + 1)));
        }
        
        menu.setItem(49, createButton(Material.BARRIER, "§cKapat", null));
        
        return menu;
    }
    
    /**
     * Sözleşme Detay Menüsü
     */
    public static Inventory createDetailMenu(Contract contract) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Sözleşme Detayları");
        
        // Sözleşme bilgileri
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta meta = infoItem.getItemMeta();
        meta.setDisplayName("§e" + getContractTypeName(contract.getType()));
        List<String> lore = new ArrayList<>();
        lore.add("§7İssuer: §e" + Bukkit.getOfflinePlayer(contract.getIssuer()).getName());
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        lore.add("§7Cezası: §c" + contract.getPenalty() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        
        // Tip'e göre özel bilgiler
        switch (contract.getType()) {
            case PLAYER_KILL:
                lore.add("§7Hedef: §c" + Bukkit.getOfflinePlayer(contract.getTargetPlayer()).getName());
                break;
            case TERRITORY_RESTRICT:
                lore.add("§7Yasak Bölgeler: §c" + contract.getRestrictedAreas().size() + " adet");
                break;
            case NON_AGGRESSION:
                lore.add("§7Hedef: §c" + Bukkit.getOfflinePlayer(contract.getNonAggressionTarget()).getName());
                break;
        }
        
        meta.setLore(lore);
        infoItem.setItemMeta(meta);
        menu.setItem(13, infoItem);
        
        // Kabul Et butonu
        if (contract.getAcceptor() == null) {
            menu.setItem(11, createButton(Material.EMERALD_BLOCK, "§a[Kabul Et]", "§7Kan imzası gerekli"));
        }
        
        // Reddet butonu
        menu.setItem(15, createButton(Material.REDSTONE_BLOCK, "§c[Reddet]", null));
        
        // Geri butonu
        menu.setItem(22, createButton(Material.ARROW, "§eGeri", null));
        
        return menu;
    }
    
    private static ItemStack createContractItem(Contract contract) {
        Material icon = getContractIcon(contract.getType());
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + getContractTypeName(contract.getType()));
        List<String> lore = new ArrayList<>();
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        if (contract.getAcceptor() != null) {
            lore.add("§7Kabul Eden: §e" + Bukkit.getOfflinePlayer(contract.getAcceptor()).getName());
        } else {
            lore.add("§7Durum: §aAçık");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static Material getContractIcon(Contract.ContractType type) {
        switch (type) {
            case PLAYER_KILL: return Material.DIAMOND_SWORD;
            case TERRITORY_RESTRICT: return Material.BARRIER;
            case NON_AGGRESSION: return Material.SHIELD;
            case BASE_PROTECTION: return Material.BEACON;
            default: return Material.PAPER;
        }
    }
}
```

---

## 2. GÖREV SİSTEMİ

### 📊 Mevcut Durum Analizi

**Mevcut Kod:**
- `Mission.java`: Sadece `KILL_MOB` ve `GATHER_ITEM` var
- `MissionManager.java`: Basit rastgele görev atama (totem seviyesine göre)
- `MissionListener.java`: Sadece `EntityDeathEvent` ve `PlayerInteractEvent` var

**Eksikler:**
- ❌ Lokasyon ziyareti görevleri (visit location)
- ❌ Yapı inşa görevleri (build structure)
- ❌ Seviye bazlı rastgele görev üretimi
- ❌ Kapsamlı ilerleme takibi
- ❌ GUI menü

---

### 🎯 Önerilen Çözüm

#### **2.1 Veri Yapısı (Mission.java Güncellemesi)**

```java
package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Mission {
    // Görev Tipi Enum
    public enum Type {
        KILL_MOB,              // Mob öldür
        GATHER_ITEM,           // Malzeme topla
        VISIT_LOCATION,        // Lokasyon ziyaret et
        BUILD_STRUCTURE,       // Yapı inşa et
        KILL_PLAYER,           // Oyuncu öldür
        CRAFT_ITEM,            // Item craft et
        MINE_BLOCK,            // Blok kaz
        TRAVEL_DISTANCE        // Mesafe kat et
    }
    
    // Görev Zorluk Seviyesi
    public enum Difficulty {
        EASY,      // Kolay (Seviye 1)
        MEDIUM,    // Orta (Seviye 2-3)
        HARD,      // Zor (Seviye 4-5)
        EXPERT     // Uzman (Seviye 5+)
    }
    
    private final UUID id = UUID.randomUUID();
    private final UUID playerId;
    private final Type type;
    private final Difficulty difficulty;
    
    // Hedefler (tip'e göre)
    private EntityType targetEntity = null;      // KILL_MOB için
    private Material targetMaterial = null;      // GATHER_ITEM, CRAFT_ITEM için
    private Location targetLocation = null;      // VISIT_LOCATION için
    private String structureType = null;         // BUILD_STRUCTURE için
    private UUID targetPlayer = null;            // KILL_PLAYER için
    private int targetDistance = 0;              // TRAVEL_DISTANCE için
    
    // İlerleme
    private int targetAmount;
    private int progress = 0;
    
    // Ödül
    private final ItemStack reward;
    private final double rewardMoney;
    
    // Süre
    private final long deadline; // Süre (milisaniye)
    
    // Constructor'lar
    public Mission(UUID playerId, Type type, Difficulty difficulty, 
                   int targetAmount, ItemStack reward, double rewardMoney, long deadlineDays) {
        this.playerId = playerId;
        this.type = type;
        this.difficulty = difficulty;
        this.targetAmount = targetAmount;
        this.reward = reward;
        this.rewardMoney = rewardMoney;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
    }
    
    // Getter/Setter metodları...
    public Type getType() { return type; }
    public Difficulty getDifficulty() { return difficulty; }
    public Location getTargetLocation() { return targetLocation; }
    public void setTargetLocation(Location loc) { this.targetLocation = loc; }
    public String getStructureType() { return structureType; }
    public void setStructureType(String type) { this.structureType = type; }
    public UUID getTargetPlayer() { return targetPlayer; }
    public void setTargetPlayer(UUID target) { this.targetPlayer = target; }
    public int getTargetDistance() { return targetDistance; }
    public void setTargetDistance(int distance) { this.targetDistance = distance; }
    public boolean isExpired() { return System.currentTimeMillis() > deadline; }
}
```

---

#### **2.2 Rastgele Görev Üretimi (MissionManager.java Güncellemesi)**

```java
package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Mission;
import me.mami.stratocraft.manager.DifficultyManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class MissionManager {
    private final Map<UUID, Mission> activeMissions = new HashMap<>();
    private final Random random = new Random();
    private final DifficultyManager difficultyManager;
    
    public MissionManager(DifficultyManager dm) {
        this.difficultyManager = dm;
    }
    
    /**
     * Seviyeye göre rastgele görev üret
     */
    public Mission generateRandomMission(Player player) {
        // Oyuncunun seviyesini al (DifficultyManager'dan)
        int playerLevel = difficultyManager.getPlayerLevel(player.getUniqueId());
        
        // Seviyeye göre zorluk belirle
        Mission.Difficulty difficulty = getDifficultyByLevel(playerLevel);
        
        // Rastgele görev tipi seç
        Mission.Type[] availableTypes = getAvailableTypes(difficulty);
        Mission.Type selectedType = availableTypes[random.nextInt(availableTypes.length)];
        
        // Görev oluştur
        return createMissionByType(player, selectedType, difficulty);
    }
    
    /**
     * Seviyeye göre zorluk belirle
     */
    private Mission.Difficulty getDifficultyByLevel(int level) {
        if (level <= 1) return Mission.Difficulty.EASY;
        if (level <= 3) return Mission.Difficulty.MEDIUM;
        if (level <= 5) return Mission.Difficulty.HARD;
        return Mission.Difficulty.EXPERT;
    }
    
    /**
     * Zorluğa göre mevcut görev tipleri
     */
    private Mission.Type[] getAvailableTypes(Mission.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return new Mission.Type[]{
                    Mission.Type.KILL_MOB,
                    Mission.Type.GATHER_ITEM,
                    Mission.Type.MINE_BLOCK
                };
            case MEDIUM:
                return new Mission.Type[]{
                    Mission.Type.KILL_MOB,
                    Mission.Type.GATHER_ITEM,
                    Mission.Type.VISIT_LOCATION,
                    Mission.Type.CRAFT_ITEM
                };
            case HARD:
                return new Mission.Type[]{
                    Mission.Type.KILL_MOB,
                    Mission.Type.VISIT_LOCATION,
                    Mission.Type.BUILD_STRUCTURE,
                    Mission.Type.KILL_PLAYER
                };
            case EXPERT:
                return new Mission.Type[]{
                    Mission.Type.BUILD_STRUCTURE,
                    Mission.Type.KILL_PLAYER,
                    Mission.Type.TRAVEL_DISTANCE
                };
        }
        return new Mission.Type[]{Mission.Type.KILL_MOB};
    }
    
    /**
     * Tip'e göre görev oluştur
     */
    private Mission createMissionByType(Player player, Mission.Type type, Mission.Difficulty difficulty) {
        int targetAmount = getTargetAmountByDifficulty(difficulty, type);
        ItemStack reward = getRewardByDifficulty(difficulty);
        double rewardMoney = getRewardMoneyByDifficulty(difficulty);
        long deadlineDays = getDeadlineByDifficulty(difficulty);
        
        Mission mission = new Mission(player.getUniqueId(), type, difficulty, 
                                     targetAmount, reward, rewardMoney, deadlineDays);
        
        // Tip'e göre hedef belirle
        switch (type) {
            case KILL_MOB:
                mission.setTargetEntity(getRandomMobByDifficulty(difficulty));
                break;
            case GATHER_ITEM:
                mission.setTargetMaterial(getRandomMaterialByDifficulty(difficulty));
                break;
            case VISIT_LOCATION:
                mission.setTargetLocation(generateRandomLocation(player.getLocation(), difficulty));
                break;
            case BUILD_STRUCTURE:
                mission.setStructureType(getRandomStructureByDifficulty(difficulty));
                break;
            case KILL_PLAYER:
                // Rastgele bir online oyuncu seç (kendisi hariç)
                mission.setTargetPlayer(getRandomOnlinePlayer(player));
                break;
            case TRAVEL_DISTANCE:
                mission.setTargetDistance(getTargetDistanceByDifficulty(difficulty));
                break;
        }
        
        return mission;
    }
    
    /**
     * Zorluğa göre hedef miktar
     */
    private int getTargetAmountByDifficulty(Mission.Difficulty difficulty, Mission.Type type) {
        int base = switch (type) {
            case KILL_MOB, GATHER_ITEM -> 10;
            case VISIT_LOCATION -> 1;
            case BUILD_STRUCTURE -> 1;
            case KILL_PLAYER -> 1;
            case TRAVEL_DISTANCE -> 1000; // Blok cinsinden
            default -> 10;
        };
        
        return switch (difficulty) {
            case EASY -> base;
            case MEDIUM -> base * 2;
            case HARD -> base * 3;
            case EXPERT -> base * 5;
        };
    }
    
    /**
     * Rastgele lokasyon üret (oyuncunun konumuna göre)
     */
    private Location generateRandomLocation(Location playerLoc, Mission.Difficulty difficulty) {
        int radius = switch (difficulty) {
            case EASY -> 500;      // 500 blok
            case MEDIUM -> 1000;   // 1000 blok
            case HARD -> 2000;      // 2000 blok
            case EXPERT -> 5000;    // 5000 blok
        };
        
        int x = playerLoc.getBlockX() + random.nextInt(radius * 2) - radius;
        int z = playerLoc.getBlockZ() + random.nextInt(radius * 2) - radius;
        int y = playerLoc.getWorld().getHighestBlockYAt(x, z);
        
        return new Location(playerLoc.getWorld(), x, y, z);
    }
    
    // Diğer yardımcı metodlar...
}
```

---

#### **2.3 İlerleme Takibi (MissionListener.java Güncellemesi)**

```java
package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.MissionManager;
import me.mami.stratocraft.model.Mission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class MissionListener implements Listener {
    private final MissionManager missionManager;
    
    // Lokasyon ziyareti için takip (her oyuncu için son konum)
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    
    public MissionListener(MissionManager mm) {
        this.missionManager = mm;
    }
    
    /**
     * Mob Öldürme Takibi
     */
    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        missionManager.handleKill(killer, event.getEntityType());
    }
    
    /**
     * Oyuncu Öldürme Takibi
     */
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;
        missionManager.handlePlayerKill(killer, victim);
    }
    
    /**
     * Lokasyon Ziyareti Takibi
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;
        
        // Her 5 saniyede bir kontrol et (performans için)
        Location lastLoc = lastLocations.get(player.getUniqueId());
        if (lastLoc != null && lastLoc.distance(to) < 10) return; // Çok yakın
        
        lastLocations.put(player.getUniqueId(), to);
        missionManager.handleLocationVisit(player, to);
    }
    
    /**
     * Yapı İnşa Takibi
     */
    @EventHandler
    public void onStructureBuild(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material placed = event.getBlockPlaced().getType();
        
        // Yapı pattern kontrolü (StructureActivationListener'dan)
        // Eğer yapı aktive edildiyse, MissionManager'a bildir
        missionManager.handleStructureBuild(player, event.getBlockPlaced().getLocation());
    }
    
    /**
     * Item Craft Takibi
     */
    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Material crafted = event.getRecipe().getResult().getType();
        missionManager.handleCraft(player, crafted);
    }
    
    /**
     * Mesafe Kat Etme Takibi
     */
    @EventHandler
    public void onPlayerMoveDistance(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        
        double distance = from.distance(to);
        if (distance > 0.1) { // Gerçek hareket
            missionManager.handleTravel(player, distance);
        }
    }
}
```

---

#### **2.4 GUI Menü Tasarımı**

```java
package me.mami.stratocraft.gui;

import me.mami.stratocraft.model.Mission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class MissionMenu {
    
    /**
     * Ana Görev Menüsü
     */
    public static Inventory createMainMenu(Mission mission) {
        Inventory menu = Bukkit.createInventory(null, 27, "§eGörev Menüsü");
        
        if (mission == null) {
            // Görev yok - Yeni görev al butonu
            menu.setItem(13, createButton(Material.EMERALD, "§aYeni Görev Al", 
                "§7Totem'e sağ tık yap"));
            return menu;
        }
        
        // Aktif görev bilgileri
        ItemStack missionItem = new ItemStack(Material.BOOK);
        ItemMeta meta = missionItem.getItemMeta();
        meta.setDisplayName("§e" + getMissionTypeName(mission.getType()));
        List<String> lore = new ArrayList<>();
        lore.add("§7Zorluk: §e" + mission.getDifficulty().name());
        lore.add("§7İlerleme: §a" + mission.getProgress() + "§7/§a" + mission.getTargetAmount());
        lore.add("§7Süre: §e" + formatTime(mission.getDeadline()));
        
        // Tip'e göre hedef bilgisi
        switch (mission.getType()) {
            case KILL_MOB:
                lore.add("§7Hedef: §c" + mission.getTargetEntity().name());
                break;
            case GATHER_ITEM:
                lore.add("§7Hedef: §e" + mission.getTargetMaterial().name());
                break;
            case VISIT_LOCATION:
                Location loc = mission.getTargetLocation();
                lore.add("§7Hedef: §e" + loc.getBlockX() + ", " + loc.getBlockZ());
                break;
            case BUILD_STRUCTURE:
                lore.add("§7Hedef: §e" + mission.getStructureType());
                break;
        }
        
        // Ödül bilgisi
        lore.add("§7Ödül: §a" + mission.getRewardMoney() + " Altın");
        if (mission.getReward() != null) {
            lore.add("§7+ " + mission.getReward().getType().name());
        }
        
        meta.setLore(lore);
        missionItem.setItemMeta(meta);
        menu.setItem(13, missionItem);
        
        // İlerleme barı (görsel)
        int progressPercent = (mission.getProgress() * 100) / mission.getTargetAmount();
        int filledSlots = (progressPercent * 9) / 100;
        for (int i = 0; i < 9; i++) {
            if (i < filledSlots) {
                menu.setItem(i, new ItemStack(Material.GREEN_STAINED_GLASS_PANE));
            } else {
                menu.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        }
        
        // Ödül önizleme
        if (mission.getReward() != null) {
            menu.setItem(15, mission.getReward());
        }
        
        // Tamamlandıysa teslim et butonu
        if (mission.isCompleted()) {
            menu.setItem(22, createButton(Material.EMERALD_BLOCK, "§a[Teslim Et]", 
                "§7Ödülü al"));
        }
        
        return menu;
    }
}
```

---

## 3. ALIŞVERİŞ SİSTEMİ

### 📊 Mevcut Durum Analizi

**Mevcut Kod:**
- `Shop.java`: Sadece `sellingItem` ve `priceItem` var
- `ShopManager.java`: Basit satın alma sistemi (sandık tabanlı)
- Teklif sistemi yok

**Eksikler:**
- ❌ GUI menü
- ❌ Teklif sistemi (counter offer)
- ❌ Bildirim sistemi
- ❌ Filtreleme ve arama

---

### 🎯 Önerilen Çözüm

#### **3.1 Veri Yapısı (Shop.java Güncellemesi)**

```java
package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Shop {
    private final UUID ownerId;
    private final Location location;
    private final ItemStack sellingItem;
    private final ItemStack priceItem;
    private final boolean protectedZone;
    
    // Teklif Sistemi
    private final List<Offer> offers = new ArrayList<>(); // Gelen teklifler
    
    // Shop ayarları
    private boolean acceptOffers = true; // Teklif kabul ediyor mu?
    private int maxOffers = 10; // Maksimum teklif sayısı
    
    public static class Offer {
        private final UUID offerer; // Teklif veren
        private final ItemStack offerItem; // Teklif edilen item
        private final int offerAmount; // Teklif miktarı
        private final long offerTime; // Teklif zamanı
        private boolean accepted = false; // Kabul edildi mi?
        private boolean rejected = false; // Reddedildi mi?
        
        public Offer(UUID offerer, ItemStack offerItem, int offerAmount) {
            this.offerer = offerer;
            this.offerItem = offerItem;
            this.offerAmount = offerAmount;
            this.offerTime = System.currentTimeMillis();
        }
        
        // Getter/Setter metodları...
    }
    
    public List<Offer> getOffers() { return offers; }
    public void addOffer(Offer offer) { 
        if (offers.size() < maxOffers) {
            offers.add(offer);
        }
    }
    public void removeOffer(Offer offer) { offers.remove(offer); }
    public boolean isAcceptOffers() { return acceptOffers; }
    public void setAcceptOffers(boolean accept) { this.acceptOffers = accept; }
}
```

---

#### **3.2 Teklif Sistemi (ShopManager.java Güncellemesi)**

```java
package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopManager {
    
    /**
     * Teklif Gönder
     */
    public void sendOffer(Player offerer, Shop shop, ItemStack offerItem, int offerAmount) {
        if (!shop.isAcceptOffers()) {
            offerer.sendMessage("§cBu mağaza teklif kabul etmiyor!");
            return;
        }
        
        if (shop.getOffers().size() >= shop.getMaxOffers()) {
            offerer.sendMessage("§cBu mağazaya maksimum teklif sayısına ulaşıldı!");
            return;
        }
        
        // Teklif oluştur
        Shop.Offer offer = new Shop.Offer(offerer.getUniqueId(), offerItem, offerAmount);
        shop.addOffer(offer);
        
        // Mağaza sahibine bildirim gönder
        Player owner = Bukkit.getPlayer(shop.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§e════════════════════════════");
            owner.sendMessage("§eYENİ TEKLİF ALDIN!");
            owner.sendMessage("§7Teklif Veren: §e" + offerer.getName());
            owner.sendMessage("§7İstediğin: §e" + shop.getPriceItem().getType().name() + 
                            " x" + shop.getPriceItem().getAmount());
            owner.sendMessage("§7Teklif: §a" + offerItem.getType().name() + " x" + offerAmount);
            owner.sendMessage("§e[Kabul Et] [Reddet]");
            owner.sendMessage("§e════════════════════════════");
            
            // ActionBar bildirimi
            owner.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                    "§eYeni teklif! /shop offers komutunu kullan"));
        }
        
        // Teklif verene onay
        offerer.sendMessage("§aTeklifin gönderildi! Mağaza sahibi bildirildi.");
    }
    
    /**
     * Teklif Kabul Et
     */
    public void acceptOffer(Player owner, Shop shop, Shop.Offer offer) {
        if (!shop.getOwnerId().equals(owner.getUniqueId())) {
            owner.sendMessage("§cBu mağaza sana ait değil!");
            return;
        }
        
        Player offerer = Bukkit.getPlayer(offer.getOfferer());
        if (offerer == null || !offerer.isOnline()) {
            owner.sendMessage("§cTeklif veren oyuncu offline!");
            return;
        }
        
        // Teklif verenin envanterinde teklif item'ı var mı?
        if (!offerer.getInventory().containsAtLeast(offer.getOfferItem(), offer.getOfferAmount())) {
            owner.sendMessage("§cTeklif veren oyuncunun envanterinde yeterli item yok!");
            offerer.sendMessage("§cTeklifin kabul edildi ama envanterinde yeterli item yok!");
            return;
        }
        
        // Mağaza sahibinin envanterinde satılan item var mı?
        if (!owner.getInventory().containsAtLeast(shop.getSellingItem(), shop.getSellingItem().getAmount())) {
            owner.sendMessage("§cEnvanterinde satılan item yok!");
            return;
        }
        
        // Takas yap
        // 1. Teklif verenden teklif item'ını al
        offerer.getInventory().removeItem(new ItemStack(offer.getOfferItem().getType(), offer.getOfferAmount()));
        
        // 2. Mağaza sahibinden satılan item'ı al
        owner.getInventory().removeItem(shop.getSellingItem());
        
        // 3. Teklif verene satılan item'ı ver
        offerer.getInventory().addItem(shop.getSellingItem());
        
        // 4. Mağaza sahibine teklif item'ını ver
        owner.getInventory().addItem(new ItemStack(offer.getOfferItem().getType(), offer.getOfferAmount()));
        
        // Mesajlar
        owner.sendMessage("§aTeklif kabul edildi! Takas tamamlandı.");
        offerer.sendMessage("§aTeklifin kabul edildi! Takas tamamlandı.");
        
        // Teklifi listeden kaldır
        offer.setAccepted(true);
        shop.removeOffer(offer);
    }
    
    /**
     * Teklif Reddet
     */
    public void rejectOffer(Player owner, Shop shop, Shop.Offer offer) {
        if (!shop.getOwnerId().equals(owner.getUniqueId())) {
            owner.sendMessage("§cBu mağaza sana ait değil!");
            return;
        }
        
        Player offerer = Bukkit.getPlayer(offer.getOfferer());
        if (offerer != null && offerer.isOnline()) {
            offerer.sendMessage("§cTeklifin reddedildi.");
        }
        
        offer.setRejected(true);
        shop.removeOffer(offer);
    }
}
```

---

#### **3.3 GUI Menü Tasarımı**

```java
package me.mami.stratocraft.gui;

import me.mami.stratocraft.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class ShopMenu {
    
    /**
     * Mağaza Menüsü
     */
    public static Inventory createShopMenu(Shop shop) {
        Inventory menu = Bukkit.createInventory(null, 27, "§aMarket");
        
        // Satılan item (Slot 11)
        ItemStack sellingItem = shop.getSellingItem().clone();
        ItemMeta sellingMeta = sellingItem.getItemMeta();
        List<String> sellingLore = new ArrayList<>();
        sellingLore.add("§7Satılan Item");
        sellingLore.add("§7Miktar: §e" + sellingItem.getAmount());
        sellingMeta.setLore(sellingLore);
        sellingItem.setItemMeta(sellingMeta);
        menu.setItem(11, sellingItem);
        
        // İstenen item (Slot 13)
        ItemStack priceItem = shop.getPriceItem().clone();
        ItemMeta priceMeta = priceItem.getItemMeta();
        List<String> priceLore = new ArrayList<>();
        priceLore.add("§7İstenen Ödeme");
        priceLore.add("§7Miktar: §e" + priceItem.getAmount());
        priceMeta.setLore(priceLore);
        priceItem.setItemMeta(priceMeta);
        menu.setItem(13, priceItem);
        
        // Satın Al butonu (Slot 15)
        menu.setItem(15, createButton(Material.EMERALD_BLOCK, "§a[Satın Al]", 
            "§7Klasik satın alma"));
        
        // Teklif Ver butonu (Slot 17)
        if (shop.isAcceptOffers()) {
            menu.setItem(17, createButton(Material.GOLD_BLOCK, "§e[Teklif Ver]", 
                "§7Alternatif ödeme teklif et"));
        }
        
        // Teklifler butonu (Slot 22) - Sadece mağaza sahibi için
        if (shop.getOffers().size() > 0) {
            menu.setItem(22, createButton(Material.PAPER, "§eTeklifler (" + 
                shop.getOffers().size() + ")", "§7Gelen teklifleri gör"));
        }
        
        // Kapat butonu (Slot 26)
        menu.setItem(26, createButton(Material.BARRIER, "§cKapat", null));
        
        return menu;
    }
    
    /**
     * Teklif Verme Menüsü
     */
    public static Inventory createOfferMenu(Shop shop) {
        Inventory menu = Bukkit.createInventory(null, 27, "§eTeklif Ver");
        
        // İstenen item bilgisi (Slot 4)
        ItemStack wantedItem = shop.getPriceItem().clone();
        ItemMeta wantedMeta = wantedItem.getItemMeta();
        List<String> wantedLore = new ArrayList<>();
        wantedLore.add("§7Mağaza sahibi bunu istiyor:");
        wantedLore.add("§7" + wantedItem.getType().name() + " x" + wantedItem.getAmount());
        wantedMeta.setLore(wantedLore);
        wantedItem.setItemMeta(wantedMeta);
        menu.setItem(4, wantedItem);
        
        // Teklif item'ı seç (Slot 13) - Oyuncu envanterinden seçecek
        menu.setItem(13, createButton(Material.CHEST, "§eTeklif Item'ı Seç", 
            "§7Envanterinden item seç"));
        
        // Miktar ayarla (Slot 11, 15)
        menu.setItem(11, createButton(Material.REDSTONE, "§c-1", "§7Miktar azalt"));
        menu.setItem(15, createButton(Material.EMERALD, "§a+1", "§7Miktar artır"));
        
        // Teklif Gönder butonu (Slot 22)
        menu.setItem(22, createButton(Material.EMERALD_BLOCK, "§a[Teklif Gönder]", null));
        
        // Geri butonu (Slot 18)
        menu.setItem(18, createButton(Material.ARROW, "§eGeri", null));
        
        return menu;
    }
    
    /**
     * Teklifler Listesi Menüsü (Mağaza sahibi için)
     */
    public static Inventory createOffersMenu(Shop shop) {
        Inventory menu = Bukkit.createInventory(null, 54, "§eGelen Teklifler");
        
        int slot = 0;
        for (Shop.Offer offer : shop.getOffers()) {
            if (slot >= 45) break; // 45 slot yeterli
            
            ItemStack offerItem = new ItemStack(offer.getOfferItem().getType(), offer.getOfferAmount());
            ItemMeta meta = offerItem.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Teklif Veren: §e" + Bukkit.getOfflinePlayer(offer.getOfferer()).getName());
            lore.add("§7Teklif: §a" + offer.getOfferItem().getType().name() + " x" + offer.getOfferAmount());
            lore.add("§7Zaman: §e" + formatTime(offer.getOfferTime()));
            lore.add("");
            lore.add("§a[Kabul Et]");
            lore.add("§c[Reddet]");
            meta.setLore(lore);
            offerItem.setItemMeta(meta);
            menu.setItem(slot, offerItem);
            slot++;
        }
        
        // Kapat butonu (Slot 49)
        menu.setItem(49, createButton(Material.BARRIER, "§cKapat", null));
        
        return menu;
    }
}
```

---

## GENEL ÖNERİLER

### 📝 Kod Yapısı Önerileri

1. **Event Priority Kullanımı:**
   - `EventPriority.MONITOR` - Sadece takip için
   - `EventPriority.HIGH` - Önemli kontroller için

2. **Performans Optimizasyonu:**
   - Chunk-based cache (TerritoryManager gibi)
   - Event-based cache güncelleme
   - Async işlemler (veritabanı kayıtları)

3. **Veri Saklama:**
   - `DataManager` ile kalıcı kayıt
   - UUID tabanlı takip
   - JSON/MySQL entegrasyonu

4. **GUI Menü Best Practices:**
   - Sayfalama sistemi (54 slot menüler)
   - Confirmation menüleri (önemli işlemler için)
   - ItemStack metadata ile veri saklama (PDC)

---

### 🎯 Uygulama Sırası

1. **Sözleşme Sistemi** (En karmaşık)
   - Veri yapısı güncellemesi
   - İhlal takip sistemi
   - Ceza sistemi
   - GUI menü

2. **Görev Sistemi** (Orta karmaşık)
   - Veri yapısı güncellemesi
   - Rastgele görev üretimi
   - İlerleme takibi
   - GUI menü

3. **Alışveriş Sistemi** (En basit)
   - Teklif sistemi
   - GUI menü
   - Bildirim sistemi

---

**🎮 Bu rapor, 3 sistemin detaylı çözümlerini içermektedir. Her sistem için veri yapıları, event handling, GUI menüler ve kod örnekleri sunulmuştur.**

