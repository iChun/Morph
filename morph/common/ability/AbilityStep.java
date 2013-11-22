package morph.common.ability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.api.Ability;

public class AbilityStep extends Ability 
{

	public float stepHeight;
	
	public AbilityStep()
	{
		stepHeight = 1.0F;
	}
	
	public AbilityStep(float height)
	{
		stepHeight = height;
	}
	
	@Override
	public Ability parse(String[] args)
	{
		stepHeight = Float.parseFloat(args[0]);
		return this;
	}
	
	@Override
	public String getType() 
	{
		return "step";
	}

	@Override
	public void tick() 
	{
		if (getParent().stepHeight != stepHeight)
		{
			getParent().stepHeight = stepHeight;
		}
	}

	@Override
	public void kill() 
	{
		if (getParent().stepHeight == stepHeight) 
		{
			getParent().stepHeight = 0.5F;
		}
	}

	@Override
	public Ability clone() 
	{
		return new AbilityStep(stepHeight);
	}

	@Override
	public void save(NBTTagCompound tag) 
	{
		tag.setFloat("stepHeight", stepHeight);
	}

	@Override
	public void load(NBTTagCompound tag) 
	{
		stepHeight = tag.getFloat("stepHeight");
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
