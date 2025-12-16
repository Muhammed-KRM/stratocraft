package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.mami.stratocraft.model.Contract;
import me.mami.stratocraft.model.ContractRequest;

/**
 * Kontrat İsteği Yöneticisi
 * Çift taraflı kontrat sistemi için istek yönetimi
 */
public class ContractRequestManager {
    private final List<ContractRequest> requests = new ArrayList<>();
    private final me.mami.stratocraft.Main plugin;
    
    public ContractRequestManager(me.mami.stratocraft.Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * İstek gönderme
     */
    public ContractRequest sendRequest(UUID sender, UUID target, Contract.ContractScope scope) {
        // Aynı istek zaten var mı kontrol et
        ContractRequest existing = requests.stream()
            .filter(r -> r.getSender().equals(sender) && 
                        r.getTarget().equals(target) && 
                        r.getStatus() == ContractRequest.ContractRequestStatus.PENDING)
            .findFirst()
            .orElse(null);
        
        if (existing != null) {
            return null; // Zaten bekleyen bir istek var
        }
        
        ContractRequest request = new ContractRequest(sender, target, scope);
        requests.add(request);
        
        // Bildirim gönder
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            Player senderPlayer = Bukkit.getPlayer(sender);
            String senderName = senderPlayer != null ? senderPlayer.getName() : "Bilinmeyen";
            targetPlayer.sendMessage("§6═══════════════════════════════════");
            targetPlayer.sendMessage("§e§lYENİ KONTRAT İSTEĞİ!");
            targetPlayer.sendMessage("§7" + senderName + " size kontrat isteği gönderdi");
            targetPlayer.sendMessage("§7Pusuladan kontrol edebilirsiniz");
            targetPlayer.sendMessage("§6═══════════════════════════════════");
        }
        
        return request;
    }
    
    /**
     * İstek kabul etme
     */
    public boolean acceptRequest(UUID requestId, UUID playerId) {
        ContractRequest request = getRequest(requestId);
        if (request == null) return false;
        
        // Sadece hedef oyuncu kabul edebilir
        if (!request.getTarget().equals(playerId)) return false;
        
        // Sadece PENDING durumundaki istekler kabul edilebilir
        if (request.getStatus() != ContractRequest.ContractRequestStatus.PENDING) return false;
        
        request.setStatus(ContractRequest.ContractRequestStatus.ACCEPTED);
        
        // Bildirim gönder
        Player senderPlayer = Bukkit.getPlayer(request.getSender());
        if (senderPlayer != null && senderPlayer.isOnline()) {
            Player targetPlayer = Bukkit.getPlayer(request.getTarget());
            String targetName = targetPlayer != null ? targetPlayer.getName() : "Bilinmeyen";
            senderPlayer.sendMessage("§6═══════════════════════════════════");
            senderPlayer.sendMessage("§a§lKONTRAT İSTEĞİ KABUL EDİLDİ!");
            senderPlayer.sendMessage("§7" + targetName + " isteğinizi kabul etti");
            senderPlayer.sendMessage("§7Şimdi şartlarınızı belirleyebilirsiniz");
            senderPlayer.sendMessage("§6═══════════════════════════════════");
        }
        
        return true;
    }
    
    /**
     * İstek reddetme
     */
    public boolean rejectRequest(UUID requestId, UUID playerId) {
        ContractRequest request = getRequest(requestId);
        if (request == null) return false;
        
        // Sadece hedef oyuncu reddedebilir
        if (!request.getTarget().equals(playerId)) return false;
        
        // Sadece PENDING durumundaki istekler reddedilebilir
        if (request.getStatus() != ContractRequest.ContractRequestStatus.PENDING) return false;
        
        request.setStatus(ContractRequest.ContractRequestStatus.REJECTED);
        
        // Bildirim gönder
        Player senderPlayer = Bukkit.getPlayer(request.getSender());
        if (senderPlayer != null && senderPlayer.isOnline()) {
            Player targetPlayer = Bukkit.getPlayer(request.getTarget());
            String targetName = targetPlayer != null ? targetPlayer.getName() : "Bilinmeyen";
            senderPlayer.sendMessage("§c" + targetName + " kontrat isteğinizi reddetti.");
        }
        
        return true;
    }
    
    /**
     * İstek iptal etme
     */
    public boolean cancelRequest(UUID requestId, UUID playerId) {
        ContractRequest request = getRequest(requestId);
        if (request == null) return false;
        
        // Sadece gönderen iptal edebilir
        if (!request.getSender().equals(playerId)) return false;
        
        // Sadece PENDING durumundaki istekler iptal edilebilir
        if (request.getStatus() != ContractRequest.ContractRequestStatus.PENDING) return false;
        
        request.setStatus(ContractRequest.ContractRequestStatus.CANCELLED);
        
        return true;
    }
    
    /**
     * İstek getirme
     */
    public ContractRequest getRequest(UUID requestId) {
        return requests.stream()
            .filter(r -> r.getId().equals(requestId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Bekleyen istekleri getir (belirli oyuncu için)
     */
    public List<ContractRequest> getPendingRequests(UUID playerId) {
        return requests.stream()
            .filter(r -> r.getTarget().equals(playerId) && 
                        r.getStatus() == ContractRequest.ContractRequestStatus.PENDING)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Kabul edilmiş istekleri getir (belirli oyuncu için)
     */
    public List<ContractRequest> getAcceptedRequests(UUID playerId) {
        return requests.stream()
            .filter(r -> (r.getSender().equals(playerId) || r.getTarget().equals(playerId)) &&
                        r.getStatus() == ContractRequest.ContractRequestStatus.ACCEPTED)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Oyuncunun gönderdiği bekleyen istekleri getir
     */
    public List<ContractRequest> getSentPendingRequests(UUID playerId) {
        return requests.stream()
            .filter(r -> r.getSender().equals(playerId) && 
                        r.getStatus() == ContractRequest.ContractRequestStatus.PENDING)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Tüm istekleri getir (veritabanı için)
     */
    public List<ContractRequest> getAllRequests() {
        return new ArrayList<>(requests);
    }
    
    /**
     * İstek ekle (veritabanından yükleme için)
     */
    public void addRequest(ContractRequest request) {
        if (request != null && getRequest(request.getId()) == null) {
            requests.add(request);
        }
    }
    
    /**
     * İstek kaldır
     */
    public void removeRequest(UUID requestId) {
        requests.removeIf(r -> r.getId().equals(requestId));
    }
}
