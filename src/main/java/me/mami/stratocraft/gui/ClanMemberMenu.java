package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.clan.ClanRankSystem;
import me.mami.stratocraft.model.Clan;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * ClanMemberMenu - Klan Üye Yönetimi GUI Menüsü
 * 
 * Özellikler:
 * - Üye listesi görüntüleme
 * - Rütbe değiştirme (Lider/General)
 * - Üye çıkarma (Lider/General)
 * - Online/Offline durumu
 * - Üye aktivite bilgisi
 */
public class ClanMemberMenu implements Listener {
    private final Main plugin;
    private final ClanManager clanManager;
    private final ClanRankSystem rankSystem;
    
    // Rütbe değiştirme için geçici depolama (player -> target member)
    private final Map<UUID, UUID> pendingRankChange = new HashMap<>();
    // Üye çıkarma için geçici depolama
    private final Map<UUID, UUID> pendingMemberRemove = new HashMap<>();
    
    public ClanMemberMenu(Main plugin, ClanManager clanManager, ClanRankSystem rankSystem) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.rankSystem = rankSystem;
    }
    
    /**
     * Ana üye menüsünü aç
     */
    public void openMenu(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        // 54 slotlu menü oluştur (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Klan Üyeleri");
        
        // Üye listesini al ve sırala (rütbe seviyesine göre)
        List<Map.Entry<UUID, Clan.Rank>> sortedMembers = new ArrayList<>(clan.getMembers().entrySet());
        sortedMembers.sort((a, b) -> {
            // Lider her zaman en üstte
            if (a.getValue() == Clan.Rank.LEADER) return -1;
            if (b.getValue() == Clan.Rank.LEADER) return 1;
            // Sonra rütbe seviyesine göre (isAtLeast kullanarak)
            // Yüksek rütbe önce gelir
            if (a.getValue().isAtLeast(b.getValue()) && !b.getValue().isAtLeast(a.getValue())) {
                return -1; // a daha yüksek
            } else if (b.getValue().isAtLeast(a.getValue()) && !a.getValue().isAtLeast(b.getValue())) {
                return 1; // b daha yüksek
            } else {
                return 0; // Aynı seviye
            }
        });
        
        // Üye item'larını oluştur (Slot 9-44)
        int slot = 9;
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        boolean canManage = (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL);
        
        for (Map.Entry<UUID, Clan.Rank> entry : sortedMembers) {
            if (slot > 44) break; // Menü dolu
            
            UUID memberId = entry.getKey();
            Clan.Rank memberRank = entry.getValue();
            
            // Üye item'ı oluştur (UUID'yi NBT'ye ekle)
            ItemStack memberItem = createMemberItem(memberId, memberRank, canManage, player.getUniqueId().equals(memberId));
            
            // UUID'yi item meta'ya ekle (custom NBT - güvenilir yöntem)
            ItemMeta meta = memberItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "member_uuid");
                meta.getPersistentDataContainer().set(uuidKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, memberId.toString());
                memberItem.setItemMeta(meta);
            }
            
            menu.setItem(slot, memberItem);
            slot++;
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
        
        // Bilgi butonu (Slot 4)
        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eÜye Yönetimi");
            infoMeta.setLore(Arrays.asList(
                "§7═══════════════════════",
                "§7Toplam Üye: §e" + sortedMembers.size(),
                "§7Online: §a" + getOnlineCount(clan),
                "§7Offline: §7" + (sortedMembers.size() - getOnlineCount(clan)),
                "§7═══════════════════════",
                canManage ? "§aLider/General olarak üye yönetimi yapabilirsiniz" : "§7Sadece görüntüleme modu"
            ));
            infoButton.setItemMeta(infoMeta);
        }
        menu.setItem(4, infoButton);
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Üye item'ı oluştur
     */
    private ItemStack createMemberItem(UUID memberId, Clan.Rank rank, boolean canManage, boolean isSelf) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);
        String memberName = offlinePlayer.getName();
        if (memberName == null) memberName = "Bilinmeyen";
        
        // Online/Offline kontrolü
        Player onlinePlayer = Bukkit.getPlayer(memberId);
        boolean isOnline = onlinePlayer != null;
        
        // Kafa item'ı (Player Head veya Skeleton Skull)
        ItemStack item = new ItemStack(isOnline ? Material.PLAYER_HEAD : Material.SKELETON_SKULL);
        
        // SkullMeta ayarla (oyuncu kafası için)
        if (item.getItemMeta() instanceof SkullMeta && isOnline) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(onlinePlayer);
                item.setItemMeta(skullMeta);
            }
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                item.setItemMeta(meta);
            }
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        // Rütbe renkleri
        String rankColor = getRankColor(rank);
        String rankName = getRankName(rank);
        
        meta.setDisplayName(rankColor + "§l" + memberName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7Rütbe: " + rankColor + rankName);
        lore.add("§7Durum: " + (isOnline ? "§aOnline" : "§7Offline"));
        
        // Aktivite bilgisi (ClanActivitySystem varsa)
        if (plugin.getClanActivitySystem() != null) {
            long lastActivity = plugin.getClanActivitySystem().getLastActivity(memberId);
            if (lastActivity > 0) {
                long inactiveTime = System.currentTimeMillis() - lastActivity;
                long days = inactiveTime / (1000 * 60 * 60 * 24);
                if (days > 0) {
                    lore.add("§7Son Görülme: §e" + days + " gün önce");
                } else {
                    long hours = inactiveTime / (1000 * 60 * 60);
                    if (hours > 0) {
                        lore.add("§7Son Görülme: §a" + hours + " saat önce");
                    } else {
                        lore.add("§7Son Görülme: §aBugün");
                    }
                }
            }
        }
        
        lore.add("§7═══════════════════════");
        
        // Yönetim butonları (sadece Lider/General ve kendisi değilse)
        if (canManage && !isSelf && rank != Clan.Rank.LEADER) {
            lore.add("§eSol Tık: §7Rütbe Değiştir");
            lore.add("§cSağ Tık: §7Üyeyi Çıkar");
        } else if (isSelf) {
            lore.add("§7Kendi üyeliğinizi yönetemezsiniz");
        } else if (rank == Clan.Rank.LEADER) {
            lore.add("§7Lider çıkarılamaz");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Rütbe rengi
     */
    private String getRankColor(Clan.Rank rank) {
        if (rank == null) return "§7";
        switch (rank) {
            case LEADER: return "§c";
            case ELITE: return "§6";
            case GENERAL: return "§e";
            case MEMBER: return "§a";
            case RECRUIT: return "§7";
            default: return "§7";
        }
    }
    
    /**
     * Rütbe adı (Türkçe)
     */
    private String getRankName(Clan.Rank rank) {
        if (rank == null) return "Bilinmeyen";
        switch (rank) {
            case LEADER: return "Lider";
            case ELITE: return "Elite";
            case GENERAL: return "General";
            case MEMBER: return "Üye";
            case RECRUIT: return "Acemi";
            default: return "Bilinmeyen";
        }
    }
    
    /**
     * Online üye sayısı
     */
    private int getOnlineCount(Clan clan) {
        if (clan == null) return 0;
        int count = 0;
        for (UUID memberId : clan.getMembers().keySet()) {
            if (Bukkit.getPlayer(memberId) != null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Rütbe seçim menüsünü aç
     */
    public void openRankSelectionMenu(Player player, UUID targetMemberId) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            return;
        }
        
        Clan.Rank currentRank = clan.getRank(targetMemberId);
        if (currentRank == null) {
            player.sendMessage("§cÜye bulunamadı!");
            return;
        }
        
        // Geçici depolama
        pendingRankChange.put(player.getUniqueId(), targetMemberId);
        
        // 27 slotlu menü (3x9)
        Inventory menu = Bukkit.createInventory(null, 27, "§6Rütbe Seç");
        
        // Rütbe butonları
        Clan.Rank[] ranks = {Clan.Rank.RECRUIT, Clan.Rank.MEMBER, Clan.Rank.GENERAL, Clan.Rank.ELITE};
        int[] slots = {10, 12, 14, 16};
        
        for (int i = 0; i < ranks.length; i++) {
            Clan.Rank rank = ranks[i];
            ItemStack rankItem = new ItemStack(getRankMaterial(rank));
            ItemMeta meta = rankItem.getItemMeta();
            if (meta != null) {
                String rankColor = getRankColor(rank);
                meta.setDisplayName(rankColor + "§l" + getRankName(rank));
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                if (rank == currentRank) {
                    lore.add("§e§lMevcut Rütbe");
                } else {
                    lore.add("§7Bu rütbeye terfi ettir");
                }
                lore.add("§7═══════════════════════");
                
                meta.setLore(lore);
                rankItem.setItemMeta(meta);
            }
            menu.setItem(slots[i], rankItem);
        }
        
        // Lider rütbesi (sadece gösterim, değiştirilemez)
        ItemStack leaderItem = new ItemStack(Material.BARRIER);
        ItemMeta leaderMeta = leaderItem.getItemMeta();
        if (leaderMeta != null) {
            leaderMeta.setDisplayName("§c§lLider");
            leaderMeta.setLore(Arrays.asList(
                "§7═══════════════════════",
                "§cLider rütbesi değiştirilemez",
                "§7Liderlik devretme ritüeli gerekli",
                "§7═══════════════════════"
            ));
            leaderItem.setItemMeta(leaderMeta);
        }
        menu.setItem(22, leaderItem);
        
        // Geri butonu (Slot 0)
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Geri");
            backMeta.setLore(Arrays.asList("§7Üye listesine dön"));
            backButton.setItemMeta(backMeta);
        }
        menu.setItem(0, backButton);
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Rütbe material'ı
     */
    private Material getRankMaterial(Clan.Rank rank) {
        if (rank == null) return Material.BARRIER;
        switch (rank) {
            case RECRUIT: return Material.WOODEN_SWORD;
            case MEMBER: return Material.IRON_SWORD;
            case GENERAL: return Material.DIAMOND_SWORD;
            case ELITE: return Material.NETHERITE_SWORD;
            default: return Material.BARRIER;
        }
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // Üye listesi menüsü
        if (title.equals("§6Klan Üyeleri")) {
            handleMemberListClick(event);
        }
        // Rütbe seçim menüsü
        else if (title.equals("§6Rütbe Seç")) {
            handleRankSelectionClick(event);
        }
    }
    
    /**
     * Üye listesi menüsü tıklama
     */
    private void handleMemberListClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
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
        boolean canManage = (playerRank == Clan.Rank.LEADER || playerRank == Clan.Rank.GENERAL);
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri butonu
                if (plugin.getClanMenu() != null) {
                    plugin.getClanMenu().openMenu(player);
                } else {
                    player.closeInventory();
                }
                break;
                
            case PLAYER_HEAD:
            case SKELETON_SKULL:
                // Üye item'ı
                if (!canManage) {
                    player.sendMessage("§cÜye yönetimi için Lider veya General olmalısınız!");
                    player.closeInventory();
                    return;
                }
                
                // Üye UUID'sini al (NBT'den veya item meta'dan)
                UUID targetMemberId = getMemberIdFromItem(clicked, clan);
                if (targetMemberId == null) {
                    // Alternatif: Slot numarasından al (daha güvenilir)
                    int slot = event.getSlot();
                    targetMemberId = getMemberIdFromSlot(slot, clan);
                }
                
                if (targetMemberId == null) {
                    player.sendMessage("§cÜye bulunamadı!");
                    return;
                }
                
                // Kendisi veya lider kontrolü
                if (targetMemberId.equals(player.getUniqueId())) {
                    player.sendMessage("§cKendi üyeliğinizi yönetemezsiniz!");
                    return;
                }
                
                Clan.Rank targetRank = clan.getRank(targetMemberId);
                if (targetRank == Clan.Rank.LEADER) {
                    player.sendMessage("§cLider çıkarılamaz veya rütbesi değiştirilemez!");
                    return;
                }
                
                // Sol tık: Rütbe değiştir
                if (event.isLeftClick()) {
                    openRankSelectionMenu(player, targetMemberId);
                }
                // Sağ tık: Üye çıkar
                else if (event.isRightClick()) {
                    // Onay menüsü aç
                    openRemoveConfirmationMenu(player, targetMemberId);
                }
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Rütbe seçim menüsü tıklama
     */
    private void handleRankSelectionClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        UUID playerId = player.getUniqueId();
        UUID targetMemberId = pendingRankChange.get(playerId);
        
        if (targetMemberId == null) {
            player.sendMessage("§cHedef üye bulunamadı!");
            player.closeInventory();
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(playerId);
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            player.closeInventory();
            pendingRankChange.remove(playerId);
            return;
        }
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri butonu
                pendingRankChange.remove(playerId);
                openMenu(player);
                break;
                
            case WOODEN_SWORD:
                // RECRUIT
                changeMemberRank(player, clan, targetMemberId, Clan.Rank.RECRUIT);
                break;
                
            case IRON_SWORD:
                // MEMBER
                changeMemberRank(player, clan, targetMemberId, Clan.Rank.MEMBER);
                break;
                
            case DIAMOND_SWORD:
                // GENERAL
                changeMemberRank(player, clan, targetMemberId, Clan.Rank.GENERAL);
                break;
                
            case NETHERITE_SWORD:
                // ELITE
                changeMemberRank(player, clan, targetMemberId, Clan.Rank.ELITE);
                break;
        }
    }
    
    /**
     * Üye çıkarma onay menüsü
     */
    private void openRemoveConfirmationMenu(Player player, UUID targetMemberId) {
        pendingMemberRemove.put(player.getUniqueId(), targetMemberId);
        
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetMemberId);
        String targetName = targetPlayer.getName();
        if (targetName == null) targetName = "Bilinmeyen";
        
        // 27 slotlu menü
        Inventory menu = Bukkit.createInventory(null, 27, "§cÜye Çıkar: " + targetName);
        
        // Onay butonu (Slot 11 - Kırmızı)
        ItemStack confirmButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§c§lONAYLA");
            confirmMeta.setLore(Arrays.asList(
                "§7═══════════════════════",
                "§c" + targetName + " klanından çıkarılacak!",
                "§cBu işlem geri alınamaz!",
                "§7═══════════════════════"
            ));
            confirmButton.setItemMeta(confirmMeta);
        }
        menu.setItem(11, confirmButton);
        
        // İptal butonu (Slot 15 - Yeşil)
        ItemStack cancelButton = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§a§lİPTAL");
            cancelMeta.setLore(Arrays.asList("§7İşlemi iptal et"));
            cancelButton.setItemMeta(cancelMeta);
        }
        menu.setItem(15, cancelButton);
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Üye çıkarma onay menüsü tıklama
     */
    @EventHandler
    public void onRemoveConfirmationClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("§cÜye Çıkar: ")) return;
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        UUID playerId = player.getUniqueId();
        UUID targetMemberId = pendingMemberRemove.get(playerId);
        
        if (targetMemberId == null) {
            player.closeInventory();
            return;
        }
        
        Clan clan = clanManager.getClanByPlayer(playerId);
        if (clan == null) {
            player.sendMessage("§cBir klana üye değilsin!");
            player.closeInventory();
            pendingMemberRemove.remove(playerId);
            return;
        }
        
        switch (clicked.getType()) {
            case RED_CONCRETE:
                // Onayla
                removeMember(player, clan, targetMemberId);
                pendingMemberRemove.remove(playerId);
                player.closeInventory();
                // Menüyü yeniden aç
                Bukkit.getScheduler().runTaskLater(plugin, () -> openMenu(player), 5L);
                break;
                
            case GREEN_CONCRETE:
                // İptal
                pendingMemberRemove.remove(playerId);
                openMenu(player);
                break;
        }
    }
    
    /**
     * Rütbe değiştir
     */
    private void changeMemberRank(Player player, Clan clan, UUID targetMemberId, Clan.Rank newRank) {
        if (clan == null || targetMemberId == null || newRank == null) return;
        
        // Yetki kontrolü
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        if (playerRank != Clan.Rank.LEADER && playerRank != Clan.Rank.GENERAL) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return;
        }
        
        // Rütbe değiştir
        clan.setRank(targetMemberId, newRank);
        
        // Oyuncuya bildir
        Player targetPlayer = Bukkit.getPlayer(targetMemberId);
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§aRütbeniz değiştirildi: §e" + getRankName(newRank));
        }
        
        player.sendMessage("§a" + Bukkit.getOfflinePlayer(targetMemberId).getName() + 
            " adlı üyenin rütbesi §e" + getRankName(newRank) + "§a olarak değiştirildi!");
        
        // Geçici depolamayı temizle
        pendingRankChange.remove(player.getUniqueId());
        
        // Menüyü yeniden aç
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> openMenu(player), 5L);
    }
    
    /**
     * Üye çıkar
     */
    private void removeMember(Player player, Clan clan, UUID targetMemberId) {
        if (clan == null || targetMemberId == null) return;
        
        // Yetki kontrolü
        Clan.Rank playerRank = clan.getRank(player.getUniqueId());
        if (playerRank != Clan.Rank.LEADER && playerRank != Clan.Rank.GENERAL) {
            player.sendMessage("§cBu işlem için yetkiniz yok!");
            return;
        }
        
        // Üye çıkar
        clanManager.removeMember(clan, targetMemberId);
        
        // Oyuncuya bildir
        Player targetPlayer = Bukkit.getPlayer(targetMemberId);
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§c" + clan.getName() + " klanından çıkarıldınız!");
        }
        
        player.sendMessage("§a" + Bukkit.getOfflinePlayer(targetMemberId).getName() + 
            " adlı üye klanından çıkarıldı!");
        
        // Klan üyelerine bildir
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage("§c" + Bukkit.getOfflinePlayer(targetMemberId).getName() + 
                    " klanından çıkarıldı!");
            }
        }
    }
    
    /**
     * Item'dan üye UUID'sini al (NBT'den veya item meta'dan isim ile)
     */
    private UUID getMemberIdFromItem(ItemStack item, Clan clan) {
        if (item == null || clan == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        // Önce NBT'den dene
        org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "member_uuid");
        if (meta.getPersistentDataContainer().has(uuidKey, org.bukkit.persistence.PersistentDataType.STRING)) {
            String uuidStr = meta.getPersistentDataContainer().get(uuidKey, org.bukkit.persistence.PersistentDataType.STRING);
            if (uuidStr != null) {
                try {
                    return UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    // UUID formatı geçersiz, isim ile devam et
                }
            }
        }
        
        // NBT yoksa isim ile ara
        if (!meta.hasDisplayName()) return null;
        
        String displayName = meta.getDisplayName();
        // Renk kodlarını temizle
        displayName = displayName.replaceAll("§[0-9a-fk-or]", "");
        displayName = displayName.replace("§l", "");
        displayName = displayName.trim();
        
        // Klan üyeleri arasında ara
        for (Map.Entry<UUID, Clan.Rank> entry : clan.getMembers().entrySet()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String memberName = offlinePlayer.getName();
            if (memberName != null && memberName.equals(displayName)) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    /**
     * Slot numarasından üye UUID'sini al (alternatif yöntem)
     */
    private UUID getMemberIdFromSlot(int slot, Clan clan) {
        if (clan == null) return null;
        
        // Slot 9-44 arası üye slotları
        if (slot < 9 || slot > 44) return null;
        
        // Üye listesini sırala (aynı sıralama)
        List<Map.Entry<UUID, Clan.Rank>> sortedMembers = new ArrayList<>(clan.getMembers().entrySet());
        sortedMembers.sort((a, b) -> {
            if (a.getValue() == Clan.Rank.LEADER) return -1;
            if (b.getValue() == Clan.Rank.LEADER) return 1;
            if (a.getValue().isAtLeast(b.getValue()) && !b.getValue().isAtLeast(a.getValue())) {
                return -1;
            } else if (b.getValue().isAtLeast(a.getValue()) && !a.getValue().isAtLeast(b.getValue())) {
                return 1;
            } else {
                return 0;
            }
        });
        
        // Slot index'i hesapla (slot 9 = index 0)
        int index = slot - 9;
        if (index >= 0 && index < sortedMembers.size()) {
            return sortedMembers.get(index).getKey();
        }
        
        return null;
    }
}

