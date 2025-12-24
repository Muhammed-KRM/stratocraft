# KAOS EJDERİ YAPAY ZEKA AKIŞ ŞEMASI

## 📊 GENEL BAKIŞ

Kaos Ejderi (Chaos Dragon) 3 durumlu bir state machine kullanır:
1. **GO_CENTER** - Merkeze gitme durumu
2. **ATTACK_CLAN** - Klan kristallerine saldırma durumu  
3. **ATTACK_PLAYER** - Oyunculara saldırma durumu

---

## 🔄 DURUM MAKİNESİ AKIŞ ŞEMASI

```
┌─────────────────────────────────────────────────────────────┐
│                    BAŞLANGIÇ (Spawn)                       │
│              disasterState = GO_CENTER                      │
│              hasArrivedCenter = false                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │     GO_CENTER DURUMU         │
        │  (Merkeze Gitme)             │
        └──────────────┬───────────────┘
                       │
        ┌──────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│ Hedef yok mu? │              │ Hedef var     │
│ Merkez konumu │              │ Merkeze doğru │
│ al (Difficulty│              │ hareket et    │
│ Manager'dan)  │              │               │
└───────┬───────┘              └───────┬───────┘
        │                               │
        └───────────────┬───────────────┘
                        │
                        ▼
        ┌──────────────────────────────┐
        │ Merkeze ulaştı mı?           │
        │ (distance <= 50 blok)        │
        └──────────────┬───────────────┘
                       │
        ┌──────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│ HAYIR         │              │ EVET          │
│ Merkeze doğru │              │ hasArrivedCenter│
│ hareket et    │              │ = true         │
│               │              │ updateStateAfter│
│               │              │ CenterReached()│
└───────────────┘              └───────┬───────┘
                                       │
                        ┌──────────────┴──────────────┐
                        │                              │
                        ▼                              ▼
        ┌──────────────────────────────┐  ┌──────────────────────────────┐
        │ updateStateAfterCenterReached │  │ Merkeze 1500 blok yakında    │
        │                               │  │ klan var mı?                 │
        └───────────────┬───────────────┘  └──────────────┬───────────────┘
                        │                                 │
        ┌───────────────┴───────────────┐                │
        │                               │                │
        ▼                               ▼                ▼
┌───────────────┐              ┌───────────────┐  ┌───────────────┐
│ Merkeze 1500  │              │ Merkeze 1500  │  │ EVET           │
│ blok yakında  │              │ blok yakında   │  │ ATTACK_CLAN    │
│ klan var mı?  │              │ klan yok      │  │ durumuna geç   │
└───────┬───────┘              └───────┬───────┘  │ (en yakın klan)│
        │                               │          └────────────────┘
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│ EVET          │              │ HAYIR         │
│ ATTACK_CLAN   │              │ ATTACK_PLAYER  │
│ durumuna geç  │              │ durumuna geç   │
│ (en yakın     │              │ (en yakın      │
│ klan)         │              │ oyuncu)        │
└───────────────┘              └────────────────┘
```

---

## 🏛️ ATTACK_CLAN DURUMU AKIŞ ŞEMASI

```
┌─────────────────────────────────────────────────────────────┐
│              ATTACK_CLAN DURUMU                            │
│         (Klan Kristallerine Saldırma)                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │ Hedef kristal var mı?        │
        │ Ve kristal yok edilmedi mi?  │
        └──────────────┬───────────────┘
                       │
        ┌──────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│ HAYIR         │              │ EVET          │
│ Yeni hedef    │              │ Kristale doğru │
│ bul           │              │ hareket et    │
└───────┬───────┘              └───────┬───────┘
        │                               │
        ▼                               ▼
┌───────────────┐              ┌──────────────────────────────┐
│ Merkeze 1500  │              │ Kristale yakın mı?           │
│ blok yakında  │              │ (distance <= 5 blok)         │
│ klan var mı?  │              └──────────────┬───────────────┘
│ (merkeze göre)│                            │
└───────┬───────┘                            │
        │                      ┌──────────────┴───────────────┐
        ▼                      │                              │
┌───────────────┐              ▼                              ▼
│ EVET           │      ┌───────────────┐          ┌───────────────┐
│ En yakın klan │      │ HAYIR         │          │ EVET          │
│ kristalini    │      │ Kristale doğru │          │ Kristale saldır│
│ hedef al      │      │ hareket et     │          │ attackCrystal()│
│               │      │               │          └───────┬───────┘
└───────┬───────┘      └───────────────┘                  │
        │                                                   │
        ▼                                                   ▼
┌───────────────┐                                  ┌──────────────────────────────┐
│ HAYIR         │                                  │ Kristal yok edildi mi?       │
│ ATTACK_PLAYER │                                  └──────────────┬───────────────┘
│ durumuna geç  │                                                 │
└───────────────┘                                                 │
                                                                  ▼
                                                         ┌──────────────────────────────┐
                                                         │ EVET                        │
                                                         │ findNewTargetAfterCrystal   │
                                                         │ Destroyed()                 │
                                                         └──────────────┬───────────────┘
                                                                        │
                                                         ┌──────────────┴───────────────┐
                                                         │                              │
                                                         ▼                              ▼
                                                ┌───────────────┐          ┌───────────────┐
                                                │ 1. 1000 blok  │          │ 2. Oyuncu var │
                                                │ yakında klan  │          │ mı?           │
                                                │ var mı?       │          └───────┬───────┘
                                                └───────┬───────┘                  │
                                                        │                          ▼
                                        ┌───────────────┴───────────────┐  ┌───────────────┐
                                        │                              │  │ EVET           │
                                        ▼                              ▼  │ ATTACK_PLAYER  │
                                ┌───────────────┐          ┌───────────────┐ durumuna geç   │
                                │ EVET          │          │ HAYIR         │               │
                                │ ATTACK_CLAN   │          │ 3. En yakın   │               │
                                │ durumuna devam│          │ klana yönel   │               │
                                │ (yeni kristal)│          │ (uzakta olsa  │               │
                                └───────────────┘          │ bile)         │               │
                                                            │ ATTACK_CLAN   │               │
                                                            └───────────────┘               │
                                                                                            │
                                                                                            ▼
                                                                                   ┌───────────────┐
                                                                                   │ HAYIR          │
                                                                                   │ En yakın klana │
                                                                                   │ yönel (uzakta  │
                                                                                   │ olsa bile)     │
                                                                                   │ ATTACK_CLAN    │
                                                                                   └────────────────┘
```

---

## 👤 ATTACK_PLAYER DURUMU AKIŞ ŞEMASI

```
┌─────────────────────────────────────────────────────────────┐
│              ATTACK_PLAYER DURUMU                            │
│              (Oyunculara Saldırma)                            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │ Hedef oyuncu var mı?         │
        │ Ve oyuncu online/ölü değil?  │
        └──────────────┬───────────────┘
                       │
        ┌──────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌───────────────┐              ┌───────────────┐
│ HAYIR         │              │ EVET          │
│ En yakın      │              │ Oyuncuya doğru│
│ oyuncuyu bul  │              │ hareket et    │
└───────┬───────┘              └───────────────┘
        │
        ▼
┌───────────────┐
│ Oyuncu       │
│ bulundu mu?  │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ EVET          │
│ Hedef oyuncu  │
│ ayarla       │
└───────┬───────┘
        │
        ▼
┌──────────────────────────────┐
│ 20 saniyede bir klan kontrolü│
│ (lastClanCheckTime)          │
└──────────────┬───────────────┘
               │
        ┌──────┴──────┐
        │             │
        ▼             ▼
┌───────────────┐  ┌───────────────┐
│ 20 saniye      │  │ Henüz 20 saniye│
│ geçti mi?      │  │ geçmedi       │
└───────┬───────┘  └───────┬───────┘
        │                   │
        ▼                   │
┌───────────────┐           │
│ 1000 blok     │           │
│ yakında klan  │           │
│ var mı?       │           │
│ (current'a    │           │
│ göre)         │           │
└───────┬───────┘           │
        │                   │
        ▼                   │
┌───────────────┐           │
│ EVET          │           │
│ ATTACK_CLAN   │           │
│ durumuna geç  │           │
│ (yeni kristal)│           │
└───────────────┘           │
                            │
                            ▼
                ┌──────────────────────────────┐
                │ Oyuncuya doğru hareket et    │
                │ (targetPlayer != null)        │
                └──────────────┬───────────────┘
                               │
                ┌──────────────┴───────────────┐
                │                               │
                ▼                               ▼
        ┌───────────────┐              ┌───────────────┐
        │ Oyuncu       │              │ Oyuncu       │
        │ bulunamadı   │              │ bulundu       │
        │ En yakın     │              │ Oyuncuya      │
        │ klana yönel  │              │ hareket et    │
        │ ATTACK_CLAN  │              │               │
        └───────────────┘              └───────────────┘
```

---

## 🔍 ÖNEMLİ METODLAR VE MANTIK

### 1. `updateStateAfterCenterReached()` - Merkeze Ulaştıktan Sonra
```java
// Merkez konumunu al (current değil, gerçek merkez)
Location centerLocation = getCenterLocation(plugin, current);

// Merkeze 1500 blok yakında klan var mı? (merkeze göre kontrol)
List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);

if (!nearbyCrystals.isEmpty()) {
    // Klan bulundu → ATTACK_CLAN
    disaster.setDisasterState(DisasterState.ATTACK_CLAN);
    Location targetCrystal = nearbyCrystals.get(0); // En yakın klan
    disaster.setTargetCrystal(targetCrystal);
} else {
    // Yakında klan yok → ATTACK_PLAYER
    disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
    Player nearestPlayer = findNearestPlayer(current);
    disaster.setTargetPlayer(nearestPlayer);
}
```

### 2. `handleAttackClan()` - Klan Saldırısı
```java
// Hedef kristal yoksa veya kırıldıysa
if (targetCrystal == null || isCrystalDestroyed(targetCrystal)) {
    // Merkeze 1500 blok yakında klan var mı? (merkeze göre kontrol)
    List<Location> nearbyCrystals = findCrystalsInRadius(plugin, centerLocation, 1500.0);
    
    if (!nearbyCrystals.isEmpty()) {
        // Klan bulundu → ATTACK_CLAN devam
        targetCrystal = nearbyCrystals.get(0);
    } else {
        // Yakında klan yok → ATTACK_PLAYER
        disaster.setDisasterState(DisasterState.ATTACK_PLAYER);
    }
}
```

### 3. `handleAttackPlayer()` - Oyuncu Saldırısı
```java
// 20 saniyede bir klan kontrolü yap
if (now - disaster.getLastClanCheckTime() >= 20000) {
    // 1000 blok yakında klan var mı? (current'a göre kontrol)
    List<Location> nearbyCrystals = findCrystalsInRadius(plugin, current, 1000.0);
    
    if (!nearbyCrystals.isEmpty()) {
        // Klan bulundu → ATTACK_CLAN
        disaster.setDisasterState(DisasterState.ATTACK_CLAN);
    }
}
```

### 4. `findNewTargetAfterCrystalDestroyed()` - Kristal Yok Edildikten Sonra
```java
// 1. 1000 blok yakında klan var mı?
List<Location> nearbyCrystals = findCrystalsInRadius(plugin, current, 1000.0);
if (!nearbyCrystals.isEmpty()) {
    // ATTACK_CLAN devam
    return;
}

// 2. Oyuncu var mı?
Player nearestPlayer = findNearestPlayer(current);
if (nearestPlayer != null) {
    // ATTACK_PLAYER
    return;
}

// 3. En yakın klana yönel (uzakta olsa bile)
Location nearestCrystal = findNearestCrystal(plugin, current);
if (nearestCrystal != null) {
    // ATTACK_CLAN
}
```

---

## ✅ YAPILAN DÜZELTMELER

### Düzeltme 1: Tutarlı Klan Kontrolü ✅
**Önceki Sorun:**
- `handleAttackPlayer()` → **Current** konumuna **1000 blok** yakında klan arıyordu
- `handleAttackClan()` → Merkeze **1500 blok** yakında klan arıyordu
- `findNewTargetAfterCrystalDestroyed()` → **Current** konumuna **1000 blok** yakında klan arıyordu

**Düzeltme:**
- Merkeze ulaştıysa (`hasArrivedCenter() == true`): **Merkeze göre 1500 blok** kontrol
- Merkeze ulaşmadıysa: **Current'a göre 1000 blok** kontrol
- Tüm metodlarda tutarlı hale getirildi

### Düzeltme 2: Yarıçap Standardizasyonu ✅
**Yeni Mantık:**
```java
if (disaster.hasArrivedCenter()) {
    // Merkeze ulaştıysa merkeze göre kontrol (1500 blok)
    searchLocation = getCenterLocation(plugin, current);
    searchRadius = 1500.0;
} else {
    // Merkeze ulaşmadıysa current'a göre kontrol (1000 blok)
    searchLocation = current;
    searchRadius = 1000.0;
}
```

### Düzeltme 3: Hedef Seçim Mantığı ✅
**Yeni Öncelik:**
1. Merkeze ulaştıysa: Merkeze **1500 blok** yakında klan (merkeze göre) → ATTACK_CLAN
2. Merkeze ulaşmadıysa: Current'a **1000 blok** yakında klan (current'a göre) → ATTACK_CLAN
3. Klan yoksa: Oyuncu → ATTACK_PLAYER
4. Oyuncu yoksa: En yakın klan (uzakta olsa bile) → ATTACK_CLAN

**Düzeltilen Metodlar:**
- ✅ `handleAttackPlayer()` - Merkeze ulaştıysa merkeze göre kontrol
- ✅ `handleAttackClan()` - Merkeze ulaştıysa merkeze göre kontrol
- ✅ `findNewTargetAfterCrystalDestroyed()` - Merkeze ulaştıysa merkeze göre kontrol

---

## 📝 ÖZET

**Kaos Ejderi AI'sı (Düzeltilmiş):**
1. ✅ Merkeze gidiyor (GO_CENTER)
2. ✅ Merkeze ulaştığında klan kontrolü yapıyor (merkeze 1500 blok)
3. ✅ Klan varsa klanlara saldırıyor (ATTACK_CLAN)
4. ✅ Klan yoksa oyunculara saldırıyor (ATTACK_PLAYER)
5. ✅ Oyuncu saldırısı sırasında 20 saniyede bir klan kontrolü yapıyor:
   - Merkeze ulaştıysa: Merkeze 1500 blok yakında klan arıyor
   - Merkeze ulaşmadıysa: Current'a 1000 blok yakında klan arıyor
6. ✅ Kristal yok edildikten sonra yeni hedef buluyor:
   - Merkeze ulaştıysa: Merkeze 1500 blok yakında klan arıyor
   - Merkeze ulaşmadıysa: Current'a 1000 blok yakında klan arıyor

**Düzeltme:** Tüm klan kontrolleri artık merkeze ulaşma durumuna göre tutarlı şekilde çalışıyor. Merkeze ulaştıysa merkeze göre, ulaşmadıysa current'a göre kontrol yapılıyor.

