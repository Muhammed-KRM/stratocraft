# 💎 Klan Kristali Can Sistemi - Detaylı Dokümantasyon

## 📋 İÇİNDEKİLER

1. [Temel Can Sistemi](#1-temel-can-sistemi)
2. [Kristal Güçlendirme Yapısı](#2-kristal-güçlendirme-yapısı-crystal-enhancement-structure)
3. [Kristal Zırh Yapısı](#3-kristal-zırh-yapısı-crystal-armor-structure)
4. [Kristal Kalkan Yapısı](#4-kristal-kalkan-yapısı-crystal-shield-structure)
5. [Klan Gücüne Göre Can Artışı](#5-klan-gücüne-göre-can-artışı)
6. [Can Yenileme Yapısı](#6-can-yenileme-yapısı-crystal-regeneration-structure)
7. [Özel Itemler ve Tarifler](#7-özel-itemler-ve-tarifler)
8. [Boss Drop Sistemi](#8-boss-drop-sistemi)
9. [Tam Kod Implementasyonu](#9-tam-kod-implementasyonu)

---

## 1. TEMEL CAN SİSTEMİ

### 1.1. Clan Modeline Can Sistemi Ekleme

**Dosya:** `src/main/java/me/mami/stratocraft/model/Clan.java`

**Eklenmesi Gereken Alanlar:**

```java
// Klan kristali can sistemi
private double crystalMaxHealth = 100.0; // Maksimum can (kalıcı artışlar buraya eklenir)
private double crystalCurrentHealth = 100.0; // Mevcut can (hasar aldığında azalır)
private long lastCrystalRegenTime = 0; // Son can yenileme zamanı

// Savunma sistemleri
private double crystalDamageReduction = 0.0; // Hasar azaltma çarpanı (0.0 = %0, 0.5 = %50)
private int crystalShieldBlocks = 0; // Kalan kalkan blok sayısı (saldırıları engeller)
private int crystalMaxShieldBlocks = 0; // Maksimum kalkan blok sayısı
```

**Getter/Setter Metodları:**

```java
// Can sistemi getters/setters
public double getCrystalMaxHealth() { return crystalMaxHealth; }
public void setCrystalMaxHealth(double health) { 
    this.crystalMaxHealth = Math.max(100.0, health); // Minimum 100 HP
}

public double getCrystalCurrentHealth() { return crystalCurrentHealth; }
public void setCrystalCurrentHealth(double health) { 
    this.crystalCurrentHealth = Math.max(0.0, Math.min(health, crystalMaxHealth));
}

// Can artırma (kalıcı - maksimum canı artırır)
public void increaseCrystalMaxHealth(double amount) {
    this.crystalMaxHealth += amount;
    // Mevcut canı da artır (yeni maksimum canın %80'i kadar)
    this.crystalCurrentHealth = Math.min(crystalCurrentHealth + (amount * 0.8), crystalMaxHealth);
}

// Can yenileme (geçici - sadece mevcut canı artırır)
public void regenerateCrystalHealth(double amount) {
    this.crystalCurrentHealth = Math.min(crystalCurrentHealth + amount, crystalMaxHealth);
}

// Hasar alma (zırh ve kalkan kontrolü ile)
public void damageCrystal(double damage) {
    // Kalkan kontrolü: Eğer kalkan varsa, önce kalkana hasar ver
    if (crystalShieldBlocks > 0) {
        crystalShieldBlocks--;
        // Kalkan hasarı tamamen engelledi
        return;
    }
    
    // Zırh kontrolü: Hasarı azalt
    double finalDamage = damage * (1.0 - crystalDamageReduction);
    
    this.crystalCurrentHealth = Math.max(0.0, crystalCurrentHealth - finalDamage);
    if (crystalCurrentHealth <= 0) {
        destroyCrystal();
    }
}

// Zırh sistemi
public double getCrystalDamageReduction() { return crystalDamageReduction; }
public void setCrystalDamageReduction(double reduction) { 
    this.crystalDamageReduction = Math.max(0.0, Math.min(1.0, reduction)); // 0.0 - 1.0 arası
}

// Kalkan sistemi
public int getCrystalShieldBlocks() { return crystalShieldBlocks; }
public void setCrystalShieldBlocks(int blocks) { 
    this.crystalShieldBlocks = Math.max(0, blocks);
    this.crystalMaxShieldBlocks = Math.max(crystalMaxShieldBlocks, blocks);
}
public int getCrystalMaxShieldBlocks() { return crystalMaxShieldBlocks; }
public void addCrystalShieldBlocks(int blocks) {
    this.crystalShieldBlocks = Math.min(crystalMaxShieldBlocks, crystalShieldBlocks + blocks);
}
```

---

## 2. KRISTAL GÜÇLENDİRME YAPISI (Crystal Enhancement Structure)

### 2.1. Yapı Tanımı

**Yapı Adı:** Kristal Güçlendirme Yapısı (Crystal Enhancement Structure)  
**Yapı Tipi:** `CRYSTAL_ENHANCEMENT_STRUCTURE`  
**Amaç:** Özel itemler atılarak kristal canını kalıcı olarak artırır

### 2.2. Yapı Özellikleri

- **Seviye 1:** Her item +25 HP artırır, maksimum 10 item/dakika
- **Seviye 2:** Her item +50 HP artırır, maksimum 20 item/dakika
- **Seviye 3:** Her item +100 HP artırır, maksimum 30 item/dakika

### 2.3. Nasıl Üretilir?

**Ritüel Gereksinimleri:**
- **Seviye 1:** 
  - 3x3 Cobblestone zemin
  - Merkez: Ender Crystal
  - Üstünde: Anvil (Örs)
  - Etrafında: 4x Iron Block (köşelerde)
  
- **Seviye 2:**
  - Seviye 1 yapısı üzerine
  - 4x Diamond Block (köşelerde)
  - Merkez: Enchanted Golden Apple
  
- **Seviye 3:**
  - Seviye 2 yapısı üzerine
  - 4x Netherite Block (köşelerde)
  - Merkez: Nether Star

**Ritüel Yapımı:**
1. Oyuncu 3x3 Cobblestone zemin hazırlar
2. Merkeze Ender Crystal koyar
3. Üstüne Anvil koyar
4. Köşelere Iron Block koyar
5. Shift + Sağ Tık yapar (ritüel başlatır)
6. Yapı oluşur

### 2.4. Nasıl Çalışır?

**Kullanım Süreci:**
1. Oyuncu yapıya yaklaşır (5 blok içinde)
2. Elinde "Kristal Güçlendirme Taşı" (veya diğer özel itemler) tutar
3. Yapıya sağ tıklar
4. Item yapının envanterine girer
5. Yapı itemi işler (1-3 saniye)
6. Kristal canı artar
7. Item tüketilir

**Mantık:**
- Yapı her item için bir işleme süresi bekler
- Seviyeye göre maksimum işleme hızı vardır
- Her item farklı HP artışı sağlar
- Yapı envanteri: 27 slot (3x9)

### 2.5. Kod Implementasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/handler/structure/CrystalEnhancementHandler.java`

```java
package me.mami.stratocraft.handler.structure;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kristal Güçlendirme Yapısı Handler
 * Özel itemler atılarak kristal canını artırır
 */
public class CrystalEnhancementHandler implements Listener {
    private final Main plugin;
    private final Map<UUID, Long> lastProcessTime = new HashMap<>(); // Yapı UUID -> Son işleme zamanı
    private final Map<UUID, Integer> itemsProcessedThisMinute = new HashMap<>(); // Yapı UUID -> Bu dakikada işlenen item sayısı
    
    // Item -> HP artışı mapping
    private final Map<String, Double> itemHealthBoost = new HashMap<>();
    
    public CrystalEnhancementHandler(Main plugin) {
        this.plugin = plugin;
        initializeItemHealthBoosts();
    }
    
    private void initializeItemHealthBoosts() {
        // Özel itemler ve HP artışları
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE", 25.0); // Temel taş
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE_ADVANCED", 50.0); // Gelişmiş taş
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE_ELITE", 100.0); // Elite taş
        itemHealthBoost.put("CRYSTAL_ENHANCEMENT_STONE_LEGENDARY", 200.0); // Efsanevi taş
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        // Yapı kontrolü
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_ENHANCEMENT_STRUCTURE) {
            return;
        }
        
        // Klan kontrolü
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(blockLoc);
        if (clan == null) {
            player.sendMessage("§cBu yapı bir klana ait değil!");
            return;
        }
        
        // Oyuncu klan üyesi mi?
        if (clan.getRank(player.getUniqueId()) == null) {
            player.sendMessage("§cBu yapıyı kullanmak için klan üyesi olmalısınız!");
            return;
        }
        
        // Mesafe kontrolü (5 blok içinde)
        if (player.getLocation().distance(blockLoc) > 5) {
            player.sendMessage("§cYapıya çok uzaksınız! (5 blok içinde olmalısınız)");
            return;
        }
        
        // Item kontrolü
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§cElinde özel bir item tutmalısınız!");
            return;
        }
        
        // Item tipi kontrolü
        String itemId = getItemId(item);
        if (itemId == null || !itemHealthBoost.containsKey(itemId)) {
            player.sendMessage("§cBu item kristal güçlendirme için kullanılamaz!");
            return;
        }
        
        // İşleme hızı kontrolü
        if (!canProcessItem(structure, player)) {
            player.sendMessage("§cYapı şu anda çok fazla item işliyor! Lütfen bekleyin.");
            return;
        }
        
        // Item işleme
        processItem(structure, clan, item, itemId, player);
        
        event.setCancelled(true);
    }
    
    private Structure findStructureAt(Location loc) {
        if (plugin.getTerritoryManager() == null) return null;
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(loc);
        if (clan == null) return null;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(loc) < 2.0) {
                return structure;
            }
        }
        
        return null;
    }
    
    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_id");
        org.bukkit.persistence.PersistentDataContainer container = 
            item.getItemMeta().getPersistentDataContainer();
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        
        return null;
    }
    
    private boolean canProcessItem(Structure structure, Player player) {
        UUID structureId = structure.getLocation().hashCode(); // Basit UUID (gerçekte structure.getId() kullanılmalı)
        long now = System.currentTimeMillis();
        
        // Son işleme zamanı kontrolü (minimum 1 saniye aralık)
        Long lastTime = lastProcessTime.get(structureId);
        if (lastTime != null && (now - lastTime) < 1000) {
            return false;
        }
        
        // Dakika başına maksimum item kontrolü
        int maxItemsPerMinute = getMaxItemsPerMinute(structure.getLevel());
        Integer processed = itemsProcessedThisMinute.get(structureId);
        
        if (processed != null && processed >= maxItemsPerMinute) {
            // 1 dakika geçti mi kontrol et
            if (lastTime != null && (now - lastTime) < 60000) {
                return false;
            } else {
                // 1 dakika geçti, sıfırla
                itemsProcessedThisMinute.put(structureId, 0);
            }
        }
        
        return true;
    }
    
    private int getMaxItemsPerMinute(int level) {
        switch (level) {
            case 1: return 10;
            case 2: return 20;
            case 3: return 30;
            default: return 10;
        }
    }
    
    private void processItem(Structure structure, Clan clan, ItemStack item, String itemId, Player player) {
        UUID structureId = structure.getLocation().hashCode();
        long now = System.currentTimeMillis();
        
        // HP artışı hesapla
        double baseHealthBoost = itemHealthBoost.get(itemId);
        double levelMultiplier = getLevelMultiplier(structure.getLevel());
        double finalHealthBoost = baseHealthBoost * levelMultiplier;
        
        // Kristal canını artır
        clan.increaseCrystalMaxHealth(finalHealthBoost);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // İşleme kayıtları
        lastProcessTime.put(structureId, now);
        itemsProcessedThisMinute.put(structureId, 
            itemsProcessedThisMinute.getOrDefault(structureId, 0) + 1);
        
        // Efektler
        Location structureLoc = structure.getLocation();
        structureLoc.getWorld().spawnParticle(
            org.bukkit.Particle.TOTEM, 
            structureLoc.add(0.5, 1, 0.5), 
            30, 
            0.5, 0.5, 0.5, 0.1
        );
        
        // Mesajlar
        player.sendMessage("§aKristal güçlendirildi! (+" + 
            String.format("%.1f", finalHealthBoost) + " HP)");
        
        // Klan üyelerine bildir
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage("§7" + player.getName() + " kristali güçlendirdi! (+" + 
                    String.format("%.1f", finalHealthBoost) + " HP)");
            }
        }
        
        // İşleme animasyonu (1-3 saniye)
        new BukkitRunnable() {
            @Override
            public void run() {
                // İşleme tamamlandı efekti
                structureLoc.getWorld().spawnParticle(
                    org.bukkit.Particle.ENCHANTMENT_TABLE,
                    structureLoc.add(0.5, 1, 0.5),
                    20,
                    0.3, 0.3, 0.3, 0.1
                );
            }
        }.runTaskLater(plugin, 20L); // 1 saniye sonra
    }
    
    private double getLevelMultiplier(int level) {
        switch (level) {
            case 1: return 1.0;
            case 2: return 2.0;
            case 3: return 4.0;
            default: return 1.0;
        }
    }
}
```

---

## 3. KRISTAL ZIRH YAPISI (Crystal Armor Structure)

### 3.1. Yapı Tanımı

**Yapı Adı:** Kristal Zırh Yapısı (Crystal Armor Structure)  
**Yapı Tipi:** `CRYSTAL_ARMOR_STRUCTURE`  
**Amaç:** Kristale gelen hasarı azaltır (hasar çarpanını düşürür)

### 3.2. Yapı Özellikleri

- **Seviye 1:** %10 hasar azaltma, maksimum %10
- **Seviye 2:** %25 hasar azaltma, maksimum %25
- **Seviye 3:** %50 hasar azaltma, maksimum %50

**Çalışma Mantığı:**
- Yapı aktif olduğu sürece kristal hasar azaltma çarpanına sahip olur
- Birden fazla zırh yapısı varsa, en yüksek seviyeli olanın değeri kullanılır
- Yapı yakıt tüketir (her hasar alımında)

### 3.3. Nasıl Üretilir?

**Ritüel Gereksinimleri:**
- **Seviye 1:**
  - 3x3 Iron Block zemin
  - Merkez: Shield (Kalkan)
  - Üstünde: Anvil
  - Etrafında: 4x Iron Block (köşelerde)
  
- **Seviye 2:**
  - Seviye 1 yapısı üzerine
  - 4x Diamond Block (köşelerde)
  - Merkez: Enchanted Golden Apple
  
- **Seviye 3:**
  - Seviye 2 yapısı üzerine
  - 4x Netherite Block (köşelerde)
  - Merkez: Nether Star

### 3.4. Nasıl Çalışır?

**Yakıt Sistemi:**
- Yapı çalışmak için yakıt gerektirir
- Yakıt: "Zırh Yakıtı" (Armor Fuel) itemi
- Her hasar alımında yakıt tüketilir
- Yakıt bittiğinde zırh pasif olur

**Yakıt Ekleme:**
1. Oyuncu yapıya yaklaşır
2. Elinde "Zırh Yakıtı" tutar
3. Yapıya sağ tıklar
4. Yakıt yapının envanterine girer

### 3.5. Kod Implementasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/handler/structure/CrystalArmorHandler.java`

```java
package me.mami.stratocraft.handler.structure;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kristal Zırh Yapısı Handler
 * Kristale gelen hasarı azaltır
 */
public class CrystalArmorHandler implements Listener {
    private final Main plugin;
    private final Map<UUID, Integer> structureFuel = new HashMap<>(); // Yapı UUID -> Yakıt miktarı
    
    // Seviye -> Hasar azaltma çarpanı
    private final Map<Integer, Double> levelDamageReduction = new HashMap<>();
    
    public CrystalArmorHandler(Main plugin) {
        this.plugin = plugin;
        initializeDamageReduction();
        
        // Her saniye zırh kontrolü yap
        startArmorUpdateTask();
    }
    
    private void initializeDamageReduction() {
        levelDamageReduction.put(1, 0.10); // %10
        levelDamageReduction.put(2, 0.25); // %25
        levelDamageReduction.put(3, 0.50); // %50
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_ARMOR_STRUCTURE) {
            return;
        }
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(blockLoc);
        if (clan == null) return;
        
        if (clan.getRank(player.getUniqueId()) == null) {
            player.sendMessage("§cBu yapıyı kullanmak için klan üyesi olmalısınız!");
            return;
        }
        
        // Yakıt ekleme
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && isArmorFuel(item)) {
            addFuel(structure, clan, item, player);
            event.setCancelled(true);
        }
    }
    
    private Structure findStructureAt(Location loc) {
        if (plugin.getTerritoryManager() == null) return null;
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(loc);
        if (clan == null) return null;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(loc) < 2.0) {
                return structure;
            }
        }
        
        return null;
    }
    
    private boolean isArmorFuel(ItemStack item) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals("ARMOR_FUEL");
    }
    
    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_id");
        org.bukkit.persistence.PersistentDataContainer container = 
            item.getItemMeta().getPersistentDataContainer();
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        
        return null;
    }
    
    private void addFuel(Structure structure, Clan clan, ItemStack item, Player player) {
        UUID structureId = structure.getLocation().hashCode();
        int fuelPerItem = 100; // Her item 100 yakıt
        
        int currentFuel = structureFuel.getOrDefault(structureId, 0);
        int maxFuel = getMaxFuel(structure.getLevel());
        
        if (currentFuel >= maxFuel) {
            player.sendMessage("§cYapının yakıt deposu dolu! (Max: " + maxFuel + ")");
            return;
        }
        
        // Yakıt ekle
        int newFuel = Math.min(maxFuel, currentFuel + fuelPerItem);
        structureFuel.put(structureId, newFuel);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§aYakıt eklendi! (" + newFuel + "/" + maxFuel + ")");
    }
    
    private int getMaxFuel(int level) {
        switch (level) {
            case 1: return 500;
            case 2: return 1000;
            case 3: return 2000;
            default: return 500;
        }
    }
    
    /**
     * Zırh güncelleme task'ı - Her saniye çalışır
     */
    private void startArmorUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllArmorStructures();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Her saniye
    }
    
    private void updateAllArmorStructures() {
        if (plugin.getTerritoryManager() == null) return;
        
        for (Clan clan : plugin.getTerritoryManager().getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            // En yüksek seviyeli zırh yapısını bul
            Structure bestArmorStructure = null;
            int maxLevel = 0;
            
            for (Structure structure : clan.getStructures()) {
                if (structure.getType() == Structure.Type.CRYSTAL_ARMOR_STRUCTURE) {
                    if (structure.getLevel() > maxLevel) {
                        UUID structureId = structure.getLocation().hashCode();
                        int fuel = structureFuel.getOrDefault(structureId, 0);
                        
                        // Yakıt varsa aktif
                        if (fuel > 0) {
                            bestArmorStructure = structure;
                            maxLevel = structure.getLevel();
                        }
                    }
                }
            }
            
            // Zırh değerini ayarla
            if (bestArmorStructure != null) {
                double damageReduction = levelDamageReduction.get(maxLevel);
                clan.setCrystalDamageReduction(damageReduction);
            } else {
                // Zırh yapısı yok veya yakıt yok
                clan.setCrystalDamageReduction(0.0);
            }
        }
    }
    
    /**
     * Hasar alındığında yakıt tüket (ChaosDragonHandler'dan çağrılacak)
     */
    public void consumeFuelOnDamage(Clan clan, double damage) {
        // En yüksek seviyeli zırh yapısını bul
        Structure bestArmorStructure = null;
        int maxLevel = 0;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getType() == Structure.Type.CRYSTAL_ARMOR_STRUCTURE) {
                if (structure.getLevel() > maxLevel) {
                    UUID structureId = structure.getLocation().hashCode();
                    int fuel = structureFuel.getOrDefault(structureId, 0);
                    
                    if (fuel > 0) {
                        bestArmorStructure = structure;
                        maxLevel = structure.getLevel();
                    }
                }
            }
        }
        
        if (bestArmorStructure != null) {
            UUID structureId = bestArmorStructure.getLocation().hashCode();
            int currentFuel = structureFuel.getOrDefault(structureId, 0);
            
            // Hasar başına yakıt tüket (hasar miktarına göre)
            int fuelConsumption = (int) Math.ceil(damage / 10.0); // Her 10 hasar için 1 yakıt
            int newFuel = Math.max(0, currentFuel - fuelConsumption);
            
            structureFuel.put(structureId, newFuel);
            
            // Yakıt bitti mi?
            if (newFuel == 0) {
                // Klan üyelerine bildir
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = org.bukkit.Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§c⚠ Kristal Zırh Yapısı yakıtı bitti! Zırh pasif oldu.");
                    }
                }
            }
        }
    }
}
```

---

## 4. KRISTAL KALKAN YAPISI (Crystal Shield Structure)

### 4.1. Yapı Tanımı

**Yapı Adı:** Kristal Kalkan Yapısı (Crystal Shield Structure)  
**Yapı Tipi:** `CRYSTAL_SHIELD_STRUCTURE`  
**Amaç:** Belirli sayıda saldırıyı tamamen engeller (hiç hasar almaz)

### 4.2. Yapı Özellikleri

- **Seviye 1:** 5 kalkan bloğu, maksimum 5
- **Seviye 2:** 15 kalkan bloğu, maksimum 15
- **Seviye 3:** 30 kalkan bloğu, maksimum 30

**Çalışma Mantığı:**
- Her saldırı 1 kalkan bloğu tüketir
- Kalkan bloğu varsa saldırı tamamen engellenir (0 hasar)
- Kalkan bloğu yoksa normal hasar alınır
- Kalkan bloğu "Kalkan Yakıtı" ile doldurulur

### 4.3. Nasıl Üretilir?

**Ritüel Gereksinimleri:**
- **Seviye 1:**
  - 3x3 Obsidian zemin
  - Merkez: Shield (Kalkan)
  - Üstünde: Beacon
  - Etrafında: 4x Iron Block (köşelerde)
  
- **Seviye 2:**
  - Seviye 1 yapısı üzerine
  - 4x Diamond Block (köşelerde)
  - Merkez: Enchanted Golden Apple
  
- **Seviye 3:**
  - Seviye 2 yapısı üzerine
  - 4x Netherite Block (köşelerde)
  - Merkez: Nether Star

### 4.4. Nasıl Çalışır?

**Yakıt Sistemi:**
- Yapı "Kalkan Yakıtı" (Shield Fuel) itemi ile doldurulur
- Her yakıt itemi belirli sayıda kalkan bloğu ekler
- Her saldırı 1 kalkan bloğu tüketir
- Kalkan bloğu bittiğinde normal hasar alınır

**Yakıt Ekleme:**
1. Oyuncu yapıya yaklaşır
2. Elinde "Kalkan Yakıtı" tutar
3. Yapıya sağ tıklar
4. Kalkan bloğu eklenir

### 4.5. Kod Implementasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/handler/structure/CrystalShieldHandler.java`

```java
package me.mami.stratocraft.handler.structure;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kristal Kalkan Yapısı Handler
 * Belirli sayıda saldırıyı tamamen engeller
 */
public class CrystalShieldHandler implements Listener {
    private final Main plugin;
    
    // Seviye -> Maksimum kalkan bloğu
    private final Map<Integer, Integer> levelMaxShieldBlocks = new HashMap<>();
    
    // Seviye -> Yakıt başına kalkan bloğu
    private final Map<Integer, Integer> levelShieldBlocksPerFuel = new HashMap<>();
    
    public CrystalShieldHandler(Main plugin) {
        this.plugin = plugin;
        initializeShieldValues();
        
        // Her saniye kalkan güncelleme
        startShieldUpdateTask();
    }
    
    private void initializeShieldValues() {
        // Seviye -> Maksimum kalkan bloğu
        levelMaxShieldBlocks.put(1, 5);
        levelMaxShieldBlocks.put(2, 15);
        levelMaxShieldBlocks.put(3, 30);
        
        // Seviye -> Yakıt başına kalkan bloğu
        levelShieldBlocksPerFuel.put(1, 1);
        levelShieldBlocksPerFuel.put(2, 3);
        levelShieldBlocksPerFuel.put(3, 5);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_SHIELD_STRUCTURE) {
            return;
        }
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(blockLoc);
        if (clan == null) return;
        
        if (clan.getRank(player.getUniqueId()) == null) {
            player.sendMessage("§cBu yapıyı kullanmak için klan üyesi olmalısınız!");
            return;
        }
        
        // Yakıt ekleme
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && isShieldFuel(item)) {
            addShieldBlocks(structure, clan, item, player);
            event.setCancelled(true);
        }
    }
    
    private Structure findStructureAt(Location loc) {
        if (plugin.getTerritoryManager() == null) return null;
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(loc);
        if (clan == null) return null;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(loc) < 2.0) {
                return structure;
            }
        }
        
        return null;
    }
    
    private boolean isShieldFuel(ItemStack item) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals("SHIELD_FUEL");
    }
    
    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_id");
        org.bukkit.persistence.PersistentDataContainer container = 
            item.getItemMeta().getPersistentDataContainer();
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        
        return null;
    }
    
    private void addShieldBlocks(Structure structure, Clan clan, ItemStack item, Player player) {
        int currentBlocks = clan.getCrystalShieldBlocks();
        int maxBlocks = levelMaxShieldBlocks.get(structure.getLevel());
        int blocksPerFuel = levelShieldBlocksPerFuel.get(structure.getLevel());
        
        if (currentBlocks >= maxBlocks) {
            player.sendMessage("§cKalkan deposu dolu! (Max: " + maxBlocks + ")");
            return;
        }
        
        // Kalkan bloğu ekle
        int newBlocks = Math.min(maxBlocks, currentBlocks + blocksPerFuel);
        clan.setCrystalShieldBlocks(newBlocks);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§aKalkan bloğu eklendi! (" + newBlocks + "/" + maxBlocks + ")");
        
        // Efekt
        Location structureLoc = structure.getLocation();
        structureLoc.getWorld().spawnParticle(
            org.bukkit.Particle.BARRIER,
            structureLoc.add(0.5, 1, 0.5),
            20,
            0.3, 0.3, 0.3, 0.1
        );
    }
    
    /**
     * Kalkan güncelleme task'ı
     */
    private void startShieldUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Kalkan değerleri zaten Clan modelinde tutuluyor
                // Burada sadece görsel güncellemeler yapılabilir
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Hasar alındığında kalkan bloğu tüket (ChaosDragonHandler'dan çağrılacak)
     */
    public boolean consumeShieldBlockOnDamage(Clan clan) {
        int currentBlocks = clan.getCrystalShieldBlocks();
        
        if (currentBlocks > 0) {
            // Kalkan bloğu var, saldırıyı engelle
            clan.setCrystalShieldBlocks(currentBlocks - 1);
            
            // Klan üyelerine bildir (kritik seviyede)
            if (currentBlocks <= 3) {
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = org.bukkit.Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage("§c⚠ Kalkan bloğu azaldı! (" + (currentBlocks - 1) + " kaldı)");
                    }
                }
            }
            
            return true; // Saldırı engellendi
        }
        
        return false; // Kalkan bloğu yok, normal hasar alınacak
    }
}
```

---

## 5. KLAN GÜCÜNE GÖRE CAN ARTIŞI

### 5.1. Mantık

Klanın toplam gücü (yapıların seviyeleri toplamı) arttıkça kristal canı da artar.

**Formül:**
```
Toplam Güç = Tüm yapıların seviyelerinin toplamı
Can Artışı = Toplam Güç * 2.5 HP
```

**Örnek:**
- 10 yapı, her biri seviye 1 = 10 toplam güç = +25 HP
- 20 yapı, her biri seviye 2 = 40 toplam güç = +100 HP
- 30 yapı, her biri seviye 3 = 90 toplam güç = +225 HP

### 5.2. Kod Implementasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/task/CrystalPowerUpdateTask.java`

```java
package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Klan gücüne göre kristal canını günceller
 * Her 5 dakikada bir çalışır
 */
public class CrystalPowerUpdateTask extends BukkitRunnable {
    private final Main plugin;
    private final double HP_PER_POWER = 2.5; // Her güç birimi için 2.5 HP
    
    public CrystalPowerUpdateTask(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() {
        if (plugin.getTerritoryManager() == null) return;
        
        for (Clan clan : plugin.getTerritoryManager().getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            // Toplam güç hesapla
            int totalPower = 0;
            for (Structure structure : clan.getStructures()) {
                totalPower += structure.getLevel();
            }
            
            // Can artışı hesapla
            double healthFromPower = totalPower * HP_PER_POWER;
            
            // Mevcut maksimum canı kontrol et
            double currentMaxHealth = clan.getCrystalMaxHealth();
            double baseHealth = 100.0; // Temel can
            
            // Güçten gelen can artışını hesapla
            double powerHealthIncrease = healthFromPower - (currentMaxHealth - baseHealth);
            
            // Eğer güç artışı varsa, canı artır
            if (powerHealthIncrease > 0) {
                clan.increaseCrystalMaxHealth(powerHealthIncrease);
                
                // Klan üyelerine bildir (sadece önemli artışlarda)
                if (powerHealthIncrease >= 50) {
                    for (UUID memberId : clan.getMembers().keySet()) {
                        Player member = org.bukkit.Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.sendMessage("§aKlan gücü arttı! Kristal canı +" + 
                                String.format("%.1f", powerHealthIncrease) + " HP");
                        }
                    }
                }
            }
        }
    }
}
```

**Main.java'da başlatma:**

```java
// Main.java içinde
private CrystalPowerUpdateTask crystalPowerTask;

@Override
public void onEnable() {
    // ... diğer kodlar ...
    
    // Klan gücü güncelleme task'ı (her 5 dakika)
    crystalPowerTask = new CrystalPowerUpdateTask(this);
    crystalPowerTask.runTaskTimer(this, 0L, 6000L); // 6000 tick = 5 dakika
}
```

---

## 6. CAN YENİLEME YAPISI (Crystal Regeneration Structure)

### 6.1. Yapı Tanımı

**Yapı Adı:** Can Yenileme Yapısı (Crystal Regeneration Structure)  
**Yapı Tipi:** `CRYSTAL_REGENERATION_STRUCTURE`  
**Amaç:** Özel yakıt ile kristal canını yeniler

### 6.2. Yapı Özellikleri

- **Seviye 1:** 1 HP/dakika yenileme, maksimum 500 yakıt
- **Seviye 2:** 2 HP/dakika yenileme, maksimum 1000 yakıt
- **Seviye 3:** 5 HP/dakika yenileme, maksimum 2000 yakıt

**Çalışma Mantığı:**
- Yapı aktif olduğu sürece (yakıt varsa) kristal canını yeniler
- Her dakika belirli miktar HP yeniler
- Yakıt bittiğinde durur

### 6.3. Nasıl Üretilir?

**Ritüel Gereksinimleri:**
- **Seviye 1:**
  - 3x3 Gold Block zemin
  - Merkez: Golden Apple
  - Üstünde: Beacon
  - Etrafında: 4x Gold Block (köşelerde)
  
- **Seviye 2:**
  - Seviye 1 yapısı üzerine
  - 4x Diamond Block (köşelerde)
  - Merkez: Enchanted Golden Apple
  
- **Seviye 3:**
  - Seviye 2 yapısı üzerine
  - 4x Netherite Block (köşelerde)
  - Merkez: Nether Star

### 6.4. Nasıl Çalışır?

**Yakıt Sistemi:**
- Yapı "Can Yenileme Yakıtı" (Regeneration Fuel) itemi ile doldurulur
- Her yakıt itemi belirli miktar yakıt ekler
- Her dakika yakıt tüketilir ve can yenilenir

### 6.5. Kod Implementasyonu

**Dosya:** `src/main/java/me/mami/stratocraft/handler/structure/CrystalRegenerationHandler.java`

```java
package me.mami.stratocraft.handler.structure;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Can Yenileme Yapısı Handler
 * Özel yakıt ile kristal canını yeniler
 */
public class CrystalRegenerationHandler implements Listener {
    private final Main plugin;
    private final Map<UUID, Integer> structureFuel = new HashMap<>(); // Yapı UUID -> Yakıt miktarı
    
    // Seviye -> HP/dakika yenileme hızı
    private final Map<Integer, Double> levelRegenRate = new HashMap<>();
    
    // Seviye -> Maksimum yakıt
    private final Map<Integer, Integer> levelMaxFuel = new HashMap<>();
    
    public CrystalRegenerationHandler(Main plugin) {
        this.plugin = plugin;
        initializeRegenValues();
        
        // Her dakika can yenileme
        startRegenerationTask();
    }
    
    private void initializeRegenValues() {
        // Seviye -> HP/dakika
        levelRegenRate.put(1, 1.0);
        levelRegenRate.put(2, 2.0);
        levelRegenRate.put(3, 5.0);
        
        // Seviye -> Maksimum yakıt
        levelMaxFuel.put(1, 500);
        levelMaxFuel.put(2, 1000);
        levelMaxFuel.put(3, 2000);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Player player = event.getPlayer();
        Location blockLoc = clickedBlock.getLocation();
        
        Structure structure = findStructureAt(blockLoc);
        if (structure == null || structure.getType() != Structure.Type.CRYSTAL_REGENERATION_STRUCTURE) {
            return;
        }
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(blockLoc);
        if (clan == null) return;
        
        if (clan.getRank(player.getUniqueId()) == null) {
            player.sendMessage("§cBu yapıyı kullanmak için klan üyesi olmalısınız!");
            return;
        }
        
        // Yakıt ekleme
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && isRegenerationFuel(item)) {
            addFuel(structure, clan, item, player);
            event.setCancelled(true);
        }
    }
    
    private Structure findStructureAt(Location loc) {
        if (plugin.getTerritoryManager() == null) return null;
        
        Clan clan = plugin.getTerritoryManager().getTerritoryOwner(loc);
        if (clan == null) return null;
        
        for (Structure structure : clan.getStructures()) {
            if (structure.getLocation().distance(loc) < 2.0) {
                return structure;
            }
        }
        
        return null;
    }
    
    private boolean isRegenerationFuel(ItemStack item) {
        String itemId = getItemId(item);
        return itemId != null && itemId.equals("REGENERATION_FUEL");
    }
    
    private String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "custom_id");
        org.bukkit.persistence.PersistentDataContainer container = 
            item.getItemMeta().getPersistentDataContainer();
        
        if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
            return container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        }
        
        return null;
    }
    
    private void addFuel(Structure structure, Clan clan, ItemStack item, Player player) {
        UUID structureId = structure.getLocation().hashCode();
        int fuelPerItem = 100; // Her item 100 yakıt
        
        int currentFuel = structureFuel.getOrDefault(structureId, 0);
        int maxFuel = levelMaxFuel.get(structure.getLevel());
        
        if (currentFuel >= maxFuel) {
            player.sendMessage("§cYapının yakıt deposu dolu! (Max: " + maxFuel + ")");
            return;
        }
        
        // Yakıt ekle
        int newFuel = Math.min(maxFuel, currentFuel + fuelPerItem);
        structureFuel.put(structureId, newFuel);
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§aYakıt eklendi! (" + newFuel + "/" + maxFuel + ")");
    }
    
    /**
     * Can yenileme task'ı - Her dakika çalışır
     */
    private void startRegenerationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                regenerateAllCrystals();
            }
        }.runTaskTimer(plugin, 0L, 1200L); // 1200 tick = 1 dakika
    }
    
    private void regenerateAllCrystals() {
        if (plugin.getTerritoryManager() == null) return;
        
        for (Clan clan : plugin.getTerritoryManager().getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            // Can yenileme yapısını bul
            Structure regenStructure = null;
            int maxLevel = 0;
            
            for (Structure structure : clan.getStructures()) {
                if (structure.getType() == Structure.Type.CRYSTAL_REGENERATION_STRUCTURE) {
                    if (structure.getLevel() > maxLevel) {
                        UUID structureId = structure.getLocation().hashCode();
                        int fuel = structureFuel.getOrDefault(structureId, 0);
                        
                        // Yakıt varsa aktif
                        if (fuel > 0) {
                            regenStructure = structure;
                            maxLevel = structure.getLevel();
                        }
                    }
                }
            }
            
            // Can yenile
            if (regenStructure != null) {
                double regenRate = levelRegenRate.get(maxLevel);
                UUID structureId = regenStructure.getLocation().hashCode();
                
                // Can yenile
                clan.regenerateCrystalHealth(regenRate);
                
                // Yakıt tüket (1 dakika için 1 yakıt)
                int currentFuel = structureFuel.getOrDefault(structureId, 0);
                if (currentFuel > 0) {
                    structureFuel.put(structureId, currentFuel - 1);
                }
                
                // Efekt (can yenilendiğinde)
                Location crystalLoc = clan.getCrystalLocation();
                if (crystalLoc != null) {
                    crystalLoc.getWorld().spawnParticle(
                        org.bukkit.Particle.HEART,
                        crystalLoc,
                        5,
                        0.3, 0.3, 0.3, 0.1
                    );
                }
            }
        }
    }
}
```

---

## 7. ÖZEL İTEMLER VE TARİFLER

### 7.1. Item Tanımları

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ItemManager.java`

```java
// ItemManager.java içine eklenecek

// ========== KRISTAL GÜÇLENDİRME İTEMLERİ ==========
public static ItemStack CRYSTAL_ENHANCEMENT_STONE; // Temel taş
public static ItemStack CRYSTAL_ENHANCEMENT_STONE_ADVANCED; // Gelişmiş taş
public static ItemStack CRYSTAL_ENHANCEMENT_STONE_ELITE; // Elite taş
public static ItemStack CRYSTAL_ENHANCEMENT_STONE_LEGENDARY; // Efsanevi taş

// ========== YAKIT İTEMLERİ ==========
public static ItemStack ARMOR_FUEL; // Zırh yakıtı
public static ItemStack SHIELD_FUEL; // Kalkan yakıtı
public static ItemStack REGENERATION_FUEL; // Can yenileme yakıtı

// init() metodunda oluşturulacak:
private void initCrystalItems() {
    // Kristal Güçlendirme Taşı (Temel)
    CRYSTAL_ENHANCEMENT_STONE = create(
        Material.EMERALD,
        "CRYSTAL_ENHANCEMENT_STONE",
        "§6Kristal Güçlendirme Taşı",
        java.util.Arrays.asList(
            "§7Kristal Güçlendirme Yapısına",
            "§7atılarak kristal canını",
            "§7kalıcı olarak artırır.",
            "",
            "§e+25 HP (Seviye 1 yapı)",
            "§e+50 HP (Seviye 2 yapı)",
            "§e+100 HP (Seviye 3 yapı)"
        )
    );
    
    // Kristal Güçlendirme Taşı (Gelişmiş)
    CRYSTAL_ENHANCEMENT_STONE_ADVANCED = create(
        Material.EMERALD_BLOCK,
        "CRYSTAL_ENHANCEMENT_STONE_ADVANCED",
        "§bGelişmiş Kristal Güçlendirme Taşı",
        java.util.Arrays.asList(
            "§7Kristal Güçlendirme Yapısına",
            "§7atılarak kristal canını",
            "§7kalıcı olarak artırır.",
            "",
            "§e+50 HP (Seviye 1 yapı)",
            "§e+100 HP (Seviye 2 yapı)",
            "§e+200 HP (Seviye 3 yapı)"
        )
    );
    
    // Kristal Güçlendirme Taşı (Elite)
    CRYSTAL_ENHANCEMENT_STONE_ELITE = create(
        Material.DIAMOND,
        "CRYSTAL_ENHANCEMENT_STONE_ELITE",
        "§5Elite Kristal Güçlendirme Taşı",
        java.util.Arrays.asList(
            "§7Kristal Güçlendirme Yapısına",
            "§7atılarak kristal canını",
            "§7kalıcı olarak artırır.",
            "",
            "§e+100 HP (Seviye 1 yapı)",
            "§e+200 HP (Seviye 2 yapı)",
            "§e+400 HP (Seviye 3 yapı)"
        )
    );
    
    // Kristal Güçlendirme Taşı (Efsanevi)
    CRYSTAL_ENHANCEMENT_STONE_LEGENDARY = create(
        Material.NETHER_STAR,
        "CRYSTAL_ENHANCEMENT_STONE_LEGENDARY",
        "§6§lEfsanevi Kristal Güçlendirme Taşı",
        java.util.Arrays.asList(
            "§7Kristal Güçlendirme Yapısına",
            "§7atılarak kristal canını",
            "§7kalıcı olarak artırır.",
            "",
            "§e+200 HP (Seviye 1 yapı)",
            "§e+400 HP (Seviye 2 yapı)",
            "§e+800 HP (Seviye 3 yapı)"
        )
    );
    
    // Zırh Yakıtı
    ARMOR_FUEL = create(
        Material.IRON_INGOT,
        "ARMOR_FUEL",
        "§7Zırh Yakıtı",
        java.util.Arrays.asList(
            "§7Kristal Zırh Yapısına",
            "§7atılarak zırh yakıtı",
            "§7ekler.",
            "",
            "§e+100 Yakıt"
        )
    );
    
    // Kalkan Yakıtı
    SHIELD_FUEL = create(
        Material.SHIELD,
        "SHIELD_FUEL",
        "§bKalkan Yakıtı",
        java.util.Arrays.asList(
            "§7Kristal Kalkan Yapısına",
            "§7atılarak kalkan bloğu",
            "§7ekler.",
            "",
            "§e+1 Kalkan Bloğu (Seviye 1)",
            "§e+3 Kalkan Bloğu (Seviye 2)",
            "§e+5 Kalkan Bloğu (Seviye 3)"
        )
    );
    
    // Can Yenileme Yakıtı
    REGENERATION_FUEL = create(
        Material.GOLDEN_APPLE,
        "REGENERATION_FUEL",
        "§aCan Yenileme Yakıtı",
        java.util.Arrays.asList(
            "§7Can Yenileme Yapısına",
            "§7atılarak can yenileme",
            "§7yakıtı ekler.",
            "",
            "§e+100 Yakıt"
        )
    );
}
```

### 7.2. Tarifler

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ItemManager.java` - `registerRecipes()` metoduna eklenecek:

```java
// Kristal Güçlendirme Taşı (Temel) - Crafting Table
ShapedRecipe crystalEnhancementStone = new ShapedRecipe(
    new NamespacedKey(Main.getInstance(), "crystal_enhancement_stone"),
    CRYSTAL_ENHANCEMENT_STONE
);
crystalEnhancementStone.shape(" E ", "EDE", " E ");
crystalEnhancementStone.setIngredient('E', Material.EMERALD);
crystalEnhancementStone.setIngredient('D', Material.DIAMOND);
Bukkit.addRecipe(crystalEnhancementStone);

// Kristal Güçlendirme Taşı (Gelişmiş) - Crafting Table
// Gereksinim: 4x Temel Taş + 1x Ender Pearl
ShapedRecipe crystalEnhancementStoneAdvanced = new ShapedRecipe(
    new NamespacedKey(Main.getInstance(), "crystal_enhancement_stone_advanced"),
    CRYSTAL_ENHANCEMENT_STONE_ADVANCED
);
crystalEnhancementStoneAdvanced.shape("SSS", "SES", "SSS");
crystalEnhancementStoneAdvanced.setIngredient('S', CRYSTAL_ENHANCEMENT_STONE);
crystalEnhancementStoneAdvanced.setIngredient('E', Material.ENDER_PEARL);
Bukkit.addRecipe(crystalEnhancementStoneAdvanced);

// Kristal Güçlendirme Taşı (Elite) - Crafting Table
// Gereksinim: 4x Gelişmiş Taş + 1x Nether Star
ShapedRecipe crystalEnhancementStoneElite = new ShapedRecipe(
    new NamespacedKey(Main.getInstance(), "crystal_enhancement_stone_elite"),
    CRYSTAL_ENHANCEMENT_STONE_ELITE
);
crystalEnhancementStoneElite.shape("SSS", "SNS", "SSS");
crystalEnhancementStoneElite.setIngredient('S', CRYSTAL_ENHANCEMENT_STONE_ADVANCED);
crystalEnhancementStoneElite.setIngredient('N', Material.NETHER_STAR);
Bukkit.addRecipe(crystalEnhancementStoneElite);

// Kristal Güçlendirme Taşı (Efsanevi) - Boss Drop
// Kaos Ejderi veya Void Dragon'dan düşer (nadir)

// Zırh Yakıtı - Crafting Table
ShapedRecipe armorFuel = new ShapedRecipe(
    new NamespacedKey(Main.getInstance(), "armor_fuel"),
    ARMOR_FUEL
);
armorFuel.shape("III", "ICI", "III");
armorFuel.setIngredient('I', Material.IRON_INGOT);
armorFuel.setIngredient('C', Material.COAL);
Bukkit.addRecipe(armorFuel);

// Kalkan Yakıtı - Crafting Table
ShapedRecipe shieldFuel = new ShapedRecipe(
    new NamespacedKey(Main.getInstance(), "shield_fuel"),
    SHIELD_FUEL
);
shieldFuel.shape(" I ", "ISI", " I ");
shieldFuel.setIngredient('I', Material.IRON_INGOT);
shieldFuel.setIngredient('S', Material.SHIELD);
Bukkit.addRecipe(shieldFuel);

// Can Yenileme Yakıtı - Crafting Table
ShapedRecipe regenerationFuel = new ShapedRecipe(
    new NamespacedKey(Main.getInstance(), "regeneration_fuel"),
    REGENERATION_FUEL
);
regenerationFuel.shape(" G ", "GAG", " G ");
regenerationFuel.setIngredient('G', Material.GOLD_INGOT);
regenerationFuel.setIngredient('A', Material.GOLDEN_APPLE);
Bukkit.addRecipe(regenerationFuel);
```

---

## 8. BOSS DROP SİSTEMİ

### 8.1. Boss Drop Tablosu

**Dosya:** `src/main/java/me/mami/stratocraft/listener/MobDropListener.java`

```java
// MobDropListener.java içine eklenecek

@EventHandler
public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    String mobName = entity.getCustomName();
    
    if (mobName == null) return;
    
    // Kaos Ejderi
    if (mobName.contains("Kaos Ejderi")) {
        // %5 şans ile Efsanevi Kristal Güçlendirme Taşı
        if (Math.random() < 0.05) {
            event.getDrops().add(ItemManager.CRYSTAL_ENHANCEMENT_STONE_LEGENDARY.clone());
        }
        // %20 şans ile Elite Taş
        if (Math.random() < 0.20) {
            event.getDrops().add(ItemManager.CRYSTAL_ENHANCEMENT_STONE_ELITE.clone());
        }
    }
    
    // Void Dragon
    if (mobName.contains("Void Dragon") || mobName.contains("Hiçlik Ejderi")) {
        // %10 şans ile Efsanevi Taş
        if (Math.random() < 0.10) {
            event.getDrops().add(ItemManager.CRYSTAL_ENHANCEMENT_STONE_LEGENDARY.clone());
        }
        // %30 şans ile Elite Taş
        if (Math.random() < 0.30) {
            event.getDrops().add(ItemManager.CRYSTAL_ENHANCEMENT_STONE_ELITE.clone());
        }
    }
    
    // Titan Golem
    if (mobName.contains("Titan Golem")) {
        // %15 şans ile Elite Taş
        if (Math.random() < 0.15) {
            event.getDrops().add(ItemManager.CRYSTAL_ENHANCEMENT_STONE_ELITE.clone());
        }
        // %40 şans ile Gelişmiş Taş
        if (Math.random() < 0.40) {
            event.getDrops().add(ItemManager.CRYSTAL_ENHANCEMENT_STONE_ADVANCED.clone());
        }
    }
    
    // Diğer bosslar...
}
```

---

## 9. TAM KOD İMPLEMENTASYONU

### 9.1. StructureType Enum'una Ekleme

**Dosya:** `src/main/java/me/mami/stratocraft/enums/StructureType.java`

```java
// StructureType enum'una eklenecek:
CRYSTAL_ENHANCEMENT_STRUCTURE,    // Kristal Güçlendirme Yapısı
CRYSTAL_ARMOR_STRUCTURE,          // Kristal Zırh Yapısı
CRYSTAL_SHIELD_STRUCTURE,         // Kristal Kalkan Yapısı
CRYSTAL_REGENERATION_STRUCTURE,   // Can Yenileme Yapısı
```

### 9.2. ChaosDragonHandler Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/handler/impl/ChaosDragonHandler.java`

```java
// attackCrystal metodunu güncelle:

private boolean attackCrystal(Disaster disaster, Location crystalLoc, Main plugin) {
    if (plugin == null || plugin.getTerritoryManager() == null) return false;
    
    Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
    if (targetClan == null) return false;
    
    EnderCrystal crystal = targetClan.getCrystalEntity();
    if (crystal == null || crystal.isDead()) return false;
    
    // Kalkan kontrolü
    CrystalShieldHandler shieldHandler = plugin.getCrystalShieldHandler();
    if (shieldHandler != null) {
        boolean blocked = shieldHandler.consumeShieldBlockOnDamage(targetClan);
        if (blocked) {
            // Saldırı engellendi
            crystalLoc.getWorld().spawnParticle(
                org.bukkit.Particle.BARRIER,
                crystalLoc,
                20,
                0.5, 0.5, 0.5, 0.1
            );
            return false; // Kristal hasar almadı
        }
    }
    
    // Felaket hasarı hesapla
    double baseDamage = disaster.getDamageMultiplier() * 10.0;
    
    // Zırh kontrolü
    CrystalArmorHandler armorHandler = plugin.getCrystalArmorHandler();
    if (armorHandler != null) {
        // Zırh yakıt tüket
        armorHandler.consumeFuelOnDamage(targetClan, baseDamage);
    }
    
    // Hasar azaltma çarpanı
    double damageReduction = targetClan.getCrystalDamageReduction();
    double finalDamage = baseDamage * (1.0 - damageReduction);
    
    // Kristale hasar ver
    targetClan.damageCrystal(finalDamage);
    
    double currentHealth = targetClan.getCrystalCurrentHealth();
    double maxHealth = targetClan.getCrystalMaxHealth();
    double healthPercent = (currentHealth / maxHealth) * 100.0;
    
    // Partikül efekti (can yüzdesine göre)
    if (healthPercent > 50) {
        crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, crystalLoc, 10);
    } else if (healthPercent > 25) {
        crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, crystalLoc, 15);
    } else {
        crystalLoc.getWorld().spawnParticle(org.bukkit.Particle.LAVA, crystalLoc, 20);
    }
    
    // Klan üyelerine uyarı
    for (UUID memberId : targetClan.getMembers().keySet()) {
        Player member = org.bukkit.Bukkit.getPlayer(memberId);
        if (member != null && member.isOnline()) {
            member.sendMessage("§c⚠ Kristal hasar aldı! Can: " + 
                String.format("%.1f", currentHealth) + "/" + 
                String.format("%.1f", maxHealth) + " (" + 
                String.format("%.1f", healthPercent) + "%)");
        }
    }
    
    // Can bitti mi?
    if (currentHealth <= 0) {
        crystal.remove();
        targetClan.destroyCrystal();
        org.bukkit.Bukkit.broadcastMessage(
            org.bukkit.ChatColor.RED + "" + org.bukkit.ChatColor.BOLD + 
            targetClan.getName() + " klanının kristali yok edildi!"
        );
        return true;
    }
    
    return false;
}
```

### 9.3. Main.java'da Handler Kayıtları

**Dosya:** `src/main/java/me/mami/stratocraft/Main.java`

```java
// Main.java içine eklenecek:

private CrystalEnhancementHandler crystalEnhancementHandler;
private CrystalArmorHandler crystalArmorHandler;
private CrystalShieldHandler crystalShieldHandler;
private CrystalRegenerationHandler crystalRegenerationHandler;

@Override
public void onEnable() {
    // ... diğer kodlar ...
    
    // Kristal sistem handler'ları
    crystalEnhancementHandler = new CrystalEnhancementHandler(this);
    crystalArmorHandler = new CrystalArmorHandler(this);
    crystalShieldHandler = new CrystalShieldHandler(this);
    crystalRegenerationHandler = new CrystalRegenerationHandler(this);
    
    getServer().getPluginManager().registerEvents(crystalEnhancementHandler, this);
    getServer().getPluginManager().registerEvents(crystalArmorHandler, this);
    getServer().getPluginManager().registerEvents(crystalShieldHandler, this);
    getServer().getPluginManager().registerEvents(crystalRegenerationHandler, this);
    
    // Klan gücü güncelleme task'ı
    new CrystalPowerUpdateTask(this).runTaskTimer(this, 0L, 6000L);
}

// Getter metodları
public CrystalEnhancementHandler getCrystalEnhancementHandler() {
    return crystalEnhancementHandler;
}

public CrystalArmorHandler getCrystalArmorHandler() {
    return crystalArmorHandler;
}

public CrystalShieldHandler getCrystalShieldHandler() {
    return crystalShieldHandler;
}

public CrystalRegenerationHandler getCrystalRegenerationHandler() {
    return crystalRegenerationHandler;
}
```

---

## ✅ ÖZET

Bu sistem:

1. ✅ **4 farklı yapı** içerir (Güçlendirme, Zırh, Kalkan, Yenileme)
2. ✅ **Özel itemler** ve **tarifler** içerir
3. ✅ **Boss drop sistemi** içerir
4. ✅ **Yakıt sistemi** içerir (Zırh, Kalkan, Yenileme için)
5. ✅ **Klan gücüne göre can artışı** içerir
6. ✅ **Tam kod implementasyonu** içerir
7. ✅ **Her yapı için detaylı mantık ve süreç** içerir

Tüm sistemler birbiriyle entegre çalışır ve kristal canını korumak için kapsamlı bir savunma sistemi sağlar.

