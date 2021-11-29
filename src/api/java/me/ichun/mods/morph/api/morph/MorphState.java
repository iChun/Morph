package me.ichun.mods.morph.api.morph;

import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.api.mob.trait.Trait;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

public class MorphState implements Comparable<MorphState>
{
    public MorphVariant variant;
    private LivingEntity entInstance;
    private final Collection<ItemEntity> entInstanceDropCapture = new ArrayList<>();
    public float renderedShadowSize;
    public ArrayList<Trait<?>> traits = new ArrayList<>();

    private MorphState(){}

    public MorphState(MorphVariant variant, PlayerEntity player)
    {
        this.variant = variant;
        this.traits = MorphApi.getApiImpl().getTraitsForVariant(variant, player);
        //TODO make sure other clients know what traits the player has unlocked
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
        livingInstance.captureDrops(entInstanceDropCapture); //We don't want our mob instance to drop items
        entInstanceDropCapture.clear(); //Have the items, GC.

        syncEntityPosRotWithPlayer(livingInstance, player);

        syncInventory(livingInstance, player, true); //reset the inventory so the entity doesn't actually use our equipment when ticking.

        if(livingInstance.canUpdate())
        {
            livingInstance.tick();
        }

        syncEntityWithPlayer(livingInstance, player);

        if(!resetInventory)
        {
            syncInventory(livingInstance, player, false); //sync the inventory for rendering purposes.
        }

        livingInstance.getDataManager().setClean(); //we don't want to flood the client with packets for an entity it can't find.
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

            for(Trait<?> trait : traits)
            {
                trait.livingInstance = entInstance;
            }
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

        //Clear potions so they don't get ticked when we tick this entity
        living.getActivePotionMap().clear();
    }

    public static void syncEntityWithPlayer(LivingEntity living, PlayerEntity player)
    {
        syncEntityPosRotWithPlayer(living, player); //resync with the player position in case the entity moved whilst ticking.

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

        //Sync potions for rendering purposes
        living.getActivePotionMap().putAll(player.getActivePotionMap());

        specialEntityPlayerSync(living, player);
    }

    public static void specialEntityPlayerSync(LivingEntity living, PlayerEntity player)
    {
        for(BiConsumer<LivingEntity, PlayerEntity> consumer : MorphApi.getApiImpl().getModPlayerMorphSyncConsumers())
        {
            consumer.accept(living, player);
        }
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
