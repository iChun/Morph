package me.ichun.mods.morph.common.handler;

import me.ichun.mods.morph.api.IApi;
import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.client.render.RenderMorph;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerMorphHandler implements IApi
{
    private static final PlayerMorphHandler INSTANCE = new PlayerMorphHandler();

    public static PlayerMorphHandler getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        MorphApi.setApiImpl(INSTANCE);
    }

    @Override
    public boolean canPlayerMorph(EntityPlayer player)
    {
        return true; //TODO check the Morph classic config here in the future.
    }

    @Override
    public boolean hasMorph(String playerName, Side side)
    {
        return side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName) || Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName);
    }

    @Override
    public float morphProgress(String playerName, Side side)
    {
        if(side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName))
        {
            return Morph.proxy.tickHandlerClient.morphsActive.get(playerName).morphTime / (float)Morph.config.morphTime;
        }
        else if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName))
        {
            return Morph.proxy.tickHandlerServer.morphsActive.get(playerName).morphTime / (float)Morph.config.morphTime;
        }
        return 1.0F;
    }

    @Override
    public float timeToCompleteMorph()
    {
        return Morph.config.morphTime;
    }

    @Override
    public EntityLivingBase getPrevMorphEntity(String playerName, Side side)
    {
        if(side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerClient.morphsActive.get(playerName);
            if(info != null && info.prevState != null)
            {
                return info.prevState.entInstance;
            }
        }
        else if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(playerName);
            if(info != null && info.prevState != null)
            {
                return info.prevState.entInstance;
            }
        }
        return null;
    }

    @Override
    public EntityLivingBase getMorphEntity(String playerName, Side side)
    {
        if(side.isClient() && Morph.proxy.tickHandlerClient.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerClient.morphsActive.get(playerName);
            if(info != null)
            {
                return info.nextState.entInstance;
            }
        }
        else if(Morph.proxy.tickHandlerServer.morphsActive.containsKey(playerName))
        {
            MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(playerName);
            if(info != null)
            {
                return info.nextState.entInstance;
            }
        }
        return null;
    }

    @Override
    public boolean forceDemorph(EntityPlayerMP player)
    {
        //TODO this
        return false;
    }

    @Override
    public boolean forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph)
    {
        //TODO this, clearly
        return false;
    }

    @Override
    public boolean acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire, boolean forceMorph, boolean killEntityClientside)
    {
        //TODO this
        if(killEntityClientside)
        {

        }
        return false;
    }

    @Override
    public ResourceLocation getMorphSkinTexture()
    {
        return RenderMorph.morphSkin;
    }

    @Override
    public boolean isMorphApi()
    {
        return true;
    }
}
