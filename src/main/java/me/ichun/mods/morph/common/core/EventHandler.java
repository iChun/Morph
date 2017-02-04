package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.ichun.mods.ichunutil.common.core.event.RendererSafeCompatibilityEvent;

import java.util.Map;

public class EventHandler
{
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRendererSafeCompatibility(RendererSafeCompatibilityEvent event)
    {
        for(Object obj : Minecraft.getMinecraft().getRenderManager().entityRenderMap.entrySet())
        {
            Map.Entry e = (Map.Entry)obj;
            Class clz = (Class)e.getKey();
            Render rend = (Render)e.getValue();
            if(EntityLivingBase.class.isAssignableFrom(clz))
            {
                ModelHandler.dissectForModels(clz, rend);
            }
        }
    }

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

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entityLiving.worldObj.isRemote)
        {
            if(event.source.getEntity() instanceof EntityPlayerMP && event.entityLiving != event.source.getEntity())
            {
                EntityPlayerMP player = (EntityPlayerMP)event.source.getEntity();

                if(PlayerMorphHandler.getInstance().canPlayerMorph(player))
                {
                    EntityLivingBase living = event.entityLiving; //entity to acquire

                    if(event.entityLiving instanceof EntityPlayerMP)
                    {
                        EntityPlayerMP player1 = (EntityPlayerMP)event.entityLiving;

                        //TODO get the morph that the player was using.
/*
                        MorphInfo info = Morph.proxy.tickHandlerServer.getPlayerMorphInfo(player1);
                        if(info != null)
                        {
                            if(info.getMorphing())
                            {
                                living = info.prevState.entInstance;
                            }
                            else
                            {
                                living = info.nextState.entInstance;
                            }
                        }
*/
                    }

                    PlayerMorphHandler.getInstance().acquireMorph(player, living, Morph.config.instaMorph == 1, true);
                }
            }
        }
    }
}
