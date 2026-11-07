package dev.chenjl.detailedench.compat;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;

/** 1.21.1 override: attribute constants are already RegistryEntry and some names differ. */
public final class Attrs {
    public static final RegistryEntry<EntityAttribute> BURNING_TIME = EntityAttributes.GENERIC_BURNING_TIME;
    public static final RegistryEntry<EntityAttribute> EXPLOSION_KNOCKBACK_RESISTANCE =
            EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE;
    public static final RegistryEntry<EntityAttribute> MINING_EFFICIENCY = EntityAttributes.PLAYER_MINING_EFFICIENCY;
    public static final RegistryEntry<EntityAttribute> WATER_MOVEMENT_EFFICIENCY =
            EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY;
    public static final RegistryEntry<EntityAttribute> OXYGEN_BONUS = EntityAttributes.GENERIC_OXYGEN_BONUS;
    public static final RegistryEntry<EntityAttribute> SNEAKING_SPEED = EntityAttributes.PLAYER_SNEAKING_SPEED;
    public static final RegistryEntry<EntityAttribute> SWEEPING_DAMAGE_RATIO =
            EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO;
    public static final RegistryEntry<EntityAttribute> MOVEMENT_SPEED = EntityAttributes.GENERIC_MOVEMENT_SPEED;

    private Attrs() {
    }
}
