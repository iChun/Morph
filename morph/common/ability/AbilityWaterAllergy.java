package morph.common.ability;

import morph.api.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
	public void postRender() {}

	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void load(NBTTagCompound tag) {}
}
