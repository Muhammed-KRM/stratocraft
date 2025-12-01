package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BossManager;
import me.mami.stratocraft.manager.DifficultyManager;
import me.mami.stratocraft.manager.TamingManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Canlı Eğitme Sistemi Dinleyicisi
 * - Ritüel kontrolü ve canlı eğitme
 * - Binme sistemi
 * - Eğitilmiş canlı ölüm kontrolü
 */
public class TamingListener implements Listener {
    private final TamingManager tamingManager;
    private final DifficultyManager difficultyManager;
    private final BossManager bossManager;
    
    public TamingListener(TamingManager tamingManager, DifficultyManager difficultyManager, BossManager bossManager) {
        this.tamingManager = tamingManager;
        this.difficultyManager = difficultyManager;
        this.bossManager = bossManager;
    }
    
    /**
     * Ritüel aktifleştirme (blok deseni + item ile sağ tık)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onTamingRitual(PlayerInteractEvent event) {
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
        
        // Yakındaki canlıyı bul (3 blok yarıçap)
        LivingEntity targetEntity = findNearbyCreature(clickedBlock.getLocation(), 3);
        if (targetEntity == null) {
            return; // Yakında canlı yok
        }
        
        // Canlı eğitilebilir mi?
        if (!tamingManager.canBeTamed(targetEntity)) {
            return;
        }
        
        // Boss mu?
        BossManager.BossData bossData = bossManager != null ? bossManager.getBossData(targetEntity.getUniqueId()) : null;
        int difficultyLevel = difficultyManager.getDifficultyLevel(clickedBlock.getLocation());
        
        if (bossData != null) {
            // Boss eğitme ritüeli
            handleBossTamingRitual(player, clickedBlock, targetEntity, bossData.getType(), item);
        } else {
            // Normal canlı eğitme ritüeli (seviyeye göre)
            handleNormalTamingRitual(player, clickedBlock, targetEntity, difficultyLevel, item);
        }
        
        event.setCancelled(true);
    }
    
    /**
     * Normal canlı eğitme ritüeli
     */
    private void handleNormalTamingRitual(Player player, Block centerBlock, LivingEntity entity, 
                                          int difficultyLevel, ItemStack item) {
        // Ritüel deseni kontrol et
        if (!tamingManager.checkRitualPattern(centerBlock, difficultyLevel)) {
            player.sendMessage("§cRitüel deseni yanlış! Seviye " + difficultyLevel + " için doğru deseni yapmalısın.");
            showRitualPattern(player, difficultyLevel);
            return;
        }
        
        // Ritüel aktifleştirme itemi kontrolü
        Material requiredItem = tamingManager.getRitualActivationItem(difficultyLevel);
        if (item.getType() != requiredItem) {
            player.sendMessage("§cRitüel için " + requiredItem.name() + " gerekli!");
            return;
        }
        
        // Cooldown kontrolü
        Location ritualLoc = centerBlock.getLocation();
        if (tamingManager.isOnCooldown(ritualLoc)) {
            player.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Canlıyı eğit
        if (tamingManager.tameCreature(entity, player.getUniqueId(), difficultyLevel)) {
            // Item tüket
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Ritüel efekti
            Location loc = entity.getLocation();
            loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 30, 1, 1, 1, 0.1);
            loc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, loc, 20, 0.5, 1, 0.5, 0.1);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_WOLF_WHINE, 1.0f, 1.0f);
            
            player.sendMessage("§a§lCanlı eğitildi!");
            
            // Cooldown kaydet
            tamingManager.setCooldown(ritualLoc);
        } else {
            player.sendMessage("§cCanlı eğitilemedi!");
        }
    }
    
    /**
     * Boss eğitme ritüeli
     */
    private void handleBossTamingRitual(Player player, Block centerBlock, LivingEntity entity,
                                       BossManager.BossType bossType, ItemStack item) {
        // Ritüel deseni kontrol et
        if (!tamingManager.checkBossRitualPattern(centerBlock, bossType)) {
            player.sendMessage("§cRitüel deseni yanlış! " + bossManager.getBossDisplayName(bossType) + " için doğru deseni yapmalısın.");
            return;
        }
        
        // Ritüel aktifleştirme itemi kontrolü
        Material requiredItem = tamingManager.getBossRitualActivationItem(bossType);
        if (item.getType() != requiredItem) {
            player.sendMessage("§cRitüel için " + requiredItem.name() + " gerekli!");
            return;
        }
        
        // Cooldown kontrolü
        Location ritualLoc = centerBlock.getLocation();
        if (tamingManager.isOnCooldown(ritualLoc)) {
            player.sendMessage("§cRitüel henüz hazır değil! Lütfen bekleyin.");
            return;
        }
        
        // Boss'u eğit
        int difficultyLevel = difficultyManager.getDifficultyLevel(centerBlock.getLocation());
        if (tamingManager.tameCreature(entity, player.getUniqueId(), difficultyLevel)) {
            // Item tüket
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Ritüel efekti
            Location loc = entity.getLocation();
            loc.getWorld().spawnParticle(org.bukkit.Particle.HEART, loc, 50, 2, 2, 2, 0.2);
            loc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, loc, 30, 1, 1, 1, 0.1);
            loc.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, loc, 20, 1, 1, 1, 0.3);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            loc.getWorld().playSound(loc, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            
            player.sendMessage("§a§l" + bossManager.getBossDisplayName(bossType) + " eğitildi!");
            
            // Cooldown kaydet
            tamingManager.setCooldown(ritualLoc);
        } else {
            player.sendMessage("§cBoss eğitilemedi!");
        }
    }
    
    /**
     * Shift+Sağ tık ile takip edilecek kişi belirleme
     * Normal sağ tık ile binme
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRideCreature(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getRightClicked();
        Player player = event.getPlayer();
        
        // Eğitilmiş mi?
        if (!tamingManager.isTamed(entity)) {
            return;
        }
        
        // Kullanabilir mi? (Sahip veya aynı klan)
        if (!tamingManager.canUseCreature(entity, player.getUniqueId())) {
            player.sendMessage("§cBu canlıyı kullanamazsın! Sadece sahip veya klan üyeleri kullanabilir.");
            return;
        }
        
        // Shift+Sağ tık = Takip edilecek kişi belirle
        if (player.isSneaking()) {
            // Aynı klan kontrolü
            me.mami.stratocraft.manager.ClanManager clanManager = plugin.getClanManager();
            if (clanManager != null) {
                java.util.UUID ownerId = tamingManager.getOwner(entity);
                me.mami.stratocraft.model.Clan ownerClan = clanManager.getClanByPlayer(ownerId);
                me.mami.stratocraft.model.Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
                
                if (ownerClan == null || playerClan == null || !ownerClan.equals(playerClan)) {
                    player.sendMessage("§cBu canlıyı sadece aynı klan üyeleri takip edebilir!");
                    return;
                }
            }
            
            // Takip edilecek kişiyi ayarla
            if (tamingManager.setFollowingTarget(entity, player.getUniqueId(), player.getUniqueId())) {
                player.sendMessage("§a§lCanlı artık seni takip ediyor!");
                entity.getWorld().spawnParticle(org.bukkit.Particle.HEART, entity.getLocation(), 5, 0.5, 1, 0.5, 0.1);
            }
            event.setCancelled(true);
            return;
        }
        
        // Normal sağ tık = Binme
        // Binilebilir mi?
        if (!tamingManager.isRideable(entity)) {
            player.sendMessage("§eBu canlıya binilemez!");
            return;
        }
        
        // Zaten biniliyor mu?
        if (!entity.getPassengers().isEmpty()) {
            player.sendMessage("§cBu canlıya zaten biniliyor!");
            return;
        }
        
        // Bin
        tamingManager.makeRideable(entity, player);
        player.sendMessage("§a§lCanlıya bindin!");
    }
    
    /**
     * Eğitilmiş canlı ölüm kontrolü
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onTamedCreatureDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (tamingManager.isTamed(entity)) {
            // Eğitilmiş canlı öldü - listeden kaldır
            tamingManager.removeTamedCreature(entity.getUniqueId());
        }
    }
    
    /**
     * Koruma sistemi - takip edilen kişiye saldırıldığında
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onProtectedPlayerDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        
        // Yakındaki eğitilmiş canlıları kontrol et
        for (org.bukkit.entity.Entity nearby : victim.getWorld().getNearbyEntities(victim.getLocation(), 20, 20, 20)) {
            if (!(nearby instanceof LivingEntity)) {
                continue;
            }
            
            LivingEntity creature = (LivingEntity) nearby;
            
            // Eğitilmiş mi?
            if (!tamingManager.isTamed(creature)) {
                continue;
            }
            
            // Bu canlı bu oyuncuyu takip ediyor mu?
            java.util.UUID followingTarget = tamingManager.getFollowingTarget(creature);
            if (followingTarget == null || !followingTarget.equals(victim.getUniqueId())) {
                continue;
            }
            
            // Canlı oturtulmuş mu? (biniliyor mu?)
            if (!creature.getPassengers().isEmpty()) {
                continue; // Oturtulmuş, saldırmaz
            }
            
            // Saldıran kişi
            org.bukkit.entity.Entity attacker = event.getDamager();
            if (attacker instanceof Player) {
                Player attackerPlayer = (Player) attacker;
                
                // Aynı klan mı kontrol et
                me.mami.stratocraft.manager.ClanManager clanManager = plugin.getClanManager();
                if (clanManager != null) {
                    me.mami.stratocraft.model.Clan victimClan = clanManager.getClanByPlayer(victim.getUniqueId());
                    me.mami.stratocraft.model.Clan attackerClan = clanManager.getClanByPlayer(attackerPlayer.getUniqueId());
                    
                    if (victimClan != null && attackerClan != null && victimClan.equals(attackerClan)) {
                        continue; // Aynı klan, saldırmaz
                    }
                }
                
                // Canlı saldırıcıya saldırır
                if (creature instanceof org.bukkit.entity.Mob) {
                    ((org.bukkit.entity.Mob) creature).setTarget(attackerPlayer);
                    victim.sendMessage("§a§l" + creature.getCustomName() + " seni koruyor!");
                }
            } else if (attacker instanceof LivingEntity) {
                // Mob saldırısı - canlı saldırıcıya saldırır
                if (creature instanceof org.bukkit.entity.Mob) {
                    ((org.bukkit.entity.Mob) creature).setTarget((LivingEntity) attacker);
                }
            }
        }
    }
    
    /**
     * Yakındaki canlıyı bul
     */
    private LivingEntity findNearbyCreature(Location loc, double radius) {
        for (org.bukkit.entity.Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (nearby instanceof LivingEntity && !(nearby instanceof Player)) {
                return (LivingEntity) nearby;
            }
        }
        return null;
    }
    
    /**
     * Ritüel desenini göster
     */
    private void showRitualPattern(Player player, int difficultyLevel) {
        player.sendMessage("§6=== Seviye " + difficultyLevel + " Eğitme Ritüeli ===");
        player.sendMessage("§7Merkez bloğa sağ tıkla ve doğru deseni yap:");
        
        Material[][] pattern = tamingManager.getRitualPatternForLevel(difficultyLevel);
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
        
        Material activationItem = tamingManager.getRitualActivationItem(difficultyLevel);
        player.sendMessage("§7Aktifleştirme itemi: §e" + activationItem.name());
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
            case BEDROCK: return "§0B";
            case WHEAT: return "§eW";
            case CARROT: return "§6C";
            case GOLDEN_APPLE: return "§6G";
            case ENCHANTED_GOLDEN_APPLE: return "§bE";
            case NETHER_STAR: return "§d★";
            default: return "?";
        }
    }
    
}

