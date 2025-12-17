package me.mami.stratocraft.listener;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.enums.StructureType;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TerritoryManager;
import me.mami.stratocraft.manager.clan.ClanRankSystem;
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
    private final ClanRankSystem rankSystem;

    // Cooldown: Oyuncu UUID -> Son aktivasyon zamanı
    private final Map<UUID, Long> activationCooldowns = new HashMap<>();
    private static final long ACTIVATION_COOLDOWN = 5000L; // 5 saniye

    public StructureActivationListener(ClanManager cm, TerritoryManager tm, ClanRankSystem rankSystem) {
        this.clanManager = cm;
        this.territoryManager = tm;
        this.rankSystem = rankSystem;
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

        // Pattern kontrolü - önce pattern'i kontrol et
        Structure detectedStructure = detectStructurePattern(clicked, player);
        if (detectedStructure == null)
            return;

        // Kişisel yapılar (klan zorunlu değil)
        // Geriye uyumluluk için Structure.Type'dan StructureType'a çevir
        StructureType detectedType = StructureType.valueOf(detectedStructure.getType().name());
        if (detectedType == StructureType.PERSONAL_MISSION_GUILD ||
            detectedType == StructureType.CONTRACT_OFFICE ||
            detectedType == StructureType.MARKET_PLACE ||
            detectedType == StructureType.RECIPE_LIBRARY) {
            
            // Kişisel yapılar için klan kontrolü yok
            // Yapıyı en yakın klana ekle (varsa) veya geçici olarak sakla
            Clan nearbyClan = territoryManager.getTerritoryOwner(clicked.getLocation());
            if (nearbyClan != null) {
                nearbyClan.addStructure(detectedStructure);
            } else {
                // Klansız bölgede - geçici yapı (ileride global yapı sistemi eklenebilir)
                // Şimdilik en yakın klana ekle (oyuncunun klanı varsa)
                Clan playerClan = clanManager.getClanByPlayer(player.getUniqueId());
                if (playerClan != null) {
                    playerClan.addStructure(detectedStructure);
                }
            }
            
            event.setCancelled(true);
            setCooldown(player.getUniqueId());
            activateStructureEffects(player, detectedStructure);
            player.sendMessage("§a§l" + getStructureName(detectedStructure.getType()) +
                    " AKTİVE EDİLDİ! (Seviye " + detectedStructure.getLevel() + ")");
            player.playSound(clicked.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            return;
        }

        // Klan yapıları için kontrol
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cKlan yapıları için bir klana üye olmanız gerekiyor!");
            return;
        }

        // Klan bölgesinde mi?
        Clan owner = territoryManager.getTerritoryOwner(clicked.getLocation());
        if (owner == null || !owner.equals(clan)) {
            player.sendMessage("§cKlan yapıları sadece kendi bölgenizde kurulabilir!");
            return;
        }

        // YENİ: Yetki kontrolü (ClanRankSystem kullan)
        if (rankSystem != null) {
            if (!rankSystem.hasPermission(clan, player.getUniqueId(), 
                    ClanRankSystem.Permission.BUILD_STRUCTURE)) {
                player.sendMessage("§cYapı kurma yetkiniz yok!");
                return;
            }
        } else {
            // RankSystem yoksa eski kontrol
            if (clan.getRank(player.getUniqueId()) == Clan.Rank.RECRUIT) {
                player.sendMessage("§cAcemilerin yapı kurma yetkisi yok!");
                return;
            }
        }

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

        // 6. KİŞİSEL GÖREV LONCASI (Personal Mission Guild) - Lectern + Taş
        Structure personalMissionGuild = checkPersonalMissionGuild(center);
        if (personalMissionGuild != null)
            return personalMissionGuild;

        // 7. KLAN YÖNETİM MERKEZİ (Clan Management Center) - Beacon + Demir Bloğu
        Structure clanManagementCenter = checkClanManagementCenter(center);
        if (clanManagementCenter != null)
            return clanManagementCenter;

        // 8. KLAN BANKASI (Clan Bank) - Ender Chest + Demir Bloğu
        Structure clanBank = checkClanBank(center);
        if (clanBank != null)
            return clanBank;

        // 9. KLAN GÖREV LONCASI (Clan Mission Guild) - Lectern + Demir Bloğu
        Structure clanMissionGuild = checkClanMissionGuild(center);
        if (clanMissionGuild != null)
            return clanMissionGuild;

        // 10. EĞİTİM ALANI (Training Arena) - Enchanting Table + Demir Bloğu
        Structure trainingArena = checkTrainingArena(center);
        if (trainingArena != null)
            return trainingArena;

        // 11. KERVAN İSTASYONU (Caravan Station) - Chest + Demir Bloğu
        Structure caravanStation = checkCaravanStation(center);
        if (caravanStation != null)
            return caravanStation;

        // 12. KONTRAT BÜROSU (Contract Office) - Anvil + Taş
        Structure contractOffice = checkContractOffice(center);
        if (contractOffice != null)
            return contractOffice;

        // 13. MARKET (Market Place) - Chest + Sign + Taş
        Structure marketPlace = checkMarketPlace(center);
        if (marketPlace != null)
            return marketPlace;

        // 14. TARİF KÜTÜPHANESİ (Recipe Library) - Lectern + Bookshelf
        Structure recipeLibrary = checkRecipeLibrary(center);
        if (recipeLibrary != null)
            return recipeLibrary;

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

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.ALCHEMY_TOWER.name()), center.getLocation(), level);
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

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.POISON_REACTOR.name()), center.getLocation(), level);
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

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.TECTONIC_STABILIZER.name()), center.getLocation(), level);
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

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.WATCHTOWER.name()), center.getLocation(), level);
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

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.AUTO_TURRET.name()), center.getLocation(), level);
    }

    // ========== YÖNETİM YAPILARI PATTERN KONTROLLERİ ==========

    /**
     * Kişisel Görev Loncası: Lectern + 2x2 Taş taban (her yere yapılabilir)
     */
    private Structure checkPersonalMissionGuild(Block center) {
        if (center.getType() != Material.LECTERN)
            return null;

        // 2x2 Taş taban kontrolü
        Block below = center.getRelative(BlockFace.DOWN);
        int stoneCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.STONE || 
                    checkBlock.getType() == Material.COBBLESTONE ||
                    checkBlock.getType() == Material.STONE_BRICKS) {
                    stoneCount++;
                }
            }
        }

        if (stoneCount < 3) // En az 3/4 blok
            return null;

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.PERSONAL_MISSION_GUILD.name()), center.getLocation(), 1);
    }

    /**
     * Klan Yönetim Merkezi: Beacon + 3x3 Demir Bloğu taban
     */
    private Structure checkClanManagementCenter(Block center) {
        if (center.getType() != Material.BEACON)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        if (!check3x3Platform(below, Material.IRON_BLOCK)) {
            return null;
        }

        // Seviye: Demir Bloğu sayısına göre
        int ironCount = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.IRON_BLOCK) {
                    ironCount++;
                }
            }
        }

        int level = 1;
        if (ironCount >= 6) level = 2;
        if (ironCount >= 8) level = 3;

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.CLAN_MANAGEMENT_CENTER.name()), center.getLocation(), level);
    }

    /**
     * Klan Bankası: Ender Chest + 2x2 Demir Bloğu taban
     */
    private Structure checkClanBank(Block center) {
        if (center.getType() != Material.ENDER_CHEST)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        int ironCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.IRON_BLOCK) {
                    ironCount++;
                }
            }
        }

        if (ironCount < 3)
            return null;

        int level = ironCount >= 4 ? 2 : 1;
        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.CLAN_BANK.name()), center.getLocation(), level);
    }

    /**
     * Klan Görev Loncası: Lectern + 2x2 Demir Bloğu taban
     */
    private Structure checkClanMissionGuild(Block center) {
        if (center.getType() != Material.LECTERN)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        int ironCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.IRON_BLOCK) {
                    ironCount++;
                }
            }
        }

        if (ironCount < 3)
            return null;

        int level = ironCount >= 4 ? 2 : 1;
        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.CLAN_MISSION_GUILD.name()), center.getLocation(), level);
    }

    /**
     * Eğitim Alanı: Enchanting Table + 2x2 Demir Bloğu taban
     */
    private Structure checkTrainingArena(Block center) {
        if (center.getType() != Material.ENCHANTING_TABLE)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        int ironCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.IRON_BLOCK) {
                    ironCount++;
                }
            }
        }

        if (ironCount < 3)
            return null;

        int level = ironCount >= 4 ? 2 : 1;
        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.TRAINING_ARENA.name()), center.getLocation(), level);
    }

    /**
     * Kervan İstasyonu: Chest + 2x2 Demir Bloğu taban
     */
    private Structure checkCaravanStation(Block center) {
        if (center.getType() != Material.CHEST)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        int ironCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.IRON_BLOCK) {
                    ironCount++;
                }
            }
        }

        if (ironCount < 3)
            return null;

        int level = ironCount >= 4 ? 2 : 1;
        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.CARAVAN_STATION.name()), center.getLocation(), level);
    }

    /**
     * Kontrat Bürosu: Anvil + 2x2 Taş taban (her yere yapılabilir)
     */
    private Structure checkContractOffice(Block center) {
        if (center.getType() != Material.ANVIL && 
            center.getType() != Material.CHIPPED_ANVIL &&
            center.getType() != Material.DAMAGED_ANVIL)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        int stoneCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.STONE || 
                    checkBlock.getType() == Material.COBBLESTONE ||
                    checkBlock.getType() == Material.STONE_BRICKS) {
                    stoneCount++;
                }
            }
        }

        if (stoneCount < 3)
            return null;

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.CONTRACT_OFFICE.name()), center.getLocation(), 1);
    }

    /**
     * Market: Chest + Sign + 2x2 Taş taban (her yere yapılabilir)
     */
    private Structure checkMarketPlace(Block center) {
        if (center.getType() != Material.CHEST)
            return null;

        // Sign kontrolü (yanında veya üstünde)
        boolean hasSign = false;
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP}) {
            Block checkBlock = center.getRelative(face);
            if (checkBlock.getType().name().contains("SIGN")) {
                hasSign = true;
                break;
            }
        }

        if (!hasSign)
            return null;

        Block below = center.getRelative(BlockFace.DOWN);
        int stoneCount = 0;
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                Block checkBlock = below.getRelative(x, 0, z);
                if (checkBlock.getType() == Material.STONE || 
                    checkBlock.getType() == Material.COBBLESTONE ||
                    checkBlock.getType() == Material.STONE_BRICKS) {
                    stoneCount++;
                }
            }
        }

        if (stoneCount < 3)
            return null;

        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.MARKET_PLACE.name()), center.getLocation(), 1);
    }

    /**
     * Tarif Kütüphanesi: Lectern + Bookshelf yanında (her yere yapılabilir)
     */
    private Structure checkRecipeLibrary(Block center) {
        if (center.getType() != Material.LECTERN)
            return null;

        // Bookshelf kontrolü (yanında en az 2 tane)
        int bookshelfCount = 0;
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block checkBlock = center.getRelative(face);
            if (checkBlock.getType() == Material.BOOKSHELF || 
                checkBlock.getType() == Material.CHISELED_BOOKSHELF) {
                bookshelfCount++;
            }
        }

        if (bookshelfCount < 2)
            return null;

        int level = bookshelfCount >= 4 ? 2 : 1;
        // Geriye uyumluluk için StructureType'dan Structure.Type'a çevir
        return new Structure(Structure.Type.valueOf(StructureType.RECIPE_LIBRARY.name()), center.getLocation(), level);
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
     * ✅ GÜÇLENDİRİLDİ: Daha fazla partikül, ses ve görsel efekt
     */
    private void activateStructureEffects(Player player, Structure structure) {
        Location loc = structure.getLocation();
        World world = loc.getWorld();
        if (world == null)
            return;

        // ✅ GÜÇLENDİRİLDİ: Daha fazla partikül efektleri (görünürlük için - balista gibi)
        Location effectLoc = loc.clone().add(0, 1, 0);
        
        // Ana partikül patlaması (balista gibi)
        world.spawnParticle(Particle.EXPLOSION_LARGE, effectLoc, 3, 0, 0, 0, 0);
        world.spawnParticle(Particle.SMOKE_LARGE, effectLoc, 30, 1.5, 1.5, 1.5, 0.1);
        
        // Renkli partiküller
        world.spawnParticle(Particle.TOTEM, effectLoc, 100, 1.5, 1.5, 1.5, 0.5);
        world.spawnParticle(Particle.END_ROD, effectLoc, 50, 1.0, 1.0, 1.0, 0.2);
        world.spawnParticle(Particle.VILLAGER_HAPPY, effectLoc, 30, 0.8, 0.8, 0.8, 0.1);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, effectLoc, 40, 1.2, 1.2, 1.2, 0.3);
        
        // Havai fişek efekti (balista gibi)
        org.bukkit.entity.Firework firework = (org.bukkit.entity.Firework) world.spawnEntity(
            effectLoc, org.bukkit.entity.EntityType.FIREWORK);
        org.bukkit.inventory.meta.FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(org.bukkit.FireworkEffect.builder()
            .with(org.bukkit.FireworkEffect.Type.BURST)
            .withColor(org.bukkit.Color.GREEN, org.bukkit.Color.YELLOW, org.bukkit.Color.AQUA)
            .flicker(true).trail(true).build());
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
        
        // ✅ GÜÇLENDİRİLDİ: Daha fazla ses efekti
        world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1.5f);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        // ✅ GÜÇLENDİRİLDİ: Daha görünür title ve actionbar
        player.sendTitle("§6§l⚡ YAPI AKTİVE! ⚡",
                "§e" + getStructureName(structure.getType()) + " §7(Seviye " + structure.getLevel() + ")",
                10, 60, 20);
        player.sendActionBar("§a§l✓ " + getStructureName(structure.getType()) + " başarıyla aktifleştirildi!");
        
        // ✅ GÜÇLENDİRİLDİ: Yapı etrafında ışık efekti (glow)
        Main plugin = Main.getInstance();
        if (plugin != null) {
            for (int i = 0; i < 5; i++) {
                final int delay = i * 4; // Her 4 tick'te bir
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (world != null && loc.getWorld() != null) {
                        world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 20, 1.5, 1.5, 1.5, 0.1);
                    }
                }, delay);
            }
        }
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
            // Yönetim & Menü Yapıları
            case PERSONAL_MISSION_GUILD:
                return "Kişisel Görev Loncası";
            case CLAN_MANAGEMENT_CENTER:
                return "Klan Yönetim Merkezi";
            case CLAN_BANK:
                return "Klan Bankası";
            case CLAN_MISSION_GUILD:
                return "Klan Görev Loncası";
            case TRAINING_ARENA:
                return "Eğitim Alanı";
            case CARAVAN_STATION:
                return "Kervan İstasyonu";
            case CONTRACT_OFFICE:
                return "Kontrat Bürosu";
            case MARKET_PLACE:
                return "Market";
            case RECIPE_LIBRARY:
                return "Tarif Kütüphanesi";
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
