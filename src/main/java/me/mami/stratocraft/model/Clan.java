package me.mami.stratocraft.model;

import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;

import java.util.*;

public class Clan {
    public enum Rank {
        LEADER(5), ELITE(4), GENERAL(3), MEMBER(2), RECRUIT(1);
        private final int level;

        Rank(int level) {
            this.level = level;
        }

        public boolean isAtLeast(Rank other) {
            return this.level >= other.level;
        }
    }

    private UUID id = UUID.randomUUID();
    private String name;
    // Thread-safe: Collections.synchronizedMap kullan (veya ConcurrentHashMap)
    private final Map<UUID, Rank> members = Collections.synchronizedMap(new HashMap<>());
    private final List<Structure> structures = Collections.synchronizedList(new ArrayList<>());
    private final Set<UUID> guests = Collections.synchronizedSet(new HashSet<>());
    private Territory territory;
    private double bankBalance = 0;
    private int storedXP = 0; // XP Bankası için
    private Location crystalLocation; // Klan Kristali lokasyonu
    private EnderCrystal crystalEntity; // Kristal entity referansı
    private long createdAt; // Klan kurulma zamanı (grace period için)
    private boolean hasCrystal = false; // Kristal yerleştirildi mi? (Ölümsüz klan önleme)

    public Clan(String name, UUID leader) {
        this.name = name;
        this.members.put(leader, Rank.LEADER);
        this.createdAt = System.currentTimeMillis();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; } // DataManager için
    public String getName() { return name; }
    public void setName(String newName) { this.name = newName; } // Klan ismi değiştirme için
    public Map<UUID, Rank> getMembers() { return members; }
    public void addMember(UUID uuid, Rank rank) { 
        if (uuid != null && rank != null) {
            members.put(uuid, rank);
        }
    }
    public Rank getRank(UUID uuid) { 
        if (uuid == null) return null;
        return members.get(uuid); 
    }
    
    /**
     * Rütbe değiştir (ClanRankSystem için)
     */
    public void setRank(UUID uuid, Rank rank) {
        if (uuid == null || rank == null) return;
        if (members.containsKey(uuid)) {
            members.put(uuid, rank);
        }
    }

    public List<Structure> getStructures() { return structures; }
    public void addStructure(Structure s) { structures.add(s); }

    public Territory getTerritory() { return territory; }
    public void setTerritory(Territory t) { 
        this.territory = t;
        // Cache güncellemesi TerritoryManager'da yapılmalı (setCacheDirty çağrılmalı)
    }

    public void deposit(double amount) { bankBalance += amount; }
    public void withdraw(double amount) { bankBalance -= amount; }
    public double getBalance() { return bankBalance; }

    public void addGuest(UUID uuid) { guests.add(uuid); }
    public boolean isGuest(UUID uuid) { return guests.contains(uuid); }
    public Set<UUID> getGuests() { return guests; }

    public int getTechLevel() {
        return structures.stream().mapToInt(Structure::getLevel).max().orElse(1);
    }
    
    public int getStoredXP() { return storedXP; }
    public void setStoredXP(int amount) { this.storedXP = Math.max(0, amount); }
    public void addXP(int amount) { this.storedXP += amount; }
    public void removeXP(int amount) { this.storedXP = Math.max(0, this.storedXP - amount); }
    
    public Location getCrystalLocation() { return crystalLocation; }
    public void setCrystalLocation(Location loc) { 
        this.crystalLocation = loc;
        this.hasCrystal = (loc != null);
    }
    public EnderCrystal getCrystalEntity() { return crystalEntity; }
    public void setCrystalEntity(EnderCrystal crystal) { 
        this.crystalEntity = crystal;
        this.hasCrystal = (crystal != null);
    }
    
    /**
     * Kristal yerleştirildi mi? (Ölümsüz klan önleme)
     */
    public boolean hasCrystal() { return hasCrystal; }
    
    /**
     * Kristal durumunu ayarla
     */
    public void setHasCrystal(boolean hasCrystal) { this.hasCrystal = hasCrystal; }
    
    public boolean isGeneral(UUID uuid) {
        Rank rank = getRank(uuid);
        return rank == Rank.GENERAL || rank == Rank.LEADER;
    }
    
    public UUID getLeader() {
        // Thread-safe: Synchronized
        synchronized (members) {
            for (Map.Entry<UUID, Rank> entry : members.entrySet()) {
                if (entry.getValue() == Rank.LEADER) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    // Grace Period (Başlangıç Koruması)
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long time) { this.createdAt = time; } // DataManager için
    
    /**
     * Grace period aktif mi? (24 saat = 86400000 ms)
     */
    public boolean isInGracePeriod() {
        long gracePeriodDuration = 24 * 60 * 60 * 1000L; // 24 saat
        return System.currentTimeMillis() - createdAt < gracePeriodDuration;
    }
    
    /**
     * Grace period süresi kaldı mı? (saniye cinsinden)
     */
    public long getGracePeriodRemaining() {
        long gracePeriodDuration = 24 * 60 * 60 * 1000L; // 24 saat
        long remaining = gracePeriodDuration - (System.currentTimeMillis() - createdAt);
        return Math.max(0, remaining / 1000); // Saniye cinsinden
    }
}

