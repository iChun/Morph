package morph.client.entity;

import morph.client.model.ModelMorphAcquisition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityMorphAcquisition extends EntityLivingBase
{
	public EntityLivingBase acquired;
	public EntityLivingBase acquirer;
	
	public int progress;
	
	public ModelMorphAcquisition model;
	
	public EntityMorphAcquisition(World par1World) 
	{
		super(par1World);
		model = new ModelMorphAcquisition(this);
		yOffset = -0.5F;
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
		model = new ModelMorphAcquisition(this);
		progress = 0;
		yOffset = -0.5F;
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
		posY = acquired.boundingBox.minY + (acquirer.boundingBox.minY - acquired.boundingBox.minY) * prog + yOffset;
		posZ = acquired.posZ + (acquirer.posZ - acquired.posZ) * prog;
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
	public ItemStack getCurrentItemOrArmor(int i) {
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int i, ItemStack itemstack) {
	}

	@Override
	public ItemStack[] getLastActiveItems() {
		return new ItemStack[0];
	}

}
