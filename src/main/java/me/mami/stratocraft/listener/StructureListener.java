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

    @EventHandler
    public void onBuild(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        Clan clan = clanManager.getClanByPlayer(p.getUniqueId());
        
        if (!ItemManager.isCustomItem(p.getInventory().getItemInMainHand(), "BLUEPRINT")) return;

        if (clan == null) {
            p.sendMessage("§cKlanın yok!");
            return;
        }

        // --- YAPI KONTROLLERİ ---

        // 1. SİMYA KULESİ
        if (b.getType() == Material.ENCHANTING_TABLE) {
            if (checkRecipe(p, "ALCHEMY")) {
                if (validator.validate(b.getLocation(), "alchemy_tower")) {
                    createStructure(p, clan, b, Structure.Type.ALCHEMY_TOWER, "Simya Kulesi");
                } else {
                    p.sendMessage("§cYapı şemaya uymuyor! (alchemy_tower.schem)");
                }
            }
        }

        // 2. TEKTONİK SABİTLEYİCİ
        else if (b.getType() == Material.PISTON) {
            if (checkRecipe(p, "TECTONIC")) {
                if (validator.validate(b.getLocation(), "tectonic_stabilizer")) {
                    createStructure(p, clan, b, Structure.Type.TECTONIC_STABILIZER, "Tektonik Sabitleyici");
                }
            }
        }

        // 3. ŞİFA KULESİ
        else if (b.getType() == Material.LANTERN) {
            if (validator.validate(b.getLocation(), "healing_tower")) {
                createStructure(p, clan, b, Structure.Type.HEALING_BEACON, "Şifa Kulesi");
            }
        }
        
        // 4. GLOBAL PAZAR KAPISI
        else if (b.getType() == Material.ENDER_CHEST) {
             if (validator.validate(b.getLocation(), "market_gate")) {
                createStructure(p, clan, b, Structure.Type.GLOBAL_MARKET_GATE, "Global Pazar");
             }
        }
        
        // 5. OTOMATİK TARET (Hurda teknolojisi - Antik Dişli + Piston ile yapılır)
        else if (b.getType() == Material.DISPENSER) {
            // Oyuncunun elinde Antik Dişli ve Piston var mı kontrol et
            org.bukkit.inventory.ItemStack mainHand = p.getInventory().getItemInMainHand();
            org.bukkit.inventory.ItemStack offHand = p.getInventory().getItemInOffHand();
            
            boolean hasAncientGear = mainHand != null && mainHand.getType() == Material.IRON_NUGGET && 
                                     mainHand.getItemMeta() != null && 
                                     mainHand.getItemMeta().getDisplayName() != null &&
                                     mainHand.getItemMeta().getDisplayName().contains("Antik Dişli");
            boolean hasPiston = (mainHand != null && mainHand.getType() == Material.PISTON) ||
                                (offHand != null && offHand.getType() == Material.PISTON);
            
            if (hasAncientGear && hasPiston) {
                createStructure(p, clan, b, Structure.Type.AUTO_TURRET, "Otomatik Taret");
                // Malzemeleri tüket
                if (mainHand.getType() == Material.IRON_NUGGET) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                } else if (offHand != null && offHand.getType() == Material.IRON_NUGGET) {
                    offHand.setAmount(offHand.getAmount() - 1);
                }
                if (mainHand.getType() == Material.PISTON) {
                    mainHand.setAmount(mainHand.getAmount() - 1);
                } else if (offHand != null && offHand.getType() == Material.PISTON) {
                    offHand.setAmount(offHand.getAmount() - 1);
                }
            } else {
                p.sendMessage("§cOtomatik Taret için Antik Dişli ve Piston gerekli!");
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
        // Araştırma Manager'ı kontrol et
        if (!researchManager.hasRecipeBook(p, recipeId)) {
            p.sendMessage("§cGerekli tarife sahip değilsin!");
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
        p.sendMessage("§a" + name + " aktif edildi!");
        p.playSound(b.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
        b.getWorld().spawnParticle(Particle.END_ROD, b.getLocation().add(0.5, 1, 0.5), 50);
    }
    
    private boolean checkStructureCost(Player p, Structure.Type type) {
        org.bukkit.inventory.Inventory inv = p.getInventory();
        
        switch (type) {
            case ALCHEMY_TOWER:
                // Simya Kulesi: 32 Altın + 16 Elmas
                return inv.contains(Material.GOLD_INGOT, 32) && inv.contains(Material.DIAMOND, 16);
            case TECTONIC_STABILIZER:
                // Tektonik Sabitleyici: 16 Titanyum + 8 Piston
                return (ItemManager.TITANIUM_INGOT != null && 
                        inv.contains(ItemManager.TITANIUM_INGOT, 16)) &&
                       inv.contains(Material.PISTON, 8);
            case HEALING_BEACON:
                // Şifa Kulesi: 16 Demir + 8 Lapis
                return inv.contains(Material.IRON_INGOT, 16) && inv.contains(Material.LAPIS_LAZULI, 8);
            case GLOBAL_MARKET_GATE:
                // Global Pazar: 32 Altın + 16 Ender Pearl
                return inv.contains(Material.GOLD_INGOT, 32) && inv.contains(Material.ENDER_PEARL, 16);
            default:
                return true; // Diğer yapılar için maliyet yok (şimdilik)
        }
    }
    
    private void consumeStructureCost(Player p, Structure.Type type) {
        org.bukkit.inventory.Inventory inv = p.getInventory();
        
        switch (type) {
            case ALCHEMY_TOWER:
                inv.removeItem(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 32),
                              new org.bukkit.inventory.ItemStack(Material.DIAMOND, 16));
                break;
            case TECTONIC_STABILIZER:
                if (ItemManager.TITANIUM_INGOT != null) {
                    inv.removeItem(ItemManager.TITANIUM_INGOT.clone().asQuantity(16),
                                  new org.bukkit.inventory.ItemStack(Material.PISTON, 8));
                }
                break;
            case HEALING_BEACON:
                inv.removeItem(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 16),
                              new org.bukkit.inventory.ItemStack(Material.LAPIS_LAZULI, 8));
                break;
            case GLOBAL_MARKET_GATE:
                inv.removeItem(new org.bukkit.inventory.ItemStack(Material.GOLD_INGOT, 32),
                              new org.bukkit.inventory.ItemStack(Material.ENDER_PEARL, 16));
                break;
        }
    }
}

