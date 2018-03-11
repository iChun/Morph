package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;

public class AbilityClimb extends Ability
{
    public AbilityClimb()
    {
        type = "climb";
    }

    @Override
    public void tick()
    {
        if(getParent().collidedHorizontally)
        {
            getParent().motionY -= getParent().motionY * getStrength();
            if(getParent().isSneaking())
            {
                getParent().motionY += 0D * getStrength();
            }
            else
            {
                getParent().motionY += 0.2D * getStrength();
            }
            getParent().fallDistance -= getParent().fallDistance * getStrength();
        }
        //TODO test fall distance negation
    }
}
