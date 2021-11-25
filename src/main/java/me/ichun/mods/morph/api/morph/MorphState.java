package me.ichun.mods.morph.api.morph;

import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MorphState implements Comparable<MorphState>
{
    public MorphVariant variant;
    private LivingEntity entInstance;
    public float renderedShadowSize;
    public ArrayList<Trait<?>> traits = new ArrayList<>();

    private MorphState(){}

    public MorphState(MorphVariant variant, PlayerEntity player)
    {
        this.variant = variant;
        this.traits = MorphApi.getApiImpl().getTraitsForVariant(variant, player);
    }

    //For Traits
    public void activateHooks()
    {
        for(Trait<?> trait : traits)
        {
            trait.addHooks();
        }
    }
    public void deactivateHooks()
    {
        for(Trait<?> trait : traits)
        {
            trait.removeHooks();
        }
    }

    public void tick(PlayerEntity player, boolean resetInventory)
    {
        LivingEntity livingInstance = getEntityInstance(player.world, player.getGameProfile().getId());

        syncEntityPosRotWithPlayer(livingInstance, player);

        if(livingInstance.canUpdate())
        {
            livingInstance.tick();
        }

        syncEntityWithPlayer(livingInstance, player);

        syncInventory(livingInstance, player, resetInventory);
    }

    public void tickTraits()
    {
        for(Trait<?> trait : traits)
        {
            trait.doTick(1F);
        }
    }

    @Nonnull
    public LivingEntity getEntityInstance(World world, @Nullable UUID playerId)
    {
        if(entInstance == null || entInstance.world != world)
        {
            entInstance = variant.createEntityInstance(world, playerId);
        }

        return entInstance;
    }

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

        //Entity stuff
        living.collidedHorizontally = player.collidedHorizontally;
        living.collidedVertically = player.collidedVertically;
        living.setOnGround(player.isOnGround());
        living.setSneaking(player.isSneaking());
        living.setSwimming(player.isSwimming());
        living.setSprinting(player.isSprinting());

        //Cannot set silent, no more ambient noise??
//        living.setSilent(true); //we don't wanna hear the mob when they get damaged by fire ticks or something

        living.setHealth(living.getMaxHealth() * (player.getHealth() / player.getMaxHealth()));
        living.hurtTime = player.hurtTime;
        living.deathTime = player.deathTime;

        //LivingRender related stuff
        living.swingProgressInt = player.swingProgressInt;
        living.isSwingInProgress = player.isSwingInProgress;
        living.swingingHand = player.swingingHand;
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

        if(living instanceof MobEntity)
        {
            MobEntity mob = (MobEntity)living;
            mob.setLeftHanded(player.getPrimaryHand() == HandSide.LEFT);
            mob.setAggroed(player.isHandActive());
        }

        if(living instanceof PlayerEntity)
        {
            PlayerEntity playerEntity = (PlayerEntity)living;
            playerEntity.setPrimaryHand(player.getPrimaryHand());
        }

        if(living instanceof IAngerable)
        {
            ((IAngerable)living).setAngerTime(((IAngerable)living).isAngry() ? 1000 : 0);
        }

        //TODO synching of the pillager swing
        //TODO syncing of death of ender dragon
    }

    public static void syncInventory(LivingEntity living, PlayerEntity player, boolean reset)
    {
        if(living instanceof PlayerEntity)
        {
            PlayerEntity playerEntity = (PlayerEntity)living;

            //player entity plays sound when equipping items.
            for(EquipmentSlotType value : EquipmentSlotType.values())
            {
                boolean shouldReset = reset && (value == EquipmentSlotType.MAINHAND || value == EquipmentSlotType.OFFHAND);
                if(!ItemStack.areItemStacksEqual(living.getItemStackFromSlot(value), shouldReset ? ItemStack.EMPTY : player.getItemStackFromSlot(value)))
                {
                    ItemStack copy = shouldReset ? ItemStack.EMPTY : player.getItemStackFromSlot(value).copy();
                    if (value == EquipmentSlotType.MAINHAND) {
                        playerEntity.inventory.mainInventory.set(playerEntity.inventory.currentItem, copy);
                    } else if (value == EquipmentSlotType.OFFHAND) {
                        playerEntity.inventory.offHandInventory.set(0, copy);
                    } else if (value.getSlotType() == EquipmentSlotType.Group.ARMOR) {
                        playerEntity.inventory.armorInventory.set(value.getIndex(), copy);
                    }
                }
            }
        }
        else
        {
            for(EquipmentSlotType value : EquipmentSlotType.values())
            {
                boolean shouldReset = reset && (value == EquipmentSlotType.MAINHAND || value == EquipmentSlotType.OFFHAND);
                if(!ItemStack.areItemStacksEqual(living.getItemStackFromSlot(value), shouldReset ? ItemStack.EMPTY : player.getItemStackFromSlot(value)))
                {
                    living.setItemStackToSlot(value, shouldReset ? ItemStack.EMPTY : player.getItemStackFromSlot(value).copy());
                }
            }
        }

        if(player.isHandActive())
        {
            if(player.getItemInUseMaxCount() == 1)
            {
                Hand hand = player.getActiveHand();
                living.setActiveHand(hand);
                living.setLivingFlag(1, true);
                living.setLivingFlag(2, hand == Hand.OFF_HAND);
            }
        }
        else
        {
            living.setLivingFlag(1, false);
            living.resetActiveHand();
        }
    }
}
