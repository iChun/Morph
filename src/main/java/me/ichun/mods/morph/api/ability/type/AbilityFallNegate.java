package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;

public class AbilityFallNegate extends Ability
{
    public AbilityFallNegate()
    {
        type = "fallNegate";
    }

    @Override
    public void tick()
    {
        getParent().fallDistance -= getParent().fallDistance * getStrength();
    }
}
