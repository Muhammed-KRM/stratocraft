# STRATOCRAFT - RİTÜEL SİSTEMİ

## 🔥 Ritüel Sistemi Nedir?

Ritüeller, Stratocraft'ta **fiziksel blok düzenekleriyle** yapılan özel etkileşimlerdir. **Hiçbir komut kullanılmaz**, her şey bloklarla yapılır!

**YENİ Özellikler** ⭐:
- ✅ **Güvenlik İyileştirmeleri**: Tüm ritüellerde null check'ler eklendi
- ✅ **Config Entegrasyonu**: Cooldown süreleri config'den alınıyor
- ✅ **Klan Üyeliği Kontrolleri**: Terfi ritüelinde klan üyeliği kontrolü eklendi
- ✅ **Hata Yönetimi**: Kritik bölgelerde try-catch blokları eklendi

---

## 📋 İÇİNDEKİLER

1. [Klan Ritüelleri](#klan-ritüelleri)
2. [Üye Yönetim Ritüelleri](#üye-yönetim-ritüelleri)
3. [Yapi Ritüelleri](#yapi-ritüelleri)
4. [Savaş Ritüelleri](#savaş-ritüelleri)
5. [Boss Çağırma Ritüelleri](#boss-çağırma-ritüelleri)
6. [Canlı Eğitme Ritüelleri](#canlı-eğitme-ritüelleri)
7. [Çiftleşme Ritüelleri](#çiftleşme-ritüelleri)
8. [Ritüel Güç Sistemi](#ritüel-güç-sistemi) ⭐ YENİ

---

## 🏰 KLAN RİTÜELLERİ

**Not**: Klan kurma artık sadece **Klan Kristali** ile yapılır. Ritüel ile klan kurma kaldırılmıştır. Detaylar için `01_klan_sistemi.md` dosyasına bakın.

---

## 👥 ÜYE YÖNETİM RİTÜELLERİ

### 1. Ateş Ritüeli (Üye Alma)

**Gereksinimler**:
- 3x3 Stripped Log (soyulmuş odun kütüğü) platform
- 1 Flint and Steel (çakmak)
- Lider veya General yetkisi

**Platform Kurulumu**:
```
[L][L][L]
[L][L][L]    L = Stripped Log (herhangi bir soyulmuş kütük)
[L][L][L]

Tüm bloklar Stripped Log olmalı!
```

**Adımlar**:
```
1. Davet edilecek oyuncu platformun üzerine gelir
2. Lider/General eline Flint and Steel alır
3. SHIFT + SAĞ TIK (platforma)
4. SONUÇ:
   - Platform üzerindeki klansız oyuncular klan üyesi olur
   - FLAME partikülleri
   - "KLANA KATILDI" title
   - Globalk mesaj: "X kişi katıldı"
```

**Görsel Efektler**:
- 100 FLAME partikülü
- BEACON_ACTIVATE sesi
- Her üyede ENDER_DRAGON_FLAP sesi

**Cooldown**: 10 saniye (config'den ayarlanabilir)

**Güvenlik Kontrolleri** ⭐ YENİ:
- ✅ **Null Check**: Elindeki item null kontrolü yapılıyor (güvenlik)
- ✅ **Yetki Kontrolü**: Sadece Lider veya General yapabilir
- ✅ **Config Entegrasyonu**: Cooldown süresi config'den alınıyor

---

### 3. Terfi Ritüeli (Rütbe Verme)

**Gereksinimler**:
- 3x3 Stone Brick platform
- 4 Redstone Torch (köşelerde)
- Külçe (Altın = General, Demir = Member)
- Sadece Lider yapabilir

**Platform Kurulumu**:
```
[T]   [S]   [T]
    [S][S]
[F]  [S][F]  [F]    S = Stone Brick
    [S][S]          F = Fire/Campfire (ortada)
[T]   [S]   [T]    T = Redstone Torch (köşelerde)

Ortada ateş yak!
```

**Adımlar**:
```
1. Terfi edilecek kişi platformun üzerine gelir
2. Lider eline rütbeye göre külçe alır:
   - Altın Külçe → GENERAL
   - Demir Külçe → MEMBER
3. Lider o kişinin ÜZERİNE KÜLÇEYI ATAR (Q tuşu veya sürükle-bırak)
4. Külçe yere düştüğünde ritüel tetiklenir
5. SONUÇ:
   - Rütbe verilir
   - Altın/Gri partikülaltar
   - Şimşek efekti
   - "TERFİ ETTİ" title
```

**Partiküller**:
- General: VILLAGER_HAPPY (altın)
- Member: SMOKE_NORMAL (gri)

**Güvenlik Kontrolleri** ⭐ YENİ:
- ✅ **Klan Üyeliği Kontrolü**: Terfi edilecek oyuncu mutlaka klan üyesi olmalı
- ✅ **Null Check**: Elindeki item (Altın/Demir Külçe) null kontrolü yapılıyor
- ✅ **Rütbe Kontrolü**: Zaten üst rütbede olan oyunculara terfi verilemez
- ✅ **Cooldown Sistemi**: Ritüel spam önleme için cooldown var

**Admin Komutu** ⭐ YENİ:
```
/stratocraft clan promote <klan> <oyuncu> <RECRUIT|MEMBER|ELITE|GENERAL>
```

**Açıklama**: Ritüel simülasyonu yapar (ritüel yapısı gerekmez). Test için kullanılabilir.

**Özellikler**:
- ✅ Ritüel yapısı gerekmez
- ✅ Sadece yukarı doğru terfi (rütbe seviyesi kontrolü)
- ✅ Partikül efektleri (GENERAL için TOTEM, diğerleri için VILLAGER_HAPPY)
- ✅ Ses efektleri ve title mesajları

**Kullanım Örnekleri**:
```
/stratocraft clan promote TestKlan PlayerName MEMBER
/stratocraft clan promote TestKlan PlayerName GENERAL
/stratocraft clan terfi TestKlan PlayerName ELITE
```

---

## 🏗️ YAPI RİTÜELLERİ

### 4. Yapı Aktifleştirme Ritüeli

**Gereksinimler**:
- Yapı blueprint'i craft etmiş ol
- Belirtilen blok düzenini kur
- Yakıt (malzemeye göre değişir)

**Genel Adımlar**:
```
1. Tarif Kitabından blueprint öğren (gerekiyorsa)
2. Yapı bloklarını düzenle:
   - Temel bloklar (Stone/Iron/Titanyum/vb.)
   - Merkez bloğu (genelde özel bir blok)
   - Enerji kaynağı (Beacon, Glowstone vb.)
3. Eline yakıt al:
   - Basit yapılar: Coal
   - İleri yapılar: Diamond
   - Efsanevi yapılar: Karanlık Madde
4. Merkez bloğa SHIFT + SAĞ TIK
5. SONUÇ:
   - Yapı aktif hale gelir
   - Klan yapı listesine eklenir
   - Pasif etkisi başlar
```

**Örnek - Zehir Kulesi**:
```
Platform: 5x5 Emerald Block
Merkez: 1 Cauldron (kazana zehir iksiri dolu)
Yakıt: 10 Rotten Flesh

Ritüel:
1. Eline Rotten Flesh al
2. Cauldron'a SHIFT + SAĞ TIK
3. Aktif olur → Düşmanlara otomatik Poison verir
```

---

## ⚔️ SAVAŞ RİTÜELLERİ

### 5. Kuşatma İlanı Ritüeli

**Gereksinimler**:
- Beacon (kuşatma anıtı)
- 64 Obsidian
- 32 TNT
- General veya Lider yetkisi

**Adımlar**:
```
1. Düşman klan sınırının 50 blok yakınına Beacon koy
2. Beaconun altına 3x3 Obsidian piramit yap:

   Seviye 1:  3x3 Obsidian
   Üstüne: Beacon

3. Eline TNT al
4. Beacon'a SHIFT + SAĞ TIK
5. SONUÇ:
   - Kuşatma başlar
   - 5 dakika hazırlık süresi
   - İki klan bildiri alır
   - Sayaç bittikten sonra düşman yapıları hasarlı
```

**Mesajlar**:
```
Saldıran: "Kuşatma başlattınız! 5 dakika hazırlık..."
Savunan: "UYARI! Klan kuşatma altında! 5 dakika kaldı!"
```

---

### 6. Kristal Yıkım Ritüeli

**ÖNMLİ**: Artık **özel ritüel gerekmez**!

**Basit Yöntem**:
```
1. Kuşatma başlat
2. Çitleri kır (bölgeye gir)
3. Savunma yapılarını aş
4. Kristale ulaş
5. NORMAL SİLAHLA vur
6. Kristal kırılır → Klan dağılır
```

---


---

## 🤝 DİPLOMASİ RİTÜELLERİ

### 7. Kan Anlaşması (Müttefiklik)

**Gereksinimler**:
- İki klan lideri
- Her liderin elinde 1 Elmas
- Liderler birbirine yakın olmalı (3 blok)

**Adımlar**:
```
1. İki lider karşı karşıya gelir
2. İkisi de SHIFT'e basılı tutar
3. Ellerine Elmas alırlar
4. Birbirlerine SAĞ TIKLARLAR
5. SONUÇ:
   - İki klan müttefik olur
   - Kalp ve End Rod partikülleri
   - "MÜTTEFİK OLUNDU" title
   - Elmaslar tüketilir
```

**Cooldown**: 10 saniye

---

## 👑 YÖNETİM RİTÜELLERİ

### 8. Taç Geçişi (Liderlik Devri)

**Gereksinimler**:
- Lider ve devredilecek üye
- Liderin elinde Altın Kask (Golden Helmet)
- Klan Kristali yakınında (10 blok)

**Adımlar**:
```
1. Lider ve üye Klan Kristali yanına gider
2. Lider eline Altın Kask alır
3. SHIFT'e basılı tutarak üyeye SAĞ TIKLAR
4. SONUÇ:
   - Liderlik devredilir
   - Eski lider General olur
   - Şimşek ve Totem efektleri
   - Altın Kask tüketilir
```

### 9. Yeniden Adlandırma (İsim Değiştirme)

**Gereksinimler**:
- Lider
- İsimlendirilmiş Kağıt (Yeni isim)
- Klan Kristali yakınında (5 blok)

**Adımlar**:
```
1. Örs'te kağıda yeni klan ismini yaz
2. Klan Kristali yanına git
3. SHIFT'e basılı tutarak havaya/kristale SAĞ TIKLA
4. SONUÇ:
   - Klan ismi değişir
   - "İSİM DEĞİŞTİ" title
   - Kağıt tüketilir
```

---

## 🔥 AYRILIK RİTÜELLERİ

### 10. Sürgün Ateşi (Klandan Atma)

**Gereksinimler**:
- Lider
- İsimlendirilmiş Kağıt (Atılacak oyuncunun ismi)
- Ruh Ateşi (Soul Fire)

**Adımlar**:
```
1. Örs'te kağıda atılacak oyuncunun ismini yaz
2. Ruh Ateşi (Soul Fire) bul veya yak
3. Kağıdı ateşin üzerine AT (Q ile)
4. SONUÇ:
   - Oyuncu klandan atılır
   - Ghast çığlığı sesi
   - Patlama efekti
   - Oyuncuya "SÜRGÜN EDİLDİN" mesajı gider
```

### 11. Yemin Bozma (Klandan Ayrılma)

**Gereksinimler**:
- Herhangi bir üye (Lider hariç)
- İsimlendirilmiş Kağıt (Kendi ismi veya Klan ismi)
- Normal Ateş veya Ruh Ateşi

**Adımlar**:
```
1. Örs'te kağıda kendi ismini veya klan ismini yaz
2. Bir ateş kaynağı bul
3. SHIFT'e basılı tutarak ateşe SAĞ TIKLA
4. SONUÇ:
   - Klandan ayrılırsın
   - "YEMİN KIRILDI" title
   - Kağıt yanar
```

---

## 📖 HIZLI RİTÜEL REHBERİ (GÜNCEL)

```
Klan Kur:
→ 3x3 Cobblestone + Crafting Table
→ Named Paper (Masa üstünde sağ tık)

Üye Al:
→ 3x3 Stripped Log
→ Shift + Çakmak (Oyuncu üstündeyken)

Terfi Ver:
→ 3x3 Stone Brick + 4 Redstone Torch + Ateş
→ Külçe at (Altın=General, Demir=Üye)

Müttefik Ol:
→ İki Lider + Shift + Elmas + Sağ Tık

Liderlik Devret:
→ Kristal Yanı + Altın Kask + Shift + Sağ Tık

Klan İsmi Değiştir:
→ Kristal Yanı + Named Paper + Shift + Sağ Tık

Klandan At (Sürgün):
→ Soul Fire + Named Paper (İsimli kağıdı ateşe at)

Klandan Ayrıl:
→ Ateş + Named Paper + Shift + Sağ Tık
```

---

## 🐉 BOSS ÇAĞIRMA RİTÜELLERİ

**YENİ ÖZELLİK**: Artık tüm boss ritüellerinde **Çağırma Çekirdeği** kullanılıyor!

### Çağırma Çekirdeği Nedir?

**Çağırma Çekirdeği** (`SUMMON_CORE`), tüm boss ritüellerinin merkezinde bulunan özel bir bloktur. Bu çekirdek:
- Admin komutu ile verilebilir: `/scadmin give tool summon_core`
- Yerleştirildiğinde `END_CRYSTAL` bloğu olarak görünür
- Ritüel deseni çekirdeğin **altına** (1 blok aşağıya) yapılır
- Aktivasyon itemi ile çekirdeğe sağ tıklanarak boss çağrılır
- Hangi boss çağrılacağı aktivasyon itemine göre belirlenir

### Genel Adımlar

**Tüm Boss Ritüelleri İçin:**
```
1. Çağırma Çekirdeği'ni yerleştir (admin komutu veya manuel)
2. Boss'un ritüel desenini çekirdeğin altına yerleştir
3. Tüm blokların doğru olduğundan emin ol
4. Eline aktifleştirme item'ını al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Boss spawn olur!
```

**Cooldown**: Her ritüel konumu için 1 dakika cooldown var.

**Admin Komutu ile Otomatik Yapı**: `/scadmin boss build <boss_tipi>`

**Boss Drop Sistemi**:
- Boss öldürüldüğünde **%100 şansla** kendi özel itemi düşer
- Boss seviyesine göre **%60-100 şansla** özel zırh/silah tarif kitapları düşer (1-3 adet)
- Boss seviyesine göre **%40-90 şansla** yapı tarif kitapları düşer

**Boss Özel İtemleri**:
- Seviye 1: Goblin Kralı Taçı, Ork Şefi Amuleti
- Seviye 2: Troll Kralı Kalbi
- Seviye 3: Ejderha Ölçeği, T-Rex Dişi, Cyclops Gözü
- Seviye 4: Titan Golem Çekirdeği, Phoenix Tüyü, Kraken Dokunaçı
- Seviye 5: Şeytan Lordu Boynuzu, Hiçlik Ejderi Kalbi

**Tarif Kitapları**:
- Her boss seviyesine göre ilgili zırh/silah tarif kitapları düşer
- Yapı tarif kitapları da boss seviyesine göre düşer

---

### Seviye 1 Bosslar

#### 1. Goblin Kralı (GOBLIN_KING)

**Ritüel Deseni:**
```
C C C
C E C  (C = Cobblestone, E = Çağırma Çekirdeği - END_CRYSTAL)
C C C
```

**Aktifleştirme Item:** Rotten Flesh

**Adımlar:**
```
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 3x3 Cobblestone platform oluştur (çekirdeğin altına)
3. Eline Rotten Flesh al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Goblin Kralı spawn olur!
```

**Admin Komutu**: `/scadmin boss build goblin_king`

---

#### 2. Ork Şefi (ORC_CHIEF)

**Ritüel Deseni:**
```
S S S
S E S  (S = Stone, E = Çağırma Çekirdeği - END_CRYSTAL)
S S S
```

**Aktifleştirme Item:** Iron Sword

**Adımlar:**
```
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 3x3 Stone platform oluştur (çekirdeğin altına)
3. Eline Iron Sword al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Ork Şefi spawn olur!
```

**Admin Komutu**: `/scadmin boss build orc_chief`

---

### Seviye 2 Bosslar

#### 3. Troll Kralı (TROLL_KING)

**Ritüel Deseni:**
```
B B B
B D B  (B = Stone Bricks, D = Diamond Block)
B B B
```

**Aktifleştirme Item:** Stone Axe

**Adımlar:**
```
1. 3x3 Stone Bricks platform oluştur
2. Merkeze Diamond Block koy
3. Eline Stone Axe al
4. Diamond Block'a SAĞ TIKLA
5. Troll Kralı spawn olur!
```

---

### Seviye 3 Bosslar

#### 4. Ejderha (DRAGON) - 2 Faz

**Ritüel Deseni:**
```
O O O O O
O · · · O
O · E · O  (O = Obsidian, E = Emerald Block, · = Boş)
O · · · O
O O O O O
```

**Aktifleştirme Item:** Dragon Egg

**Adımlar:**
```
1. 5x5 Obsidian platform oluştur
2. Merkeze Emerald Block koy
3. Eline Dragon Egg al
4. Emerald Block'a SAĞ TIKLA
5. Ejderha spawn olur!
```

---

#### 5. T-Rex (TREX)

**Ritüel Deseni:**
```
D S S S D
S · · · S
S · G · S  (D = Diamond Block, S = Stone, G = Gold Block)
S · · · S
D S S S D
```

**Aktifleştirme Item:** Bone

**Adımlar:**
```
1. 5x5 Stone platform oluştur
2. Köşelere Diamond Block, merkeze Gold Block koy
3. Eline Bone al
4. Gold Block'a SAĞ TIKLA
5. T-Rex spawn olur!
```

---

#### 6. Tek Gözlü Dev (CYCLOPS) - 2 Faz

**Ritüel Deseni:**
```
G B B B G
B · · · B
B · E · B  (G = Gold Block, B = Stone Bricks, E = Emerald Block, · = Boş)
B · · · B
G B B B G
```

**Aktifleştirme Item:** Ender Eye

**Adımlar:**
```
1. 5x5 Stone Bricks platform oluştur
2. Köşelere Gold Block, merkeze Emerald Block koy
3. Eline Ender Eye al
4. Emerald Block'a SAĞ TIKLA
5. Tek Gözlü Dev spawn olur!
```

---

### Seviye 4 Bosslar

#### 7. Titan Golem (TITAN_GOLEM) - 3 Faz, Zayıf: 🔥 Alev

**Ritüel Deseni:**
```
D O O O O O D
O · · · · · O
O · · · · · O
O · · N · · O  (D = Diamond Block, O = Obsidian, N = Netherite Block, · = Boş)
O · · · · · O
O · · · · · O
D O O O O O D
```

**Aktifleştirme Item:** Nether Star

**Adımlar:**
```
1. 7x7 Obsidian platform oluştur
2. Köşelere Diamond Block, merkeze Netherite Block koy
3. Eline Nether Star al
4. Netherite Block'a SAĞ TIKLA
5. Titan Golem spawn olur!
```

**Not:** Nether Star iki boss için kullanılır (Titan Golem ve Khaos Tanrısı). Sistem ritüel desenine göre hangi boss olduğunu belirler.

---

#### 8. Cehennem Ejderi (HELL_DRAGON) - 2 Faz, Zayıf: 💧 Su

**Ritüel Deseni:**
```
O N N N N N O
N · · · · · N
N · · · · · N
N · · B · · N  (O = Obsidian, N = Netherrack, B = Beacon, · = Boş)
N · · · · · N
N · · · · · N
O N N N N N O
```

**Aktifleştirme Item:** Blaze Rod

**Adımlar:**
```
1. 7x7 Netherrack platform oluştur
2. Köşelere Obsidian, merkeze Beacon koy
3. Eline Blaze Rod al
4. Beacon'a SAĞ TIKLA
5. Cehennem Ejderi spawn olur!
```

---

#### 9. Hydra (HYDRA) - 3 Faz, Zayıf: ☠️ Zehir

**Ritüel Deseni:**
```
E P P P P P E
P · · · · · P
P · · · · · P
P · · C · · P  (E = Emerald Block, P = Prismarine, C = Conduit, · = Boş)
P · · · · · P
P · · · · · P
E P P P P P E
```

**Aktifleştirme Item:** Heart of the Sea

**Adımlar:**
```
1. 7x7 Prismarine platform oluştur
2. Köşelere Emerald Block, merkeze Conduit koy
3. Eline Heart of the Sea al
4. Conduit'e SAĞ TIKLA
5. Hydra spawn olur!
```

---

#### 10. Phoenix (PHOENIX) - 2 Faz, Zayıf: 💧 Su

**Ritüel Deseni:**
```
N N N N N
N · · · N
N · B · N  (N = Netherrack, B = Beacon, · = Boş)
N · · · N
N N N N N
```

**Aktifleştirme Item:** Blaze Powder

**Adımlar:**
```
1. 5x5 Netherrack platform oluştur
2. Merkeze Beacon koy
3. Eline Blaze Powder al
4. Beacon'a SAĞ TIKLA
5. Phoenix spawn olur!
```

---

### Seviye 5 Bosslar

#### 11. Hiçlik Ejderi (VOID_DRAGON) - 3 Faz

**Ritüel Deseni:**
```
O O O O O O O
O · · · · · O
O · · · · · O
O · · E · · O  (O = Obsidian, E = End Portal Frame, · = Boş)
O · · · · · O
O · · · · · O
O O O O O O O
```

**Aktifleştirme Item:** Ender Dragon Egg

**Adımlar:**
```
1. 7x7 Obsidian platform oluştur
2. Merkeze End Portal Frame koy
3. Eline Ender Dragon Egg al
4. End Portal Frame'e SAĞ TIKLA
5. Hiçlik Ejderi spawn olur!
```

---

#### 12. Kaos Titani (CHAOS_TITAN) - 3 Faz

**Ritüel Deseni:**
```
N N N N N N N
N D · · · D N
N · · · · · N
N · · B · · N  (N = Netherite Block, D = Diamond Block, B = Beacon, · = Boş)
N · · · · · N
N D · · · D N
N N N N N N N
```

**Aktifleştirme Item:** Nether Star

**Adımlar:**
```
1. 7x7 Netherite Block platform oluştur
2. Köşelere Diamond Block koy (kenarlarda)
3. Merkeze Beacon koy
4. Eline Nether Star al
5. Beacon'a SAĞ TIKLA
6. Kaos Titani spawn olur!
```

---

#### 13. Khaos Tanrısı (CHAOS_GOD) - 3 Faz, Zayıf: 🔥 Alev + ☠️ Zehir

**Ritüel Deseni:**
```
N B B B B B B B N
B O · · · · · O B
B · · · · · · · B
B · · · · · · · B
B · · · E · · · B  (N = Netherite Block, B = Bedrock, O = Obsidian, E = End Stone Bricks, · = Boş)
B · · · · · · · B
B · · · · · · · B
B O · · · · · O B
N B B B B B B B N
```

**Aktifleştirme Item:** Nether Star

**Adımlar:**
```
1. 9x9 Bedrock platform oluştur
2. Köşelere Netherite Block, kenarlara Obsidian, merkeze End Stone Bricks koy
3. Eline Nether Star al
4. End Stone Bricks'e SAĞ TIKLA
5. Khaos Tanrısı spawn olur!
```

**Not:** Nether Star üç boss için kullanılır. Sistem ritüel desenine göre hangi boss olduğunu belirler:
- 7x7 Obsidian + Merkez Netherite Block + Köşeler Diamond → Titan Golem
- 7x7 Netherite Block + Merkez Beacon + Kenarlar Diamond → Kaos Titani
- 9x9 Bedrock + Merkez End Stone Bricks + Köşeler Netherite + Kenarlar Obsidian → Khaos Tanrısı

---

## 🐾 CANLI EĞİTME RİTÜELLERİ

**YENİ ÖZELLİK**: Artık tüm eğitim ritüellerinde **Eğitim Çekirdeği** kullanılıyor! Detaylı bilgi için `17_egitme_sistemi.md` dosyasına bakın.

### Genel Adımlar

**Tüm Eğitme Ritüelleri İçin:**
```
1. Canlının zorluk seviyesini belirle (1-5)
   → Merkeze yakın = Seviye 1
   → Merkeze uzak = Seviye 5

2. Eğitim Çekirdeği'ni yerleştir (merkeze)

3. Ritüel platformunu kur (seviyeye göre, çekirdeğin altına)

4. Canlıyı platformun üzerine getir

5. Eline aktivasyon itemini al

6. Eğitim Çekirdeği'ne SAĞ TIKLA

7. SONUÇ:
   - Canlı eğitilir
   - Cinsiyet belirlenir (♂/♀)
   - Parıldama efekti
   - Sahibini takip eder
```

**Cooldown**: 30 saniye (ritüel başına)

**Admin Komutu ile Otomatik Yapı**: `/scadmin tame build <seviye>`

---

### Zorluk Seviyesi Ritüelleri

#### Seviye 1: Basit Canlılar (0-200 blok)

**Platform:**
```
C C C
C H C  (C = Cobblestone, H = Hay Bale)
C C C
```

**Aktivasyon İtemi:** Wheat (Buğday)

---

#### Seviye 2: Orta Canlılar (200-1000 blok)

**Platform:**
```
S S S
S I S  (S = Stone, I = Iron Block)
S S S
```

**Aktivasyon İtemi:** Carrot (Havuç)

---

#### Seviye 3: İleri Canlılar (1000-3000 blok)

**Platform:**
```
O O O
O D O  (O = Obsidian, D = Diamond Block)
O O O
```

**Aktivasyon İtemi:** Golden Apple (Altın Elma)

---

#### Seviye 4: Çok İleri Canlılar (3000-5000 blok)

**Platform:**
```
B B B B B
B · · · B
B · N · B  (B = Bedrock, N = Netherite Block, · = Boş)
B · · · B
B B B B B
```

**Aktivasyon İtemi:** Enchanted Golden Apple (Büyülü Altın Elma)

---

#### Seviye 5: Efsanevi Canlılar (5000+ blok)

**Platform:**
```
B B B B B B B
B · · · · · B
B · · · · · B
B · · E · · B  (B = Bedrock, E = End Crystal, · = Boş)
B · · · · · B
B · · · · · B
B B B B B B B
```

**Aktivasyon İtemi:** Nether Star

---

### Boss Eğitme Ritüelleri

Bosslar için özel ritüel desenleri vardır. Her boss'un kendi eğitme ritüeli vardır. Detaylı bilgi için `17_egitme_sistemi.md` dosyasına bakın.

**Genel Adımlar:**
```
1. Boss'u yakalayın (eğitilmiş olmamalı)
2. Boss'un eğitme ritüel desenini kurun
3. Boss'u platformun üzerine getirin
4. Eline aktivasyon itemini alın
5. Merkez bloğa SAĞ TIKLA
6. Boss eğitilir!
```

---

## 💕 ÇİFTLEŞTİRME RİTÜELLERİ

Eğitilmiş canlılar çiftleştirilebilir. İki yöntem vardır: Doğal çiftleştirme ve Çiftleştirme Tesisi. Detaylı bilgi için `18_ureme_sistemi.md` dosyasına bakın.

### Doğal Çiftleştirme

**Gereksinimler:**
- 1 dişi eğitilmiş canlı (♀)
- 1 erkek eğitilmiş canlı (♂)
- Her ikisi de aynı sahibe ait olmalı
- Yemek item'ı (canlı türüne göre)

**Adımlar:**
```
1. Dişi ve erkek canlıları yan yana getir (5 blok mesafe içinde)
2. Her ikisine de yemek ver (SAĞ TIKLA)
3. Kalp partikülleri görünür
4. 1 dakika bekle
5. Yavru doğar!
```

**Süre**: 1 dakika (60 saniye)

**Notlar:**
- Memeli canlılar → Direkt yavru doğar
- Yumurtlayan canlılar → Yumurta bırakır (kaplumbağa mantığı)

---

### Çiftleştirme Tesisi

Çiftleştirme tesisleri, çiftleştirme süresini hızlandırır ve daha fazla kontrol sağlar.

**Gereksinimler:**
- Çiftleştirme Tesisi (seviyeye göre)
- 1 dişi + 1 erkek eğitilmiş canlı
- Yiyecek (en az 3 blok)

**Tesis Seviyeleri:**

**Seviye 1:**
- Süre: 1 gün (24 saat)
- Platform: 3x3 Hay Bale

**Seviye 2:**
- Süre: 2 gün (48 saat)
- Platform: 5x5 Hay Bale

**Seviye 3:**
- Süre: 3 gün (72 saat)
- Platform: 7x7 Hay Bale

**Seviye 4:**
- Süre: 4 gün (96 saat)
- Platform: 9x9 Hay Bale

**Seviye 5:**
- Süre: 5 gün (120 saat)
- Platform: 11x11 Hay Bale

**Adımlar:**
```
1. Çiftleştirme tesisini kur (admin komutu veya manuel)
2. Dişi ve erkek canlıları tesise getir
3. Yiyecekleri tesise koy (en az 3 blok)
4. Çiftleştirme başlar
5. Süre bitince yavru doğar!
```

---

## 📖 HIZLI RİTÜEL REHBERİ (GÜNCEL)

```
Klan Kur:
→ Klan Kristali kullan (ritüel yok)

Üye Al:
→ 3x3 Stripped Log
→ Shift + Çakmak (Oyuncu üstündeyken)

Terfi Ver:
→ 3x3 Stone Brick + 4 Redstone Torch + Ateş
→ Külçe at (Altın=General, Demir=Üye)

Müttefik Ol:
→ İki Lider + Shift + Elmas + Sağ Tık

Liderlik Devret:
→ Kristal Yanı + Altın Kask + Shift + Sağ Tık

Klan İsmi Değiştir:
→ Kristal Yanı + Named Paper + Shift + Sağ Tık

Klandan At (Sürgün):
→ Soul Fire + Named Paper (İsimli kağıdı ateşe at)

Klandan Ayrıl:
→ Ateş + Named Paper + Shift + Sağ Tık

Boss Çağır:
→ Ritüel deseni kur + Aktifleştirme item + Sağ Tık

Canlı Eğit:
→ Ritüel deseni kur + Canlıyı getir + Aktifleştirme item + Sağ Tık

Canlı Çiftleştir:
→ 1 Dişi + 1 Erkek + Yemek ver (Doğal)
→ Veya Çiftleştirme Tesisi kullan
```

---

---

## ⚡ RİTÜEL GÜÇ SİSTEMİ (YENİ)

### ✅ Ritüel Başarılı Olduğunda Güç Kazanma

**Ritüeller artık klan gücüne katkı sağlıyor!**

Başarılı ritüeller, klanın **Ritüel Kaynak Gücü**ne eklenir ve klanın toplam gücünü artırır.

### Nasıl Çalışır?

**1. Ritüel Başarılı Olduğunda:**
```
- Ritüel başarıyla tamamlanır
- Kullanılan kaynaklar kaydedilir
- Klan gücüne eklenir
```

**2. Güç Hesaplama:**
```
Ritüel Kaynak Gücü = Σ (Kaynak Tipi × Miktar × Ritüel Çarpanı)

Örnek:
- Demir: 5 puan/kaynak
- Elmas: 10 puan/kaynak
- Kızıl Elmas: 18 puan/kaynak
- Karanlık Madde: 50 puan/kaynak
```

**3. Desteklenen Ritüeller:**
- ✅ **Üye Alma Ritüeli** (Ateş Ritüeli) - Çakmak tüketir
- ✅ **Ayrılma Ritüeli** (Kağıt Ritüeli) - Kağıt tüketir
- ✅ **Batarya Ateşleme** - Yakıt tipine göre (Demir, Elmas, Kızıl Elmas, Karanlık Madde)

### Önemli Notlar

**Sadece Başarılı Ritüeller:**
- ❌ Başarısız ritüeller güç vermez
- ✅ Sadece başarıyla tamamlanan ritüeller güç verir

**Klan Gücüne Etkisi:**
- Ritüel gücü, klanın toplam gücüne eklenir
- Klan seviyesi hesaplamasında kullanılır
- Felaket zorluğunu etkiler

### Config Ayarları

Ritüel güç değerleri `config.yml` dosyasından ayarlanabilir:

```yaml
clan-power-system:
  ritual-resources:
    iron: 5
    diamond: 10
    red-diamond: 18
    dark-matter: 50
    titanium: 15
    default: 3
```

---

**🎮 Ritüellerle gücü elde et, fiziksel dünyayı şekillendir!**
