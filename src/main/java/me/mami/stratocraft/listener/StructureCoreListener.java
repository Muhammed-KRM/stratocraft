package me.mami.stratocraft.listener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.StructureActivationItemManager;
import me.mami.stratocraft.manager.StructureCoreManager;
import me.mami.stratocraft.manager.StructureRecipeManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;

/**
 * Yapı Çekirdeği Listener
 * 
 * Sorumluluklar:
 * - Yapı çekirdeği yerleştirme
 * - Yapı aktivasyonu (item ile)
 * - Yapı menü açma (aktif yapılara sağ tık)
 * 
 * Thread-Safe: Cooldown için ConcurrentHashMap
 */
public class StructureCoreListener implements Listener {
    
    private final Main plugin;
    private final StructureCoreManager coreManager;
    private final StructureRecipeManager recipeManager;
    private final StructureActivationItemManager activationItemManager;
    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;
    
    // Cooldown: Player UUID -> Son aktivasyon zamanı
    private final ConcurrentHashMap<UUID, Long> activationCooldowns = new ConcurrentHashMap<>();
    private static final long ACTIVATION_COOLDOWN = 2000L; // 2 saniye
    
    public StructureCoreListener(Main plugin, 
                                StructureCoreManager coreManager,
                                StructureRecipeManager recipeManager,
                                StructureActivationItemManager activationItemManager,
                                ClanManager clanManager,
                                TerritoryManager territoryManager) {
        this.plugin = plugin;
        this.coreManager = coreManager;
        this.recipeManager = recipeManager;
        this.activationItemManager = activationItemManager;
        this.clanManager = clanManager;
        this.territoryManager = territoryManager;
    }
    
    /**
     * Yapı çekirdeği yerleştirme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureCorePlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!ItemManager.isCustomItem(item, "STRUCTURE_CORE")) {
            return;
        }
        
        Block placed = event.getBlockPlaced();
        Location coreLoc = placed.getLocation();
        Player player = event.getPlayer();
        
        // Yapı çekirdeği olarak işaretle (memory'de tut)
        // ✅ KRİTİK: PDC'ye yazma işlemi onStructureCorePlaceRestore'da yapılacak (MONITOR priority)
        coreManager.addInactiveCore(coreLoc, player.getUniqueId());
        
        // Mesaj
        player.sendMessage("§a§l✓ Yapı çekirdeği yerleştirildi!");
        player.sendMessage("§7Etrafına yapıyı kur ve aktivasyon item'ı ile aktifleştir.");
        
        // Efekt
        player.playSound(coreLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        Location particleLoc = coreLoc.clone().add(0.5, 0.5, 0.5);
        coreLoc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 20, 0.3, 0.3, 0.3, 0.1);
    }
    
    /**
     * Yapı aktivasyonu ve menü açma
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureCoreInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        
        Location coreLoc = clicked.getLocation();
        
        // Yapı çekirdeği kontrolü
        if (!coreManager.isStructureCore(clicked)) {
            return;
        }
        
        // Aktif yapı kontrolü (menü açma)
        Structure activeStructure = coreManager.getActiveStructure(coreLoc);
        if (activeStructure != null) {
            // Aktif yapıya sağ tık → Menü aç (StructureMenuListener'da işlenecek)
            // Burada sadece event'i iptal et
            event.setCancelled(true);
            return;
        }
        
        // Inaktif çekirdek → Aktivasyon işlemi
        if (!coreManager.isInactiveCore(coreLoc)) {
            return;
        }
        
        event.setCancelled(true);
        
        // Cooldown kontrolü
        if (isOnCooldown(player.getUniqueId())) {
            player.sendMessage("§cYapı aktivasyonu için beklemen gerekiyor!");
            return;
        }
        
        // Aktivasyon item'ı kontrolü
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§cAktivasyon item'ı elinde olmalı!");
            player.sendMessage("§7Yapı tipine göre aktivasyon item'ları:");
            player.sendMessage("§e• §7Kişisel Görev Loncası: §fDemir Külçe (IRON_INGOT)");
            player.sendMessage("§e• §7Klan Görev Loncası: §fZümrüt (EMERALD)");
            player.sendMessage("§e• §7Klan Bankası: §fAltın Külçe (GOLD_INGOT)");
            player.sendMessage("§e• §7Klan Yönetim Merkezi: §fNether Yıldızı (NETHER_STAR)");
            player.sendMessage("§e• §7Sözleşme Ofisi: §fElmas (DIAMOND)");
            player.sendMessage("§e• §7Pazar Yeri: §fKömür (COAL)");
            player.sendMessage("§e• §7Tarif Kütüphanesi: §fKitap (BOOK)");
            player.sendMessage("§e• §7Eğitim Alanı: §fDemir Kılıç (IRON_SWORD)");
            player.sendMessage("§e• §7Kervan İstasyonu: §fSandık (CHEST)");
            player.sendMessage("§e• §7Simya Kulesi: §fTitanyum Külçe (özel item)");
            return;
        }
        
        // Hangi yapı tipi için bu item kullanılabilir? (YENİ: StructureType)
        me.mami.stratocraft.enums.StructureType targetType = activationItemManager.getStructureTypeForItem(handItem);
        if (targetType == null) {
            player.sendMessage("§cBu item ile yapı aktifleştirilemez!");
            player.sendMessage("§7Elindeki item: §e" + handItem.getType().name());
            player.sendMessage("§7Geçerli aktivasyon item'ları:");
            player.sendMessage("§e• §fDemir Külçe §7→ Kişisel Görev Loncası");
            player.sendMessage("§e• §fZümrüt §7→ Klan Görev Loncası");
            player.sendMessage("§e• §fAltın Külçe §7→ Klan Bankası");
            player.sendMessage("§e• §fNether Yıldızı §7→ Klan Yönetim Merkezi");
            player.sendMessage("§e• §fElmas §7→ Sözleşme Ofisi");
            player.sendMessage("§e• §fKömür §7→ Pazar Yeri");
            player.sendMessage("§e• §fKitap §7→ Tarif Kütüphanesi");
            player.sendMessage("§e• §fDemir Kılıç §7→ Eğitim Alanı");
            player.sendMessage("§e• §fSandık §7→ Kervan İstasyonu");
            return;
        }
        
        // Tarif kontrolü (async - performanslı)
        player.sendMessage("§7Yapı kontrol ediliyor...");
        player.playSound(coreLoc, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
        
        recipeManager.validateStructureAsync(coreLoc, targetType, (isValid) -> {
            // Main thread'de devam et
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!isValid) {
                    // Hata mesajı
                    player.sendMessage("§c§l✗ Yapı tarife uymuyor!");
                    player.sendMessage("§7Lütfen yapıyı doğru şekilde kurun.");
                    player.sendMessage("§7Tarif: §e" + recipeManager.getRecipeInfo(targetType));
                    player.playSound(coreLoc, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    Location errorParticleLoc = coreLoc.clone().add(0.5, 1, 0.5);
                    coreLoc.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, 
                        errorParticleLoc, 20, 0.5, 0.5, 0.5, 0.1);
                    return;
                }
                
                // Aktivasyon başarılı
                activateStructure(player, coreLoc, targetType, handItem);
            });
        });
        
        setCooldown(player.getUniqueId());
    }
    
    /**
     * Yapıyı aktifleştir (YENİ: StructureType)
     */
    private void activateStructure(Player player, Location coreLoc, me.mami.stratocraft.enums.StructureType type, ItemStack activationItem) {
        // Klan kontrolü (kişisel yapılar hariç)
        Clan clan = null;
        boolean isPersonalStructure = isPersonalStructure(type);
        
        if (!isPersonalStructure) {
            clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan == null) {
                player.sendMessage("§cKlan yapıları için bir klana üye olmanız gerekiyor!");
                return;
            }
            
            // ✅ DÜZELTME: Klan bölgesinde mi? (kristal kontrolü)
            Clan owner = territoryManager.getTerritoryOwner(coreLoc);
            if (owner == null) {
                player.sendMessage("§cBu yapıyı sadece klan alanında kurabilirsiniz!");
                player.sendMessage("§7Klan alanı olmayan yere yapı kurulamaz!");
                return;
            }
            if (!owner.equals(clan)) {
                player.sendMessage("§cBu yapıyı sadece kendi klan bölgenizde kurabilirsiniz!");
                return;
            }
            
            // ✅ DÜZELTME: Klan kristali var mı kontrol et (Territory center kontrolü)
            if (clan.getTerritory() == null || clan.getTerritory().getCenter() == null) {
                player.sendMessage("§cKlan kristali bulunamadı! Yapı aktif olamaz.");
                player.sendMessage("§7Klan alanı olmayan yere yapı kurulamaz!");
                return;
            }
        } else {
            // Kişisel yapılar için en yakın klanı bul (varsa)
            clan = territoryManager.getTerritoryOwner(coreLoc);
            if (clan == null) {
                clan = clanManager.getClanByPlayer(player.getUniqueId());
            }
        }
        
        // Yapı oluştur (GERİYE UYUMLULUK: Eski Structure kullan)
        // TODO: İleride BaseStructure/ClanStructure kullanılacak
        Structure.Type legacyType;
        try {
            legacyType = Structure.Type.valueOf(type.name());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cYapı tipi geçersiz!");
            return;
        }
        // YENİ: OwnerId belirle (CLAN_OWNED yapılar için)
        UUID ownerId = null;
        if (isPersonalStructure(type)) {
            // Kişisel yapılar için oyuncu UUID'si ownerId olarak kaydedilir
            ownerId = player.getUniqueId();
        } else {
            // Klan yapıları için ownerId null (klan sahipliği yeterli)
            // İleride CLAN_OWNED tipi yapılar eklenirse burada set edilebilir
        }
        
        Structure structure = new Structure(legacyType, coreLoc, 1, ownerId);
        
        // Klan varsa ekle
        if (clan != null) {
            clan.addStructure(structure);
        }
        
        // Çekirdeği aktif yapıya dönüştür
        coreManager.activateCore(coreLoc, structure);
        
        // Aktivasyon item'ını tüket
        if (activationItem.getAmount() > 1) {
            activationItem.setAmount(activationItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Başarı mesajı ve efektler
        String structureName = getStructureDisplayName(type);
        player.sendMessage("§a§l✓ " + structureName + " başarıyla aktif edildi!");
        player.sendMessage("§7Yapıya sağ tıklayarak menüyü açabilirsiniz.");
        
        // Ses efektleri ve partikül efektleri
        Location effectLoc = coreLoc.clone().add(0.5, 1, 0.5);
        player.playSound(effectLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        player.playSound(effectLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
        player.playSound(effectLoc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        
        // Partikül efektleri
        coreLoc.getWorld().spawnParticle(Particle.END_ROD, effectLoc, 100, 1.5, 1.5, 1.5, 0.1);
        coreLoc.getWorld().spawnParticle(Particle.TOTEM, effectLoc, 50, 1.0, 1.0, 1.0, 0.05);
        coreLoc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, effectLoc, 30, 1.0, 1.0, 1.0, 0.1);
        coreLoc.getWorld().spawnParticle(Particle.TOTEM, effectLoc, 20, 0.5, 0.5, 0.5, 0.1);
        
        // Başlık mesajı
        player.sendTitle("§a§l✓ BAŞARILI", "§7" + structureName + " aktif edildi!", 10, 40, 10);
    }
    
    /**
     * Kişisel yapı mı kontrol et (YENİ: StructureType)
     */
    private boolean isPersonalStructure(me.mami.stratocraft.enums.StructureType type) {
        if (type == null) return false;
        
        return type == me.mami.stratocraft.enums.StructureType.PERSONAL_MISSION_GUILD ||
               type == me.mami.stratocraft.enums.StructureType.CONTRACT_OFFICE ||
               type == me.mami.stratocraft.enums.StructureType.MARKET_PLACE ||
               type == me.mami.stratocraft.enums.StructureType.RECIPE_LIBRARY;
    }
    
    /**
     * Yapı görünen adını al (YENİ: StructureType - StructureHelper kullan)
     */
    private String getStructureDisplayName(me.mami.stratocraft.enums.StructureType type) {
        if (type == null) return "Yapı";
        
        // StructureHelper kullan
        return me.mami.stratocraft.util.StructureHelper.getStructureDisplayName(type);
    }
    
    /**
     * Cooldown kontrolü
     */
    private boolean isOnCooldown(UUID playerId) {
        Long lastTime = activationCooldowns.get(playerId);
        if (lastTime == null) return false;
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTime) < ACTIVATION_COOLDOWN;
    }
    
    /**
     * Cooldown ayarla
     */
    private void setCooldown(UUID playerId) {
        activationCooldowns.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * ✅ DÜZELTME: Yapı çekirdeği kırıldığında özel item drop et
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureCoreBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        if (block.getType() != Material.OAK_LOG) {
            return;
        }
        
        Location coreLoc = block.getLocation();
        
        // ✅ ÖNCE MEMORY'DEN KONTROL ET (TileState gerektirmez)
        UUID ownerId = coreManager.getCoreOwner(coreLoc);
        
        // ✅ Eğer memory'de yoksa, PDC'den oku (chunk yüklüyse)
        if (ownerId == null) {
            try {
                org.bukkit.Chunk chunk = coreLoc.getChunk();
                if (chunk.isLoaded()) {
                    ownerId = me.mami.stratocraft.util.CustomBlockData.getStructureCoreOwner(block);
                }
            } catch (Exception e) {
                // Chunk yüklenemiyorsa atla
            }
        }
        
        if (ownerId == null) {
            return; // Normal OAK_LOG
        }
        
        // ✅ Normal drop'ları iptal et
        event.setDropItems(false);
        
        // ✅ Özel item oluştur (STRUCTURE_CORE item'ı)
        // ✅ DÜZELTME: Owner verisi ekleme - stacklenme için owner verisi eklenmeyecek
        // Owner verisi sadece yerleştirme sırasında memory'den alınır, item'da tutulmaz
        // Bu sayede tüm yapı çekirdekleri stacklenebilir
        ItemStack structureCoreItem = ItemManager.STRUCTURE_CORE != null ? ItemManager.STRUCTURE_CORE.clone() : null;
        if (structureCoreItem != null) {
            // ✅ Owner verisi EKLENMEYECEK - stacklenme için
            // Owner verisi sadece StructureCoreManager'da memory'de tutulur
            // Item'da owner verisi yok, bu yüzden tüm çekirdekler stacklenebilir
            
            // ✅ Özel item'ı drop et
            block.getWorld().dropItemNaturally(block.getLocation(), structureCoreItem);
        }
        
        // ✅ Yapı çekirdeğini temizle (StructureCoreManager'dan)
        coreManager.removeStructure(coreLoc);
        
        // ✅ CustomBlockData'dan da temizle
        me.mami.stratocraft.util.CustomBlockData.removeStructureCoreData(block);
    }
    
    /**
     * ✅ YENİ: BlockPlaceEvent'te ItemStack'ten veri geri yükleme
     * ✅ KRİTİK: MONITOR priority kullan (blok artık dünyada olduğu için PDC'ye yazabiliriz)
     * ✅ DÜZELTME: Item'da owner verisi yok (stacklenme için), bu yüzden oyuncunun UUID'sini kullan
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureCorePlaceRestore(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();
        
        if (block.getType() != Material.OAK_LOG || item == null || player == null) {
            return;
        }
        
        // ✅ ItemStack'ten kontrol et (STRUCTURE_CORE item'ı mı?)
        if (!ItemManager.isCustomItem(item, "STRUCTURE_CORE")) {
            return;
        }
        
        // ✅ KRİTİK: Blok artık dünyada, PDC'ye yazabiliriz
        // Owner verisi oyuncunun UUID'si (item'da yok, stacklenme için)
        me.mami.stratocraft.util.CustomBlockData.setStructureCoreData(block, player.getUniqueId());
                    
        // ✅ Memory'de zaten kayıtlı (onStructureCorePlace'te), burada sadece PDC'ye yazıyoruz
    }
}

