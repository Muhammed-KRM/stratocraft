package me.mami.stratocraft.world;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

/**
 * Dünyada özel madenlerin (Titanyum, Kızıl Elmas) fiziksel olarak oluşmasını sağlar.
 * Bu populator, chunk generation sırasında çalışır.
 */
public class OrePopulator extends BlockPopulator {
    
    @Override
    public void populate(World world, Random random, org.bukkit.Chunk chunk) {
        // Her chunk'ta %30 şansla özel maden oluştur
        if (random.nextDouble() > 0.3) return;
        
        // Chunk içinde rastgele 1-3 maden oluştur
        int oreCount = random.nextInt(3) + 1;
        
        for (int i = 0; i < oreCount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            
            // Derinlik kontrolü: -64 ile -20 arası
            int y = random.nextInt(44) - 64; // -64 ile -20 arası
            
            Block block = chunk.getBlock(x, y, z);
            
            // Sadece taş veya deepslate üzerine yerleştir
            if (block.getType() != Material.STONE && 
                block.getType() != Material.DEEPSLATE &&
                block.getType() != Material.DEEPSLATE_COAL_ORE &&
                block.getType() != Material.DEEPSLATE_IRON_ORE &&
                block.getType() != Material.DEEPSLATE_DIAMOND_ORE) {
                continue;
            }
            
            // Titanyum Cevheri (%70 şans) veya Kızıl Elmas (%30 şans)
            if (random.nextDouble() < 0.7) {
                // Titanyum Cevheri - Daha yaygın
                if (ItemManager.TITANIUM_ORE != null) {
                    // Custom item'ı blok olarak yerleştiremeyiz, bu yüzden özel bir blok kullanmalıyız
                    // Alternatif: ANCIENT_DEBRIS kullan ve kırıldığında Titanyum düşür
                    // NOT: Normal bloklara NBT eklenemez, bu yüzden sadece Material kontrolü yapılacak
                    block.setType(Material.ANCIENT_DEBRIS);
                }
            } else {
                // Kızıl Elmas - Çok nadir, sadece -60 ve altında
                if (y <= -60 && ItemManager.RED_DIAMOND != null) {
                    block.setType(Material.DEEPSLATE_DIAMOND_ORE);
                    // NOT: Normal bloklara NBT eklenemez, bu yüzden sadece Material ve konum kontrolü yapılacak
                }
            }
        }
    }
}

