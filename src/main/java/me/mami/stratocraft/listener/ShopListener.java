package me.mami.stratocraft.listener;

import me.mami.stratocraft.gui.ShopMenu;
import me.mami.stratocraft.manager.ShopManager;
import me.mami.stratocraft.model.Shop;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class ShopListener implements Listener {
    private final ShopManager shopManager;
    
    // Teklif verme için geçici veri saklama
    private final java.util.Map<java.util.UUID, ItemStack> pendingOfferItems = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, Integer> pendingOfferAmounts = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, Shop> pendingOfferShops = new java.util.HashMap<>();

    public ShopListener(ShopManager sm) { 
        this.shopManager = sm;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onShopInteract(PlayerInteractEvent event) {
        // GUI MENÜ İLE MARKET ETKİLEŞİMİ
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) {
            Shop shop = shopManager.getShop(event.getClickedBlock().getLocation());
            if (shop != null) {
                event.setCancelled(true); // Sandığı açma, GUI menü aç
                Player player = event.getPlayer();
                player.openInventory(ShopMenu.createShopMenu(shop));
                return;
            }
        }
    }

    // YENİ: MARKET KURULUMU (TABELA İLE)
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // Tabela formatı:
        // Satır 1: [MARKET]
        // (Diğer satırlar otomatik dolacak veya dekoratif kalacak)
        
        if (event.getLine(0).equalsIgnoreCase("[MARKET]")) {
            org.bukkit.block.Block block = event.getBlock();
            // Tabelanın altındaki blok Sandık mı?
            org.bukkit.block.Block attached = block.getRelative(org.bukkit.block.BlockFace.DOWN); // Basitçe altı kontrol edelim
            
            if (attached.getType() == Material.CHEST) {
                Chest chest = (Chest) attached.getState();
                
                // Market kurmak için sandığın içinde:
                // 1. Slot (0): Satılacak Eşya
                // 2. Slot (1): İstenen Ücret Eşyası (Örn: Altın)
                ItemStack sellItem = chest.getInventory().getItem(0);
                ItemStack priceItem = chest.getInventory().getItem(1);

                if (sellItem == null || priceItem == null) {
                    event.getPlayer().sendMessage("§cMarket kurmak için sandığın ilk slotuna SATILACAK, ikinci slotuna ÜCRET eşyasını koymalısın.");
                    event.setLine(0, "§cHATA");
                    return;
                }

                // Market Oluştur
                // protectedZone parametresi basitlik için false, klan bölgesi kontrolü eklenebilir.
                shopManager.createShop(event.getPlayer(), attached.getLocation(), sellItem, priceItem, true);
                
                // HAVADA DÖNEN EŞYA OLUŞTUR
                org.bukkit.Location displayLoc = attached.getLocation().add(0.5, 1.5, 0.5);
                org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) 
                    displayLoc.getWorld().spawnEntity(displayLoc, org.bukkit.entity.EntityType.ARMOR_STAND);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setInvulnerable(true);
                stand.setSmall(true);
                stand.setCustomNameVisible(false);
                stand.setMarker(true);
                stand.getEquipment().setHelmet(sellItem.clone());
                
                // Tabelayı Güncelle
                event.setLine(0, "§a[MARKET]");
                event.setLine(1, sellItem.getAmount() + "x " + sellItem.getType().toString());
                event.setLine(2, "Fiyat:");
                event.setLine(3, priceItem.getAmount() + "x " + priceItem.getType().toString());
                
                event.getPlayer().sendMessage("§aMarket başarıyla kuruldu!");
            }
        }
    }
    
    // ========== GUI MENÜ İŞLEMLERİ ==========
    
    /**
     * Mağaza menüsü tıklama işlemleri
     */
    @EventHandler
    public void onShopMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Mağaza menüsü
        if (title.equals("§aMarket")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            // Shop bilgisini al (slot 11'deki satılan item'dan)
            ItemStack sellingItem = event.getInventory().getItem(11);
            if (sellingItem == null) return;
            
            // Shop'u bul (tüm shopları tara)
            Shop shop = null;
            for (Shop s : shopManager.getAllShops()) {
                if (s.getSellingItem().isSimilar(sellingItem)) {
                    shop = s;
                    break;
                }
            }
            if (shop == null) return;
            
            // Buton kontrolü
            if (clicked.getType() == Material.EMERALD_BLOCK && clicked.getItemMeta().getDisplayName().equals("§a[Satın Al]")) {
                // Klasik satın alma
                player.closeInventory();
                shopManager.handlePurchase(player, shop);
            } else if (clicked.getType() == Material.GOLD_BLOCK && clicked.getItemMeta().getDisplayName().equals("§e[Teklif Ver]")) {
                // Teklif verme menüsüne git
                pendingOfferShops.put(player.getUniqueId(), shop);
                player.openInventory(ShopMenu.createOfferMenu(shop));
            } else if (clicked.getType() == Material.PAPER && clicked.getItemMeta().getDisplayName().startsWith("§eTeklifler")) {
                // Sadece mağaza sahibi görebilir
                if (shop.getOwnerId().equals(player.getUniqueId())) {
                    player.openInventory(ShopMenu.createOffersMenu(shop));
                } else {
                    player.sendMessage("§cBu mağaza sana ait değil!");
                }
            } else if (clicked.getType() == Material.BARRIER && clicked.getItemMeta().getDisplayName().equals("§cKapat")) {
                player.closeInventory();
            }
        }
        
        // Teklif verme menüsü
        else if (title.equals("§eTeklif Ver")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            Shop shop = pendingOfferShops.get(player.getUniqueId());
            if (shop == null) return;
            
            if (clicked.getType() == Material.CHEST && clicked.getItemMeta().getDisplayName().equals("§eTeklif Item'ı Seç")) {
                // Oyuncunun elindeki item'ı kontrol et
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem != null && handItem.getType() != Material.AIR) {
                    ItemStack offerItem = handItem.clone();
                    offerItem.setAmount(1);
                    pendingOfferItems.put(player.getUniqueId(), offerItem);
                    pendingOfferAmounts.putIfAbsent(player.getUniqueId(), 1);
                    player.sendMessage("§aItem seçildi: §e" + offerItem.getType().name());
                    player.sendMessage("§7Miktar: §e" + pendingOfferAmounts.get(player.getUniqueId()));
                } else {
                    player.sendMessage("§cTeklif etmek için elinde bir item tutmalısın!");
                }
            } else if (clicked.getType() == Material.REDSTONE && clicked.getItemMeta().getDisplayName().equals("§c-1")) {
                // Miktar azalt
                int current = pendingOfferAmounts.getOrDefault(player.getUniqueId(), 1);
                if (current > 1) {
                    pendingOfferAmounts.put(player.getUniqueId(), current - 1);
                    player.sendMessage("§7Miktar: §e" + (current - 1));
                }
            } else if (clicked.getType() == Material.EMERALD && clicked.getItemMeta().getDisplayName().equals("§a+1")) {
                // Miktar artır
                int current = pendingOfferAmounts.getOrDefault(player.getUniqueId(), 1);
                pendingOfferAmounts.put(player.getUniqueId(), current + 1);
                player.sendMessage("§7Miktar: §e" + (current + 1));
            } else if (clicked.getType() == Material.EMERALD_BLOCK && clicked.getItemMeta().getDisplayName().equals("§a[Teklif Gönder]")) {
                // Teklif gönder
                ItemStack offerItem = pendingOfferItems.get(player.getUniqueId());
                int offerAmount = pendingOfferAmounts.getOrDefault(player.getUniqueId(), 1);
                
                if (offerItem == null) {
                    // Oyuncunun elindeki item'ı kontrol et
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem != null && handItem.getType() != Material.AIR) {
                        offerItem = handItem.clone();
                        offerItem.setAmount(1); // Sadece tip için
                    } else {
                        player.sendMessage("§cTeklif etmek için elinde bir item tutmalısın!");
                        return;
                    }
                }
                
                if (offerAmount <= 0) {
                    player.sendMessage("§cMiktar 0'dan büyük olmalı!");
                    return;
                }
                
                // Teklif gönder
                shopManager.sendOffer(player, shop, offerItem, offerAmount);
                player.closeInventory();
                
                // Temizle
                pendingOfferItems.remove(player.getUniqueId());
                pendingOfferAmounts.remove(player.getUniqueId());
                pendingOfferShops.remove(player.getUniqueId());
            } else if (clicked.getType() == Material.ARROW && clicked.getItemMeta().getDisplayName().equals("§eGeri")) {
                // Ana menüye geri dön
                player.openInventory(ShopMenu.createShopMenu(shop));
            }
        }
        
        // Teklifler listesi menüsü
        else if (title.equals("§eGelen Teklifler")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            
            // Shop'u bul
            Shop shop = null;
            for (Shop s : shopManager.getAllShops()) {
                if (s.getOwnerId().equals(player.getUniqueId())) {
                    shop = s;
                    break;
                }
            }
            if (shop == null || !shop.getOwnerId().equals(player.getUniqueId())) {
                player.sendMessage("§cBu mağaza sana ait değil!");
                return;
            }
            
            // Tıklanan teklifi bul
            int slot = event.getSlot();
            if (slot < shop.getOffers().size()) {
                Shop.Offer offer = shop.getOffers().get(slot);
                
                // Lore'dan kontrol et (Kabul Et / Reddet)
                if (clicked.getItemMeta() != null && clicked.getItemMeta().getLore() != null) {
                    List<String> lore = clicked.getItemMeta().getLore();
                    if (lore.contains("§a[Kabul Et]")) {
                        // Teklif kabul et
                        shopManager.acceptOffer(player, shop, offer);
                        player.openInventory(ShopMenu.createOffersMenu(shop)); // Menüyü yenile
                    } else if (lore.contains("§c[Reddet]")) {
                        // Teklif reddet
                        shopManager.rejectOffer(player, shop, offer);
                        player.openInventory(ShopMenu.createOffersMenu(shop)); // Menüyü yenile
                    }
                }
            }
            
            if (clicked.getType() == Material.BARRIER && clicked.getItemMeta().getDisplayName().equals("§cKapat")) {
                player.closeInventory();
            }
        }
    }
    
    /**
     * Menü kapanınca temizle
     */
    @EventHandler
    public void onShopMenuClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String title = event.getView().getTitle();
            
            if (title.equals("§eTeklif Ver")) {
                // Teklif verme menüsü kapanınca temizle
                pendingOfferItems.remove(player.getUniqueId());
                pendingOfferAmounts.remove(player.getUniqueId());
                pendingOfferShops.remove(player.getUniqueId());
            }
        }
    }
    
    /**
     * Oyuncu item'ı eline aldığında teklif verme menüsünü güncelle
     */
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (pendingOfferShops.containsKey(player.getUniqueId())) {
            // Teklif verme menüsü açık, item'ı güncelle
            ItemStack handItem = player.getInventory().getItem(event.getNewSlot());
            if (handItem != null && handItem.getType() != Material.AIR) {
                ItemStack offerItem = handItem.clone();
                offerItem.setAmount(1);
                pendingOfferItems.put(player.getUniqueId(), offerItem);
                pendingOfferAmounts.putIfAbsent(player.getUniqueId(), 1);
            }
        }
    }
}

