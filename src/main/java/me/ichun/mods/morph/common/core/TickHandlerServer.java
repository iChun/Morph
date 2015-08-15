package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphVariant;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class TickHandlerServer
{
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            for(MorphInfo info : Morph.proxy.tickHandlerServer.morphsActive.values())
            {
                info.tick();
            }
        }
    }

    public HashMap<String, MorphInfo> morphsActive = new HashMap<String, MorphInfo>(); //These are the active morphs. Entity instance are retreived from here
    public HashMap<String, ArrayList<MorphVariant>> playerMorphs = new HashMap<String, ArrayList<MorphVariant>>();//These are the available morphs for each player. No entity instance is required or created here.
}
