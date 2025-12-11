package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.TamingManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.util.TamingHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Eğitme GUI Menüsü
 * 
 * Özellikler:
 * - Eğitilmiş canlıları listeleme
 * - Canlı detayları
 * - Canlı yönetimi (takip, binme)
 */
public class TamingMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final TamingManager tamingManager;
    
    // Açık detay menüleri (player -> entity)
    private final java.util.Map<UUID, LivingEntity> openDetailMenus = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Kişisel mod takibi (player -> personalOnly)
    private final java.util.Map<UUID, Boolean> personalMode = new java.util.concurrent.ConcurrentHashMap<>();
    
    public TamingMenu(Main plugin, ClanManager clanManager, TamingManager tamingManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.tamingManager = tamingManager;
    }
    
    /**
     * Ana eğitme menüsünü aç
     */
    public void openMainMenu(Player player) {
        openMainMenu(player, false);
    }
    
    /**
     * Ana eğitme menüsünü aç (kişisel veya klan modu)
     */
    public void openMainMenu(Player player, boolean personalOnly) {
        if (player == null) return;
        
        // Manager null kontrolleri
        if (tamingManager == null) {
            player.sendMessage("§cEğitme sistemi aktif değil!");
            plugin.getLogger().warning("TamingManager null! Menü açılamıyor.");
            return;
        }
        
        if (!personalOnly && clanManager == null) {
            player.sendMessage("§cKlan sistemi aktif değil!");
            plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
            return;
        }
        
        // Kişisel mod bilgisini sakla
        personalMode.put(player.getUniqueId(), personalOnly);
        
        // Oyuncunun eğitilmiş canlılarını getir
        List<LivingEntity> tamedCreatures = TamingHelper.getTamedCreatures(player, tamingManager);
        
        // Kişisel modda klan canlılarını gösterme
        if (!personalOnly) {
            // Klan üyesiyse klan canlılarını da göster
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            boolean showClanCreatures = false;
            if (clan != null) {
                List<LivingEntity> clanCreatures = TamingHelper.getClanTamedCreatures(clan, tamingManager);
                if (!clanCreatures.isEmpty()) {
                    showClanCreatures = true;
                    // Klan canlılarını da ekle (tekrar yok)
                    for (LivingEntity creature : clanCreatures) {
                        if (!tamedCreatures.contains(creature)) {
                            tamedCreatures.add(creature);
                        }
                    }
                }
            }
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Eğitilmiş Canlılar");
        
        // Canlıları listele (45 slot - 0-44)
        int slot = 0;
        for (LivingEntity creature : tamedCreatures) {
            if (creature == null || !creature.isValid() || creature.isDead() || slot >= 45) break;
            
            String name = creature.getCustomName();
            if (name == null) {
                name = creature.getType().name();
            }
            
            TamingManager.Gender gender = tamingManager.getGender(creature);
            String genderStr = gender == TamingManager.Gender.MALE ? "§b♂" : "§d♀";
            boolean isRideable = tamingManager.isRideable(creature);
            
            UUID ownerId = tamingManager.getOwner(creature);
            boolean isOwned = ownerId != null && ownerId.equals(player.getUniqueId());
            
            Material icon = TamingHelper.getCreatureIcon(creature);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Cinsiyet: " + genderStr);
            lore.add("§7Sahip: §e" + (isOwned ? "Sen" : "Klan Üyesi"));
            lore.add("§7Binilebilir: " + (isRideable ? "§aEvet" : "§cHayır"));
            lore.add("§7Sağlık: §e" + String.format("%.1f", creature.getHealth()) + 
                     "§7/§e" + String.format("%.1f", creature.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Detayları gör");
            if (isOwned) {
                lore.add("§eSağ Tık: §7Yönet");
            }
            
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot++, item);
        }
        
        // Bilgi butonu
        menu.setItem(45, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Canlı: §e" + tamedCreatures.size(),
                "§7Eğitilmiş canlılarınızı yönetin")));
        
        // Üreme menüsü butonu
        menu.setItem(49, createButton(Material.GOLDEN_APPLE, "§a§lÜREME MENÜSÜ", 
            Arrays.asList("§7Üreme çiftlerini yönet")));
        
        // Geri butonu - kişisel modda Personal Terminal'e dön
        String backText = personalOnly ? "§7Kişisel Terminal'e dön" : "§7Ana klan menüsüne dön";
        menu.setItem(53, createButton(Material.ARROW, "§7Geri", Arrays.asList(backText)));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Canlı detay menüsünü aç
     */
    public void openCreatureDetailMenu(Player player, LivingEntity creature) {
        if (player == null || creature == null || !creature.isValid() || creature.isDead()) return;
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Canlı Detayları");
        
        String name = creature.getCustomName();
        if (name == null) {
            name = creature.getType().name();
        }
        
        List<String> info = TamingHelper.getCreatureInfo(creature, tamingManager);
        
        Material icon = TamingHelper.getCreatureIcon(creature);
        
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l" + name);
            meta.setLore(info);
            item.setItemMeta(meta);
        }
        menu.setItem(13, item);
        
        UUID ownerId = tamingManager.getOwner(creature);
        boolean isOwned = ownerId != null && ownerId.equals(player.getUniqueId());
        
        // Yönetim butonları (sadece sahip için)
        if (isOwned) {
            // Işınlanma butonu
            menu.setItem(31, createButton(Material.ENDER_PEARL, "§eIşınlan", 
                Arrays.asList("§7Canlının konumuna ışınlan")));
            
            // Binme butonu (binilebilirse)
            if (tamingManager.isRideable(creature)) {
                menu.setItem(40, createButton(Material.SADDLE, "§a§lBİN", 
                    Arrays.asList("§7Canlıya bin")));
            }
        }
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Canlı listesine dön")));
        
        openDetailMenus.put(player.getUniqueId(), creature);
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Eğitilmiş Canlılar")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6Canlı Detayları")) {
            handleDetailMenuClick(event);
        }
    }
    
    private void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 53) {
            // Geri butonu - kişisel modda Personal Terminal'e dön
            Boolean isPersonal = personalMode.getOrDefault(player.getUniqueId(), false);
            if (isPersonal) {
                // Personal Terminal'e dön
                if (plugin.getPersonalTerminalListener() != null) {
                    plugin.getPersonalTerminalListener().openMainMenu(player);
                } else {
                    player.closeInventory();
                    player.sendMessage("§eKişisel Terminal'e dönmek için terminal item'ına sağ tık yapın.");
                }
            } else {
                // Klan menüsüne dön
                if (plugin.getClanMenu() != null) {
                    plugin.getClanMenu().openMenu(player);
                }
            }
            return;
        }
        
        if (slot == 49 && clicked.getType() == Material.GOLDEN_APPLE) {
            // Üreme menüsü
            if (plugin.getBreedingMenu() != null) {
                plugin.getBreedingMenu().openMainMenu(player);
            }
            return;
        }
        
        if (slot < 45) {
            // Canlı seçildi
            List<LivingEntity> tamedCreatures = TamingHelper.getTamedCreatures(player, tamingManager);
            if (slot < tamedCreatures.size()) {
                LivingEntity creature = tamedCreatures.get(slot);
                if (creature != null && creature.isValid()) {
                    if (event.isLeftClick()) {
                        openCreatureDetailMenu(player, creature);
                    } else if (event.isRightClick()) {
                        UUID ownerId = tamingManager.getOwner(creature);
                        if (ownerId != null && ownerId.equals(player.getUniqueId())) {
                            openCreatureManagementMenu(player, creature);
                        }
                    }
                }
            }
        }
    }
    
    private void handleDetailMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        LivingEntity creature = openDetailMenus.get(player.getUniqueId());
        if (creature == null || !creature.isValid() || creature.isDead()) {
            openMainMenu(player);
            return;
        }
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
            return;
        }
        
        if (slot == 31 && clicked.getType() == Material.ENDER_PEARL) {
            // Işınlanma
            org.bukkit.Location loc = creature.getLocation();
            if (loc == null || loc.getWorld() == null) {
                player.sendMessage("§cCanlının konumu geçersiz!");
                return;
            }
            
            // World null check
            if (loc.getWorld() == null) {
                player.sendMessage("§cCanlının dünyası geçersiz!");
                return;
            }
            
            player.teleport(loc.add(0.5, 1, 0.5));
            player.sendMessage("§aCanlının konumuna ışınlandınız!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        } else if (slot == 40 && clicked.getType() == Material.SADDLE) {
            // Binme
            if (tamingManager.isRideable(creature)) {
                creature.addPassenger(player);
                player.sendMessage("§aCanlıya bindiniz!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
            } else {
                player.sendMessage("§cBu canlıya binilemez!");
            }
        }
    }
    
    /**
     * Canlı yönetim menüsünü aç
     */
    private void openCreatureManagementMenu(Player player, LivingEntity creature) {
        if (player == null || creature == null || !creature.isValid()) return;
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Canlı Yönetimi");
        
        String name = creature.getCustomName();
        if (name == null) {
            name = creature.getType().name();
        }
        
        // Canlı bilgisi
        menu.setItem(13, createButton(TamingHelper.getCreatureIcon(creature), "§e" + name, 
            TamingHelper.getCreatureInfo(creature, tamingManager)));
        
        // Takip modu butonu
        UUID followingTarget = getFollowingTarget(creature);
        boolean isFollowing = followingTarget != null;
        
        menu.setItem(11, createButton(Material.LEAD, isFollowing ? "§aTakip Ediyor" : "§7Takip Etmiyor", 
            Arrays.asList("§7Canlının takip durumunu değiştir",
                "§7Şu an: " + (isFollowing ? "§aTakip ediyor" : "§cTakip etmiyor"))));
        
        // Binme butonu (binilebilirse)
        if (tamingManager.isRideable(creature)) {
            menu.setItem(15, createButton(Material.SADDLE, "§a§lBİN", 
                Arrays.asList("§7Canlıya bin")));
        }
        
        // Geri butonu
        menu.setItem(18, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Canlı listesine dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Canlının takip ettiği hedefi getir
     */
    private UUID getFollowingTarget(LivingEntity creature) {
        if (creature == null || !creature.isValid()) return null;
        
        try {
            // Reflection ile followingTargets map'inden al
            java.lang.reflect.Field targetsField = TamingManager.class.getDeclaredField("followingTargets");
            targetsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, UUID> targets = (java.util.Map<UUID, UUID>) targetsField.get(tamingManager);
            if (targets != null) {
                return targets.get(creature.getUniqueId());
            }
        } catch (Exception e) {
            // Hata durumunda metadata'dan al
            if (creature.hasMetadata("FollowingTarget")) {
                return UUID.fromString(creature.getMetadata("FollowingTarget").get(0).asString());
            }
        }
        
        return null;
    }
    
    /**
     * Buton oluştur
     */
    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

