package me.mami.stratocraft.manager;

import me.mami.stratocraft.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuffManager {
    // Klan ID -> Buff bitiş zamanı (millis)
    private final Map<UUID, Long> conquerorBuffs = new HashMap<>(); // Fatih Buff'ı
    private final Map<UUID, Long> heroBuffs = new HashMap<>(); // Kahraman Buff'ı
    
    private static final long CONQUEROR_DURATION = 24 * 60 * 60 * 1000; // 24 saat
    private static final long HERO_DURATION = 48 * 60 * 60 * 1000; // 48 saat

    /**
     * Fatih Buff'ı: Savaş kazanan klana verilir
     * - %20 daha fazla hasar
     * - %30 daha hızlı üretim
     */
    public void applyConquerorBuff(Clan winner) {
        conquerorBuffs.put(winner.getId(), System.currentTimeMillis() + CONQUEROR_DURATION);
        Bukkit.broadcastMessage("§6§l" + winner.getName() + " klanı Fatih Buff'ı kazandı! 24 saat sürecek.");
        
        // Tüm klan üyelerine buff uygula (sadece online olanlara)
        for (UUID memberId : winner.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null && p.isOnline()) {
                applyConquerorBuffToPlayer(p);
            }
            // Offline oyuncular için buff, giriş yaptıklarında checkBuffsOnJoin ile uygulanacak
        }
    }

    /**
     * Kahraman Buff'ı: Felaket tarafından yıkılan klana verilir
     * - %15 daha fazla can
     * - %25 daha fazla savunma
     */
    public void applyHeroBuff(Clan victim) {
        heroBuffs.put(victim.getId(), System.currentTimeMillis() + HERO_DURATION);
        Bukkit.broadcastMessage("§b§l" + victim.getName() + " klanı Kahraman Buff'ı kazandı! 48 saat sürecek.");
        
        for (UUID memberId : victim.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null && p.isOnline()) {
                applyHeroBuffToPlayer(p);
            }
            // Offline oyuncular için buff, giriş yaptıklarında checkBuffsOnJoin ile uygulanacak
        }
    }

    private void applyConquerorBuffToPlayer(Player p) {
        // Önce eski modifier'ı kaldır (varsa)
        Collection<AttributeModifier> modifiers = 
            p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers();
        for (AttributeModifier mod : new ArrayList<>(modifiers)) {
            if (mod.getName().equals("conqueror_damage")) {
                p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(mod);
            }
        }
        
        // Hasar artışı
        AttributeModifier damageMod = new AttributeModifier(
            UUID.randomUUID(),
            "conqueror_damage",
            0.2,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
            EquipmentSlot.HAND
        );
        p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(damageMod);
    }

    private void applyHeroBuffToPlayer(Player p) {
        // Önce eski modifier'ları kaldır (varsa)
        Collection<AttributeModifier> healthModifiers = 
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers();
        for (AttributeModifier mod : new ArrayList<>(healthModifiers)) {
            if (mod.getName().equals("hero_health")) {
                p.getAttribute(Attribute.GENERIC_MAX_HEALTH).removeModifier(mod);
            }
        }
        
        Collection<AttributeModifier> armorModifiers = 
            p.getAttribute(Attribute.GENERIC_ARMOR).getModifiers();
        for (AttributeModifier mod : new ArrayList<>(armorModifiers)) {
            if (mod.getName().equals("hero_armor")) {
                p.getAttribute(Attribute.GENERIC_ARMOR).removeModifier(mod);
            }
        }
        
        // Can artışı
        AttributeModifier healthMod = new AttributeModifier(
            UUID.randomUUID(),
            "hero_health",
            0.15,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
            EquipmentSlot.HAND
        );
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthMod);
        p.setHealth(Math.min(p.getHealth() * 1.15, p.getMaxHealth()));
        
        // Savunma artışı
        AttributeModifier armorMod = new AttributeModifier(
            UUID.randomUUID(),
            "hero_armor",
            0.25,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
            EquipmentSlot.CHEST
        );
        p.getAttribute(Attribute.GENERIC_ARMOR).addModifier(armorMod);
    }

    public boolean hasConquerorBuff(Clan clan) {
        Long endTime = conquerorBuffs.get(clan.getId());
        if (endTime == null) return false;
        if (System.currentTimeMillis() > endTime) {
            conquerorBuffs.remove(clan.getId());
            return false;
        }
        return true;
    }

    public boolean hasHeroBuff(Clan clan) {
        Long endTime = heroBuffs.get(clan.getId());
        if (endTime == null) return false;
        if (System.currentTimeMillis() > endTime) {
            heroBuffs.remove(clan.getId());
            return false;
        }
        return true;
    }

    /**
     * Oyuncu giriş yaptığında buff'ları kontrol et ve uygula
     */
    public void checkBuffsOnJoin(Player p, Clan clan) {
        if (hasConquerorBuff(clan)) {
            applyConquerorBuffToPlayer(p);
        }
        if (hasHeroBuff(clan)) {
            applyHeroBuffToPlayer(p);
        }
    }
}

