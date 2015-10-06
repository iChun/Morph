package me.ichun.mods.morph.client.entity;

import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.model.ModelMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.util.ObfHelper;
import us.ichun.mods.ichunutil.common.core.util.ResourceHelper;

@SideOnly(Side.CLIENT)
public class EntityMorphAcquisition extends EntityLivingBase
{
    public EntityLivingBase acquired;
    public EntityLivingBase acquirer;

    public int progress;

    public ModelMorph model;
    public ResourceLocation acquiredTexture;
    public float prevScaleX = -1F;
    public float prevScaleY = -1F;
    public float prevScaleZ = -1F;

    public EntityMorphAcquisition(World par1World)
    {
        super(par1World);
        model = new ModelMorph();
        acquiredTexture = ResourceHelper.texPig;
        setSize(0.1F, 0.1F);
        noClip = true;
        renderDistanceWeight = 10D;
        ignoreFrustumCheck = true;
    }

    public EntityMorphAcquisition(World par1World, EntityLivingBase ac, EntityLivingBase ar)
    {
        super(par1World);
        acquired = ac;
        acquirer = ar;
        model = new ModelMorph(ModelHandler.getEntityModelInfo(ac), null, ac, null);
        for(ModelRenderer renderer : model.nextModels)
        {
            renderer.setRotationPoint(0F, 8F, 0F);
            renderer.rotateAngleX = renderer.rotateAngleY = renderer.rotateAngleZ = 0F;
        }
        Render rend = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(ac);
        acquiredTexture = ObfHelper.invokeGetEntityTexture(rend, rend.getClass(), ac);
        progress = 0;
        setSize(0.1F, 0.1F);
        noClip = true;
        renderDistanceWeight = 10D;
        ignoreFrustumCheck = true;
        setLocationAndAngles(acquired.posX, acquired.posY, acquired.posZ, acquired.rotationYaw, acquired.rotationPitch);
    }

    @Override
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        progress++;
        if(progress > 40)
        {
            setDead();
            return;
        }

        float prog = (float)progress / 20F;

        if(prog > 1.0F)
        {
            prog = 1.0F;
        }
        prog = (float)Math.pow(prog, 2);

        posX = acquired.posX + (acquirer.posX - acquired.posX) * prog;
        posY = acquired.getEntityBoundingBox().minY + (acquirer.getEntityBoundingBox().minY - acquired.getEntityBoundingBox().minY) * prog;
        posZ = acquired.posZ + (acquirer.posZ - acquired.posZ) * prog;
        setPosition(posX, posY, posZ);
    }

    @Override
    public boolean isEntityAlive()
    {
        return !this.isDead;
    }

    @Override
    public void setHealth(float par1)
    {
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound par1NBTTagCompound)
    {
        return false;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {}

    @Override
    public ItemStack getHeldItem() {
        return null;
    }

    @Override
    public ItemStack getEquipmentInSlot(int slotIn)
    {
        return null;
    }

    @Override
    public ItemStack getCurrentArmor(int i)
    {
        return null;
    }

    @Override
    public void setCurrentItemOrArmor(int i, ItemStack itemstack) {
    }

    @Override
    public ItemStack[] getInventory() {
        return new ItemStack[0];
    }

}
