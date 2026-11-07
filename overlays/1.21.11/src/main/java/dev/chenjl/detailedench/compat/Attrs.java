package dev.chenjl.detailedench.compat;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;

/** 1.21.11 override: attribute constants are RegistryEntry values. */
public final class Attrs {
    public static final RegistryEntry<EntityAttribute> BURNING_TIME = EntityAttributes.BURNING_TIME;
    public static final RegistryEntry<EntityAttribute> EXPLOSION_KNOCKBACK_RESISTANCE =
            EntityAttributes.EXPLOSION_KNOCKBACK_RESISTANCE;
    public static final RegistryEntry<EntityAttribute> MINING_EFFICIENCY = EntityAttributes.MINING_EFFICIENCY;
    public static final RegistryEntry<EntityAttribute> WATER_MOVEMENT_EFFICIENCY =
            EntityAttributes.WATER_MOVEMENT_EFFICIENCY;
    public static final RegistryEntry<EntityAttribute> OXYGEN_BONUS = EntityAttributes.OXYGEN_BONUS;
    public static final RegistryEntry<EntityAttribute> SNEAKING_SPEED = EntityAttributes.SNEAKING_SPEED;
    public static final RegistryEntry<EntityAttribute> SWEEPING_DAMAGE_RATIO = EntityAttributes.SWEEPING_DAMAGE_RATIO;
    public static final RegistryEntry<EntityAttribute> MOVEMENT_SPEED = EntityAttributes.MOVEMENT_SPEED;

    private Attrs() {
    }
}
