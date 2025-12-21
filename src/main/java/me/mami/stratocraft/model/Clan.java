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

        public int getLevel() {
            return level;
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
    
    // ✅ YENİ: Çoklu savaş desteği - Bu klanın savaşta olduğu klanlar
    private final Set<UUID> warringClans = Collections.synchronizedSet(new HashSet<>());
    
    // ✅ YENİ: İttifaklar - Bu klanın ittifak olduğu klanlar (referans için)
    private final Set<UUID> allianceClans = Collections.synchronizedSet(new HashSet<>());

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
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null) {
            plugin.getLogger().info("[CLAN] setCrystalLocation çağrıldı: " + getName() + 
                ", location: " + (loc != null ? loc.toString() : "null") + 
                ", mevcut hasCrystal: " + hasCrystal);
        }
        
        this.crystalLocation = loc;
        // ✅ DÜZELTME: crystalLocation null ise hasCrystal false, değilse true
        this.hasCrystal = (loc != null);
        
        if (plugin != null) {
            plugin.getLogger().info("[CLAN] setCrystalLocation tamamlandı: " + getName() + 
                ", yeni hasCrystal: " + hasCrystal);
        }
    }
    
    public EnderCrystal getCrystalEntity() { return crystalEntity; }
    
    public void setCrystalEntity(EnderCrystal crystal) { 
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null) {
            plugin.getLogger().info("[CLAN] setCrystalEntity çağrıldı: " + getName() + 
                ", entity: " + (crystal != null ? crystal.getUniqueId() : "null") + 
                ", mevcut hasCrystal: " + hasCrystal);
        }
        
        this.crystalEntity = crystal;
        // ✅ DÜZELTME: crystalEntity null ise hasCrystal false yapma, sadece crystalLocation kontrolü yeterli
        // (crystalEntity null olabilir ama crystalLocation var olabilir - sunucu restart sonrası)
        // hasCrystal'ı crystalLocation'dan çıkar
        this.hasCrystal = (this.crystalLocation != null);
        
        if (plugin != null) {
            plugin.getLogger().info("[CLAN] setCrystalEntity tamamlandı: " + getName() + 
                ", yeni hasCrystal: " + hasCrystal + 
                ", crystalLocation: " + (crystalLocation != null ? crystalLocation.toString() : "null"));
        }
    }
    
    /**
     * Kristal yerleştirildi mi? (Ölümsüz klan önleme)
     */
    public boolean hasCrystal() { 
        // ✅ DÜZELTME: crystalLocation varsa hasCrystal true olmalı (crystalEntity null olsa bile)
        boolean result = hasCrystal || (crystalLocation != null);
        
        // ✅ DEBUG: Tutarsızlık kontrolü
        if (crystalLocation != null && !hasCrystal) {
            me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null) {
                plugin.getLogger().warning("[CLAN] hasCrystal() tutarsızlık tespit edildi: " + getName() + 
                    " - crystalLocation var ama hasCrystal false! Düzeltiliyor...");
            }
            // Düzelt
            this.hasCrystal = true;
            return true;
        }
        
        return result;
    }
    
    /**
     * Kristal durumunu ayarla
     */
    public void setHasCrystal(boolean hasCrystal) { 
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null) {
            plugin.getLogger().info("[CLAN] setHasCrystal çağrıldı: " + getName() + 
                ", hasCrystal: " + hasCrystal + 
                ", mevcut crystalLocation: " + (crystalLocation != null ? crystalLocation.toString() : "null"));
        }
        
        this.hasCrystal = hasCrystal;
        
        // ✅ DÜZELTME: hasCrystal false yapılıyorsa ve crystalLocation varsa, crystalLocation'ı null yap
        // (Tutarsızlık önleme)
        if (!hasCrystal && crystalLocation != null) {
            if (plugin != null) {
                plugin.getLogger().warning("[CLAN] setHasCrystal(false) çağrıldı ama crystalLocation var! " + 
                    "crystalLocation null yapılıyor: " + getName());
            }
            this.crystalLocation = null;
            this.crystalEntity = null;
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[CLAN] setHasCrystal tamamlandı: " + getName() + 
                ", hasCrystal: " + this.hasCrystal + 
                ", crystalLocation: " + (crystalLocation != null ? crystalLocation.toString() : "null"));
        }
    }
    
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
    
    // ✅ YENİ: Savaşta olunan klanlar yönetimi
    
    /**
     * Bu klanın savaşta olduğu klanlar
     */
    public Set<UUID> getWarringClans() {
        return new HashSet<>(warringClans); // Defensive copy
    }
    
    /**
     * Savaşta olunan klan ekle
     */
    public void addWarringClan(UUID clanId) {
        if (clanId != null && !clanId.equals(this.id)) {
            warringClans.add(clanId);
        }
    }
    
    /**
     * Savaşta olunan klan kaldır
     */
    public void removeWarringClan(UUID clanId) {
        warringClans.remove(clanId);
    }
    
    /**
     * Bu klan belirli bir klanla savaşta mı?
     */
    public boolean isAtWarWith(UUID clanId) {
        return warringClans.contains(clanId);
    }
    
    /**
     * Savaşta olunan klan sayısı
     */
    public int getWarringClanCount() {
        return warringClans.size();
    }
    
    /**
     * Tüm savaşları temizle
     */
    public void clearAllWars() {
        warringClans.clear();
    }
    
    // ✅ YENİ: İttifaklar yönetimi (referans için)
    
    /**
     * Bu klanın ittifak olduğu klanlar (referans için)
     */
    public Set<UUID> getAllianceClans() {
        return new HashSet<>(allianceClans); // Defensive copy
    }
    
    /**
     * İttifak klan ekle
     */
    public void addAllianceClan(UUID clanId) {
        if (clanId != null && !clanId.equals(this.id)) {
            allianceClans.add(clanId);
        }
    }
    
    /**
     * İttifak klan kaldır
     */
    public void removeAllianceClan(UUID clanId) {
        allianceClans.remove(clanId);
    }
    
    /**
     * Bu klan belirli bir klanla ittifak mı?
     */
    public boolean isAlliedWith(UUID clanId) {
        return allianceClans.contains(clanId);
    }
}

