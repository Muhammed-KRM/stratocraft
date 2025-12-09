package me.mami.stratocraft.model;

/**
 * Klan Güç Profili
 * 
 * Bir klanın tüm güç bileşenlerini taşıyan veri yapısı.
 * SGP (Stratocraft Global Power) sisteminin klan veri modeli.
 */
public class ClanPowerProfile {
    // ========== BİLEŞENLER ==========
    private double memberPowerSum;       // Üyelerin toplam gücü
    private double structurePower;       // Yapı gücü
    private double ritualBlockPower;      // Ritüel blok gücü (klan arazisi)
    private double ritualResourcePower;   // Ritüel kaynak gücü (kullanım geçmişi)
    
    // ========== TOPLAM ==========
    private double totalClanPower;        // Toplam klan gücü
    
    // ========== META ==========
    private int clanLevel;                // Hesaplanmış klan seviyesi
    private long lastUpdate;              // Son güncelleme zamanı
    
    /**
     * Boş profil oluştur
     */
    public ClanPowerProfile() {
        this.memberPowerSum = 0.0;
        this.structurePower = 0.0;
        this.ritualBlockPower = 0.0;
        this.ritualResourcePower = 0.0;
        this.totalClanPower = 0.0;
        this.clanLevel = 1;
        this.lastUpdate = System.currentTimeMillis();
    }
    
    // ========== GETTERS & SETTERS ==========
    
    public double getMemberPowerSum() {
        return memberPowerSum;
    }
    
    public void setMemberPowerSum(double memberPowerSum) {
        this.memberPowerSum = memberPowerSum;
    }
    
    public double getStructurePower() {
        return structurePower;
    }
    
    public void setStructurePower(double structurePower) {
        this.structurePower = structurePower;
    }
    
    public double getRitualBlockPower() {
        return ritualBlockPower;
    }
    
    public void setRitualBlockPower(double ritualBlockPower) {
        this.ritualBlockPower = ritualBlockPower;
    }
    
    public double getRitualResourcePower() {
        return ritualResourcePower;
    }
    
    public void setRitualResourcePower(double ritualResourcePower) {
        this.ritualResourcePower = ritualResourcePower;
    }
    
    public double getTotalClanPower() {
        return totalClanPower;
    }
    
    public void setTotalClanPower(double totalClanPower) {
        this.totalClanPower = totalClanPower;
    }
    
    public int getClanLevel() {
        return clanLevel;
    }
    
    public void setClanLevel(int clanLevel) {
        this.clanLevel = clanLevel;
    }
    
    public long getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    /**
     * Profil bilgilerini string olarak döndür (debug için)
     */
    @Override
    public String toString() {
        return String.format(
            "ClanPowerProfile{members=%.1f, structures=%.1f, blocks=%.1f, " +
            "resources=%.1f, total=%.1f, Level=%d}",
            memberPowerSum, structurePower, ritualBlockPower,
            ritualResourcePower, totalClanPower, clanLevel
        );
    }
}

