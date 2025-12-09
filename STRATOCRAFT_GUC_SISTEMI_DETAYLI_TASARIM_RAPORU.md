# 🎯 STRATOCRAFT GÜÇ SİSTEMİ - DETAYLI TASARIM RAPORU

## 📋 İÇİNDEKİLER

1. [Genel Amaç ve Felsefe](#genel-amac)
2. [Temel Kavramlar ve Terminoloji](#temel-kavramlar)
3. [Güç Kaynakları ve Detaylı Formüller](#guc-kaynaklari)
4. [Seviye Sistemi ve Eğri Tasarımı](#seviye-sistemi)
5. [Koruma Sistemi Detayları](#koruma-sistemi)
6. [Mevcut Sistemlerle Entegrasyon](#entegrasyon)
7. [Kod Mimarisi ve Tasarım Prensipleri](#kod-mimarisi)
8. [Performans Optimizasyonu](#performans)
9. [Test Senaryoları ve Örnekler](#test-senaryolari)
10. [Gelecek Geliştirmeler](#gelecek)

---

## 🎯 GENEL AMAÇ VE FELSEFE {#genel-amac}

### Sistemin Hedefleri

Bu hibrit güç sistemi, Stratocraft oyununun **bel kemiği** olacak şekilde tasarlanmıştır. Sistemin temel amaçları:

#### 1. **Tek Çatı Altında Toplama** 🏗️
- Tüm güç kaynaklarını (eşya, ritüel, yapılar, ustalık, bufflar) tek bir merkezi sistemde toplamak
- Dağınık güç hesaplamalarını birleştirmek
- Tutarlı ve öngörülebilir bir güç modeli oluşturmak

#### 2. **Modüler ve Yeniden Kullanılabilir Yapı** 🔄
- Hem PvP/PvE hem de felaket/klan sistemleri için kullanılabilir
- Her sistem kendi ihtiyacına göre güç bileşenlerini seçebilir
- Yeni sistemler kolayca entegre edilebilir

#### 3. **Oyuncuya Gelişimi Hissettirme** 📈
- Dengeyi bozmadan ilerleme hissi vermek
- Her seviye atlamada anlamlı bir güç artışı
- Hem hızlı hem de uzun vadeli hedefler sunmak

#### 4. **Tam Config Tabanlı Yönetim** ⚙️
- Tüm değerler config.yml'den yönetilebilir
- Formüller bile parametreleştirilmiş
- Oyunu kapatmadan dengeleme yapılabilir

#### 5. **Temiz ve Genişletilebilir Kod** 💻
- Her özellik için ayrı fonksiyon
- Strategy Pattern kullanımı
- Değişiklikler birbirini etkilemez

---

## 📚 TEMEL KAVRAMLAR VE TERMİNOLOJİ {#temel-kavramlar}

### Güç Türleri

#### **SGP (Stratocraft Global Power)** 🌐
- **Tanım:** Oyuncu veya klan için hesaplanan **toplam güç puanı**
- **Kullanım:** Genel güç seviyesi, klan sıralaması, genel ilerleme
- **Hesaplama:** Combat Power + Progression Power (ağırlıklı toplam)

#### **Combat Power (CP)** ⚔️
- **Tanım:** Savaş/dövüş odaklı **anlık güç**
- **Bileşenler:**
  - Eşya gücü (silah + zırh)
  - Aktif bufflar
  - Özel itemler
- **Kullanım:** PvP, PvE, Felaket zorluğu hesaplama

#### **Progression Power (PP)** 🏰
- **Tanım:** Kalıcı ilerleme/gelişim gücü
- **Bileşenler:**
  - Klan yapıları
  - Ritüel blokları
  - Ritüel kaynakları
  - Ustalık seviyeleri
- **Kullanım:** Klan seviyesi, toprak limiti, ekonomi sistemleri

### Veri Yapıları

#### **PlayerPowerProfile** 👤
```java
public class PlayerPowerProfile {
    // Bileşenler
    private double gearPower;           // Eşya gücü
    private double trainingPower;       // Ustalık gücü
    private double buffPower;           // Buff gücü
    private double ritualPower;         // Ritüel gücü (oyuncu bazlı)
    
    // Toplamlar
    private double totalCombatPower;    // CP
    private double totalProgressionPower; // PP
    private double totalSGP;            // Toplam güç
    
    // Meta
    private int playerLevel;            // Hesaplanmış seviye
    private long lastUpdate;            // Son güncelleme zamanı
}
```

#### **ClanPowerProfile** 🏛️
```java
public class ClanPowerProfile {
    // Bileşenler
    private double memberPowerSum;     // Üyelerin toplam gücü
    private double structurePower;      // Yapı gücü
    private double ritualBlockPower;    // Ritüel blok gücü
    private double ritualResourcePower; // Ritüel kaynak gücü
    
    // Toplam
    private double totalClanPower;      // Toplam klan gücü
    
    // Meta
    private int clanLevel;              // Hesaplanmış klan seviyesi
    private long lastUpdate;            // Son güncelleme zamanı
}
```

### Güç Ayrımının Avantajları

| Sistem | Kullanacağı Güç Türü | Neden |
|--------|---------------------|-------|
| **Felaket Sistemi** | Combat Power (CP) | Anlık savaş gücü önemli |
| **Klan Seviyesi** | Progression Power (PP) | Kalıcı yatırımlar önemli |
| **PvP Koruma** | Total SGP | Genel güç farkı önemli |
| **Toprak Sistemi** | Progression Power (PP) | Yapılar ve gelişim önemli |
| **Ekonomi** | Total SGP | Genel güç = ekonomik potansiyel |

---

## 💎 GÜÇ KAYNAKLARI VE DETAYLI FORMÜLLER {#guc-kaynaklari}

### 2.1. Eşya Gücü (Gear Power) ⚔️

#### Kaynaklar
- **Silah Seviyesi:** `ItemManager.getWeaponLevel(item)`
- **Zırh Seviyesi:** `ItemManager.getArmorLevel(item)`
- **Özel Itemler:** `SpecialItemManager` (gelecekte)

#### Puan Tablosu

| Seviye | Silah Puanı | Zırh Puanı (Parça) | Açıklama |
|--------|------------|-------------------|----------|
| **1** | 60 | 40 | Başlangıç ekipmanı |
| **2** | 150 | 100 | Gelişmiş ekipman |
| **3** | 400 | 250 | Elit ekipman |
| **4** | 900 | 600 | Efsanevi ekipman |
| **5** | 1600 | 1000 | Tanrısal ekipman |

#### Örnek Hesaplamalar

**Senaryo 1: Yeni Oyuncu**
```
Silah: Seviye 1 (60 puan)
Zırh: 2 parça Seviye 1 (2 × 40 = 80 puan)
Toplam Gear Power: 140 puan
```

**Senaryo 2: Orta Seviye Oyuncu**
```
Silah: Seviye 3 (400 puan)
Zırh: 4 parça Seviye 2 (4 × 100 = 400 puan)
Toplam Gear Power: 800 puan
```

**Senaryo 3: Elit Oyuncu (Tam Set Seviye 5)**
```
Silah: Seviye 5 (1600 puan)
Zırh: 4 parça Seviye 5 (4 × 1000 = 4000 puan)
Toplam Gear Power: 5600 puan
```

#### Fonksiyon Yapısı

```java
/**
 * Silah gücü hesapla
 */
private double calculateWeaponPower(Player player) {
    ItemStack weapon = player.getInventory().getItemInMainHand();
    if (weapon == null) return 0.0;
    
    int level = ItemManager.getWeaponLevel(weapon);
    return powerConfig.getWeaponPower(level);
}

/**
 * Zırh gücü hesapla (tüm parçalar)
 */
private double calculateArmorPower(Player player) {
    double totalPower = 0.0;
    ItemStack[] armor = player.getInventory().getArmorContents();
    
    for (ItemStack piece : armor) {
        if (piece != null) {
            int level = ItemManager.getArmorLevel(piece);
            totalPower += powerConfig.getArmorPower(level);
        }
    }
    
    // Tam set bonusu (4 parça)
    if (armor.length == 4 && allPiecesEquipped(armor)) {
        totalPower *= powerConfig.getArmorSetBonus(); // Örn: 1.1x
    }
    
    return totalPower;
}

/**
 * Toplam eşya gücü
 */
public double calculateGearPower(Player player) {
    return calculateWeaponPower(player) + 
           calculateArmorPower(player) + 
           calculateSpecialItemPower(player);
}
```

---

### 2.2. Ritüel Blok Gücü (Ritual Block Power) 🏗️

#### Kaynaklar
- Klan arazisi içindeki belirli blok türleri
- `TerritoryManager` ile klan arazisi sınırları
- Ritüel sistemi ile kullanılan bloklar

#### Puan Tablosu

| Blok Türü | Puan | Açıklama |
|-----------|------|----------|
| **Demir Blok** | 8 | Temel yapı malzemesi |
| **Altın Blok** | 12 | Değerli yapı malzemesi |
| **Elmas Blok** | 25 | Çok değerli yapı malzemesi |
| **Obsidyen** | 30 | Güçlü savunma malzemesi |
| **Zümrüt Blok** | 35 | Nadir yapı malzemesi |
| **Titanyum/Netherite** | 150 | Efsanevi yapı malzemesi |
| **Diğer** | 5 | Varsayılan değer |

#### Tasarımsal Kararlar

**1. Klan Arazisi Odaklı:**
- ✅ Sadece klan arazisi içindeki bloklar sayılır
- ✅ Oyuncu envanteri değil, **yerleşim** önemli
- ✅ Progression odaklı (mühendislik ödülü)

**2. Async Tarama:**
- ✅ Sync tarama yapılmaz (performans)
- ✅ 15 dakikada bir async task ile taranır
- ✅ Sadece değişiklik olduğunda güncellenir

**3. Snapshot Sistemi:**
```java
public class ClanRitualBlockSnapshot {
    private UUID clanId;
    private Map<Material, Integer> blockCounts; // Blok türü -> sayı
    private long lastScanTime;
    private boolean needsUpdate;
}
```

#### Örnek Senaryo

**Klan Arazisi Taraması:**
```
Klan: "Epic Builders"
Arazi: 100x100 blok (10,000 blok)

Tarama Sonuçları:
- Demir Blok: 500 adet → 500 × 8 = 4,000 puan
- Elmas Blok: 50 adet → 50 × 25 = 1,250 puan
- Obsidyen: 200 adet → 200 × 30 = 6,000 puan
- Titanyum Blok: 10 adet → 10 × 150 = 1,500 puan

Toplam Ritüel Blok Gücü: 12,750 puan
```

#### Fonksiyon Yapısı

```java
/**
 * Klan ritüel blok gücü hesapla (snapshot'tan)
 */
public double calculateClanRitualBlockPower(Clan clan) {
    ClanRitualBlockSnapshot snapshot = getBlockSnapshot(clan);
    if (snapshot == null) return 0.0;
    
    double totalPower = 0.0;
    for (Map.Entry<Material, Integer> entry : snapshot.getBlockCounts().entrySet()) {
        double blockPower = powerConfig.getRitualBlockPower(entry.getKey());
        totalPower += blockPower * entry.getValue();
    }
    
    return totalPower;
}

/**
 * Async blok taraması (15 dakikada bir)
 */
@Async
public void scanClanTerritoryBlocks(Clan clan) {
    Territory territory = territoryManager.getTerritory(clan);
    if (territory == null) return;
    
    Map<Material, Integer> blockCounts = new HashMap<>();
    
    // Klan arazisi içindeki blokları tara
    for (Location loc : territory.getAllBlocks()) {
        Material material = loc.getBlock().getType();
        if (isRitualBlock(material)) {
            blockCounts.put(material, blockCounts.getOrDefault(material, 0) + 1);
        }
    }
    
    // Snapshot'ı güncelle
    updateBlockSnapshot(clan, blockCounts);
}
```

---

### 2.3. Ritüel Kaynak Gücü (Ritual Resource Power) 🔮

#### Kaynaklar
- Ritüelleri aktif eden itemler/kaynaklar
- Ritüel kullanım geçmişi

#### Puan Tablosu

| Kaynak Türü | Puan | Açıklama |
|-------------|------|----------|
| **Demir** | 5 | Temel ritüel kaynağı |
| **Elmas** | 10 | Değerli ritüel kaynağı |
| **Kızıl Elmas** | 18 | Nadir ritüel kaynağı |
| **Titanyum** | 15 | İleri ritüel kaynağı |
| **Karanlık Madde** | 50 | Efsanevi ritüel kaynağı |
| **Diğer** | 3 | Varsayılan değer |

#### Tasarımsal Kararlar

**1. Progression Odaklı:**
- ✅ Ritüel **başarıyla tamamlandığında** puan kazanılır
- ✅ Kullanım geçmişi kaydedilir
- ✅ Toplam kullanım sayısına göre puan

**2. Ritüel İstatistikleri:**
```java
public class ClanRitualStats {
    private UUID clanId;
    private Map<String, RitualUsage> ritualUsages; // Ritüel tipi -> kullanım bilgisi
    
    public class RitualUsage {
        private int totalUses;           // Toplam kullanım sayısı
        private Map<String, Integer> resourcesUsed; // Kaynak -> miktar
        private long lastUsedTime;
    }
}
```

#### Örnek Senaryo

**Klan Ritüel Geçmişi:**
```
Klan: "Mystic Order"

Ritüel Kullanımları:
- Ateş Ritüeli: 50 kullanım
  → Kullanılan: 50 × Demir (5 puan) = 250 puan
  
- Terfi Ritüeli: 20 kullanım
  → Kullanılan: 20 × Altın (12 puan) = 240 puan
  
- Boss Çağırma: 5 kullanım
  → Kullanılan: 5 × Karanlık Madde (50 puan) = 250 puan

Toplam Ritüel Kaynak Gücü: 740 puan
```

#### Fonksiyon Yapısı

```java
/**
 * Klan ritüel kaynak gücü hesapla
 */
public double calculateClanRitualResourcePower(Clan clan) {
    ClanRitualStats stats = getRitualStats(clan);
    if (stats == null) return 0.0;
    
    double totalPower = 0.0;
    for (RitualUsage usage : stats.getRitualUsages().values()) {
        for (Map.Entry<String, Integer> entry : usage.getResourcesUsed().entrySet()) {
            double resourcePower = powerConfig.getRitualResourcePower(entry.getKey());
            totalPower += resourcePower * entry.getValue();
        }
    }
    
    return totalPower;
}

/**
 * Ritüel başarıyla tamamlandığında çağrılır
 */
public void onRitualSuccess(Clan clan, String ritualType, 
                           Map<String, Integer> usedResources) {
    ClanRitualStats stats = getOrCreateRitualStats(clan);
    RitualUsage usage = stats.getRitualUsages().getOrDefault(ritualType, new RitualUsage());
    
    usage.setTotalUses(usage.getTotalUses() + 1);
    usage.setLastUsedTime(System.currentTimeMillis());
    
    // Kullanılan kaynakları ekle
    for (Map.Entry<String, Integer> entry : usedResources.entrySet()) {
        int current = usage.getResourcesUsed().getOrDefault(entry.getKey(), 0);
        usage.getResourcesUsed().put(entry.getKey(), current + entry.getValue());
    }
    
    // Cache'i güncelle
    clearClanCache(clan);
}
```

---

### 2.4. Klan Yapı Gücü (Structure Power) 🏰

#### Kaynaklar
- `Clan.getStructures()` - Klan yapıları ve seviyeleri
- Klan Kristali (sabit bonus)

#### Puan Tablosu

| Seviye | Yapı Puanı | Açıklama |
|--------|-----------|----------|
| **1** | 100 | Temel yapı |
| **2** | 250 | Gelişmiş yapı |
| **3** | 500 | Elit yapı |
| **4** | 1200 | Efsanevi yapı |
| **5** | 2000 | Tanrısal yapı |
| **Klan Kristali** | +500 | Sabit bonus (her zaman) |

#### Örnek Senaryo

**Klan Yapıları:**
```
Klan: "Fortress Builders"

Yapılar:
- Savunma Kulesi: Seviye 3 → 500 puan
- Üretim Tesisi: Seviye 2 → 250 puan
- Araştırma Merkezi: Seviye 4 → 1,200 puan
- Batarya: Seviye 5 → 2,000 puan
- Klan Kristali: +500 puan (sabit)

Toplam Yapı Gücü: 4,450 puan
```

#### Fonksiyon Yapısı

```java
/**
 * Klan yapı gücü hesapla
 */
public double calculateClanStructurePower(Clan clan) {
    if (clan == null) return 0.0;
    
    double totalPower = 0.0;
    
    // Klan Kristali (sabit bonus)
    if (clan.getCrystalEntity() != null && !clan.getCrystalEntity().isDead()) {
        totalPower += powerConfig.getCrystalBasePower(); // 500
    }
    
    // Yapılar
    for (Structure structure : clan.getStructures()) {
        int level = structure.getLevel();
        double structurePower = powerConfig.getStructurePower(level);
        
        // Yapı tipine göre çarpan (opsiyonel)
        double typeMultiplier = powerConfig.getStructureTypeMultiplier(structure.getType());
        totalPower += structurePower * typeMultiplier;
    }
    
    return totalPower;
}
```

---

### 2.5. Antrenman / Ustalık Gücü (Training / Mastery Power) 🎓

#### Kaynaklar
- `TrainingManager.getTotalUses(player, ritualId)`
- `TrainingManager.getMasteryLevel()`

#### Hibrit Formül

\[
\text{masteryPower} = B \times \left(\frac{\text{masteryPercent}}{100}\right)^{E}
\]

**Parametreler:**
- `B` (base-power) = **150** (config'den)
- `E` (exponent) = **1.4** (config'den)

#### Puan Tablosu

| Ustalık Yüzdesi | Hesaplama | Puan | Açıklama |
|----------------|-----------|------|----------|
| **%100** | 150 × (1.0)^1.4 | 150 | Normal güç |
| **%150** | 150 × (1.5)^1.4 | ~250 | İyi ustalık |
| **%200** | 150 × (2.0)^1.4 | ~400 | Mükemmel ustalık |
| **%300** | 150 × (3.0)^1.4 | ~700 | Grandmaster |

#### Tasarımsal Kararlar

**1. %100 Altı:**
- ✅ Ekstra puan verilmez (normal kabul edilir)
- ✅ Sadece "tam güce ulaşmış" ritüeller bonus verir

**2. Üstel Artış:**
- ✅ Yüksek ustalık zor kazanılır, ödülü de büyük olur
- ✅ Exponent 1.4 → dengeli artış (çok agresif değil)

#### Örnek Senaryo

**Oyuncu Ustalık Geçmişi:**
```
Oyuncu: "MasterRitualist"

Ritüel Ustalıkları:
- Ateş Topu: 200 kullanım → %200 ustalık → ~400 puan
- Buz Duvarı: 150 kullanım → %150 ustalık → ~250 puan
- Şimşek Çağırma: 300 kullanım → %300 ustalık → ~700 puan
- Toprak Kalkanı: 50 kullanım → %50 ustalık → 0 puan (henüz %100 değil)

Toplam Ustalık Gücü: 1,350 puan
```

#### Fonksiyon Yapısı

```java
/**
 * Oyuncu ustalık gücü hesapla
 */
public double calculatePlayerTrainingMasteryPower(Player player) {
    if (trainingManager == null) return 0.0;
    
    UUID playerId = player.getUniqueId();
    Map<String, Integer> playerTraining = trainingManager.getAllTrainingData()
        .getOrDefault(playerId, new HashMap<>());
    
    if (playerTraining.isEmpty()) return 0.0;
    
    double totalPower = 0.0;
    
    for (String ritualId : playerTraining.keySet()) {
        int totalUses = trainingManager.getTotalUses(playerId, ritualId);
        
        // Ustalık yüzdesi hesapla (örnek: 200 kullanım = %200)
        // Bu formül TrainingManager'dan alınabilir veya burada hesaplanabilir
        double masteryPercent = calculateMasteryPercent(totalUses, ritualId);
        
        if (masteryPercent > 100) {
            totalPower += powerConfig.getMasteryPower(masteryPercent);
        }
    }
    
    return totalPower;
}

/**
 * Ustalık gücü formülü
 */
private double getMasteryPower(double masteryPercent) {
    if (masteryPercent <= 100) return 0.0;
    
    double basePower = powerConfig.getMasteryBasePower(); // 150
    double exponent = powerConfig.getMasteryExponent();   // 1.4
    
    return basePower * Math.pow(masteryPercent / 100.0, exponent);
}
```

---

### 2.6. Toplam Güç Hesaplama (Orchestrator) 🎼

#### PlayerPowerProfile Hesaplama

```java
/**
 * Oyuncu güç profili hesapla (tüm bileşenleri topla)
 */
public PlayerPowerProfile calculatePlayerProfile(Player player) {
    if (player == null || !player.isOnline()) {
        return new PlayerPowerProfile(); // Boş profil
    }
    
    PlayerPowerProfile profile = new PlayerPowerProfile();
    
    // 1. Eşya gücü
    profile.setGearPower(calculateGearPower(player));
    
    // 2. Ustalık gücü
    profile.setTrainingPower(calculatePlayerTrainingMasteryPower(player));
    
    // 3. Buff gücü (felaket sistemiyle uyum için)
    profile.setBuffPower(calculateBuffPower(player));
    
    // 4. Ritüel gücü (oyuncu bazlı, gelecekte eklenebilir)
    profile.setRitualPower(0.0); // Şimdilik 0
    
    // Toplamlar
    double combatPower = profile.getGearPower() + 
                        profile.getBuffPower();
    
    double progressionPower = profile.getTrainingPower() + 
                            profile.getRitualPower();
    
    // Ağırlıklı toplam (config'den)
    double combatWeight = powerConfig.getCombatPowerWeight();    // 0.6
    double progressionWeight = powerConfig.getProgressionPowerWeight(); // 0.4
    
    double totalSGP = (combatPower * combatWeight) + 
                      (progressionPower * progressionWeight);
    
    profile.setTotalCombatPower(combatPower);
    profile.setTotalProgressionPower(progressionPower);
    profile.setTotalSGP(totalSGP);
    
    // Seviye hesapla
    profile.setPlayerLevel(calculatePlayerLevel(totalSGP));
    profile.setLastUpdate(System.currentTimeMillis());
    
    return profile;
}
```

#### ClanPowerProfile Hesaplama

```java
/**
 * Klan güç profili hesapla
 */
public ClanPowerProfile calculateClanProfile(Clan clan) {
    if (clan == null) return new ClanPowerProfile();
    
    ClanPowerProfile profile = new ClanPowerProfile();
    
    // 1. Üye güçleri toplamı
    double memberPowerSum = 0.0;
    for (UUID memberId : clan.getMembers()) {
        Player member = Bukkit.getPlayer(memberId);
        if (member != null && member.isOnline()) {
            PlayerPowerProfile memberProfile = calculatePlayerProfile(member);
            memberPowerSum += memberProfile.getTotalSGP();
        }
    }
    profile.setMemberPowerSum(memberPowerSum);
    
    // 2. Yapı gücü
    profile.setStructurePower(calculateClanStructurePower(clan));
    
    // 3. Ritüel blok gücü
    profile.setRitualBlockPower(calculateClanRitualBlockPower(clan));
    
    // 4. Ritüel kaynak gücü
    profile.setRitualResourcePower(calculateClanRitualResourcePower(clan));
    
    // Toplam klan gücü
    double totalClanPower = memberPowerSum + 
                           profile.getStructurePower() + 
                           profile.getRitualBlockPower() + 
                           profile.getRitualResourcePower();
    
    profile.setTotalClanPower(totalClanPower);
    
    // Klan seviyesi hesapla
    profile.setClanLevel(calculateClanLevel(totalClanPower));
    profile.setLastUpdate(System.currentTimeMillis());
    
    return profile;
}
```

---

## 📊 SEVİYE SİSTEMİ VE EĞRİ TASARIMI {#seviye-sistemi}

### 3.1. Oyuncu Seviye Eğrisi (Hibrit Sistem) 📈

#### Aşama 1: Hızlı İlerleme (Seviye 1-10)

**Formül:**
\[
\text{level} = \sqrt{\frac{\text{power}}{100}}
\]

**Özellikler:**
- ✅ Yeni oyuncular hızlı seviye atlar
- ✅ Motivasyon sağlar
- ✅ İlk 10 seviye kolay ulaşılır

**Seviye Tablosu:**

| Güç Puanı | Seviye | Açıklama |
|-----------|--------|----------|
| 100 | 1 | Başlangıç |
| 400 | 2 | İlk ilerleme |
| 900 | 3 | Gelişim |
| 1,600 | 4 | Orta seviye |
| 2,500 | 5 | İyi seviye |
| 3,600 | 6 | Gelişmiş |
| 4,900 | 7 | İleri seviye |
| 6,400 | 8 | Çok iyi |
| 8,100 | 9 | Mükemmel |
| 10,000 | 10 | Elit |

#### Aşama 2: Zor İlerleme (Seviye 11+)

**Formül:**
\[
\text{level} = 10 + \left\lfloor \log_{10}\left(\frac{\text{power}}{10000}\right) \times 3 \right\rfloor
\]

**Özellikler:**
- ✅ İleri seviyeler zor kazanılır
- ✅ Prestij sağlar
- ✅ Uzun vadeli hedef

**Seviye Tablosu:**

| Güç Puanı | Seviye | Açıklama |
|-----------|--------|----------|
| 10,000 | 10 | Elit (geçiş noktası) |
| 25,000 | 11 | Master |
| 50,000 | 12 | Grandmaster |
| 100,000 | 13 | Legend |
| 200,000 | 14 | Mythic |
| 500,000 | 15 | Transcendent |
| 1,000,000 | 16 | Divine |

#### Hibrit Sistem Avantajları

✅ **Yeni Oyuncular:** Hızlı ilerleme → motivasyon  
✅ **Deneyimli Oyuncular:** Zorlu hedefler → prestij  
✅ **Dengeli Eğri:** Ne çok kolay ne çok zor  

#### Fonksiyon Yapısı

```java
/**
 * Oyuncu seviyesi hesapla (hibrit sistem)
 */
public int calculatePlayerLevel(double power) {
    if (power < 0) return 1;
    
    // Geçiş noktası (config'den)
    double switchPower = powerConfig.getLevelSwitchPower(); // 10,000
    
    if (power < switchPower) {
        // Aşama 1: Karekök (hızlı ilerleme)
        double basePower = powerConfig.getLevelBasePower(); // 100
        double level = Math.sqrt(power / basePower);
        return Math.max(1, (int) Math.floor(level));
    } else {
        // Aşama 2: Logaritmik (zor ilerleme)
        double multiplier = powerConfig.getLevelMultiplier(); // 3.0
        double level = 10 + Math.floor(Math.log10(power / switchPower) * multiplier);
        int maxLevel = powerConfig.getMaxPlayerLevel(); // 20
        return Math.min((int) level, maxLevel);
    }
}
```

---

### 3.2. Klan Seviye Eğrisi 🏛️

#### Formül

\[
\text{clanLevel} = \max\left(1,\ \left\lfloor \log_{10}\left(\frac{\text{clanPower}}{\text{clanBasePower}}\right) \times \text{clanMultiplier} \right\rfloor + 1\right)
\]

**Parametreler:**
- `clanBasePower` = **500** (config'den)
- `clanMultiplier` = **2.0** (config'den)

#### Seviye Tablosu

| Klan Gücü | Seviye | Açıklama |
|-----------|--------|----------|
| 500 | 1 | Yeni Klan |
| 1,580 | 2 | Gelişen Klan |
| 5,000 | 3 | Orta Klan |
| 15,800 | 4 | İleri Klan |
| 50,000 | 5 | Güçlü Klan |
| 158,000 | 6 | Çok Güçlü Klan |
| 500,000 | 7 | Efsanevi Klan |
| 1,580,000 | 8 | Destansı Klan |

#### Fonksiyon Yapısı

```java
/**
 * Klan seviyesi hesapla
 */
public int calculateClanLevel(double clanPower) {
    if (clanPower < 0) return 1;
    
    double basePower = powerConfig.getClanLevelBasePower(); // 500
    double multiplier = powerConfig.getClanLevelMultiplier(); // 2.0
    int maxLevel = powerConfig.getMaxClanLevel(); // 15
    
    if (clanPower < basePower) return 1;
    
    double level = Math.floor(Math.log10(clanPower / basePower) * multiplier) + 1;
    return Math.min((int) level, maxLevel);
}
```

---

## 🛡️ KORUMA SİSTEMİ DETAYLARI {#koruma-sistemi}

### 4.1. Koruma Kuralları

#### 1. Onurlu Savaş Aralığı (Honorable Combat Range)

**Kural:**
```
Eğer targetPower < attackerPower × protectionThreshold ise
    → Saldırı YASAK
```

**Parametreler:**
- `protectionThreshold` = **0.5** (config'den)
- Hedef, saldıranın **%50'sinden düşükse** saldırı yapılamaz

**Örnek Senaryo:**
```
Saldıran: 10,000 puan
Hedef: 4,000 puan
Eşik: 10,000 × 0.5 = 5,000 puan

4,000 < 5,000 → Saldırı YASAK ❌

---

Saldıran: 10,000 puan
Hedef: 6,000 puan
Eşik: 10,000 × 0.5 = 5,000 puan

6,000 > 5,000 → Saldırı YAPILABİLİR ✅
```

#### 2. Acemi Koruması (Rookie Protection)

**Kural:**
```
Eğer targetPower < rookieThreshold (5,000) VE
   attackerPower > strongPlayerThreshold (10,000) VE
   target ilk saldıran DEĞİLSE
    → Saldırı YASAK
```

**Amaç:**
- Yeni oyuncuları güçlü oyunculardan korumak
- Adil oyun ortamı sağlamak

**Örnek Senaryo:**
```
Saldıran: 15,000 puan (Güçlü oyuncu)
Hedef: 3,000 puan (Acemi oyuncu)
Acemi Eşiği: 5,000 puan

3,000 < 5,000 VE 15,000 > 10,000 → Saldırı YASAK ❌

---

Saldıran: 8,000 puan (Orta seviye)
Hedef: 3,000 puan (Acemi oyuncu)

8,000 < 10,000 → Acemi koruması DEVRE DIŞI
Normal koruma kontrolü yapılır → Saldırı YAPILABİLİR ✅
```

#### 3. Klan Savaşı İstisnası (War Exception)

**Kural:**
```
Eğer ClanManager.areAtWar(attackerClan, targetClan) ise
    → Tüm koruma kuralları DEVRE DIŞI
    → Herkes herkese saldırabilir
```

**Amaç:**
- Klan savaşlarında stratejik saldırılar yapılabilir
- Güçlü klanlar zayıf klanlara saldırabilir (savaş stratejisi)

#### 4. Klan İçi Koruma (Clan Internal Protection)

**Kural:**
```
Eğer attacker ve target aynı klandaysa:
    → Daha katı eşik: clanProtectionThreshold (0.6)
    → Veya tamamen kapalı (config'den)
```

**Amaç:**
- Klan içi dostane rekabet
- Klan içi zorbalığı önlemek

**Örnek Senaryo:**
```
Aynı Klan: "Epic Warriors"

Saldıran: 10,000 puan
Hedef: 5,000 puan
Klan İçi Eşik: 10,000 × 0.6 = 6,000 puan

5,000 < 6,000 → Saldırı YASAK ❌

---

Saldıran: 10,000 puan
Hedef: 7,000 puan
Klan İçi Eşik: 10,000 × 0.6 = 6,000 puan

7,000 > 6,000 → Saldırı YAPILABİLİR ✅
```

### 4.2. Koruma Kontrol Fonksiyonu

```java
/**
 * Oyuncu saldırı yapabilir mi? (Tüm koruma kuralları)
 */
public boolean canAttackPlayer(Player attacker, Player target) {
    if (attacker == null || target == null) return false;
    if (attacker.equals(target)) return false; // Kendine saldıramaz
    
    // Güçleri al (cache'den)
    double attackerPower = getCachedPlayerPower(attacker);
    double targetPower = getCachedPlayerPower(target);
    
    // 1. Klan savaşı kontrolü (en yüksek öncelik)
    Clan attackerClan = clanManager.getClanByPlayer(attacker.getUniqueId());
    Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
    
    if (attackerClan != null && targetClan != null) {
        if (clanManager.areAtWar(attackerClan, targetClan)) {
            return true; // Savaşta herkes herkese saldırabilir
        }
    }
    
    // 2. Klan içi koruma
    if (attackerClan != null && attackerClan.equals(targetClan)) {
        double clanThreshold = attackerPower * powerConfig.getClanProtectionThreshold();
        if (targetPower < clanThreshold) {
            attacker.sendMessage("§cKlan içinde güçsüz üyelere saldıramazsın!");
            return false;
        }
    }
    
    // 3. Acemi koruması
    double rookieThreshold = powerConfig.getRookieThreshold(); // 5,000
    double strongPlayerThreshold = powerConfig.getStrongPlayerThreshold(); // 10,000
    
    if (targetPower < rookieThreshold && 
        attackerPower > strongPlayerThreshold) {
        attacker.sendMessage("§cBu oyuncu çok güçsüz! Onurlu bir savaş değil.");
        return false;
    }
    
    // 4. Normal koruma (Onurlu Savaş Aralığı)
    double protectionThreshold = attackerPower * powerConfig.getProtectionThreshold();
    if (targetPower < protectionThreshold) {
        attacker.sendMessage("§cBu oyuncu senin dengin değil! Saldırı yapılamaz.");
        return false;
    }
    
    return true; // Tüm kontroller geçti
}
```

### 4.3. Event Entegrasyonu

```java
/**
 * PvP Koruma Listener
 */
@EventHandler(priority = EventPriority.HIGH)
public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || 
        !(event.getEntity() instanceof Player)) {
        return; // Sadece oyuncu-oyuncu saldırıları
    }
    
    Player attacker = (Player) event.getDamager();
    Player target = (Player) event.getEntity();
    
    // Koruma kontrolü
    ClanPowerSystem powerSystem = Main.getInstance().getClanPowerSystem();
    if (powerSystem != null && !powerSystem.canAttackPlayer(attacker, target)) {
        event.setCancelled(true);
        // Mesaj zaten canAttackPlayer içinde gönderildi
    }
}
```

---

## 🔗 MEVCUT SİSTEMLERLE ENTEGRASYON {#entegrasyon}

### 5.1. Felaket Sistemi Entegrasyonu 🌪️

#### Mevcut Durum

**Şu anki sistem:**
- `PlayerPowerCalculator` + `ServerPowerCalculator` + `DisasterPowerConfig`
- Dinamik zorluk, **özellikle combat gücünü** baz alıyor
- Ağırlıklı sistem: Yapı %30, Eşya %40, Buff %15, Eğitim %10, Klan Tech %5

#### Entegrasyon Stratejisi

**Seçenek 1: Köprü Fonksiyon (Önerilen - Şimdilik)**

```java
/**
 * Felaket sistemi için uyumlu güç hesaplama
 * Mevcut PlayerPowerCalculator ile uyumlu
 */
public double getDisasterRelevantPower(Player player) {
    // Şimdilik mevcut sistemi kullan
    if (playerPowerCalculator != null) {
        return playerPowerCalculator.calculatePlayerPower(player);
    }
    
    // Gelecekte ClanPowerSystem'e geçiş için hazır
    // PlayerPowerProfile profile = calculatePlayerProfile(player);
    // return profile.getTotalCombatPower();
    
    return 0.0;
}
```

**Avantajlar:**
- ✅ Mevcut felaket kodu minimal değişir
- ✅ Gelecekte kolayca geçiş yapılabilir
- ✅ İki sistem paralel çalışabilir

**Seçenek 2: Tam Entegrasyon (Gelecek)**

```java
/**
 * ServerPowerCalculator güncellemesi
 */
public double calculateServerPower() {
    // Yeni sistemden combat power al
    double totalCombatPower = 0.0;
    int activePlayerCount = 0;
    
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.isOnline() && !player.isDead()) {
            PlayerPowerProfile profile = clanPowerSystem.calculatePlayerProfile(player);
            totalCombatPower += profile.getTotalCombatPower();
            activePlayerCount++;
        }
    }
    
    if (activePlayerCount == 0) return 0.0;
    
    double averagePower = totalCombatPower / activePlayerCount;
    double playerCountMultiplier = powerConfig.getPlayerCountMultiplier(activePlayerCount);
    
    return averagePower * playerCountMultiplier;
}
```

#### Önerilen Yaklaşım

**FAZ 1 (Şimdi):**
- Felaket sistemi mevcut `PlayerPowerCalculator`'ı kullanmaya devam eder
- `ClanPowerSystem` sadece PvP koruma ve klan seviyesi için kullanılır

**FAZ 2 (Gelecek):**
- Felaket sistemi `ClanPowerSystem`'in Combat Power'ını kullanır
- Daha tutarlı ve merkezi bir sistem

---

### 5.2. Klan Sistemi Entegrasyonu 🏛️

#### Klan Model Güncellemeleri

**Clan.java'ya eklenecekler:**

```java
public class Clan {
    // ... mevcut alanlar ...
    
    // Güç sistemi cache
    private double cachedPower = 0.0;
    private int cachedLevel = 1;
    private long lastPowerUpdate = 0;
    
    // Getters/Setters
    public double getCachedPower() { return cachedPower; }
    public void setCachedPower(double power) { this.cachedPower = power; }
    
    public int getCachedLevel() { return cachedLevel; }
    public void setCachedLevel(int level) { this.cachedLevel = level; }
}
```

**ÖNEMLİ:** Hesaplama mantığı **Clan sınıfında değil**, `ClanPowerSystem` içinde olacak!

#### Klan Seviyesine Bağlı Özellikler

**Önerilen Özellikler:**

| Klan Seviyesi | Max Bölge | Max Üye | Max Müttefik | Özel Özellik |
|---------------|-----------|---------|--------------|--------------|
| **1-3** | 1 | 10 | 0 | Temel klan |
| **4-6** | 3 | 15 | 1 | Gelişmiş klan |
| **7-9** | 5 | 20 | 2 | Güçlü klan |
| **10-12** | 7 | 25 | 3 | Efsanevi klan |
| **13-15** | 10 | 30 | 5 | Destansı klan |

**Kod Örneği:**

```java
/**
 * Klan seviyesine göre max bölge sayısı
 */
public int getMaxTerritories(Clan clan) {
    int level = clanPowerSystem.calculateClanLevel(clan);
    
    if (level <= 3) return 1;
    if (level <= 6) return 3;
    if (level <= 9) return 5;
    if (level <= 12) return 7;
    return 10; // Seviye 13+
}
```

#### Klan Güç Güncelleme Noktaları

**Otomatik Güncelleme:**
1. ✅ Yapı kuruldu/yıkıldı → `StructurePlaceEvent` / `StructureBreakEvent`
2. ✅ Üye eklendi/çıkarıldı → `ClanMemberJoinEvent` / `ClanMemberLeaveEvent`
3. ✅ Ritüel başarıyla tamamlandı → `RitualSuccessEvent`
4. ✅ Periyodik güncelleme → Her 30 dakikada bir async task

**Fonksiyon:**

```java
/**
 * Klan gücünü güncelle (event-based)
 */
public void updateClanPower(Clan clan) {
    if (clan == null) return;
    
    ClanPowerProfile profile = calculateClanProfile(clan);
    
    // Cache'e kaydet
    clan.setCachedPower(profile.getTotalClanPower());
    clan.setCachedLevel(profile.getClanLevel());
    clan.setLastPowerUpdate(System.currentTimeMillis());
    
    // Klan seviyesi değiştiyse bildirim gönder
    int oldLevel = clan.getCachedLevel(); // Önceki seviye
    if (profile.getClanLevel() > oldLevel) {
        broadcastClanLevelUp(clan, oldLevel, profile.getClanLevel());
    }
}
```

---

### 5.3. PvP / Oyuncu Koruma Entegrasyonu ⚔️

#### CombatListener Entegrasyonu

**Mevcut CombatListener'a eklenecek:**

```java
@EventHandler(priority = EventPriority.HIGH)
public void onPlayerAttack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player) || 
        !(event.getEntity() instanceof Player)) {
        return;
    }
    
    Player attacker = (Player) event.getDamager();
    Player target = (Player) event.getEntity();
    
    // Güç sistemi koruma kontrolü
    Main plugin = Main.getInstance();
    if (plugin != null && plugin.getClanPowerSystem() != null) {
        ClanPowerSystem powerSystem = plugin.getClanPowerSystem();
        
        if (!powerSystem.canAttackPlayer(attacker, target)) {
            event.setCancelled(true);
            return;
        }
    }
    
    // ... mevcut combat kodları ...
}
```

#### Özel Arena / Boss Sistemi

**Boss Arena'da koruma:**
- Boss savaşlarında koruma **devre dışı** olabilir (config'den)
- Özel PvP arenalarında koruma **devre dışı** olabilir

```java
/**
 * Özel arena kontrolü
 */
public boolean canAttackPlayer(Player attacker, Player target, boolean ignoreProtection) {
    if (ignoreProtection) {
        return true; // Arena'da koruma yok
    }
    
    return canAttackPlayer(attacker, target); // Normal koruma
}
```

---

### 5.4. Ritüel Sistemi Entegrasyonu 🔮

#### Ritüel Event'leri

**Ritüel başarıyla tamamlandığında:**

```java
/**
 * Ritüel Listener'da
 */
@EventHandler
public void onRitualComplete(RitualCompleteEvent event) {
    Clan clan = event.getClan();
    String ritualType = event.getRitualType();
    Map<String, Integer> usedResources = event.getUsedResources();
    Map<Material, Integer> usedBlocks = event.getUsedBlocks();
    
    // Güç sistemine bildir
    Main plugin = Main.getInstance();
    if (plugin != null && plugin.getClanPowerSystem() != null) {
        ClanPowerSystem powerSystem = plugin.getClanPowerSystem();
        powerSystem.onRitualSuccess(clan, ritualType, usedResources, usedBlocks);
    }
}
```

**ClanPowerSystem'de:**

```java
/**
 * Ritüel başarıyla tamamlandığında çağrılır
 */
public void onRitualSuccess(Clan clan, String ritualType, 
                           Map<String, Integer> usedResources,
                           Map<Material, Integer> usedBlocks) {
    if (clan == null) return;
    
    // Ritüel istatistiklerini güncelle
    ClanRitualStats stats = getOrCreateRitualStats(clan);
    RitualUsage usage = stats.getRitualUsages()
        .getOrDefault(ritualType, new RitualUsage());
    
    usage.setTotalUses(usage.getTotalUses() + 1);
    usage.setLastUsedTime(System.currentTimeMillis());
    
    // Kullanılan kaynakları ekle
    for (Map.Entry<String, Integer> entry : usedResources.entrySet()) {
        int current = usage.getResourcesUsed().getOrDefault(entry.getKey(), 0);
        usage.getResourcesUsed().put(entry.getKey(), current + entry.getValue());
    }
    
    // Kullanılan blokları snapshot'a ekle (opsiyonel)
    // Bu bloklar zaten klan arazisinde, async tarama yakalayacak
    
    // Cache'i temizle (güç yeniden hesaplanacak)
    clearClanCache(clan);
    
    // Klan gücünü güncelle
    updateClanPower(clan);
}
```

---

### 5.5. Diğer Sistemlerle Uyum 🔄

#### Buff Sistemi

**Buff gücü hesaplama:**

```java
/**
 * Buff gücü hesapla (felaket sistemiyle uyum için)
 */
private double calculateBuffPower(Player player) {
    double totalPower = 0.0;
    
    // PotionEffect buffları
    for (PotionEffect effect : player.getActivePotionEffects()) {
        double multiplier = powerConfig.getBuffMultiplier(effect.getType());
        int amplifier = effect.getAmplifier() + 1;
        totalPower += amplifier * powerConfig.getBuffBaseValue() * multiplier;
    }
    
    // BuffManager buffları (Klan bazlı)
    Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
    if (buffManager != null && clan != null) {
        if (buffManager.hasConquerorBuff(clan)) {
            totalPower += powerConfig.getConquerorBuffPower();
        }
        if (buffManager.hasHeroBuff(clan)) {
            totalPower += powerConfig.getHeroBuffPower();
        }
    }
    
    return totalPower;
}
```

#### Territory Sistemi

**Toprak limiti:**

```java
/**
 * Klan seviyesine göre max toprak sayısı
 */
public int getMaxTerritories(Clan clan) {
    int level = calculateClanLevel(clan);
    return powerConfig.getMaxTerritoriesByLevel(level);
}
```

#### Economy Sistemi

**Vergi sistemi:**

```java
/**
 * Klan seviyesine göre vergi oranı
 */
public double getTaxRate(Clan clan) {
    int level = calculateClanLevel(clan);
    // Yüksek seviye klanlar daha az vergi öder
    return powerConfig.getTaxRateByLevel(level);
}
```

---

## 🏗️ KOD MİMARİSİ VE TASARIM PRENSİPLERİ {#kod-mimarisi}

### 6.1. Sınıf Yapısı

#### Ana Sınıflar

```
me.mami.stratocraft.manager
├── StratocraftPowerSystem.java      # Ana güç hesaplama sistemi
├── StratocraftPowerConfig.java      # Config yönetimi
├── PlayerPowerProfile.java          # Oyuncu güç profili (data class)
├── ClanPowerProfile.java            # Klan güç profili (data class)
├── ClanRitualBlockSnapshot.java     # Ritüel blok snapshot (data class)
├── ClanRitualStats.java             # Ritüel istatistikleri (data class)
└── PowerSystemListener.java         # Event listener'lar
```

#### Interface'ler (Strategy Pattern)

```java
/**
 * Güç hesaplama stratejisi (gelecekte farklı algoritmalar için)
 */
public interface PowerCalculationStrategy {
    double calculatePower(Player player);
    double calculatePower(Clan clan);
}

/**
 * Seviye hesaplama stratejisi
 */
public interface LevelCalculationStrategy {
    int calculateLevel(double power);
}
```

### 6.2. Fonksiyon Ayrışması

#### Her Özellik İçin Ayrı Fonksiyon

```java
public class StratocraftPowerSystem {
    // ========== EŞYA GÜCÜ ==========
    private double calculateWeaponPower(Player player) { ... }
    private double calculateArmorPower(Player player) { ... }
    private double calculateSpecialItemPower(Player player) { ... }
    public double calculateGearPower(Player player) { ... }
    
    // ========== RİTÜEL GÜCÜ ==========
    public double calculateClanRitualBlockPower(Clan clan) { ... }
    public double calculateClanRitualResourcePower(Clan clan) { ... }
    private double getRitualBlockPower(Material material) { ... }
    private double getRitualResourcePower(String resourceId) { ... }
    
    // ========== YAPI GÜCÜ ==========
    public double calculateClanStructurePower(Clan clan) { ... }
    private double getStructurePower(int level) { ... }
    private double getStructureTypeMultiplier(Structure.Type type) { ... }
    
    // ========== USTALIK GÜCÜ ==========
    public double calculatePlayerTrainingMasteryPower(Player player) { ... }
    private double getMasteryPower(double masteryPercent) { ... }
    private double calculateMasteryPercent(int totalUses, String ritualId) { ... }
    
    // ========== BUFF GÜCÜ ==========
    private double calculateBuffPower(Player player) { ... }
    private double getBuffMultiplier(PotionEffectType type) { ... }
    
    // ========== TOPLAM GÜÇ ==========
    public PlayerPowerProfile calculatePlayerProfile(Player player) { ... }
    public ClanPowerProfile calculateClanProfile(Clan clan) { ... }
    
    // ========== SEVİYE HESAPLAMA ==========
    public int calculatePlayerLevel(double power) { ... }
    public int calculateClanLevel(double clanPower) { ... }
    
    // ========== KORUMA SİSTEMİ ==========
    public boolean canAttackPlayer(Player attacker, Player target) { ... }
    public boolean isRookie(Player player) { ... }
    public boolean isAtWar(Clan a, Clan b) { ... }
}
```

### 6.3. Config Yönetimi

#### Tüm Değerler Config'de

```yaml
stratocraft-power-system:
  # Eşya güç puanları
  gear-power:
    weapon:
      level-1: 60
      level-2: 150
      level-3: 400
      level-4: 900
      level-5: 1600
    armor:
      level-1: 40
      level-2: 100
      level-3: 250
      level-4: 600
      level-5: 1000
    armor-set-bonus: 1.1  # Tam set bonusu
  
  # Ritüel blok güç puanları
  ritual-blocks:
    iron: 8
    gold: 12
    diamond: 25
    obsidian: 30
    emerald: 35
    titanyum: 150
    default: 5
  
  # Ritüel kaynak güç puanları
  ritual-resources:
    iron: 5
    diamond: 10
    red-diamond: 18
    titanium: 15
    dark-matter: 50
    default: 3
  
  # Ustalık güç puanları
  mastery:
    base-power: 150
    exponent: 1.4
  
  # Yapı güç puanları
  structure-power:
    crystal-base: 500  # Klan kristali sabit bonus
    level-1: 100
    level-2: 250
    level-3: 500
    level-4: 1200
    level-5: 2000
  
  # Seviye sistemi
  level-system:
    player:
      base-power: 100
      switch-power: 10000  # Karekök -> Logaritmik geçiş noktası
      multiplier: 3.0
      max-level: 20
    clan:
      base-power: 500
      multiplier: 2.0
      max-level: 15
  
  # Koruma sistemi
  protection:
    threshold: 0.5              # Normal koruma eşiği
    clan-threshold: 0.6          # Klan içi koruma eşiği
    rookie-threshold: 5000       # Acemi eşiği
    strong-player-threshold: 10000 # Güçlü oyuncu eşiği
  
  # Güç ağırlıkları
  power-weights:
    combat: 0.6      # Combat Power ağırlığı
    progression: 0.4 # Progression Power ağırlığı
```

### 6.4. Temiz Kod Prensipleri

#### 1. Tek Sorumluluk Prensibi (SRP)

**Her sınıf tek bir sorumluluğa sahip:**

- `StratocraftPowerSystem`: Sadece güç hesaplama + cache + koruma
- `StratocraftPowerConfig`: Sadece config okuma
- `PlayerPowerProfile`: Sadece veri taşıma
- `PowerSystemListener`: Sadece event handling

#### 2. Açık/Kapalı Prensibi (OCP)

**Genişlemeye açık, değişime kapalı:**

```java
/**
 * Yeni güç kaynağı eklemek için:
 * 1. Yeni calculateXxxPower() fonksiyonu ekle
 * 2. Config'e değerleri ekle
 * 3. calculatePlayerProfile() içine bir satır ekle
 * 
 * Mevcut kod değişmez!
 */
```

#### 3. Bağımlılık Tersine Çevirme (DIP)

**Interface'ler üzerinden çalışma:**

```java
// Kötü: Direkt sınıf bağımlılığı
private TrainingManager trainingManager;

// İyi: Interface bağımlılığı (gelecekte)
private ITrainingManager trainingManager;
```

#### 4. DRY (Don't Repeat Yourself)

**Tekrarlanan kod yok:**

```java
// Kötü: Her yerde aynı cache kontrolü
if (cache.containsKey(id) && cacheTime < duration) { ... }

// İyi: Tek bir fonksiyon
private double getCachedPower(UUID id, Supplier<Double> calculator) { ... }
```

---

## ⚡ PERFORMANS OPTİMİZASYONU {#performans}

### 7.1. Cache Stratejisi

#### Cache Yapısı

```java
public class PowerCache {
    // Oyuncu cache
    private final Map<UUID, CachedPlayerPower> playerCache = new ConcurrentHashMap<>();
    
    // Klan cache
    private final Map<UUID, CachedClanPower> clanCache = new ConcurrentHashMap<>();
    
    // Cache süreleri
    private static final long PLAYER_CACHE_DURATION = 5000L;  // 5 saniye
    private static final long CLAN_CACHE_DURATION = 300000L; // 5 dakika
}

public class CachedPlayerPower {
    private double power;
    private int level;
    private long lastUpdate;
    private boolean needsUpdate; // Event-based güncelleme için
}
```

#### Cache Güncelleme Stratejisi

**1. Event-Based Güncelleme (Öncelikli):**

```java
// InventoryCloseEvent → Güç hesapla
@EventHandler
public void onInventoryClose(InventoryCloseEvent event) {
    if (event.getPlayer() instanceof Player) {
        Player player = (Player) event.getPlayer();
        // Güç hesapla ve cache'e kaydet
        calculateAndCachePlayerPower(player);
    }
}

// StructurePlaceEvent → Klan gücü güncelle
@EventHandler
public void onStructurePlace(StructurePlaceEvent event) {
    Clan clan = event.getClan();
    updateClanPower(clan);
}
```

**2. Periyodik Güncelleme (Yedek):**

```java
// Her 30 saniyede bir tüm oyuncuları güncelle (async)
@Async
public void periodicPowerUpdate() {
    for (Player player : Bukkit.getOnlinePlayers()) {
        if (player.isOnline() && !player.isDead()) {
            calculateAndCachePlayerPower(player);
        }
    }
}
```

**3. Lazy Güncelleme:**

```java
/**
 * Cache'den oku, yoksa hesapla
 */
public double getPlayerPower(Player player) {
    UUID id = player.getUniqueId();
    CachedPlayerPower cached = playerCache.get(id);
    
    if (cached != null && !cached.needsUpdate() && 
        System.currentTimeMillis() - cached.getLastUpdate() < PLAYER_CACHE_DURATION) {
        return cached.getPower(); // Cache'den dön
    }
    
    // Hesapla ve cache'e kaydet
    double power = calculatePlayerPower(player);
    playerCache.put(id, new CachedPlayerPower(power, System.currentTimeMillis()));
    return power;
}
```

### 7.2. Async İşlemler

#### Blok Taraması (Async)

```java
/**
 * Klan arazisi blok taraması (15 dakikada bir, async)
 */
@Async
public void scanClanTerritoryBlocks(Clan clan) {
    Territory territory = territoryManager.getTerritory(clan);
    if (territory == null) return;
    
    Map<Material, Integer> blockCounts = new HashMap<>();
    int scannedBlocks = 0;
    int maxBlocksPerTick = 1000; // Tick başına max blok
    
    // Chunk'ları async olarak tara
    for (Chunk chunk : territory.getChunks()) {
        if (!chunk.isLoaded()) continue;
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    Material material = block.getType();
                    
                    if (isRitualBlock(material)) {
                        blockCounts.put(material, 
                            blockCounts.getOrDefault(material, 0) + 1);
                    }
                    
                    scannedBlocks++;
                    if (scannedBlocks % maxBlocksPerTick == 0) {
                        // Her 1000 blokta bir kısa bekleme (sunucuyu yormamak için)
                        try {
                            Thread.sleep(10); // 10ms bekle
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
        }
    }
    
    // Snapshot'ı güncelle (main thread'de)
    Bukkit.getScheduler().runTask(plugin, () -> {
        updateBlockSnapshot(clan, blockCounts);
    });
}
```

### 7.3. Performans Metrikleri

#### Ölçülebilir Metrikler

```java
public class PowerSystemMetrics {
    private long totalCalculations = 0;
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long averageCalculationTime = 0;
    
    public double getCacheHitRate() {
        long total = cacheHits + cacheMisses;
        if (total == 0) return 0.0;
        return (double) cacheHits / total * 100.0;
    }
}
```

**Hedef Performans:**
- ✅ Cache hit rate: **%80+**
- ✅ Ortalama hesaplama süresi: **< 5ms**
- ✅ Async blok taraması: **< 1 saniye** (1000 blok/tick)

---

## 🧪 TEST SENARYOLARI VE ÖRNEKLER {#test-senaryolari}

### 8.1. Oyuncu Güç Senaryoları

#### Senaryo 1: Yeni Oyuncu

```
Oyuncu: "NewPlayer"
Eşya: Seviye 1 silah + 2 parça Seviye 1 zırh
Ustalık: Yok

Hesaplama:
- Silah: 60 puan
- Zırh: 2 × 40 = 80 puan
- Ustalık: 0 puan
- Toplam: 140 puan
- Seviye: 1 (sqrt(140/100) = 1.18 → 1)
```

#### Senaryo 2: Orta Seviye Oyuncu

```
Oyuncu: "MidPlayer"
Eşya: Seviye 3 silah + 4 parça Seviye 2 zırh
Ustalık: 2 ritüel %150 ustalık

Hesaplama:
- Silah: 400 puan
- Zırh: 4 × 100 = 400 puan (tam set bonusu: 400 × 1.1 = 440)
- Ustalık: 2 × 250 = 500 puan
- Toplam: 1,340 puan
- Seviye: 3 (sqrt(1340/100) = 3.66 → 3)
```

#### Senaryo 3: Elit Oyuncu

```
Oyuncu: "ElitePlayer"
Eşya: Seviye 5 silah + 4 parça Seviye 5 zırh
Ustalık: 5 ritüel %200-300 ustalık

Hesaplama:
- Silah: 1,600 puan
- Zırh: 4 × 1,000 = 4,000 puan (tam set bonusu: 4,000 × 1.1 = 4,400)
- Ustalık: ~2,000 puan (ortalama)
- Toplam: ~8,000 puan
- Seviye: 8 (sqrt(8000/100) = 8.94 → 8)
```

### 8.2. Klan Güç Senaryoları

#### Senaryo 1: Yeni Klan

```
Klan: "NewClan"
Üyeler: 3 oyuncu (ortalama 500 puan)
Yapılar: 2 Seviye 1 yapı + Klan Kristali
Ritüel: Yok

Hesaplama:
- Üye Gücü: 3 × 500 = 1,500 puan
- Yapı Gücü: 2 × 100 + 500 = 700 puan
- Ritüel: 0 puan
- Toplam: 2,200 puan
- Klan Seviyesi: 1 (log10(2200/500) × 2.0 = 0.64 → 1)
```

#### Senaryo 2: Güçlü Klan

```
Klan: "PowerClan"
Üyeler: 10 oyuncu (ortalama 5,000 puan)
Yapılar: 5 Seviye 3-4 yapı + Klan Kristali
Ritüel Bloklar: 10,000 puan
Ritüel Kaynaklar: 2,000 puan

Hesaplama:
- Üye Gücü: 10 × 5,000 = 50,000 puan
- Yapı Gücü: ~5,000 + 500 = 5,500 puan
- Ritüel Bloklar: 10,000 puan
- Ritüel Kaynaklar: 2,000 puan
- Toplam: 67,500 puan
- Klan Seviyesi: 4 (log10(67500/500) × 2.0 = 2.13 → 3, +1 = 4)
```

### 8.3. Koruma Sistemi Senaryoları

#### Senaryo 1: Normal Saldırı (İzinli)

```
Saldıran: 10,000 puan
Hedef: 6,000 puan
Eşik: 10,000 × 0.5 = 5,000 puan

6,000 > 5,000 → Saldırı YAPILABİLİR ✅
```

#### Senaryo 2: Güçsüz Hedef (Yasak)

```
Saldıran: 10,000 puan
Hedef: 4,000 puan
Eşik: 10,000 × 0.5 = 5,000 puan

4,000 < 5,000 → Saldırı YASAK ❌
Mesaj: "Bu oyuncu senin dengin değil!"
```

#### Senaryo 3: Acemi Koruması

```
Saldıran: 15,000 puan (Güçlü)
Hedef: 3,000 puan (Acemi)
Acemi Eşiği: 5,000
Güçlü Oyuncu Eşiği: 10,000

3,000 < 5,000 VE 15,000 > 10,000 → Saldırı YASAK ❌
Mesaj: "Bu oyuncu çok güçsüz! Onurlu bir savaş değil."
```

#### Senaryo 4: Klan Savaşı (İstisna)

```
Klan A: "Warriors" (10,000 puanlık oyuncu)
Klan B: "Defenders" (3,000 puanlık oyuncu)
Durum: Savaşta

Koruma: DEVRE DIŞI → Saldırı YAPILABİLİR ✅
(Savaşta stratejik saldırılar yapılabilir)
```

---

## 🚀 GELECEK GELİŞTİRMELER {#gelecek}

### 9.1. Kısa Vadeli (1-2 Hafta)

1. ✅ **Temel Sistem:** Eşya + Ustalık gücü
2. ✅ **PvP Koruma:** Temel koruma sistemi
3. ✅ **Seviye Sistemi:** Hibrit seviye algoritması
4. ✅ **Config Entegrasyonu:** Tüm değerler config'de

### 9.2. Orta Vadeli (1-2 Ay)

1. 🔄 **Ritüel Entegrasyonu:** Blok ve kaynak gücü
2. 🔄 **Klan Sistemi:** Seviye bazlı özellikler
3. 🔄 **Felaket Entegrasyonu:** Combat Power kullanımı
4. 🔄 **Async Optimizasyon:** Blok taraması

### 9.3. Uzun Vadeli (3+ Ay)

1. 🔮 **Gelişmiş Özellikler:**
   - Relic Power (kalıntı gücü)
   - Reputation Power (itibar gücü)
   - Achievement Power (başarı gücü)

2. 🔮 **Görselleştirme:**
   - Güç skorboard'u
   - Klan sıralaması
   - Seviye gösterimi

3. 🔮 **Ekonomi Entegrasyonu:**
   - Güç bazlı vergi
   - Güç bazlı maaş
   - Güç bazlı ticaret limitleri

---

## 📝 SONUÇ VE ÖNERİLER

### Sistem Avantajları

✅ **Modüler:** Her özellik ayrı fonksiyon  
✅ **Config Tabanlı:** Tüm değerler ayarlanabilir  
✅ **Performanslı:** Cache + async işlemler  
✅ **Genişletilebilir:** Yeni özellikler kolayca eklenebilir  
✅ **Temiz Kod:** SOLID prensipleri  
✅ **Uyumlu:** Mevcut sistemlerle entegre  

### Uygulama Önceliği

1. **FAZ 1:** Temel sistem (eşya + ustalık + seviye)
2. **FAZ 2:** PvP koruma sistemi
3. **FAZ 3:** Ritüel entegrasyonu
4. **FAZ 4:** Klan sistemi entegrasyonu
5. **FAZ 5:** Felaket sistemi entegrasyonu
6. **FAZ 6:** Performans optimizasyonu

---

**Rapor Hazırlayan:** AI Assistant  
**Tarih:** 2024  
**Versiyon:** 2.0 - Detaylı Tasarım  
**Durum:** Onay Bekliyor ✅

