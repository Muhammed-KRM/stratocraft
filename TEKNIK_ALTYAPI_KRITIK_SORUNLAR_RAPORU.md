# 🔧 TEKNİK ALTYAPI KRİTİK SORUNLAR RAPORU

## 📋 İÇİNDEKİLER

1. [Veri Kaybı Riski](#veri-kaybı-riski)
2. [Main Thread Tıkanıklığı](#main-thread-tıkanıklığı)
3. [Memory Leak Riskleri](#memory-leak-riskleri)
4. [Dupe (Eşya Kopyalama) Açıkları](#dupe-açıkları)
5. [Config Reload Sorunu](#config-reload-sorunu)
6. [Çözüm Planı](#çözüm-planı)

---

## 🚨 1. VERİ KAYBI RİSKİ {#veri-kaybı-riski}

### ⚠️ **KRİTİK ÖNCELİK**

### Mevcut Durum

**Dosya:** `src/main/java/me/mami/stratocraft/manager/DataManager.java`

**Sorun:**
- Veriler JSON dosyalarına yazılıyor (`clans.json`, `contracts.json`, vb.)
- Async kayıt var ama **crash durumunda veri kaybı riski yüksek**
- `saveAll()` metodu snapshot alıyor ama dosyaya yazma sırasında crash olursa veri kaybolur

**Kod Analizi:**
```java
// DataManager.java - Satır 291-363
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    // File locking ile
    if (!saveLock.tryLock()) {
        plugin.getLogger().warning("§eVeri kaydetme devam ediyor, atlandı...");
        return;
    }
    
    try {
        // Tüm dosyalara yazma işlemi
        writeClanSnapshot(clanSnapshot);
        writeContractSnapshot(contractSnapshot);
        // ... diğer dosyalar
    } catch (Exception e) {
        errors.add(e);
    }
});
```

**Risk Senaryosu:**
1. Oyuncular 3 saat savaştı, bölgeler el değiştirdi
2. `saveAll()` çağrıldı, snapshot alındı
3. Async thread dosyaya yazmaya başladı
4. **Sunucu crash oldu** (elektrik kesintisi, hata, vb.)
5. Dosya yazma işlemi yarıda kaldı
6. Sunucu tekrar açıldığında **son kayıttan önceki veriler yüklenir**
7. **3 saatlik emek kaybolur**

### Çözüm: SQLite Geçişi

**Neden SQLite?**
- ✅ **ACID Uyumlu**: Transaction garantisi (all-or-nothing)
- ✅ **Anında Kayıt**: Her işlem anında veritabanına yazılır
- ✅ **Crash Güvenli**: WAL (Write-Ahead Logging) modu ile crash'te bile veri kaybı olmaz
- ✅ **Performans**: JSON'dan daha hızlı (indexleme, sorgulama)
- ✅ **Küçük**: Tek dosya, kolay yedekleme

**Geçiş Planı:**
1. SQLite wrapper sınıfı oluştur (`DatabaseManager.java`)
2. Mevcut JSON dosyalarını SQLite'a migrate et (tek seferlik script)
3. `DataManager`'ı SQLite kullanacak şekilde güncelle
4. Her işlemde transaction kullan (beginTransaction → commit)

**Örnek Kod:**
```java
// DatabaseManager.java (YENİ)
public class DatabaseManager {
    private Connection connection;
    
    public void saveClan(Clan clan) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT OR REPLACE INTO clans (id, name, data) VALUES (?, ?, ?)")) {
            connection.setAutoCommit(false); // Transaction başlat
            
            stmt.setString(1, clan.getId().toString());
            stmt.setString(2, clan.getName());
            stmt.setString(3, gson.toJson(clan));
            
            stmt.executeUpdate();
            connection.commit(); // İşlem tamamlandı, kaydet
            
        } catch (SQLException e) {
            connection.rollback(); // Hata olursa geri al
            throw new RuntimeException(e);
        }
    }
}
```

**Öncelik:** 🔴 **ÇOK YÜKSEK** - Veri kaybı oyuncu deneyimini yok eder

---

## 🐌 2. MAIN THREAD TIKANIKLIĞI {#main-thread-tıkanıklığı}

### ⚠️ **YÜKSEK ÖNCELİK**

### Mevcut Durum

**Dosya:** `src/main/java/me/mami/stratocraft/manager/StructureValidator.java`

**Sorun:**
- `validate()` metodu **main thread'de** çalışıyor
- Binlerce blok kontrolü yapıyor (schematic dosyasındaki her blok için)
- Büyük yapılar için **1-2 saniye lag spike** oluşturabilir

**Kod Analizi:**
```java
// StructureValidator.java - Satır 18-57
public boolean validate(Location centerBlock, String schematicName) {
    // ... dosya okuma (I/O - main thread'de!)
    
    for (BlockVector3 vec : clipboard.getRegion()) {
        // Her blok için dünyadan okuma (main thread'de!)
        Block worldBlock = centerBlock.clone().add(relX, relY, relZ).getBlock();
        String worldMaterial = worldBlock.getType().name();
        
        if (!worldMaterial.equals(schemaMaterial)) {
            return false;
        }
    }
    return true;
}
```

**Risk Senaryosu:**
1. Oyuncu devasa bir `Nexus` yapısı kurdu (1000+ blok)
2. Shift+Sağ tık yaptı, yapı doğrulaması başladı
3. `validate()` metodu **main thread'de** çalışıyor
4. 1000 blok kontrol ediliyor, her biri için dünyadan okuma yapılıyor
5. **1-2 saniye boyunca sunucu donuyor**
6. Tüm oyuncular lag yaşıyor, chat yazamıyor, hareket edemiyor

**Diğer Riskli Yerler:**
- `StratocraftPowerSystem.calculatePlayerProfile()` - Ağır hesaplamalar (cache var ama yine de riskli)
- `StructureBuilder.buildStructure()` - Büyük yapılar için lag spike

### Çözüm: Async İşlemler

**Yapılacaklar:**
1. `StructureValidator.validate()` metodunu async yap
2. Validasyon sonucunu callback ile bildir
3. Validasyon sırasında oyuncuya "Kontrol ediliyor..." mesajı göster

**Örnek Kod:**
```java
// StructureValidator.java (GÜNCELLENMİŞ)
public void validateAsync(Location centerBlock, String schematicName, 
                         Consumer<Boolean> callback) {
    // Async thread'de çalıştır
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        boolean result = validateInternal(centerBlock, schematicName);
        
        // Sonucu main thread'e bildir
        Bukkit.getScheduler().runTask(plugin, () -> {
            callback.accept(result);
        });
    });
}

private boolean validateInternal(Location centerBlock, String schematicName) {
    // Mevcut validate() metodunun içeriği (async thread'de çalışır)
    // ...
}
```

**Öncelik:** 🟠 **YÜKSEK** - Lag spike oyuncu deneyimini bozar

---

## 💾 3. MEMORY LEAK RİSKLERİ {#memory-leak-riskleri}

### ⚠️ **ORTA ÖNCELİK**

### Mevcut Durum

**Dosya:** `src/main/java/me/mami/stratocraft/manager/BatteryParticleManager.java`

**İyi Haber:** ✅ `BatteryParticleManager` iyi yazılmış
- Task'lar `cancel()` ediliyor
- Oyuncu çıkışında `stopAllBatteryParticles()` çağrılıyor
- ConcurrentHashMap kullanılıyor (thread-safe)

**Kontrol Edilmesi Gerekenler:**
1. Diğer particle manager'lar (`NewBatteryManager`, vb.)
2. Tüm `BukkitTask` başlatılan yerler
3. `InventoryCloseEvent` listener'ları (menü kapanınca task'lar duruyor mu?)

**Risk Senaryosu:**
1. Oyuncu batarya koydu, particle efektleri başladı
2. Oyuncu oyundan çıktı
3. `PlayerQuitEvent` listener'ı `stopAllBatteryParticles()` çağırmadı
4. Particle task'ları **sonsuza kadar** çalışmaya devam eder
5. 1 hafta sonra binlerce hayalet task RAM'i doldurur
6. Sunucu çöker

### Çözüm: Task Yönetimi Sistemi

**Yapılacaklar:**
1. Tüm task'ları merkezi bir `TaskManager` ile yönet
2. Oyuncu çıkışında otomatik temizlik
3. Periyodik task audit (kontrol)

**Örnek Kod:**
```java
// TaskManager.java (YENİ)
public class TaskManager {
    private final Map<UUID, Set<BukkitTask>> playerTasks = new ConcurrentHashMap<>();
    
    public void registerPlayerTask(UUID playerId, BukkitTask task) {
        playerTasks.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet())
                   .add(task);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        Set<BukkitTask> tasks = playerTasks.remove(playerId);
        if (tasks != null) {
            tasks.forEach(BukkitTask::cancel);
        }
    }
}
```

**Öncelik:** 🟡 **ORTA** - Uzun vadede sorun olur

---

## 🎭 4. DUPE (EŞYA KOPYALAMA) AÇIKLARI {#dupe-açıkları}

### ⚠️ **YÜKSEK ÖNCELİK**

### Mevcut Durum

**Dosya:** `src/main/java/me/mami/stratocraft/manager/clan/ClanBankSystem.java`

**Sorun:**
- `depositItem()` metodunda **race condition** riski var
- Önce bankaya ekliyor, sonra envanterden alıyor
- Arada crash/disconnect olursa item hem bankada hem envanterde kalabilir

**Kod Analizi:**
```java
// ClanBankSystem.java - Satır 236-254
// 1. Önce bankaya ekle
HashMap<Integer, ItemStack> overflow = bankChest.addItem(depositItem);

if (!overflow.isEmpty()) {
    // Sandık dolu, geri ver
    return false;
}

// 2. Sonra envanterden al
player.getInventory().removeItem(toRemove);
```

**Risk Senaryosu:**
1. Oyuncu bankaya 64 elmas koydu
2. `addItem()` başarılı oldu, elmas bankaya eklendi
3. **Oyuncu internetini kesti** (disconnect)
4. `removeItem()` çalışmadı (oyuncu offline)
5. Elmas hem bankada hem oyuncunun envanterinde (dupe!)

**Diğer Riskli Yerler:**
- `ShopManager.handlePurchase()` - İyi görünüyor (transaction mantığı var)
- `ClanBankMenu.depositAllItems()` - Kontrol edilmeli

### Çözüm: Transaction Mantığı

**Yapılacaklar:**
1. **Önce envanterden al**, sonra bankaya ekle
2. Eğer bankaya ekleme başarısız olursa, item'i geri ver
3. `InventoryCloseEvent` listener'ında pending transaction'ları kontrol et

**Örnek Kod:**
```java
// ClanBankSystem.java (GÜNCELLENMİŞ)
public boolean depositItem(Player player, ItemStack item, int amount) {
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
        // Banka dolu, item'i geri ver (rollback)
        HashMap<Integer, ItemStack> refundResult = player.getInventory().addItem(toRemove);
        if (!refundResult.isEmpty()) {
            // Envanter dolu, yere düşür
            player.getWorld().dropItemNaturally(player.getLocation(), toRemove);
        }
        return false;
    }
    
    // 3. İŞLEM BAŞARILI
    return true;
}
```

**Öncelik:** 🟠 **YÜKSEK** - Dupe exploit oyun ekonomisini bozar

---

## 🔄 5. CONFIG RELOAD SORUNU {#config-reload-sorunu}

### ⚠️ **DÜŞÜK ÖNCELİK**

### Mevcut Durum

**Kontrol Edilmesi Gerekenler:**
- `Main.java`'da reload komutu var mı?
- Config reload atıldığında cache'ler temizleniyor mu?
- `LangManager` gibi cache kullanan sistemler reload'u destekliyor mu?

**Risk Senaryosu:**
1. Admin `config.yml`'de klan kurma ücretini değiştirdi
2. `/stratocraft reload` yazdı
3. Config dosyası yeniden yüklendi
4. Ama `ClanManager` içindeki cache temizlenmedi
5. Eski ücret kullanılmaya devam eder

### Çözüm: Reload Sistemi

**Yapılacaklar:**
1. `Main.java`'da reload komutu ekle
2. Tüm manager'larda `reload()` metodu oluştur
3. Reload atıldığında tüm cache'leri temizle

**Örnek Kod:**
```java
// Main.java
@EventHandler
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("stratocraft")) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Config reload
            reloadConfig();
            configManager.reload();
            
            // Cache temizleme
            if (stratocraftPowerSystem != null) {
                stratocraftPowerSystem.clearCache();
            }
            if (clanManager != null) {
                clanManager.clearCache();
            }
            // ... diğer manager'lar
            
            sender.sendMessage("§aConfig yeniden yüklendi!");
            return true;
        }
    }
    return false;
}
```

**Öncelik:** 🟢 **DÜŞÜK** - Kritik değil ama iyi olur

---

## 📊 ÇÖZÜM PLANI {#çözüm-planı}

### Öncelik Sırası

1. **🔴 ÇOK YÜKSEK: Veri Kaybı Riski**
   - SQLite geçişi
   - Süre: 2-3 gün
   - Etki: Veri kaybını önler, oyuncu emeğini korur

2. **🟠 YÜKSEK: Main Thread Tıkanıklığı**
   - StructureValidator async yap
   - Süre: 1 gün
   - Etki: Lag spike'ları önler, oyun akışını korur

3. **🟠 YÜKSEK: Dupe Açıkları**
   - Transaction mantığı ekle
   - Süre: 1 gün
   - Etki: Exploit'leri önler, ekonomi dengesini korur

4. **🟡 ORTA: Memory Leak Riskleri**
   - TaskManager sistemi
   - Süre: 1 gün
   - Etki: Uzun vadede sunucu stabilitesini korur

5. **🟢 DÜŞÜK: Config Reload**
   - Reload sistemi
   - Süre: 0.5 gün
   - Etki: Admin deneyimini iyileştirir

### Uygulama Sırası

**Hafta 1:**
- ✅ SQLite geçişi (DatabaseManager oluştur, migrate script yaz)
- ✅ StructureValidator async yap

**Hafta 2:**
- ✅ Dupe açıklarını kapat
- ✅ TaskManager sistemi

**Hafta 3:**
- ✅ Config reload sistemi
- ✅ Test ve optimizasyon

---

## 📝 SONUÇ

Bu 5 kritik sorun, oyuncu özgürlüğüne dokunmadan teknik altyapıyı güçlendirecek. Özellikle **veri kaybı** ve **lag spike** sorunları oyuncu deneyimini doğrudan etkiler ve öncelikli olarak çözülmelidir.

**Özgürlük Felsefesi:** Bu düzeltmeler oyuncuların özgürlüğünü kısıtlamaz, aksine oyunun teknik olarak ayakta kalmasını sağlar. Oyuncular hala istedikleri gibi oynayabilir, sadece teknik sorunlar çözülür.

