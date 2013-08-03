package morph.client.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import morph.client.morph.MorphInfoClient;
import morph.common.Morph;
import morph.common.core.ObfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient
	implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		Minecraft mc = Minecraft.getMinecraft();
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try
		{
			int id = stream.readByte();
			switch(id)
			{
				case 0:
				{
					String name = stream.readUTF();
					
					boolean morphing = stream.readBoolean();
					int morphProg = stream.readInt();

					byte isPlayer = stream.readByte();
					
					String username1 = stream.readUTF();
					String username2 = stream.readUTF();
					
					NBTTagCompound prevTag = Morph.readNBTTagCompound(stream);
					NBTTagCompound nextTag = Morph.readNBTTagCompound(stream);

					EntityLivingBase prevEnt;
					EntityLivingBase nextEnt;
					
					if(username1.equalsIgnoreCase(""))
					{
						prevEnt = (EntityLivingBase)EntityList.createEntityFromNBT(prevTag, Minecraft.getMinecraft().theWorld);
					}
					else if(username1.equalsIgnoreCase(mc.thePlayer.username))
					{
						prevEnt = mc.thePlayer;
					}
					else
					{
						prevEnt = new EntityOtherPlayerMP(mc.theWorld, username1);
					}
					
					if(username2.equalsIgnoreCase(""))
					{
						nextEnt = (EntityLivingBase)EntityList.createEntityFromNBT(nextTag, Minecraft.getMinecraft().theWorld);
					}
					else if(username2.equalsIgnoreCase(mc.thePlayer.username))
					{
						nextEnt = mc.thePlayer;
					}
					else
					{
						nextEnt = new EntityOtherPlayerMP(mc.theWorld, username2);
					}
					
					ObfHelper.forceSetSize(prevEnt, 0.0F, 0.0F);
					ObfHelper.forceSetSize(nextEnt, 0.0F, 0.0F);
					
					if(prevEnt != mc.thePlayer)
					{
						prevEnt.noClip = true;
					}
					if(nextEnt != mc.thePlayer)
					{
						nextEnt.noClip = true;
					}
					
//					System.out.println(prevEnt);
//					System.out.println(nextEnt);
					
					MorphInfoClient info = new MorphInfoClient(name, prevEnt, nextEnt);
					info.setMorphing(morphing);
					info.morphProgress = morphProg;
					Morph.proxy.tickHandlerClient.playerMorphInfo.put(name, info);

					break;
				}
			}
		}
		catch(IOException e)
		{
		}
	}
}
