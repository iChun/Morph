package morph.common.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
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
			
//			for(int i = 0 ; i < world.loadedEntityList.size(); i++)
//			{
//				if(world.loadedEntityList.get(i) instanceof EntityCow)
//				{
//					((EntityCow)world.loadedEntityList.get(i)).setDead();
//				}
//			}
			
			if(world.provider.dimensionId == 0)
			{
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
				
//				ArrayList<MorphState> states = getPlayerMorphs(world, "ohaiiChun");
//				for(MorphState state : states)
//				{
//					System.out.println(state.identifier);
//				}
			}
		}
	}

	public void playerTick(WorldServer world, EntityPlayerMP player)
	{
	}
	
	public MorphState getSelfState(World world, String name)
	{
		ArrayList<MorphState> list = getPlayerMorphs(world, name);
		for(MorphState state : list)
		{
			if(state.playerName.equalsIgnoreCase(state.playerMorph))
			{
				return state;
			}
		}
		return new MorphState(world, name, name, null, world.isRemote);
	}
	
	public ArrayList<MorphState> getPlayerMorphs(World world, String name)
	{
		ArrayList<MorphState> list = playerMorphs.get(name);
		if(list == null)
		{
			list = new ArrayList<MorphState>();
			playerMorphs.put(name, list);
			list.add(new MorphState(world, name, name, null, world.isRemote));
		}
		return list;
	}
	
	public boolean hasMorphState(EntityPlayer player, MorphState state)
	{
		ArrayList<MorphState> states = getPlayerMorphs(player.worldObj, player.username);
		if(!state.playerMorph.equalsIgnoreCase(""))
		{
			for(MorphState mState : states)
			{
				if(mState.playerMorph.equalsIgnoreCase(state.playerMorph))
				{
					return true;
				}
			}
		}
		return false;
	}

	public long clock;
	
	public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
	public HashMap<String, ArrayList<MorphState>> playerMorphs = new HashMap<String, ArrayList<MorphState>>();
}