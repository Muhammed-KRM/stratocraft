package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.clan.ClanMissionSystem;
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

import java.util.*;

/**
 * ClanMissionMenu - Klan Görev Sistemi GUI Menüsü
 * 
 * Özellikler:
 * - Aktif görevleri görüntüleme
 * - Görev ilerlemesini takip etme
 * - Üye bazlı ilerleme gösterimi
 * - Görev oluşturma (Lider/General)
 * - Görev geçmişi (opsiyonel)
 */
public class ClanMissionMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final ClanMissionSystem missionSystem;
    
    public ClanMissionMenu(Main plugin, ClanManager clanManager, ClanMissionSystem missionSystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.missionSystem = missionSystem;
    }
    
    /**
     * Ana görev menüsünü aç
     */
    public void openMenu(Player player) {
        if (player == null) return;
        
        // Manager null kontrolleri
        if (clanManager == null) {
            player.sendMessage("§cKlan sistemi aktif değil!");
            plugin.getLogger().warning("ClanManager null! Menü açılamıyor.");
            return;
        }
        
        if (missionSystem == null) {
            player.sendMessage("§cGörev sistemi aktif değil!");
            plugin.getLogger().warning("ClanMissionSystem null! Menü açılamıyor.");
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // 54 slotlu menü oluştur (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Klan Görevleri");
        
        // Aktif görev var mı?
        ClanMissionSystem.ClanMission activeMission = missionSystem.getActiveMission(clan);
        
        if (activeMission != null) {
            // Aktif görev bilgileri (Slot 4 - Ortada)
            ItemStack missionInfo = createMissionInfoItem(activeMission);
            menu.setItem(4, missionInfo);
            
            // İlerleme çubuğu (Slot 13-17)
            createProgressBar(menu, activeMission);
            
            // Üye ilerlemeleri (Slot 19-53)
            createMemberProgressItems(menu, activeMission, clan);
            
            // Geri butonu (Slot 0)
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Geri");
                backMeta.setLore(Arrays.asList("§7Ana menüye dön"));
                backButton.setItemMeta(backMeta);
            }
            menu.setItem(0, backButton);
            
            // Görev iptal butonu (Slot 8 - Sadece Lider/General)
            Clan.Rank playerRank = clan.getRank(player.getUniqueId());
            if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
                ItemStack cancelButton = new ItemStack(Material.BARRIER);
                ItemMeta cancelMeta = cancelButton.getItemMeta();
                if (cancelMeta != null) {
                    cancelMeta.setDisplayName("§cGörevi İptal Et");
                    cancelMeta.setLore(Arrays.asList(
                        "§7Bu görevi iptal et",
                        "§cDikkat: Bu işlem geri alınamaz!"
                    ));
                    cancelButton.setItemMeta(cancelMeta);
                }
                menu.setItem(8, cancelButton);
            }
        } else {
            // Aktif görev yok
            ItemStack noMission = new ItemStack(Material.BARRIER);
            ItemMeta noMissionMeta = noMission.getItemMeta();
            if (noMissionMeta != null) {
                noMissionMeta.setDisplayName("§cAktif Görev Yok");
                noMissionMeta.setLore(Arrays.asList(
                    "§7Şu anda aktif bir görev bulunmuyor",
                    "§7Lider veya General görev oluşturabilir"
                ));
                noMission.setItemMeta(noMissionMeta);
            }
            menu.setItem(22, noMission);
            
            // Görev oluştur butonu (Slot 31 - Sadece Lider/General)
            Clan.Rank playerRank = clan.getRank(player.getUniqueId());
            if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
                ItemStack createButton = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta createMeta = createButton.getItemMeta();
                if (createMeta != null) {
                    createMeta.setDisplayName("§aYeni Görev Oluştur");
                    createMeta.setLore(Arrays.asList(
                        "§7Yeni bir klan görevi oluştur",
                        "§7Tıklayarak görev oluşturma menüsünü aç"
                    ));
                    createButton.setItemMeta(createMeta);
                }
                menu.setItem(31, createButton);
            }
            
            // Geri butonu (Slot 0)
            ItemStack backButton = new ItemStack(Material.ARROW);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName("§7Geri");
                backMeta.setLore(Arrays.asList("§7Ana menüye dön"));
                backButton.setItemMeta(backMeta);
            }
            menu.setItem(0, backButton);
        }
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Görev bilgi item'ı oluştur
     */
    private ItemStack createMissionInfoItem(ClanMissionSystem.ClanMission mission) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§6§l" + getMissionTypeName(mission.getType()));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7Hedef: §e" + mission.getTarget());
        lore.add("§7İlerleme: §e" + mission.getProgress() + "§7/§e" + mission.getTarget());
        
        // İlerleme yüzdesi
        double progressPercent = (double) mission.getProgress() / mission.getTarget() * 100;
        lore.add("§7Tamamlanma: §e" + String.format("%.1f", progressPercent) + "%");
        
        // Kalan süre
        long remainingTime = mission.getExpiryTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            long hours = remainingTime / (1000 * 60 * 60);
            long minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60);
            lore.add("§7Kalan Süre: §e" + hours + "s " + minutes + "dk");
        } else {
            lore.add("§cSüre Doldu!");
        }
        
        if (mission.getTargetMaterial() != null) {
            lore.add("§7Hedef Materyal: §e" + mission.getTargetMaterial().name());
        }
        
        if (mission.getDescription() != null && !mission.getDescription().isEmpty()) {
            lore.add("§7═══════════════════════");
            lore.add("§7Açıklama:");
            // Açıklamayı satırlara böl (uzunsa)
            String[] descLines = mission.getDescription().split("\n");
            for (String line : descLines) {
                if (line.length() > 30) {
                    // Uzun satırları böl
                    for (int i = 0; i < line.length(); i += 30) {
                        int end = Math.min(i + 30, line.length());
                        lore.add("§7" + line.substring(i, end));
                    }
                } else {
                    lore.add("§7" + line);
                }
            }
        }
        
        lore.add("§7═══════════════════════");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * İlerleme çubuğu oluştur
     */
    private void createProgressBar(Inventory menu, ClanMissionSystem.ClanMission mission) {
        double progressPercent = (double) mission.getProgress() / mission.getTarget();
        int filledSlots = (int) (progressPercent * 5); // 5 slot çubuk
        
        for (int i = 0; i < 5; i++) {
            ItemStack barItem;
            if (i < filledSlots) {
                barItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta meta = barItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§a■");
                    barItem.setItemMeta(meta);
                }
            } else {
                barItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = barItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§7■");
                    barItem.setItemMeta(meta);
                }
            }
            menu.setItem(13 + i, barItem);
        }
    }
    
    /**
     * Üye ilerleme item'ları oluştur
     */
    private void createMemberProgressItems(Inventory menu, ClanMissionSystem.ClanMission mission, Clan clan) {
        // Üye ilerlemelerini al (reflection ile)
        Map<UUID, Integer> memberProgress = getMemberProgress(mission);
        if (memberProgress == null || memberProgress.isEmpty()) {
            // İlerleme yok
            ItemStack noProgress = new ItemStack(Material.BARRIER);
            ItemMeta meta = noProgress.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cHenüz İlerleme Yok");
                meta.setLore(Arrays.asList("§7Hiçbir üye henüz katkıda bulunmadı"));
                noProgress.setItemMeta(meta);
            }
            menu.setItem(22, noProgress);
            return;
        }
        
        // Üye ilerlemelerini sırala (en çok katkıda bulunan önce)
        List<Map.Entry<UUID, Integer>> sortedProgress = new ArrayList<>(memberProgress.entrySet());
        sortedProgress.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Slot 19'dan başla (maksimum 35 slot = 19-53)
        int slot = 19;
        for (Map.Entry<UUID, Integer> entry : sortedProgress) {
            if (slot > 53) break; // Menü dolu
            
            UUID memberId = entry.getKey();
            int progress = entry.getValue();
            
            // Oyuncu adını al
            String memberName = Bukkit.getOfflinePlayer(memberId).getName();
            if (memberName == null) memberName = "Bilinmeyen";
            
            // Online/Offline kontrolü
            Player onlinePlayer = Bukkit.getPlayer(memberId);
            Material headMaterial = onlinePlayer != null ? Material.PLAYER_HEAD : Material.SKELETON_SKULL;
            
            ItemStack memberItem = new ItemStack(headMaterial);
            ItemMeta memberMeta = memberItem.getItemMeta();
            if (memberMeta != null) {
                memberMeta.setDisplayName("§e" + memberName);
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Katkı: §e" + progress);
                
                // Yüzde hesapla
                double memberPercent = (double) progress / mission.getTarget() * 100;
                lore.add("§7Toplam İlerleme: §e" + String.format("%.1f", memberPercent) + "%");
                
                lore.add("§7Durum: " + (onlinePlayer != null ? "§aOnline" : "§7Offline"));
                lore.add("§7═══════════════════════");
                
                memberMeta.setLore(lore);
                memberItem.setItemMeta(memberMeta);
            }
            
            menu.setItem(slot, memberItem);
            slot++;
        }
    }
    
    /**
     * Üye ilerlemelerini al (reflection ile)
     */
    private Map<UUID, Integer> getMemberProgress(ClanMissionSystem.ClanMission mission) {
        try {
            java.lang.reflect.Field field = ClanMissionSystem.ClanMission.class.getDeclaredField("memberProgress");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Integer> progress = (Map<UUID, Integer>) field.get(mission);
            return progress;
        } catch (Exception e) {
            plugin.getLogger().warning("Member progress okuma hatası: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Görev tipi adını al
     */
    private String getMissionTypeName(ClanMissionSystem.MissionType type) {
        if (type == null) return "Bilinmeyen";
        
        switch (type) {
            case DEPOSIT_ITEM:
                return "Item Yatırma";
            case BUILD_STRUCTURE:
                return "Yapı İnşa";
            case USE_RITUAL:
                return "Ritüel Tamamla";
            case WIN_WAR:
                return "Savaş Kazan";
            default:
                return type.name();
        }
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Klan Görevleri")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            player.closeInventory();
            return;
        }
        
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri butonu
                if (plugin.getClanMenu() != null) {
                    plugin.getClanMenu().openMenu(player);
                } else {
                    player.closeInventory();
                }
                break;
                
            case BARRIER:
                // Görev iptal butonu (sadece Lider/General)
                if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
                    ClanMissionSystem.ClanMission activeMission = missionSystem.getActiveMission(clan);
                    if (activeMission != null) {
                        // Görevi iptal et
                        if (missionSystem.cancelMission(clan, player)) {
                            player.sendMessage("§aGörev iptal edildi!");
                            player.closeInventory();
                            // Menüyü yeniden aç
                            Bukkit.getScheduler().runTaskLater(plugin, () -> openMenu(player), 5L);
                        } else {
                            player.sendMessage("§cGörev iptal edilemedi!");
                        }
                    }
                }
                break;
                
            case WRITABLE_BOOK:
                // Yeni görev oluştur butonu (sadece Lider/General)
                if (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL) {
                    player.closeInventory();
                    player.sendMessage("§eGörev oluşturma sistemi yakında eklenecek!");
                    player.sendMessage("§7Şu an için komut kullanın: /klan görev oluştur");
                }
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}

