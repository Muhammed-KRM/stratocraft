package me.mami.stratocraft.manager;

import me.mami.stratocraft.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Silah mod değiştirme menüsünü yöneten sınıf
 * Shift + Sağ Tık ile GUI açılır
 */
public class WeaponModeManager implements Listener {

    private final Main plugin;
    private final NamespacedKey modeKey;
    private final NamespacedKey weaponIdKey;

    public WeaponModeManager(Main plugin) {
        this.plugin = plugin;
        this.modeKey = new NamespacedKey(plugin, "weapon_mode");
        this.weaponIdKey = new NamespacedKey(plugin, "special_item_id");
    }

    /**
     * Oyuncu Shift + Sağ Tık yaptığında Menüyü Aç
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onWeaponInteract(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT")) return;
        if (!event.getPlayer().isSneaking()) return;

        Player player = event.getPlayer();
        // Önce event.getItem() kontrol et, yoksa elindeki item'ı al
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            item = player.getInventory().getItemInMainHand();
        }
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(weaponIdKey, PersistentDataType.STRING)) return;

        // Sadece Tier 4 ve Tier 5 silahlar için menü aç (ID kontrolü)
        String weaponId = meta.getPersistentDataContainer().get(weaponIdKey, PersistentDataType.STRING);
        if (weaponId != null && (weaponId.startsWith("l4_") || weaponId.startsWith("l5_"))) {
            event.setCancelled(true); // Bloğu koymayı veya kalkanı engelle
            openModeMenu(player, item, weaponId);
        }
    }

    /**
     * Mod seçim menüsünü aç
     */
    private void openModeMenu(Player player, ItemStack weapon, String weaponId) {
        Inventory gui = Bukkit.createInventory(null, 9, 
            Component.text("§5§lMod Seçimi").color(NamedTextColor.DARK_PURPLE));

        // Mevcut modu al
        int currentMode = getWeaponMode(weapon);

        // Silaha göre mod ikonlarını hazırla
        if (weaponId.equals("l5_1_void_walker")) {
            gui.setItem(2, createModeIcon(Material.ENDER_PEARL, "Mod 1: Işınlanma", "Sağ tıkla ışınlan ve patlat.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.OBSIDIAN, "Mod 2: Kara Delik", "Gelen hasarı emen kalkan.", 2, currentMode == 2));
        } 
        else if (weaponId.equals("l5_2_meteor_caller")) {
            gui.setItem(2, createModeIcon(Material.FIRE_CHARGE, "Mod 1: Kıyamet", "Gökten meteor yağdır.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.LAVA_BUCKET, "Mod 2: Yer Yaran", "Yeri yarıp lav çıkart.", 2, currentMode == 2));
        }
        else if (weaponId.equals("l5_5_time_keeper")) {
            gui.setItem(2, createModeIcon(Material.CLOCK, "Mod 1: Zamanı Durdur", "Tüm mobları dondur.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.COMPASS, "Mod 2: Geri Sar", "Canını ve yerini geri sar.", 2, currentMode == 2));
        }
        // TIER 4 SİLAHLAR
        else if (weaponId.equals("l4_1_elementalist")) {
            gui.setItem(2, createModeIcon(Material.FIRE_CHARGE, "Mod 1: Ateş", "Önündeki alana alev atar.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.ICE, "Mod 2: Buz", "Etrafındaki herkesi dondur.", 2, currentMode == 2));
        }
        else if (weaponId.equals("l4_2_life_death")) {
            gui.setItem(2, createModeIcon(Material.WITHER_SKELETON_SKULL, "Mod 1: Ölüm", "Wither kafası fırlat.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.GOLDEN_APPLE, "Mod 2: Yaşam", "Canını yenile.", 2, currentMode == 2));
        }
        else if (weaponId.equals("l4_3_mjolnir_v2")) {
            gui.setItem(2, createModeIcon(Material.LIGHTNING_ROD, "Mod 1: Şimşek", "Zincirleme şimşek çarpar.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.IRON_AXE, "Mod 2: Fırlat", "Çekici fırlat ve geri çağır.", 2, currentMode == 2));
        }
        else if (weaponId.equals("l4_4_ranger_pride")) {
            gui.setItem(2, createModeIcon(Material.SPYGLASS, "Mod 1: Sniper", "Hedefe lazer at.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.CROSSBOW, "Mod 2: Pompalı", "Çoklu ok at.", 2, currentMode == 2));
        }
        else if (weaponId.equals("l4_5_magnetic_glove")) {
            gui.setItem(2, createModeIcon(Material.MAGMA_CREAM, "Mod 1: Çek", "Hedefi kendine çek.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.SLIME_BALL, "Mod 2: İt", "Hedefi uzağa it.", 2, currentMode == 2));
        }
        // TIER 5 SİLAHLAR (Eksik olanlar)
        else if (weaponId.equals("l5_3_titan_slayer")) {
            gui.setItem(2, createModeIcon(Material.TRIDENT, "Mod 1: %5 Hasar", "Mevcut canın %5'i hasar.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.ARROW, "Mod 2: Mızrak Yağmuru", "Gökten mızrak yağdır.", 2, currentMode == 2));
        }
        else if (weaponId.equals("l5_4_soul_reaper")) {
            gui.setItem(2, createModeIcon(Material.ZOMBIE_HEAD, "Mod 1: Hortlak Çağır", "Zombi çağır.", 1, currentMode == 1));
            gui.setItem(6, createModeIcon(Material.TNT, "Mod 2: Ruh Patlaması", "Yakındaki zombileri patlat.", 2, currentMode == 2));
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    /**
     * Mod ikonu oluştur
     */
    private ItemStack createModeIcon(Material mat, String name, String desc, int modeId, boolean isSelected) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        // Seçili mod için özel işaretleme
        String displayName = isSelected ? "§a§l✓ " + name : name;
        meta.displayName(Component.text(displayName));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(desc));
        if (isSelected) {
            lore.add(Component.text("§a§l► ŞU AN AKTİF"));
        }
        meta.lore(lore);
        
        // Mod ID'sini ikona kaydet ki tıklayınca bilelim
        meta.getPersistentDataContainer().set(modeKey, PersistentDataType.INTEGER, modeId);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Menü tıklama olayı
     */
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().title() == null || 
            !event.getView().title().toString().contains("Mod Seçimi")) return;
        event.setCancelled(true); // Eşyayı alamasın

        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack icon = event.getCurrentItem();
        
        if (!icon.getItemMeta().getPersistentDataContainer().has(modeKey, PersistentDataType.INTEGER)) return;

        int selectedMode = icon.getItemMeta().getPersistentDataContainer().get(modeKey, PersistentDataType.INTEGER);
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Silahın modunu değiştir
        ItemMeta weaponMeta = weapon.getItemMeta();
        weaponMeta.getPersistentDataContainer().set(modeKey, PersistentDataType.INTEGER, selectedMode);
        
        // Silahın ismini güncelle (Görsel geri bildirim)
        String oldName = weaponMeta.displayName() != null ?  
            ((net.kyori.adventure.text.TextComponent)weaponMeta.displayName()).content() : "Silah";
        
        // İsimde parantez varsa temizle (Örn: "Kılıç (Mod: 1)")
        if (oldName.contains(" (Mod:")) {
            oldName = oldName.substring(0, oldName.indexOf(" (Mod:"));
        }
        
        weaponMeta.displayName(Component.text(oldName + " (Mod: " + selectedMode + ")").color(NamedTextColor.RED));
        weapon.setItemMeta(weaponMeta);

        player.sendMessage(Component.text("Silah Modu Değiştirildi: " + selectedMode).color(NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        player.closeInventory();
    }
    
    /**
     * Bir silahın o anki modunu öğrenmek için yardımcı metod
     */
    private int getWeaponMode(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 1;
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(modeKey, PersistentDataType.INTEGER, 1);
    }
    
    /**
     * Statik metod - dışarıdan çağrılabilir
     */
    public static int getWeaponMode(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return 1;
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "weapon_mode");
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 1);
    }
}

