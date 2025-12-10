package me.mami.stratocraft.gui;

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.ContractManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ContractMenu - Kontrat Sistemi GUI Menüsü
 * 
 * Özellikler:
 * - Kontrat listesi görüntüleme (sayfalama)
 * - Kontrat detayları görüntüleme
 * - Kontrat kabul etme/reddetme
 * - Kontrat oluşturma wizard (çok adımlı)
 * - Tüm kontrat tiplerini destekleme
 * - Thread-safe operations
 */
public class ContractMenu implements Listener {
    private final Main plugin;
    private final ContractManager contractManager;
    private final ClanManager clanManager;
    
    // Kontrat oluşturma wizard'ı için geçici depolama
    private final Map<UUID, ContractWizardState> wizardStates = new ConcurrentHashMap<>();
    
    // Kontrat detay menüsü için geçici depolama (player -> contract ID)
    private final Map<UUID, UUID> viewingContract = new ConcurrentHashMap<>();
    
    // Sayfa numaraları (player -> page)
    private final Map<UUID, Integer> currentPages = new ConcurrentHashMap<>();
    
    /**
     * Kontrat oluşturma wizard durumu
     */
    private static class ContractWizardState {
        Contract.ContractType type;
        Contract.ContractScope scope;
        double reward;
        double penalty;
        long deadlineDays;
        Material material;
        int amount;
        UUID targetPlayer;
        List<org.bukkit.Location> restrictedAreas;
        int restrictedRadius;
        UUID nonAggressionTarget;
        String structureType;
        int step = 0; // Wizard adımı (0 = tip seçimi, 1 = kapsam, 2 = ödül, 3 = ceza, 4 = süre, 5+ = tip'e özel)
    }
    
    public ContractMenu(Main plugin, ContractManager contractManager, ClanManager clanManager) {
        this.plugin = plugin;
        this.contractManager = contractManager;
        this.clanManager = clanManager;
    }
    
    /**
     * Ana kontrat listesi menüsünü aç
     */
    public void openMainMenu(Player player, int page) {
        if (player == null) return;
        
        List<Contract> contracts = contractManager != null ? contractManager.getContracts() : new ArrayList<>();
        if (contracts == null) contracts = new ArrayList<>();
        
        // Sadece açık kontratları göster (acceptor == null)
        List<Contract> openContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract != null && contract.getAcceptor() == null && !contract.isExpired() && !contract.isBreached()) {
                openContracts.add(contract);
            }
        }
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(openContracts.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        // 54 slotlu menü oluştur (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Aktif Kontratlar - Sayfa " + page);
        
        // Kontratları göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, openContracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = openContracts.get(i);
            if (contract != null) {
                ItemStack contractItem = createContractItem(contract);
                // UUID'yi NBT'ye ekle
                ItemMeta meta = contractItem.getItemMeta();
                if (meta != null) {
                    org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "contract_id");
                    meta.getPersistentDataContainer().set(uuidKey, 
                        org.bukkit.persistence.PersistentDataType.STRING, contract.getId().toString());
                    contractItem.setItemMeta(meta);
                }
                menu.setItem(slot, contractItem);
            slot++;
            }
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", 
                Arrays.asList("§7Sayfa " + (page - 1))));
        }
        
        if (endIndex < openContracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Yeni kontrat oluştur butonu (Slot 49)
        menu.setItem(49, createButton(Material.WRITABLE_BOOK, "§aYeni Kontrat Oluştur", 
            Arrays.asList("§7Tıklayarak kontrat oluşturma sihirbazını başlat")));
        
        // Kapat butonu (Slot 48)
        menu.setItem(48, createButton(Material.BARRIER, "§cKapat", null));
        
        // Bilgi butonu (Slot 50)
        menu.setItem(50, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Kontrat: §e" + openContracts.size(),
                "§7Sayfa: §e" + page + "§7/§e" + totalPages)));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Kontrat detay menüsünü aç
     */
    public void openDetailMenu(Player player, UUID contractId) {
        if (player == null || contractId == null) return;
        
        Contract contract = contractManager != null ? contractManager.getContract(contractId) : null;
        if (contract == null) {
            player.sendMessage("§cKontrat bulunamadı!");
            return;
        }
        
        viewingContract.put(player.getUniqueId(), contractId);
        
        // 27 slotlu menü (3x9)
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Detayları");
        
        // Kontrat bilgileri (Slot 13)
        ItemStack infoItem = createContractDetailItem(contract);
        menu.setItem(13, infoItem);
        
        // Kabul Et butonu (Slot 11 - sadece açık kontratlar için)
        if (contract.getAcceptor() == null && !contract.isExpired() && !contract.isBreached()) {
            menu.setItem(11, createButton(Material.EMERALD_BLOCK, "§a[Kabul Et]", 
                Arrays.asList("§7Kan imzası gerekli (1 kalp kaybı)",
                    "§cDikkat: İhlal durumunda ceza uygulanır!")));
        }
        
        // Reddet butonu (Slot 15)
        menu.setItem(15, createButton(Material.REDSTONE_BLOCK, "§c[Reddet]", 
            Arrays.asList("§7Kontratı reddet")));
        
        // Geri butonu (Slot 22)
        menu.setItem(22, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Listeye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kontrat oluşturma wizard'ını başlat
     */
    public void startCreationWizard(Player player) {
        if (player == null) return;
        
        // Klan kontrolü
        Clan clan = clanManager != null ? clanManager.getClanByPlayer(player.getUniqueId()) : null;
        if (clan == null) {
            player.sendMessage("§cKontrat oluşturmak için bir klana üye olmalısınız!");
            return;
        }
        
        // Wizard durumu oluştur
        ContractWizardState state = new ContractWizardState();
        wizardStates.put(player.getUniqueId(), state);
        
        // Tip seçim menüsünü aç
        openTypeSelectionMenu(player);
    }
    
    /**
     * Kontrat tipi seçim menüsü
     */
    private void openTypeSelectionMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Tipi Seç");
        
        // Kontrat tipleri
        Contract.ContractType[] types = {
            Contract.ContractType.MATERIAL_DELIVERY,
            Contract.ContractType.PLAYER_KILL,
            Contract.ContractType.TERRITORY_RESTRICT,
            Contract.ContractType.NON_AGGRESSION,
            Contract.ContractType.BASE_PROTECTION,
            Contract.ContractType.STRUCTURE_BUILD
        };
        
        Material[] icons = {
            Material.CHEST,
            Material.DIAMOND_SWORD,
            Material.BARRIER,
            Material.SHIELD,
            Material.BEACON,
            Material.STRUCTURE_BLOCK
        };
        
        int[] slots = {10, 11, 12, 13, 14, 15};
        
        for (int i = 0; i < types.length; i++) {
            menu.setItem(slots[i], createTypeButton(types[i], icons[i]));
        }
        
        // Geri butonu (Slot 0)
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", null));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kontrat tipi butonu oluştur
     */
    private ItemStack createTypeButton(Contract.ContractType type, Material icon) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + getContractTypeName(type));
            meta.setLore(Arrays.asList(
                "§7═══════════════════════",
                "§7" + getContractTypeDescription(type),
                "§7═══════════════════════"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Kontrat tipi açıklaması
     */
    private String getContractTypeDescription(Contract.ContractType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case MATERIAL_DELIVERY:
                return "Belirli bir malzemenin teslim edilmesi";
            case PLAYER_KILL:
                return "Belirli bir oyuncunun öldürülmesi";
            case TERRITORY_RESTRICT:
                return "Belirli bölgelerde yasaklama";
            case NON_AGGRESSION:
                return "Saldırmama anlaşması";
            case BASE_PROTECTION:
                return "Base koruma sözleşmesi";
            case STRUCTURE_BUILD:
                return "Belirli bir yapının inşa edilmesi";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Kontrat item'ı oluştur
     */
    private ItemStack createContractItem(Contract contract) {
        if (contract == null) return new ItemStack(Material.BARRIER);
        
        Material icon = getContractIcon(contract.getType());
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§e" + getContractTypeName(contract.getType()));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        lore.add("§7Ceza: §c" + contract.getPenalty() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        
        // Tip'e özel bilgiler
        switch (contract.getType()) {
            case MATERIAL_DELIVERY:
                if (contract.getMaterial() != null) {
                    lore.add("§7Malzeme: §e" + contract.getMaterial().name());
                    lore.add("§7Miktar: §e" + contract.getAmount());
                }
                break;
            case PLAYER_KILL:
                if (contract.getTargetPlayer() != null) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getTargetPlayer());
                    lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                }
                break;
        }
        
        lore.add("§7═══════════════════════");
        lore.add("§eSol Tık: §7Detayları Görüntüle");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Kontrat detay item'ı oluştur
     */
    private ItemStack createContractDetailItem(Contract contract) {
        if (contract == null) return new ItemStack(Material.BARRIER);
        
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§e" + getContractTypeName(contract.getType()));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // İssuer bilgisi
        OfflinePlayer issuer = Bukkit.getOfflinePlayer(contract.getIssuer());
        lore.add("§7Veren: §e" + (issuer.getName() != null ? issuer.getName() : "Bilinmeyen"));
        
        // Kapsam bilgisi
        lore.add("§7Kapsam: §e" + getContractScopeName(contract.getScope()));
        
        lore.add("§7═══════════════════════");
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        lore.add("§7Ceza: §c" + contract.getPenalty() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        
        // Tip'e göre özel bilgiler
        switch (contract.getType()) {
            case MATERIAL_DELIVERY:
                if (contract.getMaterial() != null) {
                    lore.add("§7═══════════════════════");
                    lore.add("§7Malzeme: §e" + contract.getMaterial().name());
                    lore.add("§7Miktar: §e" + contract.getAmount());
                    lore.add("§7Teslim: §a" + contract.getDelivered() + "§7/§a" + contract.getAmount());
                }
                break;
            case PLAYER_KILL:
                if (contract.getTargetPlayer() != null) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getTargetPlayer());
                    lore.add("§7═══════════════════════");
                    lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                }
                break;
            case TERRITORY_RESTRICT:
                lore.add("§7═══════════════════════");
                lore.add("§7Yasak Bölgeler: §c" + (contract.getRestrictedAreas() != null ? contract.getRestrictedAreas().size() : 0) + " adet");
                lore.add("§7Yarıçap: §e" + contract.getRestrictedRadius() + " blok");
                break;
            case NON_AGGRESSION:
                if (contract.getNonAggressionTarget() != null) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getNonAggressionTarget());
                    lore.add("§7═══════════════════════");
                    lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                }
                break;
            case STRUCTURE_BUILD:
                if (contract.getStructureType() != null) {
                    lore.add("§7═══════════════════════");
                    lore.add("§7Yapı: §e" + contract.getStructureType());
                }
                break;
            case BASE_PROTECTION:
                lore.add("§7═══════════════════════");
                lore.add("§7Base koruma sözleşmesi aktif");
                break;
        }
        
        // Durum bilgisi
        lore.add("§7═══════════════════════");
        if (contract.getAcceptor() != null) {
            OfflinePlayer acceptor = Bukkit.getOfflinePlayer(contract.getAcceptor());
            lore.add("§7Kabul Eden: §e" + (acceptor.getName() != null ? acceptor.getName() : "Bilinmeyen"));
        } else {
            lore.add("§7Durum: §aAçık");
        }
        
        if (contract.isBreached()) {
            lore.add("§c§lİHLAL EDİLDİ!");
        } else if (contract.isExpired()) {
            lore.add("§c§lSÜRE DOLDU!");
        } else if (contract.isCompleted()) {
            lore.add("§a§lTAMAMLANDI!");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Kontrat tipi ikonu
     */
    private Material getContractIcon(Contract.ContractType type) {
        if (type == null) return Material.PAPER;
        switch (type) {
            case PLAYER_KILL:
                return Material.DIAMOND_SWORD;
            case TERRITORY_RESTRICT:
                return Material.BARRIER;
            case NON_AGGRESSION:
                return Material.SHIELD;
            case BASE_PROTECTION:
                return Material.BEACON;
            case MATERIAL_DELIVERY:
                return Material.CHEST;
            case STRUCTURE_BUILD:
                return Material.STRUCTURE_BLOCK;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Kontrat tipi ismi
     */
    private String getContractTypeName(Contract.ContractType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case MATERIAL_DELIVERY:
                return "Malzeme Temini";
            case PLAYER_KILL:
                return "Oyuncu Avı";
            case TERRITORY_RESTRICT:
                return "Bölge Yasağı";
            case NON_AGGRESSION:
                return "Saldırmama Anlaşması";
            case BASE_PROTECTION:
                return "Base Koruma";
            case STRUCTURE_BUILD:
                return "Yapı İnşa";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Kontrat kapsamı ismi
     */
    private String getContractScopeName(Contract.ContractScope scope) {
        if (scope == null) return "Bilinmeyen";
        switch (scope) {
            case PLAYER_TO_PLAYER:
                return "Oyuncu → Oyuncu";
            case CLAN_TO_CLAN:
                return "Klan → Klan";
            case PLAYER_TO_CLAN:
                return "Oyuncu → Klan";
            case CLAN_TO_PLAYER:
                return "Klan → Oyuncu";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Yardımcı metod: Buton oluştur
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
    
    /**
     * Zaman formatla
     */
    private String formatTime(long deadline) {
        long totalSeconds = (deadline - System.currentTimeMillis()) / 1000;
        if (totalSeconds <= 0) return "§cSüre Doldu";
        
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (days > 0) {
            return days + "g " + hours + "s " + minutes + "d";
        } else if (hours > 0) {
            return hours + "s " + minutes + "d " + seconds + "sn";
        } else if (minutes > 0) {
            return minutes + "d " + seconds + "sn";
        } else {
            return seconds + "sn";
        }
    }
    
    /**
     * Item'dan kontrat UUID'sini al
     */
    private UUID getContractIdFromItem(ItemStack item) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "contract_id");
        if (meta.getPersistentDataContainer().has(uuidKey, org.bukkit.persistence.PersistentDataType.STRING)) {
            String uuidStr = meta.getPersistentDataContainer().get(uuidKey, org.bukkit.persistence.PersistentDataType.STRING);
            if (uuidStr != null) {
                try {
                    return UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
    
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // Ana kontrat listesi
        if (title.startsWith("§6Aktif Kontratlar")) {
            handleMainMenuClick(event);
        }
        // Kontrat detay menüsü
        else if (title.equals("§6Kontrat Detayları")) {
            handleDetailMenuClick(event);
        }
        // Kontrat tipi seçim menüsü
        else if (title.equals("§6Kontrat Tipi Seç")) {
            handleTypeSelectionClick(event);
        }
    }
    
    /**
     * Ana menü tıklama
     */
    private void handleMainMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (clicked.getType()) {
            case ARROW:
                // Sayfalama
                int currentPage = currentPages.getOrDefault(player.getUniqueId(), 1);
                if (event.getSlot() == 45) {
                    // Önceki sayfa
                    openMainMenu(player, currentPage - 1);
                } else if (event.getSlot() == 53) {
                    // Sonraki sayfa
                    openMainMenu(player, currentPage + 1);
                }
                break;
                
            case WRITABLE_BOOK:
                // Yeni kontrat oluştur
                startCreationWizard(player);
                break;
                
            case BARRIER:
                // Kapat
                player.closeInventory();
                break;
                
            default:
                // Kontrat item'ı
                UUID contractId = getContractIdFromItem(clicked);
                if (contractId != null) {
                    openDetailMenu(player, contractId);
                }
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Detay menüsü tıklama
     */
    private void handleDetailMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        UUID contractId = viewingContract.get(player.getUniqueId());
        if (contractId == null) {
            player.sendMessage("§cKontrat bulunamadı!");
            player.closeInventory();
            return;
        }
        
        Contract contract = contractManager != null ? contractManager.getContract(contractId) : null;
        if (contract == null) {
            player.sendMessage("§cKontrat bulunamadı!");
            player.closeInventory();
            viewingContract.remove(player.getUniqueId());
            return;
        }
        
        switch (clicked.getType()) {
            case EMERALD_BLOCK:
                // Kabul et
                if (contract.getAcceptor() == null && !contract.isExpired() && !contract.isBreached()) {
                    // Kan imzası: 1 kalp kaybı
                    org.bukkit.attribute.AttributeInstance maxHealthAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealthAttr != null) {
                        double currentMax = maxHealthAttr.getBaseValue();
                        double newMax = Math.max(1.0, currentMax - 2.0); // 1 kalp = 2 can
                        maxHealthAttr.setBaseValue(newMax);
                        if (player.getHealth() > newMax) {
                            player.setHealth(newMax);
                        }
                    }
                    
                    // Kontratı kabul et
                    if (contractManager != null) {
                        contractManager.acceptContract(contractId, player.getUniqueId());
                    }
                    
                    player.sendMessage("§aKontrat kabul edildi! §c1 kalp kaybettiniz (kan imzası).");
                    player.sendMessage("§7Kontratı tamamladığınızda kalp geri verilecek.");
                    
                    player.closeInventory();
                    viewingContract.remove(player.getUniqueId());
                } else {
                    player.sendMessage("§cBu kontrat kabul edilemez!");
                }
                break;
                
            case REDSTONE_BLOCK:
                // Reddet
                player.closeInventory();
                viewingContract.remove(player.getUniqueId());
                int page = currentPages.getOrDefault(player.getUniqueId(), 1);
                openMainMenu(player, page);
                break;
                
            case ARROW:
                // Geri
                viewingContract.remove(player.getUniqueId());
                int currentPage = currentPages.getOrDefault(player.getUniqueId(), 1);
                openMainMenu(player, currentPage);
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Tip seçim menüsü tıklama
     */
    private void handleTypeSelectionClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                wizardStates.remove(player.getUniqueId());
                int page = currentPages.getOrDefault(player.getUniqueId(), 1);
                openMainMenu(player, page);
                break;
                
            case CHEST:
                state.type = Contract.ContractType.MATERIAL_DELIVERY;
                player.sendMessage("§eMalzeme Temini kontratı seçildi. Kontrat oluşturma wizard'ı yakında eklenecek.");
                player.closeInventory();
                wizardStates.remove(player.getUniqueId());
                // TODO: Wizard devamı (kapsam, ödül, ceza, süre, malzeme, miktar)
                break;
                
            case DIAMOND_SWORD:
                state.type = Contract.ContractType.PLAYER_KILL;
                player.sendMessage("§eOyuncu Avı kontratı seçildi. Kontrat oluşturma wizard'ı yakında eklenecek.");
                player.closeInventory();
                wizardStates.remove(player.getUniqueId());
                // TODO: Wizard devamı
                break;
                
            default:
                // Diğer tipler için benzer işlemler
                player.sendMessage("§cBu kontrat tipi henüz desteklenmiyor!");
                break;
        }
    }
}
