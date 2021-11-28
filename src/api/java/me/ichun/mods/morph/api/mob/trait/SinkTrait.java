package me.ichun.mods.morph.api.mob.trait;

import net.minecraft.util.math.vector.Vector3d;

public class SinkTrait extends Trait<SinkTrait>
{
    public transient boolean isInWater;

    public SinkTrait()
    {
        type = "traitSink";
    }

    @Override
    public void tick(float strength)
    {
        if(player.isInWater())
        {
            Vector3d motion = player.getMotion();
            if(player.collidedHorizontally)
            {
                player.setMotion(motion.x, 0.07D * strength, motion.z);
            }
            else if(motion.y > -0.07D && !player.abilities.isFlying)
            {
                player.setMotion(motion.add(0D, -0.07D * strength, 0D));
            }
        }
        else if(isInWater && !player.abilities.isFlying)
        {
            player.setMotion(player.getMotion().add(0D, 0.32D * strength, 0D));
        }
        isInWater = player.isInWater();
    }

    @Override
    public SinkTrait copy()
    {
        return new SinkTrait();
    }
}
