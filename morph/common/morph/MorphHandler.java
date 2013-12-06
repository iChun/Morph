package morph.common.morph;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import morph.common.Morph;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.FakePlayer;

public class MorphHandler 
{

	public static MorphState addOrGetMorphState(ArrayList<MorphState> states, MorphState state)
	{
		int pos = -1;
		for(int i = states.size() - 1; i >= 0; i--)
		{
			MorphState mState = states.get(i);
			if(mState.identifier.equalsIgnoreCase(state.identifier) || !state.playerMorph.equalsIgnoreCase("") && mState.playerMorph.equalsIgnoreCase(state.playerMorph))
			{
				states.remove(i);
				pos = i;
			}
		}
		if(state.playerName.equalsIgnoreCase(state.playerMorph) && !state.playerMorph.equalsIgnoreCase(""))
		{
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
		if(Morph.sortMorphsAlphabetically == 2)
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
			states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username);
		}
		else
		{
			states = new ArrayList<MorphState>();
			states.add(morphState);
		}
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try
		{
			stream.writeByte(1); //id
			
			stream.writeBoolean(clear);
			
			for(MorphState state : states)
			{
				stream.writeUTF("state");
				Morph.writeNBTTagCompound(state.getTag(), stream);
				
				if(bytes.toByteArray().length > 24000)
				{
					stream.writeUTF("##end");
					
					PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Morph", bytes.toByteArray()), (Player)player);
					
					bytes = new ByteArrayOutputStream();
					stream = new DataOutputStream(bytes);
					
					stream.writeByte(1); //id
					
					stream.writeBoolean(false);
				}
			}
			
			stream.writeUTF("##end");
		}
		catch(IOException e)
		{
		}
		PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Morph", bytes.toByteArray()), (Player)player);
	}

	public static MorphState getMorphState(EntityPlayerMP player, String identifier) 
	{
		ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username);
		
		for(MorphState state : states)
		{
			if(state.identifier.equalsIgnoreCase(identifier))
			{
				return state;
			}
		}
		return null;
	}
	
}
