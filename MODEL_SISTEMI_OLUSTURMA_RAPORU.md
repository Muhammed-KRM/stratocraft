# 📦 Model Sistemi Oluşturma Raporu

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Oluşturulan Modeller](#oluşturulan-modeller)
3. [Model Hiyerarşisi](#model-hiyerarşisi)
4. [Kod Güncellemeleri](#kod-güncellemeleri)
5. [Sonraki Adımlar](#sonraki-adımlar)

---

## 🎯 GENEL BAKIŞ

MVC benzeri bir model sistemi oluşturuldu. Tüm modeller `model` paketi altında kategorize edildi ve base modellerden inheritance ile türetildi.

**Yapı:**
```
model/
├── base/
│   └── BaseModel.java          # Tüm modellerin temel sınıfı
├── item/
│   ├── BaseItem.java          # Tüm item'ların temel sınıfı
│   ├── WeaponItem.java        # Silah item'ları
│   └── OreItem.java           # Maden item'ları
├── block/
│   ├── BaseBlock.java         # Tüm blokların temel sınıfı
│   ├── TrapCoreBlock.java     # Tuzak çekirdeği
│   └── StructureCoreBlock.java # Yapı çekirdeği
├── player/
│   └── PlayerData.java        # Oyuncu verileri
└── clan/
    └── ClanData.java          # Klan verileri (genişletilmiş)
```

---

## 📦 OLUŞTURULAN MODELLER

### 1. ✅ Base Model (`BaseModel.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/base/BaseModel.java`

**Özellikler:**
- `UUID id` - Benzersiz kimlik
- `long createdAt` - Oluşturulma zamanı
- `long lastUpdated` - Son güncelleme zamanı
- `updateTimestamp()` - Otomatik zaman damgası güncelleme

**Kullanım:**
```java
public abstract class BaseModel {
    protected UUID id;
    protected long createdAt;
    protected long lastUpdated;
    
    protected void updateTimestamp() {
        this.lastUpdated = System.currentTimeMillis();
    }
}
```

---

### 2. ✅ Item Modelleri

#### BaseItem (`BaseItem.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/item/BaseItem.java`

**Özellikler:**
- `String itemId` - Custom item ID (örn: "TITANIUM_INGOT")
- `Material material` - Bukkit Material
- `String displayName` - Görünen isim
- `List<String> lore` - Açıklama satırları
- `ItemStack itemStack` - Bukkit ItemStack referansı

**Kullanım:**
```java
public abstract class BaseItem extends BaseModel {
    protected String itemId;
    protected Material material;
    protected String displayName;
    protected List<String> lore;
    protected ItemStack itemStack;
}
```

#### WeaponItem (`WeaponItem.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/item/WeaponItem.java`

**Özellikler:**
- `double damage` - Hasar değeri
- `int durability` - Dayanıklılık
- `Map<Attribute, AttributeModifier> attributes` - Özel attribute'lar
- `List<String> specialAbilities` - Özel yetenekler

**Kullanım:**
```java
WeaponItem weapon = new WeaponItem("WAR_FAN", Material.FEATHER);
weapon.setDamage(10.0);
weapon.setDurability(100);
```

#### OreItem (`OreItem.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/item/OreItem.java`

**Özellikler:**
- `Material smeltResult` - Eritme sonucu
- `int rarity` - Nadirlik (1-10)
- `double spawnChance` - Spawn şansı (0.0-1.0)

**Kullanım:**
```java
OreItem ore = new OreItem("TITANIUM_ORE", Material.IRON_ORE);
ore.setSmeltResult(Material.IRON_INGOT);
ore.setRarity(7);
ore.setSpawnChance(0.05);
```

---

### 3. ✅ Blok Modelleri

#### BaseBlock (`BaseBlock.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/block/BaseBlock.java`

**Özellikler:**
- `Location location` - Blok konumu
- `Material material` - Blok tipi
- `UUID ownerId` - Sahip UUID
- `boolean isActive` - Aktif durumu

**Kullanım:**
```java
public abstract class BaseBlock extends BaseModel {
    protected Location location;
    protected Material material;
    protected UUID ownerId;
    protected boolean isActive;
}
```

#### TrapCoreBlock (`TrapCoreBlock.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/block/TrapCoreBlock.java`

**Özellikler:**
- `String trapType` - Tuzak tipi (TrapManager.TrapType.name())
- `int fuel` - Kalan patlama hakkı
- `List<Location> frameBlocks` - Magma Block çerçevesi
- `boolean isCovered` - Üstü kapatılmış mı?
- `UUID ownerClanId` - Sahip klan ID (nullable)

**Kullanım:**
```java
TrapCoreBlock trap = new TrapCoreBlock(location);
trap.setTrapType("HELL_TRAP");
trap.setFuel(10);
trap.setOwnerId(playerId);
trap.setActive(true);
```

#### StructureCoreBlock (`StructureCoreBlock.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/block/StructureCoreBlock.java`

**Özellikler:**
- `Structure.Type structureType` - Yapı tipi
- `int structureLevel` - Yapı seviyesi
- `boolean isActivated` - Aktifleştirilmiş mi?
- `UUID ownerClanId` - Sahip klan ID (nullable, personal yapılar için null)

**Kullanım:**
```java
StructureCoreBlock core = new StructureCoreBlock(location);
core.setStructureType(Structure.Type.CLAN_BANK);
core.setStructureLevel(1);
core.setActivated(true);
```

---

### 4. ✅ Oyuncu Modelleri

#### PlayerData (`PlayerData.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/player/PlayerData.java`

**Özellikler:**
- `UUID playerId` - Bukkit Player UUID
- `UUID clanId` - Klan ID (null = klansız)
- `Clan.Rank rank` - Klan içi rütbe (null = klansız)
- `boolean isInClan` - Klan durumu bool değişkeni
- `long lastActivity` - Son aktivite zamanı
- `UUID powerProfileId` - PlayerPowerProfile referansı (gelecekte)

**Kullanım:**
```java
PlayerData playerData = new PlayerData(playerId);
playerData.setClan(clanId, Clan.Rank.MEMBER);
playerData.updateActivity();
```

**Metodlar:**
- `setClan(UUID clanId, Clan.Rank rank)` - Klan üyeliği ayarla
- `leaveClan()` - Klandan ayrıl
- `updateActivity()` - Aktivite zamanını güncelle

---

### 5. ✅ Klan Modelleri

#### ClanData (`ClanData.java`)

**Konum:** `src/main/java/me/mami/stratocraft/model/clan/ClanData.java`

**Özellikler:**
- Mevcut `Clan.java`'nın tüm özellikleri
- **Yeni:** `double power` - Klan gücü
- **Yeni:** `int level` - Klan seviyesi
- **Yeni:** `int structureCount` - Yapı sayısı (cache)
- **Yeni:** `int offlineProtectionFuel` - Offline koruma yakıtı

**Kullanım:**
```java
ClanData clanData = new ClanData("Klan Adı", leaderId);
clanData.setPower(1000.0);
clanData.setLevel(5);
clanData.setOfflineProtectionFuel(7200); // 2 saat
```

**Dönüşüm Metodları:**
- `fromClan(Clan clan)` - Mevcut Clan.java'dan ClanData oluştur
- `toClan()` - ClanData'yı mevcut Clan.java'ya dönüştür

---

## 🌳 MODEL HİYERARŞİSİ

```
BaseModel (abstract)
├── BaseItem (abstract)
│   ├── WeaponItem
│   └── OreItem
├── BaseBlock (abstract)
│   ├── TrapCoreBlock
│   └── StructureCoreBlock
├── PlayerData
└── ClanData
```

**Inheritance Mantığı:**
- `BaseModel` → Tüm modellerin temel sınıfı (ID, zaman damgaları)
- `BaseItem` → Tüm item'ların temel sınıfı (itemId, material, displayName)
- `BaseBlock` → Tüm blokların temel sınıfı (location, material, ownerId)
- Özel modeller → Base modellerden türeyen, özel özellikler ekleyen modeller

---

## 🔧 KOD GÜNCELLEMELERİ

### Mevcut Kodları Bozmadan Entegrasyon

**Strateji:**
1. Yeni modeller eklendi, mevcut kodlar korundu
2. `ClanData` mevcut `Clan.java` ile uyumlu (dönüşüm metodları var)
3. Yeni modeller kullanıma hazır, mevcut kodlar çalışmaya devam ediyor

**Örnek Kullanım:**

```java
// Mevcut Clan.java kullanımı (değişmedi)
Clan clan = clanManager.getClanByPlayer(playerId);

// Yeni ClanData kullanımı (opsiyonel)
ClanData clanData = ClanData.fromClan(clan);
clanData.setPower(1000.0);
clanData.setLevel(5);

// Geri dönüşüm (mevcut sistemle uyumlu)
Clan updatedClan = clanData.toClan();
```

---

## 📝 SONRAKI ADIMLAR

### Yüksek Öncelik

1. **PlayerDataManager Oluşturma**
   - `PlayerDataManager.java` oluştur
   - `ClanManager` entegrasyonu
   - `ClanManager.addMember()` ve `removeMember()` metodlarında `PlayerData` güncelle

2. **TrapManager Güncelleme**
   - `TrapManager`'ı `TrapCoreBlock` modeli kullanacak şekilde güncelle
   - Mevcut `TrapData` inner class'ını `TrapCoreBlock` ile değiştir

3. **StructureCoreManager Güncelleme**
   - `StructureCoreManager`'ı `StructureCoreBlock` modeli kullanacak şekilde güncelle
   - Mevcut `inactiveCores` ve `activeStructures` Map'lerini `StructureCoreBlock` kullanacak şekilde güncelle

### Orta Öncelik

4. **ItemManager Güncelleme**
   - `ItemManager`'ı `BaseItem` ve türevleri kullanacak şekilde güncelle
   - Custom item'ları modellere kaydet

5. **DataManager Entegrasyonu**
   - Yeni modelleri `DataManager`'a entegre et
   - SQLite kayıt/okuma metodları ekle

6. **Dokümantasyon Güncelleme**
   - Model kullanım örnekleri
   - Migration rehberi

---

## 📊 ÖZET

### Oluşturulan Modeller

1. ✅ `BaseModel` - Temel model sınıfı
2. ✅ `BaseItem` - Item temel sınıfı
3. ✅ `WeaponItem` - Silah item modeli
4. ✅ `OreItem` - Maden item modeli
5. ✅ `BaseBlock` - Blok temel sınıfı
6. ✅ `TrapCoreBlock` - Tuzak çekirdeği modeli
7. ✅ `StructureCoreBlock` - Yapı çekirdeği modeli
8. ✅ `PlayerData` - Oyuncu veri modeli
9. ✅ `ClanData` - Klan veri modeli (genişletilmiş)

### Model Özellikleri

- ✅ **Inheritance:** Base modellerden türeyen modeller
- ✅ **Thread-Safe:** Collections.synchronizedMap/List/Set kullanımı
- ✅ **Timestamp:** Otomatik zaman damgası güncelleme
- ✅ **Geriye Uyumluluk:** Mevcut kodlarla uyumlu dönüşüm metodları

### Durum

- ✅ **Modeller Oluşturuldu** - Tüm base ve özel modeller hazır
- ⚠️ **Entegrasyon Bekliyor** - Manager'ların güncellenmesi gerekiyor
- ⚠️ **DataManager Entegrasyonu** - SQLite kayıt/okuma metodları eklenmeli

---

**Son Güncelleme:** 2024
**Durum:** ✅ **MODELLER OLUŞTURULDU** - Entegrasyon bekliyor

