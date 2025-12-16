package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import me.mami.stratocraft.enums.ContractType;
import me.mami.stratocraft.enums.PenaltyType;
import me.mami.stratocraft.gui.ContractMenu;
import me.mami.stratocraft.model.ContractTerms;

/**
 * Kontrat Şartları Yöneticisi
 * Çift taraflı kontrat sistemi için şart yönetimi
 */
public class ContractTermsManager {
    private final List<ContractTerms> allTerms = new ArrayList<>();
    private final me.mami.stratocraft.Main plugin;
    
    public ContractTermsManager(me.mami.stratocraft.Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Şart oluşturma (Wizard state'den)
     */
    public ContractTerms createTerms(UUID requestId, UUID playerId, ContractMenu.ContractWizardState state) {
        if (state == null || state.contractType == null) return null;
        
        ContractTerms terms = new ContractTerms(requestId, playerId, state.contractType);
        
        // Tip'e göre parametreleri set et
        switch (state.contractType) {
            case RESOURCE_COLLECTION:
                terms.setMaterial(state.material);
                terms.setAmount(state.amount);
                break;
            case COMBAT:
                terms.setTargetPlayer(state.targetPlayer);
                break;
            case TERRITORY:
                if (state.restrictedAreas != null) {
                    terms.setRestrictedAreas(state.restrictedAreas);
                }
                terms.setRestrictedRadius(state.restrictedRadius > 0 ? state.restrictedRadius : 50);
                break;
            case CONSTRUCTION:
                terms.setStructureType(state.structureType);
                break;
        }
        
        // Genel parametreler
        terms.setDeadline(System.currentTimeMillis() + (long)(state.deadlineDays * 24 * 60 * 60 * 1000));
        terms.setReward(state.reward);
        terms.setPenaltyType(state.penaltyType != null ? state.penaltyType : me.mami.stratocraft.enums.PenaltyType.BANK_PENALTY);
        terms.setPenalty(state.penalty);
        
        allTerms.add(terms);
        return terms;
    }
    
    /**
     * Şart güncelleme
     */
    public boolean updateTerms(UUID termsId, ContractMenu.ContractWizardState state) {
        ContractTerms terms = getTerms(termsId);
        if (terms == null || state == null) return false;
        
        // Tip'e göre parametreleri güncelle
        if (state.contractType != null) {
            terms.setType(state.contractType);
            
            switch (state.contractType) {
                case RESOURCE_COLLECTION:
                    terms.setMaterial(state.material);
                    terms.setAmount(state.amount);
                    break;
                case COMBAT:
                    terms.setTargetPlayer(state.targetPlayer);
                    break;
                case TERRITORY:
                    if (state.restrictedAreas != null) {
                        terms.setRestrictedAreas(state.restrictedAreas);
                    }
                    terms.setRestrictedRadius(state.restrictedRadius > 0 ? state.restrictedRadius : 50);
                    break;
                case CONSTRUCTION:
                    terms.setStructureType(state.structureType);
                    break;
            }
        }
        
        // Genel parametreleri güncelle
        if (state.deadlineDays > 0) {
            terms.setDeadline(System.currentTimeMillis() + (long)(state.deadlineDays * 24 * 60 * 60 * 1000));
        }
        if (state.reward > 0) {
            terms.setReward(state.reward);
        }
        if (state.penaltyType != null) {
            terms.setPenaltyType(state.penaltyType);
        }
        if (state.penalty >= 0) {
            terms.setPenalty(state.penalty);
        }
        
        return true;
    }
    
    /**
     * Şart onaylama
     */
    public boolean approveTerms(UUID termsId, UUID playerId) {
        ContractTerms terms = getTerms(termsId);
        if (terms == null) return false;
        
        // Sadece şart sahibi onaylayabilir
        if (!terms.getPlayerId().equals(playerId)) return false;
        
        terms.setApproved(true);
        return true;
    }
    
    /**
     * Şart getirme
     */
    public ContractTerms getTerms(UUID termsId) {
        return allTerms.stream()
            .filter(t -> t.getId().equals(termsId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * İsteğe göre şart getirme
     */
    public ContractTerms getTermsByRequest(UUID requestId, UUID playerId) {
        return allTerms.stream()
            .filter(t -> t.getContractRequestId().equals(requestId) && 
                        t.getPlayerId().equals(playerId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Oyuncunun şartlarını getir
     */
    public List<ContractTerms> getTermsByPlayer(UUID playerId) {
        return allTerms.stream()
            .filter(t -> t.getPlayerId().equals(playerId))
            .collect(Collectors.toList());
    }
    
    /**
     * İsteğe ait tüm şartları getir
     */
    public List<ContractTerms> getTermsByRequest(UUID requestId) {
        return allTerms.stream()
            .filter(t -> t.getContractRequestId().equals(requestId))
            .collect(Collectors.toList());
    }
    
    /**
     * Tüm şartları getir (veritabanı için)
     */
    public List<ContractTerms> getAllTerms() {
        return new ArrayList<>(allTerms);
    }
    
    /**
     * Şart ekle (veritabanından yükleme için)
     */
    public void addTerms(ContractTerms terms) {
        if (terms != null && getTerms(terms.getId()) == null) {
            allTerms.add(terms);
        }
    }
    
    /**
     * Şart kaldır
     */
    public void removeTerms(UUID termsId) {
        allTerms.removeIf(t -> t.getId().equals(termsId));
    }
}
