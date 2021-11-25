package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.util.DamageSource;

public class MoistSkinTrait extends Trait<MoistSkinTrait>
{
    public Integer maxMoistness;

    public transient int moistness = -100;

    public MoistSkinTrait()
    {
        type = "traitMoistSkin";
    }

    @Override
    public void addHooks()
    {
        if(maxMoistness == null)
        {
            maxMoistness = 2400;
        }
    }

    @Override
    public void tick(float strength)
    {
        if(moistness == -100)
        {
            moistness = maxMoistness;
        }

        if (player.isInWaterRainOrBubbleColumn())
        {
            moistness = maxMoistness;
        }
        else
        {
            moistness--;
            if(moistness <= 0)
            {
                player.attackEntityFrom(DamageSource.DRYOUT, 1.0F);
            }
        }
    }

    @Override
    public MoistSkinTrait copy()
    {
        return new MoistSkinTrait();
    }
}
