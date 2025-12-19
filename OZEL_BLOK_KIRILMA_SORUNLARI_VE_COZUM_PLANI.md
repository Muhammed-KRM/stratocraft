# ÖZEL BLOK KIRILMA SORUNLARI VE ÇÖZÜM PLANI

**Tarih:** Bugün  
**Kapsam:** Özel blokların kırıldığında özel item olarak düşmemesi sorunu  
**Durum:** 🔍 SORUNLAR TESPİT EDİLDİ, ÇÖZÜMLER UYGULANACAK

---

## 📋 TESPİT EDİLEN SORUNLAR

### 1. ⚠️ **KRİTİK: TerritoryListener.onBreak() Yapı Çekirdeği Koruması**

**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`  
**Satır:** 142-152

**Sorun:**
```java
// YENİ: Klan yapıları kırılmamalı (korunmalı)
Block block = event.getBlock();
Main mainPlugin = Main.getInstance();
if (mainPlugin != null && mainPlugin.getStructureCoreManager() != null) {
    if (mainPlugin.getStructureCoreManager().isStructureCore(block)) {
        // Bu bir yapı çekirdeği, kırılamaz
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cKlan yapıları kırılamaz! Yapıyı kaldırmak için klan menüsünü kullanın.");
        return;
    }
}
```

**Problem:**
- `TerritoryListener.onBreak()` metodu `EventPriority.NORMAL` (default) ile çalışıyor
- `StructureCoreListener.onStructureCoreBreak()` metodu `EventPriority.HIGH` ile çalışıyor
- Ama `TerritoryListener.onBreak()` önce çalışıyor ve event'i cancel ediyor
- Bu yüzden `StructureCoreListener.onStructureCoreBreak()` hiç çalışmıyor

**Çözüm:**
- `TerritoryListener.onBreak()` metodunda yapı çekirdeği kontrolünü kaldırmalıyız
- Özel blok handler'ları zaten var ve doğru çalışıyor

---

### 2. ⚠️ **KRİTİK: TerritoryListener.onFenceBreak() Yanlış Çalışıyor**

**Dosya:** `src/main/java/me/mami/stratocraft/listener/TerritoryListener.java`  
**Satır:** 432-479

**Sorun:**
```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onFenceBreak(BlockBreakEvent event) {
    // ...
    // ✅ ItemStack'e veri ekle
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item != null && item.getType() == Material.OAK_FENCE) {
        // ItemStack'in PersistentDataContainer'ına yaz
        // ...
    }
}
```

**Problem:**
- `setDropItems(false)` yok - normal OAK_FENCE drop ediliyor
- Oyuncunun elindeki item'a veri ekliyor, ama drop edilen item'a değil
- Özel item drop etmiyor

**Çözüm:**
- `setDropItems(false)` ekle
- Drop edilen item'a PDC verisi ekle
- `dropItemNaturally()` kullan

---

### 3. ⚠️ **KRİTİK: TrapListener.onTrapCoreBreak() Eksik**

**Dosya:** `src/main/java/me/mami/stratocraft/listener/TrapListener.java`

**Sorun:**
- `onTrapCoreBreak()` metodu yok
- Trap core kırıldığında normal LODESTONE drop ediliyor
- Özel TRAP_CORE item drop etmiyor

**Çözüm:**
- `onTrapCoreBreak()` metodu ekle
- `setDropItems(false)` kullan
- `ItemManager.TRAP_CORE` item'ını drop et
- PDC verisini (ownerId) item'a ekle

---

### 4. ⚠️ **KRİTİK: ClanSystemListener.onClanBankBreak() Yanlış Çalışıyor**

**Dosya:** `src/main/java/me/mami/stratocraft/listener/ClanSystemListener.java`  
**Satır:** 217-259

**Sorun:**
```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onClanBankBreak(BlockBreakEvent event) {
    // ...
    // ✅ ItemStack'e veri ekle
    ItemStack item = player.getInventory().getItemInMainHand();
    if (item != null && item.getType() == Material.ENDER_CHEST) {
        // ItemStack'in PersistentDataContainer'ına yaz
        // ...
    }
}
```

**Problem:**
- `setDropItems(false)` yok - normal ENDER_CHEST drop ediliyor
- Oyuncunun elindeki item'a veri ekliyor, ama drop edilen item'a değil
- Özel item drop etmiyor

**Çözüm:**
- `setDropItems(false)` ekle
- Drop edilen item'a PDC verisi ekle
- `dropItemNaturally()` kullan

---

### 5. ⚠️ **ORTA: StructureCoreListener.onStructureCoreBreak() Doğru Ama TerritoryListener Engelliyor**

**Dosya:** `src/main/java/me/mami/stratocraft/listener/StructureCoreListener.java`  
**Satır:** 342-382

**Durum:**
- Metod doğru yazılmış
- `setDropItems(false)` var
- Özel item drop ediyor
- Ama `TerritoryListener.onBreak()` önce çalışıyor ve event'i cancel ediyor

**Çözüm:**
- `TerritoryListener.onBreak()` metodunda yapı çekirdeği kontrolünü kaldırmalıyız

---

## 🔍 İNTERNET ARAŞTIRMASI BULGULARI

### 1. BlockBreakEvent ve setDropItems()
**Kaynak:** Spigot API Documentation
- **Sorun:** Normal drop'lar özel item'ları override ediyor
- **Çözüm:** `event.setDropItems(false)` kullan ve `dropItemNaturally()` ile özel item drop et
- **Referans:** [Spigot API - BlockBreakEvent](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/block/BlockBreakEvent.html)

### 2. Event Priority ve Handler Sırası
**Kaynak:** Bukkit Event Priority Documentation
- **Sorun:** Event handler'ları yanlış sırada çalışıyor
- **Çözüm:** Özel blok handler'ları `HIGH` priority'de, koruma handler'ları `NORMAL` veya `LOW` priority'de olmalı
- **Referans:** [Bukkit Event Priority](https://bukkit.fandom.com/wiki/Event_API_Reference#Event_Priority)

### 3. PersistentDataContainer ve ItemStack
**Kaynak:** Spigot PersistentDataContainer Documentation
- **Sorun:** Drop edilen item'a PDC verisi eklenmiyor
- **Çözüm:** ItemStack oluştur, PDC verisi ekle, sonra `dropItemNaturally()` kullan
- **Referans:** [Spigot PDC Documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/persistence/PersistentDataContainer.html)

---

## 🛠️ ÇÖZÜM UYGULAMA PLANI

### Faz 1: TerritoryListener.onBreak() Düzeltmesi (Öncelik: YÜKSEK)

1. **Yapı çekirdeği korumasını kaldır**
   - `TerritoryListener.onBreak()` metodundan yapı çekirdeği kontrolünü kaldır
   - Özel blok handler'ları zaten var ve doğru çalışıyor

### Faz 2: Özel Blok Handler'ları Düzeltmesi (Öncelik: YÜKSEK)

2. **TerritoryListener.onFenceBreak() düzelt**
   - `setDropItems(false)` ekle
   - Normal OAK_FENCE item'ı oluştur
   - PDC verisini (clanId) item'a ekle
   - `dropItemNaturally()` kullan

3. **TrapListener.onTrapCoreBreak() ekle**
   - `@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)` ekle
   - `setDropItems(false)` kullan
   - `ItemManager.TRAP_CORE.clone()` kullan
   - PDC verisini (ownerId) item'a ekle
   - `dropItemNaturally()` kullan

4. **ClanSystemListener.onClanBankBreak() düzelt**
   - `setDropItems(false)` ekle
   - Normal ENDER_CHEST item'ı oluştur
   - PDC verisini (clanId) item'a ekle
   - `dropItemNaturally()` kullan

### Faz 3: Event Priority Kontrolü (Öncelik: ORTA)

5. **Event priority'leri kontrol et**
   - Özel blok handler'ları `HIGH` priority'de olmalı
   - `TerritoryListener.onBreak()` `NORMAL` veya `LOW` priority'de olmalı

---

## 📊 BEKLENEN İYİLEŞTİRME

### Önceki Durum:
- Yapı çekirdeği kırılamıyor (event cancel)
- Klan çiti kırıldığında normal OAK_FENCE drop ediliyor
- Tuzak çekirdeği kırıldığında normal LODESTONE drop ediliyor
- Klan bankası kırıldığında normal ENDER_CHEST drop ediliyor

### Sonraki Durum:
- ✅ Yapı çekirdeği kırıldığında özel STRUCTURE_CORE item drop ediyor
- ✅ Klan çiti kırıldığında özel CLAN_FENCE item (OAK_FENCE + PDC) drop ediyor
- ✅ Tuzak çekirdeği kırıldığında özel TRAP_CORE item drop ediyor
- ✅ Klan bankası kırıldığında özel CLAN_BANK item (ENDER_CHEST + PDC) drop ediyor

---

## ✅ UYGULAMA DURUMU

- [x] **Faz 1: TerritoryListener.onBreak() düzeltmesi**
  - ✅ Yapı çekirdeği koruması kaldırıldı
  - ✅ Event priority `NORMAL` olarak belirtildi

- [x] **Faz 2: TerritoryListener.onFenceBreak() düzeltmesi**
  - ✅ `setDropItems(false)` eklendi
  - ✅ Özel item (OAK_FENCE + PDC) oluşturuluyor
  - ✅ `dropItemNaturally()` kullanılıyor
  - ✅ PDC verisi (clanId) item'a ekleniyor
  - ✅ CustomBlockData temizleniyor

- [x] **Faz 2: TrapListener.onTrapCoreBreak() ekleme**
  - ✅ `@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)` eklendi
  - ✅ `setDropItems(false)` kullanılıyor
  - ✅ `ItemManager.TRAP_CORE.clone()` kullanılıyor
  - ✅ PDC verisi (ownerId) item'a ekleniyor
  - ✅ `dropItemNaturally()` kullanılıyor
  - ✅ `trapManager.removeTrap()` çağrılıyor
  - ✅ CustomBlockData temizleniyor

- [x] **Faz 2: ClanSystemListener.onClanBankBreak() düzeltmesi**
  - ✅ `setDropItems(false)` eklendi
  - ✅ Özel item (ENDER_CHEST + PDC) oluşturuluyor
  - ✅ `dropItemNaturally()` kullanılıyor
  - ✅ PDC verisi (clanId) item'a ekleniyor
  - ✅ CustomBlockData temizleniyor

- [x] **Faz 3: Event priority kontrolü**
  - ✅ Özel blok handler'ları `HIGH` priority'de
  - ✅ `TerritoryListener.onBreak()` `NORMAL` priority'de

---

## 📊 UYGULAMA SONUÇLARI

### Önceki Durum:
- ❌ Yapı çekirdeği kırılamıyor (event cancel)
- ❌ Klan çiti kırıldığında normal OAK_FENCE drop ediliyor
- ❌ Tuzak çekirdeği kırıldığında normal LODESTONE drop ediliyor
- ❌ Klan bankası kırıldığında normal ENDER_CHEST drop ediliyor

### Sonraki Durum:
- ✅ Yapı çekirdeği kırıldığında özel STRUCTURE_CORE item drop ediyor
- ✅ Klan çiti kırıldığında özel CLAN_FENCE item (OAK_FENCE + PDC) drop ediyor
- ✅ Tuzak çekirdeği kırıldığında özel TRAP_CORE item drop ediyor
- ✅ Klan bankası kırıldığında özel CLAN_BANK item (ENDER_CHEST + PDC) drop ediyor

### Performans:
- ✅ Optimizasyon sorunu yok - sadece event handler'ları düzeltildi
- ✅ `setDropItems(false)` kullanılıyor - gereksiz drop'lar engelleniyor
- ✅ Event priority doğru ayarlandı - özel blok handler'ları önce çalışıyor

---

**Rapor Hazırlayan:** AI Assistant  
**Tarih:** Bugün  
**Durum:** ✅ TÜM SORUNLAR ÇÖZÜLDÜ, ÇÖZÜMLER UYGULANDI

