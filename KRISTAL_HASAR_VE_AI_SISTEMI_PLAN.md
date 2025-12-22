# Klan Kristali Hasar ve AI Sistemi - Detaylı Plan

## 1. KRISTAL HASAR SİSTEMİ

### 1.1. Problem
- EnderCrystal normalde bir vuruşta patlar (bug)
- Klan üyeleri kristale vurduğunda patlamamalı, sadece canı gitmeli
- Tüm canlılar (bosslar, moblar) kristale hasar verebilmeli

### 1.2. Çözüm: EntityDamageEvent Listener

**Mantık:**
1. **EnderCrystal Hasar Engelleme**: 
   - EntityDamageEvent'te EnderCrystal'a hasar geldiğinde event'i iptal et
   - Hasar kaynağını kontrol et (Player, LivingEntity, vb.)
   - Can sistemine yönlendir

2. **Klan Üyesi Kontrolü**:
   - Oyuncu klan üyesi ise: Sadece can sistemi çalışmalı (patlamamalı)
   - Normal oyuncu ise: Belki koruma olabilir veya hasar verebilir (config'den ayarlanabilir)

3. **Canlı Hasar Sistemi**:
   - Bosslar ve moblar kristale hasar verebilmeli
   - Hasar miktarı entity tipine göre değişebilir
   - Config'den ayarlanabilir hasar çarpanları

### 1.3. Hasar Hesaplama
```
Base Damage = Entity Attack Damage (veya config'den)
Final Damage = Base Damage * (1.0 - Crystal Armor Reduction)
Shield Check: Eğer shield varsa, hasar engellenir
Health Reduction: crystalCurrentHealth -= Final Damage
```

## 2. AI SİSTEMİ - ÖNCELİKLİ HEDEF SEÇİMİ

### 2.1. Hedef Öncelik Sistemi

**Öncelik Sırası:**
1. **Klan Kristali** (50 blok içinde): En yüksek öncelik
2. **Oyuncu** (30 blok içinde): İkinci öncelik
3. **Rastgele Hedef**: Diğer durumlar

**Mantık:**
```
IF (50 blok içinde klan kristali varsa):
    HEDEF = Klan Kristali
    AI MOD = CRYSTAL_ATTACK
ELSE IF (30 blok içinde oyuncu varsa):
    HEDEF = Oyuncu
    AI MOD = PLAYER_ATTACK
ELSE:
    HEDEF = Rastgele (mevcut sistem)
    AI MOD = NORMAL
```

### 2.2. AI Güncelleme Sistemi

**Her 2 saniyede bir:**
- Yakındaki kristalleri tara (50 blok)
- Yakındaki oyuncuları tara (30 blok)
- Hedef önceliğini güncelle
- Entity'nin hedefini ayarla

### 2.3. Entity Tipleri

**Özel Canlılar (Öncelikli):**
- Bosslar (BossManager'dan)
- Felaket Entity'leri (DisasterManager'dan)
- Mini Felaket Mobları (yeni eklenecek)

**Normal Moblar:**
- Normal Minecraft mobları (zombi, iskelet, vb.)
- Bunlar da kristale saldırabilir ama öncelik düşük

## 3. MİNİ FELAKET SİSTEMİ

### 3.1. Mini Felaket Tipleri

**1. KRISTAL AVCI DALGASI (Crystal Hunter Wave)**
- **Güç Seviyesi**: Orta (Seviye 2)
- **Spawn**: 20-30 adet güçlendirilmiş mob
- **Mob Tipleri**: Zombi, İskelet, Creeper karışımı
- **Özellikler**:
  - %50 can artışı
  - %30 hasar artışı
  - Kristale %200 hasar bonusu
  - Hız artışı (Speed I)

**2. KRISTAL YOK EDİCİ ORDUSU (Crystal Destroyer Army)**
- **Güç Seviyesi**: Yüksek (Seviye 3)
- **Spawn**: 15-20 adet çok güçlü mob
- **Mob Tipleri**: Zombie, Skeleton, Spider, Creeper
- **Özellikler**:
  - %100 can artışı
  - %50 hasar artışı
  - Kristale %300 hasar bonusu
  - Hız artışı (Speed II)
  - Zırh (Resistance I)

**3. KRISTAL FELAKETİ (Crystal Catastrophe)**
- **Güç Seviyesi**: Çok Yüksek (Seviye 4)
- **Spawn**: 10-15 adet elite mob
- **Mob Tipleri**: Elite Zombie, Elite Skeleton, Elite Creeper
- **Özellikler**:
  - %150 can artışı
  - %75 hasar artışı
  - Kristale %400 hasar bonusu
  - Hız artışı (Speed III)
  - Zırh (Resistance II)
  - Rejenerasyon (Regeneration I)

### 3.2. Mini Felaket Spawn Sistemi

**Spawn Mantığı:**
1. Rastgele bir klan kristali seç
2. Kristal etrafında 20-50 blok mesafede spawn noktası belirle
3. Mobları spawn et
4. AI sistemine kaydet (kristal hedefi olarak)

**Spawn Sıklığı:**
- Config'den ayarlanabilir (varsayılan: 30 dakikada bir)
- Her mini felaket farklı sıklıkta olabilir

### 3.3. Mob Güçlendirme Sistemi

**Güçlendirme Metodları:**
1. **Attribute Modifier**: Can ve hasar artışı
2. **Potion Effect**: Hız, zırh, rejenerasyon
3. **Metadata**: Özel etiket (kristal avcısı)
4. **Custom AI**: Kristale yönelme davranışı

## 4. SİSTEM ENTEGRASYONU

### 4.1. Event Listener Sistemi

**CrystalDamageListener:**
- EntityDamageEvent dinle
- EnderCrystal kontrolü
- Hasar kaynağı kontrolü
- Can sistemine yönlendirme

### 4.2. AI Manager Sistemi

**CrystalTargetingAI:**
- Entity'lerin hedef seçimini yönet
- Öncelik sistemini uygula
- Periyodik güncelleme

### 4.3. Mini Felaket Manager

**MiniDisasterManager:**
- Mini felaket spawn'larını yönet
- Mob güçlendirmelerini uygula
- AI sistemine entegre et

## 5. CONFIG AYARLARI

```yaml
crystal-damage-system:
  # Oyuncu hasarı
  player-damage-enabled: true
  player-damage-multiplier: 1.0
  clan-member-damage-enabled: true
  clan-member-damage-multiplier: 0.5  # Klan üyeleri daha az hasar verir
  
  # Entity hasarı
  entity-damage-enabled: true
  boss-damage-multiplier: 2.0
  mob-damage-multiplier: 0.5
  mini-disaster-damage-multiplier: 1.5
  
  # AI sistemi
  ai-update-interval: 40  # 2 saniye (40 tick)
  crystal-priority-distance: 50  # 50 blok
  player-priority-distance: 30  # 30 blok

mini-disasters:
  crystal-hunter-wave:
    enabled: true
    spawn-interval: 1800  # 30 dakika (saniye)
    mob-count: 25
    health-bonus: 0.5  # %50
    damage-bonus: 0.3  # %30
    crystal-damage-bonus: 2.0  # %200
    speed-level: 1
    
  crystal-destroyer-army:
    enabled: true
    spawn-interval: 2400  # 40 dakika
    mob-count: 18
    health-bonus: 1.0  # %100
    damage-bonus: 0.5  # %50
    crystal-damage-bonus: 3.0  # %300
    speed-level: 2
    resistance-level: 1
    
  crystal-catastrophe:
    enabled: true
    spawn-interval: 3600  # 60 dakika
    mob-count: 12
    health-bonus: 1.5  # %150
    damage-bonus: 0.75  # %75
    crystal-damage-bonus: 4.0  # %400
    speed-level: 3
    resistance-level: 2
    regeneration-level: 1
```

## 6. UYGULAMA SIRASI

1. **CrystalDamageListener** oluştur (EntityDamageEvent)
2. **CrystalTargetingAI** oluştur (AI sistemi)
3. **MiniDisasterManager** oluştur (mini felaketler)
4. **Config** güncelle
5. **Main.java** entegrasyonu
6. **Test** ve optimizasyon

