package me.ichun.mods.morph.client.morph;

import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.model.ModelInfo;
import me.ichun.mods.morph.client.model.ModelMorph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
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
            for(ModelRenderer renderer : modelMorph.modelList)
            {
                if(renderer.compiled)
                {
                    GLAllocation.deleteDisplayLists(renderer.displayList);
                    renderer.compiled = false;
                }
            }
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
        ent.worldObj = player.worldObj;
        ent.ridingEntity = player.ridingEntity;
        ent.hurtTime = player.hurtTime;
        ent.deathTime = player.deathTime;
        ent.isSwingInProgress = player.isSwingInProgress;
        ent.swingProgress = player.swingProgress;
        ent.swingProgressInt = player.swingProgressInt;

        boolean prevOnGround = ent.onGround;
        ent.onGround = player.onGround;

        if(player != mc.thePlayer) //Testing to see if the mob is on the ground or not if the morph isn't the MC player
        {
            ent.noClip = false;
            ent.setEntityBoundingBox(player.getEntityBoundingBox());
            ent.moveEntity(0.0D, -0.01D, 0.0D);
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
        
        if(ent instanceof EntityDragon)
        {
            ent.prevRotationYaw += 180F;
            ent.rotationYaw += 180F;
            ((EntityDragon)ent).deathTicks = player.deathTime;
        }

        for(int i = 0; i < 5; i++)
        {
            if(ent.getEquipmentInSlot(i) == null && player.getEquipmentInSlot(i) != null ||
                    ent.getEquipmentInSlot(i) != null && player.getEquipmentInSlot(i) == null ||
                    ent.getEquipmentInSlot(i) != null && player.getEquipmentInSlot(i) != null &&
                            !ent.getEquipmentInSlot(i).isItemEqual(player.getEquipmentInSlot(i)))
            {
                ent.setCurrentItemOrArmor(i, player.getEquipmentInSlot(i) != null ? player.getEquipmentInSlot(i).copy() : null);
            }
        }

        if(ent instanceof EntityPlayer && ((EntityPlayer)ent).getItemInUse() != player.getItemInUse())
        {
            ((EntityPlayer)ent).setItemInUse(player.getItemInUse() == null ? null : player.getItemInUse().copy(), player.getItemInUseCount());
        }
        //TODO check flight ability?
    }
}
