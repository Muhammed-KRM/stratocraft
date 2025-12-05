package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BatteryManager;
import me.mami.stratocraft.manager.BatteryManager.BatteryData;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import me.mami.stratocraft.Main;

public class BatteryListener implements Listener {
    private final BatteryManager batteryManager;
    private final TerritoryManager territoryManager;
    private final ResearchManager researchManager;
    private me.mami.stratocraft.manager.TrainingManager trainingManager;

    public BatteryListener(BatteryManager bm, TerritoryManager tm, ResearchManager rm) { 
        this.batteryManager = bm;
        this.territoryManager = tm;
        this.researchManager = rm;
    }
    
    public void setTrainingManager(me.mami.stratocraft.manager.TrainingManager tm) {
        this.trainingManager = tm;
    }
    
    private int getAlchemyTowerLevel(Player p) {
        Clan clan = territoryManager.getClanManager().getClanByPlayer(p.getUniqueId());
        if (clan == null) return 0;
        return clan.getStructures().stream()
                .filter(s -> s.getType() == Structure.Type.ALCHEMY_TOWER)
                .mapToInt(Structure::getLevel)
                .max()
                .orElse(0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        // Çift el kontrolünü engelle (Sadece ana el)
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();
        int slot = player.getInventory().getHeldItemSlot();

        // --- DURUM 1: ATEŞLEME (SOL TIK) ---
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            if (batteryManager.hasLoadedBattery(player, slot)) {
                event.setCancelled(true); // Bloğu kırmayı engelle
                fireBattery(player, slot);
                return;
            }
        }
                
        // --- DURUM 2: İPTAL ETME VEYA YÜKLEME (SAĞ TIK) ---
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            
            // Eğer bu slotta zaten yüklü bir batarya varsa
            if (batteryManager.hasLoadedBattery(player, slot)) {
                ItemStack handItem = player.getInventory().getItemInMainHand();
                Block clickedBlock = action == Action.RIGHT_CLICK_BLOCK ? event.getClickedBlock() : null;
                
                // AKTİF ETME vs İPTAL ETME AYIRIMI:
                // - Aktif etme: Batarya yüklü + elinde yakıt + bloğa sağ tık (shift yok) → Yeni batarya yükleme
                // - İptal etme: Batarya yüklü + (shift var VEYA elinde yakıt yok VEYA havaya tık) → İptal et
                
                boolean isBatteryFuel = false;
                if (handItem != null) {
                    Material fuel = handItem.getType();
                    isBatteryFuel = fuel == Material.DIAMOND || fuel == Material.IRON_INGOT || 
                                  ItemManager.isCustomItem(handItem, "RED_DIAMOND") ||
                                  ItemManager.isCustomItem(handItem, "DARK_MATTER") ||
                                  ItemManager.isCustomItem(handItem, "LIGHTNING_CORE") ||
                                  ItemManager.isCustomItem(handItem, "STAR_CORE") ||
                                  ItemManager.isCustomItem(handItem, "RUBY") ||
                                  ItemManager.isCustomItem(handItem, "ADAMANTITE") ||
                                  fuel == Material.COBBLESTONE ||
                                  fuel == Material.LAVA_BUCKET;
                }
                
                // AKTİF ETME: Batarya yüklü + elinde yakıt + bloğa sağ tık (shift yok) → Yeni batarya yükleme
                if (isBatteryFuel && clickedBlock != null && !player.isSneaking()) {
                    // Eski bataryayı otomatik iptal et (aktif etme = yeni batarya yükleme)
                    batteryManager.removeBattery(player, slot);
                    // Yeni batarya yükleme işlemi (aktif etme)
                    checkAndLoadBattery(player, clickedBlock, slot, event);
                    return;
                }
                
                // İPTAL ETME: Diğer durumlarda iptal et
                // ÖN KONTROL 1: Batarya yeni aktif edildi mi? (2 saniye içinde iptal edilemez)
                if (batteryManager.isBatteryRecentlyActivated(player, slot)) {
                    event.setCancelled(true);
                    player.sendMessage("§cBatarya yeni aktif edildi! 2 saniye sonra iptal edebilirsin.");
                    return;
                }
                
                // ÖN KONTROL 2: Shift basılı değilse iptal edilebilir (zaten yukarıda kontrol edildi)
                // Normal iptal etme işlemi
                event.setCancelled(true); // Başka bir şeyle etkileşimi engelle
                dischargeBattery(player, slot);
                return;
            }

            // Eğer yüklü değilse ve bir bloğa tıklıyorsa -> YÜKLEME KONTROLÜ
            if (action == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null) {
                    // Önce "Yüklü Batarya Çekirdeği" kontrolü
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem != null && handItem.getType() == Material.ECHO_SHARD) {
                        if (isLoadedBatteryCore(handItem)) {
                            // Yüklü Batarya Çekirdeği kullanarak hızlı yükleme
                            if (restoreBatteryFromCore(player, handItem, slot, clickedBlock, event)) {
                                return; // Başarılı, devam etme
                            }
                        }
                    }
                    
                    // Her batarya tipi için kontrol
                    checkAndLoadBattery(player, clickedBlock, slot, event);
                    // Yükleme başarılı olduysa (event iptal edildiyse), kodun devam etmemesi için return
                    if (event.isCancelled()) {
                        return;
                    }
                }
            }
            
            // Havaya tıklama durumunda da Yüklü Batarya Çekirdeği kontrolü (blok gerekmez)
            if ((action == Action.RIGHT_CLICK_AIR) && player.isSneaking()) {
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem != null && handItem.getType() == Material.ECHO_SHARD) {
                    if (isLoadedBatteryCore(handItem)) {
                        // Havaya tıklayınca da çalışabilir (blok gerekmez, null geçilebilir)
                        if (restoreBatteryFromCore(player, handItem, slot, null, event)) {
                            return;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Batarya yükleme kontrolü - Tüm batarya tipleri için
     */
    private void checkAndLoadBattery(Player player, Block centerBlock, int slot, PlayerInteractEvent event) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null) return;
        
        // Önce yeni batarya sistemini kontrol et
        if (checkNewBatterySystem(player, centerBlock, slot, event)) {
            return; // Yeni sistemde bulunduysa eski sisteme geçme
        }
        
        // 1. MAGMA BATARYASI (3 Magma Bloğu üst üste) - ESKİ SİSTEM (geriye dönük uyumluluk)
        if (centerBlock.getType() == Material.MAGMA_BLOCK) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block above = centerBlock.getRelative(BlockFace.UP);
            
            if (below.getType() == Material.MAGMA_BLOCK && above.getType() == Material.MAGMA_BLOCK) {
                Material fuel = handItem.getType();
                boolean isRedDiamond = ItemManager.isCustomItem(handItem, "RED_DIAMOND");
                boolean isDarkMatter = ItemManager.isCustomItem(handItem, "DARK_MATTER");
                
                if (fuel == Material.DIAMOND || fuel == Material.IRON_INGOT || isRedDiamond || isDarkMatter) {
                    // Tarif kontrolü
                    boolean hasRecipe = researchManager.hasRecipeBook(player, "MAGMA_BATTERY");
                    if (!me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(player)) {
                        if ((isRedDiamond || isDarkMatter) && !hasRecipe) {
                            player.sendMessage("§cAteştopu Bataryası'nın gelişmiş versiyonu için tarif kitabı gerekli!");
                            return;
                        }
                    }
                    
                    // Aynı slotta başka batarya var mı kontrolü
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var! Önce onu ateşle veya iptal et.");
                        event.setCancelled(true);
                        return;
                    }
                    
                    // Antrenman/Mastery kontrolü (yükleme sırasında sadece çarpanı hesapla, kayıt ateşleme sırasında yapılacak)
                    double powerMultiplier = trainingManager != null ? trainingManager.getMasteryMultiplier(player.getUniqueId(), "MAGMA_BATTERY") : 1.0;
                    int alchemyLevel = getAlchemyTowerLevel(player);
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    boolean hasAmplifier = ItemManager.isCustomItem(offHand, "FLAME_AMPLIFIER");
                    
                    // Seviye tespiti
                    int batteryLevel = batteryManager.detectBatteryLevel(centerBlock, Material.MAGMA_BLOCK);
                    if (batteryLevel == 0) batteryLevel = 1; // Varsayılan seviye 1
                    
                    // Seviye 5 için özel kontrol (altında BEACON, üstünde NETHER_STAR veya BEDROCK)
                    if (batteryLevel >= 4) {
                        Block bottom = centerBlock;
                        while (bottom.getRelative(BlockFace.DOWN).getType() == Material.MAGMA_BLOCK) {
                            bottom = bottom.getRelative(BlockFace.DOWN);
                        }
                        Block top = centerBlock;
                        while (top.getRelative(BlockFace.UP).getType() == Material.MAGMA_BLOCK) {
                            top = top.getRelative(BlockFace.UP);
                        }
                        Block belowSpecial = bottom.getRelative(BlockFace.DOWN);
                        Block aboveSpecial = top.getRelative(BlockFace.UP);
                        if (belowSpecial.getType() == Material.BEACON && 
                            (aboveSpecial.getType() == Material.NETHER_STAR || 
                             aboveSpecial.getType() == Material.BEDROCK)) {
                            batteryLevel = 5;
                        } else if (batteryLevel == 4) {
                            // Seviye 4 için özel kontrol (altında BEACON yeterli)
                            if (belowSpecial.getType() == Material.BEACON) {
                                batteryLevel = 4;
                            } else {
                                batteryLevel = 3; // 11+ blok ama özel blok yok = Seviye 3
                            }
                        }
                    }
                    
                    // Batarya verisini oluştur (seviye ile)
                    BatteryData data = new BatteryData("Ateş Topu", fuel, alchemyLevel, hasAmplifier, 
                                                      powerMultiplier, isRedDiamond, isDarkMatter, batteryLevel);
                    
                    // Seviye mesajı
                    if (batteryLevel > 1) {
                        player.sendMessage("§6§lSeviye " + batteryLevel + " Batarya tespit edildi!");
                        if (batteryLevel == 5) {
                            player.sendMessage("§c§lEFSANEVI GÜÇ AKTİF! Dağ yıkma, klan yok etme ve boss yenme gücü hazır!");
                        }
                    }
                    
                    // Yükleme işlemi
                    loadBattery(player, centerBlock, below, above, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 2. YILDIRIM BATARYASI (3 Demir Bloğu üst üste)
        if (centerBlock.getType() == Material.IRON_BLOCK) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block above = centerBlock.getRelative(BlockFace.UP);
            
            if (below.getType() == Material.IRON_BLOCK && above.getType() == Material.IRON_BLOCK) {
                if (ItemManager.isCustomItem(handItem, "LIGHTNING_CORE")) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Yıldırım", Material.AIR, 0, false, 1.0, false, false);
                    loadBattery(player, centerBlock, below, above, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 3. KARA DELİK (3 Obsidyen üst üste)
        if (centerBlock.getType() == Material.OBSIDIAN) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block above = centerBlock.getRelative(BlockFace.UP);
            
            if (below.getType() == Material.OBSIDIAN && above.getType() == Material.OBSIDIAN) {
                if (ItemManager.isCustomItem(handItem, "DARK_MATTER")) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Kara Delik", Material.AIR, 0, false, 1.0, false, true);
                    loadBattery(player, centerBlock, below, above, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 4. ANLIK KÖPRÜ (3 Buz üst üste)
        if (centerBlock.getType() == Material.PACKED_ICE) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block above = centerBlock.getRelative(BlockFace.UP);
            
            if (below.getType() == Material.PACKED_ICE && above.getType() == Material.PACKED_ICE) {
                if (handItem.getType() == Material.FEATHER) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Anlık Köprü", Material.FEATHER, 0, false, 1.0, false, false);
                    loadBattery(player, centerBlock, below, above, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 5. SIĞINAK KÜPÜ (3 Kırıktaş üst üste)
        if (centerBlock.getType() == Material.COBBLESTONE) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block above = centerBlock.getRelative(BlockFace.UP);
            
            if (below.getType() == Material.COBBLESTONE && above.getType() == Material.COBBLESTONE) {
                if (handItem.getType() == Material.IRON_INGOT) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Sığınak Küpü", Material.IRON_INGOT, 0, false, 1.0, false, false);
                    loadBattery(player, centerBlock, below, above, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 6. YERÇEKİMİ ÇAPASI (Örs + Slime Bloğu altında)
        if (centerBlock.getType() == Material.ANVIL) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.SLIME_BLOCK) {
                if (handItem.getType() == Material.IRON_INGOT) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Yerçekimi Çapası", Material.IRON_INGOT, 0, false, 1.0, false, false);
                    loadBattery(player, centerBlock, below, null, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 7. TOPRAK SURU (3 Toprak Bloğu yanyana)
        if (centerBlock.getType() == Material.DIRT) {
            Block east = centerBlock.getRelative(BlockFace.EAST);
            Block west = centerBlock.getRelative(BlockFace.WEST);
            
            if (east.getType() == Material.DIRT && west.getType() == Material.DIRT) {
                if (handItem.getType() == Material.COBBLESTONE || ItemManager.isCustomItem(handItem, "TITANIUM_INGOT")) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Toprak Suru", handItem.getType(), 0, false, 1.0, false, false);
                    // Yanyana bloklar için özel yükleme
                    loadBatteryHorizontal(player, centerBlock, east, west, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 8. MANYETİK BOZUCU (3 Demir + 1 Lapis üstte)
        if (centerBlock.getType() == Material.LAPIS_BLOCK) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block below2 = below.getRelative(BlockFace.DOWN);
            
            if (below.getType() == Material.IRON_BLOCK && below2.getType() == Material.IRON_BLOCK) {
                if (handItem.getType() == Material.IRON_INGOT) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Manyetik Bozucu", Material.IRON_INGOT, 0, false, 1.0, false, false);
                    loadBatteryVertical(player, centerBlock, below, below2, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 9. SİSMİK ÇEKİÇ (Örs + 2 Demir altında)
        if (centerBlock.getType() == Material.ANVIL) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block below2 = below.getRelative(BlockFace.DOWN);
            
            if (below.getType() == Material.IRON_BLOCK && below2.getType() == Material.IRON_BLOCK) {
                if (ItemManager.isCustomItem(handItem, "STAR_CORE")) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Sismik Çekiç", Material.AIR, 0, false, 1.0, false, false);
                    loadBatteryVertical(player, centerBlock, below, below2, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 10. OZON KALKANI (Beacon + Cam altında)
        if (centerBlock.getType() == Material.BEACON) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            
            if (below.getType() == Material.GLASS) {
                if (ItemManager.isCustomItem(handItem, "RUBY")) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Ozon Kalkanı", Material.AIR, 0, false, 1.0, false, false);
                    loadBattery(player, centerBlock, below, null, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 11. ENERJİ DUVARI (3 Demir üst üste + Adamantite)
        if (centerBlock.getType() == Material.IRON_BLOCK) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            Block below2 = below.getRelative(BlockFace.DOWN);
            
            if (below.getType() == Material.IRON_BLOCK && below2.getType() == Material.IRON_BLOCK) {
                if (ItemManager.isCustomItem(handItem, "ADAMANTITE")) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Enerji Duvarı", Material.AIR, 0, false, 1.0, false, false);
                    loadBatteryVertical(player, centerBlock, below, below2, handItem, slot, data, event);
                    return;
                }
            }
        }
        
        // 12. LAV HENDEKÇİSİ (2 Lav üst üste)
        if (centerBlock.getType() == Material.LAVA) {
            Block below = centerBlock.getRelative(BlockFace.DOWN);
            
            if (below.getType() == Material.LAVA) {
                if (handItem.getType() == Material.LAVA_BUCKET) {
                    if (batteryManager.hasLoadedBattery(player, slot)) {
                        player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                        event.setCancelled(true);
                        return;
                    }
                    
                    BatteryData data = new BatteryData("Lav Hendekçisi", Material.LAVA_BUCKET, 0, false, 1.0, false, false);
                    loadBattery(player, centerBlock, below, null, handItem, slot, data, event);
                    // Lava bucket özel - sadece 1 tane kullanılır, bucket geri gelir
                    player.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
                    return;
                }
            }
        }
    }
    
    /**
     * Bataryayı yükleme işlemi (tüm blokları kaldır - seviyeye göre)
     */
    private void loadBattery(Player player, Block center, Block below, Block above, 
                            ItemStack handItem, int slot, BatteryData data, PlayerInteractEvent event) {
        event.setCancelled(true);
        
        // 1. Eşyayı al
        if (handItem.getType() != Material.LAVA_BUCKET) { // Lava bucket özel işlenir
            handItem.setAmount(handItem.getAmount() - 1);
        }
        
        // 2. Yapıyı yok et - TÜM BLOKLARI KALDIR (seviyeye göre)
        int batteryLevel = data.getBatteryLevel();
        Material baseBlock = center.getType();
        
        // Önce en alttaki bloğu bul
        Block bottom = center;
        while (bottom.getRelative(BlockFace.DOWN).getType() == baseBlock) {
            bottom = bottom.getRelative(BlockFace.DOWN);
        }
        
        // En üstteki bloğu bul
        Block top = center;
        while (top.getRelative(BlockFace.UP).getType() == baseBlock) {
            top = top.getRelative(BlockFace.UP);
        }
        
        // Tüm baseBlock bloklarını kaldır (alttan üste kadar)
        Block current = bottom;
        int removed = 0;
        while (current.getY() <= top.getY() && removed < 30) { // Güvenlik için maksimum 30
            if (current.getType() == baseBlock) {
                current.setType(Material.AIR);
                removed++;
            }
            current = current.getRelative(BlockFace.UP);
        }
        
        // Seviye 5 için özel blokları da kaldır (BEACON altında, NETHER_STAR/BEDROCK üstte)
        if (batteryLevel == 5) {
            // Altındaki BEACON'u kaldır
            Block belowSpecial = bottom.getRelative(BlockFace.DOWN);
            if (belowSpecial.getType() == Material.BEACON) {
                belowSpecial.setType(Material.AIR);
            }
            
            // Üstündeki NETHER_STAR veya BEDROCK'u kaldır
            Block aboveSpecial = top.getRelative(BlockFace.UP);
            if (aboveSpecial.getType() == Material.NETHER_STAR || aboveSpecial.getType() == Material.BEDROCK) {
                aboveSpecial.setType(Material.AIR);
            }
        }
        
        // Yan blokları kaldır (seviye 2+ için)
        if (batteryLevel >= 2) {
            // Merkez bloğu bul (bottom ve top arasında ortada)
            Block middle = bottom;
            int height = (int)(top.getY() - bottom.getY() + 1);
            int middleOffset = height / 2;
            for (int i = 0; i < middleOffset; i++) {
                middle = middle.getRelative(BlockFace.UP);
            }
            
            Block east = middle.getRelative(BlockFace.EAST);
            Block west = middle.getRelative(BlockFace.WEST);
            Block north = middle.getRelative(BlockFace.NORTH);
            Block south = middle.getRelative(BlockFace.SOUTH);
            
            // Yan blokların türünü kontrol et (sideBlock olabilir)
            Material sideBlock = null;
            if (east.getType() != baseBlock && east.getType() != Material.AIR) sideBlock = east.getType();
            if (west.getType() != baseBlock && west.getType() != Material.AIR) sideBlock = west.getType();
            
            if (sideBlock != null) {
                // Tüm yan blokları kaldır (4 yönde)
                if (east.getType() == sideBlock) east.setType(Material.AIR);
                if (west.getType() == sideBlock) west.setType(Material.AIR);
                if (north.getType() == sideBlock) north.setType(Material.AIR);
                if (south.getType() == sideBlock) south.setType(Material.AIR);
            }
        }
        
        // Efektler
        org.bukkit.Location effectLoc = center.getLocation().add(0.5, 0.5, 0.5);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 1);
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 10);
        
        // 3. Bataryayı Manager'a kaydet (Yükle)
        batteryManager.loadBattery(player, slot, data);
        
        // Ses efekti
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
    }
    
    /**
     * Bataryayı yükleme işlemi (3 blok üst üste - dikey)
     */
    private void loadBatteryVertical(Player player, Block top, Block middle, Block bottom, 
                                    ItemStack handItem, int slot, BatteryData data, PlayerInteractEvent event) {
        event.setCancelled(true);
        
        // 1. Eşyayı al
        handItem.setAmount(handItem.getAmount() - 1);
        
        // 2. Yapıyı yok et
        top.setType(Material.AIR);
        if (middle != null) middle.setType(Material.AIR);
        if (bottom != null) bottom.setType(Material.AIR);
        
        // Efektler
        org.bukkit.Location effectLoc = middle != null ? middle.getLocation().add(0.5, 0.5, 0.5) : top.getLocation().add(0.5, 0.5, 0.5);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 1);
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 10);
        
        // 3. Bataryayı Manager'a kaydet
        batteryManager.loadBattery(player, slot, data);
        
        // Ses efekti
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
    }
    
    /**
     * Bataryayı yükleme işlemi (3 blok yanyana - yatay)
     */
    private void loadBatteryHorizontal(Player player, Block center, Block east, Block west, 
                                      ItemStack handItem, int slot, BatteryData data, PlayerInteractEvent event) {
        event.setCancelled(true);
        
        // 1. Eşyayı al
        handItem.setAmount(handItem.getAmount() - 1);
        
        // 2. Yapıyı yok et
        center.setType(Material.AIR);
        if (east != null) east.setType(Material.AIR);
        if (west != null) west.setType(Material.AIR);
        
        // Efektler
        org.bukkit.Location effectLoc = center.getLocation().add(0.5, 0.5, 0.5);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 1);
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 10);
        
        // 3. Bataryayı Manager'a kaydet
        batteryManager.loadBattery(player, slot, data);
        
        // Ses efekti
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
    }
    
    /**
     * Bataryayı ateşleme mantığı
     */
    private void fireBattery(Player player, int slot) {
        BatteryData data = batteryManager.getLoadedBattery(player, slot);
        if (data == null) return;
        
        String type = data.getType();
        
        // BatteryManager metodlarını çağır (kod tekrarını önlemek için)
        if (type.equals("Ateş Topu")) {
            // Mastery kaydı (ateşleme sırasında yapılır)
            if (trainingManager != null) {
                trainingManager.recordUse(player.getUniqueId(), "MAGMA_BATTERY");
            }
            
            // BatteryManager metodunu çağır (seviye ile + yakıt tipi bilgisi)
            int batteryLevel = data.getBatteryLevel();
            batteryManager.fireMagmaBattery(player, data.getFuel(), data.getAlchemyLevel(), 
                                          data.hasAmplifier(), data.getTrainingMultiplier(), batteryLevel,
                                          data.isRedDiamond(), data.isDarkMatter());
            
            // Seviye 5 özel güçler
            if (batteryLevel == 5) {
                // Klan yıkımı (sağ elinde özel item varsa)
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (ItemManager.isCustomItem(mainHand, "RED_DIAMOND")) {
                    batteryManager.fireClanDestroyer(player, territoryManager);
                }
                
                // Boss yıkımı (sol elinde özel item varsa)
                ItemStack offHand = player.getInventory().getItemInOffHand();
                if (ItemManager.isCustomItem(offHand, "STAR_CORE")) {
                    batteryManager.fireBossDestroyer(player, 
                        me.mami.stratocraft.Main.getInstance().getBossManager());
                }
            }
            
            // Mastery mesajları ve seviye atlama kontrolü (Listener'a özel)
            handleMasteryMessages(player, "MAGMA_BATTERY", data);
            
        } else if (type.equals("Yıldırım")) {
            batteryManager.fireLightningBattery(player);
            // BUG FIX: Ateşlendikten sonra bataryayı sil
            batteryManager.removeBattery(player, slot);
        } else {
            // Yeni batarya sistemi - BatteryType enum kullanarak
            BatteryManager.BatteryType batteryType = getBatteryTypeFromName(type);
            if (batteryType != null) {
                batteryManager.fireBattery(player, batteryType, data);
                // BUG FIX: Ateşlendikten sonra bataryayı sil
                batteryManager.removeBattery(player, slot);
            } else {
                // Eski bataryalar (geriye dönük uyumluluk)
                if (type.equals("Kara Delik")) {
                    batteryManager.fireBlackHole(player);
                } else if (type.equals("Anlık Köprü")) {
                    batteryManager.createInstantBridge(player);
                } else if (type.equals("Sığınak Küpü")) {
                    batteryManager.createInstantBunker(player);
                } else if (type.equals("Yerçekimi Çapası")) {
                    batteryManager.fireGravityAnchor(player);
                } else if (type.equals("Toprak Suru")) {
                    batteryManager.createEarthWall(player, data.getFuel());
                } else if (type.equals("Manyetik Bozucu")) {
                    batteryManager.fireMagneticDisruptor(player);
                } else if (type.equals("Sismik Çekiç")) {
                    batteryManager.fireSeismicHammer(player);
                } else if (type.equals("Ozon Kalkanı")) {
                    batteryManager.activateOzoneShield(player, player.getLocation());
                } else if (type.equals("Enerji Duvarı")) {
                    batteryManager.createEnergyWall(player);
                } else if (type.equals("Lav Hendekçisi")) {
                    batteryManager.createLavaTrench(player, territoryManager);
                }
                
                // Ateşlendikten sonra bataryayı sil
                batteryManager.removeBattery(player, slot);
            }
        }
        
        // BUG FIX: "Ateş Topu" için de bataryayı sil
        if (type.equals("Ateş Topu")) {
            batteryManager.removeBattery(player, slot);
        }
    }
    
    /**
     * Batarya isminden BatteryType enum'una çevir
     */
    private BatteryManager.BatteryType getBatteryTypeFromName(String name) {
        for (BatteryManager.BatteryType type : BatteryManager.BatteryType.values()) {
            if (type.getDisplayName().equals(name)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Yeni batarya tespit sistemi - BatteryType enum kullanarak
     * @return true if battery was found and loaded, false otherwise
     */
    private boolean checkNewBatterySystem(Player player, Block centerBlock, int slot, PlayerInteractEvent event) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null) return false;
        
        Block below = centerBlock.getRelative(BlockFace.DOWN);
        Block above = centerBlock.getRelative(BlockFace.UP);
        Block east = centerBlock.getRelative(BlockFace.EAST);
        Block west = centerBlock.getRelative(BlockFace.WEST);
        
        // Tüm BatteryType'ları kontrol et
        for (BatteryManager.BatteryType batteryType : BatteryManager.BatteryType.values()) {
            Material baseBlock = batteryType.getBaseBlock();
            Material sideBlock = batteryType.getSideBlock();
            
            // Temel blok kontrolü
            if (centerBlock.getType() != baseBlock) continue;
            
            // Üst üste blok kontrolü
            if (below.getType() != baseBlock || above.getType() != baseBlock) continue;
            
            // Seviye tespiti
            int batteryLevel = batteryManager.detectBatteryLevel(centerBlock, baseBlock);
            if (batteryLevel != batteryType.getLevel()) continue;
            
            // Seviye 5 için özel kontrol: baseBlock BEDROCK olmalı
            if (batteryLevel == 5) {
                // Seviye 5 bataryalar sadece BEDROCK kullanır
                if (baseBlock != Material.BEDROCK) continue;
                
                // Alt ve üst blokları bul
                Block bottom = centerBlock;
                while (bottom.getRelative(BlockFace.DOWN).getType() == baseBlock) {
                    bottom = bottom.getRelative(BlockFace.DOWN);
                }
                Block top = centerBlock;
                while (top.getRelative(BlockFace.UP).getType() == baseBlock) {
                    top = top.getRelative(BlockFace.UP);
                }
                Block belowSpecial = bottom.getRelative(BlockFace.DOWN);
                Block aboveSpecial = top.getRelative(BlockFace.UP);
                
                // Seviye 5 için: altında BEACON olmalı
                if (belowSpecial.getType() != Material.BEACON) continue;
                
                // Üstteki özel blok sideBlock ile eşleşmeli (enum'da sideBlock üstteki özel blok)
                if (sideBlock != null && aboveSpecial.getType() != sideBlock) continue;
            }
            
            // Yan blok kontrolü (seviye 2-4 için, seviye 5 için yan blok yok)
            if (sideBlock != null && batteryLevel >= 2 && batteryLevel <= 4) {
                // Yan bloklar sadece EAST, WEST, NORTH, SOUTH olmalı
                Block north = centerBlock.getRelative(BlockFace.NORTH);
                Block south = centerBlock.getRelative(BlockFace.SOUTH);
                boolean hasSideBlock = east.getType() == sideBlock || west.getType() == sideBlock ||
                                      north.getType() == sideBlock || south.getType() == sideBlock;
                if (!hasSideBlock) continue;
            }
            
            // Yakıt kontrolü - Seviye 5 için DARK_MATTER zorunlu
            Material fuel = handItem.getType();
            boolean isDarkMatter = ItemManager.isCustomItem(handItem, "DARK_MATTER");
            boolean isRedDiamond = ItemManager.isCustomItem(handItem, "RED_DIAMOND");
            
            if (batteryLevel == 5) {
                // Seviye 5 için sadece DARK_MATTER kabul edilir
                if (!isDarkMatter) {
                    player.sendMessage("§c§lSeviye 5 bataryalar için §5§lKaranlık Madde §cgerekli!");
                    event.setCancelled(true);
                    return true;
                }
            } else {
                // Diğer seviyeler için normal yakıt kontrolü
                boolean isValidFuel = fuel == Material.DIAMOND || fuel == Material.IRON_INGOT ||
                                     isRedDiamond || isDarkMatter;
                if (!isValidFuel) continue;
            }
            
            // Zaten yüklü batarya var mı?
            if (batteryManager.hasLoadedBattery(player, slot)) {
                player.sendMessage("§cBu slotta zaten yüklü bir batarya var!");
                event.setCancelled(true);
                return true;
            }
            
            // Batarya verisini oluştur (isRedDiamond ve isDarkMatter zaten yukarıda tanımlı)
            int alchemyLevel = getAlchemyTowerLevel(player);
            ItemStack offHand = player.getInventory().getItemInOffHand();
            boolean hasAmplifier = ItemManager.isCustomItem(offHand, "FLAME_AMPLIFIER");
            double trainingMultiplier = 1.0; // Yeni bataryalar için mastery yok (şimdilik)
            
            BatteryData data = new BatteryData(
                batteryType.getDisplayName(),
                fuel,
                alchemyLevel,
                hasAmplifier,
                trainingMultiplier,
                isRedDiamond,
                isDarkMatter,
                batteryLevel
            );
            
            // Yükleme işlemi
            loadBattery(player, centerBlock, below, above, handItem, slot, data, event);
            
            // Seviye mesajı
            if (batteryLevel > 1) {
                player.sendMessage("§6§lSeviye " + batteryLevel + " " + batteryType.getDisplayName() + " tespit edildi!");
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Mastery mesajlarını ve seviye atlama kontrolünü yönet (Listener'a özel)
     */
    private void handleMasteryMessages(Player p, String trainingKey, BatteryData data) {
        if (trainingManager == null) return;
        
        int previousLevel = trainingManager.getPreviousMasteryLevel(p.getUniqueId(), trainingKey);
        int newLevel = trainingManager.getMasteryLevel(p.getUniqueId(), trainingKey);
        int totalUses = trainingManager.getTotalUses(p.getUniqueId(), trainingKey);
        boolean nowTrained = trainingManager.isTrained(p.getUniqueId(), trainingKey);
        
        if (newLevel > previousLevel) {
            if (previousLevel == -1 && newLevel == 0) {
                p.sendTitle("§a§lANTRENMAN TAMAMLANDI!", "§eArtık tam güçle kullanabilirsin!", 10, 70, 20);
                p.sendMessage("§a§l════════════════════════════");
                p.sendMessage("§e§l★ ANTRENMAN TAMAMLANDI ★");
                p.sendMessage("§7Artık bataryayı tam güçle kullanabilirsin!");
                p.sendMessage("§7Mastery seviyesi için 20 kullanım gerekli.");
                p.sendMessage("§a§l════════════════════════════");
                p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            } else {
                p.sendTitle("§6§lSEVİYE ATLADI!", "§eMastery Seviye " + newLevel + " §7(" + getMasteryBonusText(newLevel) + ")", 10, 70, 20);
                p.sendMessage("§6§l════════════════════════════");
                p.sendMessage("§e§l★ MASTERY SEVİYE " + newLevel + " ★");
                p.sendMessage("§7Güç artışı: §a" + getMasteryBonusText(newLevel));
                p.sendMessage("§7Toplam kullanım: §b" + totalUses);
                p.sendMessage("§6§l════════════════════════════");
                p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        } else if (!nowTrained) {
            int remaining = trainingManager.getRemainingUses(p.getUniqueId(), trainingKey);
            double progress = trainingManager.getTrainingProgress(p.getUniqueId(), trainingKey);
            p.sendMessage("§e[Antrenman] §7Güç: §c" + String.format("%.0f", progress * 100) + "% §7(" + remaining + " kullanım kaldı)");
        } else if (newLevel > 0) {
            p.sendMessage("§a[Mastery Seviye " + newLevel + "] §7Güç: §e" + getMasteryBonusText(newLevel) + " §7(" + totalUses + " kullanım)");
        }
    }
    
    /**
     * Mastery bonus metnini al
     */
    private String getMasteryBonusText(int level) {
        if (level <= 0) return "Yok";
        if (level == 1) return "+20%";
        if (level == 2) return "+30%";
        if (level == 3) return "+40%";
        return "+" + (20 + (level * 10)) + "%";
    }
    
    /**
     * Bataryayı boşa çıkarma (İptal) mantığı
     */
    private void dischargeBattery(Player player, int slot) {
        BatteryData data = batteryManager.getLoadedBattery(player, slot);
        if (data == null) return;
        
        batteryManager.removeBattery(player, slot);
        
        player.sendMessage(ChatColor.RED + "❌ " + data.getType() + " deşarj edildi. (Kaynaklar geri verilmez).");
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 0.5f);
    }
    
    /**
     * Oyuncu çıkışında bataryaları temizle
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        batteryManager.clearBatteries(event.getPlayer());
    }
    
    /**
     * Oyuncu öldüğünde yüklü bataryaları "Yüklü Batarya Çekirdeği" item'ına dönüştür
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Oyuncunun yüklü bataryalarını kontrol et
        if (!batteryManager.hasAnyLoadedBattery(player)) {
            return; // Batarya yok, devam et
        }
        
        // Tüm yüklü bataryaları item'a dönüştür ve drop et
        java.util.Map<Integer, BatteryData> batteries = batteryManager.getAllLoadedBatteries(player);
        if (batteries == null || batteries.isEmpty()) {
            return;
        }
        
        for (java.util.Map.Entry<Integer, BatteryData> entry : batteries.entrySet()) {
            int slot = entry.getKey();
            BatteryData battery = entry.getValue();
            
            // Yüklü Batarya Çekirdeği item'ı oluştur
            ItemStack batteryCore = createLoadedBatteryCore(battery, slot);
            
            // Item'ı drop et
            player.getWorld().dropItemNaturally(player.getLocation(), batteryCore);
        }
        
        // Bataryaları temizle
        batteryManager.clearBatteries(player);
        
        // Oyuncuya bilgi ver (oyuncu ölü olsa bile mesaj gönderilebilir, ama güvenli olmak için kontrol)
        if (player.isOnline()) {
            // Ölüm mesajı genelde chat'e gider, ama oyuncu ölü olabilir
            // Bu yüzden delayed task ile gönderelim
            org.bukkit.Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (player.isOnline()) {
                    player.sendMessage("§e⚡ Yüklü bataryalarınız 'Yüklü Batarya Çekirdeği' olarak düştü!");
                    player.sendMessage("§7Bu item'ı kullanarak bataryalarınızı hızlıca yeniden yükleyebilirsiniz.");
                }
            }, 20L); // 1 saniye sonra (oyuncu respawn olmuş olabilir)
        }
    }
    
    /**
     * Yüklü Batarya Çekirdeği item'ı oluştur
     */
    private ItemStack createLoadedBatteryCore(BatteryData battery, int slot) {
        ItemStack item = new ItemStack(Material.ECHO_SHARD); // Echo Shard görsel olarak uygun
        ItemMeta meta = item.getItemMeta();
        
        if (meta == null) return item;
        
        // Item ismi ve açıklama
        meta.setDisplayName("§6⚡ Yüklü Batarya Çekirdeği");
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7Batarya Tipi: §e" + battery.getType());
        lore.add("§7Slot: §e" + (slot + 1));
        lore.add("§7Yakıt: §e" + getFuelDisplayName(battery.getFuel(), battery.isRedDiamond(), battery.isDarkMatter()));
        if (battery.getAlchemyLevel() > 0) {
            lore.add("§7Simya Seviyesi: §e" + battery.getAlchemyLevel());
        }
        if (battery.hasAmplifier()) {
            lore.add("§7Amplifikatör: §aVar");
        }
        lore.add("");
        lore.add("§7Shift + Sağ Tık ile bataryayı");
        lore.add("§7hızlıca yeniden yükleyebilirsiniz.");
        meta.setLore(lore);
        
        // NBT ile batarya verilerini sakla
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "loaded_battery");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, 
            serializeBatteryData(battery, slot));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Batarya verilerini serialize et (NBT için)
     */
    private String serializeBatteryData(BatteryData battery, int slot) {
        // Format: type:fuel:alchemyLevel:hasAmplifier:trainingMultiplier:isRedDiamond:isDarkMatter:batteryLevel:slot
        return battery.getType() + ":" +
               battery.getFuel().name() + ":" +
               battery.getAlchemyLevel() + ":" +
               battery.hasAmplifier() + ":" +
               battery.getTrainingMultiplier() + ":" +
               battery.isRedDiamond() + ":" +
               battery.isDarkMatter() + ":" +
               battery.getBatteryLevel() + ":" +
               slot;
    }
    
    /**
     * Yakıt ismini göster
     */
    private String getFuelDisplayName(Material fuel, boolean isRedDiamond, boolean isDarkMatter) {
        if (isDarkMatter) return "Karanlık Madde";
        if (isRedDiamond) return "Kızıl Elmas";
        if (fuel == Material.DIAMOND) return "Elmas";
        if (fuel == Material.IRON_INGOT) return "Demir";
        return fuel.name();
    }
    
    /**
     * Item "Yüklü Batarya Çekirdeği" mi kontrol et
     */
    private boolean isLoadedBatteryCore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "loaded_battery");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }
    
    /**
     * Yüklü Batarya Çekirdeği'nden bataryayı geri yükle
     */
    private boolean restoreBatteryFromCore(Player player, ItemStack coreItem, int slot, 
                                            Block clickedBlock, PlayerInteractEvent event) {
        if (coreItem == null || !coreItem.hasItemMeta()) return false;
        
        ItemMeta meta = coreItem.getItemMeta();
        if (meta == null) return false;
        
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "loaded_battery");
        String data = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (data == null) return false;
        
        // Batarya verilerini deserialize et
        BatteryData batteryData = deserializeBatteryData(data);
        if (batteryData == null) return false;
        
        // Aynı slotta başka batarya var mı?
        if (batteryManager.hasLoadedBattery(player, slot)) {
            player.sendMessage("§cBu slotta zaten yüklü bir batarya var! Önce onu ateşle veya iptal et.");
            return false;
        }
        
        // Bataryayı yükle (blok yapısı gerekmez, direkt yükle)
        batteryManager.loadBattery(player, slot, batteryData);
        
        // Item'ı tüket (1 adet azalt)
        if (coreItem.getAmount() > 1) {
            coreItem.setAmount(coreItem.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Event'i iptal et
        event.setCancelled(true);
        
        // Efektler (clickedBlock null ise oyuncunun konumunu kullan)
        org.bukkit.Location effectLoc = clickedBlock != null ? 
            clickedBlock.getLocation().clone().add(0.5, 0.5, 0.5) : 
            player.getLocation().clone().add(0, 1, 0);
        player.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, effectLoc, 1);
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, effectLoc, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 2f);
        
        player.sendMessage("§a⚡ " + batteryData.getType() + " bataryası başarıyla yeniden yüklendi!");
        return true;
    }
    
    /**
     * Batarya verilerini deserialize et
     */
    private BatteryData deserializeBatteryData(String data) {
        try {
            String[] parts = data.split(":");
            // Eski format (8 parça) veya yeni format (9 parça) destekle
            if (parts.length < 8) return null;
            
            String type = parts[0];
            Material fuel = Material.valueOf(parts[1]);
            int alchemyLevel = Integer.parseInt(parts[2]);
            boolean hasAmplifier = Boolean.parseBoolean(parts[3]);
            double trainingMultiplier = Double.parseDouble(parts[4]);
            boolean isRedDiamond = Boolean.parseBoolean(parts[5]);
            boolean isDarkMatter = Boolean.parseBoolean(parts[6]);
            
            // batteryLevel: yeni format (9 parça) veya varsayılan 1 (eski format)
            int batteryLevel = parts.length >= 9 ? Integer.parseInt(parts[7]) : 1;
            // slot bilgisi: son parça (yeni format) veya 7. parça (eski format)
            
            return new BatteryData(type, fuel, alchemyLevel, hasAmplifier, 
                                 trainingMultiplier, isRedDiamond, isDarkMatter, batteryLevel);
        } catch (Exception e) {
            return null;
        }
    }
}
