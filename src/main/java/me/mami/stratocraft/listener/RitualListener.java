package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class RitualListener implements Listener {
    private final ClanManager clanManager;

    public RitualListener(ClanManager cm) { this.clanManager = cm; }

    @EventHandler
    public void onRitual(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        
        Block b = event.getClickedBlock();
        Player leader = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(leader.getUniqueId());

        if (clan == null || clan.getRank(leader.getUniqueId()) != Clan.Rank.LEADER) return;
        
        // --- TERFİ RİTÜELİ KONTROLÜ ---
        // Kurulum: 3x3 Taş Tuğla, Köşelerde Kızıltaş Meşalesi, Ortada Ateş
        if (b.getType() == Material.FIRE && b.getRelative(BlockFace.DOWN).getType() == Material.STONE_BRICKS) {
            
            // Kolaylık için sadece Altın Külçe kontrolü yapalım
            if (leader.getInventory().getItemInMainHand().getType() == Material.GOLD_INGOT) {
                
                leader.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof Player && e != leader)
                    .map(e -> (Player)e)
                    .findFirst()
                    .ifPresent(target -> {
                        if (clan.getRank(target.getUniqueId()) == Clan.Rank.MEMBER) {
                            clanManager.addMember(clan, target.getUniqueId(), Clan.Rank.GENERAL);
                            leader.sendMessage("§a" + target.getName() + " General rütbesine yükseltildi!");
                            leader.getInventory().getItemInMainHand().setAmount(leader.getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            leader.sendMessage("§eBu kişi zaten General veya daha üst rütbede.");
                        }
                    });
            } else if (leader.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT) {
                // ... Üye terfisi mantığı ...
            }
        }
    }
}

