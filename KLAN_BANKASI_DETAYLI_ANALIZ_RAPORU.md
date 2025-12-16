# 🏦 Klan Bankası Detaylı Analiz Raporu

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Sistem Mimarisi](#sistem-mimarisi)
3. [Tüm Özellikler ve Fonksiyonlar](#tüm-özellikler-ve-fonksiyonlar)
4. [Sorunlar ve Hatalar](#sorunlar-ve-hatalar)
5. [Eksik Özellikler](#eksik-özellikler)
6. [Kod Kalitesi Analizi](#kod-kalitesi-analizi)

---

## 🎯 GENEL BAKIŞ

Klan bankası sistemi **iki farklı sistem** içeriyor:

1. **Eski Sistem (Para Bazlı):** `RitualInteractionListener.java` - Altın yatırma/çekme
2. **Yeni Sistem (Item Bazlı):** `ClanBankSystem.java` - Item yatırma/çekme, maaş, transfer kontratları

**Durum:** ⚠️ **İKİ SİSTEM BİRLİKTE ÇALIŞIYOR** - Bu karışıklığa neden olabilir!

---

## 🏗️ SİSTEM MİMARİSİ

### Ana Dosyalar

1. **`ClanBankSystem.java`** - Ana sistem (Item bazlı)
2. **`ClanBankMenu.java`** - GUI menüsü
3. **`ClanBankConfig.java`** - Config yönetimi
4. **`RitualInteractionListener.java`** - Eski para sistemi (satır 1118-1197)
5. **`Clan.java`** - `bankBalance`, `deposit()`, `withdraw()` metodları

### Bağımlılıklar

- `ClanManager` - Klan yönetimi
- `ClanRankSystem` - Yetki kontrolü
- `ClanBankConfig` - Config yönetimi

---

## 📦 TÜM ÖZELLİKLER VE FONKSİYONLAR

### 1. ✅ KLAN BANKASI OLUŞTURMA

**Dosya:** `ClanBankSystem.java:69-106`

**Fonksiyon:** `createBankChest(Player player, Location chestLoc)`

**İşlev:**
1. Ender Chest kontrolü yapar
2. Klan üyeliği kontrolü yapar
3. Yetki kontrolü yapar (Lider veya General)
4. Item Frame'de "KLAN_BANKASI" Name Tag kontrolü yapar
5. Metadata ekler (`ClanBank`)
6. Konumu kaydeder (`bankChestLocations`)

**Kod:**
```java
public boolean createBankChest(Player player, Location chestLoc) {
    // Ender Chest kontrolü
    if (block.getType() != Material.ENDER_CHEST) {
        return false;
    }
    
    // Yetki kontrolü
    if (!rankSystem.hasPermission(clan, player.getUniqueId(), 
            ClanRankSystem.Permission.MANAGE_BANK)) {
        return false;
    }
    
    // Name Tag kontrolü
    boolean hasBankTag = checkForBankTag(chestLoc);
    
    // Metadata ekle
    block.setMetadata("ClanBank", new FixedMetadataValue(plugin, clan.getId().toString()));
    
    // Konumu kaydet
    bankChestLocations.put(clan.getId(), chestLoc);
}
```

**Sorunlar:**
- ⚠️ **Name Tag kontrolü zor:** Item Frame'de Name Tag kontrolü yapılıyor ama bu çok hassas
- ⚠️ **Metadata kalıcı değil:** Sunucu restart'ta metadata kaybolur, konum kaydedilmeli

**Durum:** ✅ Çalışıyor ama iyileştirilebilir

---

### 2. ✅ BANKAYA ERİŞİM (GUI)

**Dosya:** `ClanBankMenu.java:50-115`, `ClanSystemListener.java:161-195`

**Fonksiyon:** `openMainMenu(Player player)`

**İşlev:**
1. Ana menüyü açar (27 slot)
2. Banka sandığı butonu gösterir
3. Maaş bilgisi gösterir
4. Transfer kontratları butonu gösterir
5. Bilgi butonu gösterir

**Erişim Yolları:**
- `ClanSystemListener.java:173-181` - Ender Chest'e sağ tık (metadata kontrolü)
- `StructureMenuListener.java:177-178` - Yapı menüsünden

**Kod:**
```java
// ClanSystemListener.java:173-181
if (bankSystem != null && clicked.getType() == Material.ENDER_CHEST) {
    if (clicked.hasMetadata("ClanBank")) {
        event.setCancelled(true);
        plugin.getClanBankMenu().openMainMenu(player);
    }
}
```

**Sorunlar:**
- ❌ **Metadata kontrolü:** Ender Chest metadata'sı restart'ta kaybolur
- ⚠️ **Sadece metadata kontrolü:** Konum kontrolü yok

**Durum:** ⚠️ Kısmen çalışıyor (metadata kaybolursa çalışmaz)

---

### 3. ✅ BANKAYA ITEM YATIRMA

**Dosya:** `ClanBankSystem.java:209-267`

**Fonksiyon:** `depositItem(Player player, ItemStack item, int amount)`

**İşlev:**
1. Null check'ler yapar
2. Klan üyeliği kontrolü yapar
3. Envanter kontrolü yapar (`containsAtLeast`)
4. Banka sandığı kontrolü yapar
5. **Transaction mantığı:** Önce envanterden al, sonra bankaya ekle (dupe önleme)
6. Sandık doluysa rollback yapar

**Kod:**
```java
// 1. ÖNCE ENVANTERDEN AL (transaction başlat)
ItemStack toRemove = item.clone();
toRemove.setAmount(amount);
HashMap<Integer, ItemStack> removeResult = player.getInventory().removeItem(toRemove);

if (!removeResult.isEmpty()) {
    // Envanterden alınamadı, işlem iptal
    return false;
}

// 2. SONRA BANKAYA EKLE
ItemStack depositItem = item.clone();
depositItem.setAmount(amount);
HashMap<Integer, ItemStack> overflow = bankChest.addItem(depositItem);

if (!overflow.isEmpty()) {
    // Sandık dolu, item'i geri ver (rollback)
    player.getInventory().addItem(toRemove);
    return false;
}
```

**Kullanım:**
- `ClanBankMenu.java:318-337` - `depositAllItems()` - Tüm itemleri yatırır

**Sorunlar:**
- ✅ Transaction mantığı doğru (dupe önleme var)
- ⚠️ **Cache sorunu:** `getBankChest()` cache kullanıyor, gerçek sandık ile senkronize olmayabilir

**Durum:** ✅ Çalışıyor ama cache sorunu olabilir

---

### 4. ✅ BANKADAN ITEM ÇEKME

**Dosya:** `ClanBankSystem.java:272-329`

**Fonksiyon:** `withdrawItem(Player player, Material material, int amount)`

**İşlev:**
1. Null check'ler yapar
2. Klan üyeliği kontrolü yapar
3. **Yetki kontrolü yapar** (`hasWithdrawPermission`)
4. Banka sandığı kontrolü yapar
5. **Transaction mantığı:** Önce bankadan al, sonra envantere ekle (dupe önleme)
6. Envanter doluysa rollback yapar

**Yetki Kontrolü:**
```java
// ClanBankSystem.java:334-354
private boolean hasWithdrawPermission(Clan.Rank rank) {
    switch (rank) {
        case LEADER: return true; // Sınırsız
        case GENERAL: return config.canGeneralWithdraw(); // Config'den
        case ELITE: return config.canEliteWithdraw(); // Config'den
        case MEMBER: return config.canMemberWithdraw(); // Config'den (varsayılan: false)
        case RECRUIT: return false; // Çekme yetkisi yok
    }
}
```

**Kullanım:**
- `ClanBankMenu.java:385-428` - `onWithdrawMenuClick()` - Çekme menüsünden

**Sorunlar:**
- ✅ Transaction mantığı doğru
- ⚠️ **Cache sorunu:** Aynı cache sorunu
- ⚠️ **removeItem() kontrolü:** `removeItem()` null dönebilir, kontrol eksik

**Durum:** ✅ Çalışıyor ama cache sorunu olabilir

---

### 5. ⚠️ BANKAYA PARA YATIRMA (ESKİ SİSTEM)

**Dosya:** `RitualInteractionListener.java:1118-1197`

**Fonksiyon:** `onClanBankAccess(PlayerInteractEvent event)`

**İşlev:**
1. Shift + Sağ Tık kontrolü yapar
2. Chest kontrolü yapar (CHEST veya TRAPPED_CHEST)
3. Klan üyeliği kontrolü yapar
4. Yetki kontrolü yapar (Lider veya General)
5. **Altın ile yatırma:** Elinde Altın ile yatırır
6. **Boş el ile çekme:** Maksimum 64 altın çeker

**Kod:**
```java
// Para yatırma (Altın ile)
if (handItem != null && handItem.getType() == Material.GOLD_INGOT) {
    int amount = handItem.getAmount();
    clan.deposit(amount); // Clan.java'daki deposit() metodu
    handItem.setAmount(0);
}

// Para çekme (Boş el ile)
else if (handItem == null || handItem.getType() == Material.AIR) {
    int withdrawAmount = (int) Math.min(64, clan.getBalance());
    clan.withdraw(withdrawAmount);
    // Altın ver
    ItemStack gold = new ItemStack(Material.GOLD_INGOT, withdrawAmount);
    player.getInventory().addItem(gold);
}
```

**Sorunlar:**
- ❌ **İki sistem çakışıyor:** Yeni item sistemi ile eski para sistemi birlikte çalışıyor
- ❌ **Chest kontrolü yok:** Hangi chest'in klan bankası olduğu kontrol edilmiyor
- ❌ **Transaction yok:** Dupe riski var (önce al, sonra ekle mantığı yok)
- ❌ **Envanter dolu kontrolü eksik:** Çekme sırasında envanter doluysa yere düşer

**Durum:** ⚠️ Çalışıyor ama güvensiz ve eski sistem

---

### 6. ✅ BANKAYA ERİŞİM (SANDIK MENÜSÜ)

**Dosya:** `ClanBankMenu.java:120-158`

**Fonksiyon:** `openBankChestMenu(Player player)`

**İşlev:**
1. Banka sandığını açar (54 slot)
2. Sandık içeriğini kopyalar (0-44 slot)
3. Yatır/Çek butonları ekler (45, 46 slot)
4. Geri butonu ekler (49 slot)

**Kod:**
```java
// Sandık içeriğini kopyala
ItemStack[] contents = bankChest.getContents();
for (int i = 0; i < Math.min(contents.length, 45); i++) {
    if (contents[i] != null && contents[i].getType() != Material.AIR) {
        menu.setItem(i, contents[i].clone());
    }
}
```

**Sorunlar:**
- ❌ **Kopya sorunu:** Menü sandık içeriğinin **kopyasını** gösteriyor, gerçek sandık ile senkronize değil
- ❌ **updateBankChest() sorunu:** `updateBankChest()` metodu menüden bankaya kopyalıyor ama bu **ters mantık**
- ⚠️ **Cache sorunu:** `getBankChest()` cache'den dönen inventory, gerçek sandık değil

**Durum:** ❌ **ÇALIŞMIYOR** - Sandık içeriği senkronize değil

---

### 7. ✅ BANKAYA TÜM İTEMLERİ YATIRMA

**Dosya:** `ClanBankMenu.java:318-337`

**Fonksiyon:** `depositAllItems(Player player, Clan clan)`

**İşlev:**
1. Oyuncunun envanterindeki tüm itemleri döngüye alır
2. Her item için `depositItem()` çağırır
3. Başarılı yatırma sayısını sayar
4. Menüyü yeniler

**Kod:**
```java
for (ItemStack item : player.getInventory().getContents()) {
    if (item != null && item.getType() != Material.AIR) {
        if (bankSystem.depositItem(player, item, item.getAmount())) {
            deposited++;
        }
    }
}
```

**Sorunlar:**
- ⚠️ **Her item için ayrı çağrı:** Performans sorunu olabilir
- ⚠️ **Menü yenileme:** Menü yenileniyor ama sandık içeriği güncel olmayabilir (cache sorunu)

**Durum:** ✅ Çalışıyor ama optimize edilebilir

---

### 8. ✅ BANKADAN ITEM ÇEKME MENÜSÜ

**Dosya:** `ClanBankMenu.java:342-380`

**Fonksiyon:** `openWithdrawMenu(Player player, Clan clan)`

**İşlev:**
1. Banka içeriğini gösterir (54 slot)
2. Her item için lore ekler (miktar, tıklama seçenekleri)
3. Geri butonu ekler

**Tıklama Seçenekleri:**
- Sol Tık: Tümünü çek
- Sağ Tık: Yarısını çek
- Shift+Sol: Tek çek

**Kod:**
```java
// Banka içeriğini göster
ItemStack[] contents = bankChest.getContents();
for (ItemStack item : contents) {
    if (item != null && item.getType() != Material.AIR && slot < 45) {
        List<String> lore = new ArrayList<>();
        lore.add("§aSol Tık: §7Tümünü çek");
        lore.add("§eSağ Tık: §7Yarısını çek");
        lore.add("§cShift+Sol: §7Tek çek");
        menu.setItem(slot++, displayItem);
    }
}
```

**Sorunlar:**
- ⚠️ **Cache sorunu:** `getBankChest()` cache'den dönen inventory, güncel olmayabilir
- ⚠️ **Miktar gösterimi:** `item.getAmount()` gösteriliyor ama bu cache'den gelen miktar

**Durum:** ✅ Çalışıyor ama cache sorunu olabilir

---

### 9. ✅ BANKADAN ITEM ÇEKME (MENÜDEN)

**Dosya:** `ClanBankMenu.java:385-428`

**Fonksiyon:** `onWithdrawMenuClick(InventoryClickEvent event)`

**İşlev:**
1. Menü başlığı kontrolü yapar
2. Tıklama tipine göre miktar belirler:
   - Shift+Sol: 1 adet
   - Sağ Tık: Yarısı
   - Sol Tık: Tümü
3. `withdrawItem()` çağırır
4. Menüyü yeniler

**Kod:**
```java
if (event.isShiftClick()) {
    amount = 1; // Tek çek
} else if (event.isRightClick()) {
    amount = amount / 2; // Yarısını çek
}
// Sol tık = Tümünü çek

if (bankSystem.withdrawItem(player, material, amount)) {
    openWithdrawMenu(player, clan); // Menüyü yenile
}
```

**Sorunlar:**
- ⚠️ **Miktar hesaplama:** `clicked.getAmount()` kullanılıyor ama bu cache'den gelen miktar, gerçek miktar olmayabilir
- ⚠️ **Menü yenileme:** Menü yenileniyor ama cache güncel olmayabilir

**Durum:** ✅ Çalışıyor ama miktar hesaplama sorunu olabilir

---

### 10. ❌ BANKAYA SANDIK İÇERİĞİNİ GÜNCELLEME

**Dosya:** `ClanBankMenu.java:433-451`

**Fonksiyon:** `updateBankChest(Player player, Clan clan)`

**İşlev:**
1. Açık menüyü alır
2. Menü içeriğini banka sandığına kopyalar (0-44 slot)

**Kod:**
```java
// Sandık içeriğini güncelle
for (int i = 0; i < 45; i++) {
    ItemStack menuItem = menu.getItem(i);
    if (menuItem != null && menuItem.getType() != Material.AIR) {
        // Menüden bankaya kopyala
        bankChest.setItem(i, menuItem.clone());
    } else {
        // Boş slot
        bankChest.setItem(i, null);
    }
}
```

**Sorunlar:**
- ❌ **Ters mantık:** Menüden bankaya kopyalama yapılıyor ama bu **yanlış**
- ❌ **Cache sorunu:** `getBankChest()` cache'den dönen inventory, gerçek sandık değil
- ❌ **Senkronizasyon yok:** Gerçek sandık ile cache arasında senkronizasyon yok
- ❌ **Kullanılmıyor:** Bu metod çağrılıyor ama işe yaramıyor

**Durum:** ❌ **ÇALIŞMIYOR** - Ters mantık ve cache sorunu

---

### 11. ✅ OTOMATIK MAAŞ SİSTEMİ

**Dosya:** `ClanBankSystem.java:359-472`

**Fonksiyon:** `distributeSalaries()`, `distributeSalaryToMember()`

**İşlev:**
1. Tüm klanları döngüye alır
2. Her klan için banka sandığını kontrol eder
3. Her üye için son maaş zamanını kontrol eder
4. Maaş zamanı geldiyse maaş dağıtır
5. Config'den rütbe bazlı maaş itemleri alır
6. Bankadan item çeker ve oyuncuya verir

**Çağrılma:**
- `Main.java:1442-1443` - Scheduled task (her tick'te)

**Kod:**
```java
// Main.java:1442-1443
if (clanBankSystem != null) {
    clanBankSystem.distributeSalaries();
}
```

**Maaş Itemleri (Config'den):**
```java
// ClanBankConfig.java:64-80
// Leader: 10 Diamond + 50 Gold
// General: 5 Diamond + 25 Gold
// Elite: 15 Gold
// Member: 10 Iron
// Recruit: Maaş almaz
```

**Sorunlar:**
- ⚠️ **Rate limiting var:** Her tick'te maksimum 5 klan, her klan için maksimum 10 üye (lag önleme)
- ⚠️ **Cache sorunu:** `getBankChest()` cache kullanıyor, gerçek sandık ile senkronize olmayabilir
- ⚠️ **Offline üyeler:** Offline üyelere maaş verilmiyor (gelecekte bekleme listesi eklenebilir)
- ⚠️ **Transaction yok:** Maaş verilirken transaction mantığı yok, dupe riski var

**Durum:** ✅ Çalışıyor ama cache ve transaction sorunları var

---

### 12. ✅ OTOMATIK TRANSFER KONTRAKLARI

**Dosya:** `ClanBankSystem.java:477-620`

**Fonksiyonlar:**
- `createTransferContract()` - Kontrat oluştur
- `processTransferContracts()` - Kontratları işle
- `processTransferContract()` - Tek kontratı işle

**İşlev:**
1. Lider/General kontrat oluşturur
2. Hedef oyuncu, malzeme, miktar, aralık belirlenir
3. Scheduled task kontratları işler
4. Aralık geldiğinde bankadan item çeker ve hedef oyuncuya verir

**Kod:**
```java
// Kontrat oluştur
TransferContract contract = new TransferContract();
contract.setTargetPlayerId(targetPlayerId);
contract.setMaterial(material);
contract.setAmount(amount);
contract.setInterval(interval);
transferContracts.put(clan.getId(), contracts);
```

**Çağrılma:**
- `Main.java:1454-1455` - Scheduled task (her tick'te)

**Sorunlar:**
- ⚠️ **Rate limiting var:** Her tick'te maksimum 10 kontrat (lag önleme)
- ⚠️ **Cache sorunu:** `getBankChest()` cache kullanıyor
- ⚠️ **Transaction yok:** Dupe riski var
- ⚠️ **Offline kontrolü:** Offline oyunculara transfer yapılmıyor (atlanıyor)

**Durum:** ✅ Çalışıyor ama cache ve transaction sorunları var

---

### 13. ⚠️ BANKAYA ERİŞİM (SANDIK MENÜSÜNDEN GÜNCELLEME)

**Dosya:** `ClanBankMenu.java:259-299`

**Fonksiyon:** `handleBankChestClick(InventoryClickEvent event)`

**İşlev:**
1. Yatır/Çek butonlarına tıklama kontrolü yapar
2. Sandık slotlarına (0-44) tıklama kontrolü yapar
3. Sandık içeriğini güncellemek için `updateBankChest()` çağırır

**Kod:**
```java
// Sandık slotları (0-44) - Normal işlem
if (slot < 45) {
    // Sandık içeriğini güncelle
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        updateBankChest(player, playerClan);
    }, 1L);
}
```

**Sorunlar:**
- ❌ **updateBankChest() sorunu:** Ters mantık, menüden bankaya kopyalama yapılıyor
- ❌ **Cache sorunu:** Cache ile gerçek sandık senkronize değil
- ❌ **Event cancel edilmiyor:** Sandık slotlarına tıklama cancel edilmiyor, oyuncu item alıp verebilir

**Durum:** ❌ **ÇALIŞMIYOR** - Ters mantık ve event kontrolü eksik

---

### 14. ✅ BANKAYA ERİŞİM (GETTER)

**Dosya:** `ClanBankSystem.java:145-204`

**Fonksiyon:** `getBankChest(Clan clan)`

**İşlev:**
1. Cache kontrolü yapar (5 saniye cache)
2. Konum kontrolü yapar
3. Blok kontrolü yapar (Ender Chest)
4. Metadata kontrolü yapar
5. **Sanal inventory oluşturur** (27 slot)

**Kod:**
```java
// Cache kontrolü
Inventory cached = bankChestCache.get(clanId);
if (cached != null && cacheTime != null && 
    now - cacheTime < BANK_CHEST_CACHE_DURATION) {
    return cached;
}

// Yeni bir inventory oluştur (27 slot - ender chest boyutu)
inventory = org.bukkit.Bukkit.createInventory(null, 27, "§5Klan Bankası");
bankChestCache.put(clanId, inventory);
```

**Sorunlar:**
- ❌ **Sanal inventory:** Gerçek sandık değil, **sanal bir inventory** oluşturuluyor
- ❌ **Senkronizasyon yok:** Gerçek sandık ile sanal inventory arasında senkronizasyon yok
- ❌ **Cache süresi:** 5 saniye cache süresi çok kısa, sürekli yeni inventory oluşturuluyor
- ❌ **Gerçek sandık kullanılmıyor:** Ender Chest'in gerçek inventory'si kullanılmıyor

**Durum:** ❌ **ÇALIŞMIYOR** - Sanal inventory kullanılıyor, gerçek sandık ile bağlantı yok

---

### 15. ✅ TRANSFER KONTRAKLARI MENÜSÜ

**Dosya:** `ClanBankMenu.java:163-207`

**Fonksiyon:** `openTransferContractsMenu(Player player)`

**İşlev:**
1. Aktif kontratları listeler
2. Her kontrat için item gösterir
3. Kontrat bilgilerini lore'da gösterir (hedef, malzeme, miktar, durum)

**Kod:**
```java
for (ClanBankSystem.TransferContract contract : contracts) {
    lore.add("§7Hedef: §e" + target.getName());
    lore.add("§7Malzeme: §e" + contract.getMaterial().name());
    lore.add("§7Miktar: §e" + contract.getAmount());
    lore.add("§7Durum: §e" + (contract.isActive() ? "Aktif" : "Pasif"));
    menu.setItem(slot++, createButton(contract.getMaterial(), "§eTransfer Kontratı", lore));
}
```

**Sorunlar:**
- ✅ Çalışıyor

**Durum:** ✅ Çalışıyor

---

### 16. ✅ PARA SİSTEMİ (ESKİ)

**Dosya:** `Clan.java:29, 76-78`

**Fonksiyonlar:**
- `deposit(double amount)` - Para yatır
- `withdraw(double amount)` - Para çek
- `getBalance()` - Bakiye al

**İşlev:**
- Basit double değer tutar
- `bankBalance` field'ı kullanır

**Kullanım:**
- `RitualInteractionListener.java:1155, 1177` - Eski para sistemi

**Sorunlar:**
- ⚠️ **İki sistem:** Item sistemi ile para sistemi ayrı çalışıyor
- ⚠️ **Validasyon yok:** Negatif bakiye kontrolü yok

**Durum:** ✅ Çalışıyor

---

## 🚨 SORUNLAR VE HATALAR

### ❌ Kritik Sorunlar

1. **Sanal Inventory Sorunu**
   - **Dosya:** `ClanBankSystem.java:191-196`
   - **Sorun:** `getBankChest()` gerçek sandık yerine **sanal bir inventory** oluşturuyor
   - **Etki:** Sandık içeriği hiçbir zaman gerçek sandığa kaydedilmiyor
   - **Çözüm:** Ender Chest'in gerçek inventory'sini kullanmalı

2. **Cache Senkronizasyon Sorunu**
   - **Dosya:** `ClanBankSystem.java:141-204`
   - **Sorun:** Cache ile gerçek sandık arasında senkronizasyon yok
   - **Etki:** Sandık içeriği güncel değil
   - **Çözüm:** Cache'i kaldırmalı veya gerçek sandık ile senkronize etmeli

3. **updateBankChest() Ters Mantık**
   - **Dosya:** `ClanBankMenu.java:433-451`
   - **Sorun:** Menüden bankaya kopyalama yapılıyor (ters mantık)
   - **Etki:** Sandık içeriği güncellenmiyor
   - **Çözüm:** Bankadan menüye kopyalama yapmalı

4. **İki Sistem Çakışması**
   - **Dosya:** `RitualInteractionListener.java:1118-1197` vs `ClanBankSystem.java`
   - **Sorun:** Eski para sistemi ile yeni item sistemi birlikte çalışıyor
   - **Etki:** Karışıklık, dupe riski
   - **Çözüm:** Eski sistemi kaldırmalı veya birleştirmeli

5. **Metadata Kalıcılık Sorunu**
   - **Dosya:** `ClanBankSystem.java:99`
   - **Sorun:** Metadata restart'ta kaybolur
   - **Etki:** Restart sonrası banka erişilemez
   - **Çözüm:** Konumları database'e kaydetmeli

6. **Transaction Eksikliği (Eski Sistem)**
   - **Dosya:** `RitualInteractionListener.java:1149-1196`
   - **Sorun:** Para yatırma/çekme transaction mantığı yok
   - **Etki:** Dupe riski
   - **Çözüm:** Transaction mantığı eklemeli

7. **Event Cancel Eksikliği**
   - **Dosya:** `ClanBankMenu.java:259-299`
   - **Sorun:** Sandık slotlarına tıklama cancel edilmiyor
   - **Etki:** Oyuncu item alıp verebilir (dupe riski)
   - **Çözüm:** Event'i cancel etmeli

### ⚠️ Orta Öncelikli Sorunlar

8. **Maaş Sistemi Transaction Eksikliği**
   - **Dosya:** `ClanBankSystem.java:442-467`
   - **Sorun:** Maaş verilirken transaction mantığı yok
   - **Etki:** Dupe riski
   - **Çözüm:** Transaction mantığı eklemeli

9. **Transfer Kontratları Transaction Eksikliği**
   - **Dosya:** `ClanBankSystem.java:587-620`
   - **Sorun:** Transfer sırasında transaction mantığı yok
   - **Etki:** Dupe riski
   - **Çözüm:** Transaction mantığı eklemeli

10. **Name Tag Kontrolü Zor**
    - **Dosya:** `ClanBankSystem.java:111-135`
    - **Sorun:** Item Frame'de Name Tag kontrolü çok hassas
    - **Etki:** Kullanıcı deneyimi kötü
    - **Çözüm:** Daha basit bir aktivasyon yöntemi kullanmalı

11. **Cache Süresi Kısa**
    - **Dosya:** `ClanBankSystem.java:143`
    - **Sorun:** 5 saniye cache süresi çok kısa
    - **Etki:** Sürekli yeni inventory oluşturuluyor
    - **Çözüm:** Cache süresini artırmalı veya cache'i kaldırmalı

---

## 📊 ÖZELLİK DURUM TABLOSU

| Özellik | Durum | Dosya | Sorunlar |
|---------|-------|-------|----------|
| Banka Oluşturma | ✅ Çalışıyor | ClanBankSystem.java:69 | Metadata kalıcılık |
| Banka Erişim (GUI) | ⚠️ Kısmen | ClanBankMenu.java:50 | Metadata kontrolü |
| Item Yatırma | ✅ Çalışıyor | ClanBankSystem.java:209 | Cache sorunu |
| Item Çekme | ✅ Çalışıyor | ClanBankSystem.java:272 | Cache sorunu |
| Para Yatırma (Eski) | ⚠️ Çalışıyor | RitualInteractionListener.java:1149 | Transaction yok |
| Para Çekme (Eski) | ⚠️ Çalışıyor | RitualInteractionListener.java:1167 | Transaction yok |
| Sandık Menüsü | ❌ Çalışmıyor | ClanBankMenu.java:120 | Sanal inventory |
| Tüm Itemleri Yatır | ✅ Çalışıyor | ClanBankMenu.java:318 | Optimize edilebilir |
| Çekme Menüsü | ✅ Çalışıyor | ClanBankMenu.java:342 | Cache sorunu |
| Sandık Güncelleme | ❌ Çalışmıyor | ClanBankMenu.java:433 | Ters mantık |
| Maaş Sistemi | ✅ Çalışıyor | ClanBankSystem.java:359 | Transaction yok |
| Transfer Kontratları | ✅ Çalışıyor | ClanBankSystem.java:477 | Transaction yok |
| Transfer Menüsü | ✅ Çalışıyor | ClanBankMenu.java:163 | - |

---

## 🔧 EKSİK ÖZELLİKLER

1. **Gerçek Sandık Entegrasyonu**
   - Ender Chest'in gerçek inventory'si kullanılmıyor
   - Sanal inventory kullanılıyor

2. **Database Entegrasyonu**
   - Banka konumları database'e kaydedilmiyor
   - Metadata kullanılıyor (restart'ta kaybolur)

3. **Sandık İçeriği Persistence**
   - Sandık içeriği database'e kaydedilmiyor
   - Sadece memory'de tutuluyor

4. **Transaction Logging**
   - Yatırma/çekme işlemleri loglanmıyor
   - Audit trail yok

5. **Banka Limitleri**
   - Sandık boyutu limiti yok
   - Item limiti yok

6. **Banka Güvenliği**
   - Savaş durumunda banka erişimi kontrolü yok
   - Misafir erişimi kontrolü yok

---

## 💻 KOD KALİTESİ ANALİZİ

### ✅ İyi Yönler

1. **Transaction Mantığı (Yeni Sistem)**
   - Item yatırma/çekme transaction mantığı var
   - Dupe önleme yapılıyor

2. **Null Check'ler**
   - Tüm metodlarda null check'ler var
   - Exception handling var

3. **Thread-Safety**
   - `ConcurrentHashMap` kullanılıyor
   - Synchronized bloklar var

4. **Rate Limiting**
   - Maaş ve transfer sistemlerinde rate limiting var
   - Lag önleme yapılıyor

### ❌ Kötü Yönler

1. **Sanal Inventory Kullanımı**
   - Gerçek sandık yerine sanal inventory kullanılıyor
   - Bu büyük bir mimari hata

2. **Cache Sorunları**
   - Cache ile gerçek sandık senkronize değil
   - Cache süresi çok kısa

3. **İki Sistem Çakışması**
   - Eski ve yeni sistem birlikte çalışıyor
   - Karışıklığa neden oluyor

4. **Metadata Kullanımı**
   - Metadata kalıcı değil
   - Database kullanılmalı

5. **Event Kontrolü Eksikliği**
   - Sandık menüsünde event cancel edilmiyor
   - Dupe riski var

---

## 📝 ÖNERİLER

### Yüksek Öncelik

1. **Gerçek Sandık Entegrasyonu**
   - Ender Chest'in gerçek inventory'sini kullan
   - Sanal inventory'yi kaldır

2. **Database Entegrasyonu**
   - Banka konumlarını database'e kaydet
   - Sandık içeriğini database'e kaydet

3. **Eski Sistemi Kaldır**
   - `RitualInteractionListener.java:1118-1197` kaldır
   - Sadece yeni item sistemi kullan

4. **Event Kontrolü**
   - Sandık menüsünde event'i cancel et
   - Dupe önleme yap

### Orta Öncelik

5. **Transaction Mantığı (Maaş/Transfer)**
   - Maaş ve transfer sistemlerinde transaction mantığı ekle

6. **Cache İyileştirme**
   - Cache'i kaldır veya gerçek sandık ile senkronize et

7. **updateBankChest() Düzeltme**
   - Ters mantığı düzelt
   - Bankadan menüye kopyalama yap

---

**Son Güncelleme:** 2024
**Durum:** ⚠️ **%50 ÇALIŞIYOR** - Kritik sorunlar var

