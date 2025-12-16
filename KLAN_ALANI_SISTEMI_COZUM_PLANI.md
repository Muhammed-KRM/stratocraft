# Klan Alanı Sistemi Çözüm Planı

## 📋 İÇİNDEKİLER

1. [Tespit Edilen Sorunlar](#tespit-edilen-sorunlar)
2. [Çözüm Mimarisi](#çözüm-mimarisi)
3. [Yeni Modeller](#yeni-modeller)
4. [Config Yapısı](#config-yapısı)
5. [Kod Değişiklikleri](#kod-değişiklikleri)
6. [GUI Değişiklikleri](#gui-değişiklikleri)
7. [Admin Komutları](#admin-komutları)
8. [Main.java Güncellemeleri](#mainjava-güncellemeleri)

---

## 🐛 TESPİT EDİLEN SORUNLAR

### 1. ❌ KRİTİK: Klan Alanı Sınırları Çitlere Bağlı

**Sorun:**
- Çitler kırıldığında klan alanı görünmüyor
- `Territory` modeli sadece `center` ve `radius` tutuyor
- Çit lokasyonları kaydedilmiyor
- Sınır koordinatları hesaplanmıyor

**Mevcut Kod:**
```java
// Territory.java
public class Territory {
    private final Location center;
    private int radius = 50;
    // ❌ Çit lokasyonları yok
    // ❌ Sınır koordinatları yok
    // ❌ Min/Max Y koordinatları yok
}
```

**Sonuç:**
- Çitler kırıldığında sınırlar kayboluyor
- Partikül efekti gösterilemiyor
- Alan genişletme/küçültme çalışmıyor

---

### 2. ❌ KRİTİK: Klan Çiti vs Normal Çit Ayrımı Yok

**Sorun:**
- Şu an sadece `Material.OAK_FENCE` kontrolü var
- Her `OAK_FENCE` klan çiti olarak kabul ediliyor
- Klan çiti item'ı var ama yerleştirildiğinde NBT kayboluyor
- Normal çitler de klan çiti olarak algılanıyor

**Mevcut Kod:**
```java
// TerritoryListener.java:237-238
if (block.getType() != Material.OAK_FENCE) {
    return; // ❌ Sadece Material kontrolü
}
```

**Sonuç:**
- Normal çitler klan alanı oluşturabiliyor
- Klan çiti kontrolü yapılamıyor

---

### 3. ❌ KRİTİK: Klan Kristali Kontrolü Eksik

**Sorun:**
- Her `EnderCrystal` klan kristali olarak algılanabilir
- `ItemManager.isClanItem()` kontrolü var ama entity kontrolü yok
- Metadata veya özel işaretleme yok

**Mevcut Kod:**
```java
// TerritoryListener.java:325
if (!ItemManager.isClanItem(event.getItem(), "CRYSTAL")) return;
// ✅ Item kontrolü var ama entity kontrolü yok
```

**Sonuç:**
- Normal EnderCrystal'lar klan kristali olarak algılanabilir

---

### 4. ❌ KRİTİK: Klan Alanı Genişletme/Küçültme Sistemi Yok

**Sorun:**
- Otomatik genişletme kodu var ama çalışmıyor
- Manuel genişletme/küçültme menüsü yok
- `CLAN_MANAGEMENT_CENTER` yapısı var ama menü yok

**Mevcut Kod:**
```java
// TerritoryListener.java:234-288
@EventHandler
public void onFencePlace(BlockPlaceEvent event) {
    // ❌ Otomatik genişletme var ama çalışmıyor
    // ❌ Manuel kontrol yok
}
```

**Sonuç:**
- Oyuncular alanı genişletemiyor/küçültemiyor
- Sadece admin komutu ile manuel genişletme var

---

### 5. ❌ KRİTİK: Sınır Görselleştirme Eksik

**Sorun:**
- Partikül sistemi var ama sadece radius bazlı
- Çit lokasyonlarına göre partikül yok
- Sürekli çalışan bir task yok

**Mevcut Kod:**
```java
// TerritoryListener.java:497-593
@EventHandler
public void onPlayerMove(PlayerMoveEvent event) {
    // ✅ Partikül var ama sadece radius bazlı
    // ❌ Çit lokasyonlarına göre yok
}
```

**Sonuç:**
- Çitler kırıldığında sınırlar görünmüyor
- Sadece radius bazlı görselleştirme var

---

### 6. ⚠️ Klan Alanı Y Yüksekliği Sorunu

**Sorun:**
- `Territory` modeli Y koordinatlarını tutmuyor
- Gökyüzüne 150 blok, yer altına 50 blok kontrolü yok
- Çit yüksekliği yerden yere değişiyorsa en yüksek/en alçak çit kontrolü yok

**Mevcut Kod:**
```java
// Territory.java
// ❌ MinY, MaxY yok
// ❌ Yükseklik kontrolü yok
```

**Sonuç:**
- Klan alanı sadece X-Z düzleminde çalışıyor
- Y ekseni kontrolü yok

---

## 🏗️ ÇÖZÜM MİMARİSİ

### 1. Yeni Territory Modeli

**Yeni Model:** `TerritoryData.java` (Territory'yi genişletir)

**Özellikler:**
- Çit lokasyonları listesi (`List<Location>`)
- Sınır koordinatları (`List<Location>` - hesaplanmış)
- MinY, MaxY koordinatları
- Çit yükseklik analizi
- Sınır hesaplama algoritması

### 2. Klan Çiti Metadata Sistemi

**Çözüm:**
- Klan çiti yerleştirildiğinde metadata ekle
- `BlockPlaceEvent`'te kontrol et
- Metadata kalıcı olmadığı için `TerritoryData`'da kaydet

### 3. Klan Kristali Metadata Sistemi

**Çözüm:**
- Klan kristali yerleştirildiğinde metadata ekle
- `Entity` metadata ile işaretle
- `findClanByCrystal()` metodunda metadata kontrolü

### 4. Sınır Görselleştirme Sistemi

**Çözüm:**
- Sürekli çalışan bir task (`TerritoryBoundaryParticleTask`)
- Her klan üyesi için sınır partikülleri
- Çit lokasyonlarına göre partikül çizgisi

### 5. Klan Alanı Genişletme/Küçültme Sistemi

**Çözüm:**
- `CLAN_MANAGEMENT_CENTER` yapısına sağ tıklayınca menü aç
- "Klan Alanı Genişletme" ve "Küçültme" butonları
- Çit kontrolü yap, alan hesapla, genişlet/küçült

---

## 📦 YENİ MODELLER

### 1. TerritoryData.java

**Konum:** `src/main/java/me/mami/stratocraft/model/territory/TerritoryData.java`

**Özellikler:**
```java
public class TerritoryData extends BaseModel {
    private UUID clanId;
    private Location center;
    private int radius; // Geriye uyumluluk için
    
    // YENİ ÖZELLİKLER
    private List<Location> fenceLocations; // Çit lokasyonları
    private List<Location> boundaryCoordinates; // Hesaplanmış sınır koordinatları
    private int minY; // En alçak çit Y koordinatı
    private int maxY; // En yüksek çit Y koordinatı
    private int skyHeight; // Gökyüzüne yükseklik (config'den)
    private int groundDepth; // Yer altına derinlik (config'den)
    private long lastBoundaryUpdate; // Son sınır güncelleme zamanı
    
    // Metodlar
    public void addFenceLocation(Location loc);
    public void removeFenceLocation(Location loc);
    public void calculateBoundaries(); // Sınır koordinatlarını hesapla
    public void updateYBounds(); // MinY, MaxY güncelle
    public boolean isInsideTerritory(Location loc); // Konum kontrolü
    public List<Location> getBoundaryLine(); // Partikül için sınır çizgisi
}
```

### 2. ClanFenceBlock.java

**Konum:** `src/main/java/me/mami/stratocraft/model/block/ClanFenceBlock.java`

**Özellikler:**
```java
public class ClanFenceBlock extends BaseBlock {
    private UUID ownerClanId; // Hangi klana ait
    private boolean isBoundaryFence; // Sınır çiti mi?
    private int fenceIndex; // Çit sırası (sınır hesaplama için)
    
    public ClanFenceBlock(Location location, UUID ownerClanId) {
        super(location, Material.OAK_FENCE);
        this.ownerClanId = ownerClanId;
        this.isBoundaryFence = false;
    }
}
```

---

## ⚙️ CONFIG YAPISI

### config.yml Eklentileri

**Konum:** `src/main/resources/config.yml` (clan bölümüne eklenecek)

```yaml
# Klan Ayarları
clan:
  # ... mevcut ayarlar ...
  
  # Klan Alanı Ayarları (YENİ)
  territory:
    # Yükseklik Ayarları
    sky-height: 150  # Gökyüzüne yükseklik (blok)
    ground-depth: 50  # Yer altına derinlik (blok)
    
    # Sınır Görselleştirme
    boundary-particle:
      enabled: true
      type: REDSTONE  # Partikül tipi (REDSTONE, END_ROD, TOTEM, vb.)
      color: GREEN  # Partikül rengi (REDSTONE için: RED, GREEN, BLUE, YELLOW, vb.)
      density: 0.5  # Partikül yoğunluğu (0.0-1.0)
      update-interval: 20  # Güncelleme aralığı (tick) - 20 = 1 saniye
      visible-distance: 100  # Görünür mesafe (blok)
      particle-spacing: 2.0  # Partikül arası mesafe (blok)
    
    # Alan Genişletme/Küçültme
    expansion:
      min-area: 9  # Minimum alan (blok²) - 3x3 = 9
      max-area: 10000  # Maksimum alan (blok²) - 100x100 = 10000
      cooldown: 60  # Cooldown (saniye) - Oyuncu başına
      require-fence-connection: true  # Çitler bağlantılı olmalı mı?
      check-overlap: true  # Diğer klan alanlarıyla çakışma kontrolü
      overlap-buffer: 5  # Çakışma buffer mesafesi (blok)
      max-expansion-per-action: 50  # İşlem başına maksimum genişletme (blok)
    
    # Çit Ayarları
    fence:
      material: OAK_FENCE  # Çit material'ı
      metadata-key: "ClanFence"  # Metadata key
      require-clan-fence-item: true  # Klan çiti item'ı gerekli mi?
      min-fence-count: 4  # Minimum çit sayısı (alan oluşturmak için)
      fence-connection-distance: 2  # Çit bağlantı mesafesi (blok)
    
    # Kristal Ayarları
    crystal:
      metadata-key: "ClanCrystal"  # Metadata key
      require-clan-crystal-item: true  # Klan kristali item'ı gerekli mi?
      min-distance-from-other: 100  # Diğer klan kristallerinden minimum mesafe (blok)
    
    # Sınır Hesaplama
    boundary-calculation:
      async: true  # Async hesaplama (büyük alanlar için)
      cache-duration: 300000  # Cache süresi (ms) - 5 dakika
      recalculate-on-fence-break: true  # Çit kırıldığında yeniden hesapla
      recalculate-on-fence-place: true  # Çit yerleştirildiğinde yeniden hesapla
```

### TerritoryConfig.java

**Dosya:** `src/main/java/me/mami/stratocraft/manager/config/TerritoryConfig.java`

```java
package me.mami.stratocraft.manager.config;

import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;

public class TerritoryConfig {
    // Yükseklik Ayarları
    private int skyHeight = 150;
    private int groundDepth = 50;
    
    // Sınır Görselleştirme
    private boolean boundaryParticleEnabled = true;
    private Particle boundaryParticleType = Particle.REDSTONE;
    private Color boundaryParticleColor = Color.GREEN;
    private double boundaryParticleDensity = 0.5;
    private int boundaryParticleUpdateInterval = 20;
    private int boundaryParticleVisibleDistance = 100;
    private double boundaryParticleSpacing = 2.0;
    
    // Alan Genişletme/Küçültme
    private int minArea = 9;
    private int maxArea = 10000;
    private int expansionCooldown = 60;
    private boolean requireFenceConnection = true;
    private boolean checkOverlap = true;
    private int overlapBuffer = 5;
    private int maxExpansionPerAction = 50;
    
    // Çit Ayarları
    private String fenceMaterial = "OAK_FENCE";
    private String fenceMetadataKey = "ClanFence";
    private boolean requireClanFenceItem = true;
    private int minFenceCount = 4;
    private int fenceConnectionDistance = 2;
    
    // Kristal Ayarları
    private String crystalMetadataKey = "ClanCrystal";
    private boolean requireClanCrystalItem = true;
    private int minDistanceFromOtherCrystal = 100;
    
    // Sınır Hesaplama
    private boolean asyncBoundaryCalculation = true;
    private long boundaryCacheDuration = 300000;
    private boolean recalculateOnFenceBreak = true;
    private boolean recalculateOnFencePlace = true;
    
    public void loadFromConfig(FileConfiguration config) {
        // Yükseklik Ayarları
        skyHeight = config.getInt("clan.territory.sky-height", 150);
        groundDepth = config.getInt("clan.territory.ground-depth", 50);
        
        // Sınır Görselleştirme
        boundaryParticleEnabled = config.getBoolean("clan.territory.boundary-particle.enabled", true);
        String particleTypeStr = config.getString("clan.territory.boundary-particle.type", "REDSTONE");
        try {
            boundaryParticleType = Particle.valueOf(particleTypeStr);
        } catch (IllegalArgumentException e) {
            boundaryParticleType = Particle.REDSTONE;
        }
        
        String colorStr = config.getString("clan.territory.boundary-particle.color", "GREEN");
        boundaryParticleColor = parseColor(colorStr);
        boundaryParticleDensity = config.getDouble("clan.territory.boundary-particle.density", 0.5);
        boundaryParticleUpdateInterval = config.getInt("clan.territory.boundary-particle.update-interval", 20);
        boundaryParticleVisibleDistance = config.getInt("clan.territory.boundary-particle.visible-distance", 100);
        boundaryParticleSpacing = config.getDouble("clan.territory.boundary-particle.particle-spacing", 2.0);
        
        // Alan Genişletme/Küçültme
        minArea = config.getInt("clan.territory.expansion.min-area", 9);
        maxArea = config.getInt("clan.territory.expansion.max-area", 10000);
        expansionCooldown = config.getInt("clan.territory.expansion.cooldown", 60);
        requireFenceConnection = config.getBoolean("clan.territory.expansion.require-fence-connection", true);
        checkOverlap = config.getBoolean("clan.territory.expansion.check-overlap", true);
        overlapBuffer = config.getInt("clan.territory.expansion.overlap-buffer", 5);
        maxExpansionPerAction = config.getInt("clan.territory.expansion.max-expansion-per-action", 50);
        
        // Çit Ayarları
        fenceMaterial = config.getString("clan.territory.fence.material", "OAK_FENCE");
        fenceMetadataKey = config.getString("clan.territory.fence.metadata-key", "ClanFence");
        requireClanFenceItem = config.getBoolean("clan.territory.fence.require-clan-fence-item", true);
        minFenceCount = config.getInt("clan.territory.fence.min-fence-count", 4);
        fenceConnectionDistance = config.getInt("clan.territory.fence.fence-connection-distance", 2);
        
        // Kristal Ayarları
        crystalMetadataKey = config.getString("clan.territory.crystal.metadata-key", "ClanCrystal");
        requireClanCrystalItem = config.getBoolean("clan.territory.crystal.require-clan-crystal-item", true);
        minDistanceFromOtherCrystal = config.getInt("clan.territory.crystal.min-distance-from-other", 100);
        
        // Sınır Hesaplama
        asyncBoundaryCalculation = config.getBoolean("clan.territory.boundary-calculation.async", true);
        boundaryCacheDuration = config.getLong("clan.territory.boundary-calculation.cache-duration", 300000);
        recalculateOnFenceBreak = config.getBoolean("clan.territory.boundary-calculation.recalculate-on-fence-break", true);
        recalculateOnFencePlace = config.getBoolean("clan.territory.boundary-calculation.recalculate-on-fence-place", true);
    }
    
    private Color parseColor(String colorStr) {
        switch (colorStr.toUpperCase()) {
            case "RED": return Color.RED;
            case "GREEN": return Color.GREEN;
            case "BLUE": return Color.BLUE;
            case "YELLOW": return Color.YELLOW;
            case "ORANGE": return Color.ORANGE;
            case "PURPLE": return Color.PURPLE;
            case "WHITE": return Color.WHITE;
            case "BLACK": return Color.BLACK;
            default: return Color.GREEN;
        }
    }
    
    // Getters
    public int getSkyHeight() { return skyHeight; }
    public int getGroundDepth() { return groundDepth; }
    public boolean isBoundaryParticleEnabled() { return boundaryParticleEnabled; }
    public Particle getBoundaryParticleType() { return boundaryParticleType; }
    public Color getBoundaryParticleColor() { return boundaryParticleColor; }
    public double getBoundaryParticleDensity() { return boundaryParticleDensity; }
    public int getBoundaryParticleUpdateInterval() { return boundaryParticleUpdateInterval; }
    public int getBoundaryParticleVisibleDistance() { return boundaryParticleVisibleDistance; }
    public double getBoundaryParticleSpacing() { return boundaryParticleSpacing; }
    public int getMinArea() { return minArea; }
    public int getMaxArea() { return maxArea; }
    public int getExpansionCooldown() { return expansionCooldown; }
    public boolean isRequireFenceConnection() { return requireFenceConnection; }
    public boolean isCheckOverlap() { return checkOverlap; }
    public int getOverlapBuffer() { return overlapBuffer; }
    public int getMaxExpansionPerAction() { return maxExpansionPerAction; }
    public String getFenceMaterial() { return fenceMaterial; }
    public String getFenceMetadataKey() { return fenceMetadataKey; }
    public boolean isRequireClanFenceItem() { return requireClanFenceItem; }
    public int getMinFenceCount() { return minFenceCount; }
    public int getFenceConnectionDistance() { return fenceConnectionDistance; }
    public String getCrystalMetadataKey() { return crystalMetadataKey; }
    public boolean isRequireClanCrystalItem() { return requireClanCrystalItem; }
    public int getMinDistanceFromOtherCrystal() { return minDistanceFromOtherCrystal; }
    public boolean isAsyncBoundaryCalculation() { return asyncBoundaryCalculation; }
    public long getBoundaryCacheDuration() { return boundaryCacheDuration; }
    public boolean isRecalculateOnFenceBreak() { return recalculateOnFenceBreak; }
    public boolean isRecalculateOnFencePlace() { return recalculateOnFencePlace; }
}
```

---

## 💻 KOD DEĞİŞİKLİKLERİ

### 1. TerritoryData.java Oluştur

**Dosya:** `src/main/java/me/mami/stratocraft/model/territory/TerritoryData.java`

**Özellikler:**
- `Territory` modelini genişletir
- Çit lokasyonlarını tutar
- Sınır koordinatlarını hesaplar
- Y yükseklik kontrolü yapar

### 2. ClanFenceBlock.java Oluştur

**Dosya:** `src/main/java/me/mami/stratocraft/model/block/ClanFenceBlock.java`

**Özellikler:**
- Klan çiti blok modeli
- Metadata ile işaretleme
- Klan ID'si tutma

### 3. TerritoryManager.java Güncelle

**Değişiklikler:**
- `TerritoryData` kullanımı
- Çit lokasyonları yönetimi
- Sınır hesaplama metodları
- Y yükseklik kontrolü

### 4. TerritoryListener.java Güncelle

**Değişiklikler:**
- Klan çiti metadata kontrolü
- Klan kristali metadata kontrolü
- Çit yerleştirme/kırma event'leri
- Sınır görselleştirme güncellemesi

### 5. TerritoryBoundaryParticleTask.java Oluştur

**Dosya:** `src/main/java/me/mami/stratocraft/task/TerritoryBoundaryParticleTask.java`

**Özellikler:**
- Sürekli çalışan task
- Her klan üyesi için sınır partikülleri
- Çit lokasyonlarına göre partikül çizgisi
- Performans optimizasyonu

### 6. ClanTerritoryMenu.java Oluştur

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ClanTerritoryMenu.java`

**Özellikler:**
- Klan alanı genişletme/küçültme menüsü
- Çit kontrolü
- Alan hesaplama
- Onay sistemi

### 7. StructureMenuListener.java Güncelle

**Değişiklikler:**
- `CLAN_MANAGEMENT_CENTER` için menü açma
- Yetki kontrolü (Lider/General)

---

## 🎨 GUI DEĞİŞİKLİKLERİ

### ClanTerritoryMenu.java

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ClanTerritoryMenu.java`

**Menü Yapısı:**
```
┌─────────────────────────────────┐
│   Klan Alanı Yönetimi           │
├─────────────────────────────────┤
│ [Genişlet] [Küçült] [Bilgi]     │
│ [Sınırlar] [Yeniden Hesapla]    │
│ [Çıkış]                         │
└─────────────────────────────────┘
```

**Butonlar:**

1. **Genişlet (Slot 10):**
   - **Açıklama:** Klan alanını genişlet
   - **Yetki:** Lider veya General
   - **İşlemler:**
     - Çit kontrolü yap (`isSurroundedByClanFences`)
     - Yeni alan hesapla (flood-fill)
     - Y yüksekliğini kontrol et
     - Çakışma kontrolü yap
     - Onay menüsü aç
   - **Onay Menüsü:**
     - Eski alan: X blok²
     - Yeni alan: Y blok²
     - Genişletme: Z blok²
     - [Onayla] [İptal]

2. **Küçült (Slot 12):**
   - **Açıklama:** Klan alanını küçült
   - **Yetki:** Lider veya General
   - **İşlemler:**
     - Çit kontrolü yap
     - Yeni alan hesapla
     - Y yüksekliğini kontrol et
     - Onay menüsü aç
   - **Onay Menüsü:**
     - Eski alan: X blok²
     - Yeni alan: Y blok²
     - Küçültme: Z blok²
     - [Onayla] [İptal]

3. **Bilgi (Slot 14):**
   - **Açıklama:** Mevcut alan bilgisi
   - **Yetki:** Tüm klan üyeleri
   - **Bilgiler:**
     - Radius: X blok
     - Alan: Y blok²
     - Çit Sayısı: Z
     - Y Yüksekliği: MinY - MaxY
     - Gökyüzüne: +150 blok
     - Yer Altına: -50 blok
     - Sınır Koordinat Sayısı: N

4. **Sınırlar (Slot 16):**
   - **Açıklama:** Sınır koordinatlarını partikül ile göster
   - **Yetki:** Tüm klan üyeleri
   - **İşlemler:**
     - Sınır koordinatlarını al
     - Partikül efekti göster (10 saniye)
     - Mesaj gönder

5. **Yeniden Hesapla (Slot 22):**
   - **Açıklama:** Sınır koordinatlarını yeniden hesapla
   - **Yetki:** Lider veya General
   - **İşlemler:**
     - Çit lokasyonlarını kontrol et
     - Y yüksekliğini güncelle
     - Sınır koordinatlarını hesapla
     - Cache'i temizle

6. **Çıkış (Slot 26):**
   - **Açıklama:** Menüyü kapat
   - **Yetki:** Tüm klan üyeleri

**Kod Yapısı:**
```java
public class ClanTerritoryMenu implements Listener {
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    private final TerritoryConfig config;
    
    // Pending işlemler (onay için)
    private final Map<UUID, PendingExpansion> pendingExpansions = new ConcurrentHashMap<>();
    private final Map<UUID, PendingShrinkage> pendingShrinkages = new ConcurrentHashMap<>();
    
    public void openMenu(Player player) {
        // Yetki kontrolü
        // Menü oluştur
        // Butonları yerleştir
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        // Menü tıklama işlemleri
    }
    
    private void handleExpand(Player player, Clan clan) {
        // Genişletme işlemi
    }
    
    private void handleShrink(Player player, Clan clan) {
        // Küçültme işlemi
    }
    
    private void showInfo(Player player, Clan clan) {
        // Bilgi göster
    }
    
    private void showBoundaries(Player player, Clan clan) {
        // Sınır partikülleri göster
    }
    
    private void recalculateBoundaries(Player player, Clan clan) {
        // Sınırları yeniden hesapla
    }
}
```

### StructureMenuListener.java Güncellemesi

**Değişiklikler:**
```java
case CLAN_MANAGEMENT_CENTER:
    Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
    if (clan == null) {
        player.sendMessage("§cBir klana üye değilsiniz!");
        return;
    }
    
    // Yetki kontrolü (Lider veya General)
    Clan.Rank rank = clan.getRank(player.getUniqueId());
    if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
        player.sendMessage("§cBu menüye erişim yetkiniz yok! (Lider/General)");
        return;
    }
    
    // Klan bölgesinde mi kontrol
    Clan owner = territoryManager.getTerritoryOwner(structure.getLocation());
    if (owner == null || !owner.equals(clan)) {
        player.sendMessage("§cBu yapıya erişim yetkiniz yok!");
        return;
    }
    
    // YENİ: Klan Alanı Yönetim Menüsü
    if (plugin.getClanTerritoryMenu() != null) {
        plugin.getClanTerritoryMenu().openMenu(player);
    } else {
        // Fallback: Eski klan menüsü
        if (plugin.getClanMenu() != null) {
            plugin.getClanMenu().openMenu(player);
        }
    }
    break;
```

---

## 🔧 ADMIN KOMUTLARI

### Mevcut Komutlar

**`/stratocraft clan territory <klan> expand <miktar>`**
- ✅ Zaten var
- Güncelleme: `TerritoryData` kullanımı

**`/stratocraft clan territory <klan> reset`**
- ✅ Zaten var
- Güncelleme: Çit lokasyonlarını temizle

**`/stratocraft clan territory <klan> info`**
- ✅ Zaten var
- Güncelleme: Yeni bilgiler (çit sayısı, Y yüksekliği, sınır koordinatları)

### Yeni Komutlar

**`/stratocraft clan territory <klan> recalculate`**
- Sınır koordinatlarını yeniden hesapla
- Çit lokasyonlarını kontrol et
- Y yüksekliğini güncelle

**`/stratocraft clan territory <klan> setfence <x> <y> <z>`**
- Manuel çit lokasyonu ekle
- Test için

**`/stratocraft clan territory <klan> clearfences`**
- Tüm çit lokasyonlarını temizle

**`/stratocraft clan territory <klan> showboundaries`**
- Sınır koordinatlarını göster
- Partikül efekti göster

### Tab Completion

**Güncellemeler:**
- `territory` alt komutları: `expand`, `reset`, `info`, `recalculate`, `setfence`, `clearfences`, `showboundaries`
- Klan isimleri otomatik tamamlama

---

## 🚀 MAIN.JAVA GÜNCELLEMELERİ

### Yeni Manager'lar

```java
// TerritoryBoundaryManager
territoryBoundaryManager = new TerritoryBoundaryManager(this, territoryManager);

// TerritoryConfigManager (ConfigManager içinde)
territoryConfig = configManager.getTerritoryConfig();
```

### Yeni Task'lar

```java
// TerritoryBoundaryParticleTask
territoryBoundaryTask = new TerritoryBoundaryParticleTask(
    this, territoryManager, territoryBoundaryManager
);
territoryBoundaryTask.start(); // 20 tick interval
```

### Yeni Listener'lar

```java
// ClanTerritoryMenu (GUI)
clanTerritoryMenu = new ClanTerritoryMenu(clanManager, territoryManager);
Bukkit.getPluginManager().registerEvents(clanTerritoryMenu, this);
```

### Config Yükleme

```java
// ConfigManager içinde
territoryConfig = new TerritoryConfig();
territoryConfig.loadFromConfig(getConfig());
```

---

## 📝 DETAYLI ÇÖZÜM ADIMLARI

### Adım 1: Yeni Modeller Oluştur

**Öncelik:** ⚠️ KRİTİK

1. **`TerritoryData.java`** oluştur
   - `Territory` modelini genişletir
   - Çit lokasyonları listesi
   - Sınır koordinatları listesi
   - MinY, MaxY koordinatları
   - Sınır hesaplama metodları

2. **`ClanFenceBlock.java`** oluştur
   - `BaseBlock`'dan türetilir
   - Klan ID'si tutar
   - Metadata ile işaretleme

3. **`TerritoryConfig.java`** oluştur
   - Config yükleme
   - Getter metodları

### Adım 2: TerritoryManager Güncelle

**Öncelik:** ⚠️ KRİTİK

1. **`TerritoryData` kullanımı**
   - `Territory` yerine `TerritoryData` kullan
   - Geriye uyumluluk için wrapper metodlar

2. **Çit lokasyonları yönetimi**
   - `addFenceLocation()`
   - `removeFenceLocation()`
   - `getFenceLocations()`

3. **Sınır hesaplama metodları**
   - `calculateBoundaries()` - Async
   - `getBoundaryLine()` - Partikül için
   - Cache mekanizması

4. **Y yükseklik kontrolü**
   - `updateYBounds()` - MinY, MaxY güncelle
   - `isInsideTerritory()` - 3D kontrol

### Adım 3: TerritoryListener Güncelle

**Öncelik:** ⚠️ KRİTİK

1. **Klan çiti metadata kontrolü**
   - `BlockPlaceEvent` - Metadata ekle
   - `BlockBreakEvent` - Metadata kontrolü
   - `ItemManager.isClanItem()` kontrolü

2. **Klan kristali metadata kontrolü**
   - `Entity` metadata ekle
   - `findClanByCrystal()` güncelle

3. **Çit yerleştirme/kırma event'leri**
   - Çit yerleştirildiğinde `TerritoryData` güncelle
   - Çit kırıldığında `TerritoryData` güncelle
   - Sınır koordinatlarını yeniden hesapla

4. **Sınır görselleştirme güncellemesi**
   - `TerritoryBoundaryParticleTask` kullan
   - Eski `onPlayerMove` metodunu güncelle

### Adım 4: Partikül Sistemi Oluştur

**Öncelik:** ⚠️ KRİTİK

1. **`TerritoryBoundaryParticleTask.java`** oluştur
   - Sürekli çalışan task
   - Her klan üyesi için sınır partikülleri
   - Config'den partikül ayarları

2. **Performans optimizasyonu**
   - Chunk-based rendering
   - Distance-based culling
   - Rate limiting

### Adım 5: GUI Menüsü Oluştur

**Öncelik:** YÜKSEK

1. **`ClanTerritoryMenu.java`** oluştur
   - Menü yapısı
   - Buton yerleşimi
   - Event handling

2. **Genişletme/küçültme butonları**
   - Çit kontrolü
   - Alan hesaplama
   - Onay sistemi

3. **Bilgi butonu**
   - Mevcut alan bilgisi
   - Çit sayısı
   - Y yüksekliği

4. **Sınırlar butonu**
   - Partikül efekti
   - Süre kontrolü

### Adım 6: Config Sistemi

**Öncelik:** YÜKSEK

1. **`TerritoryConfig.java`** oluştur
   - Config yükleme
   - Getter metodları

2. **`config.yml` eklentileri**
   - `clan.territory` bölümü
   - Tüm ayarlar

3. **ConfigManager güncellemesi**
   - `TerritoryConfig` yükleme
   - Getter metodu

### Adım 7: Admin Komutları

**Öncelik:** ORTA

1. **Mevcut komutları güncelle**
   - `expand` - `TerritoryData` kullanımı
   - `reset` - Çit lokasyonlarını temizle
   - `info` - Yeni bilgiler

2. **Yeni komutlar ekle**
   - `recalculate`
   - `setfence`
   - `clearfences`
   - `showboundaries`
   - `setbounds`

3. **Tab completion güncelle**
   - Alt komutlar
   - Klan isimleri

### Adım 8: Main.java Entegrasyonu

**Öncelik:** YÜKSEK

1. **Yeni manager'ları başlat**
   - `TerritoryBoundaryManager`
   - `TerritoryConfig` (ConfigManager'dan)

2. **Yeni task'ları başlat**
   - `TerritoryBoundaryParticleTask`
   - Config'den interval

3. **Yeni listener'ları kaydet**
   - `ClanTerritoryMenu`

4. **Getter metodları ekle**
   - `getTerritoryBoundaryManager()`
   - `getTerritoryConfig()`
   - `getClanTerritoryMenu()`

### Adım 9: StructureMenuListener Güncelle

**Öncelik:** YÜKSEK

1. **`CLAN_MANAGEMENT_CENTER` menü açma**
   - Yetki kontrolü (Lider/General)
   - `ClanTerritoryMenu` açma

### Adım 10: DataManager Güncellemesi

**Öncelik:** ORTA

1. **`TerritoryData` kaydetme/yükleme**
   - SQLite entegrasyonu
   - Çit lokasyonları kaydetme
   - Sınır koordinatları kaydetme
   - Y yüksekliği kaydetme

---

## 🔍 SORUN ÇÖZÜM DETAYLARI

### Sorun 1: Çitler Kırıldığında Sınırlar Kayboluyor

**Çözüm:**
- `TerritoryData` modelinde çit lokasyonlarını kaydet
- Çit kırıldığında `removeFenceLocation()` çağır
- Sınır koordinatlarını yeniden hesapla
- Partikül sistemini güncelle

### Sorun 2: Klan Çiti vs Normal Çit

**Çözüm:**
- `BlockPlaceEvent`'te klan çiti item'ı kontrolü
- Metadata ekle (`"ClanFence"`)
- `TerritoryData`'da kaydet
- `BlockBreakEvent`'te metadata kontrolü

### Sorun 3: Klan Kristali Kontrolü

**Çözüm:**
- `Entity` metadata ekle (`"ClanCrystal"`)
- `findClanByCrystal()` metodunda metadata kontrolü
- Normal `EnderCrystal`'ları filtrele

### Sorun 4: Alan Genişletme/Küçültme

**Çözüm:**
- `CLAN_MANAGEMENT_CENTER` yapısına sağ tıklayınca menü aç
- Çit kontrolü yap (`isSurroundedByClanFences`)
- Alan hesapla (flood-fill)
- Y yüksekliğini kontrol et
- Genişlet/küçült

### Sorun 5: Sınır Görselleştirme

**Çözüm:**
- `TerritoryBoundaryParticleTask` oluştur
- Her klan üyesi için sınır partikülleri
- Çit lokasyonlarına göre partikül çizgisi
- Config'den partikül tipi, renk, yoğunluk

### Sorun 6: Y Yüksekliği

**Çözüm:**
- `TerritoryData` modelinde `minY`, `maxY` tut
- Çit yerleştirildiğinde Y koordinatlarını kontrol et
- En yüksek/en alçak çit bul
- Gökyüzüne 150, yer altına 50 blok hesapla

---

## 📊 PERFORMANS OPTİMİZASYONLARI

### 1. Sınır Hesaplama

- **Async:** Büyük alanlar için async hesaplama
- **Cache:** Sınır koordinatlarını cache'le
- **Incremental:** Sadece değişen kısımları güncelle

### 2. Partikül Sistemi

- **Chunk-based:** Sadece yüklü chunk'larda partikül
- **Distance-based:** Oyuncuya yakın partiküller
- **Rate Limiting:** Partikül yoğunluğu config'den

### 3. Çit Kontrolü

- **Metadata:** Hızlı lookup için metadata
- **Spatial Index:** Büyük alanlar için spatial index
- **Incremental:** Sadece yeni çitleri kontrol et

---

## ✅ TEST SENARYOLARI

### Test 1: Klan Kurma
1. Klan çiti ile alan çevir
2. Klan kristali yerleştir
3. Klan kur
4. Çit lokasyonları kaydedildi mi? ✅
5. Sınır koordinatları hesaplandı mı? ✅

### Test 2: Çit Kırma
1. Klan çitlerini kır
2. Sınırlar hala görünüyor mu? ✅
3. Partikül efekti çalışıyor mu? ✅

### Test 3: Alan Genişletme
1. `CLAN_MANAGEMENT_CENTER` yapısına sağ tıkla
2. "Genişlet" butonuna bas
3. Yeni çitlerle alan çevir
4. Alan genişledi mi? ✅

### Test 4: Y Yüksekliği
1. Farklı yüksekliklerde çit yerleştir
2. MinY, MaxY doğru hesaplandı mı? ✅
3. Gökyüzüne 150, yer altına 50 blok çalışıyor mu? ✅

### Test 5: Normal Çit vs Klan Çiti
1. Normal çit yerleştir
2. Klan alanı oluştu mu? ❌ (olmamalı)
3. Klan çiti yerleştir
4. Klan alanı oluştu mu? ✅

---

## 📋 ÖNCELİK SIRASI

### Yüksek Öncelik

1. **TerritoryData Modeli** ⚠️ KRİTİK
2. **Klan Çiti Metadata Sistemi** ⚠️ KRİTİK
3. **Klan Kristali Metadata Sistemi** ⚠️ KRİTİK
4. **Sınır Görselleştirme Sistemi** ⚠️ KRİTİK

### Orta Öncelik

5. **Alan Genişletme/Küçültme Menüsü**
6. **Y Yüksekliği Kontrolü**
7. **Config Sistemi**

### Düşük Öncelik

8. **Admin Komutları Güncellemeleri**
9. **Performans Optimizasyonları**
10. **Test Senaryoları**

---

## 🎯 SONUÇ

Bu plan, tüm tespit edilen sorunları çözecek ve klan alanı sistemini tam işlevsel hale getirecektir. Yeni modeller, config sistemi, GUI menüleri ve admin komutları ile kapsamlı bir çözüm sunulmaktadır.

### Özet

**Tespit Edilen Sorunlar:**
1. ❌ Çitler kırıldığında sınırlar kayboluyor
2. ❌ Klan çiti vs normal çit ayrımı yok
3. ❌ Klan kristali kontrolü eksik
4. ❌ Alan genişletme/küçültme sistemi yok
5. ❌ Sınır görselleştirme eksik
6. ❌ Y yüksekliği kontrolü yok

**Çözümler:**
1. ✅ `TerritoryData` modeli - Çit lokasyonları ve sınır koordinatları
2. ✅ Metadata sistemi - Klan çiti ve kristal işaretleme
3. ✅ Partikül sistemi - Sürekli çalışan sınır görselleştirme
4. ✅ GUI menüsü - Alan genişletme/küçültme
5. ✅ Y yüksekliği kontrolü - MinY, MaxY hesaplama
6. ✅ Config sistemi - Tüm değerler config'den

**Durum:** 📝 **PLAN HAZIR** - İmplementasyona başlanabilir

---

## 📌 EK NOTLAR

### Performans Önerileri

1. **Async Hesaplama:**
   - Büyük alanlar için async sınır hesaplama
   - Main thread'i kilitleme

2. **Cache Mekanizması:**
   - Sınır koordinatlarını cache'le
   - Config'den cache süresi

3. **Rate Limiting:**
   - Partikül yoğunluğu kontrolü
   - Cooldown mekanizması

### Güvenlik Önerileri

1. **Çakışma Kontrolü:**
   - Diğer klan alanlarıyla çakışma kontrolü
   - Buffer mesafesi

2. **Anti-Abuse:**
   - Maksimum genişletme limiti
   - Cooldown mekanizması
   - Minimum çit sayısı

### Test Önerileri

1. **Küçük Alan Testi:**
   - 3x3 alan
   - Çit kırma
   - Sınır görselleştirme

2. **Büyük Alan Testi:**
   - 100x100 alan
   - Performans testi
   - Async hesaplama testi

3. **Y Yüksekliği Testi:**
   - Farklı yüksekliklerde çitler
   - MinY, MaxY kontrolü

---

**Son Güncelleme:** 2024
**Hazırlayan:** AI Assistant

