package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.StratocraftPowerSystem;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.ClanPowerProfile;
import me.mami.stratocraft.model.PlayerPowerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
 * Güç Sistemi GUI Menüsü
 * 
 * Özellikler:
 * - Oyuncu güç bilgileri
 * - Klan güç bilgileri
 * - Top oyuncular listesi
 * - Güç bileşenleri detayları
 */
public class PowerMenu implements Listener {
    private final Main plugin;
    private final StratocraftPowerSystem powerSystem;
    
    // Sayfa numaraları (player -> page)
    private final java.util.Map<UUID, Integer> currentPages = new java.util.concurrent.ConcurrentHashMap<>();
    
    // Kişisel mod takibi (player -> personalMode)
    private final java.util.Map<UUID, Boolean> personalMode = new java.util.concurrent.ConcurrentHashMap<>();
    
    public PowerMenu(Main plugin, StratocraftPowerSystem powerSystem) {
        this.plugin = plugin;
        this.powerSystem = powerSystem;
    }
    
    /**
     * Ana güç menüsünü aç
     */
    public void openMainMenu(Player player) {
        openMainMenu(player, false);
    }
    
    /**
     * Ana güç menüsünü aç (kişisel mod takibi ile)
     */
    public void openMainMenu(Player player, boolean fromPersonalTerminal) {
        if (player == null || powerSystem == null) return;
        
        // Kişisel mod bilgisini sakla
        personalMode.put(player.getUniqueId(), fromPersonalTerminal);
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Güç Sistemi");
        
        // Kendi gücünü göster
        PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(player);
        List<String> myPowerLore = new ArrayList<>();
        myPowerLore.add("§7═══════════════════════");
        myPowerLore.add("§eToplam SGP: §f" + String.format("%.2f", profile.getTotalSGP()));
        myPowerLore.add("§eCombat Power: §f" + String.format("%.2f", profile.getTotalCombatPower()));
        myPowerLore.add("§eProgression Power: §f" + String.format("%.2f", profile.getTotalProgressionPower()));
        myPowerLore.add("§eSeviye: §f" + profile.getPlayerLevel());
        myPowerLore.add("§7═══════════════════════");
        menu.setItem(11, createButton(Material.DIAMOND, "§e§lKendi Gücüm", myPowerLore));
        
        // Klan gücü (eğer klan varsa)
        me.mami.stratocraft.manager.ClanManager clanManager = plugin.getClanManager();
        if (clanManager == null) {
            plugin.getLogger().warning("ClanManager null! Klan gücü gösterilemiyor.");
        } else {
            Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan != null) {
                ClanPowerProfile clanProfile = powerSystem.calculateClanProfile(clan);
                List<String> clanPowerLore = new ArrayList<>();
                clanPowerLore.add("§7═══════════════════════");
                clanPowerLore.add("§eToplam Klan Gücü: §f" + String.format("%.2f", clanProfile.getTotalClanPower()));
                clanPowerLore.add("§eKlan Seviyesi: §f" + clanProfile.getClanLevel());
                clanPowerLore.add("§7═══════════════════════");
                menu.setItem(13, createButton(Material.BEACON, "§e§lKlan Gücü", clanPowerLore));
            }
        }
        
        // Top oyuncular
        menu.setItem(15, createButton(Material.GOLD_INGOT, "§e§lTop Oyuncular", 
            Arrays.asList("§7En güçlü oyuncuları görüntüle")));
        
        // Güç bileşenleri
        menu.setItem(22, createButton(Material.BOOK, "§eGüç Bileşenleri", 
            Arrays.asList("§7Gücünüzün detaylı analizi")));
        
        // Kapat butonu
        menu.setItem(18, createButton(Material.BARRIER, "§cKapat", null));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Top oyuncular menüsü
     */
    public void openTopPlayersMenu(Player player, int page) {
        if (player == null || powerSystem == null) return;
        
        me.mami.stratocraft.manager.SimpleRankingSystem rankingSystem = plugin.getSimpleRankingSystem();
        if (rankingSystem == null) {
            player.sendMessage("§cSıralama sistemi yüklenemedi!");
            return;
        }
        
        List<me.mami.stratocraft.manager.SimpleRankingSystem.PlayerRanking> rankings = 
            rankingSystem.getTopPlayers(100);
        
        // Sayfalama
        int totalPages = Math.max(1, (int) Math.ceil(rankings.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Top Oyuncular - Sayfa " + page);
        
        // Oyuncuları göster
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, rankings.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            me.mami.stratocraft.manager.SimpleRankingSystem.PlayerRanking ranking = rankings.get(i);
            String medal = getMedal(i + 1);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§eSGP: §f" + String.format("%.2f", ranking.getPower()));
            lore.add("§eSeviye: §f" + ranking.getLevel());
            lore.add("§7═══════════════════════");
            
            menu.setItem(slot++, createButton(Material.PLAYER_HEAD, 
                medal + " §e" + (i + 1) + ". §f" + ranking.getPlayerName(), lore));
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§7Önceki Sayfa", null));
        }
        if (page < totalPages) {
            menu.setItem(53, createButton(Material.ARROW, "§7Sonraki Sayfa", null));
        }
        
        // Geri butonu
        menu.setItem(49, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Güç bileşenleri menüsü
     */
    public void openComponentsMenu(Player player) {
        if (player == null || powerSystem == null) return;
        
        PlayerPowerProfile profile = powerSystem.calculatePlayerProfile(player);
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Güç Bileşenleri");
        
        // Bileşenler
        menu.setItem(10, createButton(Material.DIAMOND_CHESTPLATE, "§eEşya Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getGearPower()) + " SGP")));
        
        menu.setItem(11, createButton(Material.EXPERIENCE_BOTTLE, "§eUstalık Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getTrainingPower()) + " SGP")));
        
        menu.setItem(12, createButton(Material.POTION, "§eBuff Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getBuffPower()) + " SGP")));
        
        menu.setItem(13, createButton(Material.ENCHANTING_TABLE, "§eRitüel Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getRitualPower()) + " SGP")));
        
        // Toplamlar
        menu.setItem(15, createButton(Material.DIAMOND_SWORD, "§eCombat Power", 
            Arrays.asList("§7" + String.format("%.2f", profile.getTotalCombatPower()) + " SGP")));
        
        menu.setItem(16, createButton(Material.BOOK, "§eProgression Power", 
            Arrays.asList("§7" + String.format("%.2f", profile.getTotalProgressionPower()) + " SGP")));
        
        menu.setItem(22, createButton(Material.NETHER_STAR, "§e§lToplam SGP", 
            Arrays.asList("§7" + String.format("%.2f", profile.getTotalSGP()) + " SGP")));
        
        // Geri butonu
        menu.setItem(18, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Klan güç detay menüsü
     */
    public void openClanPowerMenu(Player player) {
        if (player == null || powerSystem == null) return;
        
        Clan clan = plugin.getClanManager().getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsiniz!");
            return;
        }
        
        ClanPowerProfile profile = powerSystem.calculateClanProfile(clan);
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Klan Güç Detayları");
        
        // Bileşenler
        menu.setItem(10, createButton(Material.PLAYER_HEAD, "§eÜye Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getMemberPowerSum()) + " SGP")));
        
        menu.setItem(11, createButton(Material.BEACON, "§eYapı Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getStructurePower()) + " SGP")));
        
        menu.setItem(12, createButton(Material.ENCHANTING_TABLE, "§eRitüel Blok Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getRitualBlockPower()) + " SGP")));
        
        menu.setItem(13, createButton(Material.REDSTONE, "§eRitüel Kaynak Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getRitualResourcePower()) + " SGP")));
        
        // Toplam
        menu.setItem(22, createButton(Material.NETHER_STAR, "§e§lToplam Klan Gücü", 
            Arrays.asList("§7" + String.format("%.2f", profile.getTotalClanPower()) + " SGP",
                "§7Klan Seviyesi: §e" + profile.getClanLevel())));
        
        // Geri butonu
        menu.setItem(18, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (title.equals("§6Güç Sistemi")) {
            handleMainMenuClick(event);
        } else if (title.startsWith("§6Top Oyuncular")) {
            handleTopPlayersClick(event);
        } else if (title.equals("§6Güç Bileşenleri")) {
            handleComponentsClick(event);
        } else if (title.equals("§6Klan Güç Detayları")) {
            handleClanPowerClick(event);
        }
    }
    
    private void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (clicked.getType()) {
            case DIAMOND:
                // Güç bileşenleri
                openComponentsMenu(player);
                break;
                
            case BEACON:
                // Klan güç detayları
                openClanPowerMenu(player);
                break;
                
            case GOLD_INGOT:
                // Top oyuncular
                openTopPlayersMenu(player, 1);
                break;
                
            case BOOK:
                // Güç bileşenleri
                openComponentsMenu(player);
                break;
                
            case BARRIER:
                // Kapat veya Personal Terminal'e dön
                Boolean isPersonal = personalMode.getOrDefault(player.getUniqueId(), false);
                if (isPersonal && plugin.getPersonalTerminalListener() != null) {
                    plugin.getPersonalTerminalListener().openMainMenu(player);
                } else {
                    player.closeInventory();
                }
                break;
        }
    }
    
    private void handleTopPlayersClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        String title = event.getView().getTitle();
        int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
        
        switch (clicked.getType()) {
            case ARROW:
                if (slot == 45) {
                    openTopPlayersMenu(player, currentPage - 1);
                } else if (slot == 53) {
                    openTopPlayersMenu(player, currentPage + 1);
                } else if (slot == 49) {
                    // Geri butonu - Personal Terminal'e dön (kişisel modda)
                    Boolean isPersonal = personalMode.getOrDefault(player.getUniqueId(), false);
                    if (isPersonal && plugin.getPersonalTerminalListener() != null) {
                        plugin.getPersonalTerminalListener().openMainMenu(player);
                    } else {
                        openMainMenu(player);
                    }
                }
                break;
        }
    }
    
    private void handleComponentsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            // Geri butonu - Personal Terminal'e dön (kişisel modda)
            Boolean isPersonal = personalMode.getOrDefault(player.getUniqueId(), false);
            if (isPersonal && plugin.getPersonalTerminalListener() != null) {
                plugin.getPersonalTerminalListener().openMainMenu(player);
            } else {
                openMainMenu(player);
            }
        }
    }
    
    private void handleClanPowerClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            // Geri butonu - Personal Terminal'e dön (kişisel modda)
            Boolean isPersonal = personalMode.getOrDefault(player.getUniqueId(), false);
            if (isPersonal && plugin.getPersonalTerminalListener() != null) {
                plugin.getPersonalTerminalListener().openMainMenu(player);
            } else {
                openMainMenu(player);
            }
        }
    }
    
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
    
    private String getMedal(int rank) {
        switch (rank) {
            case 1: return "§6§l🥇";
            case 2: return "§7§l🥈";
            case 3: return "§c§l🥉";
            default: return "§7";
        }
    }
}













