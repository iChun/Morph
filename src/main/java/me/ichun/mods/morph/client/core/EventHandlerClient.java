package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketRequestMorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

public class EventHandlerClient
{
    public PlayerMorphData morphData;
    public HudHandler hudHandler;

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
    public void onRenderNameplate(RenderNameplateEvent event)
    {
        if(MorphRenderHandler.denyRenderNameplate || event.getEntity().getPersistentData().contains(MorphVariant.NBT_PLAYER_ID) && !MorphRenderHandler.isRenderingMorph)
        {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) //TODO network player Infos during player list
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
        setPlayerMorphData(null);
    }

    public void handleInput(KeyBind keyBind, boolean isReleased)
    {
        hudHandler.handleInput(keyBind, isReleased);
    }

    public void updateMorph(MorphVariant variant) //TODO if I ever remove morphs, ensure to update the selector.
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

            Collections.sort(morphData.morphs); //sort in order of name.
        }
        else
        {
            Morph.LOGGER.error("We got morph data but we don't have the save data! Variant: {}", variant.id);
        }
    }

    public void setPlayerMorphData(PlayerMorphData playerMorphData)
    {
        morphData = playerMorphData;

        if(morphData == null) //disconnected, do cleanup
        {
            if(hudHandler != null)
            {
                hudHandler.destroy();

                MinecraftForge.EVENT_BUS.unregister(hudHandler);
                hudHandler = null;
            }
        }
        else
        {
            Collections.sort(morphData.morphs); //sort in order of name.

            if(hudHandler == null)
            {
                hudHandler = new HudHandler(Minecraft.getInstance(), morphData);
                MinecraftForge.EVENT_BUS.register(hudHandler);
            }
            else
            {
                hudHandler.update(morphData);
            }
        }
    }
}
