package me.ichun.mods.morph.client.core;

import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TickHandlerClient
{
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

    public HashMap<String, MorphInfoClient> morphsActive = new HashMap<String, MorphInfoClient>();
    public LinkedHashMap<String, ArrayList<MorphState>> playerMorphs = new LinkedHashMap<String, ArrayList<MorphState>>(); //LinkedHashMap maintains insertion order.
}
