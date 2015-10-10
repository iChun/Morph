package me.ichun.mods.morph.api.ability.types.active;

import me.ichun.mods.morph.api.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityFloat extends Ability
{
	public double terminalVelocity;
	public boolean negateFallDistance;
	
	public AbilityFloat()
	{
        iconResource = new ResourceLocation("morph", "textures/icon/float.png");
		terminalVelocity = -1000D;
		negateFallDistance = false;
	}
	
	public AbilityFloat(double termVelo, boolean negateFall)
	{
        iconResource = new ResourceLocation("morph", "textures/icon/float.png");
		terminalVelocity = termVelo;
		negateFallDistance = negateFall;
	}
	
	@Override
	public String getType() 
	{
		return "float";
	}

	@Override
	public void tick() 
	{
		boolean flying = false;
		if(getParent() instanceof EntityPlayer)
		{
			flying = ((EntityPlayer)getParent()).capabilities.isFlying;
		}
		if(!flying && getParent().motionY < terminalVelocity)
		{
            isActive = true;
			getParent().motionY = terminalVelocity;
			if(negateFallDistance)
			{
				getParent().fallDistance = 0.0F;
			}
		}
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFloat(terminalVelocity, negateFallDistance);
	}

    @Override
    public boolean isCharacteristic(EntityLivingBase living)
    {
        return true;
    }
}
