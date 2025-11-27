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
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
            
            // Eğer bu slotta zaten yüklü bir batarya varsa -> İPTAL ET
            if (batteryManager.hasLoadedBattery(player, slot)) {
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
        
        if (type.equals("Ateş Topu")) {
            fireMagmaBattery(player, data);
        } else if (type.equals("Yıldırım")) {
            fireLightningBattery(player);
        } else if (type.equals("Kara Delik")) {
            fireBlackHole(player);
        } else if (type.equals("Anlık Köprü")) {
            createInstantBridge(player);
        } else if (type.equals("Sığınak Küpü")) {
            createInstantBunker(player);
        } else if (type.equals("Yerçekimi Çapası")) {
            fireGravityAnchor(player);
        } else if (type.equals("Toprak Suru")) {
            createEarthWall(player, data.getFuel());
        } else if (type.equals("Manyetik Bozucu")) {
            fireMagneticDisruptor(player);
        } else if (type.equals("Sismik Çekiç")) {
            fireSeismicHammer(player);
        } else if (type.equals("Ozon Kalkanı")) {
            activateOzoneShield(player, player.getLocation());
        } else if (type.equals("Enerji Duvarı")) {
            createEnergyWall(player);
        } else if (type.equals("Lav Hendekçisi")) {
            createLavaTrench(player);
        }
        
        // Ateşlendikten sonra bataryayı sil
        batteryManager.removeBattery(player, slot);
    }
    
    /**
     * Ateş Topu bataryası
     */
    private void fireMagmaBattery(Player p, BatteryData data) {
        int count;
        Material fuel = data.getFuel();
        if (fuel == Material.DIAMOND) count = 5;
        else if (data.isRedDiamond()) count = 20;
        else if (data.isDarkMatter()) count = 50;
        else count = 2;
        
        // Simya Kulesi seviyesine göre güç artışı
        if (data.getAlchemyLevel() > 0) {
            double multiplier = 1.0 + (data.getAlchemyLevel() * 0.1);
            count = (int) (count * multiplier);
        }
        
        // Mastery çarpanı uygula
        count = (int) (count * data.getTrainingMultiplier());
        if (count < 1) count = 1;
        
        float yield = data.hasAmplifier() ? 4.0f : 2.0f;
        yield = (float) (yield * data.getTrainingMultiplier());
        
        for (int i = 0; i < count; i++) {
            Fireball fb = p.launchProjectile(Fireball.class);
            fb.setVelocity(p.getLocation().getDirection().multiply(1.5));
            fb.setYield(yield);
            if (data.getAlchemyLevel() >= 5 && data.getTrainingMultiplier() >= 1.0) {
                fb.setIsIncendiary(true);
            }
        }
        
        // Mastery kaydı (ateşleme sırasında yapılır)
        if (trainingManager != null) {
            trainingManager.recordUse(p.getUniqueId(), "MAGMA_BATTERY");
        }
        
        // Mastery mesajları ve seviye atlama kontrolü
        String masteryMsg = "";
        if (data.getTrainingMultiplier() < 1.0) {
            masteryMsg = " §7[Antrenman Modu]";
        } else if (data.getTrainingMultiplier() > 1.0) {
            int bonusPercent = (int) ((data.getTrainingMultiplier() - 1.0) * 100);
            masteryMsg = " §a[Mastery +%" + bonusPercent + "]";
        }
        
        // Mastery seviye atlama kontrolü
        if (trainingManager != null) {
            int previousLevel = trainingManager.getPreviousMasteryLevel(p.getUniqueId(), "MAGMA_BATTERY");
            int newLevel = trainingManager.getMasteryLevel(p.getUniqueId(), "MAGMA_BATTERY");
            int totalUses = trainingManager.getTotalUses(p.getUniqueId(), "MAGMA_BATTERY");
            boolean nowTrained = trainingManager.isTrained(p.getUniqueId(), "MAGMA_BATTERY");
            
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
                int remaining = trainingManager.getRemainingUses(p.getUniqueId(), "MAGMA_BATTERY");
                double progress = trainingManager.getTrainingProgress(p.getUniqueId(), "MAGMA_BATTERY");
                p.sendMessage("§e[Antrenman] §7Güç: §c" + String.format("%.0f", progress * 100) + "% §7(" + remaining + " kullanım kaldı)");
            } else if (newLevel > 0) {
                p.sendMessage("§a[Mastery Seviye " + newLevel + "] §7Güç: §e" + getMasteryBonusText(newLevel) + " §7(" + totalUses + " kullanım)");
            }
        }
        
        p.sendMessage("§6Ateş topları fırlatıldı! (" + count + " adet)" + 
                     (data.getAlchemyLevel() > 0 ? " [Simya Kulesi Seviye " + data.getAlchemyLevel() + "]" : "") + masteryMsg);
        p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
    }
    
    /**
     * Yıldırım bataryası
     */
    private void fireLightningBattery(Player p) {
        org.bukkit.Location target = p.getTargetBlock(null, 50).getLocation();
        p.getWorld().strikeLightning(target);
        p.sendMessage("§eYıldırım düştü!");
    }
    
    /**
     * Kara Delik bataryası
     */
    private void fireBlackHole(Player p) {
        org.bukkit.Location target = p.getTargetBlock(null, 30).getLocation();
        p.getWorld().createExplosion(target, 0F);
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, target, 1);
        for (org.bukkit.entity.Entity e : target.getWorld().getNearbyEntities(target, 15, 15, 15)) {
            if (e instanceof org.bukkit.entity.LivingEntity && e != p) {
                Vector dir = target.toVector().subtract(e.getLocation().toVector()).normalize().multiply(1.5);
                e.setVelocity(dir);
            }
        }
        p.sendMessage("§5Kara Delik aktif!");
    }
    
    /**
     * Anlık Köprü bataryası
     */
    private void createInstantBridge(Player p) {
        org.bukkit.Location start = p.getLocation().clone().subtract(0, 1, 0);
        Vector dir = p.getLocation().getDirection().setY(0).normalize();
        for (int i = 1; i <= 15; i++) {
            org.bukkit.Location point = start.clone().add(dir.clone().multiply(i));
            if (point.getBlock().getType() == Material.AIR) {
                point.getBlock().setType(Material.PACKED_ICE);
            }
        }
        p.sendMessage("§bBuz Köprüsü kuruldu!");
    }
    
    /**
     * Sığınak Küpü bataryası
     */
    private void createInstantBunker(Player p) {
        org.bukkit.Location center = p.getLocation().clone();
        int r = 2;
        for (int x = -r; x <= r; x++) {
            for (int y = 0; y <= 3; y++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r || y == 3 || y == 0) {
                        org.bukkit.block.Block b = center.clone().add(x, y, z).getBlock();
                        if (b.getType() == Material.AIR) b.setType(Material.COBBLESTONE);
                    }
                }
            }
        }
        p.teleport(center.clone().add(0, 1, 0));
        p.sendMessage("§7Sığınak oluşturuldu!");
    }
    
    /**
     * Yerçekimi Çapası bataryası
     */
    private void fireGravityAnchor(Player p) {
        p.sendMessage("§5Yerçekimi Çapası Aktif!");
        for (org.bukkit.entity.Entity e : p.getNearbyEntities(50, 100, 50)) {
            if (e instanceof Player && ((Player) e).isGliding()) {
                e.setVelocity(new Vector(0, -3, 0));
                ((Player) e).setGliding(false);
                e.sendMessage("§c§lYERÇEKİMİ ÇAPASINA YAKALANDIN!");
            }
        }
    }
    
    /**
     * Toprak Suru bataryası
     */
    private void createEarthWall(Player p, Material material) {
        org.bukkit.Location start = p.getLocation().clone().add(p.getLocation().getDirection().setY(0).normalize().multiply(2));
        boolean isTitanium = ItemManager.TITANIUM_INGOT != null && material == Material.IRON_INGOT && 
                             ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "TITANIUM_INGOT");
        boolean isAdamantite = ItemManager.ADAMANTITE != null && 
                               ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "ADAMANTITE");
        
        int height = isTitanium ? 5 : 3;
        Material wallMat = Material.COBBLESTONE;
        
        if (isAdamantite) {
            wallMat = Material.BARRIER;
            height = 4;
            p.sendMessage("§5Adamantite Enerji Kalkanı oluşturuldu!");
        } else if (isTitanium) {
            wallMat = Material.IRON_BLOCK;
        }
        
        for (int y = 0; y < height; y++) {
            for (int x = -1; x <= 1; x++) {
                org.bukkit.Location blockLoc = start.clone().add(x, y, 0);
                if (blockLoc.getBlock().getType() == Material.AIR) {
                    blockLoc.getBlock().setType(wallMat);
                    if (isAdamantite) {
                        p.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, blockLoc.add(0.5, 0.5, 0.5), 3);
                    }
                }
            }
        }
        if (!isAdamantite) {
            p.sendMessage("§7Toprak Suru oluşturuldu!");
        }
    }
    
    /**
     * Manyetik Bozucu bataryası
     */
    private void fireMagneticDisruptor(Player p) {
        p.sendMessage("§5Manyetik Bozucu Aktif!");
        for (org.bukkit.entity.Entity e : p.getNearbyEntities(20, 20, 20)) {
            if (e instanceof Player && e != p) {
                Player target = (Player) e;
                ItemStack mainHand = target.getInventory().getItemInMainHand();
                if (mainHand != null && mainHand.getType() != Material.AIR) {
                    target.getWorld().dropItemNaturally(target.getLocation(), mainHand.clone());
                    target.getInventory().setItemInMainHand(null);
                    target.sendMessage("§c§lSİLAHIN DÜŞTÜ!");
                }
            }
        }
    }
    
    /**
     * Sismik Çekiç bataryası
     */
    private void fireSeismicHammer(Player p) {
        org.bukkit.Location target = p.getTargetBlock(null, 30).getLocation();
        p.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, target, 5);
        p.sendMessage("§6Sismik Çekiç Aktif! Yer altı titreşimleri gönderildi!");
        // Hiçlik Solucanı için titreşim sinyali
        me.mami.stratocraft.Main plugin = me.mami.stratocraft.Main.getInstance();
        if (plugin != null && plugin.getDisasterManager() != null) {
            plugin.getDisasterManager().forceWormSurface(target);
        }
    }
    
    /**
     * Ozon Kalkanı bataryası
     */
    private void activateOzoneShield(Player p, org.bukkit.Location center) {
        int radius = 15;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x*x + z*z <= radius*radius) {
                    org.bukkit.Location loc = center.clone().add(x, 0, z);
                    if (loc.getBlock().getType() == Material.AIR) {
                        loc.getBlock().setType(Material.BARRIER);
                        p.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, loc, 1);
                    }
                }
            }
        }
        p.sendMessage("§bOzon Kalkanı aktif! Güneş Fırtınası koruması sağlandı.");
    }
    
    /**
     * Enerji Duvarı bataryası
     */
    private void createEnergyWall(Player p) {
        org.bukkit.Location start = p.getLocation().clone().add(p.getLocation().getDirection().setY(0).normalize().multiply(2));
        for (int y = 0; y < 5; y++) {
            for (int x = -2; x <= 2; x++) {
                org.bukkit.Location loc = start.clone().add(x, y, 0);
                if (loc.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.BARRIER);
                    p.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, loc, 3);
                }
            }
        }
        p.sendMessage("§bEnerji Duvarı oluşturuldu!");
    }
    
    /**
     * Lav Hendekçisi bataryası
     */
    private void createLavaTrench(Player p) {
        org.bukkit.Location start = p.getLocation().clone().add(p.getLocation().getDirection().setY(0).normalize().multiply(3));
        for (int i = 0; i < 10; i++) {
            org.bukkit.Location loc = start.clone().add(i, -1, 0);
            if (loc.getBlock().getType() != Material.LAVA) {
                loc.getBlock().setType(Material.LAVA);
            }
        }
        p.sendMessage("§cLav Hendekçisi kuruldu!");
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
