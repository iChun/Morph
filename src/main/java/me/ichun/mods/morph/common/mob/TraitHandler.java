package me.ichun.mods.morph.common.mob;

import me.ichun.mods.morph.api.mob.trait.FallNegateTrait;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.api.mob.trait.ability.SlowFallAbility;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.util.Util;

import java.util.HashMap;

public class TraitHandler
{
    private static final HashMap<String, Class<? extends Trait>> TRAITS = Util.make(new HashMap<>(), m -> {
        //Register the default traits
        m.put("traitFallNegate", FallNegateTrait.class);

        //Register the default abilities
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
