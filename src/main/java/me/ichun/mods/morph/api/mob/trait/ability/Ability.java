package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.mob.trait.Trait;

public abstract class Ability<T extends Ability> extends Trait<T>
{
    public Double useCost;

    @Override
    public void doTick(float strength)
    {
        if(MorphApi.getApiImpl().canUseAbility(player, this))
        {
            this.tick(strength);
        }
    }

    @Override
    public void doTransitionalTick(T prevTrait, float transitionProgress)
    {
        if(MorphApi.getApiImpl().canUseAbility(player, this))
        {
            this.transitionalTick(prevTrait, transitionProgress);
        }
    }

    public boolean isActive()//As in, is the ability an actively used ability rather than something passive that happens all the time.
    {
        return false;
    }

    @Override
    public boolean isAbility()
    {
        return true;
    }
}
