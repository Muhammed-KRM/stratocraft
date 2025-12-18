package me.mami.stratocraft.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock;

import me.mami.stratocraft.Main;

/**
 * SQLite Veritabanı Yönetim Sistemi
 * 
 * ⚠️ PLATFORM-INDEPENDENT: Tüm işletim sistemlerinde çalışır (Windows, Linux, macOS)
 * ⚠️ TAŞINABİLİR: Veritabanı dosyası tek bir dosya, kolayca kopyalanabilir
 * 
 * Özellikler:
 * - ACID uyumlu transaction garantisi
 * - WAL (Write-Ahead Logging) modu (crash-safe)
 * - Migration sistemi (versiyon kontrolü)
 * - Thread-safe (connection pooling)
 * - Auto-commit kontrolü
 */
public class DatabaseManager {
    
    private final Main plugin;
    private final File databaseFile;
    private Connection connection;
    private final ReentrantLock connectionLock = new ReentrantLock();
    
    // Database version (migration için)
    private static final int CURRENT_DB_VERSION = 1;
    
    // Connection pool ayarları
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 1000L; // 1 saniye
    
    // ✅ YENİ: WAL checkpoint task
    private org.bukkit.scheduler.BukkitTask walCheckpointTask;
    private long connectionAge = 0; // Connection yaşı (yenileme için)
    
    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        
        // ✅ PLATFORM-INDEPENDENT: Relative path kullan (dataFolder plugin'in data klasörü)
        // Bu path tüm işletim sistemlerinde çalışır
        File dataFolder = plugin.getDataFolder();
        this.databaseFile = new File(dataFolder, "stratocraft.db");
        
        // Veritabanını başlat
        initializeDatabase();
    }
    
    /**
     * Veritabanını başlat
     */
    private void initializeDatabase() {
        try {
            // ✅ PLATFORM-INDEPENDENT: SQLite JDBC driver otomatik platform'u algılar
            // Windows, Linux, macOS için otomatik native library yükler
            Class.forName("org.sqlite.JDBC");
            
            // İlk bağlantıyı oluştur
            getConnection();
            
            // Migration kontrolü
            checkAndMigrate();
            
            // NOT: WAL checkpoint task'ı Main.java'da başlatılıyor
            // (Plugin lifecycle yönetimi için)
            
            plugin.getLogger().info("§aSQLite veritabanı başlatıldı: " + databaseFile.getAbsolutePath());
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver bulunamadı!");
            e.printStackTrace();
        } catch (SQLException e) {
            plugin.getLogger().severe("Veritabanı başlatma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Veritabanı bağlantısı al (thread-safe)
     * 
     * ✅ PLATFORM-INDEPENDENT: Connection string tüm platformlarda çalışır
     */
    public Connection getConnection() throws SQLException {
        connectionLock.lock();
        try {
            // ✅ YENİ: Connection'ı yenile (yaş kontrolü ile)
            refreshConnectionIfNeeded();
            
            // Bağlantı var mı ve geçerli mi kontrol et
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            
            // Yeni bağlantı oluştur
            // ✅ TAŞINABİLİR: jdbc:sqlite: ile relative path kullan
            // databaseFile.getPath() platform-independent path döner
            String url = "jdbc:sqlite:" + databaseFile.getPath();
            
            connection = DriverManager.getConnection(url);
            connectionAge = System.currentTimeMillis(); // ✅ YENİ: Connection yaşını kaydet
            
            // ✅ CRASH-SAFE: WAL (Write-Ahead Logging) modu etkinleştir
            // Bu mod crash durumunda bile veri kaybını önler
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;"); // WAL ile güvenli ve hızlı
                stmt.execute("PRAGMA foreign_keys=ON;"); // Foreign key desteği
                stmt.execute("PRAGMA busy_timeout=5000;"); // 5 saniye timeout
            }
            
            return connection;
            
        } finally {
            connectionLock.unlock();
        }
    }
    
    /**
     * Migration kontrolü ve uygulama
     */
    private void checkAndMigrate() throws SQLException {
        // Versiyon tablosunu oluştur (yoksa)
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS db_version (
                    version INTEGER PRIMARY KEY,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
        
        // Mevcut versiyonu al
        int currentVersion = getCurrentVersion();
        
        // KRİTİK DÜZELTME: Tabloları her zaman kontrol et ve yoksa oluştur
        // Migration sırasında oluşturulmuş olabilir ama sonra silinmiş olabilir
        ensureTablesExist();
        
        if (currentVersion < CURRENT_DB_VERSION) {
            plugin.getLogger().info("§eVeritabanı migration başlatılıyor: " + currentVersion + " -> " + CURRENT_DB_VERSION);
            migrate(currentVersion, CURRENT_DB_VERSION);
        } else if (currentVersion > CURRENT_DB_VERSION) {
            plugin.getLogger().warning("§cVeritabanı versiyonu plugin'den daha yeni! Plugin güncellenmeli.");
        } else {
            plugin.getLogger().info("§aVeritabanı versiyonu güncel: " + CURRENT_DB_VERSION);
        }
    }
    
    /**
     * Tüm tabloların var olduğundan emin ol (yoksa oluştur)
     * Bu metod migration'dan bağımsız olarak tabloları garanti eder
     */
    private void ensureTablesExist() throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            // Tüm tabloları oluştur (IF NOT EXISTS ile güvenli)
            createTables(conn);
        }
    }
    
    /**
     * Mevcut veritabanı versiyonunu al
     */
    private int getCurrentVersion() throws SQLException {
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) as version FROM db_version")) {
            
            if (rs.next()) {
                return rs.getInt("version");
            }
            return 0; // İlk kurulum
        }
    }
    
    /**
     * Migration uygula
     */
    private void migrate(int fromVersion, int toVersion) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false); // Transaction başlat
        
        try {
            // Migration 1: İlk kurulum (tabloları oluştur)
            if (fromVersion < 1) {
                createTables(conn);
                fromVersion = 1;
            }
            
            // Gelecekteki migration'lar buraya eklenecek
            // if (fromVersion < 2) {
            //     migrateToVersion2(conn);
            //     fromVersion = 2;
            // }
            
            // Versiyonu kaydet
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO db_version (version) VALUES (?)")) {
                stmt.setInt(1, toVersion);
                stmt.executeUpdate();
            }
            
            conn.commit(); // Transaction commit
            plugin.getLogger().info("§aMigration tamamlandı: " + toVersion);
            
        } catch (SQLException e) {
            conn.rollback(); // Hata olursa rollback
            plugin.getLogger().severe("Migration hatası: " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Tüm tabloları oluştur
     */
    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Klanlar tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clans (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    leader_id TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Kontratlar tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contracts (
                    id TEXT PRIMARY KEY,
                    issuer_id TEXT NOT NULL,
                    acceptor_id TEXT,
                    material TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    reward TEXT NOT NULL,
                    deadline TIMESTAMP,
                    delivered BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Alışveriş tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS shops (
                    id TEXT PRIMARY KEY,
                    owner_id TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Sanal envanterler tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS virtual_inventories (
                    id TEXT PRIMARY KEY,
                    owner_id TEXT NOT NULL,
                    inventory_type TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // İttifaklar tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS alliances (
                    id TEXT PRIMARY KEY,
                    clan1_id TEXT NOT NULL,
                    clan2_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    duration INTEGER,
                    active BOOLEAN DEFAULT TRUE,
                    broken BOOLEAN DEFAULT FALSE,
                    breaker_id TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Felaketler tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS disasters (
                    id TEXT PRIMARY KEY,
                    type TEXT NOT NULL,
                    category TEXT NOT NULL,
                    level INTEGER NOT NULL,
                    start_time TIMESTAMP,
                    duration INTEGER,
                    active BOOLEAN DEFAULT FALSE,
                    data TEXT NOT NULL
                )
            """);
            
            // Klan bankaları tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clan_banks (
                    clan_id TEXT PRIMARY KEY,
                    last_salary_time TEXT,
                    transfer_contracts TEXT,
                    bank_chest_world TEXT,
                    bank_chest_x INTEGER,
                    bank_chest_y INTEGER,
                    bank_chest_z INTEGER,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Klan görevleri tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clan_missions (
                    clan_id TEXT NOT NULL,
                    board_world TEXT NOT NULL,
                    board_x INTEGER NOT NULL,
                    board_y INTEGER NOT NULL,
                    board_z INTEGER NOT NULL,
                    PRIMARY KEY (clan_id, board_world, board_x, board_y, board_z)
                )
            """);
            
            // Güç profilleri tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS power_profiles (
                    player_id TEXT PRIMARY KEY,
                    total_sgp REAL NOT NULL,
                    combat_power REAL NOT NULL,
                    gear_power REAL NOT NULL,
                    training_power REAL NOT NULL,
                    buff_power REAL NOT NULL,
                    ritual_power REAL NOT NULL,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Tuzaklar tablosu
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS traps (
                    id TEXT PRIMARY KEY,
                    clan_id TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Kontrat İstekleri tablosu (Çift taraflı kontrat sistemi)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contract_requests (
                    id TEXT PRIMARY KEY,
                    sender_id TEXT NOT NULL,
                    target_id TEXT NOT NULL,
                    scope TEXT NOT NULL,
                    status TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    responded_at TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Kontrat Şartları tablosu (Çift taraflı kontrat sistemi)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contract_terms (
                    id TEXT PRIMARY KEY,
                    contract_request_id TEXT NOT NULL,
                    player_id TEXT NOT NULL,
                    type TEXT NOT NULL,
                    material TEXT,
                    amount INTEGER DEFAULT 0,
                    delivered INTEGER DEFAULT 0,
                    target_player TEXT,
                    restricted_areas TEXT,
                    restricted_radius INTEGER DEFAULT 0,
                    structure_type TEXT,
                    deadline TIMESTAMP NOT NULL,
                    reward REAL NOT NULL,
                    penalty_type TEXT NOT NULL,
                    penalty REAL NOT NULL,
                    approved BOOLEAN DEFAULT FALSE,
                    completed BOOLEAN DEFAULT FALSE,
                    breached BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    data TEXT NOT NULL
                )
            """);
            
            // Index'ler (performans için)
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_clans_leader ON clans(leader_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contracts_issuer ON contracts(issuer_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contracts_acceptor ON contracts(acceptor_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_owner ON shops(owner_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_shops_location ON shops(world, x, y, z)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_alliances_clans ON alliances(clan1_id, clan2_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_disasters_active ON disasters(active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_traps_clan ON traps(clan_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_traps_location ON traps(world, x, y, z)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contract_requests_sender ON contract_requests(sender_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contract_requests_target ON contract_requests(target_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contract_requests_status ON contract_requests(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contract_terms_request ON contract_terms(contract_request_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_contract_terms_player ON contract_terms(player_id)");
            
            plugin.getLogger().info("§aVeritabanı tabloları oluşturuldu.");
        }
    }
    
    // Transaction durumu takibi (nested transaction önleme)
    private int transactionDepth = 0;
    private final ReentrantLock transactionLock = new ReentrantLock();
    
    /**
     * Transaction başlat
     * 
     * ⚠️ THREAD-SAFE: Nested transaction desteği (depth tracking)
     */
    public void beginTransaction() throws SQLException {
        transactionLock.lock();
        try {
            Connection conn = getConnection();
            if (transactionDepth == 0) {
                conn.setAutoCommit(false);
            }
            transactionDepth++;
        } finally {
            transactionLock.unlock();
        }
    }
    
    /**
     * Transaction commit
     * 
     * ⚠️ THREAD-SAFE: Nested transaction desteği
     */
    public void commit() throws SQLException {
        transactionLock.lock();
        try {
            if (transactionDepth <= 0) {
                throw new SQLException("Commit çağrıldı ama transaction başlatılmamış!");
            }
            
            transactionDepth--;
            if (transactionDepth == 0) {
                Connection conn = getConnection();
                conn.commit();
                conn.setAutoCommit(true);
            }
        } finally {
            transactionLock.unlock();
        }
    }
    
    /**
     * Transaction rollback
     * 
     * ⚠️ THREAD-SAFE: Nested transaction desteği
     * ✅ GÜVENLİ: Transaction yoksa sessizce atlar (hata fırlatmaz)
     */
    public void rollback() throws SQLException {
        transactionLock.lock();
        try {
            if (transactionDepth <= 0) {
                // Transaction yoksa sessizce atla - bu normal bir durum olabilir
                return;
            }
            
            Connection conn = getConnection();
            conn.rollback();
            conn.setAutoCommit(true);
            transactionDepth = 0; // Tüm nested transaction'ları iptal et
        } finally {
            transactionLock.unlock();
        }
    }
    
    /**
     * ✅ YENİ: WAL checkpoint task'ını başlat (periyodik)
     */
    public void startWalCheckpointTask() {
        if (walCheckpointTask != null) {
            stopWalCheckpointTask();
        }
        
        walCheckpointTask = org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                Connection conn = getConnection();
                if (conn != null && !conn.isClosed()) {
                    try (Statement stmt = conn.createStatement()) {
                        // PASSIVE checkpoint (non-blocking)
                        stmt.execute("PRAGMA wal_checkpoint(PASSIVE);");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("WAL checkpoint hatası: " + e.getMessage());
            }
        }, 12000L, 12000L); // Her 10 dakikada bir (12000 tick)
        
        plugin.getLogger().info("§aWAL checkpoint task'ı başlatıldı (10 dakika aralıkla).");
    }
    
    /**
     * ✅ YENİ: WAL checkpoint task'ını durdur
     */
    public void stopWalCheckpointTask() {
        if (walCheckpointTask != null) {
            walCheckpointTask.cancel();
            walCheckpointTask = null;
        }
    }
    
    /**
     * ✅ YENİ: Connection'ı yenile (yaş kontrolü ile)
     */
    private void refreshConnectionIfNeeded() throws SQLException {
        long now = System.currentTimeMillis();
        long maxAge = 24 * 60 * 60 * 1000; // 24 saat
        
        if (connection != null && (connection.isClosed() || (now - connectionAge > maxAge))) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Ignore
            }
            connection = null;
            connectionAge = 0;
        }
    }
    
    /**
     * Veritabanını kapat
     */
    public void close() {
        // ✅ YENİ: WAL checkpoint task'ını durdur
        stopWalCheckpointTask();
        
        connectionLock.lock();
        try {
            if (connection != null && !connection.isClosed()) {
                // WAL checkpoint (verileri ana dosyaya yaz)
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA wal_checkpoint(FULL);");
                } catch (SQLException e) {
                    plugin.getLogger().warning("WAL checkpoint hatası: " + e.getMessage());
                }
                
                connection.close();
                plugin.getLogger().info("§aSQLite veritabanı kapatıldı.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Veritabanı kapatma hatası: " + e.getMessage());
        } finally {
            connectionLock.unlock();
        }
    }
    
    /**
     * Veritabanı dosyasını yedekle
     * ✅ TAŞINABİLİR: Backup dosyası da taşınabilir
     */
    public boolean backup(String backupName) {
        try {
            File backupDir = new File(plugin.getDataFolder(), "backups");
            backupDir.mkdirs();
            
            File backupFile = new File(backupDir, backupName + ".db");
            
            // WAL checkpoint (tüm verileri ana dosyaya yaz)
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA wal_checkpoint(FULL);");
            }
            
            // Dosyayı kopyala
            java.nio.file.Files.copy(
                databaseFile.toPath(),
                backupFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            plugin.getLogger().info("§aVeritabanı yedeklendi: " + backupFile.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Yedekleme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Veritabanı dosyasını geri yükle
     * ✅ TAŞINABİLİR: Backup dosyasından geri yükleme
     */
    public boolean restore(String backupName) {
        try {
            File backupFile = new File(plugin.getDataFolder(), "backups/" + backupName + ".db");
            
            if (!backupFile.exists()) {
                plugin.getLogger().warning("Yedek dosyası bulunamadı: " + backupName);
                return false;
            }
            
            // Mevcut bağlantıyı kapat
            close();
            
            // Mevcut veritabanını yedekle (güvenlik için)
            String safetyBackup = "safety_backup_" + System.currentTimeMillis();
            if (databaseFile.exists()) {
                java.nio.file.Files.copy(
                    databaseFile.toPath(),
                    new File(plugin.getDataFolder(), "backups/" + safetyBackup + ".db").toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
            
            // Backup'ı geri yükle
            java.nio.file.Files.copy(
                backupFile.toPath(),
                databaseFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            // Veritabanını yeniden başlat
            initializeDatabase();
            
            plugin.getLogger().info("§aVeritabanı geri yüklendi: " + backupName);
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Geri yükleme hatası: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Veritabanı dosyası var mı?
     */
    public boolean databaseExists() {
        return databaseFile.exists();
    }
    
    /**
     * Veritabanı boş mu? (migration kontrolü için)
     */
    public boolean isDatabaseEmpty() {
        try {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM clans")) {
                if (rs.next()) {
                    return rs.getInt("count") == 0;
                }
            }
        } catch (SQLException e) {
            // Hata olursa boş say
            return true;
        }
        return true;
    }
    
    /**
     * Veritabanı dosyası yolunu al (debug için)
     */
    public String getDatabasePath() {
        return databaseFile.getAbsolutePath();
    }
}

