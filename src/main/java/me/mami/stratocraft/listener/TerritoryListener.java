package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.SiegeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Territory;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TerritoryListener implements Listener {
    private final TerritoryManager territoryManager;
    private final SiegeManager siegeManager;

    public TerritoryListener(TerritoryManager tm, SiegeManager sm) {
        this.territoryManager = tm;
        this.siegeManager = sm;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(event.getPlayer())) {
            return; // Admin bypass yetkisi varsa korumaları atla
        }
        
        Clan owner = territoryManager.getTerritoryOwner(event.getBlock().getLocation());
        
        // Sahipsiz yerse kırılabilir
        if (owner == null) return;
        
        // Ölümsüz klan önleme: Kristal yoksa bölge koruması yok
        if (!owner.hasCrystal()) {
            return; // Kristal yoksa koruma yok
        }
        
        // Kendi yerinse kırılabilir (Rütbe kontrolü dahil)
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(event.getPlayer().getUniqueId());
        
        if (playerClan != null && playerClan.equals(owner)) {
            // Rütbe Kontrolü: Recruit (Acemi) yapı kıramaz
            if (playerClan.getRank(event.getPlayer().getUniqueId()) == Clan.Rank.RECRUIT) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cAcemilerin yapı yıkma yetkisi yok!");
                return;
            }
            return; // Yetkisi varsa kırabilir
        }
        
        // Misafir İzni (Guest)
        if (owner.isGuest(event.getPlayer().getUniqueId())) {
             return; 
        }

        // --- ENERJİ KALKANI OFFLINE KORUMA ---
        // Eğer klan üyelerinden hiçbiri online değilse VE kalkan yakıtı > 0 ise hasarı iptal et
        Structure core = owner.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .findFirst().orElse(null);
        
        if (core != null && core.isShieldActive()) {
            boolean anyOnline = owner.getMembers().keySet().stream()
                    .anyMatch(uuid -> org.bukkit.Bukkit.getPlayer(uuid) != null);
            
            if (!anyOnline) {
                // Offline koruma aktif
                event.setCancelled(true);
                event.getPlayer().sendMessage("§bEnerji Kalkanı aktif! Offline klan korunuyor. Kalkan Gücü: " + core.getShieldFuel());
                core.consumeFuel(); // Yakıt tüket
                return;
            }
        }

        // --- TAMAMLANMIŞ KUŞATMA KONTROLLERİ ---
        
        // Düşman bölgesi ise: SADECE KUŞATMA VARSA KIRILABİLİR
        if (siegeManager.isUnderSiege(owner)) {
            // Eğer Ana Kristali (Beacon) kırarsa oyunu bitir
            if (event.getBlock().getType() == Material.BEACON) {
                // Kalkan (Shield) Kontrolü
                if (core != null && core.isShieldActive()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§bKristal Enerji Kalkanı ile korunuyor! Kalkan Gücü: " + core.getShieldFuel());
                    return;
                }
                
                // Kalkan yoksa zafer!
                siegeManager.endSiege(playerClan, owner); // Kazanan: playerClan
                event.setDropItems(false);
                event.getBlock().setType(Material.AIR); // Kristali sil
                event.getPlayer().sendMessage("§6§lZAFER! Düşman kristalini parçaladın.");
            }
            return; // Kuşatma altındayken diğer blokları kırmaya izin ver (Stratejik yıkım)
        }

        // Koruma Aktif (Savaş yoksa dokunamazsın)
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cBu bölge " + owner.getName() + " klanına ait! Önce kuşatma başlatmalısın.");
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onFuelAdd(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.BEACON) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.COAL && event.getItem().getType() != Material.CHARCOAL) return;

        Clan owner = territoryManager.getTerritoryOwner(event.getClickedBlock().getLocation());
        if (owner == null) return;

        Structure core = owner.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.CORE)
                .findFirst().orElse(null);

        if (core == null) return;

        core.addFuel(10);
        event.getPlayer().sendMessage("§aKalkan Yakıtı Eklendi. Seviye: " + core.getShieldFuel());
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
    
    // ========== KLAN KRISTALİ KURMA ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalPlace(PlayerInteractEvent event) {
        // Sadece sağ tıklama ve blok yüzeyine koyma işlemi
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        // Eşya bizim Klan Kristali mi?
        if (!ItemManager.isClanItem(event.getItem(), "CRYSTAL")) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        // Kristal havaya değil yere konur, üstü boş olmalı
        Block placeLocation = clickedBlock.getRelative(BlockFace.UP);
        if (placeLocation.getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        
        Player player = event.getPlayer();
        
        // Oyuncunun zaten klanı var mı?
        if (territoryManager.getClanManager().getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cZaten bir klanın var! Yeni kurmak için eskisini terk etmelisin.");
            event.setCancelled(true);
            return;
        }
        
        // --- ALAN KONTROLÜ (FENCE CHECK) - ASYNC ---
        event.setCancelled(true); // Önce iptal et, async kontrol sonrası devam edeceğiz
        
        // Async flood-fill kontrolü (büyük alanlar için main thread'i kilitlememek için)
        Player finalPlayer = player;
        Block finalPlaceLocation = placeLocation;
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
            me.mami.stratocraft.Main.getInstance(),
            () -> {
                boolean isValid = isSurroundedByClanFences(finalPlaceLocation);
                
                // Main thread'e geri dön
                org.bukkit.Bukkit.getScheduler().runTask(
                    me.mami.stratocraft.Main.getInstance(),
                    () -> {
                        if (!isValid) {
                            finalPlayer.sendMessage("§cKlan Kristali sadece §6Klan Çitleri §cile tamamen çevrelenmiş güvenli bir alana kurulabilir!");
                            return;
                        }
                        
                        // --- KLAN KURULUMU ---
                        continueCrystalPlacement(finalPlayer, finalPlaceLocation);
                    }
                );
            }
        );
        
        return; // Async işlem başladı, buradan çık
    }
    
    /**
     * Kristal yerleştirme işlemini tamamla (main thread'de)
     */
    private void continueCrystalPlacement(Player player, Block placeLocation) {
        // Eşyayı tüket
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Kristal Entity'sini oluştur
        Location crystalLoc = placeLocation.getLocation().add(0.5, 0, 0.5);
        EnderCrystal crystalEntity = (EnderCrystal) crystalLoc.getWorld().spawnEntity(crystalLoc, EntityType.ENDER_CRYSTAL);
        crystalEntity.setShowingBottom(true); // Tabanı görünsün
        crystalEntity.setBeamTarget(null);
        
        // Klanı Sisteme Kaydet
        String clanName = player.getName() + "_Klanı"; // Geçici isim
        Clan newClan = territoryManager.getClanManager().createClan(clanName, player.getUniqueId());
        if (newClan != null) {
            newClan.setCrystalLocation(crystalLoc);
            newClan.setCrystalEntity(crystalEntity);
            newClan.setTerritory(new Territory(newClan.getId(), crystalLoc));
            // hasCrystal otomatik setCrystalLocation ile true olur
            territoryManager.setCacheDirty(); // Cache'i güncelle
            
            player.sendMessage("§a§lTEBRİKLER! §eKlan Kristali aktifleşti ve bölgeni mühürledi.");
            player.getWorld().strikeLightningEffect(crystalLoc); // Görsel şimşek
            player.getWorld().spawnParticle(Particle.TOTEM, crystalLoc, 100, 1, 1, 1, 0.5);
            player.playSound(crystalLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.sendTitle("§6§lKLAN KURULDU", "§e" + clanName, 10, 70, 20);
        }
    }
    
    // Flood Fill Algoritması ile Çit Kontrolü
    private boolean isSurroundedByClanFences(Block center) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(center);
        visited.add(center);
        
        int minArea = 9; // Minimum alan büyüklüğü (3x3 = 9 blok)
        int iterations = 0;
        
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            iterations++;
            
            // Maksimum limit kaldırıldı - istediğin kadar büyük alan olabilir
            
            // 4 Yöne bak (Kuzey, Güney, Doğu, Batı)
            Block[] neighbors = {
                current.getRelative(BlockFace.NORTH),
                current.getRelative(BlockFace.SOUTH),
                current.getRelative(BlockFace.EAST),
                current.getRelative(BlockFace.WEST)
            };
            
            for (Block neighbor : neighbors) {
                if (visited.contains(neighbor)) continue;
                
                // Eğer blok bizim Klan Çiti ise, burası sınırdır
                // NOT: Blok koyulduğunda NBT verisi kaybolur, bu yüzden Material kontrolü yapıyoruz
                // İleride BlockPlaceEvent ile koyulan çitlerin lokasyonunu kaydetmemiz gerekebilir
                if (neighbor.getType() == Material.OAK_FENCE) {
                    // Sınır bulundu, bu yöne devam etme
                    continue;
                }
                
                // Eğer hava değilse ve çit de değilse, engel say
                if (neighbor.getType() != Material.AIR) {
                    // Engel var, sınır kabul et
                    continue;
                }
                
                // Eğer boşluksa ve sınır değilse, kuyruğa ekle
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
        
        // Minimum alan kontrolü: En az 3x3 alan olmalı
        if (visited.size() < minArea) {
            return false;
        }
        
        // Eğer döngü limit aşılmadan bittiyse ve minimum şartları sağlıyorsa, kapalı bir alandır
        return true;
    }
    
    // ========== KLAN KRISTALİ KIRILMA ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalBreak(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        
        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        
        // Bu kristal bir klan kristali mi?
        Clan owner = findClanByCrystal(crystal);
        if (owner == null) return; // Normal end crystal
        
        // Kristal kırılıyor mu? (EnderCrystal'ın sağlığı 1.0, yeterli hasar aldığında kırılır)
        // EnderCrystal'da getHealth() yok ama hasar >= 1.0 olduğunda kırılır
        if (event.getFinalDamage() >= 1.0 && !event.isCancelled()) {
            // Kırılma nedenini kontrol et
            Player breaker = null;
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    breaker = (Player) damager;
                }
            }
            
            // Lider kendi kristalini kırıyor mu?
            if (breaker != null && owner.getRank(breaker.getUniqueId()) == Clan.Rank.LEADER) {
                // Lider klanı bozdu
                territoryManager.getClanManager().disbandClan(owner);
                territoryManager.setCacheDirty(); // Cache'i güncelle
                breaker.sendMessage("§cKlanınız dağıtıldı!");
                crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                return;
            }
            
            // Kuşatma var mı?
            if (siegeManager.isUnderSiege(owner)) {
                // Savaşta kristal kırıldı - klan bozuldu
                Clan attacker = territoryManager.getClanManager().getClanByPlayer(breaker != null ? breaker.getUniqueId() : null);
                if (attacker != null && !attacker.equals(owner)) {
                    siegeManager.endSiege(attacker, owner);
                    if (breaker != null) {
                        breaker.sendMessage("§6§lZAFER! Düşman kristalini parçaladın.");
                    }
                }
                territoryManager.getClanManager().disbandClan(owner);
                territoryManager.setCacheDirty(); // Cache'i güncelle
                crystal.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, crystal.getLocation(), 1);
                return;
            }
            
            // Normal durumda sadece lider kırabilir
            if (breaker != null) {
                Clan playerClan = territoryManager.getClanManager().getClanByPlayer(breaker.getUniqueId());
                if (playerClan == null || !playerClan.equals(owner) || owner.getRank(breaker.getUniqueId()) != Clan.Rank.LEADER) {
                    event.setCancelled(true);
                    breaker.sendMessage("§cKlan Kristalini sadece klan lideri kırabilir!");
                    return;
                }
            } else {
                // Doğal hasar (lava, patlama vb.) - engelle
                event.setCancelled(true);
            }
        }
    }
    
    // Kristal entity'sine göre klanı bul
    private Clan findClanByCrystal(EnderCrystal crystal) {
        for (Clan clan : territoryManager.getClanManager().getAllClans()) {
            if (clan.getCrystalEntity() != null && clan.getCrystalEntity().equals(crystal)) {
                return clan;
            }
        }
        return null;
    }
    
    // ========== KLAN KRISTALİ HAREKET ETTİRME ==========
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onCrystalMove(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        
        // Oyuncu shift'e basıyor mu ve elinde boş mu?
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem != null && handItem.getType() != Material.AIR) return;
        
        // Tıklanan blokun üstünde kristal var mı?
        Block clicked = event.getClickedBlock();
        Location checkLoc = clicked.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);
        
        for (Entity entity : clicked.getWorld().getNearbyEntities(checkLoc, 0.5, 0.5, 0.5)) {
            if (entity instanceof EnderCrystal) {
                EnderCrystal crystal = (EnderCrystal) entity;
                Clan owner = findClanByCrystal(crystal);
                
                if (owner != null && owner.getRank(player.getUniqueId()) == Clan.Rank.LEADER) {
                    // Lider kristali hareket ettirebilir
                    // Yeni lokasyon seç
                    Block newLocation = clicked.getRelative(BlockFace.UP);
                    if (newLocation.getType() == Material.AIR) {
                        // Çit kontrolü
                        if (isSurroundedByClanFences(newLocation)) {
                            // Kristali taşı
                            Location newLoc = newLocation.getLocation().add(0.5, 0, 0.5);
                            crystal.teleport(newLoc);
                            owner.setCrystalLocation(newLoc);
                            owner.setTerritory(new Territory(owner.getId(), newLoc));
                            territoryManager.setCacheDirty(); // Cache'i güncelle
                            player.sendMessage("§aKlan Kristali taşındı!");
                        } else {
                            player.sendMessage("§cYeni konum Klan Çitleri ile çevrili olmalı!");
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}

