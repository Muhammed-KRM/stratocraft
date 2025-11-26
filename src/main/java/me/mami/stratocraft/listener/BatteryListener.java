package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.BatteryManager;
import me.mami.stratocraft.manager.ItemManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
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
    private final TerritoryManager territoryManager;

    public BatteryListener(BatteryManager bm, TerritoryManager tm) { 
        this.batteryManager = bm;
        this.territoryManager = tm;
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

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;

        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack hand = p.getInventory().getItemInMainHand();

        // 1. MAGMA BATARYASI (Geliştirilmiş)
        if (b.getType() == Material.MAGMA_BLOCK && b.getRelative(BlockFace.DOWN).getType() == Material.MAGMA_BLOCK) {
            Material fuel = hand.getType();
            boolean isRedDiamond = ItemManager.isCustomItem(hand, "RED_DIAMOND");
            boolean isDarkMatter = ItemManager.isCustomItem(hand, "DARK_MATTER");
            
            if (fuel == Material.DIAMOND || fuel == Material.IRON_INGOT || isRedDiamond || isDarkMatter) {
                int alchemyLevel = getAlchemyTowerLevel(p);
                ItemStack offHand = p.getInventory().getItemInOffHand();
                boolean hasAmplifier = ItemManager.isCustomItem(offHand, "FLAME_AMPLIFIER");
                batteryManager.fireMagmaBattery(p, fuel, alchemyLevel, hasAmplifier);
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

        // 7. TOPRAK SURU (3 Toprak Bloğu yanyana)
        if (b.getType() == Material.DIRT && b.getRelative(BlockFace.EAST).getType() == Material.DIRT 
            && b.getRelative(BlockFace.WEST).getType() == Material.DIRT) {
            if (hand.getType() == Material.COBBLESTONE || ItemManager.isCustomItem(hand, "TITANIUM_INGOT")) {
                batteryManager.createEarthWall(p, hand.getType());
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 8. MANYETİK BOZUCU (3 Demir + 1 Lapis üstte)
        if (b.getType() == Material.LAPIS_BLOCK && b.getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK
            && b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK) {
            if (hand.getType() == Material.IRON_INGOT) {
                batteryManager.fireMagneticDisruptor(p);
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 9. SİSMİK ÇEKİÇ (Felaket Mücadele)
        if (b.getType() == Material.ANVIL && b.getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK
            && b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK) {
            if (ItemManager.isCustomItem(hand, "STAR_CORE")) {
                batteryManager.fireSeismicHammer(p);
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 10. OZON KALKANI (Güneş Fırtınası Koruma)
        if (b.getType() == Material.BEACON && b.getRelative(BlockFace.DOWN).getType() == Material.GLASS) {
            if (ItemManager.isCustomItem(hand, "RUBY")) {
                batteryManager.activateOzoneShield(p, b.getLocation());
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 11. ENERJİ DUVARI (Adamantite ile)
        if (b.getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK
            && b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.IRON_BLOCK) {
            if (ItemManager.isCustomItem(hand, "ADAMANTITE")) {
                batteryManager.createEnergyWall(p);
                hand.setAmount(hand.getAmount() - 1);
            }
        }

        // 12. LAV HENDEKÇİSİ (Alan Savunması)
        if (b.getType() == Material.LAVA && b.getRelative(BlockFace.DOWN).getType() == Material.LAVA) {
            if (hand.getType() == Material.BUCKET && hand.getType() == Material.LAVA_BUCKET) {
                batteryManager.createLavaTrench(p);
            }
        }
    }
}

