package me.ichun.mods.morph.client.core;

import me.ichun.mods.ichunutil.client.key.KeyBind;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.api.morph.MorphVariant;
import me.ichun.mods.morph.client.render.MorphRenderHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.MorphInfoImpl;
import me.ichun.mods.morph.common.morph.save.PlayerMorphData;
import me.ichun.mods.morph.common.packet.PacketRequestMorphInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
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

        PlayerEntity player = event.getPlayer();

        //Disables the render of this player if this player is riding the render view entity and the game is in first person
        if(Morph.configClient.morphDisableRidingPlayerRenderInFirstPerson && player.getRidingEntity() == Minecraft.getInstance().getRenderViewEntity() && Minecraft.getInstance().gameSettings.getPointOfView().equals(PointOfView.FIRST_PERSON))
        {
            event.setCanceled(true);
            return;
        }

        MorphRenderHandler.restoreShadowSize(event.getRenderer());

        if(!player.removed)
        {
            MorphInfoImpl info = (MorphInfoImpl)MorphHandler.INSTANCE.getMorphInfo(player);
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
        setPlayerMorphData(null);
    }

    public void handleInput(KeyBind keyBind, boolean isReleased)
    {
        hudHandler.handleInput(keyBind, isReleased);
    }

    public void updateMorph(MorphVariant variant)
    {
        if(morphData != null)
        {
            boolean handled = false;

            ArrayList<MorphVariant> morphs = morphData.morphs;
            for(int i = 0; i < morphs.size(); i++)
            {
                MorphVariant morph = morphs.get(i);
                if(morph.id.equals(variant.id))
                {
                    morphs.remove(i);
                    if(variant.hasVariants())
                    {
                        morphs.add(i, variant);
                    }
                    handled = true;
                    break;
                }
            }

            if(!handled && variant.hasVariants()) //presume a new morph
            {
                morphs.add(variant);
            }

            hudHandler.updateMorphs();

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
            morphData.morphs.removeIf(morph -> !morph.hasVariants()); //we remove those that don't have variants, don't care no more

            Collections.sort(morphData.morphs); //sort in order of name.

            if(hudHandler == null)
            {
                hudHandler = new HudHandler(Minecraft.getInstance(), morphData);
                MinecraftForge.EVENT_BUS.register(hudHandler);
            }
            else
            {
                hudHandler.updateBiomass(morphData);
                hudHandler.updateMorphs();
            }
        }
    }
}
