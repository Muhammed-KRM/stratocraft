# STRATOCRAFT - YAPILAR SİSTEMİ

## 🏗️ Yapılar Nedir?

Yapılar, oyunda **pasif güç veren** veya **özel işlevler sağlayan** fiziksel inşaatlardır. Tüm yapılar **Yapı Çekirdeği** sistemi ile çalışır.

---

## 📋 İÇİNDEKİLER

1. [Yapı Çekirdeği Sistemi](#yapı-çekirdeği-sistemi) ⭐ YENİ
2. [Yapı Kategorileri](#yapi-kategorileri)
3. [Yapı Sahiplik Sistemi](#yapı-sahiplik-sistemi) ⭐ YENİ
4. [Klan Yapıları](#klan-yapilari)
5. [Yönetim Yapıları](#yönetim-yapilari) ⭐ YENİ
6. [Yapı Tarifleri](#yapı-tarifleri) ⭐ GÜNCELLENDİ
7. [Yapı Aktivasyonu](#yapı-aktivasyonu)
8. [Yapı Güç Sistemi](#yapı-güç-sistemi)

---

## 🔧 YAPI ÇEKİRDEĞİ SİSTEMİ ⭐ YENİ

### Yapı Çekirdeği Nedir?

**Yapı Çekirdeği**, tüm yapıların temel taşıdır. Özel bir blok olarak çalışır ve metadata ile işaretlenir.

**Özellikler**:
- ✅ **Özel Blok**: `OAK_LOG` materialı kullanır ama özel bir item ile yerleştirilir
- ✅ **Metadata Kontrolü**: Normal OAK_LOG blokları yapı çekirdeği olarak algılanmaz
- ✅ **Yapı Çekirdeği Item'ı**: `STRUCTURE_CORE` item'ı ile yerleştirilir
- ✅ **Aktivasyon**: Yapı çekirdeği yerleştirildikten sonra yapı kurulur ve aktivasyon item'ı ile aktifleştirilir

### Yapı Çekirdeği Yerleştirme

**Adımlar**:
```
1. Elinde "Yapı Çekirdeği" (STRUCTURE_CORE) item'ı olmalı
2. Yerleştirmek istediğin yere sağ tık yap
3. OAK_LOG bloğu yerleştirilir ve metadata ile işaretlenir
4. "✓ Yapı çekirdeği yerleştirildi!" mesajı gelir
5. Etrafına yapıyı kur ve aktivasyon item'ı ile aktifleştir
```

**Önemli**:
- Normal OAK_LOG blokları yapı çekirdeği olarak algılanmaz
- Sadece STRUCTURE_CORE item'ı ile yerleştirilen bloklar yapı çekirdeği olur
- Metadata kontrolü sayesinde güvenlik sağlanır

---

## 🏛️ YAPI KATEGORİLERİ

### Kategori Sistemi

```
YAPILAR
├── 1. KLAN YAPILARI (CLAN_ONLY)
│   └── Sadece klan bölgesinde
│       Büyük, pahalı, güçlü
│
└── 2. YÖNETİM YAPILARI
    ├── A) PUBLIC (Herkes İçin)
    │   └── Herkes kullanabilir
    │       Her yere yapılabilir
    │
    └── B) CLAN_ONLY (Klan İçin)
        └── Sadece klan bölgesinde
            Klan üyeleri kullanabilir
```

---

## 🔐 YAPI SAHİPLİK SİSTEMİ ⭐ YENİ

### Sahiplik Tipleri

**1. CLAN_ONLY (Klan Yapıları)**:
- ✅ Sadece klan bölgesine yapılabilir
- ✅ Sadece klan üyeleri kullanabilir
- ✅ Sahiplik kontrolü: Klan üyeliği gerekli
- **Örnekler**: Simya Kulesi, Zehir Reaktörü, Klan Bankası, vb.

**2. CLAN_OWNED (Klan Sahipli Yapılar)**:
- ✅ Klan dışına yapılabilir
- ✅ Sadece yapan oyuncu ve klanı kullanabilir
- ✅ Sahiplik kontrolü: Yapan oyuncu veya klan üyeliği gerekli
- **Örnekler**: Şu an için özel bir yapı yok (ileride eklenebilir)

**3. PUBLIC (Herkese Açık Yapılar)**:
- ✅ Her yere yapılabilir
- ✅ Herkes kullanabilir
- ✅ Sahiplik kontrolü: YOK (herkese açık)
- **Örnekler**: Kişisel Görev Loncası, Kontrat Bürosu, Market, Tarif Kütüphanesi

---

## 📍 YAPI YERLEŞİM KURALLARI

**1. CLAN_ONLY Yapılar**:
```
Nereye yapılır: SADECE klan bölgesi içinde
Kısıtlama: Çit sınırları içinde olmalı
Özellik: Büyük, karmaşık, pahalı
Kullanım: Sadece klan üyeleri
```

**2. PUBLIC Yapılar**:
```
Nereye yapılır: Blok koyma izni olan her yere
YASAK yerler:
  ❌ Spawn bölgesi
  ❌ Başkasının klan bölgesi

İZİN VERİLEN yerler:
  ✅ Vahşi alanlar
  ✅ Kendi klan bölgen dışı
  ✅ Tarafsız topraklar

Kullanım: Herkes kullanabilir
```

---

## ⚙️ YAPI AKTİVASYON SİSTEMİ

### 🔧 Yeni Aktivasyon Yöntemi ⭐ GÜNCELLENDİ

**ÖNEMLİ**: Tüm yapı aktivasyonları için **Yapı Çekirdeği** sistemi kullanılır!

**Aktivasyon Adımları**:
```
1. Elinde "Yapı Çekirdeği" (STRUCTURE_CORE) item'ı al
2. Yapıyı kurmak istediğin yere çekirdeği yerleştir
3. Etrafına yapı tarifine göre blokları kur
4. Elinde aktivasyon item'ı olmalı (yapı tipine göre değişir)
5. Yapı çekirdeğine (OAK_LOG) sağ tık yap
6. Sistem tarifi kontrol eder (async)
7. Doğruysa → Yapı aktif olur!
```

**Aktivasyon Item'ları**:
- Her yapı tipi için farklı aktivasyon item'ı gerekir
- Örnek: Simya Kulesi için Gold Ingot + Diamond
- Örnek: Zehir Reaktörü için Prismarine + Spider Eye

**Aktivasyon Kontrolleri**:
- ✅ Yapı çekirdeği var mı? (OAK_LOG + metadata)
- ✅ Yapı çekirdeği aktif mi? (inactive core)
- ✅ Elinde aktivasyon item'ı var mı?
- ✅ Yapı tarifi doğru mu? (async kontrol)
- ✅ Klan kontrolü (kişisel yapılar hariç)
- ✅ Bölge kontrolü (CLAN_ONLY yapılar için)

---

# 🏰 KLAN YAPILARI (CLAN_ONLY)

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

### 2. Simya Kulesi (ALCHEMY_TOWER)

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 3x3 Bookshelf platformu (4-5 blok yüksek)
Üstünde: Beacon
```

**Boyut**: 5x5x10 blok

**İşlev**: Bataryaların gücünü artırır

**Seviyeler**:
```
Lv1 (3 blok): Bataryalar +%10 güç
Lv2 (4 blok): Bataryalar +%20 güç
Lv3 (5 blok): Bataryalar +%35 güç
```

**Aktivasyon**: 32 Gold Ingot + 16 Diamond

---

### 3. Zehir Reaktörü (POISON_REACTOR)

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Etrafında: 3x3 Prismarine (4 blok yüksek)
Üstünde: Beacon
```

**Boyut**: 7x7x8 blok

**İşlev**: Bölgeye giren düşmanlara sürekli zehir

**Seviyeler**:
```
Lv1: Poison I (sürekli)
Lv2: Poison II (sürekli)
Lv3: Poison III + Slowness I
```

**Menzil**: 30 blok yarıçap

**Aktivasyon**: 16 Prismarine + 8 Spider Eye

---

### 4. Tektonik Sabitleyici (TECTONIC_STABILIZER)

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**: Şema dosyası (`tectonic_stabilizer.schem`)

**Boyut**: 9x9x12 blok (ÇOK BÜYÜK!)

**İşlev**: Felaket hasarını azaltır

**Seviyeler**:
```
Lv1: %50 felaket hasar azaltma
Lv2: %70 felaket hasar azaltma
Lv3: %90 felaket hasar azaltma
```

**Gereksinim**: **Tarif Kitabı** (Boss dropu)

**Aktivasyon**: 16 Titanium Ingot + 8 Piston

---

### 5. Gözetleme Kulesi (WATCHTOWER)

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Etrafında: 3x3 Stone Brick kule (10 blok yüksek)
```

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

### 6. Otomatik Taret (AUTO_TURRET)

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 2x2 Iron Block
Üstünde: Dispenser
Altında (yükseklik): 3-5 blok Iron Block (seviye belirler)
```

**Boyut**: 3x3x4 blok

**İşlev**: Otonom ok savunması

**Seviyeler**:
```
Lv1: 1 ok/sn (2 kalp)
Lv2: 2 ok/sn (3 kalp) + ateşli ok
Lv3: 3 ok/sn (5 kalp) + patlayıcı ok
```

**Menzil**: 20 blok

**Gereksinim**: Hurda Teknolojisi (Felaket enkazı)

**Aktivasyon**: Antik Dişli + Piston

---

# 🏢 YÖNETİM YAPILARI ⭐ YENİ

## 📋 Yönetim Yapıları Listesi

### 1. Kişisel Görev Loncası (PERSONAL_MISSION_GUILD) - PUBLIC

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Cobblestone
Üstünde: Lectern
```

**Sahiplik**: PUBLIC (Herkes kullanabilir)

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

### 2. Klan Yönetim Merkezi (CLAN_MANAGEMENT_CENTER) - CLAN_ONLY

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 3x3 Iron Block
Üstünde: Beacon
```

**Sahiplik**: CLAN_ONLY (Sadece klan bölgesinde)

**İşlev**:
- Klan menüleri
- Klan yönetimi
- Üye yönetimi

---

### 3. Klan Bankası (CLAN_BANK) - CLAN_ONLY

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Gold Block
Üstünde: Chest
```

**Sahiplik**: CLAN_ONLY (Sadece klan bölgesinde)

**İşlev**:
- Klan bankası
- Ortak depolama
- Para yönetimi

---

### 4. Klan Görev Loncası (CLAN_MISSION_GUILD) - CLAN_ONLY

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Emerald Block
Üstünde: Lectern
```

**Sahiplik**: CLAN_ONLY (Sadece klan bölgesinde)

**İşlev**:
- Klan görevleri
- Klan içine yapılır
- Klan üyeleri görev alabilir

---

### 5. Eğitim Alanı (TRAINING_ARENA) - CLAN_ONLY

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 2x2 Iron Block
Üstünde: Enchanting Table
```

**Sahiplik**: CLAN_ONLY (Sadece klan bölgesinde)

**İşlev**:
- Eğitim alanı
- Canlı eğitimi
- Üreme sistemi

---

### 6. Kervan İstasyonu (CARAVAN_STATION) - CLAN_ONLY

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 2x2 Iron Block
Üstünde: Chest
```

**Sahiplik**: CLAN_ONLY (Sadece klan bölgesinde)

**İşlev**:
- Kervan istasyonu
- Şubeler arası transfer
- Lojistik sistemi

---

### 7. Kontrat Bürosu (CONTRACT_OFFICE) - PUBLIC

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Stone
Üstünde: Crafting Table
```

**Sahiplik**: PUBLIC (Herkes kullanabilir)

**İşlev**:
- Kontrat bürosu
- Genel kullanım
- Oyuncular arası kontratlar

---

### 8. Market (MARKET_PLACE) - PUBLIC

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Coal Block
Üstünde: Chest
Yanında: Sign
```

**Sahiplik**: PUBLIC (Herkes kullanabilir)

**İşlev**:
- Market
- Ticaret merkezi
- Oyuncular arası ticaret

---

### 9. Tarif Kütüphanesi (RECIPE_LIBRARY) - PUBLIC

**Yapı Çekirdeği**: OAK_LOG (merkez)

**Tarif**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Bookshelf
Üstünde: Lectern
```

**Sahiplik**: PUBLIC (Herkes kullanabilir)

**İşlev**:
- Tarif kütüphanesi
- Tarif depolama
- Araştırma bonusu

---

## 📊 YAPI TARİFLERİ ⭐ GÜNCELLENDİ

### Kod Tabanlı Tarifler

Tüm kod tabanlı tarifler **OAK_LOG** yapı çekirdeği kullanır:

**1. Kişisel Görev Loncası (PERSONAL_MISSION_GUILD)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Cobblestone
Üstünde: Lectern
```

**2. Klan Bankası (CLAN_BANK)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Gold Block
Üstünde: Chest
```

**3. Kontrat Bürosu (CONTRACT_OFFICE)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Stone
Üstünde: Crafting Table
```

**4. Klan Görev Loncası (CLAN_MISSION_GUILD)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Emerald Block
Üstünde: Lectern
```

**5. Market (MARKET_PLACE)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Coal Block
Üstünde: Chest
```

**6. Tarif Kütüphanesi (RECIPE_LIBRARY)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: Bookshelf
Üstünde: Lectern
```

**7. Klan Yönetim Merkezi (CLAN_MANAGEMENT_CENTER)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 3x3 Iron Block
Üstünde: Beacon
```

**8. Eğitim Alanı (TRAINING_ARENA)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 2x2 Iron Block
Üstünde: Enchanting Table
```

**9. Kervan İstasyonu (CARAVAN_STATION)**:
```
Merkez: OAK_LOG (Yapı Çekirdeği)
Altında: 2x2 Iron Block
Üstünde: Chest
```

### Şema Tabanlı Tarifler

Karmaşık yapılar için şema dosyaları kullanılır:

- **Simya Kulesi** (`alchemy_tower.schem`)
- **Tektonik Sabitleyici** (`tectonic_stabilizer.schem`)
- **Zehir Reaktörü** (`poison_reactor.schem`)
- **Otomatik Taret** (`auto_turret.schem`)
- **Global Pazar Kapısı** (`market_gate.schem`)

**Not**: Şema tabanlı yapılar da **OAK_LOG** yapı çekirdeği kullanır!

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
- Klan Bankası (depolama)
- Klan Görev Loncası (görevler)
```

---

### Sosyal İçin (Herkes)

**Ticaret Bölgesi**:
```
Spawn yakınına (PUBLIC yapılar):
- Kişisel Görev Loncası (görevler)
- Kontrat Bürosu (kontratlar)
- Market (ticaret)
- Tarif Kütüphanesi (tarifler)
→ Sosyal merkez!
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Yerleşim Kuralları

**CLAN_ONLY Yapılar**:
- ✅ Sadece klan bölgesi içinde
- ❌ Dışarıda YAPILAMAZ
- Büyük alan gerektirir

**PUBLIC Yapılar**:
- ✅ Vahşi alanlarda
- ❌ Spawn'da yapılamaz
- ❌ Düşman bölgesinde yapılamaz
- Herkes kullanabilir

---

### Yapı Maliyetleri

**CLAN_ONLY Yapılar**:
- Çok pahalı (Boss malzemeleri)
- Tarif kitabı gerekebilir
- Takım halinde toplanmalı

**PUBLIC Yapılar**:
- Çok ucuz (Taş, Odun)
- Yeni oyuncular bile yapabilir
- Sosyal yardım amaçlı

---

## 🏗️ YAPI GÜÇ SİSTEMİ

### ✅ Yapı Seviyesi Güç Kazanma

**Yapılar artık klan gücüne katkı sağlıyor!**

Her yapı, seviyesine göre **Güç Puanı (Power Score)** verir ve klanın toplam gücünü artırır.

### Yapı Gücü Hesaplama

**Yapı Seviyesi → Güç**:
```
- Seviye 1: 100 puan
- Seviye 2: 250 puan
- Seviye 3: 500 puan
- Seviye 4: 1200 puan
- Seviye 5: 2000 puan
```

**Klan Kristali**:
```
Klan Kristali: +500 puan (sabit bonus)
- Sadece kristal varsa ve ölü değilse
```

### Örnek Hesaplama

**Örnek Klan**:
```
- Klan Kristali: +500 puan
- Simya Kulesi (Seviye 3): +500 puan
- Tektonik Sabitleyici (Seviye 4): +1200 puan
- Toplam Yapı Gücü: 500 + 500 + 1200 = 2200 puan
```

### Klan Gücüne Etkisi

**Yapı Gücü**:
- Klanın toplam gücüne eklenir
- Klan seviyesi hesaplamasında kullanılır
- Felaket zorluğunu etkiler

### Komutlar

**Yapı gücünü görmek için**:
```
/sgp clan
```

**Klan güç bileşenlerini görmek için**:
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
