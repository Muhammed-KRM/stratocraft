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
import org.bukkit.event.player.PlayerQuitEvent;
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
    private me.mami.stratocraft.manager.ContractRequestManager contractRequestManager;
    private me.mami.stratocraft.manager.ContractTermsManager contractTermsManager;
    
    // Kontrat oluşturma wizard'ı için geçici depolama
    private final Map<UUID, ContractWizardState> wizardStates = new ConcurrentHashMap<>();
    
    // Kontrat detay menüsü için geçici depolama (player -> contract ID)
    private final Map<UUID, UUID> viewingContract = new ConcurrentHashMap<>();
    
    // Sayfa numaraları (player -> page)
    private final Map<UUID, Integer> currentPages = new ConcurrentHashMap<>();
    
    // İptal istekleri (contractId -> playerId who requested cancel)
    private final Map<UUID, UUID> cancelRequests = new ConcurrentHashMap<>();
    
    // Kontrat şablonları (oyuncu başına)
    private final Map<UUID, List<ContractTemplate>> playerTemplates = new ConcurrentHashMap<>();
    
    // Kontrat geçmişi (oyuncu başına)
    private final Map<UUID, List<Contract>> contractHistory = new ConcurrentHashMap<>();
    
    // Personal Terminal'den mi açıldı? (oyuncu -> isPersonalTerminal)
    private final Map<UUID, Boolean> isPersonalTerminal = new ConcurrentHashMap<>();
    
    /**
     * Kontrat oluşturma wizard durumu
     * Public yapıldı - ContractTermsManager'dan erişim için
     */
    public static class ContractWizardState {
        public me.mami.stratocraft.enums.ContractType contractType; // Yeni enum
        public me.mami.stratocraft.enums.PenaltyType penaltyType; // Yeni enum
        public Contract.ContractScope scope;
        public double reward;
        public double penalty;
        public long deadlineDays;
        public org.bukkit.Material material;
        public int amount;
        public UUID targetPlayer;
        public java.util.List<org.bukkit.Location> restrictedAreas;
        public int restrictedRadius;
        public UUID nonAggressionTarget;
        public String structureType;
        public int step = 0; // Wizard adımı (0 = kategori seçimi, 1 = kapsam, 2 = ödül, 3 = ceza tipi, 4 = ceza miktarı, 5 = süre, 6+ = tip'e özel)
        public String waitingForInput = null; // Chat input bekleniyor mu? ("reward", "penalty", "days", "amount", "player", "location", "structure")
        public Integer materialPage = 0; // Material seçim sayfası
        public ContractTemplate selectedTemplate = null; // Seçilen şablon
        public UUID contractRequestId = null; // Çift taraflı kontrat için request ID
        public UUID targetPlayerForRequest = null; // PLAYER_TO_PLAYER için seçilen hedef oyuncu (istek gönderilmeden önce)
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
        // Manager'ları plugin'den al
        if (plugin != null) {
            this.contractRequestManager = plugin.getContractRequestManager();
            this.contractTermsManager = plugin.getContractTermsManager();
        }
    }
    
    /**
     * HUD'a bildirim gönder
     */
    private void sendContractNotification(Player player, String message, 
                                        me.mami.stratocraft.manager.HUDManager.ContractNotificationType type) {
        if (player == null || message == null || message.isEmpty()) return;
        
        me.mami.stratocraft.manager.HUDManager hudManager = plugin != null ? plugin.getHUDManager() : null;
        if (hudManager != null) {
            hudManager.addContractNotification(player.getUniqueId(), message, type);
        }
    }
    
    /**
     * Manager'ları set et (Main.java'dan çağrılır)
     */
    public void setManagers(me.mami.stratocraft.manager.ContractRequestManager requestManager,
                           me.mami.stratocraft.manager.ContractTermsManager termsManager) {
        this.contractRequestManager = requestManager;
        this.contractTermsManager = termsManager;
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
        openMainMenu(player, page, false);
    }
    
    /**
     * Ana kontrat menüsünü aç
     * @param player Oyuncu
     * @param page Sayfa numarası
     * @param fromPersonalTerminal Personal Terminal'den mi açıldı?
     */
    public void openMainMenu(Player player, int page, boolean fromPersonalTerminal) {
        // Personal Terminal flag'ini kaydet
        if (fromPersonalTerminal) {
            isPersonalTerminal.put(player.getUniqueId(), true);
        } else {
            isPersonalTerminal.put(player.getUniqueId(), false);
        }
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
        
        // ÇİFT TARAFLI KONTRAT SİSTEMİ - YENİ BUTONLAR
        // Atılan İstekler (Slot 40) - YENİ
        if (contractRequestManager != null) {
            List<me.mami.stratocraft.model.ContractRequest> sentRequests = 
                contractRequestManager.getSentPendingRequests(player.getUniqueId());
            if (!sentRequests.isEmpty()) {
                menu.setItem(40, createButton(Material.BOOK, "§6§lAtılan İstekler", 
                    Arrays.asList("§7Gönderdiğiniz kontrat istekleri",
                        "§7Bekleyen: §e" + sentRequests.size(),
                        "§7",
                        "§aSol tıkla aç")));
            }
        }
        
        // Gelen İstekler (Slot 43)
        if (contractRequestManager != null) {
            List<me.mami.stratocraft.model.ContractRequest> pendingRequests = 
                contractRequestManager.getPendingRequests(player.getUniqueId());
            if (!pendingRequests.isEmpty()) {
                menu.setItem(43, createButton(Material.ENCHANTED_BOOK, "§e§lGelen İstekler", 
                    Arrays.asList("§7Size gönderilen kontrat istekleri",
                        "§7Bekleyen: §e" + pendingRequests.size(),
                        "§7",
                        "§aSol tıkla aç")));
            }
        }
        
        // Kabul Edilen İstekler (Slot 44)
        if (contractRequestManager != null) {
            List<me.mami.stratocraft.model.ContractRequest> acceptedRequests = 
                contractRequestManager.getAcceptedRequests(player.getUniqueId());
            if (!acceptedRequests.isEmpty()) {
                menu.setItem(44, createButton(Material.EMERALD_BLOCK, "§a§lKabul Edilen İstekler", 
                    Arrays.asList("§7Kabul ettiğiniz kontrat istekleri",
                        "§7Şartlarınızı belirleyebilirsiniz",
                        "§7Bekleyen: §a" + acceptedRequests.size(),
                        "§7",
                        "§aSol tıkla aç")));
            }
        }
        
        // Aktif Kontratlar (Slot 41) - YENİ
        List<Contract> activeContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract == null) continue;
            
            // Tek taraflı aktif kontratlar
            if (!contract.isBilateralContract() && 
                (contract.getIssuer().equals(player.getUniqueId()) || 
                 (contract.getAcceptor() != null && contract.getAcceptor().equals(player.getUniqueId()))) &&
                !contract.isExpired() && !contract.isBreached() && !contract.isCompleted()) {
                activeContracts.add(contract);
            }
            // Çift taraflı aktif kontratlar
            else if (contract.isBilateralContract() && 
                (contract.getPlayerA().equals(player.getUniqueId()) || 
                 contract.getPlayerB().equals(player.getUniqueId())) &&
                contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                activeContracts.add(contract);
            }
        }
        
        if (!activeContracts.isEmpty()) {
            menu.setItem(41, createButton(Material.DIAMOND, "§b§lAktif Kontratlar", 
                Arrays.asList("§7Aktif kontratlarınızı görüntüle",
                    "§7Aktif: §b" + activeContracts.size(),
                    "§7",
                    "§aSol tıkla aç")));
        }
        
        // Eski Kontratlar (Slot 42) - YENİ (İptal edilenler ve tamamlananlar)
        List<Contract> oldContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract == null) continue;
            
            boolean isMyContract = false;
            if (!contract.isBilateralContract()) {
                isMyContract = contract.getIssuer().equals(player.getUniqueId()) || 
                    (contract.getAcceptor() != null && contract.getAcceptor().equals(player.getUniqueId()));
            } else {
                isMyContract = contract.getPlayerA().equals(player.getUniqueId()) || 
                    contract.getPlayerB().equals(player.getUniqueId());
            }
            
            if (isMyContract) {
                // İptal edilenler
                if (contract.isBilateralContract() && 
                    contract.getContractStatus() == Contract.ContractStatus.CANCELLED) {
                    oldContracts.add(contract);
                }
                // Tamamlananlar
                else if (contract.isCompleted() || 
                    (contract.isBilateralContract() && 
                     contract.getContractStatus() == Contract.ContractStatus.COMPLETED)) {
                    oldContracts.add(contract);
                }
                // İhlal edilenler
                else if (contract.isBreached() || 
                    (contract.isBilateralContract() && 
                     contract.getContractStatus() == Contract.ContractStatus.BREACHED)) {
                    oldContracts.add(contract);
                }
            }
        }
        
        if (!oldContracts.isEmpty()) {
            long cancelledCount = oldContracts.stream().filter(c -> 
                c.isBilateralContract() && c.getContractStatus() == Contract.ContractStatus.CANCELLED).count();
            long completedCount = oldContracts.stream().filter(c -> 
                c.isCompleted() || (c.isBilateralContract() && c.getContractStatus() == Contract.ContractStatus.COMPLETED)).count();
            long breachedCount = oldContracts.stream().filter(c -> 
                c.isBreached() || (c.isBilateralContract() && c.getContractStatus() == Contract.ContractStatus.BREACHED)).count();
            
            menu.setItem(42, createButton(Material.PAPER, "§7§lEski Kontratlar", 
                Arrays.asList("§7Eski kontratlarınızı görüntüle",
                    "§7",
                    "§7İptal Edilen: §c" + cancelledCount,
                    "§7Tamamlanan: §a" + completedCount,
                    "§7İhlal Edilen: §c" + breachedCount,
                    "§7Toplam: §e" + oldContracts.size(),
                    "§7",
                    "§aSol tıkla aç")));
        }
        
        // Benim Kontratlarım (Slot 46) - Oluşturduğu kontratlar + çift taraflı kontratlar
        List<Contract> myContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract == null) continue;
            
            // Tek taraflı kontratlar
            if (!contract.isBilateralContract() && contract.getIssuer().equals(player.getUniqueId()) && 
                !contract.isExpired() && !contract.isBreached()) {
                myContracts.add(contract);
            }
            // Çift taraflı kontratlar (playerA veya playerB)
            else if (contract.isBilateralContract() && 
                (contract.getPlayerA().equals(player.getUniqueId()) || 
                 contract.getPlayerB().equals(player.getUniqueId())) &&
                contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                myContracts.add(contract);
            }
        }
        
        // Kabul edilen kontratlar (Slot 52) - Bu oyuncunun kabul ettiği kontratlar (tek taraflı)
        List<Contract> acceptedContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract != null && !contract.isBilateralContract() && 
                contract.getAcceptor() != null && 
                contract.getAcceptor().equals(player.getUniqueId()) && 
                !contract.isExpired() && !contract.isBreached()) {
                acceptedContracts.add(contract);
            }
        }
        
        if (!myContracts.isEmpty()) {
            long bilateralCount = myContracts.stream().filter(c -> c.isBilateralContract()).count();
            long singleCount = myContracts.stream().filter(c -> !c.isBilateralContract()).count();
            menu.setItem(46, createButton(Material.GOLDEN_APPLE, "§6§lBenim Kontratlarım", 
                Arrays.asList("§7Oluşturduğum kontratları görüntüle", 
                    "§7",
                    "§7Tek Taraflı: §e" + singleCount,
                    "§7Çift Taraflı: §e" + bilateralCount,
                    "§7Aktif: §e" + myContracts.stream().filter(c -> 
                        (c.isBilateralContract() && c.getContractStatus() == Contract.ContractStatus.ACTIVE) ||
                        (!c.isBilateralContract() && c.getAcceptor() != null && !c.isCompleted())).count(),
                    "§7Tamamlanan: §a" + myContracts.stream().filter(c -> 
                        (c.isBilateralContract() && c.getContractStatus() == Contract.ContractStatus.COMPLETED) ||
                        (!c.isBilateralContract() && c.isCompleted())).count(),
                    "§7Toplam: §e" + myContracts.size(),
                    "§7",
                    "§aSol tıkla aç")));
        }
        
        // Kabul Edilen Kontratlarım (Slot 52)
        if (!acceptedContracts.isEmpty()) {
            menu.setItem(52, createButton(Material.EMERALD, "§a§lKabul Ettiğim Kontratlar", 
                Arrays.asList("§7Kabul ettiğim kontratları görüntüle", 
                    "§7",
                    "§7Devam Eden: §e" + acceptedContracts.stream().filter(c -> !c.isCompleted()).count(),
                    "§7Tamamlanan: §a" + acceptedContracts.stream().filter(c -> c.isCompleted()).count(),
                    "§7Toplam: §e" + acceptedContracts.size(),
                    "§7",
                    "§aSol tıkla aç")));
        }
        
        // Kontrat geçmişi (Slot 51)
        List<Contract> history = contractHistory.getOrDefault(player.getUniqueId(), new ArrayList<>());
        if (!history.isEmpty()) {
            menu.setItem(51, createButton(Material.PAPER, "§6Kontrat Geçmişi", 
                Arrays.asList("§7Geçmiş kontratlarınızı görüntüle", "§7Toplam: §e" + history.size())));
        }
        
        // Bilgi butonu (Slot 50)
        menu.setItem(50, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Açık Kontratlar: §e" + openContracts.size(),
                "§7Benim Kontratlarım: §e" + myContracts.size(),
                "§7Kabul Ettiğim: §a" + acceptedContracts.size(),
                "§7Sayfa: §e" + page + "§7/§e" + totalPages,
                "§7",
                "§7§lNasıl Kullanılır:",
                "§7- Pusuladan (CONTRACT_PAPER) sağ tıkla",
                "§7- Yeni kontrat oluşturmak için §aYazı Kitabı",
                "§7- Oluşturduğun kontratlar: §6Altın Elma",
                "§7- Kabul ettiğin kontratlar: §aZümrüt")));
        
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
        
        // Çift taraflı kontrat için özel menü
        if (contract.isBilateralContract()) {
            openBilateralContractDetailMenu(player, contract);
            return;
        }
        
        // 27 slotlu menü (3x9)
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Detayları");
        
        // Kontrat bilgileri (Slot 13)
        ItemStack infoItem = createContractDetailItem(contract);
        menu.setItem(13, infoItem);
        
        // Kontrat sahibi kontrolü
        boolean isIssuer = contract.getIssuer().equals(player.getUniqueId());
        boolean isAcceptor = contract.getAcceptor() != null && contract.getAcceptor().equals(player.getUniqueId());
        
        // İssuer ve Acceptor bilgilerini göster (Slot 4 ve 22)
        OfflinePlayer issuer = Bukkit.getOfflinePlayer(contract.getIssuer());
        menu.setItem(4, createButton(Material.PLAYER_HEAD, "§eKontratı Oluşturan", 
            Arrays.asList("§7Oyuncu: §e" + (issuer.getName() != null ? issuer.getName() : "Bilinmeyen"),
                "§7Kapsam: §e" + getContractScopeName(contract.getScope()),
                "§7",
                "§7Bu kontratı oluşturan kişi",
                "§7ödülü ödeyecek.")));
        
        if (contract.getAcceptor() != null) {
            OfflinePlayer acceptor = Bukkit.getOfflinePlayer(contract.getAcceptor());
            menu.setItem(22, createButton(Material.EMERALD, "§aKontratı Kabul Eden", 
                Arrays.asList("§7Oyuncu: §a" + (acceptor.getName() != null ? acceptor.getName() : "Bilinmeyen"),
                    "§7",
                    "§7Bu kontratı kabul eden kişi",
                    "§7görevi tamamlayacak.")));
        } else {
            menu.setItem(22, createButton(Material.GRAY_DYE, "§7Henüz Kabul Edilmedi", 
                Arrays.asList("§7Bu kontrat henüz kimse",
                    "§7tarafından kabul edilmedi.",
                    "§7",
                    "§aKabul Et §7butonuna tıklayarak",
                    "§7kabul edebilirsiniz.")));
        }
        
        // Kabul Et butonu (Slot 11 - sadece açık kontratlar için ve issuer değilse)
        if (contract.getAcceptor() == null && !contract.isExpired() && !contract.isBreached() && !isIssuer) {
            menu.setItem(11, createButton(Material.EMERALD_BLOCK, "§a[Kabul Et]", 
                Arrays.asList("§7Bu kontratı kabul et",
                    "§7Kan imzası gerekli (1 kalp kaybı)",
                    "§cDikkat: İhlal durumunda ceza uygulanır!",
                    "§7",
                    "§7Kabul ettikten sonra görevi",
                    "§7tamamlaman gerekecek.")));
        }
        
        // Kontrat sahibi için bilgi (Slot 11 - issuer ise)
        if (isIssuer && contract.getAcceptor() == null) {
            menu.setItem(11, createButton(Material.BOOK, "§eKontratınız", 
                Arrays.asList("§7Bu kontratı siz oluşturdunuz",
                    "§7Birisi kabul ettiğinde bildirim",
                    "§7alacaksınız.")));
        }
        
        // Kontrat durumu (Slot 15)
        if (isIssuer) {
            if (contract.getAcceptor() != null) {
                OfflinePlayer acceptor = Bukkit.getOfflinePlayer(contract.getAcceptor());
                menu.setItem(15, createButton(Material.GREEN_CONCRETE, "§aKabul Edildi", 
                    Arrays.asList("§7Kabul Eden: §e" + (acceptor.getName() != null ? acceptor.getName() : "Bilinmeyen"),
                        "§7Kontrat devam ediyor...")));
            } else {
                menu.setItem(15, createButton(Material.YELLOW_CONCRETE, "§eBeklemede", 
                    Arrays.asList("§7Henüz kimse kabul etmedi",
                        "§7Kontrat açık ve görülebilir.")));
            }
        } else {
            // Reddet butonu (Slot 15 - sadece açık kontratlar için)
            if (contract.getAcceptor() == null && !contract.isExpired() && !contract.isBreached()) {
                menu.setItem(15, createButton(Material.REDSTONE_BLOCK, "§c[Reddet]", 
                    Arrays.asList("§7Kontratı reddet")));
            }
        }
        
        // Geri butonu (Slot 22)
        menu.setItem(22, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Listeye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Çift taraflı kontrat detay menüsünü aç
     */
    private void openBilateralContractDetailMenu(Player player, Contract contract) {
        if (player == null || contract == null || !contract.isBilateralContract()) return;
        
        viewingContract.put(player.getUniqueId(), contract.getId());
        
        // 54 slotlu menü (6x9)
        Inventory menu = Bukkit.createInventory(null, 54, "§6Çift Taraflı Kontrat Detayları");
        
        // Oyuncu kontrolü
        boolean isPlayerA = contract.getPlayerA().equals(player.getUniqueId());
        boolean isPlayerB = contract.getPlayerB().equals(player.getUniqueId());
        if (!isPlayerA && !isPlayerB) {
            player.sendMessage("§cBu kontratın tarafı değilsiniz!");
            return;
        }
        
        UUID otherPlayerId = isPlayerA ? contract.getPlayerB() : contract.getPlayerA();
        org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
        String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
        
        // Kontrat bilgileri (Slot 4)
        List<String> contractInfo = new ArrayList<>();
        contractInfo.add("§6═══════════════════════════════════");
        contractInfo.add("§7Kontrat ID: §f" + contract.getId().toString().substring(0, 8) + "...");
        contractInfo.add("§7Durum: §f" + (contract.getContractStatus() != null ? 
            getContractStatusName(contract.getContractStatus()) : "Bilinmiyor"));
        contractInfo.add("§7");
        contractInfo.add("§7Oyuncu A: §e" + (isPlayerA ? "§aSen" : 
            (Bukkit.getOfflinePlayer(contract.getPlayerA()).getName() != null ? 
                Bukkit.getOfflinePlayer(contract.getPlayerA()).getName() : "Bilinmeyen")));
        contractInfo.add("§7Oyuncu B: §e" + (isPlayerB ? "§aSen" : otherName));
        contractInfo.add("§6═══════════════════════════════════");
        menu.setItem(4, createButton(Material.PAPER, "§e§lKontrat Bilgileri", contractInfo));
        
        // Şartlar A (Slot 11)
        if (contract.getTermsA() != null) {
            List<String> termsALore = createTermsLore(contract.getTermsA(), isPlayerA);
            menu.setItem(11, createButton(Material.BOOK, 
                "§e" + (isPlayerA ? "§lSenin Şartların" : "§7Karşı Tarafın Şartları"), 
                termsALore));
        }
        
        // Şartlar B (Slot 15)
        if (contract.getTermsB() != null) {
            List<String> termsBLore = createTermsLore(contract.getTermsB(), isPlayerB);
            menu.setItem(15, createButton(Material.BOOK, 
                "§e" + (isPlayerB ? "§lSenin Şartların" : "§7Karşı Tarafın Şartları"), 
                termsBLore));
        }
        
        // Teslim Et butonu (Slot 20 - sadece aktif kontratlar için ve RESOURCE_COLLECTION için)
        if (contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
            me.mami.stratocraft.model.ContractTerms myTerms = isPlayerA ? contract.getTermsA() : contract.getTermsB();
            if (myTerms != null && myTerms.getType() == me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION && 
                !myTerms.isCompleted()) {
                menu.setItem(20, createButton(Material.EMERALD, "§a§lTeslim Et", 
                    Arrays.asList("§7Malzemeleri teslim etmek için",
                        "§7/kontrat teslim komutunu kullanın",
                        "§7",
                        "§7Gerekli: §e" + myTerms.getAmount() + "x " + 
                        (myTerms.getMaterial() != null ? myTerms.getMaterial().name() : "Malzeme"),
                        "§7Teslim Edilen: §a" + myTerms.getDelivered() + "/" + myTerms.getAmount())));
            }
        }
        
        // Şartları Detaylı Görüntüle butonları (Slot 12 ve 16)
        if (contract.getTermsA() != null) {
            menu.setItem(12, createButton(Material.BOOK, "§eŞartlar A Detayları", 
                Arrays.asList("§7Sol tıkla: Şartlar A'yı",
                    "§7detaylı görüntüle")));
        }
        if (contract.getTermsB() != null) {
            menu.setItem(16, createButton(Material.BOOK, "§eŞartlar B Detayları", 
                Arrays.asList("§7Sol tıkla: Şartlar B'yi",
                    "§7detaylı görüntüle")));
        }
        
        // İptal butonları
        UUID cancelRequester = cancelRequests.get(contract.getId());
        boolean hasCancelRequest = cancelRequester != null;
        boolean isCancelRequester = hasCancelRequest && cancelRequester.equals(player.getUniqueId());
        boolean isOtherCancelRequester = hasCancelRequest && cancelRequester.equals(otherPlayerId);
        
        // İptal İsteği Gönder/Onayla/Geri Çek (Slot 24)
        if (contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
            if (!hasCancelRequest) {
                // İptal isteği gönder
                menu.setItem(24, createButton(Material.REDSTONE_BLOCK, "§c§lİptal İsteği Gönder", 
                    Arrays.asList("§7Kontratı iptal etmek için",
                        "§7karşı tarafın onayı gerekir",
                        "§7",
                        "§cSol tıkla: İptal isteği gönder")));
            } else if (isCancelRequester) {
                // İptal isteği bekleniyor - geri çek butonu
                menu.setItem(24, createButton(Material.YELLOW_CONCRETE, "§e§lİptal İsteği Bekleniyor", 
                    Arrays.asList("§7Karşı tarafın onayını",
                        "§7bekliyorsunuz",
                        "§7",
                        "§7Oyuncu: §e" + otherName,
                        "§7",
                        "§cSol tıkla: İptal isteğini geri çek")));
            } else if (isOtherCancelRequester) {
                // İptal isteğini onayla
                menu.setItem(24, createButton(Material.EMERALD_BLOCK, "§a§lİptal İsteğini Onayla", 
                    Arrays.asList("§7" + otherName + " kontratı iptal",
                        "§7etmek istiyor",
                        "§7",
                        "§aSol tıkla: İptal isteğini onayla",
                        "§cDikkat: Kontrat tamamen iptal edilecek!")));
            }
        }
        
        // İptal İsteğini Reddet (Slot 25 - sadece karşı taraf iptal isteği gönderdiyse)
        if (contract.getContractStatus() == Contract.ContractStatus.ACTIVE && isOtherCancelRequester) {
            menu.setItem(25, createButton(Material.REDSTONE, "§cİptal İsteğini Reddet", 
                Arrays.asList("§7Karşı tarafın iptal isteğini",
                    "§7reddetmek için",
                    "§7",
                    "§cSol tıkla: İptal isteğini reddet")));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Şartlar için lore oluştur
     */
    private List<String> createTermsLore(me.mami.stratocraft.model.ContractTerms terms, boolean isMyTerms) {
        List<String> lore = new ArrayList<>();
        lore.add("§6═══════════════════════════════════");
        lore.add("§7Tip: §f" + (terms.getType() != null ? getContractTypeName(terms.getType()) : "Bilinmiyor"));
        
        if (terms.getType() == me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION) {
            lore.add("§7Malzeme: §f" + (terms.getMaterial() != null ? terms.getMaterial().name() : "Bilinmiyor"));
            lore.add("§7Miktar: §f" + terms.getAmount());
            lore.add("§7Teslim Edilen: §f" + terms.getDelivered() + "/" + terms.getAmount());
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.COMBAT) {
            if (terms.getTargetPlayer() != null) {
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(terms.getTargetPlayer());
                lore.add("§7Hedef: §f" + (target.getName() != null ? target.getName() : "Bilinmiyor"));
            }
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.CONSTRUCTION) {
            lore.add("§7Yapı Tipi: §f" + (terms.getStructureType() != null ? terms.getStructureType() : "Bilinmiyor"));
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.TERRITORY) {
            lore.add("§7Yasaklı Bölge: §f" + (terms.getRestrictedAreas() != null && !terms.getRestrictedAreas().isEmpty() ? 
                "Belirlenmiş" : "Yok"));
            lore.add("§7Yarıçap: §f" + terms.getRestrictedRadius());
        }
        
        lore.add("§7Süre: §f" + formatTimeRemaining(terms.getDeadline() - System.currentTimeMillis()));
        lore.add("§7Ödül: §f" + terms.getReward() + " altın");
        lore.add("§7Ceza: §f" + terms.getPenalty() + " altın");
        lore.add("§7");
        lore.add("§7Durum: " + (terms.isCompleted() ? "§aTamamlandı" : 
            (terms.isBreached() ? "§cİhlal Edildi" : "§eAktif")));
        lore.add("§6═══════════════════════════════════");
        return lore;
    }
    
    /**
     * Kontrat durumu ismi
     */
    private String getContractStatusName(Contract.ContractStatus status) {
        if (status == null) return "Bilinmiyor";
        switch (status) {
            case PENDING_TERMS_A:
                return "§eŞartlar A Bekleniyor";
            case PENDING_TERMS_B:
                return "§eŞartlar B Bekleniyor";
            case PENDING_APPROVAL:
                return "§6Onay Bekleniyor";
            case ACTIVE:
                return "§aAktif";
            case COMPLETED:
                return "§aTamamlandı";
            case BREACHED:
                return "§cİhlal Edildi";
            default:
                return "Bilinmiyor";
        }
    }
    
    /**
     * Kalan süre formatla
     */
    private String formatTimeRemaining(long millis) {
        if (millis <= 0) return "§cSüresi Doldu";
        long days = millis / (24 * 60 * 60 * 1000);
        long hours = (millis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);
        if (days > 0) return days + " gün " + hours + " saat";
        if (hours > 0) return hours + " saat " + minutes + " dakika";
        return minutes + " dakika";
    }
    
    /**
     * Kontrat oluşturma wizard'ını başlat
     */
    public void startCreationWizard(Player player) {
        startCreationWizard(player, false);
    }
    
    /**
     * Kontrat oluşturma wizard'ını başlat
     * @param player Oyuncu
     * @param fromPersonalTerminal Personal Terminal'den mi açıldı?
     */
    public void startCreationWizard(Player player, boolean fromPersonalTerminal) {
        if (player == null) return;
        
        // Personal Terminal kontrolü
        if (fromPersonalTerminal) {
            // Personal Terminal'den sadece oyuncu-oyuncu kontratları yapılabilir
            // Klan kontrolü gerekmez
        } else {
            // CONTRACT_OFFICE'den tüm kontrat tipleri yapılabilir
            // Klan kontrolü gerekli (klan kontratları için)
            Clan clan = clanManager != null ? clanManager.getClanByPlayer(player.getUniqueId()) : null;
            if (clan == null) {
                player.sendMessage("§cKlan kontratları oluşturmak için bir klana üye olmalısınız!");
                player.sendMessage("§7Oyuncu-oyuncu kontratları için Personal Terminal kullanın.");
                return;
            }
        }
        
        // Wizard durumu oluştur
        ContractWizardState state = new ContractWizardState();
        wizardStates.put(player.getUniqueId(), state);
        
        // Personal Terminal flag'ini kaydet
        isPersonalTerminal.put(player.getUniqueId(), fromPersonalTerminal);
        
        // Önce kapsam seçim menüsünü aç (çift taraflı kontrat için)
        openScopeSelectionMenu(player);
    }
    
    /**
     * Kontrat kategori seçim menüsü (YENİ: ContractType enum kullanır)
     */
    private void openTypeSelectionMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        // ✅ İYİLEŞTİRME: Adım numarası ekle
        int currentStep = getCurrentStepNumber(state);
        int totalSteps = getTotalWizardSteps(state);
        String menuTitle = "§6[Adım " + currentStep + "/" + totalSteps + "] Kontrat Tipi Seç";
        
        Inventory menu = Bukkit.createInventory(null, 27, menuTitle);
        
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
                        lore.add("§7§lGörev:");
                        lore.add("§7• Kabul eden kişi belirtilen");
                        lore.add("§7  malzemeyi toplayıp verecek");
                        lore.add("§7• Veren kişi ödülü ödeyecek");
                        lore.add("§7");
                        lore.add("§7Malzeme: §e" + getMaterialDisplayName(contract.getMaterial()));
                        lore.add("§7Miktar: §e" + contract.getAmount());
                        lore.add("§7Teslim: §a" + contract.getDelivered() + "§7/§a" + contract.getAmount());
                    }
                    break;
                case COMBAT:
                    if (contract.getTargetPlayer() != null) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(contract.getTargetPlayer());
                        lore.add("§7═══════════════════════");
                        lore.add("§7§lGörev:");
                        lore.add("§7• Kabul eden kişi hedefi");
                        lore.add("§7  öldürecek veya vuracak");
                        lore.add("§7• Veren kişi ödülü ödeyecek");
                        lore.add("§7");
                        lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                    }
                    break;
                case TERRITORY:
                    lore.add("§7═══════════════════════");
                    lore.add("§7§lGörev:");
                    lore.add("§7• Kabul eden kişi belirtilen");
                    lore.add("§7  bölgeye gitmeyecek");
                    lore.add("§7• Veren kişi ödülü ödeyecek");
                    lore.add("§7");
                    lore.add("§7Yasak Bölgeler: §c" + (contract.getRestrictedAreas() != null ? contract.getRestrictedAreas().size() : 0) + " adet");
                    lore.add("§7Yarıçap: §e" + contract.getRestrictedRadius() + " blok");
                    break;
                case CONSTRUCTION:
                    if (contract.getStructureType() != null) {
                        lore.add("§7═══════════════════════");
                        lore.add("§7§lGörev:");
                        lore.add("§7• Kabul eden kişi belirtilen");
                        lore.add("§7  yapıyı inşa edecek");
                        lore.add("§7• Veren kişi ödülü ödeyecek");
                        lore.add("§7");
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
            lore.add("§7Durum: §aAçık (Kabul bekleniyor)");
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
        // Benim Kontratlarım
        else if (title.startsWith("§6Benim Kontratlarım")) {
            handleMyContractsMenuClick(event);
        }
        // Kabul Edilen Kontratlarım
        else if (title.startsWith("§aKabul Ettiğim Kontratlar")) {
            handleAcceptedContractsMenuClick(event);
        }
        // Gelen İstekler (ÇİFT TARAFLI KONTRAT)
        else if (title.startsWith("§eGelen İstekler")) {
            handleIncomingRequestsClick(event);
        }
        // Kabul Edilen İstekler (ÇİFT TARAFLI KONTRAT)
        else if (title.startsWith("§aKabul Edilen İstekler")) {
            handleAcceptedRequestsClick(event);
        }
        // Atılan İstekler (YENİ)
        else if (title.startsWith("§6Atılan İstekler")) {
            handleSentRequestsClick(event);
        }
        // Aktif Kontratlar (YENİ)
        else if (title.startsWith("§bAktif Kontratlar")) {
            handleActiveContractsClick(event);
        }
        // Eski Kontratlar (YENİ)
        else if (title.startsWith("§7Eski Kontratlar")) {
            handleOldContractsClick(event);
        }
        // Kontrat Şartları (ÇİFT TARAFLI KONTRAT)
        else if (title.equals("§6Kontrat Şartları")) {
            handleTermsViewClick(event);
        }
        // Kontrat Karar Menüsü (Şart Ekle veya Kontratı Bitir)
        else if (title.equals("§6Kontrat Kararı")) {
            handleContractDecisionClick(event);
        }
        // Son Onay Menüsü (İlk gönderen oyuncu için - YENİ)
        else if (title.equals("§6Son Onay")) {
            handleFinalApprovalClick(event);
        }
        // Oyuncu seçim menüsü
        else if (title.equals("§6Hedef Oyuncu Seç")) {
            handlePlayerSelectionClick(event);
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
                // Personal Terminal kontrolü
                boolean fromPersonalTerminal = isPersonalTerminal.getOrDefault(player.getUniqueId(), false);
                startCreationWizard(player, fromPersonalTerminal);
                break;
                
            case BOOK:
                // Atılan İstekler (YENİ)
                if (event.getSlot() == 40) {
                    openSentRequestsMenu(player, 1);
                }
                // Şablonlardan oluştur
                else if (event.getSlot() == 47) {
                    openTemplateMenu(player);
                }
                break;
                
            case DIAMOND:
                // Aktif Kontratlar (YENİ)
                if (event.getSlot() == 41) {
                    openActiveContractsMenu(player, 1);
                }
                break;
                
            case PAPER:
                // Eski Kontratlar (YENİ)
                if (event.getSlot() == 42) {
                    openOldContractsMenu(player, 1);
                }
                // Kontrat geçmişi
                else if (event.getSlot() == 51) {
                    openContractHistoryMenu(player, 1);
                }
                break;
                
            case GOLDEN_APPLE:
                // Benim Kontratlarım
                if (event.getSlot() == 46) {
                    openMyContractsMenu(player, 1);
                }
                break;
                
            case EMERALD:
                // Kabul Edilen Kontratlarım
                if (event.getSlot() == 52) {
                    openAcceptedContractsMenu(player, 1);
                }
                break;
                
            case ENCHANTED_BOOK:
                // Gelen İstekler
                if (event.getSlot() == 43) {
                    openIncomingRequestsMenu(player, 1);
                }
                break;
                
            case EMERALD_BLOCK:
                // Kabul Edilen İstekler
                if (event.getSlot() == 44) {
                    openAcceptedRequestsMenu(player, 1);
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
        
        // Çift taraflı kontrat tıklamaları
        if (contract.isBilateralContract()) {
            handleBilateralContractDetailClick(event, contract, player);
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
     * Çift taraflı kontrat detay menüsü tıklama
     */
    private void handleBilateralContractDetailClick(InventoryClickEvent event, Contract contract, Player player) {
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        boolean isPlayerA = contract.getPlayerA().equals(player.getUniqueId());
        boolean isPlayerB = contract.getPlayerB().equals(player.getUniqueId());
        UUID otherPlayerId = isPlayerA ? contract.getPlayerB() : contract.getPlayerA();
        org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
        String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
        
        UUID cancelRequester = cancelRequests.get(contract.getId());
        boolean hasCancelRequest = cancelRequester != null;
        boolean isCancelRequester = hasCancelRequest && cancelRequester.equals(player.getUniqueId());
        boolean isOtherCancelRequester = hasCancelRequest && cancelRequester.equals(otherPlayerId);
        
        switch (clicked.getType()) {
            case REDSTONE_BLOCK:
                // İptal isteği gönder
                if (!hasCancelRequest && contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                    cancelRequests.put(contract.getId(), player.getUniqueId());
                    player.sendMessage("§eİptal isteği gönderildi! " + otherName + " onaylamalı.");
                    
                    // Karşı tarafa bildirim
                    org.bukkit.entity.Player otherPlayerOnline = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayerOnline != null && otherPlayerOnline.isOnline()) {
                        otherPlayerOnline.sendMessage("§6═══════════════════════════════════");
                        otherPlayerOnline.sendMessage("§e§lİPTAL İSTEĞİ");
                        otherPlayerOnline.sendMessage("§7" + player.getName() + " kontratı iptal");
                        otherPlayerOnline.sendMessage("§7etmek istiyor.");
                        otherPlayerOnline.sendMessage("§7");
                        otherPlayerOnline.sendMessage("§7Kontrat menüsünden onaylayabilirsiniz.");
                        otherPlayerOnline.sendMessage("§6═══════════════════════════════════");
                    }
                    
                    // Menüyü yenile
                    openBilateralContractDetailMenu(player, contract);
                }
                break;
                
            case YELLOW_CONCRETE:
                // İptal isteğini geri çek
                if (isCancelRequester && contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                    cancelRequests.remove(contract.getId());
                    player.sendMessage("§eİptal isteği geri çekildi.");
                    
                    // Karşı tarafa bildirim
                    org.bukkit.entity.Player otherPlayerOnline = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayerOnline != null && otherPlayerOnline.isOnline()) {
                        otherPlayerOnline.sendMessage("§7" + player.getName() + " iptal isteğini geri çekti.");
                    }
                    
                    // Menüyü yenile
                    openBilateralContractDetailMenu(player, contract);
                }
                break;
                
            case EMERALD_BLOCK:
                // İptal isteğini onayla
                if (isOtherCancelRequester && contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                    // İptal onaylandı - kontratı iptal et
                    if (contractManager != null) {
                        contractManager.getContracts().remove(contract);
                    }
                    
                    // İptal isteğini kaldır
                    cancelRequests.remove(contract.getId());
                    
                    player.sendMessage("§aKontrat iptal edildi!");
                    
                    // Karşı tarafa bildirim
                    org.bukkit.entity.Player otherPlayerOnline = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayerOnline != null && otherPlayerOnline.isOnline()) {
                        otherPlayerOnline.sendMessage("§aKontrat iptal edildi! " + player.getName() + " onayladı.");
                    }
                    
                    // Kan imzası geri ver (her iki tarafa) - ContractManager üzerinden
                    if (contractManager != null) {
                        contractManager.restorePermanentHealth(contract.getPlayerA(), 1);
                        contractManager.restorePermanentHealth(contract.getPlayerB(), 1);
                    }
                    
                    player.closeInventory();
                    viewingContract.remove(player.getUniqueId());
                }
                break;
                
            case REDSTONE:
                // İptal isteğini reddet
                if (isOtherCancelRequester && contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                    cancelRequests.remove(contract.getId());
                    player.sendMessage("§cİptal isteği reddedildi. Kontrat devam ediyor.");
                    
                    // Karşı tarafa bildirim
                    org.bukkit.entity.Player otherPlayerOnline = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayerOnline != null && otherPlayerOnline.isOnline()) {
                        otherPlayerOnline.sendMessage("§c" + player.getName() + " iptal isteğinizi reddetti. Kontrat devam ediyor.");
                    }
                    
                    // Menüyü yenile
                    openBilateralContractDetailMenu(player, contract);
                }
                break;
                
            case BOOK:
                // Şartları detaylı görüntüle
                int slot = event.getSlot();
                if (slot == 12 && contract.getTermsA() != null) {
                    // Şartlar A detayları
                    player.sendMessage("§6═══════════════════════════════════");
                    player.sendMessage("§e§lŞARTLAR A DETAYLARI");
                    player.sendMessage("§6═══════════════════════════════════");
                    displayTermsDetails(player, contract.getTermsA());
                } else if (slot == 16 && contract.getTermsB() != null) {
                    // Şartlar B detayları
                    player.sendMessage("§6═══════════════════════════════════");
                    player.sendMessage("§e§lŞARTLAR B DETAYLARI");
                    player.sendMessage("§6═══════════════════════════════════");
                    displayTermsDetails(player, contract.getTermsB());
                }
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
     * Kalıcı can geri ver (kan imzası) - ContractManager üzerinden
     * @deprecated ContractManager.restorePermanentHealth kullanın
     */
    @Deprecated
    private void restorePermanentHealth(UUID playerId, int hearts) {
        // Bu metod artık kullanılmıyor, ContractManager.restorePermanentHealth kullanılmalı
        if (contractManager != null) {
            contractManager.restorePermanentHealth(playerId, hearts);
        }
    }
    
    /**
     * Wizard adım sayısını hesapla (iyileştirme için)
     * ✅ DÜZELTME: Yeni sıralama [Kapsam]>[Oyuncu]>[Tip]>[Ödül]>[Ceza Tipi]>[Ceza]>[Süre]>[Tip'e Özel]>[Özet]
     */
    private int getTotalWizardSteps(ContractWizardState state) {
        if (state == null) return 9;
        
        // Eğer contractRequestId varsa (şart ekleme durumu), scope ve oyuncu seçimi yok
        if (state.contractRequestId != null) {
            // Tip seçimi (1) + Ödül (2) + Ceza Tipi (3) + Ceza (4) + Süre (5) + Tip'e özel (6) + Özet (7) = 7 adım
            return 7;
        }
        
        // Normal akış: Kapsam (1) + Oyuncu (2, sadece PLAYER_TO_PLAYER) + Tip (3) + Ödül (4) + Ceza Tipi (5) + Ceza (6) + Süre (7) + Tip'e özel (8) + Özet (9) = 9 adım
        // Ancak scope PLAYER_TO_PLAYER ise oyuncu seçimi var, diğerlerinde yok
        if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER) {
            return 9; // Kapsam, Oyuncu, Tip, Ödül, Ceza Tipi, Ceza, Süre, Tip'e özel, Özet
        } else {
            return 8; // Kapsam, Tip, Ödül, Ceza Tipi, Ceza, Süre, Tip'e özel, Özet (Oyuncu seçimi yok)
        }
    }
    
    /**
     * Mevcut adım numarasını hesapla (iyileştirme için)
     * ✅ DÜZELTME: Yeni sıralama [Kapsam]>[Oyuncu]>[Tip]>[Ödül]>[Ceza Tipi]>[Ceza]>[Süre]>[Tip'e Özel]>[Özet]
     */
    private int getCurrentStepNumber(ContractWizardState state) {
        if (state == null) return 1;
        
        // Eğer contractRequestId varsa (şart ekleme durumu)
        if (state.contractRequestId != null) {
            if (state.contractType == null) return 1; // Tip seçimi
            if (state.step < 2) return 2; // Ödül
            if (state.step < 3) return 3; // Ceza Tipi
            if (state.step < 4) return 4; // Ceza
            if (state.step < 5) return 5; // Süre
            if (state.step < 6) return 6; // Tip'e özel
            return 7; // Özet
        }
        
        // Normal akış: [Kapsam]>[Oyuncu]>[Tip]>[Ödül]>[Ceza Tipi]>[Ceza]>[Süre]>[Tip'e Özel]>[Özet]
        if (state.scope == null) return 1; // Kapsam seçimi
        if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER && state.targetPlayerForRequest == null) return 2; // Oyuncu seçimi
        if (state.contractType == null) return 3; // Tip seçimi
        if (state.step < 2) return 4; // Ödül
        if (state.step < 3) return 5; // Ceza Tipi
        if (state.step < 4) return 6; // Ceza
        if (state.step < 5) return 7; // Süre
        if (state.step < 6) return 8; // Tip'e özel
        return 9; // Özet
    }
    
    /**
     * Şartları detaylı göster
     */
    private void displayTermsDetails(Player player, me.mami.stratocraft.model.ContractTerms terms) {
        if (player == null || terms == null) return;
        
        player.sendMessage("§7Tip: §f" + (terms.getType() != null ? getContractTypeName(terms.getType()) : "Bilinmiyor"));
        
        if (terms.getType() == me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION) {
            player.sendMessage("§7Malzeme: §f" + (terms.getMaterial() != null ? terms.getMaterial().name() : "Bilinmiyor"));
            player.sendMessage("§7Miktar: §f" + terms.getAmount());
            player.sendMessage("§7Teslim Edilen: §f" + terms.getDelivered() + "/" + terms.getAmount());
            double progress = terms.getAmount() > 0 ? (double)terms.getDelivered() / terms.getAmount() * 100 : 0;
            player.sendMessage("§7İlerleme: §f" + String.format("%.1f", progress) + "%");
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.COMBAT) {
            if (terms.getTargetPlayer() != null) {
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(terms.getTargetPlayer());
                player.sendMessage("§7Hedef Oyuncu: §f" + (target.getName() != null ? target.getName() : "Bilinmiyor"));
            }
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.CONSTRUCTION) {
            player.sendMessage("§7Yapı Tipi: §f" + (terms.getStructureType() != null ? terms.getStructureType() : "Bilinmiyor"));
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.TERRITORY) {
            player.sendMessage("§7Yasaklı Bölge Sayısı: §f" + 
                (terms.getRestrictedAreas() != null ? terms.getRestrictedAreas().size() : 0));
            player.sendMessage("§7Yarıçap: §f" + terms.getRestrictedRadius() + " blok");
            if (terms.getRestrictedAreas() != null && !terms.getRestrictedAreas().isEmpty()) {
                player.sendMessage("§7Bölgeler:");
                for (int i = 0; i < Math.min(terms.getRestrictedAreas().size(), 5); i++) {
                    org.bukkit.Location loc = terms.getRestrictedAreas().get(i);
                    player.sendMessage("§7  - " + (loc.getWorld() != null ? loc.getWorld().getName() : "world") + 
                        " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
                }
                if (terms.getRestrictedAreas().size() > 5) {
                    player.sendMessage("§7  ... ve " + (terms.getRestrictedAreas().size() - 5) + " bölge daha");
                }
            }
        }
        
        player.sendMessage("§7Süre: §f" + formatTimeRemaining(terms.getDeadline() - System.currentTimeMillis()));
        player.sendMessage("§7Ödül: §f" + terms.getReward() + " altın");
        player.sendMessage("§7Ceza Tipi: §f" + (terms.getPenaltyType() != null ? 
            getPenaltyTypeName(terms.getPenaltyType()) : "Bilinmiyor"));
        player.sendMessage("§7Ceza: §f" + terms.getPenalty() + " altın");
        player.sendMessage("§7Onaylandı: §f" + (terms.isApproved() ? "§aEvet" : "§cHayır"));
        player.sendMessage("§7Tamamlandı: §f" + (terms.isCompleted() ? "§aEvet" : "§cHayır"));
        player.sendMessage("§7İhlal Edildi: §f" + (terms.isBreached() ? "§cEvet" : "§aHayır"));
        player.sendMessage("§6═══════════════════════════════════");
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
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), ana menüye dön
                if (state.contractRequestId != null) {
                    wizardStates.remove(player.getUniqueId());
                    player.closeInventory();
                    return;
                }
                // Normal akış: Scope seçimine dön
                state.step = 0;
                player.closeInventory();
                openScopeSelectionMenu(player);
                break;
                
            case CHEST:
                // RESOURCE_COLLECTION
                state.contractType = me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION;
                state.step = 2; // Tip seçildi, ödül adımına geç
                player.closeInventory();
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), direkt şart belirleme adımlarına geç
                if (state.contractRequestId != null) {
                    // Scope ve oyuncu zaten belirlenmiş (request'ten), direkt şart belirleme adımlarına geç
                    openRewardSliderMenu(player);
                } else {
                    // Normal akış: Ödül belirleme adımına geç
                    openRewardSliderMenu(player);
                }
                break;
                
            case DIAMOND_SWORD:
                // COMBAT
                state.contractType = me.mami.stratocraft.enums.ContractType.COMBAT;
                state.step = 2; // Tip seçildi, ödül adımına geç
                player.closeInventory();
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), direkt şart belirleme adımlarına geç
                if (state.contractRequestId != null) {
                    // Scope ve oyuncu zaten belirlenmiş (request'ten), direkt şart belirleme adımlarına geç
                    openRewardSliderMenu(player);
                } else {
                    // Normal akış: Ödül belirleme adımına geç
                    openRewardSliderMenu(player);
                }
                break;
                
            case BARRIER:
                // TERRITORY
                state.contractType = me.mami.stratocraft.enums.ContractType.TERRITORY;
                state.step = 2; // Tip seçildi, ödül adımına geç
                player.closeInventory();
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), direkt şart belirleme adımlarına geç
                if (state.contractRequestId != null) {
                    // Scope ve oyuncu zaten belirlenmiş (request'ten), direkt şart belirleme adımlarına geç
                    openRewardSliderMenu(player);
                } else {
                    // Normal akış: Ödül belirleme adımına geç
                    openRewardSliderMenu(player);
                }
                break;
                
            case STRUCTURE_BLOCK:
                // CONSTRUCTION
                state.contractType = me.mami.stratocraft.enums.ContractType.CONSTRUCTION;
                state.step = 2; // Tip seçildi, ödül adımına geç
                player.closeInventory();
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), direkt şart belirleme adımlarına geç
                if (state.contractRequestId != null) {
                    // Scope ve oyuncu zaten belirlenmiş (request'ten), direkt şart belirleme adımlarına geç
                    openRewardSliderMenu(player);
                } else {
                    // Normal akış: Ödül belirleme adımına geç
                    openRewardSliderMenu(player);
                }
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
        
        // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), scope seçimi yapma
        // Scope zaten request'te belirlenmiş, direkt şart belirleme adımlarına geç
        if (state.contractRequestId != null) {
            // Scope zaten belirlenmiş, direkt şart belirleme adımlarına geç
            state.step = 2;
            player.closeInventory();
            openRewardSliderMenu(player);
            return;
        }
        
        // Personal Terminal kontrolü
        boolean fromPersonalTerminal = isPersonalTerminal.getOrDefault(player.getUniqueId(), false);
        
        // ✅ İYİLEŞTİRME: Adım numarası ekle
        int currentStep = getCurrentStepNumber(state);
        int totalSteps = getTotalWizardSteps(state);
        String menuTitle = "§6[Adım " + currentStep + "/" + totalSteps + "] Kontrat Kapsamı Seç";
        
        Inventory menu = Bukkit.createInventory(null, 27, menuTitle);
        
        if (fromPersonalTerminal) {
            // Personal Terminal'den sadece PLAYER_TO_PLAYER göster
            menu.setItem(13, createButton(Material.PLAYER_HEAD, "§eOyuncu → Oyuncu", 
                Arrays.asList("§7═══════════════════════",
                    "§7Oyuncudan oyuncuya kontrat",
                    "§7═══════════════════════",
                    "§c§lNot: Personal Terminal'den",
                    "§c§lsadece oyuncu-oyuncu kontratları",
                    "§c§lyapılabilir.")));
        } else {
            // CONTRACT_OFFICE'den tüm kapsamlar göster
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
        
        // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), scope seçimi yapma
        // Scope zaten request'te belirlenmiş, direkt şart belirleme adımlarına geç
        if (state.contractRequestId != null) {
            // Scope zaten belirlenmiş, direkt şart belirleme adımlarına geç
            state.step = 2;
            player.closeInventory();
            openRewardSliderMenu(player);
            return;
        }
        
        // Personal Terminal kontrolü
        boolean fromPersonalTerminal = isPersonalTerminal.getOrDefault(player.getUniqueId(), false);
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), ana menüye dön
                if (state.contractRequestId != null) {
                    wizardStates.remove(player.getUniqueId());
                    player.closeInventory();
                    return;
                }
                // Normal akış: Ana menüye dön
                wizardStates.remove(player.getUniqueId());
                int page = currentPages.getOrDefault(player.getUniqueId(), 1);
                openMainMenu(player, page);
                break;
                
            case PLAYER_HEAD:
                // ÇİFT TARAFLI KONTRAT SİSTEMİ
                state.scope = Contract.ContractScope.PLAYER_TO_PLAYER;
                state.step = 1; // Scope seçildi, oyuncu seçimine geç
                player.closeInventory();
                
                // ✅ DÜZELTME: Oyuncu seçimi yapılmalı ama istek hemen gönderilmemeli
                // Önce online oyuncuları kontrol et
                List<org.bukkit.entity.Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                onlinePlayers.remove(player);
                
                if (!onlinePlayers.isEmpty()) {
                    // Oyuncu seçim menüsünü aç (istek göndermeden önce)
                    openPlayerSelectionMenuForRequest(player);
                } else {
                    // Online oyuncu yok, chat input iste
                    state.waitingForInput = "target_player";
                    player.sendMessage("§6═══════════════════════════════════");
                    player.sendMessage("§eHedef Oyuncu Belirle");
                    player.sendMessage("§7Chat'e hedef oyuncu ismini yazın");
                    player.sendMessage("§7İptal etmek için: §c/iptal");
                    player.sendMessage("§6═══════════════════════════════════");
                }
                break;
                
            case WHITE_BANNER:
                // CLAN_TO_CLAN - Personal Terminal'den yapılamaz
                if (fromPersonalTerminal) {
                    player.sendMessage("§cPersonal Terminal'den klan kontratları yapılamaz!");
                    player.sendMessage("§7Klan kontratları için CONTRACT_OFFICE yapısını kullanın.");
                    return;
                }
                state.scope = Contract.ContractScope.CLAN_TO_CLAN;
                state.step = 1; // Scope seçildi, tip seçimine geç
                player.closeInventory();
                openTypeSelectionMenu(player);
                break;
                
            case EMERALD:
                // PLAYER_TO_CLAN - Personal Terminal'den yapılamaz
                if (fromPersonalTerminal) {
                    player.sendMessage("§cPersonal Terminal'den klan kontratları yapılamaz!");
                    player.sendMessage("§7Klan kontratları için CONTRACT_OFFICE yapısını kullanın.");
                    return;
                }
                state.scope = Contract.ContractScope.PLAYER_TO_CLAN;
                state.step = 1; // Scope seçildi, tip seçimine geç
                player.closeInventory();
                openTypeSelectionMenu(player);
                break;
                
            case GOLD_INGOT:
                // CLAN_TO_PLAYER - Personal Terminal'den yapılamaz
                if (fromPersonalTerminal) {
                    player.sendMessage("§cPersonal Terminal'den klan kontratları yapılamaz!");
                    player.sendMessage("§7Klan kontratları için CONTRACT_OFFICE yapısını kullanın.");
                    return;
                }
                state.scope = Contract.ContractScope.CLAN_TO_PLAYER;
                state.step = 1; // Scope seçildi, tip seçimine geç
                player.closeInventory();
                openTypeSelectionMenu(player);
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
        
        // ✅ DÜZELTME: Ödül null olabilir, direkt onaylanabilir
        // if (state.reward == 0) state.reward = 100; // Varsayılan değer kaldırıldı
        
        // ✅ İYİLEŞTİRME: Adım numarası ekle
        int currentStepNum = getCurrentStepNumber(state);
        int totalStepsNum = getTotalWizardSteps(state);
        String menuTitleStr = "§6[Adım " + currentStepNum + "/" + totalStepsNum + "] Ödül Belirle";
        
        Inventory menu = Bukkit.createInventory(null, 27, menuTitleStr);
        
        // Mevcut değer göster
        List<String> valueLore = new ArrayList<>();
        valueLore.add("§7═══════════════════════");
        if (state.reward > 0) {
            valueLore.add("§eMevcut Ödül: §a" + String.format("%.0f", state.reward) + " Altın");
        } else {
            valueLore.add("§eMevcut Ödül: §7Yok (Direkt onaylarsanız ödül olmayacak)");
        }
        valueLore.add("§7");
        valueLore.add("§7ℹ️ Direkt onaylarsanız ödül");
        valueLore.add("§7belirlenmeyecek. Ama ceza");
        valueLore.add("§7belirlemek zorundasınız.");
        valueLore.add("§7═══════════════════════");
        String displayText = state.reward > 0 ? 
            "§e§l" + String.format("%.0f", state.reward) + " Altın" : 
            "§7§lÖdül Yok";
        menu.setItem(13, createButton(Material.GOLD_INGOT, displayText, valueLore));
        
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
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), tip seçimine dön
                if (state.contractRequestId != null) {
                    state.step = 0;
                    player.closeInventory();
                    openTypeSelectionMenu(player);
                } else {
                    // Normal akış: Tip seçimine dön
                    state.step = 2; // Tip seçimine dön
                    player.closeInventory();
                    openTypeSelectionMenu(player);
                }
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
                    // ✅ DÜZELTME: Onay - Ödül null olabilir, direkt ceza tipi seçimine geç
                    // Ödül belirlenmemişse null yap
                    if (state.reward <= 0) {
                        state.reward = 0; // Null olarak işaretle
                    }
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
        
        // ✅ DÜZELTME: Ceza null olabilir, direkt onaylanabilir
        // if (state.penalty == 0) state.penalty = state.reward * 0.5; // Varsayılan kaldırıldı
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Ceza Belirle");
        
        // Mevcut değer göster
        List<String> valueLore = new ArrayList<>();
        valueLore.add("§7═══════════════════════");
        if (state.penalty > 0) {
            valueLore.add("§eMevcut Ceza: §c" + String.format("%.0f", state.penalty) + " Altın");
        } else {
            valueLore.add("§eMevcut Ceza: §7Yok (Direkt onaylarsanız ceza olmayacak)");
        }
        if (state.reward > 0) {
            valueLore.add("§7Ödül: §a" + String.format("%.0f", state.reward) + " Altın");
        } else {
            valueLore.add("§7Ödül: §7Yok");
        }
        valueLore.add("§7");
        valueLore.add("§7ℹ️ Direkt onaylarsanız ceza");
        valueLore.add("§7belirlenmeyecek. Ama ödül");
        valueLore.add("§7belirlemediyseniz devam edemezsiniz.");
        valueLore.add("§7═══════════════════════");
        String displayText = state.penalty > 0 ? 
            "§c§l" + String.format("%.0f", state.penalty) + " Altın" : 
            "§7§lCeza Yok";
        menu.setItem(13, createButton(Material.REDSTONE_BLOCK, displayText, valueLore));
        
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
                // ✅ DÜZELTME: Eğer ödül null ise, ödül menüsüne dön
                // Eğer ödül varsa, ödül menüsüne dön
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
                    // ✅ DÜZELTME: Onay - Ceza null olabilir, ama ödül kontrolü yap
                    // Ceza belirlenmemişse null yap
                    if (state.penalty <= 0) {
                        state.penalty = 0; // Null olarak işaretle
                    }
                    
                    // ✅ Ödül kontrolü: Eğer ödül de null ise devam edemez
                    if (state.reward <= 0 && state.penalty <= 0) {
                        player.sendMessage("§c§lHATA!");
                        player.sendMessage("§7En az birini belirlemek zorundasınız:");
                        player.sendMessage("§7- Ödül veya");
                        player.sendMessage("§7- Ceza");
                        player.sendMessage("§7");
                        player.sendMessage("§7Geri gidip ödül belirleyebilirsiniz.");
                        return;
                    }
                    
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
     * Oyuncu seçim menüsü aç (Çift taraflı kontrat için - istek gönderme)
     */
    private void openPlayerSelectionMenuForRequest(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        // ✅ DÜZELTME: Başlık değiştirildi - artık "İstek Gönder" yazmıyor
        Inventory menu = Bukkit.createInventory(null, 54, "§6Hedef Oyuncu Seç");
        
        // Online oyuncuları listele
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Kendisini listeden çıkar
        
        int slot = 0;
        for (Player target : onlinePlayers) {
            if (slot >= 45) break;
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Oyuncu: §e" + target.getName());
            
            // Klan bilgisi
            if (clanManager != null) {
                Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
                if (targetClan != null) {
                    lore.add("§7Klan: §b" + targetClan.getName());
                } else {
                    lore.add("§7Klan: §7Yok");
                }
            }
            lore.add("§7═══════════════════════");
            lore.add("§aSol tıkla seç");
            lore.add("§7Şartları belirledikten sonra");
            lore.add("§7istek gönderilecek");
            
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + target.getName());
                meta.setLore(lore);
                // Oyuncu UUID'sini lore'a ekle
                List<String> itemLore = new ArrayList<>(lore);
                itemLore.add("§8playerUUID:" + target.getUniqueId().toString());
                meta.setLore(itemLore);
                playerHead.setItemMeta(meta);
            }
            menu.setItem(slot++, playerHead);
        }
        
        // Chat'ten yazma seçeneği (Slot 49)
        menu.setItem(49, createButton(Material.WRITABLE_BOOK, "§eChat'ten Yaz", 
            Arrays.asList("§7Online değilse chat'e oyuncu", "§7ismini yazabilirsiniz")));
        
        // Geri butonu (Slot 45)
        menu.setItem(45, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Önceki adıma dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Oyuncu seçim menüsü aç (COMBAT için)
     */
    private void openPlayerSelectionMenu(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) {
            player.closeInventory();
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Hedef Oyuncu Seç");
        
        // Online oyuncuları listele
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player); // Kendisini listeden çıkar
        
        int slot = 0;
        for (Player target : onlinePlayers) {
            if (slot >= 45) break;
            
            List<String> lore = new ArrayList<>();
            lore.add("§7═══════════════════════");
            lore.add("§7Oyuncu: §e" + target.getName());
            
            // Klan bilgisi
            if (clanManager != null) {
                Clan targetClan = clanManager.getClanByPlayer(target.getUniqueId());
                if (targetClan != null) {
                    lore.add("§7Klan: §b" + targetClan.getName());
                } else {
                    lore.add("§7Klan: §7Yok");
                }
            }
            lore.add("§7═══════════════════════");
            lore.add("§aSol tıkla seç");
            
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + target.getName());
                meta.setLore(lore);
                // Oyuncu UUID'sini lore'a ekle
                List<String> itemLore = new ArrayList<>(lore);
                itemLore.add("§8playerUUID:" + target.getUniqueId().toString());
                meta.setLore(itemLore);
                playerHead.setItemMeta(meta);
            }
            menu.setItem(slot++, playerHead);
        }
        
        // ✅ İYİLEŞTİRME: Açıklayıcı bilgi (Slot 49)
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7═══════════════════════");
        infoLore.add("§7ℹ️ Oyuncu seçildikten sonra");
        infoLore.add("§7şartları belirleyeceksiniz.");
        infoLore.add("§7");
        infoLore.add("§7İstek şartlar belirlendikten");
        infoLore.add("§7sonra gönderilecek.");
        infoLore.add("§7═══════════════════════");
        menu.setItem(49, createButton(Material.BOOK, "§eℹ️ Bilgi", infoLore));
        
        // Chat'ten yazma seçeneği (Slot 48)
        menu.setItem(48, createButton(Material.WRITABLE_BOOK, "§eChat'ten Yaz", 
            Arrays.asList("§7Online değilse chat'e oyuncu", "§7veya klan ismini yazabilirsiniz")));
        
        // Geri butonu (Slot 45)
        menu.setItem(45, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Önceki adıma dön")));
        
        // İptal butonu (Slot 53)
        menu.setItem(53, createButton(Material.RED_CONCRETE, "§cİptal", 
            Arrays.asList("§7Kontrat oluşturmayı iptal et")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Oyuncu seçim menüsü tıklama
     */
    private void handlePlayerSelectionClick(InventoryClickEvent event) {
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
        
        // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), oyuncu seçimi yapma
        // Oyuncu zaten request'te belirlenmiş, direkt şart belirleme adımlarına geç
        if (state.contractRequestId != null) {
            // Oyuncu zaten belirlenmiş, direkt şart belirleme adımlarına geç
            state.step = 2;
            player.closeInventory();
            openRewardSliderMenu(player);
            return;
        }
        
        switch (clicked.getType()) {
            case ARROW:
                // Geri
                String menuTitle = event.getView().getTitle();
                if (menuTitle.equals("§6Hedef Oyuncu Seç")) {
                    // Çift taraflı kontrat için geri
                    player.closeInventory();
                    openScopeSelectionMenu(player);
                } else {
                    // Eski sistem
                    state.step = 5;
                    player.closeInventory();
                    openTypeSpecificMenu(player);
                }
                break;
                
            case WRITABLE_BOOK:
                // Chat'ten yaz
                state.waitingForInput = "player";
                player.closeInventory();
                player.sendMessage("§6═══════════════════════════════════");
                player.sendMessage("§eHedef Oyuncu/Klan Belirle");
                player.sendMessage("§7Chat'e hedef oyuncu veya klan ismini yazın");
                player.sendMessage("§7Örnek: §eOyuncuAdı §7veya §eKlanAdı");
                player.sendMessage("§7İptal etmek için: §c/iptal");
                player.sendMessage("§6═══════════════════════════════════");
                break;
                
            case PLAYER_HEAD:
                // Oyuncu seçildi
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getLore() != null) {
                    for (String line : meta.getLore()) {
                        if (line.startsWith("§8playerUUID:")) {
                            String uuidStr = line.substring("§8playerUUID:".length());
                            try {
                                UUID targetUUID = UUID.fromString(uuidStr);
                                
                                String currentMenuTitle = event.getView().getTitle();
                                
                                // ✅ DÜZELTME: PLAYER_TO_PLAYER için oyuncu seçildiğinde sadece state'e kaydet, tip seçimine geç
                                if (currentMenuTitle.equals("§6Hedef Oyuncu Seç") && 
                                    state.scope == Contract.ContractScope.PLAYER_TO_PLAYER) {
                                    // Oyuncuyu state'e kaydet (istek gönderilmeden önce)
                                    state.targetPlayerForRequest = targetUUID;
                                    state.step = 1; // Oyuncu seçildi, tip seçimine geç
                                    state.waitingForInput = null;
                                    
                                    // ✅ DÜZELTME: Tip seçimine geç
                                    player.closeInventory();
                                    openTypeSelectionMenu(player);
                                    
                                    player.sendMessage("§6═══════════════════════════════════");
                                    player.sendMessage("§a§lOYUNCU SEÇİLDİ!");
                                    player.sendMessage("§7Hedef: §e" + Bukkit.getOfflinePlayer(targetUUID).getName());
                                    player.sendMessage("§7Şimdi kontrat tipini seçin");
                                    player.sendMessage("§7Şartlar belirlendikten sonra istek gönderilecek");
                                    player.sendMessage("§6═══════════════════════════════════");
                                    return;
                                } else {
                                    // Eski sistem (COMBAT için)
                                    state.targetPlayer = targetUUID;
                                    state.waitingForInput = null;
                                    state.step = 6;
                                    player.closeInventory();
                                    openSummaryMenu(player);
                                }
                                return;
                            } catch (IllegalArgumentException e) {
                                player.sendMessage("§cGeçersiz oyuncu UUID!");
                            }
                        }
                    }
                }
                break;
        }
    }
    
    /**
     * Oyuncu input iste (GERİYE UYUMLULUK - chat input için)
     */
    private void requestPlayerInput(Player player) {
        ContractWizardState state = wizardStates.get(player.getUniqueId());
        if (state == null) return;
        
        // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), oyuncu seçimi yapma
        // Oyuncu zaten request'te belirlenmiş, direkt şart belirleme adımlarına geç
        if (state.contractRequestId != null) {
            // Oyuncu zaten belirlenmiş, direkt şart belirleme adımlarına geç
            state.step = 2;
            openRewardSliderMenu(player);
            return;
        }
        
        // Önce menüyü aç, eğer online oyuncu yoksa chat input iste
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player);
        
        if (!onlinePlayers.isEmpty()) {
            // Online oyuncular var, menüyü aç
            openPlayerSelectionMenu(player);
        } else {
            // Online oyuncu yok, chat input iste
            state.waitingForInput = "player";
            player.sendMessage("§6═══════════════════════════════════");
            player.sendMessage("§eHedef Oyuncu/Klan Belirle");
            player.sendMessage("§7Chat'e hedef oyuncu veya klan ismini yazın");
            player.sendMessage("§7Örnek: §eOyuncuAdı §7veya §eKlanAdı");
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
     * Oyuncu oyundan çıktığında wizard state'lerini temizle
     * ✅ PERFORMANS: Memory leak önleme - tüm Map'leri temizle
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Wizard state temizle
        wizardStates.remove(playerId);
        
        // Görüntülenen kontrat temizle
        viewingContract.remove(playerId);
        
        // Sayfa numarası temizle
        currentPages.remove(playerId);
        
        // Personal Terminal flag'ini temizle
        isPersonalTerminal.remove(playerId);
        
        // ✅ YENİ: Kontrat şablonlarını temizle
        playerTemplates.remove(playerId);
        
        // ✅ YENİ: Kontrat geçmişini temizle
        contractHistory.remove(playerId);
        
        // İptal isteklerini temizle (bu oyuncunun gönderdiği istekler)
        cancelRequests.entrySet().removeIf(entry -> entry.getValue().equals(playerId));
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
                
            case "target_player":
                // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), oyuncu seçimi yapma
                if (state.contractRequestId != null) {
                    // Oyuncu zaten belirlenmiş, direkt şart belirleme adımlarına geç
                    state.waitingForInput = null;
                    state.step = 2;
                    openRewardSliderMenu(player);
                    return;
                }
                
                // ✅ DÜZELTME: Çift taraflı kontrat için oyuncu seçimi - sadece state'e kaydet, istek gönderme
                Player targetPlayerForRequest = Bukkit.getPlayer(message);
                if (targetPlayerForRequest != null && targetPlayerForRequest.isOnline()) {
                    if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER) {
                        // Oyuncuyu state'e kaydet (istek gönderilmeden önce)
                        state.targetPlayerForRequest = targetPlayerForRequest.getUniqueId();
                        state.step = 1; // Oyuncu seçildi, tip seçimine geç
                        state.waitingForInput = null;
                        
                        // ✅ DÜZELTME: Tip seçimine geç
                        openTypeSelectionMenu(player);
                        
                        player.sendMessage("§6═══════════════════════════════════");
                        player.sendMessage("§a§lOYUNCU SEÇİLDİ!");
                        player.sendMessage("§7Hedef: §e" + targetPlayerForRequest.getName());
                        player.sendMessage("§7Şimdi kontrat tipini seçin");
                        player.sendMessage("§7Şartlar belirlendikten sonra istek gönderilecek");
                        player.sendMessage("§6═══════════════════════════════════");
                    } else {
                        player.sendMessage("§cGeçersiz kapsam!");
                    }
                } else {
                    player.sendMessage("§cOyuncu bulunamadı veya online değil!");
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
        
        // ✅ DÜZELTME: Eğer contractRequestId varsa (şart ekleme durumu), oyuncu seçimi yapma
        // Oyuncu zaten request'te belirlenmiş, direkt şart belirleme adımlarına geç
        if (state.contractRequestId != null) {
            // Oyuncu zaten belirlenmiş, direkt şart belirleme adımlarına geç
            state.step = 2;
            player.closeInventory();
            openRewardSliderMenu(player);
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
        
        // ✅ İYİLEŞTİRME: Adım numarası ekle
        int currentStep = getCurrentStepNumber(state);
        int totalSteps = getTotalWizardSteps(state);
        String menuTitle = "§6[Adım " + currentStep + "/" + totalSteps + "] Kontrat Özeti";
        
        Inventory menu = Bukkit.createInventory(null, 54, menuTitle); // 54 slot (6x9) - daha fazla yer için
        
        // ✅ İYİLEŞTİRME: Sizin şartlarınız bölümü
        List<String> summaryLore = new ArrayList<>();
        summaryLore.add("§7═══════════════════════");
        summaryLore.add("§e§lSİZİN ŞARTLARINIZ:");
        summaryLore.add("§7═══════════════════════");
        summaryLore.add("§7Tip: §e" + (state.contractType != null ? getContractTypeName(state.contractType) : "Bilinmeyen"));
        summaryLore.add("§7Kapsam: §e" + getContractScopeName(state.scope));
        if (state.reward > 0) {
            summaryLore.add("§7Ödül: §a" + String.format("%.0f", state.reward) + " Altın");
        } else {
            summaryLore.add("§7Ödül: §7Yok");
        }
        if (state.penalty > 0) {
            summaryLore.add("§7Ceza: §c" + String.format("%.0f", state.penalty) + " Altın");
        } else {
            summaryLore.add("§7Ceza: §7Yok");
        }
        summaryLore.add("§7Süre: §e" + state.deadlineDays + " Gün");
        
        // ✅ İYİLEŞTİRME: Eğer contractRequestId varsa (şart ekleme durumu), karşı tarafın şartlarını göster
        if (state.contractRequestId != null && contractRequestManager != null && contractTermsManager != null) {
            me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(state.contractRequestId);
            if (request != null) {
                UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
                    request.getTarget() : request.getSender();
                me.mami.stratocraft.model.ContractTerms otherTerms = 
                    contractTermsManager.getTermsByRequest(state.contractRequestId, otherPlayerId);
                
                if (otherTerms != null) {
                    org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
                    String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
                    
                    summaryLore.add("§7═══════════════════════");
                    summaryLore.add("§7§lKARŞI TARAFIN ŞARTLARI:");
                    summaryLore.add("§7═══════════════════════");
                    summaryLore.addAll(createTermsLore(otherTerms, otherName, false));
                }
            }
        }
        
        summaryLore.add("§7═══════════════════════");
        
        // Kontrat mantığı açıklaması
        summaryLore.add("§7§lKontrat Mantığı:");
        if (state.contractType != null) {
            switch (state.contractType) {
                case RESOURCE_COLLECTION:
                    summaryLore.add("§7• §eSen kontratı oluşturuyorsun");
                    summaryLore.add("§7• Birisi kontratı kabul edecek");
                    summaryLore.add("§7• §aKabul eden kişi belirtilen");
                    summaryLore.add("§7  malzemeyi toplayıp §eSANA§7 verecek");
                    summaryLore.add("§7• §eSen ödülü vereceksin");
                    summaryLore.add("§7");
                    summaryLore.add("§7§lKontrat Mantığı:");
                    summaryLore.add("§7Kabul eden kişi malzemeyi toplayacak");
                    summaryLore.add("§7ve /kontrat teslim komutu ile sana verecek.");
                    summaryLore.add("§7Sen de ödülü ödeyeceksin.");
                    summaryLore.add("§7");
                    summaryLore.add("§7Malzeme: §e" + (state.material != null ? getMaterialDisplayName(state.material) : "Yok"));
                    summaryLore.add("§7Miktar: §e" + state.amount);
                    if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER) {
                        summaryLore.add("§7");
                        summaryLore.add("§7§lKapsam: Oyuncudan Oyuncuya");
                        summaryLore.add("§7Malzeme §eSANA§7 verilecek");
                    }
                    break;
                case COMBAT:
                    summaryLore.add("§7• Sen kontratı oluşturuyorsun");
                    summaryLore.add("§7• Birisi kontratı kabul edecek");
                    summaryLore.add("§7• Kabul eden kişi hedefi");
                    summaryLore.add("§7  öldürecek veya vuracak");
                    summaryLore.add("§7• Sen ödülü vereceksin");
                    summaryLore.add("§7");
                    if (state.targetPlayer != null) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(state.targetPlayer);
                        summaryLore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
                    } else {
                        summaryLore.add("§7Hedef: §cHenüz seçilmedi");
                    }
                    break;
                case TERRITORY:
                    summaryLore.add("§7• Sen kontratı oluşturuyorsun");
                    summaryLore.add("§7• Birisi kontratı kabul edecek");
                    summaryLore.add("§7• Kabul eden kişi belirtilen");
                    summaryLore.add("§7  bölgeye gitmeyecek");
                    summaryLore.add("§7• Sen ödülü vereceksin");
                    summaryLore.add("§7");
                    if (state.restrictedAreas != null && !state.restrictedAreas.isEmpty()) {
                        summaryLore.add("§7Yasak Bölge: §e" + state.restrictedAreas.size() + " bölge");
                        summaryLore.add("§7Yarıçap: §e" + (state.restrictedRadius > 0 ? state.restrictedRadius : 50) + " blok");
                    } else {
                        summaryLore.add("§7Yasak Bölge: §cHenüz seçilmedi");
                    }
                    break;
                case CONSTRUCTION:
                    summaryLore.add("§7• Sen kontratı oluşturuyorsun");
                    summaryLore.add("§7• Birisi kontratı kabul edecek");
                    summaryLore.add("§7• Kabul eden kişi belirtilen");
                    summaryLore.add("§7  yapıyı inşa edecek");
                    summaryLore.add("§7• Sen ödülü vereceksin");
                    summaryLore.add("§7");
                    summaryLore.add("§7Yapı Tipi: §e" + (state.structureType != null ? state.structureType : "Yok"));
                    break;
            }
        }
        
        summaryLore.add("§7═══════════════════════");
        
        // ✅ DÜZELTME: Ödül/Ceza kontrolü mesajı
        if (state.reward <= 0 && state.penalty <= 0) {
            summaryLore.add("§7");
            summaryLore.add("§c§l⚠️ UYARI!");
            summaryLore.add("§7En az birini belirlemek zorundasınız:");
            summaryLore.add("§7- Ödül veya");
            summaryLore.add("§7- Ceza");
            summaryLore.add("§7");
            summaryLore.add("§7Geri gidip ödül veya ceza belirleyebilirsiniz.");
        }
        
        // ✅ İYİLEŞTİRME: Açıklayıcı mesaj ekle
        if (state.contractRequestId != null) {
            summaryLore.add("§7");
            summaryLore.add("§7ℹ️ Şartlarınız kaydedilecek.");
            summaryLore.add("§7İlk gönderen oyuncu onayladığında");
            summaryLore.add("§7kontrat aktif olacak.");
        } else if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER) {
            summaryLore.add("§7");
            summaryLore.add("§7ℹ️ Bu şartlar karşı tarafa gönderilecek.");
            summaryLore.add("§7Karşı taraf kabul ederse kontrat");
            summaryLore.add("§7aktif olacak.");
        }
        
        menu.setItem(13, createButton(Material.PAPER, "§eKontrat Özeti", summaryLore));
        
        // Onay butonu
        String approveText = state.contractRequestId != null ? "§a§lONAYLA" : "§a§lONAYLA VE GÖNDER";
        List<String> approveLore = new ArrayList<>();
        if (state.contractRequestId != null) {
            approveLore.add("§7Şartlarınızı kaydet");
        } else {
            approveLore.add("§7Kontratı oluştur ve istek gönder");
        }
        menu.setItem(11, createButton(Material.GREEN_CONCRETE, approveText, approveLore));
        
        // Şablon olarak kaydet butonu (sadece yeni kontrat için)
        if (state.contractRequestId == null) {
            menu.setItem(12, createButton(Material.BOOK, "§eŞablon Olarak Kaydet", 
                Arrays.asList("§7Bu kontratı şablon olarak kaydet", "§7Daha sonra hızlıca kullanabilirsiniz")));
        }
        
        // İptal butonu
        menu.setItem(15, createButton(Material.RED_CONCRETE, "§c§lİPTAL", 
            Arrays.asList("§7Kontrat oluşturmayı iptal et")));
        
        // Geri butonu
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Önceki adıma dön")));
        
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
                // ✅ DÜZELTME: Ödül/Ceza kontrolü - En az birini belirlemek zorunda
                if (state.reward <= 0 && state.penalty <= 0) {
                    player.sendMessage("§c§lHATA!");
                    player.sendMessage("§7En az birini belirlemek zorundasınız:");
                    player.sendMessage("§7- Ödül veya");
                    player.sendMessage("§7- Ceza");
                    player.sendMessage("§7");
                    player.sendMessage("§7Geri gidip ödül veya ceza belirleyebilirsiniz.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }
                
                // Kontratı oluştur
                createContractFromState(player, state);
                wizardStates.remove(player.getUniqueId());
                player.closeInventory();
                player.sendMessage("§6═══════════════════════════════════");
                player.sendMessage("§a§lKONTRA T BAŞARIYLA OLUŞTURULDU!");
                player.sendMessage("§7");
                player.sendMessage("§7Kontratınızı görmek için:");
                player.sendMessage("§7• Pusuladan (CONTRACT_PAPER) sağ tıkla");
                player.sendMessage("§7• §6Altın Elma §7butonuna tıkla");
                player.sendMessage("§7• Oluşturduğun kontratları görüntüle");
                player.sendMessage("§7");
                player.sendMessage("§7Kontrat şu anda açık ve");
                player.sendMessage("§7herkes tarafından görülebilir.");
                player.sendMessage("§7Birisi kabul ettiğinde bildirim alacaksın.");
                player.sendMessage("§6═══════════════════════════════════");
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
        
        // ✅ DÜZELTME: PLAYER_TO_PLAYER için önce istek gönder, sonra şartları kaydet
        if (state.scope == Contract.ContractScope.PLAYER_TO_PLAYER && 
            state.targetPlayerForRequest != null && 
            contractRequestManager != null) {
            
            // İstek gönder
            me.mami.stratocraft.model.ContractRequest request = 
                contractRequestManager.sendRequest(player.getUniqueId(), 
                    state.targetPlayerForRequest, state.scope);
            
            if (request == null) {
                player.sendMessage("§cİstek gönderilemedi! Zaten bekleyen bir istek olabilir.");
                return;
            }
            
            // Request ID'yi state'e ekle
            state.contractRequestId = request.getId();
            
            // Şartları kaydet
            if (contractTermsManager != null && state.contractType != null) {
                me.mami.stratocraft.model.ContractTerms senderTerms = 
                    contractTermsManager.createTerms(request.getId(), player.getUniqueId(), state);
                
                if (senderTerms != null) {
                    // İlk gönderen kendi şartlarını onaylamış sayılır
                    contractTermsManager.approveTerms(senderTerms.getId(), player.getUniqueId());
                }
            }
            
            player.sendMessage("§6═══════════════════════════════════");
            player.sendMessage("§a§lKONTRAT İSTEĞİ GÖNDERİLDİ!");
            player.sendMessage("§7Hedef: §e" + Bukkit.getOfflinePlayer(state.targetPlayerForRequest).getName());
            player.sendMessage("§7İstek kabul edildiğinde bildirim alacaksınız");
            player.sendMessage("§6═══════════════════════════════════");
            
            // HUD'a bildirim gönder
            sendContractNotification(player, "İstek gönderildi: " + 
                Bukkit.getOfflinePlayer(state.targetPlayerForRequest).getName(), 
                me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
            
            return; // Çift taraflı kontrat için burada bitir
        }
        
        // Çift taraflı kontrat için şart kaydetme (kabul edilmiş istek için)
        if (state.contractRequestId != null && contractTermsManager != null) {
            // Şartları kaydet
            me.mami.stratocraft.model.ContractTerms terms = 
                contractTermsManager.createTerms(state.contractRequestId, player.getUniqueId(), state);
            
            if (terms != null) {
                player.sendMessage("§6═══════════════════════════════════");
                player.sendMessage("§a§lŞARTLARINIZ KAYDEDİLDİ!");
                player.sendMessage("§7Şartlarınız kaydedildi.");
                player.sendMessage("§7Karşı taraf şartlarını belirlediğinde");
                player.sendMessage("§7onaylayabileceksiniz.");
                player.sendMessage("§6═══════════════════════════════════");
                
                // HUD'a bildirim gönder
                sendContractNotification(player, "Şartlarınız kaydedildi", 
                    me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
                
                // Şartları onayla
                contractTermsManager.approveTerms(terms.getId(), player.getUniqueId());
                
                // Karşı tarafın şartları hazır mı kontrol et
                me.mami.stratocraft.model.ContractRequest request = 
                    contractRequestManager != null ? contractRequestManager.getRequest(state.contractRequestId) : null;
                if (request != null) {
                    UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
                        request.getTarget() : request.getSender();
                    me.mami.stratocraft.model.ContractTerms otherTerms = 
                        contractTermsManager.getTermsByRequest(state.contractRequestId, otherPlayerId);
                    
                    // YENİ: Her durumda ilk gönderen oyuncuya son onay bildirimi gönder
                    // Eğer şartları belirleyen kişi ilk gönderen değilse (yani target ise)
                    if (request.getTarget().equals(player.getUniqueId())) {
                        // Target şartlarını belirledi, sender'a son onay bildirimi gönder
                        org.bukkit.entity.Player senderPlayer = Bukkit.getPlayer(request.getSender());
                        if (senderPlayer != null && senderPlayer.isOnline()) {
                            senderPlayer.sendMessage("§6═══════════════════════════════════");
                            senderPlayer.sendMessage("§e§lSON ONAY GEREKİYOR!");
                            senderPlayer.sendMessage("§7" + player.getName() + " şartlarını belirledi");
                            senderPlayer.sendMessage("§7Şimdi kontratı son kez onaylamanız gerekiyor");
                            senderPlayer.sendMessage("§7Kabul ederseniz kontrat aktif olacak");
                            senderPlayer.sendMessage("§7Reddederseniz kontrat iptal olacak");
                            senderPlayer.sendMessage("§6═══════════════════════════════════");
                            
                            // HUD'a bildirim gönder
                            sendContractNotification(senderPlayer, "Son onay gerekiyor: " + player.getName(), 
                                me.mami.stratocraft.manager.HUDManager.ContractNotificationType.WARNING);
                            
                            // İlk gönderen oyuncuya son onay menüsü aç
                            openFinalApprovalMenu(senderPlayer, request.getId());
                        }
                    } else {
                        // Sender şartlarını belirledi, target'a bildirim gönder
                        org.bukkit.entity.Player targetPlayer = Bukkit.getPlayer(request.getTarget());
                        if (targetPlayer != null && targetPlayer.isOnline()) {
                            targetPlayer.sendMessage("§6═══════════════════════════════════");
                            targetPlayer.sendMessage("§e§lŞARTLAR HAZIR!");
                            targetPlayer.sendMessage("§7" + player.getName() + " şartlarını belirledi");
                            targetPlayer.sendMessage("§7İlk gönderen oyuncu onayladığında kontrat aktif olacak");
                            targetPlayer.sendMessage("§6═══════════════════════════════════");
                        }
                    }
                }
                
                wizardStates.remove(player.getUniqueId());
                player.closeInventory();
                return;
            }
        }
        
        // Eski sistem (tek taraflı kontrat)
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
     * Çift taraflı kontrat oluştur (her iki şart hazır olduğunda)
     */
    private void createBilateralContract(me.mami.stratocraft.model.ContractRequest request,
                                        me.mami.stratocraft.model.ContractTerms termsA,
                                        me.mami.stratocraft.model.ContractTerms termsB) {
        if (contractManager == null || request == null || termsA == null || termsB == null) return;
        
        // Şartların hangi oyuncuya ait olduğunu kontrol et
        me.mami.stratocraft.model.ContractTerms senderTerms = 
            termsA.getPlayerId().equals(request.getSender()) ? termsA : termsB;
        me.mami.stratocraft.model.ContractTerms targetTerms = 
            termsA.getPlayerId().equals(request.getTarget()) ? termsA : termsB;
        
        // Kontrat oluştur (playerA = sender, playerB = target)
        Contract contract = new Contract(request.getSender(), request.getTarget(), 
            request.getId(), senderTerms, targetTerms);
        
        // ContractManager'a ekle
        contractManager.getContracts().add(contract);
        
        // Her iki oyuncuya da kan imzası uygula
        org.bukkit.entity.Player playerA = Bukkit.getPlayer(request.getSender());
        org.bukkit.entity.Player playerB = Bukkit.getPlayer(request.getTarget());
        
        if (playerA != null && playerA.isOnline()) {
            applyBloodSignature(playerA, 1);
            playerA.sendMessage("§6═══════════════════════════════════");
            playerA.sendMessage("§a§lKONTRAT AKTİF!");
            playerA.sendMessage("§7Kontrat aktif hale geldi");
            playerA.sendMessage("§c1 kalp kaybettiniz (kan imzası)");
            playerA.sendMessage("§7Kontrat tamamlandığında kalp geri verilecek");
            playerA.sendMessage("§6═══════════════════════════════════");
            
            // HUD'a bildirim gönder
            sendContractNotification(playerA, "Kontrat aktif oldu", 
                me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
        }
        
        if (playerB != null && playerB.isOnline()) {
            applyBloodSignature(playerB, 1);
            playerB.sendMessage("§6═══════════════════════════════════");
            playerB.sendMessage("§a§lKONTRAT AKTİF!");
            playerB.sendMessage("§7Kontrat aktif hale geldi");
            playerB.sendMessage("§c1 kalp kaybettiniz (kan imzası)");
            playerB.sendMessage("§7Kontrat tamamlandığında kalp geri verilecek");
            playerB.sendMessage("§6═══════════════════════════════════");
            
            // HUD'a bildirim gönder
            sendContractNotification(playerB, "Kontrat aktif oldu", 
                me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
        }
    }
    
    /**
     * Kan imzası uygula
     */
    private void applyBloodSignature(org.bukkit.entity.Player player, int hearts) {
        if (player == null) return;
        
        org.bukkit.attribute.AttributeInstance maxHealthAttr = 
            player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            double currentMax = maxHealthAttr.getBaseValue();
            double newMax = Math.max(1.0, currentMax - (hearts * 2.0)); // 1 kalp = 2 can
            maxHealthAttr.setBaseValue(newMax);
            if (player.getHealth() > newMax) {
                player.setHealth(newMax);
            }
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
     * Benim Kontratlarım menüsü (Oluşturduğu kontratlar)
     */
    private void openMyContractsMenu(Player player, int page) {
        if (player == null || contractManager == null) return;
        
        List<Contract> contracts = contractManager.getContracts();
        if (contracts == null) contracts = new ArrayList<>();
        
        // Bu oyuncunun kontratlarını göster (tek taraflı + çift taraflı)
        List<Contract> myContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract == null) continue;
            
            // Tek taraflı kontratlar (issuer)
            if (!contract.isBilateralContract() && contract.getIssuer().equals(player.getUniqueId()) && 
                !contract.isExpired() && !contract.isBreached()) {
                myContracts.add(contract);
            }
            // Çift taraflı kontratlar (playerA veya playerB)
            else if (contract.isBilateralContract() && 
                (contract.getPlayerA().equals(player.getUniqueId()) || 
                 contract.getPlayerB().equals(player.getUniqueId())) &&
                contract.getContractStatus() != Contract.ContractStatus.BREACHED) {
                myContracts.add(contract);
            }
        }
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(myContracts.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        // 54 slotlu menü oluştur
        Inventory menu = Bukkit.createInventory(null, 54, "§6Benim Kontratlarım - Sayfa " + page);
        
        // Kontratları göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, myContracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = myContracts.get(i);
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
        
        if (endIndex < myContracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        // Bilgi butonu (Slot 50)
        long bilateralCount = myContracts.stream().filter(c -> c.isBilateralContract()).count();
        long singleCount = myContracts.stream().filter(c -> !c.isBilateralContract()).count();
        menu.setItem(50, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Kontrat: §e" + myContracts.size(),
                "§7Tek Taraflı: §e" + singleCount,
                "§7Çift Taraflı: §e" + bilateralCount,
                "§7Aktif: §e" + myContracts.stream().filter(c -> 
                    (c.isBilateralContract() && c.getContractStatus() == Contract.ContractStatus.ACTIVE) ||
                    (!c.isBilateralContract() && c.getAcceptor() != null && !c.isCompleted())).count(),
                "§7Sayfa: §e" + page + "§7/§e" + totalPages)));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Benim Kontratlarım menüsü tıklama
     */
    private void handleMyContractsMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (clicked.getType()) {
            case ARROW:
                // Sayfalama veya geri
                int currentPage = currentPages.getOrDefault(player.getUniqueId(), 1);
                if (event.getSlot() == 45) {
                    // Önceki sayfa
                    openMyContractsMenu(player, currentPage - 1);
                } else if (event.getSlot() == 53) {
                    // Sonraki sayfa
                    openMyContractsMenu(player, currentPage + 1);
                } else if (event.getSlot() == 49) {
                    // Geri
                    openMainMenu(player, 1);
                }
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
     * Kabul Edilen Kontratlarım menüsü (Bu oyuncunun kabul ettiği kontratlar)
     */
    private void openAcceptedContractsMenu(Player player, int page) {
        if (player == null || contractManager == null) return;
        
        List<Contract> contracts = contractManager.getContracts();
        if (contracts == null) contracts = new ArrayList<>();
        
        // Sadece bu oyuncunun kabul ettiği kontratları göster
        List<Contract> acceptedContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract != null && contract.getAcceptor() != null && 
                contract.getAcceptor().equals(player.getUniqueId()) && 
                !contract.isExpired() && !contract.isBreached()) {
                acceptedContracts.add(contract);
            }
        }
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(acceptedContracts.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        // 54 slotlu menü oluştur
        Inventory menu = Bukkit.createInventory(null, 54, "§aKabul Ettiğim Kontratlar - Sayfa " + page);
        
        // Kontratları göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, acceptedContracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = acceptedContracts.get(i);
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
        
        if (endIndex < acceptedContracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        // Bilgi butonu (Slot 50)
        menu.setItem(50, createButton(Material.BOOK, "§eBilgi", 
            Arrays.asList("§7Toplam Kontrat: §e" + acceptedContracts.size(),
                "§7Devam Eden: §e" + acceptedContracts.stream().filter(c -> !c.isCompleted()).count(),
                "§7Tamamlanan: §a" + acceptedContracts.stream().filter(c -> c.isCompleted()).count(),
                "§7Sayfa: §e" + page + "§7/§e" + totalPages,
                "§7",
                "§7Bu kontratları tamamlamak için",
                "§7/kontrat teslim komutunu kullanın.")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Kabul Edilen Kontratlarım menüsü tıklama
     */
    private void handleAcceptedContractsMenuClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (clicked.getType()) {
            case ARROW:
                // Sayfalama veya geri
                int currentPage = currentPages.getOrDefault(player.getUniqueId(), 1);
                String title = event.getView().getTitle();
                if (title.startsWith("§aKabul Ettiğim Kontratlar")) {
                    if (event.getSlot() == 45) {
                        // Önceki sayfa
                        openAcceptedContractsMenu(player, currentPage - 1);
                    } else if (event.getSlot() == 53) {
                        // Sonraki sayfa
                        openAcceptedContractsMenu(player, currentPage + 1);
                    } else if (event.getSlot() == 49) {
                        // Geri
                        openMainMenu(player, 1);
                    }
                }
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
    }
    
    /**
     * Gelen İstekler menüsü (ÇİFT TARAFLI KONTRAT SİSTEMİ)
     */
    private void openIncomingRequestsMenu(Player player, int page) {
        if (player == null || contractRequestManager == null) return;
        
        List<me.mami.stratocraft.model.ContractRequest> requests = 
            contractRequestManager.getPendingRequests(player.getUniqueId());
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(requests.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§eGelen İstekler - Sayfa " + page);
        
        // İstekleri göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, requests.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            me.mami.stratocraft.model.ContractRequest request = requests.get(i);
            if (request != null) {
                org.bukkit.OfflinePlayer sender = Bukkit.getOfflinePlayer(request.getSender());
                String senderName = sender.getName() != null ? sender.getName() : "Bilinmeyen";
                
                long timeAgo = System.currentTimeMillis() - request.getCreatedAt();
                String timeStr = formatTimeAgo(timeAgo);
                
                // ✅ DÜZELTME: Gönderenin şartlarını göster
                me.mami.stratocraft.model.ContractTerms senderTerms = null;
                if (contractTermsManager != null) {
                    senderTerms = contractTermsManager.getTermsByRequest(request.getId(), request.getSender());
                }
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Gönderen: §e" + senderName);
                lore.add("§7Kapsam: §e" + getContractScopeName(request.getScope()));
                lore.add("§7Tarih: §7" + timeStr);
                lore.add("§7═══════════════════════");
                
                // ✅ DÜZELTME: Şartları göster (3 parametreli versiyon kullan - daha detaylı)
                if (senderTerms != null) {
                    lore.add("§7§lGönderenin Şartları:");
                    lore.addAll(createTermsLore(senderTerms, senderName, false));
                } else {
                    lore.add("§7§lŞartlar: §cHenüz belirlenmedi");
                }
                
                lore.add("§7═══════════════════════");
                lore.add("§aSol Tık: §7Kabul Et (Şartları Direkt Kabul)");
                lore.add("§eOrta Tık: §7Şart Ekle (Kendi Şartlarını Belirle)");
                lore.add("§cSağ Tık: §7Reddet");
                
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§e" + senderName + " - Kontrat İsteği");
                    meta.setLore(lore);
                    // Request ID'yi NBT'ye ekle
                    org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                    meta.getPersistentDataContainer().set(requestKey, 
                        org.bukkit.persistence.PersistentDataType.STRING, request.getId().toString());
                    item.setItemMeta(meta);
                }
                menu.setItem(slot++, item);
            }
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", 
                Arrays.asList("§7Sayfa " + (page - 1))));
        }
        if (endIndex < requests.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Gelen İstekler menüsü tıklama
     */
    private void handleIncomingRequestsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            int slot = event.getSlot();
            String title = event.getView().getTitle();
            int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
            
            if (slot == 45) {
                openIncomingRequestsMenu(player, currentPage - 1);
            } else if (slot == 53) {
                openIncomingRequestsMenu(player, currentPage + 1);
            } else if (slot == 49) {
                openMainMenu(player, 1);
            }
            return;
        }
        
        // Request ID'yi NBT'den al
        UUID requestId = getRequestIdFromItem(clicked);
        if (requestId == null || contractRequestManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        // ✅ DÜZELTME: Sol tık = Direkt kabul (şartları kabul et), Orta tık = Şart ekle, Sağ tık = Reddet
        if (event.getClick() == org.bukkit.event.inventory.ClickType.LEFT) {
            // Direkt kabul - karşı tarafın şartlarını kabul et
            if (contractRequestManager.acceptRequest(requestId, player.getUniqueId())) {
                // Gönderenin şartlarını kontrol et
                me.mami.stratocraft.model.ContractTerms senderTerms = null;
                if (contractTermsManager != null) {
                    senderTerms = contractTermsManager.getTermsByRequest(requestId, request.getSender());
                }
                
                if (senderTerms != null) {
                    // ✅ DÜZELTME: Şartları direkt kabul et (kendi şartlarını eklemeden)
                    // Bu durumda sadece gönderenin şartları geçerli olacak
                    player.sendMessage("§6═══════════════════════════════════");
                    player.sendMessage("§a§lKONTRAT KABUL EDİLDİ!");
                    player.sendMessage("§7Karşı tarafın şartlarını kabul ettiniz");
                    player.sendMessage("§7İlk gönderen oyuncu onayladığında kontrat aktif olacak");
                    player.sendMessage("§6═══════════════════════════════════");
                    
                    // ✅ DÜZELTME: İlk gönderen oyuncuya son onay mesajı gönder
                    org.bukkit.entity.Player senderPlayer = Bukkit.getPlayer(request.getSender());
                    if (senderPlayer != null && senderPlayer.isOnline()) {
                        senderPlayer.sendMessage("§6═══════════════════════════════════");
                        senderPlayer.sendMessage("§e§lSON ONAY GEREKİYOR!");
                        senderPlayer.sendMessage("§7" + player.getName() + " şartlarınızı kabul etti");
                        senderPlayer.sendMessage("§7Şimdi kontratı son kez onaylamanız gerekiyor");
                        senderPlayer.sendMessage("§7Kabul ederseniz kontrat aktif olacak");
                        senderPlayer.sendMessage("§7Reddederseniz kontrat iptal olacak");
                        senderPlayer.sendMessage("§6═══════════════════════════════════");
                        
                        // HUD'a bildirim gönder
                        sendContractNotification(senderPlayer, "Son onay gerekiyor: " + player.getName(), 
                            me.mami.stratocraft.manager.HUDManager.ContractNotificationType.WARNING);
                        
                        // İlk gönderen oyuncuya son onay menüsü aç
                        openFinalApprovalMenu(senderPlayer, requestId);
                    }
                    
                    player.closeInventory();
                } else {
                    // Şartlar henüz belirlenmemiş, normal kabul akışı
                    player.sendMessage("§aKontrat isteği kabul edildi!");
                    player.closeInventory();
                    openContractDecisionMenu(player, requestId);
                }
                
                // HUD'a bildirim gönder
                sendContractNotification(player, "İstek kabul edildi", 
                    me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
            } else {
                player.sendMessage("§cİstek kabul edilemedi!");
            }
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
            // Şart Ekle - Şart belirleme wizard'ı başlat
            if (contractRequestManager.acceptRequest(requestId, player.getUniqueId())) {
                player.closeInventory();
                openContractDecisionMenu(player, requestId);
            } else {
                player.sendMessage("§cİstek kabul edilemedi!");
            }
        } else if (event.getClick() == org.bukkit.event.inventory.ClickType.RIGHT) {
            // Reddet
            if (contractRequestManager.rejectRequest(requestId, player.getUniqueId())) {
                player.sendMessage("§cKontrat isteği reddedildi.");
                
                // HUD'a bildirim gönder
                sendContractNotification(player, "İstek reddedildi", 
                    me.mami.stratocraft.manager.HUDManager.ContractNotificationType.WARNING);
                
                openIncomingRequestsMenu(player, 1);
            } else {
                player.sendMessage("§cİstek reddedilemedi!");
            }
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kabul Edilen İstekler menüsü (ÇİFT TARAFLI KONTRAT SİSTEMİ)
     */
    private void openAcceptedRequestsMenu(Player player, int page) {
        if (player == null || contractRequestManager == null) return;
        
        List<me.mami.stratocraft.model.ContractRequest> requests = 
            contractRequestManager.getAcceptedRequests(player.getUniqueId());
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(requests.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§aKabul Edilen İstekler - Sayfa " + page);
        
        // İstekleri göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, requests.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            me.mami.stratocraft.model.ContractRequest request = requests.get(i);
            if (request != null) {
                UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
                    request.getTarget() : request.getSender();
                org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
                String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
                
                // Şartlar hazır mı kontrol et
                me.mami.stratocraft.model.ContractTerms myTerms = null;
                me.mami.stratocraft.model.ContractTerms otherTerms = null;
                if (contractTermsManager != null) {
                    myTerms = contractTermsManager.getTermsByRequest(request.getId(), player.getUniqueId());
                    otherTerms = contractTermsManager.getTermsByRequest(request.getId(), otherPlayerId);
                }
                
                String status;
                Material icon;
                if (myTerms == null) {
                    status = "§7Şartlarınızı Belirleyin";
                    icon = Material.WRITABLE_BOOK;
                } else if (otherTerms == null) {
                    status = "§eKarşı Taraf Şartlarını Belirliyor";
                    icon = Material.BOOK;
                } else if (!myTerms.isApproved() || !otherTerms.isApproved()) {
                    status = "§6Onay Bekleniyor";
                    icon = Material.PAPER;
                } else {
                    status = "§aKontrat Aktif";
                    icon = Material.EMERALD;
                }
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Oyuncu: §e" + otherName);
                lore.add("§7Durum: " + status);
                lore.add("§7═══════════════════════");
                if (myTerms == null) {
                    lore.add("§aSol Tık: §7Şartları Belirle");
                } else if (otherTerms != null && (!myTerms.isApproved() || !otherTerms.isApproved())) {
                    lore.add("§aSol Tık: §7Şartları Görüntüle ve Onayla");
                } else {
                    lore.add("§aSol Tık: §7Kontratı Görüntüle");
                }
                
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§e" + otherName + " ile Kontrat");
                    meta.setLore(lore);
                    // Request ID'yi NBT'ye ekle
                    org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                    meta.getPersistentDataContainer().set(requestKey, 
                        org.bukkit.persistence.PersistentDataType.STRING, request.getId().toString());
                    item.setItemMeta(meta);
                }
                menu.setItem(slot++, item);
            }
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", 
                Arrays.asList("§7Sayfa " + (page - 1))));
        }
        if (endIndex < requests.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Kabul Edilen İstekler menüsü tıklama
     */
    private void handleAcceptedRequestsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            int slot = event.getSlot();
            String title = event.getView().getTitle();
            int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
            
            if (slot == 45) {
                openAcceptedRequestsMenu(player, currentPage - 1);
            } else if (slot == 53) {
                openAcceptedRequestsMenu(player, currentPage + 1);
            } else if (slot == 49) {
                openMainMenu(player, 1);
            }
            return;
        }
        
        // Request ID'yi NBT'den al
        UUID requestId = getRequestIdFromItem(clicked);
        if (requestId == null || contractRequestManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        
        // Şartlar hazır mı kontrol et
        me.mami.stratocraft.model.ContractTerms myTerms = null;
        me.mami.stratocraft.model.ContractTerms otherTerms = null;
        if (contractTermsManager != null) {
            myTerms = contractTermsManager.getTermsByRequest(request.getId(), player.getUniqueId());
            otherTerms = contractTermsManager.getTermsByRequest(request.getId(), otherPlayerId);
        }
        
        if (myTerms == null) {
            // Şartları belirle
            startTermsWizard(player, request.getId());
        } else if (otherTerms != null && (!myTerms.isApproved() || !otherTerms.isApproved())) {
            // Şartları görüntüle ve onayla
            openTermsViewMenu(player, request.getId());
        } else {
            // Kontrat aktif, detayları göster
            // Kontrat ID'yi bul
            Contract bilateralContract = null;
            if (contractManager != null) {
                for (Contract c : contractManager.getContracts()) {
                    if (c != null && c.isBilateralContract() && 
                        c.getContractRequestId() != null && c.getContractRequestId().equals(request.getId())) {
                        bilateralContract = c;
                        break;
                    }
                }
            }
            
            if (bilateralContract != null) {
                openBilateralContractDetailMenu(player, bilateralContract);
            } else {
                player.sendMessage("§cKontrat bulunamadı!");
            }
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Request ID'yi item'dan al
     */
    private UUID getRequestIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
        String requestIdStr = meta.getPersistentDataContainer().get(requestKey, 
            org.bukkit.persistence.PersistentDataType.STRING);
        if (requestIdStr == null) return null;
        
        try {
            return UUID.fromString(requestIdStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Zaman formatı (geçen süre)
     */
    private String formatTimeAgo(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + " saniye önce";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " dakika önce";
        long hours = minutes / 60;
        if (hours < 24) return hours + " saat önce";
        long days = hours / 24;
        return days + " gün önce";
    }
    
    /**
     * Şart belirleme wizard'ı başlat (çift taraflı kontrat için)
     * ✅ DÜZELTME: Oyuncu seçimi yapılmamalı, scope zaten PLAYER_TO_PLAYER
     */
    private void startTermsWizard(Player player, UUID requestId) {
        if (player == null || requestId == null || contractRequestManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) {
            player.sendMessage("§cİstek bulunamadı!");
            return;
        }
        
        // Wizard state oluştur
        ContractWizardState state = new ContractWizardState();
        state.contractRequestId = requestId; // Request ID'yi sakla
        state.scope = request.getScope(); // ✅ Scope'u request'ten al (zaten PLAYER_TO_PLAYER)
        // ✅ Oyuncu seçimi yapılmamalı - request'te zaten var
        wizardStates.put(player.getUniqueId(), state);
        
        // ✅ DÜZELTME: Tip seçim menüsünü aç (scope seçimi yapılmayacak)
        openTypeSelectionMenu(player);
    }
    
    /**
     * Şart Onaylama Menüsü (İlk gönderen oyuncu için)
     * Karşı tarafın şartlarını görüp onaylamak için
     */
    private void openTermsApprovalMenu(Player player, UUID requestId) {
        if (player == null || requestId == null || contractRequestManager == null || 
            contractTermsManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
        String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
        
        me.mami.stratocraft.model.ContractTerms myTerms = 
            contractTermsManager.getTermsByRequest(requestId, player.getUniqueId());
        me.mami.stratocraft.model.ContractTerms otherTerms = 
            contractTermsManager.getTermsByRequest(requestId, otherPlayerId);
        
        if (myTerms == null || otherTerms == null) {
            player.sendMessage("§cŞartlar henüz hazır değil!");
            return;
        }
        
        // ✅ İYİLEŞTİRME: Daha açıklayıcı başlık
        Inventory menu = Bukkit.createInventory(null, 54, "§6⚠️ SON ONAY GEREKİYOR!");
        
        // ✅ İYİLEŞTİRME: Başlık açıklaması (Slot 4)
        List<String> headerLore = new ArrayList<>();
        headerLore.add("§7═══════════════════════");
        headerLore.add("§e" + otherName + " şartlarınızı kabul etti");
        headerLore.add("§7(veya şartlarını belirledi).");
        headerLore.add("§7");
        headerLore.add("§7ℹ️ Her iki tarafın şartlarını onaylarsanız");
        headerLore.add("§7kontrat aktif olacak.");
        headerLore.add("§7═══════════════════════");
        menu.setItem(4, createButton(Material.BOOK, "§e§lSON ONAY GEREKİYOR", headerLore));
        
        // ✅ İYİLEŞTİRME: Senin şartların (Slot 20 - sol taraf)
        List<String> myLore = new ArrayList<>();
        myLore.add("§7═══════════════════════");
        myLore.add("§e§lSİZİN ŞARTLARINIZ:");
        myLore.add("§7═══════════════════════");
        myLore.addAll(createTermsLore(myTerms, otherName, true));
        menu.setItem(20, createButton(Material.BOOK, "§e§lSİZİN ŞARTLARINIZ", myLore));
        
        // ✅ İYİLEŞTİRME: Karşı tarafın şartları (Slot 24 - sağ taraf)
        List<String> otherLore = new ArrayList<>();
        otherLore.add("§7═══════════════════════");
        otherLore.add("§7§l" + otherName.toUpperCase() + "'NİN ŞARTLARI:");
        otherLore.add("§7═══════════════════════");
        otherLore.addAll(createTermsLore(otherTerms, otherName, false));
        otherLore.add("§7");
        otherLore.add("§7⚠️ Bu şartları onaylamanız gerekiyor");
        menu.setItem(24, createButton(Material.WRITABLE_BOOK, "§7§l" + otherName.toUpperCase() + "'NİN ŞARTLARI", otherLore));
        
        // ✅ İYİLEŞTİRME: Onay butonu (Slot 22 - ortada)
        List<String> approveLore = new ArrayList<>();
        if (!myTerms.isApproved()) {
            approveLore.add("§7Her iki tarafın şartlarını onayla");
            approveLore.add("§7Kontrat aktif hale gelecek");
            menu.setItem(22, createButton(Material.GREEN_CONCRETE, "§a§l✅ ONAYLA", approveLore));
        } else {
            approveLore.add("§7Kontratı onayladınız");
            approveLore.add("§7Karşı taraf onayladığında aktif olacak");
            menu.setItem(22, createButton(Material.EMERALD, "§aOnaylandı", approveLore));
        }
        
        // Request ID'yi NBT'ye ekle
        ItemStack approveItem = menu.getItem(22);
        if (approveItem != null) {
            ItemMeta meta = approveItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                meta.getPersistentDataContainer().set(requestKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, requestId.toString());
                approveItem.setItemMeta(meta);
                menu.setItem(22, approveItem);
            }
        }
        
        // ✅ İYİLEŞTİRME: Reddet butonu (Slot 40)
        menu.setItem(40, createButton(Material.RED_CONCRETE, "§c§l❌ REDDET", 
            Arrays.asList("§7Kontratı reddet ve iptal et")));
        
        // Request ID'yi reddet butonuna da ekle
        ItemStack rejectItem = menu.getItem(40);
        if (rejectItem != null) {
            ItemMeta meta = rejectItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                meta.getPersistentDataContainer().set(requestKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, requestId.toString());
                rejectItem.setItemMeta(meta);
                menu.setItem(40, rejectItem);
            }
        }
        
        // Geri butonu (Slot 0)
        menu.setItem(0, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Önceki menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Şartları Onayla menüsü tıklama (İlk gönderen oyuncu için)
     */
    private void handleTermsApprovalClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        // ✅ İYİLEŞTİRME: Geri butonu (Slot 0)
        if (slot == 0 && clicked.getType() == Material.ARROW) {
            player.closeInventory();
            openMainMenu(player, 1);
            return;
        }
        
        // Request ID'yi NBT'den al
        UUID requestId = getRequestIdFromItem(clicked);
        if (requestId == null || contractRequestManager == null || contractTermsManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        
        me.mami.stratocraft.model.ContractTerms myTerms = 
            contractTermsManager.getTermsByRequest(requestId, player.getUniqueId());
        me.mami.stratocraft.model.ContractTerms otherTerms = 
            contractTermsManager.getTermsByRequest(requestId, otherPlayerId);
        
        // ✅ İYİLEŞTİRME: Onay butonu (Slot 22)
        if (slot == 22 && (clicked.getType() == Material.GREEN_CONCRETE || clicked.getType() == Material.EMERALD)) {
            // Onayla
            if (myTerms != null && !myTerms.isApproved()) {
                contractTermsManager.approveTerms(myTerms.getId(), player.getUniqueId());
                
                // Karşı tarafın şartları da onaylandı mı kontrol et
                if (otherTerms != null && otherTerms.isApproved()) {
                    // Her iki taraf da onayladı, kontratı oluştur
                    createBilateralContract(request, 
                        request.getSender().equals(player.getUniqueId()) ? myTerms : otherTerms,
                        request.getSender().equals(player.getUniqueId()) ? otherTerms : myTerms);
                    
                    player.sendMessage("§6═══════════════════════════════════");
                    player.sendMessage("§a§lKONTRAT AKTİF!");
                    player.sendMessage("§7Her iki taraf da onayladı");
                    player.sendMessage("§7Kontrat aktif hale geldi");
                    player.closeInventory();
                } else {
                    player.sendMessage("§aŞartlarınız onaylandı!");
                    player.sendMessage("§7Karşı taraf onayladığında kontrat aktif olacak");
                    openTermsApprovalMenu(player, requestId);
                }
            } else {
                player.sendMessage("§cŞartlar zaten onaylanmış!");
            }
        } 
        // ✅ İYİLEŞTİRME: Reddet butonu (Slot 40)
        else if (slot == 40 && clicked.getType() == Material.RED_CONCRETE) {
            // Reddet
            if (contractRequestManager.rejectRequest(requestId, player.getUniqueId())) {
                player.sendMessage("§6═══════════════════════════════════");
                player.sendMessage("§c§lKONTRAT REDDEDİLDİ!");
                player.sendMessage("§7Kontrat iptal edildi.");
                player.sendMessage("§6═══════════════════════════════════");
                
                // Karşı tarafa bildirim
                org.bukkit.entity.Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
                if (otherPlayer != null && otherPlayer.isOnline()) {
                    otherPlayer.sendMessage("§6═══════════════════════════════════");
                    otherPlayer.sendMessage("§c" + player.getName() + " kontratı reddetti.");
                    otherPlayer.sendMessage("§7Kontrat iptal edildi.");
                    otherPlayer.sendMessage("§6═══════════════════════════════════");
                }
                
                player.closeInventory();
            } else {
                player.sendMessage("§cKontrat reddedilemedi!");
            }
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Şart görüntüleme menüsü
     */
    private void openTermsViewMenu(Player player, UUID requestId) {
        if (player == null || requestId == null || contractRequestManager == null || 
            contractTermsManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
        String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
        
        me.mami.stratocraft.model.ContractTerms myTerms = 
            contractTermsManager.getTermsByRequest(requestId, player.getUniqueId());
        me.mami.stratocraft.model.ContractTerms otherTerms = 
            contractTermsManager.getTermsByRequest(requestId, otherPlayerId);
        
        if (myTerms == null || otherTerms == null) {
            player.sendMessage("§cŞartlar henüz hazır değil!");
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Şartları");
        
        // Senin şartların (Slot 11)
        List<String> myLore = createTermsLore(myTerms, otherName, true);
        menu.setItem(11, createButton(Material.BOOK, "§e§lSENİN ŞARTLARIN", myLore));
        
        // Karşı tarafın şartları (Slot 15)
        List<String> otherLore = createTermsLore(otherTerms, otherName, false);
        menu.setItem(15, createButton(Material.BOOK, "§7§l" + otherName.toUpperCase() + "'NİN ŞARTLARI", otherLore));
        
        // Onay butonu (Slot 13)
        if (!myTerms.isApproved()) {
            menu.setItem(13, createButton(Material.GREEN_CONCRETE, "§a§lKONTRA TI ONAYLA", 
                Arrays.asList("§7Her iki tarafın şartlarını onayla",
                    "§7Kontrat aktif hale gelecek")));
        } else {
            menu.setItem(13, createButton(Material.EMERALD, "§aOnaylandı", 
                Arrays.asList("§7Kontratı onayladınız",
                    "§7Karşı taraf onayladığında aktif olacak")));
        }
        
        // Geri butonu (Slot 18)
        menu.setItem(18, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Önceki menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Şart lore oluştur
     */
    private List<String> createTermsLore(me.mami.stratocraft.model.ContractTerms terms, String otherName, boolean isMine) {
        List<String> lore = new ArrayList<>();
        lore.add("§7═══════════════════════");
        
        if (terms.getType() == me.mami.stratocraft.enums.ContractType.RESOURCE_COLLECTION) {
            lore.add("§7Tip: §eKaynak Toplama");
            if (terms.getMaterial() != null) {
                lore.add("§7Malzeme: §e" + getMaterialDisplayName(terms.getMaterial()));
                lore.add("§7Miktar: §e" + terms.getAmount());
            }
        } else if (terms.getType() == me.mami.stratocraft.enums.ContractType.COMBAT) {
            lore.add("§7Tip: §cSavaş");
            if (terms.getTargetPlayer() != null) {
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(terms.getTargetPlayer());
                lore.add("§7Hedef: §c" + (target.getName() != null ? target.getName() : "Bilinmeyen"));
            }
        }
        
        lore.add("§7Süre: §e" + formatTime(terms.getDeadline()));
        lore.add("§7Ödül: §a" + terms.getReward() + " Altın");
        lore.add("§7Ceza: §c" + terms.getPenalty() + " Altın");
        lore.add("§7═══════════════════════");
        
        if (isMine) {
            lore.add("§7" + otherName + " bu şartları");
            lore.add("§7tamamlamalı.");
        } else {
            lore.add("§7Sen bu şartları");
            lore.add("§7tamamlamalısın.");
        }
        
        return lore;
    }
    
    /**
     * Şart görüntüleme menüsü tıklama
     */
    private void handleTermsViewClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.GREEN_CONCRETE) {
            // Onay butonu - Request ID'yi item'dan al
            // Geçici çözüm: viewingContract map'ini kullan
            // TODO: Daha iyi bir yöntem (request ID'yi NBT'ye ekle)
            UUID requestId = null;
            
            // Tüm kabul edilen istekleri kontrol et
            if (contractRequestManager != null) {
                List<me.mami.stratocraft.model.ContractRequest> acceptedRequests = 
                    contractRequestManager.getAcceptedRequests(player.getUniqueId());
                
                for (me.mami.stratocraft.model.ContractRequest req : acceptedRequests) {
                    if (contractTermsManager != null) {
                        me.mami.stratocraft.model.ContractTerms myTerms = 
                            contractTermsManager.getTermsByRequest(req.getId(), player.getUniqueId());
                        me.mami.stratocraft.model.ContractTerms otherTerms = null;
                        UUID otherPlayerId = req.getSender().equals(player.getUniqueId()) ? 
                            req.getTarget() : req.getSender();
                        if (otherPlayerId != null) {
                            otherTerms = contractTermsManager.getTermsByRequest(req.getId(), otherPlayerId);
                        }
                        
                        if (myTerms != null && otherTerms != null && !myTerms.isApproved()) {
                            requestId = req.getId();
                            break;
                        }
                    }
                }
            }
            
            if (requestId != null && contractTermsManager != null) {
                me.mami.stratocraft.model.ContractTerms myTerms = 
                    contractTermsManager.getTermsByRequest(requestId, player.getUniqueId());
                
                if (myTerms != null && !myTerms.isApproved()) {
                    // Şartları onayla
                    contractTermsManager.approveTerms(myTerms.getId(), player.getUniqueId());
                    
                    // Karşı tarafın şartları da onaylandı mı kontrol et
                    me.mami.stratocraft.model.ContractRequest request = 
                        contractRequestManager != null ? contractRequestManager.getRequest(requestId) : null;
                    if (request != null) {
                        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
                            request.getTarget() : request.getSender();
                        me.mami.stratocraft.model.ContractTerms otherTerms = 
                            contractTermsManager.getTermsByRequest(requestId, otherPlayerId);
                        
                        if (otherTerms != null && otherTerms.isApproved()) {
                            // Her iki taraf da onayladı, kontratı oluştur
                            createBilateralContract(request, 
                                request.getSender().equals(player.getUniqueId()) ? myTerms : otherTerms,
                                request.getSender().equals(player.getUniqueId()) ? otherTerms : myTerms);
                            
                            player.sendMessage("§6═══════════════════════════════════");
                            player.sendMessage("§a§lKONTRAT AKTİF!");
                            player.sendMessage("§7Her iki taraf da onayladı");
                            player.sendMessage("§7Kontrat aktif hale geldi");
                            player.closeInventory();
                        } else {
                            player.sendMessage("§aŞartlarınız onaylandı!");
                            player.sendMessage("§7Karşı taraf onayladığında kontrat aktif olacak");
                            openAcceptedRequestsMenu(player, 1);
                        }
                    }
                } else {
                    player.sendMessage("§cŞartlar zaten onaylanmış!");
                }
            } else {
                player.sendMessage("§cOnaylanacak şart bulunamadı!");
            }
        } else if (clicked.getType() == Material.ARROW) {
            // Geri
            openAcceptedRequestsMenu(player, 1);
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kontrat Karar Menüsü (Şart Ekle veya Kontratı Bitir)
     * İstek kabul edildikten sonra açılır
     */
    private void openContractDecisionMenu(Player player, UUID requestId) {
        if (player == null || requestId == null || contractRequestManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) {
            player.sendMessage("§cİstek bulunamadı!");
            return;
        }
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
        String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Kontrat Kararı");
        
        // Bilgi
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7═══════════════════════");
        infoLore.add("§7Kontrat İsteği Kabul Edildi");
        infoLore.add("§7");
        infoLore.add("§7Karşı Taraf: §e" + otherName);
        infoLore.add("§7");
        infoLore.add("§7Şimdi ne yapmak istersiniz?");
        infoLore.add("§7═══════════════════════");
        menu.setItem(13, createButton(Material.BOOK, "§eKontrat Kararı", infoLore));
        
        // Şart Ekle butonu (Slot 11)
        List<String> addTermsLore = new ArrayList<>();
        addTermsLore.add("§7═══════════════════════");
        addTermsLore.add("§7Çift Taraflı Kontrat");
        addTermsLore.add("§7");
        addTermsLore.add("§7Kendi şartlarınızı belirleyin");
        addTermsLore.add("§7Karşı taraf da şartlarını belirleyecek");
        addTermsLore.add("§7Her iki taraf onayladığında");
        addTermsLore.add("§7kontrat aktif olacak");
        addTermsLore.add("§7═══════════════════════");
        menu.setItem(11, createButton(Material.WRITABLE_BOOK, "§a§lŞart Ekle", addTermsLore));
        
        // ✅ DÜZELTME: Kontratı Bitir butonu (Slot 15) - Direkt karşı tarafın şartlarını kabul et
        List<String> finishLore = new ArrayList<>();
        finishLore.add("§7═══════════════════════");
        finishLore.add("§7Direkt Kabul");
        finishLore.add("§7");
        finishLore.add("§7Karşı tarafın şartlarını");
        finishLore.add("§7direkt kabul edersiniz");
        finishLore.add("§7Kendi şartlarınız olmayacak");
        finishLore.add("§7Kontrat direkt aktif olacak");
        finishLore.add("§7═══════════════════════");
        menu.setItem(15, createButton(Material.GREEN_CONCRETE, "§a§lKabul Et (Direkt)", finishLore));
        
        // Request ID'yi NBT'ye ekle (her iki butona da)
        ItemStack addTermsItem = menu.getItem(11);
        ItemStack finishItem = menu.getItem(15);
        if (addTermsItem != null) {
            ItemMeta meta = addTermsItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                meta.getPersistentDataContainer().set(requestKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, requestId.toString());
                addTermsItem.setItemMeta(meta);
                menu.setItem(11, addTermsItem);
            }
        }
        if (finishItem != null) {
            ItemMeta meta = finishItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                meta.getPersistentDataContainer().set(requestKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, requestId.toString());
                finishItem.setItemMeta(meta);
                menu.setItem(15, finishItem);
            }
        }
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Kontrat Karar Menüsü tıklama
     */
    private void handleContractDecisionClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Request ID'yi NBT'den al
        UUID requestId = getRequestIdFromItem(clicked);
        if (requestId == null || contractRequestManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        switch (clicked.getType()) {
            case WRITABLE_BOOK:
                // Şart Ekle - Şart belirleme wizard'ı başlat
                player.closeInventory();
                startTermsWizard(player, requestId);
                break;
                
            case GREEN_CONCRETE:
                // Kontratı Bitir - Tek taraflı kontrat oluştur
                player.closeInventory();
                createUnilateralContract(player, request);
                break;
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Tek taraflı kontrat oluştur (sadece ilk gönderenin şartlarıyla)
     * İkinci oyuncu şart eklemediği için direkt kontrat oluşturulur
     */
    private void createUnilateralContract(Player player, me.mami.stratocraft.model.ContractRequest request) {
        if (contractManager == null || request == null) return;
        
        UUID senderId = request.getSender();
        UUID targetId = request.getTarget();
        
        // İlk gönderen oyuncunun şartlarını kontrol et
        me.mami.stratocraft.model.ContractTerms senderTerms = null;
        if (contractTermsManager != null) {
            senderTerms = contractTermsManager.getTermsByRequest(request.getId(), senderId);
        }
        
        // Eğer ilk gönderenin şartları yoksa, hata ver
        if (senderTerms == null) {
            player.sendMessage("§cİlk gönderen oyuncunun şartları bulunamadı!");
            player.sendMessage("§7Tek taraflı kontrat için ilk gönderenin şartları gerekli.");
            player.sendMessage("§7İlk gönderen oyuncu önce şartlarını belirlemeli.");
            return;
        }
        
        // YENİ: Tek taraflı kontrat için de ilk gönderen oyuncuya son onay bildirimi gönder
        org.bukkit.entity.Player senderPlayer = Bukkit.getPlayer(senderId);
        org.bukkit.entity.Player targetPlayer = Bukkit.getPlayer(targetId);
        
        if (senderPlayer != null && senderPlayer.isOnline()) {
            senderPlayer.sendMessage("§6═══════════════════════════════════");
            senderPlayer.sendMessage("§e§lSON ONAY GEREKİYOR!");
            senderPlayer.sendMessage("§7" + (targetPlayer != null ? targetPlayer.getName() : "Bilinmeyen") + 
                " kontratı kabul etti");
            senderPlayer.sendMessage("§7Şimdi kontratı son kez onaylamanız gerekiyor");
            senderPlayer.sendMessage("§7Kabul ederseniz kontrat aktif olacak");
            senderPlayer.sendMessage("§7Reddederseniz kontrat iptal olacak");
            senderPlayer.sendMessage("§6═══════════════════════════════════");
            
            // HUD'a bildirim gönder
            sendContractNotification(senderPlayer, "Son onay gerekiyor", 
                me.mami.stratocraft.manager.HUDManager.ContractNotificationType.WARNING);
            
            // İlk gönderen oyuncuya son onay menüsü aç
            openFinalApprovalMenu(senderPlayer, request.getId());
        }
        
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage("§6═══════════════════════════════════");
            targetPlayer.sendMessage("§e§lKONTRA T KABUL EDİLDİ!");
            targetPlayer.sendMessage("§7İlk gönderen oyuncu onayladığında kontrat aktif olacak");
            targetPlayer.sendMessage("§6═══════════════════════════════════");
        }
    }
    
    /**
     * Son Onay Menüsü (İlk gönderen oyuncu için)
     * İkinci oyuncu şartlarını yazdıktan sonra açılır
     * İlk gönderen oyuncu kabul ederse kontrat aktif, reddederse iptal olur
     */
    private void openFinalApprovalMenu(Player player, UUID requestId) {
        if (player == null || requestId == null || contractRequestManager == null || 
            contractTermsManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        org.bukkit.OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(otherPlayerId);
        String otherName = otherPlayer.getName() != null ? otherPlayer.getName() : "Bilinmeyen";
        
        me.mami.stratocraft.model.ContractTerms myTerms = 
            contractTermsManager.getTermsByRequest(requestId, player.getUniqueId());
        me.mami.stratocraft.model.ContractTerms otherTerms = 
            contractTermsManager.getTermsByRequest(requestId, otherPlayerId);
        
        if (myTerms == null) {
            player.sendMessage("§cŞartlar henüz hazır değil!");
            return;
        }
        
        Inventory menu = Bukkit.createInventory(null, 27, "§6Son Onay");
        
        // Bilgi (Slot 13)
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7═══════════════════════");
        infoLore.add("§7Kontrat Son Onayı");
        infoLore.add("§7");
        infoLore.add("§7Karşı Taraf: §e" + otherName);
        infoLore.add("§7");
        if (otherTerms != null) {
            infoLore.add("§7Karşı taraf şartlarını belirledi");
            infoLore.add("§7Şimdi kontratı son kez onaylamanız gerekiyor");
        } else {
            infoLore.add("§7Karşı taraf kontratı kabul etti");
            infoLore.add("§7Şimdi kontratı son kez onaylamanız gerekiyor");
        }
        infoLore.add("§7");
        infoLore.add("§7Kabul ederseniz kontrat aktif olacak");
        infoLore.add("§7Reddederseniz kontrat iptal olacak");
        infoLore.add("§7═══════════════════════");
        menu.setItem(13, createButton(Material.BOOK, "§eSon Onay", infoLore));
        
        // Şartları Görüntüle butonu (Slot 11) - Eğer çift taraflı ise
        if (otherTerms != null) {
            List<String> termsLore = createTermsLore(otherTerms, otherName, false);
            termsLore.add("§7");
            termsLore.add("§7§lKARŞI TARAFIN ŞARTLARI");
            menu.setItem(11, createButton(Material.WRITABLE_BOOK, "§7§lŞartları Görüntüle", termsLore));
        }
        
        // Kabul Et butonu (Slot 12)
        menu.setItem(12, createButton(Material.GREEN_CONCRETE, "§a§lKABUL ET", 
            Arrays.asList("§7Kontratı kabul et",
                "§7Kontrat aktif hale gelecek")));
        
        // Reddet butonu (Slot 14)
        menu.setItem(14, createButton(Material.RED_CONCRETE, "§c§lREDDET", 
            Arrays.asList("§7Kontratı reddet",
                "§7Kontrat iptal olacak")));
        
        // Request ID'yi NBT'ye ekle (her iki butona da)
        ItemStack acceptItem = menu.getItem(12);
        ItemStack rejectItem = menu.getItem(14);
        if (acceptItem != null) {
            ItemMeta meta = acceptItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                meta.getPersistentDataContainer().set(requestKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, requestId.toString());
                acceptItem.setItemMeta(meta);
                menu.setItem(12, acceptItem);
            }
        }
        if (rejectItem != null) {
            ItemMeta meta = rejectItem.getItemMeta();
            if (meta != null) {
                org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                meta.getPersistentDataContainer().set(requestKey, 
                    org.bukkit.persistence.PersistentDataType.STRING, requestId.toString());
                rejectItem.setItemMeta(meta);
                menu.setItem(14, rejectItem);
            }
        }
        
        // Geri butonu (Slot 18)
        menu.setItem(18, createButton(Material.ARROW, "§7Geri", 
            Arrays.asList("§7Önceki menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Son Onay Menüsü tıklama (İlk gönderen oyuncu için)
     */
    private void handleFinalApprovalClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Request ID'yi NBT'den al
        UUID requestId = getRequestIdFromItem(clicked);
        if (requestId == null || contractRequestManager == null || contractTermsManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        UUID otherPlayerId = request.getSender().equals(player.getUniqueId()) ? 
            request.getTarget() : request.getSender();
        
        me.mami.stratocraft.model.ContractTerms myTerms = 
            contractTermsManager.getTermsByRequest(requestId, player.getUniqueId());
        me.mami.stratocraft.model.ContractTerms otherTerms = 
            contractTermsManager.getTermsByRequest(requestId, otherPlayerId);
        
        if (clicked.getType() == Material.GREEN_CONCRETE) {
            // Kabul Et
            if (otherTerms != null) {
                // Çift taraflı kontrat
                createBilateralContract(request, myTerms, otherTerms);
                
                player.sendMessage("§6═══════════════════════════════════");
                player.sendMessage("§a§lKONTRAT AKTİF!");
                player.sendMessage("§7Kontratı kabul ettiniz");
                player.sendMessage("§7Kontrat aktif hale geldi");
                player.closeInventory();
            } else {
                // Tek taraflı kontrat
                if (myTerms != null) {
                    Contract contract = new Contract(request.getSender(), request.getTarget(), 
                        request.getId(), myTerms, null);
                    contractManager.getContracts().add(contract);
                    
                    player.sendMessage("§6═══════════════════════════════════");
                    player.sendMessage("§a§lKONTRAT AKTİF!");
                    player.sendMessage("§7Kontratı kabul ettiniz");
                    player.sendMessage("§7Kontrat aktif hale geldi");
                    
                    // HUD'a bildirim gönder
                    sendContractNotification(player, "Kontrat aktif oldu", 
                        me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
                    
                    player.closeInventory();
                    
                    // Karşı tarafa bildirim
                    org.bukkit.entity.Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
                    if (otherPlayer != null && otherPlayer.isOnline()) {
                        otherPlayer.sendMessage("§6═══════════════════════════════════");
                        otherPlayer.sendMessage("§a§lKONTRAT AKTİF!");
                        otherPlayer.sendMessage("§7İlk gönderen oyuncu kontratı onayladı");
                        otherPlayer.sendMessage("§7Kontrat aktif hale geldi");
                        otherPlayer.sendMessage("§6═══════════════════════════════════");
                        
                        // HUD'a bildirim gönder
                        sendContractNotification(otherPlayer, "Kontrat aktif oldu", 
                            me.mami.stratocraft.manager.HUDManager.ContractNotificationType.SUCCESS);
                    }
                }
            }
        } else if (clicked.getType() == Material.RED_CONCRETE) {
            // Reddet
            request.setStatus(me.mami.stratocraft.model.ContractRequest.ContractRequestStatus.CANCELLED);
            
            player.sendMessage("§6═══════════════════════════════════");
            player.sendMessage("§c§lKONTRAT İPTAL EDİLDİ!");
            player.sendMessage("§7Kontratı reddettiniz");
            player.sendMessage("§7Kontrat iptal edildi");
            
            // HUD'a bildirim gönder
            sendContractNotification(player, "Kontrat iptal edildi", 
                me.mami.stratocraft.manager.HUDManager.ContractNotificationType.ERROR);
            
            player.closeInventory();
            
            // Karşı tarafa bildirim
            org.bukkit.entity.Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
            if (otherPlayer != null && otherPlayer.isOnline()) {
                otherPlayer.sendMessage("§6═══════════════════════════════════");
                otherPlayer.sendMessage("§c§lKONTRAT İPTAL EDİLDİ!");
                otherPlayer.sendMessage("§7İlk gönderen oyuncu kontratı reddetti");
                otherPlayer.sendMessage("§7Kontrat iptal edildi");
                otherPlayer.sendMessage("§6═══════════════════════════════════");
                
                // HUD'a bildirim gönder
                sendContractNotification(otherPlayer, "Kontrat iptal edildi", 
                    me.mami.stratocraft.manager.HUDManager.ContractNotificationType.ERROR);
            }
        } else if (clicked.getType() == Material.ARROW) {
            // Geri
            openAcceptedRequestsMenu(player, 1);
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Atılan İstekler menüsü (YENİ)
     */
    private void openSentRequestsMenu(Player player, int page) {
        if (player == null || contractRequestManager == null) return;
        
        List<me.mami.stratocraft.model.ContractRequest> requests = 
            contractRequestManager.getSentPendingRequests(player.getUniqueId());
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(requests.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§6Atılan İstekler - Sayfa " + page);
        
        // İstekleri göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, requests.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            me.mami.stratocraft.model.ContractRequest request = requests.get(i);
            if (request != null) {
                UUID targetId = request.getTarget();
                org.bukkit.OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetId);
                String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : "Bilinmeyen";
                
                List<String> lore = new ArrayList<>();
                lore.add("§7═══════════════════════");
                lore.add("§7Hedef: §e" + targetName);
                lore.add("§7Durum: §eBeklemede");
                lore.add("§7═══════════════════════");
                lore.add("§7Sağ tıkla iptal et");
                
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§e" + targetName + " ile Kontrat İsteği");
                    meta.setLore(lore);
                    org.bukkit.NamespacedKey requestKey = new org.bukkit.NamespacedKey(plugin, "request_id");
                    meta.getPersistentDataContainer().set(requestKey, 
                        org.bukkit.persistence.PersistentDataType.STRING, request.getId().toString());
                    item.setItemMeta(meta);
                }
                menu.setItem(slot++, item);
            }
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", 
                Arrays.asList("§7Sayfa " + (page - 1))));
        }
        if (endIndex < requests.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Atılan İstekler menüsü tıklama (YENİ)
     */
    private void handleSentRequestsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            int slot = event.getSlot();
            String title = event.getView().getTitle();
            int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
            
            if (slot == 45) {
                openSentRequestsMenu(player, currentPage - 1);
            } else if (slot == 53) {
                openSentRequestsMenu(player, currentPage + 1);
            } else if (slot == 49) {
                openMainMenu(player, 1);
            }
            return;
        }
        
        // Request ID'yi NBT'den al
        UUID requestId = getRequestIdFromItem(clicked);
        if (requestId == null || contractRequestManager == null) return;
        
        me.mami.stratocraft.model.ContractRequest request = contractRequestManager.getRequest(requestId);
        if (request == null) return;
        
        // Sağ tık = İptal et
        if (event.getClick() == org.bukkit.event.inventory.ClickType.RIGHT) {
            if (contractRequestManager.cancelRequest(requestId, player.getUniqueId())) {
                player.sendMessage("§cKontrat isteği iptal edildi.");
                openSentRequestsMenu(player, 1);
            } else {
                player.sendMessage("§cİstek iptal edilemedi!");
            }
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Aktif Kontratlar menüsü (YENİ)
     */
    private void openActiveContractsMenu(Player player, int page) {
        if (player == null || contractManager == null) return;
        
        List<Contract> contracts = contractManager.getContracts();
        if (contracts == null) contracts = new ArrayList<>();
        
        // Aktif kontratları filtrele
        List<Contract> activeContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract == null) continue;
            
            // Tek taraflı aktif kontratlar
            if (!contract.isBilateralContract() && 
                (contract.getIssuer().equals(player.getUniqueId()) || 
                 (contract.getAcceptor() != null && contract.getAcceptor().equals(player.getUniqueId()))) &&
                !contract.isExpired() && !contract.isBreached() && !contract.isCompleted()) {
                activeContracts.add(contract);
            }
            // Çift taraflı aktif kontratlar
            else if (contract.isBilateralContract() && 
                (contract.getPlayerA().equals(player.getUniqueId()) || 
                 contract.getPlayerB().equals(player.getUniqueId())) &&
                contract.getContractStatus() == Contract.ContractStatus.ACTIVE) {
                activeContracts.add(contract);
            }
        }
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(activeContracts.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§bAktif Kontratlar - Sayfa " + page);
        
        // Kontratları göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, activeContracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = activeContracts.get(i);
            if (contract != null) {
                ItemStack contractItem = createContractItem(contract);
                ItemMeta meta = contractItem.getItemMeta();
                if (meta != null) {
                    org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "contract_id");
                    meta.getPersistentDataContainer().set(uuidKey, 
                        org.bukkit.persistence.PersistentDataType.STRING, contract.getId().toString());
                    contractItem.setItemMeta(meta);
                }
                menu.setItem(slot++, contractItem);
            }
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", 
                Arrays.asList("§7Sayfa " + (page - 1))));
        }
        if (endIndex < activeContracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Aktif Kontratlar menüsü tıklama (YENİ)
     */
    private void handleActiveContractsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            int slot = event.getSlot();
            String title = event.getView().getTitle();
            int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
            
            if (slot == 45) {
                openActiveContractsMenu(player, currentPage - 1);
            } else if (slot == 53) {
                openActiveContractsMenu(player, currentPage + 1);
            } else if (slot == 49) {
                openMainMenu(player, 1);
            }
            return;
        }
        
        // Contract ID'yi NBT'den al
        UUID contractId = getContractIdFromItem(clicked);
        if (contractId != null) {
            openDetailMenu(player, contractId);
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Eski Kontratlar menüsü (YENİ) - İptal edilenler ve tamamlananlar
     */
    private void openOldContractsMenu(Player player, int page) {
        if (player == null || contractManager == null) return;
        
        List<Contract> contracts = contractManager.getContracts();
        if (contracts == null) contracts = new ArrayList<>();
        
        // Eski kontratları filtrele
        List<Contract> oldContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract == null) continue;
            
            boolean isMyContract = false;
            if (!contract.isBilateralContract()) {
                isMyContract = contract.getIssuer().equals(player.getUniqueId()) || 
                    (contract.getAcceptor() != null && contract.getAcceptor().equals(player.getUniqueId()));
            } else {
                isMyContract = contract.getPlayerA().equals(player.getUniqueId()) || 
                    contract.getPlayerB().equals(player.getUniqueId());
            }
            
            if (isMyContract) {
                // İptal edilenler
                if (contract.isBilateralContract() && 
                    contract.getContractStatus() == Contract.ContractStatus.CANCELLED) {
                    oldContracts.add(contract);
                }
                // Tamamlananlar
                else if (contract.isCompleted() || 
                    (contract.isBilateralContract() && 
                     contract.getContractStatus() == Contract.ContractStatus.COMPLETED)) {
                    oldContracts.add(contract);
                }
                // İhlal edilenler
                else if (contract.isBreached() || 
                    (contract.isBilateralContract() && 
                     contract.getContractStatus() == Contract.ContractStatus.BREACHED)) {
                    oldContracts.add(contract);
                }
            }
        }
        
        // Sayfa kontrolü
        int totalPages = Math.max(1, (int) Math.ceil(oldContracts.size() / 45.0));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        currentPages.put(player.getUniqueId(), page);
        
        Inventory menu = Bukkit.createInventory(null, 54, "§7Eski Kontratlar - Sayfa " + page);
        
        // Kontratları göster (Slot 0-44)
        int startIndex = (page - 1) * 45;
        int endIndex = Math.min(startIndex + 45, oldContracts.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Contract contract = oldContracts.get(i);
            if (contract != null) {
                ItemStack contractItem = createContractItem(contract);
                ItemMeta meta = contractItem.getItemMeta();
                if (meta != null) {
                    org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "contract_id");
                    meta.getPersistentDataContainer().set(uuidKey, 
                        org.bukkit.persistence.PersistentDataType.STRING, contract.getId().toString());
                    contractItem.setItemMeta(meta);
                }
                menu.setItem(slot++, contractItem);
            }
        }
        
        // Sayfalama butonları
        if (page > 1) {
            menu.setItem(45, createButton(Material.ARROW, "§eÖnceki Sayfa", 
                Arrays.asList("§7Sayfa " + (page - 1))));
        }
        if (endIndex < oldContracts.size()) {
            menu.setItem(53, createButton(Material.ARROW, "§eSonraki Sayfa", 
                Arrays.asList("§7Sayfa " + (page + 1))));
        }
        
        // Geri butonu (Slot 49)
        menu.setItem(49, createButton(Material.ARROW, "§eGeri", 
            Arrays.asList("§7Ana menüye dön")));
        
        player.openInventory(menu);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    /**
     * Eski Kontratlar menüsü tıklama (YENİ)
     */
    private void handleOldContractsClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getType() == Material.ARROW) {
            int slot = event.getSlot();
            String title = event.getView().getTitle();
            int currentPage = Integer.parseInt(title.split(" - Sayfa ")[1]);
            
            if (slot == 45) {
                openOldContractsMenu(player, currentPage - 1);
            } else if (slot == 53) {
                openOldContractsMenu(player, currentPage + 1);
            } else if (slot == 49) {
                openMainMenu(player, 1);
            }
            return;
        }
        
        // Contract ID'yi NBT'den al
        UUID contractId = getContractIdFromItem(clicked);
        if (contractId != null) {
            openDetailMenu(player, contractId);
        }
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}
