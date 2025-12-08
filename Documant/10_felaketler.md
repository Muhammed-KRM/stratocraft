# STRATOCRAFT - FELAKET SİSTEMİ

## 🌪️ Felaketler Nedir?

Felaketler **oyuncuları merkezden çok uzaklaşmamasını ve merkeze çok yakın yerleşmemelerini sağlamak** için tasarlanmış **çok güçlü** sistemlerdir. **Tek başına başa çıkılamaz**, mutlaka takım çalışması gerektirir.

**Ana Amaç:**
- Merkezden uzaklaşmayı engellemek
- Merkeze çok yakın yerleşmeyi engellemek
- Klan kristallerini öncelikli hedef almak

---

## 📋 İÇİNDEKİLER

1. [Felaket Mekaniği](#felaket-mekaniği)
2. [Felaket Tipleri](#felaket-tipleri)
3. [Klan Kristali Hedefleme](#klan-kristali-hedefleme)
4. [Mücadele Stratejileri](#mücadele-stratejileri)
5. [Admin Komutları](#admin-komutları)
6. [Ödüller](#ödüller)

---

## 💪 DİNAMİK GÜÇ SİSTEMİ

### Güç Hesaplama Formülü

**Felaketler dinamik güçte!**

```
Formül:
Güç = TemelGüç × (1 + OyuncuSayısı × 0.1 + OrtKlanSeviyesi × 0.15)

Örnek:
Temel Güç: 100
Oyuncu Sayısı: 10
Ortalama Klan Seviyesi: 3

Güç = 100 × (1 + 10 × 0.1 + 3 × 0.15)
    = 100 × (1 + 1.0 + 0.45)
    = 100 × 2.45
    = 245

Sonuç: Felaket 245 güçte spawn olur!
```

**Faktörler**:
```
1. Oyuncu Sayısı:
   - Daha fazla oyuncu = Daha güçlü felaket
   - Her oyuncu +%10 güç

2. Klan Seviyesi:
   - Yüksek seviye klanlar = Daha güçlü felaket
   - Her seviye +%15 güç

3. Temel Güç:
   - Felaket seviyesine göre (1-4)
```

---

### Felaket Seviyeleri

**4 Seviye Felaket Sistemi:**

#### Seviye 1 (Günlük)
```
Temel Güç: 500 HP
Can Çarpanı: 1.0x
Hasar Çarpanı: 1.0x
Spawn Sıklığı: Her gün
Örnekler: Mini felaketler, Güneş Patlaması, Mini dalgalar
```

#### Seviye 2 (Orta)
```
Temel Güç: 1500 HP
Can Çarpanı: 1.5x
Hasar Çarpanı: 1.5x
Spawn Sıklığı: 3 günde bir
Örnekler: Deprem, Fırtına, Orta güçte grup (30 adet)
```

#### Seviye 3 (Büyük)
```
Temel Güç: 5000 HP
Can Çarpanı: 2.0x
Hasar Çarpanı: 2.0x
Spawn Sıklığı: Haftada bir
Örnekler: Tek Boss (Titan Golem, Khaos Ejderi), Volkanik Patlama
```

#### Seviye 4 (Mega)
```
Temel Güç: 10000+ HP
Can Çarpanı: 3.0x
Hasar Çarpanı: 3.0x
Spawn Sıklığı: 2 haftada bir
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

#### 1. Tek Boss Felaketi (SINGLE_BOSS)
Çok güçlü tek bir boss. Örnekler:
- **Titan Golem** (Seviye 3)
- **Khaos Ejderi** (Seviye 3)
- **Boşluk Titanı** (Seviye 3)
- **Hiçlik Solucanı** (Seviye 2)
- **Buzul Leviathan** (Seviye 2)

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
/stratocraft disaster test TITAN_GOLEM 3 ben
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
/stratocraft disaster start <type> [level] [konum]

Örnekler:
/stratocraft disaster start TITAN_GOLEM 3
/stratocraft disaster start SOLAR_FLARE 1 ben
/stratocraft disaster start EARTHQUAKE 2 100 64 200
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

| Felaket | Kategori | Tip | Seviye | Spawn Sıklığı | Süre |
|---------|----------|-----|--------|---------------|------|
| Titan Golem | Canavar | Tek Boss | 3 | Haftada bir | 30 dk |
| Khaos Ejderi | Canavar | Tek Boss | 3 | Haftada bir | 30 dk |
| Boşluk Titanı | Canavar | Tek Boss | 3 | Haftada bir | 30 dk |
| Hiçlik Solucanı | Canavar | Tek Boss | 2 | 3 günde bir | 20 dk |
| Buzul Leviathan | Canavar | Tek Boss | 2 | 3 günde bir | 20 dk |
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
