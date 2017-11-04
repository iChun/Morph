package me.ichun.mods.morph.client.morph;

import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.model.ModelInfo;
import me.ichun.mods.morph.client.model.ModelMorph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MorphInfoClient extends MorphInfo
{
    private ModelInfo prevStateModel; //can be null
    private ModelInfo nextStateModel; //should never be null

    private ModelMorph modelMorph; //can be null;

    public MorphInfoClient(EntityPlayer player, MorphState prevState, MorphState nextState)
    {
        super(player, prevState, nextState);
    }

    public ModelInfo getNextStateModel(World world)
    {
        if(nextStateModel == null)
        {
            nextStateModel = ModelHandler.getEntityModelInfo(nextState.getEntInstance(world));
        }
        return nextStateModel;
    }

    public ModelInfo getPrevStateModel(World world)
    {
        if(prevState != null && prevStateModel == null)
        {
            prevStateModel = ModelHandler.getEntityModelInfo(prevState.getEntInstance(world));
        }
        return prevStateModel;
    }

    public ModelMorph getModelMorph(World world)
    {
        if(modelMorph == null)
        {
            modelMorph = new ModelMorph(getPrevStateModel(world), getNextStateModel(world), prevState != null ? prevState.getEntInstance(world) : null, nextState.getEntInstance(world));
        }
        return modelMorph;
    }

    @Override
    public void clean()
    {
        super.clean();
        if(modelMorph != null)
        {
            modelMorph.modelList.stream().filter(renderer -> renderer.compiled).forEach(renderer ->
            {
                GLAllocation.deleteDisplayLists(renderer.displayList);
                renderer.compiled = false;
            });
        }
    }

    @Override
    public void syncEntityWithPlayer(EntityLivingBase ent)
    {
        if(player == null)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        //prevs
        ent.prevRotationYawHead = player.prevRotationYawHead;
        ent.prevRotationYaw = player.prevRotationYaw;
        ent.prevRotationPitch = player.prevRotationPitch;
        ent.prevRenderYawOffset = player.prevRenderYawOffset;
        ent.prevLimbSwingAmount = player.prevLimbSwingAmount;
        ent.prevSwingProgress = player.prevSwingProgress;
        ent.prevPosX = player.prevPosX;
        ent.prevPosY = player.prevPosY;
        ent.prevPosZ = player.prevPosZ;

        //currents
        ent.rotationYawHead = player.rotationYawHead;
        ent.rotationYaw = player.rotationYaw;
        ent.rotationPitch = player.rotationPitch;
        ent.renderYawOffset = player.renderYawOffset;
        ent.limbSwingAmount = player.limbSwingAmount;
        ent.limbSwing = player.limbSwing;
        ent.posX = player.posX;
        ent.posY = player.posY;
        ent.posZ = player.posZ;
        ent.motionX = player.motionX;
        ent.motionY = player.motionY;
        ent.motionZ = player.motionZ;
        ent.ticksExisted = player.ticksExisted;
        ent.isAirBorne = player.isAirBorne;
        ent.moveStrafing = player.moveStrafing;
        ent.moveForward = player.moveForward;
        ent.dimension = player.dimension;
        ent.world = player.getEntityWorld();
        ent.ridingEntity = player.ridingEntity;
        ent.hurtTime = player.hurtTime;
        ent.deathTime = player.deathTime;
        ent.isSwingInProgress = player.isSwingInProgress;
        ent.swingProgress = player.swingProgress;
        ent.swingProgressInt = player.swingProgressInt;

        boolean prevOnGround = ent.onGround;
        ent.onGround = player.onGround;

        if(player != mc.player) //Testing to see if the mob is on the ground or not if the morph isn't the MC player
        {
            ent.noClip = false;
            ent.setEntityBoundingBox(player.getEntityBoundingBox());
            ent.move(MoverType.SELF, 0.0D, -0.01D, 0.0D);
            ent.posY = player.posY; //reset the position.
        }
        ent.noClip = player.noClip;

        ent.setSneaking(player.isSneaking());
        ent.setSprinting(player.isSprinting());
        ent.setInvisible(player.isInvisible());
        ent.setHealth(ent.getMaxHealth() * (player.getHealth() / player.getMaxHealth()));

        if(ent instanceof EntitySlime && prevOnGround && !ent.onGround)
        {
            ((EntitySlime)ent).squishAmount = 0.6F;
        }

        if(ent instanceof EntityRabbit)
        {
            if(MathHelper.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ) > 0.02D && ((EntityRabbit)ent).setJumpCompletion(0) == 0 || prevOnGround && !ent.onGround)
            {
                ((EntityRabbit)ent).startJumping();
            }
        }

        if(ent instanceof EntityDragon)
        {
            ent.prevRotationYaw += 180F;
            ent.rotationYaw += 180F;
            ((EntityDragon)ent).deathTicks = player.deathTime;
        }

        for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values())
        {
            ItemStack itemstack = ent.getItemStackFromSlot(entityequipmentslot);
            ItemStack itemstack1 = player.getItemStackFromSlot(entityequipmentslot);

            if(itemstack.isEmpty() && !itemstack1.isEmpty() ||
                    !itemstack.isEmpty() && itemstack1.isEmpty() ||
                    !itemstack.isEmpty() && !itemstack1.isEmpty() &&
                            !itemstack.isItemEqual(itemstack1))
            {
                ent.setItemStackToSlot(entityequipmentslot, !itemstack1.isEmpty() ? itemstack1.copy() : ItemStack.EMPTY);
            }
        }

        if(player.activeItemStack != ent.activeItemStack)
        {
            if(player.activeItemStack == null)
            {
                ent.resetActiveHand();
            }
            else if(!player.activeItemStack.isItemEqual(ent.activeItemStack)) //TODO test that this works
            {
                ent.setActiveHand(player.getActiveHand());
                ent.activeItemStack = player.activeItemStack.copy();
                ent.activeItemStackUseCount = player.activeItemStackUseCount;
            }
        }
        //TODO check flight ability?
    }
}
