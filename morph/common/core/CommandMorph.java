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
import net.minecraft.util.EnumChatFormatting;
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
		return "/" + this.getCommandName() + " help";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] args) 
	{
		if(args.length > 0)
		{
			if(args[0].equalsIgnoreCase("help"))
			{
//				<demorph|clear|morphtarget> [player] [force (true/false)]
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph demorph [player] [force demorph even if offline (true/false)] - Makes a player demorph"));
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph clear [player] - Clears a player's morphs"));
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph morphtarget [player] - Tries to make the player morph the mob they're looking at"));
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph whitelist <player> - Adds a player to the morph skill whitelist, turning it on"));
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph unwhitelist <player> - Removes a player from the morph skill whitelist"));
			}
			else if(args[0].equalsIgnoreCase("demorph"))
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
					if(EntityHelper.demorphPlayer(player))
					{
						notifyAdmins(icommandsender, "Forcing " + player.username + " to demorph");
					}
					else
					{
						icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(player.username + " is not in morph!"));
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
						icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(args[1] + " has no morph data!"));
					}
				}
				else
				{
					icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(args[1] + " is not online!"));
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
						if(!EntityHelper.morphPlayer(player, (EntityLivingBase)mop.entityHit, false, true))
						{
							icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(player.username + " is not looking at a morphable mob."));
						}
						else
						{
							notifyAdmins(icommandsender, "Forcing " + player.username + " to morph into it's target.");
						}
					}
					else
					{
						icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(player.username + " is not looking at a morphable mob."));
					}
				}
				else if(args.length > 1)
				{
					icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Cannot find player: " + args[1]));
				}
			}
			else if(args[0].equalsIgnoreCase("whitelist"))
			{
				if(args.length == 1)
				{
					icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph whitelist <player> - Adds a player to the morph skill whitelist, turning it on"));
				}
				else
				{
					StringBuilder sb = new StringBuilder();
					for(int i = 1; i < args.length; i++)
					{
						sb.append(args[i]);
						if(i < args.length - 1)
						{
							sb.append(" ");
						}
					}
					if(!Morph.whitelistedPlayerNames.contains(sb.toString().trim()))
					{
						notifyAdmins(icommandsender, "Whitelisting " + sb.toString().trim() + " to use the Morph Skill.");
						Morph.whitelistedPlayerNames.add(sb.toString().trim());
						
						StringBuilder sb1 = new StringBuilder();
						for(int i = 0; i < Morph.whitelistedPlayerNames.size(); i++)
						{
							sb1.append(Morph.whitelistedPlayerNames.get(i));
							if(i < Morph.whitelistedPlayerNames.size() - 1)
							{
								sb1.append(", ");
							}
						}
						Morph.whitelistedPlayers = sb1.toString();
						
						Morph.saveConfig();
					}
					else
					{
						icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Player already whitelisted"));
					}
				}
			}
			else if(args[0].equalsIgnoreCase("unwhitelist"))
			{
				if(args.length == 1)
				{
					icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText(EnumChatFormatting.GRAY.toString() + "/morph unwhitelist <player> - Removes a player from the morph skill whitelist"));
				}
				else
				{
					StringBuilder sb = new StringBuilder();
					for(int i = 1; i < args.length; i++)
					{
						sb.append(args[i]);
						if(i < args.length - 1)
						{
							sb.append(" ");
						}
					}
					if(Morph.whitelistedPlayerNames.contains(sb.toString().trim()))
					{
						notifyAdmins(icommandsender, "Unwhitelisting " + sb.toString().trim() + " from using the Morph Skill.");
						Morph.whitelistedPlayerNames.remove(sb.toString().trim());
						
						StringBuilder sb1 = new StringBuilder();
						for(int i = 0; i < Morph.whitelistedPlayerNames.size(); i++)
						{
							sb1.append(Morph.whitelistedPlayerNames.get(i));
							if(i < Morph.whitelistedPlayerNames.size() - 1)
							{
								sb1.append(", ");
							}
						}
						Morph.whitelistedPlayers = sb1.toString();
						
						Morph.saveConfig();
					}
					else
					{
						icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Player is not on whitelist"));
					}
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
