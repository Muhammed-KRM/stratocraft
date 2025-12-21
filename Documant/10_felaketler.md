# STRATOCRAFT - FELAKET SİSTEMİ

## 🌪️ Felaketler Nedir?

Felaketler **oyuncuları merkezden çok uzaklaşmamasını ve merkeze çok yakın yerleşmemelerini sağlamak** için tasarlanmış **çok güçlü** sistemlerdir. **Tek başına başa çıkılamaz**, mutlaka takım çalışması gerektirir.

**Önemli:** Felaket bossları normal bosslardan **tamamen ayrıdır**. Normal bosslar (Titan Golem, Hydra, vb.) eğitilebilir ve klan üyesi olabilir, ama felaket bossları sadece klan kristallerini yok etmek için var ve çok daha güçlüdür.

**Ana Amaç:**
- Merkezden uzaklaşmayı engellemek
- Merkeze çok yakın yerleşmeyi engellemek
- Klan kristallerini öncelikli hedef almak
- **2 dakikada bir** yakındaki oyunculara saldırmak (felaket bossları için)

**Son Güncellemeler** ⭐:
- ✅ **İki Katmanlı Seviye Sistemi**: Kategori seviyeleri (otomatik spawn sıklığı) ve iç seviyeler (felaketin gücü)
- ✅ **Dinamik Güç Hesaplama**: Stratocraft Güç Sistemi (SGP) entegrasyonu
- ✅ **Handler Registry Sistemi**: Her felaket tipi için özel handler
- ✅ **4 Fazlı Felaket Sistemi**: Keşif, Saldırı, Öfke, Çaresizlik fazları

---

## 📋 İÇİNDEKİLER

1. [Felaket Mekaniği](#felaket-mekaniği)
2. [Felaket Tipleri](#felaket-tipleri)
3. [Klan Kristali Hedefleme](#klan-kristali-hedefleme)
4. [Mücadele Stratejileri](#mücadele-stratejileri)
5. [Admin Komutları](#admin-komutları)
6. [Ödüller](#ödüller)

---

## 💪 DİNAMİK GÜÇ SİSTEMİ (GÜNCELLENMİŞ)

### ✅ Yeni Stratocraft Güç Sistemi Entegrasyonu

**Felaketler artık oyuncuların gerçek gücüne göre ayarlanıyor!**

Felaket sistemi, **Stratocraft Güç Sistemi (SGP)** ile entegre edilmiştir. Artık felaketler sadece oyuncu sayısına değil, oyuncuların **gerçek güç puanlarına** göre güçlenir.

### Güç Hesaplama Formülü (Yeni Sistem)

**Yeni Formül:**
```
Sunucu Güç Puanı = Ortalama Oyuncu Gücü × Oyuncu Sayısı Çarpanı

Felaket Güç Çarpanı = 1.0 + (Sunucu Güç Puanı / 100.0) × Güç Artış Hızı

Felaket Can = Temel Can × Felaket Güç Çarpanı
Felaket Hasar = Temel Hasar × Felaket Güç Çarpanı
```

**Oyuncu Güç Puanı (SGP) Hesaplama:**
```
SGP = (Combat Power × 0.6) + (Progression Power × 0.4)

Combat Power = Eşya Gücü + Buff Gücü
Progression Power = Ustalık Gücü + Ritüel Gücü
```

**Eşya Gücü:**
- Silah seviyesi (1-5): 60-1600 puan
- Zırh seviyesi (1-5): 40-1000 puan (parça başına)
- Tam set bonusu: %10 ekstra

**Ustalık Gücü:**
- Her ritüel için %100 üzerine çıkış = Bonus güç
- Formül: `150 × (Ustalık% / 100)^1.4`

**Ritüel Gücü:**
- Ritüel blokları (Demir, Obsidyen, Elmas, vb.)
- Ritüel kaynakları (Demir, Elmas, Kızıl Elmas, Karanlık Madde)

**Yapı Gücü:**
- Klan yapıları seviyesine göre (1-5): 100-2000 puan
- Klan kristali: +500 puan

### Eski Sistem (Geriye Dönük Uyumluluk)

Eğer yeni güç sistemi yüklenmemişse, eski sistem kullanılır:

```
Formül:
Güç = TemelGüç × (1 + OyuncuSayısı × 0.1 + OrtKlanSeviyesi × 0.15)
```

### Config Ayarları

Tüm güç hesaplama parametreleri `config.yml` dosyasından ayarlanabilir:

```yaml
disaster:
  power:
    dynamic-difficulty:
      enabled: true
      power-scaling-factor: 1.0
      min-power-multiplier: 0.5
      max-power-multiplier: 5.0
      player-count-multiplier:
        1: 1.0
        5: 1.2
        10: 1.5
        20: 2.0
```

---

## 🔧 SON GÜNCELLEMELER (Son 3 Gün) ⭐

### İki Katmanlı Seviye Sistemi

**Yeni Özellikler:**
- ✅ **Kategori Seviyeleri**: Otomatik spawn sıklığı (1: Her gün, 2: 3 günde bir, 3: 7 günde bir)
- ✅ **İç Seviyeler**: Admin komutunda belirtilen, felaketin gücünü belirler (1: Zayıf, 2: Orta, 3: Güçlü)
- ✅ **Dinamik Güç Hesaplama**: Stratocraft Güç Sistemi (SGP) entegrasyonu
- ✅ **Handler Registry Sistemi**: Her felaket tipi için özel handler

**Algoritma:**
```java
// triggerDisaster() - Felaket başlat
public void triggerDisaster(DisasterType type, int categoryLevel, int internalLevel, Location spawnLoc) {
    // Kategori ve güç hesaplama
    DisasterCategory category = Disaster.getCategory(type);
    DisasterPower power = calculateDisasterPower(internalLevel);
    long duration = Disaster.getDefaultDuration(type, categoryLevel);
    
    // Entity oluştur (canlı felaketler için)
    Entity entity = null;
    if (category == DisasterCategory.CREATURE) {
        entity = spawnDisasterEntity(type, spawnLoc, power);
    }
    
    // Felaket oluştur
    activeDisaster = new Disaster(type, category, internalLevel, entity, spawnLoc, power, duration);
    
    // Handler'ı çağır
    DisasterHandler handler = handlerRegistry.getHandler(type);
    if (handler != null) {
        handler.onDisasterStart(activeDisaster);
    }
}
```

**Güç Hesaplama:**
- Oyuncu gücü ve sunucu gücü hesaplanır
- Seviyeye göre çarpan uygulanır
- Felaket can ve hasarı hesaplanır

Detaylı bilgi için: `SON_3_GUN_DEGISIKLIKLER_VE_SISTEM_DOKUMANI.md` dosyasına bakın.

---

## 🔄 FELAKET FAZ SİSTEMİ (YENİ)

### ✅ 4 Fazlı Felaket Sistemi

Felaketler artık **4 fazdan** geçer ve her fazda farklı davranışlar sergiler!

### Fazlar

#### 1. Keşif Fazı (EXPLORATION) - %100-75 Can
```
Özellikler:
- Hız: Normal (1.0x)
- Saldırı Aralığı: 2 dakika
- Özel Yetenek: Yok
- Oyuncu Saldırısı: Evet
```

#### 2. Saldırı Fazı (ASSAULT) - %75-50 Can
```
Özellikler:
- Hız: Hızlı (1.2x)
- Saldırı Aralığı: 90 saniye
- Özel Yetenek: 1 yetenek aktif
- Oyuncu Saldırısı: Evet
```

#### 3. Öfke Fazı (RAGE) - %50-25 Can
```
Özellikler:
- Hız: Çok Hızlı (1.5x)
- Saldırı Aralığı: 60 saniye
- Özel Yetenek: 2 yetenek aktif
- Oyuncu Saldırısı: Evet
```

#### 4. Çaresizlik Fazı (DESPERATION) - %25-0 Can
```
Özellikler:
- Hız: Maksimum (2.0x)
- Saldırı Aralığı: 30 saniye
- Özel Yetenek: 3 yetenek aktif
- Oyuncu Saldırısı: Evet (son çare!)
```

### Faz Geçiş Bildirimleri

Her faz geçişinde tüm oyunculara bildirim gönderilir:

```
"§c⚠ FELAKET UYARISI: [Felaket Adı] [Faz Adı] fazına geçti!"
```

### Config Ayarları

Faz sistemi parametreleri `config.yml` dosyasından ayarlanabilir:

```yaml
disaster:
  phase-system:
    enabled: true
    exploration:
      health-threshold: 0.75
      attack-interval: 120000
      ability-count: 0
      speed-multiplier: 1.0
    assault:
      health-threshold: 0.50
      attack-interval: 90000
      ability-count: 1
      speed-multiplier: 1.2
    rage:
      health-threshold: 0.25
      attack-interval: 60000
      ability-count: 2
      speed-multiplier: 1.5
    desperation:
      health-threshold: 0.0
      attack-interval: 30000
      ability-count: 3
      speed-multiplier: 2.0
```

---

### Felaket Seviyeleri

**İki Katmanlı Seviye Sistemi:**

Felaket sistemi iki katmanlı seviye sistemine sahiptir:

### 1. Kategori Seviyeleri (Otomatik Spawn Sıklığı)

Kategori seviyeleri felaketlerin otomatik spawn sıklığını belirler:

#### Kategori Seviyesi 1 (Her Gün)
```
Temel Güç: 500 HP
Can Çarpanı: 1.0x
Hasar Çarpanı: 1.0x
Spawn Sıklığı: Her gün
Örnekler: Güneş Patlaması, Mini felaketler, Mini dalgalar (100-500 adet)
```

#### Kategori Seviyesi 2 (3 Günde Bir)
```
Temel Güç: 1500 HP
Can Çarpanı: 1.5x
Hasar Çarpanı: 1.5x
Spawn Sıklığı: 3 günde bir
Örnekler: Deprem, Fırtına, Felaket Hiçlik Solucanı, Felaket Buzul Leviathan, Orta güçte grup (30 adet)
```

#### Kategori Seviyesi 3 (7 Günde Bir - Haftada Bir)
```
Temel Güç: 5000 HP
Can Çarpanı: 2.0x
Hasar Çarpanı: 2.0x
Spawn Sıklığı: 7 günde bir (haftada bir)
Örnekler: Felaket Titanı (30 blok boyunda), Felaket Khaos Ejderi, Felaket Boşluk Titanı, Volkanik Patlama
```

### 2. İç Seviyeler (Admin Komutunda Belirtilen - Felaketin Gücü)

İç seviyeler admin komutunda belirtilir ve felaketin gücünü (can/hasar) belirler:

#### İç Seviye 1 (Zayıf Form)
```
Güç Çarpanı: 0.8x
Kullanım: Test veya zayıf felaket için
```

#### İç Seviye 2 (Orta Form)
```
Güç Çarpanı: 1.0x
Kullanım: Normal felaket gücü
```

#### İç Seviye 3 (Güçlü Form)
```
Güç Çarpanı: 1.5x
Kullanım: Güçlü felaket için
```

**Örnek:** 
- `CATASTROPHIC_TITAN` kategori seviyesi 3'tür (7 günde bir otomatik spawn)
- Ama admin komutunda `/stratocraft disaster start 3 CATASTROPHIC_TITAN 1 ben` ile zayıf form çağırabilirsiniz
- Veya `/stratocraft disaster start 3 CATASTROPHIC_TITAN 3 ben` ile güçlü form çağırabilirsiniz

#### Özel Event Felaketleri
```
Admin tarafından manuel başlatılan özel felaketler.
Kategori seviyesi yok, sadece iç seviye (1-3) belirlenir.
Örnekler: Çok güçlü boss, Mini felaket dalgası (100-500 adet)
```

---

## 🏗️ FELAKET TİPLERİ

### A. CANAVAR FELAKETLER (CREATURE DISASTERS)

Canavar felaketler **merkezden uzakta** spawn olur ve **merkeze doğru ilerleyerek** yoldaki klan kristallerini yok eder.

**Özellikler:**
- Merkezden uzakta spawn olur (5000+ blok)
- Merkeze doğru ilerler
- **Klan kristalini öncelikli hedef alır**
- Kristal yok edilene kadar oyuncularla oyalanmaz
- **2 dakikada bir** yakındaki oyunculara saldırır
- Kristal yok edildikten sonra en yakın klan kristaline gider

**Alt Kategoriler:**

#### 1. Felaket Bossları (SINGLE_BOSS) - Normal Bosslardan Ayrı
Çok güçlü tek bir felaket bossu. Normal bosslardan tamamen ayrı, çok daha güçlü. Örnekler:
- **Felaket Titanı (CATASTROPHIC_TITAN)** - Kategori: 3 (7 günde bir) - 30 blok boyunda dev golem
- **Felaket Khaos Ejderi (CATASTROPHIC_CHAOS_DRAGON)** - Kategori: 3 (7 günde bir)
- **Felaket Boşluk Titanı (CATASTROPHIC_VOID_TITAN)** - Kategori: 3 (7 günde bir)
- **Felaket Hiçlik Solucanı (CATASTROPHIC_ABYSSAL_WORM)** - Kategori: 2 (3 günde bir)
- **Felaket Buzul Leviathan (CATASTROPHIC_ICE_LEVIATHAN)** - Kategori: 2 (3 günde bir)

**Not:** Bu felaket bossları normal boss sisteminden tamamen ayrıdır. Normal bosslar (Titan Golem, Hydra, vb.) eğitilebilir ve klan üyesi olabilir, ama felaket bossları sadece klan kristallerini yok etmek için var.

**İstatistikler:**
- Can: 10,000-50,000 HP
- Hasar: 50-200 HP/vuruş
- Çok güçlü özel yetenekler

#### 2. Orta Güçte Grup (MEDIUM_GROUP)
30 tane orta güçte canavar. Örnekler:
- **Zombi Ordusu** (ZOMBIE_HORDE) - 30 adet güçlendirilmiş zombi
- **İskelet Lejyonu** (SKELETON_LEGION) - 30 adet güçlendirilmiş iskelet
- **Örümcek Sürüsü** (SPIDER_SWARM) - 30 adet güçlendirilmiş örümcek

**İstatistikler:**
- Can: 500-2000 HP/başına
- Hasar: 10-50 HP/vuruş
- Her biri aynı kristale hedeflenir

#### 3. Mini Felaket Dalgası (MINI_SWARM)
100-500 adet mini canavar. Örnekler:
- **Creeper Dalgası** (CREEPER_SWARM) - 100-500 adet güçlendirilmiş creeper
- **Zombi Dalgası** (ZOMBIE_WAVE) - 100-500 adet güçlendirilmiş zombi

**İstatistikler:**
- Can: 100-500 HP/başına
- Hasar: 5-20 HP/vuruş
- Performans için max 500 adet

---

### B. DOĞA OLAYI FELAKETLER (NATURAL DISASTERS)

Doğa olayları **tüm dünyayı etkiler** ve belirli bir süre boyunca aktif kalır.

#### 1. Güneş Patlaması (SOLAR_FLARE) - Seviye 1
**Süre:** 10 dakika

**Etkiler:**
- Yüzeydeki oyuncular yanar (çatısız yerlerde)
- Yanıcı bloklar tutuşur (ahşap, yün, yapraklar)
- Etrafta lavlar oluşur
- Klan bölgelerinde etkisiz

**Hayatta Kalma:**
- Yeraltına sığın (Y 50 altı)
- Çatı altında kal
- Klan bölgesinde korun

#### 2. Deprem (EARTHQUAKE) - Seviye 2
**Süre:** 5 dakika

**Etkiler:**
- Rastgele konumlarda patlamalar
- Herkes sürekli yüksek hasar alır (1 kalp/2 saniye)
- Bloklar düşer (yukarıdan)
- Yapılara hasar
- Klan bölgelerinde etkisiz

**Hayatta Kalma:**
- Açık alanlardan kaç
- Yüksek binalardan uzak dur
- Klan bölgesinde korun

#### 3. Fırtına (STORM) - Seviye 2
**Süre:** 20 dakika

**Etkiler:**
- Oyuncular yaklaştıkça yıldırım düşer
- Rastgele konumlarda yıldırım
- Yüksek hasar (5 kalp)
- Klan bölgelerinde etkisiz

**Hayatta Kalma:**
- Açık alanlardan kaç
- Yıldırım çarpmasından korun
- Klan bölgesinde korun

#### 4. Meteor Yağmuru (METEOR_SHOWER) - Seviye 2
**Süre:** 20 dakika

**Etkiler:**
- Gökyüzünden meteor düşer
- Rastgele bölgelere
- Blok kırar
- 10 kalp hasar (çarparsa)

**Hayatta Kalma:**
- Çatı yap (Obsidian önerilir)
- Klan bölgesinde korun

#### 5. Volkanik Patlama (VOLCANIC_ERUPTION) - Seviye 3
**Süre:** 60 dakika

**Etkiler:**
- Lav akışı
- Kül bulutu
- Çok yüksek hasar (yanma)
- Geniş alan etkisi

---

### C. MİNİ FELAKETLER (MINI DISASTERS)

Mini felaketler **rastgele zamanda günde birkaç kez** ortaya çıkar. Çok güçlü değildir ama etkileri vardır.

**Özellikler:**
- Rastgele zamanda spawn
- Günde 2-5 kez
- Süre: 5-15 dakika
- Güç: Düşük-Orta

**Tipler:**

#### 1. Boss Güçlenme Dalgası (BOSS_BUFF_WAVE)
- Tüm bosslar %50 daha güçlü
- Süre: 10 dakika

#### 2. Mob İstilası (MOB_INVASION)
- 50 tane güçlendirilmiş mob spawn
- Rastgele konumlarda

#### 3. Oyuncu Buff Dalgası (PLAYER_BUFF_WAVE)
- Tüm oyunculara geçici güç buff'ı
- +%25 hasar, +%15 savunma
- Süre: 15 dakika

---

## 🎯 KLAN KRISTALİ HEDEFLEME

### Nasıl Çalışır?

1. **Felaket Spawn Olur:**
   - Merkezden uzakta spawn olur (5000+ blok)
   - En yakın klan kristalini bulur
   - Kristale doğru ilerler

2. **Kristale İlerleme:**
   - Felaket kristale doğru sürekli ilerler
   - Önüne çıkan blokları kırar
   - Yapıları yok eder

3. **Oyuncu Saldırısı:**
   - **2 dakikada bir** yakındaki oyunculara saldırır
   - Saldırı sonrası kristale devam eder
   - Oyuncularla oyalanmaz

4. **Kristal Yok Etme:**
   - Kristale 5 blok yaklaşınca yok eder
   - Klan dağılır
   - Yapılar yok edilir
   - En yakın klan kristaline gider

### Önemli Notlar

- **Felaketler oyuncularla oyalanmaz** - Kristal yok edilene kadar
- **2 dakikada bir saldırır** - Sadece yakındaki oyunculara
- **Kristal öncelikli hedef** - Her zaman en yakın kristale gider
- **Klan yok edilince** - Kahraman Buff'ı verilir (48 saat)

---

## ⚙️ FELAKET MEKANİĞİ

### Spawn Sistemi

```
Spawn Konumu: Merkezden uzakta (5000+ blok)

Davranış:
1. Merkezden uzakta doğar
2. En yakın klan kristalini bulur
3. Kristale doğru ilerler
4. 2 dakikada bir yakındaki oyunculara saldırır
5. Kristale 5 blok yaklaşınca yok eder
6. Klan dağılır
7. En yakın klan kristaline gider
8. Tekrarla
```

### Yıkım Etkisi

**Klan Kristali Yok Edilirse:**
```
Felaket kristale ulaştı:
→ Kristali kırar
→ Tüm yapıları yok eder
→ Klan dağılır
→ En yakın klan kristaline gider

AMA:
→ Klan üyeleri "Kahraman Buff'ı" alır!
→ +%30 hasar
→ +%20 savunma
→ +%15 hareket hızı
→ 48 saat sürer

AMAÇ: İntikam almak için güçlenirler
```

---

## 🎮 MÜCADELE STRATEJİLERİ

### Genel Strateji

1. **Takım Oluştur:**
   - Minimum 3-5 oyuncu
   - Farklı roller (tank, dps, support)

2. **Klan Kristalini Koru:**
   - Felaket kristale gidiyor
   - Kristali korumak için hazırlık yap
   - Tektonik Sabitleyici kur (felaket hasarını %90 azaltır)

3. **2 Dakikada Bir Saldırı:**
   - Felaket 2 dakikada bir saldırır
   - Bu süre zarfında hazırlık yap
   - Saldırı sonrası tekrar saldır

4. **Kristal Yok Edilirse:**
   - Kahraman Buff'ı al
   - İntikam için güçlen
   - Felaketi yok et

---

## 🛠️ ADMIN KOMUTLARI

### Test Komutları

#### Normal Felaket Test
```
/stratocraft disaster test <type> <level> [konum]

Örnekler:
/stratocraft disaster test CATASTROPHIC_TITAN 3 ben
/stratocraft disaster test EARTHQUAKE 2 100 64 200
/stratocraft disaster test SOLAR_FLARE 1
```

#### Grup Felaket Test (30 adet)
```
/stratocraft disaster test group <entity> <count> [konum]

Örnekler:
/stratocraft disaster test group ZOMBIE 30 ben
/stratocraft disaster test group SKELETON 30 100 64 200
```

#### Mini Dalga Test (100-500 adet)
```
/stratocraft disaster test swarm <entity> <count> [konum]

Örnekler:
/stratocraft disaster test swarm CREEPER 200 ben
/stratocraft disaster test swarm ZOMBIE 500 100 64 200
```

#### Mini Felaket Test
```
/stratocraft disaster test mini <type>

Örnekler:
/stratocraft disaster test mini BOSS_BUFF_WAVE
/stratocraft disaster test mini MOB_INVASION
/stratocraft disaster test mini PLAYER_BUFF_WAVE
```

### Normal Komutlar

#### Felaket Başlat
```
/stratocraft disaster start [Kategori seviyesi] <Felaket ismi> <İç seviye> [konum]

Parametreler:
- [Kategori seviyesi]: 1-3 (opsiyonel, belirtilmezse otomatik)
  - 1: Her gün gelen felaketler
  - 2: 3 günde bir gelen felaketler
  - 3: 7 günde bir gelen felaketler
- <Felaket ismi>: Felaket tipi (zorunlu)
- <İç seviye>: 1-3 (zorunlu) - Felaketin gücünü belirler
  - 1: Zayıf form (düşük can/hasar)
  - 2: Orta form (orta can/hasar)
  - 3: Güçlü form (yüksek can/hasar)
- [konum]: ben (oyuncunun yanında) veya X Y Z (koordinat) - opsiyonel

Örnekler:
/stratocraft disaster start 3 CATASTROPHIC_TITAN 3 ben
/stratocraft disaster start 1 SOLAR_FLARE 2 ben
/stratocraft disaster start 2 EARTHQUAKE 1 100 64 200
```

#### Felaketi Durdur
```
/stratocraft disaster stop
```

#### Felaket Bilgisi
```
/stratocraft disaster info
```

#### Felaket Listesi
```
/stratocraft disaster list
```

---

## 🎁 ÖDÜLLER

### Felaket Öldürüldüğünde

**Ödüller:**
- %50 şans: Karanlık Madde (1-3 adet)
- %50 şans: Yıldız Çekirdeği (1 adet)
- Her zaman: Enkaz Yığını (5x5x3 Ancient Debris)

### Enkaz Yığını (Wreckage)

**Nedir?**
```
Boss öldüğünde düştüğü yere 5x5x3 enkaz oluşur:
- Ancient Debris blokları
- Kazı ile topla

İçinden çıkanlar:
- Antik Dişli (5-10 adet)
- Hidrolik Piston (3-7 adet)
- Rastgele: Tarif Kitapları
```

### Kahraman Buff'ı (Hero Buff)

**Kimin Alır?** Base'i felaket tarafından yok edilen klan

**Etkiler** (48 saat):
```
+%30 Hasar (tüm saldırılar)
+%20 Savunma (tüm zırh)
+%15 Hareket Hızı
Glowing efekti (mavi parıltı)

AMAÇ: İntikam almak için güçlenirler!
```

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Tek Başına Yenilemez**: Tüm felaketler takım gerektirir (minimum 3-5 oyuncu)
2. **Merkezden Uzak**: Felaketler 5000+ blok uzakta spawn olur
3. **Klan Kristali Hedef**: Felaketler önce kristali yok eder
4. **2 Dakikada Bir Saldırı**: Felaketler yakındaki oyunculara saldırır
5. **Oyuncularla Oyalanmaz**: Kristal yok edilene kadar oyuncularla savaşmaz
6. **Enkaz Topla**: Öldükten sonra enkaz kazı, çok değerli!
7. **BossBar Görüntüsü**: Canlı felaketler için ekranın üst kısmında can ve süre gösterilir
8. **Çok Güçlü**: Felaketler tek başına başa çıkılamaz, mutlaka takım gerekir

---

## 📊 FELAKET TİPLERİ ÖZET TABLOSU

| Felaket | Kategori | Tip | Kategori Seviyesi | Spawn Sıklığı | Süre |
|---------|----------|-----|------------------|---------------|------|
| Felaket Titanı (CATASTROPHIC_TITAN) | Canavar | Felaket Bossu | 3 | 7 günde bir | 30 dk |
| Felaket Khaos Ejderi (CATASTROPHIC_CHAOS_DRAGON) | Canavar | Felaket Bossu | 3 | 7 günde bir | 30 dk |
| Felaket Boşluk Titanı (CATASTROPHIC_VOID_TITAN) | Canavar | Felaket Bossu | 3 | 7 günde bir | 30 dk |
| Felaket Hiçlik Solucanı (CATASTROPHIC_ABYSSAL_WORM) | Canavar | Felaket Bossu | 2 | 3 günde bir | 20 dk |
| Felaket Buzul Leviathan (CATASTROPHIC_ICE_LEVIATHAN) | Canavar | Felaket Bossu | 2 | 3 günde bir | 20 dk |
| Zombi Ordusu | Canavar | Grup (30) | 2 | 3 günde bir | 20 dk |
| İskelet Lejyonu | Canavar | Grup (30) | 2 | 3 günde bir | 20 dk |
| Creeper Dalgası | Canavar | Mini Dalga (100-500) | 1 | Her gün | 10 dk |
| Güneş Patlaması | Doğa | - | 1 | Her gün | 10 dk |
| Deprem | Doğa | - | 2 | 3 günde bir | 5 dk |
| Fırtına | Doğa | - | 2 | 3 günde bir | 20 dk |
| Meteor Yağmuru | Doğa | - | 2 | 3 günde bir | 20 dk |
| Volkanik Patlama | Doğa | - | 3 | Haftada bir | 60 dk |
| Boss Buff Dalgası | Mini | - | 1 | Günlük (2-5 kez) | 5-15 dk |
| Mob İstilası | Mini | - | 1 | Günlük (2-5 kez) | 5-15 dk |
| Oyuncu Buff Dalgası | Mini | - | 1 | Günlük (2-5 kez) | 5-15 dk |

---

**🎮 Felaketlere karşı takımla birleş, kristalleri koru, Kahraman ol!**
