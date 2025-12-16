package me.mami.stratocraft.model.clan;

import me.mami.stratocraft.model.base.BaseModel;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Territory;
import org.bukkit.Location;

import java.util.*;

/**
 * Klan Veri Modeli (Genişletilmiş)
 * 
 * Mevcut Clan.java'yı genişleten model
 * 
 * Ek Özellikler:
 * - Güç (power)
 * - Seviye (level)
 * - Yapı sayısı
 * - Offline koruma yakıtı
 * - Detaylı istatistikler
 */
public class ClanData extends BaseModel {
    private String name;
    private UUID leaderId;
    private Map<UUID, Clan.Rank> members;
    private List<Structure> structures;
    private Set<UUID> guests;
    private Territory territory;
    private double bankBalance;
    private int storedXP;
    private Location crystalLocation;
    private boolean hasCrystal;
    private long createdAt;
    
    // Yeni özellikler
    private double power; // Klan gücü
    private int level; // Klan seviyesi
    private int structureCount; // Yapı sayısı (cache)
    private int offlineProtectionFuel; // Offline koruma yakıtı
    
    public ClanData(String name, UUID leaderId) {
        super();
        this.name = name;
        this.leaderId = leaderId;
        this.members = Collections.synchronizedMap(new HashMap<>());
        this.members.put(leaderId, Clan.Rank.LEADER);
        this.structures = Collections.synchronizedList(new ArrayList<>());
        this.guests = Collections.synchronizedSet(new HashSet<>());
        this.bankBalance = 0.0;
        this.storedXP = 0;
        this.hasCrystal = false;
        this.createdAt = System.currentTimeMillis();
        
        // Yeni özellikler
        this.power = 0.0;
        this.level = 1;
        this.structureCount = 0;
        this.offlineProtectionFuel = 0;
    }
    
    public ClanData(UUID id, String name, UUID leaderId) {
        super(id);
        this.name = name;
        this.leaderId = leaderId;
        this.members = Collections.synchronizedMap(new HashMap<>());
        this.members.put(leaderId, Clan.Rank.LEADER);
        this.structures = Collections.synchronizedList(new ArrayList<>());
        this.guests = Collections.synchronizedSet(new HashSet<>());
        this.bankBalance = 0.0;
        this.storedXP = 0;
        this.hasCrystal = false;
        this.createdAt = System.currentTimeMillis();
        
        // Yeni özellikler
        this.power = 0.0;
        this.level = 1;
        this.structureCount = 0;
        this.offlineProtectionFuel = 0;
    }
    
    // Mevcut Clan.java metodlarını koru
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }
    
    public UUID getLeaderId() {
        return leaderId;
    }
    
    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
        updateTimestamp();
    }
    
    public Map<UUID, Clan.Rank> getMembers() {
        return members;
    }
    
    public void setMembers(Map<UUID, Clan.Rank> members) {
        this.members = members != null ? Collections.synchronizedMap(new HashMap<>(members)) : Collections.synchronizedMap(new HashMap<>());
        updateTimestamp();
    }
    
    public List<Structure> getStructures() {
        return structures;
    }
    
    public void setStructures(List<Structure> structures) {
        this.structures = structures != null ? Collections.synchronizedList(new ArrayList<>(structures)) : Collections.synchronizedList(new ArrayList<>());
        this.structureCount = this.structures.size();
        updateTimestamp();
    }
    
    public void addStructure(Structure structure) {
        if (structure != null) {
            this.structures.add(structure);
            this.structureCount = this.structures.size();
            updateTimestamp();
        }
    }
    
    public void removeStructure(Structure structure) {
        if (structure != null) {
            this.structures.remove(structure);
            this.structureCount = this.structures.size();
            updateTimestamp();
        }
    }
    
    public Set<UUID> getGuests() {
        return guests;
    }
    
    public void setGuests(Set<UUID> guests) {
        this.guests = guests != null ? Collections.synchronizedSet(new HashSet<>(guests)) : Collections.synchronizedSet(new HashSet<>());
        updateTimestamp();
    }
    
    public Territory getTerritory() {
        return territory;
    }
    
    public void setTerritory(Territory territory) {
        this.territory = territory;
        updateTimestamp();
    }
    
    public double getBankBalance() {
        return bankBalance;
    }
    
    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
        updateTimestamp();
    }
    
    public void deposit(double amount) {
        this.bankBalance += amount;
        updateTimestamp();
    }
    
    public void withdraw(double amount) {
        this.bankBalance = Math.max(0, this.bankBalance - amount);
        updateTimestamp();
    }
    
    public int getStoredXP() {
        return storedXP;
    }
    
    public void setStoredXP(int storedXP) {
        this.storedXP = Math.max(0, storedXP);
        updateTimestamp();
    }
    
    public void addXP(int amount) {
        this.storedXP += amount;
        updateTimestamp();
    }
    
    public void removeXP(int amount) {
        this.storedXP = Math.max(0, this.storedXP - amount);
        updateTimestamp();
    }
    
    public Location getCrystalLocation() {
        return crystalLocation;
    }
    
    public void setCrystalLocation(Location crystalLocation) {
        this.crystalLocation = crystalLocation;
        this.hasCrystal = (crystalLocation != null);
        updateTimestamp();
    }
    
    public boolean hasCrystal() {
        return hasCrystal;
    }
    
    public void setHasCrystal(boolean hasCrystal) {
        this.hasCrystal = hasCrystal;
        updateTimestamp();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    // Yeni özellikler
    public double getPower() {
        return power;
    }
    
    public void setPower(double power) {
        this.power = Math.max(0.0, power);
        updateTimestamp();
    }
    
    public void addPower(double amount) {
        this.power = Math.max(0.0, this.power + amount);
        updateTimestamp();
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = Math.max(1, level);
        updateTimestamp();
    }
    
    public int getStructureCount() {
        return structureCount;
    }
    
    public void updateStructureCount() {
        this.structureCount = this.structures.size();
        updateTimestamp();
    }
    
    public int getOfflineProtectionFuel() {
        return offlineProtectionFuel;
    }
    
    public void setOfflineProtectionFuel(int offlineProtectionFuel) {
        this.offlineProtectionFuel = Math.max(0, offlineProtectionFuel);
        updateTimestamp();
    }
    
    public void addOfflineProtectionFuel(int amount) {
        this.offlineProtectionFuel = Math.max(0, this.offlineProtectionFuel + amount);
        updateTimestamp();
    }
    
    public void consumeOfflineProtectionFuel() {
        if (offlineProtectionFuel > 0) {
            offlineProtectionFuel--;
            updateTimestamp();
        }
    }
    
    /**
     * Mevcut Clan.java objesinden ClanData oluştur
     */
    public static ClanData fromClan(Clan clan) {
        if (clan == null) return null;
        
        ClanData data = new ClanData(clan.getId(), clan.getName(), clan.getLeader());
        data.setMembers(new HashMap<>(clan.getMembers()));
        data.setStructures(new ArrayList<>(clan.getStructures()));
        data.setGuests(new HashSet<>(clan.getGuests()));
        data.setTerritory(clan.getTerritory());
        data.setBankBalance(clan.getBalance());
        data.setStoredXP(clan.getStoredXP());
        data.setCrystalLocation(clan.getCrystalLocation());
        data.setHasCrystal(clan.hasCrystal());
        data.setCreatedAt(clan.getCreatedAt());
        
        return data;
    }
    
    /**
     * ClanData'yı mevcut Clan.java objesine dönüştür
     */
    public Clan toClan() {
        Clan clan = new Clan(this.name, this.leaderId);
        clan.setId(this.getId());
        
        // Üyeleri ekle
        for (Map.Entry<UUID, Clan.Rank> entry : this.members.entrySet()) {
            clan.addMember(entry.getKey(), entry.getValue());
        }
        
        // Yapıları ekle
        for (Structure structure : this.structures) {
            clan.addStructure(structure);
        }
        
        // Misafirleri ekle
        for (UUID guestId : this.guests) {
            clan.addGuest(guestId);
        }
        
        clan.setTerritory(this.territory);
        clan.setCrystalLocation(this.crystalLocation);
        clan.setHasCrystal(this.hasCrystal);
        clan.setCreatedAt(this.createdAt);
        
        // Banka ve XP
        while (clan.getBalance() < this.bankBalance) {
            clan.deposit(1.0);
        }
        clan.setStoredXP(this.storedXP);
        
        return clan;
    }
}

