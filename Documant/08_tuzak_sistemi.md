# STRATOCRAFT - TUZAK SİSTEMİ

## 🪤 Tuzak Sistemi Nedir?

Tuzaklar, düşmanları yakalamak için kurduğun fiziksel düzeneklerdir. **Lodestone çekirdeği** + **Magma Block çerçevesi** + **Yakıt** = Tuzak!

---

## 📋 İÇİNDEKİLER

1. [Tuzak Kurulumu](#tuzak-kurulumu)
2. [Tuzak Türleri](#tuzak-türleri)
3. [Aktifleştirme](#aktifleştirme)
4. [Yakıt Sistemi](#yakit-sistemi)

---

## 🛠️ TUZAK KURULUMU

### Adım 1: Tuzak Çekirdeği Craft

**Tarif**:
```
[O][E][O]
[I][D][I]    O = Obsidian
[O][E][O]    E = Ender Pearl
             I = Iron Ingot
             D = Diamond

= Tuzak Çekirdeği (Lodestone görünümü)
```

---

### Adım 2: Çekirdeği Yerleştir

```
1. İstediğin yere Tuzak Çekirdeği koy
2. Sonuç: LODESTONE bloğu yerleşir
3. Bu aktif değil, sadece çekirdek
```

---

### Adım 3: Magma Block Çerçevesi Yap

**3x3 Alan (Çekirdeği Çevreleyecek)**:
```
Üstten bakış:

[M][M][M]
[M][L][M]    M = Magma Block
[M][M][M]    L = Lodestone (Tuzak Çekirdeği)

Minimum: 6 Magma Block (etrafı çevrelesin)
Maksimum: 8 Magma Block (3x3 tam çerçeve)
```

**DİKKAT**:
- En az 6 Magma Block olmalı
- Lodestone'un etrafında 3x3 alanda
- Eksikse aktifleşmez

---

### Adım 4: Yakıt Ekle

**Yakıt Tipleri**:
```
┌───────────────┬─────────────┬──────────┐
│ Yakıt         │ Süre        │ Kullanım │
├───────────────┼─────────────┼──────────┤
│ Coal          │ 10 dakika   │ Basit    │
│ Lava Bucket   │ 30 dakika   │ Orta     │
│ Blaze Rod     │ 1 saat      │ İleri    │
│ Karanlık Madde│ 6 saat      │ Efsanevi │
└───────────────┴─────────────┴──────────┘
```

---

### Adım 5: Aktifleştir

```
1. Eline yakıt al (örn: Coal)
2. Lodestone (çekirdek) üzerine git
3. SHIFT + SAĞ TIK
4. Sonuç:
   ✓ Bloklar parlar
   ✓ Partikül efekti
   ✓ "Tuzak aktif!" mesajı
   ✓ Düşman yaklaşınca tetiklenir
```

**Görsel**:
- FLAME partikülleri (Magma çerçevede)
- SMOKE partikülleri (çekirdekte)
- Kırmızı ışık

---

## 🎯 TUZAK TÜRLERİ

### Tuzak Tipi Nasıl Belirlenir?

**Yakıt = Tuzak Tipi**

```
┌──────────────────┬─────────────────┐
│ Yakıt            │ Tuzak Tipi      │
├──────────────────┼─────────────────┤
│ Lava Bucket      │ Fire Trap       │
│ Lightning  Core   │ Shock Trap      │
│ Poison Potion    │ Poison Trap     │
│ Ice/Packed Ice   │ Freeze Trap     │
│ TNT              │ Explosive Trap  │
│ Diğer (Coal vb.) │ Basic Trap      │
└──────────────────┴─────────────────┘
```

---

### 1. 🔥 Fire Trap (Ateş Tuzağı)

**Yakıt**: Lava Bucket

**Etki**:
- Düşman yaklaşınca (2 blok) ateş püskürür
- 5 kalp hasar
- 10 saniye yangın (burn)
- Cooldown: 5 saniye

**Kullanım**: Dar geçitlerde

---

### 2. ⚡ Shock Trap (Şok Tuzağı)

**Yakıt**: Lightning Core

**Etki**:
- Düşman yaklaşınca şimşek düşür
- 8 kalp hasar
- 5 saniye sersemletme (slowness + nausea)
- Cooldown: 10 saniye

**Kullanım**: Tek güçlü vuruş

---

### 3. 🧪 Poison Trap (Zehir Tuzağı)

**Yakıt**: Splash Potion of Poison

**Etki**:
- Düşman yaklaşınca zehir bulutu
- Poison II (15 saniye)
- 3 blok alan etkisi
- Cooldown: 8 saniye

**Kullanım**: Alan savunması

---

### 4. ❄️ Freeze Trap (Donma Tuzağı)

**Yakıt**: Packed Ice veya Ice Block

**Etki**:
- Düşman yaklaşınca donurur
- Slowness III (10 saniye)
- Ayağın altında buz oluşur
- Cooldown: 5 saniye

**Kullanım**: Yavaşlatma, kaçış engelleme

---

### 5. 💥 Explosive Trap (Patlayıcı Tuzak)

**Yakıt**: TNT

**Etki**:
- Düşman yaklaşınca patlama
- 10 kalp hasar (merkez)
- 3 blok alan hasarı
- Blokları kırar (dikkatli!)
- Cooldown: 15 saniye

**Kullanım**: Ağır savunma, son çare

---

### 6. ⚙️ Basic Trap (Temel Tuzak)

**Yakıt**: Coal, Charcoal

**Etki**:
- Düşman yaklaşınca hafif hasar
- 2 kalp hasar
- Küçük itme (knockback)
- Cooldown: 3 saniye

**Kullanım**: Ucuz savunma, uyarı

---

## 🔋 YAKIT SİSTEMİ

### Yakıt Eklenme

**Nasıl Eklenir?**:
```
1. Aktif tuzağınız var
2. Eline yakıt al
3. Lodestone'a SHIFT + SAĞ TIK
4. Yakıt eklenir
5. Süre uzar
```

**Yakıt Hesaplama**:
```
Her yakıt = X dakika

Coal: 10 dk
Lava: 30 dk
Blaze Rod: 60 dk
Karanlık Madde: 360 dk (6 saat)

Çoklu ekleme:
5 Coal = 50 dakika
2 Lava = 60 dakika
```

---

### Yakıt Bitince

```
Durum: Yakıt 0'a düştü

Sonuç:
- Tuzak deaktif olur
- Partikül durur
- Düşman tetiklemez

Çözüm:
→ Yeniden yakıt ekle
→ Veya tuzağı kaldır
```

---

## 🎨 TUZAK STRATEJİSİ

### Savunma Hattı

**Katmanlı Savunma**:
```
Dış Hat:
[F][F][F]    F = Freeze Trap (yavaşlatma)

Orta Hat:
  [P][P]     P = Poison Trap (hasar)

İç Hat:
    [E]      E = Explosive Trap (son savunma)

Kristal:
    [K]      K = Klan Kristali
```

---

### Dar Geçit Tuzağı

**Koridor Kontrolü**:
```
Koridor (3 blok genişlik):

[Duvar][S][Duvar]    S = Shock Trap (ortada)
[Duvar][F][Duvar]    F = Fire Trap
[Duvar][S][Duvar]    S = Shock Trap

Düşman koridordan geçemez!
```

---

### Giriş Savunması

**Ana Kapı Koruması**:
```
     [Kapı]
       ↓
    [P][P]          P = Poison Trap (hemen arkasında)
  [F]   [F]         F = Freeze Trap (yanlarda)
[E]       [E]       E = Explosive Trap (dışta)
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Tuzak Kuralları

1. **Çerçeve Zorunlu**: Minimum 6 Magma Block
2. **Yakıt = Tip**: Yakıt tuzak tipini belirler
3. **Cooldown**: Her tuzak tek seferde 1 düşman vurar
4. **Friendly Fire**: Klan üyelerine zarar vermez
5. **Patlayıcı Dikkat**: TNT tuzağı blok kırar!

---

### Yakıt Ekonomisi

**Maliyet Analizi**:
```
Ucuz (Coal):
→ 10 dk sürer
→ Sık değiştir
→ Antrenman/test için

Orta (Lava):
→ 30 dk sürer
→ Dengeli
→ Normal savunma

Pahalı (Baze Rod):
→ 1 saat sürer
→ Uzun süreli
→ Ana savunma hatları

Efsanevi (Karanlık Madde):
→ 6 saat sürer
→ Çok nadir
→ Kritik noktalar (kristal yakını)
```

---


---

## 💣 MAYIN SİSTEMİ

Mayınlar, görünmez ve ölümcül tuzaklardır. Basınç plakası ile kurulur, üzerine basıldığında yok olur.

### Mayın Kurulumu

**Gereksinimler**:
- Basınç Plakası (Herhangi bir tür)
- Mayın Tipi Belirleyici Eşya (Elde tutulmalı)

**Adımlar**:
```
1. Yere bir Basınç Plakası koy
2. Eline mayın tipine uygun eşyayı al
3. Basınç plakasına SHIFT + SAĞ TIKLA
4. SONUÇ:
   - "Mayın yerleştirildi" mesajı
   - Plaka mayına dönüşür
```

### Mayın Türleri

| Mayın Tipi | Gerekli Eşya | Etki |
|------------|--------------|------|
| **Patlama** | TNT | 3 blok çapında patlama (Alan hasarı) |
| **Yıldırım** | Lightning Core | Hedefe yıldırım çarpar (5 kalp) |
| **Zehir** | Spider Eye | Zehir etkisi (100 tick) |
| **Körlük** | Ink Sac | Körlük etkisi (100 tick) |
| **Yorgunluk** | Iron Pickaxe | Kazı yorgunluğu (200 tick) |
| **Yavaşlık** | Slime Ball | Yavaşlatma (100 tick) |

**Not**: Kendi klan üyelerin mayınlarına basmaz!

---

## 🎯 HIZLI TUZAK REHBERİ


### Basit Tuzak (Yeni Başlayanlar)

```
1. Tuzak Çekirdeği craft
2. Yere koy (Lodestone olur)
3. Etrafına 6-8 Magma Block diz
4. Eline Coal al
5. Shift + Sağ tık (Lodestone'a)
6. Aktif! (Temel tuzak)
```

### İleri Tuzak (Pro)

```
1. Çekirdeği koy
2. 8 Magma Block tam çerçeve
3. Eline Lightning Core al
4. Shift + Sağ tık
5. Sonuç: Şok Tuzağı (8 kalp hasar!)
```

---

**🎮 Tuzaklarla bölgeni koru, düşmanları yakala!**

---

## 💣 YENİ MAYIN SİSTEMİ (25 Benzersiz Mayın)

### 📋 Genel Bilgiler

Mayınlar **basınç plakaları** olarak verilir ve yere koyulduğunda otomatik olarak aktif mayın haline gelir. Her mayın benzersiz bir özelliğe sahiptir ve seviyeye göre farklı basınç plakası tipleri kullanır.

**ÖNEMLİ**: Bu sistem **eski mayın sisteminden tamamen farklıdır**! Eski sistem (Tuzak Çekirdeği + Magma Block) hala çalışıyor, ama yeni sistem daha basit ve kullanışlıdır.

**Sistem Yöneticisi**: `NewMineManager.java` - Tüm 25 mayın tipini yönetir.

---

### 🎯 Kullanım

1. **Mayın Al**: `/stratocraft mine give <seviye> <isim>`
2. **Yere Koy**: Basınç plakasını yere koy (sağ tık)
3. **Aktif Olur**: Otomatik olarak mayın haline gelir
4. **Gizle** (Opsiyonel): Shift + Gizleme Aleti ile görünmez yap

---

### 📦 Basınç Plakası Tipleri (Seviyeye Göre)

- **Seviye 1**: `STONE_PRESSURE_PLATE` (Taş Basınç Plakası)
- **Seviye 2**: `OAK_PRESSURE_PLATE` (Meşe Basınç Plakası)
- **Seviye 3**: `POLISHED_BLACKSTONE_PRESSURE_PLATE` (Cilalı Siyah Taş Basınç Plakası) + ✨ Parlama Efekti
- **Seviye 4**: `HEAVY_WEIGHTED_PRESSURE_PLATE` (Ağır Ağırlık Basınç Plakası) + ✨ Parlama Efekti
- **Seviye 5**: `LIGHT_WEIGHTED_PRESSURE_PLATE` (Hafif Ağırlık Basınç Plakası) + ✨ Parlama Efekti

**Alt Blok Tipleri (Seviyeye Göre)**:
- **Seviye 1**: Cobblestone
- **Seviye 2**: Stone
- **Seviye 3**: Iron Block
- **Seviye 4**: Diamond Block
- **Seviye 5**: Netherite Block

---

### 💣 Mayın Listesi (25 Benzersiz Mayın)

#### Seviye 1 (5 Mayın)
- **EXPLOSIVE** - Patlama Mayını: Basınca küçük patlama yapar
- **POISON** - Zehir Mayını: Basınca zehir efekti verir
- **SLOWNESS** - Yavaşlık Mayını: Basınca yavaşlatır
- **LIGHTNING** - Yıldırım Mayını: Basınca yıldırım çarpar
- **FIRE** - Yakma Mayını: Basınca yakma efekti verir

#### Seviye 2 (5 Mayın)
- **CAGE** - Kafes Hapsetme Mayını: Basınca obsidyen kafes oluşturur
- **LAUNCH** - Fırlatma Mayını: Basınca yukarı fırlatır
- **MOB_SPAWN** - Canavar Spawn Mayını: Basınca canavarlar spawnlar
- **BLINDNESS** - Körlük Mayını: Basınca körlük efekti verir
- **WEAKNESS** - Zayıflık Mayını: Basınca zayıflık efekti verir

#### Seviye 3 (5 Mayın)
- **FREEZE** - Dondurma Mayını: Basınca dondurma efekti verir
- **CONFUSION** - Karışıklık Mayını: Basınca karışıklık efekti verir
- **FATIGUE** - Yorgunluk Mayını: Basınca yorgunluk efekti verir
- **POISON_CLOUD** - Zehir Bulutu Mayını: Basınca alan zehiri oluşturur
- **LIGHTNING_STORM** - Yıldırım Fırtınası Mayını: Basınca çoklu yıldırım çarpar

#### Seviye 4 (5 Mayın)
- **MEGA_EXPLOSIVE** - Büyük Patlama Mayını: Basınca büyük patlama yapar
- **LARGE_CAGE** - Büyük Kafes Mayını: Basınca büyük kafes oluşturur
- **SUPER_LAUNCH** - Güçlü Fırlatma Mayını: Basınca çok yukarı fırlatır
- **ELITE_MOB_SPAWN** - Güçlü Canavar Spawn Mayını: Basınca güçlü canavarlar spawnlar
- **MULTI_EFFECT** - Çoklu Efekt Mayını: Basınca birden fazla efekt verir

#### Seviye 5 (5 Mayın)
- **NUCLEAR_EXPLOSIVE** - Nükleer Patlama Mayını: Basınca nükleer patlama yapar
- **DEATH_CLOUD** - Ölüm Bulutu Mayını: Basınca ölüm bulutu oluşturur
- **THUNDERSTORM** - Gök Gürültüsü Mayını: Basınca gök gürültüsü fırtınası yapar
- **BOSS_SPAWN** - Boss Spawn Mayını: Basınca boss spawnlar
- **CHAOS** - Kaos Mayını: Basınca kaos efekti verir

---

### 🔧 Admin Komutları

**Mayın Verme**:
```
/stratocraft mine give <seviye> <isim>
```

**Örnekler**:
```
/stratocraft mine give 1 explosive         → Seviye 1 Patlama Mayını
/stratocraft mine give 3 freeze             → Seviye 3 Dondurma Mayını
/stratocraft mine give 5 nuclear_explosive  → Seviye 5 Nükleer Patlama Mayını
/stratocraft mine give concealer            → Gizleme Aleti
```

**Mayın Listesi**:
```
/stratocraft mine list
```

---

### 👻 Gizleme Sistemi

**Gizleme Aleti**: `MINE_CONCEALER`
- **Kullanım**: Shift + Sağ Tık mayına
- **Efekt**: Mayın görünmez olur (sadece sahibi ve klan üyeleri görebilir)
- **Tekrar Kullanım**: Aynı işlemle görünür yapılabilir
- **Görünürlük Kuralları**:
  - **Sahibi**: Mayın ismini her zaman görebilir
  - **Klan Üyeleri**: Mayın ismini görebilir
  - **Düşmanlar**: Gizli mayınları göremez (basınç plakasını da göremez)

**Teknik Detay**:
- ArmorStand ile isim görünürlüğü kontrol ediliyor
- `Player#showEntity` ve `Player#hideEntity` kullanılıyor
- Her 20 tick'te (1 saniye) görünürlük güncelleniyor
- Yakındaki oyuncular kontrol ediliyor (10 blok yarıçap)

---

### 👻 Hayalet Tarif Desteği

**Yeni Özellik**: Mayınlar için hayalet tarif gösterimi!

**Nasıl Çalışır?**:
1. Tarif kitabına bak (mayın tarifi içeren)
2. Hayalet bloklar görünür (ArmorStand ile)
3. Basınç plakası + alt blok deseni gösterilir
4. Doğru blokları koy, hayalet bloklar kaybolur

**Desteklenen Mayınlar**:
- Tüm 25 mayın tipi için hayalet tarifler
- `GhostRecipeManager.initializeMineRecipes()` ile otomatik yüklenir
- Seviyeye göre basınç plakası + alt blok deseni

**Tarif Formatı**:
- **Merkez**: Basınç plakası (seviyeye göre)
- **Alt Blok**: Seviyeye göre blok (Cobblestone, Stone, Iron Block, vb.)

**Tarif ID Formatı**:
- `RECIPE_MINE_<MAYIN_ISMI>`
- Örnek: `RECIPE_MINE_EXPLOSIVE`, `RECIPE_MINE_NUCLEAR_EXPLOSIVE`

---

### ⚠️ Önemli Notlar

- **Dost/Düşman Ayrımı**: Mayınlar herkese zarar verir (klan üyeleri de dahil!)
- **Otomatik Aktivasyon**: Yere koyulduğunda otomatik aktif olur
- **Basınç Plakası Görünür**: Mayınlar gizli değil, açık basınç plakalarıdır (gizleme aleti ile gizlenebilir)
- **Gizleme Opsiyonel**: Gizleme aleti ile görünmez yapılabilir
- **Eski Sistem Hala Çalışıyor**: Tuzak Çekirdeği + Magma Block sistemi hala aktif
- **Spawn Koruması**: Spawn yakınında (100 blok) mayın yerleştirilemez
- **Klan Alanı**: Klan alanlarında sadece klan üyeleri mayın yerleştirebilir
- **Sınırlamalar**:
  - Oyuncu başına maksimum 100 mayın
  - Klan başına maksimum 500 mayın
  - Dünya başına maksimum 10,000 mayın

---

## 💣 YENİ MAYIN SİSTEMİ - DETAYLI AÇIKLAMA

### 📚 Sistem Özellikleri

**Temel Mekanik**:
- Mayınlar **basınç plakası** olarak verilir
- Yere yerleştirildiğinde otomatik aktif olur
- Üstüne basıldığında efekt tetiklenir
- Sadece sahibi ve klan üyeleri mayın ismini görebilir

**Seviye Sistemi**:
- **5 seviye** var (Seviye 1-5)
- Her seviyede **5 farklı mayın** (Toplam 25 mayın)
- Seviye arttıkça güç ve yapım zorluğu artar

**Basınç Plakası Türleri**:
- **Seviye 1**: Stone Pressure Plate (Taş)
- **Seviye 2**: Oak Pressure Plate (Meşe)
- **Seviye 3**: Birch Pressure Plate (Huş)
- **Seviye 4**: Dark Oak Pressure Plate (Koyu Meşe)
- **Seviye 5**: Warped Pressure Plate (Warped)

---

### 🔥 SEVİYE 1 MAYINLAR (10 Mayın - Taş Basınç Plakası)

#### 1. EXPLOSIVE (Patlama)
- **Efekt**: 3.0 güç patlama
- **Kullanım**: Basit hasar mayını
- **Komut**: `/stratocraft mine give 1 explosive`

#### 2. POISON (Zehir)
- **Efekt**: Poison II (10 saniye)
- **Kullanım**: Zehirli alan
- **Komut**: `/stratocraft mine give 1 poison`

#### 3. SLOWNESS (Yavaşlatma)
- **Efekt**: Slowness II (10 saniye)
- **Kullanım**: Düşmanı yavaşlatma
- **Komut**: `/stratocraft mine give 1 slowness`

#### 4. FIRE (Ateş)
- **Efekt**: 100 tick (5 saniye) ateş
- **Kullanım**: Yakma mayını
- **Komut**: `/stratocraft mine give 1 fire`

#### 5. LIGHTNING (Yıldırım)
- **Efekt**: Tek yıldırım çarpma
- **Kullanım**: Elektrik şoku
- **Komut**: `/stratocraft mine give 1 lightning`

---

### 🔥 SEVİYE 2 MAYINLAR (5 Mayın - Meşe Basınç Plakası)

#### 6. CAGE (Kafes)
- **Efekt**: 3x3x3 obsidyen kafes (10 saniye)
- **Kullanım**: Düşmanı hapsetme
- **Komut**: `/stratocraft mine give 2 cage`

#### 7. LAUNCH (Fırlatma)
- **Efekt**: Yukarı fırlatma (velocity 1.5)
- **Kullanım**: Düşüş hasarı vermek için
- **Komut**: `/stratocraft mine give 2 launch`

#### 8. MOB_SPAWN (Canavar Spawn)
- **Efekt**: 2 zombie spawn
- **Kullanım**: Düşmana ekstra zorluk
- **Komut**: `/stratocraft mine give 2 mob_spawn`

#### 9. BLINDNESS (Körlük)
- **Efekt**: Blindness (10 saniye)
- **Kullanım**: Düşmanı kör etme
- **Komut**: `/stratocraft mine give 2 blindness`

#### 10. WEAKNESS (Zayıflık)
- **Efekt**: Weakness II (10 saniye)
- **Kullanım**: Düşmanı zayıflatma
- **Komut**: `/stratocraft mine give 2 weakness`

---

### 🔥 SEVİYE 3 MAYINLAR (5 Mayın - Huş Basınç Plakası)

#### 11. FREEZE (Dondurma)
- **Efekt**: Slowness III + Slow Digging (10 saniye)
- **Kullanım**: Tam dondurma efekti
- **Komut**: `/stratocraft mine give 3 freeze`

#### 12. CONFUSION (Karışıklık)
- **Efekt**: Nausea (10 saniye)
- **Kullanım**: Ekranı döndürme
- **Komut**: `/stratocraft mine give 3 confusion`

#### 13. FATIGUE (Yorgunluk)
- **Efekt**: Mining Fatigue III (15 saniye)
- **Kullanım**: Blok kırmayı engelleme
- **Komut**: `/stratocraft mine give 3 fatigue`

#### 14. POISON_CLOUD (Zehir Bulutu)
- **Efekt**: 5 blok yarıçapta tüm oyunculara Poison (8 saniye)
- **Kullanım**: Alan zehirleme
- **Komut**: `/stratocraft mine give 3 poison_cloud`

#### 15. LIGHTNING_STORM (Yıldırım Fırtınası)
- **Efekt**: 3 yıldırım (0.5 saniye aralarla) + 5.0 hasar
- **Kullanım**: Elektrik fırtınası
- **Komut**: `/stratocraft mine give 3 lightning_storm`

---

### 🔥 SEVİYE 4 MAYINLAR (5 Mayın - Koyu Meşe Basınç Plakası)

#### 16. MEGA_EXPLOSIVE (Büyük Patlama)
- **Efekt**: 6.0 güç patlama
- **Kullanım**: Büyük hasar alanı
- **Komut**: `/stratocraft mine give 4 mega_explosive`

#### 17. LARGE_CAGE (Büyük Kafes)
- **Efekt**: 5x5x5 obsidyen kafes (20 saniye)
- **Kullanım**: Büyük gruplara karşı
- **Komut**: `/stratocraft mine give 4 large_cage`

#### 18. SUPER_LAUNCH (Güçlü Fırlatma)
- **Efekt**: Çok yukarı fırlatma (velocity 2.5)
- **Kullanım**: Yüksek düşüş hasarı
- **Komut**: `/stratocraft mine give 4 super_launch`

#### 19. ELITE_MOB_SPAWN (Güçlü Canavar Spawn)
- **Efekt**: 5 zombie + 2 skeleton spawn
- **Kullanım**: Güçlü düşman ordusu
- **Komut**: `/stratocraft mine give 4 elite_mob_spawn`

#### 20. MULTI_EFFECT (Çoklu Efekt)
- **Efekt**: Poison + Slowness + Blindness + Weakness (10 saniye)
- **Kullanım**: Tüm efektleri bir arada
- **Komut**: `/stratocraft mine give 4 multi_effect`

---

### 🔥 SEVİYE 5 MAYINLAR (5 Mayın - Warped Basınç Plakası)

#### 21. NUCLEAR_EXPLOSIVE (Nükleer Patlama)
- **Efekt**: 10.0 güç patlama
- **Kullanım**: Çok büyük hasar alanı
- **Komut**: `/stratocraft mine give 5 nuclear_explosive`

#### 22. DEATH_CLOUD (Ölüm Bulutu)
- **Efekt**: Poison IV (20 saniye) + sürekli 0.5 hasar (100 tick = 50 hasar) + büyük duman
- **Kullanım**: Ölümcül alan
- **Komut**: `/stratocraft mine give 5 death_cloud`

#### 23. THUNDERSTORM (Gök Gürültüsü)
- **Efekt**: 10 yıldırım (5 tick aralarla) + 10.0 hasar
- **Kullanım**: Yıldırım fırtınası
- **Komut**: `/stratocraft mine give 5 thunderstorm`

#### 24. BOSS_SPAWN (Boss Spawn)
- **Efekt**: Wither Skeleton veya Enderman spawn
- **Kullanım**: Güçlü boss düşman
- **Komut**: `/stratocraft mine give 5 boss_spawn`

#### 25. CHAOS (Kaos)
- **Efekt**: Patlama + Poison II + Slowness II + Blindness + Weakness II + Ateş + Yıldırım (10 saniye)
- **Kullanım**: Tüm efektlerin birleşimi
- **Komut**: `/stratocraft mine give 5 chaos`

---

### 🎨 Gizleme Sistemi Detayı

**Mayın Gizleme Aleti**:
- **Item**: Special stick (Sihirli değnek görünümü)
- **Kullanım**: 
  1. Mayını yerleştir
  2. Gizleme aletini al
  3. Mayına shift + sağ tık yap
  4. Mayın görünmez olur

**Görünürlük Kuralları**:
- **Sahibi**: Mayın ismini her zaman görebilir
- **Klan Üyeleri**: Mayın ismini görebilir
- **Düşmanlar**: Mayın ismini göremez (gizliyse basınç plakasını da göremez)

**Teknik Detay**:
- ArmorStand ile isim görünürlüğü kontrol ediliyor
- `Player#showEntity` ve `Player#hideEntity` kullanılıyor
- Her 20 tick'te (1 saniye) görünürlük güncelleniyor
- Yakındaki oyuncular kontrol ediliyor

**Komut**:
```
/stratocraft mine give concealer
```

---

### 🛡️ Güvenlik ve Performans

**Güvenlik**:
- Spawn yakınında (100 blok) mayın yerleştirilemez
- Klan alanlarında sadece klan üyeleri mayın yerleştirebilir
- Mayınlar otomatik olarak sahibine kayıtlı

**Performans**:
- Her mayın için ayrı ArmorStand oluşturuluyor
- Görünürlük güncellemesi her 20 tick'te bir
- Sadece 10 blok yakınındaki oyuncular kontrol ediliyor
- Mayın kaldırıldığında ArmorStand da otomatik siliniyor

**Sınırlamalar**:
- Oyuncu başına maksimum 100 mayın
- Klan başına maksimum 500 mayın
- Dünya başına maksimum 10,000 mayın

---

---

## 🔧 TEKNİK DETAYLAR

### NewMineManager Sistemi

**Dosya**: `NewMineManager.java`

**Özellikler**:
- 25 benzersiz mayın tipi yönetimi
- Basınç plakası tabanlı sistem
- Gizleme sistemi (ArmorStand ile görünürlük kontrolü)
- Klan entegrasyonu
- Performans optimizasyonları

**Veri Yapıları**:
- `activeMines`: Aktif mayınlar (Location -> MineData)
- `mineNameStands`: Mayın isim standları (Location -> ArmorStand)
- `hiddenMines`: Gizli mayınlar (Location -> boolean)

**MineType Enum**:
- 25 farklı mayın tipi
- Her mayın için seviye ve görünen isim
- Seviye 1-5 arası dağılım

### Hayalet Tarif Entegrasyonu

**GhostRecipeManager Entegrasyonu**:
- `initializeMineRecipes()` metodu ile otomatik yükleme
- Her mayın için basit hayalet tarif (basınç plakası + alt blok)
- Seviyeye göre farklı basınç plakası ve alt blok tipleri

**Tarif Oluşturma**:
```java
// Seviyeye göre basınç plakası
Material pressurePlate = getPressurePlateForLevel(level);
// Seviyeye göre alt blok
Material baseBlock = getBaseBlockForLevel(level);
```

### Performans Optimizasyonları

**Görünürlük Güncellemesi**:
- Her 20 tick'te bir (1 saniye) güncellenir
- Sadece 10 blok yakınındaki oyuncular kontrol edilir
- `Player#showEntity` ve `Player#hideEntity` kullanılır

**Mayın Tetikleme**:
- Event-based sistem (BlockPressurePlateEvent)
- Sadece aktif mayınlar kontrol edilir
- Chunk yüklü kontrolü yapılır

---

## 📊 MAYIN KARŞILAŞTIRMA TABLOSU

| Seviye | Mayın Sayısı | Basınç Plakası | Alt Blok | Özellik |
|--------|--------------|----------------|----------|---------|
| **1** | 5 | Taş | Cobblestone | Temel efektler |
| **2** | 5 | Meşe | Stone | Orta seviye efektler |
| **3** | 5 | Cilalı Siyah Taş | Demir Bloğu | Güçlü efektler + Parlama |
| **4** | 5 | Ağır Ağırlık | Elmas Bloğu | Çok güçlü efektler + Parlama |
| **5** | 5 | Hafif Ağırlık | Netherite Bloğu | Efsanevi efektler + Parlama |

---

**🎮 25 benzersiz mayınla stratejik savunma yap!**