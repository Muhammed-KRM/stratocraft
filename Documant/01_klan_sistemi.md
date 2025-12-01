# STRATOCRAFT - KLAN SİSTEMİ

## 🎯 Klan Sistemi Nedir?

Klan, Stratocraft'ta hayatta kalmanın temeli. Kendi bölgenizi oluşturun, yapılarla güçlenin, düşmanlara karşı savunun!

**Önemli**: Hiçbir `/komut` kullanılmaz. Her şey **fiziksel etkileşimle** yapılır.

---

## 📋 İÇİNDEKİLER

1. [Klan Kurma](#klan-kurma)
2. [Bölge Oluşturma](#bölge-oluşturma)
3. [Üye Yönetimi](#üye-yönetimi)
4. [Rütbe Sistemi](#rütbe-sistemi)
5. [Savaş ve İlan](#savaş-ve-ilan)

---

## 🏰 KLAN KURMA

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
2. **Minimum 10x10**, **Maksimum 150x150** alan seç
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
- Minimum 10x10, Maksimum 150x150

### Adım 4: Kristali Yerleştir

1. Çevrelediğin alanın **içine** gir
2. Eline **Klan Kristali** al
3. İstediğin yere (tercihen ortaya) **sağ tık**
4. **Sonuç**: 
   - Ender Crystal spawn olur
   - Şimşek efekti + TOTEM partikülleri
   - "§a[Klan Adı] klanı kuruldu!" mesajı
   - Çitlerin çevrelediği alan → **Klan Bölgesi**

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
- Maks: 150x150

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
├─────────────┼──────────────────────────────┤
│ GENERAL     │ - Üye davet edebilir        │
│ (Komutan)   │ - Yapı kullanabilir         │
│             │ - Savaş ilan edebilir       │
│             │ - Bölge yönetimi            │
├─────────────┼──────────────────────────────┤
│ MEMBER      │ - İnşaat yapabilir          │
│ (Üye)       │ - Sandık kullanabilir       │
│             │ - Yapılardan faydalanır     │
│             │ - Savaşabilir               │
├─────────────┼──────────────────────────────┤
│ RECRUIT     │ - Sadece gezinebilir        │
│ (Acemi)     │ - Yapı KURAMAZ              │
│             │ - Yapı YIKAMAZ              │
└─────────────┴──────────────────────────────┘
```

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
- Gri partikülermember için)
- Şimşek efekti
- Başarı sesi

---

## ⚔️ SAVAŞ VE İLAN

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

#### **Seviye Koruması**
```
Klan seviyeleri 3'ten fazla fark varsa saldırı YASAK:

Örnek 1:
Klan A (Seviye 5) → Klan B (Seviye 1) = YASAK (5-1=4 > 3)

Örnek 2:
Klan A (Seviye 3) → Klan B (Seviye 2) = İZİNLİ (3-2=1 ≤ 3)
```

**Klan Seviyesi Hesaplama**:
```
Seviye = Yapı Sayısı × Yapı Seviyesi

Örnek:
- 5 yapı × Lv1 = 5 puan
- 2 yapı × Lv3 = 6 puan
Toplam Seviye = 11
```

#### **Savaş Halinde**

**İzin Verilenler**:
- Düşman yapılarına hasar verme
- Düşman klan üyelerini vurma (PvP)
- Düşman bölgesine girme

**Yasak Olanlar**:
- Düşman bölgesinde blok kırma (sadece yapılara hasar)
- Kristali normal yöntemlerle yok etme

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

## 🛡️ ÖZEL KURALLAR

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

Gün 6: İlk Üyeler
→ Davet ritüeli platformu yap
→ Arkadaşlarını davet et

Gün 7: İlk Yapı
→ Savunma yapısı kur
→ Savaşa hazırlan!
```

---

**🎮 Klanını kur, bölgeni genişlet, düşmanlara karşı koy!**
