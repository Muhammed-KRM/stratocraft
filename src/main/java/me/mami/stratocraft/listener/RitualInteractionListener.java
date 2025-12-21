package me.mami.stratocraft.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;

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
        // ✅ DEBUG: Event tetiklendi - her zaman log
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        Player player = event.getPlayer();
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Event tetiklendi - Oyuncu: " + player.getName() + 
                ", Action: " + event.getAction() + ", Hand: " + event.getHand() + 
                ", Sneaking: " + player.isSneaking() + ", Cancelled: " + event.isCancelled());
        }
        
        // Event zaten cancel edilmişse işleme alma
        if (event.isCancelled()) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Event zaten cancel edilmiş, işlem yapılmıyor");
            }
            return;
        }
        
        // Şartlar: Shift + Sağ Tık + Elde Çakmak
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Action kontrolü başarısız: " + event.getAction() + " (beklenen: RIGHT_CLICK_BLOCK)");
            }
            return;
        }
        
        if (!player.isSneaking()) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Shift kontrolü başarısız - Oyuncu shift'e basmıyor");
            }
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] El kontrolü başarısız: " + event.getHand() + " (beklenen: HAND)");
            }
            return;
        }
        
        ItemStack handItem = event.getItem();
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Elindeki item: " + 
                (handItem != null ? handItem.getType() : "null"));
        }
        
        if (handItem == null || handItem.getType() != Material.FLINT_AND_STEEL) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Çakmak kontrolü başarısız - Elinde: " + 
                    (handItem != null ? handItem.getType() : "null") + " (beklenen: FLINT_AND_STEEL)");
            }
            return;
        }
        
        Block centerBlock = event.getClickedBlock();
        if (centerBlock == null) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Blok kontrolü başarısız - centerBlock null");
            }
            return;
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Tıklanan blok: " + centerBlock.getType() + 
                " @ " + centerBlock.getLocation());
        }
        
        // Tıklanan blok "Soyulmuş Odun" (Stripped Log) olmalı
        if (!isStrippedLog(centerBlock.getType())) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Soyulmuş odun kontrolü başarısız - Blok tipi: " + centerBlock.getType());
            }
            return;
        }
        
        Player leader = player;
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());
        
        // Yetki Kontrolü
        if (clan == null) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Klan kontrolü başarısız - Oyuncunun klanı yok");
            }
            return;
        }
        
        // ✅ YETKİ KONTROLÜ: Elit, General veya Lider olmalı
        Clan.Rank playerRank = clan.getRank(leader.getUniqueId());
        if (playerRank == null || 
            (playerRank != Clan.Rank.LEADER && 
             playerRank != Clan.Rank.GENERAL && 
             playerRank != Clan.Rank.ELITE)) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Yetki kontrolü başarısız - Oyuncu rütbesi: " + 
                    (playerRank != null ? playerRank.name() : "null"));
            }
            leader.sendMessage("§cBu ritüeli sadece Elit, General veya Lider yapabilir!");
            event.setCancelled(true); // Ateş yakmasını engelle
            return;
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] 5x5 çerçeve yapı kontrolü başlatılıyor...");
        }
        
        // ✅ DÜZELTME: 5x5 çerçeve kontrolü (end portalı gibi - kenarlar dolu, içi boş)
        // Tıklanan blok kenarda olmalı, çerçeveyi bul
        RitualFrame frame = findRitualFrame(centerBlock);
        if (frame == null) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Çerçeve bulunamadı - Tıklanan blok kenarda değil veya yapı bozuk");
            }
            leader.sendMessage("§cRitüel yapısı eksik! End portalı gibi 5x5 çerçeve gerekli (kenarlar soyulmuş odun, içi boş).");
            return;
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Çerçeve bulundu! Merkez: " + frame.center + ", İç alan: " + 
                frame.innerMinX + "," + frame.innerMinZ + " -> " + frame.innerMaxX + "," + frame.innerMaxZ);
        }
        
        // Çerçeve kontrolü
        String structureError = checkRitualFrameStructure(frame);
        if (structureError != null) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Çerçeve kontrolü başarısız - " + structureError);
            }
            leader.sendMessage("§cRitüel yapısı eksik! " + structureError);
            leader.sendMessage("§7End portalı gibi 5x5 çerçeve gerekli (kenarlar soyulmuş odun, içi boş).");
            return;
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Çerçeve kontrolü başarılı! Ritüel tetikleniyor...");
        }
        
        event.setCancelled(true); // Normal ateş yakmayı engelle, büyülü ateş yakıcaz
        
        // --- RİTÜEL BAŞARILI ---
        // ✅ OPTİMİZASYON: World'ü cache'le
        org.bukkit.World world = centerBlock.getWorld();
        
        // İç alanın merkezini bul (3x3 iç alan)
        Location innerCenter = new Location(world, 
            frame.innerMinX + 1.5, 
            centerBlock.getY() + 1, 
            frame.innerMinZ + 1.5);
        
        List<Player> recruitedPlayers = new ArrayList<>();
        // İç alandaki oyuncuları bul (3x3 alan, 2 blok yükseklik)
        // ✅ OPTİMİZASYON: instanceof pattern kullan (Java 16+)
        for (Entity entity : world.getNearbyEntities(innerCenter, 1.5, 2, 1.5)) {
            if (entity instanceof Player target) {
                // Kendisi değilse ve klanı yoksa veya farklı klandaysa
                if (!target.equals(leader)) {
                    Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
                    if (targetClan == null || !targetClan.equals(clan)) {
                    recruitedPlayers.add(target);
                }
            }
        }
        }
        
        // ✅ DÜZELTME: Ritüel başarılı olduğunda oyuncu olmasa bile ses ve partikül çıkar
        // Ateş efekti (her zaman göster) - çerçevenin merkezinde
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Ritüel başarılı! Partikül ve ses gösteriliyor...");
        }
        Location effectLoc = frame.center.getLocation().add(0.5, 1, 0.5);
        world.spawnParticle(Particle.FLAME, effectLoc, 100, 1, 0.5, 1, 0.1);
        world.playSound(effectLoc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Ritüel alanında bulunan klansız oyuncu sayısı: " + recruitedPlayers.size());
        }
        
        if (recruitedPlayers.isEmpty()) {
            leader.sendMessage("§eRitüel tamamlandı, ancak alanında klansız kimse yok.");
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Ritüel tamamlandı ama oyuncu yok");
        }
            // Oyuncu olmasa bile ritüel başarılı sayılır, sadece kimse eklenmedi
        } else {
        // Oyuncuları Klana Ekle
        for (Player newMember : recruitedPlayers) {
            clanManager.addMember(clan, newMember.getUniqueId(), Clan.Rank.RECRUIT);
            newMember.sendMessage("§6§l" + clan.getName() + " §eklanına ruhun bağlandı!");
            newMember.getWorld().spawnParticle(Particle.FLAME, newMember.getLocation(), 50, 0.5, 1, 0.5, 0.1);
            newMember.playSound(newMember.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
            newMember.sendTitle("§a§lKLANA KATILDI", "§e" + clan.getName(), 10, 70, 20);
        }
        
        leader.sendMessage("§aRitüel tamamlandı! " + recruitedPlayers.size() + " kişi katıldı.");
        }
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Ritüel başarılı oldu (oyuncu olmasa bile)
        if (plugin != null) {
            if (plugin.getStratocraftPowerSystem() != null) {
                java.util.Map<String, Integer> usedResources = new java.util.HashMap<>();
                usedResources.put("FLINT_AND_STEEL", 1); // Çakmak tüketildi
                
                plugin.getStratocraftPowerSystem().onRitualSuccess(
                    clan,
                    "RECRUITMENT_RITUAL",
                    usedResources
                );
            }
            
            // ✅ KLAN GÖREV SİSTEMİ ENTEGRASYONU: Ritüel görevi ilerlemesi (oyuncu olmasa bile)
            if (plugin.getClanMissionSystem() != null) {
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
    
    // ✅ YENİ: Ritüel çerçeve bilgisi
    private static class RitualFrame {
        Block center; // Çerçevenin merkez bloğu (kenardaki tıklanan bloktan hesaplanır)
        int minX, maxX, minZ, maxZ; // Çerçevenin sınırları (5x5)
        int innerMinX, innerMaxX, innerMinZ, innerMaxZ; // İç alan sınırları (3x3)
        
        RitualFrame(Block center, int minX, int maxX, int minZ, int maxZ) {
            this.center = center;
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
            // İç alan: kenarlar hariç (1 blok içeride)
            this.innerMinX = minX + 1;
            this.innerMaxX = maxX - 1;
            this.innerMinZ = minZ + 1;
            this.innerMaxZ = maxZ - 1;
        }
    }
    
    // ✅ YENİ: Tıklanan bloktan 5x5 çerçeveyi bul (soyulmuş odun için)
    // Tıklanan blok kenarda olmalı, çerçeveyi bulmak için tıklanan bloktan başlayarak 5x5 çerçeve kontrol eder
    private RitualFrame findRitualFrame(Block clickedBlock) {
        return findRitualFrame(clickedBlock, true); // true = soyulmuş odun
    }
    
    // ✅ YENİ: Tıklanan bloktan 5x5 çerçeveyi bul (taş tuğla veya soyulmuş odun)
    private RitualFrame findRitualFrame(Block clickedBlock, boolean useStrippedLog) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        
        int clickedX = clickedBlock.getX();
        int clickedZ = clickedBlock.getZ();
        int clickedY = clickedBlock.getY();
        org.bukkit.World world = clickedBlock.getWorld();
        
        // ✅ OPTİMİZASYON: Tıklanan blok kenarda olmalı, çerçeveyi bulmak için
        // Tıklanan blok çerçevenin herhangi bir kenarında olabilir
        // 5x5 çerçeve için, tıklanan blok çerçevenin 0-4 pozisyonunda olabilir
        
        // Çerçeve bulma: Tıklanan bloktan başlayarak 5x5 çerçeve olup olmadığını kontrol et
        // Tıklanan blok köşe olabilir (offsetX=0 veya 4, offsetZ=0 veya 4)
        // Tıklanan blok kenar olabilir (offsetX=0-4, offsetZ=0 veya 4, veya offsetX=0 veya 4, offsetZ=0-4)
        
        for (int offsetX = 0; offsetX <= 4; offsetX++) {
            for (int offsetZ = 0; offsetZ <= 4; offsetZ++) {
                int minX = clickedX - offsetX;
                int maxX = minX + 4;
                int minZ = clickedZ - offsetZ;
                int maxZ = minZ + 4;
                
                // Çerçeve kontrolü: kenarlar soyulmuş odun veya taş tuğla olmalı
                boolean isValidFrame = true;
                
                // Üst kenar (minZ) - 5 blok
                for (int x = minX; x <= maxX && isValidFrame; x++) {
                    Block b = world.getBlockAt(x, clickedY, minZ);
                    if (useStrippedLog) {
                        if (!isStrippedLog(b.getType())) {
                            isValidFrame = false;
                        }
                    } else {
                        if (b.getType() != Material.STONE_BRICKS) {
                            isValidFrame = false;
                        }
                    }
                }
                if (!isValidFrame) continue;
                
                // Alt kenar (maxZ) - 5 blok
                for (int x = minX; x <= maxX && isValidFrame; x++) {
                    Block b = world.getBlockAt(x, clickedY, maxZ);
                    if (useStrippedLog) {
                        if (!isStrippedLog(b.getType())) {
                            isValidFrame = false;
                        }
                    } else {
                        if (b.getType() != Material.STONE_BRICKS) {
                            isValidFrame = false;
                        }
                    }
                }
                if (!isValidFrame) continue;
                
                // Sol kenar (minX) - 3 blok (köşeler hariç)
                for (int z = minZ + 1; z < maxZ && isValidFrame; z++) {
                    Block b = world.getBlockAt(minX, clickedY, z);
                    if (useStrippedLog) {
                        if (!isStrippedLog(b.getType())) {
                            isValidFrame = false;
                        }
                    } else {
                        if (b.getType() != Material.STONE_BRICKS) {
                            isValidFrame = false;
                        }
                    }
                }
                if (!isValidFrame) continue;
                
                // Sağ kenar (maxX) - 3 blok (köşeler hariç)
                for (int z = minZ + 1; z < maxZ && isValidFrame; z++) {
                    Block b = world.getBlockAt(maxX, clickedY, z);
                    if (useStrippedLog) {
                        if (!isStrippedLog(b.getType())) {
                            isValidFrame = false;
                        }
                    } else {
                        if (b.getType() != Material.STONE_BRICKS) {
                            isValidFrame = false;
                        }
                    }
                }
                if (!isValidFrame) continue;
                
                // Çerçeve bulundu! Merkez bloğu hesapla (kenardan 2 blok içeride)
                Block centerBlock = world.getBlockAt(minX + 2, clickedY, minZ + 2);
                if (plugin != null) {
                    plugin.getLogger().info("[RITÜEL] Çerçeve bulundu: " + minX + "," + minZ + " -> " + maxX + "," + maxZ + 
                        " (Tıklanan: " + clickedX + "," + clickedZ + ")");
                }
                return new RitualFrame(centerBlock, minX, maxX, minZ, maxZ);
            }
        }
        
        if (plugin != null) {
            plugin.getLogger().info("[RITÜEL] Çerçeve bulunamadı - Tıklanan blok: " + clickedX + "," + clickedZ);
        }
        return null; // Çerçeve bulunamadı
    }
    
    // ✅ YENİ: 5x5 çerçeve yapısını kontrol et (kenarlar dolu, içi boş)
    private String checkRitualFrameStructure(RitualFrame frame) {
        return checkRitualFrameStructure(frame, true); // true = soyulmuş odun
    }
    
    // ✅ YENİ: 5x5 çerçeve yapısını kontrol et (taş tuğla veya soyulmuş odun)
    private String checkRitualFrameStructure(RitualFrame frame, boolean useStrippedLog) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        org.bukkit.World world = frame.center.getWorld();
        int y = frame.center.getY();
        
        java.util.List<String> errors = new java.util.ArrayList<>();
        
        // Kenarları kontrol et (soyulmuş odun veya taş tuğla olmalı)
        // Üst kenar (minZ)
        for (int x = frame.minX; x <= frame.maxX; x++) {
            Block b = world.getBlockAt(x, y, frame.minZ);
            if (useStrippedLog) {
                if (!isStrippedLog(b.getType())) {
                    errors.add("Üst kenar (" + x + "," + frame.minZ + "): " + b.getType());
                }
            } else {
                if (b.getType() != Material.STONE_BRICKS) {
                    errors.add("Üst kenar (" + x + "," + frame.minZ + "): " + b.getType());
                }
            }
        }
        
        // Alt kenar (maxZ)
        for (int x = frame.minX; x <= frame.maxX; x++) {
            Block b = world.getBlockAt(x, y, frame.maxZ);
            if (useStrippedLog) {
                if (!isStrippedLog(b.getType())) {
                    errors.add("Alt kenar (" + x + "," + frame.maxZ + "): " + b.getType());
                }
            } else {
                if (b.getType() != Material.STONE_BRICKS) {
                    errors.add("Alt kenar (" + x + "," + frame.maxZ + "): " + b.getType());
                }
            }
        }
        
        // Sol kenar (minX)
        for (int z = frame.minZ + 1; z < frame.maxZ; z++) {
            Block b = world.getBlockAt(frame.minX, y, z);
            if (useStrippedLog) {
                if (!isStrippedLog(b.getType())) {
                    errors.add("Sol kenar (" + frame.minX + "," + z + "): " + b.getType());
                }
            } else {
                if (b.getType() != Material.STONE_BRICKS) {
                    errors.add("Sol kenar (" + frame.minX + "," + z + "): " + b.getType());
                }
            }
        }
        
        // Sağ kenar (maxX)
        for (int z = frame.minZ + 1; z < frame.maxZ; z++) {
            Block b = world.getBlockAt(frame.maxX, y, z);
            if (useStrippedLog) {
                if (!isStrippedLog(b.getType())) {
                    errors.add("Sağ kenar (" + frame.maxX + "," + z + "): " + b.getType());
                }
            } else {
                if (b.getType() != Material.STONE_BRICKS) {
                    errors.add("Sağ kenar (" + frame.maxX + "," + z + "): " + b.getType());
                }
            }
        }
        
        // İç alanı kontrol et (boş olmalı - AIR veya hava olmalı)
        for (int x = frame.innerMinX; x <= frame.innerMaxX; x++) {
            for (int z = frame.innerMinZ; z <= frame.innerMaxZ; z++) {
                Block b = world.getBlockAt(x, y, z);
                if (b.getType() != Material.AIR && !b.getType().isAir()) {
                    errors.add("İç alan (" + x + "," + z + "): " + b.getType() + " (boş olmalı)");
            }
        }
        }
        
        if (errors.isEmpty()) {
            if (plugin != null) {
                plugin.getLogger().info("[RITÜEL] Çerçeve kontrolü başarılı - Kenarlar dolu, içi boş");
            }
            return null; // Başarılı
        } else {
            return "Hatalar: " + String.join(", ", errors.subList(0, Math.min(5, errors.size()))) + 
                (errors.size() > 5 ? " ve " + (errors.size() - 5) + " hata daha" : "");
        }
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
    // ========== KLANDAN ÇIKMA RİTÜELİ: "Yemin Kırma Ritüeli" (5x5 Taş Tuğla Çerçeve) ==========
    // Kurucu hariç herkes klandan çıkabilir, 5x5 taş tuğla çerçeveye shift+sağ tık+çakmak yapar
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeaveRitual(PlayerInteractEvent event) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        Player player = event.getPlayer();
        
        // Event zaten cancel edilmişse işleme alma
        if (event.isCancelled()) return;
        
        // Şartlar: Shift + Sağ Tık + Elde Çakmak
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        ItemStack handItem = event.getItem();
        if (handItem == null || handItem.getType() != Material.FLINT_AND_STEEL) return;
        
        Block centerBlock = event.getClickedBlock();
        if (centerBlock == null) return;
        
        // Tıklanan blok "Taş Tuğla" (Stone Bricks) olmalı
        if (centerBlock.getType() != Material.STONE_BRICKS) return;
        
        Player p = player;
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        
        if (clan == null) {
            if (plugin != null) {
                plugin.getLogger().info("[ÇIKMA RİTÜELİ] Klan kontrolü başarısız - Oyuncunun klanı yok");
            }
            p.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // ✅ YETKİ KONTROLÜ: Kurucu (Lider) hariç herkes klandan çıkabilir
        if (clan.getRank(p.getUniqueId()) == Clan.Rank.LEADER) {
            if (plugin != null) {
                plugin.getLogger().info("[ÇIKMA RİTÜELİ] Yetki kontrolü başarısız - Lider klandan ayrılamaz");
            }
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
        
        // 5x5 taş tuğla çerçeve kontrolü
        RitualFrame frame = findRitualFrame(centerBlock, false); // false = taş tuğla
        if (frame == null) {
            if (plugin != null) {
                plugin.getLogger().info("[ÇIKMA RİTÜELİ] Çerçeve bulunamadı");
            }
            p.sendMessage("§cRitüel yapısı eksik! End portalı gibi 5x5 taş tuğla çerçeve gerekli (kenarlar taş tuğla, içi boş).");
            return;
        }
        
        String structureError = checkRitualFrameStructure(frame, false); // false = taş tuğla
        if (structureError != null) {
            if (plugin != null) {
                plugin.getLogger().info("[ÇIKMA RİTÜELİ] Çerçeve kontrolü başarısız - " + structureError);
            }
            p.sendMessage("§cRitüel yapısı eksik! " + structureError);
            return;
        }
        
        event.setCancelled(true); // Normal ateş yakmayı engelle
        
        // ✅ NULL KONTROLÜ: Klan kontrolü (ayrılmadan önce kaydet)
        me.mami.stratocraft.model.Clan ritualClan = clan;
        String clanName = clan.getName();
        String playerName = p.getName();
        
        // Klandan ayrıl
        clanManager.removeMember(clan, p.getUniqueId());
        
        // Ritüel efekti
        org.bukkit.World world = centerBlock.getWorld();
        Location effectLoc = frame.center.getLocation().add(0.5, 1, 0.5);
        
        // Ateş partikülleri
        world.spawnParticle(Particle.FLAME, effectLoc, 50, 1, 0.5, 1, 0.1);
        world.spawnParticle(Particle.SMOKE_NORMAL, effectLoc, 30, 0.5, 0.5, 0.5, 0.05);
        
        // Oyuncu etrafında efektler
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        p.getWorld().spawnParticle(Particle.CRIT, p.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.2);
        
        // Sesler
        p.playSound(effectLoc, Sound.ITEM_FIRECHARGE_USE, 1f, 0.8f);
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        
        // Title
        p.sendTitle("§e§lYEMİN KIRILDI", "§7" + clanName + " klanından ayrıldın", 10, 70, 20);
        p.sendMessage("§e§l" + clanName + " §7klanından ayrıldın!");
        
        // Çakmağı tüket
        if (handItem.getDurability() >= handItem.getType().getMaxDurability() - 1) {
            handItem.setAmount(0);
        } else {
            handItem.setDurability((short) (handItem.getDurability() + 1));
        }
        
        // ✅ GÜÇ SİSTEMİ ENTEGRASYONU: Ritüel başarılı oldu (ayrılma ritüeli)
        if (ritualClan != null && plugin != null) {
            if (plugin.getStratocraftPowerSystem() != null) {
                java.util.Map<String, Integer> usedResources = new java.util.HashMap<>();
                usedResources.put("FLINT_AND_STEEL", 1); // Çakmak tüketildi
                
                plugin.getStratocraftPowerSystem().onRitualSuccess(
                    ritualClan,
                    "LEAVE_RITUAL",
                    usedResources
                );
            }
            
            // ✅ KLAN GÖREV SİSTEMİ ENTEGRASYONU: Ritüel görevi ilerlemesi (ayrılma ritüeli)
            if (plugin.getClanMissionSystem() != null) {
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
        
            // Klan üyelerine bildir
            for (UUID memberId : ritualClan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§7" + playerName + " klanından ayrıldı.");
                }
            }
        }
        
        // Cooldown ekle
        setCooldown(p.getUniqueId());
    }

    // ========== TERFİ RİTÜELİ: "Yükseltme Ritüeli" (5x5 Çerçeve) ==========
    // General veya Lider elinde Altın/Demir ile 5x5 çerçeveye shift+sağ tık+çakmak yapar
    // İç alandaki oyuncular terfi edilir
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPromotionRitual(PlayerInteractEvent event) {
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        Player player = event.getPlayer();
        
        // Event zaten cancel edilmişse işleme alma
        if (event.isCancelled()) return;
        
        // Şartlar: Shift + Sağ Tık + Elde Çakmak
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        ItemStack handItem = event.getItem();
        if (handItem == null || handItem.getType() != Material.FLINT_AND_STEEL) return;
        
        Block centerBlock = event.getClickedBlock();
        if (centerBlock == null) return;
        
        // Tıklanan blok "Soyulmuş Odun" (Stripped Log) olmalı
        if (!isStrippedLog(centerBlock.getType())) return;
        
        Player leader = player;
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());

        if (clan == null) {
            if (plugin != null) {
                plugin.getLogger().info("[TERFİ RİTÜELİ] Klan kontrolü başarısız - Oyuncunun klanı yok");
            }
            return;
        }
        
        // ✅ YETKİ KONTROLÜ: Sadece General veya Lider terfi yapabilir
        Clan.Rank playerRank = clan.getRank(leader.getUniqueId());
        if (playerRank == null || 
            (playerRank != Clan.Rank.LEADER && playerRank != Clan.Rank.GENERAL)) {
            if (plugin != null) {
                plugin.getLogger().info("[TERFİ RİTÜELİ] Yetki kontrolü başarısız - Oyuncu rütbesi: " + 
                    (playerRank != null ? playerRank.name() : "null"));
            }
            leader.sendMessage("§cBu ritüeli sadece General veya Lider yapabilir!");
            event.setCancelled(true);
            return;
        }
        
        // Cooldown kontrolü
        if (isOnCooldown(leader.getUniqueId())) {
            leader.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            event.setCancelled(true);
            return;
        }
        
        // 5x5 çerçeve kontrolü
        RitualFrame frame = findRitualFrame(centerBlock);
        if (frame == null) {
            if (plugin != null) {
                plugin.getLogger().info("[TERFİ RİTÜELİ] Çerçeve bulunamadı");
            }
            leader.sendMessage("§cRitüel yapısı eksik! End portalı gibi 5x5 çerçeve gerekli (kenarlar soyulmuş odun, içi boş).");
            return;
        }
        
        String structureError = checkRitualFrameStructure(frame);
        if (structureError != null) {
            if (plugin != null) {
                plugin.getLogger().info("[TERFİ RİTÜELİ] Çerçeve kontrolü başarısız - " + structureError);
            }
            leader.sendMessage("§cRitüel yapısı eksik! " + structureError);
                return;
            }
            
        event.setCancelled(true); // Normal ateş yakmayı engelle
        
        // İç alandaki oyuncuları bul
        org.bukkit.World world = centerBlock.getWorld();
        Location innerCenter = new Location(world, 
            frame.innerMinX + 1.5, 
            centerBlock.getY() + 1, 
            frame.innerMinZ + 1.5);
        
        List<Player> targets = new ArrayList<>();
        for (Entity entity : world.getNearbyEntities(innerCenter, 1.5, 2, 1.5)) {
            if (entity instanceof Player target && !target.equals(leader)) {
                // Klan üyesi olmalı
                if (clan.getMembers().containsKey(target.getUniqueId())) {
                    targets.add(target);
                }
            }
        }
        
        if (targets.isEmpty()) {
            leader.sendMessage("§eRitüel tamamlandı, ancak alanında klan üyesi yok.");
            // Çakmağı tüket
            if (handItem.getDurability() >= handItem.getType().getMaxDurability() - 1) {
                handItem.setAmount(0);
            } else {
                handItem.setDurability((short) (handItem.getDurability() + 1));
            }
                            return;
                        }
                        
        // Ritüel efekti
        Location effectLoc = frame.center.getLocation().add(0.5, 1, 0.5);
        world.spawnParticle(Particle.TOTEM, effectLoc, 100, 1, 0.5, 1, 0.1);
        world.playSound(effectLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                            
        // Elindeki item'a göre terfi yap
        ItemStack inventoryItem = leader.getInventory().getItemInMainHand();
        Material terfiItem = null;
        if (inventoryItem != null) {
            if (inventoryItem.getType() == Material.GOLD_INGOT) {
                terfiItem = Material.GOLD_INGOT;
            } else if (inventoryItem.getType() == Material.IRON_INGOT) {
                terfiItem = Material.IRON_INGOT;
            }
        }
        
        if (terfiItem == null) {
            leader.sendMessage("§cTerfi ritüeli için elinde Altın Külçe (General terfisi) veya Demir Külçe (Üye terfisi) olmalı!");
            // Çakmağı tüket
            if (handItem.getDurability() >= handItem.getType().getMaxDurability() - 1) {
                handItem.setAmount(0);
                        } else {
                handItem.setDurability((short) (handItem.getDurability() + 1));
            }
                            return;
                        }
                        
        int terfiSayisi = 0;
        for (Player target : targets) {
            Clan.Rank currentRank = clan.getRank(target.getUniqueId());
            if (currentRank == null) continue;
            
            boolean terfiEdildi = false;
            if (terfiItem == Material.GOLD_INGOT && currentRank == Clan.Rank.MEMBER) {
                // Member -> General
                clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.GENERAL);
                terfiEdildi = true;
                
                target.getWorld().spawnParticle(Particle.TOTEM, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.3);
                target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                target.sendTitle("§6§lTERFİ EDİLDİN", "§eGeneral rütbesine yükseltildin!", 10, 70, 20);
                leader.sendMessage("§a" + target.getName() + " General rütbesine yükseltildi!");
                
            } else if (terfiItem == Material.IRON_INGOT && currentRank == Clan.Rank.RECRUIT) {
                // Recruit -> Member
                clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.MEMBER);
                terfiEdildi = true;
                
                target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.3);
                target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            target.sendTitle("§a§lTERFİ EDİLDİN", "§eÜye rütbesine yükseltildin!", 10, 70, 20);
                leader.sendMessage("§a" + target.getName() + " Üye rütbesine yükseltildi!");
            }
            
            if (terfiEdildi) {
                terfiSayisi++;
            }
        }
        
        if (terfiSayisi > 0) {
            // Item tüket
            if (inventoryItem.getAmount() > 1) {
                inventoryItem.setAmount(inventoryItem.getAmount() - 1);
                            } else {
                                leader.getInventory().setItemInMainHand(null);
                            }
                            
            leader.sendMessage("§aRitüel tamamlandı! " + terfiSayisi + " kişi terfi etti.");
                        } else {
            leader.sendMessage("§eRitüel tamamlandı, ancak terfi edilecek kimse yok.");
                        }
        
        // Çakmağı tüket
        if (handItem.getDurability() >= handItem.getType().getMaxDurability() - 1) {
            handItem.setAmount(0);
        } else {
            handItem.setDurability((short) (handItem.getDurability() + 1));
            }
        
        setCooldown(leader.getUniqueId());
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
                        // YENİ: Null kontrolü - lambda içinde farklı isim kullan
                        ItemStack leaderHandItem = leader.getInventory().getItemInMainHand();
                        if (leaderHandItem != null && leaderHandItem.getAmount() > 1) {
                            leaderHandItem.setAmount(leaderHandItem.getAmount() - 1);
                        } else if (leaderHandItem != null) {
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
     * ✅ TÜM PUSULALARDA IŞINLAMA ENGELLENDİ
     * Lodestone bağlantısını kaldır ve ışınlamayı engelle
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCompassInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player p = event.getPlayer();
        ItemStack handItem = p.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() != Material.COMPASS) return;
        
        // ✅ Lodestone bağlantısını kaldır (metadata'dan)
        if (handItem.hasItemMeta()) {
            org.bukkit.inventory.meta.ItemMeta meta = handItem.getItemMeta();
            if (meta != null && meta instanceof org.bukkit.inventory.meta.CompassMeta) {
                org.bukkit.inventory.meta.CompassMeta compassMeta = (org.bukkit.inventory.meta.CompassMeta) meta;
                if (compassMeta.getLodestone() != null) {
                    compassMeta.setLodestone(null);
                    handItem.setItemMeta(compassMeta);
                    p.getInventory().setItemInMainHand(handItem);
                }
            }
        }
        
        // ✅ Tüm pusula tıklamalarında ışınlamayı engelle
        if (event.getAction() == Action.LEFT_CLICK_AIR || 
            event.getAction() == Action.LEFT_CLICK_BLOCK ||
            event.getAction() == Action.RIGHT_CLICK_AIR ||
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
        }
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
        
        // ✅ TÜM PUSULALARDA IŞINLAMA ENGELLENDİ - Sadece klan bilgisi göster
        event.setCancelled(true);
        
        // Oyuncunun klanını bul (kendi klanı veya yakındaki bir klan)
        Clan targetClan = clanManager.getClanByPlayer(p.getUniqueId());
        
        // Eğer klanı yoksa, yakındaki klan kristalini bul
        // ✅ PERFORMANS: Limit ekle - maksimum 10 klan kontrol et
        if (targetClan == null) {
            double minDistance = Double.MAX_VALUE;
            int checkedCount = 0;
            int maxChecks = 10; // ✅ OPTİMİZE: Maksimum 10 klan kontrol et
            
            for (Clan clan : clanManager.getAllClans()) {
                if (checkedCount >= maxChecks) break; // ✅ OPTİMİZE: Limit'e ulaşıldı
                
                if (clan.getCrystalLocation() != null) {
                    double distance = p.getLocation().distance(clan.getCrystalLocation());
                    if (distance <= 20 && distance < minDistance) {
                        minDistance = distance;
                        targetClan = clan;
                    }
                }
                checkedCount++;
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

