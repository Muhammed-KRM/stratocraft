# KLAN SINIR PARTİKÜL SİSTEMİ ANALİZ VE DÜZELTME

## 📋 MEVCUT DURUM ANALİZİ

### 🔍 Hangi Oyuncuya Gösteriliyor?

**Kod:** `TerritoryBoundaryParticleTask.java` - `run()` metodu

```
1. Tüm online oyuncuları döngüye al
2. Oyuncunun klanı var mı? (territoryManager.getClanManager().getClanByPlayer())
   └─> YOK → Atlama
3. TerritoryData var mı?
   └─> YOK → Atlama
4. Oyuncu aynı dünyada mı?
   └─> FARKLI → Atlama
5. Oyuncu klan alanına yakın mı? (visibleDistance + radius)
   └─> UZAK → Atlama
6. ✅ Partikül göster
```

**SONUÇ:** ✅ Sadece klan üyelerine gösteriliyor (DOĞRU)

---

### 🎯 Partiküller Nereden Çıkıyor?

**Kod:** `TerritoryBoundaryParticleTask.java` - `showBoundaryParticles()` metodu

**MEVCUT MANTIK:**
```
1. TerritoryData.getBoundaryLine() → 2D (X-Z) koordinatları al
2. Her boundaryLoc için:
   - Oyuncuya mesafe kontrolü (visibleDistance)
   - Partikül arası mesafe kontrolü (spacing)
   - Y koordinatı: effectiveY + random(-2, +2)
     └─> effectiveY = Math.max(minY, Math.min(maxY, playerY))
3. Partikül göster
```

**SORUN:** ❌
- Partiküller sadece oyuncunun Y seviyesine yakın gösteriliyor
- Tüm Y ekseni sınırları (minY'den maxY'ye kadar) gösterilmiyor
- Sadece 2D (X-Z) sınır çizgisi boyunca partikül var
- Y ekseni boyunca dikey "duvar" görünümü yok

---

## 🐛 TESPİT EDİLEN SORUNLAR

### 1. ❌ Y Ekseni Sınırları Tam Kullanılmıyor

**Mevcut Kod (Satır 166):**
```java
double y = Math.max(minY, Math.min(maxY, effectiveY + (Math.random() * 4 - 2)));
```

**Sorun:**
- Sadece oyuncunun Y seviyesine ±2 blok aralığında partikül gösteriliyor
- minY'den maxY'ye kadar tüm Y ekseni boyunca partikül gösterilmiyor
- Klan alanının dikey sınırları görünmüyor

### 2. ❌ 2D Sınır Çizgisi Yetersiz

**Mevcut Kod:**
```java
List<Location> boundaryLine = territoryData.getBoundaryLine(); // Sadece X-Z koordinatları
```

**Sorun:**
- Sadece 2D (X-Z) sınır çizgisi boyunca partikül var
- Y ekseni boyunca dikey bir "duvar" görünümü yok
- Oyuncu yukarı veya aşağı baktığında sınırları göremiyor

---

## ✅ DÜZELTME PLANI

### 1. Y Ekseni Boyunca Partikül Gösterimi

**Yeni Mantık:**
```
Her X-Z koordinatında:
  - minY'den maxY'ye kadar (belirli aralıklarla) partikül göster
  - Y ekseni aralığı: Her 5-10 blokta bir partikül (config'den ayarlanabilir)
  - Performans: Sadece oyuncunun görüş mesafesi içindeki partikülleri göster
```

### 2. 3D Sınır Görselleştirme

**Yeni Mantık:**
```
- Her boundaryLoc (X-Z) için:
  - Y ekseni boyunca partikül göster (minY → maxY)
  - Dikey "duvar" görünümü
  - Oyuncunun görüş açısına göre optimize et
```

---

## 📊 MEVCUT AKIŞ DİYAGRAMI

```
┌─────────────────────────────────────────────────────────┐
│ TerritoryBoundaryParticleTask.run()                    │
│ (Her 20 tick'te bir çalışır)                           │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Tüm Online Oyuncuları Döngüye Al │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Oyuncunun Klanı Var mı?       │
        └───────────────────────────────┘
            │                    │
         EVET                  HAYIR
            │                    │
            ▼                    ▼
    ┌──────────────┐      ┌──────────┐
    │ TerritoryData│      │  ATLA    │
    │     Var mı?  │      └──────────┘
    └──────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────┐
    │ Aynı Dünyada mı?     │
    └──────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ Oyuncu Klan Alanına Yakın mı? │
    │ (visibleDistance + radius)    │
    └──────────────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ showBoundaryParticles()       │
    └──────────────────────────────┘
            │
            ▼
    ┌──────────────────────────────┐
    │ getBoundaryLine()             │
    │ (2D X-Z koordinatları)        │
    └──────────────────────────────┘
            │
            ▼
    ┌──────────────────────────────┐
    │ Her boundaryLoc için:        │
    │ 1. Mesafe kontrolü            │
    │ 2. Spacing kontrolü           │
    │ 3. Y = effectiveY ± random(2) │ ❌ SORUN: Sadece oyuncu Y seviyesi
    │ 4. Partikül göster            │
    └──────────────────────────────┘
```

---

## 🔧 DÜZELTİLMİŞ KOD

### TerritoryBoundaryParticleTask.java - showBoundaryParticles()

**YENİ MANTIK:**
```java
// Y ekseni boyunca partikül göster (minY'den maxY'ye kadar)
int yStep = 5; // Her 5 blokta bir partikül (config'den ayarlanabilir)
for (int y = minY; y <= maxY; y += yStep) {
    // Her Y seviyesinde partikül göster
    Location particleLoc = boundaryLoc.clone();
    particleLoc.setY(y);
    
    // Oyuncuya mesafe kontrolü (3D mesafe)
    if (playerLoc.distance(particleLoc) > visibleDistance) {
        continue;
    }
    
    // Partikül göster
    player.spawnParticle(...);
}
```

---

## 📊 DÜZELTİLMİŞ AKIŞ DİYAGRAMI (SON HAL)

### Ana Akış

```
┌─────────────────────────────────────────────────────────┐
│ TerritoryBoundaryParticleTask.run()                     │
│ (Her 20 tick'te bir çalışır - config'den ayarlanabilir)│
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Tüm Online Oyuncuları Döngüye Al │
        │ for (Player player : Bukkit.getOnlinePlayers()) │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Oyuncu null veya offline mı?   │
        └───────────────────────────────┘
            │                    │
         HAYIR                 EVET
            │                    │
            ▼                    ▼
    ┌──────────────────┐  ┌──────────┐
    │ Oyuncunun Klanı   │  │  ATLA    │
    │ Var mı?           │  └──────────┘
    │ getClanByPlayer() │
    └──────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ TerritoryData Var mı?         │
    │ getTerritoryData(playerClan)   │
    └──────────────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ Center Var mı?                │
    │ Aynı Dünyada mı?             │
    └──────────────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ Oyuncu Klan Alanına Yakın mı? │
    │ distanceToCenter <=           │
    │   (visibleDistance + radius)  │
    └──────────────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ showBoundaryParticles()       │
    │ (player, territoryData)       │
    └──────────────────────────────┘
```

### showBoundaryParticles() Detaylı Akış

```
┌─────────────────────────────────────────────────────────┐
│ showBoundaryParticles(Player, TerritoryData)           │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Null Kontrolü                  │
        │ (player, territoryData)       │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Dünya Kontrolü                │
        │ (playerLoc.getWorld() ==      │
        │  center.getWorld())           │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ 2D Mesafe Kontrolü            │
        │ (distanceToCenter <=          │
        │  visibleDistance + radius)     │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ getBoundaryLine()             │
        │ (2D X-Z koordinatları)       │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Y Ekseni Sınırlarını Al       │
        │ minY = getMinY() - groundDepth│
        │ maxY = getMaxY() + skyHeight  │
        └──────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Her boundaryLoc (X-Z) için:   │
        │                               │
        │   ┌───────────────────────┐  │
        │   │ 2D Mesafe Kontrolü    │  │ ✅ Optimizasyon
        │   │ (X-Z düzlemi)         │  │
        │   └───────────────────────┘  │
        │           │                   │
        │         EVET                  │
        │           │                   │
        │           ▼                   │
        │   ┌───────────────────────┐  │
        │   │ Spacing Kontrolü       │  │ ✅ Performans
        │   │ (particleCount % spacing)│
        │   └───────────────────────┘  │
        │           │                   │
        │         EVET                  │
        │           │                   │
        │           ▼                   │
        │   ┌───────────────────────┐  │
        │   │ Y = minY'den maxY'ye  │  │ ✅ DÜZELTME
        │   │ (her 5 blokta bir)    │  │ Tüm Y ekseni
        │   │ for (y = minY; y <=   │  │
        │   │      maxY; y += 5)    │  │
        │   └───────────────────────┘  │
        │           │                   │
        │           ▼                   │
        │   ┌───────────────────────┐  │
        │   │ 3D Mesafe Kontrolü    │  │ ✅ DÜZELTME
        │   │ (playerLoc.distance   │  │ 3D mesafe
        │   │  (particleLoc))       │  │
        │   └───────────────────────┘  │
        │           │                   │
        │         EVET                  │
        │           │                   │
        │           ▼                   │
        │   ┌───────────────────────┐  │
        │   │ Partikül Göster       │  │ ✅ DÜZELTME
        │   │ Location: (X, Y, Z)   │  │ Y ekseni boyunca
        │   │ spawnParticle(...)     │  │
        │   └───────────────────────┘  │
        │           │                   │
        │           ▼                   │
        │   ┌───────────────────────┐  │
        │   │ Max Particle Kontrolü │  │ ✅ Performans
        │   │ (particleCount >= max)│  │
        │   └───────────────────────┘  │
        └───────────────────────────────┘
```

## 📊 DÜZELTİLMİŞ AKIŞ DİYAGRAMI (ÖNCEKİ VERSİYON)

```
┌─────────────────────────────────────────────────────────┐
│ TerritoryBoundaryParticleTask.run()                    │
│ (Her 20 tick'te bir çalışır)                           │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Tüm Online Oyuncuları Döngüye Al │
        └───────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────────┐
        │ Oyuncunun Klanı Var mı?       │
        └───────────────────────────────┘
            │                    │
         EVET                  HAYIR
            │                    │
            ▼                    ▼
    ┌──────────────┐      ┌──────────┐
    │ TerritoryData│      │  ATLA    │
    │     Var mı?  │      └──────────┘
    └──────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────┐
    │ Aynı Dünyada mı?     │
    └──────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ Oyuncu Klan Alanına Yakın mı? │
    │ (visibleDistance + radius)    │
    └──────────────────────────────┘
            │
         EVET
            │
            ▼
    ┌──────────────────────────────┐
    │ showBoundaryParticles()       │
    └──────────────────────────────┘
            │
            ▼
    ┌──────────────────────────────┐
    │ getBoundaryLine()             │
    │ (2D X-Z koordinatları)        │
    └──────────────────────────────┘
            │
            ▼
    ┌──────────────────────────────┐
    │ Her boundaryLoc (X-Z) için:   │
    │                               │
    │   ┌───────────────────────┐  │
    │   │ Y = minY'den maxY'ye   │  │ ✅ DÜZELTME: Tüm Y ekseni
    │   │ (her 5 blokta bir)    │  │
    │   └───────────────────────┘  │
    │           │                   │
    │           ▼                   │
    │   ┌───────────────────────┐  │
    │   │ 3D Mesafe Kontrolü    │  │ ✅ DÜZELTME: 3D mesafe
    │   │ (playerLoc.distance)  │  │
    │   └───────────────────────┘  │
    │           │                   │
    │         EVET                  │
    │           │                   │
    │           ▼                   │
    │   ┌───────────────────────┐  │
    │   │ Partikül Göster       │  │ ✅ DÜZELTME: Y ekseni boyunca
    │   │ (Location: X, Y, Z)   │  │
    │   └───────────────────────┘  │
    └──────────────────────────────┘
```

---

## ✅ DÜZELTME SONRASI

### Hangi Oyuncuya Gösteriliyor?
✅ **DOĞRU:** Sadece klan üyelerine gösteriliyor

**Kod Mantığı:**
```java
// TerritoryBoundaryParticleTask.java - run()
Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
if (playerClan == null) continue; // Klan üyesi değilse atla
```

### Partiküller Nereden Çıkıyor?
✅ **DÜZELTME:**
- **X-Z Koordinatları:** `TerritoryData.getBoundaryLine()` → 2D sınır çizgisi
- **Y Koordinatları:** minY'den maxY'ye kadar (her 5 blokta bir)
- **3D Konum:** Her (X, Y, Z) kombinasyonunda partikül
- **Mesafe Kontrolü:** 2D (optimizasyon) + 3D (kesin kontrol)
- **Performans:** Sadece görüş mesafesi içindeki partiküller

**Kod Mantığı:**
```java
// TerritoryBoundaryParticleTask.java - showBoundaryParticles()
int minY = territoryData.getMinY() - territoryData.getGroundDepth();
int maxY = territoryData.getMaxY() + territoryData.getSkyHeight();
int yStep = 5; // Her 5 blokta bir partikül

for (Location boundaryLoc : boundaryLine) {
    // 2D mesafe kontrolü (optimizasyon)
    if (distance2D > visibleDistance) continue;
    
    // Y ekseni boyunca partikül göster
    for (int y = minY; y <= maxY; y += yStep) {
        Location particleLoc = boundaryLoc.clone();
        particleLoc.setY(y);
        
        // 3D mesafe kontrolü
        if (playerLoc.distance(particleLoc) > visibleDistance) continue;
        
        // Partikül göster
        player.spawnParticle(...);
    }
}
```

### Görselleştirme
✅ **YENİ:**
- **Dikey "Duvar" Görünümü:** Y ekseni boyunca partiküller
- **Tüm Y Sınırları Görünür:** minY'den maxY'ye kadar
- **3D Sınır:** Oyuncu yukarı veya aşağı baktığında sınırları görebilir
- **Performans Optimize:** 2D ön kontrol + 3D kesin kontrol

### Performans İyileştirmeleri
✅ **YENİ:**
- 2D mesafe kontrolü önce (hızlı)
- 3D mesafe kontrolü sonra (kesin)
- Partikül limiti (maxParticles)
- Spacing kontrolü (X-Z düzleminde)

---

## 📝 ÖZET

### ✅ ÇALIŞIYOR MU?

**EVET!** Sistem şu şekilde çalışıyor:

1. **Oyuncu Seçimi:** ✅ Sadece klan üyelerine gösteriliyor
2. **Partikül Konumu:** ✅ Tüm Y ekseni sınırları boyunca gösteriliyor
3. **Performans:** ✅ Optimize edilmiş (2D + 3D mesafe kontrolü)
4. **Görselleştirme:** ✅ 3D dikey "duvar" görünümü

### 🔧 YAPILAN DÜZELTMELER

1. ✅ Y ekseni boyunca partikül gösterimi (minY → maxY)
2. ✅ 3D mesafe kontrolü eklendi
3. ✅ 2D ön kontrol ile performans optimizasyonu
4. ✅ Partikül limiti ve spacing kontrolü

### 📊 SON HALİN AKIŞ DİYAGRAMI

Yukarıdaki "DÜZELTİLMİŞ AKIŞ DİYAGRAMI (SON HAL)" bölümüne bakınız.

