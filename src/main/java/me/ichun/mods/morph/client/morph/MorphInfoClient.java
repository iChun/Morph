package me.ichun.mods.morph.client.morph;

import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.model.ModelInfo;
import me.ichun.mods.morph.client.model.ModelMorph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
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
            GameType oriGameTypePrev = null;
            NetworkPlayerInfo npiPrev = null;
            if(prevState != null)
            {
                if(prevState.getEntInstance(world) instanceof EntityOtherPlayerMP)
                {
                    npiPrev = Minecraft.getMinecraft().getConnection().getPlayerInfo(((EntityOtherPlayerMP)prevState.getEntInstance(world)).getGameProfile().getId());
                    if(npiPrev != null)
                    {
                        oriGameTypePrev = npiPrev.getGameType();
                        npiPrev.setGameType(GameType.ADVENTURE);
                    }
                }
            }

            GameType oriGameTypeNext = null;
            NetworkPlayerInfo npiNext = null;
            if(nextState.getEntInstance(world) instanceof EntityOtherPlayerMP)
            {
                npiNext = Minecraft.getMinecraft().getConnection().getPlayerInfo(((EntityOtherPlayerMP)nextState.getEntInstance(world)).getGameProfile().getId());
                if(npiNext != null)
                {
                    oriGameTypeNext = npiNext.getGameType();
                    npiNext.setGameType(GameType.ADVENTURE);
                }
            }
            modelMorph = new ModelMorph(getPrevStateModel(world), getNextStateModel(world), prevState != null ? prevState.getEntInstance(world) : null, nextState.getEntInstance(world));

            if(npiPrev != null)
            {
                npiPrev.setGameType(oriGameTypePrev);
            }
            if(npiNext != null)
            {
                npiNext.setGameType(oriGameTypeNext);
            }
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
    public void tick()
    {
        Minecraft mc = Minecraft.getMinecraft();

        GameType oriGameTypePrev = null;
        NetworkPlayerInfo npiPrev = null;
        if(prevState != null && prevState.getEntInstance(mc.world) instanceof EntityOtherPlayerMP)
        {
            EntityOtherPlayerMP player = (EntityOtherPlayerMP)prevState.getEntInstance(mc.world);
            npiPrev = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getGameProfile().getId());
            if(npiPrev != null)
            {
                oriGameTypePrev = npiPrev.getGameType();
                npiPrev.setGameType(GameType.SPECTATOR);
            }
        }

        GameType oriGameTypeNext = null;
        NetworkPlayerInfo npiNext = null;
        if(nextState.getEntInstance(mc.world) instanceof EntityOtherPlayerMP)
        {
            EntityOtherPlayerMP player = (EntityOtherPlayerMP)nextState.getEntInstance(mc.world);
            npiNext = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getGameProfile().getId());
            if(npiNext != null)
            {
                oriGameTypeNext = npiNext.getGameType();
                npiNext.setGameType(GameType.SPECTATOR);
            }
        }
        super.tick();
        if(npiPrev != null)
        {
            npiPrev.setGameType(oriGameTypePrev);
        }
        if(npiNext != null)
        {
            npiNext.setGameType(oriGameTypeNext);
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
        ent.swingingHand = player.swingingHand;
        ent.swingProgress = player.swingProgress;
        ent.swingProgressInt = player.swingProgressInt;

        boolean prevOnGround = ent.onGround;
        ent.onGround = player.onGround;

        if(player != mc.player) //Testing to see if the mob is on the ground or not if the morph isn't the MC player
        {
            ent.motionX = player.posX - player.lastTickPosX;
            ent.motionY = player.posY - player.lastTickPosY;
            ent.motionZ = player.posZ - player.lastTickPosZ;

            ent.noClip = false;
            ent.setEntityBoundingBox(player.getEntityBoundingBox());
            float distanceWalkedModified = ent.distanceWalkedModified;
            float distanceWalkedOnStepModified = ent.distanceWalkedOnStepModified;
            ent.move(MoverType.SELF, 0.0D, -0.01D, 0.0D);
            ent.distanceWalkedModified = distanceWalkedModified;
            ent.distanceWalkedOnStepModified = distanceWalkedOnStepModified;
            ent.posY = player.posY; //reset the position.
        }
        ent.noClip = true;
        ent.entityCollisionReduction = 1.0F;
        ent.setEntityBoundingBox(player.getEntityBoundingBox());

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
            if(MathHelper.sqrt(ent.motionX * ent.motionX + ent.motionZ * ent.motionZ) > 0.02D && ((EntityRabbit)ent).getJumpCompletion(0) == 0 || prevOnGround && !ent.onGround)
            {
                ((EntityRabbit)ent).startJumping();

                //createRunningParticles();
                int i = MathHelper.floor(player.posX);
                int j = MathHelper.floor(player.posY - 0.20000000298023224D);
                int k = MathHelper.floor(player.posZ);
                BlockPos blockpos = new BlockPos(i, j, k);
                IBlockState iblockstate = player.getEntityWorld().getBlockState(blockpos);

                if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE)
                {
                    player.getEntityWorld().spawnParticle(EnumParticleTypes.BLOCK_CRACK, player.posX + ((double)player.getRNG().nextFloat() - 0.5D) * (double)player.width, player.getEntityBoundingBox().minY + 0.1D, player.posZ + ((double)player.getRNG().nextFloat() - 0.5D) * (double)player.width, -ent.motionX * 4.0D, 1.5D, -ent.motionZ * 4.0D, Block.getStateId(iblockstate));
                }
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
            if(player.activeItemStack == ItemStack.EMPTY)
            {
                ent.resetActiveHand();
                if(ent instanceof IRangedAttackMob)
                {
                    ((IRangedAttackMob)ent).setSwingingArms(false);
                }
            }
            else if(!player.activeItemStack.isItemEqual(ent.activeItemStack))
            {
                ent.setActiveHand(player.getActiveHand());
                ent.activeItemStack = player.activeItemStack.copy();
                ent.activeItemStackUseCount = player.activeItemStackUseCount;
                if(ent instanceof IRangedAttackMob)
                {
                    ((IRangedAttackMob)ent).setSwingingArms(true);
                }
            }
        }
        //TODO check flight ability?
    }
}
