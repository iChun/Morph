package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.keybind.KeyBind;
import me.ichun.mods.ichunutil.common.iChunUtil;
import me.ichun.mods.morph.client.entity.EntityMorphAcquisition;
import me.ichun.mods.morph.client.render.RenderMorphAcquisition;
import me.ichun.mods.morph.client.render.RenderPlayerHand;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.core.ProxyCommon;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.lwjgl.input.Keyboard;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        Morph.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(Morph.eventHandlerClient);

        Morph.eventHandlerClient.renderHandInstance = new RenderPlayerHand();
        RenderingRegistry.registerEntityRenderingHandler(EntityMorphAcquisition.class, new RenderMorphAcquisition.RenderFactory());

        iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindAttack);
        iChunUtil.proxy.registerMinecraftKeyBind(Minecraft.getMinecraft().gameSettings.keyBindUseItem);
        iChunUtil.proxy.registerKeyBind(new KeyBind(Keyboard.KEY_DELETE), null);
    }
}
