package me.ichun.mods.morph.common.mob;

import me.ichun.mods.morph.api.mob.trait.*;
import me.ichun.mods.morph.api.mob.trait.ability.*;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.util.Util;

import java.util.HashMap;

public class TraitHandler
{
    private static final HashMap<String, Class<? extends Trait>> TRAITS = Util.make(new HashMap<>(), m -> {
        //Register the default traits
        m.put("traitEffectResistance", EffectResistanceTrait.class);
        m.put("traitFallNegate", FallNegateTrait.class);
        m.put("traitFloat", FloatTrait.class);
        m.put("traitHostile", HostileTrait.class);
        m.put("traitIntimidate", IntimidateTrait.class);
        m.put("traitSink", SinkTrait.class);
        m.put("traitStepHeight", StepHeightTrait.class);
        m.put("traitSunburn", SunburnTrait.class);
        m.put("traitWaterBreather", WaterBreatherTrait.class);
        m.put("traitWaterSensitivity", WaterSensitivityTrait.class);
        //TODO float - slimes

        //Damage immunity traits
        m.put("traitImmunityExplosive", ExplosiveImmunityTrait.class);
        m.put("traitImmunityFire", FireImmunityTrait.class);
        m.put("traitImmunityMagic", MagicImmunityTrait.class);
        m.put("traitImmunityDamageSource", DamageSourceImmunityTrait.class);

        //Register the default abilities
        m.put("abilityClimb", ClimbAbility.class);
        m.put("abilityEffectAttack", EffectAttackAbility.class);
        m.put("abilityFlight", FlyAbility.class);
        m.put("abilityFlightFlap", FlightFlapAbility.class);
        m.put("abilitySlowFall", SlowFallAbility.class);
    });

    public static void registerTrait(String type, Class<? extends Trait> clz)
    {
        if(TRAITS.containsKey(type))
        {
            Morph.LOGGER.warn("We already have another Trait of type {} with class {}. This is a mod-level override so we shall acknowledge it. Overriding with class {}", type, TRAITS.get(type).getName(), clz.getName());
        }

        TRAITS.put(type, clz);
    }
}
