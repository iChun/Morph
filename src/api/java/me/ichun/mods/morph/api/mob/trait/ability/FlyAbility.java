package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.tags.FluidTags;

public class FlyAbility extends Ability<FlyAbility>
{
    public Boolean slowdownInWater;

    public transient float lastStrength = 0F;

    public FlyAbility()
    {
        type = "abilityFlight";
    }

    @Override
    public void tick(float strength)
    {
        if(strength == 1F)
        {
            if(lastStrength != 1F)
            {
                if(!player.abilities.allowFlying)
                {
                    player.abilities.allowFlying = true;

                    player.sendPlayerAbilities();
                }
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
        else if(lastStrength == 1F) //strength != 1F, but lastStrength == 1F. We're morphing out, disable flight.
        {
            if(canPlayerFly() && player.abilities.allowFlying)
            {
                player.abilities.allowFlying = false;
                player.abilities.isFlying = false;

                player.sendPlayerAbilities();
            }
        }

        lastStrength = strength;
    }

    @Override
    public void transitionalTick(FlyAbility prevTrait, float transitionProgress)
    {
        lastStrength = 1F;
        super.transitionalTick(prevTrait, transitionProgress);
    }

    @Override
    public FlyAbility copy()
    {
        FlyAbility ability = new FlyAbility();
        ability.slowdownInWater = this.slowdownInWater;
        return ability;
    }

    public boolean canPlayerFly()
    {
        return player.isSpectator() || player.isCreative();
    }
}
