package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BossManager;
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
    
    public BossListener(BossManager bossManager) {
        this.bossManager = bossManager;
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
        
        // Hangi boss için ritüel?
        Block centerBlock = clickedBlock;
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
        
        // Özel drop'lar (ileride eklenebilir)
        // event.getDrops().add(...);
        
        // Boss'u listeden kaldır
        bossManager.removeBoss(entity.getUniqueId());
        
        // Ölüm efekti
        Location loc = entity.getLocation();
        loc.getWorld().spawnParticle(org.bukkit.Particle.EXPLOSION_LARGE, loc, 5, 1, 1, 1, 0.1);
        loc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, loc, 50, 2, 2, 2, 0.2);
    }
}

