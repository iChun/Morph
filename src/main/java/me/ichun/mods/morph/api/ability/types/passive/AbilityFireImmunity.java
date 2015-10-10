package me.ichun.mods.morph.api.ability.types.passive;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class AbilityFireImmunity extends Ability
{
	public AbilityFireImmunity()
	{
        iconResource = new ResourceLocation("morph", "textures/icon/fireImmunity.png");
	}

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
				info = Morph.proxy.tickHandlerServer.morphsActive.get(player.getCommandSenderName());
			}
			else
			{
				info = Morph.proxy.tickHandlerClient.morphsActive.get(player.getCommandSenderName());
			}
		}
		
		boolean fireproof = true;
		
		if(info != null && info.nextState.getEntInstance(getParent().worldObj) instanceof EntitySkeleton)
		{
			EntitySkeleton skele = (EntitySkeleton)info.nextState.getEntInstance(getParent().worldObj);
			if(skele.getSkeletonType() != 1)
			{
				fireproof = false;
			}
		}
		
		if(fireproof)
		{
			if(!getParent().isImmuneToFire())
			{
                getParent().isImmuneToFire = true;
			}
			getParent().extinguish();
		}
	}

	@Override
	public void kill(ArrayList<Ability> nextAbilities)
	{
        getParent().isImmuneToFire = false;
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFireImmunity();
	}

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

    @Override
    public boolean isCharacteristic(EntityLivingBase living)
    {
        return true;
    }
}
