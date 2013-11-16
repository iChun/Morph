package morph.common.ability;

import morph.api.Ability;
import morph.common.Morph;
import morph.common.core.ObfHelper;
import morph.common.morph.MorphInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class AbilityFireImmunity extends Ability {

	@Override
	public String getType() 
	{
		return "fireImmunity";
	}

	@Override
	public void tick() 
	{
		MorphInfo info = null;
		if(getParent() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)getParent();
			if(!player.worldObj.isRemote)
			{
				info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
			}
			else
			{
				info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.username);
			}
		}
		
		boolean fireproof = true;
		
		if(info != null && info.nextState.entInstance instanceof EntitySkeleton)
		{
			EntitySkeleton skele = (EntitySkeleton)info.nextState.entInstance;
			if(skele.getSkeletonType() != 1)
			{
				fireproof = false;
			}
		}
		
		if(fireproof)
		{
			if(!getParent().isImmuneToFire())
			{
				try
				{
					ObfuscationReflectionHelper.setPrivateValue(Entity.class, getParent(), true, ObfHelper.isImmuneToFire);
				}
				catch(Exception e)
				{
					ObfHelper.obfWarning();
					e.printStackTrace();
				}
			}
			getParent().extinguish();
		}
	}

	@Override
	public void kill() 
	{
		try
		{
			ObfuscationReflectionHelper.setPrivateValue(Entity.class, getParent(), false, ObfHelper.isImmuneToFire);
		}
		catch(Exception e)
		{
			ObfHelper.obfWarning();
			e.printStackTrace();
		}
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFireImmunity();
	}

	@Override
	public void postRender() {}

	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void load(NBTTagCompound tag) {}
	
	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean entityHasAbility(EntityLivingBase living)
	{
		if(living instanceof EntitySkeleton)
		{
			EntitySkeleton skele = (EntitySkeleton)living;
			if(skele.getSkeletonType() != 1)
			{
				return false;
			}
		}
		return true;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fireImmunity.png");


}
