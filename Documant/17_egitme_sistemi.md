# STRATOCRAFT - EĞİTME SİSTEMİ

## 🐾 Eğitme Sistemi Nedir?

Stratocraft'ta **her özel isimli canlı eğitilebilir**! Ritüel tabanlı sistem ile canlıları eğit, sahiplen, binebilir hale getir.

---

## 📋 İÇİNDEKİLER

1. [Temel Mekanikler](#temel-mekanikler)
2. [Zorluk Seviyesi Ritüelleri](#zorluk-seviyesi-ritüelleri)
3. [Boss Eğitme Ritüelleri](#boss-eğitme-ritüelleri)
4. [Binilebilir Canlılar](#binilebilir-canlılar)
5. [Sahiplik ve Paylaşım](#sahiplik-ve-paylaşım)
6. [Ustalık Güç Sistemi](#ustalık-güç-sistemi) ⭐ YENİ

---

## ⚙️ TEMEL MEKANİKLER

### Eğitilebilir Canlılar

**Kural**: Özel isimli tüm canlılar eğitilebilir!

```
Eğitilebilir:
✓ Tüm özel moblar (Goblin, Ork, Troll, vb.)
✓ Tüm bosslar (Ejderha, Phoenix, Hydra, vb.)
✓ İsimlendirilmiş canlılar

Eğitilemez:
❌ İsimsiz normal moblar
❌ Zaten eğitilmiş canlılar
```

---

### Eğitme Süreci

**Adımlar**:
```
1. Canlının zorluk seviyesini belirle (1-5)
   → Merkeze yakın = Seviye 1
   → Merkeze uzak = Seviye 5

2. Ritüel platformunu kur (seviyeye göre)

3. Canlıyı platformun üzerine getir

4. Eline aktivasyon itemini al

5. SHIFT + SAĞ TIK (canlıya)

6. SONUÇ:
   - Canlı eğitilir
   - Cinsiyet belirlenir (♂/♀)
   - Parıldama efekti
   - Sahibini takip eder
```

**Cooldown**: 30 saniye (ritüel başına)

---

### Eğitilmiş Canlı Özellikleri

**Görsel Değişiklikler**:
```
İsim: "Goblin ♂ [Eğitilmiş]"
Efekt: Glowing (parıldama)
Renk: Mavi (♂ Erkek) / Pembe (♀ Dişi)
```

**Davranış**:
```
- Sahibini takip eder
- Klan üyeleri kullanabilir
- Shift+Sağ tık ile takip hedefi değiştir
- Binilebilir ise binme mümkün
```

---

## 🎯 ZORLUK SEVİYESİ RİTÜELLERİ

**ÖNEMLİ DEĞİŞİKLİK**: Artık tüm eğitim ritüellerinde **Eğitim Çekirdeği** kullanılıyor! Merkez bloğa Eğitim Çekirdeği yerleştirilir ve ritüel deseni çekirdeğin altına yapılır.

### Eğitim Çekirdeği Nedir?

**Eğitim Çekirdeği** (`TAMING_CORE`), tüm eğitim ritüellerinin merkezinde bulunan özel bir bloktur. Bu çekirdek:
- Admin komutu ile verilebilir: `/scadmin give tool taming_core`
- Yerleştirildiğinde `BEACON` bloğu olarak görünür
- Ritüel deseni çekirdeğin **altına** (1 blok aşağıya) yapılır
- Aktivasyon itemi ile çekirdeğe sağ tıklanarak ritüel aktifleştirilir

---

### Seviye 1: Basit Canlılar (0-200 blok)

**Platform**: 3x3 Dirt/Grass Block + Merkez Eğitim Çekirdeği

```
[D][G][D]
[G][C][G]    D = Dirt
[D][G][D]    G = Grass Block
             C = Eğitim Çekirdeği (BEACON bloğu)
```

**Aktivasyon İtemi**: Wheat (Buğday)

**Eğitilebilir Canlılar**: Goblin, Ork, Kurt Adam

**Admin Komutu ile Otomatik Yapı**: `/scadmin tame build 1`

---

### Seviye 2: Orta Canlılar (200-1000 blok)

**Platform**: 3x3 Cobblestone + Merkez Eğitim Çekirdeği

```
[C][C][C]
[C][E][C]    C = Cobblestone
[C][C][C]    E = Eğitim Çekirdeği (BEACON bloğu)
```

**Aktivasyon İtemi**: Bread (Ekmek) - **Güncellendi!**

**Eğitilebilir Canlılar**: İskelet Şövalye, Karanlık Büyücü, Dev Örümcek

**Admin Komutu ile Otomatik Yapı**: `/scadmin tame build 2`

---

### Seviye 3: İleri Canlılar (1000-3000 blok)

**Platform**: 5x5 Stone Bricks + Merkez Eğitim Çekirdeği

```
[B][B][B][B][B]
[B][ ][ ][ ][B]
[B][ ][E][ ][B]    B = Stone Bricks
[B][ ][ ][ ][B]    E = Eğitim Çekirdeği (BEACON bloğu)
[B][B][B][B][B]    [ ] = Boş
```

**Aktivasyon İtemi**: Golden Apple (Altın Elma)

**Eğitilebilir Canlılar**: Minotaur, Harpy, Basilisk

**Admin Komutu ile Otomatik Yapı**: `/scadmin tame build 3`

---

### Seviye 4: Güçlü Canlılar (3000-5000 blok)

**Platform**: 5x5 Obsidian + Merkez Eğitim Çekirdeği

```
[O][O][O][O][O]
[O][ ][ ][ ][O]
[O][ ][E][ ][O]    O = Obsidian
[O][ ][ ][ ][O]    E = Eğitim Çekirdeği (BEACON bloğu)
[O][O][O][O][O]    [ ] = Boş
```

**Aktivasyon İtemi**: Enchanted Golden Apple (Büyülü Altın Elma)

**Eğitilebilir Canlılar**: Griffin, Wraith, Lich

**Admin Komutu ile Otomatik Yapı**: `/scadmin tame build 4`

---

### Seviye 5: Efsanevi Canlılar (5000+ blok)

**Platform**: 7x7 Bedrock + Merkez Eğitim Çekirdeği

```
[R][R][R][R][R][R][R]
[R][ ][ ][ ][ ][ ][R]
[R][ ][ ][ ][ ][ ][R]
[R][ ][ ][E][ ][ ][R]    R = Bedrock
[R][ ][ ][ ][ ][ ][R]    E = Eğitim Çekirdeği (BEACON bloğu)
[R][ ][ ][ ][ ][ ][R]    [ ] = Boş
[R][R][R][R][R][R][R]
```

**Aktivasyon İtemi**: Nether Star (Nether Yıldızı)

**Eğitilebilir Canlılar**: Kraken, Behemoth, Legendary Dragon

**Admin Komutu ile Otomatik Yapı**: `/scadmin tame build 5`

---

## 👑 BOSS EĞİTME RİTÜELLERİ

Bosslar özel ritüeller gerektirir!

### 1. Goblin Kralı

**Platform**: 3x3 Gold Block + Merkez Hay Block

```
[G][G][G]
[G][H][G]    G = Gold Block
[G][G][G]    H = Hay Block
```

**Aktivasyon İtemi**: Rotten Flesh (Çürük Et)

---

### 2. Ork Şefi

**Platform**: 3x3 Iron Block (Tam dolu)

```
[I][I][I]
[I][I][I]    I = Iron Block
[I][I][I]
```

**Aktivasyon İtemi**: Iron Sword (Demir Kılıç)

---

### 3. Troll Kralı

**Platform**: 3x3 Diamond Block + Merkez Stone

```
[D][D][D]
[D][S][D]    D = Diamond Block
[D][D][D]    S = Stone
```

**Aktivasyon İtemi**: Stone Axe (Taş Balta)

---

### 4. Ejderha

**Platform**: 5x5 Emerald Block + Merkez Dragon Egg

```
[E][E][E][E][E]
[E][ ][ ][ ][E]
[E][ ][D][ ][E]    E = Emerald Block
[E][ ][ ][ ][E]    D = Dragon Egg
[E][E][E][E][E]
```

**Aktivasyon İtemi**: Dragon Egg (Ejderha Yumurtası)

---

### 5. T-Rex

**Platform**: 5x5 Gold Block + Merkez Bone Block

```
[G][G][G][G][G]
[G][ ][ ][ ][G]
[G][ ][B][ ][G]    G = Gold Block
[G][ ][ ][ ][G]    B = Bone Block
[G][G][G][G][G]
```

**Aktivasyon İtemi**: Bone (Kemik)

---

### 6. Cyclops (Tek Gözlü Dev)

**Platform**: 5x5 Emerald Block + Merkez End Stone

```
[E][E][E][E][E]
[E][ ][ ][ ][E]
[E][ ][S][ ][E]    E = Emerald Block
[E][ ][ ][ ][E]    S = End Stone
[E][E][E][E][E]
```

**Aktivasyon İtemi**: Ender Eye (Ender Gözü)

---

### 7. Titan Golem

**Platform**: 7x7 Netherite Block + Merkez Beacon

```
[N][N][N][N][N][N][N]
[N][ ][ ][ ][ ][ ][N]
[N][ ][ ][ ][ ][ ][N]
[N][ ][ ][B][ ][ ][N]    N = Netherite Block
[N][ ][ ][ ][ ][ ][N]    B = Beacon
[N][ ][ ][ ][ ][ ][N]
[N][N][N][N][N][N][N]
```

**Aktivasyon İtemi**: Nether Star (Nether Yıldızı)

---

### 8. Cehennem Ejderi

**Platform**: 7x7 Netherrack + Merkez Magma Block

```
[R][R][R][R][R][R][R]
[R][ ][ ][ ][ ][ ][R]
[R][ ][ ][ ][ ][ ][R]
[R][ ][ ][M][ ][ ][R]    R = Netherrack
[R][ ][ ][ ][ ][ ][R]    M = Magma Block
[R][ ][ ][ ][ ][ ][R]
[R][R][R][R][R][R][R]
```

**Aktivasyon İtemi**: Blaze Rod (Blaze Çubuğu)

---

### 9. Hydra

**Platform**: 7x7 Prismarine + Merkez Conduit

```
[P][P][P][P][P][P][P]
[P][ ][ ][ ][ ][ ][P]
[P][ ][ ][ ][ ][ ][P]
[P][ ][ ][C][ ][ ][P]    P = Prismarine
[P][ ][ ][ ][ ][ ][P]    C = Conduit
[P][ ][ ][ ][ ][ ][P]
[P][P][P][P][P][P][P]
```

**Aktivasyon İtemi**: Heart of the Sea (Deniz Kalbi)

---

### 10. Khaos Tanrısı

**Platform**: 9x9 Bedrock + Merkez Beacon

```
[B][B][B][B][B][B][B][B][B]
[B][ ][ ][ ][ ][ ][ ][ ][B]
[B][ ][ ][ ][ ][ ][ ][ ][B]
[B][ ][ ][ ][ ][ ][ ][ ][B]
[B][ ][ ][ ][C][ ][ ][ ][B]    B = Bedrock
[B][ ][ ][ ][ ][ ][ ][ ][B]    C = Beacon
[B][ ][ ][ ][ ][ ][ ][ ][B]
[B][ ][ ][ ][ ][ ][ ][ ][B]
[B][B][B][B][B][B][B][B][B]
```

**Aktivasyon İtemi**: Nether Star (Nether Yıldızı)

---

## 🏇 BİNİLEBİLİR CANLILAR

### Binme Mekaniği

**Nasıl Binilir?**:
```
1. Canlıyı eğit
2. Canlının yanına git
3. SHIFT + SAĞ TIK (canlıya)
4. Binilir!
```

**Kontrol**:
```
W/A/S/D = Hareket
Space = Zıpla / Uç
Shift = İn (uçarken)
Sol Tık = İn (yerden)
```

---

### Binilebilir Canlı Listesi

| Canlı | Özellik | Hız |
|-------|---------|-----|
| **Ejderha** | Uçar, Ateş direnci | Çok Hızlı |
| **T-Rex** | Yüksek hasar | Orta |
| **Griffin** | Uçar, Hızlı | Hızlı |
| **Savaş Ayısı** | Dayanıklılık buff | Yavaş |
| **Phoenix** | Uçar, Ateş buff | Hızlı |
| **Wyvern** | Uçar, Çok hızlı | Çok Hızlı |
| **Cehennem Ejderi** | Uçar, Ateş hasar | Çok Hızlı |
| **Hydra** | Uçar, Çok güçlü | Orta |
| **Khaos Tanrısı** | Uçar, Efsanevi | Çok Hızlı |

---

## 👥 SAHİPLİK VE PAYLAŞIM

### Sahiplik Sistemi

**Eğiten Kişi = Sahip**:
```
- Canlıyı kontrol edebilir
- Takip hedefini değiştirebilir
- Binebilir
- Çiftleştirebilir
```

---

### Klan Paylaşımı

**Aynı Klan Üyeleri**:
```
✓ Canlıyı kullanabilir
✓ Binebilir
✓ Takip hedefini değiştirebilir
✓ Çiftleştirebilir

❌ Sahipliği değiştiremez
```

---

### Takip Hedefi Değiştirme

**Nasıl Yapılır?**:
```
1. Eğitilmiş canlının yanına git
2. SHIFT + SAĞ TIK (canlıya)
3. Artık seni takip eder

Not: Sadece sahip veya klan üyesi yapabilir
```

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Cinsiyet Sistemi**: Her eğitilen canlıya rastgele cinsiyet atanır (♂/♀)
2. **Cooldown**: Her ritüel 30 saniye cooldown'a sahip
3. **Platform Kontrolü**: Platform tam olarak doğru olmalı, yoksa ritüel çalışmaz
4. **Boss Ritüelleri**: Bosslar için özel ritüeller gerekir
5. **Klan Paylaşımı**: Klan üyeleri eğitilmiş canlıları kullanabilir
6. **Binilebilirlik**: Sadece belirli canlılar binilebilir
7. **Ölüm**: Canlı ölürse eğitim kaydı silinir

---

## 🎯 HIZLI EĞİTME REHBERİ

### Basit Canlı (Seviye 1)

```
1. 3x3 Cobblestone + Merkez Hay Bale kur
2. Canlıyı üzerine getir
3. Eline Wheat al
4. Shift + Sağ tık (canlıya)
5. Eğitildi!
```

### Boss Eğitme (Ejderha)

```
1. 5x5 Emerald Block + Merkez Dragon Egg kur
2. Ejderhayı üzerine getir
3. Eline Dragon Egg al
4. Shift + Sağ tık (ejderhaya)
5. Eğitildi! Artık binebilirsin!
```

---

---

## 🎓 USTALIK GÜÇ SİSTEMİ (YENİ)

### ✅ Ritüel Ustalığı Güç Kazanma

**Ritüelleri kullandıkça ustalık kazanırsın ve güçlenirsin!**

Her ritüel için **%100 üzerine çıkış** yaptığında, o ritüel için bonus güç kazanırsın.

### Nasıl Çalışır?

**1. Ustalık Hesaplama:**
```
Her ritüel kullanımı = Ustalık artışı
100 kullanım = %100 ustalık
200 kullanım = %200 ustalık
```

**2. Güç Hesaplama:**
```
Ustalık Gücü = 150 × (Ustalık% / 100)^1.4

Örnekler:
- %150 ustalık: 150 × (1.5)^1.4 ≈ 250 puan
- %200 ustalık: 150 × (2.0)^1.4 ≈ 400 puan
- %300 ustalık: 150 × (3.0)^1.4 ≈ 700 puan
```

**3. Desteklenen Ritüeller:**
- ✅ Tüm batarya tipleri
- ✅ Tüm ritüel tipleri
- ✅ Her ritüel için ayrı ustalık takibi

### Önemli Notlar

**%100 Altı Ustalık:**
- ❌ %100 altı ustalık güç vermez
- ✅ Sadece %100 üzerine çıkış güç verir

**Oyuncu Gücüne Etkisi:**
- Ustalık gücü, oyuncunun **Progression Power**'ına eklenir
- Toplam SGP hesaplamasında kullanılır
- Felaket zorluğunu etkiler

### Komutlar

**Ustalık gücünü görmek için:**
```
/sgp components
```

**Toplam gücü görmek için:**
```
/sgp
```

### Config Ayarları

Ustalık güç değerleri `config.yml` dosyasından ayarlanabilir:

```yaml
clan-power-system:
  mastery:
    base-power: 150
    exponent: 1.4
```

---

**🎮 Canlıları eğit, ordu kur, düşmanları ez!**
