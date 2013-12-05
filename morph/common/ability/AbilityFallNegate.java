package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import morph.api.Ability;

public class AbilityFallNegate extends Ability 
{
	
	@Override
	public String getType() 
	{
		return "fallNegate";
	}

	@Override
	public void tick() 
	{
		getParent().fallDistance = -0.5F;
	}

	@Override
	public void kill() {}

	@Override
	public Ability clone() 
	{
		return new AbilityFallNegate();
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
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fallNegate.png");

}
