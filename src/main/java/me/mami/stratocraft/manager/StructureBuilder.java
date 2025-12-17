package me.mami.stratocraft.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import me.mami.stratocraft.Main;

/**
 * Yapı oluşturma yardımcı sınıfı
 * Şema dosyalarını yükler ve dünyaya yerleştirir
 */
public class StructureBuilder {

    /**
     * Şema dosyasını yükle ve dünyaya yerleştir
     * Önce FAWE dener, yoksa normal WorldEdit kullanır
     *
     * @param location      Yerleştirilecek konum
     * @param schematicName Şema dosya adı (uzantı olmadan veya tam yol)
     * @return Başarılı olursa true
     */
    public static boolean pasteSchematic(Location location, String schematicName) {
        // WorldEdit yüklü mü kontrol et
        if (!Main.isWorldEditAvailable()) {
            Main.getInstance().getLogger().warning("WorldEdit yüklü değil! Schematic paste edilemedi: " + schematicName);
            createPlaceholderStructure(location); // Yer tutucu yapı oluştur
            return false;
        }
        
        // FAWE varsa onu kullan (daha hızlı)
        if (hasFAWE()) {
            return pasteSchematicFAWE(location, schematicName);
        }

        // Normal WorldEdit kullan
        return pasteSchematicWorldEdit(location, schematicName);
    }
    
    /**
     * FAWE yüklü mü kontrol et
     */
    private static boolean hasFAWE() {
        try {
            Class.forName("com.fastasyncworldedit.core.FaweAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * FAWE ile şema yükle (async, daha hızlı)
     * Not: FAWE WorldEdit API'si ile uyumludur, normal WorldEdit metodunu kullanır
     * FAWE'nin avantajı async çalışmasıdır, API aynıdır
     */
    private static boolean pasteSchematicFAWE(Location location, String schematicName) {
        // FAWE varsa normal WorldEdit API'sini kullan (FAWE otomatik async yapar)
        // FAWE WorldEdit API'si ile uyumlu olduğu için aynı kodu kullanabiliriz
        return pasteSchematicWorldEdit(location, schematicName);
    }
    
    /**
     * Normal WorldEdit ile şema yükle
     */
    private static boolean pasteSchematicWorldEdit(Location location, String schematicName) {
        File file = findSchematicFile(schematicName);

        if (file == null || !file.exists()) {
            return false;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(file);
                ClipboardReader reader = format.getReader(fis)) {

            Clipboard clipboard = reader.read();

            // WorldEdit API null kontrolü
            WorldEdit worldEdit = WorldEdit.getInstance();
            if (worldEdit == null) {
                Main.getInstance().getLogger().warning("WorldEdit API bulunamadı! Şema yüklenemedi: " + schematicName);
                return false;
            }

            // Bukkit World'ü WorldEdit World'e çevir (WorldEdit 7.2.9 için)
            com.sk89q.worldedit.world.World weWorld;
            try {
                // WorldEdit 7.x'te adapt() metodu kullanılır
                weWorld = BukkitAdapter.adapt(location.getWorld());
            } catch (Exception e) {
                // Eğer adapt() yoksa, alternatif yöntem dene
                Main.getInstance().getLogger().warning("WorldEdit adaptasyonu başarısız: " + e.getMessage());
                return false;
            }

            try (EditSession editSession = worldEdit.newEditSession(weWorld)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);
                return true;
            }
        } catch (IOException | WorldEditException e) {
            Main.getInstance().getLogger().warning("Şema yüklenirken hata: " + schematicName + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Şema dosyasını bul (tam yol veya kısa isim)
     */
    private static File findSchematicFile(String schematicName) {
        File dataFolder = Main.getInstance().getDataFolder();
        File schematicsDir = new File(dataFolder, "schematics");
        
        // Eğer zaten tam yol ise
        if (schematicName.contains("/")) {
            File file = new File(schematicsDir, schematicName + ".schem");
            if (file.exists()) {
                return file;
            }
            // .schematic uzantısını da dene
            file = new File(schematicsDir, schematicName + ".schematic");
            if (file.exists()) {
                return file;
            }
        } else {
            // Kısa isim ise schematics/ klasöründe ara
            File file = new File(schematicsDir, schematicName + ".schem");
            if (file.exists()) {
                return file;
            }
            file = new File(schematicsDir, schematicName + ".schematic");
            if (file.exists()) {
                return file;
            }
        }
        
        return null;
    }
    
    /**
     * Şema dosyası var mı kontrol et
     */
    public static boolean schematicExists(String schematicName) {
        File file = findSchematicFile(schematicName);
        return file != null && file.exists();
    }
    
    /**
     * Şema klasörlerini oluştur (otomatik)
     */
    public static void createSchematicDirectories() {
        File dataFolder = Main.getInstance().getDataFolder();
        File schematicsDir = new File(dataFolder, "schematics");
        File dungeonsDir = new File(schematicsDir, "dungeons");
        File biomesDir = new File(schematicsDir, "biomes");
        File biomesStructuresDir = new File(biomesDir, "structures");
        File biomesCustomDir = new File(biomesDir, "custom");
        
        // Ana klasörler
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        }
        
        // Zindan klasörleri
        if (!dungeonsDir.exists()) {
            dungeonsDir.mkdirs();
        }
        for (int level = 1; level <= 5; level++) {
            File levelDir = new File(dungeonsDir, "level" + level);
            if (!levelDir.exists()) {
                levelDir.mkdirs();
            }
        }
        
        // Biyom klasörleri
        if (!biomesDir.exists()) {
            biomesDir.mkdirs();
        }
        if (!biomesStructuresDir.exists()) {
            biomesStructuresDir.mkdirs();
        }
        if (!biomesCustomDir.exists()) {
            biomesCustomDir.mkdirs();
        }
        
        Main.getInstance().getLogger().info("Şema klasörleri oluşturuldu: " + schematicsDir.getAbsolutePath());
    }

    /**
     * Şema bulunamazsa yer tutucu yapı oluştur (3 kırmızı yün kulesi)
     */
    public static void createPlaceholderStructure(Location location) {
        World world = location.getWorld();
        if (world == null)
            return;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        // 3 kırmızı yün üst üste
        world.getBlockAt(x, y, z).setType(Material.RED_WOOL);
        world.getBlockAt(x, y + 1, z).setType(Material.RED_WOOL);
        world.getBlockAt(x, y + 2, z).setType(Material.RED_WOOL);
    }

    /**
     * Konumdaki blokları temizle (hava yap)
     */
    public static void clearArea(Location center, int radiusX, int radiusY, int radiusZ) {
        World world = center.getWorld();
        if (world == null)
            return;

        for (int x = -radiusX; x <= radiusX; x++) {
            // Sadece merkezden yukarıyı temizle (yere gömülmeyi önle)
            // y=0 (zemin) ve yukarısı
            for (int y = 0; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    Block block = world.getBlockAt(
                            center.getBlockX() + x,
                            center.getBlockY() + y,
                            center.getBlockZ() + z);

                    // Spawn veya klan alanı kontrolü
                    if (isProtectedLocation(block.getLocation())) {
                        continue;
                    }

                    block.setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Konum korumalı mı kontrol et (spawn veya klan alanı)
     */
    private static boolean isProtectedLocation(Location loc) {
        // Spawn kontrolü
        Location spawn = loc.getWorld().getSpawnLocation();
        if (spawn != null && loc.distance(spawn) < 50) {
            return true;
        }

        // Klan alanı kontrolü (TerritoryManager'dan)
        Main plugin = Main.getInstance();
        if (plugin != null && plugin.getTerritoryManager() != null) {
            return plugin.getTerritoryManager().getTerritoryOwner(loc) != null;
        }

        return false;
    }
}
