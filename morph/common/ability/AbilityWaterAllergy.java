package morph.common.ability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public class AbilityWaterAllergy extends Ability {

	@Override
	public String getType() 
	{
		return "waterAllergy";
	}

	@Override
	public void tick() 
	{
		if(getParent().isWet())
		{
			getParent().attackEntityFrom(DamageSource.drown, 1.0F);
		}
	}

	@Override
	public void kill() 
	{
	}

	@Override
	public Ability clone() 
	{
		return new AbilityWaterAllergy();
	}

	@Override
	public void postRender() 
	{
	}

}
