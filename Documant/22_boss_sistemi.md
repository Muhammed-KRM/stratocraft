# 🐉 Boss Sistemi - Kapsamlı Rehber

## 📋 İçindekiler

1. [Genel Bakış](#genel-bakış)
2. [Boss Çağırma Sistemi](#boss-çağırma-sistemi)
3. [Boss Listesi ve Detayları](#boss-listesi-ve-detayları)
4. [Boss Yetenekleri](#boss-yetenekleri)
5. [Faz Sistemi](#faz-sistemi)
6. [Zayıf Noktalar ve Zayıflıklar](#zayıf-noktalar-ve-zayıflıklar)
7. [Arena Transformasyon Sistemi](#arena-transformasyon-sistemi)
8. [Boss Dropları](#boss-dropları)
9. [BossBar Sistemi](#bossbar-sistemi)
10. [Doğada Spawn](#doğada-spawn)
11. [Admin Komutları](#admin-komutları)
12. [Strateji İpuçları](#strateji-ipuçları)

---

## 📋 Genel Bakış

Stratocraft'ta **13 farklı boss** bulunmaktadır. Her boss:
- **Ritüel ile çağrılabilir** (özel blok deseni + Çağırma Çekirdeği + aktivasyon itemi)
- **Doğada nadiren spawn olabilir** (zorluk seviyesine göre)
- **Özel yetenekler** kullanır (ateş, yıldırım, patlama, blok fırlatma, zehir vb.)
- **Zayıf noktaları** olabilir (güçlü bosslar için)
- **Faz sistemi** olabilir (güçlü bosslar için 2-3 faz)
- **Arena transformasyonu** yapar (güçlü bosslar için)

### Boss Seviyeleri

- **Seviye 1:** Goblin Kralı, Ork Şefi
- **Seviye 2:** Troll Kralı
- **Seviye 3:** Ejderha, T-Rex, Tek Gözlü Dev (Cyclops)
- **Seviye 4:** Titan Golem, Cehennem Ejderi, Hydra, Phoenix
- **Seviye 5:** Hiçlik Ejderi (Void Dragon), Kaos Titani, Khaos Tanrısı

---

## 🎯 Boss Çağırma Sistemi

### Çağırma Çekirdeği (Summon Core)

**YENİ ÖZELLİK**: Artık tüm boss ritüellerinde **Çağırma Çekirdeği** kullanılıyor!

**Çağırma Çekirdeği** (`SUMMON_CORE`), tüm boss ritüellerinin merkezinde bulunan özel bir bloktur:
- Admin komutu ile verilebilir: `/scadmin give tool summon_core`
- Yerleştirildiğinde `END_CRYSTAL` bloğu olarak görünür
- Ritüel deseni çekirdeğin **altına** (1 blok aşağıya) yapılır
- Aktivasyon itemi ile çekirdeğe sağ tıklanarak boss çağrılır
- Hangi boss çağrılacağı aktivasyon itemine göre belirlenir

### Genel Ritüel Adımları

**Tüm Boss Ritüelleri İçin:**
```
1. Çağırma Çekirdeği'ni yerleştir (admin komutu veya manuel)
2. Boss'un ritüel desenini çekirdeğin altına yerleştir
3. Tüm blokların doğru olduğundan emin ol
4. Eline aktifleştirme item'ını al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Boss spawn olur!
```

**Cooldown**: Her ritüel konumu için **60 saniye** cooldown var.

**Admin Komutu ile Otomatik Yapı**: `/scadmin boss build <boss_tipi>`

---

## 🐉 Boss Listesi ve Detayları

### Seviye 1 Bosslar

#### 1. **Goblin Kralı** (GOBLIN_KING)

**Temel Bilgiler:**
- **Can:** 150 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Zombie
- **Seviye:** 1

**Özel Yetenekler:**
- Koşu Saldırısı (CHARGE)
- Minyon Çağırma (SUMMON_MINIONS) → Zombie'ler
- Patlama (EXPLOSION)

**Ritüel Deseni:**
```
C C C
C E C  (C = Cobblestone, E = Çağırma Çekirdeği - END_CRYSTAL)
C C C
```

**Aktifleştirme Item:** Rotten Flesh

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 3x3 Cobblestone platform oluştur (çekirdeğin altına)
3. Eline Rotten Flesh al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Goblin Kralı spawn olur!

**Admin Komutu**: `/scadmin boss build goblin_king`

**Drop:**
- %100: Goblin Kralı Taçı (Goblin Crown)
- %30-70: Tarif Kitapları (Seviye 1)

---

#### 2. **Ork Şefi** (ORC_CHIEF)

**Temel Bilgiler:**
- **Can:** 200 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Zombie
- **Seviye:** 1-2

**Özel Yetenekler:**
- Koşu Saldırısı (CHARGE)
- Blok Fırlatma (BLOCK_THROW)
- Minyon Çağırma (SUMMON_MINIONS) → Zombie'ler

**Ritüel Deseni:**
```
S S S
S E S  (S = Stone, E = Çağırma Çekirdeği - END_CRYSTAL)
S S S
```

**Aktifleştirme Item:** Iron Sword

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 3x3 Stone platform oluştur (çekirdeğin altına)
3. Eline Iron Sword al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Ork Şefi spawn olur!

**Admin Komutu**: `/scadmin boss build orc_chief`

**Drop:**
- %100: Ork Şefi Amuleti (Orc Amulet)
- %30-70: Tarif Kitapları (Seviye 1)

---

### Seviye 2 Bosslar

#### 3. **Troll Kralı** (TROLL_KING)

**Temel Bilgiler:**
- **Can:** 300 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Zombie
- **Seviye:** 2

**Özel Yetenekler:**
- Blok Fırlatma (BLOCK_THROW)
- Şok Dalgası (SHOCKWAVE)
- Kendini İyileştirme (HEAL) → %15 can iyileştirme

**Ritüel Deseni:**
```
B B B
B E B  (B = Stone Bricks, E = Çağırma Çekirdeği - END_CRYSTAL)
B B B
```

**Aktifleştirme Item:** Stone Axe

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 3x3 Stone Bricks platform oluştur (çekirdeğin altına)
3. Eline Stone Axe al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Troll Kralı spawn olur!

**Admin Komutu**: `/scadmin boss build troll_king`

**Drop:**
- %100: Troll Kralı Kalbi (Troll Heart)
- %40-80: Tarif Kitapları (Seviye 2)

---

### Seviye 3 Bosslar

#### 4. **Ejderha** (DRAGON) - 2 Faz

**Temel Bilgiler:**
- **Can:** 500 HP
- **Faz:** 2 (Faz 1: %100-50, Faz 2: %50-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Phantom
- **Seviye:** 3

**Özel Yetenekler:**
- **Faz 1:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Işınlanma (TELEPORT)
  - Patlama (EXPLOSION)
- **Faz 2:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Işınlanma (TELEPORT)
  - Minyon Çağırma (SUMMON_MINIONS) → Blaze'ler

**Ritüel Deseni:**
```
O O O O O
O · · · O
O · E · O  (O = Obsidian, E = Çağırma Çekirdeği - END_CRYSTAL, · = Boş)
O · · · O
O O O O O
```

**Aktifleştirme Item:** Dragon Egg

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 5x5 Obsidian platform oluştur (çekirdeğin altına)
3. Eline Dragon Egg al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Ejderha spawn olur!

**Admin Komutu**: `/scadmin boss build dragon`

**Drop:**
- %100: Ejderha Ölçeği (Dragon Scale)
- %50-90: Tarif Kitapları (Seviye 3)

---

#### 5. **T-Rex** (TREX)

**Temel Bilgiler:**
- **Can:** 600 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Ravager
- **Seviye:** 3

**Özel Yetenekler:**
- Koşu Saldırısı (CHARGE)
- Şok Dalgası (SHOCKWAVE)
- Patlama (EXPLOSION)

**Ritüel Deseni:**
```
D S S S D
S · · · S
S · E · S  (D = Diamond Block, S = Stone, E = Çağırma Çekirdeği - END_CRYSTAL)
S · · · S
D S S S D
```

**Aktifleştirme Item:** Bone

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 5x5 Stone platform oluştur (çekirdeğin altına)
3. Köşelere Diamond Block, merkeze Çağırma Çekirdeği koy
4. Eline Bone al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. T-Rex spawn olur!

**Admin Komutu**: `/scadmin boss build trex`

**Drop:**
- %100: T-Rex Dişi (T-Rex Tooth)
- %50-90: Tarif Kitapları (Seviye 3)

---

#### 6. **Tek Gözlü Dev** (CYCLOPS) - 2 Faz

**Temel Bilgiler:**
- **Can:** 700 HP
- **Faz:** 2 (Faz 1: %100-66, Faz 2: %66-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Giant
- **Seviye:** 3-4

**Özel Yetenekler:**
- **Faz 1:**
  - Blok Fırlatma (BLOCK_THROW)
  - Şok Dalgası (SHOCKWAVE)
  - Koşu Saldırısı (CHARGE)
- **Faz 2:**
  - Blok Fırlatma (BLOCK_THROW)
  - Şok Dalgası (SHOCKWAVE)
  - Patlama (EXPLOSION)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme

**Ritüel Deseni:**
```
G B B B G
B · · · B
B · E · B  (G = Gold Block, B = Stone Bricks, E = Çağırma Çekirdeği - END_CRYSTAL)
B · · · B
G B B B G
```

**Aktifleştirme Item:** Ender Eye

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 5x5 Stone Bricks platform oluştur (çekirdeğin altına)
3. Köşelere Gold Block, merkeze Çağırma Çekirdeği koy
4. Eline Ender Eye al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Tek Gözlü Dev spawn olur!

**Admin Komutu**: `/scadmin boss build cyclops`

**Drop:**
- %100: Cyclops Gözü (Cyclops Eye)
- %50-90: Tarif Kitapları (Seviye 3)

---

### Seviye 4 Bosslar

#### 7. **Titan Golem** (TITAN_GOLEM) - 3 Faz + Zayıf Nokta

**Temel Bilgiler:**
- **Can:** 800 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** Var (3x hasar)
- **Zayıflık:** 🔥 **Alev** (2x hasar)
- **Entity Tipi:** Iron Golem
- **Seviye:** 4

**Özel Yetenekler:**
- **Faz 1:**
  - Blok Fırlatma (BLOCK_THROW)
  - Şok Dalgası (SHOCKWAVE)
  - Patlama (EXPLOSION)
- **Faz 2:**
  - Blok Fırlatma (BLOCK_THROW)
  - Şok Dalgası (SHOCKWAVE)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme
- **Faz 3:**
  - Blok Fırlatma (BLOCK_THROW)
  - Şok Dalgası (SHOCKWAVE)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Patlama (EXPLOSION)
  - Minyon Çağırma (SUMMON_MINIONS) → Iron Golem'ler

**Ritüel Deseni:**
```
D O O O O O O D
O · · · · · · O
O · · · · · · O
O · · E · · · O  (D = Diamond Block, O = Obsidian, E = Çağırma Çekirdeği - END_CRYSTAL)
O · · · · · · O
O · · · · · · O
D O O O O O O D
```

**Aktifleştirme Item:** Nether Star

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 7x7 Obsidian platform oluştur (çekirdeğin altına)
3. Köşelere Diamond Block, merkeze Çağırma Çekirdeği koy
4. Eline Nether Star al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Titan Golem spawn olur!

**Admin Komutu**: `/scadmin boss build titan_golem`

**Drop:**
- %100: Titan Golem Çekirdeği (Titan Core)
- %60-100: Tarif Kitapları (Seviye 4)

**Strateji:**
- Alev hasarı kullanarak 2x hasar ver
- Zayıf noktasına vurarak 3x kritik hasar ver
- Faz 3'te minyonlara dikkat et

---

#### 8. **Cehennem Ejderi** (HELL_DRAGON) - 2 Faz + Zayıf Nokta

**Temel Bilgiler:**
- **Can:** 900 HP
- **Faz:** 2 (Faz 1: %100-50, Faz 2: %50-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** 💧 **Su** (2x hasar)
- **Entity Tipi:** Phantom
- **Seviye:** 4

**Özel Yetenekler:**
- **Faz 1:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Işınlanma (TELEPORT)
  - Patlama (EXPLOSION)
- **Faz 2:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Zehir Bulutu (POISON_CLOUD)
  - Işınlanma (TELEPORT)

**Ritüel Deseni:**
```
O N N N N N N O
N · · · · · · N
N · · · · · · N
N · · E · · · N  (O = Obsidian, N = Netherrack, E = Çağırma Çekirdeği - END_CRYSTAL)
N · · · · · · N
N · · · · · · N
O N N N N N N O
```

**Aktifleştirme Item:** Blaze Rod

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 7x7 Netherrack platform oluştur (çekirdeğin altına)
3. Köşelere Obsidian, merkeze Çağırma Çekirdeği koy
4. Eline Blaze Rod al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Cehennem Ejderi spawn olur!

**Admin Komutu**: `/scadmin boss build hell_dragon`

**Drop:**
- %100: Ejderha Ölçeği (Dragon Scale) - Cehennem Ejderi versiyonu
- %60-100: Tarif Kitapları (Seviye 4)

**Strateji:**
- Su hasarı kullanarak 2x hasar ver (boğulma hasarı)
- Işınlanma yeteneğine dikkat et
- Zehir bulutundan kaç

---

#### 9. **Hydra** (HYDRA) - 3 Faz + Zayıf Nokta

**Temel Bilgiler:**
- **Can:** 850 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** ☠️ **Zehir** (2x hasar)
- **Entity Tipi:** Guardian
- **Seviye:** 4-5

**Özel Yetenekler:**
- **Faz 1:**
  - Zehir Bulutu (POISON_CLOUD)
  - Işınlanma (TELEPORT)
  - Minyon Çağırma (SUMMON_MINIONS) → Guardian'lar
- **Faz 2:**
  - Zehir Bulutu (POISON_CLOUD)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme
  - Minyon Çağırma (SUMMON_MINIONS) → Guardian'lar
- **Faz 3:**
  - Zehir Bulutu (POISON_CLOUD)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Patlama (EXPLOSION)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme
  - Minyon Çağırma (SUMMON_MINIONS) → Guardian'lar

**Ritüel Deseni:**
```
E P P P P P P E
P · · · · · · P
P · · · · · · P
P · · E · · · P  (E = Emerald Block, P = Prismarine, E = Çağırma Çekirdeği - END_CRYSTAL)
P · · · · · · P
P · · · · · · P
E P P P P P P E
```

**Aktifleştirme Item:** Heart of the Sea

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 7x7 Prismarine platform oluştur (çekirdeğin altına)
3. Köşelere Emerald Block, merkeze Çağırma Çekirdeği koy
4. Eline Heart of the Sea al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Hydra spawn olur!

**Admin Komutu**: `/scadmin boss build hydra`

**Drop:**
- %100: Kraken Dokunaçı (Kraken Tentacle)
- %60-100: Tarif Kitapları (Seviye 4)

**Strateji:**
- Zehir potion efekti kullanarak 2x hasar ver
- Zehir bulutundan kaç
- Minyonları önce temizle

---

#### 10. **Phoenix** (PHOENIX) - 2 Faz

**Temel Bilgiler:**
- **Can:** 600 HP
- **Faz:** 2 (Faz 1: %100-50, Faz 2: %50-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Blaze
- **Seviye:** 4

**Özel Yetenekler:**
- **Faz 1:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Işınlanma (TELEPORT)
- **Faz 2:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme
  - Patlama (EXPLOSION)

**Ritüel Deseni:**
```
N N N N N
N · · · N
N · E · N  (N = Netherrack, E = Çağırma Çekirdeği - END_CRYSTAL)
N · · · N
N N N N N
```

**Aktifleştirme Item:** Blaze Powder

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 5x5 Netherrack platform oluştur (çekirdeğin altına)
3. Eline Blaze Powder al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Phoenix spawn olur!

**Admin Komutu**: `/scadmin boss build phoenix`

**Drop:**
- %100: Phoenix Tüyü (Phoenix Feather)
- %60-100: Tarif Kitapları (Seviye 4)

---

### Seviye 5 Bosslar

#### 11. **Hiçlik Ejderi** (VOID_DRAGON) - 3 Faz

**Temel Bilgiler:**
- **Can:** 1200 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Ender Dragon
- **Seviye:** 5

**Özel Yetenekler:**
- **Faz 1:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Işınlanma (TELEPORT)
- **Faz 2:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Patlama (EXPLOSION)
  - Minyon Çağırma (SUMMON_MINIONS) → Blaze'ler
- **Faz 3:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Patlama (EXPLOSION)
  - Şok Dalgası (SHOCKWAVE)
  - Işınlanma (TELEPORT)
  - Minyon Çağırma (SUMMON_MINIONS) → Blaze'ler

**Ritüel Deseni:**
```
O O O O O O O
O · · · · · O
O · · · · · O
O · · E · · O  (O = Obsidian, E = Çağırma Çekirdeği - END_CRYSTAL)
O · · · · · O
O · · · · · O
O O O O O O O
```

**Aktifleştirme Item:** Dragon Egg

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 7x7 Obsidian platform oluştur (çekirdeğin altına)
3. Eline Dragon Egg al
4. Çağırma Çekirdeği'ne SAĞ TIKLA
5. Hiçlik Ejderi spawn olur!

**Admin Komutu**: `/scadmin boss build void_dragon`

**Drop:**
- %100: Hiçlik Ejderi Kalbi (Void Dragon Heart)
- %70-100: Tarif Kitapları (Seviye 5)

---

#### 12. **Kaos Titani** (CHAOS_TITAN) - 3 Faz

**Temel Bilgiler:**
- **Can:** 1100 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** Yok
- **Zayıflık:** Yok
- **Entity Tipi:** Wither
- **Seviye:** 5

**Özel Yetenekler:**
- **Faz 1:**
  - Şok Dalgası (SHOCKWAVE)
  - Blok Fırlatma (BLOCK_THROW)
  - Patlama (EXPLOSION)
- **Faz 2:**
  - Şok Dalgası (SHOCKWAVE)
  - Blok Fırlatma (BLOCK_THROW)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Minyon Çağırma (SUMMON_MINIONS) → Iron Golem'ler
- **Faz 3:**
  - Şok Dalgası (SHOCKWAVE)
  - Blok Fırlatma (BLOCK_THROW)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Patlama (EXPLOSION)
  - Minyon Çağırma (SUMMON_MINIONS) → Iron Golem'ler

**Ritüel Deseni:**
```
N N N N N N N
N D · · · D N
N · · · · · N
N · · E · · N  (N = Netherite Block, D = Diamond Block, E = Çağırma Çekirdeği - END_CRYSTAL)
N · · · · · N
N D · · · D N
N N N N N N N
```

**Aktifleştirme Item:** Nether Star

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 7x7 Netherite Block platform oluştur (çekirdeğin altına)
3. Kenarlara Diamond Block, merkeze Çağırma Çekirdeği koy
4. Eline Nether Star al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Kaos Titani spawn olur!

**Admin Komutu**: `/scadmin boss build chaos_titan`

**Drop:**
- %100: Şeytan Lordu Boynuzu (Demon Lord Horn)
- %70-100: Tarif Kitapları (Seviye 5)

---

#### 13. **Khaos Tanrısı** (CHAOS_GOD) - 3 Faz + Zayıf Noktalar

**Temel Bilgiler:**
- **Can:** 1000 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** Var (3x hasar)
- **Zayıflık:** 🔥 **Alev** + ☠️ **Zehir** (2x hasar)
- **Entity Tipi:** Wither
- **Seviye:** 5

**Özel Yetenekler:**
- **Faz 1:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Işınlanma (TELEPORT)
- **Faz 2:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Zehir Bulutu (POISON_CLOUD)
  - Patlama (EXPLOSION)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme
- **Faz 3:**
  - Ateş Püskürtme (FIRE_BREATH)
  - Yıldırım Atma (LIGHTNING_STRIKE)
  - Zehir Bulutu (POISON_CLOUD)
  - Patlama (EXPLOSION)
  - Şok Dalgası (SHOCKWAVE)
  - Kendini İyileştirme (HEAL) → %15 can iyileştirme
  - Minyon Çağırma (SUMMON_MINIONS) → Wither Skeleton'lar

**Ritüel Deseni:**
```
N B B B B B B B N
B O · · · · · O B
B · · · · · · · B
B · · · · · · · B
B · · · E · · · B  (N = Netherite Block, B = Bedrock, O = Obsidian, E = Çağırma Çekirdeği - END_CRYSTAL)
B · · · · · · · B
B · · · · · · · B
B O · · · · · O B
N B B B B B B B N
```

**Aktifleştirme Item:** Nether Star

**Ritüel Yapılışı:**
1. Çağırma Çekirdeği'ni yerleştir (merkeze)
2. 9x9 Bedrock platform oluştur (çekirdeğin altına)
3. Köşelere Netherite Block, kenarlara Obsidian, merkeze Çağırma Çekirdeği koy
4. Eline Nether Star al
5. Çağırma Çekirdeği'ne SAĞ TIKLA
6. Khaos Tanrısı spawn olur!

**Admin Komutu**: `/scadmin boss build chaos_god`

**Drop:**
- %100: Şeytan Lordu Boynuzu (Demon Lord Horn)
- %70-100: Tarif Kitapları (Seviye 5)

**Strateji:**
- Alev ve zehir hasarı kullanarak 2x hasar ver
- Zayıf noktasına vurarak 3x kritik hasar ver
- En zor boss, tüm yeteneklerini kullanır
- Faz 3'te çok dikkatli ol

---

## ⚔️ Boss Yetenekleri

### 1. **Ateş Püskürtme (FIRE_BREATH)**

**Açıklama:**
- Boss hedefe doğru ateş püskürtür
- 10 blok mesafeye kadar ateş partikülleri
- Oyunculara 60 tick (3 saniye) yanma efekti
- 3 HP hasar

**Kullanan Bosslar:**
- Ejderha, Cehennem Ejderi, Hiçlik Ejderi, Phoenix, Khaos Tanrısı

**Cooldown:** 5 saniye

**Tehdit Uyarısı:** Var (3 saniye önceden uyarı)

---

### 2. **Patlama (EXPLOSION)**

**Açıklama:**
- Boss'un konumunda 3 blok yarıçaplı patlama
- Blok kırmaz (güvenli)
- Yüksek hasar (patlama hasarı)

**Kullanan Bosslar:**
- Goblin Kralı, Ejderha, T-Rex, Tek Gözlü Dev, Titan Golem, Cehennem Ejderi, Hydra, Phoenix, Hiçlik Ejderi, Kaos Titani, Khaos Tanrısı

**Cooldown:** 8 saniye

**Tehdit Uyarısı:** Var (3 saniye önceden uyarı)

---

### 3. **Yıldırım Atma (LIGHTNING_STRIKE)**

**Açıklama:**
- Hedef oyuncunun konumuna yıldırım düşer
- 5 HP hasar
- Elektrik partikülleri
- 2 blok yarıçaplı alan hasarı

**Kullanan Bosslar:**
- Ejderha, Tek Gözlü Dev, Titan Golem, Cehennem Ejderi, Hydra, Hiçlik Ejderi, Kaos Titani, Khaos Tanrısı

**Cooldown:** 5 saniye

**Tehdit Uyarısı:** Var (3 saniye önceden uyarı)

---

### 4. **Blok Fırlatma (BLOCK_THROW)**

**Açıklama:**
- Boss'un üstünden 3 adet Cobblestone bloğu hedefe fırlatılır
- FallingBlock olarak spawn olur
- Hasar verir
- Bloklar drop etmez

**Kullanan Bosslar:**
- Ork Şefi, Troll Kralı, Tek Gözlü Dev, Titan Golem, Kaos Titani

**Cooldown:** 5 saniye

**Tehdit Uyarısı:** Yok

---

### 5. **Zehir Bulutu (POISON_CLOUD)**

**Açıklama:**
- 5 blok yarıçaplı alanda zehir bulutu oluşturur
- Yakındaki oyunculara zehir efekti (60 tick, seviye 1)
- 2 HP/saniye hasar

**Kullanan Bosslar:**
- Cehennem Ejderi, Hydra, Khaos Tanrısı

**Cooldown:** 5 saniye

**Tehdit Uyarısı:** Yok

---

### 6. **Işınlanma (TELEPORT)**

**Açıklama:**
- Boss hedef oyuncunun yakınına ışınlanır
- Portal partikülleri
- Sürpriz saldırı için
- 3 blok mesafe içinde güvenli konum arar

**Kullanan Bosslar:**
- Ejderha, Cehennem Ejderi, Hydra, Phoenix, Hiçlik Ejderi, Khaos Tanrısı

**Cooldown:** 12 saniye

**Tehdit Uyarısı:** Yok

---

### 7. **Koşu Saldırısı (CHARGE)**

**Açıklama:**
- Boss hedefe doğru hızlıca koşar
- Yüksek hız (1.5x multiplier)
- Çarpışma hasarı
- Geri savurma efekti

**Kullanan Bosslar:**
- Goblin Kralı, Ork Şefi, Tek Gözlü Dev

**Cooldown:** 5 saniye

**Tehdit Uyarısı:** Var (3 saniye önceden uyarı)

---

### 8. **Minyon Çağırma (SUMMON_MINIONS)**

**Açıklama:**
- Boss tipine göre minyonlar spawn olur
- 2 adet minyon
- Boss'un etrafında 2 blok mesafede spawn olur

**Minyon Tipleri:**
- **Goblin Kralı, Ork Şefi, Troll Kralı:** Zombie
- **Ejderha, Cehennem Ejderi, Hiçlik Ejderi:** Blaze
- **Titan Golem, Kaos Titani:** Iron Golem
- **Hydra:** Guardian
- **Khaos Tanrısı:** Wither Skeleton

**Kullanan Bosslar:**
- Goblin Kralı, Ork Şefi, Ejderha, Titan Golem, Hydra, Hiçlik Ejderi, Kaos Titani, Khaos Tanrısı

**Cooldown:** 12 saniye

**Tehdit Uyarısı:** Yok

---

### 9. **Kendini İyileştirme (HEAL)**

**Açıklama:**
- Boss kendini %15 iyileştirir
- Kalp partikülleri
- Kritik durumlarda kullanır
- Maksimum canı geçemez

**Kullanan Bosslar:**
- Troll Kralı, Tek Gözlü Dev, Titan Golem, Hydra, Phoenix, Khaos Tanrısı

**Cooldown:** 12 saniye

**Tehdit Uyarısı:** Yok

---

### 10. **Şok Dalgası (SHOCKWAVE)**

**Açıklama:**
- 6 blok yarıçaplı şok dalgası
- Oyuncuları iter (geri savurma)
- 4 HP hasar
- Patlama partikülleri

**Kullanan Bosslar:**
- Troll Kralı, T-Rex, Tek Gözlü Dev, Titan Golem, Hiçlik Ejderi, Kaos Titani, Khaos Tanrısı

**Cooldown:** 8 saniye

**Tehdit Uyarısı:** Var (3 saniye önceden uyarı)

---

## 🔄 Faz Sistemi

Güçlü bosslar faz sistemi kullanır. Can %'sine göre faz değişir:

### 2 Fazlı Bosslar:
- **Faz 1:** %100-50 can
- **Faz 2:** %50-0 can

**2 Fazlı Bosslar:**
- Ejderha
- Cehennem Ejderi
- Phoenix

### 3 Fazlı Bosslar:
- **Faz 1:** %100-66 can
- **Faz 2:** %66-33 can
- **Faz 3:** %33-0 can

**3 Fazlı Bosslar:**
- Titan Golem
- Hydra
- Hiçlik Ejderi
- Kaos Titani
- Khaos Tanrısı

### Faz Değişimi:
- Faz değiştiğinde:
  - Duyuru mesajı gösterilir
  - Yeni yetenekler aktif olur
  - Ses efekti çalar
  - BossBar güncellenir

---

## ⚡ Zayıf Noktalar ve Zayıflıklar

### Zayıf Nokta Sistemi

Bazı bossların **zayıf noktaları** vardır. Bu zayıf noktalara hasar verildiğinde **3x hasar** alırlar.

**Zayıf Noktası Olan Bosslar:**
- Titan Golem
- Khaos Tanrısı

**Zayıf Nokta Süresi:** 5 saniye (aktif olduğunda)

**Zayıf Nokta Efektleri:**
- Kritik vuruş partikülleri
- Ses efektleri
- Oyuncuya mesaj: "⚡ ZAYIF NOKTASI VURULDU! 3x hasar!"

---

### Zayıflık Sistemi

Bazı bossların **zayıflıkları** vardır. Bu zayıflıklara hasar verildiğinde **2x hasar** alırlar.

**Zayıflık Türleri:**

1. **🔥 Alev Zayıflığı (FIRE)**
   - Alev, Lava, Ateş hasarı 2x
   - **Bosslar:** Titan Golem, Khaos Tanrısı

2. **💧 Su Zayıflığı (WATER)**
   - Boğulma hasarı 2x
   - **Bosslar:** Cehennem Ejderi

3. **☠️ Zehir Zayıflığı (POISON)**
   - Zehir potion efekti 2x hasar
   - **Bosslar:** Hydra, Khaos Tanrısı

4. **⚡ Yıldırım Zayıflığı (LIGHTNING)**
   - Yıldırım hasarı 2x
   - (Şu an kullanılmıyor)

---

### Kalkan Sistemi

Bazı bosslar **kalkan** kullanabilir. Kalkan aktifken:
- **%70 hasar azaltma** (hasarın %30'u geçer)
- Kalkan partikülleri
- Kalkan süresi: 3 saniye

---

## 🏟️ Arena Transformasyon Sistemi

Boss spawn olduğunda, etrafındaki alan dinamik olarak dönüşmeye başlar. Bu sistem **NewBossArenaManager** tarafından yönetilir.

### 🌍 Arena Mekanikleri

#### 1. **Dinamik Yayılma**
- Boss spawn olduğunda arena transformasyonu başlar
- Boss'tan dışa doğru sürekli yayılır
- Her 2 saniyede 1.2 blok genişler
- Maksimum yarıçap boss seviyesine göre:
  - **Seviye 1:** 15 blok
  - **Seviye 2:** 20 blok
  - **Seviye 3:** 25 blok
  - **Seviye 4:** 30 blok
  - **Seviye 5:** 35 blok

#### 2. **Kule Oluşturma**
- **İlk Kuleler:** Boss spawn olduğunda hemen oluşur
- **Sürekli Oluşturma:** Her 60 saniyede bir (30 döngü) yeni kuleler eklenir
- **Kule Sayısı:** Her oluşturmada 5-9 kule
- **Kule Boyutları:**
  - Yükseklik: 2-15 blok (rastgele)
  - Genişlik: 1-6 blok (rastgele, kare taban)
- **Kule Malzemeleri:** Boss tipine göre değişir:
  - **Titan Golem:** Demir Bloğu
  - **Chaos God/Titan:** Obsidyen
  - **Void Dragon:** End Stone Bricks
  - **Dragon/Hell Dragon:** Netherrack
  - **Hydra:** Prismarine Bricks
  - **Phoenix:** Magma Bloğu
  - **Diğerleri:** Cobblestone veya Stone Bricks

#### 3. **Çevresel Tehlikeler**
Boss arenasında sürekli tehlikeler oluşur:

- **Oluşturma Sıklığı:** Her 2 saniyede bir (her döngüde)
- **Tehlike Sayısı:** Her döngüde 12-19 tehlike
- **Dağılım:**
  - **%45 Örümcek Ağı:** Zemin + 1-5 blok yukarıda
  - **%40 Lav:** Zemin seviyesinde
  - **%15 Su:** Zemin seviyesinde
- **Menzil:** Boss'tan 8 blok uzaklıktan başlar, arena yarıçapına kadar

#### 4. **Blok Transformasyonu**
- Arena içindeki zemin blokları boss tipine göre dönüşür
- Her döngüde 8 blok dönüştürülür
- **Boss Tipine Göre Malzemeler:**
  - **Seviye 1-2:** Dirt, Coarse Dirt, Gravel, Cobblestone, Stone
  - **Seviye 2:** Grass Block, Podzol, Mossy Cobblestone, Stone, Andesite
  - **Seviye 3:** Netherrack, Blackstone, Basalt, Magma Block
  - **Seviye 4:** Stone, Diorite, Granite, Deepslate, Iron Block, Obsidian
  - **Seviye 5:** End Stone, End Stone Bricks, Purpur Block, Obsidian, Crying Obsidian

### ⚡ Performans Optimizasyonları

#### Dinamik Öncelik Sistemi (YENİ!)

Arena sistemi artık **dinamik öncelik sistemi** ile çalışıyor:

**Oyuncu Grupları:**
- 50 blok içindeki oyuncular aynı grup sayılır
- Yan yana oyuncular ortak arena task'larını paylaşır
- Her oyuncu grubuna minimum 5 arena task'ı garanti edilir

**Dinamik Arena Limiti:**
- Formül: `MAX(25, oyuncu_grup_sayısı × arenas_per_group)`
- Minimum 25 arena garantisi
- Oyuncu sayısı arttıkça limit otomatik artar

**50 Blok Genişleme Kuralı:**
- **50 blok içindeki bosslar:** Arena genişler, kuleler oluşur, tehlikeler oluşur
- **50 blok dışındaki bosslar:** Arena genişlemez (mevcut boyutta kalır)

**Uzak Arena Durdurma:**
- **Normal:** 100+ blok uzaktaki bosslar durdurulur
- **Performans Sorunu:** 50+ blok uzaktaki bosslar durdurulur
- **Ciddi Performans Sorunu:** 25+ blok uzaktaki bosslar durdurulur

**Uzak Arena Tekrar Başlatma (YENİ!):**
- Durdurulmuş arenalar, oyuncu yaklaştığında **otomatik tekrar başlatılır**
- Her döngüde durdurulmuş arenalar kontrol edilir
- Oyuncu mesafesi `currentFarDistance` içine girerse arena tekrar başlatılır

#### Mesafe Bazlı Aktivasyon
- **Aktif Arena:** Oyuncu dinamik uzaklık limiti içindeyse
- **Pasif Arena:** Oyuncu uzaklık limiti dışındaysa (hiçbir işlem yapılmaz)
- **Önceliklendirme:** En yakın bosslar önce işlenir

#### Chunk Kontrolü
- Yüklü olmayan chunk'larda işlem yapılmaz
- Performans için kritik optimizasyon

#### Merkezi Task Sistemi
- Her arena için ayrı task yok
- Tek merkezi task tüm arenaları yönetir
- Her arena kendi döngü sayacını tutar

#### Otomatik Performans Optimizasyonu (YENİ!)

Sistem, sunucunun TPS değerini kontrol eder ve otomatik olarak ayarları düşürür:

**TPS Kontrolü:**
- **TPS ≥ 18.0:** Normal ayarlar
- **TPS < 18.0:** Performans sorunu → Ayarlar düşürülür

**Otomatik Ayarlama:**
- **Arenas Per Group:** 5 → 3 (performans sorunu varsa)
- **Oyuncu Grup Mesafesi:** 50 blok → 25 blok (performans sorunu varsa)
- **Uzaklık Limiti:** 100 blok → 50 blok → 25 blok (performans sorunu varsa)

**Performans İyileştiğinde:**
- Ayarlar yavaşça normale döner
- TPS > 19.5 ise: 25 blok → 50 blok
- TPS > 19.8 ise: 50 blok → 100 blok

### 🎮 Oyuncu Deneyimi

#### Görsel Efektler
- Blok dönüşümünde partikül efektleri (%15 şans)
- Ses efektleri (taş kırılma sesi)
- Sürekli değişen arena ortamı

#### Stratejik Önemi
- **Kuleler:** Taktiksel pozisyonlar, yüksek yer avantajı
- **Tehlikeler:** Hareket kısıtlaması, dikkat gerektirir
- **Blok Dönüşümü:** Arena'nın görünümü değişir, tanıdık alanlar kaybolur

### 📊 Arena Özellikleri Özeti

| Özellik | Değer | Config'den Değiştirilebilir |
|---------|-------|----------------------------|
| **Yayılma Hızı** | 1.2 blok / 2 saniye | ✅ |
| **Kule Oluşturma** | İlk: Hemen, Sonra: Her 60 saniyede | ✅ |
| **Tehlike Oluşturma** | Her 2 saniyede 12-19 tehlike | ✅ |
| **Blok Dönüşümü** | Her 2 saniyede 8 blok | ✅ |
| **Aktif Menzil** | Dinamik (100/50/25 blok) | ✅ |
| **Maksimum Arena** | Dinamik (oyuncu sayısına göre) | ✅ |
| **Arena Genişleme Limiti** | 50 blok | ✅ |
| **Oyuncu Grup Mesafesi** | 50 blok (normal), 25 blok (performans sorunu) | ✅ |
| **Grup Başına Arena** | 5 (normal), 3 (performans sorunu) | ✅ |
| **Task Interval** | 40 tick (2 saniye) | ✅ |
| **TPS Eşiği** | 18.0 | ✅ |

### ⚙️ Config Entegrasyonu (YENİ!)

**Tüm arena ayarları config dosyasından okunur ve değiştirilebilir!**

#### Config Dosyası Yolu:
```
plugins/Stratocraft/config.yml
```

#### Config Bölümü:
```yaml
boss:
  arena:
    # Dinamik öncelik sistemi ayarları
    min-arenas-per-group: 5              # Her oyuncu grubuna minimum arena sayısı
    min-arenas-per-group-fallback: 3     # Performans sorunu varsa düşürülmüş arena sayısı
    base-max-active-arenas: 25           # Temel maksimum arena sayısı
    task-interval: 40                     # Task çalışma aralığı (tick) - 2 saniye
    blocks-per-cycle: 8                   # Her döngüde dönüştürülecek blok sayısı
    hazard-create-interval: 1             # Tehlike oluşturma aralığı (döngü)
    player-group-distance: 50.0           # Oyuncu grup mesafesi (blok)
    player-group-distance-fallback: 25.0  # Performans sorunu varsa grup mesafesi (blok)
    far-distance: 100.0                   # Uzaklık limiti (blok)
    far-distance-fallback: 50.0          # Performans sorunu varsa uzaklık limiti (blok)
    far-distance-min: 25.0                # Minimum uzaklık limiti (blok)
    arena-expansion-limit: 50.0           # Arena genişleme limiti (blok)
    group-cache-duration: 5000            # Oyuncu grupları cache süresi (milisaniye)
    tps-threshold: 18.0                   # Performans sorunu TPS eşiği
    tps-sample-size: 100                  # TPS ölçümü için örnek sayısı (tick)
```

#### Config Değiştirme:
1. `config.yml` dosyasını düzenle
2. `/scadmin arena reload` komutu ile yeniden yükle
3. Veya sunucuyu yeniden başlat

**Not:** Config değişiklikleri anında uygulanır (reload komutu ile).

### ⚠️ Önemli Notlar

1. **Boss Hareketi:** Boss 5+ blok hareket ederse, arena yeni konumdan başlar
2. **Boss Ölümü:** Boss öldüğünde arena transformasyonu durur
3. **Performans:** Uzak arenalar pasif kalır, performans etkilenmez
4. **Chunk Yükleme:** Chunk yüklü değilse işlem yapılmaz

---

## 🏆 Boss Dropları

### Boss Özel İtemleri

Bosslar öldürüldüğünde **%100 şansla** kendi özel itemlerini düşürürler. Bu itemler özel zırh ve silah tariflerinde kullanılır.

#### Seviye 1 Boss İtemleri

**👑 Goblin Kralı Taçı (Goblin Crown)**
- **Boss:** Goblin Kralı
- **Görünüm:** Altın Miğfer
- **Kullanım:** Seviye 1-2 özel zırh/silah tariflerinde

**🏺 Ork Şefi Amuleti (Orc Amulet)**
- **Boss:** Ork Şefi
- **Görünüm:** Altın Elma
- **Kullanım:** Seviye 1-2 özel zırh/silah tariflerinde

#### Seviye 2 Boss İtemleri

**❤️ Troll Kralı Kalbi (Troll Heart)**
- **Boss:** Troll Kralı
- **Görünüm:** Deniz Kalbi
- **Kullanım:** Seviye 2 özel zırh/silah tariflerinde

#### Seviye 3 Boss İtemleri

**🐉 Ejderha Ölçeği (Dragon Scale)**
- **Boss:** Ejderha, Cehennem Ejderi
- **Görünüm:** Kaplumbağa Kabuğu (Turtle Scute)
- **Kullanım:** Seviye 3 özel zırh/silah tariflerinde

**🦷 T-Rex Dişi (T-Rex Tooth)**
- **Boss:** T-Rex
- **Görünüm:** Kemik
- **Kullanım:** Seviye 3 özel zırh/silah tariflerinde

**👁️ Cyclops Gözü (Cyclops Eye)**
- **Boss:** Cyclops
- **Görünüm:** Ender Gözü
- **Kullanım:** Seviye 3 özel zırh/silah tariflerinde

#### Seviye 4 Boss İtemleri

**⭐ Titan Golem Çekirdeği (Titan Core)**
- **Boss:** Titan Golem
- **Görünüm:** Nether Star
- **Kullanım:** Seviye 4 özel zırh/silah tariflerinde

**🔥 Phoenix Tüyü (Phoenix Feather)**
- **Boss:** Phoenix
- **Görünüm:** Tüy
- **Kullanım:** Seviye 4 özel zırh/silah tariflerinde

**🐙 Kraken Dokunaçı (Kraken Tentacle)**
- **Boss:** Hydra (Kraken/Cehennem Ejderi)
- **Görünüm:** Yosun
- **Kullanım:** Seviye 4 özel zırh/silah tariflerinde

#### Seviye 5 Boss İtemleri

**👹 Şeytan Lordu Boynuzu (Demon Lord Horn)**
- **Boss:** Kaos Titan, Khaos Tanrısı
- **Görünüm:** Keçi Boynuzu
- **Kullanım:** Seviye 5 özel zırh/silah tariflerinde

**💜 Hiçlik Ejderi Kalbi (Void Dragon Heart)**
- **Boss:** Hiçlik Ejderi
- **Görünüm:** Echo Shard
- **Kullanım:** Seviye 5 özel zırh/silah tariflerinde

---

### Tarif Kitapları

Bosslar öldürüldüğünde **tarif kitapları** da düşer:

#### Yapı Tarif Kitapları

Boss seviyesine göre yapı tarif kitapları düşer:

- **Seviye 1:** %40 şans → Temel yapılar (Alchemy Tower, Healing Beacon)
- **Seviye 2:** %50 şans → Orta seviye yapılar (Poison Reactor, Wall Generator, Auto Turret)
- **Seviye 3:** %60 şans → İleri seviye yapılar (Tectonic Stabilizer, Siege Factory, Gravity Well, Global Market Gate)
- **Seviye 4:** %70 şans → Çok ileri seviye yapılar (Lava Trencher, Drone Station, Teleporter, Oil Refinery)
- **Seviye 5:** %80 şans → Efsanevi yapılar (Weather Machine, Invisibility Cloak)

#### Özel Zırh/Silah Tarif Kitapları

Boss seviyesine göre özel zırh/silah tarif kitapları düşer:

- **Seviye 1:** %30 şans → Seviye 1 zırh/silah tarifleri (5 zırh + 5 silah)
- **Seviye 2:** %40 şans → Seviye 2 zırh/silah tarifleri (5 zırh + 5 silah)
- **Seviye 3:** %50 şans → Seviye 3 zırh/silah tarifleri (5 zırh + 5 silah)
- **Seviye 4:** %60 şans → Seviye 4 zırh/silah tarifleri (5 zırh + 5 silah)
- **Seviye 5:** %70 şans → Seviye 5 zırh/silah tarifleri (5 zırh + 5 silah)

**Not:** Her boss öldürüldüğünde rastgele bir tarif kitabı düşer (yapı veya zırh/silah).

---

## 📊 BossBar Sistemi

### BossBar Özellikleri

Tüm bosslar spawn edildiğinde **BossBar** gösterilir:

- **Görünürlük:** Ekranın üst kısmında
- **İçerik:** Boss ismi ve can bilgisi
- **Format:** `§c§l[Boss İsmi] §7[Can/Maksimum Can]`
- **Renk:** Kırmızı (BarColor.RED)
- **Stil:** Solid (BarStyle.SOLID)

### BossBar Güncelleme

- **Güncelleme Sıklığı:** Her saniye (20 tick)
- **Progress:** Boss'un mevcut canı / maksimum canı
- **Mesafe Kontrolü:** 100 blok içindeki oyunculara gösterilir
- **Maksimum BossBar:** Oyuncu başına 3 bossBar (en yakın 3 boss)

### Faz Bilgisi

Çok fazlı bosslar için faz bilgisi BossBar'da gösterilir (gelecekte eklenecek).

---

## 🌍 Doğada Spawn

Bosslar doğada da nadiren spawn olabilir:

### Spawn Şansı

- **Seviye 1:** %1 şans → Goblin Kralı, Ork Şefi
- **Seviye 2:** %1.5 şans → Ork Şefi, Troll Kralı
- **Seviye 3:** %2 şans → Ejderha, T-Rex, Tek Gözlü Dev
- **Seviye 4:** %2.5 şans → Tek Gözlü Dev, Titan Golem, Cehennem Ejderi, Hydra, Phoenix
- **Seviye 5:** %3 şans → Hydra, Khaos Tanrısı

### Spawn Mekanizması

- **WorldGenerationListener** tarafından kontrol edilir
- Zorluk seviyesine göre rastgele boss seçilir
- Spawn edildiğinde duyuru yapılır
- Arena transformasyonu otomatik başlar

### ⚠️ Önemli Notlar

1. **Hazırlıksız Yakalanma:** Doğada spawn olan bosslar hazırlıksız yakalayabilir
2. **Zorluk Seviyesi:** Spawn şansı zorluk seviyesine bağlıdır
3. **Rastgele:** Hangi boss spawn olacağı rastgeledir

---

## 🎮 Admin Komutları

### Boss Komutları

#### Boss Listesi
```bash
/scadmin boss list
```
Tüm boss tiplerini listeler.

#### Boss Spawn Et
```bash
/scadmin boss spawn <boss_tipi>
```
Örnek: `/scadmin boss spawn DRAGON`

**Boss Tipleri:**
- `goblin_king` - Goblin Kralı
- `orc_chief` - Ork Şefi
- `troll_king` - Troll Kralı
- `dragon` - Ejderha
- `trex` - T-Rex
- `cyclops` - Tek Gözlü Dev
- `titan_golem` - Titan Golem
- `hell_dragon` - Cehennem Ejderi
- `hydra` - Hydra
- `phoenix` - Phoenix
- `void_dragon` - Hiçlik Ejderi
- `chaos_titan` - Kaos Titani
- `chaos_god` - Khaos Tanrısı

#### Ritüel Deseni Göster
```bash
/scadmin boss ritual <boss_tipi>
```
Örnek: `/scadmin boss ritual CHAOS_GOD`

Boss'un ritüel desenini gösterir.

#### Ritüel Yapısı Oluştur
```bash
/scadmin boss build <boss_tipi>
```
Örnek: `/scadmin boss build goblin_king`

Boss'un ritüel yapısını otomatik oluşturur.

#### Çağırma Çekirdeği Ver
```bash
/scadmin give tool summon_core
```
Çağırma Çekirdeği item'ını verir.

---

### Arena Yönetim Komutları (YENİ!)

#### Sistem Durumu
```bash
/scadmin arena status
```
Arena sisteminin durumunu ve performans metriklerini gösterir:
- Aktif arena sayısı
- Durdurulmuş arena sayısı
- Toplam işlenen/durdurulan arena sayıları
- Ortalama mesafe
- Mevcut TPS değeri
- Oyuncu grup sayısı
- Grup başına arena sayısı
- Grup mesafesi ve uzaklık limiti
- Metrik süresi

#### Oyuncu Grupları
```bash
/scadmin arena groups
```
Oyuncu gruplarını listeler:
- Her grubun oyuncu sayısı
- Grup içindeki oyuncu isimleri

#### Config Ayarları
```bash
/scadmin arena settings
```
Mevcut config ayarlarını gösterir:
- Tüm arena ayarları (config'den okunan değerler)
- Normal ve fallback değerleri

#### Metrikleri Sıfırla
```bash
/scadmin arena reset
```
Performans metriklerini sıfırlar:
- Toplam işlenen/durdurulan sayıları sıfırlar
- Metrik süresini sıfırlar

#### Config Yeniden Yükle
```bash
/scadmin arena reload
```
Config dosyasını yeniden yükler:
- `config.yml` değişikliklerini uygular
- Sunucu yeniden başlatmaya gerek kalmaz
- Anında uygulanır

---

## 🎯 Strateji İpuçları

### Genel Stratejiler

1. **Hazırlık:**
   - Yeterli zırh ve silah al
   - İyileştirme potionları hazırla
   - Takım halinde savaş (özellikle güçlü bosslar için)

2. **Mesafe Kontrolü:**
   - Bazı bosslar ışınlanabilir, mesafeyi koru
   - Ateş püskürtmelerinden kaç
   - Yıldırım saldırılarına dikkat et

3. **Zayıf Noktaları Kullan:**
   - Titan Golem → Alev hasarı (2x)
   - Cehennem Ejderi → Su hasarı (2x)
   - Hydra → Zehir hasarı (2x)
   - Khaos Tanrısı → Alev + Zehir hasarı (2x)

4. **Zayıf Nokta Vuruşları:**
   - Titan Golem ve Khaos Tanrısı'nın zayıf noktalarına vur (3x hasar)
   - Zayıf nokta aktifken kritik vuruş yap

5. **Faz Değişimlerine Hazır Ol:**
   - Faz değiştiğinde yeni yetenekler gelir
   - Daha agresif olur
   - Dikkatli ol!

6. **Minyonları Önce Öldür:**
   - Minyonlar rahatsız edici olabilir
   - Önce onları temizle
   - Sonra boss'a odaklan

7. **Arena Tehlikeleri:**
   - Örümcek ağlarından kaç
   - Lav havuzlarına dikkat et
   - Kuleleri taktiksel olarak kullan

8. **Kalkan Sistemi:**
   - Boss kalkan kullanıyorsa bekle
   - Kalkan bitince saldır

### Boss Özel Stratejileri

#### Goblin Kralı / Ork Şefi
- En kolay bosslar
- Minyonları önce temizle
- Koşu saldırılarına dikkat et

#### Troll Kralı
- İyileştirme yeteneğine dikkat et
- Şok dalgasından kaç
- Blok fırlatmalarına dikkat et

#### Ejderha
- Işınlanma yeteneğine dikkat et
- Faz 2'de minyonlar gelir
- Ateş püskürtmelerinden kaç

#### T-Rex
- Şok dalgasından kaç
- Koşu saldırılarına dikkat et
- Patlamalardan uzak dur

#### Tek Gözlü Dev (Cyclops)
- Faz 2'de iyileştirme yeteneği gelir
- Blok fırlatmalarına dikkat et
- Şok dalgasından kaç

#### Titan Golem
- **Alev hasarı kullan!** (2x hasar)
- Zayıf noktasına vur (3x hasar)
- Faz 3'te minyonlar gelir
- Yıldırım saldırılarına dikkat et

#### Cehennem Ejderi
- **Su hasarı kullan!** (2x hasar)
- Işınlanma yeteneğine dikkat et
- Zehir bulutundan kaç
- Faz 2'de daha agresif olur

#### Hydra
- **Zehir potion efekti kullan!** (2x hasar)
- Zehir bulutundan kaç
- Minyonları önce temizle
- Faz 3'te çok dikkatli ol

#### Phoenix
- Ateş püskürtmelerinden kaç
- İyileştirme yeteneğine dikkat et
- Faz 2'de patlamalara dikkat et

#### Hiçlik Ejderi
- En güçlü bosslardan biri
- Tüm yetenekleri kullanır
- Faz 3'te çok dikkatli ol
- Minyonları önce temizle

#### Kaos Titani
- Blok fırlatmalarına dikkat et
- Şok dalgasından kaç
- Faz 3'te minyonlar gelir
- Yıldırım saldırılarına dikkat et

#### Khaos Tanrısı
- **EN ZOR BOSS!**
- **Alev ve zehir hasarı kullan!** (2x hasar)
- Zayıf noktasına vur (3x hasar)
- Tüm yetenekleri kullanır
- Faz 3'te çok dikkatli ol
- Minyonları önce temizle
- İyileştirme yeteneğine dikkat et

---

## 📊 Boss Karşılaştırma Tablosu

| Boss | Can | Faz | Zayıf Nokta | Zayıflık | Seviye | Entity |
|------|-----|-----|-------------|----------|--------|--------|
| Goblin Kralı | 150 | 1 | - | - | 1 | Zombie |
| Ork Şefi | 200 | 1 | - | - | 1-2 | Zombie |
| Troll Kralı | 300 | 1 | - | - | 2 | Zombie |
| Ejderha | 500 | 2 | - | - | 3 | Phantom |
| T-Rex | 600 | 1 | - | - | 3 | Ravager |
| Tek Gözlü Dev | 700 | 2 | - | - | 3-4 | Giant |
| Titan Golem | 800 | 3 | ✅ | 🔥 Alev | 4 | Iron Golem |
| Cehennem Ejderi | 900 | 2 | - | 💧 Su | 4 | Phantom |
| Hydra | 850 | 3 | - | ☠️ Zehir | 4-5 | Guardian |
| Phoenix | 600 | 2 | - | - | 4 | Blaze |
| Hiçlik Ejderi | 1200 | 3 | - | - | 5 | Ender Dragon |
| Kaos Titani | 1100 | 3 | - | - | 5 | Wither |
| Khaos Tanrısı | 1000 | 3 | ✅ | 🔥 Alev + ☠️ Zehir | 5 | Wither |

---

## ⚠️ Önemli Notlar

1. **Ritüel Deseni:**
   - Bloklar tam olarak desene uymalı
   - Çağırma Çekirdeği doğru yerde olmalı
   - Boş alanlar (·) hava olmalı

2. **Aktifleştirme:**
   - Sadece Çağırma Çekirdeği'ne sağ tıkla
   - Elinde doğru item olmalı
   - Ritüel deseni doğru olmalı

3. **Boss Savaşı:**
   - Bosslar güçlüdür, hazırlıklı ol!
   - Zayıf noktalarını kullan
   - Faz değişimlerine dikkat et
   - Minyonlara dikkat et

4. **Doğada Spawn:**
   - Çok nadir olur
   - Zorluk seviyesine göre değişir
   - Hazırlıksız yakalanma!

5. **Arena Transformasyonu:**
   - Güçlü bosslar için arena dönüşür
   - Tehlikeler oluşur
   - Kuleler taktiksel avantaj sağlar
   - Dinamik öncelik sistemi ile optimize edilir
   - Config'den ayarlanabilir

6. **BossBar:**
   - Tüm bosslar için gösterilir
   - Can bilgisi güncel tutulur
   - 100 blok mesafe içinde görünür

7. **Cooldown:**
   - Her ritüel konumu için 60 saniye cooldown
   - Aynı yerde tekrar çağırmak için bekle

8. **Config Ayarları:**
   - Tüm arena ayarları `config.yml` dosyasından okunur
   - `/scadmin arena reload` ile anında uygulanır
   - Sunucu yöneticileri tarafından değiştirilebilir

9. **Performans Metrikleri:**
   - Sistem durumu `/scadmin arena status` ile izlenebilir
   - Detaylı performans metrikleri toplanır
   - Metrikler `/scadmin arena reset` ile sıfırlanabilir

---

## 📚 İlgili Dökümanlar

- **03_rituel_sistemi.md** - Ritüel sistemi detayları
- **05_ozel_esyalar.md** - Boss dropları ve özel itemler
- **15_arastirma_sistemi.md** - Tarif kitabı sistemi
- **20_admin_komutlari.md** - Admin komutları

---

**İyi savaşlar! 🗡️**

