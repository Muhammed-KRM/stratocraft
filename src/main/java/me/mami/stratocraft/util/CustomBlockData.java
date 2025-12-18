package me.mami.stratocraft.util;

import me.mami.stratocraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Özel Blok Veri Yönetimi
 * 
 * PersistentDataContainer kullanarak özel blok verilerini kalıcı şekilde tutar.
 * Server restart sonrası veriler korunur.
 */
public class CustomBlockData {
    private static Main plugin;
    
    // NamespacedKey'ler
    private static final NamespacedKey CLAN_FENCE_KEY = new NamespacedKey("stratocraft", "clan_fence");
    private static final NamespacedKey CLAN_CRYSTAL_KEY = new NamespacedKey("stratocraft", "clan_crystal");
    private static final NamespacedKey STRUCTURE_CORE_KEY = new NamespacedKey("stratocraft", "structure_core");
    private static final NamespacedKey STRUCTURE_CORE_OWNER_KEY = new NamespacedKey("stratocraft", "structure_core_owner");
    private static final NamespacedKey TRAP_CORE_KEY = new NamespacedKey("stratocraft", "trap_core");
    private static final NamespacedKey TRAP_CORE_OWNER_KEY = new NamespacedKey("stratocraft", "trap_core_owner");
    private static final NamespacedKey CLAN_BANK_KEY = new NamespacedKey("stratocraft", "clan_bank");
    
    /**
     * Plugin instance'ı set et (Main.java'da çağrılmalı)
     */
    public static void initialize(Main mainPlugin) {
        plugin = mainPlugin;
    }
    
    // ========== KLAN ÇİTİ METODLARI ==========
    
    /**
     * Klan çiti verisini kaydet
     * 
     * @param block Çit bloğu
     * @param clanId Klan ID'si (null olabilir)
     * @return Başarılıysa true
     */
    public static boolean setClanFenceData(Block block, UUID clanId) {
        if (block == null) return false;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (clanId != null) {
                container.set(CLAN_FENCE_KEY, PersistentDataType.STRING, clanId.toString());
            } else {
                container.remove(CLAN_FENCE_KEY);
            }
            
            state.update(); // ✅ KRİTİK: BlockState güncellemesi gerekli!
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan çiti verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Klan çiti verisini oku
     * 
     * @param block Çit bloğu
     * @return Klan ID'si (yoksa null)
     */
    public static UUID getClanFenceData(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (container.has(CLAN_FENCE_KEY, PersistentDataType.STRING)) {
                String clanIdStr = container.get(CLAN_FENCE_KEY, PersistentDataType.STRING);
                return UUID.fromString(clanIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan çiti verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Klan çiti mi kontrol et
     * 
     * @param block Çit bloğu
     * @return Klan çiti ise true
     */
    public static boolean isClanFence(Block block) {
        if (block == null || block.getType() != org.bukkit.Material.OAK_FENCE) {
            return false;
        }
        
        return getClanFenceData(block) != null;
    }
    
    /**
     * Klan çiti verisini temizle
     * 
     * @param block Çit bloğu
     */
    public static void removeClanFenceData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            container.remove(CLAN_FENCE_KEY);
            state.update();
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan çiti verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== KLAN KRİSTALİ METODLARI ==========
    
    /**
     * Klan kristali verisini kaydet
     * 
     * @param block Kristal bloğu
     * @param clanId Klan ID'si (null olabilir)
     * @return Başarılıysa true
     */
    public static boolean setClanCrystalData(Block block, UUID clanId) {
        if (block == null) return false;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (clanId != null) {
                container.set(CLAN_CRYSTAL_KEY, PersistentDataType.STRING, clanId.toString());
            } else {
                container.remove(CLAN_CRYSTAL_KEY);
            }
            
            state.update();
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan kristali verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Klan kristali verisini oku
     * 
     * @param block Kristal bloğu
     * @return Klan ID'si (yoksa null)
     */
    public static UUID getClanCrystalData(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (container.has(CLAN_CRYSTAL_KEY, PersistentDataType.STRING)) {
                String clanIdStr = container.get(CLAN_CRYSTAL_KEY, PersistentDataType.STRING);
                return UUID.fromString(clanIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan kristali verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Klan kristali mi kontrol et
     * 
     * @param block Kristal bloğu
     * @return Klan kristali ise true
     */
    public static boolean isClanCrystal(Block block) {
        if (block == null) return false;
        
        return getClanCrystalData(block) != null;
    }
    
    /**
     * Klan kristali verisini temizle
     * 
     * @param block Kristal bloğu
     */
    public static void removeClanCrystalData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            container.remove(CLAN_CRYSTAL_KEY);
            state.update();
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan kristali verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== YAPI ÇEKİRDEĞİ METODLARI ==========
    
    /**
     * Yapı çekirdeği verisini kaydet
     */
    public static boolean setStructureCoreData(Block block, UUID ownerId) {
        if (block == null) return false;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (ownerId != null) {
                container.set(STRUCTURE_CORE_KEY, PersistentDataType.BYTE, (byte) 1);
                container.set(STRUCTURE_CORE_OWNER_KEY, PersistentDataType.STRING, ownerId.toString());
            } else {
                container.remove(STRUCTURE_CORE_KEY);
                container.remove(STRUCTURE_CORE_OWNER_KEY);
            }
            
            state.update();
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Yapı çekirdeği verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Yapı çekirdeği verisini oku
     */
    public static UUID getStructureCoreOwner(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (container.has(STRUCTURE_CORE_OWNER_KEY, PersistentDataType.STRING)) {
                String ownerIdStr = container.get(STRUCTURE_CORE_OWNER_KEY, PersistentDataType.STRING);
                return UUID.fromString(ownerIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Yapı çekirdeği verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Yapı çekirdeği mi kontrol et
     */
    public static boolean isStructureCore(Block block) {
        if (block == null || block.getType() != org.bukkit.Material.OAK_LOG) {
            return false;
        }
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            return container.has(STRUCTURE_CORE_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Yapı çekirdeği verisini temizle
     */
    public static void removeStructureCoreData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            container.remove(STRUCTURE_CORE_KEY);
            container.remove(STRUCTURE_CORE_OWNER_KEY);
            state.update();
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Yapı çekirdeği verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== TUZAK ÇEKİRDEĞİ METODLARI ==========
    
    /**
     * Tuzak çekirdeği verisini kaydet
     */
    public static boolean setTrapCoreData(Block block, UUID ownerId) {
        if (block == null) return false;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (ownerId != null) {
                container.set(TRAP_CORE_KEY, PersistentDataType.BYTE, (byte) 1);
                container.set(TRAP_CORE_OWNER_KEY, PersistentDataType.STRING, ownerId.toString());
            } else {
                container.remove(TRAP_CORE_KEY);
                container.remove(TRAP_CORE_OWNER_KEY);
            }
            
            state.update();
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Tuzak çekirdeği verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Tuzak çekirdeği verisini oku
     */
    public static UUID getTrapCoreOwner(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (container.has(TRAP_CORE_OWNER_KEY, PersistentDataType.STRING)) {
                String ownerIdStr = container.get(TRAP_CORE_OWNER_KEY, PersistentDataType.STRING);
                return UUID.fromString(ownerIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Tuzak çekirdeği verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Tuzak çekirdeği mi kontrol et
     */
    public static boolean isTrapCore(Block block) {
        if (block == null || block.getType() != org.bukkit.Material.LODESTONE) {
            return false;
        }
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            return container.has(TRAP_CORE_KEY, PersistentDataType.BYTE);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Tuzak çekirdeği verisini temizle
     */
    public static void removeTrapCoreData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            container.remove(TRAP_CORE_KEY);
            container.remove(TRAP_CORE_OWNER_KEY);
            state.update();
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Tuzak çekirdeği verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
    
    // ========== KLAN BANKASI METODLARI ==========
    
    /**
     * Klan bankası verisini kaydet
     */
    public static boolean setClanBankData(Block block, UUID clanId) {
        if (block == null) return false;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (clanId != null) {
                container.set(CLAN_BANK_KEY, PersistentDataType.STRING, clanId.toString());
            } else {
                container.remove(CLAN_BANK_KEY);
            }
            
            state.update();
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan bankası verisi kaydedilemedi: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Klan bankası verisini oku
     */
    public static UUID getClanBankData(Block block) {
        if (block == null) return null;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            
            if (container.has(CLAN_BANK_KEY, PersistentDataType.STRING)) {
                String clanIdStr = container.get(CLAN_BANK_KEY, PersistentDataType.STRING);
                return UUID.fromString(clanIdStr);
            }
            
            return null;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan bankası verisi okunamadı: " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Klan bankası mı kontrol et
     */
    public static boolean isClanBank(Block block) {
        if (block == null) return false;
        
        return getClanBankData(block) != null;
    }
    
    /**
     * Klan bankası verisini temizle
     */
    public static void removeClanBankData(Block block) {
        if (block == null) return;
        
        try {
            BlockState state = block.getState();
            PersistentDataContainer container = state.getPersistentDataContainer();
            container.remove(CLAN_BANK_KEY);
            state.update();
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Klan bankası verisi temizlenemedi: " + e.getMessage());
            }
        }
    }
}

