package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.util.DamageSource;

public class WaterSensitivityTrait extends Trait<WaterSensitivityTrait>
{
    public WaterSensitivityTrait()
    {
        type = "traitWaterSensitivity";
    }

    @Override
    public void tick(float strength)
    {
        if(!player.world.isRemote && strength == 1F && player.isInWaterRainOrBubbleColumn())
        {
            player.attackEntityFrom(DamageSource.DROWN, strength);
        }
    }

    @Override
    public WaterSensitivityTrait copy()
    {
        return new WaterSensitivityTrait();
    }
}
