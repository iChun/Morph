package morph.common.ability;

import net.minecraft.nbt.NBTTagCompound;
import morph.api.Ability;

public class AbilityHostile extends Ability {

	/**
	 * This class is empty because it's just a trait class to be used as an identifier by the EventHandler
	 */
	
	@Override
	public String getType() 
	{
		return "hostile";
	}

	@Override
	public void tick() {}

	@Override
	public void kill() {}

	@Override
	public Ability clone() 
	{
		return new AbilityHostile();
	}

	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void load(NBTTagCompound tag) {}

	@Override
	public void postRender() {}

}
