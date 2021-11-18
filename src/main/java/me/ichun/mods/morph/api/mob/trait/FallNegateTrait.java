package me.ichun.mods.morph.api.mob.trait;

public class FallNegateTrait extends Trait
{
    public FallNegateTrait()
    {
        type = "traitFallNegate";
    }

    @Override
    public void tick(float strength)
    {
        player.fallDistance -= player.fallDistance * strength;
    }

    @Override
    public FallNegateTrait copy()
    {
        return new FallNegateTrait();
    }
}
