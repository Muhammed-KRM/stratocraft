package me.mami.stratocraft.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.DataManager;

/**
 * SQLite Veri Yönetim Sistemi
 * 
 * ✅ PLATFORM-INDEPENDENT: Tüm işletim sistemlerinde çalışır
 * ✅ TAŞINABİLİR: Veritabanı dosyası tek dosya, kolayca kopyalanabilir
 * ✅ ACID UYUMLU: Transaction garantisi (all-or-nothing)
 * ✅ CRASH-SAFE: WAL modu ile crash'te bile veri kaybı olmaz
 * 
 * DataManager ile entegre çalışır, snapshot'ları SQLite'a kaydeder
 */
public class SQLiteDataManager {
    
    private final Main plugin;
    private final DatabaseManager databaseManager;
    private final Gson gson;
    private final ReentrantLock saveLock = new ReentrantLock();
    
    // ✅ YENİ: Eski veri temizleme task'ı
    private org.bukkit.scheduler.BukkitTask oldDataCleanupTask;
    
    public SQLiteDataManager(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        // NOT: Eski veri temizleme task'ı Main.java'da başlatılıyor
        // (Plugin lifecycle yönetimi için)
    }
    
    /**
     * Klan snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     * Eğer tek başına çağrılıyorsa transaction başlatılır
     */
    public void saveClanSnapshot(DataManager.ClanSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.clans == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO clans (id, name, leader_id, data) VALUES (?, ?, ?, ?)")) {
                
                for (DataManager.ClanData clan : snapshot.clans) {
                    if (clan == null) continue;
                    
                    // Leader ID'yi bul
                    String leaderId = null;
                    if (clan.members != null) {
                        for (Map.Entry<String, String> entry : clan.members.entrySet()) {
                            if ("LEADER".equalsIgnoreCase(entry.getValue())) {
                                leaderId = entry.getKey();
                                break;
                            }
                        }
                    }
                    
                    // Tüm klan verisini JSON olarak kaydet
                    String jsonData = gson.toJson(clan);
                    
                    stmt.setString(1, clan.id);
                    stmt.setString(2, clan.name != null ? clan.name : "Unknown");
                    stmt.setString(3, leaderId != null ? leaderId : "");
                    stmt.setString(4, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            databaseManager.commit();
            
        } catch (SQLException e) {
            databaseManager.rollback();
            throw e;
        } finally {
            // DÜZELTME: inTransaction=true olduğunda lock bu metod tarafından alınmadı
            // Bu yüzden unlock yapmaya çalışmak IllegalMonitorStateException'a neden olur
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Kontrat snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     * Eğer tek başına çağrılıyorsa transaction başlatılır
     */
    public void saveContractSnapshot(DataManager.ContractSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.contracts == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski kontratları sil (tam senkronizasyon için)
            try (Statement deleteStmt = conn.createStatement()) {
                deleteStmt.execute("DELETE FROM contracts");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO contracts (id, issuer_id, acceptor_id, material, amount, reward, deadline, delivered, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                for (DataManager.ContractData contract : snapshot.contracts) {
                    if (contract == null) continue;
                    
                    String jsonData = gson.toJson(contract);
                    
                    stmt.setString(1, contract.id);
                    stmt.setString(2, contract.issuer != null ? contract.issuer : "");
                    stmt.setString(3, contract.acceptor != null ? contract.acceptor : "");
                    stmt.setString(4, contract.material != null ? contract.material : "");
                    stmt.setInt(5, contract.amount);
                    stmt.setString(6, String.valueOf(contract.reward));
                    stmt.setTimestamp(7, contract.deadline != 0 ? new Timestamp(contract.deadline) : null);
                    stmt.setBoolean(8, contract.delivered > 0);
                    stmt.setString(9, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveContractSnapshot(DataManager.ContractSnapshot snapshot) throws SQLException {
        saveContractSnapshot(snapshot, false);
    }
    
    /**
     * Alışveriş snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveShopSnapshot(DataManager.ShopSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.shops == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO shops (id, owner_id, world, x, y, z, data) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                
                for (DataManager.ShopData shop : snapshot.shops) {
                    if (shop == null) continue;
                    
                    String jsonData = gson.toJson(shop);
                    
                    stmt.setString(1, shop.id);
                    stmt.setString(2, shop.ownerId != null ? shop.ownerId : shop.owner);
                    // Location kontrolü (LocationData veya fallback)
                    if (shop.locationData != null) {
                        stmt.setString(3, shop.locationData.world);
                        stmt.setInt(4, (int) shop.locationData.x);
                        stmt.setInt(5, (int) shop.locationData.y);
                        stmt.setInt(6, (int) shop.locationData.z);
                    } else if (shop.locationString != null && shop.locationString.contains(":")) {
                        // Fallback: locationString'den parse et
                        String[] parts = shop.locationString.split(":");
                        if (parts.length >= 4) {
                            stmt.setString(3, parts[0]);
                            stmt.setInt(4, Integer.parseInt(parts[1]));
                            stmt.setInt(5, Integer.parseInt(parts[2]));
                            stmt.setInt(6, Integer.parseInt(parts[3]));
                        } else {
                            continue; // Geçersiz location
                        }
                    } else {
                        continue; // Geçersiz location
                    }
                    stmt.setString(7, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveShopSnapshot(DataManager.ShopSnapshot snapshot) throws SQLException {
        saveShopSnapshot(snapshot, false);
    }
    
    /**
     * İttifak snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveAllianceSnapshot(DataManager.AllianceSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.alliances == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO alliances (id, clan1_id, clan2_id, type, duration, active, broken, breaker_id, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                for (DataManager.AllianceData alliance : snapshot.alliances) {
                    if (alliance == null) continue;
                    
                    String jsonData = gson.toJson(alliance);
                    
                    stmt.setString(1, alliance.id);
                    stmt.setString(2, alliance.clan1Id);
                    stmt.setString(3, alliance.clan2Id);
                    stmt.setString(4, alliance.type);
                    stmt.setLong(5, alliance.expiresAt - alliance.createdAt);
                    stmt.setBoolean(6, alliance.active);
                    stmt.setBoolean(7, alliance.broken);
                    stmt.setString(8, alliance.breakerClanId);
                    stmt.setString(9, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveAllianceSnapshot(DataManager.AllianceSnapshot snapshot) throws SQLException {
        saveAllianceSnapshot(snapshot, false);
    }
    
    /**
     * Felaket snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveDisasterSnapshot(DataManager.DisasterSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski felaketleri pasif yap
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("UPDATE disasters SET active = FALSE");
            }
            
            if (snapshot.disaster != null) {
                String jsonData = gson.toJson(snapshot.disaster);
                
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO disasters (id, type, category, level, start_time, duration, active, data) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    
                    stmt.setString(1, "active_disaster");
                    stmt.setString(2, snapshot.disaster.type);
                    stmt.setString(3, snapshot.disaster.category);
                    stmt.setInt(4, snapshot.disaster.level);
                    stmt.setTimestamp(5, snapshot.disaster.startTime != 0 ? 
                        new Timestamp(snapshot.disaster.startTime) : null);
                    stmt.setLong(6, snapshot.disaster.duration);
                    stmt.setBoolean(7, true);
                    stmt.setString(8, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                    stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
                }
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveDisasterSnapshot(DataManager.DisasterSnapshot snapshot) throws SQLException {
        saveDisasterSnapshot(snapshot, false);
    }
    
    /**
     * Klan bankası snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveClanBankSnapshot(DataManager.ClanBankSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.banks == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO clan_banks (clan_id, last_salary_time, transfer_contracts, " +
                "bank_chest_world, bank_chest_x, bank_chest_y, bank_chest_z, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                for (Map.Entry<String, DataManager.BankData> entry : snapshot.banks.entrySet()) {
                    if (entry.getValue() == null) continue;
                    
                    DataManager.BankData bank = entry.getValue();
                    String jsonData = gson.toJson(bank);
                    
                    // Location parse et
                    String world = null;
                    int x = 0, y = 0, z = 0;
                    if (bank.chestLocation != null && !bank.chestLocation.isEmpty()) {
                        // Location format: "world:x:y:z"
                        String[] parts = bank.chestLocation.split(":");
                        if (parts.length >= 4) {
                            world = parts[0];
                            x = Integer.parseInt(parts[1]);
                            y = Integer.parseInt(parts[2]);
                            z = Integer.parseInt(parts[3]);
                        }
                    }
                    
                    // lastSalaryTime'ı JSON string'e çevir
                    String lastSalaryTimeJson = bank.lastSalaryTime != null ? 
                        gson.toJson(bank.lastSalaryTime) : null;
                    
                    // transferContracts'ı JSON string'e çevir
                    String transferContractsJson = bank.transferContracts != null ? 
                        gson.toJson(bank.transferContracts) : null;
                    
                    stmt.setString(1, bank.clanId);
                    stmt.setString(2, lastSalaryTimeJson);
                    stmt.setString(3, transferContractsJson);
                    stmt.setString(4, world);
                    stmt.setInt(5, x);
                    stmt.setInt(6, y);
                    stmt.setInt(7, z);
                    stmt.setString(8, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveClanBankSnapshot(DataManager.ClanBankSnapshot snapshot) throws SQLException {
        saveClanBankSnapshot(snapshot, false);
    }
    
    /**
     * Klan görevleri snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveClanMissionSnapshot(DataManager.ClanMissionSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.missionBoardLocations == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski görev tahtalarını sil
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM clan_missions");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO clan_missions (clan_id, board_world, board_x, board_y, board_z) " +
                "VALUES (?, ?, ?, ?, ?)")) {
                
                for (Map.Entry<String, List<DataManager.MissionBoardLocation>> entry : 
                     snapshot.missionBoardLocations.entrySet()) {
                    if (entry.getValue() == null) continue;
                    
                    for (DataManager.MissionBoardLocation loc : entry.getValue()) {
                        if (loc == null) continue;
                        
                        stmt.setString(1, entry.getKey());
                        stmt.setString(2, loc.world);
                        stmt.setInt(3, loc.x);
                        stmt.setInt(4, loc.y);
                        stmt.setInt(5, loc.z);
                        stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                    }
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveClanMissionSnapshot(DataManager.ClanMissionSnapshot snapshot) throws SQLException {
        saveClanMissionSnapshot(snapshot, false);
    }
    
    /**
     * Tuzak snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveTrapSnapshot(DataManager.TrapSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.activeTraps == null) {
            // ✅ DÜZELTME: Hiç trap yoksa transaction başlatma
            return;
        }
        
        // ✅ DÜZELTME: Trap sayısı 0 ise transaction başlatma
        if (snapshot.activeTraps.isEmpty()) {
            return;
        }
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski tuzakları sil
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM traps");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO traps (id, clan_id, world, x, y, z, type, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                for (DataManager.TrapData trap : snapshot.activeTraps) {
                    if (trap == null) continue;
                    
                    // ✅ DÜZELTME: clan_id NOT NULL constraint hatası - null kontrolü
                    // Eğer clanId null ise, ownerClanId'den al (fallback)
                    if (trap.clanId == null || trap.clanId.isEmpty()) {
                        if (trap.ownerClanId != null && !trap.ownerClanId.isEmpty()) {
                            trap.clanId = trap.ownerClanId; // Fallback: ownerClanId'den al
                        } else {
                            plugin.getLogger().warning("Tuzak kaydedilemedi: clan_id null veya boş (trap.id: " + 
                                (trap.id != null ? trap.id : "null") + ")");
                            continue; // clan_id olmayan tuzakları atla
                        }
                    }
                    
                    // Location kontrolü (LocationData veya String formatında olabilir)
                    String world = null;
                    int x = 0, y = 0, z = 0;
                    
                    if (trap.location != null) {
                        // LocationData formatında
                        world = trap.location.world;
                        x = (int) trap.location.x;
                        y = (int) trap.location.y;
                        z = (int) trap.location.z;
                    } else if (trap.id != null && trap.id.contains(":")) {
                        // ID'den parse et (fallback)
                        String[] parts = trap.id.split(":");
                        if (parts.length >= 4) {
                            try {
                            world = parts[0];
                            x = Integer.parseInt(parts[1]);
                            y = Integer.parseInt(parts[2]);
                            z = Integer.parseInt(parts[3]);
                            } catch (NumberFormatException e) {
                                // Geçersiz format, bu tuzak kaydedilemez
                                plugin.getLogger().warning("Tuzak kaydedilemedi: Geçersiz ID formatı (trap.id: " + trap.id + ")");
                                continue;
                            }
                        }
                    }
                    
                    if (world == null) continue; // Geçersiz location
                    
                    String jsonData = gson.toJson(trap);
                    
                    stmt.setString(1, trap.id != null ? trap.id : UUID.randomUUID().toString());
                    stmt.setString(2, trap.clanId); // ✅ Artık null değil (yukarıda kontrol edildi)
                    stmt.setString(3, world);
                    stmt.setInt(4, x);
                    stmt.setInt(5, y);
                    stmt.setInt(6, z);
                    stmt.setString(7, trap.type);
                    stmt.setString(8, jsonData);
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            // ✅ DÜZELTME: Sadece transaction başlatılmışsa commit et
            if (!inTransaction) {
                // Transaction başlatılmış mı kontrol et
                try {
                    // ✅ DÜZELTME: conn zaten yukarıda tanımlı, yeniden tanımlama
                    // conn null olamaz çünkü yukarıda getConnection() çağrıldı
                    if (!conn.getAutoCommit()) {
                        databaseManager.commit();
                    }
                } catch (SQLException commitEx) {
                    // Transaction başlatılmamışsa sessizce geç
                    if (commitEx.getMessage() != null && 
                        commitEx.getMessage().contains("transaction başlatılmamış")) {
                        // Transaction başlatılmamış, bu normal (hiç trap yoksa)
                        plugin.getLogger().fine("Trap snapshot kaydı: Transaction başlatılmamış (hiç trap yok)");
                    } else {
                        throw commitEx;
                    }
                }
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                try {
                    databaseManager.rollback();
                } catch (SQLException rollbackEx) {
                    // Rollback hatası sessizce log'lanır
                    plugin.getLogger().fine("Trap snapshot rollback hatası: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveTrapSnapshot(DataManager.TrapSnapshot snapshot) throws SQLException {
        saveTrapSnapshot(snapshot, false);
    }
    
    /**
     * Sanal envanter snapshot'ını SQLite'a kaydet
     * 
     * ⚠️ NOT: Bu metod transaction içinde çağrılmalı (saveAll içinden)
     */
    public void saveInventorySnapshot(DataManager.InventorySnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.inventories == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski envanterleri sil
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM virtual_inventories");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO virtual_inventories (id, owner_id, inventory_type, data) " +
                "VALUES (?, ?, ?, ?)")) {
                
                for (Map.Entry<String, String> entry : snapshot.inventories.entrySet()) {
                    if (entry.getKey() == null || entry.getValue() == null) continue;
                    
                    // ID format: "ownerId:type" veya sadece "ownerId"
                    String[] parts = entry.getKey().split(":");
                    String ownerId = parts.length > 0 ? parts[0] : entry.getKey();
                    String inventoryType = parts.length > 1 ? parts[1] : "default";
                    
                    stmt.setString(1, entry.getKey());
                    stmt.setString(2, ownerId);
                    stmt.setString(3, inventoryType);
                    stmt.setString(4, entry.getValue());
                    stmt.addBatch(); // ✅ OPTIMIZATION: Batch insert
                }
                stmt.executeBatch(); // ✅ OPTIMIZATION: Tüm batch'i bir seferde çalıştır
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    /**
     * Overload: Varsayılan olarak transaction dışında
     */
    public void saveInventorySnapshot(DataManager.InventorySnapshot snapshot) throws SQLException {
        saveInventorySnapshot(snapshot, false);
    }
    
    /**
     * ContractRequest snapshot'ını SQLite'a kaydet
     */
    public void saveContractRequestSnapshot(DataManager.ContractRequestSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.requests == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski istekleri sil
            try (Statement deleteStmt = conn.createStatement()) {
                deleteStmt.execute("DELETE FROM contract_requests");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO contract_requests (id, sender_id, target_id, scope, status, created_at, responded_at, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                for (DataManager.ContractRequestData request : snapshot.requests) {
                    if (request == null) continue;
                    
                    String jsonData = gson.toJson(request);
                    
                    stmt.setString(1, request.id);
                    stmt.setString(2, request.sender);
                    stmt.setString(3, request.target);
                    stmt.setString(4, request.scope != null ? request.scope : "PLAYER_TO_PLAYER");
                    stmt.setString(5, request.status != null ? request.status : "PENDING");
                    stmt.setTimestamp(6, new Timestamp(request.createdAt));
                    stmt.setTimestamp(7, request.respondedAt != null ? new Timestamp(request.respondedAt) : null);
                    stmt.setString(8, jsonData);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    public void saveContractRequestSnapshot(DataManager.ContractRequestSnapshot snapshot) throws SQLException {
        saveContractRequestSnapshot(snapshot, false);
    }
    
    /**
     * ContractTerms snapshot'ını SQLite'a kaydet
     */
    public void saveContractTermsSnapshot(DataManager.ContractTermsSnapshot snapshot, boolean inTransaction) throws SQLException {
        if (snapshot == null || snapshot.terms == null) return;
        
        if (!inTransaction) {
            saveLock.lock();
            try {
                databaseManager.beginTransaction();
            } catch (SQLException e) {
                saveLock.unlock();
                throw e;
            }
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Önce tüm eski şartları sil
            try (Statement deleteStmt = conn.createStatement()) {
                deleteStmt.execute("DELETE FROM contract_terms");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO contract_terms (id, contract_request_id, player_id, type, material, amount, delivered, " +
                "target_player, restricted_areas, restricted_radius, structure_type, deadline, reward, penalty_type, " +
                "penalty, approved, completed, breached, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                for (DataManager.ContractTermsData term : snapshot.terms) {
                    if (term == null) continue;
                    
                    String jsonData = gson.toJson(term);
                    
                    stmt.setString(1, term.id);
                    stmt.setString(2, term.contractRequestId);
                    stmt.setString(3, term.playerId);
                    stmt.setString(4, term.type != null ? term.type : "RESOURCE_COLLECTION");
                    stmt.setString(5, term.material);
                    stmt.setInt(6, term.amount);
                    stmt.setInt(7, term.delivered);
                    stmt.setString(8, term.targetPlayer);
                    stmt.setString(9, term.restrictedAreas);
                    stmt.setInt(10, term.restrictedRadius);
                    stmt.setString(11, term.structureType);
                    stmt.setTimestamp(12, new Timestamp(term.deadline));
                    stmt.setDouble(13, term.reward);
                    stmt.setString(14, term.penaltyType != null ? term.penaltyType : "BANK_PENALTY");
                    stmt.setDouble(15, term.penalty);
                    stmt.setBoolean(16, term.approved);
                    stmt.setBoolean(17, term.completed);
                    stmt.setBoolean(18, term.breached);
                    stmt.setString(19, jsonData);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            
            if (!inTransaction) {
                databaseManager.commit();
            }
            
        } catch (SQLException e) {
            if (!inTransaction) {
                databaseManager.rollback();
            }
            throw e;
        } finally {
            if (!inTransaction) {
                saveLock.unlock();
            }
        }
    }
    
    public void saveContractTermsSnapshot(DataManager.ContractTermsSnapshot snapshot) throws SQLException {
        saveContractTermsSnapshot(snapshot, false);
    }
    
    /**
     * Tüm verileri SQLite'a kaydet (transaction içinde)
     * 
     * ✅ ACID UYUMLU: Tüm işlemler tek transaction içinde
     */
    public void saveAll(
            DataManager.ClanSnapshot clanSnapshot,
            DataManager.ContractSnapshot contractSnapshot,
            DataManager.ShopSnapshot shopSnapshot,
            DataManager.InventorySnapshot inventorySnapshot,
            DataManager.AllianceSnapshot allianceSnapshot,
            DataManager.DisasterSnapshot disasterSnapshot,
            DataManager.ClanBankSnapshot bankSnapshot,
            DataManager.ClanMissionSnapshot missionSnapshot,
            DataManager.TrapSnapshot trapSnapshot,
            DataManager.ContractRequestSnapshot requestSnapshot,
            DataManager.ContractTermsSnapshot termsSnapshot) throws SQLException {
        
        saveLock.lock();
        boolean transactionStarted = false;
        try {
            // ✅ ACID UYUMLU: Tüm işlemler tek transaction içinde
            // Transaction başlat (nested transaction destekleniyor)
            // ✅ DÜZELTME: Veritabanı bağlantısı kontrolü (onDisable sırasında kapatılmış olabilir)
            try {
                // Veritabanı bağlantısı kontrolü
                Connection testConn = databaseManager.getConnection();
                if (testConn == null || testConn.isClosed()) {
                    saveLock.unlock();
                    plugin.getLogger().warning("SQLite veritabanı bağlantısı kapalı, kayıt atlanıyor.");
                    return; // Bağlantı kapalıysa sessizce çık (onDisable sırasında normal)
                }
                
                databaseManager.beginTransaction();
                transactionStarted = true;
            } catch (SQLException beginEx) {
                saveLock.unlock();
                // ✅ DÜZELTME: onDisable sırasında bağlantı kapatılmış olabilir, bu normal
                // ✅ DÜZELTME: Exception fırlatma yerine return (async context'te sorun çıkmasın)
                if (beginEx.getMessage() != null && 
                    (beginEx.getMessage().contains("closed") || beginEx.getMessage().contains("Connection"))) {
                    plugin.getLogger().info("SQLite veritabanı bağlantısı kapalı, kayıt atlanıyor.");
                } else {
                    plugin.getLogger().severe("SQLite transaction başlatma hatası: " + beginEx.getMessage());
                }
                return; // ✅ DÜZELTME: Exception fırlatma yerine return (async context'te sorun çıkmasın)
            }
            
            // ✅ DÜZELTME: Transaction başlatıldıktan sonra snapshot'ları kaydet
            // Tüm snapshot'ları transaction içinde kaydet
            if (clanSnapshot != null) saveClanSnapshot(clanSnapshot, true);
            if (contractSnapshot != null) saveContractSnapshot(contractSnapshot, true);
            if (shopSnapshot != null) saveShopSnapshot(shopSnapshot, true);
            if (inventorySnapshot != null) saveInventorySnapshot(inventorySnapshot, true);
            if (allianceSnapshot != null) saveAllianceSnapshot(allianceSnapshot, true);
            if (disasterSnapshot != null) saveDisasterSnapshot(disasterSnapshot, true);
            if (bankSnapshot != null) saveClanBankSnapshot(bankSnapshot, true);
            if (missionSnapshot != null) saveClanMissionSnapshot(missionSnapshot, true);
            if (trapSnapshot != null) saveTrapSnapshot(trapSnapshot, true);
            if (requestSnapshot != null) saveContractRequestSnapshot(requestSnapshot, true);
            if (termsSnapshot != null) saveContractTermsSnapshot(termsSnapshot, true);
            
            // ✅ DÜZELTME: Commit et (sadece transaction başlatılmışsa)
            if (transactionStarted) {
                try {
                    // ✅ DÜZELTME: Commit'ten önce connection kontrolü (bağlantı kapanmış olabilir)
                    Connection testConn = databaseManager.getConnection();
                    if (testConn == null || testConn.isClosed()) {
                        plugin.getLogger().warning("SQLite veritabanı bağlantısı commit sırasında kapalı, rollback yapılıyor.");
                        try {
                            databaseManager.rollback();
                        } catch (SQLException rollbackEx) {
                            // Rollback hatası sessizce log'lanır
                        }
                        return; // Bağlantı kapalıysa commit yapma
                    }
                    
                    databaseManager.commit();
                    plugin.getLogger().info("§aTüm veriler SQLite'a kaydedildi.");
                } catch (SQLException commitEx) {
                    // ✅ DÜZELTME: "Commit çağrıldı ama transaction başlatılmamış!" hatası için özel kontrol
                    if (commitEx.getMessage() != null && commitEx.getMessage().contains("transaction başlatılmamış")) {
                        plugin.getLogger().warning("SQLite transaction durumu tutarsız, rollback yapılıyor: " + commitEx.getMessage());
                        try {
                            databaseManager.rollback();
                        } catch (SQLException rollbackEx) {
                            // Rollback hatası sessizce log'lanır
                        }
                        return; // Transaction tutarsız, exception fırlatma
                    }
                    throw commitEx; // Diğer hatalar için dış catch'e fırlat
                }
            }
                
        } catch (SQLException e) {
            // ✅ DÜZELTME: Rollback et (sadece transaction başlatılmışsa)
            if (transactionStarted) {
                try {
                    databaseManager.rollback();
                } catch (SQLException rollbackEx) {
                    plugin.getLogger().severe("SQLite rollback hatası: " + rollbackEx.getMessage());
                }
            }
            plugin.getLogger().severe("SQLite kayıt hatası: " + e.getMessage());
            // ✅ DÜZELTME: Async context'te exception fırlatma yerine log yaz (DataManager zaten try-catch içinde)
            // throw e; // Async context'te exception fırlatma sorun çıkarabilir
        } finally {
            saveLock.unlock();
        }
    }
    
    /**
     * ✅ YENİ: Eski veri temizleme task'ını başlat (her 24 saatte bir)
     */
    public void startOldDataCleanupTask() {
        // Eğer zaten bir task varsa önce durdur
        if (oldDataCleanupTask != null) {
            stopOldDataCleanupTask();
        }
        
        oldDataCleanupTask = org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                cleanupOldData();
            } catch (Exception e) {
                plugin.getLogger().warning("Eski veri temizleme hatası: " + e.getMessage());
            }
        }, 1728000L, 1728000L); // 24 saat = 1728000 tick
        
        plugin.getLogger().info("§aEski veri temizleme task'ı başlatıldı (24 saat aralıkla).");
    }
    
    /**
     * ✅ YENİ: Eski veri temizleme task'ını durdur
     */
    public void stopOldDataCleanupTask() {
        if (oldDataCleanupTask != null) {
            oldDataCleanupTask.cancel();
            oldDataCleanupTask = null;
        }
    }
    
    /**
     * ✅ YENİ: Eski verileri temizle (30 günden eski)
     */
    public void cleanupOldData() throws SQLException {
        Connection conn = databaseManager.getConnection();
        long cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 gün
        Timestamp cutoffTimestamp = new Timestamp(cutoffTime);
        
        int totalDeleted = 0;
        
        // Tamamlanmış kontratlar (30 günden eski)
        try (PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM contracts WHERE delivered = 1 AND created_at < ?")) {
            stmt.setTimestamp(1, cutoffTimestamp);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                plugin.getLogger().info("Eski kontratlar temizlendi: " + deleted);
                totalDeleted += deleted;
            }
        }
        
        // Pasif ittifaklar (30 günden eski ve broken = true)
        try (PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM alliances WHERE broken = 1 AND created_at < ?")) {
            stmt.setTimestamp(1, cutoffTimestamp);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                plugin.getLogger().info("Eski ittifaklar temizlendi: " + deleted);
                totalDeleted += deleted;
            }
        }
        
        // Pasif felaketler (30 günden eski ve active = false)
        try (PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM disasters WHERE active = 0 AND start_time < ?")) {
            stmt.setTimestamp(1, cutoffTimestamp);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                plugin.getLogger().info("Eski felaketler temizlendi: " + deleted);
                totalDeleted += deleted;
            }
        }
        
        // Eski kontrat istekleri (30 günden eski ve status != 'PENDING')
        try (PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM contract_requests WHERE status != 'PENDING' AND created_at < ?")) {
            stmt.setTimestamp(1, cutoffTimestamp);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                plugin.getLogger().info("Eski kontrat istekleri temizlendi: " + deleted);
                totalDeleted += deleted;
            }
        }
        
        if (totalDeleted > 0) {
            plugin.getLogger().info("§aToplam " + totalDeleted + " eski veri kaydı temizlendi.");
            
            // VACUUM yap (veritabanı boyutunu küçült)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("VACUUM;");
                plugin.getLogger().info("§aVeritabanı optimize edildi (VACUUM).");
            } catch (SQLException e) {
                plugin.getLogger().warning("VACUUM hatası: " + e.getMessage());
            }
        }
    }
    
    /**
     * ✅ YENİ: SQLite'dan klanları yükle
     */
    public List<me.mami.stratocraft.model.Clan> loadClans() throws SQLException {
        List<me.mami.stratocraft.model.Clan> clans = new java.util.ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT data FROM clans")) {
            
            while (rs.next()) {
                try {
                    String jsonData = rs.getString("data");
                    if (jsonData == null || jsonData.isEmpty()) continue;
                    
                    // JSON'dan ClanData'ya parse et
                    DataManager.ClanData clanData = gson.fromJson(jsonData, DataManager.ClanData.class);
                    if (clanData == null || clanData.id == null) continue;
                    
                    // ClanData'dan Clan objesine dönüştür
                    me.mami.stratocraft.model.Clan clan = convertClanDataToClan(clanData);
                    if (clan != null) {
                        clans.add(clan);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Klan yükleme hatası: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        return clans;
    }
    
    /**
     * ✅ YENİ: ClanData'dan Clan objesine dönüştür
     */
    private me.mami.stratocraft.model.Clan convertClanDataToClan(DataManager.ClanData data) {
        if (data == null || data.id == null || data.name == null) return null;
        
        try {
            UUID clanId = UUID.fromString(data.id);
            
            // Leader ID'yi bul
            UUID leaderId = null;
            if (data.members != null) {
                for (Map.Entry<String, String> entry : data.members.entrySet()) {
                    if ("LEADER".equalsIgnoreCase(entry.getValue())) {
                        leaderId = UUID.fromString(entry.getKey());
                        break;
                    }
                }
            }
            
            if (leaderId == null) {
                plugin.getLogger().warning("Klan yükleme hatası: Leader bulunamadı - " + data.id);
                return null;
            }
            
            // Clan oluştur
            me.mami.stratocraft.model.Clan clan = new me.mami.stratocraft.model.Clan(data.name, leaderId);
            clan.setId(clanId);
            
            // Members
            if (data.members != null) {
                for (Map.Entry<String, String> entry : data.members.entrySet()) {
                    UUID memberId = UUID.fromString(entry.getKey());
                    me.mami.stratocraft.model.Clan.Rank rank = me.mami.stratocraft.model.Clan.Rank.valueOf(entry.getValue());
                    if (!memberId.equals(leaderId)) {
                        clan.addMember(memberId, rank);
                    }
                }
            }
            
            // Territory
            if (data.territory != null && data.territory.center != null) {
                org.bukkit.Location center = deserializeLocation(data.territory.center);
                if (center != null) {
                    me.mami.stratocraft.model.Territory territory = 
                        new me.mami.stratocraft.model.Territory(clanId, center);
                    if (data.territory.radius != null) {
                        territory.expand(data.territory.radius - 50);
                    }
                    if (data.territory.outposts != null) {
                        for (String outpostStr : data.territory.outposts) {
                            org.bukkit.Location outpost = deserializeLocation(outpostStr);
                            if (outpost != null) {
                                territory.addOutpost(outpost);
                            }
                        }
                    }
                    clan.setTerritory(territory);
                }
            }
            
            // Structures
            if (data.structures != null) {
                for (DataManager.StructureData sd : data.structures) {
                    UUID ownerId = sd.ownerId != null ? UUID.fromString(sd.ownerId) : null;
                    org.bukkit.Location loc = deserializeLocation(sd.location);
                    if (loc != null) {
                        me.mami.stratocraft.model.Structure structure = new me.mami.stratocraft.model.Structure(
                            me.mami.stratocraft.model.Structure.Type.valueOf(sd.type),
                            loc,
                            sd.level,
                            ownerId
                        );
                        if (sd.shieldFuel != null) {
                            for (int i = 0; i < sd.shieldFuel; i++) {
                                structure.addFuel(1);
                            }
                        }
                        clan.addStructure(structure);
                    }
                }
            }
            
            // Bank balance ve XP
            if (data.bankBalance != null) {
                clan.deposit(data.bankBalance);
            }
            if (data.storedXP != null) {
                clan.setStoredXP(data.storedXP);
            }
            
            // Guests
            if (data.guests != null) {
                for (String guestId : data.guests) {
                    try {
                        clan.addGuest(UUID.fromString(guestId));
                    } catch (IllegalArgumentException e) {
                        // Geçersiz UUID, atla
                    }
                }
            }
            
            // Warring clans
            if (data.warringClans != null) {
                for (String warringClanId : data.warringClans) {
                    try {
                        clan.addWarringClan(UUID.fromString(warringClanId));
                    } catch (IllegalArgumentException e) {
                        // Geçersiz UUID, atla
                    }
                }
            }
            
            // Alliance clans
            if (data.allianceClans != null) {
                for (String allianceClanId : data.allianceClans) {
                    try {
                        clan.addAllianceClan(UUID.fromString(allianceClanId));
                    } catch (IllegalArgumentException e) {
                        // Geçersiz UUID, atla
                    }
                }
            }
            
            // ✅ YENİ: Klan kristali konumu ve hasCrystal flag'i
            if (data.crystalLocation != null) {
                org.bukkit.Location crystalLoc = deserializeLocation(data.crystalLocation);
                if (crystalLoc != null) {
                    clan.setCrystalLocation(crystalLoc);
                    if (data.hasCrystal != null) {
                        clan.setHasCrystal(data.hasCrystal);
                    } else {
                        clan.setHasCrystal(true); // Eski veriler için
                    }
                    
                    // ✅ YENİ: Kristal sistemi verilerini yükle
                    if (clan.hasCrystal()) {
                        if (data.crystalMaxHealth != null) {
                            clan.setCrystalMaxHealth(data.crystalMaxHealth);
                        } else {
                            clan.setCrystalMaxHealth(100.0);
                        }
                        if (data.crystalCurrentHealth != null) {
                            clan.setCrystalCurrentHealth(data.crystalCurrentHealth);
                        } else {
                            clan.setCrystalCurrentHealth(clan.getCrystalMaxHealth());
                        }
                        if (data.crystalDamageReduction != null) {
                            clan.setCrystalDamageReduction(data.crystalDamageReduction);
                        }
                        if (data.crystalShieldBlocks != null) {
                            clan.setCrystalShieldBlocks(data.crystalShieldBlocks);
                        }
                        if (data.crystalMaxShieldBlocks != null) {
                            clan.setCrystalMaxShieldBlocks(data.crystalMaxShieldBlocks);
                        }
                        if (data.lastCrystalRegenTime != null) {
                            clan.setLastCrystalRegenTime(data.lastCrystalRegenTime);
                        }
                    }
                }
            }
            
            return clan;
        } catch (Exception e) {
            plugin.getLogger().warning("Klan dönüştürme hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * ✅ YENİ: Location string'den Location objesine dönüştür
     * Format desteği: "world;x;y;z;yaw;pitch" (noktalı virgül) veya "world:x:y:z:yaw:pitch" (iki nokta)
     */
    private org.bukkit.Location deserializeLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) return null;
        
        try {
            // ✅ DÜZELTME: Önce noktalı virgül formatını kontrol et (DataManager formatı)
            if (locationStr.contains(";")) {
                String[] parts = locationStr.split(";");
                if (parts.length >= 4) {
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
                    if (world == null) {
                        plugin.getLogger().warning("Location deserialize: World bulunamadı: " + parts[0]);
                        return null;
                    }
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    float yaw = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0f;
                    float pitch = parts.length >= 6 ? Float.parseFloat(parts[5]) : 0f;
                    return new org.bukkit.Location(world, x, y, z, yaw, pitch);
                }
            }
            
            // İki nokta formatı (eski format)
            if (locationStr.contains(":")) {
                String[] parts = locationStr.split(":");
                if (parts.length >= 4) {
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
                    if (world == null) {
                        plugin.getLogger().warning("Location deserialize: World bulunamadı: " + parts[0]);
                        return null;
                    }
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    float yaw = parts.length >= 5 ? Float.parseFloat(parts[4]) : 0f;
                    float pitch = parts.length >= 6 ? Float.parseFloat(parts[5]) : 0f;
                    return new org.bukkit.Location(world, x, y, z, yaw, pitch);
                }
            }
            
            // JSON formatı denemesi (son çare)
            try {
                return gson.fromJson(locationStr, org.bukkit.Location.class);
            } catch (Exception jsonEx) {
                // JSON başarısız, format hatası
                plugin.getLogger().warning("Location deserialize hatası: " + locationStr + " - Geçersiz format (ne ; ne : ne JSON)");
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Location deserialize hatası: " + locationStr + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

