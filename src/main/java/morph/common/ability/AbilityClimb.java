package morph.common.ability;

import morph.api.Ability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AbilityClimb extends Ability {

	@Override
	public String getType() 
	{
		return "climb";
	}

	@Override
	public void tick() 
	{
		if(getParent().isCollidedHorizontally)
		{
			getParent().fallDistance = 0.0F;
			if(getParent().isSneaking())
			{
				getParent().motionY = 0.0D;
			}
			else
			{
				getParent().motionY = 0.1176D; //(0.2D - 0.08D) * 0.98D
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
		return new AbilityClimb();
	}

	@Override
	public void postRender() {}

	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void load(NBTTagCompound tag) {}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/climb.png");

}
