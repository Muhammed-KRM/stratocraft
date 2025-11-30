package me.mami.stratocraft.manager;

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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Yapı oluşturma yardımcı sınıfı
 * Şema dosyalarını yükler ve dünyaya yerleştirir
 */
public class StructureBuilder {

    /**
     * Şema dosyasını yükle ve dünyaya yerleştir
     * 
     * @param location      Yerleştirilecek konum
     * @param schematicName Şema dosya adı (uzantı olmadan)
     * @return Başarılı olursa true
     */
    public static boolean pasteSchematic(Location location, String schematicName) {
        File file = new File(Main.getInstance().getDataFolder() + "/schematics/" + schematicName + ".schem");

        if (!file.exists()) {
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
     * Şema dosyası var mı kontrol et
     */
    public static boolean schematicExists(String schematicName) {
        File file = new File(Main.getInstance().getDataFolder() + "/schematics/" + schematicName + ".schem");
        return file.exists();
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
