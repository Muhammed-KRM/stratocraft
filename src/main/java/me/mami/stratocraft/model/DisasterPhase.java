package me.mami.stratocraft.model;

/**
 * Felaket Faz Sistemi
 * 
 * Her felaket 4 fazdan oluşur:
 * - EXPLORATION: Keşif (100%-75% Can)
 * - ASSAULT: Saldırı (75%-50% Can)
 * - RAGE: Öfke (50%-25% Can)
 * - DESPERATION: Son Çare (25%-0% Can)
 */
public enum DisasterPhase {
    EXPLORATION(1.0, 0.75, "Keşif", 120000L, 0, 1.0, false),
    ASSAULT(0.75, 0.50, "Saldırı", 90000L, 2, 1.2, false),
    RAGE(0.50, 0.25, "Öfke", 60000L, 5, 1.5, true),
    DESPERATION(0.25, 0.0, "Son Çare", 30000L, 10, 2.0, true);
    
    private final double maxHealthPercent;      // Maksimum can yüzdesi (örn: 1.0 = %100)
    private final double minHealthPercent;       // Minimum can yüzdesi (örn: 0.75 = %75)
    private final String displayName;            // Görünen isim
    private final long attackInterval;           // Saldırı aralığı (ms)
    private final int activeAbilityCount;        // Aktif yetenek sayısı
    private final double movementSpeedMultiplier; // Hareket hızı çarpanı
    private final boolean attackPlayers;         // Oyunculara saldırı yapıyor mu?
    
    DisasterPhase(double max, double min, String name, long interval, 
                  int abilities, double speedMultiplier, boolean attackPlayers) {
        this.maxHealthPercent = max;
        this.minHealthPercent = min;
        this.displayName = name;
        this.attackInterval = interval;
        this.activeAbilityCount = abilities;
        this.movementSpeedMultiplier = speedMultiplier;
        this.attackPlayers = attackPlayers;
    }
    
    /**
     * Can yüzdesine göre mevcut fazı belirle
     * 
     * @param healthPercent Can yüzdesi (0.0 - 1.0 arası)
     * @return Mevcut faz
     */
    public static DisasterPhase getCurrentPhase(double healthPercent) {
        // Sağlık yüzdesi kontrolü
        if (healthPercent > 1.0) healthPercent = 1.0;
        if (healthPercent < 0.0) healthPercent = 0.0;
        
        // Fazları kontrol et (yüksekten düşüğe)
        // Özel durum: Tam eşik değerlerinde (örn: 0.75, 0.50, 0.25) bir sonraki faza geç
        for (DisasterPhase phase : values()) {
            // EXPLORATION için: 1.0 >= healthPercent > 0.75
            // ASSAULT için: 0.75 >= healthPercent > 0.50
            // RAGE için: 0.50 >= healthPercent > 0.25
            // DESPERATION için: 0.25 >= healthPercent >= 0.0
            if (healthPercent <= phase.maxHealthPercent) {
                if (phase == DESPERATION) {
                    // DESPERATION için: 0.25 >= healthPercent >= 0.0 (min dahil)
                    if (healthPercent >= phase.minHealthPercent) {
                        return phase;
                    }
                } else {
                    // Diğer fazlar için: max > healthPercent > min
                    if (healthPercent > phase.minHealthPercent) {
                        return phase;
                    }
                }
            }
        }
        
        // Eğer hiçbir faz eşleşmezse (sadece güvenlik için), DESPERATION döndür
        return DESPERATION;
    }
    
    /**
     * Bir sonraki fazı al (faz geçişi için)
     */
    public DisasterPhase getNextPhase() {
        switch (this) {
            case EXPLORATION:
                return ASSAULT;
            case ASSAULT:
                return RAGE;
            case RAGE:
                return DESPERATION;
            case DESPERATION:
            default:
                return null; // Son faz, bir sonraki yok
        }
    }
    
    /**
     * Faz geçişi yapılabilir mi?
     */
    public boolean canTransitionTo(DisasterPhase nextPhase) {
        return getNextPhase() == nextPhase;
    }
    
    // Getters
    public double getMaxHealthPercent() { return maxHealthPercent; }
    public double getMinHealthPercent() { return minHealthPercent; }
    public String getDisplayName() { return displayName; }
    public long getAttackInterval() { return attackInterval; }
    public int getActiveAbilityCount() { return activeAbilityCount; }
    public double getMovementSpeedMultiplier() { return movementSpeedMultiplier; }
    public boolean shouldAttackPlayers() { return attackPlayers; }
}

