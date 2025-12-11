package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.BreedingManager;
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
 * Üreme GUI Menüsü
 * 
 * Özellikler:
 * - Aktif üreme çiftlerini listeleme
 * - Üreme çifti oluşturma
 * - Üreme tesisleri yönetimi
 */
public class BreedingMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final TamingManager tamingManager;
    private final BreedingManager breedingManager;
    
    // Açık menüler (player -> entity pair)
    private final java.util.Map<UUID, LivingEntity[]> openPairMenus = new java.util.concurrent.ConcurrentHashMap<>();
    
    public BreedingMenu(Main plugin, ClanManager clanManager, TamingManager tamingManager,
                       BreedingManager breedingManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.tamingManager = tamingManager;
        this.breedingManager = breedingManager;
    }
    
    /**
     * Ana üreme menüsünü aç
     */
    public void openMainMenu(Player player) {
        if (player == null) return;
        
        // Kişisel menü - klan kontrolü yok, sadece oyuncunun çiftlerini göster
        // Aktif üreme çiftlerini bul (sadece oyuncunun)
        List<BreedingPair> activePairs = getActiveBreedingPairs(player, null);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Üreme Yönetimi");
        
        // Üreme çiftlerini listele (45 slot - 0-44)
        int slot = 0;
        for (BreedingPair pair : activePairs) {
            if (pair == null || slot >= 45) break;
            
            String femaleName = pair.female.getCustomName();
            if (femaleName == null) {
                femaleName = pair.female.getType().name();
            }
            
            String maleName = pair.male.getCustomName();
            if (maleName == null) {
                maleName = pair.male.getType().name();
            }
            
            String remainingTime = formatTime(pair.remainingTime);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Dişi: §e" + femaleName);
            lore.add("§7Erkek: §e" + maleName);
            lore.add("§7Kalan Süre: §e" + remainingTime);
            lore.add("§7Durum: " + (pair.isActive ? "§aAktif" : "§cPasif"));
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Detayları gör");
            
            ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§eÜreme Çifti");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            menu.setItem(slot++, item);
        }
        
        // Yeni çift oluştur butonu
        menu.setItem(49, createButton(Material.EMERALD, "§a§lYENİ ÇİFT OLUŞTUR", 
            Arrays.asList("§7Eğitilmiş canlılardan",
                "§7üreme çifti oluştur")));
        
        // Bilgi butonu
        menu.setItem(45, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Aktif Çift: §e" + activePairs.size(),
                "§7Üreme çiftleri yönetimi")));
        
        // Geri butonu
        menu.setItem(53, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Eğitme menüsüne dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Üreme çifti detay menüsünü aç
     */
    public void openBreedingPairMenu(Player player, LivingEntity female, LivingEntity male) {
        if (player == null || female == null || male == null) return;
        
        if (!female.isValid() || female.isDead() || !male.isValid() || male.isDead()) {
            player.sendMessage("§cCanlılar artık geçerli değil!");
            openMainMenu(player);
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Üreme Çifti Detayları");
        
        String femaleName = female.getCustomName();
        if (femaleName == null) {
            femaleName = female.getType().name();
        }
        
        String maleName = male.getCustomName();
        if (maleName == null) {
            maleName = male.getType().name();
        }
        
        // Dişi canlı
        List<String> femaleLore = TamingHelper.getCreatureInfo(female, tamingManager);
        menu.setItem(20, createButton(TamingHelper.getCreatureIcon(female), "§d§lDİŞİ", femaleLore));
        
        // Erkek canlı
        List<String> maleLore = TamingHelper.getCreatureInfo(male, tamingManager);
        menu.setItem(24, createButton(TamingHelper.getCreatureIcon(male), "§b§lERKEK", maleLore));
        
        // Üreme durumu
        BreedingPair pair = findBreedingPair(female, male);
        if (pair != null) {
            String remainingTime = formatTime(pair.remainingTime);
            menu.setItem(13, createButton(Material.GOLDEN_APPLE, "§e§lÜREME AKTİF", 
                Arrays.asList("§7Kalan Süre: §e" + remainingTime,
                    "§7Durum: " + (pair.isActive ? "§aAktif" : "§cPasif"))));
        } else {
            menu.setItem(13, createButton(Material.BARRIER, "§cPasif", 
                Arrays.asList("§7Bu çift aktif değil")));
        }
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Üreme listesine dön")));
        
        openPairMenus.put(player.getUniqueId(), new LivingEntity[]{female, male});
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Üreme çifti oluşturma menüsünü aç
     */
    public void openCreatePairMenu(Player player) {
        if (player == null) return;
        
        // Oyuncunun eğitilmiş canlılarını getir
        List<LivingEntity> tamedCreatures = TamingHelper.getTamedCreatures(player, tamingManager);
        
        // Dişi ve erkek canlıları ayır
        List<LivingEntity> females = new ArrayList<>();
        List<LivingEntity> males = new ArrayList<>();
        
        for (LivingEntity creature : tamedCreatures) {
            if (creature == null || !creature.isValid() || creature.isDead()) continue;
            
            TamingManager.Gender gender = tamingManager.getGender(creature);
            if (gender == TamingManager.Gender.FEMALE) {
                females.add(creature);
            } else if (gender == TamingManager.Gender.MALE) {
                males.add(creature);
            }
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Üreme Çifti Oluştur");
        
        // Dişi canlıları listele (sol taraf - 0-26)
        int slot = 0;
        for (LivingEntity female : females) {
            if (female == null || !female.isValid() || slot >= 27) break;
            
            String name = female.getCustomName();
            if (name == null) {
                name = female.getType().name();
            }
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Cinsiyet: §dDişi");
            lore.add("§7Sağlık: §e" + String.format("%.1f", female.getHealth()));
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Seç");
            
            menu.setItem(slot++, createButton(TamingHelper.getCreatureIcon(female), "§d" + name, lore));
        }
        
        // Erkek canlıları listele (sağ taraf - 27-44)
        slot = 27;
        for (LivingEntity male : males) {
            if (male == null || !male.isValid() || slot >= 45) break;
            
            String name = male.getCustomName();
            if (name == null) {
                name = male.getType().name();
            }
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Cinsiyet: §bErkek");
            lore.add("§7Sağlık: §e" + String.format("%.1f", male.getHealth()));
            lore.add("§7═══════════════════════");
            lore.add("§aSol Tık: §7Seç");
            
            menu.setItem(slot++, createButton(TamingHelper.getCreatureIcon(male), "§b" + name, lore));
        }
        
        // Seçilen çift bilgisi (ortada)
        menu.setItem(49, createButton(Material.BARRIER, "§cÇift Seçilmedi", 
            Arrays.asList("§7Önce dişi ve erkek seçin")));
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Üreme Yönetimi")) {
            handleMainMenuClick(event);
        } else if (title.equals("§6Üreme Çifti Detayları")) {
            handlePairMenuClick(event);
        } else if (title.equals("§6Üreme Çifti Oluştur")) {
            handleCreateMenuClick(event);
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
            // Geri butonu
            if (plugin.getTamingMenu() != null) {
                plugin.getTamingMenu().openMainMenu(player);
            }
            return;
        }
        
        if (slot == 49 && clicked.getType() == Material.EMERALD) {
            // Yeni çift oluştur
            openCreatePairMenu(player);
            return;
        }
        
        if (slot < 45) {
            // Üreme çifti seçildi
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan == null) return;
            
            List<BreedingPair> activePairs = getActiveBreedingPairs(player, clan);
            if (slot < activePairs.size()) {
                BreedingPair pair = activePairs.get(slot);
                if (pair != null) {
                    openBreedingPairMenu(player, pair.female, pair.male);
                }
            }
        }
    }
    
    private void handlePairMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
        }
    }
    
    private void handleCreateMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        if (slot == 45) {
            // Geri butonu
            openMainMenu(player);
            return;
        }
        
        // Seçim yapıldı
        // TODO: Seçilen canlıları sakla ve çift oluştur
        // Şimdilik basit mesaj
        player.sendMessage("§eÇift oluşturma özelliği yakında eklenecek!");
    }
    
    /**
     * Aktif üreme çiftlerini getir
     */
    private List<BreedingPair> getActiveBreedingPairs(Player player, Clan clan) {
        // Kişisel menü - clan null ise sadece oyuncunun çiftlerini getir
        List<BreedingPair> pairs = new ArrayList<>();
        
        if (player == null || breedingManager == null) {
            return pairs;
        }
        
        try {
            // Reflection ile activeBreedings map'inden al
            java.lang.reflect.Field breedingsField = BreedingManager.class.getDeclaredField("activeBreedings");
            breedingsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, BreedingManager.BreedingData> activeBreedings = 
                (java.util.Map<UUID, BreedingManager.BreedingData>) breedingsField.get(breedingManager);
            
            if (activeBreedings != null) {
                UUID playerId = player.getUniqueId();
                for (BreedingManager.BreedingData data : activeBreedings.values()) {
                    if (data == null) continue;
                    
                    LivingEntity female = data.getFemale();
                    LivingEntity male = data.getMale();
                    
                    if (female == null || male == null || 
                        !female.isValid() || female.isDead() ||
                        !male.isValid() || male.isDead()) {
                        continue;
                    }
                    
                    // Kişisel menü - sadece oyuncunun çiftlerini göster
                    UUID ownerId = data.getOwnerId();
                    if (ownerId != null && ownerId.equals(playerId)) {
                        long remainingTime = data.getRemainingTime();
                        boolean isActive = !data.isComplete();
                        
                        pairs.add(new BreedingPair(female, male, remainingTime, isActive));
                    }
                }
            }
        } catch (Exception e) {
            // Hata durumunda boş liste döndür
        }
        
        return pairs;
    }
    
    /**
     * Üreme çiftini bul
     */
    private BreedingPair findBreedingPair(LivingEntity female, LivingEntity male) {
        if (female == null || male == null) return null;
        
        try {
            java.lang.reflect.Field breedingsField = BreedingManager.class.getDeclaredField("activeBreedings");
            breedingsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, BreedingManager.BreedingData> activeBreedings = 
                (java.util.Map<UUID, BreedingManager.BreedingData>) breedingsField.get(breedingManager);
            
            if (activeBreedings != null) {
                BreedingManager.BreedingData data = activeBreedings.get(female.getUniqueId());
                if (data != null && data.getMale().equals(male)) {
                    long remainingTime = data.getRemainingTime();
                    boolean isActive = !data.isComplete();
                    return new BreedingPair(female, male, remainingTime, isActive);
                }
            }
        } catch (Exception e) {
            // Hata durumunda null döndür
        }
        
        return null;
    }
    
    /**
     * Süreyi formatla
     */
    private String formatTime(long milliseconds) {
        if (milliseconds <= 0) return "§cTamamlandı";
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return "§e" + days + " gün " + (hours % 24) + " saat";
        } else if (hours > 0) {
            return "§e" + hours + " saat " + (minutes % 60) + " dakika";
        } else if (minutes > 0) {
            return "§e" + minutes + " dakika " + (seconds % 60) + " saniye";
        } else {
            return "§e" + seconds + " saniye";
        }
    }
    
    /**
     * Üreme çifti veri sınıfı
     */
    private static class BreedingPair {
        final LivingEntity female;
        final LivingEntity male;
        final long remainingTime;
        final boolean isActive;
        
        BreedingPair(LivingEntity female, LivingEntity male, long remainingTime, boolean isActive) {
            this.female = female;
            this.male = male;
            this.remainingTime = remainingTime;
            this.isActive = isActive;
        }
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

