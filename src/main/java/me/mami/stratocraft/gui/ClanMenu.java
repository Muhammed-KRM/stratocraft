package me.mami.stratocraft.gui;

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
 * ClanMenu - Klan menü sistemi (GUI)
 */
public class ClanMenu implements Listener {
    private final ClanManager clanManager;

    public ClanMenu(ClanManager cm) {
        this.clanManager = cm;
    }

    public void openMenu(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return;
        }

        // 9 slotlu menü oluştur
        Inventory menu = Bukkit.createInventory(null, 9, "§6Klan Menüsü");

        // Klan Bilgisi (Ortada)
        ItemStack clanInfo = new ItemStack(Material.BEACON);
        ItemMeta clanMeta = clanInfo.getItemMeta();
        clanMeta.setDisplayName("§6§l" + clan.getName());
        clanMeta.setLore(Arrays.asList(
            "§7Bakiye: §e" + clan.getBalance() + " altın",
            "§7Üye Sayısı: §e" + clan.getMembers().size(),
            "§7Teknoloji Seviyesi: §e" + clan.getTechLevel()
        ));
        clanInfo.setItemMeta(clanMeta);
        menu.setItem(4, clanInfo);

        // Üyeler Butonu
        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = members.getItemMeta();
        membersMeta.setDisplayName("§aÜyeler");
        membersMeta.setLore(Arrays.asList("§7Klan üyelerini görüntüle"));
        members.setItemMeta(membersMeta);
        menu.setItem(1, members);

        // Market Butonu
        ItemStack market = new ItemStack(Material.EMERALD);
        ItemMeta marketMeta = market.getItemMeta();
        marketMeta.setDisplayName("§aMarket");
        marketMeta.setLore(Arrays.asList("§7Klan marketini aç"));
        market.setItemMeta(marketMeta);
        menu.setItem(3, market);

        // Yükseltmeler Butonu
        ItemStack upgrades = new ItemStack(Material.ANVIL);
        ItemMeta upgradesMeta = upgrades.getItemMeta();
        upgradesMeta.setDisplayName("§aYükseltmeler");
        upgradesMeta.setLore(Arrays.asList("§7Yapı yükseltmelerini görüntüle"));
        upgrades.setItemMeta(upgradesMeta);
        menu.setItem(5, upgrades);

        // Bakiye Butonu
        ItemStack balance = new ItemStack(Material.GOLD_INGOT);
        ItemMeta balanceMeta = balance.getItemMeta();
        balanceMeta.setDisplayName("§aBakiye: §e" + clan.getBalance());
        balanceMeta.setLore(Arrays.asList("§7Klan bakiyesini görüntüle"));
        balance.setItemMeta(balanceMeta);
        menu.setItem(7, balance);

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
        
        switch (clicked.getType()) {
            case PLAYER_HEAD:
                // Üyeler menüsü
                player.sendMessage("§aKlan Üyeleri:");
                if (clan != null) {
                    for (UUID memberId : clan.getMembers().keySet()) {
                        Player member = Bukkit.getPlayer(memberId);
                        String name = member != null ? member.getName() : "Offline";
                        me.mami.stratocraft.model.Clan.Rank rank = clan.getRank(memberId);
                        String rankStr = rank != null ? rank.toString() : "UNKNOWN";
                        player.sendMessage("§7- §e" + name + " §7(" + rankStr + ")");
                    }
                }
                player.closeInventory();
                break;

            case EMERALD:
                // Market menüsü (basit versiyon)
                player.sendMessage("§aMarket menüsü yakında eklenecek!");
                player.closeInventory();
                break;

            case ANVIL:
                // Yükseltmeler menüsü (basit versiyon)
                player.sendMessage("§aYükseltmeler menüsü yakında eklenecek!");
                player.closeInventory();
                break;

            case GOLD_INGOT:
                // Bakiye bilgisi
                if (clan != null) {
                    player.sendMessage("§aKlan Bakiyesi: §e" + clan.getBalance() + " altın");
                } else {
                    // Hata sesi
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage("§cBir klana üye değilsin!");
                }
                player.closeInventory();
                break;
            default:
                // Diğer tüm Material'lar için hiçbir şey yapma
                break;
        }
    }
}

