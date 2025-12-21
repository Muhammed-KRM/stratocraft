# STRATOCRAFT - KLAN SİSTEMİ

## 🎯 Klan Sistemi Nedir?

Klan, Stratocraft'ta hayatta kalmanın temeli. Kendi bölgenizi oluşturun, yapılarla güçlenin, düşmanlara karşı savunun!

**Önemli**: Hiçbir `/komut` kullanılmaz. Her şey **fiziksel etkileşimle** yapılır.

**Son Güncellemeler** ⭐:
- ✅ **Klan Kristali Persistence Sistemi**: Sunucu restart sonrası kristaller otomatik restore edilir
- ✅ **Veri Tutarlılığı Düzeltmeleri**: `crystalLocation` ve `hasCrystal` senkronizasyonu
- ✅ **Kristal Kırma Sistemi**: Kristal kırıldığında klan otomatik dağıtılır ve özel item drop edilir

---

## 📋 İÇİNDEKİLER

1. [Klan Kurma](#klan-kurma)
2. [Bölge Oluşturma](#bölge-oluşturma)
3. [Üye Yönetimi](#üye-yönetimi)
4. [Rütbe Sistemi](#rütbe-sistemi)
5. [Savaş ve İlan](#savaş-ve-ilan)
6. [İttifaklar](#ittifaklar)
7. [Klan Güç Sistemi](#klan-güç-sistemi) ⭐ YENİ

---

## 🏰 KLAN KURMA

### ⛔ Başlangıç Koruması (Grace Period)

**Yeni kurulan klanlar 24 saat korunur!**

- Yeni kurulan klanlara **24 saat** boyunca saldırılamaz
- Bu süre içinde klan güvenli bir şekilde gelişebilir
- Grace period süresi dolduktan sonra normal savaş kuralları geçerli olur

**Koruma Özellikleri:**
- Kuşatma anıtı dikilemez
- Bölge koruması aktif
- Normal oyun mekanikleri çalışır (sadece saldırı engellenir)

---

### Adım 1: Malzeme Toplama

**Gerekli Craft'lar**:
```
Klan Kristali:
 [Diamond Block] 
[Diamond Block][Ender Pearl][Diamond Block]
 [Obsidian]

= 1x Klan Kristali

Klan Çiti:
[Oak Planks][Iron Ingot][Oak Planks]
[Oak Planks][Iron Ingot][Oak Planks]

= 64x Klan Çiti
```

### Adım 2: Alan Belirleme

1. İstediğin yeri seç (düz alan tercih edilir)
2. **Minimum 10x10** alan seç (maksimum limit yok, istediğin kadar büyük olabilir)
3. Bu alan senin başlangıç bölgen olacak

### Adım 3: Çitleri Yerleştir

**Kapalı Dörtgen Oluştur**:
```
Yukarıdan bakış:

[Ç][Ç][Ç][Ç][Ç][Ç][Ç]
[Ç]                 [Ç]
[Ç]    [İÇ ALAN]   [Ç]    Ç = Klan Çiti
[Ç]                 [Ç]
[Ç][Ç][Ç][Ç][Ç][Ç][Ç]

DİKKAT: Çitler birbirine değmeli!
```

**Kurallar**:
- Çitler arasında 2 bloktan fazla boşluk OL MAMALI
- Alan kapalı olmalı (delik olmamalı)
- Minimum 10x10 (maksimum limit yok)

### Adım 4: Kristali Yerleştir

1. Çevrelediğin alanın **içine** gir
2. Eline **Klan Kristali** al
3. İstediğin yere (tercihen ortaya) **sağ tık**
4. **Sonuç**: 
   - Ender Crystal spawn olur
   - Şimşek efekti + TOTEM partikülleri
   - "§a[Klan Adı] klanı kuruldu!" mesajı
   - Çitlerin çevrelediği alan → **Klan Bölgesi**

**Önemli Notlar** ⭐ YENİ:
- ✅ **Persistence**: Kristal konumu veritabanında saklanır, sunucu restart sonrası otomatik restore edilir
- ✅ **Veri Tutarlılığı**: `crystalLocation` ve `hasCrystal` otomatik senkronize edilir
- ✅ **Metadata**: Kristal entity'sine metadata eklenir (klan kristali olduğunu belirtmek için)

---

## 🗺️ BÖLGE OLUŞTURMA

### Bölge Nasıl Belirlenir?

**Klan Çitinin Çevrelediği Alan = Bölge Sınırı**

```
Flood-Fill Algoritması:
1. Kristal yerleştirilir
2. Sistem kristalden başlayarak tüm yönlere yayılır
3. Klan Çiti (OAK_FENCE) ile karşılaşınca durur
4. Kapalı alan tespit edilirse → Bölge oluşur
5. Açık alan ise → "Çitlerle tam çevrele!" hatası
```

### Bölge Özellikleri

**Koruma**:
- Düşman klanlarmakla **blok kıramaz**
- Düşman klanlar **inşaat yapamaz**
- Sadece klan üyeleri yapabilir

**Klan Üyeleri İçin**:
- Üyeler birbirlerine **zarar veremez** (bölge içinde)
- Düşman girse bile üyeler birbirlerine dokunmuyor

**Genişletme**:
- Çitleri genişlet → Bölge otomatik büyür
- Maksimum limit yok, istediğin kadar genişletebilirsin

---

## 👥 ÜYE YÖNETİMİ

### Üye Davet Ritüeli

**Gereksinimler**:
- 3x3 Taş Tuğla (Stone Brick) platform
- Ortada Ateş (Campfire veya normal ateş)
- Name Tag (isim etiketi)

**Adımlar**:
```
1. Klan bölgesine 3x3 Taş Tuğla döşe:

[S][S][S]
[S][F][S]    S = Stone Brick
[S][S][S]    F = Fire/Campfire

2. Lider: Eline Name Tag al
3. Lider: Ateşe Shift + Sağ Tık
4. Davet edilen oyuncu: Ateşe gir
5. Sonuç: Klan üyesi olur!
```

**Görsel Efektler**:
- Yeşil partikül ler
- Başarı sesi
- "§a[Oyuncu] klanınıza katıldı!"

### Üye Çıkarma

**Yöntem**: Lider veya General, o üyeyi "Terfi Ritüeli" platformuna çağırır ve **elinde hiçbir şey olmadan** Shift+Tık yapar.

**Sonuç**: Üye klan

dan çıkarılır.

---

## 👑 RÜTBE SİSTEMİ

### Gelişmiş Yetki Sistemi (ClanRankSystem) ⭐ YENİ

**Yeni Özellik**: Klan sistemi artık **ClanRankSystem** ile çalışıyor. Detaylı yetki kontrolü yapılıyor.

**Yetki Tipleri**:
- `BUILD_STRUCTURE` - Yapı inşa etme
- `DESTROY_STRUCTURE` - Yapı yıkma
- `ADD_MEMBER` - Üye ekleme
- `REMOVE_MEMBER` - Üye çıkarma
- `START_WAR` - Savaş başlatma
- `MANAGE_BANK` - Banka yönetimi
- `WITHDRAW_BANK` - Bankadan para çekme (limitli)
- `MANAGE_ALLIANCE` - İttifak yönetimi
- `USE_RITUAL` - Ritüel kullanma
- `START_MISSION` - Görev başlatma
- `TRANSFER_LEADERSHIP` - Liderlik devretme

### Rütbeler ve Yetkiler

```
┌─────────────┬──────────────────────────────┐
│ Rütbe       │ Yetkiler                    │
├─────────────┼──────────────────────────────┤
│ LEADER      │ - Tüm yetkiler              │
│ (Lider)     │ - Klanı dağıtabilir         │
│             │ - Üye çıkarabilir           │
│             │ - Rütbe verebilir           │
│             │ - Kristali taşıyabilir      │
│             │ - Liderlik devredebilir     │
├─────────────┼──────────────────────────────┤
│ GENERAL     │ - Üye davet edebilir        │
│ (Komutan)   │ - Yapı kur/yıkabilir        │
│             │ - Savaş ilan edebilir       │
│             │ - Banka yönetebilir         │
│             │ - İttifak yönetebilir       │
│             │ - Bölge yönetimi            │
├─────────────┼──────────────────────────────┤
│ ELITE       │ - Yapı kurabilir            │
│ (Seçkin)    │ - Ritüel kullanabilir       │
│             │ - Bankadan çekebilir (limitli)│
│             │ - Görev başlatabilir        │
├─────────────┼──────────────────────────────┤
│ MEMBER      │ - Yapı kullanabilir         │
│ (Üye)       │ - Sandık kullanabilir       │
│             │ - Yapılardan faydalanır     │
│             │ - Savaşabilir               │
│             │ - Blok KIRAMAZ ⚠️ YENİ      │
│             │ - Blok KOYAMAZ ⚠️ YENİ      │
├─────────────┼──────────────────────────────┤
│ RECRUIT     │ - Sadece gezinebilir        │
│ (Acemi)     │ - Yapı KURAMAZ              │
│             │ - Yapı YIKAMAZ              │
│             │ - Hiçbir yetki yok          │
└─────────────┴──────────────────────────────┘
```

**Önemli**: Yapı kurma işlemlerinde **ClanRankSystem** kontrolü yapılıyor. RECRUIT rütbesindeki oyuncular yapı kuramaz!

### Terfi Ritüeli

**Platform Kurulumu**:
```
3x3 Taş Tuğla + 4 Köşede Kızıltaş Meşalesi:

[T]   [S]   [T]
    [S][S]
[F]  [S]F]  [F]    S = Stone Brick
    [S][S]          F = Redstone Torch
[T]   [S]   [T]    T = Köşede Meşale

Ortada ateş yak
```

**Terfi Verme**:
```
1. Lider terfi ettireceği kişiye yaklaşır
2. Lider eline rütbeye göre külçe alır:
   - Altın Külçe → GENERAL
   - Demir Külçe → MEMBER
3. Lider o kişinin üzerine külçeyi atar (Q tuşu)
4. Sonuç: Rütbe verilir!
```

**Görsel Efektler**:
- Altın partiküller (General için)
- Gri partiküller (Member için)
- Şimşek efekti
- Başarı sesi

**Güvenlik Kontrolleri** ⭐ YENİ:
- ✅ **Klan Üyeliği Kontrolü**: Terfi edilecek oyuncu mutlaka klan üyesi olmalı
- ✅ **Null Check**: Elindeki item null kontrolü yapılıyor (güvenlik)
- ✅ **Cooldown Sistemi**: Ritüel spam önleme için cooldown var

---

## ⚔️ SAVAŞ VE İLAN

### Savaş Açma Koşulları

**Yetki Gereksinimleri**:
```
Sadece General ve Lider savaş açabilir:
- Member (Üye) → Savaş açamaz
- Recruit (Acemi) → Savaş açamaz
```

**Aktif Üye Gereksinimleri**:
```
1. Klanın %35'i aktif olmalı (online)
   Örnek: 10 üyeli klan → En az 4 üye online

2. En az bir General aktif olmalı
   → Lider veya General online olmalı
```

**Savaş Açma Yöntemi**:
```
1. Düşman klanın 50 blok yakınına Beacon dik
2. 3x3 Obsidian piramit yap
3. TNT ile aktifleştir
4. Savaş başlar!
```

---

### Otomatik Savaş İlanı

Stratocraft'ta savaş **iki durumda otomatik** başlar:

#### **1. Yakın Klan Kurulması**
```
Durum:
- Klan A'nın kristali var
- Klan B, Klan A'nın kristalinden 100 blok içine klan kurar

SONUÇ: Otomatik savaş!

Mesaj:
"§c§l[SAVAŞ İLANI]
§cKlan A ile Klan B arasında savaş başladı!
§7Sebep: Yakın bölge"
```

#### **2. Saldırı Yapısı Kurulması**
```
Durum:
- Klan A'nın bölgesi var
- Klan B, Klan A'nın sınırına 50 blok mesafede saldırı yapısı koyar

Saldırı Yapıları:
- Mancınık (Catapult)
- Balista (Ballista)
- Lav Fıskiyesi
- Zehir Dağıtıcı

SONUÇ: Otomatik savaş!
```

### Savaş Kuralları

#### **Seviye Koruması (Klan Koruma Sistemi)**

**Kural 1: Saldırı Engelleme**
```
Klan seviyeleri 3'ten fazla fark varsa saldırı YASAK:

Örnek 1:
Klan A (Seviye 5) → Klan B (Seviye 1) = YASAK (5-1=4 > 3)

Örnek 2:
Klan A (Seviye 3) → Klan B (Seviye 2) = İZİNLİ (3-2=1 ≤ 3)
```

**Kural 2: Hasar Azaltma**
```
3 seviye altındaki klanın oyuncularına %95 hasar azaltma:

Örnek:
Klan A (Seviye 5) → Klan B (Seviye 1) oyuncusuna saldırırsa:
- Normal hasar: 10 kalp
- Gerçek hasar: 0.5 kalp (%95 azalma)

Bu koruma:
✓ PvP savaşlarında aktif
✓ Kuşatma sırasında aktif
✓ Batarya saldırılarında aktif
✓ Tuzak hasarlarında aktif
```

**Klan Seviyesi Hesaplama**:
```
Seviye = Yapı Sayısı × Yapı Seviyesi

Örnek:
- 5 yapı × Lv1 = 5 puan
- 2 yapı × Lv3 = 6 puan
Toplam Seviye = 11
```

**Koruma Mantığı**:
```
Amaç: Güçlü klanların zayıf klanları ezmesini önlemek

Koruma Aktif Olduğunda:
- Saldırı engellenir (kuşatma başlatılamaz)
- Hasar %95 azalır (oyunculara zarar verilemez)
- Yapılara hasar verilebilir (ama çok az)

Koruma Kalktığında:
- Normal savaş kuralları geçerli
- Tam hasar verilir
- Kuşatma başlatılabilir
```

#### **Savaş Halinde**

**İzin Verilenler**:
- Düşman yapılarına hasar verme
- Düşman klan üyelerini vurma (PvP)
- Düşman bölgesine girme

**Yasak Olanlar**:
- Düşman bölgesinde blok kırma (sadece yapılara hasar)
- Kristali normal yöntemlerle yok etme
- Ender Pearl ile başkasının klanına ışınlanma

---

## 💎 KRİSTAL YOK ETME

### Nasıl Yıkılır?

**Normal Silahla Vurma**:
- Özel ritüel YOK
- Çitleri kır
- İçeri gir
- Kristale ulaş
- **Normal silahla vur**

### Kristal Kırıldığında

```
Sonuçlar:

1. Klan dağılır
2. Bölge koruması kalkar
3. Tüm yapılar savunmasız kalır
4. Savaş biter

Kazanan Klan:
- Kaybeden kasasının %50'si
- "Fatih" buff'ı (24 saat):
  * +%20 hasar
  * +%30 üretim hızı
- Düşman yapılarının ana malzemeleri düşer
```

---

## 🏳️ BEYAZ BAYRAK - PES ETME SİSTEMİ

### Nasıl Pes Edilir?

Savaşta kaybetmek üzereyseniz, **Beyaz Bayrak** çekerek pes edebilirsiniz.

**Gereksinimler**:
```
- Klanınız savaşta olmalı
- Yetki: General veya Lider (sadece)
- Beyaz Bayrak (White Banner) klan bölgenizde olmalı
```

**Adımlar**:
```
1. Klan bölgenize White Banner (Beyaz Bayrak) koy
2. Eline hiçbir şey alma (boş el)
3. Shift + Sağ Tık (Beyaz Bayrağa)
4. SONUÇ: Klanınız pes eder
```

**Pes Etme Sonuçları**:
```
✓ Klan YOK OLMAZ (dağılmaz)
✓ Savaş biter
✓ Klandaki TÜM sandıkların itemlerinin YARISI gider
✓ Klan kasasının %50'si kazanan klana gider
✓ Kazanan klan Fatih Buff'ı alır
```

**Önemli**: Pes etmek, kristal kırılmasından daha iyi bir seçenektir çünkü klanınız dağılmaz!

---

## 🛡️ ÖZEL KURALLAR

### Ender Pearl Kısıtlaması

**Kural**: Başkasının klan bölgesine Ender Pearl ile ışınlanamazsın!

```
İzin Verilenler:
✓ Kendi klan bölgene ışınlanabilirsin
✓ Misafir olduğun klana ışınlanabilirsin

Yasak:
❌ Başkasının klan bölgesine ışınlanamazsın
```

**Mesaj**: "§cEnder Pearl ile başkasının klan bölgesine ışınlanamazsın!"

---

### Kristal Taşıma

**Sadece Lider** kristali taşıyabilir:
```
1. Lider kristale yaklaş
2. Shift + Boş El + Sağ Tık
3. Yeni konum seç (çit içinde olmalı)
4. Kristal ışınlanır
```

### Offline Koruma

**Kristale Yakıt Ekle**:
```
1. Kömür veya Kükürt al
2. Kristale sağ tık
3. Yakıt eklenir

Etki:
- Klan üyeleri çevrimdışıyken kristal hasar almaz
- Maks 12 saat yakıt
- Her saldırıda yakıt tüketir
```

---

## 🎯 HIZLI KLAN KURMA REHBERİ

```
Gün 1: Malzeme Toplama
→ 3 Diamond Block + 1 Ender Pearl + 1 Obsidian
→ 64+ Oak Planks + 10+ Iron Ingot

Gün 2: Craft
→ 1x Klan Kristali
→ 64x Klan Çiti

Gün 3: Bölge Seçimi
→ Düz alan bul
→ 20x20 veya büyük alan planla

Gün 4: Çitleri Diz
→ Kapalı dörtgen oluştur
→ Çitlerin birbirine değdiğinden emin ol

Gün 5: Kristal Yerleştir
→ İçine gir
→ Kristali koy
→ KLAN KURULDU!
→ **24 saat başlangıç koruması aktif!**

---

## 🤝 İTTİFAK SİSTEMİ

İttifaklar, klanlar arası **kalıcı ve bağlayıcı** anlaşmalardır. Kontrat sistemine benzer şekilde çalışır ancak **daha güçlü ve cezalı**dır.

### İttifak Tipleri

1. **Savunma İttifakı (DEFENSIVE)**: Bir klana saldırılırsa diğeri otomatik yardım eder
2. **Saldırı İttifakı (OFFENSIVE)**: Birlikte saldırı yapılır
3. **Ticaret İttifakı (TRADE)**: Ticaret bonusları
4. **Tam İttifak (FULL)**: Tüm özellikler (en güçlü)

### İttifak Kurma Ritüeli

**Gereksinimler:**
- **İki Lider**: Her iki klanın lideri olmalı
- **Elmas**: Her iki liderin elinde Elmas olmalı
- **Yakınlık**: İki lider birbirine 3 blok yakın olmalı
- **Shift**: Her iki lider Shift'e basılı tutmalı

**Adımlar:**
```
1. İki lider birbirine yaklaşır (3 blok mesafe)
2. Her ikisi de Shift'e basılı tutar
3. Her ikisinin elinde Elmas olmalı
4. Bir lider diğerine Shift + Sağ Tık yapar
5. SONUÇ:
   - İttifak kurulur
   - Elmaslar tüketilir
   - Partikül efektleri
   - Sunucuya duyuru
```

**Görsel Efektler:**
- HEART partikülleri (kırmızı)
- END_ROD partikülleri (beyaz)
- TOTEM partikülleri (renkli)
- "İTTİFAK KURULDU" title
- Sunucu broadcast mesajı

**Cooldown:**
- Her klan 5 dakika içinde tekrar ittifak kuramaz

### İttifak İhlali ve Ceza

**İhlal Durumları:**
1. **İttifaklı klana saldırı**: İttifaklı klana kuşatma başlatmak
2. **İttifaklı klanı yok etme**: İttifaklı klanın kristalini kırmak
3. **İttifakı tek taraflı bozma**: Ritüel olmadan ittifakı sonlandırma

**Ceza Sistemi:**
```
İhlal Edildiğinde:
- İhlal eden klanın bakiyesinin %20'si kesilir
- İhlal eden klan üyelerine "HAİN" etiketi verilir
- Diğer klana tazminat ödenir (ihlal eden klanın bakiyesinden %10)
- Sunucuya duyuru yapılır
```

**Örnek:**
```
Klan A ve Klan B ittifak halinde
Klan A, Klan B'ye saldırır
→ Klan A'nın bakiyesi: 10000 altın
→ Ceza: 2000 altın kesilir
→ Klan B'ye tazminat: 1000 altın
→ Klan A üyeleri: [HAİN] etiketi alır
```

### İttifak Sonlandırma

**Karşılıklı Sonlandırma:**
- İki lider birlikte ritüel yaparak ittifakı sonlandırabilir
- **Cezasız** sonlandırma
- Ritüel: Elinde Kırmızı Çiçek ile aynı ritüel

**Tek Taraflı Sonlandırma:**
- İttifakı ihlal etmek = otomatik sonlandırma + ceza

**Süre Dolması:**
- Eğer ittifak süreli ise, süre dolunca otomatik sona erer
- Cezasız sonlandırma

### İttifak ve Savaş

**Kurallar:**
- İttifaklı klanlara **saldırılamaz** (otomatik engellenir)
- İttifaklı klanlara saldırı denemesi = **İttifak İhlali**
- İttifaklı klanlar birlikte boss'a saldırabilir

**Örnek Senaryo:**
```
Klan A ve Klan B ittifak halinde
Klan C, Klan A'ya saldırmak ister
→ Klan B otomatik Klan A'yı savunur
→ Klan C hem Klan A hem Klan B ile savaşır
```

### Önemli Notlar

1. **İttifaklar Kalıcıdır**: Bozulmadıkça veya süre dolmadıkça devam eder
2. **İhlal Cezası Ağırdır**: İttifakı bozmak pahalıya mal olur
3. **Sadece Liderler**: İttifak kurma/sonlandırma sadece liderler yapabilir
4. **Cooldown Var**: Spam önleme için 5 dakika cooldown

---

Gün 6: İlk Üyeler
→ Davet ritüeli platformu yap
→ Arkadaşlarını davet et

Gün 7: İlk Yapı
→ Savunma yapısı kur
→ Savaşa hazırlan!
```

---

---

## 💪 KLAN GÜÇ SİSTEMİ (YENİ)

### ✅ Stratocraft Güç Sistemi (SGP)

**Klanlar artık güç puanlarına sahip!**

Her klanın bir **Klan Güç Puanı (Clan Power)** vardır ve bu puan klanın seviyesini belirler.

### Klan Gücü Hesaplama

**Klan Gücü = Üye Güçleri + Yapı Gücü + Ritüel Blok Gücü + Ritüel Kaynak Gücü**

#### 1. Üye Güçleri
```
Tüm klan üyelerinin toplam SGP'si
- Online üyeler: Gerçek zamanlı hesaplama
- Offline üyeler: Cache'den (24 saat geçerli)
```

#### 2. Yapı Gücü
```
Yapı Seviyesi → Güç:
- Seviye 1: 100 puan
- Seviye 2: 250 puan
- Seviye 3: 500 puan
- Seviye 4: 1200 puan
- Seviye 5: 2000 puan
- Klan Kristali: +500 puan (sabit)
```

#### 3. Ritüel Blok Gücü
```
Ritüel Blokları → Güç:
- Demir Blok: 8 puan/blok
- Obsidyen: 30 puan/blok
- Elmas Blok: 25 puan/blok
- Altın Blok: 12 puan/blok
- Zümrüt Blok: 35 puan/blok
- Netherite Blok: 150 puan/blok
```

**Not:** Event-based tracking sistemi kullanılır (performans için)

#### 4. Ritüel Kaynak Gücü
```
Ritüel Kaynakları → Güç:
- Demir: 5 puan/kaynak
- Elmas: 10 puan/kaynak
- Kızıl Elmas: 18 puan/kaynak
- Karanlık Madde: 50 puan/kaynak
- Titanyum: 15 puan/kaynak
```

**Not:** Sadece başarılı ritüeller güç verir

### Klan Seviyesi

**Klan seviyesi, klan gücüne göre logaritmik olarak hesaplanır:**

```
Seviye = log(Klan Gücü / 500) × 2.0

Maksimum Seviye: 15
```

### Komutlar

**Klan gücünü görmek için:**
```
/sgp clan
```

**Top klanları görmek için:**
```
/sgp top clans [limit]
```

### Oyuncu Seviyesi

**Oyuncu seviyesi, hibrit algoritma ile hesaplanır:**

```
Aşama 1 (1-10 Seviye): Karekök (hızlı ilerleme)
Seviye = √(Güç / 100)

Aşama 2 (11+ Seviye): Logaritmik (zor ilerleme)
Seviye = 10 + log₁₀(Güç / 10000) × 3.0

Maksimum Seviye: 20
```

**Örnek:**
```
500 puan → Seviye 2 (karekök)
5000 puan → Seviye 7 (karekök)
10000 puan → Seviye 10 (karekök)
50000 puan → Seviye 14 (logaritmik)
```

### PvP Koruma Sistemi

**Güçlü oyuncular zayıf oyunculara saldıramaz!**

#### 1. Onurlu Savaş Aralığı (Honorable Combat Range)
```
Kural:
Hedef Gücü < Saldıran Gücü × 0.5 ise
    → Saldırı YASAK

Örnek:
Saldıran: 10,000 puan
Hedef: 4,000 puan
Eşik: 10,000 × 0.5 = 5,000 puan

4,000 < 5,000 → Saldırı YASAK ❌
```

#### 2. Acemi Koruması (Rookie Protection)
```
Kural:
Hedef Gücü < 5,000 puan VE
Saldıran Gücü > 10,000 puan VE
Hedef ilk saldıran DEĞİLSE
    → Saldırı YASAK

Amaç: Yeni oyuncuları güçlü oyunculardan korumak
```

#### 3. Klan Savaşı İstisnası
```
Klan savaşında:
→ Tüm koruma kuralları DEVRE DIŞI
→ Stratejik saldırılar yapılabilir
```

#### 4. Klan İçi Koruma
```
Aynı klandaki oyuncular için:
→ Daha katı eşik (%60)
→ Veya tamamen kapalı (config'den)
```

#### 5. Histerezis Sistemi (Zırh Çıkarma Exploit Önleme)
```
Zırh çıkarıldığında:
→ Güç hemen düşmez
→ 60 saniye gecikme ile düşer
→ Bu süre içinde koruma aktif kalır

Amaç: Hızlı zırh çıkarıp takma exploit'ini önlemek
```

### HUD Entegrasyonu

**Güç bilgisi otomatik olarak HUD'da görünür:**
```
Sağ taraftaki bilgi panosunda:
💪 Güç: 1234 SGP (Seviye 5)

- Her saniye güncellenir (cache ile optimize)
- Thread-safe yapı
- 5 saniyelik cache (performans)
```

### Config Ayarları

Klan güç değerleri `config.yml` dosyasından ayarlanabilir:

```yaml
clan-power-system:
  structure-power:
    crystal-base: 500
    level-1: 100
    level-2: 250
    level-3: 500
    level-4: 1200
    level-5: 2000
  ritual-blocks:
    iron: 8
    obsidian: 30
    diamond: 25
    gold: 12
    emerald: 35
    titanyum: 150
  ritual-resources:
    iron: 5
    diamond: 10
    red-diamond: 18
    dark-matter: 50
  level-system:
    player-base-power: 100
    player-switch-power: 10000
    player-log-multiplier: 3.0
    player-max-level: 20
    clan-base-power: 500
    clan-multiplier: 2.0
    max-clan-level: 15
  protection:
    threshold: 0.5
    clan-threshold: 0.6
    rookie-threshold: 5000
    strong-player-threshold: 10000
    gear-decrease-delay: 60000
  power-weights:
    combat: 0.6
    progression: 0.4
```

---

---

## 🔧 SON GÜNCELLEMELER (Son 3 Gün) ⭐

### Klan Kristali Persistence ve Restore Sistemi

**Sorun:** Sunucu restart sonrası klan kristalleri kayboluyordu ve klanlar "kristalsiz klan" durumuna düşüyordu.

**Çözüm:** Kapsamlı persistence ve restore sistemi eklendi.

#### Veri Tutarlılığı Düzeltmeleri

**Dosya:** `Clan.java`

**Değişiklikler:**
- `setCrystalLocation()`: `crystalLocation` ve `hasCrystal` otomatik senkronize edilir
- `setCrystalEntity()`: `crystalEntity` null olsa bile `crystalLocation` varsa `hasCrystal` true
- `hasCrystal()`: Tutarsızlık kontrolü ve otomatik düzeltme
- `setHasCrystal()`: `hasCrystal` false yapılırsa `crystalLocation` ve `crystalEntity` null yapılır

**Algoritma:**
1. `crystalLocation` varsa → `hasCrystal` otomatik `true`
2. `hasCrystal` `false` yapılırsa → `crystalLocation` ve `crystalEntity` `null` yapılır
3. `hasCrystal()` çağrıldığında tutarsızlık tespit edilirse otomatik düzeltilir

#### Kristal Restore Sistemi

**Dosya:** `Main.java` - `restoreClanCrystals()`

**Çalışma Süreci:**
1. Sunucu açıldığında `onEnable()` içinde `restoreClanCrystals()` çağrılır
2. Her klan için `crystalLocation` kontrol edilir
3. `hasCrystal` tutarsızlığı düzeltilir
4. Chunk yüklenir (gerekirse)
5. Aynı konumda mevcut entity aranır
6. Mevcut entity varsa bağlanır ve metadata eklenir
7. Yoksa yeni entity oluşturulur ve bağlanır

#### Kristal Kırma ve Klan Dağıtma Sistemi

**Dosya:** `TerritoryListener.java`

**Çalışma Süreci:**
1. Kristal hasar alır → `onCrystalBreak()` tetiklenir
2. `findClanByCrystal()` ile klan bulunur (entity referansı veya location kontrolü)
3. Final damage >= 1.0 ise kristal kırılır
4. Klan dağıtılır (`disbandClan()`)
5. Tüm üyelere mesaj gönderilir
6. Patlama efekti gösterilir
7. `onCrystalDeath()` tetiklenir
8. Özel item oluşturulur (PDC verisi ile)
9. Item drop edilir

**Özellikler:**
- Entity referansı kaybolsa bile location kontrolü ile klan bulunur
- Sunucu restart sonrası entity referansı otomatik güncellenir
- Metadata otomatik eklenir (yoksa)
- Özel item drop edilir (PDC verisi ile, tekrar yerleştirilebilir)

**Debug Logları:**
- Tüm kritik noktalara debug logları eklendi:
  - `[CLAN_CRYSTAL_RESTORE]` - Restore süreci
  - `[KRISTAL KIRMA]` - Kırma süreci
  - `[KRISTAL ÖLÜM]` - Item drop süreci
  - `[KRISTAL BULMA]` - Klan bulma süreci
  - `[CLAN]` - Clan model değişiklikleri

Detaylı bilgi için: `SON_3_GUN_DEGISIKLIKLER_VE_SISTEM_DOKUMANI.md` dosyasına bakın.

### İttifak Sistemi Güncellemeleri

**Dosya:** `AllianceManager.java`, `Alliance.java`

**İttifak Tipleri:**
- **DEFENSIVE:** Savunma İttifakı (birine saldırılırsa diğeri yardım eder)
- **OFFENSIVE:** Saldırı İttifakı (birlikte saldırı yapılır)
- **TRADE:** Ticaret İttifakı (ticaret bonusları)
- **FULL:** Tam İttifak (en güçlü)

**Özellikler:**
- ✅ Cooldown kontrolü (spam önleme - 5 dakika)
- ✅ İttifak ihlal cezası (klan bakiyesinin %20'si)
- ✅ Persistence sistemi (`alliances.json`)
- ✅ Otomatik restore (sunucu açılırken)

**İttifak İhlal Cezaları:**
- Klan bakiyesinin %20'si kesilir
- İhlal eden klan üyelerine "Hain" etiketi verilir
- Broadcast mesajı gönderilir

### Klan Bankası Sistemi

**Dosya:** `ClanBankSystem.java`

**Özellikler:**
- ✅ Para yatırma/çekme
- ✅ Item yatırma/çekme (transaction mantığı)
- ✅ Otomatik maaş dağıtımı (24 saatte bir)
- ✅ Transfer kontratları
- ✅ Yetki kontrolü (Leader, General, Elite)

**Transaction Mantığı:**
1. Önce envanterden al (transaction başlat)
2. Sonra bankaya ekle
3. Hata durumunda rollback (item geri ver)

**Otomatik Maaş Sistemi:**
- Her 24 saatte bir otomatik dağıtılır
- Rate limiting ile lag önlenir (5 klan/tick, 10 üye/klan)
- Rütbeye göre maaş item'i (config'den)

### Klan Sanal Envanter Sistemi

**Dosya:** `VirtualStorageListener.java`

**Özellikler:**
- ✅ Her klan için 54 slot sanal envanter
- ✅ Cache ile optimize edilmiş
- ✅ Persistence ile kaydedilir
- ✅ Envanter overflow kontrolü

**Algoritma:**
- Virtual inventory oluşturma (cache kontrolü ile)
- Item ekleme (overflow kontrolü ile)
- Cache temizleme (periyodik)

Detaylı bilgi için: `SON_3_GUN_DEGISIKLIKLER_VE_SISTEM_DOKUMANI.md` dosyasına bakın.

---

**🎮 Klanını kur, bölgeni genişlet, düşmanlara karşı koy!**
