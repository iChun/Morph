package morph.client.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.common.morph.MorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient 
	implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.RENDER)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{
				preRenderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		if (type.equals(EnumSet.of(TickType.CLIENT)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{      		
				worldTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld);
			}
		}
		else if (type.equals(EnumSet.of(TickType.PLAYER)))
		{
			playerTick((World)((EntityPlayer)tickData[0]).worldObj, (EntityPlayer)tickData[0]);
		}
		else if (type.equals(EnumSet.of(TickType.RENDER)))
		{
			if(Minecraft.getMinecraft().theWorld != null)
			{
				renderTick(Minecraft.getMinecraft(), Minecraft.getMinecraft().theWorld, (Float)tickData[0]); //only ingame
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.CLIENT, TickType.PLAYER, TickType.RENDER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerClientMorph";
	}

	public void worldTick(Minecraft mc, WorldClient world)
	{
		if(clock != world.getWorldTime())
		{
			clock = world.getWorldTime();
			
			if(clock != world.getWorldTime())
			{
				clock = world.getWorldTime();
				
				for(Entry<String, MorphInfo> e : playerMorphInfo.entrySet())
				{
					MorphInfo info = e.getValue();
					
					if(info.getMorphing())
					{
						info.morphProgress++;
						if(info.morphProgress > 80)
						{
							info.morphProgress = 80;
						}
					}
				}
			}
		}
	}

	public void playerTick(World world, EntityPlayer player)
	{
	}

	public void preRenderTick(Minecraft mc, World world, float renderTick)
	{
	}

	public void renderTick(Minecraft mc, World world, float renderTick)
	{
	}
	
	public long clock;
	
	public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
}