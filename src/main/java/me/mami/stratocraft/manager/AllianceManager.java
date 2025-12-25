package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Alliance;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * İttifak Yönetim Sistemi
 * Kontrat sistemine benzer şekilde çalışır
 */
public class AllianceManager {
    private final List<Alliance> activeAlliances = new ArrayList<>();
    private final ClanManager clanManager;
    private final Map<UUID, Long> allianceCooldowns = new HashMap<>(); // Spam önleme
    private static final long ALLIANCE_COOLDOWN = 300000L; // 5 dakika
    
    public AllianceManager(ClanManager cm) {
        this.clanManager = cm;
    }
    
    /**
     * İttifak oluştur
     */
    public Alliance createAlliance(UUID clan1Id, UUID clan2Id, Alliance.Type type, long durationDays) {
        // Zaten ittifak var mı kontrol et
        if (hasAlliance(clan1Id, clan2Id)) {
            return null;
        }
        
        Alliance alliance = new Alliance(clan1Id, clan2Id, type, durationDays);
        activeAlliances.add(alliance);
        
        // ✅ YENİ: allianceClans listelerine ekle
        Clan clan1 = clanManager.getClanById(clan1Id);
        Clan clan2 = clanManager.getClanById(clan2Id);
        if (clan1 != null) {
            clan1.addAllianceClan(clan2Id);
        }
        if (clan2 != null) {
            clan2.addAllianceClan(clan1Id);
        }
        
        return alliance;
    }
    
    /**
     * İttifak var mı?
     */
    public boolean hasAlliance(UUID clan1Id, UUID clan2Id) {
        return activeAlliances.stream()
            .anyMatch(a -> a.isActive() && 
                ((a.getClan1Id().equals(clan1Id) && a.getClan2Id().equals(clan2Id)) ||
                 (a.getClan1Id().equals(clan2Id) && a.getClan2Id().equals(clan1Id))));
    }
    
    /**
     * Aktif ittifakları getir
     */
    public List<Alliance> getAlliances(UUID clanId) {
        List<Alliance> result = new ArrayList<>();
        for (Alliance alliance : activeAlliances) {
            if (alliance.isActive() && alliance.involvesClan(clanId)) {
                result.add(alliance);
            }
        }
        return result;
    }
    
    /**
     * İttifakı ihlal et (ceza uygulanır)
     */
    public void breakAlliance(UUID allianceId, UUID breakerClanId) {
        Alliance alliance = getAlliance(allianceId);
        if (alliance == null || !alliance.isActive()) return;
        
        UUID otherClanId = alliance.getOtherClan(breakerClanId);
        
        alliance.breakAlliance(breakerClanId);
        
        // ✅ YENİ: allianceClans listelerinden kaldır
        Clan breakerClan = clanManager.getClanById(breakerClanId);
        Clan otherClan = clanManager.getClanById(otherClanId);
        if (breakerClan != null) {
            breakerClan.removeAllianceClan(otherClanId);
        }
        if (otherClan != null) {
            otherClan.removeAllianceClan(breakerClanId);
        }
        
        // Cezalar
        if (breakerClan != null) {
            // İhlal cezası: Klan bakiyesinin %20'si
            double penalty = breakerClan.getBalance() * 0.2;
            breakerClan.withdraw(penalty);
            
            // Broadcast
            Bukkit.broadcastMessage("§4§lİTTİFAK İHLALİ! §c" + breakerClan.getName() + 
                " klanı ittifakı bozdu! Cezası: " + penalty + " altın");
            
            // İhlal eden klan üyelerine "Hain" etiketi (Contract sistemindeki gibi)
            for (UUID memberId : breakerClan.getMembers().keySet()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage("§4§l[HAİN] §cİttifak ihlali nedeniyle cezalandırıldınız!");
                }
            }
        }
        
        if (otherClan != null) {
            // Diğer klana tazminat (ihlal eden klanın bakiyesinden)
            if (breakerClan != null && breakerClan.getBalance() > 0) {
                double compensation = breakerClan.getBalance() * 0.1;
                breakerClan.withdraw(compensation);
                otherClan.deposit(compensation);
            }
        }
    }
    
    /**
     * İttifakı bul
     */
    public Alliance getAlliance(UUID allianceId) {
        return activeAlliances.stream()
            .filter(a -> a.getId().equals(allianceId))
            .findFirst().orElse(null);
    }
    
    /**
     * İttifakı sonlandır (karşılıklı)
     */
    public void dissolveAlliance(UUID allianceId, UUID requestingClanId) {
        Alliance alliance = getAlliance(allianceId);
        if (alliance == null || !alliance.isActive()) return;
        
        UUID clan1Id = alliance.getClan1Id();
        UUID clan2Id = alliance.getClan2Id();
        
        // Karşılıklı sonlandırma - ceza yok
        alliance.setActive(false);
        
        // ✅ YENİ: allianceClans listelerinden kaldır
        Clan clan1 = clanManager.getClanById(clan1Id);
        Clan clan2 = clanManager.getClanById(clan2Id);
        if (clan1 != null) {
            clan1.removeAllianceClan(clan2Id);
        }
        if (clan2 != null) {
            clan2.removeAllianceClan(clan1Id);
        }
        
        if (clan1 != null && clan2 != null) {
            Bukkit.broadcastMessage("§e" + clan1.getName() + " ve " + clan2.getName() + 
                " klanları arasındaki ittifak sona erdi.");
        }
    }
    
    /**
     * Süresi dolmuş ittifakları kontrol et
     */
    public void checkExpiredAlliances() {
        for (Alliance alliance : new ArrayList<>(activeAlliances)) {
            if (alliance.isExpired() && alliance.isActive()) {
                UUID clan1Id = alliance.getClan1Id();
                UUID clan2Id = alliance.getClan2Id();
                
                alliance.setActive(false);
                
                // ✅ YENİ: allianceClans listelerinden kaldır
                Clan clan1 = clanManager.getClanById(clan1Id);
                Clan clan2 = clanManager.getClanById(clan2Id);
                if (clan1 != null) {
                    clan1.removeAllianceClan(clan2Id);
                }
                if (clan2 != null) {
                    clan2.removeAllianceClan(clan1Id);
                }
                
                if (clan1 != null && clan2 != null) {
                    Bukkit.broadcastMessage("§7" + clan1.getName() + " ve " + clan2.getName() + 
                        " klanları arasındaki ittifak süresi doldu.");
                }
            }
        }
    }
    
    /**
     * Cooldown kontrolü
     */
    public boolean isOnCooldown(UUID clanId) {
        Long lastTime = allianceCooldowns.get(clanId);
        if (lastTime == null) return false;
        return System.currentTimeMillis() - lastTime < ALLIANCE_COOLDOWN;
    }
    
    public void setCooldown(UUID clanId) {
        allianceCooldowns.put(clanId, System.currentTimeMillis());
    }
    
    public List<Alliance> getAllAlliances() {
        return new ArrayList<>(activeAlliances); // Defensive copy
    }
    
    /**
     * Alliance yükle (DataManager'dan çağrılır)
     */
    public void loadAlliance(Alliance alliance) {
        if (alliance == null) return;
        
        // Duplicate kontrolü: Aynı ID'ye sahip alliance var mı?
        boolean exists = activeAlliances.stream()
            .anyMatch(a -> a.getId().equals(alliance.getId()));
        
        if (!exists) {
            activeAlliances.add(alliance);
        }
    }
}

