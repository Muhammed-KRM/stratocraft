# STRATOCRAFT - ZORLUK SİSTEMİ

## 🌍 Zorluk Sistemi Nedir?

Stratocraft'ta dünya **merkezden uzaklaştıkça zorlaşır**! Spawn noktasından ne kadar uzaksanız, o kadar güçlü moblar ve değerli madenler bulursunuz.

---

## 📋 İÇİNDEKİLER

1. [Merkez Noktası](#merkez-noktası)
2. [5 Zorluk Bölgesi](#5-zorluk-bölgesi)
3. [Mob Kısıtlamaları](#mob-kısıtlamaları)
4. [Maden Kısıtlamaları](#maden-kısıtlamaları)

---

## 🎯 MERKEZ NOKTASI

### Merkez Nedir?

**Merkez Noktası**: Dünyanın spawn noktası (varsayılan)

```
Merkez = (X: 0, Z: 0) veya Spawn Noktası
```

**Uzaklık Hesaplama**:
```
Uzaklık = √((X - MerkezX)² + (Z - MerkezZ)²)

Örnek:
Merkez: (0, 0)
Sen: (1000, 1000)
Uzaklık = √(1000² + 1000²) = 1414 blok
```

---

### Admin Komutu

**Merkezi Değiştir**:
```
/stratocraft setcenter

Etki:
- Bulunduğun konum merkez olur
- Config'e kaydedilir
- Tüm zorluk hesaplamaları yeniden yapılır
```

---

## 🗺️ 5 ZORLUK BÖLGESİ

### Seviye 0: Başlangıç Alanı (0-200 blok)

**Özellikler**:
```
Uzaklık: 0-200 blok
Zorluk: Çok Kolay
Moblar: Normal Minecraft mobları
Madenler: Vanilla madenler
```

**Amaç**: Yeni oyuncular için güvenli alan

---

### Seviye 1: Başlangıç (200-1000 blok)

**Özellikler**:
```
Uzaklık: 200-1000 blok
Zorluk: Kolay
İsim: "Başlangıç"
```

**Spawn Olan Moblar**:
```
- Goblin (30 HP)
- Yaban Domuzu (50 HP)
- Kurt Sürüsü (60 HP)
- Yılan (40 HP)
- Kartal (45 HP)
- Ayı (90 HP)
```

**Spawn Olan Madenler**:
```
- Kükürt (Sulfur)
- Boksit (Bauxite)
- Tuz Kayası (Rock Salt)
```

---

### Seviye 2: Orta (1000-3000 blok)

**Özellikler**:
```
Uzaklık: 1000-3000 blok
Zorluk: Orta
İsim: "Orta"
```

**Spawn Olan Moblar**:
```
- Ork (80 HP)
- Troll (120 HP)
- İskelet Şövalye (60 HP)
- Karanlık Büyücü (50 HP)
- Kurt Adam (70 HP)
- Dev Örümcek (60 HP)
- Minotaur (100 HP)
- Harpy (40 HP)
- Basilisk (90 HP)

YENİ MOBLAR:
- Demir Golem (150 HP)
- Buz Ejderi (200 HP)
- Ateş Yılanı (180 HP)
- Toprak Dev (250 HP)
- Ruh Avcısı (120 HP)
```

**Spawn Olan Madenler**:
```
- Tüm Seviye 1 madenler
- Titanyum (yeni!)
```

---

### Seviye 3: Zor (3000-5000 blok)

**Özellikler**:
```
Uzaklık: 3000-5000 blok
Zorluk: Zor
İsim: "Zor"
```

**Spawn Olan Moblar**:
```
- T-Rex (400 HP)
- Cyclops (300 HP)
- Griffin (350 HP)
- Wraith (150 HP)
- Lich (250 HP)
- Kraken (400 HP)
- Phoenix (300 HP)
- Behemoth (500 HP)

YENİ MOBLAR:
- Gölge Ejderi (350 HP)
- Işık Ejderi (350 HP)
- Fırtına Dev (400 HP)
- Lav Ejderi (400 HP)
- Buz Dev (400 HP)
```

**Spawn Olan Madenler**:
```
- Tüm Seviye 1-2 madenler
- Mithril (yeni!)
```

---

### Seviye 4: Çok Zor (5000-10000 blok)

**Özellikler**:
```
Uzaklık: 5000-10000 blok
Zorluk: Çok Zor
İsim: "Çok Zor"
```

**Spawn Olan Moblar**:
```
- Ejderha (500 HP)
- Wyvern (Çok Hızlı)
- Cehennem Ejderi (Ateşli)
- Korku Solucanı (Yeraltı)
- Savaş Ayısı (Binilebilir)
- Gölge Panteri (Hızlı)

YENİ MOBLAR:
- Kızıl Şeytan (500 HP)
- Kara Ejder (600 HP)
- Ölüm Şövalyesi (450 HP)
- Kaos Ejderi (700 HP)
- Cehennem Şeytanı (550 HP)
```

**Spawn Olan Madenler**:
```
- Tüm Seviye 1-3 madenler
- Astral Cevheri (yeni!)
```

---

### Seviye 5: Efsanevi (10000+ blok)

**Özellikler**:
```
Uzaklık: 10000+ blok
Zorluk: Efsanevi
İsim: "Efsanevi"
```

**Spawn Olan Moblar**:
```
- Titan Golem (400 HP + Özel Yetenekler)
- Hydra (600 HP)
- Hiçlik Solucanı (300 HP, Yeraltı)

YENİ MOBLAR:
- Efsanevi Ejder (1000 HP)
- Tanrı Katili (1200 HP)
- Hiçlik Yaratığı (800 HP)
- Zaman Ejderi (900 HP)
- Kader Yaratığı (850 HP)
```

**Spawn Olan Madenler**:
```
- Tüm Seviye 1-4 madenler
- Kızıl Elmas (en nadir!)
```

---

## 🦖 MOB KISITLAMALARI

### Seviye Bazlı Spawn

**Kural**: Her mob sadece kendi seviyesinde spawn olur!

```
Goblin → Sadece Seviye 1 (200-1000 blok)
Ork → Sadece Seviye 2 (1000-3000 blok)
Ejderha → Sadece Seviye 4 (5000-10000 blok)
```

**Örnek**:
```
Sen 500 blok uzaktasın (Seviye 1):
✓ Goblin spawn olabilir
✓ Yaban Domuzu spawn olabilir
❌ Ork spawn OLAMAZ (Seviye 2 gerekli)
❌ Ejderha spawn OLAMAZ (Seviye 4 gerekli)
```

---

### Mob Listesi (Seviyeye Göre)

| Seviye | Moblar |
|--------|--------|
| **1** | Goblin, Yaban Domuzu, Kurt Sürüsü, Yılan, Kartal, Ayı |
| **2** | Ork, Troll, İskelet Şövalye, Karanlık Büyücü, Kurt Adam, Dev Örümcek, Minotaur, Harpy, Basilisk, Demir Golem, Buz Ejderi, Ateş Yılanı, Toprak Dev, Ruh Avcısı |
| **3** | T-Rex, Cyclops, Griffin, Wraith, Lich, Kraken, Phoenix, Behemoth, Gölge Ejderi, Işık Ejderi, Fırtına Dev, Lav Ejderi, Buz Dev |
| **4** | Ejderha, Wyvern, Cehennem Ejderi, Korku Solucanı, Savaş Ayısı, Gölge Panteri, Kızıl Şeytan, Kara Ejder, Ölüm Şövalyesi, Kaos Ejderi, Cehennem Şeytanı |
| **5** | Titan Golem, Hydra, Hiçlik Solucanı, Efsanevi Ejder, Tanrı Katili, Hiçlik Yaratığı, Zaman Ejderi, Kader Yaratığı |

---

## ⛏️ MADEN KISITLAMALARI

### Seviye Bazlı Spawn

**Kural**: Madenler minimum seviyeden itibaren spawn olur!

```
Kükürt → Seviye 1+ (her yerde)
Titanyum → Seviye 2+ (1000+ blok)
Mithril → Seviye 3+ (3000+ blok)
Astral → Seviye 4+ (5000+ blok)
Kızıl Elmas → Seviye 5+ (10000+ blok)
```

**Örnek**:
```
Sen 4000 blok uzaktasın (Seviye 3):
✓ Kükürt bulabilirsin
✓ Titanyum bulabilirsin
✓ Mithril bulabilirsin
❌ Astral bulamazsın (Seviye 4 gerekli)
❌ Kızıl Elmas bulamazsın (Seviye 5 gerekli)
```

---

### Maden Listesi (Seviyeye Göre)

| Maden | Minimum Seviye | Minimum Uzaklık |
|-------|----------------|-----------------|
| **Kükürt** | 1 | 200 blok |
| **Boksit** | 1 | 200 blok |
| **Tuz Kayası** | 1 | 200 blok |
| **Titanyum** | 2 | 1000 blok |
| **Mithril** | 3 | 3000 blok |
| **Astral Cevheri** | 4 | 5000 blok |
| **Kızıl Elmas** | 5 | 10000 blok |

---

## 🎯 STRATEJİK PLANLAMA

### Yeni Başlayanlar (0-1000 blok)

**Tavsiye**:
```
1. Merkeze yakın kal (güvenli)
2. Temel madenler topla (Kükürt, Boksit)
3. Seviye 1 mobları eğit (Goblin)
4. Ekipman hazırla
```

---

### Orta Seviye (1000-3000 blok)

**Tavsiye**:
```
1. Titanyum madenciliğine başla
2. Ork ve Troll eğit
3. Klan kur
4. Base inşa et
```

---

### İleri Seviye (3000-5000 blok)

**Tavsiye**:
```
1. Mithril topla
2. Ejderha ve Griffin av
3. Güçlü yapılar kur
4. Klan savaşlarına hazırlan
```

---

### Uzman (5000+ blok)

**Tavsiye**:
```
1. Astral Cevheri ve Kızıl Elmas ara
2. Titan Golem ve Hydra eğit
3. Efsanevi ekipman craft et
4. Felaketlere hazırlan
```

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Merkez Noktası**: Varsayılan olarak spawn noktası
2. **2D Uzaklık**: Sadece X ve Z eksenleri (Y yüksekliği önemli değil)
3. **Seviye Kısıtlamaları**: Moblar sadece kendi seviyelerinde spawn olur
4. **Maden Kısıtlamaları**: Madenler minimum seviyeden itibaren bulunur
5. **Biyom Değişimi**: Zorluk seviyesi biyomları da etkiler
6. **Zindan Spawn**: Yüksek zorluk bölgelerinde zindanlar spawn olur
7. **Felaketler**: Uzak bölgelerde (5000+ blok) felaketler başlar

---

## 🎮 HIZLI REHBERİ

### Konumunu Öğren

```
1. F3'e bas (debug ekranı)
2. X ve Z koordinatlarına bak
3. Merkeze olan uzaklığı hesapla
4. Hangi seviyede olduğunu anla
```

### Seviyeni Kontrol Et

```
Mesafe Hesaplama:
√((X - 0)² + (Z - 0)²)

Örnek:
X: 2000, Z: 2000
Mesafe = √(2000² + 2000²) = 2828 blok
Seviye = 3 (Zor)
```

---

**🎮 Merkezden uzaklaş, güçlen, efsanevi ol!**
