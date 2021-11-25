package me.ichun.mods.morph.api.mob.trait;

public class FloatTrait extends Trait<FloatTrait>
{
    public FloatTrait()
    {
        type = "traitFloat";
    }

    @Override
    public void tick(float strength)
    {
        if((player.isInWater() || player.isInLava()) && !player.abilities.isFlying)
        {
            player.setMotion(player.getMotion().add(0D, 0.07D * strength, 0D));
        }
    }

    @Override
    public FloatTrait copy()
    {
        return new FloatTrait();
    }
}
