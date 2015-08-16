package me.ichun.mods.morph.client.core;

import me.ichun.mods.morph.common.core.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.common.iChunUtil;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();

        iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindAttack);
        iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindUseItem);
        iChunUtil.proxy.registerKeyBind(new KeyBind(Keyboard.KEY_DELETE), null);
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
