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

        // NOT: Diğer yapılar (POISON_REACTOR, SIEGE_FACTORY, WALL_GENERATOR, vb.) 
        // için şema dosyaları (.schem) oluşturulduktan sonra buraya eklenebilir.
        // Şu an için temel 4 yapı aktif: Simya Kulesi, Tektonik Sabitleyici, Şifa Kulesi, Global Pazar
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
        c.addStructure(new Structure(type, b.getLocation(), 1));
        p.sendMessage("§a" + name + " aktif edildi!");
        p.playSound(b.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 1);
        b.getWorld().spawnParticle(Particle.END_ROD, b.getLocation().add(0.5, 1, 0.5), 50);
    }
}

