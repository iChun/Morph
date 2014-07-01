package morph.common.morph;

import cpw.mods.fml.common.FMLCommonHandler;
import ichun.common.core.network.PacketHandler;
import morph.common.Morph;
import morph.common.packet.PacketMorphStates;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;

import java.util.*;

public class MorphHandler 
{
    public static HashMap<Class<? extends EntityLivingBase>, ArrayList<String>> stripperMappings = new HashMap<Class<? extends EntityLivingBase>, ArrayList<String>>();

    //TODO migrate name tags to player UUID in the future...? Do it now... or in 1.7.9?
	public static MorphState addOrGetMorphState(ArrayList<MorphState> states, MorphState state)
	{
		int pos = -1;
		boolean isFavourite = state.isFavourite;
		for(int i = states.size() - 1; i >= 0; i--)
		{
			MorphState mState = states.get(i);
			if(mState.identifier.equalsIgnoreCase(state.identifier) || !state.playerMorph.equalsIgnoreCase("") && mState.playerMorph.equalsIgnoreCase(state.playerMorph))
			{
				isFavourite = mState.isFavourite;
				states.remove(i);
				pos = i;
			}
		}
		if(state.playerName.equalsIgnoreCase(state.playerMorph) && !state.playerMorph.equalsIgnoreCase(""))
		{
			isFavourite = true;
			pos = 0;
		}
		if(pos != -1)
		{
			states.add(pos, state);
		}
		else
		{
			states.add(state);
		}
		state.isFavourite = isFavourite;
		if(FMLCommonHandler.instance().getEffectiveSide().isClient() && Morph.config.getInt("sortMorphs") == 2)
		{
			Collections.sort(states);
		}
		return state;
	}
	
	public static void updatePlayerOfMorphStates(EntityPlayerMP player, MorphState morphState, boolean clear)
	{
		if(player.playerNetServerHandler == null || player.getClass() == FakePlayer.class)
		{
			return;
		}
		ArrayList<MorphState> states;
		if(morphState == null)
		{
			states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player);
		}
		else
		{
			states = new ArrayList<MorphState>();
			states.add(morphState);
		}

        PacketHandler.sendToPlayer(Morph.channels, new PacketMorphStates(clear, states), player);
	}

	public static MorphState getMorphState(EntityPlayerMP player, String identifier) 
	{
		ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player);
		
		for(MorphState state : states)
		{
			if(state.identifier.equalsIgnoreCase(identifier))
			{
				return state;
			}
		}
		return null;
	}

	public static void reorderMorphs(String starter, LinkedHashMap<String, ArrayList<MorphState>> morphMap) 
	{
		if(Morph.config.getInt("sortMorphs") == 1 || Morph.config.getInt("sortMorphs") == 2)
		{
			ArrayList<String> order = new ArrayList<String>();
			Iterator<String> ite = morphMap.keySet().iterator();
			while(ite.hasNext())
			{
				order.add(ite.next());
			}
			Collections.sort(order);
			
			order.remove(starter);
			
			order.add(0, starter);
			
			LinkedHashMap<String, ArrayList<MorphState>> bufferList = new LinkedHashMap<String, ArrayList<MorphState>>(morphMap);
			
			morphMap.clear();
			
			for(int i = 0; i < order.size(); i++)
			{
				morphMap.put(order.get(i), bufferList.get(order.get(i)));
			}
		}
	}

    public static ArrayList<String> getNBTTagsToStrip(EntityLivingBase ent)
    {
        ArrayList<String> tagsToStrip = new ArrayList<String>();
        Class clz = ent.getClass();
        while(clz != EntityLivingBase.class)
        {
            tagsToStrip.addAll(getNBTTagsToStrip(clz));
            clz = clz.getSuperclass();
        }
        return tagsToStrip;
    }

    public static ArrayList<String> getNBTTagsToStrip(Class<? extends EntityLivingBase> clz)
    {
        ArrayList<String> list = MorphHandler.stripperMappings.get(clz);
        if(list == null)
        {
            list = new ArrayList<String>();
            MorphHandler.stripperMappings.put(clz, list);
        }
        return list;
    }
}
