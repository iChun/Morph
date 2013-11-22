package morph.common.ability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.api.Ability;

public class AbilityStep extends Ability 
{

	@Override
	public String getType() 
	{
		return "step";
	}

	@Override
	public void tick() {
		if (getParent().stepHeight != 1f)
		{
			getParent().stepHeight = 1f;
		}
	}

	@Override
	public void kill() 
	{
		if (getParent().stepHeight == 1f) 
		{
			getParent().stepHeight = 0.5f;
		}
	}

	@Override
	public Ability clone() 
	{
		return new AbilityStep();
	}

	@Override
	public void save(NBTTagCompound tag) 
	{

	}

	@Override
	public void load(NBTTagCompound tag) 
	{

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void postRender() 
	{

	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}

	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/step.png");
}
