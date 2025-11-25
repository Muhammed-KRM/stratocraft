package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Mission;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class MissionManager {
    private final Map<UUID, Mission> activeMissions = new HashMap<>();
    private final Random random = new Random();

    public void interactWithTotem(Player p, Material totemMaterial) {
        if (activeMissions.containsKey(p.getUniqueId())) {
            Mission mission = activeMissions.get(p.getUniqueId());
            if (mission.isCompleted()) {
                p.getInventory().addItem(mission.getReward());
                p.sendMessage("§a[LONCA] Görev Tamamlandı! Ödülünü aldın.");
                activeMissions.remove(p.getUniqueId());
            } else {
                p.sendMessage("§e[LONCA] §7Durum: " + mission.getProgress() + "/" + mission.getTargetAmount());
            }
        } else {
            assignNewMission(p, totemMaterial);
        }
    }

    private void assignNewMission(Player p, Material tier) {
        Mission mission;
        // Totem seviye sistemi
        if (tier == Material.COBBLESTONE || tier == Material.STONE) {
            // Taş Totem - Basit görevler
            if (random.nextBoolean()) {
                mission = new Mission(p.getUniqueId(), Mission.Type.KILL_MOB, EntityType.ZOMBIE, 10, new ItemStack(Material.IRON_INGOT, 5));
                p.sendMessage("§e[LONCA] §7Yeni Görev: 10 Zombi Öldür.");
            } else {
                mission = new Mission(p.getUniqueId(), Mission.Type.GATHER_ITEM, Material.OAK_LOG, 64, new ItemStack(Material.GOLD_INGOT, 3));
                p.sendMessage("§e[LONCA] §7Yeni Görev: 64 Odun Topla.");
            }
        } else if (tier == Material.DIAMOND_BLOCK || tier == Material.DIAMOND) {
            // Elmas Totem - Zor görevler
            if (random.nextBoolean()) {
                ItemStack reward = random.nextBoolean() ? ItemManager.RECIPE_BOOK_TECTONIC : ItemManager.DEVIL_HORN;
                mission = new Mission(p.getUniqueId(), Mission.Type.KILL_MOB, EntityType.ENDERMAN, 20, reward);
                p.sendMessage("§6[LONCA] §cZorlu Görev: 20 Enderman Avla.");
            } else {
                ItemStack reward = ItemManager.TITANIUM_INGOT != null ? ItemManager.TITANIUM_INGOT : new ItemStack(Material.DIAMOND, 5);
                mission = new Mission(p.getUniqueId(), Mission.Type.GATHER_ITEM, Material.DEEPSLATE_DIAMOND_ORE, 10, reward);
                p.sendMessage("§6[LONCA] §cZorlu Görev: 10 Derin Elmas Madeni Topla.");
            }
        } else {
            // Varsayılan
            mission = new Mission(p.getUniqueId(), Mission.Type.KILL_MOB, EntityType.ZOMBIE, 10, new ItemStack(Material.IRON_INGOT, 5));
            p.sendMessage("§e[LONCA] §7Yeni Görev: 10 Zombi Öldür.");
        }
        activeMissions.put(p.getUniqueId(), mission);
    }
    
    public void handleKill(Player p, EntityType type) {
        if (activeMissions.containsKey(p.getUniqueId())) {
            Mission m = activeMissions.get(p.getUniqueId());
            if (m.getType() == Mission.Type.KILL_MOB && m.getTargetEntity() == type) {
                m.addProgress(1);
                if (m.isCompleted()) p.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
            }
        }
    }
    
    public void handleGather(Player p, Material material) {
        if (activeMissions.containsKey(p.getUniqueId())) {
            Mission m = activeMissions.get(p.getUniqueId());
            if (m.getType() == Mission.Type.GATHER_ITEM && m.getTargetMaterial() == material) {
                m.addProgress(1);
                if (m.isCompleted()) p.sendMessage("§aGörev hedefine ulaşıldı! Toteme dön.");
            }
        }
    }
}

