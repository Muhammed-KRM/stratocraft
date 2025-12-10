# DATA PERSISTENCE ENTEGRASYON REHBERİ

## ✅ TAMAMLANAN ÖZELLİKLER

### 1. ✅ Atomic Write Sistemi
- **Lokasyon:** `DataManager.java` - `atomicWrite()` metodu
- **Açıklama:** Geçici dosyaya yaz, başarılı olursa rename ile taşı
- **Kullanım:** Tüm `write*Snapshot()` metodları artık `atomicWrite()` kullanıyor

### 2. ✅ Backup/Restore Sistemi
- **Lokasyon:** `DataManager.java` - `createBackup()`, `restoreFromBackup()`, `listBackups()`
- **Açıklama:** Her kayıt öncesi otomatik backup, son 5 backup saklanır
- **Admin Komutu:** `/stratocraft data restore <dosya>` ve `/stratocraft data list <dosya>`

### 3. ✅ Error Recovery Sistemi
- **Lokasyon:** `DataManager.java` - `saveAll()` metodunda hata kontrolü
- **Açıklama:** Hata durumunda backup'tan geri yükleme önerisi

### 4. ✅ Data Validation
- **Lokasyon:** `DataManager.java` - `isValidUUID()`, `isValidLocation()`, `safeJsonParse()`
- **Açıklama:** UUID format, Location world, JSON corruption kontrolü
- **Kullanım:** Tüm `load*()` metodlarında validation yapılıyor

### 5. ✅ Transaction Support
- **Lokasyon:** `DataManager.java` - `saveAll()` metodunda error tracking
- **Açıklama:** Her dosya için ayrı try-catch, hata durumunda loglama

### 6. ✅ Scheduled Auto-Save
- **Lokasyon:** `DataManager.java` - `startAutoSave()`, `stopAutoSave()`
- **Config:** `config.yml` - `data-manager.auto-save-enabled` ve `auto-save-interval`
- **Entegrasyon:** `Main.java` - `onEnable()` ve `onDisable()`

### 7. ✅ File Locking
- **Lokasyon:** `DataManager.java` - `ReentrantLock saveLock`
- **Açıklama:** Async save sırasında race condition önleme

### 8. ✅ Versioning
- **Lokasyon:** `DataManager.java` - `DATA_VERSION = 1`
- **Açıklama:** Mission ve Trap dosyalarında version kontrolü

### 9. ✅ Corruption Detection
- **Lokasyon:** `DataManager.java` - `safeJsonParse()` metodu
- **Açıklama:** JSON parse hatalarını yakalar, backup önerisi yapar

---

## 📋 ENTEGRASYON NOKTALARI

### 1. Main.java Entegrasyonu

#### onEnable() içinde:
```java
// Veri yükleme (yeni sistemlerle)
dataManager.loadAll(clanManager, contractManager, shopManager, virtualStorageListener, 
        allianceManager, disasterManager, clanBankSystem, clanMissionSystem, clanActivitySystem, trapManager);

// Periyodik otomatik kayıt başlat
if (dataManager != null) {
    dataManager.startAutoSave(() -> {
        // Auto-save callback: Tüm verileri kaydet (async)
        dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, 
                allianceManager, disasterManager, clanBankSystem, clanMissionSystem, 
                clanActivitySystem, trapManager, false);
        return null;
    });
}
```

#### onDisable() içinde:
```java
// Periyodik otomatik kayıt durdur
if (dataManager != null) {
    dataManager.stopAutoSave();
}

// Veri kaydetme (forceSync = true)
dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, 
        allianceManager, disasterManager, clanBankSystem, clanMissionSystem, 
        clanActivitySystem, trapManager, true);
```

#### Getter Metodu:
```java
public DataManager getDataManager() {
    return dataManager;
}
```

---

### 2. Config.yml Ayarları

```yaml
# Data Manager Ayarları
data-manager:
  auto-save-enabled: true          # Periyodik otomatik kayıt aktif mi?
  auto-save-interval: 300000       # Otomatik kayıt aralığı (ms) - 5 dakika
```

---

### 3. Admin Komutları

#### Komut: `/stratocraft data restore <dosya>`
- **Açıklama:** Backup'tan geri yükleme
- **Örnek:** `/stratocraft data restore clans.json`
- **Lokasyon:** `AdminCommandExecutor.java` - `handleDataManager()`

#### Komut: `/stratocraft data list <dosya>`
- **Açıklama:** Backup'ları listeleme
- **Örnek:** `/stratocraft data list clans.json`
- **Lokasyon:** `AdminCommandExecutor.java` - `handleDataManager()`

#### Tab Completion:
- `AdminCommandExecutor.java` - `onTabComplete()` metodunda `"data"` eklendi

---

## 🔧 KULLANIM ÖRNEKLERİ

### Manuel Kayıt (Async):
```java
dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, 
        allianceManager, disasterManager, clanBankSystem, clanMissionSystem, 
        clanActivitySystem, trapManager, false);
```

### Manuel Kayıt (Sync - onDisable için):
```java
dataManager.saveAll(clanManager, contractManager, shopManager, virtualStorageListener, 
        allianceManager, disasterManager, clanBankSystem, clanMissionSystem, 
        clanActivitySystem, trapManager, true);
```

### Backup'tan Geri Yükleme:
```java
boolean success = dataManager.restoreFromBackup("clans.json");
if (success) {
    // Sunucuyu yeniden başlat
}
```

### Backup Listeleme:
```java
List<String> backups = dataManager.listBackups("clans.json");
for (String backup : backups) {
    System.out.println(backup);
}
```

---

## ⚠️ ÖNEMLİ NOTLAR

1. **Auto-Save Callback:** `startAutoSave()` metoduna callback verilmeli, aksi halde çalışmaz
2. **Force Sync:** `onDisable()`'da her zaman `forceSync = true` kullanılmalı
3. **Backup Klasörü:** `plugins/Stratocraft/backups/` klasörü otomatik oluşturulur
4. **Max Backups:** Son 5 backup saklanır, eski backup'lar otomatik silinir
5. **File Locking:** Async save sırasında `tryLock()` kullanılır, başarısız olursa atlanır

---

## 🐛 HATA AYIKLAMA

### Backup Bulunamadı:
- `backups/` klasörünü kontrol et
- Dosya adının doğru olduğundan emin ol (`.json` uzantısı ile)

### Auto-Save Çalışmıyor:
- `config.yml`'de `auto-save-enabled: true` olduğundan emin ol
- `Main.java`'da `startAutoSave()` çağrıldığından emin ol
- Callback'in doğru verildiğinden emin ol

### Veri Kaybı:
- `backups/` klasöründen en son backup'ı kontrol et
- `/stratocraft data list <dosya>` ile backup'ları listele
- `/stratocraft data restore <dosya>` ile geri yükle

---

## 📊 PERFORMANS

- **Atomic Write:** Dosya bozulmasını önler, minimal performans etkisi
- **Backup:** Her kayıt öncesi kopyalama, disk kullanımı artar (son 5 backup)
- **Auto-Save:** Config'den ayarlanabilir interval (default: 5 dakika)
- **File Locking:** Async save sırasında race condition önleme, minimal overhead

---

## ✅ TAMAMLANAN TÜM ÖZELLİKLER

1. ✅ Atomic Write
2. ✅ Backup/Restore
3. ✅ Error Recovery
4. ✅ Data Validation
5. ✅ Transaction Support
6. ✅ Scheduled Auto-Save
7. ✅ File Locking
8. ✅ Versioning
9. ✅ Corruption Detection

**Tüm özellikler başarıyla entegre edildi!** 🎉

