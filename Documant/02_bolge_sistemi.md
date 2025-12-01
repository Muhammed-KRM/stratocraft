# STRATOCRAFT - BÖLGE SİSTEMİ

## 🗺️ Bölge Sistemi Nedir?

Bölge, **Klan Çitinin çevrelediği alandan** oluşur. Sadece bu alanda yapılar kurabilir ve felaketlerden korunabilirsiniz.

---

## 📋 İÇİNDEKİLER

1. [Bölge Oluşturma](#bölge-oluşturma)
2. [Bölge Genişletme](#bölge-genişletme)
3. [Bölge Korumaları](#bölge-korumalari)
4. [Offline Koruma](#offline-koruma)
5. [Kristal Yönetimi](#kristal-yönetimi)

---

## 🏗️ BÖLGE OLUŞTURMA

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
Maksimum: 150x150

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
Limit: 150x150

Neden Limit Var?
- Performans
- Denge (büyük klanlar çok alan kaplamasın)
- Felaket mekaniği (çok büyük alanlar savunulamaz)
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
Seviye 4: 100x100 (Maksimum - savaş için)
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

### Rütbe Bazlı Yetkiler

```
┌──────────┬─────────┬─────────┬─────────┐
│ Rütbe    │ İnşaat  │ Yıkım   │ Sandık  │
├──────────┼─────────┼─────────┼─────────┤
│ LEADER   │ ✓       │ ✓       │ ✓       │
│ GENERAL  │ ✓       │ ✓       │ ✓       │
│ MEMBER   │ ✓       │ ✓       │ ✓       │
│ RECRUIT  │ ❌      │ ❌      │ ✓       │
└──────────┴─────────┴─────────┴─────────┘
```

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

Faz 4 (Maksimum - 100x100):
- Tam savunma hattı
- Çoklu katmanlar
- Savaş hazırlığı
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
