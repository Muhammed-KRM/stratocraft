package me.mami.stratocraft.model;

import java.util.*;

public class Clan {
    public enum Rank {
        LEADER(4), GENERAL(3), MEMBER(2), RECRUIT(1);
        private final int level;

        Rank(int level) {
            this.level = level;
        }

        public boolean isAtLeast(Rank other) {
            return this.level >= other.level;
        }
    }

    private final UUID id = UUID.randomUUID();
    private final String name;
    private final Map<UUID, Rank> members = new HashMap<>();
    private final List<Structure> structures = new ArrayList<>();
    private final Set<UUID> guests = new HashSet<>();
    private Territory territory;
    private double bankBalance = 0;

    public Clan(String name, UUID leader) {
        this.name = name;
        this.members.put(leader, Rank.LEADER);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Map<UUID, Rank> getMembers() { return members; }
    public void addMember(UUID uuid, Rank rank) { members.put(uuid, rank); }
    public Rank getRank(UUID uuid) { return members.get(uuid); }

    public List<Structure> getStructures() { return structures; }
    public void addStructure(Structure s) { structures.add(s); }

    public Territory getTerritory() { return territory; }
    public void setTerritory(Territory t) { this.territory = t; }

    public void deposit(double amount) { bankBalance += amount; }
    public void withdraw(double amount) { bankBalance -= amount; }
    public double getBalance() { return bankBalance; }

    public void addGuest(UUID uuid) { guests.add(uuid); }
    public boolean isGuest(UUID uuid) { return guests.contains(uuid); }

    public int getTechLevel() {
        return structures.stream().mapToInt(Structure::getLevel).max().orElse(1);
    }
}

