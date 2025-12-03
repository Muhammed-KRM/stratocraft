package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BossManager;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Boss Sistemi Dinleyicisi
 * - Ritüel kontrolü ve boss çağırma
 * - Boss hasar kontrolü (zayıf noktalar)
 * - Boss ölüm kontrolü
 */
public class BossListener implements Listener {
    private final BossManager bossManager;
    private final me.mami.stratocraft.Main plugin;
    
    public BossListener(BossManager bossManager, me.mami.stratocraft.Main plugin) {
        this.bossManager = bossManager;
        this.plugin = plugin;
    }
    
    /**
     * Çağırma Çekirdeği yerleştirme (item ile bloğa sağ tık)
     * Yüksek priority ile önce çalışır, böylece ritüel aktifleştirme ile çakışmaz
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSummonCorePlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Çağırma Çekirdeği kontrolü
        if (!ItemManager.isCustomItem(item, "SUMMON_CORE")) {
            return;
        }
        
        // Bloğun üstüne yerleştir
        Block targetBlock = clickedBlock.getRelative(org.bukkit.block.BlockFace.UP);
        if (targetBlock.getType() != Material.AIR) {
            player.sendMessage("§cBuraya yerleştirilemez! Blok boş olmalı.");
            event.setCancelled(true);
            return;
        }
        
        // Çağırma Çekirdeği olarak işaretle
        targetBlock.setType(Material.END_CRYSTAL);
        targetBlock.setMetadata("SummonCore", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        
        // Item tüket
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        player.sendMessage("§a§lÇağırma Çekirdeği yerleştirildi!");
        player.sendMessage("§7Etrafına boss seviyesine göre yapıları yap ve aktifleştirme itemi ile sağ tıkla.");
        
        // Efekt
        org.bukkit.Location loc = targetBlock.getLocation().add(0.5, 0.5, 0.5);
        loc.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, loc, 20, 0.5, 0.5, 0.5, 0.3);
        loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        
        event.setCancelled(true);
    }
    
    /**
     * Ritüel aktifleştirme (blok deseni + item ile sağ tık)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRitualActivate(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // Merkez bloğun Çağırma Çekirdeği olup olmadığını kontrol et
        Block centerBlock = clickedBlock;
        if (!centerBlock.hasMetadata("SummonCore")) {
            return;
        }
        
        // Hangi boss için ritüel? (aktifleştirme item'ına göre)
        BossManager.BossType bossType = getBossTypeFromItem(item.getType(), centerBlock);
        
        // NETHER_STAR için özel kontrol (ritüel desenine göre boss tipi belirlenir)
        if (item.getType() == Material.NETHER_STAR && bossType == null) {
            // Ritüel desenini kontrol et - hangi boss deseni varsa onu seç
            if (bossManager.checkRitualPattern(centerBlock, BossManager.BossType.TITAN_GOLEM)) {
                bossType = BossManager.BossType.TITAN_GOLEM;
            } else if (bossManager.checkRitualPattern(centerBlock, BossManager.BossType.CHAOS_GOD)) {
                bossType = BossManager.BossType.CHAOS_GOD;
            } else {
                player.sendMessage("§cRitüel deseni yanlış! Nether Star için Titan Golem veya Khaos Tanrısı deseni yapmalısın.");
                player.sendMessage("§7Titan Golem: 7x7 Obsidian + Merkez Netherite Block");
                player.sendMessage("§7Khaos Tanrısı: 9x9 Bedrock + Merkez End Stone Bricks");
                return;
            }
        }
        
        if (bossType == null) {
            return;
        }
        
        // Ritüel deseni kontrol et (NETHER_STAR için zaten kontrol edildi)
        if (item.getType() != Material.NETHER_STAR) {
            if (!bossManager.checkRitualPattern(centerBlock, bossType)) {
                player.sendMessage("§cRitüel deseni yanlış! " + bossManager.getBossDisplayName(bossType) + " için doğru deseni yapmalısın.");
                showRitualPattern(player, bossType);
                return;
            }
        }
        
        // Ritüel aktifleştirme itemi kontrolü
        Material requiredItem = bossManager.getRitualActivationItem(bossType);
        if (item.getType() != requiredItem) {
            player.sendMessage("§cRitüel için " + requiredItem.name() + " gerekli!");
            return;
        }
        
        // Boss spawn et
        Location spawnLoc = centerBlock.getLocation().add(0.5, 1, 0.5);
        if (bossManager.spawnBossFromRitual(spawnLoc, bossType, player.getUniqueId())) {
            // Item tüket
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Ritüel efekti
            centerBlock.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, spawnLoc, 100, 1, 1, 1, 0.5);
            centerBlock.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, spawnLoc, 50, 1, 1, 1, 0.3);
            centerBlock.getWorld().playSound(spawnLoc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            centerBlock.getWorld().playSound(spawnLoc, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
            
            player.sendMessage("§a§l" + bossManager.getBossDisplayName(bossType) + " çağrıldı!");
            event.setCancelled(true);
        } else {
            player.sendMessage("§cRitüel başarısız! Cooldown aktif olabilir.");
        }
    }
    
    /**
     * Item'dan boss tipi bul
     * NETHER_STAR için ritüel desenini kontrol et (TITAN_GOLEM veya CHAOS_GOD)
     */
    private BossManager.BossType getBossTypeFromItem(Material material, Block clickedBlock) {
        switch (material) {
            case ROTTEN_FLESH: return BossManager.BossType.GOBLIN_KING;
            case IRON_SWORD: return BossManager.BossType.ORC_CHIEF;
            case STONE_AXE: return BossManager.BossType.TROLL_KING;
            case DRAGON_EGG: return BossManager.BossType.DRAGON;
            case BONE: return BossManager.BossType.TREX;
            case ENDER_EYE: return BossManager.BossType.CYCLOPS;
            case NETHER_STAR: 
                // Nether Star iki boss için kullanılıyor, ritüel desenini kontrol et
                // Önce Titan Golem desenini kontrol et (7x7)
                if (clickedBlock != null && bossManager.checkRitualPattern(clickedBlock, BossManager.BossType.TITAN_GOLEM)) {
                    return BossManager.BossType.TITAN_GOLEM;
                }
                // Sonra Chaos God desenini kontrol et (9x9)
                if (clickedBlock != null && bossManager.checkRitualPattern(clickedBlock, BossManager.BossType.CHAOS_GOD)) {
                    return BossManager.BossType.CHAOS_GOD;
                }
                // Varsayılan olarak null dön (desen kontrolü yapılacak)
                return null;
            case BLAZE_ROD: return BossManager.BossType.HELL_DRAGON;
            case HEART_OF_THE_SEA: return BossManager.BossType.HYDRA;
            default: return null;
        }
    }
    
    /**
     * Ritüel desenini göster
     */
    private void showRitualPattern(Player player, BossManager.BossType bossType) {
        player.sendMessage("§6=== " + bossManager.getBossDisplayName(bossType) + " Ritüel Deseni ===");
        player.sendMessage("§7Merkez bloğa sağ tıkla ve doğru deseni yap:");
        
        Material[][] pattern = getRitualPatternForDisplay(bossType);
        if (pattern != null) {
            int size = pattern.length;
            for (int x = 0; x < size; x++) {
                StringBuilder line = new StringBuilder("§7");
                for (int z = 0; z < size; z++) {
                    Material mat = pattern[x][z];
                    if (mat == null) {
                        line.append("· ");
                    } else {
                        line.append(getMaterialSymbol(mat)).append(" ");
                    }
                }
                player.sendMessage(line.toString());
            }
        }
        
        Material activationItem = bossManager.getRitualActivationItem(bossType);
        player.sendMessage("§7Aktifleştirme itemi: §e" + activationItem.name());
    }
    
    /**
     * Ritüel deseni al (gösterim için)
     */
    private Material[][] getRitualPatternForDisplay(BossManager.BossType bossType) {
        return bossManager.getRitualPattern(bossType);
    }
    
    /**
     * Material sembolü
     */
    private String getMaterialSymbol(Material mat) {
        switch (mat) {
            case COBBLESTONE: return "§7C";
            case STONE: return "§8S";
            case STONE_BRICKS: return "§8B";
            case OBSIDIAN: return "§5O";
            case GOLD_BLOCK: return "§6G";
            case IRON_BLOCK: return "§fI";
            case DIAMOND_BLOCK: return "§bD";
            case EMERALD_BLOCK: return "§aE";
            case NETHERITE_BLOCK: return "§4N";
            case NETHERRACK: return "§cN";
            case PRISMARINE: return "§3P";
            case BEDROCK: return "§0B";
            case BEACON: return "§b★";
            case CONDUIT: return "§b●";
            case END_STONE_BRICKS: return "§eE";
            default: return "?";
        }
    }
    
    /**
     * Boss hasar kontrolü (zayıf noktalar)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        BossManager.BossData bossData = bossManager.getBossData(entity.getUniqueId());
        
        if (bossData == null) {
            return;
        }
        
        // Zayıflık çarpanı uygula
        double multiplier = bossManager.getWeaknessMultiplier(bossData, event.getCause());
        if (multiplier > 1.0) {
            // Base damage'i artır (armor koruması da olsun)
            double newDamage = event.getDamage() * multiplier;
            event.setDamage(newDamage);
            
            // Zayıf nokta vuruldu efekti
            entity.getWorld().spawnParticle(org.bukkit.Particle.CRIT_MAGIC, entity.getLocation(), 20, 0.5, 1, 0.5, 0.1);
            entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.5f);
            
            // Yakındaki oyunculara mesaj
            for (org.bukkit.entity.Player nearby : entity.getWorld().getPlayers()) {
                if (nearby.getLocation().distance(entity.getLocation()) <= 20) {
                    nearby.sendMessage("§c§lZAYIF NOKTASI VURULDU! " + String.format("%.1f", multiplier) + "x hasar!");
                }
            }
        }
        
        // Zehir zayıflığı kontrolü
        if (bossData.getWeaknesses().contains(BossManager.BossWeakness.POISON)) {
            if (entity.hasPotionEffect(PotionEffectType.POISON)) {
                // Zehir hasarı artır
                PotionEffect poison = entity.getPotionEffect(PotionEffectType.POISON);
                if (poison != null) {
                    // Ekstra hasar ver
                    entity.damage(2.0 * (poison.getAmplifier() + 1));
                }
            }
        }
    }
    
    /**
     * Boss ölüm kontrolü
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        BossManager.BossData bossData = bossManager.getBossData(entity.getUniqueId());
        
        if (bossData == null) {
            return;
        }
        
        // Boss öldü
        String bossName = bossManager.getBossDisplayName(bossData.getType());
        
        // Duyuru
        entity.getWorld().getPlayers().forEach(p -> {
            if (p.getLocation().distance(entity.getLocation()) <= 50) {
                p.sendTitle("§c§l" + bossName + " ÖLDÜ!", "§eZafer!", 20, 100, 20);
                p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.8f);
            }
        });
        
        // ========== TARİF KİTAPLARI DÜŞÜRME ==========
        // Bosslardan hem yapı tarifleri hem de özel zırh/silah tarifleri düşer
        dropRequiredRecipeBook(event, bossData.getType());
        dropSpecialItemRecipeBooks(event, bossData.getType());
        
        // ========== BOSS ÖZEL İTEMLERİ DÜŞÜRME ==========
        // Her boss kendi özel itemini düşürür
        dropBossItems(event, bossData.getType());
        
        // Boss'u listeden kaldır
        bossManager.removeBoss(entity.getUniqueId());
        
        // Ölüm efekti
        Location loc = entity.getLocation();
        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, loc, 5, 1, 1, 1, 0.1);
        loc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, loc, 50, 2, 2, 2, 0.2);
    }
    
    /**
     * Boss'tan gerekli tarif kitabı düşür (aktifleştirme için gerekenler)
     * Boss seviyesine göre tarif seviyesi artar
     */
    private void dropRequiredRecipeBook(EntityDeathEvent event, BossManager.BossType bossType) {
        // Boss seviyesini belirle
        int bossLevel = getBossLevel(bossType);
        
        // Gerekli tarifler (aktifleştirme için gerekenler)
        // Sadece bazı yapılar çalışması için tarif gerektirir
        java.util.List<ItemStack> requiredRecipes = new java.util.ArrayList<>();
        
        // Seviye 1 bosslar - Temel yapılar
        if (bossLevel >= 1) {
            if (ItemManager.RECIPE_ALCHEMY_TOWER != null) requiredRecipes.add(ItemManager.RECIPE_ALCHEMY_TOWER);
            if (ItemManager.RECIPE_HEALING_BEACON != null) requiredRecipes.add(ItemManager.RECIPE_HEALING_BEACON);
        }
        
        // Seviye 2 bosslar - Orta seviye yapılar
        if (bossLevel >= 2) {
            if (ItemManager.RECIPE_POISON_REACTOR != null) requiredRecipes.add(ItemManager.RECIPE_POISON_REACTOR);
            if (ItemManager.RECIPE_WALL_GENERATOR != null) requiredRecipes.add(ItemManager.RECIPE_WALL_GENERATOR);
            if (ItemManager.RECIPE_AUTO_TURRET != null) requiredRecipes.add(ItemManager.RECIPE_AUTO_TURRET);
        }
        
        // Seviye 3 bosslar - İleri seviye yapılar
        if (bossLevel >= 3) {
            if (ItemManager.RECIPE_TECTONIC_STABILIZER != null) requiredRecipes.add(ItemManager.RECIPE_TECTONIC_STABILIZER);
            if (ItemManager.RECIPE_SIEGE_FACTORY != null) requiredRecipes.add(ItemManager.RECIPE_SIEGE_FACTORY);
            if (ItemManager.RECIPE_GRAVITY_WELL != null) requiredRecipes.add(ItemManager.RECIPE_GRAVITY_WELL);
            if (ItemManager.RECIPE_GLOBAL_MARKET_GATE != null) requiredRecipes.add(ItemManager.RECIPE_GLOBAL_MARKET_GATE);
        }
        
        // Seviye 4 bosslar - Çok ileri seviye yapılar
        if (bossLevel >= 4) {
            if (ItemManager.RECIPE_LAVA_TRENCHER != null) requiredRecipes.add(ItemManager.RECIPE_LAVA_TRENCHER);
            if (ItemManager.RECIPE_DRONE_STATION != null) requiredRecipes.add(ItemManager.RECIPE_DRONE_STATION);
            if (ItemManager.RECIPE_TELEPORTER != null) requiredRecipes.add(ItemManager.RECIPE_TELEPORTER);
            if (ItemManager.RECIPE_OIL_REFINERY != null) requiredRecipes.add(ItemManager.RECIPE_OIL_REFINERY);
        }
        
        // Seviye 5 bosslar - Efsanevi yapılar
        if (bossLevel >= 5) {
            if (ItemManager.RECIPE_WEATHER_MACHINE != null) requiredRecipes.add(ItemManager.RECIPE_WEATHER_MACHINE);
            if (ItemManager.RECIPE_INVISIBILITY_CLOAK != null) requiredRecipes.add(ItemManager.RECIPE_INVISIBILITY_CLOAK);
        }
        
        if (requiredRecipes.isEmpty()) {
            return;
        }
        
        // Boss seviyesine göre düşürme şansı
        double dropChance = 0.3 + (bossLevel * 0.1); // Seviye 1: %40, Seviye 5: %80
        dropChance = Math.min(dropChance, 0.9); // Maksimum %90
        
        if (new java.util.Random().nextDouble() < dropChance) {
            // Rastgele bir gerekli tarif seç
            ItemStack randomRecipe = requiredRecipes.get(new java.util.Random().nextInt(requiredRecipes.size()));
            event.getDrops().add(randomRecipe.clone());
        }
    }
    
    /**
     * Boss seviyesini belirle
     */
    private int getBossLevel(BossManager.BossType bossType) {
        switch (bossType) {
            case GOBLIN_KING:
            case ORC_CHIEF:
                return 1;
            case TROLL_KING:
                return 2;
            case DRAGON:
            case TREX:
            case CYCLOPS:
                return 3;
            case TITAN_GOLEM:
            case PHOENIX:
            case HYDRA:
            case HELL_DRAGON:
                return 4;
            case VOID_DRAGON:
            case CHAOS_TITAN:
            case CHAOS_GOD:
                return 5;
            default:
                return 1;
        }
    }
    
    /**
     * Boss'tan özel zırh/silah tarif kitapları düşür
     */
    private void dropSpecialItemRecipeBooks(EntityDeathEvent event, BossManager.BossType bossType) {
        int bossLevel = getBossLevel(bossType);
        
        // Boss seviyesine göre tarif seviyesi belirlenir
        // Seviye 1 bosslar -> Seviye 1 tarifler
        // Seviye 2 bosslar -> Seviye 2 tarifler
        // vb.
        
        java.util.List<ItemStack> recipeBooks = new java.util.ArrayList<>();
        
        // Seviye 1 tarifler
        if (bossLevel >= 1) {
            if (ItemManager.RECIPE_ARMOR_L1_1 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L1_1);
            if (ItemManager.RECIPE_ARMOR_L1_2 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L1_2);
            if (ItemManager.RECIPE_ARMOR_L1_3 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L1_3);
            if (ItemManager.RECIPE_ARMOR_L1_4 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L1_4);
            if (ItemManager.RECIPE_ARMOR_L1_5 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L1_5);
            if (ItemManager.RECIPE_WEAPON_L1_1 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L1_1);
            if (ItemManager.RECIPE_WEAPON_L1_2 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L1_2);
            if (ItemManager.RECIPE_WEAPON_L1_3 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L1_3);
            if (ItemManager.RECIPE_WEAPON_L1_4 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L1_4);
            if (ItemManager.RECIPE_WEAPON_L1_5 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L1_5);
        }
        
        // Seviye 2 tarifler
        if (bossLevel >= 2) {
            if (ItemManager.RECIPE_ARMOR_L2_1 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L2_1);
            if (ItemManager.RECIPE_ARMOR_L2_2 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L2_2);
            if (ItemManager.RECIPE_ARMOR_L2_3 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L2_3);
            if (ItemManager.RECIPE_ARMOR_L2_4 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L2_4);
            if (ItemManager.RECIPE_ARMOR_L2_5 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L2_5);
            if (ItemManager.RECIPE_WEAPON_L2_1 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L2_1);
            if (ItemManager.RECIPE_WEAPON_L2_2 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L2_2);
            if (ItemManager.RECIPE_WEAPON_L2_3 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L2_3);
            if (ItemManager.RECIPE_WEAPON_L2_4 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L2_4);
            if (ItemManager.RECIPE_WEAPON_L2_5 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L2_5);
        }
        
        // Seviye 3 tarifler
        if (bossLevel >= 3) {
            if (ItemManager.RECIPE_ARMOR_L3_1 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L3_1);
            if (ItemManager.RECIPE_ARMOR_L3_2 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L3_2);
            if (ItemManager.RECIPE_ARMOR_L3_3 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L3_3);
            if (ItemManager.RECIPE_ARMOR_L3_4 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L3_4);
            if (ItemManager.RECIPE_ARMOR_L3_5 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L3_5);
            if (ItemManager.RECIPE_WEAPON_L3_1 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L3_1);
            if (ItemManager.RECIPE_WEAPON_L3_2 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L3_2);
            if (ItemManager.RECIPE_WEAPON_L3_3 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L3_3);
            if (ItemManager.RECIPE_WEAPON_L3_4 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L3_4);
            if (ItemManager.RECIPE_WEAPON_L3_5 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L3_5);
        }
        
        // Seviye 4 tarifler
        if (bossLevel >= 4) {
            if (ItemManager.RECIPE_ARMOR_L4_1 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L4_1);
            if (ItemManager.RECIPE_ARMOR_L4_2 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L4_2);
            if (ItemManager.RECIPE_ARMOR_L4_3 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L4_3);
            if (ItemManager.RECIPE_ARMOR_L4_4 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L4_4);
            if (ItemManager.RECIPE_ARMOR_L4_5 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L4_5);
            if (ItemManager.RECIPE_WEAPON_L4_1 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L4_1);
            if (ItemManager.RECIPE_WEAPON_L4_2 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L4_2);
            if (ItemManager.RECIPE_WEAPON_L4_3 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L4_3);
            if (ItemManager.RECIPE_WEAPON_L4_4 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L4_4);
            if (ItemManager.RECIPE_WEAPON_L4_5 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L4_5);
        }
        
        // Seviye 5 tarifler
        if (bossLevel >= 5) {
            if (ItemManager.RECIPE_ARMOR_L5_1 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L5_1);
            if (ItemManager.RECIPE_ARMOR_L5_2 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L5_2);
            if (ItemManager.RECIPE_ARMOR_L5_3 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L5_3);
            if (ItemManager.RECIPE_ARMOR_L5_4 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L5_4);
            if (ItemManager.RECIPE_ARMOR_L5_5 != null) recipeBooks.add(ItemManager.RECIPE_ARMOR_L5_5);
            if (ItemManager.RECIPE_WEAPON_L5_1 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L5_1);
            if (ItemManager.RECIPE_WEAPON_L5_2 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L5_2);
            if (ItemManager.RECIPE_WEAPON_L5_3 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L5_3);
            if (ItemManager.RECIPE_WEAPON_L5_4 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L5_4);
            if (ItemManager.RECIPE_WEAPON_L5_5 != null) recipeBooks.add(ItemManager.RECIPE_WEAPON_L5_5);
        }
        
        if (recipeBooks.isEmpty()) {
            return;
        }
        
        // Boss seviyesine göre düşürme şansı
        double dropChance = 0.2 + (bossLevel * 0.1); // Seviye 1: %30, Seviye 5: %70
        dropChance = Math.min(dropChance, 0.8); // Maksimum %80
        
        if (new java.util.Random().nextDouble() < dropChance) {
            // Rastgele bir tarif seç
            ItemStack randomRecipe = recipeBooks.get(new java.util.Random().nextInt(recipeBooks.size()));
            event.getDrops().add(randomRecipe.clone());
        }
    }
    
    /**
     * Boss'tan özel item düşür (her boss kendi itemini düşürür)
     */
    private void dropBossItems(EntityDeathEvent event, BossManager.BossType bossType) {
        ItemStack bossItem = null;
        
        switch (bossType) {
            case GOBLIN_KING:
                bossItem = ItemManager.GOBLIN_CROWN;
                break;
            case ORC_CHIEF:
                bossItem = ItemManager.ORC_AMULET;
                break;
            case TROLL_KING:
                bossItem = ItemManager.TROLL_HEART;
                break;
            case DRAGON:
            case HELL_DRAGON:
                bossItem = ItemManager.DRAGON_SCALE;
                break;
            case TREX:
                bossItem = ItemManager.TREX_TOOTH;
                break;
            case CYCLOPS:
                bossItem = ItemManager.CYCLOPS_EYE;
                break;
            case TITAN_GOLEM:
                bossItem = ItemManager.TITAN_CORE;
                break;
            case PHOENIX:
                bossItem = ItemManager.PHOENIX_FEATHER;
                break;
            case HYDRA:
                bossItem = ItemManager.KRAKEN_TENTACLE;
                break;
            case VOID_DRAGON:
            case CHAOS_TITAN:
            case CHAOS_GOD:
                bossItem = ItemManager.DEMON_LORD_HORN;
                break;
            default:
                return;
        }
        
        if (bossItem != null) {
            // %100 şansla boss item'i düşür
            event.getDrops().add(bossItem.clone());
        }
    }
}

