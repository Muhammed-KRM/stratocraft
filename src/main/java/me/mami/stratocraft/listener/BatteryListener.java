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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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
                    // Her batarya tipi için kontrol
                    checkAndLoadBattery(player, clickedBlock, slot, event);
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
        
        // 1. MAGMA BATARYASI (3 Magma Bloğu üst üste)
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
                    
                    // Batarya verisini oluştur
                    BatteryData data = new BatteryData("Ateş Topu", fuel, alchemyLevel, hasAmplifier, 
                                                      powerMultiplier, isRedDiamond, isDarkMatter);
                    
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
     * Bataryayı yükleme işlemi (3 blok üst üste)
     */
    private void loadBattery(Player player, Block center, Block below, Block above, 
                            ItemStack handItem, int slot, BatteryData data, PlayerInteractEvent event) {
        event.setCancelled(true);
        
        // 1. Eşyayı al
        if (handItem.getType() != Material.LAVA_BUCKET) { // Lava bucket özel işlenir
            handItem.setAmount(handItem.getAmount() - 1);
        }
        
        // 2. Yapıyı yok et (Blokları kır)
        center.setType(Material.AIR);
        if (below != null) below.setType(Material.AIR);
        if (above != null) above.setType(Material.AIR);
        
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
            
            // BatteryManager metodunu çağır
            batteryManager.fireMagmaBattery(player, data.getFuel(), data.getAlchemyLevel(), 
                                          data.hasAmplifier(), data.getTrainingMultiplier());
            
            // Mastery mesajları ve seviye atlama kontrolü (Listener'a özel)
            handleMasteryMessages(player, "MAGMA_BATTERY", data);
            
        } else if (type.equals("Yıldırım")) {
            batteryManager.fireLightningBattery(player);
        } else if (type.equals("Kara Delik")) {
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
}
