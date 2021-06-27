package me.ichun.mods.morph.api.morph;

import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MorphState
{
    public MorphVariant variant;
    private LivingEntity entInstance;

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
            EntityType<?> value = ForgeRegistries.ENTITIES.getValue(variant.id);
            if(value != null)
            {
                if(value.equals(EntityType.PLAYER))
                {
                    //TODO special handling for the player
                }
                else
                {
                    CompoundNBT tags = variant.getCumulativeTags();

                    Entity ent = value.create(world);
                    if(ent instanceof LivingEntity)
                    {
                        ent.read(tags);

                        entInstance = (LivingEntity)ent;
                        entInstance.setEntityId(MorphInfo.getNextEntId()); //to prevent ID collision
                    }
                }
            }

            if(entInstance == null) //we can't find the entity type or errored out somewhere... have a pig.
            {
                MorphApi.getLogger().error("Cannot find entity type: " + variant.id);
                entInstance = EntityType.PIG.create(world);
            }
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
        living.setSilent(player.isSilent());

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
        //TODO syncing of death of ender dragon
    }

    public static void specialEntityPlayerSync(LivingEntity living, PlayerEntity player)
    {
        if(living instanceof AgeableEntity)
        {
            ((AgeableEntity)living).setGrowingAge(living.isChild() ? -24000 : 0);
        }
    }
}
