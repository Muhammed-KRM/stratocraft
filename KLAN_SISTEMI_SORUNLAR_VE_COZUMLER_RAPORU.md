# 🚨 Klan Sistemi Sorunlar ve Çözümler Raporu

## 📋 İÇİNDEKİLER

1. [Tespit Edilen Sorunlar](#tespit-edilen-sorunlar)
2. [Veri Yapıları Analizi](#veri-yapıları-analizi)
3. [Rütbe Sistemi Analizi](#rütbe-sistemi-analizi)
4. [Ritüel Sistemi Yeniden Tasarım Planı](#ritüel-sistemi-yeniden-tasarım-planı)
5. [Çözüm Önerileri](#çözüm-önerileri)

---

## 🐛 TESPİT EDİLEN SORUNLAR

### 1. ❌ KRİTİK: Klan Kristali Kırılma Kontrolü Eksik

**Sorun:**
- Kristal yerleştirildikten sonra isim girmeden önce kristal kırılırsa
- Sonra isim verilince klan yine de kuruluyor
- `pending.crystalEntity` null olabilir ama kontrol edilmiyor

**Mevcut Kod:**
```java
// TerritoryListener.java:464-490
org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
    Clan newClan = territoryManager.getClanManager().createClan(message, player.getUniqueId());
    if (newClan != null) {
        newClan.setCrystalLocation(pending.crystalLoc);
        newClan.setCrystalEntity(pending.crystalEntity); // ⚠️ NULL OLABİLİR!
        // ...
    }
});
```

**Sorun Detayı:**
- `onChatInput()` metodunda kristal kontrolü yok
- `pending.crystalEntity` null ise `NullPointerException` riski
- Kristal kırıldıysa klan kurulmamalı
- `onCrystalBreak()` metodunda pending kontrolü yok

**Çözüm:**
1. `onCrystalBreak()` metodunda pending kontrolü ekle
2. `onChatInput()` metodunda kristal kontrolü ekle
3. Kristal entity'si null veya ölü ise işlemi iptal et

---

### 2. ❌ KRİTİK: Üye Alma Ritüeli Çalışmıyor

**Sorun:**
- Üye alma ritüeli hiçbir işe yaramıyor
- Ritüel tetiklenmiyor veya oyuncular bulunmuyor

**Mevcut Kod:**
```java
// RitualInteractionListener.java:68-176
@EventHandler(priority = EventPriority.HIGH)
public void onRecruitmentRitual(PlayerInteractEvent event) {
    // Shift + Sağ Tık + Elde Çakmak
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    if (!event.getPlayer().isSneaking()) return;
    if (event.getHand() != EquipmentSlot.HAND) return;
    
    ItemStack handItem = event.getItem();
    if (handItem == null || handItem.getType() != Material.FLINT_AND_STEEL) return;
    
    Block centerBlock = event.getClickedBlock();
    if (centerBlock == null) return;
    
    // Merkez blok "Soyulmuş Odun" (Stripped Log) olmalı
    if (!isStrippedLog(centerBlock.getType())) return;
    
    // 3x3 Alan Kontrolü
    if (!checkRitualStructure(centerBlock)) {
        return; // Yapı bozuksa ateş yakar geçer, ritüel tetiklenmez
    }
    
    event.setCancelled(true); // Normal ateş yakmayı engelle
    // ...
}
```

**Sorun Detayı:**
- Ritüel yapısı kontrolü çok katı (3x3 tam olmalı)
- Oyuncu bulma mantığı yanlış (1.5 blok yarıçap çok küçük)
- Çakmak kullanımı yerine merkez bloğa ateş yakma olmalı
- Ritüel tetiklenme mantığı yanlış

**Çözüm:**
- Batarya sistemindeki gibi merkez blok + tarif kontrolü
- Merkez bloğun üstüne ateş yakmak ritüeli tetiklemeli(merkez blok genelde ortada olmalı mesela 3x3 bir blok dizilirseher hangi bir kenarın otrasındaki blok olmalı. yada merkez blok için fiğer bloklardan farklı ir blok kullanılmalı mesela diğer tüm odunlar soyulmuşken birtanesi soyulamaış olmalı.)
- Ritüel alanı daha geniş olmalı (kare/dikdörtgen)

---

### 3. ⚠️ Ritüel Sistemi Yeniden Tasarım Gerekiyor

**Kullanıcı İsteği:**
- Batarya sistemindeki gibi merkez blok olmalı
- Merkez bloğun üstüne ateş yakmak ritüeli tetiklemeli
- Çakmak kullanımı yerine merkez bloğa ateş yakma
- Ritüeller kare/dikdörtgen şeklinde
- İçindeki oyuncuları algılayıp klana almalı

**Mevcut Sistem:**
- Çakmak ile tetikleniyor
- 3x3 kare kontrolü yapılıyor
- Oyuncu bulma mantığı yanlış

**Yeni Sistem Tasarımı:**
- Merkez blok (örn: Stripped Log)
- Merkez bloğun üstüne ateş yakmak ritüeli tetikler
- Tarif kontrolü (batarya sistemindeki gibi)
- Ritüel alanı (kare/dikdörtgen) içindeki oyuncuları bul

---

## 📊 VERİ YAPILARI ANALİZİ

### Oyuncu Verileri

**Soru:** Oyuncunun durumunu tutan kod var mı?

**Cevap:** ❌ **HAYIR - Direkt Oyuncu Modeli Yok**

**Mevcut Durum:**

1. **Klan Üyeliği:** `ClanManager.playerClanMap` (UUID -> Clan UUID)
   ```java
   // ClanManager.java:23
   private final Map<UUID, UUID> playerClanMap = new ConcurrentHashMap<>();
   ```
   - **Ne Tutuyor:** Oyuncunun hangi klana üye olduğu
   - **Nasıl Kullanılıyor:** `getClanByPlayer(UUID)` ile klan bulunuyor

2. **Klan İçi Rütbe:** `Clan.members` (UUID -> Rank)
   ```java
   // Clan.java:25
   private final Map<UUID, Rank> members = Collections.synchronizedMap(new HashMap<>());
   ```
   - **Ne Tutuyor:** Klan içindeki oyuncunun rütbesi
   - **Nasıl Kullanılıyor:** `clan.getRank(UUID)` ile rütbe alınıyor

3. **Oyuncu Güç Profili:** `PlayerPowerProfile` (sadece güç verileri)
   ```java
   // PlayerPowerProfile.java
   public class PlayerPowerProfile {
       private UUID playerId;
       private double gearPower;
       private double trainingPower;
       // ... (klan bilgisi yok)
   }
   ```
   - **Ne Tutuyor:** Sadece güç verileri (klan bilgisi yok)

**Sorun:**
- Oyuncunun klan durumunu tutan **merkezi bir model yok**
- Klan bilgisi `ClanManager` ve `Clan` objelerinde dağınık
- Oyuncunun klan durumu **bool değişkeni yok**
- Oyuncu verileri bir arada değil

**Öneri:**
- `PlayerData` veya `StratocraftPlayer` modeli oluştur
- Klan durumu, rütbe, aktivite gibi verileri tut

---

### Model Dosyaları

**Konum:** `src/main/java/me/mami/stratocraft/model/`

**Mevcut Modeller:**
1. ✅ `Clan.java` - Klan modeli
2. ✅ `PlayerPowerProfile.java` - Oyuncu güç profili (klan bilgisi yok)
3. ✅ `ClanPowerProfile.java` - Klan güç profili
4. ✅ `Territory.java` - Klan bölgesi
5. ✅ `Structure.java` - Yapı modeli
6. ✅ `Contract.java` - Kontrat modeli
7. ✅ `Mission.java` - Görev modeli
8. ✅ `Shop.java` - Dükkan modeli
9. ✅ `Alliance.java` - İttifak modeli
10. ✅ `Disaster.java` - Felaket modeli

**Eksik:**
- ❌ `PlayerData.java` veya `StratocraftPlayer.java` - Oyuncu verileri modeli

---

## 🎖️ RÜTBE SİSTEMİ ANALİZİ

### Rütbe Enum'u

**Konum:** `Clan.java:9-20`

```java
public enum Rank {
    LEADER(5), ELITE(4), GENERAL(3), MEMBER(2), RECRUIT(1);
    private final int level;

    Rank(int level) {
        this.level = level;
    }

    public boolean isAtLeast(Rank other) {
        return this.level >= other.level;
    }
}
```

**Nasıl Tutuluyor:**
- **Enum olarak** tutuluyor (ID değil, string değil)
- `Clan.members` Map'inde: `Map<UUID, Rank>`
- Her oyuncu için bir `Rank` enum değeri

**Rütbe Seviyeleri:**
1. **LEADER (5):** En yüksek seviye - Tüm yetkiler
2. **ELITE (4):** Elite üye - Yapı inşa, Ritüel, Banka çekme (limitli), Görev başlatma
3. **GENERAL (3):** General - Yapı inşa/yıkma, Üye ekle/çıkar, Savaş başlat, Banka yönetimi
4. **MEMBER (2):** Normal üye - Sadece yapı kullanma
5. **RECRUIT (1):** Acemi (en düşük) - Hiçbir yetki

**Kullanım:**
```java
// Rütbe alma
Clan.Rank rank = clan.getRank(playerId);

// Rütbe kontrolü
if (rank == Clan.Rank.LEADER) {
    // Lider işlemleri
}

// Rütbe karşılaştırma
if (rank.isAtLeast(Clan.Rank.GENERAL)) {
    // General veya daha yüksek
}
```

**Veri Akışı:**
```
Oyuncu Klanda mı?
    ↓
ClanManager.playerClanMap.get(playerId) → Clan UUID
    ↓
ClanManager.clans.get(clanId) → Clan objesi
    ↓
Clan.members.get(playerId) → Rank enum
```

**Sorun:**
- ✅ Rütbe sistemi doğru çalışıyor
- ⚠️ Rütbe değiştirme işlemleri kontrol edilmeli
- ⚠️ Oyuncunun klan durumu bool değişkeni yok (sadece `getClanByPlayer() != null` kontrolü)

---

## 🔧 RİTÜEL SİSTEMİ YENİDEN TASARIM PLANI

### Mevcut Batarya Sistemi Analizi

**Batarya Sistemi Nasıl Çalışıyor:**

1. **Merkez Blok Kontrolü:**
   ```java
   // NewBatteryListener.java:152
   RecipeCheckResult result = batteryManager.checkAllRecipes(centerBlock);
   ```

2. **Tarif Kontrolü:**
   ```java
   // NewBatteryManager.java:349
   public RecipeCheckResult checkAllRecipes(Block centerBlock) {
       Material clickedMaterial = centerBlock.getType();
       
       // Tıklanan bloğun hangi tarifin merkez bloğu olduğunu kontrol et
       List<RecipeChecker> matchingCenterCheckers = new ArrayList<>();
       for (RecipeChecker checker : recipeCheckers.values()) {
           BlockPattern pattern = checker.getPattern();
           if (pattern != null && pattern.getCenterBlock() == clickedMaterial) {
               matchingCenterCheckers.add(checker);
           }
       }
       
       // SADECE tıklanan bloğun merkez bloğu olduğu tarifleri kontrol et
       for (RecipeChecker checker : matchingCenterCheckers) {
           RecipeCheckResult result = checker.checkRecipe(centerBlock);
           if (result.matches()) {
               return result;
           }
       }
   }
   ```

3. **Yakıt Kontrolü:**
   - Elinde yakıt item'ı ile merkez bloğa sağ tık
   - Yakıt tüketilir, batarya yüklenir

**Ritüel Sistemi İçin Yeni Tasarım:**

1. **Merkez Blok:**
   - Her ritüel için bir merkez blok tipi
   - Örn: Üye Alma = Stripped Log, Terfi = Stone Bricks, Ayrılma = Red Wool

2. **Tarif Kontrolü:**
   - Batarya sistemindeki gibi `BlockPattern` kullan
   - Merkez blok + etrafındaki bloklar = tarif

3. **Ateş Yakma:**
   - Merkez bloğun üstüne ateş yakmak ritüeli tetikler
   - Çakmak kullanımı yerine `BlockIgniteEvent` veya `BlockPlaceEvent` (FIRE)

4. **Oyuncu Bulma:**
   - Ritüel alanı (kare/dikdörtgen) içindeki oyuncuları bul
   - Daha geniş alan kontrolü (örn: 3x3x2 blok)

---

## 💡 ÇÖZÜM ÖNERİLERİ

### 1. Klan Kristali Kırılma Kontrolü

**Dosya:** `TerritoryListener.java`

**Değişiklikler:**

1. **onCrystalBreak() Metodunda Pending Kontrolü Ekle:**
   ```java
   @EventHandler(priority = EventPriority.HIGH)
   public void onCrystalBreak(EntityDamageEvent event) {
       if (!(event.getEntity() instanceof EnderCrystal)) return;
       
       EnderCrystal crystal = (EnderCrystal) event.getEntity();
       
       // ⚠️ YENİ: Pending klan oluşturma var mı?
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
       
       // Mevcut kod devam ediyor...
   }
   ```

2. **onChatInput() Metodunda Kontrol Ekle:**
   ```java
   @EventHandler(priority = EventPriority.HIGH)
   public void onChatInput(AsyncPlayerChatEvent event) {
       Player player = event.getPlayer();
       PendingClanCreation pending = waitingForClanName.get(player.getUniqueId());
       
       if (pending == null) return;
       
       // ⚠️ YENİ: Kristal kontrolü
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
       
       // Mevcut kod devam ediyor...
   }
   ```

---

### 2. Ritüel Sistemi Yeniden Tasarım

**Yeni Dosya:** `ClanRitualManager.java`

**Yeni Sistem:**

1. **Ritüel Tarifleri:**
   ```java
   public enum RitualType {
       RECRUITMENT,    // Üye Alma
       PROMOTION,      // Terfi
       DEMOTION,       // Rütbe Düşürme
       LEAVE,          // Ayrılma
       KICK            // Atma
   }
   
   public class RitualRecipe {
       private Material centerBlock;
       private BlockPattern pattern; // Batarya sistemindeki gibi
       private RitualType type;
       private Material fireTrigger; // FIRE veya SOUL_FIRE
       private int width;  // Ritüel alanı genişliği
       private int length; // Ritüel alanı uzunluğu
       private int height; // Ritüel alanı yüksekliği
   }
   ```

2. **Ritüel Tetikleme (BlockPlaceEvent - FIRE):**
   ```java
   @EventHandler(priority = EventPriority.HIGH)
   public void onFirePlace(BlockPlaceEvent event) {
       if (event.getBlock().getType() != Material.FIRE && 
           event.getBlock().getType() != Material.SOUL_FIRE) return;
       
       Block fireBlock = event.getBlock();
       Block centerBlock = fireBlock.getRelative(BlockFace.DOWN);
       
       // Ritüel tarifi kontrol et
       RitualRecipe recipe = ritualManager.findRecipe(centerBlock);
       if (recipe == null) return; // Ritüel değil
       
       // Ritüel alanı kontrol et (tarif eşleşiyor mu?)
       if (!ritualManager.checkRitualArea(centerBlock, recipe)) {
           return; // Tarif eşleşmedi
       }
       
       // Ritüel tetikle
       ritualManager.triggerRitual(event.getPlayer(), recipe, centerBlock);
   }
   ```

3. **Oyuncu Bulma:**
   ```java
   public List<Player> findPlayersInRitualArea(Block centerBlock, RitualRecipe recipe) {
       // Ritüel alanı hesapla (kare/dikdörtgen)
       Location center = centerBlock.getLocation().add(0.5, 1, 0.5);
       int width = recipe.getWidth(); // Örn: 3
       int length = recipe.getLength(); // Örn: 3
       int height = recipe.getHeight(); // Örn: 2
       
       List<Player> players = new ArrayList<>();
       for (Entity entity : centerBlock.getWorld().getNearbyEntities(
               center, width/2.0, height, length/2.0)) {
           if (entity instanceof Player) {
               players.add((Player) entity);
           }
       }
       return players;
   }
   ```

---

### 3. Oyuncu Veri Modeli Oluşturma

**Yeni Dosya:** `PlayerData.java`

```java
package me.mami.stratocraft.model;

import java.util.UUID;

public class PlayerData {
    private UUID playerId;
    private UUID clanId; // null = klansız
    private Clan.Rank rank; // null = klansız
    private boolean isInClan; // Klan durumu bool değişkeni
    private long lastActivity;
    // ... diğer veriler
    
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.isInClan = false;
        this.clanId = null;
        this.rank = null;
    }
    
    public void setClan(UUID clanId, Clan.Rank rank) {
        this.clanId = clanId;
        this.rank = rank;
        this.isInClan = (clanId != null);
    }
    
    public void leaveClan() {
        this.clanId = null;
        this.rank = null;
        this.isInClan = false;
    }
    
    // Getters
    public UUID getPlayerId() { return playerId; }
    public UUID getClanId() { return clanId; }
    public Clan.Rank getRank() { return rank; }
    public boolean isInClan() { return isInClan; }
    public long getLastActivity() { return lastActivity; }
    
    // Setters
    public void setLastActivity(long time) { this.lastActivity = time; }
}
```

**Yeni Dosya:** `PlayerDataManager.java`

```java
package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.PlayerData;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData(playerId));
    }
    
    public void setClan(UUID playerId, UUID clanId, Clan.Rank rank) {
        PlayerData data = getPlayerData(playerId);
        data.setClan(clanId, rank);
    }
    
    public void leaveClan(UUID playerId) {
        PlayerData data = getPlayerData(playerId);
        data.leaveClan();
    }
    
    public boolean isInClan(UUID playerId) {
        PlayerData data = getPlayerData(playerId);
        return data.isInClan();
    }
}
```

**ClanManager Entegrasyonu:**
```java
// ClanManager.java:addMember() metodunda
public void addMember(Clan clan, UUID memberId, Clan.Rank rank) {
    // Mevcut kod...
    
    // ⚠️ YENİ: PlayerData güncelle
    if (playerDataManager != null) {
        playerDataManager.setClan(memberId, clan.getId(), rank);
    }
}

// ClanManager.java:removeMember() metodunda
public void removeMember(Clan clan, UUID memberId) {
    // Mevcut kod...
    
    // ⚠️ YENİ: PlayerData güncelle
    if (playerDataManager != null) {
        playerDataManager.leaveClan(memberId);
    }
}
```

---

## 📝 ÖNCELİK SIRASI

### Yüksek Öncelik

1. **Klan Kristali Kırılma Kontrolü** ⚠️ KRİTİK
   - `onCrystalBreak()` metodunda pending kontrolü ekle
   - `onChatInput()` metodunda kristal kontrolü ekle
   - Test et

2. **Oyuncu Veri Modeli Oluşturma**
   - `PlayerData.java` oluştur
   - `PlayerDataManager.java` oluştur
   - `ClanManager` entegrasyonu

### Orta Öncelik

3. **Ritüel Sistemi Yeniden Tasarım**
   - `ClanRitualManager.java` oluştur
   - Batarya sistemindeki gibi tarif kontrolü
   - Ateş yakma ile tetikleme

4. **Üye Alma Ritüeli Düzeltme**
   - Yeni ritüel sistemi ile entegre et
   - Test et

---

## 📊 ÖZET

### Tespit Edilen Sorunlar

1. ❌ **Klan Kristali Kırılma Kontrolü Eksik** - KRİTİK
2. ❌ **Üye Alma Ritüeli Çalışmıyor** - KRİTİK
3. ⚠️ **Ritüel Sistemi Yeniden Tasarım Gerekiyor**

### Veri Yapıları

- ❌ **Oyuncu Veri Modeli Yok** - `PlayerData` oluşturulmalı
- ✅ **Rütbe Enum'u Var** - `Clan.Rank` enum olarak tutuluyor
- ✅ **Klan Üyeliği Tutuluyor** - `ClanManager.playerClanMap` ve `Clan.members`

### Çözümler

1. **Klan Kristali:** `onCrystalBreak()` ve `onChatInput()` metodlarına kontrol ekle
2. **Ritüel Sistemi:** Batarya sistemindeki gibi merkez blok + tarif kontrolü
3. **Oyuncu Verileri:** `PlayerData` modeli oluştur

---

**Son Güncelleme:** 2024
**Durum:** ⚠️ **SORUNLAR TESPİT EDİLDİ** - Çözümler hazır
