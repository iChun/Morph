package morph.common.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerServer
	implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player plyr) 
	{
		EntityPlayerMP player = (EntityPlayerMP)plyr;
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try
		{
			int id = stream.readByte();
			switch(id)
			{
				case 0:
				{
					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}
}
