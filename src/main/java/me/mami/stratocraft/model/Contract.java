package me.mami.stratocraft.model;

import org.bukkit.Material;
import java.util.UUID;

public class Contract {
    private final UUID id = UUID.randomUUID();
    private final UUID issuer;
    private final Material material;
    private final int amount;
    private final double reward;

    public Contract(UUID issuer, Material material, int amount, double reward) {
        this.issuer = issuer;
        this.material = material;
        this.amount = amount;
        this.reward = reward;
    }

    public UUID getId() { return id; }
    public UUID getIssuer() { return issuer; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public double getReward() { return reward; }
}

