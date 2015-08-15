package me.ichun.mods.morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;

public class ApiDummy implements IApi
{
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
        return 80; //defaults. May be changed via config.
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
    public void forceDemorph(EntityPlayerMP player){}

    @Override
    public void forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph){}

    @Override
    public void acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire){}

    @Override
    public boolean isMorphApi()
    {
        return false;
    }
}
