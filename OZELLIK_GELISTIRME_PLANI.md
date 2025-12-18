# 🎯 ÖZELLİK GELİŞTİRME PLANI

## 📋 GENEL PRENSİPLER

### Modüler Yapı
- Her özellik kendi sınıfında
- Bağımlılıklar en aza indirilmiş
- Interface'ler ile gevşek bağlantı
- Tek sorumluluk prensibi (Single Responsibility)

### Güvenlik Kontrolleri
- Null check'ler her yerde
- Yetki kontrolleri (permission + rank)
- Thread-safe veri yapıları
- Cooldown sistemleri
- Anti-abuse kontrolleri

### Hata Yönetimi
- Try-catch blokları kritik yerlerde
- Graceful degradation (hata durumunda sistem çalışmaya devam)
- Logging (warning/error seviyeleri)
- Kullanıcıya anlaşılır mesajlar

### Performans
- Cache kullanımı (LRU cache)
- Async işlemler (dosya I/O)
- Lazy loading
- Batch işlemler

---

## 1. KLAN YAPILARI GUI MENÜSÜ

### 1.1. Amaç
Oyuncuların klan yapılarını görüntülemesi, yönetmesi ve seviye yükseltmesi için GUI menüsü.

### 1.2. Ana Fonksiyonlar

#### `ClanStructureMenu.java`
- `openMainMenu(Player player)` - Ana menü (yapı listesi)
- `openStructureDetailMenu(Player player, Structure structure)` - Yapı detay menüsü
- `openUpgradeMenu(Player player, Structure structure)` - Yükseltme menüsü
- `handleMenuClick(InventoryClickEvent event)` - Tıklama işlemleri

### 1.3. Destekleyici Fonksiyonlar

#### `StructureHelper.java` (Yeni utility sınıf)
- `getStructureDisplayName(Structure.Type type)` - Türkçe isim
- `getStructureDescription(Structure.Type type)` - Açıklama
- `getStructureIcon(Structure.Type type)` - GUI ikonu
- `getStructurePowerContribution(Structure structure, StratocraftPowerSystem powerSystem)` - Güç katkısı
- `getUpgradeCost(Structure structure, int targetLevel)` - Yükseltme maliyeti
- `canUpgrade(Structure structure, Clan clan, Player player)` - Yükseltme kontrolü

### 1.4. Entegrasyon Noktaları
- `ClanManager` - Klan yapılarını al
- `ClanRankSystem` - Yetki kontrolü
- `StratocraftPowerSystem` - Güç katkısı hesaplama
- `ClanMenu.java` - Ana klan menüsünden açılacak

### 1.5. Güvenlik Kontrolleri
- Klan üyeliği kontrolü
- Rütbe kontrolü (yükseltme için LEADER/GENERAL)
- Yapı sahipliği kontrolü
- Cooldown (yükseltme işlemleri için)

### 1.6. Hata Senaryoları
- Klan yok → Mesaj göster, menüyü kapat
- Yapı yok → Ana menüye dön
- Yetersiz kaynak → Yükseltme iptal, mesaj göster
- Yetki yok → Mesaj göster, işlemi iptal

---

## 2. İTTİFAK GUI MENÜSÜ

### 2.1. Amaç
Oyuncuların ittifakları görüntülemesi, yönetmesi ve yeni ittifak kurması için GUI menüsü.

### 2.2. Ana Fonksiyonlar

#### `AllianceMenu.java`
- `openMainMenu(Player player)` - Ana menü (aktif ittifaklar)
- `openAllianceDetailMenu(Player player, Alliance alliance)` - İttifak detay menüsü
- `openCreateAllianceMenu(Player player)` - İttifak kurma menüsü
- `handleMenuClick(InventoryClickEvent event)` - Tıklama işlemleri

### 2.3. Destekleyici Fonksiyonlar

#### `AllianceHelper.java` (Yeni utility sınıf)
- `getAllianceTypeDisplayName(Alliance.Type type)` - Türkçe isim
- `getAllianceTypeDescription(Alliance.Type type)` - Açıklama
- `getAllianceBonuses(Alliance alliance)` - Bonus listesi
- `getRemainingTime(Alliance alliance)` - Kalan süre
- `canCreateAlliance(Clan clan1, Clan clan2, Player player)` - İttifak kurma kontrolü

### 2.4. Entegrasyon Noktaları
- `AllianceManager` - İttifak yönetimi
- `ClanManager` - Klan bilgileri
- `ClanRankSystem` - Yetki kontrolü (MANAGE_ALLIANCE)
- `RitualInteractionListener` - Fiziksel ritüel entegrasyonu

### 2.5. Güvenlik Kontrolleri
- Klan liderliği kontrolü
- Cooldown kontrolü (ritüel için)
- İttifak limiti kontrolü
- Aynı klan kontrolü

### 2.6. Hata Senaryoları
- Klan yok → Mesaj göster
- İttifak yok → Ana menüye dön
- Cooldown aktif → Mesaj göster
- Yetki yok → Mesaj göster

---

## 3. KERVAN SİSTEMİ TETİKLEYİCİSİ

### 3.1. Amaç
Oyuncuların kervan oluşturması için GUI menüsü veya fiziksel ritüel.

### 3.2. Ana Fonksiyonlar

#### `CaravanMenu.java` (Yeni GUI menüsü)
- `openMainMenu(Player player)` - Ana menü (aktif kervanlar)
- `openCreateCaravanMenu(Player player)` - Kervan oluşturma menüsü
- `openCaravanDetailMenu(Player player, Entity caravan)` - Kervan detay menüsü
- `handleMenuClick(InventoryClickEvent event)` - Tıklama işlemleri

#### `CaravanRitualListener.java` (Alternatif: Fiziksel ritüel)
- `onCaravanRitual(PlayerInteractEvent event)` - Ritüel tetikleme
- `checkCaravanRitualPattern(Location location)` - Pattern kontrolü

### 3.3. Destekleyici Fonksiyonlar

#### `CaravanHelper.java` (Yeni utility sınıf)
- `calculateCargoValue(List<ItemStack> cargo)` - Yük değeri
- `validateCaravanRoute(Location start, Location end)` - Rota kontrolü
- `getCaravanStatus(Entity caravan)` - Durum bilgisi
- `getEstimatedArrivalTime(Location start, Location end)` - Tahmini varış süresi

### 3.4. Entegrasyon Noktaları
- `CaravanManager` - Kervan yönetimi
- `ClanManager` - Klan bilgileri
- `TerritoryManager` - Bölge kontrolü
- `GameBalanceConfig` - Anti-abuse ayarları

### 3.5. Güvenlik Kontrolleri
- Mesafe kontrolü (min distance)
- Yük değeri kontrolü (min value)
- Yük miktarı kontrolü (min stacks)
- Cooldown kontrolü
- Dünya kontrolü (aynı dünya)

### 3.6. Hata Senaryoları
- Yetersiz yük → Mesaj göster
- Çok kısa rota → Mesaj göster
- Cooldown aktif → Mesaj göster
- Farklı dünya → Mesaj göster

---

## 4. YAPI SEVİYE YÜKSELTME SİSTEMİ

### 4.1. Amaç
Yapıların seviyesini yükseltme sistemi.

### 4.2. Ana Fonksiyonlar

#### `StructureUpgradeSystem.java` (Yeni manager)
- `canUpgrade(Structure structure, Clan clan, Player player)` - Yükseltme kontrolü
- `calculateUpgradeCost(Structure structure, int targetLevel)` - Maliyet hesaplama
- `upgradeStructure(Structure structure, Clan clan, Player player)` - Yükseltme işlemi
- `getMaxLevel(Structure.Type type)` - Maksimum seviye

### 4.3. Destekleyici Fonksiyonlar

#### `StructureUpgradeHelper.java` (Yeni utility sınıf)
- `getUpgradeMaterials(Structure.Type type, int currentLevel, int targetLevel)` - Gerekli malzemeler
- `validateUpgradeLocation(Structure structure)` - Konum kontrolü
- `applyUpgradeEffects(Structure structure, Player player)` - Efektler

### 4.4. Entegrasyon Noktaları
- `Structure.java` - Yapı modeli
- `Clan.java` - Klan yapıları
- `ClanStructureMenu.java` - GUI menüsü
- `StructureListener.java` - Yapı listener'ı

### 4.5. Güvenlik Kontrolleri
- Seviye limiti kontrolü (max level)
- Maliyet kontrolü
- Yetki kontrolü (LEADER/GENERAL)
- Yapı sahipliği kontrolü
- Konum kontrolü (yapı hala var mı?)

### 4.6. Hata Senaryoları
- Maksimum seviye → Mesaj göster
- Yetersiz kaynak → Mesaj göster
- Yetki yok → Mesaj göster
- Yapı yok → Mesaj göster

---

## 5. BOSS FAZ SİSTEMİ TAMAMLAMA

### 5.1. Amaç
Boss faz geçişlerini tamamlama ve zayıf nokta sistemini düzeltme.

### 5.2. Ana Fonksiyonlar

#### `BossManager.java` (Güncelleme)
- `checkPhaseTransition(Boss boss)` - Faz geçişi kontrolü
- `transitionToPhase(Boss boss, BossPhase newPhase)` - Faz geçişi
- `updateWeakPoints(Boss boss)` - Zayıf nokta güncelleme
- `applyPhaseEffects(Boss boss, BossPhase phase)` - Faz efektleri

### 5.3. Destekleyici Fonksiyonlar

#### `BossPhaseHelper.java` (Yeni utility sınıf)
- `getPhaseHealthThreshold(BossPhase phase)` - Sağlık eşiği
- `getPhaseAbilities(BossPhase phase)` - Faz yetenekleri
- `getWeakPointLocations(Boss boss, BossPhase phase)` - Zayıf nokta konumları

### 5.4. Entegrasyon Noktaları
- `BossManager` - Boss yönetimi
- `BossTask` - Boss görevleri
- `NewBossArenaManager` - Arena transformasyonu
- `DisasterManager` - Felaket entegrasyonu

### 5.5. Güvenlik Kontrolleri
- Boss null kontrolü
- Faz geçişi kontrolü (sıralı)
- Arena kontrolü
- Thread-safe işlemler

### 5.6. Hata Senaryoları
- Boss yok → Log, işlemi iptal
- Faz geçişi hatası → Log, mevcut fazda kal
- Arena yok → Log, işlemi iptal

---

## 6. EĞİTME/ÜREME GUI MENÜLERİ

### 6.1. Amaç
Eğitilmiş canlıları ve üreme sistemini yönetmek için GUI menüleri.

### 6.2. Ana Fonksiyonlar

#### `TamingMenu.java`
- `openMainMenu(Player player)` - Ana menü (eğitilmiş canlılar)
- `openCreatureDetailMenu(Player player, Entity creature)` - Canlı detay menüsü
- `handleMenuClick(InventoryClickEvent event)` - Tıklama işlemleri

#### `BreedingMenu.java`
- `openMainMenu(Player player)` - Ana menü (üreme çiftleri)
- `openBreedingPairMenu(Player player, Entity parent1, Entity parent2)` - Üreme çifti menüsü
- `handleMenuClick(InventoryClickEvent event)` - Tıklama işlemleri

### 6.3. Destekleyici Fonksiyonlar

#### `TamingHelper.java` (Yeni utility sınıf)
- `getTamedCreatures(Player player)` - Eğitilmiş canlılar
- `getCreatureInfo(Entity creature)` - Canlı bilgileri
- `canBreed(Entity creature1, Entity creature2)` - Üreme kontrolü

### 6.4. Entegrasyon Noktaları
- `TamingManager` - Eğitme yönetimi
- `BreedingManager` - Üreme yönetimi
- `ClanManager` - Klan bilgileri (klan canlıları)

### 6.5. Güvenlik Kontrolleri
- Canlı sahipliği kontrolü
- Eğitme durumu kontrolü
- Üreme cooldown kontrolü
- Klan üyeliği kontrolü

### 6.6. Hata Senaryoları
- Canlı yok → Mesaj göster
- Sahiplik yok → Mesaj göster
- Cooldown aktif → Mesaj göster

---

## 7. ZORLUK SİSTEMİ ENTEGRASYONU

### 7.1. Amaç
Zorluk sistemini boss ve mob spawn sistemlerine entegre etme.

### 7.2. Ana Fonksiyonler

#### `DifficultyManager.java` (Güncelleme)
- `calculateBossDifficulty(Location location, List<Player> nearbyPlayers)` - Boss zorluğu
- `calculateMobDifficulty(Location location, List<Player> nearbyPlayers)` - Mob zorluğu
- `applyDifficultyToBoss(Boss boss, double difficulty)` - Boss'a uygula
- `applyDifficultyToMob(Entity mob, double difficulty)` - Mob'a uygula

### 7.3. Destekleyici Fonksiyonlar

#### `DifficultyHelper.java` (Yeni utility sınıf)
- `getNearbyPlayers(Location location, double radius)` - Yakındaki oyuncular
- `calculateAveragePower(List<Player> players)` - Ortalama güç
- `scaleBossStats(Boss boss, double multiplier)` - Boss istatistikleri ölçekle

### 7.4. Entegrasyon Noktaları
- `BossManager` - Boss spawn
- `MobManager` - Mob spawn
- `DisasterManager` - Felaket sistemi (zaten entegre)
- `StratocraftPowerSystem` - Güç hesaplama

### 7.5. Güvenlik Kontrolleri
- Null kontrolü
- Zorluk limitleri (min/max)
- Thread-safe işlemler
- Performans kontrolü (çok fazla oyuncu)

### 7.6. Hata Senaryoları
- Boss/Mob yok → Log, işlemi iptal
- Zorluk hesaplama hatası → Varsayılan değer kullan
- Performans sorunu → Cache kullan

---

## 📐 KOD YAPISI

### Dosya Organizasyonu
```
src/main/java/me/mami/stratocraft/
├── gui/
│   ├── ClanStructureMenu.java (YENİ)
│   ├── AllianceMenu.java (YENİ)
│   ├── CaravanMenu.java (YENİ)
│   ├── TamingMenu.java (YENİ)
│   └── BreedingMenu.java (YENİ)
├── manager/
│   ├── StructureUpgradeSystem.java (YENİ)
│   └── DifficultyManager.java (GÜNCELLEME)
├── util/
│   ├── StructureHelper.java (YENİ)
│   ├── AllianceHelper.java (YENİ)
│   ├── CaravanHelper.java (YENİ)
│   ├── TamingHelper.java (YENİ)
│   ├── StructureUpgradeHelper.java (YENİ)
│   ├── BossPhaseHelper.java (YENİ)
│   └── DifficultyHelper.java (YENİ)
└── listener/
    └── CaravanRitualListener.java (YENİ - Opsiyonel)
```

### Bağımlılık Grafi
```
ClanStructureMenu → StructureHelper → ClanManager, StratocraftPowerSystem
AllianceMenu → AllianceHelper → AllianceManager, ClanManager
CaravanMenu → CaravanHelper → CaravanManager, ClanManager
TamingMenu → TamingHelper → TamingManager
BreedingMenu → TamingHelper → BreedingManager
StructureUpgradeSystem → StructureUpgradeHelper → Structure, Clan
BossManager → BossPhaseHelper → Boss, BossPhase
DifficultyManager → DifficultyHelper → BossManager, MobManager
```

---

## 🔒 GÜVENLİK KONTROLLERİ (GENEL)

### Her İşlemde Kontrol Edilecekler
1. **Null Kontrolleri**
   - Player null check
   - Clan null check
   - Structure/Alliance/Caravan null check
   - Manager null check

2. **Yetki Kontrolleri**
   - Klan üyeliği
   - Rütbe kontrolü (LEADER/GENERAL)
   - Permission kontrolü

3. **Cooldown Kontrolleri**
   - İşlem cooldown'u
   - Ritüel cooldown'u

4. **Anti-Abuse Kontrolleri**
   - Mesafe kontrolü
   - Değer kontrolü
   - Miktar kontrolü

5. **Thread-Safety**
   - ConcurrentHashMap kullanımı
   - Synchronized bloklar (gerekirse)
   - Atomic işlemler

---

## 🐛 HATA YÖNETİMİ

### Logging Seviyeleri
- **INFO**: Normal işlemler (yapı yükseltme, ittifak kurma)
- **WARNING**: Beklenmeyen durumlar (null check başarısız, yetki yok)
- **SEVERE**: Kritik hatalar (exception, sistem hatası)

### Kullanıcı Mesajları
- Türkçe, anlaşılır mesajlar
- Renk kodları (§a başarı, §c hata, §e bilgi)
- Detaylı açıklamalar (neden başarısız oldu)

### Graceful Degradation
- Hata durumunda sistem çalışmaya devam etmeli
- Fallback mekanizmaları (cache, varsayılan değerler)
- Kullanıcıya bilgi ver, işlemi iptal et

---

## ⚡ PERFORMANS OPTİMİZASYONU

### Cache Kullanımı
- LRU Cache (son kullanılan veriler)
- Yapı listesi cache
- İttifak listesi cache

### Async İşlemler
- Dosya I/O (DataManager)
- Uzun hesaplamalar (güç hesaplama)

### Lazy Loading
- Menü açıldığında veri yükleme
- İhtiyaç duyulduğunda hesaplama

### Batch İşlemler
- Toplu yapı güncelleme
- Toplu ittifak kontrolü

---

## ✅ TEST SENARYOLARI

### Her Özellik İçin
1. Normal kullanım (başarılı)
2. Yetki yok (hata mesajı)
3. Yetersiz kaynak (hata mesajı)
4. Null durumlar (graceful handling)
5. Cooldown aktif (hata mesajı)
6. Thread-safety (çoklu oyuncu)

---

## 📝 UYGULAMA SIRASI

1. **Klan Yapıları GUI** (En çok kullanılacak)
2. **Yapı Seviye Yükseltme** (Yapılar GUI ile birlikte)
3. **İttifak GUI** (Sosyal özellik)
4. **Kervan Tetikleyicisi** (Ticaret için önemli)
5. **Boss Faz Sistemi** (Oyun içeriği)
6. **Eğitme/Üreme GUI** (Daha az kullanılır)
7. **Zorluk Entegrasyonu** (Sistem iyileştirmesi)

---

**Plan Hazırlayan:** AI Assistant  
**Tarih:** 2024  
**Versiyon:** 1.0













