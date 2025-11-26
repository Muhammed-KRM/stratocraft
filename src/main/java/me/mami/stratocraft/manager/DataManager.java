package me.mami.stratocraft.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.mami.stratocraft.Main;
import me.mami.stratocraft.model.Clan;
import me.mami.stratocraft.model.Contract;
import me.mami.stratocraft.model.Shop;
import me.mami.stratocraft.model.Structure;
import me.mami.stratocraft.model.Territory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import me.mami.stratocraft.listener.VirtualStorageListener;

import java.io.*;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

public class DataManager {
    private final Main plugin;
    private final File dataFolder;
    private final Gson gson;
    
    public DataManager(Main plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .create();
        
        // Klasörleri oluştur
        new File(dataFolder, "data").mkdirs();
    }
    
    // ========== KAYDETME METODLARI ==========
    
    public void saveAll(ClanManager clanManager, ContractManager contractManager, 
                       ShopManager shopManager, VirtualStorageListener virtualStorage) {
        try {
            saveClans(clanManager);
            saveContracts(contractManager);
            saveShops(shopManager);
            saveVirtualInventories(virtualStorage);
            plugin.getLogger().info("§aTüm veriler kaydedildi!");
        } catch (Exception e) {
            plugin.getLogger().severe("§cVeri kaydetme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveClans(ClanManager clanManager) throws IOException {
        File file = new File(dataFolder, "data/clans.json");
        List<ClanData> clanDataList = new ArrayList<>();
        
        for (Clan clan : clanManager.getAllClans()) {
            ClanData data = new ClanData();
            data.id = clan.getId().toString();
            data.name = clan.getName();
            data.members = clan.getMembers().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().name()));
            data.bankBalance = clan.getBalance();
            data.storedXP = clan.getStoredXP();
            data.guests = clan.getMembers().keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            
            // Territory
            if (clan.getTerritory() != null) {
                Territory t = clan.getTerritory();
                data.territory = new TerritoryData();
                data.territory.center = serializeLocation(t.getCenter());
                data.territory.radius = t.getRadius();
                data.territory.outposts = t.getOutposts().stream()
                        .map(this::serializeLocation)
                        .collect(Collectors.toList());
            }
            
            // Structures
            data.structures = clan.getStructures().stream()
                    .map(s -> {
                        StructureData sd = new StructureData();
                        sd.type = s.getType().name();
                        sd.location = serializeLocation(s.getLocation());
                        sd.level = s.getLevel();
                        sd.shieldFuel = s.getShieldFuel();
                        return sd;
                    })
                    .collect(Collectors.toList());
            
            clanDataList.add(data);
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(clanDataList, writer);
        }
    }
    
    private void saveContracts(ContractManager contractManager) throws IOException {
        File file = new File(dataFolder, "data/contracts.json");
        List<ContractData> contractDataList = new ArrayList<>();
        
        for (Contract contract : contractManager.getContracts()) {
            ContractData data = new ContractData();
            data.id = contract.getId().toString();
            data.issuer = contract.getIssuer().toString();
            data.acceptor = contract.getAcceptor() != null ? contract.getAcceptor().toString() : null;
            data.material = contract.getMaterial().name();
            data.amount = contract.getAmount();
            data.reward = contract.getReward();
            data.delivered = contract.getDelivered();
            data.deadline = contract.getDeadline();
            contractDataList.add(data);
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(contractDataList, writer);
        }
    }
    
    private void saveShops(ShopManager shopManager) throws IOException {
        File file = new File(dataFolder, "data/shops.json");
        List<ShopData> shopDataList = shopManager.getAllShops().stream()
                .map(shop -> {
                    ShopData data = new ShopData();
                    data.id = UUID.randomUUID().toString(); // Shop ID yoksa random
                    data.owner = shop.getOwnerId().toString();
                    data.location = serializeLocation(shop.getLocation());
                    data.sellItem = serializeItemStack(shop.getSellingItem());
                    data.priceItem = serializeItemStack(shop.getPriceItem());
                    data.protectedZone = shop.isProtectedZone();
                    return data;
                })
                .collect(Collectors.toList());
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(shopDataList, writer);
        }
    }
    
    private void saveVirtualInventories(VirtualStorageListener virtualStorage) throws IOException {
        File file = new File(dataFolder, "data/virtual_inventories.json");
        Map<String, String> inventoryMap = new HashMap<>();
        
        for (Map.Entry<UUID, Inventory> entry : virtualStorage.getVirtualInventories().entrySet()) {
            String base64 = serializeInventory(entry.getValue());
            inventoryMap.put(entry.getKey().toString(), base64);
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(inventoryMap, writer);
        }
    }
    
    // ========== YÜKLEME METODLARI ==========
    
    public void loadAll(ClanManager clanManager, ContractManager contractManager,
                       ShopManager shopManager, VirtualStorageListener virtualStorage) {
        try {
            loadClans(clanManager);
            loadContracts(contractManager);
            loadShops(shopManager);
            loadVirtualInventories(virtualStorage);
            plugin.getLogger().info("§aTüm veriler yüklendi!");
        } catch (Exception e) {
            plugin.getLogger().severe("§cVeri yükleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadClans(ClanManager clanManager) throws IOException {
        File file = new File(dataFolder, "data/clans.json");
        if (!file.exists()) return;
        
        try (FileReader reader = new FileReader(file)) {
            List<ClanData> clanDataList = gson.fromJson(reader, 
                    new TypeToken<List<ClanData>>(){}.getType());
            
            if (clanDataList == null) return;
            
            for (ClanData data : clanDataList) {
                UUID clanId = UUID.fromString(data.id);
                UUID leaderId = data.members.entrySet().stream()
                        .filter(e -> e.getValue().equals("LEADER"))
                        .map(e -> UUID.fromString(e.getKey()))
                        .findFirst().orElse(null);
                
                if (leaderId == null) continue;
                
                // Clan oluştur ve ID'yi set et
                Clan clan = new Clan(data.name, leaderId);
                clan.setId(clanId);
                
                // Members
                for (Map.Entry<String, String> entry : data.members.entrySet()) {
                    UUID memberId = UUID.fromString(entry.getKey());
                    Clan.Rank rank = Clan.Rank.valueOf(entry.getValue());
                    if (!memberId.equals(leaderId)) {
                        clan.addMember(memberId, rank);
                    }
                }
                
                // Territory
                if (data.territory != null) {
                    Territory territory = new Territory(clanId, deserializeLocation(data.territory.center));
                    territory.expand(data.territory.radius - 50);
                    for (String outpostStr : data.territory.outposts) {
                        territory.addOutpost(deserializeLocation(outpostStr));
                    }
                    clan.setTerritory(territory);
                }
                
                // Structures
                for (StructureData sd : data.structures) {
                    Structure structure = new Structure(
                            Structure.Type.valueOf(sd.type),
                            deserializeLocation(sd.location),
                            sd.level
                    );
                    for (int i = 0; i < sd.shieldFuel; i++) {
                        structure.addFuel(1);
                    }
                    clan.addStructure(structure);
                }
                
                // Bank balance ve XP
                clan.deposit(data.bankBalance);
                clan.setStoredXP(data.storedXP);
                
                // Guests
                for (String guestId : data.guests) {
                    clan.addGuest(UUID.fromString(guestId));
                }
                
                // ClanManager'a ekle
                clanManager.loadClan(clan);
            }
        }
    }
    
    private void loadContracts(ContractManager contractManager) throws IOException {
        File file = new File(dataFolder, "data/contracts.json");
        if (!file.exists()) return;
        
        try (FileReader reader = new FileReader(file)) {
            List<ContractData> contractDataList = gson.fromJson(reader,
                    new TypeToken<List<ContractData>>(){}.getType());
            
            if (contractDataList == null) return;
            
            for (ContractData data : contractDataList) {
                Contract contract = new Contract(
                        UUID.fromString(data.issuer),
                        org.bukkit.Material.valueOf(data.material),
                        data.amount,
                        data.reward,
                        (data.deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                );
                
                // ID ve diğer alanları set et
                contract.setId(UUID.fromString(data.id));
                contract.setDelivered(data.delivered);
                if (data.acceptor != null) {
                    contract.setAcceptor(UUID.fromString(data.acceptor));
                }
                
                contractManager.loadContract(contract);
            }
        }
    }
    
    private void loadShops(ShopManager shopManager) throws IOException {
        File file = new File(dataFolder, "data/shops.json");
        if (!file.exists()) return;
        
        try (FileReader reader = new FileReader(file)) {
            List<ShopData> shopDataList = gson.fromJson(reader,
                    new TypeToken<List<ShopData>>(){}.getType());
            
            if (shopDataList == null) return;
            
            for (ShopData data : shopDataList) {
                Shop shop = new Shop(
                        UUID.fromString(data.owner),
                        deserializeLocation(data.location),
                        deserializeItemStack(data.sellItem),
                        deserializeItemStack(data.priceItem),
                        data.protectedZone
                );
                shopManager.loadShop(shop);
            }
        }
    }
    
    private void loadVirtualInventories(VirtualStorageListener virtualStorage) throws IOException {
        File file = new File(dataFolder, "data/virtual_inventories.json");
        if (!file.exists()) return;
        
        try (FileReader reader = new FileReader(file)) {
            Map<String, String> inventoryMap = gson.fromJson(reader,
                    new TypeToken<Map<String, String>>(){}.getType());
            
            if (inventoryMap == null) return;
            
            for (Map.Entry<String, String> entry : inventoryMap.entrySet()) {
                UUID clanId = UUID.fromString(entry.getKey());
                Inventory inv = deserializeInventory(entry.getValue());
                virtualStorage.setVirtualInventory(clanId, inv);
            }
        }
    }
    
    // ========== YARDIMCI METODLAR ==========
    
    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + 
               ";" + loc.getYaw() + ";" + loc.getPitch();
    }
    
    private Location deserializeLocation(String str) {
        String[] parts = str.split(";");
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
    }
    
    private String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
    
    private ItemStack deserializeItemStack(String str) {
        if (str == null) return null;
        try {
            byte[] data = Base64.getDecoder().decode(str);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String serializeInventory(Inventory inv) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(inv.getSize());
            for (int i = 0; i < inv.getSize(); i++) {
                dataOutput.writeObject(inv.getItem(i));
            }
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
    
    private Inventory deserializeInventory(String str) {
        try {
            byte[] data = Base64.getDecoder().decode(str);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();
            Inventory inv = Bukkit.createInventory(null, size, "§5Sanal Bağlantı - Şubeler Arası Depo");
            for (int i = 0; i < size; i++) {
                inv.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return inv;
        } catch (Exception e) {
            return Bukkit.createInventory(null, 54, "§5Sanal Bağlantı - Şubeler Arası Depo");
        }
    }
    
    // ========== DATA CLASSES ==========
    
    private static class ClanData {
        String id;
        String name;
        Map<String, String> members;
        double bankBalance;
        int storedXP;
        List<String> guests;
        TerritoryData territory;
        List<StructureData> structures;
    }
    
    private static class TerritoryData {
        String center;
        int radius;
        List<String> outposts;
    }
    
    private static class StructureData {
        String type;
        String location;
        int level;
        int shieldFuel;
    }
    
    private static class ContractData {
        String id;
        String issuer;
        String acceptor;
        String material;
        int amount;
        double reward;
        int delivered;
        long deadline;
    }
    
    private static class ShopData {
        @SuppressWarnings("unused")
        String id;
        String owner;
        String location;
        String sellItem;
        String priceItem;
        boolean protectedZone;
    }
    
    // Location adapter for Gson
    private static class LocationAdapter implements com.google.gson.JsonSerializer<Location>, 
            com.google.gson.JsonDeserializer<Location> {
        @Override
        public com.google.gson.JsonElement serialize(Location src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("world", src.getWorld().getName());
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            obj.addProperty("z", src.getZ());
            obj.addProperty("yaw", src.getYaw());
            obj.addProperty("pitch", src.getPitch());
            return obj;
        }
        
        @Override
        public Location deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) {
            com.google.gson.JsonObject obj = json.getAsJsonObject();
            return new Location(
                    Bukkit.getWorld(obj.get("world").getAsString()),
                    obj.get("x").getAsDouble(),
                    obj.get("y").getAsDouble(),
                    obj.get("z").getAsDouble(),
                    obj.get("yaw").getAsFloat(),
                    obj.get("pitch").getAsFloat()
            );
        }
    }
}
