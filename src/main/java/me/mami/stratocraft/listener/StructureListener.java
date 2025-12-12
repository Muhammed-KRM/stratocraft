package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.ResearchManager;
import me.mami.stratocraft.manager.StructureValidator;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class StructureListener implements Listener {
    private final ClanManager clanManager;
    private final StructureValidator validator;
    private final ResearchManager researchManager;

    public StructureListener(ClanManager cm, ResearchManager rm) {
        this.clanManager = cm;
        this.researchManager = rm;
        this.validator = new StructureValidator();
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onBuild(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        // BUG FIX: Shift+Sağ tık kontrolü - Normal Minecraft kullanımını engelleme
        // Yapı aktivasyonu için shift+sağ tık gerekli (normal kullanımı engellemez)
        if (!event.getPlayer().isSneaking()) return;

        Player p = event.getPlayer();
        
        // Admin bypass kontrolü - Yapı kurma kısıtlamalarını atla
        // Not: Admin'ler yine de klan üyesi olmalı veya bu kontrol kaldırılabilir
        // Şimdilik admin bypass sadece koruma kısıtlamalarında çalışıyor
        
        Block b = event.getClickedBlock();
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        
        if (!ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "BLUEPRINT")) return;

        if (clan == null) {
            // Admin bypass: Admin'ler klan olmadan da yapı kurabilir (opsiyonel)
            // Şimdilik normal kontrolü koruyoruz
            p.sendMessage("§cKlanın yok!");
            return;
        }

        // --- YAPI KONTROLLERİ ---

        // 1. SİMYA KULESİ (Tarif gerektirir)
        if (b.getType() == Material.ENCHANTING_TABLE) {
            if (checkRecipe(p, "ALCHEMY_TOWER")) {
                p.sendMessage("§7Yapı kontrol ediliyor...");
                validator.validateAsync(b.getLocation(), "alchemy_tower", (isValid) -> {
                    if (isValid) {
                        createStructure(p, clan, b, Structure.Type.ALCHEMY_TOWER, "Simya Kulesi");
                    } else {
                        p.sendMessage("§cYapı şemaya uymuyor! (alchemy_tower.schem)");
                    }
                });
            }
        }

        // 2. TEKTONİK SABİTLEYİCİ (Tarif gerektirir)
        else if (b.getType() == Material.PISTON) {
            if (checkRecipe(p, "TECTONIC_STABILIZER")) {
                p.sendMessage("§7Yapı kontrol ediliyor...");
                p.playSound(b.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                validator.validateAsync(b.getLocation(), "tectonic_stabilizer", (isValid) -> {
                    if (isValid) {
                        createStructure(p, clan, b, Structure.Type.TECTONIC_STABILIZER, "Tektonik Sabitleyici");
                    } else {
                        p.sendMessage("§c§l✗ Yapı şemaya uymuyor!");
                        p.sendMessage("§7Lütfen §etectonic_stabilizer.schem §7şemasına uygun şekilde yapıyı kurun.");
                        p.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        b.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, b.getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                    }
                });
            }
        }

        // 3. ŞİFA KULESİ (KALDIRILDI - Fener ile sağ tıklama bug'a neden oluyordu)
        // else if (b.getType() == Material.LANTERN) {
        //     if (checkRecipe(p, "HEALING_BEACON")) {
        //         if (validator.validate(b.getLocation(), "healing_tower")) {
        //             createStructure(p, clan, b, Structure.Type.HEALING_BEACON, "Şifa Kulesi");
        //         }
        //     }
        // }
        
        // 4. GLOBAL PAZAR KAPISI (Tarif gerektirir)
        else if (b.getType() == Material.ENDER_CHEST) {
            if (checkRecipe(p, "GLOBAL_MARKET_GATE")) {
                p.sendMessage("§7Yapı kontrol ediliyor...");
                p.playSound(b.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                validator.validateAsync(b.getLocation(), "market_gate", (isValid) -> {
                    if (isValid) {
                        createStructure(p, clan, b, Structure.Type.GLOBAL_MARKET_GATE, "Global Pazar");
                    } else {
                        p.sendMessage("§c§l✗ Yapı şemaya uymuyor!");
                        p.sendMessage("§7Lütfen §emarket_gate.schem §7şemasına uygun şekilde yapıyı kurun.");
                        p.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        b.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, b.getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                    }
                });
            }
        }
        
        // 5. ZEHİR REAKTÖRÜ (Tarif gerektirir)
        else if (b.getType() == Material.BEACON) {
            // Yeşil beacon ise zehir reaktörü
            if (checkRecipe(p, "POISON_REACTOR")) {
                p.sendMessage("§7Yapı kontrol ediliyor...");
                p.playSound(b.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                validator.validateAsync(b.getLocation(), "poison_reactor", (isValid) -> {
                    if (isValid) {
                        createStructure(p, clan, b, Structure.Type.POISON_REACTOR, "Zehir Reaktörü");
                    } else {
                        p.sendMessage("§c§l✗ Yapı şemaya uymuyor!");
                        p.sendMessage("§7Lütfen §epoison_reactor.schem §7şemasına uygun şekilde yapıyı kurun.");
                        p.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        b.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, b.getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                    }
                });
            }
        }
        
        // 6. OTOMATİK TARET (Tarif gerektirir)
        else if (b.getType() == Material.DISPENSER) {
            if (checkRecipe(p, "AUTO_TURRET")) {
                // Otomatik taret için özel kontrol (Antik Dişli + Piston)
                org.bukkit.inventory.ItemStack mainHand = p.getInventory().getItemInMainHand();
                org.bukkit.inventory.ItemStack offHand = p.getInventory().getItemInOffHand();
                
                boolean hasAncientGear = mainHand != null && mainHand.getType() == Material.IRON_NUGGET && 
                                         mainHand.getItemMeta() != null && 
                                         mainHand.getItemMeta().getDisplayName() != null &&
                                         mainHand.getItemMeta().getDisplayName().contains("Antik Dişli");
                boolean hasPiston = (mainHand != null && mainHand.getType() == Material.PISTON) ||
                                   (offHand != null && offHand.getType() == Material.PISTON);
                
                if (hasAncientGear && hasPiston) {
                    p.sendMessage("§7Yapı kontrol ediliyor...");
                    p.playSound(b.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                    validator.validateAsync(b.getLocation(), "auto_turret", (isValid) -> {
                        if (isValid) {
                            createStructure(p, clan, b, Structure.Type.AUTO_TURRET, "Otomatik Taret");
                            // Malzemeleri tüket
                            if (mainHand != null && mainHand.getType() == Material.IRON_NUGGET) {
                                mainHand.setAmount(mainHand.getAmount() - 1);
                            } else if (offHand != null && offHand.getType() == Material.IRON_NUGGET) {
                                offHand.setAmount(offHand.getAmount() - 1);
                            }
                            if (mainHand != null && mainHand.getType() == Material.PISTON) {
                                mainHand.setAmount(mainHand.getAmount() - 1);
                            } else if (offHand != null && offHand.getType() == Material.PISTON) {
                                offHand.setAmount(offHand.getAmount() - 1);
                            }
                        } else {
                            p.sendMessage("§c§l✗ Yapı şemaya uymuyor!");
                            p.sendMessage("§7Lütfen §eauto_turret.schem §7şemasına uygun şekilde yapıyı kurun.");
                            p.playSound(b.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            b.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, b.getLocation().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5, 0.1);
                        }
                    });
                } else {
                    p.sendMessage("§cOtomatik Taret için Antik Dişli ve Piston gerekiyor!");
                }
            }
        }
        
        // 6. XP BANKASI - Tecrübe yatır/çek
        else if (b.getType() == Material.EXPERIENCE_BOTTLE || b.getType() == Material.ENCHANTING_TABLE) {
            Structure xpBank = clan.getStructures().stream()
                    .filter(s -> s.getType() == Structure.Type.XP_BANK && 
                                s.getLocation().distance(b.getLocation()) <= 5)
                    .findFirst().orElse(null);
            
            if (xpBank != null) {
                if (p.isSneaking()) {
                    // XP Çek
                    int storedXP = getStoredXP(clan);
                    if (storedXP > 0) {
                        int toGive = Math.min(storedXP, 100);
                        p.giveExp(toGive);
                        setStoredXP(clan, storedXP - toGive);
                        p.sendMessage("§a" + toGive + " XP çektin! Kalan: " + (storedXP - toGive));
                    } else {
                        p.sendMessage("§cXP Bankası boş!");
                    }
                } else {
                    // XP Yatır
                    int playerXP = p.getTotalExperience();
                    if (playerXP > 0) {
                        int toStore = Math.min(playerXP, 100);
                        p.giveExp(-toStore);
                        int currentStored = getStoredXP(clan);
                        setStoredXP(clan, currentStored + toStore);
                        p.sendMessage("§a" + toStore + " XP yatırdın! Toplam: " + (currentStored + toStore));
                    } else {
                        p.sendMessage("§cYatıracak XP'n yok!");
                    }
                }
            }
        }
        
        // 7. IŞINLANMA PLATFORMU - Şubeler arası ışınlanma
        else if (b.getType() == Material.END_PORTAL_FRAME || b.getType() == Material.END_PORTAL) {
            Structure teleporter = clan.getStructures().stream()
                    .filter(s -> s.getType() == Structure.Type.TELEPORTER && 
                                s.getLocation().distance(b.getLocation()) <= 3)
                    .findFirst().orElse(null);
            
            if (teleporter != null) {
                // Diğer şubeleri listele
                java.util.List<org.bukkit.Location> outposts = clan.getTerritory().getOutposts();
                if (outposts.isEmpty()) {
                    p.sendMessage("§cIşınlanacak şube yok! Önce bir şube kurmalısın.");
                } else {
                    // İlk şubeye ışınlan (basit versiyon)
                    org.bukkit.Location target = outposts.get(0);
                    p.teleport(target);
                    p.sendMessage("§bIşınlandın! Şube: " + 
                        (int)target.getX() + ", " + (int)target.getY() + ", " + (int)target.getZ());
                }
            }
        }

        // NOT: Diğer yapılar (POISON_REACTOR, SIEGE_FACTORY, WALL_GENERATOR, vb.) 
        // için şema dosyaları (.schem) oluşturulduktan sonra buraya eklenebilir.
    }
    
    // XP Bankası için yardımcı metodlar
    private int getStoredXP(Clan clan) {
        return clan.getStoredXP();
    }
    
    private void setStoredXP(Clan clan, int amount) {
        clan.setStoredXP(amount);
    }

    private boolean checkRecipe(Player p, String recipeId) {
        // Admin bypass kontrolü
        if (me.mami.stratocraft.util.ListenerUtil.hasAdminBypass(p)) {
            return true; // Admin bypass yetkisi varsa tarif kısıtlamalarını atla
        }
        
        // Araştırma Manager'ı kontrol et
        if (!researchManager.hasRecipeBook(p, recipeId)) {
            p.sendMessage("§cGerekli tarife sahip değilsin!");
            p.sendMessage("§7Bu yapıyı aktifleştirmek için §e" + recipeId + " §7tarif kitabı gerekiyor.");
            p.sendMessage("§7Tarif kitapları normal canlılardan veya bosslardan düşebilir.");
            return false;
        }
        return true;
    }

    private void createStructure(Player p, Clan c, Block b, Structure.Type type, String name) {
        // Yapı inşa maliyetleri kontrolü
        if (!checkStructureCost(p, type)) {
            p.sendMessage("§cYapı inşa etmek için yeterli malzemeniz yok!");
            return;
        }
        
        // Malzemeleri tüket
        consumeStructureCost(p, type);
        
        c.addStructure(new Structure(type, b.getLocation(), 1));
        
        // ✅ İYİLEŞTİRME: Daha belirgin efektler
        org.bukkit.Location loc = b.getLocation().add(0.5, 1, 0.5);
        
        // Mesaj
        p.sendMessage("§a§l✓ " + name + " başarıyla aktif edildi!");
        p.sendMessage("§7Yapıya sağ tıklayarak menüyü açabilirsiniz.");
        
        // Ses efektleri
        p.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        p.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
        p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        
        // Partikül efektleri (daha belirgin)
        // Ana partikül patlaması
        b.getWorld().spawnParticle(Particle.END_ROD, loc, 100, 1.5, 1.5, 1.5, 0.1);
        b.getWorld().spawnParticle(Particle.TOTEM, loc, 50, 1.0, 1.0, 1.0, 0.05);
        b.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 30, 1.0, 1.0, 1.0, 0.1);
        
        // Işık efekti (firework)
        b.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.5, 0.5, 0.5, 0.1);
        
        // Başarı mesajı (başlık)
        p.sendTitle("§a§l✓ BAŞARILI", "§7" + name + " aktif edildi!", 10, 40, 10);
    }
    
    private boolean checkStructureCost(Player p, Structure.Type type) {
        org.bukkit.inventory.Inventory inv = p.getInventory();
        
        switch (type) {
            case ALCHEMY_TOWER:
                // Simya Kulesi: 32 Altın + 16 Elmas
                return inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 32), 32) &&
                       inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 16), 16);
            case TECTONIC_STABILIZER:
                // Tektonik Sabitleyici: 16 Titanyum + 8 Piston
                if (ItemManager.TITANIUM_INGOT == null) return false;
                int titaniumCount = 0;
                for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
                    if (item != null && ItemManager.isCustomItem(item, "TITANIUM_INGOT")) {
                        titaniumCount += item.getAmount();
                    }
                }
                return titaniumCount >= 16 && inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.PISTON, 8), 8);
            case HEALING_BEACON:
                // Şifa Kulesi: 16 Demir + 8 Lapis
                return inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 16), 16) &&
                       inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.LAPIS_LAZULI, 8), 8);
            case GLOBAL_MARKET_GATE:
                // Global Pazar: 32 Altın + 16 Ender Pearl
                return inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 32), 32) &&
                       inv.containsAtLeast(new org.bukkit.inventory.ItemStack(Material.ENDER_PEARL, 16), 16);
            default:
                return true; // Diğer yapılar için maliyet yok (şimdilik)
        }
    }
    
    private void consumeStructureCost(Player p, Structure.Type type) {
        org.bukkit.inventory.Inventory inv = p.getInventory();
        
        switch (type) {
            case ALCHEMY_TOWER:
                // 32 Altın + 16 Elmas sil
                removeItems(inv, Material.GOLD_INGOT, 32);
                removeItems(inv, Material.DIAMOND, 16);
                break;
            case TECTONIC_STABILIZER:
                // 16 Titanyum + 8 Piston sil
                if (ItemManager.TITANIUM_INGOT != null) {
                    removeCustomItems(inv, "TITANIUM_INGOT", 16);
                }
                removeItems(inv, Material.PISTON, 8);
                break;
            case HEALING_BEACON:
                // 16 Demir + 8 Lapis sil
                removeItems(inv, Material.IRON_INGOT, 16);
                removeItems(inv, Material.LAPIS_LAZULI, 8);
                break;
            case GLOBAL_MARKET_GATE:
                // 32 Altın + 16 Ender Pearl sil
                removeItems(inv, Material.GOLD_INGOT, 32);
                removeItems(inv, Material.ENDER_PEARL, 16);
                break;
            case TELEPORTER:
            case INVISIBILITY_CLOAK:
            case WALL_GENERATOR:
            case WEATHER_MACHINE:
            case CORE:
            case LIBRARY:
            case MOB_GRINDER:
            case SIEGE_FACTORY:
            case XP_BANK:
            case DRONE_STATION:
            case POISON_REACTOR:
            case AUTO_TURRET:
            case GRAVITY_WELL:
            case LAVA_TRENCHER:
            case CROP_ACCELERATOR:
            case WARNING_SIGN:
            case MAG_RAIL:
            case WATCHTOWER:
            case FOOD_SILO:
            case OIL_REFINERY:
            case ARMORY:
            case AUTO_DRILL:
                // Bu yapı tipleri için özel maliyet yok veya başka yerden yönetiliyor
                break;
        }
    }
    
    // Yardımcı metod: Normal eşyaları sil
    private void removeItems(org.bukkit.inventory.Inventory inv, Material material, int amount) {
        int remaining = amount;
        for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == material && remaining > 0) {
                int remove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - remove);
                remaining -= remove;
            }
        }
    }
    
    // Yardımcı metod: Özel eşyaları sil
    private void removeCustomItems(org.bukkit.inventory.Inventory inv, String customId, int amount) {
        int remaining = amount;
        for (org.bukkit.inventory.ItemStack item : inv.getContents()) {
            if (item != null && ItemManager.isCustomItem(item, customId) && remaining > 0) {
                int remove = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - remove);
                remaining -= remove;
            }
        }
    }
}

