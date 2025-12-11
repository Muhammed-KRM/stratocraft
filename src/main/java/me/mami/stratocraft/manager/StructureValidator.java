package me.mami.stratocraft.manager;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Yapı Doğrulama Sistemi
 * 
 * ⚠️ PERFORMANS: Async işlem kullanır (main thread'i bloklamaz)
 */
public class StructureValidator {

    /**
     * Async yapı doğrulama (main thread'i bloklamaz)
     * 
     * ⚠️ NOT: Block okuma işlemi main thread'de yapılmalı (World API thread-safe değil)
     * Bu yüzden dosya okuma async'te yapılır, block kontrolü main thread'de yapılır
     * 
     * @param centerBlock Yapının merkez bloğu
     * @param schematicName Şema dosya adı (.schem uzantısı olmadan)
     * @param callback Doğrulama sonucu (true/false)
     */
    public void validateAsync(Location centerBlock, String schematicName, Consumer<Boolean> callback) {
        if (centerBlock == null || schematicName == null || callback == null) {
            if (callback != null) callback.accept(false);
            return;
        }
        
        // Async thread'de şema dosyasını oku (I/O işlemi)
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            // Şema dosyasını oku ve block kontrol listesini hazırla
            java.util.List<BlockCheck> blockChecks = readSchematic(schematicName);
            
            if (blockChecks == null || blockChecks.isEmpty()) {
                // Şema okunamadı, main thread'de callback çağır
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    callback.accept(false);
                });
                return;
            }
            
            // Block kontrolünü main thread'de yap (World API thread-safe değil)
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                boolean result = checkBlocks(centerBlock, blockChecks);
                callback.accept(result);
            });
        });
    }
    
    /**
     * Şema dosyasını oku ve block kontrol listesini hazırla (async-safe)
     */
    private java.util.List<BlockCheck> readSchematic(String schematicName) {
        File file = new File(Main.getInstance().getDataFolder() + "/schematics/" + schematicName + ".schem");
        if (!file.exists()) {
            Main.getInstance().getLogger().warning("Şema bulunamadı: " + schematicName);
            return null;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Main.getInstance().getLogger().warning("Şema formatı desteklenmiyor: " + schematicName);
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             ClipboardReader reader = format.getReader(fis)) {
            Clipboard clipboard = reader.read();
            BlockVector3 origin = clipboard.getOrigin();
            
            java.util.List<BlockCheck> blockChecks = new java.util.ArrayList<>();
            for (BlockVector3 vec : clipboard.getRegion()) {
                String schemaMaterial = clipboard.getBlock(vec).getBlockType().getId().replace("minecraft:", "").toUpperCase();
                
                if (schemaMaterial.equals("AIR") || schemaMaterial.equals("CAVE_AIR")) continue;

                int relX = vec.getX() - origin.getX();
                int relY = vec.getY() - origin.getY();
                int relZ = vec.getZ() - origin.getZ();
                
                blockChecks.add(new BlockCheck(relX, relY, relZ, schemaMaterial));
            }
            
            return blockChecks;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Block kontrolü yap (main thread'de çalışmalı)
     */
    private boolean checkBlocks(Location centerBlock, java.util.List<BlockCheck> blockChecks) {
        if (centerBlock == null || blockChecks == null) return false;
        
        for (BlockCheck check : blockChecks) {
            Block worldBlock = centerBlock.clone().add(check.relX, check.relY, check.relZ).getBlock();
            String worldMaterial = worldBlock.getType().name();
            
            if (!worldMaterial.equals(check.expectedMaterial)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Senkron yapı doğrulama (eski metod - geriye dönük uyumluluk)
     * ⚠️ UYARI: Bu metod main thread'de çalışır, büyük yapılar için lag spike oluşturabilir!
     * 
     * @deprecated validateAsync() kullanın
     */
    @Deprecated
    public boolean validate(Location centerBlock, String schematicName) {
        File file = new File(Main.getInstance().getDataFolder() + "/schematics/" + schematicName + ".schem");
        if (!file.exists()) {
            Main.getInstance().getLogger().warning("Şema bulunamadı: " + schematicName);
            return false;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            Main.getInstance().getLogger().warning("Şema formatı desteklenmiyor: " + schematicName);
            return false;
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             ClipboardReader reader = format.getReader(fis)) {
            Clipboard clipboard = reader.read();
            BlockVector3 origin = clipboard.getOrigin();

            for (BlockVector3 vec : clipboard.getRegion()) {
                String schemaMaterial = clipboard.getBlock(vec).getBlockType().getId().replace("minecraft:", "").toUpperCase();
                
                if (schemaMaterial.equals("AIR") || schemaMaterial.equals("CAVE_AIR")) continue;

                int relX = vec.getX() - origin.getX();
                int relY = vec.getY() - origin.getY();
                int relZ = vec.getZ() - origin.getZ();

                Block worldBlock = centerBlock.clone().add(relX, relY, relZ).getBlock();
                String worldMaterial = worldBlock.getType().name();

                if (!worldMaterial.equals(schemaMaterial)) {
                    return false; 
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Block kontrol verisi (async-safe)
     */
    private static class BlockCheck {
        final int relX, relY, relZ;
        final String expectedMaterial;
        
        BlockCheck(int relX, int relY, int relZ, String expectedMaterial) {
            this.relX = relX;
            this.relY = relY;
            this.relZ = relZ;
            this.expectedMaterial = expectedMaterial;
        }
    }
