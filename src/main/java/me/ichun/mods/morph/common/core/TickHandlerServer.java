package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphVariant;
import me.ichun.mods.morph.common.packet.PacketDemorph;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TickHandlerServer
{
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Iterator<Map.Entry<String, MorphInfo>> ite = Morph.proxy.tickHandlerServer.morphsActive.entrySet().iterator();
            while(ite.hasNext())
            {
                Map.Entry<String, MorphInfo> e = ite.next();
                MorphInfo info = e.getValue();

                info.tick();

                if(!info.isMorphing() && info.nextState.getEntInstance(info.player.worldObj).getCommandSenderName().equals(info.player.getCommandSenderName())) //Player has fully demorphed
                {
                    ite.remove();
                    Morph.channel.sendToAll(new PacketDemorph(info.player.getCommandSenderName()));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.side.isServer() && event.phase == TickEvent.Phase.START)
        {
            if(event.player.worldObj.playerEntities.contains(event.player))
            {
                MorphInfo info = morphsActive.get(event.player.getCommandSenderName());
                if(info != null)
                {
                    info.player = event.player;
                }
            }
        }
    }

    public ArrayList<MorphVariant> getPlayerMorphs(String name)
    {
        ArrayList<MorphVariant> morphs = playerMorphs.get(name);
        if(morphs == null)
        {
            morphs = new ArrayList<MorphVariant>();
            MorphVariant variant = new MorphVariant(MorphVariant.PLAYER_MORPH_ID).setPlayerName(name);
            variant.thisVariant.isFavourite = true;
            morphs.add(variant); //Add the player self's morph variant when getting this list.
            playerMorphs.put(name, morphs);
        }
        return morphs;
    }

    public HashMap<String, MorphInfo> morphsActive = new HashMap<String, MorphInfo>(); //These are the active morphs. Entity instance are retreived from here
    public HashMap<String, ArrayList<MorphVariant>> playerMorphs = new HashMap<String, ArrayList<MorphVariant>>();//These are the available morphs for each player. No entity instance is required or created here.
}
