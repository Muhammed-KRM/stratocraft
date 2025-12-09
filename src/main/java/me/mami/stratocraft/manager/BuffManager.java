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
    
    private me.mami.stratocraft.Main plugin;
    private me.mami.stratocraft.manager.GameBalanceConfig balanceConfig;
    
    public void setPlugin(me.mami.stratocraft.Main plugin) {
        this.plugin = plugin;
    }
    
    public void setBalanceConfig(me.mami.stratocraft.manager.GameBalanceConfig config) {
        this.balanceConfig = config;
    }
    
    private double getConquerorDamageMultiplier() {
        return balanceConfig != null ? balanceConfig.getConquerorBuffDamageMultiplier() : 0.2;
    }
    
    private double getHeroHealthMultiplier() {
        return balanceConfig != null ? balanceConfig.getHeroBuffHealthMultiplier() : 0.15;
    }
    
    private double getHeroDefenseMultiplier() {
        return balanceConfig != null ? balanceConfig.getHeroBuffDefenseMultiplier() : 0.25;
    }
    
    private long getConquerorDuration() {
        if (plugin != null && plugin.getConfigManager() != null) {
            return plugin.getConfigManager().getConquerorBuffDuration();
        }
        return 24 * 60 * 60 * 1000; // Varsayılan 24 saat
    }
    
    private long getHeroDuration() {
        if (plugin != null && plugin.getConfigManager() != null) {
            return plugin.getConfigManager().getHeroBuffDuration();
        }
        return 48 * 60 * 60 * 1000; // Varsayılan 48 saat
    }

    /**
     * Fatih Buff'ı: Savaş kazanan klana verilir
     * - %20 daha fazla hasar
     * - %30 daha hızlı üretim
     */
    public void applyConquerorBuff(Clan winner) {
        conquerorBuffs.put(winner.getId(), System.currentTimeMillis() + getConquerorDuration());
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
        heroBuffs.put(victim.getId(), System.currentTimeMillis() + getHeroDuration());
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
        
        // Hasar artışı (config'den)
        double damageMultiplier = getConquerorDamageMultiplier();
        AttributeModifier damageMod = new AttributeModifier(
            UUID.randomUUID(),
            "conqueror_damage",
            damageMultiplier,
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
        
        // Can artışı (config'den)
        double healthMultiplier = getHeroHealthMultiplier();
        AttributeModifier healthMod = new AttributeModifier(
            UUID.randomUUID(),
            "hero_health",
            healthMultiplier,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
            EquipmentSlot.HAND
        );
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(healthMod);
        double newHealthMultiplier = 1.0 + healthMultiplier;
        p.setHealth(Math.min(p.getHealth() * newHealthMultiplier, p.getMaxHealth()));
        
        // Savunma artışı (config'den)
        double defenseMultiplier = getHeroDefenseMultiplier();
        AttributeModifier armorMod = new AttributeModifier(
            UUID.randomUUID(),
            "hero_armor",
            defenseMultiplier,
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
     * Fatih Buff'ının bitiş zamanını al (HUD için)
     */
    public Long getConquerorBuffEnd(UUID clanId) {
        Long endTime = conquerorBuffs.get(clanId);
        if (endTime == null) return null;
        if (System.currentTimeMillis() > endTime) {
            conquerorBuffs.remove(clanId);
            return null;
        }
        return endTime;
    }
    
    /**
     * Kahraman Buff'ının bitiş zamanını al (HUD için)
     */
    public Long getHeroBuffEnd(UUID clanId) {
        Long endTime = heroBuffs.get(clanId);
        if (endTime == null) return null;
        if (System.currentTimeMillis() > endTime) {
            heroBuffs.remove(clanId);
            return null;
        }
        return endTime;
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

