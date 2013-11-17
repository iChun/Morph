package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.api.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;


public class AbilityFloat extends Ability {

	public double terminalVelocity;
	public boolean negateFallDistance;
	
	public AbilityFloat()
	{
		terminalVelocity = -1000D;
		negateFallDistance = false;
	}
	
	public AbilityFloat(double termVelo, boolean negateFall)
	{
		terminalVelocity = termVelo;
		negateFallDistance = negateFall;
	}
	
	public AbilityFloat(Object...data)
	{
		this();
		try
		{
			if(data[0] instanceof Double)
			{
				terminalVelocity = (Double)data[0];
			}
			else if(data[0] instanceof Float)
			{
				terminalVelocity = (Float)data[0];
			}
			else if(data[0] instanceof Integer)
			{
				terminalVelocity = (Integer)data[0];
			}
			else if(data[0] instanceof String)
			{
				terminalVelocity = Double.parseDouble((String)data[0]);
			}
			
			if(data[1] instanceof Boolean)
			{
				negateFallDistance = (Boolean)data[1];
			}
			else if(data[1] instanceof String)
			{
				negateFallDistance = Boolean.parseBoolean((String)data[1]);
			}
		}
		catch(Exception e)
		{
			
		}
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
	public void postRender() {}

	@Override
	public void save(NBTTagCompound tag) 
	{
		tag.setDouble("terminalVelocity", terminalVelocity);
		tag.setBoolean("negateFallDistance", negateFallDistance);
	}

	@Override
	public void load(NBTTagCompound tag) 
	{
		terminalVelocity = tag.getDouble("terminalVelocity");
		negateFallDistance = tag.getBoolean("negateFallDistance");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/float.png");

}
