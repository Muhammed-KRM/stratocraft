# 23. Config Değerleri ve Oyun Dengesi

Bu döküman, `config.yml` dosyasındaki tüm oyun dengesi ve ayarlanabilir değerleri açıklar. Her değerin ne işe yaradığı, artırıldığında/azaltıldığında ne olacağı ve önerilen aralıkları detaylıca anlatılır.

---

## 📋 İçindekiler

1. [Batarya Sistemi](#batarya-sistemi)
2. [Training (Antrenman) Sistemi](#training-antrenman-sistemi)
3. [Power System (Güç Sistemi)](#power-system-güç-sistemi)
4. [Main System (Ana Sistem)](#main-system-ana-sistem)
5. [Ritüel Sistemi](#ritüel-sistemi)
6. [Task Intervals (Görev Aralıkları)](#task-intervals-görev-aralıkları)

---

## 🔋 Batarya Sistemi

### Hasar Değerleri

#### Seviye Bazlı Temel Hasar

```yaml
game-balance:
  battery:
    damage:
      level1-base: 5.0
      level2-base: 10.0
      level3-base: 50.0
      level4-base: 100.0
      level5-base: 300.0
```

**Açıklama:** Her batarya seviyesi için temel hasar değeri. Bu değerler yakıt çarpanları ile çarpılarak final hasar hesaplanır.

**Etkisi:**
- **Artırılırsa:** Bataryalar daha fazla hasar verir, oyun daha kolaylaşır (PvE için).
- **Azaltılırsa:** Bataryalar daha az hasar verir, oyun daha zorlaşır.

**Önerilen Aralık:**
- Level 1: 3.0 - 8.0
- Level 2: 8.0 - 15.0
- Level 3: 40.0 - 70.0
- Level 4: 80.0 - 150.0
- Level 5: 250.0 - 400.0

#### Özel Batarya Hasar Değerleri

```yaml
game-balance:
  battery:
    damage:
      special:
        level3-lightning-storm: 50.0
        level3-ice-age: 70.0
        level4-tesla-tower: 100.0
        level4-death-cloud: 120.0
        level4-electric-shield: 70.0
        level5-apocalypse-reactor: 300.0
        level5-boss-killer: 300.0
        level5-area-destroyer: 300.0
        level5-mountain-destroyer: 300.0
```

**Açıklama:** Özel batarya tipleri için özel hasar değerleri. Bu bataryalar genellikle daha güçlü veya özel etkilere sahiptir.

**Etkisi:**
- **Artırılırsa:** Özel bataryalar daha güçlü olur, boss savaşları ve zorlu durumlar daha kolaylaşır.
- **Azaltılırsa:** Özel bataryalar zayıflar, oyun daha zorlaşır.

**Önerilen Aralık:**
- Level 3 özel: 40.0 - 90.0
- Level 4 özel: 80.0 - 150.0
- Level 5 özel: 250.0 - 400.0

### Radius (Yarıçap) Değerleri

```yaml
game-balance:
  battery:
    radius:
      level1-base: 5
      level2-base: 5
      level3-base: 7
      level4-base: 10
      level5-base: 40
```

**Açıklama:** Bataryaların etki alanı yarıçapı (blok cinsinden). Bu değer yakıt çarpanları ile çarpılarak final yarıçap hesaplanır.

**Etkisi:**
- **Artırılırsa:** Bataryalar daha geniş alanlara etki eder, daha fazla hedefe ulaşır.
- **Azaltılırsa:** Bataryalar daha dar alanlara etki eder, daha az hedefe ulaşır.

**Önerilen Aralık:**
- Level 1-2: 3 - 8
- Level 3: 5 - 10
- Level 4: 8 - 15
- Level 5: 30 - 50

### Özel Batarya Radius ve Duration

```yaml
game-balance:
  battery:
    special:
      level3-lightning-storm-radius: 7
      level3-lightning-storm-duration: 5
      level5-boss-killer-radius: 50
      level5-area-destroyer-radius: 50
      level5-mountain-destroyer-radius: 50
      level5-mountain-destroyer-height: 15
```

**Açıklama:** Özel bataryalar için radius, duration ve height değerleri.

**Etkisi:**
- **Radius artırılırsa:** Daha geniş alan etkilenir.
- **Duration artırılırsa:** Sürekli etkili bataryalar daha uzun süre aktif kalır.
- **Height artırılırsa:** Dikey etki alanı genişler.

**Önerilen Aralık:**
- Lightning Storm radius: 5 - 10
- Lightning Storm duration: 3 - 8 (saniye)
- Boss Killer radius: 40 - 60
- Area/Mountain Destroyer radius: 40 - 60
- Mountain Destroyer height: 10 - 20

### Explosion (Patlama) Gücü

```yaml
game-balance:
  battery:
    special:
      level5-area-destroyer-explosion: 8.0
      level5-mountain-destroyer-explosion: 10.0
      level5-apocalypse-reactor-explosion: 10.0
```

**Açıklama:** Patlama efektli bataryaların patlama gücü. Minecraft'ın `createExplosion` metodundaki `power` parametresi.

**Etkisi:**
- **Artırılırsa:** Patlamalar daha güçlü olur, daha fazla blok kırılır ve daha fazla hasar verilir.
- **Azaltılırsa:** Patlamalar zayıflar, daha az blok kırılır.

**Önerilen Aralık:** 5.0 - 15.0

**Not:** Çok yüksek değerler (20+) sunucu performansını olumsuz etkileyebilir.

### Yakıt Çarpanları

```yaml
game-balance:
  battery:
    fuel-multipliers:
      dark-matter: 10.0
      red-diamond: 5.0
      diamond: 2.5
      iron: 1.0
```

**Açıklama:** Farklı yakıt tiplerinin batarya gücünü ne kadar artırdığı. Final hasar = Temel hasar × Yakıt çarpanı.

**Etkisi:**
- **Artırılırsa:** O yakıt tipi daha değerli olur, oyuncular daha fazla o yakıtı kullanır.
- **Azaltılırsa:** O yakıt tipi daha az değerli olur, oyuncular başka yakıtlara yönelir.

**Önerilen Aralık:**
- Dark Matter: 8.0 - 15.0 (en değerli)
- Red Diamond: 4.0 - 7.0
- Diamond: 2.0 - 4.0
- Iron: 1.0 (sabit, referans noktası)

**Denge Notu:** Yakıt çarpanları arasındaki fark çok büyük olursa, oyuncular sadece en güçlü yakıtı kullanır. Çok küçük olursa, yakıt çeşitliliği azalır.

### RayTrace Mesafeleri

```yaml
game-balance:
  battery:
    raytrace:
      max-distance: 50
      short-distance: 30
```

**Açıklama:** Bataryaların hedef bulmak için kullandığı RayTrace mesafeleri (blok cinsinden).

**Etkisi:**
- **Artırılırsa:** Bataryalar daha uzaktaki hedefleri bulabilir, daha kolay kullanılır.
- **Azaltılırsa:** Bataryalar sadece yakındaki hedefleri bulabilir, daha zor kullanılır.

**Önerilen Aralık:**
- max-distance: 40 - 60
- short-distance: 20 - 40

**Performans Notu:** Çok yüksek değerler (100+) performans sorunlarına yol açabilir.

---

## 🎯 Training (Antrenman) Sistemi

### Kullanım Eşikleri

```yaml
game-balance:
  training:
    thresholds:
      full-power: 5
      mastery-start: 20
      max-power: 30
```

**Açıklama:**
- **full-power:** Kaç kullanımda tam güce (%100) ulaşılacağı.
- **mastery-start:** Kaç kullanımda ustalaşma sisteminin başlayacağı.
- **max-power:** Kaç kullanımda maksimum güce ulaşılacağı.

**Etkisi:**
- **Artırılırsa:** Oyuncuların bataryaları tam güce ulaşması daha uzun sürer, oyun daha zorlaşır.
- **Azaltılırsa:** Oyuncular hızlıca tam güce ulaşır, oyun daha kolaylaşır.

**Önerilen Aralık:**
- full-power: 3 - 8
- mastery-start: 15 - 30
- max-power: 25 - 40

**Denge Notu:** Bu değerler arasındaki fark çok büyük olursa, oyuncular uzun süre zayıf kalır. Çok küçük olursa, ilerleme hissi azalır.

### Seviye Bazlı Başlangıç Güçleri

```yaml
game-balance:
  training:
    start-power:
      level1: 0.2
      level2: 0.4
      level3: 0.6
      level4: 0.7
      level5: 0.8
```

**Açıklama:** Her batarya seviyesi için ilk kullanımdaki güç yüzdesi (0.0 - 1.0 arası).

**Etkisi:**
- **Artırılırsa:** Yeni bataryalar daha güçlü başlar, oyun daha kolaylaşır.
- **Azaltılırsa:** Yeni bataryalar daha zayıf başlar, oyun daha zorlaşır.

**Önerilen Aralık:**
- Level 1: 0.1 - 0.3
- Level 2: 0.3 - 0.5
- Level 3: 0.5 - 0.7
- Level 4: 0.6 - 0.8
- Level 5: 0.7 - 0.9

**Denge Notu:** Seviye arttıkça başlangıç gücü artmalı, böylece yüksek seviye bataryalar daha değerli olur.

### Güç Artış Değerleri

```yaml
game-balance:
  training:
    power-increments:
      use2: 0.2
      use3: 0.4
      use4: 0.6
      max-multiplier: 1.5
      mastery-increment: 0.5
```

**Açıklama:**
- **use2/use3/use4:** 2., 3., 4. kullanımlarda ne kadar güç artışı olacağı.
- **max-multiplier:** Maksimum güç çarpanı (örn: 1.5 = %150 güç).
- **mastery-increment:** Ustalaşma sisteminde her 10 kullanımda ne kadar güç artışı olacağı.

**Etkisi:**
- **Artırılırsa:** Güç artışı daha hızlı olur, oyun daha kolaylaşır.
- **Azaltılırsa:** Güç artışı daha yavaş olur, oyun daha zorlaşır.

**Önerilen Aralık:**
- use2: 0.15 - 0.25
- use3: 0.30 - 0.50
- use4: 0.50 - 0.70
- max-multiplier: 1.3 - 1.8
- mastery-increment: 0.3 - 0.7

**Denge Notu:** Bu değerler birbirleriyle uyumlu olmalı. Örneğin, use4 çok yüksekse, max-multiplier'e çok hızlı ulaşılır.

---

## ⚡ Power System (Güç Sistemi)

### Slot Update Cooldown

```yaml
game-balance:
  power-system:
    slot-update-cooldown: 500
```

**Açıklama:** Oyuncu slot (silah) değiştiğinde güç hesaplamasının ne kadar süre sonra yapılacağı (milisaniye).

**Etkisi:**
- **Artırılırsa:** Güç güncellemesi daha seyrek yapılır, performans artar ama güncelleme gecikir.
- **Azaltılırsa:** Güç güncellemesi daha sık yapılır, performans azalır ama güncelleme hızlanır.

**Önerilen Aralık:** 300 - 1000 (ms)

**Performans Notu:** Çok düşük değerler (100ms altı) performans sorunlarına yol açabilir.

### Player Name Update Interval

```yaml
game-balance:
  power-system:
    player-name-update-interval: 600
```

**Açıklama:** Oyuncu adlarının (seviye ve renk) ne kadar sıklıkla güncelleneceği (tick cinsinden).

**Etkisi:**
- **Artırılırsa:** Oyuncu adları daha seyrek güncellenir, performans artar ama güncelleme gecikir.
- **Azaltılırsa:** Oyuncu adları daha sık güncellenir, performans azalır ama güncelleme hızlanır.

**Önerilen Aralık:** 400 - 1200 (tick)

**Not:** 20 tick = 1 saniye. 600 tick = 30 saniye.

---

## 🎮 Main System (Ana Sistem)

### RayTrace Interval

```yaml
game-balance:
  main:
    raytrace-interval: 20
```

**Açıklama:** Casusluk Dürbünü için RayTrace işleminin ne kadar sıklıkla yapılacağı (tick cinsinden).

**Etkisi:**
- **Artırılırsa:** RayTrace daha seyrek yapılır, performans artar ama hedef bulma gecikir.
- **Azaltılırsa:** RayTrace daha sık yapılır, performans azalır ama hedef bulma hızlanır.

**Önerilen Aralık:** 10 - 40 (tick)

**Performans Notu:** RayTrace ağır bir işlemdir. Çok düşük değerler (5 tick altı) performans sorunlarına yol açabilir.

### RayTrace Max Distance

```yaml
game-balance:
  main:
    raytrace-max-distance: 50
```

**Açıklama:** Casusluk Dürbünü için maksimum RayTrace mesafesi (blok cinsinden).

**Etkisi:**
- **Artırılırsa:** Daha uzaktaki hedefler bulunabilir, dürbün daha güçlü olur.
- **Azaltılırsa:** Sadece yakındaki hedefler bulunabilir, dürbün zayıflar.

**Önerilen Aralık:** 40 - 80 (blok)

**Performans Notu:** Çok yüksek değerler (100+) performans sorunlarına yol açabilir.

---

## 🔮 Ritüel Sistemi

### Ritual Cooldown

```yaml
game-balance:
  ritual:
    cooldown: 10000
```

**Açıklama:** Bir ritüel yapıldıktan sonra bir sonraki ritüelin yapılabilmesi için geçmesi gereken süre (milisaniye).

**Etkisi:**
- **Artırılırsa:** Ritüeller daha seyrek yapılabilir, oyun daha zorlaşır ve ritüel spam'ı önlenir.
- **Azaltılırsa:** Ritüeller daha sık yapılabilir, oyun daha kolaylaşır ama spam riski artar.

**Önerilen Aralık:** 5000 - 20000 (ms)

**Denge Notu:** Çok düşük değerler (2000ms altı) ritüel spam'ına yol açabilir. Çok yüksek değerler (30000ms üstü) oyuncu deneyimini olumsuz etkiler.

---

## ⏱️ Task Intervals (Görev Aralıkları)

### Mob Ride Task Interval

```yaml
game-balance:
  tasks:
    mob-ride-interval: 5
```

**Açıklama:** MobRideTask'ın ne kadar sıklıkla çalışacağı (tick cinsinden). Bu task, oyuncuların binebileceği mobların kontrolünü yapar.

**Etkisi:**
- **Artırılırsa:** Kontrol daha seyrek yapılır, performans artar ama tepki süresi uzar.
- **Azaltılırsa:** Kontrol daha sık yapılır, performans azalır ama tepki süresi kısalır.

**Önerilen Aralık:** 3 - 10 (tick)

**Performans Notu:** Çok düşük değerler (2 tick altı) performans sorunlarına yol açabilir.

---

## 📊 Genel Denge Önerileri

### 1. Hasar Değerleri
- **PvE (Oyuncu vs Çevre):** Hasar değerleri yüksek olabilir, oyuncular güçlü hisseder.
- **PvP (Oyuncu vs Oyuncu):** Hasar değerleri düşük olmalı, dengeli savaşlar için.

### 2. Cooldown ve Interval Değerleri
- **Performans:** Yüksek değerler performansı artırır ama tepki süresini uzatır.
- **Oyuncu Deneyimi:** Düşük değerler tepki süresini kısaltır ama performansı düşürür.
- **Denge:** Orta değerler genellikle en iyisidir.

### 3. Radius ve Mesafe Değerleri
- **Performans:** Küçük değerler performansı artırır.
- **Oyun Deneyimi:** Büyük değerler daha etkileyici deneyim sağlar.
- **Denge:** Sunucu performansına göre ayarlanmalı.

### 4. Güç Artış Değerleri
- **İlerleme Hissi:** Yavaş artış = uzun süreli ilerleme hissi.
- **Hızlı İlerleme:** Hızlı artış = hızlı güçlenme ama kısa süreli ilerleme.
- **Denge:** Oyuncuların uzun süre oynaması için yavaş ama sürekli artış önerilir.

---

## 🔧 Config Değişikliği Sonrası

Config değerlerini değiştirdikten sonra:
1. **Sunucuyu yeniden başlatın** veya `/reload` komutunu kullanın.
2. **Değişiklikleri test edin** - Oyuncularla birlikte test etmek en iyisidir.
3. **Performansı izleyin** - TPS (Ticks Per Second) değerlerini kontrol edin.
4. **Oyuncu geri bildirimlerini toplayın** - Denge ayarları oyuncu deneyimine göre ayarlanmalıdır.

---

## 📝 Notlar

- **Tüm değerler opsiyoneldir** - Config'de olmayan değerler varsayılan değerlerle çalışır.
- **Performans önceliklidir** - Çok yüksek değerler sunucu performansını olumsuz etkileyebilir.
- **Denge sürekli ayarlanmalıdır** - Oyun geliştikçe denge ayarları da güncellenmelidir.
- **Oyuncu geri bildirimi önemlidir** - Oyuncuların görüşleri denge ayarlarında kritik rol oynar.

---

## ⚔️ Siege Sistemi (Kuşatma)

### Yağma Yüzdesi

```yaml
game-balance:
  siege:
    loot-percentage: 0.5
    chest-loot-percentage: 0.5
```

**Açıklama:**
- **loot-percentage:** Kuşatma kazananına verilecek para yüzdesi (%50)
- **chest-loot-percentage:** Pes etme durumunda sandık itemlerinden alınacak yüzde (%50)

**Etkisi:**
- **Artırılırsa:** Kuşatma ödülleri artar, savaşlar daha değerli olur
- **Azaltılırsa:** Kuşatma ödülleri azalır, savaşlar daha az değerli olur

**Önerilen Aralık:**
- loot-percentage: 0.3 - 0.7 (%30 - %70)
- chest-loot-percentage: 0.3 - 0.7 (%30 - %70)

---

## 🛡️ Buff Sistemi

### Fatih Buff Çarpanları

```yaml
game-balance:
  buff:
    conqueror:
      damage-multiplier: 0.2      # %20 hasar artışı
      production-multiplier: 0.3  # %30 üretim hızı artışı
```

**Açıklama:** Savaş kazanan klana verilen Fatih Buff'ının çarpanları.

**Etkisi:**
- **Artırılırsa:** Fatih Buff daha güçlü olur, savaş kazananları daha avantajlı olur
- **Azaltılırsa:** Fatih Buff zayıflar, savaş ödülleri azalır

**Önerilen Aralık:**
- damage-multiplier: 0.15 - 0.30 (%15 - %30)
- production-multiplier: 0.20 - 0.40 (%20 - %40)

### Kahraman Buff Çarpanları

```yaml
game-balance:
  buff:
    hero:
      health-multiplier: 0.15     # %15 can artışı
      defense-multiplier: 0.25    # %25 savunma artışı
```

**Açıklama:** Felaket tarafından yıkılan klana verilen Kahraman Buff'ının çarpanları.

**Etkisi:**
- **Artırılırsa:** Kahraman Buff daha güçlü olur, felaket mağdurları daha avantajlı olur
- **Azaltılırsa:** Kahraman Buff zayıflar, felaket sonrası toparlanma zorlaşır

**Önerilen Aralık:**
- health-multiplier: 0.10 - 0.25 (%10 - %25)
- defense-multiplier: 0.20 - 0.35 (%20 - %35)

---

## 📜 Contract Sistemi (Kontrat)

### Kontrat Ayarları

```yaml
game-balance:
  contract:
    reward-multiplier: 0.5              # Ödül çarpanı
    default-days: 7                      # Varsayılan süre (gün)
    health-restore-per-heart: 2.0       # Her kalp için geri verilen can
```

**Açıklama:**
- **reward-multiplier:** Kontrat ödül çarpanı (kan imzası için kullanılır)
- **default-days:** Varsayılan kontrat süresi (gün)
- **health-restore-per-heart:** Kontrat tamamlandığında her kalp için geri verilen can (2.0 = 1 kalp)

**Etkisi:**
- **Artırılırsa:** Kontratlar daha değerli olur, oyuncular daha fazla kontrat yapar
- **Azaltılırsa:** Kontratlar daha az değerli olur, oyuncular daha az kontrat yapar

**Önerilen Aralık:**
- reward-multiplier: 0.3 - 0.7
- default-days: 5 - 14 (gün)
- health-restore-per-heart: 1.5 - 2.5

---

## 🛒 Shop Sistemi (Market)

### Market Vergi Oranı

```yaml
game-balance:
  shop:
    tax-percentage: 0.05    # %5 vergi
```

**Açıklama:** Korumalı bölgelerde yapılan alışverişlerden alınan vergi oranı.

**Etkisi:**
- **Artırılırsa:** Vergi artar, klanlar daha fazla gelir elde eder
- **Azaltılırsa:** Vergi azalır, oyuncular daha az öder

**Önerilen Aralık:** 0.03 - 0.10 (%3 - %10)

**Denge Notu:** Çok yüksek vergi oyuncuları korumasız bölgelere yönlendirir. Çok düşük vergi klan ekonomisini zayıflatır.

---

## 🎯 Mission Sistemi (Görev)

### Tier 1 Görevleri (Taş Totem)

```yaml
game-balance:
  mission:
    tier1:
      kill-mob-reward-amount: 5     # Öldürme görevi ödülü (Iron Ingot)
      gather-reward-amount: 3       # Toplama görevi ödülü (Gold Ingot)
      kill-mob-target: 10           # Öldürme hedefi (10 mob)
      gather-target: 64              # Toplama hedefi (64 item)
```

**Açıklama:** Taş Totem için basit görevlerin ödülleri ve hedefleri.

**Etkisi:**
- **Artırılırsa:** Görevler daha zor ama ödülleri daha fazla olur
- **Azaltılırsa:** Görevler daha kolay ama ödülleri daha az olur

**Önerilen Aralık:**
- kill-mob-reward-amount: 3 - 8
- gather-reward-amount: 2 - 5
- kill-mob-target: 5 - 15
- gather-target: 32 - 128

### Tier 2 Görevleri (Elmas Totem)

```yaml
game-balance:
  mission:
    tier2:
      kill-mob-reward-amount: 5     # Öldürme görevi ödülü (Diamond)
      kill-mob-target: 20           # Öldürme hedefi (20 mob)
      gather-target: 10               # Toplama hedefi (10 item)
```

**Açıklama:** Elmas Totem için zor görevlerin ödülleri ve hedefleri.

**Etkisi:**
- **Artırılırsa:** Görevler daha zor ama ödülleri daha fazla olur
- **Azaltılırsa:** Görevler daha kolay ama ödülleri daha az olur

**Önerilen Aralık:**
- kill-mob-reward-amount: 3 - 10
- kill-mob-target: 15 - 30
- gather-target: 5 - 20

**Denge Notu:** Tier 2 görevleri Tier 1'den daha zor ve ödüllü olmalı, böylece oyuncular ilerleme hissi yaşar.

---

---

## 👹 Boss Sistemi

### BossBar Ayarları

```yaml
game-balance:
  boss:
    max-bossbar-distance: 100       # BossBar gösterim mesafesi (blok)
    max-bossbars-per-player: 3      # Oyuncu başına maksimum BossBar sayısı
    ritual-cooldown: 60000          # Ritüel cooldown (ms) - 60 saniye
    weak-point-duration: 5000       # Zayıf nokta süresi (ms) - 5 saniye
    shield-duration: 3000           # Kalkan süresi (ms) - 3 saniye
```

**Açıklama:**
- **max-bossbar-distance:** Oyuncuların boss'ları ne kadar mesafeden görebileceği (blok)
- **max-bossbars-per-player:** Bir oyuncunun aynı anda görebileceği maksimum boss sayısı
- **ritual-cooldown:** Boss ritüeli yapıldıktan sonra bir sonraki ritüelin yapılabilmesi için geçmesi gereken süre
- **weak-point-duration:** Boss'un zayıf noktasının aktif kalacağı süre
- **shield-duration:** Boss'un kalkanının aktif kalacağı süre

**Etkisi:**
- **Artırılırsa:** Boss'lar daha uzaktan görülebilir, daha uzun süreli yetenekler
- **Azaltılırsa:** Boss'lar daha yakından görülebilir, daha kısa süreli yetenekler

**Önerilen Aralık:**
- max-bossbar-distance: 50 - 150 (blok)
- max-bossbars-per-player: 2 - 5
- ritual-cooldown: 30000 - 120000 (ms)
- weak-point-duration: 3000 - 10000 (ms)
- shield-duration: 2000 - 6000 (ms)

---

## 🪤 Trap Sistemi (Tuzak)

### Tuzak Yakıt Değerleri

```yaml
game-balance:
  trap:
    fuel:
      diamond: 5                    # Elmas yakıt değeri
      emerald: 10                    # Zümrüt yakıt değeri
      titanium: 20                   # Titanyum yakıt değeri
```

**Açıklama:** Farklı yakıt tiplerinin tuzaklara verdiği patlama hakkı sayısı.

**Etkisi:**
- **Artırılırsa:** Yakıtlar daha değerli olur, tuzaklar daha uzun süre aktif kalır
- **Azaltılırsa:** Yakıtlar daha az değerli olur, tuzaklar daha kısa süre aktif kalır

**Önerilen Aralık:**
- diamond: 3 - 8
- emerald: 8 - 15
- titanium: 15 - 30

### Tuzak Hasar Değerleri

```yaml
game-balance:
  trap:
    damage:
      hell-trap: 3.0                 # Cehennem tuzağı hasarı
      shock-trap: 2.0                # Şok tuzağı hasarı
      mine: 5.0                      # Mayın hasarı
      poison-trap: 0.5               # Zehir tuzağı hasarı (sürekli)
      black-hole: 10.0               # Kara delik tuzağı hasarı
```

**Açıklama:** Her tuzak tipinin verdiği hasar miktarı.

**Etkisi:**
- **Artırılırsa:** Tuzaklar daha güçlü olur, oyuncular daha fazla hasar alır
- **Azaltılırsa:** Tuzaklar zayıflar, oyuncular daha az hasar alır

**Önerilen Aralık:**
- hell-trap: 2.0 - 5.0
- shock-trap: 1.0 - 4.0
- mine: 3.0 - 8.0
- poison-trap: 0.3 - 1.0
- black-hole: 5.0 - 15.0

---

## 💣 Mine Sistemi (Mayın)

### Seviye Bazlı Hasar Değerleri

```yaml
game-balance:
  mine:
    damage:
      level1-base: 3.0               # Seviye 1 mayın temel hasarı
      level2-base: 5.0               # Seviye 2 mayın temel hasarı
      level3-base: 8.0               # Seviye 3 mayın temel hasarı
      level4-base: 12.0              # Seviye 4 mayın temel hasarı
      level5-base: 20.0               # Seviye 5 mayın temel hasarı
```

**Açıklama:** Her mayın seviyesi için temel hasar değeri. Bu değerler mayın tipine göre kullanılır.

**Etkisi:**
- **Artırılırsa:** Mayınlar daha güçlü olur, oyuncular daha fazla hasar alır
- **Azaltılırsa:** Mayınlar zayıflar, oyuncular daha az hasar alır

**Önerilen Aralık:**
- Level 1: 2.0 - 5.0
- Level 2: 4.0 - 8.0
- Level 3: 6.0 - 12.0
- Level 4: 10.0 - 18.0
- Level 5: 15.0 - 30.0

**Denge Notu:** Seviye arttıkça hasar artmalı, böylece yüksek seviye mayınlar daha değerli olur.

---

## 🐉 Mob Sistemi (Canavar)

### Özel Mob Can ve Hasar Değerleri

```yaml
game-balance:
  mob:
    hell-dragon-health: 200.0        # Cehennem Ejderi canı
    terror-worm-health: 100.0        # Toprak Solucanı canı
    war-bear-health: 150.0           # Savaş Ayısı canı
    shadow-panther-health: 80.0      # Gölge Panteri canı
    wyvern-health: 250.0             # Wyvern canı
    fire-amphiptere-health: 120.0    # Ateş Amfibiterü canı
    fire-amphiptere-damage: 10.0     # Ateş Amfibiterü hasarı
    hell-dragon-size: 20             # Cehennem Ejderi boyutu
    wyvern-size: 15                  # Wyvern boyutu
```

**Açıklama:** Özel mobların can, hasar ve boyut değerleri.

**Etkisi:**
- **Artırılırsa:** Moblar daha güçlü ve büyük olur, oyuncular için daha zor
- **Azaltılırsa:** Moblar zayıflar ve küçülür, oyuncular için daha kolay

**Önerilen Aralık:**
- Can değerleri: 50.0 - 500.0
- Hasar değerleri: 5.0 - 20.0
- Boyut değerleri: 10 - 30

**Denge Notu:** Mobların güçleri oyun dengesine göre ayarlanmalı. Çok güçlü moblar oyuncuları zorlar, çok zayıf moblar oyunu kolaylaştırır.

---

**Son Güncelleme:** Bu döküman, oyunun mevcut versiyonuna göre güncellenmiştir. Yeni özellikler eklendikçe bu döküman da güncellenecektir.

