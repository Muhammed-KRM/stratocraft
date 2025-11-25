package me.mami.stratocraft.manager;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import me.mami.stratocraft.Main;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class StructureValidator {

    public boolean validate(Location centerBlock, String schematicName) {
        File file = new File(Main.getInstance().getDataFolder() + "/schematics/" + schematicName + ".schem");
        if (!file.exists()) {
            Main.getInstance().getLogger().warning("Şema bulunamadı: " + schematicName);
            return false;
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file))) {
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
}

