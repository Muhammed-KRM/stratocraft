package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.PeaceRequest;

/**
 * Barış Anlaşması İstek Yönetim Sistemi
 * 
 * Kontrat sistemine benzer şekilde çalışır
 */
public class PeaceRequestManager {
    private final List<PeaceRequest> activeRequests = new ArrayList<>();
    private final ClanManager clanManager;
    private final SiegeManager siegeManager;
    
    public PeaceRequestManager(ClanManager cm, SiegeManager sm) {
        this.clanManager = cm;
        this.siegeManager = sm;
    }
    
    /**
     * Barış anlaşması isteği gönder
     */
    public PeaceRequest sendPeaceRequest(UUID senderClanId, UUID targetClanId) {
        // Klanlar var mı?
        Clan sender = clanManager.getClanById(senderClanId);
        Clan target = clanManager.getClanById(targetClanId);
        
        if (sender == null || target == null) {
            return null;
        }
        
        // Aynı klan mı?
        if (senderClanId.equals(targetClanId)) {
            return null;
        }
        
        // Savaşta mılar?
        if (!sender.isAtWarWith(targetClanId)) {
            return null; // Savaşta değiller, barış anlaşması gerekmez
        }
        
        // Zaten aktif istek var mı?
        if (hasActiveRequest(senderClanId, targetClanId)) {
            return null;
        }
        
        // Yeni istek oluştur
        PeaceRequest request = new PeaceRequest(senderClanId, targetClanId);
        activeRequests.add(request);
        
        // Broadcast
        Bukkit.broadcastMessage("§e" + sender.getName() + " klanı, " + target.getName() + 
            " klanına barış anlaşması teklifi gönderdi!");
        
        return request;
    }
    
    /**
     * Aktif istek var mı?
     */
    public boolean hasActiveRequest(UUID clan1Id, UUID clan2Id) {
        return activeRequests.stream()
            .anyMatch(r -> r.isValid() && 
                ((r.getSenderClanId().equals(clan1Id) && r.getTargetClanId().equals(clan2Id)) ||
                 (r.getSenderClanId().equals(clan2Id) && r.getTargetClanId().equals(clan1Id))));
    }
    
    /**
     * İsteği onayla
     */
    public boolean acceptRequest(UUID requestId, UUID acceptingClanId) {
        PeaceRequest request = getRequest(requestId);
        if (request == null || !request.isValid()) {
            return false;
        }
        
        // İsteği alan klan onaylamalı
        if (!request.getTargetClanId().equals(acceptingClanId)) {
            return false;
        }
        
        // Onayla
        request.accept();
        
        // Savaşı bitir
        Clan sender = clanManager.getClanById(request.getSenderClanId());
        Clan target = clanManager.getClanById(request.getTargetClanId());
        
        if (sender != null && target != null) {
            // Savaşı bitir (iki taraflı)
            siegeManager.endWar(sender, target);
            
            // Broadcast
            Bukkit.broadcastMessage("§a§lBARIŞ ANLAŞMASI! §e" + sender.getName() + 
                " ve " + target.getName() + " klanları barış imzaladı!");
        }
        
        return true;
    }
    
    /**
     * İsteği reddet
     */
    public boolean rejectRequest(UUID requestId, UUID rejectingClanId) {
        PeaceRequest request = getRequest(requestId);
        if (request == null || !request.isValid()) {
            return false;
        }
        
        // İsteği alan klan reddetmeli
        if (!request.getTargetClanId().equals(rejectingClanId)) {
            return false;
        }
        
        request.reject();
        return true;
    }
    
    /**
     * İsteği bul
     */
    public PeaceRequest getRequest(UUID requestId) {
        return activeRequests.stream()
            .filter(r -> r.getId().equals(requestId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Klanın gönderdiği istekler
     */
    public List<PeaceRequest> getSentRequests(UUID clanId) {
        return activeRequests.stream()
            .filter(r -> r.getSenderClanId().equals(clanId) && r.isValid())
            .collect(Collectors.toList());
    }
    
    /**
     * Klanın aldığı istekler
     */
    public List<PeaceRequest> getReceivedRequests(UUID clanId) {
        return activeRequests.stream()
            .filter(r -> r.getTargetClanId().equals(clanId) && r.isValid())
            .collect(Collectors.toList());
    }
    
    /**
     * Klanın tüm istekleri (gönderilen + alınan)
     */
    public List<PeaceRequest> getAllRequests(UUID clanId) {
        return activeRequests.stream()
            .filter(r -> r.involvesClan(clanId) && r.isValid())
            .collect(Collectors.toList());
    }
    
    /**
     * Süresi dolmuş istekleri temizle
     */
    public void cleanupExpiredRequests() {
        activeRequests.removeIf(r -> r.isExpired() || r.isAccepted() || r.isRejected());
    }
    
    /**
     * İsteği yükle (DataManager'dan çağrılır)
     */
    public void loadRequest(PeaceRequest request) {
        if (request == null) return;
        
        boolean exists = activeRequests.stream()
            .anyMatch(r -> r.getId().equals(request.getId()));
        
        if (!exists) {
            activeRequests.add(request);
        }
    }
    
    /**
     * Tüm istekleri getir
     */
    public List<PeaceRequest> getAllRequests() {
        return new ArrayList<>(activeRequests);
    }
    
    /**
     * Tüm aktif istekleri Map olarak getir (ID -> Request)
     * AdminCommandExecutor uyumluluğu için
     */
    public Map<UUID, PeaceRequest> getAllActiveRequests() {
        Map<UUID, PeaceRequest> result = new HashMap<>();
        for (PeaceRequest request : activeRequests) {
            if (request.isValid()) {
                result.put(request.getId(), request);
            }
        }
        return result;
    }
}

