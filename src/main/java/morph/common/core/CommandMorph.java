package morph.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import ichun.common.core.EntityHelperBase;
import ichun.common.core.network.PacketHandler;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

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
                icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.demorph").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
                icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.clear").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
                icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.morphtarget").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
                icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.addtolist").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
                icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.removefromlist").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
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
                    player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[1]);
                }
                if(player != null)
                {
                    if(EntityHelper.demorphPlayer(player))
                    {
                        func_152373_a(icommandsender, this, "morph.command.forcingDemorph", player.getCommandSenderName());
                    }
                    else
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.notInMorph", player.getCommandSenderName()));
                    }
                }
                else if(args.length > 2 && args[2].equalsIgnoreCase("true"))
                {
                    try
                    {
                        EntityPlayerMP player1 = new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), DimensionManager.getWorld(0), EntityHelperBase.getSimpleGameProfileFromName(args[1]), new ItemInWorldManager(DimensionManager.getWorld(0)));
                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().readPlayerDataFromFile(player1);
                        if(Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).hasKey("morphData"))
                        {
                            MorphState state = Morph.proxy.tickHandlerServer.getSelfState(DimensionManager.getWorld(0), player1);

                            MorphInfo info = new MorphInfo(args[1], state, state);
                            info.morphProgress = 80;
                            info.healthOffset = Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).getDouble("healthOffset");

                            Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).removeTag("morphData");

                            PacketHandler.sendToAll(Morph.channels, info.getMorphInfoAsPacket());

                            Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player1, null);

                            func_152373_a(icommandsender, this, "morph.command.forcingDemorph", args[1]);

                            //Workaround to force save.
                            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.add(player1);
                            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().saveAllPlayerData();
                            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.remove(player1);
                        }
                        else
                        {
                            icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.noMorphData", args[1]));
                        }
                    }
                    catch(Exception e)
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.cannotReadMorphData", args[1]));
                    }
                }
                else
                {
                    icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.notOnline", args[1]));
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
                    player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[1]);
                }
                if(player != null)
                {
                    MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player);

                    MorphState state1;

                    MorphState state2 = Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player);

                    if(info != null)
                    {
                        state1 = info.nextState;
                        MorphInfo info2 = new MorphInfo(player.getCommandSenderName(), state1, state2);
                        info2.setMorphing(true);
                        info2.healthOffset = info.healthOffset;
                        info2.preMorphHealth = player.getHealth();

                        Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player, info2);

                        PacketHandler.sendToAll(Morph.channels, info2.getMorphInfoAsPacket());

                        player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
                    }
                    Morph.proxy.tickHandlerServer.removeAllPlayerMorphsExcludingCurrentMorph(player);

                    MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP)player, null, true);

                    func_152373_a(icommandsender, this, "morph.command.clearingMorphs", args[1]);
                }
                else
                {
                    try
                    {
                        EntityPlayerMP player1 = new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), DimensionManager.getWorld(0), EntityHelperBase.getSimpleGameProfileFromName(args[1]), new ItemInWorldManager(DimensionManager.getWorld(0)));
                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().readPlayerDataFromFile(player1);

                        if(Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).hasKey("morphData"))
                        {
                            MorphState state = Morph.proxy.tickHandlerServer.getSelfState(DimensionManager.getWorld(0), player1);

                            MorphInfo info = new MorphInfo(args[1], state, state);
                            info.morphProgress = 80;
                            info.healthOffset = Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).getDouble("healthOffset");

                            Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).removeTag("morphData");

                            PacketHandler.sendToAll(Morph.channels, info.getMorphInfoAsPacket());

                            Morph.proxy.tickHandlerServer.setPlayerMorphInfo(player1, null);
                        }
                        Morph.proxy.tickHandlerServer.removeAllPlayerMorphsExcludingCurrentMorph(player1);
                        if(Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).hasKey("morphStatesCount"))
                        {
                            Morph.proxy.tickHandlerServer.getMorphDataFromPlayer(player1).removeTag("morphStatesCount");
                        }
                        func_152373_a(icommandsender, this, "morph.command.clearingMorphs", args[1]);

                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.add(player1);
                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().saveAllPlayerData();
                        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList.remove(player1);
                    }
                    catch(Exception e)
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.failToClear", args[1]));
                    }
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
                    player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[1]);
                }
                if(player != null)
                {
                    MovingObjectPosition mop = EntityHelper.getEntityLook(player, 4D, false, 1.0F);
                    if(mop != null && mop.entityHit != null && mop.entityHit instanceof EntityLivingBase)
                    {
                        EntityLivingBase living = (EntityLivingBase)mop.entityHit;

                        if(living instanceof EntityPlayerMP)
                        {
                            EntityPlayerMP player1 = (EntityPlayerMP)living;

                            MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player1);
                            if(info != null)
                            {
                                if(info.getMorphing())
                                {
                                    living = info.prevState.entInstance;
                                }
                                else
                                {
                                    living = info.nextState.entInstance;
                                }
                            }
                        }

                        if(!EntityHelper.morphPlayer(player, living, false, true))
                        {
                            icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.notLookingAtMorphable", player.getCommandSenderName()));
                        }
                        else
                        {
                            func_152373_a(icommandsender, this, "morph.command.forcingMorphTarget", player.getCommandSenderName());
                        }
                    }
                    else
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.notLookingAtMorphable", player.getCommandSenderName()));
                    }
                }
                else if(args.length > 1)
                {
                    icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.cannotFindPlayer", args[1]));
                }
            }
            else if(args[0].equalsIgnoreCase("addtolist"))
            {
                if(args.length == 1)
                {
                    icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.addtolist").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
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
                    if(!Morph.playerList.contains(sb.toString().trim()))
                    {
                        func_152373_a(icommandsender, this, "morph.command.addingToBlackWhite", sb.toString().trim());
                        Morph.playerList.add(sb.toString().trim());

                        StringBuilder sb1 = new StringBuilder();
                        for(int i = 0; i < Morph.playerList.size(); i++)
                        {
                            sb1.append(Morph.playerList.get(i));
                            if(i < Morph.playerList.size() - 1)
                            {
                                sb1.append(", ");
                            }
                        }
                        Morph.config.get("blackwhitelistedPlayers").set(sb1.toString());
                        Morph.config.save();
                    }
                    else
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.alreadyInList"));
                    }
                }
            }
            else if(args[0].equalsIgnoreCase("removefromlist"))
            {
                if(args.length == 1)
                {
                    icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.removefromlist").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
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
                    if(Morph.playerList.contains(sb.toString().trim()))
                    {
                        func_152373_a(icommandsender, this, "morph.command.removingFromBlackWhite", sb.toString().trim());
                        Morph.playerList.remove(sb.toString().trim());

                        StringBuilder sb1 = new StringBuilder();
                        for(int i = 0; i < Morph.playerList.size(); i++)
                        {
                            sb1.append(Morph.playerList.get(i));
                            if(i < Morph.playerList.size() - 1)
                            {
                                sb1.append(", ");
                            }
                        }
                        Morph.config.get("blackwhitelistedPlayers").set(sb1.toString());
                        Morph.config.save();
                    }
                    else
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("morph.command.notInList"));
                    }
                }
            }
        }
        else
        {
            throw new WrongUsageException(getCommandUsage(icommandsender));
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender ics, String[] args)
    {
        if(args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, new String[] { "demorph", "clear", "morphtarget", "addtolist", "removefromlist" });
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
