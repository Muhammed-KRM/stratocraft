package me.mami.stratocraft.model;

import org.bukkit.Material;
import java.util.UUID;

public class Contract {
    private UUID id = UUID.randomUUID();
    private final UUID issuer;
    private final Material material;
    private final int amount;
    private final double reward;
    private final long deadline; // Süre (milisaniye)
    private UUID acceptor = null;
    private int delivered = 0;
    private boolean completed = false;

    public Contract(UUID issuer, Material material, int amount, double reward, long deadlineDays) {
        this.issuer = issuer;
        this.material = material;
        this.amount = amount;
        this.reward = reward;
        this.deadline = System.currentTimeMillis() + (deadlineDays * 24 * 60 * 60 * 1000);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; } // DataManager için
    public UUID getIssuer() { return issuer; }
    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public double getReward() { return reward; }
    public long getDeadline() { return deadline; }
    public UUID getAcceptor() { return acceptor; }
    public void setAcceptor(UUID acceptor) { this.acceptor = acceptor; }
    public int getDelivered() { return delivered; }
    public void setDelivered(int delivered) { this.delivered = delivered; } // DataManager için
    public void addDelivered(int amount) { this.delivered += amount; }
    public boolean isCompleted() { return completed || delivered >= amount; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean isExpired() { return System.currentTimeMillis() > deadline && !isCompleted(); }
}

