package me.mami.stratocraft.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

import me.mami.stratocraft.Main;
import me.mami.stratocraft.manager.ClanManager;
import me.mami.stratocraft.manager.ContractManager;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;

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
    
    // Kontrat şablonları (oyuncu başına)
    private final Map<UUID, List<ContractTemplate>> playerTemplates = new ConcurrentHashMap<>();
    
    // Kontrat geçmişi (oyuncu başına)
    private final Map<UUID, List<Contract>> contractHistory = new ConcurrentHashMap<>();
    
    /**
     * Kontrat oluşturma wizard durumu
     */
    private static class ContractWizardState {
        me.mami.stratocraft.enums.ContractType contractType; // Yeni enum
        me.mami.stratocraft.enums.PenaltyType penaltyType; // Yeni enum
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
        int step = 0; // Wizard adımı (0 = kategori seçimi, 1 = kapsam, 2 = ödül, 3 = ceza tipi, 4 = ceza miktarı, 5 = süre, 6+ = tip'e özel)
        String waitingForInput = null; // Chat input bekleniyor mu? ("reward", "penalty", "days", "amount", "player", "location", "structure")
        Integer materialPage = 0; // Material seçim sayfası
        ContractTemplate selectedTemplate = null; // Seçilen şablon
    }
    
    /**
     * Kontrat şablonu
     */
    private static class ContractTemplate {
        String name;
        me.mami.stratocraft.enums.ContractType contractType; // Yeni enum
        me.mami.stratocraft.enums.PenaltyType penaltyType; // Yeni enum
        Contract.ContractScope scope;
        double reward;
        double penalty;
        long deadlineDays;
        Material material;
        int amount;
        UUID targetPlayer;
        String structureType;
        
        ContractTemplate(String name) {
            this.name = name;
        }
    }
    
    public ContractMenu(Main plugin, ContractManager contractManager, ClanManager clanManager) {
        this.plugin = plugin;
        this.contractManager = contractManager;
        this.clanManager = clanManager;
    }
    
    /**
     * Kontrat geçmişine ekle (ContractManager'dan çağrılır)
     */
    public void addContractToHistory(Contract contract) {
        if (contract == null) return;
        
        // İssuer ve acceptor için geçmişe ekle
        if (contract.getIssuer() != null) {
            List<Contract> issuerHistory = contractHistory.getOrDefault(contract.getIssuer(), new ArrayList<>());
            issuerHistory.add(contract);
            if (issuerHistory.size() > 50) {
                issuerHistory.remove(0); // Son 50 kontratı sakla
            }
            contractHistory.put(contract.getIssuer(), issuerHistory);
        }
        
        if (contract.getAcceptor() != null) {
            List<Contract> acceptorHistory = contractHistory.getOrDefault(contract.getAcceptor(), new ArrayList<>());
            acceptorHistory.add(contract);
            if (acceptorHistory.size() > 50) {
                acceptorHistory.remove(0); // Son 50 kontratı sakla
            }
            contractHistory.put(contract.getAcceptor(), acceptorHistory);
        }
    }
    
    /**
     * Ana kontrat listesi menüsünü aç
     */
    public void openMainMenu(Player player, int page) {
        if (player == null) return;
        
        // Manager null kontrolleri
        if (contractManager == null) {
            player.sendMessage("§cKontrat sistemi aktif değil!");
            plugin.getLogger().warning("ContractManager null! Menü açılamıyor.");
            return;
        }
        
        List<Contract> contracts = contractManager.getContracts();
        if (contracts == null) {
            plugin.getLogger().warning("ContractManager.getContracts() null döndü!");
            contracts = new ArrayList<>();
        }
        
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
        
        // Şablonlardan oluştur (Slot 47)
        List<ContractTemplate> templates = playerTemplates.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (!templates.isEmpty()) {
            menu.setItem(47, createButton(Material.BOOK, "§eŞablonlardan Oluştur", 
                Arrays.asList("§7Kayıtlı şablonları kullan", "§7Şablon sayısı: §e" + templates.size())));
        }
        
        // Kapat butonu (Slot 48)
        menu.setItem(48, createButton(Material.BARRIER, "§cKapat", null));
        
        // Kontrat geçmişi (Slot 51)
        List<Contract> history = contractHistory.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (!history.isEmpty()) {
            menu.setItem(51, createButton(Material.PAPER, "§6Kontrat Geçmişi", 
                Arrays.asList("§7Geçmiş kontratlarınızı görüntüle", "§7Toplam: §e" + history.size())));
        }
        
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
     * Kontrat kategori seçim menüsü (YENİ: ContractType enum kullanır)
     */
    private void openTypeSelectionMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Kategorisi Seç");
        
        // Yeni kontrat kategorileri
        me.mami.stratocraft.enums.ContractType[] types = {
            me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION,
            me.mami.stratocraft.enums.ContractType.CONSTRUCTION,
            me.mami.stratocraft.enums.ContractType.COMBAT,
            me.mami.stratocraft.enums.ContractType.TERRITORY
        };
        
        Material[] icons = {
            Material.CHEST,           // RESOURCE_COLLECTION
            Material.STRUCTURE_BLOCK,  // CONSTRUCTION
            Material.DIAMOND_SWORD,    // COMBAT
            Material.BARRIER           // TERRITORY
        };
        
        int[] slots = {10, 12, 14, 16};
        
        for (int i = 0; i < types.length; i++) {
            menu.setItem(slots[i], createTypeButton(types[i], icons[i]));
        }
        
        // Geri butonu (Slot 0)
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", null));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kontrat kategori butonu oluştur (YENİ: ContractType enum kullanır)
     */
    private ItemStack createTypeButton(me.mami.stratocraft.enums.ContractType type, Material icon) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + getContractTypeName(type));
            meta.setLore(Arrays.asList(
                "§7═══════════════════════",
                "§7" + getContractTypeDescription(type),
                "§7═══════════════════════",
                "§aSol tıkla seç"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Kontrat kategori açıklaması (YENİ: ContractType enum kullanır)
     */
    private String getContractTypeDescription(me.mami.stratocraft.enums.ContractType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case RESOURCE_COLLECTION:
                return "Kaynak toplama kontratları";
            case CONSTRUCTION:
                return "İnşaat kontratları";
            case COMBAT:
                return "Savaş kontratları (Öldürme, vurma)";
            case TERRITORY:
                return "Bölge kontratları (Gitme, gitmeme)";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Kontrat kategori ismi (YENİ: ContractType enum kullanır)
     */
    private String getContractTypeName(me.mami.stratocraft.enums.ContractType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case RESOURCE_COLLECTION:
                return "Kaynak Toplama";
            case CONSTRUCTION:
                return "İnşaat";
            case COMBAT:
                return "Savaş";
            case TERRITORY:
                return "Bölge";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Eski Contract.ContractType için geriye uyumluluk (deprecated)
     */
    @Deprecated
    private String getContractTypeName(Contract.ContractType type) {
        if (type == null) return "Bilinmeyen";
        switch (type) {
            case MATERIAL_DELIVERY:
                return "Malzeme Teslimi";
            case PLAYER_KILL:
                return "Oyuncu Öldürme";
            case TERRITORY_RESTRICT:
                return "Bölge Yasaklama";
            case NON_AGGRESSION:
                return "Saldırmama";
            case BASE_PROTECTION:
                return "Base Koruma";
            case STRUCTURE_BUILD:
                return "Yapı İnşa";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Kontrat item'ı oluştur
     */
    private ItemStack createContractItem(Contract contract) {
        if (contract == null) return new ItemStack(Material.BARRIER);
        
        // Yeni enum kullan
        me.mami.stratocraft.enums.ContractType contractType = contract.getContractType();
        Material icon = getContractIcon(contractType);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName("§e" + getContractTypeName(contractType));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        
        // Ceza tipi bilgisi
        me.mami.stratocraft.enums.PenaltyType penaltyType = contract.getPenaltyType();
        if (penaltyType != null) {
            lore.add("§7Ceza Tipi: §c" + getPenaltyTypeName(penaltyType));
        }
        lore.add("§7Ceza: §c" + contract.getPenalty() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        
        // Tip'e özel bilgiler (yeni enum'lara göre)
        if (contractType != null) {
            switch (contractType) {
                case RESOURCE_COLLECTION:
                    if (contract.getMaterial() != null) {
                        lore.add("§7Malzeme: §e" + contract.getMaterial().name());
                        lore.add("§7Miktar: §e" + contract.getAmount());
                    }
                    break;
                case COMBAT:
                    if (contract.getTargetPlayer() != null) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getTargetPlayer());
                        lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                    }
                    break;
                case TERRITORY:
                    if (contract.getRestrictedAreas() != null && !contract.getRestrictedAreas().isEmpty()) {
                        lore.add("§7Yasak Bölge: §e" + contract.getRestrictedAreas().size() + " bölge");
                        lore.add("§7Yarıçap: §e" + contract.getRestrictedRadius() + " blok");
                    }
                    break;
                case CONSTRUCTION:
                    if (contract.getStructureType() != null) {
                        lore.add("§7Yapı Tipi: §e" + contract.getStructureType());
                    }
                    break;
            }
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
        
        // Yeni enum kullan
        me.mami.stratocraft.enums.ContractType contractType = contract.getContractType();
        meta.setDisplayName("§e" + getContractTypeName(contractType));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        // İssuer bilgisi
        OfflinePlayer issuer = Bukkit.getOfflinePlayer(contract.getIssuer());
        lore.add("§7Veren: §e" + (issuer.getName() != null ? issuer.getName() : "Bilinmeyen"));
        
        // Kapsam bilgisi
        lore.add("§7Kapsam: §e" + getContractScopeName(contract.getScope()));
        
        lore.add("§7═══════════════════════");
        lore.add("§7Ödül: §a" + contract.getReward() + " Altın");
        
        // Ceza tipi bilgisi
        me.mami.stratocraft.enums.PenaltyType penaltyType = contract.getPenaltyType();
        if (penaltyType != null) {
            lore.add("§7Ceza Tipi: §c" + getPenaltyTypeName(penaltyType));
        }
        lore.add("§7Ceza: §c" + contract.getPenalty() + " Altın");
        lore.add("§7Süre: §e" + formatTime(contract.getDeadline()));
        
        // Tip'e göre özel bilgiler (yeni enum'lara göre)
        if (contractType != null) {
            switch (contractType) {
                case RESOURCE_COLLECTION:
                    if (contract.getMaterial() != null) {
                        lore.add("§7═══════════════════════");
                        lore.add("§7Malzeme: §e" + contract.getMaterial().name());
                        lore.add("§7Miktar: §e" + contract.getAmount());
                        lore.add("§7Teslim: §a" + contract.getDelivered() + "§7/§a" + contract.getAmount());
                    }
                    break;
                case COMBAT:
                    if (contract.getTargetPlayer() != null) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getTargetPlayer());
                        lore.add("§7═══════════════════════");
                        lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                    }
                    break;
                case TERRITORY:
                    lore.add("§7═══════════════════════");
                    lore.add("§7Yasak Bölgeler: §c" + (contract.getRestrictedAreas() != null ? contract.getRestrictedAreas().size() : 0) + " adet");
                    lore.add("§7Yarıçap: §e" + contract.getRestrictedRadius() + " blok");
                    break;
                case CONSTRUCTION:
                    if (contract.getStructureType() != null) {
                        lore.add("§7═══════════════════════");
                        lore.add("§7Yapı: §e" + contract.getStructureType());
                    }
                    break;
            }
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
     * Kontrat tipi ikonu (YENİ: ContractType enum kullanır)
     */
    private Material getContractIcon(me.mami.stratocraft.enums.ContractType type) {
        if (type == null) return Material.PAPER;
        switch (type) {
            case RESOURCE_COLLECTION:
                return Material.CHEST;
            case CONSTRUCTION:
                return Material.BRICKS;
            case COMBAT:
                return Material.DIAMOND_SWORD;
            case TERRITORY:
                return Material.BARRIER;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Eski Contract.ContractType için geriye uyumluluk (deprecated)
     */
    @Deprecated
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
     * Ceza tipi ismi
     */
    private String getPenaltyTypeName(me.mami.stratocraft.enums.PenaltyType penaltyType) {
        if (penaltyType == null) return "Bilinmeyen";
        switch (penaltyType) {
            case HEALTH_PENALTY:
                return "Can Cezası";
            case BANK_PENALTY:
                return "Banka Cezası";
            case MORTGAGE:
                return "Hipotek";
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
        // Kontrat kategori seçim menüsü
        else if (title.equals("§6Kontrat Kategorisi Seç") || title.equals("§6Kontrat Tipi Seç")) {
            handleTypeSelectionClick(event);
        }
        // Ceza tipi seçim menüsü
        else if (title.equals("§6Ceza Tipi Seç")) {
            handlePenaltyTypeSelectionClick(event);
        }
        // Kapsam seçim menüsü
        else if (title.equals("§6Kontrat Kapsamı Seç")) {
            handleScopeSelectionClick(event);
        }
        // Malzeme seçim menüsü
        else if (title.equals("§6Malzeme Seç")) {
            handleMaterialSelectionClick(event);
        }
        // Yapı tipi seçim menüsü
        else if (title.equals("§6Yapı Tipi Seç")) {
            handleStructureTypeSelectionClick(event);
        }
        // Ödül slider menüsü
        else if (title.equals("§6Ödül Belirle")) {
            handleRewardSliderClick(event);
        }
        // Ceza slider menüsü
        else if (title.equals("§6Ceza Belirle")) {
            handlePenaltySliderClick(event);
        }
        // Süre seçim menüsü
        else if (title.equals("§6Süre Belirle")) {
            handleTimeSelectionClick(event);
        }
        // Gün/Saat/Dakika ayarlama menüleri
        else if (title.equals("§6Gün Ayarla") || title.equals("§6Saat Ayarla") || title.equals("§6Dakika Ayarla")) {
            handleTimeAdjustmentClick(event);
        }
        // Özet menüsü
        else if (title.equals("§6Kontrat Özeti")) {
            handleSummaryMenuClick(event);
        }
        // Şablon menüsü
        else if (title.equals("§6Kontrat Şablonları")) {
            handleTemplateMenuClick(event);
        }
        // Kontrat geçmişi
        else if (title.startsWith("§6Kontrat Geçmişi")) {
            handleContractHistoryClick(event);
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
                
            case BOOK:
                // Şablonlardan oluştur
                if (event.getSlot() == 47) {
                    openTemplateMenu(player);
                }
                break;
                
            case PAPER:
                // Kontrat geçmişi
                if (event.getSlot() == 51) {
                    openContractHistoryMenu(player, 1);
                }
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
                // RESOURCE_COLLECTION
                state.contractType = me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION;
                state.step = 1;
                player.closeInventory();
                openScopeSelectionMenu(player);
                break;
                
            case DIAMOND_SWORD:
                // COMBAT
                state.contractType = me.mami.stratocraft.enums.ContractType.COMBAT;
                state.step = 1;
                player.closeInventory();
                openScopeSelectionMenu(player);
                break;
                
            case BARRIER:
                // TERRITORY
                state.contractType = me.mami.stratocraft.enums.ContractType.TERRITORY;
                state.step = 1;
                player.closeInventory();
                openScopeSelectionMenu(player);
                break;
                
            case STRUCTURE_BLOCK:
                // CONSTRUCTION
                state.contractType = me.mami.stratocraft.enums.ContractType.CONSTRUCTION;
                state.step = 1;
                player.closeInventory();
                openScopeSelectionMenu(player);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Kapsam seçim menüsü
     */
    private void openScopeSelectionMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Kapsamı Seç");
        
        // Kapsam seçenekleri
        Contract.ContractScope[] scopes = {
            Contract.ContractScope.PLAYER_TO_PLAYER,
            Contract.ContractScope.CLAN_TO_CLAN,
            Contract.ContractScope.PLAYER_TO_CLAN,
            Contract.ContractScope.CLAN_TO_PLAYER
        };
        
        Material[] icons = {
            Material.PLAYER_HEAD,
            Material.WHITE_BANNER,
            Material.EMERALD,
            Material.GOLD_INGOT
        };
        
        int[] slots = {10, 11, 12, 13};
        
        for (int i = 0; i < scopes.length; i++) {
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7" + getContractScopeDescription(scopes[i]));
            lore.add("§7═══════════════════════");
            menu.setItem(slots[i], createButton(icons[i], "§e" + getContractScopeName(scopes[i]), lore));
        }
        
        // Geri butonu
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kapsam açıklaması
     */
    private String getContractScopeDescription(Contract.ContractScope scope) {
        if (scope == null) return "Bilinmeyen";
        switch (scope) {
            case PLAYER_TO_PLAYER:
                return "Oyuncudan oyuncuya kontrat";
            case CLAN_TO_CLAN:
                return "Klandan klana kontrat";
            case PLAYER_TO_CLAN:
                return "Oyuncudan klana kontrat";
            case CLAN_TO_PLAYER:
                return "Klandan oyuncuya kontrat";
            default:
                return "Bilinmeyen";
        }
    }
    
    /**
     * Malzeme seçim menüsü tıklama
     */
    private void handleMaterialSelectionClick(InventoryClickEvent event) {
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
        
        int slot = event.getSlot();
        
        // Sayfalama butonları
        if (clicked.getType() == Material.ARROW) {
            if (slot == 45) {
                // Önceki sayfa
                if (state.materialPage == null) state.materialPage = 0;
                if (state.materialPage > 0) {
                    state.materialPage--;
                    openMaterialSelectionMenu(player);
                }
            } else if (slot == 53) {
                // Sonraki sayfa
                if (state.materialPage == null) state.materialPage = 0;
                state.materialPage++;
                openMaterialSelectionMenu(player);
            }
            return;
        }
        
        // Geri butonu
        if (clicked.getType() == Material.BARRIER && slot == 49) {
            state.step = 4;
            player.closeInventory();
            requestDaysInput(player);
            return;
        }
        
        // Malzeme seçildi
        state.material = clicked.getType();
        state.step = 6;
        player.closeInventory();
        requestAmountInput(player);
    }
    
    /**
     * Yapı tipi seçim menüsü tıklama
     */
    private void handleStructureTypeSelectionClick(InventoryClickEvent event) {
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
        
        if (clicked.getType() == Material.ARROW) {
            // Geri
            state.step = 4;
            player.closeInventory();
            requestDaysInput(player);
            return;
        }
        
        // Yapı tipi seçildi (ItemMeta'dan isim al)
        ItemMeta meta = clicked.getItemMeta();
        if (meta != null && meta.getDisplayName() != null) {
            String displayName = meta.getDisplayName();
            // "§e" prefix'ini kaldır
            state.structureType = displayName.replace("§e", "").trim();
            state.step = 6;
            player.closeInventory();
            openSummaryMenu(player);
        }
    }
    
    /**
     * Ceza tipi seçim menüsü
     */
    private void openPenaltyTypeSelectionMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6Ceza Tipi Seç");
        
        // Ceza tipleri
        me.mami.stratocraft.enums.PenaltyType[] penaltyTypes = {
            me.mami.stratocraft.enums.PenaltyType.HEALTH_PENALTY,
            me.mami.stratocraft.enums.PenaltyType.BANK_PENALTY,
            me.mami.stratocraft.enums.PenaltyType.MORTGAGE
        };
        
        int slot = 10;
        for (me.mami.stratocraft.enums.PenaltyType penaltyType : penaltyTypes) {
            if (slot > 16) break;
            
            Material icon = switch (penaltyType) {
                case HEALTH_PENALTY -> Material.REDSTONE;
                case BANK_PENALTY -> Material.GOLD_INGOT;
                case MORTGAGE -> Material.CHEST;
            };
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7" + getPenaltyTypeName(penaltyType));
            lore.add("§7═══════════════════════");
            
            ItemStack item = createButton(icon, "§e" + getPenaltyTypeName(penaltyType), lore);
            // ItemStack'e penaltyType bilgisini lore'a ekle (custom data yerine)
            List<String> itemLore = new ArrayList<>(lore);
            itemLore.add("§8penaltyType:" + penaltyType.name());
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setLore(itemLore);
                item.setItemMeta(itemMeta);
            }
            menu.setItem(slot, item);
            slot += 2;
        }
        
        // Geri butonu
        menu.setItem(22, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ödül seçimine dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Ceza tipi seçim menüsü tıklama
     */
    private void handlePenaltyTypeSelectionClick(InventoryClickEvent event) {
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
                // Geri - Ödül seçimine dön
                state.step = 2;
                player.closeInventory();
                openRewardSliderMenu(player);
                break;
                
            case REDSTONE:
            case GOLD_INGOT:
            case CHEST:
                // Ceza tipi seçildi - lore'dan oku
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getLore() != null) {
                    for (String line : meta.getLore()) {
                        if (line.startsWith("§8penaltyType:")) {
                            String penaltyTypeName = line.substring("§8penaltyType:".length());
                            try {
                                state.penaltyType = me.mami.stratocraft.enums.PenaltyType.valueOf(penaltyTypeName);
                                state.step = 4;
                                player.closeInventory();
                                openPenaltySliderMenu(player);
                            } catch (IllegalArgumentException e) {
                                player.sendMessage("§cGeçersiz ceza tipi!");
                            }
                            return;
                        }
                    }
                }
                break;
        }
    }
    
    /**
     * Kapsam seçim menüsü tıklama
     */
    private void handleScopeSelectionClick(InventoryClickEvent event) {
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
                // Geri - Tip seçimine dön
                state.step = 0;
                player.closeInventory();
                openTypeSelectionMenu(player);
                break;
                
            case PLAYER_HEAD:
                state.scope = Contract.ContractScope.PLAYER_TO_PLAYER;
                state.step = 2;
                player.closeInventory();
                openRewardSliderMenu(player);
                break;
                
            case WHITE_BANNER:
                state.scope = Contract.ContractScope.CLAN_TO_CLAN;
                state.step = 2;
                player.closeInventory();
                openRewardSliderMenu(player);
                break;
                
            case EMERALD:
                state.scope = Contract.ContractScope.PLAYER_TO_CLAN;
                state.step = 2;
                player.closeInventory();
                openRewardSliderMenu(player);
                break;
                
            case GOLD_INGOT:
                state.scope = Contract.ContractScope.CLAN_TO_PLAYER;
                state.step = 2;
                player.closeInventory();
                openRewardSliderMenu(player);
                break;
        }
    }
    
    /**
     * Ödül slider menüsü aç
     */
    private void openRewardSliderMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        if (state.reward == 0) state.reward = 100; // Varsayılan değer
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Ödül Belirle");
        
        // Mevcut değer göster
        List<String> valueLore = new ArrayList<>();
        valueLore.add("§7═══════════════════════");
        valueLore.add("§eMevcut Ödül: §a" + String.format("%.0f", state.reward) + " Altın");
        valueLore.add("§7═══════════════════════");
        menu.setItem(13, createButton(Material.GOLD_INGOT, "§e§l" + String.format("%.0f", state.reward) + " Altın", valueLore));
        
        // Hızlı değerler
        menu.setItem(9, createButton(Material.EMERALD, "§a100", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(10, createButton(Material.GOLD_INGOT, "§a500", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(11, createButton(Material.DIAMOND, "§a1000", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(12, createButton(Material.NETHERITE_INGOT, "§a5000", Arrays.asList("§7Hızlı seçim")));
        
        // Artırma/Azaltma butonları
        menu.setItem(17, createButton(Material.GREEN_CONCRETE, "§a+100", Arrays.asList("§7Ödülü 100 artır")));
        menu.setItem(18, createButton(Material.LIME_CONCRETE, "§a+50", Arrays.asList("§7Ödülü 50 artır")));
        menu.setItem(19, createButton(Material.YELLOW_CONCRETE, "§e+10", Arrays.asList("§7Ödülü 10 artır")));
        menu.setItem(20, createButton(Material.ORANGE_CONCRETE, "§e+1", Arrays.asList("§7Ödülü 1 artır")));
        
        menu.setItem(22, createButton(Material.RED_CONCRETE, "§c-1", Arrays.asList("§7Ödülü 1 azalt")));
        menu.setItem(23, createButton(Material.ORANGE_CONCRETE, "§c-10", Arrays.asList("§7Ödülü 10 azalt")));
        menu.setItem(24, createButton(Material.YELLOW_CONCRETE, "§c-50", Arrays.asList("§7Ödülü 50 azalt")));
        menu.setItem(25, createButton(Material.RED_CONCRETE, "§c-100", Arrays.asList("§7Ödülü 100 azalt")));
        
        // Onay ve Geri butonları
        menu.setItem(15, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", Arrays.asList("§7Bu ödülü kabul et")));
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Ödül slider menüsü tıklama
     */
    private void handleRewardSliderClick(InventoryClickEvent event) {
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
        
        int slot = event.getSlot();
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                state.step = 1;
                player.closeInventory();
                openScopeSelectionMenu(player);
                break;
                
            case EMERALD:
                if (slot == 9) {
                    state.reward = 100;
                    openRewardSliderMenu(player);
                }
                break;
                
            case GOLD_INGOT:
                if (slot == 10) {
                    state.reward = 500;
                    openRewardSliderMenu(player);
                } else if (slot == 13) {
                    // Mevcut değer - değişiklik yok, sadece göster
                    openRewardSliderMenu(player);
                }
                break;
                
            case DIAMOND:
                if (slot == 11) {
                    state.reward = 1000;
                    openRewardSliderMenu(player);
                }
                break;
                
            case NETHERITE_INGOT:
                if (slot == 12) {
                    state.reward = 5000;
                    openRewardSliderMenu(player);
                }
                break;
                
            case GREEN_CONCRETE:
            case LIME_CONCRETE:
            case YELLOW_CONCRETE:
            case ORANGE_CONCRETE:
                // Artırma butonları
                if (slot == 17) state.reward += 100;
                else if (slot == 18) state.reward += 50;
                else if (slot == 19) state.reward += 10;
                else if (slot == 20) state.reward += 1;
                else if (slot == 15) {
                    // Onay - Ceza tipi seçimine geç
                    state.step = 3;
                    player.closeInventory();
                    openPenaltyTypeSelectionMenu(player);
                    return;
                }
                if (state.reward < 1) state.reward = 1;
                if (state.reward > 1000000) state.reward = 1000000; // Maksimum limit
                openRewardSliderMenu(player);
                break;
                
            case RED_CONCRETE:
                // Azaltma butonları
                if (slot == 22) state.reward -= 1;
                else if (slot == 23) state.reward -= 10;
                else if (slot == 24) state.reward -= 50;
                else if (slot == 25) state.reward -= 100;
                if (state.reward < 1) state.reward = 1;
                openRewardSliderMenu(player);
                break;
        }
    }
    
    /**
     * Ceza slider menüsü aç
     */
    private void openPenaltySliderMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        if (state.penalty == 0) state.penalty = state.reward * 0.5; // Varsayılan: ödülün yarısı
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Ceza Belirle");
        
        // Mevcut değer göster
        List<String> valueLore = new ArrayList<>();
        valueLore.add("§7═══════════════════════");
        valueLore.add("§eMevcut Ceza: §c" + String.format("%.0f", state.penalty) + " Altın");
        valueLore.add("§7Ödül: §a" + String.format("%.0f", state.reward) + " Altın");
        valueLore.add("§7═══════════════════════");
        menu.setItem(13, createButton(Material.REDSTONE_BLOCK, "§c§l" + String.format("%.0f", state.penalty) + " Altın", valueLore));
        
        // Hızlı değerler (ödülün yüzdesi)
        menu.setItem(9, createButton(Material.EMERALD, "§a%25", Arrays.asList("§7Ödülün %25'i")));
        menu.setItem(10, createButton(Material.GOLD_INGOT, "§a%50", Arrays.asList("§7Ödülün %50'si")));
        menu.setItem(11, createButton(Material.DIAMOND, "§a%75", Arrays.asList("§7Ödülün %75'i")));
        menu.setItem(12, createButton(Material.NETHERITE_INGOT, "§a%100", Arrays.asList("§7Ödülün %100'ü")));
        
        // Artırma/Azaltma butonları
        menu.setItem(17, createButton(Material.GREEN_CONCRETE, "§a+100", Arrays.asList("§7Cezayı 100 artır")));
        menu.setItem(18, createButton(Material.LIME_CONCRETE, "§a+50", Arrays.asList("§7Cezayı 50 artır")));
        menu.setItem(19, createButton(Material.YELLOW_CONCRETE, "§e+10", Arrays.asList("§7Cezayı 10 artır")));
        menu.setItem(20, createButton(Material.ORANGE_CONCRETE, "§e+1", Arrays.asList("§7Cezayı 1 artır")));
        
        menu.setItem(22, createButton(Material.RED_CONCRETE, "§c-1", Arrays.asList("§7Cezayı 1 azalt")));
        menu.setItem(23, createButton(Material.ORANGE_CONCRETE, "§c-10", Arrays.asList("§7Cezayı 10 azalt")));
        menu.setItem(24, createButton(Material.YELLOW_CONCRETE, "§c-50", Arrays.asList("§7Cezayı 50 azalt")));
        menu.setItem(25, createButton(Material.RED_CONCRETE, "§c-100", Arrays.asList("§7Cezayı 100 azalt")));
        
        // Onay ve Geri butonları
        menu.setItem(15, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", Arrays.asList("§7Bu cezayı kabul et")));
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Ceza slider menüsü tıklama
     */
    private void handlePenaltySliderClick(InventoryClickEvent event) {
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
        
        int slot = event.getSlot();
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                state.step = 2;
                player.closeInventory();
                openRewardSliderMenu(player);
                break;
                
            case EMERALD:
                if (slot == 9) {
                    state.penalty = state.reward * 0.25;
                    openPenaltySliderMenu(player);
                }
                break;
                
            case GOLD_INGOT:
                if (slot == 10) {
                    state.penalty = state.reward * 0.5;
                    openPenaltySliderMenu(player);
                }
                break;
                
            case DIAMOND:
                if (slot == 11) {
                    state.penalty = state.reward * 0.75;
                    openPenaltySliderMenu(player);
                }
                break;
                
            case NETHERITE_INGOT:
                if (slot == 12) {
                    state.penalty = state.reward;
                    openPenaltySliderMenu(player);
                }
                break;
                
            case GREEN_CONCRETE:
            case LIME_CONCRETE:
            case YELLOW_CONCRETE:
            case ORANGE_CONCRETE:
                // Artırma butonları
                if (slot == 17) state.penalty += 100;
                else if (slot == 18) state.penalty += 50;
                else if (slot == 19) state.penalty += 10;
                else if (slot == 20) state.penalty += 1;
                else if (slot == 15) {
                    // Onay - Ceza miktarı belirlendi, süre seçimine geç
                    state.step = 5;
                    player.closeInventory();
                    openTimeSelectionMenu(player);
                    return;
                }
                if (state.penalty < 0) state.penalty = 0;
                if (state.penalty > 1000000) state.penalty = 1000000; // Maksimum limit
                openPenaltySliderMenu(player);
                break;
                
            case RED_CONCRETE:
            case REDSTONE_BLOCK:
                // Azaltma butonları
                if (slot == 22) state.penalty -= 1;
                else if (slot == 23) state.penalty -= 10;
                else if (slot == 24) state.penalty -= 50;
                else if (slot == 25) state.penalty -= 100;
                if (state.penalty < 0) state.penalty = 0;
                openPenaltySliderMenu(player);
                break;
        }
    }
    
    /**
     * Ödül input iste (eski metod - geriye uyumluluk için)
     */
    private void requestRewardInput(Player player) {
        openRewardSliderMenu(player);
    }
    
    /**
     * Ceza input iste
     */
    private void requestPenaltyInput(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) return;
        
        state.waitingForInput = "penalty";
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§eKontrat Ceza Miktarı Belirle");
        player.sendMessage("§7Chat'e ceza miktarını yazın (örn: 500)");
        player.sendMessage("§7Mevcut ödül: §a" + state.reward);
        player.sendMessage("§7İptal etmek için: §c/iptal");
        player.sendMessage("§6═══════════════════════════════════");
    }
    
    /**
     * Süre seçim menüsü aç (gün/hafta/ay/saat/dakika)
     */
    private void openTimeSelectionMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        if (state.deadlineDays == 0) state.deadlineDays = 7; // Varsayılan: 7 gün
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Süre Belirle");
        
        // Mevcut süre göster
        long totalDays = state.deadlineDays;
        long weeks = totalDays / 7;
        long days = totalDays % 7;
        long hours = (totalDays * 24) % 24;
        long minutes = (totalDays * 24 * 60) % 60;
        
        List<String> timeLore = new ArrayList<>();
        timeLore.add("§7═══════════════════════");
        timeLore.add("§eMevcut Süre:");
        if (weeks > 0) timeLore.add("§7  " + weeks + " Hafta");
        if (days > 0) timeLore.add("§7  " + days + " Gün");
        if (hours > 0) timeLore.add("§7  " + hours + " Saat");
        if (minutes > 0) timeLore.add("§7  " + minutes + " Dakika");
        timeLore.add("§7Toplam: §e" + totalDays + " Gün");
        timeLore.add("§7═══════════════════════");
        menu.setItem(22, createButton(Material.CLOCK, "§e§lMevcut Süre", timeLore));
        
        // Hızlı seçimler (Gün)
        menu.setItem(10, createButton(Material.PAPER, "§a1 Gün", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(11, createButton(Material.PAPER, "§a3 Gün", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(12, createButton(Material.PAPER, "§a7 Gün", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(13, createButton(Material.PAPER, "§a14 Gün", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(14, createButton(Material.PAPER, "§a30 Gün", Arrays.asList("§7Hızlı seçim")));
        
        // Hızlı seçimler (Hafta)
        menu.setItem(19, createButton(Material.BOOK, "§a1 Hafta", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(20, createButton(Material.BOOK, "§a2 Hafta", Arrays.asList("§7Hızlı seçim")));
        menu.setItem(21, createButton(Material.BOOK, "§a4 Hafta", Arrays.asList("§7Hızlı seçim")));
        
        // Hızlı seçimler (Ay)
        menu.setItem(28, createButton(Material.BOOKSHELF, "§a1 Ay", Arrays.asList("§730 gün")));
        menu.setItem(29, createButton(Material.BOOKSHELF, "§a2 Ay", Arrays.asList("§760 gün")));
        menu.setItem(30, createButton(Material.BOOKSHELF, "§a3 Ay", Arrays.asList("§790 gün")));
        
        // Detaylı zaman seçimi butonları
        menu.setItem(37, createButton(Material.SUNFLOWER, "§eGün Ekle/Azalt", Arrays.asList("§7Gün bazlı ayarlama")));
        menu.setItem(38, createButton(Material.CLOCK, "§eSaat Ekle/Azalt", Arrays.asList("§7Saat bazlı ayarlama")));
        menu.setItem(39, createButton(Material.REDSTONE, "§eDakika Ekle/Azalt", Arrays.asList("§7Dakika bazlı ayarlama")));
        
        // Onay ve Geri butonları
        menu.setItem(40, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", Arrays.asList("§7Bu süreyi kabul et")));
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Süre seçim menüsü tıklama
     */
    private void handleTimeSelectionClick(InventoryClickEvent event) {
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
        
        int slot = event.getSlot();
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                state.step = 3;
                player.closeInventory();
                openPenaltySliderMenu(player);
                break;
                
            case PAPER:
                // Gün seçimi
                if (slot == 10) state.deadlineDays = 1;
                else if (slot == 11) state.deadlineDays = 3;
                else if (slot == 12) state.deadlineDays = 7;
                else if (slot == 13) state.deadlineDays = 14;
                else if (slot == 14) state.deadlineDays = 30;
                openTimeSelectionMenu(player);
                break;
                
            case BOOK:
                // Hafta seçimi
                if (slot == 19) state.deadlineDays = 7;
                else if (slot == 20) state.deadlineDays = 14;
                else if (slot == 21) state.deadlineDays = 28;
                openTimeSelectionMenu(player);
                break;
                
            case BOOKSHELF:
                // Ay seçimi
                if (slot == 28) state.deadlineDays = 30;
                else if (slot == 29) state.deadlineDays = 60;
                else if (slot == 30) state.deadlineDays = 90;
                openTimeSelectionMenu(player);
                break;
                
            case SUNFLOWER:
                // Gün ekle/azalt menüsü
                openDayAdjustmentMenu(player);
                break;
                
            case CLOCK:
                // Saat ekle/azalt menüsü
                openHourAdjustmentMenu(player);
                break;
                
            case REDSTONE:
                // Dakika ekle/azalt menüsü
                openMinuteAdjustmentMenu(player);
                break;
                
            case GREEN_CONCRETE:
                // Onay
                state.step = 5;
                player.closeInventory();
                openTypeSpecificMenu(player);
                break;
        }
    }
    
    /**
     * Gün ayarlama menüsü
     */
    private void openDayAdjustmentMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Gün Ayarla");
        
        // Mevcut gün
        menu.setItem(13, createButton(Material.SUNFLOWER, "§e§l" + state.deadlineDays + " Gün", 
            Arrays.asList("§7Mevcut gün sayısı")));
        
        // Artırma/Azaltma
        menu.setItem(17, createButton(Material.GREEN_CONCRETE, "§a+7", Arrays.asList("§77 gün ekle")));
        menu.setItem(18, createButton(Material.LIME_CONCRETE, "§a+1", Arrays.asList("§71 gün ekle")));
        menu.setItem(22, createButton(Material.RED_CONCRETE, "§c-1", Arrays.asList("§71 gün azalt")));
        menu.setItem(23, createButton(Material.RED_CONCRETE, "§c-7", Arrays.asList("§77 gün azalt")));
        
        menu.setItem(26, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", Arrays.asList("§7Geri dön")));
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki menü")));
        
        player.openInventory(menu);
    }
    
    /**
     * Saat ayarlama menüsü
     */
    private void openHourAdjustmentMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        // Günü saate çevir
        long totalHours = (long)(state.deadlineDays * 24);
        long days = totalHours / 24;
        long hours = totalHours % 24;
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Saat Ayarla");
        
        menu.setItem(13, createButton(Material.CLOCK, "§e§l" + days + "g " + hours + "s", 
            Arrays.asList("§7Mevcut süre")));
        
        // Artırma/Azaltma
        menu.setItem(17, createButton(Material.GREEN_CONCRETE, "§a+24", Arrays.asList("§724 saat ekle")));
        menu.setItem(18, createButton(Material.LIME_CONCRETE, "§a+1", Arrays.asList("§71 saat ekle")));
        menu.setItem(22, createButton(Material.RED_CONCRETE, "§c-1", Arrays.asList("§71 saat azalt")));
        menu.setItem(23, createButton(Material.RED_CONCRETE, "§c-24", Arrays.asList("§724 saat azalt")));
        
        menu.setItem(26, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", Arrays.asList("§7Geri dön")));
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki menü")));
        
        player.openInventory(menu);
    }
    
    /**
     * Dakika ayarlama menüsü
     */
    private void openMinuteAdjustmentMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        // Günü dakikaya çevir
        double totalDays = state.deadlineDays;
        long days = (long)totalDays;
        double remainingDays = totalDays - days;
        long totalHours = (long)(remainingDays * 24);
        long hours = totalHours;
        double remainingHours = (remainingDays * 24) - totalHours;
        long minutes = (long)(remainingHours * 60);
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Dakika Ayarla");
        
        menu.setItem(13, createButton(Material.REDSTONE, "§e§l" + days + "g " + hours + "s " + minutes + "d", 
            Arrays.asList("§7Mevcut süre")));
        
        // Artırma/Azaltma
        menu.setItem(17, createButton(Material.GREEN_CONCRETE, "§a+60", Arrays.asList("§760 dakika ekle")));
        menu.setItem(18, createButton(Material.LIME_CONCRETE, "§a+1", Arrays.asList("§71 dakika ekle")));
        menu.setItem(22, createButton(Material.RED_CONCRETE, "§c-1", Arrays.asList("§71 dakika azalt")));
        menu.setItem(23, createButton(Material.RED_CONCRETE, "§c-60", Arrays.asList("§760 dakika azalt")));
        
        menu.setItem(26, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", Arrays.asList("§7Geri dön")));
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki menü")));
        
        player.openInventory(menu);
    }
    
    /**
     * Zaman ayarlama menüsü tıklama (Gün/Saat/Dakika)
     */
    private void handleTimeAdjustmentClick(InventoryClickEvent event) {
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
        
        String title = event.getView().getTitle();
        int slot = event.getSlot();
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri - Süre seçim menüsüne dön
                player.closeInventory();
                openTimeSelectionMenu(player);
                break;
                
            case GREEN_CONCRETE:
                // Onay butonu veya artırma butonları
                if (slot == 26) {
                    // Onay - Süre seçim menüsüne dön
                    player.closeInventory();
                    openTimeSelectionMenu(player);
                } else {
                    // Artırma butonları
                    if (title.equals("§6Gün Ayarla")) {
                        if (slot == 17) state.deadlineDays += 7;
                        else if (slot == 18) state.deadlineDays += 1;
                        if (state.deadlineDays < 0) state.deadlineDays = 0;
                        openDayAdjustmentMenu(player);
                    } else if (title.equals("§6Saat Ayarla")) {
                        if (slot == 17) state.deadlineDays += 1; // 24 saat = 1 gün
                        else if (slot == 18) state.deadlineDays += (1.0 / 24.0); // 1 saat
                        if (state.deadlineDays < 0) state.deadlineDays = 0;
                        openHourAdjustmentMenu(player);
                    } else if (title.equals("§6Dakika Ayarla")) {
                        if (slot == 17) state.deadlineDays += (1.0 / 24.0 / 60.0 * 60); // 60 dakika = 1 saat
                        else if (slot == 18) state.deadlineDays += (1.0 / 24.0 / 60.0); // 1 dakika
                        if (state.deadlineDays < 0) state.deadlineDays = 0;
                        openMinuteAdjustmentMenu(player);
                    }
                }
                break;
                
            case LIME_CONCRETE:
                // Artırma butonları
                if (title.equals("§6Gün Ayarla")) {
                    if (slot == 18) state.deadlineDays += 1;
                    if (state.deadlineDays < 0) state.deadlineDays = 0;
                    openDayAdjustmentMenu(player);
                } else if (title.equals("§6Saat Ayarla")) {
                    if (slot == 18) state.deadlineDays += (1.0 / 24.0); // 1 saat
                    if (state.deadlineDays < 0) state.deadlineDays = 0;
                    openHourAdjustmentMenu(player);
                } else if (title.equals("§6Dakika Ayarla")) {
                    if (slot == 18) state.deadlineDays += (1.0 / 24.0 / 60.0); // 1 dakika
                    if (state.deadlineDays < 0) state.deadlineDays = 0;
                    openMinuteAdjustmentMenu(player);
                }
                break;
                
            case RED_CONCRETE:
                // Azaltma
                if (title.equals("§6Gün Ayarla")) {
                    if (slot == 22) state.deadlineDays -= 1;
                    else if (slot == 23) state.deadlineDays -= 7;
                    if (state.deadlineDays < 0) state.deadlineDays = 0;
                    openDayAdjustmentMenu(player);
                } else if (title.equals("§6Saat Ayarla")) {
                    if (slot == 22) state.deadlineDays -= (1.0 / 24.0); // 1 saat
                    else if (slot == 23) state.deadlineDays -= 1; // 24 saat = 1 gün
                    if (state.deadlineDays < 0) state.deadlineDays = 0;
                    openHourAdjustmentMenu(player);
                } else if (title.equals("§6Dakika Ayarla")) {
                    if (slot == 22) state.deadlineDays -= (1.0 / 24.0 / 60.0); // 1 dakika
                    else if (slot == 23) state.deadlineDays -= (1.0 / 24.0 / 60.0 * 60); // 60 dakika = 1 saat
                    if (state.deadlineDays < 0) state.deadlineDays = 0;
                    openMinuteAdjustmentMenu(player);
                }
                break;
        }
    }
    
    /**
     * Süre input iste (eski metod - geriye uyumluluk için)
     */
    private void requestDaysInput(Player player) {
        openTimeSelectionMenu(player);
    }
    
    /**
     * Miktar input iste (MATERIAL_DELIVERY için)
     */
    private void requestAmountInput(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) return;
        
        state.waitingForInput = "amount";
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§eMalzeme Miktarı Belirle");
        player.sendMessage("§7Chat'e miktarı yazın (örn: 64)");
        player.sendMessage("§7Seçilen malzeme: §e" + (state.material != null ? state.material.name() : "Yok"));
        player.sendMessage("§7İptal etmek için: §c/iptal");
        player.sendMessage("§6═══════════════════════════════════");
    }
    
    /**
     * Oyuncu input iste (PLAYER_KILL, NON_AGGRESSION için)
     */
    private void requestPlayerInput(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) return;
        
        state.waitingForInput = "player";
        if (state.contractType == me.mami.stratocraft.enums.ContractType.COMBAT) {
            player.sendMessage("§6═══════════════════════════════════");
            player.sendMessage("§eHedef Oyuncu Belirle");
            player.sendMessage("§7Chat'e hedef oyuncunun ismini yazın");
            player.sendMessage("§7İptal etmek için: §c/iptal");
            player.sendMessage("§6═══════════════════════════════════");
        } else if (state.contractType == me.mami.stratocraft.enums.ContractType.COMBAT) {
            player.sendMessage("§6═══════════════════════════════════");
            player.sendMessage("§eHedef Belirle (Oyuncu/Klan)");
            player.sendMessage("§7Chat'e hedef oyuncu veya klan ismini yazın");
            player.sendMessage("§7İptal etmek için: §c/iptal");
            player.sendMessage("§6═══════════════════════════════════");
        }
    }
    
    /**
     * Chat input handler
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
    public void onChatInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        
        if (state == null || state.waitingForInput == null) return;
        
        event.setCancelled(true);
        
        String message = event.getMessage().trim();
        
        // İptal kontrolü
        if (message.equalsIgnoreCase("iptal") || message.equalsIgnoreCase("cancel")) {
            wizardStates.remove(player.getUniqueId());
            player.sendMessage("§cKontrat oluşturma iptal edildi.");
            return;
        }
        
        // Main thread'de işle
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            processChatInput(player, state, message);
        });
    }
    
    /**
     * Chat input işle
     */
    private void processChatInput(Player player, ContractWizardState state, String message) {
        switch (state.waitingForInput) {
            case "reward":
                try {
                    double reward = Double.parseDouble(message);
                    if (reward <= 0) {
                        player.sendMessage("§cÖdül miktarı 0'dan büyük olmalı!");
                        return;
                    }
                    state.reward = reward;
                    state.waitingForInput = null;
                    state.step = 3;
                    openPenaltySliderMenu(player);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz sayı! Lütfen bir sayı girin (örn: 1000)");
                }
                break;
                
            case "penalty":
                try {
                    double penalty = Double.parseDouble(message);
                    if (penalty < 0) {
                        player.sendMessage("§cCeza miktarı 0 veya daha büyük olmalı!");
                        return;
                    }
                    state.penalty = penalty;
                    state.waitingForInput = null;
                    state.step = 4;
                    requestDaysInput(player);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz sayı! Lütfen bir sayı girin (örn: 500)");
                }
                break;
                
            case "days":
                try {
                    long days = Long.parseLong(message);
                    if (days <= 0) {
                        player.sendMessage("§cGün sayısı 0'dan büyük olmalı!");
                        return;
                    }
                    state.deadlineDays = days;
                    state.waitingForInput = null;
                    state.step = 5;
                    // Tip'e özel parametrelere geç
                    openTypeSpecificMenu(player);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz sayı! Lütfen bir sayı girin (örn: 7)");
                }
                break;
                
            case "amount":
                try {
                    int amount = Integer.parseInt(message);
                    if (amount <= 0) {
                        player.sendMessage("§cMiktar 0'dan büyük olmalı!");
                        return;
                    }
                    state.amount = amount;
                    state.waitingForInput = null;
                    state.step = 6;
                    openSummaryMenu(player);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cGeçersiz sayı! Lütfen bir sayı girin (örn: 64)");
                }
                break;
                
            case "player":
                // Oyuncu veya klan bul
                Player targetPlayer = Bukkit.getPlayer(message);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    if (state.contractType == me.mami.stratocraft.enums.ContractType.COMBAT) {
                        state.targetPlayer = targetPlayer.getUniqueId();
                        state.waitingForInput = null;
                        state.step = 6;
                        openSummaryMenu(player);
                    } else if (state.contractType == me.mami.stratocraft.enums.ContractType.COMBAT) {
                        // Oyuncu veya klan kontrolü
                        Clan targetClan = clanManager.getClanByName(message);
                        if (targetClan != null) {
                            // Klan ID'sini nonAggressionTarget'a kaydet (özel işaretleme gerekebilir)
                            state.nonAggressionTarget = targetClan.getId();
                            state.waitingForInput = null;
                            state.step = 6;
                            openSummaryMenu(player);
                        } else {
                            state.nonAggressionTarget = targetPlayer.getUniqueId();
                            state.waitingForInput = null;
                            state.step = 6;
                            openSummaryMenu(player);
                        }
                    }
                } else {
                    // Klan kontrolü
                    Clan targetClan = clanManager.getClanByName(message);
                    if (targetClan != null) {
                        if (state.contractType == me.mami.stratocraft.enums.ContractType.COMBAT) {
                            state.nonAggressionTarget = targetClan.getId();
                            state.waitingForInput = null;
                            state.step = 6;
                            openSummaryMenu(player);
                        } else {
                            player.sendMessage("§cOyuncu bulunamadı: " + message);
                        }
                    } else {
                        player.sendMessage("§cOyuncu veya klan bulunamadı: " + message);
                    }
                }
                break;
                
            case "location":
                // Location input için özel kontrol (onay komutu ile)
                if (message.equalsIgnoreCase("onay") || message.equalsIgnoreCase("ok") || message.equalsIgnoreCase("confirm")) {
                    if (state.restrictedAreas == null) {
                        state.restrictedAreas = new ArrayList<>();
                    }
                    state.restrictedAreas.add(player.getLocation());
                    state.restrictedRadius = 50; // Varsayılan radius
                    state.waitingForInput = null;
                    state.step = 6;
                    openSummaryMenu(player);
                } else {
                    player.sendMessage("§cKonum onaylamak için chat'e: §aonay");
                    player.sendMessage("§7Şu anki konumunuz: §e" + 
                        player.getLocation().getBlockX() + ", " + 
                        player.getLocation().getBlockY() + ", " + 
                        player.getLocation().getBlockZ());
                }
                break;
                
            case "template_name":
                // Şablon ismi
                if (message.length() < 3 || message.length() > 20) {
                    player.sendMessage("§cŞablon ismi 3-20 karakter arasında olmalı!");
                    return;
                }
                
                // Şablon oluştur
                ContractTemplate template = new ContractTemplate(message);
                ContractWizardState templateState = wizardStates.get(player.getUniqueId());
                if (templateState != null) {
                    template.contractType = templateState.contractType;
                    template.scope = templateState.scope;
                    template.reward = templateState.reward;
                    template.penalty = templateState.penalty;
                    template.deadlineDays = templateState.deadlineDays;
                    template.material = templateState.material;
                    template.amount = templateState.amount;
                    template.targetPlayer = templateState.targetPlayer;
                    template.structureType = templateState.structureType;
                    
                    List<ContractTemplate> templates = playerTemplates.getOrDefault(player.getUniqueId(), new ArrayList<>());
                    templates.add(template);
                    playerTemplates.put(player.getUniqueId(), templates);
                    
                    state.waitingForInput = null;
                    player.sendMessage("§aŞablon kaydedildi: " + message);
                    player.sendMessage("§7Şablon menüsünden kullanabilirsiniz.");
                }
                break;
        }
    }
    
    /**
     * Tip'e özel menü aç
     */
    private void openTypeSpecificMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        if (state.contractType == null) return;
        switch (state.contractType) {
            case RESOURCE_COLLECTION:
                openMaterialSelectionMenu(player);
                break;
            case COMBAT:
                requestPlayerInput(player);
                break;
            case TERRITORY:
                // Bölge seçimi için location picker (şimdilik chat input)
                state.waitingForInput = "location";
                player.sendMessage("§6═══════════════════════════════════");
                player.sendMessage("§eYasak Bölge Merkezi Seç");
                player.sendMessage("§7İstediğiniz konuma gidin");
                player.sendMessage("§7Chat'e §aonay §7yazarak konumu onaylayın");
                player.sendMessage("§7Şu anki konumunuz: §e" + 
                    player.getLocation().getBlockX() + ", " + 
                    player.getLocation().getBlockY() + ", " + 
                    player.getLocation().getBlockZ());
                player.sendMessage("§7İptal etmek için: §c/iptal");
                player.sendMessage("§6═══════════════════════════════════");
                break;
            case CONSTRUCTION:
                openStructureTypeMenu(player);
                break;
        }
    }
    
    /**
     * Malzeme seçim menüsü (MATERIAL_DELIVERY için)
     */
    private void openMaterialSelectionMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Malzeme Seç");
        
        // Genişletilmiş malzeme listesi (sayfalama ile)
        Material[] materials = {
            // Değerli metaller
            Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD,
            Material.NETHERITE_INGOT, Material.COPPER_INGOT,
            // Temel kaynaklar
            Material.COAL, Material.REDSTONE, Material.LAPIS_LAZULI, Material.QUARTZ,
            Material.AMETHYST_SHARD, Material.RAW_IRON, Material.RAW_GOLD, Material.RAW_COPPER,
            // Özel bloklar
            Material.OBSIDIAN, Material.BEACON, Material.ENCHANTED_BOOK, Material.NETHER_STAR,
            Material.ENDER_PEARL, Material.ENDER_EYE, Material.SHULKER_SHELL,
            // Ağaç ve doğal kaynaklar
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG,
            // Taşlar
            Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE, Material.BLACKSTONE,
            Material.NETHERRACK, Material.END_STONE,
            // Yiyecekler
            Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.BREAD,
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP,
            // Diğer
            Material.STRING, Material.LEATHER, Material.FEATHER, Material.BONE,
            Material.SPIDER_EYE, Material.GUNPOWDER, Material.BLAZE_ROD, Material.GHAST_TEAR
        };
        
        // Sayfalama için state'e sayfa bilgisi ekle
        int currentPage = state.materialPage = (state.materialPage != null ? state.materialPage : 0);
        int itemsPerPage = 45;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, materials.length);
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot >= 45) break;
            Material mat = materials[i];
            List<String> lore = Arrays.asList("§7Sol tıkla seç");
            menu.setItem(slot++, createButton(mat, "§e" + getMaterialDisplayName(mat), lore));
        }
        
        // Sayfalama butonları
        if (currentPage > 0) {
            menu.setItem(45, createButton(Material.ARROW, "§7Önceki Sayfa", 
                Arrays.asList("§7Sayfa " + (currentPage))));
        }
        
        if (endIndex < materials.length) {
            menu.setItem(53, createButton(Material.ARROW, "§7Sonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (currentPage + 2))));
        }
        
        // Geri butonu
        menu.setItem(49, createButton(Material.BARRIER, "§cGeri", Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Yapı tipi seçim menüsü (STRUCTURE_BUILD için)
     */
    private void openStructureTypeMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Yapı Tipi Seç");
        
        // Yapı tipleri (Structure.Type enum'ından)
        String[] structureTypes = {
            "BATTERY", "TELEPORTER", "DEFENSE_TOWER", "RESOURCE_GENERATOR"
        };
        
        Material[] icons = {
            Material.REDSTONE_BLOCK, Material.ENDER_PEARL, Material.ARROW, Material.IRON_INGOT
        };
        
        int[] slots = {10, 11, 12, 13};
        
        for (int i = 0; i < structureTypes.length; i++) {
            List<String> lore = Arrays.asList("§7Sol tıkla seç");
            menu.setItem(slots[i], createButton(icons[i], "§e" + structureTypes[i], lore));
        }
        
        // Geri butonu
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Özet menüsü ve onay
     */
    private void openSummaryMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Özeti");
        
        // Özet bilgileri
        List<String> summaryLore = new ArrayList<>();
        summaryLore.add("§7═══════════════════════");
        summaryLore.add("§7Tip: §e" + (state.contractType != null ? getContractTypeName(state.contractType) : "Bilinmeyen"));
        summaryLore.add("§7Kapsam: §e" + getContractScopeName(state.scope));
        summaryLore.add("§7Ödül: §a" + state.reward + " Altın");
        summaryLore.add("§7Ceza: §c" + state.penalty + " Altın");
        summaryLore.add("§7Süre: §e" + state.deadlineDays + " Gün");
        
        // Tip'e özel bilgiler
        if (state.contractType != null) {
            switch (state.contractType) {
                case RESOURCE_COLLECTION:
                    summaryLore.add("§7Malzeme: §e" + (state.material != null ? state.material.name() : "Yok"));
                    summaryLore.add("§7Miktar: §e" + state.amount);
                    break;
                case COMBAT:
                    if (state.targetPlayer != null) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(state.targetPlayer);
                        summaryLore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                    }
                    break;
                case TERRITORY:
                    if (state.restrictedAreas != null && !state.restrictedAreas.isEmpty()) {
                        summaryLore.add("§7Yasak Bölge: §e" + state.restrictedAreas.size() + " bölge");
                        summaryLore.add("§7Yarıçap: §e" + (state.restrictedRadius > 0 ? state.restrictedRadius : 50) + " blok");
                    } else {
                        summaryLore.add("§7Yasak Bölge: §cHenüz seçilmedi");
                    }
                    break;
                case CONSTRUCTION:
                    summaryLore.add("§7Yapı Tipi: §e" + (state.structureType != null ? state.structureType : "Yok"));
                    break;
            }
        }
        
        summaryLore.add("§7═══════════════════════");
        
        menu.setItem(13, createButton(Material.PAPER, "§eKontrat Özeti", summaryLore));
        
        // Onay butonu
        menu.setItem(11, createButton(Material.GREEN_CONCRETE, "§a§lONAYLA", 
            Arrays.asList("§7Kontratı oluştur")));
        
        // Şablon olarak kaydet butonu
        menu.setItem(12, createButton(Material.BOOK, "§eŞablon Olarak Kaydet", 
            Arrays.asList("§7Bu kontratı şablon olarak kaydet", "§7Daha sonra hızlıca kullanabilirsiniz")));
        
        // İptal butonu
        menu.setItem(15, createButton(Material.RED_CONCRETE, "§c§lİPTAL", 
            Arrays.asList("§7Kontrat oluşturmayı iptal et")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Özet menüsü tıklama
     */
    private void handleSummaryMenuClick(InventoryClickEvent event) {
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
            case GREEN_CONCRETE:
                // Kontratı oluştur
                createContractFromState(player, state);
                wizardStates.remove(player.getUniqueId());
                player.closeInventory();
                player.sendMessage("§aKontrat başarıyla oluşturuldu!");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                break;
                
            case BOOK:
                // Şablon olarak kaydet
                saveTemplateFromState(player, state);
                break;
                
            case RED_CONCRETE:
                // İptal
                wizardStates.remove(player.getUniqueId());
                player.closeInventory();
                player.sendMessage("§cKontrat oluşturma iptal edildi.");
                break;
        }
    }
    
    /**
     * State'den kontrat oluştur (YENİ: ContractType ve PenaltyType enum kullanır)
     */
    private void createContractFromState(Player player, ContractWizardState state) {
        if (contractManager == null) return;
        
        // PenaltyType varsayılan değeri
        if (state.penaltyType == null) {
            state.penaltyType = me.mami.stratocraft.enums.PenaltyType.BANK_PENALTY;
        }
        
        // Temel kontrat oluştur (YENİ: ContractType ve PenaltyType kullanır)
        contractManager.createContract(
            player.getUniqueId(),
            state.contractType,
            state.scope,
            state.reward,
            state.penaltyType,
            state.deadlineDays
        );
        
        // En son oluşturulan kontratı al (ID ile eşleştirme yaparak)
        List<Contract> contracts = contractManager.getContracts();
        Contract contract = null;
        
        // En son eklenen kontratı bul (issuer ve contractType eşleşen)
        for (int i = contracts.size() - 1; i >= 0; i--) {
            Contract c = contracts.get(i);
            if (c != null && c.getIssuer().equals(player.getUniqueId()) && 
                c.getContractType() == state.contractType && c.getScope() == state.scope &&
                Math.abs(c.getReward() - state.reward) < 0.01) {
                contract = c;
                break;
            }
        }
        
        if (contract == null) {
            player.sendMessage("§cKontrat oluşturulurken hata oluştu!");
            return;
        }
        
        // Kategori'ye özel parametreleri set et
        switch (state.contractType) {
            case RESOURCE_COLLECTION:
                if (state.material != null) {
                    contract.setMaterial(state.material);
                }
                if (state.amount > 0) {
                    contract.setAmount(state.amount);
                }
                break;
            case COMBAT:
                if (state.targetPlayer != null) {
                    contract.setTargetPlayer(state.targetPlayer);
                }
                if (state.nonAggressionTarget != null) {
                    contract.setNonAggressionTarget(state.nonAggressionTarget);
                }
                break;
            case TERRITORY:
                if (state.restrictedAreas != null && !state.restrictedAreas.isEmpty()) {
                    contract.setRestrictedAreas(state.restrictedAreas);
                }
                contract.setRestrictedRadius(state.restrictedRadius > 0 ? state.restrictedRadius : 50);
                break;
            case CONSTRUCTION:
                if (state.structureType != null && !state.structureType.isEmpty()) {
                    contract.setStructureType(state.structureType);
                }
                break;
        }
        
        // Kontratı veritabanına kaydet (otomatik kayıt için)
        if (plugin.getClanManager() != null) {
            // Kontrat kaydedildi
        }
    }
    
    /**
     * Material display name (Türkçe)
     */
    private String getMaterialDisplayName(Material material) {
        if (material == null) return "Bilinmeyen";
        
        // Basit Türkçe çeviri
        switch (material) {
            case DIAMOND: return "Elmas";
            case GOLD_INGOT: return "Altın Külçe";
            case IRON_INGOT: return "Demir Külçe";
            case EMERALD: return "Zümrüt";
            case NETHERITE_INGOT: return "Netherite Külçe";
            case COPPER_INGOT: return "Bakır Külçe";
            case OBSIDIAN: return "Obsidyen";
            case COAL: return "Kömür";
            case REDSTONE: return "Kızıltaş";
            case LAPIS_LAZULI: return "Lapis Lazuli";
            case QUARTZ: return "Nether Quartz";
            case AMETHYST_SHARD: return "Ametist Parçası";
            case RAW_IRON: return "Ham Demir";
            case RAW_GOLD: return "Ham Altın";
            case RAW_COPPER: return "Ham Bakır";
            case BEACON: return "İşaret Işığı";
            case ENCHANTED_BOOK: return "Büyülü Kitap";
            case NETHER_STAR: return "Nether Yıldızı";
            case ENDER_PEARL: return "Ender İncisi";
            case ENDER_EYE: return "Ender Gözü";
            case SHULKER_SHELL: return "Shulker Kabuğu";
            case OAK_LOG: return "Meşe Kütüğü";
            case SPRUCE_LOG: return "Ladin Kütüğü";
            case BIRCH_LOG: return "Huş Kütüğü";
            case JUNGLE_LOG: return "Orman Kütüğü";
            case ACACIA_LOG: return "Akasya Kütüğü";
            case DARK_OAK_LOG: return "Koyu Meşe Kütüğü";
            case MANGROVE_LOG: return "Mangrov Kütüğü";
            case STONE: return "Taş";
            case COBBLESTONE: return "Kırık Taş";
            case DEEPSLATE: return "Derin Kayrak";
            case BLACKSTONE: return "Siyah Taş";
            case NETHERRACK: return "Netherrack";
            case END_STONE: return "End Taşı";
            case GOLDEN_APPLE: return "Altın Elma";
            case ENCHANTED_GOLDEN_APPLE: return "Büyülü Altın Elma";
            case BREAD: return "Ekmek";
            case COOKED_BEEF: return "Pişmiş Et";
            case COOKED_PORKCHOP: return "Pişmiş Domuz Eti";
            case STRING: return "İp";
            case LEATHER: return "Deri";
            case FEATHER: return "Tüy";
            case BONE: return "Kemik";
            case SPIDER_EYE: return "Örümcek Gözü";
            case GUNPOWDER: return "Barut";
            case BLAZE_ROD: return "Blaze Çubuğu";
            case GHAST_TEAR: return "Ghast Gözyaşı";
            default: 
                // Material ismini daha okunabilir hale getir
                String name = material.name().toLowerCase().replace("_", " ");
                String[] words = name.split(" ");
                StringBuilder result = new StringBuilder();
                for (String word : words) {
                    if (result.length() > 0) result.append(" ");
                    result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
                }
                return result.toString();
        }
    }
    
    /**
     * Şablon menüsü aç
     */
    private void openTemplateMenu(Player player) {
        List<ContractTemplate> templates = playerTemplates.getOrDefault(player.getUniqueId(), new ArrayList<>());
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Kontrat Şablonları");
        
        if (templates.isEmpty()) {
            menu.setItem(22, createButton(Material.BARRIER, "§cŞablon Yok", 
                Arrays.asList("§7Henüz şablon kaydetmediniz", "§7Özet menüsünden şablon kaydedebilirsiniz")));
            menu.setItem(0, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        } else {
            int slot = 0;
            for (ContractTemplate template : templates) {
                if (slot >= 45) break;
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Tip: §e" + (template.contractType != null ? getContractTypeName(template.contractType) : "Bilinmeyen"));
                lore.add("§7Kapsam: §e" + getContractScopeName(template.scope));
                lore.add("§7Ödül: §a" + template.reward + " Altın");
                lore.add("§7Ceza: §c" + template.penalty + " Altın");
                lore.add("§7Süre: §e" + template.deadlineDays + " Gün");
                if (template.material != null) {
                    lore.add("§7Malzeme: §e" + getMaterialDisplayName(template.material));
                    lore.add("§7Miktar: §e" + template.amount);
                }
                lore.add("§7═══════════════════════");
                lore.add("§aSol Tık: §7Bu şablonu kullan");
                lore.add("§cSağ Tık: §7Şablonu sil");
                
                menu.setItem(slot++, createButton(Material.BOOK, "§e" + template.name, lore));
            }
            
            // Yeni şablon oluştur butonu
            menu.setItem(49, createButton(Material.WRITABLE_BOOK, "§aYeni Şablon Oluştur", 
                Arrays.asList("§7Mevcut kontratı şablon olarak kaydet")));
        }
        
        // Geri butonu
        menu.setItem(45, createButton(Material.ARROW, "§7Geri", Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Şablon menüsü tıklama
     */
    private void handleTemplateMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        List<ContractTemplate> templates = playerTemplates.getOrDefault(player.getUniqueId(), new ArrayList<>());
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                int page = currentPages.getOrDefault(player.getUniqueId(), 1);
                player.closeInventory();
                openMainMenu(player, page);
                break;
                
            case BOOK:
                // Şablon seçildi
                int slot = event.getSlot();
                if (slot < templates.size()) {
                    ContractTemplate template = templates.get(slot);
                    if (event.isRightClick()) {
                        // Şablonu sil
                        templates.remove(slot);
                        playerTemplates.put(player.getUniqueId(), templates);
                        player.sendMessage("§cŞablon silindi: " + template.name);
                        openTemplateMenu(player);
                    } else {
                        // Şablonu kullan
                        useTemplate(player, template);
                    }
                }
                break;
                
            case WRITABLE_BOOK:
                // Mevcut wizard state'inden şablon oluştur
                ContractWizardState state = wizardStates.get(player.getUniqueId());
                if (state != null && state.step == 6) {
                    // Özet menüsündeyken şablon kaydet
                    saveTemplateFromState(player, state);
                } else {
                    player.sendMessage("§cŞablon oluşturmak için önce bir kontrat oluşturmalısınız!");
                }
                break;
        }
    }
    
    /**
     * Şablonu kullan
     */
    private void useTemplate(Player player, ContractTemplate template) {
        ContractWizardState state = new ContractWizardState();
        state.contractType = template.contractType;
        state.scope = template.scope;
        state.reward = template.reward;
        state.penalty = template.penalty;
        state.deadlineDays = template.deadlineDays;
        state.material = template.material;
        state.amount = template.amount;
        state.targetPlayer = template.targetPlayer;
        state.structureType = template.structureType;
        state.step = 6; // Direkt özet menüsüne git
        
        wizardStates.put(player.getUniqueId(), state);
        player.closeInventory();
        openSummaryMenu(player);
    }
    
    /**
     * State'den şablon kaydet
     */
    private void saveTemplateFromState(Player player, ContractWizardState state) {
        // Şablon ismi iste
        state.waitingForInput = "template_name";
        player.closeInventory();
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§eŞablon İsmi Belirle");
        player.sendMessage("§7Chat'e şablon ismini yazın");
        player.sendMessage("§7İptal etmek için: §c/iptal");
        player.sendMessage("§6═══════════════════════════════════");
    }
    
    /**
     * Kontrat geçmişi menüsü
     */
    private void openContractHistoryMenu(Player player, int page) {
        List<Contract> history = contractHistory.getOrDefault(player.getUniqueId(), new ArrayList<>());
        
        // Sadece bu oyuncunun kontratlarını göster (issuer veya acceptor)
        List<Contract> playerContracts = new ArrayList<>();
        for (Contract contract : history) {
            if (contract.getIssuer().equals(player.getUniqueId()) || 
                (contract.getAcceptor() != null && contract.getAcceptor().equals(player.getUniqueId()))) {
                playerContracts.add(contract);
            }
        }
        
        // Sayfalama
        int totalPages = Math.max(1, (int) Math.ceil(playerContracts.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Kontrat Geçmişi - Sayfa " + page);
        
        // Kontratları göster
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, playerContracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = playerContracts.get(i);
            menu.setItem(slot++, createContractItem(contract));
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
     * Kontrat geçmişi menüsü tıklama
     */
    private void handleContractHistoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (clicked.getType()) {
            case ARROW:
                int slot = event.getSlot();
                String title = event.getView().getTitle();
                int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
                
                if (slot == 45) {
                    // Önceki sayfa
                    openContractHistoryMenu(player, currentPage - 1);
                } else if (slot == 53) {
                    // Sonraki sayfa
                    openContractHistoryMenu(player, currentPage + 1);
                } else if (slot == 49) {
                    // Geri
                    int page = currentPages.getOrDefault(player.getUniqueId(), 1);
                    player.closeInventory();
                    openMainMenu(player, page);
                }
                break;
                
            default:
                // Kontrat detayları
                UUID contractId = getContractIdFromItem(clicked);
                if (contractId != null) {
                    openDetailMenu(player, contractId);
                }
                break;
        }
    }}
