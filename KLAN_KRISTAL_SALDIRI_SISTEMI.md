# KLAN KRISTAL SALDIRI SİSTEMİ - DOKÜMANTASYON

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Hasar Sistemi](#hasar-sistemi)
3. [Felaket Boss Saldırıları](#felaket-boss-saldırıları)
4. [Gece Saldırı Dalgası](#gece-saldırı-dalgası)
5. [Vahşi Creeper Sistemi](#vahşi-creeper-sistemi)
6. [Akış Şemaları](#akış-şemaları)

---

## 🎯 GENEL BAKIŞ

Klan kristalleri artık çeşitli kaynaklardan saldırı alabilir:
- **Felaket Bossları**: Kaos Ejderi, Titan Golem, Abyssal Worm, Void Titan, Ice Leviathan
- **Normal Bosslar**: Ork Şefi, Troll Kralı, Goblin Kralı (gece dalgası)
- **Özel Moblar**: Ork, İskelet Şövalye, Troll, Goblin, Kurt Adam (gece dalgası)
- **Vahşi Creeper**: Özel patlama yeteneği olan creeper'lar (gece dalgası)

---

## ⚔️ HASAR SİSTEMİ

### Hasar Hesaplama Formülü

Tüm saldırılar `CrystalAttackHelper` sınıfı üzerinden yönetilir:

```
Final Hasar = Base Hasar × (1 - Zırh Azaltma) - Kalkan Blokları
```

### Hasar Tipleri

#### 1. Felaket Boss Hasarı
- **Base Hasar**: `10.0 × damageMultiplier`
- **Örnek**: 
  - `damageMultiplier = 1.0` → **10.0 hasar**
  - `damageMultiplier = 2.0` → **20.0 hasar**
  - `damageMultiplier = 3.0` → **30.0 hasar**

**Felaket Bossları ve Hasar Miktarları:**
- **Kaos Ejderi**: `10.0 × damageMultiplier` (normalde 1.0-3.0 arası)
- **Titan Golem**: `10.0 × damageMultiplier`
- **Abyssal Worm**: `10.0 × damageMultiplier`
- **Void Titan**: `10.0 × damageMultiplier`
- **Ice Leviathan**: `10.0 × damageMultiplier`

#### 2. Normal Boss Hasarı
- **Seviye 1 Boss**: **5.0 hasar** (Goblin Kralı)
- **Seviye 2 Boss**: **8.0 hasar** (Ork Şefi)
- **Seviye 3 Boss**: **12.0 hasar** (Troll Kralı)
- **Seviye 4 Boss**: **18.0 hasar**
- **Seviye 5 Boss**: **25.0 hasar**

#### 3. Özel Mob Hasarı
- **Ork**: **3.0 hasar**
- **Troll**: **4.0 hasar**
- **İskelet Şövalye**: **2.5 hasar**
- **Goblin**: **1.5 hasar**
- **Kurt Adam**: **2.0 hasar**
- **Karanlık Büyücü**: **1.8 hasar**
- **Dev Örümcek**: **2.2 hasar**

#### 4. Vahşi Creeper Hasarı
- **Base Hasar**: **9.0 hasar** (Normal creeper: ~3.0, 3 katı)
- **Patlama Gücü**: Normal creeper'ın 3 katı (12.0 explosion power)

---

## 🌙 GECE SALDIRI DALGASI

### Zamanlama

**Minecraft Gün Döngüsü:**
- Bir gün = **24000 tick** (20 dakika)
- Gece yarısı = **18000 tick** (15 dakika sonra)
- Güneş doğuşu = **0 tick** (20 dakika sonra)
- Gece süresi = **6000 tick** (5 dakika)

**Dalga Başlangıcı:**
- Gece yarısına yakın başlar (18000 ± 100 tick)
- Tüm dünyaya bildirim gönderilir

**Dalga Bitişi:**
- Güneş doğunca biter (0 ± 100 tick)
- Tüm moblar durur

### Spawn Mekanizması

**Spawn Konumu:**
- Klan sınırından **50 blok ötede**
- Rastgele açı (0-360 derece)
- Yüksek blok üzerinde

**Spawn Hızı:**
- İlk 1 dakika: Her **10 saniyede** bir spawn
- Sonrasında: Her **5 saniyede** bir spawn (hızlanır)

**Spawn Dağılımı:**
- **%20** Boss (Ork Şefi, Troll Kralı, Goblin Kralı)
- **%30** Vahşi Creeper (3-7 adet grup halinde)
- **%50** Özel Mob (Ork, İskelet Şövalye, Troll, Goblin, Kurt Adam)

### AI Davranışı

**Hedef Bulma:**
- 1000 blok yarıçap içindeki en yakın klan kristali
- Kristal yok edilirse yeni hedef bulur

**Hareket:**
- Kristale doğru sürekli hareket
- Takılma durumunda zıplama
- Oyunculara da tepki verir (10 blok yarıçap)

**Saldırı:**
- Kristale 5 blok yaklaşınca saldırır
- Her saldırıda hasar uygular
- Kalkan ve zırh kontrolü yapılır

---

## 💣 VAHŞİ CREEPER SİSTEMİ

### Özellikler

**Patlama Gücü:**
- Normal creeper: 3.0 explosion power
- Vahşi Creeper: **12.0 explosion power** (3 katı)

**Kristal Hasarı:**
- **9.0 hasar** (normal creeper'ın 3 katı)
- 10 blok yarıçap içindeyse kristale hasar verir

### AI Davranışı

**Hedef:**
- Klan kristaline doğru hareket eder
- Oyunculara da tepki verir (10 blok yarıçap)

**Zıplama:**
- Önünde engel varsa zıplar
- Hendeklerden geçebilir
- Takılma durumunda yüksek zıplama

**Patlama Tetikleme:**
- Klan sınırına **3 blok yaklaştığında** patlar
- Sınıra yaklaşamıyorsa (duvar varsa) en yakın noktada patlar
- Oyuncuya yakınsa da patlayabilir

**Takılma Önleme:**
- 1 saniye takılı kalırsa zıplar
- Rastgele yön dener
- Duvarı patlatmaya çalışır

---

## 📊 AKIŞ ŞEMALARI

### 1. Felaket Boss Saldırı Akışı

```
[Felaket Boss Spawn]
    ↓
[GO_CENTER State]
    ↓
[Merkeze Git (50 blok yarıçap)]
    ↓
[hasArrivedCenter = true]
    ↓
[ATTACK_CLAN State]
    ↓
[1500 blok yarıçap içinde klan ara]
    ↓
[Klan Bulundu mu?]
    ├─ Evet → [Kristale Git]
    └─ Hayır → [ATTACK_PLAYER State]
    ↓
[Kristale 5 blok yaklaştı mı?]
    ├─ Evet → [CrystalAttackHelper.attackCrystalByDisaster()]
    │   ↓
    │   [Kalkan Kontrolü]
    │   ├─ Kalkan var → [Engellendi, return]
    │   └─ Kalkan yok → [Hasar Hesapla: 10.0 × multiplier]
    │       ↓
    │       [Zırh Kontrolü]
    │       ↓
    │       [Hasar Uygula]
    │       ↓
    │       [Kristal Yok Edildi mi?]
    │       ├─ Evet → [Yeni Hedef Bul]
    │       └─ Hayır → [Devam Et]
    └─ Hayır → [Kristale Doğru Hareket Et]
```

### 2. Gece Saldırı Dalgası Akışı

```
[Her 5 Saniyede Kontrol]
    ↓
[Gece Yarısı mı? (18000 ± 100)]
    ├─ Evet → [Dalga Başlat]
    │   ↓
    │   [Tüm Klanlar İçin]
    │   ↓
    │   [Spawn Konumu Bul (Sınır + 50 blok)]
    │   ↓
    │   [Mob Tipi Seç]
    │   ├─ %20 → [Boss Spawn]
    │   ├─ %30 → [Vahşi Creeper Spawn (3-7 adet)]
    │   └─ %50 → [Özel Mob Spawn]
    │   ↓
    │   [MobClanAttackAI.attachAI()]
    │   ↓
    │   [Her 10 Saniyede Yeni Spawn]
    │   │   (1 dakika sonra 5 saniyeye düşer)
    │
    └─ Hayır → [Devam Et]
    ↓
[Güneş Doğdu mu? (0 ± 100)]
    ├─ Evet → [Dalga Durdur]
    └─ Hayır → [Devam Et]
```

### 3. Mob Klan Saldırı AI Akışı

```
[Mob Spawn]
    ↓
[MobClanAttackAI.attachAI()]
    ↓
[Her Tick AI Çalıştır]
    ↓
[Entity Hala Var mı?]
    ├─ Hayır → [AI Kaldır, Durdur]
    └─ Evet → [Devam Et]
    ↓
[Klan Hala Var mı?]
    ├─ Hayır → [En Yakın Klan Bul (1000 blok)]
    └─ Evet → [Devam Et]
    ↓
[Her 1 Saniyede Hedef Güncelle]
    ↓
[Kristale Doğru Hareket Et]
    ↓
[Kristale 5 Blok Yaklaştı mı?]
    ├─ Evet → [Saldır]
    │   ↓
    │   [Boss mu?]
    │   ├─ Evet → [CrystalAttackHelper.attackCrystalByBoss()]
    │   └─ Hayır → [CrystalAttackHelper.attackCrystalBySpecialMob()]
    │       ↓
    │       [Hasar Uygula]
    └─ Hayır → [Devam Et]
    ↓
[Takıldı mı? (1 saniye)]
    ├─ Evet → [Zıpla, Rastgele Yön Dene]
    └─ Hayır → [Devam Et]
```

### 4. Vahşi Creeper AI Akışı

```
[Vahşi Creeper Spawn]
    ↓
[AI Başlat]
    ↓
[Her Tick Kontrol]
    ↓
[Klan Sınırına 3 Blok Yakın mı?]
    ├─ Evet → [Patla!]
    │   ↓
    │   [12.0 Explosion Power]
    │   ↓
    │   [Kristal 10 Blok İçinde mi?]
    │   ├─ Evet → [CrystalAttackHelper.attackCrystalByWildCreeper()]
    │   └─ Hayır → [Sadece Patlama]
    │   ↓
    │   [Creeper Yok Et]
    │
    └─ Hayır → [Devam Et]
    ↓
[Oyuncu 10 Blok Yakında mı?]
    ├─ Evet → [Oyuncuya Doğru Git]
    └─ Hayır → [Kristale Doğru Git]
    ↓
[Önünde Engel Var mı?]
    ├─ Evet → [Zıpla]
    └─ Hayır → [Normal Hareket]
    ↓
[Takıldı mı? (1 saniye)]
    ├─ Evet → [Yüksek Zıplama, Rastgele Yön]
    └─ Hayır → [Devam Et]
```

---

## 📈 HASAR ÖZET TABLOSU

| Saldırgan Tipi | Base Hasar | Örnek Hasar (Zırh Yok) | Örnek Hasar (%50 Zırh) |
|----------------|------------|-------------------------|------------------------|
| **Felaket Boss (multiplier=1.0)** | 10.0 | 10.0 | 5.0 |
| **Felaket Boss (multiplier=2.0)** | 20.0 | 20.0 | 10.0 |
| **Felaket Boss (multiplier=3.0)** | 30.0 | 30.0 | 15.0 |
| **Boss Seviye 1** | 5.0 | 5.0 | 2.5 |
| **Boss Seviye 2** | 8.0 | 8.0 | 4.0 |
| **Boss Seviye 3** | 12.0 | 12.0 | 6.0 |
| **Boss Seviye 4** | 18.0 | 18.0 | 9.0 |
| **Boss Seviye 5** | 25.0 | 25.0 | 12.5 |
| **Ork** | 3.0 | 3.0 | 1.5 |
| **Troll** | 4.0 | 4.0 | 2.0 |
| **İskelet Şövalye** | 2.5 | 2.5 | 1.25 |
| **Goblin** | 1.5 | 1.5 | 0.75 |
| **Kurt Adam** | 2.0 | 2.0 | 1.0 |
| **Vahşi Creeper** | 9.0 | 9.0 | 4.5 |

---

## 🔧 TEKNİK DETAYLAR

### CrystalAttackHelper

**Sınıf**: `me.mami.stratocraft.util.CrystalAttackHelper`

**Metodlar:**
- `attackCrystalByDisaster()`: Felaket boss saldırısı
- `attackCrystalByBoss()`: Normal boss saldırısı
- `attackCrystalBySpecialMob()`: Özel mob saldırısı
- `attackCrystalByWildCreeper()`: Vahşi creeper saldırısı

**AttackResult:**
- `success`: Saldırı başarılı mı?
- `blocked`: Kalkan tarafından engellendi mi?
- `damageDealt`: Verilen hasar
- `currentHealth`: Kalan can
- `maxHealth`: Maksimum can
- `destroyed`: Kristal yok edildi mi?

### NightWaveManager

**Sınıf**: `me.mami.stratocraft.manager.NightWaveManager`

**Başlatma:**
```java
NightWaveManager waveManager = new NightWaveManager(plugin, territoryManager, mobManager, bossManager);
waveManager.start();
```

**Özellikler:**
- Her 5 saniyede bir gece kontrolü
- Otomatik dalga başlatma/durdurma
- Spawn hızı artışı (1 dakika sonra)

### MobClanAttackAI

**Sınıf**: `me.mami.stratocraft.util.MobClanAttackAI`

**Kullanım:**
```java
MobClanAttackAI.attachAI(entity, targetClan, plugin);
```

**Özellikler:**
- Her tick AI çalıştırma
- Otomatik hedef bulma
- Takılma önleme

### WildCreeper

**Sınıf**: `me.mami.stratocraft.entity.WildCreeper`

**Kullanım:**
```java
WildCreeper.spawnWildCreeper(location, targetClan, plugin);
```

**Özellikler:**
- 3 kat güçlü patlama
- Zıplama yeteneği
- Sınır algılama (3 blok)

---

## 🎮 OYUNCU DENEYİMİ

### Bildirimler

**Klan Üyelerine:**
```
§c⚠ Kristal hasar aldı! [Felaket Boss] 20.0 hasar - Can: 80.0/100.0 (80.0%)
```

**Sunucu Geneli:**
```
§c§l⚠ GECE SALDIRI DALGASI BAŞLADI! ⚠
§7Bosslar ve özel moblar klanlara saldırıyor!
```

### Görsel Efektler

**Hasar Partikülleri:**
- Can > %50: Yeşil partikül (VILLAGER_HAPPY)
- Can %25-50: Kırmızı partikül (DAMAGE_INDICATOR)
- Can < %25: Lava partikülü (LAVA)

**Kalkan Engelleme:**
- Barrier partikülü (BLOCK_CRACK)

---

## 📝 SONUÇ

Bu sistem sayesinde:
- ✅ Tüm felaket bossları kristallere saldırıyor
- ✅ Gece dalgası otomatik başlıyor
- ✅ Boss ve özel moblar klanlara saldırıyor
- ✅ Vahşi creeper'lar duvarları patlatıyor
- ✅ Hasar sistemi merkezi ve tutarlı
- ✅ Kalkan ve zırh sistemi çalışıyor

**Tüm saldırılar `CrystalAttackHelper` üzerinden yönetiliyor, bu sayede hasar hesaplamaları tutarlı ve bakımı kolay!**

---

## 🎯 HASAR ÖZETİ - KİM NE KADAR HASAR VERİYOR?

### Felaket Bossları
| Boss | Hasar Formülü | Örnek (multiplier=2.0) | Örnek (%50 Zırh) |
|------|---------------|------------------------|------------------|
| **Kaos Ejderi** | `10.0 × multiplier` | **20.0** | 10.0 |
| **Titan Golem** | `10.0 × multiplier` | **20.0** | 10.0 |
| **Abyssal Worm** | `10.0 × multiplier` | **20.0** | 10.0 |
| **Void Titan** | `10.0 × multiplier` | **20.0** | 10.0 |
| **Ice Leviathan** | `10.0 × multiplier` | **20.0** | 10.0 |

**Saldırı Şekli:** Kristale 5 blok yaklaşınca fiziksel saldırı

---

### Normal Bosslar (Gece Dalgası)
| Boss | Seviye | Base Hasar | Örnek (%50 Zırh) |
|------|--------|------------|------------------|
| **Goblin Kralı** | 1 | **5.0** | 2.5 |
| **Ork Şefi** | 2 | **8.0** | 4.0 |
| **Troll Kralı** | 3 | **12.0** | 6.0 |

**Saldırı Şekli:** Kristale 5 blok yaklaşınca fiziksel saldırı

---

### Özel Moblar (Gece Dalgası)
| Mob | Base Hasar | Örnek (%50 Zırh) |
|-----|------------|------------------|
| **Troll** | **4.0** | 2.0 |
| **Ork** | **3.0** | 1.5 |
| **İskelet Şövalye** | **2.5** | 1.25 |
| **Kurt Adam** | **2.0** | 1.0 |
| **Dev Örümcek** | **2.2** | 1.1 |
| **Karanlık Büyücü** | **1.8** | 0.9 |
| **Goblin** | **1.5** | 0.75 |

**Saldırı Şekli:** Kristale 5 blok yaklaşınca fiziksel saldırı

---

### Vahşi Creeper (Gece Dalgası)
| Özellik | Değer |
|---------|-------|
| **Base Hasar** | **9.0** |
| **Patlama Gücü** | **12.0** (normal creeper: 4.0) |
| **Patlama Tetikleme** | Klan sınırına 3 blok yaklaşınca |
| **Kristal Hasar Yarıçapı** | 10 blok |

**Saldırı Şekli:** Klan sınırına yaklaşınca patlama (3 kat güçlü)

---

## 🔄 SİSTEM AKIŞ ŞEMASI

### Tam Sistem Akışı

```
[Oyun Başlatıldı]
    ↓
[NightWaveManager.start()]
    ↓
[Her 5 Saniyede Gece Kontrolü]
    ↓
[Gece Yarısı mı? (18000 ± 100)]
    ├─ Evet → [Gece Dalgası Başlat]
    │   ↓
    │   [Tüm Klanlar İçin]
    │   ↓
    │   [Spawn Konumu Bul (Sınır + 50 blok)]
    │   ↓
    │   [Mob Tipi Seç]
    │   ├─ %20 → [Boss Spawn]
    │   │   ↓
    │   │   [MobClanAttackAI.attachAI()]
    │   │   ↓
    │   │   [Her Tick: Kristale Git]
    │   │   ↓
    │   │   [5 Blok Yaklaşınca]
    │   │   ↓
    │   │   [CrystalAttackHelper.attackCrystalByBoss()]
    │   │   ↓
    │   │   [Hasar: 5.0-25.0 (seviyeye göre)]
    │   │
    │   ├─ %30 → [Vahşi Creeper Spawn (3-7 adet)]
    │   │   ↓
    │   │   [WildCreeper AI]
    │   │   ↓
    │   │   [Klan Sınırına 3 Blok Yaklaşınca]
    │   │   ↓
    │   │   [Patla! (12.0 explosion power)]
    │   │   ↓
    │   │   [CrystalAttackHelper.attackCrystalByWildCreeper()]
    │   │   ↓
    │   │   [Hasar: 9.0]
    │   │
    │   └─ %50 → [Özel Mob Spawn]
    │       ↓
    │       [MobClanAttackAI.attachAI()]
    │       ↓
    │       [Her Tick: Kristale Git]
    │       ↓
    │       [5 Blok Yaklaşınca]
    │       ↓
    │       [CrystalAttackHelper.attackCrystalBySpecialMob()]
    │       ↓
    │       [Hasar: 1.5-4.0 (mob tipine göre)]
    │
    │   [Her 10 Saniyede Yeni Spawn]
    │   │   (1 dakika sonra 5 saniyeye düşer)
    │
    └─ Hayır → [Devam Et]
    ↓
[Güneş Doğdu mu? (0 ± 100)]
    ├─ Evet → [Dalga Durdur]
    └─ Hayır → [Devam Et]
```

### Felaket Boss Saldırı Akışı

```
[Felaket Boss Spawn]
    ↓
[GO_CENTER State]
    ↓
[Merkeze Git (50 blok yarıçap)]
    ↓
[hasArrivedCenter = true]
    ↓
[ATTACK_CLAN State]
    ↓
[1500 blok yarıçap içinde klan ara]
    ↓
[Klan Bulundu mu?]
    ├─ Evet → [Kristale Git]
    └─ Hayır → [ATTACK_PLAYER State]
    ↓
[Kristale 5 Blok Yaklaştı mı?]
    ├─ Evet → [CrystalAttackHelper.attackCrystalByDisaster()]
    │   ↓
    │   [Kalkan Kontrolü]
    │   ├─ Kalkan var → [Engellendi, return]
    │   └─ Kalkan yok → [Hasar: 10.0 × multiplier]
    │       ↓
    │       [Zırh Kontrolü]
    │       ↓
    │       [Final Hasar = Base × (1 - Zırh Azaltma)]
    │       ↓
    │       [targetClan.damageCrystal(finalDamage)]
    │       ↓
    │       [Kristal Yok Edildi mi?]
    │       ├─ Evet → [Yeni Hedef Bul]
    │       └─ Hayır → [Devam Et]
    └─ Hayır → [Kristale Doğru Hareket Et]
```

---

## 🛠️ KURULUM VE KULLANIM

### Main.java'da Başlatma

```java
// onEnable() içinde
nightWaveManager = new NightWaveManager(
    this, territoryManager, mobManager, bossManager);
nightWaveManager.start();

// onDisable() içinde
if (nightWaveManager != null) {
    nightWaveManager.stop();
}
```

### Manuel Dalga Başlatma (Test İçin)

```java
// Belirli bir dünya için dalga başlat
World world = Bukkit.getWorld("world");
// NightWaveManager içinde private metod, gerekirse public yapılabilir
```

---

## 📊 PERFORMANS NOTLARI

- **Gece Kontrolü**: Her 5 saniyede bir (100 tick)
- **Spawn Kontrolü**: Her 10 saniyede bir (200 tick), 1 dakika sonra 5 saniyeye düşer
- **AI Tick**: Her tick (1L) - sadece aktif moblar için
- **Hedef Güncelleme**: Her 1 saniyede bir (20 tick)

**Optimizasyon:**
- Sadece aktif dalga sırasında AI çalışır
- Dalga bittiğinde tüm AI'lar otomatik durur
- Entity öldüğünde AI otomatik temizlenir

---

## ✅ TEST SENARYOLARI

1. **Felaket Boss Testi:**
   - `/stratocraft disaster start 3 CATASTROPHIC_CHAOS_DRAGON 2`
   - Boss merkeze gitmeli
   - Merkeze ulaşınca klanlara saldırmalı
   - Kristale 5 blok yaklaşınca hasar vermeli

2. **Gece Dalgası Testi:**
   - `/time set 18000` (gece yarısı)
   - Dalga başlamalı
   - Moblar spawn olmalı
   - Klanlara saldırmalı
   - `/time set 0` (güneş doğuşu)
   - Dalga durmalı

3. **Vahşi Creeper Testi:**
   - Gece dalgası başladığında
   - Vahşi creeper spawn olmalı
   - Klan sınırına yaklaşınca patlamalı
   - Kristale hasar vermeli

---

**Sistem tamamen hazır ve çalışır durumda! 🎉**

