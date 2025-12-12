# 🏰 Klan Sistemi Detaylı Durum Raporu

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Klan Kurma](#klan-kurma)
3. [Klan Kristali](#klan-kristali)
4. [Klan Sınırları](#klan-sınırları)
5. [Savaş Sistemi](#savaş-sistemi)
6. [Yetki Sistemi](#yetki-sistemi)
7. [Klan Koruması](#klan-koruması)
8. [Kervan Sistemi](#kervan-sistemi)
9. [Özel Sandık Sistemi](#özel-sandık-sistemi)
10. [Klan Yapı Buffları](#klan-yapı-buffları)
11. [Klan Görevleri](#klan-görevleri)
12. [Klan Ritüelleri](#klan-ritüelleri)
13. [Eğitilen Canlılar](#eğitilen-canlılar)
14. [Klan Alanı Genişletme](#klan-alanı-genişletme)
15. [Eksikler ve Sorunlar](#eksikler-ve-sorunlar)

---

## 🎯 GENEL BAKIŞ

Klan sistemi **modüler yapı** ile implement edilmiş ve **çok sayıda özellik** içeriyor. Sistem genel olarak **çalışır durumda** ancak bazı özellikler **eksik** veya **tam implement edilmemiş**.

**Durum Özeti:**
- ✅ **Çalışan Özellikler:** 12/15
- ⚠️ **Kısmen Çalışan:** 1/15
- ❌ **Eksik/Çalışmayan:** 2/15

---

## 1. ✅ KLAN KURMA

### Durum: **ÇALIŞIYOR**

**Dosya:** `TerritoryListener.java` (satır 307-337)

**Nasıl Çalışıyor:**
1. Oyuncu **Klan Kristali** item'ını alır (`CRYSTAL` custom item)
2. Yere **çitlerle çevrili bir alan** oluşturur (minimum 3x3)
3. Çevrili alanın **merkezine kristali yerleştirir**
4. Kristal yerleştirildiğinde **chat'e klan ismi yazması istenir**
5. İsim yazıldığında:
   - Klan oluşturulur (`ClanManager.createClan()`)
   - Territory oluşturulur (50 blok radius)
   - Kristal entity olarak kaydedilir
   - Efektler gösterilir (şimşek, partikül, ses)

**Kod:**
```java
// TerritoryListener.java:307-337
Clan newClan = territoryManager.getClanManager().createClan(message, player.getUniqueId());
if (newClan != null) {
    newClan.setCrystalLocation(pending.crystalLoc);
    newClan.setCrystalEntity(pending.crystalEntity);
    Territory territory = new Territory(newClan.getId(), pending.crystalLoc);
    if (territory.getRadius() < 50) {
        territory.expand(50 - territory.getRadius());
    }
    newClan.setTerritory(territory);
    // ...
}
```

**Kontroller:**
- ✅ Lider zaten bir klana üye mi? → Engellenir
- ✅ Aynı isimde klan var mı? → Engellenir
- ✅ İsim validasyonu (boş, 32 karakter limiti)
- ✅ Çit kontrolü (Flood Fill algoritması)

**Sorunlar:**
- ❌ Yok

---

## 2. ✅ KLAN KRISTALİ YERİNİ TAŞIMA

### Durum: **ÇALIŞIYOR**

**Dosya:** `TerritoryListener.java` (satır 558-669)

**Nasıl Çalışıyor:**
1. Lider kristale **5 blok yakın** olmalı
2. **Shift + Sağ Tık** (boş el ile)
3. Yeni konum **çitlerle çevrili** olmalı
4. Async çit kontrolü yapılır (Flood Fill)
5. Kristal taşınır, Territory yeni konuma güncellenir

**Kod:**
```java
// TerritoryListener.java:560-669
@EventHandler(priority = EventPriority.HIGH)
public void onCrystalMove(PlayerInteractEvent event) {
    // Lider kontrolü
    if (playerClan.getRank(player.getUniqueId()) != Clan.Rank.LEADER) {
        return;
    }
    
    // Async çit kontrolü
    org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(...);
    
    // Kristali taşı
    finalCrystal.teleport(finalNewLoc);
    finalOwner.setCrystalLocation(finalNewLoc);
    // ...
}
```

**Kontroller:**
- ✅ Lider kontrolü
- ✅ Mesafe kontrolü (5 blok)
- ✅ Çit kontrolü (async)
- ✅ Yeni konum boş mu?

**Sorunlar:**
- ❌ Yok

---

## 3. ✅ KLAN SINIRLARINI PARTİKÜLLE GÖSTERME

### Durum: **ÇALIŞIYOR**

**Dosya:** `TerritoryListener.java` (satır 343-416)

**Nasıl Çalışıyor:**
1. Oyuncu hareket eder (`PlayerMoveEvent`)
2. Oyuncu **klan üyesi** mi kontrol edilir
3. Oyuncu **sınırın 10 blok yakınında** mı kontrol edilir
4. Sınır çizgisinde **yeşil partiküller** gösterilir
5. **Cooldown sistemi** var (1 saniye)

**Kod:**
```java
// TerritoryListener.java:343-416
@EventHandler(priority = EventPriority.MONITOR)
public void onPlayerMove(PlayerMoveEvent event) {
    // Cooldown kontrolü
    if (lastTime != null && (now - lastTime) < BOUNDARY_PARTICLE_COOLDOWN) {
        return;
    }
    
    // Sınırın 10 blok yakınındaysa partikül göster
    if (distanceToBoundary <= 10) {
        showTerritoryBoundary(player, territory, to);
    }
}
```

**Özellikler:**
- ✅ Sadece klan üyelerine görünür
- ✅ Yeşil partiküller (REDSTONE particle, yeşil renk)
- ✅ Cooldown (spam önleme)
- ✅ Performans optimizasyonu (blok değişikliği kontrolü)

**Sorunlar:**
- ❌ Yok

---

## 4. ✅ SAVAŞ AÇMA VE PES BAYRAĞI

### Durum: **ÇALIŞIYOR**

**Dosya:** `SiegeListener.java`

### 4.1. Savaş Açma

**Nasıl Çalışıyor:**
1. **General veya Lider** olmalı
2. **Beacon** (Kuşatma Anıtı) yerleştirir
3. Düşman bölgesinin **50 blok yakınında** olmalı
4. Klanın **%35'i aktif** olmalı
5. En az **1 General aktif** olmalı
6. Grace Period kontrolü (yeni klanlar 24 saat korunur)

**Kod:**
```java
// SiegeListener.java:34-143
@EventHandler
public void onSiegeAnitPlace(BlockPlaceEvent event) {
    // Yetki kontrolü
    if (rank != Clan.Rank.GENERAL && rank != Clan.Rank.LEADER) {
        return;
    }
    
    // Aktif üye kontrolü
    if (!checkActiveMembers(attacker, 0.35)) {
        return;
    }
    
    siegeManager.startSiege(attacker, defender, player);
}
```

**Kontroller:**
- ✅ Yetki kontrolü (General/Lider)
- ✅ Aktif üye kontrolü (%35)
- ✅ General aktif kontrolü
- ✅ Grace Period kontrolü
- ✅ Mesafe kontrolü (50 blok)
- ✅ Spam önleme (5 dakika cooldown)

### 4.2. Pes Bayrağı

**Nasıl Çalışıyor:**
1. **General veya Lider** olmalı
2. **Beyaz Bayrak** (White Banner) yerleştirir
3. Klan bölgesinde olmalı
4. **Shift + Sağ Tık** yapar
5. Klan pes eder, sandıkların yarısı gider

**Kod:**
```java
// SiegeListener.java:149-183
@EventHandler(priority = EventPriority.HIGH)
public void onWhiteFlagSurrender(PlayerInteractEvent event) {
    // Yetki kontrolü
    if (rank != Clan.Rank.GENERAL && rank != Clan.Rank.LEADER) {
        return;
    }
    
    siegeManager.surrender(clan, territoryManager.getClanManager());
}
```

**Sorunlar:**
- ❌ Yok

---

## 5. ✅ YETKİ SİSTEMİ

### Durum: **ÇALIŞIYOR**

**Dosya:** `ClanRankSystem.java`

**Rütbeler:**
- **LEADER** (5): Tüm yetkiler
- **ELITE** (4): Yapı inşa, Ritüel, Banka çekme (limitli), Görev başlatma
- **GENERAL** (3): Yapı inşa/yıkma, Üye ekleme/çıkarma, Savaş başlatma, Banka yönetimi, İttifak
- **MEMBER** (2): Sadece yapı kullanma
- **RECRUIT** (1): Hiçbir yetki

**Yetkiler:**
```java
// ClanRankSystem.java:33-45
public enum Permission {
    BUILD_STRUCTURE,      // Yapı inşa etme
    DESTROY_STRUCTURE,    // Yapı yıkma
    ADD_MEMBER,          // Üye ekleme
    REMOVE_MEMBER,       // Üye çıkarma
    START_WAR,           // Savaş başlatma
    MANAGE_BANK,         // Banka yönetimi
    WITHDRAW_BANK,       // Bankadan para çekme (limitli)
    MANAGE_ALLIANCE,     // İttifak yönetimi
    USE_RITUAL,          // Ritüel kullanma
    START_MISSION,       // Görev başlatma
    TRANSFER_LEADERSHIP  // Liderlik devretme
}
```

**Kullanım:**
```java
// ClanRankSystem.java:55-68
public boolean hasPermission(Clan clan, UUID playerId, Permission permission) {
    if (!clan.getMembers().containsKey(playerId)) {
        return false;
    }
    
    Clan.Rank rank = clan.getRank(playerId);
    Set<Permission> rankPermissions = getRankPermissions(rank);
    return rankPermissions.contains(permission);
}
```

**Sorunlar:**
- ❌ Yok

---

## 6. ✅ KLAN KORUMASI

### Durum: **ÇALIŞIYOR**

**Dosya:** `ClanSystemListener.java`, `GriefProtectionListener.java`, `ClanProtectionSystem.java`

### 6.1. Blok Kırma Koruması

**Nasıl Çalışıyor:**
1. Oyuncu blok kırmaya çalışır
2. Bölge sahibi kontrol edilir
3. Klan üyesi değilse → **Engellenir**
4. Savaş durumunda → **İzin verilir** (saldıran klan)

**Kod:**
```java
// TerritoryListener.java:59-150
@EventHandler
public void onBreak(BlockBreakEvent event) {
    Clan owner = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
    if (owner == null) return; // Boş arazi
    
    // Ölümsüz klan önleme: Kristal yoksa bölge koruması yok
    if (!owner.hasCrystal()) {
        return;
    }
    
    Clan playerClan = territoryManager.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
    
    // Kendi yerinse kırılabilir (Rütbe kontrolü dahil)
    if (playerClan != null && playerClan.equals(owner)) {
        // Recruit yapı kıramaz
        if (playerClan.getRank(event.getPlayer().getUniqueId()) == Clan.Rank.RECRUIT) {
            event.setCancelled(true);
            return;
        }
        return; // Yetkisi varsa kırabilir
    }
    
    // Misafir izni
    if (owner.isGuest(event.getPlayer().getUniqueId())) {
        return;
    }
    
    // Savaş kontrolü
    if (siegeManager.isUnderSiege(owner)) {
        Clan attacker = siegeManager.getAttacker(owner);
        if (attacker != null && attacker.equals(playerClan)) {
            return; // Savaşta saldıran klan kırabilir
        }
    }
    
    // Enerji kalkanı offline koruma
    // ...
    
    // Engelle
    event.setCancelled(true);
}
```

**Özellikler:**
- ✅ Kristal kontrolü (hasCrystal)
- ✅ Rütbe kontrolü (Recruit yapı kıramaz)
- ✅ Misafir izni
- ✅ Savaş istisnası
- ✅ Enerji kalkanı offline koruma

### 6.2. Sandık Açma Koruması

**Durum:** **EKSİK - EKLENMELİ**

**Not:** Sandık açma koruması için özel bir event handler **BULUNAMADI**.

**Mevcut Durum:**
- ❌ `InventoryOpenEvent` handler yok
- ❌ Sandık açma koruması yok
- ⚠️ Sadece blok kırma koruması var

**Sorun:**
- Klan dışı oyuncular sandıkları açabiliyor (koruma yok)
- Savaş durumunda da kontrol yok

**Öneri:**
- `InventoryOpenEvent` ile sandık açma koruması eklenmeli
- `TerritoryListener.java`'ya eklenebilir
- Aynı mantık: Klan üyesi değilse engelle, savaş durumunda izin ver

### 6.3. Diğer Korumalar

**GriefProtectionListener.java:**
- ✅ Piston koruması (farklı bölgeler arası hareket engellenir)
- ✅ Hopper hırsızlığı koruması
- ✅ Su/Lav akışı koruması
- ✅ TNT/Patlama koruması

### 6.3. Oyuncu Saldırı Koruması

**Nasıl Çalışıyor:**
1. **Güç bazlı koruma** (%40 eşik)
2. **Seviye bazlı koruma** (5 seviye farkı)
3. **Acemi koruması** (3,000 güç + Seviye 5 altı)
4. **Aktivite bazlı koruma** (7 gün offline)
5. **Klan içi koruma** (%50 eşik)
6. **Savaş istisnası** (en yüksek öncelik)

**Kod:**
```java
// ClanProtectionSystem.java:64-129
public boolean canAttackPlayer(Player attacker, Player target) {
    // 1. Klan savaşı kontrolü (en yüksek öncelik)
    if (isClanAtWar(attacker, target)) {
        return true;
    }
    
    // 2. Güç bazlı koruma
    if (!checkPowerProtection(attacker, attackerPower, targetPower)) {
        return false;
    }
    
    // 3. Seviye bazlı koruma
    if (!checkLevelProtection(attacker, attackerLevel, targetLevel)) {
        return false;
    }
    
    // ... diğer kontroller
}
```

**Sorunlar:**
- ❌ Yok

---

## 7. ✅ KERVAN SİSTEMİ

### Durum: **ÇALIŞIYOR**

**Dosya:** `CaravanManager.java`, `CaravanListener.java`

**Nasıl Çalışıyor:**
1. Oyuncu **kervan oluşturur** (CaravanMenu'dan)
2. **Minimum mesafe** kontrolü (1000 blok - config'den)
3. **Minimum yük** kontrolü (20 stack - config'den)
4. **Minimum değer** kontrolü (5000 altın - config'den)
5. **Mule** spawn edilir, eşyalar yüklenir
6. Hedefe ulaştığında **x1.5 değer** kazanır

**Kod:**
```java
// CaravanManager.java:35-110
public boolean createCaravan(Player owner, Location start, Location end,
                             List<ItemStack> cargo, double totalValue) {
    // Mesafe kontrolü
    if (distance < minDistance) {
        return false;
    }
    
    // Yük kontrolü
    if (totalItems < minItems) {
        return false;
    }
    
    // Mule oluştur
    Mule mule = start.getWorld().spawn(start, Mule.class);
    // ...
}
```

**Özellikler:**
- ✅ Anti-abuse kontrolleri
- ✅ Config'den ayarlanabilir
- ✅ Aynı dünya kontrolü
- ✅ Hedefe ulaşma kontrolü

**Sorunlar:**
- ❌ Yok

---

## 8. ⚠️ ÖZEL SANDIK SİSTEMİ

### Durum: **KISMEN ÇALIŞIYOR**

**Dosya:** `VirtualStorageListener.java`

**Mevcut Sistem:**
- **Sanal Bağlantı** (TELEPORTER yapısı) ile **şubeler arası paylaşılan depo**
- Ender Chest'e sağ tık → Sanal envanter açılır
- **Tüm klan üyeleri** erişebilir

**Kod:**
```java
// VirtualStorageListener.java:29-54
@EventHandler
public void onVirtualStorageAccess(PlayerInteractEvent event) {
    if (b.getType() != Material.ENDER_CHEST) return;
    
    // TELEPORTER yapısı var mı?
    Structure virtualLink = clan.getStructures().stream()
        .filter(s -> s.getType() == Structure.Type.TELEPORTER && 
                    s.getLocation().distance(b.getLocation()) <= 10)
        .findFirst().orElse(null);
    
    if (virtualLink != null) {
        Inventory virtualInv = getVirtualInventory(clan.getId());
        p.openInventory(virtualInv);
    }
}
```

**Sorun:**
- ❌ **Sadece oyuncunun kendi açabileceği özel sandık sistemi YOK**
- ⚠️ Şu anda sadece **şubeler arası paylaşılan depo** var
- ⚠️ Oyuncu bazlı özel sandık sistemi implement edilmemiş

**Öneri:**
- Oyuncu bazlı özel sandık sistemi eklenmeli
- Metadata ile sandık sahibi işaretlenmeli
- Sadece sahip açabilmeli

---

## 9. ✅ KLAN YAPI BUFFLARI

### Durum: **ÇALIŞIYOR**

**Dosya:** `StructureEffectTask.java`, `BuffTask.java`

**Yapılar ve Buffları:**

### 9.1. Simya Kulesi (ALCHEMY_TOWER)
- **Efekt:** Batarya güçlendirme
- **Menzil:** 15-25 blok (seviyeye göre)
- **Hedef:** Sadece klan üyeleri

### 9.2. Zehir Reaktörü (POISON_REACTOR)
- **Efekt:** Düşmanlara zehir
- **Menzil:** 20-30 blok (seviyeye göre)
- **Hedef:** Düşmanlar (klan üyesi değilse)

### 9.3. Şifa Kulesi (HEALING_BEACON)
- **Efekt:** Sürekli iyileştirme
- **Menzil:** 13-19 blok (seviyeye göre)
- **Hedef:** Sadece klan üyeleri

### 9.4. Gözetleme Kulesi (WATCHTOWER)
- **Efekt:** Düşman tespiti ve uyarı
- **Menzil:** 75-125 blok (seviyeye göre)
- **Hedef:** Klan üyelerine uyarı

**Kod:**
```java
// StructureEffectTask.java:305-314
private Collection<Player> getNearbyPlayersFromClan(Location loc, int radius, Clan clan) {
    return loc.getWorld().getNearbyEntities(loc, radius, radius, radius).stream()
        .filter(e -> e instanceof Player)
        .map(e -> (Player) e)
        .filter(p -> {
            Clan playerClan = clanManager.getClanByPlayer(p.getUniqueId());
            return playerClan != null && playerClan.equals(clan);
        })
        .toList();
}
```

**Kontrol:**
- ✅ Sadece klan üyelerine buff veriliyor
- ✅ Düşmanlara debuff veriliyor
- ✅ Seviye bazlı menzil ve güç

**Sorunlar:**
- ❌ Yok

---

## 10. ✅ KLAN GÖREVLERİ

### Durum: **ÇALIŞIYOR**

**Dosya:** `ClanMissionSystem.java`

**Nasıl Çalışıyor:**
1. **Lider veya General** görev oluşturur
2. Görev tipi seçilir:
   - `DEPOSIT_ITEM` - Kaynak yatırma
   - `BUILD_STRUCTURE` - Yapı inşası
   - `USE_RITUAL` - Ritüel yapma
3. Görev tahtasına (Lectern) kitap yerleştirilir
4. Üyeler görevi tamamlar
5. Tamamlandığında ödüller dağıtılır

**Kod:**
```java
// ClanMissionSystem.java:152-200
public boolean createMission(Player creator, MissionType type, int target, 
                            Material targetMaterial, String description) {
    // Yetki kontrolü
    if (!rankSystem.hasPermission(clan, creator.getUniqueId(), 
            ClanRankSystem.Permission.START_MISSION)) {
        return false;
    }
    
    // Görev oluştur
    ClanMission mission = new ClanMission();
    // ...
}
```

**Özellikler:**
- ✅ Yetki kontrolü
- ✅ Üye ilerleme takibi
- ✅ Toplam ilerleme takibi
- ✅ Ödül dağıtımı
- ✅ Görev iptal etme

**Sorunlar:**
- ❌ Yok

---

## 11. ✅ KLAN RİTÜELLERİ

### Durum: **ÇALIŞIYOR**

**Dosya:** `RitualInteractionListener.java`

**Ritüeller:**

### 11.1. Ateş Ritüeli (Üye Alma)
- **Gereksinim:** 3x3 Stripped Log platform
- **Yetki:** Lider veya General
- **Efekt:** Platform üzerindeki klansız oyuncular klan üyesi olur

### 11.2. Ayrılma Ritüeli
- **Gereksinim:** Named Paper (yemin kırma kağıdı)
- **Efekt:** Oyuncu klanından ayrılır

### 11.3. Terfi Ritüeli
- **Gereksinim:** 3x3 Stone Brick, 4 Redstone Torch, Külçe
- **Yetki:** Sadece Lider
- **Efekt:** Üye rütbesi yükseltilir

**Kod:**
```java
// RitualInteractionListener.java:68-110
@EventHandler(priority = EventPriority.HIGH)
public void onRecruitmentRitual(PlayerInteractEvent event) {
    // Yetki kontrolü
    if (!leaderId.equals(leader.getUniqueId()) && 
        !clan.isGeneral(leader.getUniqueId())) {
        return;
    }
    
    // 3x3 alan kontrolü
    if (!checkRitualStructure(centerBlock)) {
        return;
    }
    
    // Üye ekle
    // ...
}
```

**Sorunlar:**
- ❌ Yok

---

## 12. ✅ EĞİTİLEN CANLILAR

### Durum: **ÇALIŞIYOR**

**Dosya:** `TamingManager.java`, `TamingListener.java`

**Nasıl Çalışıyor:**
1. Canlı eğitilir (TamingManager)
2. Sahip kaydedilir
3. **Klan üyeleri** de kullanabilir (`canUseCreature()`)
4. Binme ve takip sistemi çalışır

**Kod:**
```java
// TamingManager.java:292-319
public boolean canUseCreature(LivingEntity entity, UUID playerId) {
    UUID ownerId = getOwner(entity);
    
    // Sahip mi?
    if (ownerId.equals(playerId)) {
        return true;
    }
    
    // Aynı klan mı?
    Clan ownerClan = clanManager.getClanByPlayer(ownerId);
    Clan playerClan = clanManager.getClanByPlayer(playerId);
    
    if (ownerClan != null && playerClan != null && 
        ownerClan.equals(playerClan)) {
        return true;
    }
    
    return false;
}
```

**Kullanım:**
```java
// TamingListener.java:278-304
@EventHandler(priority = EventPriority.HIGH)
public void onRideCreature(PlayerInteractEntityEvent event) {
    // Kullanabilir mi?
    if (!tamingManager.canUseCreature(entity, player.getUniqueId())) {
        player.sendMessage("§cBu canlıyı kullanamazsın! Sadece sahip veya klan üyeleri kullanabilir.");
        return;
    }
    
    // Binme veya takip
    // ...
}
```

**Sorunlar:**
- ❌ Yok

---

## 13. ❌ KLAN ALANI GENİŞLETME

### Durum: **ÇALIŞMIYOR (OTOMATIK GENİŞLETME YOK)**

**Dosya:** `TerritoryListener.java`, `Territory.java`

**Mevcut Sistem:**
- ✅ **Manuel genişletme:** Admin komutu ile (`/stratocraft clan territory <klan> expand <miktar>`)
- ✅ **Territory.expand()** metodu var
- ❌ **Otomatik genişletme:** Çitle çevirince otomatik genişleme **YOK**

**Kod:**
```java
// Territory.java:22
public void expand(int amount) { 
    this.radius += amount; 
}
```

**Admin Komutu:**
```java
// AdminCommandExecutor.java:5820-5835
case "expand":
    int amount = Integer.parseInt(args[3]);
    territory.expand(amount);
    territoryManager.setCacheDirty();
    p.sendMessage("§aKlan alanı " + amount + " blok genişletildi!");
```

**Sorun:**
- ❌ **Çitle çevirince otomatik genişletme sistemi YOK**
- ❌ Sadece admin komutu ile manuel genişletme var
- ❌ Oyuncular alanı genişletemiyor

**Öneri:**
- Çit kontrolü yapılıyor (`isSurroundedByClanFences`)
- Bu kontrolü kullanarak otomatik genişletme sistemi eklenebilir
- Lider kristali taşıdığında veya çit eklendiğinde alan genişleyebilir

---

## 14. 📊 DİĞER ÖZELLİKLER

### 14.1. Klan Bankası ✅
- **Dosya:** `ClanBankSystem.java`, `RitualInteractionListener.java`
- **Durum:** Çalışıyor
- **Özellikler:** Para yatırma/çekme, Item yatırma/çekme, Yetki kontrolü

### 14.2. Klan Aktivite Sistemi ✅
- **Dosya:** `ClanActivitySystem.java`
- **Durum:** Çalışıyor
- **Özellikler:** Üye aktivite takibi, Offline süre takibi

### 14.3. Klan Seviye Bonusu ✅
- **Dosya:** `ClanLevelBonusSystem.java`
- **Durum:** Çalışıyor
- **Özellikler:** Seviye bazlı bonuslar, Güç bonusu

### 14.4. Klan İttifak Sistemi ✅
- **Dosya:** `AllianceManager.java`
- **Durum:** Çalışıyor
- **Özellikler:** İttifak kurma, İttifak yönetimi

---

## 🚨 EKSİKLER VE SORUNLAR

### ❌ Kritik Eksikler

1. **Klan Alanı Otomatik Genişletme**
   - **Durum:** Çalışmıyor
   - **Açıklama:** Çitle çevirince otomatik genişleme yok
   - **Öncelik:** YÜKSEK
   - **Çözüm:** `TerritoryListener.java`'da çit kontrolü yapılıyor, bu kullanılarak otomatik genişletme eklenebilir

2. **Oyuncu Bazlı Özel Sandık**
   - **Durum:** Kısmen çalışıyor (sadece şubeler arası depo var)
   - **Açıklama:** Sadece oyuncunun kendi açabileceği özel sandık yok
   - **Öncelik:** ORTA
   - **Çözüm:** Metadata ile sandık sahibi işaretlenmeli, sadece sahip açabilmeli

### ⚠️ Potansiyel Sorunlar

1. **Performans**
   - Sınır partikül sistemi her hareket eden oyuncu için çalışıyor
   - Cooldown var ama büyük klanlarda performans sorunu olabilir

2. **Thread-Safety**
   - Bazı yerlerde `synchronized` kullanılıyor
   - `ConcurrentHashMap` kullanımı iyi ama bazı yerlerde `HashMap` kullanılıyor

---

## 📈 İSTATİSTİKLER

### Çalışan Özellikler: **12/15** (80.0%)
- ✅ Klan kurma
- ✅ Klan kristali taşıma
- ✅ Klan sınırları partikül
- ✅ Savaş açma
- ✅ Pes bayrağı
- ✅ Yetki sistemi
- ✅ Klan koruması
- ✅ Kervan sistemi
- ✅ Klan yapı buffları
- ✅ Klan görevleri
- ✅ Klan ritüelleri
- ✅ Eğitilen canlılar

### Kısmen Çalışan: **1/15** (6.7%)
- ⚠️ Özel sandık sistemi (sadece şubeler arası depo var)

### Çalışmayan: **2/15** (13.3%)
- ❌ Klan alanı otomatik genişletme
- ❌ Sandık açma koruması

---

## 🎯 SONUÇ

Klan sistemi **genel olarak çalışır durumda** ve **çok sayıda özellik** içeriyor. Ancak **2 önemli eksik** var:

1. **Klan alanı otomatik genişletme** - Çalışmıyor
2. **Oyuncu bazlı özel sandık** - Kısmen çalışıyor

**Öneriler:**
1. Otomatik genişletme sistemi eklenmeli (yüksek öncelik)
2. Oyuncu bazlı özel sandık sistemi eklenmeli (orta öncelik)
3. Performans optimizasyonları yapılmalı (düşük öncelik)

---

**Son Güncelleme:** 2024
**Durum:** ✅ %80.0 ÇALIŞIYOR

---

## 📝 ÖZET TABLO

| Özellik | Durum | Dosya | Notlar |
|---------|-------|-------|--------|
| Klan Kurma | ✅ Çalışıyor | TerritoryListener.java | Kristal + çit kontrolü |
| Klan Kristali Taşıma | ✅ Çalışıyor | TerritoryListener.java | Shift + sağ tık, çit kontrolü |
| Klan Sınırları Partikül | ✅ Çalışıyor | TerritoryListener.java | Yeşil partiküller, cooldown |
| Savaş Açma | ✅ Çalışıyor | SiegeListener.java | Beacon, yetki kontrolü |
| Pes Bayrağı | ✅ Çalışıyor | SiegeListener.java | White Banner, shift + sağ tık |
| Yetki Sistemi | ✅ Çalışıyor | ClanRankSystem.java | 5 rütbe, detaylı yetkiler |
| Blok Kırma Koruması | ✅ Çalışıyor | TerritoryListener.java | Rütbe kontrolü, savaş istisnası |
| Sandık Açma Koruması | ❌ Eksik | - | **EKLENMELİ** |
| Kervan Sistemi | ✅ Çalışıyor | CaravanManager.java | Anti-abuse kontrolleri |
| Özel Sandık | ⚠️ Kısmen | VirtualStorageListener.java | Sadece şubeler arası depo |
| Klan Yapı Buffları | ✅ Çalışıyor | StructureEffectTask.java | Sadece klan üyelerine |
| Klan Görevleri | ✅ Çalışıyor | ClanMissionSystem.java | Yetki kontrolü, ilerleme takibi |
| Klan Ritüelleri | ✅ Çalışıyor | RitualInteractionListener.java | Üye alma, ayrılma, terfi |
| Eğitilen Canlılar | ✅ Çalışıyor | TamingManager.java | Klan üyeleri kullanabilir |
| Klan Alanı Genişletme | ❌ Çalışmıyor | - | **EKLENMELİ** (otomatik) |

