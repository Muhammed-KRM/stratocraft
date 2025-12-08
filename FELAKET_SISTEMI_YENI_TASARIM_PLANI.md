# 🌋 FELAKET SİSTEMİ YENİ TASARIM PLANI

## 📊 GENEL BAKIŞ

Felaket sistemi tamamen yeniden tasarlanacak. Ana amaç: **Oyuncuları merkezden çok uzaklaşmamasını ve merkeze çok yakın yerleşmemelerini sağlamak**.

---

## 🎯 ANA HEDEFLER

1. **Felaketler çok güçlü olmalı** - Tek başına başa çıkılamaz
2. **Merkezden uzaklaşmayı engelle** - Uzakta spawn olan felaketler merkeze doğru ilerler
3. **Merkeze çok yakın yerleşmeyi engelle** - Merkeze yakın felaketler spawn olur
4. **Klan kristalini öncelikli hedef** - Canavar felaketler önce kristali yok eder
5. **Oyuncularla oyalanmaz** - Kristal yok edilene kadar oyuncularla savaşmaz (2 dk'da bir saldırır)

---

## 🏗️ SİSTEM MİMARİSİ

### 1. FELAKET TİPLERİ

#### A. CANAVAR FELAKETLER (CREATURE DISASTERS)
**Özellikler:**
- Merkezden uzakta spawn olur (5000+ blok)
- Merkeze doğru ilerler
- Klan kristalini öncelikli hedef alır
- Kristal yok edilene kadar oyuncularla oyalanmaz
- 2 dakikada bir yakındaki oyunculara saldırır
- Kristal yok edildikten sonra en yakın klan kristaline gider

**Alt Kategoriler:**
1. **Tek Boss Felaketi** (Mega Boss)
   - Çok güçlü tek bir boss
   - Örnek: Titan Golem, Khaos Ejderi
   - Can: 10,000-50,000 HP
   - Hasar: 50-200 HP/vuruş

2. **Orta Güçte Grup** (30 adet)
   - 30 tane orta güçte canavar
   - Örnek: 30 tane güçlendirilmiş zombi, iskelet
   - Can: 500-2000 HP/başına
   - Hasar: 10-50 HP/vuruş

3. **Mini Felaket Dalgası** (100-500 adet)
   - Yüzlerce mini felaket
   - Bossların güçlendirilmiş halleri
   - Örnek: 200 tane güçlendirilmiş creeper, 300 tane güçlendirilmiş zombi
   - Can: 100-500 HP/başına
   - Hasar: 5-20 HP/vuruş

#### B. DOĞA OLAYI FELAKETLER (NATURAL DISASTERS)
**Özellikler:**
- Tüm dünyayı etkiler
- Belirli bir süre boyunca aktif kalır
- Oyuncuları ve yapıları etkiler

**Tipler:**
1. **Deprem (EARTHQUAKE)**
   - Etrafta patlamalar olur
   - Herkes sürekli yüksek hasar alır (1 dakika)
   - Bloklar düşer, yapılar hasar alır

2. **Fırtına (STORM)**
   - Oyuncular yaklaştıkça yıldırım düşer
   - Rastgele konumlarda yıldırım
   - Yüksek hasar

3. **Güneş Patlaması (SOLAR_FLARE)**
   - Yanıcı bloklar yanar
   - Etrafta lavlar oluşur
   - Çatısı olmayan yerde bulunan tüm oyuncular yanar

#### C. MİNİ FELAKETLER (MINI DISASTERS)
**Özellikler:**
- Rastgele zamanda günde birkaç kez ortaya çıkar
- Çok güçlü değildir
- Bosslara/moblara buff basabilir
- Mini boss spawn edebilir
- Oyunculara buff verebilir
- Mini etkinlikler olur

**Örnekler:**
- "Boss Güçlenme Dalgası" - Tüm bosslar %50 daha güçlü
- "Mob İstilası" - 50 tane güçlendirilmiş mob spawn
- "Oyuncu Buff Dalgası" - Tüm oyunculara geçici güç buff'ı

---

## 📅 SPAWN SİSTEMİ

### Seviye Sistemi

| Seviye | Açıklama | Spawn Sıklığı | Örnekler |
|--------|----------|--------------|----------|
| **1** | Günlük Felaketler | Her gün | Mini felaketler, Güneş Patlaması |
| **2** | Orta Felaketler | 3 günde bir | Deprem, Fırtına, Orta güçte grup |
| **3** | Büyük Felaketler | Haftada bir | Tek Boss, Volkanik Patlama |
| **4** | Mega Felaketler | 2 haftada bir | Çok güçlü boss, Mini felaket dalgası |

### Mini Felaketler
- Rastgele zamanda günde 2-5 kez
- Süre: 5-15 dakika
- Güç: Düşük-Orta

---

## 🎮 OYUNCU DAVRANIŞI

### Canavar Felaketler İçin

1. **Klan Kristali Hedefleme:**
   - Felaket en yakın klan kristalini bulur
   - Kristale doğru ilerler
   - Kristal yok edilene kadar oyuncularla oyalanmaz

2. **Oyuncu Saldırısı:**
   - 2 dakikada bir yakındaki oyunculara saldırır
   - Saldırı sonrası kristale devam eder
   - Kristal yok edildikten sonra oyuncularla savaşır

3. **Klan Yok Etme:**
   - Kristal yok edilince klan dağılır
   - Yapılar yok edilir
   - En yakın klan kristaline gider

### Doğa Olayları İçin

1. **Deprem:**
   - Rastgele konumlarda patlama
   - Herkes sürekli hasar alır
   - Bloklar düşer

2. **Fırtına:**
   - Oyuncular yaklaştıkça yıldırım
   - Rastgele konumlarda yıldırım

3. **Güneş Patlaması:**
   - Yanıcı bloklar yanar
   - Lavlar oluşur
   - Çatısız oyuncular yanar

---

## 💻 TEKNİK İMPLEMENTASYON

### 1. YENİ SINIFLAR

#### `DisasterType.java` (Enum Genişletme)
```java
public enum Type {
    // Canavar Felaketler
    TITAN_GOLEM,           // Seviye 3 - Tek Boss
    CHAOS_DRAGON,          // Seviye 3 - Tek Boss
    ABYSSAL_WORM,          // Seviye 2 - Tek Boss
    VOID_TITAN,            // Seviye 3 - Tek Boss
    ICE_LEVIATHAN,         // Seviye 2 - Tek Boss
    
    ZOMBIE_HORDE,          // Seviye 2 - 30 Orta Güçte
    SKELETON_LEGION,       // Seviye 2 - 30 Orta Güçte
    CREEPER_SWARM,         // Seviye 1 - 100-500 Mini
    
    // Doğa Olayları
    SOLAR_FLARE,           // Seviye 1 - Güneş Patlaması
    EARTHQUAKE,            // Seviye 2 - Deprem
    STORM,                 // Seviye 2 - Fırtına
    VOLCANIC_ERUPTION,     // Seviye 3 - Volkanik Patlama
    METEOR_SHOWER,         // Seviye 2 - Meteor Yağmuru
    
    // Mini Felaketler
    BOSS_BUFF_WAVE,        // Mini - Boss güçlenme
    MOB_INVASION,          // Mini - Mob istilası
    PLAYER_BUFF_WAVE       // Mini - Oyuncu buff
}
```

#### `DisasterCategory.java` (Yeni)
```java
public enum Category {
    CREATURE,      // Canavar felaketler
    NATURAL,       // Doğa olayları
    MINI           // Mini felaketler
}
```

#### `CreatureDisasterType.java` (Yeni)
```java
public enum CreatureDisasterType {
    SINGLE_BOSS,           // Tek boss
    MEDIUM_GROUP,          // 30 orta güçte
    MINI_SWARM            // 100-500 mini
}
```

### 2. YENİ METODLAR

#### `DisasterManager.java`
```java
// Klan kristali bulma
public Location findNearestCrystal(Location from);

// Felaket hedef belirleme
public void setDisasterTarget(Disaster disaster, Location target);

// Mini felaket spawn
public void spawnMiniDisaster(MiniDisasterType type);

// Grup felaket spawn (30 adet)
public void spawnGroupDisaster(EntityType entityType, int count, Location spawnLoc);

// Mini felaket dalgası spawn (100-500 adet)
public void spawnSwarmDisaster(EntityType entityType, int count, Location spawnLoc);
```

#### `DisasterTask.java`
```java
// Klan kristali hedefleme
private void targetCrystal(Disaster disaster, Entity entity);

// Oyuncu saldırısı (2 dk'da bir)
private void attackNearbyPlayers(Disaster disaster, Entity entity);

// Klan yok etme
private void destroyClan(Clan clan, Location disasterLoc);
```

### 3. YENİ ÖZELLİKLER

#### A. Klan Kristali Hedefleme
- `TerritoryManager` ile klan kristalleri bulunur
- En yakın kristal hedef olarak belirlenir
- Felaket kristale doğru ilerler
- Kristal yok edilince en yakın kristale gider

#### B. Oyuncu Saldırısı (2 dk'da bir)
- Her 2 dakikada bir (2400 tick)
- Yakındaki oyunculara saldırır
- Saldırı sonrası kristale devam eder

#### C. Mini Felaketler
- Rastgele zamanda spawn
- Günde 2-5 kez
- Süre: 5-15 dakika
- Buff/debuff sistemleri

---

## 📝 YAPILACAKLAR LİSTESİ

### Faz 1: Temel Altyapı (Öncelik: YÜKSEK)

1. ✅ **DisasterType enum genişletme**
   - Yeni felaket tipleri ekle
   - Mini felaket tipleri ekle

2. ✅ **DisasterCategory enum ekleme**
   - CREATURE, NATURAL, MINI kategorileri

3. ✅ **CreatureDisasterType enum ekleme**
   - SINGLE_BOSS, MEDIUM_GROUP, MINI_SWARM

4. ✅ **Disaster model güncelleme**
   - Yeni alanlar: `creatureDisasterType`, `targetCrystalLocation`
   - Yeni metodlar: `getTargetCrystal()`, `setTargetCrystal()`

### Faz 2: Klan Kristali Hedefleme (Öncelik: YÜKSEK)

5. ✅ **Klan kristali bulma metodu**
   - `DisasterManager.findNearestCrystal(Location from)`
   - `TerritoryManager` ile entegrasyon

6. ✅ **Felaket hedef belirleme**
   - `DisasterTask.targetCrystal()` metodu
   - Kristale doğru ilerleme

7. ✅ **Kristal yok etme**
   - Kristal kırılınca klan dağılır
   - En yakın kristale geçiş

### Faz 3: Oyuncu Saldırısı (Öncelik: ORTA)

8. ✅ **2 dakikada bir saldırı**
   - `DisasterTask.attackNearbyPlayers()` metodu
   - Tick sayacı (2400 tick = 2 dakika)

9. ✅ **Saldırı sonrası kristale devam**
   - Saldırı bitince hedef kristale dön

### Faz 4: Grup Felaketler (Öncelik: ORTA)

10. ✅ **30 adet orta güçte spawn**
    - `DisasterManager.spawnGroupDisaster()` metodu
    - Her birine hedef kristal atama

11. ✅ **100-500 adet mini spawn**
    - `DisasterManager.spawnSwarmDisaster()` metodu
    - Performans optimizasyonu

### Faz 5: Doğa Olayları Tamamlama (Öncelik: ORTA)

12. ✅ **Deprem implementasyonu**
    - Rastgele patlamalar
    - Sürekli hasar

13. ✅ **Fırtına implementasyonu**
    - Yıldırım düşmesi
    - Oyuncu yaklaşınca yıldırım

14. ✅ **Güneş Patlaması güncelleme**
    - Lav oluşturma
    - Çatısız oyuncular yanar

### Faz 6: Mini Felaketler (Öncelik: DÜŞÜK)

15. ✅ **Mini felaket sistemi**
    - `MiniDisasterManager` sınıfı
    - Rastgele spawn sistemi

16. ✅ **Buff/Debuff sistemleri**
    - Boss güçlenme
    - Mob güçlenme
    - Oyuncu buff

### Faz 7: Admin Komutları (Öncelik: YÜKSEK)

17. ✅ **Test komutları**
    - `/stratocraft disaster test <type> <level> <location>`
    - `/stratocraft disaster test group <entity> <count> <location>`
    - `/stratocraft disaster test swarm <entity> <count> <location>`
    - `/stratocraft disaster test mini <type>`

### Faz 8: Dökümantasyon (Öncelik: ORTA)

18. ✅ **Döküman güncelleme**
    - `10_felaketler.md` güncelleme
    - Yeni özellikler ekleme
    - Admin komutları ekleme

---

## 🔧 TEKNİK DETAYLAR

### Klan Kristali Bulma Algoritması

```java
public Location findNearestCrystal(Location from) {
    Location nearest = null;
    double minDistance = Double.MAX_VALUE;
    
    for (Clan clan : clanManager.getAllClans()) {
        if (clan.getCrystalLocation() == null) continue;
        
        Location crystalLoc = clan.getCrystalLocation();
        double distance = from.distance(crystalLoc);
        
        if (distance < minDistance) {
            minDistance = distance;
            nearest = crystalLoc;
        }
    }
    
    return nearest;
}
```

### 2 Dakikada Bir Saldırı

```java
private long lastAttackTime = 0;
private static final long ATTACK_INTERVAL = 2400L; // 2 dakika = 2400 tick

if (System.currentTimeMillis() - lastAttackTime >= ATTACK_INTERVAL * 50) {
    attackNearbyPlayers(disaster, entity);
    lastAttackTime = System.currentTimeMillis();
}
```

### Grup Felaket Spawn

```java
public void spawnGroupDisaster(EntityType entityType, int count, Location center) {
    List<Entity> entities = new ArrayList<>();
    
    for (int i = 0; i < count; i++) {
        // Rastgele konum (center etrafında 20 blok yarıçap)
        Location spawnLoc = center.clone().add(
            (random.nextDouble() - 0.5) * 40,
            0,
            (random.nextDouble() - 0.5) * 40
        );
        
        Entity entity = world.spawnEntity(spawnLoc, entityType);
        // Güçlendirme
        strengthenEntity(entity);
        entities.add(entity);
    }
    
    // Her birine hedef kristal atama
    Location targetCrystal = findNearestCrystal(center);
    for (Entity entity : entities) {
        setEntityTarget(entity, targetCrystal);
    }
}
```

---

## 📊 PERFORMANS OPTİMİZASYONLARI

1. **Chunk Yönetimi**
   - Sadece aktif felaketlerin chunk'ları yüklenir
   - Felaket bittiğinde chunk'lar unload edilir

2. **Entity Yönetimi**
   - Mini felaket dalgası için entity limiti (max 500)
   - Performans düşerse spawn durdurulur

3. **Hedef Bulma Cache**
   - Klan kristalleri cache'lenir
   - 10 saniyede bir güncellenir

---

## 🎮 OYUNCU DENEYİMİ

### Uyarılar
- Felaket spawn olmadan 2 dakika önce uyarı
- BossBar ile felaket bilgisi
- Klan kristali hedef alındığında uyarı

### Ödüller
- Felaket yok edilince ödül
- Klan kristali korunursa bonus ödül
- Mini felaketlerden küçük ödüller

---

## ✅ ONAY BEKLENEN NOKTALAR

1. **Felaket Güç Seviyeleri** - Onaylanmalı
2. **Spawn Sıklıkları** - Onaylanmalı
3. **Mini Felaket Detayları** - Onaylanmalı
4. **Admin Komut Formatı** - Onaylanmalı
5. **Döküman İçeriği** - Onaylanmalı

---

**Plan Hazırlandı: 2024**
**Durum: Onay Bekleniyor**
