package me.mami.stratocraft.model;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Mission {
    public enum Type { KILL_MOB, GATHER_ITEM }

    private final UUID playerId;
    private final Type type;
    private final EntityType targetEntity;
    private final Material targetMaterial;
    private final int targetAmount;
    private final ItemStack reward;
    private int progress = 0;

    public Mission(UUID playerId, Type type, EntityType target, int amount, ItemStack reward) {
        this.playerId = playerId;
        this.type = type;
        this.targetEntity = target;
        this.targetMaterial = null;
        this.targetAmount = amount;
        this.reward = reward;
    }
    
    public Mission(UUID playerId, Type type, Material target, int amount, ItemStack reward) {
        this.playerId = playerId;
        this.type = type;
        this.targetEntity = null;
        this.targetMaterial = target;
        this.targetAmount = amount;
        this.reward = reward;
    }

    public void addProgress(int amount) { this.progress += amount; }
    public boolean isCompleted() { return progress >= targetAmount; }
    public Type getType() { return type; }
    public EntityType getTargetEntity() { return targetEntity; }
    public Material getTargetMaterial() { return targetMaterial; }
    public ItemStack getReward() { return reward; }
    public int getProgress() { return progress; }
    public int getTargetAmount() { return targetAmount; }
}

