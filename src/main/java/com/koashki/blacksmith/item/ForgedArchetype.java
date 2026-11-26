package com.koashki.blacksmith.item;

import java.util.List;

public enum ForgedArchetype {
    SWORD("sword", "Sword", 6.0f, -2.4f, 1.0f, List.of(StrikeKind.HEAVY, StrikeKind.LIGHT, StrikeKind.HEAVY, StrikeKind.LIGHT, StrikeKind.QUENCH)),
    GREATSWORD("greatsword", "Greatsword", 8.5f, -3.0f, 1.25f, List.of(StrikeKind.HEAVY, StrikeKind.HEAVY, StrikeKind.STABILIZE, StrikeKind.HEAVY, StrikeKind.HEAVY, StrikeKind.QUENCH)),
    DAGGER("dagger", "Dagger", 4.5f, -1.6f, 0.85f, List.of(StrikeKind.LIGHT, StrikeKind.LIGHT, StrikeKind.HEAVY, StrikeKind.LIGHT, StrikeKind.QUENCH)),
    SPEAR("spear", "Spear", 6.5f, -2.5f, 1.10f, List.of(StrikeKind.LIGHT, StrikeKind.HEAVY, StrikeKind.STABILIZE, StrikeKind.HEAVY, StrikeKind.QUENCH)),
    WAR_HAMMER("war_hammer", "War Hammer", 9.0f, -3.1f, 1.30f, List.of(StrikeKind.HEAVY, StrikeKind.HEAVY, StrikeKind.HEAVY, StrikeKind.STABILIZE, StrikeKind.QUENCH));

    public enum StrikeKind {
        LIGHT,
        HEAVY,
        STABILIZE,
        QUENCH
    }

    private final String id;
    private final String displayName;
    private final float baseDamage;
    private final float attackSpeed;
    private final float durabilityMultiplier;
    private final List<StrikeKind> cadence;

    ForgedArchetype(String id, String displayName, float baseDamage, float attackSpeed, float durabilityMultiplier, List<StrikeKind> cadence) {
        this.id = id;
        this.displayName = displayName;
        this.baseDamage = baseDamage;
        this.attackSpeed = attackSpeed;
        this.durabilityMultiplier = durabilityMultiplier;
        this.cadence = cadence;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public float baseDamage() {
        return baseDamage;
    }

    public float attackSpeed() {
        return attackSpeed;
    }

    public float durabilityMultiplier() {
        return durabilityMultiplier;
    }

    public List<StrikeKind> cadence() {
        return cadence;
    }

    public static ForgedArchetype byId(String id) {
        for (ForgedArchetype value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        return SWORD;
    }
}
