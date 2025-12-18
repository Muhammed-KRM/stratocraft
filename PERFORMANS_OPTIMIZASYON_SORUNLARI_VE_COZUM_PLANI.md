# PERFORMANS OPTİMİZASYON SORUNLARI VE ÇÖZÜM PLANI

## 📋 İÇİNDEKİLER

1. [Genel Bakış](#genel-bakış)
2. [Tespit Edilen Sorunlar](#tespit-edilen-sorunlar)
3. [Detaylı Sorun Analizi](#detaylı-sorun-analizi)
4. [Çözüm Önerileri](#çözüm-önerileri)
5. [İnternet Araştırması ve Karşılaştırma](#internet-araştırması-ve-karşılaştırma)
6. [Uygulama Planı](#uygulama-planı)
7. [Kaynaklar](#kaynaklar)

---

## GENEL BAKIŞ

Bu doküman, Stratocraft plugin'inde zaman geçtikçe artan performans sorunlarının tespiti ve çözüm önerilerini içerir. Sorunlar özellikle veri yazma/yedekleme, veritabanı optimizasyonu ve memory leak'lerle ilgilidir.

### Sorun Özeti

- ⚠️ **Zaman geçtikçe artan performans sorunları** (memory leak benzeri)
- ⚠️ **Veri yazma/yedekleme sırasında sorunlar**
- ⚠️ **Eski kayıtların silinmemesi**
- ⚠️ **SQLite veritabanı optimizasyon sorunları**
- ⚠️ **Geçici dosyaların temizlenmemesi**

---

## TESPİT EDİLEN SORUNLAR

### 1. ⚠️ **KRİTİK: Geçici Dosyaların Temizlenmemesi**

**Sorun:**
- `atomicWrite()` metodunda `.tmp` ve `.old` dosyaları oluşturuluyor
- Hata durumlarında veya normal akışta bazı geçici dosyalar temizlenmeyebilir
- Zaman geçtikçe disk alanı dolabilir ve I/O performansı düşebilir

**Kod İncelemesi:**
```java
// DataManager.java - atomicWrite()
File tempFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");
File oldFile = new File(targetFile.getParentFile(), targetFile.getName() + ".old");
```

**Etki:**
- Disk alanı tükenmesi
- Dosya sistemi performans düşüşü
- Backup klasöründe gereksiz dosyalar

---

### 2. ⚠️ **KRİTİK: SQLite WAL Checkpoint Periyodik Yapılmıyor**

**Sorun:**
- WAL (Write-Ahead Logging) checkpoint sadece `close()` metodunda yapılıyor
- Uzun süreli çalışmada WAL dosyası büyüyebilir
- WAL dosyası büyüdükçe performans düşer

**Kod İncelemesi:**
```java
// DatabaseManager.java - close()
stmt.execute("PRAGMA wal_checkpoint(FULL);");
```

**Etki:**
- WAL dosyası büyümesi (örneğin 100MB+)
- Veritabanı performans düşüşü
- Disk I/O artışı

---

### 3. ⚠️ **YÜKSEK ÖNCELİK: Eski Verilerin Silinmemesi**

**Sorun:**
- SQLite'da `DELETE FROM` kullanılıyor ama eski veriler (tamamlanmış kontratlar, eski tuzaklar, pasif ittifaklar) silinmiyor
- Veritabanı büyümesi
- Query performansı düşüşü

**Kod İncelemesi:**
```java
// SQLiteDataManager.java
deleteStmt.execute("DELETE FROM contracts"); // Tüm kontratları sil, eski verileri tutmuyor
```

**Etki:**
- Veritabanı dosya boyutu artışı
- Index performansı düşüşü
- Query sürelerinin artması

---

### 4. ⚠️ **YÜKSEK ÖNCELİK: Connection Pool Eksikliği**

**Sorun:**
- Tek bir connection kullanılıyor (singleton pattern)
- Uzun süreli çalışmada connection timeout veya hata olabilir
- Connection leak riski

**Kod İncelemesi:**
```java
// DatabaseManager.java
private Connection connection; // Tek connection
```

**Etki:**
- Connection timeout hataları
- Veritabanı lock sorunları
- Performans düşüşü

---

### 5. ⚠️ **ORTA ÖNCELİK: Memory Leak Riski (Snapshot Cache)**

**Sorun:**
- Snapshot'lar büyük `ArrayList`/`HashMap`'ler içeriyor
- Snapshot'lar oluşturulduktan sonra temizlenmiyor
- GC (Garbage Collection) pressure

**Kod İncelemesi:**
```java
// DataManager.java
ClanSnapshot clanSnapshot = createClanSnapshot(clanManager); // Büyük liste
// Snapshot kullanıldıktan sonra temizlenmiyor
```

**Etki:**
- Memory kullanımı artışı
- GC sürelerinin uzaması
- Sunucu lag'leri

---

### 6. ⚠️ **ORTA ÖNCELİK: Auto-Save Çok Sık**

**Sorun:**
- Auto-save 5 dakikada bir yapılıyor (varsayılan)
- Her auto-save'de tüm veriler snapshot alınıyor ve kaydediliyor
- Disk I/O yükü

**Kod İncelemesi:**
```java
// DataManager.java
private long autoSaveInterval = 300000L; // 5 dakika
```

**Etki:**
- Disk I/O artışı
- CPU kullanımı artışı
- Sunucu lag'leri (özellikle büyük veri setlerinde)

---

### 7. ⚠️ **DÜŞÜK ÖNCELİK: Backup Temizleme Eksikliği**

**Sorun:**
- Backup temizleme sadece `createBackup()` metodunda yapılıyor
- Eğer backup oluşturulmazsa eski backup'lar birikir
- Disk alanı tükenmesi

**Kod İncelemesi:**
```java
// DataManager.java
cleanupOldBackups(backupFolder, baseName, extension); // Sadece createBackup()'ta çağrılıyor
```

**Etki:**
- Disk alanı tükenmesi
- Backup klasöründe gereksiz dosyalar

---

### 8. ⚠️ **DÜŞÜK ÖNCELİK: Cache Temizleme Eksikliği**

**Sorun:**
- Bazı cache'ler (örneğin snapshot cache'leri) temizlenmiyor
- Oyuncu çıkışında bazı cache'ler temizlenmiyor
- Memory leak riski

**Kod İncelemesi:**
```java
// DataManager.java - Snapshot cache'leri yok ama snapshot'lar temizlenmiyor
```

**Etki:**
- Memory kullanımı artışı
- GC pressure

---

## DETAYLI SORUN ANALİZİ

### 1. Geçici Dosya Sorunu

**Akış Şeması:**
```
atomicWrite() çağrılıyor
  ├─▶ .tmp dosyası oluşturuluyor
  ├─▶ Veri .tmp dosyasına yazılıyor
  ├─▶ .old dosyası oluşturuluyor (varsa)
  ├─▶ .tmp dosyası hedef dosyaya taşınıyor
  └─▶ Hata durumunda .tmp dosyası silinmeyebilir ❌
```

**Sorun:**
- Windows'ta dosya kilitlenmesi durumunda `.tmp` dosyası silinemeyebilir
- `.old` dosyası her zaman silinmiyor
- Crash durumunda geçici dosyalar kalabilir

---

### 2. SQLite WAL Checkpoint Sorunu

**Akış Şeması:**
```
Veri yazılıyor
  ├─▶ WAL modu aktif
  ├─▶ Veriler WAL dosyasına yazılıyor
  ├─▶ WAL dosyası büyüyor
  └─▶ Checkpoint sadece close()'da yapılıyor ❌
```

**Sorun:**
- WAL dosyası sürekli büyüyor
- Checkpoint yapılmadığı için WAL dosyası ana veritabanına yazılmıyor
- Performans düşüşü

---

### 3. Eski Veri Sorunu

**Akış Şeması:**
```
Veri kaydediliyor
  ├─▶ DELETE FROM table (tüm veriler siliniyor)
  ├─▶ Yeni veriler INSERT ediliyor
  └─▶ Eski veriler (tamamlanmış kontratlar vb.) silinmiyor ❌
```

**Sorun:**
- Tamamlanmış kontratlar silinmiyor
- Eski tuzaklar silinmiyor
- Pasif ittifaklar silinmiyor
- Veritabanı büyümesi

---

## ÇÖZÜM ÖNERİLERİ

### 1. ✅ Geçici Dosya Temizleme Sistemi

**Çözüm:**
- Periyodik geçici dosya temizleme task'ı
- Plugin başlangıcında eski geçici dosyaları temizle
- Hata durumunda geçici dosyaları temizle

**Kod:**
```java
// DataManager.java
private void cleanupTempFiles() {
    File dataFolder = new File(plugin.getDataFolder(), "data");
    File[] tempFiles = dataFolder.listFiles((dir, name) -> 
        name.endsWith(".tmp") || name.endsWith(".old"));
    
    if (tempFiles != null) {
        long now = System.currentTimeMillis();
        long maxAge = 24 * 60 * 60 * 1000; // 24 saat
        
        for (File tempFile : tempFiles) {
            if (now - tempFile.lastModified() > maxAge) {
                tempFile.delete();
            }
        }
    }
}
```

**Uygulama:**
- Plugin başlangıcında `cleanupTempFiles()` çağrılacak
- Periyodik task (her 1 saatte bir) eklenecek
- `atomicWrite()` metodunda hata durumunda temizleme yapılacak

---

### 2. ✅ SQLite WAL Checkpoint Periyodik Yapılması

**Çözüm:**
- Periyodik WAL checkpoint task'ı (her 10 dakikada bir)
- WAL dosya boyutu kontrolü
- Otomatik checkpoint

**Kod:**
```java
// DatabaseManager.java
private BukkitTask walCheckpointTask;

public void startWalCheckpointTask() {
    walCheckpointTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                // PASSIVE checkpoint (non-blocking)
                stmt.execute("PRAGMA wal_checkpoint(PASSIVE);");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("WAL checkpoint hatası: " + e.getMessage());
        }
    }, 12000L, 12000L); // Her 10 dakikada bir (12000 tick)
}

public void stopWalCheckpointTask() {
    if (walCheckpointTask != null) {
        walCheckpointTask.cancel();
        walCheckpointTask = null;
    }
}
```

**Uygulama:**
- `DatabaseManager` constructor'ında `startWalCheckpointTask()` çağrılacak
- `onDisable()`'da `stopWalCheckpointTask()` çağrılacak
- FULL checkpoint sadece `close()`'da yapılacak

---

### 3. ✅ Eski Verilerin Otomatik Silinmesi

**Çözüm:**
- Periyodik eski veri temizleme task'ı
- Tamamlanmış kontratlar (30 günden eski)
- Eski tuzaklar (30 günden eski)
- Pasif ittifaklar (30 günden eski)

**Kod:**
```java
// SQLiteDataManager.java
public void cleanupOldData() throws SQLException {
    Connection conn = databaseManager.getConnection();
    long cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 gün
    
    try (PreparedStatement stmt = conn.prepareStatement(
        "DELETE FROM contracts WHERE delivered = 1 AND created_at < ?")) {
        stmt.setTimestamp(1, new Timestamp(cutoffTime));
        int deleted = stmt.executeUpdate();
        if (deleted > 0) {
            plugin.getLogger().info("Eski kontratlar temizlendi: " + deleted);
        }
    }
    
    // Diğer tablolar için benzer işlemler...
}
```

**Uygulama:**
- Periyodik task (her 24 saatte bir)
- Config'den temizleme süresi ayarlanabilir
- Log kaydı

---

### 4. ✅ Connection Pool Sistemi (Opsiyonel)

**Çözüm:**
- HikariCP gibi connection pool kütüphanesi kullanılabilir
- Veya basit connection pool implementasyonu

**Not:** SQLite için genellikle tek connection yeterlidir, ancak uzun süreli çalışmada connection yenileme gerekebilir.

**Kod:**
```java
// DatabaseManager.java
private void refreshConnectionIfNeeded() throws SQLException {
    if (connection != null && connection.isClosed()) {
        connection = null; // Yeni connection oluşturulacak
    }
    
    // Connection yaşı kontrolü (24 saat)
    if (connection != null && connectionAge > 24 * 60 * 60 * 1000) {
        connection.close();
        connection = null;
    }
}
```

---

### 5. ✅ Snapshot Cache Temizleme

**Çözüm:**
- Snapshot'lar kullanıldıktan sonra null yapılacak
- GC'ye yardımcı olmak için explicit null assignment

**Kod:**
```java
// DataManager.java - saveAll()
try {
    // Snapshot'lar oluşturuluyor
    ClanSnapshot clanSnapshot = createClanSnapshot(clanManager);
    // ... diğer snapshot'lar
    
    // Snapshot'lar kullanılıyor
    // ...
    
} finally {
    // Snapshot'ları temizle
    clanSnapshot = null;
    contractSnapshot = null;
    // ... diğer snapshot'lar
}
```

---

### 6. ✅ Auto-Save Optimizasyonu

**Çözüm:**
- Auto-save interval'ı artır (10 dakika)
- Incremental save (sadece değişen veriler)
- Config'den ayarlanabilir

**Kod:**
```java
// DataManager.java
private long autoSaveInterval = 600000L; // 10 dakika (varsayılan)

// Config'den:
autoSaveInterval = config.getLong("data-manager.auto-save-interval", 600000L);
```

---

### 7. ✅ Backup Temizleme Periyodik Yapılması

**Çözüm:**
- Periyodik backup temizleme task'ı
- Plugin başlangıcında eski backup'ları temizle

**Kod:**
```java
// DataManager.java
private void cleanupOldBackupsPeriodic() {
    File backupFolder = new File(dataFolder, BACKUP_FOLDER);
    File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".json") || name.endsWith(".db"));
    
    if (backupFiles != null) {
        for (File backupFile : backupFiles) {
            // Her dosya tipi için ayrı temizleme
            String baseName = backupFile.getName().substring(0, backupFile.getName().lastIndexOf('.'));
            String extension = backupFile.getName().substring(backupFile.getName().lastIndexOf('.'));
            cleanupOldBackups(backupFolder, baseName, extension);
        }
    }
}
```

---

### 8. ✅ Cache Temizleme İyileştirmesi

**Çözüm:**
- Oyuncu çıkışında tüm ilgili cache'ler temizlenecek
- Periyodik cache temizleme task'ı

**Kod:**
```java
// DataManager.java
public void onPlayerQuit(UUID playerId) {
    // İlgili cache'leri temizle
    // ...
}
```

---

## İNTERNET ARAŞTIRMASI VE KARŞILAŞTIRMA

### 1. SQLite WAL Checkpoint Best Practices

**Araştırma Sonuçları:**
- **SQLite dokümantasyonu:** WAL checkpoint'in periyodik yapılması öneriliyor
- **Minecraft plugin best practices:** Her 5-10 dakikada bir PASSIVE checkpoint yapılması öneriliyor
- **Performans:** PASSIVE checkpoint non-blocking, FULL checkpoint blocking

**Karşılaştırma:**
- ✅ **Önerilen Çözüm:** Her 10 dakikada bir PASSIVE checkpoint
- ✅ **Uygulama:** `DatabaseManager`'a periyodik task eklenecek

---

### 2. Geçici Dosya Temizleme Best Practices

**Araştırma Sonuçları:**
- **Java best practices:** Geçici dosyaların otomatik temizlenmesi öneriliyor
- **Minecraft plugin best practices:** Plugin başlangıcında ve periyodik olarak temizleme yapılması öneriliyor
- **Windows specific:** Dosya kilitlenmesi durumunda retry mekanizması gerekli

**Karşılaştırma:**
- ✅ **Önerilen Çözüm:** Plugin başlangıcında + periyodik temizleme (her 1 saatte bir)
- ✅ **Uygulama:** `DataManager`'a `cleanupTempFiles()` metodu eklenecek

---

### 3. Eski Veri Temizleme Best Practices

**Araştırma Sonuçları:**
- **Database best practices:** Eski verilerin periyodik temizlenmesi öneriliyor
- **Minecraft plugin best practices:** 30 günden eski verilerin temizlenmesi öneriliyor
- **Performans:** DELETE işlemi index'leri günceller, VACUUM gerekebilir

**Karşılaştırma:**
- ✅ **Önerilen Çözüm:** Her 24 saatte bir eski veri temizleme (30 günden eski)
- ✅ **Uygulama:** `SQLiteDataManager`'a `cleanupOldData()` metodu eklenecek

---

### 4. Memory Leak Prevention Best Practices

**Araştırma Sonuçları:**
- **Java best practices:** Büyük objelerin explicit null yapılması öneriliyor
- **Minecraft plugin best practices:** Oyuncu çıkışında tüm cache'lerin temizlenmesi öneriliyor
- **GC optimization:** Explicit null assignment GC'ye yardımcı olur

**Karşılaştırma:**
- ✅ **Önerilen Çözüm:** Snapshot'lar kullanıldıktan sonra null yapılacak
- ✅ **Uygulama:** `DataManager.saveAll()` metodunda finally bloğunda temizleme

---

### 5. Auto-Save Optimization Best Practices

**Araştırma Sonuçları:**
- **Minecraft plugin best practices:** Auto-save interval'ı 10-15 dakika arasında öneriliyor
- **Disk I/O optimization:** Incremental save öneriliyor (sadece değişen veriler)
- **Performance:** Çok sık auto-save disk I/O yükü oluşturur

**Karşılaştırma:**
- ✅ **Önerilen Çözüm:** Auto-save interval'ı 10 dakikaya çıkarılacak
- ✅ **Uygulama:** Config'den ayarlanabilir yapılacak

---

## UYGULAMA PLANI

### FAZE 1: Kritik Sorunlar (Öncelik: YÜKSEK)

1. ✅ **Geçici Dosya Temizleme**
   - `DataManager.cleanupTempFiles()` metodu eklenecek
   - Plugin başlangıcında çağrılacak
   - Periyodik task (her 1 saatte bir)

2. ✅ **SQLite WAL Checkpoint**
   - `DatabaseManager.startWalCheckpointTask()` metodu eklenecek
   - Her 10 dakikada bir PASSIVE checkpoint
   - `onDisable()`'da durdurulacak

3. ✅ **Eski Veri Temizleme**
   - `SQLiteDataManager.cleanupOldData()` metodu eklenecek
   - Periyodik task (her 24 saatte bir)
   - Config'den temizleme süresi ayarlanabilir

---

### FAZE 2: Orta Öncelikli Sorunlar

4. ✅ **Snapshot Cache Temizleme**
   - `DataManager.saveAll()` metodunda finally bloğunda temizleme
   - Explicit null assignment

5. ✅ **Auto-Save Optimizasyonu**
   - Auto-save interval'ı 10 dakikaya çıkarılacak
   - Config'den ayarlanabilir

6. ✅ **Backup Temizleme Periyodik**
   - Periyodik backup temizleme task'ı
   - Plugin başlangıcında eski backup'ları temizle

---

### FAZE 3: Düşük Öncelikli Sorunlar

7. ✅ **Cache Temizleme İyileştirmesi**
   - Oyuncu çıkışında tüm ilgili cache'ler temizlenecek
   - Periyodik cache temizleme task'ı

8. ✅ **Connection Pool (Opsiyonel)**
   - Connection yenileme mekanizması
   - Connection yaşı kontrolü

---

## KAYNAKLAR

### 1. SQLite WAL Checkpoint
- **SQLite Dokümantasyonu:** https://www.sqlite.org/wal.html
- **WAL Checkpoint Best Practices:** https://www.sqlite.org/pragma.html#pragma_wal_checkpoint

### 2. Java Geçici Dosya Yönetimi
- **Java File I/O Best Practices:** https://docs.oracle.com/javase/tutorial/essential/io/
- **Windows File Locking:** https://docs.microsoft.com/en-us/windows/win32/fileio/file-locking

### 3. Minecraft Plugin Performance Optimization
- **Bukkit Performance Best Practices:** https://bukkit.fandom.com/wiki/Plugin_Tutorial
- **Paper Performance Optimization:** https://docs.papermc.io/paper/reference/paper-global-configuration

### 4. Memory Leak Prevention
- **Java Memory Management:** https://www.oracle.com/java/technologies/javase/gc-tuning-6.html
- **Minecraft Plugin Memory Leak Prevention:** https://www.spigotmc.org/wiki/memory-leaks/

### 5. Database Optimization
- **SQLite Performance Tuning:** https://www.sqlite.org/performance.html
- **Database Cleanup Best Practices:** https://www.sqlite.org/lang_vacuum.html

---

## SONUÇ

Tüm tespit edilen sorunlar için çözüm önerileri sunulmuştur. Öncelik sırasına göre uygulama yapılmalıdır:

1. **Kritik Sorunlar:** Geçici dosya temizleme, WAL checkpoint, eski veri temizleme
2. **Orta Öncelikli Sorunlar:** Snapshot cache temizleme, auto-save optimizasyonu, backup temizleme
3. **Düşük Öncelikli Sorunlar:** Cache temizleme iyileştirmesi, connection pool

Bu çözümler uygulandığında, zaman geçtikçe artan performans sorunları çözülecek ve sunucu daha stabil çalışacaktır.

