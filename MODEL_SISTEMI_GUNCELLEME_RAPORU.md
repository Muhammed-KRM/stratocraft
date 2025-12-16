# Model Sistemi Güncelleme Raporu

## Genel Bakış

Bu dokümantasyon, Stratocraft plugin'inde yeni model sisteminin oluşturulması ve mevcut kodların bu yeni modellere göre güncellenmesi sürecini detaylandırmaktadır.

## Oluşturulan Modeller

### 1. Base Modeller

#### `BaseModel.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/base/BaseModel.java`
- **Amaç:** Tüm modeller için ortak özellikler sağlar
- **Özellikler:**
  - `id` (UUID): Her model için benzersiz kimlik
  - `createdAt` (long): Oluşturulma zamanı
  - `updatedAt` (long): Son güncelleme zamanı
  - `updateTimestamp()`: Otomatik zaman damgası güncelleme

### 2. Item Modelleri

#### `BaseItem.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/item/BaseItem.java`
- **Amaç:** Tüm özel item'lar için temel sınıf
- **Özellikler:**
  - `customId`: Item'ın özel kimliği
  - `displayName`: Görünen isim
  - `material`: Minecraft Material tipi
  - `lore`: Item açıklamaları
  - `isStackable`: Yığınlanabilir mi?
  - `maxStackSize`: Maksimum yığın boyutu
  - `toItemStack()`: ItemStack'e dönüştürme (abstract)

#### `WeaponItem.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/item/WeaponItem.java`
- **Amaç:** Silah item'ları için özel model
- **Özellikler:**
  - `attackDamage`: Saldırı hasarı
  - `attackSpeed`: Saldırı hızı
  - `durability`: Dayanıklılık
  - `AttributeModifier` desteği

#### `OreItem.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/item/OreItem.java`
- **Amaç:** Maden item'ları için özel model
- **Özellikler:**
  - `smeltResult`: Erime sonucu
  - `smeltResultAmount`: Erime miktarı
  - `rarity`: Nadirlik
  - `spawnChance`: Spawn şansı

### 3. Block Modelleri

#### `BaseBlock.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/block/BaseBlock.java`
- **Amaç:** Tüm özel bloklar için temel sınıf
- **Özellikler:**
  - `location`: Blok konumu
  - `material`: Blok Material tipi
  - `ownerId`: Sahip oyuncu UUID
  - `isActive`: Aktif mi?

#### `TrapCoreBlock.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/block/TrapCoreBlock.java`
- **Amaç:** Tuzak çekirdek blokları için özel model
- **Özellikler:**
  - `trapType`: Tuzak tipi (String)
  - `fuel`: Kalan yakıt miktarı
  - `frameBlocks`: Çerçeve blokları listesi
  - `isCovered`: Üstü kapatılmış mı?
  - `ownerClanId`: Sahip klan ID (nullable)
  - `addFuel()`, `consumeFuel()`: Yakıt yönetimi metodları

#### `StructureCoreBlock.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/block/StructureCoreBlock.java`
- **Amaç:** Yapı çekirdek blokları için özel model
- **Özellikler:**
  - `structureType`: Yapı tipi (Structure.Type)
  - `structureLevel`: Yapı seviyesi
  - `isActivated`: Aktifleştirilmiş mi?
  - `ownerClanId`: Sahip klan ID (nullable, personal yapılar için null)

### 4. Player Modelleri

#### `PlayerData.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/player/PlayerData.java`
- **Amaç:** Oyuncu verilerini merkezi olarak yönetir
- **Özellikler:**
  - `playerId`: Oyuncu UUID
  - `clanId`: Klan ID (null = klansız)
  - `rank`: Klan rütbesi (null = klansız)
  - `lastActivity`: Son aktivite zamanı
  - `personalPower`: Kişisel güç puanı
  - `playerLevel`: Oyuncu seviyesi
  - `joinClan()`, `leaveClan()`: Klan yönetimi helper metodları

### 5. Clan Modelleri

#### `ClanData.java`
- **Konum:** `src/main/java/me/mami/stratocraft/model/clan/ClanData.java`
- **Amaç:** Klan verilerini genişletilmiş şekilde yönetir (Clan sınıfından miras alır)
- **Özellikler:**
  - `clanPower`: Klanın toplam gücü
  - `clanLevel`: Klanın seviyesi
  - `offlineProtectionFuel`: Offline koruma yakıtı
  - `memberLastActivity`: Üyelerin son aktivite zamanları (Map)
  - `fromClan()`, `toClan()`: Dönüşüm metodları

## Kod Güncellemeleri

### 1. TrapManager.java

#### Değişiklikler:
- **Yeni Map'ler:**
  - `activeTrapCores`: `Map<Location, TrapCoreBlock>` (YENİ MODEL)
  - `inactiveTrapCores`: `Map<Location, TrapCoreBlock>` (YENİ MODEL, önceden `Map<Location, UUID>` idi)
  
- **Güncellenen Metodlar:**
  - `createTrap()`: Artık `TrapCoreBlock` oluşturuyor ve `activeTrapCores` map'ine ekliyor
  - `triggerTrapForTrapCore()`: Yeni `TrapCoreBlock` modeli ile çalışıyor
  - `executeTrapEffect()`: `TrapCoreBlock` parametresi alıyor
  - `loadTraps()`: Hem aktif hem inaktif tuzakları `TrapCoreBlock` olarak yüklüyor
  - `registerInactiveTrapCore()`: Artık `TrapCoreBlock` oluşturuyor
  - `addInactiveTrapCore()`: Yeni metod, `TrapCoreBlock` ekliyor
  - `getInactiveTrapCore()`: Yeni metod, `TrapCoreBlock` döndürüyor
  - `getInactiveTrapCoreOwner()`: GERİYE UYUMLULUK için, UUID döndürüyor

- **Geriye Uyumluluk:**
  - Eski `TrapData` inner class hala mevcut ve kullanılıyor (deprecated)
  - `activeTraps` map'i hala mevcut (deprecated)
  - Eski metodlar hala çalışıyor, ancak yeni kodlar `TrapCoreBlock` kullanıyor

#### Performans İyileştirmeleri:
- `updateCoverBlockMappingForTrapCore()`: Yeni model için cover block mapping'i
- `showTrapActivationParticles()`: Hem `TrapCoreBlock` hem `TrapData` için overload edilmiş

### 2. StructureCoreManager.java

#### Değişiklikler:
- **Yeni Map'ler:**
  - `inactiveStructureCoreBlocks`: `Map<Location, StructureCoreBlock>` (YENİ MODEL)
  - `activeStructureCoreBlocks`: `Map<Location, StructureCoreBlock>` (YENİ MODEL)
  
- **Güncellenen Metodlar:**
  - `addInactiveStructureCoreBlock()`: Yeni metod, `StructureCoreBlock` ekliyor
  - `activateStructureCoreBlock()`: Yeni metod, `StructureCoreBlock`'u aktifleştiriyor
  - `getActiveStructureCoreBlock()`: Yeni metod, aktif yapı çekirdeğini döndürüyor
  - `removeStructureCoreBlock()`: Yeni metod, yapı çekirdeğini kaldırıyor
  - `getAllInactiveStructureCoreBlocks()`: Yeni metod
  - `getAllActiveStructureCoreBlocks()`: Yeni metod

- **Geriye Uyumluluk:**
  - Eski `inactiveCores` (Map<Location, UUID>) hala mevcut
  - Eski `activeStructures` (Map<Location, Structure>) hala mevcut
  - Eski metodlar hala çalışıyor, ancak yeni kodlar `StructureCoreBlock` kullanıyor

### 3. StructureCoreListener.java

#### Değişiklikler:
- **Yeni Kullanım:**
  - `onStructureCorePlace()`: Artık `StructureCoreBlock` oluşturuyor ve `addInactiveStructureCoreBlock()` kullanıyor
  - `activateStructure()`: Artık `activateStructureCoreBlock()` kullanıyor ve `StructureCoreBlock` modelini güncelliyor

### 4. Main.java

#### Değişiklikler:
- **Yeni Manager:**
  - `PlayerDataManager`: Yeni oluşturulan manager, `onEnable()` içinde initialize ediliyor
  - `getPlayerDataManager()`: Getter metodu eklendi

## Model Sistemi Avantajları

### 1. Merkezi Veri Yönetimi
- Tüm oyuncu, klan, item ve blok verileri merkezi modellerde tutuluyor
- Veri tutarsızlığı riski azaltıldı

### 2. Tip Güvenliği
- Her model kendi tipine özgü özellikler içeriyor
- Compile-time hata kontrolü sağlanıyor

### 3. Genişletilebilirlik
- Yeni özellikler eklemek için sadece model sınıfına yeni alanlar eklemek yeterli
- Inheritance sayesinde ortak özellikler tekrar edilmiyor

### 4. Thread-Safety
- Modeller `ConcurrentHashMap` ile yönetiliyor
- Timestamp güncellemeleri otomatik yapılıyor

### 5. Veritabanı Entegrasyonu
- Modeller SQLite'a kaydedilmeye hazır
- `BaseModel`'deki `id`, `createdAt`, `updatedAt` alanları veritabanı şemasına uygun

## Geriye Uyumluluk

Tüm güncellemeler geriye uyumlu şekilde yapıldı:
- Eski kodlar hala çalışıyor
- Yeni kodlar yeni modelleri kullanıyor
- Eski ve yeni sistemler yan yana çalışabiliyor
- Kademeli geçiş mümkün

## Sonraki Adımlar

### Öncelikli Görevler:
1. **DataManager Güncellemesi:** SQLite'a kaydetme/yükleme işlemlerini yeni modellere göre güncelle
2. **ClanManager Güncellemesi:** `ClanData` ve `PlayerData` kullanımına geç
3. **TerritoryManager Güncellemesi:** Yeni modelleri kullan
4. **RitualInteractionListener Güncellemesi:** `PlayerData` kullanımına geç
5. **ClanBankSystem Güncellemesi:** `ClanData` kullanımına geç
6. **ClanMissionSystem Güncellemesi:** `ClanData` ve `PlayerData` kullanımına geç
7. **ClanActivitySystem Güncellemesi:** `ClanData` ve `PlayerData` kullanımına geç
8. **ItemManager Güncellemesi:** `BaseItem`, `WeaponItem`, `OreItem` kullanımına geç
9. **GUI Güncellemeleri:** Tüm GUI'ler yeni modelleri kullanmalı

### Uzun Vadeli Görevler:
1. Eski `TrapData` inner class'ını tamamen kaldır
2. Eski `inactiveCores` ve `activeStructures` map'lerini kaldır
3. Tüm deprecated metodları kaldır
4. Model sistemi için unit testler yaz

## Notlar

- Tüm model sınıfları `BaseModel`'den türetiliyor
- Her model kendi `updateTimestamp()` metodunu çağırarak otomatik zaman damgası güncellemesi yapıyor
- Modeller thread-safe değil, ancak manager'lar `ConcurrentHashMap` kullanarak thread-safety sağlıyor
- Model sınıfları sadece veri tutuyor, iş mantığı manager'larda

## Sonuç

Model sistemi başarıyla oluşturuldu ve temel entegrasyonlar yapıldı. Sistem genişletilebilir, tip güvenli ve geriye uyumlu bir yapıya sahip. Kalan güncellemeler kademeli olarak yapılabilir.
