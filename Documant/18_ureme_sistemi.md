# STRATOCRAFT - ÜREME SİSTEMİ

## 🐣 Üreme Sistemi Nedir?

Eğitilmiş canlıları çiftleştirerek yavru üret! **Memeli canlılar** direkt yavru doğurur, **yumurtlayan canlılar** yumurta bırakır.

---

## 📋 İÇİNDEKİLER

1. [Temel Mekanikler](#temel-mekanikler)
2. [Çiftleştirme Tesisleri](#çiftleştirme-tesisleri)
3. [Doğal Çiftleştirme](#doğal-çiftleştirme)
4. [Yumurta Sistemi](#yumurta-sistemi)

---

## ⚙️ TEMEL MEKANİKLER

### Çiftleştirme Kuralları

**Gereksinimler**:
```
✓ 1 Dişi canlı (♀)
✓ 1 Erkek canlı (♂)
✓ Her ikisi de eğitilmiş olmalı
✓ Aynı sahibe ait olmalı
✓ Aynı tür olmalı
```

---

### Memeli vs Yumurtlayan

**Memeli Canlılar** (Direkt Yavru):
```
- Ork
- Goblin
- Troll
- Minotaur
- Savaş Ayısı
- Kurt Adam
```

**Yumurtlayan Canlılar** (Yumurta):
```
- Ejderha
- Griffin
- Phoenix
- Wyvern
- Hydra
- Harpy
- T-Rex
```

---

## 🏭 ÇİFTLEŞTİRME TESİSLERİ

**YENİ ÖZELLİK**: Artık üreme tesisleri **seviyeli** ve **Üreme Çekirdeği** ile çalışıyor!

### Üreme Çekirdeği Nedir?

**Üreme Çekirdeği** (`BREEDING_CORE`), tüm üreme tesislerinin merkezinde bulunan özel bir bloktur. Bu çekirdek:
- Admin komutu ile verilebilir: `/scadmin give tool breeding_core`
- Yerleştirildiğinde `BEACON` bloğu olarak görünür
- Tesis içindeki canlıları otomatik bulur ve çiftleştirir
- Aktivasyon: Çekirdeğe sağ tıklayarak aktifleştirilir

### Tesis Oluşturma

**Adımlar**:
```
1. Admin komutu ile tesis yapısını oluştur: /scadmin breeding build <seviye>
2. Üreme Çekirdeği otomatik yerleştirilir (merkeze)
3. Tesis içine 1 dişi + 1 erkek canlı getir (aynı tür)
4. Üreme Çekirdeği'ne sağ tıkla
5. Sistem otomatik olarak uygun çifti bulur ve çiftleştirme başlar
```

**Önemli**: Eğer tesis içinde 2'den fazla canlı varsa, sistem rastgele bir erkek ve bir dişi seçer.

---

### Seviye 1 Tesis

**Süre**: 1 gün (24 saat)

**Platform**: 3x3 Hay Bale + Merkez Üreme Çekirdeği

```
[H][H][H]
[H][C][H]    H = Hay Bale
[H][H][H]    C = Üreme Çekirdeği (BEACON bloğu)
```

**Admin Komutu**: `/scadmin breeding build 1`

---

### Seviye 2 Tesis

**Süre**: 2 gün (48 saat)

**Platform**: 5x5 Hay Bale + Merkez Üreme Çekirdeği

```
[H][H][H][H][H]
[H][ ][ ][ ][H]
[H][ ][C][ ][H]    H = Hay Bale
[H][ ][ ][ ][H]    C = Üreme Çekirdeği (BEACON bloğu)
[H][H][H][H][H]    [ ] = Boş
```

**Admin Komutu**: `/scadmin breeding build 2`

---

### Seviye 3 Tesis

**Süre**: 3 gün (72 saat)

**Platform**: 7x7 Hay Bale + Merkez Üreme Çekirdeği

```
[H][H][H][H][H][H][H]
[H][ ][ ][ ][ ][ ][H]
[H][ ][ ][ ][ ][ ][H]
[H][ ][ ][C][ ][ ][H]    H = Hay Bale
[H][ ][ ][ ][ ][ ][H]    C = Üreme Çekirdeği (BEACON bloğu)
[H][ ][ ][ ][ ][ ][H]    [ ] = Boş
[H][H][H][H][H][H][H]
```

**Admin Komutu**: `/scadmin breeding build 3`

---

### Seviye 4 Tesis

**Süre**: 4 gün (96 saat)

**Platform**: 9x9 Hay Bale + Merkez Üreme Çekirdeği

**Admin Komutu**: `/scadmin breeding build 4`

---

### Seviye 5 Tesis

**Süre**: 5 gün (120 saat)

**Platform**: 11x11 Hay Bale + Merkez Üreme Çekirdeği

**Admin Komutu**: `/scadmin breeding build 5`

---

## 🌾 DOĞAL ÇİFTLEŞTİRME

Tesis olmadan da çiftleştirme yapılabilir!

### Nasıl Yapılır?

**Adımlar**:
```
1. 1 dişi + 1 erkek canlıyı yan yana getir
2. Her ikisine de yiyecek ver (sağ tık)
3. Kalp partikülleri görünür
4. 1 dakika bekle
5. Yavru doğar!
```

**Süre**: 1 dakika (60 saniye)

**Avantaj**: Hızlı
**Dezavantaj**: Manuel işlem gerektirir

---

## 🥚 YUMURTA SİSTEMİ

Yumurtlayan canlılar için özel mekanik!

### Yumurta Bırakma

**Çiftleştirme Sonrası**:
```
1. Dişi canlı yumurta bırakır
2. Yumurta görünümü: Kaplumbağa (baby)
3. İsim: "[Canlı İsmi] Yumurtası"
4. Yumurta büyümeye başlar
```

---

### Yumurta Çatlama

**Süreç**:
```
1. Yumurta zamanla büyür (Minecraft kaplumbağa mekaniği)
2. Belirli bir yaşa ulaşınca çatlar
3. İçinden yavru çıkar
4. Yavru otomatik eğitilmiş olur
5. Sahip: Yumurtanın sahibi
```

**Süre**: Minecraft'ın doğal kaplumbağa büyüme süresi

---

## 👶 YAVRU ÖZELLİKLERİ

### Otomatik Eğitim

**Doğan Yavru**:
```
✓ Otomatik eğitilmiş
✓ Sahip: Anne-babanın sahibi
✓ Rastgele cinsiyet (♂/♀)
✓ İsim: "[Parent İsmi] Yavrusu"
✓ Glowing efekti
```

---

### İstatistikler

**Yavru Özellikleri**:
```
- Can: Parent ile aynı
- Hasar: Parent ile aynı
- Yetenekler: Parent ile aynı
- Binilebilirlik: Parent ile aynı
```

---

## 🔍 CİNSİYET TARAYICISI

**YENİ ÖZELLİK**: Canlıların cinsiyetini görmek için **Cinsiyet Tarayıcısı** kullanılabilir!

### Cinsiyet Tarayıcısı Nedir?

**Cinsiyet Tarayıcısı** (`GENDER_SCANNER`), eğitilmiş canlıların cinsiyetini gösteren özel bir eşyadır.

**Kullanım**:
```
1. Cinsiyet Tarayıcısı'nı eline al: /scadmin give tool gender_scanner
2. Eğitilmiş bir canlıya sağ tıkla
3. Canlının cinsiyeti chat'te gösterilir: "♂ Erkek" veya "♀ Dişi"
```

**Görünüm**: Spyglass (Dürbün) benzeri

---

## ⚡ ADMIN ÖZELLİKLERİ

### Anında Tamamlama

**Komut**: `/scadmin breeding complete <location>`

**Etki**:
```
- Çiftleştirme süresini atlar
- Anında yavru doğar
- Test ve hızlı üretim için
```

### Otomatik Tesis Yapımı

**Komut**: `/scadmin breeding build <seviye>`

**Seviyeler**: 1-5

**Etki**:
```
- Tesis yapısını otomatik oluşturur
- Üreme Çekirdeği'ni merkeze yerleştirir
- Tesis seviyesini ayarlar
```

### Eşya Verme

**Komutlar**:
```
/scadmin give tool breeding_core    → Üreme Çekirdeği
/scadmin give tool gender_scanner   → Cinsiyet Tarayıcısı
```

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Cinsiyet Kontrolü**: Mutlaka 1 dişi + 1 erkek olmalı
2. **Sahiplik**: Her ikisi de aynı kişiye ait olmalı
3. **Eğitim**: Sadece eğitilmiş canlılar çiftleştirilebilir
4. **Yiyecek**: Tesiste en az 3 yiyecek bloğu gerekli
5. **Süre**: Tesis seviyesi arttıkça süre uzar
6. **Yumurta**: Yumurtlayan canlılar direkt yavru doğurmaz
7. **Otomatik Eğitim**: Doğan yavru otomatik eğitilmiş olur

---

## 🎯 HIZLI ÜRETİM REHBERİ

### Tesis ile Çiftleştirme

```
1. Seviye 1 tesis oluştur
2. 3x3 alana 3+ yiyecek bloğu koy
3. 1 dişi canlı getir (tesise ekle)
4. 1 erkek canlı getir (tesise ekle)
5. Çiftleştirme otomatik başlar
6. 1 gün bekle
7. Yavru doğar!
```

### Doğal Çiftleştirme (Hızlı)

```
1. 1 dişi + 1 erkek yan yana getir
2. Her ikisine yiyecek ver
3. 1 dakika bekle
4. Yavru doğar!
```

### Yumurta Çatlama

```
1. Yumurtlayan canlıları çiftleştir
2. Yumurta bırakılır
3. Yumurtayı bekle (doğal büyüme)
4. Çatladığında yavru çıkar
5. Yavru otomatik eğitilmiş!
```

---

**🎮 Canlıları çiftleştir, ordu büyüt, dünyaya hükmet!**
