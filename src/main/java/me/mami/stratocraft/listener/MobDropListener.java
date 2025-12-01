package me.mami.stratocraft.listener;

import me.mami.stratocraft.manager.ItemManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Özel mob drop sistemi - Her seviye için özel mobların özel itemler düşürmesi
 */
public class MobDropListener implements Listener {
    private final Random random = new Random();
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.getCustomName() == null) {
            return;
        }
        
        String mobName = entity.getCustomName();
        Entity killer = entity.getKiller();
        
        // Normal drop'ları temizle (özel drop sistemi kullanacağız)
        // event.getDrops().clear(); // Yorum satırı - normal drop'ları da bırakabiliriz
        
        // ========== SEVİYE 1 MOBLAR (200-1000 blok) ==========
        
        if (mobName.contains("Yaban Domuzu")) {
            // %20 şansla Domuz Postu, her zaman Domuz Eti
            if (random.nextDouble() < 0.20 && ItemManager.WILD_BOAR_HIDE != null) {
                event.getDrops().add(ItemManager.WILD_BOAR_HIDE.clone());
            }
            if (ItemManager.WILD_BOAR_MEAT != null) {
                ItemStack meat = ItemManager.WILD_BOAR_MEAT.clone();
                meat.setAmount(1 + random.nextInt(2)); // 1-2 adet
                event.getDrops().add(meat);
            }
        }
        
        if (mobName.contains("Kurt Sürüsü")) {
            // %15 şansla Kurt Dişi, her zaman Kurt Postu
            if (random.nextDouble() < 0.15 && ItemManager.WOLF_FANG != null) {
                event.getDrops().add(ItemManager.WOLF_FANG.clone());
            }
            if (ItemManager.WOLF_PELT != null) {
                event.getDrops().add(ItemManager.WOLF_PELT.clone());
            }
        }
        
        if (mobName.contains("Yılan")) {
            // %25 şansla Yılan Zehri, her zaman Yılan Derisi
            if (random.nextDouble() < 0.25 && ItemManager.SNAKE_VENOM != null) {
                event.getDrops().add(ItemManager.SNAKE_VENOM.clone());
            }
            if (ItemManager.SNAKE_SKIN != null) {
                event.getDrops().add(ItemManager.SNAKE_SKIN.clone());
            }
        }
        
        if (mobName.contains("Kartal")) {
            // %20 şansla Kartal Tüyü, her zaman Kartal Pençesi
            if (random.nextDouble() < 0.20 && ItemManager.EAGLE_FEATHER != null) {
                event.getDrops().add(ItemManager.EAGLE_FEATHER.clone());
            }
            if (ItemManager.EAGLE_CLAW != null) {
                event.getDrops().add(ItemManager.EAGLE_CLAW.clone());
            }
        }
        
        if (mobName.contains("Ayı")) {
            // %15 şansla Ayı Pençesi, her zaman Ayı Postu
            if (random.nextDouble() < 0.15 && ItemManager.BEAR_CLAW != null) {
                event.getDrops().add(ItemManager.BEAR_CLAW.clone());
            }
            if (ItemManager.BEAR_PELT != null) {
                event.getDrops().add(ItemManager.BEAR_PELT.clone());
            }
        }
        
        // ========== SEVİYE 2 MOBLAR (1000-3000 blok) ==========
        
        if (mobName.contains("Demir Golem")) {
            // %30 şansla Demir Çekirdek, her zaman Demir Tozu
            if (random.nextDouble() < 0.30 && ItemManager.IRON_CORE != null) {
                event.getDrops().add(ItemManager.IRON_CORE.clone());
            }
            if (ItemManager.IRON_DUST != null) {
                ItemStack dust = ItemManager.IRON_DUST.clone();
                dust.setAmount(2 + random.nextInt(3)); // 2-4 adet
                event.getDrops().add(dust);
            }
        }
        
        if (mobName.contains("Buz Ejderi")) {
            // %25 şansla Buz Kalbi, her zaman Buz Kristali
            if (random.nextDouble() < 0.25 && ItemManager.ICE_HEART != null) {
                event.getDrops().add(ItemManager.ICE_HEART.clone());
            }
            if (ItemManager.ICE_CRYSTAL != null) {
                ItemStack crystal = ItemManager.ICE_CRYSTAL.clone();
                crystal.setAmount(1 + random.nextInt(2)); // 1-2 adet
                event.getDrops().add(crystal);
            }
        }
        
        if (mobName.contains("Ateş Yılanı")) {
            // %20 şansla Ateş Çekirdeği, her zaman Ateş Ölçeği
            if (random.nextDouble() < 0.20 && ItemManager.FIRE_CORE != null) {
                event.getDrops().add(ItemManager.FIRE_CORE.clone());
            }
            if (ItemManager.FIRE_SCALE != null) {
                ItemStack scale = ItemManager.FIRE_SCALE.clone();
                scale.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Toprak Dev")) {
            // %30 şansla Toprak Taşı, her zaman Toprak Tozu
            if (random.nextDouble() < 0.30 && ItemManager.EARTH_STONE != null) {
                event.getDrops().add(ItemManager.EARTH_STONE.clone());
            }
            if (ItemManager.EARTH_DUST != null) {
                ItemStack dust = ItemManager.EARTH_DUST.clone();
                dust.setAmount(3 + random.nextInt(2)); // 3-4 adet
                event.getDrops().add(dust);
            }
        }
        
        if (mobName.contains("Ruh Avcısı")) {
            // %25 şansla Ruh Parçası, her zaman Hayalet Tozu
            if (random.nextDouble() < 0.25 && ItemManager.SOUL_FRAGMENT != null) {
                event.getDrops().add(ItemManager.SOUL_FRAGMENT.clone());
            }
            if (ItemManager.GHOST_DUST != null) {
                ItemStack dust = ItemManager.GHOST_DUST.clone();
                dust.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(dust);
            }
        }
        
        // ========== SEVİYE 3 MOBLAR (3000-5000 blok) ==========
        
        if (mobName.contains("Gölge Ejderi")) {
            // %20 şansla Gölge Kalbi, her zaman Gölge Ölçeği
            if (random.nextDouble() < 0.20 && ItemManager.SHADOW_HEART != null) {
                event.getDrops().add(ItemManager.SHADOW_HEART.clone());
            }
            if (ItemManager.SHADOW_SCALE != null) {
                ItemStack scale = ItemManager.SHADOW_SCALE.clone();
                scale.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Işık Ejderi")) {
            // %20 şansla Işık Kalbi, her zaman Işık Tüyü
            if (random.nextDouble() < 0.20 && ItemManager.LIGHT_HEART != null) {
                event.getDrops().add(ItemManager.LIGHT_HEART.clone());
            }
            if (ItemManager.LIGHT_FEATHER != null) {
                ItemStack feather = ItemManager.LIGHT_FEATHER.clone();
                feather.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(feather);
            }
        }
        
        if (mobName.contains("Fırtına Dev")) {
            // %25 şansla Fırtına Çekirdeği, her zaman Fırtına Tozu
            if (random.nextDouble() < 0.25 && ItemManager.STORM_CORE != null) {
                event.getDrops().add(ItemManager.STORM_CORE.clone());
            }
            if (ItemManager.STORM_DUST != null) {
                ItemStack dust = ItemManager.STORM_DUST.clone();
                dust.setAmount(3 + random.nextInt(2)); // 3-4 adet
                event.getDrops().add(dust);
            }
        }
        
        if (mobName.contains("Lav Ejderi")) {
            // %20 şansla Lav Kalbi, her zaman Lav Ölçeği
            if (random.nextDouble() < 0.20 && ItemManager.LAVA_HEART != null) {
                event.getDrops().add(ItemManager.LAVA_HEART.clone());
            }
            if (ItemManager.LAVA_SCALE != null) {
                ItemStack scale = ItemManager.LAVA_SCALE.clone();
                scale.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Buz Dev")) {
            // %25 şansla Buz Çekirdeği, her zaman Buz Parçası
            if (random.nextDouble() < 0.25 && ItemManager.ICE_CORE != null) {
                event.getDrops().add(ItemManager.ICE_CORE.clone());
            }
            if (ItemManager.ICE_SHARD != null) {
                ItemStack shard = ItemManager.ICE_SHARD.clone();
                shard.setAmount(3 + random.nextInt(2)); // 3-4 adet
                event.getDrops().add(shard);
            }
        }
        
        // ========== SEVİYE 4 MOBLAR (5000+ blok) ==========
        
        if (mobName.contains("Kızıl Şeytan")) {
            // %15 şansla Şeytan Boynuzu, her zaman Şeytan Kanı
            if (random.nextDouble() < 0.15 && ItemManager.DEVIL_HORN != null) {
                event.getDrops().add(ItemManager.DEVIL_HORN.clone());
            }
            if (ItemManager.DEVIL_BLOOD != null) {
                ItemStack blood = ItemManager.DEVIL_BLOOD.clone();
                blood.setAmount(1 + random.nextInt(2)); // 1-2 adet
                event.getDrops().add(blood);
            }
        }
        
        if (mobName.contains("Kara Ejder")) {
            // %20 şansla Kara Ejder Kalbi, her zaman Kara Ejder Ölçeği
            if (random.nextDouble() < 0.20 && ItemManager.BLACK_DRAGON_HEART != null) {
                event.getDrops().add(ItemManager.BLACK_DRAGON_HEART.clone());
            }
            if (ItemManager.BLACK_DRAGON_SCALE != null) {
                ItemStack scale = ItemManager.BLACK_DRAGON_SCALE.clone();
                scale.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Ölüm Şövalyesi")) {
            // %25 şansla Ölüm Kılıcı Parçası, her zaman Ölüm Tozu
            if (random.nextDouble() < 0.25 && ItemManager.DEATH_SWORD_FRAGMENT != null) {
                event.getDrops().add(ItemManager.DEATH_SWORD_FRAGMENT.clone());
            }
            if (ItemManager.DEATH_DUST != null) {
                ItemStack dust = ItemManager.DEATH_DUST.clone();
                dust.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(dust);
            }
        }
        
        if (mobName.contains("Kaos Ejderi")) {
            // %20 şansla Kaos Çekirdeği, her zaman Kaos Ölçeği
            if (random.nextDouble() < 0.20 && ItemManager.CHAOS_CORE != null) {
                event.getDrops().add(ItemManager.CHAOS_CORE.clone());
            }
            if (ItemManager.CHAOS_SCALE != null) {
                ItemStack scale = ItemManager.CHAOS_SCALE.clone();
                scale.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Cehennem Şeytanı")) {
            // %15 şansla Cehennem Taşı, her zaman Cehennem Ateşi
            if (random.nextDouble() < 0.15 && ItemManager.HELL_STONE != null) {
                event.getDrops().add(ItemManager.HELL_STONE.clone());
            }
            if (ItemManager.HELL_FIRE != null) {
                ItemStack fire = ItemManager.HELL_FIRE.clone();
                fire.setAmount(1 + random.nextInt(2)); // 1-2 adet
                event.getDrops().add(fire);
            }
        }
        
        // ========== SEVİYE 5 MOBLAR (Efsanevi) ==========
        
        if (mobName.contains("Efsanevi Ejder")) {
            // %10 şansla Efsanevi Ejder Kalbi, her zaman Efsanevi Ejder Ölçeği
            if (random.nextDouble() < 0.10 && ItemManager.LEGENDARY_DRAGON_HEART != null) {
                event.getDrops().add(ItemManager.LEGENDARY_DRAGON_HEART.clone());
            }
            if (ItemManager.LEGENDARY_DRAGON_SCALE != null) {
                ItemStack scale = ItemManager.LEGENDARY_DRAGON_SCALE.clone();
                scale.setAmount(3 + random.nextInt(2)); // 3-4 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Tanrı Katili")) {
            // %12 şansla Tanrı Kanı, her zaman Tanrı Parçası
            if (random.nextDouble() < 0.12 && ItemManager.GOD_BLOOD != null) {
                event.getDrops().add(ItemManager.GOD_BLOOD.clone());
            }
            if (ItemManager.GOD_FRAGMENT != null) {
                ItemStack fragment = ItemManager.GOD_FRAGMENT.clone();
                fragment.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(fragment);
            }
        }
        
        if (mobName.contains("Hiçlik Yaratığı")) {
            // %10 şansla Hiçlik Çekirdeği, her zaman Hiçlik Tozu
            if (random.nextDouble() < 0.10 && ItemManager.VOID_CORE != null) {
                event.getDrops().add(ItemManager.VOID_CORE.clone());
            }
            if (ItemManager.VOID_DUST != null) {
                ItemStack dust = ItemManager.VOID_DUST.clone();
                dust.setAmount(3 + random.nextInt(2)); // 3-4 adet
                event.getDrops().add(dust);
            }
        }
        
        if (mobName.contains("Zaman Ejderi")) {
            // %15 şansla Zaman Çekirdeği, her zaman Zaman Ölçeği
            if (random.nextDouble() < 0.15 && ItemManager.TIME_CORE != null) {
                event.getDrops().add(ItemManager.TIME_CORE.clone());
            }
            if (ItemManager.TIME_SCALE != null) {
                ItemStack scale = ItemManager.TIME_SCALE.clone();
                scale.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(scale);
            }
        }
        
        if (mobName.contains("Kader Yaratığı")) {
            // %12 şansla Kader Taşı, her zaman Kader Parçası
            if (random.nextDouble() < 0.12 && ItemManager.FATE_STONE != null) {
                event.getDrops().add(ItemManager.FATE_STONE.clone());
            }
            if (ItemManager.FATE_FRAGMENT != null) {
                ItemStack fragment = ItemManager.FATE_FRAGMENT.clone();
                fragment.setAmount(2 + random.nextInt(2)); // 2-3 adet
                event.getDrops().add(fragment);
            }
        }
    }
}

