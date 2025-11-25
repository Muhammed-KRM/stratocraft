package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BatteryManager;
import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BatteryListener implements Listener {
    private final BatteryManager batteryManager;

    public BatteryListener(BatteryManager bm) { this.batteryManager = bm; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack hand = p.getInventory().getItemInMainHand();

        // 1. MAGMA BATARYASI
        if (b.getType() == Material.MAGMA_BLOCK && b.getRelative(BlockFace.DOWN).getType() == Material.MAGMA_BLOCK) {
            if (hand.getType() == Material.DIAMOND || hand.getType() == Material.IRON_INGOT) {
                batteryManager.fireMagmaBattery(p, hand.getType(), false);
            }
        }

        // 2. YILDIRIM BATARYASI (3 Demir + Yıldırım Çekirdeği)
        if (b.getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK) {
            if (ItemManager.isCustomItem(hand, "LIGHTNING_CORE")) {
                batteryManager.fireLightningBattery(p);
            }
        }

        // 3. KARA DELİK (3 Obsidyen + Karanlık Madde)
        if (b.getType() == Material.OBSIDIAN && b.getRelative(BlockFace.DOWN).getType() == Material.OBSIDIAN) {
            if (ItemManager.isCustomItem(hand, "DARK_MATTER")) {
                batteryManager.fireBlackHole(p);
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 4. ANLIK KÖPRÜ (3 Buz + Tüy)
        if (b.getType() == Material.PACKED_ICE && b.getRelative(BlockFace.DOWN).getType() == Material.PACKED_ICE) {
            if (hand.getType() == Material.FEATHER) {
                batteryManager.createInstantBridge(p);
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 5. SIĞINAK KÜPÜ (3 Kırıktaş + Demir)
        if (b.getType() == Material.COBBLESTONE && b.getRelative(BlockFace.DOWN).getType() == Material.COBBLESTONE) {
            if (hand.getType() == Material.IRON_INGOT) {
                batteryManager.createInstantBunker(p);
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 6. YERÇEKİMİ ÇAPASI (2 Slime + Örs + Demir)
        if (b.getType() == Material.ANVIL && b.getRelative(BlockFace.DOWN).getType() == Material.SLIME_BLOCK) {
            if (hand.getType() == Material.IRON_INGOT) {
                batteryManager.fireGravityAnchor(p);
            }
        }
    }
}

