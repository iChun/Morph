package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.render.HudRenderer;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketRequestMorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class EventHandlerClient
{
    public PlayerMorphData morphData;
    public HudRenderer hudRenderer;

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event)
    {
        if(MorphRenderHandler.isRenderingMorph) //we're rendering a player morph, forgetaboutit
        {
            return;
        }

        MorphRenderHandler.restoreShadowSize(event.getRenderer());

        PlayerEntity player = event.getPlayer();
        if(!player.removed)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(player);
            if(!info.requested)
            {
                Morph.channel.sendToServer(new PacketRequestMorphInfo(player.getGameProfile().getId()));
                info.requested = true;
            }
            else if(info.isMorphed())
            {
                event.setCanceled(true);

                MorphRenderHandler.renderMorphInfo(player, info, event.getMatrixStack(), event.getBuffers(), event.getLight(), event.getPartialRenderTick());

                MorphRenderHandler.setShadowSize(event.getRenderer(), info, event.getPartialRenderTick());
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && Minecraft.getInstance().player != null && !Minecraft.getInstance().player.removed)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(Minecraft.getInstance().player);
            if(info.isMorphed() && (info.getMorphProgress(event.renderTickTime) < 1F || Morph.configServer.aggressiveSizeRecalculation)) //is morphing
            {
                Minecraft.getInstance().player.eyeHeight = info.getMorphEyeHeight(event.renderTickTime);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(!event.player.removed && event.player == Minecraft.getInstance().player && event.player.ticksExisted == 10)
        {
            MorphInfo info = MorphHandler.INSTANCE.getMorphInfo(event.player);
            if(!info.requested)
            {
                Morph.channel.sendToServer(new PacketRequestMorphInfo(event.player.getGameProfile().getId()));
                info.requested = true;
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent event)
    {
        morphData = null;
        if(hudRenderer != null)
        {
            MinecraftForge.EVENT_BUS.unregister(hudRenderer);
            hudRenderer = null;
        }
    }

    public void handleInput(KeyBind keyBind, boolean isReleased)
    {
        if(hudRenderer == null)
        {
            hudRenderer = new HudRenderer();
            MinecraftForge.EVENT_BUS.register(hudRenderer);
        }

        hudRenderer.handleInput(keyBind, isReleased);
    }

    public void updateMorph(MorphVariant variant)
    {
        if(morphData != null)
        {
            boolean added = false;

            ArrayList<MorphVariant> morphs = morphData.morphs;
            for(int i = 0; i < morphs.size(); i++)
            {
                MorphVariant morph = morphs.get(i);
                if(morph.id.equals(variant.id))
                {
                    morphs.remove(i);
                    morphs.add(i, variant);
                    added = true;
                    break;
                }
            }

            if(!added) //presume a new morph
            {
                morphs.add(variant);
            }

            //TODO update if the selector is open
        }
        else
        {
            Morph.LOGGER.error("We got morph data but we don't have the save data! Variant: {}", variant.id);
        }
    }
}
