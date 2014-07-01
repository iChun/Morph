package morph.common.core;

import cpw.mods.fml.common.FMLCommonHandler;
import ichun.common.core.network.ChannelHandler;
import morph.client.core.TickHandlerClient;
import morph.common.Morph;
import morph.common.packet.*;
import morph.common.thread.ThreadGetOnlineResources;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

public class CommonProxy
{
    public void initMod()
    {
        Morph.parseBlacklist(Morph.config.getString("blacklistedMobs"));
        Morph.parsePlayerList(Morph.config.getString("blackwhitelistedPlayers"));

        Morph.channels = ChannelHandler.getChannelHandlers("Morph", PacketGuiInput.class, PacketMorphInfo.class, PacketSession.class, PacketMorphAcquisition.class, PacketCompleteDemorph.class, PacketMorphStates.class);
    }

    public void initPostMod()
    {
        Iterator ite = EntityList.classToStringMapping.entrySet().iterator();
        while(ite.hasNext())
        {
            Entry e = (Entry)ite.next();
            Class clz = (Class)e.getKey();
            if(EntityLivingBase.class.isAssignableFrom(clz) && !compatibleEntities.contains(clz))
            {
                compatibleEntities.add(clz);
            }
        }
        compatibleEntities.add(EntityPlayer.class);

        (new ThreadGetOnlineResources(Morph.config.getString("customPatchLink"))).start();
    }

    public void initTickHandlers()
    {
        tickHandlerServer = new TickHandlerServer();
        FMLCommonHandler.instance().bus().register(tickHandlerServer);
    }

    public void initCommands(MinecraftServer server)
    {
        ICommandManager manager = server.getCommandManager();
        if(manager instanceof CommandHandler)
        {
            CommandHandler handler = (CommandHandler)manager;
            handler.registerCommand(new CommandMorph());
        }
    }

    public TickHandlerClient tickHandlerClient;
    public TickHandlerServer tickHandlerServer;

    public ArrayList<Class> compatibleEntities = new ArrayList<Class>();
}
