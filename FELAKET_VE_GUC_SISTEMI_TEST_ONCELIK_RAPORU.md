# 🧪 FELAKET VE GÜÇ SİSTEMİ TEST ÖNCELİK RAPORU

## 📋 RAPOR AMACI

Bu rapor, **felaket sistemi** ve **oyuncu güç hesaplama sistemi** testlerinin yapılabilmesi için:
1. **Zorunlu** yapılması gereken özellikleri
2. **Entegrasyon** gereken noktaları
3. **Güncelleme** gereken yerleri
4. **Öncelik sırasını** belirler.

---

## 🎯 TEST İÇİN ZORUNLU ÖZELLİKLER

### ✅ FAZ 1: KRİTİK ENTEGRASYONLAR (Test İçin Zorunlu)

#### 1.1. Ritüel Güç Entegrasyonu ⚠️ **ÇOK YÜKSEK ÖNCELİK - TEST İÇİN ZORUNLU**

**Durum:** Sistem hazır, sadece entegrasyon eksik

**Yapılması Gerekenler:**

**A. RitualInteractionListener.java Güncellemesi:**
```java
// Mevcut: onRecruitmentRitual metodunda
// Eklenecek: Ritüel başarılı olduğunda güç sistemi bildirimi

@EventHandler(priority = EventPriority.HIGH)
public void onRecruitmentRitual(PlayerInteractEvent event) {
    // ... mevcut kod ...
    
    // Ritüel başarılı oldu
    if (recruitedPlayers.size() > 0) {
        // ✅ EKLENECEK: Güç sistemi entegrasyonu
        Map<String, Integer> usedResources = new HashMap<>();
        usedResources.put("FLINT_AND_STEEL", 1); // Çakmak tüketildi
        
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
            plugin.getStratocraftPowerSystem().onRitualSuccess(
                clan, 
                "RECRUITMENT_RITUAL", 
                usedResources
            );
        }
    }
}
```

**B. NewBatteryManager.java Güncellemesi:**
```java
// Batarya aktifleştiğinde güç sistemi bildirimi

public void activateBattery(Player player, BatteryType type, Location location) {
    // ... mevcut kod ...
    
    // Batarya başarıyla aktifleşti
    Clan clan = territoryManager.getTerritoryOwner(location);
    if (clan != null) {
        // ✅ EKLENECEK: Güç sistemi entegrasyonu
        Map<String, Integer> usedResources = getBatteryResources(type);
        
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
            plugin.getStratocraftPowerSystem().onRitualSuccess(
                clan,
                "BATTERY_" + type.name(),
                usedResources
            );
        }
    }
}
```

**C. Diğer Ritüel Sistemleri:**
- `BreedingManager.java` - Üreme ritüelleri
- `ResearchManager.java` - Araştırma ritüelleri
- Diğer ritüel listener'ları

**Tahmini Süre:** 2-4 saat

**Test İçin Önemi:** ⭐⭐⭐⭐⭐ (Çok Kritik)
- Ritüel kaynak gücü hesaplanmıyor
- Klan gücü eksik hesaplanıyor
- Felaket zorluğu yanlış hesaplanabilir

---

#### 1.2. Felaket Sistemi - Güç Sistemi Entegrasyonu ⚠️ **ÇOK YÜKSEK ÖNCELİK - TEST İÇİN ZORUNLU**

**Durum:** Mevcut sistem `PlayerPowerCalculator` kullanıyor, yeni `StratocraftPowerSystem` ile entegre edilmeli

**Yapılması Gerekenler:**

**A. DisasterManager.java Güncellemesi:**
```java
// Mevcut: PlayerPowerCalculator kullanıyor
// Güncelleme: StratocraftPowerSystem kullanmalı (veya köprü fonksiyon)

public class DisasterManager {
    private PlayerPowerCalculator playerPowerCalculator; // Eski sistem
    private StratocraftPowerSystem stratocraftPowerSystem; // Yeni sistem
    
    /**
     * Felaket için oyuncu gücü al (köprü fonksiyon)
     */
    private double getPlayerPowerForDisaster(Player player) {
        // Yeni sistem varsa onu kullan
        if (stratocraftPowerSystem != null) {
            PlayerPowerProfile profile = stratocraftPowerSystem.calculatePlayerProfile(player);
            // Felaket için combat power önemli
            return profile.getTotalCombatPower();
        }
        
        // Fallback: Eski sistem
        if (playerPowerCalculator != null) {
            return playerPowerCalculator.calculatePlayerPower(player);
        }
        
        return 0.0;
    }
    
    /**
     * ServerPowerCalculator güncellemesi
     */
    private double calculateServerPower() {
        // Yeni sistemden tüm oyuncuların gücünü al
        if (stratocraftPowerSystem != null) {
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (onlinePlayers.isEmpty()) return 0.0;
            
            double totalPower = 0.0;
            for (Player player : onlinePlayers) {
                PlayerPowerProfile profile = stratocraftPowerSystem.calculatePlayerProfile(player);
                totalPower += profile.getTotalSGP(); // Veya totalCombatPower
            }
            
            double averagePower = totalPower / onlinePlayers.size();
            int activePlayerCount = onlinePlayers.size();
            
            // Aktif oyuncu sayısı çarpanı
            double playerCountMultiplier = 1.0 + (activePlayerCount * 0.1); // Her oyuncu %10 bonus
            
            return averagePower * playerCountMultiplier;
        }
        
        // Fallback: Eski sistem
        if (serverPowerCalculator != null) {
            return serverPowerCalculator.calculateServerPower();
        }
        
        return 0.0;
    }
}
```

**B. Config Entegrasyonu:**
```yaml
# config.yml
disaster-system:
  power-calculation:
    use-new-system: true  # Yeni güç sistemi kullan
    use-combat-power: true  # Combat power kullan (felaket için)
    # false ise total SGP kullan
```

**Tahmini Süre:** 3-5 saat

**Test İçin Önemi:** ⭐⭐⭐⭐⭐ (Çok Kritik)
- Felaket zorluğu yanlış hesaplanıyor
- Test sonuçları güvenilir olmaz

---

#### 1.3. Komut Sistemi (Test İçin) ⚠️ **YÜKSEK ÖNCELİK - TEST İÇİN ZORUNLU**

**Durum:** Güç görüntüleme komutları yok

**Yapılması Gerekenler:**

**A. SGP Komutları:**
```java
public class SGPCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular için!");
            return true;
        }
        
        Player player = (Player) sender;
        Main plugin = Main.getInstance();
        StratocraftPowerSystem powerSystem = plugin.getStratocraftPowerSystem();
        
        if (powerSystem == null) {
            player.sendMessage("§cGüç sistemi yüklenemedi!");
            return true;
        }
        
        if (args.length == 0) {
            // Kendi gücünü göster
            showPlayerPower(player, player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "me":
            case "self":
                showPlayerPower(player, player);
                break;
                
            case "player":
            case "p":
                if (args.length < 2) {
                    player.sendMessage("§cKullanım: /sgp player <oyuncu>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cOyuncu bulunamadı!");
                    return true;
                }
                showPlayerPower(player, target);
                break;
                
            case "clan":
            case "c":
                Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("§cBir klana ait değilsiniz!");
                    return true;
                }
                showClanPower(player, clan);
                break;
                
            case "top":
                showTopPlayers(player, 10);
                break;
                
            case "components":
                showPowerComponents(player, player);
                break;
                
            default:
                player.sendMessage("§cBilinmeyen komut! /sgp help");
                break;
        }
        
        return true;
    }
    
    private void showPlayerPower(Player viewer, Player target) {
        StratocraftPowerSystem powerSystem = Main.getInstance().getStratocraftPowerSystem();
        PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(target);
        
        viewer.sendMessage("§6=== " + target.getName() + " Güç Bilgileri ===");
        viewer.sendMessage("§eToplam SGP: §f" + String.format("%.2f", profile.getTotalSGP()));
        viewer.sendMessage("§eCombat Power: §f" + String.format("%.2f", profile.getTotalCombatPower()));
        viewer.sendMessage("§eProgression Power: §f" + String.format("%.2f", profile.getTotalProgressionPower()));
        viewer.sendMessage("§eSeviye: §f" + profile.getPlayerLevel());
        viewer.sendMessage("§6--- Bileşenler ---");
        viewer.sendMessage("§7- Eşya Gücü: §f" + String.format("%.2f", profile.getGearPower()));
        viewer.sendMessage("§7- Ustalık Gücü: §f" + String.format("%.2f", profile.getTrainingPower()));
        viewer.sendMessage("§7- Buff Gücü: §f" + String.format("%.2f", profile.getBuffPower()));
        viewer.sendMessage("§7- Ritüel Gücü: §f" + String.format("%.2f", profile.getRitualPower()));
    }
    
    private void showClanPower(Player viewer, Clan clan) {
        StratocraftPowerSystem powerSystem = Main.getInstance().getStratocraftPowerSystem();
        ClanPowerProfile profile = powerSystem.calculateClanProfile(clan);
        
        viewer.sendMessage("§6=== " + clan.getName() + " Klan Güç Bilgileri ===");
        viewer.sendMessage("§eToplam Klan Gücü: §f" + String.format("%.2f", profile.getTotalClanPower()));
        viewer.sendMessage("§eKlan Seviyesi: §f" + profile.getClanLevel());
        viewer.sendMessage("§6--- Bileşenler ---");
        viewer.sendMessage("§7- Üye Gücü: §f" + String.format("%.2f", profile.getMemberPowerSum()));
        viewer.sendMessage("§7- Yapı Gücü: §f" + String.format("%.2f", profile.getStructurePower()));
        viewer.sendMessage("§7- Ritüel Blok Gücü: §f" + String.format("%.2f", profile.getRitualBlockPower()));
        viewer.sendMessage("§7- Ritüel Kaynak Gücü: §f" + String.format("%.2f", profile.getRitualResourcePower()));
    }
}
```

**B. plugin.yml Güncellemesi:**
```yaml
commands:
  sgp:
    description: Güç sistemi komutları
    usage: /sgp [me|player|clan|top|components]
    aliases: [power, guc]
```

**Tahmini Süre:** 2-3 saat

**Test İçin Önemi:** ⭐⭐⭐⭐ (Yüksek)
- Güç değerlerini görmek için gerekli
- Test sırasında doğrulama için kritik

---

### ✅ FAZ 2: TEST DESTEK SİSTEMLERİ (Test İçin Önerilen)

#### 2.1. Güç Sıralaması Sistemi (Basit Versiyon) ⚠️ **ORTA ÖNCELİK - TEST İÇİN ÖNERİLEN**

**Durum:** Test sırasında güç karşılaştırması için yararlı

**Yapılması Gerekenler:**

**A. Basit Sıralama Sistemi:**
```java
public class SimpleRankingSystem {
    /**
     * Top oyuncuları al (basit versiyon)
     */
    public List<PlayerRanking> getTopPlayers(int limit) {
        StratocraftPowerSystem powerSystem = Main.getInstance().getStratocraftPowerSystem();
        List<PlayerRanking> rankings = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(player);
            rankings.add(new PlayerRanking(player, profile.getTotalSGP(), profile.getPlayerLevel()));
        }
        
        return rankings.stream()
            .sorted(Comparator.comparing(PlayerRanking::getPower).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

**B. /sgp top Komutu:**
```java
case "top":
    List<PlayerRanking> topPlayers = rankingSystem.getTopPlayers(10);
    player.sendMessage("§6=== Top 10 Oyuncular ===");
    for (int i = 0; i < topPlayers.size(); i++) {
        PlayerRanking ranking = topPlayers.get(i);
        player.sendMessage("§e" + (i + 1) + ". §f" + ranking.getPlayerName() + 
            " §7- §f" + String.format("%.2f", ranking.getPower()) + " SGP");
    }
    break;
```

**Tahmini Süre:** 1-2 saat

**Test İçin Önemi:** ⭐⭐⭐ (Orta)
- Güç karşılaştırması için yararlı
- Test sonuçlarını doğrulamak için

---

#### 2.2. Güç Geçmişi (Basit Versiyon) ⚠️ **DÜŞÜK ÖNCELİK - TEST İÇİN OPSİYONEL**

**Durum:** Test sırasında güç değişimini takip etmek için

**Yapılması Gerekenler:**

**A. Basit Log Sistemi:**
```java
public class SimplePowerHistory {
    // Oyuncu -> Son güç değeri
    private final Map<UUID, Double> lastPower = new ConcurrentHashMap<>();
    
    /**
     * Güç değişimini logla
     */
    public void logPowerChange(Player player, double newPower) {
        Double oldPower = lastPower.get(player.getUniqueId());
        if (oldPower != null) {
            double change = newPower - oldPower;
            if (Math.abs(change) > 100) { // Önemli değişim
                plugin.getLogger().info("Güç Değişimi: " + player.getName() + 
                    " - Eski: " + oldPower + ", Yeni: " + newPower + 
                    ", Değişim: " + (change > 0 ? "+" : "") + change);
            }
        }
        lastPower.put(player.getUniqueId(), newPower);
    }
}
```

**Tahmini Süre:** 1 saat

**Test İçin Önemi:** ⭐⭐ (Düşük)
- Test sırasında güç değişimini görmek için
- Zorunlu değil

---

## 🎮 OYUN İÇİN GEREKLİ AMA TEST İÇİN ZORUNLU OLMAYAN ÖZELLİKLER

### ⭐ FAZ 3: OYUN İÇİN GEREKLİ ÖZELLİKLER (Test Sonrası)

#### 3.1. Günlük Görevler Sistemi ⭐ **YÜKSEK ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük - Test için gerekli değil)
**Oyun İçin Önemi:** ⭐⭐⭐⭐⭐ (Çok Yüksek - Oyuncu tutma için kritik)

**Yapılması Gerekenler:**
- Günlük görev sistemi
- Görev ilerleme takibi
- Görev ödülleri

**Tahmini Süre:** 2-3 gün

---

#### 3.2. Başarı Sistemi ⭐ **YÜKSEK ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐⭐⭐ (Çok Yüksek - Oyuncu motivasyonu için kritik)

**Yapılması Gerekenler:**
- Başarı kategorileri
- Başarı takibi
- Başarı ödülleri

**Tahmini Süre:** 2-3 gün

---

#### 3.3. Etkinlik Sistemi ⭐ **ORTA ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐⭐ (Yüksek - Oyuncu aktivitesi için)

**Tahmini Süre:** 3-4 gün

---

#### 3.4. Klan Seviye Sistemi (Gelişmiş) ⭐ **YÜKSEK ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐⭐ (Düşük - Temel seviye hesaplama var)
**Oyun İçin Önemi:** ⭐⭐⭐⭐⭐ (Çok Yüksek - Klan gelişimi için kritik)

**Yapılması Gerekenler:**
- Seviye bazlı özellikler (üye limiti, toprak limiti, yapı limiti)
- Seviye bazlı bonuslar

**Tahmini Süre:** 1-2 gün

---

#### 3.5. Klan Rütbe Sistemi (Gelişmiş) ⭐ **YÜKSEK ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük - Temel rütbe sistemi var)
**Oyun İçin Önemi:** ⭐⭐⭐⭐ (Yüksek - Klan yönetimi için)

**Tahmini Süre:** 1-2 gün

---

#### 3.6. Ritüel Seviye Sistemi ⭐ **ORTA ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐⭐ (Yüksek - Ritüel derinliği için)

**Tahmini Süre:** 2-3 gün

---

#### 3.7. Prestij Sistemi ⭐ **ORTA ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐ (Orta - Uzun vadeli hedef)

**Tahmini Süre:** 2-3 gün

---

#### 3.8. Güç Bazlı Özellikler (Dungeon, Item) ⭐ **ORTA ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐⭐ (Yüksek - İçerik derinliği için)

**Tahmini Süre:** 3-4 gün

---

#### 3.9. Klan Marketi Sistemi ⭐ **ORTA ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐ (Orta - Ekonomi için)

**Tahmini Süre:** 3-4 gün

---

#### 3.10. Sosyal Özellikler (Arkadaş, Profil) ⭐ **DÜŞÜK ÖNCELİK - OYUN İÇİN**

**Test İçin Önemi:** ⭐ (Düşük)
**Oyun İçin Önemi:** ⭐⭐⭐ (Orta - Sosyal etkileşim için)

**Tahmini Süre:** 2-3 gün

---

## 📊 ÖNCELİK SIRALAMASI ÖZET

### 🔴 FAZ 1: TEST İÇİN ZORUNLU (Hemen Yapılmalı)

| # | Özellik | Öncelik | Süre | Test İçin Önemi |
|---|---------|---------|------|-----------------|
| 1 | Ritüel Güç Entegrasyonu | ⭐⭐⭐⭐⭐ | 2-4 saat | ⭐⭐⭐⭐⭐ |
| 2 | Felaket-Güç Sistemi Entegrasyonu | ⭐⭐⭐⭐⭐ | 3-5 saat | ⭐⭐⭐⭐⭐ |
| 3 | Komut Sistemi (/sgp) | ⭐⭐⭐⭐ | 2-3 saat | ⭐⭐⭐⭐ |

**Toplam Süre:** 7-12 saat (1-2 gün)

---

### 🟡 FAZ 2: TEST İÇİN ÖNERİLEN (Test Sırasında Yararlı)

| # | Özellik | Öncelik | Süre | Test İçin Önemi |
|---|---------|---------|------|-----------------|
| 4 | Güç Sıralaması (Basit) | ⭐⭐⭐ | 1-2 saat | ⭐⭐⭐ |
| 5 | Güç Geçmişi (Basit) | ⭐⭐ | 1 saat | ⭐⭐ |

**Toplam Süre:** 2-3 saat

---

### 🟢 FAZ 3: OYUN İÇİN GEREKLİ (Test Sonrası)

| # | Özellik | Öncelik | Süre | Oyun İçin Önemi |
|---|---------|---------|------|-----------------|
| 6 | Günlük Görevler | ⭐⭐⭐⭐⭐ | 2-3 gün | ⭐⭐⭐⭐⭐ |
| 7 | Başarı Sistemi | ⭐⭐⭐⭐⭐ | 2-3 gün | ⭐⭐⭐⭐⭐ |
| 8 | Klan Seviye Sistemi (Gelişmiş) | ⭐⭐⭐⭐ | 1-2 gün | ⭐⭐⭐⭐⭐ |
| 9 | Klan Rütbe Sistemi (Gelişmiş) | ⭐⭐⭐⭐ | 1-2 gün | ⭐⭐⭐⭐ |
| 10 | Ritüel Seviye Sistemi | ⭐⭐⭐ | 2-3 gün | ⭐⭐⭐⭐ |
| 11 | Etkinlik Sistemi | ⭐⭐⭐ | 3-4 gün | ⭐⭐⭐⭐ |
| 12 | Güç Bazlı Özellikler | ⭐⭐⭐ | 3-4 gün | ⭐⭐⭐⭐ |
| 13 | Prestij Sistemi | ⭐⭐ | 2-3 gün | ⭐⭐⭐ |
| 14 | Klan Marketi | ⭐⭐ | 3-4 gün | ⭐⭐⭐ |
| 15 | Sosyal Özellikler | ⭐⭐ | 2-3 gün | ⭐⭐⭐ |

---

## 🔧 ENTEGRASYON NOKTALARI DETAYLI

### 1. Ritüel Entegrasyonu Noktaları

**Dosyalar:**
- `src/main/java/me/mami/stratocraft/listener/RitualInteractionListener.java`
  - `onRecruitmentRitual()` - Satır ~126
  - `onLeaveRitual()` - Satır ~370
  
- `src/main/java/me/mami/stratocraft/manager/NewBatteryManager.java`
  - `activateBattery()` - Batarya aktifleştiğinde
  
- `src/main/java/me/mami/stratocraft/manager/BreedingManager.java`
  - Üreme ritüelleri için
  
- `src/main/java/me/mami/stratocraft/manager/ResearchManager.java`
  - Araştırma ritüelleri için

**Entegrasyon Kodu:**
```java
// Her ritüel başarılı olduğunda:
Main plugin = Main.getInstance();
if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
    Map<String, Integer> usedResources = new HashMap<>();
    // Ritüel tipine göre kaynakları ekle
    usedResources.put("DIAMOND", 10);
    usedResources.put("IRON", 5);
    
    plugin.getStratocraftPowerSystem().onRitualSuccess(
        clan,
        "RITUAL_TYPE",
        usedResources
    );
}

// Ritüel başarısız olduğunda:
plugin.getStratocraftPowerSystem().onRitualFailure(clan, "RITUAL_TYPE");
```

---

### 2. Felaket Sistemi Entegrasyonu Noktaları

**Dosyalar:**
- `src/main/java/me/mami/stratocraft/manager/DisasterManager.java`
  - `calculateDisasterPower()` - Satır ~200-300 (tahmini)
  - `spawnDisaster()` - Felaket spawn edilirken
  
- `src/main/java/me/mami/stratocraft/manager/ServerPowerCalculator.java`
  - `calculateServerPower()` - Sunucu gücü hesaplama

**Güncelleme Stratejisi:**

**Seçenek 1: Köprü Fonksiyon (Önerilen - Hızlı)**
```java
// DisasterManager.java
private double getPlayerPowerForDisaster(Player player) {
    if (stratocraftPowerSystem != null) {
        PlayerPowerProfile profile = stratocraftPowerSystem.calculatePlayerProfile(player);
        return profile.getTotalCombatPower(); // Felaket için combat power
    }
    // Fallback: Eski sistem
    return playerPowerCalculator.calculatePlayerPower(player);
}
```

**Seçenek 2: Tam Entegrasyon (Gelecek)**
```java
// ServerPowerCalculator tamamen StratocraftPowerSystem kullanır
// Daha büyük refactor gerektirir
```

---

### 3. Komut Sistemi Entegrasyonu

**Dosyalar:**
- `src/main/java/me/mami/stratocraft/command/SGPCommand.java` (YENİ)
- `src/main/resources/plugin.yml` (Güncelleme)

**Main.java Entegrasyonu:**
```java
// onEnable() içinde:
getCommand("sgp").setExecutor(new SGPCommand());
getCommand("sgp").setTabCompleter(new SGPCommand());
```

---

## 📝 TEST SENARYOLARI İÇİN GEREKLİ ÖZELLİKLER

### Senaryo 1: Oyuncu Gücü Hesaplama Testi

**Gerekenler:**
- ✅ `/sgp me` komutu (güç görüntüleme)
- ✅ `/sgp components` komutu (bileşen analizi)
- ⚠️ Ritüel entegrasyonu (ritüel gücü testi için)

**Test Adımları:**
1. Oyuncu farklı eşyalar takar → Güç değişimi kontrol edilir
2. Oyuncu ritüel yapar → Ritüel gücü artışı kontrol edilir
3. Oyuncu ustalık kazanır → Ustalık gücü artışı kontrol edilir

---

### Senaryo 2: Felaket Zorluk Testi

**Gerekenler:**
- ✅ Felaket-Güç sistemi entegrasyonu
- ✅ `/sgp top` komutu (güç karşılaştırması)
- ⚠️ Güç sıralaması (test sonuçlarını doğrulamak için)

**Test Adımları:**
1. Düşük güçlü oyuncular → Felaket zorluğu düşük olmalı
2. Yüksek güçlü oyuncular → Felaket zorluğu yüksek olmalı
3. Güç değişimi → Felaket zorluğu dinamik değişmeli

---

### Senaryo 3: Klan Gücü Testi

**Gerekenler:**
- ✅ `/sgp clan` komutu
- ✅ Ritüel entegrasyonu (ritüel blok/kaynak gücü)
- ⚠️ Güç sıralaması (klan karşılaştırması)

**Test Adımları:**
1. Klan üyeleri eklenir → Klan gücü artışı
2. Ritüel bloklar koyulur → Ritüel blok gücü artışı
3. Ritüel yapılır → Ritüel kaynak gücü artışı

---

## 🎯 SONUÇ VE ÖNERİLER

### Test İçin Minimum Gereksinimler

**Zorunlu (Test Yapılabilmesi İçin):**
1. ✅ Ritüel Güç Entegrasyonu (2-4 saat)
2. ✅ Felaket-Güç Sistemi Entegrasyonu (3-5 saat)
3. ✅ Komut Sistemi (/sgp) (2-3 saat)

**Toplam:** 7-12 saat (1-2 gün)

---

### Test İçin Önerilen (Test Kalitesi İçin)

4. ✅ Güç Sıralaması (Basit) (1-2 saat)
5. ✅ Güç Geçmişi (Basit) (1 saat)

**Toplam:** 2-3 saat

---

### Oyun İçin Gerekli (Test Sonrası)

6-15. Tüm diğer özellikler (test sonrası yapılabilir)

---

## 📋 YAPILACAKLAR LİSTESİ

### Hemen Yapılacaklar (Test İçin Zorunlu)

- [ ] **1. Ritüel Güç Entegrasyonu**
  - [ ] RitualInteractionListener.java güncelle
  - [ ] NewBatteryManager.java güncelle
  - [ ] Diğer ritüel sistemleri güncelle
  
- [ ] **2. Felaket-Güç Sistemi Entegrasyonu**
  - [ ] DisasterManager.java güncelle (köprü fonksiyon)
  - [ ] ServerPowerCalculator güncelle
  - [ ] Config entegrasyonu
  
- [ ] **3. Komut Sistemi**
  - [ ] SGPCommand.java oluştur
  - [ ] plugin.yml güncelle
  - [ ] Main.java entegrasyonu

### Test Sırasında Yapılacaklar (Önerilen)

- [ ] **4. Güç Sıralaması (Basit)**
  - [ ] SimpleRankingSystem.java oluştur
  - [ ] /sgp top komutu ekle

- [ ] **5. Güç Geçmişi (Basit)**
  - [ ] SimplePowerHistory.java oluştur
  - [ ] Log sistemi ekle

### Test Sonrası Yapılacaklar (Oyun İçin)

- [ ] 6-15. Diğer tüm özellikler (öncelik sırasına göre)

---

**Rapor Tarihi:** 2024  
**Versiyon:** 1.0 - Test Öncelik Raporu  
**Durum:** ✅ Test Planı Hazır

