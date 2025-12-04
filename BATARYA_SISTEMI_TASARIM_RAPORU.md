# 🔋 STRATOCRAFT BATARYA SİSTEMİ TASARIM RAPORU

## 📋 Rapor Özeti

Bu rapor, **Stratocraft Batarya Anayasası** kurallarına göre yeni batarya tasarımlarını ve kod mantığını içerir.

**Tarih**: 2024
**Versiyon**: 2.0 (Anayasa Uyumlu)

---

## 📜 BATARYA ANAYASASI KURALLARI (Özet)

1. **Tek Kimlik**: Bir blok türü sadece TEK BİR batarya için kullanılabilir
2. **Seviye Sistemi**: Seviye güç/karmaşıklık gösterir (1-5)
3. **Yakıt Etkisi**: Yakıt etkiyi artırır (Demir → Elmas → Kızıl Elmas → Karanlık Madde)
4. **Hibrit Yapılar**: Farklı blokların karışımı yeni batarya sayılır
5. **Benzersizlik**: Hiçbir batarya aynı işlevi yapamaz

---

## 🎯 YENİ BATARYA TASARIMLARI

### ⚔️ SALDIRI BATARYALARI (5 Seviye Örnekleri)

#### Seviye 1: Yıldırım Asası (Lightning Staff)

**Blok Yapısı**: 3x **Demir Bloğu** (üst üste)

**İşlevi**: Manuel nişanlı yıldırım
- Oyuncunun baktığı tek bir noktaya yıldırım düşürür
- RayTrace ile hedef belirlenir
- Tek vuruş, güçlü hasar

**Kod Mantığı**:
```java
// BatteryType enum
ATTACK_LIGHTNING_STAFF_L1("Yıldırım Asası", BatteryCategory.ATTACK, 1, Material.IRON_BLOCK, null)

// Ateşleme metodu
private void fireLightningStaff(Player player, BatteryData data) {
    Location target = getTargetLocation(player, 50); // RayTrace ile hedef
    player.getWorld().strikeLightning(target);
    // Hasar: 10 kalp (yakıt ile artar)
}
```

**Yakıt Etkisi**:
- Demir: 1 yıldırım, 10 kalp hasar
- Elmas: 1 yıldırım, 15 kalp hasar
- Kızıl Elmas: 1 yıldırım, 20 kalp hasar + zincirleme (3 hedef)
- Karanlık Madde: 1 yıldırım, 30 kalp hasar + zincirleme (5 hedef)

**Benzersizlik**: Tek nokta nişanlı yıldırım (Tesla Kulesi'nden farklı)

---

#### Seviye 2: Çift Ateş Topu (Double Fireball)

**Blok Yapısı**: 5x **Magma Bloğu** (üst üste) + **Nethrack** (yan blok)

**İşlevi**: İki ateş topu atışı
- Aynı anda iki ateş topu fırlatır
- Paralel veya çapraz atış
- Orta menzil hasar

**Kod Mantığı**:
```java
ATTACK_DOUBLE_FIREBALL_L2("Çift Ateş Topu", BatteryCategory.ATTACK, 2, Material.MAGMA_BLOCK, Material.NETHERRACK)

private void fireDoubleFireball(Player player, BatteryData data) {
    Location spawnLoc = player.getEyeLocation();
    Vector direction = player.getLocation().getDirection();
    
    // İki ateş topu (paralel)
    Fireball fb1 = spawnLoc.getWorld().spawn(spawnLoc.clone().add(0.5, 0, 0), Fireball.class);
    Fireball fb2 = spawnLoc.getWorld().spawn(spawnLoc.clone().add(-0.5, 0, 0), Fireball.class);
    
    fb1.setVelocity(direction.multiply(1.5));
    fb2.setVelocity(direction.multiply(1.5));
}
```

**Benzersizlik**: İki top aynı anda (Cehennem Topu'ndan farklı)

---

#### Seviye 3: Meteor Yağmuru (Meteor Shower)

**Blok Yapısı**: 7x **Obsidyen** (üst üste) + **Magma Bloğu** (yan blok)

**İşlevi**: Gökyüzünden meteor yağdırma
- Hedef noktanın üzerinden meteor düşer
- 5 meteor (yakıt ile artar)
- Blok kırar (savaşta olan klan alanlarında)

**Kod Mantığı**:
```java
ATTACK_METEOR_SHOWER_L3("Meteor Yağmuru", BatteryCategory.ATTACK, 3, Material.OBSIDIAN, Material.MAGMA_BLOCK)

private void fireMeteorShower(Player player, BatteryData data) {
    Location target = getTargetLocation(player, 30);
    int meteorCount = 5 * getFuelMultiplier(data.getFuel());
    
    for (int i = 0; i < meteorCount; i++) {
        Location skyLoc = target.clone().add(
            (Math.random() - 0.5) * 10, 
            30 + Math.random() * 10, 
            (Math.random() - 0.5) * 10
        );
        
        Fireball meteor = skyLoc.getWorld().spawn(skyLoc, Fireball.class);
        meteor.setDirection(new Vector(0, -1, 0));
        meteor.setYield(8.0f);
    }
}
```

**Benzersizlik**: Gökyüzünden düşen meteor (Cehennem Topu'ndan farklı)

---

#### Seviye 4: Tesla Kulesi (Tesla Tower)

**Blok Yapısı**: 9x **Bakır Bloğu** (üst üste) + **Redstone Bloğu** (yan blok)

**İşlevi**: Otomatik alan etkili elektrik
- Oyuncunun etrafındaki düşmanlara otomatik elektrik verir
- 30 saniye süre
- Sürekli hasar (her 2 saniyede bir)

**Kod Mantığı**:
```java
ATTACK_TESLA_TOWER_L4("Tesla Kulesi", BatteryCategory.ATTACK, 4, Material.COPPER_BLOCK, Material.REDSTONE_BLOCK)

private void fireTeslaTower(Player player, BatteryData data) {
    int duration = 30 * 20; // 30 saniye (tick)
    double radius = 15.0;
    
    new BukkitRunnable() {
        int ticks = 0;
        @Override
        public void run() {
            if (ticks >= duration || !player.isOnline()) {
                cancel();
                return;
            }
            
            // Her 2 saniyede bir (40 tick)
            if (ticks % 40 == 0) {
                for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                    if (e instanceof LivingEntity && e != player) {
                        // Elektrik hasarı
                        ((LivingEntity) e).damage(5, player);
                        e.getWorld().strikeLightningEffect(e.getLocation());
                    }
                }
            }
            ticks++;
        }
    }.runTaskTimer(plugin, 0L, 1L);
}
```

**Benzersizlik**: Otomatik alan etkili (Yıldırım Asası'ndan farklı)

---

#### Seviye 5: Kıyamet Reaktörü (Apocalypse Reactor)

**Blok Yapısı**: 11x **Bedrock** (üst üste) + **End Crystal** (üstte) + **Beacon** (altta)

**İşlevi**: Tüm elementlerin kombinasyonu
- Meteor yağmuru
- Yıldırım fırtınası
- Ölüm bulutu
- Buz çağı
- 40x40 alan etkisi

**Kod Mantığı**:
```java
ATTACK_APOCALYPSE_REACTOR_L5("Kıyamet Reaktörü", BatteryCategory.ATTACK, 5, Material.BEDROCK, Material.END_CRYSTAL)

private void fireApocalypseReactor(Player player, BatteryData data) {
    Location target = getTargetLocation(player, 50);
    int areaSize = 40;
    
    // 1. Meteor yağmuru
    spawnMeteorShower(target, 20);
    
    // 2. Yıldırım fırtınası
    spawnLightningStorm(target, areaSize, 10);
    
    // 3. Ölüm bulutu
    spawnDeathCloud(target, areaSize, 15);
    
    // 4. Buz çağı
    freezeArea(target, areaSize, 10);
}
```

**Benzersizlik**: Tüm elementlerin kombinasyonu (benzersiz felaket)

---

### 🏗️ OLUŞTURMA BATARYALARI (5 Seviye Örnekleri)

#### Seviye 1: Taş Köprü (Stone Bridge)

**Blok Yapısı**: 3x **Taş Bloğu** (üst üste)

**İşlevi**: Basit köprü oluşturma
- Oyuncunun baktığı yöne 10 blok uzunlukta taş köprü yapar
- Sadece savaşta olan klan alanlarında çalışır

**Kod Mantığı**:
```java
CONSTRUCTION_STONE_BRIDGE_L1("Taş Köprü", BatteryCategory.CONSTRUCTION, 1, Material.STONE, null)

private void buildStoneBridge(Player player, BatteryData data) {
    if (!isInSiegeTerritory(player)) {
        player.sendMessage("§cOluşturma bataryaları sadece savaşta olan klan alanlarında çalışır!");
        return;
    }
    
    Location start = getTargetLocation(player, 5);
    Vector direction = player.getLocation().getDirection().normalize();
    int length = 10;
    
    for (int i = 0; i < length; i++) {
        Location blockLoc = start.clone().add(direction.multiply(i));
        blockLoc.getBlock().setType(Material.STONE);
    }
}
```

**Benzersizlik**: Basit köprü (diğer yapılardan farklı)

---

#### Seviye 2: Obsidyen Kafes (Obsidian Cage)

**Blok Yapısı**: 5x **Obsidyen** (üst üste) + **Demir Bloğu** (yan blok)

**İşlevi**: Hapsetme kafesi
- 10x10x5 obsidyen kafes oluşturur
- İçindeki düşmanları hapseder

**Kod Mantığı**:
```java
CONSTRUCTION_OBSIDIAN_CAGE_L2("Obsidyen Kafes", BatteryCategory.CONSTRUCTION, 2, Material.OBSIDIAN, Material.IRON_BLOCK)

private void buildObsidianCage(Player player, BatteryData data) {
    Location center = getTargetLocation(player, 10);
    int size = 10;
    int height = 5;
    
    // Kafes duvarları
    for (int x = -size/2; x <= size/2; x++) {
        for (int z = -size/2; z <= size/2; z++) {
            for (int y = 0; y < height; y++) {
                if (x == -size/2 || x == size/2 || z == -size/2 || z == size/2 || y == 0 || y == height-1) {
                    Location loc = center.clone().add(x, y, z);
                    if (canModifyTerritory(player, loc)) {
                        loc.getBlock().setType(Material.OBSIDIAN);
                    }
                }
            }
        }
    }
}
```

**Benzersizlik**: Hapsetme kafesi (köprüden farklı)

---

#### Seviye 3: Netherite Köprü (Netherite Bridge)

**Blok Yapısı**: 7x **Netherite Bloğu** (üst üste) + **Netherite Külçesi** (yan blok)

**İşlevi**: Güçlü köprü
- 30 blok uzunlukta netherite köprü
- Çok dayanıklı (patlamaya dayanıklı)

**Kod Mantığı**:
```java
CONSTRUCTION_NETHERITE_BRIDGE_L3("Netherite Köprü", BatteryCategory.CONSTRUCTION, 3, Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT)

private void buildNetheriteBridge(Player player, BatteryData data) {
    Location start = getTargetLocation(player, 5);
    Vector direction = player.getLocation().getDirection().normalize();
    int length = 30;
    
    for (int i = 0; i < length; i++) {
        Location blockLoc = start.clone().add(direction.multiply(i));
        if (canModifyTerritory(player, blockLoc)) {
            blockLoc.getBlock().setType(Material.NETHERITE_BLOCK);
        }
    }
}
```

**Benzersizlik**: Uzun ve dayanıklı köprü (taş köprüden farklı)

---

#### Seviye 4: Obsidyen Kale (Obsidian Castle)

**Blok Yapısı**: 9x **Obsidyen** (üst üste) + **End Crystal** (yan blok)

**İşlevi**: Büyük kale
- 20x20x10 obsidyen kale
- Çok katmanlı savunma

**Kod Mantığı**:
```java
CONSTRUCTION_OBSIDIAN_CASTLE_L4("Obsidyen Kale", BatteryCategory.CONSTRUCTION, 4, Material.OBSIDIAN, Material.END_CRYSTAL)

private void buildObsidianCastle(Player player, BatteryData data) {
    Location center = getTargetLocation(player, 10);
    int size = 20;
    int height = 10;
    
    // Kale duvarları ve iç yapı
    for (int x = -size/2; x <= size/2; x++) {
        for (int z = -size/2; z <= size/2; z++) {
            for (int y = 0; y < height; y++) {
                // Duvarlar ve köşeler
                if (isWall(x, z, size) || isCorner(x, z, size)) {
                    Location loc = center.clone().add(x, y, z);
                    if (canModifyTerritory(player, loc)) {
                        loc.getBlock().setType(Material.OBSIDIAN);
                    }
                }
            }
        }
    }
}
```

**Benzersizlik**: Büyük kale (kafes ve köprüden farklı)

---

#### Seviye 5: Netherite Köprü (Efsanevi) (Netherite Bridge Legendary)

**Blok Yapısı**: 11x **Bedrock** (üst üste) + **Beacon** (üstte) + **Beacon** (altta)

**İşlevi**: Efsanevi köprü
- 100 blok uzunlukta netherite köprü
- Çok geniş (5 blok genişlik)
- Patlamaya ve tüm hasarlara dayanıklı

**Kod Mantığı**:
```java
CONSTRUCTION_NETHERITE_BRIDGE_L5("Netherite Köprü (Efsanevi)", BatteryCategory.CONSTRUCTION, 5, Material.BEDROCK, Material.BEACON)

private void buildNetheriteBridgeLegendary(Player player, BatteryData data) {
    Location start = getTargetLocation(player, 5);
    Vector direction = player.getLocation().getDirection().normalize();
    int length = 100;
    int width = 5;
    
    for (int i = 0; i < length; i++) {
        for (int w = -width/2; w <= width/2; w++) {
            Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
            Location blockLoc = start.clone()
                .add(direction.multiply(i))
                .add(perpendicular.multiply(w));
            
            if (canModifyTerritory(player, blockLoc)) {
                blockLoc.getBlock().setType(Material.NETHERITE_BLOCK);
            }
        }
    }
}
```

**Benzersizlik**: Çok uzun ve geniş köprü (diğer köprülerden farklı)

---

### 💚 DESTEK BATARYALARI (5 Seviye Örnekleri)

#### Seviye 1: Can Yenileme (Heal)

**Blok Yapısı**: 3x **Altın Bloğu** (üst üste)

**İşlevi**: Can verme
- Kendine + yakındaki klan üyelerine 5 kalp can verir
- 10 blok yarıçap

**Kod Mantığı**:
```java
SUPPORT_HEAL_L1("Can Yenileme", BatteryCategory.SUPPORT, 1, Material.GOLD_BLOCK, null)

private void fireHeal(Player player, BatteryData data) {
    double radius = 10.0;
    int healAmount = 5; // 5 kalp
    
    // Kendine
    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    player.setHealth(Math.min(player.getHealth() + healAmount, maxHealth));
    
    // Klan üyelerine
    for (Player nearby : getNearbyClanMembers(player, radius)) {
        double nearbyMaxHealth = nearby.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        nearby.setHealth(Math.min(nearby.getHealth() + healAmount, nearbyMaxHealth));
    }
}
```

**Benzersizlik**: Can verme (diğer efektlerden farklı)

---

#### Seviye 2: Hız Artışı (Gelişmiş) (Speed Boost Advanced)

**Blok Yapısı**: 5x **Zümrüt Bloğu** (üst üste) + **Zümrüt** (yan blok)

**İşlevi**: Hız artışı
- Speed III (15 saniye)
- 15 blok yarıçap

**Kod Mantığı**:
```java
SUPPORT_SPEED_L2("Hız Artışı (Gelişmiş)", BatteryCategory.SUPPORT, 2, Material.EMERALD_BLOCK, Material.EMERALD)

private void fireSpeedBoost(Player player, BatteryData data) {
    double radius = 15.0;
    int duration = 15 * 20; // 15 saniye
    int amplifier = 2; // Speed III
    
    // Kendine
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
    
    // Klan üyelerine
    for (Player nearby : getNearbyClanMembers(player, radius)) {
        nearby.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
    }
}
```

**Benzersizlik**: Hız artışı (can vermeden farklı)

---

#### Seviye 3: Hasar Artışı (Güçlü) (Damage Boost Powerful)

**Blok Yapısı**: 7x **Elmas Bloğu** (üst üste) + **Elmas Bloğu** (yan blok)

**İşlevi**: Hasar artışı
- Strength III (20 saniye)
- 20 blok yarıçap

**Kod Mantığı**:
```java
SUPPORT_DAMAGE_L3("Hasar Artışı (Güçlü)", BatteryCategory.SUPPORT, 3, Material.DIAMOND_BLOCK, Material.DIAMOND_BLOCK)

private void fireDamageBoost(Player player, BatteryData data) {
    double radius = 20.0;
    int duration = 20 * 20; // 20 saniye
    int amplifier = 2; // Strength III
    
    // Kendine
    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, amplifier));
    
    // Klan üyelerine
    for (Player nearby : getNearbyClanMembers(player, radius)) {
        nearby.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, amplifier));
    }
}
```

**Benzersizlik**: Hasar artışı (hız ve can vermeden farklı)

---

#### Seviye 4: Zırh Artışı (Çok Güçlü) (Armor Boost Very Powerful)

**Blok Yapısı**: 9x **Demir Bloğu** (üst üste) + **Beacon** (yan blok)

**İşlevi**: Zırh artışı
- Damage Resistance IV (30 saniye)
- 25 blok yarıçap

**Kod Mantığı**:
```java
SUPPORT_ARMOR_L4("Zırh Artışı (Çok Güçlü)", BatteryCategory.SUPPORT, 4, Material.IRON_BLOCK, Material.BEACON)

private void fireArmorBoost(Player player, BatteryData data) {
    double radius = 25.0;
    int duration = 30 * 20; // 30 saniye
    int amplifier = 3; // Damage Resistance IV
    
    // Kendine
    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, amplifier));
    
    // Klan üyelerine
    for (Player nearby : getNearbyClanMembers(player, radius)) {
        nearby.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, amplifier));
    }
}
```

**Benzersizlik**: Zırh artışı (diğer efektlerden farklı)

---

#### Seviye 5: Can Yenileme (Efsanevi) (Heal Legendary)

**Blok Yapısı**: 11x **Bedrock** (üst üste) + **Nether Star** (üstte) + **Beacon** (altta)

**İşlevi**: Efsanevi can verme
- Tam can + 50 kalp ekstra (absorption)
- 30 blok yarıçap

**Kod Mantığı**:
```java
SUPPORT_HEAL_L5("Can Yenileme (Efsanevi)", BatteryCategory.SUPPORT, 5, Material.BEDROCK, Material.NETHER_STAR)

private void fireHealLegendary(Player player, BatteryData data) {
    double radius = 30.0;
    
    // Kendine
    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    player.setHealth(maxHealth);
    player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 24)); // 50 kalp absorption
    
    // Klan üyelerine
    for (Player nearby : getNearbyClanMembers(player, radius)) {
        double nearbyMaxHealth = nearby.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        nearby.setHealth(nearbyMaxHealth);
        nearby.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 24));
    }
}
```

**Benzersizlik**: Tam can + absorption (diğer can vermelerden farklı)

---

## 💻 KOD MANTIĞI VE YAPISI

### 1. Batarya Tespit Sistemi

**Mevcut Kod Yapısı**:
```java
// BatteryListener.java - checkNewBatterySystem()
private boolean checkNewBatterySystem(Player player, Block centerBlock, int slot, PlayerInteractEvent event) {
    // 1. Tüm BatteryType enum'larını kontrol et
    for (BatteryManager.BatteryType batteryType : BatteryManager.BatteryType.values()) {
        Material baseBlock = batteryType.getBaseBlock();
        Material sideBlock = batteryType.getSideBlock();
        
        // 2. Temel blok kontrolü
        if (centerBlock.getType() != baseBlock) continue;
        
        // 3. Üst üste blok kontrolü
        if (below.getType() != baseBlock || above.getType() != baseBlock) continue;
        
        // 4. Seviye tespiti
        int batteryLevel = batteryManager.detectBatteryLevel(centerBlock, baseBlock);
        if (batteryLevel != batteryType.getLevel()) continue;
        
        // 5. Yan blok kontrolü (seviye 2+ için)
        if (sideBlock != null && batteryLevel >= 2) {
            // Yan blok kontrolü
        }
    }
}
```

**Yeni Sistem İçin Gerekli Değişiklikler**:

1. **Tek Kimlik Kontrolü**: Her blok türü için sadece bir batarya olmalı
```java
// BatteryManager.java - Yeni metod
public boolean isBlockTypeUsed(Material blockType) {
    int count = 0;
    for (BatteryType type : BatteryType.values()) {
        if (type.getBaseBlock() == blockType) count++;
    }
    return count > 1; // Birden fazla kullanım varsa hata
}
```

2. **Hibrit Yapı Kontrolü**: Yan blok kombinasyonları kontrol edilmeli
```java
// BatteryListener.java - Hibrit kontrol
private boolean checkHybridStructure(Block centerBlock, Material baseBlock, Material sideBlock) {
    Block east = centerBlock.getRelative(BlockFace.EAST);
    Block west = centerBlock.getRelative(BlockFace.WEST);
    Block north = centerBlock.getRelative(BlockFace.NORTH);
    Block south = centerBlock.getRelative(BlockFace.SOUTH);
    
    return east.getType() == sideBlock || west.getType() == sideBlock ||
           north.getType() == sideBlock || south.getType() == sideBlock;
}
```

3. **Benzersizlik Kontrolü**: Aynı işlevli bataryalar kontrol edilmeli
```java
// BatteryManager.java - Benzersizlik kontrolü
public boolean isFunctionUnique(BatteryType newType) {
    for (BatteryType existing : BatteryType.values()) {
        if (existing == newType) continue;
        if (existing.getCategory() == newType.getCategory() && 
            existing.getFunctionType() == newType.getFunctionType()) {
            return false; // Aynı işlev var!
        }
    }
    return true;
}
```

---

### 2. Batarya Ateşleme Sistemi

**Mevcut Kod Yapısı**:
```java
// BatteryManager.java - fireBattery()
public void fireBattery(Player player, BatteryType batteryType, BatteryData data) {
    switch (batteryType.getCategory()) {
        case ATTACK:
            fireAttackBattery(player, batteryType, data);
            break;
        case CONSTRUCTION:
            fireConstructionBattery(player, batteryType, data);
            break;
        case SUPPORT:
            fireSupportBattery(player, batteryType, data);
            break;
    }
}
```

**Yeni Sistem İçin Gerekli Değişiklikler**:

1. **Yakıt Çarpanı Sistemi**: Yakıt tipine göre güç artışı
```java
// BatteryManager.java - Yakıt çarpanı
private double getFuelMultiplier(Material fuel, boolean isRedDiamond, boolean isDarkMatter) {
    if (isDarkMatter) return 10.0;
    if (isRedDiamond) return 5.0;
    if (fuel == Material.DIAMOND) return 2.5;
    if (fuel == Material.IRON_INGOT) return 1.0;
    return 1.0;
}
```

2. **Seviye Çarpanı Sistemi**: Seviyeye göre güç artışı
```java
// BatteryManager.java - Seviye çarpanı
private double getLevelMultiplier(int level) {
    switch (level) {
        case 1: return 1.0;
        case 2: return 1.5;
        case 3: return 2.5;
        case 4: return 4.0;
        case 5: return 10.0;
        default: return 1.0;
    }
}
```

---

### 3. Batarya Enum Yapısı

**Mevcut Yapı**:
```java
public enum BatteryType {
    ATTACK_FIREBALL_L1("Ateş Topu", BatteryCategory.ATTACK, 1, Material.MAGMA_BLOCK, null),
    // ...
}
```

**Yeni Sistem İçin Önerilen Yapı**:
```java
public enum BatteryType {
    // Saldırı Bataryaları
    ATTACK_LIGHTNING_STAFF_L1("Yıldırım Asası", BatteryCategory.ATTACK, 1, 
        Material.IRON_BLOCK, null, BatteryFunction.MANUAL_TARGET),
    ATTACK_DOUBLE_FIREBALL_L2("Çift Ateş Topu", BatteryCategory.ATTACK, 2, 
        Material.MAGMA_BLOCK, Material.NETHERRACK, BatteryFunction.DUAL_SHOT),
    ATTACK_METEOR_SHOWER_L3("Meteor Yağmuru", BatteryCategory.ATTACK, 3, 
        Material.OBSIDIAN, Material.MAGMA_BLOCK, BatteryFunction.AREA_RAIN),
    ATTACK_TESLA_TOWER_L4("Tesla Kulesi", BatteryCategory.ATTACK, 4, 
        Material.COPPER_BLOCK, Material.REDSTONE_BLOCK, BatteryFunction.AUTO_AREA),
    ATTACK_APOCALYPSE_REACTOR_L5("Kıyamet Reaktörü", BatteryCategory.ATTACK, 5, 
        Material.BEDROCK, Material.END_CRYSTAL, BatteryFunction.COMBO_DISASTER),
    
    // Oluşturma Bataryaları
    CONSTRUCTION_STONE_BRIDGE_L1("Taş Köprü", BatteryCategory.CONSTRUCTION, 1, 
        Material.STONE, null, BatteryFunction.BRIDGE),
    // ...
    
    // Destek Bataryaları
    SUPPORT_HEAL_L1("Can Yenileme", BatteryCategory.SUPPORT, 1, 
        Material.GOLD_BLOCK, null, BatteryFunction.HEAL),
    // ...
    
    private final BatteryFunction functionType; // YENİ: İşlev tipi
    
    BatteryType(String displayName, BatteryCategory category, int level, 
                Material baseBlock, Material sideBlock, BatteryFunction functionType) {
        // ...
        this.functionType = functionType;
    }
}

// YENİ: İşlev tipi enum'u
public enum BatteryFunction {
    // Saldırı İşlevleri
    MANUAL_TARGET,      // Manuel nişanlı (Yıldırım Asası)
    DUAL_SHOT,          // Çift atış (Çift Ateş Topu)
    AREA_RAIN,          // Alan yağmuru (Meteor Yağmuru)
    AUTO_AREA,          // Otomatik alan (Tesla Kulesi)
    COMBO_DISASTER,     // Kombinasyon felaket (Kıyamet Reaktörü)
    
    // Oluşturma İşlevleri
    BRIDGE,             // Köprü
    CAGE,               // Kafes
    CASTLE,             // Kale
    WALL,               // Duvar
    TOWER,              // Kule
    
    // Destek İşlevleri
    HEAL,               // Can verme
    SPEED,              // Hız artışı
    DAMAGE,             // Hasar artışı
    ARMOR,              // Zırh artışı
    REGENERATION        // Yenilenme
}
```

---

## 🔍 MEVCUT SİSTEMDEKİ SORUNLAR VE ÇÖZÜMLER

### Sorun 1: Blok Türü Çakışması

**Mevcut Durum**:
- `IRON_BLOCK` hem "Yıldırım" (saldırı) hem de "Zırh Artışı" (destek) bataryalarında kullanılıyor
- **Kural 1 İhlali**: Tek Kimlik kuralı

**Çözüm**:
- `IRON_BLOCK` → Sadece "Yıldırım Asası" için kullanılmalı
- "Zırh Artışı" için yeni blok türü kullanılmalı (örneğin: `IRON_BARS` veya `ANVIL`)

---

### Sorun 2: Aynı İşlevli Bataryalar

**Mevcut Durum**:
- "Yıldırım" (L1) ve "Gök Gürültüsü" (L4) → İkisi de baktığın yere yıldırım atıyor
- **Kural 5 İhlali**: Benzersizlik kuralı

**Çözüm**:
- "Yıldırım Asası" (L1) → Manuel nişanlı tek nokta yıldırım
- "Tesla Kulesi" (L4) → Otomatik alan etkili elektrik (farklı işlev)

---

### Sorun 3: Seviye Sistemi Eksikliği

**Mevcut Durum**:
- Seviye tespiti sadece blok sayısına göre yapılıyor
- Yan blok kontrolü eksik

**Çözüm**:
- Seviye tespiti: Blok sayısı + yan blok kontrolü + özel blok kontrolü
- Seviye 5 için: Altında Beacon + üstünde özel blok zorunlu

---

## 📊 ÖNERİLEN KOD YAPISI

### 1. BatteryType Enum Genişletilmesi

```java
public enum BatteryType {
    // Yeni alanlar
    private final BatteryFunction functionType;
    private final String uniqueId; // Benzersiz ID
    
    // Constructor
    BatteryType(String displayName, BatteryCategory category, int level, 
                Material baseBlock, Material sideBlock, BatteryFunction functionType) {
        this.functionType = functionType;
        this.uniqueId = category.name() + "_" + functionType.name() + "_L" + level;
    }
    
    // Benzersizlik kontrolü
    public boolean isUnique() {
        for (BatteryType other : values()) {
            if (other != this && 
                other.getCategory() == this.getCategory() && 
                other.getFunctionType() == this.getFunctionType()) {
                return false;
            }
        }
        return true;
    }
}
```

### 2. Batarya Tespit Sistemi Güncellemesi

```java
// BatteryListener.java
private boolean checkNewBatterySystem(Player player, Block centerBlock, int slot, PlayerInteractEvent event) {
    // 1. Temel blok kontrolü
    Material baseBlock = centerBlock.getType();
    
    // 2. Tüm olası bataryaları bul
    List<BatteryType> possibleBatteries = new ArrayList<>();
    for (BatteryType type : BatteryType.values()) {
        if (type.getBaseBlock() == baseBlock) {
            possibleBatteries.add(type);
        }
    }
    
    // 3. Tek Kimlik kontrolü (Kural 1)
    if (possibleBatteries.size() > 1) {
        player.sendMessage("§cHATA: Bu blok türü birden fazla batarya için kullanılıyor!");
        return false;
    }
    
    // 4. Seviye tespiti (Kural 2)
    int detectedLevel = detectBatteryLevel(centerBlock, baseBlock);
    
    // 5. Yan blok kontrolü (Kural 4 - Hibrit)
    Material sideBlock = checkSideBlock(centerBlock);
    
    // 6. Eşleşen bataryayı bul
    BatteryType matchedBattery = findMatchingBattery(baseBlock, sideBlock, detectedLevel);
    
    if (matchedBattery != null) {
        // Batarya yükleme
        loadBattery(player, centerBlock, slot, matchedBattery, event);
        return true;
    }
    
    return false;
}
```

### 3. Yakıt ve Seviye Çarpanı Sistemi

```java
// BatteryManager.java
public void fireBattery(Player player, BatteryType batteryType, BatteryData data) {
    // Yakıt çarpanı (Kural 3)
    double fuelMultiplier = getFuelMultiplier(data.getFuel(), data.isRedDiamond(), data.isDarkMatter());
    
    // Seviye çarpanı (Kural 2)
    double levelMultiplier = getLevelMultiplier(batteryType.getLevel());
    
    // Toplam çarpan
    double totalMultiplier = fuelMultiplier * levelMultiplier;
    
    // Batarya tipine göre ateşleme
    switch (batteryType.getCategory()) {
        case ATTACK:
            fireAttackBattery(player, batteryType, data, totalMultiplier);
            break;
        // ...
    }
}
```

---

## ✅ SONUÇ VE ÖNERİLER

### Yapılması Gerekenler

1. **Blok Türü Çakışmalarını Çöz**:
   - Her blok türü için sadece bir batarya belirle
   - Çakışan bataryalar için yeni blok türleri kullan

2. **Benzersizlik Kontrolü Ekle**:
   - `BatteryFunction` enum'u ekle
   - Her batarya için benzersiz işlev belirle
   - Aynı işlevli bataryaları kaldır veya değiştir

3. **Hibrit Yapı Desteği**:
   - Yan blok kontrolünü geliştir
   - Farklı kombinasyonları destekle

4. **Seviye Sistemi İyileştir**:
   - Seviye tespitini daha doğru yap
   - Seviye 5 için özel blok kontrolü ekle

5. **Yakıt Sistemi Genişlet**:
   - Yakıt çarpanlarını kodla
   - Her batarya tipi için özel yakıt etkileri ekle

---

## 📝 ÖRNEK BATARYA LİSTESİ (Özet)

### Saldırı Bataryaları
- **L1**: Yıldırım Asası (3x Demir Bloğu) - Manuel nişanlı
- **L2**: Çift Ateş Topu (5x Magma + Nethrack) - Çift atış
- **L3**: Meteor Yağmuru (7x Obsidyen + Magma) - Alan yağmuru
- **L4**: Tesla Kulesi (9x Bakır + Redstone) - Otomatik alan
- **L5**: Kıyamet Reaktörü (11x Bedrock + End Crystal) - Kombinasyon felaket

### Oluşturma Bataryaları
- **L1**: Taş Köprü (3x Taş) - Basit köprü
- **L2**: Obsidyen Kafes (5x Obsidyen + Demir) - Hapsetme
- **L3**: Netherite Köprü (7x Netherite + Netherite Ingot) - Güçlü köprü
- **L4**: Obsidyen Kale (9x Obsidyen + End Crystal) - Büyük kale
- **L5**: Netherite Köprü (Efsanevi) (11x Bedrock + Beacon) - Efsanevi köprü

### Destek Bataryaları
- **L1**: Can Yenileme (3x Altın) - Can verme
- **L2**: Hız Artışı (5x Zümrüt + Zümrüt) - Hız artışı
- **L3**: Hasar Artışı (7x Elmas + Elmas) - Hasar artışı
- **L4**: Zırh Artışı (9x Demir + Beacon) - Zırh artışı
- **L5**: Can Yenileme (Efsanevi) (11x Bedrock + Nether Star) - Tam can + absorption

---

**🎮 Bu rapor, Stratocraft Batarya Anayasası kurallarına göre yeni batarya sisteminin tasarımını ve kod mantığını içerir.**

