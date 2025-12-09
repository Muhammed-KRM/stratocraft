package me.mami.stratocraft.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;

public class CaravanManager {
    private final Map<UUID, Entity> activeCaravans = new HashMap<>();
    private final Map<UUID, Location> caravanTargets = new HashMap<>(); // Kervan hedefleri
    private final Map<UUID, Clan> caravanClans = new HashMap<>(); // Kervan sahibi klanlar
    private final Map<Entity, UUID> caravanOwners = new HashMap<>(); // Entity -> Owner UUID (ters mapping)
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    /**
     * Kervan oluşturma - Anti-abuse kontrolleri ile
     */
    public boolean createCaravan(Player owner, Location start, Location end, List<ItemStack> cargo) {
        Main plugin = Main.getInstance();
        if (plugin == null) return false;
        
        ConfigManager configManager = plugin.getConfigManager();
        if (configManager == null) return false;
        
        // 1. DÜNYA KONTROLÜ (Farklı dünyalar arası mesafe ölçülemez)
        if (!start.getWorld().equals(end.getWorld())) {
            owner.sendMessage("§cKervanlar şimdilik sadece aynı dünya içinde seyahat edebilir!");
            return false;
        }
        
        // 2. MESAFE KONTROLÜ (Anti-abuse) - Config'den
        int minDistance = balanceConfig != null ? balanceConfig.getCaravanMinDistance() : 1000;
        double distance = start.distance(end);
        if (distance < minDistance) {
            owner.sendMessage("§cTicaret rotası çok kısa! Hedef en az " + minDistance + " blok uzakta olmalı.");
            return false;
        }
        
        // 3. MALZEME SAYISI KONTROLÜ (Anti-abuse) - Config'den
        int totalItems = 0;
        for (ItemStack item : cargo) {
            if (item != null && item.getType() != Material.AIR) {
                totalItems += item.getAmount();
            }
        }
        int minStacks = balanceConfig != null ? balanceConfig.getCaravanMinStacks() : 20;
        int minItems = minStacks * 64; // 20 stack = 1280 item
        if (totalItems < minItems) {
            owner.sendMessage("§cKervan çıkarmak için yükünüz çok az! En az " + minStacks + " stack (" + minItems + " adet) eşya yükleyin.");
            return false;
        }
        
        // 4. YÜK DEĞERİ KONTROLÜ (Anti-abuse) - Config'den
        double totalValue = calculateCargoValue(cargo);
        double minValue = balanceConfig != null ? balanceConfig.getCaravanMinValue() : 5000.0;
        if (totalValue < minValue) {
            owner.sendMessage("§cKervan çıkarmak için yükünüz çok değersiz! En az " + minValue + " altın değerinde eşya yükleyin.");
            return false;
        }
        
        // 5. Kervanı oluştur (Mule kullan - daha fazla envanter)
        Mule mule = start.getWorld().spawn(start, Mule.class);
        mule.setTamed(true);
        mule.setOwner(owner);
        
        // Eşyaları yükle
        for (ItemStack item : cargo) {
            if (item != null && item.getType() != Material.AIR) {
                mule.getInventory().addItem(item);
            }
        }
        
        // Metadata ile işaretle
        mule.setMetadata("Caravan", new FixedMetadataValue(plugin, true));
        mule.setMetadata("CaravanOwner", new FixedMetadataValue(plugin, owner.getUniqueId().toString()));
        
        // Kayıtları tut
        activeCaravans.put(owner.getUniqueId(), mule);
        caravanTargets.put(owner.getUniqueId(), end);
        caravanOwners.put(mule, owner.getUniqueId());
        
        Clan ownerClan = Main.getInstance().getClanManager().getClanByPlayer(owner.getUniqueId());
        if (ownerClan != null) {
            caravanClans.put(owner.getUniqueId(), ownerClan);
        }
        
        owner.sendMessage("§a§lKervan oluşturuldu! §7Hedefe ulaştığında mallarınız x1.5 değer kazanacak.");
        owner.sendMessage("§eUyarı: Kervanınız saldırıya açık! Dikkatli olun.");
        
        // Kervan hedefe ulaşma kontrolü
        checkCaravanArrival(owner.getUniqueId());
        return true;
    }
    
    /**
     * Eşya değerini hesapla (basit fiyat sistemi)
     */
    private double calculateCargoValue(List<ItemStack> cargo) {
        double total = 0;
        for (ItemStack item : cargo) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            // Basit fiyat tablosu
            double price = getItemPrice(item.getType());
            if (ItemManager.isCustomItem(item, "RED_DIAMOND")) {
                price = 1000.0; // Kızıl Elmas çok değerli
            } else if (ItemManager.isCustomItem(item, "TITANIUM_INGOT")) {
                price = 500.0;
            } else if (ItemManager.isCustomItem(item, "DARK_MATTER")) {
                price = 800.0;
            }
            
            total += price * item.getAmount();
        }
        return total;
    }
    
    /**
     * Eşya fiyat tablosu
     */
    private double getItemPrice(Material material) {
        switch (material) {
            case DIAMOND: return 100.0;
            case GOLD_INGOT: return 50.0;
            case IRON_INGOT: return 10.0;
            case EMERALD: return 80.0;
            case NETHERITE_INGOT: return 500.0;
            case LAPIS_LAZULI: return 5.0;
            case REDSTONE: return 2.0;
            case COAL: return 1.0;
            default: return 1.0; // Diğer eşyalar için minimum değer
        }
    }
    
    /**
     * Bu entity bir kervan mı?
     */
    public boolean isCaravan(Entity entity) {
        return entity != null && entity.hasMetadata("Caravan");
    }
    
    /**
     * Kervan sahibini al
     */
    public UUID getOwner(Entity caravan) {
        if (caravan == null) return null;
        if (caravan.hasMetadata("CaravanOwner")) {
            try {
                String uuidStr = caravan.getMetadata("CaravanOwner").get(0).asString();
                return UUID.fromString(uuidStr);
            } catch (Exception e) {
                return null;
            }
        }
        return caravanOwners.get(caravan);
    }

    public Entity getCaravan(UUID playerId) {
        return activeCaravans.get(playerId);
    }
    
    /**
     * Kervan hedefe ulaşma kontrolü
     */
    private void checkCaravanArrival(UUID playerId) {
        Main plugin = Main.getInstance();
        if (plugin == null) return;
        
        int arrivalRadius = balanceConfig != null ? balanceConfig.getCaravanArrivalRadius() : 5;
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Entity caravan = activeCaravans.get(playerId);
                Location target = caravanTargets.get(playerId);
                
                if (caravan == null || !caravan.isValid() || target == null) {
                    cancel();
                    return;
                }
                
                // Dünya kontrolü (farklı dünyalar arası mesafe ölçülemez)
                if (!caravan.getLocation().getWorld().equals(target.getWorld())) {
                    cancel();
                    return;
                }
                
                // Kervan hedefe yaklaştı mı?
                if (caravan.getLocation().distance(target) <= arrivalRadius) {
                    // Ödül: Mallar x1.5 değer kazanır
                    Clan clan = caravanClans.get(playerId);
                    if (clan != null && caravan instanceof Mule) {
                        Mule mule = (Mule) caravan;
                        double totalValue = 0;
                        
                        // Mule'nin envanterindeki eşyaları değerlendir
                        ItemStack[] contents = mule.getInventory().getContents();
                        if (contents != null) {
                            for (ItemStack item : contents) {
                                if (item != null && item.getType() != Material.AIR) {
                                    totalValue += calculateCargoValue(java.util.Arrays.asList(item));
                                }
                            }
                        }
                        
                        // Config'den bonus çarpanı
                        double rewardMultiplier = balanceConfig != null ? balanceConfig.getCaravanRewardMultiplier() : 1.5;
                        double reward = totalValue * rewardMultiplier;
                        clan.deposit(reward);
                        
                        // Kervanı temizle
                        mule.getInventory().clear();
                        activeCaravans.remove(playerId);
                        caravanTargets.remove(playerId);
                        caravanClans.remove(playerId);
                        caravanOwners.remove(mule);
                        
                        Player owner = plugin.getServer().getPlayer(playerId);
                        if (owner != null && owner.isOnline()) {
                            owner.sendMessage("§a§l════════════════════════════");
                            owner.sendMessage("§a§lKERVAN HEDEFE ULAŞTI!");
                            owner.sendMessage("§eMallarınız x" + rewardMultiplier + " değer kazandı!");
                            owner.sendMessage("§6Toplam Ödül: §a" + String.format("%.2f", reward) + " Altın");
                            owner.sendMessage("§a§l════════════════════════════");
                        }
                        
                        // Kervanı kaldır
                        mule.remove();
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Her saniye kontrol et
    }
    
    /**
     * Kervanı temizle (ölüm durumunda)
     */
    public void removeCaravan(Entity caravan) {
        UUID ownerId = getOwner(caravan);
        if (ownerId != null) {
            activeCaravans.remove(ownerId);
            caravanTargets.remove(ownerId);
            caravanClans.remove(ownerId);
        }
        caravanOwners.remove(caravan);
    }
    
    /**
     * Aktif kervan sayısını döndür (Admin komutları için)
     */
    public int getActiveCaravanCount() {
        return activeCaravans.size();
    }
    
    /**
     * Tüm aktif kervanları döndür (Admin komutları için)
     */
    public Map<UUID, Entity> getActiveCaravans() {
        return new HashMap<>(activeCaravans);
    }
    
    /**
     * Tüm kervanları temizle (Admin komutları için)
     */
    public int clearAllCaravans() {
        int count = activeCaravans.size();
        
        // Tüm kervan entity'lerini kaldır
        for (Entity caravan : new ArrayList<>(activeCaravans.values())) {
            if (caravan != null && caravan.isValid()) {
                caravan.remove();
            }
        }
        
        activeCaravans.clear();
        caravanTargets.clear();
        caravanClans.clear();
        caravanOwners.clear();
        
        return count;
    }
}
