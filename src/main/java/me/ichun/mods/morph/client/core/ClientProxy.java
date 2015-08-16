package me.ichun.mods.morph.client.core;

import me.ichun.mods.morph.common.core.CommonProxy;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();
    }

    @Override
    public void init()
    {
        super.init();

        //        tickHandlerClient.renderMorphInstance = new RenderMorph(new ModelMorph(), 0.0F);
        //        RenderingRegistry.registerEntityRenderingHandler(EntityMorphAcquisition.class, tickHandlerClient.renderMorphInstance);
    }

    @Override
    public void postInit()
    {
        super.postInit();
    }

    @Override
    public void initTickHandlers()
    {
        super.initTickHandlers();

        tickHandlerClient = new TickHandlerClient();
        FMLCommonHandler.instance().bus().register(tickHandlerClient);
    }
}
