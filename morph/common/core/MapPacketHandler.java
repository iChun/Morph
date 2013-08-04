package morph.common.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MapPacketHandler 
	implements ITinyPacketHandler 
{

	@Override
	public void handle(NetHandler handler, Packet131MapData mapData) 
	{
		int id = mapData.uniqueID;
		if(handler instanceof NetServerHandler)
		{
			handleServerPacket((NetServerHandler)handler, mapData.uniqueID, mapData.itemData, (EntityPlayerMP)handler.getPlayer());
		}
		else
		{
			handleClientPacket((NetClientHandler)handler, mapData.uniqueID, mapData.itemData);
		}
	}


	public void handleServerPacket(NetServerHandler handler, short id, byte[] data, EntityPlayerMP player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					String identifier = stream.readUTF();
					
					MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
					if(info != null && info.getMorphing())
					{
						break;
					}
					
					MorphState old = info != null ? info.nextState : null;
					
					MorphState state = MorphHandler.getMorphState(player, identifier);
					
					if(state != null)
					{
						MorphInfo info2 = new MorphInfo(player.username, old, state);
						if(old == null)
						{
							info2.setMorphing(false);
							info2.morphProgress = 80;
						}
						else
						{
							info2.setMorphing(true);
						}
						
						Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.username, info2);
						
						PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());
						
						player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
					}
					
					break;
				}
			}
		}
		catch(IOException e)
		{
		}

	}
	
	//TODO Side Split
	
	@SideOnly(Side.CLIENT)
	public void handleClientPacket(NetClientHandler handler, short id, byte[] data)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
		try
		{
			switch(id)
			{
				case 0:
				{
					String user = stream.readUTF();
					
					try
					{
						
						
					}
					catch(Exception e)
					{
					}
					
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}
}