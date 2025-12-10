package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.manager.clan.ClanActivitySystem;
import me.mami.stratocraft.manager.clan.ClanLevelBonusSystem;
import me.mami.stratocraft.manager.clan.ClanMissionSystem;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * ClanStatsMenu - Klan İstatistikleri GUI Menüsü
 * 
 * Özellikler:
 * - Klan genel istatistikleri (güç, seviye, yapı sayısı)
 * - Üye istatistikleri (en aktif, en güçlü)
 * - Görev başarı oranı
 * - Savaş geçmişi (gelecekte eklenecek)
 * - Klan seviye bonusları
 */
public class ClanStatsMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    
    public ClanStatsMenu(Main plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
    }
    
    /**
     * Ana istatistik menüsünü aç
     */
    public void openMenu(Player player) {
        if (player == null) return;
        
        Clan clan = clanManager != null ? clanManager.getClanByPlayer(player.getUniqueId()) : null;
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // 54 slotlu menü oluştur (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Klan İstatistikleri");
        
        // Genel Bilgiler (Slot 4)
        menu.setItem(4, createGeneralInfoItem(clan));
        
        // Güç İstatistikleri (Slot 10)
        menu.setItem(10, createPowerStatsItem(clan));
        
        // Üye İstatistikleri (Slot 12)
        menu.setItem(12, createMemberStatsItem(clan));
        
        // Yapı İstatistikleri (Slot 14)
        menu.setItem(14, createStructureStatsItem(clan));
        
        // Görev İstatistikleri (Slot 16)
        menu.setItem(16, createMissionStatsItem(clan));
        
        // Seviye Bonusları (Slot 22)
        menu.setItem(22, createLevelBonusItem(clan));
        
        // En Aktif Üyeler (Slot 28-35)
        createTopActiveMembers(menu, clan, 28);
        
        // En Güçlü Üyeler (Slot 37-44)
        createTopPowerfulMembers(menu, clan, 37);
        
        // Geri butonu (Slot 0)
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Genel bilgiler item'ı
     */
    private ItemStack createGeneralInfoItem(Clan clan) {
        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§6§l" + clan.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // Klan seviyesi
        int clanLevel = calculateClanLevel(clan);
        lore.add("§7Seviye: §e" + clanLevel);
        
        // Üye sayısı
        int memberCount = clan.getMembers() != null ? clan.getMembers().size() : 0;
        lore.add("§7Üye Sayısı: §e" + memberCount);
        
        // Online üye sayısı
        int onlineCount = getOnlineMemberCount(clan);
        lore.add("§7Online: §a" + onlineCount + "§7/§e" + memberCount);
        
        // Kuruluş tarihi
        if (clan.getCreatedAt() > 0) {
            long daysSinceCreation = (System.currentTimeMillis() - clan.getCreatedAt()) / (1000 * 60 * 60 * 24);
            lore.add("§7Kuruluş: §e" + daysSinceCreation + " gün önce");
        }
        
        // Bölge bilgisi
        if (clan.getTerritory() != null) {
            lore.add("§7Bölge Yarıçapı: §e" + clan.getTerritory().getRadius() + " blok");
        }
        
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Güç istatistikleri item'ı
     */
    private ItemStack createPowerStatsItem(Clan clan) {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§cGüç İstatistikleri");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // Klan toplam gücü
        double totalPower = calculateClanTotalPower(clan);
        lore.add("§7Toplam Güç: §e" + String.format("%.2f", totalPower));
        
        // Ortalama üye gücü
        int memberCount = clan.getMembers() != null ? clan.getMembers().size() : 1;
        double avgPower = totalPower / memberCount;
        lore.add("§7Ortalama Üye Gücü: §e" + String.format("%.2f", avgPower));
        
        // En güçlü üye
        UUID strongestMember = getStrongestMember(clan);
        if (strongestMember != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(strongestMember);
            lore.add("§7En Güçlü Üye: §e" + (player.getName() != null ? player.getName() : "Bilinmeyen"));
        }
        
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Üye istatistikleri item'ı
     */
    private ItemStack createMemberStatsItem(Clan clan) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§aÜye İstatistikleri");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // Toplam üye
        int totalMembers = clan.getMembers() != null ? clan.getMembers().size() : 0;
        lore.add("§7Toplam Üye: §e" + totalMembers);
        
        // Online/Offline
        int onlineCount = getOnlineMemberCount(clan);
        lore.add("§7Online: §a" + onlineCount);
        lore.add("§7Offline: §7" + (totalMembers - onlineCount));
        
        // Rütbe dağılımı
        Map<Clan.Rank, Integer> rankDistribution = getRankDistribution(clan);
        lore.add("§7═══════════════════════");
        lore.add("§7Rütbe Dağılımı:");
        for (Map.Entry<Clan.Rank, Integer> entry : rankDistribution.entrySet()) {
            String rankName = getRankName(entry.getKey());
            lore.add("§7  " + rankName + ": §e" + entry.getValue());
        }
        
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Yapı istatistikleri item'ı
     */
    private ItemStack createStructureStatsItem(Clan clan) {
        ItemStack item = new ItemStack(Material.STRUCTURE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§eYapı İstatistikleri");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // Toplam yapı sayısı
        int structureCount = clan.getStructures() != null ? clan.getStructures().size() : 0;
        lore.add("§7Toplam Yapı: §e" + structureCount);
        
        // Teknoloji seviyesi
        int techLevel = clan.getTechLevel();
        lore.add("§7Teknoloji Seviyesi: §e" + techLevel);
        
        // Yapı tipleri (gelecekte detaylandırılabilir)
        if (structureCount > 0) {
            lore.add("§7═══════════════════════");
            lore.add("§7Yapı detayları için");
            lore.add("§7yapı menüsünü kullanın");
        }
        
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Görev istatistikleri item'ı
     */
    private ItemStack createMissionStatsItem(Clan clan) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§bGörev İstatistikleri");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // Görev sistemi kontrolü
        ClanMissionSystem missionSystem = plugin != null ? plugin.getClanMissionSystem() : null;
        if (missionSystem != null) {
            // Aktif görev
            ClanMissionSystem.ClanMission activeMission = missionSystem.getActiveMission(clan);
            if (activeMission != null) {
                lore.add("§7Aktif Görev: §aVar");
                lore.add("§7Tip: §e" + getMissionTypeName(activeMission.getType()));
                lore.add("§7İlerleme: §e" + activeMission.getProgress() + 
                    "§7/§e" + activeMission.getTarget());
            } else {
                lore.add("§7Aktif Görev: §7Yok");
            }
        } else {
            lore.add("§7Görev Sistemi: §cAktif Değil");
        }
        
        lore.add("§7═══════════════════════");
        lore.add("§7Geçmiş görevler için");
        lore.add("§7görev menüsünü kullanın");
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Seviye bonusları item'ı
     */
    private ItemStack createLevelBonusItem(Clan clan) {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§6Seviye Bonusları");
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        int clanLevel = calculateClanLevel(clan);
        lore.add("§7Mevcut Seviye: §e" + clanLevel);
        
        // Seviye bonusları
        ClanLevelBonusSystem levelBonusSystem = plugin != null ? plugin.getClanLevelBonusSystem() : null;
        if (levelBonusSystem != null) {
            double powerBonus = levelBonusSystem.getClanPowerBonus(clan);
            if (powerBonus > 0) {
                lore.add("§7Güç Bonusu: §a+" + String.format("%.1f", powerBonus * 100) + "%");
            }
            
            // Özellik erişimi
            lore.add("§7═══════════════════════");
            lore.add("§7Erişilebilir Özellikler:");
            
            if (levelBonusSystem.hasClanFeature(clan, ClanLevelBonusSystem.ClanFeature.SPECIAL_STRUCTURES)) {
                lore.add("§a  ✓ Gelişmiş Yapılar");
            }
            if (levelBonusSystem.hasClanFeature(clan, ClanLevelBonusSystem.ClanFeature.ALLIANCE_SYSTEM)) {
                lore.add("§a  ✓ İttifak Sistemi");
            }
            if (levelBonusSystem.hasClanFeature(clan, ClanLevelBonusSystem.ClanFeature.CLAN_WARS)) {
                lore.add("§a  ✓ Kuşatma Silahları");
            }
        } else {
            lore.add("§7Bonus Sistemi: §cAktif Değil");
        }
        
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * En aktif üyeleri göster
     */
    private void createTopActiveMembers(Inventory menu, Clan clan, int startSlot) {
        ClanActivitySystem activitySystem = plugin != null ? plugin.getClanActivitySystem() : null;
        if (activitySystem == null || clan.getMembers() == null) return;
        
        // En aktif üyeleri al
        List<UUID> activeMembers = activitySystem.getMostActiveMembers(clan, 8);
        
        int slot = startSlot;
        for (UUID memberId : activeMembers) {
            if (slot > startSlot + 7) break;
            
            OfflinePlayer player = Bukkit.getOfflinePlayer(memberId);
            String name = player.getName();
            if (name == null) name = "Bilinmeyen";
            
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a" + name);
                
                List<String> lore = new ArrayList<>();
                long lastActivity = activitySystem.getLastActivity(memberId);
                long inactiveTime = System.currentTimeMillis() - lastActivity;
                long days = inactiveTime / (1000 * 60 * 60 * 24);
                
                if (days == 0) {
                    lore.add("§7Son Görülme: §aBugün");
                } else if (days == 1) {
                    lore.add("§7Son Görülme: §eDün");
                } else {
                    lore.add("§7Son Görülme: §e" + days + " gün önce");
                }
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot, item);
            slot++;
        }
    }
    
    /**
     * En güçlü üyeleri göster
     */
    private void createTopPowerfulMembers(Inventory menu, Clan clan, int startSlot) {
        if (clan.getMembers() == null) return;
        
        StratocraftPowerSystem powerSystem = plugin != null ? plugin.getStratocraftPowerSystem() : null;
        if (powerSystem == null) return;
        
        // Üye güçlerini hesapla ve sırala
        List<Map.Entry<UUID, Double>> memberPowers = new ArrayList<>();
        for (UUID memberId : clan.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                try {
                    double power = powerSystem.calculatePlayerProfile(player).getTotalCombatPower();
                    memberPowers.add(new AbstractMap.SimpleEntry<>(memberId, power));
                } catch (Exception e) {
                    // Hata durumunda atla
                }
            }
        }
        
        // Güce göre sırala
        memberPowers.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        int slot = startSlot;
        for (int i = 0; i < Math.min(8, memberPowers.size()); i++) {
            if (slot > startSlot + 7) break;
            
            Map.Entry<UUID, Double> entry = memberPowers.get(i);
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            String name = player.getName();
            if (name == null) name = "Bilinmeyen";
            
            ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§c" + name);
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Güç: §e" + String.format("%.2f", entry.getValue()));
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot, item);
            slot++;
        }
    }
    
    /**
     * Yardımcı metodlar
     */
    private int calculateClanLevel(Clan clan) {
        if (clan == null) return 1;
        
        ClanLevelBonusSystem levelBonusSystem = plugin != null ? plugin.getClanLevelBonusSystem() : null;
        if (levelBonusSystem != null) {
            try {
                // Public metod, direkt çağır
                return levelBonusSystem.calculateClanLevel(clan);
            } catch (Exception e) {
                // Fallback: Basit hesaplama
            }
        }
        
        // Fallback: Üye sayısına göre seviye
        int memberCount = clan.getMembers() != null ? clan.getMembers().size() : 0;
        return Math.max(1, memberCount / 5);
    }
    
    private double calculateClanTotalPower(Clan clan) {
        if (clan == null) return 0.0;
        
        StratocraftPowerSystem powerSystem = plugin != null ? plugin.getStratocraftPowerSystem() : null;
        if (powerSystem == null) return 0.0;
        
        try {
            return powerSystem.calculateClanProfile(clan).getTotalClanPower();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private UUID getStrongestMember(Clan clan) {
        if (clan == null || clan.getMembers() == null) return null;
        
        StratocraftPowerSystem powerSystem = plugin != null ? plugin.getStratocraftPowerSystem() : null;
        if (powerSystem == null) return null;
        
        UUID strongest = null;
        double maxPower = 0.0;
        
        for (UUID memberId : clan.getMembers().keySet()) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                try {
                    double power = powerSystem.calculatePlayerProfile(player).getTotalCombatPower();
                    if (power > maxPower) {
                        maxPower = power;
                        strongest = memberId;
                    }
                } catch (Exception e) {
                    // Hata durumunda atla
                }
            }
        }
        
        return strongest;
    }
    
    private int getOnlineMemberCount(Clan clan) {
        if (clan == null || clan.getMembers() == null) return 0;
        
        int count = 0;
        for (UUID memberId : clan.getMembers().keySet()) {
            if (Bukkit.getPlayer(memberId) != null) {
                count++;
            }
        }
        return count;
    }
    
    private Map<Clan.Rank, Integer> getRankDistribution(Clan clan) {
        Map<Clan.Rank, Integer> distribution = new HashMap<>();
        if (clan == null || clan.getMembers() == null) return distribution;
        
        for (Clan.Rank rank : clan.getMembers().values()) {
            distribution.put(rank, distribution.getOrDefault(rank, 0) + 1);
        }
        
        return distribution;
    }
    
    private String getRankName(Clan.Rank rank) {
        if (rank == null) return "Bilinmeyen";
        switch (rank) {
            case LEADER: return "Lider";
            case ELITE: return "Elite";
            case GENERAL: return "General";
            case MEMBER: return "Üye";
            case RECRUIT: return "Acemi";
            default: return "Bilinmeyen";
        }
    }
    
    private String getMissionTypeName(ClanMissionSystem.MissionType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case DEPOSIT_ITEM: return "Item Yatırma";
            case BUILD_STRUCTURE: return "Yapı İnşa";
            case USE_RITUAL: return "Ritüel Kullan";
            case WIN_WAR: return "Savaş Kazan";
            default: return type.name();
        }
    }
    
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Klan İstatistikleri")) return;
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri butonu
                if (plugin != null && plugin.getClanMenu() != null) {
                    plugin.getClanMenu().openMenu(player);
                } else {
                    player.closeInventory();
                }
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}

