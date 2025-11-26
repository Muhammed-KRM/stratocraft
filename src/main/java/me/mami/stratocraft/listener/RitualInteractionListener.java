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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RitualInteractionListener - Komutsuz Ritüel Sistemi
 * Oyuncular fiziksel etkileşimlerle klan işlemlerini yapar
 */
public class RitualInteractionListener implements Listener {
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    
    // Cooldown sistemi: Oyuncu UUID -> Son ritüel zamanı
    private final Map<UUID, Long> ritualCooldowns = new HashMap<>();
    private static final long RITUAL_COOLDOWN = 10000L; // 10 saniye

    public RitualInteractionListener(ClanManager cm, TerritoryManager tm) {
        this.clanManager = cm;
        this.territoryManager = tm;
    }

    // ========== KLAN KURMA: "Temel Taşı Ritüeli" ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onClanCreate(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        
        Player p = event.getPlayer();
        Block clicked = event.getClickedBlock();
        
        // Crafting Table kontrolü
        if (clicked.getType() != Material.CRAFTING_TABLE) return;
        
        // Oyuncu zaten bir klana üye mi?
        if (clanManager.getClanByPlayer(p.getUniqueId()) != null) {
            p.sendMessage("§cZaten bir klana üyesin!");
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(p.getUniqueId())) {
            p.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Elinde Named Paper var mı?
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.PAPER) return;
        
        ItemMeta meta = handItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            p.sendMessage("§cRitüel için isimlendirilmiş bir kağıt gerekli! (Örs'te isim yaz)");
            return;
        }
        
        String clanName = meta.getDisplayName().replace("§r", "").trim();
        if (clanName.isEmpty()) {
            p.sendMessage("§cKağıdın üzerinde bir isim olmalı!");
            return;
        }
        
        // 3x3 Cobblestone platform kontrolü
        Block tableBlock = clicked;
        if (!checkCobblestonePlatform(tableBlock)) {
            p.sendMessage("§cRitüel için 3x3 Kırık Taş (Cobblestone) platform gerekli!");
            // Görsel geri bildirim: Yanlış blokların üzerinde kırmızı partiküller
            showPlatformError(tableBlock);
            return;
        }
        
        // Oyuncu Crafting Table'ın üzerinde mi?
        if (!p.getLocation().getBlock().equals(tableBlock.getRelative(BlockFace.UP))) {
            p.sendMessage("§cRitüel için Çalışma Masasının üzerine çıkmalısın!");
            return;
        }
        
        // Klan oluştur
        Clan newClan = clanManager.createClan(clanName, p.getUniqueId());
        if (newClan != null) {
            // Bölge oluştur
            newClan.setTerritory(new me.mami.stratocraft.model.Territory(newClan.getId(), p.getLocation()));
            
            // Efektler
            Location loc = tableBlock.getLocation().add(0.5, 1, 0.5);
            
            // Şimşek (zarar vermeyen)
            p.getWorld().strikeLightningEffect(loc);
            
            // Partiküller
            p.getWorld().spawnParticle(Particle.TOTEM, loc, 100, 1, 1, 1, 0.5);
            p.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 1, 1, 1, 0.3);
            
            // Ses
            p.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            p.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
            
            // Title
            p.sendTitle("§6§lKLAN KURULDU", "§e" + clanName, 10, 70, 20);
            
            // Kağıdı tüket
            handItem.setAmount(handItem.getAmount() - 1);
            
            // Cooldown ekle
            setCooldown(p.getUniqueId());
            
            Bukkit.broadcastMessage("§6§l" + p.getName() + " §6klanı kurdu: §e" + clanName);
        }
    }

    // ========== KLAN DAVET: "Yemin Çemberi" ==========
    
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
        targetClan.getMembers().remove(target.getUniqueId());
        
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

    // ========== KLANDAN AYRILMA: "Bağ Kesme" ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeaveRitual(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return; // Shift kontrolü
        
        Player p = event.getPlayer();
        Block clicked = event.getClickedBlock();
        
        // Beacon (Core) kontrolü
        if (clicked.getType() != Material.BEACON) return;
        
        // Elinde Makas var mı?
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.SHEARS) return;
        
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        if (clan == null) {
            p.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // Bu klanın Core'u mu?
        Structure core = clan.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .filter(s -> s.getLocation().getBlock().equals(clicked))
                .findFirst().orElse(null);
        
        if (core == null) {
            p.sendMessage("§cBu senin klanının Ana Kristali değil!");
            return;
        }
        
        // Lider ayrılamaz
        if (clan.getRank(p.getUniqueId()) == Clan.Rank.LEADER) {
            p.sendMessage("§cLider klandan ayrılamaz! Önce liderliği devret.");
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(p.getUniqueId())) {
            p.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Klandan ayrıl
        clan.getMembers().remove(p.getUniqueId());
        
        // Makası kır
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            p.getInventory().setItemInMainHand(null);
        }
        
        // Efektler
        Location coreLoc = clicked.getLocation().add(0.5, 1, 0.5);
        coreLoc.getWorld().spawnParticle(Particle.CRIT, coreLoc, 30, 0.5, 1, 0.5, 0.3);
        p.playSound(coreLoc, Sound.ITEM_SHEARS_BREAK, 1f, 1f);
        
        // Işınlanma (bölgenin dışına)
        Location teleportLoc = territoryManager.getClanManager().getAllClans().stream()
                .filter(c -> c.getTerritory() != null)
                .filter(c -> c.getTerritory().getCenter().distance(p.getLocation()) < 100)
                .map(c -> c.getTerritory().getCenter())
                .findFirst()
                .map(loc -> loc.clone().add(50, 10, 50)) // Güvenli mesafe
                .orElse(p.getWorld().getSpawnLocation());
        
        p.teleport(teleportLoc);
        p.sendTitle("§e§lBAĞ KESİLDİ", "§7Klandan ayrıldın", 10, 70, 20);
        p.sendMessage("§eKlandan ayrıldın!");
        
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
            
            // Altın Külçe ile General terfisi
            if (leader.getInventory().getItemInMainHand().getType() == Material.GOLD_INGOT) {
                
                leader.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof Player && e != leader)
                    .map(e -> (Player)e)
                    .findFirst()
                    .ifPresent(target -> {
                        if (clan.getRank(target.getUniqueId()) == Clan.Rank.MEMBER) {
                            clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.GENERAL);
                            
                            // Efektler
                            Location loc = target.getLocation();
                            loc.getWorld().spawnParticle(Particle.TOTEM, loc, 30, 0.5, 1, 0.5, 0.3);
                            leader.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                            
                            leader.sendMessage("§a" + target.getName() + " General rütbesine yükseltildi!");
                            target.sendTitle("§6§lTERFİ EDİLDİN", "§eGeneral rütbesine yükseltildin!", 10, 70, 20);
                            leader.getInventory().getItemInMainHand().setAmount(leader.getInventory().getItemInMainHand().getAmount() - 1);
                            
                            setCooldown(leader.getUniqueId());
                        } else {
                            leader.sendMessage("§eBu kişi zaten General veya daha üst rütbede.");
                        }
                    });
            } else if (leader.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT) {
                // Üye terfisi: Recruit -> Member
                leader.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof Player && e != leader)
                    .map(e -> (Player)e)
                    .findFirst()
                    .ifPresent(target -> {
                        if (clan.getRank(target.getUniqueId()) == Clan.Rank.RECRUIT) {
                            clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.MEMBER);
                            
                            // Efektler
                            Location loc = target.getLocation();
                            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 30, 0.5, 1, 0.5, 0.3);
                            leader.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            
                            leader.sendMessage("§a" + target.getName() + " Üye rütbesine yükseltildi!");
                            target.sendTitle("§a§lTERFİ EDİLDİN", "§eÜye rütbesine yükseltildin!", 10, 70, 20);
                            leader.getInventory().getItemInMainHand().setAmount(leader.getInventory().getItemInMainHand().getAmount() - 1);
                            
                            setCooldown(leader.getUniqueId());
                        } else {
                            leader.sendMessage("§eBu kişi zaten Üye veya daha üst rütbede.");
                        }
                    });
            }
        }
    }

    // ========== MÜTTEFİKLİK: "Kan Anlaşması" ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onAllianceRitual(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_ENTITY) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return; // Shift kontrolü
        
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player p1 = event.getPlayer();
        Player p2 = (Player) event.getRightClicked();
        
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
            return; // Sadece liderler müttefik olabilir
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(p1.getUniqueId()) || isOnCooldown(p2.getUniqueId())) {
            p1.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Müttefiklik (basit versiyon - guests olarak ekle)
        clan1.addGuest(p2.getUniqueId());
        clan2.addGuest(p1.getUniqueId());
        
        // Elmasları tüket
        handItem.setAmount(handItem.getAmount() - 1);
        p2Hand.setAmount(p2Hand.getAmount() - 1);
        
        // Efektler
        Location midLoc = p1.getLocation().add(p2.getLocation()).multiply(0.5);
        midLoc.getWorld().spawnParticle(Particle.HEART, midLoc, 20, 1, 1, 1, 0.1);
        midLoc.getWorld().spawnParticle(Particle.END_ROD, midLoc, 30, 1, 1, 1, 0.2);
        
        p1.playSound(midLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        p2.playSound(midLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        
        p1.sendTitle("§a§lMÜTTEFİK OLUNDU", "§e" + clan2.getName(), 10, 70, 20);
        p2.sendTitle("§a§lMÜTTEFİK OLUNDU", "§e" + clan1.getName(), 10, 70, 20);
        
        Bukkit.broadcastMessage("§a§l" + clan1.getName() + " §7ve §a" + clan2.getName() + " §7klanları müttefik oldu!");
        
        // Cooldown ekle
        setCooldown(p1.getUniqueId());
        setCooldown(p2.getUniqueId());
    }

    // ========== YARDIMCI METODLAR ==========
    
    private boolean checkCobblestonePlatform(Block center) {
        // 3x3 Cobblestone kontrolü (center'ın altında)
        Block base = center.getRelative(BlockFace.DOWN);
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block check = base.getRelative(x, 0, z);
                if (check.getType() != Material.COBBLESTONE) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Ritüel platform hatası için görsel geri bildirim
     * Yanlış blokların üzerinde kırmızı partiküller gösterir
     */
    private void showPlatformError(Block center) {
        Block base = center.getRelative(BlockFace.DOWN);
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block check = base.getRelative(x, 0, z);
                if (check.getType() != Material.COBBLESTONE) {
                    // Yanlış blok - kırmızı partikül göster
                    Location loc = check.getLocation().add(0.5, 1.2, 0.5);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 10, 
                        new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
                }
            }
        }
    }
    
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
        return System.currentTimeMillis() - lastRitual < RITUAL_COOLDOWN;
    }
    
    private void setCooldown(UUID playerId) {
        ritualCooldowns.put(playerId, System.currentTimeMillis());
    }
}

