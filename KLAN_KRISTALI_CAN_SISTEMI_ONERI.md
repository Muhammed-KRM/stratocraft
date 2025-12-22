# 💎 Klan Kristali Can Sistemi Önerisi

## 📋 Genel Bakış

Klan kristali can sistemi, klanların kristallerini korumak için kalıcı bir can artırma mekanizması sağlar. Bu sistem sadece hasarı yenilemez, aynı zamanda kalıcı olarak kristal canını artırır.

---

## 🎯 Sistem Mantığı

### 1. **Temel Can Sistemi**
- Her klan kristalinin bir **maksimum can** değeri vardır
- Başlangıç canı: **100 HP** (config'den ayarlanabilir)
- Mevcut can: Maksimum canın altında olabilir (hasar aldığında azalır)

### 2. **Can Artırma Yöntemleri**

#### A. **Yapı Seviyesi ile Can Artırma**
- Klan yapıları (örneğin: Savunma Kulesi, Güçlendirme Merkezi) seviye atladıkça kristal canı kalıcı olarak artar
- Örnek: Savunma Kulesi Seviye 1 → +50 HP, Seviye 2 → +100 HP, Seviye 3 → +200 HP

#### B. **Özel Eşyalar ile Can Artırma**
- Oyuncular özel eşyalar (örneğin: "Kristal Güçlendirme Taşı") kullanarak kristal canını kalıcı olarak artırabilir
- Bu eşyalar nadir drop'lar veya özel üretimler olabilir

#### C. **Ritüel ile Can Artırma**
- Belirli ritüeller (örneğin: "Kristal Güçlendirme Ritüeli") ile kristal canı kalıcı olarak artırılabilir
- Ritüel için özel malzemeler gerekir

#### D. **Klan Seviyesi ile Can Artırma**
- Klan seviyesi arttıkça kristal canı otomatik olarak artar
- Örnek: Klan Seviye 1 → 100 HP, Seviye 2 → 150 HP, Seviye 3 → 250 HP

### 3. **Can Yenileme (Regeneration)**
- Can yenileme, sadece mevcut canı maksimum cana getirir (kalıcı artış değil)
- Yöntemler:
  - **Zaman bazlı**: Belirli aralıklarla otomatik yenilenir (örneğin: 1 HP/dakika)
  - **Yapı bazlı**: Belirli yapılar (örneğin: İyileştirme Merkezi) can yenileme hızını artırır
  - **Eşya bazlı**: Oyuncular özel eşyalar kullanarak canı yenileyebilir

---

## 💻 Kod Örneği

### 1. **Clan Modeline Can Sistemi Ekleme**

```java
// Clan.java içine eklenecek alanlar
private double crystalMaxHealth = 100.0; // Maksimum can (kalıcı artışlar buraya eklenir)
private double crystalCurrentHealth = 100.0; // Mevcut can (hasar aldığında azalır)
private long lastCrystalRegenTime = 0; // Son can yenileme zamanı

// Getter/Setter metodları
public double getCrystalMaxHealth() { return crystalMaxHealth; }
public void setCrystalMaxHealth(double health) { 
    this.crystalMaxHealth = Math.max(100.0, health); // Minimum 100 HP
}

public double getCrystalCurrentHealth() { return crystalCurrentHealth; }
public void setCrystalCurrentHealth(double health) { 
    this.crystalCurrentHealth = Math.max(0.0, Math.min(health, crystalMaxHealth));
}

// Can artırma (kalıcı - maksimum canı artırır)
public void increaseCrystalMaxHealth(double amount) {
    this.crystalMaxHealth += amount;
    // Mevcut canı da artır (yeni maksimum canın %80'i kadar)
    this.crystalCurrentHealth = Math.min(crystalCurrentHealth + (amount * 0.8), crystalMaxHealth);
}

// Can yenileme (geçici - sadece mevcut canı artırır)
public void regenerateCrystalHealth(double amount) {
    this.crystalCurrentHealth = Math.min(crystalCurrentHealth + amount, crystalMaxHealth);
}

// Hasar alma
public void damageCrystal(double damage) {
    this.crystalCurrentHealth = Math.max(0.0, crystalCurrentHealth - damage);
    if (crystalCurrentHealth <= 0) {
        // Kristal yok edildi
        destroyCrystal();
    }
}

// Kristal yok etme
private void destroyCrystal() {
    if (crystalEntity != null) {
        crystalEntity.remove();
    }
    crystalEntity = null;
    crystalLocation = null;
    hasCrystal = false;
    // Klanı dağıt
    // ...
}
```

### 2. **Yapı Seviyesi ile Can Artırma**

```java
// Structure.java veya ilgili yapı handler'ında
public void onStructureLevelUp(Clan clan, Structure structure) {
    if (structure.getType() == Structure.Type.DEFENSE_TOWER) {
        // Savunma Kulesi seviye atladığında kristal canını artır
        double healthIncrease = 0;
        switch (structure.getLevel()) {
            case 1:
                healthIncrease = 50.0;
                break;
            case 2:
                healthIncrease = 100.0;
                break;
            case 3:
                healthIncrease = 200.0;
                break;
        }
        
        if (healthIncrease > 0) {
            clan.increaseCrystalMaxHealth(healthIncrease);
            // Oyunculara bildir
            Bukkit.broadcastMessage(ChatColor.GREEN + clan.getName() + 
                " klanının kristali güçlendirildi! (+" + healthIncrease + " HP)");
        }
    }
}
```

### 3. **Felaket Saldırısında Can Sistemi**

```java
// ChaosDragonHandler.java - attackCrystal metodunda
private void attackCrystal(Disaster disaster, Location crystalLoc, Main plugin) {
    if (plugin == null || plugin.getTerritoryManager() == null) return;
    
    Clan targetClan = plugin.getTerritoryManager().getTerritoryOwner(crystalLoc);
    if (targetClan == null) return;
    
    EnderCrystal crystal = targetClan.getCrystalEntity();
    if (crystal == null || crystal.isDead()) return;
    
    // Felaket hasarı hesapla
    double damage = disaster.getDamageMultiplier() * 10.0; // Base hasar * çarpan
    
    // Kristale hasar ver
    targetClan.damageCrystal(damage);
    
    double currentHealth = targetClan.getCrystalCurrentHealth();
    double maxHealth = targetClan.getCrystalMaxHealth();
    double healthPercent = (currentHealth / maxHealth) * 100.0;
    
    // Partikül efekti (can yüzdesine göre)
    if (healthPercent > 50) {
        // Sağlıklı (yeşil)
        crystalLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, crystalLoc, 10);
    } else if (healthPercent > 25) {
        // Orta (sarı)
        crystalLoc.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, crystalLoc, 15);
    } else {
        // Kritik (kırmızı)
        crystalLoc.getWorld().spawnParticle(Particle.LAVA, crystalLoc, 20);
    }
    
    // Klan üyelerine uyarı
    for (UUID memberId : targetClan.getMembers().keySet()) {
        Player member = Bukkit.getPlayer(memberId);
        if (member != null && member.isOnline()) {
            member.sendMessage(ChatColor.RED + "⚠ Kristal hasar aldı! Can: " + 
                String.format("%.1f", currentHealth) + "/" + 
                String.format("%.1f", maxHealth) + " (" + 
                String.format("%.1f", healthPercent) + "%)");
        }
    }
    
    // Can bitti mi?
    if (currentHealth <= 0) {
        crystal.remove();
        targetClan.destroyCrystal();
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + 
            targetClan.getName() + " klanının kristali yok edildi!");
    }
}
```

### 4. **Can Yenileme Sistemi (Task)**

```java
// CrystalRegenerationTask.java (yeni dosya)
public class CrystalRegenerationTask extends BukkitRunnable {
    private final TerritoryManager territoryManager;
    private final double regenRate = 1.0; // 1 HP/dakika (config'den okunabilir)
    private final long regenInterval = 1200L; // 1 dakika = 1200 tick
    
    @Override
    public void run() {
        if (territoryManager == null) return;
        
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (clan == null || !clan.hasCrystal()) continue;
            
            // Mevcut can maksimum canın altındaysa yenile
            if (clan.getCrystalCurrentHealth() < clan.getCrystalMaxHealth()) {
                // Yapı bazlı yenileme hızı artışı
                double regenMultiplier = 1.0;
                for (Structure structure : clan.getStructures()) {
                    if (structure.getType() == Structure.Type.HEALING_CENTER) {
                        regenMultiplier += structure.getLevel() * 0.2; // Her seviye %20 artış
                    }
                }
                
                double regenAmount = regenRate * regenMultiplier;
                clan.regenerateCrystalHealth(regenAmount);
            }
        }
    }
}
```

### 5. **Özel Eşya ile Can Artırma**

```java
// RitualInteractionListener.java veya ilgili listener'da
@EventHandler
public void onCrystalEnhancementItemUse(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();
    
    // Özel eşya kontrolü (örneğin: "Kristal Güçlendirme Taşı")
    if (item != null && item.getType() == Material.EMERALD && 
        item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
        item.getItemMeta().getDisplayName().equals("§6Kristal Güçlendirme Taşı")) {
        
        Clan playerClan = territoryManager.getPlayerClan(player.getUniqueId());
        if (playerClan == null || !playerClan.hasCrystal()) {
            player.sendMessage(ChatColor.RED + "Klanınızın kristali yok!");
            return;
        }
        
        // Kristal yakınında mı?
        Location crystalLoc = playerClan.getCrystalLocation();
        if (crystalLoc == null || player.getLocation().distance(crystalLoc) > 10) {
            player.sendMessage(ChatColor.RED + "Kristale yakın değilsiniz! (10 blok içinde olmalısınız)");
            return;
        }
        
        // Can artır
        double healthIncrease = 50.0; // Config'den okunabilir
        playerClan.increaseCrystalMaxHealth(healthIncrease);
        
        // Eşyayı tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Efekt
        crystalLoc.getWorld().spawnParticle(Particle.TOTEM, crystalLoc, 30);
        player.sendMessage(ChatColor.GREEN + "Kristal güçlendirildi! (+" + healthIncrease + " HP)");
        
        event.setCancelled(true);
    }
}
```

---

## 📊 Config Önerisi

```yaml
# config.yml içine eklenecek
crystal:
  base_health: 100.0  # Başlangıç canı
  max_health_cap: 10000.0  # Maksimum can limiti
  regen_rate: 1.0  # Can yenileme hızı (HP/dakika)
  regen_interval: 1200  # Can yenileme aralığı (tick)
  
  # Yapı bazlı can artışları
  structure_health_boosts:
    defense_tower:
      level_1: 50.0
      level_2: 100.0
      level_3: 200.0
    healing_center:
      level_1: 25.0
      level_2: 50.0
      level_3: 100.0
      regen_multiplier_per_level: 0.2  # Her seviye %20 yenileme hızı artışı
  
  # Klan seviyesi bazlı can artışları
  clan_level_health_boosts:
    level_1: 100.0
    level_2: 150.0
    level_3: 250.0
    level_4: 400.0
    level_5: 600.0
```

---

## 🎮 Kullanıcı Deneyimi

### 1. **Kristal Can Göstergesi**
- BossBar veya ActionBar ile kristal canı gösterilebilir
- Örnek: `§c[████████░░] 800/1000 HP (80%)`

### 2. **Uyarı Sistemi**
- Can %50'nin altına düştüğünde klan üyelerine uyarı
- Can %25'in altına düştüğünde kritik uyarı
- Can %10'un altına düştüğünde acil uyarı

### 3. **Görsel Efektler**
- Can yüzdesine göre kristal rengi değişebilir (yeşil → sarı → kırmızı)
- Hasar aldığında partikül efektleri
- Can yenilendiğinde iyileştirme efektleri

---

## 🔄 Veri Saklama

### DataManager.java'da eklenecek:

```java
// ClanSnapshot içine
public static class ClanSnapshot {
    // ... mevcut alanlar ...
    public double crystalMaxHealth = 100.0;
    public double crystalCurrentHealth = 100.0;
}

// Kaydetme
data.crystalMaxHealth = clan.getCrystalMaxHealth();
data.crystalCurrentHealth = clan.getCrystalCurrentHealth();

// Yükleme
if (data.crystalMaxHealth > 0) {
    clan.setCrystalMaxHealth(data.crystalMaxHealth);
}
if (data.crystalCurrentHealth > 0) {
    clan.setCrystalCurrentHealth(data.crystalCurrentHealth);
}
```

---

## ✅ Özet

Bu sistem:
1. ✅ **Kalıcı can artışı** sağlar (yapılar, eşyalar, ritüeller ile)
2. ✅ **Can yenileme** mekanizması içerir (zaman bazlı, yapı bazlı)
3. ✅ **Felaket saldırılarında** can sistemi kullanır
4. ✅ **Kullanıcı dostu** görsel geri bildirimler sağlar
5. ✅ **Config'den ayarlanabilir** tüm değerler
6. ✅ **Veri saklama** desteği içerir

Bu sistem sayesinde klanlar kristallerini korumak için stratejik kararlar alabilir ve kristallerini güçlendirmek için çaba gösterebilirler.

