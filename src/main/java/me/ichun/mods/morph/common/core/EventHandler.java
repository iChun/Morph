package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler
{
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(event.world.isRemote && event.world instanceof WorldClient)
        {
            //Clean up the Morph States and stuff like that to prevent mem leaks.
            for(MorphInfoClient info : Morph.proxy.tickHandlerClient.morphsActive.values())
            {
                info.clean();
            }
        }
    }
}
