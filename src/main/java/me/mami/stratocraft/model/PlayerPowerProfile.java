package me.mami.stratocraft.model;

/**
 * Oyuncu Güç Profili
 * 
 * Bir oyuncunun tüm güç bileşenlerini taşıyan veri yapısı.
 * SGP (Stratocraft Global Power) sisteminin temel veri modeli.
 */
public class PlayerPowerProfile {
    // ========== BİLEŞENLER ==========
    private double gearPower;           // Eşya gücü (silah + zırh)
    private double trainingPower;        // Ustalık gücü (ritüel mastery)
    private double buffPower;            // Buff gücü (aktif bufflar)
    private double ritualPower;          // Ritüel gücü (oyuncu bazlı, gelecekte)
    
    // ========== TOPLAMLAR ==========
    private double totalCombatPower;     // CP (Combat Power) - Savaş odaklı
    private double totalProgressionPower; // PP (Progression Power) - İlerleme odaklı
    private double totalSGP;             // SGP (Stratocraft Global Power) - Toplam güç
    
    // ========== META ==========
    private int playerLevel;             // Hesaplanmış seviye
    private long lastUpdate;              // Son güncelleme zamanı
    
    // ========== HİSTEREZİS SİSTEMİ (Zırh Çıkarma Exploit Önleme) ==========
    private double cachedGearPower;      // Son hesaplanan eşya gücü
    private long lastGearDecreaseTime;   // Son güç düşüş zamanı
    
    /**
     * Boş profil oluştur
     */
    public PlayerPowerProfile() {
        this.gearPower = 0.0;
        this.trainingPower = 0.0;
        this.buffPower = 0.0;
        this.ritualPower = 0.0;
        this.totalCombatPower = 0.0;
        this.totalProgressionPower = 0.0;
        this.totalSGP = 0.0;
        this.playerLevel = 1;
        this.lastUpdate = System.currentTimeMillis();
        this.cachedGearPower = 0.0;
        this.lastGearDecreaseTime = 0L;
    }
    
    // ========== GETTERS & SETTERS ==========
    
    public double getGearPower() {
        return gearPower;
    }
    
    public void setGearPower(double gearPower) {
        // Histerezis: Güç düşüşü kontrolü
        if (gearPower < this.gearPower) {
            // Güç düştü, zamanı kaydet
            this.lastGearDecreaseTime = System.currentTimeMillis();
        } else {
            // Güç arttı, cache'i güncelle
            this.cachedGearPower = gearPower;
        }
        this.gearPower = gearPower;
    }
    
    public double getTrainingPower() {
        return trainingPower;
    }
    
    public void setTrainingPower(double trainingPower) {
        this.trainingPower = trainingPower;
    }
    
    public double getBuffPower() {
        return buffPower;
    }
    
    public void setBuffPower(double buffPower) {
        this.buffPower = buffPower;
    }
    
    public double getRitualPower() {
        return ritualPower;
    }
    
    public void setRitualPower(double ritualPower) {
        this.ritualPower = ritualPower;
    }
    
    public double getTotalCombatPower() {
        return totalCombatPower;
    }
    
    public void setTotalCombatPower(double totalCombatPower) {
        this.totalCombatPower = totalCombatPower;
    }
    
    public double getTotalProgressionPower() {
        return totalProgressionPower;
    }
    
    public void setTotalProgressionPower(double totalProgressionPower) {
        this.totalProgressionPower = totalProgressionPower;
    }
    
    public double getTotalSGP() {
        return totalSGP;
    }
    
    public void setTotalSGP(double totalSGP) {
        this.totalSGP = totalSGP;
    }
    
    public int getPlayerLevel() {
        return playerLevel;
    }
    
    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }
    
    public long getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    // ========== HİSTEREZİS SİSTEMİ ==========
    
    /**
     * Etkili eşya gücü (histerezis ile)
     * Güç düşüşü için gecikme uygulanır (exploit önleme)
     * 
     * @param gearDecreaseDelay Gecikme süresi (ms)
     * @return Etkili eşya gücü
     */
    public double getEffectiveGearPower(long gearDecreaseDelay) {
        if (gearPower >= cachedGearPower) {
            // Güç arttı veya aynı, anlık güncelleme
            cachedGearPower = gearPower;
            return gearPower;
        }
        
        // Güç düştü, gecikme kontrolü
        long timeSinceDecrease = System.currentTimeMillis() - lastGearDecreaseTime;
        if (timeSinceDecrease < gearDecreaseDelay) {
            // Hala gecikme süresi içinde, eski gücü kullan
            return cachedGearPower;
        }
        
        // Gecikme süresi geçti, yeni gücü kullan
        cachedGearPower = gearPower;
        return gearPower;
    }
    
    public double getCachedGearPower() {
        return cachedGearPower;
    }
    
    public long getLastGearDecreaseTime() {
        return lastGearDecreaseTime;
    }
    
    /**
     * Profil bilgilerini string olarak döndür (debug için)
     */
    @Override
    public String toString() {
        return String.format(
            "PlayerPowerProfile{gear=%.1f, training=%.1f, buff=%.1f, " +
            "CP=%.1f, PP=%.1f, SGP=%.1f, Level=%d}",
            gearPower, trainingPower, buffPower,
            totalCombatPower, totalProgressionPower, totalSGP, playerLevel
        );
    }
}

