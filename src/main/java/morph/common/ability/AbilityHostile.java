package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
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

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/hostile.png");

}
