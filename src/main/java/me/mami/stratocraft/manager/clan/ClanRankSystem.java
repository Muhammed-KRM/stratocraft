package me.mami.stratocraft.manager.clan;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gelişmiş Klan Rütbe Sistemi
 * 
 * Özellikler:
 * - Detaylı yetki sistemi
 * - Elite rütbesi (yeni)
 * - Liderlik devretme ritüeli
 * - Rütbe bazlı izinler
 */
public class ClanRankSystem {
    private final Main plugin;
    private final ClanManager clanManager;
    
    /**
     * Rütbe yetkileri
     */
    public enum Permission {
        BUILD_STRUCTURE,      // Yapı inşa etme
        DESTROY_STRUCTURE,    // Yapı yıkma
        ADD_MEMBER,          // Üye ekleme
        REMOVE_MEMBER,       // Üye çıkarma
        START_WAR,           // Savaş başlatma
        MANAGE_BANK,         // Banka yönetimi
        WITHDRAW_BANK,       // Bankadan para çekme (limitli)
        MANAGE_ALLIANCE,     // İttifak yönetimi
        USE_RITUAL,          // Ritüel kullanma
        START_MISSION,       // Görev başlatma
        TRANSFER_LEADERSHIP  // Liderlik devretme
    }
    
    public ClanRankSystem(Main plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
    }
    
    /**
     * Oyuncunun yetkisi var mı?
     */
    public boolean hasPermission(Clan clan, UUID playerId, Permission permission) {
        if (clan == null || playerId == null || permission == null) return false;
        
        // Klan üyesi mi kontrol et
        if (!clan.getMembers().containsKey(playerId)) {
            return false;
        }
        
        Clan.Rank rank = clan.getRank(playerId);
        if (rank == null) return false;
        
        Set<Permission> rankPermissions = getRankPermissions(rank);
        return rankPermissions != null && rankPermissions.contains(permission);
    }
    
    /**
     * Rütbe yetkilerini al
     */
    private Set<Permission> getRankPermissions(Clan.Rank rank) {
        switch (rank) {
            case LEADER:
                return EnumSet.allOf(Permission.class); // Tüm yetkiler
            case GENERAL:
                return EnumSet.of(
                    Permission.BUILD_STRUCTURE,
                    Permission.DESTROY_STRUCTURE,
                    Permission.ADD_MEMBER,
                    Permission.REMOVE_MEMBER,
                    Permission.START_WAR,
                    Permission.MANAGE_BANK,
                    Permission.MANAGE_ALLIANCE
                );
            case ELITE:
                return EnumSet.of(
                    Permission.BUILD_STRUCTURE,
                    Permission.USE_RITUAL,
                    Permission.WITHDRAW_BANK, // Limitli
                    Permission.START_MISSION
                );
            case MEMBER:
                return EnumSet.of(
                    Permission.BUILD_STRUCTURE // Sadece yapı kullanma
                );
            case RECRUIT:
                return EnumSet.noneOf(Permission.class); // Hiçbir yetki
            default:
                return EnumSet.noneOf(Permission.class);
        }
    }
    
    /**
     * Liderlik devretme ritüeli
     * 
     * Gereksinimler:
     * - Mevcut Lider + Yeni Lider (General olmalı)
     * - Her ikisi de kristale 3 blok yakın
     * - Her ikisinin elinde Nether Star olmalı
     * - Shift + Sağ Tık (birbirlerine)
     */
    public boolean transferLeadership(Player currentLeader, Player newLeader, Location crystalLoc) {
        // Null check
        if (currentLeader == null || newLeader == null || currentLeader.equals(newLeader)) {
            return false;
        }
        
        if (clanManager == null) {
            plugin.getLogger().warning("ClanManager null! Liderlik devretme yapılamıyor.");
            return false;
        }
        
        Clan clan = clanManager.getClanByPlayer(currentLeader.getUniqueId());
        if (clan == null) {
            if (currentLeader != null) {
                currentLeader.sendMessage("§cKlan bulunamadı!");
            }
            return false;
        }
        
        // Aynı klan mı kontrol et
        Clan newLeaderClan = clanManager.getClanByPlayer(newLeader.getUniqueId());
        if (newLeaderClan == null || !newLeaderClan.equals(clan)) {
            if (currentLeader != null) {
                currentLeader.sendMessage("§cHedef oyuncu aynı klanda değil!");
            }
            return false;
        }
        
        // Lider kontrolü
        Clan.Rank currentRank = clan.getRank(currentLeader.getUniqueId());
        if (currentRank == null || currentRank != Clan.Rank.LEADER) {
            return false;
        }
        
        // Yeni lider General olmalı
        Clan.Rank newRank = clan.getRank(newLeader.getUniqueId());
        if (newRank == null || newRank != Clan.Rank.GENERAL) {
            if (currentLeader != null) {
                currentLeader.sendMessage("§cLiderlik devretmek için hedef General olmalı!");
            }
            return false;
        }
        
        // Mesafe kontrolü (3 blok)
        if (crystalLoc != null) {
            try {
                double distance1 = currentLeader.getLocation().distance(crystalLoc);
                double distance2 = newLeader.getLocation().distance(crystalLoc);
                if (distance1 > 3 || distance2 > 3) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                // Farklı dünyalar
                return false;
            }
        }
        
        // Nether Star kontrolü
        if (!hasItemInHand(currentLeader, Material.NETHER_STAR) ||
            !hasItemInHand(newLeader, Material.NETHER_STAR)) {
            return false;
        }
        
        // Liderlik devret (thread-safe: synchronized + setRank kullan)
        synchronized (clan.getMembers()) {
            // Double-check: Lider hala aynı mı?
            if (clan.getRank(currentLeader.getUniqueId()) != Clan.Rank.LEADER) {
                return false;
            }
            
            // setRank metodunu kullan (tutarlılık için)
            clan.setRank(currentLeader.getUniqueId(), Clan.Rank.GENERAL);
            clan.setRank(newLeader.getUniqueId(), Clan.Rank.LEADER);
        }
        
        // Partikül efektleri
        if (crystalLoc != null && crystalLoc.getWorld() != null) {
            try {
                crystalLoc.getWorld().spawnParticle(Particle.TOTEM, crystalLoc, 50, 1, 1, 1, 0.1);
                crystalLoc.getWorld().playSound(crystalLoc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } catch (Exception e) {
                plugin.getLogger().warning("Liderlik devretme efekti hatası: " + e.getMessage());
            }
        }
        
        // Broadcast
        org.bukkit.Bukkit.broadcastMessage("§6§l" + currentLeader.getName() + " liderliği " + 
            newLeader.getName() + " devretti!");
        
        return true;
    }
    
    /**
     * Oyuncunun elinde item var mı?
     */
    private boolean hasItemInHand(Player player, Material material) {
        if (player == null || material == null) return false;
        try {
            ItemStack item = player.getInventory().getItemInMainHand();
            return item != null && item.getType() == material;
        } catch (Exception e) {
            plugin.getLogger().warning("Item kontrolü hatası: " + e.getMessage());
            return false;
        }
    }
}

