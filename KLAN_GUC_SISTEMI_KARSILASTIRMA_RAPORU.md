# KLAN GÜÇ SİSTEMİ - KARŞILAŞTIRMA VE OPTİMİZASYON RAPORU

## 📊 İKİ SİSTEMİN KARŞILAŞTIRMASI

### 1. PUANLAMA DEĞERLERİ KARŞILAŞTIRMASI

#### A. Özel Eşyalar (Silah + Zırh)

**SİSTEM 1 (Benim Sistemim):**
- Silah: 60, 150, 400, 900, 1500
- Zırh: 40, 100, 250, 600, 1000 (parça başına)
- **Tam Set Seviye 5:** 1500 + (1000 × 4) = **5,500 Puan**

**SİSTEM 2 (Diğer AI):**
- Silah: 50, 150, 400, 900, 1800
- Zırh: (Belirtilmemiş, muhtemelen silahın %60-70'i)
- **Tam Set Seviye 5:** ~9,000 Puan (tahmini)

**ANALİZ:**
- Sistem 2 daha yüksek puanlar veriyor (enflasyon riski)
- Sistem 1 daha dengeli görünüyor
- **ÖNERİ:** Sistem 1'in puanlarını kullan, ama Seviye 5 için biraz artır (1800 yerine 1500 kalabilir)

---

#### B. Ritüel Blokları

**SİSTEM 1:**
- Demir: 8, Altın: 12, Elmas: 25, Obsidyen: 30, Zümrüt: 35

**SİSTEM 2:**
- Demir: 10, Elmas: 35, Zümrüt: 25, Obsidyen: 5, Netherite/Titanyum: 150

**ANALİZ:**
- Sistem 2'de Obsidyen çok düşük (5), bu mantıksız (Obsidyen daha değerli olmalı)
- Sistem 2'de Netherite/Titanyum çok yüksek (150) - bu iyi bir fikir
- **ÖNERİ:** Sistem 1'in değerlerini kullan, ama Titanyum/Netherite için 150 ekle

---

#### C. Ritüel Kaynakları

**SİSTEM 1:**
- Demir: 5, Elmas: 10, Kızıl Elmas: 18, Titanyum: 15, Karanlık Madde: 50

**SİSTEM 2:**
- (Belirtilmemiş, sadece "envanterdeki kaynaklar" diyor)

**ANALİZ:**
- Sistem 1 daha detaylı ve mantıklı
- **ÖNERİ:** Sistem 1'in değerlerini kullan

---

#### D. Antrenman/Ustalık

**SİSTEM 1:**
- Formül: `100 × (masteryPercent / 100)^1.5`
- %150 = ~183 puan
- %200 = ~283 puan
- %300 = ~520 puan

**SİSTEM 2:**
- Formül: `(masteryPercent - 100)^1.2 × 10`
- %150 = ~350 puan
- %200 = ~1000 puan
- %300 = ~2800 puan

**ANALİZ:**
- Sistem 2 çok daha agresif (3-5 kat daha fazla puan)
- Bu enflasyona yol açabilir
- **ÖNERİ:** Sistem 1'in formülünü kullan, ama çarpanı biraz artır (100 → 150)

---

#### E. Klan Yapıları

**SİSTEM 1:**
- 1: 100, 2: 250, 3: 500, 4: 1200, 5: 2000

**SİSTEM 2:**
- Klan Kristali: 500 (sabit)
- Yapılar: Seviye × 500 (1: 500, 3: 1500, 5: 2500)

**ANALİZ:**
- Sistem 2 daha yüksek puanlar veriyor
- Sistem 1 daha kademeli artış gösteriyor
- **ÖNERİ:** Sistem 1'in değerlerini kullan, ama Klan Kristali için +500 sabit puan ekle

---

### 2. SEVİYE ALGORİTMASI KARŞILAŞTIRMASI

#### SİSTEM 1 (Logaritmik - Log10):
```
Seviye = floor(log10(power / 100) × 2.5) + 1
```
- 1,000 puan = Seviye 4
- 5,000 puan = Seviye 7
- 10,000 puan = Seviye 8
- 50,000 puan = Seviye 11
- 200,000 puan = Seviye 13

#### SİSTEM 2 (Karekök):
```
Seviye = sqrt(power / 100)
```
- 1,000 puan = Seviye 3
- 5,000 puan = Seviye 7
- 10,000 puan = Seviye 10
- 50,000 puan = Seviye 22
- 200,000 puan = Seviye 44

**ANALİZ:**
- Sistem 2 (Karekök) daha hızlı seviye atlama sağlıyor (oyuncu motivasyonu için iyi)
- Sistem 1 (Log10) daha yavaş seviye atlama (daha dengeli, elit seviyeler zor)
- **ÖNERİ:** **HİBRİT SİSTEM** - İlk 10 seviye için karekök (hızlı ilerleme), sonrası için logaritmik (zor ilerleme)

---

### 3. KORUMA SİSTEMİ KARŞILAŞTIRMASI

#### SİSTEM 1:
- Normal: Hedef, saldıranın %50'sinden düşükse saldırı yapılamaz
- Klan içi: %60 eşik

#### SİSTEM 2:
- Normal: Hedef, saldıranın %33'ünden (1/3) düşükse saldırı yapılamaz
- Acemi koruması: 5,000 puan altı için özel koruma
- Klan savaşı: Koruma devre dışı

**ANALİZ:**
- Sistem 2 daha esnek (acemi koruması, klan savaşı istisnası)
- Sistem 1 daha basit ama yeterli
- **ÖNERİ:** Sistem 2'nin özelliklerini ekle (acemi koruması + klan savaşı istisnası)

---

### 4. PERFORMANS YAKLAŞIMLARI

#### SİSTEM 1:
- 5 saniye cache
- Her çağrıda cache kontrolü
- Ritüel blokları/kaynakları henüz implement edilmemiş

#### SİSTEM 2:
- InventoryCloseEvent'te hesaplama (çok akıllı!)
- Savaş anında sadece cache okuma
- Asenkron blok taraması (10-20 dakikada bir)

**ANALİZ:**
- Sistem 2'nin performans yaklaşımı çok daha iyi
- InventoryCloseEvent kullanımı mükemmel bir fikir
- **ÖNERİ:** Sistem 2'nin performans yaklaşımını kullan

---

### 5. CONFIG YÖNETİMİ

#### SİSTEM 1:
- ✅ Tam config tabanlı
- ✅ Tüm değerler config.yml'de
- ✅ Kolay dengeleme

#### SİSTEM 2:
- ❌ Config yönetimi belirtilmemiş
- ❌ Kod içinde hardcoded değerler var

**ANALİZ:**
- Sistem 1 çok daha iyi
- **ÖNERİ:** Sistem 1'in config yönetimini kullan

---

## 🎯 OPTİMİZE SİSTEM TASARIMI

### HİBRİT SİSTEM ÖNERİSİ

En iyi özellikleri birleştiren optimize sistem:

#### 1. PUANLAMA DEĞERLERİ

**Özel Eşyalar:**
- Silah: 60, 150, 400, 900, **1600** (biraz artırıldı)
- Zırh: 40, 100, 250, 600, 1000 (parça başına)
- **Tam Set Seviye 5:** 1600 + (1000 × 4) = **5,600 Puan**

**Ritüel Blokları:**
- Demir: 8, Altın: 12, Elmas: 25, Obsidyen: 30, Zümrüt: 35
- **YENİ:** Titanyum/Netherite: 150

**Ritüel Kaynakları:**
- Demir: 5, Elmas: 10, Kızıl Elmas: 18, Titanyum: 15, Karanlık Madde: 50

**Antrenman/Ustalık:**
- Formül: `150 × (masteryPercent / 100)^1.4` (biraz yumuşatıldı)
- %150 = ~250 puan
- %200 = ~400 puan
- %300 = ~700 puan

**Klan Yapıları:**
- Klan Kristali: +500 (sabit bonus)
- Yapılar: 100, 250, 500, 1200, 2000

---

#### 2. SEVİYE ALGORİTMASI (HİBRİT)

**İlk 10 Seviye (Hızlı İlerleme - Karekök):**
```
Seviye = sqrt(power / basePower)
basePower = 100
```

**11+ Seviye (Yavaş İlerleme - Logaritmik):**
```
Seviye = 10 + floor(log10(power / 10000) × 3)
```

**Örnek Seviyeler:**
- 1,000 puan = Seviye 3 (Karekök)
- 5,000 puan = Seviye 7 (Karekök)
- 10,000 puan = Seviye 10 (Karekök)
- 50,000 puan = Seviye 13 (Logaritmik)
- 200,000 puan = Seviye 16 (Logaritmik)

**Avantajlar:**
- Yeni oyuncular hızlı seviye atlar (motivasyon)
- İleri seviyeler zor (prestij)
- Dengeli ilerleme eğrisi

---

#### 3. KORUMA SİSTEMİ (GELİŞTİRİLMİŞ)

**Normal Saldırı:**
- Hedef, saldıranın %50'sinden düşükse saldırı yapılamaz
- **İSTİSNA:** Hedef ilk saldırırsa koruma devre dışı

**Acemi Koruması:**
- 5,000 puan altı oyuncular için özel koruma
- Güçlü oyuncular (10,000+ puan) acemilere saldıramaz
- **AMAÇ:** Yeni oyuncuları korumak

**Klan İçi:**
- %60 eşik (daha yüksek koruma)
- Klan savaşı aktifse koruma devre dışı

**Klan Savaşı:**
- Koruma sistemi tamamen devre dışı
- Herkes herkese saldırabilir

---

#### 4. PERFORMANS OPTİMİZASYONU

**Cache Stratejisi:**
1. **InventoryCloseEvent:** Oyuncu envanteri kapattığında güç hesapla ve cache'e kaydet
2. **Savaş Anında:** Sadece cache'den oku (hesaplama yapma!)
3. **Periyodik Güncelleme:** Her 30 saniyede bir tüm oyuncuların gücünü güncelle (async)
4. **Event-Based Güncelleme:**
   - Item değişikliği (equip/unequip)
   - Yapı kuruldu/yıkıldı
   - Antrenman tamamlandı
   - Oyuncu giriş/çıkış

**Blok Taraması:**
- Asenkron task (her 15 dakikada bir)
- Sadece klan arazisi içindeki blokları tara
- TerritoryManager ile entegre

---

#### 5. SİSTEM MİMARİSİ

**Ana Sınıf: `StratocraftPowerSystem`**
- Güç hesaplama
- Seviye hesaplama
- Koruma kontrolü
- Cache yönetimi

**Config Sınıfı: `StratocraftPowerConfig`**
- Tüm puanlar config'den
- Algoritma parametreleri
- Koruma eşikleri

**Listener: `PowerSystemListener`**
- InventoryCloseEvent
- EntityDamageByEntityEvent (koruma kontrolü)
- PlayerJoinEvent (cache güncelleme)
- StructurePlaceEvent (klan gücü güncelleme)

**Async Task: `PowerUpdateTask`**
- Periyodik güç güncelleme
- Blok taraması
- Cache temizleme

---

## 📈 BEKLENEN SONUÇLAR

### Oyuncu Güç Dağılımı (Tahmini)

**Yeni Oyuncu (1-2 saat):**
- Seviye 1-2 itemler: ~300-500 puan
- Seviye: 2-3

**Orta Seviye Oyuncu (1-2 hafta):**
- Seviye 3-4 itemler: ~2,000-4,000 puan
- Antrenman: ~500-1,000 puan
- Toplam: ~3,000-5,000 puan
- Seviye: 5-7

**İleri Seviye Oyuncu (1-2 ay):**
- Seviye 5 itemler: ~5,600 puan
- Antrenman: ~2,000-3,000 puan
- Ritüel blokları: ~1,000-2,000 puan
- Toplam: ~10,000-15,000 puan
- Seviye: 10-12

**Elit Oyuncu (3+ ay):**
- Maksimum itemler: ~5,600 puan
- Yüksek antrenman: ~5,000-8,000 puan
- Çok ritüel: ~5,000-10,000 puan
- Toplam: ~20,000-50,000 puan
- Seviye: 14-22

---

## ✅ SONUÇ VE ÖNERİLER

### En İyi Özellikler Birleştirildi:

1. ✅ **Puanlama:** Sistem 1'in dengeli değerleri + Sistem 2'nin Titanyum/Netherite fikri
2. ✅ **Seviye Algoritması:** Hibrit sistem (karekök + logaritmik)
3. ✅ **Koruma:** Sistem 2'nin esnek özellikleri (acemi koruması, klan savaşı)
4. ✅ **Performans:** Sistem 2'nin akıllı yaklaşımı (InventoryCloseEvent)
5. ✅ **Config:** Sistem 1'in tam config yönetimi

### Uygulama Önceliği:

1. **FAZ 1:** Temel sistem (item + antrenman gücü)
2. **FAZ 2:** Seviye algoritması ve koruma sistemi
3. **FAZ 3:** Ritüel blokları/kaynakları entegrasyonu
4. **FAZ 4:** Performans optimizasyonu (async task, blok taraması)

---

## 🔧 TEKNİK DETAYLAR

### Cache Yapısı:
```java
Map<UUID, CachedPowerData> playerPowerCache
- power: double
- level: int
- lastUpdate: long
- needsUpdate: boolean
```

### Event Entegrasyonu:
- `InventoryCloseEvent` → Güç hesapla
- `EntityDamageByEntityEvent` → Koruma kontrolü
- `PlayerJoinEvent` → Cache güncelle
- `StructurePlaceEvent` → Klan gücü güncelle

### Async Task:
- Her 30 saniyede bir: Tüm oyuncuların gücünü güncelle
- Her 15 dakikada bir: Klan arazilerindeki blokları tara

---

**Rapor Hazırlayan:** AI Assistant
**Tarih:** 2024
**Versiyon:** 1.0

