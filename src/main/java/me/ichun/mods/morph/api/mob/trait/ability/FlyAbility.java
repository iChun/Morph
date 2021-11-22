package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.tags.FluidTags;

public class FlyAbility extends Ability<FlyAbility>
{
    public Boolean slowdownInWater;

    public transient boolean doNotDisableOnHookRemoval = false;

    public FlyAbility()
    {
        type = "abilityFlight";
    }

    @Override
    public void removeHooks()
    {
        if(player.abilities.allowFlying && !doNotDisableOnHookRemoval)
        {
            player.abilities.allowFlying = false;
            player.abilities.isFlying = false;

            player.sendPlayerAbilities();
        }
    }

    @Override
    public void tick(float strength)
    {
        if(!player.abilities.allowFlying)
        {
            player.abilities.allowFlying = true;

            player.sendPlayerAbilities();
        }

        if(slowdownInWater != null && slowdownInWater && player.abilities.isFlying && !player.abilities.isCreativeMode && player.areEyesInFluid(FluidTags.WATER))
        {
            boolean hasSwim = false;

            for(Trait<?> trait : stateTraits)
            {
                if("traitSwim".equals(trait.type))
                {
                    hasSwim = true;
                    break;
                }
            }

            if(!hasSwim)
            {
                player.setMotion(player.getMotion().mul(1D + (0.65D - 1D) * strength, 1D + (0.2D - 1D) * strength, 1D + (0.65D - 1D) * strength));
            }
        }

        player.fallDistance -= player.fallDistance * strength;
    }

    @Override
    public void transitionalTick(FlyAbility prevTrait, float transitionProgress)
    {
        super.transitionalTick(prevTrait, transitionProgress);
        prevTrait.doNotDisableOnHookRemoval = true;
    }

    @Override
    public FlyAbility copy()
    {
        FlyAbility ability = new FlyAbility();
        ability.slowdownInWater = this.slowdownInWater;
        return ability;
    }
}
