package morph.common.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.common.morph.MorphInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerServer 
	implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) 
	{
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
        if (type.equals(EnumSet.of(TickType.WORLD)))
        {
        	worldTick((WorldServer)tickData[0]);
        }
        else if (type.equals(EnumSet.of(TickType.PLAYER)))
        {
        	playerTick((WorldServer)((EntityPlayerMP)tickData[0]).worldObj, (EntityPlayerMP)tickData[0]);
        }
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.WORLD, TickType.PLAYER);
	}

	@Override
	public String getLabel() 
	{
		return "TickHandlerServerMorph";
	}

	public void worldTick(WorldServer world)
	{
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
						info.setMorphing(false);
					}
				}
			}
		}
	}

	public void playerTick(WorldServer world, EntityPlayerMP player)
	{
	}

	public long clock;
	
	public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
}