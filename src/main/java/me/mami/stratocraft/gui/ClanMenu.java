package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * ClanMenu - Klan menü sistemi (GUI) - Genişletilmiş Versiyon
 * 
 * Özellikler:
 * - Klan bilgileri
 * - Üye yönetimi
 * - Banka erişimi
 * - Görevler
 * - Maaş yönetimi (Lider/General)
 * - Alan genişletme (Lider/General)
 * - Market
 * - Yükseltmeler
 */
public class ClanMenu implements Listener {
    private final ClanManager clanManager;
    private Main plugin;

    public ClanMenu(ClanManager cm) {
        this.clanManager = cm;
        this.plugin = Main.getInstance();
    }

    public void openMenu(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return;
        }

        // 27 slotlu menü oluştur (3x9)
        Inventory menu = Bukkit.createInventory(null, 27, "§6Klan Menüsü");

        // Klan Bilgisi (Ortada - Slot 13)
        ItemStack clanInfo = new ItemStack(Material.BEACON);
        ItemMeta clanMeta = clanInfo.getItemMeta();
        if (clanMeta != null) {
            clanMeta.setDisplayName("§6§l" + clan.getName());
            
            // Territory bilgisi
            String territoryInfo = "Yok";
            if (clan.getTerritory() != null) {
                territoryInfo = "Radius: " + clan.getTerritory().getRadius() + " blok";
            }
            
            clanMeta.setLore(Arrays.asList(
                "§7Bakiye: §e" + clan.getBalance() + " altın",
                "§7Üye Sayısı: §e" + clan.getMembers().size(),
                "§7Teknoloji Seviyesi: §e" + clan.getTechLevel(),
                "§7Bölge: §e" + territoryInfo
            ));
            clanInfo.setItemMeta(clanMeta);
        }
        menu.setItem(13, clanInfo);

        // Üyeler Butonu (Slot 10)
        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = members.getItemMeta();
        if (membersMeta != null) {
            membersMeta.setDisplayName("§aÜyeler");
            membersMeta.setLore(Arrays.asList(
                "§7Klan üyelerini görüntüle",
                "§7Tıklayarak üye listesini aç"
            ));
            members.setItemMeta(membersMeta);
        }
        menu.setItem(10, members);

        // Banka Butonu (Slot 11) - YENİ
        ItemStack bank = new ItemStack(Material.ENDER_CHEST);
        ItemMeta bankMeta = bank.getItemMeta();
        if (bankMeta != null) {
            bankMeta.setDisplayName("§aKlan Bankası");
            bankMeta.setLore(Arrays.asList(
                "§7Klan bankasına eriş",
                "§7Item yatır/çek"
            ));
            bank.setItemMeta(bankMeta);
        }
        menu.setItem(11, bank);

        // Görevler Butonu (Slot 12) - YENİ
        ItemStack missions = new ItemStack(Material.BOOK);
        ItemMeta missionsMeta = missions.getItemMeta();
        if (missionsMeta != null) {
            missionsMeta.setDisplayName("§aKlan Görevleri");
            missionsMeta.setLore(Arrays.asList(
                "§7Aktif görevleri görüntüle",
                "§7Görev ilerlemesini takip et"
            ));
            missions.setItemMeta(missionsMeta);
        }
        menu.setItem(12, missions);

        // Maaş Yönetimi Butonu (Slot 14) - YENİ (Sadece Lider/General)
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
            ItemStack salary = new ItemStack(Material.GOLD_NUGGET);
            ItemMeta salaryMeta = salary.getItemMeta();
            if (salaryMeta != null) {
                salaryMeta.setDisplayName("§aMaaş Yönetimi");
                salaryMeta.setLore(Arrays.asList(
                    "§7Maaş ayarlarını yönet",
                    "§7Maaş iptal et/aktifleştir"
                ));
                salary.setItemMeta(salaryMeta);
            }
            menu.setItem(14, salary);
        }

        // Alan Genişletme Butonu (Slot 15) - YENİ (Sadece Lider/General)
        if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
            ItemStack expand = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta expandMeta = expand.getItemMeta();
            if (expandMeta != null) {
                String radiusInfo = "Yok";
                if (clan.getTerritory() != null) {
                    radiusInfo = clan.getTerritory().getRadius() + " blok";
                }
                expandMeta.setDisplayName("§aAlan Genişletme");
                expandMeta.setLore(Arrays.asList(
                    "§7Mevcut Alan: §e" + radiusInfo,
                    "§7Alanı genişletmek için tıkla"
                ));
                expand.setItemMeta(expandMeta);
            }
            menu.setItem(15, expand);
        }

        // Market Butonu (Slot 16)
        ItemStack market = new ItemStack(Material.EMERALD);
        ItemMeta marketMeta = market.getItemMeta();
        if (marketMeta != null) {
            marketMeta.setDisplayName("§aMarket");
            marketMeta.setLore(Arrays.asList("§7Klan marketini aç"));
            market.setItemMeta(marketMeta);
        }
        menu.setItem(16, market);

        // Yükseltmeler Butonu (Slot 17)
        ItemStack upgrades = new ItemStack(Material.ANVIL);
        ItemMeta upgradesMeta = upgrades.getItemMeta();
        if (upgradesMeta != null) {
            upgradesMeta.setDisplayName("§aYükseltmeler");
            upgradesMeta.setLore(Arrays.asList("§7Yapı yükseltmelerini görüntüle"));
            upgrades.setItemMeta(upgradesMeta);
        }
        menu.setItem(17, upgrades);
        
        // İstatistikler Butonu (Slot 18) - YENİ
        ItemStack stats = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = stats.getItemMeta();
        if (statsMeta != null) {
            statsMeta.setDisplayName("§aİstatistikler");
            statsMeta.setLore(Arrays.asList(
                "§7Klan istatistiklerini görüntüle",
                "§7Güç, seviye, üye bilgileri"
            ));
            stats.setItemMeta(statsMeta);
        }
        menu.setItem(18, stats);

        // Bakiye Butonu (Slot 22)
        ItemStack balance = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balance.getItemMeta();
        if (balanceMeta != null) {
            balanceMeta.setDisplayName("§aBakiye: §e" + clan.getBalance());
            balanceMeta.setLore(Arrays.asList("§7Klan bakiyesini görüntüle"));
            balance.setItemMeta(balanceMeta);
        }
        menu.setItem(22, balance);

        // Menü açılma sesi
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        player.openInventory(menu);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Klan Menüsü")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true); // Menüden eşya çıkarılmasını engelle

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Tıklama sesi
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            player.closeInventory();
            return;
        }
        
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        
        switch (clicked.getType()) {
            case PLAYER_HEAD:
                // Üyeler menüsü - GUI
                if (plugin != null && plugin.getClanMemberMenu() != null) {
                    plugin.getClanMemberMenu().openMenu(player);
                } else {
                    player.sendMessage("§cÜye yönetimi sistemi aktif değil!");
                    player.closeInventory();
                }
                break;

            case ENDER_CHEST:
                // Banka erişimi - YENİ
                if (plugin != null && plugin.getClanBankSystem() != null) {
                    org.bukkit.inventory.Inventory bankChest = 
                        plugin.getClanBankSystem().getBankChest(clan);
                    if (bankChest != null) {
                        player.openInventory(bankChest);
                        player.sendMessage("§aKlan bankası açıldı!");
                    } else {
                        player.sendMessage("§cKlan bankası bulunamadı! Önce bir banka oluşturun.");
                    }
                } else {
                    player.sendMessage("§cBanka sistemi aktif değil!");
                }
                break;

            case BOOK:
                // Görevler - GUI Menüsü
                if (plugin != null && plugin.getClanMissionSystem() != null && plugin.getClanMissionMenu() != null) {
                    plugin.getClanMissionMenu().openMenu(player);
                } else {
                    player.sendMessage("§cGörev sistemi aktif değil!");
                    player.closeInventory();
                }
                break;

            case GOLD_NUGGET:
                // Maaş Yönetimi - YENİ (Sadece Lider/General)
                if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
                    player.sendMessage("§a§l═══════════════════════════");
                    player.sendMessage("§aMaaş Yönetimi:");
                    player.sendMessage("§7Maaş sistemi otomatik çalışıyor.");
                    player.sendMessage("§7Maaş iptal etmek için: §e/klan maas iptal");
                    player.sendMessage("§7Maaş aktifleştirmek için: §e/klan maas aktif");
                    player.sendMessage("§a§l═══════════════════════════");
                } else {
                    player.sendMessage("§cBu işlem için yetkiniz yok!");
                }
                player.closeInventory();
                break;

            case GRASS_BLOCK:
                // Alan Genişletme - YENİ (Sadece Lider/General)
                if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
                    if (clan.getTerritory() != null) {
                        player.sendMessage("§a§l═══════════════════════════");
                        player.sendMessage("§aAlan Genişletme:");
                        player.sendMessage("§7Mevcut Alan: §e" + clan.getTerritory().getRadius() + " blok");
                        player.sendMessage("§7Alanı genişletmek için: §e/klan alan genislet <miktar>");
                        player.sendMessage("§7Not: Alan genişletme için gerekli itemler gerekli.");
                        player.sendMessage("§a§l═══════════════════════════");
                    } else {
                        player.sendMessage("§cKlan bölgeniz yok! Önce bir kristal dikin.");
                    }
                } else {
                    player.sendMessage("§cBu işlem için yetkiniz yok!");
                }
                player.closeInventory();
                break;

            case EMERALD:
                // Market menüsü
                player.sendMessage("§aMarket menüsü yakında eklenecek!");
                player.closeInventory();
                break;

            case ANVIL:
                // Yükseltmeler menüsü
                player.sendMessage("§aYükseltmeler menüsü yakında eklenecek!");
                player.closeInventory();
                break;
                
            case PAPER:
                // İstatistikler
                if (plugin != null && plugin.getClanStatsMenu() != null) {
                    plugin.getClanStatsMenu().openMenu(player);
                } else {
                    player.sendMessage("§cİstatistik sistemi aktif değil!");
                    player.closeInventory();
                }
                break;

            case GOLD_INGOT:
                // Bakiye bilgisi
                player.sendMessage("§a§l═══════════════════════════");
                player.sendMessage("§aKlan Bakiyesi: §e" + clan.getBalance() + " altın");
                player.sendMessage("§7Para yatırmak için: §e/klan para yatir <miktar>");
                player.sendMessage("§7Para çekmek için: §e/klan para cek <miktar>");
                player.sendMessage("§a§l═══════════════════════════");
                player.closeInventory();
                break;
            default:
                // Diğer tüm Material'lar için hiçbir şey yapma
                break;
        }
    }
}

