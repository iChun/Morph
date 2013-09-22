package morph.common.ability;

import net.minecraft.entity.player.EntityPlayer;

public class AbilityFly extends Ability {

	@Override
	public String getType() 
	{
		return "fly";
	}

	@Override
	public void tick() 
	{
		if(getParent() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)getParent();
			if(!player.capabilities.allowFlying)
			{
				player.capabilities.allowFlying = true;
				player.sendPlayerAbilities();
			}
		}
		getParent().fallDistance = 0.0F;
		//TODO make "Thing" take note of this so it can fly...
	}

	@Override
	public void kill() 
	{
		if(getParent() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)getParent();
			if(!player.capabilities.isCreativeMode)
			{
				player.capabilities.allowFlying = false;
				if(player.capabilities.isFlying)
				{
					player.capabilities.isFlying = false;
				}
				player.sendPlayerAbilities();
			}
		}
	}

	@Override
	public Ability clone() 
	{
		return new AbilityFly();
	}

	@Override
	public void postRender() 
	{
	}

}
