# KLAN SİSTEMİ KAPSAMLI SORUN ANALİZİ VE ÇÖZÜM PLANI

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Klan Kurma Sistemi Analizi](#klan-kurma-sistemi-analizi)
3. [Klan Alanı Koruma Sistemi Analizi](#klan-alanı-koruma-sistemi-analizi)
4. [Klan Sınırları Görselleştirme Sistemi](#klan-sınırları-görselleştirme-sistemi)
5. [Oyuncu Klan Üyeliği ve Özellik Kontrol Sistemi](#oyuncu-klan-üyeliği-ve-özellik-kontrol-sistemi)
6. [Klan Alanı Güncelleme Sistemi](#klan-alanı-güncelleme-sistemi)
7. [Y Ekseni Sınırları Hesaplama](#y-ekseni-sınırları-hesaplama)
8. [Çözüm Önerileri ve Uygulama Planı](#çözüm-önerileri-ve-uygulama-planı)
9. [Kaynakça](#kaynakça)

---

## GENEL BAKIŞ

### Sistem Mimarisi

```
┌─────────────────────────────────────────────────────────────┐
│                    KLAN SİSTEMİ MİMARİSİ                    │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│  ClanManager     │──────▶│ TerritoryManager │──────▶│ TerritoryData    │
│  - Klan verileri │      │  - Alan kontrolü  │      │  - Çit lokasyonları
│  - Üye yönetimi  │      │  - Cache sistemi  │      │  - Sınır koordinatları
└──────────────────┘      └──────────────────┘      └──────────────────┘
         │                          │                          │
         │                          │                          │
         ▼                          ▼                          ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ TerritoryListener│      │BoundaryManager   │      │  PlayerData      │
│  - Event handling│      │  - Sınır hesaplama│      │  - Klan üyeliği  │
│  - Korumalar     │      │  - Partikül      │      │  - Buff kontrolü │
└──────────────────┘      └──────────────────┘      └──────────────────┘
```

### Tespit Edilen Ana Sorunlar

1. ❌ **Klan Kurma Sistemi**: Çit algılama algoritması yükseklik farklarını düzgün işlemiyor
2. ❌ **Y Ekseni Sınırları**: En yukarıdaki çitten 150 blok, en alttaki çitten 20 blok hesaplama eksik
3. ❌ **Klan Alanı Koruma**: Başka oyuncular blok kırıp koyabiliyor (koruma çalışmıyor)
4. ❌ **Klan Sınırları Görselleştirme**: Partikül sistemi çalışmıyor veya eksik
5. ❌ **Oyuncu Özellik Kontrolü**: Sürekli çalışan bir oyuncu özellik kontrol sistemi yok
6. ❌ **Klan Alanı Güncelleme**: Tek tuş ile alan güncelleme çalışmıyor

---

## KLAN KURMA SİSTEMİ ANALİZİ

### Mevcut Sistem Akış Şeması

```
┌─────────────────────────────────────────────────────────────┐
│              KLAN KURMA SÜRECİ (Mevcut)                     │
└─────────────────────────────────────────────────────────────┘

1. Oyuncu Klan Kristali Yerleştirir
   │
   ├─▶ ItemManager.isClanItem("CRYSTAL") kontrolü
   │   │
   │   ├─▶ ✅ Geçerli → Devam
   │   └─▶ ❌ Geçersiz → İptal
   │
   ├─▶ Oyuncunun zaten klanı var mı?
   │   │
   │   ├─▶ ✅ Var → İptal
   │   └─▶ ❌ Yok → Devam
   │
   ├─▶ ASYNC: isSurroundedByClanFences3D() kontrolü
   │   │
   │   ├─▶ 3D Flood-Fill Algoritması
   │   │   │
   │   │   ├─▶ ✅ 6 yöne bakıyor (NORTH, SOUTH, EAST, WEST, UP, DOWN)
   │   │   ├─▶ ✅ Yükseklik toleransı var (fenceHeightTolerance)
   │   │   ├─▶ ✅ Çit bağlantı kontrolü var (isFenceConnected)
   │   │   └─▶ ⚠️ SORUN: Y ekseni sınırları hesaplanmıyor
   │   │
   │   ├─▶ ✅ Çevrelenmiş → continueCrystalPlacement()
   │   └─▶ ❌ Çevrelenmemiş → Hata mesajı
   │
   └─▶ Klan Oluşturma
       │
       ├─▶ Territory oluşturuluyor
       ├─▶ TerritoryData oluşturuluyor
       ├─▶ Çit lokasyonları toplanıyor
       └─▶ ⚠️ SORUN: Y ekseni sınırları (minY, maxY) düzgün hesaplanmıyor
```

### Tespit Edilen Sorunlar

#### 1. ❌ Y Ekseni Sınırları Hesaplanmıyor

**Sorun:**
- `TerritoryData` modelinde `minY` ve `maxY` alanları var
- Ancak klan kurulurken bu değerler düzgün hesaplanmıyor
- Config'de `skyHeight = 150` ve `groundDepth = 20` var ama kullanılmıyor

**Kod Analizi:**
```java
// TerritoryData.java:184-221
public void updateYBounds() {
    // Çitlerden minY ve maxY hesaplanıyor
    // ANCAK: Klan kurulurken bu metod çağrılmıyor!
}
```

**Etki:**
- Klan alanı Y ekseninde sınırsız görünüyor
- Partikül sistemi Y ekseninde doğru çalışmıyor
- Koruma sistemi Y ekseninde çalışmıyor

#### 2. ⚠️ Çit Algılama Algoritması Yetersiz

**Sorun:**
- `isSurroundedByClanFences3D()` metodu var ve çalışıyor
- Ancak Y ekseni sınırları hesaplanmıyor
- Config'deki `fenceHeightTolerance` kullanılıyor ama yeterli değil

**Kod Analizi:**
```java
// TerritoryListener.java:1203-1300
private boolean isSurroundedByClanFences3D(Block center, int heightTolerance) {
    // ✅ 3D flood-fill var
    // ✅ Yükseklik toleransı var
    // ❌ ANCAK: Y ekseni sınırları (minY, maxY) hesaplanmıyor
    // ❌ ANCAK: skyHeight ve groundDepth kullanılmıyor
}
```

**Etki:**
- Çitler algılanıyor ama Y ekseni sınırları hesaplanmıyor
- Klan alanı Y ekseninde sınırsız

#### 3. ⚠️ continueCrystalPlacement() Metodu Eksik

**Sorun:**
- `continueCrystalPlacement()` metodunda TerritoryData oluşturuluyor
- Ancak Y ekseni sınırları hesaplanmıyor
- Çit lokasyonları toplanıyor ama minY/maxY güncellenmiyor

**Kod Analizi:**
```java
// TerritoryListener.java:1077-1120
// YENİ: TerritoryData oluştur ve çit lokasyonlarını ekle
if (boundaryManager != null && territoryConfig != null) {
    TerritoryData territoryData = boundaryManager.getTerritoryData(newClan);
    // ✅ TerritoryData oluşturuluyor
    // ✅ Çit lokasyonları toplanıyor (collectFenceLocationsAsync)
    // ❌ ANCAK: updateYBounds() çağrılmıyor!
    // ❌ ANCAK: skyHeight ve groundDepth set edilmiyor!
}
```

**Gerçek Kod:**
```java
// TerritoryListener.java:1077-1104
if (boundaryManager != null && territoryConfig != null) {
    TerritoryData territoryData = new TerritoryData(newClan.getId(), pending.crystalLoc);
    territoryData.setRadius(territory.getRadius());
    territoryData.setSkyHeight(territoryConfig.getSkyHeight()); // ✅ Set ediliyor
    territoryData.setGroundDepth(territoryConfig.getGroundDepth()); // ✅ Set ediliyor
    
    // Async çit lokasyonlarını topla
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        findAndAddFenceLocations(pending.placeLocation.getLocation(), territoryData);
        
        // Main thread'e geri dön
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (boundaryManager != null) {
                boundaryManager.setTerritoryData(newClan, territoryData);
            }
            // ❌ updateYBounds() çağrılmıyor!
        });
    });
}
```

**Sorun:**
- `skyHeight` ve `groundDepth` set ediliyor ✅
- Ancak `updateYBounds()` çağrılmıyor ❌
- Çit lokasyonları eklendikten sonra Y ekseni sınırları güncellenmiyor ❌

---

## KLAN ALANI KORUMA SİSTEMİ ANALİZİ

### Mevcut Sistem Akış Şeması

```
┌─────────────────────────────────────────────────────────────┐
│          KLAN ALANI KORUMA SİSTEMİ (Mevcut)                 │
└─────────────────────────────────────────────────────────────┘

1. BlockBreakEvent / BlockPlaceEvent
   │
   ├─▶ Admin bypass kontrolü
   │   │
   │   ├─▶ ✅ Admin → İzin ver
   │   └─▶ ❌ Normal oyuncu → Devam
   │
   ├─▶ TerritoryManager.getTerritoryOwner(location)
   │   │
   │   ├─▶ Chunk-based cache kontrolü
   │   │   │
   │   │   ├─▶ ✅ Cache'de var → Hızlı dönüş
   │   │   └─▶ ❌ Cache'de yok → Legacy kontrol
   │   │
   │   ├─▶ GeometryUtil.isInsideRadius() kontrolü
   │   │   │
   │   │   ├─▶ ✅ 2D kontrol (X, Z)
   │   │   └─▶ ❌ SORUN: Y ekseni kontrolü YOK!
   │   │
   │   ├─▶ ✅ Sahip bulundu → Devam
   │   └─▶ ❌ Sahipsiz → İzin ver
   │
   ├─▶ owner.hasCrystal() kontrolü
   │   │
   │   ├─▶ ✅ Kristal var → Devam
   │   └─▶ ❌ Kristal yok → İzin ver (koruma yok)
   │
   ├─▶ Oyuncu klan kontrolü
   │   │
   │   ├─▶ ✅ Kendi klanı → Rütbe kontrolü
   │   │   │
   │   │   ├─▶ ✅ RECRUIT → İzin verme
   │   │   └─▶ ✅ Diğer rütbeler → İzin ver
   │   │
   │   ├─▶ ✅ Misafir (Guest) → İzin ver
   │   ├─▶ ✅ Savaş durumu → İzin ver
   │   └─▶ ❌ Düşman klan → İptal et
   │
   └─▶ ⚠️ SORUN: Y ekseni kontrolü yapılmıyor!
```

### Tespit Edilen Sorunlar

#### 1. ❌ Y Ekseni Kontrolü Eksik

**Sorun:**
- `TerritoryManager.getTerritoryOwner()` sadece 2D (X, Z) kontrolü yapıyor
- `GeometryUtil.isInsideRadius()` sadece 2D mesafe hesaplıyor
- Y ekseni kontrolü hiç yapılmıyor

**Kod Analizi:**
```java
// TerritoryManager.java:29-58
public Clan getTerritoryOwner(Location loc) {
    // Chunk cache kontrolü
    // GeometryUtil.isInsideRadius() → Sadece 2D!
    // ❌ Y ekseni kontrolü YOK!
}

// GeometryUtil.java (tahmini)
public static boolean isInsideRadius(Location center, Location loc, int radius) {
    double distance2D = Math.sqrt(
        Math.pow(loc.getX() - center.getX(), 2) + 
        Math.pow(loc.getZ() - center.getZ(), 2)
    );
    // ❌ Y ekseni kontrolü YOK!
    return distance2D <= radius;
}
```

**Etki:**
- Y ekseninde sınırsız koruma
- Oyuncular Y ekseninde çok yukarıdan veya aşağıdan blok kırıp koyabiliyor
- Klan alanı Y ekseninde korunmuyor

#### 2. ⚠️ TerritoryData.isInsideTerritory() Kullanılmıyor

**Sorun:**
- `TerritoryData.isInsideTerritory()` metodu var ve Y ekseni kontrolü yapıyor
- Ancak `TerritoryManager.getTerritoryOwner()` bu metodu kullanmıyor
- Sadece eski `GeometryUtil.isInsideRadius()` kullanılıyor

**Kod Analizi:**
```java
// TerritoryData.java:272-290
public boolean isInsideTerritory(Location loc) {
    // ✅ 2D kontrol (X, Z)
    // ✅ Y ekseni kontrolü var!
    // ✅ skyHeight ve groundDepth kullanılıyor!
    // ANCAK: Bu metod kullanılmıyor!
}
```

**Etki:**
- Y ekseni kontrolü yapılabiliyor ama kullanılmıyor
- Koruma sistemi Y ekseninde çalışmıyor

#### 3. ⚠️ Cache Sistemi Y Ekseni İçin Çalışmıyor

**Sorun:**
- Chunk-based cache sadece X, Z koordinatlarını kullanıyor
- Y ekseni cache'de tutulmuyor
- Aynı chunk'ta farklı Y seviyelerinde farklı klanlar olabilir

**Kod Analizi:**
```java
// TerritoryManager.java:37-39
int chunkX = loc.getBlockX() >> 4;
int chunkZ = loc.getBlockZ() >> 4;
String chunkKey = chunkX + ";" + chunkZ;
// ❌ Y ekseni cache'de YOK!
```

**Etki:**
- Cache sistemi Y ekseni için çalışmıyor
- Performans sorunları olabilir

---

## KLAN SINIRLARI GÖRSELLEŞTİRME SİSTEMİ

### Mevcut Sistem Akış Şeması

```
┌─────────────────────────────────────────────────────────────┐
│      KLAN SINIRLARI GÖRSELLEŞTİRME SİSTEMİ (Mevcut)        │
└─────────────────────────────────────────────────────────────┘

1. TerritoryBoundaryParticleTask (Sürekli çalışan task)
   │
   ├─▶ Config kontrolü (isBoundaryParticleEnabled)
   │   │
   │   ├─▶ ❌ Kapalı → Dur
   │   └─▶ ✅ Açık → Devam
   │
   ├─▶ Tüm online oyuncuları kontrol et
   │   │
   │   ├─▶ Oyuncunun klanı var mı?
   │   │   │
   │   │   ├─▶ ❌ Yok → Atla
   │   │   └─▶ ✅ Var → Devam
   │   │
   │   ├─▶ TerritoryData al
   │   │   │
   │   │   ├─▶ ❌ Yok → Atla
   │   │   └─▶ ✅ Var → Devam
   │   │
   │   ├─▶ Mesafe kontrolü (visibleDistance)
   │   │   │
   │   │   ├─▶ ❌ Çok uzak → Atla
   │   │   └─▶ ✅ Yakın → Devam
   │   │
   │   └─▶ showBoundaryParticles()
   │       │
   │       ├─▶ boundaryLine al (TerritoryData.getBoundaryLine())
   │       │   │
   │       │   ├─▶ ✅ Sınır koordinatları var → Partikül göster
   │       │   └─▶ ❌ Sınır koordinatları yok → Atla
   │       │
   │       └─▶ ⚠️ SORUN: Y ekseni sınırları dikkate alınmıyor!
   │
2. TerritoryListener.onPlayerMove() (Oyuncu hareket event'i)
   │
   ├─▶ Cooldown kontrolü
   │   │
   │   ├─▶ ✅ Cooldown'da → Atla
   │   └─▶ ❌ Cooldown bitti → Devam
   │
   ├─▶ Oyuncunun klanı var mı?
   │   │
   │   ├─▶ ❌ Yok → Atla
   │   └─▶ ✅ Var → Devam
   │
   ├─▶ Territory al
   │   │
   │   ├─▶ ❌ Yok → Atla
   │   └─▶ ✅ Var → Devam
   │
   ├─▶ Mesafe kontrolü (10 blok)
   │   │
   │   ├─▶ ❌ Çok uzak → Atla
   │   └─▶ ✅ Yakın → showTerritoryBoundary()
   │       │
   │       └─▶ ⚠️ SORUN: Y ekseni sınırları dikkate alınmıyor!
```

### Tespit Edilen Sorunlar

#### 1. ❌ Y Ekseni Sınırları Dikkate Alınmıyor

**Sorun:**
- `showBoundaryParticles()` ve `showTerritoryBoundary()` metodları Y ekseni sınırlarını dikkate almıyor
- Partiküller sadece X, Z koordinatlarında gösteriliyor
- Y ekseninde sınırlar görünmüyor

**Kod Analizi:**
```java
// TerritoryBoundaryParticleTask.java:110-144
private void showBoundaryParticles(Player player, TerritoryData territoryData) {
    List<Location> boundaryLine = territoryData.getBoundaryLine();
    // ❌ Y ekseni sınırları dikkate alınmıyor!
    // ❌ Partiküller sadece center.getY() seviyesinde gösteriliyor!
}

// TerritoryListener.java:1173-1199
private void showTerritoryBoundary(Player player, Territory territory, Location playerLoc) {
    // ❌ Y ekseni sınırları dikkate alınmıyor!
    // ❌ Partiküller sadece playerLoc.getY() seviyesinde gösteriliyor!
}
```

**Etki:**
- Klan üyeleri Y ekseninde sınırları göremiyor
- Partikül sistemi Y ekseninde çalışmıyor

#### 2. ⚠️ TerritoryData.getBoundaryLine() Y Ekseni İçin Çalışmıyor

**Sorun:**
- `TerritoryData.getBoundaryLine()` sadece X, Z koordinatlarını döndürüyor
- Y koordinatı sadece `center.getY()` olarak ayarlanıyor
- Y ekseni sınırları (minY, maxY) kullanılmıyor

**Kod Analizi:**
```java
// TerritoryData.java:229-267
public void calculateBoundaries() {
    // Sadece X, Z koordinatları hesaplanıyor
    Location boundaryLoc = new Location(center.getWorld(), x, center.getY(), z);
    // ❌ Y koordinatı sadece center.getY() olarak ayarlanıyor!
    // ❌ minY ve maxY kullanılmıyor!
}
```

**Etki:**
- Sınır koordinatları Y ekseni için doğru değil
- Partikül sistemi Y ekseninde çalışmıyor

#### 3. ⚠️ Oyuncu Klan Üyeliği Kontrolü Eksik

**Sorun:**
- `TerritoryBoundaryParticleTask` oyuncunun klanını kontrol ediyor
- Ancak oyuncu klan üyeliği değiştiğinde güncellenmiyor
- Cache sistemi yok

**Kod Analizi:**
```java
// TerritoryBoundaryParticleTask.java:82-83
Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
// ❌ Her seferinde sorgu yapılıyor, cache yok!
```

**Etki:**
- Performans sorunları olabilir
- Oyuncu klan üyeliği değiştiğinde güncellenmiyor

---

## OYUNCU KLAN ÜYELİĞİ VE ÖZELLİK KONTROL SİSTEMİ

### Mevcut Sistem Analizi

```
┌─────────────────────────────────────────────────────────────┐
│    OYUNCU KLAN ÜYELİĞİ VE ÖZELLİK KONTROL SİSTEMİ           │
└─────────────────────────────────────────────────────────────┘

1. PlayerData Modeli
   │
   ├─▶ ✅ Klan üyeliği var (clanId, rank, isInClan)
   ├─▶ ✅ Aktivite takibi var (lastActivity)
   └─▶ ⚠️ SORUN: Sürekli çalışan kontrol sistemi YOK!

2. BuffTask (Sürekli çalışan task)
   │
   ├─▶ ✅ Klan özel yapılar kontrol ediliyor
   ├─▶ ✅ Territory yapıları kontrol ediliyor
   └─▶ ⚠️ SORUN: Oyuncu klan üyeliği kontrolü eksik!

3. TerritoryBoundaryParticleTask
   │
   ├─▶ ✅ Oyuncu klan üyeliği kontrol ediliyor
   └─▶ ⚠️ SORUN: Sadece partikül için, genel kontrol değil!

4. ❌ SORUN: Genel oyuncu özellik kontrol sistemi YOK!
```

### Tespit Edilen Sorunlar

#### 1. ❌ Sürekli Çalışan Oyuncu Özellik Kontrol Sistemi Yok

**Sorun:**
- Oyuncu klan üyeliği, buff'lar, partiküller vb. için sürekli çalışan bir kontrol sistemi yok
- Her sistem kendi kontrolünü yapıyor
- Merkezi bir kontrol sistemi yok

**Etki:**
- Kod tekrarı
- Performans sorunları
- Tutarsızlıklar

#### 2. ⚠️ PlayerData Cache Sistemi Eksik

**Sorun:**
- `PlayerDataManager` var ama cache sistemi yok
- Her seferinde `getClanByPlayer()` sorgusu yapılıyor
- Performans sorunları olabilir

**Kod Analizi:**
```java
// PlayerDataManager.java:33-36
public PlayerData getPlayerData(UUID playerId) {
    return playerDataMap.computeIfAbsent(playerId, k -> new PlayerData(playerId));
    // ✅ Cache var ama kullanılmıyor!
}
```

**Etki:**
- Performans sorunları
- Gereksiz sorgular

#### 3. ⚠️ Buff Sistemi Entegrasyonu Eksik

**Sorun:**
- `BuffManager` var ve çalışıyor
- Ancak sürekli çalışan bir kontrol sistemi yok
- Oyuncu klan üyeliği değiştiğinde buff'lar güncellenmiyor

**Kod Analizi:**
```java
// BuffManager.java:209-216
public void checkBuffsOnJoin(Player p, Clan clan) {
    // ✅ Oyuncu giriş yaptığında kontrol ediliyor
    // ❌ ANCAK: Sürekli çalışan kontrol YOK!
}
```

**Etki:**
- Buff'lar sadece oyuncu giriş yaptığında kontrol ediliyor
- Oyuncu klan üyeliği değiştiğinde buff'lar güncellenmiyor

---

## KLAN ALANI GÜNCELLEME SİSTEMİ

### Mevcut Sistem Akış Şeması

```
┌─────────────────────────────────────────────────────────────┐
│          KLAN ALANI GÜNCELLEME SİSTEMİ (Mevcut)             │
└─────────────────────────────────────────────────────────────┘

1. ClanTerritoryMenu.recalculateBoundaries()
   │
   ├─▶ TerritoryData al
   │   │
   │   ├─▶ ❌ Yok → Hata mesajı
   │   └─▶ ✅ Var → Devam
   │
   ├─▶ Klan kristali lokasyonu al
   │   │
   │   ├─▶ ❌ Yok → Hata mesajı
   │   └─▶ ✅ Var → Devam
   │
   ├─▶ ASYNC: isSurroundedByClanFences() kontrolü
   │   │
   │   ├─▶ ✅ Çevrelenmiş → Devam
   │   └─▶ ❌ Çevrelenmemiş → Hata mesajı
   │
   ├─▶ Eski sınırları temizle
   │   │
   │   ├─▶ territoryData.clearBoundaries()
   │   └─▶ ⚠️ SORUN: Eski çit lokasyonları temizlenmiyor!
   │
   ├─▶ Yeni çit lokasyonlarını topla
   │   │
   │   ├─▶ collectFenceLocations() → Yeni çitler
   │   └─▶ ⚠️ SORUN: Eski çit lokasyonları silinmiyor!
   │
   ├─▶ TerritoryData'yı güncelle
   │   │
   │   ├─▶ clearFenceLocations() → Eski çitleri temizle
   │   ├─▶ Yeni çitleri ekle
   │   └─▶ ⚠️ SORUN: Y ekseni sınırları güncellenmiyor!
   │
   ├─▶ Sınırları hesapla
   │   │
   │   ├─▶ calculateBoundaries()
   │   └─▶ ⚠️ SORUN: Y ekseni sınırları hesaplanmıyor!
   │
   └─▶ Cache'i güncelle
       │
       └─▶ territoryManager.setCacheDirty()
```

### Tespit Edilen Sorunlar

#### 1. ❌ Eski Çit Lokasyonları Düzgün Temizlenmiyor

**Sorun:**
- `recalculateBoundaries()` metodunda eski çit lokasyonları temizleniyor
- Ancak dünyadaki gerçek çitler kontrol edilmiyor
- Eski çitler hala TerritoryData'da kalabilir

**Kod Analizi:**
```java
// ClanTerritoryMenu.java:357-361
territoryData.clearFenceLocations();
for (Location fenceLoc : newFenceLocations) {
    territoryData.addFenceLocation(fenceLoc);
}
// ⚠️ SORUN: Eski çitler dünyada hala var mı kontrol edilmiyor!
```

**Etki:**
- Eski çitler TerritoryData'da kalabilir
- Sınır hesaplama yanlış olabilir

#### 2. ❌ Y Ekseni Sınırları Güncellenmiyor

**Sorun:**
- `recalculateBoundaries()` metodunda Y ekseni sınırları güncellenmiyor
- `updateYBounds()` çağrılmıyor
- `skyHeight` ve `groundDepth` kullanılmıyor

**Kod Analizi:**
```java
// ClanTerritoryMenu.java:363-364
territoryData.calculateBoundaries();
// ❌ updateYBounds() çağrılmıyor!
// ❌ skyHeight ve groundDepth kullanılmıyor!
```

**Etki:**
- Y ekseni sınırları güncellenmiyor
- Partikül sistemi Y ekseninde çalışmıyor

#### 3. ⚠️ collectFenceLocations() Metodu Eksik veya Yetersiz

**Sorun:**
- `collectFenceLocations()` metodu var mı kontrol edilmeli
- Eğer varsa, düzgün çalışıyor mu kontrol edilmeli
- Y ekseni sınırlarını dikkate alıyor mu kontrol edilmeli

---

## Y EKSENİ SINIRLARI HESAPLAMA

### Gereksinimler

1. **En yukarıdaki çitten 150 blok yukarı**: `maxY + skyHeight` (skyHeight = 150)
2. **En alttaki çitten 20 blok aşağı**: `minY - groundDepth` (groundDepth = 20)

### Mevcut Durum

```
┌─────────────────────────────────────────────────────────────┐
│          Y EKSENİ SINIRLARI HESAPLAMA (Mevcut)              │
└─────────────────────────────────────────────────────────────┘

TerritoryData Modeli:
├─▶ ✅ minY: En alçak çit Y koordinatı (hesaplanıyor)
├─▶ ✅ maxY: En yüksek çit Y koordinatı (hesaplanıyor)
├─▶ ✅ skyHeight: 150 (config'den)
├─▶ ✅ groundDepth: 20 (config'den)
└─▶ ⚠️ SORUN: Bu değerler kullanılmıyor!

isInsideTerritory() Metodu:
├─▶ ✅ Y ekseni kontrolü var
├─▶ ✅ skyHeight ve groundDepth kullanılıyor
└─▶ ⚠️ SORUN: Bu metod kullanılmıyor!

TerritoryManager.getTerritoryOwner():
├─▶ ❌ Y ekseni kontrolü YOK!
└─▶ ❌ isInsideTerritory() kullanılmıyor!
```

### Çözüm Önerisi

1. **TerritoryManager.getTerritoryOwner()** metodunu güncelle
   - `TerritoryData.isInsideTerritory()` metodunu kullan
   - Y ekseni kontrolü ekle

2. **TerritoryData.updateYBounds()** metodunu çağır
   - Klan kurulurken
   - Klan alanı güncellenirken
   - Çit eklenip kaldırılırken

3. **Partikül sistemi** güncelle
   - Y ekseni sınırlarını dikkate al
   - minY ve maxY kullan

---

## ÇÖZÜM ÖNERİLERİ VE UYGULAMA PLANI

### FAZE 1: Y Ekseni Sınırları Hesaplama ve Kullanımı

#### 1.1 TerritoryManager Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/manager/TerritoryManager.java`

**Değişiklikler:**
```java
public Clan getTerritoryOwner(Location loc) {
    // Mevcut kod...
    
    // ✅ YENİ: TerritoryData.isInsideTerritory() kullan
    for (Clan clan : clanManager.getAllClans()) {
        Territory t = clan.getTerritory();
        if (t == null) continue;
        
        // TerritoryData al
        TerritoryBoundaryManager boundaryManager = ...; // Inject edilmeli
        TerritoryData data = boundaryManager.getTerritoryData(clan);
        
        if (data != null && data.isInsideTerritory(loc)) {
            // Cache'e ekle
            // ...
            return clan;
        }
    }
    
    return null;
}
```

#### 1.2 TerritoryData.updateYBounds() Çağrıları

**Dosyalar:**
- `TerritoryListener.java` - continueCrystalPlacement()
- `ClanTerritoryMenu.java` - recalculateBoundaries()
- `TerritoryBoundaryManager.java` - addFenceLocation(), removeFenceLocation()

**Değişiklikler:**
```java
// TerritoryListener.java:continueCrystalPlacement()
TerritoryData data = boundaryManager.getTerritoryData(clan);
// Çit lokasyonları eklendikten sonra:
data.updateYBounds(); // ✅ YENİ
data.setSkyHeight(config.getSkyHeight()); // ✅ YENİ
data.setGroundDepth(config.getGroundDepth()); // ✅ YENİ
```

#### 1.3 Partikül Sistemi Güncelleme

**Dosyalar:**
- `TerritoryBoundaryParticleTask.java`
- `TerritoryListener.java` - showTerritoryBoundary()

**Değişiklikler:**
```java
// TerritoryBoundaryParticleTask.java:showBoundaryParticles()
private void showBoundaryParticles(Player player, TerritoryData territoryData) {
    // Mevcut kod...
    
    // ✅ YENİ: Y ekseni sınırlarını dikkate al
    int minY = territoryData.getMinY() - territoryData.getGroundDepth();
    int maxY = territoryData.getMaxY() + territoryData.getSkyHeight();
    int playerY = player.getLocation().getBlockY();
    
    // Oyuncunun Y seviyesine göre partikül göster
    for (Location boundaryLoc : boundaryLine) {
        // Y koordinatını oyuncunun Y seviyesine göre ayarla
        int particleY = Math.max(minY, Math.min(maxY, playerY));
        Location particleLoc = boundaryLoc.clone();
        particleLoc.setY(particleY);
        
        // Partikül göster
        // ...
    }
}
```

### FAZE 2: Klan Alanı Koruma Sistemi Düzeltme

#### 2.1 TerritoryManager.getTerritoryOwner() Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/manager/TerritoryManager.java`

**Değişiklikler:**
- `TerritoryData.isInsideTerritory()` metodunu kullan
- Y ekseni kontrolü ekle

#### 2.2 TerritoryListener.onBreak() ve onBlockPlaceInTerritory() Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

**Değişiklikler:**
- `TerritoryManager.getTerritoryOwner()` zaten güncellenecek
- Ek bir değişiklik gerekmiyor

### FAZE 3: Oyuncu Özellik Kontrol Sistemi

#### 3.1 PlayerFeatureMonitor Sınıfı Oluştur

**Dosya:** `src/main/java/me/mami/stratocraft/task/PlayerFeatureMonitor.java`

**Özellikler:**
- Sürekli çalışan task
- Oyuncu klan üyeliği kontrolü
- Buff kontrolü
- Partikül kontrolü
- Cache sistemi

**Kod:**
```java
package me.mami.stratocraft.task;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.BuffManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerFeatureMonitor {
    private final Main plugin;
    private final ClanManager clanManager;
    private final BuffManager buffManager;
    
    // Cache: Player UUID -> Clan ID
    private final Map<UUID, UUID> playerClanCache = new ConcurrentHashMap<>();
    
    private int taskId = -1;
    private static final long UPDATE_INTERVAL = 20L; // 1 saniye
    
    public PlayerFeatureMonitor(Main plugin, ClanManager clanManager, BuffManager buffManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.buffManager = buffManager;
    }
    
    public void start() {
        if (taskId != -1) {
            stop();
        }
        
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::run, 0L, UPDATE_INTERVAL).getTaskId();
    }
    
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    private void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            
            UUID playerId = player.getUniqueId();
            Clan clan = clanManager.getClanByPlayer(playerId);
            
            // Cache güncelle
            UUID cachedClanId = playerClanCache.get(playerId);
            if (clan != null) {
                UUID currentClanId = clan.getId();
                if (!currentClanId.equals(cachedClanId)) {
                    // Klan değişti, cache güncelle
                    playerClanCache.put(playerId, currentClanId);
                    // Buff'ları güncelle
                    updatePlayerBuffs(player, clan);
                }
            } else {
                if (cachedClanId != null) {
                    // Klandan ayrıldı, cache temizle
                    playerClanCache.remove(playerId);
                    // Buff'ları temizle
                    clearPlayerBuffs(player);
                }
            }
            
            // Sürekli kontrol: Buff'lar, partiküller vb.
            if (clan != null) {
                checkPlayerBuffs(player, clan);
                // Diğer kontroller...
            }
        }
    }
    
    private void updatePlayerBuffs(Player player, Clan clan) {
        if (buffManager != null) {
            buffManager.checkBuffsOnJoin(player, clan);
        }
    }
    
    private void clearPlayerBuffs(Player player) {
        // Buff'ları temizle
    }
    
    private void checkPlayerBuffs(Player player, Clan clan) {
        // Sürekli buff kontrolü
    }
    
    public UUID getCachedClanId(UUID playerId) {
        return playerClanCache.get(playerId);
    }
}
```

#### 3.2 Main.java'da PlayerFeatureMonitor Başlat

**Dosya:** `src/main/java/me/mami/stratocraft/Main.java`

**Değişiklikler:**
```java
private PlayerFeatureMonitor playerFeatureMonitor;

public void onEnable() {
    // Mevcut kod...
    
    // ✅ YENİ: PlayerFeatureMonitor başlat
    playerFeatureMonitor = new PlayerFeatureMonitor(
        this, 
        clanManager, 
        buffManager
    );
    playerFeatureMonitor.start();
}

public void onDisable() {
    // Mevcut kod...
    
    // ✅ YENİ: PlayerFeatureMonitor durdur
    if (playerFeatureMonitor != null) {
        playerFeatureMonitor.stop();
    }
}
```

### FAZE 4: Klan Alanı Güncelleme Sistemi

#### 4.1 ClanTerritoryMenu.recalculateBoundaries() Güncelleme

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ClanTerritoryMenu.java`

**Değişiklikler:**
```java
private void recalculateBoundaries(Player player, Clan clan) {
    // Mevcut kod...
    
    // ✅ YENİ: Eski çit lokasyonlarını dünyadan kontrol et
    List<Location> oldFenceLocations = new ArrayList<>(territoryData.getFenceLocations());
    for (Location oldFenceLoc : oldFenceLocations) {
        if (oldFenceLoc.getWorld() != null) {
            Block block = oldFenceLoc.getBlock();
            // Eğer çit hala klan çiti ise, yeni listeye ekle
            if (block.getType() == Material.OAK_FENCE && 
                CustomBlockData.isClanFence(block)) {
                // Zaten yeni listede olacak
            } else {
                // Çit kırılmış veya değişmiş, TerritoryData'dan kaldır
                territoryData.removeFenceLocation(oldFenceLoc);
            }
        }
    }
    
    // ✅ YENİ: Y ekseni sınırlarını güncelle
    territoryData.updateYBounds();
    territoryData.setSkyHeight(config.getSkyHeight());
    territoryData.setGroundDepth(config.getGroundDepth());
    
    // Mevcut kod...
}
```

#### 4.2 collectFenceLocations() Metodu Oluştur veya Güncelle

**Dosya:** `src/main/java/me/mami/stratocraft/gui/ClanTerritoryMenu.java`

**Kod:**
```java
private List<Location> collectFenceLocations(Location center, Clan clan) {
    List<Location> fenceLocations = new ArrayList<>();
    
    // Klan kristalini çevreleyen çitleri topla
    // 3D flood-fill ile tüm çitleri bul
    Set<Block> visited = new HashSet<>();
    Queue<Block> queue = new LinkedList<>();
    
    Block centerBlock = center.getBlock();
    queue.add(centerBlock);
    visited.add(centerBlock);
    
    int maxIterations = 10000;
    int iterations = 0;
    
    while (!queue.isEmpty() && iterations < maxIterations) {
        Block current = queue.poll();
        iterations++;
        
        BlockFace[] faces = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
        };
        
        for (BlockFace face : faces) {
            Block neighbor = current.getRelative(face);
            if (visited.contains(neighbor)) continue;
            
            Material type = neighbor.getType();
            
            if (type == Material.OAK_FENCE) {
                if (CustomBlockData.isClanFence(neighbor)) {
                    UUID fenceClanId = CustomBlockData.getClanFenceData(neighbor);
                    if (fenceClanId != null && fenceClanId.equals(clan.getId())) {
                        fenceLocations.add(neighbor.getLocation());
                        visited.add(neighbor);
                        continue;
                    }
                }
            }
            
            if (type != Material.AIR && 
                type != Material.CAVE_AIR && 
                type != Material.VOID_AIR) {
                visited.add(neighbor);
                continue;
            }
            
            visited.add(neighbor);
            queue.add(neighbor);
        }
    }
    
    return fenceLocations;
}
```

### FAZE 5: Test ve Doğrulama

#### 5.1 Test Senaryoları

1. **Klan Kurma Testi:**
   - Yükseklik farkı olan çitlerle klan kurma
   - Havada çitlerle klan kurma
   - Y ekseni sınırlarının doğru hesaplanması

2. **Klan Alanı Koruma Testi:**
   - Y ekseninde koruma testi
   - Başka oyuncuların blok kırma/koyma engelleme
   - Klan üyelerinin blok kırma/koyma izni

3. **Klan Sınırları Görselleştirme Testi:**
   - Partikül sisteminin Y ekseninde çalışması
   - Klan üyelerinin sınırları görmesi
   - Performans testi

4. **Klan Alanı Güncelleme Testi:**
   - Tek tuş ile alan güncelleme
   - Eski çit lokasyonlarının temizlenmesi
   - Yeni çit lokasyonlarının eklenmesi

---

## KAYNAKÇA

### İnternet Araştırması

1. **Minecraft Plugin Territory System:**
   - Factions Plugin Documentation
   - GriefPrevention Plugin Documentation
   - WorldGuard Plugin Documentation

2. **3D Flood-Fill Algorithm:**
   - Computer Graphics Algorithms
   - Pathfinding Algorithms
   - Minecraft Plugin Development Forums

3. **Bukkit Persistent Data:**
   - Bukkit API Documentation
   - Spigot API Documentation
   - Plugin Development Best Practices

4. **Performance Optimization:**
   - Minecraft Server Performance Optimization
   - Java Concurrency Best Practices
   - Cache System Design Patterns

### Kod Referansları

1. **TerritoryManager.java** - Alan kontrolü ve cache sistemi
2. **TerritoryData.java** - Alan veri modeli ve Y ekseni kontrolü
3. **TerritoryListener.java** - Event handling ve koruma sistemi
4. **TerritoryBoundaryManager.java** - Sınır hesaplama ve yönetimi
5. **ClanTerritoryMenu.java** - GUI ve alan güncelleme
6. **TerritoryBoundaryParticleTask.java** - Partikül sistemi

---

## DETAYLI KOD ANALİZLERİ

### findAndAddFenceLocations() Metodu

**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java:1953`

**Mevcut Kod:**
```java
private void findAndAddFenceLocations(Location centerLocation, TerritoryData territoryData) {
    // 3D flood-fill ile çitleri bul
    Set<Block> visited = new HashSet<>();
    Queue<Block> queue = new LinkedList<>();
    
    Block centerBlock = centerLocation.getBlock();
    queue.add(centerBlock);
    visited.add(centerBlock);
    
    int maxIterations = 10000;
    int iterations = 0;
    
    while (!queue.isEmpty() && iterations < maxIterations) {
        Block current = queue.poll();
        iterations++;
        
        BlockFace[] faces = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
        };
        
        for (BlockFace face : faces) {
            Block neighbor = current.getRelative(face);
            if (visited.contains(neighbor)) continue;
            
            Material type = neighbor.getType();
            
            if (type == Material.OAK_FENCE) {
                if (isClanFenceFast(neighbor)) {
                    territoryData.addFenceLocation(neighbor.getLocation());
                    visited.add(neighbor);
                    continue;
                }
            }
            
            if (type != Material.AIR && 
                type != Material.CAVE_AIR && 
                type != Material.VOID_AIR) {
                visited.add(neighbor);
                continue;
            }
            
            visited.add(neighbor);
            queue.add(neighbor);
        }
    }
    
    // ❌ SORUN: updateYBounds() çağrılmıyor!
    // ❌ SORUN: calculateBoundaries() çağrılmıyor!
}
```

**Sorun:**
- Çit lokasyonları ekleniyor ✅
- Ancak `updateYBounds()` çağrılmıyor ❌
- `calculateBoundaries()` çağrılmıyor ❌
- Y ekseni sınırları güncellenmiyor ❌

**Çözüm:**
```java
// Metodun sonuna ekle:
territoryData.updateYBounds(); // ✅ Y ekseni sınırlarını güncelle
territoryData.calculateBoundaries(); // ✅ Sınır koordinatlarını hesapla
```

---

## İNTERNET ARAŞTIRMASI SONUÇLARI

### 1. Minecraft Plugin Territory System Best Practices

**Kaynak:** SpigotMC Forums, Bukkit Forums

**Bulunan Çözümler:**
1. **3D Flood-Fill Algoritması:**
   - Y ekseni kontrolü için 6 yöne bakılmalı (NORTH, SOUTH, EAST, WEST, UP, DOWN)
   - Yükseklik toleransı kullanılmalı
   - Performans için async işlem yapılmalı

2. **Territory Protection:**
   - Chunk-based cache kullanılmalı
   - Y ekseni kontrolü yapılmalı
   - Event-based cache güncelleme yapılmalı

3. **Boundary Visualization:**
   - Partikül sistemi async olmalı
   - Y ekseni sınırları dikkate alınmalı
   - Cooldown sistemi kullanılmalı

### 2. Performance Optimization Techniques

**Kaynak:** Java Concurrency Best Practices, Minecraft Server Optimization

**Bulunan Çözümler:**
1. **Cache System:**
   - ConcurrentHashMap kullanılmalı
   - Event-based cache güncelleme yapılmalı
   - Cache invalidation stratejisi olmalı

2. **Async Processing:**
   - Büyük işlemler async yapılmalı
   - Main thread bloke edilmemeli
   - Callback pattern kullanılmalı

3. **Player Feature Monitoring:**
   - Sürekli çalışan task kullanılmalı
   - Cache sistemi olmalı
   - Event-based güncelleme yapılmalı

---

## SONUÇ

Bu doküman, klan sistemindeki tüm sorunları analiz etmiş ve çözüm önerileri sunmuştur. Öncelik sırası:

1. **FAZE 1**: Y Ekseni Sınırları (KRİTİK)
   - `updateYBounds()` çağrıları ekle
   - `TerritoryManager.getTerritoryOwner()` güncelle
   - Partikül sistemi güncelle

2. **FAZE 2**: Klan Alanı Koruma Sistemi (KRİTİK)
   - `TerritoryData.isInsideTerritory()` kullan
   - Y ekseni kontrolü ekle
   - Cache sistemi güncelle

3. **FAZE 3**: Oyuncu Özellik Kontrol Sistemi (ÖNEMLİ)
   - `PlayerFeatureMonitor` sınıfı oluştur
   - Cache sistemi ekle
   - Sürekli çalışan task başlat

4. **FAZE 4**: Klan Alanı Güncelleme Sistemi (ÖNEMLİ)
   - `recalculateBoundaries()` güncelle
   - `collectFenceLocations()` oluştur veya güncelle
   - Y ekseni sınırlarını güncelle

5. **FAZE 5**: Test ve Doğrulama (GEREKLİ)
   - Tüm test senaryolarını çalıştır
   - Performans testleri yap
   - Oyuncu geri bildirimleri topla

Her fazın uygulanması sonrası test edilmeli ve doğrulanmalıdır.

---

## EK NOTLAR

### Önemli Dosyalar

1. **TerritoryManager.java** - Alan kontrolü ve cache sistemi
2. **TerritoryData.java** - Alan veri modeli ve Y ekseni kontrolü
3. **TerritoryListener.java** - Event handling ve koruma sistemi
4. **TerritoryBoundaryManager.java** - Sınır hesaplama ve yönetimi
5. **ClanTerritoryMenu.java** - GUI ve alan güncelleme
6. **TerritoryBoundaryParticleTask.java** - Partikül sistemi

### Önemli Metodlar

1. **TerritoryData.updateYBounds()** - Y ekseni sınırlarını güncelle
2. **TerritoryData.isInsideTerritory()** - 3D alan kontrolü
3. **TerritoryManager.getTerritoryOwner()** - Alan sahibi bulma
4. **isSurroundedByClanFences3D()** - 3D flood-fill algoritması
5. **findAndAddFenceLocations()** - Çit lokasyonlarını topla

### Config Ayarları

1. **skyHeight**: 150 (en yukarıdaki çitten 150 blok yukarı)
2. **groundDepth**: 20 (en alttaki çitten 20 blok aşağı)
3. **fenceHeightTolerance**: 5 (çitler arası maksimum yükseklik farkı)
4. **fenceConnectionRequired**: true (çitlerin bağlantılı olması gerekli mi?)

