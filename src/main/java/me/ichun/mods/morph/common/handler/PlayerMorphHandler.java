package me.ichun.mods.morph.common.handler;

import me.ichun.mods.morph.api.IApi;
import me.ichun.mods.morph.api.MorphApi;
import me.ichun.mods.morph.common.Morph;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
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
    public boolean hasMorph(String playerName, Side side)
    {
        return false;
    }

    @Override
    public float morphProgress(String playerName, Side side)
    {
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
        return null;
    }

    @Override
    public EntityLivingBase getMorphEntity(String playerName, Side side)
    {
        return null;
    }

    @Override
    public void forceDemorph(EntityPlayerMP player)
    {
    }

    @Override
    public void forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph)
    {
    }

    @Override
    public void acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire)
    {
    }

    @Override
    public boolean isMorphApi()
    {
        return true;
    }
}
