# STRATOCRAFT - FELAKET SİSTEMİ

## 🌪️ Felaketler Nedir?

Felaketler **doğa olayları**dır, normal boss değil! **Merkezden uzakta** spawlanır ve **merkeze doğru ilerleyerek** yoldaki baseleri yok eder.

---

## 📋 İÇİNDEKİLER

1. [Felaket Mekaniği](#felaket-mekaniği)
2. [5 Feladet Tipi](#5-felaket-tipi)
3. [Mücadele Stratejileri](#mücadele-stratejileri)
4. [Ödüller](#ödüller)


---

## 💪 DİNAMİK GÜÇ SİSTEMİ

### Güç Hesaplama Formülü

**Felaketler artık dinamik güçte!**

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
   - Felaket seviyesine göre (1-3)
```

---

### Felaket Seviyeleri

**3 Seviye Felaket**:

#### Seviye 1 (Kolay)
```
Temel Güç: 100
Can Çarpanı: 1.0x
Hasar Çarpanı: 1.0x
Spawn Sıklığı: Sık (her 30 dakika)
```

#### Seviye 2 (Orta)
```
Temel Güç: 200
Can Çarpanı: 1.5x
Hasar Çarpanı: 1.5x
Spawn Sıklığı: Orta (her 1 saat)
```

#### Seviye 3 (Zor)
```
Temel Güç: 300
Can Çarpanı: 2.0x
Hasar Çarpanı: 2.0x
Spawn Sıklığı: Nadir (her 2 saat)
```

---

### Otomatik Spawn Sistemi

**Felaketler otomatik spawn olur!**

```
Kontrol Sıklığı: Her 10 dakika

Spawn Şansı:
- Seviye 1: %50 (sık)
- Seviye 2: %30 (orta)
- Seviye 3: %10 (nadir)

Koşullar:
✓ Aktif felaket yok
✓ En az 3 oyuncu online
✓ Spawn şansı tuttu
```

---

### BossBar Sayaç

**Felaket sırasında ekranda sayaç!**

**Canlı Felaketler** (Titan Golem, Hiçlik Solucanı, vb.) için:
- Ekranın üst kısmında **BossBar** gösterilir
- Felaket ismi
- Can gösterimi: `Can/Maksimum Can` (örn: `500/500`)
- Kalan süre: `⏰ 5:23` formatında
- Can barı: Progress bar ile görsel gösterim
- Renk değişimi: 
  - Kırmızı (>%60 can)
  - Sarı (%30-60 can)
  - Yeşil (<%30 can)

**Doğa Olayları** (Güneş Fırtınası, Deprem, vb.) için:
- Ekranın alt kısmında **ActionBar** gösterilir
- Felaket ismi
- Kalan süre: `⏰ 5:23` formatında

---

## ⚙️ FELAKET MEKANİĞİ


### Spawn Sistemi

```
Spawn Konumu: Haritanın köşesi (5000+ blok uzakta)

Davranış:
1. Merkezden uzakta doğar
2. MERKEZE DOĞRU ilerler
3. Yolda base bulursa → YOK EDER
4. Base yok edince → Bir sonraki base'e gider
5. Merkeze ulaşırsa → Etraftaki baseleri tek tek yok eder
```

---

### Yıkım Etkisi

**Base Yok Edilirse**:
```
Felaket base'i buldu:
→ Tüm yapıları yok eder
→ Kristali kırar
→ Klan dağılır

AMA:
→ Klan üyeleri "Kahraman Buff'ı" alır!
→ +%30 hasar
→ +%20 savunma
→ 48 saat sürer

AMAÇ: İntikam almak için güçlenirler
```

---

### Güç Dengesi

```
Yakında Base Varsa:
→ Feladet max güçte
→ Base yok eder

Yakında Base Yoksa:
→ Feladet gücü azalır (%50 hasar düşüşü)
→ Daha kolay yenilir

STRATEJI: Felaket merkezden uzaklaştıkça zayıflar
```

---

## 🗿 9 FELAKET TİPİ

### 1. Yürüyen Dağ (TITAN GOLEM)

**Görünüm**: Dev Giant (4 kat büyük)

**İstatistikler**:
```
Can: 500 HP (250 kalp)
Hasar: 25 (12.5 kalp/vuruş)
Hız: Yavaş ama durdurulamaz
```

**Özel Yetenekler**:
```
Her 5 saniyede:

1. Toprak Fırlatma:
   → Düşmana toprak bloğu fırlatır
   → 5 kalp hasar

2. Zıplama:
   → En yakın düşmana zıplar
   → Landing patlama (alan hasar)

3. Şok Dalgası:
   → 8 blok çaptaki herkese 10 kalp
   → Geri itme + ELECTRIC_SPARK partikül
```

**Zayıf Nokta**: Arkasındaki "soğutma panelleri" (vur arkadan!)

**Spawn**: Haritanın kuzey-batı köşesi

---

### 2. Hiçlik Solucanı (ABYSSAL WORM)

**Görünüm**: Dev Silverfish (yeraltında)

**İstatistikler**:
```
Can: 300 HP (150 kalp)
Hasar: Orta
Hız: Yeraltında hızlı
```

**Davranış**:
```
- Yer ALTINDAN merkeze ilerler
- Yüzeye çıkmaz (normalde)
- Baselerin TEMELLERİNİ kazar
- Blokları yutar
```

**Sismik Çekiç Bataryası**:
```
Kurulum: Özel blok dizilimi
Kullanım: Shift + Sağ tık
Etki: Solucanı YÜZEYE ÇIKMAYA ZORLA

Mesaj: "SİSMİK ÇEKİÇ! Hiçlik Solucanı yüzeye çıkmaya zorlandı!"
```

**Mücadele**:
```
1. Sismik Çekiç kur
2. Solucanı yüzeye çıkar
3. Hepiniz birlikte saldırın
4. Tekrar kaçarsa → Tekrar çekiç kullan
```

**Spawn**: Yeraltı (Y -50 altı), uzak bölgeler

---

### 3. Güneş Fırtınası (SOLAR FLARE)

**Tip**: Yaratık değil, **10 dakika süren OLAY**

**Etki**:
```
- Gökyüzü KIRMIZI olur
- Yüzeyde duranlar YANAR (Fire Damage)
- Ahşap yapılar TUTUŞURolve
- Ekinler KURURUR
```

**Hayatta Kalma**:
```
Seçenek 1: Yeraltına sığın
→ Y 50 altına in
→ 10 dakika bekle

Seçenek 2: Ozon Kalkanı Bataryası
→ Ritüel kur (özel tarif)
→ Bölgeyi korur
→ Pahalı ama etkili
```

**Uyarı**:
```
Başlamadan 2 dakika önce:
"UYARI! Güneş Fırtınası yaklaşıyor! Yeraltına sığının!"
```

**Spawn**: Rastgele, gündüz saatlerinde

---

### 4. Khaos Ejderi (CHAOS_DRAGON)

**Görünüm**: Dev Ender Dragon

**İstatistikler**:
```
Can: 600 HP (300 kalp)
Hasar: 30 (15 kalp/vuruş)
Hız: Hızlı (uçuyor)
```

**Özel Yetenekler**:
```
- Ateş püskürtme (50 blok menzil)
- Oyuncuları yakma
- Yüksek hasar
```

**Spawn**: Gökyüzünde, merkeze doğru uçar

---

### 5. Boşluk Titanı (VOID_TITAN)

**Görünüm**: Dev Wither

**İstatistikler**:
```
Can: 700 HP (350 kalp)
Hasar: 35 (17.5 kalp/vuruş)
Hız: Orta
```

**Özel Yetenekler**:
```
- Boşluk patlaması (rastgele konumlarda)
- Yüksek patlama hasarı
- Blok yıkma
```

**Spawn**: Yer seviyesinde, merkeze doğru ilerler

---

### 6. Buzul Leviathan (ICE_LEVIATHAN)

**Görünüm**: Dev Elder Guardian (buzda)

**İstatistikler**:
```
Can: 400 HP (200 kalp)
Hasar: Donma + 20 (10 kalp/vuruş)
Hız: Suda/buzda hızlı
```

**Özel Yetenekler**:
```
- Etrafındaki oyuncuları dondurma (30 blok menzil)
- Blokları buz yapma
- Mining Fatigue efekti
```

**Etki**:
```
- Suda yüzerken base bulursa donlaırr
- Tüm bloklar ICE olur
- Yapılar bozulur
- Klan donma hasarı alır
```

**Mücadele**: Ateş bataryaları + lav

---

### 7. Meteor Yağmuru (METEOR_SHOWER)

**Tip**: Doğa Olayı (20 dakika)

**Etki**:
```
- Gökyüzünden FallingBlock (Anvil) düşer
- Rastgele bölgelere
- Blok kırar
- 10 kalp hasar (çarparsa)
```

**Hayatta Kalma**: Çatı yap (Obsidian)

---

### 8. Deprem (EARTHQUAKE)

**Tip**: Doğa Olayı (5 dakika)

**Etki**:
```
- Yer sarsılır
- Bloklar düşer
- Yapılara hasar
```

---

### 9. Volkanik Patlama (VOLCANIC_ERUPTION)

**Tip**: Doğa Olayı (60 dakika)

**Etki**:
```
- Lav akışı
- Kül bulutu
- Çok yüksek hasar (yanma)
- Geniş alan etkisi
```

---

## 🎯 MÜCADELE STRATEJİLERİ

### Titan Golem

**Takım Kompozisyonu**:
```
3 Okçu (uzaktan arkasına vur)
2 Savaşçı (dikkat dağıt)
1 Destek (potion at, heal)
```

**Taktik**:
```
1. Önden YAKLAŞMAY!
2. Yan/arkadan saldır
3. Şok Dalgası gelince KAÇÇ
4. Tekrar saldır
5. Tekrarla
```

---

### Hiçlik Solucanı

**Ekipman**:
```
- Sismik Çekiç Bataryası (3 adet)
- Ateş Topu bataryası (50x)
- Potion of Strength
```

**Taktik**:
```
1. Sismik Çekiç kur (3 farklı yere)
2. Solucan yere gelince AKTİFLEŞTİR
3. Yüzeye çıkınca HIZLA saldır
4. Tekrar kaçınca → 2. Çekiç
5. Bitirene kadar tekrarla
```

---

### Güneş Fırtınası

**Hazırlık** (Uyarıdan sonra):
```
2 dakika var:

1. Tüm önemli eşyaları sandığa koy
2. Yeraltı sığınağına git (Y 30)
3. Gıda/potion hazırla
4. 10 dakika bekle
5. BAĞTTI - Yüzeye çık
```

**Ozon Kalkanı** (İleri Seviye):
```
Tarif Kitabı gerekli (Boss dropu)

Kurulum: Özel yapı (pahalı)
Etki: Bölgeyi korur
Maliyet: 50 Elmas + 10 Yakut
```

---

## 🎁 ÖDÜLLER

### Boss Dropları

**Feladet Öldürüldüğünde**:
```
%50 şans: Karanlık Madde (1-3 adet)
%50 şans: Yıldız Çekirdeği (1 adet)

Her zaman: Enkaz Yığını (5x5 Ancient Debris)
```

---

### Enkaz Yığını (Wreckage)

**Nedir?**:
```
Boss öldüğünde düştüğü yere 5x5x3 enkaz oluşur:
- Ancient Debris blokları
- Kazı ile topla

İçinden çıkanlar:
- Antik Dişli (5-10 adet)
- Hidrolik Piston (3-7 adet)
- Rastgele: Tarif Kitapları
```

**Kullanım**:
```
Antik Dişli + Hidrolik Piston:
→ Otomatik Taret craft
→ Drone İstasyonu upgrade
→ Gelişmiş yapılar
```

---

### Kahraman Buff'ı (Hero Buff)

**Kimin Alır?**: Base'i felaket tarafından yok edilen klan

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
3. **Merkeze Doğru**: Sürekli merkeze ilerlerler
4. **Base Bulursa**: O base'i yok edene kadar durmaz
5. **Enkaz Topla**: Öldükten sonra enkaz kazı, çok değerli!
6. **BossBar Görüntüsü**: Canlı felaketler için ekranın üst kısmında can ve süre gösterilir
7. **9 Felaket Tipi**: 5 canlı felaket + 4 doğa olayı

---

**🎮 Felaketlere karşı takımla birleş, dropları topla, Kahraman ol!**

---

## 📊 BOSSBAR GÖRÜNTÜSÜ

### Canlı Felaketler İçin BossBar

Tüm canlı felaketler (Titan Golem, Hiçlik Solucanı, vb.) spawn edildiğinde:
- Ekranın **üst kısmında** BossBar gösterilir
- **Can gösterimi**: `Can/Maksimum Can` formatında
- **Kalan süre**: `⏰ 5:23` formatında
- **Progress bar**: Can yüzdesine göre görsel gösterim
- **Renk değişimi**: 
  - 🔴 Kırmızı: >%60 can
  - 🟡 Sarı: %30-60 can
  - 🟢 Yeşil: <%30 can

### Doğa Olayları İçin ActionBar

Doğa olayları (Güneş Fırtınası, Deprem, vb.) için:
- Ekranın **alt kısmında** ActionBar gösterilir
- **Felaket ismi** ve **kalan süre** gösterilir

### Tüm Bosslar İçin BossBar

Tüm bosslar spawn edildiğinde:
- Ekranın **üst kısmında** BossBar gösterilir
- **Boss ismi** ve **faz bilgisi** (çok fazlı bosslar için)
- **Can gösterimi**: `Can/Maksimum Can` formatında
- **Progress bar**: Can yüzdesine göre görsel gösterim
- **Renk değişimi**: Can durumuna göre otomatik
