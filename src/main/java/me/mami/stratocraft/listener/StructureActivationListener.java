package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Structure.Type;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * StructureActivationListener - Yapı aktivasyon sistemi
 * 
 * Oyuncular blok blok yapıyı kurduktan sonra aktivasyon ritüeli yapar:
 * 1. Oyuncu pattern'i manuel olarak kurar
 * 2. Merkez bloğa shift+sağ tık yapar
 * 3. Sistem pattern'i kontrol eder
 * 4. Doğruysa → Yapı aktif olur, klana eklenir
 */
public class StructureActivationListener implements Listener {

    private final ClanManager clanManager;
    private final TerritoryManager territoryManager;

    // Cooldown: Oyuncu UUID -> Son aktivasyon zamanı
    private final Map<UUID, Long> activationCooldowns = new HashMap<>();
    private static final long ACTIVATION_COOLDOWN = 5000L; // 5 saniye

    public StructureActivationListener(ClanManager cm, TerritoryManager tm) {
        this.clanManager = cm;
        this.territoryManager = tm;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onStructureActivation(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!event.getPlayer().isSneaking())
            return;

        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (clicked == null)
            return;

        // Cooldown kontrolü
        if (isOnCooldown(player.getUniqueId())) {
            player.sendMessage("§cYapı aktivasyonu için beklemen gerekiyor!");
            return;
        }

        // Oyuncu bir klana üye mi?
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            return; // Klansız oyuncu yapı aktive edemez
        }

        // Klan bölgesinde mi?
        Clan owner = territoryManager.getTerritoryOwner(clicked.getLocation());
        if (owner == null || !owner.equals(clan)) {
            return; // Kendi bölgesinde değil
        }

        // Yetki kontrolü: Recruit yapı aktive edemez
        if (clan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
            player.sendMessage("§cAcemilerin yapı kurma yetkisi yok!");
            return;
        }

        // Pattern kontrolü - her yapı tipi için kontrol et
        Structure detectedStructure = detectStructurePattern(clicked, player);

        if (detectedStructure != null) {
            // Yapıyı klana ekle
            clan.addStructure(detectedStructure);

            // Cooldown ekle
            setCooldown(player.getUniqueId());

            // Başarı mesajı ve efektler
            event.setCancelled(true);
            activateStructureEffects(player, detectedStructure);

            player.sendMessage("§a§l" + getStructureName(detectedStructure.getType()) +
                    " AKTİVE EDİLDİ! (Seviye " + detectedStructure.getLevel() + ")");
            player.playSound(clicked.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        }
    }

    /**
     * Yapı pattern'ini tespit et
     */
    private Structure detectStructurePattern(Block center, Player player) {
        // Her yapı tipi için pattern kontrolü

        // 1. SİMYA KULESİ (Alchemy Tower) - 3x3x5 Bookshelf + Beacon üstte
        Structure alchemyTower = checkAlchemyTower(center);
        if (alchemyTower != null)
            return alchemyTower;

        // 2. ZEHİR REAKTÖRÜ (Poison Reactor) - 3x3x4 Prismarine + Beacon
        Structure poisonReactor = checkPoisonReactor(center);
        if (poisonReactor != null)
            return poisonReactor;

        // 3. TEKTONİK SABİTLEYİCİ (Tectonic Stabilizer) - 5x5x6 Obsidian + End Rod
        Structure tectonicStabilizer = checkTectonicStabilizer(center);
        if (tectonicStabilizer != null)
            return tectonicStabilizer;

        // 4. GÖZETLEME KULESİ (Watchtower) - 3x3x10 Stone Brick kule
        Structure watchtower = checkWatchtower(center);
        if (watchtower != null)
            return watchtower;

        // 5. OTOMATİK TARET (Auto Turret) - 2x2x3 Iron Block + Dispenser
        Structure autoTurret = checkAutoTurret(center);
        if (autoTurret != null)
            return autoTurret;

        // Eklenebilir: Diğer yapılar...

        return null;
    }

    // ========== YAPI PATTERN KONTROLLERİ ==========

    /**
     * Simya Kulesi: 3x3 Kitaplık taban + 5 blok yüksek + Beacon üstte
     */
    private Structure checkAlchemyTower(Block center) {
        if (center.getType() != Material.BEACON)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);

        // 3x3 Kitaplık platformu kontrolü (4 blok yüksek)
        int height = 0;
        int maxHeight = 0;

        // Yüksekliği say
        for (int y = 1; y <= 5; y++) {
            Block checkBlock = center.getRelative(0, -y, 0);
            if (checkBlock.getType() == Material.BOOKSHELF ||
                    checkBlock.getType() == Material.CHISELED_BOOKSHELF) {
                height++;
                maxHeight = y;
            } else {
                break;
            }
        }

        // Seviye belirleme: 1-3 blok = Lv1, 4-5 blok = Lv2, 5+ blok = Lv3
        int level = 1;
        if (height >= 5)
            level = 3;
        else if (height >= 4)
            level = 2;
        else if (height >= 3)
            level = 1;
        else
            return null; // Çok kısa

        // 3x3 platform kontrolü (taban)
        Block base = center.getRelative(0, -maxHeight - 1, 0);
        if (!check3x3Platform(base, Material.BOOKSHELF, Material.CHISELED_BOOKSHELF)) {
            return null;
        }

        return new Structure(Type.ALCHEMY_TOWER, center.getLocation(), level);
    }

    /**
     * Zehir Reaktörü: 3x3 Prismarine + 4 blok yüksek + Beacon üstte
     */
    private Structure checkPoisonReactor(Block center) {
        if (center.getType() != Material.BEACON)
            return null;

        // Altında 3-5 blok Prismarine kontrolü
        int height = 0;
        for (int y = 1; y <= 5; y++) {
            Block checkBlock = center.getRelative(0, -y, 0);
            if (checkBlock.getType() == Material.PRISMARINE ||
                    checkBlock.getType() == Material.DARK_PRISMARINE) {
                height++;
            } else {
                break;
            }
        }

        int level = 1;
        if (height >= 5)
            level = 3;
        else if (height >= 4)
            level = 2;
        else if (height >= 3)
            level = 1;
        else
            return null;

        Block base = center.getRelative(0, -height - 1, 0);
        if (!check3x3Platform(base, Material.PRISMARINE, Material.DARK_PRISMARINE)) {
            return null;
        }

        return new Structure(Type.POISON_REACTOR, center.getLocation(), level);
    }

    /**
     * Tektonik Sabitleyici: 5x5 Obsidian platform + End Rod ortada
     */
    private Structure checkTectonicStabilizer(Block center) {
        if (center.getType() != Material.END_ROD)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);

        // 5x5 Obsidian platform kontrolü
        int obsidianCount = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.OBSIDIAN) {
                    obsidianCount++;
                }
            }
        }

        // En az 20 blok obsidian (5x5 = 25, biraz tolerans)
        if (obsidianCount < 20)
            return null;

        // Seviye: Obsidian sayısına göre
        int level = obsidianCount >= 25 ? 3 : (obsidianCount >= 23 ? 2 : 1);

        return new Structure(Type.TECTONIC_STABILIZER, center.getLocation(), level);
    }

    /**
     * Gözetleme Kulesi: 3x3 taban + 8-12 blok yüksek kule + Beacon üstte
     */
    private Structure checkWatchtower(Block center) {
        if (center.getType() != Material.BEACON)
            return null;

        // Yüksekliği kontrol et (Stone Brick)
        int height = 0;
        for (int y = 1; y <= 15; y++) {
            Block checkBlock = center.getRelative(0, -y, 0);
            if (checkBlock.getType() == Material.STONE_BRICKS) {
                height++;
            } else {
                break;
            }
        }

        // Minimum 8 blok yüksek olmalı
        if (height < 8)
            return null;

        int level = 1;
        if (height >= 12)
            level = 3;
        else if (height >= 10)
            level = 2;

        Block base = center.getRelative(0, -height - 1, 0);
        if (!check3x3Platform(base, Material.STONE_BRICKS)) {
            return null;
        }

        return new Structure(Type.WATCHTOWER, center.getLocation(), level);
    }

    /**
     * Otomatik Taret: 2x2 Iron Block + Dispenser üstte
     */
    private Structure checkAutoTurret(Block center) {
        if (center.getType() != Material.DISPENSER)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);

        // 2x2 Iron Block kontrolü
        int ironCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.IRON_BLOCK) {
                    ironCount++;
                }
            }
        }

        if (ironCount < 4)
            return null;

        // Yükseklik kontrolü (3-5 blok)
        int height = 0;
        for (int y = 1; y <= 5; y++) {
            Block checkBlock = center.getRelative(0, -y, 0);
            if (checkBlock.getType() == Material.IRON_BLOCK) {
                height++;
            } else {
                break;
            }
        }

        int level = Math.min(3, Math.max(1, height - 1));

        return new Structure(Type.AUTO_TURRET, center.getLocation(), level);
    }

    // ========== YARDIMCI METODLAR ==========

    /**
     * 3x3 platform kontrolü (merkez hariç, 8 blok kenar)
     */
    private boolean check3x3Platform(Block center, Material... validMaterials) {
        Set<Material> validSet = new HashSet<>(Arrays.asList(validMaterials));

        int validBlocks = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0)
                    continue; // Merkezi atla

                Block checkBlock = center.getRelative(x, 0, z);
                if (validSet.contains(checkBlock.getType())) {
                    validBlocks++;
                }
            }
        }

        // En az 6/8 blok doğru olmalı (tolerans)
        return validBlocks >= 6;
    }

    /**
     * Yapı aktivasyon efektleri
     */
    private void activateStructureEffects(Player player, Structure structure) {
        Location loc = structure.getLocation();
        World world = loc.getWorld();
        if (world == null)
            return;

        // Partikül efektleri
        world.spawnParticle(Particle.TOTEM, loc, 50, 1, 1, 1, 0.3);
        world.spawnParticle(Particle.END_ROD, loc, 30, 0.5, 0.5, 0.5, 0.1);

        // Ses
        world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.5f);

        // Title
        player.sendTitle("§6§lYAPI AKTİVE!",
                "§e" + getStructureName(structure.getType()),
                10, 40, 10);
    }

    /**
     * Yapı ismini al (Türkçe)
     */
    private String getStructureName(Type type) {
        switch (type) {
            case ALCHEMY_TOWER:
                return "Simya Kulesi";
            case POISON_REACTOR:
                return "Zehir Reaktörü";
            case TECTONIC_STABILIZER:
                return "Tektonik Sabitleyici";
            case WATCHTOWER:
                return "Gözetleme Kulesi";
            case AUTO_TURRET:
                return "Otomatik Taret";
            case SIEGE_FACTORY:
                return "Kuşatma Fabrikası";
            case HEALING_BEACON:
                return "Şifa Kulesi";
            case GLOBAL_MARKET_GATE:
                return "Global Pazar Kapısı";
            default:
                return type.name();
        }
    }

    /**
     * Cooldown kontrolü
     */
    private boolean isOnCooldown(UUID playerId) {
        if (!activationCooldowns.containsKey(playerId))
            return false;

        long lastTime = activationCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastTime) < ACTIVATION_COOLDOWN;
    }

    /**
     * Cooldown ayarla
     */
    private void setCooldown(UUID playerId) {
        activationCooldowns.put(playerId, System.currentTimeMillis());
    }
}
