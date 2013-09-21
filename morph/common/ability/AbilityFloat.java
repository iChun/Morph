package morph.common.ability;

import net.minecraft.entity.player.EntityPlayer;


public class AbilityFloat extends Ability {

	public final double terminalVelocity;
	public final boolean negateFallDistance;
	
	public AbilityFloat(double termVelo, boolean negateFall)
	{
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
			getParent().motionY = terminalVelocity;
			if(negateFallDistance)
			{
				getParent().fallDistance = 0.0F;
			}
		}
	}

	@Override
	public void kill() 
	{
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFloat(terminalVelocity, negateFallDistance);
	}

	@Override
	public void postRender() 
	{
	}

}
