# 🎮 Boss Sistemi - Detaylı Rehber

## 📋 Genel Bakış

Stratocraft'ta 10 farklı boss bulunmaktadır. Her boss:
- **Ritüel ile çağrılabilir** (özel blok deseni + item)
- **Doğada nadiren spawn olabilir** (zorluk seviyesine göre)
- **Özel hareketler** kullanır (ateş, yıldırım, patlama, blok fırlatma, zehir vb.)
- **Zayıf noktaları** olabilir (güçlü bosslar için)
- **Faz sistemi** olabilir (güçlü bosslar için 2-3 faz)

---

## 🐉 Boss Listesi

### Seviye 1 Bosslar

#### 1. **Goblin Kralı** (GOBLIN_KING)
- **Can:** 150 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Özel Hareketler:**
  - Koşu Saldırısı (CHARGE)
  - Minyon Çağırma (SUMMON_MINIONS)
  - Patlama (EXPLOSION)
- **Ritüel Deseni:** 3x3 Cobblestone + Merkez Gold Block
- **Aktifleştirme Item:** Rotten Flesh
- **Ritüel Yapılışı:**
```
C C C
C G C  (C = Cobblestone, G = Gold Block)
C C C
```
Merkez bloğa (Gold Block) Rotten Flesh ile sağ tıkla.

---

#### 2. **Ork Şefi** (ORC_CHIEF)
- **Can:** 200 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Özel Hareketler:**
  - Koşu Saldırısı (CHARGE)
  - Blok Fırlatma (BLOCK_THROW)
  - Minyon Çağırma (SUMMON_MINIONS)
- **Ritüel Deseni:** 3x3 Stone + Merkez Iron Block
- **Aktifleştirme Item:** Iron Sword
- **Ritüel Yapılışı:**
```
S S S
S I S  (S = Stone, I = Iron Block)
S S S
```
Merkez bloğa (Iron Block) Iron Sword ile sağ tıkla.

---

### Seviye 2 Bosslar

#### 3. **Troll Kralı** (TROLL_KING)
- **Can:** 300 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Özel Hareketler:**
  - Blok Fırlatma (BLOCK_THROW)
  - Şok Dalgası (SHOCKWAVE)
  - Kendini İyileştirme (HEAL)
- **Ritüel Deseni:** 3x3 Stone Bricks + Merkez Diamond Block
- **Aktifleştirme Item:** Stone Axe
- **Ritüel Yapılışı:**
```
B B B
B D B  (B = Stone Bricks, D = Diamond Block)
B B B
```
Merkez bloğa (Diamond Block) Stone Axe ile sağ tıkla.

---

### Seviye 3 Bosslar

#### 4. **Ejderha** (DRAGON) - 2 Faz
- **Can:** 500 HP
- **Faz:** 2 (Faz 1: %100-50, Faz 2: %50-0)
- **Zayıf Nokta:** Yok
- **Özel Hareketler:**
  - **Faz 1:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Işınlanma (TELEPORT)
    - Patlama (EXPLOSION)
  - **Faz 2:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Işınlanma (TELEPORT)
    - Minyon Çağırma (SUMMON_MINIONS)
- **Ritüel Deseni:** 5x5 Obsidian + Merkez Emerald Block
- **Aktifleştirme Item:** Dragon Egg
- **Ritüel Yapılışı:**
```
O O O O O
O · · · O
O · E · O  (O = Obsidian, E = Emerald Block, · = Boş)
O · · · O
O O O O O
```
Merkez bloğa (Emerald Block) Dragon Egg ile sağ tıkla.

---

#### 5. **T-Rex** (TREX)
- **Can:** 600 HP
- **Faz:** 1
- **Zayıf Nokta:** Yok
- **Özel Hareketler:**
  - Koşu Saldırısı (CHARGE)
  - Şok Dalgası (SHOCKWAVE)
  - Patlama (EXPLOSION)
- **Ritüel Deseni:** 5x5 Stone + Merkez Gold Block + Köşeler Diamond
- **Aktifleştirme Item:** Bone
- **Ritüel Yapılışı:**
```
D S S S D
S · · · S
S · G · S  (D = Diamond Block, S = Stone, G = Gold Block)
S · · · S
D S S S D
```
Merkez bloğa (Gold Block) Bone ile sağ tıkla.

---

#### 6. **Tek Gözlü Dev** (CYCLOPS) - 2 Faz
- **Can:** 700 HP
- **Faz:** 2 (Faz 1: %100-66, Faz 2: %66-0)
- **Zayıf Nokta:** Yok
- **Özel Hareketler:**
  - **Faz 1:**
    - Blok Fırlatma (BLOCK_THROW)
    - Şok Dalgası (SHOCKWAVE)
    - Koşu Saldırısı (CHARGE)
  - **Faz 2:**
    - Blok Fırlatma (BLOCK_THROW)
    - Şok Dalgası (SHOCKWAVE)
    - Patlama (EXPLOSION)
    - Kendini İyileştirme (HEAL)
- **Ritüel Deseni:** 5x5 Stone Bricks + Merkez Emerald Block + Köşeler Gold
- **Aktifleştirme Item:** Ender Eye
- **Ritüel Yapılışı:**
```
G B B B G
B · · · B
B · E · B  (G = Gold Block, B = Stone Bricks, E = Emerald Block)
B · · · B
G B B B G
```
Merkez bloğa (Emerald Block) Ender Eye ile sağ tıkla.

---

### Seviye 4 Bosslar

#### 7. **Titan Golem** (TITAN_GOLEM) - 3 Faz + Zayıf Nokta
- **Can:** 800 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** 🔥 **Alev** (2x hasar)
- **Özel Hareketler:**
  - **Faz 1:**
    - Blok Fırlatma (BLOCK_THROW)
    - Şok Dalgası (SHOCKWAVE)
    - Patlama (EXPLOSION)
  - **Faz 2:**
    - Blok Fırlatma (BLOCK_THROW)
    - Şok Dalgası (SHOCKWAVE)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Kendini İyileştirme (HEAL)
  - **Faz 3:**
    - Blok Fırlatma (BLOCK_THROW)
    - Şok Dalgası (SHOCKWAVE)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Patlama (EXPLOSION)
    - Minyon Çağırma (SUMMON_MINIONS)
- **Ritüel Deseni:** 7x7 Obsidian + Merkez Netherite Block + Köşeler Diamond
- **Aktifleştirme Item:** Nether Star
- **Ritüel Yapılışı:**
```
D O O O O O O D
O · · · · · · O
O · · · · · · O
O · · N · · · O  (D = Diamond, O = Obsidian, N = Netherite Block)
O · · · · · · O
O · · · · · · O
D O O O O O O D
```
Merkez bloğa (Netherite Block) Nether Star ile sağ tıkla.

---

#### 8. **Cehennem Ejderi** (HELL_DRAGON) - 2 Faz + Zayıf Nokta
- **Can:** 900 HP
- **Faz:** 2 (Faz 1: %100-50, Faz 2: %50-0)
- **Zayıf Nokta:** 💧 **Su** (2x hasar)
- **Özel Hareketler:**
  - **Faz 1:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Işınlanma (TELEPORT)
    - Patlama (EXPLOSION)
  - **Faz 2:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Zehir Bulutu (POISON_CLOUD)
    - Işınlanma (TELEPORT)
- **Ritüel Deseni:** 7x7 Netherrack + Merkez Beacon + Köşeler Obsidian
- **Aktifleştirme Item:** Blaze Rod
- **Ritüel Yapılışı:**
```
O N N N N N N O
N · · · · · · N
N · · · · · · N
N · · ★ · · · N  (O = Obsidian, N = Netherrack, ★ = Beacon)
N · · · · · · N
N · · · · · · N
O N N N N N N O
```
Merkez bloğa (Beacon) Blaze Rod ile sağ tıkla.

---

#### 9. **Hydra** (HYDRA) - 3 Faz + Zayıf Nokta
- **Can:** 1000 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Nokta:** ☠️ **Zehir** (2x hasar)
- **Özel Hareketler:**
  - **Faz 1:**
    - Zehir Bulutu (POISON_CLOUD)
    - Işınlanma (TELEPORT)
    - Minyon Çağırma (SUMMON_MINIONS)
  - **Faz 2:**
    - Zehir Bulutu (POISON_CLOUD)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Kendini İyileştirme (HEAL)
    - Minyon Çağırma (SUMMON_MINIONS)
  - **Faz 3:**
    - Zehir Bulutu (POISON_CLOUD)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Patlama (EXPLOSION)
    - Kendini İyileştirme (HEAL)
    - Minyon Çağırma (SUMMON_MINIONS)
- **Ritüel Deseni:** 7x7 Prismarine + Merkez Conduit + Köşeler Emerald
- **Aktifleştirme Item:** Heart of the Sea
- **Ritüel Yapılışı:**
```
E P P P P P P E
P · · · · · · P
P · · · · · · P
P · · ● · · · P  (E = Emerald, P = Prismarine, ● = Conduit)
P · · · · · · P
P · · · · · · P
E P P P P P P E
```
Merkez bloğa (Conduit) Heart of the Sea ile sağ tıkla.

---

### Seviye 5 Bosslar

#### 10. **Khaos Tanrısı** (CHAOS_GOD) - 3 Faz + Zayıf Noktalar
- **Can:** 1000 HP
- **Faz:** 3 (Faz 1: %100-66, Faz 2: %66-33, Faz 3: %33-0)
- **Zayıf Noktalar:** 🔥 **Alev** + ☠️ **Zehir** (2x hasar)
- **Özel Hareketler:**
  - **Faz 1:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Işınlanma (TELEPORT)
  - **Faz 2:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Zehir Bulutu (POISON_CLOUD)
    - Patlama (EXPLOSION)
    - Kendini İyileştirme (HEAL)
  - **Faz 3:**
    - Ateş Püskürtme (FIRE_BREATH)
    - Yıldırım Atma (LIGHTNING_STRIKE)
    - Zehir Bulutu (POISON_CLOUD)
    - Patlama (EXPLOSION)
    - Şok Dalgası (SHOCKWAVE)
    - Kendini İyileştirme (HEAL)
    - Minyon Çağırma (SUMMON_MINIONS)
- **Ritüel Deseni:** 9x9 Bedrock + Merkez End Crystal + Köşeler Netherite + Kenarlar Obsidian
- **Aktifleştirme Item:** Nether Star
- **Ritüel Yapılışı:**
```
N B B B B B B B N
B O · · · · · O B
B · · · · · · · B
B · · · · · · · B
B · · · ◆ · · · B  (N = Netherite, B = Bedrock, O = Obsidian, ◆ = End Crystal)
B · · · · · · · B
B · · · · · · · B
B O · · · · · O B
N B B B B B B B N
```
Merkez bloğa (End Crystal) Nether Star ile sağ tıkla.

---

## 🎯 Özel Hareketler Açıklaması

### 1. **Ateş Püskürtme (FIRE_BREATH)**
- Boss hedefe doğru ateş püskürtür
- 10 blok mesafeye kadar ateş partikülleri
- Oyunculara 60 tick (3 saniye) yanma efekti
- 2 HP hasar

### 2. **Patlama (EXPLOSION)**
- Boss'un konumunda 3 blok yarıçaplı patlama
- Blok kırmaz
- Yüksek hasar

### 3. **Yıldırım Atma (LIGHTNING_STRIKE)**
- Hedef oyuncunun konumuna yıldırım düşer
- 5 HP hasar
- Elektrik partikülleri

### 4. **Blok Fırlatma (BLOCK_THROW)**
- Boss'un 3x3 alanındaki blokları alır ve hedefe fırlatır
- FallingBlock olarak spawn olur
- Hasar verir

### 5. **Zehir Bulutu (POISON_CLOUD)**
- 5 blok yarıçaplı alanda zehir bulutu oluşturur
- Yakındaki oyunculara zehir efekti (100 tick, seviye 1)
- 2 HP/saniye hasar

### 6. **Işınlanma (TELEPORT)**
- Boss hedef oyuncunun yakınına ışınlanır
- Portal partikülleri
- Sürpriz saldırı için

### 7. **Koşu Saldırısı (CHARGE)**
- Boss hedefe doğru hızlıca koşar
- Yüksek hız
- Çarpışma hasarı

### 8. **Minyon Çağırma (SUMMON_MINIONS)**
- Boss tipine göre minyonlar spawn olur
- Goblin Kralı → Goblinler
- Ork Şefi → Orklar
- vb.

### 9. **Kendini İyileştirme (HEAL)**
- Boss kendini %20 iyileştirir
- Kalp partikülleri
- Kritik durumlarda kullanır

### 10. **Şok Dalgası (SHOCKWAVE)**
- 5 blok yarıçaplı şok dalgası
- Oyuncuları iter
- 3 HP hasar

---

## ⚔️ Zayıf Noktalar

Bazı bossların zayıf noktaları vardır. Bu zayıf noktalara hasar verildiğinde **2x hasar** alırlar.

### Zayıf Nokta Türleri:

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

## 🔄 Faz Sistemi

Güçlü bosslar faz sistemi kullanır. Can %'sine göre faz değişir:

### 2 Fazlı Bosslar:
- **Faz 1:** %100-50 can
- **Faz 2:** %50-0 can

### 3 Fazlı Bosslar:
- **Faz 1:** %100-66 can
- **Faz 2:** %66-33 can
- **Faz 3:** %33-0 can

Faz değiştiğinde:
- Duyuru mesajı gösterilir
- Yeni yetenekler aktif olur
- Ses efekti çalar

---

## 🌍 Doğada Spawn

Bosslar doğada da nadiren spawn olabilir:

- **Seviye 1:** %1 şans → Goblin Kralı, Ork Şefi
- **Seviye 2:** %1.5 şans → Ork Şefi, Troll Kralı
- **Seviye 3:** %2 şans → Ejderha, T-Rex, Tek Gözlü Dev
- **Seviye 4:** %2.5 şans → Tek Gözlü Dev, Titan Golem, Cehennem Ejderi, Hydra
- **Seviye 5:** %3 şans → Hydra, Khaos Tanrısı

---

## 🎮 Admin Komutları

### Boss Listesi
```bash
/stratocraft boss list
```

### Boss Spawn Et
```bash
/stratocraft boss spawn <type>
```
Örnek: `/stratocraft boss spawn DRAGON`

### Ritüel Deseni Göster
```bash
/stratocraft boss ritual <type>
```
Örnek: `/stratocraft boss ritual CHAOS_GOD`

---

## 📝 Ritüel Yapım Adımları

1. **Deseni Yerleştir:**
   - Boss'un ritüel desenini yere yerleştir
   - Merkez bloğu doğru yere koy
   - Tüm blokların doğru olduğundan emin ol

2. **Aktifleştirme Item'ı Al:**
   - Boss'un aktifleştirme item'ını envanterinde bulundur
   - Örnek: Dragon Egg, Nether Star, vb.

3. **Ritüel Aktifleştir:**
   - Merkez bloğa (ritüel deseninin merkezi) sağ tıkla
   - Elinde aktifleştirme item'ı olmalı
   - Boss spawn olur!

4. **Cooldown:**
   - Her ritüel konumu için 1 dakika cooldown var
   - Aynı yerde tekrar çağırmak için bekle

---

## ⚠️ Önemli Notlar

1. **Ritüel Deseni:**
   - Bloklar tam olarak desene uymalı
   - Merkez blok doğru yerde olmalı
   - Boş alanlar (·) hava olmalı

2. **Aktifleştirme:**
   - Sadece merkez bloğa sağ tıkla
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

---

## 🎯 Strateji İpuçları

1. **Zayıf Noktaları Kullan:**
   - Titan Golem → Alev hasarı
   - Cehennem Ejderi → Su hasarı
   - Hydra → Zehir hasarı
   - Khaos Tanrısı → Alev + Zehir hasarı

2. **Faz Değişimlerine Hazır Ol:**
   - Faz değiştiğinde yeni yetenekler gelir
   - Daha agresif olur
   - Dikkatli ol!

3. **Minyonları Önce Öldür:**
   - Minyonlar rahatsız edici olabilir
   - Önce onları temizle
   - Sonra boss'a odaklan

4. **Mesafe Kontrolü:**
   - Bazı bosslar ışınlanabilir
   - Mesafeyi koru
   - Ateş püskürtmelerinden kaç

---

## 📊 Boss Karşılaştırma Tablosu

| Boss | Can | Faz | Zayıf Nokta | Seviye |
|------|-----|-----|-------------|--------|
| Goblin Kralı | 150 | 1 | - | 1 |
| Ork Şefi | 200 | 1 | - | 1-2 |
| Troll Kralı | 300 | 1 | - | 2 |
| Ejderha | 500 | 2 | - | 3 |
| T-Rex | 600 | 1 | - | 3 |
| Tek Gözlü Dev | 700 | 2 | - | 3-4 |
| Titan Golem | 800 | 3 | 🔥 Alev | 4 |
| Cehennem Ejderi | 900 | 2 | 💧 Su | 4 |
| Hydra | 1000 | 3 | ☠️ Zehir | 4-5 |
| Khaos Tanrısı | 1000 | 3 | 🔥 Alev + ☠️ Zehir | 5 |

---

## 🏟️ ARENA TRANSFORMASYON SİSTEMİ

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
- **Toplam Artış:** Önceki sisteme göre **25-30 kat daha fazla** tehlike!

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

#### Mesafe Bazlı Aktivasyon
- **Aktif Arena:** Oyuncu 100 blok içindeyse
- **Pasif Arena:** Oyuncu 100+ blok uzaktaysa (hiçbir işlem yapılmaz)
- **Önceliklendirme:** En yakın 20 arena her döngüde işlenir

#### Chunk Kontrolü
- Yüklü olmayan chunk'larda işlem yapılmaz
- Performans için kritik optimizasyon

#### Merkezi Task Sistemi
- Her arena için ayrı task yok
- Tek merkezi task tüm arenaları yönetir
- Her arena kendi döngü sayacını tutar

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

| Özellik | Değer |
|---------|-------|
| **Yayılma Hızı** | 1.2 blok / 2 saniye |
| **Kule Oluşturma** | İlk: Hemen, Sonra: Her 60 saniyede |
| **Tehlike Oluşturma** | Her 2 saniyede 12-19 tehlike |
| **Blok Dönüşümü** | Her 2 saniyede 8 blok |
| **Aktif Menzil** | 100 blok (oyuncu mesafesi) |
| **Maksimum Arena** | 50 eşzamanlı arena |

### ⚠️ Önemli Notlar

1. **Boss Hareketi:** Boss 5+ blok hareket ederse, arena yeni konumdan başlar
2. **Boss Ölümü:** Boss öldüğünde arena transformasyonu durur
3. **Performans:** Uzak arenalar pasif kalır, performans etkilenmez
4. **Chunk Yükleme:** Chunk yüklü değilse işlem yapılmaz

---

**İyi savaşlar! 🗡️**

