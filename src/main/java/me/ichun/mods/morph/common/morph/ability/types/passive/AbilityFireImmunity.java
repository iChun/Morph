package me.ichun.mods.morph.common.morph.ability.types.passive;

import me.ichun.mods.morph.api.ability.Ability;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class AbilityFireImmunity extends Ability
{
	public AbilityFireImmunity()
	{
        iconResource = new ResourceLocation("morph", "textures/icon/fire_immunity.png");
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
				info = Morph.eventHandlerServer.morphsActive.get(player.getName());
			}
			else
			{
				info = Morph.eventHandlerClient.morphsActive.get(player.getName());
			}
		}
		
		boolean fireproof = true;
		
		if(info != null && info.nextState.getEntInstance(getParent().worldObj) instanceof EntitySkeleton)
		{
			EntitySkeleton skele = (EntitySkeleton)info.nextState.getEntInstance(getParent().worldObj);
			if(skele.getSkeletonType() != SkeletonType.WITHER)
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
			if(skele.getSkeletonType() != SkeletonType.WITHER)
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
