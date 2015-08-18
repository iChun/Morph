package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.client.core.TickHandlerClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.AbilityHandler;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.thread.ThreadGetResources;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommonProxy
{
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());

        initTickHandlers();

        AbilityHandler.init();
        PlayerMorphHandler.init();

        (new ThreadGetResources(Morph.config.customPatchLink)).start();
    }

    public void init()
    {
    }

    public void postInit()
    {}

    public void initTickHandlers()
    {
        tickHandlerServer = new TickHandlerServer();
        FMLCommonHandler.instance().bus().register(tickHandlerServer);
    }

    public TickHandlerServer tickHandlerServer;
    public TickHandlerClient tickHandlerClient;
}
