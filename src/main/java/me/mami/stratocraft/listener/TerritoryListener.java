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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.Color;

import java.util.*;

public class TerritoryListener implements Listener {
    private final TerritoryManager territoryManager;
    private final SiegeManager siegeManager;
    // Klan kurma için chat input sistemi
    private final Map<UUID, PendingClanCreation> waitingForClanName = new HashMap<>();
    
    // Bekleyen klan oluşturma verisi
    private static class PendingClanCreation {
        final Location crystalLoc;
        final EnderCrystal crystalEntity;
        final Block placeLocation;
        
        PendingClanCreation(Location crystalLoc, EnderCrystal crystalEntity, Block placeLocation) {
            this.crystalLoc = crystalLoc;
            this.crystalEntity = crystalEntity;
            this.placeLocation = placeLocation;
        }
    }

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
     * Admin komutu için klan kurulumu başlat (public metod)
     * ✅ Çitler zaten createClanAdmin'de oluşturuldu, burada sadece chat input bekliyoruz
     */
    public void startAdminClanCreation(Player player, Location crystalLoc, org.bukkit.entity.EnderCrystal crystalEntity, Block placeLocation) {
        // Chat input için beklet
        waitingForClanName.put(player.getUniqueId(), new PendingClanCreation(crystalLoc, crystalEntity, placeLocation));
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§lKLAN KURULUYOR!");
        player.sendMessage("§7Çitler ve kristal oluşturuldu!");
        player.sendMessage("§7Lütfen chat'e klan ismini yazın:");
        player.sendMessage("§7(İptal için 'iptal' yazın)");
        player.sendMessage("§6§l════════════════════════════");
        // ✅ Çitler zaten createClanAdmin'de oluşturuldu, burada sadece chat input bekliyoruz
        // Çitler korunacak çünkü zaten dünyada var
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
        
        // Chat input için beklet
        waitingForClanName.put(player.getUniqueId(), new PendingClanCreation(crystalLoc, crystalEntity, placeLocation));
        player.sendMessage("§6§l════════════════════════════");
        player.sendMessage("§e§lKLAN KURULUYOR!");
        player.sendMessage("§7Lütfen chat'e klan ismini yazın:");
        player.sendMessage("§7(İptal için 'iptal' yazın)");
        player.sendMessage("§6§l════════════════════════════");
    }
    
    /**
     * Chat input ile klan ismi al
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChatInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingClanCreation pending = waitingForClanName.get(player.getUniqueId());
        
        if (pending == null) return; // Bu oyuncu klan kurmuyor
        
        event.setCancelled(true); // Chat mesajını iptal et
        
        String message = event.getMessage().trim();
        
        // İptal kontrolü
        if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
            waitingForClanName.remove(player.getUniqueId());
            pending.crystalEntity.remove(); // Kristali kaldır
            player.sendMessage("§cKlan kurma iptal edildi.");
            return;
        }
        
        // İsim validasyonu
        if (message.length() < 3 || message.length() > 16) {
            player.sendMessage("§cKlan ismi 3-16 karakter arasında olmalı!");
            return;
        }
        
        // Özel karakter kontrolü
        if (!message.matches("^[a-zA-Z0-9_]+$")) {
            player.sendMessage("§cKlan ismi sadece harf, rakam ve alt çizgi içerebilir!");
            return;
        }
        
        // Aynı isimde klan var mı?
        boolean nameExists = territoryManager.getClanManager().getAllClans().stream()
            .anyMatch(c -> c.getName().equalsIgnoreCase(message));
        
        if (nameExists) {
            player.sendMessage("§cBu isimde bir klan zaten var!");
            return;
        }
        
        // Main thread'de klanı oluştur
        org.bukkit.Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            Clan newClan = territoryManager.getClanManager().createClan(message, player.getUniqueId());
            if (newClan != null) {
                newClan.setCrystalLocation(pending.crystalLoc);
                newClan.setCrystalEntity(pending.crystalEntity);
                // Minimum sınır ile Territory oluştur (50 blok radius)
                Territory territory = new Territory(newClan.getId(), pending.crystalLoc);
                // Territory constructor'ında zaten radius = 50, ama emin olmak için:
                if (territory.getRadius() < 50) {
                    territory.expand(50 - territory.getRadius());
                }
                newClan.setTerritory(territory);
                newClan.setHasCrystal(true); // Kristal var
                territoryManager.setCacheDirty(); // Cache'i güncelle
                
                player.sendMessage("§a§lTEBRİKLER! §eKlan Kristali aktifleşti ve bölgeni mühürledi.");
                player.getWorld().strikeLightningEffect(pending.crystalLoc); // Görsel şimşek
                player.getWorld().spawnParticle(Particle.TOTEM, pending.crystalLoc, 100, 1, 1, 1, 0.5);
                player.playSound(pending.crystalLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                player.sendTitle("§6§lKLAN KURULDU", "§e" + message, 10, 70, 20);
            } else {
                player.sendMessage("§cKlan oluşturulamadı! Zaten bir klanın olabilir.");
                pending.crystalEntity.remove(); // Kristali kaldır
            }
            
            waitingForClanName.remove(player.getUniqueId());
        });
    }
    
    // Partikül cooldown (performans için)
    private final Map<UUID, Long> lastBoundaryParticleTime = new HashMap<>();
    private static final long BOUNDARY_PARTICLE_COOLDOWN = 1000L; // 1 saniye
    
    /**
     * Klan sınırlarını partikül ile göster (klan üyelerine)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // PERFORMANS: Sadece blok değiştiyse çalış
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;
        
        // Cooldown kontrolü
        long now = System.currentTimeMillis();
        Long lastTime = lastBoundaryParticleTime.get(player.getUniqueId());
        if (lastTime != null && (now - lastTime) < BOUNDARY_PARTICLE_COOLDOWN) {
            return; // Cooldown'da
        }
        
        // Oyuncunun klanını kontrol et
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return; // Klan üyesi değil
        
        Territory territory = playerClan.getTerritory();
        if (territory == null || territory.getCenter() == null) return;
        
        // Oyuncu kendi klanının sınırına yakın mı? (10 blok mesafe)
        Location center = territory.getCenter();
        double distanceToCenter = center.distance(to);
        double radius = territory.getRadius();
        double distanceToBoundary = Math.abs(distanceToCenter - radius);
        
        // Sınırın 10 blok yakınındaysa partikül göster
        if (distanceToBoundary <= 10) {
            // Sınır çizgisini göster (her 2 blokta bir partikül)
            showTerritoryBoundary(player, territory, to);
            lastBoundaryParticleTime.put(player.getUniqueId(), now);
        }
    }
    
    /**
     * Klan sınırını partikül ile göster
     */
    private void showTerritoryBoundary(Player player, Territory territory, Location playerLoc) {
        Location center = territory.getCenter();
        if (center == null || center.getWorld() == null) return;
        if (!center.getWorld().equals(playerLoc.getWorld())) return;
        
        double radius = territory.getRadius();
        double angle = Math.atan2(playerLoc.getZ() - center.getZ(), playerLoc.getX() - center.getX());
        
        // Sınır çizgisinde birkaç noktada partikül göster
        for (int i = -2; i <= 2; i++) {
            double offsetAngle = angle + (i * 0.3); // Her 0.3 radyan (yaklaşık 17 derece)
            double x = center.getX() + (radius * Math.cos(offsetAngle));
            double z = center.getZ() + (radius * Math.sin(offsetAngle));
            
            // Yükseklik: Oyuncunun göz seviyesi ± 2 blok
            double y = playerLoc.getY() + (i * 0.5);
            
            Location particleLoc = new Location(center.getWorld(), x, y, z);
            
            // Mesafe kontrolü (performans)
            if (playerLoc.distance(particleLoc) > 20) continue;
            
            // Klan rengine göre partikül (yeşil - kendi klanı)
            player.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0f)); // Yeşil
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
            Entity damager = null;
            if (event instanceof EntityDamageByEntityEvent) {
                damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    breaker = (Player) damager;
                }
            }
            
            // Felaket entity'si kristali kırıyor mu? (DisasterTask'tan geliyor)
            // Felaket entity'leri için özel durum - klanı dağıt
            if (damager != null && !(damager instanceof Player)) {
                // Bu bir felaket entity'si olabilir - DisasterTask zaten klanı dağıtacak
                // Burada sadece event'i işle, klan dağıtma DisasterTask'ta yapılıyor
                return; // DisasterTask zaten işleyecek
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
        
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        
        // ÖNCE: Oyuncunun klanı var mı ve kristal var mı kontrol et
        Clan playerClan = territoryManager.getClanManager().getClanByPlayer(player.getUniqueId());
        if (playerClan == null) return; // Oyuncunun klanı yok
        
        // Kristal var mı?
        if (playerClan.getCrystalLocation() == null || playerClan.getCrystalEntity() == null) {
            return; // Kristal yok, devam etme
        }
        
        EnderCrystal crystal = playerClan.getCrystalEntity();
        Location crystalLoc = crystal.getLocation();
        
        // ÖNCE: Oyuncu kristale yakın mı? (5 blok mesafe) - Bu kontrol önce yapılmalı
        double distance = player.getLocation().distance(crystalLoc);
        if (distance > 5) {
            // Kristale yakın değilse hiçbir şey yapma (mesaj gönderme, sadece return)
            return;
        }
        
        // Şimdi shift+sağ tık ve elinde boş mu kontrolü (sadece kristale yakınsa)
        if (!player.isSneaking()) return;
        
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem != null && handItem.getType() != Material.AIR) return;
        
        // Lider kontrolü
        if (playerClan.getRank(player.getUniqueId()) != Clan.Rank.LEADER) {
            return; // Lider değil, devam etme
        }
        
        // Tıklanan bloğun üstünde kristal var mı kontrol et
        Location checkLoc = clicked.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);
        boolean isCrystalAtClicked = (crystalLoc.getBlockX() == checkLoc.getBlockX() && 
                                     crystalLoc.getBlockY() == checkLoc.getBlockY() && 
                                     crystalLoc.getBlockZ() == checkLoc.getBlockZ());
        
        if (!isCrystalAtClicked) {
            // Kristal tıklanan bloğun üstünde değil, yeni konum olarak kullan
            Block newLocation = clicked.getRelative(BlockFace.UP);
            if (newLocation.getType() != Material.AIR) {
                player.sendMessage("§cYeni konum boş olmalı!");
                event.setCancelled(true);
                return;
            }
            
            // Aynı konuma taşıma kontrolü
            Location newLoc = newLocation.getLocation().add(0.5, 0, 0.5);
            if (crystalLoc.getBlockX() == newLoc.getBlockX() && 
                crystalLoc.getBlockY() == newLoc.getBlockY() && 
                crystalLoc.getBlockZ() == newLoc.getBlockZ()) {
                player.sendMessage("§cKristal zaten bu konumda!");
                event.setCancelled(true);
                return;
            }
            
            // Async çit kontrolü (büyük alanlar için lag önleme)
            event.setCancelled(true);
            Block finalNewLocation = newLocation;
            Player finalPlayer = player;
            EnderCrystal finalCrystal = crystal;
            Clan finalOwner = playerClan;
            Location finalNewLoc = newLoc;
            
            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
                me.mami.stratocraft.Main.getInstance(),
                () -> {
                    boolean isValid = isSurroundedByClanFences(finalNewLocation);
                    
                    // Main thread'e geri dön
                    org.bukkit.Bukkit.getScheduler().runTask(
                        me.mami.stratocraft.Main.getInstance(),
                        () -> {
                            if (!isValid) {
                                finalPlayer.sendMessage("§cYeni konum Klan Çitleri ile çevrili olmalı!");
                                return;
                            }
                            
                            // Kristali taşı
                            finalCrystal.teleport(finalNewLoc);
                            finalOwner.setCrystalLocation(finalNewLoc);
                            finalOwner.setCrystalEntity(finalCrystal); // Entity referansını güncelle
                            // Minimum sınır ile Territory oluştur (50 blok radius)
                            Territory territory = new Territory(finalOwner.getId(), finalNewLoc);
                            if (territory.getRadius() < 50) {
                                territory.expand(50 - territory.getRadius());
                            }
                            finalOwner.setTerritory(territory);
                            territoryManager.setCacheDirty(); // Cache'i güncelle
                            
                            // Efektler
                            finalPlayer.getWorld().spawnParticle(Particle.TOTEM, finalNewLoc, 50, 0.5, 0.5, 0.5, 0.1);
                            finalPlayer.playSound(finalNewLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
                            
                            finalPlayer.sendMessage("§a§lKlan Kristali taşındı!");
                        }
                    );
                }
            );
            
            return; // İşlem başladı, çık
        }
    }
}

