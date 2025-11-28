package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Ritüel Tabanlı Tuzak Sistemi
 * 
 * Yapı: Magma Block çerçeve + LODESTONE çekirdek
 * Gizleme: Üstü kapatılmalı
 * Yakıt: Elmas (5), Zümrüt (10), Titanyum (20)
 * Tuzak Tipleri: Cehennem, Şok, Kara Delik, Mayın
 */
public class TrapManager {
    private final Main plugin;
    private final ClanManager clanManager;
    private final Map<Location, TrapData> activeTraps = new HashMap<>();
    private final Map<UUID, me.mami.stratocraft.model.Clan> clanCache = new HashMap<>(); // Performans optimizasyonu
    private File trapsFile;
    private FileConfiguration trapsConfig;
    
    public enum TrapType {
        HELL_TRAP,      // Cehennem Tuzağı (Magma Cream) - 3x3 lava
        SHOCK_TRAP,     // Şok Tuzağı (Lightning Core) - Yıldırım
        BLACK_HOLE,     // Kara Delik (Ender Pearl) - Körlük + Yavaşlık
        MINE,           // Mayın (TNT) - Yüksek hasarlı patlama
        POISON_TRAP     // Zehir Tuzağı (Spider Eye) - Zehir efekti
    }
    
    public static class TrapData {
        private final UUID ownerId;
        private final UUID ownerClanId;
        private final TrapType type;
        private int fuel; // Kalan patlama hakkı
        private final Location coreLocation;
        private final List<Location> frameBlocks; // Magma Block çerçevesi
        private boolean isCovered; // Üstü kapatılmış mı?
        
        public TrapData(UUID ownerId, UUID ownerClanId, TrapType type, int fuel, Location coreLocation) {
            this.ownerId = ownerId;
            this.ownerClanId = ownerClanId;
            this.type = type;
            this.fuel = fuel;
            this.coreLocation = coreLocation;
            this.frameBlocks = new ArrayList<>();
            this.isCovered = false;
        }
        
        public UUID getOwnerId() { return ownerId; }
        public UUID getOwnerClanId() { return ownerClanId; }
        public TrapType getType() { return type; }
        public int getFuel() { return fuel; }
        public void consumeFuel() { if (fuel > 0) fuel--; }
        public void addFuel(int amount) { fuel += amount; }
        public Location getCoreLocation() { return coreLocation; }
        public List<Location> getFrameBlocks() { return frameBlocks; }
        public boolean isCovered() { return isCovered; }
        public void setCovered(boolean covered) { this.isCovered = covered; }
    }
    
    public TrapManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        loadTraps();
    }
    
    /**
     * Tuzak yapısını kontrol et (Magma Block çerçeve + TRAP_CORE çekirdek)
     * Esnek boyut desteği: 3x3, 3x6, 5x5, vb. (End Portal mantığı)
     */
    public boolean isTrapStructure(Block centerBlock) {
        // Tuzak çekirdeği kontrolü (LODESTONE + TRAP_CORE metadata)
        if (centerBlock.getType() != Material.LODESTONE) return false;
        if (!centerBlock.hasMetadata("TrapCoreItem")) return false;
        
        // Esnek boyut kontrolü - End Portal mantığı
        // Çerçeve boyutunu tespit et (3x3, 3x6, 5x5, vb.)
        int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
        boolean foundFrameX = false;
        boolean foundFrameZ = false;
        
        // X ekseninde çerçeve genişliğini bul
        for (int x = -6; x <= 6; x++) {
            Block block = centerBlock.getRelative(x, 0, 0);
            if (block.getType() == Material.MAGMA_BLOCK) {
                if (!foundFrameX) {
                    minX = x;
                    foundFrameX = true;
                }
                maxX = x;
            }
        }
        
        // Z ekseninde çerçeve genişliğini bul
        for (int z = -6; z <= 6; z++) {
            Block block = centerBlock.getRelative(0, 0, z);
            if (block.getType() == Material.MAGMA_BLOCK) {
                if (!foundFrameZ) {
                    minZ = z;
                    foundFrameZ = true;
                }
                maxZ = z;
            }
        }
        
        // Çerçeve bulunamadıysa geçersiz
        if (!foundFrameX || !foundFrameZ) return false;
        
        // Çerçeve boyutları geçerli mi? (En az 3x3 olmalı)
        int widthX = maxX - minX + 1;
        int widthZ = maxZ - minZ + 1;
        
        if (widthX < 3 || widthZ < 3) return false;
        
        // Çerçevenin tamamı Magma Block mu kontrol et
        int magmaCount = 0;
        int expectedCount = (widthX * widthZ) - 1; // Merkez hariç
        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == 0 && z == 0) continue; // Merkez hariç
                Block block = centerBlock.getRelative(x, 0, z);
                if (block.getType() == Material.MAGMA_BLOCK) {
                    magmaCount++;
                }
            }
        }
        
        // Çerçevenin en az %80'i Magma Block olmalı (esnek kontrol)
        return magmaCount >= (expectedCount * 0.8);
    }
    
    /**
     * Tuzak çerçeve boyutlarını al (genişlik x uzunluk)
     */
    public int[] getTrapDimensions(Block centerBlock) {
        int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
        boolean foundFrameX = false;
        boolean foundFrameZ = false;
        
        for (int x = -6; x <= 6; x++) {
            Block block = centerBlock.getRelative(x, 0, 0);
            if (block.getType() == Material.MAGMA_BLOCK) {
                if (!foundFrameX) {
                    minX = x;
                    foundFrameX = true;
                }
                maxX = x;
            }
        }
        
        for (int z = -6; z <= 6; z++) {
            Block block = centerBlock.getRelative(0, 0, z);
            if (block.getType() == Material.MAGMA_BLOCK) {
                if (!foundFrameZ) {
                    minZ = z;
                    foundFrameZ = true;
                }
                maxZ = z;
            }
        }
        
        if (!foundFrameX || !foundFrameZ) {
            return new int[]{0, 0};
        }
        
        return new int[]{maxX - minX + 1, maxZ - minZ + 1};
    }
    
    /**
     * Tuzak oluştur
     */
    public boolean createTrap(Player player, Block coreBlock, TrapType type, Material fuelMaterial) {
        if (!isTrapStructure(coreBlock)) return false;
        
        // Yakıt miktarını belirle
        int fuel = 0;
        if (fuelMaterial == Material.DIAMOND) {
            fuel = 5;
        } else if (fuelMaterial == Material.EMERALD) {
            fuel = 10;
        } else if (ItemManager.isCustomItem(new org.bukkit.inventory.ItemStack(fuelMaterial), "TITANIUM_INGOT")) {
            fuel = 20;
        } else {
            return false;
        }
        
        UUID ownerId = player.getUniqueId();
        UUID clanId = null;
        if (clanManager.getClanByPlayer(ownerId) != null) {
            clanId = clanManager.getClanByPlayer(ownerId).getId();
        }
        
        TrapData trap = new TrapData(ownerId, clanId, type, fuel, coreBlock.getLocation());
        
        // Çerçeve boyutlarını al
        int[] dimensions = getTrapDimensions(coreBlock);
        int widthX = dimensions[0];
        int widthZ = dimensions[1];
        
        // Çerçeve bloklarını kaydet (esnek boyut)
        int minX = -(widthX / 2);
        int maxX = widthX / 2;
        int minZ = -(widthZ / 2);
        int maxZ = widthZ / 2;
        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == 0 && z == 0) continue; // Merkez hariç
                Block block = coreBlock.getRelative(x, 0, z);
                if (block.getType() == Material.MAGMA_BLOCK) {
                    trap.getFrameBlocks().add(block.getLocation());
                }
            }
        }
        
        // Metadata ile işaretle
        coreBlock.setMetadata("TrapCore", new FixedMetadataValue(plugin, true));
        coreBlock.setMetadata("TrapOwner", new FixedMetadataValue(plugin, ownerId.toString()));
        
        activeTraps.put(coreBlock.getLocation(), trap);
        saveTraps();
        
        player.sendMessage("§a§lTUZAK OLUŞTURULDU!");
        player.sendMessage("§7Yakıt: §e" + fuel + " patlama hakkı");
        player.sendMessage("§7Önemli: Üstünü kapatmadan aktifleşmez!");
        player.sendMessage("§7Aktifleştirme: Üstteki bloklara Shift + Sağ Tık yap (yakıt elinde olmalı)!");
        
        return true;
    }
    
    /**
     * Tuzak aktifleştirme (Shift + Sağ Tık ile üstteki kapatma bloklarına tıklama)
     */
    public boolean activateTrap(Player player, Location trapCore, ItemStack fuelItem) {
        // Null kontrolleri
        if (player == null || trapCore == null || fuelItem == null) {
            return false;
        }
        
        TrapData trap = activeTraps.get(trapCore);
        if (trap == null) {
            player.sendMessage("§cBu tuzak bulunamadı!");
            return false;
        }
        
        // Sahiplik kontrolü
        if (!trap.getOwnerId().equals(player.getUniqueId())) {
            // Klan kontrolü
            if (trap.getOwnerClanId() == null || 
                clanManager.getClanByPlayer(player.getUniqueId()) == null ||
                !clanManager.getClanByPlayer(player.getUniqueId()).getId().equals(trap.getOwnerClanId())) {
                player.sendMessage("§cBu tuzak sana ait değil!");
                return false;
            }
        }
        
        // Üstü kapatılmış mı kontrol et
        checkTrapCoverage(trapCore);
        if (!trap.isCovered()) {
            player.sendMessage("§cTuzak üstü tamamen kapatılmamış! Tüm çerçeve bloklarının üstü kapatılmalı.");
            return false;
        }
        
        // Yakıt kontrolü
        if (fuelItem == null || fuelItem.getType() == Material.AIR) {
            player.sendMessage("§cElinde yakıt olmalı!");
            return false;
        }
        
        Material fuelMaterial = fuelItem.getType();
        int fuelToAdd = 0;
        
        // Elmas, Zümrüt, Titanyum
        if (fuelMaterial == Material.DIAMOND) {
            fuelToAdd = 5;
        } else if (fuelMaterial == Material.EMERALD) {
            fuelToAdd = 10;
        } else if (ItemManager.isCustomItem(fuelItem, "TITANIUM_INGOT")) {
            fuelToAdd = 20;
        } else if (fuelMaterial == Material.IRON_INGOT) {
            fuelToAdd = 3; // Demir için düşük yakıt
        } else if (ItemManager.isCustomItem(fuelItem, "SULFUR")) {
            fuelToAdd = 2;
        } else if (ItemManager.isCustomItem(fuelItem, "BAUXITE_INGOT")) {
            fuelToAdd = 4;
        } else if (ItemManager.isCustomItem(fuelItem, "ROCK_SALT")) {
            fuelToAdd = 2;
        } else if (ItemManager.isCustomItem(fuelItem, "MITHRIL_INGOT")) {
            fuelToAdd = 15;
        } else if (ItemManager.isCustomItem(fuelItem, "ASTRAL_CRYSTAL")) {
            fuelToAdd = 25;
        } else {
            player.sendMessage("§cGeçersiz yakıt! Elmas, Zümrüt, Titanyum, Demir veya diğer madenler kullan.");
            return false;
        }
        
        // Yakıt ekle
        trap.addFuel(fuelToAdd);
        
        // Yakıt item'ını tüket (miktar kontrolü ile)
        int currentAmount = fuelItem.getAmount();
        if (currentAmount > 0) {
            fuelItem.setAmount(currentAmount - 1);
        }
        
        saveTraps();
        
        // Bildirim (sadece sahip ve klan üyelerine)
        sendTrapActivationNotification(trap, player);
        
        // Kırmızı partiküller (sadece sahip ve klan üyelerine görünür)
        showTrapActivationParticles(trap);
        
        return true;
    }
    
    /**
     * Tuzak aktifleştirme bildirimi (sadece sahip ve klan üyelerine)
     */
    private void sendTrapActivationNotification(TrapData trap, Player activator) {
        String message = "§a§lTUZAK AKTİFLEŞTİRİLDİ!";
        String fuelMessage = "§7Yakıt: §e" + trap.getFuel() + " patlama hakkı";
        
        // Sahibe bildir
        Player owner = plugin.getServer().getPlayer(trap.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(message);
            owner.sendMessage(fuelMessage);
        }
        
        // Aktifleştiren kişiye bildir (sahip değilse)
        if (!activator.getUniqueId().equals(trap.getOwnerId())) {
            activator.sendMessage(message);
            activator.sendMessage(fuelMessage);
        }
        
        // Klan üyelerine bildir
        if (trap.getOwnerClanId() != null) {
            me.mami.stratocraft.model.Clan clan = getClanById(trap.getOwnerClanId());
            if (clan != null) {
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline() && 
                        !member.getUniqueId().equals(trap.getOwnerId()) &&
                        !member.getUniqueId().equals(activator.getUniqueId())) {
                        member.sendMessage(message);
                        member.sendMessage(fuelMessage);
                    }
                }
            }
        }
    }
    
    /**
     * Tuzak aktifleştirme partikülleri (sadece sahip ve klan üyelerine görünür)
     */
    private void showTrapActivationParticles(TrapData trap) {
        Location coreLoc = trap.getCoreLocation();
        
        // Tüm çerçeve bloklarının üstünde kırmızı partikül göster
        for (Location frameLoc : trap.getFrameBlocks()) {
            Location particleLoc = frameLoc.clone().add(0.5, 1.5, 0.5);
            
            // Sahibe göster
            Player owner = plugin.getServer().getPlayer(trap.getOwnerId());
            if (owner != null && owner.isOnline()) {
                owner.spawnParticle(org.bukkit.Particle.REDSTONE, particleLoc, 5, 
                    0.3, 0.3, 0.3, 0, 
                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            }
            
            // Klan üyelerine göster
            if (trap.getOwnerClanId() != null) {
                me.mami.stratocraft.model.Clan clan = getClanById(trap.getOwnerClanId());
                if (clan != null) {
                    for (UUID memberId : clan.getMembers().keySet()) {
                        Player member = plugin.getServer().getPlayer(memberId);
                        if (member != null && member.isOnline() && 
                            !member.getUniqueId().equals(trap.getOwnerId())) {
                            member.spawnParticle(org.bukkit.Particle.REDSTONE, particleLoc, 5, 
                                0.3, 0.3, 0.3, 0, 
                                new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
                        }
                    }
                }
            }
        }
        
        // Çekirdek bloğunun üstünde de partikül göster
        Location coreParticleLoc = coreLoc.clone().add(0.5, 1.5, 0.5);
        
        Player owner = plugin.getServer().getPlayer(trap.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.spawnParticle(org.bukkit.Particle.REDSTONE, coreParticleLoc, 10, 
                0.5, 0.5, 0.5, 0, 
                new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
        }
        
        if (trap.getOwnerClanId() != null) {
            me.mami.stratocraft.model.Clan clan = getClanById(trap.getOwnerClanId());
            if (clan != null) {
                for (UUID memberId : clan.getMembers().keySet()) {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline() && 
                        !member.getUniqueId().equals(trap.getOwnerId())) {
                        member.spawnParticle(org.bukkit.Particle.REDSTONE, coreParticleLoc, 10, 
                            0.5, 0.5, 0.5, 0, 
                            new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.5f));
                    }
                }
            }
        }
    }
    
    /**
     * Clan ID'ye göre Clan bul (Performans optimizasyonu - Cache kullan)
     */
    private me.mami.stratocraft.model.Clan getClanById(UUID clanId) {
        if (clanId == null) return null;
        
        // Cache'den kontrol et
        if (clanCache.containsKey(clanId)) {
            me.mami.stratocraft.model.Clan cached = clanCache.get(clanId);
            if (cached != null) {
                // Cache'deki klan hala geçerli mi kontrol et
                // Eğer klan üyelerinden biri hala bu klana aitse, cache geçerli
                if (!cached.getMembers().isEmpty()) {
                    UUID firstMember = cached.getMembers().keySet().iterator().next();
                    me.mami.stratocraft.model.Clan current = clanManager.getClanByPlayer(firstMember);
                    if (current != null && current.getId().equals(clanId)) {
                        return cached;
                    }
                }
                // Geçersiz cache, temizle
                clanCache.remove(clanId);
            }
        }
        
        // Cache'de yoksa, tüm klanlardan bul (sadece ilk sefer)
        for (me.mami.stratocraft.model.Clan clan : clanManager.getAllClans()) {
            if (clan.getId().equals(clanId)) {
                // Cache'e ekle
                clanCache.put(clanId, clan);
                return clan;
            }
        }
        return null;
    }
    
    /**
     * Tuzak üstünün kapatılıp kapatılmadığını kontrol et
     */
    public void checkTrapCoverage(Location coreLocation) {
        TrapData trap = activeTraps.get(coreLocation);
        if (trap == null) return;
        
        Block coreBlock = coreLocation.getBlock();
        if (coreBlock.getType() != Material.LODESTONE) return;
        
        // Üstteki blok kontrolü
        Block above = coreBlock.getRelative(0, 1, 0);
        boolean covered = above.getType() != Material.AIR && 
                         above.getType() != Material.CAVE_AIR &&
                         above.getType() != Material.VOID_AIR;
        
        // Çerçeve bloklarının üstünü de kontrol et (tüm çerçeve kapatılmalı)
        for (Location frameLoc : trap.getFrameBlocks()) {
            Block frameBlock = frameLoc.getBlock();
            if (frameBlock.getType() != Material.MAGMA_BLOCK) continue; // Çerçeve bozulmuş olabilir
            
            Block frameAbove = frameBlock.getRelative(0, 1, 0);
            if (frameAbove.getType() == Material.AIR || 
                frameAbove.getType() == Material.CAVE_AIR ||
                frameAbove.getType() == Material.VOID_AIR) {
                covered = false;
                break;
            }
        }
        
        trap.setCovered(covered);
    }
    
    /**
     * Tuzak tetikleme (PlayerMoveEvent'ten çağrılır)
     */
    public void triggerTrap(Location triggerLocation, Player victim) {
        // Trigger bloğunun altında tuzak var mı?
        Block below = triggerLocation.getBlock().getRelative(0, -1, 0);
        Block below2 = below.getRelative(0, -1, 0);
        
        TrapData trap = null;
        Location trapCore = null;
        
        // 1 blok altında kontrol
        if (below.hasMetadata("TrapCore")) {
            trapCore = below.getLocation();
            trap = activeTraps.get(trapCore);
        }
        // 2 blok altında kontrol
        else if (below2.hasMetadata("TrapCore")) {
            trapCore = below2.getLocation();
            trap = activeTraps.get(trapCore);
        }
        
        if (trap == null || !trap.isCovered()) return;
        
        // GİZLEME KONTROLÜ: Tuzağın üstü açıksa (Hava ise) çalışma
        Block trapBlock = trapCore.getBlock();
        Block coverBlock = trapBlock.getRelative(0, 1, 0); // Tuzağın üstündeki blok
        
        // Eğer tuzağın üstü açıksa (Hava ise) veya Yarım Blok vb. değilse çalışma
        // Yani oyuncu tuzağı gizlememiş
        if (coverBlock.getType() == Material.AIR || 
            coverBlock.getType() == Material.CAVE_AIR ||
            coverBlock.getType() == Material.VOID_AIR) {
            return; // Tuzak gizlenmemiş, çalışma
        }
        
        // Klan kontrolü - Dostlar korunur
        if (trap.getOwnerClanId() != null && victim != null) {
            if (clanManager.getClanByPlayer(victim.getUniqueId()) != null) {
                UUID victimClanId = clanManager.getClanByPlayer(victim.getUniqueId()).getId();
                if (victimClanId.equals(trap.getOwnerClanId())) {
                    return; // Dost, tuzak tetiklenmez
                }
            }
        }
        
        // Yakıt kontrolü
        if (trap.getFuel() <= 0) {
            removeTrap(trapCore);
            return;
        }
        
        // Tuzak tetiklenmeden önce CLICK sesi (0.5 saniye önce korku efekti)
        victim.playSound(triggerLocation, org.bukkit.Sound.BLOCK_TRIPWIRE_CLICK_ON, 1.0f, 1.0f);
        
        // Tuzak tipine göre etki
        executeTrap(trap, triggerLocation, victim);
        
        // Yakıt tüket
        trap.consumeFuel();
        if (trap.getFuel() <= 0) {
            removeTrap(trapCore);
        } else {
            saveTraps();
        }
    }
    
    /**
     * Tuzak etkisini uygula
     */
    private void executeTrap(TrapData trap, Location triggerLoc, Player victim) {
        switch (trap.getType()) {
            case HELL_TRAP:
                // 3x3 alanı lava yap
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = triggerLoc.clone().add(x, 0, z).getBlock();
                        if (block.getType() == Material.AIR || block.getType().isSolid()) {
                            block.setType(Material.LAVA);
                        }
                    }
                }
                if (victim != null) {
                    victim.sendMessage("§c§lCEHENNEM TUZAĞINA YAKALANDIN!");
                }
                break;
                
            case SHOCK_TRAP:
                // Yıldırım çarptır
                triggerLoc.getWorld().strikeLightning(triggerLoc);
                if (victim != null) {
                    victim.sendMessage("§e§lŞOK TUZAĞINA YAKALANDIN!");
                }
                break;
                
            case BLACK_HOLE:
                // Körlük ve Yavaşlık ver
                if (victim != null) {
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.BLINDNESS, 100, 2, false, false));
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 3, false, false));
                    victim.sendMessage("§5§lKARA DELİK TUZAĞINA YAKALANDIN!");
                }
                break;
                
            case MINE:
                // Yüksek hasarlı patlama (blok kırmaz)
                triggerLoc.getWorld().createExplosion(triggerLoc, 5.0f, false, false);
                if (victim != null) {
                    victim.sendMessage("§c§lMAYINA BASDIN!");
                }
                break;
                
            case POISON_TRAP:
                // Zehir efekti (10 blok yarıçap)
                if (victim != null) {
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.POISON, 100, 1, false, false));
                    victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0, false, false));
                    victim.sendMessage("§2§lZEHİR TUZAĞINA YAKALANDIN!");
                    
                    // Etrafındaki diğer oyunculara da zehir ver
                    for (org.bukkit.entity.Entity nearby : triggerLoc.getWorld()
                            .getNearbyEntities(triggerLoc, 10, 10, 10)) {
                        if (nearby instanceof Player && nearby != victim) {
                            Player nearbyPlayer = (Player) nearby;
                            // Klan kontrolü
                            if (trap.getOwnerClanId() != null) {
                                if (clanManager.getClanByPlayer(nearbyPlayer.getUniqueId()) != null) {
                                    UUID nearbyClanId = clanManager.getClanByPlayer(nearbyPlayer.getUniqueId()).getId();
                                    if (nearbyClanId.equals(trap.getOwnerClanId())) {
                                        continue; // Dost, zehir verilmez
                                    }
                                }
                            }
                            nearbyPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.POISON, 80, 0, false, false));
                        }
                    }
                }
                break;
        }
    }
    
    /**
     * Tuzak kaldır
     */
    public void removeTrap(Location coreLocation) {
        TrapData trap = activeTraps.remove(coreLocation);
        if (trap != null) {
            Block coreBlock = coreLocation.getBlock();
            coreBlock.removeMetadata("TrapCore", plugin);
            coreBlock.removeMetadata("TrapOwner", plugin);
            saveTraps();
        }
    }
    
    /**
     * Tuzakları kaydet (public - Main.java'dan çağrılabilir)
     */
    public void saveTraps() {
        if (trapsConfig == null) return;
        
        trapsConfig.set("traps", null);
        for (Map.Entry<Location, TrapData> entry : activeTraps.entrySet()) {
            Location loc = entry.getKey();
            TrapData trap = entry.getValue();
            
            String path = "traps." + loc.getWorld().getName() + "." + 
                         loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            
            trapsConfig.set(path + ".owner", trap.getOwnerId().toString());
            trapsConfig.set(path + ".clan", trap.getOwnerClanId() != null ? trap.getOwnerClanId().toString() : null);
            trapsConfig.set(path + ".type", trap.getType().name());
            trapsConfig.set(path + ".fuel", trap.getFuel());
        }
        
        try {
            trapsConfig.save(trapsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Tuzaklar kaydedilemedi: " + e.getMessage());
        }
    }
    
    /**
     * Tuzakları yükle
     */
    private void loadTraps() {
        trapsFile = new File(plugin.getDataFolder(), "traps.yml");
        if (!trapsFile.exists()) {
            try {
                trapsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("traps.yml oluşturulamadı: " + e.getMessage());
            }
        }
        
        trapsConfig = YamlConfiguration.loadConfiguration(trapsFile);
        
        if (trapsConfig.getConfigurationSection("traps") == null) return;
        
        for (String worldName : trapsConfig.getConfigurationSection("traps").getKeys(false)) {
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) continue;
            
            for (String locStr : trapsConfig.getConfigurationSection("traps." + worldName).getKeys(false)) {
                String[] coords = locStr.split(",");
                if (coords.length != 3) continue;
                
                try {
                    Location loc = new Location(world, 
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]));
                    
                    UUID ownerId = UUID.fromString(trapsConfig.getString("traps." + worldName + "." + locStr + ".owner"));
                    String clanStr = trapsConfig.getString("traps." + worldName + "." + locStr + ".clan");
                    UUID clanId = clanStr != null ? UUID.fromString(clanStr) : null;
                    TrapType type = TrapType.valueOf(trapsConfig.getString("traps." + worldName + "." + locStr + ".type"));
                    int fuel = trapsConfig.getInt("traps." + worldName + "." + locStr + ".fuel");
                    
                    TrapData trap = new TrapData(ownerId, clanId, type, fuel, loc);
                    activeTraps.put(loc, trap);
                    
                    // Metadata'yı geri yükle
                    Block block = loc.getBlock();
                    if (block.getType() == Material.LODESTONE) {
                        block.setMetadata("TrapCore", new FixedMetadataValue(plugin, true));
                        block.setMetadata("TrapOwner", new FixedMetadataValue(plugin, ownerId.toString()));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Tuzak yüklenemedi: " + locStr + " - " + e.getMessage());
                }
            }
        }
    }
    
    public Map<Location, TrapData> getActiveTraps() {
        return new HashMap<>(activeTraps);
    }
}

