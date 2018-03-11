package me.ichun.mods.morph.api.ability.type;

import me.ichun.mods.morph.api.ability.Ability;

public class AbilityFloat extends Ability
{
    public Double floatMotion;

    public AbilityFloat()
    {
        type = "float";
    }

    @Override
    public void tick()
    {
        double floatm = floatMotion != null ? floatMotion : -0.114D;
        if(getParent().motionY < floatm)
        {
            getParent().motionY -= getParent().motionY * getStrength();
            getParent().motionY += floatm * getStrength();
        }
        getParent().fallDistance -= getParent().fallDistance * getStrength();
    }
}
