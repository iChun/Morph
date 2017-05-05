package me.ichun.mods.morph.common.core;

import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.AbilityHandler;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.packet.*;
import me.ichun.mods.morph.common.thread.ThreadGetResources;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    public void preInit()
    {
        Morph.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(Morph.eventHandlerServer);

        AbilityHandler.init();
        PlayerMorphHandler.init();

        (new ThreadGetResources(Morph.config.customPatchLink)).start();

        ResourceLocation rs = new ResourceLocation("morph", "morph");
        Morph.soundMorph = GameRegistry.register(new SoundEvent(rs).setRegistryName(rs));

        Morph.channel = new PacketChannel(Morph.MOD_NAME, PacketUpdateMorphList.class, PacketUpdateActiveMorphs.class, PacketGuiInput.class, PacketDemorph.class, PacketAcquireEntity.class);
    }
}
