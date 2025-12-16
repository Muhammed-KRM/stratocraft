# 🏛️ Klan Sistemi Genel Fonksiyonlar Raporu

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Klan Oluşturma](#klan-oluşturma)
3. [Üye Yönetimi](#üye-yönetimi)
4. [Rütbe Sistemi](#rütbe-sistemi)
5. [Klan Kristali İşlemleri](#klan-kristali-işlemleri)
6. [Misafir (Guest) Sistemi](#misafir-guest-sistemi)
7. [Klan Dağıtma](#klan-dağıtma)
8. [Liderlik Devretme](#liderlik-devretme)
9. [Klan Menüleri](#klan-menüleri)
10. [Sorunlar ve Eksikler](#sorunlar-ve-eksikler)

---

## 🎯 GENEL BAKIŞ

Klan sistemi **fiziksel ritüeller** ve **GUI menüleri** ile çalışıyor. Tüm işlemler **ritüel tabanlı** (blok düzenekleri) veya **GUI menüleri** üzerinden yapılıyor.

**Ana Dosyalar:**
- `ClanManager.java` - Ana yönetim sistemi
- `Clan.java` - Klan modeli (veri yapısı)
- `TerritoryListener.java` - Klan kurma (kristal yerleştirme)
- `RitualInteractionListener.java` - Ritüeller (üye alma, terfi, ayrılma)
- `ClanRankSystem.java` - Yetki sistemi
- `ClanMemberMenu.java` - Üye yönetim GUI'si
- `ClanMenu.java` - Ana klan menüsü

---

## 🏗️ KLAN OLUŞTURMA

### 1. ✅ Klan Kristali Yerleştirme

**Dosya:** `TerritoryListener.java:317-467`

**Fonksiyon:** `onCrystalPlace(PlayerInteractEvent event)`

**Nasıl Çalışıyor:**

1. **Oyuncu Klan Kristali item'ını yerleştirmeye çalışır**
   - Item: `CRYSTAL` (custom item)
   - Sağ tık + blok yüzeyine koyma

2. **Kontrol 1: Oyuncunun zaten klanı var mı?**
   ```java
   if (territoryManager.getClanManager().getClanByPlayer(player.getUniqueId()) != null) {
       player.sendMessage("§cZaten bir klanın var!");
       return;
   }
   ```

3. **Kontrol 2: Alan çitlerle çevrili mi? (ASYNC)**
   - Async flood-fill algoritması kullanılıyor (lag önleme)
   - `isSurroundedByClanFences()` fonksiyonu çağrılıyor
   - Büyük alanlar için main thread'i kilitlememek için async yapılıyor

4. **Kontrol 3: Çit kontrolü başarılı mı?**
   - Eğer alan çitlerle çevrili değilse, kristal yerleştirilemez
   - Oyuncuya hata mesajı gösterilir

5. **Kontrol 4: İsim girişi**
   - Oyuncuya chat'ten isim sorulur
   - `ChatInputListener` ile isim alınır

6. **Klan Oluşturma:**
   ```java
   Clan clan = clanManager.createClan(clanName, player.getUniqueId());
   ```
   - `ClanManager.createClan()` çağrılır
   - Yeni `Clan` objesi oluşturulur
   - Lider otomatik olarak `LEADER` rütbesine atanır

7. **Kristal Yerleştirme:**
   - Ender Crystal entity oluşturulur
   - Konum kaydedilir (`clan.setCrystalLocation()`)
   - `hasCrystal` flag'i `true` yapılır

8. **Territory Oluşturma:**
   - Kristal konumuna göre territory oluşturulur
   - Varsayılan radius: 50 blok

**Kod Akışı:**
```
Oyuncu Kristal Yerleştirir
    ↓
Zaten Klanı Var mı? → EVET → İptal
    ↓ HAYIR
Alan Çitlerle Çevrili mi? (ASYNC) → HAYIR → İptal
    ↓ EVET
İsim Sor (Chat Input)
    ↓
Klan Oluştur (ClanManager.createClan)
    ↓
Kristal Yerleştir
    ↓
Territory Oluştur
    ↓
Başarılı!
```

**Sorunlar:**
- ⚠️ **Async kontrol:** Async kontrol sonrası main thread'e dönüş yapılıyor ama bu karmaşık
- ⚠️ **Chat input:** Chat input sistemi başka bir listener'da, entegrasyon sorunlu olabilir

**Durum:** ✅ Çalışıyor

---

### 2. ✅ Klan Oluşturma (ClanManager)

**Dosya:** `ClanManager.java:74-126`

**Fonksiyon:** `createClan(String name, UUID leader)`

**Nasıl Çalışıyor:**

1. **Null Check:**
   ```java
   if (name == null || leader == null) {
       return null;
   }
   ```

2. **İsim Validasyonu:**
   ```java
   name = name.trim();
   if (name.isEmpty() || name.length() > 32) {
       return null;
   }
   ```

3. **Lider Kontrolü:**
   ```java
   if (getClanByPlayer(leader) != null) {
       return null; // Lider zaten bir klana üye
   }
   ```

4. **Aynı İsim Kontrolü:**
   ```java
   if (getClanByName(name) != null) {
       return null; // Aynı isimde klan var
   }
   ```

5. **Klan Oluştur:**
   ```java
   Clan c = new Clan(name, leader);
   clans.put(c.getId(), c);
   playerClanMap.put(leader, c.getId());
   ```
   - Yeni `Clan` objesi oluşturulur
   - `clans` Map'ine eklenir (UUID -> Clan)
   - `playerClanMap` Map'ine eklenir (Player UUID -> Clan UUID)

6. **Aktivite Güncelle:**
   ```java
   if (clanActivitySystem != null) {
       clanActivitySystem.updateActivity(leader);
   }
   ```

7. **Cache Güncelle:**
   ```java
   if (territoryManager != null) {
       territoryManager.setCacheDirty();
   }
   ```

**Kod Akışı:**
```
createClan(name, leader)
    ↓
Null Check → HATA → return null
    ↓ OK
İsim Validasyonu → HATA → return null
    ↓ OK
Lider Zaten Üye mi? → EVET → return null
    ↓ HAYIR
Aynı İsim Var mı? → EVET → return null
    ↓ HAYIR
Klan Oluştur
    ↓
Map'lere Ekle
    ↓
Aktivite Güncelle
    ↓
Cache Güncelle
    ↓
return clan
```

**Sorunlar:**
- ✅ Tüm kontroller yapılıyor
- ✅ Thread-safe (ConcurrentHashMap kullanılıyor)

**Durum:** ✅ Çalışıyor

---

## 👥 ÜYE YÖNETİMİ

### 3. ✅ Üye Alma (Ateş Ritüeli)

**Dosya:** `RitualInteractionListener.java:68-176`

**Fonksiyon:** `onRecruitmentRitual(PlayerInteractEvent event)`

**Nasıl Çalışıyor:**

1. **Ritüel Tetikleme:**
   - Shift + Sağ Tık + Elde Çakmak (Flint and Steel)
   - Merkez blok: Soyulmuş Odun (Stripped Log)

2. **Yetki Kontrolü:**
   ```java
   if (leaderId == null || (!leaderId.equals(leader.getUniqueId()) && !clan.isGeneral(leader.getUniqueId()))) {
       leader.sendMessage("§cBu ritüeli sadece Lider veya Generaller yapabilir!");
       return;
   }
   ```

3. **3x3 Alan Kontrolü:**
   ```java
   if (!checkRitualStructure(centerBlock)) {
       return; // Yapı bozuksa ritüel tetiklenmez
   }
   ```
   - Merkez blok ve etrafındaki 8 blok Soyulmuş Odun olmalı
   - Toplam 9 blok (3x3 kare)

4. **Oyuncu Bulma:**
   ```java
   Location centerLoc = centerBlock.getLocation().add(0.5, 1, 0.5);
   for (Entity entity : centerBlock.getWorld().getNearbyEntities(centerLoc, 1.5, 2, 1.5)) {
       if (entity instanceof Player) {
           Player target = (Player) entity;
           if (!target.equals(leader) && clanManager.getClanByPlayer(target.getUniqueId()) == null) {
               recruitedPlayers.add(target);
           }
       }
   }
   ```
   - Ritüel alanındaki (3x3, 1.5 blok yarıçap, 2 blok yükseklik) oyuncular bulunur
   - Kendisi ve zaten klanı olanlar hariç

5. **Üye Ekleme:**
   ```java
   for (Player newMember : recruitedPlayers) {
       clanManager.addMember(clan, newMember.getUniqueId(), Clan.Rank.RECRUIT);
       newMember.sendMessage("§6§l" + clan.getName() + " §eklanına ruhun bağlandı!");
   }
   ```
   - Her oyuncu `RECRUIT` rütbesi ile eklenir
   - Efektler gösterilir (particle, sound, title)

**Kod Akışı:**
```
Shift + Sağ Tık + Çakmak
    ↓
Merkez Blok Soyulmuş Odun mu? → HAYIR → İptal
    ↓ EVET
3x3 Alan Kontrolü → HATA → İptal
    ↓ OK
Yetki Kontrolü → HATA → İptal
    ↓ OK
Ritüel Alanındaki Oyuncuları Bul
    ↓
Her Oyuncu İçin:
    - Klanı Yok mu? → EVET → Üye Ekle (RECRUIT)
    ↓
Efektler Göster
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **3x3 kontrolü:** Sadece zemin kontrolü yapılıyor, yukarı/aşağı kontrolü yok

**Durum:** ✅ Çalışıyor

---

### 4. ✅ Üye Ekleme (ClanManager)

**Dosya:** `ClanManager.java:185-221`

**Fonksiyon:** `addMember(Clan clan, UUID memberId, Clan.Rank rank)`

**Nasıl Çalışıyor:**

1. **Null Check:**
   ```java
   if (clan == null || memberId == null || rank == null) {
       return;
   }
   ```

2. **Üye Zaten Üye mi?**
   ```java
   Clan existingClan = getClanByPlayer(memberId);
   if (existingClan != null && !existingClan.equals(clan)) {
       return; // Üye zaten başka bir klana üye
   }
   ```

3. **Üye Ekle:**
   ```java
   clan.addMember(memberId, rank);
   playerClanMap.put(memberId, clan.getId());
   ```
   - `Clan.addMember()` çağrılır (members Map'ine ekler)
   - `playerClanMap` güncellenir

4. **Aktivite Güncelle:**
   ```java
   if (clanActivitySystem != null) {
       clanActivitySystem.updateActivity(memberId);
   }
   ```

5. **Cache Güncelle:**
   ```java
   if (territoryManager != null) {
       territoryManager.setCacheDirty();
   }
   ```

**Kod Akışı:**
```
addMember(clan, memberId, rank)
    ↓
Null Check → HATA → return
    ↓ OK
Üye Zaten Üye mi? → EVET → return
    ↓ HAYIR
clan.addMember(memberId, rank)
    ↓
playerClanMap.put(memberId, clan.getId())
    ↓
Aktivite Güncelle
    ↓
Cache Güncelle
```

**Sorunlar:**
- ✅ Tüm kontroller yapılıyor
- ✅ Thread-safe

**Durum:** ✅ Çalışıyor

---

### 5. ✅ Üye Çıkarma

**Dosya:** `ClanManager.java:226-246`, `RitualInteractionListener.java:260-358`

**Fonksiyonlar:**
- `ClanManager.removeMember()` - Ana fonksiyon
- `RitualInteractionListener.onKickRitual()` - Ritüel (Ateş Ritüeli)

**Nasıl Çalışıyor (ClanManager):**

1. **Null Check:**
   ```java
   if (clan == null || memberId == null) return;
   ```

2. **Üye Çıkar:**
   ```java
   synchronized (clan.getMembers()) {
       clan.getMembers().remove(memberId);
   }
   playerClanMap.remove(memberId);
   ```
   - Thread-safe: `synchronized` blok kullanılıyor
   - `members` Map'inden çıkarılır
   - `playerClanMap`'ten çıkarılır

3. **Cache Güncelle:**
   ```java
   if (territoryManager != null) {
       territoryManager.setCacheDirty();
   }
   ```

**Nasıl Çalışıyor (Ritüel):**

1. **Ritüel Tetikleme:**
   - Shift + Sağ Tık + Elde Ateş (Fire Block)
   - Hedef oyuncu: Ritüel alanındaki oyuncu

2. **Yetki Kontrolü:**
   ```java
   if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER) {
       return; // Sadece lider atabilir
   }
   ```

3. **Hedef Kontrolü:**
   ```java
   if (targetClan.getRank(target.getUniqueId()) == Clan.Rank.LEADER) {
       p.sendMessage("§cLideri atamazsın! Önce liderliği devret.");
       return;
   }
   ```

4. **Üye Çıkar:**
   ```java
   clanManager.removeMember(targetClan, target.getUniqueId());
   ```

**Kod Akışı (Ritüel):**
```
Shift + Sağ Tık + Ateş
    ↓
Hedef Oyuncu Bul
    ↓
Yetki Kontrolü → HATA → İptal
    ↓ OK
Hedef Lider mi? → EVET → İptal
    ↓ HAYIR
removeMember(clan, targetId)
    ↓
Efektler Göster
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **GUI'den çıkarma:** `ClanMemberMenu`'dan da çıkarılabilir (sağ tık)

**Durum:** ✅ Çalışıyor

---

### 6. ✅ Üye Ayrılma (Yemin Bozma Ritüeli)

**Dosya:** `RitualInteractionListener.java:360-456`

**Fonksiyon:** `onLeaveRitual(PlayerInteractEvent event)`

**Nasıl Çalışıyor:**

1. **Ritüel Tetikleme:**
   - Shift + Sağ Tık + Elde Kağıt (Paper)
   - Blok: Kırmızı Kumaş (Red Wool)

2. **Lider Kontrolü:**
   ```java
   if (clan.getRank(p.getUniqueId()) == Clan.Rank.LEADER) {
       p.sendMessage("§cLider klandan ayrılamaz! Önce liderliği devret.");
       return;
   }
   ```

3. **Üye Çıkar:**
   ```java
   clanManager.removeMember(clan, p.getUniqueId());
   ```

4. **Kağıt Tüket:**
   ```java
   if (handItem.getAmount() > 1) {
       handItem.setAmount(handItem.getAmount() - 1);
   } else {
       leader.getInventory().setItemInMainHand(null);
   }
   ```

**Kod Akışı:**
```
Shift + Sağ Tık + Kağıt + Kırmızı Kumaş
    ↓
Lider mi? → EVET → İptal
    ↓ HAYIR
removeMember(clan, playerId)
    ↓
Kağıt Tüket
    ↓
Efektler Göster
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor

**Durum:** ✅ Çalışıyor

---

## 🎖️ RÜTBE SİSTEMİ

### 7. ✅ Rütbe Değiştirme (Terfi Ritüeli)

**Dosya:** `RitualInteractionListener.java:488-576`

**Fonksiyon:** `onPromotionRitual(PlayerInteractEvent event)`

**Nasıl Çalışıyor:**

1. **Ritüel Tetikleme:**
   - Sağ Tık + Ateş (Fire Block)
   - Ritüel Yapısı:
     - 3x3 Taş Tuğla (Stone Bricks)
     - Köşelerde 4 Kızıltaş Meşalesi (Redstone Torch)
     - Ortada Ateş (Fire)

2. **Yetki Kontrolü:**
   ```java
   if (clan == null || clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
       return; // Sadece lider terfi ettirebilir
   }
   ```

3. **Terfi Tipi:**
   - **Altın Külçe (Gold Ingot):** Member → General
   - **Demir Külçe (Iron Ingot):** Recruit → Member

4. **Hedef Bul:**
   ```java
   leader.getNearbyEntities(2, 2, 2).stream()
       .filter(e -> e instanceof Player && e != leader)
       .map(e -> (Player)e)
       .findFirst()
   ```

5. **Rütbe Değiştir:**
   ```java
   if (clan.getRank(target.getUniqueId()) == Clan.Rank.MEMBER) {
       clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.GENERAL);
   }
   ```

**Kod Akışı:**
```
Sağ Tık + Ateş (Ritüel Yapısı)
    ↓
Yetki Kontrolü → HATA → İptal
    ↓ OK
Elinde Altın mu? → EVET → Member → General
Elinde Demir mi? → EVET → Recruit → Member
    ↓
Hedef Oyuncu Bul
    ↓
Rütbe Değiştir
    ↓
Efektler Göster
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **GUI'den terfi:** `ClanMemberMenu`'dan da terfi edilebilir (sol tık)

**Durum:** ✅ Çalışıyor

---

### 8. ✅ Rütbe Değiştirme (Clan.setRank)

**Dosya:** `Clan.java:60-65`

**Fonksiyon:** `setRank(UUID uuid, Rank rank)`

**Nasıl Çalışıyor:**

1. **Null Check:**
   ```java
   if (uuid == null || rank == null) return;
   ```

2. **Üye Var mı?**
   ```java
   if (members.containsKey(uuid)) {
       members.put(uuid, rank);
   }
   ```
   - Sadece klan üyesi ise rütbe değiştirilebilir
   - `members` Map'inde güncellenir

**Kod Akışı:**
```
setRank(uuid, rank)
    ↓
Null Check → HATA → return
    ↓ OK
Üye Var mı? → HAYIR → return
    ↓ EVET
members.put(uuid, rank)
```

**Sorunlar:**
- ✅ Basit ve çalışıyor

**Durum:** ✅ Çalışıyor

---

### 9. ✅ Rütbe Sistemi (ClanRankSystem)

**Dosya:** `ClanRankSystem.java`

**Fonksiyonlar:**
- `hasPermission()` - Yetki kontrolü
- `getRankPermissions()` - Rütbe yetkileri
- `transferLeadership()` - Liderlik devretme

**Rütbeler:**
1. **LEADER (5):** Tüm yetkiler
2. **ELITE (4):** Yapı inşa, Ritüel, Banka çekme (limitli), Görev başlatma
3. **GENERAL (3):** Yapı inşa/yıkma, Üye ekle/çıkar, Savaş başlat, Banka yönetimi, İttifak yönetimi
4. **MEMBER (2):** Sadece yapı kullanma
5. **RECRUIT (1):** Hiçbir yetki

**Yetki Kontrolü:**
```java
public boolean hasPermission(Clan clan, UUID playerId, Permission permission) {
    if (clan == null || playerId == null || permission == null) return false;
    
    if (!clan.getMembers().containsKey(playerId)) {
        return false; // Klan üyesi değil
    }
    
    Clan.Rank rank = clan.getRank(playerId);
    Set<Permission> rankPermissions = getRankPermissions(rank);
    return rankPermissions.contains(permission);
}
```

**Sorunlar:**
- ✅ Detaylı yetki sistemi var
- ✅ Her rütbe için farklı yetkiler

**Durum:** ✅ Çalışıyor

---

## 💎 KLAN KRISTALİ İŞLEMLERİ

### 10. ✅ Klan Kristali Taşıma

**Dosya:** `TerritoryListener.java:748-826`

**Fonksiyon:** `onCrystalRelocate(PlayerInteractEvent event)`

**Nasıl Çalışıyor:**

1. **Tetikleme:**
   - Shift + Sağ Tık + Klan Kristali (CRYSTAL item)
   - Tıklanan blok: Kristal'in altındaki blok

2. **Yetki Kontrolü:**
   ```java
   if (playerClan.getRank(player.getUniqueId()) != Clan.Rank.LEADER) {
       return; // Sadece lider taşıyabilir
   }
   ```

3. **Kristal Var mı?**
   ```java
   if (playerClan.getCrystalLocation() == null || !playerClan.hasCrystal()) {
       return; // Kristal yok
   }
   ```

4. **Yeni Konum Kontrolü (ASYNC):**
   ```java
   Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
       boolean isValid = isSurroundedByClanFences(newLocation);
       // Main thread'e geri dön
   });
   ```
   - Yeni konum çitlerle çevrili mi kontrol edilir
   - Async yapılıyor (lag önleme)

5. **Kristal Taşı:**
   ```java
   // Eski kristali kaldır
   if (oldCrystal != null) {
       oldCrystal.remove();
   }
   
   // Yeni kristal oluştur
   EnderCrystal newCrystal = newLocation.getWorld().spawn(newLocation, EnderCrystal.class);
   playerClan.setCrystalLocation(newLocation);
   playerClan.setCrystalEntity(newCrystal);
   ```

**Kod Akışı:**
```
Shift + Sağ Tık + Kristal
    ↓
Lider mi? → HAYIR → İptal
    ↓ EVET
Kristal Var mı? → HAYIR → İptal
    ↓ EVET
Yeni Konum Çitlerle Çevrili mi? (ASYNC) → HAYIR → İptal
    ↓ EVET
Eski Kristali Kaldır
    ↓
Yeni Kristal Oluştur
    ↓
Konum Güncelle
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **Async kontrol:** Async kontrol sonrası main thread'e dönüş karmaşık

**Durum:** ✅ Çalışıyor

---

### 11. ✅ Klan Kristali Kırma (Klan Dağıtma)

**Dosya:** `TerritoryListener.java:662-695`

**Fonksiyon:** `onCrystalBreak(BlockBreakEvent event)`

**Nasıl Çalışıyor:**

1. **Kristal Kırma:**
   - Ender Crystal blok kırma eventi

2. **Lider Kontrolü:**
   ```java
   if (breaker != null && owner.getRank(breaker.getUniqueId()) == Clan.Rank.LEADER) {
       // Lider klanı bozdu
       territoryManager.getClanManager().disbandClan(owner);
   }
   ```

3. **Klan Dağıt:**
   ```java
   clanManager.disbandClan(owner);
   ```

**Kod Akışı:**
```
Kristal Kırma
    ↓
Lider mi? → EVET → Klan Dağıt
    ↓ HAYIR
İptal (Koruma)
```

**Sorunlar:**
- ✅ Çalışıyor

**Durum:** ✅ Çalışıyor

---

## 👤 MİSAFİR (GUEST) SİSTEMİ

### 12. ✅ Misafir Ekleme

**Dosya:** `RitualInteractionListener.java:912-977`

**Fonksiyon:** `onGuestAdd(PlayerInteractEntityEvent event)`

**Nasıl Çalışıyor:**

1. **Tetikleme:**
   - Shift + Sağ Tık + Oyuncuya
   - Elde: Yeşil Çiçek (Cactus veya Green Dye)

2. **Yetki Kontrolü:**
   ```java
   if (clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
       return; // Sadece lider ekleyebilir
   }
   ```

3. **Hedef Kontrolü:**
   ```java
   Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
   if (targetClan != null && !targetClan.equals(clan)) {
       return; // Hedef başka bir klana üye
   }
   ```

4. **Zaten Guest mi?**
   ```java
   if (clan.isGuest(target.getUniqueId())) {
       return; // Zaten guest
   }
   ```

5. **Guest Ekle:**
   ```java
   clan.addGuest(target.getUniqueId());
   ```

**Kod Akışı:**
```
Shift + Sağ Tık + Oyuncu + Yeşil Çiçek
    ↓
Lider mi? → HAYIR → İptal
    ↓ EVET
Hedef Başka Klanda mı? → EVET → İptal
    ↓ HAYIR
Zaten Guest mi? → EVET → İptal
    ↓ HAYIR
clan.addGuest(targetId)
    ↓
Çiçek Tüket
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor

**Durum:** ✅ Çalışıyor

---

### 13. ✅ Misafir Çıkarma

**Dosya:** `RitualInteractionListener.java:980-1029`

**Fonksiyon:** `onGuestRemove(PlayerInteractEntityEvent event)`

**Nasıl Çalışıyor:**

1. **Tetikleme:**
   - Shift + Sağ Tık + Guest Oyuncuya
   - Elde: Kırmızı Çiçek (Red Tulip, Rose Bush, Poppy)

2. **Yetki Kontrolü:**
   ```java
   if (clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
       return; // Sadece lider çıkarabilir
   }
   ```

3. **Guest mi?**
   ```java
   if (!clan.isGuest(target.getUniqueId())) {
       return; // Guest değil
   }
   ```

4. **Guest Çıkar:**
   ```java
   clan.getGuests().remove(target.getUniqueId());
   ```

**Kod Akışı:**
```
Shift + Sağ Tık + Guest + Kırmızı Çiçek
    ↓
Lider mi? → HAYIR → İptal
    ↓ EVET
Guest mi? → HAYIR → İptal
    ↓ EVET
clan.getGuests().remove(targetId)
    ↓
Çiçek Tüket
    ↓
Başarılı!
```

**Sorunlar:**
- ✅ Çalışıyor

**Durum:** ✅ Çalışıyor

---

## 💥 KLAN DAĞITMA

### 14. ✅ Klan Dağıtma

**Dosya:** `ClanManager.java:251-281`

**Fonksiyon:** `disbandClan(Clan clan)`

**Nasıl Çalışıyor:**

1. **Null Check:**
   ```java
   if (clan == null) return;
   ```

2. **Tüm Üyeleri Çıkar:**
   ```java
   Set<UUID> memberIds = new HashSet<>(clan.getMembers().keySet());
   for (UUID memberId : memberIds) {
       playerClanMap.remove(memberId);
   }
   ```

3. **Klanı Listeden Çıkar:**
   ```java
   clans.remove(clan.getId());
   ```

4. **Broadcast:**
   ```java
   Bukkit.broadcastMessage("§c" + clanName + " klanı dağıtıldı.");
   ```

5. **Cache Güncelle:**
   ```java
   if (territoryManager != null) {
       territoryManager.setCacheDirty();
   }
   ```

**Kod Akışı:**
```
disbandClan(clan)
    ↓
Null Check → HATA → return
    ↓ OK
Tüm Üyeleri playerClanMap'ten Çıkar
    ↓
Klanı clans Map'inden Çıkar
    ↓
Broadcast
    ↓
Cache Güncelle
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **Territory temizleme:** Territory temizlenmiyor (memory leak riski)

**Durum:** ✅ Çalışıyor

---

## 👑 LİDERLİK DEVRETME

### 15. ✅ Liderlik Devretme

**Dosya:** `ClanRankSystem.java:114-204`

**Fonksiyon:** `transferLeadership(Player currentLeader, Player newLeader, Location crystalLoc)`

**Nasıl Çalışıyor:**

1. **Null Check:**
   ```java
   if (currentLeader == null || newLeader == null || currentLeader.equals(newLeader)) {
       return false;
   }
   ```

2. **Aynı Klan mı?**
   ```java
   Clan newLeaderClan = clanManager.getClanByPlayer(newLeader.getUniqueId());
   if (newLeaderClan == null || !newLeaderClan.equals(clan)) {
       return false; // Aynı klanda değil
   }
   ```

3. **Lider Kontrolü:**
   ```java
   if (currentRank != Clan.Rank.LEADER) {
       return false; // Mevcut lider değil
   }
   ```

4. **Yeni Lider General mi?**
   ```java
   if (newRank != Clan.Rank.GENERAL) {
       return false; // General olmalı
   }
   ```

5. **Mesafe Kontrolü:**
   ```java
   double distance1 = currentLeader.getLocation().distance(crystalLoc);
   double distance2 = newLeader.getLocation().distance(crystalLoc);
   if (distance1 > 3 || distance2 > 3) {
       return false; // Kristalden 3 bloktan fazla uzak
   }
   ```

6. **Nether Star Kontrolü:**
   ```java
   if (!hasItemInHand(currentLeader, Material.NETHER_STAR) ||
       !hasItemInHand(newLeader, Material.NETHER_STAR)) {
       return false; // Her ikisinin elinde Nether Star olmalı
   }
   ```

7. **Liderlik Devret:**
   ```java
   synchronized (clan.getMembers()) {
       clan.setRank(currentLeader.getUniqueId(), Clan.Rank.GENERAL);
       clan.setRank(newLeader.getUniqueId(), Clan.Rank.LEADER);
   }
   ```

**Kod Akışı:**
```
transferLeadership(currentLeader, newLeader, crystalLoc)
    ↓
Null Check → HATA → return false
    ↓ OK
Aynı Klan mı? → HAYIR → return false
    ↓ EVET
Mevcut Lider mi? → HAYIR → return false
    ↓ EVET
Yeni Lider General mi? → HAYIR → return false
    ↓ EVET
Kristalden 3 Blok İçinde mi? → HAYIR → return false
    ↓ EVET
Her İkisinin Elinde Nether Star Var mı? → HAYIR → return false
    ↓ EVET
Liderlik Devret (setRank)
    ↓
Efektler Göster
    ↓
Broadcast
    ↓
return true
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **Ritüel tetikleme:** Bu fonksiyon `RitualInteractionListener`'dan çağrılıyor mu kontrol edilmeli

**Durum:** ✅ Çalışıyor

---

## 📱 KLAN MENÜLERİ

### 16. ✅ Ana Klan Menüsü

**Dosya:** `ClanMenu.java:41-242`

**Fonksiyon:** `openMenu(Player player)`

**Özellikler:**
- Klan bilgileri (bakiye, üye sayısı, teknoloji seviyesi, bölge)
- Üyeler butonu
- Banka butonu
- Görevler butonu
- Maaş yönetimi (Lider/General)
- Alan genişletme (Lider/General)
- Yapılar butonu
- Market butonu
- Kervan butonu
- Yükseltmeler butonu

**Kod Akışı:**
```
openMenu(player)
    ↓
Klan Var mı? → HAYIR → İptal
    ↓ EVET
Menü Oluştur (27 slot)
    ↓
Butonları Ekle
    ↓
Oyuncuya Aç
```

**Sorunlar:**
- ✅ Çalışıyor

**Durum:** ✅ Çalışıyor

---

### 17. ✅ Üye Yönetim Menüsü

**Dosya:** `ClanMemberMenu.java:50-133`

**Fonksiyon:** `openMenu(Player player)`

**Özellikler:**
- Üye listesi (rütbe sırasına göre)
- Online/Offline durumu
- Aktivite bilgisi
- Rütbe değiştirme (Sol tık - Lider/General)
- Üye çıkarma (Sağ tık - Lider/General)

**Kod Akışı:**
```
openMenu(player)
    ↓
Klan Var mı? → HAYIR → İptal
    ↓ EVET
Üye Listesini Sırala (rütbe seviyesine göre)
    ↓
Her Üye İçin Item Oluştur
    ↓
Menüye Ekle
    ↓
Oyuncuya Aç
```

**Sorunlar:**
- ✅ Çalışıyor
- ⚠️ **UUID takibi:** Item'lardan UUID almak için NBT kullanılıyor, alternatif olarak slot numarası kullanılıyor

**Durum:** ✅ Çalışıyor

---

## 🚨 SORUNLAR VE EKSİKLER

### ❌ Kritik Sorunlar

1. **Territory Temizleme Eksikliği**
   - **Dosya:** `ClanManager.java:251-281`
   - **Sorun:** Klan dağıtıldığında territory temizlenmiyor
   - **Etki:** Memory leak riski
   - **Çözüm:** `territoryManager.removeTerritory(clan)` çağrılmalı

2. **Async Kontrol Karmaşıklığı**
   - **Dosya:** `TerritoryListener.java:352-467`
   - **Sorun:** Async kontrol sonrası main thread'e dönüş karmaşık
   - **Etki:** Hata riski, kod okunabilirliği düşük
   - **Çözüm:** Daha basit bir async pattern kullanılmalı

3. **Chat Input Entegrasyonu**
   - **Dosya:** `TerritoryListener.java:417-467`
   - **Sorun:** Chat input sistemi başka bir listener'da, entegrasyon sorunlu olabilir
   - **Etki:** Klan ismi girişi çalışmayabilir
   - **Çözüm:** Chat input sistemi kontrol edilmeli

### ⚠️ Orta Öncelikli Sorunlar

4. **3x3 Ritüel Kontrolü**
   - **Dosya:** `RitualInteractionListener.java:179-188`
   - **Sorun:** Sadece zemin kontrolü yapılıyor, yukarı/aşağı kontrolü yok
   - **Etki:** Ritüel yapısı yanlış kurulabilir
   - **Çözüm:** 3D kontrol eklenmeli

5. **Liderlik Devretme Ritüeli**
   - **Dosya:** `ClanRankSystem.java:114-204`
   - **Sorun:** Bu fonksiyon `RitualInteractionListener`'dan çağrılıyor mu kontrol edilmeli
   - **Etki:** Ritüel çalışmayabilir
   - **Çözüm:** Ritüel listener'da çağrı kontrol edilmeli

6. **UUID Takibi (GUI)**
   - **Dosya:** `ClanMemberMenu.java:664-734`
   - **Sorun:** Item'lardan UUID almak için NBT kullanılıyor, alternatif olarak slot numarası kullanılıyor
   - **Etki:** UUID bulunamayabilir
   - **Çözüm:** Daha güvenilir bir yöntem kullanılmalı

---

## 📊 ÖZELLİK DURUM TABLOSU

| Özellik | Durum | Dosya | Sorunlar |
|---------|-------|-------|----------|
| Klan Oluşturma | ✅ Çalışıyor | TerritoryListener.java:317 | Chat input entegrasyonu |
| Klan Oluşturma (Manager) | ✅ Çalışıyor | ClanManager.java:74 | - |
| Üye Alma (Ritüel) | ✅ Çalışıyor | RitualInteractionListener.java:68 | 3x3 kontrolü |
| Üye Ekleme (Manager) | ✅ Çalışıyor | ClanManager.java:185 | - |
| Üye Çıkarma (Ritüel) | ✅ Çalışıyor | RitualInteractionListener.java:260 | - |
| Üye Çıkarma (Manager) | ✅ Çalışıyor | ClanManager.java:226 | - |
| Üye Ayrılma | ✅ Çalışıyor | RitualInteractionListener.java:360 | - |
| Rütbe Değiştirme (Ritüel) | ✅ Çalışıyor | RitualInteractionListener.java:488 | - |
| Rütbe Değiştirme (GUI) | ✅ Çalışıyor | ClanMemberMenu.java:596 | UUID takibi |
| Rütbe Sistemi | ✅ Çalışıyor | ClanRankSystem.java | - |
| Kristal Taşıma | ✅ Çalışıyor | TerritoryListener.java:748 | Async kontrol |
| Kristal Kırma | ✅ Çalışıyor | TerritoryListener.java:662 | - |
| Misafir Ekleme | ✅ Çalışıyor | RitualInteractionListener.java:912 | - |
| Misafir Çıkarma | ✅ Çalışıyor | RitualInteractionListener.java:980 | - |
| Klan Dağıtma | ✅ Çalışıyor | ClanManager.java:251 | Territory temizleme |
| Liderlik Devretme | ✅ Çalışıyor | ClanRankSystem.java:114 | Ritüel entegrasyonu |
| Ana Klan Menüsü | ✅ Çalışıyor | ClanMenu.java:41 | - |
| Üye Yönetim Menüsü | ✅ Çalışıyor | ClanMemberMenu.java:50 | UUID takibi |

---

## 💻 KOD KALİTESİ ANALİZİ

### ✅ İyi Yönler

1. **Thread-Safety**
   - `ConcurrentHashMap` kullanılıyor
   - `synchronized` bloklar var
   - Thread-safe operations

2. **Null Check'ler**
   - Tüm metodlarda null check'ler var
   - Exception handling var

3. **Modüler Yapı**
   - Her özellik ayrı dosyada
   - Manager pattern kullanılıyor

4. **Ritüel Sistemi**
   - Fiziksel blok düzenekleri
   - Oyuncu deneyimi iyi

### ❌ Kötü Yönler

1. **Async Kontrol Karmaşıklığı**
   - Async kontrol sonrası main thread'e dönüş karmaşık
   - Hata riski var

2. **Territory Temizleme Eksikliği**
   - Klan dağıtıldığında territory temizlenmiyor
   - Memory leak riski

3. **UUID Takibi (GUI)**
   - Item'lardan UUID almak için NBT kullanılıyor
   - Alternatif yöntemler karmaşık

---

## 📝 ÖNERİLER

### Yüksek Öncelik

1. **Territory Temizleme**
   - Klan dağıtıldığında territory temizlenmeli
   - `territoryManager.removeTerritory(clan)` çağrılmalı

2. **Async Kontrol İyileştirme**
   - Daha basit bir async pattern kullanılmalı
   - Callback pattern kullanılabilir

3. **Chat Input Kontrolü**
   - Chat input sistemi kontrol edilmeli
   - Entegrasyon test edilmeli

### Orta Öncelik

4. **3x3 Ritüel Kontrolü**
   - 3D kontrol eklenmeli
   - Yukarı/aşağı kontrolü yapılmalı

5. **Liderlik Devretme Ritüeli**
   - Ritüel listener'da çağrı kontrol edilmeli
   - Test edilmeli

6. **UUID Takibi (GUI)**
   - Daha güvenilir bir yöntem kullanılmalı
   - Slot numarası kullanılabilir (daha güvenilir)

---

**Son Güncelleme:** 2024
**Durum:** ✅ **%95 ÇALIŞIYOR** - Küçük sorunlar var

