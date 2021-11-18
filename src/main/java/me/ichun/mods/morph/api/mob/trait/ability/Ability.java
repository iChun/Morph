package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.mob.trait.Trait;

public abstract class Ability extends Trait
{
    public Double purchaseCost;
    public Double useCost;

    public boolean isActive()
    {
        return false;
    }

    @Override
    public boolean isAbility()
    {
        return true;
    }
}
