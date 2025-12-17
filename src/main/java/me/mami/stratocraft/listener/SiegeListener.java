package me.mami.stratocraft.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.task.SiegeTimer;

public class SiegeListener implements Listener {
    private final SiegeManager siegeManager;
    private final TerritoryManager territoryManager;
    
    // Spam attack önleme: Her klan için son anıt dikme zamanı
    private final Map<UUID, Long> lastSiegeMonumentTime = new HashMap<>();
    private static final long SIEGE_MONUMENT_COOLDOWN = 300000L; // 5 dakika (300 saniye)

    public SiegeListener(SiegeManager sm, TerritoryManager tm) {
        this.siegeManager = sm;
        this.territoryManager = tm;
    }

    @EventHandler
    public void onSiegeAnitPlace(BlockPlaceEvent event) {
        // Kuşatma Anıtı - Beacon
        if (event.getBlock().getType() != Material.BEACON) return;

        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(event.getPlayer())) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }

        Player player = event.getPlayer();
        Clan attacker = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (attacker == null) {
            player.sendMessage("§cSavaş açmak için klan üyesi olmalısın!");
            event.setCancelled(true);
            return;
        }

        // Yetki kontrolü: Sadece General veya Lider
        Clan.Rank rank = attacker.getRank(player.getUniqueId());
        if (rank != Clan.Rank.GENERAL && rank != Clan.Rank.LEADER) {
            player.sendMessage("§cSadece General veya Lider savaş açabilir!");
            event.setCancelled(true);
            return;
        }

        // Aktif üye kontrolü: %35 aktif olmalı
        if (!checkActiveMembers(attacker, 0.35)) {
            player.sendMessage("§cKlanın %35'i aktif olmalı! (En az " + (int)Math.ceil(attacker.getMembers().size() * 0.35) + " üye)");
            event.setCancelled(true);
            return;
        }

        // En az bir general aktif olmalı
        if (!hasActiveGeneral(attacker)) {
            player.sendMessage("§cEn az bir General aktif olmalı!");
            event.setCancelled(true);
            return;
        }

        // ✅ YENİ: Totem yerleştirilen blok
        Block totemBlock = event.getBlock();
        
        // Düşman bölgesine yakın mı? (50 blok)
        Clan defender = territoryManager.getTerritoryOwner(totemBlock.getLocation());
        
        if (defender != null && !defender.equals(attacker)) {
            // Grace Period kontrolü: Yeni kurulan klanlar 24 saat korunur
            if (defender.isInGracePeriod()) {
                long remainingSeconds = defender.getGracePeriodRemaining();
                long hours = remainingSeconds / 3600;
                long minutes = (remainingSeconds % 3600) / 60;
                player.sendMessage("§e" + defender.getName() + " klanı başlangıç koruması altında! " + 
                    hours + " saat " + minutes + " dakika kaldı.");
                event.setCancelled(true);
                return;
            }
            
            // Mesafe kontrolü (50 blok)
            if (defender.getTerritory() != null && defender.getTerritory().getCenter() != null) {
                double distance = totemBlock.getLocation().distance(defender.getTerritory().getCenter());
                if (distance > 50) {
                    player.sendMessage("§cSavaş Totemi düşman bölgesinin 50 blok yakınında olmalı!");
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (!siegeManager.isUnderSiege(defender)) {
                // Spam attack önleme: Aynı saldıran klan 5 dakika içinde tekrar anıt dikemez
                UUID attackerId = attacker.getId();
                Long lastTime = lastSiegeMonumentTime.get(attackerId);
                if (lastTime != null && System.currentTimeMillis() - lastTime < SIEGE_MONUMENT_COOLDOWN) {
                    long remainingSeconds = (SIEGE_MONUMENT_COOLDOWN - (System.currentTimeMillis() - lastTime)) / 1000;
                    player.sendMessage("§cKuşatma Anıtı cooldown'da! Lütfen " + remainingSeconds + " saniye bekleyin.");
                    event.setCancelled(true);
                    return;
                }
                
                // Offline koruma: Eğer savunan klan offline ise ve kalkan yakıtı varsa, yakıt tüket
                Structure core = defender.getStructures().stream()
                    .filter(s -> s.getType() == Structure.Type.CORE)
                    .findFirst().orElse(null);
                
                if (core != null && core.isShieldActive()) {
                    boolean anyOnline = defender.getMembers().keySet().stream()
                        .anyMatch(uuid -> Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline());
                    
                    if (!anyOnline) {
                        // Offline koruma aktif - yakıt tüket (spam attack önleme: maksimum 5 yakıt tüket)
                        int fuelToConsume = Math.min(5, core.getShieldFuel());
                        for (int i = 0; i < fuelToConsume; i++) {
                            core.consumeFuel();
                        }
                        player.sendMessage("§bOffline koruma aktif! " + fuelToConsume + " yakıt tüketildi. Kalan: " + core.getShieldFuel());
                    }
                }
                
                // ✅ YENİ: Zaten savaşta mılar kontrolü
                if (attacker.isAtWarWith(defender.getId())) {
                    player.sendMessage("§eBu klanla zaten savaş halindesiniz!");
                    event.setCancelled(true);
                    return;
                }
                
                siegeManager.startSiege(attacker, defender, player);
                lastSiegeMonumentTime.put(attackerId, System.currentTimeMillis());
                
                new SiegeTimer(defender, me.mami.stratocraft.Main.getInstance())
                    .runTaskTimer(me.mami.stratocraft.Main.getInstance(), 20L, 20L); // 1 saniye = 20 tick
                player.sendMessage("§6Savaş İlan Edildi! Hazırlık süresi başladı.");
            } else {
                player.sendMessage("§eBu klan zaten savaş halinde.");
                event.setCancelled(true);
            }
        } else {
            player.sendMessage("§cSavaş Totemi düşman bölgesinin yakınında olmalı!");
            event.setCancelled(true);
        }
    }
    
    /**
     * ✅ YENİ: Savaş Totemi yapısı kontrolü
     * Yapı: 2 Altın Blok (alt) + 2 Demir Blok (üst)
     * 
     * Yapı:
     *   [IRON_BLOCK] [IRON_BLOCK]  (Y: +1)
     *   [GOLD_BLOCK] [GOLD_BLOCK]  (Y: 0)
     */
    private boolean checkWarTotemStructure(Block center) {
        Material centerType = center.getType();
        
        // Merkez blok Altın veya Demir olmalı
        if (centerType != Material.GOLD_BLOCK && centerType != Material.IRON_BLOCK) {
            return false;
        }
        
        // İki senaryo: Altın blok yerleştirildi (alt) veya Demir blok yerleştirildi (üst)
        Block gold1, gold2, iron1, iron2;
        
        if (centerType == Material.GOLD_BLOCK) {
            // Altın blok yerleştirildi (alt katman)
            gold1 = center;
            gold2 = center.getRelative(org.bukkit.block.BlockFace.EAST);
            iron1 = center.getRelative(org.bukkit.block.BlockFace.UP);
            iron2 = center.getRelative(org.bukkit.block.BlockFace.UP).getRelative(org.bukkit.block.BlockFace.EAST);
        } else {
            // Demir blok yerleştirildi (üst katman)
            iron1 = center;
            iron2 = center.getRelative(org.bukkit.block.BlockFace.EAST);
            gold1 = center.getRelative(org.bukkit.block.BlockFace.DOWN);
            gold2 = center.getRelative(org.bukkit.block.BlockFace.DOWN).getRelative(org.bukkit.block.BlockFace.EAST);
        }
        
        // Kontrol: 2 Altın + 2 Demir
        boolean hasGold1 = gold1 != null && gold1.getType() == Material.GOLD_BLOCK;
        boolean hasGold2 = gold2 != null && gold2.getType() == Material.GOLD_BLOCK;
        boolean hasIron1 = iron1 != null && iron1.getType() == Material.IRON_BLOCK;
        boolean hasIron2 = iron2 != null && iron2.getType() == Material.IRON_BLOCK;
        
        return hasGold1 && hasGold2 && hasIron1 && hasIron2;
    }

    /**
     * ✅ YENİ: Beyaz Bayrak - Pes Etme Sistemi (Çoklu savaş desteği)
     * Beyaz Bayrak = White Banner (Banner)
     * Shift + Sağ tık ile belirli bir klana karşı pes etme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onWhiteFlagSurrender(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.WHITE_BANNER) return;
        if (!event.getPlayer().isSneaking()) return; // Shift + Sağ tık

        Player player = event.getPlayer();
        Clan clan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) return;

        // ✅ YENİ: Savaşta mı? (herhangi bir klanla)
        if (!siegeManager.isUnderSiege(clan)) {
            player.sendMessage("§cKlanınız savaşta değil!");
            return;
        }

        // Yetki kontrolü: Sadece General veya Lider
        Clan.Rank rank = clan.getRank(player.getUniqueId());
        if (rank != Clan.Rank.GENERAL && rank != Clan.Rank.LEADER) {
            player.sendMessage("§cSadece General veya Lider pes edebilir!");
            return;
        }

        // Beyaz bayrak klan bölgesinde mi?
        if (territoryManager.getTerritoryOwner(event.getClickedBlock().getLocation()) != clan) {
            player.sendMessage("§cBeyaz Bayrak klan bölgenizde olmalı!");
            return;
        }

        // ✅ YENİ: Çoklu savaş - İlk savaşta olunan klana pes et
        // (GUI menüsünden belirli bir klana karşı pes etme eklenecek)
        Set<UUID> warringClans = siegeManager.getWarringClans(clan.getId());
        if (warringClans.isEmpty()) {
            player.sendMessage("§cKlanınız savaşta değil!");
            return;
        }
        
        // İlk savaşta olunan klana pes et (GUI'den seçim eklenecek)
        UUID firstWar = warringClans.iterator().next();
        siegeManager.surrender(clan, firstWar, territoryManager.getClanManager());
        player.sendMessage("§f§lBEYAZ BAYRAK ÇEKİLDİ! §eKlanınız pes etti.");
        Bukkit.broadcastMessage("§f§l" + clan.getName() + " klanı pes etti! Sandıkların yarısı kazanan klana gitti.");
    }

    /**
     * Klanın %X'i aktif mi kontrol et
     */
    private boolean checkActiveMembers(Clan clan, double percentage) {
        int totalMembers = clan.getMembers().size();
        if (totalMembers == 0) return false;
        
        int requiredActive = (int) Math.ceil(totalMembers * percentage);
        int activeCount = 0;
        
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                activeCount++;
            }
        }
        
        return activeCount >= requiredActive;
    }

    /**
     * En az bir general aktif mi kontrol et
     */
    private boolean hasActiveGeneral(Clan clan) {
        for (Map.Entry<UUID, Clan.Rank> entry : clan.getMembers().entrySet()) {
            if (entry.getValue() == Clan.Rank.GENERAL || entry.getValue() == Clan.Rank.LEADER) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    return true;
                }
            }
        }
        return false;
    }
}

