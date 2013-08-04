package morph.common.morph;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import morph.common.Morph;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;

public class MorphHandler 
{

	public static MorphState addOrGetMorphState(ArrayList<MorphState> states, MorphState state)
	{
		int pos = -1;
		for(int i = states.size() - 1; i >= 0; i--)
		{
			MorphState mState = states.get(i);
			if(mState.identifier.equalsIgnoreCase(state.identifier))
			{
				states.remove(i);
				pos = i;
			}
		}
		if(pos != -1)
		{
			states.add(pos, state);
		}
		else
		{
			states.add(state);
		}
		return state;
	}
	
	public static void updatePlayerOfMorphStates(EntityPlayerMP player, MorphState morphState)
	{
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
			
			for(MorphState state : states)
			{
				stream.writeUTF("state");
				Morph.writeNBTTagCompound(state.getTag(), stream);
			}
			
			stream.writeUTF("##end");
		}
		catch(IOException e)
		{
		}
		PacketDispatcher.sendPacketToPlayer(new Packet250CustomPayload("Morph", bytes.toByteArray()), (Player)player);
	}
	
}
