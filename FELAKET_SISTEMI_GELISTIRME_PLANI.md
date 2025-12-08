# 🌋 FELAKET SİSTEMİ GELİŞTİRME PLANI

## 📊 MEVCUT DURUM ANALİZİ

### ✅ ÇALIŞAN ÖZELLİKLER
1. Temel felaket spawn sistemi
2. Klan kristali bulma (findNearestCrystal)
3. 2 dakikada bir oyuncu saldırısı
4. Kristal yok etme
5. Doğa olayları (Deprem, Fırtına, Güneş Patlaması)
6. Admin test komutları

### ❌ EKSİK/ÇALIŞMAYAN ÖZELLİKLER

#### 1. **Grup Felaketler İçin Hareket Sistemi YOK**
- 30 adet veya 100-500 adet entity'ler spawn oluyor
- Ama kristale gitmiyorlar (AI yok)
- Sadece tek boss felaketler hareket ediyor

#### 2. **Hardcoded Değerler**
- Spawn mesafesi: 5000 blok (sabit)
- Saldırı aralığı: 2 dakika (sabit)
- Saldırı yarıçapı: 30 blok (sabit)
- Kristal yakınlık: 5 blok (sabit)
- Cache güncelleme: 10 saniye (sabit)
- Her felaket tipi için özel değerler (zıplama süresi, patlama gücü, vs.)

#### 3. **Config Entegrasyonu YOK**
- Felaket güçleri config'den okunmuyor
- Her felaket tipi için ayrı config yok
- Spawn sıklıkları config'den okunmuyor
- Özel yetenek değerleri config'den okunmuyor

#### 4. **Modüler Yapı Eksik**
- Her felaket tipi için ayrı handler (tekrar eden kodlar)
- Ortak fonksiyonlar yok
- Entity güçlendirme tekrar ediyor
- Hareket mantığı tekrar ediyor

#### 5. **Destek Fonksiyonları Eksik**
- Entity güçlendirme fonksiyonu yok
- Entity hedefleme fonksiyonu yok
- Blok kırma fonksiyonu yok
- Patlama oluşturma fonksiyonu yok
- Partikül efekti fonksiyonu yok

#### 6. **Entity AI Sistemi YOK**
- Grup felaketler için pathfinding yok
- Entity'lerin hedefe gitmesi için AI yok
- Sıkışma önleme sistemi eksik

---

## 🏗️ YENİ MİMARİ TASARIM

### 1. CONFIG SİSTEMİ

#### `DisasterConfig.java` (Yeni Sınıf)
Her felaket tipi için ayrı config sınıfı:

```java
public class DisasterConfig {
    // Genel Ayarlar
    private double spawnDistance;          // Spawn mesafesi (blok)
    private long attackInterval;           // Oyuncu saldırı aralığı (ms)
    private double attackRadius;           // Saldırı yarıçapı (blok)
    private double crystalProximity;       // Kristal yakınlık (blok)
    private long crystalCacheInterval;     // Cache güncelleme (ms)
    
    // Güç Ayarları
    private double baseHealth;             // Temel can
    private double baseDamage;              // Temel hasar
    private double healthMultiplier;        // Can çarpanı
    private double damageMultiplier;       // Hasar çarpanı
    
    // Hareket Ayarları
    private double moveSpeed;               // Hareket hızı
    private double jumpHeight;              // Zıplama yüksekliği
    private boolean canJump;                // Zıplama yapabilir mi?
    private boolean canTeleport;           // Işınlanabilir mi?
    
    // Özel Yetenekler
    private long abilityCooldown;          // Yetenek cooldown (ms)
    private double explosionPower;        // Patlama gücü
    private int blockBreakRadius;          // Blok kırma yarıçapı
    // ... her felaket için özel ayarlar
}
```

#### Config YAML Yapısı
```yaml
disaster:
  # Genel Ayarlar
  general:
    spawn-distance: 5000          # Spawn mesafesi (blok)
    attack-interval: 120000       # Oyuncu saldırı aralığı (ms) - 2 dakika
    attack-radius: 30             # Saldırı yarıçapı (blok)
    crystal-proximity: 5          # Kristal yakınlık (blok)
    crystal-cache-interval: 10000 # Cache güncelleme (ms)
    chunk-unload-delay: 200       # Chunk unload gecikmesi (tick)
  
  # Güç Hesaplama
  power:
    player-multiplier: 0.1        # Oyuncu başına çarpan
    clan-multiplier: 0.15         # Klan seviyesi başına çarpan
  
  # Seviye Bazlı Güçler
  levels:
    level1:
      base-health: 500
      base-damage: 1.0
      health-multiplier: 1.0
      damage-multiplier: 1.0
    level2:
      base-health: 1500
      base-damage: 2.0
      health-multiplier: 1.5
      damage-multiplier: 1.5
    level3:
      base-health: 5000
      base-damage: 5.0
      health-multiplier: 2.0
      damage-multiplier: 2.0
    level4:
      base-health: 10000
      base-damage: 10.0
      health-multiplier: 3.0
      damage-multiplier: 3.0
  
  # Felaket Tipi Bazlı Ayarlar
  types:
    TITAN_GOLEM:
      move-speed: 0.4
      jump-height: 1.2
      can-jump: true
      can-teleport: false
      jump-interval-min: 300      # Tick
      jump-interval-max: 400      # Tick
      block-throw-interval: 200   # Tick
      explosion-interval: 200     # Tick
      explosion-power: 4.0
      block-break-radius: 3
      passive-explosion-power: 2.0
    
    ABYSSAL_WORM:
      move-speed: 0.3
      jump-height: 0.0
      can-jump: false
      can-teleport: true
      teleport-distance: 5
      dig-speed: 1.0
    
    CHAOS_DRAGON:
      move-speed: 0.5
      jump-height: 0.0
      can-jump: false
      can-teleport: false
      fire-breath-chance: 5       # %
      fire-breath-range: 50
      fire-damage: 5.0
    
    VOID_TITAN:
      move-speed: 0.3
      jump-height: 0.0
      can-jump: false
      can-teleport: false
      void-explosion-chance: 3    # %
      void-explosion-power: 4.0
      void-explosion-radius: 10
    
    ICE_LEVIATHAN:
      move-speed: 0.3
      jump-height: 0.0
      can-jump: false
      can-teleport: false
      freeze-chance: 5            # %
      freeze-radius: 30
      freeze-duration: 100        # Tick
      ice-conversion-chance: 30   # %
      ice-conversion-radius: 5
    
    ZOMBIE_HORDE:
      move-speed: 0.3
      jump-height: 0.0
      can-jump: false
      can-teleport: false
      group-size: 30
      spawn-radius: 20
    
    CREEPER_SWARM:
      move-speed: 0.2
      jump-height: 0.0
      can-jump: false
      can-teleport: false
      group-size-min: 100
      group-size-max: 500
      spawn-radius: 50
      health-percentage: 0.2      # %20 güç
    
    SOLAR_FLARE:
      fire-tick-duration: 100     # Tick
      flammable-chance-log: 0.05  # %
      flammable-chance-other: 0.15 # %
      lava-spawn-chance: 0.02     # %
      scan-radius: 5
    
    EARTHQUAKE:
      explosion-chance: 5         # %
      explosion-radius: 20
      explosion-power: 3.0
      damage-interval: 40         # Tick
      damage-amount: 2.0
      block-fall-radius: 2
      block-fall-height: 5
    
    STORM:
      lightning-chance-nearby: 3  # %
      lightning-chance-random: 1  # %
      lightning-radius: 10
      lightning-damage: 10.0
      lightning-damage-radius: 5
```

---

### 2. DESTEK FONKSİYONLARI

#### `DisasterUtils.java` (Yeni Sınıf)
Ortak fonksiyonlar:

```java
public class DisasterUtils {
    // Entity güçlendirme
    public static void strengthenEntity(Entity entity, DisasterConfig config);
    
    // Entity hedefleme (AI)
    public static void setEntityTarget(Entity entity, Location target, DisasterConfig config);
    
    // Blok kırma
    public static void breakBlocks(Location center, int radius, DisasterConfig config);
    
    // Patlama oluşturma
    public static void createExplosion(Location loc, double power, boolean breakBlocks);
    
    // Partikül efekti
    public static void playEffect(Location loc, Particle particle, int count);
    
    // Mesafe hesaplama
    public static double calculateDistance(Location from, Location to);
    
    // Yön hesaplama
    public static Vector calculateDirection(Location from, Location to);
    
    // Güvenli konum bulma
    public static Location findSafeLocation(Location center, int radius);
    
    // Chunk yükleme
    public static void loadChunk(Location loc, boolean force);
}
```

#### `DisasterBehavior.java` (Yeni Sınıf)
Felaket davranış mantığı:

```java
public class DisasterBehavior {
    // Hareket
    public static void moveToTarget(Entity entity, Location target, DisasterConfig config);
    
    // Saldırı
    public static void attackPlayers(Entity entity, Location center, DisasterConfig config);
    
    // Blok kırma
    public static void breakBlocksInPath(Entity entity, Location target, DisasterConfig config);
    
    // Sıkışma önleme
    public static void preventStuck(Entity entity, Location target, DisasterConfig config);
    
    // Grup hareketi (30 adet veya 100-500 adet için)
    public static void moveGroupToTarget(List<Entity> entities, Location target, DisasterConfig config);
}
```

#### `DisasterEntityAI.java` (Yeni Sınıf)
Entity AI sistemi (grup felaketler için):

```java
public class DisasterEntityAI {
    // Entity'yi hedefe yönlendir
    public static void navigateToTarget(Entity entity, Location target, DisasterConfig config);
    
    // Pathfinding (basit)
    public static Location findPath(Location from, Location to, int maxDistance);
    
    // Engelleri aş
    public static void avoidObstacles(Entity entity, Location target, DisasterConfig config);
    
    // Grup AI (tüm entity'ler birlikte hareket eder)
    public static void updateGroupAI(List<Entity> entities, Location target, DisasterConfig config);
}
```

---

### 3. MODÜLER HANDLER SİSTEMİ

#### `DisasterHandler.java` (Interface)
```java
public interface DisasterHandler {
    void handle(Disaster disaster, Entity entity, DisasterConfig config);
    void handleGroup(Disaster disaster, List<Entity> entities, DisasterConfig config);
}
```

#### Her Felaket İçin Handler
```java
public class TitanGolemHandler implements DisasterHandler {
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Titan Golem özel mantığı
    }
}

public class AbyssalWormHandler implements DisasterHandler {
    @Override
    public void handle(Disaster disaster, Entity entity, DisasterConfig config) {
        // Hiçlik Solucanı özel mantığı
    }
}
```

---

### 4. CONFIG YÖNETİMİ

#### `DisasterConfigManager.java` (Yeni Sınıf)
```java
public class DisasterConfigManager {
    private final Map<Disaster.Type, DisasterConfig> configs = new HashMap<>();
    
    public void loadConfigs(FileConfiguration config);
    public DisasterConfig getConfig(Disaster.Type type);
    public DisasterConfig getConfigForLevel(int level);
}
```

---

## 📝 YAPILACAKLAR LİSTESİ

### Faz 1: Config Sistemi (Öncelik: YÜKSEK)

1. ✅ **DisasterConfig.java oluştur**
   - Her felaket tipi için config sınıfı
   - Tüm ayarlanabilir değerler

2. ✅ **ConfigManager'a felaket ayarları ekle**
   - Config yükleme
   - Getter metodları

3. ✅ **config.yml'a felaket ayarları ekle**
   - Genel ayarlar
   - Seviye bazlı güçler
   - Her felaket tipi için özel ayarlar

### Faz 2: Destek Fonksiyonları (Öncelik: YÜKSEK)

4. ✅ **DisasterUtils.java oluştur**
   - Entity güçlendirme
   - Blok kırma
   - Patlama oluşturma
   - Partikül efektleri
   - Mesafe/yön hesaplama

5. ✅ **DisasterBehavior.java oluştur**
   - Hareket mantığı
   - Saldırı mantığı
   - Blok kırma mantığı
   - Sıkışma önleme

6. ✅ **DisasterEntityAI.java oluştur**
   - Entity hedefleme
   - Pathfinding
   - Grup AI

### Faz 3: Modüler Handler Sistemi (Öncelik: ORTA)

7. ✅ **DisasterHandler interface oluştur**
   - handle() metodu
   - handleGroup() metodu

8. ✅ **Her felaket için handler oluştur**
   - TitanGolemHandler
   - AbyssalWormHandler
   - ChaosDragonHandler
   - VoidTitanHandler
   - IceLeviathanHandler
   - GroupDisasterHandler (30 adet için)
   - SwarmDisasterHandler (100-500 adet için)

9. ✅ **DisasterTask'ı refactor et**
   - Handler sistemi kullan
   - Tekrar eden kodları kaldır

### Faz 4: Grup Felaketler Hareket Sistemi (Öncelik: YÜKSEK)

10. ✅ **Grup entity'ler için AI ekle**
    - Her entity'yi hedefe yönlendir
    - Pathfinding
    - Sıkışma önleme

11. ✅ **DisasterTask'a grup hareketi ekle**
    - Grup felaketler için özel işleme
    - Her entity'yi ayrı ayrı yönet

### Faz 5: Config Entegrasyonu (Öncelik: YÜKSEK)

12. ✅ **Tüm hardcoded değerleri config'den oku**
    - Spawn mesafesi
    - Saldırı aralığı
    - Yarıçaplar
    - Güç değerleri

13. ✅ **Her felaket tipi için config yükleme**
    - DisasterConfigManager kullan
    - Config'den oku ve ayarla

### Faz 6: Kod Temizliği (Öncelik: ORTA)

14. ✅ **Tekrar eden kodları kaldır**
    - Ortak fonksiyonlara taşı
    - DRY prensibi

15. ✅ **Kod organizasyonu**
    - Paket yapısı
    - Sınıf sorumlulukları
    - İsimlendirme

---

## 🔧 TEKNİK DETAYLAR

### Entity AI Sistemi

Grup felaketler için basit AI:

```java
public static void navigateToTarget(Entity entity, Location target, DisasterConfig config) {
    Location current = entity.getLocation();
    Vector direction = target.toVector().subtract(current.toVector()).normalize();
    
    // Hareket hızı config'den
    double speed = config.getMoveSpeed();
    Vector velocity = direction.multiply(speed);
    
    // Y eksenini sıfırla (uçmayı engelle)
    velocity.setY(0);
    
    // Önünde engel var mı kontrol et
    Block frontBlock = current.clone().add(direction).getBlock();
    if (frontBlock.getType() != Material.AIR) {
        // Sıkışma önleme
        if (config.canJump()) {
            velocity.setY(config.getJumpHeight());
        } else if (config.canTeleport()) {
            Location teleportLoc = findSafeLocation(current, 5);
            entity.teleport(teleportLoc);
            return;
        }
    }
    
    entity.setVelocity(velocity);
}
```

### Config Yükleme

```java
public void loadDisasterConfigs(FileConfiguration config) {
    // Genel ayarlar
    DisasterConfig generalConfig = new DisasterConfig();
    generalConfig.setSpawnDistance(config.getDouble("disaster.general.spawn-distance", 5000));
    generalConfig.setAttackInterval(config.getLong("disaster.general.attack-interval", 120000));
    // ...
    
    // Her felaket tipi için
    for (Disaster.Type type : Disaster.Type.values()) {
        String path = "disaster.types." + type.name();
        DisasterConfig typeConfig = generalConfig.clone();
        
        // Tip'e özel ayarlar
        if (config.contains(path)) {
            typeConfig.setMoveSpeed(config.getDouble(path + ".move-speed", 0.3));
            typeConfig.setJumpHeight(config.getDouble(path + ".jump-height", 0.0));
            // ...
        }
        
        configs.put(type, typeConfig);
    }
}
```

---

## ✅ ONAY BEKLENEN NOKTALAR

1. **Config Yapısı** - Uygun mu?
2. **Destek Fonksiyonları** - Yeterli mi?
3. **Modüler Yapı** - Uygun mu?
4. **Entity AI Sistemi** - Yeterli mi?
5. **Kod Organizasyonu** - Uygun mu?

---

**Plan Hazırlandı: 2024**
**Durum: Onay Bekleniyor**
