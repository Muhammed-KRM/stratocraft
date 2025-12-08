# 🌋 FELAKET SİSTEMİ EKLENECEK ÖZELLİKLER - DETAYLI DÖKÜMAN

## 📋 İÇİNDEKİLER
1. [Dinamik Zorluk Sistemi (Öncelikli)](#1-dinamik-zorluk-sistemi-öncelikli)
2. [Faz Sistemi](#2-faz-sistemi)
3. [Özel Yetenekler ve Hareketler](#3-özel-yetenekler-ve-hareketler)
4. [AI İyileştirmeleri](#4-ai-iyileştirmeleri)
5. [Çevresel Etkiler ve Korkutma](#5-çevresel-etkiler-ve-korkutma)
6. [Görsel ve İşitsel Efektler](#6-görsel-ve-işitsel-efektler)
7. [İşbirlikçi Mekanikler](#7-işbirlikçi-mekanikler)
8. [Ödül ve İlerleme Sistemi](#8-ödül-ve-ilerleme-sistemi)
9. [Özel Senaryolar](#9-özel-senaryolar)

---

## 1. DİNAMİK ZORLUK SİSTEMİ (ÖNCELİKLİ)

### 🎯 Genel Bakış
Sunucudaki tüm oyuncuların gücüne göre felaketlerin güçlenmesi. Bu sistem, sunucudaki herkesin felaketi kesmek için çabaladığı durumlarda felaketlerin daha güçlü olmasını sağlar.

### 📊 Güç Hesaplama Sistemi

#### **1.1 Oyuncu Güç Puanı (Player Power Score)**

Her oyuncunun gücü aşağıdaki faktörlere göre hesaplanır:

##### **A. Klan Yapıları (Structure Power)**
```
Yapı Gücü = Σ (Yapı Seviyesi × Yapı Tipi Çarpanı)

Yapı Tipi Çarpanları:
- Batarya (Battery): 2.0x
- Araştırma Merkezi (Research Center): 1.5x
- Üretim Yapısı (Production): 1.2x
- Savunma Yapısı (Defense): 1.8x
- Diğer Yapılar: 1.0x

Örnek:
- 3x Batarya Lv3 = 3 × 3 × 2.0 = 18 puan
- 2x Araştırma Lv2 = 2 × 2 × 1.5 = 6 puan
- Toplam Yapı Gücü = 24 puan
```

##### **B. Eşya Gücü (Item Power)**
```
Eşya Gücü = Silah Gücü + Zırh Gücü + Özel Eşya Gücü

Silah Gücü:
- Seviye 1 Silah: 5 puan
- Seviye 2 Silah: 10 puan
- Seviye 3 Silah: 20 puan
- Seviye 4 Silah: 40 puan
- Seviye 5 Silah: 80 puan

Zırh Gücü (Her parça):
- Seviye 1 Zırh: 3 puan/parça
- Seviye 2 Zırh: 6 puan/parça
- Seviye 3 Zırh: 12 puan/parça
- Seviye 4 Zırh: 24 puan/parça
- Seviye 5 Zırh: 48 puan/parça
- Tam Set Bonus: +50% (4 parça takılıysa)

Özel Eşya Gücü:
- Tier 1 Özel Eşya: 10 puan
- Tier 2 Özel Eşya: 25 puan
- Tier 3 Özel Eşya: 50 puan
- Tier 4 Özel Eşya: 100 puan

Örnek:
- Lv5 Kılıç: 80 puan
- Lv4 Tam Zırh Seti: (24 × 4) × 1.5 = 144 puan
- Tier 3 Özel Eşya: 50 puan
- Toplam Eşya Gücü = 274 puan
```

##### **C. Buff ve Güçlendirmeler (Buff Power)**
```
Buff Gücü = Σ (Buff Seviyesi × Buff Tipi Çarpanı)

Buff Tipi Çarpanları:
- Hasar Artırımı (Damage Boost): 2.0x
- Savunma Artırımı (Defense Boost): 1.5x
- Hız Artırımı (Speed Boost): 0.5x
- İyileştirme (Regeneration): 1.0x
- Diğer Bufflar: 0.8x

Örnek:
- %50 Hasar Artırımı: 50 × 2.0 = 100 puan
- %30 Savunma Artırımı: 30 × 1.5 = 45 puan
- Toplam Buff Gücü = 145 puan
```

##### **D. Eğitim Seviyesi (Training Level)**
```
Eğitim Gücü = Eğitim Seviyesi × 5

Örnek:
- Eğitim Seviyesi 10: 10 × 5 = 50 puan
```

##### **E. Klan Seviyesi (Clan Tech Level)**
```
Klan Teknoloji Gücü = Klan Tech Level × 10

Örnek:
- Tech Level 5: 5 × 10 = 50 puan
```

#### **1.2 Oyuncu Toplam Güç Puanı**
```
Oyuncu Güç Puanı = 
    Yapı Gücü × 0.3 +      // %30 ağırlık
    Eşya Gücü × 0.4 +      // %40 ağırlık
    Buff Gücü × 0.15 +     // %15 ağırlık
    Eğitim Gücü × 0.1 +    // %10 ağırlık
    Klan Tech Gücü × 0.05  // %5 ağırlık
```

**Örnek Hesaplama:**
```
Bir oyuncu için:
- Yapı Gücü: 24 puan
- Eşya Gücü: 274 puan
- Buff Gücü: 145 puan
- Eğitim Gücü: 50 puan
- Klan Tech Gücü: 50 puan

Toplam = (24 × 0.3) + (274 × 0.4) + (145 × 0.15) + (50 × 0.1) + (50 × 0.05)
       = 7.2 + 109.6 + 21.75 + 5 + 2.5
       = 146.05 puan
```

#### **1.3 Sunucu Toplam Güç Puanı**
```
Sunucu Güç Puanı = 
    (Tüm Oyuncuların Güç Puanları Toplamı) / (Aktif Oyuncu Sayısı) × Aktif Oyuncu Sayısı Çarpanı

Aktif Oyuncu Sayısı Çarpanları:
- 1-3 oyuncu: 0.8x (Daha az zorluk)
- 4-6 oyuncu: 1.0x (Normal)
- 7-10 oyuncu: 1.3x (Daha zor)
- 11-15 oyuncu: 1.6x (Çok zor)
- 16+ oyuncu: 2.0x (Maksimum zorluk)
```

**Örnek Hesaplama:**
```
Sunucuda 8 oyuncu var:
- Oyuncu 1: 146 puan
- Oyuncu 2: 120 puan
- Oyuncu 3: 180 puan
- Oyuncu 4: 95 puan
- Oyuncu 5: 200 puan
- Oyuncu 6: 150 puan
- Oyuncu 7: 110 puan
- Oyuncu 8: 130 puan

Toplam = 1131 puan
Ortalama = 1131 / 8 = 141.375 puan
Aktif Oyuncu Çarpanı (7-10): 1.3x

Sunucu Güç Puanı = 141.375 × 1.3 = 183.79 puan
```

#### **1.4 Felaket Güç Çarpanı Hesaplama**
```
Felaket Güç Çarpanı = 
    Base Multiplier × 
    (1 + (Sunucu Güç Puanı / 100) × Power Scaling Factor)

Power Scaling Factor (Config'den):
- Minimum: 0.5 (Yavaş artış)
- Normal: 1.0 (Orta artış)
- Maksimum: 2.0 (Hızlı artış)

Örnek:
- Base Multiplier: 1.0
- Sunucu Güç Puanı: 183.79
- Power Scaling Factor: 1.0

Felaket Güç Çarpanı = 1.0 × (1 + (183.79 / 100) × 1.0)
                    = 1.0 × (1 + 1.8379)
                    = 2.8379x

Bu çarpan felaketin can ve hasarına uygulanır:
- Can: Base Can × 2.8379
- Hasar: Base Hasar × 2.8379
```

### 💻 Teknik İmplementasyon

#### **1.5 Yeni Sınıflar**

##### **PlayerPowerCalculator.java**
```java
package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

/**
 * Oyuncu güç puanı hesaplama sistemi
 */
public class PlayerPowerCalculator {
    
    // Ağırlık çarpanları (config'den okunabilir)
    private static final double STRUCTURE_WEIGHT = 0.3;
    private static final double ITEM_WEIGHT = 0.4;
    private static final double BUFF_WEIGHT = 0.15;
    private static final double TRAINING_WEIGHT = 0.1;
    private static final double CLAN_TECH_WEIGHT = 0.05;
    
    /**
     * Oyuncunun toplam güç puanını hesapla
     */
    public static double calculatePlayerPower(Player player, ClanManager clanManager, TrainingManager trainingManager) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        
        double structurePower = calculateStructurePower(clan);
        double itemPower = calculateItemPower(player);
        double buffPower = calculateBuffPower(player);
        double trainingPower = calculateTrainingPower(player, trainingManager);
        double clanTechPower = calculateClanTechPower(clan);
        
        return (structurePower * STRUCTURE_WEIGHT) +
               (itemPower * ITEM_WEIGHT) +
               (buffPower * BUFF_WEIGHT) +
               (trainingPower * TRAINING_WEIGHT) +
               (clanTechPower * CLAN_TECH_WEIGHT);
    }
    
    /**
     * Klan yapıları gücü
     */
    private static double calculateStructurePower(Clan clan) {
        if (clan == null) return 0;
        
        double totalPower = 0;
        for (Structure structure : clan.getStructures()) {
            double multiplier = getStructureTypeMultiplier(structure.getType());
            totalPower += structure.getLevel() * multiplier;
        }
        return totalPower;
    }
    
    /**
     * Yapı tipi çarpanı
     */
    private static double getStructureTypeMultiplier(Structure.Type type) {
        switch (type) {
            case BATTERY: return 2.0;
            case RESEARCH_CENTER: return 1.5;
            case PRODUCTION: return 1.2;
            case DEFENSE: return 1.8;
            default: return 1.0;
        }
    }
    
    /**
     * Eşya gücü
     */
    private static double calculateItemPower(Player player) {
        double weaponPower = calculateWeaponPower(player);
        double armorPower = calculateArmorPower(player);
        double specialItemPower = calculateSpecialItemPower(player);
        
        return weaponPower + armorPower + specialItemPower;
    }
    
    /**
     * Silah gücü
     */
    private static double calculateWeaponPower(Player player) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return 0;
        
        int level = ItemManager.getWeaponLevel(weapon);
        if (level == 0) return 0;
        
        // Seviye bazlı güç: 5, 10, 20, 40, 80
        return Math.pow(2, level - 1) * 5;
    }
    
    /**
     * Zırh gücü
     */
    private static double calculateArmorPower(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        double totalPower = 0;
        int equippedPieces = 0;
        
        for (ItemStack piece : armor) {
            if (piece != null) {
                int level = ItemManager.getArmorLevel(piece);
                if (level > 0) {
                    // Seviye bazlı güç: 3, 6, 12, 24, 48
                    totalPower += Math.pow(2, level - 1) * 3;
                    equippedPieces++;
                }
            }
        }
        
        // Tam set bonusu (4 parça)
        if (equippedPieces == 4) {
            totalPower *= 1.5;
        }
        
        return totalPower;
    }
    
    /**
     * Özel eşya gücü
     */
    private static double calculateSpecialItemPower(Player player) {
        // Tüm envanteri kontrol et
        double totalPower = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && SpecialItemManager.isSpecialItem(item)) {
                int tier = SpecialItemManager.getTier(item);
                // Tier bazlı güç: 10, 25, 50, 100
                totalPower += tier * tier * 10;
            }
        }
        return totalPower;
    }
    
    /**
     * Buff gücü
     */
    private static double calculateBuffPower(Player player) {
        double totalPower = 0;
        
        for (PotionEffect effect : player.getActivePotionEffects()) {
            double multiplier = getBuffTypeMultiplier(effect.getType());
            int amplifier = effect.getAmplifier() + 1; // 0-based to 1-based
            totalPower += amplifier * 10 * multiplier;
        }
        
        // Özel bufflar (BuffManager'dan)
        // TODO: BuffManager entegrasyonu
        
        return totalPower;
    }
    
    /**
     * Buff tipi çarpanı
     */
    private static double getBuffTypeMultiplier(org.bukkit.potion.PotionEffectType type) {
        // Hasar artırımı
        if (type == org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE) return 2.0;
        // Savunma artırımı
        if (type == org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE) return 1.5;
        // Hız
        if (type == org.bukkit.potion.PotionEffectType.SPEED) return 0.5;
        // İyileştirme
        if (type == org.bukkit.potion.PotionEffectType.REGENERATION) return 1.0;
        // Diğer
        return 0.8;
    }
    
    /**
     * Eğitim gücü
     */
    private static double calculateTrainingPower(Player player, TrainingManager trainingManager) {
        if (trainingManager == null) return 0;
        int level = trainingManager.getTrainingLevel(player.getUniqueId());
        return level * 5;
    }
    
    /**
     * Klan teknoloji gücü
     */
    private static double calculateClanTechPower(Clan clan) {
        if (clan == null) return 0;
        return clan.getTechLevel() * 10;
    }
}
```

##### **ServerPowerCalculator.java**
```java
package me.mami.stratocraft.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Sunucu toplam güç puanı hesaplama sistemi
 */
public class ServerPowerCalculator {
    
    /**
     * Sunucu toplam güç puanını hesapla
     */
    public static double calculateServerPower(ClanManager clanManager, TrainingManager trainingManager) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.isEmpty()) return 0;
        
        double totalPower = 0;
        int activePlayerCount = 0;
        
        for (Player player : players) {
            if (player.isOnline() && !player.isDead()) {
                double playerPower = PlayerPowerCalculator.calculatePlayerPower(
                    player, clanManager, trainingManager
                );
                totalPower += playerPower;
                activePlayerCount++;
            }
        }
        
        if (activePlayerCount == 0) return 0;
        
        double averagePower = totalPower / activePlayerCount;
        double playerCountMultiplier = getPlayerCountMultiplier(activePlayerCount);
        
        return averagePower * playerCountMultiplier;
    }
    
    /**
     * Aktif oyuncu sayısına göre çarpan
     */
    private static double getPlayerCountMultiplier(int playerCount) {
        if (playerCount <= 3) return 0.8;
        if (playerCount <= 6) return 1.0;
        if (playerCount <= 10) return 1.3;
        if (playerCount <= 15) return 1.6;
        return 2.0; // 16+
    }
}
```

##### **DisasterManager.java Güncellemesi**
```java
// Mevcut calculateDisasterPower metodunu güncelle

public DisasterPower calculateDisasterPower(int level) {
    // Config'den seviye config'i al
    DisasterConfig levelConfig;
    if (configManager != null) {
        levelConfig = configManager.getConfigForLevel(level);
    } else {
        levelConfig = new DisasterConfig();
    }
    
    // YENİ: Sunucu güç puanını hesapla
    double serverPower = ServerPowerCalculator.calculateServerPower(
        clanManager, 
        trainingManager
    );
    
    // Config'den temel güç ve çarpanlar
    double baseHealth = levelConfig.getBaseHealth() * levelConfig.getHealthMultiplier();
    double baseDamage = levelConfig.getBaseDamage() * levelConfig.getDamageMultiplier();
    
    // YENİ: Güç çarpanı hesaplama (sunucu gücüne göre)
    double powerScalingFactor = levelConfig.getPowerScalingFactor(); // Config'den
    double powerMultiplier = 1.0 + (serverPower / 100.0) * powerScalingFactor;
    
    // Maksimum ve minimum sınırlar (config'den)
    double minMultiplier = levelConfig.getMinPowerMultiplier(); // Varsayılan: 0.5
    double maxMultiplier = levelConfig.getMaxPowerMultiplier(); // Varsayılan: 5.0
    
    powerMultiplier = Math.max(minMultiplier, Math.min(maxMultiplier, powerMultiplier));
    
    // Hesaplanmış güç
    double calculatedHealth = baseHealth * powerMultiplier;
    double calculatedDamage = baseDamage * powerMultiplier;
    
    return new DisasterPower(calculatedHealth, calculatedDamage, powerMultiplier);
}
```

### ⚙️ Config.yml Güncellemeleri

```yaml
disaster:
  # Güç Hesaplama Sistemi
  power:
    # Eski sistem (geriye dönük uyumluluk için)
    player-multiplier: 0.1
    clan-multiplier: 0.15
    
    # YENİ: Dinamik Zorluk Sistemi
    dynamic-difficulty:
      enabled: true
      power-scaling-factor: 1.0      # Güç artış hızı (0.5-2.0)
      min-power-multiplier: 0.5      # Minimum güç çarpanı
      max-power-multiplier: 5.0       # Maksimum güç çarpanı
      
      # Ağırlık çarpanları
      weights:
        structure: 0.3
        item: 0.4
        buff: 0.15
        training: 0.1
        clan-tech: 0.05
      
      # Yapı tipi çarpanları
      structure-multipliers:
        battery: 2.0
        research-center: 1.5
        production: 1.2
        defense: 1.8
        default: 1.0
      
      # Oyuncu sayısı çarpanları
      player-count-multipliers:
        "1-3": 0.8
        "4-6": 1.0
        "7-10": 1.3
        "11-15": 1.6
        "16+": 2.0
```

### 📊 Örnek Senaryolar

#### **Senaryo 1: Zayıf Sunucu**
```
Aktif Oyuncu: 3
Ortalama Güç: 50 puan
Oyuncu Çarpanı: 0.8x

Sunucu Güç Puanı = 50 × 0.8 = 40 puan
Felaket Çarpanı = 1.0 × (1 + (40/100) × 1.0) = 1.4x

Sonuç: Felaketler %40 daha güçlü
```

#### **Senaryo 2: Orta Güçlü Sunucu**
```
Aktif Oyuncu: 8
Ortalama Güç: 150 puan
Oyuncu Çarpanı: 1.3x

Sunucu Güç Puanı = 150 × 1.3 = 195 puan
Felaket Çarpanı = 1.0 × (1 + (195/100) × 1.0) = 2.95x

Sonuç: Felaketler %195 daha güçlü
```

#### **Senaryo 3: Çok Güçlü Sunucu**
```
Aktif Oyuncu: 15
Ortalama Güç: 300 puan
Oyuncu Çarpanı: 1.6x

Sunucu Güç Puanı = 300 × 1.6 = 480 puan
Felaket Çarpanı = 1.0 × (1 + (480/100) × 1.0) = 5.8x
Maksimum Sınır: 5.0x

Sonuç: Felaketler %400 daha güçlü (maksimum)
```

---

## 2. FAZ SİSTEMİ

### 📊 Faz Geçişleri

Her felaket 4 fazdan oluşur:

#### **Faz 1: Keşif (100%-75% Can)**
- **Davranış:** Normal hareket, temel saldırılar
- **Özel Yetenekler:** Yok
- **Saldırı Sıklığı:** Normal (2 dakikada bir)
- **Kristal Hedefleme:** Aktif

#### **Faz 2: Saldırı (75%-50% Can)**
- **Davranış:** Daha agresif, özel yetenekler aktif
- **Özel Yetenekler:** 1-2 yetenek aktif
- **Saldırı Sıklığı:** Artmış (1.5 dakikada bir)
- **Kristal Hedefleme:** Aktif, daha hızlı

#### **Faz 3: Öfke (50%-25% Can)**
- **Davranış:** Çok agresif, tüm yetenekler aktif
- **Özel Yetenekler:** Tüm yetenekler aktif
- **Saldırı Sıklığı:** Çok artmış (1 dakikada bir)
- **Kristal Hedefleme:** Çok hızlı, oyunculara da saldırı

#### **Faz 4: Son Çare (25%-0% Can)**
- **Davranış:** Umutsuz saldırılar, kendini feda etme
- **Özel Yetenekler:** Sürekli kullanım
- **Saldırı Sıklığı:** Maksimum (30 saniyede bir)
- **Kristal Hedefleme:** Öncelikli, oyunculara sürekli saldırı

### 💻 Teknik İmplementasyon

```java
public enum DisasterPhase {
    EXPLORATION(1.0, 0.75, "Keşif", 120000L, 0),
    ASSAULT(0.75, 0.50, "Saldırı", 90000L, 2),
    RAGE(0.50, 0.25, "Öfke", 60000L, 5),
    DESPERATION(0.25, 0.0, "Son Çare", 30000L, 10);
    
    private final double maxHealthPercent;
    private final double minHealthPercent;
    private final String displayName;
    private final long attackInterval;
    private final int activeAbilityCount;
    
    DisasterPhase(double max, double min, String name, long interval, int abilities) {
        this.maxHealthPercent = max;
        this.minHealthPercent = min;
        this.displayName = name;
        this.attackInterval = interval;
        this.activeAbilityCount = abilities;
    }
    
    public static DisasterPhase getCurrentPhase(double healthPercent) {
        for (DisasterPhase phase : values()) {
            if (healthPercent <= phase.maxHealthPercent && 
                healthPercent > phase.minHealthPercent) {
                return phase;
            }
        }
        return DESPERATION;
    }
    
    // Getters...
}
```

---

## 3. ÖZEL YETENEKLER VE HAREKETLER

### ⚔️ Her Felaket Tipi İçin Özel Yetenekler

Detaylar `FELAKET_SISTEMI_EPIK_GELISTIRME_ONERILERI.md` dosyasında.

**Özet:**
- **Titan Golem:** Yer Sarsma, Taş Fırlatma, Taş Duvar
- **Khaos Ejderi:** Ateş Püskürtme, Gökyüzü Saldırısı, Ateş Yağmuru
- **Hiçlik Solucanı:** Yer Altına Dalış, Hiçlik Çekimi, Yer Yarığı
- **Buzul Leviathan:** Buz Fırtınası, Buz Duvarı, Buz Patlaması

---

## 4. AI İYİLEŞTİRMELERİ

### 🧠 Akıllı Hedef Seçimi
- Stratejik hedefleme (en zayıf klan)
- Oyuncu tehdit analizi
- Çoklu hedef sistemi

### 🎯 Gelişmiş Pathfinding
- A* algoritması
- Dinamik rota bulma
- Grup koordinasyonu

### 🏃 Akıllı Hareket Desenleri
- Zigzag hareket
- Sprint modu
- Geri çekilme
- Flanking

---

## 5. ÇEVRESEL ETKİLER VE KORKUTMA

### 🌍 Çevre Değişiklikleri
- Gökyüzü değişimi
- Yer titremesi
- Hava değişimi
- Blok yıkımı
- Kalıcı izler

### 🎭 Korkutma Mekanikleri
- Uyarı sistemi (5 dk, 2 dk, 30 sn önce)
- Psikolojik etkiler (karanlık mod, ses efektleri)

---

## 6. GÖRSEL VE İŞİTSEL EFEKTLER

### ✨ Partikül Efektleri
- Her felaket için özel efektler
- Faz geçişi animasyonları
- Kritik hasar efektleri

### 🔊 Ses Efektleri
- Ambient müzik
- Yetenek sesleri
- Faz geçişi sesleri

---

## 7. İŞBİRLİKÇİ MEKANİKLER

### 👥 Takım Çalışması
- Tank/DPS/Healer sistemi
- Zayıf nokta mekanikleri
- Koordinasyon gerektiren görevler

---

## 8. ÖDÜL VE İLERLEME SİSTEMİ

### 🏆 Ödül Sistemi
- Faz geçişi ödülleri
- Özel başarımlar
- Nadir ödüller

### 📊 İlerleme Sistemi
- Felaket defteri
- Felaket seviyesi
- Rozetler ve başarımlar

---

## 9. ÖZEL SENARYOLAR

### 🎬 Epik Anlar
- Felaket girişi
- Faz geçişi
- Ölüm animasyonu

### 🎮 Mini Oyunlar
- Felaket kaçışı
- Felaket savunması

---

## 🎯 UYGULAMA ÖNCELİĞİ

### ⚡ Faz 1: Dinamik Zorluk (1 Hafta)
1. PlayerPowerCalculator sınıfı
2. ServerPowerCalculator sınıfı
3. DisasterManager güncellemesi
4. Config.yml güncellemeleri
5. Test ve dengeleme

### 🚀 Faz 2: Faz Sistemi (3-5 Gün)
1. DisasterPhase enum
2. Disaster model güncellemesi
3. Handler güncellemeleri
4. Faz geçişi animasyonları

### 🏆 Faz 3: Özel Yetenekler (1-2 Hafta)
1. DisasterAbility interface
2. Her felaket için 2-3 yetenek
3. Yetenek sistemi entegrasyonu
4. Test ve dengeleme

### 🎨 Faz 4: Görsel/İşitsel (1 Hafta)
1. Partikül efektleri
2. Ses efektleri
3. Animasyonlar

### 🌍 Faz 5: Çevresel Etkiler (1 Hafta)
1. Gökyüzü değişimi
2. Yer titremesi
3. Çevre hasarı

---

## 📝 NOTLAR

- **Dinamik Zorluk Sistemi en öncelikli özelliktir**
- Tüm hesaplamalar config'den ayarlanabilir olmalı
- Performans optimizasyonu önemli (cache kullanımı)
- Test senaryoları hazırlanmalı
- Dengeleme için admin komutları eklenmeli

---

**Hazırlayan:** AI Assistant
**Tarih:** 2024
**Durum:** Planlama - Uygulanmayı Bekliyor
