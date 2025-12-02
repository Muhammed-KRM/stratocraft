# STRATOCRAFT - RİTÜEL SİSTEMİ

## 🔥 Ritüel Sistemi Nedir?

Ritüeller, Stratocraft'ta **fiziksel blok düzenekleriyle** yapılan özel etkileşimlerdir. **Hiçbir komut kullanılmaz**, her şey bloklarla yapılır!

---

## 📋 İÇİNDEKİLER

1. [Klan Ritüelleri](#klan-ritüelleri)
2. [Üye Yönetim Ritüelleri](#üye-yönetim-ritüelleri)
3. [Yapi Ritüelleri](#yapi-ritüelleri)
4. [Savaş Ritüelleri](#savaş-ritüelleri)

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

**Cooldown**: 10 saniye

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

**🎮 Ritüellerle gücü elde et, fiziksel dünyayı şekillendir!**
