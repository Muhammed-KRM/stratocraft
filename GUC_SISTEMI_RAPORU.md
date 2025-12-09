## Genel Amaç

Bu hibrit güç sistemiyle hedef:

- **Tüm güç kaynaklarını tek bir çatı altında toplamak** (eşya, ritüel, yapılar, ustalık, bufflar vb.)
- **Hem PvP/PvE hem de felaket/klan sistemleri için yeniden kullanılabilir**, modüler bir yapı kurmak
- Dengeyi bozmadan **oyuncuya gelişimi hissettiren** bir matematiksel model sağlamak
- Tüm değerleri **config tabanlı** yapmak, formülleri bile mümkün olduğunca parametreleştirmek
- Her özelliğe özel fonksiyonlar ve sınıflar ile **değiştirilebilir stratejiler** (Strategy pattern) kullanmak

Aşağıda, bu hedeflere göre detaylı bir **sistem tasarım raporu** var. Bunu onayladıktan sonra, adım adım koda döndürebiliriz.

---

## 1. Temel Kavramlar ve Sözlük

- **SGP (Stratocraft Global Power)**: Oyuncu ve klan için hesaplanan toplam güç puanı.
- **Combat Power (CP)**: Savaş/dövüş odaklı anlık güç (PvP, PvE, Felaket).
- **Progression Power (PP)**: Kalıcı ilerleme/gelişim gücü (yapılar, ustalık, ritüel altyapısı).
- **PlayerPowerProfile**: Bir oyuncunun tüm güç bileşenlerini taşıyan veri yapısı.
- **ClanPowerProfile**: Bir klanın tüm güç bileşenlerini taşıyan veri yapısı.

Bu ayrım sayesinde:
- Felaket sistemi daha çok **Combat Power**’a bakabilir.
- Klan seviyesi, toprak, ekonomi gibi sistemler daha çok **Progression Power**’ı kullanabilir.
- İkisini farklı ağırlıklarla birleştirip toplam SGP üretebiliriz.

---

## 2. Güç Kaynakları ve Formüller (Hibrit Tasarım)

### 2.1. Eşya Gücü (Gear Power)

**Kaynaklar:**
- Silah seviyesi (ItemManager)
- Zırh seviyesi (ItemManager)
- Özel item/aksesuar (SpecialItemManager, persistent data)

**Önerilen puanlar (config’e taşınmış halde zaten var, sadece netleştiriyoruz):**

- **Silah:**
  - Level 1: 60
  - Level 2: 150
  - Level 3: 400
  - Level 4: 900
  - Level 5: 1600  *(diğer AI’nin 1800 fikrini biraz yumuşatılmış formda alıyoruz)*

- **Zırh (parça başına):**
  - Level 1: 40
  - Level 2: 100
  - Level 3: 250
  - Level 4: 600
  - Level 5: 1000

> **Tam set seviye 5 oyuncu:**
> - Silah: 1600
> - Zırh: 4 × 1000 = 4000  
> - Toplam Gear Power: **5600**

**Fonksiyon kırılımı (ayrı ayrı, birbirini bozmayacak şekilde):**
- `double calculateWeaponPower(Player p)`
- `double calculateArmorPower(Player p)`
- `double calculateSpecialItemPower(Player p)` *(ileride eklenebilir)*
- `double calculateGearPower(Player p)` → yukarıdakilerin toplamı

Tüm katsayılar `ClanPowerConfig` ve/veya `DisasterPowerConfig` içinden okunacak.

---

### 2.2. Ritüel Blok Gücü (Ritual Block Power)

**Kaynaklar:**
- Klan arazisi içindeki belirli blok türleri (TerritoryManager + ritüel sistemi)

**Önerilen puanlar:**
- Demir Blok: 8
- Altın Blok: 12
- Elmas Blok: 25
- Obsidyen: 30
- Zümrüt Blok: 35
- **Titanyum/Netherite Blok:** 150 (diğer AI’den gelen “çok değerli blok” fikri)

**Tasarımsal karar:**
- **Oyuncu envanteri üzerinden değil**, **klan arazisi taraması üzerinden** hesaplanacak (progression odaklı).
- Tarama **sync yapılmayacak**, 10–20 dakikada bir **async task** ile yapılacak.
- Her klan için `ClanRitualBlockSnapshot` gibi bir yapı tutulacak:
  - `Map<Material, Integer> blockCounts`
  - `lastScanTime`

**Fonksiyonlar:**
- `double calculateClanRitualBlockPower(Clan clan)`
- `double getRitualBlockPower(Material m)` → config’ten
- `AsyncTask scanClanTerritoryBlocks(Clan clan)` → snapshot’ı günceller

---

### 2.3. Ritüel Kaynak Gücü (Ritual Resource Power)

**Kaynaklar:**
- Ritüelleri aktif eden item’ler / kaynaklar
  - Demir, Elmas, Kızıl Elmas, Karanlık Madde, Titanyum vb.

**Önerilen puanlar:**
- Demir: 5
- Elmas: 10
- Kızıl Elmas: 18
- Titanyum: 15
- Karanlık Madde: 50
- Default: 3

**Tasarımsal karar:**
- Bu puanlar **iki yerde** kullanılabilir:
  1. **Anlık ritüel gücü** (örneğin belli bir süre klanın saldırı/defense buff’ı)
  2. **Toplam progression gücü** (ritüel kullanım geçmişine göre)

Başlangıç için **progression tarafına** yazmak daha tutarlı:
- Ritüel her başarıyla yapıldığında `ClanRitualStats` içine kaydedilir:
  - `Map<String ritualType, int usageCount>`
- Toplam kaynak gücü, bu kullanımlardan türetilir.

**Fonksiyonlar:**
- `double calculateClanRitualResourcePower(Clan clan)`
- `double getRitualResourcePower(String resourceId)`

---

### 2.4. Klan Yapı Gücü (Structure Power)

**Kaynaklar:**
- `Clan.getStructures()` (Structure + level)

**Önerilen puanlar (zaten config’te):**
- Level 1: 100
- Level 2: 250
- Level 3: 500
- Level 4: 1200
- Level 5: 2000
- + **Klan Kristali:** 500 sabit bonus (eklendiğinde daha epik his verir)

**Fonksiyonlar:**
- `double calculateClanStructurePower(Clan clan)`
- `double getStructurePower(int level)` (config’ten)
- (Opsiyonel) `double getStructurePowerByType(Structure.Type type, int level)`  
  → Bazı yapılar (savunma kuleleri vs.) daha çok puan edebilir.

---

### 2.5. Antrenman / Ustalık Gücü (Training / Mastery Power)

**Kaynaklar:**
- `TrainingManager.getTotalUses(player, ritualId)`
- `TrainingManager.getMasteryLevel() / getMasteryMultiplier()`

**Önerilen hibrit formül:**

\[
\text{masteryPower} = B \times \left(\frac{\text{masteryPercent}}{100}\right)^{E}
\]

- `B` (base-power) ≈ 150
- `E` (exponent) ≈ 1.4

**Örnek:**
- %150 → ~250 puan
- %200 → ~400 puan
- %300 → ~700 puan

**Tasarımsal karar:**
- **%100 altı** sadece “normal” kabul edilir, ekstra puan verilmez.
- 100 üstü için bonus puan verilir (emek odaklı ödül).

**Fonksiyonlar:**
- `double calculatePlayerTrainingMasteryPower(Player player)`  
  → Tüm ritüel kayıtlarını dolaşır, her biri için masteryPercent hesaplar.
- `double getMasteryPower(double masteryPercent)` → formül + config.

---

### 2.6. Toplam Oyuncu ve Klan Güçleri

**PlayerPowerProfile:**
- `gearPower`
- `trainingPower`
- `buffPower` *(istersen buradan da ekleyebiliriz, felaket sistemiyle uyum için)*
- `progressionPower` (ritüel + yapılar vs. istenirse eklenebilir)
- `totalCombatPower` (CP) → gear + buff + uygun kısımlar
- `totalProgressionPower` (PP)
- `totalSGP` → ağırlıklı birleşim (config: CP:PP oranları)

**ClanPowerProfile:**
- `memberPowerSum` (üyelerin CP veya SGP toplamı)
- `structurePower`
- `ritualBlockPower`
- `ritualResourcePower`
- `totalClanPower`
- `clanLevel`

Her biri için **ayrı hesap fonksiyonları**, sonrasında hepsini toplayan bir “orchestrator” fonksiyon:

- `PlayerPowerProfile calculatePlayerProfile(Player p)`
- `ClanPowerProfile calculateClanProfile(Clan c)`

Bu yapıyla:
- Bir fonksiyonu değiştirirken (`calculateRitualBlockPower`) diğerleri etkilenmez.
- Formüller, sadece ilgili fonksiyonun içinde ve config tabanlı.

---

## 3. Seviye ve Eğri (Level Curve) Tasarımı

### 3.1. Oyuncu Seviye Eğrisi (Hibrit)

**Aşama 1 – Hızlı ilerleme (Seviye 1–10):**

\[
\text{level} = \sqrt{\frac{\text{power}}{100}}
\]

- 1,000 puan → seviye ~3
- 5,000 puan → seviye ~7
- 10,000 puan → seviye ~10

**Aşama 2 – Zor ilerleme (Seviye 11+):**

\[
\text{level} = 10 + \left\lfloor \log_{10}\left(\frac{\text{power}}{10000}\right) \times 3 \right\rfloor
\]

- 50,000 → ~13
- 200,000 → ~16

**Fonksiyon:**
- `int calculatePlayerLevel(double power)`  
  → İki aşamalı eğri, parametreleri config’den (`player-level.base-power`, `player-level.multiplier`, `player-level.switch-power` gibi).

### 3.2. Klan Seviye Eğrisi

Benzer, ama biraz daha “yavaş”:

\[
\text{clanLevel} = \max\left(1,\ \left\lfloor \log_{10}\left(\frac{\text{clanPower}}{\text{clanBasePower}}\right) \times \text{clanMultiplier} \right\rfloor + 1\right)
\]

- `clanBasePower` ≈ 500
- `clanMultiplier` ≈ 2.0

**Fonksiyon:**
- `int calculateClanLevel(double clanPower)`

---

## 4. Koruma Sistemi Tasarımı

### 4.1. Temel Kurallar

- **Onurlu Savaş Aralığı:**
  - Eğer `targetPower < attackerPower * protectionThreshold` ise **saldırı yasak**.
  - `protectionThreshold` ≈ 0.5 (config’den).

- **Acemi Koruması:**
  - Eğer `targetPower < rookieThreshold` (ör: 5000) VE hedef ilk saldıran değilse:
    - Güçlü oyuncular (örneğin `attackerPower > 10000`) saldıramaz.

- **Klan Savaşı İstisnası:**
  - Eğer `ClanManager.areAtWar(attackerClan, targetClan)`:
    - Koruma sistemi **devre dışı**.

- **Klan İçi Koruma:**
  - Aynı klandaki oyuncular için daha katı eşik:
    - `clanProtectionThreshold` ≈ 0.6
  - İstersen “klan içi friendly fire tamamen kapalı” da yapabiliriz.

**Fonksiyonlar:**
- `boolean canAttackPlayer(Player attacker, Player target)`
- `boolean isRookie(Player p)` → power < threshold
- `boolean isAtWar(Clan a, Clan b)` → ClanManager’den

**Event entegrasyonu:**
- `EntityDamageByEntityEvent` listener’ında:
  - `Main.getInstance().getClanPowerSystem().canAttackPlayer(attacker, target)` kontrolü
  - Yasaksa event cancel + mesaj.

---

## 5. Mevcut Sistemlerle Uyum

### 5.1. Felaket Sistemi (Disaster / Dynamic Difficulty)

Şu an:
- `PlayerPowerCalculator` + `ServerPowerCalculator` + `DisasterPowerConfig`
- Dinamik zorluk, **özellikle combat gücünü** baz alıyor.

**Uyum Planı:**
- `ClanPowerSystem` → daha **genel/geniş** bir güç sistemi.
- Felaket sistemi için, `PlayerPowerCalculator`’ın altına şu eklenebilir:
  - `double getCombatPower(Player p)` → ClanPowerSystem’deki PlayerProfile’dan CP okumak
  - Veya tam tersi: ClanPowerSystem, PlayerPowerCalculator’ı kullanarak CP’yi alsın.

İki seçenek:

1. **Felaket → PlayerPowerCalculator (şimdiki gibi)**  
   ClanPowerSystem sadece “klan/ilerleme” tarafında kalır.  
   Avantaj: Mevcut felaket kodu minimal değişir.

2. **Felaket → ClanPowerSystem’in Combat Power’ı**  
   ServerPowerCalculator, `ClanPowerSystem.calculatePlayerPower` ile beslenir.  
   Avantaj: Tek merkezden tüm güç hesaplama; Dezavantaj: refactor daha büyük.

**ÖNERİ:**  
Şimdilik **felaket sistemiyle doğrudan entegre ETME**, sadece araya bir köprü fonksiyon koy:
- `double getDisasterRelevantPower(Player p)`  
Gelecekte istersek felaket sistemi buraya döner, kodu kırmadan.

---

### 5.2. Klan Sistemi

Klan sistemi zaten erken aşamada ve büyük değişime açık dedin. Burada:

- `Clan` modeline şunlar eklenebilir:
  - `double cachedPower`
  - `int clanLevel`
  - `long lastPowerUpdate`

Ama **hesaplama mantığı Clan sınıfında değil**, tamamen `ClanPowerSystem` içinde olacak:
- `ClanPowerProfile calculateClanProfile(Clan c)`
- `void updateClanCachedPower(Clan c, ClanPowerProfile p)`

Klan sisteminde:
- **Vergi, savaş, ally limiti, toprak limiti** gibi kurallar klan seviyesine bağlanabilir.
- Örneğin:
  - Seviye 1–3: max 1 bölge
  - Seviye 4–6: max 3 bölge
  - Seviye 7+: max 5 bölge

Bu değişiklikler klan sistemini “bozmaz”, aksine güçlendirir.

---

### 5.3. PvP / Oyuncu Koruma

Yeni sistem:
- Basit bir entry point:
  - `ClanPowerSystem.canAttackPlayer(attacker, target)`
- PvP ile ilgili her yerde (komutlar, özel arenalar, boss event’leri) bu fonksiyon kullanılabilir.

Ayrıyeten:
- Özel “ritüel kalkanı” gibi mekanikler eklenmek istenirse:
  - `boolean hasTemporaryProtection(Player p)` gibi ekstra kontroller eklenebilir (örneğin belli bir ritüel sonrası 10 dk koruma).

---

### 5.4. Ritüel Sistemi

Ritüel sistemiyle entegrasyon:

- Ritüel aktivasyonlarında:
  - `ClanPowerSystem` bilgilendirilebilir:
    - `onRitualSuccess(Clan clan, RitualType type, Map<Material,Integer> usedBlocks, Map<ResourceType,Integer> usedResources)`
  - Bu event üzerinden:
    - Kullanılan kaynaklar → `ritualResourcePower`
    - Kurulan ritüel yapıları → `ritualBlockPower` snapshot’ına yansıtılabilir.

Bu sayede:
- Ritüel yapan, yatırım yapan, base dizayn eden klanlar **tabloya yansır**.
- Sadece “item kasan” değil, “mühendislik yapan” oyuncular da güçlenir.

---

## 6. Kod Tasarım Prensipleri (Temiz Kod)

- **Tek Sorumluluk:**  
  - `ClanPowerSystem`: Sadece hesaplama + cache + koruma kontrolü.
  - `ClanPowerConfig`: Sadece config okuma + formül parametreleri.
  - Listener’lar: Sadece event’ten doğru fonksiyonu çağırır.

- **Her özellik için ayrı fonksiyon:**
  - Item, ritüel, yapı, ustalık, buff → hepsi ayrı `calculateXxxPower` metodunda.
  - Seviye hesaplama, koruma kontrolü ayrı.

- **Parametreleştirme:**
  - Her formülün katsayıları (`basePower`, `exponent`, `threshold`) config’te.
  - Böylece yaşayarak gördükçe sadece config oynayıp dengeleyebilirsin.

- **Genişlemeye açık / değişime kapalı (Open/Closed):**
  - Yeni bir güç kaynağı (ör. “Relic Power”) eklemek istediğinde:
    - Yeni bir `calculateRelicPower(Player p)` fonksiyonu + birkaç config değeri
    - Toplam hesaba bunu dahil eden satıra küçük bir ekleme yeterli.

---

## 7. Sonraki Adım Önerisi

1. Bu tasarımı beraber üzerinden geçelim:
   - Puanlar / seviyeler sence mantıklı mı?
   - Özellikle **koruma eşiği** ve **acemi koruması** hoşuna gidiyor mu?
2. Sonra:
   - Mevcut `ClanPowerSystem / ClanPowerConfig`’i bu tasarıma göre revize ederiz (fonksiyon ayrışması + seviye algoritması).
   - Ardından **sadece PvP koruma** tarafını entegre ederiz (küçük, güvenli bir adım).
   - Daha sonra felaket ve klan özelliklerine bağlantıları sırayla ekleriz.

İstersen bir sonraki adımda:
- **“Sadece oyuncu seviyesi + PvP koruma kısmını”** kod tarafına taşıyalım,
- Sonra ritüel ve klan tarafını ekleyelim.