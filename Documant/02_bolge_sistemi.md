# STRATOCRAFT - BÖLGE SİSTEMİ

## 🗺️ Bölge Sistemi Nedir?

Bölge, **Klan Çitinin çevrelediği alandan** oluşur. Sadece bu alanda yapılar kurabilir ve felaketlerden korunabilirsiniz.

**Son Güncellemeler** ⭐:
- ✅ **Territory Boundary Particle Sistemi**: Dinamik partikül yoğunluğu (oyuncuya yakınken daha yoğun)
- ✅ **Flood Fill Algoritması**: 2.5D ve 2D flood fill ile çit tespiti ve alan hesaplama
- ✅ **Territory Persistence**: Bölge sınırları veritabanında saklanır, sunucu restart sonrası otomatik restore edilir
- ✅ **Performans Optimizasyonları**: Chunk-based cache, async hesaplama, rate limiting

---

## 📋 İÇİNDEKİLER

1. [Bölge Oluşturma](#bölge-oluşturma)
2. [Bölge Genişletme](#bölge-genişletme)
3. [Bölge Korumaları](#bölge-korumalari)
4. [Offline Koruma](#offline-koruma)
5. [Kristal Yönetimi](#kristal-yönetimi)
6. [Territory Boundary Particle Sistemi](#territory-boundary-particle-sistemi) ⭐ YENİ
7. [Son Güncellemeler](#son-güncellemeler-son-3-gün) ⭐ YENİ

---

## 🏗️ BÖLGE OLUŞTURMA

### ⚠️ Önemli Not: Çit Tespiti Sistemi ⭐ YENİ

**Yeni Algoritma:**
- Sistem artık klan kristalinden başlayarak tüm yönlere yayılır
- Klan çitleri (OAK_FENCE) ile karşılaşınca durur
- Kapalı alan tespit edilirse → Bölge oluşur
- Açık alan ise → "Çitlerle tam çevrele!" hatası

**Flood Fill Algoritması:**
```
1. Klan kristali yerleştirilir
2. Sistem kristalden başlayarak tüm yönlere yayılır (2.5D flood fill)
3. Klan Çiti (OAK_FENCE) ile karşılaşınca durur
4. Kapalı alan tespit edilirse → Bölge oluşur
5. Açık alan ise → Hata verir
```

**Performans Optimizasyonları:**
- ✅ Chunk-based cache (territory cache)
- ✅ Async hesaplama (büyük alanlar için)
- ✅ Rate limiting (lag önleme)
- ✅ Max radius kontrolü (sonsuz döngü önleme)

### Sistem Nasıl Çalışır?

**Flood-Fill Algoritması**:
```
1. Klan Kristali yerleştirilir
2. Sistem kristalden başlayarak tüm yönlere yayılır
3. Klan Çiti (OAK_FENCE) ile karşılaşınca durur
4. Kapalı alan tespit edilirse → Bölge oluşur
5. Açık alan ise → Hata verir
```

### Adım Adım Bölge Kurma

#### **1. Alan Planla**
```
Boyutlar:
Minimum: 10x10
Maksimum: Limit yok (istediğin kadar büyük olabilir)

Örnek Başlangıç: 20x20 veya 30x30
```

#### **2. Klan Çiti Craft**
```
[Oak Planks][Iron Ingot][Oak Planks]
[Oak Planks][Iron Ingot][Oak Planks]

= 64x Klan Çiti
```

#### **3. Çitleri Yerleştir**

**KAPALI DÖRTGEN** oluşturmalısın:
```
Yukarıdan bakış:

[Ç][Ç][Ç][Ç][Ç][Ç][Ç]
[Ç]                 [Ç]
[Ç]                 [Ç]
[Ç]     ALAN        [Ç]    Ç = Klan Çiti
[Ç]                 [Ç]
[Ç]                 [Ç]
[Ç][Ç][Ç][Ç][Ç][Ç][Ç]

KURALLAR:
- Çitler birbirine değmeli (max 2 blok ara)
- Delik olmamalı
- Kapalı dörtgen olmalı
```

#### **4. Klan Kristali Yerleştir**

```
1. Alanın içine gir
2. Eline Klan Kristali al
3. İstediğin yere (tercihen ortaya) sağ tık
4. SONUÇ:
   ✓ Ender Crystal spawn olur
   ✓ Çitlerin çevrelediği alan → BÖLGE
   ✓ Şimşek + TOTEM partikülleri
   ✓ "Klan kuruldu!" mesajı
```

---

## 📐 BÖLGE GENİŞLETME

### Çit Ekleyerek Genişletme

**Dinamik Genişletme**:
```
1. Mevcut çitlerin dışına yeni çit diz
2. Yeni kapalı alan oluştur
3. Bölge otomatik genişler

Örnek:
Önceki: 20x20
Yeni çitler: 30x30
→ Bölge 30x30 olur
```

### Maksimum Boyut

```
Limit: Yok

Not: Klan bölgenizi istediğiniz kadar genişletebilirsiniz. 
Ancak çok büyük alanlar savunması zor olabilir, stratejik planlama önemlidir.
```

### Genişletme Stratejisi

**Aşamalı Genişletme**:
```
Seviye 1: 20x20 (Başlangıç)
   ↓ Üye sayısı arttıkça
Seviye 2: 40x40 (Orta)
   ↓ Yapılar kuruldukça  
Seviye 3: 70x70 (İleri)
   ↓ Güçlendikçe
Seviye 4: 100x100+ (Savaş için - limit yok)
```

---

## 🛡️ BÖLGE KORUMALARI

### Temel Korumalar

#### **1. Blok Koruma**
```
Düşman Klanlar:
❌ Blok kıramaz
❌ Blok koyamaz
❌ Yapı inşa edemez

Klan Üyeleri:
✓ Her şeyi yapabilir
✓ Rütbesine göre yetki
```

#### **2. PvP Koruma**
```
Klan Üyeleri Birbirine:
❌ Hasar veremez (bölge içinde)
❌ Kaza ile vuramazlar

Düşman Gelse Bile:
✓ Üyeler birbirlerine dokunmuyor
✓ Sadece düşmana odaklanır
```

#### **3. Kuşatma İstisnası**
```
Eğer Kuşatma Başlatılmışsa:
→ Düşman gir ebilir
→ Yapılara hasar verebilir
→ KRİSTALE ulaşabilir
```

#### **4. Ender Pearl Kısıtlaması**
```
Başkasının Klan Bölgesine:
❌ Ender Pearl ile ışınlanamazsın

İzin Verilenler:
✓ Kendi klan bölgene ışınlanabilirsin
✓ Misafir olduğun klana ışınlanabilirsin
```

### Rütbe Bazlı Yetkiler

```
┌──────────┬─────────┬─────────┬─────────┐
│ Rütbe    │ İnşaat  │ Yıkım   │ Sandık  │
├──────────┼─────────┼─────────┼─────────┤
│ LEADER   │ ✓       │ ✓       │ ✓       │
│ GENERAL  │ ✓       │ ✓       │ ✓       │
│ ELITE    │ ✓       │ ✓       │ ✓       │
│ MEMBER   │ ❌      │ ❌      │ ✓       │ ⚠️ YENİ
│ RECRUIT  │ ❌      │ ❌      │ ❌      │
└──────────┴─────────┴─────────┴─────────┘
```

**YENİ Değişiklikler** ⭐:
- ✅ **MEMBER Rütbesi**: Artık blok kırma ve yerleştirme yetkisi YOK (sadece yapı kullanabilir)
- ✅ **RECRUIT Rütbesi**: Sandık açma yetkisi de kaldırıldı
- ✅ **ELITE Rütbesi**: Artık blok yıkma yetkisi VAR

**YENİ Özellikler** ⭐:
- ✅ **ClanRankSystem Entegrasyonu**: Yapı kurma/yıkma işlemlerinde detaylı yetki kontrolü
- ✅ **Metadata Sistemi**: Klan çitleri metadata ile işaretleniyor
- ✅ **TerritoryData Yönetimi**: Çitler otomatik olarak TerritoryData'ya ekleniyor/kaldırılıyor
- ✅ **Async Hesaplama**: Büyük alanlar için async flood-fill algoritması (lag önleme)

---

## 🎆 TERRITORY BOUNDARY PARTICLE SİSTEMİ ⭐ YENİ

### Dinamik Partikül Yoğunluğu

**Özellikler:**
- Oyuncuya yakınken partiküller daha yoğun gösterilir
- Oyuncudan uzaklaştıkça partiküller azalır
- Sınır çizgisi boyunca partiküller gösterilir
- Performans optimizasyonu: Chunk-based cache ve rate limiting

**Algoritma:**
```
1. Oyuncu bölge sınırına yaklaşır
2. Sistem oyuncunun konumunu kontrol eder
3. Sınır çizgisi hesaplanır (TerritoryData'dan)
4. Partikül yoğunluğu oyuncuya olan mesafeye göre ayarlanır:
   - Yakın (< 10 blok): Yüksek yoğunluk (her 2 blokta 1 partikül)
   - Orta (10-30 blok): Orta yoğunluk (her 5 blokta 1 partikül)
   - Uzak (> 30 blok): Düşük yoğunluk (her 10 blokta 1 partikül)
5. Partiküller sınır çizgisi boyunca gösterilir
```

**Performans Optimizasyonları:**
- ✅ Chunk-based cache (territory cache)
- ✅ Rate limiting (1 saniye cooldown)
- ✅ Async hesaplama (büyük alanlar için)
- ✅ Oyuncu bazlı partikül gösterimi (sadece yakındaki oyuncular için)

### Territory Persistence

**Özellikler:**
- Bölge sınırları veritabanında saklanır
- Sunucu restart sonrası otomatik restore edilir
- TerritoryData model ile yönetilir
- Flood fill algoritması ile çitler otomatik tespit edilir

**Çalışma Süreci:**
1. Klan kristali yerleştirilir
2. Çitler toplanır (`collectFenceLocations()`)
3. `calculateBoundaries()` çağrılır
4. Çitler varsa flood fill ile alan bulunur
5. Sınır çizgisi hesaplanır
6. TerritoryData güncellenir ve kaydedilir
7. Partikül task'ı güncellenmiş veriyi kullanır

Detaylı bilgi için: `SON_3_GUN_DEGISIKLIKLER_VE_SISTEM_DOKUMANI.md` dosyasına bakın.

---

## 🔧 SON GÜNCELLEMELER (Son 3 Gün) ⭐

### Territory Boundary Particle Sistemi

**Yeni Özellikler:**
- Dinamik partikül yoğunluğu (oyuncuya yakınken daha yoğun)
- Territory persistence (bölge sınırları veritabanında saklanır)
- Flood fill algoritması ile çit tespiti
- Performans optimizasyonları (chunk-based cache, async hesaplama)

### Territory Persistence

**Sorun:** Sunucu restart sonrası bölge sınırları yanlış gösteriliyordu.

**Çözüm:** TerritoryData model ile bölge sınırları veritabanında saklanır ve sunucu açıldığında otomatik restore edilir.

**Algoritma:**
1. Bölge oluşturulduğunda TerritoryData kaydedilir
2. Sunucu kapanırken `saveAll()` çağrılır
3. TerritoryData JSON'a çevrilir ve `territory_data.json`'a yazılır
4. Sunucu açıldığında `loadAll()` çağrılır
5. JSON'dan okunur ve TerritoryData objeleri oluşturulur
6. Partikül task'ı güncellenmiş veriyi kullanır

Detaylı bilgi için: `SON_3_GUN_DEGISIKLIKLER_VE_SISTEM_DOKUMANI.md` dosyasına bakın.

---

**YENİ Özellikler** ⭐:
- ✅ **ClanRankSystem Entegrasyonu**: Yapı kurma/yıkma işlemlerinde detaylı yetki kontrolü
- ✅ **Metadata Sistemi**: Klan çitleri metadata ile işaretleniyor
- ✅ **TerritoryData Yönetimi**: Çitler otomatik olarak TerritoryData'ya ekleniyor/kaldırılıyor
- ✅ **Async Hesaplama**: Büyük alanlar için async flood-fill algoritması (lag önleme)

---

## 🔋 OFFLINE KORUMA (Kalkan Enerjisi)

### Yakıt Sistemi

**Kristale Yakıt Ekleme**:
```
1. Eline Kömür veya Kükürt al
2. Klan Kristaline yaklaş
3. Sağ Tık
4. Yakıt eklenir

Görsel:
→ Kristal etrafında mavi partikül
→ "Kalkan Yakıtı: +10 saat" mesajı
```

### Yakıt Tipleri

```
┌──────────────┬───────────────┬──────────┐
│ Yakıt        │ Süre          │ Kaynak   │
├──────────────┼───────────────┼──────────┤
│ Kömür        │ +2 saat       │ Kolay    │
│ Kükürt       │ +4 saat       │ Orta     │
│ Karanlık     │ +12 saat      │ Efsane   │
│ Madde        │ (Maximum)     │          │
└──────────────┴───────────────┴───────────┘

Maksimum Kapasite: 12 saat
```

### Offline Koruma Nasıl Çalışır?

```
Durum 1: Klan Üyeleri Online
→ Normal koruma
→ Yakıt tüketilmez

Durum 2: Klan Üyeleri Offline + Yakıt VAR
→ Kristal hasar almaz
→ Her saldırıda 1 birim yakıt harcanır
→ "Enerji Kalkanı aktif! Kalkan Gücü: 8 saat"

Durum 3: Klan Üyeleri Offline + Yakıt YOK
→ Kristal savunmasız
→ Saldırıya açık
```

### Yakıt Tüketimi

```
Tüketim Sebepleri:

1. Düşman blok kırmaya çalıştığında: -1 birim (10 dk)
2. Kristale direkt saldırıda: -2 birim (20 dk)
3. Kuşatma anıtı dikildiğinde: -5 birim (1 saat)

Yakıt Bitince:
→ Offline koruma kalkar
→ Mesaj: "Kalkan enerjisi bitti, kristal savunmasız!"
```

---

## 💎 KRİSTAL YÖNETİMİ

### Kristal Taşıma (Sadece Lider)

**Adımlar**:
```
1. Lider kristale yaklaşır
2. Shift + Boş El + Sağ Tık
3. Yeni konum seç (mutlaka çit içinde olmalı)
4. Tekrar Shift + Sağ Tık
5. Kristal ışınlanır
```

**Kurallar**:
- Yeni konum çit içinde olmalı
- Çit dışına taşınamaz
- Sadece Lider yapabilir

### Kristal Koruma

**Hasar Önleme**:
```
Offline Koruma (Yakıt Varsa):
→ Kristal hasar almaz

Kalkan Yapısı (Force Field):
→ %90 hasar azaltması
→ Enerji tüketir

Normal Durumda:
→ Sadece kuşatmada hasarlı
→ Normal PvE hasarsız
```

### Kristal Kırılması

**Nasıl Kırılır?**:
```
1. Kuşatma başlat
2. Çitleri kır (bölgeye gir)
3. Savunma yapılarını aş
4. Kristale ulaş
5. Normal silahla vur
6. Kristal kırılır

ÖZEL RİTÜEL YOK - Normal şekilde vur!
```

**Kırıldığında**:
```
Sonuçlar:
- Klan dağılır
- Bölge koruması kalkar
- Tüm yapılar savunmasız
- Savaş biter

Kazanan:
- %50 kaybeden kasası
- Fatih buff'ı (24 saat)
- Yapı malzemeleri düşer
```

---

## 🎯 BÖLGE OPTİMİZASYONU

### İdeal Bölge Tasarımı

**Savunma Odaklı**:
```
[Ç][Ç][Ç][Ç][Ç][Ç][Ç]
[Ç][T]     [T][Ç]     T = Tuzak
[Ç]   [Z]     [Ç]     Z = Zehir Kulesi
[Ç]     [K]   [Ç]     K = Kristal (ortada)
[Ç]   [L]     [Ç]     L = Lav Fıskiyesi
[Ç][R]     [R][Ç]     R = Radar
[Ç][Ç][Ç][Ç][Ç][Ç][Ç]

Katmanlar:
1. Dış çit (sınır)
2. Tuzak hattı
3. Savunma yapıları
4. Kristal (merkez)
```

### Genişletme Planlaması

**Aşamalı Büyüme**:
```
Faz 1 (Başlangıç - 20x20):
- Kristal ortada
- 2-3 temel yapı
- Basit savunma

Faz 2 (Gelişme - 40x40):
- Üretim yapıları ekle
- Tuzak sistemleri kur
- Radar ekle

Faz 3 (İleri - 70x70):
- Şube alanları
- Enerji ağı
- Gelişmiş savunma

Faz 4 (İleri Seviye - 100x100+):
- Tam savunma hattı
- Çoklu katmanlar
- Savaş hazırlığı
- Limit yok, istediğin kadar büyütebilirsin
```

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Çit Güncellemesi**: Çitleri değiştirirsen bölge anında güncellenir
2. **Kapalı Alan**: Delik varsa bölge oluşmaz
3. **Offline Yakıt**: Mutlaka doluktu, gece baskınlarına karşı
4. **Kristal Yedekleme**: Offline koruma + Kalkan yapısı + Tuzaklar = Güvenli
5. **Genişletme**: Yavaş yavaş genişlet, çok hızlı büyüme savunulamaz

---

**🎮 Bölgeni dikkatle planla, yakıtını unutma, savunmanı güçlendir!**
