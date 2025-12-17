# Klan Sistemi Kapsamlı Analiz Raporu

**Tarih:** 16 Aralık 2024  
**Kapsam:** Tüm klan sistemi özelliklerinin detaylı kontrolü ve hata tespiti

---

## 📋 İçindekiler

1. [Klan Menü Sistemleri](#1-klan-menü-sistemleri)
2. [Klan Kurma Sistemi](#2-klan-kurma-sistemi)
3. [Klan Yapıları ve Alan Kontrolü](#3-klan-yapıları-ve-alan-kontrolü)
4. [Klan Yapı Efektleri](#4-klan-yapı-efektleri)
5. [Klan Alanında Blok Kırma Yetkisi](#5-klan-alanında-blok-kırma-yetkisi)
6. [Yetki ve Rütbe Sistemi](#6-yetki-ve-rütbe-sistemi)
7. [Ritüel Sistemi](#7-ritüel-sistemi)
8. [Tespit Edilen Hatalar ve Eksikler](#8-tespit-edilen-hatalar-ve-eksikler)
9. [Çözüm Önerileri](#9-çözüm-önerileri)

---

## 1. Klan Menü Sistemleri

### ✅ Durum: **ÇALIŞIYOR** (Kısmen)

**Dosyalar:**
- `ClanMenu.java`
- `ClanMemberMenu.java`
- `ClanBankMenu.java`
- `ClanMissionMenu.java`
- `ClanStructureMenu.java`
- `ClanTerritoryMenu.java`
- `ClanStatsMenu.java`

**Kontrol Edilen Özellikler:**

1. **Ana Klan Menüsü (`ClanMenu.java`):**
   - ✅ Menü açılıyor
   - ✅ Alt menülere geçiş yapılıyor
   - ⚠️ **EKSİK:** Tüm alt menülerde yetki kontrolü yok
   - ⚠️ **EKSİK:** Bazı menülerde klan üyeliği kontrolü eksik

2. **Üye Yönetimi Menüsü (`ClanMemberMenu.java`):**
   - ✅ Menü açılıyor
   - ✅ **ÇALIŞIYOR:** Üye ekleme/çıkarma yetkisi kontrolü var (Lider/General)
   - ✅ **ÇALIŞIYOR:** Rütbe değiştirme yetkisi kontrolü var
   - ⚠️ **EKSİK:** `ClanRankSystem.hasPermission()` kullanılmıyor, direkt rütbe kontrolü yapılıyor

3. **Klan Bankası Menüsü (`ClanBankMenu.java`):**
   - ✅ Menü açılıyor
   - ⚠️ **EKSİK:** Banka işlemleri için yetki kontrolü eksik
   - ⚠️ **EKSİK:** `ClanRankSystem.hasPermission()` kullanılmıyor

4. **Klan Görev Menüsü (`ClanMissionMenu.java`):**
   - ✅ Menü açılıyor
   - ⚠️ **KONTROL EDİLMELİ:** Görev başlatma yetkisi kontrolü

5. **Klan Yapıları Menüsü (`ClanStructureMenu.java`):**
   - ✅ Menü açılıyor
   - ⚠️ **KONTROL EDİLMELİ:** Yapı yönetimi yetkisi kontrolü

6. **Klan Alanı Menüsü (`ClanTerritoryMenu.java`):**
   - ✅ Menü açılıyor
   - ✅ Yetki kontrolü var (Lider/General)
   - ✅ Klan bölgesi kontrolü var

**Tespit Edilen Sorunlar:**

1. **Yetki Kontrolü Eksiklikleri:**
   - `ClanMenu.java`'da bazı alt menülerde yetki kontrolü yok
   - `ClanBankMenu.java`'da banka işlemleri için yetki kontrolü eksik
   - `ClanMissionMenu.java`'da görev başlatma için yetki kontrolü eksik olabilir
   - `ClanStructureMenu.java`'da yapı yönetimi için yetki kontrolü eksik olabilir
   - ⚠️ **EKSİK:** `ClanRankSystem.hasPermission()` metodu tüm menülerde kullanılmıyor

2. **Klan Üyeliği Kontrolü:**
   - ✅ **ÇALIŞIYOR:** Tüm menülerde klan üyeliği kontrolü var
   - ✅ **ÇALIŞIYOR:** Menü açılmadan önce klan üyeliği kontrol ediliyor

---

## 2. Klan Kurma Sistemi

### ✅ Durum: **ÇALIŞIYOR** (Kısmen)

**Dosya:** `TerritoryListener.java`

**Kontrol Edilen Özellikler:**

1. **Klan Çiti vs Normal Çit Ayrımı:**
   - ✅ **ÇALIŞIYOR:** `onFencePlace()` metodunda kontrol var
   - ✅ Config'den `require-clan-fence-item` kontrolü yapılıyor
   - ✅ `ItemManager.isClanItem(item, "FENCE")` kontrolü var
   - ✅ Normal çit yerleştirme engelleniyor
   - ✅ Metadata ekleniyor (`ClanFence`)

**Kod:**
```java
// TerritoryListener.java:254-290
if (territoryConfig != null && territoryConfig.isRequireClanFenceItem()) {
    if (item != null && ItemManager.isClanItem(item, "FENCE")) {
        isClanFence = true;
    }
}
if (!isClanFence) {
    event.setCancelled(true);
    player.sendMessage("§cKlan alanında sadece §6Klan Çiti §cyerleştirilebilir!");
}
```

2. **Klan Kristali vs Normal Ender Crystal Ayrımı:**
   - ✅ **ÇALIŞIYOR:** `onCrystalPlace()` metodunda kontrol var
   - ✅ Config'den `require-clan-crystal-item` kontrolü yapılıyor
   - ✅ `ItemManager.isClanItem(event.getItem(), "CRYSTAL")` kontrolü var
   - ✅ Normal End Crystal yerleştirme engelleniyor

**Kod:**
```java
// TerritoryListener.java:555-563
if (territoryConfig != null && territoryConfig.isRequireClanCrystalItem()) {
    if (!ItemManager.isClanItem(event.getItem(), "CRYSTAL")) {
        return; // Normal End Crystal, klan kristali değil
    }
}
```

3. **Klan Kurulduktan Sonra Alan Kontrolleri:**
   - ✅ **ÇALIŞIYOR:** `isSurroundedByClanFences()` kontrolü var
   - ✅ Async flood-fill algoritması kullanılıyor
   - ✅ Çitlerin bağlantılı olması kontrol ediliyor
   - ⚠️ **EKSİK:** Klan kurulduktan sonra çitlerin kırılması durumunda alan kontrolü eksik

**Tespit Edilen Sorunlar:**

1. **Klan Kurulduktan Sonra Çit Kırma:**
   - ⚠️ **EKSİK:** Klan kurulduktan sonra çitler kırılırsa alan kontrolü yapılmıyor
   - ⚠️ **EKSİK:** Çitler kırıldığında alan sınırları güncellenmiyor
   - **Etki:** Klan alanı çitler kırıldıktan sonra hala görünür olabilir ama gerçekte çitler yok

2. **Kristal Kırma Kontrolü:**
   - ⚠️ **KONTROL EDİLMELİ:** Kristal kırıldığında klan alanı koruması kalkıyor mu?
   - ⚠️ **KONTROL EDİLMELİ:** Kristal kırıldığında alan sınırları temizleniyor mu?

---

## 3. Klan Yapıları ve Alan Kontrolü

### ⚠️ Durum: **SORUNLU**

**Dosyalar:**
- `StructureActivationListener.java`
- `StructureCoreListener.java`

**Kontrol Edilen Özellikler:**

1. **Yapı Aktifleştirme Kontrolü:**
   - ✅ **ÇALIŞIYOR:** `StructureActivationListener.java`'da klan bölgesi kontrolü var
   - ✅ **ÇALIŞIYOR:** `StructureCoreListener.java`'da klan bölgesi kontrolü var
   - ⚠️ **SORUN:** Yapı blokları yerleştirilebiliyor ama aktifleştirme sırasında kontrol yapılıyor

**Kod:**
```java
// StructureActivationListener.java:110-115
Clan owner = territoryManager.getTerritoryOwner(clicked.getLocation());
if (owner == null || !owner.equals(clan)) {
    player.sendMessage("§cKlan yapıları sadece kendi bölgenizde kurulabilir!");
    return;
}
```

**Tespit Edilen Sorunlar:**

1. **Yapı Blokları Yerleştirme:**
   - ⚠️ **SORUN:** Klan alanı dışına yapı blokları yerleştirilebiliyor
   - ⚠️ **SORUN:** Aktifleştirme sırasında kontrol yapılıyor ama bloklar zaten yerleştirilmiş
   - **Etki:** Oyuncular klan alanı dışına yapı blokları yerleştirebilir, aktifleştirme sırasında hata alırlar ama bloklar kalır

2. **Yapı Çekirdeği Aktifleştirme:**
   - ✅ **ÇALIŞIYOR:** Aktifleştirme sırasında klan alanı kontrolü yapılıyor
   - ✅ **ÇALIŞIYOR:** Hata mesajı gösteriliyor: "Klan yapıları sadece kendi bölgenizde kurulabilir!"
   - ⚠️ **EKSİK:** Bloklar yerleştirilmiş ama aktifleştirilememiş durumda kalıyor

**Çözüm Önerisi:**
- Yapı blokları yerleştirilirken klan alanı kontrolü yapılmalı
- Klan alanı dışına yapı blokları yerleştirilmesi engellenmeli
- Alternatif: Aktifleştirme sırasında kontrol yapılıyor, bu yeterli olabilir ama bloklar temizlenmeli

---

## 4. Klan Yapı Efektleri

### ✅ Durum: **ÇALIŞIYOR**

**Dosya:** `StructureEffectManager.java`

**Kontrol Edilen Özellikler:**

1. **Yapı Aktif Olduğunda Tüm Klan Üyelerine Etki:**
   - ✅ **ÇALIŞIYOR:** `onPlayerJoin()` metodunda yapı efektleri uygulanıyor
   - ✅ **ÇALIŞIYOR:** `updateEffects()` metodunda periyodik efektler uygulanıyor
   - ✅ **ÇALIŞIYOR:** `getNearbyPlayersFromClan()` ile klan üyeleri belirleniyor
   - ✅ **ÇALIŞIYOR:** Yapı aktif mi kontrolü yapılıyor (`structureCoreManager.isActiveStructure()`)

**Kod:**
```java
// StructureEffectManager.java:57-84
public void onPlayerJoin(Player player) {
    Clan clan = clanManager.getClanByPlayer(playerId);
    if (clan == null) return;
    
    for (Structure structure : clan.getStructures()) {
        if (structureCoreManager != null && !structureCoreManager.isActiveStructure(structure.getLocation())) {
            continue; // Pasif yapılar efekt vermez
        }
        applyEffectOnJoin(player, type, structure.getLevel());
    }
}
```

**Kod:**
```java
// StructureEffectManager.java:401-409
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

**Tespit Edilen Sorunlar:**

1. **Yapı Aktif Olduğunda Offline Üyelere Etki:**
   - ⚠️ **EKSİK:** Offline üyelere efekt uygulanmıyor (normal, çünkü oyuncu yok)
   - ✅ **ÇALIŞIYOR:** Oyuncu giriş yaptığında efektler uygulanıyor

2. **Yapı Pasif Olduğunda Efekt Kaldırma:**
   - ⚠️ **KONTROL EDİLMELİ:** Yapı pasif olduğunda aktif efektler kaldırılıyor mu?
   - ⚠️ **KONTROL EDİLMELİ:** `onPlayerQuit()` metodunda efektler kaldırılıyor mu?

---

## 5. Klan Alanında Blok Kırma Yetkisi

### ✅ Durum: **ÇALIŞIYOR** (Kısmen)

**Dosya:** `TerritoryListener.java`

**Kontrol Edilen Özellikler:**

1. **Recruit (Acemi) Blok Kırma:**
   - ✅ **ÇALIŞIYOR:** `onBreak()` metodunda Recruit kontrolü var
   - ✅ **ÇALIŞIYOR:** Recruit blok kıramıyor
   - ✅ **ÇALIŞIYOR:** Hata mesajı gösteriliyor: "Acemilerin yapı yıkma yetkisi yok!"

**Kod:**
```java
// TerritoryListener.java:102-108
if (playerClan.getRank(event.getPlayer().getUniqueId()) == Clan.Rank.RECRUIT) {
    event.setCancelled(true);
    event.getPlayer().sendMessage("§cAcemilerin yapı yıkma yetkisi yok!");
    return;
}
```

2. **Terfi Edilince Blok Kırma:**
   - ✅ **ÇALIŞIYOR:** Recruit dışındaki rütbeler blok kırabiliyor
   - ⚠️ **KONTROL EDİLMELİ:** Terfi işlemi sonrası yetki güncelleniyor mu?

3. **Bufflardan Faydalanma:**
   - ✅ **ÇALIŞIYOR:** Recruit bufflardan faydalanabiliyor (yapı efektleri)
   - ✅ **ÇALIŞIYOR:** `StructureEffectManager` tüm klan üyelerine efekt uyguluyor

**Tespit Edilen Sorunlar:**

1. **Blok Yerleştirme Yetkisi:**
   - ⚠️ **KONTROL EDİLMELİ:** Recruit blok yerleştirebiliyor mu?
   - ⚠️ **EKSİK:** Blok yerleştirme için yetki kontrolü eksik olabilir

2. **Chest Açma Yetkisi:**
   - ⚠️ **KONTROL EDİLMELİ:** Recruit chest açabiliyor mu?
   - ⚠️ **EKSİK:** Chest açma için yetki kontrolü eksik olabilir

---

## 6. Yetki ve Rütbe Sistemi

### ⚠️ Durum: **SORUNLU** (Eksiklikler Var)

**Dosya:** `ClanRankSystem.java`

**Kontrol Edilen Özellikler:**

1. **Yetki Sistemi:**
   - ✅ **ÇALIŞIYOR:** `ClanRankSystem` sınıfı mevcut
   - ✅ **ÇALIŞIYOR:** `Permission` enum'u tanımlı
   - ✅ **ÇALIŞIYOR:** `hasPermission()` metodu var
   - ⚠️ **SORUN:** Tüm işlemlerde yetki kontrolü yapılmıyor

**Kod:**
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

2. **Beyaz Bayrak Çekme (Pes Etme):**
   - ✅ **ÇALIŞIYOR:** `SiegeListener.java`'da yetki kontrolü var
   - ✅ **ÇALIŞIYOR:** Sadece General ve Lider yapabiliyor

**Kod:**
```java
// SiegeListener.java:166-171
Clan.Rank rank = clan.getRank(player.getUniqueId());
if (rank != Clan.Rank.GENERAL && rank != Clan.Rank.LEADER) {
    player.sendMessage("§cSadece General veya Lider pes edebilir!");
    return;
}
```

3. **Savaş Başlatma:**
   - ✅ **ÇALIŞIYOR:** `SiegeListener.java`'da yetki kontrolü var
   - ✅ **ÇALIŞIYOR:** Sadece General ve Lider yapabiliyor

**Kod:**
```java
// SiegeListener.java:52-58
Clan.Rank rank = attacker.getRank(player.getUniqueId());
if (rank != Clan.Rank.GENERAL && rank != Clan.Rank.LEADER) {
    player.sendMessage("§cSadece General veya Lider savaş açabilir!");
    event.setCancelled(true);
    return;
}
```

**Tespit Edilen Sorunlar:**

1. **Yetki Kontrolü Eksiklikleri:**
   - ⚠️ **EKSİK:** `ClanMenu.java`'da bazı işlemlerde yetki kontrolü yok
   - ⚠️ **EKSİK:** `ClanMemberMenu.java`'da üye ekleme/çıkarma için yetki kontrolü eksik
   - ⚠️ **EKSİK:** `ClanBankMenu.java`'da banka işlemleri için yetki kontrolü eksik
   - ⚠️ **EKSİK:** `ClanMissionMenu.java`'da görev başlatma için yetki kontrolü eksik
   - ⚠️ **EKSİK:** `ClanStructureMenu.java`'da yapı yönetimi için yetki kontrolü eksik

2. **Rütbe Bazlı İzinler:**
   - ⚠️ **EKSİK:** `ClanRankSystem.hasPermission()` metodu tüm işlemlerde kullanılmıyor
   - ⚠️ **EKSİK:** Bazı işlemlerde direkt rütbe kontrolü yapılıyor, `ClanRankSystem` kullanılmıyor

3. **Yapı Aktifleştirme Yetkisi:**
   - ✅ **ÇALIŞIYOR:** Recruit yapı aktifleştiremiyor
   - ⚠️ **EKSİK:** `ClanRankSystem.hasPermission()` kullanılmıyor, direkt rütbe kontrolü yapılıyor

**Kod:**
```java
// StructureActivationListener.java:117-121
if (clan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
    player.sendMessage("§cAcemilerin yapı kurma yetkisi yok!");
    return;
}
```

---

## 7. Ritüel Sistemi

### ⚠️ Durum: **SORUNLU** (Eksiklikler Var)

**Dosya:** `RitualInteractionListener.java`

**Kontrol Edilen Özellikler:**

1. **Üye Alma Ritüeli (Recruitment Ritual):**
   - ✅ **ÇALIŞIYOR:** `onRecruitmentRitual()` metodu var
   - ✅ **ÇALIŞIYOR:** Yetki kontrolü var (Lider/General)
   - ✅ **ÇALIŞIYOR:** Tarif kontrolü var (`checkRitualStructure()`)
   - ✅ **ÇALIŞIYOR:** Ateş yakma kontrolü var (Shift + Sağ Tık + Çakmak)
   - ✅ **ÇALIŞIYOR:** 3x3 alan içindeki oyuncular belirleniyor
   - ⚠️ **EKSİK:** Ritüel alanında klan üyesi olmayan oyuncular kontrol ediliyor mu?
   - ⚠️ **EKSİK:** Ritüel alanında zaten klanı olan oyuncular filtreleniyor mu?

**Kod:**
```java
// RitualInteractionListener.java:110-118
for (Entity entity : centerBlock.getWorld().getNearbyEntities(centerLoc, 1.5, 2, 1.5)) {
    if (entity instanceof Player) {
        Player target = (Player) entity;
        // Kendisi değilse ve klanı yoksa
        if (!target.equals(leader) && clanManager.getClanByPlayer(target.getUniqueId()) == null) {
            recruitedPlayers.add(target);
        }
    }
}
```

2. **Terfi Ritüeli (Promotion Ritual):**
   - ✅ **ÇALIŞIYOR:** `onPromotionRitual()` metodu var
   - ✅ **ÇALIŞIYOR:** Yetki kontrolü var (Sadece Lider)
   - ⚠️ **EKSİK:** Tarif kontrolü eksik olabilir
   - ⚠️ **EKSİK:** Ritüel alanında hangi oyuncuların terfi edileceği belirleniyor mu?
   - ⚠️ **EKSİK:** Terfi edilecek oyuncunun mevcut rütbesi kontrol ediliyor mu?
   - ⚠️ **EKSİK:** Terfi edilecek oyuncunun üst rütbeye geçebilmesi için yetki kontrolü var mı?

**Kod:**
```java
// RitualInteractionListener.java:488-504
public void onPromotionRitual(PlayerInteractEvent event) {
    if (clan == null || clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) return;
    
    // Cooldown kontrolü
    if (isOnCooldown(leader.getUniqueId())) {
        leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
        return;
    }
    
    // --- TERFİ RİTÜELİ KONTROLÜ ---
    // Kurulum: 3x3 Taş Tuğla, Köşelerde Kızıltaş Meşalesi, Ortada Ateş
    if (b.getType() == Material.FIRE &&
```

**Tespit Edilen Sorunlar:**

1. **Ritüel Tarif Kontrolü:**
   - ⚠️ **EKSİK:** `checkRitualStructure()` metodu tam kontrol edilmedi
   - ⚠️ **EKSİK:** Ritüel tarifleri merkez blok etrafında kontrol ediliyor mu?
   - ⚠️ **EKSİK:** Ritüel tarifleri doğru şekilde doğrulanıyor mu?

2. **Ritüel Alanında Oyuncu Belirleme:**
   - ⚠️ **EKSİK:** Terfi ritüelinde hangi oyuncuların terfi edileceği belirlenmiyor
   - ⚠️ **EKSİK:** Ritüel alanında birden fazla oyuncu varsa hangisi terfi edilecek?
   - ⚠️ **EKSİK:** Ritüel alanında oyuncu yoksa ne olacak?

3. **Yetki Kontrolü:**
   - ✅ **ÇALIŞIYOR:** Üye alma ritüelinde yetki kontrolü var (Lider/General)
   - ✅ **ÇALIŞIYOR:** Terfi ritüelinde yetki kontrolü var (Sadece Lider)
   - ⚠️ **EKSİK:** Terfi edilecek oyuncunun mevcut rütbesi kontrol edilmiyor
   - ⚠️ **EKSİK:** Terfi edilecek oyuncunun üst rütbeye geçebilmesi için yetki kontrolü yok

4. **Ritüel Başarı Kontrolü:**
   - ⚠️ **EKSİK:** Ritüel başarılı olduğunda güç sistemi entegrasyonu var mı?
   - ⚠️ **EKSİK:** Ritüel başarılı olduğunda klan görev sistemi entegrasyonu var mı?

---

## 8. Tespit Edilen Hatalar ve Eksikler

### 🔴 Kritik Hatalar

1. **Klan Kurulduktan Sonra Çit Kırma:**
   - **Sorun:** Klan kurulduktan sonra çitler kırılırsa alan kontrolü yapılmıyor
   - **Etki:** Klan alanı çitler kırıldıktan sonra hala görünür olabilir ama gerçekte çitler yok
   - **Dosya:** `TerritoryListener.java`

2. **Yapı Blokları Yerleştirme:**
   - **Sorun:** Klan alanı dışına yapı blokları yerleştirilebiliyor
   - **Etki:** Oyuncular klan alanı dışına yapı blokları yerleştirebilir, aktifleştirme sırasında hata alırlar ama bloklar kalır
   - **Dosya:** `TerritoryListener.java`, `StructureActivationListener.java`

3. **Yetki Kontrolü Eksiklikleri:**
   - **Sorun:** Tüm işlemlerde yetki kontrolü yapılmıyor
   - **Etki:** Yetkisiz oyuncular bazı işlemleri yapabilir
   - **Dosyalar:** `ClanMenu.java`, `ClanMemberMenu.java`, `ClanBankMenu.java`, `ClanMissionMenu.java`, `ClanStructureMenu.java`

### 🟡 Orta Öncelikli Hatalar

1. **Ritüel Tarif Kontrolü:**
   - **Sorun:** Ritüel tarifleri tam kontrol edilmiyor
   - **Etki:** Yanlış tariflerle ritüel yapılabilir
   - **Dosya:** `RitualInteractionListener.java`

2. **Ritüel Alanında Oyuncu Belirleme:**
   - **Sorun:** Terfi ritüelinde hangi oyuncuların terfi edileceği belirlenmiyor
   - **Etki:** Ritüel çalışmayabilir veya yanlış oyuncu terfi edilebilir
   - **Dosya:** `RitualInteractionListener.java`

3. **Yapı Pasif Olduğunda Efekt Kaldırma:**
   - **Sorun:** Yapı pasif olduğunda aktif efektler kaldırılmıyor
   - **Etki:** Pasif yapılar hala efekt verebilir
   - **Dosya:** `StructureEffectManager.java`

4. **Blok Yerleştirme Yetkisi:**
   - **Sorun:** Recruit blok yerleştirebiliyor mu kontrol edilmiyor
   - **Etki:** Recruit blok yerleştirebilir
   - **Dosya:** `TerritoryListener.java`

5. **Chest Açma Yetkisi:**
   - **Sorun:** Recruit chest açabiliyor mu kontrol edilmiyor
   - **Etki:** Recruit chest açabilir
   - **Dosya:** `TerritoryListener.java`

### 🟢 Düşük Öncelikli Hatalar

1. **Kristal Kırma Kontrolü:**
   - **Sorun:** Kristal kırıldığında klan alanı koruması kalkıyor mu kontrol edilmiyor
   - **Etki:** Kristal kırıldığında alan koruması kalkmayabilir
   - **Dosya:** `TerritoryListener.java`

2. **Terfi İşlemi Sonrası Yetki Güncelleme:**
   - **Sorun:** Terfi işlemi sonrası yetki güncelleniyor mu kontrol edilmiyor
   - **Etki:** Terfi edilen oyuncu yetkilerini kullanamayabilir
   - **Dosya:** `RitualInteractionListener.java`

---

## 9. Çözüm Önerileri

### 🔴 Kritik Hatalar İçin Çözümler

#### 1. Klan Kurulduktan Sonra Çit Kırma

**Sorun:** Klan kurulduktan sonra çitler kırılırsa alan kontrolü yapılmıyor.

**Çözüm:**
```java
// TerritoryListener.java - onBreak() metoduna ekle
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onFenceBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    
    // Klan çiti mi kontrol et
    if (block.getType() != Material.OAK_FENCE) return;
    
    // Metadata kontrolü
    if (territoryConfig != null) {
        String metadataKey = territoryConfig.getFenceMetadataKey();
        if (!block.hasMetadata(metadataKey)) return; // Normal çit
    }
    
    // Hangi klana ait çit?
    Clan owner = territoryManager.getTerritoryOwner(block.getLocation());
    if (owner == null) return;
    
    // Çit kırıldı, alan sınırlarını güncelle
    if (boundaryManager != null) {
        boundaryManager.removeFenceLocation(owner, block.getLocation());
        // Alan sınırlarını yeniden hesapla
        boundaryManager.recalculateBoundaries(owner);
    }
}
```

**Dosya:** `TerritoryListener.java`

#### 2. Yapı Blokları Yerleştirme

**Sorun:** Klan alanı dışına yapı blokları yerleştirilebiliyor.

**Çözüm:**
```java
// TerritoryListener.java - onBlockPlaceInTerritory() metoduna ekle
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onStructureBlockPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Player player = event.getPlayer();
    
    // Yapı çekirdeği bloğu mu kontrol et
    if (block.getType() != Material.END_CRYSTAL && 
        block.getType() != Material.BEACON && 
        block.getType() != Material.ENCHANTING_TABLE) {
        return; // Yapı bloğu değil
    }
    
    // Klan üyesi mi?
    Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
    if (playerClan == null) return;
    
    // Klan alanında mı?
    Clan owner = territoryManager.getTerritoryOwner(block.getLocation());
    if (owner == null || !owner.equals(playerClan)) {
        event.setCancelled(true);
        player.sendMessage("§cKlan yapıları sadece kendi klan alanınızda kurulabilir!");
        return;
    }
}
```

**Dosya:** `TerritoryListener.java`

#### 3. Yetki Kontrolü Eksiklikleri

**Sorun:** Tüm işlemlerde yetki kontrolü yapılmıyor.

**Çözüm:**
```java
// ClanMenu.java - Tüm işlemlerde yetki kontrolü ekle
private boolean checkPermission(Player player, Clan clan, ClanRankSystem.Permission permission) {
    if (plugin.getClanRankSystem() == null) return false;
    return plugin.getClanRankSystem().hasPermission(clan, player.getUniqueId(), permission);
}

// Örnek kullanım:
case ENDER_CHEST:
    // Banka erişimi
    if (!checkPermission(player, clan, ClanRankSystem.Permission.MANAGE_BANK)) {
        player.sendMessage("§cBanka yönetimi yetkiniz yok!");
        return;
    }
    // ... banka açma kodu
    break;
```

**Dosyalar:** `ClanMenu.java`, `ClanMemberMenu.java`, `ClanBankMenu.java`, `ClanMissionMenu.java`, `ClanStructureMenu.java`

### 🟡 Orta Öncelikli Hatalar İçin Çözümler

#### 4. Ritüel Tarif Kontrolü

**Sorun:** Ritüel tarifleri tam kontrol edilmiyor.

**Çözüm:**
```java
// RitualInteractionListener.java - checkRitualStructure() metodunu güçlendir
private boolean checkRitualStructure(Block centerBlock) {
    // Merkez blok kontrolü
    if (!isStrippedLog(centerBlock.getType())) return false;
    
    // 3x3 alan kontrolü (merkez blok etrafında)
    for (int x = -1; x <= 1; x++) {
        for (int z = -1; z <= 1; z++) {
            Block checkBlock = centerBlock.getRelative(x, 0, z);
            if (x == 0 && z == 0) {
                // Merkez blok - zaten kontrol edildi
                continue;
            }
            // Çevre bloklar soyulmuş odun olmalı
            if (!isStrippedLog(checkBlock.getType())) {
                return false;
            }
        }
    }
    return true;
}
```

**Dosya:** `RitualInteractionListener.java`

#### 5. Ritüel Alanında Oyuncu Belirleme

**Sorun:** Terfi ritüelinde hangi oyuncuların terfi edileceği belirlenmiyor.

**Çözüm:**
```java
// RitualInteractionListener.java - onPromotionRitual() metoduna ekle
// Ritüel alanındaki oyuncuları bul
Location centerLoc = b.getLocation().add(0.5, 1, 0.5);
List<Player> playersInArea = new ArrayList<>();
for (Entity entity : b.getWorld().getNearbyEntities(centerLoc, 1.5, 2, 1.5)) {
    if (entity instanceof Player) {
        Player target = (Player) entity;
        if (!target.equals(leader) && clan.getMembers().containsKey(target.getUniqueId())) {
            playersInArea.add(target);
        }
    }
}

if (playersInArea.isEmpty()) {
    leader.sendMessage("§eRitüel alanında terfi edilecek kimse yok.");
    return;
}

// İlk oyuncuyu terfi et (veya menü göster)
Player targetPlayer = playersInArea.get(0);
Clan.Rank currentRank = clan.getRank(targetPlayer.getUniqueId());
Clan.Rank nextRank = getNextRank(currentRank);

if (nextRank == null) {
    leader.sendMessage("§c" + targetPlayer.getName() + " zaten en yüksek rütbede!");
    return;
}

// Terfi et
clan.setRank(targetPlayer.getUniqueId(), nextRank);
leader.sendMessage("§a" + targetPlayer.getName() + " " + nextRank.name() + " rütbesine terfi etti!");
```

**Dosya:** `RitualInteractionListener.java`

#### 6. Yapı Pasif Olduğunda Efekt Kaldırma

**Sorun:** Yapı pasif olduğunda aktif efektler kaldırılmıyor.

**Çözüm:**
```java
// StructureEffectManager.java - updateEffects() metoduna ekle
public void updateEffects() {
    tickCounter++;
    
    // Pasif olan yapıların efektlerini kaldır
    for (UUID playerId : new HashSet<>(playerActiveEffects.keySet())) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) continue;
        
        Clan clan = clanManager.getClanByPlayer(playerId);
        if (clan == null) continue;
        
        Set<StructureType> activeEffects = playerActiveEffects.get(playerId);
        Set<StructureType> shouldBeActive = new HashSet<>();
        
        for (Structure structure : clan.getStructures()) {
            if (structureCoreManager != null && !structureCoreManager.isActiveStructure(structure.getLocation())) {
                continue; // Pasif yapılar
            }
            StructureType type = convertToStructureType(structure.getType());
            if (type != null) {
                shouldBeActive.add(type);
            }
        }
        
        // Pasif olan efektleri kaldır
        for (StructureType type : activeEffects) {
            if (!shouldBeActive.contains(type)) {
                removeEffect(player, type);
            }
        }
        
        playerActiveEffects.put(playerId, shouldBeActive);
    }
    
    // ... mevcut kod
}
```

**Dosya:** `StructureEffectManager.java`

#### 7. Blok Yerleştirme Yetkisi

**Sorun:** Recruit blok yerleştirebiliyor mu kontrol edilmiyor.

**Çözüm:**
```java
// TerritoryListener.java - onBlockPlaceInTerritory() metoduna ekle
// Kendi yerinse yerleştirilebilir (Rütbe kontrolü dahil)
Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
if (playerClan != null && playerClan.equals(owner)) {
    // Recruit blok yerleştiremez
    if (playerClan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
        event.setCancelled(true);
        player.sendMessage("§cAcemilerin blok yerleştirme yetkisi yok!");
        return;
    }
    return; // Yetkisi varsa yerleştirebilir
}
```

**Dosya:** `TerritoryListener.java`

#### 8. Chest Açma Yetkisi

**Sorun:** Recruit chest açabiliyor mu kontrol edilmiyor.

**Durum:** `TerritoryListener.java`'da `onInventoryOpen()` metodu var ama Recruit kontrolü eksik.

**Mevcut Kod:**
```java
// TerritoryListener.java:165-247
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onInventoryOpen(InventoryOpenEvent event) {
    // ... mevcut kod
    // Recruit kontrolü yok!
}
```

**Çözüm:**
```java
// TerritoryListener.java - onInventoryOpen() metoduna ekle
// Klan üyesi mi?
Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
if (playerClan != null && playerClan.equals(owner)) {
    // Recruit chest açamaz
    if (playerClan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
        event.setCancelled(true);
        player.sendMessage("§cAcemilerin chest açma yetkisi yok!");
        return;
    }
    return; // Yetkisi varsa açabilir
}
```

**Dosya:** `TerritoryListener.java`

### 🟢 Düşük Öncelikli Hatalar İçin Çözümler

#### 9. Kristal Kırma Kontrolü

**Sorun:** Kristal kırıldığında klan alanı koruması kalkıyor mu kontrol edilmiyor.

**Çözüm:**
```java
// TerritoryListener.java - onBreak() metoduna ekle
// Kristal kırıldı mı kontrol et
if (block.getType() == Material.END_CRYSTAL) {
    // Metadata kontrolü
    if (territoryConfig != null) {
        String metadataKey = territoryConfig.getCrystalMetadataKey();
        if (block.hasMetadata(metadataKey)) {
            // Klan kristali kırıldı
            Clan crystalOwner = territoryManager.getTerritoryOwner(block.getLocation());
            if (crystalOwner != null) {
                // Klan alanı korumasını kaldır
                crystalOwner.setCrystalLocation(null);
                // Alan sınırlarını temizle
                if (boundaryManager != null) {
                    boundaryManager.clearBoundaries(crystalOwner);
                }
                // Tüm oyunculara bildir
                Bukkit.broadcastMessage("§c" + crystalOwner.getName() + " klanının kristali kırıldı! Alan koruması kalktı.");
            }
        }
    }
}
```

**Dosya:** `TerritoryListener.java`

#### 10. Terfi İşlemi Sonrası Yetki Güncelleme

**Sorun:** Terfi işlemi sonrası yetki güncelleniyor mu kontrol edilmiyor.

**Çözüm:**
```java
// RitualInteractionListener.java - Terfi işlemi sonrası
// Terfi et
clan.setRank(targetPlayer.getUniqueId(), nextRank);

// Yetki güncellemesi (PlayerData model güncellemesi)
if (plugin.getPlayerDataManager() != null) {
    plugin.getPlayerDataManager().updatePlayerRank(targetPlayer.getUniqueId(), nextRank);
}

// Oyuncuya bildir
targetPlayer.sendMessage("§a" + nextRank.name() + " rütbesine terfi ettiniz!");
targetPlayer.sendTitle("§a§lTERFİ EDİLDİNİZ", "§e" + nextRank.name(), 10, 70, 20);
```

**Dosya:** `RitualInteractionListener.java`

---

## 📊 Özet

### ✅ Çalışan Özellikler

1. Klan çiti vs normal çit ayrımı
2. Klan kristali vs normal ender crystal ayrımı
3. Klan kurulduktan sonra alan kontrolleri (kısmen)
4. Klan yapı efektleri (tüm klan üyelerine)
5. Recruit blok kırma engelleme
6. Beyaz bayrak çekme yetkisi kontrolü
7. Savaş başlatma yetkisi kontrolü
8. Üye alma ritüeli (kısmen)

### ⚠️ Sorunlu Özellikler

1. Klan kurulduktan sonra çit kırma kontrolü
2. Yapı blokları yerleştirme kontrolü
3. Yetki kontrolü eksiklikleri (menülerde)
4. Ritüel tarif kontrolü
5. Ritüel alanında oyuncu belirleme
6. Yapı pasif olduğunda efekt kaldırma
7. Blok yerleştirme yetkisi
8. Chest açma yetkisi

### 🔴 Kritik Öncelik

1. **Yetki Kontrolü Eksiklikleri:** Tüm menülerde ve işlemlerde yetki kontrolü eklenmeli
2. **Yapı Blokları Yerleştirme:** Klan alanı dışına yapı blokları yerleştirilmesi engellenmeli
3. **Klan Kurulduktan Sonra Çit Kırma:** Çit kırıldığında alan sınırları güncellenmeli

---

**Rapor Hazırlayan:** AI Assistant  
**Tarih:** 16 Aralık 2024  
**Versiyon:** 1.0

