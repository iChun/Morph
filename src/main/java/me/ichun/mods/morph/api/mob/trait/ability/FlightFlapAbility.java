package me.ichun.mods.morph.api.mob.trait.ability;

import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlightFlapAbility extends Ability<FlightFlapAbility>
{
    public Boolean resetVerticalVelocity;
    public Double velocityAdded;
    public Integer flapLimit;
    public Boolean slowdownInWater;

    public transient double flaps;
    public transient boolean keyHeld;
    public transient boolean wasOnGround; //we tick after the entity ticks so the ent tick already sets jump when we're on the ground

    public FlightFlapAbility()
    {
        type = "abilityFlightFlap";
    }

    @Override
    public void addHooks()
    {
        super.addHooks();
        if(velocityAdded == null)
        {
            velocityAdded = 0.42D;
        }
    }

    @Override
    public void tick(float strength)
    {
        combinedTick(strength, velocityAdded);
    }

    @Override
    public void transitionalTick(FlightFlapAbility prevTrait, float transitionProgress)
    {
        flaps = prevTrait.flaps;
        combinedTick(1F, MathHelper.lerp(transitionProgress, prevTrait.velocityAdded, velocityAdded));
    }

    private void combinedTick(float strength, double velocityToAdd)
    {
        if(player.world.isRemote)
        {
            clientTick(strength, velocityToAdd);
        }

        if(slowdownInWater != null && slowdownInWater && player.areEyesInFluid(FluidTags.WATER))
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

    @OnlyIn(Dist.CLIENT)
    private void clientTick(float strength, double velocityToAdd)
    {
        if(!keyHeld && Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown()) //hit jump key
        {
            boolean canFlap = (flapLimit == null || flaps < flapLimit) && !wasOnGround;

            //taken from LivingEntity.livingTick, onGround replaced with canFlap
            if(!player.abilities.isFlying && canFlap)
            {
                double d7;
                if (player.isInLava()) {
                    d7 = player.func_233571_b_(FluidTags.LAVA);
                } else {
                    d7 = player.func_233571_b_(FluidTags.WATER);
                }

                boolean flag = player.isInWater() && d7 > 0.0D;
                double d8 = player.getFluidJumpHeight();
                if (!flag || !(d7 > d8))
                {
                    if (!player.isInLava() || !(d7 > d8))
                    {
                        jump(strength, velocityToAdd);
                    }
                    else
                    {
                        player.setMotion(player.getMotion().add(0.0D, (double)0.04F * player.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue(), 0.0D));
                    }
                }
                else
                {
                    player.setMotion(player.getMotion().add(0.0D, (double)0.04F * player.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue(), 0.0D));
                }
            }
        }
        keyHeld = Minecraft.getInstance().gameSettings.keyBindJump.isKeyDown();
        wasOnGround = player.isOnGround();

        //reset the flap count if the player is on the ground.
        if(player.isOnGround())
        {
            flaps = 0;
        }
    }

    public void jump(float strength, double velocityToAdd)
    {
        //Mostly taken from entity.jump
        double d = velocityToAdd * getJumpFactor() * strength;
        if(player.isPotionActive(Effects.JUMP_BOOST))
        {
            d += 0.1D * (player.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1);
        }

        Vector3d motion = player.getMotion();
        if(resetVerticalVelocity != null && resetVerticalVelocity)
        {
            player.setMotion(motion.x, d, motion.z);
        }
        else
        {
            player.setMotion(motion.add(0D, d, 0D));
        }

        player.isAirBorne = true;
        //we're intentionally not triggering the Forge event, we're not doing a fresh jump.
    }

    public float getJumpFactor() {
        float f = player.world.getBlockState(player.getPosition()).getBlock().getJumpFactor();
        float f1 = player.world.getBlockState(new BlockPos(player.getPosX(), player.getBoundingBox().minY - 0.5000001D, player.getPosZ())).getBlock().getJumpFactor();
        return (double)f == 1.0D ? f1 : f;
    }

    @Override
    public FlightFlapAbility copy()
    {
        FlightFlapAbility ability = new FlightFlapAbility();
        ability.resetVerticalVelocity = this.resetVerticalVelocity;
        ability.velocityAdded = this.velocityAdded;
        ability.flapLimit = this.flapLimit;
        ability.slowdownInWater = this.slowdownInWater;
        return ability;
    }
}
