# 🔋 YENİ BATARYA TARİFLERİ - DETAYLI DÖKÜMAN

## 📋 GENEL BİLGİLER

- **Toplam Tarif Sayısı:** 25
- **Sistem:** Her tarif kendine özel `RecipeChecker` sınıfı ile kontrol edilir
- **Merkez Blok:** Oyuncunun tıklayacağı blok (aktifleştirme noktası)
- **Koordinat Sistemi:** Merkez blok (0,0,0) referans alınır
  - X: Doğu (+), Batı (-)
  - Y: Yukarı (+), Aşağı (-)
  - Z: Güney (+), Kuzey (-)

---

## ⚔️ SEVİYE 1 TARİFLERİ (5 Batarya)

### 1. Yıldırım Asası (Lightning Staff)
- **Seviye:** 1
- **Merkez Blok:** IRON_BLOCK
- **Şekil:** Dikey Kule (3 blok üst üste)
- **Toplam Blok:** 3

```
Yukarıdan Görünüm (Y ekseni):
     ↑
  [IRON]  ← 1 blok yukarı
     |
  [IRON]  ← MERKEZ (tıklama noktası)
     |
  [IRON]  ← 1 blok aşağı
```

**Blok Yerleşimi:**
- Merkez (0,0,0): IRON_BLOCK
- Yukarı (0,1,0): IRON_BLOCK
- Aşağı (0,-1,0): IRON_BLOCK

---

### 2. Cehennem Topu (Hellfire Ball)
- **Seviye:** 1
- **Merkez Blok:** MAGMA_BLOCK
- **Şekil:** Yatay Çizgi (3 blok doğu-batı)
- **Toplam Blok:** 3

```
Yukarıdan Görünüm (Y ekseni):
[MAGMA] ← [MAGMA] → [MAGMA]
  -1       0 (merkez)   +1
```

**Blok Yerleşimi:**
- Merkez (0,0,0): MAGMA_BLOCK
- Doğu (1,0,0): MAGMA_BLOCK
- Batı (-1,0,0): MAGMA_BLOCK

---

### 3. Buz Topu (Ice Ball)
- **Seviye:** 1
- **Merkez Blok:** PACKED_ICE
- **Şekil:** T Şekli
- **Toplam Blok:** 4

```
Yukarıdan Görünüm:
     ↑
  [ICE]  ← 1 blok yukarı
     |
[ICE] ← [ICE] → [ICE]
-1     0 (merkez)  +1
     |
  [ICE]  ← 1 blok güney
```

**Blok Yerleşimi:**
- Merkez (0,0,0): PACKED_ICE
- Yukarı (0,1,0): PACKED_ICE
- Kuzey (0,0,-1): PACKED_ICE
- Güney (0,0,1): PACKED_ICE

---

### 4. Zehir Oku (Poison Arrow)
- **Seviye:** 1
- **Merkez Blok:** EMERALD_BLOCK
- **Şekil:** 2x2 Kare
- **Toplam Blok:** 4

```
Yukarıdan Görünüm:
[EMERALD] [EMERALD]  ← Doğu-Kuzey köşe
    ↑
[EMERALD] [EMERALD]  ← Merkez + Doğu
    (merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): EMERALD_BLOCK
- Doğu (1,0,0): EMERALD_BLOCK
- Kuzey (0,0,-1): EMERALD_BLOCK
- Doğu-Kuzey köşe (1,0,-1): EMERALD_BLOCK

---

### 5. Şok Dalgası (Shock Wave)
- **Seviye:** 1
- **Merkez Blok:** REDSTONE_BLOCK
- **Şekil:** Artı (+) Şekli
- **Toplam Blok:** 5

```
Yukarıdan Görünüm:
     ↑
  [RED]  ← Kuzey
     |
[RED] ← [RED] → [RED]
-1     0 (merkez)  +1
     |
  [RED]  ← Güney
```

**Blok Yerleşimi:**
- Merkez (0,0,0): REDSTONE_BLOCK
- Doğu (1,0,0): REDSTONE_BLOCK
- Batı (-1,0,0): REDSTONE_BLOCK
- Kuzey (0,0,-1): REDSTONE_BLOCK
- Güney (0,0,1): REDSTONE_BLOCK

---

## ⚔️ SEVİYE 2 TARİFLERİ (5 Batarya)

### 6. Çift Ateş Topu (Double Fireball)
- **Seviye:** 2
- **Merkez Blok:** MAGMA_BLOCK
- **Şekil:** Piramit (3x3 taban + 1 üstte)
- **Toplam Blok:** 10

```
Yukarıdan Görünüm (Alt Kat):
[MAGMA][MAGMA][MAGMA]
[MAGMA][MAGMA][MAGMA]  ← 3x3 taban
[MAGMA][MAGMA][MAGMA]
      (merkez)

Yukarıdan Görünüm (Üst Kat - 1 blok yukarı):
        ↑
     [NETHERRACK]
```

**Blok Yerleşimi:**
- Merkez (0,0,0): MAGMA_BLOCK
- 3x3 taban (8 yan blok): MAGMA_BLOCK
  - Doğu (1,0,0), Batı (-1,0,0)
  - Kuzey (0,0,-1), Güney (0,0,1)
  - Doğu-Kuzey (1,0,-1), Doğu-Güney (1,0,1)
  - Batı-Kuzey (-1,0,-1), Batı-Güney (-1,0,1)
- Üstte (0,1,0): NETHERRACK

---

### 7. Zincir Yıldırım (Chain Lightning)
- **Seviye:** 2
- **Merkez Blok:** IRON_BLOCK
- **Şekil:** Yatay Çizgi (5 blok)
- **Toplam Blok:** 5

```
Yukarıdan Görünüm:
[GOLD] ← [IRON] ← [IRON] → [IRON] → [IRON]
  -2       -1       0 (merkez)   +1      +2
```

**Blok Yerleşimi:**
- Merkez (0,0,0): IRON_BLOCK
- Doğu 1 (1,0,0): IRON_BLOCK
- Doğu 2 (2,0,0): IRON_BLOCK
- Batı 1 (-1,0,0): IRON_BLOCK
- Batı 2 (-2,0,0): GOLD_BLOCK

---

### 8. Buz Fırtınası (Ice Storm)
- **Seviye:** 2
- **Merkez Blok:** PACKED_ICE
- **Şekil:** L Şekli (3 yukarı + 2 doğu)
- **Toplam Blok:** 6

```
Yan Görünüm (X ekseni):
[ICE]     [ICE]
[ICE]     [ICE]
[ICE] → [ICE] → [BLUE_ICE]
(merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): PACKED_ICE
- Yukarı 1 (0,1,0): PACKED_ICE
- Yukarı 2 (0,2,0): PACKED_ICE
- Doğu 1 (1,0,0): PACKED_ICE
- Doğu 2 (2,0,0): BLUE_ICE

---

### 9. Asit Yağmuru (Acid Rain)
- **Seviye:** 2
- **Merkez Blok:** EMERALD_BLOCK
- **Şekil:** Çapraz X Şekli
- **Toplam Blok:** 5

```
Yukarıdan Görünüm:
[EMERALD]        [SLIME]
        \      /
         [EMERALD]  ← Merkez
        /      \
[EMERALD]        [EMERALD]
```

**Blok Yerleşimi:**
- Merkez (0,0,0): EMERALD_BLOCK
- Doğu-Güney (1,0,1): EMERALD_BLOCK
- Batı-Kuzey (-1,0,-1): EMERALD_BLOCK
- Doğu-Kuzey (1,0,-1): SLIME_BLOCK
- Batı-Güney (-1,0,1): EMERALD_BLOCK

---

### 10. Elektrik Ağı (Electric Net)
- **Seviye:** 2
- **Merkez Blok:** REDSTONE_BLOCK
- **Şekil:** 3x3 Kare
- **Toplam Blok:** 9

```
Yukarıdan Görünüm:
[RED][RED][RED]
[RED][RED][RED]  ← 3x3 kare
[RED][RED][LAPIS]
      (merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): REDSTONE_BLOCK
- 8 yan blok: REDSTONE_BLOCK
  - Doğu, Batı, Kuzey, Güney
  - Doğu-Kuzey, Doğu-Güney
  - Batı-Kuzey, Batı-Güney
- Batı-Güney köşe (-1,0,1): LAPIS_BLOCK (özel)

---

## ⚔️ SEVİYE 3 TARİFLERİ (5 Batarya)

### 11. Meteor Yağmuru (Meteor Shower)
- **Seviye:** 3
- **Merkez Blok:** OBSIDIAN
- **Şekil:** 2 Katlı Piramit (5x5 alt + 3x3 üst)
- **Toplam Blok:** 34

```
Yan Görünüm:
        ↑
   [OBSIDIAN]  ← 3x3 üst kat (1 blok yukarı)
[OBS][OBS][OBS]
[OBS][OBS][MAGMA]
[OBS][OBS][OBS]

[OBS][OBS][OBS][OBS][OBS]  ← 5x5 alt kat
[OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS]
         (merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): OBSIDIAN
- Alt kat (5x5): OBSIDIAN (24 blok)
- Üst kat (3x3, 1 blok yukarı): OBSIDIAN (8 blok)
- Üst kat Doğu-Güney (1,1,1): MAGMA_BLOCK (özel)

---

### 12. Yıldırım Fırtınası (Lightning Storm)
- **Seviye:** 3
- **Merkez Blok:** IRON_BLOCK
- **Şekil:** H Şekli (yatay + dikey)
- **Toplam Blok:** 9

```
Yan Görünüm:
[IRON]     [IRON]
[DIAMOND]  [IRON]
[IRON] ← [IRON] → [IRON]
(merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): IRON_BLOCK
- Yatay: Doğu (1,0,0), Batı (-1,0,0)
- Dikey: Yukarı 1 (0,1,0), Yukarı 2 (0,2,0) → DIAMOND_BLOCK
- Doğu-Yukarı 1 (1,1,0), Doğu-Yukarı 2 (1,2,0)
- Batı-Yukarı 1 (-1,1,0), Batı-Yukarı 2 (-1,2,0)

---

### 13. Buz Çağı (Ice Age)
- **Seviye:** 3
- **Merkez Blok:** PACKED_ICE
- **Şekil:** Yıldız Şekli (5 uçlu)
- **Toplam Blok:** 6

```
Yukarıdan Görünüm:
     ↑
  [ICE]  ← Kuzey
     |
[ICE] ← [ICE] → [ICE]
-1     0 (merkez)  +1
     |
  [FROSTED_ICE]  ← Güney (özel)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): PACKED_ICE
- Yukarı (0,1,0): PACKED_ICE
- Aşağı (0,-1,0): PACKED_ICE
- Doğu (1,0,0): PACKED_ICE
- Batı (-1,0,0): PACKED_ICE
- Güney (0,0,1): FROSTED_ICE (özel)

---

### 14. Zehir Bombası (Poison Bomb)
- **Seviye:** 3
- **Merkez Blok:** EMERALD_BLOCK
- **Şekil:** Çapraz Kule (X şekli dikey)
- **Toplam Blok:** 9

```
Yan Görünüm (çapraz):
[EMERALD]        [EMERALD]
[EMERALD]        [EMERALD]
[EMERALD]        [EMERALD]
     \              /
      [EMERALD] ← Merkez
     /              \
[EMERALD]        [POISONOUS_POTATO]
[EMERALD]        [EMERALD]
[EMERALD]        [EMERALD]
```

**Blok Yerleşimi:**
- Merkez (0,0,0): EMERALD_BLOCK
- Çapraz bloklar (1-2 yukarı):
  - Doğu-Güney (1,1,1), (1,2,1)
  - Batı-Kuzey (-1,1,-1), (-1,2,-1)
  - Doğu-Kuzey (1,1,-1), (1,2,-1) → POISONOUS_POTATO (özel)
  - Batı-Güney (-1,1,1), (-1,2,1)

---

### 15. Elektrik Fırtınası (Electric Storm)
- **Seviye:** 3
- **Merkez Blok:** REDSTONE_BLOCK
- **Şekil:** Z Şekli (yatay + çapraz)
- **Toplam Blok:** 7

```
Yan Görünüm:
[RED] → [RED] → [RED]
                ↓
            [RED]
            [RED]
[GLOWSTONE] ← [RED]
  -2          -1
         (merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): REDSTONE_BLOCK
- Doğu 1 (1,0,0), Doğu 2 (2,0,0)
- Doğu-Yukarı 1 (2,1,0), Doğu-Yukarı 2 (1,1,0)
- Batı 1 (-1,0,0), Batı 2 (-2,0,0) → GLOWSTONE (özel)

---

## ⚔️ SEVİYE 4 TARİFLERİ (5 Batarya)

### 16. Tesla Kulesi (Tesla Tower)
- **Seviye:** 4
- **Merkez Blok:** COPPER_BLOCK
- **Şekil:** 3 Katlı Kule (her katta 3x3)
- **Toplam Blok:** 27

```
Yan Görünüm:
        ↑
   [COPPER]  ← Üst kat (2 blok yukarı)
[COPPER][COPPER][COPPER]
[COPPER][COPPER][COPPER]
[COPPER][COPPER][COPPER]

   [COPPER]  ← Orta kat (1 blok yukarı)
[COPPER][COPPER][COPPER]
[COPPER][COPPER][REDSTONE]  ← Özel
[COPPER][COPPER][COPPER]

[COPPER][COPPER][COPPER]  ← Alt kat
[COPPER][COPPER][COPPER]
[COPPER][COPPER][COPPER]
         (merkez)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): COPPER_BLOCK
- Alt kat (3x3): COPPER_BLOCK (8 blok)
- Orta kat (3x3, 1 yukarı): COPPER_BLOCK (7 blok) + REDSTONE_BLOCK (1 blok, özel)
- Üst kat (3x3, 2 yukarı): COPPER_BLOCK (8 blok)

---

### 17. Cehennem Ateşi (Hellfire)
- **Seviye:** 4
- **Merkez Blok:** MAGMA_BLOCK
- **Şekil:** Çapraz Spiral (X şekli 3D)
- **Toplam Blok:** 11

```
3D Görünüm:
[MAGMA]        [MAGMA]
[MAGMA]        [MAGMA]
[MAGMA]        [MAGMA]
     \              /
      [MAGMA] ← Merkez
     /              \
[NETHER_STAR]    [MAGMA]
[MAGMA]          [MAGMA]
[MAGMA]          [MAGMA]
```

**Blok Yerleşimi:**
- Merkez (0,0,0): MAGMA_BLOCK
- Çapraz spiral bloklar (0-2 yukarı):
  - Doğu-Güney (1,0,1), (1,1,1), (1,2,1)
  - Batı-Kuzey (-1,0,-1), (-1,1,-1), (-1,2,-1)
  - Doğu-Kuzey (1,0,-1), (1,1,-1)
  - Batı-Güney (-1,0,1) → NETHER_STAR (özel)
  - Batı-Güney yukarı (-1,1,1), (-1,2,1)

---

### 18. Buz Kalesi (Ice Fortress)
- **Seviye:** 4
- **Merkez Blok:** PACKED_ICE
- **Şekil:** Kale Şekli (duvarlar + köşeler)
- **Toplam Blok:** 17

```
Yukarıdan Görünüm (Alt Kat):
[ICE][ICE][ICE][ICE][ICE]
[ICE]           [ICE]
[ICE]   [ICE]   [ICE]  ← Merkez
[ICE]           [ICE]
[ICE][ICE][SNOW][ICE][ICE]  ← Özel köşe

Yukarıdan Görünüm (Üst Kat - 1 blok yukarı):
[ICE][ICE][ICE][ICE]
[ICE]           [ICE]
[ICE]           [ICE]
[ICE][ICE][ICE][ICE]
```

**Blok Yerleşimi:**
- Merkez (0,0,0): PACKED_ICE
- Duvarlar (5x5 çerçeve): PACKED_ICE
- Köşeler: PACKED_ICE (3 köşe) + SNOW_BLOCK (1 köşe, özel)
- Üst kat köşeler (1 yukarı): PACKED_ICE (4 köşe)

---

### 19. Ölüm Bulutu (Death Cloud)
- **Seviye:** 4
- **Merkez Blok:** EMERALD_BLOCK
- **Şekil:** Yıldız Şekli (8 uçlu)
- **Toplam Blok:** 17

```
Yukarıdan Görünüm:
    [EMERALD]
[EMERALD]  [EMERALD]
    \        /
     [EMERALD]  ← Merkez
    /        \
[EMERALD]  [EMERALD]
    [EMERALD]

+ 2 blok uzaklıkta:
[EMERALD]        [EMERALD]
        \      /
         [EMERALD]
        /      \
[EMERALD]        [WITHER_SKULL]  ← Özel
```

**Blok Yerleşimi:**
- Merkez (0,0,0): EMERALD_BLOCK
- 4 yön (1-2 blok): Doğu, Batı, Kuzey, Güney
- Çaprazlar (1-2 blok): Tüm çapraz yönler
- Batı-Güney 2 blok (-2,0,2): WITHER_SKELETON_SKULL (özel)

---

### 20. Elektrik Kalkanı (Electric Shield)
- **Seviye:** 4
- **Merkez Blok:** REDSTONE_BLOCK
- **Şekil:** Kare Halka (içi boş 5x5)
- **Toplam Blok:** 20

```
Yukarıdan Görünüm (Alt Kat):
[RED][RED][RED][RED][RED]
[RED]           [RED]
[RED]   [RED]   [RED]  ← Merkez
[RED]           [RED]
[RED][RED][RED][RED][RED]

Yukarıdan Görünüm (Üst Kat - 1 blok yukarı):
[RED][RED][RED][RED]
[RED]           [RED]
[RED]           [RED]
[RED][RED][END_CRYSTAL][RED]  ← Özel köşe
```

**Blok Yerleşimi:**
- Merkez (0,0,0): REDSTONE_BLOCK
- Dış halka (5x5 çerçeve): REDSTONE_BLOCK
- Üst kat halka (1 yukarı): REDSTONE_BLOCK (7 blok) + END_CRYSTAL (1 köşe, özel)

---

## ⚔️ SEVİYE 5 TARİFLERİ (5 Batarya)

### 21. Kıyamet Reaktörü (Apocalypse Reactor)
- **Seviye:** 5
- **Merkez Blok:** OBSIDIAN (BEDROCK yerine)
- **Şekil:** Büyük Piramit (7x7 taban, 5x5, 3x3, 1 üstte)
- **Toplam Blok:** ~25

```
Yan Görünüm:
        ↑
     [END_CRYSTAL]  ← En üstte özel
        ↑
   [OBSIDIAN]  ← 3x3 üst kat
[OBS][OBS][OBS]

[OBS][OBS][OBS][OBS][OBS]  ← 5x5 orta kat
[OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS]

[OBS][OBS][OBS][OBS][OBS][OBS][OBS]  ← 7x7 taban
[OBS][OBS][OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS][OBS][OBS]
[OBS][OBS][OBS][OBS][OBS][OBS][OBS]
         (merkez)
        ↓
     [BEACON]  ← En altta özel
```

**Blok Yerleşimi:**
- Merkez (0,0,0): OBSIDIAN
- 7x7 taban: OBSIDIAN (en altta)
- 5x5 orta kat: OBSIDIAN
- 3x3 üst kat: OBSIDIAN
- En üstte (0,2,0): END_CRYSTAL (özel)
- En altta (0,-2,0): BEACON (özel)

---

### 22. Lava Tufanı (Lava Tsunami)
- **Seviye:** 5
- **Merkez Blok:** MAGMA_BLOCK (BEDROCK yerine)
- **Şekil:** Yatay Dalga (5x5 yatay düzlem)
- **Toplam Blok:** 26

```
Yukarıdan Görünüm:
[MAGMA][MAGMA][MAGMA][MAGMA][MAGMA]
[MAGMA][MAGMA][MAGMA][MAGMA][MAGMA]
[MAGMA][MAGMA][MAGMA][MAGMA][MAGMA]  ← 5x5 yatay
[MAGMA][MAGMA][MAGMA][MAGMA][MAGMA]
[MAGMA][MAGMA][MAGMA][MAGMA][MAGMA]
         (merkez)
        ↑
     [MAGMA]  ← 1 blok yukarı
        ↓
     [BEACON]  ← 1 blok aşağı (özel)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): MAGMA_BLOCK
- 5x5 yatay düzlem: MAGMA_BLOCK (24 blok)
- Üstte (0,1,0): MAGMA_BLOCK
- Altta (0,-1,0): BEACON (özel)

---

### 23. Boss Katili (Boss Killer)
- **Seviye:** 5
- **Merkez Blok:** NETHERITE_BLOCK (BEDROCK yerine)
- **Şekil:** T Şekli 3D (yatay + dikey)
- **Toplam Blok:** 13

```
Yan Görünüm:
        ↑
     [DRAGON_HEAD]  ← 3 blok yukarı (özel)
        ↑
     [NETHERITE]
        ↑
     [NETHERITE]
[NETHERITE] ← [NETHERITE] → [NETHERITE]
     (merkez)
        ↓
     [NETHERITE]
        ↓
     [NETHERITE]
        ↓
     [BEACON]  ← 3 blok aşağı (özel)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): NETHERITE_BLOCK
- Yatay T: Doğu 1-2, Batı 1-2, Kuzey 1-2
- Dikey T: Yukarı 1-2, Aşağı 1-2
- En üstte (0,3,0): DRAGON_HEAD (özel)
- En altta (0,-3,0): BEACON (özel)

---

### 24. Alan Yok Edici (Area Destroyer)
- **Seviye:** 5
- **Merkez Blok:** ANVIL (BEDROCK yerine)
- **Şekil:** Büyük Kare (7x7 düzlem)
- **Toplam Blok:** 50

```
Yukarıdan Görünüm:
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]  ← 7x7 kare
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]
[ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL][ANVIL]
         (merkez)
        ↑
     [ANVIL]  ← 1 blok yukarı
        ↓
     [BEACON]  ← 1 blok aşağı (özel)
```

**Blok Yerleşimi:**
- Merkez (0,0,0): ANVIL
- 7x7 kare: ANVIL (48 blok)
- Üstte (0,1,0): ANVIL
- Altta (0,-1,0): BEACON (özel)

---

### 25. Dağ Yok Edici (Mountain Destroyer)
- **Seviye:** 5
- **Merkez Blok:** NETHER_STAR (BEDROCK yerine)
- **Şekil:** Çapraz X Şekli 3D (her yönde 5 blok)
- **Toplam Blok:** 21

```
Yukarıdan Görünüm:
[NETHER]              [NETHER]
  [NETHER]          [NETHER]
    [NETHER]      [NETHER]
      [NETHER]  [NETHER]
        [NETHER] ← Merkez
      [NETHER]  [NETHER]
    [NETHER]      [NETHER]
  [NETHER]          [NETHER]
[NETHER]              [NETHER]

+ Üstte ve altta özel bloklar
```

**Blok Yerleşimi:**
- Merkez (0,0,0): NETHER_STAR
- Çapraz X şekli (1-4 blok uzaklıkta):
  - Doğu-Güney: (1,0,1), (2,0,2), (3,0,3), (4,0,4)
  - Batı-Kuzey: (-1,0,-1), (-2,0,-2), (-3,0,-3), (-4,0,-4)
  - Doğu-Kuzey: (1,0,-1), (2,0,-2), (3,0,-3), (4,0,-4)
  - Batı-Güney: (-1,0,1), (-2,0,2), (-3,0,3), (-4,0,4)
- Üstte (0,1,0): NETHER_STAR
- Altta (0,-1,0): BEACON (özel)

---

## 📊 ÖZET TABLO

| # | Batarya Adı | Seviye | Merkez Blok | Şekil | Toplam Blok |
|---|-------------|--------|-------------|-------|-------------|
| 1 | Yıldırım Asası | 1 | IRON_BLOCK | Dikey Kule | 3 |
| 2 | Cehennem Topu | 1 | MAGMA_BLOCK | Yatay Çizgi | 3 |
| 3 | Buz Topu | 1 | PACKED_ICE | T Şekli | 4 |
| 4 | Zehir Oku | 1 | EMERALD_BLOCK | 2x2 Kare | 4 |
| 5 | Şok Dalgası | 1 | REDSTONE_BLOCK | Artı (+) | 5 |
| 6 | Çift Ateş Topu | 2 | MAGMA_BLOCK | Piramit | 10 |
| 7 | Zincir Yıldırım | 2 | IRON_BLOCK | Yatay Çizgi | 5 |
| 8 | Buz Fırtınası | 2 | PACKED_ICE | L Şekli | 6 |
| 9 | Asit Yağmuru | 2 | EMERALD_BLOCK | Çapraz X | 5 |
| 10 | Elektrik Ağı | 2 | REDSTONE_BLOCK | 3x3 Kare | 9 |
| 11 | Meteor Yağmuru | 3 | OBSIDIAN | 2 Katlı Piramit | 34 |
| 12 | Yıldırım Fırtınası | 3 | IRON_BLOCK | H Şekli | 9 |
| 13 | Buz Çağı | 3 | PACKED_ICE | Yıldız (5 uçlu) | 6 |
| 14 | Zehir Bombası | 3 | EMERALD_BLOCK | Çapraz Kule | 9 |
| 15 | Elektrik Fırtınası | 3 | REDSTONE_BLOCK | Z Şekli | 7 |
| 16 | Tesla Kulesi | 4 | COPPER_BLOCK | 3 Katlı Kule | 27 |
| 17 | Cehennem Ateşi | 4 | MAGMA_BLOCK | Çapraz Spiral | 11 |
| 18 | Buz Kalesi | 4 | PACKED_ICE | Kale Şekli | 17 |
| 19 | Ölüm Bulutu | 4 | EMERALD_BLOCK | Yıldız (8 uçlu) | 17 |
| 20 | Elektrik Kalkanı | 4 | REDSTONE_BLOCK | Kare Halka | 20 |
| 21 | Kıyamet Reaktörü | 5 | OBSIDIAN | Büyük Piramit | ~25 |
| 22 | Lava Tufanı | 5 | MAGMA_BLOCK | 5x5 Yatay | 26 |
| 23 | Boss Katili | 5 | NETHERITE_BLOCK | T Şekli 3D | 13 |
| 24 | Alan Yok Edici | 5 | ANVIL | 7x7 Kare | 50 |
| 25 | Dağ Yok Edici | 5 | NETHER_STAR | Çapraz X 3D | 21 |

---

## 🔧 TEKNİK NOTLAR

1. **Koordinat Sistemi:**
   - Merkez blok her zaman (0,0,0)
   - X: Doğu (+), Batı (-)
   - Y: Yukarı (+), Aşağı (-)
   - Z: Güney (+), Kuzey (-)

2. **Özel Bloklar:**
   - Seviye 5 bataryalarda genellikle üstte ve altta özel bloklar var
   - Özel bloklar tarifin benzersizliğini sağlar

3. **BEDROCK Kullanımı:**
   - Seviye 5 bataryalarda BEDROCK yerine farklı bloklar kullanılıyor:
     - Kıyamet Reaktörü: OBSIDIAN
     - Lava Tufanı: MAGMA_BLOCK
     - Boss Katili: NETHERITE_BLOCK
     - Alan Yok Edici: ANVIL
     - Dağ Yok Edici: NETHER_STAR

4. **Tarif Değişikliği:**
   - Her tarif `RecipeChecker` interface'ini implement eder
   - `checkRecipe()` metodu merkez bloktan başlayarak kontrol yapar
   - `getBatteryName()` metodu batarya ismini döndürür
   - `getLevel()` metodu seviyeyi döndürür

---

## 📝 KULLANIM NOTLARI

- Oyuncu merkez bloğa (tıklama noktası) sağ tıklayarak bataryayı aktif eder
- Shift + Sağ tık ile yükleme yapılır
- Tüm bloklar doğru yerleştirilmiş olmalı
- Seviye 5 bataryalar için DARK_MATTER yakıt zorunlu

