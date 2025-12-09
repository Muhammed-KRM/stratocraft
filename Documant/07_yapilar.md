# STRATOCRAFT - YAPILAR SİSTEMİ

## 🏗️ Yapılar Nedir?

Yapılar, oyunda **pasif güç veren** veya **özel işlevler sağlayan** fiziksel inşaatlardır. **2 ana kategori** var!

---

## 📋 İÇİNDEKİLER

1. [Yapı Kategorileri](#yapi-kategorileri)
2. [Klan Yapıları](#klan-yapilari)
3. [Dışarı Yapılan Yapılar - A](#dişari-yapilar-a-klan-özel)
4. [Dışarı Yapılan Yapılar - B](#dişari-yapilar-b-herkes)
5. [Yapı Güç Sistemi](#yapı-güç-sistemi) ⭐ YENİ

---

## 🏛️ YAPI KATEGORİLERİ

### Kategori Sistemi

```
YAPILAR
├── 1. KLAN YAPILARI
│   └── Sadece klan bölgesinde
│       Büyük, pahalı, güçlü
│
└── 2. DIŞARI YAPILAN YAPILAR
    ├── A) Klan Özel Yapılar
    │   └── Yapan + klanı faydalanır
    │       Orta boyut, savaş odaklı
    │
    └── B) Herkes İçin Yapılar
        └── Herkes kullanabilir
            Küçük, ucuz, sosyal
```

---

## 📍 YAPI YERLEŞİM KURALLARI

**1. Klan Yapıları**:
```
Nereye yapılır: SADECE klan bölgesi içinde
Kısıtlama: Çit sınırları içinde olmalı
Özellik: Büyük, karmaşık, pahalı
```

**2. Dışarı Yapılar (A ve B)**:
```
Nereye yapılır: Blok koyma izni olan her yere
YASAK yerler:
  ❌ Spawn bölgesi
  ❌ Başkasının klan bölgesi

İZİN VERİLEN yerler:
  ✅ Vahşi alanlar
  ✅ Kendi klan bölgen dışı
  ✅ Tarafsız topraklar
```

---

## ⚙️ YAPI AKTİVASYON SİSTEMİ

### 🔧 Aktivasyon Yöntemi

**ÖNEMLİ**: Tüm yapı aktivasyonları için **Shift + Sağ Tık** gereklidir!

**Neden?**
- Normal Minecraft bloklarının kullanımını engellemez
- Örs (Anvil), Büyü Masası (Enchanting Table), Ender Sandığı gibi bloklar normal kullanımda çalışır
- Sadece yapı aktivasyonu için shift+sağ tık yapılmalıdır

**Aktivasyon Adımları**:
```
1. Elinde "Blueprint" (Plan) item'ı olmalı
2. Yapı desenini manuel olarak kur
3. Merkez bloğa Shift + Sağ Tık yap
4. Sistem deseni kontrol eder
5. Doğruysa → Yapı aktif olur!
```

### 🚫 Kaldırılan Özellikler

**1. Fener (LANTERN) ile Şifa Kulesi**:
- ❌ **KALDIRILDI**: Fener ile sağ tıklama bug'a neden oluyordu
- Artık fener normal Minecraft bloğu olarak çalışır
- Şifa Kulesi için farklı bir aktivasyon yöntemi kullanılmalı

**2. Enerji Kalkanı (Shield/Force Field) Sistemi**:
- ❌ **KALDIRILDI**: Shield sistemi sorunlu ve bug'a neden oluyordu
- Artık shield oluşturulamaz
- Mevcut shield'lar kırıldığında düzgün temizlenir

### 📋 Yapı Aktivasyonu İçin Gereksinimler

**Gerekli Malzemeler**:
- **Blueprint** (Plan) item'ı elinde olmalı
- Yapı için gerekli tarif kitabı öğrenilmiş olmalı
- Yapı deseni doğru kurulmuş olmalı
- Klan üyesi olmalı (Recruit hariç)
- Kendi klan bölgesinde olmalı

**Aktivasyon Kontrolleri**:
- ✅ Shift + Sağ Tık yapıldı mı?
- ✅ Elinde Blueprint var mı?
- ✅ Tarif kitabı öğrenilmiş mi?
- ✅ Yapı deseni doğru mu?
- ✅ Klan üyesi mi? (Recruit değil)
- ✅ Kendi bölgesinde mi?

---

# 🏰 1. KLAN YAPILARI

**Özellikler**:
- ⭐ Sadece **klan bölgesi içinde** yapılabilir
- ⭐ Çok **büyük ve karmaşık** yapılar
- ⭐ **Pahalı** malzemeler gerektirir
- ⭐ Klana **güçlü pasif buff'lar** verir
- ⭐ Bazıları **alarm/uyarı** sistemleri

---

## 🔮 Klan Yapıları Listesi

### 1. Ana Kristal (CORE)

**Zorunlu** - Her klanın olmalı

**Boyut**: 3x3x5 blok

**Malzeme**:
- 1 Klan Kristali
- 8 Dragon Egg (çerçeve)
- 4 Beacon (köşeler)

**İşlev**:
- Klan merkezi
- Offline koruma merkezi
- 12 saat max yakıt

**Özel**: Kırılırsa klan dağılır!

---

### 2. Simya Kulesi (Alchemy Tower)

**Boyut**: 5x5x10 blok

**İşlev**: Bataryaların gücünü artırır

**Seviyeler**:
```
Lv1 (Taş): Bataryalar +%10 güç
Lv2 (Demir): Bataryalar +%20 güç
Lv3 (Titanyum): Bataryalar +%35 güç
Lv4 (Adamantite): +%50 güç + alan artışı
Lv5 (Karanlık Madde): +%75 güç + çift atış
```

---

### 3. Zehir Reaktörü (Poison Reactor)

**Boyut**: 7x7x8 blok

**İşlev**: Bölgeye giren düşmanlara sürekli zehir

**Seviyeler**:
```
Lv1: Poison I (sürekli)
Lv2: Poison II (sürekli)
Lv3: Poison III + Slowness I
Lv4: Poison III + Slowness II + Nausea
Lv5: Poison IV + Slowness III + Blindness
```

**Menzil**: 30 blok yarıçap

---

### 4. Tektonik Sabitleyici (Tectonic Stabilizer)

**Boyut**: 9x9x12 blok (ÇOK BÜYÜK!)

**İşlev**: Felaket hasarını azaltır

**Seviyeler**:
```
Lv1: %50 felaket hasar azaltma
Lv2: %70 felaket hasar azaltma
Lv3: %90 felaket hasar azaltma
Lv4: %95 + Golem'i yavaşlatır
Lv5: %99 + Tüm felaketlere karşı
```

**Gereksinim**: **Tarif Kitabı** (Boss dropu)

---

### 5. Gözetleme Kulesi (Watchtower)

**Boyut**: 3x3x15 blok (yüksek kule)

**İşlev**: **ALARM SİSTEMİ** - Erken uyarı

**Seviyeler**:
```
Lv1: 100 blok menzil → Koordinat bilgisi
Lv3: 200 blok menzil → Sayı + ekipman
Lv5: 300 blok menzil → Tam analiz
```

**Mesaj Örneği**:
```
⚠️ DİKKAT! Kuzey'de 3 düşman tespit edildi!
📍 Konum: X:1234, Z:5678
⚔️ Ekipman: Tam zırhlı, elmas kılıç
```

---

### 6. Kuşatma Fabrikası (Siege Factory)

**Boyut**: 10x10x6 blok

**İşlev**: Mancınık ve Balista üretir

```
Lv1: 1 saat = 1 Mancınık
Lv3: 30 dk = 1 Mancınık + 1 Balista
Lv5: 15 dk = 2 Mancınık + 2 Balista
```

---

### 7. Otomatik Taret (Auto Turret)

**Boyut**: 3x3x4 blok

**İşlev**: Otonom ok savunması

```
Lv1: 1 ok/sn (2 kalp)
Lv3: 2 ok/sn (3 kalp) + ateşli ok
Lv5: 3 ok/sn (5 kalp) + patlayıcı ok
```

**Menzil**: 20 blok

**Gereksinim**: Hurda Teknolojisi (Felaket enkzı)

---

# 🏕️ 2A. DIŞARI YAPILAR - KLAN ÖZEL

**Özellikler**:
- ⭐ **Dışarıda** yapılabilir (spawn/düşman bölgesi hariç)
- ⭐ Sadece **yapan oyuncu + klanı** faydalanır
- ⭐ **Orta boyutlu**, savaşlarda kullanılır
- ⭐ **Geçici** veya sınırlı süre

---

## 🏕️ Klan Özel Yapı Listesi

### 1. Şifa Tapınağı (Healing Shrine)

**Boyut**: 5x5x3 blok (küçük tapınak)

**Malzeme**:
- 20 Ametist Bloğu
- 4 Beacon
- 1 Enchanting Table (orta)

**İşlev**:
- İçine girenlere **sürekli regen**
- Regeneration II efekti
- Sadece **yapan oyuncu + klanı** etkilenir

**Kullanım**: Savaş alanına kur, yaralılar burada iyileşsin

**Süre**: Yakıt bitene kadar (Coal ile beslenir)

---

### 2. Geçici Kale (Temporary Fort)

**Boyut**: 7x7x5 blok

**Malzeme**:
- 50 Cobblestone
- 10 Iron Block
- 4 Torch

**İşlev**:
- Hızlı savunma yapısı
- İçindekiler +Resistance I
- 30 dakika sonra kaybolur (otomatik yıkılır)

**Kullanım**: Savaşta acil sığınak

---

### 3. Cephane Deposu (Ammo Cache)

**Boyut**: 3x3x2 blok

**Malzeme**:
- 1 Double Chest
- 8 Iron Block
- 4 Redstone Torch

**İşlev**:
- Savaş malzemesi deposu
- Sadece klan üyeleri açabilir
- 1 saat sonra patlar (trap olmasın diye)

**Kullanım**: Savaş alanına yakın, hızlı silah/ok yenileme

---

### 4. Sismik Radar (Seismic Sensor)

**Boyut**: 3x3x3 blok

**Malzeme**:
- 9 Note Block
- 1 Observer
- 4 Redstone

**İşlev**:
- 50 blok yarıçapta hareket algılar
- Sadece yapan klan için uyarı
- "⚠️ 20 blok uzakta hareket!" mesajı

**Kullanım**: Gizli üs çevresine kur

---

# 🌍 2B. DIŞARI YAPILAR - HERKES İÇİN

**Özellikler**:
- ⭐ **Dışarıda** yapılabilir
- ⭐ **Herkes kullanabilir** (rakipler bile!)
- ⭐ **Küçük ve ucuz**
- ⭐ **Sosyal/ekonomik** amaçlı
- ⭐ **Kalıcı** (kırılmaz)

---

## 🌍 Herkes İçin Yapı Listesi

### 1. Görev Loncası (Quest Guild)

**Boyut**: 5x5x4 blok (küçük ev)

**Malzeme**:
```
Lv1 (Taş Totem):
- 20 Stone
- 4 Oak Planks
- 1 Sign

Lv2 (Demir Totem):
- 30 Stone
- 10 Iron
- 1 Lectern

Lv3 (Elmas Totem):
- 50 Stone
- 20 Iron
- 5 Diamond
- 1 Enchanting Table
```

**İşlev**:
- **Herkes görev alabilir**
- Seviyeye göre zorluk artışı
- Ödül: Para, eşya, XP

**Görev Örnekleri**:
```
Lv1: "64 Odun getir" → 50 Altın
Lv2: "10 Zombi öldür" → 200 Altın
Lv3: "1 Boss öldür" → 2000 Altın + Tarif
```

---

### 2. Ticaret Platformu (Trade Platform)

**Boyut**: 3x3x2 blok

**Malzeme**:
- 9 Oak Planks
- 4 Chest
- 1 Lectern

**İşlev**:
- **Herkes eşya satabilir/alabilir**
- Basit pazar yeri
- %5 vergi (yapana gider)

**Kullanım**: Oyuncular arası ticaret noktası

---

### 3. Harita Taşı (Waypoint Stone)

**Boyut**: 1x1x2 blok (çok küçük!)

**Malzeme**:
- 1 Stone
- 1 Sign

**İşlev**:
- Koordinat işaretleyici
- Herkes görebilir
- Harita'da görünür

**Kullanım**: Önemli yerleri işaretle (örn: "Kuzey Madeni")

---

### 4. Dinlenme Kampı (Rest Camp)

**Boyut**: 4x4x3 blok

**Malzeme**:
- 1 Campfire
- 4 Bed
- 8 Oak Planks

**İşlev**:
- **Herkes kullanabilir**
- Spawn noktası set edebilir (geçici)
- Yemek pişirme (Campfire)

**Kullanım**: Uzak bölgelerde güvenli nokta

---

### 5. Bilgi Panosu (Notice Board)

**Boyut**: 1x2x1 blok

**Malzeme**:
- 6 Oak Planks
- 1 Sign

**İşlev**:
- **Herkes mesaj yazabilir**
- 10 mesaj sınırı
- Herkese açık duyuru

**Kullanım**: Sunucu duyuruları, ticaret ilanları

---

## 📊 YAPI KARŞILAŞTIRMA TABLOSU

```
┌─────────────────┬─────────┬──────────┬─────────┬──────────┐
│ Kategori        │ Boyut   │ Maliyet  │ Fayda   │ Konum    │
├─────────────────┼─────────┼──────────┼─────────┼──────────┤
│ Klan Yapıları   │ Büyük   │ Çok Yük. │ Klan    │ İçeride  │
│                 │ 5x5+    │ Boss Mat.│ Pasif   │ Sadece   │
├─────────────────┼─────────┼──────────┼─────────┼──────────┤
│ Dışarı-A (Klan) │ Orta    │ Orta     │ Klan    │ Dışarıda │
│                 │ 3-7 blok│ Normal   │ Geçici  │ İzinli   │
├─────────────────┼─────────┼──────────┼─────────┼──────────┤
│ Dışarı-B (Pub.) │ Küçük   │ Ucuz     │ Herkes  │ Dışarıda │
│                 │ 1-5 blok│ Taş/Odun │ Sosyal  │ İzinli   │
└─────────────────┴─────────┴──────────┴─────────┴──────────┘
```

---

## 🎯 STRATEJİK KULLANIM

### Klan İçin (Savunma)

**Ev Üssü**:
```
Klan Bölgesi İçinde:
- Ana Kristal (ortada)
- Zehir Reaktörü (savunma)
- Gözetleme Kulesi (alarm)
- Simya Kulesi (batarya buff)
```

---

### Savaş İçin (Dışarıda)

**Saldırı Kampı**:
```
Düşman base yakınına (Dışarı-A):
- Şifa Tapınağı (iyileşme)
- Geçici Kale (sığınak)
- Cephane Deposu (silah)
→ Saldırı üssü hazır!
```

---

### Sosyal İçin (Herkes)

**Ticaret Bölgesi**:
```
Spawn yakınına (Dışarı-B):
- Görev Loncası (görevler)
- Ticaret Platformu (pazar)
- Harita Taşı (işaretler)
- Dinlenme Kampı (spawn point)
→ Sosyal merkez!
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Yerleşim Kuralları

**Klan Yapıları**:
- ✅ Sadece klan bölgesi içinde
- ❌ Dışarıda YAPILAMAZ
- Büyük alan gerektirir

**Dışarı Yapı (A - Klan)**:
- ✅ Vahşi alanlarda
- ❌ Spawn'da yapılamaz
- ❌ Düşman bölgesinde yapılamaz
- Sadece klanın kullanır

**Dışarı Yapı (B - Herkes)**:
- ✅ Vahşi alanlarda
- ❌ Spawn'da yapılamaz
- ❌ Düşman bölgesinde yapılamaz
- Herkes kullanabilir

---

### Yapı Maliyetleri

**Klan Yapıları**:
- Çok pahalı (Boss malzemeleri)
- Tarif kitabı gerekebilir
- Takım halinde toplanmalı

**Dışarı-A (Klan)**:
- Orta maliyet
- Normal oyunda bulunur
- Bireysel yapılabilir

**Dışarı-B (Herkes)**:
- Çok ucuz (Taş, Odun)
- Yeni oyuncular bile yapabilir
- Sosyal yardım amaçlı

---

---

## 🏗️ YAPI GÜÇ SİSTEMİ (YENİ)

### ✅ Yapı Seviyesi Güç Kazanma

**Yapılar artık klan gücüne katkı sağlıyor!**

Her yapı, seviyesine göre **Güç Puanı (Power Score)** verir ve klanın toplam gücünü artırır.

### Yapı Gücü Hesaplama

**Yapı Seviyesi → Güç:**
```
- Seviye 1: 100 puan
- Seviye 2: 250 puan
- Seviye 3: 500 puan
- Seviye 4: 1200 puan
- Seviye 5: 2000 puan
```

**Klan Kristali:**
```
Klan Kristali: +500 puan (sabit bonus)
- Sadece kristal varsa ve ölü değilse
```

### Örnek Hesaplama

**Örnek Klan:**
```
- Klan Kristali: +500 puan
- Simya Kulesi (Seviye 3): +500 puan
- Tektonik Sabitleyici (Seviye 4): +1200 puan
- Toplam Yapı Gücü: 500 + 500 + 1200 = 2200 puan
```

### Klan Gücüne Etkisi

**Yapı Gücü:**
- Klanın toplam gücüne eklenir
- Klan seviyesi hesaplamasında kullanılır
- Felaket zorluğunu etkiler

### Komutlar

**Yapı gücünü görmek için:**
```
/sgp clan
```

**Klan güç bileşenlerini görmek için:**
```
/sgp components
```

### Config Ayarları

Yapı güç değerleri `config.yml` dosyasından ayarlanabilir:

```yaml
clan-power-system:
  structure-power:
    crystal-base: 500
    level-1: 100
    level-2: 250
    level-3: 500
    level-4: 1200
    level-5: 2000
```

---

**🎮 Yapıları kur, klanını güçlendir, sunucuya katkıda bulun!**
