package me.mami.stratocraft.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.DataManager;

import java.sql.*;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

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
    
    public SQLiteDataManager(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
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
            saveLock.unlock();
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
        if (snapshot == null || snapshot.activeTraps == null) return;
        
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
                            world = parts[0];
                            x = Integer.parseInt(parts[1]);
                            y = Integer.parseInt(parts[2]);
                            z = Integer.parseInt(parts[3]);
                        }
                    }
                    
                    if (world == null) continue; // Geçersiz location
                    
                    String jsonData = gson.toJson(trap);
                    
                    stmt.setString(1, trap.id != null ? trap.id : UUID.randomUUID().toString());
                    stmt.setString(2, trap.clanId);
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
            DataManager.TrapSnapshot trapSnapshot) throws SQLException {
        
        saveLock.lock();
        try {
            // ✅ ACID UYUMLU: Tüm işlemler tek transaction içinde
            databaseManager.beginTransaction();
            
            try {
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
                
                databaseManager.commit();
                plugin.getLogger().info("§aTüm veriler SQLite'a kaydedildi.");
                
            } catch (SQLException e) {
                databaseManager.rollback();
                plugin.getLogger().severe("SQLite kayıt hatası: " + e.getMessage());
                throw e;
            }
        } finally {
            saveLock.unlock();
        }
    }
}

