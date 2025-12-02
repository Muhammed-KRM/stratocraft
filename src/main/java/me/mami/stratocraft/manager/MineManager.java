package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Mayın Sistemi
 * 
 * Basınç plakası tabanlı, görünür mayınlar
 * Basınca yok olur, kolay yapılır
 * Farklı etkiler: Patlama, Yıldırım, Zehir, Körlük, Madencinin Yorgunluğu
 */
public class MineManager {
    private final Main plugin;
    private final ClanManager clanManager;

    // Aktif mayınlar (Location -> MineData)
    private final Map<Location, MineData> activeMines = new HashMap<>();
    // Mayın isim standları (Location -> ArmorStand)
    private final Map<Location, ArmorStand> mineNameStands = new HashMap<>();

    private File minesFile;
    private FileConfiguration minesConfig;

    public enum MineType {
        EXPLOSIVE, // Patlama (TNT) - Alan hasarı
        LIGHTNING, // Yıldırım (Lightning Core) - Tek hedef
        POISON, // Zehir (Spider Eye) - Tek hedef
        BLINDNESS, // Körlük (Ink Sac) - Tek hedef
        FATIGUE, // Madencinin Yorgunluğu (Iron Pickaxe) - Tek hedef
        SLOWNESS, // Yavaşlık (Slime Ball) - Tek hedef
        FIRE, // Ateş (Blaze Rod) - Yanma efekti
        FREEZE, // Dondurma (Ice) - Buz efekti
        WEAKNESS, // Zayıflık (Bone) - Zayıflık efekti
        CONFUSION // Karışıklık (Fermented Spider Eye) - Nausea efekti
    }

    public static class MineData {
        private final UUID ownerId;
        private final UUID ownerClanId;
        private final MineType type;
        private final Location location;

        public MineData(UUID ownerId, UUID ownerClanId, MineType type, Location location) {
            this.ownerId = ownerId;
            this.ownerClanId = ownerClanId;
            this.type = type;
            this.location = location;
        }

        public UUID getOwnerId() {
            return ownerId;
        }

        public UUID getOwnerClanId() {
            return ownerClanId;
        }

        public MineType getType() {
            return type;
        }

        public Location getLocation() {
            return location;
        }
    }

    public MineManager(Main plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        loadMines();
    }

    /**
     * Mayın tipine göre basınç plakası tipi döndür
     */
    private Material getPressurePlateType(MineType type) {
        switch (type) {
            case EXPLOSIVE:
                return Material.STONE_PRESSURE_PLATE; // Kırmızımsı
            case LIGHTNING:
                return Material.LIGHT_WEIGHTED_PRESSURE_PLATE; // Altın
            case POISON:
                return Material.OAK_PRESSURE_PLATE; // Yeşilimsi
            case BLINDNESS:
                return Material.HEAVY_WEIGHTED_PRESSURE_PLATE; // Demir
            case FATIGUE:
                return Material.STONE_PRESSURE_PLATE;
            case SLOWNESS:
                return Material.OAK_PRESSURE_PLATE;
            case FIRE:
                return Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
            case FREEZE:
                return Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
            case WEAKNESS:
                return Material.STONE_PRESSURE_PLATE;
            case CONFUSION:
                return Material.OAK_PRESSURE_PLATE;
            default:
                return Material.STONE_PRESSURE_PLATE;
        }
    }

    /**
     * Mayın oluştur
     */
    public boolean createMine(Player player, Block pressurePlate, MineType type) {
        if (pressurePlate == null || player == null) {
            return false;
        }

        Location loc = pressurePlate.getLocation();

        // Zaten mayın var mı?
        if (activeMines.containsKey(loc)) {
            player.sendMessage("§cBu konumda zaten bir mayın var!");
            return false;
        }

        // Klan ID'sini al
        UUID clanId = null;
        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            clanId = clanManager.getClanByPlayer(player.getUniqueId()).getId();
        }

        // Basınç plakası tipini değiştir (mayın tipine göre)
        Material plateType = getPressurePlateType(type);
        if (pressurePlate.getType() != plateType) {
            pressurePlate.setType(plateType);
        }

        // MineData oluştur
        MineData mine = new MineData(player.getUniqueId(), clanId, type, loc);

        // Aktif mayınlar listesine ekle
        activeMines.put(loc, mine);

        // Metadata ekle
        pressurePlate.setMetadata("Mine", new FixedMetadataValue(plugin, true));
        pressurePlate.setMetadata("MineOwner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        pressurePlate.setMetadata("MineType", new FixedMetadataValue(plugin, type.name()));

        // Özel isim için görünmez armor stand oluştur
        Location standLoc = loc.clone().add(0.5, 0.1, 0.5);
        ArmorStand nameStand = (ArmorStand) loc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
        nameStand.setVisible(false);
        nameStand.setGravity(false);
        nameStand.setInvulnerable(true);
        nameStand.setCustomNameVisible(true);
        nameStand.setCustomName("§c§l" + getMineTypeName(type));
        nameStand.setMarker(true); // Görünmez ve etkileşimsiz
        nameStand.setSmall(true);
        nameStand.setHeadPose(new EulerAngle(0, 0, 0));
        nameStand.setMetadata("MineNameStand", new FixedMetadataValue(plugin, true));
        
        mineNameStands.put(loc, nameStand);

        // Kaydet
        saveMines();

        player.sendMessage("§a§lMayın yerleştirildi: §e" + getMineTypeName(type));
        return true;
    }

    /**
     * Mayın tetikle
     */
    public void triggerMine(Block pressurePlate, Player victim) {
        if (pressurePlate == null || victim == null) {
            return;
        }

        Location loc = pressurePlate.getLocation();
        MineData mine = activeMines.get(loc);

        if (mine == null) {
            return;
        }

        // Klan kontrolü - Dostlar korunur
        if (mine.getOwnerClanId() != null && victim != null) {
            me.mami.stratocraft.model.Clan victimClan = clanManager.getClanByPlayer(victim.getUniqueId());
            if (victimClan != null && victimClan.getId().equals(mine.getOwnerClanId())) {
                return; // Aynı klan, mayın tetiklenmez
            }
        }

        // Sahip kontrolü KALDIRILDI - Herkes basınca mayın tetiklenir

        // Mayın etkisini uygula
        applyMineEffect(mine, victim, pressurePlate.getLocation());

        // Mayını kaldır (basınca yok olur)
        removeMine(loc);

        // Basınç plakasını kaldır
        pressurePlate.setType(Material.AIR);
    }

    /**
     * Mayın etkisini uygula
     */
    private void applyMineEffect(MineData mine, Player victim, Location mineLoc) {
        switch (mine.getType()) {
            case EXPLOSIVE:
                // Patlama - Alan hasarı
                mineLoc.getWorld().createExplosion(mineLoc, 3.0f, false, false);
                victim.sendMessage("§c§lMAYIN PATLADI!");
                break;

            case LIGHTNING:
                // Yıldırım - Tek hedef
                mineLoc.getWorld().strikeLightning(mineLoc);
                victim.damage(5.0);
                victim.sendMessage("§e§lYILDIRIM ÇARPTI!");
                break;

            case POISON:
                // Zehir - Tek hedef
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false, false));
                victim.sendMessage("§2§lZEHİRLENDİN!");
                break;

            case BLINDNESS:
                // Körlük - Tek hedef
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false, false));
                victim.sendMessage("§8§lKÖR OLDUN!");
                break;

            case FATIGUE:
                // Madencinin Yorgunluğu - Tek hedef
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 2, false, false));
                victim.sendMessage("§7§lYORGUNLUK HİSSEDİYORSUN!");
                break;

            case SLOWNESS:
                // Yavaşlık - Tek hedef
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1, false, false));
                victim.sendMessage("§b§lYAVAŞLADIN!");
                break;

            case FIRE:
                // Ateş - Yanma efekti
                victim.setFireTicks(100); // 5 saniye yanma
                victim.damage(2.0); // İlk hasar
                victim.sendMessage("§c§lYANDIN!");
                mineLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, 
                    mineLoc.clone().add(0.5, 0.1, 0.5), 30, 0.3, 0.1, 0.3, 0.1);
                break;

            case FREEZE:
                // Dondurma - Buz efekti
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 150, 2, false, false)); // Çok yavaş
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 150, 1, false, false));
                victim.sendMessage("§b§lDONDUN!");
                mineLoc.getWorld().spawnParticle(org.bukkit.Particle.SNOWBALL, 
                    mineLoc.clone().add(0.5, 0.1, 0.5), 30, 0.3, 0.1, 0.3, 0.1);
                break;

            case WEAKNESS:
                // Zayıflık - Zayıflık efekti
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 1, false, false));
                victim.sendMessage("§7§lZAYIFLADIN!");
                break;

            case CONFUSION:
                // Karışıklık - Nausea efekti
                victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1, false, false));
                victim.sendMessage("§d§lKARIŞIKLIK HİSSEDİYORSUN!");
                break;
        }

        // Ses efekti
        mineLoc.getWorld().playSound(mineLoc, org.bukkit.Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);

        // Partikül efekti
        mineLoc.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE,
                mineLoc.clone().add(0.5, 0.1, 0.5), 20, 0.3, 0.1, 0.3, 0.1);
    }

    /**
     * Mayını kaldır
     */
    public void removeMine(Location loc) {
        MineData mine = activeMines.remove(loc);
        if (mine != null) {
            Block block = loc.getBlock();
            block.removeMetadata("Mine", plugin);
            block.removeMetadata("MineOwner", plugin);
            block.removeMetadata("MineType", plugin);
            
            // İsim standını kaldır
            ArmorStand nameStand = mineNameStands.remove(loc);
            if (nameStand != null && !nameStand.isDead()) {
                nameStand.remove();
            }
            
            saveMines();
        }
    }

    /**
     * Mayın tipi ismini al
     */
    public String getMineTypeName(MineType type) {
        switch (type) {
            case EXPLOSIVE:
                return "Patlama Mayını";
            case LIGHTNING:
                return "Yıldırım Mayını";
            case POISON:
                return "Zehir Mayını";
            case BLINDNESS:
                return "Körlük Mayını";
            case FATIGUE:
                return "Yorgunluk Mayını";
            case SLOWNESS:
                return "Yavaşlık Mayını";
            case FIRE:
                return "Ateş Mayını";
            case FREEZE:
                return "Dondurma Mayını";
            case WEAKNESS:
                return "Zayıflık Mayını";
            case CONFUSION:
                return "Karışıklık Mayını";
            default:
                return "Bilinmeyen Mayın";
        }
    }

    /**
     * Mayınları kaydet
     */
    private void saveMines() {
        if (minesFile == null) {
            minesFile = new File(plugin.getDataFolder(), "mines.yml");
        }

        if (minesConfig == null) {
            minesConfig = new YamlConfiguration();
        }

        minesConfig.set("mines", null); // Temizle

        int index = 0;
        for (Map.Entry<Location, MineData> entry : activeMines.entrySet()) {
            Location loc = entry.getKey();
            MineData mine = entry.getValue();

            String path = "mines." + index;
            minesConfig.set(path + ".world", loc.getWorld().getName());
            minesConfig.set(path + ".x", loc.getBlockX());
            minesConfig.set(path + ".y", loc.getBlockY());
            minesConfig.set(path + ".z", loc.getBlockZ());
            minesConfig.set(path + ".owner", mine.getOwnerId().toString());
            minesConfig.set(path + ".clan", mine.getOwnerClanId() != null ? mine.getOwnerClanId().toString() : null);
            minesConfig.set(path + ".type", mine.getType().name());

            index++;
        }

        try {
            minesConfig.save(minesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Mayınlar kaydedilemedi: " + e.getMessage());
        }
    }

    /**
     * Mayınları yükle
     */
    private void loadMines() {
        minesFile = new File(plugin.getDataFolder(), "mines.yml");

        if (!minesFile.exists()) {
            try {
                minesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Mayın dosyası oluşturulamadı: " + e.getMessage());
            }
            return;
        }

        minesConfig = YamlConfiguration.loadConfiguration(minesFile);

        if (!minesConfig.contains("mines")) {
            return;
        }

        for (String key : minesConfig.getConfigurationSection("mines").getKeys(false)) {
            String path = "mines." + key;

            String worldName = minesConfig.getString(path + ".world");
            int x = minesConfig.getInt(path + ".x");
            int y = minesConfig.getInt(path + ".y");
            int z = minesConfig.getInt(path + ".z");

            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                continue;
            }

            Location loc = new Location(world, x, y, z);

            UUID ownerId = UUID.fromString(minesConfig.getString(path + ".owner"));
            String clanIdStr = minesConfig.getString(path + ".clan");
            UUID clanId = clanIdStr != null ? UUID.fromString(clanIdStr) : null;
            MineType type = MineType.valueOf(minesConfig.getString(path + ".type"));

            MineData mine = new MineData(ownerId, clanId, type, loc);
            activeMines.put(loc, mine);

            // Metadata ekle
            Block block = loc.getBlock();
            if (block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                    block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE ||
                    block.getType() == Material.STONE_PRESSURE_PLATE ||
                    block.getType() == Material.OAK_PRESSURE_PLATE) {
                block.setMetadata("Mine", new FixedMetadataValue(plugin, true));
                block.setMetadata("MineOwner", new FixedMetadataValue(plugin, ownerId.toString()));
                block.setMetadata("MineType", new FixedMetadataValue(plugin, type.name()));
                
                // Basınç plakası tipini güncelle
                Material plateType = getPressurePlateType(type);
                if (block.getType() != plateType) {
                    block.setType(plateType);
                }
                
                // Özel isim için görünmez armor stand oluştur
                Location standLoc = loc.clone().add(0.5, 0.1, 0.5);
                ArmorStand nameStand = (ArmorStand) loc.getWorld().spawnEntity(standLoc, EntityType.ARMOR_STAND);
                nameStand.setVisible(false);
                nameStand.setGravity(false);
                nameStand.setInvulnerable(true);
                nameStand.setCustomNameVisible(true);
                nameStand.setCustomName("§c§l" + getMineTypeName(type));
                nameStand.setMarker(true);
                nameStand.setSmall(true);
                nameStand.setHeadPose(new EulerAngle(0, 0, 0));
                nameStand.setMetadata("MineNameStand", new FixedMetadataValue(plugin, true));
                
                mineNameStands.put(loc, nameStand);
            }
        }

        plugin.getLogger().info("Mayınlar yüklendi: " + activeMines.size() + " mayın");
    }

    /**
     * Oyuncu çıkışında temizle
     */
    public void clearPlayerData(Player player) {
        // Sahip olduğu mayınları kaldır (opsiyonel)
        // Şimdilik sadece boş metod
    }
}
