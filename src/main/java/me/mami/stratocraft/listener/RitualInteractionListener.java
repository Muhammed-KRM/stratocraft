package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Entity;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RitualInteractionListener - Komutsuz Ritüel Sistemi
 * Oyuncular fiziksel etkileşimlerle klan işlemlerini yapar
 */
public class RitualInteractionListener implements Listener {
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    private me.mami.stratocraft.manager.AllianceManager allianceManager;
    
    // Cooldown sistemi: Oyuncu UUID -> Son ritüel zamanı
    private final Map<UUID, Long> ritualCooldowns = new HashMap<>();
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    
    /**
     * Ritüel cooldown'u al (config'den)
     */
    private long getRitualCooldown() {
        return balanceConfig != null ? balanceConfig.getRitualCooldown() : 10000L;
    }

    public RitualInteractionListener(ClanManager cm, TerritoryManager tm) {
        this.clanManager = cm;
        this.territoryManager = tm;
        // ✅ CONFIG: GameBalanceConfig'i al
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getConfigManager() != null) {
            this.balanceConfig = plugin.getConfigManager().getGameBalanceConfig();
        }
    }
    
    public void setAllianceManager(me.mami.stratocraft.manager.AllianceManager am) {
        this.allianceManager = am;
    }

    // ========== KLAN ÜYE ALMA: "Ateş Ritüeli" (3x3 Soyulmuş Odun) ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onRecruitmentRitual(PlayerInteractEvent event) {
        // Şartlar: Shift + Sağ Tık + Elde Çakmak
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        ItemStack handItem = event.getItem();
        if (handItem == null || handItem.getType() != Material.FLINT_AND_STEEL) return;
        
        Block centerBlock = event.getClickedBlock();
        if (centerBlock == null) return;
        
        // Merkez blok "Soyulmuş Odun" (Stripped Log) olmalı
        if (!isStrippedLog(centerBlock.getType())) return;
        
        Player leader = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        
        // Yetki Kontrolü
        if (clan == null) return;
        UUID leaderId = clan.getLeader();
        if (leaderId == null || (!leaderId.equals(leader.getUniqueId()) && !clan.isGeneral(leader.getUniqueId()))) {
            leader.sendMessage("§cBu ritüeli sadece Lider veya Generaller yapabilir!");
            event.setCancelled(true); // Ateş yakmasını engelle
            return;
        }
        
        // 3x3 Alan Kontrolü
        if (!checkRitualStructure(centerBlock)) {
            // Yapı bozuksa ateş yakar geçer, ritüel tetiklenmez
            return;
        }
        
        event.setCancelled(true); // Normal ateş yakmayı engelle, büyülü ateş yakıcaz
        
        // --- RİTÜEL BAŞARILI ---
        // Karenin içindeki oyuncuları bul (centerBlock'un 1 blok yukarısındaki 3x3 alan)
        Location centerLoc = centerBlock.getLocation().add(0.5, 1, 0.5);
        
        List<Player> recruitedPlayers = new ArrayList<>();
        // 3x3 alan içindeki oyuncuları bul (1.5 blok yarıçap, 2 blok yükseklik)
        for (Entity entity : centerBlock.getWorld().getNearbyEntities(centerLoc, 1.5, 2, 1.5)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                // Kendisi değilse ve klanı yoksa
                if (!target.equals(leader) && clanManager.getClanByPlayer(target.getUniqueId()) == null) {
                    recruitedPlayers.add(target);
                }
            }
        }
        
        if (recruitedPlayers.isEmpty()) {
            leader.sendMessage("§eRitüel alanında klansız kimse yok.");
            return;
        }
        
        // Oyuncuları Klana Ekle
        for (Player newMember : recruitedPlayers) {
            clanManager.addMember(clan, newMember.getUniqueId(), Clan.Rank.RECRUIT);
            newMember.sendMessage("§6§l" + clan.getName() + " §eklanına ruhun bağlandı!");
            newMember.getWorld().spawnParticle(Particle.FLAME, newMember.getLocation(), 50, 0.5, 1, 0.5, 0.1);
            newMember.playSound(newMember.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
            newMember.sendTitle("§a§lKLANA KATILDI", "§e" + clan.getName(), 10, 70, 20);
        }
        
        // Ateş efekti
        centerBlock.getWorld().spawnParticle(Particle.FLAME, centerLoc, 100, 1, 0.5, 1, 0.1);
        centerBlock.getWorld().playSound(centerLoc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        
        leader.sendMessage("§aRitüel tamamlandı! " + recruitedPlayers.size() + " kişi katıldı.");
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Ritüel başarılı oldu
        // ✅ NULL KONTROLÜ: Klan ve başarı kontrolü
        if (clan != null && recruitedPlayers.size() > 0) {
            me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
                java.util.Map<String, Integer> usedResources = new java.util.HashMap<>();
                usedResources.put("FLINT_AND_STEEL", 1); // Çakmak tüketildi
                
                plugin.getStratocraftPowerSystem().onRitualSuccess(
                    clan,
                    "RECRUITMENT_RITUAL",
                    usedResources
                );
            }
            
            // ✅ KLAN GÖREV SİSTEMİ ENTEGRASYONU: Ritüel görevi ilerlemesi
            if (plugin != null && plugin.getClanMissionSystem() != null) {
                try {
                    plugin.getClanMissionSystem().updateMissionProgress(
                        clan, 
                        leader.getUniqueId(), 
                        me.mami.stratocraft.manager.clan.ClanMissionSystem.MissionType.USE_RITUAL, 
                        1
                    );
                } catch (Exception e) {
                    plugin.getLogger().warning("Görev ilerlemesi hatası (Ritüel): " + e.getMessage());
                }
            }
        }
        
        // Çakmağı tüket (dayanıklılık azalt)
        if (handItem.getDurability() >= handItem.getType().getMaxDurability() - 1) {
            handItem.setAmount(0);
        } else {
            handItem.setDurability((short) (handItem.getDurability() + 1));
        }
    }
    
    // 3x3 Kare Kontrolü
    private boolean checkRitualStructure(Block center) {
        // Merkez zaten kontrol edildi. Etrafındaki 8 bloğa bak.
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block rel = center.getRelative(x, 0, z);
                if (!isStrippedLog(rel.getType())) return false;
            }
        }
        return true;
    }
    
    private boolean isStrippedLog(Material mat) {
        return mat.toString().contains("STRIPPED") && mat.toString().contains("LOG");
    }
    
    // ========== KLAN DAVET: "Yemin Çemberi" (ESKİ SİSTEM - KALDIRILABİLİR) ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInviteRitual(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.REDSTONE_TORCH) return;
        
        Player p = event.getPlayer();
        
        // Shift kontrolü
        if (!p.isSneaking()) return;
        
        // Cooldown kontrolü
        if (isOnCooldown(p.getUniqueId())) {
            p.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            event.setCancelled(true);
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        if (clan == null) return;
        
        // Yetki kontrolü
        if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER && 
            clan.getRank(p.getUniqueId()) != Clan.Rank.GENERAL) {
            return; // Yetkisiz
        }
        
        Block torch = event.getBlock();
        
        // 5x5 Taş Tuğla çember kontrolü
        if (!checkInviteCircle(torch)) {
            return; // Çember yapısı yok
        }
        
        // Çemberin içindeki klansız oyuncuları bul
        Location center = torch.getLocation().add(0.5, 1, 0.5);
        int invited = 0;
        
        for (Player nearby : p.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(center) <= 2.5) {
                if (nearby.equals(p)) continue; // Kendisi değil
                
                Clan targetClan = clanManager.getClanByPlayer(nearby.getUniqueId());
                if (targetClan == null) {
                    // Klansız - davet et
                    clanManager.addMember(clan, nearby.getUniqueId(), Clan.Rank.RECRUIT);
                    invited++;
                    
                    // Efektler
                    nearby.getWorld().spawnParticle(Particle.DRAGON_BREATH, nearby.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
                    nearby.playSound(nearby.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
                    nearby.sendTitle("§a§lYEMİN EDİLDİ", "§e" + clan.getName() + " klanına katıldın!", 10, 70, 20);
                    nearby.sendMessage("§a" + clan.getName() + " klanına katıldın!");
                }
            }
        }
        
        if (invited > 0) {
            // Çember etrafında partiküller
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * 2 * Math.PI;
                double x = center.getX() + 2.5 * Math.cos(angle);
                double z = center.getZ() + 2.5 * Math.sin(angle);
                Location partLoc = new Location(center.getWorld(), x, center.getY(), z);
                center.getWorld().spawnParticle(Particle.DRAGON_BREATH, partLoc, 5, 0.1, 0.1, 0.1, 0.05);
            }
            
            p.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            p.sendMessage("§a§lYEMİN EDİLDİ: " + invited + " yeni kardeş katıldı!");
            
            // Cooldown ekle
            setCooldown(p.getUniqueId());
        } else {
            event.setCancelled(true); // Meşaleyi kırma
            p.sendMessage("§cÇemberin içinde klansız oyuncu yok!");
        }
    }

    // ========== KLANDAN ATMA: "Sürgün Ateşi" ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onExileRitual(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        ItemStack dropped = event.getItemDrop().getItemStack();
        
        // Named Paper kontrolü
        if (dropped.getType() != Material.PAPER) return;
        
        ItemMeta meta = dropped.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        if (clan == null) return;
        
        // Yetki kontrolü
        if (clan.getRank(p.getUniqueId()) != Clan.Rank.LEADER) {
            return; // Sadece lider atabilir
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(p.getUniqueId())) {
            p.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            event.setCancelled(true);
            return;
        }
        
        // Eşyanın düştüğü yerde Soul Fire var mı?
        Location dropLoc = event.getItemDrop().getLocation();
        Block fireBlock = dropLoc.getBlock();
        
        if (fireBlock.getType() != Material.SOUL_FIRE) {
            return; // Soul Fire değil
        }
        
        // Kağıttaki ismi al
        String targetName = meta.getDisplayName().replace("§r", "").trim();
        
        // Hedef oyuncuyu bul
        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            p.sendMessage("§cOyuncu bulunamadı veya offline!");
            return;
        }
        
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        if (targetClan == null || !targetClan.equals(clan)) {
            p.sendMessage("§cBu oyuncu senin klanında değil!");
            return;
        }
        
        // Lideri atma
        if (targetClan.getRank(target.getUniqueId()) == Clan.Rank.LEADER) {
            p.sendMessage("§cLideri atamazsın! Önce liderliği devret.");
            return;
        }
        
        // Klandan at
        clanManager.removeMember(targetClan, target.getUniqueId());
        
        // Efektler
        Location fireLoc = fireBlock.getLocation().add(0.5, 0.5, 0.5);
        
        // Patlama efekti
        fireLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLoc, 1);
        fireLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, fireLoc, 20, 0.5, 0.5, 0.5, 0.1);
        
        // Ses
        p.playSound(fireLoc, Sound.ENTITY_GHAST_SCREAM, 1f, 0.8f);
        target.playSound(target.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1f, 0.8f);
        
        // Title
        target.sendTitle("§4§lKLANDAN SÜRGÜN EDİLDİN", "§c" + clan.getName(), 10, 70, 20);
        p.sendMessage("§c" + target.getName() + " klandan sürgün edildi!");
        
        // Eşyayı yok et
        event.getItemDrop().remove();
        
        // Cooldown ekle
        setCooldown(p.getUniqueId());
    }

    // ========== KLANDAN AYRILMA: "Yemin Kağıdı Yakma" ==========
    // Oyuncu elinde isimlendirilmiş bir kağıt ile normal ateşe shift+sağ tık yaparak klandan ayrılabilir
    // Her yerden yapılabilir, sadece ateş gerekir
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeaveRitual(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return; // Shift kontrolü
        
        Player p = event.getPlayer();
        Block clicked = event.getClickedBlock();
        
        // Normal ateş kontrolü (FIRE veya SOUL_FIRE)
        if (clicked.getType() != Material.FIRE && clicked.getType() != Material.SOUL_FIRE) return;
        
        // Elinde isimlendirilmiş kağıt var mı?
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.PAPER) return;
        
        ItemMeta meta = handItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            p.sendMessage("§cRitüel için isimlendirilmiş bir kağıt gerekli! (Örs'te isim yaz)");
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        if (clan == null) {
            p.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // Kağıttaki isim klan ismi veya oyuncu ismi olmalı (doğrulama için)
        String paperName = meta.getDisplayName().replace("§r", "").trim();
        if (!paperName.equalsIgnoreCase(clan.getName()) && !paperName.equalsIgnoreCase(p.getName())) {
            p.sendMessage("§cKağıttaki isim klan ismin veya kendi ismin olmalı!");
            return;
        }
        
        // Lider ayrılamaz
        if (clan.getRank(p.getUniqueId()) == Clan.Rank.LEADER) {
            p.sendMessage("§cLider klandan ayrılamaz! Önce liderliği devret.");
            event.setCancelled(true);
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(p.getUniqueId())) {
            p.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            event.setCancelled(true);
            return;
        }
        
        // Ateş yakmayı engelle, ritüel yapacağız
        event.setCancelled(true);
        
        // Klandan ayrıl
        clanManager.removeMember(clan, p.getUniqueId());
        
        // Kağıdı tüket
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            p.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location fireLoc = clicked.getLocation().add(0.5, 0.5, 0.5);
        
        // Ateş partikülleri
        fireLoc.getWorld().spawnParticle(Particle.FLAME, fireLoc, 50, 0.3, 0.3, 0.3, 0.1);
        fireLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, fireLoc, 30, 0.5, 0.5, 0.5, 0.05);
        
        // Oyuncu etrafında efektler
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.CRIT, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.2);
        
        // Sesler
        p.playSound(fireLoc, Sound.ITEM_FIRECHARGE_USE, 1f, 0.8f);
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        
        // Title
        p.sendTitle("§e§lYEMİN KIRILDI", "§7" + clan.getName() + " klanından ayrıldın", 10, 70, 20);
        p.sendMessage("§e§l" + clan.getName() + " §7klanından ayrıldın!");
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Ritüel başarılı oldu (ayrılma ritüeli)
        // ✅ NULL KONTROLÜ: Klan kontrolü (ayrıldıktan sonra clan null olabilir, önce kaydet)
        me.mami.stratocraft.model.Clan ritualClan = clan; // Ayrılmadan önce kaydet
        if (ritualClan != null) {
            me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
            if (plugin != null && plugin.getStratocraftPowerSystem() != null) {
                java.util.Map<String, Integer> usedResources = new java.util.HashMap<>();
                usedResources.put("PAPER", 1); // Kağıt tüketildi
                
                plugin.getStratocraftPowerSystem().onRitualSuccess(
                    ritualClan,
                    "LEAVE_RITUAL",
                    usedResources
                );
            }
            
            // ✅ KLAN GÖREV SİSTEMİ ENTEGRASYONU: Ritüel görevi ilerlemesi (ayrılma ritüeli)
            // Not: Ayrılma ritüeli de bir ritüel olduğu için görev ilerlemesi eklenebilir
            if (plugin != null && plugin.getClanMissionSystem() != null) {
                try {
                    plugin.getClanMissionSystem().updateMissionProgress(
                        ritualClan, 
                        p.getUniqueId(), 
                        me.mami.stratocraft.manager.clan.ClanMissionSystem.MissionType.USE_RITUAL, 
                        1
                    );
                } catch (Exception e) {
                    plugin.getLogger().warning("Görev ilerlemesi hatası (Ayrılma Ritüeli): " + e.getMessage());
                }
            }
        }
        
        // Klan üyelerine bildir (isteğe bağlı)
        String playerName = p.getName();
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§7" + playerName + " klanından ayrıldı.");
            }
        }
        
        // Cooldown ekle
        setCooldown(p.getUniqueId());
    }

    // ========== TERFİ RİTÜELİ (Eski RitualListener'dan) ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPromotionRitual(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Block b = event.getClickedBlock();
        if (b == null) return;
        Player leader = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());

        if (clan == null || clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) return;
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // --- TERFİ RİTÜELİ KONTROLÜ ---
        // Kurulum: 3x3 Taş Tuğla, Köşelerde Kızıltaş Meşalesi, Ortada Ateş
        if (b.getType() == Material.FIRE && b.getRelative(BlockFace.DOWN).getType() == Material.STONE_BRICKS) {
            
            // Köşelerde Kızıltaş Meşalesi kontrolü
            boolean hasRedstoneTorches = 
                b.getRelative(BlockFace.NORTH_WEST).getType() == Material.REDSTONE_TORCH &&
                b.getRelative(BlockFace.NORTH_EAST).getType() == Material.REDSTONE_TORCH &&
                b.getRelative(BlockFace.SOUTH_WEST).getType() == Material.REDSTONE_TORCH &&
                b.getRelative(BlockFace.SOUTH_EAST).getType() == Material.REDSTONE_TORCH;
            
            if (!hasRedstoneTorches) {
                leader.sendMessage("§cRitüel için köşelerde 4 Kızıltaş Meşalesi gerekli!");
                return;
            }
            
            // Ateş yakmayı engelle
            event.setCancelled(true);
            
            // Altın Külçe ile General terfisi
            ItemStack handItem = leader.getInventory().getItemInMainHand();
            if (handItem != null && handItem.getType() == Material.GOLD_INGOT) {
                
                leader.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof Player && e != leader)
                    .map(e -> (Player)e)
                    .findFirst()
                    .ifPresent(target -> {
                        // YENİ: Klan üyeliği kontrolü
                        if (!clan.getMembers().containsKey(target.getUniqueId())) {
                            leader.sendMessage("§cBu oyuncu klanınızın üyesi değil!");
                            return;
                        }
                        
                        if (clan.getRank(target.getUniqueId()) == Clan.Rank.MEMBER) {
                            clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.GENERAL);
                            
                            // Efektler
                            Location loc = target.getLocation();
                            loc.getWorld().spawnParticle(Particle.TOTEM, loc, 30, 0.5, 1, 0.5, 0.3);
                            leader.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                            
                            leader.sendMessage("§a" + target.getName() + " General rütbesine yükseltildi!");
                            target.sendTitle("§6§lTERFİ EDİLDİN", "§eGeneral rütbesine yükseltildin!", 10, 70, 20);
                            if (handItem.getAmount() > 1) {
                                handItem.setAmount(handItem.getAmount() - 1);
                            } else {
                                leader.getInventory().setItemInMainHand(null);
                            }
                            
                            setCooldown(leader.getUniqueId());
                        } else {
                            leader.sendMessage("§eBu kişi zaten General veya daha üst rütbede.");
                        }
                    });
            } else if (handItem != null && handItem.getType() == Material.IRON_INGOT) {
                // Üye terfisi: Recruit -> Member
                leader.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof Player && e != leader)
                    .map(e -> (Player)e)
                    .findFirst()
                    .ifPresent(target -> {
                        // YENİ: Klan üyeliği kontrolü
                        if (!clan.getMembers().containsKey(target.getUniqueId())) {
                            leader.sendMessage("§cBu oyuncu klanınızın üyesi değil!");
                            return;
                        }
                        
                        if (clan.getRank(target.getUniqueId()) == Clan.Rank.RECRUIT) {
                            clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.MEMBER);
                            
                            // Efektler
                            Location loc = target.getLocation();
                            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 30, 0.5, 1, 0.5, 0.3);
                            leader.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            
                            leader.sendMessage("§a" + target.getName() + " Üye rütbesine yükseltildi!");
                            target.sendTitle("§a§lTERFİ EDİLDİN", "§eÜye rütbesine yükseltildin!", 10, 70, 20);
                            if (handItem.getAmount() > 1) {
                                handItem.setAmount(handItem.getAmount() - 1);
                            } else {
                                leader.getInventory().setItemInMainHand(null);
                            }
                            
                            setCooldown(leader.getUniqueId());
                        } else {
                            leader.sendMessage("§eBu kişi zaten Üye veya daha üst rütbede.");
                        }
                    });
            }
        }
    }

    // ========== İTTİFAK: "Kan Anlaşması Ritüeli" (YENİ SİSTEM) ==========
    // İki lider elinde Elmas ile birbirine shift+sağ tık yapar
    // İttifak tipi: Elinde farklı itemlar ile farklı ittifaklar yapılabilir
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onAllianceRitual(PlayerInteractEntityEvent event) {
        if (allianceManager == null) return; // AllianceManager yoksa çalışma
        
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Player)) return;

        Player p1 = event.getPlayer();
        Player p2 = (Player) event.getRightClicked();

        // Her iki oyuncu da shift'te olmalı
        if (!p1.isSneaking() || !p2.isSneaking()) return;
        
        // Elinde Elmas var mı?
        ItemStack handItem = p1.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.DIAMOND) return;
        
        // p2 de Shift'e basıyor mu ve elinde Elmas var mı?
        if (!p2.isSneaking()) return;
        ItemStack p2Hand = p2.getInventory().getItemInMainHand();
        if (p2Hand == null || p2Hand.getType() != Material.DIAMOND) return;
        
        // İkisi de birbirine bakıyor mu? (basit kontrol)
        if (p1.getLocation().distance(p2.getLocation()) > 3) return;
        
        Clan clan1 = clanManager.getClanByPlayer(p1.getUniqueId());
        Clan clan2 = clanManager.getClanByPlayer(p2.getUniqueId());
        
        if (clan1 == null || clan2 == null) return;
        if (clan1.equals(clan2)) return; // Aynı klan
        
        // İkisi de lider mi?
        if (clan1.getRank(p1.getUniqueId()) != Clan.Rank.LEADER ||
            clan2.getRank(p2.getUniqueId()) != Clan.Rank.LEADER) {
            return; // Sadece liderler ittifak yapabilir
        }
        
        // Cooldown kontrolü (AllianceManager'dan)
        if (allianceManager.isOnCooldown(clan1.getId()) || allianceManager.isOnCooldown(clan2.getId())) {
            p1.sendMessage("§cİttifak ritüeli henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Zaten ittifak var mı?
        if (allianceManager.hasAlliance(clan1.getId(), clan2.getId())) {
            p1.sendMessage("§eBu klanlar zaten ittifak halinde!");
            return;
        }
        
        // İttifak tipi belirleme: Elinde farklı itemlar ile farklı ittifaklar
        // Şimdilik FULL ittifak (elinde başka item yoksa)
        me.mami.stratocraft.model.Alliance.Type allianceType = me.mami.stratocraft.model.Alliance.Type.FULL;
        
        // İttifak oluştur (süresiz - 0 = süresiz)
        me.mami.stratocraft.model.Alliance alliance = allianceManager.createAlliance(
            clan1.getId(), clan2.getId(), allianceType, 0);
        
        if (alliance == null) {
            p1.sendMessage("§cİttifak oluşturulamadı!");
            return;
        }
        
        // Elmasları tüket
        handItem.setAmount(handItem.getAmount() - 1);
        p2Hand.setAmount(p2Hand.getAmount() - 1);
        
        // Efektler
        Location midLoc = p1.getLocation().add(p2.getLocation()).multiply(0.5);
        midLoc.getWorld().spawnParticle(Particle.HEART, midLoc, 20, 1, 1, 1, 0.1);
        midLoc.getWorld().spawnParticle(Particle.END_ROD, midLoc, 30, 1, 1, 1, 0.2);
        midLoc.getWorld().spawnParticle(Particle.TOTEM, midLoc, 50, 1, 1, 1, 0.3);
        
        p1.playSound(midLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        p2.playSound(midLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        
        p1.sendTitle("§a§lİTTİFAK KURULDU", "§e" + clan2.getName(), 10, 70, 20);
        p2.sendTitle("§a§lİTTİFAK KURULDU", "§e" + clan1.getName(), 10, 70, 20);
        
        Bukkit.broadcastMessage("§a§l" + clan1.getName() + " §7ve §a" + clan2.getName() + 
            " §7klanları ittifak kurdu! (Tip: " + allianceType.name() + ")");
        
        // Cooldown ekle
        allianceManager.setCooldown(clan1.getId());
        allianceManager.setCooldown(clan2.getId());
    }

    // ========== LİDERLİK DEVRİ: "Taç Geçişi" (ZOR) ==========
    // Lider elinde Altın Taç (Golden Helmet) ile Klan Kristali yakınında hedef oyuncuya shift+sağ tık yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeadershipTransfer(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player leader = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        
        if (!leader.isSneaking()) return;
        
        // Elinde Altın Taç var mı?
        ItemStack handItem = leader.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.GOLDEN_HELMET) return;
        
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        if (clan == null) return;
        
        // Lider kontrolü
        if (clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
            leader.sendMessage("§cBu ritüeli sadece klan lideri yapabilir!");
            return;
        }
        
        // Hedef oyuncu aynı klanda mı?
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        if (targetClan == null || !targetClan.equals(clan)) {
            leader.sendMessage("§cHedef oyuncu senin klanında değil!");
            return;
        }
        
        // Klan Kristali yakınında mı? (10 blok mesafe)
        if (clan.getCrystalLocation() == null) {
            leader.sendMessage("§cKlan Kristali bulunamadı!");
            return;
        }
        
        double distanceToCrystal = leader.getLocation().distance(clan.getCrystalLocation());
        if (distanceToCrystal > 10) {
            leader.sendMessage("§cLiderlik devri için Klan Kristali yakınında olmalısın! (10 blok)");
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Liderliği devret
        clan.getMembers().put(leader.getUniqueId(), Clan.Rank.GENERAL);
        clan.getMembers().put(target.getUniqueId(), Clan.Rank.LEADER);
        
        // playerClanMap'i güncelle (liderlik değiştiği için gerekli değil ama tutarlılık için)
        // Not: playerClanMap zaten doğru klan ID'sini gösteriyor, sadece rütbe değişti
        
        // Altın Tacı tüket
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            leader.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location crystalLoc = clan.getCrystalLocation();
        crystalLoc.getWorld().spawnParticle(Particle.TOTEM, crystalLoc, 100, 1, 1, 1, 0.5);
        crystalLoc.getWorld().spawnParticle(Particle.END_ROD, crystalLoc, 50, 1, 1, 1, 0.3);
        leader.getWorld().strikeLightningEffect(crystalLoc);
        
        leader.playSound(crystalLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        target.playSound(crystalLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        
        leader.sendTitle("§6§lLİDERLİK DEVREDİLDİ", "§e" + target.getName(), 10, 70, 20);
        target.sendTitle("§6§lYENİ LİDER", "§e" + clan.getName(), 10, 70, 20);
        
        // Klan üyelerine bildir
        String leaderName = leader.getName();
        String targetName = target.getName();
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§6§l" + leaderName + " liderliği " + targetName + " devretti!");
            }
        }
        
        setCooldown(leader.getUniqueId());
    }

    // ========== KLAN İSMİ DEĞİŞTİRME: "Yeniden Adlandırma" (ORTA) ==========
    // Lider elinde yeni isimli kağıt ile Klan Kristali yakınında shift+sağ tık yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClanRename(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;
        
        Player leader = event.getPlayer();
        Block clicked = event.getClickedBlock();
        
        // Klan Kristali yakınında mı? (Kristal entity'sine tıklama veya yakınında)
        if (clicked == null) return;
        
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        if (clan == null) return;
        
        if (clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
            return; // Sadece lider
        }
        
        // Klan Kristali yakınında mı? (5 blok mesafe)
        if (clan.getCrystalLocation() == null) return;
        if (leader.getLocation().distance(clan.getCrystalLocation()) > 5) {
            leader.sendMessage("§cKlan ismini değiştirmek için Klan Kristali yakınında olmalısın!");
            return;
        }
        
        // Elinde isimlendirilmiş kağıt var mı?
        ItemStack handItem = leader.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.PAPER) return;
        
        ItemMeta meta = handItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            leader.sendMessage("§cRitüel için isimlendirilmiş bir kağıt gerekli! (Örs'te yeni isim yaz)");
            return;
        }
        
        String newName = meta.getDisplayName().replace("§r", "").trim();
        if (newName.isEmpty() || newName.equals(clan.getName())) {
            leader.sendMessage("§cYeni isim eski isimden farklı olmalı!");
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        event.setCancelled(true); // Blok etkileşimini engelle
        
        // Klan ismini değiştir
        String oldName = clan.getName();
        clan.setName(newName);
        
        // Klan üyelerine bildir
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§eKlan ismi değiştirildi: §7" + oldName + " §e→ §a" + newName);
            }
        }
        
        leader.sendMessage("§aKlan ismi başarıyla değiştirildi: §7" + oldName + " §a→ §e" + newName);
        
        // Kağıdı tüket
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            leader.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location crystalLoc = clan.getCrystalLocation();
        crystalLoc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, crystalLoc, 50, 1, 1, 1, 0.3);
        leader.playSound(crystalLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
        
        setCooldown(leader.getUniqueId());
    }

    // ========== MÜTTEFİKLİK İPTALİ: "Anlaşma Kırma" (KOLAY) ==========
    // Lider elinde Kırmızı Çiçek (Rose/Red Tulip) ile müttefik liderine shift+sağ tık yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onAllianceBreak(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player p1 = event.getPlayer();
        Player p2 = (Player) event.getRightClicked();
        
        if (!p1.isSneaking()) return;
        
        // Elinde Kırmızı Çiçek var mı?
        ItemStack handItem = p1.getInventory().getItemInMainHand();
        if (handItem == null || (handItem.getType() != Material.RED_TULIP && 
            handItem.getType() != Material.ROSE_BUSH && handItem.getType() != Material.POPPY)) return;
        
        Clan clan1 = clanManager.getClanByPlayer(p1.getUniqueId());
        Clan clan2 = clanManager.getClanByPlayer(p2.getUniqueId());
        
        if (clan1 == null || clan2 == null) return;
        if (clan1.equals(clan2)) return; // Aynı klan
        
        // İkisi de lider mi?
        if (clan1.getRank(p1.getUniqueId()) != Clan.Rank.LEADER ||
            clan2.getRank(p2.getUniqueId()) != Clan.Rank.LEADER) {
            return; // Sadece liderler iptal edebilir
        }
        
        // Müttefik mi?
        if (!clan1.isGuest(p2.getUniqueId()) || !clan2.isGuest(p1.getUniqueId())) {
            p1.sendMessage("§cBu klanlar müttefik değil!");
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(p1.getUniqueId())) {
            p1.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Müttefikliği iptal et
        clan1.getGuests().remove(p2.getUniqueId());
        clan2.getGuests().remove(p1.getUniqueId());
        
        // Çiçeği tüket
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            p1.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location midLoc = p1.getLocation().add(p2.getLocation()).multiply(0.5);
        midLoc.getWorld().spawnParticle(Particle.SMOKE_LARGE, midLoc, 30, 1, 1, 1, 0.1);
        midLoc.getWorld().spawnParticle(Particle.CRIT, midLoc, 20, 1, 1, 1, 0.2);
        
        p1.playSound(midLoc, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        p2.playSound(midLoc, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        
        p1.sendTitle("§c§lMÜTTEFİKLİK İPTAL", "§7" + clan2.getName(), 10, 70, 20);
        p2.sendTitle("§c§lMÜTTEFİKLİK İPTAL", "§7" + clan1.getName(), 10, 70, 20);
        
        Bukkit.broadcastMessage("§c§l" + clan1.getName() + " §7ve §c" + clan2.getName() + " §7klanları arasındaki müttefiklik sona erdi!");
        
        setCooldown(p1.getUniqueId());
    }

    // ========== GUEST EKLEME: "Misafir Daveti" (KOLAY) ==========
    // Lider elinde Yeşil Çiçek (Green Dye veya Cactus) ile oyuncuya shift+sağ tık yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onGuestAdd(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player leader = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        
        if (!leader.isSneaking()) return;
        
        // Elinde Yeşil Çiçek var mı? (Cactus veya Green Dye)
        ItemStack handItem = leader.getInventory().getItemInMainHand();
        if (handItem == null || (handItem.getType() != Material.CACTUS && 
            handItem.getType() != Material.GREEN_DYE)) return;
        
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        if (clan == null) return;
        
        // Lider kontrolü
        if (clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
            leader.sendMessage("§cBu ritüeli sadece klan lideri yapabilir!");
            return;
        }
        
        // Hedef oyuncu başka bir klanda mı?
        Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
        if (targetClan != null && !targetClan.equals(clan)) {
            leader.sendMessage("§cBu oyuncu başka bir klana üye!");
            return;
        }
        
        // Zaten guest mi?
        if (clan.isGuest(target.getUniqueId())) {
            leader.sendMessage("§eBu oyuncu zaten misafir!");
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Guest ekle
        clan.addGuest(target.getUniqueId());
        
        // Çiçeği tüket
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            leader.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location targetLoc = target.getLocation();
        targetLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, targetLoc, 30, 0.5, 1, 0.5, 0.1);
        leader.playSound(targetLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        
        leader.sendMessage("§a" + target.getName() + " misafir olarak eklendi!");
        target.sendMessage("§a" + clan.getName() + " klanına misafir olarak eklendin!");
        target.sendTitle("§a§lMİSAFİR", "§e" + clan.getName(), 10, 70, 20);
        
        setCooldown(leader.getUniqueId());
    }

    // ========== GUEST ÇIKARMA: "Misafir Kovma" (KOLAY) ==========
    // Lider elinde Kırmızı Çiçek ile guest oyuncuya shift+sağ tık yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onGuestRemove(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player leader = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        
        if (!leader.isSneaking()) return;
        
        // Elinde Kırmızı Çiçek var mı?
        ItemStack handItem = leader.getInventory().getItemInMainHand();
        if (handItem == null || (handItem.getType() != Material.RED_TULIP && 
            handItem.getType() != Material.ROSE_BUSH && handItem.getType() != Material.POPPY)) return;
        
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        if (clan == null) return;
        
        // Lider kontrolü
        if (clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) {
            return; // Sadece lider
        }
        
        // Guest mi?
        if (!clan.isGuest(target.getUniqueId())) {
            return; // Guest değil
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Guest çıkar
        clan.getGuests().remove(target.getUniqueId());
        
        // Çiçeği tüket
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            leader.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location targetLoc = target.getLocation();
        targetLoc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, targetLoc, 20, 0.5, 1, 0.5, 0.1);
        leader.playSound(targetLoc, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        
        leader.sendMessage("§c" + target.getName() + " misafir listesinden çıkarıldı!");
        target.sendMessage("§c" + clan.getName() + " klanından misafirliğin sona erdi!");
        
        setCooldown(leader.getUniqueId());
    }

    // ========== RÜTBE DÜŞÜRME: "Geri Alma" (ORTA) ==========
    // Lider elinde Kömür ile terfi ritüeli yapısında hedef oyuncuya shift+sağ tık yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onDemotionRitual(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;
        
        Block b = event.getClickedBlock();
        if (b == null) return;
        Player leader = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        
        if (clan == null || clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) return;
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Terfi ritüeli yapısı kontrolü (3x3 Taş Tuğla, Köşelerde Kızıltaş Meşalesi, Ortada Ateş)
        if (b.getType() == Material.FIRE && b.getRelative(BlockFace.DOWN).getType() == Material.STONE_BRICKS) {
            
            // Köşelerde Kızıltaş Meşalesi kontrolü
            boolean hasRedstoneTorches = 
                b.getRelative(BlockFace.NORTH_WEST).getType() == Material.REDSTONE_TORCH &&
                b.getRelative(BlockFace.NORTH_EAST).getType() == Material.REDSTONE_TORCH &&
                b.getRelative(BlockFace.SOUTH_WEST).getType() == Material.REDSTONE_TORCH &&
                b.getRelative(BlockFace.SOUTH_EAST).getType() == Material.REDSTONE_TORCH;
            
            if (!hasRedstoneTorches) {
                return; // Yapı yok
            }
            
            // Elinde Kömür var mı?
            ItemStack handItem = leader.getInventory().getItemInMainHand();
            if (handItem != null && (handItem.getType() == Material.COAL || 
                handItem.getType() == Material.CHARCOAL)) {
                
                leader.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof Player && e != leader)
                    .map(e -> (Player)e)
                    .findFirst()
                    .ifPresent(target -> {
                        Clan.Rank currentRank = clan.getRank(target.getUniqueId());
                        if (currentRank == null) return;
                        
                        // Rütbe düşürme
                        Clan.Rank newRank = null;
                        if (currentRank == Clan.Rank.GENERAL) {
                            newRank = Clan.Rank.MEMBER;
                        } else if (currentRank == Clan.Rank.MEMBER) {
                            newRank = Clan.Rank.RECRUIT;
                        } else {
                            leader.sendMessage("§eBu kişi zaten en düşük rütbede!");
                            return;
                        }
                        
                        clanManager.addMember(clan, target.getUniqueId(), newRank);
                        
                        // Ateş yakmayı engelle
                        event.setCancelled(true);
                        
                        // Efektler
                        Location loc = target.getLocation();
                        loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 30, 0.5, 1, 0.5, 0.3);
                        leader.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                        
                        leader.sendMessage("§c" + target.getName() + " " + newRank.name() + " rütbesine düşürüldü!");
                        target.sendTitle("§c§lRÜTBE DÜŞÜRÜLDÜ", "§7" + newRank.name(), 10, 70, 20);
                        // YENİ: Null kontrolü
                        ItemStack handItem = leader.getInventory().getItemInMainHand();
                        if (handItem != null && handItem.getAmount() > 1) {
                            handItem.setAmount(handItem.getAmount() - 1);
                        } else if (handItem != null) {
                            leader.getInventory().setItemInMainHand(null);
                        }
                        
                        setCooldown(leader.getUniqueId());
                    });
            }
        }
    }

    // ========== KLAN BANKASI: "Hazine Kutusu" (ORTA) ==========
    // Lider/General elinde Altın ile Chest'e shift+sağ tık yaparak para yatırır
    // Lider/General elinde Boş el ile Chest'e shift+sağ tık yaparak para çeker
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClanBankAccess(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;
        
        Player p = event.getPlayer();
        Block clicked = event.getClickedBlock();
        
        // Chest kontrolü
        if (clicked == null || (clicked.getType() != Material.CHEST && 
            clicked.getType() != Material.TRAPPED_CHEST)) return;
        
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        if (clan == null) {
            p.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // Yetki kontrolü (Lider veya General)
        Clan.Rank rank = clan.getRank(p.getUniqueId());
        if (rank != Clan.Rank.LEADER && rank != Clan.Rank.GENERAL) {
            p.sendMessage("§cBu işlem için Lider veya General rütbesi gerekli!");
            return;
        }
        
        event.setCancelled(true); // Chest açılmasını engelle
        
        ItemStack handItem = p.getInventory().getItemInMainHand();
        
        // Para yatırma (Altın ile)
        if (handItem != null && handItem.getType() == Material.GOLD_INGOT) {
            int amount = handItem.getAmount();
            
            if (amount <= 0) return;
            
            // Para yatırma
            clan.deposit(amount);
            handItem.setAmount(0);
            
            // Efektler
            Location chestLoc = clicked.getLocation().add(0.5, 0.5, 0.5);
            chestLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, chestLoc, 20, 0.3, 0.3, 0.3, 0.1);
            p.playSound(chestLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            
            p.sendMessage("§a" + amount + " altın klan kasasına yatırıldı!");
            p.sendMessage("§eKlan Bakiyesi: §6" + clan.getBalance() + " altın");
        }
        // Para çekme (Boş el ile - maksimum 64 altın)
        else if (handItem == null || handItem.getType() == Material.AIR) {
            if (clan.getBalance() <= 0) {
                p.sendMessage("§cKlan kasasında para yok!");
                return;
            }
            
            // Maksimum 64 altın çek
            int withdrawAmount = (int) Math.min(64, clan.getBalance());
            
            // Para çekme
            clan.withdraw(withdrawAmount);
            
            // Altın ver
            ItemStack gold = new ItemStack(Material.GOLD_INGOT, withdrawAmount);
            if (p.getInventory().firstEmpty() == -1) {
                // Envanter dolu, yere bırak
                p.getWorld().dropItemNaturally(p.getLocation(), gold);
                p.sendMessage("§eEnvanterin dolu! Altınlar yere düştü.");
            } else {
                p.getInventory().addItem(gold);
            }
            
            // Efektler
            Location chestLoc = clicked.getLocation().add(0.5, 0.5, 0.5);
            chestLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, chestLoc, 20, 0.3, 0.3, 0.3, 0.1);
            p.playSound(chestLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            
            p.sendMessage("§a" + withdrawAmount + " altın klan kasasından çekildi!");
            p.sendMessage("§eKalan Bakiye: §6" + clan.getBalance() + " altın");
        }
    }
    
    // ========== KLAN İSTATİSTİKLERİ: "Bilgi Taşı" (KOLAY) ==========
    /**
     * ✅ DÜZELTME: Pusula ile sol tık ışınlanmayı engelle
     * Sadece özel item'larda (PERSONAL_TERMINAL gibi) özel özellikler çalışmalı
     * Normal pusulalarda Minecraft'ın lodestone sistemi çalışmamalı
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassTeleportPrevent(PlayerInteractEvent event) {
        // Sol tık kontrolü
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player p = event.getPlayer();
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.COMPASS) return;
        
        // ✅ Özel item kontrolü - sadece özel item'larda özel özellikler çalışmalı
        // Personal Terminal → Menü açma (başka listener'da işlenecek)
        if (me.mami.stratocraft.manager.ItemManager.isCustomItem(handItem, "PERSONAL_TERMINAL")) {
            return; // Personal Terminal başka listener'da işlenecek
        }
        
        // ✅ Normal pusula → Işınlanmayı engelle (Minecraft'ın lodestone sistemi)
        // Eğer başka özel bir item varsa (örneğin TELEPORT_COMPASS), burada kontrol edilebilir
        // Şimdilik sadece PERSONAL_TERMINAL özel item olarak kabul ediliyor
        event.setCancelled(true);
    }
    
    /**
     * ✅ DÜZELTME: Pusula ile sağ tık ışınlanmayı engelle
     * Sadece özel item'larda (PERSONAL_TERMINAL gibi) özel özellikler çalışmalı
     * Normal pusulalarda Minecraft'ın lodestone sistemi çalışmamalı
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassRightClickPrevent(PlayerInteractEvent event) {
        // Sağ tık kontrolü
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player p = event.getPlayer();
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.COMPASS) return;
        
        // ✅ Özel item kontrolü - sadece özel item'larda özel özellikler çalışmalı
        // Personal Terminal → Menü açma (başka listener'da işlenecek)
        if (me.mami.stratocraft.manager.ItemManager.isCustomItem(handItem, "PERSONAL_TERMINAL")) {
            return; // Personal Terminal başka listener'da işlenecek
        }
        
        // Shift + Sağ tık ise klan bilgisi göster (onClanStatsView'da işlenecek)
        if (p.isSneaking()) {
            return; // onClanStatsView'da işlenecek
        }
        
        // ✅ Normal pusula → Işınlanmayı engelle (Minecraft'ın lodestone sistemi)
        // Eğer başka özel bir item varsa (örneğin TELEPORT_COMPASS), burada kontrol edilebilir
        // Şimdilik sadece PERSONAL_TERMINAL özel item olarak kabul ediliyor
        event.setCancelled(true);
    }
    
    // Oyuncu elinde Kompas ile herhangi bir yerde shift+sağ tık yapar (Kristal yakınında olmasına gerek yok)
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClanStatsView(PlayerInteractEvent event) {
        // ✅ DÜZELTME: Hem RIGHT_CLICK_BLOCK hem RIGHT_CLICK_AIR kontrol et
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;
        
        Player p = event.getPlayer();
        
        // Elinde Kompas var mı?
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.COMPASS) return;
        
        // Personal Terminal kontrolü - eğer Personal Terminal ise bu listener'ı atla
        if (me.mami.stratocraft.manager.ItemManager.isCustomItem(handItem, "PERSONAL_TERMINAL")) {
            return; // Personal Terminal başka listener'da işlenecek
        }
        
        // ✅ DÜZELTME: Normal pusula ile ışınlanmayı engelle (sadece özel item'lere güç ver)
        // Eğer özel bir item değilse, sadece bilgi göster (ışınlanma yok)
        // Minecraft'ın lodestone pusula sistemi otomatik çalışıyor, bunu engellemek için event'i iptal et
        
        // ✅ Özel item kontrolü - sadece özel item'larda özel özellikler çalışmalı
        // Personal Terminal → Menü açma (başka listener'da işlenecek)
        if (me.mami.stratocraft.manager.ItemManager.isCustomItem(handItem, "PERSONAL_TERMINAL")) {
            return; // Personal Terminal başka listener'da işlenecek
        }
        
        // Normal pusula → Işınlanmayı engelle
        event.setCancelled(true);
        
        // Oyuncunun klanını bul (kendi klanı veya yakındaki bir klan)
        Clan targetClan = clanManager.getClanByPlayer(p.getUniqueId());
        
        // Eğer klanı yoksa, yakındaki klan kristalini bul
        if (targetClan == null) {
            double minDistance = Double.MAX_VALUE;
            for (Clan clan : clanManager.getAllClans()) {
                if (clan.getCrystalLocation() != null) {
                    double distance = p.getLocation().distance(clan.getCrystalLocation());
                    if (distance <= 20 && distance < minDistance) {
                        minDistance = distance;
                        targetClan = clan;
                    }
                }
            }
        }
        
        if (targetClan == null) {
            p.sendMessage("§cYakında bir Klan Kristali yok veya bir klana üye değilsin!");
            return;
        }
        
        event.setCancelled(true); // Blok etkileşimini engelle
        
        // Klan bilgilerini göster
        p.sendMessage("§6=== " + targetClan.getName() + " Klanı ===");
        p.sendMessage("§7Üye Sayısı: §e" + targetClan.getMembers().size());
        p.sendMessage("§7Bakiye: §6" + targetClan.getBalance() + " altın");
        p.sendMessage("§7Teknoloji Seviyesi: §b" + targetClan.getTechLevel());
        p.sendMessage("§7XP Bankası: §a" + targetClan.getStoredXP() + " XP");
        
        // Lider bilgisi
        UUID leaderId = targetClan.getLeader();
        if (leaderId != null) {
            Player leader = Bukkit.getPlayer(leaderId);
            if (leader != null && leader.isOnline()) {
                p.sendMessage("§7Lider: §e" + leader.getName() + " §7(Online)");
            } else {
                p.sendMessage("§7Lider: §7" + Bukkit.getOfflinePlayer(leaderId).getName() + " §7(Offline)");
            }
        }
        
        // Üye listesi (ilk 5)
        p.sendMessage("§7Üyeler:");
        int count = 0;
        for (Map.Entry<UUID, Clan.Rank> entry : targetClan.getMembers().entrySet()) {
            if (count >= 5) {
                p.sendMessage("§7... ve " + (targetClan.getMembers().size() - 5) + " kişi daha");
                break;
            }
            Player member = Bukkit.getPlayer(entry.getKey());
            String memberName = member != null ? member.getName() : Bukkit.getOfflinePlayer(entry.getKey()).getName();
            String rankName = entry.getValue().name();
            p.sendMessage("§7  - §e" + memberName + " §7(" + rankName + ")");
            count++;
        }
        
        // Efektler
        Location crystalLoc = targetClan.getCrystalLocation();
        if (crystalLoc != null) {
            crystalLoc.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, crystalLoc, 30, 0.5, 0.5, 0.5, 0.1);
            p.playSound(crystalLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        }
    }

    // ========== YARDIMCI METODLAR ==========
    
    private boolean checkInviteCircle(Block torch) {
        // 5x5 Taş Tuğla çember kontrolü (torch'un etrafında)
        // 4 köşede Kızıltaş Meşalesi olmalı
        
        Block base = torch.getRelative(BlockFace.DOWN);
        
        // Köşeler kontrolü
        Block nw = base.getRelative(-2, 0, -2);
        Block ne = base.getRelative(2, 0, -2);
        Block sw = base.getRelative(-2, 0, 2);
        Block se = base.getRelative(2, 0, 2);
        
        if (nw.getType() != Material.REDSTONE_TORCH ||
            ne.getType() != Material.REDSTONE_TORCH ||
            sw.getType() != Material.REDSTONE_TORCH ||
            se.getType() != Material.REDSTONE_TORCH) {
            return false;
        }
        
        // Çember sınırları (Taş Tuğla)
        for (int i = -2; i <= 2; i++) {
            // Kuzey ve Güney sınırları
            if (base.getRelative(i, 0, -2).getType() != Material.STONE_BRICKS ||
                base.getRelative(i, 0, 2).getType() != Material.STONE_BRICKS) {
                return false;
            }
            // Doğu ve Batı sınırları
            if (base.getRelative(-2, 0, i).getType() != Material.STONE_BRICKS ||
                base.getRelative(2, 0, i).getType() != Material.STONE_BRICKS) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isOnCooldown(UUID playerId) {
        Long lastRitual = ritualCooldowns.get(playerId);
        if (lastRitual == null) return false;
        return System.currentTimeMillis() - lastRitual < getRitualCooldown();
    }
    
    private void setCooldown(UUID playerId) {
        ritualCooldowns.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * Oyuncu çıkışında cooldown'u temizle (Memory leak önleme)
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        ritualCooldowns.remove(event.getPlayer().getUniqueId());
    }
}

