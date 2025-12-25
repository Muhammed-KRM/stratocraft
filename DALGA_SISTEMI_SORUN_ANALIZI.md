# DALGA SİSTEMİ SORUN ANALİZİ

## 🔍 SON 2 COMMIT'TE YAPILAN DEĞİŞİKLİKLER

### Commit 1: `a9317df` - WildCreeper ve NightWaveManager Refactor
- ✅ WildCreeper zıplama mantığı düzeltildi
- ✅ Boss spawning logic optimize edildi
- ✅ findSpawnedBoss metodu eklendi

### Commit 2: `e9a4ed5` - Sistem İyileştirmeleri
- ✅ Location deserialization düzeltmeleri
- ✅ Mob AI behavior iyileştirmeleri
- ✅ Territory management güncellemeleri
- ✅ WildCreeper zıplama mekaniği düzeltildi (sürekli uçma sorunu)

---

## 🐛 TESPİT EDİLEN SORUNLAR

### 1. ❌ DALGA BAŞLATMA MANTIĞI SORUNU

**Sorun:** `checkAndStartWaves()` metodunda gece kontrolü yanlış

**Mevcut Kod:**
```java
boolean isNight = time >= startTime || time < endTime;
```

**Problem:**
- `startTime = 18000` (gece yarısı)
- `endTime = 0` (güneş doğuşu)
- `time >= 18000 || time < 0` → Bu her zaman `true` döner!
- Minecraft'ta `time` değeri 0-24000 arasındadır
- `time < 0` hiçbir zaman `true` olmaz (time her zaman >= 0)

**Doğru Mantık:**
```java
// Gece: 18000-24000 veya 0-6000 (güneş doğuşuna kadar)
boolean isNight = time >= startTime || time < endTime;
// Ama endTime = 0 olduğu için time < 0 hiçbir zaman true olmaz!
```

**Çözüm:**
```java
// Gece: 18000-24000 arası (gece yarısından güneş doğuşuna kadar)
// Ama endTime = 0 olduğu için, gece kontrolü: time >= 18000
boolean isNight = time >= startTime; // 18000-24000 arası
```

**VEYA:**

```java
// Gece: 18000-24000 veya 0-6000 (güneş doğuşuna kadar)
// endTime = 0 ise, gece: time >= 18000 || time < 6000
// Ama config'de endTime = 0, bu yüzden:
boolean isNight = time >= startTime || (endTime > 0 && time < endTime);
// Veya daha basit:
boolean isNight = time >= startTime; // Gece yarısından sonra
```

---

### 2. ❌ DALGA DURDURMA MANTIĞI SORUNU

**Sorun:** `checkAndStartWaves()` metodunda güneş doğuşu kontrolü yanlış

**Mevcut Kod:**
```java
// Güneş doğuşu kontrolü (endTime ± 100 tick tolerans)
if (!isNight && activeWaves.getOrDefault(world, false)) {
    // Güneş doğdu mu? (endTime ± 100)
    if (time >= (endTime - 100) && time <= (endTime + 100)) {
        stopWave(world);
    }
}
```

**Problem:**
- `endTime = 0` (güneş doğuşu)
- `time >= (0 - 100) && time <= (0 + 100)` → `time >= -100 && time <= 100`
- `time >= -100` her zaman `true` (time >= 0)
- Bu kontrol her zaman çalışır ve dalgayı durdurur!

**Çözüm:**
```java
// Güneş doğuşu: time = 0 (veya 0'a yakın)
if (activeWaves.getOrDefault(world, false)) {
    // Güneş doğdu mu? (0 ± 100 tick tolerans)
    if (time >= 0 && time <= 100) {
        stopWave(world);
    }
}
```

**VEYA:**

```java
// Gece bitti mi? (time < startTime ve time >= endTime)
// Ama endTime = 0 olduğu için:
if (activeWaves.getOrDefault(world, false)) {
    // Gece bitti mi? (time < 18000 ve time >= 0)
    if (time < startTime && time >= endTime) {
        stopWave(world);
    }
}
```

---

### 3. ⚠️ SPAWN INTERVAL HESAPLAMA SORUNU

**Sorun:** `startSpawnForAllClans()` metodunda spawn interval hesaplaması yanlış

**Mevcut Kod:**
```java
waveTick += spawnIntervalInitial; // İlk interval
// ...
long spawnInterval = waveTick < speedIncreaseTime ? spawnIntervalInitial : spawnIntervalFast;
if (waveTick % spawnInterval == 0) {
    spawnMobsForClan(clan);
}
```

**Problem:**
- `waveTick` her task çalışmasında `spawnIntervalInitial` (200) artırılıyor
- Ama task `spawnIntervalInitial` (200 tick) aralıklarla çalışıyor
- Bu yüzden `waveTick` çok hızlı artıyor ve spawn mantığı bozuluyor

**Çözüm:**
```java
private long waveTick = 0;

@Override
public void run() {
    if (!activeWaves.getOrDefault(world, false)) {
        cancel();
        return;
    }
    
    waveTick += spawnIntervalInitial; // Her task çalışmasında artır
    
    // Spawn interval hesapla
    long spawnInterval = waveTick < speedIncreaseTime ? spawnIntervalInitial : spawnIntervalFast;
    
    // Her spawn interval'da spawn yap
    if (waveTick % spawnInterval == 0) {
        // Tüm klanlar için spawn
        for (Clan clan : currentClans) {
            spawnMobsForClan(clan);
        }
    }
}
```

**VEYA DAHA BASİT:**

```java
private int spawnCounter = 0;

@Override
public void run() {
    if (!activeWaves.getOrDefault(world, false)) {
        cancel();
        return;
    }
    
    spawnCounter++;
    
    // Spawn interval hesapla
    long spawnInterval = spawnCounter * spawnIntervalInitial < speedIncreaseTime ? 
        spawnIntervalInitial : spawnIntervalFast;
    
    // Her spawn interval'da spawn yap
    if (spawnCounter % (spawnInterval / spawnIntervalInitial) == 0) {
        for (Clan clan : currentClans) {
            spawnMobsForClan(clan);
        }
    }
}
```

---

### 4. ⚠️ İLK SPAWN MANTIĞI

**Sorun:** `startSpawnForAllClans()` metodunda ilk spawn hemen yapılıyor, sonra task başlatılıyor

**Mevcut Kod:**
```java
for (Clan clan : allClans) {
    startSpawnForClan(clan); // Hemen spawn
}

// Sonra task başlatılıyor
spawnTask.runTaskTimer(plugin, 0L, spawnIntervalInitial);
```

**Problem:**
- İlk spawn hemen yapılıyor (hemen)
- Sonra task başlatılıyor ve tekrar spawn yapıyor
- Bu çift spawn'a neden olabilir

**Çözüm:**
```java
// İlk spawn'ı task içinde yap
spawnTask.runTaskTimer(plugin, 0L, spawnIntervalInitial);
// Task ilk çalıştığında spawn yapacak
```

---

## ✅ ÖNERİLEN DÜZELTMELER

### 1. Gece Kontrolü Düzeltmesi

```java
private void checkAndStartWaves() {
    for (World world : Bukkit.getWorlds()) {
        if (world.getEnvironment() != org.bukkit.World.Environment.NORMAL) {
            continue;
        }
        
        long time = world.getTime();
        
        // ✅ DÜZELTME: Gece kontrolü
        // Gece: time >= 18000 (gece yarısından sonra)
        // Güneş doğuşu: time = 0 (veya 0'a yakın)
        boolean isNight = time >= startTime; // 18000-24000 arası
        
        // Gece yarısı kontrolü (startTime ± 100 tick tolerans)
        if (isNight && !activeWaves.getOrDefault(world, false)) {
            if (time >= (startTime - 100) && time <= (startTime + 100)) {
                startWave(world);
            }
        }
        
        // ✅ DÜZELTME: Güneş doğuşu kontrolü
        // Güneş doğuşu: time = 0 (veya 0'a yakın)
        if (activeWaves.getOrDefault(world, false)) {
            // Gece bitti mi? (time < startTime veya time = 0'a yakın)
            if (time < startTime && time <= 100) {
                stopWave(world);
            }
        }
    }
}
```

### 2. Spawn Interval Düzeltmesi

```java
BukkitRunnable spawnTask = new BukkitRunnable() {
    private long waveTick = 0;
    
    @Override
    public void run() {
        if (!activeWaves.getOrDefault(world, false)) {
            cancel();
            return;
        }
        
        // ✅ DÜZELTME: waveTick'i task interval'ına göre artır
        waveTick += spawnIntervalInitial;
        
        // Spawn interval hesapla
        long spawnInterval = waveTick < speedIncreaseTime ? spawnIntervalInitial : spawnIntervalFast;
        
        // ✅ DÜZELTME: Spawn kontrolü
        // Her spawn interval'da spawn yap
        if (waveTick % spawnInterval == 0) {
            List<Clan> currentClans = new ArrayList<>(territoryManager.getClanManager().getAllClans());
            for (Clan clan : currentClans) {
                if (clan.getCrystalLocation() == null || 
                    !clan.getCrystalLocation().getWorld().equals(world)) {
                    continue;
                }
                spawnMobsForClan(clan);
            }
        }
    }
};
```

---

## 📊 SONUÇ

**Tespit Edilen Sorunlar:**
1. ❌ Gece kontrolü yanlış (`time < 0` hiçbir zaman true olmaz)
2. ❌ Güneş doğuşu kontrolü yanlış (`time >= -100` her zaman true)
3. ⚠️ Spawn interval hesaplaması karmaşık
4. ⚠️ İlk spawn mantığı çift spawn'a neden olabilir

**Öncelik:**
1. **YÜKSEK:** Gece/güneş kontrolü düzeltilmeli (dalga hiç başlamıyor veya hemen duruyor)
2. **ORTA:** Spawn interval düzeltilmeli (spawn çok sık veya hiç olmuyor)
3. **DÜŞÜK:** İlk spawn mantığı iyileştirilebilir

