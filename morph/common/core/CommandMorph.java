package morph.common.core;

import java.util.ArrayList;
import java.util.List;

import morph.common.Morph;
import morph.common.morph.MorphHandler;
import morph.common.morph.MorphInfo;
import morph.common.morph.MorphState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.network.PacketDispatcher;

public class CommandMorph extends CommandBase
{

	@Override
	public String getCommandName() 
	{
		return "morph";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) 
	{
		return "/" + this.getCommandName() + "<demorph|clear|morphtargetr> [player] [force (true/false)]";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] args) 
	{
		if(args.length > 0)
		{
			if(args[0].equalsIgnoreCase("demorph"))
			{
				EntityPlayer player;
				if(args.length > 1)
				{
					player = PlayerSelector.matchOnePlayer(icommandsender, args[1]);
				}
				else
				{
					player = getCommandSenderAsPlayer(icommandsender);
				}
		        if (player == null)
		        {
		        	player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1]);
		        }
				if(player != null)
				{
					MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
					
					MorphState state1;
					
					MorphState state2 = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.username);
					
					if(info != null)
					{
						state1 = info.nextState;
						MorphInfo info2 = new MorphInfo(player.username, state1, state2);
						info2.setMorphing(true);
						
						Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.username, info2);
						
						PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());
						
						player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
						
						notifyAdmins(icommandsender, "Forcing " + args[1] + " to demorph");
					}
					else
					{
						icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(args[1] + " is not in morph!"));
					}
				}
				else if(args.length > 2 && args[2].equalsIgnoreCase("true"))
				{
					if(Morph.proxy.tickHandlerServer.saveData.hasKey(args[1] + "_morphData"))
					{
						Morph.proxy.tickHandlerServer.saveData.removeTag(args[1] + "_morphData");
						
						MorphState state = Morph.proxy.tickHandlerServer.getSelfState(DimensionManager.getWorld(0), args[1]);
						
						MorphInfo info = new MorphInfo(args[1], state, state);
						info.morphProgress = 80;
						
						PacketDispatcher.sendPacketToAllPlayers(info.getMorphInfoAsPacket());
						
						Morph.proxy.tickHandlerServer.playerMorphInfo.remove(args[1]);
						
						notifyAdmins(icommandsender, "Forcing " + args[1] + " to demorph");
					}
					else
					{
						icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(args[1] + " has no morph data!"));
					}
				}
				else
				{
					icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(args[1] + " is not online!"));
				}
			}
			else if(args[0].equalsIgnoreCase("clear"))
			{
				EntityPlayer player;
				if(args.length > 1)
				{
					player = PlayerSelector.matchOnePlayer(icommandsender, args[1]);
				}
				else
				{
					player = getCommandSenderAsPlayer(icommandsender);
				}
		        if (player == null)
		        {
		        	player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1]);
		        }
				if(player != null)
				{
					MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
					
					MorphState state1;
					
					MorphState state2 = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.username);
					
					if(info != null)
					{
						state1 = info.nextState;
						MorphInfo info2 = new MorphInfo(player.username, state1, state2);
						info2.setMorphing(true);
						
						Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.username, info2);
						
						PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());
						
						player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
					}
					Morph.proxy.tickHandlerServer.playerMorphs.remove(player.username);
					
					MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, null, true);
					
					notifyAdmins(icommandsender, "Clearing " + args[1] + "'s morphs");
				}
				else
				{
					if(Morph.proxy.tickHandlerServer.saveData.hasKey(args[1] + "_morphData"))
					{
						Morph.proxy.tickHandlerServer.saveData.removeTag(args[1] + "_morphData");
						
						MorphState state = Morph.proxy.tickHandlerServer.getSelfState(DimensionManager.getWorld(0), args[1]);
						
						MorphInfo info = new MorphInfo(args[1], state, state);
						info.morphProgress = 80;
						
						PacketDispatcher.sendPacketToAllPlayers(info.getMorphInfoAsPacket());
						
						Morph.proxy.tickHandlerServer.playerMorphInfo.remove(args[1]);
					}
					Morph.proxy.tickHandlerServer.playerMorphs.remove(args[1]);
					if(Morph.proxy.tickHandlerServer.saveData.hasKey(args[1] + "_morphStatesCount"))
					{
						Morph.proxy.tickHandlerServer.saveData.removeTag(args[1] + "_morphStatesCount");
					}
					notifyAdmins(icommandsender, "Clearing " + args[1] + "'s morphs");
				}
			}
			else if(args[0].equalsIgnoreCase("morphtarget"))
			{
				EntityPlayerMP player;
				if(args.length > 1)
				{
					player = PlayerSelector.matchOnePlayer(icommandsender, args[1]);
				}
				else
				{
					player = getCommandSenderAsPlayer(icommandsender);
				}
		        if (player == null)
		        {
		        	player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[1]);
		        }
		        if(player != null)
		        {
		        	MovingObjectPosition mop = EntityHelper.getEntityLook(player, 4D, false, 1.0F);
		        	if(mop != null && mop.entityHit != null && mop.entityHit instanceof EntityLivingBase)
		        	{
		        		if(!EntityHelper.morphPlayer(player, (EntityLivingBase)mop.entityHit, false))
		        		{
		        			icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(player.username + " is not looking at a morphable mob."));
		        		}
		        		else
		        		{
		        			notifyAdmins(icommandsender, "Forcing " + player.username + " to morph into it's target.");
		        		}
		        	}
		        	else
		        	{
		        		icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d(player.username + " is not looking at a morphable mob."));
		        	}
		        }
		        else if(args.length > 1)
		        {
		        	icommandsender.sendChatToPlayer(ChatMessageComponent.func_111066_d("Cannot find player: " + args[1]));
		        }
			}
		}
		else
		{
			throw new WrongUsageException(getCommandUsage(icommandsender), new Object[0]);
		}
	}

	@Override
    public List addTabCompletionOptions(ICommandSender ics, String[] args)
    {
		if(args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, new String[] { "demorph", "clear", "morphtarget" });
		}
		else if (args.length == 2)
		{
			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}
		else if(args.length == 3 && args[0].equalsIgnoreCase("demorph"))
		{
			ArrayList list = new ArrayList();
			list.add("true");
			return list;
		}
        return null;
    }
	
}
