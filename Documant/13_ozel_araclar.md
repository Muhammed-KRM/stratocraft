# STRATOCRAFT - ÖZEL ARAÇLAR

## 🔧 Özel Araçlar Nedir?

Özel araçlar, savaşta ve keşitte kullanılan **fiziksel ekipmanlar**dır. 3 ana araç var: Kancalar ve Dürbün.

**KOD DOĞRULANDI**: SpecialItemManager.java'dan tüm mekanikler doğrulanmıştır.

---

## 📋 İÇİNDEKİLER

1. [Kanca Sistemi](#kanca-sistemi)
2. [Casusluk Dürbünü](#casusluk-dürbünü)

---

## 🪝 KANCA SİSTEMİ

### 1. Paslı Kanca (Rusty Hook)

**İstatistikler** (KOД DOĞRULANDI):
```java
// SpecialItemManager.java satır 22
private static final double RUSTY_HOOK_RANGE = 7.0;
```

**Menzil**: **7 blok** (maksimum)

**Cooldown**: **2 saniye**
```java
// SpecialItemManager.java satır 32
private static final long HOOK_COOLDOWN = 2000; // 2 saniye
```

---

#### Nasıl Çalışır?

**Mekanik** (KOD DOĞRULANDI):
```java
// SpecialItemManager.java satır 37-74
// Sadece zemine takıldığında çeker (FLY HACK ÖNLEMİ)
if (event.getState() != PlayerFishEvent.State.IN_GROUND) {
    return; // İptal
}
```

**Adımlar**:
```
1. Paslı Kanca eli ne al
2. Hedef bloğa fırlat (sağ tık)
3. Zemine takılırsa → OTOMATIK ÇEKİŞ
4. Cooldown başlar (2 saniye)
```

**Çekiş Gücü**:
```java
// SpecialItemManager.java satır 72
pullPlayer(player, hook.getLocation(), 0.4); // 0.4 = zayıf çekiş
```

---

#### Craft

**Tarif**:
```
 [I]        I = Iron Ingot
 [I]        S = String
 [S]

= Paslı Kanca
```

```java
// SpecialItemManager.java satır 256-263
// Basit tarif - Araştırma gerekmez
rustyHookRecipe.shape(" I ", " I ", " S ");
```

---

### 2. Titan Kancası (Titan Grapple)

**İstatistikler** (KOD DOĞRULANDI):
```java
// SpecialItemManager.java satır 23
private static final double TITAN_GRAPPLE_RANGE = 40.0;
```

**Menzil**: **40 blok** (çok uzun!)

**Cooldown**: **2 saniye** (aynı)

---

#### Nasıl Çalışır?

**Mekanik** (KOD DOĞRULANDI):
```java
// SpecialItemManager.java satır 76-96
// Slow Falling ver (düşme hasarı yok)
player.addPotionEffect(new PotionEffect(
    PotionEffectType.SLOW_FALLING, 60, 0)); // 3 saniye

// Güçlü çekiş
pullPlayer(player, hook.getLocation(), 0.8); // 0.8 = güçlü!

// Dayanıklılık azalt
damageItem(rod, 1);
```

**Özel Özellikler**:
- **Slow Falling** buff (3 saniye) → Düşme hasarı yok!
- **2x daha güçlü** çekiş
- **40 blok** menzil (Paslıdan 5.7x uzun)
- Her kullanımda **dayanıklılık azalır**

---

#### Craft (TARİF GEREKLİ!)

**Tarif**:
```
 [T]        T = Titanium Ingot (2 adet)
 [T]        M = Mithril String
[M][S]      S = Nether Star

= Titan Kancası
```

**Gereksinim**: **Tarif Kitabı** (Boss dropu)

---

### Kanca Kuralları (Anti-Fly Hack)

**FLY HACK ÖNLEMİ** (KOD DOĞRULANDI):
```java
// SpecialItemManager.java satır 38-41
// Sadece IN_GROUND durumunda çeker
if (event.getState() != PlayerFishEvent.State.IN_GROUND) {
    return; // Havada/suda çekmez
}
```

**Neden?**: Oyuncular havada spam yaparak uçamaz!

**Cooldown Sistemi**:
```java
// SpecialItemManager.java satır 46-52
if (hookCooldowns.containsKey(player.getUniqueId())) {
    long timeLeft = ...
    player.sendMessage("§cKanca soğumadı! Bekle: " + timeLeft + " sn");
    return;
}
```

---

## 🔭 CASUSLUK DÜRBİNÜ (Spyglass)

### Mekanik (KOD DOĞRULANDI)

**Nasıl Çalışır?**:
```java
// SpecialItemManager.java satır 24
private static final long SPY_DURATION = 3000; // 3 saniye
```

**3 Saniye Kuralı**:
```
1. Dürbünle hedef oyuncuya bak
2. 3 saniye kesintisiz bak
3. Bilgileri göster
```

**Takip Sistemi**:
```java
// SpecialItemManager.java satır 118-142
// Hedef değişirse → Zamanlayıcı sıfırlanır
Player previousTarget = spyTargets.get(player);
if (!previousTarget.equals(target)) {
    spyStartTimes.put(player, System.currentTimeMillis());
    return; // Yeniden başlat
}

// 3 saniye geçti mi?
long elapsed = System.currentTimeMillis() - startTime;
if (elapsed >= SPY_DURATION) {
    showPlayerInfo(player, target); // BİLGİLERİ GÖSTER
}
```

---

### Gösterilen Bilgiler (KOD DOĞRULANDI)

**Rapor Formatı**:
```java
// SpecialItemManager.java satır 148-182
§e§lCASUSLUK RAPORU: §f[Oyuncu_Adı]
§7Can: §c[X]/§c[MAX]
§7Zırh: §b[ARMOR_POINTS]§7/20
§7Envanter: §e[DOLU_SLOT]§7/§e36 §7(§e[%]§7)
```

**Can Hesaplama**:
```java
// satır 151-153
target.getHealth() // Şu anki can
target.getAttribute(GENERIC_MAX_HEALTH).getValue() // Max can
```

**Zırh Hesaplama**:
```java
// satır 156-166, 216-250
// Her parça zırh puanı:
LEATHER: 1 puan
GOLD/CHAINMAIL: 2 puan
IRON: 3 puan
DIAMOND: 4 puan
NETHERITE: 5 puan

Toplam: 0-20 puan
```

**Envanter Dold: uluğu**:
```java
// satır 170-180
// 36 slotun kaçı dolu?
[X]/36 (%Y dolu)
```

---

### Kullanım

**Adımlar**:
```
1. Dürbünü (Spyglass) eline al
2. Hedef oyuncuya nişan al
3. 3 saniye KESİNTİSİZ bak
   - Hareket edebilirsin
   - Ama nişanı kaçırma!
4. 3 saniye sonra → BİLGİLER GÖSTER
```

**Görsel**:
```
§6§ı═══════════════════════════
§e§lCASUSLUK RAPORU: §fJohn_Doe
§7Can: §c15.0§7/§c20.0
§7Zırh: §b16§7/20
§7Envanter: §e28§7/§e36 §7(§e78%§7)
§6§l═══════════════════════════
```

---

## 🎯 STRATEJİK KULLANIM

### Kanca Taktikleri

**Kaçış**:
```
1. Düşmanlar kovalıyor
2. Yüksek tepeye fırlat
3. ÇEK!
4. Kaç (Slow Falling sayesinde düşme hasarı yok - Titan için)
```

**Saldırı**:
```
1. Düşman duvarda saklanıyor
2. Duvarın üstüne fırlat
3. ÇEK!
4. Üstüne atla, saldır
```

**Keşif**:
```
1. Uzun mesafe atla (Titan Grapple)
2. Uçurum aş
3. Dağa tırman
```

---

### Dürbün Taktikleri

**Savaş Öncesi İstihbarat**:
```
1. Düşman baseini gözetle
2. Oyunculara dürbünle bak
3. Zırh durumunu öğren:
   - Full Diamond (16-20 puan) → Zor
   - Yarı zırh (8-12 puan) → Orta
   - Zırhsız (0-4 puan) → Kolay
4. Strateji belirle
```

**Ambush (Pusu)**:
```
1. Kervan yolunu gözetle
2. Oyuncuları tara:
   - Envanter dolu (80%+) → Değerli yük
   - Envanter boş (20%-) → Boş kervan, saldırma
3. Saldırıya karar ver
```

---

## ⚠️ ÖNEMLİ NOTLAR

### Kanca Limitleri

**Cooldown**: Her iki kanca da **2 saniye** cooldown
```
Spam yapılamaz!
2 saniye sonra tekrar kullanabilirsin.
```

**Fly Hack Önleme**: Sadece zemine takılınca çeker
```
Havada spam yaparak uçamazın!
```

**Dayanıklılık**: Titan Kancası her kullanımda aşınır
```
Dikkatli kullan, kırılabilir!
```

---

### Dürbün Limitleri

**3 Saniye Kesintisiz**:
```
Hareket etsen de olur AMA nişanı kaçırma!
Hedef değişirse → Zamanlayıcı sıfırlanır
```

**Sadece Oyuncular**:
```
Mob'lara bakamazsın
Sadece oyuncu bilgisi
```

---

## 🎯 HIZLI ARAÇ REHBERİ

### Paslı Kanca (Yeni Başlayanlar)

```
Craft: 2 Demir + 1 İp = Paslı Kanca
Menzil: 7 blok
Kullanım: Basit kaçış/tırmanma
Maliyet: Ucuz
```

### Titan Kancası (Pro)

```
Craft: 2 Titanyum + Mithril İp + Nether Star + TARİF
Menzil: 40 blok
Kullanım: Uzun mesafe, düşme hasarı yok
Maliyet: Çok pahalı
ÖZELLİK: Slow Falling (3 sn)
```

### Dürbün (Keşif)

```
Kullanım: 3 saniye hedefn e bak
Bilgi: Can, Zırh, Envanter
Strateji: Savaş öncesi istihbarat
Maliyet: Vanilla item (kolay)
```

---

**🎮 Araçlarla avantaj kazan, düşmanı tanı, savaşı kazan!**
