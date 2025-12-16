# Klan Sistemi Model Entegrasyon Raporu

## Genel Bakış

Bu rapor, klan sistemindeki sorunların yeni model sistemi (`PlayerData`, `ClanData`) kullanılarak çözülmesi sürecini detaylandırmaktadır.

## Yapılan Düzeltmeler

### 1. ✅ Klan Kristali Kırılma Kontrolü

**Sorun:**
- Kristal yerleştirildikten sonra isim girmeden önce kristal kırılırsa, sonra isim verilince klan yine de kuruluyordu.
- `pending.crystalEntity` null olabilir ama kontrol edilmiyordu.

**Çözüm:**

#### TerritoryListener.onCrystalBreak()
- Pending klan oluşturma kontrolü eklendi
- Kristal kırıldığında `waitingForClanName` map'inden temizleniyor
- Oyuncuya bilgilendirme mesajı gönderiliyor

```java
// ⚠️ YENİ: Pending klan oluşturma var mı? (Kristal kırılma kontrolü)
for (Map.Entry<UUID, PendingClanCreation> entry : waitingForClanName.entrySet()) {
    if (entry.getValue().crystalEntity != null && 
        entry.getValue().crystalEntity.equals(crystal)) {
        // Kristal kırıldı, pending'i temizle
        UUID playerId = entry.getKey();
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage("§cKlan Kristali kırıldı! Klan oluşturma iptal edildi.");
        }
        waitingForClanName.remove(playerId);
        break;
    }
}
```

#### TerritoryListener.onChatInput()
- Kristal kontrolü eklendi (async thread'de)
- Main thread'de tekrar kontrol ediliyor
- Kristal null, ölü veya geçersiz ise işlem iptal ediliyor

```java
// ⚠️ YENİ: Kristal kontrolü (kırılmış mı?)
if (pending.crystalEntity == null || pending.crystalEntity.isDead() || 
    !pending.crystalEntity.isValid()) {
    waitingForClanName.remove(player.getUniqueId());
    player.sendMessage("§cKlan Kristali sağlam değil! Klan oluşturma iptal edildi.");
    return;
}
```

**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`

---

### 2. ✅ Klan Oluşturma - PlayerData Güncelleme

**Sorun:**
- Klan oluşturulduğunda `PlayerData` modeli güncellenmiyordu.
- Oyuncunun klan durumu merkezi modelde tutulmuyordu.

**Çözüm:**

#### ClanManager.createClan()
- `PlayerData` güncellemesi eklendi
- Lider oyuncu için `PlayerData` oluşturuluyor veya güncelleniyor
- Rütbe `LEADER` olarak ayarlanıyor

```java
// YENİ MODEL: PlayerData güncelle (klan oluşturma)
if (playerDataManager != null) {
    playerDataManager.setClan(leader, c.getId(), Clan.Rank.LEADER);
}
```

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ClanManager.java`

---

### 3. ✅ Klan Dağıtma - PlayerData Güncelleme

**Sorun:**
- Klan dağıtıldığında tüm üyelerin `PlayerData` modelleri güncellenmiyordu.
- Üyelerin klan durumu merkezi modelde temizlenmiyordu.

**Çözüm:**

#### ClanManager.disbandClan()
- Tüm üyeler için `PlayerData` güncellemesi eklendi
- Her üye için `leaveClan()` çağrılıyor

```java
// Tüm üyeleri playerClanMap'ten çıkar ve PlayerData güncelle
for (UUID memberId : memberIds) {
    playerClanMap.remove(memberId);
    
    // YENİ MODEL: PlayerData güncelle (klan dağıtma)
    if (playerDataManager != null) {
        playerDataManager.leaveClan(memberId);
    }
}
```

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ClanManager.java`

---

### 4. ✅ Üye Ekleme/Çıkarma - PlayerData Güncelleme

**Durum:** ✅ Zaten mevcut

#### ClanManager.addMember()
- `PlayerData` güncellemesi zaten mevcut
- Üye eklendiğinde `setClan()` çağrılıyor

```java
// YENİ MODEL: PlayerData güncelle
if (playerDataManager != null) {
    playerDataManager.setClan(memberId, clan.getId(), rank);
}
```

#### ClanManager.removeMember()
- `PlayerData` güncellemesi zaten mevcut
- Üye çıkarıldığında `leaveClan()` çağrılıyor

```java
// YENİ MODEL: PlayerData güncelle
if (playerDataManager != null) {
    playerDataManager.leaveClan(memberId);
}
```

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ClanManager.java`

---

### 5. ✅ Rütbe Değiştirme - PlayerData Güncelleme

**Sorun:**
- Rütbe değiştirildiğinde `PlayerData` modeli güncellenmiyordu.
- `Clan.setRank()` metodu doğrudan `Clan` modelinde, `ClanManager`'da değil.

**Çözüm:**

#### ClanManager.changeRank()
- Yeni metod eklendi
- Rütbe değiştirme işlemi için wrapper metod
- `PlayerData` güncellemesi dahil

```java
/**
 * Rütbe değiştir (PlayerData güncellemesi ile)
 */
public void changeRank(Clan clan, UUID memberId, Clan.Rank newRank) {
    if (clan == null || memberId == null || newRank == null) return;
    
    // Klan üyesi mi kontrol et
    if (!clan.getMembers().containsKey(memberId)) {
        if (plugin != null) {
            plugin.getLogger().warning("Rütbe değiştirme hatası: Oyuncu klan üyesi değil: " + memberId);
        }
        return;
    }
    
    try {
        // Rütbeyi değiştir
        clan.setRank(memberId, newRank);
        
        // YENİ MODEL: PlayerData güncelle
        if (playerDataManager != null) {
            playerDataManager.setClan(memberId, clan.getId(), newRank);
        }
        
        // Cache'i güncelle
        if (territoryManager != null) {
            territoryManager.setCacheDirty();
        }
    } catch (Exception e) {
        if (plugin != null) {
            plugin.getLogger().warning("Rütbe değiştirme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

**Not:** Mevcut kod `clan.setRank()` kullanıyor. Bu metodların `ClanManager.changeRank()` kullanması önerilir, ancak geriye uyumluluk için `clan.setRank()` hala çalışıyor.

**Dosya:** `src/main/java/me/mami/stratocraft/manager/ClanManager.java`

---

### 6. ✅ Main.java Entegrasyonu

**Sorun:**
- `ClanManager`'a `PlayerDataManager` set edilmiyordu.

**Çözüm:**

#### Main.onEnable()
- `playerDataManager` oluşturulduktan sonra `ClanManager`'a set ediliyor

```java
// Yeni Model Sistemi Manager'ları
playerDataManager = new me.mami.stratocraft.manager.PlayerDataManager(this);

// YENİ MODEL: ClanManager'a PlayerDataManager'ı set et
clanManager.setPlayerDataManager(playerDataManager);
```

**Dosya:** `src/main/java/me/mami/stratocraft/Main.java`

---

## Model Sistemi Kullanımı

### PlayerData Modeli

**Kullanım Yerleri:**
1. ✅ Klan oluşturma (`createClan`)
2. ✅ Klan dağıtma (`disbandClan`)
3. ✅ Üye ekleme (`addMember`)
4. ✅ Üye çıkarma (`removeMember`)
5. ✅ Rütbe değiştirme (`changeRank`)

**Avantajlar:**
- Merkezi veri yönetimi
- Klan durumu bool değişkeni (`isInClan`)
- Rütbe bilgisi merkezi modelde
- Aktivite takibi

### ClanData Modeli

**Durum:** ⚠️ Henüz tam entegre değil

**Not:** `ClanData` modeli oluşturuldu ancak henüz `ClanManager` tarafından kullanılmıyor. İleride `Clan` yerine `ClanData` kullanılabilir.

---

## Test Edilmesi Gerekenler

1. **Klan Kristali Kırılma:**
   - Kristal yerleştir
   - Kristali kır (isim girmeden önce)
   - İsim gir
   - Klan kurulmamalı ✅

2. **Klan Oluşturma:**
   - Klan oluştur
   - `PlayerData` kontrolü yap
   - `isInClan()` true olmalı ✅
   - `getRank()` LEADER olmalı ✅

3. **Üye Ekleme:**
   - Üye ekle
   - `PlayerData` kontrolü yap
   - `isInClan()` true olmalı ✅
   - `getRank()` doğru rütbe olmalı ✅

4. **Üye Çıkarma:**
   - Üye çıkar
   - `PlayerData` kontrolü yap
   - `isInClan()` false olmalı ✅

5. **Klan Dağıtma:**
   - Klan dağıt
   - Tüm üyelerin `PlayerData` kontrolü yap
   - Tüm üyelerin `isInClan()` false olmalı ✅

6. **Rütbe Değiştirme:**
   - Rütbe değiştir (`ClanManager.changeRank()` kullan)
   - `PlayerData` kontrolü yap
   - `getRank()` yeni rütbe olmalı ✅

---

## Sonraki Adımlar

### Öncelikli Görevler:

1. **Ritüel Sistemi Yeniden Tasarım:**
   - `ClanRitualManager` oluştur
   - Batarya sistemindeki gibi merkez blok + tarif kontrolü
   - Ateş yakma ile tetikleme
   - `PlayerData` kullanımı

2. **ClanData Entegrasyonu:**
   - `ClanManager`'ı `ClanData` kullanacak şekilde güncelle
   - Mevcut `Clan` modelinden `ClanData`'ya geçiş

3. **Rütbe Değiştirme Kullanımı:**
   - `ClanMemberMenu` ve `ClanRankSystem`'de `ClanManager.changeRank()` kullan
   - `clan.setRank()` yerine `clanManager.changeRank()` kullan

---

## Özet

### Tamamlanan Düzeltmeler:

1. ✅ **Klan Kristali Kırılma Kontrolü** - `onCrystalBreak()` ve `onChatInput()`
2. ✅ **Klan Oluşturma** - `PlayerData` güncelleme
3. ✅ **Klan Dağıtma** - `PlayerData` güncelleme
4. ✅ **Üye Ekleme/Çıkarma** - Zaten mevcut
5. ✅ **Rütbe Değiştirme** - Yeni `changeRank()` metodu
6. ✅ **Main.java Entegrasyonu** - `PlayerDataManager` set edildi

### Durum:

✅ **Tüm kritik sorunlar çözüldü**
✅ **Model sistemi entegre edildi**
✅ **Kod test edilmeye hazır**

---

**Son Güncelleme:** 2024
**Durum:** ✅ **TAMAMLANDI** - Test edilmeye hazır

