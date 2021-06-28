package me.ichun.mods.morph.api.morph;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MorphState implements Comparable<MorphState>
{
    public MorphVariant variant;
    private LivingEntity entInstance;
    public float renderedShadowSize;

    private MorphState(){}

    public MorphState(MorphVariant variant)
    {
        this.variant = variant;
    }

    public void tick(PlayerEntity player)
    {
        LivingEntity livingInstance = getEntityInstance(player.world);

        syncEntityPosRotWithPlayer(livingInstance, player);

        if(livingInstance.canUpdate())
        {
            livingInstance.tick();
        }

        syncEntityWithPlayer(livingInstance, player);
    }

    @Nonnull
    public LivingEntity getEntityInstance(World world)
    {
        if(entInstance == null || entInstance.world != world)
        {
            entInstance = variant.createEntityInstance(world);
        }

        return entInstance;
    }

    //TODO handle the living isChild, force the left/right handedness

    public CompoundNBT write(CompoundNBT tag)
    {
        tag.put("variant", variant.write(new CompoundNBT()));
        return tag;
    }

    public void read(CompoundNBT tag)
    {
        variant = MorphVariant.createFromNBT(tag.getCompound("variant"));
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof MorphState)
        {
            MorphState state = (MorphState)obj;
            return Objects.equals(variant, state.variant);
        }
        return false;
    }

    @Override
    public int compareTo(MorphState o)
    {
        return variant.compareTo(o.variant);
    }

    public static MorphState createFromNbt(CompoundNBT tag)
    {
        MorphState state = new MorphState();
        state.read(tag);
        return state;
    }

    public static void syncEntityPosRotWithPlayer(LivingEntity living, PlayerEntity player)
    {
        living.ticksExisted = player.ticksExisted;

        living.setLocationAndAngles(player.getPosX(), player.getPosY(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
        living.lastTickPosX = player.lastTickPosX;
        living.lastTickPosY = player.lastTickPosY;
        living.lastTickPosZ = player.lastTickPosZ;

        living.prevPosX = player.prevPosX;
        living.prevPosY = player.prevPosY;
        living.prevPosZ = player.prevPosZ;

        living.prevRotationYaw = player.prevRotationYaw;
        living.prevRotationPitch = player.prevRotationPitch;

        living.rotationYawHead = player.rotationYawHead;
        living.prevRotationYawHead = player.prevRotationYawHead;

        living.renderYawOffset = player.renderYawOffset;
        living.prevRenderYawOffset = player.prevRenderYawOffset;
    }

    public static void syncEntityWithPlayer(LivingEntity living, PlayerEntity player)
    {
        syncEntityPosRotWithPlayer(living, player);

        //Others
        living.limbSwing = player.limbSwing;
        living.limbSwingAmount = player.limbSwingAmount;

        living.setMotion(player.getMotion());

        living.setOnGround(player.isOnGround());
        living.setSneaking(player.isSneaking());
        living.setSwimming(player.isSwimming());
        living.setSprinting(player.isSprinting());
        living.setSilent(true); //we don't wanna hear the mob when they get damaged by fire ticks or something

        living.setHealth(living.getMaxHealth() * (player.getHealth() / player.getMaxHealth()));
        living.hurtTime = player.hurtTime;
        living.deathTime = player.deathTime;

        //LivingRender related stuff
        living.swingProgress = player.swingProgress;
        living.prevSwingProgress = player.prevSwingProgress;

        living.ridingEntity = player.ridingEntity;

        Pose pose = living.getPose();
        living.setPose(player.getPose());

        if(pose != living.getPose())
        {
            living.recalculateSize();
        }

        living.setInvisible(player.isInvisible());

        living.setUniqueId(player.getUniqueID());

        living.setGlowing(player.isGlowing());

        //EntityRendererManager stuff

        living.forceFireTicks(player.getFireTimer());

        specialEntityPlayerSync(living, player);
    }

    public static void specialEntityPlayerSync(LivingEntity living, PlayerEntity player)
    {
        if(living instanceof AgeableEntity)
        {
            ((AgeableEntity)living).setGrowingAge(living.isChild() ? -24000 : 0);
        }
        //TODO syncing of death of ender dragon
    }
}
