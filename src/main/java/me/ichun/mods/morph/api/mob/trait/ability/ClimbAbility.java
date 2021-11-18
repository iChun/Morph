package me.ichun.mods.morph.api.mob.trait.ability;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ClimbAbility extends Ability<ClimbAbility>
{
    public ClimbAbility()
    {
        type = "abilityClimb";
    }

    @Override
    public void tick(float strength)
    {
        if(player.collidedHorizontally)
        {
            Vector3d motion = player.getMotion();
            double motionX = MathHelper.clamp(motion.x, -0.15F, 0.15F);
            double motionZ = MathHelper.clamp(motion.z, -0.15F, 0.15F);
            double motionY = motion.y;
            if(player.isSneaking())
            {
                motionY -= motionY * strength;
            }
            else
            {
                motionY -= (motionY - 0.2D) * strength;
            }
            player.setMotion(motionX, motionY, motionZ);

            player.fallDistance -= player.fallDistance * strength;
        }
    }

    @Override
    public ClimbAbility copy()
    {
        return new ClimbAbility();
    }
}
