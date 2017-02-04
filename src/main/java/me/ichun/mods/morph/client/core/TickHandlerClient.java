package me.ichun.mods.morph.client.core;

import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.client.render.RenderMorph;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TickHandlerClient
{
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.phase == TickEvent.Phase.START)
        {
            if(mc.theWorld == null && (!morphsActive.isEmpty() || !playerMorphs.isEmpty()))
            {
                //world is null, not connected to any worlds, get rid of objects for GC.
                morphsActive.clear();
                playerMorphs.clear();
            }
        }
        else
        {
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(!mc.isGamePaused())
            {
                for(MorphInfoClient info : Morph.proxy.tickHandlerClient.morphsActive.values())
                {
                    info.tick();
                }
            }
        }
    }

    public RenderMorph renderMorphInstance;

    public HashMap<String, MorphInfoClient> morphsActive = new HashMap<String, MorphInfoClient>(); //Current morphs per-player
    public LinkedHashMap<String, ArrayList<MorphState>> playerMorphs = new LinkedHashMap<String, ArrayList<MorphState>>(); //Minecraft Player's available morphs. LinkedHashMap maintains insertion order.
}
