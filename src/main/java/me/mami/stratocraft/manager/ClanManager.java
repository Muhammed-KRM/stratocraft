package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.player.PlayerData;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Klan Yönetim Sistemi (Modüler Yapı)
 * 
 * Özellikler:
 * - Thread-safe operations (ConcurrentHashMap)
 * - Null check'ler ve exception handling
 * - Yeni sistemlerle entegrasyon
 * - Config entegrasyonu
 * - Binlerce oyuncu için optimize
 */
public class ClanManager {
    // Thread-safe: ConcurrentHashMap kullan
    private final Map<UUID, Clan> clans = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerClanMap = new ConcurrentHashMap<>();
    
    // Entegrasyon: Diğer sistemler
    private TerritoryManager territoryManager; // Cache güncellemesi için
    private Main plugin; // Plugin referansı (log için)
    
    // Yeni sistemler (setter injection)
    private me.mami.stratocraft.manager.clan.ClanActivitySystem clanActivitySystem;
    private me.mami.stratocraft.manager.clan.ClanBankSystem clanBankSystem;
    private me.mami.stratocraft.manager.clan.ClanMissionSystem clanMissionSystem;
    
    // Yeni Model Sistemi
    private PlayerDataManager playerDataManager;
    
    public ClanManager() {
        // Varsayılan constructor
    }
    
    public ClanManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Plugin setter
     */
    public void setPlugin(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * TerritoryManager setter (Cache güncellemesi için)
     */
    public void setTerritoryManager(TerritoryManager tm) {
        this.territoryManager = tm;
    }
    
    /**
     * Yeni sistemler setter'ları
     */
    public void setClanActivitySystem(me.mami.stratocraft.manager.clan.ClanActivitySystem system) {
        this.clanActivitySystem = system;
    }
    
    public void setClanBankSystem(me.mami.stratocraft.manager.clan.ClanBankSystem system) {
        this.clanBankSystem = system;
    }
    
    public void setClanMissionSystem(me.mami.stratocraft.manager.clan.ClanMissionSystem system) {
        this.clanMissionSystem = system;
    }
    
    /**
     * PlayerDataManager setter (Yeni Model Sistemi)
     */
    public void setPlayerDataManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }
    
    /**
     * Klan oluştur (null check ve validation ile)
     */
    public Clan createClan(String name, UUID leader) {
        // Null check
        if (name == null || leader == null) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan oluşturma hatası: name veya leader null!");
            }
            return null;
        }
        
        // İsim validasyonu
        name = name.trim();
        if (name.isEmpty() || name.length() > 32) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan ismi geçersiz: " + name);
            }
            return null;
        }
        
        // Lider zaten bir klana üye mi?
        if (getClanByPlayer(leader) != null) {
            return null;
        }
        
        // Aynı isimde klan var mı?
        if (getClanByName(name) != null) {
            return null;
        }
        
        try {
            // Klan oluştur
            Clan c = new Clan(name, leader);
            clans.put(c.getId(), c);
            playerClanMap.put(leader, c.getId());
            
            // YENİ MODEL: PlayerData güncelle (klan oluşturma)
            if (playerDataManager != null) {
                playerDataManager.setClan(leader, c.getId(), Clan.Rank.LEADER);
            }
            
            // Yeni sistemler: Aktivite güncelle
            if (clanActivitySystem != null) {
                clanActivitySystem.updateActivity(leader);
            }
            
            // Cache'i güncelle
            if (territoryManager != null) {
                territoryManager.setCacheDirty();
            }
            
            return c;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan oluşturma hatası: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
    
    /**
     * Oyuncuya göre klan getir
     */
    public Clan getClanByPlayer(UUID uuid) {
        if (uuid == null) return null;
        
        UUID clanId = playerClanMap.get(uuid);
        if (clanId == null) return null;
        
        return clans.get(clanId);
    }
    
    /**
     * Tüm klanları getir (thread-safe copy)
     */
    public Collection<Clan> getAllClans() {
        // Thread-safe: Copy of values
        return new ArrayList<>(clans.values());
    }
    
    /**
     * UUID ile klan getir
     */
    public Clan getClan(UUID clanId) {
        if (clanId == null) return null;
        return clans.get(clanId);
    }
    
    /**
     * UUID ile klan getir (alias - getClanById)
     */
    public Clan getClanById(UUID clanId) {
        return getClan(clanId);
    }
    
    /**
     * İsim ile klan getir (optimize edilmiş)
     */
    public Clan getClanByName(String name) {
        if (name == null || name.isEmpty()) return null;
        
        // Thread-safe: Copy of values
        Collection<Clan> allClans = getAllClans();
        
        for (Clan clan : allClans) {
            if (clan != null && clan.getName() != null && 
                clan.getName().equalsIgnoreCase(name)) {
                return clan;
            }
        }
        
        return null;
    }
    
    /**
     * Üye ekle (null check ve validation ile)
     */
    public void addMember(Clan clan, UUID memberId, Clan.Rank rank) {
        if (clan == null || memberId == null || rank == null) {
            if (plugin != null) {
                plugin.getLogger().warning("Üye ekleme hatası: clan, memberId veya rank null!");
            }
            return;
        }
        
        // Üye zaten bir klana üye mi?
        Clan existingClan = getClanByPlayer(memberId);
        if (existingClan != null && !existingClan.equals(clan)) {
            if (plugin != null) {
                plugin.getLogger().warning("Üye zaten başka bir klana üye: " + memberId);
            }
            return;
        }
        
        try {
            clan.addMember(memberId, rank);
            playerClanMap.put(memberId, clan.getId());
            
            // YENİ MODEL: PlayerData güncelle
            if (playerDataManager != null) {
                playerDataManager.setClan(memberId, clan.getId(), rank);
            }
            
            // Yeni sistemler: Aktivite güncelle
            if (clanActivitySystem != null) {
                clanActivitySystem.updateActivity(memberId);
            }
            
            // Cache'i güncelle
            if (territoryManager != null) {
                territoryManager.setCacheDirty();
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Üye ekleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Üye çıkar (null check ve validation ile)
     */
    public void removeMember(Clan clan, UUID memberId) {
        if (clan == null || memberId == null) return;
        
        try {
            // YENİ: Oyuncu online ise yapı efektlerini kaldır
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline() && plugin != null) {
                me.mami.stratocraft.manager.StructureEffectManager effectManager = 
                    plugin.getStructureEffectManager();
                if (effectManager != null) {
                    effectManager.removeStructureEffects(player);
                    effectManager.onPlayerQuit(player); // Aktif efektleri temizle
                }
            }
            
            // Thread-safe: Synchronized
            synchronized (clan.getMembers()) {
                clan.getMembers().remove(memberId);
            }
            playerClanMap.remove(memberId);
            
            // YENİ MODEL: PlayerData güncelle
            if (playerDataManager != null) {
                playerDataManager.leaveClan(memberId);
            }
            
            // Cache'i güncelle
            if (territoryManager != null) {
                territoryManager.setCacheDirty();
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Üye çıkarma hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Klan dağıt (null check ve validation ile)
     */
    public void disbandClan(Clan clan) {
        if (clan == null) return;
        
        try {
            String clanName = clan.getName() != null ? clan.getName() : "Bilinmeyen";
            
            // Thread-safe: Copy of keySet
            Set<UUID> memberIds = new HashSet<>(clan.getMembers().keySet());
            
            // Tüm üyeleri playerClanMap'ten çıkar ve PlayerData güncelle
            for (UUID memberId : memberIds) {
                playerClanMap.remove(memberId);
                
                // YENİ MODEL: PlayerData güncelle (klan dağıtma)
                if (playerDataManager != null) {
                    playerDataManager.leaveClan(memberId);
                }
            }
            
            // YENİ: TerritoryBoundaryManager cache'ini temizle (memory leak önleme)
            if (territoryManager != null && plugin != null) {
                me.mami.stratocraft.manager.TerritoryBoundaryManager boundaryManager = 
                    plugin.getTerritoryBoundaryManager();
                if (boundaryManager != null) {
                    boundaryManager.removeTerritoryData(clan);
                }
            }
            
            // YENİ: Klan yapılarının aktifliklerini kaldır
            if (plugin != null && plugin.getStructureCoreManager() != null) {
                for (Structure structure : clan.getStructures()) {
                    if (structure != null && structure.getLocation() != null) {
                        // Yapıyı pasifleştir (aktiflik kaldır)
                        plugin.getStructureCoreManager().removeStructure(structure.getLocation());
                    }
                }
            }
            
            // YENİ: Klan kristalini temizle
            clan.setCrystalLocation(null);
            
            // Klanı listeden çıkar
            clans.remove(clan.getId());
            
            // Broadcast
            Bukkit.broadcastMessage("§c" + clanName + " klanı dağıtıldı.");
            
            // Cache'i güncelle
            if (territoryManager != null) {
                territoryManager.setCacheDirty();
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan dağıtma hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Rütbe değiştir (PlayerData güncellemesi ile)
     */
    public void changeRank(Clan clan, UUID memberId, Clan.Rank newRank) {
        if (clan == null || memberId == null || newRank == null) return;
        
        // Klan üyesi mi kontrol et
        if (!clan.getMembers().containsKey(memberId)) {
            if (plugin != null) {
                plugin.getLogger().warning("Rütbe değiştirme hatası: Oyuncu klan üyesi değil: " + memberId);
            }
            return;
        }
        
        try {
            // Rütbeyi değiştir
            clan.setRank(memberId, newRank);
            
            // YENİ MODEL: PlayerData güncelle
            if (playerDataManager != null) {
                playerDataManager.setClan(memberId, clan.getId(), newRank);
            }
            
            // Cache'i güncelle
            if (territoryManager != null) {
                territoryManager.setCacheDirty();
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Rütbe değiştirme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Klan yükle (DataManager için - null check ile)
     */
    public void loadClan(Clan clan) {
        if (clan == null) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan yükleme hatası: clan null!");
            }
            return;
        }
        
        try {
            clans.put(clan.getId(), clan);
            
            // Thread-safe: Copy of keySet
            Set<UUID> memberIds = new HashSet<>(clan.getMembers().keySet());
            
            for (UUID memberId : memberIds) {
                playerClanMap.put(memberId, clan.getId());
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan yükleme hatası: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Klan sayısı
     */
    public int getClanCount() {
        return clans.size();
    }
    
    /**
     * Toplam üye sayısı
     */
    public int getTotalMemberCount() {
        return playerClanMap.size();
    }
}

