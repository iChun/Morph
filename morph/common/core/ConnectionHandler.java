package morph.common.core;

import java.util.ArrayList;
import java.util.Map.Entry;

import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class ConnectionHandler
	implements IConnectionHandler, IPlayerTracker
{

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) //client: remove server
	{
		onClientConnected();
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) //client: local server
	{
		onClientConnected();
	}

	public void onClientConnected()
	{
		Morph.proxy.tickHandlerClient.playerMorphInfo.clear();
		Morph.proxy.tickHandlerClient.playerMorphStates.clear();
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) //server
	{
		return null;
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) //client
	{
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) //server
	{

	}

	@Override
	public void connectionClosed(INetworkManager manager) //both 
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			Morph.proxy.tickHandlerClient.playerMorphInfo.clear();
			Morph.proxy.tickHandlerClient.playerMorphStates.clear();
		}
	}

	//IPlayerTracker area

	@Override
	public void onPlayerLogin(EntityPlayer player) 
	{
		ArrayList list = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username);
		
		if(Morph.proxy.tickHandlerServer.saveData != null)
		{
			NBTTagCompound tag = Morph.proxy.tickHandlerServer.saveData;
			
			list.clear();
			
			list.add(0, new MorphState(player.worldObj, player.username, player.username, null, player.worldObj.isRemote));
			
			int count = tag.getInteger(player.username + "_morphStatesCount");
			if(count > 0)
			{
				
				for(int i = 0; i < count; i++)
				{
					MorphState state = new MorphState(player.worldObj, player.username, player.username, null, false);
					state.readTag(player.worldObj, tag.getCompoundTag(player.username + "_morphState" + i));
					if(!state.identifier.equalsIgnoreCase(""))
					{
						MorphHandler.addOrGetMorphState(list, state);
					}
				}
			}
			
			NBTTagCompound tag1 = tag.getCompoundTag(player.username + "_morphData");
			if(tag1.hasKey("playerName"))
			{
				MorphInfo info = new MorphInfo();
				info.readNBT(tag1);
				Morph.proxy.tickHandlerServer.playerMorphInfo.put(info.playerName, info);
				MorphHandler.addOrGetMorphState(list, info.nextState);
			}
		}
		
		MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, null, true);
		for(Entry<String, MorphInfo> e : Morph.proxy.tickHandlerServer.playerMorphInfo.entrySet())
		{
			PacketDispatcher.sendPacketToPlayer(e.getValue().getMorphInfoAsPacket(), (Player)player);
		}
		
		MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

		if(info != null)
		{
			ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
			player.setPosition(player.posX, player.posY, player.posZ);
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) 
	{
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) 
	{
		MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

		if(info != null)
		{
			ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
			player.setPosition(player.posX, player.posY, player.posZ);
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) 
	{
		MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

		if(info != null)
		{
			ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
			player.setPosition(player.posX, player.posY, player.posZ);
		}
	}

}
